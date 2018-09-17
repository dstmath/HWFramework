package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifipro.WifiProCommonUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
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
    public static final String DNS_ERR_MONITOR_FLAG = "hw.wifipro.dns_err_count";
    private static final int DNS_ERR_REFUSED_IDX = 5;
    public static final String DNS_MONITOR_FLAG = "hw.wifipro.dns_fail_count";
    private static final int INTERVAL_SELF_CURE = 60000;
    public static final int MAX_FAILED_CURE = 3;
    public static final int MIN_TCP_FAILED_THRESHOLD = 3;
    private static final String[] PUBLIC_DNS_CHINA = new String[]{"180.76.76.76", "223.5.5.5"};
    private static final String[] PUBLIC_DNS_OVERSEA = new String[]{"8.8.8.8", "208.67.222.222"};
    public static final int RESET_LEVEL_DEAUTH_BSSID = 208;
    public static final int RESET_LEVEL_HIGH_RESET = 205;
    public static final int RESET_LEVEL_IDLE = 200;
    public static final int RESET_LEVEL_LOW_1_DNS = 201;
    public static final int RESET_LEVEL_LOW_2_RENEW_DHCP = 202;
    public static final int RESET_LEVEL_LOW_3_STATIC_IP = 203;
    public static final int RESET_LEVEL_MIDDLE_REASSOC = 204;
    public static final int RESET_LEVEL_RECONNECT_4_INVALID_IP = 207;
    public static final int RESET_REJECTED_BY_STATIC_IP_ENABLED = 206;
    public static final int SCE_WIFI_CONNECT_STATE = 3;
    public static final int SCE_WIFI_CONNET_RETRY = 1;
    public static final int SCE_WIFI_DISABLED_DELAY = 200;
    public static final int SCE_WIFI_OFF_STATE = 1;
    public static final int SCE_WIFI_ON_STATE = 2;
    public static final int SCE_WIFI_REASSOC_STATE = 4;
    public static final int SCE_WIFI_RECONNECT_STATE = 5;
    public static final int SCE_WIFI_STATUS_ABRORT = -3;
    public static final int SCE_WIFI_STATUS_FAIL = -1;
    public static final int SCE_WIFI_STATUS_LOST = -2;
    public static final int SCE_WIFI_STATUS_SUCC = 0;
    public static final int SCE_WIFI_STATUS_UNKOWN = 1;
    public static final int SELFCURE_WIFI_CONNECT_TIMEOUT = 15000;
    public static final int SELFCURE_WIFI_OFF_TIMEOUT = 2000;
    public static final int SELFCURE_WIFI_ON_TIMEOUT = 3000;
    public static final int SELFCURE_WIFI_REASSOC_TIMEOUT = 12000;
    public static final int SELFCURE_WIFI_RECONNECT_TIMEOUT = 15000;
    public static final int SIGNAL_LEVEL_1 = 1;
    public static final int SIGNAL_LEVEL_2 = 2;
    public static final int SIGNAL_LEVEL_3 = 3;
    public static final int WIFIPRO_SOFT_CONNECT_FAILED = -4;
    public static final String WIFI_STAT_FILE = "proc/net/wifi_network_stat";

    public static int getCurrentDnsFailedCounter() {
        int counter = 0;
        try {
            return Integer.parseInt(SystemProperties.get(DNS_MONITOR_FLAG, "0"));
        } catch (NumberFormatException e) {
            return counter;
        }
    }

    public static int getCurrentDnsRefuseCounter() {
        try {
            String[] counterStr = SystemProperties.get("hw.wifipro.dns_err_count", "0").split(",");
            if (counterStr.length > 5) {
                return Integer.parseInt(counterStr[5]);
            }
            return 0;
        } catch (NumberFormatException e) {
            return 0;
        }
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
            for (int i = 0; i < scanResults.size(); i++) {
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
            for (int i = 0; i < scanResults.size(); i++) {
                ScanResult nextResult = (ScanResult) scanResults.get(i);
                if (nextResult.level >= -75) {
                    String scanSsid = "\"" + nextResult.SSID + "\"";
                    String scanResultEncrypt = nextResult.capabilities;
                    int j = 0;
                    while (j < savedNetworks.size()) {
                        WifiConfiguration nextConfig = (WifiConfiguration) savedNetworks.get(j);
                        if (nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, nextConfig.configKey())) {
                            NetworkSelectionStatus status = nextConfig.getNetworkSelectionStatus();
                            int disableReason = status.getNetworkSelectionDisableReason();
                            if (!(status.isNetworkEnabled() || disableReason < 1 || disableReason > 10 || disableReason == 7 || disableReason == 8)) {
                                autoConnectFailedNetworks.put(nextConfig.configKey(), nextConfig);
                                autoConnectFailedNetworksRssi.put(nextConfig.configKey(), Integer.valueOf(nextResult.level));
                            }
                            if (!autoConnectFailedNetworks.containsKey(nextConfig.configKey()) && wsm.isBssidDisabled(nextResult.BSSID)) {
                                autoConnectFailedNetworks.put(nextConfig.configKey(), nextConfig);
                                autoConnectFailedNetworksRssi.put(nextConfig.configKey(), Integer.valueOf(nextResult.level));
                            }
                        } else {
                            j++;
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
            if (currCureHistory == null || (currCureHistory.cureFailedCounter != 3 && System.currentTimeMillis() - currCureHistory.lastCureFailedTime >= 60000)) {
                if (bestSelfCureCandidate == null) {
                    bestSelfCureCandidate = currConfig;
                    bestSelfCureLevel = currLevel;
                    bestCureHistory = currCureHistory;
                } else if (bestCureHistory == null && currCureHistory == null) {
                    if (!currConfig.noInternetAccess && (currConfig.portalNetwork ^ 1) != 0 && (bestSelfCureCandidate.noInternetAccess || currConfig.portalNetwork)) {
                        bestSelfCureCandidate = currConfig;
                        bestSelfCureLevel = currLevel;
                    } else if ((bestSelfCureCandidate.noInternetAccess || (bestSelfCureCandidate.portalNetwork ^ 1) == 0 || !(currConfig.noInternetAccess || currConfig.portalNetwork)) && currLevel > bestSelfCureLevel) {
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
                int i = 0;
                while (i < histories.length) {
                    try {
                        if (i == 0) {
                            info.dnsSelfCureFailedCnt = Integer.valueOf(histories[i]).intValue();
                        } else if (i == 1) {
                            info.lastDnsSelfCureFailedTs = Long.valueOf(histories[i]).longValue();
                        } else if (i == 2) {
                            info.renewDhcpSelfCureFailedCnt = Integer.valueOf(histories[i]).intValue();
                        } else if (i == 3) {
                            info.lastRenewDhcpSelfCureFailedTs = Long.valueOf(histories[i]).longValue();
                        } else if (i == 4) {
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
                        i++;
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
            if (historyInfo.dnsSelfCureFailedCnt == 0 || ((historyInfo.dnsSelfCureFailedCnt == 1 && currentMs - historyInfo.lastDnsSelfCureFailedTs > 86400000) || ((historyInfo.dnsSelfCureFailedCnt == 2 && currentMs - historyInfo.lastDnsSelfCureFailedTs > 259200000) || (historyInfo.dnsSelfCureFailedCnt >= 3 && currentMs - historyInfo.lastDnsSelfCureFailedTs > 432000000)))) {
                return true;
            }
        } else if (requestCureLevel == RESET_LEVEL_LOW_2_RENEW_DHCP) {
            if (historyInfo.renewDhcpSelfCureFailedCnt >= 0) {
                return true;
            }
        } else if (requestCureLevel == RESET_LEVEL_LOW_3_STATIC_IP) {
            if (historyInfo.staticIpSelfCureFailedCnt <= 4 || ((historyInfo.staticIpSelfCureFailedCnt == 5 && currentMs - historyInfo.lastStaticIpSelfCureFailedTs > 86400000) || ((historyInfo.staticIpSelfCureFailedCnt == 6 && currentMs - historyInfo.lastStaticIpSelfCureFailedTs > 259200000) || (historyInfo.staticIpSelfCureFailedCnt >= 7 && currentMs - historyInfo.lastStaticIpSelfCureFailedTs > 432000000)))) {
                return true;
            }
        } else if (requestCureLevel != RESET_LEVEL_MIDDLE_REASSOC) {
            return requestCureLevel == RESET_LEVEL_HIGH_RESET && ((historyInfo.resetSelfCureFailedCnt <= 1 || ((historyInfo.resetSelfCureFailedCnt == 2 && currentMs - historyInfo.lastResetSelfCureFailedTs > 86400000) || ((historyInfo.resetSelfCureFailedCnt == 3 && currentMs - historyInfo.lastResetSelfCureFailedTs > 259200000) || (historyInfo.resetSelfCureFailedCnt >= 4 && currentMs - historyInfo.lastResetSelfCureFailedTs > 432000000)))) && allowSelfCure(historyInfo, requestCureLevel));
        } else {
            if ((historyInfo.reassocSelfCureFailedCnt == 0 || ((historyInfo.reassocSelfCureFailedCnt == 1 && currentMs - historyInfo.lastReassocSelfCureFailedTs > 86400000) || ((historyInfo.reassocSelfCureFailedCnt == 2 && currentMs - historyInfo.lastReassocSelfCureFailedTs > 259200000) || (historyInfo.reassocSelfCureFailedCnt >= 3 && currentMs - historyInfo.lastReassocSelfCureFailedTs > 432000000)))) && allowSelfCure(historyInfo, requestCureLevel)) {
                return true;
            }
        }
    }

    private static boolean allowSelfCure(InternetSelfCureHistoryInfo historyInfo, int requestCureLevel) {
        if (historyInfo == null) {
            return false;
        }
        long currentMs = System.currentTimeMillis();
        if (requestCureLevel != RESET_LEVEL_MIDDLE_REASSOC) {
            return requestCureLevel == RESET_LEVEL_HIGH_RESET && (historyInfo.resetSelfCureConnectFailedCnt == 0 || (historyInfo.resetSelfCureConnectFailedCnt >= 1 && currentMs - historyInfo.lastResetSelfCureConnectFailedTs > 86400000));
        } else {
            if (historyInfo.reassocSelfCureConnectFailedCnt == 0 || (historyInfo.reassocSelfCureConnectFailedCnt >= 1 && currentMs - historyInfo.lastReassocSelfCureConnectFailedTs > 86400000)) {
                return true;
            }
        }
    }

    public static void updateSelfCureHistoryInfo(InternetSelfCureHistoryInfo historyInfo, int requestCureLevel, boolean success) {
        if (historyInfo != null) {
            long currentMs = System.currentTimeMillis();
            if (requestCureLevel == RESET_LEVEL_LOW_1_DNS) {
                if (success) {
                    historyInfo.dnsSelfCureFailedCnt = 0;
                    historyInfo.lastDnsSelfCureFailedTs = 0;
                } else {
                    historyInfo.dnsSelfCureFailedCnt++;
                    historyInfo.lastDnsSelfCureFailedTs = currentMs;
                }
            } else if (requestCureLevel == RESET_LEVEL_LOW_2_RENEW_DHCP || requestCureLevel == RESET_LEVEL_DEAUTH_BSSID) {
                if (success) {
                    historyInfo.renewDhcpSelfCureFailedCnt = 0;
                    historyInfo.lastRenewDhcpSelfCureFailedTs = 0;
                } else {
                    historyInfo.renewDhcpSelfCureFailedCnt++;
                    historyInfo.lastRenewDhcpSelfCureFailedTs = currentMs;
                }
            } else if (requestCureLevel == RESET_LEVEL_LOW_3_STATIC_IP) {
                if (success) {
                    historyInfo.staticIpSelfCureFailedCnt = 0;
                    historyInfo.lastStaticIpSelfCureFailedTs = 0;
                } else {
                    historyInfo.staticIpSelfCureFailedCnt++;
                    historyInfo.lastStaticIpSelfCureFailedTs = currentMs;
                }
            } else if (requestCureLevel == RESET_LEVEL_MIDDLE_REASSOC) {
                if (success) {
                    historyInfo.reassocSelfCureFailedCnt = 0;
                    historyInfo.lastReassocSelfCureFailedTs = 0;
                } else {
                    historyInfo.reassocSelfCureFailedCnt++;
                    historyInfo.lastReassocSelfCureFailedTs = currentMs;
                }
            } else if (requestCureLevel == RESET_LEVEL_HIGH_RESET) {
                if (success) {
                    historyInfo.resetSelfCureFailedCnt = 0;
                    historyInfo.lastResetSelfCureFailedTs = 0;
                } else {
                    historyInfo.resetSelfCureFailedCnt++;
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
                    historyInfo.reassocSelfCureConnectFailedCnt = 0;
                    historyInfo.lastReassocSelfCureConnectFailedTs = 0;
                } else {
                    historyInfo.reassocSelfCureConnectFailedCnt++;
                    historyInfo.lastReassocSelfCureConnectFailedTs = currentMs;
                }
            } else if (requestCureLevel == RESET_LEVEL_HIGH_RESET) {
                if (success) {
                    historyInfo.resetSelfCureConnectFailedCnt = 0;
                    historyInfo.lastResetSelfCureConnectFailedTs = 0;
                } else {
                    historyInfo.resetSelfCureConnectFailedCnt++;
                    historyInfo.lastResetSelfCureConnectFailedTs = currentMs;
                }
            }
        }
    }

    public static InetAddress getNextIpAddr(InetAddress gateway, InetAddress currentAddr, ArrayList<InetAddress> testedAddr) {
        if (!(gateway == null || currentAddr == null || testedAddr == null)) {
            int newip = -1;
            int getCnt = 1;
            byte[] ipAddr = currentAddr.getAddress();
            Log.d("HwSelfCureEngine", "getNextIpAddr, gateway = " + gateway.getHostAddress());
            Log.d("HwSelfCureEngine", "getNextIpAddr, currentAddr = " + currentAddr.getHostAddress());
            while (true) {
                int i = getCnt;
                getCnt = i + 1;
                if (i >= 10) {
                    break;
                }
                boolean reduplicate = false;
                newip = new SecureRandom().nextInt(100) + 101;
                if (newip != (gateway.getAddress()[3] & 255)) {
                    if (newip != (ipAddr[3] & 255)) {
                        for (int i2 = 0; i2 < testedAddr.size(); i2++) {
                            Log.d("HwSelfCureEngine", "getNextIpAddr, testedAddr = " + ((InetAddress) testedAddr.get(i2)).getHostAddress());
                            if (newip == (((InetAddress) testedAddr.get(i2)).getAddress()[3] & 255)) {
                                reduplicate = true;
                                break;
                            }
                        }
                        if (newip > 0 && (reduplicate ^ 1) != 0) {
                            break;
                        }
                    }
                }
            }
            if (newip > 1 && newip <= 250 && getCnt < 10) {
                ipAddr[3] = (byte) newip;
                try {
                    return InetAddress.getByAddress(ipAddr);
                } catch (UnknownHostException e) {
                    Log.d("HwSelfCureEngine", "getNextIpAddr UnknownHostException!");
                }
            }
        }
        return null;
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
