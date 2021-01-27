package huawei.com.android.internal.app;

import android.app.ActivityThread;
import android.content.Context;
import android.icu.text.NumberingSystem;
import android.icu.util.ULocale;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.app.IHwLocaleStoreInner;
import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore;
import com.huawei.android.app.IHwLocaleStoreEx;
import com.huawei.android.os.storage.StorageManagerExt;
import huawei.android.provider.HanziToPinyin;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HwLocaleStoreEx implements IHwLocaleStoreEx {
    private static final HashMap<String, ArrayList<String>> ARRAY_DIALECT_MAP = new HashMap<>();
    private static final String BIDIRECTION_TEST_LOCALE = "ar-XB";
    private static final String CHINESE_HANT = "zh-Hant";
    private static final String CHINESE_HANT_TW = "zh-Hant-TW";
    private static final String DBID = "zz-ZX";
    private static final HashMap<String, String> DIALECT_MAP = new HashMap<>();
    private static final String ENGLISH = "en";
    private static final String ENGLISH_US = "en-US";
    private static final String GLOBAL_CONFIG = "dialects.xml";
    private static final String HBC_COUNTRY = "hbc.country";
    private static final String INDEX = "index";
    private static final String LANGUAGE = "language";
    private static final Set<String> LANGUAGES_K = new HashSet();
    private static final String LATN = "latn";
    private static final String LOCALES = "locales";
    private static final String NATIVE = "native";
    private static final String NUMBERS = "numbers";
    private static final String NUMBER_LOCALE_LIST = "ar,as,bn,fa,mr,my,ne,ur";
    private static final String PORTUGAL = "pt";
    private static final String PORTUGUESE = "pt-PT";
    private static final String PORTUGUESE_BRAZIL = "pt-BR";
    private static final String PORTUGUESE_LATN = "pt-Latn";
    private static final String PSEUDO_LOCALE = "en-XA";
    private static final String SPANISH = "es";
    private static final String SPANISH_ES = "es-ES";
    private static final String SPANISH_LATIN_AMERICA = "es-419";
    private static final String SPANISH_LATN = "es-Latn";
    private static final String TAG = "HwLocaleStoreEx";
    private static final String TW_VERSION = "TW";
    private static final String XA = "XA";
    private static final String XB = "XB";
    private static final String ZG = "ZG";
    private static final String ZX = "ZX";

    public String getFullLanguageName(Context context, Locale locale, Locale displayLocale) {
        if (locale == null || displayLocale == null) {
            return StorageManagerExt.INVALID_KEY_DESC;
        }
        String localeStr = getLanguageLocaleStr(context, locale.toLanguageTag());
        if (ENGLISH_US.equals(localeStr)) {
            localeStr = ENGLISH;
        } else if (SPANISH_LATN.equals(localeStr) || SPANISH_ES.equals(localeStr)) {
            localeStr = SPANISH;
        } else if (PORTUGUESE_LATN.equals(localeStr) || PORTUGUESE.equals(localeStr)) {
            localeStr = PORTUGAL;
        } else if (CHINESE_HANT_TW.equals(localeStr)) {
            localeStr = CHINESE_HANT;
        }
        return LocaleHelper.getDisplayName(Locale.forLanguageTag(localeStr), displayLocale, true);
    }

    private static String getLanguageLocaleStr(Context context, String localeStr) {
        List<String> specialCode = new ArrayList<>();
        specialCode.add(BIDIRECTION_TEST_LOCALE);
        specialCode.add(PSEUDO_LOCALE);
        specialCode.add(DBID);
        initDialectsData(context);
        if (specialCode.contains(localeStr)) {
            return localeStr;
        }
        return DIALECT_MAP.containsKey(localeStr) ? DIALECT_MAP.get(localeStr) : getMainLocale(localeStr);
    }

    public boolean isSupportRegion(Context context, Locale locale, String region) {
        if (locale == null || region == null) {
            return false;
        }
        initDialectsData(context);
        String selectLanguageTag = locale.toLanguageTag();
        String newLocale = getRegionChangeLocale(locale, region).toLanguageTag();
        if (HwLocaleHelperEx.getBlackLocalesPart(context).contains(newLocale)) {
            return false;
        }
        if (BIDIRECTION_TEST_LOCALE.equals(selectLanguageTag) || PSEUDO_LOCALE.equals(selectLanguageTag)) {
            return true;
        }
        if (XA.equals(region) || XB.equals(region)) {
            return false;
        }
        String selectLanguage = getLangScriptLocale(locale).toLanguageTag();
        if (TextUtils.isEmpty(locale.getCountry()) || !LANGUAGES_K.contains(selectLanguage)) {
            return true;
        }
        if (!DIALECT_MAP.containsKey(locale.toLanguageTag()) || ARRAY_DIALECT_MAP.get(DIALECT_MAP.get(locale.toLanguageTag())) == null) {
            return !DIALECT_MAP.containsKey(newLocale);
        }
        return ARRAY_DIALECT_MAP.get(DIALECT_MAP.get(locale.toLanguageTag())).contains(newLocale);
    }

    private static HashMap<String, LocaleStore.LocaleInfo> fillLanguageCache(Context context, IHwLocaleStoreInner inner) {
        Set<String> simCountries = inner.getSimCountriesEx(context);
        simCountries.add(Locale.getDefault().getCountry());
        simCountries.removeAll(HwLocaleHelperEx.getBlackRegionsPart(context));
        boolean isInDeveloperMode = Settings.Global.getInt(context.getContentResolver(), "development_settings_enabled", 0) != 0;
        List<String> relatedLocales = new ArrayList<>();
        ArrayList<String> blackList = HwLocaleHelperEx.getBlackLocalesPart(context);
        String[] supportedLocales = LocalePicker.getSupportedLocales(context);
        for (String localeId : supportedLocales) {
            localeIdIsEmpty(localeId);
            if (!blackList.contains(localeId) && simCountries.contains(Locale.forLanguageTag(localeId).getCountry())) {
                relatedLocales.add(DIALECT_MAP.containsKey(localeId) ? DIALECT_MAP.get(localeId) : getMainLocale(localeId));
            }
        }
        List<Locale> systemAssetLocales = new ArrayList<>();
        for (String assetLocale : LocalePicker.getSystemAssetLocales()) {
            systemAssetLocales.add(getLangScriptLocale(LocaleHelper.addLikelySubtags(Locale.forLanguageTag(assetLocale))));
        }
        HashMap<String, LocaleStore.LocaleInfo> localeLanguageCache = new HashMap<>(100);
        String[] supportLanguages = getSupportedLanguagesFromConfig(context);
        for (String localeId2 : supportLanguages) {
            localeIdIsEmpty(localeId2);
            LocaleStore.LocaleInfo li = inner.getLanguageLocaleInfo(localeId2);
            if (LocaleList.isPseudoLocale(li.getLocale())) {
                if (isInDeveloperMode && !blackList.contains(li.toString())) {
                    li.setTranslated(true);
                    inner.setPseudo(li, true);
                    inner.setSuggestionTypeSim(li);
                }
            }
            for (String relatedLocale : relatedLocales) {
                if (localeId2.contains(relatedLocale)) {
                    inner.setSuggestionTypeSim(li);
                }
            }
            li.setTranslated(systemAssetLocales.contains(getLangScriptLocale(LocaleHelper.addLikelySubtags(li.getLocale()))));
            localeLanguageCache.put(li.getId(), li);
        }
        return localeLanguageCache;
    }

    private static String[] getSupportedLanguagesFromConfig(Context context) {
        Set<String> realList = new HashSet<>();
        realList.addAll(LocalePicker.getRealLocaleListStaticEx(context, context.getResources().getStringArray(33816600)));
        if (TW_VERSION.equals(SystemProperties.get(HBC_COUNTRY, StorageManagerExt.INVALID_KEY_DESC).toUpperCase(Locale.ENGLISH))) {
            realList.remove(PORTUGUESE_BRAZIL);
            realList.remove(SPANISH_LATIN_AMERICA);
        }
        return (String[]) realList.toArray(new String[realList.size()]);
    }

    private static HashMap<String, LocaleStore.LocaleInfo> fillRegionCache(Context context, Locale locale, IHwLocaleStoreInner inner) {
        String defLocaleStr = locale.toLanguageTag();
        String languageScript = getLangScriptLocale(locale).toLanguageTag();
        Set<String> countryList = getValidCountries(context, locale);
        HashMap<String, LocaleStore.LocaleInfo> localeRegionCache = new HashMap<>(230);
        if (countryList.isEmpty()) {
            try {
                throw new IllformedLocaleException("Bad locale entry in locale_config.xml");
            } catch (IllformedLocaleException e) {
                Log.e(TAG, "Bad locale entry in locale_config.xml", e);
            }
        }
        Set<String> simCountries = inner.getSimCountriesEx(context);
        for (String country : countryList) {
            String localeId = getRegionChangeLocale(locale, country).toLanguageTag();
            if (!LANGUAGES_K.contains(languageScript) || ((!DIALECT_MAP.containsKey(defLocaleStr) || ARRAY_DIALECT_MAP.get(DIALECT_MAP.get(defLocaleStr)) == null || ARRAY_DIALECT_MAP.get(DIALECT_MAP.get(defLocaleStr)).contains(localeId)) && (DIALECT_MAP.containsKey(defLocaleStr) || !DIALECT_MAP.containsKey(localeId)))) {
                LocaleStore.LocaleInfo li = inner.getLanguageLocaleInfo(localeId);
                if (simCountries.contains(country)) {
                    inner.setSuggestionTypeSim(li);
                }
                localeRegionCache.put(li.getId(), li);
            }
        }
        checkIsSuggested(localeRegionCache, languageScript, inner);
        return localeRegionCache;
    }

    private static Set<String> getValidCountries(Context context, Locale locale) {
        Set<String> countryList = getCountries(context);
        String defLocaleStr = locale.toLanguageTag();
        countryList.removeAll(HwLocaleHelperEx.getBlackAllRegionsPart(context, locale));
        if (PSEUDO_LOCALE.equals(defLocaleStr)) {
            countryList.add(XA);
        } else if (BIDIRECTION_TEST_LOCALE.equals(defLocaleStr)) {
            countryList.add(XB);
        }
        return countryList;
    }

    private static void checkIsSuggested(HashMap<String, LocaleStore.LocaleInfo> localeRegionCache, String languageScript, IHwLocaleStoreInner inner) {
        List<Locale> systemAssetLocales = new ArrayList<>();
        for (String assetLocale : LocalePicker.getSystemAssetLocales()) {
            systemAssetLocales.add(LocaleHelper.addLikelySubtags(Locale.forLanguageTag(assetLocale)));
        }
        boolean isTranslated = false;
        Iterator<Locale> it = systemAssetLocales.iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().toLanguageTag().contains(languageScript)) {
                    isTranslated = true;
                    break;
                }
            } else {
                break;
            }
        }
        for (LocaleStore.LocaleInfo li : localeRegionCache.values()) {
            if (systemAssetLocales.contains(LocaleHelper.addLikelySubtags(li.getLocale()))) {
                inner.setSuggestionTypeCfg(li);
            }
            li.setTranslated(isTranslated);
        }
    }

    private static void localeIdIsEmpty(String localeId) {
        if (localeId.isEmpty()) {
            try {
                throw new IllformedLocaleException("Bad locale entry in locale_config.xml or language_lists.xml");
            } catch (IllformedLocaleException e) {
                Log.e(TAG, "Bad locale entry in locale_config.xml or language_lists.xml", e);
            }
        }
    }

    public Set<LocaleStore.LocaleInfo> getLanguageLocales(Context context, IHwLocaleStoreInner inner) {
        initDialectsData(context);
        HashSet<LocaleStore.LocaleInfo> result = new HashSet<>();
        if (context == null || inner == null) {
            return result;
        }
        for (LocaleStore.LocaleInfo li : fillLanguageCache(context, inner).values()) {
            result.add(li);
        }
        return result;
    }

    public Set<LocaleStore.LocaleInfo> getRegionLocales(Context context, Locale locale, IHwLocaleStoreInner inner) {
        HashSet<LocaleStore.LocaleInfo> result = new HashSet<>();
        if (context == null || locale == null || inner == null) {
            return result;
        }
        initDialectsData(context);
        for (LocaleStore.LocaleInfo li : fillRegionCache(context, locale, inner).values()) {
            result.add(li);
        }
        return result;
    }

    private static Set<String> getCountries(Context context) {
        Set<String> countries = new HashSet<>();
        List<String> illegalCountries = Arrays.asList(StorageManagerExt.INVALID_KEY_DESC, XA, XB, ZG, ZX);
        for (String localeId : LocalePicker.getSupportedLocales(context)) {
            String country = Locale.forLanguageTag(localeId).getCountry();
            if (!illegalCountries.contains(country)) {
                countries.add(country);
            }
        }
        return countries;
    }

    private static Locale getLangScriptLocale(Locale locale) {
        return getRegionChangeLocale(locale, StorageManagerExt.INVALID_KEY_DESC);
    }

    private static Locale getRegionChangeLocale(Locale locale, String region) {
        Locale.Builder localeBuilder = new Locale.Builder().setLanguageTag(ENGLISH);
        try {
            localeBuilder = new Locale.Builder().setLocale(locale).setRegion(region);
        } catch (IllformedLocaleException e) {
            Log.e(TAG, "Error locale: " + locale.toLanguageTag());
        }
        return localeBuilder.setExtension('u', StorageManagerExt.INVALID_KEY_DESC).build();
    }

    private static String getMainLocale(String localeId) {
        Locale locale = Locale.forLanguageTag(localeId);
        HashMap<String, String> sameLanguageMap = new HashMap<>(3);
        sameLanguageMap.put(ENGLISH, ENGLISH_US);
        sameLanguageMap.put(SPANISH, SPANISH_ES);
        sameLanguageMap.put(CHINESE_HANT, CHINESE_HANT_TW);
        String langScript = getLangScriptLocale(locale).toLanguageTag();
        return sameLanguageMap.containsKey(langScript) ? sameLanguageMap.get(langScript) : langScript;
    }

    private static Document getDialectsDocument(String fileName, Context context) {
        StringBuilder sb;
        InputStream localeInputStream = null;
        Document doc = null;
        try {
            localeInputStream = context.getAssets().open(fileName);
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(localeInputStream);
            if (localeInputStream != null) {
                try {
                    localeInputStream.close();
                } catch (IOException e) {
                    sb = new StringBuilder();
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e2) {
            Log.e(TAG, "Can not find the " + fileName + " file");
            if (localeInputStream != null) {
                try {
                    localeInputStream.close();
                } catch (IOException e3) {
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (localeInputStream != null) {
                try {
                    localeInputStream.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Can not parse the " + fileName + " file");
                }
            }
            throw th;
        }
        return doc;
        sb.append("Can not parse the ");
        sb.append(fileName);
        sb.append(" file");
        Log.e(TAG, sb.toString());
        return doc;
    }

    private static void initDialectsData(Context context) {
        Document doc;
        if (DIALECT_MAP.isEmpty() && (doc = getDialectsDocument(GLOBAL_CONFIG, context)) != null) {
            NodeList nl = doc.getElementsByTagName(INDEX);
            int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                NamedNodeMap map = nl.item(i).getAttributes();
                String language = map.getNamedItem(LANGUAGE).getNodeValue();
                String locales = map.getNamedItem(LOCALES).getNodeValue();
                String[] localeArray = locales.split(HanziToPinyin.Token.SEPARATOR);
                ArrayList<String> localeList = new ArrayList<>();
                localeList.addAll(Arrays.asList(localeArray));
                ARRAY_DIALECT_MAP.put(language, localeList);
                LANGUAGES_K.add(getLangScriptLocale(Locale.forLanguageTag(language)).toLanguageTag());
                for (String locale : locales.split(HanziToPinyin.Token.SEPARATOR)) {
                    DIALECT_MAP.put(locale, language);
                }
            }
        }
    }

    public String getDialectsName(Context context, String localeTag) {
        return getLanguageLocaleStr(context, localeTag);
    }

    public String[] getNumberSystems(Locale locale) {
        String[] numberSystem = new String[0];
        if (locale == null) {
            return numberSystem;
        }
        List<String> numberNameList = getNumberSystemName(locale);
        List<String> numberSystemList = new ArrayList<>();
        for (String numberName : numberNameList) {
            String numberDes = NumberingSystem.getInstance(getNumberSystemLocale(locale, numberName)).getDescription();
            numberSystemList.add(numberName);
            numberSystemList.add(numberDes);
        }
        return (String[]) numberSystemList.toArray(new String[numberSystemList.size()]);
    }

    public boolean isSupportNativeNumber(Locale locale) {
        List<String> numberLocaleList;
        if (locale != null && (numberLocaleList = getNumberLocaleList()) != null && numberLocaleList.contains(locale.getLanguage()) && !LocaleList.isPseudoLocale(locale)) {
            return true;
        }
        return false;
    }

    public boolean updateSystemNumber(String name) {
        Context context = ActivityThread.currentApplication();
        if (context == null || context.getPackageManager().checkPermission("android.permission.CHANGE_CONFIGURATION", context.getPackageName()) != 0) {
            return false;
        }
        List<String> numberLocaleList = getNumberLocaleList();
        Locale defaultLocale = Locale.getDefault();
        String language = defaultLocale.getLanguage();
        List<Locale> listTemp = new ArrayList<>();
        if (numberLocaleList == null || !numberLocaleList.contains(language) || !getNumberSystemName(defaultLocale).contains(name)) {
            return false;
        }
        listTemp.add(getNumberSystemLocale(defaultLocale, name));
        LocaleList localeList = LocalePicker.getLocales();
        int size = localeList.size();
        if (size == 0) {
            return false;
        }
        for (int i = 1; i < size; i++) {
            if (!listTemp.contains(localeList.get(i))) {
                listTemp.add(localeList.get(i));
            }
        }
        LocalePicker.updateLocales(new LocaleList((Locale[]) listTemp.toArray(new Locale[listTemp.size()])));
        return true;
    }

    public Locale removeSystemNumber(Locale locale) {
        new Locale.Builder();
        try {
            return new Locale.Builder().setLocale(locale).setExtension('u', StorageManagerExt.INVALID_KEY_DESC).build();
        } catch (IllformedLocaleException e) {
            Log.e(TAG, "Error locale: " + Locale.getDefault().toLanguageTag());
            return locale;
        }
    }

    private Locale getNumberSystemLocale(Locale locale, String extension) {
        new Locale.Builder();
        try {
            Locale.Builder localeBuilder = new Locale.Builder().setLocale(locale);
            return localeBuilder.setExtension('u', "nu-" + extension).build();
        } catch (IllformedLocaleException e) {
            Log.e(TAG, "Error locale: " + Locale.getDefault().toLanguageTag());
            return locale;
        }
    }

    private List<String> getNumberSystemName(Locale locale) {
        NumberingSystem system = NumberingSystem.getInstance(ULocale.forLocale(locale).setKeywordValue(NUMBERS, NATIVE));
        List<String> numberNameList = new ArrayList<>();
        numberNameList.add(LATN);
        numberNameList.add(system.getName());
        return numberNameList;
    }

    private static List<String> getNumberLocaleList() {
        return Arrays.asList(NUMBER_LOCALE_LIST.split(","));
    }
}
