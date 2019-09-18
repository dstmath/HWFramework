package android.icu.impl;

import android.icu.text.Replaceable;
import android.icu.text.ReplaceableString;
import android.icu.text.UCharacterIterator;
import android.icu.text.UTF16;

public class ReplaceableUCharacterIterator extends UCharacterIterator {
    private int currentIndex;
    private Replaceable replaceable;

    public ReplaceableUCharacterIterator(Replaceable replaceable2) {
        if (replaceable2 != null) {
            this.replaceable = replaceable2;
            this.currentIndex = 0;
            return;
        }
        throw new IllegalArgumentException();
    }

    public ReplaceableUCharacterIterator(String str) {
        if (str != null) {
            this.replaceable = new ReplaceableString(str);
            this.currentIndex = 0;
            return;
        }
        throw new IllegalArgumentException();
    }

    public ReplaceableUCharacterIterator(StringBuffer buf) {
        if (buf != null) {
            this.replaceable = new ReplaceableString(buf);
            this.currentIndex = 0;
            return;
        }
        throw new IllegalArgumentException();
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
        Replaceable replaceable2 = this.replaceable;
        int i = this.currentIndex;
        this.currentIndex = i + 1;
        return replaceable2.charAt(i);
    }

    public int previous() {
        if (this.currentIndex <= 0) {
            return -1;
        }
        Replaceable replaceable2 = this.replaceable;
        int i = this.currentIndex - 1;
        this.currentIndex = i;
        return replaceable2.charAt(i);
    }

    public void setIndex(int currentIndex2) throws IndexOutOfBoundsException {
        if (currentIndex2 < 0 || currentIndex2 > this.replaceable.length()) {
            throw new IndexOutOfBoundsException();
        }
        this.currentIndex = currentIndex2;
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
