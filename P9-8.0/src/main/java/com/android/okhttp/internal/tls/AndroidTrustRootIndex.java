package com.android.okhttp.internal.tls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public final class AndroidTrustRootIndex implements TrustRootIndex {
    private final Method findByIssuerAndSignatureMethod;
    private final X509TrustManager trustManager;

    public AndroidTrustRootIndex(X509TrustManager trustManager, Method findByIssuerAndSignatureMethod) {
        this.findByIssuerAndSignatureMethod = findByIssuerAndSignatureMethod;
        this.trustManager = trustManager;
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

    public static TrustRootIndex get(X509TrustManager trustManager) {
        try {
            Method method = trustManager.getClass().getDeclaredMethod("findTrustAnchorByIssuerAndSignature", new Class[]{X509Certificate.class});
            method.setAccessible(true);
            return new AndroidTrustRootIndex(trustManager, method);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
