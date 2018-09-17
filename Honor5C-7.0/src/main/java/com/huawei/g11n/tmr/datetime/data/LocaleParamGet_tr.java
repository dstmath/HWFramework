package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_tr {
    public HashMap<String, String> date;

    public LocaleParamGet_tr() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "\u00d6\u00d6");
                put("param_pm", "\u00d6S");
                put("param_MMM", "Oca|\u015eub|Mar|Nis|May|Haz|Tem|A\u011fu|Eyl|Eki|Kas|Ara");
                put("param_MMMM", "Ocak|\u015eubat|Mart|Nisan|May\u0131s|Haziran|Temmuz|A\u011fustos|Eyl\u00fcl|Ekim|Kas\u0131m|Aral\u0131k");
                put("param_E", "Paz|Pzt|Sal|\u00c7ar|Per|Cum|Cmt");
                put("param_E2", "Paz|Pzt|Sal|\u00c7ar|Per|Cum|Cmt");
                put("param_EEEE", "Pazar|Pazartesi|Sal\u0131|\u00c7ar\u015famba|Per\u015fembe|Cuma|Cumartesi");
                put("param_days", "bug\u00fcn|yar\u0131n|\u00f6b\u00fcr\\s+g\u00fcn");
                put("param_thisweek", "bu\\s+pazar|bu\\s+pazartesi|bu\\s+sal\u0131|bu\\s+\u00e7ar\u015famba|bu\\s+per\u015fembe|bu\\s+cuma|bu\\s+cumartesi");
                put("param_nextweek", "gelecek\\s+pazar|gelecek\\s+pazartesi|gelecek\\s+sal\u0131|gelecek\\s+\u00e7ar\u015famba|gelecek\\s+per\u015fembe|gelecek\\s+cuma|gelecek\\s+cumartesi");
            }
        };
    }
}
