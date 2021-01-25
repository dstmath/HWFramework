package com.huawei.wifi2;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HwWifi2NetworkSelector {
    private static final int FILTER_FT_ONLY_NETWORK_BIT = 16;
    private static final int FILTER_NO_INTERNET_NETWORK_BIT = 4;
    private static final int FILTER_POOR_RSSI_BIT = 1;
    private static final int FILTER_PORTAL_NETWORK_BIT = 2;
    private static final int FILTER_SIM_NETWORK_BIT = 32;
    private static final int FILTER_WAPI_NETWORK_BIT = 8;
    private static final String HUAWEI_GUEST = "\"Huawei-Guest\"NONE";
    private static String TAG = "HwWifi2NetworkSelecter";
    private Context mContext;
    private int mFiltedNetworkMask = 0;
    private WifiConfiguration mHasInternetCandidate = null;
    private ScanResult mHasNetworkScanResultCandidate = null;
    private boolean mIsInternetNeeded = true;
    private WifiConfiguration mNoInetNetworkCandidate = null;
    private ScanResult mNoInetScanResultCandidate = null;
    private int mPoorSignalThres = 3;
    private WifiManager mWifiManager = null;

    public HwWifi2NetworkSelector(Context context) {
        this.mContext = context;
    }

    public void handleBootCompleted() {
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    public void setNetworkSelectorePara(int poorSignalThres, boolean isInternetNeeded) {
        this.mPoorSignalThres = poorSignalThres;
        this.mIsInternetNeeded = isInternetNeeded;
    }

    public int getFiltedNetworkMask() {
        return this.mFiltedNetworkMask;
    }

    public WifiConfiguration selectNetwork(ScanResult[] scanResults) {
        ScanResult scanResult;
        this.mHasInternetCandidate = null;
        this.mHasNetworkScanResultCandidate = null;
        this.mNoInetNetworkCandidate = null;
        this.mNoInetScanResultCandidate = null;
        this.mFiltedNetworkMask = 0;
        if (scanResults == null || scanResults.length == 0) {
            HwHiLog.e(TAG, false, "selectNetwork scanResults is empty", new Object[0]);
            return null;
        }
        List<WifiConfiguration> cleanNetworks = filterWifiConfiguration(this.mWifiManager.getPrivilegedConfiguredNetworks(), scanResults);
        if (cleanNetworks.isEmpty()) {
            HwHiLog.e(TAG, false, "selectNetwork normalScanResult is empty", new Object[0]);
            return null;
        }
        List<WifiConfiguration> hasInternetNetworks = new ArrayList<>();
        List<WifiConfiguration> noInternetNetworks = new ArrayList<>();
        classifyNetworksByInternet(cleanNetworks, hasInternetNetworks, noInternetNetworks);
        if (noInternetNetworks.size() != 0) {
            this.mFiltedNetworkMask |= 4;
        }
        List<ScanResult> normalScanResults = new ArrayList<>();
        List<ScanResult> dualApScanResults = new ArrayList<>();
        List<ScanResult> blacklistScanResults = new ArrayList<>();
        filterAndClassifyScanResult(scanResults, normalScanResults, dualApScanResults, blacklistScanResults, hasInternetNetworks);
        if (!normalScanResults.isEmpty() || !dualApScanResults.isEmpty() || !blacklistScanResults.isEmpty()) {
            selectNetworkHasInternetAllScanResult(hasInternetNetworks, normalScanResults, dualApScanResults, blacklistScanResults);
            if (!(this.mHasInternetCandidate == null || (scanResult = this.mHasNetworkScanResultCandidate) == null || scanResult.BSSID == null)) {
                this.mHasInternetCandidate.getNetworkSelectionStatus().setNetworkSelectionBSSID(this.mHasNetworkScanResultCandidate.BSSID);
                this.mHasInternetCandidate.getNetworkSelectionStatus().setCandidate(this.mHasNetworkScanResultCandidate);
                return this.mHasInternetCandidate;
            }
        } else {
            HwHiLog.e(TAG, false, "selectNetworkwithInternet valid scanResult is null", new Object[0]);
        }
        if (!this.mIsInternetNeeded) {
            return selectNetworkWithoutInternet(scanResults, noInternetNetworks);
        }
        HwHiLog.i(TAG, false, "find not valid network for wifi2", new Object[0]);
        return null;
    }

    private WifiConfiguration selectNetworkWithoutInternet(ScanResult[] scanResults, List<WifiConfiguration> noInternetNetworks) {
        ScanResult scanResult;
        List<ScanResult> normalScanResultsNoInternet = new ArrayList<>();
        List<ScanResult> dualApScanResultsNoInternet = new ArrayList<>();
        List<ScanResult> blacklistScanResultsNoInternet = new ArrayList<>();
        filterAndClassifyScanResult(scanResults, normalScanResultsNoInternet, dualApScanResultsNoInternet, blacklistScanResultsNoInternet, noInternetNetworks);
        selectNetworkNoInternetAllScanResult(noInternetNetworks, normalScanResultsNoInternet, dualApScanResultsNoInternet, blacklistScanResultsNoInternet);
        if (this.mNoInetNetworkCandidate == null || (scanResult = this.mNoInetScanResultCandidate) == null || scanResult.BSSID == null) {
            HwHiLog.i(TAG, false, "find not valid network for wifi2", new Object[0]);
            return null;
        }
        HwHiLog.i(TAG, false, "Internet is not needed, choose no NoInetNetworkCandidate", new Object[0]);
        this.mNoInetNetworkCandidate.getNetworkSelectionStatus().setNetworkSelectionBSSID(this.mNoInetScanResultCandidate.BSSID);
        this.mNoInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(this.mNoInetScanResultCandidate);
        return this.mNoInetNetworkCandidate;
    }

    private void selectNetworkHasInternetAllScanResult(List<WifiConfiguration> networks, List<ScanResult> normalScanResults, List<ScanResult> dualApScanResults, List<ScanResult> blacklistScanResults) {
        selectNetworkHasInternetSpecScanResult(normalScanResults, networks);
        if (this.mHasInternetCandidate == null) {
            selectNetworkHasInternetSpecScanResult(dualApScanResults, networks);
            if (this.mHasInternetCandidate == null) {
                for (ScanResult scanResult : blacklistScanResults) {
                    WifiConfiguration network = NetworkTypeUtil.getConfiguredNetworkForScanResult(scanResult, networks);
                    if (network != null) {
                        selectNetworkBlackListHasInternet(network, scanResult);
                    }
                }
                if (this.mHasInternetCandidate != null) {
                    HwHiLog.i(TAG, false, "selectNetworkHasInternetAllScanResult select from blacklistScanResults", new Object[0]);
                    return;
                }
                return;
            }
            HwHiLog.i(TAG, false, "selectNetworkHasInternetAllScanResult select from dualApScanResults", new Object[0]);
            return;
        }
        HwHiLog.i(TAG, false, "selectNetworkHasInternetAllScanResult select from normalScanResults", new Object[0]);
    }

    private void selectNetworkHasInternetSpecScanResult(List<ScanResult> scanResults, List<WifiConfiguration> networks) {
        for (ScanResult scanResult : scanResults) {
            WifiConfiguration network = NetworkTypeUtil.getConfiguredNetworkForScanResult(scanResult, networks);
            if (network != null) {
                selectNetworkHasInternetSpecConfig(network, scanResult);
            }
        }
    }

    private void selectNetworkHasInternetSpecConfig(WifiConfiguration config, ScanResult scanResult) {
        if (hasInternet(config) || maybeHasInternet(config)) {
            WifiConfiguration wifiConfiguration = this.mHasInternetCandidate;
            if (wifiConfiguration == null) {
                this.mHasInternetCandidate = config;
                this.mHasNetworkScanResultCandidate = scanResult;
            } else if (!maybeHasInternet(wifiConfiguration) && maybeHasInternet(config)) {
            } else {
                if (maybeHasInternet(this.mHasInternetCandidate) && !maybeHasInternet(config)) {
                    this.mHasInternetCandidate = config;
                    this.mHasNetworkScanResultCandidate = scanResult;
                } else if (rssiStronger(scanResult, this.mHasNetworkScanResultCandidate)) {
                    this.mHasInternetCandidate = config;
                    this.mHasNetworkScanResultCandidate = scanResult;
                }
            }
        }
    }

    private void selectNetworkBlackListHasInternet(WifiConfiguration config, ScanResult scanResult) {
        Map<String, Long> wifi2BssidBlacklist = HwWifi2Injector.getInstance().getWifiConnectivityManager().getWifi2BssidBlacklist();
        if (this.mHasInternetCandidate == null) {
            this.mHasInternetCandidate = config;
            this.mHasNetworkScanResultCandidate = scanResult;
            return;
        }
        Long candidateToBlackListTime = wifi2BssidBlacklist.get(this.mHasNetworkScanResultCandidate.BSSID);
        Long scanResultToBlackListTime = wifi2BssidBlacklist.get(scanResult.BSSID);
        if (candidateToBlackListTime != null && scanResultToBlackListTime != null && candidateToBlackListTime.longValue() > scanResultToBlackListTime.longValue()) {
            this.mHasInternetCandidate = config;
            this.mHasNetworkScanResultCandidate = scanResult;
        }
    }

    private void selectNetworkBlackListNoInternet(WifiConfiguration config, ScanResult scanResult) {
        Map<String, Long> wifi2BssidBlacklist = HwWifi2Injector.getInstance().getWifiConnectivityManager().getWifi2BssidBlacklist();
        if (this.mNoInetNetworkCandidate == null) {
            this.mNoInetNetworkCandidate = config;
            this.mNoInetScanResultCandidate = scanResult;
            return;
        }
        Long candidateToBlackListTime = wifi2BssidBlacklist.get(this.mNoInetScanResultCandidate.BSSID);
        Long scanResultToBlackListTime = wifi2BssidBlacklist.get(scanResult.BSSID);
        if (candidateToBlackListTime != null && scanResultToBlackListTime != null && candidateToBlackListTime.longValue() > scanResultToBlackListTime.longValue()) {
            this.mNoInetNetworkCandidate = config;
            this.mNoInetScanResultCandidate = scanResult;
        }
    }

    private void selectNetworkNoInternetAllScanResult(List<WifiConfiguration> networks, List<ScanResult> normalScanResult, List<ScanResult> dualApScanResults, List<ScanResult> blacklistScanResults) {
        List<ScanResult> noBlacklistScanResults = new ArrayList<>();
        noBlacklistScanResults.addAll(normalScanResult);
        noBlacklistScanResults.addAll(dualApScanResults);
        for (ScanResult scanResult : noBlacklistScanResults) {
            WifiConfiguration network = NetworkTypeUtil.getConfiguredNetworkForScanResult(scanResult, networks);
            if (network != null) {
                selectNetworkNoInternet(network, scanResult);
            }
        }
        if (this.mNoInetNetworkCandidate != null) {
            HwHiLog.i(TAG, false, "selectNetworkNoInternetAllScanResult select from noBlacklistScanResults", new Object[0]);
            return;
        }
        for (ScanResult scanResult2 : blacklistScanResults) {
            WifiConfiguration network2 = NetworkTypeUtil.getConfiguredNetworkForScanResult(scanResult2, networks);
            if (network2 != null) {
                selectNetworkBlackListNoInternet(network2, scanResult2);
            }
        }
        if (this.mNoInetNetworkCandidate != null) {
            HwHiLog.i(TAG, false, "selectNetworkNoInternetAllScanResult select from BlacklistScanResults", new Object[0]);
        }
    }

    private void selectNetworkNoInternet(WifiConfiguration config, ScanResult scanResult) {
        if (!config.noInternetAccess) {
            return;
        }
        if (this.mNoInetNetworkCandidate == null) {
            HwHiLog.d(TAG, false, "selectNetworkNoInternet, no internet network = %{public}s, backup it if no other better one.", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
            this.mNoInetNetworkCandidate = config;
            this.mNoInetScanResultCandidate = scanResult;
            this.mNoInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        } else if (rssiStronger(scanResult, this.mNoInetScanResultCandidate)) {
            this.mNoInetNetworkCandidate = config;
            this.mNoInetScanResultCandidate = scanResult;
        }
    }

    private boolean hasInternet(WifiConfiguration config) {
        return config != null && !config.noInternetAccess && !config.portalNetwork && WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
    }

    private boolean maybeHasInternet(WifiConfiguration config) {
        return !WifiProCommonUtils.isOpenType(config) && !config.noInternetAccess && !config.portalNetwork && WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 103);
    }

    private boolean rssiStronger(ScanResult newObj, ScanResult oldObj) {
        if (newObj == null || oldObj == null) {
            return false;
        }
        int currentScore = WifiProCommonUtils.calculateScore(oldObj);
        int newScore = WifiProCommonUtils.calculateScore(newObj);
        if (newScore > currentScore) {
            return true;
        }
        if (newScore != currentScore || newObj.level <= oldObj.level) {
            return false;
        }
        return true;
    }

    private void filterAndClassifyScanResult(ScanResult[] scanResults, List<ScanResult> normalScanResults, List<ScanResult> dualApScanResults, List<ScanResult> blacklistScanResults, List<WifiConfiguration> networks) {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        for (ScanResult scanResult : scanResults) {
            if (!TextUtils.isEmpty(scanResult.SSID)) {
                if (TextUtils.isEmpty(scanResult.BSSID)) {
                    HwHiLog.e(TAG, false, "%{public}s is filter for null bssid", new Object[]{StringUtilEx.safeDisplaySsid(scanResult.SSID)});
                } else if (NetworkTypeUtil.getConfiguredNetworkForScanResult(scanResult, networks) != null) {
                    if (scanResult.is24GHz() != wifiInfo.is24GHz()) {
                        if (scanResult.is5GHz() != wifiInfo.is5GHz()) {
                            if (scanResult.BSSID.equals(wifiInfo.getBSSID())) {
                                HwHiLog.i(TAG, false, "%{public}s is filter for same bssid to wifi1", new Object[]{StringUtilEx.safeDisplaySsid(scanResult.SSID)});
                            } else if (WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) < this.mPoorSignalThres) {
                                HwHiLog.i(TAG, false, "%{public}s is filter for week signal level frequency is %{public}d, level is %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(scanResult.SSID), Integer.valueOf(scanResult.frequency), Integer.valueOf(scanResult.level)});
                                this.mFiltedNetworkMask |= 1;
                            } else if (isBssidInWifi2Blacklist(scanResult.BSSID)) {
                                blacklistScanResults.add(scanResult);
                            } else if (WifiProCommonUtils.isDualBandAP(wifiInfo.getBSSID(), scanResult.BSSID)) {
                                dualApScanResults.add(scanResult);
                            } else {
                                normalScanResults.add(scanResult);
                            }
                        }
                    }
                    HwHiLog.i(TAG, false, "%{public}s is filter for same band to wifi1", new Object[]{StringUtilEx.safeDisplaySsid(scanResult.SSID)});
                }
            }
        }
        HwHiLog.i(TAG, false, "filterAndClassifyScanResult normalScanResults size is %{public}d, dualApScanResults size is %{public}d, blacklistScanResults size is %{public}d", new Object[]{Integer.valueOf(normalScanResults.size()), Integer.valueOf(dualApScanResults.size()), Integer.valueOf(blacklistScanResults.size())});
    }

    private void classifyNetworksByInternet(List<WifiConfiguration> networks, List<WifiConfiguration> hasInternetNetworks, List<WifiConfiguration> noInternetNetworks) {
        for (WifiConfiguration network : networks) {
            if (hasInternet(network) || maybeHasInternet(network)) {
                hasInternetNetworks.add(network);
            } else {
                noInternetNetworks.add(network);
            }
        }
        HwHiLog.i(TAG, false, "classifyNetworksByInternet hasInternetNetworks size is %{public}d, noInternetNetworks size is %{public}d", new Object[]{Integer.valueOf(hasInternetNetworks.size()), Integer.valueOf(noInternetNetworks.size())});
    }

    private boolean isBssidInWifi2Blacklist(String bssid) {
        Map<String, Long> wifi2BssidBlacklist = HwWifi2Injector.getInstance().getWifiConnectivityManager().getWifi2BssidBlacklist();
        Long bssidAddtoBlackListTime = wifi2BssidBlacklist.get(bssid);
        if (bssidAddtoBlackListTime != null) {
            long currentTimeStamp = SystemClock.elapsedRealtime();
            if (currentTimeStamp - bssidAddtoBlackListTime.longValue() < 604800000) {
                HwHiLog.i(TAG, false, "%{public}s is in wifi2BssidBlacklist. current time is %{public}s, add to wifi2BssidBlacklist time is %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(bssid), String.valueOf(currentTimeStamp), String.valueOf(bssidAddtoBlackListTime)});
                return true;
            }
            wifi2BssidBlacklist.remove(bssid);
            HwHiLog.i(TAG, false, "wifi2 bssid %{public}s expired from blacklist.. Current time is %{public}s, wifi1 bssid add to blacklist time is %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(bssid), String.valueOf(currentTimeStamp), String.valueOf(bssidAddtoBlackListTime)});
        }
        return false;
    }

    private List<WifiConfiguration> filterWifiConfiguration(List<WifiConfiguration> networks, ScanResult[] scanResults) {
        List<ScanResult> scanResultsList = new ArrayList<>(Arrays.asList(scanResults));
        for (WifiConfiguration network : networks) {
            disableFtkeyManagement(network, scanResultsList);
        }
        List<WifiConfiguration> result = new ArrayList<>();
        for (WifiConfiguration network2 : networks) {
            if (network2.portalNetwork) {
                HwHiLog.i(TAG, false, "%{public}s is filter for portal network", new Object[]{StringUtilEx.safeDisplaySsid(network2.getPrintableSsid())});
                this.mFiltedNetworkMask |= 2;
            } else if (HUAWEI_GUEST.equals(network2.configKey())) {
                HwHiLog.i(TAG, false, "%{public}s is filter for HUAWEI_GUEST network", new Object[]{StringUtilEx.safeDisplaySsid(network2.getPrintableSsid())});
            } else if (network2.allowedKeyManagement.nextSetBit(0) == -1) {
                HwHiLog.e(TAG, false, "%{public}s is filter for no keyMgmt set, keyMgmt is %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(network2.getPrintableSsid()), network2.allowedKeyManagement});
                this.mFiltedNetworkMask |= FILTER_FT_ONLY_NETWORK_BIT;
            } else if (isSimAuthWifiConfiguration(network2)) {
                HwHiLog.i(TAG, false, "%{public}s is filter for sim auth network", new Object[]{StringUtilEx.safeDisplaySsid(network2.getPrintableSsid())});
                this.mFiltedNetworkMask |= FILTER_SIM_NETWORK_BIT;
            } else if (NetworkTypeUtil.isWapiWifiConfiguration(network2)) {
                HwHiLog.i(TAG, false, "%{public}s is filter for wapi network", new Object[]{StringUtilEx.safeDisplaySsid(network2.getPrintableSsid())});
                this.mFiltedNetworkMask |= FILTER_WAPI_NETWORK_BIT;
            } else {
                result.add(network2);
            }
        }
        return result;
    }

    private void disableFtkeyManagement(WifiConfiguration config, List<ScanResult> scanResults) {
        ScanResult targetScanResult = NetworkTypeUtil.getScanResultForConfiguredNetwork(config, scanResults);
        if (targetScanResult != null) {
            if (config.allowedKeyManagement.get(6)) {
                config.allowedKeyManagement.set(6, false);
                if (targetScanResult.capabilities.contains("PSK+FT/PSK") || targetScanResult.capabilities.contains("FT/PSK+PSK")) {
                    config.allowedKeyManagement.set(1);
                    HwHiLog.i(TAG, false, "disableFtkeyManagement convert FT_PSK to PSK for %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                } else {
                    HwHiLog.i(TAG, false, "disableFtkeyManagement %{public}s has FT_PSK, but find no PSK capabilities in scan result capabilities: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid()), targetScanResult.capabilities});
                }
            }
            if (config.allowedKeyManagement.get(7)) {
                config.allowedKeyManagement.set(7, false);
                if (targetScanResult.capabilities.contains("EAP+FT/EAP") || targetScanResult.capabilities.contains("FT/EAP+EAP") || targetScanResult.capabilities.contains("EAP_SUITE_B_192")) {
                    config.allowedKeyManagement.set(2);
                    config.allowedKeyManagement.set(3);
                    HwHiLog.i(TAG, false, "disableFtkeyManagement convert FT_EAP to WPA_EAP and IEEE8021X for %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                    return;
                }
                HwHiLog.i(TAG, false, "disableFtkeyManagement %{public}s has FT_EAP, but find no EAP capabilities in scan result capabilities: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid()), targetScanResult.capabilities});
            }
        }
    }

    private boolean isSimAuthWifiConfiguration(WifiConfiguration config) {
        if (config.enterpriseConfig == null) {
            return false;
        }
        if (config.enterpriseConfig.getPhase2Method() == 5 || config.enterpriseConfig.getEapMethod() == 4) {
            return true;
        }
        return false;
    }
}
