package com.huawei.zxing.qrcode.decoder;

import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitMatrix;

final class BitMatrixParser {
    private final BitMatrix bitMatrix;
    private boolean mirror;
    private FormatInformation parsedFormatInfo;
    private Version parsedVersion;

    BitMatrixParser(BitMatrix bitMatrix2) throws FormatException {
        int dimension = bitMatrix2.getHeight();
        if (dimension < 21 || (dimension & 3) != 1) {
            throw FormatException.getFormatInstance();
        }
        this.bitMatrix = bitMatrix2;
    }

    /* access modifiers changed from: package-private */
    public FormatInformation readFormatInformation() throws FormatException {
        if (this.parsedFormatInfo != null) {
            return this.parsedFormatInfo;
        }
        int formatInfoBits1 = 0;
        for (int i = 0; i < 6; i++) {
            formatInfoBits1 = copyBit(i, 8, formatInfoBits1);
        }
        int formatInfoBits12 = copyBit(8, 7, copyBit(8, 8, copyBit(7, 8, formatInfoBits1)));
        for (int j = 5; j >= 0; j--) {
            formatInfoBits12 = copyBit(8, j, formatInfoBits12);
        }
        int dimension = this.bitMatrix.getHeight();
        int formatInfoBits2 = 0;
        int jMin = dimension - 7;
        for (int j2 = dimension - 1; j2 >= jMin; j2--) {
            formatInfoBits2 = copyBit(8, j2, formatInfoBits2);
        }
        for (int i2 = dimension - 8; i2 < dimension; i2++) {
            formatInfoBits2 = copyBit(i2, 8, formatInfoBits2);
        }
        this.parsedFormatInfo = FormatInformation.decodeFormatInformation(formatInfoBits12, formatInfoBits2);
        if (this.parsedFormatInfo != null) {
            return this.parsedFormatInfo;
        }
        throw FormatException.getFormatInstance();
    }

    /* access modifiers changed from: package-private */
    public Version readVersion() throws FormatException {
        if (this.parsedVersion != null) {
            return this.parsedVersion;
        }
        int dimension = this.bitMatrix.getHeight();
        int provisionalVersion = (dimension - 17) >> 2;
        if (provisionalVersion <= 6) {
            return Version.getVersionForNumber(provisionalVersion);
        }
        int ijMin = dimension - 11;
        int versionBits = 0;
        for (int j = 5; j >= 0; j--) {
            for (int i = dimension - 9; i >= ijMin; i--) {
                versionBits = copyBit(i, j, versionBits);
            }
        }
        Version theParsedVersion = Version.decodeVersionInformation(versionBits);
        if (theParsedVersion == null || theParsedVersion.getDimensionForVersion() != dimension) {
            int versionBits2 = 0;
            for (int i2 = 5; i2 >= 0; i2--) {
                for (int j2 = dimension - 9; j2 >= ijMin; j2--) {
                    versionBits2 = copyBit(i2, j2, versionBits2);
                }
            }
            Version theParsedVersion2 = Version.decodeVersionInformation(versionBits2);
            if (theParsedVersion2 == null || theParsedVersion2.getDimensionForVersion() != dimension) {
                throw FormatException.getFormatInstance();
            }
            this.parsedVersion = theParsedVersion2;
            return theParsedVersion2;
        }
        this.parsedVersion = theParsedVersion;
        return theParsedVersion;
    }

    private int copyBit(int i, int j, int versionBits) {
        return this.mirror ? this.bitMatrix.get(j, i) : this.bitMatrix.get(i, j) ? (versionBits << 1) | 1 : versionBits << 1;
    }

    /* access modifiers changed from: package-private */
    public byte[] readCodewords() throws FormatException {
        BitMatrixParser bitMatrixParser = this;
        FormatInformation formatInfo = readFormatInformation();
        Version version = readVersion();
        DataMask dataMask = DataMask.forReference(formatInfo.getDataMask());
        int dimension = bitMatrixParser.bitMatrix.getHeight();
        dataMask.unmaskBitMatrix(bitMatrixParser.bitMatrix, dimension);
        BitMatrix functionPattern = version.buildFunctionPattern();
        boolean readingUp = true;
        byte[] result = new byte[version.getTotalCodewords()];
        int resultOffset = 0;
        int resultOffset2 = 0;
        int currentByte = 0;
        int j = dimension - 1;
        while (j > 0) {
            if (j == 6) {
                j--;
            }
            int currentByte2 = currentByte;
            int resultOffset3 = resultOffset2;
            int currentByte3 = resultOffset;
            int count = 0;
            while (count < dimension) {
                int i = readingUp ? (dimension - 1) - count : count;
                int bitsRead = currentByte2;
                int currentByte4 = resultOffset3;
                int bitsRead2 = currentByte3;
                int col = 0;
                while (col < 2) {
                    if (!functionPattern.get(j - col, i)) {
                        bitsRead++;
                        int currentByte5 = currentByte4 << 1;
                        if (bitMatrixParser.bitMatrix.get(j - col, i)) {
                            currentByte5 |= 1;
                        }
                        if (bitsRead == 8) {
                            result[bitsRead2] = (byte) currentByte5;
                            bitsRead = 0;
                            currentByte4 = 0;
                            bitsRead2++;
                        } else {
                            currentByte4 = currentByte5;
                        }
                    }
                    col++;
                    bitMatrixParser = this;
                }
                count++;
                currentByte3 = bitsRead2;
                resultOffset3 = currentByte4;
                currentByte2 = bitsRead;
                bitMatrixParser = this;
            }
            readingUp = !readingUp;
            j -= 2;
            resultOffset = currentByte3;
            resultOffset2 = resultOffset3;
            currentByte = currentByte2;
            bitMatrixParser = this;
        }
        if (resultOffset == version.getTotalCodewords()) {
            return result;
        }
        throw FormatException.getFormatInstance();
    }

    /* access modifiers changed from: package-private */
    public void remask() {
        if (this.parsedFormatInfo != null) {
            DataMask.forReference(this.parsedFormatInfo.getDataMask()).unmaskBitMatrix(this.bitMatrix, this.bitMatrix.getHeight());
        }
    }

    /* access modifiers changed from: package-private */
    public void setMirror(boolean mirror2) {
        this.parsedVersion = null;
        this.parsedFormatInfo = null;
        this.mirror = mirror2;
    }

    /* access modifiers changed from: package-private */
    public void mirror() {
        for (int x = 0; x < this.bitMatrix.getWidth(); x++) {
            for (int y = x + 1; y < this.bitMatrix.getHeight(); y++) {
                if (this.bitMatrix.get(x, y) != this.bitMatrix.get(y, x)) {
                    this.bitMatrix.flip(y, x);
                    this.bitMatrix.flip(x, y);
                }
            }
        }
    }
}
