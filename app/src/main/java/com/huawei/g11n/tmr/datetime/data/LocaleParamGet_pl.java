package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_pl {
    public HashMap<String, String> date;

    public LocaleParamGet_pl() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "nad\\s+ranem|rano");
                put("param_pm", "po\\s+po\u0142udniu|w\\s+nocy|w\\s+po\u0142udnie");
                put("param_MMM", "sty|lut|mar|kwi|maj|cze|lip|sie|wrz|pa\u017a|lis|gru");
                put("param_MMMM", "stycznia|lutego|marca|kwietnia|maja|czerwca|lipca|sierpnia|wrze\u015bnia|pa\u017adziernika|listopada|grudnia");
                put("param_E", "niedz\\.|pon\\.|wt\\.|\u015br\\.|czw\\.|pt\\.|sob\\.");
                put("param_E2", "niedz\\.|pon\\.|wt\\.|\u015br\\.|czw\\.|pt\\.|sob\\.");
                put("param_EEEE", "niedziela|poniedzia\u0142ek|wtorek|\u015broda|czwartek|pi\u0105tek|sobota");
                put("param_days", "dzisiaj|jutro|pojutrze");
                put("param_thisweek", "w\\s+t\u0119\\s+niedziel\u0119|w\\s+ten\\s+poniedzia\u0142ek|w\\s+ten\\s+wtorek|w\\s+t\u0119\\s+\u015brod\u0119|w\\s+ten\\s+czwartek|w\\s+ten\\s+pi\u0105tek|w\\s+t\u0119\\s+sobot\u0119");
                put("param_nextweek", "w\\s+przysz\u0142\u0105\\s+niedziel\u0119|w\\s+przysz\u0142y\\s+poniedzia\u0142ek|w\\s+przysz\u0142y\\s+wtorek|w\\s+przysz\u0142\u0105\\s+\u015brod\u0119|w\\s+przysz\u0142y\\s+czwartek|w\\s+przysz\u0142y\\s+pi\u0105tek|w\\s+przysz\u0142\u0105\\s+sobot\u0119");
            }
        };
    }
}
