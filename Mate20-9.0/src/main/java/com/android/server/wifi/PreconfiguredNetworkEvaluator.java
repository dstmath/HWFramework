package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.WifiNetworkSelector;
import com.android.server.wifi.util.ScanResultUtil;
import java.util.ArrayList;
import java.util.List;

public class PreconfiguredNetworkEvaluator implements WifiNetworkSelector.NetworkEvaluator {
    private static final String NAME = "PreconfiguredNetworkEvaluator";
    private Context mContext;
    private WifiConfigManager mWifiConfigManager;
    private WifiEapUIManager wifiEapUIManager;

    public PreconfiguredNetworkEvaluator(Context context, WifiConfigManager wifiConfigManager) {
        this.mContext = context;
        this.mWifiConfigManager = wifiConfigManager;
        this.wifiEapUIManager = new WifiEapUIManager(context);
    }

    public String getName() {
        return NAME;
    }

    public void update(List<ScanDetail> list) {
    }

    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, List<Pair<ScanDetail, WifiConfiguration>> list) {
        WifiConfiguration candidate = null;
        ScanDetail candidateScanDetail = null;
        List<WifiConfiguration> matchList = new ArrayList<>();
        int list_size = scanDetails.size();
        for (int i = 0; i < list_size; i++) {
            ScanDetail scanDetail = scanDetails.get(i);
            if (ScanResultUtil.isScanResultForEapNetwork(scanDetail.getScanResult())) {
                PreconfiguredNetwork matchResult = PreconfiguredNetworkManager.getInstance().match(scanDetail.getSSID());
                if (matchResult != null) {
                    WifiConfiguration wifiConfiguration = this.mWifiConfigManager.getConfiguredNetworkForScanDetail(scanDetail);
                    if (wifiConfiguration == null) {
                        Log.d(NAME, "wifiConfiguration is null.");
                        WifiConfiguration wifiConfig = new WifiConfiguration();
                        wifiConfig.allowedAuthAlgorithms.clear();
                        wifiConfig.allowedGroupCiphers.clear();
                        wifiConfig.allowedKeyManagement.clear();
                        wifiConfig.allowedPairwiseCiphers.clear();
                        wifiConfig.allowedPairwiseCiphers.clear();
                        wifiConfig.allowedProtocols.clear();
                        wifiConfig.SSID = "\"" + matchResult.getSsid() + "\"";
                        wifiConfig.allowedKeyManagement.set(2);
                        wifiConfig.allowedKeyManagement.set(3);
                        wifiConfig.enterpriseConfig = new WifiEnterpriseConfig();
                        wifiConfig.enterpriseConfig.setEapMethod(matchResult.getEapMethod());
                        NetworkUpdateResult result = this.mWifiConfigManager.addOrUpdateNetwork(wifiConfig, 1010);
                        if (!result.isSuccess()) {
                            Log.d(NAME, "Failed to add preconfigured network.");
                        } else {
                            this.mWifiConfigManager.enableNetwork(result.getNetworkId(), false, 1010);
                            wifiConfiguration = this.mWifiConfigManager.getConfiguredNetwork(result.getNetworkId());
                        }
                    }
                    this.mWifiConfigManager.setNetworkCandidateScanResult(wifiConfiguration.networkId, scanDetail.getScanResult(), 0);
                    this.mWifiConfigManager.updateScanDetailForNetwork(wifiConfiguration.networkId, scanDetail);
                    if (!wifiConfiguration.getNetworkSelectionStatus().isNetworkEnabled()) {
                        Log.d(NAME, "network is disabled : " + wifiConfiguration);
                    } else {
                        Log.d(NAME, "network is enabled : " + wifiConfiguration);
                        matchList.add(wifiConfiguration);
                        if (candidate == null) {
                            candidate = wifiConfiguration;
                            candidateScanDetail = scanDetail;
                        } else if (scanDetail.getScanResult().level > candidateScanDetail.getScanResult().level) {
                            candidate = wifiConfiguration;
                            candidateScanDetail = scanDetail;
                        }
                    }
                }
            }
        }
        List<ScanDetail> list2 = scanDetails;
        if (candidate == null) {
            Log.d(NAME, "Cannot find candidate.");
            return null;
        } else if (this.mWifiConfigManager.isSimPresent()) {
            return candidate;
        } else {
            int list_size2 = matchList.size();
            for (int i2 = 0; i2 < list_size2; i2++) {
                this.mWifiConfigManager.disableNetwork(matchList.get(i2).networkId, 1000);
            }
            return null;
        }
    }
}
