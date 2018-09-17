package com.android.server.wifi;

import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiSsid;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.net.wifi.WpsResult.Status;
import android.os.FileObserver;
import android.os.SystemProperties;
import android.security.Credentials;
import android.security.KeyChain;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.wifi.hotspot2.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class WifiConfigStore extends AbsWifiConfigStore {
    private static final boolean DBG = true;
    protected static final boolean HWFLOW = false;
    private static boolean HWLOGW_E = false;
    public static final String ID_STRING_KEY_CONFIG_KEY = "configKey";
    public static final String ID_STRING_KEY_CREATOR_UID = "creatorUid";
    public static final String ID_STRING_KEY_FQDN = "fqdn";
    public static final String ID_STRING_VAR_NAME = "id_str";
    public static final int STORED_VALUE_FOR_REQUIRE_PMF = 2;
    public static final String SUPPLICANT_CONFIG_FILE = "/data/misc/wifi/wpa_supplicant.conf";
    public static final String SUPPLICANT_CONFIG_FILE_BACKUP = "/data/misc/wifi/wpa_supplicant.conf.tmp";
    public static final String TAG = "WifiConfigStore";
    private static boolean VDBG;
    private static boolean VVDBG;
    private final BackupManagerProxy mBackupManagerProxy;
    private final HashSet<String> mBssidBlacklist;
    private final WpaConfigFileObserver mFileObserver;
    private final KeyStore mKeyStore;
    private final LocalLog mLocalLog;
    private final boolean mShowNetworks;
    private final WifiNative mWifiNative;

    private class SupplicantLoader implements android.net.wifi.WifiEnterpriseConfig.SupplicantLoader {
        private final int mNetId;

        SupplicantLoader(int netId) {
            this.mNetId = netId;
        }

        public String loadValue(String key) {
            String value = WifiConfigStore.this.mWifiNative.getNetworkVariable(this.mNetId, key);
            if (TextUtils.isEmpty(value)) {
                return null;
            }
            if (!enterpriseConfigKeyShouldBeQuoted(key)) {
                value = WifiConfigStore.removeDoubleQuotes(value);
            }
            return value;
        }

        private boolean enterpriseConfigKeyShouldBeQuoted(String key) {
            if (key.equals("eap") || key.equals("engine")) {
                return WifiConfigStore.HWFLOW;
            }
            return WifiConfigStore.DBG;
        }
    }

    private class SupplicantSaver implements android.net.wifi.WifiEnterpriseConfig.SupplicantSaver {
        private final WifiConfiguration mConfig;
        private final WifiConfiguration mExistingConfig;
        private final int mNetId;
        private final String mSetterSSID;

        SupplicantSaver(WifiConfiguration config, WifiConfiguration existingConfig) {
            this.mConfig = config;
            this.mNetId = config.networkId;
            this.mSetterSSID = config.SSID;
            this.mExistingConfig = existingConfig;
        }

        public boolean saveValue(String key, String value) {
            int eapMethod = -1;
            int currEapMethod = -1;
            boolean doCheck = WifiConfigStore.HWFLOW;
            Object obj = null;
            if (!(this.mConfig == null || this.mConfig.enterpriseConfig == null)) {
                eapMethod = this.mConfig.enterpriseConfig.getEapMethod();
                currEapMethod = eapMethod;
            }
            if (!(this.mExistingConfig == null || this.mExistingConfig.enterpriseConfig == null)) {
                currEapMethod = this.mExistingConfig.enterpriseConfig.getEapMethod();
                obj = this.mExistingConfig.enterpriseConfig.getFieldValue(key, "");
                doCheck = WifiConfigStore.DBG;
                WifiConfigStore.this.log("current Eap Method:" + currEapMethod + ", eap Method:" + eapMethod);
            }
            if ((key.equals("password") && value != null && value.equals("*")) || key.equals("realm") || key.equals("plmn")) {
                return WifiConfigStore.DBG;
            }
            if (doCheck && currEapMethod == eapMethod && ((eapMethod == 4 || eapMethod == 5) && ("anonymous_identity".equals(key) || "identity".equals(key)))) {
                WifiConfigStore.this.log("skip update:" + key + " for SSID:" + this.mConfig.SSID + " with eapMethod:" + eapMethod);
                return WifiConfigStore.DBG;
            }
            boolean z;
            if (value == null) {
                value = "\"\"";
            }
            if (!doCheck || obj == null || value == null) {
                z = WifiConfigStore.HWFLOW;
            } else {
                z = value.equals(obj);
            }
            if (z || WifiConfigStore.this.mWifiNative.setNetworkVariable(this.mNetId, key, value)) {
                return WifiConfigStore.DBG;
            }
            WifiConfigStore.this.loge(this.mSetterSSID + ": failed to set " + key + ": " + value);
            return WifiConfigStore.HWFLOW;
        }
    }

    private class WpaConfigFileObserver extends FileObserver {
        WpaConfigFileObserver() {
            super(WifiConfigStore.SUPPLICANT_CONFIG_FILE, 8);
        }

        public void onEvent(int event, String path) {
            if (event == 8) {
                File file = new File(WifiConfigStore.SUPPLICANT_CONFIG_FILE);
                if (WifiConfigStore.VDBG) {
                    WifiConfigStore.this.localLog("wpa_supplicant.conf changed; new size = " + file.length());
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.WifiConfigStore.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.WifiConfigStore.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiConfigStore.<clinit>():void");
    }

    WifiConfigStore(WifiNative wifiNative, KeyStore keyStore, LocalLog localLog, boolean showNetworks, boolean verboseDebug) {
        this.mBssidBlacklist = new HashSet();
        this.mWifiNative = wifiNative;
        this.mKeyStore = keyStore;
        this.mShowNetworks = showNetworks;
        this.mBackupManagerProxy = new BackupManagerProxy();
        if (this.mShowNetworks) {
            this.mLocalLog = localLog;
            this.mFileObserver = new WpaConfigFileObserver();
            this.mFileObserver.startWatching();
        } else {
            this.mLocalLog = null;
            this.mFileObserver = null;
        }
        VDBG = verboseDebug;
    }

    private static String removeDoubleQuotes(String string) {
        int length = string.length();
        if (length > 1 && string.charAt(0) == '\"' && string.charAt(length - 1) == '\"') {
            return string.substring(1, length - 1);
        }
        return string;
    }

    private static String makeString(BitSet set, String[] strings) {
        return makeStringWithException(set, strings, null);
    }

    private static String makeStringWithException(BitSet set, String[] strings, String exception) {
        StringBuilder result = new StringBuilder();
        BitSet trimmedSet = set.get(0, strings.length);
        List<String> valueSet = new ArrayList();
        for (int bit = trimmedSet.nextSetBit(0); bit >= 0; bit = trimmedSet.nextSetBit(bit + 1)) {
            String currentName = strings[bit];
            if (exception == null || !currentName.equals(exception)) {
                valueSet.add(currentName.replace('_', '-'));
            } else {
                valueSet.add(currentName);
            }
        }
        return TextUtils.join(" ", valueSet);
    }

    private static String encodeSSID(String str) {
        return Utils.toHex(removeDoubleQuotes(str).getBytes(StandardCharsets.UTF_8));
    }

    private static boolean needsKeyStore(WifiEnterpriseConfig config) {
        return (config.getClientCertificate() == null && config.getCaCertificate() == null) ? HWFLOW : DBG;
    }

    private static boolean isHardwareBackedKey(PrivateKey key) {
        return KeyChain.isBoundKeyAlgorithm(key.getAlgorithm());
    }

    private static boolean hasHardwareBackedKey(Certificate certificate) {
        return KeyChain.isBoundKeyAlgorithm(certificate.getPublicKey().getAlgorithm());
    }

    private static boolean needsSoftwareBackedKeyStore(WifiEnterpriseConfig config) {
        if (TextUtils.isEmpty(config.getClientCertificateAlias())) {
            return HWFLOW;
        }
        return DBG;
    }

    private int lookupString(String string, String[] strings) {
        int size = strings.length;
        string = string.replace('-', '_');
        for (int i = 0; i < size; i++) {
            if (string.equals(strings[i])) {
                return i;
            }
        }
        loge("Failed to look-up a string: " + string);
        return -1;
    }

    private void readNetworkBitsetVariable(int netId, BitSet variable, String varName, String[] strings) {
        String value = this.mWifiNative.getNetworkVariable(netId, varName);
        if (!TextUtils.isEmpty(value)) {
            variable.clear();
            for (String val : value.split(" ")) {
                int index = lookupString(val, strings);
                if (index >= 0) {
                    variable.set(index);
                }
            }
        }
    }

    public void readNetworkVariables(WifiConfiguration config) {
        boolean z = DBG;
        if (config != null) {
            if (VDBG) {
                localLog("readNetworkVariables: " + config.networkId);
            }
            int netId = config.networkId;
            if (netId >= 0) {
                String value = this.mWifiNative.getNetworkVariable(netId, "ssid");
                if (TextUtils.isEmpty(value)) {
                    config.SSID = null;
                } else if (value.charAt(0) != '\"') {
                    config.SSID = "\"" + WifiSsid.createFromHex(value).toString() + "\"";
                    config.oriSsid = value;
                } else {
                    config.SSID = value;
                }
                value = this.mWifiNative.getNetworkVariable(netId, "bssid");
                if (TextUtils.isEmpty(value)) {
                    config.getNetworkSelectionStatus().setNetworkSelectionBSSID(null);
                } else {
                    config.getNetworkSelectionStatus().setNetworkSelectionBSSID(value);
                }
                value = this.mWifiNative.getNetworkVariable(netId, "priority");
                config.priority = -1;
                if (!TextUtils.isEmpty(value)) {
                    try {
                        config.priority = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                    }
                }
                value = this.mWifiNative.getNetworkVariable(netId, "scan_ssid");
                config.hiddenSSID = HWFLOW;
                if (!TextUtils.isEmpty(value)) {
                    try {
                        config.hiddenSSID = Integer.parseInt(value) != 0 ? DBG : HWFLOW;
                    } catch (NumberFormatException e2) {
                    }
                }
                value = this.mWifiNative.getNetworkVariable(netId, "ieee80211w");
                config.requirePMF = HWFLOW;
                if (!TextUtils.isEmpty(value)) {
                    try {
                        if (Integer.parseInt(value) != STORED_VALUE_FOR_REQUIRE_PMF) {
                            z = HWFLOW;
                        }
                        config.requirePMF = z;
                    } catch (NumberFormatException e3) {
                    }
                }
                value = this.mWifiNative.getNetworkVariable(netId, "wep_tx_keyidx");
                config.wepTxKeyIndex = -1;
                if (!TextUtils.isEmpty(value)) {
                    try {
                        config.wepTxKeyIndex = Integer.parseInt(value);
                    } catch (NumberFormatException e4) {
                    }
                }
                for (int i = 0; i < 4; i++) {
                    value = this.mWifiNative.getNetworkVariable(netId, WifiConfiguration.wepKeyVarNames[i]);
                    if (TextUtils.isEmpty(value)) {
                        config.wepKeys[i] = null;
                    } else {
                        config.wepKeys[i] = value;
                    }
                }
                value = this.mWifiNative.getNetworkVariable(netId, "psk");
                if (TextUtils.isEmpty(value)) {
                    config.preSharedKey = null;
                } else {
                    config.preSharedKey = value;
                }
                readNetworkBitsetVariable(config.networkId, config.allowedProtocols, "proto", Protocol.strings);
                readNetworkBitsetVariable(config.networkId, config.allowedKeyManagement, "key_mgmt", KeyMgmt.strings);
                readNetworkBitsetVariable(config.networkId, config.allowedAuthAlgorithms, "auth_alg", AuthAlgorithm.strings);
                readNetworkBitsetVariable(config.networkId, config.allowedPairwiseCiphers, "pairwise", PairwiseCipher.strings);
                readNetworkBitsetVariable(config.networkId, config.allowedGroupCiphers, "group", GroupCipher.strings);
                if (config.enterpriseConfig == null) {
                    config.enterpriseConfig = new WifiEnterpriseConfig();
                }
                config.enterpriseConfig.loadFromSupplicant(new SupplicantLoader(netId));
                setWifiConfigurationWapi(config, netId);
            }
        }
    }

    public int loadNetworks(Map<String, WifiConfiguration> configs, SparseArray<Map<String, String>> networkExtras) {
        int lastPriority = 0;
        int last_id = -1;
        boolean done = HWFLOW;
        while (!done) {
            String listStr = this.mWifiNative.listNetworks(last_id);
            if (listStr == null) {
                return lastPriority;
            }
            String[] lines = listStr.split("\n");
            if (this.mShowNetworks) {
                localLog("loadNetworks:  ");
                for (String net : lines) {
                    localLog(net);
                }
            }
            for (int i = 1; i < lines.length; i++) {
                String[] result = lines[i].split("\t");
                WifiConfiguration config = new WifiConfiguration();
                try {
                    config.networkId = Integer.parseInt(result[0]);
                    last_id = config.networkId;
                    config.status = 1;
                    readNetworkVariables(config);
                    Map<String, String> extras = this.mWifiNative.getNetworkExtra(config.networkId, ID_STRING_VAR_NAME);
                    if (extras == null) {
                        extras = new HashMap();
                        String fqdn = Utils.unquote(this.mWifiNative.getNetworkVariable(config.networkId, ID_STRING_VAR_NAME));
                        if (fqdn != null) {
                            extras.put(ID_STRING_KEY_FQDN, fqdn);
                            config.FQDN = fqdn;
                            config.providerFriendlyName = "";
                        }
                    }
                    networkExtras.put(config.networkId, extras);
                    if (config.priority > lastPriority) {
                        lastPriority = config.priority;
                    }
                    config.setIpAssignment(IpAssignment.DHCP);
                    config.setProxySettings(ProxySettings.NONE);
                    if (WifiServiceImpl.isValid(config)) {
                        String configKey = (String) extras.get(ID_STRING_KEY_CONFIG_KEY);
                        if (configKey == null) {
                            configKey = config.configKey();
                            saveNetworkMetadata(config);
                        }
                        WifiConfiguration duplicateConfig = (WifiConfiguration) configs.put(configKey, config);
                        if (duplicateConfig != null) {
                            if (this.mShowNetworks) {
                                localLog("Replacing duplicate network " + duplicateConfig.networkId + " with " + config.networkId + ".");
                            }
                            this.mWifiNative.removeNetwork(duplicateConfig.networkId);
                        }
                    } else if (this.mShowNetworks) {
                        localLog("Ignoring network " + config.networkId + " because configuration " + "loaded from wpa_supplicant.conf is not valid.");
                    }
                } catch (NumberFormatException e) {
                    loge("Failed to read network-id '" + result[0] + "'");
                }
            }
            done = lines.length == 1 ? DBG : HWFLOW;
        }
        return lastPriority;
    }

    private boolean installKeys(WifiEnterpriseConfig existingConfig, WifiEnterpriseConfig config, String name) {
        boolean z = DBG;
        String privKeyName = "USRPKEY_" + name;
        String userCertName = "USRCERT_" + name;
        if (config.getClientCertificate() != null) {
            byte[] privKeyData = config.getClientPrivateKey().getEncoded();
            if (isHardwareBackedKey(config.getClientPrivateKey())) {
                Log.d(TAG, "importing keys " + name + " in hardware backed store");
            } else {
                Log.d(TAG, "importing keys " + name + " in software backed store");
            }
            z = this.mKeyStore.importKey(privKeyName, privKeyData, 1010, 0);
            if (!z) {
                return z;
            }
            z = putCertInKeyStore(userCertName, config.getClientCertificate());
            if (!z) {
                this.mKeyStore.delete(privKeyName, 1010);
                return z;
            }
        }
        X509Certificate[] caCertificates = config.getCaCertificates();
        Set<String> oldCaCertificatesToRemove = new ArraySet();
        if (!(existingConfig == null || existingConfig.getCaCertificateAliases() == null)) {
            oldCaCertificatesToRemove.addAll(Arrays.asList(existingConfig.getCaCertificateAliases()));
        }
        List list = null;
        if (caCertificates != null) {
            List<String> caCertificateAliases = new ArrayList();
            int i = 0;
            while (i < caCertificates.length) {
                String alias;
                if (caCertificates.length == 1) {
                    alias = name;
                } else {
                    Object[] objArr = new Object[STORED_VALUE_FOR_REQUIRE_PMF];
                    objArr[0] = name;
                    objArr[1] = Integer.valueOf(i);
                    alias = String.format("%s_%d", objArr);
                }
                oldCaCertificatesToRemove.remove(alias);
                z = putCertInKeyStore("CACERT_" + alias, caCertificates[i]);
                if (z) {
                    caCertificateAliases.add(alias);
                    i++;
                } else {
                    if (config.getClientCertificate() != null) {
                        this.mKeyStore.delete(privKeyName, 1010);
                        this.mKeyStore.delete(userCertName, 1010);
                    }
                    for (String addedAlias : caCertificateAliases) {
                        this.mKeyStore.delete("CACERT_" + addedAlias, 1010);
                    }
                    return z;
                }
            }
        }
        for (String oldAlias : oldCaCertificatesToRemove) {
            this.mKeyStore.delete("CACERT_" + oldAlias, 1010);
        }
        if (config.getClientCertificate() != null) {
            config.setClientCertificateAlias(name);
            config.resetClientKeyEntry();
        }
        if (caCertificates != null) {
            config.setCaCertificateAliases((String[]) list.toArray(new String[list.size()]));
            config.resetCaCertificate();
        }
        return z;
    }

    private boolean putCertInKeyStore(String name, Certificate cert) {
        try {
            byte[] certData = Credentials.convertToPem(new Certificate[]{cert});
            Log.d(TAG, "putting certificate " + name + " in keystore");
            return this.mKeyStore.put(name, certData, 1010, 0);
        } catch (IOException e) {
            return HWFLOW;
        } catch (CertificateException e2) {
            return HWFLOW;
        }
    }

    private void removeKeys(WifiEnterpriseConfig config) {
        String client = config.getClientCertificateAlias();
        if (!TextUtils.isEmpty(client)) {
            Log.d(TAG, "removing client private key and user cert");
            this.mKeyStore.delete("USRPKEY_" + client, 1010);
            this.mKeyStore.delete("USRCERT_" + client, 1010);
        }
        String[] aliases = config.getCaCertificateAliases();
        if (aliases != null) {
            for (String ca : aliases) {
                if (!TextUtils.isEmpty(ca)) {
                    Log.d(TAG, "removing CA cert: " + ca);
                    this.mKeyStore.delete("CACERT_" + ca, 1010);
                }
            }
        }
    }

    public boolean saveNetworkMetadata(WifiConfiguration config) {
        Map<String, String> metadata = new HashMap();
        if (config.isPasspoint()) {
            metadata.put(ID_STRING_KEY_FQDN, config.FQDN);
        }
        metadata.put(ID_STRING_KEY_CONFIG_KEY, config.configKey());
        metadata.put(ID_STRING_KEY_CREATOR_UID, Integer.toString(config.creatorUid));
        if (this.mWifiNative.setNetworkExtra(config.networkId, ID_STRING_VAR_NAME, metadata)) {
            return DBG;
        }
        loge("failed to set id_str: " + metadata.toString());
        return HWFLOW;
    }

    private boolean saveNetwork(WifiConfiguration config, int netId, boolean newNetwork, WifiConfiguration existingConfig) {
        if (config == null) {
            return HWFLOW;
        }
        boolean z;
        if (VDBG) {
            localLog("saveNetwork: " + netId);
        }
        if (!(newNetwork || existingConfig == null)) {
            readNetworkVariables(existingConfig);
        }
        if (newNetwork || existingConfig == null || existingConfig.SSID == null || config.SSID == null) {
            z = HWFLOW;
        } else {
            z = existingConfig.SSID.equals(config.SSID);
        }
        if (!(z || config.SSID == null)) {
            if (!this.mWifiNative.setNetworkVariable(netId, "ssid", TextUtils.isEmpty(config.oriSsid) ? encodeSSID(config.SSID) : config.oriSsid)) {
                loge("failed to set SSID: " + config.SSID);
                return HWFLOW;
            }
        }
        boolean isGlobalVersion = ("zh".equals(SystemProperties.get("ro.product.locale.language")) && "CN".equals(SystemProperties.get("ro.product.locale.region"))) ? HWFLOW : DBG;
        if (!(newNetwork || existingConfig == null || isGlobalVersion)) {
            existingConfig.cloudSecurityCheck = config.cloudSecurityCheck;
            if (!this.mWifiNative.setNetworkVariable(netId, "cloudSecurityCheck", Integer.toString(config.cloudSecurityCheck))) {
                loge("failed to set cloudSecurityCheck: " + config.cloudSecurityCheck);
                return HWFLOW;
            }
        }
        if (!saveNetworkMetadata(config)) {
            return HWFLOW;
        }
        if (newNetwork || existingConfig == null || existingConfig.BSSID == null || config.BSSID == null) {
            z = HWFLOW;
        } else {
            z = existingConfig.BSSID.equals(config.BSSID);
        }
        if (!(z || config.getNetworkSelectionStatus().getNetworkSelectionBSSID() == null)) {
            String bssid = config.getNetworkSelectionStatus().getNetworkSelectionBSSID();
            if (!this.mWifiNative.setNetworkVariable(netId, "bssid", bssid)) {
                loge("failed to set BSSID: " + bssid);
                return HWFLOW;
            }
        }
        String allowedKeyManagementString = makeString(config.allowedKeyManagement, KeyMgmt.strings);
        if (newNetwork || existingConfig == null) {
            z = HWFLOW;
        } else {
            z = existingConfig.allowedKeyManagement.equals(config.allowedKeyManagement);
        }
        if (!(z || config.allowedKeyManagement.cardinality() == 0)) {
            if (!this.mWifiNative.setNetworkVariable(netId, "key_mgmt", allowedKeyManagementString)) {
                loge("failed to set key_mgmt: " + allowedKeyManagementString);
                return HWFLOW;
            }
        }
        String allowedProtocolsString = makeString(config.allowedProtocols, Protocol.strings);
        if (newNetwork || existingConfig == null) {
            z = HWFLOW;
        } else {
            z = existingConfig.allowedProtocols.equals(config.allowedProtocols);
        }
        if (!(z || config.allowedProtocols.cardinality() == 0)) {
            if (!this.mWifiNative.setNetworkVariable(netId, "proto", allowedProtocolsString)) {
                loge("failed to set proto: " + allowedProtocolsString);
                return HWFLOW;
            }
        }
        String allowedAuthAlgorithmsString = makeString(config.allowedAuthAlgorithms, AuthAlgorithm.strings);
        if (newNetwork || existingConfig == null) {
            z = HWFLOW;
        } else {
            z = existingConfig.allowedAuthAlgorithms.equals(config.allowedAuthAlgorithms);
        }
        if (!(z || config.allowedAuthAlgorithms.cardinality() == 0)) {
            if (!this.mWifiNative.setNetworkVariable(netId, "auth_alg", allowedAuthAlgorithmsString)) {
                loge("failed to set auth_alg: " + allowedAuthAlgorithmsString);
                return HWFLOW;
            }
        }
        String allowedPairwiseCiphersString = makeString(config.allowedPairwiseCiphers, PairwiseCipher.strings);
        if (newNetwork || existingConfig == null) {
            z = HWFLOW;
        } else {
            z = existingConfig.allowedPairwiseCiphers.equals(config.allowedPairwiseCiphers);
        }
        if (!(z || config.allowedPairwiseCiphers.cardinality() == 0)) {
            if (!this.mWifiNative.setNetworkVariable(netId, "pairwise", allowedPairwiseCiphersString)) {
                loge("failed to set pairwise: " + allowedPairwiseCiphersString);
                return HWFLOW;
            }
        }
        String allowedGroupCiphersString = makeStringWithException(config.allowedGroupCiphers, GroupCipher.strings, GroupCipher.strings[4]);
        if (newNetwork || existingConfig == null) {
            z = HWFLOW;
        } else {
            z = existingConfig.allowedGroupCiphers.equals(config.allowedGroupCiphers);
        }
        if (!(z || config.allowedGroupCiphers.cardinality() == 0)) {
            if (!this.mWifiNative.setNetworkVariable(netId, "group", allowedGroupCiphersString)) {
                loge("failed to set group: " + allowedGroupCiphersString);
                return HWFLOW;
            }
        }
        if (newNetwork || existingConfig == null || existingConfig.preSharedKey == null || config.preSharedKey == null) {
            z = HWFLOW;
        } else {
            z = existingConfig.preSharedKey.equals(config.preSharedKey);
        }
        if (!(z || config.preSharedKey == null || config.preSharedKey.equals("*"))) {
            if (!this.mWifiNative.setNetworkVariable(netId, "psk", config.preSharedKey)) {
                loge("failed to set psk");
                return HWFLOW;
            }
        }
        if (isVariablesWapi(config, netId)) {
            return HWFLOW;
        }
        boolean hasSetKey = HWFLOW;
        if (config.wepKeys != null) {
            int i = 0;
            while (i < config.wepKeys.length) {
                if (newNetwork || existingConfig == null || config.wepKeys[i] == null || existingConfig.wepKeys[i] == null) {
                    z = HWFLOW;
                } else {
                    z = existingConfig.wepKeys[i].equals(config.wepKeys[i]);
                }
                if (!(z || config.wepKeys[i] == null || config.wepKeys[i].equals("*"))) {
                    if (this.mWifiNative.setNetworkVariable(netId, WifiConfiguration.wepKeyVarNames[i], config.wepKeys[i])) {
                        hasSetKey = DBG;
                    } else {
                        loge("failed to set wep_key" + i + ": " + config.wepKeys[i]);
                        return HWFLOW;
                    }
                }
                i++;
            }
        }
        if (hasSetKey) {
            if (!this.mWifiNative.setNetworkVariable(netId, "wep_tx_keyidx", Integer.toString(config.wepTxKeyIndex))) {
                loge("failed to set wep_tx_keyidx: " + config.wepTxKeyIndex);
                return HWFLOW;
            }
        }
        if (!(newNetwork || existingConfig == null)) {
            if (config.priority != existingConfig.priority) {
            }
            if (!(newNetwork || existingConfig == null)) {
                if (existingConfig.hiddenSSID != config.hiddenSSID) {
                }
                if (!(newNetwork || existingConfig == null)) {
                    if (existingConfig.requirePMF != config.requirePMF) {
                    }
                    if (!newNetwork || existingConfig == null || existingConfig.updateIdentifier == null || config.updateIdentifier == null) {
                        z = HWFLOW;
                    } else {
                        z = existingConfig.updateIdentifier.equals(config.updateIdentifier);
                    }
                    if (!(z || config.updateIdentifier == null)) {
                        if (!this.mWifiNative.setNetworkVariable(netId, "update_identifier", config.updateIdentifier)) {
                            loge(config.SSID + ": failed to set updateIdentifier: " + config.updateIdentifier);
                            return HWFLOW;
                        }
                    }
                    return DBG;
                }
                if (config.requirePMF) {
                    if (!this.mWifiNative.setNetworkVariable(netId, "ieee80211w", Integer.toString(STORED_VALUE_FOR_REQUIRE_PMF))) {
                        loge(config.SSID + ": failed to set requirePMF: " + config.requirePMF);
                        return HWFLOW;
                    }
                }
                if (newNetwork) {
                }
                z = HWFLOW;
                if (this.mWifiNative.setNetworkVariable(netId, "update_identifier", config.updateIdentifier)) {
                    loge(config.SSID + ": failed to set updateIdentifier: " + config.updateIdentifier);
                    return HWFLOW;
                }
                return DBG;
            }
            if (config.hiddenSSID) {
                if (!this.mWifiNative.setNetworkVariable(netId, "scan_ssid", Integer.toString(config.hiddenSSID ? 1 : 0))) {
                    loge(config.SSID + ": failed to set hiddenSSID: " + config.hiddenSSID);
                    return HWFLOW;
                }
            }
            if (existingConfig.requirePMF != config.requirePMF) {
                if (config.requirePMF) {
                    if (this.mWifiNative.setNetworkVariable(netId, "ieee80211w", Integer.toString(STORED_VALUE_FOR_REQUIRE_PMF))) {
                        loge(config.SSID + ": failed to set requirePMF: " + config.requirePMF);
                        return HWFLOW;
                    }
                }
            }
            if (newNetwork) {
            }
            z = HWFLOW;
            if (this.mWifiNative.setNetworkVariable(netId, "update_identifier", config.updateIdentifier)) {
                loge(config.SSID + ": failed to set updateIdentifier: " + config.updateIdentifier);
                return HWFLOW;
            }
            return DBG;
        }
        if (!this.mWifiNative.setNetworkVariable(netId, "priority", Integer.toString(config.priority))) {
            loge(config.SSID + ": failed to set priority: " + config.priority);
            return HWFLOW;
        }
        if (existingConfig.hiddenSSID != config.hiddenSSID) {
            if (config.hiddenSSID) {
                if (config.hiddenSSID) {
                }
                if (this.mWifiNative.setNetworkVariable(netId, "scan_ssid", Integer.toString(config.hiddenSSID ? 1 : 0))) {
                    loge(config.SSID + ": failed to set hiddenSSID: " + config.hiddenSSID);
                    return HWFLOW;
                }
            }
        }
        if (existingConfig.requirePMF != config.requirePMF) {
            if (config.requirePMF) {
                if (this.mWifiNative.setNetworkVariable(netId, "ieee80211w", Integer.toString(STORED_VALUE_FOR_REQUIRE_PMF))) {
                    loge(config.SSID + ": failed to set requirePMF: " + config.requirePMF);
                    return HWFLOW;
                }
            }
        }
        if (newNetwork) {
        }
        z = HWFLOW;
        if (this.mWifiNative.setNetworkVariable(netId, "update_identifier", config.updateIdentifier)) {
            loge(config.SSID + ": failed to set updateIdentifier: " + config.updateIdentifier);
            return HWFLOW;
        }
        return DBG;
    }

    private boolean updateNetworkKeys(WifiConfiguration config, WifiConfiguration existingConfig) {
        WifiEnterpriseConfig wifiEnterpriseConfig = null;
        WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
        if (needsKeyStore(enterpriseConfig)) {
            try {
                String keyId = config.getKeyIdForCredentials(existingConfig);
                if (existingConfig != null) {
                    wifiEnterpriseConfig = existingConfig.enterpriseConfig;
                }
                if (!installKeys(wifiEnterpriseConfig, enterpriseConfig, keyId)) {
                    loge(config.SSID + ": failed to install keys");
                    return HWFLOW;
                }
            } catch (IllegalStateException e) {
                loge(config.SSID + " invalid config for key installation: " + e.getMessage());
                return HWFLOW;
            }
        }
        if (enterpriseConfig.saveToSupplicant(new SupplicantSaver(config, existingConfig))) {
            return DBG;
        }
        removeKeys(enterpriseConfig);
        return HWFLOW;
    }

    public boolean addOrUpdateNetwork(WifiConfiguration config, WifiConfiguration existingConfig) {
        if (config == null) {
            return HWFLOW;
        }
        if (VDBG) {
            localLog("addOrUpdateNetwork: " + config.networkId);
        }
        int netId = config.networkId;
        boolean newNetwork = HWFLOW;
        if (netId == -1) {
            newNetwork = DBG;
            netId = this.mWifiNative.addNetwork();
            if (netId < 0) {
                loge("Failed to add a network!");
                return HWFLOW;
            }
            logi("addOrUpdateNetwork created netId=" + netId);
            config.networkId = netId;
        }
        if (!saveNetwork(config, netId, newNetwork, existingConfig)) {
            if (newNetwork) {
                this.mWifiNative.removeNetwork(netId);
                loge("Failed to set a network variable, removed network: " + netId);
            }
            return HWFLOW;
        } else if (config.enterpriseConfig != null && config.enterpriseConfig.getEapMethod() != -1) {
            return updateNetworkKeys(config, existingConfig);
        } else {
            this.mBackupManagerProxy.notifyDataChanged();
            return DBG;
        }
    }

    public boolean removeNetwork(WifiConfiguration config) {
        if (config == null) {
            return HWFLOW;
        }
        if (VDBG) {
            localLog("removeNetwork: " + config.networkId);
        }
        if (this.mWifiNative.removeNetwork(config.networkId)) {
            if (config.enterpriseConfig != null) {
                log("keep keys.");
            }
            this.mBackupManagerProxy.notifyDataChanged();
            return DBG;
        }
        loge("Remove network in wpa_supplicant failed on " + config.networkId);
        return HWFLOW;
    }

    public boolean selectNetwork(WifiConfiguration config, Collection<WifiConfiguration> configs) {
        if (config == null) {
            return HWFLOW;
        }
        if (VDBG) {
            localLog("selectNetwork: " + config.networkId);
        }
        if (this.mWifiNative.selectNetwork(config.networkId)) {
            config.status = STORED_VALUE_FOR_REQUIRE_PMF;
            markAllNetworksDisabledExcept(config.networkId, configs);
            return DBG;
        }
        loge("Select network in wpa_supplicant failed on " + config.networkId);
        return HWFLOW;
    }

    boolean disableNetwork(WifiConfiguration config) {
        if (config == null) {
            return HWFLOW;
        }
        if (VDBG) {
            localLog("disableNetwork: " + config.networkId);
        }
        if (this.mWifiNative.disableNetwork(config.networkId)) {
            config.status = 1;
            return DBG;
        }
        loge("Disable network in wpa_supplicant failed on " + config.networkId);
        return HWFLOW;
    }

    public boolean setNetworkPriority(WifiConfiguration config, int priority) {
        if (config == null) {
            return HWFLOW;
        }
        if (VDBG) {
            localLog("setNetworkPriority: " + config.networkId);
        }
        if (this.mWifiNative.setNetworkVariable(config.networkId, "priority", Integer.toString(priority))) {
            config.priority = priority;
            return DBG;
        }
        loge("Set priority of network in wpa_supplicant failed on " + config.networkId);
        return HWFLOW;
    }

    public boolean setNetworkSSID(WifiConfiguration config, String ssid) {
        if (config == null) {
            return HWFLOW;
        }
        if (VDBG) {
            localLog("setNetworkSSID: " + config.networkId);
        }
        if (this.mWifiNative.setNetworkVariable(config.networkId, "ssid", encodeSSID(ssid))) {
            config.SSID = ssid;
            return DBG;
        }
        loge("Set SSID of network in wpa_supplicant failed on " + config.networkId);
        return HWFLOW;
    }

    public boolean setNetworkBSSID(WifiConfiguration config, String bssid) {
        if (config == null || (config.networkId == -1 && config.SSID == null)) {
            return HWFLOW;
        }
        if (VDBG) {
            localLog("setNetworkBSSID: " + config.networkId);
        }
        if (this.mWifiNative.setNetworkVariable(config.networkId, "bssid", bssid)) {
            config.getNetworkSelectionStatus().setNetworkSelectionBSSID(bssid);
            return DBG;
        }
        loge("Set BSSID of network in wpa_supplicant failed on " + config.networkId);
        return HWFLOW;
    }

    public void enableHS20(boolean enable) {
        this.mWifiNative.setHs20(enable);
    }

    public boolean disableAllNetworks(Collection<WifiConfiguration> configs) {
        if (VDBG) {
            localLog("disableAllNetworks");
        }
        boolean networkDisabled = HWFLOW;
        for (WifiConfiguration enabled : configs) {
            if (disableNetwork(enabled)) {
                networkDisabled = DBG;
            }
        }
        saveConfig();
        return networkDisabled;
    }

    public boolean saveConfig() {
        return this.mWifiNative.saveConfig();
    }

    public Map<String, String> readNetworkVariablesFromSupplicantFile(String key) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        Map<String, String> result = new HashMap();
        BufferedReader bufferedReader = null;
        if (HWLOGW_E) {
            loge("readNetworkVariablesFromSupplicantFile key=" + key);
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(SUPPLICANT_CONFIG_FILE));
            try {
                result = readNetworkVariablesFromReader(reader, key);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e3) {
                        if (VDBG) {
                            loge("Could not close reader for /data/misc/wifi/wpa_supplicant.conf, " + e3);
                        }
                    }
                }
                bufferedReader = reader;
            } catch (FileNotFoundException e4) {
                e2 = e4;
                bufferedReader = reader;
                if (HWLOGW_E) {
                    loge("Could not open /data/misc/wifi/wpa_supplicant.conf, " + e2);
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e32) {
                        if (VDBG) {
                            loge("Could not close reader for /data/misc/wifi/wpa_supplicant.conf, " + e32);
                        }
                    }
                }
                return result;
            } catch (IOException e5) {
                e32 = e5;
                bufferedReader = reader;
                try {
                    if (HWLOGW_E) {
                        loge("Could not read /data/misc/wifi/wpa_supplicant.conf, " + e32);
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e322) {
                            if (VDBG) {
                                loge("Could not close reader for /data/misc/wifi/wpa_supplicant.conf, " + e322);
                            }
                        }
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e3222) {
                            if (VDBG) {
                                loge("Could not close reader for /data/misc/wifi/wpa_supplicant.conf, " + e3222);
                            }
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = reader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            if (HWLOGW_E) {
                loge("Could not open /data/misc/wifi/wpa_supplicant.conf, " + e2);
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            return result;
        } catch (IOException e7) {
            e3222 = e7;
            if (HWLOGW_E) {
                loge("Could not read /data/misc/wifi/wpa_supplicant.conf, " + e3222);
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            return result;
        }
        return result;
    }

    public Map<String, String> readNetworkVariablesFromReader(BufferedReader reader, String key) throws IOException {
        Map<String, String> result = new HashMap();
        if (HWLOGW_E) {
            localLog("readNetworkVariablesFromReader key=" + key);
        }
        boolean found = HWFLOW;
        String configKey = null;
        String value = null;
        String line = reader.readLine();
        while (line != null) {
            if (line.matches("[ \\t]*network=\\{")) {
                found = DBG;
                configKey = null;
                value = null;
            } else if (line.matches("[ \\t]*\\}")) {
                found = HWFLOW;
                configKey = null;
                value = null;
            }
            if (found) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("id_str=")) {
                    try {
                        JSONObject json = new JSONObject(URLDecoder.decode(trimmedLine.substring(8, trimmedLine.length() - 1), "UTF-8"));
                        if (json.has(ID_STRING_KEY_CONFIG_KEY)) {
                            Object configKeyFromJson = json.get(ID_STRING_KEY_CONFIG_KEY);
                            if (configKeyFromJson instanceof String) {
                                configKey = (String) configKeyFromJson;
                            }
                        }
                    } catch (JSONException e) {
                        if (VDBG) {
                            loge("Could not get configKey, " + e);
                        }
                    }
                }
                if (trimmedLine.startsWith(key + "=")) {
                    value = trimmedLine.substring(key.length() + 1);
                }
                if (!(configKey == null || value == null)) {
                    result.put(configKey, value);
                }
            }
            line = reader.readLine();
        }
        return result;
    }

    public boolean isSimConfig(WifiConfiguration config) {
        boolean z = DBG;
        if (config == null || config.enterpriseConfig == null) {
            return HWFLOW;
        }
        int method = config.enterpriseConfig.getEapMethod();
        if (!(method == 4 || method == 5 || method == 6)) {
            z = HWFLOW;
        }
        return z;
    }

    public void resetSimNetworks(Collection<WifiConfiguration> configs) {
        if (VDBG) {
            localLog("resetSimNetworks");
        }
        for (WifiConfiguration config : configs) {
            if (isSimConfig(config)) {
                this.mWifiNative.setNetworkVariable(config.networkId, "identity", "NULL");
                this.mWifiNative.setNetworkVariable(config.networkId, "anonymous_identity", "NULL");
            }
        }
    }

    public void clearBssidBlacklist() {
        if (VDBG) {
            localLog("clearBlacklist");
        }
        this.mBssidBlacklist.clear();
        this.mWifiNative.clearBlacklist();
        this.mWifiNative.setBssidBlacklist(null);
    }

    public void blackListBssid(String bssid) {
        if (bssid != null) {
            if (VDBG) {
                localLog("blackListBssid: " + bssid);
            }
            this.mBssidBlacklist.add(bssid);
            this.mWifiNative.addToBlacklist(bssid);
            this.mWifiNative.setBssidBlacklist((String[]) this.mBssidBlacklist.toArray(new String[this.mBssidBlacklist.size()]));
        }
    }

    public boolean isBssidBlacklisted(String bssid) {
        return this.mBssidBlacklist.contains(bssid);
    }

    private void markAllNetworksDisabledExcept(int netId, Collection<WifiConfiguration> configs) {
        for (WifiConfiguration config : configs) {
            if (!(config == null || config.networkId == netId || config.status == 1)) {
                config.status = 1;
            }
        }
    }

    private void markAllNetworksDisabled(Collection<WifiConfiguration> configs) {
        markAllNetworksDisabledExcept(-1, configs);
    }

    public WpsResult startWpsWithPinFromAccessPoint(WpsInfo config, Collection<WifiConfiguration> configs) {
        WpsResult result = new WpsResult();
        if (this.mWifiNative.startWpsRegistrar(config.BSSID, config.pin)) {
            markAllNetworksDisabled(configs);
            result.status = Status.SUCCESS;
        } else {
            loge("Failed to start WPS pin method configuration");
            result.status = Status.FAILURE;
        }
        return result;
    }

    public WpsResult startWpsWithPinFromDevice(WpsInfo config, Collection<WifiConfiguration> configs) {
        WpsResult result = new WpsResult();
        result.pin = this.mWifiNative.startWpsPinDisplay(config.BSSID);
        if (TextUtils.isEmpty(result.pin)) {
            loge("Failed to start WPS pin method configuration");
            result.status = Status.FAILURE;
        } else {
            markAllNetworksDisabled(configs);
            result.status = Status.SUCCESS;
        }
        return result;
    }

    public WpsResult startWpsPbc(WpsInfo config, Collection<WifiConfiguration> configs) {
        WpsResult result = new WpsResult();
        if (this.mWifiNative.startWpsPbc(config.BSSID)) {
            markAllNetworksDisabled(configs);
            result.status = Status.SUCCESS;
        } else {
            loge("Failed to start WPS push button configuration");
            result.status = Status.FAILURE;
        }
        return result;
    }

    protected void logd(String s) {
        Log.d(TAG, s);
    }

    protected void logi(String s) {
        Log.i(TAG, s);
    }

    protected void loge(String s) {
        loge(s, HWFLOW);
    }

    protected void loge(String s, boolean stack) {
        if (stack) {
            Log.e(TAG, s + " stack:" + Thread.currentThread().getStackTrace()[STORED_VALUE_FOR_REQUIRE_PMF].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        } else {
            Log.e(TAG, s);
        }
    }

    protected void log(String s) {
        Log.d(TAG, s);
    }

    private void localLog(String s) {
        if (this.mLocalLog != null) {
            this.mLocalLog.log("WifiConfigStore: " + s);
        }
    }

    private void localLogAndLogcat(String s) {
        localLog(s);
        Log.d(TAG, s);
    }
}
