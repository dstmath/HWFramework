package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Pair;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WifiLastResortWatchdog {
    public static final String BSSID_ANY = "any";
    public static final int FAILURE_CODE_ASSOCIATION = 1;
    public static final int FAILURE_CODE_AUTHENTICATION = 2;
    public static final int FAILURE_CODE_DHCP = 3;
    public static final int FAILURE_THRESHOLD = 7;
    public static final int MAX_BSSID_AGE = 10;
    private static final String TAG = "WifiLastResortWatchdog";
    private Map<String, AvailableNetworkFailureCount> mRecentAvailableNetworks = new HashMap();
    private SelfRecovery mSelfRecovery;
    private Map<String, Pair<AvailableNetworkFailureCount, Integer>> mSsidFailureCount = new HashMap();
    private boolean mVerboseLoggingEnabled = false;
    private boolean mWatchdogAllowedToTrigger = true;
    private boolean mWifiIsConnected = false;
    private WifiMetrics mWifiMetrics;

    public static class AvailableNetworkFailureCount {
        public int age = 0;
        public int associationRejection = 0;
        public int authenticationFailure = 0;
        public WifiConfiguration config;
        public int dhcpFailure = 0;
        public String ssid = "";

        AvailableNetworkFailureCount(WifiConfiguration configParam) {
            this.config = configParam;
        }

        public void incrementFailureCount(int reason) {
            switch (reason) {
                case 1:
                    this.associationRejection++;
                    return;
                case 2:
                    this.authenticationFailure++;
                    return;
                case 3:
                    this.dhcpFailure++;
                    return;
                default:
                    return;
            }
        }

        void resetCounts() {
            this.associationRejection = 0;
            this.authenticationFailure = 0;
            this.dhcpFailure = 0;
        }

        public String toString() {
            return this.ssid + " HasEverConnected: " + (this.config != null ? Boolean.valueOf(this.config.getNetworkSelectionStatus().getHasEverConnected()) : "null_config") + ", Failures: {" + "Assoc: " + this.associationRejection + ", Auth: " + this.authenticationFailure + ", Dhcp: " + this.dhcpFailure + "}";
        }
    }

    WifiLastResortWatchdog(SelfRecovery selfRecovery, WifiMetrics wifiMetrics) {
        this.mSelfRecovery = selfRecovery;
        this.mWifiMetrics = wifiMetrics;
    }

    public void updateAvailableNetworks(List<Pair<ScanDetail, WifiConfiguration>> availableNetworks) {
        String ssid;
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "updateAvailableNetworks: size = " + availableNetworks.size());
        }
        if (availableNetworks != null) {
            for (Pair<ScanDetail, WifiConfiguration> pair : availableNetworks) {
                ScanDetail scanDetail = pair.first;
                WifiConfiguration config = pair.second;
                ScanResult scanResult = scanDetail.getScanResult();
                if (scanResult != null) {
                    String bssid = scanResult.BSSID;
                    ssid = "\"" + scanDetail.getSSID() + "\"";
                    if (this.mVerboseLoggingEnabled) {
                        Log.v(TAG, " " + bssid + ": " + scanDetail.getSSID());
                    }
                    AvailableNetworkFailureCount availableNetworkFailureCount = (AvailableNetworkFailureCount) this.mRecentAvailableNetworks.get(bssid);
                    if (availableNetworkFailureCount == null) {
                        availableNetworkFailureCount = new AvailableNetworkFailureCount(config);
                        availableNetworkFailureCount.ssid = ssid;
                        Pair<AvailableNetworkFailureCount, Integer> ssidFailsAndApCount = (Pair) this.mSsidFailureCount.get(ssid);
                        if (ssidFailsAndApCount == null) {
                            ssidFailsAndApCount = Pair.create(new AvailableNetworkFailureCount(config), Integer.valueOf(1));
                            setWatchdogTriggerEnabled(true);
                        } else {
                            ssidFailsAndApCount = Pair.create((AvailableNetworkFailureCount) ssidFailsAndApCount.first, Integer.valueOf(ssidFailsAndApCount.second.intValue() + 1));
                        }
                        this.mSsidFailureCount.put(ssid, ssidFailsAndApCount);
                    }
                    if (config != null) {
                        availableNetworkFailureCount.config = config;
                    }
                    availableNetworkFailureCount.age = -1;
                    this.mRecentAvailableNetworks.put(bssid, availableNetworkFailureCount);
                }
            }
        }
        Iterator<Entry<String, AvailableNetworkFailureCount>> it = this.mRecentAvailableNetworks.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, AvailableNetworkFailureCount> entry = (Entry) it.next();
            if (((AvailableNetworkFailureCount) entry.getValue()).age < 9) {
                AvailableNetworkFailureCount availableNetworkFailureCount2 = (AvailableNetworkFailureCount) entry.getValue();
                availableNetworkFailureCount2.age++;
            } else {
                ssid = ((AvailableNetworkFailureCount) entry.getValue()).ssid;
                Pair<AvailableNetworkFailureCount, Integer> ssidFails = (Pair) this.mSsidFailureCount.get(ssid);
                if (ssidFails != null) {
                    Integer apCount = Integer.valueOf(((Integer) ssidFails.second).intValue() - 1);
                    if (apCount.intValue() > 0) {
                        this.mSsidFailureCount.put(ssid, Pair.create((AvailableNetworkFailureCount) ssidFails.first, apCount));
                    } else {
                        this.mSsidFailureCount.remove(ssid);
                    }
                } else {
                    Log.d(TAG, "updateAvailableNetworks: SSID to AP count mismatch for " + ssid);
                }
                it.remove();
            }
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, toString());
        }
    }

    public boolean noteConnectionFailureAndTriggerIfNeeded(String ssid, String bssid, int reason) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "noteConnectionFailureAndTriggerIfNeeded: [" + ssid + ", " + bssid + ", " + reason + "]");
        }
        updateFailureCountForNetwork(ssid, bssid, reason);
        boolean isRestartNeeded = checkTriggerCondition();
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "isRestartNeeded = " + isRestartNeeded);
        }
        if (isRestartNeeded) {
            setWatchdogTriggerEnabled(false);
            Log.e(TAG, "Watchdog triggering recovery");
            this.mSelfRecovery.trigger(0);
            incrementWifiMetricsTriggerCounts();
            clearAllFailureCounts();
        }
        return isRestartNeeded;
    }

    public void connectedStateTransition(boolean isEntering) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "connectedStateTransition: isEntering = " + isEntering);
        }
        this.mWifiIsConnected = isEntering;
        if (!this.mWatchdogAllowedToTrigger) {
            this.mWifiMetrics.incrementNumLastResortWatchdogSuccesses();
        }
        if (isEntering) {
            clearAllFailureCounts();
            setWatchdogTriggerEnabled(true);
        }
    }

    private void updateFailureCountForNetwork(String ssid, String bssid, int reason) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "updateFailureCountForNetwork: [" + ssid + ", " + bssid + ", " + reason + "]");
        }
        if ("any".equals(bssid)) {
            incrementSsidFailureCount(ssid, reason);
        } else {
            incrementBssidFailureCount(ssid, bssid, reason);
        }
    }

    private void incrementSsidFailureCount(String ssid, int reason) {
        Pair<AvailableNetworkFailureCount, Integer> ssidFails = (Pair) this.mSsidFailureCount.get(ssid);
        if (ssidFails == null) {
            Log.d(TAG, "updateFailureCountForNetwork: No networks for ssid = " + ssid);
        } else {
            ssidFails.first.incrementFailureCount(reason);
        }
    }

    private void incrementBssidFailureCount(String ssid, String bssid, int reason) {
        AvailableNetworkFailureCount availableNetworkFailureCount = (AvailableNetworkFailureCount) this.mRecentAvailableNetworks.get(bssid);
        if (availableNetworkFailureCount == null) {
            Log.d(TAG, "updateFailureCountForNetwork: Unable to find Network [" + ssid + ", " + bssid + "]");
        } else if (availableNetworkFailureCount.ssid.equals(ssid)) {
            if (availableNetworkFailureCount.config == null && this.mVerboseLoggingEnabled) {
                Log.v(TAG, "updateFailureCountForNetwork: network has no config [" + ssid + ", " + bssid + "]");
            }
            availableNetworkFailureCount.incrementFailureCount(reason);
            incrementSsidFailureCount(ssid, reason);
        } else {
            Log.d(TAG, "updateFailureCountForNetwork: Failed connection attempt has wrong ssid. Failed [" + ssid + ", " + bssid + "], buffered [" + availableNetworkFailureCount.ssid + ", " + bssid + "]");
        }
    }

    private boolean checkTriggerCondition() {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "checkTriggerCondition.");
        }
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            Log.d(TAG, "factory version, don't check Watchdog trigger");
            return false;
        } else if (this.mWifiIsConnected || !this.mWatchdogAllowedToTrigger) {
            return false;
        } else {
            boolean atleastOneNetworkHasEverConnected = false;
            for (Entry<String, AvailableNetworkFailureCount> entry : this.mRecentAvailableNetworks.entrySet()) {
                if (((AvailableNetworkFailureCount) entry.getValue()).config != null && ((AvailableNetworkFailureCount) entry.getValue()).config.getNetworkSelectionStatus().getHasEverConnected()) {
                    atleastOneNetworkHasEverConnected = true;
                }
                if (!isOverFailureThreshold((String) entry.getKey())) {
                    return false;
                }
            }
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "checkTriggerCondition: return = " + atleastOneNetworkHasEverConnected);
            }
            return atleastOneNetworkHasEverConnected;
        }
    }

    private void incrementWifiMetricsTriggerCounts() {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "incrementWifiMetricsTriggerCounts.");
        }
        this.mWifiMetrics.incrementNumLastResortWatchdogTriggers();
        this.mWifiMetrics.addCountToNumLastResortWatchdogAvailableNetworksTotal(this.mSsidFailureCount.size());
        int badAuth = 0;
        int badAssoc = 0;
        int badDhcp = 0;
        for (Entry<String, Pair<AvailableNetworkFailureCount, Integer>> entry : this.mSsidFailureCount.entrySet()) {
            int i;
            if (((AvailableNetworkFailureCount) ((Pair) entry.getValue()).first).authenticationFailure >= 7) {
                i = 1;
            } else {
                i = 0;
            }
            badAuth += i;
            if (((AvailableNetworkFailureCount) ((Pair) entry.getValue()).first).associationRejection >= 7) {
                i = 1;
            } else {
                i = 0;
            }
            badAssoc += i;
            if (((AvailableNetworkFailureCount) ((Pair) entry.getValue()).first).dhcpFailure >= 7) {
                i = 1;
            } else {
                i = 0;
            }
            badDhcp += i;
        }
        if (badAuth > 0) {
            this.mWifiMetrics.addCountToNumLastResortWatchdogBadAuthenticationNetworksTotal(badAuth);
            this.mWifiMetrics.incrementNumLastResortWatchdogTriggersWithBadAuthentication();
        }
        if (badAssoc > 0) {
            this.mWifiMetrics.addCountToNumLastResortWatchdogBadAssociationNetworksTotal(badAssoc);
            this.mWifiMetrics.incrementNumLastResortWatchdogTriggersWithBadAssociation();
        }
        if (badDhcp > 0) {
            this.mWifiMetrics.addCountToNumLastResortWatchdogBadDhcpNetworksTotal(badDhcp);
            this.mWifiMetrics.incrementNumLastResortWatchdogTriggersWithBadDhcp();
        }
    }

    private void clearAllFailureCounts() {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "clearAllFailureCounts.");
        }
        for (Entry<String, AvailableNetworkFailureCount> entry : this.mRecentAvailableNetworks.entrySet()) {
            AvailableNetworkFailureCount failureCount = (AvailableNetworkFailureCount) entry.getValue();
            ((AvailableNetworkFailureCount) entry.getValue()).resetCounts();
        }
        for (Entry<String, Pair<AvailableNetworkFailureCount, Integer>> entry2 : this.mSsidFailureCount.entrySet()) {
            ((AvailableNetworkFailureCount) ((Pair) entry2.getValue()).first).resetCounts();
        }
    }

    Map<String, AvailableNetworkFailureCount> getRecentAvailableNetworks() {
        return this.mRecentAvailableNetworks;
    }

    private void setWatchdogTriggerEnabled(boolean enable) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "setWatchdogTriggerEnabled: enable = " + enable);
        }
        this.mWatchdogAllowedToTrigger = enable;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mWatchdogAllowedToTrigger: ").append(this.mWatchdogAllowedToTrigger);
        sb.append("\nmWifiIsConnected: ").append(this.mWifiIsConnected);
        sb.append("\nmRecentAvailableNetworks: ").append(this.mRecentAvailableNetworks.size());
        for (Entry<String, AvailableNetworkFailureCount> entry : this.mRecentAvailableNetworks.entrySet()) {
            sb.append("\n ").append((String) entry.getKey()).append(": ").append(entry.getValue()).append(", Age: ").append(((AvailableNetworkFailureCount) entry.getValue()).age);
        }
        sb.append("\nmSsidFailureCount:");
        for (Entry<String, Pair<AvailableNetworkFailureCount, Integer>> entry2 : this.mSsidFailureCount.entrySet()) {
            Integer apCount = ((Pair) entry2.getValue()).second;
            sb.append("\n").append((String) entry2.getKey()).append(": ").append(apCount).append(",").append(((Pair) entry2.getValue()).first.toString());
        }
        return sb.toString();
    }

    public boolean isOverFailureThreshold(String bssid) {
        if (getFailureCount(bssid, 1) >= 7 || getFailureCount(bssid, 2) >= 7 || getFailureCount(bssid, 3) >= 7) {
            return true;
        }
        return false;
    }

    public int getFailureCount(String bssid, int reason) {
        AvailableNetworkFailureCount availableNetworkFailureCount = (AvailableNetworkFailureCount) this.mRecentAvailableNetworks.get(bssid);
        if (availableNetworkFailureCount == null) {
            return 0;
        }
        String ssid = availableNetworkFailureCount.ssid;
        Pair<AvailableNetworkFailureCount, Integer> ssidFails = (Pair) this.mSsidFailureCount.get(ssid);
        if (ssidFails == null) {
            Log.d(TAG, "getFailureCount: Could not find SSID count for " + ssid);
            return 0;
        }
        AvailableNetworkFailureCount failCount = ssidFails.first;
        switch (reason) {
            case 1:
                return failCount.associationRejection;
            case 2:
                return failCount.authenticationFailure;
            case 3:
                return failCount.dhcpFailure;
            default:
                return 0;
        }
    }

    protected void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
        }
    }
}
