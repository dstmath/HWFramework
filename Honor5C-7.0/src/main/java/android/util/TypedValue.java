package android.util;

import android.view.InputDevice;

public class TypedValue {
    public static final int COMPLEX_MANTISSA_MASK = 16777215;
    public static final int COMPLEX_MANTISSA_SHIFT = 8;
    public static final int COMPLEX_RADIX_0p23 = 3;
    public static final int COMPLEX_RADIX_16p7 = 1;
    public static final int COMPLEX_RADIX_23p0 = 0;
    public static final int COMPLEX_RADIX_8p15 = 2;
    public static final int COMPLEX_RADIX_MASK = 3;
    public static final int COMPLEX_RADIX_SHIFT = 4;
    public static final int COMPLEX_UNIT_DIP = 1;
    public static final int COMPLEX_UNIT_FRACTION = 0;
    public static final int COMPLEX_UNIT_FRACTION_PARENT = 1;
    public static final int COMPLEX_UNIT_IN = 4;
    public static final int COMPLEX_UNIT_MASK = 15;
    public static final int COMPLEX_UNIT_MM = 5;
    public static final int COMPLEX_UNIT_PT = 3;
    public static final int COMPLEX_UNIT_PX = 0;
    public static final int COMPLEX_UNIT_SHIFT = 0;
    public static final int COMPLEX_UNIT_SP = 2;
    public static final int DATA_NULL_EMPTY = 1;
    public static final int DATA_NULL_UNDEFINED = 0;
    public static final int DENSITY_DEFAULT = 0;
    public static final int DENSITY_NONE = 65535;
    private static final String[] DIMENSION_UNIT_STRS = null;
    private static final String[] FRACTION_UNIT_STRS = null;
    private static final float MANTISSA_MULT = 0.00390625f;
    private static final float[] RADIX_MULTS = null;
    public static final int TYPE_ATTRIBUTE = 2;
    public static final int TYPE_DIMENSION = 5;
    public static final int TYPE_FIRST_COLOR_INT = 28;
    public static final int TYPE_FIRST_INT = 16;
    public static final int TYPE_FLOAT = 4;
    public static final int TYPE_FRACTION = 6;
    public static final int TYPE_INT_BOOLEAN = 18;
    public static final int TYPE_INT_COLOR_ARGB4 = 30;
    public static final int TYPE_INT_COLOR_ARGB8 = 28;
    public static final int TYPE_INT_COLOR_RGB4 = 31;
    public static final int TYPE_INT_COLOR_RGB8 = 29;
    public static final int TYPE_INT_DEC = 16;
    public static final int TYPE_INT_HEX = 17;
    public static final int TYPE_LAST_COLOR_INT = 31;
    public static final int TYPE_LAST_INT = 31;
    public static final int TYPE_NULL = 0;
    public static final int TYPE_REFERENCE = 1;
    public static final int TYPE_STRING = 3;
    public int assetCookie;
    public int changingConfigurations;
    public int data;
    public int density;
    public int resourceId;
    public CharSequence string;
    public int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.TypedValue.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.TypedValue.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.util.TypedValue.<clinit>():void");
    }

    public TypedValue() {
        this.changingConfigurations = -1;
    }

    public final float getFloat() {
        return Float.intBitsToFloat(this.data);
    }

    public static float complexToFloat(int complex) {
        return ((float) (complex & InputDevice.SOURCE_ANY)) * RADIX_MULTS[(complex >> TYPE_FLOAT) & TYPE_STRING];
    }

    public static float complexToDimension(int data, DisplayMetrics metrics) {
        return applyDimension((data >> TYPE_NULL) & COMPLEX_UNIT_MASK, complexToFloat(data), metrics);
    }

    public static int complexToDimensionPixelOffset(int data, DisplayMetrics metrics) {
        return (int) applyDimension((data >> TYPE_NULL) & COMPLEX_UNIT_MASK, complexToFloat(data), metrics);
    }

    public static int complexToDimensionPixelSize(int data, DisplayMetrics metrics) {
        float value = complexToFloat(data);
        int res = (int) (0.5f + applyDimension((data >> TYPE_NULL) & COMPLEX_UNIT_MASK, value, metrics));
        if (res != 0) {
            return res;
        }
        if (value == 0.0f) {
            return TYPE_NULL;
        }
        if (value > 0.0f) {
            return TYPE_REFERENCE;
        }
        return -1;
    }

    @Deprecated
    public static float complexToDimensionNoisy(int data, DisplayMetrics metrics) {
        return complexToDimension(data, metrics);
    }

    public int getComplexUnit() {
        return (this.data >> TYPE_NULL) & COMPLEX_UNIT_MASK;
    }

    public static float applyDimension(int unit, float value, DisplayMetrics metrics) {
        switch (unit) {
            case TYPE_NULL /*0*/:
                return value;
            case TYPE_REFERENCE /*1*/:
                return metrics.density * value;
            case TYPE_ATTRIBUTE /*2*/:
                return metrics.scaledDensity * value;
            case TYPE_STRING /*3*/:
                return (metrics.xdpi * value) * 0.013888889f;
            case TYPE_FLOAT /*4*/:
                return metrics.xdpi * value;
            case TYPE_DIMENSION /*5*/:
                return (metrics.xdpi * value) * 0.03937008f;
            default:
                return 0.0f;
        }
    }

    public float getDimension(DisplayMetrics metrics) {
        return complexToDimension(this.data, metrics);
    }

    public static float complexToFraction(int data, float base, float pbase) {
        switch ((data >> TYPE_NULL) & COMPLEX_UNIT_MASK) {
            case TYPE_NULL /*0*/:
                return complexToFloat(data) * base;
            case TYPE_REFERENCE /*1*/:
                return complexToFloat(data) * pbase;
            default:
                return 0.0f;
        }
    }

    public float getFraction(float base, float pbase) {
        return complexToFraction(this.data, base, pbase);
    }

    public final CharSequence coerceToString() {
        int t = this.type;
        if (t == TYPE_STRING) {
            return this.string;
        }
        return coerceToString(t, this.data);
    }

    public static final String coerceToString(int type, int data) {
        switch (type) {
            case TYPE_NULL /*0*/:
                return null;
            case TYPE_REFERENCE /*1*/:
                return "@" + data;
            case TYPE_ATTRIBUTE /*2*/:
                return "?" + data;
            case TYPE_FLOAT /*4*/:
                return Float.toString(Float.intBitsToFloat(data));
            case TYPE_DIMENSION /*5*/:
                return Float.toString(complexToFloat(data)) + DIMENSION_UNIT_STRS[(data >> TYPE_NULL) & COMPLEX_UNIT_MASK];
            case TYPE_FRACTION /*6*/:
                return Float.toString(complexToFloat(data) * 100.0f) + FRACTION_UNIT_STRS[(data >> TYPE_NULL) & COMPLEX_UNIT_MASK];
            case TYPE_INT_HEX /*17*/:
                return "0x" + Integer.toHexString(data);
            case TYPE_INT_BOOLEAN /*18*/:
                return data != 0 ? "true" : "false";
            default:
                if (type >= TYPE_INT_COLOR_ARGB8 && type <= TYPE_LAST_INT) {
                    return "#" + Integer.toHexString(data);
                }
                if (type < TYPE_INT_DEC || type > TYPE_LAST_INT) {
                    return null;
                }
                return Integer.toString(data);
        }
    }

    public void setTo(TypedValue other) {
        this.type = other.type;
        this.string = other.string;
        this.data = other.data;
        this.assetCookie = other.assetCookie;
        this.resourceId = other.resourceId;
        this.density = other.density;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TypedValue{t=0x").append(Integer.toHexString(this.type));
        sb.append("/d=0x").append(Integer.toHexString(this.data));
        if (this.type == TYPE_STRING) {
            sb.append(" \"").append(this.string != null ? this.string : "<null>").append("\"");
        }
        if (this.assetCookie != 0) {
            sb.append(" a=").append(this.assetCookie);
        }
        if (this.resourceId != 0) {
            sb.append(" r=0x").append(Integer.toHexString(this.resourceId));
        }
        sb.append("}");
        return sb.toString();
    }
}
