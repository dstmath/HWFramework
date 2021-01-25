package ohos.global.icu.impl.coll;

import ohos.global.icu.impl.Normalizer2Impl;

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

    private static int getCases(int i, boolean z, int i2) {
        if (i2 > 65535) {
            int i3 = 65535 & i2;
            if (i3 >= 4096) {
                return (!z || (-67108864 & i2) != 0) ? i2 & TWO_CASES_MASK : i2 & 24;
            }
            if (i3 > i) {
                return TWO_LOWER_CASES;
            }
        } else if (i2 >= 4096) {
            int i4 = i2 & 24;
            if (!z && (i2 & 992) >= 384) {
                i4 |= 524288;
            }
            return i4;
        } else if (i2 > i) {
            return 8;
        } else {
            if (i2 < MIN_LONG) {
                return i2;
            }
        }
        return 0;
    }

    static int getCharIndex(char c) {
        if (c <= 383) {
            return c;
        }
        if (8192 > c || c >= PUNCT_LIMIT) {
            return -1;
        }
        return c - 7808;
    }

    private static int getPrimaries(int i, int i2) {
        int i3;
        int i4 = 65535 & i2;
        if (i4 >= 4096) {
            i3 = TWO_SHORT_PRIMARIES_MASK;
        } else if (i4 > i) {
            i3 = TWO_LONG_PRIMARIES_MASK;
        } else if (i4 >= MIN_LONG) {
            return 0;
        } else {
            return i2;
        }
        return i3 & i2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0013, code lost:
        if ((r4 & 992) >= 384) goto L_0x0026;
     */
    private static int getQuaternaries(int i, int i2) {
        int i3;
        if (i2 <= 65535) {
            if (i2 < 4096) {
                if (i2 <= i) {
                    if (i2 < MIN_LONG) {
                        return i2;
                    }
                    i3 = LONG_PRIMARY_MASK;
                    return i2 & i3;
                }
            }
            return Normalizer2Impl.MIN_NORMAL_MAYBE_YES;
        } else if ((i2 & 65535) <= i) {
            i3 = TWO_LONG_PRIMARIES_MASK;
            return i2 & i3;
        }
        return TWO_SHORT_PRIMARIES_MASK;
    }

    private static int getSecondariesFromOneShortCE(int i) {
        int i2 = i & 992;
        return i2 < 384 ? i2 + 32 : ((i2 + 32) << 16) | 192;
    }

    private static int getTertiaries(int i, boolean z, int i2) {
        int i3;
        int i4;
        if (i2 > 65535) {
            int i5 = 65535 & i2;
            if (i5 >= 4096) {
                return (z ? 2031647 & i2 : i2 & TWO_TERTIARIES_MASK) + 2097184;
            } else if (i5 > i) {
                int i6 = (i2 & TWO_TERTIARIES_MASK) + 2097184;
                return z ? i6 | TWO_LOWER_CASES : i6;
            }
        } else if (i2 >= 4096) {
            if (z) {
                i3 = (i2 & 31) + 32;
                if ((i2 & 992) >= 384) {
                    i4 = 2621440;
                }
                return i3;
            }
            i3 = (i2 & 7) + 32;
            if ((i2 & 992) >= 384) {
                i4 = 2097152;
            }
            return i3;
            return i4 | i3;
        } else if (i2 > i) {
            int i7 = (i2 & 7) + 32;
            return z ? i7 | 8 : i7;
        } else if (i2 < MIN_LONG) {
            return i2;
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x0091  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00b7 A[LOOP:2: B:61:0x00b3->B:63:0x00b7, LOOP_END] */
    public static int getOptions(CollationData collationData, CollationSettings collationSettings, char[] cArr) {
        char c;
        boolean z;
        int i;
        int i2;
        int i3;
        char c2;
        char[] cArr2 = collationData.fastLatinTableHeader;
        if (cArr2 == null || cArr.length != 384) {
            return -1;
        }
        if ((collationSettings.options & 12) == 0) {
            c = 3071;
        } else {
            int i4 = cArr2[0] & 255;
            int maxVariable = collationSettings.getMaxVariable() + 1;
            if (maxVariable >= i4) {
                return -1;
            }
            c = cArr2[maxVariable];
        }
        if (collationSettings.hasReordering()) {
            long j = 0;
            long j2 = 0;
            long j3 = 0;
            long j4 = 0;
            for (int i5 = 4096; i5 < 4104; i5++) {
                long reorder = collationSettings.reorder(collationData.getFirstPrimaryForGroup(i5));
                if (i5 == 4100) {
                    j4 = reorder;
                    j3 = j;
                } else if (reorder == 0) {
                    continue;
                } else if (reorder < j) {
                    return -1;
                } else {
                    if (j4 != 0 && j2 == 0 && j == j3) {
                        j2 = reorder;
                    }
                    j = reorder;
                }
            }
            long reorder2 = collationSettings.reorder(collationData.getFirstPrimaryForGroup(25));
            if (reorder2 < j) {
                return -1;
            }
            if (j2 == 0) {
                j2 = reorder2;
            }
            if (j3 >= j4 || j4 >= j2) {
                z = true;
                char[] cArr3 = collationData.fastLatinTable;
                for (i = 0; i < 384; i++) {
                    char c3 = cArr3[i];
                    if (c3 >= 4096) {
                        c2 = 64512;
                    } else if (c3 > c) {
                        c2 = 65528;
                    } else {
                        i3 = 0;
                        cArr[i] = (char) i3;
                    }
                    i3 = c2 & c3;
                    cArr[i] = (char) i3;
                }
                if (z || (collationSettings.options & 2) != 0) {
                    for (i2 = 48; i2 <= 57; i2++) {
                        cArr[i2] = 0;
                    }
                }
                return (c << 16) | collationSettings.options;
            }
        }
        z = false;
        char[] cArr32 = collationData.fastLatinTable;
        while (i < 384) {
        }
        while (i2 <= 57) {
        }
        return (c << 16) | collationSettings.options;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:156:0x020b */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:326:0x0246 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:204:0x029f */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:338:0x02da */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:254:0x032c */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:350:0x0367 */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x01af, code lost:
        if (r8 != 2) goto L_0x01b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:225:0x02e2, code lost:
        r13 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00e7, code lost:
        if (r7 != 2) goto L_0x00ea;
     */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x015a  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x01b6  */
    /* JADX WARNING: Removed duplicated region for block: B:306:0x01ae A[EDGE_INSN: B:306:0x01ae->B:127:0x01ae ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:311:0x01a5 A[SYNTHETIC] */
    public static int compareUTF16(char[] cArr, char[] cArr2, int i, CharSequence charSequence, CharSequence charSequence2, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        int i12;
        char c;
        char c2;
        char c3;
        int i13;
        char c4;
        int i14;
        int i15 = i >> 16;
        int i16 = i & 65535;
        int i17 = i2;
        int i18 = i17;
        loop0:
        while (true) {
            int i19 = 0;
            int i20 = 0;
            while (true) {
                int i21 = 4096;
                char c5 = 8256;
                if (i19 == 0) {
                    if (i17 == charSequence.length()) {
                        i19 = 2;
                    } else {
                        int i22 = i17 + 1;
                        char charAt = charSequence.charAt(i17);
                        if (charAt <= 383) {
                            char c6 = cArr2[charAt];
                            if (c6 != 0) {
                                i17 = i22;
                                i19 = c6;
                            } else if (charAt <= '9' && charAt >= '0' && (i16 & 2) != 0) {
                                return -2;
                            } else {
                                c4 = cArr[charAt];
                            }
                        } else if (8192 > charAt || charAt >= PUNCT_LIMIT) {
                            c4 = lookup(cArr, charAt);
                        } else {
                            c4 = cArr[(charAt - 8192) + 384];
                        }
                        if (c4 >= 4096) {
                            i14 = Normalizer2Impl.MIN_NORMAL_MAYBE_YES;
                        } else if (c4 > i15) {
                            i14 = LONG_PRIMARY_MASK;
                        } else {
                            long nextPair = nextPair(cArr, charAt, c4, charSequence, i22);
                            if (nextPair < 0) {
                                i22++;
                                nextPair = ~nextPair;
                            }
                            i17 = i22;
                            int i23 = (int) nextPair;
                            if (i23 == 1) {
                                return -2;
                            }
                            i19 = getPrimaries(i15, i23);
                        }
                        int i24 = i14 & c4;
                        i17 = i22;
                        i19 = i24;
                    }
                }
                while (true) {
                    if (i20 != 0) {
                        i3 = i18;
                        i4 = i20;
                        break;
                    } else if (i18 == charSequence2.length()) {
                        i3 = i18;
                        i4 = 2;
                        break;
                    } else {
                        i3 = i18 + 1;
                        char charAt2 = charSequence2.charAt(i18);
                        if (charAt2 <= 383) {
                            char c7 = cArr2[charAt2];
                            if (c7 != 0) {
                                i4 = c7;
                                break;
                            } else if (charAt2 <= '9' && charAt2 >= '0' && (i16 & 2) != 0) {
                                return -2;
                            } else {
                                c3 = cArr[charAt2];
                            }
                        } else if (8192 > charAt2 || charAt2 >= c5) {
                            c3 = lookup(cArr, charAt2);
                        } else {
                            c3 = cArr[(charAt2 - 8192) + 384];
                        }
                        if (c3 >= i21) {
                            i13 = Normalizer2Impl.MIN_NORMAL_MAYBE_YES;
                            break;
                        } else if (c3 > i15) {
                            i13 = LONG_PRIMARY_MASK;
                            break;
                        } else {
                            long nextPair2 = nextPair(cArr, charAt2, c3, charSequence2, i3);
                            if (nextPair2 < 0) {
                                i3++;
                                nextPair2 = ~nextPair2;
                            }
                            i18 = i3;
                            int i25 = (int) nextPair2;
                            if (i25 == 1) {
                                return -2;
                            }
                            i20 = getPrimaries(i15, i25);
                            i21 = 4096;
                            c5 = 8256;
                        }
                    }
                }
                i4 = i13 & c3;
                if (i19 == i4) {
                    break;
                }
                int i26 = i19 & 65535;
                int i27 = i4 & 65535;
                if (i26 != i27) {
                    if (i26 < i27) {
                        return -1;
                    }
                    return 1;
                } else if (i19 == 2) {
                    break loop0;
                } else {
                    i19 >>>= 16;
                    int i28 = i4 >>> 16;
                    i18 = i3;
                    i20 = i28;
                }
            }
            i18 = i3;
        }
        if (CollationSettings.getStrength(i16) >= 1) {
            int i29 = i2;
            int i30 = i29;
            loop3:
            while (true) {
                int i31 = 0;
                int i32 = 0;
                while (true) {
                    if (i31 == 0) {
                        if (i29 == charSequence.length()) {
                            i10 = i29;
                            i9 = 2;
                        } else {
                            i10 = i29 + 1;
                            char charAt3 = charSequence.charAt(i29);
                            if (charAt3 <= 383) {
                                c2 = cArr[charAt3];
                            } else if (8192 > charAt3 || charAt3 >= PUNCT_LIMIT) {
                                c2 = lookup(cArr, charAt3);
                            } else {
                                c2 = cArr[(charAt3 - 8192) + 384];
                            }
                            if (c2 >= 4096) {
                                i9 = getSecondariesFromOneShortCE(c2);
                            } else if (c2 > i15) {
                                i9 = 192;
                            } else {
                                long nextPair3 = nextPair(cArr, charAt3, c2, charSequence, i10);
                                if (nextPair3 < 0) {
                                    i10++;
                                    nextPair3 = ~nextPair3;
                                }
                                i29 = i10;
                                i31 = getSecondaries(i15, (int) nextPair3);
                            }
                        }
                        while (true) {
                            if (i32 == 0) {
                                i11 = i30;
                                i12 = i32;
                                break;
                            } else if (i30 == charSequence2.length()) {
                                i11 = i30;
                                i12 = 2;
                                break;
                            } else {
                                i11 = i30 + 1;
                                char charAt4 = charSequence2.charAt(i30);
                                if (charAt4 <= 383) {
                                    c = cArr[charAt4];
                                } else if (8192 > charAt4 || charAt4 >= PUNCT_LIMIT) {
                                    c = lookup(cArr, charAt4);
                                } else {
                                    c = cArr[(charAt4 - 8192) + 384];
                                }
                                if (c >= 4096) {
                                    i12 = getSecondariesFromOneShortCE(c);
                                    break;
                                } else if (c > i15) {
                                    i12 = 192;
                                    break;
                                } else {
                                    long nextPair4 = nextPair(cArr, charAt4, c, charSequence2, i11);
                                    if (nextPair4 < 0) {
                                        i11++;
                                        nextPair4 = ~nextPair4;
                                    }
                                    i30 = i11;
                                    i32 = getSecondaries(i15, (int) nextPair4);
                                }
                            }
                        }
                        if (i9 != i12) {
                            break;
                        }
                        int i33 = i9 & 65535;
                        int i34 = i12 & 65535;
                        if (i33 != i34) {
                            if ((i16 & 2048) != 0) {
                                return -2;
                            }
                            if (i33 < i34) {
                                return -1;
                            }
                            return 1;
                        } else if (i9 == 2) {
                            break loop3;
                        } else {
                            int i35 = i9 >>> 16;
                            i29 = i10;
                            i31 = i35;
                            i32 = i12 >>> 16;
                            i30 = i11;
                        }
                    } else {
                        i9 = i31;
                        i10 = i29;
                        while (true) {
                            if (i32 == 0) {
                            }
                            i30 = i11;
                            i32 = getSecondaries(i15, (int) nextPair4);
                        }
                        if (i9 != i12) {
                        }
                    }
                }
                i29 = i10;
                i30 = i11;
            }
        }
        if ((i16 & 1024) != 0) {
            boolean z = CollationSettings.getStrength(i16) == 0;
            int i36 = i2;
            int i37 = i36;
            loop6:
            do {
                i8 = 0;
                int i38 = 0;
                while (true) {
                    if (i8 == 0) {
                        if (i36 == charSequence.length()) {
                            i8 = 2;
                        } else {
                            int i39 = i36 + 1;
                            char charAt5 = charSequence.charAt(i36);
                            int lookup = charAt5 <= 383 ? cArr[charAt5] : lookup(cArr, charAt5);
                            if (lookup < MIN_LONG) {
                                long nextPair5 = nextPair(cArr, charAt5, lookup, charSequence, i39);
                                if (nextPair5 < 0) {
                                    i39++;
                                    nextPair5 = ~nextPair5;
                                }
                                lookup = (int) nextPair5;
                            }
                            i36 = i39;
                            i8 = getCases(i15, z, lookup == 1 ? 1 : 0);
                        }
                    }
                    while (true) {
                        if (i38 != 0) {
                            break;
                        } else if (i37 == charSequence2.length()) {
                            i38 = 2;
                            break;
                        } else {
                            int i40 = i37 + 1;
                            char charAt6 = charSequence2.charAt(i37);
                            int lookup2 = charAt6 <= 383 ? cArr[charAt6] : lookup(cArr, charAt6);
                            if (lookup2 < MIN_LONG) {
                                long nextPair6 = nextPair(cArr, charAt6, lookup2, charSequence2, i40);
                                if (nextPair6 < 0) {
                                    i40++;
                                    nextPair6 = ~nextPair6;
                                }
                                lookup2 = (int) nextPair6;
                            }
                            i37 = i40;
                            i38 = getCases(i15, z, lookup2 == 1 ? 1 : 0);
                        }
                    }
                    if (i8 == i38) {
                        break;
                    }
                    int i41 = i8 & 65535;
                    int i42 = i38 & 65535;
                    if (i41 != i42) {
                        if ((i16 & 256) == 0) {
                            if (i41 < i42) {
                                return -1;
                            }
                            return 1;
                        } else if (i41 < i42) {
                            return 1;
                        } else {
                            return -1;
                        }
                    } else if (i8 == 2) {
                        break loop6;
                    } else {
                        i8 >>>= 16;
                        i38 >>>= 16;
                    }
                }
            } while (i8 != 2);
            break;
        }
        if (CollationSettings.getStrength(i16) <= 1) {
            return 0;
        }
        boolean isTertiaryWithCaseBits = CollationSettings.isTertiaryWithCaseBits(i16);
        int i43 = i2;
        int i44 = i43;
        loop9:
        do {
            i5 = 0;
            int i45 = 0;
            while (true) {
                if (i5 == 0) {
                    if (i43 == charSequence.length()) {
                        i5 = 2;
                    } else {
                        int i46 = i43 + 1;
                        char charAt7 = charSequence.charAt(i43);
                        int lookup3 = charAt7 <= 383 ? cArr[charAt7] : lookup(cArr, charAt7);
                        if (lookup3 < MIN_LONG) {
                            long nextPair7 = nextPair(cArr, charAt7, lookup3, charSequence, i46);
                            if (nextPair7 < 0) {
                                i46++;
                                nextPair7 = ~nextPair7;
                            }
                            lookup3 = (int) nextPair7;
                        }
                        i43 = i46;
                        i5 = getTertiaries(i15, isTertiaryWithCaseBits, lookup3 == 1 ? 1 : 0);
                    }
                }
                while (true) {
                    if (i45 != 0) {
                        break;
                    } else if (i44 == charSequence2.length()) {
                        i45 = 2;
                        break;
                    } else {
                        int i47 = i44 + 1;
                        char charAt8 = charSequence2.charAt(i44);
                        int lookup4 = charAt8 <= 383 ? cArr[charAt8] : lookup(cArr, charAt8);
                        if (lookup4 < MIN_LONG) {
                            long nextPair8 = nextPair(cArr, charAt8, lookup4, charSequence2, i47);
                            if (nextPair8 < 0) {
                                i47++;
                                nextPair8 = ~nextPair8;
                            }
                            lookup4 = (int) nextPair8;
                        }
                        i44 = i47;
                        i45 = getTertiaries(i15, isTertiaryWithCaseBits, lookup4 == 1 ? 1 : 0);
                    }
                }
                if (i5 == i45) {
                    break;
                }
                int i48 = i5 & 65535;
                int i49 = i45 & 65535;
                if (i48 != i49) {
                    if (CollationSettings.sortsTertiaryUpperCaseFirst(i16)) {
                        if (i48 > 3) {
                            i48 ^= 24;
                        }
                        if (i49 > 3) {
                            i49 ^= 24;
                        }
                    }
                    if (i48 < i49) {
                        return -1;
                    }
                    return 1;
                }
                i6 = 2;
                if (i5 == 2) {
                    break loop9;
                }
                i5 >>>= 16;
                i45 >>>= 16;
            }
        } while (i5 != 2);
        if (CollationSettings.getStrength(i16) <= i6) {
            return 0;
        }
        int i50 = i2;
        int i51 = i50;
        do {
            i7 = 0;
            int i52 = 0;
            while (true) {
                if (i7 == 0) {
                    if (i50 == charSequence.length()) {
                        i7 = 2;
                    } else {
                        int i53 = i50 + 1;
                        char charAt9 = charSequence.charAt(i50);
                        int lookup5 = charAt9 <= 383 ? cArr[charAt9] : lookup(cArr, charAt9);
                        if (lookup5 < MIN_LONG) {
                            long nextPair9 = nextPair(cArr, charAt9, lookup5, charSequence, i53);
                            if (nextPair9 < 0) {
                                i53++;
                                nextPair9 = ~nextPair9;
                            }
                            lookup5 = (int) nextPair9;
                        }
                        i50 = i53;
                        i7 = getQuaternaries(i15, lookup5 == 1 ? 1 : 0);
                    }
                }
                while (true) {
                    if (i52 != 0) {
                        break;
                    } else if (i51 == charSequence2.length()) {
                        i52 = 2;
                        break;
                    } else {
                        int i54 = i51 + 1;
                        char charAt10 = charSequence2.charAt(i51);
                        int lookup6 = charAt10 <= 383 ? cArr[charAt10] : lookup(cArr, charAt10);
                        if (lookup6 < MIN_LONG) {
                            long nextPair10 = nextPair(cArr, charAt10, lookup6, charSequence2, i54);
                            if (nextPair10 < 0) {
                                i54++;
                                nextPair10 = ~nextPair10;
                            }
                            lookup6 = (int) nextPair10;
                        }
                        i51 = i54;
                        i52 = getQuaternaries(i15, lookup6 == 1 ? 1 : 0);
                    }
                }
                if (i7 != i52) {
                    int i55 = i7 & 65535;
                    int i56 = i52 & 65535;
                    if (i55 != i56) {
                        if (i55 < i56) {
                            return -1;
                        }
                        return 1;
                    } else if (i7 == 2) {
                        return 0;
                    } else {
                        i7 >>>= 16;
                        i52 >>>= 16;
                    }
                }
            }
        } while (i7 != 2);
        return 0;
    }

    private static int lookup(char[] cArr, int i) {
        if (8192 <= i && i < PUNCT_LIMIT) {
            return cArr[(i - 8192) + 384];
        }
        if (i == 65534) {
            return 3;
        }
        return i == 65535 ? 64680 : 1;
    }

    private static long nextPair(char[] cArr, int i, int i2, CharSequence charSequence, int i3) {
        long j;
        int i4;
        if (i2 >= MIN_LONG || i2 < 1024) {
            return (long) i2;
        }
        if (i2 >= 2048) {
            int i5 = (i2 & 1023) + 448;
            return ((long) cArr[i5]) | (((long) cArr[i5 + 1]) << 16);
        }
        int i6 = (i2 & 1023) + 448;
        boolean z = false;
        if (i3 != charSequence.length()) {
            int charAt = charSequence.charAt(i3);
            if (charAt > 383) {
                if (8192 <= charAt && charAt < PUNCT_LIMIT) {
                    charAt = (charAt - 8192) + 384;
                } else if (charAt != 65534 && charAt != 65535) {
                    return 1;
                } else {
                    charAt = -1;
                }
            }
            char c = cArr[i6];
            int i7 = i6;
            do {
                i7 += c >> '\t';
                c = cArr[i7];
                i4 = c & 511;
            } while (i4 < charAt);
            if (i4 == charAt) {
                i6 = i7;
                z = true;
            }
        }
        int i8 = cArr[i6] >> '\t';
        if (i8 == 1) {
            return 1;
        }
        char c2 = cArr[i6 + 1];
        if (i8 == 2) {
            j = (long) c2;
        } else {
            j = (((long) cArr[i6 + 2]) << 16) | ((long) c2);
        }
        return z ? ~j : j;
    }

    private static int getSecondaries(int i, int i2) {
        if (i2 > 65535) {
            int i3 = 65535 & i2;
            if (i3 >= 4096) {
                return 2097184 + (TWO_SECONDARIES_MASK & i2);
            }
            if (i3 > i) {
                return TWO_COMMON_SEC_PLUS_OFFSET;
            }
        } else if (i2 >= 4096) {
            return getSecondariesFromOneShortCE(i2);
        } else {
            if (i2 > i) {
                return 192;
            }
            if (i2 < MIN_LONG) {
                return i2;
            }
        }
        return 0;
    }

    private CollationFastLatin() {
    }
}
