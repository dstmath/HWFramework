package com.android.server.wifi;

import android.net.wifi.WifiScanLog;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.wifi.WifiNative.RoamingCapabilities;
import com.android.server.wifi.WifiNative.RoamingConfig;
import java.util.ArrayList;
import java.util.Arrays;

public class WifiConnectivityHelper {
    public static int INVALID_LIST_SIZE = -1;
    private static final String TAG = "WifiConnectivityHelper";
    public String mCurrentScanKeys = "";
    private boolean mFirmwareRoamingSupported = false;
    private int mMaxNumBlacklistBssid = INVALID_LIST_SIZE;
    private int mMaxNumWhitelistSsid = INVALID_LIST_SIZE;
    private final WifiNative mWifiNative;

    WifiConnectivityHelper(WifiNative wifiNative) {
        this.mWifiNative = wifiNative;
    }

    public boolean getFirmwareRoamingInfo() {
        this.mFirmwareRoamingSupported = false;
        this.mMaxNumBlacklistBssid = INVALID_LIST_SIZE;
        this.mMaxNumWhitelistSsid = INVALID_LIST_SIZE;
        int fwFeatureSet = this.mWifiNative.getSupportedFeatureSet();
        Log.d(TAG, "Firmware supported feature set: " + Integer.toHexString(fwFeatureSet));
        if ((8388608 & fwFeatureSet) == 0) {
            Log.d(TAG, "Firmware roaming is not supported");
            return true;
        }
        RoamingCapabilities roamingCap = new RoamingCapabilities();
        if (!this.mWifiNative.getRoamingCapabilities(roamingCap)) {
            Log.e(TAG, "Failed to get firmware roaming capabilities");
        } else if (roamingCap.maxBlacklistSize < 0 || roamingCap.maxWhitelistSize < 0) {
            Log.e(TAG, "Invalid firmware roaming capabilities: max num blacklist bssid=" + roamingCap.maxBlacklistSize + " max num whitelist ssid=" + roamingCap.maxWhitelistSize);
        } else {
            this.mFirmwareRoamingSupported = true;
            this.mMaxNumBlacklistBssid = roamingCap.maxBlacklistSize;
            this.mMaxNumWhitelistSsid = roamingCap.maxWhitelistSize;
            Log.d(TAG, "Firmware roaming supported with capabilities: max num blacklist bssid=" + this.mMaxNumBlacklistBssid + " max num whitelist ssid=" + this.mMaxNumWhitelistSsid);
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
        return INVALID_LIST_SIZE;
    }

    public int getMaxNumWhitelistSsid() {
        if (this.mFirmwareRoamingSupported) {
            return this.mMaxNumWhitelistSsid;
        }
        Log.e(TAG, "getMaxNumWhitelistSsid: Firmware roaming is not supported");
        return INVALID_LIST_SIZE;
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
            RoamingConfig roamConfig = new RoamingConfig();
            roamConfig.blacklistBssids = blacklistBssids;
            roamConfig.whitelistSsids = whitelistSsids;
            return this.mWifiNative.configureRoaming(roamConfig);
        }
    }

    public void removeNetworkIfCurrent(int networkId) {
        this.mWifiNative.removeNetworkIfCurrent(networkId);
    }

    public static void localLog(LocalLog localLog, String scanKey, String eventKey, String log) {
        localLog(localLog, scanKey, eventKey, log, null);
    }

    public static void localLog(LocalLog localLog, String scanKey, String eventKey, String log, Object... params) {
        WifiScanLog.getDefault().addEvent(scanKey, eventKey, log, params);
        String fullLog = scanKey + eventKey + " ";
        try {
            fullLog = fullLog + String.format(log, params);
        } catch (Exception e) {
            fullLog = fullLog + log;
            if (params != null) {
                fullLog = fullLog + Arrays.toString(params);
            }
        }
        localLog.log(fullLog);
    }
}
