package ohos.global.icu.text;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import ohos.global.icu.impl.ICUDebug;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.impl.coll.CollationRoot;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.util.Freezable;
import ohos.global.icu.util.ICUException;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;
import ohos.global.icu.util.VersionInfo;

public abstract class Collator implements Comparator<Object>, Freezable<Collator>, Cloneable {
    private static final String BASE = "ohos/global/icu/impl/data/icudt66b/coll";
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

    public abstract int compare(String str, String str2);

    public abstract CollationKey getCollationKey(String str);

    public int getDecomposition() {
        return 16;
    }

    public int getMaxVariable() {
        return ReorderCodes.PUNCTUATION;
    }

    public abstract RawCollationKey getRawCollationKey(String str, RawCollationKey rawCollationKey);

    public int getStrength() {
        return 2;
    }

    public abstract VersionInfo getUCAVersion();

    public abstract int getVariableTop();

    public abstract VersionInfo getVersion();

    @Override // java.lang.Object
    public int hashCode() {
        return 0;
    }

    @Override // ohos.global.icu.util.Freezable
    public boolean isFrozen() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setLocale(ULocale uLocale, ULocale uLocale2) {
    }

    @Deprecated
    public abstract int setVariableTop(String str);

    @Deprecated
    public abstract void setVariableTop(int i);

    @Override // java.util.Comparator, java.lang.Object
    public boolean equals(Object obj) {
        return this == obj || (obj != null && getClass() == obj.getClass());
    }

