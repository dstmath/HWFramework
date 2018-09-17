package org.apache.http.auth.params;

import org.apache.http.params.HttpParams;

@Deprecated
public final class AuthParams {
    private AuthParams() {
    }

    public static String getCredentialCharset(HttpParams params) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        String charset = (String) params.getParameter(AuthPNames.CREDENTIAL_CHARSET);
        if (charset == null) {
            return "US-ASCII";
        }
        return charset;
    }

    public static void setCredentialCharset(HttpParams params, String charset) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        params.setParameter(AuthPNames.CREDENTIAL_CHARSET, charset);
    }
}
