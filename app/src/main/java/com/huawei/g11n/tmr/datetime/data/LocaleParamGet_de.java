package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_de {
    public HashMap<String, String> date;

    public LocaleParamGet_de() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "vorm\\.|morgens|vormittags");
                put("param_pm", "nachmittags|nachts|Mittag|nachm\\.");
                put("param_MMM", "Jan\\.|Feb\\.|M\u00e4rz|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.");
                put("param_MMMM", "Januar|Februar|M\u00e4rz|April|Mai|Juni|Juli|August|September|Oktober|November|Dezember");
                put("param_E", "So\\.|Mo\\.|Di\\.|Mi\\.|Do\\.|Fr\\.|Sa\\.");
                put("param_E2", "So\\.|Mo\\.|Di\\.|Mi\\.|Do\\.|Fr\\.|Sa\\.");
                put("param_EEEE", "Sonntag|Montag|Dienstag|Mittwoch|Donnerstag|Freitag|Samstag");
                put("param_days", "Heute|Morgen|\u00dcbermorgen");
                put("param_thisweek", "Diesen\\s+Sonntag|Diesen\\s+Montag|Diesen\\s+Dienstag|Diesen\\s+Mittwoch|Diesen\\s+Donnerstag|Diesen\\s+Freitag|Diesen\\s+Samstag");
                put("param_nextweek", "N\u00e4chsten\\s+Sonntag|N\u00e4chsten\\s+Montag|N\u00e4chsten\\s+Dienstag|N\u00e4chsten\\s+Mittwoch|N\u00e4chsten\\s+Donnerstag|N\u00e4chsten\\s+Freitag|N\u00e4chsten\\s+Samstag");
            }
        };
    }
}
