package ohos.global.icu.impl;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.icu.text.PluralRanges;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class PluralRulesLoader extends PluralRules.Factory {
    private static final PluralRanges UNKNOWN_RANGE = new PluralRanges().freeze();
    public static final PluralRulesLoader loader = new PluralRulesLoader();
    private static Map<String, PluralRanges> localeIdToPluralRanges;
    private Map<String, String> localeIdToCardinalRulesId;
    private Map<String, String> localeIdToOrdinalRulesId;
    private Map<String, ULocale> rulesIdToEquivalentULocale;
    private final Map<String, PluralRules> rulesIdToRules = new HashMap();

    public boolean hasOverride(ULocale uLocale) {
        return false;
    }

    private PluralRulesLoader() {
    }

    public ULocale[] getAvailableULocales() {
        Set<String> keySet = getLocaleIdToRulesIdMap(PluralRules.PluralType.CARDINAL).keySet();
        ULocale[] uLocaleArr = new ULocale[keySet.size()];
        int i = 0;
        for (String str : keySet) {
            uLocaleArr[i] = ULocale.createCanonical(str);
            i++;
        }
        return uLocaleArr;
    }

    public ULocale getFunctionalEquivalent(ULocale uLocale, boolean[] zArr) {
        if (zArr != null && zArr.length > 0) {
            zArr[0] = getLocaleIdToRulesIdMap(PluralRules.PluralType.CARDINAL).containsKey(ULocale.canonicalize(uLocale.getBaseName()));
        }
        String rulesIdForLocale = getRulesIdForLocale(uLocale, PluralRules.PluralType.CARDINAL);
        if (rulesIdForLocale == null || rulesIdForLocale.trim().length() == 0) {
            return ULocale.ROOT;
        }
        ULocale uLocale2 = getRulesIdToEquivalentULocaleMap().get(rulesIdForLocale);
        if (uLocale2 == null) {
            return ULocale.ROOT;
        }
        return uLocale2;
    }

    private Map<String, String> getLocaleIdToRulesIdMap(PluralRules.PluralType pluralType) {
        checkBuildRulesIdMaps();
        return pluralType == PluralRules.PluralType.CARDINAL ? this.localeIdToCardinalRulesId : this.localeIdToOrdinalRulesId;
    }

    private Map<String, ULocale> getRulesIdToEquivalentULocaleMap() {
        checkBuildRulesIdMaps();
        return this.rulesIdToEquivalentULocale;
    }

    private void checkBuildRulesIdMaps() {
        int i;
        boolean z;
        Map<String, ULocale> map;
        Map<String, String> map2;
        Map<String, String> map3;
        synchronized (this) {
            z = this.localeIdToCardinalRulesId != null;
        }
        if (!z) {
            try {
                UResourceBundle pluralBundle = getPluralBundle();
                UResourceBundle uResourceBundle = pluralBundle.get("locales");
                map2 = new TreeMap<>();
                map = new HashMap<>();
                for (int i2 = 0; i2 < uResourceBundle.getSize(); i2++) {
                    UResourceBundle uResourceBundle2 = uResourceBundle.get(i2);
                    String key = uResourceBundle2.getKey();
                    String intern = uResourceBundle2.getString().intern();
                    map2.put(key, intern);
                    if (!map.containsKey(intern)) {
                        map.put(intern, new ULocale(key));
                    }
                }
                UResourceBundle uResourceBundle3 = pluralBundle.get("locales_ordinals");
                map3 = new TreeMap<>();
                for (i = 0; i < uResourceBundle3.getSize(); i++) {
                    UResourceBundle uResourceBundle4 = uResourceBundle3.get(i);
                    map3.put(uResourceBundle4.getKey(), uResourceBundle4.getString().intern());
                }
            } catch (MissingResourceException unused) {
                map2 = Collections.emptyMap();
                map3 = Collections.emptyMap();
                map = Collections.emptyMap();
            }
            synchronized (this) {
                if (this.localeIdToCardinalRulesId == null) {
                    this.localeIdToCardinalRulesId = map2;
                    this.localeIdToOrdinalRulesId = map3;
                    this.rulesIdToEquivalentULocale = map;
                }
            }
        }
    }

    public String getRulesIdForLocale(ULocale uLocale, PluralRules.PluralType pluralType) {
        String str;
        int lastIndexOf;
        Map<String, String> localeIdToRulesIdMap = getLocaleIdToRulesIdMap(pluralType);
        String canonicalize = ULocale.canonicalize(uLocale.getBaseName());
        while (true) {
            str = localeIdToRulesIdMap.get(canonicalize);
            if (str != null || (lastIndexOf = canonicalize.lastIndexOf("_")) == -1) {
                break;
            }
            canonicalize = canonicalize.substring(0, lastIndexOf);
        }
        return str;
    }

    public PluralRules getRulesForRulesId(String str) {
        boolean containsKey;
        PluralRules pluralRules;
        synchronized (this.rulesIdToRules) {
            containsKey = this.rulesIdToRules.containsKey(str);
            pluralRules = containsKey ? this.rulesIdToRules.get(str) : null;
        }
        if (!containsKey) {
            try {
                UResourceBundle uResourceBundle = getPluralBundle().get("rules").get(str);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < uResourceBundle.getSize(); i++) {
                    UResourceBundle uResourceBundle2 = uResourceBundle.get(i);
                    if (i > 0) {
                        sb.append("; ");
                    }
                    sb.append(uResourceBundle2.getKey());
                    sb.append(": ");
                    sb.append(uResourceBundle2.getString());
                }
                pluralRules = PluralRules.parseDescription(sb.toString());
            } catch (ParseException | MissingResourceException unused) {
            }
            synchronized (this.rulesIdToRules) {
                if (this.rulesIdToRules.containsKey(str)) {
                    pluralRules = this.rulesIdToRules.get(str);
                } else {
                    this.rulesIdToRules.put(str, pluralRules);
                }
            }
        }
        return pluralRules;
    }

    public UResourceBundle getPluralBundle() throws MissingResourceException {
        return ICUResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "plurals", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true);
    }

    public PluralRules forLocale(ULocale uLocale, PluralRules.PluralType pluralType) {
        String rulesIdForLocale = getRulesIdForLocale(uLocale, pluralType);
        if (rulesIdForLocale == null || rulesIdForLocale.trim().length() == 0) {
            return PluralRules.DEFAULT;
        }
        PluralRules rulesForRulesId = getRulesForRulesId(rulesIdForLocale);
        return rulesForRulesId == null ? PluralRules.DEFAULT : rulesForRulesId;
    }

    static {
        String[][] strArr = {new String[]{"locales", "id ja km ko lo ms my th vi zh"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "am bn fr gu hi hy kn mr pa zu"}, new String[]{"one", "one", "one"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "fa"}, new String[]{"one", "one", Constants.ATTRVAL_OTHER}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "ka"}, new String[]{"one", Constants.ATTRVAL_OTHER, "one"}, new String[]{Constants.ATTRVAL_OTHER, "one", Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "az de el gl hu it kk ky ml mn ne nl pt sq sw ta te tr ug uz"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", "one"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "af bg ca en es et eu fi nb sv ur"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "da fil is"}, new String[]{"one", "one", "one"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", "one"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "si"}, new String[]{"one", "one", "one"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "mk"}, new String[]{"one", "one", Constants.ATTRVAL_OTHER}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "lv"}, new String[]{"zero", "zero", Constants.ATTRVAL_OTHER}, new String[]{"zero", "one", "one"}, new String[]{"zero", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"one", "zero", Constants.ATTRVAL_OTHER}, new String[]{"one", "one", "one"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "zero", Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", "one"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "ro"}, new String[]{"one", "few", "few"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"few", "one", "few"}, new String[]{"few", "few", "few"}, new String[]{"few", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "few", "few"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "hr sr bs"}, new String[]{"one", "one", "one"}, new String[]{"one", "few", "few"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"few", "one", "one"}, new String[]{"few", "few", "few"}, new String[]{"few", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", "one"}, new String[]{Constants.ATTRVAL_OTHER, "few", "few"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "sl"}, new String[]{"one", "one", "few"}, new String[]{"one", "two", "two"}, new String[]{"one", "few", "few"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"two", "one", "few"}, new String[]{"two", "two", "two"}, new String[]{"two", "few", "few"}, new String[]{"two", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"few", "one", "few"}, new String[]{"few", "two", "two"}, new String[]{"few", "few", "few"}, new String[]{"few", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", "few"}, new String[]{Constants.ATTRVAL_OTHER, "two", "two"}, new String[]{Constants.ATTRVAL_OTHER, "few", "few"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "he"}, new String[]{"one", "two", Constants.ATTRVAL_OTHER}, new String[]{"one", "many", "many"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"two", "many", Constants.ATTRVAL_OTHER}, new String[]{"two", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"many", "many", "many"}, new String[]{"many", Constants.ATTRVAL_OTHER, "many"}, new String[]{Constants.ATTRVAL_OTHER, "one", Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "two", Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "many", "many"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "cs pl sk"}, new String[]{"one", "few", "few"}, new String[]{"one", "many", "many"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"few", "few", "few"}, new String[]{"few", "many", "many"}, new String[]{"few", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"many", "one", "one"}, new String[]{"many", "few", "few"}, new String[]{"many", "many", "many"}, new String[]{"many", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", "one"}, new String[]{Constants.ATTRVAL_OTHER, "few", "few"}, new String[]{Constants.ATTRVAL_OTHER, "many", "many"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "lt ru uk"}, new String[]{"one", "one", "one"}, new String[]{"one", "few", "few"}, new String[]{"one", "many", "many"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"few", "one", "one"}, new String[]{"few", "few", "few"}, new String[]{"few", "many", "many"}, new String[]{"few", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"many", "one", "one"}, new String[]{"many", "few", "few"}, new String[]{"many", "many", "many"}, new String[]{"many", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", "one"}, new String[]{Constants.ATTRVAL_OTHER, "few", "few"}, new String[]{Constants.ATTRVAL_OTHER, "many", "many"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "cy"}, new String[]{"zero", "one", "one"}, new String[]{"zero", "two", "two"}, new String[]{"zero", "few", "few"}, new String[]{"zero", "many", "many"}, new String[]{"zero", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"one", "two", "two"}, new String[]{"one", "few", "few"}, new String[]{"one", "many", "many"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"two", "few", "few"}, new String[]{"two", "many", "many"}, new String[]{"two", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"few", "many", "many"}, new String[]{"few", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"many", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", "one"}, new String[]{Constants.ATTRVAL_OTHER, "two", "two"}, new String[]{Constants.ATTRVAL_OTHER, "few", "few"}, new String[]{Constants.ATTRVAL_OTHER, "many", "many"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"locales", "ar"}, new String[]{"zero", "one", "zero"}, new String[]{"zero", "two", "zero"}, new String[]{"zero", "few", "few"}, new String[]{"zero", "many", "many"}, new String[]{"zero", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"one", "two", Constants.ATTRVAL_OTHER}, new String[]{"one", "few", "few"}, new String[]{"one", "many", "many"}, new String[]{"one", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"two", "few", "few"}, new String[]{"two", "many", "many"}, new String[]{"two", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"few", "few", "few"}, new String[]{"few", "many", "many"}, new String[]{"few", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{"many", "few", "few"}, new String[]{"many", "many", "many"}, new String[]{"many", Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "one", Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "two", Constants.ATTRVAL_OTHER}, new String[]{Constants.ATTRVAL_OTHER, "few", "few"}, new String[]{Constants.ATTRVAL_OTHER, "many", "many"}, new String[]{Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER, Constants.ATTRVAL_OTHER}};
        HashMap hashMap = new HashMap();
        String[] strArr2 = null;
        PluralRanges pluralRanges = null;
        for (String[] strArr3 : strArr) {
            if (strArr3[0].equals("locales")) {
                if (pluralRanges != null) {
                    pluralRanges.freeze();
                    for (String str : strArr2) {
                        hashMap.put(str, pluralRanges);
                    }
                }
                strArr2 = strArr3[1].split(" ");
                pluralRanges = new PluralRanges();
            } else {
                pluralRanges.add(StandardPlural.fromString(strArr3[0]), StandardPlural.fromString(strArr3[1]), StandardPlural.fromString(strArr3[2]));
            }
        }
        for (String str2 : strArr2) {
            hashMap.put(str2, pluralRanges);
        }
        localeIdToPluralRanges = Collections.unmodifiableMap(hashMap);
    }

    public PluralRanges getPluralRanges(ULocale uLocale) {
        String canonicalize = ULocale.canonicalize(uLocale.getBaseName());
        while (true) {
            PluralRanges pluralRanges = localeIdToPluralRanges.get(canonicalize);
            if (pluralRanges != null) {
                return pluralRanges;
            }
            int lastIndexOf = canonicalize.lastIndexOf("_");
            if (lastIndexOf == -1) {
                return UNKNOWN_RANGE;
            }
            canonicalize = canonicalize.substring(0, lastIndexOf);
        }
    }

    public boolean isPluralRangesAvailable(ULocale uLocale) {
        return getPluralRanges(uLocale) == UNKNOWN_RANGE;
    }
}
