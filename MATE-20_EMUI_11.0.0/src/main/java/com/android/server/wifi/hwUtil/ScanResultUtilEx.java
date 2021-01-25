package com.android.server.wifi.hwUtil;

import com.android.server.wifi.ScanDetail;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScanResultUtilEx {
    private ScanResultUtilEx() {
    }

    public static synchronized String getScanResultLogs(Set<String> oldSsidList, List<ScanDetail> results) {
        String sb;
        synchronized (ScanResultUtilEx.class) {
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
                    sb2.append(StringUtilEx.safeDisplaySsid(ssid2));
                } else {
                    oldSsidList.remove(ssid2);
                }
            }
            if (sb2.length() > 1) {
                scanResultLogs.append("Add:");
                scanResultLogs.append((CharSequence) sb2);
            }
            StringBuilder sb3 = new StringBuilder();
            for (String str : oldSsidList) {
                sb3.append(" -");
                sb3.append(StringUtilEx.safeDisplaySsid(str));
            }
            if (sb3.length() > 1) {
                scanResultLogs.append(", Remove:");
                scanResultLogs.append((CharSequence) sb3);
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
}
