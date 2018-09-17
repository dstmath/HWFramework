package com.sun.net.ssl.internal.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public abstract class X509ExtendedTrustManager implements X509TrustManager {
    public abstract void checkClientTrusted(X509Certificate[] x509CertificateArr, String str, String str2, String str3) throws CertificateException;

    public abstract void checkServerTrusted(X509Certificate[] x509CertificateArr, String str, String str2, String str3) throws CertificateException;

    protected X509ExtendedTrustManager() {
    }
}
