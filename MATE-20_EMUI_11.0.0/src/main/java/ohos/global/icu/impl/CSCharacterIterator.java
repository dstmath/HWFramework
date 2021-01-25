package ohos.global.icu.impl;

import java.text.CharacterIterator;

public class CSCharacterIterator implements CharacterIterator {
    private int index;
    private CharSequence seq;

    @Override // java.text.CharacterIterator
    public int getBeginIndex() {
        return 0;
    }

    public CSCharacterIterator(CharSequence charSequence) {
        if (charSequence != null) {
            this.seq = charSequence;
            this.index = 0;
            return;
        }
        throw new NullPointerException();
    }

    @Override // java.text.CharacterIterator
    public char first() {
        this.index = 0;
        return current();
    }

    @Override // java.text.CharacterIterator
    public char last() {
        this.index = this.seq.length();
        return previous();
    }

    @Override // java.text.CharacterIterator
    public char current() {
        if (this.index == this.seq.length()) {
            return 65535;
        }
        return this.seq.charAt(this.index);
    }

    @Override // java.text.CharacterIterator
    public char next() {
        if (this.index < this.seq.length()) {
            this.index++;
        }
        return current();
    }

    @Override // java.text.CharacterIterator
    public char previous() {
        int i = this.index;
        if (i == 0) {
            return 65535;
        }
        this.index = i - 1;
        return current();
    }

    @Override // java.text.CharacterIterator
    public char setIndex(int i) {
        if (i < 0 || i > this.seq.length()) {
            throw new IllegalArgumentException();
        }
        this.index = i;
        return current();
    }

    @Override // java.text.CharacterIterator
    public int getEndIndex() {
        return this.seq.length();
    }

    @Override // java.text.CharacterIterator
    public int getIndex() {
        return this.index;
    }

    @Override // java.text.CharacterIterator, java.lang.Object
    public Object clone() {
        CSCharacterIterator cSCharacterIterator = new CSCharacterIterator(this.seq);
        cSCharacterIterator.setIndex(this.index);
        return cSCharacterIterator;
    }
}
