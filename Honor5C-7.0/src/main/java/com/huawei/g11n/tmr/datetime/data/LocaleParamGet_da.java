package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_da {
    public HashMap<String, String> date;

    public LocaleParamGet_da() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", "[:.]");
                put("param_am", "AM");
                put("param_pm", "middag");
                put("param_MMM", "jan\\.|feb\\.|mar\\.|apr\\.|maj|jun\\.|jul\\.|aug\\.|sep\\.|okt\\.|nov\\.|dec\\.");
                put("param_MMMM", "januar|februar|marts|april|maj|juni|juli|august|september|oktober|november|december");
                put("param_E", "s\u00f8n\\.|man\\.|tir\\.|ons\\.|tor\\.|fre\\.|l\u00f8r\\.");
                put("param_E2", "s\u00f8n\\.|man\\.|tir\\.|ons\\.|tor\\.|fre\\.|l\u00f8r\\.");
                put("param_EEEE", "s\u00f8ndag|mandag|tirsdag|onsdag|torsdag|fredag|l\u00f8rdag");
                put("param_days", "i\\s+dag|i\\s+morgen|i\\s+overmorgen");
                put("param_thisweek", "denne\\s+s\u00f8ndag|denne\\s+mandag|denne\\s+tirsdag|denne\\s+onsdag|denne\\s+torsdag|denne\\s+fredag|denne\\s+l\u00f8rdag");
                put("param_nextweek", "n\u00e6ste\\s+s\u00f8ndag|n\u00e6ste\\s+mandag|n\u00e6ste\\s+tirsdag|n\u00e6ste\\s+onsdag|n\u00e6ste\\s+torsdag|n\u00e6ste\\s+fredag|n\u00e6ste\\s+l\u00f8rdag");
            }
        };
    }
}
