package android.icu.text;

import android.icu.impl.ICUDebug;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.UResource;
import android.icu.impl.coll.CollationRoot;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.util.Freezable;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.VersionInfo;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

public abstract class Collator implements Comparator<Object>, Freezable<Collator>, Cloneable {
    private static final String BASE = "android/icu/impl/data/icudt60b/coll";
    public static final int CANONICAL_DECOMPOSITION = 17;
    private static final boolean DEBUG = ICUDebug.enabled("collator");
    public static final int FULL_DECOMPOSITION = 15;
    public static final int IDENTICAL = 15;
    private static final String[] KEYWORDS = {"collation"};
    public static final int NO_DECOMPOSITION = 16;
    public static final int PRIMARY = 0;
    public static final int QUATERNARY = 3;
    private static final String RESOURCE = "collations";
    public static final int SECONDARY = 1;
    public static final int TERTIARY = 2;
    private static ServiceShim shim;

    private static final class ASCII {
        private ASCII() {
        }

        static boolean equalIgnoreCase(CharSequence left, CharSequence right) {
            int length = left.length();
            if (length != right.length()) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                char lc = left.charAt(i);
                char rc = right.charAt(i);
                if (lc != rc) {
                    if ('A' > lc || lc > 'Z') {
                        if ('A' <= rc && rc <= 'Z' && rc + ' ' == lc) {
                        }
                    } else if (lc + ' ' == rc) {
                    }
                    return false;
                }
            }
            return true;
        }
    }

    public static abstract class CollatorFactory {
        public abstract Set<String> getSupportedLocaleIDs();

        public boolean visible() {
            return true;
        }

        public Collator createCollator(ULocale loc) {
            return createCollator(loc.toLocale());
        }

        public Collator createCollator(Locale loc) {
            return createCollator(ULocale.forLocale(loc));
        }

        public String getDisplayName(Locale objectLocale, Locale displayLocale) {
            return getDisplayName(ULocale.forLocale(objectLocale), ULocale.forLocale(displayLocale));
        }

        public String getDisplayName(ULocale objectLocale, ULocale displayLocale) {
            if (!visible() || !getSupportedLocaleIDs().contains(objectLocale.getBaseName())) {
                return null;
            }
            return objectLocale.getDisplayName(displayLocale);
        }

        protected CollatorFactory() {
        }
    }

    private static final class KeywordsSink extends UResource.Sink {
        boolean hasDefault;
        LinkedList<String> values;

        private KeywordsSink() {
            this.values = new LinkedList<>();
            this.hasDefault = false;
        }

        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table collations = value.getTable();
            for (int i = 0; collations.getKeyAndValue(i, key, value); i++) {
                int type = value.getType();
                if (type == 0) {
                    if (!this.hasDefault && key.contentEquals("default")) {
                        String defcoll = value.getString();
                        if (!defcoll.isEmpty()) {
                            this.values.remove(defcoll);
                            this.values.addFirst(defcoll);
                            this.hasDefault = true;
                        }
                    }
                } else if (type == 2 && !key.startsWith("private-")) {
                    String collkey = key.toString();
                    if (!this.values.contains(collkey)) {
                        this.values.add(collkey);
                    }
                }
            }
        }
    }

    public interface ReorderCodes {
        public static final int CURRENCY = 4099;
        public static final int DEFAULT = -1;
        public static final int DIGIT = 4100;
        public static final int FIRST = 4096;
        @Deprecated
        public static final int LIMIT = 4101;
        public static final int NONE = 103;
        public static final int OTHERS = 103;
        public static final int PUNCTUATION = 4097;
        public static final int SPACE = 4096;
        public static final int SYMBOL = 4098;
    }

    static abstract class ServiceShim {
        /* access modifiers changed from: package-private */
        public abstract Locale[] getAvailableLocales();

        /* access modifiers changed from: package-private */
        public abstract ULocale[] getAvailableULocales();

        /* access modifiers changed from: package-private */
        public abstract String getDisplayName(ULocale uLocale, ULocale uLocale2);

        /* access modifiers changed from: package-private */
        public abstract Collator getInstance(ULocale uLocale);

        /* access modifiers changed from: package-private */
        public abstract Object registerFactory(CollatorFactory collatorFactory);

        /* access modifiers changed from: package-private */
        public abstract Object registerInstance(Collator collator, ULocale uLocale);

        /* access modifiers changed from: package-private */
        public abstract boolean unregister(Object obj);

        ServiceShim() {
        }
    }

    public abstract int compare(String str, String str2);

    public abstract CollationKey getCollationKey(String str);

    public abstract RawCollationKey getRawCollationKey(String str, RawCollationKey rawCollationKey);

    public abstract VersionInfo getUCAVersion();

    public abstract int getVariableTop();

    public abstract VersionInfo getVersion();

    @Deprecated
    public abstract int setVariableTop(String str);

    @Deprecated
    public abstract void setVariableTop(int i);

    public boolean equals(Object obj) {
        return this == obj || (obj != null && getClass() == obj.getClass());
    }

    public int hashCode() {
        return 0;
    }

    private void checkNotFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen Collator");
        }
    }

    public void setStrength(int newStrength) {
        checkNotFrozen();
    }

    @Deprecated
    public Collator setStrength2(int newStrength) {
        setStrength(newStrength);
        return this;
    }

    public void setDecomposition(int decomposition) {
        checkNotFrozen();
    }

    public void setReorderCodes(int... order) {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    public static final Collator getInstance() {
        return getInstance(ULocale.getDefault());
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private static ServiceShim getShim() {
        if (shim == null) {
            try {
                shim = (ServiceShim) Class.forName("android.icu.text.CollatorServiceShim").newInstance();
            } catch (MissingResourceException e) {
                throw e;
            } catch (Exception e2) {
                if (DEBUG) {
                    e2.printStackTrace();
                }
                throw new ICUException((Throwable) e2);
            }
        }
        return shim;
    }

    private static final boolean getYesOrNo(String keyword, String s) {
        if (ASCII.equalIgnoreCase(s, "yes")) {
            return true;
        }
        if (ASCII.equalIgnoreCase(s, "no")) {
            return false;
        }
        throw new IllegalArgumentException("illegal locale keyword=value: " + keyword + "=" + s);
    }

    private static final int getIntValue(String keyword, String s, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (ASCII.equalIgnoreCase(s, values[i])) {
                return i;
            }
        }
        throw new IllegalArgumentException("illegal locale keyword=value: " + keyword + "=" + s);
    }

    private static final int getReorderCode(String keyword, String s) {
        return 4096 + getIntValue(keyword, s, "space", "punct", "symbol", "currency", "digit");
    }

    private static void setAttributesFromKeywords(ULocale loc, Collator coll, RuleBasedCollator rbc) {
        int code;
        if (loc.getKeywordValue("colHiraganaQuaternary") != null) {
            throw new UnsupportedOperationException("locale keyword kh/colHiraganaQuaternary");
        } else if (loc.getKeywordValue("variableTop") == null) {
            String value = loc.getKeywordValue("colStrength");
            if (value != null) {
                int strength = getIntValue("colStrength", value, "primary", "secondary", "tertiary", "quaternary", "identical");
                coll.setStrength(strength <= 3 ? strength : 15);
            }
            String value2 = loc.getKeywordValue("colBackwards");
            if (value2 != null) {
                if (rbc != null) {
                    rbc.setFrenchCollation(getYesOrNo("colBackwards", value2));
                } else {
                    throw new UnsupportedOperationException("locale keyword kb/colBackwards only settable for RuleBasedCollator");
                }
            }
            String value3 = loc.getKeywordValue("colCaseLevel");
            if (value3 != null) {
                if (rbc != null) {
                    rbc.setCaseLevel(getYesOrNo("colCaseLevel", value3));
                } else {
                    throw new UnsupportedOperationException("locale keyword kb/colBackwards only settable for RuleBasedCollator");
                }
            }
            String value4 = loc.getKeywordValue("colCaseFirst");
            boolean z = true;
            if (value4 != null) {
                if (rbc != null) {
                    int cf = getIntValue("colCaseFirst", value4, "no", "lower", "upper");
                    if (cf == 0) {
                        rbc.setLowerCaseFirst(false);
                        rbc.setUpperCaseFirst(false);
                    } else if (cf == 1) {
                        rbc.setLowerCaseFirst(true);
                    } else {
                        rbc.setUpperCaseFirst(true);
                    }
                } else {
                    throw new UnsupportedOperationException("locale keyword kf/colCaseFirst only settable for RuleBasedCollator");
                }
            }
            String value5 = loc.getKeywordValue("colAlternate");
            if (value5 != null) {
                if (rbc != null) {
                    if (getIntValue("colAlternate", value5, "non-ignorable", "shifted") == 0) {
                        z = false;
                    }
                    rbc.setAlternateHandlingShifted(z);
                } else {
                    throw new UnsupportedOperationException("locale keyword ka/colAlternate only settable for RuleBasedCollator");
                }
            }
            String value6 = loc.getKeywordValue("colNormalization");
            if (value6 != null) {
                coll.setDecomposition(getYesOrNo("colNormalization", value6) ? 17 : 16);
            }
            String value7 = loc.getKeywordValue("colNumeric");
            if (value7 != null) {
                if (rbc != null) {
                    rbc.setNumericCollation(getYesOrNo("colNumeric", value7));
                } else {
                    throw new UnsupportedOperationException("locale keyword kn/colNumeric only settable for RuleBasedCollator");
                }
            }
            String value8 = loc.getKeywordValue("colReorder");
            if (value8 != null) {
                int[] codes = new int[183];
                int codesLength = 0;
                int scriptNameStart = 0;
                while (codesLength != codes.length) {
                    int limit = scriptNameStart;
                    while (limit < value8.length() && value8.charAt(limit) != '-') {
                        limit++;
                    }
                    String scriptName = value8.substring(scriptNameStart, limit);
                    if (scriptName.length() == 4) {
                        code = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, scriptName);
                    } else {
                        code = getReorderCode("colReorder", scriptName);
                    }
                    int codesLength2 = codesLength + 1;
                    codes[codesLength] = code;
                    if (limit != value8.length()) {
                        scriptNameStart = limit + 1;
                        codesLength = codesLength2;
                    } else if (codesLength2 != 0) {
                        int[] args = new int[codesLength2];
                        System.arraycopy(codes, 0, args, 0, codesLength2);
                        coll.setReorderCodes(args);
                    } else {
                        throw new IllegalArgumentException("no script codes for colReorder locale keyword");
                    }
                }
                throw new IllegalArgumentException("too many script codes for colReorder locale keyword: " + value8);
            }
            String value9 = loc.getKeywordValue("kv");
            if (value9 != null) {
                coll.setMaxVariable(getReorderCode("kv", value9));
            }
        } else {
            throw new UnsupportedOperationException("locale keyword vt/variableTop");
        }
    }

    public static final Collator getInstance(ULocale locale) {
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        Collator coll = getShim().getInstance(locale);
        if (!locale.getName().equals(locale.getBaseName())) {
            setAttributesFromKeywords(locale, coll, coll instanceof RuleBasedCollator ? (RuleBasedCollator) coll : null);
        }
        return coll;
    }

    public static final Collator getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    public static final Object registerInstance(Collator collator, ULocale locale) {
        return getShim().registerInstance(collator, locale);
    }

    public static final Object registerFactory(CollatorFactory factory) {
        return getShim().registerFactory(factory);
    }

    public static final boolean unregister(Object registryKey) {
        if (shim == null) {
            return false;
        }
        return shim.unregister(registryKey);
    }

    public static Locale[] getAvailableLocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableLocales("android/icu/impl/data/icudt60b/coll", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return shim.getAvailableLocales();
    }

    public static final ULocale[] getAvailableULocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableULocales("android/icu/impl/data/icudt60b/coll", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return shim.getAvailableULocales();
    }

    public static final String[] getKeywords() {
        return KEYWORDS;
    }

    public static final String[] getKeywordValues(String keyword) {
        if (keyword.equals(KEYWORDS[0])) {
            return ICUResourceBundle.getKeywordValues("android/icu/impl/data/icudt60b/coll", RESOURCE);
        }
        throw new IllegalArgumentException("Invalid keyword: " + keyword);
    }

    public static final String[] getKeywordValuesForLocale(String key, ULocale locale, boolean commonlyUsed) {
        KeywordsSink sink = new KeywordsSink();
        ((ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt60b/coll", locale)).getAllItemsWithFallback(RESOURCE, sink);
        return (String[]) sink.values.toArray(new String[sink.values.size()]);
    }

    public static final ULocale getFunctionalEquivalent(String keyword, ULocale locID, boolean[] isAvailable) {
        return ICUResourceBundle.getFunctionalEquivalent("android/icu/impl/data/icudt60b/coll", ICUResourceBundle.ICU_DATA_CLASS_LOADER, RESOURCE, keyword, locID, isAvailable, true);
    }

    public static final ULocale getFunctionalEquivalent(String keyword, ULocale locID) {
        return getFunctionalEquivalent(keyword, locID, null);
    }

    public static String getDisplayName(Locale objectLocale, Locale displayLocale) {
        return getShim().getDisplayName(ULocale.forLocale(objectLocale), ULocale.forLocale(displayLocale));
    }

    public static String getDisplayName(ULocale objectLocale, ULocale displayLocale) {
        return getShim().getDisplayName(objectLocale, displayLocale);
    }

    public static String getDisplayName(Locale objectLocale) {
        return getShim().getDisplayName(ULocale.forLocale(objectLocale), ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public static String getDisplayName(ULocale objectLocale) {
        return getShim().getDisplayName(objectLocale, ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public int getStrength() {
        return 2;
    }

    public int getDecomposition() {
        return 16;
    }

    public boolean equals(String source, String target) {
        return compare(source, target) == 0;
    }

    public UnicodeSet getTailoredSet() {
        return new UnicodeSet(0, 1114111);
    }

    public int compare(Object source, Object target) {
        return doCompare((CharSequence) source, (CharSequence) target);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int doCompare(CharSequence left, CharSequence right) {
        return compare(left.toString(), right.toString());
    }

    public Collator setMaxVariable(int group) {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    public int getMaxVariable() {
        return 4097;
    }

    public int[] getReorderCodes() {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    public static int[] getEquivalentReorderCodes(int reorderCode) {
        return CollationRoot.getData().getEquivalentScripts(reorderCode);
    }

    public boolean isFrozen() {
        return false;
    }

    public Collator freeze() {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    public Collator cloneAsThawed() {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    protected Collator() {
    }

    public ULocale getLocale(ULocale.Type type) {
        return ULocale.ROOT;
    }

    /* access modifiers changed from: package-private */
    public void setLocale(ULocale valid, ULocale actual) {
    }
}
