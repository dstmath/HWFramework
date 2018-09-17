package java.lang;

import java.util.regex.Pattern;
import sun.misc.FloatConsts;

public final class Byte extends Number implements Comparable<Byte> {
    public static final int BYTES = 1;
    private static final char[] DIGITS = null;
    public static final byte MAX_VALUE = Byte.MAX_VALUE;
    public static final byte MIN_VALUE = Byte.MIN_VALUE;
    public static final int SIZE = 8;
    public static final Class<Byte> TYPE = null;
    private static final char[] UPPER_CASE_DIGITS = null;
    private static final long serialVersionUID = -7183698231559129828L;
    private final byte value;

    private static class ByteCache {
        static final Byte[] cache = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Byte.ByteCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Byte.ByteCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Byte.ByteCache.<clinit>():void");
        }

        private ByteCache() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Byte.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Byte.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.Byte.<clinit>():void");
    }

    public static String toString(byte b) {
        return Integer.toString(b, 10);
    }

    public static Byte valueOf(byte b) {
        return ByteCache.cache[b + Pattern.CANON_EQ];
    }

    public static byte parseByte(String s, int radix) throws NumberFormatException {
        int i = Integer.parseInt(s, radix);
        if (i >= -128 && i <= FloatConsts.MAX_EXPONENT) {
            return (byte) i;
        }
        throw new NumberFormatException("Value out of range. Value:\"" + s + "\" Radix:" + radix);
    }

    public static byte parseByte(String s) throws NumberFormatException {
        return parseByte(s, 10);
    }

    public static Byte valueOf(String s, int radix) throws NumberFormatException {
        return valueOf(parseByte(s, radix));
    }

    public static Byte valueOf(String s) throws NumberFormatException {
        return valueOf(s, 10);
    }

    public static Byte decode(String nm) throws NumberFormatException {
        int i = Integer.decode(nm).intValue();
        if (i >= -128 && i <= FloatConsts.MAX_EXPONENT) {
            return valueOf((byte) i);
        }
        throw new NumberFormatException("Value " + i + " out of range from input " + nm);
    }

    public Byte(byte value) {
        this.value = value;
    }

    public Byte(String s) throws NumberFormatException {
        this.value = parseByte(s, 10);
    }

    public byte byteValue() {
        return this.value;
    }

    public short shortValue() {
        return (short) this.value;
    }

    public int intValue() {
        return this.value;
    }

    public long longValue() {
        return (long) this.value;
    }

    public float floatValue() {
        return (float) this.value;
    }

    public double doubleValue() {
        return (double) this.value;
    }

    public String toString() {
        return Integer.toString(this.value);
    }

    public int hashCode() {
        return hashCode(this.value);
    }

    public static int hashCode(byte value) {
        return value;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Byte)) {
            return false;
        }
        if (this.value == ((Byte) obj).byteValue()) {
            z = true;
        }
        return z;
    }

    public /* bridge */ /* synthetic */ int compareTo(Object anotherByte) {
        return compareTo((Byte) anotherByte);
    }

    public int compareTo(Byte anotherByte) {
        return compare(this.value, anotherByte.value);
    }

    public static int compare(byte x, byte y) {
        return x - y;
    }

    public static String toHexString(byte b, boolean upperCase) {
        char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
        return new String(0, 2, new char[]{digits[(b >> 4) & 15], digits[b & 15]});
    }
}
