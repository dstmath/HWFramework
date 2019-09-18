package com.huawei.okhttp3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum TlsVersion {
    TLS_1_3("TLSv1.3"),
    TLS_1_2("TLSv1.2"),
    TLS_1_1("TLSv1.1"),
    TLS_1_0("TLSv1"),
    SSL_3_0("SSLv3");
    
    final String javaName;

    private TlsVersion(String javaName2) {
        this.javaName = javaName2;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0059  */
    public static TlsVersion forJavaName(String javaName2) {
        char c;
        int hashCode = javaName2.hashCode();
        if (hashCode != 79923350) {
            switch (hashCode) {
                case -503070503:
                    if (javaName2.equals("TLSv1.1")) {
                        c = 2;
                        break;
                    }
                case -503070502:
                    if (javaName2.equals("TLSv1.2")) {
                        c = 1;
                        break;
                    }
                case -503070501:
                    if (javaName2.equals("TLSv1.3")) {
                        c = 0;
                        break;
                    }
            }
        } else if (javaName2.equals("TLSv1")) {
            c = 3;
            switch (c) {
                case 0:
                    return TLS_1_3;
                case 1:
                    return TLS_1_2;
                case 2:
                    return TLS_1_1;
                case 3:
                    return TLS_1_0;
                default:
                    throw new IllegalArgumentException("Unexpected TLS version: " + javaName2);
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    static List<TlsVersion> forJavaNames(String... tlsVersions) {
        List<TlsVersion> result = new ArrayList<>(tlsVersions.length);
        for (String tlsVersion : tlsVersions) {
            result.add(forJavaName(tlsVersion));
        }
        return Collections.unmodifiableList(result);
    }

    public String javaName() {
        return this.javaName;
    }
}
