package ohos.com.sun.org.apache.xerces.internal.impl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.MessageFormatter;
import ohos.com.sun.xml.internal.stream.util.ThreadLocalBufferAllocator;

public class UTF8Reader extends Reader {
    private static final boolean DEBUG_READ = false;
    public static final int DEFAULT_BUFFER_SIZE = 2048;
    protected byte[] fBuffer;
    private MessageFormatter fFormatter;
    protected InputStream fInputStream;
    private Locale fLocale;
    protected int fOffset;
    private int fSurrogate;

    @Override // java.io.Reader
    public boolean markSupported() {
        return false;
    }

    @Override // java.io.Reader
    public boolean ready() throws IOException {
        return false;
    }

    public UTF8Reader(InputStream inputStream) {
        this(inputStream, 2048, new XMLMessageFormatter(), Locale.getDefault());
    }

    public UTF8Reader(InputStream inputStream, MessageFormatter messageFormatter, Locale locale) {
        this(inputStream, 2048, messageFormatter, locale);
    }

    public UTF8Reader(InputStream inputStream, int i, MessageFormatter messageFormatter, Locale locale) {
        this.fSurrogate = -1;
        this.fFormatter = null;
        this.fLocale = null;
        this.fInputStream = inputStream;
        this.fBuffer = ThreadLocalBufferAllocator.getBufferAllocator().getByteBuffer(i);
        if (this.fBuffer == null) {
            this.fBuffer = new byte[i];
        }
        this.fFormatter = messageFormatter;
        this.fLocale = locale;
    }

    @Override // java.io.Reader
    public int read() throws IOException {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8 = this.fSurrogate;
        if (i8 == -1) {
            int i9 = 0;
            if (this.fOffset == 0) {
                i = this.fInputStream.read();
            } else {
                i = this.fBuffer[0] & 255;
                i9 = 1;
            }
            if (i == -1) {
                return -1;
            }
            if (i < 128) {
                i7 = (char) i;
            } else {
                if ((i & 224) == 192 && (i & 30) != 0) {
                    int read = i9 == this.fOffset ? this.fInputStream.read() : this.fBuffer[i9] & 255;
                    if (read == -1) {
                        expectedByte(2, 2);
                    }
                    if ((read & 192) != 128) {
                        invalidByte(2, 2, read);
                    }
                    i5 = (i << 6) & 1984;
                    i6 = read & 63;
                } else if ((i & 240) == 224) {
                    if (i9 == this.fOffset) {
                        i4 = this.fInputStream.read();
                    } else {
                        i4 = this.fBuffer[i9] & 255;
                        i9++;
                    }
                    if (i4 == -1) {
                        expectedByte(2, 3);
                    }
                    if ((i4 & 192) != 128 || ((i == 237 && i4 >= 160) || ((i & 15) == 0 && (i4 & 32) == 0))) {
                        invalidByte(2, 3, i4);
                    }
                    int read2 = i9 == this.fOffset ? this.fInputStream.read() : this.fBuffer[i9] & 255;
                    if (read2 == -1) {
                        expectedByte(3, 3);
                    }
                    if ((read2 & 192) != 128) {
                        invalidByte(3, 3, read2);
                    }
                    i5 = ((i << 12) & 61440) | ((i4 << 6) & 4032);
                    i6 = read2 & 63;
                } else if ((i & 248) == 240) {
                    if (i9 == this.fOffset) {
                        i2 = this.fInputStream.read();
                    } else {
                        i2 = this.fBuffer[i9] & 255;
                        i9++;
                    }
                    if (i2 == -1) {
                        expectedByte(2, 4);
                    }
                    if ((i2 & 192) != 128 || ((i2 & 48) == 0 && (i & 7) == 0)) {
                        invalidByte(2, 3, i2);
                    }
                    if (i9 == this.fOffset) {
                        i3 = this.fInputStream.read();
                    } else {
                        i3 = this.fBuffer[i9] & 255;
                        i9++;
                    }
                    if (i3 == -1) {
                        expectedByte(3, 4);
                    }
                    if ((i3 & 192) != 128) {
                        invalidByte(3, 3, i3);
                    }
                    int read3 = i9 == this.fOffset ? this.fInputStream.read() : this.fBuffer[i9] & 255;
                    if (read3 == -1) {
                        expectedByte(4, 4);
                    }
                    if ((read3 & 192) != 128) {
                        invalidByte(4, 4, read3);
                    }
                    int i10 = ((i << 2) & 28) | ((i2 >> 4) & 3);
                    if (i10 > 16) {
                        invalidSurrogate(i10);
                    }
                    int i11 = ((i2 << 2) & 60) | (((i10 - 1) << 6) & 960) | 55296 | ((i3 >> 4) & 3);
                    this.fSurrogate = 56320 | ((i3 << 6) & 960) | (read3 & 63);
                    return i11;
                } else {
                    invalidByte(1, 1, i);
                    return i8;
                }
                i7 = i5 | i6;
            }
            return i7;
        }
        this.fSurrogate = -1;
        return i8;
    }

