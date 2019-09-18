package android.icu.impl;

import android.icu.impl.locale.BaseLocale;
import android.icu.impl.number.Padder;
import android.icu.text.PluralRanges;
import android.icu.text.PluralRules;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

public class PluralRulesLoader extends PluralRules.Factory {
    private static final PluralRanges UNKNOWN_RANGE = new PluralRanges().freeze();
    public static final PluralRulesLoader loader = new PluralRulesLoader();
    private static Map<String, PluralRanges> localeIdToPluralRanges;
    private Map<String, String> localeIdToCardinalRulesId;
    private Map<String, String> localeIdToOrdinalRulesId;
    private Map<String, ULocale> rulesIdToEquivalentULocale;
    private final Map<String, PluralRules> rulesIdToRules = new HashMap();

    private PluralRulesLoader() {
    }

    public ULocale[] getAvailableULocales() {
        Set<String> keys = getLocaleIdToRulesIdMap(PluralRules.PluralType.CARDINAL).keySet();
        ULocale[] locales = new ULocale[keys.size()];
        int n = 0;
        for (String createCanonical : keys) {
            locales[n] = ULocale.createCanonical(createCanonical);
            n++;
        }
        return locales;
    }

    public ULocale getFunctionalEquivalent(ULocale locale, boolean[] isAvailable) {
        if (isAvailable != null && isAvailable.length > 0) {
            isAvailable[0] = getLocaleIdToRulesIdMap(PluralRules.PluralType.CARDINAL).containsKey(ULocale.canonicalize(locale.getBaseName()));
        }
        String rulesId = getRulesIdForLocale(locale, PluralRules.PluralType.CARDINAL);
        if (rulesId == null || rulesId.trim().length() == 0) {
            return ULocale.ROOT;
        }
        ULocale result = getRulesIdToEquivalentULocaleMap().get(rulesId);
        if (result == null) {
            return ULocale.ROOT;
        }
        return result;
    }

    private Map<String, String> getLocaleIdToRulesIdMap(PluralRules.PluralType type) {
        checkBuildRulesIdMaps();
        return type == PluralRules.PluralType.CARDINAL ? this.localeIdToCardinalRulesId : this.localeIdToOrdinalRulesId;
    }

    private Map<String, ULocale> getRulesIdToEquivalentULocaleMap() {
        checkBuildRulesIdMaps();
        return this.rulesIdToEquivalentULocale;
    }

    private void checkBuildRulesIdMaps() {
        int i;
        boolean haveMap;
        Map<String, String> tempLocaleIdToOrdinalRulesId;
        Map<String, ULocale> tempRulesIdToEquivalentULocale;
        Map<String, String> tempLocaleIdToCardinalRulesId;
        synchronized (this) {
            haveMap = this.localeIdToCardinalRulesId != null;
        }
        if (!haveMap) {
            try {
                UResourceBundle pluralb = getPluralBundle();
                UResourceBundle localeb = pluralb.get("locales");
                tempLocaleIdToCardinalRulesId = new TreeMap<>();
                tempRulesIdToEquivalentULocale = new HashMap<>();
                for (int i2 = 0; i2 < localeb.getSize(); i2++) {
                    UResourceBundle b = localeb.get(i2);
                    String id = b.getKey();
                    String value = b.getString().intern();
                    tempLocaleIdToCardinalRulesId.put(id, value);
                    if (!tempRulesIdToEquivalentULocale.containsKey(value)) {
                        tempRulesIdToEquivalentULocale.put(value, new ULocale(id));
                    }
                }
                UResourceBundle localeb2 = pluralb.get("locales_ordinals");
                tempLocaleIdToOrdinalRulesId = new TreeMap<>();
                for (i = 0; i < localeb2.getSize(); i++) {
                    UResourceBundle b2 = localeb2.get(i);
                    tempLocaleIdToOrdinalRulesId.put(b2.getKey(), b2.getString().intern());
                }
            } catch (MissingResourceException e) {
                tempLocaleIdToCardinalRulesId = Collections.emptyMap();
                tempLocaleIdToOrdinalRulesId = Collections.emptyMap();
                tempRulesIdToEquivalentULocale = Collections.emptyMap();
            }
            Map<String, ULocale> tempRulesIdToEquivalentULocale2 = tempRulesIdToEquivalentULocale;
            synchronized (this) {
                if (this.localeIdToCardinalRulesId == null) {
                    this.localeIdToCardinalRulesId = tempLocaleIdToCardinalRulesId;
                    this.localeIdToOrdinalRulesId = tempLocaleIdToOrdinalRulesId;
                    this.rulesIdToEquivalentULocale = tempRulesIdToEquivalentULocale2;
                }
            }
        }
    }

