package com.android.server.wifi.hotspot2;

import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.NetworkUpdateResult;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator;
import com.android.server.wifi.util.ScanResultUtil;
import java.util.ArrayList;
import java.util.List;

public class PasspointNetworkEvaluator implements NetworkEvaluator {
    private static final String NAME = "PasspointNetworkEvaluator";
    private final LocalLog mLocalLog;
    private final PasspointManager mPasspointManager;
    private final WifiConfigManager mWifiConfigManager;

    private class PasspointNetworkCandidate {
        PasspointMatch mMatchStatus;
        PasspointProvider mProvider;
        ScanDetail mScanDetail;

        PasspointNetworkCandidate(PasspointProvider provider, PasspointMatch matchStatus, ScanDetail scanDetail) {
            this.mProvider = provider;
            this.mMatchStatus = matchStatus;
            this.mScanDetail = scanDetail;
        }
    }

    public PasspointNetworkEvaluator(PasspointManager passpointManager, WifiConfigManager wifiConfigManager, LocalLog localLog) {
        this.mPasspointManager = passpointManager;
        this.mWifiConfigManager = wifiConfigManager;
        this.mLocalLog = localLog;
    }

    public String getName() {
        return NAME;
    }

    public void update(List<ScanDetail> list) {
    }

    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, List<Pair<ScanDetail, WifiConfiguration>> connectableNetworks) {
        this.mPasspointManager.sweepCache();
        List<PasspointNetworkCandidate> candidateList = new ArrayList();
        for (ScanDetail scanDetail : scanDetails) {
            if (scanDetail.getNetworkDetail() == null || (scanDetail.getNetworkDetail().isInterworking() ^ 1) == 0) {
                Pair<PasspointProvider, PasspointMatch> bestProvider = this.mPasspointManager.matchProvider(scanDetail.getScanResult());
                if (bestProvider != null) {
                    candidateList.add(new PasspointNetworkCandidate((PasspointProvider) bestProvider.first, (PasspointMatch) bestProvider.second, scanDetail));
                }
            }
        }
        if (candidateList.isEmpty()) {
            localLog("No suitable Passpoint network found");
            return null;
        }
        PasspointNetworkCandidate bestNetwork = findBestNetwork(candidateList, currentNetwork == null ? null : currentNetwork.SSID);
        if (currentNetwork == null || !TextUtils.equals(currentNetwork.SSID, ScanResultUtil.createQuotedSSID(bestNetwork.mScanDetail.getSSID())) || (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(currentNetwork, false) ^ 1) == 0) {
            WifiConfiguration config = createWifiConfigForProvider(bestNetwork);
            if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config, false)) {
                Log.w(NAME, "evaluateNetworks: MDM deny connect to restricted network!");
                return null;
            }
            connectableNetworks.add(Pair.create(bestNetwork.mScanDetail, config));
            localLog("Passpoint network to connect to: " + config.SSID);
            return config;
        }
        localLog("Staying with current Passpoint network " + currentNetwork.SSID);
        connectableNetworks.add(Pair.create(bestNetwork.mScanDetail, currentNetwork));
        return currentNetwork;
    }

    private WifiConfiguration createWifiConfigForProvider(PasspointNetworkCandidate networkInfo) {
        WifiConfiguration config = networkInfo.mProvider.getWifiConfig();
        config.SSID = ScanResultUtil.createQuotedSSID(networkInfo.mScanDetail.getSSID());
        if (networkInfo.mMatchStatus == PasspointMatch.HomeProvider) {
            config.isHomeProviderNetwork = true;
        }
        NetworkUpdateResult result = this.mWifiConfigManager.addOrUpdateNetwork(config, 1010);
        if (result.isSuccess()) {
            this.mWifiConfigManager.enableNetwork(result.getNetworkId(), false, 1010);
            this.mWifiConfigManager.setNetworkCandidateScanResult(result.getNetworkId(), networkInfo.mScanDetail.getScanResult(), 0);
            this.mWifiConfigManager.updateScanDetailForNetwork(result.getNetworkId(), networkInfo.mScanDetail);
            return this.mWifiConfigManager.getConfiguredNetwork(result.getNetworkId());
        }
        localLog("Failed to add passpoint network");
        return null;
    }

    private PasspointNetworkCandidate findBestNetwork(List<PasspointNetworkCandidate> networkList, String currentNetworkSsid) {
        PasspointNetworkCandidate bestCandidate = null;
        int bestScore = Integer.MIN_VALUE;
        for (PasspointNetworkCandidate candidate : networkList) {
            ScanDetail scanDetail = candidate.mScanDetail;
            PasspointMatch match = candidate.mMatchStatus;
            int score = PasspointNetworkScore.calculateScore(match == PasspointMatch.HomeProvider, scanDetail, this.mPasspointManager.getANQPElements(scanDetail.getScanResult()), TextUtils.equals(currentNetworkSsid, ScanResultUtil.createQuotedSSID(scanDetail.getSSID())));
            if (score > bestScore) {
                bestCandidate = candidate;
                bestScore = score;
            }
        }
        if (bestCandidate != null) {
            localLog("Best Passpoint network " + bestCandidate.mScanDetail.getSSID() + " provided by " + bestCandidate.mProvider.getConfig().getHomeSp().getFqdn());
        }
        return bestCandidate;
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
    }
}
