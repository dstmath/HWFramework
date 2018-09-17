package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Trie2_32;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUException;

public final class CollationData {
    static final /* synthetic */ boolean -assertionsDisabled = (CollationData.class.desiredAssertionStatus() ^ 1);
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    static final int JAMO_CE32S_LENGTH = 67;
    static final int MAX_NUM_SPECIAL_REORDER_CODES = 8;
    static final int REORDER_RESERVED_AFTER_LATIN = 4111;
    static final int REORDER_RESERVED_BEFORE_LATIN = 4110;
    public CollationData base;
    int[] ce32s;
    long[] ces;
    public boolean[] compressibleBytes;
    String contexts;
    public char[] fastLatinTable;
    char[] fastLatinTableHeader;
    int[] jamoCE32s = new int[67];
    public Normalizer2Impl nfcImpl;
    int numScripts;
    long numericPrimary = 301989888;
    public long[] rootElements;
    char[] scriptStarts;
    char[] scriptsIndex;
    Trie2_32 trie;
    UnicodeSet unsafeBackwardSet;

    CollationData(Normalizer2Impl nfc) {
        this.nfcImpl = nfc;
    }

    public int getCE32(int c) {
        return this.trie.get(c);
    }

    int getCE32FromSupplementary(int c) {
        return this.trie.get(c);
    }

    boolean isDigit(int c) {
        if (c < 1632) {
            return c <= 57 && 48 <= c;
        } else {
            return Collation.hasCE32Tag(getCE32(c), 10);
        }
    }

    public boolean isUnsafeBackward(int c, boolean numeric) {
        if (this.unsafeBackwardSet.contains(c)) {
            return true;
        }
        return numeric ? isDigit(c) : false;
    }

    public boolean isCompressibleLeadByte(int b) {
        return this.compressibleBytes[b];
    }

    public boolean isCompressiblePrimary(long p) {
        return isCompressibleLeadByte(((int) p) >>> 24);
    }

    int getCE32FromContexts(int index) {
        return (this.contexts.charAt(index) << 16) | this.contexts.charAt(index + 1);
    }

    int getIndirectCE32(int ce32) {
        if (-assertionsDisabled || Collation.isSpecialCE32(ce32)) {
            int tag = Collation.tagFromCE32(ce32);
            if (tag == 10) {
                return this.ce32s[Collation.indexFromCE32(ce32)];
            }
            if (tag == 13) {
                return -1;
            }
            if (tag == 11) {
                return this.ce32s[0];
            }
            return ce32;
        }
        throw new AssertionError();
    }

    int getFinalCE32(int ce32) {
        if (Collation.isSpecialCE32(ce32)) {
            return getIndirectCE32(ce32);
        }
        return ce32;
    }

    long getCEFromOffsetCE32(int c, int ce32) {
        return Collation.makeCE(Collation.getThreeBytePrimaryForOffsetData(c, this.ces[Collation.indexFromCE32(ce32)]));
    }

