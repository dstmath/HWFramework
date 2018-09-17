package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_sk {
    public HashMap<String, String> date;

    public LocaleParamGet_sk() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "AM");
                put("param_pm", "PM");
                put("param_MMM", "jan|feb|mar|apr|m\u00e1j|j\u00fan|j\u00fal|aug|sep|okt|nov|dec");
                put("param_MMMM", "janu\u00e1ra|febru\u00e1ra|marca|apr\u00edla|m\u00e1ja|j\u00fana|j\u00fala|augusta|septembra|okt\u00f3bra|novembra|decembra");
                put("param_E", "ne|po|ut|st|\u0161t|pi|so");
                put("param_E2", "ne|po|ut|st|\u0161t|pi|so");
                put("param_EEEE", "nede\u013ea|pondelok|utorok|streda|\u0161tvrtok|piatok|sobota");
                put("param_days", "Dnes|Zajtra|Pozajtra");
                put("param_thisweek", "T\u00fato\\s+nede\u013eu|Tento\\s+pondelok|Tento\\s+utorok|T\u00fato\\s+stredu|Tento\\s+\u0161tvrtok|Tento\\s+piatok|T\u00fato\\s+sobotu");
                put("param_nextweek", "Bud\u00facu\\s+nede\u013eu|Bud\u00faci\\s+pondelok|Bud\u00faci\\s+utorok|Bud\u00facu\\s+stredu|Bud\u00faci\\s+\u0161tvrtok|Bud\u00faci\\s+piatok|Bud\u00facu\\s+sobotu");
            }
        };
    }
}
