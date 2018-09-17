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

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0053  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    KeyManager[] getKeyManagers() throws GeneralSecurityException, IOException {
        Throwable th;
        if (KEY_MANAGERS != null) {
            return KEY_MANAGERS;
        }
        String keystore = System.getProperty("javax.net.ssl.keyStore");
        if (keystore == null) {
            return null;
        }
        String keystorepwd = System.getProperty("javax.net.ssl.keyStorePassword");
        char[] pwd = keystorepwd == null ? null : keystorepwd.toCharArray();
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream is = null;
        try {
            InputStream is2 = new BufferedInputStream(new FileInputStream(keystore));
            try {
                ks.load(is2, pwd);
                if (is2 != null) {
                    is2.close();
                }
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, pwd);
                KEY_MANAGERS = kmf.getKeyManagers();
                return KEY_MANAGERS;
            } catch (Throwable th2) {
                th = th2;
                is = is2;
                if (is != null) {
                    is.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (is != null) {
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0053  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    TrustManager[] getTrustManagers() throws GeneralSecurityException, IOException {
        Throwable th;
        if (TRUST_MANAGERS != null) {
            return TRUST_MANAGERS;
        }
        String keystore = System.getProperty("javax.net.ssl.trustStore");
        if (keystore == null) {
            return null;
        }
        String keystorepwd = System.getProperty("javax.net.ssl.trustStorePassword");
        char[] pwd = keystorepwd == null ? null : keystorepwd.toCharArray();
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream is = null;
        try {
            InputStream is2 = new BufferedInputStream(new FileInputStream(keystore));
            try {
                ks.load(is2, pwd);
                if (is2 != null) {
                    is2.close();
                }
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);
                TRUST_MANAGERS = tmf.getTrustManagers();
                return TRUST_MANAGERS;
            } catch (Throwable th2) {
                th = th2;
                is = is2;
                if (is != null) {
                    is.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (is != null) {
            }
            throw th;
        }
    }

    public void engineInit(KeyManager[] kms, TrustManager[] tms, SecureRandom sr) throws KeyManagementException {
        throw new KeyManagementException("Do not init() the default SSLContext ");
    }
}
