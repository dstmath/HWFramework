package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_sl {
    public HashMap<String, String> date;

    public LocaleParamGet_sl() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", ":");
                put("param_am", "dop\\.");
                put("param_pm", "pop\\.");
                put("param_MMM", "jan\\.|feb\\.|mar\\.|apr\\.|maj|jun\\.|jul\\.|avg\\.|sep\\.|okt\\.|nov\\.|dec\\.");
                put("param_MMMM", "januar|februar|marec|april|maj|junij|julij|avgust|september|oktober|november|december");
                put("param_E", "ned\\.|pon\\.|tor\\.|sre\\.|\u010det\\.|pet\\.|sob\\.");
                put("param_E2", "ned\\.|pon\\.|tor\\.|sre\\.|\u010det\\.|pet\\.|sob\\.");
                put("param_EEEE", "nedelja|ponedeljek|torek|sreda|\u010detrtek|petek|sobota");
                put("param_days", "danes|jutri|pojutri\u0161njem");
                put("param_thisweek", "to\\s+nedeljo|ta\\s+ponedeljek|Ta\\s+torek|To\\s+sredo|Ta\\s+\u010detrtek|Ta\\s+petek|To\\s+soboto");
                put("param_nextweek", "naslednjo\\s+nedeljo|naslednji\\s+ponedeljek|Naslednji\\s+torek|Naslednjo\\s+sredo|Naslednji\\s+\u010detrtek|Naslednji\\s+petek|Naslednjo\\s+soboto");
            }
        };
    }
}
