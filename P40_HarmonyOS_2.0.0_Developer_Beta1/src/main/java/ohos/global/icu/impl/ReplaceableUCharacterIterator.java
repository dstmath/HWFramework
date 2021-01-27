package ohos.global.icu.impl;

import ohos.global.icu.text.Replaceable;
import ohos.global.icu.text.ReplaceableString;
import ohos.global.icu.text.UCharacterIterator;
import ohos.global.icu.text.UTF16;

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

    public ReplaceableUCharacterIterator(StringBuffer stringBuffer) {
        if (stringBuffer != null) {
            this.replaceable = new ReplaceableString(stringBuffer);
            this.currentIndex = 0;
            return;
        }
        throw new IllegalArgumentException();
    }

    public Object clone() {
        try {
            return ReplaceableUCharacterIterator.super.clone();
        } catch (CloneNotSupportedException unused) {
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
        int current = current();
        char c = (char) current;
        if (UTF16.isLeadSurrogate(c)) {
            next();
            int current2 = current();
            previous();
            char c2 = (char) current2;
            if (UTF16.isTrailSurrogate(c2)) {
                return Character.toCodePoint(c, c2);
            }
        }
        return current;
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
        int i = this.currentIndex;
        if (i <= 0) {
            return -1;
        }
        Replaceable replaceable2 = this.replaceable;
        int i2 = i - 1;
        this.currentIndex = i2;
        return replaceable2.charAt(i2);
    }

    public void setIndex(int i) throws IndexOutOfBoundsException {
        if (i < 0 || i > this.replaceable.length()) {
            throw new IndexOutOfBoundsException();
        }
        this.currentIndex = i;
    }

    public int getText(char[] cArr, int i) {
        int length = this.replaceable.length();
        if (i < 0 || i + length > cArr.length) {
            throw new IndexOutOfBoundsException(Integer.toString(length));
        }
        this.replaceable.getChars(0, length, cArr, i);
        return length;
    }
}
