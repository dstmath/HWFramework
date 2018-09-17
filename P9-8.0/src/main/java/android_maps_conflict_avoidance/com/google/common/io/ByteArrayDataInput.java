package android_maps_conflict_avoidance.com.google.common.io;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.UTFDataFormatException;

public class ByteArrayDataInput implements DataInput {
    private byte[] mBytes;
    private int mLength = this.mBytes.length;
    private int mPos = 0;
    private char[] mUtfCharBuf = new char[128];

    public ByteArrayDataInput(byte[] bytes) {
        this.mBytes = bytes;
    }

    public boolean readBoolean() throws IOException {
        try {
            byte[] bArr = this.mBytes;
            int i = this.mPos;
            this.mPos = i + 1;
            return bArr[i] != (byte) 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EOFException();
        }
    }

    public byte readByte() throws IOException {
        try {
            byte[] bArr = this.mBytes;
            int i = this.mPos;
            this.mPos = i + 1;
            return bArr[i];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EOFException();
        }
    }

    public char readChar() throws IOException {
        try {
            byte[] bArr = this.mBytes;
            int i = this.mPos;
            this.mPos = i + 1;
            int a = bArr[i];
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            return (char) ((a << 8) | (bArr[i] & 255));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EOFException();
        }
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public void readFully(byte[] buffer) throws IOException {
        readFully(buffer, 0, buffer.length);
    }

    public void readFully(byte[] buffer, int offset, int length) throws IOException {
        if (length == 0) {
            return;
        }
        if (offset + length > buffer.length) {
            throw new IndexOutOfBoundsException();
        } else if (length <= this.mLength - this.mPos) {
            System.arraycopy(this.mBytes, this.mPos, buffer, offset, length);
            this.mPos += length;
        } else {
            this.mPos = this.mLength;
            throw new EOFException();
        }
    }

    public int readInt() throws IOException {
        try {
            byte[] bArr = this.mBytes;
            int i = this.mPos;
            this.mPos = i + 1;
            int a = bArr[i] & 255;
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            int b = bArr[i] & 255;
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            int c = bArr[i] & 255;
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            return (((a << 24) | (b << 16)) | (c << 8)) | (bArr[i] & 255);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EOFException();
        }
    }

    public String readLine() {
        if (this.mPos >= this.mLength) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        do {
            byte[] bArr = this.mBytes;
            int i = this.mPos;
            this.mPos = i + 1;
            char c = (char) bArr[i];
            if (c == 10) {
                return result.toString();
            }
            if (c != 13) {
                result.append(c);
            } else {
                if (this.mPos < this.mLength && this.mBytes[this.mPos] == (byte) 10) {
                    this.mPos++;
                }
                return result.toString();
            }
        } while (this.mPos != this.mLength);
        return result.toString();
    }

    public long readLong() throws IOException {
        try {
            byte[] bArr = this.mBytes;
            int i = this.mPos;
            this.mPos = i + 1;
            long a = (long) (bArr[i] & 255);
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            long b = (long) (bArr[i] & 255);
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            long c = (long) (bArr[i] & 255);
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            long d = (long) (bArr[i] & 255);
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            long e = (long) (bArr[i] & 255);
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            long f = (long) (bArr[i] & 255);
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            long g = (long) (bArr[i] & 255);
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            return (((((((a << 56) | (b << 48)) | (c << 40)) | (d << 32)) | (e << 24)) | (f << 16)) | (g << 8)) | ((long) (bArr[i] & 255));
        } catch (ArrayIndexOutOfBoundsException e2) {
            throw new EOFException();
        }
    }

    public short readShort() throws IOException {
        try {
            byte[] bArr = this.mBytes;
            int i = this.mPos;
            this.mPos = i + 1;
            int a = bArr[i];
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            return (short) ((a << 8) | (bArr[i] & 255));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EOFException();
        }
    }

    public String readUTF() throws IOException {
        int length = readUnsignedShort();
        if (length == 0) {
            return "";
        }
        if (length <= this.mLength - this.mPos) {
            if (length > this.mUtfCharBuf.length) {
                this.mUtfCharBuf = new char[length];
            }
            String result = convertUTF8WithBuf(this.mBytes, this.mUtfCharBuf, this.mPos, length);
            this.mPos += length;
            return result;
        }
        this.mPos = this.mLength;
        throw new EOFException();
    }

    public int readUnsignedByte() throws IOException {
        try {
            byte[] bArr = this.mBytes;
            int i = this.mPos;
            this.mPos = i + 1;
            return bArr[i] & 255;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EOFException();
        }
    }

    public int readUnsignedShort() throws IOException {
        try {
            byte[] bArr = this.mBytes;
            int i = this.mPos;
            this.mPos = i + 1;
            int a = bArr[i] & 255;
            bArr = this.mBytes;
            i = this.mPos;
            this.mPos = i + 1;
            return (a << 8) | (bArr[i] & 255);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EOFException();
        }
    }

    public int skipBytes(int length) {
        if (length > this.mLength - this.mPos) {
            length = this.mLength - this.mPos;
        }
        this.mPos += length;
        return length;
    }

    public static String convertUTF8WithBuf(byte[] buf, char[] out, int offset, int utfSize) throws UTFDataFormatException {
        int s = 0;
        int count = 0;
        while (count < utfSize) {
            int count2 = count + 1;
            char c = (char) buf[offset + count];
            out[s] = (char) c;
            if (c >= 128) {
                int a = out[s];
                int b;
                int s2;
                if ((a & 224) != 192) {
                    if ((a & 240) != 224) {
                        throw new UTFDataFormatException("Input at " + (count2 - 1) + " does not match UTF8 " + "Specification");
                    } else if (count2 + 1 < utfSize) {
                        count = count2 + 1;
                        b = buf[offset + count2];
                        count2 = count + 1;
                        int c2 = buf[offset + count];
                        if ((b & 192) == 128 && (c2 & 192) == 128) {
                            s2 = s + 1;
                            out[s] = (char) ((char) ((((a & 15) << 12) | ((b & 63) << 6)) | (c2 & 63)));
                            s = s2;
                            count = count2;
                        } else {
                            throw new UTFDataFormatException("Second or third byte at " + (count2 - 2) + " does not match UTF8 Specification");
                        }
                    } else {
                        throw new UTFDataFormatException("Third byte at " + (count2 + 1) + " does not match UTF8 Specification");
                    }
                } else if (count2 < utfSize) {
                    count = count2 + 1;
                    b = buf[offset + count2];
                    if ((b & 192) == 128) {
                        s2 = s + 1;
                        out[s] = (char) ((char) (((a & 31) << 6) | (b & 63)));
                        s = s2;
                    } else {
                        throw new UTFDataFormatException("Second byte at " + (count - 1) + " does not match UTF8 Specification");
                    }
                } else {
                    throw new UTFDataFormatException("Second byte at " + count2 + " does not match " + "UTF8 Specification");
                }
            }
            s++;
            count = count2;
        }
        return new String(out, 0, s);
    }
}
