package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.security.Credentials;
import android.security.KeyChain;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import java.io.IOException;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class WifiKeyStore {
    private static final String TAG = "WifiKeyStore";
    private final KeyStore mKeyStore;
    private boolean mVerboseLoggingEnabled = false;

    WifiKeyStore(KeyStore keyStore) {
        this.mKeyStore = keyStore;
    }

    void enableVerboseLogging(boolean verbose) {
        this.mVerboseLoggingEnabled = verbose;
    }

    private static boolean needsKeyStore(WifiEnterpriseConfig config) {
        return (config.getClientCertificate() == null && config.getCaCertificate() == null) ? false : true;
    }

    private static boolean isHardwareBackedKey(Key key) {
        return KeyChain.isBoundKeyAlgorithm(key.getAlgorithm());
    }

    private static boolean hasHardwareBackedKey(Certificate certificate) {
        return isHardwareBackedKey(certificate.getPublicKey());
    }

    private boolean installKeys(WifiEnterpriseConfig existingConfig, WifiEnterpriseConfig config, String name) {
        boolean ret = true;
        String privKeyName = "USRPKEY_" + name;
        String userCertName = "USRCERT_" + name;
        Certificate[] clientCertificateChain = config.getClientCertificateChain();
        if (!(clientCertificateChain == null || clientCertificateChain.length == 0)) {
            byte[] privKeyData = config.getClientPrivateKey().getEncoded();
            if (this.mVerboseLoggingEnabled) {
                if (isHardwareBackedKey(config.getClientPrivateKey())) {
                    Log.d(TAG, "importing keys " + name + " in hardware backed store");
                } else {
                    Log.d(TAG, "importing keys " + name + " in software backed store");
                }
            }
            ret = this.mKeyStore.importKey(privKeyName, privKeyData, 1010, 0);
            if (!ret) {
                return ret;
            }
            ret = putCertsInKeyStore(userCertName, clientCertificateChain);
            if (!ret) {
                this.mKeyStore.delete(privKeyName, 1010);
                return ret;
            }
        }
        X509Certificate[] caCertificates = config.getCaCertificates();
        Set<String> oldCaCertificatesToRemove = new ArraySet();
        if (!(existingConfig == null || existingConfig.getCaCertificateAliases() == null)) {
            oldCaCertificatesToRemove.addAll(Arrays.asList(existingConfig.getCaCertificateAliases()));
        }
        List caCertificateAliases = null;
        if (caCertificates != null) {
            List<String> caCertificateAliases2 = new ArrayList();
            int i = 0;
            while (i < caCertificates.length) {
                String alias;
                if (caCertificates.length == 1) {
                    alias = name;
                } else {
                    alias = String.format("%s_%d", new Object[]{name, Integer.valueOf(i)});
                }
                oldCaCertificatesToRemove.remove(alias);
                ret = putCertInKeyStore("CACERT_" + alias, caCertificates[i]);
                if (ret) {
                    caCertificateAliases2.add(alias);
                    i++;
                } else {
                    if (config.getClientCertificate() != null) {
                        this.mKeyStore.delete(privKeyName, 1010);
                        this.mKeyStore.delete(userCertName, 1010);
                    }
                    for (String addedAlias : caCertificateAliases2) {
                        this.mKeyStore.delete("CACERT_" + addedAlias, 1010);
                    }
                    return ret;
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
            config.setCaCertificateAliases((String[]) caCertificateAliases2.toArray(new String[caCertificateAliases2.size()]));
            config.resetCaCertificate();
        }
        return ret;
    }

    public boolean putCertInKeyStore(String name, Certificate cert) {
        return putCertsInKeyStore(name, new Certificate[]{cert});
    }

    public boolean putCertsInKeyStore(String name, Certificate[] certs) {
        try {
            byte[] certData = Credentials.convertToPem(certs);
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "putting " + certs.length + " certificate(s) " + name + " in keystore");
            }
            return this.mKeyStore.put(name, certData, 1010, 0);
        } catch (IOException e) {
            return false;
        } catch (CertificateException e2) {
            return false;
        }
    }

    public boolean putKeyInKeyStore(String name, Key key) {
        return this.mKeyStore.importKey(name, key.getEncoded(), 1010, 0);
    }

    public boolean removeEntryFromKeyStore(String name) {
        return this.mKeyStore.delete(name, 1010);
    }

    public void removeKeys(WifiEnterpriseConfig config) {
        String client = config.getClientCertificateAlias();
        if (!TextUtils.isEmpty(client)) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "removing client private key and user cert");
            }
            this.mKeyStore.delete("USRPKEY_" + client, 1010);
            this.mKeyStore.delete("USRCERT_" + client, 1010);
        }
        String[] aliases = config.getCaCertificateAliases();
        if (aliases != null) {
            for (String ca : aliases) {
                if (!TextUtils.isEmpty(ca)) {
                    if (this.mVerboseLoggingEnabled) {
                        Log.d(TAG, "removing CA cert: " + ca);
                    }
                    this.mKeyStore.delete("CACERT_" + ca, 1010);
                }
            }
        }
    }

    public boolean updateNetworkKeys(WifiConfiguration config, WifiConfiguration existingConfig) {
        WifiEnterpriseConfig wifiEnterpriseConfig = null;
        WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
        if (needsKeyStore(enterpriseConfig)) {
            try {
                String keyId = config.getKeyIdForCredentials(existingConfig);
                if (existingConfig != null) {
                    wifiEnterpriseConfig = existingConfig.enterpriseConfig;
                }
                if (!installKeys(wifiEnterpriseConfig, enterpriseConfig, keyId)) {
                    Log.e(TAG, config.SSID + ": failed to install keys");
                    return false;
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, config.SSID + " invalid config for key installation: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public static boolean needsSoftwareBackedKeyStore(WifiEnterpriseConfig config) {
        if (TextUtils.isEmpty(config.getClientCertificateAlias())) {
            return false;
        }
        return true;
    }
}
