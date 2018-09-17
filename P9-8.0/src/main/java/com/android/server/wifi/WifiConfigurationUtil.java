package com.android.server.wifi;

import android.content.pm.UserInfo;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanner.PnoSettings.PnoNetwork;
import android.os.UserHandle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class WifiConfigurationUtil {

    public static abstract class WifiConfigurationComparator implements Comparator<WifiConfiguration> {
        private static final int ENABLED_NETWORK_SCORE = 3;
        private static final int PERMANENTLY_DISABLED_NETWORK_SCORE = 1;
        private static final int TEMPORARY_DISABLED_NETWORK_SCORE = 2;

        abstract int compareNetworksWithSameStatus(WifiConfiguration wifiConfiguration, WifiConfiguration wifiConfiguration2);

        public int compare(WifiConfiguration a, WifiConfiguration b) {
            int configAScore = getNetworkStatusScore(a);
            int configBScore = getNetworkStatusScore(b);
            if (configAScore == configBScore) {
                return compareNetworksWithSameStatus(a, b);
            }
            return Integer.compare(configBScore, configAScore);
        }

        private int getNetworkStatusScore(WifiConfiguration config) {
            if (config.getNetworkSelectionStatus().isNetworkEnabled()) {
                return 3;
            }
            if (config.getNetworkSelectionStatus().isNetworkTemporaryDisabled()) {
                return 2;
            }
            return 1;
        }
    }

    public static boolean isVisibleToAnyProfile(WifiConfiguration config, List<UserInfo> profiles) {
        return !config.shared ? doesUidBelongToAnyProfile(config.creatorUid, profiles) : true;
    }

    public static boolean doesUidBelongToAnyProfile(int uid, List<UserInfo> profiles) {
        int userId = UserHandle.getUserId(uid);
        for (UserInfo profile : profiles) {
            if (profile.id == userId) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnyValidWepKey(String[] wepKeys) {
        for (String str : wepKeys) {
            if (str != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConfigForPskNetwork(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1) || config.allowedKeyManagement.get(6)) {
            return true;
        }
        return config.allowedKeyManagement.get(8);
    }

    public static boolean isConfigForEapNetwork(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return true;
        }
        return config.allowedKeyManagement.get(7);
    }

    public static boolean isConfigForWepNetwork(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(0)) {
            return hasAnyValidWepKey(config.wepKeys);
        }
        return false;
    }

    public static boolean isConfigForCertNetwork(WifiConfiguration config) {
        return config.allowedKeyManagement.get(9);
    }

    public static boolean isConfigForOpenNetwork(WifiConfiguration config) {
        int i;
        if (isConfigForWepNetwork(config) || isConfigForPskNetwork(config) || isConfigForEapNetwork(config)) {
            i = 1;
        } else {
            i = isConfigForCertNetwork(config);
        }
        return i ^ 1;
    }

    public static boolean hasIpChanged(WifiConfiguration existingConfig, WifiConfiguration newConfig) {
        if (existingConfig.getIpAssignment() != newConfig.getIpAssignment()) {
            return true;
        }
        if (newConfig.getIpAssignment() == IpAssignment.STATIC) {
            return Objects.equals(existingConfig.getStaticIpConfiguration(), newConfig.getStaticIpConfiguration()) ^ 1;
        }
        return false;
    }

    public static boolean hasProxyChanged(WifiConfiguration existingConfig, WifiConfiguration newConfig) {
        boolean z = true;
        if (existingConfig == null) {
            if (newConfig.getProxySettings() == ProxySettings.NONE) {
                z = false;
            }
            return z;
        } else if (newConfig.getProxySettings() != existingConfig.getProxySettings()) {
            return true;
        } else {
            return Objects.equals(existingConfig.getHttpProxy(), newConfig.getHttpProxy()) ^ 1;
        }
    }

    public static boolean hasEnterpriseConfigChanged(WifiEnterpriseConfig existingEnterpriseConfig, WifiEnterpriseConfig newEnterpriseConfig) {
        if (existingEnterpriseConfig == null || newEnterpriseConfig == null) {
            if (!(existingEnterpriseConfig == null && newEnterpriseConfig == null)) {
                return true;
            }
        } else if (!(existingEnterpriseConfig.getEapMethod() == newEnterpriseConfig.getEapMethod() && existingEnterpriseConfig.getPhase2Method() == newEnterpriseConfig.getPhase2Method() && Arrays.equals(existingEnterpriseConfig.getCaCertificates(), newEnterpriseConfig.getCaCertificates()))) {
            return true;
        }
        return false;
    }

    public static boolean hasCredentialChanged(WifiConfiguration existingConfig, WifiConfiguration newConfig) {
        if (Objects.equals(existingConfig.allowedKeyManagement, newConfig.allowedKeyManagement) && Objects.equals(existingConfig.allowedProtocols, newConfig.allowedProtocols) && Objects.equals(existingConfig.allowedAuthAlgorithms, newConfig.allowedAuthAlgorithms) && Objects.equals(existingConfig.allowedPairwiseCiphers, newConfig.allowedPairwiseCiphers) && Objects.equals(existingConfig.allowedGroupCiphers, newConfig.allowedGroupCiphers) && Objects.equals(existingConfig.preSharedKey, newConfig.preSharedKey) && Arrays.equals(existingConfig.wepKeys, newConfig.wepKeys) && existingConfig.wepTxKeyIndex == newConfig.wepTxKeyIndex && existingConfig.hiddenSSID == newConfig.hiddenSSID && !hasEnterpriseConfigChanged(existingConfig.enterpriseConfig, newConfig.enterpriseConfig)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:6:0x000b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isSameNetwork(WifiConfiguration config, WifiConfiguration config1) {
        if (config == null && config1 == null) {
            return true;
        }
        return config != null && config1 != null && config.networkId == config1.networkId && Objects.equals(config.SSID, config1.SSID) && Objects.equals(config.getNetworkSelectionStatus().getNetworkSelectionBSSID(), config1.getNetworkSelectionStatus().getNetworkSelectionBSSID()) && !hasCredentialChanged(config, config1);
    }

    public static PnoNetwork createPnoNetwork(WifiConfiguration config, int newPriority) {
        PnoNetwork pnoNetwork = new PnoNetwork(config.SSID);
        if (config.hiddenSSID) {
            pnoNetwork.flags = (byte) (pnoNetwork.flags | 1);
        }
        pnoNetwork.flags = (byte) (pnoNetwork.flags | 2);
        pnoNetwork.flags = (byte) (pnoNetwork.flags | 4);
        if (config.allowedKeyManagement.get(1)) {
            pnoNetwork.authBitField = (byte) (pnoNetwork.authBitField | 2);
        } else if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            pnoNetwork.authBitField = (byte) (pnoNetwork.authBitField | 4);
        } else {
            pnoNetwork.authBitField = (byte) (pnoNetwork.authBitField | 1);
        }
        return pnoNetwork;
    }
}
