package ohos.global.icu.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import ohos.global.icu.impl.CacheBase;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.ICUResourceTableAccess;
import ohos.global.icu.impl.LocaleIDParser;
import ohos.global.icu.impl.LocaleIDs;
import ohos.global.icu.impl.LocaleUtility;
import ohos.global.icu.impl.SoftCache;
import ohos.global.icu.impl.locale.AsciiUtil;
import ohos.global.icu.impl.locale.BaseLocale;
import ohos.global.icu.impl.locale.InternalLocaleBuilder;
import ohos.global.icu.impl.locale.KeyTypeData;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.global.icu.impl.locale.LocaleExtensions;
import ohos.global.icu.impl.locale.LocaleSyntaxException;
import ohos.global.icu.impl.locale.ParseStatus;
import ohos.global.icu.impl.locale.UnicodeLocaleExtension;
import ohos.global.icu.lang.UScript;
import ohos.global.icu.text.LocaleDisplayNames;
import ohos.security.seckeychain.SecKeyChain;
import ohos.telephony.TelephoneNumberUtils;

public final class ULocale implements Serializable, Comparable<ULocale> {
    public static Type ACTUAL_LOCALE = new Type();
    private static final SoftCache<Locale, ULocale, Void> CACHE = new SoftCache<Locale, ULocale, Void>() {
        /* class ohos.global.icu.util.ULocale.AnonymousClass2 */

        /* access modifiers changed from: protected */
        public ULocale createInstance(Locale locale, Void r2) {
            return JDKLocaleHelper.toULocale(locale);
        }
    };
    public static final ULocale CANADA = new ULocale("en_CA", Locale.CANADA);
    public static final ULocale CANADA_FRENCH = new ULocale("fr_CA", Locale.CANADA_FRENCH);
    private static String[][] CANONICALIZE_MAP = {new String[]{"art_LOJBAN", "jbo"}, new String[]{"cel_GAULISH", "cel__GAULISH"}, new String[]{"de_1901", "de__1901"}, new String[]{"de_1906", "de__1906"}, new String[]{"en_BOONT", "en__BOONT"}, new String[]{"en_SCOUSE", "en__SCOUSE"}, new String[]{"hy__AREVELA", "hy", null, null}, new String[]{"hy__AREVMDA", "hyw", null, null}, new String[]{"sl_ROZAJ", "sl__ROZAJ"}, new String[]{"zh_GAN", "zh__GAN"}, new String[]{"zh_GUOYU", "zh"}, new String[]{"zh_HAKKA", "zh__HAKKA"}, new String[]{"zh_MIN", "zh__MIN"}, new String[]{"zh_MIN_NAN", "zh__MINNAN"}, new String[]{"zh_WUU", "zh__WUU"}, new String[]{"zh_XIANG", "zh__XIANG"}, new String[]{"zh_YUE", "zh__YUE"}};
    public static final ULocale CHINA = new ULocale("zh_Hans_CN");
    public static final ULocale CHINESE = new ULocale("zh", Locale.CHINESE);
    private static final Locale EMPTY_LOCALE = new Locale("", "");
    private static final String EMPTY_STRING = "";
    public static final ULocale ENGLISH = new ULocale("en", Locale.ENGLISH);
    public static final ULocale FRANCE = new ULocale("fr_FR", Locale.FRANCE);
    public static final ULocale FRENCH = new ULocale("fr", Locale.FRENCH);
    public static final ULocale GERMAN = new ULocale("de", Locale.GERMAN);
    public static final ULocale GERMANY = new ULocale("de_DE", Locale.GERMANY);
    public static final ULocale ITALIAN = new ULocale("it", Locale.ITALIAN);
    public static final ULocale ITALY = new ULocale("it_IT", Locale.ITALY);
    public static final ULocale JAPAN = new ULocale("ja_JP", Locale.JAPAN);
    public static final ULocale JAPANESE = new ULocale("ja", Locale.JAPANESE);
    public static final ULocale KOREA = new ULocale("ko_KR", Locale.KOREA);
    public static final ULocale KOREAN = new ULocale("ko", Locale.KOREAN);
    private static final String LANG_DIR_STRING = "root-en-es-pt-zh-ja-ko-de-fr-it-ar+he+fa+ru-nl-pl-th-tr-";
    private static final String LOCALE_ATTRIBUTE_KEY = "attribute";
    public static final ULocale PRC = CHINA;
    public static final char PRIVATE_USE_EXTENSION = 'x';
    public static final ULocale ROOT = new ULocale("", EMPTY_LOCALE);
    public static final ULocale SIMPLIFIED_CHINESE = new ULocale("zh_Hans");
    public static final ULocale TAIWAN = new ULocale("zh_Hant_TW");
    public static final ULocale TRADITIONAL_CHINESE = new ULocale("zh_Hant");
    public static final ULocale UK = new ULocale("en_GB", Locale.UK);
    private static final String UNDEFINED_LANGUAGE = "und";
    private static final String UNDEFINED_REGION = "ZZ";
    private static final String UNDEFINED_SCRIPT = "Zzzz";
    private static final char UNDERSCORE = '_';
    private static final Pattern UND_PATTERN = Pattern.compile("^und(?=$|[_-])", 2);
    public static final char UNICODE_LOCALE_EXTENSION = 'u';
    public static final ULocale US = new ULocale("en_US", Locale.US);
    public static Type VALID_LOCALE = new Type();
    private static Locale[] defaultCategoryLocales = new Locale[Category.values().length];
    private static ULocale[] defaultCategoryULocales = new ULocale[Category.values().length];
    private static Locale defaultLocale = Locale.getDefault();
    private static ULocale defaultULocale = forLocale(defaultLocale);
    private static CacheBase<String, String, Void> nameCache = new SoftCache<String, String, Void>() {
        /* class ohos.global.icu.util.ULocale.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public String createInstance(String str, Void r2) {
            return new LocaleIDParser(str).getName();
        }
    };
    private static final long serialVersionUID = 3715177670352309217L;
    private volatile transient BaseLocale baseLocale;
    private volatile transient LocaleExtensions extensions;
    private volatile transient Locale locale;
    private String localeID;

    public enum AvailableType {
        DEFAULT,
        ONLY_LEGACY_ALIASES,
        WITH_LEGACY_ALIASES
    }

    public enum Category {
        DISPLAY,
        FORMAT
    }

    @Deprecated
    public enum Minimize {
        FAVOR_SCRIPT,
        FAVOR_REGION
    }

    @Override // java.lang.Object
    public Object clone() {
        return this;
    }

    static {
        int i = 0;
        if (JDKLocaleHelper.hasLocaleCategories()) {
            Category[] values = Category.values();
            int length = values.length;
            while (i < length) {
                Category category = values[i];
                int ordinal = category.ordinal();
                defaultCategoryLocales[ordinal] = JDKLocaleHelper.getDefault(category);
                defaultCategoryULocales[ordinal] = forLocale(defaultCategoryLocales[ordinal]);
                i++;
            }
        } else {
            Category[] values2 = Category.values();
            int length2 = values2.length;
            while (i < length2) {
                int ordinal2 = values2[i].ordinal();
                defaultCategoryLocales[ordinal2] = defaultLocale;
                defaultCategoryULocales[ordinal2] = defaultULocale;
                i++;
            }
        }
    }

    private ULocale(String str, Locale locale2) {
        this.localeID = str;
        this.locale = locale2;
    }

    private ULocale(Locale locale2) {
        this.localeID = getName(forLocale(locale2).toString());
        this.locale = locale2;
    }

    public static ULocale forLocale(Locale locale2) {
        if (locale2 == null) {
            return null;
        }
        return (ULocale) CACHE.getInstance(locale2, (Object) null);
    }

    public ULocale(String str) {
        this.localeID = getName(str);
    }

    public ULocale(String str, String str2) {
        this(str, str2, (String) null);
    }

    public ULocale(String str, String str2, String str3) {
        this.localeID = getName(lscvToID(str, str2, str3, ""));
    }

    public static ULocale createCanonical(String str) {
        return new ULocale(canonicalize(str), (Locale) null);
    }

    private static String lscvToID(String str, String str2, String str3, String str4) {
        StringBuilder sb = new StringBuilder();
        if (str != null && str.length() > 0) {
            sb.append(str);
        }
        if (str2 != null && str2.length() > 0) {
            sb.append(UNDERSCORE);
            sb.append(str2);
        }
        if (str3 != null && str3.length() > 0) {
            sb.append(UNDERSCORE);
            sb.append(str3);
        }
        if (str4 != null && str4.length() > 0) {
            if (str3 == null || str3.length() == 0) {
                sb.append(UNDERSCORE);
            }
            sb.append(UNDERSCORE);
            sb.append(str4);
        }
        return sb.toString();
    }

    public Locale toLocale() {
        if (this.locale == null) {
            this.locale = JDKLocaleHelper.toLocale(this);
        }
        return this.locale;
    }

    public static ULocale getDefault() {
        synchronized (ULocale.class) {
            if (defaultULocale == null) {
                return ROOT;
            }
            Locale locale2 = Locale.getDefault();
            if (!defaultLocale.equals(locale2)) {
                defaultLocale = locale2;
                defaultULocale = forLocale(locale2);
                if (!JDKLocaleHelper.hasLocaleCategories()) {
                    for (Category category : Category.values()) {
                        int ordinal = category.ordinal();
                        defaultCategoryLocales[ordinal] = locale2;
                        defaultCategoryULocales[ordinal] = forLocale(locale2);
                    }
                }
            }
            return defaultULocale;
        }
    }

    public static synchronized void setDefault(ULocale uLocale) {
        synchronized (ULocale.class) {
            defaultLocale = uLocale.toLocale();
            Locale.setDefault(defaultLocale);
            defaultULocale = uLocale;
            for (Category category : Category.values()) {
                setDefault(category, uLocale);
            }
        }
    }

    public static ULocale getDefault(Category category) {
        synchronized (ULocale.class) {
            int ordinal = category.ordinal();
            if (defaultCategoryULocales[ordinal] == null) {
                return ROOT;
            }
            if (JDKLocaleHelper.hasLocaleCategories()) {
                Locale locale2 = JDKLocaleHelper.getDefault(category);
                if (!defaultCategoryLocales[ordinal].equals(locale2)) {
                    defaultCategoryLocales[ordinal] = locale2;
                    defaultCategoryULocales[ordinal] = forLocale(locale2);
                }
            } else {
                Locale locale3 = Locale.getDefault();
                if (!defaultLocale.equals(locale3)) {
                    defaultLocale = locale3;
                    defaultULocale = forLocale(locale3);
                    for (Category category2 : Category.values()) {
                        int ordinal2 = category2.ordinal();
                        defaultCategoryLocales[ordinal2] = locale3;
                        defaultCategoryULocales[ordinal2] = forLocale(locale3);
                    }
                }
            }
            return defaultCategoryULocales[ordinal];
        }
    }

    public static synchronized void setDefault(Category category, ULocale uLocale) {
        synchronized (ULocale.class) {
            Locale locale2 = uLocale.toLocale();
            int ordinal = category.ordinal();
            defaultCategoryULocales[ordinal] = uLocale;
            defaultCategoryLocales[ordinal] = locale2;
            JDKLocaleHelper.setDefault(category, locale2);
        }
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.localeID.hashCode();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ULocale) {
            return this.localeID.equals(((ULocale) obj).localeID);
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0093, code lost:
        if (r5.hasNext() != false) goto L_0x0095;
     */
    public int compareTo(ULocale uLocale) {
        if (this == uLocale) {
            return 0;
        }
        int compareTo = getLanguage().compareTo(uLocale.getLanguage());
        if (compareTo == 0 && (compareTo = getScript().compareTo(uLocale.getScript())) == 0 && (compareTo = getCountry().compareTo(uLocale.getCountry())) == 0 && (compareTo = getVariant().compareTo(uLocale.getVariant())) == 0) {
            Iterator<String> keywords = getKeywords();
            Iterator<String> keywords2 = uLocale.getKeywords();
            if (keywords == null) {
                if (keywords2 == null) {
                    compareTo = 0;
                }
            } else if (keywords2 == null) {
                compareTo = 1;
            } else {
                while (true) {
                    if (compareTo != 0 || !keywords.hasNext()) {
                        break;
                    } else if (!keywords2.hasNext()) {
                        compareTo = 1;
                        break;
                    } else {
                        String next = keywords.next();
                        String next2 = keywords2.next();
                        int compareTo2 = next.compareTo(next2);
                        if (compareTo2 == 0) {
                            String keywordValue = getKeywordValue(next);
                            String keywordValue2 = uLocale.getKeywordValue(next2);
                            compareTo = keywordValue == null ? keywordValue2 == null ? 0 : -1 : keywordValue2 == null ? 1 : keywordValue.compareTo(keywordValue2);
                        } else {
                            compareTo = compareTo2;
                        }
                    }
                }
                if (compareTo == 0) {
                }
            }
            compareTo = -1;
        }
        if (compareTo < 0) {
            return -1;
        }
        return compareTo > 0 ? 1 : 0;
    }

    public static ULocale[] getAvailableLocales() {
        return (ULocale[]) ICUResourceBundle.getAvailableULocales().clone();
    }

    public static Collection<ULocale> getAvailableLocalesByType(AvailableType availableType) {
        List list;
        if (availableType != null) {
            if (availableType == AvailableType.WITH_LEGACY_ALIASES) {
                list = new ArrayList();
                Collections.addAll(list, ICUResourceBundle.getAvailableULocales(AvailableType.DEFAULT));
                Collections.addAll(list, ICUResourceBundle.getAvailableULocales(AvailableType.ONLY_LEGACY_ALIASES));
            } else {
                list = Arrays.asList(ICUResourceBundle.getAvailableULocales(availableType));
            }
            return Collections.unmodifiableList(list);
        }
        throw new IllegalArgumentException();
    }

    public static String[] getISOCountries() {
        return LocaleIDs.getISOCountries();
    }

    public static String[] getISOLanguages() {
        return LocaleIDs.getISOLanguages();
    }

    public String getLanguage() {
        return base().getLanguage();
    }

    public static String getLanguage(String str) {
        return new LocaleIDParser(str).getLanguage();
    }

    public String getScript() {
        return base().getScript();
    }

    public static String getScript(String str) {
        return new LocaleIDParser(str).getScript();
    }

    public String getCountry() {
        return base().getRegion();
    }

    public static String getCountry(String str) {
        return new LocaleIDParser(str).getCountry();
    }

    @Deprecated
    public static String getRegionForSupplementalData(ULocale uLocale, boolean z) {
        String keywordValue = uLocale.getKeywordValue("rg");
        if (keywordValue != null && keywordValue.length() == 6) {
            String upperString = AsciiUtil.toUpperString(keywordValue);
            if (upperString.endsWith("ZZZZ")) {
                return upperString.substring(0, 2);
            }
        }
        String country = uLocale.getCountry();
        return (country.length() != 0 || !z) ? country : addLikelySubtags(uLocale).getCountry();
    }

    public String getVariant() {
        return base().getVariant();
    }

    public static String getVariant(String str) {
        return new LocaleIDParser(str).getVariant();
    }

    public static String getFallback(String str) {
        return getFallbackString(getName(str));
    }

    public ULocale getFallback() {
        if (this.localeID.length() == 0 || this.localeID.charAt(0) == '@') {
            return null;
        }
        return new ULocale(getFallbackString(this.localeID), (Locale) null);
    }

    private static String getFallbackString(String str) {
        int indexOf = str.indexOf(64);
        if (indexOf == -1) {
            indexOf = str.length();
        }
        int lastIndexOf = str.lastIndexOf(95, indexOf);
        if (lastIndexOf == -1) {
            lastIndexOf = 0;
        } else {
            while (lastIndexOf > 0 && str.charAt(lastIndexOf - 1) == '_') {
                lastIndexOf--;
            }
        }
        return str.substring(0, lastIndexOf) + str.substring(indexOf);
    }

    public String getBaseName() {
        return getBaseName(this.localeID);
    }

    public static String getBaseName(String str) {
        if (str.indexOf(64) == -1) {
            return str;
        }
        return new LocaleIDParser(str).getBaseName();
    }

    public String getName() {
        return this.localeID;
    }

    private static int getShortestSubtagLength(String str) {
        int length = str.length();
        int i = length;
        int i2 = 0;
        boolean z = true;
        for (int i3 = 0; i3 < length; i3++) {
            if (str.charAt(i3) == '_' || str.charAt(i3) == '-') {
                if (i2 != 0 && i2 < i) {
                    i = i2;
                }
                z = true;
            } else {
                if (z) {
                    i2 = 0;
                    z = false;
                }
                i2++;
            }
        }
        return i;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001f, code lost:
        if (r0.length() == 0) goto L_0x0036;
     */
    public static String getName(String str) {
        String str2 = "";
        if (str != null && !str.contains("@") && getShortestSubtagLength(str) == 1) {
            str2 = forLanguageTag(str).getName();
        } else if (!"root".equalsIgnoreCase(str)) {
            str = UND_PATTERN.matcher(str).replaceFirst(str2);
            return (String) nameCache.getInstance(str, (Object) null);
        }
        str = str2;
        return (String) nameCache.getInstance(str, (Object) null);
    }

    @Override // java.lang.Object
    public String toString() {
        return this.localeID;
    }

    public Iterator<String> getKeywords() {
        return getKeywords(this.localeID);
    }

    public static Iterator<String> getKeywords(String str) {
        return new LocaleIDParser(str).getKeywords();
    }

    public String getKeywordValue(String str) {
        return getKeywordValue(this.localeID, str);
    }

    public static String getKeywordValue(String str, String str2) {
        return new LocaleIDParser(str).getKeywordValue(str2);
    }

    public static String canonicalize(String str) {
        LocaleIDParser localeIDParser = new LocaleIDParser(str, true);
        String baseName = localeIDParser.getBaseName();
        if (str.equals("")) {
            return "";
        }
        boolean z = false;
        int i = 0;
        while (true) {
            String[][] strArr = CANONICALIZE_MAP;
            if (i >= strArr.length) {
                break;
            }
            String[] strArr2 = strArr[i];
            if (strArr2[0].equals(baseName)) {
                localeIDParser.setBaseName(strArr2[1]);
                z = true;
                break;
            }
            i++;
        }
        if (!z && localeIDParser.getLanguage().equals("nb") && localeIDParser.getVariant().equals("NY")) {
            localeIDParser.setBaseName(lscvToID("nn", localeIDParser.getScript(), localeIDParser.getCountry(), null));
        }
        return localeIDParser.getName();
    }

    public ULocale setKeywordValue(String str, String str2) {
        return new ULocale(setKeywordValue(this.localeID, str, str2), (Locale) null);
    }

    public static String setKeywordValue(String str, String str2, String str3) {
        LocaleIDParser localeIDParser = new LocaleIDParser(str);
        localeIDParser.setKeywordValue(str2, str3);
        return localeIDParser.getName();
    }

    public String getISO3Language() {
        return getISO3Language(this.localeID);
    }

    public static String getISO3Language(String str) {
        return LocaleIDs.getISO3Language(getLanguage(str));
    }

    public String getISO3Country() {
        return getISO3Country(this.localeID);
    }

    public static String getISO3Country(String str) {
        return LocaleIDs.getISO3Country(getCountry(str));
    }

    public boolean isRightToLeft() {
        int indexOf;
        String script = getScript();
        if (script.length() == 0) {
            String language = getLanguage();
            if (!language.isEmpty() && (indexOf = LANG_DIR_STRING.indexOf(language)) >= 0) {
                char charAt = LANG_DIR_STRING.charAt(indexOf + language.length());
                if (charAt == '+') {
                    return true;
                }
                if (charAt == '-') {
                    return false;
                }
            }
            script = addLikelySubtags(this).getScript();
            if (script.length() == 0) {
                return false;
            }
        }
        return UScript.isRightToLeft(UScript.getCodeFromName(script));
    }

    public String getDisplayLanguage() {
        return getDisplayLanguageInternal(this, getDefault(Category.DISPLAY), false);
    }

    public String getDisplayLanguage(ULocale uLocale) {
        return getDisplayLanguageInternal(this, uLocale, false);
    }

    public static String getDisplayLanguage(String str, String str2) {
        return getDisplayLanguageInternal(new ULocale(str), new ULocale(str2), false);
    }

    public static String getDisplayLanguage(String str, ULocale uLocale) {
        return getDisplayLanguageInternal(new ULocale(str), uLocale, false);
    }

    public String getDisplayLanguageWithDialect() {
        return getDisplayLanguageInternal(this, getDefault(Category.DISPLAY), true);
    }

    public String getDisplayLanguageWithDialect(ULocale uLocale) {
        return getDisplayLanguageInternal(this, uLocale, true);
    }

    public static String getDisplayLanguageWithDialect(String str, String str2) {
        return getDisplayLanguageInternal(new ULocale(str), new ULocale(str2), true);
    }

    public static String getDisplayLanguageWithDialect(String str, ULocale uLocale) {
        return getDisplayLanguageInternal(new ULocale(str), uLocale, true);
    }

    private static String getDisplayLanguageInternal(ULocale uLocale, ULocale uLocale2, boolean z) {
        return LocaleDisplayNames.getInstance(uLocale2).languageDisplayName(z ? uLocale.getBaseName() : uLocale.getLanguage());
    }

    public String getDisplayScript() {
        return getDisplayScriptInternal(this, getDefault(Category.DISPLAY));
    }

    @Deprecated
    public String getDisplayScriptInContext() {
        return getDisplayScriptInContextInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayScript(ULocale uLocale) {
        return getDisplayScriptInternal(this, uLocale);
    }

    @Deprecated
    public String getDisplayScriptInContext(ULocale uLocale) {
        return getDisplayScriptInContextInternal(this, uLocale);
    }

    public static String getDisplayScript(String str, String str2) {
        return getDisplayScriptInternal(new ULocale(str), new ULocale(str2));
    }

    @Deprecated
    public static String getDisplayScriptInContext(String str, String str2) {
        return getDisplayScriptInContextInternal(new ULocale(str), new ULocale(str2));
    }

    public static String getDisplayScript(String str, ULocale uLocale) {
        return getDisplayScriptInternal(new ULocale(str), uLocale);
    }

    @Deprecated
    public static String getDisplayScriptInContext(String str, ULocale uLocale) {
        return getDisplayScriptInContextInternal(new ULocale(str), uLocale);
    }

    private static String getDisplayScriptInternal(ULocale uLocale, ULocale uLocale2) {
        return LocaleDisplayNames.getInstance(uLocale2).scriptDisplayName(uLocale.getScript());
    }

    private static String getDisplayScriptInContextInternal(ULocale uLocale, ULocale uLocale2) {
        return LocaleDisplayNames.getInstance(uLocale2).scriptDisplayNameInContext(uLocale.getScript());
    }

    public String getDisplayCountry() {
        return getDisplayCountryInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayCountry(ULocale uLocale) {
        return getDisplayCountryInternal(this, uLocale);
    }

    public static String getDisplayCountry(String str, String str2) {
        return getDisplayCountryInternal(new ULocale(str), new ULocale(str2));
    }

    public static String getDisplayCountry(String str, ULocale uLocale) {
        return getDisplayCountryInternal(new ULocale(str), uLocale);
    }

    private static String getDisplayCountryInternal(ULocale uLocale, ULocale uLocale2) {
        return LocaleDisplayNames.getInstance(uLocale2).regionDisplayName(uLocale.getCountry());
    }

    public String getDisplayVariant() {
        return getDisplayVariantInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayVariant(ULocale uLocale) {
        return getDisplayVariantInternal(this, uLocale);
    }

    public static String getDisplayVariant(String str, String str2) {
        return getDisplayVariantInternal(new ULocale(str), new ULocale(str2));
    }

    public static String getDisplayVariant(String str, ULocale uLocale) {
        return getDisplayVariantInternal(new ULocale(str), uLocale);
    }

    private static String getDisplayVariantInternal(ULocale uLocale, ULocale uLocale2) {
        return LocaleDisplayNames.getInstance(uLocale2).variantDisplayName(uLocale.getVariant());
    }

    public static String getDisplayKeyword(String str) {
        return getDisplayKeywordInternal(str, getDefault(Category.DISPLAY));
    }

    public static String getDisplayKeyword(String str, String str2) {
        return getDisplayKeywordInternal(str, new ULocale(str2));
    }

    public static String getDisplayKeyword(String str, ULocale uLocale) {
        return getDisplayKeywordInternal(str, uLocale);
    }

    private static String getDisplayKeywordInternal(String str, ULocale uLocale) {
        return LocaleDisplayNames.getInstance(uLocale).keyDisplayName(str);
    }

    public String getDisplayKeywordValue(String str) {
        return getDisplayKeywordValueInternal(this, str, getDefault(Category.DISPLAY));
    }

    public String getDisplayKeywordValue(String str, ULocale uLocale) {
        return getDisplayKeywordValueInternal(this, str, uLocale);
    }

    public static String getDisplayKeywordValue(String str, String str2, String str3) {
        return getDisplayKeywordValueInternal(new ULocale(str), str2, new ULocale(str3));
    }

    public static String getDisplayKeywordValue(String str, String str2, ULocale uLocale) {
        return getDisplayKeywordValueInternal(new ULocale(str), str2, uLocale);
    }

    private static String getDisplayKeywordValueInternal(ULocale uLocale, String str, ULocale uLocale2) {
        String lowerString = AsciiUtil.toLowerString(str.trim());
        return LocaleDisplayNames.getInstance(uLocale2).keyValueDisplayName(lowerString, uLocale.getKeywordValue(lowerString));
    }

    public String getDisplayName() {
        return getDisplayNameInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayName(ULocale uLocale) {
        return getDisplayNameInternal(this, uLocale);
    }

    public static String getDisplayName(String str, String str2) {
        return getDisplayNameInternal(new ULocale(str), new ULocale(str2));
    }

    public static String getDisplayName(String str, ULocale uLocale) {
        return getDisplayNameInternal(new ULocale(str), uLocale);
    }

    private static String getDisplayNameInternal(ULocale uLocale, ULocale uLocale2) {
        return LocaleDisplayNames.getInstance(uLocale2).localeDisplayName(uLocale);
    }

    public String getDisplayNameWithDialect() {
        return getDisplayNameWithDialectInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayNameWithDialect(ULocale uLocale) {
        return getDisplayNameWithDialectInternal(this, uLocale);
    }

    public static String getDisplayNameWithDialect(String str, String str2) {
        return getDisplayNameWithDialectInternal(new ULocale(str), new ULocale(str2));
    }

    public static String getDisplayNameWithDialect(String str, ULocale uLocale) {
        return getDisplayNameWithDialectInternal(new ULocale(str), uLocale);
    }

    private static String getDisplayNameWithDialectInternal(ULocale uLocale, ULocale uLocale2) {
        return LocaleDisplayNames.getInstance(uLocale2, LocaleDisplayNames.DialectHandling.DIALECT_NAMES).localeDisplayName(uLocale);
    }

    public String getCharacterOrientation() {
        return ICUResourceTableAccess.getTableString("ohos/global/icu/impl/data/icudt66b", this, "layout", "characters", "characters");
    }

    public String getLineOrientation() {
        return ICUResourceTableAccess.getTableString("ohos/global/icu/impl/data/icudt66b", this, "layout", "lines", "lines");
    }

    public static final class Type {
        private Type() {
        }
    }

    public static ULocale acceptLanguage(String str, ULocale[] uLocaleArr, boolean[] zArr) {
        ULocale[] uLocaleArr2;
        if (str != null) {
            try {
                uLocaleArr2 = parseAcceptLanguage(str, true);
            } catch (ParseException unused) {
                uLocaleArr2 = null;
            }
            if (uLocaleArr2 == null) {
                return null;
            }
            return acceptLanguage(uLocaleArr2, uLocaleArr, zArr);
        }
        throw new NullPointerException();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0093, code lost:
        r1 = r1 + 1;
     */
    public static ULocale acceptLanguage(ULocale[] uLocaleArr, ULocale[] uLocaleArr2, boolean[] zArr) {
        if (zArr != null) {
            zArr[0] = true;
        }
        int i = 0;
        while (i < uLocaleArr.length) {
            ULocale uLocale = uLocaleArr[i];
            boolean[] zArr2 = zArr;
            while (true) {
                for (int i2 = 0; i2 < uLocaleArr2.length; i2++) {
                    if (uLocaleArr2[i2].equals(uLocale)) {
                        if (zArr2 != null) {
                            zArr2[0] = false;
                        }
                        return uLocaleArr2[i2];
                    } else if (uLocale.getScript().length() == 0 && uLocaleArr2[i2].getScript().length() > 0 && uLocaleArr2[i2].getLanguage().equals(uLocale.getLanguage()) && uLocaleArr2[i2].getCountry().equals(uLocale.getCountry()) && uLocaleArr2[i2].getVariant().equals(uLocale.getVariant()) && minimizeSubtags(uLocaleArr2[i2]).getScript().length() == 0) {
                        if (zArr2 != null) {
                            zArr2[0] = false;
                        }
                        return uLocale;
                    }
                }
                Locale fallback = LocaleUtility.fallback(uLocale.toLocale());
                uLocale = fallback != null ? new ULocale(fallback) : null;
                if (uLocale == null) {
                    break;
                }
                zArr2 = null;
            }
        }
        return null;
    }

    public static ULocale acceptLanguage(String str, boolean[] zArr) {
        return acceptLanguage(str, getAvailableLocales(), zArr);
    }

    public static ULocale acceptLanguage(ULocale[] uLocaleArr, boolean[] zArr) {
        return acceptLanguage(uLocaleArr, getAvailableLocales(), zArr);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005d, code lost:
        if (r8 != '\t') goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x007c, code lost:
        if (r8 != '\t') goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x009c, code lost:
        if (r8 != '\t') goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a8, code lost:
        if (r8 != '\t') goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b4, code lost:
        if (r8 != '\t') goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00c2, code lost:
        if (r8 != '\t') goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ce, code lost:
        if (r8 == ';') goto L_0x00bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x004b, code lost:
        if (r8 != '\t') goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x010e, code lost:
        if (r8 == ';') goto L_0x00bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0133, code lost:
        if (r8 != '\t') goto L_0x006a;
     */
    static ULocale[] parseAcceptLanguage(String str, boolean z) throws ParseException {
        double d;
        TreeMap treeMap = new TreeMap();
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        String str2 = str + ",";
        int i = 0;
        char c = 0;
        boolean z2 = false;
        while (i < str2.length()) {
            char charAt = str2.charAt(i);
            boolean z3 = true;
            switch (c) {
                case 0:
                    if (('A' > charAt || charAt > 'Z') && ('a' > charAt || charAt > 'z')) {
                        if (charAt == '*') {
                            sb.append(charAt);
                            c = 2;
                        } else if (charAt != ' ') {
                        }
                        z3 = false;
                        break;
                    } else {
                        sb.append(charAt);
                        c = 1;
                        z2 = false;
                        z3 = false;
                    }
                    break;
                case 1:
                    if (('A' > charAt || charAt > 'Z') && ('a' > charAt || charAt > 'z')) {
                        if (charAt == '-') {
                            sb.append(charAt);
                        } else {
                            if (charAt == '_') {
                                if (z) {
                                    sb.append(charAt);
                                }
                            } else if ('0' > charAt || charAt > '9') {
                                if (charAt != ',') {
                                    if (!(charAt == ' ' || charAt == '\t')) {
                                    }
                                    c = 3;
                                    z3 = false;
                                    break;
                                }
                            } else if (z2) {
                                sb.append(charAt);
                                z3 = false;
                            }
                            c = 65535;
                            z3 = false;
                        }
                        z2 = true;
                        z3 = false;
                    } else {
                        sb.append(charAt);
                        z3 = false;
                    }
                    break;
                case 2:
                    if (charAt != ',') {
                        if (!(charAt == ' ' || charAt == '\t')) {
                        }
                        c = 3;
                        z3 = false;
                        break;
                    }
                    break;
                case 3:
                    if (charAt != ',') {
                        if (charAt != ';') {
                            if (charAt != ' ') {
                            }
                            z3 = false;
                            break;
                        }
                        c = 4;
                        z3 = false;
                    }
                    break;
                case 4:
                    if (charAt == 'q') {
                        c = 5;
                    } else if (charAt != ' ') {
                    }
                    z3 = false;
                    break;
                case 5:
                    if (charAt == '=') {
                        c = 6;
                    } else if (charAt != ' ') {
                    }
                    z3 = false;
                    break;
                case 6:
                    if (charAt == '0') {
                        sb2.append(charAt);
                    } else if (charAt == '1') {
                        sb2.append(charAt);
                    } else {
                        if (charAt == '.') {
                            if (z) {
                                sb2.append(charAt);
                                c = '\b';
                                z3 = false;
                                break;
                            }
                        } else {
                            if (charAt != ' ') {
                            }
                            z3 = false;
                        }
                        c = 65535;
                        z3 = false;
                    }
                    c = 7;
                    z3 = false;
                    break;
                case 7:
                    if (charAt == '.') {
                        sb2.append(charAt);
                        c = '\b';
                        z3 = false;
                        break;
                    } else if (charAt != ',') {
                        if (charAt != ' ') {
                        }
                        c = '\n';
                        z3 = false;
                    }
                    break;
                case '\b':
                    if ('0' <= charAt && charAt <= '9') {
                        sb2.append(charAt);
                        c = '\t';
                        z3 = false;
                        break;
                    }
                    c = 65535;
                    z3 = false;
                case '\t':
                    if ('0' > charAt || charAt > '9') {
                        if (charAt != ',') {
                            if (charAt != ' ') {
                            }
                            c = '\n';
                            z3 = false;
                            break;
                        }
                    } else {
                        sb2.append(charAt);
                        z3 = false;
                    }
                    break;
                case '\n':
                    if (charAt != ',') {
                        if (charAt != ' ') {
                        }
                        z3 = false;
                        break;
                    }
                    break;
                default:
                    z3 = false;
                    break;
            }
            if (c != 65535) {
                if (z3) {
                    double d2 = 1.0d;
                    if (sb2.length() != 0) {
                        try {
                            d = Double.parseDouble(sb2.toString());
                        } catch (NumberFormatException unused) {
                            d = 1.0d;
                        }
                        if (d <= 1.0d) {
                            d2 = d;
                        }
                    }
                    if (sb.charAt(0) != '*') {
                        treeMap.put(new Comparable<AnonymousClass1ULocaleAcceptLanguageQ>(d2, treeMap.size()) {
                            /* class ohos.global.icu.util.ULocale.AnonymousClass1ULocaleAcceptLanguageQ */
                            private double q;
                            private double serial;

                            {
                                this.q = r1;
                                this.serial = (double) r3;
                            }

                            public int compareTo(AnonymousClass1ULocaleAcceptLanguageQ r7) {
                                double d = this.q;
                                double d2 = r7.q;
                                if (d > d2) {
                                    return -1;
                                }
                                if (d < d2) {
                                    return 1;
                                }
                                double d3 = this.serial;
                                double d4 = r7.serial;
                                if (d3 < d4) {
                                    return -1;
                                }
                                if (d3 > d4) {
                                    return 1;
                                }
                                return 0;
                            }
                        }, new ULocale(canonicalize(sb.toString())));
                    }
                    sb.setLength(0);
                    sb2.setLength(0);
                    c = 0;
                }
                i++;
            } else {
                throw new ParseException("Invalid Accept-Language", i);
            }
        }
        if (c == 0) {
            return (ULocale[]) treeMap.values().toArray(new ULocale[treeMap.size()]);
        }
        throw new ParseException("Invalid AcceptlLanguage", i);
    }

    public static ULocale addLikelySubtags(ULocale uLocale) {
        String[] strArr = new String[3];
        int parseTagString = parseTagString(uLocale.localeID, strArr);
        String createLikelySubtagsString = createLikelySubtagsString(strArr[0], strArr[1], strArr[2], parseTagString < uLocale.localeID.length() ? uLocale.localeID.substring(parseTagString) : null);
        return createLikelySubtagsString == null ? uLocale : new ULocale(createLikelySubtagsString);
    }

    public static ULocale minimizeSubtags(ULocale uLocale) {
        return minimizeSubtags(uLocale, Minimize.FAVOR_REGION);
    }

    @Deprecated
    public static ULocale minimizeSubtags(ULocale uLocale, Minimize minimize) {
        String[] strArr = new String[3];
        int parseTagString = parseTagString(uLocale.localeID, strArr);
        String str = strArr[0];
        String str2 = strArr[1];
        String str3 = strArr[2];
        String substring = parseTagString < uLocale.localeID.length() ? uLocale.localeID.substring(parseTagString) : null;
        String createLikelySubtagsString = createLikelySubtagsString(str, str2, str3, null);
        if (isEmptyString(createLikelySubtagsString)) {
            return uLocale;
        }
        if (createLikelySubtagsString(str, null, null, null).equals(createLikelySubtagsString)) {
            return new ULocale(createTagString(str, null, null, substring));
        }
        if (minimize == Minimize.FAVOR_REGION) {
            if (str3.length() != 0 && createLikelySubtagsString(str, null, str3, null).equals(createLikelySubtagsString)) {
                return new ULocale(createTagString(str, null, str3, substring));
            }
            if (str2.length() != 0 && createLikelySubtagsString(str, str2, null, null).equals(createLikelySubtagsString)) {
                return new ULocale(createTagString(str, str2, null, substring));
            }
        } else if (str2.length() != 0 && createLikelySubtagsString(str, str2, null, null).equals(createLikelySubtagsString)) {
            return new ULocale(createTagString(str, str2, null, substring));
        } else {
            if (str3.length() != 0 && createLikelySubtagsString(str, null, str3, null).equals(createLikelySubtagsString)) {
                return new ULocale(createTagString(str, null, str3, substring));
            }
        }
        return uLocale;
    }

    private static boolean isEmptyString(String str) {
        return str == null || str.length() == 0;
    }

    private static void appendTag(String str, StringBuilder sb) {
        if (sb.length() != 0) {
            sb.append(UNDERSCORE);
        }
        sb.append(str);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0035  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0060  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0095  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00a6  */
    private static String createTagString(String str, String str2, String str3, String str4, String str5) {
        LocaleIDParser localeIDParser;
        boolean z;
        StringBuilder sb = new StringBuilder();
        if (!isEmptyString(str)) {
            appendTag(str, sb);
        } else {
            boolean isEmptyString = isEmptyString(str5);
            String str6 = UNDEFINED_LANGUAGE;
            if (isEmptyString) {
                appendTag(str6, sb);
            } else {
                localeIDParser = new LocaleIDParser(str5);
                String language = localeIDParser.getLanguage();
                if (!isEmptyString(language)) {
                    str6 = language;
                }
                appendTag(str6, sb);
                if (isEmptyString(str2)) {
                    appendTag(str2, sb);
                } else if (!isEmptyString(str5)) {
                    if (localeIDParser == null) {
                        localeIDParser = new LocaleIDParser(str5);
                    }
                    String script = localeIDParser.getScript();
                    if (!isEmptyString(script)) {
                        appendTag(script, sb);
                    }
                }
                boolean z2 = false;
                if (isEmptyString(str3)) {
                    appendTag(str3, sb);
                } else {
                    if (!isEmptyString(str5)) {
                        if (localeIDParser == null) {
                            localeIDParser = new LocaleIDParser(str5);
                        }
                        String country = localeIDParser.getCountry();
                        if (!isEmptyString(country)) {
                            appendTag(country, sb);
                        }
                    }
                    z = false;
                    if (str4 != null && str4.length() > 1) {
                        if (str4.charAt(0) != '_') {
                            z2 = true;
                        } else if (str4.charAt(1) == '_') {
                            z2 = true;
                        }
                        if (!z) {
                            if (z2) {
                                sb.append(UNDERSCORE);
                            }
                            sb.append(str4);
                        } else if (z2) {
                            sb.append(str4.substring(1));
                        } else {
                            sb.append(str4);
                        }
                    }
                    return sb.toString();
                }
                z = true;
                if (str4.charAt(0) != '_') {
                }
                if (!z) {
                }
                return sb.toString();
            }
        }
        localeIDParser = null;
        if (isEmptyString(str2)) {
        }
        boolean z22 = false;
        if (isEmptyString(str3)) {
        }
        z = true;
        if (str4.charAt(0) != '_') {
        }
        if (!z) {
        }
        return sb.toString();
    }

    static String createTagString(String str, String str2, String str3, String str4) {
        return createTagString(str, str2, str3, str4, null);
    }

    private static int parseTagString(String str, String[] strArr) {
        LocaleIDParser localeIDParser = new LocaleIDParser(str);
        String language = localeIDParser.getLanguage();
        String script = localeIDParser.getScript();
        String country = localeIDParser.getCountry();
        if (isEmptyString(language)) {
            strArr[0] = UNDEFINED_LANGUAGE;
        } else {
            strArr[0] = language;
        }
        if (script.equals(UNDEFINED_SCRIPT)) {
            strArr[1] = "";
        } else {
            strArr[1] = script;
        }
        if (country.equals(UNDEFINED_REGION)) {
            strArr[2] = "";
        } else {
            strArr[2] = country;
        }
        String variant = localeIDParser.getVariant();
        if (!isEmptyString(variant)) {
            int indexOf = str.indexOf(variant);
            return indexOf > 0 ? indexOf - 1 : indexOf;
        }
        int indexOf2 = str.indexOf(64);
        return indexOf2 == -1 ? str.length() : indexOf2;
    }

    private static String lookupLikelySubtags(String str) {
        try {
            return UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", "likelySubtags").getString(str);
        } catch (MissingResourceException unused) {
            return null;
        }
    }

    private static String createLikelySubtagsString(String str, String str2, String str3, String str4) {
        String lookupLikelySubtags;
        String lookupLikelySubtags2;
        String lookupLikelySubtags3;
        if (!isEmptyString(str2) && !isEmptyString(str3) && (lookupLikelySubtags3 = lookupLikelySubtags(createTagString(str, str2, str3, null))) != null) {
            return createTagString(null, null, null, str4, lookupLikelySubtags3);
        }
        if (!isEmptyString(str2) && (lookupLikelySubtags2 = lookupLikelySubtags(createTagString(str, str2, null, null))) != null) {
            return createTagString(null, null, str3, str4, lookupLikelySubtags2);
        }
        if (!isEmptyString(str3) && (lookupLikelySubtags = lookupLikelySubtags(createTagString(str, null, str3, null))) != null) {
            return createTagString(null, str2, null, str4, lookupLikelySubtags);
        }
        String lookupLikelySubtags4 = lookupLikelySubtags(createTagString(str, null, null, null));
        if (lookupLikelySubtags4 != null) {
            return createTagString(null, str2, str3, str4, lookupLikelySubtags4);
        }
        return null;
    }

    public String getExtension(char c) {
        if (LocaleExtensions.isValidKey(c)) {
            return extensions().getExtensionValue(Character.valueOf(c));
        }
        throw new IllegalArgumentException("Invalid extension key: " + c);
    }

    public Set<Character> getExtensionKeys() {
        return extensions().getKeys();
    }

    public Set<String> getUnicodeLocaleAttributes() {
        return extensions().getUnicodeLocaleAttributes();
    }

    public String getUnicodeLocaleType(String str) {
        if (LocaleExtensions.isValidUnicodeLocaleKey(str)) {
            return extensions().getUnicodeLocaleType(str);
        }
        throw new IllegalArgumentException("Invalid Unicode locale key: " + str);
    }

    public Set<String> getUnicodeLocaleKeys() {
        return extensions().getUnicodeLocaleKeys();
    }

    public String toLanguageTag() {
        BaseLocale base = base();
        LocaleExtensions extensions2 = extensions();
        if (base.getVariant().equalsIgnoreCase("POSIX")) {
            base = BaseLocale.getInstance(base.getLanguage(), base.getScript(), base.getRegion(), "");
            if (extensions2.getUnicodeLocaleType("va") == null) {
                InternalLocaleBuilder internalLocaleBuilder = new InternalLocaleBuilder();
                try {
                    internalLocaleBuilder.setLocale(BaseLocale.ROOT, extensions2);
                    internalLocaleBuilder.setUnicodeLocaleKeyword("va", "posix");
                    extensions2 = internalLocaleBuilder.getLocaleExtensions();
                } catch (LocaleSyntaxException e) {
                    throw new RuntimeException((Throwable) e);
                }
            }
        }
        LanguageTag parseLocale = LanguageTag.parseLocale(base, extensions2);
        StringBuilder sb = new StringBuilder();
        String language = parseLocale.getLanguage();
        if (language.length() > 0) {
            sb.append(LanguageTag.canonicalizeLanguage(language));
        }
        String script = parseLocale.getScript();
        if (script.length() > 0) {
            sb.append("-");
            sb.append(LanguageTag.canonicalizeScript(script));
        }
        String region = parseLocale.getRegion();
        if (region.length() > 0) {
            sb.append("-");
            sb.append(LanguageTag.canonicalizeRegion(region));
        }
        for (String str : parseLocale.getVariants()) {
            sb.append("-");
            sb.append(LanguageTag.canonicalizeVariant(str));
        }
        for (String str2 : parseLocale.getExtensions()) {
            sb.append("-");
            sb.append(LanguageTag.canonicalizeExtension(str2));
        }
        String privateuse = parseLocale.getPrivateuse();
        if (privateuse.length() > 0) {
            if (sb.length() > 0) {
                sb.append("-");
            }
            sb.append("x");
            sb.append("-");
            sb.append(LanguageTag.canonicalizePrivateuse(privateuse));
        }
        return sb.toString();
    }

    public static ULocale forLanguageTag(String str) {
        LanguageTag parse = LanguageTag.parse(str, (ParseStatus) null);
        InternalLocaleBuilder internalLocaleBuilder = new InternalLocaleBuilder();
        internalLocaleBuilder.setLanguageTag(parse);
        return getInstance(internalLocaleBuilder.getBaseLocale(), internalLocaleBuilder.getLocaleExtensions());
    }

    public static String toUnicodeLocaleKey(String str) {
        String bcpKey = KeyTypeData.toBcpKey(str);
        return (bcpKey != null || !UnicodeLocaleExtension.isKey(str)) ? bcpKey : AsciiUtil.toLowerString(str);
    }

    public static String toUnicodeLocaleType(String str, String str2) {
        String bcpType = KeyTypeData.toBcpType(str, str2, (Output) null, (Output) null);
        return (bcpType != null || !UnicodeLocaleExtension.isType(str2)) ? bcpType : AsciiUtil.toLowerString(str2);
    }

    public static String toLegacyKey(String str) {
        String legacyKey = KeyTypeData.toLegacyKey(str);
        return (legacyKey != null || !str.matches("[0-9a-zA-Z]+")) ? legacyKey : AsciiUtil.toLowerString(str);
    }

    public static String toLegacyType(String str, String str2) {
        String legacyType = KeyTypeData.toLegacyType(str, str2, (Output) null, (Output) null);
        return (legacyType != null || !str2.matches("[0-9a-zA-Z]+([_/\\-][0-9a-zA-Z]+)*")) ? legacyType : AsciiUtil.toLowerString(str2);
    }

    public static final class Builder {
        private final InternalLocaleBuilder _locbld = new InternalLocaleBuilder();

        public Builder setLocale(ULocale uLocale) {
            try {
                this._locbld.setLocale(uLocale.base(), uLocale.extensions());
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setLanguageTag(String str) {
            ParseStatus parseStatus = new ParseStatus();
            LanguageTag parse = LanguageTag.parse(str, parseStatus);
            if (!parseStatus.isError()) {
                this._locbld.setLanguageTag(parse);
                return this;
            }
            throw new IllformedLocaleException(parseStatus.getErrorMessage(), parseStatus.getErrorIndex());
        }

        public Builder setLanguage(String str) {
            try {
                this._locbld.setLanguage(str);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setScript(String str) {
            try {
                this._locbld.setScript(str);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setRegion(String str) {
            try {
                this._locbld.setRegion(str);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setVariant(String str) {
            try {
                this._locbld.setVariant(str);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setExtension(char c, String str) {
            try {
                this._locbld.setExtension(c, str);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setUnicodeLocaleKeyword(String str, String str2) {
            try {
                this._locbld.setUnicodeLocaleKeyword(str, str2);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder addUnicodeLocaleAttribute(String str) {
            try {
                this._locbld.addUnicodeLocaleAttribute(str);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder removeUnicodeLocaleAttribute(String str) {
            try {
                this._locbld.removeUnicodeLocaleAttribute(str);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder clear() {
            this._locbld.clear();
            return this;
        }

        public Builder clearExtensions() {
            this._locbld.clearExtensions();
            return this;
        }

        public ULocale build() {
            return ULocale.getInstance(this._locbld.getBaseLocale(), this._locbld.getLocaleExtensions());
        }
    }

    /* access modifiers changed from: private */
    public static ULocale getInstance(BaseLocale baseLocale2, LocaleExtensions localeExtensions) {
        String lscvToID = lscvToID(baseLocale2.getLanguage(), baseLocale2.getScript(), baseLocale2.getRegion(), baseLocale2.getVariant());
        Set<Character> keys = localeExtensions.getKeys();
        if (!keys.isEmpty()) {
            TreeMap treeMap = new TreeMap();
            for (Character ch : keys) {
                UnicodeLocaleExtension extension = localeExtensions.getExtension(ch);
                if (extension instanceof UnicodeLocaleExtension) {
                    UnicodeLocaleExtension unicodeLocaleExtension = extension;
                    for (String str : unicodeLocaleExtension.getUnicodeLocaleKeys()) {
                        String unicodeLocaleType = unicodeLocaleExtension.getUnicodeLocaleType(str);
                        String legacyKey = toLegacyKey(str);
                        if (unicodeLocaleType.length() == 0) {
                            unicodeLocaleType = "yes";
                        }
                        String legacyType = toLegacyType(str, unicodeLocaleType);
                        if (!legacyKey.equals("va") || !legacyType.equals("posix") || baseLocale2.getVariant().length() != 0) {
                            treeMap.put(legacyKey, legacyType);
                        } else {
                            lscvToID = lscvToID + "_POSIX";
                        }
                    }
                    Set<String> unicodeLocaleAttributes = unicodeLocaleExtension.getUnicodeLocaleAttributes();
                    if (unicodeLocaleAttributes.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (String str2 : unicodeLocaleAttributes) {
                            if (sb.length() > 0) {
                                sb.append('-');
                            }
                            sb.append(str2);
                        }
                        treeMap.put(LOCALE_ATTRIBUTE_KEY, sb.toString());
                    }
                } else {
                    treeMap.put(String.valueOf(ch), extension.getValue());
                }
            }
            if (!treeMap.isEmpty()) {
                StringBuilder sb2 = new StringBuilder(lscvToID);
                sb2.append("@");
                boolean z = false;
                for (Map.Entry entry : treeMap.entrySet()) {
                    if (z) {
                        sb2.append(";");
                    } else {
                        z = true;
                    }
                    sb2.append((String) entry.getKey());
                    sb2.append("=");
                    sb2.append((String) entry.getValue());
                }
                lscvToID = sb2.toString();
            }
        }
        return new ULocale(lscvToID);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private BaseLocale base() {
        String str;
        String str2;
        String str3;
        if (this.baseLocale == null) {
            String str4 = "";
            if (!equals(ROOT)) {
                LocaleIDParser localeIDParser = new LocaleIDParser(this.localeID);
                String language = localeIDParser.getLanguage();
                str2 = localeIDParser.getScript();
                str = localeIDParser.getCountry();
                str3 = localeIDParser.getVariant();
                str4 = language;
            } else {
                str3 = str4;
                str2 = str3;
                str = str2;
            }
            this.baseLocale = BaseLocale.getInstance(str4, str2, str, str3);
        }
        return this.baseLocale;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private LocaleExtensions extensions() {
        if (this.extensions == null) {
            Iterator<String> keywords = getKeywords();
            if (keywords == null) {
                this.extensions = LocaleExtensions.EMPTY_EXTENSIONS;
            } else {
                InternalLocaleBuilder internalLocaleBuilder = new InternalLocaleBuilder();
                while (keywords.hasNext()) {
                    String next = keywords.next();
                    if (next.equals(LOCALE_ATTRIBUTE_KEY)) {
                        for (String str : getKeywordValue(next).split("[-_]")) {
                            try {
                                internalLocaleBuilder.addUnicodeLocaleAttribute(str);
                            } catch (LocaleSyntaxException unused) {
                            }
                        }
                    } else if (next.length() >= 2) {
                        String unicodeLocaleKey = toUnicodeLocaleKey(next);
                        String unicodeLocaleType = toUnicodeLocaleType(next, getKeywordValue(next));
                        if (!(unicodeLocaleKey == null || unicodeLocaleType == null)) {
                            try {
                                internalLocaleBuilder.setUnicodeLocaleKeyword(unicodeLocaleKey, unicodeLocaleType);
                            } catch (LocaleSyntaxException unused2) {
                            }
                        }
                    } else if (next.length() == 1 && next.charAt(0) != 'u') {
                        internalLocaleBuilder.setExtension(next.charAt(0), getKeywordValue(next).replace("_", "-"));
                    }
                }
                this.extensions = internalLocaleBuilder.getLocaleExtensions();
            }
        }
        return this.extensions;
    }

    /* access modifiers changed from: private */
    public static final class JDKLocaleHelper {
        private static Object eDISPLAY = null;
        private static Object eFORMAT = null;
        private static boolean hasLocaleCategories = true;
        private static Method mGetDefault;
        private static Method mSetDefault;

        /* JADX DEBUG: Multi-variable search result rejected for r6v3, resolved type: java.lang.Object[] */
        /* JADX WARN: Multi-variable type inference failed */
        static {
            Class<?> cls;
            try {
                Class<?>[] declaredClasses = Locale.class.getDeclaredClasses();
                int length = declaredClasses.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        cls = null;
                        break;
                    }
                    cls = declaredClasses[i];
                    if (cls.getName().equals("java.util.Locale$Category")) {
                        break;
                    }
                    i++;
                }
                if (cls != null) {
                    mGetDefault = Locale.class.getDeclaredMethod("getDefault", cls);
                    mSetDefault = Locale.class.getDeclaredMethod("setDefault", cls, Locale.class);
                    Method method = cls.getMethod(SecKeyChain.SPEC_ALIAS, null);
                    Object[] enumConstants = cls.getEnumConstants();
                    for (Object obj : enumConstants) {
                        String str = (String) method.invoke(obj, null);
                        if (str.equals("DISPLAY")) {
                            eDISPLAY = obj;
                        } else if (str.equals("FORMAT")) {
                            eFORMAT = obj;
                        }
                    }
                    if (eDISPLAY == null) {
                        return;
                    }
                    if (eFORMAT != null) {
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException unused) {
            }
        }

        private JDKLocaleHelper() {
        }

        public static boolean hasLocaleCategories() {
            return hasLocaleCategories;
        }

        public static ULocale toULocale(Locale locale) {
            TreeSet<String> treeSet;
            TreeMap treeMap;
            String language = locale.getLanguage();
            String country = locale.getCountry();
            String variant = locale.getVariant();
            String script = locale.getScript();
            Set<Character> extensionKeys = locale.getExtensionKeys();
            if (!extensionKeys.isEmpty()) {
                treeMap = null;
                treeSet = null;
                for (Character ch : extensionKeys) {
                    if (ch.charValue() == 'u') {
                        Set<String> unicodeLocaleAttributes = locale.getUnicodeLocaleAttributes();
                        if (!unicodeLocaleAttributes.isEmpty()) {
                            treeSet = new TreeSet();
                            for (String str : unicodeLocaleAttributes) {
                                treeSet.add(str);
                            }
                        }
                        for (String str2 : locale.getUnicodeLocaleKeys()) {
                            String unicodeLocaleType = locale.getUnicodeLocaleType(str2);
                            if (unicodeLocaleType != null) {
                                if (!str2.equals("va")) {
                                    if (treeMap == null) {
                                        treeMap = new TreeMap();
                                    }
                                    treeMap.put(str2, unicodeLocaleType);
                                } else if (variant.length() == 0) {
                                    variant = unicodeLocaleType;
                                } else {
                                    variant = unicodeLocaleType + "_" + variant;
                                }
                            }
                        }
                    } else {
                        String extension = locale.getExtension(ch.charValue());
                        if (extension != null) {
                            if (treeMap == null) {
                                treeMap = new TreeMap();
                            }
                            treeMap.put(String.valueOf(ch), extension);
                        }
                    }
                }
            } else {
                treeMap = null;
                treeSet = null;
            }
            if (language.equals("no") && country.equals("NO") && variant.equals("NY")) {
                language = "nn";
                variant = "";
            }
            StringBuilder sb = new StringBuilder(language);
            if (script.length() > 0) {
                sb.append(ULocale.UNDERSCORE);
                sb.append(script);
            }
            if (country.length() > 0) {
                sb.append(ULocale.UNDERSCORE);
                sb.append(country);
            }
            if (variant.length() > 0) {
                if (country.length() == 0) {
                    sb.append(ULocale.UNDERSCORE);
                }
                sb.append(ULocale.UNDERSCORE);
                sb.append(variant);
            }
            if (treeSet != null) {
                StringBuilder sb2 = new StringBuilder();
                for (String str3 : treeSet) {
                    if (sb2.length() != 0) {
                        sb2.append('-');
                    }
                    sb2.append(str3);
                }
                if (treeMap == null) {
                    treeMap = new TreeMap();
                }
                treeMap.put(ULocale.LOCALE_ATTRIBUTE_KEY, sb2.toString());
            }
            if (treeMap != null) {
                sb.append('@');
                boolean z = false;
                for (Map.Entry entry : treeMap.entrySet()) {
                    String str4 = (String) entry.getKey();
                    String str5 = (String) entry.getValue();
                    if (str4.length() != 1) {
                        str4 = ULocale.toLegacyKey(str4);
                        if (str5.length() == 0) {
                            str5 = "yes";
                        }
                        str5 = ULocale.toLegacyType(str4, str5);
                    }
                    if (z) {
                        sb.append(TelephoneNumberUtils.WAIT);
                    } else {
                        z = true;
                    }
                    sb.append(str4);
                    sb.append('=');
                    sb.append(str5);
                }
            }
            return new ULocale(ULocale.getName(sb.toString()), locale);
        }

        public static Locale toLocale(ULocale uLocale) {
            Locale locale;
            String name = uLocale.getName();
            if (uLocale.getScript().length() > 0 || name.contains("@")) {
                locale = Locale.forLanguageTag(AsciiUtil.toUpperString(uLocale.toLanguageTag()));
            } else {
                locale = null;
            }
            return locale == null ? new Locale(uLocale.getLanguage(), uLocale.getCountry(), uLocale.getVariant()) : locale;
        }

        public static Locale getDefault(Category category) {
            Object obj;
            if (hasLocaleCategories) {
                int i = AnonymousClass3.$SwitchMap$ohos$global$icu$util$ULocale$Category[category.ordinal()];
                if (i == 1) {
                    obj = eDISPLAY;
                } else if (i != 2) {
                    obj = null;
                } else {
                    obj = eFORMAT;
                }
                if (obj != null) {
                    try {
                        return (Locale) mGetDefault.invoke(null, obj);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException unused) {
                    }
                }
            }
            return Locale.getDefault();
        }

        public static void setDefault(Category category, Locale locale) {
            Object obj;
            if (hasLocaleCategories) {
                int i = AnonymousClass3.$SwitchMap$ohos$global$icu$util$ULocale$Category[category.ordinal()];
                if (i == 1) {
                    obj = eDISPLAY;
                } else if (i != 2) {
                    obj = null;
                } else {
                    obj = eFORMAT;
                }
                if (obj != null) {
                    try {
                        mSetDefault.invoke(null, obj, locale);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException unused) {
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.util.ULocale$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$util$ULocale$Category = new int[Category.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$util$ULocale$Category[Category.DISPLAY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$ULocale$Category[Category.FORMAT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }
}
