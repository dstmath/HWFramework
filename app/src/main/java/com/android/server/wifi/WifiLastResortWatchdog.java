package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import android.util.Pair;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WifiLastResortWatchdog {
    public static final String BSSID_ANY = "any";
    private static final boolean DBG = true;
    public static final int FAILURE_CODE_ASSOCIATION = 1;
    public static final int FAILURE_CODE_AUTHENTICATION = 2;
    public static final int FAILURE_CODE_DHCP = 3;
    public static final int FAILURE_THRESHOLD = 7;
    public static final int MAX_BSSID_AGE = 10;
    private static final String TAG = "WifiLastResortWatchdog";
    private static final boolean VDBG = false;
    private Map<String, AvailableNetworkFailureCount> mRecentAvailableNetworks;
    private Map<String, Pair<AvailableNetworkFailureCount, Integer>> mSsidFailureCount;
    private boolean mWatchdogAllowedToTrigger;
    private boolean mWifiIsConnected;
    private WifiMetrics mWifiMetrics;

    public static class AvailableNetworkFailureCount {
        public int age;
        public int associationRejection;
        public int authenticationFailure;
        public WifiConfiguration config;
        public int dhcpFailure;
        public String ssid;

        AvailableNetworkFailureCount(WifiConfiguration configParam) {
            this.ssid = "";
            this.associationRejection = 0;
            this.authenticationFailure = 0;
            this.dhcpFailure = 0;
            this.age = 0;
            this.config = configParam;
        }

        public void incrementFailureCount(int reason) {
            switch (reason) {
                case WifiLastResortWatchdog.FAILURE_CODE_ASSOCIATION /*1*/:
                    this.associationRejection += WifiLastResortWatchdog.FAILURE_CODE_ASSOCIATION;
                case WifiLastResortWatchdog.FAILURE_CODE_AUTHENTICATION /*2*/:
                    this.authenticationFailure += WifiLastResortWatchdog.FAILURE_CODE_ASSOCIATION;
                case WifiLastResortWatchdog.FAILURE_CODE_DHCP /*3*/:
                    this.dhcpFailure += WifiLastResortWatchdog.FAILURE_CODE_ASSOCIATION;
                default:
            }
        }

        void resetCounts() {
            this.associationRejection = 0;
            this.authenticationFailure = 0;
            this.dhcpFailure = 0;
        }

        public String toString() {
            return this.ssid + ", HasEverConnected: " + (this.config != null ? Boolean.valueOf(this.config.getNetworkSelectionStatus().getHasEverConnected()) : "null_config") + ", Failures: {" + "Assoc: " + this.associationRejection + ", Auth: " + this.authenticationFailure + ", Dhcp: " + this.dhcpFailure + "}" + ", Age: " + this.age;
        }
    }

    WifiLastResortWatchdog(WifiMetrics wifiMetrics) {
        this.mRecentAvailableNetworks = new HashMap();
        this.mSsidFailureCount = new HashMap();
        this.mWifiIsConnected = false;
        this.mWatchdogAllowedToTrigger = DBG;
        this.mWifiMetrics = wifiMetrics;
    }

    public void updateAvailableNetworks(List<Pair<ScanDetail, WifiConfiguration>> availableNetworks) {
        String ssid;
        if (availableNetworks != null) {
            for (Pair<ScanDetail, WifiConfiguration> pair : availableNetworks) {
                ScanDetail scanDetail = pair.first;
                WifiConfiguration config = pair.second;
                ScanResult scanResult = scanDetail.getScanResult();
                if (scanResult != null) {
                    String bssid = scanResult.BSSID;
                    ssid = "\"" + scanDetail.getSSID() + "\"";
                    AvailableNetworkFailureCount availableNetworkFailureCount = (AvailableNetworkFailureCount) this.mRecentAvailableNetworks.get(bssid);
                    if (availableNetworkFailureCount == null) {
                        availableNetworkFailureCount = new AvailableNetworkFailureCount(config);
                        availableNetworkFailureCount.ssid = ssid;
                        Pair<AvailableNetworkFailureCount, Integer> ssidFailsAndApCount = (Pair) this.mSsidFailureCount.get(ssid);
                        if (ssidFailsAndApCount == null) {
                            ssidFailsAndApCount = Pair.create(new AvailableNetworkFailureCount(config), Integer.valueOf(FAILURE_CODE_ASSOCIATION));
                            setWatchdogTriggerEnabled(DBG);
                        } else {
                            ssidFailsAndApCount = Pair.create((AvailableNetworkFailureCount) ssidFailsAndApCount.first, Integer.valueOf(ssidFailsAndApCount.second.intValue() + FAILURE_CODE_ASSOCIATION));
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
                availableNetworkFailureCount2.age += FAILURE_CODE_ASSOCIATION;
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
    }

    public boolean noteConnectionFailureAndTriggerIfNeeded(String ssid, String bssid, int reason) {
        updateFailureCountForNetwork(ssid, bssid, reason);
        boolean isRestartNeeded = checkTriggerCondition();
        if (isRestartNeeded) {
            setWatchdogTriggerEnabled(false);
            restartWifiStack();
            incrementWifiMetricsTriggerCounts();
            clearAllFailureCounts();
        }
        return isRestartNeeded;
    }

    public void connectedStateTransition(boolean isEntering) {
        this.mWifiIsConnected = isEntering;
        if (isEntering) {
            clearAllFailureCounts();
            setWatchdogTriggerEnabled(DBG);
        }
    }

    private void updateFailureCountForNetwork(String ssid, String bssid, int reason) {
        if (BSSID_ANY.equals(bssid)) {
            incrementSsidFailureCount(ssid, reason);
        } else {
            incrementBssidFailureCount(ssid, bssid, reason);
        }
    }

    private void incrementSsidFailureCount(String ssid, int reason) {
        Pair<AvailableNetworkFailureCount, Integer> ssidFails = (Pair) this.mSsidFailureCount.get(ssid);
        if (ssidFails == null) {
            Log.v(TAG, "updateFailureCountForNetwork: No networks for ssid = " + ssid);
        } else {
            ssidFails.first.incrementFailureCount(reason);
        }
    }

    private void incrementBssidFailureCount(String ssid, String bssid, int reason) {
        AvailableNetworkFailureCount availableNetworkFailureCount = (AvailableNetworkFailureCount) this.mRecentAvailableNetworks.get(bssid);
        if (availableNetworkFailureCount == null) {
            Log.d(TAG, "updateFailureCountForNetwork: Unable to find Network [" + ssid + ", " + bssid + "]");
        } else if (availableNetworkFailureCount.ssid.equals(ssid)) {
            if (availableNetworkFailureCount.config == null) {
                availableNetworkFailureCount.incrementFailureCount(reason);
                incrementSsidFailureCount(ssid, reason);
            } else {
                availableNetworkFailureCount.incrementFailureCount(reason);
                incrementSsidFailureCount(ssid, reason);
            }
        } else {
            Log.d(TAG, "updateFailureCountForNetwork: Failed connection attempt has wrong ssid. Failed [" + ssid + ", " + bssid + "], buffered [" + availableNetworkFailureCount.ssid + ", " + bssid + "]");
        }
    }

    private boolean checkTriggerCondition() {
        if (this.mWifiIsConnected || !this.mWatchdogAllowedToTrigger) {
            return false;
        }
        boolean atleastOneNetworkHasEverConnected = false;
        for (Entry<String, AvailableNetworkFailureCount> entry : this.mRecentAvailableNetworks.entrySet()) {
            if (((AvailableNetworkFailureCount) entry.getValue()).config != null && ((AvailableNetworkFailureCount) entry.getValue()).config.getNetworkSelectionStatus().getHasEverConnected()) {
                atleastOneNetworkHasEverConnected = DBG;
            }
            if (!isOverFailureThreshold((String) entry.getKey())) {
                return false;
            }
        }
        return atleastOneNetworkHasEverConnected;
    }

    private void restartWifiStack() {
        Log.i(TAG, "Triggered.");
        Log.d(TAG, toString());
    }

    private void incrementWifiMetricsTriggerCounts() {
        this.mWifiMetrics.incrementNumLastResortWatchdogTriggers();
        this.mWifiMetrics.addCountToNumLastResortWatchdogAvailableNetworksTotal(this.mSsidFailureCount.size());
        int badAuth = 0;
        int badAssoc = 0;
        int badDhcp = 0;
        for (Entry<String, Pair<AvailableNetworkFailureCount, Integer>> entry : this.mSsidFailureCount.entrySet()) {
            int i;
            if (((AvailableNetworkFailureCount) ((Pair) entry.getValue()).first).authenticationFailure >= FAILURE_THRESHOLD) {
                i = FAILURE_CODE_ASSOCIATION;
            } else {
                i = 0;
            }
            badAuth += i;
            if (((AvailableNetworkFailureCount) ((Pair) entry.getValue()).first).associationRejection >= FAILURE_THRESHOLD) {
                i = FAILURE_CODE_ASSOCIATION;
            } else {
                i = 0;
            }
            badAssoc += i;
            if (((AvailableNetworkFailureCount) ((Pair) entry.getValue()).first).dhcpFailure >= FAILURE_THRESHOLD) {
                i = FAILURE_CODE_ASSOCIATION;
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
        this.mWatchdogAllowedToTrigger = enable;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mWatchdogAllowedToTrigger: ").append(this.mWatchdogAllowedToTrigger);
        sb.append("\nmWifiIsConnected: ").append(this.mWifiIsConnected);
        sb.append("\nmRecentAvailableNetworks: ").append(this.mRecentAvailableNetworks.size());
        for (Entry<String, AvailableNetworkFailureCount> entry : this.mRecentAvailableNetworks.entrySet()) {
            sb.append("\n ").append((String) entry.getKey()).append(": ").append(entry.getValue());
        }
        sb.append("\nmSsidFailureCount:");
        for (Entry<String, Pair<AvailableNetworkFailureCount, Integer>> entry2 : this.mSsidFailureCount.entrySet()) {
            Integer apCount = ((Pair) entry2.getValue()).second;
            sb.append("\n").append((String) entry2.getKey()).append(": ").append(apCount).append(", ").append(((Pair) entry2.getValue()).first.toString());
        }
        return sb.toString();
    }

    public boolean isOverFailureThreshold(String bssid) {
        if (getFailureCount(bssid, FAILURE_CODE_ASSOCIATION) >= FAILURE_THRESHOLD || getFailureCount(bssid, FAILURE_CODE_AUTHENTICATION) >= FAILURE_THRESHOLD || getFailureCount(bssid, FAILURE_CODE_DHCP) >= FAILURE_THRESHOLD) {
            return DBG;
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
            case FAILURE_CODE_ASSOCIATION /*1*/:
                return failCount.associationRejection;
            case FAILURE_CODE_AUTHENTICATION /*2*/:
                return failCount.authenticationFailure;
            case FAILURE_CODE_DHCP /*3*/:
                return failCount.dhcpFailure;
            default:
                return 0;
        }
    }
}
