package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_zh_hans {
    private HashMap<String, String> ampm2Time;
    public HashMap<String, String> date;

    public LocaleParamGet_zh_hans() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "\u4e0a\u5348|\u6e05\u6668|\u51cc\u6668|\u65e9[\u4e0a\u6668]?");
                put("param_pm", "\u4e0b\u5348|\u4e2d\u5348|\u665a\u4e0a|\u508d\u665a|\u534a\u591c|\u665a");
                put("param_MMM", "1\u6708|2\u6708|3\u6708|4\u6708|5\u6708|6\u6708|7\u6708|8\u6708|9\u6708|10\u6708|11\u6708|12\u6708");
                put("param_MMMM", "\u4e00\u6708|\u4e8c\u6708|\u4e09\u6708|\u56db\u6708|\u4e94\u6708|\u516d\u6708|\u4e03\u6708|\u516b\u6708|\u4e5d\u6708|\u5341\u6708|\u5341\u4e00\u6708|\u5341\u4e8c\u6708");
                put("param_E", "\u5468\u65e5|\u5468\u4e00|\u5468\u4e8c|\u5468\u4e09|\u5468\u56db|\u5468\u4e94|\u5468\u516d");
                put("param_E2", "\u5468\u65e5|\u5468\u4e00|\u5468\u4e8c|\u5468\u4e09|\u5468\u56db|\u5468\u4e94|\u5468\u516d");
                put("param_EEEE", "\u661f\u671f\u65e5|\u661f\u671f\u4e00|\u661f\u671f\u4e8c|\u661f\u671f\u4e09|\u661f\u671f\u56db|\u661f\u671f\u4e94|\u661f\u671f\u516d");
                put("param_days", "\u4eca[\u5929\u65e5]?(?!\u5e74)|\u660e[\u5929\u65e5]?(?!\u5e74)|\u540e[\u5929\u65e5](?!\u5e74)|\u5927\u540e[\u5929\u65e5](?!\u5e74)");
                put("param_thisweek", "\u672c\u5468\u65e5|\u672c\u5468\u4e00|\u672c\u5468\u4e8c|\u672c\u5468\u4e09|\u672c\u5468\u56db|\u672c\u5468\u4e94|\u672c\u5468\u516d");
                put("param_nextweek", "\u4e0b\u5468\u65e5|\u4e0b\u5468\u4e00|\u4e0b\u5468\u4e8c|\u4e0b\u5468\u4e09|\u4e0b\u5468\u56db|\u4e0b\u5468\u4e94|\u4e0b\u5468\u516d|\u4e0b\u4e0b\u5468\u65e5|\u4e0b\u4e0b\u5468\u4e00|\u4e0b\u4e0b\u5468\u4e8c|\u4e0b\u4e0b\u5468\u4e09|\u4e0b\u4e0b\u5468\u56db|\u4e0b\u4e0b\u5468\u4e94|\u4e0b\u4e0b\u5468\u516d");
                put("param_textyear", "\\u0604|\u4eca|\u660e|\u540e");
                put("param_textmonth", "\\u0604|\u672c|\u4e0b\u4e2a\uff1f");
                put("param_digit", "\\u0604|[\u96f6\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u4e24]+");
                put("param_digitMonth", "\\u0604|\u5341[\u4e00\u4e8c]|[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341]");
                put("param_digitDay", "\\u0604|\u4e09\u5341\u4e00?|\u4e8c?\u5341[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d]?|[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d]");
                put("param_digitHour", "\\u0604|\u4e8c\u5341[\u4e00\u4e8c\u4e09\u56db]?|\u5341[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d]?|[\u96f6\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u4e24]");
                put("param_digitMS", "\\u0604|[\u4e8c\u4e09\u56db\u4e94]?\u5341[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d]?|\u4e00\u5341|\u96f6[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d]");
                put("param_digitMS2", "\\u0604|[\u4e8c\u4e09\u56db\u4e94]?\u5341[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d]?|\u4e00\u5341|\u96f6?[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d]");
                put("param_timesuf", "\\u0604|\u949f|\u534a|\u6574|[\u4e00\u4e09]\u523b");
                put("param_pastForward", "\u6628[\u65e5\u5929]?\\(?|\u524d[\u65e5\u5929]\\(?|\u53bb\u5e74|\u524d\u5e74|\u4e0a\u6708|\u4e0a\u4e2a\u6708|\u4e0a\u4e2a?|1[0-9]{3}\u5e74");
                put("param_filtertext", "\u65e9\u4e00\u70b9|\u665a\u4e00\u70b9|\u660e\u540e\u5929|\u4e0a\u5348|\u6e05\u6668|\u51cc\u6668|\u65e9[\u4e0a\u6668]?|\u4e0b\u5348|\u4e2d\u5348|\u665a\u4e0a|\u508d\u665a|\u534a\u591c|\u665a|\u4eca|\u660e|\u540e|[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u4e24\u96f6]\u70b9[[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u96f6]]");
                put("mark_ShortDateLevel", "ymd");
            }
        };
        this.ampm2Time = new HashMap<String, String>() {
            {
                put("\u4e0a\u5348", "08:00");
                put("\u6e05\u6668", "04:00");
                put("\u51cc\u6668", "00:00");
                put("\u534a\u591c", "00:00");
                put("\u4e0b\u5348", "13:00");
                put("\u4e2d\u5348", "12:00");
                put("\u665a\u4e0a", "18:00");
                put("\u665a", "18:00");
                put("\u508d\u665a", "18:00");
                put("\u65e9", "5:00");
                put("\u65e9\u4e0a", "5:00");
                put("\u65e9\u6668", "5:00");
            }
        };
    }

    public String getAmPm(String str) {
        return (String) this.ampm2Time.get(str.trim());
    }
}
