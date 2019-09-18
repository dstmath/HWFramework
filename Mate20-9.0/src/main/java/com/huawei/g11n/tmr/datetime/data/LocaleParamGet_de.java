package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_de {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_am", "vorm\\.|morgens|vormittags");
            put("param_pm", "nachmittags|nachts|Mittag|nachm\\.");
            put("param_MMM", "Jan\\.|Feb\\.|März|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.");
            put("param_MMMM", "Januar|Februar|März|April|Mai|Juni|Juli|August|September|Oktober|November|Dezember");
            put("param_E", "So\\.|Mo\\.|Di\\.|Mi\\.|Do\\.|Fr\\.|Sa\\.");
            put("param_E2", "So\\.|Mo\\.|Di\\.|Mi\\.|Do\\.|Fr\\.|Sa\\.");
            put("param_EEEE", "Sonntag|Montag|Dienstag|Mittwoch|Donnerstag|Freitag|Samstag");
            put("param_days", "Heute|Morgen|Übermorgen");
            put("param_thisweek", "Diesen\\s+Sonntag|Diesen\\s+Montag|Diesen\\s+Dienstag|Diesen\\s+Mittwoch|Diesen\\s+Donnerstag|Diesen\\s+Freitag|Diesen\\s+Samstag");
            put("param_nextweek", "Nächsten\\s+Sonntag|Nächsten\\s+Montag|Nächsten\\s+Dienstag|Nächsten\\s+Mittwoch|Nächsten\\s+Donnerstag|Nächsten\\s+Freitag|Nächsten\\s+Samstag");
        }
    };
}
