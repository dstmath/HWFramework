package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_it {
    public HashMap<String, String> date;

    public LocaleParamGet_it() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "m\\.|AM");
                put("param_pm", "p\\.");
                put("param_MMM", "gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic");
                put("param_MMMM", "gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre");
                put("param_E", "dom|lun|mar|mer|gio|ven|sab");
                put("param_E2", "dom|lun|mar|mer|gio|ven|sab");
                put("param_EEEE", "domenica|luned\u00ec|marted\u00ec|mercoled\u00ec|gioved\u00ec|venerd\u00ec|sabato");
                put("param_days", "oggi|domani|dopodomani");
                put("param_thisweek", "questa\\s+domenica|questo\\s+luned\u00ec|questo\\s+marted\u00ec|questo\\s+mercoled\u00ec|questo\\s+gioved\u00ec|questo\\s+venerd\u00ec|questo\\s+sabato");
                put("param_nextweek", "domenica\\s+prossima|luned\u00ec\\s+prossimo|marted\u00ec\\s+prossimo|mercoled\u00ec\\s+prossimo|gioved\u00ec\\s+prossimo|venerd\u00ec\\s+prossimo|sabato\\s+prossimo");
            }
        };
    }
}
