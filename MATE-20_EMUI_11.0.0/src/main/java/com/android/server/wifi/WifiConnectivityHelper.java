package com.android.server.wifi;

import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.WifiNative;
import java.util.ArrayList;

public class WifiConnectivityHelper {
    @VisibleForTesting
    public static final int INVALID_LIST_SIZE = -1;
    private static final String TAG = "WifiConnectivityHelper";
    public String mCurrentScanKeys = "";
    private boolean mFirmwareRoamingSupported = false;
    private int mMaxNumBlacklistBssid = -1;
    private int mMaxNumWhitelistSsid = -1;
    private final WifiNative mWifiNative;

    WifiConnectivityHelper(WifiNative wifiNative) {
        this.mWifiNative = wifiNative;
    }

    public boolean getFirmwareRoamingInfo() {
        this.mFirmwareRoamingSupported = false;
        this.mMaxNumBlacklistBssid = -1;
        this.mMaxNumWhitelistSsid = -1;
        WifiNative wifiNative = this.mWifiNative;
        long fwFeatureSet = wifiNative.getSupportedFeatureSet(wifiNative.getClientInterfaceName());
        Log.i(TAG, "Firmware supported feature set: " + Long.toHexString(fwFeatureSet));
        if ((8388608 & fwFeatureSet) == 0) {
            Log.d(TAG, "Firmware roaming is not supported");
            return true;
        }
        WifiNative.RoamingCapabilities roamingCap = new WifiNative.RoamingCapabilities();
        WifiNative wifiNative2 = this.mWifiNative;
        if (!wifiNative2.getRoamingCapabilities(wifiNative2.getClientInterfaceName(), roamingCap)) {
            Log.e(TAG, "Failed to get firmware roaming capabilities");
        } else if (roamingCap.maxBlacklistSize < 0 || roamingCap.maxWhitelistSize < 0) {
            Log.e(TAG, "Invalid firmware roaming capabilities: max num blacklist bssid=" + roamingCap.maxBlacklistSize + " max num whitelist ssid=" + roamingCap.maxWhitelistSize);
        } else {
            this.mFirmwareRoamingSupported = true;
            this.mMaxNumBlacklistBssid = roamingCap.maxBlacklistSize;
            this.mMaxNumWhitelistSsid = roamingCap.maxWhitelistSize;
            Log.i(TAG, "Firmware roaming supported with capabilities: max num blacklist bssid=" + this.mMaxNumBlacklistBssid + " max num whitelist ssid=" + this.mMaxNumWhitelistSsid);
            return true;
        }
        return false;
    }

    public boolean isFirmwareRoamingSupported() {
        return this.mFirmwareRoamingSupported;
    }

    public int getMaxNumBlacklistBssid() {
        if (this.mFirmwareRoamingSupported) {
            return this.mMaxNumBlacklistBssid;
        }
        Log.e(TAG, "getMaxNumBlacklistBssid: Firmware roaming is not supported");
        return -1;
    }

    public int getMaxNumWhitelistSsid() {
        if (this.mFirmwareRoamingSupported) {
            return this.mMaxNumWhitelistSsid;
        }
        Log.e(TAG, "getMaxNumWhitelistSsid: Firmware roaming is not supported");
        return -1;
    }

    public boolean setFirmwareRoamingConfiguration(ArrayList<String> blacklistBssids, ArrayList<String> whitelistSsids) {
        if (!this.mFirmwareRoamingSupported) {
            Log.e(TAG, "Firmware roaming is not supported");
            return false;
        } else if (blacklistBssids == null || whitelistSsids == null) {
            Log.e(TAG, "Invalid firmware roaming configuration settings");
            return false;
        } else {
            int blacklistSize = blacklistBssids.size();
            int whitelistSize = whitelistSsids.size();
            if (blacklistSize > this.mMaxNumBlacklistBssid || whitelistSize > this.mMaxNumWhitelistSsid) {
                Log.e(TAG, "Invalid BSSID blacklist size " + blacklistSize + " SSID whitelist size " + whitelistSize + ". Max blacklist size: " + this.mMaxNumBlacklistBssid + ", max whitelist size: " + this.mMaxNumWhitelistSsid);
                return false;
            }
            WifiNative.RoamingConfig roamConfig = new WifiNative.RoamingConfig();
            roamConfig.blacklistBssids = blacklistBssids;
            roamConfig.whitelistSsids = whitelistSsids;
            WifiNative wifiNative = this.mWifiNative;
            return wifiNative.configureRoaming(wifiNative.getClientInterfaceName(), roamConfig);
        }
    }

    public void removeNetworkIfCurrent(int networkId) {
        WifiNative wifiNative = this.mWifiNative;
        wifiNative.removeNetworkIfCurrent(wifiNative.getClientInterfaceName(), networkId);
    }
}
