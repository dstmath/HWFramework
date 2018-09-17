package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_lt {
    public HashMap<String, String> date;

    public LocaleParamGet_lt() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", ":");
                put("param_am", "pr\\.p\\.");
                put("param_pm", "pop\\.");
                put("param_MMM", "saus\\.|vas\\.|kov\\.|bal\\.|geg\\.|bir\u017e\\.|liep\\.|rugp\\.|rugs\\.|spal\\.|lapkr\\.|gruod\\.");
                put("param_MMMM", "sausio|vasario|kovo|baland\u017eio|gegu\u017e\u0117s|bir\u017eelio|liepos|rugpj\u016b\u010dio|rugs\u0117jo|spalio|lapkri\u010dio|gruod\u017eio");
                put("param_E", "sk|pr|an|tr|kt|pn|\u0161t");
                put("param_E2", "sk|pr|an|tr|kt|pn|\u0161t");
                put("param_EEEE", "sekmadienis|pirmadienis|antradienis|tre\u010diadienis|ketvirtadienis|penktadienis|\u0161e\u0161tadienis");
                put("param_days", "\u0161iandien|rytoj|poryt");
                put("param_thisweek", "\u0161\u012f\\s+sekmadien\u012f|\u0161\u012f\\s+pirmadien\u012f|\u0161\u012f\\s+antradien\u012f|\u0161\u012f\\s+tre\u010diadien\u012f|\u0161\u012f\\s+ketvirtadien\u012f|\u0161\u012f\\s+penktadien\u012f|\u0161\u012f\\s+\u0161e\u0161tadien\u012f");
                put("param_nextweek", "kit\u0105\\s+sekmadien\u012f|kit\u0105\\s+pirmadien\u012f|kit\u0105\\s+antradien\u012f|kit\u0105\\s+tre\u010diadien\u012f|kit\u0105\\s+ketvirtadien\u012f|kit\u0105\\s+penktadien\u012f|kit\u0105\\s+\u0161e\u0161tadien\u012f");
                put("param_filtertext", "sk|pr|an|tr|kt|pn|\u0161t");
                put("mark_ShortDateLevel", "ymd");
            }
        };
    }
}
