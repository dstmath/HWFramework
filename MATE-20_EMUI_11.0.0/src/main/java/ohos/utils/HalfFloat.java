package ohos.utils;

import java.util.Objects;

public class HalfFloat extends Number {
    private static final short NEGATIVE_INFINITY = -1024;
    private static final short NaN = 32256;
    private static final short POSITIVE_INFINITY = 31744;
    private static final long serialVersionUID = 4916693642657173610L;
    private final short value;

    private static boolean isNaN(short s) {
        return s == 32256;
    }

    private HalfFloat(short s) {
        this.value = s;
    }

    private HalfFloat(float f) {
        this.value = fromFloat(f);
    }

    private HalfFloat(String str) {
        this.value = fromFloat(Float.parseFloat(str));
    }

    private static short fromFloat(float f) {
        if (Float.isNaN(f)) {
            return NaN;
        }
        if (f == Float.NEGATIVE_INFINITY) {
            return NEGATIVE_INFINITY;
        }
        if (f == Float.POSITIVE_INFINITY) {
            return POSITIVE_INFINITY;
        }
        return convert2Half(f);
    }

    private static short convert2Half(float f) {
        int i;
        int i2;
        int floatToRawIntBits = Float.floatToRawIntBits(f);
        int i3 = floatToRawIntBits >>> 31;
        int i4 = ((floatToRawIntBits >>> 23) & 255) - 112;
        int i5 = floatToRawIntBits & 8388607;
        if (i4 <= 0) {
            if (i4 < -10) {
                i2 = i3 << 15;
            } else {
                i5 = (i5 | 8388608) >> (1 - i4);
                if ((i5 & 4096) != 0) {
                    i5 += 8192;
                }
                i = i3 << 15;
                i2 = (i5 >> 13) | i;
            }
        } else if (i4 >= 31) {
            i2 = (i3 << 15) | 50176;
        } else {
            if ((i5 & 4096) != 0) {
                i5 += 8192;
                if ((8388608 & i5) != 0) {
                    i5 = 0;
                    i4++;
                }
            }
            i = (i3 << 15) | (i4 << 10);
            i2 = (i5 >> 13) | i;
        }
        return (short) i2;
    }

    private static float toFloat(short s) {
        if (isNaN(s)) {
            return Float.NaN;
        }
        if (s == -1024) {
            return Float.NEGATIVE_INFINITY;
        }
        if (s == 31744) {
            return Float.POSITIVE_INFINITY;
        }
        return convert2Float(s);
    }

    private static float convert2Float(short s) {
        int i = s & 65535;
        int i2 = i >>> 15;
        int i3 = (i >>> 10) & 31;
        int i4 = i & 1023;
        if (i3 == 0) {
            if (i4 == 0) {
                return Float.intBitsToFloat(i2 << 31);
            }
            while ((i4 & 1024) == 0) {
                i3--;
                i4 <<= 1;
            }
            i3++;
            i4 &= -1025;
        }
        return Float.intBitsToFloat((i4 << 13) | (i2 << 31) | ((i3 + 112) << 23));
    }

    public static HalfFloat valueOf(short s) {
        return new HalfFloat(s);
    }

    public static HalfFloat valueOf(float f) {
        return new HalfFloat(f);
    }

    public static HalfFloat valueOf(String str) {
        return new HalfFloat(str);
    }

    public short value() {
        return this.value;
    }

    @Override // java.lang.Number
    public byte byteValue() {
        return (byte) ((int) toFloat(this.value));
    }

    @Override // java.lang.Number
    public short shortValue() {
        return (short) ((int) toFloat(this.value));
    }

    @Override // java.lang.Number
    public int intValue() {
        return (int) toFloat(this.value);
    }

    @Override // java.lang.Number
    public long longValue() {
        return (long) toFloat(this.value);
    }

    @Override // java.lang.Number
    public float floatValue() {
        return toFloat(this.value);
    }

    @Override // java.lang.Number
    public double doubleValue() {
        return (double) toFloat(this.value);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.value == ((HalfFloat) obj).value;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Short.valueOf(this.value));
    }

    @Override // java.lang.Object
    public String toString() {
        return Float.toString(toFloat(this.value));
    }
}
