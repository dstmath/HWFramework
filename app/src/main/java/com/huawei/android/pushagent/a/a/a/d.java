package com.huawei.android.pushagent.a.a.a;

import com.huawei.android.pushagent.a.a.c;
import java.security.MessageDigest;

/* compiled from: Unknown */
public class d {
    public static String a(String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            instance.update(str.getBytes("UTF-8"));
            byte[] digest = instance.digest();
            StringBuffer stringBuffer = new StringBuffer(40);
            for (byte b : digest) {
                int i = b & 255;
                if (i < 16) {
                    stringBuffer.append('0');
                }
                stringBuffer.append(Integer.toHexString(i));
            }
            c.a("PushLogSC2606", "getSHA256str:" + stringBuffer.toString());
            return stringBuffer.toString();
        } catch (Throwable e) {
            c.c("PushLogSC2606", e.toString(), e);
            return str;
        } catch (Throwable e2) {
            c.c("PushLogSC2606", e2.toString(), e2);
            return str;
        } catch (Throwable e22) {
            c.c("PushLogSC2606", e22.toString(), e22);
            return str;
        }
    }
}
