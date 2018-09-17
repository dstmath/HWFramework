package com.android.okhttp;

public enum TlsVersion {
    TLS_1_2("TLSv1.2"),
    TLS_1_1("TLSv1.1"),
    TLS_1_0("TLSv1"),
    SSL_3_0("SSLv3");
    
    final String javaName;

    private TlsVersion(String javaName) {
        this.javaName = javaName;
    }

    public static TlsVersion forJavaName(String javaName) {
        if (javaName.equals("TLSv1.2")) {
            return TLS_1_2;
        }
        if (javaName.equals("TLSv1.1")) {
            return TLS_1_1;
        }
        if (javaName.equals("TLSv1")) {
            return TLS_1_0;
        }
        if (javaName.equals("SSLv3")) {
            return SSL_3_0;
        }
        throw new IllegalArgumentException("Unexpected TLS version: " + javaName);
    }

    public String javaName() {
        return this.javaName;
    }
}
