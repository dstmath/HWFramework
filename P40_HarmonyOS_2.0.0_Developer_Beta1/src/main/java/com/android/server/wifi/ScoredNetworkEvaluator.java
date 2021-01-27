package com.android.server.wifi;

import android.content.Context;
import android.database.ContentObserver;
import android.net.NetworkKey;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppData;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiNetworkScoreCache;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.wifi.WifiNetworkSelector;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.WifiPermissionsUtil;
import java.util.ArrayList;
import java.util.List;

public class ScoredNetworkEvaluator implements WifiNetworkSelector.NetworkEvaluator {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "ScoredNetworkEvaluator";
    private final ContentObserver mContentObserver;
    private final LocalLog mLocalLog;
    private boolean mNetworkRecommendationsEnabled;
    private final NetworkScoreManager mNetworkScoreManager;
    private WifiNetworkScoreCache mScoreCache;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiPermissionsUtil mWifiPermissionsUtil;

    ScoredNetworkEvaluator(final Context context, Looper looper, final FrameworkFacade frameworkFacade, NetworkScoreManager networkScoreManager, WifiConfigManager wifiConfigManager, LocalLog localLog, WifiNetworkScoreCache wifiNetworkScoreCache, WifiPermissionsUtil wifiPermissionsUtil) {
        this.mScoreCache = wifiNetworkScoreCache;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mNetworkScoreManager = networkScoreManager;
        this.mWifiConfigManager = wifiConfigManager;
        this.mLocalLog = localLog;
        this.mContentObserver = new ContentObserver(new Handler(looper)) {
            /* class com.android.server.wifi.ScoredNetworkEvaluator.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                ScoredNetworkEvaluator scoredNetworkEvaluator = ScoredNetworkEvaluator.this;
                boolean z = true;
                if (frameworkFacade.getIntegerSetting(context, "network_recommendations_enabled", 0) != 1) {
                    z = false;
                }
                scoredNetworkEvaluator.mNetworkRecommendationsEnabled = z;
            }
        };
        frameworkFacade.registerContentObserver(context, Settings.Global.getUriFor("network_recommendations_enabled"), false, this.mContentObserver);
        this.mContentObserver.onChange(false);
        LocalLog localLog2 = this.mLocalLog;
        localLog2.log("ScoredNetworkEvaluator constructed. mNetworkRecommendationsEnabled: " + this.mNetworkRecommendationsEnabled);
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public void update(List<ScanDetail> scanDetails) {
        if (this.mNetworkRecommendationsEnabled) {
            updateNetworkScoreCache(scanDetails);
        }
    }

    private void updateNetworkScoreCache(List<ScanDetail> scanDetails) {
        ArrayList<NetworkKey> unscoredNetworks = new ArrayList<>();
        for (int i = 0; i < scanDetails.size(); i++) {
            NetworkKey networkKey = NetworkKey.createFromScanResult(scanDetails.get(i).getScanResult());
            if (networkKey != null && this.mScoreCache.getScoredNetwork(networkKey) == null) {
                unscoredNetworks.add(networkKey);
            }
        }
        if (!unscoredNetworks.isEmpty() && activeScorerAllowedtoSeeScanResults()) {
            this.mNetworkScoreManager.requestScores((NetworkKey[]) unscoredNetworks.toArray(new NetworkKey[unscoredNetworks.size()]));
        }
    }

    private boolean activeScorerAllowedtoSeeScanResults() {
        NetworkScorerAppData networkScorerAppData = this.mNetworkScoreManager.getActiveScorer();
        String packageName = this.mNetworkScoreManager.getActiveScorerPackage();
        if (networkScorerAppData == null || packageName == null) {
            return false;
        }
        try {
            this.mWifiPermissionsUtil.enforceCanAccessScanResults(packageName, networkScorerAppData.packageUid);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00de, code lost:
        if (android.text.TextUtils.equals(r17, r7.BSSID) != false) goto L_0x00e4;
     */
    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, WifiNetworkSelector.NetworkEvaluator.OnConnectableListener onConnectableListener) {
        if (!this.mNetworkRecommendationsEnabled) {
            this.mLocalLog.log("Skipping evaluateNetworks; Network recommendations disabled.");
            return null;
        }
        ScoreTracker scoreTracker = new ScoreTracker();
        for (int i = 0; i < scanDetails.size(); i++) {
            ScanDetail scanDetail = scanDetails.get(i);
            ScanResult scanResult = scanDetail.getScanResult();
            if (scanResult != null) {
                if (this.mWifiConfigManager.wasEphemeralNetworkDeleted(ScanResultUtil.createQuotedSSID(scanResult.SSID))) {
                    debugLog("Ignoring disabled ephemeral SSID: " + StringUtilEx.safeDisplaySsid(scanResult.SSID));
                } else {
                    WifiConfiguration configuredNetwork = this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail);
                    boolean isCurrentNetwork = true;
                    boolean untrustedScanResult = configuredNetwork == null || !configuredNetwork.trusted;
                    if (untrustedNetworkAllowed || !untrustedScanResult) {
                        if (configuredNetwork == null) {
                            if (ScanResultUtil.isScanResultForOpenNetwork(scanResult)) {
                                scoreTracker.trackUntrustedCandidate(scanDetail);
                            }
                        } else if (!configuredNetwork.trusted || configuredNetwork.useExternalScores) {
                            if (!configuredNetwork.getNetworkSelectionStatus().isNetworkEnabled()) {
                                debugLog("Ignoring disabled SSID: " + StringUtilEx.safeDisplaySsid(configuredNetwork.SSID));
                            } else if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(configuredNetwork, false)) {
                                Log.w(TAG, "evaluateNetworks: MDM deny connect to restricted network!");
                            } else {
                                if (currentNetwork == null || currentNetwork.networkId != configuredNetwork.networkId) {
                                }
                                isCurrentNetwork = false;
                                if (!configuredNetwork.trusted) {
                                    scoreTracker.trackUntrustedCandidate(scanResult, configuredNetwork, isCurrentNetwork);
                                } else {
                                    scoreTracker.trackExternallyScoredCandidate(scanResult, configuredNetwork, isCurrentNetwork);
                                }
                                onConnectableListener.onConnectable(scanDetail, configuredNetwork, 0);
                            }
                        }
                    }
                }
            }
        }
        return scoreTracker.getCandidateConfiguration(onConnectableListener);
    }

    class ScoreTracker {
        private static final int EXTERNAL_SCORED_NONE = 0;
        private static final int EXTERNAL_SCORED_SAVED_NETWORK = 1;
        private static final int EXTERNAL_SCORED_UNTRUSTED_NETWORK = 2;
        private int mBestCandidateType = 0;
        private WifiConfiguration mEphemeralConfig;
        private int mHighScore = -128;
        private WifiConfiguration mSavedConfig;
        private ScanDetail mScanDetailCandidate;
        private ScanResult mScanResultCandidate;

        ScoreTracker() {
        }

        private Integer getNetworkScore(ScanResult scanResult, boolean isCurrentNetwork) {
            if (!ScoredNetworkEvaluator.this.mScoreCache.isScoredNetwork(scanResult)) {
                return null;
            }
            int score = ScoredNetworkEvaluator.this.mScoreCache.getNetworkScore(scanResult, isCurrentNetwork);
            if (ScoredNetworkEvaluator.DEBUG) {
                LocalLog localLog = ScoredNetworkEvaluator.this.mLocalLog;
                localLog.log(WifiNetworkSelector.toScanId(scanResult) + " has score: " + score + " isCurrentNetwork network: " + isCurrentNetwork);
            }
            return Integer.valueOf(score);
        }

        /* access modifiers changed from: package-private */
        public void trackUntrustedCandidate(ScanDetail scanDetail) {
            ScanResult scanResult = scanDetail.getScanResult();
            Integer score = getNetworkScore(scanResult, false);
            if (score != null && score.intValue() > this.mHighScore) {
                this.mHighScore = score.intValue();
                this.mScanResultCandidate = scanResult;
                this.mScanDetailCandidate = scanDetail;
                this.mBestCandidateType = 2;
                ScoredNetworkEvaluator scoredNetworkEvaluator = ScoredNetworkEvaluator.this;
                scoredNetworkEvaluator.debugLog(WifiNetworkSelector.toScanId(scanResult) + " becomes the new untrusted candidate.");
            }
        }

        /* access modifiers changed from: package-private */
        public void trackUntrustedCandidate(ScanResult scanResult, WifiConfiguration config, boolean isCurrentNetwork) {
            Integer score = getNetworkScore(scanResult, isCurrentNetwork);
            if (score != null && score.intValue() > this.mHighScore) {
                this.mHighScore = score.intValue();
                this.mScanResultCandidate = scanResult;
                this.mScanDetailCandidate = null;
                this.mBestCandidateType = 2;
                this.mEphemeralConfig = config;
                ScoredNetworkEvaluator.this.mWifiConfigManager.setNetworkCandidateScanResult(config.networkId, scanResult, 0);
                ScoredNetworkEvaluator scoredNetworkEvaluator = ScoredNetworkEvaluator.this;
                scoredNetworkEvaluator.debugLog(WifiNetworkSelector.toScanId(scanResult) + " becomes the new untrusted candidate.");
            }
        }

        /* access modifiers changed from: package-private */
        public void trackExternallyScoredCandidate(ScanResult scanResult, WifiConfiguration config, boolean isCurrentNetwork) {
            Integer score = getNetworkScore(scanResult, isCurrentNetwork);
            if (score == null) {
                return;
            }
            if (score.intValue() > this.mHighScore || (this.mBestCandidateType == 2 && score.intValue() == this.mHighScore)) {
                this.mHighScore = score.intValue();
                this.mSavedConfig = config;
                this.mScanResultCandidate = scanResult;
                this.mScanDetailCandidate = null;
                this.mBestCandidateType = 1;
                ScoredNetworkEvaluator.this.mWifiConfigManager.setNetworkCandidateScanResult(config.networkId, scanResult, 0);
                ScoredNetworkEvaluator scoredNetworkEvaluator = ScoredNetworkEvaluator.this;
                scoredNetworkEvaluator.debugLog(WifiNetworkSelector.toScanId(scanResult) + " becomes the new externally scored saved network candidate.");
            }
        }

        /* access modifiers changed from: package-private */
        public WifiConfiguration getCandidateConfiguration(WifiNetworkSelector.NetworkEvaluator.OnConnectableListener onConnectableListener) {
            ScanDetail scanDetail;
            int candidateNetworkId = -1;
            int i = this.mBestCandidateType;
            if (i == 1) {
                candidateNetworkId = this.mSavedConfig.networkId;
                ScoredNetworkEvaluator.this.mLocalLog.log(String.format("new saved network candidate %s network ID:%d", WifiNetworkSelector.toScanId(this.mScanResultCandidate), Integer.valueOf(candidateNetworkId)));
            } else if (i != 2) {
                ScoredNetworkEvaluator.this.mLocalLog.log("ScoredNetworkEvaluator did not see any good candidates.");
            } else {
                WifiConfiguration wifiConfiguration = this.mEphemeralConfig;
                if (wifiConfiguration != null) {
                    candidateNetworkId = wifiConfiguration.networkId;
                    ScoredNetworkEvaluator.this.mLocalLog.log(String.format("existing ephemeral candidate %s network ID:%d, meteredHint=%b", WifiNetworkSelector.toScanId(this.mScanResultCandidate), Integer.valueOf(candidateNetworkId), Boolean.valueOf(this.mEphemeralConfig.meteredHint)));
                } else {
                    this.mEphemeralConfig = ScanResultUtil.createNetworkFromScanResult(this.mScanResultCandidate);
                    WifiConfiguration wifiConfiguration2 = this.mEphemeralConfig;
                    wifiConfiguration2.ephemeral = true;
                    wifiConfiguration2.trusted = false;
                    wifiConfiguration2.meteredHint = ScoredNetworkEvaluator.this.mScoreCache.getMeteredHint(this.mScanResultCandidate);
                    NetworkUpdateResult result = ScoredNetworkEvaluator.this.mWifiConfigManager.addOrUpdateNetwork(this.mEphemeralConfig, 1010);
                    if (!result.isSuccess()) {
                        ScoredNetworkEvaluator.this.mLocalLog.log("Failed to add ephemeral network");
                    } else if (!ScoredNetworkEvaluator.this.mWifiConfigManager.updateNetworkSelectionStatus(result.getNetworkId(), 0)) {
                        ScoredNetworkEvaluator.this.mLocalLog.log("Failed to make ephemeral network selectable");
                    } else {
                        candidateNetworkId = result.getNetworkId();
                        if (this.mScanDetailCandidate == null) {
                            Log.e(ScoredNetworkEvaluator.TAG, "mScanDetailCandidate is null!");
                        }
                        ScoredNetworkEvaluator.this.mWifiConfigManager.setNetworkCandidateScanResult(candidateNetworkId, this.mScanResultCandidate, 0);
                        ScoredNetworkEvaluator.this.mLocalLog.log(String.format("new ephemeral candidate %s network ID:%d, meteredHint=%b", WifiNetworkSelector.toScanId(this.mScanResultCandidate), Integer.valueOf(candidateNetworkId), Boolean.valueOf(this.mEphemeralConfig.meteredHint)));
                    }
                }
            }
            WifiConfiguration ans = ScoredNetworkEvaluator.this.mWifiConfigManager.getConfiguredNetwork(candidateNetworkId);
            if (!(ans == null || (scanDetail = this.mScanDetailCandidate) == null)) {
                onConnectableListener.onConnectable(scanDetail, ans, 0);
            }
            return ans;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void debugLog(String msg) {
        if (DEBUG) {
            this.mLocalLog.log(msg);
        }
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public int getId() {
        return 4;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public String getName() {
        return TAG;
    }
}
