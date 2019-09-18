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
    /* access modifiers changed from: private */
    public static Cache<String, CertStoreHelper> cache = Cache.newSoftMemoryCache(2);
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
        CertStoreHelper helper = cache.get(type);
        if (helper != null) {
            return helper;
        }
        final String cl = classMap.get(type);
        if (cl != null) {
            try {
                return (CertStoreHelper) AccessController.doPrivileged(new PrivilegedExceptionAction<CertStoreHelper>() {
                    public CertStoreHelper run() throws ClassNotFoundException {
                        try {
                            CertStoreHelper csh = (CertStoreHelper) Class.forName(String.this, true, null).newInstance();
                            CertStoreHelper.cache.put(type, csh);
                            return csh;
                        } catch (IllegalAccessException | InstantiationException e) {
                            throw new AssertionError((Object) e);
                        }
                    }
                });
            } catch (PrivilegedActionException e) {
                throw new NoSuchAlgorithmException(type + " not available", e.getException());
            }
        } else {
            throw new NoSuchAlgorithmException(type + " not available");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0046 A[SYNTHETIC, Splitter:B:24:0x0046] */
    static boolean isCausedByNetworkIssue(String type, CertStoreException cse) {
        char c;
        int hashCode = type.hashCode();
        boolean z = true;
        if (hashCode == 84300) {
            if (type.equals("URI")) {
                c = 2;
                switch (c) {
                    case 0:
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 2331559) {
            if (type.equals("LDAP")) {
                c = 0;
                switch (c) {
                    case 0:
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 133315663 && type.equals("SSLServer")) {
            c = 1;
            switch (c) {
                case 0:
                case 1:
                    try {
                        return getInstance(type).isCausedByNetworkIssue(cse);
                    } catch (NoSuchAlgorithmException e) {
                        return false;
                    }
                case 2:
                    Throwable t = cse.getCause();
                    if (t == null || !(t instanceof IOException)) {
                        z = false;
                    }
                    return z;
                default:
                    return false;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
            case 1:
                break;
            case 2:
                break;
        }
    }
}
