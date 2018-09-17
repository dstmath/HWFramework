package com.huawei.android.pushagent.utils.a;

import android.text.TextUtils;
import com.huawei.android.pushagent.utils.d.c;

public class e {
    public static byte[] oa(byte[] bArr, byte[] bArr2) {
        return nz(bArr, bArr2, null);
    }

    public static byte[] nz(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        try {
            a nl = a.nl(bArr2);
            if (bArr3 == null) {
                return nl.nj(bArr);
            }
            return nl.nk(bArr, bArr3);
        } catch (Throwable e) {
            c.se("PushLog2951", "InvalidKeyException:" + e.getMessage(), e);
            return null;
        } catch (Throwable e2) {
            c.se("PushLog2951", "aesEncrypter get BadPaddingException:" + e2.getMessage(), e2);
            return null;
        } catch (Throwable e22) {
            c.se("PushLog2951", "IllegalBlockSizeException:" + e22.getMessage(), e22);
            return null;
        } catch (Throwable e222) {
            c.se("PushLog2951", "NoSuchAlgorithmException:" + e222.getMessage(), e222);
            return null;
        } catch (Throwable e2222) {
            c.se("PushLog2951", "NoSuchPaddingException:" + e2222.getMessage(), e2222);
            return null;
        } catch (Throwable e22222) {
            c.se("PushLog2951", "Exception:" + e22222.getMessage(), e22222);
            return null;
        }
    }

    public static byte[] nx(byte[] bArr, byte[] bArr2) {
        try {
            return a.nl(bArr2).ni(bArr);
        } catch (Throwable e) {
            c.se("PushLog2951", "InvalidKeyException:" + e.getMessage(), e);
            return null;
        } catch (Throwable e2) {
            c.se("PushLog2951", "BadPaddingException:" + e2.getMessage(), e2);
            return null;
        } catch (Throwable e22) {
            c.se("PushLog2951", "IllegalBlockSizeException:" + e22.getMessage(), e22);
            return null;
        } catch (Throwable e222) {
            c.se("PushLog2951", "NoSuchAlgorithmException:" + e222.getMessage(), e222);
            return null;
        } catch (Throwable e2222) {
            c.se("PushLog2951", "NoSuchPaddingException:" + e2222.getMessage(), e2222);
            return null;
        } catch (Throwable e22222) {
            c.se("PushLog2951", "Exception:" + e22222.getMessage(), e22222);
            return null;
        }
    }

    public static byte[] ny(byte[] bArr, String str) {
        byte[] bArr2 = null;
        try {
            return b.no(bArr, str);
        } catch (Throwable e) {
            c.se("PushLog2951", "rsa encrypt data error ", e);
            return bArr2;
        }
    }

    public static String nv(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return f.ob(str);
    }

    public static String nu(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return f.oc(str);
    }

    public static String nw(String str, byte[] bArr) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return f.od(str, bArr);
    }
}
