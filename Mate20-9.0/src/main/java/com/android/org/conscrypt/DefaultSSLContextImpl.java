package com.android.org.conscrypt;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public final class DefaultSSLContextImpl extends OpenSSLContextImpl {
    private static KeyManager[] KEY_MANAGERS;
    private static TrustManager[] TRUST_MANAGERS;

    /* access modifiers changed from: package-private */
    public KeyManager[] getKeyManagers() throws GeneralSecurityException, IOException {
        if (KEY_MANAGERS != null) {
            return KEY_MANAGERS;
        }
        String keystore = System.getProperty("javax.net.ssl.keyStore");
        InputStream is = null;
        if (keystore == null) {
            return null;
        }
        String keystorepwd = System.getProperty("javax.net.ssl.keyStorePassword");
        char[] pwd = keystorepwd == null ? null : keystorepwd.toCharArray();
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            is = new BufferedInputStream(new FileInputStream(keystore));
            ks.load(is, pwd);
            is.close();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, pwd);
            KEY_MANAGERS = kmf.getKeyManagers();
            return KEY_MANAGERS;
        } catch (Throwable th) {
            if (is != null) {
                is.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public TrustManager[] getTrustManagers() throws GeneralSecurityException, IOException {
        if (TRUST_MANAGERS != null) {
            return TRUST_MANAGERS;
        }
        String keystore = System.getProperty("javax.net.ssl.trustStore");
        InputStream is = null;
        if (keystore == null) {
            return null;
        }
        String keystorepwd = System.getProperty("javax.net.ssl.trustStorePassword");
        char[] pwd = keystorepwd == null ? null : keystorepwd.toCharArray();
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            is = new BufferedInputStream(new FileInputStream(keystore));
            ks.load(is, pwd);
            is.close();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            TRUST_MANAGERS = tmf.getTrustManagers();
            return TRUST_MANAGERS;
        } catch (Throwable th) {
            if (is != null) {
                is.close();
            }
            throw th;
        }
    }

    public void engineInit(KeyManager[] kms, TrustManager[] tms, SecureRandom sr) throws KeyManagementException {
        throw new KeyManagementException("Do not init() the default SSLContext ");
    }
}
