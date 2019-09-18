package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_sv {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_tmark", "[:.]");
            put("param_am", "f|fm");
            put("param_pm", "em|e");
            put("param_MMM", "jan|feb|mar|apr|maj|jun|jul|aug|sep|okt|nov|dec");
            put("param_MMMM", "januari|februari|mars|april|maj|juni|juli|augusti|september|oktober|november|december");
            put("param_E", "sön|mån|tis|ons|tors|fre|lör");
            put("param_E2", "sön|mån|tis|ons|tors|fre|lör");
            put("param_EEEE", "söndag|måndag|tisdag|onsdag|torsdag|fredag|lördag");
            put("param_days", "i\\s+dag|i\\s+morgon|i\\s+övermorgon");
            put("param_thisweek", "söndag\\s+denna\\s+vecka|måndag\\s+denna\\s+vecka|tisdag\\s+denna\\s+vecka|onsdag\\s+denna\\s+vecka|torsdag\\s+denna\\s+vecka|fredag\\s+denna\\s+vecka|lördag\\s+denna\\s+vecka");
            put("param_nextweek", "söndag\\s+nästa\\s+vecka|måndag\\s+nästa\\s+vecka|tisdag\\s+nästa\\s+vecka|onsdag\\s+nästa\\s+vecka|torsdag\\s+nästa\\s+vecka|fredag\\s+nästa\\s+vecka|lördag\\s+nästa\\s+vecka");
        }
    };
}
