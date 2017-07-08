package tmsdkobf;

import java.nio.ByteBuffer;

public final class ft {
    private static final byte[] mA = null;
    private static final byte[] mB = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ft.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ft.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ft.<clinit>():void");
    }

    public static boolean a(boolean z, boolean z2) {
        return z == z2;
    }

    public static boolean a(short s, short s2) {
        return s == s2;
    }

    public static boolean equals(int i, int i2) {
        return i == i2;
    }

    public static boolean a(long j, long j2) {
        return j == j2;
    }

    public static boolean equals(float f, float f2) {
        return f == f2;
    }

    public static boolean equals(Object obj, Object obj2) {
        return obj.equals(obj2);
    }

    public static <T extends Comparable<T>> int a(T t, T t2) {
        return t.compareTo(t2);
    }

    public static byte[] a(ByteBuffer byteBuffer) {
        Object obj = new byte[byteBuffer.position()];
        System.arraycopy(byteBuffer.array(), 0, obj, 0, obj.length);
        return obj;
    }
}
