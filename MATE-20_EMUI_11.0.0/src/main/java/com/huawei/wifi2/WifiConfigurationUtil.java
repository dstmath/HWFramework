package com.huawei.wifi2;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.Objects;

public class WifiConfigurationUtil {
    private static final String TAG = "HwWifi2ConfigurationUtil";

    private static boolean hasEnterpriseConfigChanged(WifiEnterpriseConfig existingEnterpriseConfig, WifiEnterpriseConfig newEnterpriseConfig) {
        if (existingEnterpriseConfig == null || newEnterpriseConfig == null) {
            if (existingEnterpriseConfig == null && newEnterpriseConfig == null) {
                return false;
            }
            return true;
        } else if (existingEnterpriseConfig.getEapMethod() == newEnterpriseConfig.getEapMethod() && existingEnterpriseConfig.getPhase2Method() == newEnterpriseConfig.getPhase2Method() && TextUtils.equals(existingEnterpriseConfig.getIdentity(), newEnterpriseConfig.getIdentity()) && TextUtils.equals(existingEnterpriseConfig.getPassword(), newEnterpriseConfig.getPassword()) && Arrays.equals(existingEnterpriseConfig.getCaCertificates(), newEnterpriseConfig.getCaCertificates())) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasCredentialChanged(WifiConfiguration existingConfig, WifiConfiguration newConfig) {
        if (existingConfig == null || newConfig == null) {
            return false;
        }
        if (Objects.equals(existingConfig.allowedKeyManagement, newConfig.allowedKeyManagement) && Objects.equals(existingConfig.allowedProtocols, newConfig.allowedProtocols) && Objects.equals(existingConfig.allowedAuthAlgorithms, newConfig.allowedAuthAlgorithms) && Objects.equals(existingConfig.allowedPairwiseCiphers, newConfig.allowedPairwiseCiphers) && Objects.equals(existingConfig.allowedGroupCiphers, newConfig.allowedGroupCiphers) && Objects.equals(existingConfig.allowedGroupManagementCiphers, newConfig.allowedGroupManagementCiphers) && Objects.equals(existingConfig.allowedSuiteBCiphers, newConfig.allowedSuiteBCiphers) && Objects.equals(existingConfig.preSharedKey, newConfig.preSharedKey) && Arrays.equals(existingConfig.wepKeys, newConfig.wepKeys) && existingConfig.wepTxKeyIndex == newConfig.wepTxKeyIndex && existingConfig.hiddenSSID == newConfig.hiddenSSID && existingConfig.requirePMF == newConfig.requirePMF && !hasEnterpriseConfigChanged(existingConfig.enterpriseConfig, newConfig.enterpriseConfig)) {
            return false;
        }
        return true;
    }

    public static boolean isSameNetwork(WifiConfiguration config, WifiConfiguration config1) {
        if (config == null && config1 == null) {
            return true;
        }
        if (config == null || config1 == null || config.networkId != config1.networkId || !Objects.equals(config.SSID, config1.SSID) || hasCredentialChanged(config, config1)) {
            return false;
        }
        return true;
    }

    public static boolean isConfigForSaeNetwork(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        return config.allowedKeyManagement.get(8);
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

    public static boolean hasAnyValidWepKey(String[] wepKeys) {
        if (wepKeys == null) {
            return false;
        }
        for (String wepkey : wepKeys) {
            if (wepkey != null) {
                return true;
            }
        }
        return false;
    }

    private static String addEnclosingQuotes(String str) {
        return "\"" + str + "\"";
    }
}