    public String getRulesIdForLocale(ULocale locale, PluralRules.PluralType type) {
        String rulesId;
        Map<String, String> idMap = getLocaleIdToRulesIdMap(type);
        String localeId = ULocale.canonicalize(locale.getBaseName());
        while (true) {
            String str = idMap.get(localeId);
            rulesId = str;
            if (str != null) {
                break;
            }
            int ix = localeId.lastIndexOf(BaseLocale.SEP);
            if (ix == -1) {
                break;
            }
            localeId = localeId.substring(0, ix);
        }
        return rulesId;
    }

    public PluralRules getRulesForRulesId(String rulesId) {
        boolean hasRules;
        PluralRules rules = null;
        synchronized (this.rulesIdToRules) {
            hasRules = this.rulesIdToRules.containsKey(rulesId);
            if (hasRules) {
                rules = this.rulesIdToRules.get(rulesId);
            }
        }
        if (!hasRules) {
            try {
                UResourceBundle setb = getPluralBundle().get("rules").get(rulesId);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < setb.getSize(); i++) {
                    UResourceBundle b = setb.get(i);
                    if (i > 0) {
                        sb.append("; ");
                    }
                    sb.append(b.getKey());
                    sb.append(PluralRules.KEYWORD_RULE_SEPARATOR);
                    sb.append(b.getString());
                }
                rules = PluralRules.parseDescription(sb.toString());
            } catch (ParseException | MissingResourceException e) {
            }
            synchronized (this.rulesIdToRules) {
                if (this.rulesIdToRules.containsKey(rulesId)) {
                    rules = this.rulesIdToRules.get(rulesId);
                } else {
                    this.rulesIdToRules.put(rulesId, rules);
                }
            }
        }
        return rules;
    }

    public UResourceBundle getPluralBundle() throws MissingResourceException {
        return ICUResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "plurals", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true);
    }

    public PluralRules forLocale(ULocale locale, PluralRules.PluralType type) {
        String rulesId = getRulesIdForLocale(locale, type);
        if (rulesId == null || rulesId.trim().length() == 0) {
            return PluralRules.DEFAULT;
        }
        PluralRules rules = getRulesForRulesId(rulesId);
        if (rules == null) {
            rules = PluralRules.DEFAULT;
        }
        return rules;
    }

    static {
        String[][] pluralRangeData = {new String[]{"locales", "id ja km ko lo ms my th vi zh"}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "am bn fr gu hi hy kn mr pa zu"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "fa"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "ka"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "az de el gl hu it kk ky ml mn ne nl pt sq sw ta te tr ug uz"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "af bg ca en es et eu fi nb sv ur"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "da fil is"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "si"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "mk"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "lv"}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "ro"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "hr sr bs"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "sl"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "he"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "cs pl sk"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "lt ru uk"}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "cy"}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{"locales", "ar"}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ZERO}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_ZERO}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_OTHER}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY}, new String[]{PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER, PluralRules.KEYWORD_OTHER}};
        String[] locales = null;
        HashMap<String, PluralRanges> tempLocaleIdToPluralRanges = new HashMap<>();
        PluralRanges pr = null;
        for (String[] row : pluralRangeData) {
            if (row[0].equals("locales")) {
                if (pr != null) {
                    pr.freeze();
                    for (String locale : locales) {
                        tempLocaleIdToPluralRanges.put(locale, pr);
                    }
                }
                locales = row[1].split(Padder.FALLBACK_PADDING_STRING);
                pr = new PluralRanges();
            } else {
                pr.add(StandardPlural.fromString(row[0]), StandardPlural.fromString(row[1]), StandardPlural.fromString(row[2]));
            }
        }
        for (String locale2 : locales) {
            tempLocaleIdToPluralRanges.put(locale2, pr);
        }
        localeIdToPluralRanges = Collections.unmodifiableMap(tempLocaleIdToPluralRanges);
    }

    public boolean hasOverride(ULocale locale) {
        return false;
    }

    public PluralRanges getPluralRanges(ULocale locale) {
        String localeId = ULocale.canonicalize(locale.getBaseName());
        while (true) {
            PluralRanges pluralRanges = localeIdToPluralRanges.get(localeId);
            PluralRanges result = pluralRanges;
            if (pluralRanges != null) {
                return result;
            }
            int ix = localeId.lastIndexOf(BaseLocale.SEP);
            if (ix == -1) {
                return UNKNOWN_RANGE;
            }
            localeId = localeId.substring(0, ix);
        }
    }

    public boolean isPluralRangesAvailable(ULocale locale) {
        return getPluralRanges(locale) == UNKNOWN_RANGE;
    }
}
