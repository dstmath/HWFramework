package com.android.server.wifi.ABS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import com.android.server.wifi.HwWifiCHRStateManagerImpl;
import com.android.server.wifi.HwWifiStatStore;
import com.android.server.wifi.HwWifiStatStoreImpl;
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
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver(this, null);
    private Context mContext;
    private HwABSCHRExEvent mCurrentApInfo;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                HwABSCHRManager.this.uploadStatistis();
            }
        }
    };
    private HwABSDataBaseManager mHwABSDataBaseManager;
    private WifiManager mWifiManager;

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ WifiBroadcastReceiver(HwABSCHRManager this$0, WifiBroadcastReceiver -this1) {
            this();
        }

        private WifiBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo != null && netInfo.getState() == State.CONNECTED) {
                    HwABSCHRManager.this.mHandler.sendEmptyMessage(1);
                }
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwABSCHRManager.this.isBootComplete = true;
            }
        }
    }

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

    private void uploadStatistis() {
        if (isNeedToUpload(this.mHwABSDataBaseManager.getCHRStatistics())) {
            HwWifiCHRStateManagerImpl.getDefault().updateWifiException(216, "ABS_STATISICS");
            HwABSCHRStatistics statistics = new HwABSCHRStatistics();
            statistics.last_upload_time = System.currentTimeMillis();
            this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
        }
    }

    private boolean isNeedToUpload(HwABSCHRStatistics record) {
        if (record == null || (this.isBootComplete ^ 1) != 0) {
            HwABSUtils.logD("isNeedToUpload record == null");
            return false;
        }
        long currTime = System.currentTimeMillis();
        HwABSUtils.logD("isNeedToUpload record.last_upload_time = " + record.last_upload_time + "   System.currentTimeMillis() = " + System.currentTimeMillis());
        if (currTime - record.last_upload_time > 86400000) {
            HwABSUtils.logD("isNeedToUpload return true");
            return true;
        }
        HwABSUtils.logD("isNeedToUpload return false");
        return false;
    }

    private WifiConfiguration getCurrentConfig(int networkId) {
        WifiConfiguration result = null;
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            return null;
        }
        for (WifiConfiguration nextConfig : configNetworks) {
            if (networkId == nextConfig.networkId) {
                result = nextConfig;
                break;
            }
        }
        return result;
    }

    private boolean isValid(WifiConfiguration config) {
        boolean z = true;
        if (config == null) {
            return false;
        }
        if (config.allowedKeyManagement.cardinality() > 1) {
            z = false;
        }
        return z;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void increaseEventStatistics(int type) {
        HwABSUtils.logD("increaseEventStatistics type = " + type);
        HwABSCHRStatistics statistics = this.mHwABSDataBaseManager.getCHRStatistics();
        if (statistics != null) {
            switch (type) {
                case 1:
                    statistics.long_connect_event++;
                case 2:
                    statistics.short_connect_event++;
                case 3:
                    statistics.search_event++;
                    this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
                case 4:
                    statistics.antenna_preempted_screen_on_event++;
                    this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
                case 5:
                    statistics.antenna_preempted_screen_off_event++;
                    this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
                case 6:
                    statistics.mo_mt_call_event++;
                    this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
                case 7:
                    statistics.siso_to_mimo_event++;
                    this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
                case 8:
                    statistics.ping_pong_times++;
                    this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
            }
            this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
        }
    }

    public static String getAPSSID(WifiInfo mWifiInfo) {
        String ssid = "";
        String strAp_Ssid = "";
        if (mWifiInfo.getSSID() == null) {
            return strAp_Ssid;
        }
        String oriSsid = mWifiInfo.getSSID();
        HwABSUtils.logD("getAPSSID oriSsid = " + oriSsid);
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
        return this.mHwABSDataBaseManager.getCHRStatistics();
    }

    public void updateCHRInfo(HwABSCHRStatistics statistics) {
        this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
    }

    public synchronized void uploadABSReassociateExeption() {
        HwABSUtils.logD("uploadABSReassociateExeption");
        if (this.mCurrentApInfo != null) {
            HwWifiCHRStateManagerImpl.getDefault().updateWifiException(215, "ABS_Reassociate_Exeption");
            this.mCurrentApInfo = null;
        }
    }

    public void updateABSTime(String ssid, long mimoTime, long sisoTime, long mimoScreenOnTime, long sisoScreenOnTime) {
        HwABSUtils.logE("updateScreenOffTime mimoTime = " + mimoTime + " sisoTime = " + sisoTime + " mimoScreenOnTime = " + mimoScreenOnTime + " sisoScreenOnTime = " + sisoScreenOnTime);
        HwABSCHRStatistics statistics = this.mHwABSDataBaseManager.getCHRStatistics();
        if (ssid == null || statistics == null) {
            HwABSUtils.logE("updateABSTime ssid == null");
            return;
        }
        statistics.mimo_time += mimoTime;
        statistics.siso_time += sisoTime;
        statistics.mimo_screen_on_time += mimoScreenOnTime;
        statistics.siso_screen_on_time += sisoScreenOnTime;
        this.mHwABSDataBaseManager.inlineUpdateCHRInfo(statistics);
        HwWifiStatStore hwStatStoreIns = HwWifiStatStoreImpl.getDefault();
        HwABSUtils.logE("updateABSTime ssid == " + ssid);
        hwStatStoreIns.updateABSTime(ssid, 0, 0, mimoTime, sisoTime, mimoScreenOnTime, sisoScreenOnTime);
    }

    public void updateCHRAssociateTimes(String ssid, int associateTimes, int associateFailedTimes) {
        HwABSUtils.logE("updateCHRAssociateTimes  associateTimes = " + associateTimes + " associateFailedTimes = " + associateFailedTimes);
        if (ssid == null) {
            HwABSUtils.logE("updateCHRAssociateTimes ssid == null");
            return;
        }
        HwWifiStatStore hwStatStoreIns = HwWifiStatStoreImpl.getDefault();
        HwABSUtils.logE("updateCHRAssociateTimes ssid == " + ssid);
        hwStatStoreIns.updateABSTime(ssid, associateTimes, associateFailedTimes, 0, 0, 0, 0);
    }

    public HwABSCHRBlackListEvent getABSBlackListException() {
        return this.mBlackListEvent;
    }

    public void uploadBlackListException(HwABSCHRBlackListEvent event) {
        HwABSUtils.logD("uploadABSBlackListExeption");
        if (event == null) {
            HwABSUtils.logD("uploadABSBlackListExeption erro null");
            return;
        }
        this.mBlackListEvent = event;
        HwWifiCHRStateManagerImpl.getDefault().updateWifiException(217, "ABS_BlackList_Exeption");
    }
}
