package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.text.MessageFormat;
import libcore.icu.ICU;
import sun.security.x509.PolicyInformation;
import sun.util.locale.BaseLocale;
import sun.util.locale.InternalLocaleBuilder;
import sun.util.locale.LanguageTag;
import sun.util.locale.LocaleExtensions;
import sun.util.locale.LocaleMatcher;
import sun.util.locale.LocaleObjectCache;
import sun.util.locale.LocaleSyntaxException;
import sun.util.locale.LocaleUtils;
import sun.util.locale.ParseStatus;

public final class Locale implements Cloneable, Serializable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final Locale CANADA = createConstant("en", "CA");
    public static final Locale CANADA_FRENCH = createConstant("fr", "CA");
    public static final Locale CHINA = SIMPLIFIED_CHINESE;
    public static final Locale CHINESE = createConstant("zh", "");
    private static final int DISPLAY_COUNTRY = 1;
    private static final int DISPLAY_LANGUAGE = 0;
    private static final int DISPLAY_SCRIPT = 3;
    private static final int DISPLAY_VARIANT = 2;
    public static final Locale ENGLISH = createConstant("en", "");
    public static final Locale FRANCE = createConstant("fr", "FR");
    public static final Locale FRENCH = createConstant("fr", "");
    public static final Locale GERMAN = createConstant("de", "");
    public static final Locale GERMANY = createConstant("de", "DE");
    public static final Locale ITALIAN = createConstant("it", "");
    public static final Locale ITALY = createConstant("it", "IT");
    public static final Locale JAPAN = createConstant("ja", "JP");
    public static final Locale JAPANESE = createConstant("ja", "");
    public static final Locale KOREA = createConstant("ko", "KR");
    public static final Locale KOREAN = createConstant("ko", "");
    private static final Cache LOCALECACHE = new Cache();
    public static final Locale PRC = SIMPLIFIED_CHINESE;
    public static final char PRIVATE_USE_EXTENSION = 'x';
    public static final Locale ROOT = createConstant("", "");
    public static final Locale SIMPLIFIED_CHINESE = createConstant("zh", "CN");
    public static final Locale TAIWAN = TRADITIONAL_CHINESE;
    public static final Locale TRADITIONAL_CHINESE = createConstant("zh", "TW");
    public static final Locale UK = createConstant("en", "GB");
    private static final String UNDETERMINED_LANGUAGE = "und";
    public static final char UNICODE_LOCALE_EXTENSION = 'u';
    public static final Locale US = createConstant("en", "US");
    private static volatile Locale defaultDisplayLocale = null;
    private static volatile Locale defaultFormatLocale = null;
    private static volatile String[] isoCountries = null;
    private static volatile String[] isoLanguages = null;
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("language", String.class), new ObjectStreamField("country", String.class), new ObjectStreamField("variant", String.class), new ObjectStreamField("hashcode", Integer.TYPE), new ObjectStreamField("script", String.class), new ObjectStreamField("extensions", String.class)};
    static final long serialVersionUID = 9149081749638150636L;
    /* access modifiers changed from: private */
    public transient BaseLocale baseLocale;
    private volatile transient int hashCodeValue;
    private volatile transient String languageTag;
    /* access modifiers changed from: private */
    public transient LocaleExtensions localeExtensions;

    public static final class Builder {
        private final InternalLocaleBuilder localeBuilder = new InternalLocaleBuilder();

        public Builder setLocale(Locale locale) {
            try {
                this.localeBuilder.setLocale(locale.baseLocale, locale.localeExtensions);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setLanguageTag(String languageTag) {
            ParseStatus sts = new ParseStatus();
            LanguageTag tag = LanguageTag.parse(languageTag, sts);
            if (!sts.isError()) {
                this.localeBuilder.setLanguageTag(tag);
                return this;
            }
            throw new IllformedLocaleException(sts.getErrorMessage(), sts.getErrorIndex());
        }

        public Builder setLanguage(String language) {
            try {
                this.localeBuilder.setLanguage(language);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setScript(String script) {
            try {
                this.localeBuilder.setScript(script);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setRegion(String region) {
            try {
                this.localeBuilder.setRegion(region);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setVariant(String variant) {
            try {
                this.localeBuilder.setVariant(variant);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setExtension(char key, String value) {
            try {
                this.localeBuilder.setExtension(key, value);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setUnicodeLocaleKeyword(String key, String type) {
            try {
                this.localeBuilder.setUnicodeLocaleKeyword(key, type);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder addUnicodeLocaleAttribute(String attribute) {
            try {
                this.localeBuilder.addUnicodeLocaleAttribute(attribute);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder removeUnicodeLocaleAttribute(String attribute) {
            if (attribute != null) {
                try {
                    this.localeBuilder.removeUnicodeLocaleAttribute(attribute);
                    return this;
                } catch (LocaleSyntaxException e) {
                    throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
                }
            } else {
                throw new NullPointerException("attribute == null");
            }
        }

        public Builder clear() {
            this.localeBuilder.clear();
            return this;
        }

        public Builder clearExtensions() {
            this.localeBuilder.clearExtensions();
            return this;
        }

        public Locale build() {
            BaseLocale baseloc = this.localeBuilder.getBaseLocale();
            LocaleExtensions extensions = this.localeBuilder.getLocaleExtensions();
            if (extensions == null && baseloc.getVariant().length() > 0) {
                extensions = Locale.getCompatibilityExtensions(baseloc.getLanguage(), baseloc.getScript(), baseloc.getRegion(), baseloc.getVariant());
            }
            return Locale.getInstance(baseloc, extensions);
        }
    }

    private static class Cache extends LocaleObjectCache<LocaleKey, Locale> {
        private Cache() {
        }

        /* access modifiers changed from: protected */
        public Locale createObject(LocaleKey key) {
            return new Locale(key.base, key.exts);
        }
    }

    public enum Category {
        DISPLAY("user.language.display", "user.script.display", "user.country.display", "user.variant.display"),
        FORMAT("user.language.format", "user.script.format", "user.country.format", "user.variant.format");
        
        final String countryKey;
        final String languageKey;
        final String scriptKey;
        final String variantKey;

        private Category(String languageKey2, String scriptKey2, String countryKey2, String variantKey2) {
            this.languageKey = languageKey2;
            this.scriptKey = scriptKey2;
            this.countryKey = countryKey2;
            this.variantKey = variantKey2;
        }
    }

    public enum FilteringMode {
        AUTOSELECT_FILTERING,
        EXTENDED_FILTERING,
        IGNORE_EXTENDED_RANGES,
        MAP_EXTENDED_RANGES,
        REJECT_EXTENDED_RANGES
    }

    public static final class LanguageRange {
        public static final double MAX_WEIGHT = 1.0d;
        public static final double MIN_WEIGHT = 0.0d;
        private volatile int hash;
        private final String range;
        private final double weight;

        public LanguageRange(String range2) {
            this(range2, 1.0d);
        }

        public LanguageRange(String range2, double weight2) {
            this.hash = 0;
            if (range2 == null) {
                throw new NullPointerException();
            } else if (weight2 < 0.0d || weight2 > 1.0d) {
                throw new IllegalArgumentException("weight=" + weight2);
            } else {
                String range3 = range2.toLowerCase();
                boolean isIllFormed = Locale.$assertionsDisabled;
                String[] subtags = range3.split(LanguageTag.SEP);
                int i = 1;
                if (isSubtagIllFormed(subtags[0], true) || range3.endsWith(LanguageTag.SEP)) {
                    isIllFormed = true;
                } else {
                    while (true) {
                        int i2 = i;
                        if (i2 >= subtags.length) {
                            break;
                        } else if (isSubtagIllFormed(subtags[i2], Locale.$assertionsDisabled)) {
                            isIllFormed = true;
                            break;
                        } else {
                            i = i2 + 1;
                        }
                    }
                }
                if (!isIllFormed) {
                    this.range = range3;
                    this.weight = weight2;
                    return;
                }
                throw new IllegalArgumentException("range=" + range3);
            }
        }

        private static boolean isSubtagIllFormed(String subtag, boolean isFirstSubtag) {
            if (subtag.equals("") || subtag.length() > 8) {
                return true;
            }
            if (subtag.equals("*")) {
                return Locale.$assertionsDisabled;
            }
            char[] charArray = subtag.toCharArray();
            if (isFirstSubtag) {
                for (char c : charArray) {
                    if (c < 'a' || c > 'z') {
                        return true;
                    }
                }
            } else {
                for (char c2 : charArray) {
                    if (c2 < '0' || ((c2 > '9' && c2 < 'a') || c2 > 'z')) {
                        return true;
                    }
                }
            }
            return Locale.$assertionsDisabled;
        }

        public String getRange() {
            return this.range;
        }

        public double getWeight() {
            return this.weight;
        }

        public static List<LanguageRange> parse(String ranges) {
            return LocaleMatcher.parse(ranges);
        }

        public static List<LanguageRange> parse(String ranges, Map<String, List<String>> map) {
            return mapEquivalents(parse(ranges), map);
        }

        public static List<LanguageRange> mapEquivalents(List<LanguageRange> priorityList, Map<String, List<String>> map) {
            return LocaleMatcher.mapEquivalents(priorityList, map);
        }

        public int hashCode() {
            if (this.hash == 0) {
                int result = (37 * 17) + this.range.hashCode();
                long bitsWeight = Double.doubleToLongBits(this.weight);
                this.hash = (37 * result) + ((int) ((bitsWeight >>> 32) ^ bitsWeight));
            }
            return this.hash;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LanguageRange)) {
                return Locale.$assertionsDisabled;
            }
            LanguageRange other = (LanguageRange) obj;
            if (!(this.hash == other.hash && this.range.equals(other.range) && this.weight == other.weight)) {
                z = false;
            }
            return z;
        }
    }

    private static final class LocaleKey {
        /* access modifiers changed from: private */
        public final BaseLocale base;
        /* access modifiers changed from: private */
        public final LocaleExtensions exts;
        private final int hash;

        private LocaleKey(BaseLocale baseLocale, LocaleExtensions extensions) {
            this.base = baseLocale;
            this.exts = extensions;
            this.hash = this.exts != null ? this.base.hashCode() ^ this.exts.hashCode() : h;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LocaleKey)) {
                return Locale.$assertionsDisabled;
            }
            LocaleKey other = (LocaleKey) obj;
            if (this.hash != other.hash || !this.base.equals(other.base)) {
                return Locale.$assertionsDisabled;
            }
            if (this.exts != null) {
                return this.exts.equals(other.exts);
            }
            if (other.exts != null) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return this.hash;
        }
    }

    private static class NoImagePreloadHolder {
        public static volatile Locale defaultLocale = Locale.initDefault();

        private NoImagePreloadHolder() {
        }
    }

    private Locale(BaseLocale baseLocale2, LocaleExtensions extensions) {
        this.hashCodeValue = 0;
        this.baseLocale = baseLocale2;
        this.localeExtensions = extensions;
    }

    public Locale(String language, String country, String variant) {
        this.hashCodeValue = 0;
        if (language == null || country == null || variant == null) {
            throw new NullPointerException();
        }
        this.baseLocale = BaseLocale.getInstance(convertOldISOCodes(language), "", country, variant);
        this.localeExtensions = getCompatibilityExtensions(language, "", country, variant);
    }

    public Locale(String language, String country) {
        this(language, country, "");
    }

    public Locale(String language) {
        this(language, "", "");
    }

    private static Locale createConstant(String lang, String country) {
        return getInstance(BaseLocale.createInstance(lang, country), null);
    }

    static Locale getInstance(String language, String country, String variant) {
        return getInstance(language, "", country, variant, null);
    }

    static Locale getInstance(String language, String script, String country, String variant, LocaleExtensions extensions) {
        if (language == null || script == null || country == null || variant == null) {
            throw new NullPointerException();
        }
        if (extensions == null) {
            extensions = getCompatibilityExtensions(language, script, country, variant);
        }
        return getInstance(BaseLocale.getInstance(language, script, country, variant), extensions);
    }

    static Locale getInstance(BaseLocale baseloc, LocaleExtensions extensions) {
        return (Locale) LOCALECACHE.get(new LocaleKey(baseloc, extensions));
    }

    public static Locale getDefault() {
        return NoImagePreloadHolder.defaultLocale;
    }

    public static Locale getDefault(Category category) {
        switch (category) {
            case DISPLAY:
                if (defaultDisplayLocale == null) {
                    synchronized (Locale.class) {
                        if (defaultDisplayLocale == null) {
                            defaultDisplayLocale = initDefault(category);
                        }
                    }
                }
                return defaultDisplayLocale;
            case FORMAT:
                if (defaultFormatLocale == null) {
                    synchronized (Locale.class) {
                        if (defaultFormatLocale == null) {
                            defaultFormatLocale = initDefault(category);
                        }
                    }
                }
                return defaultFormatLocale;
            default:
                return getDefault();
        }
    }

    public static Locale initDefault() {
        String variant;
        String country;
        String script;
        String languageTag2 = System.getProperty("user.locale", "");
        if (!languageTag2.isEmpty()) {
            return forLanguageTag(languageTag2);
        }
        String language = System.getProperty("user.language", "en");
        String region = System.getProperty("user.region");
        if (region != null) {
            int i = region.indexOf(95);
            if (i >= 0) {
                country = region.substring(0, i);
                variant = region.substring(i + 1);
            } else {
                country = region;
                variant = "";
            }
            script = "";
        } else {
            script = System.getProperty("user.script", "");
            country = System.getProperty("user.country", "");
            variant = System.getProperty("user.variant", "");
        }
        return getInstance(language, script, country, variant, null);
    }

    private static Locale initDefault(Category category) {
        Locale defaultLocale = NoImagePreloadHolder.defaultLocale;
        return getInstance(System.getProperty(category.languageKey, defaultLocale.getLanguage()), System.getProperty(category.scriptKey, defaultLocale.getScript()), System.getProperty(category.countryKey, defaultLocale.getCountry()), System.getProperty(category.variantKey, defaultLocale.getVariant()), null);
    }

    public static synchronized void setDefault(Locale newLocale) {
        synchronized (Locale.class) {
            setDefault(Category.DISPLAY, newLocale);
            setDefault(Category.FORMAT, newLocale);
            NoImagePreloadHolder.defaultLocale = newLocale;
            ICU.setDefaultLocale(newLocale.toLanguageTag());
        }
    }

    public static synchronized void setDefault(Category category, Locale newLocale) {
        synchronized (Locale.class) {
            if (category == null) {
                throw new NullPointerException("Category cannot be NULL");
            } else if (newLocale != null) {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkPermission(new PropertyPermission("user.language", "write"));
                }
                switch (category) {
                    case DISPLAY:
                        defaultDisplayLocale = newLocale;
                        break;
                    case FORMAT:
                        defaultFormatLocale = newLocale;
                        break;
                }
            } else {
                throw new NullPointerException("Can't set default locale to NULL");
            }
        }
    }

    public static Locale[] getAvailableLocales() {
        return ICU.getAvailableLocales();
    }

    public static String[] getISOCountries() {
        return ICU.getISOCountries();
    }

    public static String[] getISOLanguages() {
        return ICU.getISOLanguages();
    }

    public String getLanguage() {
        return this.baseLocale.getLanguage();
    }

    public String getScript() {
        return this.baseLocale.getScript();
    }

    public String getCountry() {
        return this.baseLocale.getRegion();
    }

    public String getVariant() {
        return this.baseLocale.getVariant();
    }

    public boolean hasExtensions() {
        if (this.localeExtensions != null) {
            return true;
        }
        return $assertionsDisabled;
    }

    public Locale stripExtensions() {
        return hasExtensions() ? getInstance(this.baseLocale, null) : this;
    }

    public String getExtension(char key) {
        if (!LocaleExtensions.isValidKey(key)) {
            throw new IllegalArgumentException("Ill-formed extension key: " + key);
        } else if (hasExtensions()) {
            return this.localeExtensions.getExtensionValue(Character.valueOf(key));
        } else {
            return null;
        }
    }

    public Set<Character> getExtensionKeys() {
        if (!hasExtensions()) {
            return Collections.emptySet();
        }
        return this.localeExtensions.getKeys();
    }

    public Set<String> getUnicodeLocaleAttributes() {
        if (!hasExtensions()) {
            return Collections.emptySet();
        }
        return this.localeExtensions.getUnicodeLocaleAttributes();
    }

    public String getUnicodeLocaleType(String key) {
        if (!isUnicodeExtensionKey(key)) {
            throw new IllegalArgumentException("Ill-formed Unicode locale key: " + key);
        } else if (hasExtensions()) {
            return this.localeExtensions.getUnicodeLocaleType(key);
        } else {
            return null;
        }
    }

    public Set<String> getUnicodeLocaleKeys() {
        if (this.localeExtensions == null) {
            return Collections.emptySet();
        }
        return this.localeExtensions.getUnicodeLocaleKeys();
    }

    /* access modifiers changed from: package-private */
    public BaseLocale getBaseLocale() {
        return this.baseLocale;
    }

    /* access modifiers changed from: package-private */
    public LocaleExtensions getLocaleExtensions() {
        return this.localeExtensions;
    }

    public final String toString() {
        int length = this.baseLocale.getLanguage().length();
        boolean e = $assertionsDisabled;
        boolean l = length != 0;
        boolean s = this.baseLocale.getScript().length() != 0;
        boolean r = this.baseLocale.getRegion().length() != 0;
        boolean v = this.baseLocale.getVariant().length() != 0;
        if (!(this.localeExtensions == null || this.localeExtensions.getID().length() == 0)) {
            e = true;
        }
        StringBuilder result = new StringBuilder(this.baseLocale.getLanguage());
        if (r || (l && (v || s || e))) {
            result.append('_');
            result.append(this.baseLocale.getRegion());
        }
        if (v && (l || r)) {
            result.append('_');
            result.append(this.baseLocale.getVariant());
        }
        if (s && (l || r)) {
            result.append("_#");
            result.append(this.baseLocale.getScript());
        }
        if (e && (l || r)) {
            result.append('_');
            if (!s) {
                result.append('#');
            }
            result.append(this.localeExtensions.getID());
        }
        return result.toString();
    }

    public String toLanguageTag() {
        if (this.languageTag != null) {
            return this.languageTag;
        }
        LanguageTag tag = LanguageTag.parseLocale(this.baseLocale, this.localeExtensions);
        StringBuilder buf = new StringBuilder();
        String subtag = tag.getLanguage();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.canonicalizeLanguage(subtag));
        }
        String subtag2 = tag.getScript();
        if (subtag2.length() > 0) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeScript(subtag2));
        }
        String subtag3 = tag.getRegion();
        if (subtag3.length() > 0) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeRegion(subtag3));
        }
        for (String s : tag.getVariants()) {
            buf.append(LanguageTag.SEP);
            buf.append(s);
        }
        for (String s2 : tag.getExtensions()) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeExtension(s2));
        }
        String subtag4 = tag.getPrivateuse();
        if (subtag4.length() > 0) {
            if (buf.length() > 0) {
                buf.append(LanguageTag.SEP);
            }
            buf.append(LanguageTag.PRIVATEUSE);
            buf.append(LanguageTag.SEP);
            buf.append(subtag4);
        }
        String langTag = buf.toString();
        synchronized (this) {
            if (this.languageTag == null) {
                this.languageTag = langTag;
            }
        }
        return this.languageTag;
    }

    public static Locale forLanguageTag(String languageTag2) {
        LanguageTag tag = LanguageTag.parse(languageTag2, null);
        InternalLocaleBuilder bldr = new InternalLocaleBuilder();
        bldr.setLanguageTag(tag);
        BaseLocale base = bldr.getBaseLocale();
        LocaleExtensions exts = bldr.getLocaleExtensions();
        if (exts == null && base.getVariant().length() > 0) {
            exts = getCompatibilityExtensions(base.getLanguage(), base.getScript(), base.getRegion(), base.getVariant());
        }
        return getInstance(base, exts);
    }

    public String getISO3Language() throws MissingResourceException {
        String lang = this.baseLocale.getLanguage();
        if (lang.length() == 3) {
            return lang;
        }
        if (lang.isEmpty()) {
            return "";
        }
        String language3 = ICU.getISO3Language(lang);
        if (lang.isEmpty() || !language3.isEmpty()) {
            return language3;
        }
        throw new MissingResourceException("Couldn't find 3-letter language code for " + lang, "FormatData_" + toString(), "ShortLanguage");
    }

    public String getISO3Country() throws MissingResourceException {
        String region = this.baseLocale.getRegion();
        if (region.length() == 3) {
            return this.baseLocale.getRegion();
        }
        if (region.isEmpty()) {
            return "";
        }
        String country3 = ICU.getISO3Country("en-" + region);
        if (region.isEmpty() || !country3.isEmpty()) {
            return country3;
        }
        throw new MissingResourceException("Couldn't find 3-letter country code for " + this.baseLocale.getRegion(), "FormatData_" + toString(), "ShortCountry");
    }

    public final String getDisplayLanguage() {
        return getDisplayLanguage(getDefault(Category.DISPLAY));
    }

    public String getDisplayLanguage(Locale locale) {
        String languageCode = this.baseLocale.getLanguage();
        if (languageCode.isEmpty()) {
            return "";
        }
        if ("und".equals(normalizeAndValidateLanguage(languageCode, $assertionsDisabled))) {
            return languageCode;
        }
        String result = ICU.getDisplayLanguage(this, locale);
        if (result == null) {
            result = ICU.getDisplayLanguage(this, getDefault());
        }
        return result;
    }

    private static String normalizeAndValidateLanguage(String language, boolean strict) {
        if (language == null || language.isEmpty()) {
            return "";
        }
        String lowercaseLanguage = language.toLowerCase(ROOT);
        if (isValidBcp47Alpha(lowercaseLanguage, 2, 3)) {
            return lowercaseLanguage;
        }
        if (!strict) {
            return "und";
        }
        throw new IllformedLocaleException("Invalid language: " + language);
    }

    private static boolean isAsciiAlphaNum(String string) {
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if ((character < 'a' || character > 'z') && ((character < 'A' || character > 'Z') && (character < '0' || character > '9'))) {
                return $assertionsDisabled;
            }
        }
        return true;
    }

    public String getDisplayScript() {
        return getDisplayScript(getDefault(Category.DISPLAY));
    }

    public String getDisplayScript(Locale inLocale) {
        if (this.baseLocale.getScript().isEmpty()) {
            return "";
        }
        String result = ICU.getDisplayScript(this, inLocale);
        if (result == null) {
            result = ICU.getDisplayScript(this, getDefault(Category.DISPLAY));
        }
        return result;
    }

    public final String getDisplayCountry() {
        return getDisplayCountry(getDefault(Category.DISPLAY));
    }

    public String getDisplayCountry(Locale locale) {
        String countryCode = this.baseLocale.getRegion();
        if (countryCode.isEmpty()) {
            return "";
        }
        if (normalizeAndValidateRegion(countryCode, $assertionsDisabled).isEmpty()) {
            return countryCode;
        }
        String result = ICU.getDisplayCountry(this, locale);
        if (result == null) {
            result = ICU.getDisplayCountry(this, getDefault());
        }
        return result;
    }

    private static String normalizeAndValidateRegion(String region, boolean strict) {
        if (region == null || region.isEmpty()) {
            return "";
        }
        String uppercaseRegion = region.toUpperCase(ROOT);
        if (isValidBcp47Alpha(uppercaseRegion, 2, 2) || isUnM49AreaCode(uppercaseRegion)) {
            return uppercaseRegion;
        }
        if (!strict) {
            return "";
        }
        throw new IllformedLocaleException("Invalid region: " + region);
    }

    private static boolean isValidBcp47Alpha(String string, int lowerBound, int upperBound) {
        int length = string.length();
        if (length < lowerBound || length > upperBound) {
            return $assertionsDisabled;
        }
        for (int i = 0; i < length; i++) {
            char character = string.charAt(i);
            if ((character < 'a' || character > 'z') && (character < 'A' || character > 'Z')) {
                return $assertionsDisabled;
            }
        }
        return true;
    }

    private static boolean isUnM49AreaCode(String code) {
        if (code.length() != 3) {
            return $assertionsDisabled;
        }
        for (int i = 0; i < 3; i++) {
            char character = code.charAt(i);
            if (character < '0' || character > '9') {
                return $assertionsDisabled;
            }
        }
        return true;
    }

    public final String getDisplayVariant() {
        return getDisplayVariant(getDefault(Category.DISPLAY));
    }

    public String getDisplayVariant(Locale inLocale) {
        String variantCode = this.baseLocale.getVariant();
        if (variantCode.isEmpty()) {
            return "";
        }
        try {
            normalizeAndValidateVariant(variantCode);
            String result = ICU.getDisplayVariant(this, inLocale);
            if (result == null) {
                result = ICU.getDisplayVariant(this, getDefault());
            }
            if (result.isEmpty()) {
                return variantCode;
            }
            return result;
        } catch (IllformedLocaleException e) {
            return variantCode;
        }
    }

    private static String normalizeAndValidateVariant(String variant) {
        if (variant == null || variant.isEmpty()) {
            return "";
        }
        String normalizedVariant = variant.replace('-', '_');
        String[] subTags = normalizedVariant.split(BaseLocale.SEP);
        int length = subTags.length;
        int i = 0;
        while (i < length) {
            if (isValidVariantSubtag(subTags[i])) {
                i++;
            } else {
                throw new IllformedLocaleException("Invalid variant: " + variant);
            }
        }
        return normalizedVariant;
    }

    private static boolean isValidVariantSubtag(String subTag) {
        if (subTag.length() < 5 || subTag.length() > 8) {
            if (subTag.length() == 4) {
                char firstChar = subTag.charAt(0);
                if (firstChar < '0' || firstChar > '9' || !isAsciiAlphaNum(subTag)) {
                    return $assertionsDisabled;
                }
                return true;
            }
        } else if (isAsciiAlphaNum(subTag)) {
            return true;
        }
        return $assertionsDisabled;
    }

    public final String getDisplayName() {
        return getDisplayName(getDefault(Category.DISPLAY));
    }

    public String getDisplayName(Locale locale) {
        int count = 0;
        StringBuilder buffer = new StringBuilder();
        String languageCode = this.baseLocale.getLanguage();
        if (!languageCode.isEmpty()) {
            String displayLanguage = getDisplayLanguage(locale);
            buffer.append(displayLanguage.isEmpty() ? languageCode : displayLanguage);
            count = 0 + 1;
        }
        String scriptCode = this.baseLocale.getScript();
        if (!scriptCode.isEmpty()) {
            if (count == 1) {
                buffer.append(" (");
            }
            String displayScript = getDisplayScript(locale);
            buffer.append(displayScript.isEmpty() ? scriptCode : displayScript);
            count++;
        }
        String countryCode = this.baseLocale.getRegion();
        if (!countryCode.isEmpty()) {
            if (count == 1) {
                buffer.append(" (");
            } else if (count == 2) {
                buffer.append(",");
            }
            String displayCountry = getDisplayCountry(locale);
            buffer.append(displayCountry.isEmpty() ? countryCode : displayCountry);
            count++;
        }
        String variantCode = this.baseLocale.getVariant();
        if (!variantCode.isEmpty()) {
            if (count == 1) {
                buffer.append(" (");
            } else if (count == 2 || count == 3) {
                buffer.append(",");
            }
            String displayVariant = getDisplayVariant(locale);
            buffer.append(displayVariant.isEmpty() ? variantCode : displayVariant);
            count++;
        }
        if (count > 1) {
            buffer.append(")");
        }
        return buffer.toString();
    }

    public Object clone() {
        try {
            return (Locale) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError((Throwable) e);
        }
    }

    public int hashCode() {
        int hc = this.hashCodeValue;
        if (hc == 0) {
            hc = this.baseLocale.hashCode();
            if (this.localeExtensions != null) {
                hc ^= this.localeExtensions.hashCode();
            }
            this.hashCodeValue = hc;
        }
        return hc;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Locale)) {
            return $assertionsDisabled;
        }
        if (!this.baseLocale.equals(((Locale) obj).baseLocale)) {
            return $assertionsDisabled;
        }
        if (this.localeExtensions != null) {
            return this.localeExtensions.equals(((Locale) obj).localeExtensions);
        }
        if (((Locale) obj).localeExtensions != null) {
            z = false;
        }
        return z;
    }

    private static String formatList(String[] stringList, String listPattern, String listCompositionPattern) {
        if (listPattern == null || listCompositionPattern == null) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < stringList.length; i++) {
                if (i > 0) {
                    result.append(',');
                }
                result.append(stringList[i]);
            }
            return result.toString();
        }
        if (stringList.length > 3) {
            stringList = composeList(new MessageFormat(listCompositionPattern), stringList);
        }
        Object[] args = new Object[(stringList.length + 1)];
        System.arraycopy((Object) stringList, 0, (Object) args, 1, stringList.length);
        args[0] = new Integer(stringList.length);
        return new MessageFormat(listPattern).format(args);
    }

    private static String[] composeList(MessageFormat format, String[] list) {
        if (list.length <= 3) {
            return list;
        }
        String newItem = format.format(new String[]{list[0], list[1]});
        String[] newList = new String[(list.length - 1)];
        System.arraycopy((Object) list, 2, (Object) newList, 1, newList.length - 1);
        newList[0] = newItem;
        return composeList(format, newList);
    }

    private static boolean isUnicodeExtensionKey(String s) {
        if (s.length() != 2 || !LocaleUtils.isAlphaNumericString(s)) {
            return $assertionsDisabled;
        }
        return true;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("language", (Object) this.baseLocale.getLanguage());
        fields.put("script", (Object) this.baseLocale.getScript());
        fields.put("country", (Object) this.baseLocale.getRegion());
        fields.put("variant", (Object) this.baseLocale.getVariant());
        fields.put("extensions", (Object) this.localeExtensions == null ? "" : this.localeExtensions.getID());
        fields.put("hashcode", -1);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        String extStr = (String) fields.get("extensions", (Object) "");
        this.baseLocale = BaseLocale.getInstance(convertOldISOCodes((String) fields.get("language", (Object) "")), (String) fields.get("script", (Object) ""), (String) fields.get("country", (Object) ""), (String) fields.get("variant", (Object) ""));
        if (extStr == null || extStr.length() <= 0) {
            this.localeExtensions = null;
            return;
        }
        try {
            InternalLocaleBuilder bldr = new InternalLocaleBuilder();
            bldr.setExtensions(extStr);
            this.localeExtensions = bldr.getLocaleExtensions();
        } catch (LocaleSyntaxException e) {
            throw new IllformedLocaleException(e.getMessage());
        }
    }

    private Object readResolve() throws ObjectStreamException {
        return getInstance(this.baseLocale.getLanguage(), this.baseLocale.getScript(), this.baseLocale.getRegion(), this.baseLocale.getVariant(), this.localeExtensions);
    }

    private static String convertOldISOCodes(String language) {
        String language2 = LocaleUtils.toLowerString(language).intern();
        if (language2 == "he") {
            return "iw";
        }
        if (language2 == "yi") {
            return "ji";
        }
        if (language2 == PolicyInformation.ID) {
            return "in";
        }
        return language2;
    }

    /* access modifiers changed from: private */
    public static LocaleExtensions getCompatibilityExtensions(String language, String script, String country, String variant) {
        if (LocaleUtils.caseIgnoreMatch(language, "ja") && script.length() == 0 && LocaleUtils.caseIgnoreMatch(country, "jp") && "JP".equals(variant)) {
            return LocaleExtensions.CALENDAR_JAPANESE;
        }
        if (!LocaleUtils.caseIgnoreMatch(language, "th") || script.length() != 0 || !LocaleUtils.caseIgnoreMatch(country, "th") || !"TH".equals(variant)) {
            return null;
        }
        return LocaleExtensions.NUMBER_THAI;
    }

    public static String adjustLanguageCode(String languageCode) {
        String adjusted = languageCode.toLowerCase(US);
        if (languageCode.equals("he")) {
            return "iw";
        }
        if (languageCode.equals(PolicyInformation.ID)) {
            return "in";
        }
        if (languageCode.equals("yi")) {
            return "ji";
        }
        return adjusted;
    }

    public static List<Locale> filter(List<LanguageRange> priorityList, Collection<Locale> locales, FilteringMode mode) {
        return LocaleMatcher.filter(priorityList, locales, mode);
    }

    public static List<Locale> filter(List<LanguageRange> priorityList, Collection<Locale> locales) {
        return filter(priorityList, locales, FilteringMode.AUTOSELECT_FILTERING);
    }

    public static List<String> filterTags(List<LanguageRange> priorityList, Collection<String> tags, FilteringMode mode) {
        return LocaleMatcher.filterTags(priorityList, tags, mode);
    }

    public static List<String> filterTags(List<LanguageRange> priorityList, Collection<String> tags) {
        return filterTags(priorityList, tags, FilteringMode.AUTOSELECT_FILTERING);
    }

    public static Locale lookup(List<LanguageRange> priorityList, Collection<Locale> locales) {
        return LocaleMatcher.lookup(priorityList, locales);
    }

    public static String lookupTag(List<LanguageRange> priorityList, Collection<String> tags) {
        return LocaleMatcher.lookupTag(priorityList, tags);
    }
}
