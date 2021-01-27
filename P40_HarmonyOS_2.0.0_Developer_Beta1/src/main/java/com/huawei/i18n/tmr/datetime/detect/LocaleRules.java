package com.huawei.i18n.tmr.datetime.detect;

import java.util.Arrays;
import java.util.HashMap;

public class LocaleRules {
    private HashMap<String, String> localesRules = null;
    private HashMap<Integer, String> rules = null;

    public LocaleRules(Rules rules2) {
        this.rules = rules2.getRules();
        init();
    }

    public HashMap<Integer, String> getLocaleRules(String locale) {
        String keysStr;
        HashMap<Integer, String> localeRules = new HashMap<>();
        if (this.localesRules.containsKey(locale) && (keysStr = this.localesRules.get(locale)) != null) {
            for (String strKey : Arrays.asList(keysStr.split(","))) {
                Integer key = Integer.valueOf(strKey.trim());
                localeRules.put(key, this.rules.get(key));
            }
        }
        localeRules.putAll(getOthers(locale));
        return localeRules;
    }

    private void init() {
        if (this.rules != null) {
            this.localesRules = new HashMap<String, String>() {
                /* class com.huawei.i18n.tmr.datetime.detect.LocaleRules.AnonymousClass1 */

                {
                    put("sv", "21006,21007");
                    put("ar", "21008,21009");
                    put("he", "21010,21018");
                    put("da", "21005,21017");
                    put("th", "31007,21011");
                    put("ja", "31001,31006,21014,41001,21002");
                    put("hu", "31001");
                    put("ko", "31001,31004,21015,21016,41001,21002");
                    put("zh_hans", "31001,31005,21014,41001,31009");
                    put("fr", "31001,31003,31018");
                    put("vi", "21012,21013,41002");
                    put("tr", "31001,21003");
                    put("nb", "21004");
                    put("eu", "21021,21019,21023");
                    put("km", "21020");
                    put("ur", "21008");
                    put("lt", "21022,21023,21028");
                    put("bo", "21024,31008,41003");
                    put("si", "31001,21028");
                    put("my", "21025,21026,41004");
                    put("lv", "21027,41005");
                    put("kk", "31001,21033,21034,21035,21029");
                    put("lo", "21011,21032,31007,31016");
                    put("be", "21030,21033,21034,31010");
                    put("ne", "31012,31011");
                    put("bn", "31012,31011");
                    put("jv", "31013,31014");
                    put("fil", "21031,31013,31015,41006");
                    put("en", "31008");
                    put("am", "21011,21036");
                    put("ml", "21037,21035");
                    put("mi", "21038,41007");
                    put("ta", "31001");
                    put("mn", "21039,21040");
                    put("sw", "41006");
                    put("ug", "21041,31017,41008");
                }
            };
        }
    }

    private HashMap<Integer, String> getOthers(String locale) {
        HashMap<Integer, String> map = new HashMap<>();
        if (!Arrays.asList("zh_hans", "ja", "ko").contains(locale)) {
            map.put(31002, this.rules.get(31002));
        }
        return map;
    }
}
