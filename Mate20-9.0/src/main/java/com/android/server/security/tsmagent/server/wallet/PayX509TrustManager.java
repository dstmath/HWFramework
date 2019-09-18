package com.android.server.security.tsmagent.server.wallet;

import android.content.Context;
import com.android.server.security.tsmagent.utils.HwLog;
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

public class PayX509TrustManager implements X509TrustManager {
    private static final String KEY_TYPE = "bks";
    private static final String TRUST_FILE = "hicloudroot.bks";
    private static final String TRUST_MANAGER_TYPE = "X509";
    private static final String TRUST_P = "";
    protected List<X509TrustManager> m509TrustManager = new ArrayList();

    public PayX509TrustManager(Context context) {
        InputStream is = null;
        if (context != null) {
            try {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
                KeyStore trustKeyStore = KeyStore.getInstance(KEY_TYPE);
                InputStream is2 = context.getAssets().open(TRUST_FILE);
                is2.reset();
                trustKeyStore.load(is2, "".toCharArray());
                is2.close();
                trustManagerFactory.init(trustKeyStore);
                TrustManager[] tms = trustManagerFactory.getTrustManagers();
                for (int i = 0; i < tms.length; i++) {
                    if (tms[i] instanceof X509TrustManager) {
                        this.m509TrustManager.add((X509TrustManager) tms[i]);
                    }
                }
                if (is2 != null) {
                    try {
                        is2.close();
                    } catch (IOException e) {
                        HwLog.e("can not close InputStream");
                    }
                }
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e2) {
                HwLog.e("PayX509TrustManager instance exception: " + e2.getMessage());
                if (is != null) {
                    is.close();
                }
            } catch (Throwable th) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e3) {
                        HwLog.e("can not close InputStream");
                    }
                }
                throw th;
            }
        } else {
            throw new IOException("context cannot be null");
        }
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        HwLog.d("checkClientTrusted start,authType= " + authType);
        try {
            if (!this.m509TrustManager.isEmpty()) {
                this.m509TrustManager.get(0).checkClientTrusted(chain, authType);
            } else {
                HwLog.e("Couldn't find a X509TrustManager");
            }
        } catch (CertificateException e) {
            HwLog.e("CertificateException:" + e.getMessage());
        }
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        HwLog.d("checkServerTrusted  start, authType= " + authType);
        try {
            if (!this.m509TrustManager.isEmpty()) {
                this.m509TrustManager.get(0).checkServerTrusted(chain, authType);
            } else {
                HwLog.e("checkServerTrusted- Couldn't find a X509TrustManager");
            }
        } catch (CertificateException e) {
            HwLog.e("CertificateException:" + e.getMessage());
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        HwLog.v("getAcceptedIssuers start");
        ArrayList<X509Certificate> list = new ArrayList<>();
        for (X509TrustManager tm : this.m509TrustManager) {
            list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
        }
        return (X509Certificate[]) list.toArray(new X509Certificate[list.size()]);
    }
}
