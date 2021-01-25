package com.android.server.wifi;

import android.content.Context;
import android.net.NetworkKey;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.Preconditions;
import com.android.server.wifi.WifiCandidates;
import com.android.server.wifi.WifiNetworkSelector;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.hwUtil.PasspointUtil;
import com.android.server.wifi.hwUtil.ScanResultUtilEx;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.util.ScanResultUtil;
import huawei.cust.HwCustUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WifiNetworkSelector {
    private static final long CONNECT_CHOICE_INVALID = 0;
    private static final long CONNECT_CHOICE_TIMEOUT = 50000;
    private static final int ID_PREFIX = 42;
    private static final int ID_SUFFIX_MOD = 1000000;
    private static final long INVALID_TIME_STAMP = Long.MIN_VALUE;
    @VisibleForTesting
    public static final int LAST_USER_SELECTION_DECAY_TO_ZERO_MS = 28800000;
    @VisibleForTesting
    public static final int LAST_USER_SELECTION_SUFFICIENT_MS = 30000;
    public static final int LEGACY_CANDIDATE_SCORER_EXP_ID = 0;
    @VisibleForTesting
    public static final int MINIMUM_NETWORK_SELECTION_INTERVAL_MS = 10000;
    private static final int MIN_SCORER_EXP_ID = 42000000;
    public static final String PRESET_CANDIDATE_SCORER_NAME = "CompatibilityScorer";
    private static final String TAG = "WifiNetworkSelector";
    @VisibleForTesting
    public static final int WIFI_POOR_SCORE = 40;
    private final Map<String, WifiCandidates.CandidateScorer> mCandidateScorers = new ArrayMap();
    private final Clock mClock;
    private final List<Pair<ScanDetail, WifiConfiguration>> mConnectableNetworks = new ArrayList();
    public WifiConnectivityHelper mConnectivityHelper;
    private Context mContext;
    public String mCurrentScanKeys = "";
    private HwCustWifiAutoJoinController mCust = ((HwCustWifiAutoJoinController) HwCustUtils.createObj(HwCustWifiAutoJoinController.class, new Object[0]));
    private final boolean mEnableAutoJoinWhenAssociated;
    private final List<NetworkEvaluator> mEvaluators = new ArrayList(3);
    private List<ScanDetail> mFilteredNetworks = new ArrayList();
    private boolean mIsEnhancedOpenSupported;
    private boolean mIsEnhancedOpenSupportedInitialized = false;
    private long mLastNetworkSelectionTimeStamp = INVALID_TIME_STAMP;
    private final LocalLog mLocalLog;
    private final ScoringParams mScoringParams;
    private final int mStayOnNetworkMinimumRxRate;
    private final int mStayOnNetworkMinimumTxRate;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiMetrics mWifiMetrics;
    private final WifiNative mWifiNative;
    private final WifiScoreCard mWifiScoreCard;

    public interface NetworkEvaluator {
        public static final int EVALUATOR_ID_CARRIER = 3;
        public static final int EVALUATOR_ID_PASSPOINT = 2;
        public static final int EVALUATOR_ID_R1 = 5;
        public static final int EVALUATOR_ID_SAVED = 0;
        public static final int EVALUATOR_ID_SCORED = 4;
        public static final int EVALUATOR_ID_SUGGESTION = 1;

        @Retention(RetentionPolicy.SOURCE)
        public @interface EvaluatorId {
        }

        public interface OnConnectableListener {
            void onConnectable(ScanDetail scanDetail, WifiConfiguration wifiConfiguration, int i);
        }

        WifiConfiguration evaluateNetworks(List<ScanDetail> list, WifiConfiguration wifiConfiguration, String str, boolean z, boolean z2, OnConnectableListener onConnectableListener);

        int getId();

        String getName();

        void update(List<ScanDetail> list);
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
    }

    private boolean isCurrentNetworkSufficient(WifiInfo wifiInfo, List<ScanDetail> scanDetails) {
        if (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            localLog("No current connected network.");
            return false;
        }
        localLog("Current connected network: " + StringUtilEx.safeDisplaySsid(wifiInfo.getSSID()) + " , ID: " + wifiInfo.getNetworkId());
        int currentRssi = wifiInfo.getRssi();
        boolean hasQualifiedRssi = currentRssi > this.mScoringParams.getSufficientRssi(wifiInfo.getFrequency());
        boolean hasActiveStream = wifiInfo.txSuccessRate > ((double) this.mStayOnNetworkMinimumTxRate) || wifiInfo.rxSuccessRate > ((double) this.mStayOnNetworkMinimumRxRate);
        if (!hasQualifiedRssi || !hasActiveStream) {
            WifiConfiguration network = this.mWifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
            if (network == null) {
                localLog("Current network was removed.");
                return false;
            } else if (this.mWifiConfigManager.getLastSelectedNetwork() == network.networkId && this.mClock.getElapsedSinceBootMillis() - this.mWifiConfigManager.getLastSelectedTimeStamp() <= 30000) {
                localLog("Current network is recently user-selected.");
                return true;
            } else if (network.osu) {
                return true;
            } else {
                if (wifiInfo.isEphemeral()) {
                    localLog("Current network is an ephemeral one.");
                    return false;
                } else if (wifiInfo.is24GHz() && is5GHzNetworkAvailable(scanDetails)) {
                    localLog("Current network is 2.4GHz. 5GHz networks available.");
                    return false;
                } else if (!hasQualifiedRssi) {
                    localLog("Current network RSSI[" + currentRssi + "]-acceptable but not qualified.");
                    return false;
                } else if (WifiConfigurationUtil.isConfigForOpenNetwork(network)) {
                    localLog("Current network is a open one.");
                    return false;
                } else if (network.numNoInternetAccessReports <= 0 || network.noInternetAccessExpected) {
                    return true;
                } else {
                    localLog("Current network has [" + network.numNoInternetAccessReports + "] no-internet access reports.");
                    return false;
                }
            }
        } else {
            localLog("Stay on current network because of good RSSI and ongoing traffic");
            return true;
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
            Context context = this.mContext;
            if (context != null && "true".equals(Settings.Global.getString(context.getContentResolver(), "hw_wifipro_enable"))) {
                return false;
            }
            if (this.mLastNetworkSelectionTimeStamp != INVALID_TIME_STAMP) {
                long gap = this.mClock.getElapsedSinceBootMillis() - this.mLastNetworkSelectionTimeStamp;
                if (gap < RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS) {
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
            localLog(keys, "50", "ClientModeImpl is in neither CONNECTED nor DISCONNECTED state. Skip network selection.");
            return false;
        }
    }

    public static String toScanId(ScanResult scanResult) {
        if (scanResult == null) {
            return "NULL";
        }
        return String.format("%s:%s", StringUtilEx.safeDisplaySsid(scanResult.SSID), ScanResultUtilEx.getConfusedBssid(scanResult.BSSID));
    }

    public static String toNetworkString(WifiConfiguration network) {
        if (network == null) {
            return null;
        }
        return StringUtilEx.safeDisplaySsid(network.SSID) + ":" + network.networkId;
    }

    public boolean isSignalTooWeak(ScanResult scanResult) {
        return scanResult.level < this.mScoringParams.getEntryRssi(scanResult.frequency);
    }

    private List<ScanDetail> filterScanResults(List<ScanDetail> scanDetails, HashSet<String> bssidBlacklist, boolean isConnected, String currentBssid, String keys) {
        StringBuffer blacklistedBssid;
        localLog(keys, "51", "filterScanResults begin");
        ArrayList<NetworkKey> unscoredNetworks = new ArrayList<>();
        List<ScanDetail> validScanDetails = new ArrayList<>();
        StringBuffer noValidSsid = new StringBuffer();
        StringBuffer blacklistedBssid2 = new StringBuffer();
        StringBuffer lowRssi = new StringBuffer();
        StringBuffer validSavedSsid = new StringBuffer();
        boolean scanResultsHaveCurrentBssid = false;
        for (ScanDetail scanDetail : scanDetails) {
            ScanResult scanResult = scanDetail.getScanResult();
            if (TextUtils.isEmpty(scanResult.SSID)) {
                noValidSsid.append(ScanResultUtilEx.getConfusedBssid(scanResult.BSSID));
                noValidSsid.append(" / ");
            } else {
                if (scanResult.BSSID.equals(currentBssid)) {
                    scanResultsHaveCurrentBssid = true;
                }
                String scanId = toScanId(scanResult);
                if (bssidBlacklist.contains(scanResult.BSSID)) {
                    blacklistedBssid2.append(scanId);
                    blacklistedBssid2.append(" / ");
                    blacklistedBssid = blacklistedBssid2;
                    localLog(keys, "54", "Blacklist has network: %s", StringUtilEx.safeDisplayBssid(scanResult.BSSID));
                } else {
                    blacklistedBssid = blacklistedBssid2;
                }
                String str = "2.4GHz";
                if (isSignalTooWeak(scanResult)) {
                    lowRssi.append(scanId);
                    lowRssi.append("(");
                    if (!scanResult.is24GHz()) {
                        str = "5GHz";
                    }
                    lowRssi.append(str);
                    lowRssi.append(")");
                    lowRssi.append(scanResult.level);
                    lowRssi.append(" / ");
                } else {
                    validScanDetails.add(scanDetail);
                    if (this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail) != null) {
                        validSavedSsid.append(scanId);
                        validSavedSsid.append("(");
                        if (!scanResult.is24GHz()) {
                            str = "5GHz";
                        }
                        validSavedSsid.append(str);
                        validSavedSsid.append(")");
                        validSavedSsid.append(scanResult.level);
                        validSavedSsid.append(" / ");
                    }
                }
                scanResultsHaveCurrentBssid = scanResultsHaveCurrentBssid;
                unscoredNetworks = unscoredNetworks;
                blacklistedBssid2 = blacklistedBssid;
            }
        }
        if (!isConnected || scanResultsHaveCurrentBssid) {
            if (noValidSsid.length() != 0) {
                localLog(keys, "53", "Networks filtered out due to invalid SSID: %s", noValidSsid);
            }
            if (lowRssi.length() != 0) {
                localLog(keys, "55", "Networks filtered out due to low signal strength: %s", lowRssi);
            }
            if (validSavedSsid.length() != 0) {
                localLog("filterScanResults end saved valid wifi SSID : " + ((Object) validSavedSsid));
            }
            return validScanDetails;
        }
        localLog(keys, "52", "Current connected BSSID %s is not in the scan results. Skip network selection.", StringUtilEx.safeDisplayBssid(currentBssid));
        validScanDetails.clear();
        return validScanDetails;
    }

    private boolean isEnhancedOpenSupported() {
        if (this.mIsEnhancedOpenSupportedInitialized) {
            return this.mIsEnhancedOpenSupported;
        }
        boolean z = true;
        this.mIsEnhancedOpenSupportedInitialized = true;
        WifiNative wifiNative = this.mWifiNative;
        if ((wifiNative.getSupportedFeatureSet(wifiNative.getClientInterfaceName()) & 536870912) == CONNECT_CHOICE_INVALID) {
            z = false;
        }
        this.mIsEnhancedOpenSupported = z;
        return this.mIsEnhancedOpenSupported;
    }

    public List<ScanDetail> getFilteredScanDetailsForOpenUnsavedNetworks() {
        List<ScanDetail> openUnsavedNetworks = new ArrayList<>();
        boolean enhancedOpenSupported = isEnhancedOpenSupported();
        for (ScanDetail scanDetail : this.mFilteredNetworks) {
            ScanResult scanResult = scanDetail.getScanResult();
            if (ScanResultUtil.isScanResultForOpenNetwork(scanResult) && ((!ScanResultUtil.isScanResultForOweNetwork(scanResult) || enhancedOpenSupported) && this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail) == null)) {
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
        return setLegacyUserConnectChoice(selected);
    }

    private boolean setLegacyUserConnectChoice(WifiConfiguration selected) {
        String str;
        boolean change = false;
        String key = selected.configKey();
        long currentTime = this.mClock.getWallClockMillis();
        for (WifiConfiguration network : this.mWifiConfigManager.getConfiguredNetworks()) {
            WifiConfiguration.NetworkSelectionStatus status = network.getNetworkSelectionStatus();
            if (network.networkId == selected.networkId) {
                if (status.getConnectChoice() != null) {
                    localLog("Remove user selection preference of " + status.getConnectChoice() + " Set Time: " + status.getConnectChoiceTimestamp() + " from " + StringUtilEx.safeDisplaySsid(network.SSID) + " : " + network.networkId);
                    this.mWifiConfigManager.clearNetworkConnectChoice(network.networkId);
                    change = true;
                }
            } else if (status.getSeenInLastQualifiedNetworkSelection()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Add key: ");
                sb.append(StringUtilEx.safeDisplaySsid(selected.getPrintableSsid()));
                sb.append(" authType: ");
                if (selected.SSID == null) {
                    str = selected.getSsidAndSecurityTypeString();
                } else {
                    str = selected.getSsidAndSecurityTypeString().substring(selected.SSID.length());
                }
                sb.append(str);
                sb.append(" Set Time: ");
                sb.append(currentTime);
                sb.append(" to ");
                sb.append(toNetworkString(network));
                localLog(sb.toString());
                this.mWifiConfigManager.setNetworkConnectChoice(network.networkId, key, currentTime);
                change = true;
            }
        }
        return change;
    }

    private void updateConfiguredNetworks() {
        List<WifiConfiguration> configuredNetworks = this.mWifiConfigManager.getConfiguredNetworks();
        if (configuredNetworks.size() == 0) {
            localLog("No configured networks.");
            return;
        }
        StringBuffer sbuf = new StringBuffer();
        for (WifiConfiguration network : configuredNetworks) {
            this.mWifiConfigManager.tryEnableNetwork(network.networkId);
            this.mWifiConfigManager.clearNetworkCandidateScanResult(network.networkId);
            WifiConfiguration.NetworkSelectionStatus status = network.getNetworkSelectionStatus();
            if (!status.isNetworkEnabled()) {
                sbuf.append("  ");
                sbuf.append(toNetworkString(network));
                sbuf.append(" ");
                for (int index = 1; index < 18; index++) {
                    int count = status.getDisableReasonCounter(index);
                    if (count > 0) {
                        sbuf.append("reason=");
                        sbuf.append(WifiConfiguration.NetworkSelectionStatus.getNetworkDisableReasonString(index));
                        sbuf.append(", count=");
                        sbuf.append(count);
                        sbuf.append("; ");
                    }
                }
                sbuf.append("\n");
            }
        }
        if (sbuf.length() > 0) {
            localLog("Disabled configured networks:");
            localLog(sbuf.toString());
        }
    }

    private WifiConfiguration overrideCandidateWithUserConnectChoice(WifiConfiguration candidate) {
        WifiConfiguration tempConfig = (WifiConfiguration) Preconditions.checkNotNull(candidate);
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
        if (candidate != candidate) {
            localLog("After user selection adjustment, the final candidate is:" + toNetworkString(candidate) + " : " + StringUtilEx.safeDisplayBssid(scanResultCandidate.BSSID));
            this.mWifiMetrics.setNominatorForNetwork(candidate.networkId, 8);
        }
        return candidate;
    }

    /* JADX INFO: Multiple debug info for r0v18 int: [D('legacySelectedNetworkId' int), D('selectedNetworkId' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0086  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x008e  */
    public WifiConfiguration selectNetwork(List<ScanDetail> scanDetails, HashSet<String> bssidBlacklist, WifiInfo wifiInfo, boolean connected, boolean disconnected, boolean untrustedNetworkAllowed) {
        boolean z;
        WifiConfiguration selectedNetwork;
        int selectedNetworkId;
        int activeExperimentId;
        int networkId;
        ScanDetail scanDetail;
        NetworkEvaluator registeredEvaluator;
        WifiNetworkSelector wifiNetworkSelector;
        WifiNetworkSelector wifiNetworkSelector2 = this;
        wifiNetworkSelector2.mFilteredNetworks.clear();
        wifiNetworkSelector2.mConnectableNetworks.clear();
        String keys = wifiNetworkSelector2.mCurrentScanKeys;
        if (scanDetails.size() == 0) {
            wifiNetworkSelector2.localLog("Empty connectivity scan result");
            return null;
        }
        WifiConfiguration currentNetwork = wifiNetworkSelector2.mWifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
        String currentBssid = wifiInfo.getBSSID();
        if (!isNetworkSelectionNeeded(scanDetails, wifiInfo, connected, disconnected, keys)) {
            wifiNetworkSelector2.localLog(keys, "56", "isNetworkSelectionNeeded return false!");
            return null;
        }
        updateConfiguredNetworks();
        for (NetworkEvaluator registeredEvaluator2 : wifiNetworkSelector2.mEvaluators) {
            registeredEvaluator2.update(scanDetails);
        }
        boolean z2 = true;
        boolean z3 = false;
        if (connected) {
            if (wifiInfo.score >= 40) {
                z = true;
                wifiNetworkSelector2.mFilteredNetworks = filterScanResults(scanDetails, bssidBlacklist, z, currentBssid, keys);
                if (wifiNetworkSelector2.mFilteredNetworks.size() != 0) {
                    wifiNetworkSelector2.localLog(keys, "57", "after filteredScanDetails size() == 0");
                    return null;
                }
                int lastUserSelectedNetworkId = wifiNetworkSelector2.mWifiConfigManager.getLastSelectedNetwork();
                double lastSelectionWeight = calculateLastSelectionWeight();
                ArraySet<Integer> mNetworkIds = new ArraySet<>();
                WifiConfiguration selectedNetwork2 = null;
                WifiCandidates wifiCandidates = new WifiCandidates(wifiNetworkSelector2.mWifiScoreCard);
                if (currentNetwork != null) {
                    wifiCandidates.setCurrent(currentNetwork.networkId, currentBssid);
                }
                WifiConfiguration savedOpenNetwork = null;
                for (NetworkEvaluator registeredEvaluator3 : wifiNetworkSelector2.mEvaluators) {
                    wifiNetworkSelector2.localLog("About to run " + registeredEvaluator3.getName() + " :");
                    WifiConnectivityHelper wifiConnectivityHelper = wifiNetworkSelector2.mConnectivityHelper;
                    if (wifiConnectivityHelper != null) {
                        wifiConnectivityHelper.mCurrentScanKeys = keys;
                    }
                    if ((registeredEvaluator3 instanceof PasspointNetworkEvaluator) && !PasspointUtil.ishs2Enabled(wifiNetworkSelector2.mContext)) {
                        Log.w(TAG, "Passpoint is disabled.");
                    } else if (!(registeredEvaluator3 instanceof PasspointNetworkEvaluator) || PasspointUtil.ishs20EanbledBySim(wifiNetworkSelector2.mContext)) {
                        WifiConfiguration choice = registeredEvaluator3.evaluateNetworks(new ArrayList(wifiNetworkSelector2.mFilteredNetworks), currentNetwork, currentBssid, connected, untrustedNetworkAllowed, new NetworkEvaluator.OnConnectableListener(mNetworkIds, lastUserSelectedNetworkId, wifiCandidates, registeredEvaluator3, lastSelectionWeight) {
                            /* class com.android.server.wifi.$$Lambda$WifiNetworkSelector$Z7htivbXF5AzGeTh0ZNbtUXC_0Q */
                            private final /* synthetic */ ArraySet f$1;
                            private final /* synthetic */ int f$2;
                            private final /* synthetic */ WifiCandidates f$3;
                            private final /* synthetic */ WifiNetworkSelector.NetworkEvaluator f$4;
                            private final /* synthetic */ double f$5;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                                this.f$4 = r5;
                                this.f$5 = r6;
                            }

                            @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator.OnConnectableListener
                            public final void onConnectable(ScanDetail scanDetail, WifiConfiguration wifiConfiguration, int i) {
                                WifiNetworkSelector.this.lambda$selectNetwork$0$WifiNetworkSelector(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, scanDetail, wifiConfiguration, i);
                            }
                        });
                        if (choice != null && !mNetworkIds.contains(Integer.valueOf(choice.networkId))) {
                            Log.wtf(TAG, registeredEvaluator3.getName() + " failed to report choice with noConnectibleListener");
                        }
                        if (choice == null || !WifiConfigurationUtil.isConfigForOpenNetwork(choice)) {
                            wifiNetworkSelector = this;
                            mNetworkIds = mNetworkIds;
                            registeredEvaluator = registeredEvaluator3;
                        } else {
                            wifiNetworkSelector = this;
                            mNetworkIds = mNetworkIds;
                            if (PasspointUtil.ishs2Enabled(wifiNetworkSelector.mContext)) {
                                registeredEvaluator = registeredEvaluator3;
                                if (registeredEvaluator instanceof SavedNetworkEvaluator) {
                                    savedOpenNetwork = choice;
                                    z3 = z3;
                                    z2 = z2;
                                    currentBssid = currentBssid;
                                    currentNetwork = currentNetwork;
                                    wifiNetworkSelector2 = wifiNetworkSelector;
                                    keys = keys;
                                    wifiCandidates = wifiCandidates;
                                }
                            } else {
                                registeredEvaluator = registeredEvaluator3;
                            }
                        }
                        if (selectedNetwork2 == null && choice != null) {
                            selectedNetwork2 = choice;
                            wifiNetworkSelector.localLog(registeredEvaluator.getName() + " selects " + toNetworkString(selectedNetwork2));
                        }
                        HwCustWifiAutoJoinController hwCustWifiAutoJoinController = wifiNetworkSelector.mCust;
                        if ((hwCustWifiAutoJoinController != null && hwCustWifiAutoJoinController.isWifiAutoJoinPriority(wifiNetworkSelector.mContext)) || HuaweiTelephonyConfigs.isChinaMobile()) {
                            if (selectedNetwork2 != null && (registeredEvaluator instanceof SavedNetworkEvaluator)) {
                                return selectedNetwork2;
                            }
                            Log.w(TAG, "not expected evaluator or selectedNetwork is null");
                        }
                        z3 = z3;
                        z2 = z2;
                        currentBssid = currentBssid;
                        currentNetwork = currentNetwork;
                        wifiNetworkSelector2 = wifiNetworkSelector;
                        keys = keys;
                        wifiCandidates = wifiCandidates;
                    } else {
                        Log.w(TAG, "Passpoint should be disabled as sim absent or not match.");
                    }
                }
                WifiCandidates wifiCandidates2 = wifiCandidates;
                String currentBssid2 = currentBssid;
                WifiConfiguration currentNetwork2 = currentNetwork;
                if ((selectedNetwork2 == null || !selectedNetwork2.isPasspoint()) && savedOpenNetwork != null) {
                    selectedNetwork = savedOpenNetwork;
                } else {
                    selectedNetwork = selectedNetwork2;
                }
                if (wifiNetworkSelector2.mConnectableNetworks.size() != wifiCandidates2.size()) {
                    wifiNetworkSelector2.localLog("Connectable: " + wifiNetworkSelector2.mConnectableNetworks.size() + " Candidates: " + wifiCandidates2.size());
                }
                Collection<Collection<WifiCandidates.Candidate>> groupedCandidates = wifiCandidates2.getGroupedCandidates();
                for (Collection<WifiCandidates.Candidate> group : groupedCandidates) {
                    WifiCandidates.Candidate best = null;
                    for (WifiCandidates.Candidate candidate : group) {
                        if (best == null || candidate.getEvaluatorId() < best.getEvaluatorId() || (candidate.getEvaluatorId() == best.getEvaluatorId() && candidate.getEvaluatorScore() > best.getEvaluatorScore())) {
                            best = candidate;
                        }
                    }
                    if (!(best == null || (scanDetail = best.getScanDetail()) == null)) {
                        wifiNetworkSelector2.mWifiConfigManager.setNetworkCandidateScanResult(best.getNetworkConfigId(), scanDetail.getScanResult(), best.getEvaluatorScore());
                    }
                }
                ArrayMap<Integer, Integer> experimentNetworkSelections = new ArrayMap<>();
                if (selectedNetwork == null) {
                    selectedNetworkId = -1;
                } else {
                    selectedNetworkId = selectedNetwork.networkId;
                }
                boolean legacyOverrideWanted = true;
                WifiCandidates.CandidateScorer activeScorer = getActiveCandidateScorer();
                Iterator<WifiCandidates.CandidateScorer> it = wifiNetworkSelector2.mCandidateScorers.values().iterator();
                int selectedNetworkId2 = selectedNetworkId;
                while (it.hasNext()) {
                    WifiCandidates.CandidateScorer candidateScorer = it.next();
                    try {
                        WifiCandidates.ScoredCandidate choice2 = wifiCandidates2.choose(candidateScorer);
                        wifiCandidates2 = wifiCandidates2;
                        if (choice2.candidateKey == null) {
                            networkId = -1;
                        } else {
                            networkId = choice2.candidateKey.networkId;
                        }
                        String chooses = " would choose ";
                        if (candidateScorer == activeScorer) {
                            chooses = " chooses ";
                            legacyOverrideWanted = candidateScorer.userConnectChoiceOverrideWanted();
                            selectedNetworkId2 = networkId;
                        }
                        String id = candidateScorer.getIdentifier();
                        int expid = experimentIdFromIdentifier(id);
                        wifiNetworkSelector2.localLog(id + chooses + networkId + " score " + choice2.value + "+/-" + choice2.err + " expid " + expid);
                        experimentNetworkSelections.put(Integer.valueOf(expid), Integer.valueOf(networkId));
                        it = it;
                        selectedNetworkId2 = selectedNetworkId2;
                        currentBssid2 = currentBssid2;
                        currentNetwork2 = currentNetwork2;
                        mNetworkIds = mNetworkIds;
                        legacyOverrideWanted = legacyOverrideWanted;
                    } catch (RuntimeException e) {
                        wifiCandidates2 = wifiCandidates2;
                        Log.wtf(TAG, "Exception running a CandidateScorer", e);
                        it = it;
                        currentBssid2 = currentBssid2;
                        currentNetwork2 = currentNetwork2;
                        mNetworkIds = mNetworkIds;
                    }
                }
                if (activeScorer == null) {
                    activeExperimentId = 0;
                } else {
                    activeExperimentId = experimentIdFromIdentifier(activeScorer.getIdentifier());
                }
                experimentNetworkSelections.put(0, Integer.valueOf(selectedNetworkId));
                Iterator<Map.Entry<Integer, Integer>> it2 = experimentNetworkSelections.entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry<Integer, Integer> entry = it2.next();
                    int experimentId = entry.getKey().intValue();
                    if (experimentId != activeExperimentId) {
                        wifiNetworkSelector2.mWifiMetrics.logNetworkSelectionDecision(experimentId, activeExperimentId, selectedNetworkId2 == entry.getValue().intValue(), groupedCandidates.size());
                        it2 = it2;
                    }
                }
                boolean cloudSecurityCheckOn = true;
                if (Settings.Global.getInt(wifiNetworkSelector2.mContext.getContentResolver(), "wifi_cloud_security_check", 0) != 1) {
                    cloudSecurityCheckOn = false;
                }
                if (!(selectedNetwork == null || selectedNetwork.cloudSecurityCheck == 0 || !cloudSecurityCheckOn)) {
                    Log.w("WifiScanLog", "SSID = " + StringUtilEx.safeDisplaySsid(selectedNetwork.SSID) + ",cloudSecurityCheck = " + selectedNetwork.cloudSecurityCheck + ", don`t attemptAutoJoin.");
                }
                WifiConfiguration selectedNetwork3 = wifiNetworkSelector2.mWifiConfigManager.getConfiguredNetwork(selectedNetworkId2);
                if (selectedNetwork3 != null && legacyOverrideWanted) {
                    selectedNetwork3 = wifiNetworkSelector2.overrideCandidateWithUserConnectChoice(selectedNetwork3);
                    wifiNetworkSelector2.mLastNetworkSelectionTimeStamp = wifiNetworkSelector2.mClock.getElapsedSinceBootMillis();
                }
                if (selectedNetwork3 == null || !HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(selectedNetwork3, false)) {
                    return selectedNetwork3;
                }
                Log.w(TAG, "selectNetwork: MDM deny connect to restricted network!");
                return null;
            }
        }
        z = false;
        wifiNetworkSelector2.mFilteredNetworks = filterScanResults(scanDetails, bssidBlacklist, z, currentBssid, keys);
        if (wifiNetworkSelector2.mFilteredNetworks.size() != 0) {
        }
    }

    public /* synthetic */ void lambda$selectNetwork$0$WifiNetworkSelector(ArraySet mNetworkIds, int lastUserSelectedNetworkId, WifiCandidates wifiCandidates, NetworkEvaluator registeredEvaluator, double lastSelectionWeight, ScanDetail scanDetail, WifiConfiguration config, int score) {
        if (config != null) {
            this.mConnectableNetworks.add(Pair.create(scanDetail, config));
            mNetworkIds.add(Integer.valueOf(config.networkId));
            if (config.networkId == lastUserSelectedNetworkId) {
                wifiCandidates.add(scanDetail, config, registeredEvaluator.getId(), score, lastSelectionWeight);
            } else {
                wifiCandidates.add(scanDetail, config, registeredEvaluator.getId(), score);
            }
            this.mWifiMetrics.setNominatorForNetwork(config.networkId, evaluatorIdToNominatorId(registeredEvaluator.getId()));
        }
    }

    private static int evaluatorIdToNominatorId(int evaluatorId) {
        if (evaluatorId == 0) {
            return 2;
        }
        if (evaluatorId == 1) {
            return 3;
        }
        if (evaluatorId == 2) {
            return 4;
        }
        if (evaluatorId == 3) {
            return 5;
        }
        if (evaluatorId == 4) {
            return 6;
        }
        Log.e(TAG, "UnrecognizedEvaluatorId" + evaluatorId);
        return 0;
    }

    private double calculateLastSelectionWeight() {
        if (this.mWifiConfigManager.getLastSelectedNetwork() != -1) {
            return Math.min(Math.max(1.0d - (((double) (this.mClock.getElapsedSinceBootMillis() - this.mWifiConfigManager.getLastSelectedTimeStamp())) / 2.88E7d), 0.0d), 1.0d);
        }
        return 0.0d;
    }

    private WifiCandidates.CandidateScorer getActiveCandidateScorer() {
        int i;
        WifiCandidates.CandidateScorer ans = this.mCandidateScorers.get(PRESET_CANDIDATE_SCORER_NAME);
        int overrideExperimentId = this.mScoringParams.getExperimentIdentifier();
        if (overrideExperimentId >= MIN_SCORER_EXP_ID) {
            Iterator<WifiCandidates.CandidateScorer> it = this.mCandidateScorers.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                WifiCandidates.CandidateScorer candidateScorer = it.next();
                if (experimentIdFromIdentifier(candidateScorer.getIdentifier()) == overrideExperimentId) {
                    ans = candidateScorer;
                    break;
                }
            }
        }
        if (ans == null) {
            Log.wtf(TAG, "CompatibilityScorer is not registered!");
        }
        WifiMetrics wifiMetrics = this.mWifiMetrics;
        if (ans == null) {
            i = 0;
        } else {
            i = experimentIdFromIdentifier(ans.getIdentifier());
        }
        wifiMetrics.setNetworkSelectorExperimentId(i);
        return ans;
    }

    public void registerNetworkEvaluator(NetworkEvaluator evaluator) {
        this.mEvaluators.add((NetworkEvaluator) Preconditions.checkNotNull(evaluator));
    }

    public void registerCandidateScorer(WifiCandidates.CandidateScorer candidateScorer) {
        String name = ((WifiCandidates.CandidateScorer) Preconditions.checkNotNull(candidateScorer)).getIdentifier();
        if (name != null) {
            this.mCandidateScorers.put(name, candidateScorer);
        }
    }

    public void unregisterCandidateScorer(WifiCandidates.CandidateScorer candidateScorer) {
        String name = ((WifiCandidates.CandidateScorer) Preconditions.checkNotNull(candidateScorer)).getIdentifier();
        if (name != null) {
            this.mCandidateScorers.remove(name);
        }
    }

    public static int experimentIdFromIdentifier(String id) {
        return MIN_SCORER_EXP_ID + (((int) (((long) id.hashCode()) & 2147483647L)) % ID_SUFFIX_MOD);
    }

    WifiNetworkSelector(Context context, WifiScoreCard wifiScoreCard, ScoringParams scoringParams, WifiConfigManager configManager, Clock clock, LocalLog localLog, WifiMetrics wifiMetrics, WifiNative wifiNative) {
        this.mWifiConfigManager = configManager;
        this.mClock = clock;
        this.mWifiScoreCard = wifiScoreCard;
        this.mScoringParams = scoringParams;
        this.mLocalLog = localLog;
        this.mContext = context;
        this.mWifiMetrics = wifiMetrics;
        this.mWifiNative = wifiNative;
        this.mEnableAutoJoinWhenAssociated = context.getResources().getBoolean(17891586);
        this.mStayOnNetworkMinimumTxRate = context.getResources().getInteger(17694935);
        this.mStayOnNetworkMinimumRxRate = context.getResources().getInteger(17694934);
    }

    /* access modifiers changed from: package-private */
    public void localLog(String scanKey, String eventKey, String log) {
        localLog(scanKey, eventKey, log, null);
    }

    /* access modifiers changed from: package-private */
    public void localLog(String scanKey, String eventKey, String log, Object... params) {
        HwScanLocalLog.localLog(this.mLocalLog, scanKey, eventKey, log, params);
    }
}
