package com.huawei.zxing.qrcode.decoder;

import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitMatrix;

/* access modifiers changed from: package-private */
public final class BitMatrixParser {
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
        FormatInformation formatInformation = this.parsedFormatInfo;
        if (formatInformation != null) {
            return formatInformation;
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
        FormatInformation formatInformation2 = this.parsedFormatInfo;
        if (formatInformation2 != null) {
            return formatInformation2;
        }
        throw FormatException.getFormatInstance();
    }

    /* access modifiers changed from: package-private */
    public Version readVersion() throws FormatException {
        Version version = this.parsedVersion;
        if (version != null) {
            return version;
        }
        int dimension = this.bitMatrix.getHeight();
        int provisionalVersion = (dimension - 17) >> 2;
        if (provisionalVersion <= 6) {
            return Version.getVersionForNumber(provisionalVersion);
        }
        int versionBits = 0;
        int ijMin = dimension - 11;
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
        int i2 = versionBits << 1;
        return this.mirror ? this.bitMatrix.get(j, i) : this.bitMatrix.get(i, j) ? i2 | 1 : i2;
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
        int bitsRead = 0;
        int currentByte = 0;
        int bitsRead2 = 0;
        int j = dimension - 1;
        while (j > 0) {
            if (j == 6) {
                j--;
            }
            int count = 0;
            while (count < dimension) {
                int i = readingUp ? (dimension - 1) - count : count;
                int col = 0;
                while (col < 2) {
                    if (!functionPattern.get(j - col, i)) {
                        bitsRead2++;
                        currentByte <<= 1;
                        if (bitMatrixParser.bitMatrix.get(j - col, i)) {
                            currentByte |= 1;
                        }
                        if (bitsRead2 == 8) {
                            result[bitsRead] = (byte) currentByte;
                            currentByte = 0;
                            bitsRead2 = 0;
                            bitsRead++;
                        }
                    }
                    col++;
                    bitMatrixParser = this;
                }
                count++;
                bitMatrixParser = this;
            }
            readingUp = !readingUp;
            j -= 2;
            bitMatrixParser = this;
        }
        if (bitsRead == version.getTotalCodewords()) {
            return result;
        }
        throw FormatException.getFormatInstance();
    }

    /* access modifiers changed from: package-private */
    public void remask() {
        FormatInformation formatInformation = this.parsedFormatInfo;
        if (formatInformation != null) {
            DataMask.forReference(formatInformation.getDataMask()).unmaskBitMatrix(this.bitMatrix, this.bitMatrix.getHeight());
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
