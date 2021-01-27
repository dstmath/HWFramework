package com.huawei.networkit.grs.requestremote;

import android.os.SystemClock;
import com.huawei.networkit.grs.common.IoUtils;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.utils.AgentUtil;
import com.huawei.networkit.grs.utils.ContextUtil;
import com.huawei.networkit.grs.utils.ssl.SecureSSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.Callable;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class RequestCallable implements Callable<GrsResponse> {
    private static final String TAG = RequestCallable.class.getSimpleName();
    private static final String TRUST_FILE = "grs_sp.bks";
    private CallBack callBack;
    private GrsResponse grsResponse;
    private String httpUrl;
    private int index;

    public RequestCallable(String httpUrl2, int index2, CallBack callBack2) {
        this.httpUrl = httpUrl2;
        this.callBack = callBack2;
        this.index = index2;
    }

    @Override // java.util.concurrent.Callable
    public GrsResponse call() throws Exception {
        byte[] responseBody;
        Logger.i(TAG, "call execute");
        try {
            long startTime = SystemClock.elapsedRealtime();
            URLConnection urlConnection = new URL(this.httpUrl).openConnection();
            if (urlConnection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
                try {
                    httpsURLConnection.setSSLSocketFactory(getSecuritySDKSocketFactory());
                    httpsURLConnection.setHostnameVerifier(SecureSSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
                } catch (IllegalArgumentException e) {
                    Logger.w(TAG, "init https ssl socket failed.");
                }
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setConnectTimeout(10000);
                httpsURLConnection.setReadTimeout(10000);
                httpsURLConnection.setRequestProperty("User-Agent", AgentUtil.getUserAgent(ContextUtil.getContext(), "NetworkKit-grs"));
                httpsURLConnection.connect();
                int code = httpsURLConnection.getResponseCode();
                if (code == 200) {
                    InputStream inputStream = null;
                    try {
                        inputStream = urlConnection.getInputStream();
                        responseBody = IoUtils.toByteArray(inputStream);
                    } finally {
                        IoUtils.closeSecure(inputStream);
                    }
                } else {
                    responseBody = null;
                }
                this.grsResponse = new GrsResponse(code, urlConnection.getHeaderFields(), responseBody, SystemClock.elapsedRealtime() - startTime);
                this.grsResponse.setUrl(this.httpUrl);
                this.grsResponse.setIndex(this.index);
                CallBack callBack2 = this.callBack;
                if (callBack2 != null) {
                    callBack2.onResponse(this.grsResponse);
                }
                return this.grsResponse;
            }
            Logger.w(TAG, "urlConnection is not an instance of HttpsURLConnection");
            return null;
        } catch (IOException e2) {
            long endTime = SystemClock.elapsedRealtime();
            Logger.w(TAG, "RequestCallable run task catch IOException", e2);
            this.grsResponse = new GrsResponse(e2, endTime - 0);
        }
    }

    private static SSLSocketFactory getSecuritySDKSocketFactory() {
        try {
            return SecureSSLSocketFactory.getInstance(ContextUtil.getContext(), TRUST_FILE);
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (CertificateException e2) {
            throw new AssertionError(e2);
        } catch (NoSuchAlgorithmException e3) {
            throw new AssertionError(e3);
        } catch (IllegalAccessException e4) {
            throw new AssertionError(e4);
        } catch (KeyStoreException e5) {
            throw new AssertionError(e5);
        } catch (KeyManagementException e6) {
            throw new AssertionError(e6);
        }
    }
}
