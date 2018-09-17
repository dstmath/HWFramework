package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_sr {
    public HashMap<String, String> date;

    public LocaleParamGet_sr() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", ":");
                put("param_am", "AM");
                put("param_pm", "PM");
                put("param_MMM", "jan|feb|mar|apr|maj|jun|jul|avg|sep|okt|nov|dec");
                put("param_MMMM", "januar|februar|mart|april|maj|jun|jul|avgust|septembar|oktobar|novembar|decembar");
                put("param_E", "ned|pon|uto|sre|\u010det|pet|sub");
                put("param_E2", "ned|pon|uto|sre|\u010det|pet|sub");
                put("param_EEEE", "nedelja|ponedeljak|utorak|sreda|\u010detvrtak|petak|subota");
                put("param_days", "danas|sutra|prekosutra");
                put("param_thisweek", "u\\s+nedelju|u\\s+ponedeljak|u\\s+utorak|u\\s+sredu|u\\s+\u010detvrtak|u\\s+petak|u\\s+subotu");
                put("param_nextweek", "slede\u0107e\\s+nedelje|slede\u0107eg\\s+ponedeljka|slede\u0107eg\\s+utorka|slede\u0107e\\s+srede|slede\u0107eg\\s+\u010detvrtka|slede\u0107eg\\s+petka|slede\u0107e\\s+subote");
            }
        };
    }
}
