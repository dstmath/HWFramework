package com.huawei.android.pushagent.a.a;

import android.content.Context;
import android.text.TextUtils;

public class d {
    public static String a(Context context, String str, String str2) {
        Object obj = "";
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return obj;
        }
        try {
            obj = com.huawei.android.pushagent.a.a.a.d.b(context, new e(context, str).b(str2 + "_v2"));
        } catch (Throwable e) {
            c.d("PushLogSC2907", e.toString(), e);
        }
        if (TextUtils.isEmpty(obj)) {
            c.a("PushLogSC2907", "not exist for:" + str2);
        }
        return obj;
    }

    public static boolean a(Context context, String str, String str2, String str3) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return false;
        }
        return new e(context, str).a(str2 + "_v2", com.huawei.android.pushagent.a.a.a.d.a(context, str3));
    }
}
