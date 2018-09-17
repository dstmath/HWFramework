package com.android.okhttp.internal.http;

import com.android.okhttp.HttpUrl;
import com.android.okhttp.Protocol;
import com.android.okhttp.Request;
import java.net.Proxy.Type;

public final class RequestLine {
    private RequestLine() {
    }

    static String get(Request request, Type proxyType, Protocol protocol) {
        StringBuilder result = new StringBuilder();
        result.append(request.method());
        result.append(' ');
        if (includeAuthorityInRequestLine(request, proxyType)) {
            result.append(request.httpUrl());
        } else {
            result.append(requestPath(request.httpUrl()));
        }
        result.append(' ');
        result.append(version(protocol));
        return result.toString();
    }

    private static boolean includeAuthorityInRequestLine(Request request, Type proxyType) {
        return !request.isHttps() && proxyType == Type.HTTP;
    }

    public static String requestPath(HttpUrl url) {
        String path = url.encodedPath();
        if (path.isEmpty()) {
            path = "/";
        }
        String query = url.encodedQuery();
        return query != null ? path + '?' + query : path;
    }

    public static String version(Protocol protocol) {
        return protocol == Protocol.HTTP_1_0 ? "HTTP/1.0" : "HTTP/1.1";
    }
}
