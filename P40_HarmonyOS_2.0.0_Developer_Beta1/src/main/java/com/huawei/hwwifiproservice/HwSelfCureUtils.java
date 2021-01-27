package com.huawei.hwwifiproservice;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.hwwifiproservice.HwSelfCureEngine;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwSelfCureUtils {
    private static final int CHINA_CONFIG_CNT = 4;
    private static final int CONFIG_CNT = 2;
    private static final int CURE_OUT_OF_DATE_MS = 7200000;
    public static final int DELAYED_DAYS_HIGH = 432000000;
    public static final int DELAYED_DAYS_LOW = 86400000;
    public static final int DELAYED_DAYS_MID = 259200000;
    public static final String DNS_CURE_IP_FLAG = "ro.config.dnscure_ipcfg";
    public static final String DNS_ERR_MONITOR_FLAG = "hw.wifipro.dns_err_count";
    private static final int DNS_ERR_REFUSED_IDX = 5;
    public static final String DNS_MONITOR_FLAG = "hw.wifipro.dns_fail_count";
    private static final int HEXADECIMAL = 16;
    private static final int INDEX_START = 0;
    private static final int INTERVAL_SELF_CURE = 60000;
    public static final int MAX_FAILED_CURE = 3;
    public static final int MIN_TCP_FAILED_THRESHOLD = 3;
    private static final int OVERSEA_CONFIG_CNT = 2;
    public static final int RESET_LEVEL_DEAUTH_BSSID = 208;
    public static final int RESET_LEVEL_HIGH_RESET = 205;
    public static final int RESET_LEVEL_IDLE = 200;
    public static final int RESET_LEVEL_LOW_1_DNS = 201;
    public static final int RESET_LEVEL_LOW_2_RENEW_DHCP = 202;
    public static final int RESET_LEVEL_LOW_3_STATIC_IP = 203;
    public static final int RESET_LEVEL_MIDDLE_REASSOC = 204;
    public static final int RESET_LEVEL_RAND_MAC_REASSOC = 209;
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
    public static final int SCE_WIFI_STATUS_REJECT = -5;
    public static final int SCE_WIFI_STATUS_SUCC = 0;
    public static final int SCE_WIFI_STATUS_UNKOWN = 1;
    private static final int SELFCURE_FAIL_LENGTH = 12;
    private static final int SELFCURE_HISTORY_LENGTH = 18;
    public static final int SELFCURE_WIFI_CONNECT_TIMEOUT = 15000;
    public static final int SELFCURE_WIFI_OFF_TIMEOUT = 2000;
    public static final int SELFCURE_WIFI_ON_TIMEOUT = 3000;
    public static final int SELFCURE_WIFI_REASSOC_TIMEOUT = 12000;
    public static final int SELFCURE_WIFI_RECONNECT_TIMEOUT = 15000;
    public static final int SELF_CURE_RAND_MAC_MAX_COUNT = 20;
    public static final int SIGNAL_LEVEL_1 = 1;
    public static final int SIGNAL_LEVEL_2 = 2;
    public static final int SIGNAL_LEVEL_3 = 3;
    private static final String TAG = "HwSelfCureEngine";
    public static final int WIFI6_MAX_BLACKLIST_NUM = 16;
    private static final int WIFI6_SINGLE_ITEM_BYTE_LEN = 8;
    private static final int WIFI6_SINGLE_MAC_LEN = 6;
    public static final int WIFIPRO_SOFT_CONNECT_FAILED = -4;
    public static final String WIFI_STAT_FILE = "proc/net/wifi_network_stat";
    private static String[] sChinaPublicDnses = {"", ""};
    private static String[] sOverseaPublicDnses = {"", ""};

    public static int getCurrentDnsFailedCounter() {
        try {
            return Integer.parseInt(SystemProperties.get(DNS_MONITOR_FLAG, "0"));
        } catch (NumberFormatException e) {
            HwHiLog.e(TAG, false, "Exception happened in getCurrentDnsFailedCounter()", new Object[0]);
            return 0;
        }
    }

    public static int getCurrentDnsRefuseCounter() {
        try {
            String[] counterStr = SystemProperties.get(DNS_ERR_MONITOR_FLAG, "0").split(",");
            if (counterStr.length > 5) {
                return Integer.parseInt(counterStr[5]);
            }
            return 0;
        } catch (NumberFormatException e) {
            HwHiLog.e(TAG, false, "Exception happened in getCurrentDnsRefuseCounter()", new Object[0]);
            return 0;
        }
    }

    public static List<String> getRefreshedCureFailedNetworks(Map<String, HwSelfCureEngine.CureFailedNetworkInfo> networkCureFailedHistory) {
        List<String> refreshedNetworksKey = new ArrayList<>();
        for (Map.Entry<String, HwSelfCureEngine.CureFailedNetworkInfo> entry : networkCureFailedHistory.entrySet()) {
            String currKey = entry.getKey();
            if (System.currentTimeMillis() - networkCureFailedHistory.get(currKey).lastCureFailedTime > 7200000) {
                refreshedNetworksKey.add(currKey);
            }
        }
        return refreshedNetworksKey;
    }

    public static List<String> searchUnstableNetworks(Map<String, WifiConfiguration> autoConnectFailedNetworks, List<ScanResult> scanResults) {
        List<String> unstableKey = new ArrayList<>();
        if (scanResults == null || scanResults.size() == 0) {
            return unstableKey;
        }
        for (Map.Entry<String, WifiConfiguration> entry : autoConnectFailedNetworks.entrySet()) {
            String currKey = entry.getKey();
            WifiConfiguration currConfig = entry.getValue();
            boolean outOfRange = true;
            Iterator<ScanResult> it = scanResults.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ScanResult itemScanResult = it.next();
                if (HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(itemScanResult.frequency, itemScanResult.level) > 2) {
                    String scanSsid = "\"" + itemScanResult.SSID + "\"";
                    String scanResultEncrypt = itemScanResult.capabilities;
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

    public static void selectDisabledNetworks(List<ScanResult> scanResults, List<WifiConfiguration> savedNetworks, Map<String, WifiConfiguration> autoConnectFailedNetworks, Map<String, Integer> autoConnectFailedNetworksRssi) {
        List<WifiConfiguration> disabledNetworks;
        Map<String, WifiConfiguration> map = autoConnectFailedNetworks;
        List<WifiConfiguration> disabledNetworks2 = new ArrayList<>();
        if (!(scanResults == null || scanResults.size() == 0 || savedNetworks == null)) {
            if (savedNetworks.size() != 0) {
                for (ScanResult itemScanResult : scanResults) {
                    if (HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(itemScanResult.frequency, itemScanResult.level) > 2) {
                        String scanSsid = "\"" + itemScanResult.SSID + "\"";
                        String scanResultEncrypt = itemScanResult.capabilities;
                        Iterator<WifiConfiguration> it = savedNetworks.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                disabledNetworks = disabledNetworks2;
                                break;
                            }
                            WifiConfiguration itemNetworks = it.next();
                            if (itemNetworks.SSID == null || !itemNetworks.SSID.equals(scanSsid) || !WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, itemNetworks.configKey())) {
                                map = autoConnectFailedNetworks;
                                disabledNetworks2 = disabledNetworks2;
                            } else {
                                WifiConfiguration.NetworkSelectionStatus status = itemNetworks.getNetworkSelectionStatus();
                                int disableReason = status.getNetworkSelectionDisableReason();
                                if (!status.isNetworkEnabled() && disableReason >= 1 && disableReason <= 11 && disableReason != 8 && disableReason != 9) {
                                    map.put(itemNetworks.configKey(), itemNetworks);
                                    autoConnectFailedNetworksRssi.put(itemNetworks.configKey(), Integer.valueOf(itemScanResult.level));
                                }
                                Bundle data = new Bundle();
                                data.putString("BSSID", itemScanResult.BSSID);
                                disabledNetworks = disabledNetworks2;
                                Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 30, data);
                                boolean isBssidDisabled = false;
                                if (result != null) {
                                    isBssidDisabled = result.getBoolean("isBssidDisabled");
                                }
                                if (!map.containsKey(itemNetworks.configKey()) && isBssidDisabled) {
                                    map.put(itemNetworks.configKey(), itemNetworks);
                                    autoConnectFailedNetworksRssi.put(itemNetworks.configKey(), Integer.valueOf(itemScanResult.level));
                                }
                            }
                        }
                        map = autoConnectFailedNetworks;
                        disabledNetworks2 = disabledNetworks;
                    }
                }
            }
        }
    }

    public static WifiConfiguration selectHighestFailedNetwork(Map<String, HwSelfCureEngine.CureFailedNetworkInfo> networkCureFailedHistory, Map<String, WifiConfiguration> autoConnectFailedNetworks, Map<String, Integer> autoConnectFailedNetworksRssi) {
        WifiConfiguration bestSelfCureCandidate = null;
        int bestSelfCureLevel = -200;
        HwSelfCureEngine.CureFailedNetworkInfo bestCureHistory = null;
        for (Map.Entry<String, WifiConfiguration> entry : autoConnectFailedNetworks.entrySet()) {
            String currKey = entry.getKey();
            WifiConfiguration currConfig = entry.getValue();
            int currLevel = autoConnectFailedNetworksRssi.get(currKey).intValue();
            HwSelfCureEngine.CureFailedNetworkInfo currCureHistory = networkCureFailedHistory.get(currKey);
            if (currCureHistory == null || (currCureHistory.cureFailedCounter != 3 && System.currentTimeMillis() - currCureHistory.lastCureFailedTime >= 60000)) {
                if (bestSelfCureCandidate == null) {
                    bestSelfCureCandidate = currConfig;
                    bestSelfCureLevel = currLevel;
                    bestCureHistory = currCureHistory;
                } else if (bestCureHistory == null && currCureHistory == null) {
                    if (!currConfig.noInternetAccess && !currConfig.portalNetwork && (bestSelfCureCandidate.noInternetAccess || currConfig.portalNetwork)) {
                        bestSelfCureCandidate = currConfig;
                        bestSelfCureLevel = currLevel;
                    } else if ((bestSelfCureCandidate.noInternetAccess || bestSelfCureCandidate.portalNetwork || (!currConfig.noInternetAccess && !currConfig.portalNetwork)) && currLevel > bestSelfCureLevel) {
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

    public static boolean isSplitSuccess(String source, String[] array) {
        if (source.isEmpty()) {
            return false;
        }
        String[] parts = source.split(";");
        if (!(parts.length == 2 || parts.length == 4)) {
            return false;
        }
        for (int i = 0; i < parts.length; i++) {
            array[i] = parts[i];
        }
        return true;
    }

    public static void initDnsServer() {
        String[] publicDnsParts = SystemProperties.get(DNS_CURE_IP_FLAG, "").split("\\|");
        if (publicDnsParts.length != 2) {
            HwHiLog.e(TAG, false, "Failed to get PublicDnsServers", new Object[0]);
            return;
        }
        String[] publicDnses = {"", "", "", ""};
        if (!isSplitSuccess(publicDnsParts[1], publicDnses)) {
            HwHiLog.e(TAG, false, "initDnsServer: isSplitSuccess failed!", new Object[0]);
        }
        String[] strArr = sOverseaPublicDnses;
        strArr[0] = publicDnses[0];
        strArr[1] = publicDnses[1];
        String[] strArr2 = sChinaPublicDnses;
        strArr2[0] = publicDnses[2];
        strArr2[1] = publicDnses[3];
        HwHiLog.i(TAG, false, "initDnsServer success!", new Object[0]);
    }

    public static ArrayList<String> getPublicDnsServers() {
        if (WifiProCommonUtils.useOperatorOverSea() || TextUtils.isEmpty(sChinaPublicDnses[0])) {
            return new ArrayList<>(Arrays.asList(sOverseaPublicDnses));
        }
        return new ArrayList<>(Arrays.asList(sChinaPublicDnses));
    }

    public static ArrayList<String> getReplacedDnsServers(String[] curDnses) {
        String[] replaceDnses;
        if (curDnses == null || curDnses.length == 0) {
            return new ArrayList<>(0);
        }
        if (curDnses.length == 1) {
            replaceDnses = (String[]) Arrays.copyOf(curDnses, curDnses.length + 1);
        } else {
            replaceDnses = (String[]) curDnses.clone();
        }
        replaceDnses[1] = getPublicDnsServers().get(0);
        return new ArrayList<>(Arrays.asList(replaceDnses));
    }

    public static void requestUpdateDnsServers(ArrayList<String> dnsServers) {
        Bundle data = new Bundle();
        data.putStringArrayList("dnsServers", dnsServers);
        WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 54, data);
    }

    private static void setSelfCureFailInfo(HwSelfCureEngine.InternetSelfCureHistoryInfo info, String[] histories, int cnt) {
        if (info != null && histories != null && histories.length == 18 && cnt == 12) {
            for (int i = 0; i < cnt; i++) {
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
                    info.randMacSelfCureFailedCnt = Integer.valueOf(histories[i]).intValue();
                } else if (i == 9) {
                    info.lastRandMacSelfCureFailedCntTs = Long.valueOf(histories[i]).longValue();
                } else if (i == 10) {
                    info.resetSelfCureFailedCnt = Integer.valueOf(histories[i]).intValue();
                } else if (i == 11) {
                    info.lastResetSelfCureFailedTs = Long.valueOf(histories[i]).longValue();
                } else {
                    HwHiLog.e(TAG, false, "exception happen", new Object[0]);
                }
            }
        }
    }

    private static void setSelfCureConnectFailInfo(HwSelfCureEngine.InternetSelfCureHistoryInfo info, String[] histories, int cnt) {
        if (info == null || histories == null || histories.length != 18 || cnt != 12) {
            HwHiLog.e(TAG, false, "setSelfCureConnectFailInfo return", new Object[0]);
            return;
        }
        for (int i = cnt; i < 18; i++) {
            if (i == 12) {
                info.reassocSelfCureConnectFailedCnt = Integer.valueOf(histories[i]).intValue();
            } else if (i == 13) {
                info.lastReassocSelfCureConnectFailedTs = Long.valueOf(histories[i]).longValue();
            } else if (i == 14) {
                info.randMacSelfCureConnectFailedCnt = Integer.valueOf(histories[i]).intValue();
            } else if (i == 15) {
                info.lastRandMacSelfCureConnectFailedCntTs = Long.valueOf(histories[i]).longValue();
            } else if (i == 16) {
                info.resetSelfCureConnectFailedCnt = Integer.valueOf(histories[i]).intValue();
            } else if (i == 17) {
                info.lastResetSelfCureConnectFailedTs = Long.valueOf(histories[i]).longValue();
            }
        }
    }

    public static HwSelfCureEngine.InternetSelfCureHistoryInfo string2InternetSelfCureHistoryInfo(String selfCureHistory) {
        HwSelfCureEngine.InternetSelfCureHistoryInfo info = new HwSelfCureEngine.InternetSelfCureHistoryInfo();
        if (selfCureHistory != null && selfCureHistory.length() > 0) {
            String[] histories = selfCureHistory.split("\\|");
            if (histories.length == 18) {
                try {
                    setSelfCureFailInfo(info, histories, 12);
                    setSelfCureConnectFailInfo(info, histories, 12);
                } catch (IllegalArgumentException e) {
                    HwHiLog.e(TAG, false, "Exception happened in string2InternetSelfCureHistoryInfo()", new Object[0]);
                }
            }
        }
        return info;
    }

    public static String internetSelfCureHistoryInfo2String(HwSelfCureEngine.InternetSelfCureHistoryInfo info) {
        StringBuilder strHistory = new StringBuilder();
        if (info != null) {
            strHistory.append(String.valueOf(info.dnsSelfCureFailedCnt) + "|");
            strHistory.append(String.valueOf(info.lastDnsSelfCureFailedTs) + "|");
            strHistory.append(String.valueOf(info.renewDhcpSelfCureFailedCnt) + "|");
            strHistory.append(String.valueOf(info.lastRenewDhcpSelfCureFailedTs) + "|");
            strHistory.append(String.valueOf(info.staticIpSelfCureFailedCnt) + "|");
            strHistory.append(String.valueOf(info.lastStaticIpSelfCureFailedTs) + "|");
            strHistory.append(String.valueOf(info.reassocSelfCureFailedCnt) + "|");
            strHistory.append(String.valueOf(info.lastReassocSelfCureFailedTs) + "|");
            strHistory.append(String.valueOf(info.randMacSelfCureFailedCnt) + "|");
            strHistory.append(String.valueOf(info.lastRandMacSelfCureFailedCntTs) + "|");
            strHistory.append(String.valueOf(info.resetSelfCureFailedCnt) + "|");
            strHistory.append(String.valueOf(info.lastResetSelfCureFailedTs) + "|");
            strHistory.append(String.valueOf(info.reassocSelfCureConnectFailedCnt) + "|");
            strHistory.append(String.valueOf(info.lastReassocSelfCureConnectFailedTs) + "|");
            strHistory.append(String.valueOf(info.randMacSelfCureConnectFailedCnt) + "|");
            strHistory.append(String.valueOf(info.lastRandMacSelfCureConnectFailedCntTs) + "|");
            strHistory.append(String.valueOf(info.resetSelfCureConnectFailedCnt) + "|");
            strHistory.append(String.valueOf(info.lastResetSelfCureConnectFailedTs));
        }
        return strHistory.toString();
    }

    public static boolean selectedSelfCureAcceptable(HwSelfCureEngine.InternetSelfCureHistoryInfo historyInfo, int requestCureLevel) {
        if (historyInfo == null) {
            return false;
        }
        long currentMs = System.currentTimeMillis();
        if (requestCureLevel == 201) {
            if (historyInfo.dnsSelfCureFailedCnt == 0) {
                return true;
            }
            if (historyInfo.dnsSelfCureFailedCnt == 1 && currentMs - historyInfo.lastDnsSelfCureFailedTs > 86400000) {
                return true;
            }
            if (historyInfo.dnsSelfCureFailedCnt == 2 && currentMs - historyInfo.lastDnsSelfCureFailedTs > 259200000) {
                return true;
            }
            if (historyInfo.dnsSelfCureFailedCnt >= 3 && currentMs - historyInfo.lastDnsSelfCureFailedTs > 432000000) {
                return true;
            }
        } else if (requestCureLevel == 202) {
            if (historyInfo.renewDhcpSelfCureFailedCnt >= 0) {
                return true;
            }
        } else if (requestCureLevel == 203) {
            if (historyInfo.staticIpSelfCureFailedCnt <= 4) {
                return true;
            }
            if (historyInfo.staticIpSelfCureFailedCnt == 5 && currentMs - historyInfo.lastStaticIpSelfCureFailedTs > 86400000) {
                return true;
            }
            if (historyInfo.staticIpSelfCureFailedCnt == 6 && currentMs - historyInfo.lastStaticIpSelfCureFailedTs > 259200000) {
                return true;
            }
            if (historyInfo.staticIpSelfCureFailedCnt >= 7 && currentMs - historyInfo.lastStaticIpSelfCureFailedTs > 432000000) {
                return true;
            }
        } else if (requestCureLevel == 204) {
            if ((historyInfo.reassocSelfCureFailedCnt == 0 || ((historyInfo.reassocSelfCureFailedCnt == 1 && currentMs - historyInfo.lastReassocSelfCureFailedTs > 86400000) || ((historyInfo.reassocSelfCureFailedCnt == 2 && currentMs - historyInfo.lastReassocSelfCureFailedTs > 259200000) || (historyInfo.reassocSelfCureFailedCnt >= 3 && currentMs - historyInfo.lastReassocSelfCureFailedTs > 432000000)))) && allowSelfCure(historyInfo, requestCureLevel)) {
                return true;
            }
        } else if (requestCureLevel == 209) {
            if (historyInfo.randMacSelfCureFailedCnt < 20) {
                return true;
            }
        } else if (requestCureLevel == 205 && ((historyInfo.resetSelfCureFailedCnt <= 1 || ((historyInfo.resetSelfCureFailedCnt == 2 && currentMs - historyInfo.lastResetSelfCureFailedTs > 86400000) || ((historyInfo.resetSelfCureFailedCnt == 3 && currentMs - historyInfo.lastResetSelfCureFailedTs > 259200000) || (historyInfo.resetSelfCureFailedCnt >= 4 && currentMs - historyInfo.lastResetSelfCureFailedTs > 432000000)))) && allowSelfCure(historyInfo, requestCureLevel))) {
            return true;
        }
        return false;
    }

    private static boolean allowSelfCure(HwSelfCureEngine.InternetSelfCureHistoryInfo historyInfo, int requestCureLevel) {
        if (historyInfo == null) {
            return false;
        }
        long currentMs = System.currentTimeMillis();
        if (requestCureLevel == 204) {
            if (historyInfo.reassocSelfCureConnectFailedCnt == 0 || (historyInfo.reassocSelfCureConnectFailedCnt >= 1 && currentMs - historyInfo.lastReassocSelfCureConnectFailedTs > 86400000)) {
                return true;
            }
        } else if (requestCureLevel == 205 && (historyInfo.resetSelfCureConnectFailedCnt == 0 || (historyInfo.resetSelfCureConnectFailedCnt >= 1 && currentMs - historyInfo.lastResetSelfCureConnectFailedTs > 86400000))) {
            return true;
        }
        return false;
    }

    private static void updateReassocAndResetHistoryInfo(HwSelfCureEngine.InternetSelfCureHistoryInfo historyInfo, int requestCureLevel, boolean isSuccess) {
        if (historyInfo != null) {
            long currentMs = System.currentTimeMillis();
            if (requestCureLevel == 204) {
                if (isSuccess) {
                    historyInfo.reassocSelfCureFailedCnt = 0;
                    historyInfo.lastReassocSelfCureFailedTs = 0;
                    return;
                }
                historyInfo.reassocSelfCureFailedCnt++;
                historyInfo.lastReassocSelfCureFailedTs = currentMs;
            } else if (requestCureLevel == 209) {
                if (isSuccess) {
                    historyInfo.randMacSelfCureFailedCnt = 0;
                    historyInfo.lastRandMacSelfCureFailedCntTs = 0;
                    return;
                }
                historyInfo.randMacSelfCureFailedCnt++;
                historyInfo.lastRandMacSelfCureFailedCntTs = currentMs;
            } else if (requestCureLevel != 205) {
            } else {
                if (isSuccess) {
                    historyInfo.resetSelfCureFailedCnt = 0;
                    historyInfo.lastResetSelfCureFailedTs = 0;
                    return;
                }
                historyInfo.resetSelfCureFailedCnt++;
                historyInfo.lastResetSelfCureFailedTs = currentMs;
            }
        }
    }

    public static void updateSelfCureHistoryInfo(HwSelfCureEngine.InternetSelfCureHistoryInfo historyInfo, int requestCureLevel, boolean success) {
        if (historyInfo != null) {
            long currentMs = System.currentTimeMillis();
            if (requestCureLevel == 201) {
                if (success) {
                    historyInfo.dnsSelfCureFailedCnt = 0;
                    historyInfo.lastDnsSelfCureFailedTs = 0;
                    return;
                }
                historyInfo.dnsSelfCureFailedCnt++;
                historyInfo.lastDnsSelfCureFailedTs = currentMs;
            } else if (requestCureLevel == 202 || requestCureLevel == 208) {
                if (success) {
                    historyInfo.renewDhcpSelfCureFailedCnt = 0;
                    historyInfo.lastRenewDhcpSelfCureFailedTs = 0;
                    return;
                }
                historyInfo.renewDhcpSelfCureFailedCnt++;
                historyInfo.lastRenewDhcpSelfCureFailedTs = currentMs;
            } else if (requestCureLevel == 203) {
                if (success) {
                    historyInfo.staticIpSelfCureFailedCnt = 0;
                    historyInfo.lastStaticIpSelfCureFailedTs = 0;
                    return;
                }
                historyInfo.staticIpSelfCureFailedCnt++;
                historyInfo.lastStaticIpSelfCureFailedTs = currentMs;
            } else if (requestCureLevel == 204 || requestCureLevel == 209 || requestCureLevel == 205) {
                updateReassocAndResetHistoryInfo(historyInfo, requestCureLevel, success);
            }
        }
    }

    public static void updateSelfCureConnectHistoryInfo(HwSelfCureEngine.InternetSelfCureHistoryInfo historyInfo, int requestCureLevel, boolean success) {
        if (historyInfo != null) {
            long currentMs = System.currentTimeMillis();
            if (requestCureLevel == 204) {
                if (success) {
                    historyInfo.reassocSelfCureConnectFailedCnt = 0;
                    historyInfo.lastReassocSelfCureConnectFailedTs = 0;
                    return;
                }
                historyInfo.reassocSelfCureConnectFailedCnt++;
                historyInfo.lastReassocSelfCureConnectFailedTs = currentMs;
            } else if (requestCureLevel == 209) {
                if (success) {
                    historyInfo.randMacSelfCureConnectFailedCnt = 0;
                    historyInfo.lastRandMacSelfCureConnectFailedCntTs = 0;
                    return;
                }
                historyInfo.randMacSelfCureConnectFailedCnt++;
                historyInfo.lastRandMacSelfCureConnectFailedCntTs = currentMs;
            } else if (requestCureLevel != 205) {
            } else {
                if (success) {
                    historyInfo.resetSelfCureConnectFailedCnt = 0;
                    historyInfo.lastResetSelfCureConnectFailedTs = 0;
                    return;
                }
                historyInfo.resetSelfCureConnectFailedCnt++;
                historyInfo.lastResetSelfCureConnectFailedTs = currentMs;
            }
        }
    }

    public static InetAddress getNextIpAddr(InetAddress gateway, InetAddress currentAddr, ArrayList<InetAddress> testedAddr) {
        int getCnt;
        int newip;
        if (gateway == null || currentAddr == null || testedAddr == null) {
            return null;
        }
        int newip2 = -1;
        int getCnt2 = 1;
        byte[] ipAddr = currentAddr.getAddress();
        HwHiLog.d(TAG, false, "getNextIpAddr, gateway = %{public}s currentAddr = %{public}s", new Object[]{StringUtilEx.safeDisplayIpAddress(gateway.getHostAddress()), StringUtilEx.safeDisplayIpAddress(currentAddr.getHostAddress())});
        while (true) {
            getCnt = getCnt2 + 1;
            if (getCnt2 >= 10) {
                newip = newip2;
                break;
            }
            boolean reduplicate = false;
            newip2 = new SecureRandom().nextInt(100) + WifiProUIDisplayManager.DIALOG_TYPE_WIFI_HANDOVER_WIFI;
            if (!(newip2 == (gateway.getAddress()[3] & 255) || newip2 == (ipAddr[3] & 255))) {
                int i = 0;
                while (true) {
                    if (i >= testedAddr.size()) {
                        break;
                    }
                    HwHiLog.d(TAG, false, "getNextIpAddr, testedAddr = %{public}s", new Object[]{StringUtilEx.safeDisplayIpAddress(testedAddr.get(i).getHostAddress())});
                    if (newip2 == (testedAddr.get(i).getAddress()[3] & 255)) {
                        reduplicate = true;
                        break;
                    }
                    i++;
                }
                if (newip2 > 0 && !reduplicate) {
                    newip = newip2;
                    break;
                }
            }
            getCnt2 = getCnt;
        }
        if (newip <= 1 || newip > 250 || getCnt >= 10) {
            return null;
        }
        ipAddr[3] = (byte) newip;
        try {
            return InetAddress.getByAddress(ipAddr);
        } catch (UnknownHostException e) {
            HwHiLog.e(TAG, false, "Exception happened in getNextIpAddr()", new Object[0]);
            return null;
        }
    }

    public static boolean isOnWlanSettings(Context context) {
        if (context != null) {
            return WifiProCommonUtils.isQueryActivityMatched(context, WifiProCommonUtils.HUAWEI_SETTINGS_WLAN);
        }
        return false;
    }

    public static byte[] blackListToByteArray(Map<String, HwSelfCureEngine.Wifi6BlackListInfo> map) {
        if (map == null || map.isEmpty()) {
            return new byte[0];
        }
        int idx = 0;
        byte[] tmpbytes = new byte[128];
        for (Map.Entry<String, HwSelfCureEngine.Wifi6BlackListInfo> entry : map.entrySet()) {
            byte[] bytes = parseWifi6BlackListInfo(entry);
            if (bytes.length == 8) {
                System.arraycopy(bytes, 0, tmpbytes, idx * 8, 8);
                idx++;
                if (idx >= 16) {
                    break;
                }
            }
        }
        byte[] totalBytes = new byte[((idx * 8) + 1)];
        totalBytes[0] = (byte) idx;
        System.arraycopy(tmpbytes, 0, totalBytes, 1, idx * 8);
        return totalBytes;
    }

    public static byte[] parseWifi6BlackListInfo(Map.Entry<String, HwSelfCureEngine.Wifi6BlackListInfo> entry) {
        if (entry == null || TextUtils.isEmpty(entry.getKey())) {
            return new byte[0];
        }
        String[] strAddrItems = entry.getKey().split(":");
        if (strAddrItems.length == 6) {
            byte[] byteArray = new byte[8];
            for (int i = 0; i < strAddrItems.length; i++) {
                try {
                    byteArray[i] = (byte) Integer.parseInt(strAddrItems[i], 16);
                } catch (NumberFormatException e) {
                    HwHiLog.e(TAG, false, "parseMacString failed", new Object[0]);
                }
            }
            byteArray[6] = (byte) entry.getValue().getActionType();
            byteArray[7] = 0;
            return byteArray;
        }
        return new byte[0];
    }
}
