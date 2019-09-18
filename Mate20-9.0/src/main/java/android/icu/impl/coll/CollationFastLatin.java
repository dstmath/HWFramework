package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;
import android.icu.text.DateTimePatternGenerator;
import dalvik.bytecode.Opcodes;

public final class CollationFastLatin {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int BAIL_OUT = 1;
    public static final int BAIL_OUT_RESULT = -2;
    static final int CASE_AND_TERTIARY_MASK = 31;
    static final int CASE_MASK = 24;
    static final int COMMON_SEC = 160;
    static final int COMMON_SEC_PLUS_OFFSET = 192;
    static final int COMMON_TER = 0;
    static final int COMMON_TER_PLUS_OFFSET = 32;
    static final int CONTRACTION = 1024;
    static final int CONTR_CHAR_MASK = 511;
    static final int CONTR_LENGTH_SHIFT = 9;
    static final int EOS = 2;
    static final int EXPANSION = 2048;
    static final int INDEX_MASK = 1023;
    public static final int LATIN_LIMIT = 384;
    public static final int LATIN_MAX = 383;
    static final int LATIN_MAX_UTF8_LEAD = 197;
    static final int LONG_INC = 8;
    static final int LONG_PRIMARY_MASK = 65528;
    static final int LOWER_CASE = 8;
    static final int MAX_LONG = 4088;
    static final int MAX_SEC_AFTER = 352;
    static final int MAX_SEC_BEFORE = 128;
    static final int MAX_SEC_HIGH = 992;
    static final int MAX_SHORT = 64512;
    static final int MAX_TER_AFTER = 7;
    static final int MERGE_WEIGHT = 3;
    static final int MIN_LONG = 3072;
    static final int MIN_SEC_AFTER = 192;
    static final int MIN_SEC_BEFORE = 0;
    static final int MIN_SEC_HIGH = 384;
    static final int MIN_SHORT = 4096;
    static final int NUM_FAST_CHARS = 448;
    static final int PUNCT_LIMIT = 8256;
    static final int PUNCT_START = 8192;
    static final int SECONDARY_MASK = 992;
    static final int SEC_INC = 32;
    static final int SEC_OFFSET = 32;
    static final int SHORT_INC = 1024;
    static final int SHORT_PRIMARY_MASK = 64512;
    static final int TERTIARY_MASK = 7;
    static final int TER_OFFSET = 32;
    static final int TWO_CASES_MASK = 1572888;
    static final int TWO_COMMON_SEC_PLUS_OFFSET = 12583104;
    static final int TWO_COMMON_TER_PLUS_OFFSET = 2097184;
    static final int TWO_LONG_PRIMARIES_MASK = -458760;
    static final int TWO_LOWER_CASES = 524296;
    static final int TWO_SECONDARIES_MASK = 65012704;
    static final int TWO_SEC_OFFSETS = 2097184;
    static final int TWO_SHORT_PRIMARIES_MASK = -67044352;
    static final int TWO_TERTIARIES_MASK = 458759;
    static final int TWO_TER_OFFSETS = 2097184;
    public static final int VERSION = 2;

