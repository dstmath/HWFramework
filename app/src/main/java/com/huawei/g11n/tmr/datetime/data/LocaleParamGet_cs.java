package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_cs {
    public HashMap<String, String> date;

    public LocaleParamGet_cs() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "AM");
                put("param_pm", "PM");
                put("param_MMM", "led|\u00fano|b\u0159e|dub|kv\u011b|\u010dvn|\u010dvc|srp|z\u00e1\u0159|\u0159\u00edj|lis|pro");
                put("param_MMMM", "ledna|\u00fanora|b\u0159ezna|dubna|kv\u011btna|\u010dervna|\u010dervence|srpna|z\u00e1\u0159\u00ed|\u0159\u00edjna|listopadu|prosince");
                put("param_E", "ne|po|\u00fat|st|\u010dt|p\u00e1|so");
                put("param_E2", "ne|po|\u00fat|st|\u010dt|p\u00e1|so");
                put("param_EEEE", "ned\u011ble|pond\u011bl\u00ed|\u00fater\u00fd|st\u0159eda|\u010dtvrtek|p\u00e1tek|sobota");
                put("param_days", "dnes|z\u00edtra|poz\u00edt\u0159\u00ed");
                put("param_thisweek", "tuto\\s+ned\u011bli|toto\\s+pond\u011bl\u00ed|toto\\s+\u00fater\u00fd|tuto\\s+st\u0159edu|tento\\s+\u010dtvrtek|tento\\s+p\u00e1tek|tuto\\s+sobotu");
                put("param_nextweek", "p\u0159\u00ed\u0161t\u00ed\\s+ned\u011bli|p\u0159\u00ed\u0161t\u00ed\\s+pond\u011bl\u00ed|p\u0159\u00ed\u0161t\u00ed\\s+\u00fater\u00fd|p\u0159\u00ed\u0161t\u00ed\\s+st\u0159edu|p\u0159\u00ed\u0161t\u00ed\\s+\u010dtvrtek|p\u0159\u00ed\u0161t\u00ed\\s+p\u00e1tek|p\u0159\u00ed\u0161t\u00ed\\s+sobotu");
            }
        };
    }
}
