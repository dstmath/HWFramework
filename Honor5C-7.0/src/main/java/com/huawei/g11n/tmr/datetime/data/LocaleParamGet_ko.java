package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_ko {
    public HashMap<String, String> date;

    public LocaleParamGet_ko() {
        this.date = new HashMap<String, String>() {
            {
                put("param_am", "\uc624\uc804");
                put("param_pm", "\uc624\ud6c4");
                put("param_MMM", "1\uc6d4|2\uc6d4|3\uc6d4|4\uc6d4|5\uc6d4|6\uc6d4|7\uc6d4|8\uc6d4|9\uc6d4|10\uc6d4|11\uc6d4|12\uc6d4");
                put("param_MMMM", "1\uc6d4|2\uc6d4|3\uc6d4|4\uc6d4|5\uc6d4|6\uc6d4|7\uc6d4|8\uc6d4|9\uc6d4|10\uc6d4|11\uc6d4|12\uc6d4");
                put("param_E", "\uc77c|\uc6d4|\ud654|\uc218|\ubaa9|\uae08|\ud1a0");
                put("param_EEEE", "\uc77c\uc694\uc77c|\uc6d4\uc694\uc77c|\ud654\uc694\uc77c|\uc218\uc694\uc77c|\ubaa9\uc694\uc77c|\uae08\uc694\uc77c|\ud1a0\uc694\uc77c");
                put("param_days", "\uc624\ub298|\ub0b4\uc77c|\ubaa8\ub808");
                put("param_thisweek", "\uc774\ubc88\\s+\uc77c\uc694\uc77c|\uc774\ubc88\\s+\uc6d4\uc694\uc77c|\uc774\ubc88\\s+\ud654\uc694\uc77c|\uc774\ubc88\\s+\uc218\uc694\uc77c|\uc774\ubc88\\s+\ubaa9\uc694\uc77c|\uc774\ubc88\\s+\uae08\uc694\uc77c|\uc774\ubc88\\s+\ud1a0\uc694\uc77c");
                put("param_nextweek", "\ub2e4\uc74c\\s+\uc77c\uc694\uc77c|\ub2e4\uc74c\\s+\uc6d4\uc694\uc77c|\ub2e4\uc74c\\s+\ud654\uc694\uc77c|\ub2e4\uc74c\\s+\uc218\uc694\uc77c|\ub2e4\uc74c\\s+\ubaa9\uc694\uc77c|\ub2e4\uc74c\\s+\uae08\uc694\uc77c|\ub2e4\uc74c\\s+\ud1a0\uc694\uc77c");
                put("mark_ShortDateLevel", "ymd");
            }
        };
    }
}
