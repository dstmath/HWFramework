package com.huawei.i18n.taboo;

import android.icu.util.ULocale;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FindTargetRules {
    private static final String CITY_NAME_REGEX = "[^a-zA-Z0-9]+";
    private static final String CITY_NAME_REPLACE = "_";
    private static final String DEFAULT_VALUE = "";
    private static final int DIALECTS_SIZE = 200;
    private static final int FILTER_SIZE = 100;
    private static final Map<String, String> HANT_PARENTS = new HashMap<String, String>() {
        /* class com.huawei.i18n.taboo.FindTargetRules.AnonymousClass1 */

        {
            put("zh-MO", "zh-Hant-HK");
        }
    };
    private static final Map<String, String> LATN_PARENTS = new HashMap<String, String>() {
        /* class com.huawei.i18n.taboo.FindTargetRules.AnonymousClass2 */

        {
            put("en-150", "en-001");
            put("en-AG", "en-001");
            put("en-AI", "en-001");
            put("en-AU", "en-001");
            put("en-BB", "en-001");
            put("en-BE", "en-001");
            put("en-BM", "en-001");
            put("en-BS", "en-001");
            put("en-BZ", "en-001");
            put("en-CC", "en-001");
            put("en-CK", "en-001");
            put("en-CX", "en-001");
            put("en-DG", "en-001");
            put("en-ER", "en-001");
            put("en-FK", "en-001");
            put("en-FM", "en-001");
            put("en-GB", "en-001");
            put("en-GD", "en-001");
            put("en-GG", "en-001");
            put("en-GI", "en-001");
            put("en-GY", "en-001");
            put("en-HK", "en-001");
            put("en-IE", "en-001");
            put("en-IM", "en-001");
            put("en-IN", "en-001");
            put("en-IO", "en-001");
            put("en-JE", "en-001");
            put("en-KI", "en-001");
            put("en-KN", "en-001");
            put("en-KY", "en-001");
            put("en-LC", "en-001");
            put("en-LR", "en-001");
            put("en-LS", "en-001");
            put("en-MM", "en-001");
            put("en-MO", "en-001");
            put("en-MS", "en-001");
            put("en-MT", "en-001");
            put("en-MY", "en-001");
            put("en-NF", "en-001");
            put("en-NR", "en-001");
            put("en-NU", "en-001");
            put("en-NZ", "en-001");
            put("en-PG", "en-001");
            put("en-PK", "en-001");
            put("en-PN", "en-001");
            put("en-PW", "en-001");
            put("en-SB", "en-001");
            put("en-SC", "en-001");
            put("en-SD", "en-001");
            put("en-SG", "en-001");
            put("en-SH", "en-001");
            put("en-SL", "en-001");
            put("en-SS", "en-001");
            put("en-SX", "en-001");
            put("en-SZ", "en-001");
            put("en-TC", "en-001");
            put("en-TK", "en-001");
            put("en-TT", "en-001");
            put("en-TV", "en-001");
            put("en-VC", "en-001");
            put("en-VG", "en-001");
            put("en-WS", "en-001");
            put("en-ZG", "en-001");
            put("es-AR", "es-419");
            put("es-BO", "es-419");
            put("es-BR", "es-419");
            put("es-CL", "es-419");
            put("es-CO", "es-419");
            put("es-CR", "es-419");
            put("es-CU", "es-419");
            put("es-DO", "es-419");
            put("es-EC", "es-419");
            put("es-GT", "es-419");
            put("es-HN", "es-419");
            put("es-MX", "es-419");
            put("es-NI", "es-419");
            put("es-PA", "es-419");
            put("es-PE", "es-419");
            put("es-PR", "es-419");
            put("es-PY", "es-419");
            put("es-SV", "es-419");
            put("es-US", "es-419");
            put("es-UY", "es-419");
            put("es-VE", "es-419");
            put("pt-AO", "pt-PT");
            put("pt-CH", "pt-PT");
            put("pt-CV", "pt-PT");
            put("pt-GQ", "pt-PT");
            put("pt-GW", "pt-PT");
            put("pt-LU", "pt-PT");
            put("pt-MO", "pt-PT");
            put("pt-MZ", "pt-PT");
            put("pt-ST", "pt-PT");
            put("pt-TL", "pt-PT");
        }
    };
    private static final int MCC_SUBSTRING_SIZE = 3;

    public static class SystemParam {
        private String country;
        private String hbcCountry;
        private List<String> mccList = new ArrayList();
        private List<String> mccmncList = new ArrayList();
        private String optb;
        private String optbopta;
        private String region;
        private String vendorCountry;

        public String getVendorCountry() {
            return this.vendorCountry;
        }

        public List<String> getMccmncList() {
            return this.mccmncList;
        }

        public String getOptbopta() {
            return this.optbopta;
        }

        public String getHbcCountry() {
            return this.hbcCountry;
        }

        public List<String> getMccList() {
            return this.mccList;
        }

        public String getOptb() {
            return this.optb;
        }

        public String getCountry() {
            return this.country;
        }

        public String getRegion() {
            return this.region;
        }

        public static SystemParam getSystemParam() {
            String str;
            SystemParam sysParam = new SystemParam();
            sysParam.optb = SystemProperties.get("ro.config.hw_optb", "");
            sysParam.country = SystemProperties.get("ro.product.locale.region", "").toUpperCase(Locale.ENGLISH);
            sysParam.region = SystemProperties.get("ro.config.hw.region", "");
            sysParam.mccList = FindTargetRules.getMccsList();
            sysParam.hbcCountry = SystemProperties.get("hbc.country", "").toUpperCase(Locale.ENGLISH);
            String systemOpta = SystemProperties.get("ro.config.hw_opta", "");
            if ("".equals(sysParam.optb) || "".equals(systemOpta)) {
                str = "";
            } else {
                str = sysParam.optb + "-" + systemOpta;
            }
            sysParam.optbopta = str;
            sysParam.mccmncList = FindTargetRules.getMccmncsList();
            sysParam.vendorCountry = SystemProperties.get("ro.hw.country", "").toUpperCase(Locale.ENGLISH);
            return sysParam;
        }
    }

    /* access modifiers changed from: private */
    public static List<String> getMccsList() {
        List<String> mccList = new ArrayList<>();
        String[] mccs = SystemProperties.get("persist.sys.mcc_match_fyrom").split(",");
        for (String mcc : mccs) {
            if (mcc != null && !"".equals(mcc)) {
                String mcc2 = mcc.substring(0, 3);
                if (!mccList.contains(mcc2)) {
                    mccList.add(mcc2);
                }
            }
        }
        return mccList;
    }

    /* access modifiers changed from: private */
    public static List<String> getMccmncsList() {
        List<String> mccmncList = new ArrayList<>();
        String[] split = SystemProperties.get("gsm.sim.operator.numeric", "").split(",");
        for (String mc : split) {
            if (mc != null && !"".equals(mc) && !mccmncList.contains(mc)) {
                mccmncList.add(mc);
            }
        }
        return mccmncList;
    }

    public static List<String> getSequentialFallBack(SystemParam param, String key) {
        List<String> sequentialFallBack = new ArrayList<>();
        if (param == null || key == null) {
            return sequentialFallBack;
        }
        Iterator it = param.mccmncList.iterator();
        while (it.hasNext()) {
            sequentialFallBack.add(key + "_mccmnc_" + ((String) it.next()));
        }
        Iterator it2 = param.mccList.iterator();
        while (it2.hasNext()) {
            sequentialFallBack.add(key + "_mcc_" + ((String) it2.next()));
        }
        if (param.optbopta != null && !"".equals(param.optbopta)) {
            sequentialFallBack.add(key + "_optbopta_" + param.optbopta);
        }
        if (param.optb != null && !"".equals(param.optb)) {
            sequentialFallBack.add(key + "_optb_" + param.optb);
        }
        if (param.hbcCountry != null && !"".equals(param.hbcCountry)) {
            sequentialFallBack.add(key + "_hc_" + param.hbcCountry);
        }
        if (param.vendorCountry != null && !"".equals(param.vendorCountry)) {
            sequentialFallBack.add(key + "_vc_" + param.vendorCountry);
        }
        if (param.country != null && !"".equals(param.country)) {
            sequentialFallBack.add(key + "_c_" + param.country);
        }
        if (param.region != null && !"".equals(param.region)) {
            sequentialFallBack.add(key + "_r_" + param.region);
        }
        sequentialFallBack.add(key + "_r_all");
        return sequentialFallBack;
    }

    public static String cityNameNormalize(String name) {
        if (name == null) {
            return name;
        }
        return name.replaceAll(CITY_NAME_REGEX, CITY_NAME_REPLACE);
    }

    public static ArrayList<String> getChain(String localeId) {
        HashMap<String, String> dialects = new HashMap<>(200);
        dialects.putAll(HANT_PARENTS);
        for (String key : HANT_PARENTS.keySet()) {
            String[] keyArray = key.split("-");
            if (keyArray.length > 1) {
                dialects.put(keyArray[0] + "-Hant-" + keyArray[1], HANT_PARENTS.get(key));
            }
        }
        dialects.putAll(LATN_PARENTS);
        for (String key2 : LATN_PARENTS.keySet()) {
            String[] keyArray2 = key2.split("-");
            if (keyArray2.length > 1) {
                dialects.put(keyArray2[0] + "-Latn-" + keyArray2[1], LATN_PARENTS.get(key2));
            }
        }
        ArrayList<String> chain = new ArrayList<>();
        if (dialects.containsKey(localeId)) {
            chain.add(localeId);
            chain.addAll(getNormalChain(dialects.get(localeId)));
        } else {
            chain.addAll(getNormalChain(localeId));
        }
        return chain;
    }

    private static ArrayList<String> getNormalChain(String localeId) {
        Set<String> specialLanguages = new HashSet<>();
        specialLanguages.add("sr-Latn");
        specialLanguages.add("zh-Hant");
        ArrayList<String> chain = new ArrayList<>();
        int index = localeId.length();
        while (index != -1 && localeId.length() > 0) {
            localeId = localeId.substring(0, index);
            chain.add(localeId);
            if (specialLanguages.contains(localeId)) {
                break;
            }
            index = localeId.lastIndexOf("-");
        }
        return chain;
    }

    public static String langTagFormat(String localeId) {
        ULocale locale;
        if (localeId == null || localeId.isEmpty() || (locale = ULocale.forLanguageTag(localeId)) == null) {
            return null;
        }
        if (locale.getScript() != null && !locale.getScript().isEmpty()) {
            return null;
        }
        ULocale scriptLocale = ULocale.addLikelySubtags(locale);
        StringBuffer sb = new StringBuffer();
        sb.append(locale.getLanguage());
        sb.append("-");
        sb.append(scriptLocale.getScript());
        if (locale.getCountry() != null && !locale.getCountry().isEmpty()) {
            sb.append("-");
            sb.append(locale.getCountry());
        }
        return sb.toString();
    }

    public static List<Map.Entry<String, Integer>> getFilterList(List<String> list, Locale locale) {
        HashMap<String, Integer> filter = new HashMap<>(100);
        for (String loc : list) {
            filter.put(loc, Integer.valueOf(computeMatchScore(locale, Locale.forLanguageTag(loc))));
        }
        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(filter.entrySet());
        Collections.sort(sortList, new Comparator<Map.Entry<String, Integer>>() {
            /* class com.huawei.i18n.taboo.FindTargetRules.AnonymousClass3 */

            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                if (o2.getValue().compareTo(o1.getValue()) != 0) {
                    return o2.getValue().compareTo(o1.getValue());
                }
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        return sortList;
    }

    private static int computeMatchScore(Locale locale, Locale res) {
        String localeLanguage = locale.getLanguage();
        String resLanguage = res.getLanguage();
        if (localeLanguage.isEmpty() || resLanguage.isEmpty() || !isSameLanguage(localeLanguage, resLanguage)) {
            return -1;
        }
        int score = 0 + 3;
        String localeTag = locale.toLanguageTag();
        String resTag = res.toLanguageTag();
        HashMap<String, String> dialects = getDialectMap();
        if (dialects.containsKey(localeTag) && resTag.equals(dialects.get(localeTag))) {
            return 8;
        }
        String localeScript = locale.getScript();
        if (localeScript.isEmpty()) {
            localeScript = ULocale.addLikelySubtags(ULocale.forLocale(locale)).getScript();
        }
        String resScript = res.getScript();
        if (resScript.isEmpty()) {
            resScript = ULocale.addLikelySubtags(ULocale.forLocale(res)).getScript();
        }
        String resRegion = res.getCountry();
        if ((!localeLanguage.equals("en") || !isSameEnglishScript(localeScript, resScript)) && !localeScript.equals(resScript)) {
            return -1;
        }
        int score2 = score + 3;
        if (resRegion.isEmpty()) {
            score2++;
        }
        String localeRegion = locale.getCountry();
        if (localeRegion.isEmpty() || !localeRegion.equals(resRegion)) {
            return score2;
        }
        return score2 + 3;
    }

    private static HashMap<String, String> getDialectMap() {
        HashMap<String, String> dialects = new HashMap<>(200);
        dialects.putAll(HANT_PARENTS);
        for (String key : HANT_PARENTS.keySet()) {
            String[] keyArray = key.split("-");
            if (keyArray.length > 1) {
                dialects.put(keyArray[0] + "-Hant-" + keyArray[1], HANT_PARENTS.get(key));
            }
        }
        dialects.putAll(LATN_PARENTS);
        for (String key2 : LATN_PARENTS.keySet()) {
            String[] keyArray2 = key2.split("-");
            if (keyArray2.length > 1) {
                dialects.put(keyArray2[0] + "-Latn-" + keyArray2[1], LATN_PARENTS.get(key2));
            }
            if (key2.contains("en-") && keyArray2.length > 1) {
                dialects.put(keyArray2[0] + "-Qaag-" + keyArray2[1], LATN_PARENTS.get(key2));
            }
        }
        return dialects;
    }

    private static boolean isSameEnglishScript(String script1, String script2) {
        return script1.replace("Qaag", "Latn").equals(script2.replace("Qaag", "Latn"));
    }

    private static boolean isSameLanguage(String language1, String langauge2) {
        return language1.replace("tl", "fil").equals(langauge2.replace("tl", "fil"));
    }
}
