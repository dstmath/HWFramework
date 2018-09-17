package org.apache.http.client.params;

import org.apache.http.params.HttpParams;

@Deprecated
public class HttpClientParams {
    private HttpClientParams() {
    }

    public static boolean isRedirecting(HttpParams params) {
        if (params != null) {
            return params.getBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static void setRedirecting(HttpParams params, boolean value) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, value);
    }

    public static boolean isAuthenticating(HttpParams params) {
        if (params != null) {
            return params.getBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, true);
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static void setAuthenticating(HttpParams params, boolean value) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        params.setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, value);
    }

    public static String getCookiePolicy(HttpParams params) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        String cookiePolicy = (String) params.getParameter(ClientPNames.COOKIE_POLICY);
        if (cookiePolicy == null) {
            return CookiePolicy.BEST_MATCH;
        }
        return cookiePolicy;
    }

    public static void setCookiePolicy(HttpParams params, String cookiePolicy) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        params.setParameter(ClientPNames.COOKIE_POLICY, cookiePolicy);
    }
}
