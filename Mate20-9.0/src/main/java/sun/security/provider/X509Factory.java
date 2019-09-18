package sun.security.provider;

import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import sun.security.util.Cache;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;

public class X509Factory {
    private static final int ENC_MAX_LENGTH = 4194304;
    private static final Cache<Object, X509CertImpl> certCache = Cache.newSoftMemoryCache(750);
    private static final Cache<Object, X509CRLImpl> crlCache = Cache.newSoftMemoryCache(750);

    public static synchronized X509CertImpl intern(X509Certificate c) throws CertificateException {
        byte[] encoding;
        X509CertImpl newC;
        synchronized (X509Factory.class) {
            if (c == null) {
                return null;
            }
            boolean isImpl = c instanceof X509CertImpl;
            if (isImpl) {
                encoding = ((X509CertImpl) c).getEncodedInternal();
            } else {
                encoding = c.getEncoded();
            }
            X509CertImpl newC2 = (X509CertImpl) getFromCache(certCache, encoding);
            if (newC2 != null) {
                return newC2;
            }
            if (isImpl) {
                newC = (X509CertImpl) c;
            } else {
                newC = new X509CertImpl(encoding);
                encoding = newC.getEncodedInternal();
            }
            addToCache(certCache, encoding, newC);
            return newC;
        }
    }

    public static synchronized X509CRLImpl intern(X509CRL c) throws CRLException {
        byte[] encoding;
        X509CRLImpl newC;
        synchronized (X509Factory.class) {
            if (c == null) {
                return null;
            }
            boolean isImpl = c instanceof X509CRLImpl;
            if (isImpl) {
                encoding = ((X509CRLImpl) c).getEncodedInternal();
            } else {
                encoding = c.getEncoded();
            }
            X509CRLImpl newC2 = (X509CRLImpl) getFromCache(crlCache, encoding);
            if (newC2 != null) {
                return newC2;
            }
            if (isImpl) {
                newC = (X509CRLImpl) c;
            } else {
                newC = new X509CRLImpl(encoding);
                encoding = newC.getEncodedInternal();
            }
            addToCache(crlCache, encoding, newC);
            return newC;
        }
    }

    private static synchronized <K, V> V getFromCache(Cache<K, V> cache, byte[] encoding) {
        V v;
        synchronized (X509Factory.class) {
            v = cache.get(new Cache.EqualByteArray(encoding));
        }
        return v;
    }

    private static synchronized <V> void addToCache(Cache<Object, V> cache, byte[] encoding, V value) {
        synchronized (X509Factory.class) {
            if (encoding.length <= 4194304) {
                cache.put(new Cache.EqualByteArray(encoding), value);
            }
        }
    }
}