    private void checkNotFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen Collator");
        }
    }

    public void setStrength(int i) {
        checkNotFrozen();
    }

    @Deprecated
    public Collator setStrength2(int i) {
        setStrength(i);
        return this;
    }

    public void setDecomposition(int i) {
        checkNotFrozen();
    }

    public void setReorderCodes(int... iArr) {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    public static final Collator getInstance() {
        return getInstance(ULocale.getDefault());
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static abstract class CollatorFactory {
        public abstract Set<String> getSupportedLocaleIDs();

        public boolean visible() {
            return true;
        }

        public Collator createCollator(ULocale uLocale) {
            return createCollator(uLocale.toLocale());
        }

        public Collator createCollator(Locale locale) {
            return createCollator(ULocale.forLocale(locale));
        }

        public String getDisplayName(Locale locale, Locale locale2) {
            return getDisplayName(ULocale.forLocale(locale), ULocale.forLocale(locale2));
        }

        public String getDisplayName(ULocale uLocale, ULocale uLocale2) {
            if (!visible() || !getSupportedLocaleIDs().contains(uLocale.getBaseName())) {
                return null;
            }
            return uLocale.getDisplayName(uLocale2);
        }

        protected CollatorFactory() {
        }
    }

    /* access modifiers changed from: package-private */
    public static abstract class ServiceShim {
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

    private static ServiceShim getShim() {
        if (shim == null) {
            try {
                shim = (ServiceShim) Class.forName("ohos.global.icu.text.CollatorServiceShim").newInstance();
            } catch (MissingResourceException e) {
                throw e;
            } catch (Exception e2) {
                if (DEBUG) {
                    e2.printStackTrace();
                }
                throw new ICUException(e2);
            }
        }
        return shim;
    }

    /* access modifiers changed from: private */
    public static final class ASCII {
        private ASCII() {
        }

        static boolean equalIgnoreCase(CharSequence charSequence, CharSequence charSequence2) {
            int length = charSequence.length();
            if (length != charSequence2.length()) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                char charAt = charSequence.charAt(i);
                char charAt2 = charSequence2.charAt(i);
                if (charAt != charAt2) {
                    if ('A' > charAt || charAt > 'Z') {
                        if ('A' <= charAt2 && charAt2 <= 'Z' && charAt2 + ' ' == charAt) {
                        }
                    } else if (charAt + ' ' == charAt2) {
                    }
                    return false;
                }
            }
            return true;
        }
    }

    private static final boolean getYesOrNo(String str, String str2) {
        if (ASCII.equalIgnoreCase(str2, "yes")) {
            return true;
        }
        if (ASCII.equalIgnoreCase(str2, "no")) {
            return false;
        }
        throw new IllegalArgumentException("illegal locale keyword=value: " + str + "=" + str2);
    }

    private static final int getIntValue(String str, String str2, String... strArr) {
        for (int i = 0; i < strArr.length; i++) {
            if (ASCII.equalIgnoreCase(str2, strArr[i])) {
                return i;
            }
        }
        throw new IllegalArgumentException("illegal locale keyword=value: " + str + "=" + str2);
    }

    private static final int getReorderCode(String str, String str2) {
        return getIntValue(str, str2, "space", "punct", "symbol", "currency", "digit") + 4096;
    }

    private static void setAttributesFromKeywords(ULocale uLocale, Collator collator, RuleBasedCollator ruleBasedCollator) {
        int i;
        if (uLocale.getKeywordValue("colHiraganaQuaternary") != null) {
            throw new UnsupportedOperationException("locale keyword kh/colHiraganaQuaternary");
        } else if (uLocale.getKeywordValue("variableTop") == null) {
            String keywordValue = uLocale.getKeywordValue("colStrength");
            if (keywordValue != null) {
                int intValue = getIntValue("colStrength", keywordValue, "primary", "secondary", "tertiary", "quaternary", "identical");
                if (intValue > 3) {
                    intValue = 15;
                }
                collator.setStrength(intValue);
            }
            String keywordValue2 = uLocale.getKeywordValue("colBackwards");
            if (keywordValue2 != null) {
                if (ruleBasedCollator != null) {
                    ruleBasedCollator.setFrenchCollation(getYesOrNo("colBackwards", keywordValue2));
                } else {
                    throw new UnsupportedOperationException("locale keyword kb/colBackwards only settable for RuleBasedCollator");
                }
            }
            String keywordValue3 = uLocale.getKeywordValue("colCaseLevel");
            if (keywordValue3 != null) {
                if (ruleBasedCollator != null) {
                    ruleBasedCollator.setCaseLevel(getYesOrNo("colCaseLevel", keywordValue3));
                } else {
                    throw new UnsupportedOperationException("locale keyword kb/colBackwards only settable for RuleBasedCollator");
                }
            }
            String keywordValue4 = uLocale.getKeywordValue("colCaseFirst");
            boolean z = true;
            if (keywordValue4 != null) {
                if (ruleBasedCollator != null) {
                    int intValue2 = getIntValue("colCaseFirst", keywordValue4, "no", "lower", "upper");
                    if (intValue2 == 0) {
                        ruleBasedCollator.setLowerCaseFirst(false);
                        ruleBasedCollator.setUpperCaseFirst(false);
                    } else if (intValue2 == 1) {
                        ruleBasedCollator.setLowerCaseFirst(true);
                    } else {
                        ruleBasedCollator.setUpperCaseFirst(true);
                    }
                } else {
                    throw new UnsupportedOperationException("locale keyword kf/colCaseFirst only settable for RuleBasedCollator");
                }
            }
            String keywordValue5 = uLocale.getKeywordValue("colAlternate");
            if (keywordValue5 != null) {
                if (ruleBasedCollator != null) {
                    if (getIntValue("colAlternate", keywordValue5, "non-ignorable", "shifted") == 0) {
                        z = false;
                    }
                    ruleBasedCollator.setAlternateHandlingShifted(z);
                } else {
                    throw new UnsupportedOperationException("locale keyword ka/colAlternate only settable for RuleBasedCollator");
                }
            }
            String keywordValue6 = uLocale.getKeywordValue("colNormalization");
            if (keywordValue6 != null) {
                collator.setDecomposition(getYesOrNo("colNormalization", keywordValue6) ? 17 : 16);
            }
            String keywordValue7 = uLocale.getKeywordValue("colNumeric");
            if (keywordValue7 != null) {
                if (ruleBasedCollator != null) {
                    ruleBasedCollator.setNumericCollation(getYesOrNo("colNumeric", keywordValue7));
                } else {
                    throw new UnsupportedOperationException("locale keyword kn/colNumeric only settable for RuleBasedCollator");
                }
            }
            String keywordValue8 = uLocale.getKeywordValue("colReorder");
            if (keywordValue8 != null) {
                int[] iArr = new int[198];
                int i2 = 0;
                int i3 = 0;
                while (i2 != iArr.length) {
                    int i4 = i3;
                    while (i4 < keywordValue8.length() && keywordValue8.charAt(i4) != '-') {
                        i4++;
                    }
                    String substring = keywordValue8.substring(i3, i4);
                    if (substring.length() == 4) {
                        i = UCharacter.getPropertyValueEnum(4106, substring);
                    } else {
                        i = getReorderCode("colReorder", substring);
                    }
                    int i5 = i2 + 1;
                    iArr[i2] = i;
                    if (i4 != keywordValue8.length()) {
                        i3 = i4 + 1;
                        i2 = i5;
                    } else if (i5 != 0) {
                        int[] iArr2 = new int[i5];
                        System.arraycopy(iArr, 0, iArr2, 0, i5);
                        collator.setReorderCodes(iArr2);
                    } else {
                        throw new IllegalArgumentException("no script codes for colReorder locale keyword");
                    }
                }
                throw new IllegalArgumentException("too many script codes for colReorder locale keyword: " + keywordValue8);
            }
            String keywordValue9 = uLocale.getKeywordValue("kv");
            if (keywordValue9 != null) {
                collator.setMaxVariable(getReorderCode("kv", keywordValue9));
            }
        } else {
            throw new UnsupportedOperationException("locale keyword vt/variableTop");
        }
    }

    public static final Collator getInstance(ULocale uLocale) {
        if (uLocale == null) {
            uLocale = ULocale.getDefault();
        }
        Collator instance = getShim().getInstance(uLocale);
        if (!uLocale.getName().equals(uLocale.getBaseName())) {
            setAttributesFromKeywords(uLocale, instance, instance instanceof RuleBasedCollator ? (RuleBasedCollator) instance : null);
        }
        return instance;
    }

    public static final Collator getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    public static final Object registerInstance(Collator collator, ULocale uLocale) {
        return getShim().registerInstance(collator, uLocale);
    }

    public static final Object registerFactory(CollatorFactory collatorFactory) {
        return getShim().registerFactory(collatorFactory);
    }

    public static final boolean unregister(Object obj) {
        ServiceShim serviceShim = shim;
        if (serviceShim == null) {
            return false;
        }
        return serviceShim.unregister(obj);
    }

    public static Locale[] getAvailableLocales() {
        ServiceShim serviceShim = shim;
        if (serviceShim == null) {
            return ICUResourceBundle.getAvailableLocales(BASE, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return serviceShim.getAvailableLocales();
    }

    public static final ULocale[] getAvailableULocales() {
        ServiceShim serviceShim = shim;
        if (serviceShim == null) {
            return ICUResourceBundle.getAvailableULocales(BASE, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return serviceShim.getAvailableULocales();
    }

    public static final String[] getKeywords() {
        return KEYWORDS;
    }

    public static final String[] getKeywordValues(String str) {
        if (str.equals(KEYWORDS[0])) {
            return ICUResourceBundle.getKeywordValues(BASE, RESOURCE);
        }
        throw new IllegalArgumentException("Invalid keyword: " + str);
    }

    public static final String[] getKeywordValuesForLocale(String str, ULocale uLocale, boolean z) {
        KeywordsSink keywordsSink = new KeywordsSink();
        UResourceBundle.getBundleInstance(BASE, uLocale).getAllItemsWithFallback(RESOURCE, keywordsSink);
        return (String[]) keywordsSink.values.toArray(new String[keywordsSink.values.size()]);
    }

    private static final class KeywordsSink extends UResource.Sink {
        boolean hasDefault;
        LinkedList<String> values;

        private KeywordsSink() {
            this.values = new LinkedList<>();
            this.hasDefault = false;
        }

        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                int type = value.getType();
                if (type == 0) {
                    if (!this.hasDefault && key.contentEquals("default")) {
                        String string = value.getString();
                        if (!string.isEmpty()) {
                            this.values.remove(string);
                            this.values.addFirst(string);
                            this.hasDefault = true;
                        }
                    }
                } else if (type == 2 && !key.startsWith("private-")) {
                    String key2 = key.toString();
                    if (!this.values.contains(key2)) {
                        this.values.add(key2);
                    }
                }
            }
        }
    }

    public static final ULocale getFunctionalEquivalent(String str, ULocale uLocale, boolean[] zArr) {
        return ICUResourceBundle.getFunctionalEquivalent(BASE, ICUResourceBundle.ICU_DATA_CLASS_LOADER, RESOURCE, str, uLocale, zArr, true);
    }

    public static final ULocale getFunctionalEquivalent(String str, ULocale uLocale) {
        return getFunctionalEquivalent(str, uLocale, null);
    }

    public static String getDisplayName(Locale locale, Locale locale2) {
        return getShim().getDisplayName(ULocale.forLocale(locale), ULocale.forLocale(locale2));
    }

    public static String getDisplayName(ULocale uLocale, ULocale uLocale2) {
        return getShim().getDisplayName(uLocale, uLocale2);
    }

    public static String getDisplayName(Locale locale) {
        return getShim().getDisplayName(ULocale.forLocale(locale), ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public static String getDisplayName(ULocale uLocale) {
        return getShim().getDisplayName(uLocale, ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public boolean equals(String str, String str2) {
        return compare(str, str2) == 0;
    }

    public UnicodeSet getTailoredSet() {
        return new UnicodeSet(0, 1114111);
    }

    @Override // java.util.Comparator
    public int compare(Object obj, Object obj2) {
        return doCompare((CharSequence) obj, (CharSequence) obj2);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int doCompare(CharSequence charSequence, CharSequence charSequence2) {
        return compare(charSequence.toString(), charSequence2.toString());
    }

    public Collator setMaxVariable(int i) {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    public int[] getReorderCodes() {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    public static int[] getEquivalentReorderCodes(int i) {
        return CollationRoot.getData().getEquivalentScripts(i);
    }

    @Override // ohos.global.icu.util.Freezable
    public Collator freeze() {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    @Override // ohos.global.icu.util.Freezable
    public Collator cloneAsThawed() {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    protected Collator() {
    }

    public ULocale getLocale(ULocale.Type type) {
        return ULocale.ROOT;
    }
}