    @Override // java.io.Reader
    public int read(char[] cArr, int i, int i2) throws IOException {
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        int i12 = this.fSurrogate;
        int i13 = -1;
        if (i12 != -1) {
            i3 = i + 1;
            cArr[i3] = (char) i12;
            this.fSurrogate = -1;
            i4 = i2 - 1;
        } else {
            i4 = i2;
            i3 = i;
        }
        int i14 = this.fOffset;
        if (i14 == 0) {
            byte[] bArr = this.fBuffer;
            if (i4 > bArr.length) {
                i4 = bArr.length;
            }
            int read = this.fInputStream.read(this.fBuffer, 0, i4);
            if (read == -1) {
                return -1;
            }
            i14 = (i3 - i) + read;
        } else {
            this.fOffset = 0;
        }
        int i15 = 0;
        while (i15 < i14) {
            byte b = this.fBuffer[i15];
            if (b < 0) {
                break;
            }
            cArr[i3] = (char) b;
            i15++;
            i3++;
        }
        int i16 = i3;
        int i17 = i14;
        while (i15 < i14) {
            byte[] bArr2 = this.fBuffer;
            byte b2 = bArr2[i15];
            if (b2 >= 0) {
                i10 = i16 + 1;
                cArr[i16] = (char) b2;
            } else {
                int i18 = b2 & 255;
                if ((i18 & 224) == 192 && (i18 & 30) != 0) {
                    i15++;
                    if (i15 < i14) {
                        i11 = bArr2[i15] & 255;
                    } else {
                        i11 = this.fInputStream.read();
                        if (i11 == i13) {
                            if (i16 > i) {
                                this.fBuffer[0] = (byte) i18;
                                this.fOffset = 1;
                            } else {
                                expectedByte(2, 2);
                            }
                        }
                        i17++;
                    }
                    if ((i11 & 192) != 128) {
                        if (i16 > i) {
                            byte[] bArr3 = this.fBuffer;
                            bArr3[0] = (byte) i18;
                            bArr3[1] = (byte) i11;
                            this.fOffset = 2;
                        } else {
                            invalidByte(2, 2, i11);
                        }
                    }
                    cArr[i16] = (char) ((i11 & 63) | ((i18 << 6) & 1984));
                    i17 += i13;
                    i16++;
                    i15++;
                    i13 = -1;
                } else if ((i18 & 240) == 224) {
                    int i19 = i15 + 1;
                    if (i19 < i14) {
                        i8 = this.fBuffer[i19] & 255;
                    } else {
                        i8 = this.fInputStream.read();
                        if (i8 == i13) {
                            if (i16 > i) {
                                this.fBuffer[0] = (byte) i18;
                                this.fOffset = 1;
                            } else {
                                expectedByte(2, 3);
                            }
                        }
                        i17++;
                    }
                    if ((i8 & 192) != 128 || ((i18 == 237 && i8 >= 160) || ((i18 & 15) == 0 && (i8 & 32) == 0))) {
                        if (i16 > i) {
                            byte[] bArr4 = this.fBuffer;
                            bArr4[0] = (byte) i18;
                            bArr4[1] = (byte) i8;
                            this.fOffset = 2;
                        } else {
                            invalidByte(2, 3, i8);
                        }
                    }
                    i15 = i19 + 1;
                    if (i15 < i14) {
                        i9 = this.fBuffer[i15] & 255;
                    } else {
                        i9 = this.fInputStream.read();
                        if (i9 == i13) {
                            if (i16 > i) {
                                byte[] bArr5 = this.fBuffer;
                                bArr5[0] = (byte) i18;
                                bArr5[1] = (byte) i8;
                                this.fOffset = 2;
                            } else {
                                expectedByte(3, 3);
                            }
                        }
                        i17++;
                    }
                    if ((i9 & 192) != 128) {
                        if (i16 > i) {
                            byte[] bArr6 = this.fBuffer;
                            bArr6[0] = (byte) i18;
                            bArr6[1] = (byte) i8;
                            bArr6[2] = (byte) i9;
                            this.fOffset = 3;
                        } else {
                            invalidByte(3, 3, i9);
                        }
                    }
                    int i20 = ((i18 << 12) & 61440) | ((i8 << 6) & 4032) | (i9 & 63);
                    i10 = i16 + 1;
                    cArr[i16] = (char) i20;
                    i17 -= 2;
                } else if ((i18 & 248) == 240) {
                    int i21 = i15 + 1;
                    if (i21 < i14) {
                        i5 = this.fBuffer[i21] & 255;
                    } else {
                        i5 = this.fInputStream.read();
                        if (i5 == -1) {
                            if (i16 > i) {
                                this.fBuffer[0] = (byte) i18;
                                this.fOffset = 1;
                            } else {
                                expectedByte(2, 4);
                            }
                        }
                        i17++;
                    }
                    if ((i5 & 192) != 128 || ((i5 & 48) == 0 && (i18 & 7) == 0)) {
                        if (i16 > i) {
                            byte[] bArr7 = this.fBuffer;
                            bArr7[0] = (byte) i18;
                            bArr7[1] = (byte) i5;
                            this.fOffset = 2;
                        } else {
                            invalidByte(2, 4, i5);
                        }
                    }
                    int i22 = i21 + 1;
                    if (i22 < i14) {
                        i6 = this.fBuffer[i22] & 255;
                    } else {
                        i6 = this.fInputStream.read();
                        if (i6 == -1) {
                            if (i16 > i) {
                                byte[] bArr8 = this.fBuffer;
                                bArr8[0] = (byte) i18;
                                bArr8[1] = (byte) i5;
                                this.fOffset = 2;
                            } else {
                                expectedByte(3, 4);
                            }
                        }
                        i17++;
                    }
                    if ((i6 & 192) != 128) {
                        if (i16 > i) {
                            byte[] bArr9 = this.fBuffer;
                            bArr9[0] = (byte) i18;
                            bArr9[1] = (byte) i5;
                            bArr9[2] = (byte) i6;
                            this.fOffset = 3;
                        } else {
                            invalidByte(3, 4, i6);
                        }
                    }
                    i15 = i22 + 1;
                    if (i15 < i14) {
                        i7 = this.fBuffer[i15] & 255;
                    } else {
                        i7 = this.fInputStream.read();
                        if (i7 == -1) {
                            if (i16 > i) {
                                byte[] bArr10 = this.fBuffer;
                                bArr10[0] = (byte) i18;
                                bArr10[1] = (byte) i5;
                                bArr10[2] = (byte) i6;
                                this.fOffset = 3;
                            } else {
                                expectedByte(4, 4);
                            }
                        }
                        i17++;
                    }
                    if ((i7 & 192) != 128) {
                        if (i16 > i) {
                            byte[] bArr11 = this.fBuffer;
                            bArr11[0] = (byte) i18;
                            bArr11[1] = (byte) i5;
                            bArr11[2] = (byte) i6;
                            bArr11[3] = (byte) i7;
                            this.fOffset = 4;
                        } else {
                            invalidByte(4, 4, i6);
                        }
                    }
                    int i23 = i16 + 1;
                    if (i23 >= cArr.length) {
                        byte[] bArr12 = this.fBuffer;
                        bArr12[0] = (byte) i18;
                        bArr12[1] = (byte) i5;
                        bArr12[2] = (byte) i6;
                        bArr12[3] = (byte) i7;
                        this.fOffset = 4;
                    } else {
                        int i24 = ((i18 << 2) & 28) | ((i5 >> 4) & 3);
                        if (i24 > 16) {
                            invalidSurrogate(i24);
                        }
                        int i25 = i6 & 63;
                        cArr[i16] = (char) (((i5 & 15) << 2) | (((i24 - 1) << 6) & 960) | 55296 | (i25 >> 4));
                        i16 = i23 + 1;
                        cArr[i23] = (char) ((i7 & 63) | 56320 | ((i25 << 6) & 960));
                        i17 -= 2;
                        i15++;
                        i13 = -1;
                    }
                } else if (i16 > i) {
                    this.fBuffer[0] = (byte) i18;
                    this.fOffset = 1;
                } else {
                    invalidByte(1, 1, i18);
                    i15++;
                    i13 = -1;
                }
                return i16 - i;
            }
            i16 = i10;
            i15++;
            i13 = -1;
        }
        return i17;
    }

