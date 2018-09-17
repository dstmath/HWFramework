package com.huawei.secure.android.common;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class b implements X509TrustManager {
    protected ArrayList<X509TrustManager> ji = new ArrayList();

    public b(InputStream inputStream, String str) {
        zx(inputStream, str);
    }

    private void zx(InputStream inputStream, String str) {
        int i = 0;
        TrustManagerFactory instance = TrustManagerFactory.getInstance("X509");
        KeyStore instance2 = KeyStore.getInstance("bks");
        instance2.load(inputStream, str.toCharArray());
        instance.init(instance2);
        TrustManager[] trustManagers = instance.getTrustManagers();
        while (true) {
            int i2 = i;
            if (i2 < trustManagers.length) {
                if (trustManagers[i2] instanceof X509TrustManager) {
                    this.ji.add((X509TrustManager) trustManagers[i2]);
                }
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) {
        if (!this.ji.isEmpty()) {
            ((X509TrustManager) this.ji.get(0)).checkClientTrusted(x509CertificateArr, str);
        }
    }

    public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) {
        if (!this.ji.isEmpty()) {
            ((X509TrustManager) this.ji.get(0)).checkServerTrusted(x509CertificateArr, str);
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[this.ji.size()];
    }
}
