package com.android.server.wifi;

import android.content.Context;
import android.database.ContentObserver;
import android.net.NetworkKey;
import android.net.NetworkScoreManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiNetworkScoreCache;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator;
import com.android.server.wifi.util.ScanResultUtil;
import java.util.ArrayList;
import java.util.List;

public class ScoredNetworkEvaluator implements NetworkEvaluator {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "ScoredNetworkEvaluator";
    private final ContentObserver mContentObserver;
    private final LocalLog mLocalLog;
    private boolean mNetworkRecommendationsEnabled;
    private final NetworkScoreManager mNetworkScoreManager;
    private WifiNetworkScoreCache mScoreCache;
    private final WifiConfigManager mWifiConfigManager;

    class ScoreTracker {
        private static final int EXTERNAL_SCORED_NONE = 0;
        private static final int EXTERNAL_SCORED_SAVED_NETWORK = 1;
        private static final int EXTERNAL_SCORED_UNTRUSTED_NETWORK = 2;
        private int mBestCandidateType = 0;
        private WifiConfiguration mEphemeralConfig;
        private int mHighScore = -128;
        private WifiConfiguration mSavedConfig;
        private ScanResult mScanResultCandidate;

        ScoreTracker() {
        }

        private Integer getNetworkScore(ScanResult scanResult, boolean isCurrentNetwork) {
            if (!ScoredNetworkEvaluator.this.mScoreCache.isScoredNetwork(scanResult)) {
                return null;
            }
            int score = ScoredNetworkEvaluator.this.mScoreCache.getNetworkScore(scanResult, isCurrentNetwork);
            if (ScoredNetworkEvaluator.DEBUG) {
                ScoredNetworkEvaluator.this.mLocalLog.log(WifiNetworkSelector.toScanId(scanResult) + " has score: " + score + " isCurrentNetwork network: " + isCurrentNetwork);
            }
            return Integer.valueOf(score);
        }

        void trackUntrustedCandidate(ScanResult scanResult) {
            Integer score = getNetworkScore(scanResult, false);
            if (score != null && score.intValue() > this.mHighScore) {
                this.mHighScore = score.intValue();
                this.mScanResultCandidate = scanResult;
                this.mBestCandidateType = 2;
                ScoredNetworkEvaluator.this.debugLog(WifiNetworkSelector.toScanId(scanResult) + " becomes the new untrusted candidate.");
            }
        }

        void trackUntrustedCandidate(ScanResult scanResult, WifiConfiguration config, boolean isCurrentNetwork) {
            Integer score = getNetworkScore(scanResult, isCurrentNetwork);
            if (score != null && score.intValue() > this.mHighScore) {
                this.mHighScore = score.intValue();
                this.mScanResultCandidate = scanResult;
                this.mBestCandidateType = 2;
                this.mEphemeralConfig = config;
                ScoredNetworkEvaluator.this.mWifiConfigManager.setNetworkCandidateScanResult(config.networkId, scanResult, 0);
                ScoredNetworkEvaluator.this.debugLog(WifiNetworkSelector.toScanId(scanResult) + " becomes the new untrusted candidate.");
            }
        }

        void trackExternallyScoredCandidate(ScanResult scanResult, WifiConfiguration config, boolean isCurrentNetwork) {
            Integer score = getNetworkScore(scanResult, isCurrentNetwork);
            if (score == null) {
                return;
            }
            if (score.intValue() > this.mHighScore || (this.mBestCandidateType == 2 && score.intValue() == this.mHighScore)) {
                this.mHighScore = score.intValue();
                this.mSavedConfig = config;
                this.mScanResultCandidate = scanResult;
                this.mBestCandidateType = 1;
                ScoredNetworkEvaluator.this.mWifiConfigManager.setNetworkCandidateScanResult(config.networkId, scanResult, 0);
                ScoredNetworkEvaluator.this.debugLog(WifiNetworkSelector.toScanId(scanResult) + " becomes the new externally scored saved network candidate.");
            }
        }

