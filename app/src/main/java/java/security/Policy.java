package java.security;

import java.util.Enumeration;

public abstract class Policy {
    public static final PermissionCollection UNSUPPORTED_EMPTY_COLLECTION = null;

    public interface Parameters {
    }

    private static class UnsupportedEmptyCollection extends PermissionCollection {
        public void add(Permission permission) {
        }

        public boolean implies(Permission permission) {
            return true;
        }

        public Enumeration<Permission> elements() {
            return null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.Policy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.Policy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.security.Policy.<clinit>():void");
    }

    public static Policy getPolicy() {
        return null;
    }

    public static void setPolicy(Policy p) {
    }

    public static Policy getInstance(String type, Parameters params) throws NoSuchAlgorithmException {
        return null;
    }

    public static Policy getInstance(String type, Parameters params, String provider) throws NoSuchProviderException, NoSuchAlgorithmException {
        return null;
    }

    public static Policy getInstance(String type, Parameters params, Provider provider) throws NoSuchAlgorithmException {
        return null;
    }

    public Provider getProvider() {
        return null;
    }

    public String getType() {
        return null;
    }

    public Parameters getParameters() {
        return null;
    }

    public PermissionCollection getPermissions(CodeSource codesource) {
        return null;
    }

    public PermissionCollection getPermissions(ProtectionDomain domain) {
        return null;
    }

    public boolean implies(ProtectionDomain domain, Permission permission) {
        return true;
    }

    public void refresh() {
    }
}
