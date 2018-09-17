package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_es_MX {
    public HashMap<String, String> date;

    public LocaleParamGet_es_MX() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "a\\.m\\.");
                put("param_pm", "p\\.m\\.");
                put("param_MMM", "ene\\.|febr\\.|mzo\\.|abr\\.|my\\.|jun\\.|jul\\.|ag\\.|set\\.|oct\\.|nov\\.|dic\\.");
                put("param_MMMM", "enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre");
                put("param_E", "dom\\.|lun\\.|mar\\.|mi\u00e9r\\.|jue\\.|vier\\.|s\u00e1b");
                put("param_E2", "dom\\.|lun\\.|mar\\.|mi\u00e9r\\.|jue\\.|vier\\.|s\u00e1b");
                put("param_EEEE", "domingo|lunes|martes|mi\u00e9rcoles|jueves|viernes|s\u00e1bado");
                put("param_days", "hoy|ma\u00f1ana|pasado\\s+ma\u00f1ana");
                put("param_thisweek", "este\\s+domingo|este\\s+lunes|este\\s+martes|este\\s+mi\u00e9rcoles|este\\s+jueves|este\\s+viernes|este\\s+s\u00e1bado");
                put("param_nextweek", "el\\s+domingo\\s+pr\u00f3ximo|el\\s+lunes\\s+pr\u00f3ximo|el\\s+martes\\s+pr\u00f3ximo|el\\s+mi\u00e9rcoles\\s+pr\u00f3ximo|el\\s+jueves\\s+pr\u00f3ximo|el\\s+viernes\\s+pr\u00f3ximo|el\\s+s\u00e1bado\\s+pr\u00f3ximo");
            }
        };
    }
}
