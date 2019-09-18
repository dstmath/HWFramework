package android.icu.text;

import android.icu.impl.CharacterIteratorWrapper;
import android.icu.impl.ReplaceableUCharacterIterator;
import android.icu.impl.UCharArrayIterator;
import android.icu.impl.UCharacterIteratorWrapper;
import java.text.CharacterIterator;

public abstract class UCharacterIterator implements Cloneable, UForwardCharacterIterator {
    public abstract int current();

    public abstract int getIndex();

    public abstract int getLength();

    public abstract int getText(char[] cArr, int i);

    public abstract int next();

    public abstract int previous();

    public abstract void setIndex(int i);

    protected UCharacterIterator() {
    }

    public static final UCharacterIterator getInstance(Replaceable source) {
        return new ReplaceableUCharacterIterator(source);
    }

    public static final UCharacterIterator getInstance(String source) {
        return new ReplaceableUCharacterIterator(source);
    }

    public static final UCharacterIterator getInstance(char[] source) {
        return getInstance(source, 0, source.length);
    }

    public static final UCharacterIterator getInstance(char[] source, int start, int limit) {
        return new UCharArrayIterator(source, start, limit);
    }

    public static final UCharacterIterator getInstance(StringBuffer source) {
        return new ReplaceableUCharacterIterator(source);
    }

    public static final UCharacterIterator getInstance(CharacterIterator source) {
        return new CharacterIteratorWrapper(source);
    }

    public CharacterIterator getCharacterIterator() {
        return new UCharacterIteratorWrapper(this);
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

    public int nextCodePoint() {
        int ch1 = next();
        if (UTF16.isLeadSurrogate((char) ch1)) {
            int ch2 = next();
            if (UTF16.isTrailSurrogate((char) ch2)) {
                return Character.toCodePoint((char) ch1, (char) ch2);
            }
            if (ch2 != -1) {
                previous();
            }
        }
        return ch1;
    }

    public int previousCodePoint() {
        int ch1 = previous();
        if (UTF16.isTrailSurrogate((char) ch1)) {
            int ch2 = previous();
            if (UTF16.isLeadSurrogate((char) ch2)) {
                return Character.toCodePoint((char) ch2, (char) ch1);
            }
            if (ch2 != -1) {
                next();
            }
        }
        return ch1;
    }

    public void setToLimit() {
        setIndex(getLength());
    }

    public void setToStart() {
        setIndex(0);
    }

    public final int getText(char[] fillIn) {
        return getText(fillIn, 0);
    }

    public String getText() {
        char[] text = new char[getLength()];
        getText(text);
        return new String(text);
    }

    public int moveIndex(int delta) {
        int x = Math.max(0, Math.min(getIndex() + delta, getLength()));
        setIndex(x);
        return x;
    }

    public int moveCodePointIndex(int delta) {
        if (delta > 0) {
            while (delta > 0 && nextCodePoint() != -1) {
                delta--;
            }
        } else {
            while (delta < 0 && previousCodePoint() != -1) {
                delta++;
            }
        }
        if (delta == 0) {
            return getIndex();
        }
        throw new IndexOutOfBoundsException();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
