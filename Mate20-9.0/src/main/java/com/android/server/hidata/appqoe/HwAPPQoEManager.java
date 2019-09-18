package com.android.server.hidata.appqoe;

import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import com.android.server.hidata.channelqoe.HwChannelQoEManager;
import com.android.server.hidata.channelqoe.IChannelQoECallback;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;

public class HwAPPQoEManager implements IChannelQoECallback {
    private static final String TAG = "HiData_HwAPPQoEManager";
    private static final int USER_HANDOVER_TO_WIFI = 9;
    private static HwAPPQoEManager mHwAPPQoEManager = null;
    IHwAPPQoECallback mBrainCallback = null;
    private Context mContext;
    private HwAPPQoEStateMachine mHwAPPQoEStateMachine = null;
    private HwChannelQoEManager mHwChannelQoEManager = null;
    IHwAPPQoECallback mWaveMappingCallback = null;

    private HwAPPQoEManager(Context context) {
        this.mContext = context;
        this.mHwAPPQoEStateMachine = HwAPPQoEStateMachine.createHwAPPQoEStateMachine(context);
    }

    public static HwAPPQoEManager createHwAPPQoEManager(Context context) {
        if (mHwAPPQoEManager == null) {
            mHwAPPQoEManager = new HwAPPQoEManager(context);
        }
        return mHwAPPQoEManager;
    }

    public static HwAPPQoEManager getInstance() {
        if (mHwAPPQoEManager != null) {
            return mHwAPPQoEManager;
        }
        return null;
    }

    public void registerAppQoECallback(IHwAPPQoECallback callback, boolean isBrain) {
        if (isBrain) {
            this.mBrainCallback = callback;
        } else {
            this.mWaveMappingCallback = callback;
        }
    }

    public void queryNetworkQuality(int UID, int scence, int network, boolean needRtt) {
        int qci;
        HwAPPQoEUtils.logD(TAG, "queryNetworkQuality UID = " + UID + " scence = " + scence + " network = " + network);
        HwAPPQoEResourceManger manager = HwAPPQoEResourceManger.getInstance();
        if (manager != null) {
            HwAPPQoEAPKConfig conifg = manager.getAPKScenceConfig(scence);
            if (conifg != null) {
                qci = conifg.mQci;
            } else {
                qci = 0;
            }
            this.mHwChannelQoEManager = HwChannelQoEManager.createInstance(this.mContext);
            this.mHwChannelQoEManager.queryChannelQuality(UID, scence, network, qci, this);
        }
    }

    public IHwAPPQoECallback getAPPQoECallback(boolean isBrain) {
        if (isBrain) {
            return this.mBrainCallback;
        }
        return this.mWaveMappingCallback;
    }

    public void startWifiLinkMonitor(int UID, int scence) {
        int qci;
        HwAPPQoEUtils.logD(TAG, "startWifiLinkMonitor UID = " + UID + " scence = " + scence);
        HwAPPQoEResourceManger manager = HwAPPQoEResourceManger.getInstance();
        if (manager != null) {
            HwAPPQoEAPKConfig conifg = manager.getAPKScenceConfig(scence);
            if (conifg != null) {
                qci = conifg.mQci;
            } else {
                qci = 0;
            }
            this.mHwChannelQoEManager = HwChannelQoEManager.createInstance(this.mContext);
            this.mHwChannelQoEManager.startWifiLinkMonitor(UID, scence, qci, this);
        }
    }

    public void stopWifiLinkMonitor(int UID, boolean stopAll) {
        this.mHwChannelQoEManager = HwChannelQoEManager.createInstance(this.mContext);
        this.mHwChannelQoEManager.stopWifiLinkMonitor(UID, stopAll);
    }

    public void onMplinkStateChange(HwAPPStateInfo appInfo, int mplinkEvent, int failReason) {
        HwAPPQoEUtils.logD(TAG, "Enter onMplinkStateChange mplinkEvent = " + mplinkEvent);
        if (mplinkEvent == 9 && appInfo != null) {
            HwAPPChrManager.getInstance().updateStatisInfo(appInfo, 8);
            HwAPPQoEUserAction mHwAPPQoEUserAction = HwAPPQoEUserAction.getInstance();
            if (mHwAPPQoEUserAction != null) {
                Context context = this.mContext;
                Context context2 = this.mContext;
                WifiInfo mWifiInfo = ((WifiManager) context.getSystemService(DevSchedFeatureRT.WIFI_FEATURE)).getConnectionInfo();
                if (mWifiInfo != null && mWifiInfo.getSSID() != null) {
                    mHwAPPQoEUserAction.updateUserActionData(1, appInfo.mAppId, mWifiInfo.getSSID());
                }
            }
        }
    }

    public HwAPPStateInfo getCurAPPStateInfo() {
        HwAPPQoEUtils.logD(TAG, "Enter getCurAPPStateInfo.");
        return this.mHwAPPQoEStateMachine.getCurAPPStateInfo();
    }

    public void logE(String info) {
        Log.e(TAG, info);
    }

    public void onChannelQuality(int UID, int scence, int network, int label) {
        HwAPPQoEUtils.logD(TAG, "onChannelQuality UID = " + UID + " scence =  network = " + network + " label = " + label);
        boolean result = false;
        if (this.mBrainCallback != null) {
            if (label == 0) {
                result = true;
            }
            this.mBrainCallback.onNetworkQualityCallBack(UID, scence, network, result);
        }
    }

    public void onWifiLinkQuality(int UID, int scence, int label) {
        HwAPPQoEUtils.logD(TAG, "onWifiLinkQuality UID = " + UID + " scence = " + scence + " label = " + label);
        boolean result = false;
        if (this.mBrainCallback != null) {
            if (label == 0) {
                result = true;
            }
            this.mBrainCallback.onWifiLinkQuality(UID, scence, result);
        }
    }

    public void onCellPSAvailable(boolean isOK, int reason) {
    }

    private static boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.System.getInt(cr, name, def) == 1;
    }

    public boolean getHidataState() {
        return getSettingsSystemBoolean(this.mContext.getContentResolver(), "smart_network_switching", false);
    }

    public void onCurrentRtt(int rtt) {
    }
}
