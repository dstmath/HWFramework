package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_sv {
    public HashMap<String, String> date;

    public LocaleParamGet_sv() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", "[:.]");
                put("param_am", "f|fm");
                put("param_pm", "em|e");
                put("param_MMM", "jan|feb|mar|apr|maj|jun|jul|aug|sep|okt|nov|dec");
                put("param_MMMM", "januari|februari|mars|april|maj|juni|juli|augusti|september|oktober|november|december");
                put("param_E", "s\u00f6n|m\u00e5n|tis|ons|tors|fre|l\u00f6r");
                put("param_E2", "s\u00f6n|m\u00e5n|tis|ons|tors|fre|l\u00f6r");
                put("param_EEEE", "s\u00f6ndag|m\u00e5ndag|tisdag|onsdag|torsdag|fredag|l\u00f6rdag");
                put("param_days", "i\\s+dag|i\\s+morgon|i\\s+\u00f6vermorgon");
                put("param_thisweek", "s\u00f6ndag\\s+denna\\s+vecka|m\u00e5ndag\\s+denna\\s+vecka|tisdag\\s+denna\\s+vecka|onsdag\\s+denna\\s+vecka|torsdag\\s+denna\\s+vecka|fredag\\s+denna\\s+vecka|l\u00f6rdag\\s+denna\\s+vecka");
                put("param_nextweek", "s\u00f6ndag\\s+n\u00e4sta\\s+vecka|m\u00e5ndag\\s+n\u00e4sta\\s+vecka|tisdag\\s+n\u00e4sta\\s+vecka|onsdag\\s+n\u00e4sta\\s+vecka|torsdag\\s+n\u00e4sta\\s+vecka|fredag\\s+n\u00e4sta\\s+vecka|l\u00f6rdag\\s+n\u00e4sta\\s+vecka");
            }
        };
    }
}
