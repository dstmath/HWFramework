package com.android.server.wifi.hotspot2;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.CarrierNetworkConfig;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.NetworkUpdateResult;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNetworkSelector;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.TelephonyUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PasspointNetworkEvaluator implements WifiNetworkSelector.NetworkEvaluator {
    private static final String NAME = "PasspointNetworkEvaluator";
    private final CarrierNetworkConfig mCarrierNetworkConfig;
    private final LocalLog mLocalLog;
    private final PasspointManager mPasspointManager;
    private SubscriptionManager mSubscriptionManager;
    private TelephonyManager mTelephonyManager;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiInjector mWifiInjector;

    /* access modifiers changed from: private */
    public class PasspointNetworkCandidate {
        PasspointMatch mMatchStatus;
        PasspointProvider mProvider;
        ScanDetail mScanDetail;

        PasspointNetworkCandidate(PasspointProvider provider, PasspointMatch matchStatus, ScanDetail scanDetail) {
            this.mProvider = provider;
            this.mMatchStatus = matchStatus;
            this.mScanDetail = scanDetail;
        }
    }

    public PasspointNetworkEvaluator(PasspointManager passpointManager, WifiConfigManager wifiConfigManager, LocalLog localLog, CarrierNetworkConfig carrierNetworkConfig, WifiInjector wifiInjector, SubscriptionManager subscriptionManager) {
        this.mPasspointManager = passpointManager;
        this.mWifiConfigManager = wifiConfigManager;
        this.mLocalLog = localLog;
        this.mCarrierNetworkConfig = carrierNetworkConfig;
        this.mWifiInjector = wifiInjector;
        this.mSubscriptionManager = subscriptionManager;
    }

    private TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = this.mWifiInjector.makeTelephonyManager();
        }
        return this.mTelephonyManager;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public int getId() {
        return 2;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public String getName() {
        return NAME;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public void update(List<ScanDetail> list) {
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, WifiNetworkSelector.NetworkEvaluator.OnConnectableListener onConnectableListener) {
        Pair<PasspointProvider, PasspointMatch> bestProvider;
        this.mPasspointManager.sweepCache();
        List<ScanDetail> filteredScanDetails = (List) scanDetails.stream().filter($$Lambda$PasspointNetworkEvaluator$GeomGkeNP2MEelBL59RV_0T1M8.INSTANCE).filter(new Predicate() {
            /* class com.android.server.wifi.hotspot2.$$Lambda$PasspointNetworkEvaluator$sa28nwdP8mGfetynNLsyMBO7E8 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return PasspointNetworkEvaluator.this.lambda$evaluateNetworks$1$PasspointNetworkEvaluator((ScanDetail) obj);
            }
        }).collect(Collectors.toList());
        createEphemeralProfileForMatchingAp(filteredScanDetails);
        List<PasspointNetworkCandidate> candidateList = new ArrayList<>();
        for (ScanDetail scanDetail : filteredScanDetails) {
            ScanResult scanResult = scanDetail.getScanResult();
            if (scanDetail.getNetworkDetail() == null && (bestProvider = this.mPasspointManager.matchProvider(scanResult)) != null) {
                if (!((PasspointProvider) bestProvider.first).isSimCredential() || TelephonyUtil.isSimPresent(this.mSubscriptionManager)) {
                    candidateList.add(new PasspointNetworkCandidate((PasspointProvider) bestProvider.first, (PasspointMatch) bestProvider.second, scanDetail));
                }
            }
        }
        if (candidateList.isEmpty()) {
            localLog("No suitable Passpoint network found");
            return null;
        }
        PasspointNetworkCandidate bestNetwork = findBestNetwork(candidateList, currentNetwork == null ? null : currentNetwork.SSID);
        if (bestNetwork == null) {
            return null;
        }
        if (currentNetwork == null || !TextUtils.equals(currentNetwork.SSID, ScanResultUtil.createQuotedSSID(bestNetwork.mScanDetail.getSSID())) || HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(currentNetwork, false)) {
            WifiConfiguration config = createWifiConfigForProvider(bestNetwork);
            if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config, false)) {
                Log.w(NAME, "evaluateNetworks: MDM deny connect to restricted network!");
                return null;
            }
            if (config != null) {
                onConnectableListener.onConnectable(bestNetwork.mScanDetail, config, 0);
                localLog("Passpoint network to connect to: " + StringUtilEx.safeDisplaySsid(config.SSID));
            }
            return config;
        }
        localLog("Staying with current Passpoint network " + StringUtilEx.safeDisplaySsid(currentNetwork.SSID));
        this.mWifiConfigManager.setNetworkCandidateScanResult(currentNetwork.networkId, bestNetwork.mScanDetail.getScanResult(), 0);
        this.mWifiConfigManager.updateScanDetailForNetwork(currentNetwork.networkId, bestNetwork.mScanDetail);
        onConnectableListener.onConnectable(bestNetwork.mScanDetail, currentNetwork, 0);
        return currentNetwork;
    }

    public /* synthetic */ boolean lambda$evaluateNetworks$1$PasspointNetworkEvaluator(ScanDetail s) {
        if (!this.mWifiConfigManager.wasEphemeralNetworkDeleted(ScanResultUtil.createQuotedSSID(s.getScanResult().SSID))) {
            return true;
        }
        LocalLog localLog = this.mLocalLog;
        localLog.log("Ignoring disabled the SSID of Passpoint AP: " + WifiNetworkSelector.toScanId(s.getScanResult()));
        return false;
    }

    private void createEphemeralProfileForMatchingAp(List<ScanDetail> filteredScanDetails) {
        PasspointConfiguration carrierConfig;
        TelephonyManager telephonyManager = getTelephonyManager();
        if (telephonyManager == null || TelephonyUtil.getCarrierType(telephonyManager) != 0 || !this.mCarrierNetworkConfig.isCarrierEncryptionInfoAvailable()) {
            return;
        }
        if (!this.mPasspointManager.hasCarrierProvider(telephonyManager.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId()).getSimOperator())) {
            int eapMethod = this.mPasspointManager.findEapMethodFromNAIRealmMatchedWithCarrier(filteredScanDetails);
            if (Utils.isCarrierEapMethod(eapMethod) && (carrierConfig = this.mPasspointManager.createEphemeralPasspointConfigForCarrier(eapMethod)) != null) {
                this.mPasspointManager.installEphemeralPasspointConfigForCarrier(carrierConfig);
            }
        }
    }

    private WifiConfiguration createWifiConfigForProvider(PasspointNetworkCandidate networkInfo) {
        WifiConfiguration config = networkInfo.mProvider.getWifiConfig();
        config.SSID = ScanResultUtil.createQuotedSSID(networkInfo.mScanDetail.getSSID());
        if (networkInfo.mMatchStatus == PasspointMatch.HomeProvider) {
            config.isHomeProviderNetwork = true;
        }
        WifiConfiguration existingNetwork = this.mWifiConfigManager.getConfiguredNetwork(config.configKey());
        if (existingNetwork == null) {
            NetworkUpdateResult result = this.mWifiConfigManager.addOrUpdateNetwork(config, 1010);
            if (!result.isSuccess()) {
                localLog("Failed to add passpoint network");
                return null;
            }
            this.mWifiConfigManager.enableNetwork(result.getNetworkId(), false, 1010);
            this.mWifiConfigManager.setNetworkCandidateScanResult(result.getNetworkId(), networkInfo.mScanDetail.getScanResult(), 0);
            this.mWifiConfigManager.updateScanDetailForNetwork(result.getNetworkId(), networkInfo.mScanDetail);
            return this.mWifiConfigManager.getConfiguredNetwork(result.getNetworkId());
        } else if (existingNetwork.getNetworkSelectionStatus().isNetworkEnabled() || this.mWifiConfigManager.tryEnableNetwork(existingNetwork.networkId)) {
            return existingNetwork;
        } else {
            localLog("Current configuration for the Passpoint AP " + StringUtilEx.safeDisplaySsid(config.SSID) + " is disabled, skip this candidate");
            return null;
        }
    }

    private PasspointNetworkCandidate findBestNetwork(List<PasspointNetworkCandidate> networkList, String currentNetworkSsid) {
        PasspointNetworkCandidate bestCandidate = null;
        int bestScore = WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK;
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
