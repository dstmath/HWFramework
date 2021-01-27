package ohos.global.icu.impl;

import ohos.global.icu.text.UCharacterIterator;

public final class UCharArrayIterator extends UCharacterIterator {
    private final int limit;
    private int pos;
    private final int start;
    private final char[] text;

    public UCharArrayIterator(char[] cArr, int i, int i2) {
        if (i < 0 || i2 > cArr.length || i > i2) {
            throw new IllegalArgumentException("start: " + i + " or limit: " + i2 + " out of range [0, " + cArr.length + ")");
        }
        this.text = cArr;
        this.start = i;
        this.limit = i2;
        this.pos = i;
    }

    public int current() {
        int i = this.pos;
        if (i < this.limit) {
            return this.text[i];
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
        int i = this.pos;
        if (i >= this.limit) {
            return -1;
        }
        char[] cArr = this.text;
        this.pos = i + 1;
        return cArr[i];
    }

    public int previous() {
        int i = this.pos;
        if (i <= this.start) {
            return -1;
        }
        char[] cArr = this.text;
        int i2 = i - 1;
        this.pos = i2;
        return cArr[i2];
    }

    public void setIndex(int i) {
        if (i >= 0) {
            int i2 = this.limit;
            int i3 = this.start;
            if (i <= i2 - i3) {
                this.pos = i3 + i;
                return;
            }
        }
        throw new IndexOutOfBoundsException("index: " + i + " out of range [0, " + (this.limit - this.start) + ")");
    }

    public int getText(char[] cArr, int i) {
        int i2 = this.limit;
        int i3 = this.start;
        int i4 = i2 - i3;
        System.arraycopy(this.text, i3, cArr, i, i4);
        return i4;
    }

    public Object clone() {
        try {
            return UCharArrayIterator.super.clone();
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }
}
