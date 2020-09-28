package com.huawei.security.conscrypt;

import android.util.Log;
import com.huawei.security.HwKeystoreManager;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HwTrustManager {
    private static final String TAG = "HwTrustManager";
    private HwKeystoreManager mKeyStore;
    private HwTrustCertificateStore mTrustCertStore;

    public HwTrustManager() {
        this.mKeyStore = HwKeystoreManager.getInstance();
        this.mTrustCertStore = null;
        this.mTrustCertStore = new HwTrustCertificateStore(this.mKeyStore);
    }

    public boolean verifyCertificateChain(X509Certificate[] chain) throws CertificateException {
        if (chain == null) {
            throw new NullPointerException("Certificate chain is null.");
        } else if (chain.length >= 2) {
            Log.d(TAG, "VerifyCertificateChain chain.length:" + chain.length);
            for (int i = 0; i < chain.length; i++) {
                chain[i].checkValidity();
                ChainStrengthAnalyzer.checkCert(chain[i]);
                if (i != 0) {
                    try {
                        PublicKey pubKey = chain[i].getPublicKey();
                        chain[i - 1].verify(pubKey);
                        if (i == chain.length - 1) {
                            chain[i].verify(pubKey);
                            if (this.mTrustCertStore.getTrustAnchor(chain[i]) != null) {
                                Log.d(TAG, "VerifyCertificateChain return true");
                                return true;
                            }
                        } else {
                            continue;
                        }
                    } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
                        throw new CertificateException("Cert chain verify failed!" + e.getMessage());
                    }
                }
            }
            return false;
        } else {
            throw new UnsupportedOperationException("UnsupportedOperationException.");
        }
    }
}
