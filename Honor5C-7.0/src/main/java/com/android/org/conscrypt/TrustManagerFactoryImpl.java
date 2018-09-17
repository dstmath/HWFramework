package com.android.org.conscrypt;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

public class TrustManagerFactoryImpl extends TrustManagerFactorySpi {
    private KeyStore keyStore;

    public void engineInit(KeyStore ks) throws KeyStoreException {
        if (ks != null) {
            this.keyStore = ks;
            return;
        }
        this.keyStore = KeyStore.getInstance("AndroidCAStore");
        try {
            this.keyStore.load(null, null);
        } catch (IOException e) {
            throw new KeyStoreException(e);
        } catch (CertificateException e2) {
            throw new KeyStoreException(e2);
        } catch (NoSuchAlgorithmException e3) {
            throw new KeyStoreException(e3);
        }
    }

    public void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        throw new InvalidAlgorithmParameterException("ManagerFactoryParameters not supported");
    }

    public TrustManager[] engineGetTrustManagers() {
        if (this.keyStore == null) {
            throw new IllegalStateException("TrustManagerFactory is not initialized");
        }
        return new TrustManager[]{new TrustManagerImpl(this.keyStore)};
    }
}
