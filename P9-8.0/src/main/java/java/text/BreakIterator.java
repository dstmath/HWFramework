package java.text;

import java.util.Locale;

public abstract class BreakIterator implements Cloneable {
    public static final int DONE = -1;

    public abstract int current();

    public abstract int first();

    public abstract int following(int i);

    public abstract CharacterIterator getText();

    public abstract int last();

    public abstract int next();

    public abstract int next(int i);

    public abstract int previous();

    public abstract void setText(CharacterIterator characterIterator);

    protected BreakIterator() {
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    public int preceding(int offset) {
        int pos = following(offset);
        while (pos >= offset && pos != -1) {
            pos = previous();
        }
        return pos;
    }

    public boolean isBoundary(int offset) {
        boolean z = true;
        if (offset == 0) {
            return true;
        }
        int boundary = following(offset - 1);
        if (boundary == -1) {
            throw new IllegalArgumentException();
        }
        if (boundary != offset) {
            z = false;
        }
        return z;
    }

    public void setText(String newText) {
        setText(new StringCharacterIterator(newText));
    }

    public static BreakIterator getWordInstance() {
        return getWordInstance(Locale.getDefault());
    }

    public static BreakIterator getWordInstance(Locale locale) {
        return new IcuIteratorWrapper(android.icu.text.BreakIterator.getWordInstance(locale));
    }

    public static BreakIterator getLineInstance() {
        return getLineInstance(Locale.getDefault());
    }

    public static BreakIterator getLineInstance(Locale locale) {
        return new IcuIteratorWrapper(android.icu.text.BreakIterator.getLineInstance(locale));
    }

    public static BreakIterator getCharacterInstance() {
        return getCharacterInstance(Locale.getDefault());
    }

    public static BreakIterator getCharacterInstance(Locale locale) {
        return new IcuIteratorWrapper(android.icu.text.BreakIterator.getCharacterInstance(locale));
    }

    public static BreakIterator getSentenceInstance() {
        return getSentenceInstance(Locale.getDefault());
    }

    public static BreakIterator getSentenceInstance(Locale locale) {
        return new IcuIteratorWrapper(android.icu.text.BreakIterator.getSentenceInstance(locale));
    }

    public static synchronized Locale[] getAvailableLocales() {
        Locale[] availableLocales;
        synchronized (BreakIterator.class) {
            availableLocales = android.icu.text.BreakIterator.getAvailableLocales();
        }
        return availableLocales;
    }
}
