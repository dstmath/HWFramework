package org.bouncycastle.util.encoders;

import java.io.IOException;
import java.io.OutputStream;

public class Base64Encoder implements Encoder {
    protected final byte[] decodingTable = new byte[128];
    protected final byte[] encodingTable = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    protected byte padding = 61;

    public Base64Encoder() {
        initialiseDecodingTable();
    }

    private int decodeLastBlock(OutputStream outputStream, char c, char c2, char c3, char c4) throws IOException {
        if (c3 == this.padding) {
            if (c4 == this.padding) {
                byte b = this.decodingTable[c];
                byte b2 = this.decodingTable[c2];
                if ((b | b2) >= 0) {
                    outputStream.write((b << 2) | (b2 >> 4));
                    return 1;
                }
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            throw new IOException("invalid characters encountered at end of base64 data");
        } else if (c4 == this.padding) {
            byte b3 = this.decodingTable[c];
            byte b4 = this.decodingTable[c2];
            byte b5 = this.decodingTable[c3];
            if ((b3 | b4 | b5) >= 0) {
                outputStream.write((b3 << 2) | (b4 >> 4));
                outputStream.write((b4 << 4) | (b5 >> 2));
                return 2;
            }
            throw new IOException("invalid characters encountered at end of base64 data");
        } else {
            byte b6 = this.decodingTable[c];
            byte b7 = this.decodingTable[c2];
            byte b8 = this.decodingTable[c3];
            byte b9 = this.decodingTable[c4];
            if ((b6 | b7 | b8 | b9) >= 0) {
                outputStream.write((b6 << 2) | (b7 >> 4));
                outputStream.write((b7 << 4) | (b8 >> 2));
                outputStream.write((b8 << 6) | b9);
                return 3;
            }
            throw new IOException("invalid characters encountered at end of base64 data");
        }
    }

    private boolean ignore(char c) {
        return c == 10 || c == 13 || c == 9 || c == ' ';
    }

    private int nextI(String str, int i, int i2) {
        while (i < i2 && ignore(str.charAt(i))) {
            i++;
        }
        return i;
    }

    private int nextI(byte[] bArr, int i, int i2) {
        while (i < i2 && ignore((char) bArr[i])) {
            i++;
        }
        return i;
    }

    public int decode(String str, OutputStream outputStream) throws IOException {
        int length = str.length();
        while (length > 0 && ignore(str.charAt(length - 1))) {
            length--;
        }
        int i = 0;
        if (length == 0) {
            return 0;
        }
        int i2 = length;
        int i3 = 0;
        while (i2 > 0 && i3 != 4) {
            if (!ignore(str.charAt(i2 - 1))) {
                i3++;
            }
            i2--;
        }
        int nextI = nextI(str, 0, i2);
        while (nextI < i2) {
            int i4 = nextI + 1;
            byte b = this.decodingTable[str.charAt(nextI)];
            int nextI2 = nextI(str, i4, i2);
            int i5 = nextI2 + 1;
            byte b2 = this.decodingTable[str.charAt(nextI2)];
            int nextI3 = nextI(str, i5, i2);
            int i6 = nextI3 + 1;
            byte b3 = this.decodingTable[str.charAt(nextI3)];
            int nextI4 = nextI(str, i6, i2);
            int i7 = nextI4 + 1;
            byte b4 = this.decodingTable[str.charAt(nextI4)];
            if ((b | b2 | b3 | b4) >= 0) {
                outputStream.write((b << 2) | (b2 >> 4));
                outputStream.write((b2 << 4) | (b3 >> 2));
                outputStream.write((b3 << 6) | b4);
                i += 3;
                nextI = nextI(str, i7, i2);
            } else {
                throw new IOException("invalid characters encountered in base64 data");
            }
        }
        int nextI5 = nextI(str, nextI, length);
        int nextI6 = nextI(str, nextI5 + 1, length);
        int nextI7 = nextI(str, nextI6 + 1, length);
        return i + decodeLastBlock(outputStream, str.charAt(nextI5), str.charAt(nextI6), str.charAt(nextI7), str.charAt(nextI(str, nextI7 + 1, length)));
    }

    public int decode(byte[] bArr, int i, int i2, OutputStream outputStream) throws IOException {
        int i3 = i2 + i;
        while (i3 > i && ignore((char) bArr[i3 - 1])) {
            i3--;
        }
        int i4 = 0;
        if (i3 == 0) {
            return 0;
        }
        int i5 = i3;
        int i6 = 0;
        while (i5 > i && i6 != 4) {
            if (!ignore((char) bArr[i5 - 1])) {
                i6++;
            }
            i5--;
        }
        int nextI = nextI(bArr, i, i5);
        while (nextI < i5) {
            int i7 = nextI + 1;
            byte b = this.decodingTable[bArr[nextI]];
            int nextI2 = nextI(bArr, i7, i5);
            int i8 = nextI2 + 1;
            byte b2 = this.decodingTable[bArr[nextI2]];
            int nextI3 = nextI(bArr, i8, i5);
            int i9 = nextI3 + 1;
            byte b3 = this.decodingTable[bArr[nextI3]];
            int nextI4 = nextI(bArr, i9, i5);
            int i10 = nextI4 + 1;
            byte b4 = this.decodingTable[bArr[nextI4]];
            if ((b | b2 | b3 | b4) >= 0) {
                outputStream.write((b << 2) | (b2 >> 4));
                outputStream.write((b2 << 4) | (b3 >> 2));
                outputStream.write((b3 << 6) | b4);
                i4 += 3;
                nextI = nextI(bArr, i10, i5);
            } else {
                throw new IOException("invalid characters encountered in base64 data");
            }
        }
        int nextI5 = nextI(bArr, nextI, i3);
        int nextI6 = nextI(bArr, nextI5 + 1, i3);
        int nextI7 = nextI(bArr, nextI6 + 1, i3);
        return i4 + decodeLastBlock(outputStream, (char) bArr[nextI5], (char) bArr[nextI6], (char) bArr[nextI7], (char) bArr[nextI(bArr, nextI7 + 1, i3)]);
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x00a2  */
    public int encode(byte[] bArr, int i, int i2, OutputStream outputStream) throws IOException {
        int i3;
        int i4;
        byte b;
        int i5 = i2 % 3;
        int i6 = i2 - i5;
        int i7 = i;
        while (true) {
            i3 = i + i6;
            i4 = 4;
            if (i7 >= i3) {
                break;
            }
            byte b2 = bArr[i7] & 255;
            byte b3 = bArr[i7 + 1] & 255;
            byte b4 = bArr[i7 + 2] & 255;
            outputStream.write(this.encodingTable[(b2 >>> 2) & 63]);
            outputStream.write(this.encodingTable[((b2 << 4) | (b3 >>> 4)) & 63]);
            outputStream.write(this.encodingTable[((b3 << 2) | (b4 >>> 6)) & 63]);
            outputStream.write(this.encodingTable[b4 & 63]);
            i7 += 3;
        }
        switch (i5) {
            case 1:
                byte b5 = bArr[i3] & 255;
                outputStream.write(this.encodingTable[(b5 >>> 2) & 63]);
                outputStream.write(this.encodingTable[(b5 << 4) & 63]);
                b = this.padding;
                break;
            case 2:
                byte b6 = bArr[i3] & 255;
                byte b7 = bArr[i3 + 1] & 255;
                outputStream.write(this.encodingTable[(b6 >>> 2) & 63]);
                outputStream.write(this.encodingTable[((b6 << 4) | (b7 >>> 4)) & 63]);
                b = this.encodingTable[(b7 << 2) & 63];
                break;
            default:
                int i8 = (i6 / 3) * 4;
                if (i5 == 0) {
                    i4 = 0;
                }
                return i8 + i4;
        }
        outputStream.write(b);
        outputStream.write(this.padding);
        int i82 = (i6 / 3) * 4;
        if (i5 == 0) {
        }
        return i82 + i4;
    }

    /* access modifiers changed from: protected */
    public void initialiseDecodingTable() {
        for (int i = 0; i < this.decodingTable.length; i++) {
            this.decodingTable[i] = -1;
        }
        for (int i2 = 0; i2 < this.encodingTable.length; i2++) {
            this.decodingTable[this.encodingTable[i2]] = (byte) i2;
        }
    }
}