        WifiConfiguration getCandidateConfiguration() {
            int candidateNetworkId = -1;
            switch (this.mBestCandidateType) {
                case 1:
                    candidateNetworkId = this.mSavedConfig.networkId;
                    ScoredNetworkEvaluator.this.mLocalLog.log(String.format("new saved network candidate %s network ID:%d", new Object[]{WifiNetworkSelector.toScanId(this.mScanResultCandidate), Integer.valueOf(candidateNetworkId)}));
                    break;
                case 2:
                    if (this.mEphemeralConfig == null) {
                        this.mEphemeralConfig = ScanResultUtil.createNetworkFromScanResult(this.mScanResultCandidate);
                        this.mEphemeralConfig.ephemeral = true;
                        this.mEphemeralConfig.meteredHint = ScoredNetworkEvaluator.this.mScoreCache.getMeteredHint(this.mScanResultCandidate);
                        NetworkUpdateResult result = ScoredNetworkEvaluator.this.mWifiConfigManager.addOrUpdateNetwork(this.mEphemeralConfig, 1010);
                        if (result.isSuccess()) {
                            if (!ScoredNetworkEvaluator.this.mWifiConfigManager.updateNetworkSelectionStatus(result.getNetworkId(), 0)) {
                                ScoredNetworkEvaluator.this.mLocalLog.log("Failed to make ephemeral network selectable");
                                break;
                            }
                            candidateNetworkId = result.getNetworkId();
                            ScoredNetworkEvaluator.this.mWifiConfigManager.setNetworkCandidateScanResult(candidateNetworkId, this.mScanResultCandidate, 0);
                            ScoredNetworkEvaluator.this.mLocalLog.log(String.format("new ephemeral candidate %s network ID:%d, meteredHint=%b", new Object[]{WifiNetworkSelector.toScanId(this.mScanResultCandidate), Integer.valueOf(candidateNetworkId), Boolean.valueOf(this.mEphemeralConfig.meteredHint)}));
                            break;
                        }
                        ScoredNetworkEvaluator.this.mLocalLog.log("Failed to add ephemeral network");
                        break;
                    }
                    candidateNetworkId = this.mEphemeralConfig.networkId;
                    ScoredNetworkEvaluator.this.mLocalLog.log(String.format("existing ephemeral candidate %s network ID:%d, meteredHint=%b", new Object[]{WifiNetworkSelector.toScanId(this.mScanResultCandidate), Integer.valueOf(candidateNetworkId), Boolean.valueOf(this.mEphemeralConfig.meteredHint)}));
                    break;
                default:
                    ScoredNetworkEvaluator.this.mLocalLog.log("ScoredNetworkEvaluator did not see any good candidates.");
                    break;
            }
            return ScoredNetworkEvaluator.this.mWifiConfigManager.getConfiguredNetwork(candidateNetworkId);
        }
    }

