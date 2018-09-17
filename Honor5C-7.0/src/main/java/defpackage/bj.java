package defpackage;

import android.text.TextUtils;

/* renamed from: bj */
public class bj {
    public static byte[] a(byte[] bArr, String str) {
        byte[] bArr2 = null;
        try {
            bArr2 = bk.b(bArr, str);
        } catch (Throwable e) {
            aw.d("PushLog2828", "rsa encrypt data error ", e);
        }
        return bArr2;
    }

    public static byte[] b(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = null;
        try {
            bArr3 = bf.a(bArr, 0, bArr2, 0);
        } catch (Throwable e) {
            aw.d("PushLog2828", "InvalidKeyException:" + e.getMessage(), e);
        } catch (Throwable e2) {
            aw.d("PushLog2828", "BadPaddingException:" + e2.getMessage(), e2);
        } catch (Throwable e22) {
            aw.d("PushLog2828", "IllegalBlockSizeException:" + e22.getMessage(), e22);
        } catch (Throwable e222) {
            aw.d("PushLog2828", "NoSuchAlgorithmException:" + e222.getMessage(), e222);
        } catch (Throwable e2222) {
            aw.d("PushLog2828", "NoSuchPaddingException:" + e2222.getMessage(), e2222);
        } catch (Throwable e22222) {
            aw.d("PushLog2828", "Exception:" + e22222.getMessage(), e22222);
        }
        return bArr3;
    }

    public static String c(String str, byte[] bArr) {
        return TextUtils.isEmpty(str) ? "" : bd.a(str, bArr);
    }

    public static byte[] c(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = null;
        try {
            bArr3 = bf.a(bArr, bArr2, 0);
        } catch (Throwable e) {
            aw.d("PushLog2828", "InvalidKeyException:" + e.getMessage(), e);
        } catch (Throwable e2) {
            aw.d("PushLog2828", "BadPaddingException:" + e2.getMessage(), e2);
        } catch (Throwable e22) {
            aw.d("PushLog2828", "IllegalBlockSizeException:" + e22.getMessage(), e22);
        } catch (Throwable e222) {
            aw.d("PushLog2828", "NoSuchAlgorithmException:" + e222.getMessage(), e222);
        } catch (Throwable e2222) {
            aw.d("PushLog2828", "NoSuchPaddingException:" + e2222.getMessage(), e2222);
        } catch (Throwable e22222) {
            aw.d("PushLog2828", "Exception:" + e22222.getMessage(), e22222);
        }
        return bArr3;
    }

    public static String decrypter(String str) {
        return TextUtils.isEmpty(str) ? "" : bd.s(str);
    }

    public static String encrypter(String str) {
        return TextUtils.isEmpty(str) ? "" : bd.r(str);
    }
}
