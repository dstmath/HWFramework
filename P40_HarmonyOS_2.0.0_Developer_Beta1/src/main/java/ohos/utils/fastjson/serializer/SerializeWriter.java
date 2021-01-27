package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import ohos.global.icu.text.Bidi;
import ohos.global.icu.util.ULocale;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.parser.JSONLexer;

public final class SerializeWriter extends Writer {
    public static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static final char[] DigitOnes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    static final char[] DigitTens = {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'};
    static final char[] ascii_chars = {'0', '0', '0', '1', '0', '2', '0', '3', '0', '4', '0', '5', '0', '6', '0', '7', '0', '8', '0', '9', '0', 'A', '0', 'B', '0', 'C', '0', 'D', '0', 'E', '0', 'F', '1', '0', '1', '1', '1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9', '1', 'A', '1', 'B', '1', 'C', '1', 'D', '1', 'E', '1', 'F', '2', '0', '2', '1', '2', '2', '2', '3', '2', '4', '2', '5', '2', '6', '2', '7', '2', '8', '2', '9', '2', 'A', '2', 'B', '2', 'C', '2', 'D', '2', 'E', '2', 'F'};
    private static final ThreadLocal<char[]> bufLocal = new ThreadLocal<>();
    static final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', ULocale.UNICODE_LOCALE_EXTENSION, 'v', 'w', ULocale.PRIVATE_USE_EXTENSION, 'y', 'z'};
    static final char[] replaceChars = new char[93];
    static final int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    static final byte[] specicalFlags_doubleQuotes = new byte[161];
    static final byte[] specicalFlags_singleQuotes = new byte[161];
    protected char[] buf;
    protected int count;
    protected int features;
    protected final Writer writer;

    static {
        byte[] bArr = specicalFlags_doubleQuotes;
        bArr[0] = 4;
        bArr[1] = 4;
        bArr[2] = 4;
        bArr[3] = 4;
        bArr[4] = 4;
        bArr[5] = 4;
        bArr[6] = 4;
        bArr[7] = 4;
        bArr[8] = 1;
        bArr[9] = 1;
        bArr[10] = 1;
        bArr[11] = 4;
        bArr[12] = 1;
        bArr[13] = 1;
        bArr[34] = 1;
        bArr[92] = 1;
        byte[] bArr2 = specicalFlags_singleQuotes;
        bArr2[0] = 4;
        bArr2[1] = 4;
        bArr2[2] = 4;
        bArr2[3] = 4;
        bArr2[4] = 4;
        bArr2[5] = 4;
        bArr2[6] = 4;
        bArr2[7] = 4;
        bArr2[8] = 1;
        bArr2[9] = 1;
        bArr2[10] = 1;
        bArr2[11] = 4;
        bArr2[12] = 1;
        bArr2[13] = 1;
        bArr2[92] = 1;
        bArr2[39] = 1;
        for (int i = 14; i <= 31; i++) {
            specicalFlags_doubleQuotes[i] = 4;
            specicalFlags_singleQuotes[i] = 4;
        }
        for (int i2 = Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT; i2 < 160; i2++) {
            specicalFlags_doubleQuotes[i2] = 4;
            specicalFlags_singleQuotes[i2] = 4;
        }
        char[] cArr = replaceChars;
        cArr[0] = '0';
        cArr[1] = '1';
        cArr[2] = '2';
        cArr[3] = '3';
        cArr[4] = '4';
        cArr[5] = '5';
        cArr[6] = '6';
        cArr[7] = '7';
        cArr[8] = 'b';
        cArr[9] = 't';
        cArr[10] = 'n';
        cArr[11] = 'v';
        cArr[12] = 'f';
        cArr[13] = 'r';
        cArr[34] = '\"';
        cArr[39] = '\'';
        cArr[47] = '/';
        cArr[92] = '\\';
    }

    public SerializeWriter() {
        this((Writer) null);
    }

    public SerializeWriter(Writer writer2) {
        this.writer = writer2;
        this.features = JSON.DEFAULT_GENERATE_FEATURE;
        this.buf = bufLocal.get();
        ThreadLocal<char[]> threadLocal = bufLocal;
        if (threadLocal != null) {
            threadLocal.set(null);
        }
        if (this.buf == null) {
            this.buf = new char[1024];
        }
    }

    public SerializeWriter(SerializerFeature... serializerFeatureArr) {
        this(null, 0, serializerFeatureArr);
    }

    public SerializeWriter(Writer writer2, int i, SerializerFeature[] serializerFeatureArr) {
        this.writer = writer2;
        this.buf = bufLocal.get();
        if (this.buf != null) {
            bufLocal.set(null);
        }
        if (this.buf == null) {
            this.buf = new char[1024];
        }
        for (SerializerFeature serializerFeature : serializerFeatureArr) {
            i |= serializerFeature.mask;
        }
        this.features = i;
    }

    public SerializeWriter(int i) {
        this(null, i);
    }

    public SerializeWriter(Writer writer2, int i) {
        this.writer = writer2;
        if (i > 0) {
            this.buf = new char[i];
            return;
        }
        throw new IllegalArgumentException("Negative initial size: " + i);
    }

    public void config(SerializerFeature serializerFeature, boolean z) {
        if (z) {
            this.features = serializerFeature.mask | this.features;
            return;
        }
        this.features = (~serializerFeature.mask) & this.features;
    }

    public boolean isEnabled(SerializerFeature serializerFeature) {
        return (this.features & serializerFeature.mask) != 0;
    }

    @Override // java.io.Writer
    public void write(int i) {
        int i2 = this.count + 1;
        if (i2 > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(i2);
            } else {
                flush();
                i2 = 1;
            }
        }
        this.buf[this.count] = (char) i;
        this.count = i2;
    }

