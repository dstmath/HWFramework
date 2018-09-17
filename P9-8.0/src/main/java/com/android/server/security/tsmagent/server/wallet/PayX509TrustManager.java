package com.android.server.security.tsmagent.server.wallet;

import android.content.Context;
import com.android.server.security.tsmagent.utils.HwLog;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
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
    private static final String TRUST_PWD = "";
    protected List<X509TrustManager> m509TrustManager = new ArrayList();

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0016 A:{Splitter: B:2:0x000d, ExcHandler: java.io.IOException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0016 A:{Splitter: B:2:0x000d, ExcHandler: java.io.IOException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0016 A:{Splitter: B:2:0x000d, ExcHandler: java.io.IOException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0016, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:7:?, code:
            com.android.server.security.tsmagent.utils.HwLog.e("PayX509TrustManager instance exception: " + r1.getMessage());
     */
    /* JADX WARNING: Missing block: B:8:0x0032, code:
            if (r3 != null) goto L_0x0034;
     */
    /* JADX WARNING: Missing block: B:10:?, code:
            r3.close();
     */
    /* JADX WARNING: Missing block: B:25:0x008d, code:
            com.android.server.security.tsmagent.utils.HwLog.e("can not close InputStream");
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:40:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public PayX509TrustManager(Context context) {
        InputStream inputStream = null;
        if (context == null) {
            try {
                throw new IOException("context cannot be null");
            } catch (Exception e) {
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                        HwLog.e("can not close InputStream");
                    }
                }
            }
        } else {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
            KeyStore trustKeyStore = KeyStore.getInstance(KEY_TYPE);
            inputStream = context.getAssets().open(TRUST_FILE);
            inputStream.reset();
            trustKeyStore.load(inputStream, "".toCharArray());
            inputStream.close();
            trustManagerFactory.init(trustKeyStore);
            TrustManager[] tms = trustManagerFactory.getTrustManagers();
            for (int i = 0; i < tms.length; i++) {
                if (tms[i] instanceof X509TrustManager) {
                    this.m509TrustManager.add((X509TrustManager) tms[i]);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    HwLog.e("can not close InputStream");
                }
            }
        }
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        HwLog.d("checkClientTrusted start,authType= " + authType);
        try {
            if (this.m509TrustManager.isEmpty()) {
                HwLog.e("Couldn't find a X509TrustManager");
            } else {
                ((X509TrustManager) this.m509TrustManager.get(0)).checkClientTrusted(chain, authType);
            }
        } catch (CertificateException e) {
            HwLog.e("CertificateException:" + e.getMessage());
        }
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        HwLog.d("checkServerTrusted  start, authType= " + authType);
        try {
            if (this.m509TrustManager.isEmpty()) {
                HwLog.e("checkServerTrusted- Couldn't find a X509TrustManager");
            } else {
                ((X509TrustManager) this.m509TrustManager.get(0)).checkServerTrusted(chain, authType);
            }
        } catch (CertificateException e) {
            HwLog.e("CertificateException:" + e.getMessage());
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        HwLog.v("getAcceptedIssuers start");
        ArrayList<X509Certificate> list = new ArrayList();
        for (X509TrustManager tm : this.m509TrustManager) {
            list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
        }
        return (X509Certificate[]) list.toArray(new X509Certificate[list.size()]);
    }
}
