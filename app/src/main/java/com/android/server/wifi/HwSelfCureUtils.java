package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.os.SystemProperties;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HwSelfCureUtils {
    private static final int CURE_OUT_OF_DATE_MS = 7200000;
    public static final int DELAYED_DAYS_HIGH = 432000000;
    public static final int DELAYED_DAYS_LOW = 86400000;
    public static final int DELAYED_DAYS_MID = 259200000;
    public static final String DNS_MONITOR_FLAG = "hw.wifipro.dns_fail_count";
    private static final int INTERVAL_SELF_CURE = 60000;
    public static final int MAX_FAILED_CURE = 3;
    public static final int MIN_TCP_FAILED_THRESHOLD = 3;
    private static final String[] PUBLIC_DNS_CHINA = null;
    private static final String[] PUBLIC_DNS_OVERSEA = null;
    public static final int RESET_LEVEL_HIGH_RESET = 205;
    public static final int RESET_LEVEL_IDLE = 200;
    public static final int RESET_LEVEL_LOW_1_DNS = 201;
    public static final int RESET_LEVEL_LOW_2_RENEW_DHCP = 202;
    public static final int RESET_LEVEL_LOW_3_STATIC_IP = 203;
    public static final int RESET_LEVEL_MIDDLE_REASSOC = 204;
    public static final int RESET_REJECTED_BY_STATIC_IP_ENABLED = 206;
    public static final int SCE_WIFI_CONNECT_STATE = 3;
    public static final int SCE_WIFI_CONNET_RETRY = 1;
    public static final int SCE_WIFI_DISABLED_DELAY = 200;
    public static final int SCE_WIFI_OFF_STATE = 1;
    public static final int SCE_WIFI_ON_STATE = 2;
    public static final int SCE_WIFI_REASSOC_STATE = 4;
    public static final int SCE_WIFI_STATUS_FAIL = -1;
    public static final int SCE_WIFI_STATUS_LOST = -2;
    public static final int SCE_WIFI_STATUS_SUCC = 0;
    public static final int SCE_WIFI_STATUS_UNKOWN = 1;
    public static final int SELFCURE_WIFI_CONNECT_TIMEOUT = 15000;
    public static final int SELFCURE_WIFI_OFF_TIMEOUT = 2000;
    public static final int SELFCURE_WIFI_ON_TIMEOUT = 3000;
    public static final int SELFCURE_WIFI_REASSOC_TIMEOUT = 12000;
    public static final int SIGNAL_LEVEL_1 = 1;
    public static final int SIGNAL_LEVEL_2 = 2;
    public static final int SIGNAL_LEVEL_3 = 3;
    public static final String WIFI_STAT_FILE = "proc/net/wifi_network_stat";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwSelfCureUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwSelfCureUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwSelfCureUtils.<clinit>():void");
    }

    public static int getCurrentDnsFailedCounter() {
        String dnsFailedCounter = SystemProperties.get(DNS_MONITOR_FLAG, "0");
        int counter = SCE_WIFI_STATUS_SUCC;
        try {
            counter = Integer.parseInt(dnsFailedCounter);
        } catch (NumberFormatException e) {
        }
        return counter;
    }

    public static List<String> getRefreshedCureFailedNetworks(Map<String, CureFailedNetworkInfo> networkCureFailedHistory) {
        List<String> refreshedNetworksKey = new ArrayList();
        for (Entry entry : networkCureFailedHistory.entrySet()) {
            String currKey = (String) entry.getKey();
            if (System.currentTimeMillis() - ((CureFailedNetworkInfo) networkCureFailedHistory.get(currKey)).lastCureFailedTime > 7200000) {
                refreshedNetworksKey.add(currKey);
            }
        }
        return refreshedNetworksKey;
    }

    public static List<String> searchUnstableNetworks(Map<String, WifiConfiguration> autoConnectFailedNetworks, List<ScanResult> scanResults) {
        List<String> unstableKey = new ArrayList();
        if (scanResults == null || scanResults.size() == 0) {
            return unstableKey;
        }
        for (Entry entry : autoConnectFailedNetworks.entrySet()) {
            String currKey = (String) entry.getKey();
            WifiConfiguration currConfig = (WifiConfiguration) entry.getValue();
            boolean outOfRange = true;
            for (int i = SCE_WIFI_STATUS_SUCC; i < scanResults.size(); i += SIGNAL_LEVEL_1) {
                ScanResult nextResult = (ScanResult) scanResults.get(i);
                if (nextResult.level >= -75) {
                    String scanSsid = "\"" + nextResult.SSID + "\"";
                    String scanResultEncrypt = nextResult.capabilities;
                    if (currConfig.SSID != null && currConfig.SSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, currConfig.configKey())) {
                        outOfRange = false;
                        break;
                    }
                }
            }
            if (outOfRange) {
                unstableKey.add(currKey);
            }
        }
        return unstableKey;
    }

    public static void selectDisabledNetworks(List<ScanResult> scanResults, List<WifiConfiguration> savedNetworks, Map<String, WifiConfiguration> autoConnectFailedNetworks, Map<String, Integer> autoConnectFailedNetworksRssi, WifiStateMachine wsm) {
        List<WifiConfiguration> disabledNetworks = new ArrayList();
        if (scanResults != null && scanResults.size() != 0 && savedNetworks != null && savedNetworks.size() != 0 && wsm != null) {
            for (int i = SCE_WIFI_STATUS_SUCC; i < scanResults.size(); i += SIGNAL_LEVEL_1) {
                ScanResult nextResult = (ScanResult) scanResults.get(i);
                if (nextResult.level >= -75) {
                    String scanSsid = "\"" + nextResult.SSID + "\"";
                    String scanResultEncrypt = nextResult.capabilities;
                    int j = SCE_WIFI_STATUS_SUCC;
                    while (j < savedNetworks.size()) {
                        WifiConfiguration nextConfig = (WifiConfiguration) savedNetworks.get(j);
                        if (nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, nextConfig.configKey())) {
                            NetworkSelectionStatus status = nextConfig.getNetworkSelectionStatus();
                            int disableReason = status.getNetworkSelectionDisableReason();
                            if (!(status.isNetworkEnabled() || disableReason < SIGNAL_LEVEL_1 || disableReason > 9 || disableReason == 6 || disableReason == 7)) {
                                autoConnectFailedNetworks.put(nextConfig.configKey(), nextConfig);
                                autoConnectFailedNetworksRssi.put(nextConfig.configKey(), Integer.valueOf(nextResult.level));
                            }
                            if (!autoConnectFailedNetworks.containsKey(nextConfig.configKey()) && wsm.isBssidDisabled(nextResult.BSSID)) {
                                autoConnectFailedNetworks.put(nextConfig.configKey(), nextConfig);
                                autoConnectFailedNetworksRssi.put(nextConfig.configKey(), Integer.valueOf(nextResult.level));
                            }
                        } else {
                            j += SIGNAL_LEVEL_1;
                        }
                    }
                }
            }
        }
    }

    public static WifiConfiguration selectHighestFailedNetwork(Map<String, CureFailedNetworkInfo> networkCureFailedHistory, Map<String, WifiConfiguration> autoConnectFailedNetworks, Map<String, Integer> autoConnectFailedNetworksRssi) {
        WifiConfiguration bestSelfCureCandidate = null;
        int bestSelfCureLevel = WifiHandover.INVALID_RSSI;
        CureFailedNetworkInfo bestCureHistory = null;
        for (Entry entry : autoConnectFailedNetworks.entrySet()) {
            String currKey = (String) entry.getKey();
            WifiConfiguration currConfig = (WifiConfiguration) entry.getValue();
            int currLevel = ((Integer) autoConnectFailedNetworksRssi.get(currKey)).intValue();
            CureFailedNetworkInfo currCureHistory = (CureFailedNetworkInfo) networkCureFailedHistory.get(currKey);
            if (currCureHistory == null || (currCureHistory.cureFailedCounter != SIGNAL_LEVEL_3 && System.currentTimeMillis() - currCureHistory.lastCureFailedTime >= 60000)) {
                if (bestSelfCureCandidate == null) {
                    bestSelfCureCandidate = currConfig;
                    bestSelfCureLevel = currLevel;
                    bestCureHistory = currCureHistory;
                } else if (bestCureHistory == null && currCureHistory == null) {
                    if (currConfig.noInternetAccess || currConfig.portalNetwork || !(bestSelfCureCandidate.noInternetAccess || currConfig.portalNetwork)) {
                        if (!(bestSelfCureCandidate.noInternetAccess || bestSelfCureCandidate.portalNetwork)) {
                            if (!currConfig.noInternetAccess) {
                                if (currConfig.portalNetwork) {
                                }
                            }
                        }
                        if (currLevel > bestSelfCureLevel) {
                            bestSelfCureCandidate = currConfig;
                            bestSelfCureLevel = currLevel;
                        }
                    } else {
                        bestSelfCureCandidate = currConfig;
                        bestSelfCureLevel = currLevel;
                    }
                } else if (bestCureHistory != null && currCureHistory == null) {
                    bestSelfCureCandidate = currConfig;
                    bestSelfCureLevel = currLevel;
                    bestCureHistory = null;
                } else if (!(bestCureHistory == null || currCureHistory == null)) {
                    if (currCureHistory.cureFailedCounter < bestCureHistory.cureFailedCounter) {
                        bestSelfCureCandidate = currConfig;
                        bestSelfCureLevel = currLevel;
                        bestCureHistory = currCureHistory;
                    } else if (currCureHistory.cureFailedCounter == bestCureHistory.cureFailedCounter && currLevel > bestSelfCureLevel) {
                        bestSelfCureCandidate = currConfig;
                        bestSelfCureLevel = currLevel;
                        bestCureHistory = currCureHistory;
                    }
                }
            }
        }
        return bestSelfCureCandidate;
    }

    public static ArrayList<String> getPublicDnsServers() {
        if (WifiProCommonUtils.useOperatorOverSea()) {
            return new ArrayList(Arrays.asList(PUBLIC_DNS_OVERSEA));
        }
        return new ArrayList(Arrays.asList(PUBLIC_DNS_CHINA));
    }

    public static InternetSelfCureHistoryInfo string2InternetSelfCureHistoryInfo(String selfCureHistory) {
        InternetSelfCureHistoryInfo info = new InternetSelfCureHistoryInfo();
        if (selfCureHistory != null && selfCureHistory.length() > 0) {
            String[] histories = selfCureHistory.split("\\|");
            if (histories.length == 14) {
                int i = SCE_WIFI_STATUS_SUCC;
                while (i < histories.length) {
                    try {
                        if (i == 0) {
                            info.dnsSelfCureFailedCnt = Integer.valueOf(histories[i]).intValue();
                        } else if (i == SIGNAL_LEVEL_1) {
                            info.lastDnsSelfCureFailedTs = Long.valueOf(histories[i]).longValue();
                        } else if (i == SIGNAL_LEVEL_2) {
                            info.renewDhcpSelfCureFailedCnt = Integer.valueOf(histories[i]).intValue();
                        } else if (i == SIGNAL_LEVEL_3) {
                            info.lastRenewDhcpSelfCureFailedTs = Long.valueOf(histories[i]).longValue();
                        } else if (i == SCE_WIFI_REASSOC_STATE) {
                            info.staticIpSelfCureFailedCnt = Integer.valueOf(histories[i]).intValue();
                        } else if (i == 5) {
                            info.lastStaticIpSelfCureFailedTs = Long.valueOf(histories[i]).longValue();
                        } else if (i == 6) {
                            info.reassocSelfCureFailedCnt = Integer.valueOf(histories[i]).intValue();
                        } else if (i == 7) {
                            info.lastReassocSelfCureFailedTs = Long.valueOf(histories[i]).longValue();
                        } else if (i == 8) {
                            info.resetSelfCureFailedCnt = Integer.valueOf(histories[i]).intValue();
                        } else if (i == 9) {
                            info.lastResetSelfCureFailedTs = Long.valueOf(histories[i]).longValue();
                        } else if (i == 10) {
                            info.reassocSelfCureConnectFailedCnt = Integer.valueOf(histories[i]).intValue();
                        } else if (i == 11) {
                            info.lastReassocSelfCureConnectFailedTs = Long.valueOf(histories[i]).longValue();
                        } else if (i == 12) {
                            info.resetSelfCureConnectFailedCnt = Integer.valueOf(histories[i]).intValue();
                        } else if (i == 13) {
                            info.lastResetSelfCureConnectFailedTs = Long.valueOf(histories[i]).longValue();
                        }
                        i += SIGNAL_LEVEL_1;
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
        return info;
    }

    public static String internetSelfCureHistoryInfo2String(InternetSelfCureHistoryInfo info) {
        StringBuilder strHistory = new StringBuilder();
        if (info != null) {
            strHistory.append(String.valueOf(info.dnsSelfCureFailedCnt)).append("|");
            strHistory.append(String.valueOf(info.lastDnsSelfCureFailedTs)).append("|");
            strHistory.append(String.valueOf(info.renewDhcpSelfCureFailedCnt)).append("|");
            strHistory.append(String.valueOf(info.lastRenewDhcpSelfCureFailedTs)).append("|");
            strHistory.append(String.valueOf(info.staticIpSelfCureFailedCnt)).append("|");
            strHistory.append(String.valueOf(info.lastStaticIpSelfCureFailedTs)).append("|");
            strHistory.append(String.valueOf(info.reassocSelfCureFailedCnt)).append("|");
            strHistory.append(String.valueOf(info.lastReassocSelfCureFailedTs)).append("|");
            strHistory.append(String.valueOf(info.resetSelfCureFailedCnt)).append("|");
            strHistory.append(String.valueOf(info.lastResetSelfCureFailedTs)).append("|");
            strHistory.append(String.valueOf(info.reassocSelfCureConnectFailedCnt)).append("|");
            strHistory.append(String.valueOf(info.lastReassocSelfCureConnectFailedTs)).append("|");
            strHistory.append(String.valueOf(info.resetSelfCureConnectFailedCnt)).append("|");
            strHistory.append(String.valueOf(info.lastResetSelfCureConnectFailedTs));
        }
        return strHistory.toString();
    }

    public static boolean selectedSelfCureAcceptable(InternetSelfCureHistoryInfo historyInfo, int requestCureLevel) {
        if (historyInfo == null) {
            return false;
        }
        long currentMs = System.currentTimeMillis();
        if (requestCureLevel == RESET_LEVEL_LOW_1_DNS) {
            if (historyInfo.dnsSelfCureFailedCnt == 0 || ((historyInfo.dnsSelfCureFailedCnt == SIGNAL_LEVEL_1 && currentMs - historyInfo.lastDnsSelfCureFailedTs > 86400000) || ((historyInfo.dnsSelfCureFailedCnt == SIGNAL_LEVEL_2 && currentMs - historyInfo.lastDnsSelfCureFailedTs > 259200000) || (historyInfo.dnsSelfCureFailedCnt >= SIGNAL_LEVEL_3 && currentMs - historyInfo.lastDnsSelfCureFailedTs > 432000000)))) {
                return true;
            }
        } else if (requestCureLevel == RESET_LEVEL_LOW_2_RENEW_DHCP) {
            if (historyInfo.renewDhcpSelfCureFailedCnt >= 0) {
                return true;
            }
        } else if (requestCureLevel == RESET_LEVEL_LOW_3_STATIC_IP) {
            if (historyInfo.staticIpSelfCureFailedCnt > SCE_WIFI_REASSOC_STATE && ((historyInfo.staticIpSelfCureFailedCnt != 5 || currentMs - historyInfo.lastStaticIpSelfCureFailedTs <= 86400000) && (historyInfo.staticIpSelfCureFailedCnt != 6 || currentMs - historyInfo.lastStaticIpSelfCureFailedTs <= 259200000))) {
                if (historyInfo.staticIpSelfCureFailedCnt >= 7 && currentMs - historyInfo.lastStaticIpSelfCureFailedTs > 432000000) {
                }
            }
            return true;
        } else if (requestCureLevel == RESET_LEVEL_MIDDLE_REASSOC) {
            if (historyInfo.reassocSelfCureFailedCnt != 0 && ((historyInfo.reassocSelfCureFailedCnt != SIGNAL_LEVEL_1 || currentMs - historyInfo.lastReassocSelfCureFailedTs <= 86400000) && (historyInfo.reassocSelfCureFailedCnt != SIGNAL_LEVEL_2 || currentMs - historyInfo.lastReassocSelfCureFailedTs <= 259200000))) {
                if (historyInfo.reassocSelfCureFailedCnt >= SIGNAL_LEVEL_3 && currentMs - historyInfo.lastReassocSelfCureFailedTs > 432000000) {
                }
            }
            if (allowSelfCure(historyInfo, requestCureLevel)) {
                return true;
            }
        } else if (requestCureLevel == RESET_LEVEL_HIGH_RESET) {
            if (historyInfo.resetSelfCureFailedCnt > SIGNAL_LEVEL_1 && ((historyInfo.resetSelfCureFailedCnt != SIGNAL_LEVEL_2 || currentMs - historyInfo.lastResetSelfCureFailedTs <= 86400000) && (historyInfo.resetSelfCureFailedCnt != SIGNAL_LEVEL_3 || currentMs - historyInfo.lastResetSelfCureFailedTs <= 259200000))) {
                if (historyInfo.resetSelfCureFailedCnt >= SCE_WIFI_REASSOC_STATE && currentMs - historyInfo.lastResetSelfCureFailedTs > 432000000) {
                }
            }
            if (allowSelfCure(historyInfo, requestCureLevel)) {
                return true;
            }
        }
        return false;
    }

    private static boolean allowSelfCure(InternetSelfCureHistoryInfo historyInfo, int requestCureLevel) {
        if (historyInfo == null) {
            return false;
        }
        long currentMs = System.currentTimeMillis();
        if (requestCureLevel != RESET_LEVEL_MIDDLE_REASSOC) {
            return requestCureLevel == RESET_LEVEL_HIGH_RESET && (historyInfo.resetSelfCureConnectFailedCnt == 0 || (historyInfo.resetSelfCureConnectFailedCnt >= SIGNAL_LEVEL_1 && currentMs - historyInfo.lastResetSelfCureConnectFailedTs > 86400000));
        } else {
            if (historyInfo.reassocSelfCureConnectFailedCnt == 0 || (historyInfo.reassocSelfCureConnectFailedCnt >= SIGNAL_LEVEL_1 && currentMs - historyInfo.lastReassocSelfCureConnectFailedTs > 86400000)) {
                return true;
            }
        }
    }

    public static void updateSelfCureHistoryInfo(InternetSelfCureHistoryInfo historyInfo, int requestCureLevel, boolean success) {
        if (historyInfo != null) {
            long currentMs = System.currentTimeMillis();
            if (requestCureLevel == RESET_LEVEL_LOW_1_DNS) {
                if (success) {
                    historyInfo.dnsSelfCureFailedCnt = SCE_WIFI_STATUS_SUCC;
                    historyInfo.lastDnsSelfCureFailedTs = 0;
                } else {
                    historyInfo.dnsSelfCureFailedCnt += SIGNAL_LEVEL_1;
                    historyInfo.lastDnsSelfCureFailedTs = currentMs;
                }
            } else if (requestCureLevel == RESET_LEVEL_LOW_2_RENEW_DHCP) {
                if (success) {
                    historyInfo.renewDhcpSelfCureFailedCnt = SCE_WIFI_STATUS_SUCC;
                    historyInfo.lastRenewDhcpSelfCureFailedTs = 0;
                } else {
                    historyInfo.renewDhcpSelfCureFailedCnt += SIGNAL_LEVEL_1;
                    historyInfo.lastRenewDhcpSelfCureFailedTs = currentMs;
                }
            } else if (requestCureLevel == RESET_LEVEL_LOW_3_STATIC_IP) {
                if (success) {
                    historyInfo.staticIpSelfCureFailedCnt = SCE_WIFI_STATUS_SUCC;
                    historyInfo.lastStaticIpSelfCureFailedTs = 0;
                } else {
                    historyInfo.staticIpSelfCureFailedCnt += SIGNAL_LEVEL_1;
                    historyInfo.lastStaticIpSelfCureFailedTs = currentMs;
                }
            } else if (requestCureLevel == RESET_LEVEL_MIDDLE_REASSOC) {
                if (success) {
                    historyInfo.reassocSelfCureFailedCnt = SCE_WIFI_STATUS_SUCC;
                    historyInfo.lastReassocSelfCureFailedTs = 0;
                } else {
                    historyInfo.reassocSelfCureFailedCnt += SIGNAL_LEVEL_1;
                    historyInfo.lastReassocSelfCureFailedTs = currentMs;
                }
            } else if (requestCureLevel == RESET_LEVEL_HIGH_RESET) {
                if (success) {
                    historyInfo.resetSelfCureFailedCnt = SCE_WIFI_STATUS_SUCC;
                    historyInfo.lastResetSelfCureFailedTs = 0;
                } else {
                    historyInfo.resetSelfCureFailedCnt += SIGNAL_LEVEL_1;
                    historyInfo.lastResetSelfCureFailedTs = currentMs;
                }
            }
        }
    }

    public static void updateSelfCureConnectHistoryInfo(InternetSelfCureHistoryInfo historyInfo, int requestCureLevel, boolean success) {
        if (historyInfo != null) {
            long currentMs = System.currentTimeMillis();
            if (requestCureLevel == RESET_LEVEL_MIDDLE_REASSOC) {
                if (success) {
                    historyInfo.reassocSelfCureConnectFailedCnt = SCE_WIFI_STATUS_SUCC;
                    historyInfo.lastReassocSelfCureConnectFailedTs = 0;
                } else {
                    historyInfo.reassocSelfCureConnectFailedCnt += SIGNAL_LEVEL_1;
                    historyInfo.lastReassocSelfCureConnectFailedTs = currentMs;
                }
            } else if (requestCureLevel == RESET_LEVEL_HIGH_RESET) {
                if (success) {
                    historyInfo.resetSelfCureConnectFailedCnt = SCE_WIFI_STATUS_SUCC;
                    historyInfo.lastResetSelfCureConnectFailedTs = 0;
                } else {
                    historyInfo.resetSelfCureConnectFailedCnt += SIGNAL_LEVEL_1;
                    historyInfo.lastResetSelfCureConnectFailedTs = currentMs;
                }
            }
        }
    }

    public static boolean isWifiProEnabled() {
        return WifiHandover.isWifiProEnabled();
    }

    public static boolean isOnWlanSettings(Context context) {
        if (context != null) {
            return WifiProCommonUtils.isQueryActivityMatched(context, "com.android.settings.Settings$WifiSettingsActivity");
        }
        return false;
    }
}
