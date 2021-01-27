package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.security.Credentials;
import android.security.KeyChain;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class WifiKeyStore {
    private static final String TAG = "WifiKeyStore";
    private final KeyStore mKeyStore;
    private boolean mVerboseLoggingEnabled = false;

    WifiKeyStore(KeyStore keyStore) {
        this.mKeyStore = keyStore;
    }

    /* access modifiers changed from: package-private */
    public void enableVerboseLogging(boolean verbose) {
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
        String alias;
        String str = name;
        boolean ret = true;
        String privKeyName = "USRPKEY_" + str;
        String userCertName = "USRCERT_" + str;
        Certificate[] clientCertificateChain = config.getClientCertificateChain();
        int i = 1010;
        if (!(clientCertificateChain == null || clientCertificateChain.length == 0)) {
            byte[] privKeyData = config.getClientPrivateKey().getEncoded();
            if (this.mVerboseLoggingEnabled) {
                if (isHardwareBackedKey(config.getClientPrivateKey())) {
                    Log.i(TAG, "importing keys " + str + " in hardware backed store");
                } else {
                    Log.i(TAG, "importing keys " + str + " in software backed store");
                }
            }
            boolean ret2 = this.mKeyStore.importKey(privKeyName, privKeyData, 1010, 0);
            if (!ret2) {
                return ret2;
            }
            ret = putCertsInKeyStore(userCertName, clientCertificateChain);
            if (!ret) {
                this.mKeyStore.delete(privKeyName, 1010);
                return ret;
            }
        }
        X509Certificate[] caCertificates = config.getCaCertificates();
        Set<String> oldCaCertificatesToRemove = new ArraySet<>();
        if (!(existingConfig == null || existingConfig.getCaCertificateAliases() == null)) {
            oldCaCertificatesToRemove.addAll(Arrays.asList(existingConfig.getCaCertificateAliases()));
        }
        List<String> caCertificateAliases = null;
        if (caCertificates != null) {
            caCertificateAliases = new ArrayList<>();
            int i2 = 0;
            while (i2 < caCertificates.length) {
                if (caCertificates.length == 1) {
                    alias = str;
                } else {
                    alias = String.format("%s_%d", str, Integer.valueOf(i2));
                }
                oldCaCertificatesToRemove.remove(alias);
                ret = putCertInKeyStore("CACERT_" + alias, caCertificates[i2]);
                if (!ret) {
                    if (config.getClientCertificate() != null) {
                        this.mKeyStore.delete(privKeyName, i);
                        this.mKeyStore.delete(userCertName, i);
                    }
                    Iterator<String> it = caCertificateAliases.iterator();
                    while (it.hasNext()) {
                        this.mKeyStore.delete("CACERT_" + it.next(), 1010);
                    }
                    return ret;
                }
                caCertificateAliases.add(alias);
                i2++;
                str = name;
                i = 1010;
            }
        }
        Iterator<String> it2 = oldCaCertificatesToRemove.iterator();
        while (it2.hasNext()) {
            this.mKeyStore.delete("CACERT_" + it2.next(), 1010);
        }
        if (config.getClientCertificate() != null) {
            config.setClientCertificateAlias(name);
            config.resetClientKeyEntry();
        }
        if (caCertificates != null) {
            config.setCaCertificateAliases((String[]) caCertificateAliases.toArray(new String[caCertificateAliases.size()]));
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
                Log.i(TAG, "putting " + certs.length + " certificate(s) " + name + " in keystore");
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
        String[] aliases;
        if (config.isAppInstalledDeviceKeyAndCert()) {
            String client = config.getClientCertificateAlias();
            if (!TextUtils.isEmpty(client)) {
                if (this.mVerboseLoggingEnabled) {
                    Log.i(TAG, "removing client private key and user cert");
                }
                this.mKeyStore.delete("USRPKEY_" + client, 1010);
                this.mKeyStore.delete("USRCERT_" + client, 1010);
            }
        }
        if (config.isAppInstalledCaCert() && (aliases = config.getCaCertificateAliases()) != null) {
            for (String ca : aliases) {
                if (!TextUtils.isEmpty(ca)) {
                    if (this.mVerboseLoggingEnabled) {
                        Log.i(TAG, "removing CA cert: " + ca);
                    }
                    this.mKeyStore.delete("CACERT_" + ca, 1010);
                }
            }
        }
    }

    private X509Certificate buildCACertificate(byte[] certData) {
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certData));
        } catch (CertificateException e) {
            return null;
        }
    }

    public boolean updateNetworkKeys(WifiConfiguration config, WifiConfiguration existingConfig) {
        WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
        if (!needsKeyStore(enterpriseConfig)) {
            return true;
        }
        try {
            if (!installKeys(existingConfig != null ? existingConfig.enterpriseConfig : null, enterpriseConfig, config.getKeyIdForCredentials(existingConfig))) {
                Log.e(TAG, StringUtilEx.safeDisplaySsid(config.SSID) + ": failed to install keys");
                return false;
            }
            if (config.allowedKeyManagement.get(10)) {
                KeyStore keyStore = this.mKeyStore;
                byte[] certData = keyStore.get("CACERT_" + config.enterpriseConfig.getCaCertificateAlias(), 1010);
                if (certData == null) {
                    Log.e(TAG, "Failed reading CA certificate for Suite-B");
                    return false;
                }
                X509Certificate x509CaCert = buildCACertificate(certData);
                if (x509CaCert != null) {
                    String sigAlgOid = x509CaCert.getSigAlgOID();
                    if (this.mVerboseLoggingEnabled) {
                        Log.i(TAG, "Signature algorithm: " + sigAlgOid);
                    }
                    config.allowedSuiteBCiphers.clear();
                    if (sigAlgOid.equals("1.2.840.113549.1.1.12")) {
                        config.allowedSuiteBCiphers.set(1);
                        if (this.mVerboseLoggingEnabled) {
                            Log.i(TAG, "Selecting Suite-B RSA");
                        }
                    } else if (sigAlgOid.equals("1.2.840.10045.4.3.3")) {
                        config.allowedSuiteBCiphers.set(0);
                        if (this.mVerboseLoggingEnabled) {
                            Log.i(TAG, "Selecting Suite-B ECDSA");
                        }
                    } else {
                        Log.e(TAG, "Invalid CA certificate type for Suite-B: " + sigAlgOid);
                        return false;
                    }
                } else {
                    Log.e(TAG, "Invalid CA certificate for Suite-B");
                    return false;
                }
            }
            return true;
        } catch (IllegalStateException e) {
            Log.e(TAG, StringUtilEx.safeDisplaySsid(config.SSID) + " invalid config for key installation: " + e.getMessage());
            return false;
        }
    }
}
