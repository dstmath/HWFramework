package com.android.server.wifi;

import android.content.Context;
import android.net.NetworkKey;
import android.net.NetworkScoreManager;
import android.net.WifiKey;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.server.wifi.util.ApConfigUtil;
import com.google.protobuf.nano.Extension;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WifiQualifiedNetworkSelector extends AbsWifiQualifiedNetworkSelector {
    public static final int BAND_AWARD_5GHz = 40;
    public static final int BSSID_BLACKLIST_EXPIRE_TIME = 300000;
    public static final int BSSID_BLACKLIST_THRESHOLD = 3;
    private static final boolean FORCE_DEBUG = true;
    private static final int INVALID_TIME_STAMP = -1;
    public static final int LAST_SELECTION_AWARD = 480;
    public static final int MINIMUM_2G_ACCEPT_RSSI = -85;
    public static final int MINIMUM_5G_ACCEPT_RSSI = -82;
    private static final int MINIMUM_QUALIFIED_NETWORK_SELECTION_INTERVAL = 10000;
    public static final int PASSPOINT_SECURITY_AWARD = 40;
    public static final int QUALIFIED_RSSI_24G_BAND = -73;
    public static final int QUALIFIED_RSSI_5G_BAND = -70;
    public static final int RSSI_SATURATION_2G_BAND = -60;
    public static final int RSSI_SATURATION_5G_BAND = -57;
    public static final int RSSI_SCORE_OFFSET = 85;
    public static final int RSSI_SCORE_SLOPE = 4;
    public static final int SAME_BSSID_AWARD = 24;
    public static final int SAME_NETWORK_AWARD = 16;
    public static final int SCAN_RESULT_MAXIMUNM_AGE = 40000;
    public static final int SECURITY_AWARD = 80;
    private static final String TAG = "WifiQualifiedNetworkSelector:";
    private Map<String, BssidBlacklistStatus> mBssidBlacklist;
    private Clock mClock;
    private Context mContext;
    private String mCurrentBssid;
    private WifiConfiguration mCurrentConnectedNetwork;
    HwCustWifiAutoJoinController mCust;
    private boolean mDbg;
    private volatile List<Pair<ScanDetail, WifiConfiguration>> mFilteredScanDetails;
    private long mLastQualifiedNetworkSelectionTimeStamp;
    private int mLastSelectionAward;
    private final LocalLog mLocalLog;
    private WifiNetworkScoreCache mNetworkScoreCache;
    private final int mNoIntnetPenalty;
    private int mPasspointSecurityAward;
    private int mRssiScoreOffset;
    private int mRssiScoreSlope;
    private int mSameBssidAward;
    private List<ScanDetail> mScanDetails;
    private NetworkScoreManager mScoreManager;
    private int mSecurityAward;
    private int mUserPreferedBand;
    private WifiConfigManager mWifiConfigManager;
    private WifiInfo mWifiInfo;

    private static class BssidBlacklistStatus {
        long mBlacklistedTimeStamp;
        int mCounter;
        boolean mIsBlacklisted;

        private BssidBlacklistStatus() {
            this.mBlacklistedTimeStamp = -1;
        }
    }

    static class ExternalScoreEvaluator {
        private int mBestCandidateType;
        private final boolean mDbg;
        private int mHighScore;
        private final LocalLog mLocalLog;
        private WifiConfiguration mSavedConfig;
        private ScanResult mScanResultCandidate;

        @Retention(RetentionPolicy.SOURCE)
        @interface BestCandidateType {
            public static final int NONE = 0;
            public static final int SAVED_NETWORK = 1;
            public static final int UNTRUSTED_NETWORK = 2;
        }

        ExternalScoreEvaluator(LocalLog localLog, boolean dbg) {
            this.mBestCandidateType = 0;
            this.mHighScore = WifiNetworkScoreCache.INVALID_NETWORK_SCORE;
            this.mLocalLog = localLog;
            this.mDbg = dbg;
        }

        void evalUntrustedCandidate(Integer score, ScanResult scanResult) {
            if (score != null && score.intValue() > this.mHighScore) {
                this.mHighScore = score.intValue();
                this.mScanResultCandidate = scanResult;
                this.mBestCandidateType = 2;
                localLog(WifiQualifiedNetworkSelector.toScanId(scanResult) + " become the new untrusted candidate");
            }
        }

        void evalSavedCandidate(Integer score, WifiConfiguration config, ScanResult scanResult) {
            if (score == null) {
                return;
            }
            if (score.intValue() > this.mHighScore || (this.mBestCandidateType == 2 && score.intValue() == this.mHighScore)) {
                this.mHighScore = score.intValue();
                this.mSavedConfig = config;
                this.mScanResultCandidate = scanResult;
                this.mBestCandidateType = 1;
                localLog(WifiQualifiedNetworkSelector.toScanId(scanResult) + " become the new externally scored saved network " + "candidate");
            }
        }

        int getBestCandidateType() {
            return this.mBestCandidateType;
        }

        int getHighScore() {
            return this.mHighScore;
        }

        public ScanResult getScanResultCandidate() {
            return this.mScanResultCandidate;
        }

        WifiConfiguration getSavedConfig() {
            return this.mSavedConfig;
        }

        private void localLog(String log) {
            if (this.mDbg) {
                this.mLocalLog.log(log);
            }
        }
    }

    private void localLog(String log) {
        if (this.mDbg) {
            this.mLocalLog.log(log);
        }
    }

    private void localLoge(String log) {
        this.mLocalLog.log(log);
    }

    void setWifiNetworkScoreCache(WifiNetworkScoreCache cache) {
        this.mNetworkScoreCache = cache;
    }

    public WifiConfiguration getConnetionTargetNetwork() {
        return this.mCurrentConnectedNetwork;
    }

    public List<Pair<ScanDetail, WifiConfiguration>> getFilteredScanDetails() {
        return this.mFilteredScanDetails;
    }

    public void setUserPreferredBand(int band) {
        this.mUserPreferedBand = band;
    }

    WifiQualifiedNetworkSelector(WifiConfigManager configureStore, Context context, WifiInfo wifiInfo, Clock clock) {
        this.mDbg = FORCE_DEBUG;
        this.mCurrentConnectedNetwork = null;
        this.mCurrentBssid = null;
        this.mScanDetails = null;
        this.mFilteredScanDetails = null;
        this.mLastQualifiedNetworkSelectionTimeStamp = -1;
        this.mLocalLog = new LocalLog(1024);
        this.mRssiScoreSlope = RSSI_SCORE_SLOPE;
        this.mRssiScoreOffset = RSSI_SCORE_OFFSET;
        this.mSameBssidAward = SAME_BSSID_AWARD;
        this.mLastSelectionAward = LAST_SELECTION_AWARD;
        this.mPasspointSecurityAward = PASSPOINT_SECURITY_AWARD;
        this.mSecurityAward = SECURITY_AWARD;
        this.mUserPreferedBand = 0;
        this.mBssidBlacklist = new HashMap();
        this.mCust = (HwCustWifiAutoJoinController) HwCustUtils.createObj(HwCustWifiAutoJoinController.class, new Object[0]);
        this.mWifiConfigManager = configureStore;
        this.mWifiInfo = wifiInfo;
        this.mClock = clock;
        this.mContext = context;
        this.mScoreManager = (NetworkScoreManager) context.getSystemService("network_score");
        if (this.mScoreManager != null) {
            this.mNetworkScoreCache = new WifiNetworkScoreCache(context);
            this.mScoreManager.registerNetworkScoreCache(1, this.mNetworkScoreCache);
        } else {
            localLoge("No network score service: Couldn't register as a WiFi score Manager, type=1 service= network_score");
            this.mNetworkScoreCache = null;
        }
        this.mRssiScoreSlope = context.getResources().getInteger(17694745);
        this.mRssiScoreOffset = context.getResources().getInteger(17694744);
        this.mSameBssidAward = context.getResources().getInteger(17694746);
        this.mLastSelectionAward = context.getResources().getInteger(17694747);
        this.mPasspointSecurityAward = context.getResources().getInteger(17694748);
        this.mSecurityAward = context.getResources().getInteger(17694749);
        this.mNoIntnetPenalty = (((((this.mWifiConfigManager.mThresholdSaturatedRssi24.get() + this.mRssiScoreOffset) * this.mRssiScoreSlope) + this.mWifiConfigManager.mBandAward5Ghz.get()) + this.mWifiConfigManager.mCurrentNetworkBoost.get()) + this.mSameBssidAward) + this.mSecurityAward;
    }

    void enableVerboseLogging(int verbose) {
        if (verbose <= 0) {
            this.mDbg = FORCE_DEBUG;
        } else {
            this.mDbg = FORCE_DEBUG;
        }
    }

    private String getNetworkString(WifiConfiguration network) {
        if (network == null) {
            return null;
        }
        return network.SSID + ":" + network.networkId;
    }

    private boolean isNetworkQualified(WifiConfiguration currentNetwork) {
        if (currentNetwork == null) {
            localLog("Disconnected");
            return false;
        }
        localLog("Current network is: " + currentNetwork.SSID + " ,ID is: " + currentNetwork.networkId);
        if (currentNetwork.ephemeral) {
            localLog("Current is ephemeral. Start reselect");
            return false;
        } else if (this.mWifiConfigManager.isOpenNetwork(currentNetwork)) {
            localLog("Current network is open network");
            return false;
        } else if (!this.mWifiInfo.is24GHz() || this.mUserPreferedBand == 2) {
            int currentRssi = this.mWifiInfo.getRssi();
            if ((!this.mWifiInfo.is24GHz() || currentRssi >= this.mWifiConfigManager.mThresholdQualifiedRssi24.get()) && (!this.mWifiInfo.is5GHz() || currentRssi >= this.mWifiConfigManager.mThresholdQualifiedRssi5.get())) {
                return FORCE_DEBUG;
            }
            localLog("Current band = " + (this.mWifiInfo.is24GHz() ? "2.4GHz band" : "5GHz band") + "current RSSI is: " + currentRssi);
            return false;
        } else {
            String str;
            StringBuilder append = new StringBuilder().append("Current band dose not match user preference. Start Qualified Network Selection Current band = ");
            if (this.mWifiInfo.is24GHz()) {
                str = "2.4GHz band";
            } else {
                str = "5GHz band";
            }
            localLog(append.append(str).append("UserPreference band = ").append(this.mUserPreferedBand).toString());
            return false;
        }
    }

    private boolean needQualifiedNetworkSelection(boolean isLinkDebouncing, boolean isConnected, boolean isDisconnected, boolean isSupplicantTransientState) {
        if (this.mScanDetails.size() == 0) {
            localLog("empty scan result");
            return false;
        } else if (isLinkDebouncing) {
            localLog("Need not Qualified Network Selection during L2 debouncing");
            return false;
        } else if (isConnected) {
            if (this.mWifiConfigManager.getEnableAutoJoinWhenAssociated()) {
                if (this.mLastQualifiedNetworkSelectionTimeStamp != -1) {
                    long gap = this.mClock.elapsedRealtime() - this.mLastQualifiedNetworkSelectionTimeStamp;
                    if (gap < 10000) {
                        localLog("Too short to last successful Qualified Network Selection Gap is:" + gap + " ms!");
                        return false;
                    }
                }
                if (this.mWifiConfigManager.getWifiConfiguration(this.mWifiInfo.getNetworkId()) == null || isNetworkQualified(this.mCurrentConnectedNetwork)) {
                    return false;
                }
                localLog("Current network is not qualified");
                return FORCE_DEBUG;
            }
            localLog("Switch network under connection is not allowed");
            return false;
        } else if (isDisconnected) {
            this.mCurrentConnectedNetwork = null;
            this.mCurrentBssid = null;
            return isSupplicantTransientState ? false : FORCE_DEBUG;
        } else {
            localLog("WifiStateMachine is not on connected or disconnected state");
            return false;
        }
    }

    int calculateBssidScore(ScanResult scanResult, WifiConfiguration network, WifiConfiguration currentNetwork, boolean sameBssid, boolean sameSelect, StringBuffer sbuf) {
        int score = ((this.mRssiScoreOffset + (scanResult.level <= this.mWifiConfigManager.mThresholdSaturatedRssi24.get() ? scanResult.level : this.mWifiConfigManager.mThresholdSaturatedRssi24.get())) * this.mRssiScoreSlope) + 0;
        sbuf.append(" RSSI score: " + score);
        if (scanResult.is5GHz()) {
            score += this.mWifiConfigManager.mBandAward5Ghz.get();
            sbuf.append(" 5GHz bonus: " + this.mWifiConfigManager.mBandAward5Ghz.get());
        }
        if (sameSelect) {
            long timeDifference = this.mClock.elapsedRealtime() - this.mWifiConfigManager.getLastSelectedTimeStamp();
            if (timeDifference > 0) {
                int bonus = this.mLastSelectionAward - ((int) ((timeDifference / 1000) / 60));
                score += bonus > 0 ? bonus : 0;
                sbuf.append(" User selected it last time " + ((timeDifference / 1000) / 60) + " minutes ago, bonus:" + bonus);
            }
        }
        if (network == currentNetwork || network.isLinked(currentNetwork)) {
            score += this.mWifiConfigManager.mCurrentNetworkBoost.get();
            sbuf.append(" Same network with current associated. Bonus: " + this.mWifiConfigManager.mCurrentNetworkBoost.get());
        }
        if (sameBssid) {
            score += this.mSameBssidAward;
            sbuf.append(" Same BSSID with current association. Bonus: " + this.mSameBssidAward);
        }
        if (network.isPasspoint()) {
            score += this.mPasspointSecurityAward;
            sbuf.append(" Passpoint Bonus:" + this.mPasspointSecurityAward);
        } else if (!this.mWifiConfigManager.isOpenNetwork(network)) {
            score += this.mSecurityAward;
            sbuf.append(" Secure network Bonus:" + this.mSecurityAward);
        }
        if (network.numNoInternetAccessReports > 0 && !network.validatedInternetAccess) {
            score -= this.mNoIntnetPenalty;
            sbuf.append(" No internet Penalty:-" + this.mNoIntnetPenalty);
        }
        sbuf.append(" Score for scanResult: " + scanResult + " and Network ID: " + network.networkId + " final score:" + score + "\n\n");
        return score;
    }

    private void updateSavedNetworkSelectionStatus() {
        List<WifiConfiguration> savedNetworks = this.mWifiConfigManager.getSavedNetworks();
        if (savedNetworks.size() == 0) {
            localLog("no saved network");
            return;
        }
        StringBuffer sbuf = new StringBuffer("Saved Network List\n");
        for (WifiConfiguration network : savedNetworks) {
            NetworkSelectionStatus status = this.mWifiConfigManager.getWifiConfiguration(network.networkId).getNetworkSelectionStatus();
            if (status.isNetworkTemporaryDisabled()) {
                this.mWifiConfigManager.tryEnableQualifiedNetwork(network.networkId);
            }
            status.setCandidate(null);
            status.setCandidateScore(Integer.MIN_VALUE);
            status.setSeenInLastQualifiedNetworkSelection(false);
            sbuf.append("    " + getNetworkString(network) + " " + " User Preferred BSSID:" + network.BSSID + " FQDN:" + network.FQDN + " " + status.getNetworkStatusString() + " Disable account: ");
            for (int index = 0; index < 11; index++) {
                sbuf.append(status.getDisableReasonCounter(index) + " ");
            }
            sbuf.append("Connect Choice:" + status.getConnectChoice() + " set time:" + status.getConnectChoiceTimestamp());
            sbuf.append("\n");
        }
        localLog(sbuf.toString());
    }

    public boolean userSelectNetwork(int netId, boolean persist) {
        WifiConfiguration selected = this.mWifiConfigManager.getWifiConfiguration(netId);
        localLog("userSelectNetwork:" + netId + " persist:" + persist);
        if (selected == null || selected.SSID == null) {
            localLoge("userSelectNetwork: Bad configuration with nid=" + netId);
            return false;
        }
        if (!selected.getNetworkSelectionStatus().isNetworkEnabled()) {
            this.mWifiConfigManager.updateNetworkSelectionStatus(netId, 0);
        }
        if (persist) {
            boolean change = false;
            String key = selected.configKey();
            long currentTime = this.mClock.currentTimeMillis();
            for (WifiConfiguration network : this.mWifiConfigManager.getSavedNetworks()) {
                WifiConfiguration config = this.mWifiConfigManager.getWifiConfiguration(network.networkId);
                NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                if (config.networkId == selected.networkId) {
                    if (status.getConnectChoice() != null) {
                        localLog("Remove user selection preference of " + status.getConnectChoice() + " Set Time: " + status.getConnectChoiceTimestamp() + " from " + config.SSID + " : " + config.networkId);
                        status.setConnectChoice(null);
                        status.setConnectChoiceTimestamp(-1);
                        change = FORCE_DEBUG;
                    }
                } else if (status.getSeenInLastQualifiedNetworkSelection() && (status.getConnectChoice() == null || !status.getConnectChoice().equals(key))) {
                    localLog("Add key:" + key + " Set Time: " + currentTime + " to " + getNetworkString(config));
                    status.setConnectChoice(key);
                    status.setConnectChoiceTimestamp(currentTime);
                    change = FORCE_DEBUG;
                }
            }
            if (!change) {
                return false;
            }
            this.mWifiConfigManager.writeKnownNetworkHistory();
            return FORCE_DEBUG;
        }
        localLog("User has no privilege to overwrite the current priority");
        return false;
    }

    public boolean enableBssidForQualityNetworkSelection(String bssid, boolean enable) {
        boolean z = FORCE_DEBUG;
        if (enable) {
            if (this.mBssidBlacklist.remove(bssid) == null) {
                z = false;
            }
            return z;
        }
        if (bssid != null) {
            BssidBlacklistStatus status = (BssidBlacklistStatus) this.mBssidBlacklist.get(bssid);
            if (status == null) {
                BssidBlacklistStatus newStatus = new BssidBlacklistStatus();
                newStatus.mCounter++;
                this.mBssidBlacklist.put(bssid, newStatus);
            } else if (!status.mIsBlacklisted) {
                status.mCounter++;
                if (status.mCounter >= BSSID_BLACKLIST_THRESHOLD) {
                    status.mIsBlacklisted = FORCE_DEBUG;
                    status.mBlacklistedTimeStamp = this.mClock.elapsedRealtime();
                    return FORCE_DEBUG;
                }
            }
        }
        return false;
    }

    private void updateBssidBlacklist() {
        Iterator<BssidBlacklistStatus> iter = this.mBssidBlacklist.values().iterator();
        while (iter.hasNext()) {
            BssidBlacklistStatus status = (BssidBlacklistStatus) iter.next();
            if (status != null && status.mIsBlacklisted && this.mClock.elapsedRealtime() - status.mBlacklistedTimeStamp >= 300000) {
                iter.remove();
            }
        }
    }

    public boolean isBssidDisabled(String bssid) {
        BssidBlacklistStatus status = (BssidBlacklistStatus) this.mBssidBlacklist.get(bssid);
        return status == null ? false : status.mIsBlacklisted;
    }

    public WifiConfiguration selectQualifiedNetwork(boolean forceSelectNetwork, boolean isUntrustedConnectionsAllowed, List<ScanDetail> scanDetails, boolean isLinkDebouncing, boolean isConnected, boolean isDisconnected, boolean isSupplicantTransient) {
        localLog("==========start qualified Network Selection==========");
        this.mScanDetails = scanDetails;
        List<Pair<ScanDetail, WifiConfiguration>> filteredScanDetails = new ArrayList();
        if (this.mCurrentConnectedNetwork == null) {
            this.mCurrentConnectedNetwork = this.mWifiConfigManager.getWifiConfiguration(this.mWifiInfo.getNetworkId());
        }
        if (this.mCurrentBssid == null) {
            this.mCurrentBssid = this.mWifiInfo.getBSSID();
        }
        if (forceSelectNetwork || needQualifiedNetworkSelection(isLinkDebouncing, isConnected, isDisconnected, isSupplicantTransient)) {
            int currentHighestScore = Integer.MIN_VALUE;
            ScanResult scanResultCandidate = null;
            WifiConfiguration networkCandidate = null;
            ExternalScoreEvaluator externalScoreEvaluator = new ExternalScoreEvaluator(this.mLocalLog, this.mDbg);
            WifiConfiguration lastUserSelectedNetwork = this.mWifiConfigManager.getWifiConfiguration(this.mWifiConfigManager.getLastSelectedConfiguration());
            if (lastUserSelectedNetwork != null) {
                localLog("Last selection is " + lastUserSelectedNetwork.SSID + " Time to now: " + (((this.mClock.elapsedRealtime() - this.mWifiConfigManager.getLastSelectedTimeStamp()) / 1000) / 60) + " minutes");
            }
            updateSavedNetworkSelectionStatus();
            updateBssidBlacklist();
            StringBuffer lowSignalScan = new StringBuffer();
            StringBuffer notSavedScan = new StringBuffer();
            StringBuffer noValidSsid = new StringBuffer();
            StringBuffer scoreHistory = new StringBuffer();
            ArrayList<NetworkKey> unscoredNetworks = new ArrayList();
            boolean cloudSecurityCheckOn = Global.getInt(this.mContext.getContentResolver(), "wifi_cloud_security_check", 0) == 1 ? FORCE_DEBUG : false;
            resetConnectConfig();
            for (ScanDetail scanDetail : this.mScanDetails) {
                ScanResult scanResult = scanDetail.getScanResult();
                if (scanResult.SSID != null && !TextUtils.isEmpty(scanResult.SSID)) {
                    String scanId = toScanId(scanResult);
                    if (this.mWifiConfigManager.isBssidBlacklisted(scanResult.BSSID) || isBssidDisabled(scanResult.BSSID)) {
                        Log.e(TAG, scanId + " is in blacklist.");
                    } else if ((!scanResult.is24GHz() || scanResult.level >= this.mWifiConfigManager.mThresholdMinimumRssi24.get()) && (!scanResult.is5GHz() || scanResult.level >= this.mWifiConfigManager.mThresholdMinimumRssi5.get())) {
                        WifiConfiguration network;
                        if (!(this.mNetworkScoreCache == null || this.mNetworkScoreCache.isScoredNetwork(scanResult))) {
                            try {
                                unscoredNetworks.add(new NetworkKey(new WifiKey("\"" + scanResult.SSID + "\"", scanResult.BSSID)));
                            } catch (IllegalArgumentException e) {
                                Log.w(TAG, "Invalid SSID=" + scanResult.SSID + " BSSID=" + scanResult.BSSID + " for network score. Skip.");
                            }
                        }
                        boolean potentiallyEphemeral = false;
                        WifiConfiguration potentialEphemeralCandidate = null;
                        WifiConfigManager wifiConfigManager = this.mWifiConfigManager;
                        boolean z = (isSupplicantTransient || isConnected) ? FORCE_DEBUG : isLinkDebouncing;
                        List<WifiConfiguration> associatedWifiConfigurations = wifiConfigManager.updateSavedNetworkWithNewScanDetail(scanDetail, z);
                        if (associatedWifiConfigurations == null) {
                            potentiallyEphemeral = FORCE_DEBUG;
                            if (this.mDbg) {
                                notSavedScan.append(scanId + " / ");
                            }
                        } else if (associatedWifiConfigurations.size() == 1) {
                            network = (WifiConfiguration) associatedWifiConfigurations.get(0);
                            if (network.ephemeral) {
                                potentialEphemeralCandidate = network;
                                potentiallyEphemeral = FORCE_DEBUG;
                            }
                        }
                        if (!potentiallyEphemeral) {
                            int highestScore = Integer.MIN_VALUE;
                            WifiConfiguration configurationCandidateForThisScan = null;
                            WifiConfiguration potentialCandidate = null;
                            for (WifiConfiguration network2 : associatedWifiConfigurations) {
                                NetworkSelectionStatus status = network2.getNetworkSelectionStatus();
                                status.setSeenInLastQualifiedNetworkSelection(FORCE_DEBUG);
                                if (potentialCandidate == null) {
                                    potentialCandidate = network2;
                                }
                                if (status.isNetworkEnabled()) {
                                    if (network2.BSSID != null && !network2.BSSID.equals(WifiLastResortWatchdog.BSSID_ANY) && !network2.BSSID.equals(scanResult.BSSID)) {
                                        localLog("Network: " + getNetworkString(network2) + " has specified" + "BSSID:" + network2.BSSID + ". Skip " + scanResult.BSSID);
                                    } else if (this.mCust != null && this.mCust.isWifiAutoJoinPriority()) {
                                        if (networkCandidate == null) {
                                            networkCandidate = network2;
                                        }
                                        networkCandidate = this.mCust.attemptAutoJoinCust(networkCandidate, network2);
                                        configurationCandidateForThisScan = networkCandidate;
                                        potentialCandidate = networkCandidate;
                                        scanResultCandidate = scanResult;
                                        status.setCandidate(scanResult);
                                    } else if (network2.cloudSecurityCheck != 0 && cloudSecurityCheckOn) {
                                        localLog("SSID = " + network2.SSID + ",cloudSecurityCheck = " + network2.cloudSecurityCheck + ", don`t attemptAutoJoin.");
                                    } else if (!selectBestNetworkByWifiPro(network2, scanResult)) {
                                        if (network2.useExternalScores) {
                                            externalScoreEvaluator.evalSavedCandidate(getNetworkScore(scanResult, false), network2, scanResult);
                                        } else {
                                            WifiConfiguration wifiConfiguration = this.mCurrentConnectedNetwork;
                                            boolean equals = this.mCurrentBssid == null ? false : this.mCurrentBssid.equals(scanResult.BSSID);
                                            boolean z2 = (lastUserSelectedNetwork != null && lastUserSelectedNetwork.networkId == network2.networkId) ? FORCE_DEBUG : false;
                                            int score = calculateBssidScore(scanResult, network2, wifiConfiguration, equals, z2, scoreHistory);
                                            if (HuaweiTelephonyConfigs.isChinaMobile()) {
                                                if (configurationCandidateForThisScan == null) {
                                                    configurationCandidateForThisScan = network2;
                                                } else if (network2.priority > configurationCandidateForThisScan.priority) {
                                                    configurationCandidateForThisScan = network2;
                                                }
                                            } else if (score > highestScore) {
                                                highestScore = score;
                                                configurationCandidateForThisScan = network2;
                                                potentialCandidate = network2;
                                            }
                                            if (score > status.getCandidateScore() || (score == status.getCandidateScore() && status.getCandidate() != null && scanResult.level > status.getCandidate().level)) {
                                                status.setCandidate(scanResult);
                                                status.setCandidateScore(score);
                                            }
                                        }
                                    }
                                }
                            }
                            filteredScanDetails.add(Pair.create(scanDetail, potentialCandidate));
                            if (HuaweiTelephonyConfigs.isChinaMobile()) {
                                if (configurationCandidateForThisScan != null) {
                                    if (networkCandidate == null) {
                                        scanResultCandidate = scanResult;
                                        networkCandidate = configurationCandidateForThisScan;
                                    } else if (configurationCandidateForThisScan.priority > networkCandidate.priority) {
                                        scanResultCandidate = scanResult;
                                        networkCandidate = configurationCandidateForThisScan;
                                    }
                                }
                            } else if (this.mCust != null && this.mCust.isWifiAutoJoinPriority()) {
                                localLog("isWifiAutoJoinPriority networkCandidate : " + networkCandidate);
                            } else if (highestScore > currentHighestScore || (highestScore == currentHighestScore && r40 != null && scanResult.level > r40.level)) {
                                currentHighestScore = highestScore;
                                scanResultCandidate = scanResult;
                                networkCandidate = configurationCandidateForThisScan;
                            }
                        } else if (isUntrustedConnectionsAllowed) {
                            Integer netScore = getNetworkScore(scanResult, false);
                            if (!(netScore == null || this.mWifiConfigManager.wasEphemeralNetworkDeleted(scanResult.SSID))) {
                                externalScoreEvaluator.evalUntrustedCandidate(netScore, scanResult);
                                filteredScanDetails.add(Pair.create(scanDetail, potentialEphemeralCandidate));
                            }
                        }
                    } else if (this.mDbg) {
                        lowSignalScan.append(scanId + "(" + (scanResult.is24GHz() ? "2.4GHz" : "5GHz") + ")" + scanResult.level + " / ");
                    }
                } else if (this.mDbg) {
                    noValidSsid.append(scanResult.BSSID + " / ");
                }
            }
            networkCandidate = candidateUpdatedByWifiPro(networkCandidate);
            scanResultCandidate = scanResultUpdatedByWifiPro(networkCandidate, scanResultCandidate);
            Log.d(TAG, "look up all networks done, chosen candidate = " + (networkCandidate != null ? networkCandidate.SSID : null));
            this.mFilteredScanDetails = filteredScanDetails;
            if (!(this.mScoreManager == null || unscoredNetworks.size() == 0)) {
                this.mScoreManager.requestScores((NetworkKey[]) unscoredNetworks.toArray(new NetworkKey[unscoredNetworks.size()]));
            }
            if (this.mDbg) {
                Log.d(TAG, lowSignalScan + " skipped due to low signal\n");
                localLog(notSavedScan + " skipped due to not saved\n ");
                localLog(noValidSsid + " skipped due to not valid SSID\n");
                localLog(scoreHistory.toString());
            }
            if (!(isWifiProEnabled() || scanResultCandidate == null)) {
                WifiConfiguration tempConfig = networkCandidate;
                while (tempConfig.getNetworkSelectionStatus().getConnectChoice() != null) {
                    String key = tempConfig.getNetworkSelectionStatus().getConnectChoice();
                    tempConfig = this.mWifiConfigManager.getWifiConfiguration(key);
                    if (tempConfig == null) {
                        localLoge("Connect choice: " + key + " has no corresponding saved config");
                        break;
                    }
                    NetworkSelectionStatus tempStatus = tempConfig.getNetworkSelectionStatus();
                    if (tempStatus.getCandidate() != null && tempStatus.isNetworkEnabled()) {
                        if (this.mCurrentConnectedNetwork == null || !this.mCurrentConnectedNetwork.isLinked(networkCandidate) || (tempStatus.getCandidate().level >= -75 && tempStatus.getCandidateScore() + PASSPOINT_SECURITY_AWARD >= this.mCurrentConnectedNetwork.getNetworkSelectionStatus().getCandidateScore())) {
                            scanResultCandidate = tempStatus.getCandidate();
                            if (this.mCust == null || !this.mCust.isWifiAutoJoinPriority()) {
                                networkCandidate = tempConfig;
                            } else {
                                networkCandidate = this.mCust.attemptAutoJoinCust(networkCandidate, tempConfig);
                            }
                        } else {
                            localLoge("ConnectChoice" + key + "is not good enough to handover, ignore.");
                        }
                    }
                }
                localLog("After user choice adjust, the final candidate is:" + getNetworkString(networkCandidate) + " : " + scanResultCandidate.BSSID);
            }
            if (scanResultCandidate == null) {
                localLog("Checking the externalScoreEvaluator for candidates...");
                networkCandidate = getExternalScoreCandidate(externalScoreEvaluator);
                if (networkCandidate != null) {
                    scanResultCandidate = networkCandidate.getNetworkSelectionStatus().getCandidate();
                }
            }
            if (scanResultCandidate == null) {
                localLog("Can not find any suitable candidates");
                return null;
            }
            String currentAssociationId;
            if (this.mCurrentConnectedNetwork == null) {
                currentAssociationId = "Disconnected";
            } else {
                currentAssociationId = getNetworkString(this.mCurrentConnectedNetwork);
            }
            String targetAssociationId = getNetworkString(networkCandidate);
            if (networkCandidate.isPasspoint()) {
                networkCandidate.SSID = "\"" + scanResultCandidate.SSID + "\"";
            }
            if (scanResultCandidate.BSSID.equals(this.mCurrentBssid)) {
                localLog(currentAssociationId + " is already the best choice!");
            } else if (this.mCurrentConnectedNetwork == null || !(this.mCurrentConnectedNetwork.networkId == networkCandidate.networkId || this.mCurrentConnectedNetwork.isLinked(networkCandidate))) {
                localLog("reconnect from " + currentAssociationId + " to " + targetAssociationId);
            } else {
                localLog("Roaming from " + currentAssociationId + " to " + targetAssociationId);
            }
            this.mCurrentBssid = scanResultCandidate.BSSID;
            this.mCurrentConnectedNetwork = networkCandidate;
            this.mLastQualifiedNetworkSelectionTimeStamp = this.mClock.elapsedRealtime();
            return networkCandidate;
        }
        localLog("Quit qualified Network Selection since it is not forced and current network is qualified already");
        this.mFilteredScanDetails = filteredScanDetails;
        return null;
    }

    WifiConfiguration getExternalScoreCandidate(ExternalScoreEvaluator scoreEvaluator) {
        switch (scoreEvaluator.getBestCandidateType()) {
            case ApConfigUtil.SUCCESS /*0*/:
                localLog("ExternalScoreEvaluator did not see any good candidates.");
                return null;
            case Extension.TYPE_DOUBLE /*1*/:
                ScanResult scanResultCandidate = scoreEvaluator.getScanResultCandidate();
                WifiConfiguration networkCandidate = scoreEvaluator.getSavedConfig();
                networkCandidate.getNetworkSelectionStatus().setCandidate(scanResultCandidate);
                localLog(String.format("new scored candidate %s network ID:%d", new Object[]{toScanId(scanResultCandidate), Integer.valueOf(networkCandidate.networkId)}));
                return networkCandidate;
            case Extension.TYPE_FLOAT /*2*/:
                ScanResult untrustedScanResultCandidate = scoreEvaluator.getScanResultCandidate();
                WifiConfiguration unTrustedNetworkCandidate = this.mWifiConfigManager.wifiConfigurationFromScanResult(untrustedScanResultCandidate);
                unTrustedNetworkCandidate.ephemeral = FORCE_DEBUG;
                if (this.mNetworkScoreCache != null) {
                    unTrustedNetworkCandidate.meteredHint = this.mNetworkScoreCache.getMeteredHint(untrustedScanResultCandidate);
                }
                this.mWifiConfigManager.saveNetwork(unTrustedNetworkCandidate, INVALID_TIME_STAMP);
                Object[] objArr = new Object[BSSID_BLACKLIST_THRESHOLD];
                objArr[0] = toScanId(untrustedScanResultCandidate);
                objArr[1] = Integer.valueOf(unTrustedNetworkCandidate.networkId);
                objArr[2] = Boolean.valueOf(unTrustedNetworkCandidate.meteredHint);
                localLog(String.format("new ephemeral candidate %s network ID:%d, meteredHint=%b", objArr));
                unTrustedNetworkCandidate.getNetworkSelectionStatus().setCandidate(untrustedScanResultCandidate);
                return unTrustedNetworkCandidate;
            default:
                localLoge("Unhandled ExternalScoreEvaluator case. No candidate selected.");
                return null;
        }
    }

    Integer getNetworkScore(ScanResult scanResult, boolean isActiveNetwork) {
        if (this.mNetworkScoreCache == null || !this.mNetworkScoreCache.isScoredNetwork(scanResult)) {
            return null;
        }
        int networkScore = this.mNetworkScoreCache.getNetworkScore(scanResult, isActiveNetwork);
        localLog(toScanId(scanResult) + " has score: " + networkScore);
        return Integer.valueOf(networkScore);
    }

    private static String toScanId(ScanResult scanResult) {
        if (scanResult == null) {
            return "NULL";
        }
        return String.format("%s:%s", new Object[]{scanResult.SSID, scanResult.BSSID});
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiQualifiedNetworkSelector");
        pw.println("WifiQualifiedNetworkSelector - Log Begin ----");
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiQualifiedNetworkSelector - Log End ----");
    }

    public boolean isWifiProEnabled() {
        return false;
    }

    public boolean selectBestNetworkByWifiPro(WifiConfiguration config, ScanResult scanResult) {
        return false;
    }

    public WifiConfiguration candidateUpdatedByWifiPro(WifiConfiguration config) {
        return config;
    }

    public ScanResult scanResultUpdatedByWifiPro(WifiConfiguration networkCandidate, ScanResult scanResult) {
        return scanResult;
    }

    public void portalNotifyChanged(boolean on, String ssid) {
    }

    public void resetSelfCureCandidateLostCnt() {
    }
}
