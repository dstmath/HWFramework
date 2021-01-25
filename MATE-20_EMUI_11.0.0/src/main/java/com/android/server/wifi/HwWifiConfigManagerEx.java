package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.WifiConfigurationUtil;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.wifi2.HwWifi2Manager;
import java.util.Collections;
import java.util.List;

public class HwWifiConfigManagerEx implements IHwWifiConfigManagerEx {
    private static final int MAX_HIDDEN_NETWORKS_NUM = 14;
    private static final int MAX_NO_RESORT_HIDDEN_NETWORKS_NUM = 9;
    private static final String TAG = "HwWifiConfigManagerEx";
    public static final WifiConfigurationUtil.WifiConfigurationComparator sScanListTimeComparator = new WifiConfigurationUtil.WifiConfigurationComparator() {
        /* class com.android.server.wifi.HwWifiConfigManagerEx.AnonymousClass1 */

        public int compareNetworksWithSameStatus(WifiConfiguration a, WifiConfiguration b) {
            return Long.compare(0 == b.lastHasInternetTimestamp ? b.lastConnected : b.lastHasInternetTimestamp, 0 == a.lastHasInternetTimestamp ? a.lastConnected : a.lastHasInternetTimestamp);
        }
    };
    private Context mContext;
    private IHwWifiConfigManagerInner mHwWifiCofigManagerInner = null;
    private int mLastPriority = -1;

    public static HwWifiConfigManagerEx createHwWifiConfigManagerEx(IHwWifiConfigManagerInner hwWifiCofigManagerInner, Context context) {
        HwHiLog.d(TAG, false, "createHwWifiConfigManagerEx is called!", new Object[0]);
        return new HwWifiConfigManagerEx(hwWifiCofigManagerInner, context);
    }

    public HwWifiConfigManagerEx(IHwWifiConfigManagerInner hwWifiCofigManagerInner, Context context) {
        this.mHwWifiCofigManagerInner = hwWifiCofigManagerInner;
        this.mContext = context;
    }

