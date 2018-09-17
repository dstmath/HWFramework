package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_fi {
    public HashMap<String, String> date;

    public LocaleParamGet_fi() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", "[:.]");
                put("param_am", "ap\\.");
                put("param_pm", "ip\\.");
                put("param_MMM", "tammikuuta|helmikuuta|maaliskuuta|huhtikuuta|toukokuuta|kes\u00e4kuuta|hein\u00e4kuuta|elokuuta|syyskuuta|lokakuuta|marraskuuta|joulukuuta");
                put("param_MMMM", "tammikuuta|helmikuuta|maaliskuuta|huhtikuuta|toukokuuta|kes\u00e4kuuta|hein\u00e4kuuta|elokuuta|syyskuuta|lokakuuta|marraskuuta|joulukuuta");
                put("param_E", "su|ma|ti|ke|to|pe|la");
                put("param_E2", "su|ma|ti|ke|to|pe|la");
                put("param_EEEE", "sunnuntai|maanantai|tiistai|keskiviikko|torstai|perjantai|lauantai");
                put("param_days", "t\u00e4n\u00e4\u00e4n|huomenna|ylihuomenna");
                put("param_thisweek", "t\u00e4n\u00e4\\s+sunnuntaina|t\u00e4n\u00e4\\s+maanantaina|t\u00e4n\u00e4\\s+tiistaina|t\u00e4n\u00e4\\s+keskiviikkona|t\u00e4n\u00e4\\s+torstaina|t\u00e4n\u00e4\\s+perjantaina|t\u00e4n\u00e4\\s+lauantaina");
                put("param_nextweek", "ensi\\s+sunnuntaina|ensi\\s+maanantaina|ensi\\s+tiistaina|ensi\\s+keskiviikkona|ensi\\s+torstaina|ensi\\s+perjantaina|ensi\\s+lauantaina");
            }
        };
    }
}
