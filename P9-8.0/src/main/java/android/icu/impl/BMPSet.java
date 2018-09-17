package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UnicodeSet.SpanCondition;
import android.icu.util.OutputInt;
import dalvik.bytecode.Opcodes;

public final class BMPSet {
    static final /* synthetic */ boolean -assertionsDisabled = (BMPSet.class.desiredAssertionStatus() ^ 1);
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
        boolean z = true;
        if (c <= 255) {
            return this.latin1Contains[c];
        }
        if (c <= Opcodes.OP_IGET_WIDE_JUMBO) {
            if ((this.table7FF[c & 63] & (1 << (c >> 6))) == 0) {
                z = false;
            }
            return z;
        } else if (c < 55296 || (c >= 57344 && c <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH)) {
            int lead = c >> 12;
            int twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
            if (twoBits > 1) {
                return containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1]);
            }
            if (twoBits == 0) {
                z = false;
            }
            return z;
        } else if (c <= 1114111) {
            return containsSlow(c, this.list4kStarts[13], this.list4kStarts[17]);
        } else {
            return false;
        }
    }

    public final int span(CharSequence s, int start, SpanCondition spanCondition, OutputInt outCount) {
        int i = start;
        int limit = s.length();
        int numSupplementary = 0;
        char c;
        char c2;
        int lead;
        int twoBits;
        if (SpanCondition.NOT_CONTAINED != spanCondition) {
            while (i < limit) {
                c = s.charAt(i);
                if (c <= 255) {
                    if (!this.latin1Contains[c]) {
                        break;
                    }
                } else if (c <= 2047) {
                    if ((this.table7FF[c & 63] & (1 << (c >> 6))) == 0) {
                        break;
                    }
                } else {
                    if (c >= 55296 && c < UCharacter.MIN_LOW_SURROGATE && i + 1 != limit) {
                        c2 = s.charAt(i + 1);
                        if (c2 >= UCharacter.MIN_LOW_SURROGATE && c2 < 57344) {
                            if (!containsSlow(Character.toCodePoint(c, c2), this.list4kStarts[16], this.list4kStarts[17])) {
                                break;
                            }
                            numSupplementary++;
                            i++;
                        }
                    }
                    lead = c >> 12;
                    twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
                    if (twoBits <= 1) {
                        if (twoBits == 0) {
                            break;
                        }
                    } else if (!containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1])) {
                        break;
                    }
                }
                i++;
            }
        } else {
            while (i < limit) {
                c = s.charAt(i);
                if (c <= 255) {
                    if (this.latin1Contains[c]) {
                        break;
                    }
                } else if (c > 2047) {
                    if (c >= 55296 && c < UCharacter.MIN_LOW_SURROGATE && i + 1 != limit) {
                        c2 = s.charAt(i + 1);
                        if (c2 >= UCharacter.MIN_LOW_SURROGATE && c2 < 57344) {
                            if (containsSlow(Character.toCodePoint(c, c2), this.list4kStarts[16], this.list4kStarts[17])) {
                                break;
                            }
                            numSupplementary++;
                            i++;
                        }
                    }
                    lead = c >> 12;
                    twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
                    if (twoBits <= 1) {
                        if (twoBits != 0) {
                            break;
                        }
                    } else if (containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1])) {
                        break;
                    }
                } else if ((this.table7FF[c & 63] & (1 << (c >> 6))) != 0) {
                    break;
                }
                i++;
            }
        }
        if (outCount != null) {
            outCount.value = (i - start) - numSupplementary;
        }
        return i;
    }

    public final int spanBack(CharSequence s, int limit, SpanCondition spanCondition) {
        char c;
        char c2;
        int lead;
        int twoBits;
        if (SpanCondition.NOT_CONTAINED != spanCondition) {
            do {
                limit--;
                c = s.charAt(limit);
                if (c <= 255) {
                    if (this.latin1Contains[c]) {
                    }
                } else if (c > 2047) {
                    if (c >= 55296 && c >= UCharacter.MIN_LOW_SURROGATE && limit != 0) {
                        c2 = s.charAt(limit - 1);
                        if (c2 >= 55296 && c2 < UCharacter.MIN_LOW_SURROGATE) {
                            if (containsSlow(Character.toCodePoint(c2, c), this.list4kStarts[16], this.list4kStarts[17])) {
                                limit--;
                                continue;
                            }
                        }
                    }
                    lead = c >> 12;
                    twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
                    if (twoBits <= 1) {
                        if (twoBits == 0) {
                        }
                    } else if (containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1])) {
                    }
                } else if ((this.table7FF[c & 63] & (1 << (c >> 6))) != 0) {
                }
            } while (limit != 0);
            return 0;
        }
        do {
            limit--;
            c = s.charAt(limit);
            if (c <= 255) {
                if (this.latin1Contains[c]) {
                }
            } else if (c > 2047) {
                if (c >= 55296 && c >= UCharacter.MIN_LOW_SURROGATE && limit != 0) {
                    c2 = s.charAt(limit - 1);
                    if (c2 >= 55296 && c2 < UCharacter.MIN_LOW_SURROGATE) {
                        if (!containsSlow(Character.toCodePoint(c2, c), this.list4kStarts[16], this.list4kStarts[17])) {
                            limit--;
                            continue;
                        }
                    }
                }
                lead = c >> 12;
                twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
                if (twoBits <= 1) {
                    if (twoBits != 0) {
                    }
                } else if (containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1])) {
                }
            } else if ((this.table7FF[c & 63] & (1 << (c >> 6))) != 0) {
            }
        } while (limit != 0);
        return 0;
        return limit + 1;
    }

    private static void set32x64Bits(int[] table, int start, int limit) {
        if (-assertionsDisabled || 64 == table.length) {
            int lead = start >> 6;
            int trail = start & 63;
            int bits = 1 << lead;
            if (start + 1 == limit) {
                table[trail] = table[trail] | bits;
                return;
            }
            int limitLead = limit >> 6;
            int limitTrail = limit & 63;
            int trail2;
            if (lead == limitLead) {
                while (true) {
                    trail2 = trail;
                    if (trail2 >= limitTrail) {
                        break;
                    }
                    trail = trail2 + 1;
                    table[trail2] = table[trail2] | bits;
                }
            } else {
                if (trail > 0) {
                    while (true) {
                        trail2 = trail + 1;
                        table[trail] = table[trail] | bits;
                        if (trail2 >= 64) {
                            break;
                        }
                        trail = trail2;
                    }
                    lead++;
                    trail = trail2;
                }
                if (lead < limitLead) {
                    bits = ~((1 << lead) - 1);
                    if (limitLead < 32) {
                        bits &= (1 << limitLead) - 1;
                    }
                    for (trail = 0; trail < 64; trail++) {
                        table[trail] = table[trail] | bits;
                    }
                }
                bits = 1 << limitLead;
                for (trail = 0; trail < limitTrail; trail++) {
                    table[trail] = table[trail] | bits;
                }
            }
            return;
        }
        throw new AssertionError();
    }

    private void initBits() {
        int limit;
        int listIndex;
        int start;
        int start2;
        int listIndex2 = 0;
        do {
            listIndex = listIndex2 + 1;
            start = this.list[listIndex2];
            if (listIndex < this.listLength) {
                listIndex2 = listIndex + 1;
                limit = this.list[listIndex];
            } else {
                limit = 1114112;
                listIndex2 = listIndex;
            }
            if (start >= 256) {
                listIndex = listIndex2;
                break;
            }
            while (true) {
                start2 = start + 1;
                this.latin1Contains[start] = true;
                if (start2 < limit && start2 < 256) {
                    start = start2;
                }
            }
        } while (limit <= 256);
        start = start2;
        listIndex = listIndex2;
        while (start < 2048) {
            int i;
            int[] iArr = this.table7FF;
            if (limit <= 2048) {
                i = limit;
            } else {
                i = 2048;
            }
            set32x64Bits(iArr, start, i);
            if (limit > 2048) {
                start = 2048;
                break;
            }
            listIndex2 = listIndex + 1;
            start = this.list[listIndex];
            if (listIndex2 < this.listLength) {
                listIndex = listIndex2 + 1;
                limit = this.list[listIndex2];
                listIndex2 = listIndex;
            } else {
                limit = 1114112;
            }
            listIndex = listIndex2;
        }
        int minStart = 2048;
        while (start < 65536) {
            if (limit > 65536) {
                limit = 65536;
            }
            if (start < minStart) {
                start = minStart;
            }
            if (start < limit) {
                int[] iArr2;
                int i2;
                if ((start & 63) != 0) {
                    start >>= 6;
                    iArr2 = this.bmpBlockBits;
                    i2 = start & 63;
                    iArr2[i2] = iArr2[i2] | (65537 << (start >> 6));
                    start = (start + 1) << 6;
                    minStart = start;
                }
                if (start < limit) {
                    if (start < (limit & -64)) {
                        set32x64Bits(this.bmpBlockBits, start >> 6, limit >> 6);
                    }
                    if ((limit & 63) != 0) {
                        limit >>= 6;
                        iArr2 = this.bmpBlockBits;
                        i2 = limit & 63;
                        iArr2[i2] = iArr2[i2] | (65537 << (limit >> 6));
                        limit = (limit + 1) << 6;
                        minStart = limit;
                    }
                }
            }
            if (limit != 65536) {
                listIndex2 = listIndex + 1;
                start = this.list[listIndex];
                if (listIndex2 < this.listLength) {
                    listIndex = listIndex2 + 1;
                    limit = this.list[listIndex2];
                    listIndex2 = listIndex;
                } else {
                    limit = 1114112;
                }
                listIndex = listIndex2;
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
