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
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class ULocale implements Serializable, Comparable<ULocale> {
    public static Type ACTUAL_LOCALE = new Type();
    private static final SoftCache<Locale, ULocale, Void> CACHE = new SoftCache<Locale, ULocale, Void>() {
        /* access modifiers changed from: protected */
        public ULocale createInstance(Locale key, Void unused) {
            return JDKLocaleHelper.toULocale(key);
        }
    };
    public static final ULocale CANADA = new ULocale("en_CA", Locale.CANADA);
    public static final ULocale CANADA_FRENCH = new ULocale("fr_CA", Locale.CANADA_FRENCH);
    private static String[][] CANONICALIZE_MAP = {new String[]{"C", "en_US_POSIX", null, null}, new String[]{"art_LOJBAN", "jbo", null, null}, new String[]{"az_AZ_CYRL", "az_Cyrl_AZ", null, null}, new String[]{"az_AZ_LATN", "az_Latn_AZ", null, null}, new String[]{"ca_ES_PREEURO", "ca_ES", "currency", "ESP"}, new String[]{"cel_GAULISH", "cel__GAULISH", null, null}, new String[]{"de_1901", "de__1901", null, null}, new String[]{"de_1906", "de__1906", null, null}, new String[]{"de__PHONEBOOK", "de", "collation", "phonebook"}, new String[]{"de_AT_PREEURO", "de_AT", "currency", "ATS"}, new String[]{"de_DE_PREEURO", "de_DE", "currency", "DEM"}, new String[]{"de_LU_PREEURO", "de_LU", "currency", "EUR"}, new String[]{"el_GR_PREEURO", "el_GR", "currency", "GRD"}, new String[]{"en_BOONT", "en__BOONT", null, null}, new String[]{"en_SCOUSE", "en__SCOUSE", null, null}, new String[]{"en_BE_PREEURO", "en_BE", "currency", "BEF"}, new String[]{"en_IE_PREEURO", "en_IE", "currency", "IEP"}, new String[]{"es__TRADITIONAL", "es", "collation", "traditional"}, new String[]{"es_ES_PREEURO", "es_ES", "currency", "ESP"}, new String[]{"eu_ES_PREEURO", "eu_ES", "currency", "ESP"}, new String[]{"fi_FI_PREEURO", "fi_FI", "currency", "FIM"}, new String[]{"fr_BE_PREEURO", "fr_BE", "currency", "BEF"}, new String[]{"fr_FR_PREEURO", "fr_FR", "currency", "FRF"}, new String[]{"fr_LU_PREEURO", "fr_LU", "currency", "LUF"}, new String[]{"ga_IE_PREEURO", "ga_IE", "currency", "IEP"}, new String[]{"gl_ES_PREEURO", "gl_ES", "currency", "ESP"}, new String[]{"hi__DIRECT", "hi", "collation", "direct"}, new String[]{"it_IT_PREEURO", "it_IT", "currency", "ITL"}, new String[]{"ja_JP_TRADITIONAL", "ja_JP", "calendar", "japanese"}, new String[]{"nl_BE_PREEURO", "nl_BE", "currency", "BEF"}, new String[]{"nl_NL_PREEURO", "nl_NL", "currency", "NLG"}, new String[]{"pt_PT_PREEURO", "pt_PT", "currency", "PTE"}, new String[]{"sl_ROZAJ", "sl__ROZAJ", null, null}, new String[]{"sr_SP_CYRL", "sr_Cyrl_RS", null, null}, new String[]{"sr_SP_LATN", "sr_Latn_RS", null, null}, new String[]{"sr_YU_CYRILLIC", "sr_Cyrl_RS", null, null}, new String[]{"th_TH_TRADITIONAL", "th_TH", "calendar", "buddhist"}, new String[]{"uz_UZ_CYRILLIC", "uz_Cyrl_UZ", null, null}, new String[]{"uz_UZ_CYRL", "uz_Cyrl_UZ", null, null}, new String[]{"uz_UZ_LATN", "uz_Latn_UZ", null, null}, new String[]{"zh_CHS", "zh_Hans", null, null}, new String[]{"zh_CHT", "zh_Hant", null, null}, new String[]{"zh_GAN", "zh__GAN", null, null}, new String[]{"zh_GUOYU", "zh", null, null}, new String[]{"zh_HAKKA", "zh__HAKKA", null, null}, new String[]{"zh_MIN", "zh__MIN", null, null}, new String[]{"zh_MIN_NAN", "zh__MINNAN", null, null}, new String[]{"zh_WUU", "zh__WUU", null, null}, new String[]{"zh_XIANG", "zh__XIANG", null, null}, new String[]{"zh_YUE", "zh__YUE", null, null}};
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
        /* access modifiers changed from: protected */
        public String createInstance(String tmpLocaleID, Void unused) {
            return new LocaleIDParser(tmpLocaleID).getName();
        }
    };
    private static final long serialVersionUID = 3715177670352309217L;
    private static String[][] variantsToKeywords = {new String[]{"EURO", "currency", "EUR"}, new String[]{"PINYIN", "collation", "pinyin"}, new String[]{"STROKE", "collation", "stroke"}};
    private volatile transient BaseLocale baseLocale;
    private volatile transient LocaleExtensions extensions;
    private volatile transient Locale locale;
    private String localeID;

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
            if (!sts.isError()) {
                this._locbld.setLanguageTag(tag);
                return this;
            }
            throw new IllformedLocaleException(sts.getErrorMessage(), sts.getErrorIndex());
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
        private static final String[][] JAVA6_MAPDATA = {new String[]{"ja_JP_JP", "ja_JP", "calendar", "japanese", "ja"}, new String[]{"no_NO_NY", "nn_NO", null, null, "nn"}, new String[]{"th_TH_TH", "th_TH", "numbers", "thai", "th"}};
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

        static {
            hasScriptsAndUnicodeExtensions = false;
            hasLocaleCategories = false;
            try {
                mGetScript = Locale.class.getMethod("getScript", null);
                mGetExtensionKeys = Locale.class.getMethod("getExtensionKeys", null);
                mGetExtension = Locale.class.getMethod("getExtension", new Class[]{Character.TYPE});
                mGetUnicodeLocaleKeys = Locale.class.getMethod("getUnicodeLocaleKeys", null);
                mGetUnicodeLocaleAttributes = Locale.class.getMethod("getUnicodeLocaleAttributes", null);
                mGetUnicodeLocaleType = Locale.class.getMethod("getUnicodeLocaleType", new Class[]{String.class});
                mForLanguageTag = Locale.class.getMethod("forLanguageTag", new Class[]{String.class});
                hasScriptsAndUnicodeExtensions = true;
            } catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
            }
            Class<?> cCategory = null;
            try {
                Class<?>[] classes = Locale.class.getDeclaredClasses();
                int length = classes.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    Class<?> c = classes[i];
                    if (c.getName().equals("java.util.Locale$Category")) {
                        cCategory = c;
                        break;
                    }
                    i++;
                }
                if (cCategory != null) {
                    mGetDefault = Locale.class.getDeclaredMethod("getDefault", new Class[]{cCategory});
                    mSetDefault = Locale.class.getDeclaredMethod("setDefault", new Class[]{cCategory, Locale.class});
                    Method mName = cCategory.getMethod("name", null);
                    for (Object e2 : cCategory.getEnumConstants()) {
                        String catVal = (String) mName.invoke(e2, null);
                        if (catVal.equals("DISPLAY")) {
                            eDISPLAY = e2;
                        } else if (catVal.equals("FORMAT")) {
                            eFORMAT = e2;
                        }
                    }
                    if (eDISPLAY == null) {
                        return;
                    }
                    if (eFORMAT != null) {
                        hasLocaleCategories = true;
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e3) {
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
            TreeSet treeSet;
            String str;
            Locale locale = loc;
            String language = loc.getLanguage();
            String country = loc.getCountry();
            String variant = loc.getVariant();
            Set<String> set = null;
            Object[] objArr = null;
            Map<String, String> keywords = null;
            try {
                String script = (String) mGetScript.invoke(locale, null);
                Set<Character> extKeys = (Set) mGetExtensionKeys.invoke(locale, null);
                int i = 1;
                if (!extKeys.isEmpty()) {
                    for (Character extKey : extKeys) {
                        if (extKey.charValue() == 'u') {
                            Set<String> uAttributes = (Set) mGetUnicodeLocaleAttributes.invoke(locale, objArr);
                            if (!uAttributes.isEmpty()) {
                                set = new TreeSet<>();
                                for (String attr : uAttributes) {
                                    set.add(attr);
                                }
                            }
                            try {
                                for (String kwKey : (Set) mGetUnicodeLocaleKeys.invoke(locale, objArr)) {
                                    Method method = mGetUnicodeLocaleType;
                                    Object[] objArr2 = new Object[i];
                                    objArr2[0] = kwKey;
                                    String kwVal = (String) method.invoke(locale, objArr2);
                                    if (kwVal == null) {
                                        treeSet = set;
                                    } else if (kwKey.equals("va")) {
                                        if (variant.length() == 0) {
                                            treeSet = set;
                                            str = kwVal;
                                        } else {
                                            StringBuilder sb = new StringBuilder();
                                            sb.append(kwVal);
                                            treeSet = set;
                                            try {
                                                sb.append(BaseLocale.SEP);
                                                sb.append(variant);
                                                str = sb.toString();
                                            } catch (IllegalAccessException e) {
                                                e = e;
                                                TreeSet treeSet2 = treeSet;
                                                throw new RuntimeException(e);
                                            } catch (InvocationTargetException e2) {
                                                e = e2;
                                                TreeSet treeSet3 = treeSet;
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        variant = str;
                                    } else {
                                        treeSet = set;
                                        if (keywords == null) {
                                            keywords = new TreeMap<>();
                                        }
                                        keywords.put(kwKey, kwVal);
                                    }
                                    set = treeSet;
                                    i = 1;
                                }
                                Set<String> attributes = set;
                            } catch (IllegalAccessException e3) {
                                e = e3;
                                Set<String> set2 = set;
                                throw new RuntimeException(e);
                            } catch (InvocationTargetException e4) {
                                e = e4;
                                Set<String> set3 = set;
                                throw new RuntimeException(e);
                            }
                        } else {
                            String extVal = (String) mGetExtension.invoke(locale, new Object[]{extKey});
                            if (extVal != null) {
                                if (keywords == null) {
                                    keywords = new TreeMap<>();
                                }
                                keywords.put(String.valueOf(extKey), extVal);
                            }
                        }
                        objArr = null;
                        i = 1;
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
                if (set != null) {
                    StringBuilder attrBuf = new StringBuilder();
                    for (String attr2 : set) {
                        if (attrBuf.length() != 0) {
                            attrBuf.append('-');
                        }
                        attrBuf.append(attr2);
                    }
                    if (keywords == null) {
                        keywords = new TreeMap<>();
                    }
                    keywords.put(ULocale.LOCALE_ATTRIBUTE_KEY, attrBuf.toString());
                }
                if (keywords != null) {
                    buf.append('@');
                    boolean addSep = false;
                    for (Map.Entry<String, String> kwEntry : keywords.entrySet()) {
                        String kwKey2 = kwEntry.getKey();
                        String kwVal2 = kwEntry.getValue();
                        if (kwKey2.length() != 1) {
                            kwKey2 = ULocale.toLegacyKey(kwKey2);
                            kwVal2 = ULocale.toLegacyType(kwKey2, kwVal2.length() == 0 ? "yes" : kwVal2);
                        }
                        if (addSep) {
                            buf.append(';');
                        } else {
                            addSep = true;
                        }
                        buf.append(kwKey2);
                        buf.append('=');
                        buf.append(kwVal2);
                    }
                }
                return new ULocale(ULocale.getName(buf.toString()), locale);
            } catch (IllegalAccessException e5) {
                e = e5;
                throw new RuntimeException(e);
            } catch (InvocationTargetException e6) {
                e = e6;
                throw new RuntimeException(e);
            }
        }

        private static ULocale toULocale6(Locale loc) {
            String locStr = loc.toString();
            if (locStr.length() == 0) {
                return ULocale.ROOT;
            }
            int i = 0;
            while (true) {
                if (i >= JAVA6_MAPDATA.length) {
                    break;
                } else if (JAVA6_MAPDATA[i][0].equals(locStr)) {
                    LocaleIDParser p = new LocaleIDParser(JAVA6_MAPDATA[i][1]);
                    p.setKeywordValue(JAVA6_MAPDATA[i][2], JAVA6_MAPDATA[i][3]);
                    locStr = p.getName();
                    break;
                } else {
                    i++;
                }
            }
            return new ULocale(ULocale.getName(locStr), loc);
        }

        private static Locale toLocale7(ULocale uloc) {
            Locale loc = null;
            String ulocStr = uloc.getName();
            if (uloc.getScript().length() > 0 || ulocStr.contains("@")) {
                String tag = AsciiUtil.toUpperString(uloc.toLanguageTag());
                try {
                    loc = (Locale) mForLanguageTag.invoke(null, new Object[]{tag});
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e2) {
                    throw new RuntimeException(e2);
                }
            }
            if (loc == null) {
                return new Locale(uloc.getLanguage(), uloc.getCountry(), uloc.getVariant());
            }
            return loc;
        }

        private static Locale toLocale6(ULocale uloc) {
            String locstr = uloc.getBaseName();
            int i = 0;
            while (true) {
                if (i >= JAVA6_MAPDATA.length) {
                    break;
                }
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
            switch (category) {
                case DISPLAY:
                    cat = eDISPLAY;
                    break;
                case FORMAT:
                    cat = eFORMAT;
                    break;
            }
            if (cat == null) {
                return loc;
            }
            try {
                return (Locale) mGetDefault.invoke(null, new Object[]{cat});
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                return loc;
            }
        }

        public static void setDefault(Category category, Locale newLocale) {
            if (hasLocaleCategories) {
                Object cat = null;
                switch (category) {
                    case DISPLAY:
                        cat = eDISPLAY;
                        break;
                    case FORMAT:
                        cat = eFORMAT;
                        break;
                }
                if (cat != null) {
                    try {
                        mSetDefault.invoke(null, new Object[]{cat, newLocale});
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    }
                }
            }
        }

        public static boolean isOriginalDefaultLocale(Locale loc) {
            boolean z = true;
            if (hasScriptsAndUnicodeExtensions) {
                try {
                    String script = (String) mGetScript.invoke(loc, null);
                    if (!loc.getLanguage().equals(getSystemProperty("user.language")) || !loc.getCountry().equals(getSystemProperty("user.country")) || !loc.getVariant().equals(getSystemProperty("user.variant")) || !script.equals(getSystemProperty("user.script"))) {
                        z = false;
                    }
                    return z;
                } catch (Exception e) {
                    return false;
                }
            } else {
                if (!loc.getLanguage().equals(getSystemProperty("user.language")) || !loc.getCountry().equals(getSystemProperty("user.country")) || !loc.getVariant().equals(getSystemProperty("user.variant"))) {
                    z = false;
                }
                return z;
            }
        }

        public static String getSystemProperty(String key) {
            final String fkey = key;
            if (System.getSecurityManager() == null) {
                return System.getProperty(fkey);
            }
            try {
                return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty(fkey);
                    }
                });
            } catch (AccessControlException e) {
                return null;
            }
        }
    }

    @Deprecated
    public enum Minimize {
        FAVOR_SCRIPT,
        FAVOR_REGION
    }

    public static final class Type {
        private Type() {
        }
    }

    static {
        int i = 0;
        defaultULocale = forLocale(defaultLocale);
        if (JDKLocaleHelper.hasLocaleCategories()) {
            Category[] values = Category.values();
            int length = values.length;
            while (i < length) {
                Category cat = values[i];
                int idx = cat.ordinal();
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
            Category[] values2 = Category.values();
            int length2 = values2.length;
            while (i < length2) {
                int idx2 = values2[i].ordinal();
                defaultCategoryLocales[idx2] = defaultLocale;
                defaultCategoryULocales[idx2] = defaultULocale;
                i++;
            }
        }
    }

    private ULocale(String localeID2, Locale locale2) {
        this.localeID = localeID2;
        this.locale = locale2;
    }

    private ULocale(Locale loc) {
        this.localeID = getName(forLocale(loc).toString());
        this.locale = loc;
    }

    public static ULocale forLocale(Locale loc) {
        if (loc == null) {
            return null;
        }
        return CACHE.getInstance(loc, null);
    }

    public ULocale(String localeID2) {
        this.localeID = getName(localeID2);
    }

    public ULocale(String a, String b) {
        this(a, b, (String) null);
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
                ULocale uLocale = ROOT;
                return uLocale;
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
            ULocale uLocale2 = defaultULocale;
            return uLocale2;
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
                ULocale uLocale = ROOT;
                return uLocale;
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
            ULocale uLocale2 = defaultCategoryULocales[idx];
            return uLocale2;
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
                            cmp = otherKwdItr == null ? 0 : -1;
                        } else if (otherKwdItr == null) {
                            cmp = 1;
                        } else {
                            while (true) {
                                if (cmp != 0 || !thisKwdItr.hasNext()) {
                                    break;
                                } else if (!otherKwdItr.hasNext()) {
                                    cmp = 1;
                                    break;
                                } else {
                                    String thisKey = thisKwdItr.next();
                                    String otherKey = otherKwdItr.next();
                                    cmp = thisKey.compareTo(otherKey);
                                    if (cmp == 0) {
                                        String thisVal = getKeywordValue(thisKey);
                                        String otherVal = other.getKeywordValue(otherKey);
                                        if (thisVal == null) {
                                            cmp = otherVal == null ? 0 : -1;
                                        } else if (otherVal == null) {
                                            cmp = 1;
                                        } else {
                                            cmp = thisVal.compareTo(otherVal);
                                        }
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

    public static String getLanguage(String localeID2) {
        return new LocaleIDParser(localeID2).getLanguage();
    }

    public String getScript() {
        return base().getScript();
    }

    public static String getScript(String localeID2) {
        return new LocaleIDParser(localeID2).getScript();
    }

    public String getCountry() {
        return base().getRegion();
    }

    public static String getCountry(String localeID2) {
        return new LocaleIDParser(localeID2).getCountry();
    }

    @Deprecated
    public static String getRegionForSupplementalData(ULocale locale2, boolean inferRegion) {
        String region = locale2.getKeywordValue("rg");
        if (region != null && region.length() == 6) {
            String regionUpper = AsciiUtil.toUpperString(region);
            if (regionUpper.endsWith(DateFormat.ABBR_UTC_TZ)) {
                return regionUpper.substring(0, 2);
            }
        }
        String region2 = locale2.getCountry();
        if (region2.length() == 0 && inferRegion) {
            region2 = addLikelySubtags(locale2).getCountry();
        }
        return region2;
    }

    public String getVariant() {
        return base().getVariant();
    }

    public static String getVariant(String localeID2) {
        return new LocaleIDParser(localeID2).getVariant();
    }

    public static String getFallback(String localeID2) {
        return getFallbackString(getName(localeID2));
    }

    public ULocale getFallback() {
        if (this.localeID.length() == 0 || this.localeID.charAt(0) == '@') {
            return null;
        }
        return new ULocale(getFallbackString(this.localeID), (Locale) null);
    }

    private static String getFallbackString(String fallback) {
        int last;
        int extStart = fallback.indexOf(64);
        if (extStart == -1) {
            extStart = fallback.length();
        }
        int last2 = fallback.lastIndexOf(95, extStart);
        if (last2 == -1) {
            last = 0;
        } else {
            last = last2;
            while (last > 0 && fallback.charAt(last - 1) == '_') {
                last--;
            }
        }
        return fallback.substring(0, last) + fallback.substring(extStart);
    }

    public String getBaseName() {
        return getBaseName(this.localeID);
    }

    public static String getBaseName(String localeID2) {
        if (localeID2.indexOf(64) == -1) {
            return localeID2;
        }
        return new LocaleIDParser(localeID2).getBaseName();
    }

    public String getName() {
        return this.localeID;
    }

    private static int getShortestSubtagLength(String localeID2) {
        int localeIDLength = localeID2.length();
        int length = localeIDLength;
        boolean reset = true;
        int tmpLength = 0;
        for (int i = 0; i < localeIDLength; i++) {
            if (localeID2.charAt(i) == '_' || localeID2.charAt(i) == '-') {
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
        }
        return length;
    }

    public static String getName(String localeID2) {
        String tmpLocaleID;
        if (localeID2 == null || localeID2.contains("@") || getShortestSubtagLength(localeID2) != 1) {
            tmpLocaleID = localeID2;
        } else {
            tmpLocaleID = forLanguageTag(localeID2).getName();
            if (tmpLocaleID.length() == 0) {
                tmpLocaleID = localeID2;
            }
        }
        return nameCache.getInstance(tmpLocaleID, null);
    }

    public String toString() {
        return this.localeID;
    }

    public Iterator<String> getKeywords() {
        return getKeywords(this.localeID);
    }

    public static Iterator<String> getKeywords(String localeID2) {
        return new LocaleIDParser(localeID2).getKeywords();
    }

    public String getKeywordValue(String keywordName) {
        return getKeywordValue(this.localeID, keywordName);
    }

    public static String getKeywordValue(String localeID2, String keywordName) {
        return new LocaleIDParser(localeID2).getKeywordValue(keywordName);
    }

    public static String canonicalize(String localeID2) {
        LocaleIDParser parser = new LocaleIDParser(localeID2, true);
        String baseName = parser.getBaseName();
        boolean foundVariant = false;
        if (localeID2.equals("")) {
            return "";
        }
        int i = 0;
        while (true) {
            if (i >= variantsToKeywords.length) {
                break;
            }
            String[] vals = variantsToKeywords[i];
            int idx = baseName.lastIndexOf(BaseLocale.SEP + vals[0]);
            if (idx > -1) {
                foundVariant = true;
                baseName = baseName.substring(0, idx);
                if (baseName.endsWith(BaseLocale.SEP)) {
                    baseName = baseName.substring(0, idx - 1);
                }
                parser.setBaseName(baseName);
                parser.defaultKeywordValue(vals[1], vals[2]);
            } else {
                i++;
            }
        }
        int i2 = 0;
        while (true) {
            if (i2 >= CANONICALIZE_MAP.length) {
                break;
            } else if (CANONICALIZE_MAP[i2][0].equals(baseName)) {
                foundVariant = true;
                String[] vals2 = CANONICALIZE_MAP[i2];
                parser.setBaseName(vals2[1]);
                if (vals2[2] != null) {
                    parser.defaultKeywordValue(vals2[2], vals2[3]);
                }
            } else {
                i2++;
            }
        }
        if (!foundVariant && parser.getLanguage().equals("nb") && parser.getVariant().equals("NY")) {
            parser.setBaseName(lscvToID("nn", parser.getScript(), parser.getCountry(), null));
        }
        return parser.getName();
    }

    public ULocale setKeywordValue(String keyword, String value) {
        return new ULocale(setKeywordValue(this.localeID, keyword, value), (Locale) null);
    }

    public static String setKeywordValue(String localeID2, String keyword, String value) {
        LocaleIDParser parser = new LocaleIDParser(localeID2);
        parser.setKeywordValue(keyword, value);
        return parser.getName();
    }

    public String getISO3Language() {
        return getISO3Language(this.localeID);
    }

    public static String getISO3Language(String localeID2) {
        return LocaleIDs.getISO3Language(getLanguage(localeID2));
    }

    public String getISO3Country() {
        return getISO3Country(this.localeID);
    }

    public static String getISO3Country(String localeID2) {
        return LocaleIDs.getISO3Country(getCountry(localeID2));
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
                char charAt = LANG_DIR_STRING.charAt(lang.length() + langIndex);
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

    public String getDisplayLanguage(ULocale displayLocale) {
        return getDisplayLanguageInternal(this, displayLocale, false);
    }

    public static String getDisplayLanguage(String localeID2, String displayLocaleID) {
        return getDisplayLanguageInternal(new ULocale(localeID2), new ULocale(displayLocaleID), false);
    }

    public static String getDisplayLanguage(String localeID2, ULocale displayLocale) {
        return getDisplayLanguageInternal(new ULocale(localeID2), displayLocale, false);
    }

    public String getDisplayLanguageWithDialect() {
        return getDisplayLanguageInternal(this, getDefault(Category.DISPLAY), true);
    }

    public String getDisplayLanguageWithDialect(ULocale displayLocale) {
        return getDisplayLanguageInternal(this, displayLocale, true);
    }

    public static String getDisplayLanguageWithDialect(String localeID2, String displayLocaleID) {
        return getDisplayLanguageInternal(new ULocale(localeID2), new ULocale(displayLocaleID), true);
    }

    public static String getDisplayLanguageWithDialect(String localeID2, ULocale displayLocale) {
        return getDisplayLanguageInternal(new ULocale(localeID2), displayLocale, true);
    }

    private static String getDisplayLanguageInternal(ULocale locale2, ULocale displayLocale, boolean useDialect) {
        return LocaleDisplayNames.getInstance(displayLocale).languageDisplayName(useDialect ? locale2.getBaseName() : locale2.getLanguage());
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

    public static String getDisplayScript(String localeID2, String displayLocaleID) {
        return getDisplayScriptInternal(new ULocale(localeID2), new ULocale(displayLocaleID));
    }

    @Deprecated
    public static String getDisplayScriptInContext(String localeID2, String displayLocaleID) {
        return getDisplayScriptInContextInternal(new ULocale(localeID2), new ULocale(displayLocaleID));
    }

    public static String getDisplayScript(String localeID2, ULocale displayLocale) {
        return getDisplayScriptInternal(new ULocale(localeID2), displayLocale);
    }

    @Deprecated
    public static String getDisplayScriptInContext(String localeID2, ULocale displayLocale) {
        return getDisplayScriptInContextInternal(new ULocale(localeID2), displayLocale);
    }

    private static String getDisplayScriptInternal(ULocale locale2, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).scriptDisplayName(locale2.getScript());
    }

    private static String getDisplayScriptInContextInternal(ULocale locale2, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).scriptDisplayNameInContext(locale2.getScript());
    }

    public String getDisplayCountry() {
        return getDisplayCountryInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayCountry(ULocale displayLocale) {
        return getDisplayCountryInternal(this, displayLocale);
    }

    public static String getDisplayCountry(String localeID2, String displayLocaleID) {
        return getDisplayCountryInternal(new ULocale(localeID2), new ULocale(displayLocaleID));
    }

    public static String getDisplayCountry(String localeID2, ULocale displayLocale) {
        return getDisplayCountryInternal(new ULocale(localeID2), displayLocale);
    }

    private static String getDisplayCountryInternal(ULocale locale2, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).regionDisplayName(locale2.getCountry());
    }

    public String getDisplayVariant() {
        return getDisplayVariantInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayVariant(ULocale displayLocale) {
        return getDisplayVariantInternal(this, displayLocale);
    }

    public static String getDisplayVariant(String localeID2, String displayLocaleID) {
        return getDisplayVariantInternal(new ULocale(localeID2), new ULocale(displayLocaleID));
    }

    public static String getDisplayVariant(String localeID2, ULocale displayLocale) {
        return getDisplayVariantInternal(new ULocale(localeID2), displayLocale);
    }

    private static String getDisplayVariantInternal(ULocale locale2, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).variantDisplayName(locale2.getVariant());
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

    public static String getDisplayKeywordValue(String localeID2, String keyword, String displayLocaleID) {
        return getDisplayKeywordValueInternal(new ULocale(localeID2), keyword, new ULocale(displayLocaleID));
    }

    public static String getDisplayKeywordValue(String localeID2, String keyword, ULocale displayLocale) {
        return getDisplayKeywordValueInternal(new ULocale(localeID2), keyword, displayLocale);
    }

    private static String getDisplayKeywordValueInternal(ULocale locale2, String keyword, ULocale displayLocale) {
        String keyword2 = AsciiUtil.toLowerString(keyword.trim());
        return LocaleDisplayNames.getInstance(displayLocale).keyValueDisplayName(keyword2, locale2.getKeywordValue(keyword2));
    }

    public String getDisplayName() {
        return getDisplayNameInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayName(ULocale displayLocale) {
        return getDisplayNameInternal(this, displayLocale);
    }

    public static String getDisplayName(String localeID2, String displayLocaleID) {
        return getDisplayNameInternal(new ULocale(localeID2), new ULocale(displayLocaleID));
    }

    public static String getDisplayName(String localeID2, ULocale displayLocale) {
        return getDisplayNameInternal(new ULocale(localeID2), displayLocale);
    }

    private static String getDisplayNameInternal(ULocale locale2, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale).localeDisplayName(locale2);
    }

    public String getDisplayNameWithDialect() {
        return getDisplayNameWithDialectInternal(this, getDefault(Category.DISPLAY));
    }

    public String getDisplayNameWithDialect(ULocale displayLocale) {
        return getDisplayNameWithDialectInternal(this, displayLocale);
    }

    public static String getDisplayNameWithDialect(String localeID2, String displayLocaleID) {
        return getDisplayNameWithDialectInternal(new ULocale(localeID2), new ULocale(displayLocaleID));
    }

    public static String getDisplayNameWithDialect(String localeID2, ULocale displayLocale) {
        return getDisplayNameWithDialectInternal(new ULocale(localeID2), displayLocale);
    }

    private static String getDisplayNameWithDialectInternal(ULocale locale2, ULocale displayLocale) {
        return LocaleDisplayNames.getInstance(displayLocale, LocaleDisplayNames.DialectHandling.DIALECT_NAMES).localeDisplayName(locale2);
    }

    public String getCharacterOrientation() {
        return ICUResourceTableAccess.getTableString(ICUData.ICU_BASE_NAME, this, "layout", "characters", "characters");
    }

    public String getLineOrientation() {
        return ICUResourceTableAccess.getTableString(ICUData.ICU_BASE_NAME, this, "layout", "lines", "lines");
    }

    public static ULocale acceptLanguage(String acceptLanguageList, ULocale[] availableLocales, boolean[] fallback) {
        ULocale[] acceptList;
        if (acceptLanguageList != null) {
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
        throw new NullPointerException();
    }

    public static ULocale acceptLanguage(ULocale[] acceptLanguageList, ULocale[] availableLocales, boolean[] fallback) {
        if (fallback != null) {
            fallback[0] = true;
        }
        for (ULocale aLocale : acceptLanguageList) {
            boolean[] setFallback = fallback;
            do {
                int j = 0;
                while (j < availableLocales.length) {
                    if (availableLocales[j].equals(aLocale)) {
                        if (setFallback != null) {
                            setFallback[0] = false;
                        }
                        return availableLocales[j];
                    } else if (aLocale.getScript().length() != 0 || availableLocales[j].getScript().length() <= 0 || !availableLocales[j].getLanguage().equals(aLocale.getLanguage()) || !availableLocales[j].getCountry().equals(aLocale.getCountry()) || !availableLocales[j].getVariant().equals(aLocale.getVariant()) || minimizeSubtags(availableLocales[j]).getScript().length() != 0) {
                        j++;
                    } else {
                        if (setFallback != null) {
                            setFallback[0] = false;
                        }
                        return aLocale;
                    }
                }
                Locale parent = LocaleUtility.fallback(aLocale.toLocale());
                if (parent != null) {
                    aLocale = new ULocale(parent);
                } else {
                    aLocale = null;
                }
                setFallback = null;
            } while (aLocale != null);
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
        TreeMap treeMap = new TreeMap();
        StringBuilder languageRangeBuf = new StringBuilder();
        StringBuilder qvalBuf = new StringBuilder();
        String acceptLanguage2 = acceptLanguage + ",";
        boolean q1 = false;
        boolean subTag = false;
        int state = 0;
        int state2 = 0;
        while (true) {
            int n = state2;
            if (n < acceptLanguage2.length()) {
                boolean gotLanguageQ = false;
                char c = acceptLanguage2.charAt(n);
                switch (state) {
                    case 0:
                        if (('A' > c || c > 'Z') && ('a' > c || c > 'z')) {
                            if (c != '*') {
                                if (!(c == ' ' || c == 9)) {
                                    state = -1;
                                    break;
                                }
                            } else {
                                languageRangeBuf.append(c);
                                state = 2;
                                break;
                            }
                        } else {
                            languageRangeBuf.append(c);
                            state = 1;
                            subTag = false;
                            break;
                        }
                        break;
                    case 1:
                        if (('A' > c || c > 'Z') && ('a' > c || c > 'z')) {
                            if (c != '-') {
                                if (c != '_') {
                                    if ('0' <= c && c <= '9') {
                                        if (!subTag) {
                                            state = -1;
                                            break;
                                        } else {
                                            languageRangeBuf.append(c);
                                            break;
                                        }
                                    } else if (c != ',') {
                                        if (c != ' ' && c != 9) {
                                            if (c != ';') {
                                                state = -1;
                                                break;
                                            } else {
                                                state = 4;
                                                break;
                                            }
                                        } else {
                                            state = 3;
                                            break;
                                        }
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
                            } else {
                                subTag = true;
                                languageRangeBuf.append(c);
                                break;
                            }
                        } else {
                            languageRangeBuf.append(c);
                            break;
                        }
                        break;
                    case 2:
                        if (c != ',') {
                            if (c != ' ' && c != 9) {
                                if (c != ';') {
                                    state = -1;
                                    break;
                                } else {
                                    state = 4;
                                    break;
                                }
                            } else {
                                state = 3;
                                break;
                            }
                        } else {
                            gotLanguageQ = true;
                            break;
                        }
                    case 3:
                        if (c != ',') {
                            if (c != ';') {
                                if (!(c == ' ' || c == 9)) {
                                    state = -1;
                                    break;
                                }
                            } else {
                                state = 4;
                                break;
                            }
                        } else {
                            gotLanguageQ = true;
                            break;
                        }
                    case 4:
                        if (c != 'q') {
                            if (!(c == ' ' || c == 9)) {
                                state = -1;
                                break;
                            }
                        } else {
                            state = 5;
                            break;
                        }
                    case 5:
                        if (c != '=') {
                            if (!(c == ' ' || c == 9)) {
                                state = -1;
                                break;
                            }
                        } else {
                            state = 6;
                            break;
                        }
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
                            } else {
                                qvalBuf.append(c);
                                state = 7;
                                break;
                            }
                        } else {
                            qvalBuf.append(c);
                            state = 7;
                            q1 = false;
                            break;
                        }
                    case 7:
                        if (c != '.') {
                            if (c != ',') {
                                if (c != ' ' && c != 9) {
                                    state = -1;
                                    break;
                                } else {
                                    state = 10;
                                    break;
                                }
                            } else {
                                gotLanguageQ = true;
                                break;
                            }
                        } else {
                            qvalBuf.append(c);
                            state = 8;
                            break;
                        }
                    case 8:
                        if ('0' <= c && c <= '9') {
                            if (q1 && c != '0' && !isLenient) {
                                state = -1;
                                break;
                            } else {
                                qvalBuf.append(c);
                                state = 9;
                                break;
                            }
                        } else {
                            state = -1;
                            break;
                        }
                    case 9:
                        if ('0' <= c && c <= '9') {
                            if (q1 && c != '0') {
                                state = -1;
                                break;
                            } else {
                                qvalBuf.append(c);
                                break;
                            }
                        } else if (c != ',') {
                            if (c != ' ' && c != 9) {
                                state = -1;
                                break;
                            } else {
                                state = 10;
                                break;
                            }
                        } else {
                            gotLanguageQ = true;
                            break;
                        }
                    case 10:
                        if (c != ',') {
                            if (!(c == ' ' || c == 9)) {
                                state = -1;
                                break;
                            }
                        } else {
                            gotLanguageQ = true;
                            break;
                        }
                }
                boolean gotLanguageQ2 = gotLanguageQ;
                if (state != -1) {
                    if (gotLanguageQ2) {
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
                            int serial = treeMap.size();
                            int i = serial;
                            treeMap.put(new Comparable<AnonymousClass1ULocaleAcceptLanguageQ>(q, serial) {
                                private double q;
                                private double serial;

                                {
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
                            }, new ULocale(canonicalize(languageRangeBuf.toString())));
                        }
                        languageRangeBuf.setLength(0);
                        qvalBuf.setLength(0);
                        state = 0;
                    }
                    state2 = n + 1;
                } else {
                    throw new ParseException("Invalid Accept-Language", n);
                }
            } else if (state == 0) {
                return (ULocale[]) treeMap.values().toArray(new ULocale[treeMap.size()]);
            } else {
                throw new ParseException("Invalid AcceptlLanguage", n);
            }
        }
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
        String str;
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
            if (!isEmptyString(alternateLang)) {
                str = alternateLang;
            } else {
                str = UNDEFINED_LANGUAGE;
            }
            appendTag(str, tag);
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
            if (trailing.charAt(0) != '_') {
                separators = 1;
            } else if (trailing.charAt(1) == '_') {
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

    private static int parseTagString(String localeID2, String[] tags) {
        LocaleIDParser parser = new LocaleIDParser(localeID2);
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
        if (!isEmptyString(variant)) {
            int index = localeID2.indexOf(variant);
            return index > 0 ? index - 1 : index;
        }
        int index2 = localeID2.indexOf(64);
        return index2 == -1 ? localeID2.length() : index2;
    }

    private static String lookupLikelySubtags(String localeId) {
        try {
            return UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "likelySubtags").getString(localeId);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private static String createLikelySubtagsString(String lang, String script, String region, String variants) {
        if (!isEmptyString(script) && !isEmptyString(region)) {
            String likelySubtags = lookupLikelySubtags(createTagString(lang, script, region, null));
            if (likelySubtags != null) {
                return createTagString(null, null, null, variants, likelySubtags);
            }
        }
        if (!isEmptyString(script)) {
            String likelySubtags2 = lookupLikelySubtags(createTagString(lang, script, null, null));
            if (likelySubtags2 != null) {
                return createTagString(null, null, region, variants, likelySubtags2);
            }
        }
        if (!isEmptyString(region)) {
            String likelySubtags3 = lookupLikelySubtags(createTagString(lang, null, region, null));
            if (likelySubtags3 != null) {
                return createTagString(null, script, null, variants, likelySubtags3);
            }
        }
        String likelySubtags4 = lookupLikelySubtags(createTagString(lang, null, null, null));
        if (likelySubtags4 != null) {
            return createTagString(null, script, region, variants, likelySubtags4);
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
            buf.append(LanguageTag.canonicalizeVariant(s));
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
            buf.append(LanguageTag.canonicalizePrivateuse(subtag4));
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
        if (bcpKey != null || !UnicodeLocaleExtension.isKey(keyword)) {
            return bcpKey;
        }
        return AsciiUtil.toLowerString(keyword);
    }

    public static String toUnicodeLocaleType(String keyword, String value) {
        String bcpType = KeyTypeData.toBcpType(keyword, value, null, null);
        if (bcpType != null || !UnicodeLocaleExtension.isType(value)) {
            return bcpType;
        }
        return AsciiUtil.toLowerString(value);
    }

    public static String toLegacyKey(String keyword) {
        String legacyKey = KeyTypeData.toLegacyKey(keyword);
        if (legacyKey != null || !keyword.matches("[0-9a-zA-Z]+")) {
            return legacyKey;
        }
        return AsciiUtil.toLowerString(keyword);
    }

    public static String toLegacyType(String keyword, String value) {
        String legacyType = KeyTypeData.toLegacyType(keyword, value, null, null);
        if (legacyType != null || !value.matches("[0-9a-zA-Z]+([_/\\-][0-9a-zA-Z]+)*")) {
            return legacyType;
        }
        return AsciiUtil.toLowerString(value);
    }

    /* access modifiers changed from: private */
    public static ULocale getInstance(BaseLocale base, LocaleExtensions exts) {
        String id = lscvToID(base.getLanguage(), base.getScript(), base.getRegion(), base.getVariant());
        Set<Character> extKeys = exts.getKeys();
        if (!extKeys.isEmpty()) {
            TreeMap<String, String> kwds = new TreeMap<>();
            for (Character key : extKeys) {
                Extension ext = exts.getExtension(key);
                if (ext instanceof UnicodeLocaleExtension) {
                    UnicodeLocaleExtension uext = (UnicodeLocaleExtension) ext;
                    for (String bcpKey : uext.getUnicodeLocaleKeys()) {
                        String bcpType = uext.getUnicodeLocaleType(bcpKey);
                        String lkey = toLegacyKey(bcpKey);
                        String ltype = toLegacyType(bcpKey, bcpType.length() == 0 ? "yes" : bcpType);
                        if (!lkey.equals("va") || !ltype.equals("posix") || base.getVariant().length() != 0) {
                            kwds.put(lkey, ltype);
                        } else {
                            id = id + "_POSIX";
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
            LocaleExtensions localeExtensions = exts;
            if (!kwds.isEmpty()) {
                StringBuilder buf = new StringBuilder(id);
                buf.append("@");
                boolean insertSep = false;
                for (Map.Entry<String, String> kwd : kwds.entrySet()) {
                    if (insertSep) {
                        buf.append(";");
                    } else {
                        insertSep = true;
                    }
                    buf.append(kwd.getKey());
                    buf.append("=");
                    buf.append(kwd.getValue());
                }
                id = buf.toString();
            }
        } else {
            LocaleExtensions localeExtensions2 = exts;
        }
        return new ULocale(id);
    }

    /* access modifiers changed from: private */
    public BaseLocale base() {
        if (this.baseLocale == null) {
            String language = "";
            String variant = language;
            String region = language;
            String script = language;
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

    /* access modifiers changed from: private */
    public LocaleExtensions extensions() {
        if (this.extensions == null) {
            Iterator<String> kwitr = getKeywords();
            if (kwitr == null) {
                this.extensions = LocaleExtensions.EMPTY_EXTENSIONS;
            } else {
                InternalLocaleBuilder intbld = new InternalLocaleBuilder();
                while (kwitr.hasNext()) {
                    String key = kwitr.next();
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
