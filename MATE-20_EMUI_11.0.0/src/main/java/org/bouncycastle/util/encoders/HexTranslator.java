package org.bouncycastle.util.encoders;

public class HexTranslator implements Translator {
    private static final byte[] hexTable = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};

    @Override // org.bouncycastle.util.encoders.Translator
    public int decode(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        int i4 = i2 / 2;
        for (int i5 = 0; i5 < i4; i5++) {
            int i6 = (i5 * 2) + i;
            byte b = bArr[i6];
            byte b2 = bArr[i6 + 1];
            if (b < 97) {
                bArr2[i3] = (byte) ((b - 48) << 4);
            } else {
                bArr2[i3] = (byte) (((b - 97) + 10) << 4);
            }
            if (b2 < 97) {
                bArr2[i3] = (byte) (bArr2[i3] + ((byte) (b2 - 48)));
            } else {
                bArr2[i3] = (byte) (bArr2[i3] + ((byte) ((b2 - 97) + 10)));
            }
            i3++;
        }
        return i4;
    }

    @Override // org.bouncycastle.util.encoders.Translator
    public int encode(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        int i4 = 0;
        int i5 = i;
        int i6 = 0;
        while (i4 < i2) {
            int i7 = i3 + i6;
            byte[] bArr3 = hexTable;
            bArr2[i7] = bArr3[(bArr[i5] >> 4) & 15];
            bArr2[i7 + 1] = bArr3[bArr[i5] & 15];
            i5++;
            i4++;
            i6 += 2;
        }
        return i2 * 2;
    }

    @Override // org.bouncycastle.util.encoders.Translator
    public int getDecodedBlockSize() {
        return 1;
    }

    @Override // org.bouncycastle.util.encoders.Translator
    public int getEncodedBlockSize() {
        return 2;
    }
}