    ScoredNetworkEvaluator(final Context context, Looper looper, final FrameworkFacade frameworkFacade, NetworkScoreManager networkScoreManager, WifiConfigManager wifiConfigManager, LocalLog localLog, WifiNetworkScoreCache wifiNetworkScoreCache) {
        this.mScoreCache = wifiNetworkScoreCache;
        this.mNetworkScoreManager = networkScoreManager;
        this.mWifiConfigManager = wifiConfigManager;
        this.mLocalLog = localLog;
        this.mContentObserver = new ContentObserver(new Handler(looper)) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                ScoredNetworkEvaluator scoredNetworkEvaluator = ScoredNetworkEvaluator.this;
                if (frameworkFacade.getIntegerSetting(context, "network_recommendations_enabled", 0) != 1) {
                    z = false;
                }
                scoredNetworkEvaluator.mNetworkRecommendationsEnabled = z;
            }
        };
        frameworkFacade.registerContentObserver(context, Global.getUriFor("network_recommendations_enabled"), false, this.mContentObserver);
        this.mContentObserver.onChange(false);
        this.mLocalLog.log("ScoredNetworkEvaluator constructed. mNetworkRecommendationsEnabled: " + this.mNetworkRecommendationsEnabled);
    }

    public void update(List<ScanDetail> scanDetails) {
        if (this.mNetworkRecommendationsEnabled) {
            updateNetworkScoreCache(scanDetails);
        }
    }

    private void updateNetworkScoreCache(List<ScanDetail> scanDetails) {
        ArrayList<NetworkKey> unscoredNetworks = new ArrayList();
        for (int i = 0; i < scanDetails.size(); i++) {
            NetworkKey networkKey = NetworkKey.createFromScanResult(((ScanDetail) scanDetails.get(i)).getScanResult());
            if (networkKey != null && this.mScoreCache.getScoredNetwork(networkKey) == null) {
                unscoredNetworks.add(networkKey);
            }
        }
        if (!unscoredNetworks.isEmpty()) {
            this.mNetworkScoreManager.requestScores((NetworkKey[]) unscoredNetworks.toArray(new NetworkKey[unscoredNetworks.size()]));
        }
    }

    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, List<Pair<ScanDetail, WifiConfiguration>> connectableNetworks) {
        if (this.mNetworkRecommendationsEnabled) {
            ScoreTracker scoreTracker = new ScoreTracker();
            for (int i = 0; i < scanDetails.size(); i++) {
                ScanDetail scanDetail = (ScanDetail) scanDetails.get(i);
                ScanResult scanResult = scanDetail.getScanResult();
                if (scanResult != null) {
                    if (this.mWifiConfigManager.wasEphemeralNetworkDeleted(ScanResultUtil.createQuotedSSID(scanResult.SSID))) {
                        debugLog("Ignoring disabled ephemeral SSID: " + scanResult.SSID);
                    } else {
                        WifiConfiguration configuredNetwork = this.mWifiConfigManager.getSavedNetworkForScanDetailAndCache(scanDetail);
                        boolean untrustedScanResult = configuredNetwork != null ? configuredNetwork.ephemeral : true;
                        if (untrustedNetworkAllowed || !untrustedScanResult) {
                            if (configuredNetwork == null) {
                                if (ScanResultUtil.isScanResultForOpenNetwork(scanResult)) {
                                    scoreTracker.trackUntrustedCandidate(scanResult);
                                }
                            } else if (configuredNetwork.ephemeral || (configuredNetwork.useExternalScores ^ 1) == 0) {
                                if (!configuredNetwork.getNetworkSelectionStatus().isNetworkEnabled()) {
                                    debugLog("Ignoring disabled SSID: " + configuredNetwork.SSID);
                                } else if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(configuredNetwork, false)) {
                                    Log.w(TAG, "evaluateNetworks: MDM deny connect to restricted network!");
                                } else {
                                    boolean isCurrentNetwork;
                                    if (currentNetwork == null || currentNetwork.networkId != configuredNetwork.networkId) {
                                        isCurrentNetwork = false;
                                    } else {
                                        isCurrentNetwork = TextUtils.equals(currentBssid, scanResult.BSSID);
                                    }
                                    if (configuredNetwork.ephemeral) {
                                        scoreTracker.trackUntrustedCandidate(scanResult, configuredNetwork, isCurrentNetwork);
                                    } else {
                                        scoreTracker.trackExternallyScoredCandidate(scanResult, configuredNetwork, isCurrentNetwork);
                                    }
                                    if (connectableNetworks != null) {
                                        connectableNetworks.add(Pair.create(scanDetail, configuredNetwork));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return scoreTracker.getCandidateConfiguration();
        }
        this.mLocalLog.log("Skipping evaluateNetworks; Network recommendations disabled.");
        return null;
    }

    private void debugLog(String msg) {
        if (DEBUG) {
            this.mLocalLog.log(msg);
        }
    }

    public String getName() {
        return TAG;
    }
}
