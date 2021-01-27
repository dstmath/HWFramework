package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.hidata.appqoe.HwAppQoeResourceManager;
import com.android.server.hidata.appqoe.HwPowerParameterConfig;
import com.huawei.pgmng.log.LogPower;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class HwWifiChipPowerAbnormal {
    private static final String ACTION_CHECK_ABNORMAL = "huawei.intent.action.check_wifi_abnormal";
    private static final int ADDBAREQ_CNT = 3;
    private static final int ADDBAREQ_DURATION = 1500000;
    private static final String ADDBAREQ_FREQ_BASELINE = "AddbareqFreqBaseline";
    private static final double ADDBAREQ_FREQ_THRESOLD = 100.0d;
    private static final int AMSDU_ADDBA_GROUPREKEY_CMD_LENGTH = 5;
    private static final String AMSDU_ADDBA_GROUPREKEY_DATA = "AmsduAddbaGroupRekeyData";
    private static final int AMSDU_DURATION = 1500000;
    private static final String AMSDU_LARGE_BASELINE = "AmsduLargeBaseline";
    private static final double AMSDU_LARGE_THRESOLD = 100.0d;
    private static final String AMSDU_SMALL_ABNORMAL_BASELINE = "AmsduSmallAbnormalBaseline";
    private static final double AMSDU_SMALL_ABNORMAL_THRESOLD = 100.0d;
    private static final String AMSDU_SMALL_BASELINE = "AmsduSmallBaseline";
    private static final double AMSDU_SMALL_THRESOLD = 100.0d;
    private static final String AMSDU_TOTAL_BASELINE = "AmsduTotalBaseline";
    private static final double AMSDU_TOTAL_THRESOLD = 0.067d;
    private static final int APF_FILTER_PKT = 2;
    private static final int ARP_OFFLOAD_PKT = 1;
    private static final String BEACON_BASELINE = "mBeaconBaseline";
    private static final int BEACON_DURATION = 1500000;
    private static final int BEACON_EXPECT_CNT = 1;
    private static final int BEACON_REAL_CNT = 0;
    private static final double BEACON_THRESOLD = 0.99d;
    private static final int BEACON_TIM_CMD_LENGTH = 4;
    private static final String BEACON_TIM_DATA = "BeaconTimData";
    private static final int CHIP_DEEP_SLEEP_TIME = 2;
    private static final int CHIP_LIGHT_SLEEP_TIME = 1;
    private static final int CHIP_TIME_CMD_LENGTH = 3;
    private static final int CHIP_WORK_TIME = 0;
    private static final String CHIP_WORK_TIME_DATA = "ChipTimeData";
    private static final double CHIP_WORK_TIME_RATIO_THRESOLD = 2.0d;
    private static final int CMD_GET_BEACON_TIM_CNT = 119;
    private static final int CMD_GET_DEVICE_FILTER_PKTS = 107;
    private static final int CMD_GET_SMALL_AMSDU_CNT = 142;
    private static final int CMD_GET_TX_TOTAL_CNT = 201;
    private static final int CMD_GET_WORK_TIME = 203;
    private static final int DEVICE_FILTER_PKT = 0;
    private static final String DISCONNECT_FREQ_BASELINE = "DisconnectFreqBaseline";
    private static final int DISCONNECT_FREQ_THRESOLD = 600000;
    private static final int DISCONNECT_THRESHOLD = 5;
    private static final int FILTER_CMD_LENGTH = 4;
    private static final int GROUPREKEY_CNT = 4;
    private static final int GROUPREKEY_DURATION = 1500000;
    private static final String GROUPREKEY_FREQ_BASELINE = "GroupRekeyFreqBaseline";
    private static final double GROUPREKY_FREQ_THRESOLD = 100.0d;
    private static final int ICMP_FILTER_PKT = 3;
    private static final int LARGE_AMSDU_CNT = 1;
    private static final int LIMIT_DELTA_TIME = 10000;
    private static final int MIX_AMSDU_CNT = 2;
    private static final String MTK_CHIP_6889 = "mt6889";
    private static final String PACKET_FILTER_DATA = "PacketFilterData";
    private static final String[] PKG_FILTER_NAME = {"deviceFilterCnt", "arpFilterCnt", "apfFilterCnt", "icmpFilterCnt"};
    private static final String PKT_BASELINE = "mPktBaseline";
    private static final int PKT_DURATION = 1500000;
    private static final double PKT_THRESOLD = 2.0d;
    private static final String PM_CHANGE_WIFI_STATE = "android.permission.ACCESS_WIFI_STATE";
    private static final String PROP_WIFI_CHIP_TYPE = SystemProperties.get("ro.hardware", "");
    private static final int SEC_TOMISEC = 1000;
    private static final int SMALL_AMSDU_CNT = 0;
    private static final String TAG = "HwWifiStateMachine_PAC";
    private static final int TIM_DURATION = 1500000;
    private static final int TIM_ERROR_CNT = 2;
    private static final String TIM_ERR_FREQ_BASELINE = "TimErrFreqBaseline";
    private static final double TIM_ERR_FREQ_THRESOLD = 0.4d;
    private static final String TIM_FREQ_BASELINE = "TimFreqBaseline";
    private static final double TIM_FREQ_THRESOLD = 0.5d;
    private static final String TIM_RATIO_BASELINE = "TimRatioBaseline";
    private static final double TIM_RATIO_THRESOLD = 0.7d;
    private static final int TIM_TOTAL_CNT = 3;
    private static final int TOTAL_RX_CNT = 1;
    private static final int TOTAL_TRX_CMD_LENGTH = 2;
    private static final String TOTAL_TRX_PKTS_DATA = "TotalTrxData";
    private static final int TOTAL_TX_CNT = 0;
    private static final int TRX_ABNORMAL_DURATION = 1500000;
    private static final int TRX_PKTS_THRESOLD = 0;
    private static final boolean WIFI_CHIP_CHECK_ENABLE = SystemProperties.getBoolean("persist.sys.wifi_check_enable", true);
    private static final int WIFI_PKT_FILTER_ABNORMAL = 228;
    private static WifiStateMachineUtils wifiStateMachineUtils = EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class);
    private final long ALARM_INTERVAL = Long.parseLong(SystemProperties.get("persist.sys.wifi_check_internal", "240000"));
    private int mAddbareqCurDuration = 0;
    private int mAddbareqDuration = 1500000;
    private double mAddbareqFreqBaseline = 100.0d;
    private int mAmsduCurDuration = 0;
    private int mAmsduDuration = 1500000;
    private double mAmsduLargeBaseline = 100.0d;
    private double mAmsduSmallAbnormalBaseline = 100.0d;
    private double mAmsduSmallBaseline = 100.0d;
    private double mAmsduTotalBaseline = AMSDU_TOTAL_THRESOLD;
    private double mBeaconBaseline = BEACON_THRESOLD;
    private int mBeaconCurDuration = 0;
    private int mBeaconDuration = 1500000;
    private double mChipWorkRatioBaseline = 2.0d;
    private Context mContext;
    private int mDisconnectCount = 0;
    private long mDisconnectCurDuration = 0;
    private double mDisconnectFreqBaseline = 600000.0d;
    private ArrayList<Long> mDisconnectTimeList = new ArrayList<>();
    private int mGroupRekeyCurDuration = 0;
    private int mGroupRekeyDuration = 1500000;
    private double mGroupRekeyFreqBaseline = 100.0d;
    private HwWifiChipPowerAbnormalReceiver mHwWifiChipPowerAbnormalReceiver = null;
    private HwWifiCHRService mHwWifiChrService;
    private boolean mInitAddbareq;
    private boolean mInitAmsdu;
    private boolean mInitBeacon;
    private boolean mInitDisconnect;
    private boolean mInitFiltPkt;
    private boolean mInitGroupRekey;
    private boolean mInitTim;
    private boolean mInitTrxPkts;
    private boolean mIsWifiConnected = false;
    private int mLastAddbareqCnt = 0;
    private int mLastBeaconExpectCnt = 0;
    private int mLastBeaconRealCnt = 0;
    private int mLastChipTime = 0;
    private int mLastChipWorkTime = 0;
    private long mLastDisconnectCurDuration = 0;
    private long mLastDisconnectTime = 0;
    private int mLastGroupRekeyCnt = 0;
    private int mLastLargeAmsduCnt = 0;
    private int mLastMixAmsduCnt = 0;
    private int mLastSmallAmsduCnt = 0;
    private int mLastTimErrorCnt = 0;
    private int mLastTimTotalCnt = 0;
    private int mLastTotalTrxPktsCnt = 0;
    private long mLastWifiTime = 0;
    private final Object mLock = new Object();
    private PendingIntent mPendingIntent = null;
    private double mPktBaseline = 2.0d;
    private int mPktCurDuration = 0;
    private int mPktDuration = 1500000;
    private int mTimCurDuration = 0;
    private int mTimDuration = 1500000;
    private double mTimErrFreqBaseline = TIM_ERR_FREQ_THRESOLD;
    private double mTimFreqBaseline = TIM_FREQ_THRESOLD;
    private double mTimRatioBaseline = TIM_RATIO_THRESOLD;
    private int mTrxAbnormalDuration = 1500000;
    private int mTrxCurDuration = 0;
    private double mTrxPktsBaseline = 0.0d;
    private ClientModeImpl mWifiStateMachine = null;

    static /* synthetic */ int access$208(HwWifiChipPowerAbnormal x0) {
        int i = x0.mDisconnectCount;
        x0.mDisconnectCount = i + 1;
        return i;
    }

    HwWifiChipPowerAbnormal(Context context, HwWifiCHRService hwWifiChrService, ClientModeImpl wifiStateMachine) {
        this.mContext = context;
        this.mHwWifiChrService = hwWifiChrService;
        this.mWifiStateMachine = wifiStateMachine;
        registerRecevier();
        registerDisconnectRecevier();
    }

    private void loadWifiBeaconTimBaselineCfg() {
        HwAppQoeResourceManager qoeResourceManager = HwAppQoeResourceManager.getInstance();
        if (qoeResourceManager.getPowerParameterConfigList() == null) {
            Log.e(TAG, "configlist is null");
            return;
        }
        HwPowerParameterConfig loadingPktBaseline = qoeResourceManager.getPowerParameterConfig(PKT_BASELINE);
        if (loadingPktBaseline != null) {
            this.mPktBaseline = loadingPktBaseline.getPowerParameterValue();
        }
        HwPowerParameterConfig loadingBeaconBaseline = qoeResourceManager.getPowerParameterConfig(BEACON_BASELINE);
        if (loadingBeaconBaseline != null) {
            this.mBeaconBaseline = loadingBeaconBaseline.getPowerParameterValue();
        }
        HwPowerParameterConfig loadingTimRatioBaseline = qoeResourceManager.getPowerParameterConfig(TIM_RATIO_BASELINE);
        if (loadingTimRatioBaseline != null) {
            this.mTimRatioBaseline = loadingTimRatioBaseline.getPowerParameterValue();
        }
        HwPowerParameterConfig loadingTimFreqBaseline = qoeResourceManager.getPowerParameterConfig(TIM_FREQ_BASELINE);
        if (loadingTimFreqBaseline != null) {
            this.mTimFreqBaseline = loadingTimFreqBaseline.getPowerParameterValue();
        }
        HwPowerParameterConfig loadingTimErrFreqBaseline = qoeResourceManager.getPowerParameterConfig(TIM_ERR_FREQ_BASELINE);
        if (loadingTimErrFreqBaseline != null) {
            this.mTimErrFreqBaseline = loadingTimErrFreqBaseline.getPowerParameterValue();
        }
    }

    private void loadWifiAmsduBaselineCfg() {
        HwAppQoeResourceManager qoeResourceManager = HwAppQoeResourceManager.getInstance();
        if (qoeResourceManager.getPowerParameterConfigList() == null) {
            Log.e(TAG, "configlist is null");
            return;
        }
        HwPowerParameterConfig loadingAmsduTotalBaseline = qoeResourceManager.getPowerParameterConfig(AMSDU_TOTAL_BASELINE);
        if (loadingAmsduTotalBaseline != null) {
            this.mAmsduTotalBaseline = loadingAmsduTotalBaseline.getPowerParameterValue();
        }
        HwPowerParameterConfig loadingAmsduSmallBaseline = qoeResourceManager.getPowerParameterConfig(AMSDU_SMALL_BASELINE);
        if (loadingAmsduSmallBaseline != null) {
            this.mAmsduSmallBaseline = loadingAmsduSmallBaseline.getPowerParameterValue();
        }
        HwPowerParameterConfig loadingAmsduLargeBaseline = qoeResourceManager.getPowerParameterConfig(AMSDU_LARGE_BASELINE);
        if (loadingAmsduLargeBaseline != null) {
            this.mAmsduLargeBaseline = loadingAmsduLargeBaseline.getPowerParameterValue();
        }
        HwPowerParameterConfig loadingAmsduSmallAbnormalBaseline = qoeResourceManager.getPowerParameterConfig(AMSDU_SMALL_ABNORMAL_BASELINE);
        if (loadingAmsduSmallAbnormalBaseline != null) {
            this.mAmsduSmallAbnormalBaseline = loadingAmsduSmallAbnormalBaseline.getPowerParameterValue();
        }
        HwPowerParameterConfig loadingAddbareqFreqBaseline = qoeResourceManager.getPowerParameterConfig(ADDBAREQ_FREQ_BASELINE);
        if (loadingAddbareqFreqBaseline != null) {
            this.mAddbareqFreqBaseline = loadingAddbareqFreqBaseline.getPowerParameterValue();
        }
        HwPowerParameterConfig loadingGroupRekeyReqBaseline = qoeResourceManager.getPowerParameterConfig(GROUPREKEY_FREQ_BASELINE);
        if (loadingGroupRekeyReqBaseline != null) {
            this.mGroupRekeyFreqBaseline = loadingGroupRekeyReqBaseline.getPowerParameterValue();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadDisconnectParam() {
        HwAppQoeResourceManager qoeResourceManager = HwAppQoeResourceManager.getInstance();
        if (qoeResourceManager.getPowerParameterConfigList() == null) {
            Log.e(TAG, "PowerParameter configlist is null");
            return;
        }
        HwPowerParameterConfig loadingDisconnectFreqBaseline = qoeResourceManager.getPowerParameterConfig(DISCONNECT_FREQ_BASELINE);
        if (loadingDisconnectFreqBaseline == null) {
            Log.e(TAG, "loadingDisconnectFreqBaseline is null");
        } else {
            this.mDisconnectFreqBaseline = loadingDisconnectFreqBaseline.getPowerParameterValue();
        }
    }

    private void registerDisconnectRecevier() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        this.mHwWifiChipPowerAbnormalReceiver = new HwWifiChipPowerAbnormalReceiver();
        this.mContext.registerReceiver(this.mHwWifiChipPowerAbnormalReceiver, filter, PM_CHANGE_WIFI_STATE, null);
    }

    /* access modifiers changed from: private */
    public class HwWifiChipPowerAbnormalReceiver extends BroadcastReceiver {
        private HwWifiChipPowerAbnormalReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                handleNetworkStateChanged(intent);
            }
        }

        private void handleNetworkStateChanged(Intent intent) {
            if (intent != null) {
                if (intent.getParcelableExtra("networkInfo") instanceof NetworkInfo) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    NetworkInfo.DetailedState state = info == null ? NetworkInfo.DetailedState.IDLE : info.getDetailedState();
                    if (state == NetworkInfo.DetailedState.CONNECTED) {
                        HwWifiChipPowerAbnormal.this.mIsWifiConnected = true;
                    } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                        if (HwWifiChipPowerAbnormal.this.mIsWifiConnected) {
                            HwWifiChipPowerAbnormal.access$208(HwWifiChipPowerAbnormal.this);
                        }
                        HwWifiChipPowerAbnormal.this.mIsWifiConnected = false;
                        if (!HwWifiChipPowerAbnormal.this.mInitDisconnect) {
                            HwWifiChipPowerAbnormal.this.loadDisconnectParam();
                            HwWifiChipPowerAbnormal.this.mInitDisconnect = true;
                        }
                        HwWifiChipPowerAbnormal.this.checkDisconnectTime();
                    } else {
                        Log.e(HwWifiChipPowerAbnormal.TAG, "unknown state");
                    }
                } else {
                    Log.e(HwWifiChipPowerAbnormal.TAG, "networkinfo get fail");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkDisconnectTime() {
        if (this.mLastDisconnectTime != 0) {
            long deltaTime = SystemClock.elapsedRealtime() - this.mLastDisconnectTime;
            this.mLastDisconnectTime = SystemClock.elapsedRealtime();
            handleDisconnectTime(deltaTime);
            return;
        }
        this.mLastDisconnectTime = SystemClock.elapsedRealtime();
    }

    private void handleDisconnectTime(long delta) {
        synchronized (this.mLock) {
            long totalDisconnectTime = 0;
            if (this.mDisconnectTimeList.size() < 5) {
                this.mDisconnectTimeList.add(Long.valueOf(delta));
            } else {
                this.mDisconnectTimeList.remove(0);
                this.mDisconnectTimeList.add(Long.valueOf(delta));
            }
            for (int i = 0; i < this.mDisconnectTimeList.size(); i++) {
                totalDisconnectTime += this.mDisconnectTimeList.get(i).longValue();
            }
            if (this.mDisconnectTimeList.size() >= 5 && ((double) totalDisconnectTime) <= this.mDisconnectFreqBaseline) {
                Log.i(TAG, "handle disconnect abnormal and push power");
                LogPower.push((int) WIFI_PKT_FILTER_ABNORMAL);
            }
        }
    }

    private void clearDisconnectStatus() {
        synchronized (this.mLock) {
            this.mDisconnectTimeList.clear();
            this.mInitDisconnect = false;
            this.mDisconnectCurDuration = 0;
            this.mLastDisconnectTime = 0;
            this.mLastDisconnectCurDuration = 0;
        }
    }

    private void registerRecevier() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHECK_ABNORMAL);
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.wifi.HwWifiChipPowerAbnormal.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    Log.w(HwWifiChipPowerAbnormal.TAG, "intent is null.");
                    return;
                }
                String action = intent.getAction();
                if (action == null) {
                    Log.w(HwWifiChipPowerAbnormal.TAG, "action is null.");
                } else if (HwWifiChipPowerAbnormal.ACTION_CHECK_ABNORMAL.equals(action)) {
                    Log.i(HwWifiChipPowerAbnormal.TAG, "onReceive alarm: " + intent.getAction());
                    HwWifiChipPowerAbnormal.this.setRtcAlarm();
                    HwWifiChipPowerAbnormal.this.checkAbnormalPacket();
                }
            }
        }, UserHandle.ALL, filter, null, null);
    }

    private int[] getWifiChipData(String dataType) {
        int cmdStart;
        int cmdLength;
        byte[] buff = {0};
        if (PACKET_FILTER_DATA.equals(dataType)) {
            cmdLength = 4;
            cmdStart = 107;
        } else if (BEACON_TIM_DATA.equals(dataType)) {
            cmdLength = 4;
            cmdStart = 119;
        } else if (AMSDU_ADDBA_GROUPREKEY_DATA.equals(dataType)) {
            cmdLength = 5;
            cmdStart = CMD_GET_SMALL_AMSDU_CNT;
        } else if (TOTAL_TRX_PKTS_DATA.equals(dataType)) {
            cmdLength = 2;
            cmdStart = CMD_GET_TX_TOTAL_CNT;
        } else {
            cmdLength = 3;
            cmdStart = CMD_GET_WORK_TIME;
        }
        int[] wifiChipData = new int[cmdLength];
        for (int index = 0; index < cmdLength; index++) {
            wifiChipData[index] = WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.sendCmdToDriver(wifiStateMachineUtils.getInterfaceName(this.mWifiStateMachine), cmdStart + index, buff);
        }
        return wifiChipData;
    }

    private String formateData(int[] wifiChipData) {
        String result = "";
        if (wifiChipData == null || wifiChipData.length == 0) {
            return result;
        }
        for (int i = 0; i < wifiChipData.length; i++) {
            result = result + String.valueOf(wifiChipData[i]) + ",";
        }
        return result.substring(0, result.length() - 1);
    }

    private void reportApfFilterCount(int[] pktFilterCnt) {
        if (pktFilterCnt != null && pktFilterCnt.length == 4) {
            Bundle chrData = new Bundle();
            for (int i = 0; i < 4; i++) {
                chrData.putInt(PKG_FILTER_NAME[i], pktFilterCnt[i]);
            }
            HwWifiCHRService hwWifiCHRService = this.mHwWifiChrService;
            if (hwWifiCHRService != null) {
                hwWifiCHRService.uploadDFTEvent(12, chrData);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void enableCheckAlarm(boolean isEnable) {
        if (WIFI_CHIP_CHECK_ENABLE) {
            if (isEnable) {
                loadWifiBeaconTimBaselineCfg();
                loadWifiAmsduBaselineCfg();
                setRtcAlarm();
                checkAbnormalPacket();
                return;
            }
            cancelAlarm();
            clearHistoryData();
        }
    }

    private void handleApfFilterCnt(int[] pktFilterCnt, long deltaTime) {
        if (pktFilterCnt == null || deltaTime < 0 || pktFilterCnt.length != 4) {
            Log.e(TAG, "pktFilterCnt is invalid");
            clearHistoryData();
        } else if (pktFilterCnt[0] < 0 || pktFilterCnt[1] < 0 || pktFilterCnt[2] < 0 || pktFilterCnt[3] < 0) {
            this.mPktCurDuration = 0;
            this.mInitFiltPkt = false;
            Log.e(TAG, "wifi pktfilter data error: " + pktFilterCnt[0] + "," + pktFilterCnt[1] + "," + pktFilterCnt[2] + "," + pktFilterCnt[3]);
        } else if (!this.mInitFiltPkt || deltaTime == 0) {
            this.mInitFiltPkt = true;
            this.mPktCurDuration = 0;
            Log.d(TAG, "skip wifi filtpkt first data");
        } else if ((((double) ((long) (((pktFilterCnt[0] + pktFilterCnt[1]) + pktFilterCnt[2]) + pktFilterCnt[3]))) / ((double) deltaTime)) * 1000.0d >= this.mPktBaseline) {
            Log.d(TAG, "wifi filtpkt over baseline");
            this.mPktCurDuration = (int) (((long) this.mPktCurDuration) + deltaTime);
        } else {
            this.mPktCurDuration = 0;
        }
    }

    private void handleBeaconCnt(int[] beaconTimCnt, long deltaTime) {
        if (beaconTimCnt == null || deltaTime < 0 || beaconTimCnt.length != 4) {
            Log.e(TAG, "beaconTimCnt is invalid");
            clearHistoryData();
            return;
        }
        int beaconRealCnt = beaconTimCnt[0] - this.mLastBeaconRealCnt;
        int beaconExpectCnt = beaconTimCnt[1] - this.mLastBeaconExpectCnt;
        if (beaconTimCnt[0] < 0 || beaconTimCnt[1] < 0 || beaconTimCnt[0] > beaconTimCnt[1] || beaconRealCnt < 0 || beaconExpectCnt < 0 || beaconRealCnt > beaconExpectCnt) {
            this.mLastBeaconRealCnt = 0;
            this.mLastBeaconExpectCnt = 0;
            this.mBeaconCurDuration = 0;
            this.mInitBeacon = false;
            Log.e(TAG, "wifi beacon data error: " + beaconTimCnt[0] + ", " + beaconTimCnt[1]);
            return;
        }
        if (!this.mInitBeacon || deltaTime == 0) {
            this.mBeaconCurDuration = 0;
            this.mInitBeacon = true;
            Log.d(TAG, "skip wifi beacon first data");
        } else if (beaconExpectCnt == 0) {
            this.mBeaconCurDuration = 0;
        } else if (((double) (beaconExpectCnt - beaconRealCnt)) / ((double) beaconExpectCnt) >= this.mBeaconBaseline) {
            Log.d(TAG, "wifi beacon over baseline");
            this.mBeaconCurDuration = (int) (((long) this.mBeaconCurDuration) + deltaTime);
        } else {
            this.mBeaconCurDuration = 0;
        }
        this.mLastBeaconRealCnt = beaconTimCnt[0];
        this.mLastBeaconExpectCnt = beaconTimCnt[1];
    }

    private void handleTimCnt(int[] beaconTimCnt, long deltaTime) {
        if (beaconTimCnt == null || deltaTime < 0 || beaconTimCnt.length != 4) {
            Log.e(TAG, "beaconTimCnt is invalid");
            clearHistoryData();
            return;
        }
        int timErrorCnt = beaconTimCnt[2] - this.mLastTimErrorCnt;
        int timTotalCnt = beaconTimCnt[3] - this.mLastTimTotalCnt;
        if (beaconTimCnt[2] >= 0 && beaconTimCnt[3] >= 0 && beaconTimCnt[2] <= beaconTimCnt[3] && timErrorCnt >= 0 && timTotalCnt >= 0) {
            if (timErrorCnt <= timTotalCnt) {
                if (this.mInitTim) {
                    if (deltaTime != 0) {
                        if (timTotalCnt != 0) {
                            double timRatio = ((double) timErrorCnt) / ((double) timTotalCnt);
                            double timFreq = (((double) timTotalCnt) / ((double) deltaTime)) * 1000.0d;
                            double timErrFreq = timRatio * timFreq;
                            if (timRatio < this.mTimRatioBaseline || timFreq < this.mTimFreqBaseline || timErrFreq < this.mTimErrFreqBaseline) {
                                this.mTimCurDuration = 0;
                            } else {
                                Log.d(TAG, "wifi tim over baseline");
                                this.mTimCurDuration = (int) (((long) this.mTimCurDuration) + deltaTime);
                            }
                        } else {
                            this.mTimCurDuration = 0;
                        }
                        this.mLastTimErrorCnt = beaconTimCnt[2];
                        this.mLastTimTotalCnt = beaconTimCnt[3];
                        return;
                    }
                }
                this.mTimCurDuration = 0;
                this.mInitTim = true;
                Log.d(TAG, "skip wifi tim first data");
                this.mLastTimErrorCnt = beaconTimCnt[2];
                this.mLastTimTotalCnt = beaconTimCnt[3];
                return;
            }
        }
        this.mLastTimErrorCnt = 0;
        this.mLastTimTotalCnt = 0;
        this.mTimCurDuration = 0;
        this.mInitTim = false;
        Log.e(TAG, "wifi tim data error: " + beaconTimCnt[2] + ", " + beaconTimCnt[3]);
    }

    private boolean checkAmsduCntAbnormal(int smallCnt, int largeCnt, int mixCnt, int totalCnt, int abnormalCnt) {
        if (smallCnt >= 0 && largeCnt >= 0 && mixCnt >= 0 && totalCnt >= 0 && abnormalCnt >= 0 && smallCnt >= mixCnt) {
            return false;
        }
        Log.e(TAG, "amsduCnt is invalid");
        return true;
    }

    private void handleAmsduCnt(int[] amsduAddbaGrouprekeyCnt, long deltaTime) {
        char c;
        char c2;
        if (amsduAddbaGrouprekeyCnt == null || deltaTime < 0 || amsduAddbaGrouprekeyCnt.length != 5) {
            Log.e(TAG, "amsduAddbaGrouprekeyCnt is invalid");
            clearHistoryData();
            return;
        }
        int smallAmsduCnt = amsduAddbaGrouprekeyCnt[0] - this.mLastSmallAmsduCnt;
        int largeAmsduCnt = amsduAddbaGrouprekeyCnt[1] - this.mLastLargeAmsduCnt;
        int mixAmsduCnt = amsduAddbaGrouprekeyCnt[2] - this.mLastMixAmsduCnt;
        int totalAmsduCnt = smallAmsduCnt + largeAmsduCnt;
        int abnormalAmsduCnt = smallAmsduCnt - mixAmsduCnt;
        if (amsduAddbaGrouprekeyCnt[0] >= 0 && amsduAddbaGrouprekeyCnt[1] >= 0 && amsduAddbaGrouprekeyCnt[2] >= 0) {
            if (!checkAmsduCntAbnormal(smallAmsduCnt, largeAmsduCnt, mixAmsduCnt, totalAmsduCnt, abnormalAmsduCnt)) {
                if (this.mInitAmsdu) {
                    if (deltaTime != 0) {
                        double largeAmsduFrequent = (((double) largeAmsduCnt) / ((double) deltaTime)) * 1000.0d;
                        double mixAmsduFrequent = (((double) mixAmsduCnt) / ((double) deltaTime)) * 1000.0d;
                        double totalAmsduFrequent = (((double) totalAmsduCnt) / ((double) deltaTime)) * 1000.0d;
                        double abnormalAmsduFrequent = (((double) abnormalAmsduCnt) / ((double) deltaTime)) * 1000.0d;
                        if ((((double) smallAmsduCnt) / ((double) deltaTime)) * 1000.0d < this.mAmsduSmallBaseline) {
                            double d = this.mAmsduLargeBaseline;
                            if (largeAmsduFrequent < d && mixAmsduFrequent < d && totalAmsduFrequent < this.mAmsduTotalBaseline && abnormalAmsduFrequent < this.mAmsduSmallAbnormalBaseline) {
                                this.mAmsduCurDuration = 0;
                                c = 0;
                                c2 = 1;
                                this.mLastSmallAmsduCnt = amsduAddbaGrouprekeyCnt[c];
                                this.mLastLargeAmsduCnt = amsduAddbaGrouprekeyCnt[c2];
                                this.mLastMixAmsduCnt = amsduAddbaGrouprekeyCnt[2];
                                return;
                            }
                        }
                        Log.d(TAG, "wifi amsdu over baseline");
                        this.mAmsduCurDuration = (int) (((long) this.mAmsduCurDuration) + deltaTime);
                        c2 = 1;
                        c = 0;
                        this.mLastSmallAmsduCnt = amsduAddbaGrouprekeyCnt[c];
                        this.mLastLargeAmsduCnt = amsduAddbaGrouprekeyCnt[c2];
                        this.mLastMixAmsduCnt = amsduAddbaGrouprekeyCnt[2];
                        return;
                    }
                }
                c2 = 1;
                this.mInitAmsdu = true;
                c = 0;
                this.mAmsduCurDuration = 0;
                Log.d(TAG, "skip wifi amsdu first data");
                this.mLastSmallAmsduCnt = amsduAddbaGrouprekeyCnt[c];
                this.mLastLargeAmsduCnt = amsduAddbaGrouprekeyCnt[c2];
                this.mLastMixAmsduCnt = amsduAddbaGrouprekeyCnt[2];
                return;
            }
        }
        this.mLastSmallAmsduCnt = 0;
        this.mLastLargeAmsduCnt = 0;
        this.mLastMixAmsduCnt = 0;
        this.mAmsduCurDuration = 0;
        this.mInitAmsdu = false;
        Log.e(TAG, "wifi Amsdu error: " + amsduAddbaGrouprekeyCnt[0] + ", " + amsduAddbaGrouprekeyCnt[1] + ", " + amsduAddbaGrouprekeyCnt[2]);
    }

    private void handleAddbareqCnt(int[] amsduAddbaGrouprekeyCnt, long deltaTime) {
        if (amsduAddbaGrouprekeyCnt == null || deltaTime < 0 || amsduAddbaGrouprekeyCnt.length != 5) {
            Log.e(TAG, "amsduAddbaGrouprekeyCnt is invalid");
            clearHistoryData();
            return;
        }
        int addbareqCnt = amsduAddbaGrouprekeyCnt[3] - this.mLastAddbareqCnt;
        if (amsduAddbaGrouprekeyCnt[3] < 0 || addbareqCnt < 0) {
            this.mLastAddbareqCnt = 0;
            this.mInitAddbareq = false;
            this.mAddbareqCurDuration = 0;
            Log.e(TAG, "wifi Addbareq error: " + amsduAddbaGrouprekeyCnt[3]);
            return;
        }
        if (!this.mInitAddbareq || deltaTime == 0) {
            this.mInitAddbareq = true;
            this.mAddbareqCurDuration = 0;
            Log.d(TAG, "skip wifi addba first data");
        } else if ((((double) addbareqCnt) / ((double) deltaTime)) * 1000.0d >= this.mAddbareqFreqBaseline) {
            Log.d(TAG, "wifi addbareq over baseline");
            this.mAddbareqCurDuration = (int) (((long) this.mAddbareqCurDuration) + deltaTime);
        } else {
            this.mAddbareqCurDuration = 0;
        }
        this.mLastAddbareqCnt = amsduAddbaGrouprekeyCnt[3];
    }

    private void handleGrouprekeyCnt(int[] amsduAddbaGrouprekeyCnt, long deltaTime) {
        if (amsduAddbaGrouprekeyCnt == null || deltaTime < 0 || amsduAddbaGrouprekeyCnt.length != 5) {
            Log.e(TAG, "amsduAddbaGrouprekeyCnt is invalid");
            clearHistoryData();
            return;
        }
        int groupRekeyCnt = amsduAddbaGrouprekeyCnt[4] - this.mLastGroupRekeyCnt;
        if (amsduAddbaGrouprekeyCnt[4] < 0 || groupRekeyCnt < 0) {
            this.mLastGroupRekeyCnt = 0;
            this.mInitGroupRekey = false;
            this.mGroupRekeyCurDuration = 0;
            Log.e(TAG, "GroupRekey error: " + amsduAddbaGrouprekeyCnt[4]);
            return;
        }
        if (!this.mInitGroupRekey || deltaTime == 0) {
            this.mInitGroupRekey = true;
            this.mGroupRekeyCurDuration = 0;
            Log.d(TAG, "skip wifi grouprekey first data");
        } else if ((((double) groupRekeyCnt) / ((double) deltaTime)) * 1000.0d >= this.mGroupRekeyFreqBaseline) {
            Log.d(TAG, "wifi grouprekey over baseline");
            this.mGroupRekeyCurDuration = (int) (((long) this.mGroupRekeyCurDuration) + deltaTime);
        } else {
            this.mGroupRekeyCurDuration = 0;
        }
        this.mLastGroupRekeyCnt = amsduAddbaGrouprekeyCnt[4];
    }

    private boolean checkTrxPktsChipTimeAbnormal(int trxCnt, int totalChipTime, int workTime) {
        if (trxCnt >= 0 && totalChipTime >= 0 && workTime >= 0 && totalChipTime >= workTime) {
            return false;
        }
        Log.e(TAG, "checkTrxPktsChipTimeAbnormal:" + trxCnt + ", " + totalChipTime + ", " + workTime);
        return true;
    }

    private void handleTotalTrxCnt(int[] totalTrxCnt, int[] chipWorkTime, long deltaTime) {
        if (totalTrxCnt == null || totalTrxCnt.length != 2 || chipWorkTime == null || chipWorkTime.length != 3 || deltaTime < 0) {
            Log.e(TAG, "totalTrxCnt and chipWorkTime is invalid");
            clearHistoryData();
            return;
        }
        int totalTrxPktsCnt = (totalTrxCnt[0] + totalTrxCnt[1]) - this.mLastTotalTrxPktsCnt;
        int totalChipTime = (chipWorkTime[0] + chipWorkTime[2]) - this.mLastChipTime;
        int workTime = chipWorkTime[0] - this.mLastChipWorkTime;
        if (totalTrxCnt[0] < 0 || totalTrxCnt[1] < 0 || chipWorkTime[0] < 0 || chipWorkTime[2] < 0 || checkTrxPktsChipTimeAbnormal(totalTrxPktsCnt, totalChipTime, workTime)) {
            this.mLastTotalTrxPktsCnt = 0;
            this.mLastChipTime = 0;
            this.mLastChipWorkTime = 0;
            this.mTrxCurDuration = 0;
            this.mInitTrxPkts = false;
            Log.e(TAG, "wifi total trx pkts and chip work time error: " + totalTrxCnt[0] + ", " + totalTrxCnt[1] + ", " + chipWorkTime[0] + ", " + chipWorkTime[2]);
            return;
        }
        if (!this.mInitTrxPkts || deltaTime == 0) {
            this.mInitTrxPkts = true;
            this.mTrxCurDuration = 0;
            Log.d(TAG, "skip wifi trx pkts first data");
        } else {
            double totalTrxFrequent = (((double) totalTrxPktsCnt) / ((double) deltaTime)) * 1000.0d;
            double chipWorkRatio = ((double) workTime) / ((double) totalChipTime);
            Log.i(TAG, "wifi chipdata cur tx, rx pkts:" + totalTrxCnt[0] + "," + totalTrxCnt[1] + ", state time:" + chipWorkTime[2] + ", delta trx pkts:" + totalTrxPktsCnt + ", work time:" + workTime + ", total time:" + totalChipTime);
            if (totalTrxFrequent > this.mTrxPktsBaseline || chipWorkRatio < this.mChipWorkRatioBaseline) {
                this.mTrxCurDuration = 0;
            } else {
                Log.i(TAG, "wifi trx total pkts and work time over baseline");
                this.mTrxCurDuration = (int) (((long) this.mTrxCurDuration) + deltaTime);
            }
        }
        this.mLastTotalTrxPktsCnt = totalTrxCnt[0] + totalTrxCnt[1];
        this.mLastChipTime = chipWorkTime[0] + chipWorkTime[2];
        this.mLastChipWorkTime = chipWorkTime[0];
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAbnormalPacket() {
        long deltaTime = 0;
        if (this.mLastWifiTime != 0) {
            deltaTime = SystemClock.elapsedRealtime() - this.mLastWifiTime;
            if (deltaTime < 10000) {
                this.mLastWifiTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "time interval < 10s, skip it");
                return;
            }
        } else {
            this.mLastWifiTime = SystemClock.elapsedRealtime();
        }
        int[] pkgFilterCnt = getWifiChipData(PACKET_FILTER_DATA);
        reportApfFilterCount(pkgFilterCnt);
        int[] beaconTimCnt = getWifiChipData(BEACON_TIM_DATA);
        handleApfFilterCnt(pkgFilterCnt, deltaTime);
        handleBeaconCnt(beaconTimCnt, deltaTime);
        handleTimCnt(beaconTimCnt, deltaTime);
        int[] amsduAddbaGrouprekeyCnt = getWifiChipData(AMSDU_ADDBA_GROUPREKEY_DATA);
        handleAmsduCnt(amsduAddbaGrouprekeyCnt, deltaTime);
        handleAddbareqCnt(amsduAddbaGrouprekeyCnt, deltaTime);
        handleGrouprekeyCnt(amsduAddbaGrouprekeyCnt, deltaTime);
        if (MTK_CHIP_6889.equals(PROP_WIFI_CHIP_TYPE)) {
            int[] totalTrxCnt = getWifiChipData(TOTAL_TRX_PKTS_DATA);
            int[] chipWorkTime = getWifiChipData(CHIP_WORK_TIME_DATA);
            handleTotalTrxCnt(totalTrxCnt, chipWorkTime, deltaTime);
            Log.i(TAG, "TotalTxPktsCnt, TotalRxPktsCnt: " + totalTrxCnt[0] + "," + totalTrxCnt[1] + ", ChipWorkTime: " + chipWorkTime[0] + "," + chipWorkTime[1] + "," + chipWorkTime[2]);
        }
        if (this.mPktCurDuration >= this.mPktDuration || this.mBeaconCurDuration >= this.mBeaconDuration || this.mTimCurDuration >= this.mTimDuration || this.mAmsduCurDuration >= this.mAmsduDuration || this.mAddbareqCurDuration >= this.mAddbareqDuration || this.mGroupRekeyCurDuration >= this.mGroupRekeyDuration || this.mTrxCurDuration >= this.mTrxAbnormalDuration) {
            Log.i(TAG, "notify abnormal and upload abnormal");
            LogPower.push((int) WIFI_PKT_FILTER_ABNORMAL);
        }
        String pkgFilterData = formateData(pkgFilterCnt);
        String beaconTimData = formateData(beaconTimCnt);
        String amsduAddbaGrouprekeyData = formateData(amsduAddbaGrouprekeyCnt);
        Log.i(TAG, "DeviceFilterCnt, ArpFilterCnt, ApfFilterCnt, IcmpFilterCnt: " + pkgFilterData);
        Log.i(TAG, "RealBeaconCnt, ExpectedBeaconCnt, ErrorTimSetCnt, TotalTimeSetCnt: " + beaconTimData);
        Log.i(TAG, "SmallAmsduCnt, LargeAmsduCnt, MixAmsduCnt, AddbaCnt, GroupRekeyCnt: " + amsduAddbaGrouprekeyData);
        this.mLastWifiTime = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRtcAlarm() {
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (this.mPendingIntent != null) {
            cancelAlarm();
        }
        Intent localIntent = new Intent(ACTION_CHECK_ABNORMAL);
        localIntent.setPackage("android");
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, localIntent, 0);
        alarmManager.set(1, System.currentTimeMillis() + this.ALARM_INTERVAL, this.mPendingIntent);
    }

    private void clearHistoryData() {
        this.mLastWifiTime = 0;
        this.mPktCurDuration = 0;
        this.mBeaconCurDuration = 0;
        this.mTimCurDuration = 0;
        this.mAmsduCurDuration = 0;
        this.mAddbareqCurDuration = 0;
        this.mGroupRekeyCurDuration = 0;
        this.mTrxCurDuration = 0;
        this.mLastBeaconRealCnt = 0;
        this.mLastBeaconExpectCnt = 0;
        this.mLastTimErrorCnt = 0;
        this.mLastTimTotalCnt = 0;
        this.mLastSmallAmsduCnt = 0;
        this.mLastLargeAmsduCnt = 0;
        this.mLastMixAmsduCnt = 0;
        this.mLastAddbareqCnt = 0;
        this.mLastGroupRekeyCnt = 0;
        this.mLastTotalTrxPktsCnt = 0;
        this.mLastChipTime = 0;
        this.mLastChipWorkTime = 0;
        this.mInitFiltPkt = false;
        this.mInitBeacon = false;
        this.mInitTim = false;
        this.mInitAmsdu = false;
        this.mInitAddbareq = false;
        this.mInitGroupRekey = false;
        this.mInitTrxPkts = false;
    }

    private void cancelAlarm() {
        if (this.mPendingIntent != null) {
            ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(this.mPendingIntent);
            this.mPendingIntent = null;
        }
    }
}
