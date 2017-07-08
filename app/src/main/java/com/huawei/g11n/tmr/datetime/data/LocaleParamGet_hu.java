package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_hu {
    public HashMap<String, String> date;

    public LocaleParamGet_hu() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "de\\.");
                put("param_pm", "du\\.");
                put("param_MMM", "jan\\.|febr\\.|m\u00e1rc\\.|\u00e1pr\\.|m\u00e1j\\.|j\u00fan\\.|j\u00fal\\.|aug\\.|szept\\.|okt\\.|nov\\.|dec\\.");
                put("param_MMMM", "janu\u00e1r|febru\u00e1r|m\u00e1rcius|\u00e1prilis|m\u00e1jus|j\u00fanius|j\u00falius|augusztus|szeptember|okt\u00f3ber|november|december");
                put("param_E", "V|H|K|Sze|Cs|P|Szo");
                put("param_E2", "Sze|Cs|Szo");
                put("param_EEEE", "vas\u00e1rnap|h\u00e9tf\u0151|kedd|szerda|cs\u00fct\u00f6rt\u00f6k|p\u00e9ntek|szombat");
                put("param_days", "ma|holnap|holnaput\u00e1n");
                put("param_thisweek", "ez\\s+a\\s+vas\u00e1rnap|ez\\s+a\\s+h\u00e9tf\u0151|ez\\s+a\\s+kedd|ez\\s+a\\s+szerda|ez\\s+a\\s+cs\u00fct\u00f6rt\u00f6k|ez\\s+a\\s+p\u00e9ntek|ez\\s+a\\s+szombat");
                put("param_nextweek", "k\u00f6vetkez\u0151\\s+vas\u00e1rnap|k\u00f6vetkez\u0151\\s+h\u00e9tf\u0151|k\u00f6vetkez\u0151\\s+kedd|k\u00f6vetkez\u0151\\s+szerda|k\u00f6vetkez\u0151\\s+cs\u00fct\u00f6rt\u00f6k|k\u00f6vetkez\u0151\\s+p\u00e9ntek|k\u00f6vetkez\u0151\\s+szombat");
            }
        };
    }
}
