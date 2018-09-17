package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_zh_hans {
    private HashMap<String, String> ampm2Time = new HashMap<String, String>() {
        {
            put("上午", "08:00");
            put("清晨", "04:00");
            put("凌晨", "00:00");
            put("半夜", "00:00");
            put("下午", "13:00");
            put("中午", "12:00");
            put("晚上", "18:00");
            put("晚", "18:00");
            put("傍晚", "18:00");
            put("早", "5:00");
            put("早上", "5:00");
            put("早晨", "5:00");
        }
    };
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_am", "上午|清晨|凌晨|早[上晨]?");
            put("param_pm", "下午|晚上|傍晚|半夜|晚");
            put("param_mm", "中午|正午");
            put("param_MMM", "1月|2月|3月|4月|5月|6月|7月|8月|9月|10月|11月|12月");
            put("param_MMMM", "一月|二月|三月|四月|五月|六月|七月|八月|九月|十月|十一月|十二月");
            put("param_E", "周日|周一|周二|周三|周四|周五|周六");
            put("param_E2", "周日|周一|周二|周三|周四|周五|周六");
            put("param_EEEE", "星期日|星期一|星期二|星期三|星期四|星期五|星期六");
            put("param_days", "今[天日]?(?!年)|明[天日]?(?!年)|后[天日](?!年)|大后[天日](?!年)");
            put("param_thisweek", "本周日|本周一|本周二|本周三|本周四|本周五|本周六");
            put("param_nextweek", "下周日|下周一|下周二|下周三|下周四|下周五|下周六|下下周日|下下周一|下下周二|下下周三|下下周四|下下周五|下下周六");
            put("param_textyear", "\\u0604|今|明|后");
            put("param_textmonth", "\\u0604|本|下个？");
            put("param_digit", "\\u0604|[零一二三四五六七八九十两]+");
            put("param_digitMonth", "\\u0604|十[一二]|[一二三四五六七八九十]");
            put("param_digitDay", "\\u0604|三十一?|二?十[一二三四五六七八九]?|[一二三四五六七八九]");
            put("param_digitHour", "\\u0604|二十[一二三四]?|十[一二三四五六七八九]?|[零一二三四五六七八九两]");
            put("param_digitMS", "\\u0604|[二三四五]?十[一二三四五六七八九]?|一十|零[一二三四五六七八九]");
            put("param_digitMS2", "\\u0604|[二三四五]?十[一二三四五六七八九]?|一十|零?[一二三四五六七八九]");
            put("param_timesuf", "\\u0604|钟|半|整|[一三]刻");
            put("param_pastForward", "昨[日天]?\\(?|前[日天]\\(?|去年|前年|上月|上个月|上个?|1[0-9]{3}年");
            put("param_filtertext", "早一点|晚一点|明后天|上午|清晨|凌晨|早[上晨]?|下午|中午|晚上|傍晚|半夜|晚|今|明|后|[一二三四五六七八九两零]点[[一二三四五六七八九零]]");
            put("mark_ShortDateLevel", "ymd");
        }
    };

    public String getAmPm(String content) {
        return (String) this.ampm2Time.get(content.trim());
    }
}
