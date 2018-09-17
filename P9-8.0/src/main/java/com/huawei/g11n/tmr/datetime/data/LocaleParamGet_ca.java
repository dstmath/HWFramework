package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_ca {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_tmark", "[:.]");
            put("param_am", "a\\.m\\.|a\\.\\s+m\\.");
            put("param_pm", "p\\.\\s+m\\.|p\\.m\\.");
            put("param_MMM", "gen\\.|feb\\.|març|abr\\.|maig|juny|jul\\.|ag\\.|set\\.|oct\\.|nov\\.|des\\.");
            put("param_MMMM", "gener|febrer|març|abril|maig|juny|juliol|agost|setembre|octubre|novembre|desembre");
            put("param_E", "dg\\.|dl\\.|dt\\.|dc\\.|dj\\.|dv\\.|ds\\.");
            put("param_E2", "dg\\.|dl\\.|dt\\.|dc\\.|dj\\.|dv\\.|ds\\.");
            put("param_EEEE", "diumenge|dilluns|dimarts|dimecres|dijous|divendres|dissabte");
            put("param_days", "avui|demà|demà\\s+passat");
            put("param_thisweek", "aquest\\s+diumenge|aquest\\s+dilluns|aquest\\s+dimarts|aquest\\s+dimecres|aquest\\s+dijous|aquest\\s+divendres|aquest\\s+dissabte");
            put("param_nextweek", "diumenge\\s+que\\s+ve|dilluns\\s+que\\s+ve|dimarts\\s+que\\s+ve|dimecres\\s+que\\s+ve|dijous\\s+que\\s+ve|divendres\\s+que\\s+ve|dissabte\\s+que\\s+ve");
        }
    };
}
