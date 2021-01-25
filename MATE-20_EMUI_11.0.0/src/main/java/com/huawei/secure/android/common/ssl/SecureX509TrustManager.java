package com.huawei.secure.android.common.ssl;

import android.content.Context;
import com.huawei.secure.android.common.util.IOUtil;
import com.huawei.secure.android.common.util.LogsUtil;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SecureX509TrustManager implements X509TrustManager {
    private static final String KEY_TYPE = "bks";
    private static final String TAG = "SecureX509TrustManager";
    private static final String TRUST_FILE = "hmsrootcas.bks";
    private static final String TRUST_MANAGER_TYPE = "X509";
    private static final String TRUST_PASSWORD = "";
    protected List<X509TrustManager> m509TrustManager = new ArrayList();

    public SecureX509TrustManager(Context context) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IllegalArgumentException {
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
                IOUtil.closeSecure(is);
            }
        } else {
            throw new IllegalArgumentException("context is null");
        }
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (!this.m509TrustManager.isEmpty()) {
            this.m509TrustManager.get(0).checkClientTrusted(chain, authType);
            return;
        }
        throw new CertificateException("checkClientTrusted CertificateException");
    }

    @Override // javax.net.ssl.X509TrustManager
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (!this.m509TrustManager.isEmpty()) {
            this.m509TrustManager.get(0).checkServerTrusted(chain, authType);
            return;
        }
        throw new CertificateException("checkServerTrusted CertificateException");
    }

    @Override // javax.net.ssl.X509TrustManager
    public X509Certificate[] getAcceptedIssuers() {
        try {
            ArrayList<X509Certificate> list = new ArrayList<>();
            for (X509TrustManager tm : this.m509TrustManager) {
                list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
            }
            return (X509Certificate[]) list.toArray(new X509Certificate[list.size()]);
        } catch (Exception e) {
            LogsUtil.e(TAG, "getAcceptedIssuers exception : " + e.getMessage());
            return new X509Certificate[0];
        }
    }
}
