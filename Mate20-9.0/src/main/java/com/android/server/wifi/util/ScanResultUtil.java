package com.android.server.wifi.util;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.hotspot2.NetworkDetail;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScanResultUtil {
    private static final String TAG = "ScanResultUtil";

    private ScanResultUtil() {
    }

    public static ScanDetail toScanDetail(ScanResult scanResult) {
        NetworkDetail networkDetail = null;
        try {
            NetworkDetail networkDetail2 = new NetworkDetail(scanResult.BSSID, scanResult.informationElements, scanResult.anqpLines, scanResult.frequency, scanResult.capabilities);
            networkDetail = networkDetail2;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException occurs when construct NetworkDetail", e);
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

    public static boolean isScanResultForWapiPskNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("WAPI-PSK");
    }

    public static boolean isScanResultForOpenNetwork(ScanResult scanResult) {
        return !isScanResultForWepNetwork(scanResult) && !isScanResultForPskNetwork(scanResult) && !isScanResultForEapNetwork(scanResult) && !isScanResultForCertNetwork(scanResult);
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
        String sb;
        synchronized (ScanResultUtil.class) {
            StringBuilder scanResultLogs = new StringBuilder();
            Set<String> newSsidList = new HashSet<>();
            for (ScanDetail detail : results) {
                String ssid = detail.getSSID();
                if (!newSsidList.contains(ssid)) {
                    newSsidList.add(ssid);
                }
            }
            StringBuilder sb2 = new StringBuilder();
            for (String ssid2 : newSsidList) {
                if (!oldSsidList.contains(ssid2)) {
                    sb2.append(" +");
                    sb2.append(ssid2);
                } else {
                    oldSsidList.remove(ssid2);
                }
            }
            if (sb2.length() > 1) {
                scanResultLogs.append("Add:");
                scanResultLogs.append(sb2);
            }
            StringBuilder sb3 = new StringBuilder();
            for (String append : oldSsidList) {
                sb3.append(" -");
                sb3.append(append);
            }
            if (sb3.length() > 1) {
                scanResultLogs.append(", Remove:");
                scanResultLogs.append(sb3);
            }
            oldSsidList.clear();
            oldSsidList.addAll(newSsidList);
            sb = scanResultLogs.toString();
        }
        return sb;
    }

    public static String getConfusedBssid(String bssid) {
        if (bssid == null) {
            return bssid;
        }
        return bssid.replaceAll(":[\\w]{1,}:[\\w]{1,}:", ":**:**:");
    }

    public static void dumpScanResults(PrintWriter pw, List<ScanResult> scanResults, long nowMs) {
        String age;
        String rssiInfo;
        PrintWriter printWriter = pw;
        if (scanResults != null && scanResults.size() != 0) {
            printWriter.println("    BSSID              Frequency      RSSI           Age(sec)     SSID                                 Flags");
            for (ScanResult r : scanResults) {
                long timeStampMs = r.timestamp / 1000;
                if (timeStampMs <= 0) {
                    age = "___?___";
                } else if (nowMs < timeStampMs) {
                    age = "  0.000";
                } else if (timeStampMs < nowMs - 1000000) {
                    age = ">1000.0";
                } else {
                    age = String.format("%3.3f", new Object[]{Double.valueOf(((double) (nowMs - timeStampMs)) / 1000.0d)});
                }
                String ssid = r.SSID == null ? "" : r.SSID;
                if (ArrayUtils.size(r.radioChainInfos) == 1) {
                    rssiInfo = String.format("%5d(%1d:%3d)       ", new Object[]{Integer.valueOf(r.level), Integer.valueOf(r.radioChainInfos[0].id), Integer.valueOf(r.radioChainInfos[0].level)});
                } else if (ArrayUtils.size(r.radioChainInfos) == 2) {
                    rssiInfo = String.format("%5d(%1d:%3d/%1d:%3d)", new Object[]{Integer.valueOf(r.level), Integer.valueOf(r.radioChainInfos[0].id), Integer.valueOf(r.radioChainInfos[0].level), Integer.valueOf(r.radioChainInfos[1].id), Integer.valueOf(r.radioChainInfos[1].level)});
                } else {
                    rssiInfo = String.format("%9d         ", new Object[]{Integer.valueOf(r.level)});
                }
                printWriter.printf("  %17s  %9d  %18s   %7s    %-32s  %s\n", new Object[]{r.BSSID, Integer.valueOf(r.frequency), rssiInfo, age, String.format("%1.32s", new Object[]{ssid}), r.capabilities});
            }
        }
    }
}
