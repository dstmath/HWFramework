package com.android.server.devicepolicy;

import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.x509.BasicConstraints;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class CertInstallHelper {
    private static final String TAG = "DPMS_CerInstallHelper";

    private CertInstallHelper() {
    }

    private static boolean isCa(X509Certificate cert) {
        ASN1InputStream asn1InputString = null;
        try {
            byte[] asn1EncodedBytes = cert.getExtensionValue("2.5.29.19");
            if (asn1EncodedBytes == null) {
                if (0 != 0) {
                    try {
                        asn1InputString.close();
                    } catch (IOException e) {
                        HwLog.e(TAG, "IOException when close asn1InputString");
                    }
                }
                return false;
            }
            ASN1InputStream asn1InputString2 = new ASN1InputStream(asn1EncodedBytes);
            ASN1Primitive primitive = asn1InputString2.readObject();
            if (primitive instanceof DEROctetString) {
                asn1InputString2 = new ASN1InputStream(((DEROctetString) primitive).getOctets());
                ASN1Primitive primitive2 = asn1InputString2.readObject();
                if (primitive2 instanceof ASN1Sequence) {
                    boolean isCA = BasicConstraints.getInstance((ASN1Sequence) primitive2).isCA();
                    try {
                        asn1InputString2.close();
                    } catch (IOException e2) {
                        HwLog.e(TAG, "IOException when close asn1InputString");
                    }
                    return isCA;
                }
            }
            try {
                asn1InputString2.close();
            } catch (IOException e3) {
                HwLog.e(TAG, "IOException when close asn1InputString");
            }
            return false;
        } catch (IOException e4) {
            HwLog.e(TAG, "IOException when parse asn1InputString");
            if (0 != 0) {
                asn1InputString.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    asn1InputString.close();
                } catch (IOException e5) {
                    HwLog.e(TAG, "IOException when close asn1InputString");
                }
            }
            throw th;
        }
    }

    public static boolean installPkcs12Cert(String password, byte[] certBuffer, String certAlias, int certInstallType) throws Exception {
        HwLog.d(TAG, "#extracted pkcs12 certs from cert buffer");
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new ByteArrayInputStream(certBuffer), password.toCharArray());
        Enumeration<String> aliases = keystore.aliases();
        if (!aliases.hasMoreElements()) {
            return false;
        }
        KeyStore.PasswordProtection pwd = new KeyStore.PasswordProtection(password.toCharArray());
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keystore.isKeyEntry(alias)) {
                KeyStore.Entry entry = keystore.getEntry(alias, pwd);
                if (entry instanceof KeyStore.PrivateKeyEntry) {
                    return installCertToKeyStore((KeyStore.PrivateKeyEntry) entry, certAlias, certInstallType);
                }
            } else {
                HwLog.d(TAG, "Skip non-key entry, alias = " + alias);
            }
        }
        return true;
    }

    private static boolean installCertToKeyStore(KeyStore.PrivateKeyEntry entry, String alias, int certInstallType) {
        X509Certificate userCert = null;
        PrivateKey userKey = entry.getPrivateKey();
        Certificate cert = entry.getCertificate();
        if (cert instanceof X509Certificate) {
            userCert = (X509Certificate) cert;
        }
        Certificate[] certs = entry.getCertificateChain();
        List<X509Certificate> caCerts = new ArrayList<>(certs.length);
        for (Certificate crt : certs) {
            if (crt instanceof X509Certificate) {
                X509Certificate caCert = (X509Certificate) crt;
                if (isCa(caCert)) {
                    caCerts.add(caCert);
                }
            }
        }
        HwLog.d(TAG, "# ca certs extracted = " + caCerts.size());
        return CertInstaller.installCert(alias == null ? SettingsMDMPlugin.EMPTY_STRING : alias, userKey, userCert, caCerts, certInstallType);
    }

    public static boolean installX509Cert(byte[] bytes, String alias, int certInstallType) {
        if (bytes == null) {
            return false;
        }
        X509Certificate userCert = null;
        List<X509Certificate> caCerts = new ArrayList<>();
        try {
            Certificate rawCert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
            if (!(rawCert instanceof X509Certificate)) {
                return false;
            }
            X509Certificate cert = (X509Certificate) rawCert;
            if (isCa(cert)) {
                caCerts.add(cert);
            } else {
                userCert = cert;
            }
            return CertInstaller.installCert(alias == null ? SettingsMDMPlugin.EMPTY_STRING : alias, null, userCert, caCerts, certInstallType);
        } catch (CertificateException e) {
            HwLog.e(TAG, "install X509 Cert error");
        }
    }
}
