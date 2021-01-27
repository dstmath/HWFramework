package com.huawei.secure.android.common.ssl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.SslErrorHandler;
import com.android.server.wifi.ABS.HwAbsUtils;
import com.android.server.wifi.hwcoex.HiCoexUtils;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class WebViewSSLCheckThread extends Thread {
    private static final String TAG = "WebViewSSLCheckThread";
    private HostnameVerifier hostnameVerifier;
    private SslErrorHandler sslErrorHandler;
    private SSLSocketFactory sslSocketFactory;
    private String url;

    public WebViewSSLCheckThread() {
    }

    public WebViewSSLCheckThread(SslErrorHandler sslErrorHandler2, String url2, Context context) throws CertificateException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException, IllegalAccessException {
        setSslErrorHandler(sslErrorHandler2);
        setUrl(url2);
        setSslSocketFactory(SecureSSLSocketFactory.getInstance(context));
        setHostnameVerifier(SecureSSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
    }

    public WebViewSSLCheckThread(SslErrorHandler sslErrorHandler2, String url2, SSLSocketFactory sslSocketFactory2, HostnameVerifier hostnameVerifier2) {
        this.sslErrorHandler = sslErrorHandler2;
        this.url = url2;
        this.sslSocketFactory = sslSocketFactory2;
        this.hostnameVerifier = hostnameVerifier2;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        super.run();
        if (this.sslErrorHandler == null) {
            Log.e(TAG, "sslErrorHandler is null");
        } else if (TextUtils.isEmpty(this.url)) {
            Log.e(TAG, "url is null");
            this.sslErrorHandler.cancel();
        } else if (this.sslSocketFactory == null) {
            Log.e(TAG, "sslSocketFactory is null");
            this.sslErrorHandler.cancel();
        } else if (this.hostnameVerifier == null) {
            Log.e(TAG, "hostnameVerifier is null");
            this.sslErrorHandler.cancel();
        } else {
            HttpsURLConnection httpsCoon = null;
            try {
                URLConnection urlConnection = new URL(this.url).openConnection();
                if (urlConnection instanceof HttpsURLConnection) {
                    httpsCoon = (HttpsURLConnection) urlConnection;
                    httpsCoon.setSSLSocketFactory(this.sslSocketFactory);
                    httpsCoon.setHostnameVerifier(this.hostnameVerifier);
                    httpsCoon.setRequestMethod("GET");
                    httpsCoon.setConnectTimeout(HiCoexUtils.TIMEOUT_CONNECT);
                    httpsCoon.setReadTimeout(HwAbsUtils.AUTO_HANDOVER_TIMER);
                    httpsCoon.connect();
                }
                if (httpsCoon != null) {
                    httpsCoon.disconnect();
                }
                this.sslErrorHandler.proceed();
            } catch (IOException e) {
                Log.e(TAG, "IO exception : " + e.getMessage());
                this.sslErrorHandler.cancel();
                if (0 != 0) {
                    httpsCoon.disconnect();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    httpsCoon.disconnect();
                }
                throw th;
            }
        }
    }

    public HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier2) {
        this.hostnameVerifier = hostnameVerifier2;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return this.sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory2) {
        this.sslSocketFactory = sslSocketFactory2;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public SslErrorHandler getSslErrorHandler() {
        return this.sslErrorHandler;
    }

    public void setSslErrorHandler(SslErrorHandler sslErrorHandler2) {
        this.sslErrorHandler = sslErrorHandler2;
    }
}
