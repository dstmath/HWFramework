package sun.security.ssl;

import java.security.AccessControlContext;
import java.security.Permission;
import java.security.Principal;
import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

public final class Krb5Helper {
    private static final String IMPL_CLASS = "sun.security.ssl.krb5.Krb5ProxyImpl";
    private static final Krb5Proxy proxy = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.Krb5Helper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.Krb5Helper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.Krb5Helper.<clinit>():void");
    }

    private Krb5Helper() {
    }

    public static boolean isAvailable() {
        return proxy != null;
    }

    private static void ensureAvailable() {
        if (proxy == null) {
            throw new AssertionError((Object) "Kerberos should have been available");
        }
    }

    public static Subject getClientSubject(AccessControlContext acc) throws LoginException {
        ensureAvailable();
        return proxy.getClientSubject(acc);
    }

    public static Subject getServerSubject(AccessControlContext acc) throws LoginException {
        ensureAvailable();
        return proxy.getServerSubject(acc);
    }

    public static SecretKey[] getServerKeys(AccessControlContext acc) throws LoginException {
        ensureAvailable();
        return proxy.getServerKeys(acc);
    }

    public static String getServerPrincipalName(SecretKey kerberosKey) {
        ensureAvailable();
        return proxy.getServerPrincipalName(kerberosKey);
    }

    public static String getPrincipalHostName(Principal principal) {
        ensureAvailable();
        return proxy.getPrincipalHostName(principal);
    }

    public static Permission getServicePermission(String principalName, String action) {
        ensureAvailable();
        return proxy.getServicePermission(principalName, action);
    }
}
