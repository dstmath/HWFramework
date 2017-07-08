package android.icu.impl;

import android.icu.text.Replaceable;
import android.icu.text.ReplaceableString;
import android.icu.text.UCharacterIterator;
import android.icu.text.UTF16;

public class ReplaceableUCharacterIterator extends UCharacterIterator {
    private int currentIndex;
    private Replaceable replaceable;

    public ReplaceableUCharacterIterator(Replaceable replaceable) {
        if (replaceable == null) {
            throw new IllegalArgumentException();
        }
        this.replaceable = replaceable;
        this.currentIndex = 0;
    }

    public ReplaceableUCharacterIterator(String str) {
        if (str == null) {
            throw new IllegalArgumentException();
        }
        this.replaceable = new ReplaceableString(str);
        this.currentIndex = 0;
    }

    public ReplaceableUCharacterIterator(StringBuffer buf) {
        if (buf == null) {
            throw new IllegalArgumentException();
        }
        this.replaceable = new ReplaceableString(buf);
        this.currentIndex = 0;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public int current() {
        if (this.currentIndex < this.replaceable.length()) {
            return this.replaceable.charAt(this.currentIndex);
        }
        return -1;
    }

    public int currentCodePoint() {
        int ch = current();
        if (UTF16.isLeadSurrogate((char) ch)) {
            next();
            int ch2 = current();
            previous();
            if (UTF16.isTrailSurrogate((char) ch2)) {
                return Character.toCodePoint((char) ch, (char) ch2);
            }
        }
        return ch;
    }

    public int getLength() {
        return this.replaceable.length();
    }

    public int getIndex() {
        return this.currentIndex;
    }

    public int next() {
        if (this.currentIndex >= this.replaceable.length()) {
            return -1;
        }
        Replaceable replaceable = this.replaceable;
        int i = this.currentIndex;
        this.currentIndex = i + 1;
        return replaceable.charAt(i);
    }

    public int previous() {
        if (this.currentIndex <= 0) {
            return -1;
        }
        Replaceable replaceable = this.replaceable;
        int i = this.currentIndex - 1;
        this.currentIndex = i;
        return replaceable.charAt(i);
    }

    public void setIndex(int currentIndex) throws IndexOutOfBoundsException {
        if (currentIndex < 0 || currentIndex > this.replaceable.length()) {
            throw new IndexOutOfBoundsException();
        }
        this.currentIndex = currentIndex;
    }

    public int getText(char[] fillIn, int offset) {
        int length = this.replaceable.length();
        if (offset < 0 || offset + length > fillIn.length) {
            throw new IndexOutOfBoundsException(Integer.toString(length));
        }
        this.replaceable.getChars(0, length, fillIn, offset);
        return length;
    }
}
