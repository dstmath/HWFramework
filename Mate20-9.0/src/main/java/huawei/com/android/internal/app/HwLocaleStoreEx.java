package huawei.com.android.internal.app;

import android.content.Context;
import android.os.LocaleList;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.app.IHwLocaleStoreInner;
import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore;
import com.huawei.android.app.IHwLocaleStoreEx;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllformedLocaleException;
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
    private static final String GLOBAL_CONFIG = "dialects.xml";
    private static final HashMap<String, String> dialectMap = new HashMap<>();
    private static final Set<String> languagesK = new HashSet();
    private static final HashMap<String, ArrayList<String>> rDialectMap = new HashMap<>();

    public String getFullLanguageName(Context context, Locale locale, Locale displayLocale) {
        String dLocaleStr = getLanguageLocaleStr(context, locale.toLanguageTag());
        if ("en-US".equals(dLocaleStr)) {
            dLocaleStr = "en";
        } else if ("es-Latn".equals(dLocaleStr) || "es-ES".equals(dLocaleStr)) {
            dLocaleStr = "es";
        } else if ("pt-Latn".equals(dLocaleStr) || "pt-PT".equals(dLocaleStr)) {
            dLocaleStr = "pt";
        } else if ("zh-Hant-TW".equals(dLocaleStr)) {
            dLocaleStr = "zh-Hant";
        }
        return LocaleHelper.getDisplayName(Locale.forLanguageTag(dLocaleStr), displayLocale, true);
    }

    private static String getLanguageLocaleStr(Context context, String localeStr) {
        List<String> specialCode = new ArrayList<>();
        specialCode.add("ar-XB");
        specialCode.add("en-XA");
        specialCode.add("zz-ZX");
        initDialectsData(context);
        if (specialCode.contains(localeStr)) {
            return localeStr;
        }
        return dialectMap.containsKey(localeStr) ? dialectMap.get(localeStr) : getMainLocale(localeStr);
    }

    public boolean isSupportRegion(Context context, Locale locale, String region) {
        initDialectsData(context);
        String selectLanguageTag = locale.toLanguageTag();
        String newLocale = getRegionChangeLocale(locale, region).toLanguageTag();
        if (LocalePicker.getBlackLanguage(context).contains(newLocale)) {
            return false;
        }
        if ("ar-XB".equals(selectLanguageTag) || "en-XA".equals(selectLanguageTag)) {
            return true;
        }
        if ("XA".equals(region) || "XB".equals(region)) {
            return false;
        }
        String selectLanguage = getLangScriptLocale(locale).toLanguageTag();
        List<String> languageZG = new ArrayList<>();
        languageZG.add("en");
        languageZG.add("en-Latn");
        languageZG.add("my");
        languageZG.add("zh-Hans");
        languageZG.add("zh-Hant");
        if ("ZG".equals(region) && !languageZG.contains(selectLanguage)) {
            return false;
        }
        if (TextUtils.isEmpty(locale.getCountry()) || !languagesK.contains(selectLanguage)) {
            return true;
        }
        if (dialectMap.containsKey(locale.toLanguageTag())) {
            return rDialectMap.get(dialectMap.get(locale.toLanguageTag())).contains(newLocale);
        }
        return !dialectMap.containsKey(newLocale);
    }

    private static HashMap<String, LocaleStore.LocaleInfo> fillLanguageCache(Context context, IHwLocaleStoreInner iHwLSI) {
        Set<String> simCountries = iHwLSI.getSimCountriesEx(context);
        simCountries.add(Locale.getDefault().getCountry());
        int i = 0;
        boolean isInDeveloperMode = Settings.Global.getInt(context.getContentResolver(), "development_settings_enabled", 0) != 0;
        List<String> relatedLocales = new ArrayList<>();
        String[] supportedLocales = LocalePicker.getSupportedLocales(context);
        int length = supportedLocales.length;
        int i2 = 0;
        while (i2 < length) {
            String localeId = supportedLocales[i2];
            if (!localeId.isEmpty()) {
                if (simCountries.contains(Locale.forLanguageTag(localeId).getCountry())) {
                    relatedLocales.add(dialectMap.containsKey(localeId) ? dialectMap.get(localeId) : getMainLocale(localeId));
                }
                i2++;
            } else {
                throw new IllformedLocaleException("Bad locale entry in locale_config.xml");
            }
        }
        List<Locale> systemAssetLocales = new ArrayList<>();
        for (String sAssetLocale : LocalePicker.getSystemAssetLocales()) {
            systemAssetLocales.add(getLangScriptLocale(LocaleHelper.addLikelySubtags(Locale.forLanguageTag(sAssetLocale))));
        }
        HashMap<String, LocaleStore.LocaleInfo> sLocaleLanguageCache = new HashMap<>();
        String[] supportedLanguagesFromConfigEx = iHwLSI.getSupportedLanguagesFromConfigEx(context);
        int length2 = supportedLanguagesFromConfigEx.length;
        while (i < length2) {
            String localeId2 = supportedLanguagesFromConfigEx[i];
            if (!localeId2.isEmpty()) {
                LocaleStore.LocaleInfo li = iHwLSI.getLanguageLocaleInfo(localeId2);
                if (LocaleList.isPseudoLocale(li.getLocale())) {
                    if (isInDeveloperMode) {
                        li.setTranslated(true);
                        iHwLSI.setPseudo(li, true);
                        iHwLSI.setSuggestionTypeSim(li);
                    } else {
                        i++;
                    }
                }
                for (String tRelatedLocale : relatedLocales) {
                    if (localeId2.contains(tRelatedLocale)) {
                        iHwLSI.setSuggestionTypeSim(li);
                    }
                }
                li.setTranslated(systemAssetLocales.contains(getLangScriptLocale(LocaleHelper.addLikelySubtags(li.getLocale()))));
                sLocaleLanguageCache.put(li.getId(), li);
                i++;
            } else {
                throw new IllformedLocaleException("Bad locale entry in language_lists.xml");
            }
        }
        return sLocaleLanguageCache;
    }

    private static HashMap<String, LocaleStore.LocaleInfo> fillRegionCache(Context context, Locale locale, IHwLocaleStoreInner iHwLSI) {
        IHwLocaleStoreInner iHwLocaleStoreInner = iHwLSI;
        String defLocaleStr = locale.toLanguageTag();
        String tLanguageScript = getLangScriptLocale(locale).toLanguageTag();
        List<String> languageZG = new ArrayList<>();
        languageZG.add("en");
        languageZG.add("en-Latn");
        languageZG.add("my");
        languageZG.add("zh-Hans");
        languageZG.add("zh-Hant");
        Set<String> simCountries = iHwLocaleStoreInner.getSimCountriesEx(context);
        Set<String> countryList = getCountries(context);
        if (languageZG.contains(tLanguageScript)) {
            countryList.add("ZG");
        }
        if ("en-XA".equals(defLocaleStr)) {
            countryList.add("XA");
        } else if ("ar-XB".equals(defLocaleStr)) {
            countryList.add("XB");
        }
        HashMap<String, LocaleStore.LocaleInfo> sLocaleRegionCache = new HashMap<>();
        for (String country : countryList) {
            if (!country.isEmpty()) {
                String localeId = getRegionChangeLocale(locale, country).toLanguageTag();
                if (!LocalePicker.getBlackLanguage(context).contains(localeId)) {
                    if (languagesK.contains(tLanguageScript)) {
                        if (dialectMap.containsKey(defLocaleStr)) {
                            if (!rDialectMap.get(dialectMap.get(defLocaleStr)).contains(localeId)) {
                            }
                        } else if (dialectMap.containsKey(localeId)) {
                        }
                    }
                    LocaleStore.LocaleInfo li = iHwLocaleStoreInner.getLanguageLocaleInfo(localeId);
                    if (simCountries.contains(country)) {
                        iHwLocaleStoreInner.setSuggestionTypeSim(li);
                    }
                    sLocaleRegionCache.put(li.getId(), li);
                }
            } else {
                Locale locale2 = locale;
                throw new IllformedLocaleException("Bad locale entry in locale_config.xml");
            }
        }
        Locale locale3 = locale;
        HashSet<String> localizedLocales = new HashSet<>();
        List<Locale> systemAssetLocales = new ArrayList<>();
        for (String sAssetLocale : LocalePicker.getSystemAssetLocales()) {
            systemAssetLocales.add(LocaleHelper.addLikelySubtags(Locale.forLanguageTag(sAssetLocale)));
        }
        for (LocaleStore.LocaleInfo li2 : sLocaleRegionCache.values()) {
            LocaleStore.LocaleInfo cachedLocale = null;
            if (systemAssetLocales.contains(LocaleHelper.addLikelySubtags(Locale.forLanguageTag(li2.getLocale().toLanguageTag())))) {
                cachedLocale = li2;
            }
            if (cachedLocale != null) {
                iHwLocaleStoreInner.setSuggestionTypeCfg(cachedLocale);
            }
            localizedLocales.add(iHwLocaleStoreInner.getLangScriptKeyEx(li2));
            li2.setTranslated(localizedLocales.contains(iHwLocaleStoreInner.getLangScriptKeyEx(li2)));
            defLocaleStr = defLocaleStr;
        }
        return sLocaleRegionCache;
    }

    public Set<LocaleStore.LocaleInfo> getLanguageLocales(Context context, IHwLocaleStoreInner iHwLSI) {
        initDialectsData(context);
        HashMap<String, LocaleStore.LocaleInfo> sLocaleLanguageCache = fillLanguageCache(context, iHwLSI);
        HashSet<LocaleStore.LocaleInfo> result = new HashSet<>();
        for (LocaleStore.LocaleInfo li : sLocaleLanguageCache.values()) {
            result.add(li);
        }
        return result;
    }

    public Set<LocaleStore.LocaleInfo> getRegionLocales(Context context, Locale locale, IHwLocaleStoreInner iHwLSI) {
        initDialectsData(context);
        HashMap<String, LocaleStore.LocaleInfo> sLocaleRegionCache = fillRegionCache(context, locale, iHwLSI);
        HashSet<LocaleStore.LocaleInfo> result = new HashSet<>();
        for (LocaleStore.LocaleInfo li : sLocaleRegionCache.values()) {
            result.add(li);
        }
        return result;
    }

    private static Set<String> getCountries(Context context) {
        Set<String> countries = new HashSet<>();
        for (String localeId : LocalePicker.getSupportedLocales(context)) {
            countries.add(Locale.forLanguageTag(localeId).getCountry());
        }
        countries.removeAll(LocalePicker.getBlackLanguage(context));
        countries.remove("");
        countries.remove("XA");
        countries.remove("XB");
        countries.remove("ZG");
        countries.remove("ZX");
        return countries;
    }

    private static Locale getLangScriptLocale(Locale locale) {
        return getRegionChangeLocale(locale, "");
    }

    private static Locale getRegionChangeLocale(Locale locale, String region) {
        Locale.Builder localeBuilder = new Locale.Builder().setLanguageTag("en");
        try {
            localeBuilder = new Locale.Builder().setLocale(locale).setRegion(region);
        } catch (IllformedLocaleException e) {
            Log.e("LocaleStore", "Error locale: " + locale.toLanguageTag());
        }
        return localeBuilder.setExtension('u', "").build();
    }

    private static String getMainLocale(String localeId) {
        String langScript = getLangScriptLocale(Locale.forLanguageTag(localeId)).toLanguageTag();
        HashMap<String, String> sameLanguageMap = new HashMap<>();
        sameLanguageMap.put("en", "en-US");
        sameLanguageMap.put("es", "es-ES");
        sameLanguageMap.put("zh-Hant", "zh-Hant-TW");
        return sameLanguageMap.containsKey(langScript) ? sameLanguageMap.get(langScript) : langScript;
    }

    private static Document getDialectsDocument(String fileName, Context context) {
        String str;
        StringBuilder sb;
        InputStream localeInputStream = null;
        Document doc = null;
        try {
            InputStream localeInputStream2 = context.getAssets().open(fileName);
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(localeInputStream2);
            if (localeInputStream2 != null) {
                try {
                    localeInputStream2.close();
                } catch (IOException e) {
                    str = "Fate";
                    sb = new StringBuilder();
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e2) {
            Log.e("Fate", "Can not find the " + fileName + " file");
            if (localeInputStream != null) {
                try {
                    localeInputStream.close();
                } catch (IOException e3) {
                    str = "Fate";
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (localeInputStream != null) {
                try {
                    localeInputStream.close();
                } catch (IOException e4) {
                    Log.e("Fate", "Can not parse the " + fileName + " file");
                }
            }
            throw th;
        }
        return doc;
        sb.append("Can not parse the ");
        sb.append(fileName);
        sb.append(" file");
        Log.e(str, sb.toString());
        return doc;
    }

    private static void initDialectsData(Context context) {
        if (dialectMap.isEmpty()) {
            Document doc = getDialectsDocument(GLOBAL_CONFIG, context);
            if (doc != null) {
                NodeList nl = doc.getElementsByTagName("index");
                int len = nl.getLength();
                for (int i = 0; i < len; i++) {
                    NamedNodeMap map = nl.item(i).getAttributes();
                    String language = map.getNamedItem("language").getNodeValue();
                    String locales = map.getNamedItem("locales").getNodeValue();
                    String[] localeArray = locales.split(" ");
                    ArrayList<String> localeList = new ArrayList<>();
                    localeList.addAll(Arrays.asList(localeArray));
                    rDialectMap.put(language, localeList);
                    languagesK.add(getLangScriptLocale(Locale.forLanguageTag(language)).toLanguageTag());
                    for (String locale : locales.split(" ")) {
                        dialectMap.put(locale, language);
                    }
                }
            }
        }
    }

    public String getDialectsName(Context context, String localeTag) {
        return getLanguageLocaleStr(context, localeTag);
    }
}
