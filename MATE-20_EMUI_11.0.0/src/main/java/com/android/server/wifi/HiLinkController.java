package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.MacAddress;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Process;
import android.util.Flog;
import android.util.SparseArray;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.util.NativeUtil;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HiLinkController {
    private static final int CMD_SET = 2;
    private static final int DELIVER_MAC_ADDRESS = 2;
    private static final String TAG = "HiLinkController";
    private static WifiStateMachineUtils wifiStateMachineUtils = EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class);
    private Context mContext = null;
    private boolean mIsHiLinkActive = false;
    private WifiConfiguration mWifiConfiguration = null;
    private ClientModeImpl mWifiStateMachine = null;

    public HiLinkController(Context context, ClientModeImpl wifiStateMachine) {
        this.mContext = context;
        this.mWifiStateMachine = wifiStateMachine;
    }

    public boolean isHiLinkActive() {
        return this.mIsHiLinkActive;
    }

    public void enableHiLinkHandshake(boolean uiEnable, String bssid) {
        HwHiLog.d(TAG, false, "enableHiLinkHandshake:%{public}s", new Object[]{String.valueOf(uiEnable)});
        if (uiEnable) {
            this.mIsHiLinkActive = true;
        } else {
            this.mIsHiLinkActive = false;
        }
        this.mWifiConfiguration = null;
        WifiInjector.getInstance().getWifiNative().mHwWifiNativeEx.enableHiLinkHandshake(uiEnable, bssid);
    }

    public void enableHiLinkHandshake(boolean uiEnable, String bssid, WifiConfiguration config) {
        String macAddress;
        HwHiLog.i(TAG, false, "enableHiLinkHandshake:%{public}s, macRandomizationSetting:%{public}d", new Object[]{String.valueOf(uiEnable), Integer.valueOf(config.macRandomizationSetting)});
        WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
        if (wifiNative == null) {
            HwHiLog.e(TAG, false, "enableHiLinkHandshake:wifinative is null", new Object[0]);
            return;
        }
        if (uiEnable) {
            if (!this.mIsHiLinkActive) {
                wifiNative.mHwWifiNativeEx.enableHiLinkHandshake(uiEnable, bssid);
            }
            if (config.macRandomizationSetting == 1) {
                MacAddress persistentMac = wifiStateMachineUtils.getWifiConfigManager(this.mWifiStateMachine).getPersistentMacAddress(config);
                if (persistentMac == null) {
                    HwHiLog.e(TAG, false, "enableHiLinkHandshake:persistentMac is null", new Object[0]);
                    return;
                }
                macAddress = persistentMac.toString();
            } else {
                macAddress = wifiNative.getFactoryMacAddress("wlan0").toString();
            }
            HwHiLog.i(TAG, false, "enableHiLinkHandshake:macAddress:%{public}s", new Object[]{StringUtilEx.safeDisplayBssid(macAddress)});
            if (macAddress != null) {
                WifiInjector.getInstance().getSupplicantStaIfaceHal().mIHwSupplicantStaIfaceHalEx.deliverStaIfaceData("wlan0", 2, 2, macAddress);
            }
            this.mWifiConfiguration = config;
        } else {
            wifiNative.mHwWifiNativeEx.enableHiLinkHandshake(uiEnable, bssid);
        }
        this.mIsHiLinkActive = uiEnable;
    }

    public void sendWpsOkcStartedBroadcast() {
        HwHiLog.d(TAG, false, "sendBroadcast: android.net.wifi.action.HILINK_STATE_CHANGED", new Object[0]);
        this.mContext.sendBroadcast(new Intent("android.net.wifi.action.HILINK_STATE_CHANGED"), "com.android.server.wifi.permission.HILINK_STATE_CHANGED");
        HwHiLog.d(TAG, false, "report HiLink connect action. EventId:%{public}d ret:%{public}s", new Object[]{400, String.valueOf(Flog.bdReport(this.mContext, 400))});
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b3, code lost:
        r9 = r11;
     */
    public NetworkUpdateResult saveWpsOkcConfiguration(int connectionNetId, String connectionBssid, List<ScanResult> scanResults) {
        WifiConfiguration wifiConfiguration;
        HwHiLog.d(TAG, false, "saveWpsOkcConfiguration: enter", new Object[0]);
        ScanResult connectionScanResult = null;
        if (this.mWifiConfiguration == null) {
            HwHiLog.d(TAG, false, "find out connectionScanResult from scanResults", new Object[0]);
            Iterator<ScanResult> it = scanResults.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ScanResult result = it.next();
                if (Objects.equals(connectionBssid, result.BSSID)) {
                    connectionScanResult = result;
                    break;
                }
            }
            if (connectionScanResult == null || !connectionScanResult.isHiLinkNetwork) {
                HwHiLog.d(TAG, false, "saveWpsOkcConfiguration: return", new Object[0]);
                return null;
            }
        }
        String ifaceName = wifiStateMachineUtils.getInterfaceName(this.mWifiStateMachine);
        Map<String, WifiConfiguration> configs = new HashMap<>();
        if (!WifiInjector.getInstance().getWifiNative().migrateNetworksFromSupplicant(ifaceName, configs, new SparseArray<>())) {
            HwHiLog.e(TAG, false, "Failed to load networks from wpa_supplicant after Wps", new Object[0]);
            return null;
        }
        WifiConfiguration config = null;
        Iterator<WifiConfiguration> it2 = configs.values().iterator();
        while (true) {
            if (!it2.hasNext()) {
                break;
            }
            WifiConfiguration value = it2.next();
            HwHiLog.d(TAG, false, "wpa_supplicant.conf-->config.SSid=%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(value.SSID)});
            if ((connectionScanResult == null || !Objects.equals(connectionScanResult.SSID, NativeUtil.removeEnclosingQuotes(value.SSID))) && ((wifiConfiguration = this.mWifiConfiguration) == null || !Objects.equals(wifiConfiguration.SSID, value.SSID))) {
            }
        }
        if (config == null) {
            HwHiLog.e(TAG, false, "wpa_supplicant doesn't store this hilink bssid", new Object[0]);
            return null;
        }
        int uid = Process.myUid();
        config.networkId = -1;
        config.isHiLinkNetwork = true;
        WifiConfiguration wifiConfiguration2 = this.mWifiConfiguration;
        if (wifiConfiguration2 != null) {
            config.macRandomizationSetting = wifiConfiguration2.macRandomizationSetting;
        }
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this.mWifiStateMachine);
        NetworkUpdateResult result2 = wifiConfigManager.addOrUpdateNetwork(config, uid);
        if (!result2.isSuccess()) {
            HwHiLog.e(TAG, false, "saveWpsOkcConfiguration: failed!", new Object[0]);
            return null;
        }
        int netId = result2.getNetworkId();
        if (!wifiConfigManager.enableNetwork(result2.getNetworkId(), true, uid)) {
            HwHiLog.e(TAG, false, "saveWpsOkcConfiguration: uid %{public}d did not have the permissions to enable=%{public}d", new Object[]{Integer.valueOf(uid), Integer.valueOf(netId)});
            return result2;
        } else if (!wifiConfigManager.updateLastConnectUid(netId, uid)) {
            HwHiLog.e(TAG, false, "saveWpsOkcConfiguration: uid %{public}d with insufficient permissions to connect=%{public}d", new Object[]{Integer.valueOf(uid), Integer.valueOf(netId)});
            return result2;
        } else {
            WifiConnectivityManager wcm = wifiStateMachineUtils.getWifiConnectivityManager(this.mWifiStateMachine);
            if (wcm != null) {
                wcm.setUserConnectChoice(netId);
            }
            this.mWifiStateMachine.saveConnectingNetwork(wifiConfigManager.getConfiguredNetwork(netId));
            return result2;
        }
    }
}
