package ohos.global.icu.impl.coll;

import java.lang.reflect.Array;
import ohos.devtools.JLogConstants;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.global.icu.lang.UProperty;
import ohos.global.icu.util.CharsTrie;

final class CollationFastLatinBuilder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long CONTRACTION_FLAG = 2147483648L;
    private static final int NUM_SPECIAL_GROUPS = 4;
    private long ce0 = 0;
    private long ce1 = 0;
    private long[][] charCEs = ((long[][]) Array.newInstance(long.class, JLogConstants.JLID_ABILITY_SHELL_ONDISCONNECT, 2));
    private UVector64 contractionCEs = new UVector64();
    private long firstDigitPrimary = 0;
    private long firstLatinPrimary = 0;
    private long firstShortPrimary = 0;
    private int headerLength = 0;
    private long lastLatinPrimary = 0;
    long[] lastSpecialPrimaries = new long[4];
    private char[] miniCEs = null;
    private StringBuilder result = new StringBuilder();
    private boolean shortPrimaryOverflow = false;
    private UVector64 uniqueCEs = new UVector64();

    private static final int compareInt64AsUnsigned(long j, long j2) {
        int i = ((j - Long.MIN_VALUE) > (j2 - Long.MIN_VALUE) ? 1 : ((j - Long.MIN_VALUE) == (j2 - Long.MIN_VALUE) ? 0 : -1));
        if (i < 0) {
            return -1;
        }
        return i > 0 ? 1 : 0;
    }

    private static boolean isContractionCharCE(long j) {
        return (j >>> 32) == 1 && j != Collation.NO_CE;
    }

    private static final int binarySearch(long[] jArr, int i, long j) {
        if (i == 0) {
            return -1;
        }
        int i2 = 0;
        while (true) {
            int i3 = (int) ((((long) i2) + ((long) i)) / 2);
            int compareInt64AsUnsigned = compareInt64AsUnsigned(j, jArr[i3]);
            if (compareInt64AsUnsigned == 0) {
                return i3;
            }
            if (compareInt64AsUnsigned < 0) {
                if (i3 == i2) {
                    return ~i2;
                }
                i = i3;
            } else if (i3 == i2) {
                return ~(i2 + 1);
            } else {
                i2 = i3;
            }
        }
    }

    CollationFastLatinBuilder() {
    }

    /* access modifiers changed from: package-private */
    public boolean forData(CollationData collationData) {
        if (this.result.length() != 0) {
            throw new IllegalStateException("attempt to reuse a CollationFastLatinBuilder");
        } else if (!loadGroups(collationData)) {
            return false;
        } else {
            this.firstShortPrimary = this.firstDigitPrimary;
            getCEs(collationData);
            encodeUniqueCEs();
            if (this.shortPrimaryOverflow) {
                this.firstShortPrimary = this.firstLatinPrimary;
                resetCEs();
                getCEs(collationData);
                encodeUniqueCEs();
            }
            boolean z = !this.shortPrimaryOverflow;
            if (z) {
                encodeCharCEs();
                encodeContractions();
            }
            this.contractionCEs.removeAllElements();
            this.uniqueCEs.removeAllElements();
            return z;
        }
    }

    /* access modifiers changed from: package-private */
    public char[] getHeader() {
        int i = this.headerLength;
        char[] cArr = new char[i];
        this.result.getChars(0, i, cArr, 0);
        return cArr;
    }

    /* access modifiers changed from: package-private */
    public char[] getTable() {
        int length = this.result.length();
        int i = this.headerLength;
        char[] cArr = new char[(length - i)];
        StringBuilder sb = this.result;
        sb.getChars(i, sb.length(), cArr, 0);
        return cArr;
    }

    private boolean loadGroups(CollationData collationData) {
        this.headerLength = 5;
        this.result.append((char) (this.headerLength | 512));
        for (int i = 0; i < 4; i++) {
            this.lastSpecialPrimaries[i] = collationData.getLastPrimaryForGroup(i + 4096);
            if (this.lastSpecialPrimaries[i] == 0) {
                return false;
            }
            this.result.append(0);
        }
        this.firstDigitPrimary = collationData.getFirstPrimaryForGroup(UProperty.EAST_ASIAN_WIDTH);
        this.firstLatinPrimary = collationData.getFirstPrimaryForGroup(25);
        this.lastLatinPrimary = collationData.getLastPrimaryForGroup(25);
        if (this.firstDigitPrimary == 0 || this.firstLatinPrimary == 0) {
            return false;
        }
        return true;
    }

    private boolean inSameGroup(long j, long j2) {
        long j3 = this.firstShortPrimary;
        if (j >= j3) {
            return j2 >= j3;
        }
        if (j2 >= j3) {
            return false;
        }
        long j4 = this.lastSpecialPrimaries[3];
        if (j > j4) {
            return j2 > j4;
        }
        if (j2 > j4) {
            return false;
        }
        int i = 0;
        while (true) {
            long j5 = this.lastSpecialPrimaries[i];
            if (j <= j5) {
                return j2 <= j5;
            }
            if (j2 <= j5) {
                return false;
            }
            i++;
        }
    }

    private void resetCEs() {
        this.contractionCEs.removeAllElements();
        this.uniqueCEs.removeAllElements();
        this.shortPrimaryOverflow = false;
        this.result.setLength(this.headerLength);
    }

    private void getCEs(CollationData collationData) {
        int i;
        CollationData collationData2;
        char c = 0;
        int i2 = 0;
        while (true) {
            if (c == 384) {
                c = 8192;
            } else if (c == 8256) {
                this.contractionCEs.addElement(511);
                return;
            }
            int ce32 = collationData.getCE32(c);
            if (ce32 == 192) {
                collationData2 = collationData.base;
                i = collationData2.getCE32(c);
            } else {
                i = ce32;
                collationData2 = collationData;
            }
            if (getCEsFromCE32(collationData2, c, i)) {
                long[][] jArr = this.charCEs;
                long[] jArr2 = jArr[i2];
                long j = this.ce0;
                jArr2[0] = j;
                jArr[i2][1] = this.ce1;
                addUniqueCE(j);
                addUniqueCE(this.ce1);
            } else {
                long[][] jArr3 = this.charCEs;
                long[] jArr4 = jArr3[i2];
                this.ce0 = Collation.NO_CE;
                jArr4[0] = 4311744768L;
                long[] jArr5 = jArr3[i2];
                this.ce1 = 0;
                jArr5[1] = 0;
            }
            if (c == 0 && !isContractionCharCE(this.ce0)) {
                addContractionEntry(DMSDPConfig.DISCOVER_SERVICE_FILTER_ALL, this.ce0, this.ce1);
                long[][] jArr6 = this.charCEs;
                jArr6[0][0] = 6442450944L;
                jArr6[0][1] = 0;
            }
            i2++;
            c = (char) (c + 1);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x00d8 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00d9 A[ADDED_TO_REGION] */
    private boolean getCEsFromCE32(CollationData collationData, int i, int i2) {
        int i3;
        long j;
        int i4;
        int i5;
        int finalCE32 = collationData.getFinalCE32(i2);
        this.ce1 = 0;
        if (Collation.isSimpleOrLongCE32(finalCE32)) {
            this.ce0 = Collation.ceFromCE32(finalCE32);
        } else {
            int tagFromCE32 = Collation.tagFromCE32(finalCE32);
            if (tagFromCE32 == 4) {
                this.ce0 = Collation.latinCE0FromCE32(finalCE32);
                this.ce1 = Collation.latinCE1FromCE32(finalCE32);
            } else if (tagFromCE32 == 5) {
                int indexFromCE32 = Collation.indexFromCE32(finalCE32);
                int lengthFromCE32 = Collation.lengthFromCE32(finalCE32);
                if (lengthFromCE32 > 2) {
                    return false;
                }
                this.ce0 = Collation.ceFromCE32(collationData.ce32s[indexFromCE32]);
                if (lengthFromCE32 == 2) {
                    this.ce1 = Collation.ceFromCE32(collationData.ce32s[indexFromCE32 + 1]);
                }
            } else if (tagFromCE32 == 6) {
                int indexFromCE322 = Collation.indexFromCE32(finalCE32);
                int lengthFromCE322 = Collation.lengthFromCE32(finalCE32);
                if (lengthFromCE322 > 2) {
                    return false;
                }
                this.ce0 = collationData.ces[indexFromCE322];
                if (lengthFromCE322 == 2) {
                    this.ce1 = collationData.ces[indexFromCE322 + 1];
                }
            } else if (tagFromCE32 == 9) {
                return getCEsFromContractionCE32(collationData, finalCE32);
            } else {
                if (tagFromCE32 != 14) {
                    return false;
                }
                this.ce0 = collationData.getCEFromOffsetCE32(i, finalCE32);
            }
        }
        long j2 = this.ce0;
        if (j2 == 0) {
            return this.ce1 == 0;
        }
        long j3 = j2 >>> 32;
        if (j3 == 0 || j3 > this.lastLatinPrimary) {
            return false;
        }
        int i6 = (int) j2;
        if ((j3 < this.firstShortPrimary && (i6 & -16384) != 83886080) || (i3 = i6 & Collation.ONLY_TERTIARY_MASK) < 1280) {
            return false;
        }
        long j4 = this.ce1;
        if (j4 != 0) {
            if ((j = j4 >>> 32) == 0) {
                i5 = (int) this.ce1;
                if ((i5 >>> 16) != 0) {
                    return false;
                }
                if (!(i4 == 0 || j >= this.firstShortPrimary || (i5 & -16384) == 83886080) || i3 < 1280) {
                    return false;
                }
            } else {
                i5 = (int) this.ce1;
                if ((i5 >>> 16) != 0) {
                }
            }
            return false;
        }
        return ((this.ce0 | this.ce1) & 192) == 0;
    }

    private boolean getCEsFromContractionCE32(CollationData collationData, int i) {
        boolean z;
        int indexFromCE32 = Collation.indexFromCE32(i);
        int cE32FromContexts = collationData.getCE32FromContexts(indexFromCE32);
        int size = this.contractionCEs.size();
        if (getCEsFromCE32(collationData, -1, cE32FromContexts)) {
            addContractionEntry(DMSDPConfig.DISCOVER_SERVICE_FILTER_ALL, this.ce0, this.ce1);
        } else {
            addContractionEntry(DMSDPConfig.DISCOVER_SERVICE_FILTER_ALL, Collation.NO_CE, 0);
        }
        CharsTrie.Iterator it = CharsTrie.iterator(collationData.contexts, indexFromCE32 + 2, 0);
        int i2 = -1;
        loop0:
        while (true) {
            z = false;
            while (it.hasNext()) {
                CharsTrie.Entry next = it.next();
                CharSequence charSequence = next.chars;
                int charIndex = CollationFastLatin.getCharIndex(charSequence.charAt(0));
                if (charIndex >= 0) {
                    if (charIndex != i2) {
                        if (z) {
                            i2 = charIndex;
                            addContractionEntry(i2, this.ce0, this.ce1);
                        } else {
                            i2 = charIndex;
                        }
                        int i3 = next.value;
                        if (charSequence.length() != 1 || !getCEsFromCE32(collationData, -1, i3)) {
                            addContractionEntry(i2, Collation.NO_CE, 0);
                        } else {
                            z = true;
                        }
                    } else if (z) {
                        addContractionEntry(charIndex, Collation.NO_CE, 0);
                    }
                }
            }
            break loop0;
        }
        if (z) {
            addContractionEntry(i2, this.ce0, this.ce1);
        }
        this.ce0 = 6442450944L | ((long) size);
        this.ce1 = 0;
        return true;
    }

    private void addContractionEntry(int i, long j, long j2) {
        this.contractionCEs.addElement((long) i);
        this.contractionCEs.addElement(j);
        this.contractionCEs.addElement(j2);
        addUniqueCE(j);
        addUniqueCE(j2);
    }

    private void addUniqueCE(long j) {
        long j2;
        int binarySearch;
        if (j != 0 && (j >>> 32) != 1 && (binarySearch = binarySearch(this.uniqueCEs.getBuffer(), this.uniqueCEs.size(), (j2 = j & -49153))) < 0) {
            this.uniqueCEs.insertElementAt(j2, ~binarySearch);
        }
    }

    private int getMiniCE(long j) {
        return this.miniCEs[binarySearch(this.uniqueCEs.getBuffer(), this.uniqueCEs.size(), j & -49153)];
    }

    private void encodeUniqueCEs() {
        int i;
        int i2;
        this.miniCEs = new char[this.uniqueCEs.size()];
        long j = this.lastSpecialPrimaries[0];
        long j2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        for (int i8 = 0; i8 < this.uniqueCEs.size(); i8++) {
            long elementAti = this.uniqueCEs.elementAti(i8);
            long j3 = elementAti >>> 32;
            if (j3 != j2) {
                while (true) {
                    if (j3 <= j) {
                        break;
                    }
                    i4++;
                    this.result.setCharAt(i4, (char) i3);
                    if (i4 >= 4) {
                        j = 4294967295L;
                        break;
                    }
                    j = this.lastSpecialPrimaries[i4];
                }
                if (j3 < this.firstShortPrimary) {
                    if (i3 == 0) {
                        i3 = 3072;
                    } else if (i3 < 4088) {
                        i3 += 8;
                    } else {
                        this.miniCEs[i8] = 1;
                        j = j;
                    }
                } else if (i3 < 4096) {
                    i3 = 4096;
                } else if (i3 < 63488) {
                    i3 += 1024;
                } else {
                    this.shortPrimaryOverflow = true;
                    this.miniCEs[i8] = 1;
                    j = j;
                }
                j2 = j3;
                j = j;
                i = 0;
                i5 = 1280;
                i6 = 160;
            } else {
                i = i7;
            }
            int i9 = (int) elementAti;
            int i10 = i9 >>> 16;
            if (i10 != i5) {
                if (i3 != 0) {
                    if (i10 >= 1280) {
                        int i11 = 160;
                        if (i10 != 1280) {
                            i11 = 192;
                            if (i6 >= 192) {
                                if (i6 >= 352) {
                                    this.miniCEs[i8] = 1;
                                    i7 = i;
                                }
                            }
                        }
                        i6 = i11;
                        i5 = i10;
                        i = 0;
                    } else if (i6 == 160) {
                        i6 = 0;
                        i5 = i10;
                        i = 0;
                    } else if (i6 >= 128) {
                        this.miniCEs[i8] = 1;
                        i7 = i;
                    }
                    i2 = i6 + 32;
                } else if (i6 == 0) {
                    i2 = 384;
                } else if (i6 < 992) {
                    i2 = i6 + 32;
                } else {
                    this.miniCEs[i8] = 1;
                    i7 = i;
                }
                i6 = i2;
                i5 = i10;
                i = 0;
            }
            if ((i9 & Collation.ONLY_TERTIARY_MASK) > 1280) {
                if (i < 7) {
                    i++;
                } else {
                    this.miniCEs[i8] = 1;
                    i7 = i;
                }
            }
            if (3072 > i3 || i3 > 4088) {
                this.miniCEs[i8] = (char) (i3 | i6 | i);
            } else {
                this.miniCEs[i8] = (char) (i3 | i);
            }
            i7 = i;
        }
    }

    private void encodeCharCEs() {
        int length = this.result.length();
        for (int i = 0; i < 448; i++) {
            this.result.append(0);
        }
        int length2 = this.result.length();
        for (int i2 = 0; i2 < 448; i2++) {
            long j = this.charCEs[i2][0];
            if (!isContractionCharCE(j)) {
                int i3 = 1;
                int encodeTwoCEs = encodeTwoCEs(j, this.charCEs[i2][1]);
                if ((encodeTwoCEs >>> 16) > 0) {
                    int length3 = this.result.length() - length2;
                    if (length3 <= 1023) {
                        StringBuilder sb = this.result;
                        sb.append((char) (encodeTwoCEs >> 16));
                        sb.append((char) encodeTwoCEs);
                        i3 = length3 | 2048;
                    }
                } else {
                    i3 = encodeTwoCEs;
                }
                this.result.setCharAt(length + i2, (char) i3);
            }
        }
    }

    private void encodeContractions() {
        int i = this.headerLength + JLogConstants.JLID_ABILITY_SHELL_ONDISCONNECT;
        int length = this.result.length();
        char c = 0;
        int i2 = 0;
        while (i2 < 448) {
            long j = this.charCEs[i2][c];
            if (isContractionCharCE(j)) {
                int length2 = this.result.length() - i;
                if (length2 > 1023) {
                    this.result.setCharAt(this.headerLength + i2, 1);
                } else {
                    int i3 = ((int) j) & Integer.MAX_VALUE;
                    boolean z = true;
                    while (true) {
                        long elementAti = this.contractionCEs.elementAti(i3);
                        if (elementAti == 511 && !z) {
                            break;
                        }
                        int encodeTwoCEs = encodeTwoCEs(this.contractionCEs.elementAti(i3 + 1), this.contractionCEs.elementAti(i3 + 2));
                        if (encodeTwoCEs == 1) {
                            this.result.append((char) ((int) (elementAti | 512)));
                        } else if ((encodeTwoCEs >>> 16) == 0) {
                            this.result.append((char) ((int) (elementAti | 1024)));
                            this.result.append((char) encodeTwoCEs);
                        } else {
                            this.result.append((char) ((int) (elementAti | 1536)));
                            StringBuilder sb = this.result;
                            sb.append((char) (encodeTwoCEs >> 16));
                            sb.append((char) encodeTwoCEs);
                        }
                        i3 += 3;
                        i2 = i2;
                        c = 0;
                        z = false;
                    }
                    this.result.setCharAt(this.headerLength + i2, (char) (length2 | 1024));
                }
            }
            i2++;
        }
        if (this.result.length() > length) {
            this.result.append((char) 511);
        }
    }

    private int encodeTwoCEs(long j, long j2) {
        if (j == 0) {
            return 0;
        }
        if (j == Collation.NO_CE) {
            return 1;
        }
        int miniCE = getMiniCE(j);
        if (miniCE == 1) {
            return miniCE;
        }
        if (miniCE >= 4096) {
            miniCE |= ((((int) j) & Collation.CASE_MASK) >> 11) + 8;
        }
        if (j2 == 0) {
            return miniCE;
        }
        int miniCE2 = getMiniCE(j2);
        if (miniCE2 == 1) {
            return miniCE2;
        }
        int i = ((int) j2) & Collation.CASE_MASK;
        if (miniCE >= 4096 && (miniCE & 992) == 160) {
            int i2 = miniCE2 & 992;
            int i3 = miniCE2 & 7;
            if (i2 >= 384 && i == 0 && i3 == 0) {
                return (miniCE & -993) | i2;
            }
        }
        if (miniCE2 <= 992 || 4096 <= miniCE2) {
            miniCE2 |= (i >> 11) + 8;
        }
        return miniCE2 | (miniCE << 16);
    }
}
