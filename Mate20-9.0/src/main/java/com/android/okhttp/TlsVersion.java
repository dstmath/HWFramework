package com.android.okhttp;

public enum TlsVersion {
    TLS_1_2("TLSv1.2"),
    TLS_1_1("TLSv1.1"),
    TLS_1_0("TLSv1"),
    SSL_3_0("SSLv3");
    
    final String javaName;

    private TlsVersion(String javaName2) {
        this.javaName = javaName2;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public static TlsVersion forJavaName(String javaName2) {
        char c;
        switch (javaName2.hashCode()) {
            case -503070503:
                if (javaName2.equals("TLSv1.1")) {
                    c = 1;
                    break;
                }
            case -503070502:
                if (javaName2.equals("TLSv1.2")) {
                    c = 0;
                    break;
                }
            case 79201641:
                if (javaName2.equals("SSLv3")) {
                    c = 3;
                    break;
                }
            case 79923350:
                if (javaName2.equals("TLSv1")) {
                    c = 2;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return TLS_1_2;
            case 1:
                return TLS_1_1;
            case 2:
                return TLS_1_0;
            case 3:
                return SSL_3_0;
            default:
                throw new IllegalArgumentException("Unexpected TLS version: " + javaName2);
        }
    }

    public String javaName() {
        return this.javaName;
    }
}
