package ohos.global.icu.impl;

import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.OutputInt;

public final class BMPSet {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static int U16_SURROGATE_OFFSET = 56613888;
    private int[] bmpBlockBits;
    private boolean[] latin1Contains;
    private final int[] list;
    private int[] list4kStarts;
    private final int listLength;
    private int[] table7FF;

    public BMPSet(int[] iArr, int i) {
        this.list = iArr;
        this.listLength = i;
        this.latin1Contains = new boolean[256];
        this.table7FF = new int[64];
        this.bmpBlockBits = new int[64];
        this.list4kStarts = new int[18];
        this.list4kStarts[0] = findCodePoint(2048, 0, this.listLength - 1);
        for (int i2 = 1; i2 <= 16; i2++) {
            int[] iArr2 = this.list4kStarts;
            iArr2[i2] = findCodePoint(i2 << 12, iArr2[i2 - 1], this.listLength - 1);
        }
        this.list4kStarts[17] = this.listLength - 1;
        initBits();
    }

    public BMPSet(BMPSet bMPSet, int[] iArr, int i) {
        this.list = iArr;
        this.listLength = i;
        this.latin1Contains = (boolean[]) bMPSet.latin1Contains.clone();
        this.table7FF = (int[]) bMPSet.table7FF.clone();
        this.bmpBlockBits = (int[]) bMPSet.bmpBlockBits.clone();
        this.list4kStarts = (int[]) bMPSet.list4kStarts.clone();
    }

    public boolean contains(int i) {
        if (i <= 255) {
            return this.latin1Contains[i];
        }
        if (i <= 2047) {
            return (this.table7FF[i & 63] & (1 << (i >> 6))) != 0;
        }
        if (i < 55296 || (i >= 57344 && i <= 65535)) {
            int i2 = i >> 12;
            int i3 = (this.bmpBlockBits[(i >> 6) & 63] >> i2) & 65537;
            if (i3 <= 1) {
                return i3 != 0;
            }
            int[] iArr = this.list4kStarts;
            return containsSlow(i, iArr[i2], iArr[i2 + 1]);
        } else if (i > 1114111) {
            return false;
        } else {
            int[] iArr2 = this.list4kStarts;
            return containsSlow(i, iArr2[13], iArr2[17]);
        }
    }

    public final int span(CharSequence charSequence, int i, UnicodeSet.SpanCondition spanCondition, OutputInt outputInt) {
        int i2;
        int i3;
        char charAt;
        int i4;
        char charAt2;
        int length = charSequence.length();
        char c = 57344;
        char c2 = 55296;
        char c3 = 2047;
        char c4 = 255;
        int i5 = 0;
        if (UnicodeSet.SpanCondition.NOT_CONTAINED != spanCondition) {
            i2 = i;
            while (i2 < length) {
                char charAt3 = charSequence.charAt(i2);
                if (charAt3 <= c4) {
                    if (!this.latin1Contains[charAt3]) {
                        break;
                    }
                } else if (charAt3 <= 2047) {
                    if ((this.table7FF[charAt3 & '?'] & (1 << (charAt3 >> 6))) == 0) {
                        break;
                    }
                } else if (charAt3 < c2 || charAt3 >= 56320 || (i4 = i2 + 1) == length || (charAt2 = charSequence.charAt(i4)) < 56320 || charAt2 >= c) {
                    int i6 = charAt3 >> '\f';
                    int i7 = (this.bmpBlockBits[(charAt3 >> 6) & 63] >> i6) & 65537;
                    if (i7 <= 1) {
                        if (i7 == 0) {
                            break;
                        }
                    } else {
                        int[] iArr = this.list4kStarts;
                        if (!containsSlow(charAt3, iArr[i6], iArr[i6 + 1])) {
                            break;
                        }
                    }
                } else {
                    int codePoint = Character.toCodePoint(charAt3, charAt2);
                    int[] iArr2 = this.list4kStarts;
                    if (!containsSlow(codePoint, iArr2[16], iArr2[17])) {
                        break;
                    }
                    i5++;
                    i2 = i4 + 1;
                    c = 57344;
                    c2 = 55296;
                    c4 = 255;
                }
                i4 = i2;
                i2 = i4 + 1;
                c = 57344;
                c2 = 55296;
                c4 = 255;
            }
        } else {
            i2 = i;
            while (i2 < length) {
                char charAt4 = charSequence.charAt(i2);
                if (charAt4 <= 255) {
                    if (this.latin1Contains[charAt4]) {
                        break;
                    }
                } else if (charAt4 <= c3) {
                    if (((1 << (charAt4 >> 6)) & this.table7FF[charAt4 & '?']) != 0) {
                        break;
                    }
                } else if (charAt4 < 55296 || charAt4 >= 56320 || (i3 = i2 + 1) == length || (charAt = charSequence.charAt(i3)) < 56320 || charAt >= 57344) {
                    int i8 = charAt4 >> '\f';
                    int i9 = (this.bmpBlockBits[(charAt4 >> 6) & 63] >> i8) & 65537;
                    if (i9 <= 1) {
                        if (i9 != 0) {
                            break;
                        }
                        i3 = i2;
                        i2 = i3 + 1;
                        c3 = 2047;
                    } else {
                        int[] iArr3 = this.list4kStarts;
                        if (containsSlow(charAt4, iArr3[i8], iArr3[i8 + 1])) {
                            break;
                        }
                        i3 = i2;
                        i2 = i3 + 1;
                        c3 = 2047;
                    }
                } else {
                    int codePoint2 = Character.toCodePoint(charAt4, charAt);
                    int[] iArr4 = this.list4kStarts;
                    if (containsSlow(codePoint2, iArr4[16], iArr4[17])) {
                        break;
                    }
                    i5++;
                    i2 = i3 + 1;
                    c3 = 2047;
                }
                i3 = i2;
                i2 = i3 + 1;
                c3 = 2047;
            }
        }
        if (outputInt != null) {
            outputInt.value = (i2 - i) - i5;
        }
        return i2;
    }

