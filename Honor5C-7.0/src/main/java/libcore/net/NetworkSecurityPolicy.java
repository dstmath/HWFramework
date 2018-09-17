package libcore.net;

public abstract class NetworkSecurityPolicy {
    private static volatile NetworkSecurityPolicy instance;

    public static final class DefaultNetworkSecurityPolicy extends NetworkSecurityPolicy {
        public boolean isCleartextTrafficPermitted() {
            return true;
        }

        public boolean isCleartextTrafficPermitted(String hostname) {
            return isCleartextTrafficPermitted();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.net.NetworkSecurityPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.net.NetworkSecurityPolicy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.net.NetworkSecurityPolicy.<clinit>():void");
    }

    public abstract boolean isCleartextTrafficPermitted();

    public abstract boolean isCleartextTrafficPermitted(String str);

    public static NetworkSecurityPolicy getInstance() {
        return instance;
    }

    public static void setInstance(NetworkSecurityPolicy policy) {
        if (policy == null) {
            throw new NullPointerException("policy == null");
        }
        instance = policy;
    }
}
