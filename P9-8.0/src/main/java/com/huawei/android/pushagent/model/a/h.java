package com.huawei.android.pushagent.model.a;

import android.content.Context;
import com.huawei.android.pushagent.utils.a.e;
import com.huawei.android.pushagent.utils.d.a;
import com.huawei.android.pushagent.utils.d.c;

public class h {
    private static final byte[] t = new byte[0];
    private static h u;
    private final a v;

    private h(Context context) {
        this.v = new a(context, "device_info");
    }

    public static h dp(Context context) {
        return dw(context);
    }

    private static h dw(Context context) {
        h hVar;
        synchronized (t) {
            if (u == null) {
                u = new h(context);
            }
            hVar = u;
        }
        return hVar;
    }

    public int dq() {
        return this.v.getInt("pushDeviceType", 1);
    }

    public boolean dz(int i) {
        c.sh("PushLog2951", "setDeviceType: " + i + "[1:NOT_GDPR, 2:GDPR]");
        return this.v.rq("pushDeviceType", Integer.valueOf(i));
    }

    public String dv() {
        return this.v.rt("pushDeviceId");
    }

    public boolean ds(String str) {
        return this.v.rq("pushDeviceId", str);
    }

    public String getDeviceId() {
        String str = "";
        try {
            return e.nu(dv());
        } catch (Throwable e) {
            c.se("PushLog2951", e.toString(), e);
            return str;
        }
    }

    public boolean dr(String str) {
        return ds(e.nv(str));
    }

    public int getDeviceIdType() {
        return this.v.getInt("pushDeviceIdType", -1);
    }

    public boolean setDeviceIdType(int i) {
        return this.v.rq("pushDeviceIdType", Integer.valueOf(i));
    }

    public String du() {
        return this.v.rt("deviceId_v2");
    }

    public boolean dx() {
        return this.v.rr("deviceId_v2");
    }

    public boolean dy() {
        return this.v.rr("macAddress");
    }

    public void dt() {
        this.v.rs();
    }
}
