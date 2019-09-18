package com.android.okhttp.internal.tls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public final class AndroidTrustRootIndex implements TrustRootIndex {
    private final Method findByIssuerAndSignatureMethod;
    private final X509TrustManager trustManager;

    public AndroidTrustRootIndex(X509TrustManager trustManager2, Method findByIssuerAndSignatureMethod2) {
        this.findByIssuerAndSignatureMethod = findByIssuerAndSignatureMethod2;
        this.trustManager = trustManager2;
    }

    public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
        X509Certificate x509Certificate = null;
        try {
            TrustAnchor trustAnchor = (TrustAnchor) this.findByIssuerAndSignatureMethod.invoke(this.trustManager, new Object[]{cert});
            if (trustAnchor != null) {
                x509Certificate = trustAnchor.getTrustedCert();
            }
            return x509Certificate;
        } catch (IllegalAccessException e) {
            throw new AssertionError();
        } catch (InvocationTargetException e2) {
            return null;
        }
    }

    public static TrustRootIndex get(X509TrustManager trustManager2) {
        try {
            Method method = trustManager2.getClass().getDeclaredMethod("findTrustAnchorByIssuerAndSignature", new Class[]{X509Certificate.class});
            method.setAccessible(true);
            return new AndroidTrustRootIndex(trustManager2, method);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
