package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_en {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_tmark", ":");
            put("param_am", "AM|a\\.m\\.");
            put("param_pm", "PM|p\\.m\\.");
            put("param_mm", "noon");
            put("param_MMM", "Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec");
            put("param_MMMM", "January|February|March|April|May|June|July|August|September|October|November|December");
            put("param_E", "Sun|Mon|Tue|Wed|Thu|Fri|Sat");
            put("param_E2", "Sun|Mon|Tue|Wed|Thu|Fri|Sat");
            put("param_EEEE", "Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday");
            put("param_days", "today|tomorrow");
            put("param_thisweek", "this\\s+Sunday|this\\s+Monday|this\\s+Tuesday|this\\s+Wednesday|this\\s+Thursday|this\\s+Friday|this\\s+Saturday");
            put("param_nextweek", "next\\s+Sunday|next\\s+Monday|next\\s+Tuesday|next\\s+Wednesday|next\\s+Thursday|next\\s+Friday|next\\s+Saturday");
            put("mark_ShortDateLevel", "mdy");
        }
    };
}