    @Override // java.io.Reader
    public long skip(long j) throws IOException {
        char[] cArr = new char[this.fBuffer.length];
        long j2 = j;
        do {
            int read = read(cArr, 0, ((long) cArr.length) < j2 ? cArr.length : (int) j2);
            if (read <= 0) {
                break;
            }
            j2 -= (long) read;
        } while (j2 > 0);
        return j - j2;
    }

    @Override // java.io.Reader
    public void mark(int i) throws IOException {
        throw new IOException(this.fFormatter.formatMessage(this.fLocale, "OperationNotSupported", new Object[]{"mark()", "UTF-8"}));
    }

    @Override // java.io.Reader
    public void reset() throws IOException {
        this.fOffset = 0;
        this.fSurrogate = -1;
    }

    @Override // java.io.Reader, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        ThreadLocalBufferAllocator.getBufferAllocator().returnByteBuffer(this.fBuffer);
        this.fBuffer = null;
        this.fInputStream.close();
    }

    private void expectedByte(int i, int i2) throws MalformedByteSequenceException {
        throw new MalformedByteSequenceException(this.fFormatter, this.fLocale, "http://www.w3.org/TR/1998/REC-xml-19980210", "ExpectedByte", new Object[]{Integer.toString(i), Integer.toString(i2)});
    }

    private void invalidByte(int i, int i2, int i3) throws MalformedByteSequenceException {
        throw new MalformedByteSequenceException(this.fFormatter, this.fLocale, "http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidByte", new Object[]{Integer.toString(i), Integer.toString(i2)});
    }

    private void invalidSurrogate(int i) throws MalformedByteSequenceException {
        throw new MalformedByteSequenceException(this.fFormatter, this.fLocale, "http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidHighSurrogate", new Object[]{Integer.toHexString(i)});
    }
}
