package android.icu.util;

import android.icu.impl.CacheBase;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUResourceTableAccess;
import android.icu.impl.LocaleIDParser;
import android.icu.impl.LocaleIDs;
import android.icu.impl.LocaleUtility;
import android.icu.impl.SoftCache;
import android.icu.impl.locale.AsciiUtil;
import android.icu.impl.locale.BaseLocale;
import android.icu.impl.locale.Extension;
import android.icu.impl.locale.InternalLocaleBuilder;
import android.icu.impl.locale.KeyTypeData;
import android.icu.impl.locale.LanguageTag;
import android.icu.impl.locale.LocaleExtensions;
import android.icu.impl.locale.LocaleSyntaxException;
import android.icu.impl.locale.ParseStatus;
import android.icu.impl.locale.UnicodeLocaleExtension;
import android.icu.lang.UScript;
import android.icu.text.DateFormat;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.LocaleDisplayNames.DialectHandling;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class ULocale implements Serializable, Comparable<ULocale> {
    public static Type ACTUAL_LOCALE = new Type();
    private static final SoftCache<Locale, ULocale, Void> CACHE = new SoftCache<Locale, ULocale, Void>() {
        protected ULocale createInstance(Locale key, Void unused) {
            return JDKLocaleHelper.toULocale(key);
        }
    };
    public static final ULocale CANADA = new ULocale("en_CA", Locale.CANADA);
    public static final ULocale CANADA_FRENCH = new ULocale("fr_CA", Locale.CANADA_FRENCH);
    private static String[][] CANONICALIZE_MAP = null;
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
    public static final char UNICODE_LOCALE_EXTENSION = 'u';
    public static final ULocale US = new ULocale("en_US", Locale.US);
    public static Type VALID_LOCALE = new Type();
    private static Locale[] defaultCategoryLocales = new Locale[Category.values().length];
    private static ULocale[] defaultCategoryULocales = new ULocale[Category.values().length];
    private static Locale defaultLocale = Locale.getDefault();
    private static ULocale defaultULocale = null;
    private static CacheBase<String, String, Void> nameCache = new SoftCache<String, String, Void>() {
        protected String createInstance(String tmpLocaleID, Void unused) {
            return new LocaleIDParser(tmpLocaleID).getName();
        }
    };
    private static final long serialVersionUID = 3715177670352309217L;
    private static String[][] variantsToKeywords;
    private volatile transient BaseLocale baseLocale;
    private volatile transient LocaleExtensions extensions;
    private volatile transient Locale locale;
    private String localeID;

    /* renamed from: android.icu.util.ULocale$1ULocaleAcceptLanguageQ */
    class AnonymousClass1ULocaleAcceptLanguageQ implements Comparable<AnonymousClass1ULocaleAcceptLanguageQ> {
        private double q;
        private double serial;

        public AnonymousClass1ULocaleAcceptLanguageQ(double theq, int theserial) {
            this.q = theq;
            this.serial = (double) theserial;
        }

        public int compareTo(AnonymousClass1ULocaleAcceptLanguageQ other) {
            if (this.q > other.q) {
                return -1;
            }
            if (this.q < other.q) {
                return 1;
            }
            if (this.serial < other.serial) {
                return -1;
            }
            if (this.serial > other.serial) {
                return 1;
            }
            return 0;
        }
    }

    public static final class Builder {
        private final InternalLocaleBuilder _locbld = new InternalLocaleBuilder();

        public Builder setLocale(ULocale locale) {
            try {
                this._locbld.setLocale(locale.base(), locale.extensions());
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
            this._locbld.setLanguageTag(tag);
            return this;
        }

        public Builder setLanguage(String language) {
            try {
                this._locbld.setLanguage(language);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setScript(String script) {
            try {
                this._locbld.setScript(script);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setRegion(String region) {
            try {
                this._locbld.setRegion(region);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setVariant(String variant) {
            try {
                this._locbld.setVariant(variant);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setExtension(char key, String value) {
            try {
                this._locbld.setExtension(key, value);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder setUnicodeLocaleKeyword(String key, String type) {
            try {
                this._locbld.setUnicodeLocaleKeyword(key, type);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder addUnicodeLocaleAttribute(String attribute) {
            try {
                this._locbld.addUnicodeLocaleAttribute(attribute);
                return this;
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
        }

        public Builder removeUnicodeLocaleAttribute(String attribute) {
            try {
                this._locbld.removeUnicodeLocaleAttribute(attribute);
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

    public enum Category {
        DISPLAY,
        FORMAT
    }

    private static final class JDKLocaleHelper {
        private static final /* synthetic */ int[] -android-icu-util-ULocale$CategorySwitchesValues = null;
        private static final String[][] JAVA6_MAPDATA;
        private static Object eDISPLAY;
        private static Object eFORMAT;
        private static boolean hasLocaleCategories;
        private static boolean hasScriptsAndUnicodeExtensions;
        private static Method mForLanguageTag;
        private static Method mGetDefault;
        private static Method mGetExtension;
        private static Method mGetExtensionKeys;
        private static Method mGetScript;
        private static Method mGetUnicodeLocaleAttributes;
        private static Method mGetUnicodeLocaleKeys;
        private static Method mGetUnicodeLocaleType;
        private static Method mSetDefault;

        private static /* synthetic */ int[] -getandroid-icu-util-ULocale$CategorySwitchesValues() {
            if (-android-icu-util-ULocale$CategorySwitchesValues != null) {
                return -android-icu-util-ULocale$CategorySwitchesValues;
            }
            int[] iArr = new int[Category.values().length];
            try {
                iArr[Category.DISPLAY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Category.FORMAT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            -android-icu-util-ULocale$CategorySwitchesValues = iArr;
            return iArr;
        }

        static {
            hasScriptsAndUnicodeExtensions = false;
            hasLocaleCategories = false;
            r12 = new String[3][];
            r12[0] = new String[]{"ja_JP_JP", "ja_JP", "calendar", "japanese", "ja"};
            r12[1] = new String[]{"no_NO_NY", "nn_NO", null, null, "nn"};
            r12[2] = new String[]{"th_TH_TH", "th_TH", "numbers", "thai", "th"};
            JAVA6_MAPDATA = r12;
            try {
                mGetScript = Locale.class.getMethod("getScript", (Class[]) null);
                mGetExtensionKeys = Locale.class.getMethod("getExtensionKeys", (Class[]) null);
                mGetExtension = Locale.class.getMethod("getExtension", new Class[]{Character.TYPE});
                mGetUnicodeLocaleKeys = Locale.class.getMethod("getUnicodeLocaleKeys", (Class[]) null);
                mGetUnicodeLocaleAttributes = Locale.class.getMethod("getUnicodeLocaleAttributes", (Class[]) null);
                mGetUnicodeLocaleType = Locale.class.getMethod("getUnicodeLocaleType", new Class[]{String.class});
                mForLanguageTag = Locale.class.getMethod("forLanguageTag", new Class[]{String.class});
                hasScriptsAndUnicodeExtensions = true;
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e2) {
            } catch (SecurityException e3) {
            }
            Class cCategory = null;
            try {
                for (Class<?> c : Locale.class.getDeclaredClasses()) {
                    if (c.getName().equals("java.util.Locale$Category")) {
                        cCategory = c;
                        break;
                    }
                }
                if (cCategory != null) {
                    mGetDefault = Locale.class.getDeclaredMethod("getDefault", new Class[]{cCategory});
                    mSetDefault = Locale.class.getDeclaredMethod("setDefault", new Class[]{cCategory, Locale.class});
                    Method mName = cCategory.getMethod("name", (Class[]) null);
                    for (Object e4 : cCategory.getEnumConstants()) {
                        String catVal = (String) mName.invoke(e4, (Object[]) null);
                        if (catVal.equals("DISPLAY")) {
                            eDISPLAY = e4;
                        } else if (catVal.equals("FORMAT")) {
                            eFORMAT = e4;
                        }
                    }
                    if (eDISPLAY != null && eFORMAT != null) {
                        hasLocaleCategories = true;
                    }
                }
            } catch (NoSuchMethodException e5) {
            } catch (IllegalArgumentException e6) {
            } catch (IllegalAccessException e7) {
            } catch (InvocationTargetException e8) {
            } catch (SecurityException e9) {
            }
        }

        private JDKLocaleHelper() {
        }

        public static boolean hasLocaleCategories() {
            return hasLocaleCategories;
        }

        public static ULocale toULocale(Locale loc) {
            return hasScriptsAndUnicodeExtensions ? toULocale7(loc) : toULocale6(loc);
        }

        public static Locale toLocale(ULocale uloc) {
            return hasScriptsAndUnicodeExtensions ? toLocale7(uloc) : toLocale6(uloc);
        }

        private static ULocale toULocale7(Locale loc) {
            IllegalAccessException e;
            InvocationTargetException e2;
            String language = loc.getLanguage();
            String script = "";
            String country = loc.getCountry();
            String variant = loc.getVariant();
            Iterable attributes = null;
            Map keywords = null;
            try {
                String kwVal;
                String kwKey;
                script = (String) mGetScript.invoke(loc, (Object[]) null);
                Set<Character> extKeys = (Set) mGetExtensionKeys.invoke(loc, (Object[]) null);
                if (!extKeys.isEmpty()) {
                    Iterator extKey$iterator = extKeys.iterator();
                    while (true) {
                        Map<String, String> keywords2;
                        Map<String, String> keywords3;
                        Set<String> attributes2;
                        Set<String> attributes3;
                        try {
                            keywords2 = keywords3;
                            attributes2 = attributes3;
                            if (!extKey$iterator.hasNext()) {
                                keywords = keywords2;
                                attributes = attributes2;
                                break;
                            }
                            Character extKey = (Character) extKey$iterator.next();
                            if (extKey.charValue() == 'u') {
                                Set<String> uAttributes = (Set) mGetUnicodeLocaleAttributes.invoke(loc, (Object[]) null);
                                if (uAttributes.isEmpty()) {
                                    attributes3 = attributes2;
                                } else {
                                    attributes3 = new TreeSet();
                                    try {
                                        for (String attr : uAttributes) {
                                            attributes3.add(attr);
                                        }
                                    } catch (IllegalAccessException e3) {
                                        e = e3;
                                        keywords3 = keywords2;
                                        throw new RuntimeException(e);
                                    } catch (InvocationTargetException e4) {
                                        e2 = e4;
                                        keywords3 = keywords2;
                                        throw new RuntimeException(e2);
                                    }
                                }
                                for (String kwKey2 : (Set) mGetUnicodeLocaleKeys.invoke(loc, (Object[]) null)) {
                                    kwVal = (String) mGetUnicodeLocaleType.invoke(loc, new Object[]{kwKey2});
                                    if (kwVal == null) {
                                        keywords3 = keywords2;
                                    } else if (!kwKey2.equals("va")) {
                                        if (keywords2 == null) {
                                            keywords3 = new TreeMap();
                                        } else {
                                            keywords3 = keywords2;
                                        }
                                        keywords3.put(kwKey2, kwVal);
                                    } else if (variant.length() == 0) {
                                        variant = kwVal;
                                        keywords3 = keywords2;
                                    } else {
                                        variant = kwVal + BaseLocale.SEP + variant;
                                        keywords3 = keywords2;
                                    }
                                    keywords2 = keywords3;
                                }
                                keywords3 = keywords2;
                            } else {
                                String extVal = (String) mGetExtension.invoke(loc, new Object[]{extKey});
                                if (extVal != null) {
                                    if (keywords2 == null) {
                                        keywords3 = new TreeMap();
                                    } else {
                                        keywords3 = keywords2;
                                    }
                                    try {
                                        keywords3.put(String.valueOf(extKey), extVal);
                                        attributes3 = attributes2;
                                    } catch (IllegalAccessException e5) {
                                        e = e5;
                                        attributes3 = attributes2;
                                    } catch (InvocationTargetException e6) {
                                        e2 = e6;
                                        attributes3 = attributes2;
                                    }
                                } else {
                                    keywords3 = keywords2;
                                    attributes3 = attributes2;
                                }
                            }
                        } catch (IllegalAccessException e7) {
                            e = e7;
                            keywords3 = keywords2;
                            attributes3 = attributes2;
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e8) {
                            e2 = e8;
                            keywords3 = keywords2;
                            attributes3 = attributes2;
                            throw new RuntimeException(e2);
                        }
                    }
                }
                if (language.equals("no") && country.equals("NO") && variant.equals("NY")) {
                    language = "nn";
                    variant = "";
                }
                StringBuilder buf = new StringBuilder(language);
                if (script.length() > 0) {
                    buf.append(ULocale.UNDERSCORE);
                    buf.append(script);
                }
                if (country.length() > 0) {
                    buf.append(ULocale.UNDERSCORE);
                    buf.append(country);
                }
                if (variant.length() > 0) {
                    if (country.length() == 0) {
                        buf.append(ULocale.UNDERSCORE);
                    }
                    buf.append(ULocale.UNDERSCORE);
                    buf.append(variant);
                }
                if (attributes != null) {
                    StringBuilder attrBuf = new StringBuilder();
                    for (String attr2 : attributes) {
                        if (attrBuf.length() != 0) {
                            attrBuf.append('-');
                        }
                        attrBuf.append(attr2);
                    }
                    if (keywords == null) {
                        keywords = new TreeMap();
                    }
                    keywords.put(ULocale.LOCALE_ATTRIBUTE_KEY, attrBuf.toString());
                }
                if (keywords != null) {
                    buf.append('@');
                    boolean addSep = false;
                    for (Entry<String, String> kwEntry : keywords.entrySet()) {
                        kwKey2 = (String) kwEntry.getKey();
                        kwVal = (String) kwEntry.getValue();
                        if (kwKey2.length() != 1) {
                            kwKey2 = ULocale.toLegacyKey(kwKey2);
                            if (kwVal.length() == 0) {
                                kwVal = "yes";
                            }
                            kwVal = ULocale.toLegacyType(kwKey2, kwVal);
                        }
                        if (addSep) {
                            buf.append(';');
                        } else {
                            addSep = true;
                        }
                        buf.append(kwKey2);
                        buf.append('=');
                        buf.append(kwVal);
                    }
                }
                return new ULocale(ULocale.getName(buf.toString()), loc, null);
            } catch (IllegalAccessException e9) {
                e = e9;
            } catch (InvocationTargetException e10) {
                e2 = e10;
            }
        }

        private static ULocale toULocale6(Locale loc) {
            String locStr = loc.toString();
            if (locStr.length() == 0) {
                return ULocale.ROOT;
            }
            for (int i = 0; i < JAVA6_MAPDATA.length; i++) {
                if (JAVA6_MAPDATA[i][0].equals(locStr)) {
                    LocaleIDParser p = new LocaleIDParser(JAVA6_MAPDATA[i][1]);
                    p.setKeywordValue(JAVA6_MAPDATA[i][2], JAVA6_MAPDATA[i][3]);
                    locStr = p.getName();
                    break;
                }
            }
            return new ULocale(ULocale.getName(locStr), loc, null);
        }

        private static Locale toLocale7(ULocale uloc) {
            Locale locale = null;
            String ulocStr = uloc.getName();
            if (uloc.getScript().length() > 0 || ulocStr.contains("@")) {
                String tag = AsciiUtil.toUpperString(uloc.toLanguageTag());
                try {
                    locale = (Locale) mForLanguageTag.invoke(null, new Object[]{tag});
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e2) {
                    throw new RuntimeException(e2);
                }
            }
            if (locale == null) {
                return new Locale(uloc.getLanguage(), uloc.getCountry(), uloc.getVariant());
            }
            return locale;
        }

        private static Locale toLocale6(ULocale uloc) {
            String locstr = uloc.getBaseName();
            int i = 0;
            while (i < JAVA6_MAPDATA.length) {
                if (locstr.equals(JAVA6_MAPDATA[i][1]) || locstr.equals(JAVA6_MAPDATA[i][4])) {
                    if (JAVA6_MAPDATA[i][2] == null) {
                        locstr = JAVA6_MAPDATA[i][0];
                        break;
                    }
                    String val = uloc.getKeywordValue(JAVA6_MAPDATA[i][2]);
                    if (val != null && val.equals(JAVA6_MAPDATA[i][3])) {
                        locstr = JAVA6_MAPDATA[i][0];
                        break;
                    }
                }
                i++;
            }
            String[] names = new LocaleIDParser(locstr).getLanguageScriptCountryVariant();
            return new Locale(names[0], names[2], names[3]);
        }

        public static Locale getDefault(Category category) {
            Locale loc = Locale.getDefault();
            if (!hasLocaleCategories) {
                return loc;
            }
            Object cat = null;
            switch (-getandroid-icu-util-ULocale$CategorySwitchesValues()[category.ordinal()]) {
                case 1:
                    cat = eDISPLAY;
                    break;
                case 2:
                    cat = eFORMAT;
                    break;
            }
            if (cat == null) {
                return loc;
            }
            try {
                return (Locale) mGetDefault.invoke(null, new Object[]{cat});
            } catch (InvocationTargetException e) {
                return loc;
            } catch (IllegalArgumentException e2) {
                return loc;
            } catch (IllegalAccessException e3) {
                return loc;
            }
        }

        public static void setDefault(Category category, Locale newLocale) {
            if (hasLocaleCategories) {
                Object cat = null;
                switch (-getandroid-icu-util-ULocale$CategorySwitchesValues()[category.ordinal()]) {
                    case 1:
                        cat = eDISPLAY;
                        break;
                    case 2:
                        cat = eFORMAT;
                        break;
                }
                if (cat != null) {
                    try {
                        mSetDefault.invoke(null, new Object[]{cat, newLocale});
                    } catch (InvocationTargetException e) {
                    } catch (IllegalArgumentException e2) {
                    } catch (IllegalAccessException e3) {
                    }
                }
            }
        }

        public static boolean isOriginalDefaultLocale(Locale loc) {
            boolean z = false;
            if (hasScriptsAndUnicodeExtensions) {
                String script = "";
                try {
                    boolean equals;
                    script = (String) mGetScript.invoke(loc, (Object[]) null);
                    if (loc.getLanguage().equals(getSystemProperty("user.language")) && loc.getCountry().equals(getSystemProperty("user.country")) && loc.getVariant().equals(getSystemProperty("user.variant"))) {
                        equals = script.equals(getSystemProperty("user.script"));
                    } else {
                        equals = false;
                    }
                    return equals;
                } catch (Exception e) {
                    return false;
                }
            }
            if (loc.getLanguage().equals(getSystemProperty("user.language")) && loc.getCountry().equals(getSystemProperty("user.country"))) {
                z = loc.getVariant().equals(getSystemProperty("user.variant"));
            }
            return z;
        }

        public static String getSystemProperty(final String key) {
            String val = null;
            String fkey = key;
            if (System.getSecurityManager() == null) {
                return System.getProperty(key);
            }
            try {
                return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty(key);
                    }
                });
            } catch (AccessControlException e) {
                return val;
            }
        }
    }

    @Deprecated
    public enum Minimize {
        FAVOR_SCRIPT,
        FAVOR_REGION
    }

    public static final class Type {
        /* synthetic */ Type(Type -this0) {
            this();
        }

        private Type() {
        }
    }

    static {
        int i = 0;
        defaultULocale = forLocale(defaultLocale);
        Category[] values;
        int length;
        int idx;
        if (JDKLocaleHelper.hasLocaleCategories()) {
            values = Category.values();
            length = values.length;
            while (i < length) {
                Category cat = values[i];
                idx = cat.ordinal();
                defaultCategoryLocales[idx] = JDKLocaleHelper.getDefault(cat);
                defaultCategoryULocales[idx] = forLocale(defaultCategoryLocales[idx]);
                i++;
            }
        } else {
            if (JDKLocaleHelper.isOriginalDefaultLocale(defaultLocale)) {
                String userScript = JDKLocaleHelper.getSystemProperty("user.script");
                if (userScript != null && LanguageTag.isScript(userScript)) {
                    BaseLocale base = defaultULocale.base();
                    defaultULocale = getInstance(BaseLocale.getInstance(base.getLanguage(), userScript, base.getRegion(), base.getVariant()), defaultULocale.extensions());
                }
            }
            values = Category.values();
            length = values.length;
            while (i < length) {
                idx = values[i].ordinal();
                defaultCategoryLocales[idx] = defaultLocale;
                defaultCategoryULocales[idx] = defaultULocale;
                i++;
            }
        }
    }

    private static void initCANONICALIZE_MAP() {
        if (CANONICALIZE_MAP == null) {
            tempCANONICALIZE_MAP = new String[50][];
            tempCANONICALIZE_MAP[0] = new String[]{"C", "en_US_POSIX", null, null};
            tempCANONICALIZE_MAP[1] = new String[]{"art_LOJBAN", "jbo", null, null};
            tempCANONICALIZE_MAP[2] = new String[]{"az_AZ_CYRL", "az_Cyrl_AZ", null, null};
            tempCANONICALIZE_MAP[3] = new String[]{"az_AZ_LATN", "az_Latn_AZ", null, null};
            tempCANONICALIZE_MAP[4] = new String[]{"ca_ES_PREEURO", "ca_ES", "currency", "ESP"};
            tempCANONICALIZE_MAP[5] = new String[]{"cel_GAULISH", "cel__GAULISH", null, null};
            tempCANONICALIZE_MAP[6] = new String[]{"de_1901", "de__1901", null, null};
            tempCANONICALIZE_MAP[7] = new String[]{"de_1906", "de__1906", null, null};
            tempCANONICALIZE_MAP[8] = new String[]{"de__PHONEBOOK", "de", "collation", "phonebook"};
            tempCANONICALIZE_MAP[9] = new String[]{"de_AT_PREEURO", "de_AT", "currency", "ATS"};
            tempCANONICALIZE_MAP[10] = new String[]{"de_DE_PREEURO", "de_DE", "currency", "DEM"};
            tempCANONICALIZE_MAP[11] = new String[]{"de_LU_PREEURO", "de_LU", "currency", "EUR"};
            tempCANONICALIZE_MAP[12] = new String[]{"el_GR_PREEURO", "el_GR", "currency", "GRD"};
            tempCANONICALIZE_MAP[13] = new String[]{"en_BOONT", "en__BOONT", null, null};
            tempCANONICALIZE_MAP[14] = new String[]{"en_SCOUSE", "en__SCOUSE", null, null};
            tempCANONICALIZE_MAP[15] = new String[]{"en_BE_PREEURO", "en_BE", "currency", "BEF"};
            tempCANONICALIZE_MAP[16] = new String[]{"en_IE_PREEURO", "en_IE", "currency", "IEP"};
            tempCANONICALIZE_MAP[17] = new String[]{"es__TRADITIONAL", "es", "collation", "traditional"};
            tempCANONICALIZE_MAP[18] = new String[]{"es_ES_PREEURO", "es_ES", "currency", "ESP"};
            tempCANONICALIZE_MAP[19] = new String[]{"eu_ES_PREEURO", "eu_ES", "currency", "ESP"};
            tempCANONICALIZE_MAP[20] = new String[]{"fi_FI_PREEURO", "fi_FI", "currency", "FIM"};
            tempCANONICALIZE_MAP[21] = new String[]{"fr_BE_PREEURO", "fr_BE", "currency", "BEF"};
            tempCANONICALIZE_MAP[22] = new String[]{"fr_FR_PREEURO", "fr_FR", "currency", "FRF"};
            tempCANONICALIZE_MAP[23] = new String[]{"fr_LU_PREEURO", "fr_LU", "currency", "LUF"};
            tempCANONICALIZE_MAP[24] = new String[]{"ga_IE_PREEURO", "ga_IE", "currency", "IEP"};
            tempCANONICALIZE_MAP[25] = new String[]{"gl_ES_PREEURO", "gl_ES", "currency", "ESP"};
            tempCANONICALIZE_MAP[26] = new String[]{"hi__DIRECT", "hi", "collation", "direct"};
            tempCANONICALIZE_MAP[27] = new String[]{"it_IT_PREEURO", "it_IT", "currency", "ITL"};
            tempCANONICALIZE_MAP[28] = new String[]{"ja_JP_TRADITIONAL", "ja_JP", "calendar", "japanese"};
            tempCANONICALIZE_MAP[29] = new String[]{"nl_BE_PREEURO", "nl_BE", "currency", "BEF"};
            tempCANONICALIZE_MAP[30] = new String[]{"nl_NL_PREEURO", "nl_NL", "currency", "NLG"};
            tempCANONICALIZE_MAP[31] = new String[]{"pt_PT_PREEURO", "pt_PT", "currency", "PTE"};
            tempCANONICALIZE_MAP[32] = new String[]{"sl_ROZAJ", "sl__ROZAJ", null, null};
            tempCANONICALIZE_MAP[33] = new String[]{"sr_SP_CYRL", "sr_Cyrl_RS", null, null};
            tempCANONICALIZE_MAP[34] = new String[]{"sr_SP_LATN", "sr_Latn_RS", null, null};
            tempCANONICALIZE_MAP[35] = new String[]{"sr_YU_CYRILLIC", "sr_Cyrl_RS", null, null};
            tempCANONICALIZE_MAP[36] = new String[]{"th_TH_TRADITIONAL", "th_TH", "calendar", "buddhist"};
            tempCANONICALIZE_MAP[37] = new String[]{"uz_UZ_CYRILLIC", "uz_Cyrl_UZ", null, null};
            tempCANONICALIZE_MAP[38] = new String[]{"uz_UZ_CYRL", "uz_Cyrl_UZ", null, null};
            tempCANONICALIZE_MAP[39] = new String[]{"uz_UZ_LATN", "uz_Latn_UZ", null, null};
            tempCANONICALIZE_MAP[40] = new String[]{"zh_CHS", "zh_Hans", null, null};
            tempCANONICALIZE_MAP[41] = new String[]{"zh_CHT", "zh_Hant", null, null};
            tempCANONICALIZE_MAP[42] = new String[]{"zh_GAN", "zh__GAN", null, null};
            tempCANONICALIZE_MAP[43] = new String[]{"zh_GUOYU", "zh", null, null};
            tempCANONICALIZE_MAP[44] = new String[]{"zh_HAKKA", "zh__HAKKA", null, null};
            tempCANONICALIZE_MAP[45] = new String[]{"zh_MIN", "zh__MIN", null, null};
            tempCANONICALIZE_MAP[46] = new String[]{"zh_MIN_NAN", "zh__MINNAN", null, null};
            tempCANONICALIZE_MAP[47] = new String[]{"zh_WUU", "zh__WUU", null, null};
            tempCANONICALIZE_MAP[48] = new String[]{"zh_XIANG", "zh__XIANG", null, null};
            tempCANONICALIZE_MAP[49] = new String[]{"zh_YUE", "zh__YUE", null, null};
            synchronized (ULocale.class) {
                if (CANONICALIZE_MAP == null) {
                    CANONICALIZE_MAP = tempCANONICALIZE_MAP;
                }
            }
        }
        if (variantsToKeywords == null) {
            tempVariantsToKeywords = new String[3][];
            tempVariantsToKeywords[0] = new String[]{"EURO", "currency", "EUR"};
            tempVariantsToKeywords[1] = new String[]{"PINYIN", "collation", "pinyin"};
            tempVariantsToKeywords[2] = new String[]{"STROKE", "collation", "stroke"};
            synchronized (ULocale.class) {
                if (variantsToKeywords == null) {
                    variantsToKeywords = tempVariantsToKeywords;
                }
            }
        }
    }

    private ULocale(String localeID, Locale locale) {
        this.localeID = localeID;
        this.locale = locale;
    }

    private ULocale(Locale loc) {
        this.localeID = getName(forLocale(loc).toString());
        this.locale = loc;
    }

    public static ULocale forLocale(Locale loc) {
        if (loc == null) {
            return null;
        }
        return (ULocale) CACHE.getInstance(loc, null);
    }

    public ULocale(String localeID) {
        this.localeID = getName(localeID);
    }

    public ULocale(String a, String b) {
        this(a, b, null);
    }

    public ULocale(String a, String b, String c) {
        this.localeID = getName(lscvToID(a, b, c, ""));
    }

    public static ULocale createCanonical(String nonCanonicalID) {
        return new ULocale(canonicalize(nonCanonicalID), (Locale) null);
    }

    private static String lscvToID(String lang, String script, String country, String variant) {
        StringBuilder buf = new StringBuilder();
        if (lang != null && lang.length() > 0) {
            buf.append(lang);
        }
        if (script != null && script.length() > 0) {
            buf.append(UNDERSCORE);
            buf.append(script);
        }
        if (country != null && country.length() > 0) {
            buf.append(UNDERSCORE);
            buf.append(country);
        }
        if (variant != null && variant.length() > 0) {
            if (country == null || country.length() == 0) {
                buf.append(UNDERSCORE);
            }
            buf.append(UNDERSCORE);
            buf.append(variant);
        }
        return buf.toString();
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
            Locale currentDefault = Locale.getDefault();
            if (!defaultLocale.equals(currentDefault)) {
                defaultLocale = currentDefault;
                defaultULocale = forLocale(currentDefault);
                if (!JDKLocaleHelper.hasLocaleCategories()) {
                    for (Category cat : Category.values()) {
                        int idx = cat.ordinal();
                        defaultCategoryLocales[idx] = currentDefault;
                        defaultCategoryULocales[idx] = forLocale(currentDefault);
                    }
                }
            }
            return defaultULocale;
        }
    }

    public static synchronized void setDefault(ULocale newLocale) {
        synchronized (ULocale.class) {
            defaultLocale = newLocale.toLocale();
            Locale.setDefault(defaultLocale);
            defaultULocale = newLocale;
            for (Category cat : Category.values()) {
                setDefault(cat, newLocale);
            }
        }
    }

    public static ULocale getDefault(Category category) {
        synchronized (ULocale.class) {
            int idx = category.ordinal();
            if (defaultCategoryULocales[idx] == null) {
                return ROOT;
            }
            if (JDKLocaleHelper.hasLocaleCategories()) {
                Locale currentCategoryDefault = JDKLocaleHelper.getDefault(category);
                if (!defaultCategoryLocales[idx].equals(currentCategoryDefault)) {
                    defaultCategoryLocales[idx] = currentCategoryDefault;
                    defaultCategoryULocales[idx] = forLocale(currentCategoryDefault);
                }
            } else {
                Locale currentDefault = Locale.getDefault();
                if (!defaultLocale.equals(currentDefault)) {
                    defaultLocale = currentDefault;
                    defaultULocale = forLocale(currentDefault);
                    for (Category cat : Category.values()) {
                        int tmpIdx = cat.ordinal();
                        defaultCategoryLocales[tmpIdx] = currentDefault;
                        defaultCategoryULocales[tmpIdx] = forLocale(currentDefault);
                    }
                }
            }
            return defaultCategoryULocales[idx];
        }
    }

    public static synchronized void setDefault(Category category, ULocale newLocale) {
        synchronized (ULocale.class) {
            Locale newJavaDefault = newLocale.toLocale();
            int idx = category.ordinal();
            defaultCategoryULocales[idx] = newLocale;
            defaultCategoryLocales[idx] = newJavaDefault;
            JDKLocaleHelper.setDefault(category, newJavaDefault);
        }
    }

    public Object clone() {
        return this;
    }

    public int hashCode() {
        return this.localeID.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ULocale) {
            return this.localeID.equals(((ULocale) obj).localeID);
        }
        return false;
    }

    public int compareTo(ULocale other) {
        int i = 0;
        if (this == other) {
            return 0;
        }
        int cmp = getLanguage().compareTo(other.getLanguage());
        if (cmp == 0) {
            cmp = getScript().compareTo(other.getScript());
            if (cmp == 0) {
                cmp = getCountry().compareTo(other.getCountry());
                if (cmp == 0) {
                    cmp = getVariant().compareTo(other.getVariant());
                    if (cmp == 0) {
                        Iterator<String> thisKwdItr = getKeywords();
                        Iterator<String> otherKwdItr = other.getKeywords();
                        if (thisKwdItr == null) {
                            if (otherKwdItr == null) {
                                cmp = 0;
                            } else {
                                cmp = -1;
                            }
                        } else if (otherKwdItr == null) {
                            cmp = 1;
                        } else {
                            while (cmp == 0 && thisKwdItr.hasNext()) {
                                if (!otherKwdItr.hasNext()) {
                                    cmp = 1;
                                    break;
                                }
                                String thisKey = (String) thisKwdItr.next();
                                String otherKey = (String) otherKwdItr.next();
                                cmp = thisKey.compareTo(otherKey);
                                if (cmp == 0) {
                                    String thisVal = getKeywordValue(thisKey);
                                    String otherVal = other.getKeywordValue(otherKey);
                                    if (thisVal == null) {
                                        if (otherVal == null) {
                                            cmp = 0;
                                        } else {
                                            cmp = -1;
                                        }
                                    } else if (otherVal == null) {
                                        cmp = 1;
                                    } else {
                                        cmp = thisVal.compareTo(otherVal);
                                    }
                                }
                            }
                            if (cmp == 0 && otherKwdItr.hasNext()) {
                                cmp = -1;
                            }
                        }
                    }
                }
            }
        }
        if (cmp < 0) {
            i = -1;
        } else if (cmp > 0) {
            i = 1;
        }
        return i;
    }

    public static ULocale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableULocales();
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

    public static String getLanguage(String localeID) {
        return new LocaleIDParser(localeID).getLanguage();
    }

    public String getScript() {
        return base().getScript();
    }

    public static String getScript(String localeID) {
        return new LocaleIDParser(localeID).getScript();
    }

    public String getCountry() {
        return base().getRegion();
    }

    public static String getCountry(String localeID) {
        return new LocaleIDParser(localeID).getCountry();
    }

    @Deprecated
    public static String getRegionForSupplementalData(ULocale locale, boolean inferRegion) {
        String region = locale.getKeywordValue("rg");
        if (region != null && region.length() == 6) {
            String regionUpper = AsciiUtil.toUpperString(region);
            if (regionUpper.endsWith(DateFormat.ABBR_UTC_TZ)) {
                return regionUpper.substring(0, 2);
            }
        }
        region = locale.getCountry();
        if (region.length() == 0 && inferRegion) {
            region = addLikelySubtags(locale).getCountry();
        }
        return region;
    }

    public String getVariant() {
        return base().getVariant();
    }

    public static String getVariant(String localeID) {
        return new LocaleIDParser(localeID).getVariant();
    }

    public static String getFallback(String localeID) {
        return getFallbackString(getName(localeID));
    }

    public ULocale getFallback() {
        if (this.localeID.length() == 0 || this.localeID.charAt(0) == '@') {
            return null;
        }
        return new ULocale(getFallbackString(this.localeID), (Locale) null);
    }

    private static String getFallbackString(String fallback) {
        int extStart = fallback.indexOf(64);
        if (extStart == -1) {
            extStart = fallback.length();
        }
        int last = fallback.lastIndexOf(95, extStart);
        if (last == -1) {
            last = 0;
        } else {
            while (last > 0 && fallback.charAt(last - 1) == UNDERSCORE) {
                last--;
            }
        }
        return fallback.substring(0, last) + fallback.substring(extStart);
    }

    public String getBaseName() {
        return getBaseName(this.localeID);
    }

    public static String getBaseName(String localeID) {
        if (localeID.indexOf(64) == -1) {
            return localeID;
        }
        return new LocaleIDParser(localeID).getBaseName();
    }

    public String getName() {
        return this.localeID;
    }

    private static int getShortestSubtagLength(String localeID) {
        int localeIDLength = localeID.length();
        int length = localeIDLength;
        boolean reset = true;
        int tmpLength = 0;
        int i = 0;
        while (i < localeIDLength) {
            if (localeID.charAt(i) == UNDERSCORE || localeID.charAt(i) == '-') {
                if (tmpLength != 0 && tmpLength < length) {
                    length = tmpLength;
                }
                reset = true;
            } else {
                if (reset) {
                    reset = false;
                    tmpLength = 0;
                }
                tmpLength++;
            }
            i++;
        }
        return length;
    }

    public static String getName(String localeID) {
        String tmpLocaleID;
        if (localeID == null || (localeID.contains("@") ^ 1) == 0 || getShortestSubtagLength(localeID) != 1) {
            tmpLocaleID = localeID;
        } else {
            tmpLocaleID = forLanguageTag(localeID).getName();
            if (tmpLocaleID.length() == 0) {
                tmpLocaleID = localeID;
            }
        }
        return (String) nameCache.getInstance(tmpLocaleID, null);
    }

    public String toString() {
        return this.localeID;
    }

    public Iterator<String> getKeywords() {
        return getKeywords(this.localeID);
    }

    public static Iterator<String> getKeywords(String localeID) {
        return new LocaleIDParser(localeID).getKeywords();
    }

    public String getKeywordValue(String keywordName) {
        return getKeywordValue(this.localeID, keywordName);
    }

    public static String getKeywordValue(String localeID, String keywordName) {
        return new LocaleIDParser(localeID).getKeywordValue(keywordName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0069  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String canonicalize(String localeID) {
        LocaleIDParser parser = new LocaleIDParser(localeID, true);
        String baseName = parser.getBaseName();
        boolean foundVariant = false;
        if (localeID.equals("")) {
            return "";
        }
        int i;
        initCANONICALIZE_MAP();
        for (String[] vals : variantsToKeywords) {
            String[] vals2;
            int idx = baseName.lastIndexOf(BaseLocale.SEP + vals2[0]);
            if (idx > -1) {
                foundVariant = true;
                baseName = baseName.substring(0, idx);
                if (baseName.endsWith(BaseLocale.SEP)) {
                    baseName = baseName.substring(0, idx - 1);
                }
                parser.setBaseName(baseName);
                parser.defaultKeywordValue(vals2[1], vals2[2]);
                for (i = 0; i < CANONICALIZE_MAP.length; i++) {
                    if (CANONICALIZE_MAP[i][0].equals(baseName)) {
                        foundVariant = true;
                        vals2 = CANONICALIZE_MAP[i];
                        parser.setBaseName(vals2[1]);
                        if (vals2[2] != null) {
                            parser.defaultKeywordValue(vals2[2], vals2[3]);
                        }
                        if (!foundVariant && parser.getLanguage().equals("nb") && parser.getVariant().equals("NY")) {
                            parser.setBaseName(lscvToID("nn", parser.getScript(), parser.getCountry(), null));
                        }
                        return parser.getName();
                    }
                }
                parser.setBaseName(lscvToID("nn", parser.getScript(), parser.getCountry(), null));
                return parser.getName();
            }
        }
        while (i < CANONICALIZE_MAP.length) {
        }
        parser.setBaseName(lscvToID("nn", parser.getScript(), parser.getCountry(), null));
        return parser.getName();
    }

    public ULocale setKeywordValue(String keyword, String value) {
        return new ULocale(setKeywordValue(this.localeID, keyword, value), (Locale) null);
    }

    public static String setKeywordValue(String localeID, String keyword, String value) {
        LocaleIDParser parser = new LocaleIDParser(localeID);
        parser.setKeywordValue(keyword, value);
        return parser.getName();
    }

    public String getISO3Language() {
        return getISO3Language(this.localeID);
    }

    public static String getISO3Language(String localeID) {
        return LocaleIDs.getISO3Language(getLanguage(localeID));
    }

    public String getISO3Country() {
        return getISO3Country(this.localeID);
    }

    public static String getISO3Country(String localeID) {
        return LocaleIDs.getISO3Country(getCountry(localeID));
    }

    public boolean isRightToLeft() {
        String script = getScript();
        if (script.length() == 0) {
            String lang = getLanguage();
            if (lang.length() == 0) {
                return false;
            }
            int langIndex = LANG_DIR_STRING.indexOf(lang);
            if (langIndex >= 0) {
                switch (LANG_DIR_STRING.charAt(lang.length() + langIndex)) {
                    case '+':
                        return true;
                    case '-':
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

    public String getDisplayLanguage(ULocale displayLocale) {
        return getDisplayLanguageInternal(this, displayLocale, false);
    }

    public static String getDisplayLanguage(String localeID, String displayLocaleID) {
        return getDisplayLanguageInternal(new ULocale(localeID), new ULocale(displayLocaleID), false);
    }

    public static String getDisplayLanguage(String localeID, ULocale displayLocale) {
        return getDisplayLanguageInternal(new ULocale(localeID), displayLocale, false);
    }

    public String getDisplayLanguageWithDialect() {
        return getDisplayLanguageInternal(this, getDefault(Category.DISPLAY), true);
    }

    public String getDisplayLanguageWithDialect(ULocale displayLocale) {
        return getDisplayLanguageInternal(this, displayLocale, true);
    }

    public static String getDisplayLanguageWithDialect(String localeID, String displayLocaleID) {
        return getDisplayLanguageInternal(new ULocale(localeID), new ULocale(displayLocaleID), true);
    }

    public static String getDisplayLanguageWithDialect(String localeID, ULocale displayLocale) {
        return getDisplayLanguageInternal(new ULocale(localeID), displayLocale, true);
    }

    private static String getDisplayLanguageInternal(ULocale locale, ULocale displayLocale, boolean useDialect) {
        return LocaleDisplayNames.getInstance(displayLocale).languageDisplayName(useDialect ? locale.getBaseName() : locale.getLanguage());
    }

    public String getDisplayScript() {
        return getDisplayScriptInternal(this, getDefault(Category.DISPLAY));
    }

    @Deprecated
    public String getDisplayScriptInContext() {
        return getDisplayScriptInContextInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayScript(ULocale displayLocale) {
        return getDisplayScriptInternal(this, displayLocale);
    }

    @Deprecated
    public String getDisplayScriptInContext(ULocale displayLocale) {
        return getDisplayScriptInContextInternal(this, displayLocale);
    }

    public static String getDisplayScript(String localeID, String displayLocaleID) {
        return getDisplayScriptInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    @Deprecated
    public static String getDisplayScriptInContext(String localeID, String displayLocaleID) {
        return getDisplayScriptInContextInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    public static String getDisplayScript(String localeID, ULocale displayLocale) {
        return getDisplayScriptInternal(new ULocale(localeID), displayLocale);
    }

    @Deprecated
    public static String getDisplayScriptInContext(String localeID, ULocale displayLocale) {
        return getDisplayScriptInContextInternal(new ULocale(localeID), displayLocale);
    }

    private static String getDisplayScriptInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).scriptDisplayName(locale.getScript());
    }

    private static String getDisplayScriptInContextInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).scriptDisplayNameInContext(locale.getScript());
    }

    public String getDisplayCountry() {
        return getDisplayCountryInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayCountry(ULocale displayLocale) {
        return getDisplayCountryInternal(this, displayLocale);
    }

    public static String getDisplayCountry(String localeID, String displayLocaleID) {
        return getDisplayCountryInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    public static String getDisplayCountry(String localeID, ULocale displayLocale) {
        return getDisplayCountryInternal(new ULocale(localeID), displayLocale);
    }

    private static String getDisplayCountryInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).regionDisplayName(locale.getCountry());
    }

    public String getDisplayVariant() {
        return getDisplayVariantInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayVariant(ULocale displayLocale) {
        return getDisplayVariantInternal(this, displayLocale);
    }

    public static String getDisplayVariant(String localeID, String displayLocaleID) {
        return getDisplayVariantInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    public static String getDisplayVariant(String localeID, ULocale displayLocale) {
        return getDisplayVariantInternal(new ULocale(localeID), displayLocale);
    }

    private static String getDisplayVariantInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).variantDisplayName(locale.getVariant());
    }

    public static String getDisplayKeyword(String keyword) {
        return getDisplayKeywordInternal(keyword, getDefault(Category.DISPLAY));
    }

    public static String getDisplayKeyword(String keyword, String displayLocaleID) {
        return getDisplayKeywordInternal(keyword, new ULocale(displayLocaleID));
    }

    public static String getDisplayKeyword(String keyword, ULocale displayLocale) {
        return getDisplayKeywordInternal(keyword, displayLocale);
    }

    private static String getDisplayKeywordInternal(String keyword, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).keyDisplayName(keyword);
    }

    public String getDisplayKeywordValue(String keyword) {
        return getDisplayKeywordValueInternal(this, keyword, getDefault(Category.DISPLAY));
    }

    public String getDisplayKeywordValue(String keyword, ULocale displayLocale) {
        return getDisplayKeywordValueInternal(this, keyword, displayLocale);
    }

    public static String getDisplayKeywordValue(String localeID, String keyword, String displayLocaleID) {
        return getDisplayKeywordValueInternal(new ULocale(localeID), keyword, new ULocale(displayLocaleID));
    }

    public static String getDisplayKeywordValue(String localeID, String keyword, ULocale displayLocale) {
        return getDisplayKeywordValueInternal(new ULocale(localeID), keyword, displayLocale);
    }

    private static String getDisplayKeywordValueInternal(ULocale locale, String keyword, ULocale displayLocale) {
        keyword = AsciiUtil.toLowerString(keyword.trim());
        return LocaleDisplayNames.getInstance(displayLocale).keyValueDisplayName(keyword, locale.getKeywordValue(keyword));
    }

    public String getDisplayName() {
        return getDisplayNameInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayName(ULocale displayLocale) {
        return getDisplayNameInternal(this, displayLocale);
    }

    public static String getDisplayName(String localeID, String displayLocaleID) {
        return getDisplayNameInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    public static String getDisplayName(String localeID, ULocale displayLocale) {
        return getDisplayNameInternal(new ULocale(localeID), displayLocale);
    }

    private static String getDisplayNameInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).localeDisplayName(locale);
    }

    public String getDisplayNameWithDialect() {
        return getDisplayNameWithDialectInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayNameWithDialect(ULocale displayLocale) {
        return getDisplayNameWithDialectInternal(this, displayLocale);
    }

    public static String getDisplayNameWithDialect(String localeID, String displayLocaleID) {
        return getDisplayNameWithDialectInternal(new ULocale(localeID), new ULocale(displayLocaleID));
    }

    public static String getDisplayNameWithDialect(String localeID, ULocale displayLocale) {
        return getDisplayNameWithDialectInternal(new ULocale(localeID), displayLocale);
    }

    private static String getDisplayNameWithDialectInternal(ULocale locale, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale, DialectHandling.DIALECT_NAMES).localeDisplayName(locale);
    }

    public String getCharacterOrientation() {
        return ICUResourceTableAccess.getTableString(ICUData.ICU_BASE_NAME, this, "layout", "characters", "characters");
    }

    public String getLineOrientation() {
        return ICUResourceTableAccess.getTableString(ICUData.ICU_BASE_NAME, this, "layout", "lines", "lines");
    }

    public static ULocale acceptLanguage(String acceptLanguageList, ULocale[] availableLocales, boolean[] fallback) {
        if (acceptLanguageList == null) {
            throw new NullPointerException();
        }
        ULocale[] acceptList;
        try {
            acceptList = parseAcceptLanguage(acceptLanguageList, true);
        } catch (ParseException e) {
            acceptList = null;
        }
        if (acceptList == null) {
            return null;
        }
        return acceptLanguage(acceptList, availableLocales, fallback);
    }

    public static ULocale acceptLanguage(ULocale[] acceptLanguageList, ULocale[] availableLocales, boolean[] fallback) {
        if (fallback != null) {
            fallback[0] = true;
        }
        for (ULocale aLocale : acceptLanguageList) {
            boolean[] setFallback = fallback;
            ULocale aLocale2;
            do {
                int j = 0;
                while (j < availableLocales.length) {
                    if (availableLocales[j].equals(aLocale2)) {
                        if (setFallback != null) {
                            setFallback[0] = false;
                        }
                        return availableLocales[j];
                    } else if (aLocale2.getScript().length() == 0 && availableLocales[j].getScript().length() > 0 && availableLocales[j].getLanguage().equals(aLocale2.getLanguage()) && availableLocales[j].getCountry().equals(aLocale2.getCountry()) && availableLocales[j].getVariant().equals(aLocale2.getVariant()) && minimizeSubtags(availableLocales[j]).getScript().length() == 0) {
                        if (setFallback != null) {
                            setFallback[0] = false;
                        }
                        return aLocale2;
                    } else {
                        j++;
                    }
                }
                Locale parent = LocaleUtility.fallback(aLocale2.toLocale());
                if (parent != null) {
                    aLocale2 = new ULocale(parent);
                } else {
                    aLocale2 = null;
                }
                setFallback = null;
            } while (aLocale2 != null);
        }
        return null;
    }

    public static ULocale acceptLanguage(String acceptLanguageList, boolean[] fallback) {
        return acceptLanguage(acceptLanguageList, getAvailableLocales(), fallback);
    }

    public static ULocale acceptLanguage(ULocale[] acceptLanguageList, boolean[] fallback) {
        return acceptLanguage(acceptLanguageList, getAvailableLocales(), fallback);
    }

    static ULocale[] parseAcceptLanguage(String acceptLanguage, boolean isLenient) throws ParseException {
        TreeMap<AnonymousClass1ULocaleAcceptLanguageQ, ULocale> map = new TreeMap();
        StringBuilder languageRangeBuf = new StringBuilder();
        StringBuilder qvalBuf = new StringBuilder();
        int state = 0;
        acceptLanguage = acceptLanguage + ",";
        boolean subTag = false;
        boolean q1 = false;
        int n = 0;
        while (n < acceptLanguage.length()) {
            boolean gotLanguageQ = false;
            char c = acceptLanguage.charAt(n);
            switch (state) {
                case 0:
                    if (('A' > c || c > 'Z') && ('a' > c || c > 'z')) {
                        if (c != '*') {
                            if (!(c == ' ' || c == 9)) {
                                state = -1;
                                break;
                            }
                        }
                        languageRangeBuf.append(c);
                        state = 2;
                        break;
                    }
                    languageRangeBuf.append(c);
                    state = 1;
                    subTag = false;
                    break;
                    break;
                case 1:
                    if (('A' > c || c > 'Z') && ('a' > c || c > 'z')) {
                        if (c != '-') {
                            if (c != '_') {
                                if ('0' <= c && c <= '9') {
                                    if (!subTag) {
                                        state = -1;
                                        break;
                                    }
                                    languageRangeBuf.append(c);
                                    break;
                                } else if (c != ',') {
                                    if (c != ' ' && c != 9) {
                                        if (c != ';') {
                                            state = -1;
                                            break;
                                        }
                                        state = 4;
                                        break;
                                    }
                                    state = 3;
                                    break;
                                } else {
                                    gotLanguageQ = true;
                                    break;
                                }
                            } else if (!isLenient) {
                                state = -1;
                                break;
                            } else {
                                subTag = true;
                                languageRangeBuf.append(c);
                                break;
                            }
                        }
                        subTag = true;
                        languageRangeBuf.append(c);
                        break;
                    }
                    languageRangeBuf.append(c);
                    break;
                    break;
                case 2:
                    if (c != ',') {
                        if (c != ' ' && c != 9) {
                            if (c != ';') {
                                state = -1;
                                break;
                            }
                            state = 4;
                            break;
                        }
                        state = 3;
                        break;
                    }
                    gotLanguageQ = true;
                    break;
                    break;
                case 3:
                    if (c != ',') {
                        if (c != ';') {
                            if (!(c == ' ' || c == 9)) {
                                state = -1;
                                break;
                            }
                        }
                        state = 4;
                        break;
                    }
                    gotLanguageQ = true;
                    break;
                case 4:
                    if (c != 'q') {
                        if (!(c == ' ' || c == 9)) {
                            state = -1;
                            break;
                        }
                    }
                    state = 5;
                    break;
                case 5:
                    if (c != '=') {
                        if (!(c == ' ' || c == 9)) {
                            state = -1;
                            break;
                        }
                    }
                    state = 6;
                    break;
                case 6:
                    if (c != '0') {
                        if (c != '1') {
                            if (c != '.') {
                                if (!(c == ' ' || c == 9)) {
                                    state = -1;
                                    break;
                                }
                            } else if (!isLenient) {
                                state = -1;
                                break;
                            } else {
                                qvalBuf.append(c);
                                state = 8;
                                break;
                            }
                        }
                        qvalBuf.append(c);
                        state = 7;
                        break;
                    }
                    q1 = false;
                    qvalBuf.append(c);
                    state = 7;
                    break;
                case 7:
                    if (c != '.') {
                        if (c != ',') {
                            if (c != ' ' && c != 9) {
                                state = -1;
                                break;
                            }
                            state = 10;
                            break;
                        }
                        gotLanguageQ = true;
                        break;
                    }
                    qvalBuf.append(c);
                    state = 8;
                    break;
                    break;
                case 8:
                    if ('0' <= c && c <= '9') {
                        if (q1 && c != '0' && (isLenient ^ 1) != 0) {
                            state = -1;
                            break;
                        }
                        qvalBuf.append(c);
                        state = 9;
                        break;
                    }
                    state = -1;
                    break;
                    break;
                case 9:
                    if ('0' <= c && c <= '9') {
                        if (q1 && c != '0') {
                            state = -1;
                            break;
                        }
                        qvalBuf.append(c);
                        break;
                    } else if (c != ',') {
                        if (c != ' ' && c != 9) {
                            state = -1;
                            break;
                        }
                        state = 10;
                        break;
                    } else {
                        gotLanguageQ = true;
                        break;
                    }
                    break;
                case 10:
                    if (c != ',') {
                        if (!(c == ' ' || c == 9)) {
                            state = -1;
                            break;
                        }
                    }
                    gotLanguageQ = true;
                    break;
            }
            if (state == -1) {
                throw new ParseException("Invalid Accept-Language", n);
            }
            if (gotLanguageQ) {
                double q = 1.0d;
                if (qvalBuf.length() != 0) {
                    try {
                        q = Double.parseDouble(qvalBuf.toString());
                    } catch (NumberFormatException e) {
                        q = 1.0d;
                    }
                    if (q > 1.0d) {
                        q = 1.0d;
                    }
                }
                if (languageRangeBuf.charAt(0) != '*') {
                    map.put(new AnonymousClass1ULocaleAcceptLanguageQ(q, map.size()), new ULocale(canonicalize(languageRangeBuf.toString())));
                }
                languageRangeBuf.setLength(0);
                qvalBuf.setLength(0);
                state = 0;
            }
            n++;
        }
        if (state == 0) {
            return (ULocale[]) map.values().toArray(new ULocale[map.size()]);
        }
        throw new ParseException("Invalid AcceptlLanguage", n);
    }

    public static ULocale addLikelySubtags(ULocale loc) {
        String[] tags = new String[3];
        String trailing = null;
        int trailingIndex = parseTagString(loc.localeID, tags);
        if (trailingIndex < loc.localeID.length()) {
            trailing = loc.localeID.substring(trailingIndex);
        }
        String newLocaleID = createLikelySubtagsString(tags[0], tags[1], tags[2], trailing);
        return newLocaleID == null ? loc : new ULocale(newLocaleID);
    }

    public static ULocale minimizeSubtags(ULocale loc) {
        return minimizeSubtags(loc, Minimize.FAVOR_REGION);
    }

    @Deprecated
    public static ULocale minimizeSubtags(ULocale loc, Minimize fieldToFavor) {
        String[] tags = new String[3];
        int trailingIndex = parseTagString(loc.localeID, tags);
        String originalLang = tags[0];
        String originalScript = tags[1];
        String originalRegion = tags[2];
        String originalTrailing = null;
        if (trailingIndex < loc.localeID.length()) {
            originalTrailing = loc.localeID.substring(trailingIndex);
        }
        String maximizedLocaleID = createLikelySubtagsString(originalLang, originalScript, originalRegion, null);
        if (isEmptyString(maximizedLocaleID)) {
            return loc;
        }
        if (createLikelySubtagsString(originalLang, null, null, null).equals(maximizedLocaleID)) {
            return new ULocale(createTagString(originalLang, null, null, originalTrailing));
        }
        if (fieldToFavor == Minimize.FAVOR_REGION) {
            if (originalRegion.length() != 0 && createLikelySubtagsString(originalLang, null, originalRegion, null).equals(maximizedLocaleID)) {
                return new ULocale(createTagString(originalLang, null, originalRegion, originalTrailing));
            }
            if (originalScript.length() != 0 && createLikelySubtagsString(originalLang, originalScript, null, null).equals(maximizedLocaleID)) {
                return new ULocale(createTagString(originalLang, originalScript, null, originalTrailing));
            }
        } else if (originalScript.length() != 0 && createLikelySubtagsString(originalLang, originalScript, null, null).equals(maximizedLocaleID)) {
            return new ULocale(createTagString(originalLang, originalScript, null, originalTrailing));
        } else {
            if (originalRegion.length() != 0 && createLikelySubtagsString(originalLang, null, originalRegion, null).equals(maximizedLocaleID)) {
                return new ULocale(createTagString(originalLang, null, originalRegion, originalTrailing));
            }
        }
        return loc;
    }

    private static boolean isEmptyString(String string) {
        return string == null || string.length() == 0;
    }

    private static void appendTag(String tag, StringBuilder buffer) {
        if (buffer.length() != 0) {
            buffer.append(UNDERSCORE);
        }
        buffer.append(tag);
    }

    private static String createTagString(String lang, String script, String region, String trailing, String alternateTags) {
        LocaleIDParser parser = null;
        boolean regionAppended = false;
        StringBuilder tag = new StringBuilder();
        if (!isEmptyString(lang)) {
            appendTag(lang, tag);
        } else if (isEmptyString(alternateTags)) {
            appendTag(UNDEFINED_LANGUAGE, tag);
        } else {
            parser = new LocaleIDParser(alternateTags);
            String alternateLang = parser.getLanguage();
            if (isEmptyString(alternateLang)) {
                alternateLang = UNDEFINED_LANGUAGE;
            }
            appendTag(alternateLang, tag);
        }
        if (!isEmptyString(script)) {
            appendTag(script, tag);
        } else if (!isEmptyString(alternateTags)) {
            if (parser == null) {
                parser = new LocaleIDParser(alternateTags);
            }
            String alternateScript = parser.getScript();
            if (!isEmptyString(alternateScript)) {
                appendTag(alternateScript, tag);
            }
        }
        if (!isEmptyString(region)) {
            appendTag(region, tag);
            regionAppended = true;
        } else if (!isEmptyString(alternateTags)) {
            if (parser == null) {
                parser = new LocaleIDParser(alternateTags);
            }
            String alternateRegion = parser.getCountry();
            if (!isEmptyString(alternateRegion)) {
                appendTag(alternateRegion, tag);
                regionAppended = true;
            }
        }
        if (trailing != null && trailing.length() > 1) {
            int separators = 0;
            if (trailing.charAt(0) != UNDERSCORE) {
                separators = 1;
            } else if (trailing.charAt(1) == UNDERSCORE) {
                separators = 2;
            }
            if (!regionAppended) {
                if (separators == 1) {
                    tag.append(UNDERSCORE);
                }
                tag.append(trailing);
            } else if (separators == 2) {
                tag.append(trailing.substring(1));
            } else {
                tag.append(trailing);
            }
        }
        return tag.toString();
    }

    static String createTagString(String lang, String script, String region, String trailing) {
        return createTagString(lang, script, region, trailing, null);
    }

    private static int parseTagString(String localeID, String[] tags) {
        LocaleIDParser parser = new LocaleIDParser(localeID);
        String lang = parser.getLanguage();
        String script = parser.getScript();
        String region = parser.getCountry();
        if (isEmptyString(lang)) {
            tags[0] = UNDEFINED_LANGUAGE;
        } else {
            tags[0] = lang;
        }
        if (script.equals(UNDEFINED_SCRIPT)) {
            tags[1] = "";
        } else {
            tags[1] = script;
        }
        if (region.equals(UNDEFINED_REGION)) {
            tags[2] = "";
        } else {
            tags[2] = region;
        }
        String variant = parser.getVariant();
        int index;
        if (isEmptyString(variant)) {
            index = localeID.indexOf(64);
            if (index == -1) {
                index = localeID.length();
            }
            return index;
        }
        index = localeID.indexOf(variant);
        if (index > 0) {
            index--;
        }
        return index;
    }

    private static String lookupLikelySubtags(String localeId) {
        try {
            return UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "likelySubtags").getString(localeId);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private static String createLikelySubtagsString(String lang, String script, String region, String variants) {
        String likelySubtags;
        if (!(isEmptyString(script) || (isEmptyString(region) ^ 1) == 0)) {
            likelySubtags = lookupLikelySubtags(createTagString(lang, script, region, null));
            if (likelySubtags != null) {
                return createTagString(null, null, null, variants, likelySubtags);
            }
        }
        if (!isEmptyString(script)) {
            likelySubtags = lookupLikelySubtags(createTagString(lang, script, null, null));
            if (likelySubtags != null) {
                return createTagString(null, null, region, variants, likelySubtags);
            }
        }
        if (!isEmptyString(region)) {
            likelySubtags = lookupLikelySubtags(createTagString(lang, null, region, null));
            if (likelySubtags != null) {
                return createTagString(null, script, null, variants, likelySubtags);
            }
        }
        likelySubtags = lookupLikelySubtags(createTagString(lang, null, null, null));
        if (likelySubtags != null) {
            return createTagString(null, script, region, variants, likelySubtags);
        }
        return null;
    }

    public String getExtension(char key) {
        if (LocaleExtensions.isValidKey(key)) {
            return extensions().getExtensionValue(Character.valueOf(key));
        }
        throw new IllegalArgumentException("Invalid extension key: " + key);
    }

    public Set<Character> getExtensionKeys() {
        return extensions().getKeys();
    }

    public Set<String> getUnicodeLocaleAttributes() {
        return extensions().getUnicodeLocaleAttributes();
    }

    public String getUnicodeLocaleType(String key) {
        if (LocaleExtensions.isValidUnicodeLocaleKey(key)) {
            return extensions().getUnicodeLocaleType(key);
        }
        throw new IllegalArgumentException("Invalid Unicode locale key: " + key);
    }

    public Set<String> getUnicodeLocaleKeys() {
        return extensions().getUnicodeLocaleKeys();
    }

    public String toLanguageTag() {
        BaseLocale base = base();
        LocaleExtensions exts = extensions();
        if (base.getVariant().equalsIgnoreCase("POSIX")) {
            base = BaseLocale.getInstance(base.getLanguage(), base.getScript(), base.getRegion(), "");
            if (exts.getUnicodeLocaleType("va") == null) {
                InternalLocaleBuilder ilocbld = new InternalLocaleBuilder();
                try {
                    ilocbld.setLocale(BaseLocale.ROOT, exts);
                    ilocbld.setUnicodeLocaleKeyword("va", "posix");
                    exts = ilocbld.getLocaleExtensions();
                } catch (LocaleSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        LanguageTag tag = LanguageTag.parseLocale(base, exts);
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
            buf.append(LanguageTag.canonicalizeVariant(s));
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
            buf.append(LanguageTag.canonicalizePrivateuse(subtag));
        }
        return buf.toString();
    }

    public static ULocale forLanguageTag(String languageTag) {
        LanguageTag tag = LanguageTag.parse(languageTag, null);
        InternalLocaleBuilder bldr = new InternalLocaleBuilder();
        bldr.setLanguageTag(tag);
        return getInstance(bldr.getBaseLocale(), bldr.getLocaleExtensions());
    }

    public static String toUnicodeLocaleKey(String keyword) {
        String bcpKey = KeyTypeData.toBcpKey(keyword);
        if (bcpKey == null && UnicodeLocaleExtension.isKey(keyword)) {
            return AsciiUtil.toLowerString(keyword);
        }
        return bcpKey;
    }

    public static String toUnicodeLocaleType(String keyword, String value) {
        String bcpType = KeyTypeData.toBcpType(keyword, value, null, null);
        if (bcpType == null && UnicodeLocaleExtension.isType(value)) {
            return AsciiUtil.toLowerString(value);
        }
        return bcpType;
    }

    public static String toLegacyKey(String keyword) {
        String legacyKey = KeyTypeData.toLegacyKey(keyword);
        if (legacyKey == null && keyword.matches("[0-9a-zA-Z]+")) {
            return AsciiUtil.toLowerString(keyword);
        }
        return legacyKey;
    }

    public static String toLegacyType(String keyword, String value) {
        String legacyType = KeyTypeData.toLegacyType(keyword, value, null, null);
        if (legacyType == null && value.matches("[0-9a-zA-Z]+([_/\\-][0-9a-zA-Z]+)*")) {
            return AsciiUtil.toLowerString(value);
        }
        return legacyType;
    }

    private static ULocale getInstance(BaseLocale base, LocaleExtensions exts) {
        String id = lscvToID(base.getLanguage(), base.getScript(), base.getRegion(), base.getVariant());
        Set<Character> extKeys = exts.getKeys();
        if (!extKeys.isEmpty()) {
            TreeMap<String, String> kwds = new TreeMap();
            for (Character key : extKeys) {
                Extension ext = exts.getExtension(key);
                if (ext instanceof UnicodeLocaleExtension) {
                    UnicodeLocaleExtension uext = (UnicodeLocaleExtension) ext;
                    for (String bcpKey : uext.getUnicodeLocaleKeys()) {
                        String bcpType = uext.getUnicodeLocaleType(bcpKey);
                        String lkey = toLegacyKey(bcpKey);
                        if (bcpType.length() == 0) {
                            bcpType = "yes";
                        }
                        String ltype = toLegacyType(bcpKey, bcpType);
                        if (lkey.equals("va") && ltype.equals("posix") && base.getVariant().length() == 0) {
                            id = id + "_POSIX";
                        } else {
                            kwds.put(lkey, ltype);
                        }
                    }
                    Set<String> uattributes = uext.getUnicodeLocaleAttributes();
                    if (uattributes.size() > 0) {
                        StringBuilder attrbuf = new StringBuilder();
                        for (String attr : uattributes) {
                            if (attrbuf.length() > 0) {
                                attrbuf.append('-');
                            }
                            attrbuf.append(attr);
                        }
                        kwds.put(LOCALE_ATTRIBUTE_KEY, attrbuf.toString());
                    }
                } else {
                    kwds.put(String.valueOf(key), ext.getValue());
                }
            }
            if (!kwds.isEmpty()) {
                StringBuilder buf = new StringBuilder(id);
                buf.append("@");
                boolean insertSep = false;
                for (Entry<String, String> kwd : kwds.entrySet()) {
                    if (insertSep) {
                        buf.append(";");
                    } else {
                        insertSep = true;
                    }
                    buf.append((String) kwd.getKey());
                    buf.append("=");
                    buf.append((String) kwd.getValue());
                }
                id = buf.toString();
            }
        }
        return new ULocale(id);
    }

    private BaseLocale base() {
        if (this.baseLocale == null) {
            String variant = "";
            String region = variant;
            String script = variant;
            String language = variant;
            if (!equals(ROOT)) {
                LocaleIDParser lp = new LocaleIDParser(this.localeID);
                language = lp.getLanguage();
                script = lp.getScript();
                region = lp.getCountry();
                variant = lp.getVariant();
            }
            this.baseLocale = BaseLocale.getInstance(language, script, region, variant);
        }
        return this.baseLocale;
    }

    private LocaleExtensions extensions() {
        if (this.extensions == null) {
            Iterator<String> kwitr = getKeywords();
            if (kwitr == null) {
                this.extensions = LocaleExtensions.EMPTY_EXTENSIONS;
            } else {
                InternalLocaleBuilder intbld = new InternalLocaleBuilder();
                while (kwitr.hasNext()) {
                    String key = (String) kwitr.next();
                    if (key.equals(LOCALE_ATTRIBUTE_KEY)) {
                        for (String uattr : getKeywordValue(key).split("[-_]")) {
                            try {
                                intbld.addUnicodeLocaleAttribute(uattr);
                            } catch (LocaleSyntaxException e) {
                            }
                        }
                    } else if (key.length() >= 2) {
                        String bcpKey = toUnicodeLocaleKey(key);
                        String bcpType = toUnicodeLocaleType(key, getKeywordValue(key));
                        if (!(bcpKey == null || bcpType == null)) {
                            try {
                                intbld.setUnicodeLocaleKeyword(bcpKey, bcpType);
                            } catch (LocaleSyntaxException e2) {
                            }
                        }
                    } else if (key.length() == 1 && key.charAt(0) != 'u') {
                        try {
                            intbld.setExtension(key.charAt(0), getKeywordValue(key).replace(BaseLocale.SEP, LanguageTag.SEP));
                        } catch (LocaleSyntaxException e3) {
                        }
                    }
                }
                this.extensions = intbld.getLocaleExtensions();
            }
        }
        return this.extensions;
    }
}
