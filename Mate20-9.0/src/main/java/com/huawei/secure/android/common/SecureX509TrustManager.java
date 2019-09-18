package com.huawei.secure.android.common;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

@Deprecated
public class SecureX509TrustManager implements X509TrustManager {
    private static final String KEY_TYPE = "bks";
    private static final String TAG = "SecureX509TrustManager";
    private static final String TRUST_FILE = "hmsrootcas.bks";
    private static final String TRUST_MANAGER_TYPE = "X509";
    private static final String TRUST_PASSWORD = "";
    protected ArrayList<X509TrustManager> m509TrustManager = new ArrayList<>();

    @Deprecated
    public SecureX509TrustManager(Context context) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IllegalAccessException {
        String str;
        if (context != null) {
            InputStream is = null;
            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
                KeyStore ks = KeyStore.getInstance(KEY_TYPE);
                is = context.getAssets().open(TRUST_FILE);
                is.reset();
                ks.load(is, "".toCharArray());
                tmf.init(ks);
                TrustManager[] tms = tmf.getTrustManagers();
                for (int i = 0; i < tms.length; i++) {
                    if (tms[i] instanceof X509TrustManager) {
                        this.m509TrustManager.add((X509TrustManager) tms[i]);
                    }
                }
                if (this.m509TrustManager.isEmpty()) {
                    throw new CertificateException("X509TrustManager is empty");
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        str = "close bks exception";
                        Log.e(TAG, str);
                    }
                }
            }
        } else {
            throw new IllegalAccessException("context is null");
        }
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (!this.m509TrustManager.isEmpty()) {
            this.m509TrustManager.get(0).checkClientTrusted(chain, authType);
            return;
        }
        throw new CertificateException("checkClientTrusted CertificateException");
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (!this.m509TrustManager.isEmpty()) {
            this.m509TrustManager.get(0).checkServerTrusted(chain, authType);
            return;
        }
        throw new CertificateException("checkServerTrusted CertificateException");
    }

    public X509Certificate[] getAcceptedIssuers() {
        ArrayList<X509Certificate> list = new ArrayList<>();
        Iterator<X509TrustManager> it = this.m509TrustManager.iterator();
        while (it.hasNext()) {
            list.addAll(Arrays.asList(it.next().getAcceptedIssuers()));
        }
        return (X509Certificate[]) list.toArray(new X509Certificate[list.size()]);
    }
}
