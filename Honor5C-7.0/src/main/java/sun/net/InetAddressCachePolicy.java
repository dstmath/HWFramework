package sun.net;

public final class InetAddressCachePolicy {
    public static final int DEFAULT_POSITIVE = 2;
    public static final int FOREVER = -1;
    public static final int NEVER = 0;
    private static int cachePolicy = 0;
    private static final String cachePolicyProp = "networkaddress.cache.ttl";
    private static final String cachePolicyPropFallback = "sun.net.inetaddr.ttl";
    private static int negativeCachePolicy = 0;
    private static final String negativeCachePolicyProp = "networkaddress.cache.negative.ttl";
    private static final String negativeCachePolicyPropFallback = "sun.net.inetaddr.negative.ttl";
    private static boolean propertyNegativeSet;
    private static boolean propertySet;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.InetAddressCachePolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.InetAddressCachePolicy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.InetAddressCachePolicy.<clinit>():void");
    }

    public static synchronized int get() {
        int i;
        synchronized (InetAddressCachePolicy.class) {
            i = cachePolicy;
        }
        return i;
    }

    public static synchronized int getNegative() {
        int i;
        synchronized (InetAddressCachePolicy.class) {
            i = negativeCachePolicy;
        }
        return i;
    }

    public static synchronized void setIfNotSet(int newPolicy) {
        synchronized (InetAddressCachePolicy.class) {
            if (!propertySet) {
                checkValue(newPolicy, cachePolicy);
                cachePolicy = newPolicy;
            }
        }
    }

    public static synchronized void setNegativeIfNotSet(int newPolicy) {
        synchronized (InetAddressCachePolicy.class) {
            if (!propertyNegativeSet) {
                negativeCachePolicy = newPolicy;
            }
        }
    }

    private static void checkValue(int newPolicy, int oldPolicy) {
        if (newPolicy != FOREVER) {
            if (oldPolicy == FOREVER || newPolicy < oldPolicy || newPolicy < FOREVER) {
                throw new SecurityException("can't make InetAddress cache more lax");
            }
        }
    }
}
