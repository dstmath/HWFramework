package com.huawei.g11n.tmr;

import java.util.Date;

public abstract class AbstractDateTmrHandle {
    private String locale;

    public abstract Date[] convertDate(String str, long j);

    public abstract int[] getTime(String str);

    public AbstractDateTmrHandle(String str) {
        this.locale = str;
    }

    public String getLocale() {
        return this.locale == null ? "" : this.locale;
    }
}
