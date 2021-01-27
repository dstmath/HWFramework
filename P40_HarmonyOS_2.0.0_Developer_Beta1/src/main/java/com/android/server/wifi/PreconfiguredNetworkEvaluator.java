package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.WifiNetworkSelector;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.wifi2.HwWifi2Manager;
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

    public int getId() {
        return 5;
    }

    public String getName() {
        return NAME;
    }

    public void update(List<ScanDetail> list) {
    }

    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, WifiNetworkSelector.NetworkEvaluator.OnConnectableListener onConnectableListener) {
        int list_size;
        WifiConfiguration candidate = null;
        ScanDetail candidateScanDetail = null;
        List<WifiConfiguration> matchList = new ArrayList<>();
        int i = 0;
        int list_size2 = scanDetails.size();
        while (i < list_size2) {
            ScanDetail scanDetail = scanDetails.get(i);
            if (!ScanResultUtil.isScanResultForEapNetwork(scanDetail.getScanResult())) {
                list_size = list_size2;
            } else {
                PreconfiguredNetwork matchResult = PreconfiguredNetworkManager.getInstance().match(scanDetail.getSSID());
                if (matchResult != null) {
                    WifiConfiguration wifiConfiguration = this.mWifiConfigManager.getConfiguredNetworkForScanDetail(scanDetail);
                    if (wifiConfiguration == null) {
                        HwHiLog.w(NAME, false, "wifiConfiguration is null.", new Object[0]);
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
                        NetworkUpdateResult result = this.mWifiConfigManager.addOrUpdateNetwork(wifiConfig, (int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM);
                        if (!result.isSuccess()) {
                            HwHiLog.w(NAME, false, "Failed to add preconfigured network.", new Object[0]);
                            list_size = list_size2;
                        } else {
                            list_size = list_size2;
                            this.mWifiConfigManager.enableNetwork(result.getNetworkId(), false, (int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM);
                            wifiConfiguration = this.mWifiConfigManager.getConfiguredNetwork(result.getNetworkId());
                        }
                    } else {
                        list_size = list_size2;
                    }
                    this.mWifiConfigManager.setNetworkCandidateScanResult(wifiConfiguration.networkId, scanDetail.getScanResult(), 0);
                    this.mWifiConfigManager.updateScanDetailForNetwork(wifiConfiguration.networkId, scanDetail);
                    if (!wifiConfiguration.getNetworkSelectionStatus().isNetworkEnabled()) {
                        HwHiLog.i(NAME, false, "network is disabled : " + wifiConfiguration, new Object[0]);
                    } else {
                        HwHiLog.i(NAME, false, "network is enabled : " + wifiConfiguration, new Object[0]);
                        matchList.add(wifiConfiguration);
                        if (candidate == null) {
                            candidate = wifiConfiguration;
                            candidateScanDetail = scanDetail;
                        } else if (scanDetail.getScanResult().level > candidateScanDetail.getScanResult().level) {
                            candidate = wifiConfiguration;
                            candidateScanDetail = scanDetail;
                        }
                    }
                } else {
                    list_size = list_size2;
                }
            }
            i++;
            list_size2 = list_size;
        }
        if (candidate != null) {
            WifiInjector wifiInject = WifiInjector.getInstance();
            if (wifiInject == null || TelephonyUtil.isSimPresent(wifiInject.mSubscriptionManager)) {
                return candidate;
            }
            int list_size3 = matchList.size();
            for (int i2 = 0; i2 < list_size3; i2++) {
                this.mWifiConfigManager.disableNetwork(matchList.get(i2).networkId, 1000);
            }
            return null;
        }
        HwHiLog.w(NAME, false, "Cannot find candidate.", new Object[0]);
        return null;
    }
}
