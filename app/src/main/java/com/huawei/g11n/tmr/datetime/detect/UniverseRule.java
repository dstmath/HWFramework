package com.huawei.g11n.tmr.datetime.detect;

import java.util.HashMap;

public class UniverseRule {
    private HashMap<Integer, String> rules;
    private HashMap<Integer, HashMap<Integer, String>> subRulesMaps;

    public UniverseRule() {
        this.subRulesMaps = new HashMap<Integer, HashMap<Integer, String>>() {
            {
                put(Integer.valueOf(20002), new HashMap<Integer, String>() {
                    {
                        put(Integer.valueOf(20012), "(([param_EEEE]|[param_E]),{0,1}[ ]*){0,1}([param_MMMM]|[param_MMM])[ ]+[regex_d][,.]{0,1}([ ]*[regex_y]\\.{0,1}){0,1}");
                        put(Integer.valueOf(40005), "([param_MMMM]|[param_MMM])[ ]+[regex_d]\\s*(-|\u0926\u0947\u0916\u093f|\u09a5\u09c7\u0995\u09c7|ngantos|tekan|\u043f\u0430)\\s*[regex_d],{0,1}(\\s*[regex_y])(\\s*\u0436\\.)?");
                    }
                });
                put(Integer.valueOf(20003), new HashMap<Integer, String>() {
                    {
                        put(Integer.valueOf(40003), "(?<![-/.])([regex_d])\\.?\\s*(-|\u2013|~|\u0926\u0947\u0916\u093f|\u09a5\u09c7\u0995\u09c7)\\s*([regex_d])(?!\\3)(/|-|(\\.[ ]*))([regex_m])(\\6([regex_y])((\\.|\\s*\u0433\\.|\\s*\u0436\\.)?))?(?![-/.])");
                        put(Integer.valueOf(40004), "(?<![-/.])(([regex_y])\\s*([-/]|\\.)\\s*)([regex_m])\\s*\\5\\s*([regex_d])\\.?(?!\\5])(\\s*(-|\u2013|~|\u0926\u0947\u0916\u093f|\u09a5\u09c7\u0995\u09c7)\\s*)([regex_d])\\.?(?![.\\d\u2013~-])");
                        put(Integer.valueOf(40006), "(?<![-/.])([regex_m])([-/]|\\.[ ]{0,3})([regex_d])\\.?\\s*(?!\\3)(-|\u2013|~|\u0926\u0947\u0916\u093f|\u09a5\u09c7\u0995\u09c7)\\s*([regex_d])\\.?(?![.\\d\u2013~-])");
                    }
                });
                put(Integer.valueOf(20004), new HashMap<Integer, String>() {
                    {
                        put(Integer.valueOf(20014), "(([param_EEEE]|[param_E]),{0,1}[ ]*)?[regex_d]\\s*(/|-|\\.)\\s*([regex_m])(\\s*\\4\\s*[regex_y]((\\.|\\s*\u0433\\.)?)){0,1}");
                        put(Integer.valueOf(20015), "(([param_EEEE]|[param_E]),{0,1}[ ]*)?([regex_m])\\s*(/|-|\\.)\\s*[regex_d](\\s*\\5\\s*[regex_y](\\.{0,1})){0,1}");
                        put(Integer.valueOf(20016), "(([param_EEEE]|[param_E]),{0,1}[ ]*)?[regex_y]\\s*(/|-|\\.)\\s*([regex_m])\\s*\\5\\s*[regex_d]");
                    }
                });
            }
        };
        this.rules = new HashMap<Integer, String>() {
            {
                put(Integer.valueOf(40001), "[regex_d]\\.?\\s*([-\u2013~]|\u062a\u0627|\u0926\u0947\u0916\u093f|\u09a5\u09c7\u0995\u09c7|ngantos|tekan|\u043f\u0430)\\s*[regex_d](\\.|([ ]+de[ ]+)|/){0,1}[ ]*([param_MMMM]|[param_MMM])(([ ]+de|\\.|,|/|\u060c){0,1}[ ]*[regex_y](\\s*\u0436\\.|\\.){0,1}){0,1}");
                put(Integer.valueOf(40002), "([regex_y]\\.{0,1}[ ]*)?([param_MMMM]|[param_MMM])[ ]+[regex_d](\\.){0,1}\\s*([-\u2013]|\u0926\u0947\u0916\u093f|\u09a5\u09c7\u0995\u09c7|ngantos|tekan|\u043f\u0430)\\s*[regex_d]");
                put(Integer.valueOf(20001), "(([param_EEEE]|[param_E]),{0,1}[ ]*){0,1}[regex_d](\\.|([ ]+de[ ]+)|/){0,1}[ ]*([param_MMMM]|[param_MMM])(([ ]+de|\\.|,|/){0,1}[ ]*[regex_y](\\s*(\u0433\u043e\u0434\u0430|\u0433))?\\.{0,1}){0,1}");
                put(Integer.valueOf(20002), "(([param_EEEE]|[param_E])[\\s\\p{Punct}]*)?([param_MMMM]|[param_MMM])([\\s\\p{Punct}]|de)+\\d{1,2}(?!\\d)\\p{Punct}?(\\s*-?\\s*\\d{1,2}(?!\\d))?([\\s\\p{Punct}]*\\d{2,4}(?!\\d))?");
                put(Integer.valueOf(20003), "(?<![-/.a-zA-Z\\d])\\d{1,4}(\\.?\\s*[-~]+\\s*\\d{1,2})?(/|-|(\\.[ ]*))\\d{1,2}(?!\\d)((/|-|(\\.[ ]*))\\d{1,4}(?!\\d)\\.?)?(\\s*[-~]+\\s*\\d{1,2}(\\.|\\s*\u0433\\.)?(?!\\d))?(?![-/.a-zA-Z\\d])");
                put(Integer.valueOf(20004), "(([param_EEEE]|[param_E])[\\s\\p{Punct}]*)?(?<!\\d)\\d{1,4}\\s*(/|-|\\.)\\s*\\d{1,2}(\\s*\\3\\s*\\d{1,4}(\\s*\u0433\\.)?)?\\.?(?!\\d)(?!\\3)");
                put(Integer.valueOf(20005), "[regex_d][ ]+([param_MMMM]|[param_MMM])([ ]+(de[ ]+)?[regex_y]\\.{0,1})?(\\s*,)?([ ]*([param_EEEE]|[param_E]))");
                put(Integer.valueOf(20006), "(([param_EEEE]|[param_E2])(,[ ]*|[ ]+))[regex_d]\\.?(?![.:\\d])");
                put(Integer.valueOf(20013), "[regex_d](\u53f7|\u65e5|\uc77c)[ ]*[(\uff08]{0,1}([param_EEEE]|[param_E])[)\uff09]{0,1}");
                put(Integer.valueOf(20007), "(?<!\u6708)([regex_d][param_digitDay])[\u53f7\u65e5\uc77c]");
                put(Integer.valueOf(20008), "([regex_y]\\.{0,1}[ ]*){0,1}([param_MMMM]|[param_MMM])[ ]+[regex_d](\\.,|\\.|,){0,1}([ ]*([param_EEEE]|[param_E])){0,1}");
                put(Integer.valueOf(20009), "([param_EEEE]|[param_E2])");
                put(Integer.valueOf(20010), "[param_days]");
                put(Integer.valueOf(20011), "([param_nextweek]|[param_thisweek])");
                put(Integer.valueOf(30001), "([regex_hms])(\\s*\\(?[regex_zzzz]\\)?(?![.:]?\\d)){0,1}");
            }
        };
    }

    public HashMap<Integer, String> getRules() {
        return this.rules;
    }

    public HashMap<Integer, HashMap<Integer, String>> getSubRulesMaps() {
        return this.subRulesMaps;
    }
}
