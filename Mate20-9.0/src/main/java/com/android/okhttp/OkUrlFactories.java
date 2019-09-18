package com.android.okhttp;

import com.android.okhttp.internal.URLFilter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class OkUrlFactories {
    private OkUrlFactories() {
    }

    public static HttpURLConnection open(OkUrlFactory okUrlFactory, URL url, Proxy proxy) {
        return okUrlFactory.open(url, proxy);
    }

    public static void setUrlFilter(OkUrlFactory okUrlFactory, URLFilter urlFilter) {
        okUrlFactory.setUrlFilter(urlFilter);
    }
}
