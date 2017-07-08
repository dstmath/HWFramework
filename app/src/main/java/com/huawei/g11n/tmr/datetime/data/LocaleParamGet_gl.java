package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_gl {
    public HashMap<String, String> date;

    public LocaleParamGet_gl() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", ":");
                put("param_am", "a\\.m\\.");
                put("param_pm", "p\\.m\\.");
                put("param_MMM", "xan|feb|mar|abr|mai|xu\u00f1|xul|ago|set|out|nov|dec");
                put("param_MMMM", "xaneiro|febreiro|marzo|abril|maio|xu\u00f1o|xullo|agosto|setembro|outubro|novembro|decembro");
                put("param_E", "Dom|Lun|Mar|M\u00e9r|Xov|Ven|S\u00e1b");
                put("param_E2", "Dom|Lun|Mar|M\u00e9r|Xov|Ven|S\u00e1b");
                put("param_EEEE", "Domingo|Luns|Martes|M\u00e9rcores|Xoves|Venres|S\u00e1bado");
                put("param_days", "hoxe|ma\u00f1\u00e1|pasadoma\u00f1\u00e1");
                put("param_thisweek", "este\\s+domingo|este\\s+luns|este\\s+martes|este\\s+m\u00e9rcores|este\\s+xoves|este\\s+venres|este\\s+s\u00e1bado");
                put("param_nextweek", "pr\u00f3ximo\\s+domingo|pr\u00f3ximo\\s+luns|pr\u00f3ximo\\s+martes|pr\u00f3ximo\\s+m\u00e9rcores|pr\u00f3ximo\\s+xoves|pr\u00f3ximo\\s+venres|pr\u00f3ximo\\s+s\u00e1bado");
            }
        };
    }
}
