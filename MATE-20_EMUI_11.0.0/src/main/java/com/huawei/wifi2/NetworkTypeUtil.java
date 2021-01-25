package com.huawei.wifi2;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class NetworkTypeUtil {
    private static final int INVALID_TYPE = -1;

    public static boolean isScanResultForPskNetwork(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        return scanResult.capabilities.contains("PSK");
    }

    public static boolean isScanResultForEapNetwork(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        return scanResult.capabilities.contains("EAP");
    }

    public static boolean isScanResultForEapSuitebNetwork(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        return scanResult.capabilities.contains("SUITE_B_192");
    }

    public static boolean isScanResultForWepNetwork(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        return scanResult.capabilities.contains("WEP");
    }

    public static boolean isScanResultForCertNetwork(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        return scanResult.capabilities.contains("CERT");
    }

    public static boolean isScanResultForWapiPskNetwork(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        return scanResult.capabilities.contains("WAPI-PSK");
    }

    public static boolean isScanResultForOweNetwork(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        return scanResult.capabilities.contains("OWE");
    }

    public static boolean isScanResultForOweTransitionNetwork(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        return scanResult.capabilities.contains("OWE_TRANSITION");
    }

    public static boolean isScanResultForSaeNetwork(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        return scanResult.capabilities.contains("SAE");
    }

    public static boolean isScanResultForPskSaeTransitionNetwork(ScanResult scanResult) {
        if (scanResult != null && scanResult.capabilities.contains("PSK") && scanResult.capabilities.contains("SAE")) {
            return true;
        }
        return false;
    }

    public static boolean isScanResultForOpenNetwork(ScanResult scanResult) {
        if (scanResult != null && !isScanResultForWepNetwork(scanResult) && !isScanResultForPskNetwork(scanResult) && !isScanResultForEapNetwork(scanResult) && !isScanResultForSaeNetwork(scanResult) && !isScanResultForEapSuitebNetwork(scanResult) && !isScanResultForCertNetwork(scanResult)) {
            return true;
        }
        return false;
    }

    public static boolean isConfigForPskNetwork(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        if (config.allowedKeyManagement.get(1) || config.allowedKeyManagement.get(6) || config.allowedKeyManagement.get(16) || config.allowedKeyManagement.get(18)) {
            return true;
        }
        return false;
    }

    public static boolean isConfigForSaeNetwork(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        return config.allowedKeyManagement.get(8);
    }

    public static boolean isConfigForOweNetwork(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        return config.allowedKeyManagement.get(9);
    }

    public static boolean isConfigForEapNetwork(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3) || config.allowedKeyManagement.get(7)) {
            return true;
        }
        return false;
    }

    public static boolean isConfigForEapSuitebNetwork(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        return config.allowedKeyManagement.get(10);
    }

    public static boolean isConfigForWepNetwork(WifiConfiguration config) {
        if (config != null && config.allowedKeyManagement.get(0) && hasAnyValidWepKey(config.wepKeys)) {
            return true;
        }
        return false;
    }

    public static boolean hasAnyValidWepKey(String[] wepKeys) {
        if (wepKeys == null) {
            return false;
        }
        for (String str : wepKeys) {
            if (str != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConfigForCertNetwork(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        if (config.allowedKeyManagement.get(17) || config.allowedKeyManagement.get(19)) {
            return true;
        }
        return false;
    }

    public static boolean isConfigForOpenNetwork(WifiConfiguration config) {
        if (config != null && !isConfigForWepNetwork(config) && !isConfigForPskNetwork(config) && !isConfigForEapNetwork(config) && !isConfigForSaeNetwork(config) && !isConfigForEapSuitebNetwork(config) && !isConfigForCertNetwork(config)) {
            return true;
        }
        return false;
    }

    public static int getNetworkType(ScanResult scanResult) {
        if (scanResult == null) {
            return -1;
        }
        if (isScanResultForSaeNetwork(scanResult)) {
            return 4;
        }
        if (isScanResultForPskNetwork(scanResult)) {
            return 2;
        }
        if (isScanResultForEapSuitebNetwork(scanResult)) {
            return 5;
        }
        if (isScanResultForEapNetwork(scanResult)) {
            return 3;
        }
        if (isScanResultForWepNetwork(scanResult)) {
            return 1;
        }
        if (isScanResultForOweNetwork(scanResult)) {
            return 6;
        }
        if (isScanResultForOpenNetwork(scanResult)) {
            return 0;
        }
        if (isScanResultForCertNetwork(scanResult)) {
            return 7;
        }
        return -1;
    }

    public static int getNetworkType(WifiConfiguration config) {
        if (config == null) {
            return -1;
        }
        if (isConfigForSaeNetwork(config)) {
            return 4;
        }
        if (isConfigForPskNetwork(config)) {
            return 2;
        }
        if (isConfigForEapSuitebNetwork(config)) {
            return 5;
        }
        if (isConfigForEapNetwork(config)) {
            return 3;
        }
        if (isConfigForWepNetwork(config)) {
            return 1;
        }
        if (isConfigForOweNetwork(config)) {
            return 6;
        }
        if (isConfigForOpenNetwork(config)) {
            return 0;
        }
        if (isConfigForCertNetwork(config)) {
            return 7;
        }
        return -1;
    }

    public static WifiConfiguration getConfiguredNetworkForScanResult(ScanResult scanResult, List<WifiConfiguration> networks) {
        if (scanResult == null || networks == null) {
            return null;
        }
        String scanResultSsid = createQuotedSsid(scanResult.SSID);
        int scanResultNetworkType = getNetworkType(scanResult);
        boolean isScanResultPskSaeInTransitionMode = true;
        boolean isScanResultOweInTransitionMode = scanResultNetworkType == 6 && isScanResultForOweTransitionNetwork(scanResult);
        if (scanResultNetworkType != 4 || !isScanResultForPskSaeTransitionNetwork(scanResult)) {
            isScanResultPskSaeInTransitionMode = false;
        }
        Iterator<WifiConfiguration> it = networks.iterator();
        while (it.hasNext()) {
            WifiConfiguration wifiConfig = it.next();
            if (Objects.equals(scanResultSsid, wifiConfig.SSID)) {
                int wifiConfigNetworkType = getNetworkType(wifiConfig);
                if (isScanResultPskSaeInTransitionMode && wifiConfigNetworkType == 2) {
                    return wifiConfig;
                }
                if ((isScanResultOweInTransitionMode && wifiConfigNetworkType == 0) || scanResultNetworkType == wifiConfigNetworkType) {
                    return wifiConfig;
                }
            }
        }
        return null;
    }

    public static ScanResult getScanResultForConfiguredNetwork(WifiConfiguration network, List<ScanResult> scanResults) {
        if (network == null || scanResults == null) {
            return null;
        }
        Iterator<ScanResult> it = scanResults.iterator();
        while (it.hasNext()) {
            ScanResult scanResult = it.next();
            String scanResultSsid = createQuotedSsid(scanResult.SSID);
            int scanResultNetworkType = getNetworkType(scanResult);
            boolean isScanResultPskSaeInTransitionMode = true;
            boolean isScanResultOweInTransitionMode = scanResultNetworkType == 6 && isScanResultForOweTransitionNetwork(scanResult);
            if (scanResultNetworkType != 4 || !isScanResultForPskSaeTransitionNetwork(scanResult)) {
                isScanResultPskSaeInTransitionMode = false;
            }
            if (Objects.equals(scanResultSsid, network.SSID)) {
                int wifiConfigNetworkType = getNetworkType(network);
                if (isScanResultPskSaeInTransitionMode && wifiConfigNetworkType == 2) {
                    return scanResult;
                }
                if ((isScanResultOweInTransitionMode && wifiConfigNetworkType == 0) || scanResultNetworkType == wifiConfigNetworkType) {
                    return scanResult;
                }
            }
        }
        return null;
    }

    public static boolean isWapiWifiConfiguration(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        if (config.allowedKeyManagement.get(16) || config.allowedKeyManagement.get(18) || config.allowedKeyManagement.get(17) || config.allowedKeyManagement.get(19)) {
            return true;
        }
        return false;
    }

    public static String createQuotedSsid(String ssid) {
        return "\"" + ssid + "\"";
    }
}
