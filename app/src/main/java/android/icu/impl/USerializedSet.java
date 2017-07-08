package android.icu.impl;

import android.icu.text.UnicodeMatcher;
import android.icu.text.UnicodeSet;
import com.android.dex.DexFormat;
import libcore.icu.DateUtilsBridge;

public final class USerializedSet {
    private char[] array;
    private int arrayOffset;
    private int bmpLength;
    private int length;

    public USerializedSet() {
        this.array = new char[8];
    }

    public final boolean getSet(char[] src, int srcStart) {
        this.array = null;
        this.length = 0;
        this.bmpLength = 0;
        this.arrayOffset = 0;
        int srcStart2 = srcStart + 1;
        this.length = src[srcStart];
        if ((this.length & DateUtilsBridge.FORMAT_ABBREV_WEEKDAY) > 0) {
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
        if (UnicodeSet.MAX_VALUE >= c) {
            if (c < DexFormat.MAX_TYPE_IDX) {
                this.length = 2;
                this.bmpLength = 2;
                this.array[0] = (char) c;
                this.array[1] = (char) (c + 1);
            } else if (c == DexFormat.MAX_TYPE_IDX) {
                this.bmpLength = 1;
                this.length = 3;
                this.array[0] = UnicodeMatcher.ETHER;
                this.array[1] = '\u0001';
                this.array[2] = '\u0000';
            } else if (c < UnicodeSet.MAX_VALUE) {
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
                this.array[0] = '\u0010';
                this.array[1] = UnicodeMatcher.ETHER;
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
                range[1] = UnicodeSet.MAX_VALUE;
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
            range[1] = UnicodeSet.MAX_VALUE;
        }
        return true;
    }

    public final boolean contains(int c) {
        boolean z = true;
        if (c > UnicodeSet.MAX_VALUE) {
            return false;
        }
        int i;
        if (c <= DexFormat.MAX_TYPE_IDX) {
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
