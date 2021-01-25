package ohos.global.icu.impl;

import java.text.CharacterIterator;
import ohos.global.icu.text.UCharacterIterator;

public class CharacterIteratorWrapper extends UCharacterIterator {
    private CharacterIterator iterator;

    public CharacterIteratorWrapper(CharacterIterator characterIterator) {
        if (characterIterator != null) {
            this.iterator = characterIterator;
            return;
        }
        throw new IllegalArgumentException();
    }

    @Override // ohos.global.icu.text.UCharacterIterator
    public int current() {
        char current = this.iterator.current();
        if (current == 65535) {
            return -1;
        }
        return current;
    }

    @Override // ohos.global.icu.text.UCharacterIterator
    public int getLength() {
        return this.iterator.getEndIndex() - this.iterator.getBeginIndex();
    }

    @Override // ohos.global.icu.text.UCharacterIterator
    public int getIndex() {
        return this.iterator.getIndex();
    }

    @Override // ohos.global.icu.text.UCharacterIterator, ohos.global.icu.text.UForwardCharacterIterator
    public int next() {
        char current = this.iterator.current();
        this.iterator.next();
        if (current == 65535) {
            return -1;
        }
        return current;
    }

    @Override // ohos.global.icu.text.UCharacterIterator
    public int previous() {
        char previous = this.iterator.previous();
        if (previous == 65535) {
            return -1;
        }
        return previous;
    }

    @Override // ohos.global.icu.text.UCharacterIterator
    public void setIndex(int i) {
        try {
            this.iterator.setIndex(i);
        } catch (IllegalArgumentException unused) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override // ohos.global.icu.text.UCharacterIterator
    public void setToLimit() {
        CharacterIterator characterIterator = this.iterator;
        characterIterator.setIndex(characterIterator.getEndIndex());
    }

    @Override // ohos.global.icu.text.UCharacterIterator
    public int getText(char[] cArr, int i) {
        int endIndex = this.iterator.getEndIndex() - this.iterator.getBeginIndex();
        int index = this.iterator.getIndex();
        if (i < 0 || i + endIndex > cArr.length) {
            throw new IndexOutOfBoundsException(Integer.toString(endIndex));
        }
        char first = this.iterator.first();
        while (first != 65535) {
            cArr[i] = first;
            first = this.iterator.next();
            i++;
        }
        this.iterator.setIndex(index);
        return endIndex;
    }

    @Override // ohos.global.icu.text.UCharacterIterator, java.lang.Object
    public Object clone() {
        try {
            CharacterIteratorWrapper characterIteratorWrapper = (CharacterIteratorWrapper) super.clone();
            characterIteratorWrapper.iterator = (CharacterIterator) this.iterator.clone();
            return characterIteratorWrapper;
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }

    @Override // ohos.global.icu.text.UCharacterIterator
    public int moveIndex(int i) {
        int endIndex = this.iterator.getEndIndex() - this.iterator.getBeginIndex();
        int index = i + this.iterator.getIndex();
        if (index < 0) {
            endIndex = 0;
        } else if (index <= endIndex) {
            endIndex = index;
        }
        return this.iterator.setIndex(endIndex);
    }

    @Override // ohos.global.icu.text.UCharacterIterator
    public CharacterIterator getCharacterIterator() {
        return (CharacterIterator) this.iterator.clone();
    }
}
