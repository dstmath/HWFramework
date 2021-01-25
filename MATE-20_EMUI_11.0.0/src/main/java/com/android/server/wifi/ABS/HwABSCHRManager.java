package com.android.server.wifi.ABS;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.List;

public class HwABSCHRManager {
    private static final int UPLOAD_TIME_INTERVAL = 86400000;
    private static final int WIFI_CONNECTED_MESSAGE = 1;
    public static final int WIFI_HANDOVER_TYPE_ANTENER_SCREEN_OFF_STATE = 5;
    public static final int WIFI_HANDOVER_TYPE_ANTENER_SCREEN_ON_STATE = 4;
    public static final int WIFI_HANDOVER_TYPE_IN_CALL = 6;
    public static final int WIFI_HANDOVER_TYPE_LONG_CONNECT_STATE = 1;
    public static final int WIFI_HANDOVER_TYPE_NONE = 0;
    public static final int WIFI_HANDOVER_TYPE_OUT_CALL = 7;
    public static final int WIFI_HANDOVER_TYPE_SEARCH_STATE = 3;
    public static final int WIFI_HANDOVER_TYPE_SHORT_CONNECT_STATE = 2;
    public static final int WIFI_HANDOVER_TYPE_SISO_TO_MIMO = 8;
    private static HwABSCHRManager mHwABSCHRManager = null;
    private IntentFilter intentFilter = new IntentFilter();
    private boolean isBootComplete = false;
    private HwABSCHRBlackListEvent mBlackListEvent = null;
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver();
    private Context mContext;
    private HwABSCHRExEvent mCurrentApInfo;
    private Handler mHandler = new Handler() {
        /* class com.android.server.wifi.ABS.HwABSCHRManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                HwABSCHRManager.this.uploadStatistis();
            }
        }
    };
    private HwABSDataBaseManager mHwABSDataBaseManager;
    private boolean mIsNeedReset = false;
    private WifiManager mWifiManager;

    private HwABSCHRManager(Context context) {
        this.mContext = context;
        this.mHwABSDataBaseManager = HwABSDataBaseManager.getInstance(context);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        registerBroadcastReceiver();
        if (this.mHwABSDataBaseManager.getCHRStatistics() == null) {
            this.mHwABSDataBaseManager.inlineAddCHRInfo(new HwABSCHRStatistics());
        }
    }

    public static HwABSCHRManager getInstance(Context context) {
        if (mHwABSCHRManager == null) {
            mHwABSCHRManager = new HwABSCHRManager(context);
        }
        return mHwABSCHRManager;
    }

    private void registerBroadcastReceiver() {
        this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter);
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        private WifiBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                        HwABSCHRManager.this.mHandler.sendEmptyMessage(1);
                    }
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    HwABSCHRManager.this.isBootComplete = true;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadStatistis() {
        if (isNeedToUpload(this.mHwABSDataBaseManager.getCHRStatistics())) {
            HwWifiCHRServiceImpl.getInstance().updateWifiException(909002035, "ABS_STATISICS");
            this.mIsNeedReset = true;
        }
    }

    private boolean isNeedToUpload(HwABSCHRStatistics record) {
        if (record == null || !this.isBootComplete) {
            HwABSUtils.logD(false, "isNeedToUpload record == null", new Object[0]);
            return false;
        }
        long currTime = System.currentTimeMillis();
        HwABSUtils.logD(false, "isNeedToUpload record.last_upload_time = %{public}s System.currentTimeMillis() = %{public}s", String.valueOf(record.last_upload_time), String.valueOf(System.currentTimeMillis()));
        if (currTime - record.last_upload_time > 86400000) {
            HwABSUtils.logD(false, "isNeedToUpload return true", new Object[0]);
            return true;
        }
        HwABSUtils.logD(false, "isNeedToUpload return false", new Object[0]);
        return false;
    }

    private WifiConfiguration getCurrentConfig(int networkId) {
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            return null;
        }
        for (WifiConfiguration nextConfig : configNetworks) {
            if (networkId == nextConfig.networkId) {
                return nextConfig;
            }
        }
        return null;
    }

    private boolean isValid(WifiConfiguration config) {
        if (config == null || config.allowedKeyManagement.cardinality() > 1) {
            return false;
        }
        return true;
    }

    public synchronized void increaseEventStatistics(int type) {
        HwABSUtils.logD(false, "increaseEventStatistics type = %{public}d", Integer.valueOf(type));
        HwABSCHRStatistics statistics = this.mHwABSDataBaseManager.getCHRStatistics();
        if (statistics != null) {
            switch (type) {
                case 1:
                    statistics.long_connect_event++;
                    break;
                case 2:
                    statistics.short_connect_event++;
                    break;
                case 3:
                    statistics.search_event++;
                    break;
                case 4:
                    statistics.antenna_preempted_screen_on_event++;
                    break;
                case 5:
                    statistics.antenna_preempted_screen_off_event++;
                    break;
                case 6:
                    statistics.mo_mt_call_event++;
                    break;
                case 7:
                    statistics.siso_to_mimo_event++;
                    break;
                case 8:
                    statistics.ping_pong_times++;
                    break;
            }
            updateRssiLevel(statistics);
            this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
        }
    }

    private void updateRssiLevel(HwABSCHRStatistics statistics) {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(mWifiInfo.getFrequency(), mWifiInfo.getRssi());
        if (rssiLevel == 0) {
            statistics.mRssiL0++;
        } else if (rssiLevel == 1) {
            statistics.mRssiL1++;
        } else if (rssiLevel == 2) {
            statistics.mRssiL2++;
        } else if (rssiLevel == 3) {
            statistics.mRssiL3++;
        } else if (rssiLevel == 4) {
            statistics.mRssiL4++;
        }
    }

    public static String getAPSSID(WifiInfo mWifiInfo) {
        String ssid = "";
        if (mWifiInfo.getSSID() == null) {
            return "";
        }
        String oriSsid = mWifiInfo.getSSID();
        HwABSUtils.logD(false, "getAPSSID oriSsid = %{public}s", oriSsid);
        if (!oriSsid.equals("") && oriSsid.length() >= 4) {
            ssid = oriSsid.substring(1, oriSsid.length() - 1);
        }
        if (ssid.length() >= 32) {
            return ssid.substring(0, 31);
        }
        return ssid;
    }

    public synchronized void initABSHandoverException(int type) {
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null) {
            WifiConfiguration config = getCurrentConfig(mWifiInfo.getNetworkId());
            if (isValid(config)) {
                this.mCurrentApInfo = new HwABSCHRExEvent();
                this.mCurrentApInfo.mABSApSsid = getAPSSID(mWifiInfo);
                this.mCurrentApInfo.mABSApBssid = mWifiInfo.getBSSID();
                this.mCurrentApInfo.mABSApChannel = mWifiInfo.getFrequency();
                this.mCurrentApInfo.mABSApRSSI = mWifiInfo.getRssi();
                this.mCurrentApInfo.mABSApAuthType = config.getAuthType();
                this.mCurrentApInfo.mSwitchType = type;
            }
        }
    }

    public synchronized HwABSCHRExEvent getExceptionInfo() {
        return this.mCurrentApInfo;
    }

    public HwABSCHRStatistics getStatisticsInfo() {
        HwABSCHRStatistics statistics = this.mHwABSDataBaseManager.getCHRStatistics();
        if (this.mIsNeedReset) {
            HwABSCHRStatistics resetStatistics = new HwABSCHRStatistics();
            resetStatistics.last_upload_time = System.currentTimeMillis();
            this.mHwABSDataBaseManager.inlineUpdateCHRInfo(resetStatistics);
            this.mIsNeedReset = false;
        }
        return statistics;
    }

    public void updateCHRInfo(HwABSCHRStatistics statistics) {
        this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
    }

    public synchronized void uploadABSReassociateExeption() {
        HwABSUtils.logD(false, "uploadABSReassociateExeption", new Object[0]);
        if (this.mCurrentApInfo != null) {
            HwWifiCHRServiceImpl.getInstance().updateWifiException(909002034, "ABS_Reassociate_Exeption");
        }
    }

    public void updateABSTime(String ssid, long mimoTime, long sisoTime, long mimoScreenOnTime, long sisoScreenOnTime) {
        HwABSUtils.logE(false, "updateScreenOffTime mimoTime = %{public}s sisoTime = %{public}s mimoScreenOnTime = %{public}s sisoScreenOnTime = %{public}s", String.valueOf(mimoTime), String.valueOf(sisoTime), String.valueOf(mimoScreenOnTime), String.valueOf(sisoScreenOnTime));
        HwABSCHRStatistics statistics = this.mHwABSDataBaseManager.getCHRStatistics();
        if (ssid == null || statistics == null) {
            HwABSUtils.logE(false, "updateABSTime ssid == null", new Object[0]);
            return;
        }
        statistics.mimo_time += mimoTime;
        statistics.siso_time += sisoTime;
        statistics.mimo_screen_on_time += mimoScreenOnTime;
        statistics.siso_screen_on_time += sisoScreenOnTime;
        this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        HwABSUtils.logE(false, "updateABSTime ssid == %{public}s", StringUtilEx.safeDisplaySsid(ssid));
        mHwWifiCHRService.updateABSTime(ssid, 0, 0, mimoTime, sisoTime, mimoScreenOnTime, sisoScreenOnTime);
    }

    public void updateCHRAssociateTimes(String ssid, int associateTimes, int associateFailedTimes) {
        HwABSUtils.logE(false, "updateCHRAssociateTimes associateTimes = %{public}d associateFailedTimes = %{public}d", Integer.valueOf(associateTimes), Integer.valueOf(associateFailedTimes));
        if (ssid == null) {
            HwABSUtils.logE(false, "updateCHRAssociateTimes ssid == null", new Object[0]);
            return;
        }
        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        HwABSUtils.logE(false, "updateCHRAssociateTimes ssid == %{public}s", StringUtilEx.safeDisplaySsid(ssid));
        mHwWifiCHRService.updateABSTime(ssid, associateTimes, associateFailedTimes, 0, 0, 0, 0);
    }

    public HwABSCHRBlackListEvent getABSBlackListException() {
        return this.mBlackListEvent;
    }

    public void uploadBlackListException(HwABSCHRBlackListEvent event) {
        HwABSUtils.logD(false, "uploadABSBlackListExeption", new Object[0]);
        if (event == null) {
            HwABSUtils.logD(false, "uploadABSBlackListExeption erro null", new Object[0]);
        } else {
            this.mBlackListEvent = event;
        }
    }
}
