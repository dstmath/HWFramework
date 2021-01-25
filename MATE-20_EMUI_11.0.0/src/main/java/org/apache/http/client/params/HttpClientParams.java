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
        if (params != null) {
            params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, value);
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static boolean isAuthenticating(HttpParams params) {
        if (params != null) {
            return params.getBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, true);
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static void setAuthenticating(HttpParams params, boolean value) {
        if (params != null) {
            params.setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, value);
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static String getCookiePolicy(HttpParams params) {
        if (params != null) {
            String cookiePolicy = (String) params.getParameter(ClientPNames.COOKIE_POLICY);
            if (cookiePolicy == null) {
                return CookiePolicy.BEST_MATCH;
            }
            return cookiePolicy;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static void setCookiePolicy(HttpParams params, String cookiePolicy) {
        if (params != null) {
            params.setParameter(ClientPNames.COOKIE_POLICY, cookiePolicy);
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }
}
