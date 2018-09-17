package com.android.okhttp.internal.http;

public final class HttpMethod {
    public static boolean invalidatesCache(String method) {
        if (method.equals("POST") || method.equals("PATCH") || method.equals("PUT") || method.equals("DELETE")) {
            return true;
        }
        return method.equals("MOVE");
    }

    public static boolean requiresRequestBody(String method) {
        if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("PROPPATCH")) {
            return true;
        }
        return method.equals("REPORT");
    }

    public static boolean permitsRequestBody(String method) {
        return (requiresRequestBody(method) || method.equals("OPTIONS") || method.equals("DELETE") || method.equals("PROPFIND") || method.equals("MKCOL")) ? true : method.equals("LOCK");
    }

    public static boolean redirectsToGet(String method) {
        return method.equals("PROPFIND") ^ 1;
    }

    private HttpMethod() {
    }
}