    public final int spanBack(CharSequence charSequence, int i, UnicodeSet.SpanCondition spanCondition) {
        char charAt;
        char charAt2;
        if (UnicodeSet.SpanCondition.NOT_CONTAINED != spanCondition) {
            do {
                i--;
                char charAt3 = charSequence.charAt(i);
                if (charAt3 <= 255) {
                    if (!this.latin1Contains[charAt3]) {
                    }
                } else if (charAt3 <= 2047) {
                    if (((1 << (charAt3 >> 6)) & this.table7FF[charAt3 & '?']) == 0) {
                    }
                } else if (charAt3 < 55296 || charAt3 < 56320 || i == 0 || (charAt2 = charSequence.charAt(i - 1)) < 55296 || charAt2 >= 56320) {
                    int i2 = charAt3 >> '\f';
                    int i3 = (this.bmpBlockBits[(charAt3 >> 6) & 63] >> i2) & 65537;
                    if (i3 > 1) {
                        int[] iArr = this.list4kStarts;
                        if (!containsSlow(charAt3, iArr[i2], iArr[i2 + 1])) {
                        }
                    } else if (i3 == 0) {
                    }
                } else {
                    int codePoint = Character.toCodePoint(charAt2, charAt3);
                    int[] iArr2 = this.list4kStarts;
                    if (containsSlow(codePoint, iArr2[16], iArr2[17])) {
                        i--;
                        continue;
                    }
                }
            } while (i != 0);
            return 0;
        }
        do {
            i--;
            char charAt4 = charSequence.charAt(i);
            if (charAt4 <= 255) {
                if (this.latin1Contains[charAt4]) {
                }
            } else if (charAt4 <= 2047) {
                if (((1 << (charAt4 >> 6)) & this.table7FF[charAt4 & '?']) != 0) {
                }
            } else if (charAt4 < 55296 || charAt4 < 56320 || i == 0 || (charAt = charSequence.charAt(i - 1)) < 55296 || charAt >= 56320) {
                int i4 = charAt4 >> '\f';
                int i5 = (this.bmpBlockBits[(charAt4 >> 6) & 63] >> i4) & 65537;
                if (i5 > 1) {
                    int[] iArr3 = this.list4kStarts;
                    if (containsSlow(charAt4, iArr3[i4], iArr3[i4 + 1])) {
                    }
                } else if (i5 != 0) {
                }
            } else {
                int codePoint2 = Character.toCodePoint(charAt, charAt4);
                int[] iArr4 = this.list4kStarts;
                if (!containsSlow(codePoint2, iArr4[16], iArr4[17])) {
                    i--;
                    continue;
                }
            }
        } while (i != 0);
        return 0;
        return i + 1;
    }

