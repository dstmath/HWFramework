package com.android.server.wifi.util;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.io.PrintWriter;
import java.util.List;

public class ScanResultUtil {
    private static final String TAG = "ScanResultUtil";

    private ScanResultUtil() {
    }

    public static ScanDetail toScanDetail(ScanResult scanResult) {
        try {
            return new ScanDetail(scanResult, new NetworkDetail(scanResult.BSSID, scanResult.informationElements, scanResult.anqpLines, scanResult.frequency, scanResult.capabilities));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException occurs when construct NetworkDetail", e);
            return null;
        }
    }

    public static boolean isScanResultForPskNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("PSK");
    }

    public static boolean isScanResultForEapNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("EAP");
    }

    public static boolean isScanResultForEapSuiteBNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("SUITE_B_192");
    }

    public static boolean isScanResultForWepNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("WEP");
    }

    public static boolean isScanResultForCertNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("CERT");
    }

    public static boolean isScanResultForWapiPskNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("WAPI-PSK");
    }

    public static boolean isScanResultForOweNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("OWE");
    }

    public static boolean isScanResultForOweTransitionNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("OWE_TRANSITION");
    }

    public static boolean isScanResultForSaeNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("SAE");
    }

    public static boolean isScanResultForPskSaeTransitionNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("PSK") && scanResult.capabilities.contains("SAE");
    }

    public static boolean isScanResultForOpenNetwork(ScanResult scanResult) {
        return !isScanResultForWepNetwork(scanResult) && !isScanResultForPskNetwork(scanResult) && !isScanResultForEapNetwork(scanResult) && !isScanResultForSaeNetwork(scanResult) && !isScanResultForEapSuiteBNetwork(scanResult) && !isScanResultForCertNetwork(scanResult);
    }

    @VisibleForTesting
    public static String createQuotedSSID(String ssid) {
        return "\"" + ssid + "\"";
    }

    public static WifiConfiguration createNetworkFromScanResult(ScanResult scanResult) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = createQuotedSSID(scanResult.SSID);
        setAllowedKeyManagementFromScanResult(scanResult, config);
        return config;
    }

    public static void setAllowedKeyManagementFromScanResult(ScanResult scanResult, WifiConfiguration config) {
        if (isScanResultForSaeNetwork(scanResult)) {
            config.setSecurityParams(4);
        } else if (isScanResultForPskNetwork(scanResult)) {
            config.setSecurityParams(2);
        } else if (isScanResultForEapSuiteBNetwork(scanResult)) {
            config.setSecurityParams(5);
        } else if (isScanResultForEapNetwork(scanResult)) {
            config.setSecurityParams(3);
        } else if (isScanResultForWepNetwork(scanResult)) {
            config.setSecurityParams(1);
        } else if (isScanResultForOweNetwork(scanResult)) {
            config.setSecurityParams(6);
        } else {
            config.setSecurityParams(0);
        }
    }

    public static void dumpScanResults(PrintWriter pw, List<ScanResult> scanResults, long nowMs) {
        String age;
        String rssiInfo;
        if (!(scanResults == null || scanResults.size() == 0)) {
            pw.println("    BSSID              Frequency      RSSI           Age(sec)     SSID                                 Flags");
            for (ScanResult r : scanResults) {
                long timeStampMs = r.timestamp / 1000;
                if (timeStampMs <= 0) {
                    age = "___?___";
                } else if (nowMs < timeStampMs) {
                    age = "  0.000";
                } else if (timeStampMs < nowMs - 1000000) {
                    age = ">1000.0";
                } else {
                    age = String.format("%3.3f", Double.valueOf(((double) (nowMs - timeStampMs)) / 1000.0d));
                }
                String ssid = r.SSID == null ? "" : r.SSID;
                if (ArrayUtils.size(r.radioChainInfos) == 1) {
                    rssiInfo = String.format("%5d(%1d:%3d)       ", Integer.valueOf(r.level), Integer.valueOf(r.radioChainInfos[0].id), Integer.valueOf(r.radioChainInfos[0].level));
                } else if (ArrayUtils.size(r.radioChainInfos) == 2) {
                    rssiInfo = String.format("%5d(%1d:%3d/%1d:%3d)", Integer.valueOf(r.level), Integer.valueOf(r.radioChainInfos[0].id), Integer.valueOf(r.radioChainInfos[0].level), Integer.valueOf(r.radioChainInfos[1].id), Integer.valueOf(r.radioChainInfos[1].level));
                } else {
                    rssiInfo = String.format("%9d         ", Integer.valueOf(r.level));
                }
                pw.printf("  %17s  %9d  %18s   %7s    %-32s  %s\n", StringUtilEx.safeDisplayBssid(r.BSSID), Integer.valueOf(r.frequency), rssiInfo, age, String.format("%1.32s", StringUtilEx.safeDisplaySsid(ssid)), r.capabilities);
            }
        }
    }
}
