package com.android.server.wifi;

import android.content.pm.UserInfo;
import android.net.IpConfiguration;
import android.net.StaticIpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanner;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.scanner.ScanResultRecords;
import com.android.server.wifi.util.NativeUtil;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class WifiConfigurationUtil {
    private static final int ENCLOSING_QUTOES_LEN = 2;
    @VisibleForTesting
    public static final String PASSWORD_MASK = "*";
    private static final int PSK_ASCII_MAX_LEN = 65;
    private static final int PSK_ASCII_MIN_LEN = 10;
    private static final int PSK_HEX_LEN = 64;
    private static final int SSID_HEX_MAX_LEN = 64;
    private static final int SSID_HEX_MIN_LEN = 2;
    private static final int SSID_UTF_8_MAX_LEN = 34;
    private static final int SSID_UTF_8_MIN_LEN = 3;
    private static final String TAG = "WifiConfigurationUtil";
    public static final boolean VALIDATE_FOR_ADD = true;
    public static final boolean VALIDATE_FOR_UPDATE = false;

    public static abstract class WifiConfigurationComparator implements Comparator<WifiConfiguration> {
        private static final int ENABLED_NETWORK_SCORE = 3;
        private static final int PERMANENTLY_DISABLED_NETWORK_SCORE = 1;
        private static final int TEMPORARY_DISABLED_NETWORK_SCORE = 2;

        /* access modifiers changed from: package-private */
        public abstract int compareNetworksWithSameStatus(WifiConfiguration wifiConfiguration, WifiConfiguration wifiConfiguration2);

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
        return config.shared || doesUidBelongToAnyProfile(config.creatorUid, profiles);
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
        return config.allowedKeyManagement.get(1) || config.allowedKeyManagement.get(6) || config.allowedKeyManagement.get(8) || config.allowedKeyManagement.get(10);
    }

    public static boolean isConfigForEapNetwork(WifiConfiguration config) {
        return config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3) || config.allowedKeyManagement.get(7);
    }

    public static boolean isConfigForWepNetwork(WifiConfiguration config) {
        if (!config.allowedKeyManagement.get(0) || !hasAnyValidWepKey(config.wepKeys)) {
            return false;
        }
        return true;
    }

    public static boolean isConfigForCertNetwork(WifiConfiguration config) {
        return config.allowedKeyManagement.get(9) || config.allowedKeyManagement.get(11);
    }

    public static boolean isConfigForOpenNetwork(WifiConfiguration config) {
        return !isConfigForWepNetwork(config) && !isConfigForPskNetwork(config) && !isConfigForEapNetwork(config) && !isConfigForCertNetwork(config);
    }

    public static boolean hasIpChanged(WifiConfiguration existingConfig, WifiConfiguration newConfig) {
        if (existingConfig.getIpAssignment() != newConfig.getIpAssignment()) {
            return true;
        }
        if (newConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
            return !Objects.equals(existingConfig.getStaticIpConfiguration(), newConfig.getStaticIpConfiguration());
        }
        return false;
    }

    public static boolean hasProxyChanged(WifiConfiguration existingConfig, WifiConfiguration newConfig) {
        boolean z = true;
        if (existingConfig == null) {
            if (newConfig.getProxySettings() == IpConfiguration.ProxySettings.NONE) {
                z = false;
            }
            return z;
        } else if (newConfig.getProxySettings() != existingConfig.getProxySettings()) {
            return true;
        } else {
            return true ^ Objects.equals(existingConfig.getHttpProxy(), newConfig.getHttpProxy());
        }
    }

    @VisibleForTesting
    public static boolean hasEnterpriseConfigChanged(WifiEnterpriseConfig existingEnterpriseConfig, WifiEnterpriseConfig newEnterpriseConfig) {
        if (existingEnterpriseConfig == null || newEnterpriseConfig == null) {
            if (!(existingEnterpriseConfig == null && newEnterpriseConfig == null)) {
                return true;
            }
        } else if (existingEnterpriseConfig.getEapMethod() != newEnterpriseConfig.getEapMethod() || existingEnterpriseConfig.getPhase2Method() != newEnterpriseConfig.getPhase2Method() || !TextUtils.equals(existingEnterpriseConfig.getIdentity(), newEnterpriseConfig.getIdentity()) || !TextUtils.equals(existingEnterpriseConfig.getAnonymousIdentity(), newEnterpriseConfig.getAnonymousIdentity()) || !TextUtils.equals(existingEnterpriseConfig.getPassword(), newEnterpriseConfig.getPassword()) || !Arrays.equals(existingEnterpriseConfig.getCaCertificates(), newEnterpriseConfig.getCaCertificates())) {
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

    private static boolean isGBKEncodedString(String ssid) {
        if (ssid.isEmpty()) {
            Log.e(TAG, "ssid is empty");
            return false;
        }
        ArrayList<Byte> ssidArray = ScanResultRecords.getDefault().getOriSsid(NativeUtil.removeEnclosingQuotes(ssid));
        if (ssidArray != null) {
            byte[] buff = NativeUtil.byteArrayFromArrayList(ssidArray);
            try {
                if (Charset.isSupported("GBK")) {
                    byte[] newBuff = NativeUtil.removeEnclosingQuotes(ssid).getBytes("GBK");
                    if (buff.length == newBuff.length) {
                        for (int i = 0; i < buff.length; i++) {
                            if (buff[i] != newBuff[i]) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            } catch (UnsupportedEncodingException e) {
            }
        }
        return false;
    }

    private static boolean validateSsid(String ssid, boolean isAdd) {
        if (isAdd) {
            if (ssid == null) {
                Log.e(TAG, "validateSsid : null string");
                return false;
            }
        } else if (ssid == null) {
            return true;
        }
        if (ssid.isEmpty()) {
            Log.e(TAG, "validateSsid failed: empty string");
            return false;
        }
        if (ssid.startsWith("\"")) {
            if (!isGBKEncodedString(ssid)) {
                byte[] ssidBytes = ssid.getBytes(StandardCharsets.UTF_8);
                if (ssidBytes.length < 3) {
                    Log.e(TAG, "validateSsid failed: utf-8 ssid string size too small: " + ssidBytes.length);
                    return false;
                } else if (ssidBytes.length > 34) {
                    Log.e(TAG, "validateSsid failed: utf-8 ssid string size too large: " + ssidBytes.length);
                    return false;
                }
            } else if (ssid.length() < 3) {
                Log.e(TAG, "validateSsid failed: GBK ssid string size too small: " + ssid.length());
                return false;
            } else if (ssid.length() > 34) {
                Log.e(TAG, "validateSsid failed: GBK ssid string size too large: " + ssid.length());
                return false;
            }
        } else if (ssid.length() < 2) {
            Log.e(TAG, "validateSsid failed: hex string size too small: " + ssid.length());
            return false;
        } else if (ssid.length() > 64) {
            Log.e(TAG, "validateSsid failed: hex string size too large: " + ssid.length());
            return false;
        }
        try {
            NativeUtil.decodeSsid(ssid);
            return true;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "validateSsid failed: malformed string: " + ssid);
            return false;
        }
    }

    private static boolean validatePsk(String psk, boolean isAdd) {
        if (isAdd) {
            if (psk == null) {
                Log.e(TAG, "validatePsk: null string");
                return false;
            }
        } else if (psk == null || psk.equals("*")) {
            return true;
        }
        if (psk.isEmpty()) {
            Log.e(TAG, "validatePsk failed: empty string");
            return false;
        }
        if (psk.startsWith("\"")) {
            byte[] pskBytes = psk.getBytes(StandardCharsets.US_ASCII);
            if (pskBytes.length < 10) {
                Log.e(TAG, "validatePsk failed: ascii string size too small: " + pskBytes.length);
                return false;
            } else if (pskBytes.length > 65) {
                Log.e(TAG, "validatePsk failed: ascii string size too large: " + pskBytes.length);
                return false;
            }
        } else if (psk.length() != 64) {
            Log.e(TAG, "validatePsk failed: hex string size mismatch: " + psk.length());
            return false;
        }
        try {
            NativeUtil.hexOrQuotedStringToBytes(psk);
            return true;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "validatePsk failed: malformed string: " + psk);
            return false;
        }
    }

    private static boolean validateBitSet(BitSet bitSet, int validValuesLength) {
        if (bitSet == null) {
            return false;
        }
        BitSet clonedBitset = (BitSet) bitSet.clone();
        clonedBitset.clear(0, validValuesLength);
        return clonedBitset.isEmpty();
    }

    private static boolean validateBitSets(WifiConfiguration config) {
        if (!validateBitSet(config.allowedKeyManagement, WifiConfiguration.KeyMgmt.strings.length)) {
            Log.e(TAG, "validateBitsets failed: invalid allowedKeyManagement bitset " + config.allowedKeyManagement);
            return false;
        } else if (!validateBitSet(config.allowedProtocols, WifiConfiguration.Protocol.strings.length)) {
            Log.e(TAG, "validateBitsets failed: invalid allowedProtocols bitset " + config.allowedProtocols);
            return false;
        } else if (!validateBitSet(config.allowedAuthAlgorithms, WifiConfiguration.AuthAlgorithm.strings.length)) {
            Log.e(TAG, "validateBitsets failed: invalid allowedAuthAlgorithms bitset " + config.allowedAuthAlgorithms);
            return false;
        } else if (!validateBitSet(config.allowedGroupCiphers, WifiConfiguration.GroupCipher.strings.length)) {
            Log.e(TAG, "validateBitsets failed: invalid allowedGroupCiphers bitset " + config.allowedGroupCiphers);
            return false;
        } else if (validateBitSet(config.allowedPairwiseCiphers, WifiConfiguration.PairwiseCipher.strings.length)) {
            return true;
        } else {
            Log.e(TAG, "validateBitsets failed: invalid allowedPairwiseCiphers bitset " + config.allowedPairwiseCiphers);
            return false;
        }
    }

    private static boolean validateKeyMgmt(BitSet keyMgmnt) {
        if (keyMgmnt.cardinality() > 1) {
            if (keyMgmnt.cardinality() != 2) {
                Log.e(TAG, "validateKeyMgmt failed: cardinality != 2");
                return false;
            } else if (!keyMgmnt.get(2)) {
                Log.e(TAG, "validateKeyMgmt failed: not WPA_EAP");
                return false;
            } else if (!keyMgmnt.get(3) && !keyMgmnt.get(1)) {
                Log.e(TAG, "validateKeyMgmt failed: not PSK or 8021X");
                return false;
            }
        }
        return true;
    }

    private static boolean validateIpConfiguration(IpConfiguration ipConfig) {
        if (ipConfig == null) {
            Log.e(TAG, "validateIpConfiguration failed: null IpConfiguration");
            return false;
        }
        if (ipConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
            StaticIpConfiguration staticIpConfig = ipConfig.getStaticIpConfiguration();
            if (staticIpConfig == null) {
                Log.e(TAG, "validateIpConfiguration failed: null StaticIpConfiguration");
                return false;
            } else if (staticIpConfig.ipAddress == null) {
                Log.e(TAG, "validateIpConfiguration failed: null static ip Address");
                return false;
            }
        }
        return true;
    }

    public static boolean validate(WifiConfiguration config, boolean isAdd) {
        if (!validateSsid(config.SSID, isAdd) || !validateBitSets(config) || !validateKeyMgmt(config.allowedKeyManagement)) {
            return false;
        }
        if ((!config.allowedKeyManagement.get(1) || validatePsk(config.preSharedKey, isAdd)) && validateIpConfiguration(config.getIpConfiguration())) {
            return true;
        }
        return false;
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

    public static WifiScanner.PnoSettings.PnoNetwork createPnoNetwork(WifiConfiguration config) {
        WifiScanner.PnoSettings.PnoNetwork pnoNetwork = new WifiScanner.PnoSettings.PnoNetwork(config.SSID);
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
