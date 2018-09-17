package com.huawei.g11n.tmr.datetime.detect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocaleRules {
    private HashMap<String, HashMap<Integer, String>> locale_rules = null;
    private HashMap<Integer, String> rules = null;

    public LocaleRules(Rules r) {
        this.rules = r.getRules();
        init();
    }

    public HashMap<Integer, String> getLocaleRules(String locale) {
        HashMap<Integer, String> localeRules = new HashMap();
        if (this.locale_rules.containsKey(locale)) {
            localeRules.putAll((Map) this.locale_rules.get(locale));
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
                            put(Integer.valueOf(21006), (String) LocaleRules.this.rules.get(Integer.valueOf(21006)));
                            put(Integer.valueOf(21007), (String) LocaleRules.this.rules.get(Integer.valueOf(21007)));
                        }
                    });
                    put("ar", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21008), (String) LocaleRules.this.rules.get(Integer.valueOf(21008)));
                            put(Integer.valueOf(21009), (String) LocaleRules.this.rules.get(Integer.valueOf(21009)));
                        }
                    });
                    put("he", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21010), (String) LocaleRules.this.rules.get(Integer.valueOf(21010)));
                            put(Integer.valueOf(21018), (String) LocaleRules.this.rules.get(Integer.valueOf(21018)));
                        }
                    });
                    put("da", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21005), (String) LocaleRules.this.rules.get(Integer.valueOf(21005)));
                            put(Integer.valueOf(21017), (String) LocaleRules.this.rules.get(Integer.valueOf(21017)));
                        }
                    });
                    put("th", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31007), (String) LocaleRules.this.rules.get(Integer.valueOf(31007)));
                            put(Integer.valueOf(21011), (String) LocaleRules.this.rules.get(Integer.valueOf(21011)));
                        }
                    });
                    put("ja", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31001), (String) LocaleRules.this.rules.get(Integer.valueOf(31001)));
                            put(Integer.valueOf(31006), (String) LocaleRules.this.rules.get(Integer.valueOf(31006)));
                            put(Integer.valueOf(21014), (String) LocaleRules.this.rules.get(Integer.valueOf(21014)));
                            put(Integer.valueOf(41001), (String) LocaleRules.this.rules.get(Integer.valueOf(41001)));
                            put(Integer.valueOf(21002), (String) LocaleRules.this.rules.get(Integer.valueOf(21002)));
                        }
                    });
                    put("hu", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31001), (String) LocaleRules.this.rules.get(Integer.valueOf(31001)));
                        }
                    });
                    put("ko", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31001), (String) LocaleRules.this.rules.get(Integer.valueOf(31001)));
                            put(Integer.valueOf(31004), (String) LocaleRules.this.rules.get(Integer.valueOf(31004)));
                            put(Integer.valueOf(21015), (String) LocaleRules.this.rules.get(Integer.valueOf(21015)));
                            put(Integer.valueOf(21016), (String) LocaleRules.this.rules.get(Integer.valueOf(21016)));
                            put(Integer.valueOf(41001), (String) LocaleRules.this.rules.get(Integer.valueOf(41001)));
                            put(Integer.valueOf(21002), (String) LocaleRules.this.rules.get(Integer.valueOf(21002)));
                        }
                    });
                    put("zh_hans", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31001), (String) LocaleRules.this.rules.get(Integer.valueOf(31001)));
                            put(Integer.valueOf(31005), (String) LocaleRules.this.rules.get(Integer.valueOf(31005)));
                            put(Integer.valueOf(21014), (String) LocaleRules.this.rules.get(Integer.valueOf(21014)));
                            put(Integer.valueOf(41001), (String) LocaleRules.this.rules.get(Integer.valueOf(41001)));
                            put(Integer.valueOf(31009), (String) LocaleRules.this.rules.get(Integer.valueOf(31009)));
                        }
                    });
                    put("fr", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31001), (String) LocaleRules.this.rules.get(Integer.valueOf(31001)));
                            put(Integer.valueOf(31003), (String) LocaleRules.this.rules.get(Integer.valueOf(31003)));
                        }
                    });
                    put("vi", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21012), (String) LocaleRules.this.rules.get(Integer.valueOf(21012)));
                            put(Integer.valueOf(21013), (String) LocaleRules.this.rules.get(Integer.valueOf(21013)));
                            put(Integer.valueOf(41002), (String) LocaleRules.this.rules.get(Integer.valueOf(41002)));
                        }
                    });
                    put("tr", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31001), (String) LocaleRules.this.rules.get(Integer.valueOf(31001)));
                            put(Integer.valueOf(21003), (String) LocaleRules.this.rules.get(Integer.valueOf(21003)));
                        }
                    });
                    put("nb", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21004), (String) LocaleRules.this.rules.get(Integer.valueOf(21004)));
                        }
                    });
                    put("eu", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21021), (String) LocaleRules.this.rules.get(Integer.valueOf(21021)));
                            put(Integer.valueOf(21019), (String) LocaleRules.this.rules.get(Integer.valueOf(21019)));
                            put(Integer.valueOf(21023), (String) LocaleRules.this.rules.get(Integer.valueOf(21023)));
                        }
                    });
                    put("km", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21020), (String) LocaleRules.this.rules.get(Integer.valueOf(21020)));
                        }
                    });
                    put("ur", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21008), (String) LocaleRules.this.rules.get(Integer.valueOf(21008)));
                        }
                    });
                    put("lt", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21022), (String) LocaleRules.this.rules.get(Integer.valueOf(21022)));
                            put(Integer.valueOf(21023), (String) LocaleRules.this.rules.get(Integer.valueOf(21023)));
                            put(Integer.valueOf(21028), (String) LocaleRules.this.rules.get(Integer.valueOf(21028)));
                        }
                    });
                    put("bo", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21024), (String) LocaleRules.this.rules.get(Integer.valueOf(21024)));
                            put(Integer.valueOf(31008), (String) LocaleRules.this.rules.get(Integer.valueOf(31008)));
                            put(Integer.valueOf(41003), (String) LocaleRules.this.rules.get(Integer.valueOf(41003)));
                        }
                    });
                    put("si", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31001), (String) LocaleRules.this.rules.get(Integer.valueOf(31001)));
                            put(Integer.valueOf(21028), (String) LocaleRules.this.rules.get(Integer.valueOf(21028)));
                        }
                    });
                    put("my", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21025), (String) LocaleRules.this.rules.get(Integer.valueOf(21025)));
                            put(Integer.valueOf(21026), (String) LocaleRules.this.rules.get(Integer.valueOf(21026)));
                            put(Integer.valueOf(41004), (String) LocaleRules.this.rules.get(Integer.valueOf(41004)));
                        }
                    });
                    put("lv", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21027), (String) LocaleRules.this.rules.get(Integer.valueOf(21027)));
                            put(Integer.valueOf(41005), (String) LocaleRules.this.rules.get(Integer.valueOf(41005)));
                        }
                    });
                    put("kk", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31001), (String) LocaleRules.this.rules.get(Integer.valueOf(31001)));
                            put(Integer.valueOf(21033), (String) LocaleRules.this.rules.get(Integer.valueOf(21033)));
                            put(Integer.valueOf(21034), (String) LocaleRules.this.rules.get(Integer.valueOf(21034)));
                            put(Integer.valueOf(21035), (String) LocaleRules.this.rules.get(Integer.valueOf(21035)));
                            put(Integer.valueOf(21029), (String) LocaleRules.this.rules.get(Integer.valueOf(21029)));
                        }
                    });
                    put("lo", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21011), (String) LocaleRules.this.rules.get(Integer.valueOf(21011)));
                            put(Integer.valueOf(21032), (String) LocaleRules.this.rules.get(Integer.valueOf(21032)));
                            put(Integer.valueOf(31007), (String) LocaleRules.this.rules.get(Integer.valueOf(31007)));
                            put(Integer.valueOf(31016), (String) LocaleRules.this.rules.get(Integer.valueOf(31016)));
                        }
                    });
                    put("be", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21030), (String) LocaleRules.this.rules.get(Integer.valueOf(21030)));
                            put(Integer.valueOf(21033), (String) LocaleRules.this.rules.get(Integer.valueOf(21033)));
                            put(Integer.valueOf(21034), (String) LocaleRules.this.rules.get(Integer.valueOf(21034)));
                            put(Integer.valueOf(31010), (String) LocaleRules.this.rules.get(Integer.valueOf(31010)));
                        }
                    });
                    put("ne", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31012), (String) LocaleRules.this.rules.get(Integer.valueOf(31012)));
                            put(Integer.valueOf(31011), (String) LocaleRules.this.rules.get(Integer.valueOf(31011)));
                        }
                    });
                    put("bn", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31012), (String) LocaleRules.this.rules.get(Integer.valueOf(31012)));
                            put(Integer.valueOf(31011), (String) LocaleRules.this.rules.get(Integer.valueOf(31011)));
                        }
                    });
                    put("jv", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31013), (String) LocaleRules.this.rules.get(Integer.valueOf(31013)));
                            put(Integer.valueOf(31014), (String) LocaleRules.this.rules.get(Integer.valueOf(31014)));
                        }
                    });
                    put("fil", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21031), (String) LocaleRules.this.rules.get(Integer.valueOf(21031)));
                            put(Integer.valueOf(31013), (String) LocaleRules.this.rules.get(Integer.valueOf(31013)));
                            put(Integer.valueOf(31015), (String) LocaleRules.this.rules.get(Integer.valueOf(31015)));
                            put(Integer.valueOf(41006), (String) LocaleRules.this.rules.get(Integer.valueOf(41006)));
                        }
                    });
                    put("en", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31008), (String) LocaleRules.this.rules.get(Integer.valueOf(31008)));
                        }
                    });
                    put("am", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21011), (String) LocaleRules.this.rules.get(Integer.valueOf(21011)));
                            put(Integer.valueOf(21036), (String) LocaleRules.this.rules.get(Integer.valueOf(21036)));
                        }
                    });
                    put("ml", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21037), (String) LocaleRules.this.rules.get(Integer.valueOf(21037)));
                            put(Integer.valueOf(21035), (String) LocaleRules.this.rules.get(Integer.valueOf(21035)));
                        }
                    });
                    put("mi", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21038), (String) LocaleRules.this.rules.get(Integer.valueOf(21038)));
                            put(Integer.valueOf(41007), (String) LocaleRules.this.rules.get(Integer.valueOf(41007)));
                        }
                    });
                    put("ta", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(31001), (String) LocaleRules.this.rules.get(Integer.valueOf(31001)));
                        }
                    });
                    put("mn", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(21039), (String) LocaleRules.this.rules.get(Integer.valueOf(21039)));
                            put(Integer.valueOf(21040), (String) LocaleRules.this.rules.get(Integer.valueOf(21040)));
                        }
                    });
                    put("sw", new HashMap<Integer, String>() {
                        {
                            put(Integer.valueOf(41006), (String) LocaleRules.this.rules.get(Integer.valueOf(41006)));
                        }
                    });
                }
            };
        }
    }

    private HashMap<Integer, String> getOthers(String locale) {
        HashMap<Integer, String> map = new HashMap();
        List<String> exLocales = Arrays.asList(new String[]{"zh_hans", "ja", "ko"});
        Integer id = Integer.valueOf(31002);
        if (!exLocales.contains(locale)) {
            map.put(id, (String) this.rules.get(id));
        }
        return map;
    }
}
