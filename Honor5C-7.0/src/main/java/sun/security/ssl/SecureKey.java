package sun.security.ssl;

/* compiled from: SSLSessionImpl */
class SecureKey {
    private static Object nullObject;
    private Object appKey;
    private Object securityCtx;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SecureKey.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SecureKey.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SecureKey.<clinit>():void");
    }

    static Object getCurrentSecurityContext() {
        SecurityManager sm = System.getSecurityManager();
        Object context = null;
        if (sm != null) {
            context = sm.getSecurityContext();
        }
        if (context == null) {
            return nullObject;
        }
        return context;
    }

    SecureKey(Object key) {
        this.appKey = key;
        this.securityCtx = getCurrentSecurityContext();
    }

    Object getAppKey() {
        return this.appKey;
    }

    Object getSecurityContext() {
        return this.securityCtx;
    }

    public int hashCode() {
        return this.appKey.hashCode() ^ this.securityCtx.hashCode();
    }

    public boolean equals(Object o) {
        if ((o instanceof SecureKey) && ((SecureKey) o).appKey.equals(this.appKey)) {
            return ((SecureKey) o).securityCtx.equals(this.securityCtx);
        }
        return false;
    }
}
