package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_it {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_am", "m\\.|AM");
            put("param_pm", "p\\.");
            put("param_MMM", "gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic");
            put("param_MMMM", "gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre");
            put("param_E", "dom|lun|mar|mer|gio|ven|sab");
            put("param_E2", "dom|lun|mar|mer|gio|ven|sab");
            put("param_EEEE", "domenica|lunedì|martedì|mercoledì|giovedì|venerdì|sabato");
            put("param_days", "oggi|domani|dopodomani");
            put("param_thisweek", "questa\\s+domenica|questo\\s+lunedì|questo\\s+martedì|questo\\s+mercoledì|questo\\s+giovedì|questo\\s+venerdì|questo\\s+sabato");
            put("param_nextweek", "domenica\\s+prossima|lunedì\\s+prossimo|martedì\\s+prossimo|mercoledì\\s+prossimo|giovedì\\s+prossimo|venerdì\\s+prossimo|sabato\\s+prossimo");
        }
    };
}
