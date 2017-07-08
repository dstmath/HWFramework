package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_nb {
    public HashMap<String, String> date;

    public LocaleParamGet_nb() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", "[:.]");
                put("param_am", "a|a\\.m\\.");
                put("param_pm", "p\\.m\\.|p");
                put("param_MMM", "jan\\.|feb\\.|mar\\.|apr\\.|mai|jun\\.|jul\\.|aug\\.|sep\\.|okt\\.|nov\\.|des\\.");
                put("param_MMMM", "januar|februar|mars|april|mai|juni|juli|august|september|oktober|november|desember");
                put("param_E", "s\u00f8n\\.|man\\.|tir\\.|ons\\.|tor\\.|fre\\.|l\u00f8r\\.");
                put("param_E2", "s\u00f8n\\.|man\\.|tir\\.|ons\\.|tor\\.|fre\\.|l\u00f8r\\.");
                put("param_EEEE", "s\u00f8ndag|mandag|tirsdag|onsdag|torsdag|fredag|l\u00f8rdag");
                put("param_days", "i\\s+dag|i\\s+morgen|i\\s+overmorgen");
                put("param_thisweek", "s\u00f8ndag\\s+denne\\s+uken|mandag\\s+denne\\s+uken|tirsdag\\s+denne\\s+uken|onsdag\\s+denne\\s+uken|torsdag\\s+denne\\s+uken|fredag\\s+denne\\s+uken|l\u00f8rdag\\s+denne\\s+uken");
                put("param_nextweek", "s\u00f8ndag\\s+neste\\s+uke|mandag\\s+neste\\s+uke|tirsdag\\s+neste\\s+uke|onsdag\\s+neste\\s+uke|torsdag\\s+neste\\s+uke|fredag\\s+neste\\s+uke|l\u00f8rdag\\s+neste\\s+uke");
            }
        };
    }
}