    public void enableAllNetworks() {
        boolean networkEnabledStateChanged = false;
        for (WifiConfiguration config : this.mHwWifiCofigManagerInner.getSavedNetworks((int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM)) {
            if (config != null && !config.getNetworkSelectionStatus().isNetworkEnabled() && this.mHwWifiCofigManagerInner.tryEnableNetwork_2(config)) {
                networkEnabledStateChanged = true;
            }
        }
        if (networkEnabledStateChanged) {
            WifiInjector.getInstance().getWifiNative();
            this.mHwWifiCofigManagerInner.sendConfiguredNetworksChangedBroadcast_2();
        }
    }

    public void disableAllNetworksNative() {
        if (WifiInjector.getInstance().getWifiNative() != null) {
            for (WifiConfiguration config : this.mHwWifiCofigManagerInner.getSavedNetworks((int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM)) {
                if (config != null) {
                    config.status = 1;
                }
            }
        }
    }

    public void enableSimNetworks() {
        for (WifiConfiguration config : this.mHwWifiCofigManagerInner.getInternalConfiguredNetworks_2()) {
            if (TelephonyUtil.isSimConfig(config)) {
                this.mHwWifiCofigManagerInner.updateNetworkSelectionStatus_2(config, 0);
            }
        }
    }

    public void partOfAddOrUpdateNetworkInternal(WifiConfiguration config, WifiConfiguration newInternalConfig) {
        int eapMethod;
        if (config.isPortalConnect) {
            newInternalConfig.isPortalConnect = config.isPortalConnect;
            WifiInjector.getInstance().getClientModeImpl().updateLastPortalConnect(config);
        }
        if (config.isHiLinkNetwork) {
            newInternalConfig.isHiLinkNetwork = config.isHiLinkNetwork;
        }
        if (config.enterpriseConfig != null && ((eapMethod = config.enterpriseConfig.getEapMethod()) == 4 || eapMethod == 5 || eapMethod == 6)) {
            newInternalConfig.enterpriseConfig.setEapSubId(config.enterpriseConfig.getEapSubId());
        }
        if (config.wapiPskTypeBcm != -1) {
            newInternalConfig.wapiPskTypeBcm = config.wapiPskTypeBcm;
        }
        if (!TextUtils.isEmpty(config.wapiAsCertBcm)) {
            newInternalConfig.wapiAsCertBcm = NativeUtil.removeEnclosingQuotes(config.wapiAsCertBcm);
        }
        if (!TextUtils.isEmpty(config.wapiUserCertBcm)) {
            newInternalConfig.wapiUserCertBcm = NativeUtil.removeEnclosingQuotes(config.wapiUserCertBcm);
        }
    }

    public String partOfDisableNetWork(int reason, int uid) {
        int appId = UserHandle.getAppId(uid);
        if (appId == 0 || appId == 1000) {
            reason = 15;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(uid);
        HwHiLog.d(TAG, false, "updateNetworkSelectionStatus:%{public}d  %{public}s", new Object[]{Integer.valueOf(reason), packageName});
        return packageName;
    }

    public void partOfRetrieveHiddenNetworkList(List<WifiConfiguration> restoreNetworks, List<WifiConfiguration> networks, List<WifiConfiguration> currentHiddenConfig) {
        if (!currentHiddenConfig.isEmpty()) {
            networks.addAll(0, currentHiddenConfig);
        }
        int hideNetCount = networks.size();
        if (hideNetCount > 14) {
            for (int i = hideNetCount - 1; i >= 9; i--) {
                restoreNetworks.add(networks.remove(i));
            }
            Collections.sort(restoreNetworks, this.mHwWifiCofigManagerInner.get());
            while (hideNetCount > 14) {
                WifiConfiguration updateConfig = restoreNetworks.remove(restoreNetworks.size() - 1);
                updateConfig.hiddenSSID = false;
                this.mHwWifiCofigManagerInner.getMConfiguredNetworks().put(updateConfig);
                HwHiLog.d(TAG, false, "retrieveHiddenNetworkList: update config:%{public}s to hiddenSSID:false", new Object[]{updateConfig.SSID});
                hideNetCount--;
            }
            for (WifiConfiguration config : restoreNetworks) {
                networks.add(config);
            }
        }
    }

    public void initLastPriority() {
        for (WifiConfiguration config : this.mHwWifiCofigManagerInner.getMConfiguredNetworks().valuesForCurrentUser()) {
            if (config.priority > this.mLastPriority) {
                this.mLastPriority = config.priority;
            }
        }
    }

    public boolean updatePriority(WifiConfiguration config, int uid) {
        HwHiLog.d(TAG, false, "updatePriority %{public}d", new Object[]{Integer.valueOf(config.networkId)});
        if (config.networkId == -1) {
            return false;
        }
        if (!WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mHwWifiCofigManagerInner.getManager().getProfiles(this.mHwWifiCofigManagerInner.getCurrentUserId()))) {
            HwHiLog.d(TAG, false, "updatePriority %{public}s: Network config is not visible to current user.", new Object[]{Integer.toString(config.networkId)});
            return false;
        }
        int i = this.mLastPriority;
        if (i == -1 || i > 1000000) {
            HwHiLog.d(TAG, false, "Need to reset the priority, mLastPriority:%{public}d", new Object[]{Integer.valueOf(this.mLastPriority)});
            for (WifiConfiguration config2 : this.mHwWifiCofigManagerInner.getMConfiguredNetworks().valuesForCurrentUser()) {
                if (config2.networkId != -1) {
                    config2.priority = 0;
                    this.mHwWifiCofigManagerInner.addOrUpdateNetwork_2(config2, uid);
                }
            }
            this.mLastPriority = 0;
        }
        int i2 = this.mLastPriority + 1;
        this.mLastPriority = i2;
        config.priority = i2;
        this.mHwWifiCofigManagerInner.addOrUpdateNetwork_2(config, uid);
        return true;
    }

    public WifiConfigurationUtil.WifiConfigurationComparator get() {
        return sScanListTimeComparator;
    }
}
