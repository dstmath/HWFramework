package com.huawei.security.conscrypt;

import android.util.Log;
import com.huawei.security.HwKeystoreManager;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

public class HwTrustManager {
    private static final String[] ECC_EQU_SUBCA_SUBJECT = {"CN=Huawei CBG Equipment S1 CA,OU=Huawei CBG,O=Huawei,C=CN", "CN=Huawei CBG Equipment S2 CA,OU=Huawei CBG,O=Huawei,C=CN", "CN=Huawei CBG Equipment S3 CA,OU=Huawei CBG,O=Huawei,C=CN"};
    private static final String[] RSA_EQU_SUBCA_SUBJECT = {"CN=Huawei CBG Mobile Equipment CA,OU=Huawei CBG,O=Huawei,C=CN", "CN=Huawei CBG IOT Equipment CA,OU=Huawei CBG,O=Huawei,C=CN"};
    private static final String TAG = "HwTrustManager";
    private HwKeystoreManager mKeyStore;
    private HwTrustCertificateStore mTrustCertStore;

    public HwTrustManager() {
        this.mKeyStore = HwKeystoreManager.getInstance();
        this.mTrustCertStore = null;
        this.mTrustCertStore = new HwTrustCertificateStore(this.mKeyStore);
    }

    public boolean checkCaCertificateSubject(X509Certificate cert) throws CertificateException {
        Log.e(TAG, "CheckCaCertificateSubject start ");
        if (cert != null) {
            String algName = cert.getSigAlgName();
            String subjectName = cert.getSubjectX500Principal().getName();
            if ("SHA256withRSA".equals(algName)) {
                int i = 0;
                while (true) {
                    String[] strArr = RSA_EQU_SUBCA_SUBJECT;
                    if (i >= strArr.length) {
                        return false;
                    }
                    if (subjectName.equals(strArr[i])) {
                        return true;
                    }
                    i++;
                }
            } else if ("SHA384withECDSA".equals(algName)) {
                int i2 = 0;
                while (true) {
                    String[] strArr2 = ECC_EQU_SUBCA_SUBJECT;
                    if (i2 >= strArr2.length) {
                        return false;
                    }
                    if (subjectName.equals(strArr2[i2])) {
                        return true;
                    }
                    i2++;
                }
            } else {
                Log.e(TAG, "CheckCaCertificateSubject failed, not support " + algName);
                return false;
            }
        } else {
            throw new NullPointerException("Ca certificate is null.");
        }
    }

    public boolean verifyRootCertificate(X509Certificate cert) throws CertificateException {
        Log.e(TAG, "verifyRootCertificate start ");
        if (cert != null) {
            try {
                cert.verify(cert.getPublicKey());
                if (this.mTrustCertStore.getTrustAnchor(cert) != null) {
                    return true;
                }
                return false;
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
                throw new CertificateException("Cert chain verify failed!" + e.getMessage());
            }
        } else {
            throw new NullPointerException("Root certificate is null.");
        }
    }

    public boolean verifyCertificateChain(X509Certificate[] chain) throws CertificateException {
        return verifyDeviceCertificateChain(chain);
    }

    public boolean verifyDeviceCertificateChain(X509Certificate[] chain) throws CertificateException {
        if (chain == null) {
            throw new NullPointerException("Certificate chain is null.");
        } else if (chain.length >= 2) {
            try {
                chain[0].checkValidity();
            } catch (CertificateNotYetValidException e) {
                Log.e(TAG, "cert 0 not valid yet, ignore it.");
            }
            ChainStrengthAnalyzer.checkCert(chain[0]);
            for (int i = 1; i < chain.length; i++) {
                chain[i].checkValidity();
                ChainStrengthAnalyzer.checkCert(chain[i]);
                try {
                    chain[i - 1].verify(chain[i].getPublicKey());
                    if (i == chain.length - 1) {
                        if (checkCaCertificateSubject(chain[i - 1])) {
                            return verifyRootCertificate(chain[i]);
                        } else {
                            Log.e(TAG, "verifyCaCertificate return false");
                            return false;
                        }
                    }
                } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e2) {
                    throw new CertificateException("Cert chain verify failed!" + e2.getMessage());
                }
            }
            return false;
        } else {
            throw new UnsupportedOperationException("UnsupportedOperationException.");
        }
    }
}
