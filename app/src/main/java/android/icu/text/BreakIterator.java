package android.icu.text;

import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Type;
import java.lang.ref.SoftReference;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;
import java.util.MissingResourceException;

public abstract class BreakIterator implements Cloneable {
    private static final boolean DEBUG = false;
    public static final int DONE = -1;
    public static final int KIND_CHARACTER = 0;
    private static final int KIND_COUNT = 5;
    public static final int KIND_LINE = 2;
    public static final int KIND_SENTENCE = 3;
    public static final int KIND_TITLE = 4;
    public static final int KIND_WORD = 1;
    public static final int WORD_IDEO = 400;
    public static final int WORD_IDEO_LIMIT = 500;
    public static final int WORD_KANA = 300;
    public static final int WORD_KANA_LIMIT = 400;
    public static final int WORD_LETTER = 200;
    public static final int WORD_LETTER_LIMIT = 300;
    public static final int WORD_NONE = 0;
    public static final int WORD_NONE_LIMIT = 100;
    public static final int WORD_NUMBER = 100;
    public static final int WORD_NUMBER_LIMIT = 200;
    private static final SoftReference<?>[] iterCache = null;
    private static BreakIteratorServiceShim shim;
    private ULocale actualLocale;
    private ULocale validLocale;

    private static final class BreakIteratorCache {
        private BreakIterator iter;
        private ULocale where;

        BreakIteratorCache(ULocale where, BreakIterator iter) {
            this.where = where;
            this.iter = (BreakIterator) iter.clone();
        }

        ULocale getLocale() {
            return this.where;
        }

        BreakIterator createBreakInstance() {
            return (BreakIterator) this.iter.clone();
        }
    }

    static abstract class BreakIteratorServiceShim {
        public abstract BreakIterator createBreakIterator(ULocale uLocale, int i);

        public abstract Locale[] getAvailableLocales();

        public abstract ULocale[] getAvailableULocales();

        public abstract Object registerInstance(BreakIterator breakIterator, ULocale uLocale, int i);

        public abstract boolean unregister(Object obj);

