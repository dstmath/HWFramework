package com.huawei.i18n.tmr.datetime;

import com.huawei.android.os.storage.StorageManagerExt;
import java.util.Date;

public abstract class AbstractDateTmrHandle {
    private String locale;

    public abstract Date[] convertDate(String str, long j);

    public abstract int[] getTime(String str);

    AbstractDateTmrHandle(String locale2) {
        this.locale = locale2;
    }

    public String getLocale() {
        String str = this.locale;
        return str != null ? str : StorageManagerExt.INVALID_KEY_DESC;
    }
}
