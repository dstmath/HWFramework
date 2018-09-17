package tmsdk.common.utils;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import tmsdk.common.tcc.TccCryptor;
import tmsdkobf.fn;
import tmsdkobf.lq;
import tmsdkobf.ls;
import tmsdkobf.lt;
import tmsdkobf.mc;

public abstract class c {
    protected final String LA;
    private final String LB;
    private final String LC;
    public ls Lz;
    private Context mContext;

    public c(Context context, String str) {
        this.LA = str;
        this.LC = context.getFilesDir().getAbsolutePath();
        this.LB = context.getCacheDir().getAbsolutePath();
        this.mContext = context;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x006a A:{SYNTHETIC, Splitter: B:35:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0081 A:{SYNTHETIC, Splitter: B:47:0x0081} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected fn a(Context context, String str, String str2, boolean z) {
        Exception e;
        Throwable th;
        fn fnVar = new fn();
        File file = new File(str);
        if (!file.exists()) {
            return fnVar;
        }
        FileInputStream fileInputStream = null;
        try {
            InputStream fileInputStream2 = new FileInputStream(file);
            InputStream inputStream;
            try {
                ls c = lt.c(fileInputStream2);
                this.Lz = c;
                byte[] bArr = new byte[fileInputStream2.available()];
                fileInputStream2.read(bArr);
                if (z && !lq.bytesToHexString(mc.l(bArr)).equals(lq.bytesToHexString(c.yU))) {
                    fn fnVar2 = fnVar;
                    if (fileInputStream2 == null) {
                        inputStream = fileInputStream2;
                    } else {
                        try {
                            fileInputStream2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    return fnVar;
                }
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
                } catch (IOException e3) {
                    e3.printStackTrace();
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
                        } catch (IOException e32) {
                            e32.printStackTrace();
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

    public String iy() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.LC).append(File.separator).append(this.LA).append(".dat");
        return stringBuffer.toString();
    }
}
