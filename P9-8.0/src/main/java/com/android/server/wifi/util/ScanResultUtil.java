package com.android.server.wifi.util;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiConfigurationUtil;
import com.android.server.wifi.hotspot2.NetworkDetail;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScanResultUtil {
    private static final String TAG = "ScanResultUtil";

    private ScanResultUtil() {
    }

    public static ScanDetail toScanDetail(ScanResult scanResult) {
        NetworkDetail networkDetail;
        try {
            networkDetail = new NetworkDetail(scanResult.BSSID, scanResult.informationElements, scanResult.anqpLines, scanResult.frequency, scanResult.capabilities);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException occurs when construct NetworkDetail", e);
            networkDetail = null;
        }
        return new ScanDetail(scanResult, networkDetail);
    }

    public static boolean isScanResultForPskNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("PSK");
    }

    public static boolean isScanResultForEapNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("EAP");
    }

    public static boolean isScanResultForWepNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("WEP");
    }

    public static boolean isScanResultForCertNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("CERT");
    }

    public static boolean isScanResultForOpenNetwork(ScanResult scanResult) {
        int i;
        if (isScanResultForWepNetwork(scanResult) || isScanResultForPskNetwork(scanResult) || isScanResultForEapNetwork(scanResult)) {
            i = 1;
        } else {
            i = isScanResultForCertNetwork(scanResult);
        }
        return i ^ 1;
    }

    public static String createQuotedSSID(String ssid) {
        return "\"" + ssid + "\"";
    }

    public static boolean doesScanResultMatchWithNetwork(ScanResult scanResult, WifiConfiguration config) {
        if (TextUtils.equals(config.SSID, createQuotedSSID(scanResult.SSID))) {
            if (isScanResultForPskNetwork(scanResult) && WifiConfigurationUtil.isConfigForPskNetwork(config)) {
                return true;
            }
            if (isScanResultForEapNetwork(scanResult) && WifiConfigurationUtil.isConfigForEapNetwork(config)) {
                return true;
            }
            if (isScanResultForWepNetwork(scanResult) && WifiConfigurationUtil.isConfigForWepNetwork(config)) {
                return true;
            }
            if (isScanResultForCertNetwork(scanResult) && WifiConfigurationUtil.isConfigForCertNetwork(config)) {
                return true;
            }
            if (isScanResultForOpenNetwork(scanResult) && WifiConfigurationUtil.isConfigForOpenNetwork(config)) {
                return true;
            }
        }
        return false;
    }

    public static WifiConfiguration createNetworkFromScanResult(ScanResult scanResult) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = createQuotedSSID(scanResult.SSID);
        setAllowedKeyManagementFromScanResult(scanResult, config);
        return config;
    }

    public static void setAllowedKeyManagementFromScanResult(ScanResult scanResult, WifiConfiguration config) {
        if (isScanResultForPskNetwork(scanResult)) {
            config.allowedKeyManagement.set(1);
        } else if (isScanResultForEapNetwork(scanResult)) {
            config.allowedKeyManagement.set(2);
            config.allowedKeyManagement.set(3);
        } else if (isScanResultForWepNetwork(scanResult)) {
            config.allowedKeyManagement.set(0);
            config.allowedAuthAlgorithms.set(0);
            config.allowedAuthAlgorithms.set(1);
        } else {
            config.allowedKeyManagement.set(0);
        }
    }

    public static synchronized String getScanResultLogs(Set<String> oldSsidList, List<ScanDetail> results) {
        String stringBuilder;
        synchronized (ScanResultUtil.class) {
            String ssid;
            StringBuilder scanResultLogs = new StringBuilder();
            Set<String> newSsidList = new HashSet();
            for (ScanDetail detail : results) {
                ssid = detail.getSSID();
                if (!newSsidList.contains(ssid)) {
                    newSsidList.add(ssid);
                }
            }
            StringBuilder sb = new StringBuilder();
            for (String ssid2 : newSsidList) {
                if (oldSsidList.contains(ssid2)) {
                    oldSsidList.remove(ssid2);
                } else {
                    sb.append(" +");
                    sb.append(ssid2);
                }
            }
            if (sb.length() > 1) {
                scanResultLogs.append("Add:").append(sb);
            }
            sb = new StringBuilder();
            for (String stringBuilder2 : oldSsidList) {
                sb.append(" -");
                sb.append(stringBuilder2);
            }
            if (sb.length() > 1) {
                scanResultLogs.append(", Remove:").append(sb);
            }
            oldSsidList.clear();
            oldSsidList.addAll(newSsidList);
            stringBuilder2 = scanResultLogs.toString();
        }
        return stringBuilder2;
    }

    public static String getConfusedBssid(String bssid) {
        if (bssid == null) {
            return bssid;
        }
        return bssid.replaceFirst("[0-9]", "+").replaceFirst("[a-z]", WifiConfigManager.PASSWORD_MASK);
    }
}
