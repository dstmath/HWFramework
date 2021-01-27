package com.android.org.conscrypt;

import java.security.KeyStore;
import javax.net.ssl.X509TrustManager;

public class TrustManagerImplEx {
    public static final X509TrustManager newInstance(KeyStore keyStore) {
        return new TrustManagerImpl(keyStore);
    }
}
