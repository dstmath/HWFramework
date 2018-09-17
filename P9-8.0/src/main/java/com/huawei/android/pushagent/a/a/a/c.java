package com.huawei.android.pushagent.a.a.a;

import android.text.TextUtils;
import com.huawei.android.pushagent.a.a.a;
import java.security.MessageDigest;

public class c {
    public static String a(String str) {
        String str2 = "";
        if (TextUtils.isEmpty(str)) {
            return str2;
        }
        try {
            return a.a(MessageDigest.getInstance("MD5").digest(b(str)));
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PushLogSC2907", "NoSuchAlgorithmException / " + e.toString(), e);
            return str2;
        } catch (Throwable e2) {
            com.huawei.android.pushagent.a.a.c.d("PushLogSC2907", "getMD5str failed:" + e2.toString(), e2);
            return str2;
        }
    }

    private static byte[] b(String str) {
        if (TextUtils.isEmpty(str)) {
            com.huawei.android.pushagent.a.a.c.d("PushLogSC2907", "getUTF8Bytes, str is empty");
            return new byte[0];
        }
        try {
            return str.getBytes("UTF-8");
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PushLogSC2907", "getBytes error:" + str, e);
            return new byte[0];
        }
    }
}
