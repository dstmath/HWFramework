package ohos.global.icu.impl;

public final class UCharacterUtility {
    private static final int NON_CHARACTER_MAX_3_1_ = 65007;
    private static final int NON_CHARACTER_MIN_3_1_ = 64976;
    private static final int NON_CHARACTER_SUFFIX_MIN_3_0_ = 65534;

    public static boolean isNonCharacter(int i) {
        if ((i & NON_CHARACTER_SUFFIX_MIN_3_0_) == NON_CHARACTER_SUFFIX_MIN_3_0_) {
            return true;
        }
        return i >= NON_CHARACTER_MIN_3_1_ && i <= NON_CHARACTER_MAX_3_1_;
    }

    static int toInt(char c, char c2) {
        return (c << 16) | c2;
    }

    static int getNullTermByteSubString(StringBuffer stringBuffer, byte[] bArr, int i) {
        byte b = 1;
        while (b != 0) {
            b = bArr[i];
            if (b != 0) {
                stringBuffer.append((char) (b & 255));
            }
            i++;
        }
        return i;
    }

    static int compareNullTermByteSubString(String str, byte[] bArr, int i, int i2) {
        int length = str.length();
        int i3 = i;
        byte b = 1;
        while (b != 0) {
            b = bArr[i2];
            i2++;
            if (b == 0) {
                break;
            } else if (i3 == length || str.charAt(i3) != ((char) (b & 255))) {
                return -1;
            } else {
                i3++;
            }
        }
        return i3;
    }

    static int skipNullTermByteSubString(byte[] bArr, int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            byte b = 1;
            while (b != 0) {
                b = bArr[i];
                i++;
            }
        }
        return i;
    }

    static int skipByteSubString(byte[] bArr, int i, int i2, byte b) {
        int i3 = 0;
        while (i3 < i2) {
            if (bArr[i + i3] == b) {
                return i3 + 1;
            }
            i3++;
        }
        return i3;
    }

    private UCharacterUtility() {
    }
}
