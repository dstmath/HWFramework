package java.lang;

import android.icu.text.UTF16;
import dalvik.bytecode.Opcodes;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import libcore.util.CharsetUtils;
import libcore.util.EmptyArray;

public final class StringFactory {
    private static final char REPLACEMENT_CHAR = '�';
    private static final int[] TABLE_UTF8_NEEDED = {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public static native String newStringFromBytes(byte[] bArr, int i, int i2, int i3);

    static native String newStringFromChars(int i, int i2, char[] cArr);

    public static native String newStringFromString(String str);

    public static String newEmptyString() {
        return newStringFromChars(EmptyArray.CHAR, 0, 0);
    }

    public static String newStringFromBytes(byte[] data) {
        return newStringFromBytes(data, 0, data.length);
    }

    public static String newStringFromBytes(byte[] data, int high) {
        return newStringFromBytes(data, high, 0, data.length);
    }

    public static String newStringFromBytes(byte[] data, int offset, int byteCount) {
        return newStringFromBytes(data, offset, byteCount, Charset.defaultCharset());
    }

    public static String newStringFromBytes(byte[] data, int offset, int byteCount, String charsetName) throws UnsupportedEncodingException {
        return newStringFromBytes(data, offset, byteCount, Charset.forNameUEE(charsetName));
    }

    public static String newStringFromBytes(byte[] data, String charsetName) throws UnsupportedEncodingException {
        return newStringFromBytes(data, 0, data.length, Charset.forNameUEE(charsetName));
    }

    public static String newStringFromBytes(byte[] data, int offset, int byteCount, Charset charset) {
        int length;
        char[] value;
        int i;
        int s;
        int s2;
        int s3;
        byte[] bArr = data;
        int i2 = offset;
        int i3 = byteCount;
        if ((i2 | i3) < 0 || i3 > bArr.length - i2) {
            Charset charset2 = charset;
            throw new StringIndexOutOfBoundsException(bArr.length, i2, i3);
        }
        String canonicalCharsetName = charset.name();
        if (canonicalCharsetName.equals("UTF-8")) {
            byte[] d = bArr;
            char[] v = new char[i3];
            int b = i2;
            int last = i2 + i3;
            int s4 = 0;
            int codePoint = 0;
            int utf8BytesSeen = 0;
            int utf8BytesNeeded = 0;
            int lowerBound = 128;
            int upperBound = 191;
            while (b < last) {
                int idx = b + 1;
                int idx2 = d[b] & 255;
                if (utf8BytesNeeded == 0) {
                    if ((idx2 & 128) == 0) {
                        s3 = s4 + 1;
                        v[s4] = (char) idx2;
                    } else if ((idx2 & 64) == 0) {
                        s3 = s4 + 1;
                        v[s4] = REPLACEMENT_CHAR;
                    } else {
                        utf8BytesNeeded = TABLE_UTF8_NEEDED[idx2 & 63];
                        if (utf8BytesNeeded == 0) {
                            v[s4] = REPLACEMENT_CHAR;
                            b = idx;
                            s4++;
                        } else {
                            codePoint = idx2 & (63 >> utf8BytesNeeded);
                            if (idx2 == 224) {
                                lowerBound = 160;
                            } else if (idx2 == 237) {
                                upperBound = 159;
                            } else if (idx2 == 240) {
                                lowerBound = 144;
                            } else if (idx2 == 244) {
                                upperBound = 143;
                            }
                        }
                    }
                    s4 = s3;
                } else if (idx2 < lowerBound || idx2 > upperBound) {
                    v[s4] = REPLACEMENT_CHAR;
                    codePoint = 0;
                    utf8BytesNeeded = 0;
                    utf8BytesSeen = 0;
                    lowerBound = 128;
                    upperBound = 191;
                    b = idx - 1;
                    s4++;
                } else {
                    lowerBound = 128;
                    upperBound = 191;
                    codePoint = (codePoint << 6) | (idx2 & 63);
                    utf8BytesSeen++;
                    if (utf8BytesNeeded == utf8BytesSeen) {
                        if (codePoint < 65536) {
                            s2 = s4 + 1;
                            v[s4] = (char) codePoint;
                        } else {
                            int s5 = s4 + 1;
                            v[s4] = (char) ((codePoint >> 10) + 55232);
                            v[s5] = (char) ((codePoint & Opcodes.OP_NEW_INSTANCE_JUMBO) + UTF16.TRAIL_SURROGATE_MIN_VALUE);
                            s2 = s5 + 1;
                        }
                        codePoint = 0;
                        utf8BytesNeeded = 0;
                        utf8BytesSeen = 0;
                        s4 = s2;
                    }
                }
                b = idx;
            }
            if (utf8BytesNeeded != 0) {
                s = s4 + 1;
                v[s4] = REPLACEMENT_CHAR;
            } else {
                s = s4;
            }
            if (s == i3) {
                value = v;
                length = s;
            } else {
                value = new char[s];
                length = s;
                byte[] bArr2 = d;
                System.arraycopy(v, 0, value, 0, s);
            }
        } else if (canonicalCharsetName.equals("ISO-8859-1")) {
            value = new char[i3];
            length = i3;
            CharsetUtils.isoLatin1BytesToChars(bArr, i2, i3, value);
        } else if (canonicalCharsetName.equals("US-ASCII")) {
            value = new char[i3];
            length = i3;
            CharsetUtils.asciiBytesToChars(bArr, i2, i3, value);
        } else {
            CharBuffer cb = charset.decode(ByteBuffer.wrap(data, offset, byteCount));
            length = cb.length();
            if (length > 0) {
                value = new char[length];
                i = 0;
                System.arraycopy(cb.array(), 0, value, 0, length);
            } else {
                i = 0;
                value = EmptyArray.CHAR;
            }
            return newStringFromChars(value, i, length);
        }
        Charset charset3 = charset;
        i = 0;
        return newStringFromChars(value, i, length);
    }

    public static String newStringFromBytes(byte[] data, Charset charset) {
        return newStringFromBytes(data, 0, data.length, charset);
    }

    public static String newStringFromChars(char[] data) {
        return newStringFromChars(data, 0, data.length);
    }

    public static String newStringFromChars(char[] data, int offset, int charCount) {
        if ((offset | charCount) >= 0 && charCount <= data.length - offset) {
            return newStringFromChars(offset, charCount, data);
        }
        throw new StringIndexOutOfBoundsException(data.length, offset, charCount);
    }

    public static String newStringFromStringBuffer(StringBuffer stringBuffer) {
        String newStringFromChars;
        synchronized (stringBuffer) {
            newStringFromChars = newStringFromChars(stringBuffer.getValue(), 0, stringBuffer.length());
        }
        return newStringFromChars;
    }

    public static String newStringFromCodePoints(int[] codePoints, int offset, int count) {
        if (codePoints == null) {
            throw new NullPointerException("codePoints == null");
        } else if ((offset | count) < 0 || count > codePoints.length - offset) {
            throw new StringIndexOutOfBoundsException(codePoints.length, offset, count);
        } else {
            char[] value = new char[(count * 2)];
            int end = offset + count;
            int length = 0;
            for (int i = offset; i < end; i++) {
                length += Character.toChars(codePoints[i], value, length);
            }
            return newStringFromChars(value, 0, length);
        }
    }

    public static String newStringFromStringBuilder(StringBuilder stringBuilder) {
        return newStringFromChars(stringBuilder.getValue(), 0, stringBuilder.length());
    }
}
