package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_pl {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_am", "nad\\s+ranem|rano");
            put("param_pm", "po\\s+południu|w\\s+nocy|w\\s+południe");
            put("param_MMM", "sty|lut|mar|kwi|maj|cze|lip|sie|wrz|paź|lis|gru");
            put("param_MMMM", "stycznia|lutego|marca|kwietnia|maja|czerwca|lipca|sierpnia|września|października|listopada|grudnia");
            put("param_E", "niedz\\.|pon\\.|wt\\.|śr\\.|czw\\.|pt\\.|sob\\.");
            put("param_E2", "niedz\\.|pon\\.|wt\\.|śr\\.|czw\\.|pt\\.|sob\\.");
            put("param_EEEE", "niedziela|poniedziałek|wtorek|środa|czwartek|piątek|sobota");
            put("param_days", "dzisiaj|jutro|pojutrze");
            put("param_thisweek", "w\\s+tę\\s+niedzielę|w\\s+ten\\s+poniedziałek|w\\s+ten\\s+wtorek|w\\s+tę\\s+środę|w\\s+ten\\s+czwartek|w\\s+ten\\s+piątek|w\\s+tę\\s+sobotę");
            put("param_nextweek", "w\\s+przyszłą\\s+niedzielę|w\\s+przyszły\\s+poniedziałek|w\\s+przyszły\\s+wtorek|w\\s+przyszłą\\s+środę|w\\s+przyszły\\s+czwartek|w\\s+przyszły\\s+piątek|w\\s+przyszłą\\s+sobotę");
        }
    };
}