    long getSingleCE(int c) {
        CollationData d;
        int ce32 = getCE32(c);
        if (ce32 == 192) {
            d = this.base;
            ce32 = this.base.getCE32(c);
        } else {
            d = this;
        }
        while (Collation.isSpecialCE32(ce32)) {
            switch (Collation.tagFromCE32(ce32)) {
                case 0:
                case 3:
                    throw new AssertionError(String.format("unexpected CE32 tag for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case 1:
                    return Collation.ceFromLongPrimaryCE32(ce32);
                case 2:
                    return Collation.ceFromLongSecondaryCE32(ce32);
                case 4:
                case 7:
                case 8:
                case 9:
                case 12:
                case 13:
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case 5:
                    if (Collation.lengthFromCE32(ce32) == 1) {
                        ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                        break;
                    }
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case 6:
                    if (Collation.lengthFromCE32(ce32) == 1) {
                        return d.ces[Collation.indexFromCE32(ce32)];
                    }
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case 10:
                    ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                    break;
                case 11:
                    if (-assertionsDisabled || c == 0) {
                        ce32 = d.ce32s[0];
                        break;
                    }
                    throw new AssertionError();
                    break;
                case 14:
                    return d.getCEFromOffsetCE32(c, ce32);
                case 15:
                    return Collation.unassignedCEFromCodePoint(c);
                default:
                    break;
            }
        }
        return Collation.ceFromSimpleCE32(ce32);
    }

    int getFCD16(int c) {
        return this.nfcImpl.getFCD16(c);
    }

    long getFirstPrimaryForGroup(int script) {
        int index = getScriptIndex(script);
        return index == 0 ? 0 : ((long) this.scriptStarts[index]) << 16;
    }

    public long getLastPrimaryForGroup(int script) {
        int index = getScriptIndex(script);
        if (index == 0) {
            return 0;
        }
        return (((long) this.scriptStarts[index + 1]) << 16) - 1;
    }

    public int getGroupForPrimary(long p) {
        p >>= 16;
        if (p < ((long) this.scriptStarts[1]) || ((long) this.scriptStarts[this.scriptStarts.length - 1]) <= p) {
            return -1;
        }
        int i;
        char index = 1;
        while (p >= ((long) this.scriptStarts[index + 1])) {
            index++;
        }
        for (i = 0; i < this.numScripts; i++) {
            if (this.scriptsIndex[i] == index) {
                return i;
            }
        }
        for (i = 0; i < 8; i++) {
            if (this.scriptsIndex[this.numScripts + i] == index) {
                return i + 4096;
            }
        }
        return -1;
    }

    private int getScriptIndex(int script) {
        if (script < 0) {
            return 0;
        }
        if (script < this.numScripts) {
            return this.scriptsIndex[script];
        }
        if (script < 4096) {
            return 0;
        }
        script -= 4096;
        if (script < 8) {
            return this.scriptsIndex[this.numScripts + script];
        }
        return 0;
    }

    public int[] getEquivalentScripts(int script) {
        char index = getScriptIndex(script);
        if (index == 0) {
            return EMPTY_INT_ARRAY;
        }
        if (script >= 4096) {
            return new int[]{script};
        }
        int i;
        int length = 0;
        for (i = 0; i < this.numScripts; i++) {
            if (this.scriptsIndex[i] == index) {
                length++;
            }
        }
        int[] dest = new int[length];
        if (length == 1) {
            dest[0] = script;
            return dest;
        }
        length = 0;
        for (i = 0; i < this.numScripts; i++) {
            if (this.scriptsIndex[i] == index) {
                int length2 = length + 1;
                dest[length] = i;
                length = length2;
            }
        }
        return dest;
    }

    void makeReorderRanges(int[] reorder, UVector32 ranges) {
        makeReorderRanges(reorder, false, ranges);
    }

    /* JADX WARNING: Removed duplicated region for block: B:109:0x0238  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x027c  */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x025a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void makeReorderRanges(int[] reorder, boolean latinMustMove, UVector32 ranges) {
        ranges.removeAllElements();
        int length;
        if (length != 0 && (length != 1 || reorder[0] != 103)) {
            short[] table = new short[(this.scriptStarts.length - 1)];
            int index = this.scriptsIndex[(this.numScripts + 4110) - 4096];
            if (index != 0) {
                table[index] = (short) 255;
            }
            index = this.scriptsIndex[(this.numScripts + 4111) - 4096];
            if (index != 0) {
                table[index] = (short) 255;
            }
            if (!-assertionsDisabled && this.scriptStarts.length < 2) {
                throw new AssertionError();
            } else if (-assertionsDisabled || this.scriptStarts[0] == 0) {
                int lowStart = this.scriptStarts[1];
                if (-assertionsDisabled || lowStart == 768) {
                    int highLimit = this.scriptStarts[this.scriptStarts.length - 1];
                    if (-assertionsDisabled || highLimit == 65280) {
                        int start;
                        int specials = 0;
                        for (int i : reorder) {
                            int reorderCode = i - 4096;
                            if (reorderCode >= 0 && reorderCode < 8) {
                                specials |= 1 << reorderCode;
                            }
                        }
                        int i2 = 0;
                        while (i2 < 8) {
                            index = this.scriptsIndex[this.numScripts + i2];
                            if (index != 0 && ((1 << i2) & specials) == 0) {
                                lowStart = addLowScriptRange(table, index, lowStart);
                            }
                            i2++;
                        }
                        int skippedReserved = 0;
                        if (specials == 0 && reorder[0] == 25 && (latinMustMove ^ 1) != 0) {
                            index = this.scriptsIndex[25];
                            if (-assertionsDisabled || index != 0) {
                                start = this.scriptStarts[index];
                                if (-assertionsDisabled || lowStart <= start) {
                                    skippedReserved = start - lowStart;
                                    lowStart = start;
                                } else {
                                    throw new AssertionError();
                                }
                            }
                            throw new AssertionError();
                        }
                        boolean hasReorderToEnd = false;
                        int i3 = 0;
                        while (i3 < length) {
                            i2 = i3 + 1;
                            int script = reorder[i3];
                            if (script == 103) {
                                hasReorderToEnd = true;
                                while (i2 < length) {
                                    length--;
                                    script = reorder[length];
                                    if (script == 103) {
                                        throw new IllegalArgumentException("setReorderCodes(): duplicate UScript.UNKNOWN");
                                    } else if (script == -1) {
                                        throw new IllegalArgumentException("setReorderCodes(): UScript.DEFAULT together with other scripts");
                                    } else {
                                        index = getScriptIndex(script);
                                        if (index != 0) {
                                            if (table[index] != (short) 0) {
                                                throw new IllegalArgumentException("setReorderCodes(): duplicate or equivalent script " + scriptCodeString(script));
                                            }
                                            highLimit = addHighScriptRange(table, index, highLimit);
                                        }
                                    }
                                }
                                for (i2 = 1; i2 < this.scriptStarts.length - 1; i2++) {
                                    if (table[i2] == 0) {
                                        start = this.scriptStarts[i2];
                                        if (!hasReorderToEnd && start > lowStart) {
                                            lowStart = start;
                                        }
                                        lowStart = addLowScriptRange(table, i2, lowStart);
                                    }
                                }
                                if (lowStart > highLimit) {
                                    int offset = 0;
                                    i2 = 1;
                                    while (true) {
                                        int nextOffset = offset;
                                        while (i2 < this.scriptStarts.length - 1) {
                                            int newLeadByte = table[i2];
                                            if (newLeadByte != 255) {
                                                nextOffset = newLeadByte - (this.scriptStarts[i2] >> 8);
                                                if (nextOffset != offset) {
                                                    break;
                                                }
                                            }
                                            i2++;
                                        }
                                        if (offset != 0 || i2 < this.scriptStarts.length - 1) {
                                            ranges.addElement((this.scriptStarts[i2] << 16) | (DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH & offset));
                                        }
                                        if (i2 != this.scriptStarts.length - 1) {
                                            offset = nextOffset;
                                            i2++;
                                        } else {
                                            return;
                                        }
                                    }
                                } else if (lowStart - (Normalizer2Impl.JAMO_VT & skippedReserved) <= highLimit) {
                                    makeReorderRanges(reorder, true, ranges);
                                    return;
                                } else {
                                    throw new ICUException("setReorderCodes(): reordering too many partial-primary-lead-byte scripts");
                                }
                            } else if (script == -1) {
                                throw new IllegalArgumentException("setReorderCodes(): UScript.DEFAULT together with other scripts");
                            } else {
                                index = getScriptIndex(script);
                                if (index != 0) {
                                    if (table[index] != (short) 0) {
                                        throw new IllegalArgumentException("setReorderCodes(): duplicate or equivalent script " + scriptCodeString(script));
                                    }
                                    lowStart = addLowScriptRange(table, index, lowStart);
                                }
                                i3 = i2;
                            }
                        }
                        while (i2 < this.scriptStarts.length - 1) {
                        }
                        if (lowStart > highLimit) {
                        }
                    } else {
                        throw new AssertionError();
                    }
                }
                throw new AssertionError();
            } else {
                throw new AssertionError();
            }
        }
    }

    private int addLowScriptRange(short[] table, int index, int lowStart) {
        int start = this.scriptStarts[index];
        if ((start & 255) < (lowStart & 255)) {
            lowStart += 256;
        }
        table[index] = (short) (lowStart >> 8);
        int limit = this.scriptStarts[index + 1];
        return ((lowStart & Normalizer2Impl.JAMO_VT) + ((limit & Normalizer2Impl.JAMO_VT) - (Normalizer2Impl.JAMO_VT & start))) | (limit & 255);
    }

    private int addHighScriptRange(short[] table, int index, int highLimit) {
        int limit = this.scriptStarts[index + 1];
        if ((limit & 255) > (highLimit & 255)) {
            highLimit -= 256;
        }
        int start = this.scriptStarts[index];
        highLimit = ((highLimit & Normalizer2Impl.JAMO_VT) - ((limit & Normalizer2Impl.JAMO_VT) - (Normalizer2Impl.JAMO_VT & start))) | (start & 255);
        table[index] = (short) (highLimit >> 8);
        return highLimit;
    }

    private static String scriptCodeString(int script) {
        return script < 4096 ? Integer.toString(script) : "0x" + Integer.toHexString(script);
    }
}
