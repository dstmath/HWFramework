package ohos.global.icu.impl;

public final class USerializedSet {
    private char[] array = new char[8];
    private int arrayOffset;
    private int bmpLength;
    private int length;

    public final boolean getSet(char[] cArr, int i) {
        this.array = null;
        this.length = 0;
        this.bmpLength = 0;
        this.arrayOffset = 0;
        int i2 = i + 1;
        this.length = cArr[i];
        int i3 = this.length;
        if ((32768 & i3) != 0) {
            this.length = i3 & 32767;
            int i4 = i2 + 1;
            if (cArr.length >= this.length + i4) {
                this.bmpLength = cArr[i2];
                i2 = i4;
            } else {
                this.length = 0;
                throw new IndexOutOfBoundsException();
            }
        } else if (cArr.length >= i2 + i3) {
            this.bmpLength = i3;
        } else {
            this.length = 0;
            throw new IndexOutOfBoundsException();
        }
        int i5 = this.length;
        this.array = new char[i5];
        System.arraycopy(cArr, i2, this.array, 0, i5);
        return true;
    }

    public final void setToOne(int i) {
        if (1114111 >= i) {
            if (i < 65535) {
                this.length = 2;
                this.bmpLength = 2;
                char[] cArr = this.array;
                cArr[0] = (char) i;
                cArr[1] = (char) (i + 1);
            } else if (i == 65535) {
                this.bmpLength = 1;
                this.length = 3;
                char[] cArr2 = this.array;
                cArr2[0] = 65535;
                cArr2[1] = 1;
                cArr2[2] = 0;
            } else if (i < 1114111) {
                this.bmpLength = 0;
                this.length = 4;
                char[] cArr3 = this.array;
                cArr3[0] = (char) (i >> 16);
                cArr3[1] = (char) i;
                int i2 = i + 1;
                cArr3[2] = (char) (i2 >> 16);
                cArr3[3] = (char) i2;
            } else {
                this.bmpLength = 0;
                this.length = 2;
                char[] cArr4 = this.array;
                cArr4[0] = 16;
                cArr4[1] = 65535;
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r10v0, resolved type: int[] */
    /* JADX DEBUG: Multi-variable search result rejected for r2v1, resolved type: char[] */
    /* JADX DEBUG: Multi-variable search result rejected for r9v7, resolved type: char */
    /* JADX DEBUG: Multi-variable search result rejected for r8v7, resolved type: char */
    /* JADX DEBUG: Multi-variable search result rejected for r9v8, resolved type: char */
    /* JADX DEBUG: Multi-variable search result rejected for r8v11, resolved type: char */
    /* JADX WARN: Multi-variable type inference failed */
    public final boolean getRange(int i, int[] iArr) {
        if (i < 0) {
            return false;
        }
        if (this.array == null) {
            this.array = new char[8];
        }
        if (iArr == 0 || iArr.length < 2) {
            throw new IllegalArgumentException();
        }
        int i2 = i * 2;
        int i3 = this.bmpLength;
        if (i2 < i3) {
            char[] cArr = this.array;
            int i4 = i2 + 1;
            iArr[0] = cArr[i2];
            if (i4 < i3) {
                iArr[1] = cArr[i4] - 1;
            } else if (i4 < this.length) {
                iArr[1] = ((cArr[i4] << 16) | cArr[i4 + 1]) - 1;
            } else {
                iArr[1] = 1114111;
            }
            return true;
        }
        int i5 = (i2 - i3) * 2;
        int i6 = this.length - i3;
        if (i5 >= i6) {
            return false;
        }
        int i7 = this.arrayOffset + i3;
        char[] cArr2 = this.array;
        int i8 = i7 + i5;
        iArr[0] = cArr2[i8 + 1] | (cArr2[i8] << 16);
        int i9 = i5 + 2;
        if (i9 < i6) {
            int i10 = i7 + i9;
            iArr[1] = (cArr2[i10 + 1] | (cArr2[i10] << 16)) - 1;
        } else {
            iArr[1] = 1114111;
        }
        return true;
    }

    public final boolean contains(int i) {
        if (i > 1114111) {
            return false;
        }
        if (i <= 65535) {
            int i2 = 0;
            while (i2 < this.bmpLength && ((char) i) >= this.array[i2]) {
                i2++;
            }
            return (i2 & 1) != 0;
        }
        char c = (char) (i >> 16);
        char c2 = (char) i;
        int i3 = this.bmpLength;
        while (i3 < this.length) {
            char[] cArr = this.array;
            if (c <= cArr[i3] && (c != cArr[i3] || c2 < cArr[i3 + 1])) {
                break;
            }
            i3 += 2;
        }
        return ((i3 + this.bmpLength) & 2) != 0;
    }

    public final int countRanges() {
        int i = this.bmpLength;
        return ((i + ((this.length - i) / 2)) + 1) / 2;
    }
}
