package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_nb {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_tmark", "[:.]");
            put("param_am", "a|a\\.m\\.");
            put("param_pm", "p\\.m\\.|p");
            put("param_MMM", "jan\\.|feb\\.|mar\\.|apr\\.|mai|jun\\.|jul\\.|aug\\.|sep\\.|okt\\.|nov\\.|des\\.");
            put("param_MMMM", "januar|februar|mars|april|mai|juni|juli|august|september|oktober|november|desember");
            put("param_E", "søn\\.|man\\.|tir\\.|ons\\.|tor\\.|fre\\.|lør\\.");
            put("param_E2", "søn\\.|man\\.|tir\\.|ons\\.|tor\\.|fre\\.|lør\\.");
            put("param_EEEE", "søndag|mandag|tirsdag|onsdag|torsdag|fredag|lørdag");
            put("param_days", "i\\s+dag|i\\s+morgen|i\\s+overmorgen");
            put("param_thisweek", "søndag\\s+denne\\s+uken|mandag\\s+denne\\s+uken|tirsdag\\s+denne\\s+uken|onsdag\\s+denne\\s+uken|torsdag\\s+denne\\s+uken|fredag\\s+denne\\s+uken|lørdag\\s+denne\\s+uken");
            put("param_nextweek", "søndag\\s+neste\\s+uke|mandag\\s+neste\\s+uke|tirsdag\\s+neste\\s+uke|onsdag\\s+neste\\s+uke|torsdag\\s+neste\\s+uke|fredag\\s+neste\\s+uke|lørdag\\s+neste\\s+uke");
        }
    };
}