    @Override // java.io.Writer
    public void write(char[] cArr, int i, int i2) {
        int i3;
        if (i < 0 || i > cArr.length || i2 < 0 || (i3 = i + i2) > cArr.length || i3 < 0) {
            throw new IndexOutOfBoundsException();
        } else if (i2 != 0) {
            int i4 = this.count + i2;
            if (i4 > this.buf.length) {
                if (this.writer == null) {
                    expandCapacity(i4);
                } else {
                    do {
                        char[] cArr2 = this.buf;
                        int length = cArr2.length;
                        int i5 = this.count;
                        int i6 = length - i5;
                        System.arraycopy(cArr, i, cArr2, i5, i6);
                        this.count = this.buf.length;
                        flush();
                        i2 -= i6;
                        i += i6;
                    } while (i2 > this.buf.length);
                    i4 = i2;
                }
            }
            System.arraycopy(cArr, i, this.buf, this.count, i2);
            this.count = i4;
        }
    }

    /* access modifiers changed from: protected */
    public void expandCapacity(int i) {
        int length = ((this.buf.length * 3) / 2) + 1;
        if (length >= i) {
            i = length;
        }
        char[] cArr = new char[i];
        System.arraycopy(this.buf, 0, cArr, 0, this.count);
        this.buf = cArr;
    }

