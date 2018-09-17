package com.huawei.android.pushagent.a.a.a;

import android.text.TextUtils;
import com.huawei.android.pushagent.a.a.b;
import com.huawei.android.pushagent.a.a.c;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* compiled from: Unknown */
public class a {
    public static String a(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            Key secretKeySpec = new SecretKeySpec(a(), "AES");
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bArr = new byte[16];
            new SecureRandom().nextBytes(bArr);
            instance.init(1, secretKeySpec, new IvParameterSpec(bArr));
            return a(com.huawei.android.pushagent.a.a.a.a(bArr), com.huawei.android.pushagent.a.a.a.a(instance.doFinal(str.getBytes("UTF-8"))));
        } catch (Throwable e) {
            c.c("AES128_CBC", "aes cbc encrypter data error", e);
            return null;
        }
    }

    private static String a(String str, String str2) {
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
            c.d("AES128_CBC", e.toString());
            return "";
        }
    }

    private static byte[] a() {
        byte[] a = com.huawei.android.pushagent.a.a.a.a(b.a());
        byte[] a2 = com.huawei.android.pushagent.a.a.a.a(b.a());
        return a(a(a(a, a2), com.huawei.android.pushagent.a.a.a.a("2A57086C86EF54970C1E6EB37BFC72B1")));
    }

    private static byte[] a(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (byte) ((byte) (bArr[i] >> 2));
        }
        return bArr;
    }

    private static byte[] a(byte[] bArr, byte[] bArr2) {
        if (bArr == null || bArr2 == null) {
            return null;
        }
        int length = bArr.length;
        if (length != bArr2.length) {
            return null;
        }
        byte[] bArr3 = new byte[length];
        for (int i = 0; i < length; i++) {
            bArr3[i] = (byte) ((byte) (bArr[i] ^ bArr2[i]));
        }
        return bArr3;
    }

    public static String b(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            Key secretKeySpec = new SecretKeySpec(a(), "AES");
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            String c = c(str);
            String d = d(str);
            if (!TextUtils.isEmpty(c)) {
                if (!TextUtils.isEmpty(d)) {
                    instance.init(2, secretKeySpec, new IvParameterSpec(com.huawei.android.pushagent.a.a.a.a(c)));
                    return new String(instance.doFinal(com.huawei.android.pushagent.a.a.a.a(d)), "UTF-8");
                }
            }
            c.b("AES128_CBC", "ivParameter or encrypedWord is null");
            return "";
        } catch (Throwable e) {
            c.c("AES128_CBC", "aes cbc decrypter data error", e);
            return "";
        }
    }

    private static String c(String str) {
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
            c.d("AES128_CBC", e.toString());
            return "";
        }
    }

    private static String d(String str) {
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
            c.d("AES128_CBC", e.toString());
            return "";
        }
    }
}
