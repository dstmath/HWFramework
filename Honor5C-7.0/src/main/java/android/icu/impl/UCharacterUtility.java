package android.icu.impl;

import dalvik.bytecode.Opcodes;

public final class UCharacterUtility {
    private static final int NON_CHARACTER_MAX_3_1_ = 65007;
    private static final int NON_CHARACTER_MIN_3_1_ = 64976;
    private static final int NON_CHARACTER_SUFFIX_MIN_3_0_ = 65534;

    public static boolean isNonCharacter(int ch) {
        boolean z = true;
        if ((ch & NON_CHARACTER_SUFFIX_MIN_3_0_) == NON_CHARACTER_SUFFIX_MIN_3_0_) {
            return true;
        }
        if (ch < NON_CHARACTER_MIN_3_1_ || ch > NON_CHARACTER_MAX_3_1_) {
            z = false;
        }
        return z;
    }

    static int toInt(char msc, char lsc) {
        return (msc << 16) | lsc;
    }

    static int getNullTermByteSubString(StringBuffer str, byte[] array, int index) {
        byte b = (byte) 1;
        while (b != null) {
            b = array[index];
            if (b != null) {
                str.append((char) (b & Opcodes.OP_CONST_CLASS_JUMBO));
            }
            index++;
        }
        return index;
    }

    static int compareNullTermByteSubString(String str, byte[] array, int strindex, int aindex) {
        byte b = (byte) 1;
        int length = str.length();
        while (b != null) {
            b = array[aindex];
            aindex++;
            if (b == null) {
                break;
            } else if (strindex == length || str.charAt(strindex) != ((char) (b & Opcodes.OP_CONST_CLASS_JUMBO))) {
                return -1;
            } else {
                strindex++;
            }
        }
        return strindex;
    }

    static int skipNullTermByteSubString(byte[] array, int index, int skipcount) {
        for (int i = 0; i < skipcount; i++) {
            byte b = (byte) 1;
            while (b != null) {
                b = array[index];
                index++;
            }
        }
        return index;
    }

    static int skipByteSubString(byte[] array, int index, int length, byte skipend) {
        int result = 0;
        while (result < length) {
            if (array[index + result] == skipend) {
                return result + 1;
            }
            result++;
        }
        return result;
    }

    private UCharacterUtility() {
    }
}
