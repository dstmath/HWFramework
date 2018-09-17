package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_ja {
    public HashMap<String, String> date;

    public LocaleParamGet_ja() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "\u5348\u524d|\u5348\u524d");
                put("param_pm", "\u6b63\u5348|\u5348\u5f8c");
                put("param_MMM", "1\u6708|2\u6708|3\u6708|4\u6708|5\u6708|6\u6708|7\u6708|8\u6708|9\u6708|10\u6708|11\u6708|12\u6708");
                put("param_MMMM", "\u4e00\u6708|\u4e8c\u6708|\u4e09\u6708|\u56db\u6708|\u4e94\u6708|\u516d\u6708|\u4e03\u6708|\u516b\u6708|\u4e5d\u6708|\u5341\u6708|\u5341\u4e00\u6708|\u5341\u4e8c\u6708");
                put("param_E", "\u65e5|\u6708|\u706b|\u6c34|\u6728|\u91d1|\u571f");
                put("param_EEEE", "\u65e5\u66dc\u65e5|\u6708\u66dc\u65e5|\u706b\u66dc\u65e5|\u6c34\u66dc\u65e5|\u6728\u66dc\u65e5|\u91d1\u66dc\u65e5|\u571f\u66dc\u65e5");
                put("param_days", "\u4eca\u65e5|\u660e\u65e5|\u660e\u5f8c\u65e5");
                put("param_thisweek", "\u4eca\u9031\u306e\u65e5\u66dc\u65e5|\u4eca\u9031\u306e\u6708\u66dc\u65e5|\u4eca\u9031\u306e\u706b\u66dc\u65e5|\u4eca\u9031\u306e\u6c34\u66dc\u65e5|\u4eca\u9031\u306e\u6728\u66dc\u65e5|\u4eca\u9031\u306e\u91d1\u66dc\u65e5|\u4eca\u9031\u306e\u571f\u66dc\u65e5");
                put("param_nextweek", "\u6765\u9031\u306e\u65e5\u66dc\u65e5|\u6765\u9031\u306e\u6708\u66dc\u65e5|\u6765\u9031\u306e\u706b\u66dc\u65e5|\u6765\u9031\u306e\u6c34\u66dc\u65e5|\u6765\u9031\u306e\u6728\u66dc\u65e5|\u6765\u9031\u306e\u91d1\u66dc\u65e5|\u6765\u9031\u306e\u571f\u66dc\u65e5");
                put("param_pastForward", "\u6628[\u65e5\u5929]|\u4e00\u6628[\u65e5\u5929]|\u6628\u5e74|\u5148\u6708");
                put("param_digit", "\\u0604|[\u96f6\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u4e24]+");
                put("param_digitMonth", "\\u0604|\u5341[\u4e00\u4e8c]|[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341]");
                put("param_digitDay", "\\u0604|\u4e09\u5341\u4e00?|\u4e8c?\u5341[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d]?|[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d]");
                put("param_textyear", "\\u0604|\u4eca|\u7fcc");
                put("param_textmonth", "\\u0604|\u4eca|\u7fcc");
                put("param_filtertext", "[0-9]+(\u65e5|\u6708|\u706b|\u6c34|\u6728|\u91d1|\u571f)");
                put("mark_ShortDateLevel", "ymd");
            }
        };
    }
}
