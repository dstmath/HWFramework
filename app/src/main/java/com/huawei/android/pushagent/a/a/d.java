package com.huawei.android.pushagent.a.a;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.a.a.a.c;

/* compiled from: Unknown */
public class d {
    public static String a(Context context, String str, String str2) {
        String str3 = "";
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return str3;
        }
        Object b;
        try {
            b = c.b(context, new e(context, str).b(str2 + "_v2"));
        } catch (Throwable e) {
            c.c("PushLogSC2606", e.toString(), e);
            String str4 = str3;
        }
        if (TextUtils.isEmpty(b)) {
            c.a("PushLogSC2606", "not exist for:" + str2);
        }
        return b;
    }

    public static boolean a(Context context, String str, String str2, String str3) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return false;
        }
        return new e(context, str).a(str2 + "_v2", c.a(context, str3));
    }
}
