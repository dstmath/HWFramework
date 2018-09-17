package android.icu.impl;

import android.icu.impl.locale.BaseLocale;
import android.icu.text.PluralRanges;
import android.icu.text.PluralRules;
import android.icu.text.PluralRules.Factory;
import android.icu.text.PluralRules.PluralType;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

public class PluralRulesLoader extends Factory {
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
        Set<String> keys = getLocaleIdToRulesIdMap(PluralType.CARDINAL).keySet();
        ULocale[] locales = new ULocale[keys.size()];
        int n = 0;
        for (String createCanonical : keys) {
            int n2 = n + 1;
            locales[n] = ULocale.createCanonical(createCanonical);
            n = n2;
        }
        return locales;
    }

    public ULocale getFunctionalEquivalent(ULocale locale, boolean[] isAvailable) {
        if (isAvailable != null && isAvailable.length > 0) {
            isAvailable[0] = getLocaleIdToRulesIdMap(PluralType.CARDINAL).containsKey(ULocale.canonicalize(locale.getBaseName()));
        }
        String rulesId = getRulesIdForLocale(locale, PluralType.CARDINAL);
        if (rulesId == null || rulesId.trim().length() == 0) {
            return ULocale.ROOT;
        }
        ULocale result = (ULocale) getRulesIdToEquivalentULocaleMap().get(rulesId);
        if (result == null) {
            return ULocale.ROOT;
        }
        return result;
    }

    private Map<String, String> getLocaleIdToRulesIdMap(PluralType type) {
        checkBuildRulesIdMaps();
        return type == PluralType.CARDINAL ? this.localeIdToCardinalRulesId : this.localeIdToOrdinalRulesId;
    }

    private Map<String, ULocale> getRulesIdToEquivalentULocaleMap() {
        checkBuildRulesIdMaps();
        return this.rulesIdToEquivalentULocale;
    }

    private void checkBuildRulesIdMaps() {
        boolean haveMap;
        synchronized (this) {
            haveMap = this.localeIdToCardinalRulesId != null;
        }
        if (!haveMap) {
            Map<String, String> tempLocaleIdToCardinalRulesId;
            Map<String, ULocale> tempRulesIdToEquivalentULocale;
            Map<String, String> tempLocaleIdToOrdinalRulesId;
            try {
                int i;
                UResourceBundle b;
                UResourceBundle pluralb = getPluralBundle();
                UResourceBundle localeb = pluralb.get("locales");
                tempLocaleIdToCardinalRulesId = new TreeMap();
                tempRulesIdToEquivalentULocale = new HashMap();
                for (i = 0; i < localeb.getSize(); i++) {
                    b = localeb.get(i);
                    String id = b.getKey();
                    String value = b.getString().intern();
                    tempLocaleIdToCardinalRulesId.put(id, value);
                    if (!tempRulesIdToEquivalentULocale.containsKey(value)) {
                        tempRulesIdToEquivalentULocale.put(value, new ULocale(id));
                    }
                }
                localeb = pluralb.get("locales_ordinals");
                tempLocaleIdToOrdinalRulesId = new TreeMap();
                for (i = 0; i < localeb.getSize(); i++) {
                    b = localeb.get(i);
                    tempLocaleIdToOrdinalRulesId.put(b.getKey(), b.getString().intern());
                }
            } catch (MissingResourceException e) {
                tempLocaleIdToCardinalRulesId = Collections.emptyMap();
                tempLocaleIdToOrdinalRulesId = Collections.emptyMap();
                tempRulesIdToEquivalentULocale = Collections.emptyMap();
            }
            synchronized (this) {
                if (this.localeIdToCardinalRulesId == null) {
                    this.localeIdToCardinalRulesId = tempLocaleIdToCardinalRulesId;
                    this.localeIdToOrdinalRulesId = tempLocaleIdToOrdinalRulesId;
                    this.rulesIdToEquivalentULocale = tempRulesIdToEquivalentULocale;
                }
            }
        }
    }

    public String getRulesIdForLocale(ULocale locale, PluralType type) {
        String rulesId;
        Map<String, String> idMap = getLocaleIdToRulesIdMap(type);
        String localeId = ULocale.canonicalize(locale.getBaseName());
        while (true) {
            rulesId = (String) idMap.get(localeId);
            if (rulesId != null) {
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
        PluralRules pluralRules = null;
        synchronized (this.rulesIdToRules) {
            hasRules = this.rulesIdToRules.containsKey(rulesId);
            if (hasRules) {
                pluralRules = (PluralRules) this.rulesIdToRules.get(rulesId);
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
                pluralRules = PluralRules.parseDescription(sb.toString());
            } catch (ParseException e) {
            } catch (MissingResourceException e2) {
            }
            synchronized (this.rulesIdToRules) {
                if (this.rulesIdToRules.containsKey(rulesId)) {
                    pluralRules = (PluralRules) this.rulesIdToRules.get(rulesId);
                } else {
                    this.rulesIdToRules.put(rulesId, pluralRules);
                }
            }
        }
        return pluralRules;
    }

    public UResourceBundle getPluralBundle() throws MissingResourceException {
        return ICUResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "plurals", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true);
    }

    public PluralRules forLocale(ULocale locale, PluralType type) {
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
        int i = 0;
        pluralRangeData = new String[171][];
        pluralRangeData[0] = new String[]{"locales", "id ja km ko lo ms my th vi zh"};
        pluralRangeData[1] = new String[]{"other", "other", "other"};
        pluralRangeData[2] = new String[]{"locales", "am bn fr gu hi hy kn mr pa zu"};
        pluralRangeData[3] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[4] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[5] = new String[]{"other", "other", "other"};
        pluralRangeData[6] = new String[]{"locales", "fa"};
        pluralRangeData[7] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, "other"};
        pluralRangeData[8] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[9] = new String[]{"other", "other", "other"};
        pluralRangeData[10] = new String[]{"locales", "ka"};
        pluralRangeData[11] = new String[]{PluralRules.KEYWORD_ONE, "other", PluralRules.KEYWORD_ONE};
        pluralRangeData[12] = new String[]{"other", PluralRules.KEYWORD_ONE, "other"};
        pluralRangeData[13] = new String[]{"other", "other", "other"};
        pluralRangeData[14] = new String[]{"locales", "az de el gl hu it kk ky ml mn ne nl pt sq sw ta te tr ug uz"};
        pluralRangeData[15] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[16] = new String[]{"other", PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[17] = new String[]{"other", "other", "other"};
        pluralRangeData[18] = new String[]{"locales", "af bg ca en es et eu fi nb sv ur"};
        pluralRangeData[19] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[20] = new String[]{"other", PluralRules.KEYWORD_ONE, "other"};
        pluralRangeData[21] = new String[]{"other", "other", "other"};
        pluralRangeData[22] = new String[]{"locales", "da fil is"};
        pluralRangeData[23] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[24] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[25] = new String[]{"other", PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[26] = new String[]{"other", "other", "other"};
        pluralRangeData[27] = new String[]{"locales", "si"};
        pluralRangeData[28] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[29] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[30] = new String[]{"other", PluralRules.KEYWORD_ONE, "other"};
        pluralRangeData[31] = new String[]{"other", "other", "other"};
        pluralRangeData[32] = new String[]{"locales", "mk"};
        pluralRangeData[33] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, "other"};
        pluralRangeData[34] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[35] = new String[]{"other", PluralRules.KEYWORD_ONE, "other"};
        pluralRangeData[36] = new String[]{"other", "other", "other"};
        pluralRangeData[37] = new String[]{"locales", "lv"};
        pluralRangeData[38] = new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_ZERO, "other"};
        pluralRangeData[39] = new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[40] = new String[]{PluralRules.KEYWORD_ZERO, "other", "other"};
        pluralRangeData[41] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ZERO, "other"};
        pluralRangeData[42] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[43] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[44] = new String[]{"other", PluralRules.KEYWORD_ZERO, "other"};
        pluralRangeData[45] = new String[]{"other", PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[46] = new String[]{"other", "other", "other"};
        pluralRangeData[47] = new String[]{"locales", "ro"};
        pluralRangeData[48] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[49] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[50] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW};
        pluralRangeData[51] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[52] = new String[]{PluralRules.KEYWORD_FEW, "other", "other"};
        pluralRangeData[53] = new String[]{"other", PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[54] = new String[]{"other", "other", "other"};
        pluralRangeData[55] = new String[]{"locales", "hr sr bs"};
        pluralRangeData[56] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[57] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[58] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[59] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[60] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[61] = new String[]{PluralRules.KEYWORD_FEW, "other", "other"};
        pluralRangeData[62] = new String[]{"other", PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[63] = new String[]{"other", PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[64] = new String[]{"other", "other", "other"};
        pluralRangeData[65] = new String[]{"locales", "sl"};
        pluralRangeData[66] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW};
        pluralRangeData[67] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO};
        pluralRangeData[68] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[69] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[70] = new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW};
        pluralRangeData[71] = new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO};
        pluralRangeData[72] = new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[73] = new String[]{PluralRules.KEYWORD_TWO, "other", "other"};
        pluralRangeData[74] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW};
        pluralRangeData[75] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO};
        pluralRangeData[76] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[77] = new String[]{PluralRules.KEYWORD_FEW, "other", "other"};
        pluralRangeData[78] = new String[]{"other", PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW};
        pluralRangeData[79] = new String[]{"other", PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO};
        pluralRangeData[80] = new String[]{"other", PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[81] = new String[]{"other", "other", "other"};
        pluralRangeData[82] = new String[]{"locales", "he"};
        pluralRangeData[83] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_TWO, "other"};
        pluralRangeData[84] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[85] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[86] = new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_MANY, "other"};
        pluralRangeData[87] = new String[]{PluralRules.KEYWORD_TWO, "other", "other"};
        pluralRangeData[88] = new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[89] = new String[]{PluralRules.KEYWORD_MANY, "other", PluralRules.KEYWORD_MANY};
        pluralRangeData[90] = new String[]{"other", PluralRules.KEYWORD_ONE, "other"};
        pluralRangeData[91] = new String[]{"other", PluralRules.KEYWORD_TWO, "other"};
        pluralRangeData[92] = new String[]{"other", PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[93] = new String[]{"other", "other", "other"};
        pluralRangeData[94] = new String[]{"locales", "cs pl sk"};
        pluralRangeData[95] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[96] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[97] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[98] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[99] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[100] = new String[]{PluralRules.KEYWORD_FEW, "other", "other"};
        pluralRangeData[101] = new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[102] = new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[103] = new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[104] = new String[]{PluralRules.KEYWORD_MANY, "other", "other"};
        pluralRangeData[105] = new String[]{"other", PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[106] = new String[]{"other", PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[107] = new String[]{"other", PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[108] = new String[]{"other", "other", "other"};
        pluralRangeData[109] = new String[]{"locales", "lt ru uk"};
        pluralRangeData[110] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[111] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[112] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[113] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[114] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[115] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[116] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[117] = new String[]{PluralRules.KEYWORD_FEW, "other", "other"};
        pluralRangeData[118] = new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[119] = new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[120] = new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[121] = new String[]{PluralRules.KEYWORD_MANY, "other", "other"};
        pluralRangeData[122] = new String[]{"other", PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[123] = new String[]{"other", PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[124] = new String[]{"other", PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[125] = new String[]{"other", "other", "other"};
        pluralRangeData[126] = new String[]{"locales", "cy"};
        pluralRangeData[127] = new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[128] = new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO};
        pluralRangeData[129] = new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[130] = new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[131] = new String[]{PluralRules.KEYWORD_ZERO, "other", "other"};
        pluralRangeData[132] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO};
        pluralRangeData[133] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[134] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[135] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[136] = new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[137] = new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[138] = new String[]{PluralRules.KEYWORD_TWO, "other", "other"};
        pluralRangeData[139] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[140] = new String[]{PluralRules.KEYWORD_FEW, "other", "other"};
        pluralRangeData[141] = new String[]{PluralRules.KEYWORD_MANY, "other", "other"};
        pluralRangeData[142] = new String[]{"other", PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ONE};
        pluralRangeData[143] = new String[]{"other", PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_TWO};
        pluralRangeData[144] = new String[]{"other", PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[145] = new String[]{"other", PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[146] = new String[]{"other", "other", "other"};
        pluralRangeData[147] = new String[]{"locales", "ar"};
        pluralRangeData[148] = new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_ZERO};
        pluralRangeData[149] = new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_ZERO};
        pluralRangeData[150] = new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[151] = new String[]{PluralRules.KEYWORD_ZERO, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[152] = new String[]{PluralRules.KEYWORD_ZERO, "other", "other"};
        pluralRangeData[153] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_TWO, "other"};
        pluralRangeData[154] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[155] = new String[]{PluralRules.KEYWORD_ONE, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[156] = new String[]{PluralRules.KEYWORD_ONE, "other", "other"};
        pluralRangeData[157] = new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[158] = new String[]{PluralRules.KEYWORD_TWO, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[159] = new String[]{PluralRules.KEYWORD_TWO, "other", "other"};
        pluralRangeData[160] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[161] = new String[]{PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[162] = new String[]{PluralRules.KEYWORD_FEW, "other", "other"};
        pluralRangeData[163] = new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[164] = new String[]{PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[165] = new String[]{PluralRules.KEYWORD_MANY, "other", "other"};
        pluralRangeData[166] = new String[]{"other", PluralRules.KEYWORD_ONE, "other"};
        pluralRangeData[167] = new String[]{"other", PluralRules.KEYWORD_TWO, "other"};
        pluralRangeData[168] = new String[]{"other", PluralRules.KEYWORD_FEW, PluralRules.KEYWORD_FEW};
        pluralRangeData[169] = new String[]{"other", PluralRules.KEYWORD_MANY, PluralRules.KEYWORD_MANY};
        pluralRangeData[170] = new String[]{"other", "other", "other"};
        PluralRanges pr = null;
        String[] locales = null;
        HashMap<String, PluralRanges> tempLocaleIdToPluralRanges = new HashMap();
        for (String[] row : pluralRangeData) {
            if (row[0].equals("locales")) {
                if (pr != null) {
                    pr.freeze();
                    for (String locale : locales) {
                        tempLocaleIdToPluralRanges.put(locale, pr);
                    }
                }
                locales = row[1].split(" ");
                pr = new PluralRanges();
            } else {
                pr.add(StandardPlural.fromString(row[0]), StandardPlural.fromString(row[1]), StandardPlural.fromString(row[2]));
            }
        }
        int length = locales.length;
        while (i < length) {
            tempLocaleIdToPluralRanges.put(locales[i], pr);
            i++;
        }
        localeIdToPluralRanges = Collections.unmodifiableMap(tempLocaleIdToPluralRanges);
    }

    public boolean hasOverride(ULocale locale) {
        return false;
    }

    public PluralRanges getPluralRanges(ULocale locale) {
        String localeId = ULocale.canonicalize(locale.getBaseName());
        while (true) {
            PluralRanges result = (PluralRanges) localeIdToPluralRanges.get(localeId);
            if (result != null) {
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
