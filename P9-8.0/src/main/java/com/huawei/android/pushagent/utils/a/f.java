package com.huawei.android.pushagent.utils.a;

import android.text.TextUtils;
import com.huawei.android.pushagent.utils.d.b;
import com.huawei.android.pushagent.utils.d.c;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class f {
    f() {
    }

    public static String ob(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return od(str, oh());
    }

    public static String od(String str, byte[] bArr) {
        if (TextUtils.isEmpty(str) || bArr == null || bArr.length <= 0) {
            return null;
        }
        try {
            Key secretKeySpec = new SecretKeySpec(bArr, "AES");
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bArr2 = new byte[16];
            new SecureRandom().nextBytes(bArr2);
            instance.init(1, secretKeySpec, new IvParameterSpec(bArr2));
            return ok(b.sb(bArr2), b.sb(instance.doFinal(str.getBytes("UTF-8"))));
        } catch (Throwable e) {
            c.se("PushLog2951", "aes cbc encrypter data error", e);
            return null;
        }
    }

    public static String oc(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return oe(str, oh());
    }

    public static String oe(String str, byte[] bArr) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (bArr == null || bArr.length <= 0) {
            return "";
        }
        try {
            Key secretKeySpec = new SecretKeySpec(bArr, "AES");
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Object oj = oj(str);
            Object oi = oi(str);
            if (TextUtils.isEmpty(oj) || TextUtils.isEmpty(oi)) {
                c.sh("PushLog2951", "ivParameter or encrypedWord is null");
                return "";
            }
            instance.init(2, secretKeySpec, new IvParameterSpec(b.sd(oj)));
            return new String(instance.doFinal(b.sd(oi)), "UTF-8");
        } catch (Throwable e) {
            c.se("PushLog2951", "aes cbc decrypter data error", e);
            return "";
        }
    }

    private static byte[] oh() {
        byte[] sd = b.sd(com.huawei.android.pushagent.constant.b.vm());
        byte[] sd2 = b.sd(c.nq());
        return og(of(of(sd, sd2), b.sd("2A57086C86EF54970C1E6EB37BFC72B1")));
    }

    private static byte[] of(byte[] bArr, byte[] bArr2) {
        if (bArr == null || bArr2 == null) {
            return null;
        }
        int length = bArr.length;
        if (length != bArr2.length) {
            return null;
        }
        byte[] bArr3 = new byte[length];
        for (int i = 0; i < length; i++) {
            bArr3[i] = (byte) (bArr[i] ^ bArr2[i]);
        }
        return bArr3;
    }

    private static byte[] og(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (byte) (bArr[i] >> 2);
        }
        return bArr;
    }

    private static String ok(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return "";
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str2.substring(0, 6));
            stringBuffer.append(str.substring(0, 6));
            stringBuffer.append(str2.substring(6, 10));
            stringBuffer.append(str.substring(6, 16));
            stringBuffer.append(str2.substring(10, 16));
            stringBuffer.append(str.substring(16));
            stringBuffer.append(str2.substring(16));
            return stringBuffer.toString();
        } catch (Exception e) {
            c.sf("PushLog2951", e.toString());
            return "";
        }
    }

    private static String oj(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str.substring(6, 12));
            stringBuffer.append(str.substring(16, 26));
            stringBuffer.append(str.substring(32, 48));
            return stringBuffer.toString();
        } catch (Exception e) {
            c.sf("PushLog2951", e.toString());
            return "";
        }
    }

    private static String oi(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str.substring(0, 6));
            stringBuffer.append(str.substring(12, 16));
            stringBuffer.append(str.substring(26, 32));
            stringBuffer.append(str.substring(48));
            return stringBuffer.toString();
        } catch (Exception e) {
            c.sf("PushLog2951", e.toString());
            return "";
        }
    }
}
