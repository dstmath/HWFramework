package java.net;

public final class StandardSocketOptions {
    public static final SocketOption<NetworkInterface> IP_MULTICAST_IF = null;
    public static final SocketOption<Boolean> IP_MULTICAST_LOOP = null;
    public static final SocketOption<Integer> IP_MULTICAST_TTL = null;
    public static final SocketOption<Integer> IP_TOS = null;
    public static final SocketOption<Boolean> SO_BROADCAST = null;
    public static final SocketOption<Boolean> SO_KEEPALIVE = null;
    public static final SocketOption<Integer> SO_LINGER = null;
    public static final SocketOption<Integer> SO_RCVBUF = null;
    public static final SocketOption<Boolean> SO_REUSEADDR = null;
    public static final SocketOption<Integer> SO_SNDBUF = null;
    public static final SocketOption<Boolean> TCP_NODELAY = null;

    private static class StdSocketOption<T> implements SocketOption<T> {
        private final String name;
        private final Class<T> type;

        StdSocketOption(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        public String name() {
            return this.name;
        }

        public Class<T> type() {
            return this.type;
        }

        public String toString() {
            return this.name;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.StandardSocketOptions.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.StandardSocketOptions.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.net.StandardSocketOptions.<clinit>():void");
    }

    private StandardSocketOptions() {
    }
}
