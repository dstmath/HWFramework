package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_pt {
    public HashMap<String, String> date;

    public LocaleParamGet_pt() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "manh\u00e3");
                put("param_pm", "tarde|noite|meio-dia|p");
                put("param_MMM", "jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez");
                put("param_MMMM", "janeiro|fevereiro|mar\u00e7o|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro");
                put("param_E", "dom|seg|ter|qua|qui|sex|s\u00e1b");
                put("param_E2", "dom|seg|ter|qua|qui|sex|s\u00e1b");
                put("param_EEEE", "domingo|segunda-feira|ter\u00e7a-feira|quarta-feira|quinta-feira|sexta-feira|s\u00e1bado");
                put("param_days", "hoje|amanh\u00e3|depois\\s+de\\s+amanh\u00e3");
                put("param_thisweek", "este\\s+domingo|esta\\s+segunda-feira|esta\\s+ter\u00e7a-feira|esta\\s+quarta-feira|esta\\s+quinta-feira|esta\\s+sexta-feira|este\\s+s\u00e1bado");
                put("param_nextweek", "pr\u00f3ximo\\s+domingo|pr\u00f3xima\\s+segunda-feira|pr\u00f3xima\\s+ter\u00e7a-feira|pr\u00f3xima\\s+quarta-feira|pr\u00f3xima\\s+quinta-feira|pr\u00f3xima\\s+sexta-feira|pr\u00f3ximo\\s+s\u00e1bado");
            }
        };
    }
}
