package ohos.global.icu.impl;

import java.text.CharacterIterator;
import ohos.global.icu.text.UCharacterIterator;

public class UCharacterIteratorWrapper implements CharacterIterator {
    private UCharacterIterator iterator;

    @Override // java.text.CharacterIterator
    public int getBeginIndex() {
        return 0;
    }

    public UCharacterIteratorWrapper(UCharacterIterator uCharacterIterator) {
        this.iterator = uCharacterIterator;
    }

    @Override // java.text.CharacterIterator
    public char first() {
        this.iterator.setToStart();
        return (char) this.iterator.current();
    }

    @Override // java.text.CharacterIterator
    public char last() {
        this.iterator.setToLimit();
        return (char) this.iterator.previous();
    }

    @Override // java.text.CharacterIterator
    public char current() {
        return (char) this.iterator.current();
    }

    @Override // java.text.CharacterIterator
    public char next() {
        this.iterator.next();
        return (char) this.iterator.current();
    }

    @Override // java.text.CharacterIterator
    public char previous() {
        return (char) this.iterator.previous();
    }

    @Override // java.text.CharacterIterator
    public char setIndex(int i) {
        this.iterator.setIndex(i);
        return (char) this.iterator.current();
    }

    @Override // java.text.CharacterIterator
    public int getEndIndex() {
        return this.iterator.getLength();
    }

    @Override // java.text.CharacterIterator
    public int getIndex() {
        return this.iterator.getIndex();
    }

    @Override // java.text.CharacterIterator, java.lang.Object
    public Object clone() {
        try {
            UCharacterIteratorWrapper uCharacterIteratorWrapper = (UCharacterIteratorWrapper) super.clone();
            uCharacterIteratorWrapper.iterator = (UCharacterIterator) this.iterator.clone();
            return uCharacterIteratorWrapper;
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }
}