    static int getCharIndex(char c) {
        if (c <= 383) {
            return c;
        }
        if (8192 > c || c >= PUNCT_LIMIT) {
            return -1;
        }
        return c - 7808;
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r5v6, types: [char] */
    public static int getOptions(CollationData data, CollationSettings settings, char[] primaries) {
        int headerLength;
        int p;
        CollationData collationData = data;
        CollationSettings collationSettings = settings;
        char[] cArr = primaries;
        char[] header = collationData.fastLatinTableHeader;
        if (header == null || cArr.length != 384) {
            return -1;
        }
        if ((collationSettings.options & 12) == 0) {
            headerLength = Opcodes.OP_IGET_CHAR_JUMBO;
        } else {
            int headerLength2 = header[0] & 255;
            int i = 1 + settings.getMaxVariable();
            if (i >= headerLength2) {
                return -1;
            }
            headerLength = header[i];
        }
        boolean digitsAreReordered = false;
        if (settings.hasReordering()) {
            long prevStart = 0;
            long beforeDigitStart = 0;
            long digitStart = 0;
            long afterDigitStart = 0;
            for (int group = 4096; group < 4104; group++) {
                long prevStart2 = collationSettings.reorder(collationData.getFirstPrimaryForGroup(group));
                if (group == 4100) {
                    beforeDigitStart = prevStart;
                    digitStart = prevStart2;
                } else if (prevStart2 == 0) {
                    continue;
                } else if (prevStart2 < prevStart) {
                    return -1;
                } else {
                    if (digitStart != 0 && afterDigitStart == 0 && prevStart == beforeDigitStart) {
                        afterDigitStart = prevStart2;
                    }
                    prevStart = prevStart2;
                }
            }
            long latinStart = collationSettings.reorder(collationData.getFirstPrimaryForGroup(25));
            if (latinStart < prevStart) {
                return -1;
            }
            if (afterDigitStart == 0) {
                afterDigitStart = latinStart;
            }
            if (beforeDigitStart >= digitStart || digitStart >= afterDigitStart) {
                digitsAreReordered = true;
            }
        }
        char[] table = collationData.fastLatinTable;
        for (int c = 0; c < 384; c++) {
            char p2 = table[c];
            if (p2 >= 4096) {
                p = p2 & Normalizer2Impl.MIN_NORMAL_MAYBE_YES;
            } else if (p2 > headerLength) {
                p = p2 & LONG_PRIMARY_MASK;
            } else {
                p = 0;
            }
            cArr[c] = (char) p;
        }
        if (digitsAreReordered || (collationSettings.options & 2) != 0) {
            for (int c2 = 48; c2 <= 57; c2++) {
                cArr[c2] = 0;
            }
        }
        return (headerLength << 16) | collationSettings.options;
    }

    /* JADX WARNING: type inference failed for: r24v0, types: [char[]] */
    /* JADX WARNING: type inference failed for: r13v11, types: [char] */
    /* JADX WARNING: type inference failed for: r13v17, types: [char] */
    /* JADX WARNING: type inference failed for: r13v27, types: [char] */
    /* JADX WARNING: type inference failed for: r14v8, types: [char] */
    /* JADX WARNING: type inference failed for: r13v38, types: [char] */
    /* JADX WARNING: type inference failed for: r13v44, types: [char] */
    /* JADX WARNING: type inference failed for: r6v54, types: [char] */
    /* JADX WARNING: type inference failed for: r6v55, types: [char] */
    /* JADX WARNING: type inference failed for: r8v48, types: [char] */
    /* JADX WARNING: type inference failed for: r8v49, types: [char] */
    /* JADX WARNING: type inference failed for: r6v66, types: [char] */
    /* JADX WARNING: type inference failed for: r6v68, types: [char] */
    /* JADX WARNING: type inference failed for: r6v69, types: [char] */
    /* JADX WARNING: type inference failed for: r8v60, types: [char] */
    /* JADX WARNING: type inference failed for: r8v62, types: [char] */
    /* JADX WARNING: type inference failed for: r8v63, types: [char] */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x0191, code lost:
        r10 = r11;
     */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r6v54, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r6v55, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r6v66, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r6v68, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r6v69, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r8v48, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r8v49, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r8v60, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r8v62, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r8v63, types: [char] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:321:0x00e5 A[EDGE_INSN: B:321:0x00e5->B:69:0x00e5 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:325:0x00be A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00c3  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x00e8  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00f4  */
    /* JADX WARNING: Unknown variable types count: 1 */
    public static int compareUTF16(char[] table, char[] r24, int options, CharSequence left, CharSequence right, int startIndex) {
        int leftPair;
        int rightPair;
        int i;
        int leftPair2;
        int rightPair2;
        int rightPair3;
        int leftPair3;
        int rightIndex;
        int rightPair4;
        int rightPair5;
        int leftPair4;
        char[] cArr = table;
        CharSequence charSequence = left;
        CharSequence charSequence2 = right;
        int variableTop = options >> 16;
        int options2 = options & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
        int leftPair5 = 0;
        int rightIndex2 = startIndex;
        int leftIndex = startIndex;
        int rightPair6 = 0;
        while (true) {
            int i2 = PUNCT_LIMIT;
            int i3 = -2;
            if (leftPair == 0) {
                if (leftIndex == left.length()) {
                    leftPair = 2;
                } else {
                    int leftIndex2 = leftIndex + 1;
                    int leftIndex3 = charSequence.charAt(leftIndex);
                    if (leftIndex3 <= 383) {
                        leftPair = r24[leftIndex3];
                        if (leftPair == 0) {
                            if (leftIndex3 <= 57 && leftIndex3 >= 48 && (options2 & 2) != 0) {
                                return -2;
                            }
                            leftPair4 = cArr[leftIndex3];
                            if (leftPair4 < 4096) {
                                leftPair = leftPair4 & Normalizer2Impl.MIN_NORMAL_MAYBE_YES;
                            } else if (leftPair4 > variableTop) {
                                leftPair = leftPair4 & LONG_PRIMARY_MASK;
                            } else {
                                int rightPair7 = rightPair6;
                                long pairAndInc = nextPair(cArr, leftIndex3, leftPair4, charSequence, leftIndex2);
                                if (pairAndInc < 0) {
                                    leftIndex2++;
                                    pairAndInc = ~pairAndInc;
                                }
                                int leftPair6 = (int) pairAndInc;
                                if (leftPair6 == 1) {
                                    return -2;
                                }
                                leftPair5 = getPrimaries(variableTop, leftPair6);
                                leftIndex = leftIndex2;
                                rightPair6 = rightPair7;
                            }
                        }
                    } else {
                        if (8192 > leftIndex3 || leftIndex3 >= PUNCT_LIMIT) {
                            leftPair4 = lookup(cArr, leftIndex3);
                        } else {
                            leftPair4 = cArr[(leftIndex3 - 8192) + 384];
                        }
                        if (leftPair4 < 4096) {
                        }
                    }
                    leftIndex = leftIndex2;
                }
                while (true) {
                    if (rightPair != 0) {
                        break;
                    } else if (rightIndex2 == right.length()) {
                        rightPair = 2;
                        break;
                    } else {
                        rightIndex = rightIndex2 + 1;
                        int rightIndex3 = charSequence2.charAt(rightIndex2);
                        if (rightIndex3 <= 383) {
                            rightPair = r24[rightIndex3];
                            if (rightPair != 0) {
                                break;
                            } else if (rightIndex3 <= 57 && rightIndex3 >= 48 && (options2 & 2) != 0) {
                                return i3;
                            } else {
                                rightPair4 = cArr[rightIndex3];
                                if (rightPair4 < 4096) {
                                    rightPair = rightPair4 & Normalizer2Impl.MIN_NORMAL_MAYBE_YES;
                                    break;
                                } else if (rightPair4 > variableTop) {
                                    rightPair = rightPair4 & LONG_PRIMARY_MASK;
                                    break;
                                } else {
                                    long pairAndInc2 = nextPair(cArr, rightIndex3, rightPair4, charSequence2, rightIndex);
                                    if (pairAndInc2 < 0) {
                                        rightIndex++;
                                        pairAndInc2 = ~pairAndInc2;
                                    }
                                    rightPair5 = (int) pairAndInc2;
                                    if (rightPair5 == 1) {
                                        return -2;
                                    }
                                    rightPair6 = getPrimaries(variableTop, rightPair5);
                                    rightIndex2 = rightIndex;
                                    i2 = PUNCT_LIMIT;
                                    i3 = -2;
                                }
                            }
                        } else {
                            if (8192 > rightIndex3 || rightIndex3 >= i2) {
                                rightPair4 = lookup(cArr, rightIndex3);
                            } else {
                                rightPair4 = cArr[(rightIndex3 - 8192) + 384];
                            }
                            if (rightPair4 < 4096) {
                            }
                        }
                    }
                }
                rightIndex2 = rightIndex;
                if (leftPair == rightPair) {
                    int leftPrimary = leftPair & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                    int rightPrimary = rightPair & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                    if (leftPrimary != rightPrimary) {
                        return leftPrimary < rightPrimary ? -1 : 1;
                    } else if (leftPair == 2) {
                        break;
                    } else {
                        leftPair5 = leftPair >>> 16;
                        rightPair6 = rightPair >>> 16;
                    }
                } else if (leftPair == 2) {
                    break;
                } else {
                    rightPair6 = 0;
                    leftPair5 = 0;
                }
            } else {
                while (true) {
                    if (rightPair != 0) {
                    }
                    rightPair6 = getPrimaries(variableTop, rightPair5);
                    rightIndex2 = rightIndex;
                    i2 = PUNCT_LIMIT;
                    i3 = -2;
                }
                rightIndex2 = rightIndex;
                if (leftPair == rightPair) {
                }
            }
        }
        if (CollationSettings.getStrength(options2) >= 1) {
            int rightIndex4 = startIndex;
            int leftIndex4 = startIndex;
            int rightPair8 = 0;
            int leftPair7 = 0;
            while (true) {
                if (leftPair2 == 0) {
                    if (leftIndex4 == left.length()) {
                        leftPair2 = 2;
                    } else {
                        int leftIndex5 = leftIndex4 + 1;
                        int leftIndex6 = charSequence.charAt(leftIndex4);
                        if (leftIndex6 <= 383) {
                            leftPair3 = cArr[leftIndex6];
                        } else if (8192 > leftIndex6 || leftIndex6 >= PUNCT_LIMIT) {
                            leftPair3 = lookup(cArr, leftIndex6);
                        } else {
                            leftPair3 = cArr[(leftIndex6 - 8192) + 384];
                        }
                        if (leftPair3 >= 4096) {
                            leftPair2 = getSecondariesFromOneShortCE(leftPair3);
                        } else if (leftPair3 > variableTop) {
                            leftPair2 = 192;
                        } else {
                            long pairAndInc3 = nextPair(cArr, leftIndex6, leftPair3, charSequence, leftIndex5);
                            if (pairAndInc3 < 0) {
                                leftIndex5++;
                                pairAndInc3 = ~pairAndInc3;
                            }
                            leftPair7 = getSecondaries(variableTop, (int) pairAndInc3);
                            leftIndex4 = leftIndex5;
                        }
                        leftIndex4 = leftIndex5;
                    }
                }
                while (true) {
                    if (rightPair2 != 0) {
                        break;
                    } else if (rightIndex4 == right.length()) {
                        rightPair2 = 2;
                        break;
                    } else {
                        int rightIndex5 = rightIndex4 + 1;
                        int rightIndex6 = charSequence2.charAt(rightIndex4);
                        if (rightIndex6 <= 383) {
                            rightPair3 = cArr[rightIndex6];
                        } else {
                            if (8192 <= rightIndex6) {
                                if (rightIndex6 < PUNCT_LIMIT) {
                                    rightPair3 = cArr[(rightIndex6 - 8192) + 384];
                                }
                            }
                            rightPair3 = lookup(cArr, rightIndex6);
                        }
                        if (rightPair3 >= 4096) {
                            rightPair2 = getSecondariesFromOneShortCE(rightPair3);
                            break;
                        } else if (rightPair3 > variableTop) {
                            rightPair2 = 192;
                            break;
                        } else {
                            long pairAndInc4 = nextPair(cArr, rightIndex6, rightPair3, charSequence2, rightIndex5);
                            if (pairAndInc4 < 0) {
                                rightIndex5++;
                                pairAndInc4 = ~pairAndInc4;
                            }
                            rightPair8 = getSecondaries(variableTop, (int) pairAndInc4);
                            rightIndex4 = rightIndex5;
                        }
                    }
                }
                if (leftPair2 != rightPair2) {
                    int leftSecondary = leftPair2 & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                    int rightSecondary = rightPair2 & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                    if (leftSecondary != rightSecondary) {
                        if ((options2 & 2048) != 0) {
                            return -2;
                        }
                        return leftSecondary < rightSecondary ? -1 : 1;
                    } else if (leftPair2 == 2) {
                        break;
                    } else {
                        leftPair7 = leftPair2 >>> 16;
                        rightPair8 = rightPair2 >>> 16;
                    }
                } else if (leftPair2 == 2) {
                    break;
                } else {
                    rightPair8 = 0;
                    leftPair7 = 0;
                }
            }
        }
        if ((options2 & 1024) != 0) {
            boolean strengthIsPrimary = CollationSettings.getStrength(options2) == 0;
            int rightIndex7 = startIndex;
            int leftIndex7 = startIndex;
            int rightPair9 = 0;
            int leftPair8 = 0;
            while (true) {
                if (leftPair8 == 0) {
                    if (leftIndex7 == left.length()) {
                        leftPair8 = 2;
                    } else {
                        int leftIndex8 = leftIndex7 + 1;
                        int leftIndex9 = charSequence.charAt(leftIndex7);
                        int leftPair9 = leftIndex9 <= 383 ? cArr[leftIndex9] : lookup(cArr, leftIndex9);
                        if (leftPair9 < MIN_LONG) {
                            long pairAndInc5 = nextPair(cArr, leftIndex9, leftPair9, charSequence, leftIndex8);
                            if (pairAndInc5 < 0) {
                                leftIndex8++;
                                pairAndInc5 = ~pairAndInc5;
                            }
                            leftPair9 = (int) pairAndInc5;
                        }
                        leftPair8 = getCases(variableTop, strengthIsPrimary, leftPair9);
                        leftIndex7 = leftIndex8;
                    }
                }
                while (true) {
                    if (rightPair9 != 0) {
                        break;
                    } else if (rightIndex7 == right.length()) {
                        rightPair9 = 2;
                        break;
                    } else {
                        int rightIndex8 = rightIndex7 + 1;
                        int rightIndex9 = charSequence2.charAt(rightIndex7);
                        int rightPair10 = rightIndex9 <= 383 ? cArr[rightIndex9] : lookup(cArr, rightIndex9);
                        if (rightPair10 < MIN_LONG) {
                            long pairAndInc6 = nextPair(cArr, rightIndex9, rightPair10, charSequence2, rightIndex8);
                            if (pairAndInc6 < 0) {
                                rightIndex8++;
                                pairAndInc6 = ~pairAndInc6;
                            }
                            rightPair10 = (int) pairAndInc6;
                        }
                        rightPair9 = getCases(variableTop, strengthIsPrimary, rightPair10);
                        rightIndex7 = rightIndex8;
                    }
                }
                if (leftPair8 != rightPair9) {
                    int leftCase = leftPair8 & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                    int rightCase = rightPair9 & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                    if (leftCase != rightCase) {
                        if ((options2 & 256) == 0) {
                            return leftCase < rightCase ? -1 : 1;
                        }
                        return leftCase < rightCase ? 1 : -1;
                    } else if (leftPair8 == 2) {
                        break;
                    } else {
                        leftPair8 >>>= 16;
                        rightPair9 >>>= 16;
                    }
                } else if (leftPair8 == 2) {
                    break;
                } else {
                    rightPair9 = 0;
                    leftPair8 = 0;
                }
            }
        }
        if (CollationSettings.getStrength(options2) <= 1) {
            return 0;
        }
        boolean withCaseBits = CollationSettings.isTertiaryWithCaseBits(options2);
        int rightIndex10 = startIndex;
        int leftIndex10 = startIndex;
        int rightPair11 = 0;
        int leftPair10 = 0;
        while (true) {
            if (leftPair10 == 0) {
                if (leftIndex10 == left.length()) {
                    leftPair10 = 2;
                } else {
                    int leftIndex11 = leftIndex10 + 1;
                    int leftIndex12 = charSequence.charAt(leftIndex10);
                    int leftPair11 = leftIndex12 <= 383 ? cArr[leftIndex12] : lookup(cArr, leftIndex12);
                    if (leftPair11 < MIN_LONG) {
                        long pairAndInc7 = nextPair(cArr, leftIndex12, leftPair11, charSequence, leftIndex11);
                        if (pairAndInc7 < 0) {
                            leftIndex11++;
                            pairAndInc7 = ~pairAndInc7;
                        }
                        leftPair11 = (int) pairAndInc7;
                    }
                    leftPair10 = getTertiaries(variableTop, withCaseBits, leftPair11);
                    leftIndex10 = leftIndex11;
                }
            }
            while (true) {
                if (rightPair11 != 0) {
                    break;
                } else if (rightIndex10 == right.length()) {
                    rightPair11 = 2;
                    break;
                } else {
                    int rightIndex11 = rightIndex10 + 1;
                    int rightIndex12 = charSequence2.charAt(rightIndex10);
                    int rightPair12 = rightIndex12 <= 383 ? cArr[rightIndex12] : lookup(cArr, rightIndex12);
                    if (rightPair12 < MIN_LONG) {
                        long pairAndInc8 = nextPair(cArr, rightIndex12, rightPair12, charSequence2, rightIndex11);
                        if (pairAndInc8 < 0) {
                            rightIndex11++;
                            pairAndInc8 = ~pairAndInc8;
                        }
                        rightPair12 = (int) pairAndInc8;
                    }
                    rightPair11 = getTertiaries(variableTop, withCaseBits, rightPair12);
                    rightIndex10 = rightIndex11;
                }
            }
            if (leftPair10 == rightPair11) {
                i = 2;
                if (leftPair10 == 2) {
                    break;
                }
                rightPair11 = 0;
                leftPair10 = 0;
            } else {
                int leftTertiary = leftPair10 & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                int rightTertiary = rightPair11 & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                if (leftTertiary != rightTertiary) {
                    if (CollationSettings.sortsTertiaryUpperCaseFirst(options2)) {
                        if (leftTertiary > 3) {
                            leftTertiary ^= 24;
                        }
                        if (rightTertiary > 3) {
                            rightTertiary ^= 24;
                        }
                    }
                    return leftTertiary < rightTertiary ? -1 : 1;
                }
                i = 2;
                if (leftPair10 == 2) {
                    break;
                }
                leftPair10 >>>= 16;
                rightPair11 >>>= 16;
            }
        }
        if (CollationSettings.getStrength(options2) <= i) {
            return 0;
        }
        int rightIndex13 = startIndex;
        int leftIndex13 = startIndex;
        int rightPair13 = 0;
        int leftPair12 = 0;
        while (true) {
            if (leftPair12 == 0) {
                if (leftIndex13 == left.length()) {
                    leftPair12 = 2;
                } else {
                    int leftIndex14 = leftIndex13 + 1;
                    int leftIndex15 = charSequence.charAt(leftIndex13);
                    int leftPair13 = leftIndex15 <= 383 ? cArr[leftIndex15] : lookup(cArr, leftIndex15);
                    if (leftPair13 < MIN_LONG) {
                        long pairAndInc9 = nextPair(cArr, leftIndex15, leftPair13, charSequence, leftIndex14);
                        if (pairAndInc9 < 0) {
                            leftIndex14++;
                            pairAndInc9 = ~pairAndInc9;
                        }
                        leftPair13 = (int) pairAndInc9;
                    }
                    leftPair12 = getQuaternaries(variableTop, leftPair13);
                    leftIndex13 = leftIndex14;
                }
            }
            while (true) {
                if (rightPair13 != 0) {
                    break;
                } else if (rightIndex13 == right.length()) {
                    rightPair13 = 2;
                    break;
                } else {
                    int rightIndex14 = rightIndex13 + 1;
                    int rightIndex15 = charSequence2.charAt(rightIndex13);
                    int rightPair14 = rightIndex15 <= 383 ? cArr[rightIndex15] : lookup(cArr, rightIndex15);
                    if (rightPair14 < MIN_LONG) {
                        long pairAndInc10 = nextPair(cArr, rightIndex15, rightPair14, charSequence2, rightIndex14);
                        if (pairAndInc10 < 0) {
                            rightIndex14++;
                            pairAndInc10 = ~pairAndInc10;
                        }
                        rightPair14 = (int) pairAndInc10;
                    }
                    rightPair13 = getQuaternaries(variableTop, rightPair14);
                    rightIndex13 = rightIndex14;
                }
            }
            if (leftPair12 != rightPair13) {
                int leftQuaternary = leftPair12 & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                int rightQuaternary = rightPair13 & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                if (leftQuaternary != rightQuaternary) {
                    return leftQuaternary < rightQuaternary ? -1 : 1;
                } else if (leftPair12 == 2) {
                    break;
                } else {
                    leftPair12 >>>= 16;
                    rightPair13 >>>= 16;
                }
            } else if (leftPair12 == 2) {
                break;
            } else {
                rightPair13 = 0;
                leftPair12 = 0;
            }
        }
        return 0;
    }

    private static int lookup(char[] table, int c) {
        if (8192 <= c && c < PUNCT_LIMIT) {
            return table[(c - 8192) + 384];
        }
        if (c == 65534) {
            return 3;
        }
        if (c == 65535) {
            return 64680;
        }
        return 1;
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r8v0, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r8v2, types: [char] */
    private static long nextPair(char[] table, int c, int ce, CharSequence s16, int sIndex) {
        long result;
        int x;
        if (ce >= MIN_LONG || ce < 1024) {
            return (long) ce;
        }
        if (ce >= 2048) {
            int index = NUM_FAST_CHARS + (ce & 1023);
            return (((long) table[index + 1]) << 16) | ((long) table[index]);
        }
        int index2 = NUM_FAST_CHARS + (ce & 1023);
        boolean inc = false;
        if (sIndex != s16.length()) {
            int nextIndex = sIndex;
            int i = nextIndex + 1;
            int c2 = s16.charAt(nextIndex);
            if (c2 > 383) {
                if (8192 <= c2 && c2 < PUNCT_LIMIT) {
                    c2 = (c2 - 8192) + 384;
                } else if (c2 != 65534 && c2 != 65535) {
                    return 1;
                } else {
                    c2 = -1;
                }
            }
            int i2 = index2;
            int head = table[i2];
            do {
                i2 += head >> 9;
                head = table[i2];
                x = head & 511;
            } while (x < c2);
            if (x == c2) {
                index2 = i2;
                inc = true;
            }
        }
        int length = table[index2] >> 9;
        if (length == 1) {
            return 1;
        }
        char ce2 = table[index2 + 1];
        if (length == 2) {
            result = (long) ce2;
        } else {
            result = (((long) table[index2 + 2]) << 16) | ((long) ce2);
        }
        return inc ? ~result : result;
    }

    private static int getPrimaries(int variableTop, int pair) {
        int ce = 65535 & pair;
        if (ce >= 4096) {
            return TWO_SHORT_PRIMARIES_MASK & pair;
        }
        if (ce > variableTop) {
            return TWO_LONG_PRIMARIES_MASK & pair;
        }
        if (ce >= MIN_LONG) {
            return 0;
        }
        return pair;
    }

    private static int getSecondariesFromOneShortCE(int ce) {
        int ce2 = ce & 992;
        if (ce2 < 384) {
            return ce2 + 32;
        }
        return ((ce2 + 32) << 16) | 192;
    }

    private static int getSecondaries(int variableTop, int pair) {
        if (pair > 65535) {
            int ce = 65535 & pair;
            if (ce >= 4096) {
                return (TWO_SECONDARIES_MASK & pair) + 2097184;
            }
            if (ce > variableTop) {
                return TWO_COMMON_SEC_PLUS_OFFSET;
            }
            return 0;
        } else if (pair >= 4096) {
            return getSecondariesFromOneShortCE(pair);
        } else {
            if (pair > variableTop) {
                return 192;
            }
            if (pair >= MIN_LONG) {
                return 0;
            }
            return pair;
        }
    }

    private static int getCases(int variableTop, boolean strengthIsPrimary, int pair) {
        if (pair > 65535) {
            int ce = 65535 & pair;
            if (ce >= 4096) {
                if (!strengthIsPrimary || (-67108864 & pair) != 0) {
                    return pair & TWO_CASES_MASK;
                }
                return pair & 24;
            } else if (ce > variableTop) {
                return TWO_LOWER_CASES;
            } else {
                return 0;
            }
        } else if (pair >= 4096) {
            int ce2 = pair;
            int pair2 = pair & 24;
            if (strengthIsPrimary || (ce2 & 992) < 384) {
                return pair2;
            }
            return pair2 | 524288;
        } else if (pair > variableTop) {
            return 8;
        } else {
            if (pair >= MIN_LONG) {
                return 0;
            }
            return pair;
        }
    }

    private static int getTertiaries(int variableTop, boolean withCaseBits, int pair) {
        int pair2;
        int pair3;
        if (pair > 65535) {
            int ce = 65535 & pair;
            if (ce >= 4096) {
                if (withCaseBits) {
                    pair2 = pair & 2031647;
                } else {
                    pair2 = pair & TWO_TERTIARIES_MASK;
                }
                return pair2 + 2097184;
            } else if (ce <= variableTop) {
                return 0;
            } else {
                int pair4 = (pair & TWO_TERTIARIES_MASK) + 2097184;
                if (withCaseBits) {
                    return pair4 | TWO_LOWER_CASES;
                }
                return pair4;
            }
        } else if (pair >= 4096) {
            int ce2 = pair;
            if (withCaseBits) {
                pair3 = (pair & 31) + 32;
                if ((ce2 & 992) >= 384) {
                    return 2621440 | pair3;
                }
            } else {
                pair3 = (pair & 7) + 32;
                if ((ce2 & 992) >= 384) {
                    return 2097152 | pair3;
                }
            }
            return pair3;
        } else if (pair > variableTop) {
            int pair5 = (pair & 7) + 32;
            if (withCaseBits) {
                return pair5 | 8;
            }
            return pair5;
        } else if (pair >= MIN_LONG) {
            return 0;
        } else {
            return pair;
        }
    }

    private static int getQuaternaries(int variableTop, int pair) {
        if (pair <= 65535) {
            if (pair >= 4096) {
                if ((pair & 992) >= 384) {
                    return TWO_SHORT_PRIMARIES_MASK;
                }
                return Normalizer2Impl.MIN_NORMAL_MAYBE_YES;
            } else if (pair > variableTop) {
                return Normalizer2Impl.MIN_NORMAL_MAYBE_YES;
            } else {
                if (pair >= MIN_LONG) {
                    return pair & LONG_PRIMARY_MASK;
                }
                return pair;
            }
        } else if ((65535 & pair) > variableTop) {
            return TWO_SHORT_PRIMARIES_MASK;
        } else {
            return pair & TWO_LONG_PRIMARIES_MASK;
        }
    }

    private CollationFastLatin() {
    }
}
