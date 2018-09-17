package android.icu.impl;

import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UCharacterIterator;
import java.text.CharacterIterator;

public class CharacterIteratorWrapper extends UCharacterIterator {
    private CharacterIterator iterator;

    public CharacterIteratorWrapper(CharacterIterator iter) {
        if (iter == null) {
            throw new IllegalArgumentException();
        }
        this.iterator = iter;
    }

    public int current() {
        int c = this.iterator.current();
        if (c == DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            return -1;
        }
        return c;
    }

    public int getLength() {
        return this.iterator.getEndIndex() - this.iterator.getBeginIndex();
    }

    public int getIndex() {
        return this.iterator.getIndex();
    }

    public int next() {
        int i = this.iterator.current();
        this.iterator.next();
        if (i == DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            return -1;
        }
        return i;
    }

    public int previous() {
        int i = this.iterator.previous();
        if (i == DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            return -1;
        }
        return i;
    }

    public void setIndex(int index) {
        try {
            this.iterator.setIndex(index);
        } catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public void setToLimit() {
        this.iterator.setIndex(this.iterator.getEndIndex());
    }

    public int getText(char[] fillIn, int offset) {
        int length = this.iterator.getEndIndex() - this.iterator.getBeginIndex();
        int currentIndex = this.iterator.getIndex();
        if (offset < 0 || offset + length > fillIn.length) {
            throw new IndexOutOfBoundsException(Integer.toString(length));
        }
        char ch = this.iterator.first();
        while (true) {
            int offset2 = offset;
            if (ch != 65535) {
                offset = offset2 + 1;
                fillIn[offset2] = ch;
                ch = this.iterator.next();
            } else {
                this.iterator.setIndex(currentIndex);
                return length;
            }
        }
    }

    public Object clone() {
        try {
            CharacterIteratorWrapper result = (CharacterIteratorWrapper) super.clone();
            result.iterator = (CharacterIterator) this.iterator.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public int moveIndex(int delta) {
        int length = this.iterator.getEndIndex() - this.iterator.getBeginIndex();
        int idx = this.iterator.getIndex() + delta;
        if (idx < 0) {
            idx = 0;
        } else if (idx > length) {
            idx = length;
        }
        return this.iterator.setIndex(idx);
    }

    public CharacterIterator getCharacterIterator() {
        return (CharacterIterator) this.iterator.clone();
    }
}
