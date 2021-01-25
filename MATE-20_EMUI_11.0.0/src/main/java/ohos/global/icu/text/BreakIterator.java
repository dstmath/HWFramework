package ohos.global.icu.text;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import ohos.global.icu.impl.CSCharacterIterator;
import ohos.global.icu.impl.CacheValue;
import ohos.global.icu.impl.ICUDebug;
import ohos.global.icu.util.ICUCloneNotSupportedException;
import ohos.global.icu.util.ULocale;

public abstract class BreakIterator implements Cloneable {
    private static final boolean DEBUG = ICUDebug.enabled("breakiterator");
    public static final int DONE = -1;
    public static final int KIND_CHARACTER = 0;
    private static final int KIND_COUNT = 5;
    public static final int KIND_LINE = 2;
    public static final int KIND_SENTENCE = 3;
    @Deprecated
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
    private static final CacheValue<?>[] iterCache = new CacheValue[5];
    private static BreakIteratorServiceShim shim;
    private ULocale actualLocale;
    private ULocale validLocale;

    public abstract int current();

    public abstract int first();

    public abstract int following(int i);

    public int getRuleStatus() {
        return 0;
    }

    public abstract CharacterIterator getText();

    public abstract int last();

    public abstract int next();

    public abstract int next(int i);

    public abstract int previous();

    public abstract void setText(CharacterIterator characterIterator);

    protected BreakIterator() {
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    public int preceding(int i) {
        int following = following(i);
        while (following >= i && following != -1) {
            following = previous();
        }
        return following;
    }

    public boolean isBoundary(int i) {
        return i == 0 || following(i + -1) == i;
    }

    public int getRuleStatusVec(int[] iArr) {
        if (iArr == null || iArr.length <= 0) {
            return 1;
        }
        iArr[0] = 0;
        return 1;
    }

    public void setText(String str) {
        setText(new StringCharacterIterator(str));
    }

    public void setText(CharSequence charSequence) {
        setText(new CSCharacterIterator(charSequence));
    }

    public static BreakIterator getWordInstance() {
        return getWordInstance(ULocale.getDefault());
    }

    public static BreakIterator getWordInstance(Locale locale) {
        return getBreakInstance(ULocale.forLocale(locale), 1);
    }

    public static BreakIterator getWordInstance(ULocale uLocale) {
        return getBreakInstance(uLocale, 1);
    }

    public static BreakIterator getLineInstance() {
        return getLineInstance(ULocale.getDefault());
    }

    public static BreakIterator getLineInstance(Locale locale) {
        return getBreakInstance(ULocale.forLocale(locale), 2);
    }

    public static BreakIterator getLineInstance(ULocale uLocale) {
        return getBreakInstance(uLocale, 2);
    }

    public static BreakIterator getCharacterInstance() {
        return getCharacterInstance(ULocale.getDefault());
    }

    public static BreakIterator getCharacterInstance(Locale locale) {
        return getBreakInstance(ULocale.forLocale(locale), 0);
    }

    public static BreakIterator getCharacterInstance(ULocale uLocale) {
        return getBreakInstance(uLocale, 0);
    }

    public static BreakIterator getSentenceInstance() {
        return getSentenceInstance(ULocale.getDefault());
    }

    public static BreakIterator getSentenceInstance(Locale locale) {
        return getBreakInstance(ULocale.forLocale(locale), 3);
    }

    public static BreakIterator getSentenceInstance(ULocale uLocale) {
        return getBreakInstance(uLocale, 3);
    }

    @Deprecated
    public static BreakIterator getTitleInstance() {
        return getTitleInstance(ULocale.getDefault());
    }

    @Deprecated
    public static BreakIterator getTitleInstance(Locale locale) {
        return getBreakInstance(ULocale.forLocale(locale), 4);
    }

    @Deprecated
    public static BreakIterator getTitleInstance(ULocale uLocale) {
        return getBreakInstance(uLocale, 4);
    }

    public static Object registerInstance(BreakIterator breakIterator, Locale locale, int i) {
        return registerInstance(breakIterator, ULocale.forLocale(locale), i);
    }

    public static Object registerInstance(BreakIterator breakIterator, ULocale uLocale, int i) {
        BreakIteratorCache breakIteratorCache;
        CacheValue<?>[] cacheValueArr = iterCache;
        if (!(cacheValueArr[i] == null || (breakIteratorCache = (BreakIteratorCache) cacheValueArr[i].get()) == null || !breakIteratorCache.getLocale().equals(uLocale))) {
            iterCache[i] = null;
        }
        return getShim().registerInstance(breakIterator, uLocale, i);
    }

    public static boolean unregister(Object obj) {
        if (obj != null) {
            if (shim == null) {
                return false;
            }
            for (int i = 0; i < 5; i++) {
                iterCache[i] = null;
            }
            return shim.unregister(obj);
        }
        throw new IllegalArgumentException("registry key must not be null");
    }

    @Deprecated
    public static BreakIterator getBreakInstance(ULocale uLocale, int i) {
        BreakIteratorCache breakIteratorCache;
        if (uLocale != null) {
            CacheValue<?>[] cacheValueArr = iterCache;
            if (cacheValueArr[i] != null && (breakIteratorCache = (BreakIteratorCache) cacheValueArr[i].get()) != null && breakIteratorCache.getLocale().equals(uLocale)) {
                return breakIteratorCache.createBreakInstance();
            }
            BreakIterator createBreakIterator = getShim().createBreakIterator(uLocale, i);
            iterCache[i] = CacheValue.getInstance(new BreakIteratorCache(uLocale, createBreakIterator));
            return createBreakIterator;
        }
        throw new NullPointerException("Specified locale is null");
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

    /* access modifiers changed from: private */
    public static final class BreakIteratorCache {
        private BreakIterator iter;
        private ULocale where;

        BreakIteratorCache(ULocale uLocale, BreakIterator breakIterator) {
            this.where = uLocale;
            this.iter = (BreakIterator) breakIterator.clone();
        }

        /* access modifiers changed from: package-private */
        public ULocale getLocale() {
            return this.where;
        }

        /* access modifiers changed from: package-private */
        public BreakIterator createBreakInstance() {
            return (BreakIterator) this.iter.clone();
        }
    }

    /* access modifiers changed from: package-private */
    public static abstract class BreakIteratorServiceShim {
        public abstract BreakIterator createBreakIterator(ULocale uLocale, int i);

        public abstract Locale[] getAvailableLocales();

        public abstract ULocale[] getAvailableULocales();

        public abstract Object registerInstance(BreakIterator breakIterator, ULocale uLocale, int i);

        public abstract boolean unregister(Object obj);

        BreakIteratorServiceShim() {
        }
    }

    private static BreakIteratorServiceShim getShim() {
        if (shim == null) {
            try {
                shim = (BreakIteratorServiceShim) Class.forName("ohos.global.icu.text.BreakIteratorFactory").newInstance();
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

    public final ULocale getLocale(ULocale.Type type) {
        return type == ULocale.ACTUAL_LOCALE ? this.actualLocale : this.validLocale;
    }

    /* access modifiers changed from: package-private */
    public final void setLocale(ULocale uLocale, ULocale uLocale2) {
        boolean z = true;
        boolean z2 = uLocale == null;
        if (uLocale2 != null) {
            z = false;
        }
        if (z2 == z) {
            this.validLocale = uLocale;
            this.actualLocale = uLocale2;
            return;
        }
        throw new IllegalArgumentException();
    }
}
