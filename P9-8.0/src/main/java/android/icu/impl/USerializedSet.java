package android.icu.impl;

import android.icu.text.DateTimePatternGenerator;

public final class USerializedSet {
    private char[] array = new char[8];
    private int arrayOffset;
    private int bmpLength;
    private int length;

    public final boolean getSet(char[] src, int srcStart) {
        this.array = null;
        this.length = 0;
        this.bmpLength = 0;
        this.arrayOffset = 0;
        int srcStart2 = srcStart + 1;
        this.length = src[srcStart];
        if ((this.length & 32768) != 0) {
            this.length &= 32767;
            if (src.length < (srcStart2 + 1) + this.length) {
                this.length = 0;
                throw new IndexOutOfBoundsException();
            }
            srcStart = srcStart2 + 1;
            this.bmpLength = src[srcStart2];
        } else if (src.length < this.length + srcStart2) {
            this.length = 0;
            throw new IndexOutOfBoundsException();
        } else {
            this.bmpLength = this.length;
            srcStart = srcStart2;
        }
        this.array = new char[this.length];
        System.arraycopy(src, srcStart, this.array, 0, this.length);
        return true;
    }

    public final void setToOne(int c) {
        if (1114111 >= c) {
            if (c < DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                this.length = 2;
                this.bmpLength = 2;
                this.array[0] = (char) c;
                this.array[1] = (char) (c + 1);
            } else if (c == DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                this.bmpLength = 1;
                this.length = 3;
                this.array[0] = 65535;
                this.array[1] = 1;
                this.array[2] = 0;
            } else if (c < 1114111) {
                this.bmpLength = 0;
                this.length = 4;
                this.array[0] = (char) (c >> 16);
                this.array[1] = (char) c;
                c++;
                this.array[2] = (char) (c >> 16);
                this.array[3] = (char) c;
            } else {
                this.bmpLength = 0;
                this.length = 2;
                this.array[0] = 16;
                this.array[1] = 65535;
            }
        }
    }

    public final boolean getRange(int rangeIndex, int[] range) {
        if (rangeIndex < 0) {
            return false;
        }
        if (this.array == null) {
            this.array = new char[8];
        }
        if (range == null || range.length < 2) {
            throw new IllegalArgumentException();
        }
        rangeIndex *= 2;
        if (rangeIndex < this.bmpLength) {
            int rangeIndex2 = rangeIndex + 1;
            range[0] = this.array[rangeIndex];
            if (rangeIndex2 < this.bmpLength) {
                range[1] = this.array[rangeIndex2] - 1;
            } else if (rangeIndex2 < this.length) {
                range[1] = ((this.array[rangeIndex2] << 16) | this.array[rangeIndex2 + 1]) - 1;
            } else {
                range[1] = 1114111;
            }
            return true;
        }
        rangeIndex = (rangeIndex - this.bmpLength) * 2;
        int suppLength = this.length - this.bmpLength;
        if (rangeIndex >= suppLength) {
            return false;
        }
        int offset = this.arrayOffset + this.bmpLength;
        range[0] = (this.array[offset + rangeIndex] << 16) | this.array[(offset + rangeIndex) + 1];
        rangeIndex += 2;
        if (rangeIndex < suppLength) {
            range[1] = ((this.array[offset + rangeIndex] << 16) | this.array[(offset + rangeIndex) + 1]) - 1;
        } else {
            range[1] = 1114111;
        }
        return true;
    }

    public final boolean contains(int c) {
        boolean z = true;
        if (c > 1114111) {
            return false;
        }
        int i;
        if (c <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            i = 0;
            while (i < this.bmpLength && ((char) c) >= this.array[i]) {
                i++;
            }
            if ((i & 1) == 0) {
                z = false;
            }
            return z;
        }
        char high = (char) (c >> 16);
        char low = (char) c;
        i = this.bmpLength;
        while (i < this.length && (high > this.array[i] || (high == this.array[i] && low >= this.array[i + 1]))) {
            i += 2;
        }
        if (((this.bmpLength + i) & 2) == 0) {
            z = false;
        }
        return z;
    }

    public final int countRanges() {
        return ((this.bmpLength + ((this.length - this.bmpLength) / 2)) + 1) / 2;
    }
}
