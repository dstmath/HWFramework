package com.android.server;

import android.app.Notification;
import android.common.HwFrameworkFactory;
import android.net.IConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import com.android.server.connectivity.NetworkAgentInfo;
import java.util.List;

public abstract class AbstractConnectivityService extends IConnectivityManager.Stub {
    protected static final int DUAL_CARD_MMS_DELAY = 300000;
    protected static final int EVENT_RESTORE_DATA_SUB = 16;
    public static final int EVENT_SET_EXPLICITLY_UNSELECTED = 528585;
    public static final int EVENT_TRIGGER_INVALIDLINK_NETWORK_MONITOR = 528588;
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

    /* access modifiers changed from: protected */
    public void handleConnect(int netType) {
    }

    /* access modifiers changed from: protected */
    public void handleConnecting(NetworkInfo info, NetworkInfo.State state) {
    }

    public void setSmartKeyguardLevel(String level) {
    }

    public void setUseCtrlSocket(boolean flag) {
    }

    public void setApIpv4AddressFixed(boolean isFixed) {
    }

    public boolean isApIpv4AddressFixed() {
        return false;
    }

    /* access modifiers changed from: protected */
    public String getMmsFeature(String feature) {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isAlwaysAllowMMSforRoaming(int networkType, String feature) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isMmsAutoSetSubDiffFromDataSub(int networkType, String feature) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isMmsSubDiffFromDataSub(int networkType, String feature) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isNetRequestersPidsContainCurrentPid(List<Integer>[] listArr, int usedNetworkType, Integer currentPid) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isNeedTearMmsAndRestoreData(int networkType, String feature, Handler mHandler) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isMultiSimEnabled() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().isMultiSimEnabled();
    }

    /* access modifiers changed from: protected */
    public boolean needSetUserDataEnabled(boolean enabled) {
        return true;
    }

    public boolean startBrowserForWifiPortal(Notification notification, String ssid) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
    }

    /* access modifiers changed from: protected */
    public void hintUserSwitchToMobileWhileWifiDisconnected(NetworkInfo.State state, int type) {
    }

    /* access modifiers changed from: protected */
    public void enableDefaultTypeApnWhenWifiConnectionStateChanged(NetworkInfo.State state, int type) {
    }

    /* access modifiers changed from: protected */
    public void enableDefaultTypeApnWhenBlueToothTetheringStateChanged(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
    }

    /* access modifiers changed from: protected */
    public boolean reportPortalNetwork(NetworkAgentInfo nai, int result) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean ignoreRemovedByWifiPro(NetworkAgentInfo nai) {
        return false;
    }

    public Network getNetworkForTypeWifi() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void setVpnSettingValue(boolean enable) {
    }

    /* access modifiers changed from: protected */
    public boolean isNetworkRequestBip(NetworkRequest nr) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkSupportBip(NetworkAgentInfo nai, NetworkRequest nr) {
        return false;
    }

    public void setLteMobileDataEnabled(boolean enable) {
    }

    public int checkLteConnectState() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void handleLteMobileDataStateChange(NetworkInfo info) {
    }

    public long getLteTotalRxBytes() {
        return 0;
    }

    public long getLteTotalTxBytes() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean ifNeedToStartLteMmsTimer(NetworkRequest request) {
        return false;
    }

    /* access modifiers changed from: protected */
    public NetworkCapabilities changeWifiMmsNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void wifiMmsRelease(NetworkRequest networkRequest) {
    }

    public boolean turnOffVpn(String packageName, int userId) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateDefaultNetworkRouting(NetworkAgentInfo oldDefaultNet, NetworkAgentInfo newDefaultNet) {
    }

    public void sendNetworkStickyBroadcastAsUser(String action, NetworkAgentInfo na) {
    }
}
