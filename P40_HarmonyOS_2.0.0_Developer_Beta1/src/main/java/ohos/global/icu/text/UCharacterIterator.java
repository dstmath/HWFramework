package ohos.global.icu.text;

import java.text.CharacterIterator;
import ohos.global.icu.impl.CharacterIteratorWrapper;
import ohos.global.icu.impl.ReplaceableUCharacterIterator;
import ohos.global.icu.impl.UCharArrayIterator;
import ohos.global.icu.impl.UCharacterIteratorWrapper;

public abstract class UCharacterIterator implements Cloneable, UForwardCharacterIterator {
    public abstract int current();

    public abstract int getIndex();

    public abstract int getLength();

    public abstract int getText(char[] cArr, int i);

    @Override // ohos.global.icu.text.UForwardCharacterIterator
    public abstract int next();

    public abstract int previous();

    public abstract void setIndex(int i);

    protected UCharacterIterator() {
    }

    public static final UCharacterIterator getInstance(Replaceable replaceable) {
        return new ReplaceableUCharacterIterator(replaceable);
    }

    public static final UCharacterIterator getInstance(String str) {
        return new ReplaceableUCharacterIterator(str);
    }

    public static final UCharacterIterator getInstance(char[] cArr) {
        return getInstance(cArr, 0, cArr.length);
    }

    public static final UCharacterIterator getInstance(char[] cArr, int i, int i2) {
        return new UCharArrayIterator(cArr, i, i2);
    }

    public static final UCharacterIterator getInstance(StringBuffer stringBuffer) {
        return new ReplaceableUCharacterIterator(stringBuffer);
    }

    public static final UCharacterIterator getInstance(CharacterIterator characterIterator) {
        return new CharacterIteratorWrapper(characterIterator);
    }

    public CharacterIterator getCharacterIterator() {
        return new UCharacterIteratorWrapper(this);
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

    @Override // ohos.global.icu.text.UForwardCharacterIterator
    public int nextCodePoint() {
        int next = next();
        char c = (char) next;
        if (UTF16.isLeadSurrogate(c)) {
            int next2 = next();
            char c2 = (char) next2;
            if (UTF16.isTrailSurrogate(c2)) {
                return Character.toCodePoint(c, c2);
            }
            if (next2 != -1) {
                previous();
            }
        }
        return next;
    }

    public int previousCodePoint() {
        int previous = previous();
        char c = (char) previous;
        if (UTF16.isTrailSurrogate(c)) {
            int previous2 = previous();
            char c2 = (char) previous2;
            if (UTF16.isLeadSurrogate(c2)) {
                return Character.toCodePoint(c2, c);
            }
            if (previous2 != -1) {
                next();
            }
        }
        return previous;
    }

    public void setToLimit() {
        setIndex(getLength());
    }

    public void setToStart() {
        setIndex(0);
    }

    public final int getText(char[] cArr) {
        return getText(cArr, 0);
    }

    public String getText() {
        char[] cArr = new char[getLength()];
        getText(cArr);
        return new String(cArr);
    }

    public int moveIndex(int i) {
        int max = Math.max(0, Math.min(getIndex() + i, getLength()));
        setIndex(max);
        return max;
    }

    public int moveCodePointIndex(int i) {
        if (i > 0) {
            while (i > 0 && nextCodePoint() != -1) {
                i--;
            }
        } else {
            while (i < 0 && previousCodePoint() != -1) {
                i++;
            }
        }
        if (i == 0) {
            return getIndex();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
