package com.huawei.android.pushselfshow.utils.b;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.os.Build.VERSION;
import com.huawei.android.pushagent.a.a.c;
import java.io.IOException;
import java.net.SocketTimeoutException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class d {
    private static String b = "PushSelfShowLog";
    private Context a;

    public d(Context context) {
        this.a = context;
    }

    public String a() {
        String str = null;
        try {
            str = VERSION.SDK_INT < 11 ? Proxy.getHost(this.a) : System.getProperty("http.proxyHost");
            c.b(b, "proxyHost=" + str);
        } catch (Exception e) {
            c.d(b, "getProxyHost error:" + e.getMessage());
        }
        return str;
    }

    public HttpResponse a(String str, HttpClient httpClient, HttpGet httpGet) {
        HttpResponse httpResponse = null;
        try {
            HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 30000);
            HttpConnectionParams.setSoTimeout(params, 30000);
            HttpClientParams.setRedirecting(params, true);
            HttpProtocolParams.setUseExpectContinue(params, false);
            a((HttpRequest) httpGet, httpClient, str);
            return httpClient.execute(httpGet);
        } catch (SocketTimeoutException e) {
            c.d(b, "SocketTimeoutException occur" + e.getMessage());
            return httpResponse;
        } catch (ClientProtocolException e2) {
            c.d(b, "ClientProtocolException occur" + e2.getMessage());
            return httpResponse;
        } catch (IOException e3) {
            c.d(b, "IOException occur" + e3.getMessage());
            return httpResponse;
        } catch (Exception e4) {
            c.d(b, "Exception occur" + e4.getMessage());
            return httpResponse;
        }
    }

    public void a(HttpRequest httpRequest, HttpClient httpClient, String str) {
        httpRequest.setHeader("Accept-Encoding", "");
        String a = a();
        int b = b();
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.a.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.getType() == 0 && a != null && a.length() > 0 && b != -1) {
            HttpParams params = httpClient.getParams();
            ConnRouteParams.setDefaultProxy(params, new HttpHost(a(), b()));
            httpRequest.setParams(params);
        }
    }

    public int b() {
        int i = -1;
        try {
            if (VERSION.SDK_INT < 11) {
                i = Proxy.getPort(this.a);
            } else {
                String property = System.getProperty("http.proxyPort");
                if (property == null) {
                    property = "-1";
                }
                i = Integer.parseInt(property);
            }
            c.b(b, "proxyPort=" + i);
        } catch (Exception e) {
            c.d(b, "proxyPort error:" + e.getMessage());
        }
        return i;
    }
}
