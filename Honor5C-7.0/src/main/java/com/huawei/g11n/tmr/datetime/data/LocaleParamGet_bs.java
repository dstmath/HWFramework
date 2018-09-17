package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_bs {
    public HashMap<String, String> date;

    public LocaleParamGet_bs() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", ":");
                put("param_am", "AM");
                put("param_pm", "PM");
                put("param_MMM", "jan|feb|mar|apr|maj|jun|jul|avg|sep|okt|nov|dec");
                put("param_MMMM", "januar|februar|mart|april|maj|juni|juli|avgust|septembar|oktobar|novembar|decembar");
                put("param_E", "ned|pon|uto|sri|\u010det|pet|sub");
                put("param_E2", "ned|pon|uto|sri|\u010det|pet|sub");
                put("param_EEEE", "nedjelja|ponedjeljak|utorak|srijeda|\u010detvrtak|petak|subota");
                put("param_days", "danas|sutra|prekosutra");
                put("param_thisweek", "ova\\s+nedjelja|ovaj\\s+ponedjeljak|ovaj\\s+utorak|ova\\s+srijeda|ovaj\\s+\u010detvrtak|ovaj\\s+petak|ova\\s+subota");
                put("param_nextweek", "sljede\u0107a\\s+nedjelja|sljede\u0107i\\s+ponedjeljak|sljede\u0107i\\s+utorak|sljede\u0107a\\s+srijeda|sljede\u0107i\\s+\u010detvrtak|sljede\u0107i\\s+petak|sljede\u0107a\\s+subota");
            }
        };
    }
}
