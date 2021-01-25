package com.android.server.hidata.histream;

import android.content.Context;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.IMonitor;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.arbitration.IHiDataCHRCallBack;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.histream.HwHistreamCHRStatisticsInfo;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class HwHiStreamCHRManager {
    private static final int CHR_CALL_LEVEL1_MAX_PERIOD = 300;
    private static final int CHR_CALL_LEVEL2_MAX_PERIOD = 900;
    private static final int CHR_COLLECT_QOE_AFTER_HANDOVER = 15000;
    private static final int CHR_COLLECT_QOE_PERIOD = 6000;
    private static final int CHR_COLLECT_TUP_PERIOD = 3;
    private static final int CHR_DS_QOE_REPORT_MIN_INTERVAL = 30000;
    private static final int CHR_GET_CUR_RTT_DELAY = 6000;
    private static final int CHR_HANDOVER_EVENT_GET_TUP_DELAY = 8000;
    private static final int CHR_HANDOVER_EVENT_UPLOAD_DELAY = 22000;
    public static final int CHR_MPLINK_FAIL_BIND_FAIL = 8;
    public static final int CHR_MPLINK_FAIL_COEXISTENCE = 2;
    public static final int CHR_MPLINK_FAIL_ENVIRONMENT = 1;
    public static final int CHR_MPLINK_FAIL_HISTRORYQOE = 4;
    public static final int CHR_MPLINK_FAIL_ISBINDING = 6;
    public static final int CHR_MPLINK_FAIL_OTHERS = 20;
    public static final int CHR_MPLINK_FAIL_PINGPONG = 3;
    public static final int CHR_MPLINK_FAIL_QUERY_TIMEOUT = 7;
    public static final int CHR_MPLINK_FAIL_TARGETNETWORK = 5;
    public static final int CHR_MPLINK_FAIL_UNBIND_FAIL = 9;
    public static final int CHR_MPLINK_SUCCESS = 0;
    private static final int CHR_START_COLLECT_PARA_DELAY = 30000;
    private static final int CHR_TRAFFIC_UPDATE_PERIOD = 2;
    private static final int CHR_UPLOAD_COLLECT_PARAMETER_MAX_COUNT = 12;
    private static final int CHR_UPLOAD_EVENT_APPQOE_HISTORY_RECORDS = 909009048;
    private static final int CHR_UPLOAD_EVENT_DOUYIN_STATISTICS = 909002053;
    private static final int CHR_UPLOAD_EVENT_HANDOVER = 909002039;
    private static final int CHR_UPLOAD_EVENT_QOE_PARA_COLLECT = 909002042;
    private static final int CHR_UPLOAD_EVENT_SPACE_INFO = 909009047;
    private static final int CHR_UPLOAD_EVENT_STALL_INFO = 909009035;
    private static final int CHR_UPLOAD_EVENT_WECHAT_AUDIO_STATISTICS = 909002041;
    private static final int CHR_UPLOAD_EVENT_WECHAT_VIDEO_STATISTICS = 909002040;
    private static final int CHR_UPLOAD_HANOVER_EVNET_MAX_COUNT = 20;
    private static final int CHR_UPLOAD_STALL_EVNET_MAX_COUNT = 20;
    public static final int EVENT_CHR_APPEND_MPLINK_TO_WIFI = 6;
    public static final int EVENT_CHR_ERROR_STOP_MPLINK = 7;
    public static final int EVENT_CHR_HANDOVER_TO_CELLULAR = 2;
    public static final int EVENT_CHR_HANDOVER_TO_WIFI = 1;
    public static final int EVENT_CHR_STALL_BEGIN_MPLINK = 8;
    public static final int EVENT_CHR_STALL_MPLINK_TO_CELLULAR = 5;
    public static final int EVENT_CHR_STALL_MPLINK_TO_WIFI = 3;
    public static final int EVENT_CHR_USER_STOP_MPLINK = 9;
    public static final int EVENT_CHR_WIFI_RECOVER_MPLINK_TO_WIFI = 4;
    private static final int USER_MANUALLY_SWITCH_INTERVAL = 3000;
    private static final int USER_MANUALLY_WIFI_ENABLE_INTERVAL = 6000;
    private static HwHiStreamCHRManager mHwHiStreamCHRManager;
    private Handler mCHRManagerHandler;
    private int mCallId = 0;
    private Context mContext;
    private int mCurNetRtt = -1;
    private long mDsQoeLastReportTime = 0;
    private int mHandoverEventId = 0;
    private ArrayList<HwHistreamCHRHandoverInfo> mHandoverInfolist = new ArrayList<>();
    private IHiDataCHRCallBack mIHiDataCHRCallBack;
    private long mLastCollectCellularRttTime = 0;
    private long mLastGetCurRttTime = 0;
    private HistreamWifiInfo mLastWifiInfo = new HistreamWifiInfo();
    private HwHistreamCHRStatisticsInfo mOtherAppStatisticsInfo;
    private int mStallEventId = 0;
    private ArrayList<HwHistreamCHRStallInfo> mStallInfoList = new ArrayList<>();
    private int mUploadCollectParaCount = 0;
    private long mUploadCollectParaStartCountTime = 0;
    private int mUploadHandoverCount = 0;
    private long mUploadHandoverStartCountTime = 0;
    private int mUploadStallCount = 0;
    private long mUploadStallStartCountTime = 0;
    private HwHistreamCHRStatisticsInfo mWechatStatisticsInfo;
    private WifiManager mWifiManager;

    public static class HistreamWifiInfo {
        int mApType = -1;
        int mChLoad = -1;
        int mChannel = -1;
        int mFailRate = -1;
        int mLastTxBad = 0;
        int mLastTxGood = 0;
        int mRssi = -1;
        int mSnr = -1;
        String mSsid = HwAPPQoEUtils.INVALID_STRING_VALUE;
    }

    private HwHiStreamCHRManager(Context context, Handler handler) {
        this.mContext = context;
        this.mCHRManagerHandler = handler;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    public static HwHiStreamCHRManager createInstance(Context context, Handler handler) {
        if (mHwHiStreamCHRManager == null) {
            mHwHiStreamCHRManager = new HwHiStreamCHRManager(context, handler);
        }
        return mHwHiStreamCHRManager;
    }

    public static HwHiStreamCHRManager getInstance() {
        return mHwHiStreamCHRManager;
    }

    public synchronized void registCHRCallback(IHiDataCHRCallBack callback) {
        this.mIHiDataCHRCallBack = callback;
    }

    public void handleUploadStallEventDelay(Message msg) {
        if (msg != null && msg.obj != null) {
            int eventNum = ((Bundle) msg.obj).getInt("eventNum");
            HwHistreamCHRStallInfo curStallEvent = null;
            int pos = 0;
            int size = this.mStallInfoList.size();
            int i = 0;
            while (true) {
                if (i >= size) {
                    break;
                }
                HwHistreamCHRStallInfo stallEvent = this.mStallInfoList.get(i);
                if (stallEvent != null) {
                    if (eventNum == stallEvent.mEventId) {
                        curStallEvent = stallEvent;
                        pos = i;
                        break;
                    }
                    i++;
                } else {
                    return;
                }
            }
            if (curStallEvent != null) {
                if (6000 > System.currentTimeMillis() - this.mLastGetCurRttTime) {
                    curStallEvent.mNetRtt = this.mCurNetRtt;
                }
                Bundle chrBundle = new Bundle();
                chrBundle.putString("mAPKName", curStallEvent.mAPKName);
                chrBundle.putInt("mScenario", curStallEvent.mScenario);
                chrBundle.putInt("mRAT", curStallEvent.mRAT);
                chrBundle.putInt("mUlTup", curStallEvent.mUlTup);
                chrBundle.putInt("mDlTup", curStallEvent.mDlTup);
                chrBundle.putInt("mApRtt", curStallEvent.mApRtt);
                chrBundle.putInt("mNetRtt", curStallEvent.mNetRtt);
                chrBundle.putInt("mCellSig", curStallEvent.mCellSig);
                chrBundle.putInt("mCellRsrq", curStallEvent.mCellRsrq);
                chrBundle.putInt("mCellSinr", curStallEvent.mCellSinr);
                chrBundle.putInt("mNeiborApRssi", curStallEvent.mNeiborApRssi);
                chrBundle.putInt("mWifiSnr", curStallEvent.mWifiSnr);
                chrBundle.putInt("mWifiChload", curStallEvent.mWifiChload);
                chrBundle.putInt("mNetDlTup", curStallEvent.mNetDlTup);
                if (this.mIHiDataCHRCallBack != null) {
                    HwHiStreamUtils.logD(false, "handleUploadStallEventDelay:send stream stall chr event", new Object[0]);
                    this.mIHiDataCHRCallBack.uploadHiDataDFTEvent(CHR_UPLOAD_EVENT_STALL_INFO, chrBundle);
                }
                this.mUploadStallCount++;
                curStallEvent.printCHRStallInfo();
                this.mStallInfoList.remove(pos);
            }
        }
    }

    public void handleUploadHandoverInfo(Message msg) {
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            int eventNum = bundle.getInt("eventNum");
            HwHistreamCHRHandoverInfo curHandoverEvent = null;
            int pos = 0;
            int size = this.mHandoverInfolist.size();
            int i = 0;
            while (true) {
                if (i >= size) {
                    break;
                }
                HwHistreamCHRHandoverInfo handoverEvent = this.mHandoverInfolist.get(i);
                if (handoverEvent != null) {
                    if (eventNum == handoverEvent.mEventId) {
                        curHandoverEvent = handoverEvent;
                        pos = i;
                        break;
                    }
                    i++;
                } else {
                    return;
                }
            }
            if (curHandoverEvent != null) {
                long curTime = System.currentTimeMillis();
                HwHistreamCHRStatisticsInfo appStatisticsInfo = getCurStatisticsInfo();
                if (appStatisticsInfo == null) {
                    curHandoverEvent.printCHRHandoverInfo();
                    sendCHRHandoverEvent(curHandoverEvent);
                    this.mHandoverInfolist.remove(pos);
                } else if (4 == msg.what) {
                    if (800 == appStatisticsInfo.mCurrNetwork) {
                        updateTup(appStatisticsInfo, 800);
                        curHandoverEvent.mWifiRxTupAft = appStatisticsInfo.mWifiRxTup1;
                    } else if (801 == appStatisticsInfo.mCurrNetwork) {
                        updateTup(appStatisticsInfo, 801);
                        curHandoverEvent.mCellRxTup = appStatisticsInfo.mCelluarRxTup1;
                    }
                    HwHiStreamUtils.logD(false, "handleUploadHandoverInfo: update throughput after handover", new Object[0]);
                    Handler handler = this.mCHRManagerHandler;
                    handler.sendMessageDelayed(handler.obtainMessage(5, bundle), 22000);
                } else if (5 == msg.what) {
                    if (HwArbitrationDEFS.WIFI_RX_BYTES_THRESHOLD >= curTime - appStatisticsInfo.mLastBadQoeTime) {
                        curHandoverEvent.mStreamQoeAft = 107;
                    } else {
                        curHandoverEvent.mStreamQoeAft = 106;
                    }
                    curHandoverEvent.printCHRHandoverInfo();
                    sendCHRHandoverEvent(curHandoverEvent);
                    this.mHandoverInfolist.remove(pos);
                }
            }
        }
    }

    public void onCHRAppStateChange(HwAPPStateInfo stateInfo, int appState) {
        HwHistreamCHRStatisticsInfo currAppStatisticsInfo;
        HwHiStreamTraffic mHwHiStreamTraffic = HwHiStreamTraffic.getInstance();
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
        if (mHwHiStreamTraffic != null && mHwHiStreamNetworkMonitor != null && stateInfo != null) {
            int scenceId = stateInfo.mScenceId;
            int appUID = stateInfo.mAppUID;
            int networkType = stateInfo.mNetworkType;
            if (100 == appState) {
                if (isVoipScene(scenceId)) {
                    this.mWechatStatisticsInfo = new HwHistreamCHRStatisticsInfo(HwHiStreamUtils.WECHAT_NAME, scenceId, appUID);
                    currAppStatisticsInfo = this.mWechatStatisticsInfo;
                } else {
                    this.mOtherAppStatisticsInfo = new HwHistreamCHRStatisticsInfo("none", scenceId, appUID);
                    this.mOtherAppStatisticsInfo.mCellLv3Cnt = scenceId;
                    if (!MpLinkCommonUtils.isMpLinkEnabled(this.mContext)) {
                        this.mOtherAppStatisticsInfo.mWiFiLv3Cnt = 4;
                    } else {
                        this.mOtherAppStatisticsInfo.mWiFiLv3Cnt = stateInfo.mUserType;
                    }
                    currAppStatisticsInfo = this.mOtherAppStatisticsInfo;
                }
                long startTime = System.currentTimeMillis();
                currAppStatisticsInfo.mCallStartTime = startTime;
                currAppStatisticsInfo.mStartCellularTraffic = mHwHiStreamTraffic.getTotalTraffic(0, startTime, appUID, 801);
                currAppStatisticsInfo.mCurrNetwork = networkType;
                if (800 == networkType) {
                    currAppStatisticsInfo.mStartInWiFiCnt = 1;
                    currAppStatisticsInfo.mLastWifiTime = startTime;
                } else if (801 == networkType) {
                    currAppStatisticsInfo.mStartInCellularCnt = 1;
                    currAppStatisticsInfo.mLastCellTime = startTime;
                }
                currAppStatisticsInfo.mUserType = stateInfo.mUserType;
                int i = this.mCallId;
                currAppStatisticsInfo.mCallId = i;
                this.mCallId = i + 1;
                this.mCHRManagerHandler.removeMessages(7);
                this.mCHRManagerHandler.sendEmptyMessageDelayed(7, HwArbitrationDEFS.DelayTimeMillisA);
                return;
            }
            if (101 == appState) {
                HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo = this.mWechatStatisticsInfo;
                if (hwHistreamCHRStatisticsInfo != null) {
                    updateAppStatisticInfo(hwHistreamCHRStatisticsInfo);
                    uploadCHRAppStatisticInfo(this.mWechatStatisticsInfo);
                    this.mWechatStatisticsInfo = null;
                }
                HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo2 = this.mOtherAppStatisticsInfo;
                if (hwHistreamCHRStatisticsInfo2 != null) {
                    updateAppStatisticInfo(hwHistreamCHRStatisticsInfo2);
                    uploadCHRAppStatisticInfo(this.mOtherAppStatisticsInfo);
                    this.mOtherAppStatisticsInfo = null;
                }
                this.mCHRManagerHandler.removeMessages(7);
            }
        }
    }

    private void uploadCHRAppStatisticInfo(HwHistreamCHRStatisticsInfo appStatisticsInfo) {
        HwHiStreamDataBaseManager mHwHiStreamDataBaseManager = HwHiStreamDataBaseManager.getInstance(this.mContext);
        if (appStatisticsInfo != null && mHwHiStreamDataBaseManager != null) {
            long curTime = System.currentTimeMillis();
            HwHistreamCHRStatisticsInfo statisticsBef = mHwHiStreamDataBaseManager.getAppStatistics(appStatisticsInfo.mScenario);
            if (statisticsBef == null) {
                appStatisticsInfo.mLastUploadTime = curTime;
                mHwHiStreamDataBaseManager.insertAppStatistics(appStatisticsInfo);
                sendCHRStatisticEvent(appStatisticsInfo);
                HwHiStreamUtils.logD(false, "uploadCHRAppStatisticInfo: first call", new Object[0]);
                return;
            }
            statisticsBef.updateCHRStatisticsInfo(appStatisticsInfo);
            if (86400000 <= curTime - statisticsBef.mLastUploadTime) {
                statisticsBef.printCHRStatisticsInfo();
                statisticsBef.mLastUploadTime = curTime;
                sendCHRStatisticEvent(statisticsBef);
                statisticsBef = new HwHistreamCHRStatisticsInfo(statisticsBef.mApkName, statisticsBef.mScenario, statisticsBef.mUid);
                statisticsBef.mNum = 0;
                statisticsBef.mLastUploadTime = curTime;
            }
            statisticsBef.printCHRStatisticsInfo();
            mHwHiStreamDataBaseManager.updateAppStatistics(statisticsBef);
        }
    }

    private void updateAppStatisticInfo(HwHistreamCHRStatisticsInfo appStatisticsInfo) {
        long j;
        HwHiStreamTraffic mHwHiStreamTraffic = HwHiStreamTraffic.getInstance();
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
        if (mHwHiStreamTraffic != null && mHwHiStreamNetworkMonitor != null && appStatisticsInfo != null) {
            long curTime = System.currentTimeMillis();
            long curCellularTraffic = mHwHiStreamTraffic.getTotalTraffic(0, curTime, appStatisticsInfo.mUid, 801);
            if (800 == appStatisticsInfo.mCurrNetwork && 0 != appStatisticsInfo.mLastWifiTime) {
                appStatisticsInfo.mCallInWiFiDur = (int) (((long) appStatisticsInfo.mCallInWiFiDur) + ((curTime - appStatisticsInfo.mLastWifiTime) / 1000));
                j = 0;
                appStatisticsInfo.mLastWifiTime = 0;
            } else if (801 != appStatisticsInfo.mCurrNetwork || 0 == appStatisticsInfo.mLastCellTime) {
                j = 0;
            } else {
                appStatisticsInfo.mCallInCellularDur = (int) (((long) appStatisticsInfo.mCallInCellularDur) + ((curTime - appStatisticsInfo.mLastCellTime) / 1000));
                j = 0;
                appStatisticsInfo.mLastCellTime = 0;
            }
            if (j != appStatisticsInfo.mLastMplinkTime) {
                appStatisticsInfo.mMplinkDur = (int) (((long) appStatisticsInfo.mMplinkDur) + ((curTime - appStatisticsInfo.mLastMplinkTime) / 1000));
                appStatisticsInfo.mMplinkEnTraf += (int) getTraffic(appStatisticsInfo.mMplinkStartTraffic, curCellularTraffic);
            }
            appStatisticsInfo.mTrfficCell = (int) getTraffic(appStatisticsInfo.mStartCellularTraffic, curCellularTraffic);
            if (appStatisticsInfo.mCallInCellularDur < 300 && appStatisticsInfo.mCallInCellularDur > 0) {
                appStatisticsInfo.mCellLv1Cnt = 1;
            } else if (appStatisticsInfo.mCallInCellularDur >= 900 || appStatisticsInfo.mCallInCellularDur <= 0) {
                HwHiStreamUtils.logD(false, "over CHR_CALL_LEVEL2_MAX_PERIOD, ignore", new Object[0]);
            } else {
                appStatisticsInfo.mCellLv2Cnt = 1;
            }
            if (appStatisticsInfo.mCallInWiFiDur < 300 && appStatisticsInfo.mCallInWiFiDur > 0) {
                appStatisticsInfo.mWiFiLv1Cnt = 1;
            } else if (appStatisticsInfo.mCallInWiFiDur >= 900 || appStatisticsInfo.mCallInWiFiDur <= 0) {
                HwHiStreamUtils.logD(false, "over CHR_CALL_LEVEL2_MAX_PERIOD, ignore", new Object[0]);
            } else {
                appStatisticsInfo.mWiFiLv2Cnt = 1;
            }
            if (1 == appStatisticsInfo.mStallSwitchCnt) {
                appStatisticsInfo.mStallSwitch1Cnt = 1;
            } else if (appStatisticsInfo.mStallSwitchCnt > 1) {
                appStatisticsInfo.mStallSwitchAbove1Cnt = 1;
            }
            if (2 == appStatisticsInfo.mUserType) {
                appStatisticsInfo.mVipSwitchCnt = appStatisticsInfo.mStallSwitchCnt;
            }
            appStatisticsInfo.printCHRStatisticsInfo();
        }
    }

    public void onUpdateQuality(int wifiRxTraffic, int cellularRxTraffic, int wifiTxTraffic, int celluarTxTraffic, int uid) {
        HwHistreamCHRStatisticsInfo appStatisticsInfo;
        HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo = this.mWechatStatisticsInfo;
        if (hwHistreamCHRStatisticsInfo == null || uid != hwHistreamCHRStatisticsInfo.mUid) {
            HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo2 = this.mOtherAppStatisticsInfo;
            if (hwHistreamCHRStatisticsInfo2 != null && uid == hwHistreamCHRStatisticsInfo2.mUid) {
                appStatisticsInfo = this.mOtherAppStatisticsInfo;
            } else {
                return;
            }
        } else {
            appStatisticsInfo = this.mWechatStatisticsInfo;
        }
        appStatisticsInfo.mTafficPtr %= appStatisticsInfo.mWifiTraffic.length;
        HwHistreamCHRStatisticsInfo.AppTraffic wifiTraffic = appStatisticsInfo.mWifiTraffic[appStatisticsInfo.mTafficPtr];
        HwHistreamCHRStatisticsInfo.AppTraffic cellularTraffic = appStatisticsInfo.mCellularTraffic[appStatisticsInfo.mTafficPtr];
        if (!(wifiTraffic == null || cellularTraffic == null)) {
            if (800 == appStatisticsInfo.mCurrNetwork) {
                wifiTraffic.rx = wifiRxTraffic;
                wifiTraffic.tx = wifiTxTraffic;
                cellularTraffic.rx = -1;
                cellularTraffic.tx = -1;
            } else if (801 == appStatisticsInfo.mCurrNetwork) {
                cellularTraffic.rx = cellularRxTraffic;
                cellularTraffic.tx = celluarTxTraffic;
                wifiTraffic.rx = -1;
                wifiTraffic.tx = -1;
            }
        }
        appStatisticsInfo.mTafficPtr++;
        updateNetworkInfo(appStatisticsInfo.mCurrNetwork);
    }

    private void updateNetworkInfo(int curNetwork) {
        WifiManager wifiManager;
        RssiPacketCountInfo otaInfo;
        if (800 == curNetwork && (wifiManager = this.mWifiManager) != null) {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (!(info == null || -1 == info.getFrequency())) {
                this.mLastWifiInfo.mSsid = info.getSSID();
                this.mLastWifiInfo.mRssi = info.getRssi();
                this.mLastWifiInfo.mChannel = info.getFrequency();
                this.mLastWifiInfo.mFailRate = (int) info.txBadRate;
                this.mLastWifiInfo.mApType = getAPType();
                this.mLastWifiInfo.mSnr = info.getSnr();
            }
            HwWifiBoost mHwWifiBoost = HwWifiBoost.getInstance(this.mContext);
            if (mHwWifiBoost != null && (otaInfo = mHwWifiBoost.getOTAInfo()) != null) {
                int txBad = otaInfo.txbad;
                int txGood = otaInfo.txgood;
                if (txGood - this.mLastWifiInfo.mLastTxGood > 0) {
                    HistreamWifiInfo histreamWifiInfo = this.mLastWifiInfo;
                    histreamWifiInfo.mFailRate = ((txBad - histreamWifiInfo.mLastTxBad) * 10000) / (txGood - this.mLastWifiInfo.mLastTxGood);
                }
                HistreamWifiInfo histreamWifiInfo2 = this.mLastWifiInfo;
                histreamWifiInfo2.mLastTxBad = txBad;
                histreamWifiInfo2.mLastTxGood = txGood;
            }
        }
    }

    private void updateTup(HwHistreamCHRStatisticsInfo appStatisticsInfo, int networkType) {
        int txTup;
        if (appStatisticsInfo != null) {
            int tafficLen = appStatisticsInfo.mWifiTraffic.length;
            int rxTraffic = 0;
            int ptr = appStatisticsInfo.mTafficPtr;
            int count = 0;
            int i = 0;
            while (true) {
                txTup = -1;
                if (i >= 3) {
                    break;
                }
                int ptr2 = ptr % tafficLen;
                HwHistreamCHRStatisticsInfo.AppTraffic appTraffic = 800 == networkType ? appStatisticsInfo.mWifiTraffic[ptr2] : appStatisticsInfo.mCellularTraffic[ptr2];
                if (!(appTraffic == null || -1 == appTraffic.rx)) {
                    rxTraffic += appTraffic.rx;
                    count++;
                }
                ptr = ptr2 + 1;
                i++;
            }
            int rxTup2 = count == 0 ? -1 : rxTraffic / (count * 2);
            int rxTraffic2 = 0;
            int txTraffic = 0;
            int ptr3 = appStatisticsInfo.mTafficPtr + 3;
            int count2 = 0;
            for (int i2 = 0; i2 < 3; i2++) {
                int ptr4 = ptr3 % tafficLen;
                HwHistreamCHRStatisticsInfo.AppTraffic appTraffic2 = 800 == networkType ? appStatisticsInfo.mWifiTraffic[ptr4] : appStatisticsInfo.mCellularTraffic[ptr4];
                if (!(appTraffic2 == null || -1 == appTraffic2.rx)) {
                    rxTraffic2 += appTraffic2.rx;
                    txTraffic += appTraffic2.tx;
                    count2++;
                }
                ptr3 = ptr4 + 1;
            }
            int rxTup1 = count2 == 0 ? -1 : rxTraffic2 / (count2 * 2);
            if (count2 != 0) {
                txTup = txTraffic / (count2 * 2);
            }
            if (800 == networkType) {
                appStatisticsInfo.mWifiRxTup2 = rxTup2;
                appStatisticsInfo.mWifiRxTup1 = rxTup1;
                appStatisticsInfo.mWifiTxTup = txTup;
            } else {
                appStatisticsInfo.mCelluarRxTup2 = rxTup2;
                appStatisticsInfo.mCelluarRxTup1 = rxTup1;
                appStatisticsInfo.mCelluarTxTup = txTup;
            }
            HwHiStreamUtils.logD(false, "updateTup: networkType=%{public}d,WifiTxTup=%{public}d,WifiRxTup1=%{public}d,WifiRxTup2=%{public}d,CelluarTxTup=%{public}d,CelluarRxTup1=%{public}d,CelluarRxTup2=%{public}d,TafficPtr= %{public}d", Integer.valueOf(networkType), Integer.valueOf(appStatisticsInfo.mWifiTxTup), Integer.valueOf(appStatisticsInfo.mWifiRxTup1), Integer.valueOf(appStatisticsInfo.mWifiRxTup2), Integer.valueOf(appStatisticsInfo.mCelluarTxTup), Integer.valueOf(appStatisticsInfo.mCelluarRxTup1), Integer.valueOf(appStatisticsInfo.mCelluarRxTup2), Integer.valueOf(appStatisticsInfo.mTafficPtr));
        }
    }

    public void onNetworkChange() {
        HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo = this.mOtherAppStatisticsInfo;
        if (hwHistreamCHRStatisticsInfo != null) {
            handleNetworkChange(hwHistreamCHRStatisticsInfo);
        }
        HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo2 = this.mWechatStatisticsInfo;
        if (hwHistreamCHRStatisticsInfo2 != null) {
            handleNetworkChange(hwHistreamCHRStatisticsInfo2);
        }
    }

    public void onMplinkStateChange(Message msg) {
        int i;
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            int sceneId = bundle.getInt("sceneId");
            int mplinkEvent = bundle.getInt("mplinkEvent");
            int failReason = bundle.getInt("failReason");
            HwHiStreamUtils.logD(false, "onMplinkStateChange: sceneId= %{public}d,mplinkEvent=%{public}d ,failReason=%{public}d", Integer.valueOf(sceneId), Integer.valueOf(mplinkEvent), Integer.valueOf(failReason));
            HwHistreamCHRStatisticsInfo appStatisticsInfo = null;
            HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo = this.mOtherAppStatisticsInfo;
            if (hwHistreamCHRStatisticsInfo == null || sceneId != hwHistreamCHRStatisticsInfo.mScenario) {
                HwHistreamCHRStatisticsInfo hwHistreamCHRStatisticsInfo2 = this.mWechatStatisticsInfo;
                if (hwHistreamCHRStatisticsInfo2 != null && sceneId == hwHistreamCHRStatisticsInfo2.mScenario) {
                    appStatisticsInfo = this.mWechatStatisticsInfo;
                }
            } else {
                appStatisticsInfo = this.mOtherAppStatisticsInfo;
            }
            HwHiStreamTraffic mHwHiStreamTraffic = HwHiStreamTraffic.getInstance();
            HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
            if (appStatisticsInfo != null && mHwHiStreamNetworkMonitor != null && mHwHiStreamTraffic != null) {
                handleNetworkChange(appStatisticsInfo);
                long curTime = System.currentTimeMillis();
                if (5 == mplinkEvent) {
                    if (failReason == 0) {
                        appStatisticsInfo.mStallSwitchCnt++;
                        appStatisticsInfo.mMplinkEnCnt++;
                        appStatisticsInfo.mLastMplinkTime = curTime;
                        appStatisticsInfo.mMplinkStartTraffic = mHwHiStreamTraffic.getTotalTraffic(0, curTime, appStatisticsInfo.mUid, 801);
                    } else if (failReason == 1) {
                        appStatisticsInfo.mMplinkEnFailCnt++;
                        if (!MpLinkCommonUtils.isMpLinkEnabled(this.mContext)) {
                            appStatisticsInfo.mHicureEnCnt++;
                        } else {
                            appStatisticsInfo.mMplinkEnFailEnvironCnt++;
                        }
                    } else if (failReason == 2) {
                        appStatisticsInfo.mMplinkEnFailCnt++;
                        appStatisticsInfo.mMplinkEnFailCoexistCnt++;
                    } else if (failReason == 3) {
                        appStatisticsInfo.mMplinkEnFailCnt++;
                        appStatisticsInfo.mMplinkEnFailPingPongCnt++;
                    } else if (failReason == 4) {
                        appStatisticsInfo.mMplinkEnFailCnt++;
                        appStatisticsInfo.mMplinkEnFailHistoryQoeCnt++;
                    } else if (failReason == 5) {
                        appStatisticsInfo.mMplinkEnFailCnt++;
                        appStatisticsInfo.mMplinkEnFailChQoeCnt++;
                    } else if (failReason == 20) {
                        appStatisticsInfo.mMplinkEnFailCnt++;
                    }
                } else if (4 != mplinkEvent && 3 != mplinkEvent && 7 != mplinkEvent && 9 != mplinkEvent) {
                } else {
                    if (failReason == 0) {
                        if (0 != appStatisticsInfo.mLastMplinkTime) {
                            appStatisticsInfo.mMplinkDur = (int) (((long) appStatisticsInfo.mMplinkDur) + ((curTime - appStatisticsInfo.mLastMplinkTime) / 1000));
                            i = 4;
                            appStatisticsInfo.mMplinkEnTraf += (int) getTraffic(appStatisticsInfo.mMplinkStartTraffic, mHwHiStreamTraffic.getTotalTraffic(0, curTime, appStatisticsInfo.mUid, 801));
                            HwHiStreamUtils.logD(false, "onMplinkStateChange: mplink disable,mMplinkDur= %{public}d,mMplinkEnTraf=%{public}d", Integer.valueOf(appStatisticsInfo.mMplinkDur), Integer.valueOf(appStatisticsInfo.mMplinkEnTraf));
                        } else {
                            i = 4;
                        }
                        appStatisticsInfo.mLastMplinkTime = 0;
                        if (3 == mplinkEvent) {
                            appStatisticsInfo.mMplinkDisStallCnt++;
                            appStatisticsInfo.mStallSwitchCnt++;
                        } else if (i == mplinkEvent) {
                            appStatisticsInfo.mMplinkDisWifiGoodCnt++;
                        } else if (9 == mplinkEvent) {
                            appStatisticsInfo.mStallSwitch0Cnt++;
                        }
                    } else {
                        appStatisticsInfo.mMplinkDisFailCnt++;
                    }
                }
            }
        }
    }

    private void handleNetworkChange(HwHistreamCHRStatisticsInfo appStatisticsInfo) {
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
        if (appStatisticsInfo == null) {
            return;
        }
        if (mHwHiStreamNetworkMonitor != null) {
            int networkType = mHwHiStreamNetworkMonitor.getCurrNetworkType(appStatisticsInfo.mUid);
            if (appStatisticsInfo.mCurrNetwork != networkType) {
                long curTime = System.currentTimeMillis();
                mHwHiStreamNetworkMonitor.mLastHandoverTime = curTime;
                if (800 == appStatisticsInfo.mCurrNetwork && 0 != appStatisticsInfo.mLastWifiTime) {
                    appStatisticsInfo.mCallInWiFiDur = (int) (((long) appStatisticsInfo.mCallInWiFiDur) + ((curTime - appStatisticsInfo.mLastWifiTime) / 1000));
                    appStatisticsInfo.mLastWifiTime = 0;
                } else if (801 == appStatisticsInfo.mCurrNetwork && 0 != appStatisticsInfo.mLastCellTime) {
                    appStatisticsInfo.mCallInCellularDur = (int) (((long) appStatisticsInfo.mCallInCellularDur) + ((curTime - appStatisticsInfo.mLastCellTime) / 1000));
                    appStatisticsInfo.mLastCellTime = 0;
                }
                if (800 == networkType) {
                    appStatisticsInfo.mLastWifiTime = curTime;
                    if (3000 > curTime - mHwHiStreamNetworkMonitor.mLastCellDisableTime || (true == mHwHiStreamNetworkMonitor.getMoblieDateSettings() && 6000 > curTime - mHwHiStreamNetworkMonitor.mLastWifiEnabledTime)) {
                        HwHiStreamUtils.logD(false, "+++++handleNetworkChange: USER manually handover to WiFi++++++", new Object[0]);
                        appStatisticsInfo.mSwitch2WifiCnt++;
                    }
                } else if (801 == networkType) {
                    appStatisticsInfo.mLastCellTime = curTime;
                    if (3000 > curTime - mHwHiStreamNetworkMonitor.mLastWifiDisabledTime) {
                        HwHiStreamUtils.logD(false, "+++++handleNetworkChange: USER manually handover to cellular++++++", new Object[0]);
                        if (!MpLinkCommonUtils.isMpLinkEnabled(this.mContext)) {
                            appStatisticsInfo.mHicureSucCnt++;
                        }
                        appStatisticsInfo.mSwitch2CellCnt++;
                    }
                }
                HwHiStreamUtils.logD(false, "handleNetworkChange: mScenario=%{public}d,oldNetwork=%{public}d,currNetwork=%{public}d,CallInWiFiDur=%{public}d,mCallInCellularDur=%{public}d", Integer.valueOf(appStatisticsInfo.mScenario), Integer.valueOf(appStatisticsInfo.mCurrNetwork), Integer.valueOf(networkType), Integer.valueOf(appStatisticsInfo.mCallInWiFiDur), Integer.valueOf(appStatisticsInfo.mCallInCellularDur));
                appStatisticsInfo.mCurrNetwork = networkType;
            }
        }
    }

    public int getAPType() {
        int type;
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null || ((type = getAuthType(wifiInfo.getNetworkId())) != 1 && type != 4)) {
            return 1;
        }
        return 0;
    }

    private int getAuthType(int networkId) {
        List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            return -1;
        }
        for (WifiConfiguration config : configs) {
            if (config != null && isValid(config) && networkId == config.networkId) {
                return config.getAuthType();
            }
        }
        return -1;
    }

    private boolean isValid(WifiConfiguration config) {
        if (config == null || config.allowedKeyManagement.cardinality() > 1) {
            return false;
        }
        return true;
    }

    private HwHistreamCHRStatisticsInfo getCurStatisticsInfo() {
        if (this.mOtherAppStatisticsInfo != null) {
            return this.mOtherAppStatisticsInfo;
        }
        if (this.mWechatStatisticsInfo != null) {
            return this.mWechatStatisticsInfo;
        }
        return null;
    }

    private long getTraffic(long startTraffic, long endTraffic) {
        HwHiStreamUtils.logD(false, "getTraffic startTraffic = %{public}s,endTraffic=%{public}s", String.valueOf(startTraffic), String.valueOf(endTraffic));
        if (endTraffic <= startTraffic || startTraffic <= 0) {
            return 0;
        }
        return (endTraffic - startTraffic) / 1000;
    }

    private boolean isVoipScene(int appSceneId) {
        if (appSceneId == 100105 || appSceneId == 100106) {
            return true;
        }
        return false;
    }

    private void sendCHRCollectParaEvent(HwHistreamCHRMachineInfo machineInfo) {
        if (machineInfo != null) {
            this.mUploadCollectParaCount++;
            HwHiStreamUtils.logD(false, "sendCHRCollectParaEvent: mUploadCollectParaCount=%{public}d", Integer.valueOf(this.mUploadCollectParaCount));
            IMonitor.EventStream collectParaEventStream = IMonitor.openEventStream((int) CHR_UPLOAD_EVENT_QOE_PARA_COLLECT);
            if (collectParaEventStream != null) {
                collectParaEventStream.setParam(0, machineInfo.mRxTup1Bef).setParam(1, machineInfo.mRxTup2Bef).setParam(2, machineInfo.mChLoad).setParam(3, machineInfo.mTxFail1Bef).setParam(4, machineInfo.mTxFail2Bef).setParam(5, machineInfo.mApkName).setParam(6, machineInfo.mScenario).setParam(7, machineInfo.mStreamQoe).setParam(8, machineInfo.mWechatVideoQoe).setParam(9, machineInfo.mRAT).setParam(10, machineInfo.mWifiRssi).setParam(11, machineInfo.mWifiSnr).setParam(12, machineInfo.mCellSig).setParam(13, machineInfo.mCellQuality).setParam(14, machineInfo.mCellSinr).setParam(15, machineInfo.mNetDlTup).setParam(16, machineInfo.mNetRtt);
                IMonitor.sendEvent(collectParaEventStream);
                IMonitor.closeEventStream(collectParaEventStream);
            }
        }
    }

    private void sendCHRHandoverEvent(HwHistreamCHRHandoverInfo handoverInfo) {
        IMonitor.EventStream handoverEventStream = IMonitor.openEventStream((int) CHR_UPLOAD_EVENT_HANDOVER);
        if (handoverInfo != null && handoverEventStream != null) {
            IMonitor.EventStream spaceInfoStream = null;
            IMonitor.EventStream historyRecordStream = null;
            this.mUploadHandoverCount++;
            HwHiStreamUtils.logD(false, "sendCHRHandoverEvent: mUploadHandoverCount=%{public}d", Integer.valueOf(this.mUploadHandoverCount));
            handoverEventStream.setParam(0, handoverInfo.mApkName).setParam(1, handoverInfo.mScenario).setParam(2, handoverInfo.mEventType).setParam(3, handoverInfo.mWifiSsidBef).setParam(4, handoverInfo.mWifiRssiBef).setParam(5, handoverInfo.mWifiChBef).setParam(6, handoverInfo.mWifiSsidAft).setParam(7, handoverInfo.mWifiRssiAft).setParam(8, handoverInfo.mWifiChAft).setParam(9, handoverInfo.mCellRat).setParam(10, handoverInfo.mCellSig).setParam(11, handoverInfo.mCellFreq).setParam(12, handoverInfo.mWifiRxTup1Bef).setParam(13, handoverInfo.mWifiRxTup2Bef).setParam(14, handoverInfo.mWifiChLoad).setParam(15, handoverInfo.mWifiTxFail1Bef).setParam(16, handoverInfo.mWifiTxFail2Bef).setParam(17, handoverInfo.mWifiRxTupAft).setParam(18, handoverInfo.mCellRxTup).setParam(19, handoverInfo.mSwitchCauseBef).setParam(20, handoverInfo.mWifiSnr).setParam(21, handoverInfo.mCellQuality).setParam(22, handoverInfo.mCellSinr).setParam(23, handoverInfo.mStreamQoeBef).setParam(24, handoverInfo.mStreamQoeAft).setParam(25, handoverInfo.mApType).setParam(27, handoverInfo.mTupBef).setParam(28, handoverInfo.mRttBef);
            if (handoverInfo.mWavemappingInfo != null) {
                spaceInfoStream = IMonitor.openEventStream(909009047);
                historyRecordStream = IMonitor.openEventStream(909009048);
                if (!(spaceInfoStream == null || historyRecordStream == null)) {
                    spaceInfoStream.setParam(0, handoverInfo.mWavemappingInfo.spaceIdAll).setParam(1, handoverInfo.mWavemappingInfo.modelVerAll).setParam(2, handoverInfo.mWavemappingInfo.spaceIdMain).setParam(3, handoverInfo.mWavemappingInfo.modelVerMain).setParam(4, handoverInfo.mWavemappingInfo.spaceIdCell).setParam(5, handoverInfo.mWavemappingInfo.modelVerCell).setParam(6, handoverInfo.mWavemappingInfo.netIdCnt).setParam(7, handoverInfo.mWavemappingInfo.netName).setParam(8, handoverInfo.mWavemappingInfo.netFreq).setParam(9, handoverInfo.mWavemappingInfo.netType).setParam(10, handoverInfo.mWavemappingInfo.recordDays);
                    historyRecordStream.setParam(0, handoverInfo.mScenario).setParam(1, spaceInfoStream).setParam(2, handoverInfo.mWavemappingInfo.duration).setParam(3, handoverInfo.mWavemappingInfo.goodCnt).setParam(4, handoverInfo.mWavemappingInfo.poorCnt).setParam(5, handoverInfo.mWavemappingInfo.dataRx).setParam(6, handoverInfo.mWavemappingInfo.dataTx);
                    handoverEventStream.setParam(26, historyRecordStream);
                }
            } else {
                HwHiStreamUtils.logD(false, "sendCHRHandoverEvent:wave mapping history info is null", new Object[0]);
            }
            IMonitor.sendEvent(handoverEventStream);
            IMonitor.closeEventStream(spaceInfoStream);
            IMonitor.closeEventStream(historyRecordStream);
            IMonitor.closeEventStream(handoverEventStream);
        }
    }

    private void sendCHRStatisticEvent(HwHistreamCHRStatisticsInfo appStatisticsInfo) {
        if (appStatisticsInfo != null) {
            IMonitor.EventStream statisticsStream = null;
            switch (appStatisticsInfo.mScenario) {
                case HwAPPQoEUtils.SCENE_AUDIO /* 100105 */:
                    statisticsStream = IMonitor.openEventStream((int) CHR_UPLOAD_EVENT_WECHAT_AUDIO_STATISTICS);
                    if (statisticsStream != null) {
                        statisticsStream.setParam(0, appStatisticsInfo.mNum).setParam(1, appStatisticsInfo.mStartInCellularCnt).setParam(2, appStatisticsInfo.mStartInWiFiCnt).setParam(3, appStatisticsInfo.mCallInCellularDur).setParam(4, appStatisticsInfo.mCallInWiFiDur).setParam(5, appStatisticsInfo.mCellLv1Cnt).setParam(6, appStatisticsInfo.mCellLv2Cnt).setParam(7, appStatisticsInfo.mCellLv3Cnt).setParam(8, appStatisticsInfo.mWiFiLv1Cnt).setParam(9, appStatisticsInfo.mWiFiLv2Cnt).setParam(10, appStatisticsInfo.mWiFiLv3Cnt).setParam(11, appStatisticsInfo.mTrfficCell).setParam(12, appStatisticsInfo.mVipSwitchCnt).setParam(13, appStatisticsInfo.mStallSwitchCnt).setParam(14, appStatisticsInfo.mStallSwitch0Cnt).setParam(15, appStatisticsInfo.mStallSwitch1Cnt).setParam(16, appStatisticsInfo.mStallSwitchAbove1Cnt).setParam(17, appStatisticsInfo.mSwitch2CellCnt).setParam(18, appStatisticsInfo.mSwitch2WifiCnt).setParam(19, appStatisticsInfo.mMplinkDur).setParam(20, appStatisticsInfo.mMplinkEnCnt).setParam(21, appStatisticsInfo.mMplinkDisStallCnt).setParam(22, appStatisticsInfo.mMplinkDisWifiGoodCnt).setParam(23, appStatisticsInfo.mMplinkEnFailCnt).setParam(24, appStatisticsInfo.mMplinkDisFailCnt).setParam(25, appStatisticsInfo.mMplinkEnTraf).setParam(26, appStatisticsInfo.mMplinkEnFailEnvironCnt).setParam(27, appStatisticsInfo.mMplinkEnFailCoexistCnt).setParam(28, appStatisticsInfo.mMplinkEnFailPingPongCnt).setParam(29, appStatisticsInfo.mMplinkEnFailHistoryQoeCnt).setParam(30, appStatisticsInfo.mMplinkEnFailChQoeCnt).setParam(31, appStatisticsInfo.mHicureEnCnt).setParam(32, appStatisticsInfo.mHicureSucCnt);
                        break;
                    } else {
                        return;
                    }
                case HwAPPQoEUtils.SCENE_VIDEO /* 100106 */:
                    statisticsStream = IMonitor.openEventStream((int) CHR_UPLOAD_EVENT_WECHAT_VIDEO_STATISTICS);
                    if (statisticsStream != null) {
                        statisticsStream.setParam(0, appStatisticsInfo.mNum).setParam(1, appStatisticsInfo.mStartInCellularCnt).setParam(2, appStatisticsInfo.mStartInWiFiCnt).setParam(3, appStatisticsInfo.mCallInCellularDur).setParam(4, appStatisticsInfo.mCallInWiFiDur).setParam(5, appStatisticsInfo.mCellLv1Cnt).setParam(6, appStatisticsInfo.mCellLv2Cnt).setParam(7, appStatisticsInfo.mCellLv3Cnt).setParam(8, appStatisticsInfo.mWiFiLv1Cnt).setParam(9, appStatisticsInfo.mWiFiLv2Cnt).setParam(10, appStatisticsInfo.mWiFiLv3Cnt).setParam(11, appStatisticsInfo.mTrfficCell).setParam(12, appStatisticsInfo.mVipSwitchCnt).setParam(13, appStatisticsInfo.mStallSwitchCnt).setParam(14, appStatisticsInfo.mStallSwitch0Cnt).setParam(15, appStatisticsInfo.mStallSwitch1Cnt).setParam(16, appStatisticsInfo.mStallSwitchAbove1Cnt).setParam(17, appStatisticsInfo.mSwitch2CellCnt).setParam(18, appStatisticsInfo.mSwitch2WifiCnt).setParam(19, appStatisticsInfo.mMplinkDur).setParam(20, appStatisticsInfo.mMplinkEnCnt).setParam(21, appStatisticsInfo.mMplinkDisStallCnt).setParam(22, appStatisticsInfo.mMplinkDisWifiGoodCnt).setParam(23, appStatisticsInfo.mMplinkEnFailCnt).setParam(24, appStatisticsInfo.mMplinkDisFailCnt).setParam(25, appStatisticsInfo.mMplinkEnTraf).setParam(26, appStatisticsInfo.mMplinkEnFailEnvironCnt).setParam(27, appStatisticsInfo.mMplinkEnFailCoexistCnt).setParam(28, appStatisticsInfo.mMplinkEnFailPingPongCnt).setParam(29, appStatisticsInfo.mMplinkEnFailHistoryQoeCnt).setParam(30, appStatisticsInfo.mMplinkEnFailChQoeCnt).setParam(31, appStatisticsInfo.mHicureEnCnt).setParam(32, appStatisticsInfo.mHicureSucCnt);
                        break;
                    } else {
                        return;
                    }
                case HwAPPQoEUtils.SCENE_DOUYIN /* 100501 */:
                case HwAPPQoEUtils.SCENE_KUAISHOU /* 100701 */:
                case HwAPPQoEUtils.SCENE_TIKTOK /* 100901 */:
                    statisticsStream = IMonitor.openEventStream((int) CHR_UPLOAD_EVENT_DOUYIN_STATISTICS);
                    if (statisticsStream != null) {
                        statisticsStream.setParam(0, appStatisticsInfo.mNum).setParam(1, appStatisticsInfo.mStartInCellularCnt).setParam(2, appStatisticsInfo.mStartInWiFiCnt).setParam(3, appStatisticsInfo.mCallInCellularDur).setParam(4, appStatisticsInfo.mCallInWiFiDur).setParam(5, appStatisticsInfo.mCellLv1Cnt).setParam(6, appStatisticsInfo.mCellLv2Cnt).setParam(7, appStatisticsInfo.mCellLv3Cnt).setParam(8, appStatisticsInfo.mWiFiLv1Cnt).setParam(9, appStatisticsInfo.mWiFiLv2Cnt).setParam(10, appStatisticsInfo.mWiFiLv3Cnt).setParam(11, appStatisticsInfo.mTrfficCell).setParam(12, appStatisticsInfo.mStallSwitch0Cnt).setParam(13, appStatisticsInfo.mStallSwitch1Cnt).setParam(14, appStatisticsInfo.mStallSwitchAbove1Cnt).setParam(15, appStatisticsInfo.mSwitch2CellCnt).setParam(16, appStatisticsInfo.mSwitch2WifiCnt).setParam(17, appStatisticsInfo.mMplinkDur).setParam(18, appStatisticsInfo.mMplinkEnCnt).setParam(19, appStatisticsInfo.mMplinkDisStallCnt).setParam(20, appStatisticsInfo.mMplinkDisWifiGoodCnt).setParam(21, appStatisticsInfo.mMplinkEnFailCnt).setParam(22, appStatisticsInfo.mMplinkDisFailCnt).setParam(23, appStatisticsInfo.mMplinkEnTraf).setParam(24, appStatisticsInfo.mMplinkEnFailEnvironCnt).setParam(25, appStatisticsInfo.mMplinkEnFailCoexistCnt).setParam(26, appStatisticsInfo.mMplinkEnFailPingPongCnt).setParam(27, appStatisticsInfo.mMplinkEnFailHistoryQoeCnt).setParam(28, appStatisticsInfo.mMplinkEnFailChQoeCnt).setParam(29, appStatisticsInfo.mHicureEnCnt).setParam(30, appStatisticsInfo.mHicureSucCnt);
                        break;
                    } else {
                        return;
                    }
            }
            IMonitor.sendEvent(statisticsStream);
            IMonitor.closeEventStream(statisticsStream);
        }
    }
}
