package com.huawei.g11n.tmr.datetime.detect;

import java.util.Arrays;
import java.util.HashMap;

public class LocaleRules {
    private HashMap<String, HashMap<Integer, String>> locale_rules = null;
    /* access modifiers changed from: private */
    public HashMap<Integer, String> rules = null;

    public LocaleRules(Rules r) {
        this.rules = r.getRules();
        init();
    }

    public HashMap<Integer, String> getLocaleRules(String locale) {
        HashMap<Integer, String> localeRules = new HashMap<>();
        if (this.locale_rules.containsKey(locale)) {
            localeRules.putAll(this.locale_rules.get(locale));
        }
        localeRules.putAll(getOthers(locale));
        return localeRules;
    }

    private void init() {
        if (this.rules != null) {
            this.locale_rules = new HashMap<String, HashMap<Integer, String>>() {
                {
                    put("sv", new HashMap<Integer, String>() {
                        {
                            put(21006, (String) LocaleRules.this.rules.get(21006));
                            put(21007, (String) LocaleRules.this.rules.get(21007));
                        }
                    });
                    put("ar", new HashMap<Integer, String>() {
                        {
                            put(21008, (String) LocaleRules.this.rules.get(21008));
                            put(21009, (String) LocaleRules.this.rules.get(21009));
                        }
                    });
                    put("he", new HashMap<Integer, String>() {
                        {
                            put(21010, (String) LocaleRules.this.rules.get(21010));
                            put(21018, (String) LocaleRules.this.rules.get(21018));
                        }
                    });
                    put("da", new HashMap<Integer, String>() {
                        {
                            put(21005, (String) LocaleRules.this.rules.get(21005));
                            put(21017, (String) LocaleRules.this.rules.get(21017));
                        }
                    });
                    put("th", new HashMap<Integer, String>() {
                        {
                            put(31007, (String) LocaleRules.this.rules.get(31007));
                            put(21011, (String) LocaleRules.this.rules.get(21011));
                        }
                    });
                    put("ja", new HashMap<Integer, String>() {
                        {
                            put(31001, (String) LocaleRules.this.rules.get(31001));
                            put(31006, (String) LocaleRules.this.rules.get(31006));
                            put(21014, (String) LocaleRules.this.rules.get(21014));
                            put(41001, (String) LocaleRules.this.rules.get(41001));
                            put(21002, (String) LocaleRules.this.rules.get(21002));
                        }
                    });
                    put("hu", new HashMap<Integer, String>() {
                        {
                            put(31001, (String) LocaleRules.this.rules.get(31001));
                        }
                    });
                    put("ko", new HashMap<Integer, String>() {
                        {
                            put(31001, (String) LocaleRules.this.rules.get(31001));
                            put(31004, (String) LocaleRules.this.rules.get(31004));
                            put(21015, (String) LocaleRules.this.rules.get(21015));
                            put(21016, (String) LocaleRules.this.rules.get(21016));
                            put(41001, (String) LocaleRules.this.rules.get(41001));
                            put(21002, (String) LocaleRules.this.rules.get(21002));
                        }
                    });
                    put("zh_hans", new HashMap<Integer, String>() {
                        {
                            put(31001, (String) LocaleRules.this.rules.get(31001));
                            put(31005, (String) LocaleRules.this.rules.get(31005));
                            put(21014, (String) LocaleRules.this.rules.get(21014));
                            put(41001, (String) LocaleRules.this.rules.get(41001));
                            put(31009, (String) LocaleRules.this.rules.get(31009));
                        }
                    });
                    put("fr", new HashMap<Integer, String>() {
                        {
                            put(31001, (String) LocaleRules.this.rules.get(31001));
                            put(31003, (String) LocaleRules.this.rules.get(31003));
                        }
                    });
                    put("vi", new HashMap<Integer, String>() {
                        {
                            put(21012, (String) LocaleRules.this.rules.get(21012));
                            put(21013, (String) LocaleRules.this.rules.get(21013));
                            put(41002, (String) LocaleRules.this.rules.get(41002));
                        }
                    });
                    put("tr", new HashMap<Integer, String>() {
                        {
                            put(31001, (String) LocaleRules.this.rules.get(31001));
                            put(21003, (String) LocaleRules.this.rules.get(21003));
                        }
                    });
                    put("nb", new HashMap<Integer, String>() {
                        {
                            put(21004, (String) LocaleRules.this.rules.get(21004));
                        }
                    });
                    put("eu", new HashMap<Integer, String>() {
                        {
                            put(21021, (String) LocaleRules.this.rules.get(21021));
                            put(21019, (String) LocaleRules.this.rules.get(21019));
                            put(21023, (String) LocaleRules.this.rules.get(21023));
                        }
                    });
                    put("km", new HashMap<Integer, String>() {
                        {
                            put(21020, (String) LocaleRules.this.rules.get(21020));
                        }
                    });
                    put("ur", new HashMap<Integer, String>() {
                        {
                            put(21008, (String) LocaleRules.this.rules.get(21008));
                        }
                    });
                    put("lt", new HashMap<Integer, String>() {
                        {
                            put(21022, (String) LocaleRules.this.rules.get(21022));
                            put(21023, (String) LocaleRules.this.rules.get(21023));
                            put(21028, (String) LocaleRules.this.rules.get(21028));
                        }
                    });
                    put("bo", new HashMap<Integer, String>() {
                        {
                            put(21024, (String) LocaleRules.this.rules.get(21024));
                            put(31008, (String) LocaleRules.this.rules.get(31008));
                            put(41003, (String) LocaleRules.this.rules.get(41003));
                        }
                    });
                    put("si", new HashMap<Integer, String>() {
                        {
                            put(31001, (String) LocaleRules.this.rules.get(31001));
                            put(21028, (String) LocaleRules.this.rules.get(21028));
                        }
                    });
                    put("my", new HashMap<Integer, String>() {
                        {
                            put(21025, (String) LocaleRules.this.rules.get(21025));
                            put(21026, (String) LocaleRules.this.rules.get(21026));
                            put(41004, (String) LocaleRules.this.rules.get(41004));
                        }
                    });
                    put("lv", new HashMap<Integer, String>() {
                        {
                            put(21027, (String) LocaleRules.this.rules.get(21027));
                            put(41005, (String) LocaleRules.this.rules.get(41005));
                        }
                    });
                    put("kk", new HashMap<Integer, String>() {
                        {
                            put(31001, (String) LocaleRules.this.rules.get(31001));
                            put(21033, (String) LocaleRules.this.rules.get(21033));
                            put(21034, (String) LocaleRules.this.rules.get(21034));
                            put(21035, (String) LocaleRules.this.rules.get(21035));
                            put(21029, (String) LocaleRules.this.rules.get(21029));
                        }
                    });
                    put("lo", new HashMap<Integer, String>() {
                        {
                            put(21011, (String) LocaleRules.this.rules.get(21011));
                            put(21032, (String) LocaleRules.this.rules.get(21032));
                            put(31007, (String) LocaleRules.this.rules.get(31007));
                            put(31016, (String) LocaleRules.this.rules.get(31016));
                        }
                    });
                    put("be", new HashMap<Integer, String>() {
                        {
                            put(21030, (String) LocaleRules.this.rules.get(21030));
                            put(21033, (String) LocaleRules.this.rules.get(21033));
                            put(21034, (String) LocaleRules.this.rules.get(21034));
                            put(31010, (String) LocaleRules.this.rules.get(31010));
                        }
                    });
                    put("ne", new HashMap<Integer, String>() {
                        {
                            put(31012, (String) LocaleRules.this.rules.get(31012));
                            put(31011, (String) LocaleRules.this.rules.get(31011));
                        }
                    });
                    put("bn", new HashMap<Integer, String>() {
                        {
                            put(31012, (String) LocaleRules.this.rules.get(31012));
                            put(31011, (String) LocaleRules.this.rules.get(31011));
                        }
                    });
                    put("jv", new HashMap<Integer, String>() {
                        {
                            put(31013, (String) LocaleRules.this.rules.get(31013));
                            put(31014, (String) LocaleRules.this.rules.get(31014));
                        }
                    });
                    put("fil", new HashMap<Integer, String>() {
                        {
                            put(21031, (String) LocaleRules.this.rules.get(21031));
                            put(31013, (String) LocaleRules.this.rules.get(31013));
                            put(31015, (String) LocaleRules.this.rules.get(31015));
                            put(41006, (String) LocaleRules.this.rules.get(41006));
                        }
                    });
                    put("en", new HashMap<Integer, String>() {
                        {
                            put(31008, (String) LocaleRules.this.rules.get(31008));
                        }
                    });
                    put("am", new HashMap<Integer, String>() {
                        {
                            put(21011, (String) LocaleRules.this.rules.get(21011));
                            put(21036, (String) LocaleRules.this.rules.get(21036));
                        }
                    });
                    put("ml", new HashMap<Integer, String>() {
                        {
                            put(21037, (String) LocaleRules.this.rules.get(21037));
                            put(21035, (String) LocaleRules.this.rules.get(21035));
                        }
                    });
                    put("mi", new HashMap<Integer, String>() {
                        {
                            put(21038, (String) LocaleRules.this.rules.get(21038));
                            put(41007, (String) LocaleRules.this.rules.get(41007));
                        }
                    });
                    put("ta", new HashMap<Integer, String>() {
                        {
                            put(31001, (String) LocaleRules.this.rules.get(31001));
                        }
                    });
                    put("mn", new HashMap<Integer, String>() {
                        {
                            put(21039, (String) LocaleRules.this.rules.get(21039));
                            put(21040, (String) LocaleRules.this.rules.get(21040));
                        }
                    });
                    put("sw", new HashMap<Integer, String>() {
                        {
                            put(41006, (String) LocaleRules.this.rules.get(41006));
                        }
                    });
                }
            };
        }
    }

    private HashMap<Integer, String> getOthers(String locale) {
        HashMap<Integer, String> map = new HashMap<>();
        if (!Arrays.asList(new String[]{"zh_hans", "ja", "ko"}).contains(locale)) {
            map.put(31002, this.rules.get(31002));
        }
        return map;
    }
}
