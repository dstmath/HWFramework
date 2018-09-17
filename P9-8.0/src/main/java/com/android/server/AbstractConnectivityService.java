package com.android.server;

import android.app.Notification;
import android.common.HwFrameworkFactory;
import android.net.IConnectivityManager.Stub;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.NetworkRequest;
import android.os.Handler;
import com.android.server.connectivity.NetworkAgentInfo;
import java.util.List;

public abstract class AbstractConnectivityService extends Stub {
    protected static final int DUAL_CARD_MMS_DELAY = 300000;
    protected static final int EVENT_RESTORE_DATA_SUB = 16;
    public static final int EVENT_SET_EXPLICITLY_UNSELECTED = 528585;
    public static final int EVENT_TRIGGER_ROAMING_NETWORK_MONITOR = 528587;
    public static final int EVENT_UPDATE_NETWORK_CONCURRENTLY = 528586;
    protected static final int RESTORE_DATA_SUB_AFTER_MMS_DISCON_DELAY = 2000;
    protected static final int RESTORE_DATA_SUB_DELAY = 10000;
    protected boolean dataSubRestoreRequested = false;
    protected int lastPrefDataSubscription = 0;
    protected boolean mmsNetConnectRequested = false;

    protected enum DomainPreferType {
        DOMAIN_ONLY_WIFI(0),
        DOMAIN_PREFER_VOLTE(1),
        DOMAIN_PREFER_WIFI(2),
        DOMAIN_PREFER_CELLULAR(3);
        
        private int mValue;

        private DomainPreferType(int value) {
            this.mValue = value;
        }

        public int value() {
            return this.mValue;
        }

        public static DomainPreferType fromInt(int value) {
            for (DomainPreferType e : values()) {
                if (e.mValue == value) {
                    return e;
                }
            }
            return null;
        }

        public String toString() {
            switch (this.mValue) {
                case 0:
                    return "DOMAIN_ONLY_WIFI";
                case 1:
                    return "DOMAIN_PREFER_VOLTE";
                case 2:
                    return "DOMAIN_PREFER_WIFI";
                case 3:
                    return "DOMAIN_PREFER_CELLULAR";
                default:
                    return "DOMAIN_PREFER_UNKNOWN";
            }
        }
    }

    protected void handleConnect(int netType) {
    }

    protected void handleConnecting(NetworkInfo info, State state) {
    }

    public void setSmartKeyguardLevel(String level) {
    }

    public void setUseCtrlSocket(boolean flag) {
    }

    protected String getMmsFeature(String feature) {
        return null;
    }

    protected boolean isAlwaysAllowMMSforRoaming(int networkType, String feature) {
        return false;
    }

    protected boolean isMmsAutoSetSubDiffFromDataSub(int networkType, String feature) {
        return false;
    }

    protected boolean isMmsSubDiffFromDataSub(int networkType, String feature) {
        return false;
    }

    protected boolean isNetRequestersPidsContainCurrentPid(List<Integer>[] listArr, int usedNetworkType, Integer currentPid) {
        return true;
    }

    protected boolean isNeedTearMmsAndRestoreData(int networkType, String feature, Handler mHandler) {
        return false;
    }

    protected boolean isMultiSimEnabled() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().isMultiSimEnabled();
    }

    protected boolean needSetUserDataEnabled(boolean enabled) {
        return true;
    }

    public boolean startBrowserForWifiPortal(Notification notification, String ssid) {
        return false;
    }

    protected void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
    }

    protected void hintUserSwitchToMobileWhileWifiDisconnected(State state, int type) {
    }

    protected void enableDefaultTypeApnWhenWifiConnectionStateChanged(State state, int type) {
    }

    protected void enableDefaultTypeApnWhenBlueToothTetheringStateChanged(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
    }

    protected void setExplicitlyUnselected(NetworkAgentInfo nai) {
    }

    protected void updateNetworkConcurrently(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
    }

    protected boolean reportPortalNetwork(NetworkAgentInfo nai, int result) {
        return false;
    }

    protected boolean ignoreRemovedByWifiPro(NetworkAgentInfo nai) {
        return false;
    }

    public Network getNetworkForTypeWifi() {
        return null;
    }

    protected void setVpnSettingValue(boolean enable) {
    }

    public void triggerRoamingNetworkMonitor(NetworkAgentInfo networkAgent) {
    }

    protected boolean isNetworkRequestBip(NetworkRequest nr) {
        return false;
    }

    protected boolean checkNetworkSupportBip(NetworkAgentInfo nai, NetworkRequest nr) {
        return false;
    }

    public void setLteMobileDataEnabled(boolean enable) {
    }

    public int checkLteConnectState() {
        return -1;
    }

    protected void handleLteMobileDataStateChange(NetworkInfo info) {
    }

    public long getLteTotalRxBytes() {
        return 0;
    }

    public long getLteTotalTxBytes() {
        return 0;
    }

    protected boolean ifNeedToStartLteMmsTimer(NetworkRequest request) {
        return false;
    }

    protected NetworkCapabilities changeWifiMmsNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        return null;
    }

    protected void wifiMmsRelease(NetworkRequest networkRequest) {
    }

    public boolean turnOffVpn(String packageName, int userId) {
        return false;
    }
}
