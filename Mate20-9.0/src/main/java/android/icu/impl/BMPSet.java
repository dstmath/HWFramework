package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.UnicodeSet;
import android.icu.util.OutputInt;

public final class BMPSet {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static int U16_SURROGATE_OFFSET = 56613888;
    private int[] bmpBlockBits;
    private boolean[] latin1Contains;
    private final int[] list;
    private int[] list4kStarts;
    private final int listLength;
    private int[] table7FF;

    public BMPSet(int[] parentList, int parentListLength) {
        this.list = parentList;
        this.listLength = parentListLength;
        this.latin1Contains = new boolean[256];
        this.table7FF = new int[64];
        this.bmpBlockBits = new int[64];
        this.list4kStarts = new int[18];
        this.list4kStarts[0] = findCodePoint(2048, 0, this.listLength - 1);
        for (int i = 1; i <= 16; i++) {
            this.list4kStarts[i] = findCodePoint(i << 12, this.list4kStarts[i - 1], this.listLength - 1);
        }
        this.list4kStarts[17] = this.listLength - 1;
        initBits();
    }

    public BMPSet(BMPSet otherBMPSet, int[] newParentList, int newParentListLength) {
        this.list = newParentList;
        this.listLength = newParentListLength;
        this.latin1Contains = (boolean[]) otherBMPSet.latin1Contains.clone();
        this.table7FF = (int[]) otherBMPSet.table7FF.clone();
        this.bmpBlockBits = (int[]) otherBMPSet.bmpBlockBits.clone();
        this.list4kStarts = (int[]) otherBMPSet.list4kStarts.clone();
    }

