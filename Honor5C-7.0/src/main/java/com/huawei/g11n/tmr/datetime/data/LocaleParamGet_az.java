package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_az {
    public HashMap<String, String> date;

    public LocaleParamGet_az() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", ":");
                put("param_am", "AM");
                put("param_pm", "PM");
                put("param_MMM", "yan|fev|mar|apr|may|iyn|iyl|avq|sen|okt|noy|dek");
                put("param_MMMM", "yanvar|fevral|mart|aprel|may|iyun|iyul|avqust|sentyabr|oktyabr|noyabr|dekabr");
                put("param_E", "B\\.|B\\.E\\.|\u00c7\\.A\\.|\u00c7\\.|C\\.A\\.|C\\.|\u015e\\.");
                put("param_E2", "B\\.|B\\.E\\.|\u00c7\\.A\\.|\u00c7\\.|C\\.A\\.|C\\.|\u015e\\.");
                put("param_EEEE", "bazar|bazar\\s+ert\u0259si|\u00e7\u0259r\u015f\u0259nb\u0259\\s+ax\u015fam\u0131|\u00e7\u0259r\u015f\u0259nb\u0259|c\u00fcm\u0259\\s+ax\u015fam\u0131|c\u00fcm\u0259|\u015f\u0259nb\u0259");
                put("param_days", "bu\\s+g\u00fcn|sabah");
                put("param_thisweek", "bu\\s+bazar|bu\\s+bazar\\s+ert\u0259si|bu\\s+\u00e7\u0259r\u015f\u0259nb\u0259\\s+ax\u015fam\u0131|bu\\s+\u00e7\u0259r\u015f\u0259nb\u0259|bu\\s+c\u00fcm\u0259\\s+ax\u015fam\u0131|bu\\s+c\u00fcm\u0259|bu\\s+\u015f\u0259nb\u0259");
                put("param_nextweek", "g\u0259l\u0259n\\s+bazar|g\u0259l\u0259n\\s+bazar\\s+ert\u0259si|g\u0259l\u0259n\\s+\u00e7\u0259r\u015f\u0259nb\u0259\\s+ax\u015fam\u0131|g\u0259l\u0259n\\s+\u00e7\u0259r\u015f\u0259nb\u0259|g\u0259l\u0259n\\s+c\u00fcm\u0259\\s+ax\u015fam\u0131|g\u0259l\u0259n\\s+c\u00fcm\u0259|g\u0259l\u0259n\\s+\u015f\u0259nb\u0259");
            }
        };
    }
}
