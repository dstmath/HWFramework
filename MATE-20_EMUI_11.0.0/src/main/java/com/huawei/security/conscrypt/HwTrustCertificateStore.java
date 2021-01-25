package com.huawei.security.conscrypt;

import android.util.Log;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.security.auth.x500.X500Principal;

public class HwTrustCertificateStore {
    private static final String TAG = "HwTrustCertificateStore";
    private HwKeystoreManager mKeyStore = HwKeystoreManager.getInstance();

    /* access modifiers changed from: private */
    public interface CertSelector {
        boolean match(X509Certificate x509Certificate, X509Certificate x509Certificate2);
    }

    public HwTrustCertificateStore(HwKeystoreManager keystore) {
        this.mKeyStore = keystore;
    }

    private static X509Certificate[] toCertificates(List<byte[]> bytes) {
        if (bytes == null || bytes.size() == 0) {
            Log.e(TAG, "Invalid param.");
            return null;
        }
        try {
            Log.d(TAG, "toCertificates bytes.size:" + bytes.size());
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate[] certs = new X509Certificate[bytes.size()];
            int i = 0;
            do {
                byte[] data = bytes.get(i);
                if (data != null) {
                    if (data.length != 0) {
                        certs[i] = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(data));
                        i++;
                    }
                }
                Log.e(TAG, "data is null");
                return null;
            } while (i < bytes.size());
            return certs;
        } catch (CertificateException e) {
            Log.w(TAG, "Couldn't parse certificates in keystore CertificateException", e);
            return null;
        }
    }

    public X509Certificate getTrustAnchor(X509Certificate c) {
        X509Certificate trustCert = (X509Certificate) findCert(c, c.getSubjectX500Principal(), new GetTrustAnchorCertSelector(), X509Certificate.class);
        if (trustCert != null) {
            return trustCert;
        }
        return null;
    }

    public X509Certificate findIssuer(X509Certificate c) {
        X509Certificate cert = (X509Certificate) findCert(c, c.getIssuerX500Principal(), new FindIssuerCertSelector(), X509Certificate.class);
        if (cert != null) {
            return cert;
        }
        return null;
    }

    private <T> T findCert(X509Certificate c, X500Principal subject, CertSelector selector, Class<T> desiredReturnType) {
        if (this.mKeyStore == null) {
            Log.e(TAG, "mKeyStore is null!");
            return null;
        }
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        if (this.mKeyStore.exportTrustCert(outChain) != 1) {
            Log.e(TAG, "exportTrustCert failed!");
            return null;
        }
        List<byte[]> certsByte = outChain.getCertificates();
        if (certsByte == null) {
            Log.e(TAG, "findCert failed!");
            return null;
        }
        X509Certificate[] certList = toCertificates(certsByte);
        if (certList == null) {
            Log.e(TAG, "findCert toCertificates failed!");
            return null;
        }
        for (X509Certificate x509Certificate : certList) {
            T t = (T) x509Certificate;
            boolean match = selector.match(t, c);
            boolean equals = subject != null ? subject.getName().equals(t.getSubjectX500Principal().getName()) : false;
            if (!match || !equals) {
                Log.d(TAG, "exportTrustCert match:" + match + " equals:" + equals);
            }
            if (match && equals) {
                Log.d(TAG, "findCert find the trust cert!");
                if (desiredReturnType == X509Certificate.class) {
                    return t;
                }
                if (desiredReturnType == Boolean.class) {
                    return (T) Boolean.TRUE;
                }
                throw new AssertionError();
            }
        }
        return null;
    }

    static class GetTrustAnchorCertSelector implements CertSelector {
        GetTrustAnchorCertSelector() {
        }

        @Override // com.huawei.security.conscrypt.HwTrustCertificateStore.CertSelector
        public boolean match(X509Certificate ca, X509Certificate c) {
            return ca.getPublicKey().equals(c.getPublicKey());
        }
    }

    static class FindIssuerCertSelector implements CertSelector {
        FindIssuerCertSelector() {
        }

        @Override // com.huawei.security.conscrypt.HwTrustCertificateStore.CertSelector
        public boolean match(X509Certificate ca, X509Certificate c) {
            try {
                c.verify(ca.getPublicKey());
                return true;
            } catch (CertificateException e) {
                Log.e(HwTrustCertificateStore.TAG, "FindIssuerCertSelector match fail CertificateException!");
                return false;
            } catch (NoSuchAlgorithmException e2) {
                Log.e(HwTrustCertificateStore.TAG, "FindIssuerCertSelector match fail NoSuchAlgorithmException!");
                return false;
            } catch (InvalidKeyException e3) {
                Log.e(HwTrustCertificateStore.TAG, "FindIssuerCertSelector match fail InvalidKeyException!");
                return false;
            } catch (NoSuchProviderException e4) {
                Log.e(HwTrustCertificateStore.TAG, "FindIssuerCertSelector match fail NoSuchProviderException!");
                return false;
            } catch (SignatureException e5) {
                Log.e(HwTrustCertificateStore.TAG, "FindIssuerCertSelector match fail SignatureException!");
                return false;
            }
        }
    }
}