    public boolean contains(int c) {
        if (c <= 255) {
            return this.latin1Contains[c];
        }
        boolean z = false;
        if (c <= 2047) {
            if ((this.table7FF[c & 63] & (1 << (c >> 6))) != 0) {
                z = true;
            }
            return z;
        } else if (c < 55296 || (c >= 57344 && c <= 65535)) {
            int lead = c >> 12;
            int twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
            if (twoBits > 1) {
                return containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1]);
            }
            if (twoBits != 0) {
                z = true;
            }
            return z;
        } else if (c <= 1114111) {
            return containsSlow(c, this.list4kStarts[13], this.list4kStarts[17]);
        } else {
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x0128  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x012b  */
    public final int span(CharSequence s, int start, UnicodeSet.SpanCondition spanCondition, OutputInt outCount) {
        int twoBits;
        CharSequence charSequence = s;
        OutputInt outputInt = outCount;
        int i = start;
        int limit = s.length();
        int numSupplementary = 0;
        UnicodeSet.SpanCondition spanCondition2 = UnicodeSet.SpanCondition.NOT_CONTAINED;
        char c = 17;
        char c2 = 16;
        char c3 = 57344;
        char c4 = 55296;
        char c5 = 255;
        char c6 = UCharacter.MIN_LOW_SURROGATE;
        if (spanCondition2 != spanCondition) {
            while (i < limit) {
                char c7 = charSequence.charAt(i);
                if (c7 <= c5) {
                    if (!this.latin1Contains[c7]) {
                        break;
                    }
                } else if (c7 > 2047) {
                    if (c7 >= 55296 && c7 < c6 && i + 1 != limit) {
                        char charAt = charSequence.charAt(i + 1);
                        char c22 = charAt;
                        if (charAt >= c6) {
                            char c23 = c22;
                            if (c23 < c3) {
                                if (!containsSlow(Character.toCodePoint(c7, c23), this.list4kStarts[c2], this.list4kStarts[17])) {
                                    break;
                                }
                                numSupplementary++;
                                i++;
                            }
                        }
                    }
                    int lead = c7 >> 12;
                    int twoBits2 = (this.bmpBlockBits[(c7 >> 6) & 63] >> lead) & 65537;
                    if (twoBits2 <= 1) {
                        if (twoBits2 == 0) {
                            break;
                        }
                    } else if (!containsSlow(c7, this.list4kStarts[lead], this.list4kStarts[lead + 1])) {
                        break;
                    }
                } else if ((this.table7FF[c7 & '?'] & (1 << (c7 >> 6))) == 0) {
                    break;
                }
                i++;
                c2 = 16;
                c3 = 57344;
                c5 = 255;
                c6 = UCharacter.MIN_LOW_SURROGATE;
            }
        } else {
            while (i < limit) {
                char c8 = charSequence.charAt(i);
                if (c8 <= 255) {
                    if (this.latin1Contains[c8]) {
                        break;
                    }
                } else if (c8 <= 2047) {
                    if ((this.table7FF[c8 & '?'] & (1 << (c8 >> 6))) != 0) {
                        break;
                    }
                } else {
                    if (c8 >= c4 && c8 < 56320 && i + 1 != limit) {
                        char charAt2 = charSequence.charAt(i + 1);
                        char c24 = charAt2;
                        if (charAt2 >= 56320) {
                            if (c24 < 57344) {
                                if (containsSlow(Character.toCodePoint(c8, c24), this.list4kStarts[16], this.list4kStarts[c])) {
                                    break;
                                }
                                numSupplementary++;
                                i++;
                                i++;
                                c = 17;
                                c4 = 55296;
                            }
                            int lead2 = c8 >> 12;
                            twoBits = (this.bmpBlockBits[(c8 >> 6) & 63] >> lead2) & 65537;
                            if (twoBits > 1) {
                                if (twoBits != 0) {
                                    break;
                                }
                                i++;
                                c = 17;
                                c4 = 55296;
                            } else if (containsSlow(c8, this.list4kStarts[lead2], this.list4kStarts[lead2 + 1])) {
                                break;
                            } else {
                                i++;
                                c = 17;
                                c4 = 55296;
                            }
                        }
                    }
                    int lead22 = c8 >> 12;
                    twoBits = (this.bmpBlockBits[(c8 >> 6) & 63] >> lead22) & 65537;
                    if (twoBits > 1) {
                    }
                }
                i++;
                c = 17;
                c4 = 55296;
            }
        }
        if (outputInt != null) {
            outputInt.value = (i - start) - numSupplementary;
        }
        return i;
    }

    public final int spanBack(CharSequence s, int limit, UnicodeSet.SpanCondition spanCondition) {
        int limit2;
        CharSequence charSequence = s;
        UnicodeSet.SpanCondition spanCondition2 = UnicodeSet.SpanCondition.NOT_CONTAINED;
        char c = 16;
        char c2 = UCharacter.MIN_LOW_SURROGATE;
        if (spanCondition2 != spanCondition) {
            limit2 = limit;
            while (true) {
                limit2--;
                char c3 = charSequence.charAt(limit2);
                if (c3 > 255) {
                    if (c3 <= 2047) {
                        if ((this.table7FF[c3 & '?'] & (1 << (c3 >> 6))) == 0) {
                            break;
                        }
                    } else {
                        if (c3 >= 55296 && c3 >= c2 && limit2 != 0) {
                            char charAt = charSequence.charAt(limit2 - 1);
                            char c22 = charAt;
                            if (charAt >= 55296 && c22 < c2) {
                                if (!containsSlow(Character.toCodePoint(c22, c3), this.list4kStarts[c], this.list4kStarts[17])) {
                                    break;
                                }
                                limit2--;
                            }
                        }
                        int lead = c3 >> 12;
                        int twoBits = (this.bmpBlockBits[(c3 >> 6) & 63] >> lead) & 65537;
                        if (twoBits > 1) {
                            if (!containsSlow(c3, this.list4kStarts[lead], this.list4kStarts[lead + 1])) {
                                break;
                            }
                        } else if (twoBits == 0) {
                            break;
                        }
                    }
                } else if (!this.latin1Contains[c3]) {
                    break;
                }
                if (limit2 == 0) {
                    return 0;
                }
                c = 16;
                c2 = UCharacter.MIN_LOW_SURROGATE;
            }
        } else {
            limit2 = limit;
            do {
                limit2--;
                char c4 = charSequence.charAt(limit2);
                if (c4 <= 255) {
                    if (this.latin1Contains[c4]) {
                    }
                } else if (c4 > 2047) {
                    if (c4 >= 55296 && c4 >= 56320 && limit2 != 0) {
                        char charAt2 = charSequence.charAt(limit2 - 1);
                        char c23 = charAt2;
                        if (charAt2 >= 55296 && c23 < 56320) {
                            if (!containsSlow(Character.toCodePoint(c23, c4), this.list4kStarts[16], this.list4kStarts[17])) {
                                limit2--;
                                continue;
                            }
                        }
                    }
                    int lead2 = c4 >> 12;
                    int twoBits2 = (this.bmpBlockBits[(c4 >> 6) & 63] >> lead2) & 65537;
                    if (twoBits2 <= 1) {
                        if (twoBits2 != 0) {
                        }
                    } else if (containsSlow(c4, this.list4kStarts[lead2], this.list4kStarts[lead2 + 1])) {
                    }
                } else if ((this.table7FF[c4 & '?'] & (1 << (c4 >> 6))) != 0) {
                }
                continue;
            } while (limit2 != 0);
            return 0;
        }
        return limit2 + 1;
    }

    private static void set32x64Bits(int[] table, int start, int limit) {
        int trail;
        int lead = start >> 6;
        int trail2 = start & 63;
        int bits = 1 << lead;
        if (start + 1 == limit) {
            table[trail2] = table[trail2] | bits;
            return;
        }
        int limitLead = limit >> 6;
        int limitTrail = limit & 63;
        if (lead == limitLead) {
            while (trail2 < limitTrail) {
                table[trail2] = table[trail2] | bits;
                trail2++;
            }
        } else {
            if (trail2 > 0) {
                while (true) {
                    trail = trail2 + 1;
                    table[trail2] = table[trail2] | bits;
                    if (trail >= 64) {
                        break;
                    }
                    trail2 = trail;
                }
                lead++;
                int i = trail;
            }
            if (lead < limitLead) {
                int bits2 = ~((1 << lead) - 1);
                if (limitLead < 32) {
                    bits2 &= (1 << limitLead) - 1;
                }
                for (int trail3 = 0; trail3 < 64; trail3++) {
                    table[trail3] = table[trail3] | bits2;
                }
            }
            int bits3 = 1 << limitLead;
            for (int trail4 = 0; trail4 < limitTrail; trail4++) {
                table[trail4] = table[trail4] | bits3;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x00bd A[LOOP:0: B:1:0x0001->B:48:0x00bd, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x002a A[SYNTHETIC] */
    private void initBits() {
        int start;
        int listIndex;
        int limit;
        int minStart;
        int limit2;
        int listIndex2;
        int limit3;
        int start2 = 0;
        while (true) {
            int listIndex3 = start2 + 1;
            start = this.list[start2];
            if (listIndex3 < this.listLength) {
                limit = this.list[listIndex3];
                listIndex3++;
            } else {
                limit = 1114112;
            }
            if (start >= 256) {
                break;
            }
            while (true) {
                int start3 = start + 1;
                this.latin1Contains[start] = true;
                if (start3 < limit && start3 < 256) {
                    start = start3;
                } else if (limit <= 256) {
                    start = start3;
                    break;
                } else {
                    start2 = listIndex;
                }
            }
            if (limit <= 256) {
            }
        }
        while (true) {
            minStart = 2048;
            if (start >= 2048) {
                break;
            }
            set32x64Bits(this.table7FF, start, limit <= 2048 ? limit : 2048);
            if (limit > 2048) {
                start = 2048;
                break;
            }
            int listIndex4 = listIndex + 1;
            start = this.list[listIndex];
            if (listIndex4 < this.listLength) {
                limit3 = this.list[listIndex4];
                listIndex = listIndex4 + 1;
            } else {
                limit3 = 1114112;
                listIndex = listIndex4;
            }
        }
        while (start < 65536) {
            if (limit > 65536) {
                limit = 65536;
            }
            if (start < minStart) {
                start = minStart;
            }
            if (start < limit) {
                if ((start & 63) != 0) {
                    int start4 = start >> 6;
                    int[] iArr = this.bmpBlockBits;
                    int i = start4 & 63;
                    iArr[i] = iArr[i] | (65537 << (start4 >> 6));
                    start = (start4 + 1) << 6;
                    minStart = start;
                }
                if (start < limit) {
                    if (start < (limit & -64)) {
                        set32x64Bits(this.bmpBlockBits, start >> 6, limit >> 6);
                    }
                    if ((limit & 63) != 0) {
                        int limit4 = limit >> 6;
                        int[] iArr2 = this.bmpBlockBits;
                        int i2 = limit4 & 63;
                        iArr2[i2] = (65537 << (limit4 >> 6)) | iArr2[i2];
                        limit = (limit4 + 1) << 6;
                        minStart = limit;
                    }
                }
            }
            if (limit != 65536) {
                int listIndex5 = listIndex + 1;
                start = this.list[listIndex];
                if (listIndex5 < this.listLength) {
                    limit2 = this.list[listIndex5];
                    listIndex2 = listIndex5 + 1;
                } else {
                    limit2 = 1114112;
                    listIndex2 = listIndex5;
                }
            } else {
                return;
            }
        }
    }

    private int findCodePoint(int c, int lo, int hi) {
        if (c < this.list[lo]) {
            return lo;
        }
        if (lo >= hi || c >= this.list[hi - 1]) {
            return hi;
        }
        while (true) {
            int i = (lo + hi) >>> 1;
            if (i == lo) {
                return hi;
            }
            if (c < this.list[i]) {
                hi = i;
            } else {
                lo = i;
            }
        }
    }

    private final boolean containsSlow(int c, int lo, int hi) {
        return (findCodePoint(c, lo, hi) & 1) != 0;
    }
}
