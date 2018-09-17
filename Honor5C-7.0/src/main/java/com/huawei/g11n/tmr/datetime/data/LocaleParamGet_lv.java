package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_lv {
    public HashMap<String, String> date;

    public LocaleParamGet_lv() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", ":");
                put("param_am", "AM");
                put("param_pm", "PM");
                put("param_MMM", "janv\\.|febr\\.|marts|apr\\.|maijs|j\u016bn\\.|j\u016bl\\.|aug\\.|sept\\.|okt\\.|nov\\.|dec\\.");
                put("param_MMMM", "janv\u0101r\u012b|febru\u0101r\u012b|mart\u0101|apr\u012bl\u012b|maij\u0101|j\u016bnij\u0101|j\u016blij\u0101|august\u0101|septembr\u012b|oktobr\u012b|novembr\u012b|decembr\u012b");
                put("param_E", "Sv|Pr|Ot|Tr|Ce|Pk|Se");
                put("param_E2", "Sv|Pr|Ot|Tr|Ce|Pk|Se");
                put("param_EEEE", "sv\u0113tdiena|pirmdiena|otrdiena|tre\u0161diena|ceturtdiena|piektdiena|sestdiena");
                put("param_days", "\u0161odien|r\u012bt|par\u012bt");
                put("param_thisweek", "\u0161aj\u0101\\s+sv\u0113tdien\u0101|\u0161aj\u0101\\s+pirmdien\u0101|\u0161aj\u0101\\s+otrdien\u0101|\u0161aj\u0101\\s+tre\u0161dien\u0101|\u0161aj\u0101\\s+ceturtdien\u0101|\u0161aj\u0101\\s+piektdien\u0101|\u0161aj\u0101\\s+sestdien\u0101");
                put("param_nextweek", "n\u0101kamaj\u0101\\s+sv\u0113tdien\u0101|n\u0101kamaj\u0101\\s+pirmdien\u0101|n\u0101kamaj\u0101\\s+otrdien\u0101|n\u0101kamaj\u0101\\s+tre\u0161dien\u0101|n\u0101kamaj\u0101\\s+ceturtdien\u0101|n\u0101kamaj\u0101\\s+piektdien\u0101|n\u0101kamaj\u0101\\s+sestdien\u0101");
            }
        };
    }
}
