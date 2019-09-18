package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.util.PasspointUtil;
import com.android.server.wifi.util.ScanResultUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WifiNetworkSelector {
    private static final long CONNECT_CHOICE_INVALID = 0;
    private static final long CONNECT_CHOICE_TIMEOUT = 50000;
    public static final int EVALUATOR_MIN_PRIORITY = 6;
    private static final long INVALID_TIME_STAMP = Long.MIN_VALUE;
    public static final int MAX_NUM_EVALUATORS = 6;
    @VisibleForTesting
    public static final int MINIMUM_NETWORK_SELECTION_INTERVAL_MS = 10000;
    private static final String TAG = "WifiNetworkSelector";
    private final Clock mClock;
    private volatile List<Pair<ScanDetail, WifiConfiguration>> mConnectableNetworks = new ArrayList();
    public WifiConnectivityHelper mConnectivityHelper;
    private Context mContext;
    public String mCurrentScanKeys = "";
    private final boolean mEnableAutoJoinWhenAssociated;
    private final NetworkEvaluator[] mEvaluators = new NetworkEvaluator[6];
    private List<ScanDetail> mFilteredNetworks = new ArrayList();
    private long mLastNetworkSelectionTimeStamp = INVALID_TIME_STAMP;
    private final LocalLog mLocalLog;
    private final ScoringParams mScoringParams;
    private final int mStayOnNetworkMinimumRxRate;
    private final int mStayOnNetworkMinimumTxRate;
    private final WifiConfigManager mWifiConfigManager;

    public interface NetworkEvaluator {
        WifiConfiguration evaluateNetworks(List<ScanDetail> list, WifiConfiguration wifiConfiguration, String str, boolean z, boolean z2, List<Pair<ScanDetail, WifiConfiguration>> list2);

        String getName();

        void update(List<ScanDetail> list);
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
    }

    private boolean isCurrentNetworkSufficient(WifiInfo wifiInfo, List<ScanDetail> scanDetails) {
        WifiConfiguration network = this.mWifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
        if (network == null) {
            localLog("No current connected network.");
            return false;
        }
        localLog("Current connected network: " + network.SSID + " , ID: " + network.networkId);
        int currentRssi = wifiInfo.getRssi();
        boolean hasQualifiedRssi = currentRssi > this.mScoringParams.getSufficientRssi(wifiInfo.getFrequency());
        boolean hasActiveStream = wifiInfo.txSuccessRate > ((double) this.mStayOnNetworkMinimumTxRate) || wifiInfo.rxSuccessRate > ((double) this.mStayOnNetworkMinimumRxRate);
        if (hasQualifiedRssi && hasActiveStream) {
            localLog("Stay on current network because of good RSSI and ongoing traffic");
            return true;
        } else if (network.ephemeral) {
            localLog("Current network is an ephemeral one.");
            return false;
        } else if (WifiConfigurationUtil.isConfigForOpenNetwork(network)) {
            localLog("Current network is a open one.");
            return false;
        } else if (wifiInfo.is24GHz() && is5GHzNetworkAvailable(scanDetails)) {
            localLog("Current network is 2.4GHz. 5GHz networks available.");
            return false;
        } else if (!hasQualifiedRssi) {
            localLog("Current network RSSI[" + currentRssi + "]-acceptable but not qualified.");
            return false;
        } else if (network.numNoInternetAccessReports <= 0 || network.noInternetAccessExpected) {
            return true;
        } else {
            localLog("Current network has [" + network.numNoInternetAccessReports + "] no-internet access reports.");
            return false;
        }
    }

    private boolean is5GHzNetworkAvailable(List<ScanDetail> scanDetails) {
        for (ScanDetail detail : scanDetails) {
            if (detail.getScanResult().is5GHz()) {
                return true;
            }
        }
        return false;
    }

    private boolean isNetworkSelectionNeeded(List<ScanDetail> scanDetails, WifiInfo wifiInfo, boolean connected, boolean disconnected, String keys) {
        if (scanDetails.size() == 0) {
            localLog(keys + "Empty connectivity scan results. Skip network selection.");
            return false;
        } else if (connected) {
            if (this.mContext != null && "true".equals(Settings.Global.getString(this.mContext.getContentResolver(), "hw_wifipro_enable"))) {
                return false;
            }
            if (this.mLastNetworkSelectionTimeStamp != INVALID_TIME_STAMP) {
                long gap = this.mClock.getElapsedSinceBootMillis() - this.mLastNetworkSelectionTimeStamp;
                if (gap < 10000) {
                    localLog(keys, "48", "Too short since last network selection: %s ms.  Skip network selection.", Long.valueOf(gap));
                    return false;
                }
            }
            if (isCurrentNetworkSufficient(wifiInfo, scanDetails)) {
                localLog(keys, "49", "Current connected network already sufficient. Skip network selection.");
                return false;
            }
            localLog("Current connected network is not sufficient.");
            return true;
        } else if (disconnected) {
            return true;
        } else {
            localLog(keys, "50", "WifiStateMachine is in neither CONNECTED nor DISCONNECTED state. Skip network selection.");
            return false;
        }
    }

    public static String toScanId(ScanResult scanResult) {
        if (scanResult == null) {
            return "NULL";
        }
        return String.format("%s:%s", new Object[]{scanResult.SSID, ScanResultUtil.getConfusedBssid(scanResult.BSSID)});
    }

    public static String toNetworkString(WifiConfiguration network) {
        if (network == null) {
            return null;
        }
        return network.SSID + ":" + network.networkId;
    }

    public boolean isSignalTooWeak(ScanResult scanResult) {
        return scanResult.level < this.mScoringParams.getEntryRssi(scanResult.frequency);
    }

    private List<ScanDetail> filterScanResults(List<ScanDetail> scanDetails, HashSet<String> bssidBlacklist, boolean isConnected, String currentBssid, String keys) {
        String str = currentBssid;
        String str2 = keys;
        localLog(str2, "51", "filterScanResults begin");
        new ArrayList();
        List<ScanDetail> validScanDetails = new ArrayList<>();
        StringBuffer noValidSsid = new StringBuffer();
        StringBuffer blacklistedBssid = new StringBuffer();
        StringBuffer lowRssi = new StringBuffer();
        boolean scanResultsHaveCurrentBssid = false;
        for (ScanDetail scanDetail : scanDetails) {
            ScanResult scanResult = scanDetail.getScanResult();
            if (TextUtils.isEmpty(scanResult.SSID)) {
                noValidSsid.append(ScanResultUtil.getConfusedBssid(scanResult.BSSID));
                noValidSsid.append(" / ");
            } else {
                if (scanResult.BSSID.equals(str)) {
                    scanResultsHaveCurrentBssid = true;
                }
                String scanId = toScanId(scanResult);
                if (bssidBlacklist.contains(scanResult.BSSID)) {
                    blacklistedBssid.append(scanId);
                    blacklistedBssid.append(" / ");
                }
                if (isSignalTooWeak(scanResult)) {
                    lowRssi.append(scanId);
                    lowRssi.append("(");
                    lowRssi.append(scanResult.is24GHz() ? "2.4GHz" : "5GHz");
                    lowRssi.append(")");
                    lowRssi.append(scanResult.level);
                    lowRssi.append(" / ");
                } else {
                    validScanDetails.add(scanDetail);
                }
            }
        }
        HashSet<String> hashSet = bssidBlacklist;
        if (!isConnected || scanResultsHaveCurrentBssid) {
            if (noValidSsid.length() != 0) {
                localLog(str2, "53", "Networks filtered out due to invalid SSID: %s", noValidSsid);
            }
            if (blacklistedBssid.length() != 0) {
                localLog(str2, "54", "Networks filtered out due to blacklist: %s", blacklistedBssid);
            }
            if (lowRssi.length() != 0) {
                localLog(str2, "55", "Networks filtered out due to low signal strength: %s", lowRssi);
            }
            return validScanDetails;
        }
        localLog(str2, "52", "Current connected BSSID %s is not in the scan results. Skip network selection.", str);
        validScanDetails.clear();
        return validScanDetails;
    }

    public List<ScanDetail> getFilteredScanDetailsForOpenUnsavedNetworks() {
        List<ScanDetail> openUnsavedNetworks = new ArrayList<>();
        for (ScanDetail scanDetail : this.mFilteredNetworks) {
            if (ScanResultUtil.isScanResultForOpenNetwork(scanDetail.getScanResult()) && this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail) == null) {
                openUnsavedNetworks.add(scanDetail);
            }
        }
        return openUnsavedNetworks;
    }

    public List<ScanDetail> getFilteredScanDetailsForCarrierUnsavedNetworks(CarrierNetworkConfig carrierConfig) {
        List<ScanDetail> carrierUnsavedNetworks = new ArrayList<>();
        for (ScanDetail scanDetail : this.mFilteredNetworks) {
            ScanResult scanResult = scanDetail.getScanResult();
            if (ScanResultUtil.isScanResultForEapNetwork(scanResult) && carrierConfig.isCarrierNetwork(scanResult.SSID) && this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail) == null) {
                carrierUnsavedNetworks.add(scanDetail);
            }
        }
        return carrierUnsavedNetworks;
    }

    public List<Pair<ScanDetail, WifiConfiguration>> getConnectableScanDetails() {
        return this.mConnectableNetworks;
    }

    public boolean setUserConnectChoice(int netId) {
        localLog("userSelectNetwork: network ID=" + netId);
        WifiConfiguration selected = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (selected == null || selected.SSID == null) {
            localLog("userSelectNetwork: Invalid configuration with nid=" + netId);
            return false;
        }
        if (!selected.getNetworkSelectionStatus().isNetworkEnabled()) {
            this.mWifiConfigManager.updateNetworkSelectionStatus(netId, 0);
        }
        boolean change = false;
        String key = selected.configKey();
        long currentTime = this.mClock.getWallClockMillis();
        for (WifiConfiguration network : this.mWifiConfigManager.getSavedNetworks()) {
            WifiConfiguration.NetworkSelectionStatus status = network.getNetworkSelectionStatus();
            if (network.networkId == selected.networkId) {
                if (status.getConnectChoice() != null) {
                    localLog("Remove user selection preference of " + status.getConnectChoice() + " Set Time: " + status.getConnectChoiceTimestamp() + " from " + network.SSID + " : " + network.networkId);
                    this.mWifiConfigManager.clearNetworkConnectChoice(network.networkId);
                    change = true;
                }
            } else if (status.getSeenInLastQualifiedNetworkSelection()) {
                localLog("Add key: " + key + " Set Time: " + currentTime + " to " + toNetworkString(network));
                this.mWifiConfigManager.setNetworkConnectChoice(network.networkId, key, currentTime);
                change = true;
            }
        }
        return change;
    }

    private WifiConfiguration overrideCandidateWithUserConnectChoice(WifiConfiguration candidate) {
        WifiConfiguration tempConfig = candidate;
        WifiConfiguration originalCandidate = candidate;
        ScanResult scanResultCandidate = candidate.getNetworkSelectionStatus().getCandidate();
        while (true) {
            if (tempConfig.getNetworkSelectionStatus().getConnectChoice() == null) {
                break;
            }
            long choiceSetToGet = this.mClock.getWallClockMillis() - tempConfig.getNetworkSelectionStatus().getConnectChoiceTimestamp();
            if (choiceSetToGet < CONNECT_CHOICE_INVALID || choiceSetToGet > CONNECT_CHOICE_TIMEOUT) {
                break;
            }
            String key = tempConfig.getNetworkSelectionStatus().getConnectChoice();
            tempConfig = this.mWifiConfigManager.getConfiguredNetwork(key);
            if (tempConfig == null) {
                localLog("Connect choice: " + key + " has no corresponding saved config.");
                break;
            }
            WifiConfiguration.NetworkSelectionStatus tempStatus = tempConfig.getNetworkSelectionStatus();
            if (tempStatus.getCandidate() != null && tempStatus.isNetworkEnabled()) {
                scanResultCandidate = tempStatus.getCandidate();
                candidate = tempConfig;
            }
        }
        if (candidate != originalCandidate) {
            localLog("After user selection adjustment, the final candidate is:" + toNetworkString(candidate) + " : " + scanResultCandidate.BSSID);
        }
        return candidate;
    }

    /* JADX WARNING: type inference failed for: r5v2 */
    /* JADX WARNING: type inference failed for: r5v3, types: [boolean, int] */
    /* JADX WARNING: type inference failed for: r5v6 */
    /* JADX WARNING: type inference failed for: r5v8 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    public WifiConfiguration selectNetwork(List<ScanDetail> scanDetails, HashSet<String> bssidBlacklist, WifiInfo wifiInfo, boolean connected, boolean disconnected, boolean untrustedNetworkAllowed) {
        ? r5;
        boolean z;
        this.mFilteredNetworks.clear();
        this.mConnectableNetworks.clear();
        String keys = this.mCurrentScanKeys;
        if (scanDetails.size() == 0) {
            localLog("Empty connectivity scan result");
            return null;
        }
        WifiConfiguration currentNetwork = this.mWifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
        String currentBssid = wifiInfo.getBSSID();
        if (!isNetworkSelectionNeeded(scanDetails, wifiInfo, connected, disconnected, keys)) {
            localLog(keys, "56", "isNetworkSelectionNeeded return false!");
            return null;
        }
        boolean z2 = false;
        for (NetworkEvaluator registeredEvaluator : this.mEvaluators) {
            if (registeredEvaluator != null) {
                registeredEvaluator.update(scanDetails);
            } else {
                List<ScanDetail> list = scanDetails;
            }
        }
        this.mFilteredNetworks = filterScanResults(scanDetails, bssidBlacklist, connected, currentBssid, keys);
        if (this.mFilteredNetworks.size() == 0) {
            localLog(keys, "57", "after filteredScanDetails size() == 0");
            return null;
        }
        NetworkEvaluator[] networkEvaluatorArr = this.mEvaluators;
        int length = networkEvaluatorArr.length;
        WifiConfiguration savedOpenNetwork = null;
        WifiConfiguration selectedNetwork = null;
        int i = 0;
        while (true) {
            if (i >= length) {
                r5 = z2;
                break;
            }
            NetworkEvaluator registeredEvaluator2 = networkEvaluatorArr[i];
            if (registeredEvaluator2 != null) {
                if (this.mConnectivityHelper != null) {
                    this.mConnectivityHelper.mCurrentScanKeys = keys;
                }
                if ((registeredEvaluator2 instanceof PasspointNetworkEvaluator) && !PasspointUtil.ishs2Enabled(this.mContext)) {
                    Log.w(TAG, "Passpoint is disabled.");
                } else if (!(registeredEvaluator2 instanceof PasspointNetworkEvaluator) || PasspointUtil.ishs20EanbledBySim(this.mContext)) {
                    ? r52 = z2;
                    selectedNetwork = registeredEvaluator2.evaluateNetworks(new ArrayList(this.mFilteredNetworks), currentNetwork, currentBssid, connected, untrustedNetworkAllowed, this.mConnectableNetworks);
                    if (selectedNetwork != null && WifiConfigurationUtil.isConfigForOpenNetwork(selectedNetwork) && PasspointUtil.ishs2Enabled(this.mContext) && (registeredEvaluator2 instanceof SavedNetworkEvaluator)) {
                        savedOpenNetwork = selectedNetwork;
                        z = r52;
                        i++;
                        List<ScanDetail> list2 = scanDetails;
                        z2 = z;
                    } else if (selectedNetwork != null) {
                        Object[] objArr = new Object[2];
                        objArr[r52] = registeredEvaluator2.getClass().getName();
                        objArr[1] = selectedNetwork.SSID;
                        localLog(keys, "58", "after %s evaluateNetworks, get wifi %s", objArr);
                        r5 = r52;
                        break;
                    } else {
                        z = r52;
                        i++;
                        List<ScanDetail> list22 = scanDetails;
                        z2 = z;
                    }
                } else {
                    Log.w(TAG, "Passpoint should be disabled as sim absent or not match.");
                }
            }
            z = z2;
            i++;
            List<ScanDetail> list222 = scanDetails;
            z2 = z;
        }
        if ((selectedNetwork == null || !selectedNetwork.isPasspoint()) && savedOpenNetwork != null) {
            selectedNetwork = savedOpenNetwork;
        }
        boolean cloudSecurityCheckOn = Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_cloud_security_check", r5) == 1 ? true : r5;
        if (!(selectedNetwork == null || selectedNetwork.cloudSecurityCheck == 0 || !cloudSecurityCheckOn)) {
            Log.w("WifiScanLog", "SSID = " + selectedNetwork.SSID + ",cloudSecurityCheck = " + selectedNetwork.cloudSecurityCheck + ", don`t attemptAutoJoin.");
            selectedNetwork = null;
        }
        if (selectedNetwork != null) {
            selectedNetwork = overrideCandidateWithUserConnectChoice(selectedNetwork);
            Object[] objArr2 = new Object[1];
            objArr2[r5] = selectedNetwork.SSID;
            localLog(keys, "59", "after overrideCandidateWithUserConnectChoice, get wifi %s", objArr2);
            this.mLastNetworkSelectionTimeStamp = this.mClock.getElapsedSinceBootMillis();
            if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(selectedNetwork, r5)) {
                Log.w(TAG, "selectNetwork: MDM deny connect to restricted network!");
                selectedNetwork = null;
            }
        }
        return selectedNetwork;
    }

    public boolean registerNetworkEvaluator(NetworkEvaluator evaluator, int priority) {
        if (priority < 0 || priority >= 6) {
            localLog("Invalid network evaluator priority: " + priority);
            return false;
        } else if (this.mEvaluators[priority] != null) {
            localLog("Priority " + priority + " is already registered by " + this.mEvaluators[priority].getName());
            return false;
        } else {
            this.mEvaluators[priority] = evaluator;
            return true;
        }
    }

    WifiNetworkSelector(Context context, ScoringParams scoringParams, WifiConfigManager configManager, Clock clock, LocalLog localLog) {
        this.mWifiConfigManager = configManager;
        this.mClock = clock;
        this.mScoringParams = scoringParams;
        this.mLocalLog = localLog;
        this.mContext = context;
        this.mEnableAutoJoinWhenAssociated = context.getResources().getBoolean(17957081);
        this.mStayOnNetworkMinimumTxRate = context.getResources().getInteger(17694906);
        this.mStayOnNetworkMinimumRxRate = context.getResources().getInteger(17694905);
    }

    /* access modifiers changed from: package-private */
    public void localLog(String scanKey, String eventKey, String log) {
        localLog(scanKey, eventKey, log, null);
    }

    /* access modifiers changed from: package-private */
    public void localLog(String scanKey, String eventKey, String log, Object... params) {
        WifiConnectivityHelper.localLog(this.mLocalLog, scanKey, eventKey, log, params);
    }
}
