package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_es {
    public HashMap<String, String> date;

    public LocaleParamGet_es() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "a\\.\\s+m\\.");
                put("param_pm", "p\\.\\s+m\\.|p\\.m\\.");
                put("param_MMM", "ene\\.|feb\\.|mar\\.|abr\\.|may\\.|jun\\.|jul\\.|ago\\.|sept\\.|oct\\.|nov\\.|dic\\.");
                put("param_MMMM", "enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre");
                put("param_E", "dom\\.|lun\\.|mar\\.|mi\u00e9\\.|jue\\.|vie\\.|s\u00e1b\\.");
                put("param_E2", "dom\\.|lun\\.|mar\\.|mi\u00e9\\.|jue\\.|vie\\.|s\u00e1b\\.");
                put("param_EEEE", "domingo|lunes|martes|mi\u00e9rcoles|jueves|viernes|s\u00e1bado");
                put("param_days", "hoy|ma\u00f1ana|pasado\\s+ma\u00f1ana");
                put("param_thisweek", "este\\s+domingo|este\\s+lunes|este\\s+martes|este\\s+mi\u00e9rcoles|este\\s+jueves|este\\s+viernes|este\\s+s\u00e1bado");
                put("param_nextweek", "el\\s+pr\u00f3ximo\\s+domingo|el\\s+pr\u00f3ximo\\s+lunes|el\\s+pr\u00f3ximo\\s+martes|el\\s+pr\u00f3ximo\\s+mi\u00e9rcoles|el\\s+pr\u00f3ximo\\s+jueves|el\\s+pr\u00f3ximo\\s+viernes|el\\s+pr\u00f3ximo\\s+s\u00e1bado");
            }
        };
    }
}
