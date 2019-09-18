package android.icu.impl;

import android.icu.text.UCharacterIterator;

public final class UCharArrayIterator extends UCharacterIterator {
    private final int limit;
    private int pos;
    private final int start;
    private final char[] text;

    public UCharArrayIterator(char[] text2, int start2, int limit2) {
        if (start2 < 0 || limit2 > text2.length || start2 > limit2) {
            throw new IllegalArgumentException("start: " + start2 + " or limit: " + limit2 + " out of range [0, " + text2.length + ")");
        }
        this.text = text2;
        this.start = start2;
        this.limit = limit2;
        this.pos = start2;
    }

    public int current() {
        if (this.pos < this.limit) {
            return this.text[this.pos];
        }
        return -1;
    }

    public int getLength() {
        return this.limit - this.start;
    }

    public int getIndex() {
        return this.pos - this.start;
    }

    public int next() {
        if (this.pos >= this.limit) {
            return -1;
        }
        char[] cArr = this.text;
        int i = this.pos;
        this.pos = i + 1;
        return cArr[i];
    }

    public int previous() {
        if (this.pos <= this.start) {
            return -1;
        }
        char[] cArr = this.text;
        int i = this.pos - 1;
        this.pos = i;
        return cArr[i];
    }

    public void setIndex(int index) {
        if (index < 0 || index > this.limit - this.start) {
            throw new IndexOutOfBoundsException("index: " + index + " out of range [0, " + (this.limit - this.start) + ")");
        }
        this.pos = this.start + index;
    }

    public int getText(char[] fillIn, int offset) {
        int len = this.limit - this.start;
        System.arraycopy(this.text, this.start, fillIn, offset, len);
        return len;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
