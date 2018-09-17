package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_et {
    public HashMap<String, String> date;

    public LocaleParamGet_et() {
        this.date = new HashMap<String, String>() {
            {
                put("param_tmark", ":");
                put("param_am", "AM");
                put("param_pm", "PM");
                put("param_MMM", "jaan|veebr|m\u00e4rts|apr|mai|juuni|juuli|aug|sept|okt|nov|dets");
                put("param_MMMM", "jaanuar|veebruar|m\u00e4rts|aprill|mai|juuni|juuli|august|september|oktoober|november|detsember");
                put("param_E", "P|E|T|K|N|R|L");
                put("param_E2", "P|E|T|K|N|R|L");
                put("param_EEEE", "p\u00fchap\u00e4ev|esmasp\u00e4ev|teisip\u00e4ev|kolmap\u00e4ev|neljap\u00e4ev|reede|laup\u00e4ev");
                put("param_days", "t\u00e4na|homme|\u00fclehomme");
                put("param_thisweek", "k\u00e4esolev\\s+p\u00fchap\u00e4ev|k\u00e4esolev\\s+esmasp\u00e4ev|k\u00e4esolev\\s+teisip\u00e4ev|k\u00e4esolev\\s+kolmap\u00e4ev|k\u00e4esolev\\s+neljap\u00e4ev|k\u00e4esolev\\s+reede|k\u00e4esolev\\s+laup\u00e4ev");
                put("param_nextweek", "j\u00e4rgmine\\s+p\u00fchap\u00e4ev|j\u00e4rgmine\\s+esmasp\u00e4ev|j\u00e4rgmine\\s+teisip\u00e4ev|j\u00e4rgmine\\s+kolmap\u00e4ev|j\u00e4rgmine\\s+neljap\u00e4ev|j\u00e4rgmine\\s+reede|j\u00e4rgmine\\s+laup\u00e4ev");
                put("param_filtertext", "P|E|T|K|N|R|L");
            }
        };
    }
}
