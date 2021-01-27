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
        byte b = this.padding;
        if (c3 == b) {
            if (c4 == b) {
                byte[] bArr = this.decodingTable;
                byte b2 = bArr[c];
                byte b3 = bArr[c2];
                if ((b2 | b3) >= 0) {
                    outputStream.write((b2 << 2) | (b3 >> 4));
                    return 1;
                }
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            throw new IOException("invalid characters encountered at end of base64 data");
        } else if (c4 == b) {
            byte[] bArr2 = this.decodingTable;
            byte b4 = bArr2[c];
            byte b5 = bArr2[c2];
            byte b6 = bArr2[c3];
            if ((b4 | b5 | b6) >= 0) {
                outputStream.write((b4 << 2) | (b5 >> 4));
                outputStream.write((b5 << 4) | (b6 >> 2));
                return 2;
            }
            throw new IOException("invalid characters encountered at end of base64 data");
        } else {
            byte[] bArr3 = this.decodingTable;
            byte b7 = bArr3[c];
            byte b8 = bArr3[c2];
            byte b9 = bArr3[c3];
            byte b10 = bArr3[c4];
            if ((b7 | b8 | b9 | b10) >= 0) {
                outputStream.write((b7 << 2) | (b8 >> 4));
                outputStream.write((b8 << 4) | (b9 >> 2));
                outputStream.write((b9 << 6) | b10);
                return 3;
            }
            throw new IOException("invalid characters encountered at end of base64 data");
        }
    }

    private boolean ignore(char c) {
        return c == '\n' || c == '\r' || c == '\t' || c == ' ';
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

    @Override // org.bouncycastle.util.encoders.Encoder
    public int decode(String str, OutputStream outputStream) throws IOException {
        byte[] bArr = new byte[54];
        int length = str.length();
        while (length > 0 && ignore(str.charAt(length - 1))) {
            length--;
        }
        if (length == 0) {
            return 0;
        }
        int i = length;
        int i2 = 0;
        while (i > 0 && i2 != 4) {
            if (!ignore(str.charAt(i - 1))) {
                i2++;
            }
            i--;
        }
        int nextI = nextI(str, 0, i);
        int i3 = 0;
        int i4 = 0;
        while (nextI < i) {
            int i5 = nextI + 1;
            byte b = this.decodingTable[str.charAt(nextI)];
            int nextI2 = nextI(str, i5, i);
            int i6 = nextI2 + 1;
            byte b2 = this.decodingTable[str.charAt(nextI2)];
            int nextI3 = nextI(str, i6, i);
            int i7 = nextI3 + 1;
            byte b3 = this.decodingTable[str.charAt(nextI3)];
            int nextI4 = nextI(str, i7, i);
            int i8 = nextI4 + 1;
            byte b4 = this.decodingTable[str.charAt(nextI4)];
            if ((b | b2 | b3 | b4) >= 0) {
                int i9 = i3 + 1;
                bArr[i3] = (byte) ((b << 2) | (b2 >> 4));
                int i10 = i9 + 1;
                bArr[i9] = (byte) ((b2 << 4) | (b3 >> 2));
                i3 = i10 + 1;
                bArr[i10] = (byte) ((b3 << 6) | b4);
                i4 += 3;
                if (i3 == bArr.length) {
                    outputStream.write(bArr);
                    i3 = 0;
                }
                nextI = nextI(str, i8, i);
            } else {
                throw new IOException("invalid characters encountered in base64 data");
            }
        }
        if (i3 > 0) {
            outputStream.write(bArr, 0, i3);
        }
        int nextI5 = nextI(str, nextI, length);
        int nextI6 = nextI(str, nextI5 + 1, length);
        int nextI7 = nextI(str, nextI6 + 1, length);
        return i4 + decodeLastBlock(outputStream, str.charAt(nextI5), str.charAt(nextI6), str.charAt(nextI7), str.charAt(nextI(str, nextI7 + 1, length)));
    }

    @Override // org.bouncycastle.util.encoders.Encoder
    public int decode(byte[] bArr, int i, int i2, OutputStream outputStream) throws IOException {
        byte[] bArr2 = new byte[54];
        int i3 = i2 + i;
        while (i3 > i && ignore((char) bArr[i3 - 1])) {
            i3--;
        }
        if (i3 == 0) {
            return 0;
        }
        int i4 = i3;
        int i5 = 0;
        while (i4 > i && i5 != 4) {
            if (!ignore((char) bArr[i4 - 1])) {
                i5++;
            }
            i4--;
        }
        int nextI = nextI(bArr, i, i4);
        int i6 = 0;
        int i7 = 0;
        while (nextI < i4) {
            int i8 = nextI + 1;
            byte b = this.decodingTable[bArr[nextI]];
            int nextI2 = nextI(bArr, i8, i4);
            int i9 = nextI2 + 1;
            byte b2 = this.decodingTable[bArr[nextI2]];
            int nextI3 = nextI(bArr, i9, i4);
            int i10 = nextI3 + 1;
            byte b3 = this.decodingTable[bArr[nextI3]];
            int nextI4 = nextI(bArr, i10, i4);
            int i11 = nextI4 + 1;
            byte b4 = this.decodingTable[bArr[nextI4]];
            if ((b | b2 | b3 | b4) >= 0) {
                int i12 = i6 + 1;
                bArr2[i6] = (byte) ((b << 2) | (b2 >> 4));
                int i13 = i12 + 1;
                bArr2[i12] = (byte) ((b2 << 4) | (b3 >> 2));
                i6 = i13 + 1;
                bArr2[i13] = (byte) ((b3 << 6) | b4);
                if (i6 == bArr2.length) {
                    outputStream.write(bArr2);
                    i6 = 0;
                }
                i7 += 3;
                nextI = nextI(bArr, i11, i4);
            } else {
                throw new IOException("invalid characters encountered in base64 data");
            }
        }
        if (i6 > 0) {
            outputStream.write(bArr2, 0, i6);
        }
        int nextI5 = nextI(bArr, nextI, i3);
        int nextI6 = nextI(bArr, nextI5 + 1, i3);
        int nextI7 = nextI(bArr, nextI6 + 1, i3);
        return i7 + decodeLastBlock(outputStream, (char) bArr[nextI5], (char) bArr[nextI6], (char) bArr[nextI7], (char) bArr[nextI(bArr, nextI7 + 1, i3)]);
    }

    @Override // org.bouncycastle.util.encoders.Encoder
    public int encode(byte[] bArr, int i, int i2, OutputStream outputStream) throws IOException {
        byte[] bArr2 = new byte[72];
        while (i2 > 0) {
            int min = Math.min(54, i2);
            outputStream.write(bArr2, 0, encode(bArr, i, min, bArr2, 0));
            i += min;
            i2 -= min;
        }
        return ((i2 + 2) / 3) * 4;
    }

    public int encode(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws IOException {
        int i4 = (i + i2) - 2;
        int i5 = i;
        int i6 = i3;
        while (i5 < i4) {
            int i7 = i5 + 1;
            byte b = bArr[i5];
            int i8 = i7 + 1;
            int i9 = bArr[i7] & 255;
            int i10 = i8 + 1;
            int i11 = bArr[i8] & 255;
            int i12 = i6 + 1;
            byte[] bArr3 = this.encodingTable;
            bArr2[i6] = bArr3[(b >>> 2) & 63];
            int i13 = i12 + 1;
            bArr2[i12] = bArr3[((b << 4) | (i9 >>> 4)) & 63];
            int i14 = i13 + 1;
            bArr2[i13] = bArr3[((i9 << 2) | (i11 >>> 6)) & 63];
            i6 = i14 + 1;
            bArr2[i14] = bArr3[i11 & 63];
            i5 = i10;
        }
        int i15 = i2 - (i5 - i);
        if (i15 == 1) {
            int i16 = bArr[i5] & 255;
            int i17 = i6 + 1;
            byte[] bArr4 = this.encodingTable;
            bArr2[i6] = bArr4[(i16 >>> 2) & 63];
            int i18 = i17 + 1;
            bArr2[i17] = bArr4[(i16 << 4) & 63];
            int i19 = i18 + 1;
            byte b2 = this.padding;
            bArr2[i18] = b2;
            i6 = i19 + 1;
            bArr2[i19] = b2;
        } else if (i15 == 2) {
            int i20 = bArr[i5] & 255;
            int i21 = bArr[i5 + 1] & 255;
            int i22 = i6 + 1;
            byte[] bArr5 = this.encodingTable;
            bArr2[i6] = bArr5[(i20 >>> 2) & 63];
            int i23 = i22 + 1;
            bArr2[i22] = bArr5[((i20 << 4) | (i21 >>> 4)) & 63];
            int i24 = i23 + 1;
            bArr2[i23] = bArr5[(i21 << 2) & 63];
            i6 = i24 + 1;
            bArr2[i24] = this.padding;
        }
        return i6 - i3;
    }

    /* access modifiers changed from: protected */
    public void initialiseDecodingTable() {
        int i = 0;
        int i2 = 0;
        while (true) {
            byte[] bArr = this.decodingTable;
            if (i2 >= bArr.length) {
                break;
            }
            bArr[i2] = -1;
            i2++;
        }
        while (true) {
            byte[] bArr2 = this.encodingTable;
            if (i < bArr2.length) {
                this.decodingTable[bArr2[i]] = (byte) i;
                i++;
            } else {
                return;
            }
        }
    }
}
