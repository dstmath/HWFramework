package tmsdkobf;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import tmsdk.common.TMSDKContext;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.f;

public class mk {
    public static <T> T a(Context context, String str, String str2, T t) {
        return a(context, str, str2, t, null);
    }

    public static <T> T a(Context context, String str, String str2, T -l_5_R, String str3) {
        if (str == null || str2 == null) {
            return -l_5_R;
        }
        T a;
        try {
            a = d(context, str, str3).a(str2, (Object) -l_5_R);
        } catch (Exception e) {
            e.printStackTrace();
            f.e("deepclean", "head error::" + e.toString());
            a = -l_5_R;
        }
        if (a == null) {
            a = -l_5_R;
        }
        return a;
    }

    public static <T> T b(Context context, String str, String str2, T -l_5_R, String str3) {
        if (str == null || str2 == null) {
            return -l_5_R;
        }
        T a;
        try {
            a = c(context, str, str3).a(str2, (Object) -l_5_R);
        } catch (Exception e) {
            e.printStackTrace();
            a = -l_5_R;
        }
        if (a == null) {
            a = -l_5_R;
        }
        return a;
    }

    private static fn c(Context -l_4_R, String str, String str2) {
        fn fnVar = new fn();
        Context currentContext = TMSDKContext.getCurrentContext();
        if (currentContext != null) {
            -l_4_R = currentContext;
        }
        try {
            InputStream open = -l_4_R.getResources().getAssets().open(str, 1);
            ls c = lt.c(open);
            byte[] bArr = new byte[open.available()];
            open.read(bArr);
            if (!lq.bytesToHexString(mc.l(bArr)).equals(lq.bytesToHexString(c.yU))) {
                return fnVar;
            }
            byte[] decrypt = TccCryptor.decrypt(bArr, null);
            if (str2 != null && str2.length() > 0) {
                fnVar.B(str2);
            }
            fnVar.b(decrypt);
            return fnVar;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x008b A:{SYNTHETIC, Splitter: B:50:0x008b} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0074 A:{SYNTHETIC, Splitter: B:38:0x0074} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static fn d(Context context, String str, String str2) {
        Exception e;
        Throwable th;
        fn fnVar = new fn();
        String b = lu.b(context, str, null);
        if (b == null || b.length() == 0) {
            return fnVar;
        }
        File file = new File(b);
        if (!file.exists()) {
            return fnVar;
        }
        FileInputStream fileInputStream = null;
        try {
            InputStream fileInputStream2 = new FileInputStream(file);
            InputStream inputStream;
            try {
                ls c = lt.c(fileInputStream2);
                byte[] bArr = new byte[fileInputStream2.available()];
                fileInputStream2.read(bArr);
                if (lq.bytesToHexString(mc.l(bArr)).equals(lq.bytesToHexString(c.yU))) {
                    byte[] decrypt = TccCryptor.decrypt(bArr, null);
                    if (str2 != null) {
                        if (str2.length() > 0) {
                            fnVar.B(str2);
                        }
                    }
                    fnVar.b(decrypt);
                    if (fileInputStream2 == null) {
                        inputStream = fileInputStream2;
                        return fnVar;
                    }
                    try {
                        fileInputStream2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    return fnVar;
                }
                fn fnVar2 = fnVar;
                if (fileInputStream2 == null) {
                    inputStream = fileInputStream2;
                } else {
                    try {
                        fileInputStream2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                return fnVar;
            } catch (Exception e4) {
                e = e4;
                fileInputStream = fileInputStream2;
                try {
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    return fnVar;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = fileInputStream2;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            e.printStackTrace();
            if (fileInputStream != null) {
            }
            return fnVar;
        }
    }
}
