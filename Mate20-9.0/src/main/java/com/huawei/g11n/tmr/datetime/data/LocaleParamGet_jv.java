package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_jv {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_tmark", "\\.|:");
            put("param_am", "AWAN|esuk");
            put("param_pm", "BENGI|sore");
            put("param_MMM", "Jan|Feb|Mar|Apr|Mei|Jun|Jul|Agu|Sep|Okt|Nov|Des");
            put("param_MMMM", "Januari|Februari|Maret|April|Mei|Juni|Juli|Augustus|September|Oktober|November|Desember");
            put("param_E", "Min|Sen|Sel|Reb|Kem|Jem|Set");
            put("param_E2", "Min|Senin|Sel|Reb|Kem|Jumat|Set");
            put("param_EEEE", "Minggu|Senen|Selasa|Rebo|Kemis|Jemuwah|Setu");
            put("param_days", "Dina iki|Sesuk|Suk mben");
            put("param_thisweek", "iki\\s+Minggu|iki\\s+Senen|iki\\s+Selasa|iki\\s+Rebo|iki\\s+Kemis|iki\\s+Jemuwah|iki\\s+Setu");
            put("param_nextweek", "suk\\s+Minggu|suk\\s+Senen|suk\\s+Selasa|suk\\s+Rebo|suk\\s+Kemis|suk\\s+Jemuwah|suk\\s+Setu");
            put("param_period", "|ngantos|nganti|tekan");
        }
    };
}
