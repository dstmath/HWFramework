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

public final class ICU {
    private static final BasicLruCache<String, String> CACHED_PATTERNS = new BasicLruCache(8);
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
        int second = string.indexOf(95, first + 1);
        int third = string.indexOf(95, second + 1);
        String secondString;
        if (first == -1) {
            outputArray[0] = string;
        } else if (second == -1) {
            outputArray[0] = string.substring(0, first);
            secondString = string.substring(first + 1);
            if (secondString.length() == 4) {
                outputArray[1] = secondString;
            } else if (secondString.length() == 2 || secondString.length() == 3) {
                outputArray[2] = secondString;
            } else {
                outputArray[3] = secondString;
            }
        } else if (third == -1) {
            outputArray[0] = string.substring(0, first);
            secondString = string.substring(first + 1, second);
            String thirdString = string.substring(second + 1);
            if (secondString.length() == 4) {
                outputArray[1] = secondString;
                if (thirdString.length() == 2 || thirdString.length() == 3 || thirdString.isEmpty()) {
                    outputArray[2] = thirdString;
                } else {
                    outputArray[3] = thirdString;
                }
            } else if (secondString.isEmpty() || secondString.length() == 2 || secondString.length() == 3) {
                outputArray[2] = secondString;
                outputArray[3] = thirdString;
            } else {
                outputArray[3] = string.substring(first + 1);
            }
        } else {
            outputArray[0] = string.substring(0, first);
            secondString = string.substring(first + 1, second);
            if (secondString.length() == 4) {
                outputArray[1] = secondString;
                outputArray[2] = string.substring(second + 1, third);
                outputArray[3] = string.substring(third + 1);
                return;
            }
            outputArray[2] = secondString;
            outputArray[3] = string.substring(second + 1);
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
            String[] extensions = localeId.substring(extensionsIndex + 1).split(";");
            int i = 0;
            int length = extensions.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                String extension = extensions[i2];
                if (extension.startsWith("attribute=")) {
                    for (String unicodeAttribute : extension.substring("attribute=".length()).split(LanguageTag.SEP)) {
                        unicodeAttributeSet.add(unicodeAttribute);
                    }
                } else {
                    int separatorIndex = extension.indexOf(61);
                    if (separatorIndex == 1) {
                        extensionsMap.put(Character.valueOf(extension.charAt(0)), extension.substring(2));
                    } else {
                        unicodeKeywordsMap.put(extension.substring(0, separatorIndex), extension.substring(separatorIndex + 1));
                    }
                }
                i = i2 + 1;
            }
        }
        String[] outputArray = new String[]{"", "", "", ""};
        if (extensionsIndex == -1) {
            parseLangScriptRegionAndVariants(localeId, outputArray);
        } else {
            parseLangScriptRegionAndVariants(localeId.substring(0, extensionsIndex), outputArray);
        }
        Builder builder = new Builder();
        builder.setLanguage(outputArray[0]);
        builder.setRegion(outputArray[2]);
        builder.setVariant(outputArray[3]);
        builder.setScript(outputArray[1]);
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
        for (String localeName : localeNames) {
            set.add(localeFromIcuLocaleId(localeName));
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
        char[] result = new char[3];
        int resultIndex = 0;
        boolean sawDay = false;
        boolean sawMonth = false;
        boolean sawYear = false;
        int i = 0;
        while (i < pattern.length()) {
            char ch = pattern.charAt(i);
            if (ch == 'd' || ch == 'L' || ch == 'M' || ch == 'y') {
                int resultIndex2;
                if (ch == 'd' && (sawDay ^ 1) != 0) {
                    resultIndex2 = resultIndex + 1;
                    result[resultIndex] = 'd';
                    sawDay = true;
                    resultIndex = resultIndex2;
                } else if ((ch == 'L' || ch == 'M') && (sawMonth ^ 1) != 0) {
                    resultIndex2 = resultIndex + 1;
                    result[resultIndex] = 'M';
                    sawMonth = true;
                    resultIndex = resultIndex2;
                } else if (ch == 'y' && (sawYear ^ 1) != 0) {
                    resultIndex2 = resultIndex + 1;
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
            } else if (i >= pattern.length() - 1 || pattern.charAt(i + 1) != PatternTokenizer.SINGLE_QUOTE) {
                i = pattern.indexOf(39, i + 1);
                if (i == -1) {
                    throw new IllegalArgumentException("Bad quoting in " + pattern);
                }
                i++;
            } else {
                i++;
            }
            i++;
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
