package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_es {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_am", "a\\.\\s+m\\.");
            put("param_pm", "p\\.\\s+m\\.|p\\.m\\.");
            put("param_MMM", "ene\\.|feb\\.|mar\\.|abr\\.|may\\.|jun\\.|jul\\.|ago\\.|sept\\.|oct\\.|nov\\.|dic\\.");
            put("param_MMMM", "enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre");
            put("param_E", "dom\\.|lun\\.|mar\\.|mié\\.|jue\\.|vie\\.|sáb\\.");
            put("param_E2", "dom\\.|lun\\.|mar\\.|mié\\.|jue\\.|vie\\.|sáb\\.");
            put("param_EEEE", "domingo|lunes|martes|miércoles|jueves|viernes|sábado");
            put("param_days", "hoy|mañana|pasado\\s+mañana");
            put("param_thisweek", "este\\s+domingo|este\\s+lunes|este\\s+martes|este\\s+miércoles|este\\s+jueves|este\\s+viernes|este\\s+sábado");
            put("param_nextweek", "el\\s+próximo\\s+domingo|el\\s+próximo\\s+lunes|el\\s+próximo\\s+martes|el\\s+próximo\\s+miércoles|el\\s+próximo\\s+jueves|el\\s+próximo\\s+viernes|el\\s+próximo\\s+sábado");
        }
    };
}
