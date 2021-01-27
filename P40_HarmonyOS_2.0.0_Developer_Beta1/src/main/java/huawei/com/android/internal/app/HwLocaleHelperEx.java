package huawei.com.android.internal.app;

import android.content.Context;
import android.content.res.Resources;
import android.icu.util.ULocale;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.internal.app.IHwLocaleStoreInner;
import com.android.internal.app.LocaleStore;
import com.huawei.android.app.IHwLocaleHelperEx;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.taboo.TabooReader;
import huawei.android.provider.HwSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class HwLocaleHelperEx implements IHwLocaleHelperEx {
    private static final boolean IS_DT = "150".equals(SystemProperties.get("ro.config.hw_opta", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF));
    private static final boolean IS_HIDE_COUNTRY_NAME = SystemProperties.getBoolean("ro.config.hw_hide_country_name", false);
    private static final boolean IS_MKD = "807".equals(SystemProperties.get("ro.config.hw_optb", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF));
    private static final String TAG = "HwLocaleHelperEx";
    IHwLocaleStoreInner mLocaleStoreInner;

    public HwLocaleHelperEx() {
        this(null);
    }

    public HwLocaleHelperEx(IHwLocaleStoreInner inner) {
        this.mLocaleStoreInner = inner;
    }

    public List<String> getRelatedLocalesEx() {
        String[] localeArray = Resources.getSystem().getStringArray(17236107);
        List<String> relatedLocales = new ArrayList<>();
        relatedLocales.addAll(Arrays.asList(localeArray));
        return relatedLocales;
    }

    public int getCompareIntEx(LocaleStore.LocaleInfo firLocaleInfo, LocaleStore.LocaleInfo secLocaleInfo, List<String> relatedLocales) {
        String firStr = firLocaleInfo.getId();
        String secStr = secLocaleInfo.getId();
        if (firStr.startsWith("en") || firStr.startsWith("es") || firStr.startsWith("pt")) {
            firStr = firStr.replace("-Latn", StorageManagerExt.INVALID_KEY_DESC);
            secStr = secStr.replace("-Latn", StorageManagerExt.INVALID_KEY_DESC);
        }
        if (!this.mLocaleStoreInner.isSuggestedLocale(firLocaleInfo) || (this.mLocaleStoreInner.getSuggestionTypeSim(firLocaleInfo) == this.mLocaleStoreInner.getSuggestionTypeSim(secLocaleInfo) && relatedLocales.contains(firStr) == relatedLocales.contains(secStr))) {
            return 0;
        }
        if (this.mLocaleStoreInner.getSuggestionTypeSim(firLocaleInfo)) {
            return relatedLocales.contains(firStr) ? -1 : 1;
        }
        if (this.mLocaleStoreInner.getSuggestionTypeSim(secLocaleInfo)) {
            return relatedLocales.contains(secStr) ? 1 : -1;
        }
        return 0;
    }

    public ArrayList<String> getBlackLocales(Context context) {
        ArrayList<String> blacks = new ArrayList<>();
        blacks.addAll(getBlackRegionsPart(context));
        blacks.addAll(getBlackLocalesPart(context));
        return blacks;
    }

    public static ArrayList<String> getBlackAllRegionsPart(Context context, Locale locale) {
        return getBlackAllRegionsPart(context, locale, false);
    }

    private static ArrayList<String> getBlackAllRegionsPart(Context context, Locale locale, boolean useTabooData) {
        ArrayList<String> blackRegions = new ArrayList<>();
        blackRegions.addAll(getBlackRegionsPart(context, useTabooData));
        ULocale giveLikelyLocale = ULocale.addLikelySubtags(ULocale.forLocale(locale));
        String giveLanguage = giveLikelyLocale.getLanguage();
        String giveScript = giveLikelyLocale.getScript();
        Iterator<String> it = getBlackLocalesPart(context, useTabooData).iterator();
        while (it.hasNext()) {
            ULocale blackLikelyLocale = ULocale.addLikelySubtags(ULocale.forLanguageTag(it.next()));
            if (giveLanguage.equals(blackLikelyLocale.getLanguage()) && giveScript.equals(blackLikelyLocale.getScript())) {
                blackRegions.add(blackLikelyLocale.getCountry());
            }
        }
        return blackRegions;
    }

    public static ArrayList<String> getTabooBlackAllRegionsPart(Context context, Locale locale) {
        return getBlackAllRegionsPart(context, locale, true);
    }

    public static ArrayList<String> getTabooBlackLangs2String(Context context) {
        ArrayList<String> blackLanguages = new ArrayList<>();
        String blackString = getBlackString(context, true);
        if (blackString != null) {
            for (String localeStr : blackString.split(",")) {
                String localeStr2 = localeStr.trim();
                if (localeStr2.endsWith("*")) {
                    blackLanguages.add(localeStr2.replace("-*", StorageManagerExt.INVALID_KEY_DESC));
                }
            }
        }
        return blackLanguages;
    }

    public static ArrayList<Locale> getBlackLangsPart(Context context) {
        return getBlackLangsPart(context, false);
    }

    private static ArrayList<Locale> getBlackLangsPart(Context context, boolean useTabooData) {
        ArrayList<Locale> blackLanguages = new ArrayList<>();
        String blackString = getBlackString(context, useTabooData);
        if (blackString != null) {
            for (String localeStr : blackString.split(",")) {
                String localeStr2 = localeStr.trim();
                if (localeStr2.endsWith("*")) {
                    blackLanguages.add(Locale.forLanguageTag(localeStr2.replace("-*", StorageManagerExt.INVALID_KEY_DESC)));
                }
            }
        }
        return blackLanguages;
    }

    public static ArrayList<Locale> getTabooBlackLangsPart(Context context) {
        return getBlackLangsPart(context, true);
    }

    public static ArrayList<String> getBlackRegionsPart(Context context) {
        return getBlackRegionsPart(context, false);
    }

    private static ArrayList<String> getBlackRegionsPart(Context context, boolean useTabooData) {
        ArrayList<String> blackRegions = new ArrayList<>();
        String blackString = getBlackString(context, useTabooData);
        if (blackString != null) {
            for (String localeStr : blackString.split(",")) {
                String localeStr2 = localeStr.trim();
                if (localeStr2.startsWith("*")) {
                    blackRegions.add(localeStr2.split("-")[1]);
                }
            }
        }
        return blackRegions;
    }

    public static ArrayList<String> getBlackLocalesPart(Context context) {
        return getBlackLocalesPart(context, false);
    }

    private static ArrayList<String> getBlackLocalesPart(Context context, boolean useTabooData) {
        ArrayList<String> blackLocales = new ArrayList<>();
        String blackString = getBlackString(context, useTabooData);
        if (blackString == null) {
            return blackLocales;
        }
        for (String localeStr : blackString.split(",")) {
            String localeStr2 = localeStr.trim();
            if (!localeStr2.startsWith("*") && !localeStr2.endsWith("*")) {
                Locale locale = Locale.forLanguageTag(localeStr2);
                if (!locale.getLanguage().isEmpty() && !locale.getCountry().isEmpty()) {
                    blackLocales.add(localeStr2);
                }
            }
        }
        return blackLocales;
    }

    private static String getBlackString(Context context, boolean useTabooData) {
        String cloudBlackString;
        String blackString = null;
        if (useTabooData) {
            cloudBlackString = TabooReader.getValue(TabooReader.ParamType.TABOO_BLACK_LANG, null, null);
        } else {
            blackString = Settings.System.getString(context.getContentResolver(), "black_languages");
            cloudBlackString = TabooReader.getValue(TabooReader.ParamType.BLACK_LANG, null, null);
        }
        if (cloudBlackString != null) {
            blackString = cloudBlackString;
        }
        if (blackString != null) {
            return blackString.replace("tl", "fil").replace("_", "-");
        }
        return blackString;
    }

    public static ArrayList<String> getBlackCities(Context context) {
        ArrayList<String> blackCitys = new ArrayList<>();
        String tabooCitys = TabooReader.getValue(TabooReader.ParamType.BLACK_CITY, null, null);
        if (tabooCitys != null) {
            blackCitys.addAll(Arrays.asList(tabooCitys.split(",")));
            return blackCitys;
        }
        String blackStrings = Settings.System.getString(context.getContentResolver(), "black_timezone_cities");
        if (blackStrings != null) {
            blackCitys.addAll(Arrays.asList(blackStrings.split(",")));
        }
        return blackCitys;
    }

    public static String getCityName(String city, Locale displayLocale) {
        return TabooReader.getValue(TabooReader.ParamType.CITY_NAME, displayLocale, city);
    }

    private String replaceMyDisplayName(Locale locale, Locale displayLocale, String display) {
        Locale displayLocale2;
        Locale systemLocale = Locale.getDefault();
        if (!"my".equals(locale.getLanguage()) || !"my".equals(displayLocale.getLanguage())) {
            return display;
        }
        if ("Qaag".equals(systemLocale.getScript())) {
            displayLocale2 = Locale.forLanguageTag("my-Qaag");
        } else {
            displayLocale2 = Locale.forLanguageTag("my");
        }
        return ULocale.getDisplayName(locale.toLanguageTag(), ULocale.forLocale(displayLocale2));
    }

    public String replaceCountryName(Locale locale, Locale displayLocale, String giveString) {
        if (locale == null || displayLocale == null || giveString == null || displayLocale.getLanguage().isEmpty()) {
            return giveString;
        }
        ULocale uDisplayLocale = ULocale.forLocale(displayLocale);
        String localeTag = locale.toLanguageTag();
        if (TextUtils.isEmpty(locale.getLanguage())) {
            localeTag = "en-" + locale.getCountry();
        }
        if (!giveString.equals(ULocale.getDisplayCountry(localeTag, uDisplayLocale))) {
            return giveString;
        }
        String display = replaceMKCountryName(locale, displayLocale, giveString);
        String tabooCountry = TabooReader.getValue(TabooReader.ParamType.REGION_NAME, displayLocale, locale.getCountry());
        return tabooCountry != null ? tabooCountry : display;
    }

    private static String replaceMKCountryName(Locale locale, Locale displayLocale, String country) {
        if (!"MK".equals(locale.getCountry()) || !"en".equals(displayLocale.getLanguage())) {
            return country;
        }
        return "North Macedonia";
    }

    public String replaceDisplayName(Locale locale, Locale displayLocale, String result) {
        String localeTag;
        if (locale == null || displayLocale == null || result == null || locale.getLanguage().isEmpty() || displayLocale.getLanguage().isEmpty()) {
            return result;
        }
        String display = replaceMKDisplayName(locale, displayLocale, replaceTestLocaleDisplayName(locale, replaceMyDisplayName(locale, displayLocale, result)));
        String changeName = TabooReader.getValue(TabooReader.ParamType.LANGUAGE_NAME, displayLocale, locale.toLanguageTag());
        if (TextUtils.isEmpty(changeName)) {
            if (TextUtils.isEmpty(locale.getScript())) {
                localeTag = locale.getLanguage();
            } else {
                localeTag = locale.getLanguage() + "-" + locale.getScript();
            }
            changeName = TabooReader.getValue(TabooReader.ParamType.LANGUAGE_NAME, displayLocale, localeTag);
        }
        if (!TextUtils.isEmpty(changeName)) {
            String transTaboo = changeName.replaceAll("[\\(|፣|\\)|،|,|、|‘|（|，|）|၊]", "_");
            String transDisplay = display.replaceAll("[\\(|፣|\\)|،|,|、|‘|（|，|）|၊]", "_");
            String[] tabooArray = transTaboo.split("_");
            String[] displayArray = transDisplay.split("_");
            if (tabooArray.length > 0 && displayArray.length > 0 && tabooArray[0] != null && displayArray[0] != null) {
                display = display.replaceFirst(displayArray[0], tabooArray[0]);
            }
            if (tabooArray.length > 1 && displayArray.length > 1 && tabooArray[1] != null && displayArray[1] != null) {
                display = display.replaceFirst(displayArray[1], tabooArray[1]);
            }
        }
        if (!TextUtils.isEmpty(locale.getCountry())) {
            display = replaceDisplayCountryName(locale, displayLocale, display);
        }
        return replaceParentheses(display);
    }

    private String replaceDisplayCountryName(Locale locale, Locale displayLocale, String result) {
        ULocale uDisplayLocale = ULocale.forLocale(displayLocale);
        String icuCountry = formatParentheses(ULocale.getDisplayCountry(locale.getLanguage() + "-" + locale.getCountry(), uDisplayLocale));
        String tabooCountry = TabooReader.getValue(TabooReader.ParamType.REGION_NAME, displayLocale, locale.getCountry());
        if (tabooCountry == null) {
            return result;
        }
        String tabooCountry2 = formatParentheses(tabooCountry);
        int lastIndex = result.lastIndexOf(icuCountry);
        if (lastIndex == -1) {
            return result;
        }
        String endDisplay = result.substring(lastIndex);
        String startDisplay = result.substring(0, lastIndex);
        String endDisplay2 = endDisplay.replace(icuCountry, tabooCountry2);
        return startDisplay + endDisplay2;
    }

    private String formatParentheses(String country) {
        if (country.contains("（")) {
            return country.replace("（", "［").replace("）", "］");
        }
        return country.replace("(", "[").replace(")", "]");
    }

    private String replaceParentheses(String display) {
        if (!IS_HIDE_COUNTRY_NAME || display == null) {
            return display;
        }
        String replaceDisplay = display.replace("(", "（");
        String[] str = replaceDisplay.split("（");
        if (str.length > 0) {
            return str[0];
        }
        return replaceDisplay;
    }

    private static String replaceMKDisplayName(Locale locale, Locale displayLocale, String result) {
        String replaceStr = result;
        String[] specialCode3 = {"mk_MK", "mk"};
        String[] specialNames3 = {"FYROM", "FYROM"};
        if ("MK".equals(locale.getCountry()) && "en".equals(displayLocale.getLanguage())) {
            replaceStr = replaceStr.replace("Macedonia (FYROM)", "North Macedonia");
        }
        if ((!isGreeceSIM() || (IS_DT && IS_MKD)) && (!IS_DT || IS_MKD)) {
            return replaceStr;
        }
        return getCustomDisplayName(locale, specialCode3, specialNames3, result);
    }

    private static boolean isGreeceSIM() {
        ArrayList<String> mccList = new ArrayList<>();
        mccList.add("202");
        String simOperator = SystemProperties.get("persist.sys.mcc_match_fyrom");
        if (simOperator == null || simOperator.length() < 4) {
            return false;
        }
        if (simOperator.charAt(0) == ',') {
            simOperator = simOperator.substring(1);
        }
        if (mccList.contains(simOperator.substring(0, 3))) {
            return true;
        }
        return false;
    }

    private static String replaceTestLocaleDisplayName(Locale locale, String display) {
        return getCustomDisplayName(locale, new String[]{"ar_XB", "en_XA", "zz_ZX", "zz"}, new String[]{"[Bidirection test locale]", "[Pseudo locale]", "[DBID version]", "[DBID version]"}, display);
    }

    private static String getCustomDisplayName(Locale locale, String[] specialLocaleCodes, String[] specialLocaleNames, String originalStr) {
        String code = locale.toString();
        for (int i = 0; i < specialLocaleCodes.length; i++) {
            if (specialLocaleCodes[i].equals(code)) {
                return specialLocaleNames[i];
            }
        }
        return originalStr;
    }
}
