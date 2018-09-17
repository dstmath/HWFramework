package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.text.MessageFormat;
import libcore.icu.ICU;
import sun.security.util.SecurityConstants;
import sun.security.x509.PolicyInformation;
import sun.security.x509.X509CertInfo;
import sun.util.LocaleServiceProviderPool;
import sun.util.locale.BaseLocale;
import sun.util.locale.InternalLocaleBuilder;
import sun.util.locale.LanguageTag;
import sun.util.locale.LocaleExtensions;
import sun.util.locale.LocaleObjectCache;
import sun.util.locale.LocaleSyntaxException;
import sun.util.locale.LocaleUtils;
import sun.util.locale.ParseStatus;
import sun.util.locale.UnicodeLocaleExtension;

public final class Locale implements Cloneable, Serializable {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final /* synthetic */ int[] -java-util-Locale$CategorySwitchesValues = null;
    public static final Locale CANADA = null;
    public static final Locale CANADA_FRENCH = null;
    public static final Locale CHINA = null;
    public static final Locale CHINESE = null;
    private static final int DISPLAY_COUNTRY = 1;
    private static final int DISPLAY_LANGUAGE = 0;
    private static final int DISPLAY_SCRIPT = 3;
    private static final int DISPLAY_VARIANT = 2;
    public static final Locale ENGLISH = null;
    public static final Locale FRANCE = null;
    public static final Locale FRENCH = null;
    public static final Locale GERMAN = null;
    public static final Locale GERMANY = null;
    public static final Locale ITALIAN = null;
    public static final Locale ITALY = null;
    public static final Locale JAPAN = null;
    public static final Locale JAPANESE = null;
    public static final Locale KOREA = null;
    public static final Locale KOREAN = null;
    private static final Cache LOCALECACHE = null;
    public static final Locale PRC = null;
    public static final char PRIVATE_USE_EXTENSION = 'x';
    public static final Locale ROOT = null;
    public static final Locale SIMPLIFIED_CHINESE = null;
    public static final Locale TAIWAN = null;
    public static final Locale TRADITIONAL_CHINESE = null;
    public static final Locale UK = null;
    private static final String UNDETERMINED_LANGUAGE = "und";
    public static final char UNICODE_LOCALE_EXTENSION = 'u';
    public static final Locale US = null;
    private static Locale defaultDisplayLocale = null;
    private static Locale defaultFormatLocale = null;
    private static Locale defaultLocale = null;
    private static volatile String[] isoCountries = null;
    private static volatile String[] isoLanguages = null;
    private static final ObjectStreamField[] serialPersistentFields = null;
    static final long serialVersionUID = 9149081749638150636L;
    private transient BaseLocale baseLocale;
    private volatile transient int hashCodeValue;
    private transient LocaleExtensions localeExtensions;

    public static final class Builder {
        private final InternalLocaleBuilder localeBuilder;

        public Builder() {
            this.localeBuilder = new InternalLocaleBuilder();
        }

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
            if (sts.isError()) {
                throw new IllformedLocaleException(sts.getErrorMessage(), sts.getErrorIndex());
            }
            this.localeBuilder.setLanguageTag(tag);
            return this;
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
            if (attribute == null) {
                throw new NullPointerException("attribute == null");
            }
            try {
                this.localeBuilder.removeUnicodeLocaleAttribute(attribute);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
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

        protected Locale createObject(LocaleKey key) {
            return new Locale(key.base, key.exts, null);
        }
    }

    public enum Category {
        ;
        
        final String countryKey;
        final String languageKey;
        final String scriptKey;
        final String variantKey;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.Locale.Category.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.Locale.Category.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.Locale.Category.<clinit>():void");
        }

        private Category(String languageKey, String scriptKey, String countryKey, String variantKey) {
            this.languageKey = languageKey;
            this.scriptKey = scriptKey;
            this.countryKey = countryKey;
            this.variantKey = variantKey;
        }
    }

    private static final class LocaleKey {
        private final BaseLocale base;
        private final LocaleExtensions exts;
        private final int hash;

        /* synthetic */ LocaleKey(BaseLocale baseLocale, LocaleExtensions extensions, LocaleKey localeKey) {
            this(baseLocale, extensions);
        }

