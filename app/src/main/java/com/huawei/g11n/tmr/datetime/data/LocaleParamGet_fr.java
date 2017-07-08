package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_fr {
    public HashMap<String, String> date;

    public LocaleParamGet_fr() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "AM|matin");
                put("param_pm", "apr\u00e8s-midi|soir|midi|ap\\.-m\\.|ap\\.m\\.|soir|midi|p");
                put("param_MMM", "janv\\.|f\u00e9vr\\.|mars|avr\\.|mai|juin|juil\\.|ao\u00fbt|sept\\.|oct\\.|nov\\.|d\u00e9c\\.");
                put("param_MMMM", "janvier|f\u00e9vrier|mars|avril|mai|juin|juillet|ao\u00fbt|septembre|octobre|novembre|d\u00e9cembre");
                put("param_E", "dim\\.|lun\\.|mar\\.|mer\\.|jeu\\.|ven\\.|sam\\.");
                put("param_E2", "dim\\.|lun\\.|mar\\.|mer\\.|jeu\\.|ven\\.|sam\\.");
                put("param_EEEE", "dimanche|lundi|mardi|mercredi|jeudi|vendredi|samedi");
                put("param_days", "aujourd'hui|demain|apr\u00e8s-demain");
                put("param_thisweek", "ce\\s+dimanche|ce\\s+lundi|ce\\s+mardi|ce\\s+mercredi|ce\\s+jeudi|ce\\s+vendredi|ce\\s+samedi");
                put("param_nextweek", "dimanche\\s+prochain|lundi\\s+prochain|mardi\\s+prochain|mercredi\\s+prochain|jeudi\\s+prochain|vendredi\\s+prochain|samedi\\s+prochain");
            }
        };
    }
}
