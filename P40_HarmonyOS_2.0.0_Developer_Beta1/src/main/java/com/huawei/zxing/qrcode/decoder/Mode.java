package com.huawei.zxing.qrcode.decoder;

public enum Mode {
    TERMINATOR(new int[]{0, 0, 0}, 0),
    NUMERIC(new int[]{10, 12, 14}, 1),
    ALPHANUMERIC(new int[]{9, 11, 13}, 2),
    STRUCTURED_APPEND(new int[]{0, 0, 0}, 3),
    BYTE(new int[]{8, 16, 16}, 4),
    ECI(new int[]{0, 0, 0}, 7),
    KANJI(new int[]{8, 10, 12}, 8),
    FNC1_FIRST_POSITION(new int[]{0, 0, 0}, 5),
    FNC1_SECOND_POSITION(new int[]{0, 0, 0}, 9),
    HANZI(new int[]{8, 10, 12}, 13);
    
    private final int bits;
    private final int[] characterCountBitsForVersions;

    private Mode(int[] characterCountBitsForVersions2, int bits2) {
        this.characterCountBitsForVersions = characterCountBitsForVersions2;
        this.bits = bits2;
    }

    public static Mode forBits(int bits2) {
        if (bits2 == 0) {
            return TERMINATOR;
        }
        if (bits2 == 1) {
            return NUMERIC;
        }
        if (bits2 == 2) {
            return ALPHANUMERIC;
        }
        if (bits2 == 3) {
            return STRUCTURED_APPEND;
        }
        if (bits2 == 4) {
            return BYTE;
        }
        if (bits2 == 5) {
            return FNC1_FIRST_POSITION;
        }
        if (bits2 == 7) {
            return ECI;
        }
        if (bits2 == 8) {
            return KANJI;
        }
        if (bits2 == 9) {
            return FNC1_SECOND_POSITION;
        }
        if (bits2 == 13) {
            return HANZI;
        }
        throw new IllegalArgumentException();
    }

    public int getCharacterCountBits(Version version) {
        int offset;
        int number = version.getVersionNumber();
        if (number <= 9) {
            offset = 0;
        } else if (number <= 26) {
            offset = 1;
        } else {
            offset = 2;
        }
        return this.characterCountBitsForVersions[offset];
    }

    public int getBits() {
        return this.bits;
    }
}