        BreakIteratorServiceShim() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.BreakIterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.BreakIterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.BreakIterator.<clinit>():void");
    }

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
            throw new ICUCloneNotSupportedException(e);
        }
    }

    public int preceding(int offset) {
        int pos = following(offset);
        while (pos >= offset && pos != DONE) {
            pos = previous();
        }
        return pos;
    }

    public boolean isBoundary(int offset) {
        boolean z = true;
        if (offset == 0) {
            return true;
        }
        if (following(offset + DONE) != offset) {
            z = DEBUG;
        }
        return z;
    }

    public int getRuleStatus() {
        return WORD_NONE;
    }

    public int getRuleStatusVec(int[] fillInArray) {
        if (fillInArray != null && fillInArray.length > 0) {
            fillInArray[WORD_NONE] = WORD_NONE;
        }
        return KIND_WORD;
    }

    public void setText(String newText) {
        setText(new StringCharacterIterator(newText));
    }

    public static BreakIterator getWordInstance() {
        return getWordInstance(ULocale.getDefault());
    }

    public static BreakIterator getWordInstance(Locale where) {
        return getBreakInstance(ULocale.forLocale(where), KIND_WORD);
    }

    public static BreakIterator getWordInstance(ULocale where) {
        return getBreakInstance(where, KIND_WORD);
    }

    public static BreakIterator getLineInstance() {
        return getLineInstance(ULocale.getDefault());
    }

    public static BreakIterator getLineInstance(Locale where) {
        return getBreakInstance(ULocale.forLocale(where), KIND_LINE);
    }

    public static BreakIterator getLineInstance(ULocale where) {
        return getBreakInstance(where, KIND_LINE);
    }

    public static BreakIterator getCharacterInstance() {
        return getCharacterInstance(ULocale.getDefault());
    }

    public static BreakIterator getCharacterInstance(Locale where) {
        return getBreakInstance(ULocale.forLocale(where), WORD_NONE);
    }

    public static BreakIterator getCharacterInstance(ULocale where) {
        return getBreakInstance(where, WORD_NONE);
    }

    public static BreakIterator getSentenceInstance() {
        return getSentenceInstance(ULocale.getDefault());
    }

    public static BreakIterator getSentenceInstance(Locale where) {
        return getBreakInstance(ULocale.forLocale(where), KIND_SENTENCE);
    }

    public static BreakIterator getSentenceInstance(ULocale where) {
        return getBreakInstance(where, KIND_SENTENCE);
    }

    public static BreakIterator getTitleInstance() {
        return getTitleInstance(ULocale.getDefault());
    }

    public static BreakIterator getTitleInstance(Locale where) {
        return getBreakInstance(ULocale.forLocale(where), KIND_TITLE);
    }

    public static BreakIterator getTitleInstance(ULocale where) {
        return getBreakInstance(where, KIND_TITLE);
    }

    public static Object registerInstance(BreakIterator iter, Locale locale, int kind) {
        return registerInstance(iter, ULocale.forLocale(locale), kind);
    }

    public static Object registerInstance(BreakIterator iter, ULocale locale, int kind) {
        if (iterCache[kind] != null) {
            BreakIteratorCache cache = (BreakIteratorCache) iterCache[kind].get();
            if (cache != null && cache.getLocale().equals(locale)) {
                iterCache[kind] = null;
            }
        }
        return getShim().registerInstance(iter, locale, kind);
    }

    public static boolean unregister(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("registry key must not be null");
        } else if (shim == null) {
            return DEBUG;
        } else {
            for (int kind = WORD_NONE; kind < KIND_COUNT; kind += KIND_WORD) {
                iterCache[kind] = null;
            }
            return shim.unregister(key);
        }
    }

    @Deprecated
    public static BreakIterator getBreakInstance(ULocale where, int kind) {
        if (where == null) {
            throw new NullPointerException("Specified locale is null");
        }
        if (iterCache[kind] != null) {
            BreakIteratorCache cache = (BreakIteratorCache) iterCache[kind].get();
            if (cache != null && cache.getLocale().equals(where)) {
                return cache.createBreakInstance();
            }
        }
        BreakIterator result = getShim().createBreakIterator(where, kind);
        iterCache[kind] = new SoftReference(new BreakIteratorCache(where, result));
        if (result instanceof RuleBasedBreakIterator) {
            ((RuleBasedBreakIterator) result).setBreakType(kind);
        }
        return result;
    }

    public static synchronized Locale[] getAvailableLocales() {
        Locale[] availableLocales;
        synchronized (BreakIterator.class) {
            availableLocales = getShim().getAvailableLocales();
        }
        return availableLocales;
    }

    public static synchronized ULocale[] getAvailableULocales() {
        ULocale[] availableULocales;
        synchronized (BreakIterator.class) {
            availableULocales = getShim().getAvailableULocales();
        }
        return availableULocales;
    }

    private static BreakIteratorServiceShim getShim() {
        if (shim == null) {
            try {
                shim = (BreakIteratorServiceShim) Class.forName("android.icu.text.BreakIteratorFactory").newInstance();
            } catch (MissingResourceException e) {
                throw e;
            } catch (Exception e2) {
                if (DEBUG) {
                    e2.printStackTrace();
                }
                throw new RuntimeException(e2.getMessage());
            }
        }
        return shim;
    }

    public final ULocale getLocale(Type type) {
        return type == ULocale.ACTUAL_LOCALE ? this.actualLocale : this.validLocale;
    }

    final void setLocale(ULocale valid, ULocale actual) {
        Object obj;
        Object obj2 = KIND_WORD;
        if (valid == null) {
            obj = KIND_WORD;
        } else {
            obj = WORD_NONE;
        }
        if (actual != null) {
            obj2 = WORD_NONE;
        }
        if (obj != obj2) {
            throw new IllegalArgumentException();
        }
        this.validLocale = valid;
        this.actualLocale = actual;
    }
}
