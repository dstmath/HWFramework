package com.huawei.g11n.tmr;

import com.huawei.uikit.effect.BuildConfig;
import java.util.Date;

public abstract class AbstractDateTmrHandle {
    private String locale;

    public abstract Date[] convertDate(String str, long j);

    public abstract int[] getTime(String str);

    public AbstractDateTmrHandle(String locale2) {
        this.locale = locale2;
    }

    public String getLocale() {
        String str = this.locale;
        return str != null ? str : BuildConfig.FLAVOR;
    }
}
