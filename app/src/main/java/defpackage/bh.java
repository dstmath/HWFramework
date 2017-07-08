package defpackage;

import android.text.TextUtils;
import java.security.MessageDigest;

/* renamed from: bh */
public class bh {
    public static String getMD5str(String str) {
        String str2 = "";
        if (!TextUtils.isEmpty(str)) {
            try {
                str2 = au.f(MessageDigest.getInstance("MD5").digest(bh.v(str)));
            } catch (Throwable e) {
                aw.d("PushLog2828", "NoSuchAlgorithmException / " + e.toString(), e);
            } catch (Throwable e2) {
                aw.d("PushLog2828", "getMD5str failed:" + e2.toString(), e2);
            }
        }
        return str2;
    }

    private static byte[] v(String str) {
        if (TextUtils.isEmpty(str)) {
            aw.e("PushLog2828", "getUTF8Bytes, str is empty");
            return new byte[0];
        }
        try {
            return str.getBytes("UTF-8");
        } catch (Throwable e) {
            aw.d("PushLog2828", "getBytes error:" + str, e);
            return new byte[0];
        }
    }
}
