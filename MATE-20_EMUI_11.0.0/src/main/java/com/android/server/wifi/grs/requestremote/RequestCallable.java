package com.android.server.wifi.grs.requestremote;

import android.util.Log;
import com.android.server.wifi.grs.utils.ContextUtil;
import com.android.server.wifi.grs.utils.IoUtils;
import com.huawei.secure.android.common.ssl.SecureSSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.Callable;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class RequestCallable implements Callable<GrsResponse> {
    private static final int HTTP_SUCCESS = 200;
    private static final String TAG = RequestCallable.class.getSimpleName();
    private static final int URLCONNECTION_TIMEOUT = 10000;
    private CallBack mCallBack;
    private GrsResponse mGrsResponse;
    private String mHttpUrl;
    private int mIndex;

    public RequestCallable(String httpUrl, int index, CallBack callBack) {
        this.mHttpUrl = httpUrl;
        this.mCallBack = callBack;
        this.mIndex = index;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0081, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0082, code lost:
        android.util.Log.d(com.android.server.wifi.grs.requestremote.RequestCallable.TAG, "RequestTask run task catch Exception");
        r6.mGrsResponse = new com.android.server.wifi.grs.requestremote.GrsResponse(r0);
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0081 A[ExcHandler: IllegalAccessException | IllegalArgumentException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException (r0v7 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:16:0x006c] */
    @Override // java.util.concurrent.Callable
    public GrsResponse call() {
        Log.d(TAG, "start https request");
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(this.mHttpUrl).openConnection();
            if (urlConnection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) urlConnection;
                SecureSSLSocketFactory socketFactory = SecureSSLSocketFactory.getInstance(ContextUtil.getContext());
                if (socketFactory != null && (socketFactory instanceof SSLSocketFactory)) {
                    httpsUrlConnection.setSSLSocketFactory(socketFactory);
                    httpsUrlConnection.setHostnameVerifier(SecureSSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
                    urlConnection = httpsUrlConnection;
                }
            }
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
            String str = TAG;
            Log.d(str, "response code = " + code);
            byte[] responseBody = null;
            if (code == 200) {
                InputStream inputStream = null;
                try {
                    inputStream = urlConnection.getInputStream();
                    responseBody = IoUtils.toByteArray(inputStream);
                    IoUtils.closeSecure(inputStream);
                } catch (IllegalAccessException | IllegalArgumentException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                } catch (Throwable th) {
                    IoUtils.closeSecure(inputStream);
                    throw th;
                }
            }
            this.mGrsResponse = new GrsResponse(code, urlConnection.getHeaderFields(), responseBody);
        } catch (IOException e2) {
            Log.d(TAG, "RequestTask run task catch IOException");
            this.mGrsResponse = new GrsResponse(e2);
        }
        this.mGrsResponse.setUrl(this.mHttpUrl);
        this.mGrsResponse.setIndex(this.mIndex);
        CallBack callBack = this.mCallBack;
        if (callBack != null) {
            callBack.onResponse(this.mGrsResponse);
        }
        return this.mGrsResponse;
    }
}
