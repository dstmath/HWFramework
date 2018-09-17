package java.lang;

import java.util.regex.Pattern;
import sun.misc.FloatConsts;
import sun.util.locale.LanguageTag;

public final class Long extends Number implements Comparable<Long> {
    public static final int BYTES = 8;
    public static final long MAX_VALUE = Long.MAX_VALUE;
    public static final long MIN_VALUE = Long.MIN_VALUE;
    public static final int SIZE = 64;
    public static final Class<Long> TYPE = null;
    private static final long serialVersionUID = 4290774380558885855L;
    private final long value;

    private static class LongCache {
        static final Long[] cache = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Long.LongCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Long.LongCache.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.lang.Long.LongCache.<clinit>():void");
        }

        private LongCache() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.Long.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.Long.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.Long.<clinit>():void");
    }

    public static java.lang.String toString(long r8, int r10) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r4 = 2;
        if (r10 < r4) goto L_0x0007;
    L_0x0003:
        r4 = 36;
        if (r10 <= r4) goto L_0x0009;
    L_0x0007:
        r10 = 10;
    L_0x0009:
        r4 = 10;
        if (r10 != r4) goto L_0x0012;
    L_0x000d:
        r4 = toString(r8);
        return r4;
    L_0x0012:
        r4 = 65;
        r0 = new char[r4];
        r1 = 64;
        r4 = 0;
        r4 = (r8 > r4 ? 1 : (r8 == r4 ? 0 : -1));
        if (r4 >= 0) goto L_0x003a;
    L_0x001e:
        r3 = 1;
    L_0x001f:
        if (r3 != 0) goto L_0x0038;
    L_0x0021:
        r8 = -r8;
        r2 = r1;
    L_0x0023:
        r4 = -r10;
        r4 = (long) r4;
        r4 = (r8 > r4 ? 1 : (r8 == r4 ? 0 : -1));
        if (r4 > 0) goto L_0x003c;
    L_0x0029:
        r1 = r2 + -1;
        r4 = java.lang.Integer.digits;
        r6 = (long) r10;
        r6 = r8 % r6;
        r6 = -r6;
        r5 = (int) r6;
        r4 = r4[r5];
        r0[r2] = r4;
        r4 = (long) r10;
        r8 = r8 / r4;
    L_0x0038:
        r2 = r1;
        goto L_0x0023;
    L_0x003a:
        r3 = 0;
        goto L_0x001f;
    L_0x003c:
        r4 = java.lang.Integer.digits;
        r6 = -r8;
        r5 = (int) r6;
        r4 = r4[r5];
        r0[r2] = r4;
        if (r3 == 0) goto L_0x0054;
    L_0x0046:
        r1 = r2 + -1;
        r4 = 45;
        r0[r1] = r4;
    L_0x004c:
        r4 = new java.lang.String;
        r5 = 65 - r1;
        r4.<init>(r0, r1, r5);
        return r4;
    L_0x0054:
        r1 = r2;
        goto L_0x004c;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.Long.toString(long, int):java.lang.String");
    }

    public static String toHexString(long i) {
        return toUnsignedString(i, 4);
    }

    public static String toOctalString(long i) {
        return toUnsignedString(i, 3);
    }

    public static String toBinaryString(long i) {
        return toUnsignedString(i, 1);
    }

    private static String toUnsignedString(long i, int shift) {
        char[] buf = new char[SIZE];
        int charPos = SIZE;
        long mask = (long) ((1 << shift) - 1);
        do {
            charPos--;
            buf[charPos] = Integer.digits[(int) (i & mask)];
            i >>>= shift;
        } while (i != 0);
        return new String(buf, charPos, 64 - charPos);
    }

    public static String toString(long i) {
        if (i == MIN_VALUE) {
            return "-9223372036854775808";
        }
        int size = i < 0 ? stringSize(-i) + 1 : stringSize(i);
        char[] buf = new char[size];
        getChars(i, size, buf);
        return new String(buf);
    }

    static void getChars(long i, int index, char[] buf) {
        int charPos = index;
        char sign = '\u0000';
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        while (i > 2147483647L) {
            long q = i / 100;
            int r = (int) (i - (((q << 6) + (q << 5)) + (q << 2)));
            i = q;
            charPos--;
            buf[charPos] = Integer.DigitOnes[r];
            charPos--;
            buf[charPos] = Integer.DigitTens[r];
        }
        int i2 = (int) i;
        while (i2 >= Record.OVERFLOW_OF_INT16) {
            int q2 = i2 / 100;
            r = i2 - (((q2 << 6) + (q2 << 5)) + (q2 << 2));
            i2 = q2;
            charPos--;
            buf[charPos] = Integer.DigitOnes[r];
            charPos--;
            buf[charPos] = Integer.DigitTens[r];
        }
        do {
            q2 = (52429 * i2) >>> 19;
            charPos--;
            buf[charPos] = Integer.digits[i2 - ((q2 << 3) + (q2 << 1))];
            i2 = q2;
        } while (q2 != 0);
        if (sign != '\u0000') {
            buf[charPos - 1] = sign;
        }
    }

    static int stringSize(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) {
                return i;
            }
            p *= 10;
        }
        return 19;
    }

    public static long parseLong(String s, int radix) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        } else if (radix < 2) {
            throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
        } else if (radix > 36) {
            throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
        } else {
            long result = 0;
            boolean negative = false;
            int i = 0;
            int len = s.length();
            long limit = -9223372036854775807L;
            if (len > 0) {
                char firstChar = s.charAt(0);
                if (firstChar < '0') {
                    if (firstChar == '-') {
                        negative = true;
                        limit = MIN_VALUE;
                    } else if (firstChar != '+') {
                        throw NumberFormatException.forInputString(s);
                    }
                    if (len == 1) {
                        throw NumberFormatException.forInputString(s);
                    }
                    i = 1;
                }
                long multmin = limit / ((long) radix);
                int i2 = i;
                while (i2 < len) {
                    i = i2 + 1;
                    int digit = Character.digit(s.charAt(i2), radix);
                    if (digit < 0) {
                        throw NumberFormatException.forInputString(s);
                    } else if (result < multmin) {
                        throw NumberFormatException.forInputString(s);
                    } else {
                        result *= (long) radix;
                        if (result < ((long) digit) + limit) {
                            throw NumberFormatException.forInputString(s);
                        }
                        result -= (long) digit;
                        i2 = i;
                    }
                }
                return negative ? result : -result;
            } else {
                throw NumberFormatException.forInputString(s);
            }
        }
    }

    public static long parseLong(String s) throws NumberFormatException {
        return parseLong(s, 10);
    }

    public static Long valueOf(String s, int radix) throws NumberFormatException {
        return valueOf(parseLong(s, radix));
    }

    public static Long valueOf(String s) throws NumberFormatException {
        return valueOf(parseLong(s, 10));
    }

    public static Long valueOf(long l) {
        if (l < -128 || l > 127) {
            return new Long(l);
        }
        return LongCache.cache[((int) l) + Pattern.CANON_EQ];
    }

    public static Long decode(String nm) throws NumberFormatException {
        int radix = 10;
        int index = 0;
        boolean negative = false;
        if (nm.length() == 0) {
            throw new NumberFormatException("Zero length string");
        }
        char firstChar = nm.charAt(0);
        if (firstChar == '-') {
            negative = true;
            index = 1;
        } else if (firstChar == '+') {
            index = 1;
        }
        if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        } else if (nm.startsWith("#", index)) {
            index++;
            radix = 16;
        } else if (nm.startsWith("0", index) && nm.length() > index + 1) {
            index++;
            radix = BYTES;
        }
        if (nm.startsWith(LanguageTag.SEP, index) || nm.startsWith("+", index)) {
            throw new NumberFormatException("Sign character in wrong position");
        }
        try {
            Long result = valueOf(nm.substring(index), radix);
            if (negative) {
                return valueOf(-result.longValue());
            }
            return result;
        } catch (NumberFormatException e) {
            String constant;
            if (negative) {
                constant = LanguageTag.SEP + nm.substring(index);
            } else {
                constant = nm.substring(index);
            }
            return valueOf(constant, radix);
        }
    }

    public Long(long value) {
        this.value = value;
    }

    public Long(String s) throws NumberFormatException {
        this.value = parseLong(s, 10);
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
        return this.value;
    }

    public float floatValue() {
        return (float) this.value;
    }

    public double doubleValue() {
        return (double) this.value;
    }

    public String toString() {
        return toString(this.value);
    }

    public int hashCode() {
        return hashCode(this.value);
    }

    public static int hashCode(long value) {
        return (int) ((value >>> 32) ^ value);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Long)) {
            return false;
        }
        if (this.value == ((Long) obj).longValue()) {
            z = true;
        }
        return z;
    }

    public static Long getLong(String nm) {
        return getLong(nm, null);
    }

    public static Long getLong(String nm, long val) {
        Long result = getLong(nm, null);
        return result == null ? valueOf(val) : result;
    }

    public static Long getLong(String nm, Long val) {
        String str = null;
        try {
            str = System.getProperty(nm);
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e2) {
        }
        if (str != null) {
            try {
                return decode(str);
            } catch (NumberFormatException e3) {
            }
        }
        return val;
    }

    public /* bridge */ /* synthetic */ int compareTo(Object anotherLong) {
        return compareTo((Long) anotherLong);
    }

    public int compareTo(Long anotherLong) {
        return compare(this.value, anotherLong.value);
    }

    public static int compare(long x, long y) {
        if (x < y) {
            return -1;
        }
        return x == y ? 0 : 1;
    }

    public static long highestOneBit(long i) {
        i |= i >> 1;
        i |= i >> 2;
        i |= i >> 4;
        i |= i >> BYTES;
        i |= i >> 16;
        i |= i >> 32;
        return i - (i >>> 1);
    }

    public static long lowestOneBit(long i) {
        return (-i) & i;
    }

    public static int numberOfLeadingZeros(long i) {
        if (i == 0) {
            return SIZE;
        }
        int n = 1;
        int x = (int) (i >>> 32);
        if (x == 0) {
            n = 33;
            x = (int) i;
        }
        if ((x >>> 16) == 0) {
            n += 16;
            x <<= 16;
        }
        if ((x >>> 24) == 0) {
            n += BYTES;
            x <<= BYTES;
        }
        if ((x >>> 28) == 0) {
            n += 4;
            x <<= 4;
        }
        if ((x >>> 30) == 0) {
            n += 2;
            x <<= 2;
        }
        return n - (x >>> 31);
    }

    public static int numberOfTrailingZeros(long i) {
        if (i == 0) {
            return SIZE;
        }
        int x;
        int n = 63;
        int y = (int) i;
        if (y != 0) {
            n = 31;
            x = y;
        } else {
            x = (int) (i >>> 32);
        }
        y = x << 16;
        if (y != 0) {
            n -= 16;
            x = y;
        }
        y = x << BYTES;
        if (y != 0) {
            n -= 8;
            x = y;
        }
        y = x << 4;
        if (y != 0) {
            n -= 4;
            x = y;
        }
        y = x << 2;
        if (y != 0) {
            n -= 2;
            x = y;
        }
        return n - ((x << 1) >>> 31);
    }

    public static int bitCount(long i) {
        i -= (i >>> 1) & 6148914691236517205L;
        i = (i & 3689348814741910323L) + ((i >>> 2) & 3689348814741910323L);
        i = ((i >>> 4) + i) & 1085102592571150095L;
        i += i >>> BYTES;
        i += i >>> 16;
        return ((int) (i + (i >>> 32))) & FloatConsts.MAX_EXPONENT;
    }

    public static long rotateLeft(long i, int distance) {
        return (i << distance) | (i >>> (-distance));
    }

    public static long rotateRight(long i, int distance) {
        return (i >>> distance) | (i << (-distance));
    }

    public static long reverse(long i) {
        i = ((6148914691236517205L & i) << 1) | ((i >>> 1) & 6148914691236517205L);
        i = ((3689348814741910323L & i) << 2) | ((i >>> 2) & 3689348814741910323L);
        i = ((1085102592571150095L & i) << 4) | ((i >>> 4) & 1085102592571150095L);
        i = ((71777214294589695L & i) << 8) | ((i >>> 8) & 71777214294589695L);
        return (((i << 48) | ((4294901760L & i) << 16)) | ((i >>> 16) & 4294901760L)) | (i >>> 48);
    }

    public static int signum(long i) {
        return (int) ((i >> 63) | ((-i) >>> 63));
    }

    public static long reverseBytes(long i) {
        i = ((i & 71777214294589695L) << 8) | ((i >>> 8) & 71777214294589695L);
        return (((i << 48) | ((i & 4294901760L) << 16)) | ((i >>> 16) & 4294901760L)) | (i >>> 48);
    }

    public static long sum(long a, long b) {
        return a + b;
    }

    public static long max(long a, long b) {
        return Math.max(a, b);
    }

    public static long min(long a, long b) {
        return Math.min(a, b);
    }
}