    @Override // java.io.Writer
    public void write(String str, int i, int i2) {
        int i3;
        int i4 = this.count + i2;
        if (i4 > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(i4);
            } else {
                while (true) {
                    char[] cArr = this.buf;
                    int length = cArr.length;
                    int i5 = this.count;
                    int i6 = length - i5;
                    i3 = i + i6;
                    str.getChars(i, i3, cArr, i5);
                    this.count = this.buf.length;
                    flush();
                    i2 -= i6;
                    if (i2 <= this.buf.length) {
                        break;
                    }
                    i = i3;
                }
                i4 = i2;
                i = i3;
            }
        }
        str.getChars(i, i2 + i, this.buf, this.count);
        this.count = i4;
    }

    public void writeTo(Writer writer2) throws IOException {
        if (this.writer == null) {
            writer2.write(this.buf, 0, this.count);
            return;
        }
        throw new UnsupportedOperationException("writer not null");
    }

    public void writeTo(OutputStream outputStream, String str) throws IOException {
        writeTo(outputStream, Charset.forName(str));
    }

    public void writeTo(OutputStream outputStream, Charset charset) throws IOException {
        if (this.writer == null) {
            outputStream.write(new String(this.buf, 0, this.count).getBytes(charset.name()));
            return;
        }
        throw new UnsupportedOperationException("writer not null");
    }

    @Override // java.io.Writer, java.lang.Appendable
    public SerializeWriter append(CharSequence charSequence) {
        String charSequence2 = charSequence == null ? "null" : charSequence.toString();
        write(charSequence2, 0, charSequence2.length());
        return this;
    }

    @Override // java.io.Writer, java.lang.Appendable
    public SerializeWriter append(CharSequence charSequence, int i, int i2) {
        if (charSequence == null) {
            charSequence = "null";
        }
        String charSequence2 = charSequence.subSequence(i, i2).toString();
        write(charSequence2, 0, charSequence2.length());
        return this;
    }

    @Override // java.io.Writer, java.lang.Appendable
    public SerializeWriter append(char c) {
        write(c);
        return this;
    }

    public byte[] toBytes(String str) {
        if (this.writer == null) {
            if (str == null) {
                str = "UTF-8";
            }
            try {
                return new String(this.buf, 0, this.count).getBytes(str);
            } catch (UnsupportedEncodingException e) {
                throw new JSONException("toBytes error", e);
            }
        } else {
            throw new UnsupportedOperationException("writer not null");
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return new String(this.buf, 0, this.count);
    }

    @Override // java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        if (this.writer != null && this.count > 0) {
            flush();
        }
        char[] cArr = this.buf;
        if (cArr.length <= 8192) {
            bufLocal.set(cArr);
        }
        this.buf = null;
    }

    @Override // java.io.Writer
    public void write(String str) {
        if (str == null) {
            writeNull();
        } else {
            write(str, 0, str.length());
        }
    }

    public void writeInt(int i) {
        if (i == Integer.MIN_VALUE) {
            write("-2147483648");
            return;
        }
        int i2 = 0;
        while ((i < 0 ? -i : i) > sizeTable[i2]) {
            i2++;
        }
        int i3 = i2 + 1;
        if (i < 0) {
            i3++;
        }
        int i4 = this.count + i3;
        if (i4 > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(i4);
            } else {
                char[] cArr = new char[i3];
                getChars((long) i, i3, cArr);
                write(cArr, 0, cArr.length);
                return;
            }
        }
        getChars((long) i, i4, this.buf);
        this.count = i4;
    }

    public void writeByteArray(byte[] bArr) {
        int length = bArr.length;
        int i = 0;
        boolean z = (this.features & SerializerFeature.UseSingleQuotes.mask) != 0;
        char c = z ? '\'' : '\"';
        if (length == 0) {
            write(z ? "''" : "\"\"");
            return;
        }
        char[] cArr = JSONLexer.CA;
        int i2 = (length / 3) * 3;
        int i3 = length - 1;
        int i4 = this.count;
        int i5 = (((i3 / 3) + 1) << 2) + i4 + 2;
        if (i5 > this.buf.length) {
            if (this.writer != null) {
                write(c);
                int i6 = 0;
                while (i6 < i2) {
                    int i7 = i6 + 1;
                    int i8 = i7 + 1;
                    int i9 = ((bArr[i6] & 255) << 16) | ((bArr[i7] & 255) << 8) | (bArr[i8] & 255);
                    write(cArr[(i9 >>> 18) & 63]);
                    write(cArr[(i9 >>> 12) & 63]);
                    write(cArr[(i9 >>> 6) & 63]);
                    write(cArr[i9 & 63]);
                    i6 = i8 + 1;
                }
                int i10 = length - i2;
                if (i10 > 0) {
                    int i11 = (bArr[i2] & 255) << 10;
                    if (i10 == 2) {
                        i = (bArr[i3] & 255) << 2;
                    }
                    int i12 = i11 | i;
                    write(cArr[i12 >> 12]);
                    write(cArr[(i12 >>> 6) & 63]);
                    write(i10 == 2 ? cArr[i12 & 63] : '=');
                    write(61);
                }
                write(c);
                return;
            }
            expandCapacity(i5);
        }
        this.count = i5;
        int i13 = i4 + 1;
        this.buf[i4] = c;
        int i14 = 0;
        while (i14 < i2) {
            int i15 = i14 + 1;
            int i16 = i15 + 1;
            int i17 = ((bArr[i14] & 255) << 16) | ((bArr[i15] & 255) << 8);
            int i18 = i16 + 1;
            int i19 = i17 | (bArr[i16] & 255);
            char[] cArr2 = this.buf;
            int i20 = i13 + 1;
            cArr2[i13] = cArr[(i19 >>> 18) & 63];
            int i21 = i20 + 1;
            cArr2[i20] = cArr[(i19 >>> 12) & 63];
            int i22 = i21 + 1;
            cArr2[i21] = cArr[(i19 >>> 6) & 63];
            i13 = i22 + 1;
            cArr2[i22] = cArr[i19 & 63];
            i14 = i18;
        }
        int i23 = length - i2;
        if (i23 > 0) {
            int i24 = (bArr[i2] & 255) << 10;
            if (i23 == 2) {
                i = (bArr[i3] & 255) << 2;
            }
            int i25 = i24 | i;
            char[] cArr3 = this.buf;
            cArr3[i5 - 5] = cArr[i25 >> 12];
            cArr3[i5 - 4] = cArr[(i25 >>> 6) & 63];
            cArr3[i5 - 3] = i23 == 2 ? cArr[i25 & 63] : '=';
            this.buf[i5 - 2] = '=';
        }
        this.buf[i5 - 1] = c;
    }

    public void writeLong(long j) {
        if (j == Long.MIN_VALUE) {
            write("-9223372036854775808");
            return;
        }
        int i = (j > 0 ? 1 : (j == 0 ? 0 : -1));
        long j2 = i < 0 ? -j : j;
        int i2 = 1;
        long j3 = 10;
        while (true) {
            if (i2 >= 19) {
                i2 = 0;
                break;
            } else if (j2 < j3) {
                break;
            } else {
                j3 *= 10;
                i2++;
            }
        }
        if (i2 == 0) {
            i2 = 19;
        }
        if (i < 0) {
            i2++;
        }
        int i3 = this.count + i2;
        if (i3 > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(i3);
            } else {
                char[] cArr = new char[i2];
                getChars(j, i2, cArr);
                write(cArr, 0, cArr.length);
                return;
            }
        }
        getChars(j, i3, this.buf);
        this.count = i3;
    }

    public void writeNull() {
        write("null");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0093, code lost:
        if (r14 == -1) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00c1, code lost:
        if ((ohos.utils.fastjson.serializer.SerializerFeature.WriteSlashAsSpecial.mask & r17.features) != 0) goto L_0x00c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00c9, code lost:
        if (r3 != '\\') goto L_0x00b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00d2, code lost:
        if (r3 != '\"') goto L_0x00b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00e5, code lost:
        if (r14 == -1) goto L_0x0095;
     */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x00e8 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00d7  */
    public void writeStringWithDoubleQuote(String str, char c, boolean z) {
        int i;
        boolean z2;
        if (str == null) {
            writeNull();
            if (c != 0) {
                write(c);
                return;
            }
            return;
        }
        int length = str.length();
        int i2 = this.count + length + 2;
        if (c != 0) {
            i2++;
        }
        char c2 = '/';
        if (i2 > this.buf.length) {
            if (this.writer != null) {
                write(34);
                for (int i3 = 0; i3 < str.length(); i3++) {
                    char charAt = str.charAt(i3);
                    byte[] bArr = specicalFlags_doubleQuotes;
                    if ((charAt >= bArr.length || bArr[charAt] == 0) && (charAt != '/' || (this.features & SerializerFeature.WriteSlashAsSpecial.mask) == 0)) {
                        write(charAt);
                    } else {
                        write(92);
                        write(replaceChars[charAt]);
                    }
                }
                write(34);
                if (c != 0) {
                    write(c);
                    return;
                }
                return;
            }
            expandCapacity(i2);
        }
        int i4 = this.count;
        int i5 = i4 + 1;
        int i6 = i5 + length;
        char[] cArr = this.buf;
        cArr[i4] = '\"';
        str.getChars(0, length, cArr, i5);
        this.count = i2;
        if (z) {
            int i7 = i2;
            int i8 = -1;
            int i9 = -1;
            int i10 = 0;
            char c3 = 0;
            int i11 = i5;
            while (i11 < i6) {
                char c4 = this.buf[i11];
                if (c4 == 8232) {
                    i10++;
                    i7 += 4;
                } else if (c4 >= ']') {
                    if (c4 >= 127 && c4 < 160) {
                        if (i8 == -1) {
                            i8 = i11;
                        }
                        i10++;
                        i7 += 4;
                        c3 = c4;
                        i9 = i11;
                    }
                    i11++;
                    c2 = '/';
                } else {
                    if (c4 != ' ') {
                        if (c4 == c2) {
                        }
                        if (c4 > '#') {
                        }
                        if (c4 > 31) {
                            if (c4 != '\\') {
                            }
                        }
                        z2 = true;
                        if (!z2) {
                            i10++;
                            byte[] bArr2 = specicalFlags_doubleQuotes;
                            if (c4 < bArr2.length && bArr2[c4] == 4) {
                                i7 += 4;
                            }
                        } else {
                            i11++;
                            c2 = '/';
                        }
                    }
                    z2 = false;
                    if (!z2) {
                    }
                }
                c3 = c4;
                i8 = i11;
                i9 = i8;
                i11++;
                c2 = '/';
            }
            if (i10 > 0) {
                int i12 = i7 + i10;
                if (i12 > this.buf.length) {
                    expandCapacity(i12);
                }
                this.count = i12;
                if (i10 == 1) {
                    if (c3 == 8232) {
                        int i13 = i9 + 1;
                        char[] cArr2 = this.buf;
                        System.arraycopy(cArr2, i13, cArr2, i9 + 6, (i6 - i9) - 1);
                        char[] cArr3 = this.buf;
                        cArr3[i9] = '\\';
                        cArr3[i13] = ULocale.UNICODE_LOCALE_EXTENSION;
                        int i14 = i13 + 1;
                        cArr3[i14] = '2';
                        int i15 = i14 + 1;
                        cArr3[i15] = '0';
                        int i16 = i15 + 1;
                        cArr3[i16] = '2';
                        cArr3[i16 + 1] = '8';
                    } else {
                        byte[] bArr3 = specicalFlags_doubleQuotes;
                        if (c3 >= bArr3.length || bArr3[c3] != 4) {
                            int i17 = i9 + 1;
                            char[] cArr4 = this.buf;
                            System.arraycopy(cArr4, i17, cArr4, i9 + 2, (i6 - i9) - 1);
                            char[] cArr5 = this.buf;
                            cArr5[i9] = '\\';
                            cArr5[i17] = replaceChars[c3];
                        } else {
                            int i18 = i9 + 1;
                            char[] cArr6 = this.buf;
                            System.arraycopy(cArr6, i18, cArr6, i9 + 6, (i6 - i9) - 1);
                            char[] cArr7 = this.buf;
                            cArr7[i9] = '\\';
                            int i19 = i18 + 1;
                            cArr7[i18] = ULocale.UNICODE_LOCALE_EXTENSION;
                            int i20 = i19 + 1;
                            char[] cArr8 = DIGITS;
                            cArr7[i19] = cArr8[(c3 >>> '\f') & 15];
                            int i21 = i20 + 1;
                            cArr7[i20] = cArr8[(c3 >>> '\b') & 15];
                            cArr7[i21] = cArr8[(c3 >>> 4) & 15];
                            cArr7[i21 + 1] = cArr8[c3 & 15];
                        }
                    }
                } else if (i10 > 1) {
                    for (int i22 = i8 - i5; i22 < str.length(); i22++) {
                        char charAt2 = str.charAt(i22);
                        byte[] bArr4 = specicalFlags_doubleQuotes;
                        if (charAt2 >= bArr4.length || bArr4[charAt2] == 0) {
                            if (charAt2 != '/' || (this.features & SerializerFeature.WriteSlashAsSpecial.mask) == 0) {
                                if (charAt2 == 8232) {
                                    char[] cArr9 = this.buf;
                                    int i23 = i8 + 1;
                                    cArr9[i8] = '\\';
                                    int i24 = i23 + 1;
                                    cArr9[i23] = ULocale.UNICODE_LOCALE_EXTENSION;
                                    int i25 = i24 + 1;
                                    char[] cArr10 = DIGITS;
                                    cArr9[i24] = cArr10[(charAt2 >>> '\f') & 15];
                                    int i26 = i25 + 1;
                                    cArr9[i25] = cArr10[(charAt2 >>> '\b') & 15];
                                    int i27 = i26 + 1;
                                    cArr9[i26] = cArr10[(charAt2 >>> 4) & 15];
                                    i = i27 + 1;
                                    cArr9[i27] = cArr10[charAt2 & 15];
                                    i8 = i;
                                } else {
                                    this.buf[i8] = charAt2;
                                    i8++;
                                }
                            }
                        }
                        char[] cArr11 = this.buf;
                        int i28 = i8 + 1;
                        cArr11[i8] = '\\';
                        if (specicalFlags_doubleQuotes[charAt2] == 4) {
                            int i29 = i28 + 1;
                            cArr11[i28] = ULocale.UNICODE_LOCALE_EXTENSION;
                            int i30 = i29 + 1;
                            char[] cArr12 = DIGITS;
                            cArr11[i29] = cArr12[(charAt2 >>> '\f') & 15];
                            int i31 = i30 + 1;
                            cArr11[i30] = cArr12[(charAt2 >>> '\b') & 15];
                            int i32 = i31 + 1;
                            cArr11[i31] = cArr12[(charAt2 >>> 4) & 15];
                            i = i32 + 1;
                            cArr11[i32] = cArr12[charAt2 & 15];
                        } else {
                            i = i28 + 1;
                            cArr11[i28] = replaceChars[charAt2];
                        }
                        i8 = i;
                    }
                }
            }
        }
        if (c != 0) {
            char[] cArr13 = this.buf;
            int i33 = this.count;
            cArr13[i33 - 2] = '\"';
            cArr13[i33 - 1] = c;
            return;
        }
        this.buf[this.count - 1] = '\"';
    }

    public void write(boolean z) {
        write(z ? "true" : "false");
    }

    public void writeString(String str) {
        if ((this.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
            writeStringWithSingleQuote(str);
        } else {
            writeStringWithDoubleQuote(str, 0, true);
        }
    }

    /* access modifiers changed from: protected */
    public void writeStringWithSingleQuote(String str) {
        int i = 0;
        if (str == null) {
            int i2 = this.count + 4;
            if (i2 > this.buf.length) {
                expandCapacity(i2);
            }
            "null".getChars(0, 4, this.buf, this.count);
            this.count = i2;
            return;
        }
        int length = str.length();
        int i3 = this.count + length + 2;
        if (i3 > this.buf.length) {
            if (this.writer != null) {
                write(39);
                while (i < str.length()) {
                    char charAt = str.charAt(i);
                    if (charAt <= '\r' || charAt == '\\' || charAt == '\'' || (charAt == '/' && (this.features & SerializerFeature.WriteSlashAsSpecial.mask) != 0)) {
                        write(92);
                        write(replaceChars[charAt]);
                    } else {
                        write(charAt);
                    }
                    i++;
                }
                write(39);
                return;
            }
            expandCapacity(i3);
        }
        int i4 = this.count;
        int i5 = i4 + 1;
        int i6 = i5 + length;
        char[] cArr = this.buf;
        cArr[i4] = '\'';
        str.getChars(0, length, cArr, i5);
        this.count = i3;
        int i7 = -1;
        char c = 0;
        for (int i8 = i5; i8 < i6; i8++) {
            char c2 = this.buf[i8];
            if (c2 <= '\r' || c2 == '\\' || c2 == '\'' || (c2 == '/' && (this.features & SerializerFeature.WriteSlashAsSpecial.mask) != 0)) {
                i++;
                i7 = i8;
                c = c2;
            }
        }
        int i9 = i3 + i;
        if (i9 > this.buf.length) {
            expandCapacity(i9);
        }
        this.count = i9;
        if (i == 1) {
            char[] cArr2 = this.buf;
            int i10 = i7 + 1;
            System.arraycopy(cArr2, i10, cArr2, i7 + 2, (i6 - i7) - 1);
            char[] cArr3 = this.buf;
            cArr3[i7] = '\\';
            cArr3[i10] = replaceChars[c];
        } else if (i > 1) {
            char[] cArr4 = this.buf;
            int i11 = i7 + 1;
            System.arraycopy(cArr4, i11, cArr4, i7 + 2, (i6 - i7) - 1);
            char[] cArr5 = this.buf;
            cArr5[i7] = '\\';
            cArr5[i11] = replaceChars[c];
            int i12 = i6 + 1;
            for (int i13 = i11 - 2; i13 >= i5; i13--) {
                char c3 = this.buf[i13];
                if (c3 <= '\r' || c3 == '\\' || c3 == '\'' || (c3 == '/' && (this.features & SerializerFeature.WriteSlashAsSpecial.mask) != 0)) {
                    char[] cArr6 = this.buf;
                    int i14 = i13 + 1;
                    System.arraycopy(cArr6, i14, cArr6, i13 + 2, (i12 - i13) - 1);
                    char[] cArr7 = this.buf;
                    cArr7[i13] = '\\';
                    cArr7[i14] = replaceChars[c3];
                    i12++;
                }
            }
        }
        this.buf[this.count - 1] = '\'';
    }

    public void writeFieldName(String str, boolean z) {
        if ((this.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
            if ((this.features & SerializerFeature.QuoteFieldNames.mask) != 0) {
                writeStringWithSingleQuote(str);
                write(58);
                return;
            }
            writeKeyWithSingleQuoteIfHasSpecial(str);
        } else if ((this.features & SerializerFeature.QuoteFieldNames.mask) != 0) {
            writeStringWithDoubleQuote(str, ':', z);
        } else {
            writeKeyWithDoubleQuoteIfHasSpecial(str);
        }
    }

    private void writeKeyWithDoubleQuoteIfHasSpecial(String str) {
        int length = str.length();
        boolean z = true;
        int i = this.count + length + 1;
        if (i > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(i);
            } else if (length == 0) {
                write(34);
                write(34);
                write(58);
                return;
            } else {
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        z = false;
                        break;
                    }
                    char charAt = str.charAt(i2);
                    byte[] bArr = specicalFlags_doubleQuotes;
                    if (charAt < bArr.length && bArr[charAt] != 0) {
                        break;
                    }
                    i2++;
                }
                if (z) {
                    write(34);
                }
                for (int i3 = 0; i3 < length; i3++) {
                    char charAt2 = str.charAt(i3);
                    byte[] bArr2 = specicalFlags_doubleQuotes;
                    if (charAt2 >= bArr2.length || bArr2[charAt2] == 0) {
                        write(charAt2);
                    } else {
                        write(92);
                        write(replaceChars[charAt2]);
                    }
                }
                if (z) {
                    write(34);
                }
                write(58);
                return;
            }
        }
        if (length == 0) {
            int i4 = this.count;
            if (i4 + 3 > this.buf.length) {
                expandCapacity(i4 + 3);
            }
            char[] cArr = this.buf;
            int i5 = this.count;
            this.count = i5 + 1;
            cArr[i5] = '\"';
            int i6 = this.count;
            this.count = i6 + 1;
            cArr[i6] = '\"';
            int i7 = this.count;
            this.count = i7 + 1;
            cArr[i7] = ':';
            return;
        }
        int i8 = this.count;
        int i9 = i8 + length;
        str.getChars(0, length, this.buf, i8);
        this.count = i;
        int i10 = i8;
        boolean z2 = false;
        while (i10 < i9) {
            char[] cArr2 = this.buf;
            char c = cArr2[i10];
            byte[] bArr3 = specicalFlags_doubleQuotes;
            if (c < bArr3.length && bArr3[c] != 0) {
                if (!z2) {
                    i += 3;
                    if (i > cArr2.length) {
                        expandCapacity(i);
                    }
                    this.count = i;
                    char[] cArr3 = this.buf;
                    int i11 = i10 + 1;
                    System.arraycopy(cArr3, i11, cArr3, i10 + 3, (i9 - i10) - 1);
                    char[] cArr4 = this.buf;
                    System.arraycopy(cArr4, 0, cArr4, 1, i10);
                    char[] cArr5 = this.buf;
                    cArr5[i8] = '\"';
                    cArr5[i11] = '\\';
                    int i12 = i11 + 1;
                    cArr5[i12] = replaceChars[c];
                    i9 += 2;
                    cArr5[this.count - 2] = '\"';
                    i10 = i12;
                    z2 = true;
                } else {
                    i++;
                    if (i > cArr2.length) {
                        expandCapacity(i);
                    }
                    this.count = i;
                    char[] cArr6 = this.buf;
                    int i13 = i10 + 1;
                    System.arraycopy(cArr6, i13, cArr6, i10 + 2, i9 - i10);
                    char[] cArr7 = this.buf;
                    cArr7[i10] = '\\';
                    cArr7[i13] = replaceChars[c];
                    i9++;
                    i10 = i13;
                }
            }
            i10++;
        }
        this.buf[this.count - 1] = ':';
    }

    private void writeKeyWithSingleQuoteIfHasSpecial(String str) {
        int length = str.length();
        boolean z = true;
        int i = this.count + length + 1;
        if (i > this.buf.length) {
            if (this.writer == null) {
                expandCapacity(i);
            } else if (length == 0) {
                write(39);
                write(39);
                write(58);
                return;
            } else {
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        z = false;
                        break;
                    }
                    char charAt = str.charAt(i2);
                    byte[] bArr = specicalFlags_singleQuotes;
                    if (charAt < bArr.length && bArr[charAt] != 0) {
                        break;
                    }
                    i2++;
                }
                if (z) {
                    write(39);
                }
                for (int i3 = 0; i3 < length; i3++) {
                    char charAt2 = str.charAt(i3);
                    byte[] bArr2 = specicalFlags_singleQuotes;
                    if (charAt2 >= bArr2.length || bArr2[charAt2] == 0) {
                        write(charAt2);
                    } else {
                        write(92);
                        write(replaceChars[charAt2]);
                    }
                }
                if (z) {
                    write(39);
                }
                write(58);
                return;
            }
        }
        if (length == 0) {
            int i4 = this.count;
            if (i4 + 3 > this.buf.length) {
                expandCapacity(i4 + 3);
            }
            char[] cArr = this.buf;
            int i5 = this.count;
            this.count = i5 + 1;
            cArr[i5] = '\'';
            int i6 = this.count;
            this.count = i6 + 1;
            cArr[i6] = '\'';
            int i7 = this.count;
            this.count = i7 + 1;
            cArr[i7] = ':';
            return;
        }
        int i8 = this.count;
        int i9 = i8 + length;
        str.getChars(0, length, this.buf, i8);
        this.count = i;
        int i10 = i8;
        boolean z2 = false;
        while (i10 < i9) {
            char[] cArr2 = this.buf;
            char c = cArr2[i10];
            byte[] bArr3 = specicalFlags_singleQuotes;
            if (c < bArr3.length && bArr3[c] != 0) {
                if (!z2) {
                    i += 3;
                    if (i > cArr2.length) {
                        expandCapacity(i);
                    }
                    this.count = i;
                    char[] cArr3 = this.buf;
                    int i11 = i10 + 1;
                    System.arraycopy(cArr3, i11, cArr3, i10 + 3, (i9 - i10) - 1);
                    char[] cArr4 = this.buf;
                    System.arraycopy(cArr4, 0, cArr4, 1, i10);
                    char[] cArr5 = this.buf;
                    cArr5[i8] = '\'';
                    cArr5[i11] = '\\';
                    int i12 = i11 + 1;
                    cArr5[i12] = replaceChars[c];
                    i9 += 2;
                    cArr5[this.count - 2] = '\'';
                    i10 = i12;
                    z2 = true;
                } else {
                    i++;
                    if (i > cArr2.length) {
                        expandCapacity(i);
                    }
                    this.count = i;
                    char[] cArr6 = this.buf;
                    int i13 = i10 + 1;
                    System.arraycopy(cArr6, i13, cArr6, i10 + 2, i9 - i10);
                    char[] cArr7 = this.buf;
                    cArr7[i10] = '\\';
                    cArr7[i13] = replaceChars[c];
                    i9++;
                    i10 = i13;
                }
            }
            i10++;
        }
        this.buf[i - 1] = ':';
    }

    @Override // java.io.Writer, java.io.Flushable
    public void flush() {
        Writer writer2 = this.writer;
        if (writer2 != null) {
            try {
                writer2.write(this.buf, 0, this.count);
                this.writer.flush();
                this.count = 0;
            } catch (IOException e) {
                throw new JSONException(e.getMessage(), e);
            }
        }
    }

    protected static void getChars(long j, int i, char[] cArr) {
        char c;
        if (j < 0) {
            c = '-';
            j = -j;
        } else {
            c = 0;
        }
        while (j > 2147483647L) {
            long j2 = j / 100;
            int i2 = (int) (j - (((j2 << 6) + (j2 << 5)) + (j2 << 2)));
            int i3 = i - 1;
            cArr[i3] = DigitOnes[i2];
            i = i3 - 1;
            cArr[i] = DigitTens[i2];
            j = j2;
        }
        int i4 = (int) j;
        while (i4 >= 65536) {
            int i5 = i4 / 100;
            int i6 = i4 - (((i5 << 6) + (i5 << 5)) + (i5 << 2));
            int i7 = i - 1;
            cArr[i7] = DigitOnes[i6];
            i = i7 - 1;
            cArr[i] = DigitTens[i6];
            i4 = i5;
        }
        while (true) {
            int i8 = (52429 * i4) >>> 19;
            i--;
            cArr[i] = digits[i4 - ((i8 << 3) + (i8 << 1))];
            if (i8 == 0) {
                break;
            }
            i4 = i8;
        }
        if (c != 0) {
            cArr[i - 1] = c;
        }
    }
}
