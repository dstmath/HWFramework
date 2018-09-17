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
import java.util.Map;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Cache;

public abstract class CertStoreHelper {
    private static final int NUM_TYPES = 2;
    private static Cache<String, CertStoreHelper> cache;
    private static final Map<String, String> classMap = null;

    /* renamed from: sun.security.provider.certpath.CertStoreHelper.1 */
    static class AnonymousClass1 implements PrivilegedExceptionAction<CertStoreHelper> {
        final /* synthetic */ String val$cl;
        final /* synthetic */ String val$type;

        AnonymousClass1(String val$cl, String val$type) {
            this.val$cl = val$cl;
            this.val$type = val$type;
        }

        public CertStoreHelper run() throws ClassNotFoundException {
            try {
                CertStoreHelper csh = (CertStoreHelper) Class.forName(this.val$cl, true, null).newInstance();
                CertStoreHelper.cache.put(this.val$type, csh);
                return csh;
            } catch (Object e) {
                throw new AssertionError(e);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.CertStoreHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.CertStoreHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.CertStoreHelper.<clinit>():void");
    }

    public abstract CertStore getCertStore(URI uri) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    public abstract boolean isCausedByNetworkIssue(CertStoreException certStoreException);

    public abstract X509CRLSelector wrap(X509CRLSelector x509CRLSelector, Collection<X500Principal> collection, String str) throws IOException;

    public abstract X509CertSelector wrap(X509CertSelector x509CertSelector, X500Principal x500Principal, String str) throws IOException;

    public static CertStoreHelper getInstance(String type) throws NoSuchAlgorithmException {
        CertStoreHelper helper = (CertStoreHelper) cache.get(type);
        if (helper != null) {
            return helper;
        }
        String cl = (String) classMap.get(type);
        if (cl == null) {
            throw new NoSuchAlgorithmException(type + " not available");
        }
        try {
            return (CertStoreHelper) AccessController.doPrivileged(new AnonymousClass1(cl, type));
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
