package com.huawei.android.pushagent.utils;

import android.app.ActivityManager;
import android.text.TextUtils;
import com.huawei.android.pushagent.utils.d.c;

public abstract class f {
    public static int vb() {
        return ActivityManager.getCurrentUser();
    }

    public static String vd(int i) {
        if (i < 0 || i > 99) {
            i = 0;
        }
        return vf(String.valueOf(i));
    }

    public static String vc(String str) {
        return vf(str);
    }

    public static int ve(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                c.sf("PushLog2951", "format userId failt, userId is: " + str);
            }
        }
        return vb();
    }

    private static String vf(String str) {
        if (TextUtils.isEmpty(str)) {
            return "00";
        }
        while (str.length() < 2) {
            str = "0" + str;
        }
        return str.substring(0, 2);
    }
}
