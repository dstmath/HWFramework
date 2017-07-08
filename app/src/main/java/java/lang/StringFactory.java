package java.lang;

import android.icu.text.UTF16;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import libcore.util.CharsetUtils;
import libcore.util.EmptyArray;
import org.w3c.dom.traversal.NodeFilter;

public final class StringFactory {
    private static final char REPLACEMENT_CHAR = '\ufffd';

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
        if ((offset | byteCount) >= 0) {
            if (byteCount <= data.length - offset) {
                char[] value;
                int length;
                String canonicalCharsetName = charset.name();
                if (canonicalCharsetName.equals("UTF-8")) {
                    byte[] d = data;
                    char[] v = new char[byteCount];
                    int last = offset + byteCount;
                    int s = 0;
                    int idx = offset;
                    while (idx < last) {
                        int s2;
                        int idx2 = idx + 1;
                        byte b0 = data[idx];
                        if ((b0 & NodeFilter.SHOW_COMMENT) == 0) {
                            s2 = s + 1;
                            v[s] = (char) (b0 & Opcodes.OP_CONST_CLASS_JUMBO);
                        } else {
                            int i = b0 & Opcodes.OP_SHL_INT_LIT8;
                            if (r0 != 192) {
                                i = b0 & Opcodes.OP_INVOKE_DIRECT_EMPTY;
                                if (r0 != 224) {
                                    i = b0 & Opcodes.OP_INVOKE_VIRTUAL_QUICK;
                                    if (r0 != 240) {
                                        i = b0 & SCSU.ARMENIANINDEX;
                                        if (r0 != 248) {
                                            i = b0 & SCSU.KATAKANAINDEX;
                                            if (r0 != 252) {
                                                s2 = s + 1;
                                                v[s] = REPLACEMENT_CHAR;
                                            }
                                        }
                                    }
                                }
                            }
                            int utfCount = 1;
                            i = b0 & Opcodes.OP_INVOKE_DIRECT_EMPTY;
                            if (r0 == 224) {
                                utfCount = 2;
                            } else {
                                i = b0 & Opcodes.OP_INVOKE_VIRTUAL_QUICK;
                                if (r0 == 240) {
                                    utfCount = 3;
                                } else {
                                    i = b0 & SCSU.ARMENIANINDEX;
                                    if (r0 == 248) {
                                        utfCount = 4;
                                    } else {
                                        i = b0 & SCSU.KATAKANAINDEX;
                                        if (r0 == 252) {
                                            utfCount = 5;
                                        }
                                    }
                                }
                            }
                            if (idx2 + utfCount > last) {
                                s2 = s + 1;
                                v[s] = REPLACEMENT_CHAR;
                                s = s2;
                                idx = idx2;
                            } else {
                                int val = b0 & (31 >> (utfCount - 1));
                                int i2 = 0;
                                idx = idx2;
                                while (i2 < utfCount) {
                                    idx2 = idx + 1;
                                    byte b = data[idx];
                                    i = b & Opcodes.OP_AND_LONG_2ADDR;
                                    if (r0 != 128) {
                                        s2 = s + 1;
                                        v[s] = REPLACEMENT_CHAR;
                                        s = s2;
                                        idx = idx2 - 1;
                                        break;
                                    }
                                    val = (val << 6) | (b & 63);
                                    i2++;
                                    idx = idx2;
                                }
                                if (utfCount != 2 && val >= 55296 && val <= 57343) {
                                    s2 = s + 1;
                                    v[s] = REPLACEMENT_CHAR;
                                    s = s2;
                                } else if (val > 1114111) {
                                    s2 = s + 1;
                                    v[s] = REPLACEMENT_CHAR;
                                    s = s2;
                                } else {
                                    if (val < 65536) {
                                        s2 = s + 1;
                                        v[s] = (char) val;
                                    } else {
                                        int x = val & DexFormat.MAX_TYPE_IDX;
                                        int lo = UTF16.TRAIL_SURROGATE_MIN_VALUE | (x & Opcodes.OP_NEW_INSTANCE_JUMBO);
                                        s2 = s + 1;
                                        v[s] = (char) (((((((val >> 16) & 31) - 1) & DexFormat.MAX_TYPE_IDX) << 6) | UTF16.SURROGATE_MIN_VALUE) | (x >> 10));
                                        s = s2 + 1;
                                        v[s2] = (char) lo;
                                        s2 = s;
                                    }
                                    idx2 = idx;
                                }
                            }
                        }
                        s = s2;
                        idx = idx2;
                    }
                    if (s == byteCount) {
                        value = v;
                        length = s;
                    } else {
                        value = new char[s];
                        length = s;
                        System.arraycopy(v, 0, value, 0, s);
                    }
                } else {
                    if (canonicalCharsetName.equals("ISO-8859-1")) {
                        value = new char[byteCount];
                        length = byteCount;
                        CharsetUtils.isoLatin1BytesToChars(data, offset, byteCount, value);
                    } else {
                        if (canonicalCharsetName.equals("US-ASCII")) {
                            value = new char[byteCount];
                            length = byteCount;
                            CharsetUtils.asciiBytesToChars(data, offset, byteCount, value);
                        } else {
                            CharBuffer cb = charset.decode(ByteBuffer.wrap(data, offset, byteCount));
                            length = cb.length();
                            if (length > 0) {
                                value = new char[length];
                                System.arraycopy(cb.array(), 0, value, 0, length);
                            } else {
                                value = EmptyArray.CHAR;
                            }
                        }
                    }
                }
                return newStringFromChars(value, 0, length);
            }
        }
        throw new StringIndexOutOfBoundsException(data.length, offset, byteCount);
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
            int length = 0;
            for (int i = offset; i < offset + count; i++) {
                length += Character.toChars(codePoints[i], value, length);
            }
            return newStringFromChars(value, 0, length);
        }
    }

    public static String newStringFromStringBuilder(StringBuilder stringBuilder) {
        return newStringFromChars(stringBuilder.getValue(), 0, stringBuilder.length());
    }
}
