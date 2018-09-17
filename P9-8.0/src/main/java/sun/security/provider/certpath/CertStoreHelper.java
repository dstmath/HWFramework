package sun.security.provider.certpath;

import java.io.IOException;
import java.net.URI;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Cache;

public abstract class CertStoreHelper {
    private static final int NUM_TYPES = 2;
    private static Cache<String, CertStoreHelper> cache = Cache.newSoftMemoryCache(2);
    private static final Map<String, String> classMap = new HashMap(2);

    public abstract CertStore getCertStore(URI uri) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    public abstract boolean isCausedByNetworkIssue(CertStoreException certStoreException);

    public abstract X509CRLSelector wrap(X509CRLSelector x509CRLSelector, Collection<X500Principal> collection, String str) throws IOException;

    public abstract X509CertSelector wrap(X509CertSelector x509CertSelector, X500Principal x500Principal, String str) throws IOException;

    static {
        classMap.put("LDAP", "sun.security.provider.certpath.ldap.LDAPCertStoreHelper");
        classMap.put("SSLServer", "sun.security.provider.certpath.ssl.SSLServerCertStoreHelper");
    }

    public static CertStoreHelper getInstance(final String type) throws NoSuchAlgorithmException {
        CertStoreHelper helper = (CertStoreHelper) cache.get(type);
        if (helper != null) {
            return helper;
        }
        final String cl = (String) classMap.get(type);
        if (cl == null) {
            throw new NoSuchAlgorithmException(type + " not available");
        }
        try {
            return (CertStoreHelper) AccessController.doPrivileged(new PrivilegedExceptionAction<CertStoreHelper>() {
                /* JADX WARNING: Removed duplicated region for block: B:3:0x0018 A:{Splitter: B:0:0x0000, ExcHandler: java.lang.InstantiationException (r2_0 'e' java.lang.Object)} */
                /* JADX WARNING: Missing block: B:3:0x0018, code:
            r2 = move-exception;
     */
                /* JADX WARNING: Missing block: B:5:0x001e, code:
            throw new java.lang.AssertionError(r2);
     */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public CertStoreHelper run() throws ClassNotFoundException {
                    try {
                        CertStoreHelper csh = (CertStoreHelper) Class.forName(cl, true, null).newInstance();
                        CertStoreHelper.cache.put(type, csh);
                        return csh;
                    } catch (Object e) {
                    }
                }
            });
        } catch (PrivilegedActionException e) {
            throw new NoSuchAlgorithmException(type + " not available", e.getException());
        }
    }

    static boolean isCausedByNetworkIssue(String type, CertStoreException cse) {
        boolean z = false;
        if (type.equals("LDAP") || type.equals("SSLServer")) {
            try {
                return getInstance(type).isCausedByNetworkIssue(cse);
            } catch (NoSuchAlgorithmException e) {
                return z;
            }
        } else if (!type.equals("URI")) {
            return z;
        } else {
            Throwable t = cse.getCause();
            if (t != null) {
                z = t instanceof IOException;
            }
            return z;
        }
    }
}
