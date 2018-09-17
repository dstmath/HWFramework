package defpackage;

import android.text.TextUtils;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* renamed from: bd */
class bd {
    public static String a(String str, byte[] bArr) {
        String str2 = null;
        if (!(TextUtils.isEmpty(str) || bArr == null || bArr.length <= 0)) {
            try {
                Key secretKeySpec = new SecretKeySpec(bArr, "AES");
                Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
                byte[] bArr2 = new byte[16];
                new SecureRandom().nextBytes(bArr2);
                instance.init(1, secretKeySpec, new IvParameterSpec(bArr2));
                str2 = bd.c(au.f(bArr2), au.f(instance.doFinal(str.getBytes("UTF-8"))));
            } catch (Throwable e) {
                aw.d("PushLog2828", "aes cbc encrypter data error", e);
            }
        }
        return str2;
    }

    private static byte[] a(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = null;
        if (!(bArr == null || bArr2 == null)) {
            int length = bArr.length;
            if (length == bArr2.length) {
                bArr3 = new byte[length];
                for (int i = 0; i < length; i++) {
                    bArr3[i] = (byte) (bArr[i] ^ bArr2[i]);
                }
            }
        }
        return bArr3;
    }

    public static String b(String str, byte[] bArr) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (bArr == null || bArr.length <= 0) {
            return "";
        }
        try {
            Key secretKeySpec = new SecretKeySpec(bArr, "AES");
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            String t = bd.t(str);
            String u = bd.u(str);
            if (TextUtils.isEmpty(t) || TextUtils.isEmpty(u)) {
                aw.i("PushLog2828", "ivParameter or encrypedWord is null");
                return "";
            }
            instance.init(2, secretKeySpec, new IvParameterSpec(au.m(t)));
            return new String(instance.doFinal(au.m(u)), "UTF-8");
        } catch (Throwable e) {
            aw.d("PushLog2828", "aes cbc decrypter data error", e);
            return "";
        }
    }

    private static String c(String str, String str2) {
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
            aw.e("PushLog2828", e.toString());
            return "";
        }
    }

    private static byte[] ce() {
        byte[] m = au.m(ax.bM());
        byte[] m2 = au.m(bg.bM());
        return bd.i(bd.a(bd.a(m, m2), au.m("2A57086C86EF54970C1E6EB37BFC72B1")));
    }

    private static byte[] i(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (byte) (bArr[i] >> 2);
        }
        return bArr;
    }

    public static String r(String str) {
        return TextUtils.isEmpty(str) ? null : bd.a(str, bd.ce());
    }

    public static String s(String str) {
        return TextUtils.isEmpty(str) ? "" : bd.b(str, bd.ce());
    }

    private static String t(String str) {
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
            aw.e("PushLog2828", e.toString());
            return "";
        }
    }

    private static String u(String str) {
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
            aw.e("PushLog2828", e.toString());
            return "";
        }
    }
}
