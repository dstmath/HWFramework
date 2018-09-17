package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_ro {
    public HashMap<String, String> date;

    public LocaleParamGet_ro() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "a\\.m\\.");
                put("param_pm", "p\\.m\\.");
                put("param_MMM", "ian\\.|feb\\.|mar\\.|apr\\.|mai|iun\\.|iul\\.|aug\\.|sept\\.|oct\\.|nov\\.|dec\\.");
                put("param_MMMM", "ianuarie|februarie|martie|aprilie|mai|iunie|iulie|august|septembrie|octombrie|noiembrie|decembrie");
                put("param_E", "Dum|Lun|Mar|Mie|Joi|Vin|S\u00e2m");
                put("param_E2", "Dum|Lun|Mar|Mie|Joi|Vin|S\u00e2m");
                put("param_EEEE", "duminic\u0103|luni|mar\u021bi|miercuri|joi|vineri|s\u00e2mb\u0103t\u0103");
                put("param_days", "azi|m\u00e2ine|poim\u00e2ine");
                put("param_thisweek", "duminica\\s+aceasta|lunea\\s+aceasta|mar\u021bea\\s+aceasta|miercurea\\s+aceasta|joia\\s+aceasta|vinerea\\s+aceasta|s\u00e2mb\u0103ta\\s+aceasta");
                put("param_nextweek", "duminica\\s+viitoare|lunea\\s+viitoare|mar\u021bea\\s+viitoare|miercurea\\s+viitoare|joia\\s+viitoare|vinerea\\s+viitoare|s\u00e2mb\u0103ta\\s+viitoare");
            }
        };
    }
}