        private LocaleKey(BaseLocale baseLocale, LocaleExtensions extensions) {
            this.base = baseLocale;
            this.exts = extensions;
            int h = this.base.hashCode();
            if (this.exts != null) {
                h ^= this.exts.hashCode();
            }
            this.hash = h;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LocaleKey)) {
                return Locale.-assertionsDisabled;
            }
            LocaleKey other = (LocaleKey) obj;
            if (this.hash != other.hash || !this.base.equals(other.base)) {
                return Locale.-assertionsDisabled;
            }
            if (this.exts != null) {
                return this.exts.equals(other.exts);
            }
            if (other.exts != null) {
                z = Locale.-assertionsDisabled;
            }
            return z;
        }

        public int hashCode() {
            return this.hash;
        }
    }

    private static /* synthetic */ int[] -getjava-util-Locale$CategorySwitchesValues() {
        if (-java-util-Locale$CategorySwitchesValues != null) {
            return -java-util-Locale$CategorySwitchesValues;
        }
        int[] iArr = new int[Category.values().length];
        try {
            iArr[Category.DISPLAY.ordinal()] = DISPLAY_COUNTRY;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Category.FORMAT.ordinal()] = DISPLAY_VARIANT;
        } catch (NoSuchFieldError e2) {
        }
        -java-util-Locale$CategorySwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.Locale.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.Locale.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.Locale.<clinit>():void");
    }

    /* synthetic */ Locale(BaseLocale baseLocale, LocaleExtensions extensions, Locale locale) {
        this(baseLocale, extensions);
    }

    private Locale(BaseLocale baseLocale, LocaleExtensions extensions) {
        this.hashCodeValue = DISPLAY_LANGUAGE;
        this.baseLocale = baseLocale;
        this.localeExtensions = extensions;
    }

    public Locale(String language, String country, String variant) {
        this.hashCodeValue = DISPLAY_LANGUAGE;
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
        return (Locale) LOCALECACHE.get(new LocaleKey(baseloc, extensions, null));
    }

    public static Locale getDefault() {
        if (defaultLocale == null) {
            defaultLocale = initDefault();
        }
        return defaultLocale;
    }

    public static Locale getDefault(Category category) {
        switch (-getjava-util-Locale$CategorySwitchesValues()[category.ordinal()]) {
            case DISPLAY_COUNTRY /*1*/:
                if (defaultDisplayLocale == null) {
                    defaultDisplayLocale = initDefault(category);
                }
                return defaultDisplayLocale;
            case DISPLAY_VARIANT /*2*/:
                if (defaultFormatLocale == null) {
                    defaultFormatLocale = initDefault(category);
                }
                return defaultFormatLocale;
            default:
                if (-assertionsDisabled) {
                    return getDefault();
                }
                throw new AssertionError((Object) "Unknown Category");
        }
    }

    public static Locale initDefault() {
        String languageTag = System.getProperty("user.locale", "");
        if (!languageTag.isEmpty()) {
            return forLanguageTag(languageTag);
        }
        String country;
        String variant;
        String script;
        String language = System.getProperty("user.language", "en");
        String region = System.getProperty("user.region");
        if (region != null) {
            int i = region.indexOf(95);
            if (i >= 0) {
                country = region.substring(DISPLAY_LANGUAGE, i);
                variant = region.substring(i + DISPLAY_COUNTRY);
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
        Locale defaultLocale = getDefault();
        return getInstance(System.getProperty(category.languageKey, defaultLocale.getLanguage()), System.getProperty(category.scriptKey, defaultLocale.getScript()), System.getProperty(category.countryKey, defaultLocale.getCountry()), System.getProperty(category.variantKey, defaultLocale.getVariant()), null);
    }

    public static synchronized void setDefault(Locale newLocale) {
        synchronized (Locale.class) {
            setDefault(Category.DISPLAY, newLocale);
            setDefault(Category.FORMAT, newLocale);
            defaultLocale = newLocale;
            ICU.setDefaultLocale(newLocale.toLanguageTag());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void setDefault(Category category, Locale newLocale) {
        synchronized (Locale.class) {
            if (category != null) {
                if (newLocale != null) {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(new PropertyPermission("user.language", SecurityConstants.PROPERTY_WRITE_ACTION));
                    }
                    switch (-getjava-util-Locale$CategorySwitchesValues()[category.ordinal()]) {
                        case DISPLAY_COUNTRY /*1*/:
                            defaultDisplayLocale = newLocale;
                            break;
                        case DISPLAY_VARIANT /*2*/:
                            defaultFormatLocale = newLocale;
                            break;
                        default:
                            if (!-assertionsDisabled) {
                                throw new AssertionError((Object) "Unknown Category");
                            }
                            break;
                    }
                }
                throw new NullPointerException("Can't set default locale to NULL");
            }
            throw new NullPointerException("Category cannot be NULL");
        }
    }

    public static Locale[] getAvailableLocales() {
        return LocaleServiceProviderPool.getAllAvailableLocales();
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

    public String getExtension(char key) {
        if (!LocaleExtensions.isValidKey(key)) {
            throw new IllegalArgumentException("Ill-formed extension key: " + key);
        } else if (this.localeExtensions == null) {
            return null;
        } else {
            return this.localeExtensions.getExtensionValue(Character.valueOf(key));
        }
    }

    public Set<Character> getExtensionKeys() {
        if (this.localeExtensions == null) {
            return Collections.emptySet();
        }
        return this.localeExtensions.getKeys();
    }

    public Set<String> getUnicodeLocaleAttributes() {
        if (this.localeExtensions == null) {
            return Collections.emptySet();
        }
        return this.localeExtensions.getUnicodeLocaleAttributes();
    }

    public String getUnicodeLocaleType(String key) {
        if (!UnicodeLocaleExtension.isKey(key)) {
            throw new IllegalArgumentException("Ill-formed Unicode locale key: " + key);
        } else if (this.localeExtensions == null) {
            return null;
        } else {
            return this.localeExtensions.getUnicodeLocaleType(key);
        }
    }

    public Set<String> getUnicodeLocaleKeys() {
        if (this.localeExtensions == null) {
            return Collections.emptySet();
        }
        return this.localeExtensions.getUnicodeLocaleKeys();
    }

    BaseLocale getBaseLocale() {
        return this.baseLocale;
    }

    LocaleExtensions getLocaleExtensions() {
        return this.localeExtensions;
    }

    public final String toString() {
        boolean l = this.baseLocale.getLanguage().length() != 0 ? true : -assertionsDisabled;
        boolean s = this.baseLocale.getScript().length() != 0 ? true : -assertionsDisabled;
        boolean r = this.baseLocale.getRegion().length() != 0 ? true : -assertionsDisabled;
        boolean v = this.baseLocale.getVariant().length() != 0 ? true : -assertionsDisabled;
        boolean e = (this.localeExtensions == null || this.localeExtensions.getID().length() == 0) ? -assertionsDisabled : true;
        StringBuilder result = new StringBuilder(this.baseLocale.getLanguage());
        if (r || (l && (v || s || e))) {
            result.append('_').append(this.baseLocale.getRegion());
        }
        if (v && (l || r)) {
            result.append('_').append(this.baseLocale.getVariant());
        }
        if (s && (l || r)) {
            result.append("_#").append(this.baseLocale.getScript());
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
        LanguageTag tag = LanguageTag.parseLocale(this.baseLocale, this.localeExtensions);
        StringBuilder buf = new StringBuilder();
        String subtag = tag.getLanguage();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.canonicalizeLanguage(subtag));
        }
        subtag = tag.getScript();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeScript(subtag));
        }
        subtag = tag.getRegion();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeRegion(subtag));
        }
        for (String s : tag.getVariants()) {
            buf.append(LanguageTag.SEP);
            buf.append(s);
        }
        for (String s2 : tag.getExtensions()) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeExtension(s2));
        }
        subtag = tag.getPrivateuse();
        if (subtag.length() > 0) {
            if (buf.length() > 0) {
                buf.append(LanguageTag.SEP);
            }
            buf.append(LanguageTag.PRIVATEUSE).append(LanguageTag.SEP);
            buf.append(subtag);
        }
        return buf.toString();
    }

    public static Locale forLanguageTag(String languageTag) {
        LanguageTag tag = LanguageTag.parse(languageTag, null);
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
        if (lang.length() == DISPLAY_SCRIPT) {
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
        if (region.length() == DISPLAY_SCRIPT) {
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
        if (UNDETERMINED_LANGUAGE.equals(normalizeAndValidateLanguage(languageCode, -assertionsDisabled))) {
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
        if (isValidBcp47Alpha(lowercaseLanguage, DISPLAY_VARIANT, DISPLAY_SCRIPT)) {
            return lowercaseLanguage;
        }
        if (!strict) {
            return UNDETERMINED_LANGUAGE;
        }
        throw new IllformedLocaleException("Invalid language: " + language);
    }

    private static boolean isAsciiAlphaNum(String string) {
        for (int i = DISPLAY_LANGUAGE; i < string.length(); i += DISPLAY_COUNTRY) {
            char character = string.charAt(i);
            if ((character < 'a' || character > 'z') && ((character < 'A' || character > 'Z') && (character < '0' || character > '9'))) {
                return -assertionsDisabled;
            }
        }
        return true;
    }

    public String getDisplayScript() {
        return getDisplayScript(getDefault());
    }

    public String getDisplayScript(Locale locale) {
        if (this.baseLocale.getScript().isEmpty()) {
            return "";
        }
        String result = ICU.getDisplayScript(this, locale);
        if (result == null) {
            result = ICU.getDisplayScript(this, getDefault());
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
        if (normalizeAndValidateRegion(countryCode, -assertionsDisabled).isEmpty()) {
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
        if (isValidBcp47Alpha(uppercaseRegion, DISPLAY_VARIANT, DISPLAY_VARIANT) || isUnM49AreaCode(uppercaseRegion)) {
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
            return -assertionsDisabled;
        }
        for (int i = DISPLAY_LANGUAGE; i < length; i += DISPLAY_COUNTRY) {
            char character = string.charAt(i);
            if ((character < 'a' || character > 'z') && (character < 'A' || character > 'Z')) {
                return -assertionsDisabled;
            }
        }
        return true;
    }

    private static boolean isUnM49AreaCode(String code) {
        if (code.length() != DISPLAY_SCRIPT) {
            return -assertionsDisabled;
        }
        for (int i = DISPLAY_LANGUAGE; i < DISPLAY_SCRIPT; i += DISPLAY_COUNTRY) {
            char character = code.charAt(i);
            if (character < '0' || character > '9') {
                return -assertionsDisabled;
            }
        }
        return true;
    }

    public final String getDisplayVariant() {
        return getDisplayVariant(getDefault(Category.DISPLAY));
    }

    public String getDisplayVariant(Locale locale) {
        String variantCode = this.baseLocale.getVariant();
        if (variantCode.isEmpty()) {
            return "";
        }
        try {
            normalizeAndValidateVariant(variantCode);
            String result = ICU.getDisplayVariant(this, locale);
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
        int i = DISPLAY_LANGUAGE;
        int length = subTags.length;
        while (i < length) {
            if (isValidVariantSubtag(subTags[i])) {
                i += DISPLAY_COUNTRY;
            } else {
                throw new IllformedLocaleException("Invalid variant: " + variant);
            }
        }
        return normalizedVariant;
    }

    private static boolean isValidVariantSubtag(String subTag) {
        if (subTag.length() < 5 || subTag.length() > 8) {
            if (subTag.length() == 4) {
                char firstChar = subTag.charAt(DISPLAY_LANGUAGE);
                return (firstChar < '0' || firstChar > '9' || !isAsciiAlphaNum(subTag)) ? -assertionsDisabled : true;
            }
        } else if (isAsciiAlphaNum(subTag)) {
            return true;
        }
    }

    public final String getDisplayName() {
        return getDisplayName(getDefault(Category.DISPLAY));
    }

    public String getDisplayName(Locale locale) {
        int count = DISPLAY_LANGUAGE;
        StringBuilder buffer = new StringBuilder();
        String languageCode = this.baseLocale.getLanguage();
        if (!languageCode.isEmpty()) {
            String displayLanguage = getDisplayLanguage(locale);
            if (!displayLanguage.isEmpty()) {
                languageCode = displayLanguage;
            }
            buffer.append(languageCode);
            count = DISPLAY_COUNTRY;
        }
        String scriptCode = this.baseLocale.getScript();
        if (!scriptCode.isEmpty()) {
            if (count == DISPLAY_COUNTRY) {
                buffer.append(" (");
            }
            String displayScript = getDisplayScript(locale);
            if (!displayScript.isEmpty()) {
                scriptCode = displayScript;
            }
            buffer.append(scriptCode);
            count += DISPLAY_COUNTRY;
        }
        String countryCode = this.baseLocale.getRegion();
        if (!countryCode.isEmpty()) {
            if (count == DISPLAY_COUNTRY) {
                buffer.append(" (");
            } else if (count == DISPLAY_VARIANT) {
                buffer.append(",");
            }
            String displayCountry = getDisplayCountry(locale);
            if (!displayCountry.isEmpty()) {
                countryCode = displayCountry;
            }
            buffer.append(countryCode);
            count += DISPLAY_COUNTRY;
        }
        String variantCode = this.baseLocale.getVariant();
        if (!variantCode.isEmpty()) {
            if (count == DISPLAY_COUNTRY) {
                buffer.append(" (");
            } else if (count == DISPLAY_VARIANT || count == DISPLAY_SCRIPT) {
                buffer.append(",");
            }
            String displayVariant = getDisplayVariant(locale);
            if (!displayVariant.isEmpty()) {
                variantCode = displayVariant;
            }
            buffer.append(variantCode);
            count += DISPLAY_COUNTRY;
        }
        if (count > DISPLAY_COUNTRY) {
            buffer.append(")");
        }
        return buffer.toString();
    }

    public Object clone() {
        try {
            return (Locale) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
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
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Locale)) {
            return -assertionsDisabled;
        }
        if (!this.baseLocale.equals(((Locale) obj).baseLocale)) {
            return -assertionsDisabled;
        }
        if (this.localeExtensions != null) {
            return this.localeExtensions.equals(((Locale) obj).localeExtensions);
        }
        return ((Locale) obj).localeExtensions == null ? true : -assertionsDisabled;
    }

    private static String formatList(String[] stringList, String listPattern, String listCompositionPattern) {
        if (listPattern == null || listCompositionPattern == null) {
            StringBuffer result = new StringBuffer();
            for (int i = DISPLAY_LANGUAGE; i < stringList.length; i += DISPLAY_COUNTRY) {
                if (i > 0) {
                    result.append(',');
                }
                result.append(stringList[i]);
            }
            return result.toString();
        }
        Object stringList2;
        if (stringList.length > DISPLAY_SCRIPT) {
            stringList2 = composeList(new MessageFormat(listCompositionPattern), stringList);
        }
        Object args = new Object[(stringList2.length + DISPLAY_COUNTRY)];
        System.arraycopy(stringList2, (int) DISPLAY_LANGUAGE, args, (int) DISPLAY_COUNTRY, stringList2.length);
        args[DISPLAY_LANGUAGE] = new Integer(stringList2.length);
        return new MessageFormat(listPattern).format(args);
    }

    private static String[] composeList(MessageFormat format, String[] list) {
        if (list.length <= DISPLAY_SCRIPT) {
            return list;
        }
        String[] listItems = new String[DISPLAY_VARIANT];
        listItems[DISPLAY_LANGUAGE] = list[DISPLAY_LANGUAGE];
        listItems[DISPLAY_COUNTRY] = list[DISPLAY_COUNTRY];
        String newItem = format.format(listItems);
        Object newList = new String[(list.length - 1)];
        System.arraycopy((Object) list, (int) DISPLAY_VARIANT, newList, (int) DISPLAY_COUNTRY, newList.length - 1);
        newList[DISPLAY_LANGUAGE] = newItem;
        return composeList(format, newList);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        PutField fields = out.putFields();
        fields.put("language", this.baseLocale.getLanguage());
        fields.put("script", this.baseLocale.getScript());
        fields.put("country", this.baseLocale.getRegion());
        fields.put("variant", this.baseLocale.getVariant());
        fields.put(X509CertInfo.EXTENSIONS, this.localeExtensions == null ? "" : this.localeExtensions.getID());
        fields.put("hashcode", -1);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        GetField fields = in.readFields();
        String script = (String) fields.get("script", (Object) "");
        String country = (String) fields.get("country", (Object) "");
        String variant = (String) fields.get("variant", (Object) "");
        String extStr = (String) fields.get(X509CertInfo.EXTENSIONS, (Object) "");
        this.baseLocale = BaseLocale.getInstance(convertOldISOCodes((String) fields.get("language", (Object) "")), script, country, variant);
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
        language = LocaleUtils.toLowerString(language).intern();
        if (language == "he") {
            return "iw";
        }
        if (language == "yi") {
            return "ji";
        }
        if (language == PolicyInformation.ID) {
            return "in";
        }
        return language;
    }

    private static LocaleExtensions getCompatibilityExtensions(String language, String script, String country, String variant) {
        if (LocaleUtils.caseIgnoreMatch(language, "ja") && script.length() == 0 && LocaleUtils.caseIgnoreMatch(country, "jp") && "JP".equals(variant)) {
            return LocaleExtensions.CALENDAR_JAPANESE;
        }
        if (LocaleUtils.caseIgnoreMatch(language, "th") && script.length() == 0 && LocaleUtils.caseIgnoreMatch(country, "th") && "TH".equals(variant)) {
            return LocaleExtensions.NUMBER_THAI;
        }
        return null;
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
}
