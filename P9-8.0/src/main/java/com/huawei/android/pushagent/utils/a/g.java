package com.huawei.android.pushagent.utils.a;

import android.text.TextUtils;
import com.huawei.android.pushagent.utils.d.b;
import com.huawei.android.pushagent.utils.d.c;
import java.security.MessageDigest;

public class g {
    public static String ol(String str) {
        String str2 = "";
        if (TextUtils.isEmpty(str)) {
            return str2;
        }
        try {
            return b.sb(MessageDigest.getInstance("MD5").digest(om(str)));
        } catch (Throwable e) {
            c.se("PushLog2951", "NoSuchAlgorithmException / " + e.toString(), e);
            return str2;
        } catch (Throwable e2) {
            c.se("PushLog2951", "getMD5str failed:" + e2.toString(), e2);
            return str2;
        }
    }

    private static byte[] om(String str) {
        if (TextUtils.isEmpty(str)) {
            c.sf("PushLog2951", "getUTF8Bytes, str is empty");
            return new byte[0];
        }
        try {
            return str.getBytes("UTF-8");
        } catch (Throwable e) {
            c.se("PushLog2951", "getBytes error:" + str, e);
            return new byte[0];
        }
    }
}
