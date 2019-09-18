package com.android.org.bouncycastle.util.encoders;

import java.io.IOException;
import java.io.OutputStream;

public class Base64Encoder implements Encoder {
    protected final byte[] decodingTable = new byte[128];
    protected final byte[] encodingTable = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    protected byte padding = 61;

    /* access modifiers changed from: protected */
    public void initialiseDecodingTable() {
        for (int i = 0; i < this.decodingTable.length; i++) {
            this.decodingTable[i] = -1;
        }
        for (int i2 = 0; i2 < this.encodingTable.length; i2++) {
            this.decodingTable[this.encodingTable[i2]] = (byte) i2;
        }
    }

    public Base64Encoder() {
        initialiseDecodingTable();
    }

    public int encode(byte[] data, int off, int length, OutputStream out) throws IOException {
        int modulus = length % 3;
        int dataLength = length - modulus;
        for (int i = off; i < off + dataLength; i += 3) {
            int a1 = data[i] & 255;
            int a2 = data[i + 1] & 255;
            int a3 = data[i + 2] & 255;
            out.write(this.encodingTable[(a1 >>> 2) & 63]);
            out.write(this.encodingTable[((a1 << 4) | (a2 >>> 4)) & 63]);
            out.write(this.encodingTable[((a2 << 2) | (a3 >>> 6)) & 63]);
            out.write(this.encodingTable[a3 & 63]);
        }
        switch (modulus) {
            case 1:
                int d1 = data[off + dataLength] & 255;
                out.write(this.encodingTable[(d1 >>> 2) & 63]);
                out.write(this.encodingTable[(d1 << 4) & 63]);
                out.write(this.padding);
                out.write(this.padding);
                break;
            case 2:
                int d12 = data[off + dataLength] & 255;
                int d2 = data[off + dataLength + 1] & 255;
                out.write(this.encodingTable[(d12 >>> 2) & 63]);
                out.write(this.encodingTable[((d12 << 4) | (d2 >>> 4)) & 63]);
                out.write(this.encodingTable[(d2 << 2) & 63]);
                out.write(this.padding);
                break;
        }
        int i2 = 4;
        int i3 = (dataLength / 3) * 4;
        if (modulus == 0) {
            i2 = 0;
        }
        return i3 + i2;
    }

    private boolean ignore(char c) {
        return c == 10 || c == 13 || c == 9 || c == ' ';
    }

    public int decode(byte[] data, int off, int length, OutputStream out) throws IOException {
        int outLen = 0;
        int end = off + length;
        while (end > off && ignore((char) data[end - 1])) {
            end--;
        }
        int finish = end - 4;
        int i = nextI(data, off, finish);
        while (i < finish) {
            int i2 = i + 1;
            byte b1 = this.decodingTable[data[i]];
            int i3 = nextI(data, i2, finish);
            int i4 = i3 + 1;
            byte i5 = this.decodingTable[data[i3]];
            int i6 = nextI(data, i4, finish);
            int i7 = i6 + 1;
            byte i8 = this.decodingTable[data[i6]];
            int i9 = nextI(data, i7, finish);
            int i10 = i9 + 1;
            byte i11 = this.decodingTable[data[i9]];
            if ((b1 | i5 | i8 | i11) >= 0) {
                out.write((b1 << 2) | (i5 >> 4));
                out.write((i5 << 4) | (i8 >> 2));
                out.write((i8 << 6) | i11);
                outLen += 3;
                i = nextI(data, i10, finish);
            } else {
                throw new IOException("invalid characters encountered in base64 data");
            }
        }
        return outLen + decodeLastBlock(out, (char) data[end - 4], (char) data[end - 3], (char) data[end - 2], (char) data[end - 1]);
    }

    private int nextI(byte[] data, int i, int finish) {
        while (i < finish && ignore((char) data[i])) {
            i++;
        }
        return i;
    }

    public int decode(String data, OutputStream out) throws IOException {
        int length = 0;
        int end = data.length();
        while (end > 0 && ignore(data.charAt(end - 1))) {
            end--;
        }
        int finish = end - 4;
        int i = nextI(data, 0, finish);
        while (i < finish) {
            int i2 = i + 1;
            byte b1 = this.decodingTable[data.charAt(i)];
            int i3 = nextI(data, i2, finish);
            int i4 = i3 + 1;
            byte i5 = this.decodingTable[data.charAt(i3)];
            int i6 = nextI(data, i4, finish);
            int i7 = i6 + 1;
            byte i8 = this.decodingTable[data.charAt(i6)];
            int i9 = nextI(data, i7, finish);
            int i10 = i9 + 1;
            byte i11 = this.decodingTable[data.charAt(i9)];
            if ((b1 | i5 | i8 | i11) >= 0) {
                out.write((b1 << 2) | (i5 >> 4));
                out.write((i5 << 4) | (i8 >> 2));
                out.write((i8 << 6) | i11);
                length += 3;
                i = nextI(data, i10, finish);
            } else {
                throw new IOException("invalid characters encountered in base64 data");
            }
        }
        return length + decodeLastBlock(out, data.charAt(end - 4), data.charAt(end - 3), data.charAt(end - 2), data.charAt(end - 1));
    }

    private int decodeLastBlock(OutputStream out, char c1, char c2, char c3, char c4) throws IOException {
        if (c3 == this.padding) {
            if (c4 == this.padding) {
                byte b1 = this.decodingTable[c1];
                byte b2 = this.decodingTable[c2];
                if ((b1 | b2) >= 0) {
                    out.write((b1 << 2) | (b2 >> 4));
                    return 1;
                }
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            throw new IOException("invalid characters encountered at end of base64 data");
        } else if (c4 == this.padding) {
            byte b12 = this.decodingTable[c1];
            byte b22 = this.decodingTable[c2];
            byte b3 = this.decodingTable[c3];
            if ((b12 | b22 | b3) >= 0) {
                out.write((b12 << 2) | (b22 >> 4));
                out.write((b22 << 4) | (b3 >> 2));
                return 2;
            }
            throw new IOException("invalid characters encountered at end of base64 data");
        } else {
            byte b13 = this.decodingTable[c1];
            byte b23 = this.decodingTable[c2];
            byte b32 = this.decodingTable[c3];
            byte b4 = this.decodingTable[c4];
            if ((b13 | b23 | b32 | b4) >= 0) {
                out.write((b13 << 2) | (b23 >> 4));
                out.write((b23 << 4) | (b32 >> 2));
                out.write((b32 << 6) | b4);
                return 3;
            }
            throw new IOException("invalid characters encountered at end of base64 data");
        }
    }

    private int nextI(String data, int i, int finish) {
        while (i < finish && ignore(data.charAt(i))) {
            i++;
        }
        return i;
    }
}
