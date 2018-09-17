package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_ja {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_am", "午前|午前");
            put("param_pm", "正午|午後");
            put("param_MMM", "1月|2月|3月|4月|5月|6月|7月|8月|9月|10月|11月|12月");
            put("param_MMMM", "一月|二月|三月|四月|五月|六月|七月|八月|九月|十月|十一月|十二月");
            put("param_E", "日|月|火|水|木|金|土");
            put("param_EEEE", "日曜日|月曜日|火曜日|水曜日|木曜日|金曜日|土曜日");
            put("param_days", "今日|明日|明後日");
            put("param_thisweek", "今週の日曜日|今週の月曜日|今週の火曜日|今週の水曜日|今週の木曜日|今週の金曜日|今週の土曜日");
            put("param_nextweek", "来週の日曜日|来週の月曜日|来週の火曜日|来週の水曜日|来週の木曜日|来週の金曜日|来週の土曜日");
            put("param_pastForward", "昨[日天]|一昨[日天]|昨年|先月");
            put("param_digit", "\\u0604|[零一二三四五六七八九十两]+");
            put("param_digitMonth", "\\u0604|十[一二]|[一二三四五六七八九十]");
            put("param_digitDay", "\\u0604|三十一?|二?十[一二三四五六七八九]?|[一二三四五六七八九]");
            put("param_textyear", "\\u0604|今|翌");
            put("param_textmonth", "\\u0604|今|翌");
            put("param_filtertext", "[0-9]+(日|月|火|水|木|金|土)");
            put("mark_ShortDateLevel", "ymd");
        }
    };
}
