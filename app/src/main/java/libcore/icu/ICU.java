package libcore.icu;

import android.icu.impl.PatternTokenizer;
import android.icu.impl.locale.LanguageTag;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import libcore.util.BasicLruCache;
import org.xmlpull.v1.XmlPullParser;

public final class ICU {
    private static final BasicLruCache<String, String> CACHED_PATTERNS = null;
    private static final int IDX_LANGUAGE = 0;
    private static final int IDX_REGION = 2;
    private static final int IDX_SCRIPT = 1;
    private static final int IDX_VARIANT = 3;
    public static final int U_BUFFER_OVERFLOW_ERROR = 15;
    public static final int U_ILLEGAL_CHAR_FOUND = 12;
    public static final int U_INVALID_CHAR_FOUND = 10;
    public static final int U_TRUNCATED_CHAR_FOUND = 11;
    public static final int U_ZERO_ERROR = 0;
    private static Locale[] availableLocalesCache;
    private static String[] isoCountries;
    private static String[] isoLanguages;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.icu.ICU.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.icu.ICU.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.icu.ICU.<clinit>():void");
    }

    @Deprecated
    public static native String addLikelySubtags(String str);

    private static native String[] getAvailableBreakIteratorLocalesNative();

    private static native String[] getAvailableCalendarLocalesNative();

    private static native String[] getAvailableCollatorLocalesNative();

    public static native String[] getAvailableCurrencyCodes();

    private static native String[] getAvailableDateFormatLocalesNative();

    private static native String[] getAvailableLocalesNative();

    private static native String[] getAvailableNumberFormatLocalesNative();

    private static native String getBestDateTimePatternNative(String str, String str2);

    public static native String getCldrVersion();

    public static native String getCurrencyCode(String str);

    private static native String getCurrencyDisplayName(String str, String str2);

    public static native int getCurrencyFractionDigits(String str);

    public static native int getCurrencyNumericCode(String str);

    private static native String getCurrencySymbol(String str, String str2);

    public static native String getDefaultLocale();

    private static native String getDisplayCountryNative(String str, String str2);

    private static native String getDisplayLanguageNative(String str, String str2);

    private static native String getDisplayScriptNative(String str, String str2);

    private static native String getDisplayVariantNative(String str, String str2);

    public static native String getISO3Country(String str);

    public static native String getISO3Language(String str);

    private static native String[] getISOCountriesNative();

    private static native String[] getISOLanguagesNative();

    public static native String getIcuVersion();

    @Deprecated
    public static native String getScript(String str);

    public static native String getTZDataVersion();

    public static native String getUnicodeVersion();

    static native boolean initLocaleDataNative(String str, LocaleData localeData);

    public static native void setDefaultLocale(String str);

    private static native String toLowerCase(String str, String str2);

    private static native String toUpperCase(String str, String str2);

    public static String[] getISOLanguages() {
        if (isoLanguages == null) {
            isoLanguages = getISOLanguagesNative();
        }
        return (String[]) isoLanguages.clone();
    }

    public static String[] getISOCountries() {
        if (isoCountries == null) {
            isoCountries = getISOCountriesNative();
        }
        return (String[]) isoCountries.clone();
    }

    private static void parseLangScriptRegionAndVariants(String string, String[] outputArray) {
        int first = string.indexOf(95);
        int second = string.indexOf(95, first + IDX_SCRIPT);
        int third = string.indexOf(95, second + IDX_SCRIPT);
        if (first == -1) {
            outputArray[IDX_LANGUAGE] = string;
        } else if (second == -1) {
            outputArray[IDX_LANGUAGE] = string.substring(IDX_LANGUAGE, first);
            secondString = string.substring(first + IDX_SCRIPT);
            if (secondString.length() == 4) {
                outputArray[IDX_SCRIPT] = secondString;
            } else if (secondString.length() == IDX_REGION || secondString.length() == IDX_VARIANT) {
                outputArray[IDX_REGION] = secondString;
            } else {
                outputArray[IDX_VARIANT] = secondString;
            }
        } else if (third == -1) {
            outputArray[IDX_LANGUAGE] = string.substring(IDX_LANGUAGE, first);
            secondString = string.substring(first + IDX_SCRIPT, second);
            String thirdString = string.substring(second + IDX_SCRIPT);
            if (secondString.length() == 4) {
                outputArray[IDX_SCRIPT] = secondString;
                if (thirdString.length() == IDX_REGION || thirdString.length() == IDX_VARIANT || thirdString.isEmpty()) {
                    outputArray[IDX_REGION] = thirdString;
                } else {
                    outputArray[IDX_VARIANT] = thirdString;
                }
            } else if (secondString.isEmpty() || secondString.length() == IDX_REGION || secondString.length() == IDX_VARIANT) {
                outputArray[IDX_REGION] = secondString;
                outputArray[IDX_VARIANT] = thirdString;
            } else {
                outputArray[IDX_VARIANT] = string.substring(first + IDX_SCRIPT);
            }
        } else {
            outputArray[IDX_LANGUAGE] = string.substring(IDX_LANGUAGE, first);
            secondString = string.substring(first + IDX_SCRIPT, second);
            if (secondString.length() == 4) {
                outputArray[IDX_SCRIPT] = secondString;
                outputArray[IDX_REGION] = string.substring(second + IDX_SCRIPT, third);
                outputArray[IDX_VARIANT] = string.substring(third + IDX_SCRIPT);
                return;
            }
            outputArray[IDX_REGION] = secondString;
            outputArray[IDX_VARIANT] = string.substring(second + IDX_SCRIPT);
        }
    }

    public static Locale localeFromIcuLocaleId(String localeId) {
        int extensionsIndex = localeId.indexOf(64);
        Map<Character, String> extensionsMap = Collections.EMPTY_MAP;
        Map<String, String> unicodeKeywordsMap = Collections.EMPTY_MAP;
        Set<String> unicodeAttributeSet = Collections.EMPTY_SET;
        if (extensionsIndex != -1) {
            extensionsMap = new HashMap();
            unicodeKeywordsMap = new HashMap();
            unicodeAttributeSet = new HashSet();
            String str = ";";
            String[] extensions = localeId.substring(extensionsIndex + IDX_SCRIPT).split(r23);
            int length = extensions.length;
            for (int i = IDX_LANGUAGE; i < length; i += IDX_SCRIPT) {
                String extension = extensions[i];
                if (extension.startsWith("attribute=")) {
                    String[] split = extension.substring("attribute=".length()).split(LanguageTag.SEP);
                    int length2 = split.length;
                    for (int i2 = IDX_LANGUAGE; i2 < length2; i2 += IDX_SCRIPT) {
                        unicodeAttributeSet.add(split[i2]);
                    }
                } else {
                    int separatorIndex = extension.indexOf(61);
                    if (separatorIndex == IDX_SCRIPT) {
                        String value = extension.substring(IDX_REGION);
                        extensionsMap.put(Character.valueOf(extension.charAt(IDX_LANGUAGE)), value);
                    } else {
                        unicodeKeywordsMap.put(extension.substring(IDX_LANGUAGE, separatorIndex), extension.substring(separatorIndex + IDX_SCRIPT));
                    }
                }
            }
        }
        String[] outputArray = new String[]{XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE, XmlPullParser.NO_NAMESPACE};
        if (extensionsIndex == -1) {
            parseLangScriptRegionAndVariants(localeId, outputArray);
        } else {
            parseLangScriptRegionAndVariants(localeId.substring(IDX_LANGUAGE, extensionsIndex), outputArray);
        }
        Builder builder = new Builder();
        builder.setLanguage(outputArray[IDX_LANGUAGE]);
        builder.setRegion(outputArray[IDX_REGION]);
        builder.setVariant(outputArray[IDX_VARIANT]);
        builder.setScript(outputArray[IDX_SCRIPT]);
        for (String attribute : unicodeAttributeSet) {
            builder.addUnicodeLocaleAttribute(attribute);
        }
        for (Entry<String, String> keyword : unicodeKeywordsMap.entrySet()) {
            builder.setUnicodeLocaleKeyword((String) keyword.getKey(), (String) keyword.getValue());
        }
        for (Entry<Character, String> extension2 : extensionsMap.entrySet()) {
            builder.setExtension(((Character) extension2.getKey()).charValue(), (String) extension2.getValue());
        }
        return builder.build();
    }

    public static Locale[] localesFromStrings(String[] localeNames) {
        LinkedHashSet<Locale> set = new LinkedHashSet();
        int length = localeNames.length;
        for (int i = IDX_LANGUAGE; i < length; i += IDX_SCRIPT) {
            set.add(localeFromIcuLocaleId(localeNames[i]));
        }
        return (Locale[]) set.toArray(new Locale[set.size()]);
    }

    public static Locale[] getAvailableLocales() {
        if (availableLocalesCache == null) {
            availableLocalesCache = localesFromStrings(getAvailableLocalesNative());
        }
        return (Locale[]) availableLocalesCache.clone();
    }

    public static Locale[] getAvailableBreakIteratorLocales() {
        return localesFromStrings(getAvailableBreakIteratorLocalesNative());
    }

    public static Locale[] getAvailableCalendarLocales() {
        return localesFromStrings(getAvailableCalendarLocalesNative());
    }

    public static Locale[] getAvailableCollatorLocales() {
        return localesFromStrings(getAvailableCollatorLocalesNative());
    }

    public static Locale[] getAvailableDateFormatLocales() {
        return localesFromStrings(getAvailableDateFormatLocalesNative());
    }

    public static Locale[] getAvailableDateFormatSymbolsLocales() {
        return getAvailableDateFormatLocales();
    }

    public static Locale[] getAvailableDecimalFormatSymbolsLocales() {
        return getAvailableNumberFormatLocales();
    }

    public static Locale[] getAvailableNumberFormatLocales() {
        return localesFromStrings(getAvailableNumberFormatLocalesNative());
    }

    public static String getBestDateTimePattern(String skeleton, Locale locale) {
        String pattern;
        String languageTag = locale.toLanguageTag();
        String key = skeleton + "\t" + languageTag;
        synchronized (CACHED_PATTERNS) {
            pattern = (String) CACHED_PATTERNS.get(key);
            if (pattern == null) {
                pattern = getBestDateTimePatternNative(skeleton, languageTag);
                CACHED_PATTERNS.put(key, pattern);
            }
        }
        return pattern;
    }

    public static char[] getDateFormatOrder(String pattern) {
        char[] result = new char[IDX_VARIANT];
        int resultIndex = IDX_LANGUAGE;
        boolean sawDay = false;
        boolean sawMonth = false;
        boolean sawYear = false;
        int i = IDX_LANGUAGE;
        while (i < pattern.length()) {
            char ch = pattern.charAt(i);
            if (ch == 'd' || ch == 'L' || ch == 'M' || ch == 'y') {
                int resultIndex2;
                if (ch == 'd' && !sawDay) {
                    resultIndex2 = resultIndex + IDX_SCRIPT;
                    result[resultIndex] = 'd';
                    sawDay = true;
                    resultIndex = resultIndex2;
                } else if ((ch == 'L' || ch == 'M') && !sawMonth) {
                    resultIndex2 = resultIndex + IDX_SCRIPT;
                    result[resultIndex] = 'M';
                    sawMonth = true;
                    resultIndex = resultIndex2;
                } else if (ch == 'y' && !sawYear) {
                    resultIndex2 = resultIndex + IDX_SCRIPT;
                    result[resultIndex] = 'y';
                    sawYear = true;
                    resultIndex = resultIndex2;
                }
            } else if (ch == 'G') {
                continue;
            } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                throw new IllegalArgumentException("Bad pattern character '" + ch + "' in " + pattern);
            } else if (ch != PatternTokenizer.SINGLE_QUOTE) {
                continue;
            } else if (i >= pattern.length() - 1 || pattern.charAt(i + IDX_SCRIPT) != PatternTokenizer.SINGLE_QUOTE) {
                i = pattern.indexOf(39, i + IDX_SCRIPT);
                if (i == -1) {
                    throw new IllegalArgumentException("Bad quoting in " + pattern);
                }
                i += IDX_SCRIPT;
            } else {
                i += IDX_SCRIPT;
            }
            i += IDX_SCRIPT;
        }
        return result;
    }

    public static String toLowerCase(String s, Locale locale) {
        return toLowerCase(s, locale.toLanguageTag());
    }

    public static String toUpperCase(String s, Locale locale) {
        return toUpperCase(s, locale.toLanguageTag());
    }

    public static boolean U_FAILURE(int error) {
        return error > 0;
    }

    public static String getCurrencyDisplayName(Locale locale, String currencyCode) {
        return getCurrencyDisplayName(locale.toLanguageTag(), currencyCode);
    }

    public static String getCurrencySymbol(Locale locale, String currencyCode) {
        return getCurrencySymbol(locale.toLanguageTag(), currencyCode);
    }

    public static String getDisplayCountry(Locale targetLocale, Locale locale) {
        return getDisplayCountryNative(targetLocale.toLanguageTag(), locale.toLanguageTag());
    }

    public static String getDisplayLanguage(Locale targetLocale, Locale locale) {
        return getDisplayLanguageNative(targetLocale.toLanguageTag(), locale.toLanguageTag());
    }

    public static String getDisplayVariant(Locale targetLocale, Locale locale) {
        return getDisplayVariantNative(targetLocale.toLanguageTag(), locale.toLanguageTag());
    }

    public static String getDisplayScript(Locale targetLocale, Locale locale) {
        return getDisplayScriptNative(targetLocale.toLanguageTag(), locale.toLanguageTag());
    }

    public static Locale addLikelySubtags(Locale locale) {
        return Locale.forLanguageTag(addLikelySubtags(locale.toLanguageTag()).replace('_', '-'));
    }
}
