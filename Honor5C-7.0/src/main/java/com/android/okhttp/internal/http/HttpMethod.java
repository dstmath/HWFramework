package com.android.okhttp.internal.http;

public final class HttpMethod {
    public static boolean invalidatesCache(String method) {
        if (method.equals("POST") || method.equals("PATCH") || method.equals("PUT")) {
            return true;
        }
        return method.equals("DELETE");
    }

    public static boolean requiresRequestBody(String method) {
        if (method.equals("POST") || method.equals("PUT")) {
            return true;
        }
        return method.equals("PATCH");
    }

    public static boolean permitsRequestBody(String method) {
        if (requiresRequestBody(method)) {
            return true;
        }
        return method.equals("DELETE");
    }

    private HttpMethod() {
    }
}