    private static void set32x64Bits(int[] iArr, int i, int i2) {
        int i3 = i >> 6;
        int i4 = i & 63;
        int i5 = 1 << i3;
        if (i + 1 == i2) {
            iArr[i4] = iArr[i4] | i5;
            return;
        }
        int i6 = i2 >> 6;
        int i7 = i2 & 63;
        if (i3 == i6) {
            while (i4 < i7) {
                iArr[i4] = iArr[i4] | i5;
                i4++;
            }
            return;
        }
        if (i4 > 0) {
            while (true) {
                int i8 = i4 + 1;
                iArr[i4] = iArr[i4] | i5;
                if (i8 >= 64) {
                    break;
                }
                i4 = i8;
            }
            i3++;
        }
        if (i3 < i6) {
            int i9 = ~((1 << i3) - 1);
            if (i6 < 32) {
                i9 &= (1 << i6) - 1;
            }
            for (int i10 = 0; i10 < 64; i10++) {
                iArr[i10] = iArr[i10] | i9;
            }
        }
        int i11 = 1 << i6;
        for (int i12 = 0; i12 < i7; i12++) {
            iArr[i12] = iArr[i12] | i11;
        }
    }

    private void initBits() {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6 = 0;
        while (true) {
            int[] iArr = this.list;
            i = i6 + 1;
            i2 = iArr[i6];
            if (i < this.listLength) {
                i3 = iArr[i];
                i++;
            } else {
                i3 = 1114112;
            }
            if (i2 >= 256) {
                break;
            }
            while (true) {
                i5 = i2 + 1;
                this.latin1Contains[i2] = true;
                if (i5 >= i3 || i5 >= 256) {
                    break;
                }
                i2 = i5;
            }
            if (i3 > 256) {
                i2 = i5;
                break;
            }
            i6 = i;
        }
        while (true) {
            i4 = 2048;
            if (i2 >= 2048) {
                break;
            }
            set32x64Bits(this.table7FF, i2, i3 <= 2048 ? i3 : 2048);
            if (i3 > 2048) {
                i2 = 2048;
                break;
            }
            int[] iArr2 = this.list;
            int i7 = i + 1;
            int i8 = iArr2[i];
            if (i7 < this.listLength) {
                int i9 = i7 + 1;
                i3 = iArr2[i7];
                i2 = i8;
                i = i9;
            } else {
                i2 = i8;
                i = i7;
                i3 = 1114112;
            }
        }
        while (i2 < 65536) {
            if (i3 > 65536) {
                i3 = 65536;
            }
            if (i2 < i4) {
                i2 = i4;
            }
            if (i2 < i3) {
                if ((i2 & 63) != 0) {
                    int i10 = i2 >> 6;
                    int[] iArr3 = this.bmpBlockBits;
                    int i11 = i10 & 63;
                    iArr3[i11] = iArr3[i11] | (65537 << (i10 >> 6));
                    i4 = (i10 + 1) << 6;
                    i2 = i4;
                }
                if (i2 < i3) {
                    if (i2 < (i3 & -64)) {
                        set32x64Bits(this.bmpBlockBits, i2 >> 6, i3 >> 6);
                    }
                    if ((i3 & 63) != 0) {
                        int i12 = i3 >> 6;
                        int[] iArr4 = this.bmpBlockBits;
                        int i13 = i12 & 63;
                        iArr4[i13] = iArr4[i13] | (65537 << (i12 >> 6));
                        i3 = (i12 + 1) << 6;
                        i4 = i3;
                    }
                }
            }
            if (i3 != 65536) {
                int[] iArr5 = this.list;
                int i14 = i + 1;
                int i15 = iArr5[i];
                if (i14 < this.listLength) {
                    int i16 = i14 + 1;
                    i3 = iArr5[i14];
                    i2 = i15;
                    i = i16;
                } else {
                    i2 = i15;
                    i = i14;
                    i3 = 1114112;
                }
            } else {
                return;
            }
        }
    }

    private int findCodePoint(int i, int i2, int i3) {
        int[] iArr = this.list;
        if (i < iArr[i2]) {
            return i2;
        }
        if (i2 >= i3 || i >= iArr[i3 - 1]) {
            return i3;
        }
        while (true) {
            int i4 = (i2 + i3) >>> 1;
            if (i4 == i2) {
                return i3;
            }
            if (i < this.list[i4]) {
                i3 = i4;
            } else {
                i2 = i4;
            }
        }
    }

    private final boolean containsSlow(int i, int i2, int i3) {
        return (findCodePoint(i, i2, i3) & 1) != 0;
    }
}
