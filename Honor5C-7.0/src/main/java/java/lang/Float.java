package java.lang;

import sun.misc.FloatConsts;
import sun.misc.FpUtils;

public final class Float extends Number implements Comparable<Float> {
    public static final int BYTES = 4;
    public static final int MAX_EXPONENT = 127;
    public static final float MAX_VALUE = Float.MAX_VALUE;
    public static final int MIN_EXPONENT = -126;
    public static final float MIN_NORMAL = Float.MIN_NORMAL;
    public static final float MIN_VALUE = Float.MIN_VALUE;
    public static final float NEGATIVE_INFINITY = Float.NEGATIVE_INFINITY;
    public static final float NaN = Float.NaN;
    public static final float POSITIVE_INFINITY = Float.POSITIVE_INFINITY;
    public static final int SIZE = 32;
    public static final Class<Float> TYPE = null;
    private static final long serialVersionUID = -2671257302660747028L;
    private final float value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Float.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Float.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.Float.<clinit>():void");
    }

    public static native int floatToRawIntBits(float f);

    public static native float intBitsToFloat(int i);

    public static String toString(float f) {
        return FloatingDecimal.getThreadLocalInstance().loadFloat(f).toJavaFormatString();
    }

    public static String toHexString(float f) {
        if (Math.abs(f) >= MIN_NORMAL || f == 0.0f) {
            return Double.toHexString((double) f);
        }
        return Double.toHexString(FpUtils.scalb((double) f, -896)).replaceFirst("p-1022$", "p-126");
    }

    public static Float valueOf(String s) throws NumberFormatException {
        return new Float(FloatingDecimal.getThreadLocalInstance().readJavaFormatString(s).floatValue());
    }

    public static Float valueOf(float f) {
        return new Float(f);
    }

    public static float parseFloat(String s) throws NumberFormatException {
        return FloatingDecimal.getThreadLocalInstance().readJavaFormatString(s).floatValue();
    }

    public static boolean isNaN(float v) {
        return v != v;
    }

    public static boolean isInfinite(float v) {
        return v == POSITIVE_INFINITY || v == NEGATIVE_INFINITY;
    }

    public static boolean isFinite(float f) {
        return Math.abs(f) <= MAX_VALUE;
    }

    public Float(float value) {
        this.value = value;
    }

    public Float(double value) {
        this.value = (float) value;
    }

    public Float(String s) throws NumberFormatException {
        this(valueOf(s).floatValue());
    }

    public boolean isNaN() {
        return isNaN(this.value);
    }

    public boolean isInfinite() {
        return isInfinite(this.value);
    }

    public String toString() {
        return toString(this.value);
    }

    public byte byteValue() {
        return (byte) ((int) this.value);
    }

    public short shortValue() {
        return (short) ((int) this.value);
    }

    public int intValue() {
        return (int) this.value;
    }

    public long longValue() {
        return (long) this.value;
    }

    public float floatValue() {
        return this.value;
    }

    public double doubleValue() {
        return (double) this.value;
    }

    public int hashCode() {
        return floatToIntBits(this.value);
    }

    public static int hashCode(float value) {
        return floatToIntBits(value);
    }

    public boolean equals(Object obj) {
        if ((obj instanceof Float) && floatToIntBits(((Float) obj).value) == floatToIntBits(this.value)) {
            return true;
        }
        return false;
    }

    public static int floatToIntBits(float value) {
        int result = floatToRawIntBits(value);
        if ((result & FloatConsts.EXP_BIT_MASK) != FloatConsts.EXP_BIT_MASK || (FloatConsts.SIGNIF_BIT_MASK & result) == 0) {
            return result;
        }
        return 2143289344;
    }

    public int compareTo(Float anotherFloat) {
        return compare(this.value, anotherFloat.value);
    }

    public static int compare(float f1, float f2) {
        int i = -1;
        if (f1 < f2) {
            return -1;
        }
        if (f1 > f2) {
            return 1;
        }
        int thisBits = floatToIntBits(f1);
        int anotherBits = floatToIntBits(f2);
        if (thisBits == anotherBits) {
            i = 0;
        } else if (thisBits >= anotherBits) {
            i = 1;
        }
        return i;
    }

    public static float sum(float a, float b) {
        return a + b;
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static float min(float a, float b) {
        return Math.min(a, b);
    }
}
