package com.huawei.android.pushagent.utils.c;

import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.huawei.android.pushagent.utils.d.c;

public class g {
    public String getDeviceId() {
        String serial;
        if (VERSION.SDK_INT >= 26) {
            try {
                serial = Build.getSerial();
            } catch (Throwable e) {
                c.se("PushLog2951", "get deviceId meets exception", e);
                serial = null;
            }
        } else {
            serial = null;
        }
        if (TextUtils.isEmpty(serial)) {
            serial = Build.SERIAL;
        }
        if ("unknown".equals(serial)) {
            return null;
        }
        return serial;
    }
}
