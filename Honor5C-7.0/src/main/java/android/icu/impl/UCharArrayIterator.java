package android.icu.impl;

import android.icu.text.UCharacterIterator;

public final class UCharArrayIterator extends UCharacterIterator {
    private final int limit;
    private int pos;
    private final int start;
    private final char[] text;

    public UCharArrayIterator(char[] text, int start, int limit) {
        if (start < 0 || limit > text.length || start > limit) {
            throw new IllegalArgumentException("start: " + start + " or limit: " + limit + " out of range [0, " + text.length + ")");
        }
        this.text = text;
        this.start = start;
        this.limit = limit;
        this.pos = start;
    }

    public int current() {
        return this.pos < this.limit ? this.text[this.pos] : -1;
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
