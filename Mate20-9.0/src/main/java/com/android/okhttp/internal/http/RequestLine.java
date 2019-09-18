package com.android.okhttp.internal.http;

import com.android.okhttp.HttpUrl;
import com.android.okhttp.Request;
import java.net.Proxy;

public final class RequestLine {
    private RequestLine() {
    }

    static String get(Request request, Proxy.Type proxyType) {
        StringBuilder result = new StringBuilder();
        result.append(request.method());
        result.append(' ');
        if (includeAuthorityInRequestLine(request, proxyType)) {
            result.append(request.httpUrl());
        } else {
            result.append(requestPath(request.httpUrl()));
        }
        result.append(" HTTP/1.1");
        return result.toString();
    }

    private static boolean includeAuthorityInRequestLine(Request request, Proxy.Type proxyType) {
        return !request.isHttps() && proxyType == Proxy.Type.HTTP;
    }

    public static String requestPath(HttpUrl url) {
        String path = url.encodedPath();
        String query = url.encodedQuery();
        if (query == null) {
            return path;
        }
        return path + '?' + query;
    }
}
