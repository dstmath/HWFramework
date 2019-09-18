package android.icu.impl;

import java.text.CharacterIterator;

public class CSCharacterIterator implements CharacterIterator {
    private int index;
    private CharSequence seq;

    public CSCharacterIterator(CharSequence text) {
        if (text != null) {
            this.seq = text;
            this.index = 0;
            return;
        }
        throw new NullPointerException();
    }

    public char first() {
        this.index = 0;
        return current();
    }

    public char last() {
        this.index = this.seq.length();
        return previous();
    }

    public char current() {
        if (this.index == this.seq.length()) {
            return 65535;
        }
        return this.seq.charAt(this.index);
    }

    public char next() {
        if (this.index < this.seq.length()) {
            this.index++;
        }
        return current();
    }

    public char previous() {
        if (this.index == 0) {
            return 65535;
        }
        this.index--;
        return current();
    }

    public char setIndex(int position) {
        if (position < 0 || position > this.seq.length()) {
            throw new IllegalArgumentException();
        }
        this.index = position;
        return current();
    }

    public int getBeginIndex() {
        return 0;
    }

    public int getEndIndex() {
        return this.seq.length();
    }

    public int getIndex() {
        return this.index;
    }

    public Object clone() {
        CSCharacterIterator copy = new CSCharacterIterator(this.seq);
        copy.setIndex(this.index);
        return copy;
    }
}
