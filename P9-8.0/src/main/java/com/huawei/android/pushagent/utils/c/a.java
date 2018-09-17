package com.huawei.android.pushagent.utils.c;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.huawei.android.pushagent.utils.d.c;

public abstract class a implements c {
    private Context appCtx;

    public a(Context context) {
        this.appCtx = context.getApplicationContext();
    }

    public String getDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager) this.appCtx.getSystemService("phone");
        if (telephonyManager == null) {
            return null;
        }
        try {
            return telephonyManager.getDeviceId();
        } catch (Throwable e) {
            c.se("PushLog2951", "get deviceId meets exception", e);
            return null;
        }
    }
}
