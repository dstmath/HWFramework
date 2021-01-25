package ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex;

import java.io.PrintStream;
import java.io.Serializable;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;

/* access modifiers changed from: package-private */
public final class RangeToken extends Token implements Serializable {
    private static final int MAPSIZE = 256;
    private static final long serialVersionUID = 3257568399592010545L;
    boolean compacted;
    RangeToken icaseCache = null;
    int[] map = null;
    int nonMapIndex;
    int[] ranges;
    boolean sorted;

    RangeToken(int i) {
        super(i);
        setSorted(false);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
    public void addRange(int i, int i2) {
        this.icaseCache = null;
        if (i > i2) {
            i2 = i;
            i = i2;
        }
        int[] iArr = this.ranges;
        if (iArr == null) {
            this.ranges = new int[2];
            int[] iArr2 = this.ranges;
            iArr2[0] = i;
            iArr2[1] = i2;
            setSorted(true);
            return;
        }
        int length = iArr.length;
        int i3 = length - 1;
        if (iArr[i3] + 1 == i) {
            iArr[i3] = i2;
            return;
        }
        int[] iArr3 = new int[(length + 2)];
        System.arraycopy(iArr, 0, iArr3, 0, length);
        this.ranges = iArr3;
        if (this.ranges[i3] >= i) {
            setSorted(false);
        }
        int[] iArr4 = this.ranges;
        iArr4[length] = i;
        iArr4[length + 1] = i2;
        if (!this.sorted) {
            sortRanges();
        }
    }

    private final boolean isSorted() {
        return this.sorted;
    }

    private final void setSorted(boolean z) {
        this.sorted = z;
        if (!z) {
            this.compacted = false;
        }
    }

    private final boolean isCompacted() {
        return this.compacted;
    }

    private final void setCompacted() {
        this.compacted = true;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
    public void sortRanges() {
        int[] iArr;
        if (!isSorted() && (iArr = this.ranges) != null) {
            for (int length = iArr.length - 4; length >= 0; length -= 2) {
                int i = 0;
                while (i <= length) {
                    int[] iArr2 = this.ranges;
                    int i2 = i + 2;
                    if (iArr2[i] > iArr2[i2] || (iArr2[i] == iArr2[i2] && iArr2[i + 1] > iArr2[i + 3])) {
                        int[] iArr3 = this.ranges;
                        int i3 = iArr3[i2];
                        iArr3[i2] = iArr3[i];
                        iArr3[i] = i3;
                        int i4 = i + 3;
                        int i5 = iArr3[i4];
                        int i6 = i + 1;
                        iArr3[i4] = iArr3[i6];
                        iArr3[i6] = i5;
                    }
                    i = i2;
                }
            }
            setSorted(true);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
    public void compactRanges() {
        int i;
        int i2;
        int[] iArr = this.ranges;
        if (iArr != null && iArr.length > 2 && !isCompacted()) {
            int i3 = 0;
            int i4 = 0;
            while (true) {
                int[] iArr2 = this.ranges;
                if (i3 < iArr2.length) {
                    if (i4 != i3) {
                        int i5 = i3 + 1;
                        iArr2[i4] = iArr2[i3];
                        i = i5 + 1;
                        iArr2[i4 + 1] = iArr2[i5];
                    } else {
                        i = i3 + 2;
                    }
                    int i6 = i4 + 1;
                    int i7 = this.ranges[i6];
                    i3 = i;
                    while (true) {
                        int[] iArr3 = this.ranges;
                        if (i3 >= iArr3.length || (i2 = i7 + 1) < iArr3[i3]) {
                            break;
                        }
                        if (i2 == iArr3[i3]) {
                            iArr3[i6] = iArr3[i3 + 1];
                            i7 = iArr3[i6];
                        } else {
                            int i8 = i3 + 1;
                            if (i7 >= iArr3[i8]) {
                                continue;
                            } else if (i7 < iArr3[i8]) {
                                iArr3[i6] = iArr3[i8];
                                i7 = iArr3[i6];
                            } else {
                                throw new RuntimeException("Token#compactRanges(): Internel Error: [" + this.ranges[i4] + "," + this.ranges[i6] + "] [" + this.ranges[i3] + "," + this.ranges[i8] + "]");
                            }
                        }
                        i3 += 2;
                    }
                    i4 += 2;
                } else {
                    if (i4 != iArr2.length) {
                        int[] iArr4 = new int[i4];
                        System.arraycopy(iArr2, 0, iArr4, 0, i4);
                        this.ranges = iArr4;
                    }
                    setCompacted();
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
    public void mergeRanges(Token token) {
        RangeToken rangeToken = (RangeToken) token;
        sortRanges();
        rangeToken.sortRanges();
        if (rangeToken.ranges != null) {
            this.icaseCache = null;
            setSorted(true);
            int[] iArr = this.ranges;
            int i = 0;
            if (iArr == null) {
                this.ranges = new int[rangeToken.ranges.length];
                int[] iArr2 = rangeToken.ranges;
                System.arraycopy(iArr2, 0, this.ranges, 0, iArr2.length);
                return;
            }
            int[] iArr3 = new int[(iArr.length + rangeToken.ranges.length)];
            int i2 = 0;
            int i3 = 0;
            while (true) {
                if (i < this.ranges.length || i2 < rangeToken.ranges.length) {
                    int[] iArr4 = this.ranges;
                    if (i >= iArr4.length) {
                        int i4 = i3 + 1;
                        int[] iArr5 = rangeToken.ranges;
                        int i5 = i2 + 1;
                        iArr3[i3] = iArr5[i2];
                        i3 = i4 + 1;
                        i2 = i5 + 1;
                        iArr3[i4] = iArr5[i5];
                    } else {
                        int[] iArr6 = rangeToken.ranges;
                        if (i2 >= iArr6.length) {
                            int i6 = i3 + 1;
                            int i7 = i + 1;
                            iArr3[i3] = iArr4[i];
                            i3 = i6 + 1;
                            i = i7 + 1;
                            iArr3[i6] = iArr4[i7];
                        } else if (iArr6[i2] < iArr4[i] || (iArr6[i2] == iArr4[i] && iArr6[i2 + 1] < iArr4[i + 1])) {
                            int i8 = i3 + 1;
                            int[] iArr7 = rangeToken.ranges;
                            int i9 = i2 + 1;
                            iArr3[i3] = iArr7[i2];
                            i3 = i8 + 1;
                            i2 = i9 + 1;
                            iArr3[i8] = iArr7[i9];
                        } else {
                            int i10 = i3 + 1;
                            int[] iArr8 = this.ranges;
                            int i11 = i + 1;
                            iArr3[i3] = iArr8[i];
                            i3 = i10 + 1;
                            i = i11 + 1;
                            iArr3[i10] = iArr8[i11];
                        }
                    }
                } else {
                    this.ranges = iArr3;
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
    public void subtractRanges(Token token) {
        if (token.type == 5) {
            intersectRanges(token);
            return;
        }
        RangeToken rangeToken = (RangeToken) token;
        if (rangeToken.ranges != null && this.ranges != null) {
            this.icaseCache = null;
            sortRanges();
            compactRanges();
            rangeToken.sortRanges();
            rangeToken.compactRanges();
            int[] iArr = new int[(this.ranges.length + rangeToken.ranges.length)];
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            while (true) {
                int[] iArr2 = this.ranges;
                if (i >= iArr2.length) {
                    break;
                }
                int[] iArr3 = rangeToken.ranges;
                if (i2 >= iArr3.length) {
                    break;
                }
                int i4 = iArr2[i];
                int i5 = i + 1;
                int i6 = iArr2[i5];
                int i7 = iArr3[i2];
                int i8 = i2 + 1;
                int i9 = iArr3[i8];
                if (i6 < i7) {
                    int i10 = i3 + 1;
                    iArr[i3] = iArr2[i];
                    iArr[i10] = iArr2[i5];
                    i3 = i10 + 1;
                    i = i5 + 1;
                } else {
                    if (i6 >= i7 && i4 <= i9) {
                        if (i7 > i4 || i6 > i9) {
                            if (i7 <= i4) {
                                this.ranges[i] = i9 + 1;
                            } else if (i6 <= i9) {
                                int i11 = i3 + 1;
                                iArr[i3] = i4;
                                i3 = i11 + 1;
                                iArr[i11] = i7 - 1;
                            } else {
                                int i12 = i3 + 1;
                                iArr[i3] = i4;
                                i3 = i12 + 1;
                                iArr[i12] = i7 - 1;
                                this.ranges[i] = i9 + 1;
                            }
                        }
                        i += 2;
                    } else if (i9 >= i4) {
                        throw new RuntimeException("Token#subtractRanges(): Internal Error: [" + this.ranges[i] + "," + this.ranges[i5] + "] - [" + rangeToken.ranges[i2] + "," + rangeToken.ranges[i8] + "]");
                    }
                    i2 += 2;
                }
            }
            while (true) {
                int[] iArr4 = this.ranges;
                if (i < iArr4.length) {
                    int i13 = i3 + 1;
                    int i14 = i + 1;
                    iArr[i3] = iArr4[i];
                    i3 = i13 + 1;
                    i = i14 + 1;
                    iArr[i13] = iArr4[i14];
                } else {
                    this.ranges = new int[i3];
                    System.arraycopy(iArr, 0, this.ranges, 0, i3);
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
    public void intersectRanges(Token token) {
        RangeToken rangeToken = (RangeToken) token;
        if (rangeToken.ranges != null && this.ranges != null) {
            this.icaseCache = null;
            sortRanges();
            compactRanges();
            rangeToken.sortRanges();
            rangeToken.compactRanges();
            int[] iArr = new int[(this.ranges.length + rangeToken.ranges.length)];
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            while (true) {
                int[] iArr2 = this.ranges;
                if (i >= iArr2.length) {
                    break;
                }
                int[] iArr3 = rangeToken.ranges;
                if (i2 >= iArr3.length) {
                    break;
                }
                int i4 = iArr2[i];
                int i5 = i + 1;
                int i6 = iArr2[i5];
                int i7 = iArr3[i2];
                int i8 = i2 + 1;
                int i9 = iArr3[i8];
                if (i6 >= i7) {
                    if (i6 < i7 || i4 > i9) {
                        if (i9 >= i4) {
                            throw new RuntimeException("Token#intersectRanges(): Internal Error: [" + this.ranges[i] + "," + this.ranges[i5] + "] & [" + rangeToken.ranges[i2] + "," + rangeToken.ranges[i8] + "]");
                        }
                    } else if (i7 <= i7 && i6 <= i9) {
                        int i10 = i3 + 1;
                        iArr[i3] = i4;
                        i3 = i10 + 1;
                        iArr[i10] = i6;
                    } else if (i7 <= i4) {
                        int i11 = i3 + 1;
                        iArr[i3] = i4;
                        i3 = i11 + 1;
                        iArr[i11] = i9;
                        this.ranges[i] = i9 + 1;
                    } else if (i6 <= i9) {
                        int i12 = i3 + 1;
                        iArr[i3] = i7;
                        i3 = i12 + 1;
                        iArr[i12] = i6;
                    } else {
                        int i13 = i3 + 1;
                        iArr[i3] = i7;
                        i3 = i13 + 1;
                        iArr[i13] = i9;
                        this.ranges[i] = i9 + 1;
                    }
                    i2 += 2;
                }
                i += 2;
            }
            while (true) {
                int[] iArr4 = this.ranges;
                if (i < iArr4.length) {
                    int i14 = i3 + 1;
                    int i15 = i + 1;
                    iArr[i3] = iArr4[i];
                    i3 = i14 + 1;
                    i = i15 + 1;
                    iArr[i14] = iArr4[i15];
                } else {
                    this.ranges = new int[i3];
                    System.arraycopy(iArr, 0, this.ranges, 0, i3);
                    return;
                }
            }
        }
    }

    static Token complementRanges(Token token) {
        if (token.type == 4 || token.type == 5) {
            RangeToken rangeToken = (RangeToken) token;
            rangeToken.sortRanges();
            rangeToken.compactRanges();
            int[] iArr = rangeToken.ranges;
            int length = iArr.length + 2;
            int i = 0;
            if (iArr[0] == 0) {
                length -= 2;
            }
            int[] iArr2 = rangeToken.ranges;
            int i2 = iArr2[iArr2.length - 1];
            if (i2 == 1114111) {
                length -= 2;
            }
            RangeToken createRange = Token.createRange();
            createRange.ranges = new int[length];
            int[] iArr3 = rangeToken.ranges;
            if (iArr3[0] > 0) {
                int[] iArr4 = createRange.ranges;
                iArr4[0] = 0;
                iArr4[1] = iArr3[0] - 1;
                i = 2;
            }
            int i3 = 1;
            while (true) {
                int[] iArr5 = rangeToken.ranges;
                if (i3 >= iArr5.length - 2) {
                    break;
                }
                int[] iArr6 = createRange.ranges;
                int i4 = i + 1;
                iArr6[i] = iArr5[i3] + 1;
                i = i4 + 1;
                iArr6[i4] = iArr5[i3 + 1] - 1;
                i3 += 2;
            }
            if (i2 != 1114111) {
                int[] iArr7 = createRange.ranges;
                iArr7[i] = i2 + 1;
                iArr7[i + 1] = 1114111;
            }
            createRange.setCompacted();
            return createRange;
        }
        throw new IllegalArgumentException("Token#complementRanges(): must be RANGE: " + token.type);
    }

    /* access modifiers changed from: package-private */
    public synchronized RangeToken getCaseInsensitiveToken() {
        if (this.icaseCache != null) {
            return this.icaseCache;
        }
        RangeToken createRange = this.type == 4 ? Token.createRange() : Token.createNRange();
        for (int i = 0; i < this.ranges.length; i += 2) {
            for (int i2 = this.ranges[i]; i2 <= this.ranges[i + 1]; i2++) {
                if (i2 > 65535) {
                    createRange.addRange(i2, i2);
                } else {
                    char upperCase = Character.toUpperCase((char) i2);
                    createRange.addRange(upperCase, upperCase);
                }
            }
        }
        RangeToken createRange2 = this.type == 4 ? Token.createRange() : Token.createNRange();
        for (int i3 = 0; i3 < createRange.ranges.length; i3 += 2) {
            for (int i4 = createRange.ranges[i3]; i4 <= createRange.ranges[i3 + 1]; i4++) {
                if (i4 > 65535) {
                    createRange2.addRange(i4, i4);
                } else {
                    char upperCase2 = Character.toUpperCase((char) i4);
                    createRange2.addRange(upperCase2, upperCase2);
                }
            }
        }
        createRange2.mergeRanges(createRange);
        createRange2.mergeRanges(this);
        createRange2.compactRanges();
        this.icaseCache = createRange2;
        return createRange2;
    }

    /* access modifiers changed from: package-private */
    public void dumpRanges() {
        System.err.print("RANGE: ");
        if (this.ranges == null) {
            System.err.println(" NULL");
        }
        for (int i = 0; i < this.ranges.length; i += 2) {
            PrintStream printStream = System.err;
            printStream.print("[" + this.ranges[i] + "," + this.ranges[i + 1] + "] ");
        }
        System.err.println("");
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
    public boolean match(int i) {
        if (this.map == null) {
            createMap();
        }
        if (this.type == 4) {
            if (i < 256) {
                return (this.map[i / 32] & (1 << (i & 31))) != 0;
            }
            int i2 = this.nonMapIndex;
            while (true) {
                int[] iArr = this.ranges;
                if (i2 >= iArr.length) {
                    return false;
                }
                if (iArr[i2] <= i && i <= iArr[i2 + 1]) {
                    return true;
                }
                i2 += 2;
            }
        } else if (i < 256) {
            return (this.map[i / 32] & (1 << (i & 31))) == 0;
        } else {
            int i3 = this.nonMapIndex;
            while (true) {
                int[] iArr2 = this.ranges;
                if (i3 >= iArr2.length) {
                    return true;
                }
                if (iArr2[i3] <= i && i <= iArr2[i3 + 1]) {
                    return false;
                }
                i3 += 2;
            }
        }
    }

    private void createMap() {
        int[] iArr = new int[8];
        int length = this.ranges.length;
        int i = 0;
        for (int i2 = 0; i2 < 8; i2++) {
            iArr[i2] = 0;
        }
        while (true) {
            int[] iArr2 = this.ranges;
            if (i >= iArr2.length) {
                break;
            }
            int i3 = iArr2[i];
            int i4 = iArr2[i + 1];
            if (i3 >= 256) {
                break;
            }
            while (i3 <= i4 && i3 < 256) {
                int i5 = i3 / 32;
                iArr[i5] = iArr[i5] | (1 << (i3 & 31));
                i3++;
            }
            if (i4 >= 256) {
                break;
            }
            i += 2;
        }
        length = i;
        this.map = iArr;
        this.nonMapIndex = length;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.Token
    public String toString(int i) {
        int i2 = 0;
        if (this.type == 4) {
            if (this == Token.token_dot) {
                return ".";
            }
            if (this == Token.token_0to9) {
                return "\\d";
            }
            if (this == Token.token_wordchars) {
                return "\\w";
            }
            if (this == Token.token_spaces) {
                return "\\s";
            }
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("[");
            while (i2 < this.ranges.length) {
                if ((i & 1024) != 0 && i2 > 0) {
                    stringBuffer.append(",");
                }
                int[] iArr = this.ranges;
                int i3 = i2 + 1;
                if (iArr[i2] == iArr[i3]) {
                    stringBuffer.append(escapeCharInCharClass(iArr[i2]));
                } else {
                    stringBuffer.append(escapeCharInCharClass(iArr[i2]));
                    stringBuffer.append(LocaleUtility.IETF_SEPARATOR);
                    stringBuffer.append(escapeCharInCharClass(this.ranges[i3]));
                }
                i2 += 2;
            }
            stringBuffer.append("]");
            return stringBuffer.toString();
        } else if (this == Token.token_not_0to9) {
            return "\\D";
        } else {
            if (this == Token.token_not_wordchars) {
                return "\\W";
            }
            if (this == Token.token_not_spaces) {
                return "\\S";
            }
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("[^");
            while (i2 < this.ranges.length) {
                if ((i & 1024) != 0 && i2 > 0) {
                    stringBuffer2.append(",");
                }
                int[] iArr2 = this.ranges;
                int i4 = i2 + 1;
                if (iArr2[i2] == iArr2[i4]) {
                    stringBuffer2.append(escapeCharInCharClass(iArr2[i2]));
                } else {
                    stringBuffer2.append(escapeCharInCharClass(iArr2[i2]));
                    stringBuffer2.append(LocaleUtility.IETF_SEPARATOR);
                    stringBuffer2.append(escapeCharInCharClass(this.ranges[i4]));
                }
                i2 += 2;
            }
            stringBuffer2.append("]");
            return stringBuffer2.toString();
        }
    }

    private static String escapeCharInCharClass(int i) {
        if (i == 9) {
            return "\\t";
        }
        if (i == 10) {
            return "\\n";
        }
        if (i == 12) {
            return "\\f";
        }
        if (i == 13) {
            return "\\r";
        }
        if (i == 27) {
            return "\\e";
        }
        if (!(i == 44 || i == 45)) {
            switch (i) {
                case 91:
                case 92:
                case 93:
                case 94:
                    break;
                default:
                    if (i < 32) {
                        String str = "0" + Integer.toHexString(i);
                        return "\\x" + str.substring(str.length() - 2, str.length());
                    } else if (i >= 65536) {
                        String str2 = "0" + Integer.toHexString(i);
                        return "\\v" + str2.substring(str2.length() - 6, str2.length());
                    } else {
                        return "" + ((char) i);
                    }
            }
        }
        return "\\" + ((char) i);
    }
}
