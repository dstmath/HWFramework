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

public class HwAbsChrManager {
    private static final int MIN_ORI_SSID_LENGTH = 4;
    private static final int SSID_MAX_LENGTH = 32;
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
    private static HwAbsChrManager sHwAbsChrManager = null;
    private IntentFilter intentFilter = new IntentFilter();
    private boolean isBootComplete = false;
    private HwAbsChrBlackListEvent mBlackListEvent = null;
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver();
    private Context mContext;
    private HwAbsChrExEvent mCurrentApInfo;
    private Handler mHandler = new Handler() {
        /* class com.android.server.wifi.ABS.HwAbsChrManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                HwAbsChrManager.this.uploadStatistis();
            }
        }
    };
    private HwAbsDataBaseManager mHwAbsDataBaseManager;
    private boolean mIsNeedReset = false;
    private WifiManager mWifiManager;

    private HwAbsChrManager(Context context) {
        this.mContext = context;
        this.mHwAbsDataBaseManager = HwAbsDataBaseManager.getInstance(context);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        registerBroadcastReceiver();
        if (this.mHwAbsDataBaseManager.getChrStatistics() == null) {
            this.mHwAbsDataBaseManager.inlineAddChrInfo(new HwAbsChrStatistics());
        }
    }

    public static HwAbsChrManager getInstance(Context context) {
        if (sHwAbsChrManager == null) {
            sHwAbsChrManager = new HwAbsChrManager(context);
        }
        return sHwAbsChrManager;
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
                        HwAbsChrManager.this.mHandler.sendEmptyMessage(1);
                    }
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    HwAbsChrManager.this.isBootComplete = true;
                } else {
                    HwAbsUtils.logD(false, "No processing type", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadStatistis() {
        if (isNeedToUpload(this.mHwAbsDataBaseManager.getChrStatistics())) {
            HwWifiCHRServiceImpl.getInstance().updateWifiException(909002035, "ABS_STATISICS");
            this.mIsNeedReset = true;
        }
    }

    private boolean isNeedToUpload(HwAbsChrStatistics record) {
        if (record == null || !this.isBootComplete) {
            HwAbsUtils.logD(false, "isNeedToUpload record == null", new Object[0]);
            return false;
        }
        long currTime = System.currentTimeMillis();
        HwAbsUtils.logD(false, "isNeedToUpload record.lastUploadTime = %{public}s System.currentTimeMillis() = %{public}s", String.valueOf(record.lastUploadTime), String.valueOf(System.currentTimeMillis()));
        if (currTime - record.lastUploadTime > 86400000) {
            HwAbsUtils.logD(false, "isNeedToUpload return true", new Object[0]);
            return true;
        }
        HwAbsUtils.logD(false, "isNeedToUpload return false", new Object[0]);
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
        HwAbsUtils.logD(false, "increaseEventStatistics type = %{public}d", Integer.valueOf(type));
        HwAbsChrStatistics statistics = this.mHwAbsDataBaseManager.getChrStatistics();
        if (statistics != null) {
            switch (type) {
                case 1:
                    statistics.longConnectEvent++;
                    break;
                case 2:
                    statistics.shortConnectEvent++;
                    break;
                case 3:
                    statistics.searchEvent++;
                    break;
                case 4:
                    statistics.antennaPreemptedScreenOnEvent++;
                    break;
                case 5:
                    statistics.antennaPreemptedScreenOffEvent++;
                    break;
                case 6:
                    statistics.moMtCallEvent++;
                    break;
                case 7:
                    statistics.sisoToMimoEvent++;
                    break;
                case 8:
                    statistics.pingPongTimes++;
                    break;
            }
            updateRssiLevel(statistics);
            this.mHwAbsDataBaseManager.inlineUpdateChrInfo(statistics);
        }
    }

    private void updateRssiLevel(HwAbsChrStatistics statistics) {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(wifiInfo.getFrequency(), wifiInfo.getRssi());
        if (rssiLevel == 0) {
            statistics.rssiL0++;
        } else if (rssiLevel == 1) {
            statistics.rssiL1++;
        } else if (rssiLevel == 2) {
            statistics.rssiL2++;
        } else if (rssiLevel == 3) {
            statistics.rssiL3++;
        } else if (rssiLevel == 4) {
            statistics.rssiL4++;
        }
    }

    public static String getApSsid(WifiInfo wifiInfo) {
        String ssid = "";
        if (wifiInfo.getSSID() == null) {
            return "";
        }
        String oriSsid = wifiInfo.getSSID();
        HwAbsUtils.logD(false, "getApSsid oriSsid = %{public}s", oriSsid);
        if (!"".equals(oriSsid) && oriSsid.length() >= 4) {
            ssid = oriSsid.substring(1, oriSsid.length() - 1);
        }
        if (ssid.length() >= 32) {
            return ssid.substring(0, 31);
        }
        return ssid;
    }

    public synchronized void initAbsHandoverException(int type) {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            WifiConfiguration config = getCurrentConfig(wifiInfo.getNetworkId());
            if (isValid(config)) {
                this.mCurrentApInfo = new HwAbsChrExEvent();
                this.mCurrentApInfo.mAbsApSsid = getApSsid(wifiInfo);
                this.mCurrentApInfo.mAbsApBssid = wifiInfo.getBSSID();
                this.mCurrentApInfo.mAbsApChannel = wifiInfo.getFrequency();
                this.mCurrentApInfo.mAbsApRssi = wifiInfo.getRssi();
                this.mCurrentApInfo.mAbsApAuthType = config.getAuthType();
                this.mCurrentApInfo.mSwitchType = type;
            }
        }
    }

    public synchronized HwAbsChrExEvent getExceptionInfo() {
        return this.mCurrentApInfo;
    }

    public HwAbsChrStatistics getStatisticsInfo() {
        HwAbsChrStatistics statistics = this.mHwAbsDataBaseManager.getChrStatistics();
        if (this.mIsNeedReset) {
            HwAbsChrStatistics resetStatistics = new HwAbsChrStatistics();
            resetStatistics.lastUploadTime = System.currentTimeMillis();
            this.mHwAbsDataBaseManager.inlineUpdateChrInfo(resetStatistics);
            this.mHwAbsDataBaseManager.inlineUpdateChrInfo(resetStatistics);
            this.mIsNeedReset = false;
        }
        return statistics;
    }

    public void updateChrInfo(HwAbsChrStatistics statistics) {
        this.mHwAbsDataBaseManager.inlineUpdateChrInfo(statistics);
    }

    public synchronized void uploadAbsReassociateExeption() {
        HwAbsUtils.logD(false, "uploadAbsReassociateExeption", new Object[0]);
        if (this.mCurrentApInfo != null) {
            HwWifiCHRServiceImpl.getInstance().updateWifiException(909002034, "ABS_Reassociate_Exeption");
        }
    }

    public void updateAbsTime(String ssid, long mimoTime, long sisoTime, long mimoScreenOnTime, long sisoScreenOnTime) {
        HwAbsUtils.logE(false, "updateScreenOffTime mimoTime = %{public}s sisoTime = %{public}s mimoScreenOnTime = %{public}s sisoScreenOnTime = %{public}s", String.valueOf(mimoTime), String.valueOf(sisoTime), String.valueOf(mimoScreenOnTime), String.valueOf(sisoScreenOnTime));
        HwAbsChrStatistics statistics = this.mHwAbsDataBaseManager.getChrStatistics();
        if (ssid == null || statistics == null) {
            HwAbsUtils.logE(false, "updateABSTime ssid == null", new Object[0]);
            return;
        }
        statistics.mimoTime += mimoTime;
        statistics.sisoTime += sisoTime;
        statistics.mimoScreenOnTime += mimoScreenOnTime;
        statistics.sisoScreenOnTime += sisoScreenOnTime;
        this.mHwAbsDataBaseManager.inlineUpdateChrInfo(statistics);
        HwWifiCHRService mHwWifiChrService = HwWifiCHRServiceImpl.getInstance();
        HwAbsUtils.logE(false, "updateAbsTime ssid == %{public}s", StringUtilEx.safeDisplaySsid(ssid));
        mHwWifiChrService.updateABSTime(ssid, 0, 0, mimoTime, sisoTime, mimoScreenOnTime, sisoScreenOnTime);
    }

    public void updateChrAssociateTimes(String ssid, int associateTimes, int associateFailedTimes) {
        HwAbsUtils.logE(false, "updateCHRAssociateTimes associateTimes = %{public}d associateFailedTimes = %{public}d", Integer.valueOf(associateTimes), Integer.valueOf(associateFailedTimes));
        if (ssid == null) {
            HwAbsUtils.logE(false, "updateChrAssociateTimes ssid == null", new Object[0]);
            return;
        }
        HwWifiCHRService mHwWifiChrService = HwWifiCHRServiceImpl.getInstance();
        HwAbsUtils.logE(false, "updateChrAssociateTimes ssid == %{public}s", StringUtilEx.safeDisplaySsid(ssid));
        mHwWifiChrService.updateABSTime(ssid, associateTimes, associateFailedTimes, 0, 0, 0, 0);
    }

    public HwAbsChrBlackListEvent getAbsBlackListException() {
        return this.mBlackListEvent;
    }

    public void uploadBlacklistException(HwAbsChrBlackListEvent event) {
        HwAbsUtils.logD(false, "uploadBlacklistException", new Object[0]);
        if (event == null) {
            HwAbsUtils.logD(false, "uploadAbsBlackListExeption error null", new Object[0]);
        } else {
            this.mBlackListEvent = event;
        }
    }
}
