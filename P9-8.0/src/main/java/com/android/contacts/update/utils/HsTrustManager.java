package com.android.contacts.update.utils;

import com.android.contacts.hwsdk.TrustManagerImplF;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class HsTrustManager implements X509TrustManager {
    private X509TrustManager tm = null;

    public HsTrustManager() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidCAStore");
            keyStore.load(null, null);
            this.tm = TrustManagerImplF.newInstance(keyStore);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        this.tm.checkClientTrusted(chain, authType);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            this.tm.checkServerTrusted(chain, authType);
        } catch (Throwable e) {
            e.printStackTrace();
            Throwable t = e;
            while (t != null) {
                if (!(t instanceof CertificateExpiredException) && !(t instanceof CertificateNotYetValidException)) {
                    t = t.getCause();
                } else {
                    return;
                }
            }
            throw e;
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return this.tm.getAcceptedIssuers();
    }
}
