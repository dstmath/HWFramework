package tmsdkobf;

import android.content.Context;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ok {
    public static fs a(Context context, byte[] bArr, byte[] bArr2, fs fsVar, boolean z) {
        if (bArr2 == null || bArr2.length == 0) {
            return null;
        }
        byte[] a = a(context, bArr, bArr2);
        return a != null ? ot.a(a, fsVar, z) : null;
    }

    public static fs a(byte[] bArr, byte[] bArr2, fs fsVar) {
        return a(null, bArr, bArr2, fsVar, false);
    }

    @Deprecated
    public static byte[] a(Context context, byte[] bArr, fs fsVar) {
        if (fsVar == null) {
            return null;
        }
        byte[] c = c(fsVar);
        return c != null ? TccCryptor.encrypt(c, bArr) : null;
    }

    public static byte[] a(Context context, byte[] bArr, byte[] bArr2) {
        if (bArr2 == null || bArr2.length == 0) {
            return null;
        }
        try {
            byte[] decrypt = TccCryptor.decrypt(bArr2, bArr);
            return decrypt != null ? r(decrypt) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] b(fs fsVar) {
        return ot.d(fsVar);
    }

    public static byte[] b(byte[] bArr, fs fsVar) {
        return a(null, bArr, fsVar);
    }

    public static fs c(byte[] bArr, fs fsVar) {
        return ot.a(bArr, fsVar, false);
    }

    public static byte[] c(fs fsVar) {
        if (fsVar == null) {
            return null;
        }
        byte[] q = q(ot.d(fsVar));
        return q != null ? q : null;
    }

    public static fs d(byte[] bArr, fs fsVar) {
        byte[] r = r(bArr);
        return r != null ? ot.a(r, fsVar, false) : null;
    }

    public static byte[] decrypt(byte[] bArr, byte[] bArr2) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        try {
            byte[] decrypt = TccCryptor.decrypt(bArr, bArr2);
            return decrypt != null ? decrypt : null;
        } catch (Throwable th) {
            th.printStackTrace();
            d.c("ConverterUtil", " decrypt() e: " + th.toString());
            return null;
        }
    }

    public static byte[] encrypt(byte[] bArr, byte[] bArr2) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        try {
            return TccCryptor.encrypt(bArr, bArr2);
        } catch (Throwable th) {
            th.printStackTrace();
            d.c("ConverterUtil", " encrypt() e: " + th.toString());
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static byte[] q(byte[] bArr) {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream);
        try {
            deflaterOutputStream.write(bArr);
            deflaterOutputStream.finish();
            byte[] toByteArray = byteArrayOutputStream.toByteArray();
            try {
                byteArrayOutputStream.close();
                deflaterOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return toByteArray;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        } catch (Throwable th) {
            try {
                byteArrayOutputStream.close();
                deflaterOutputStream.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static byte[] r(byte[] bArr) {
        InputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int read = inflaterInputStream.read();
            if (read == -1) {
                break;
            }
            try {
                byteArrayOutputStream.write(read);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (Throwable th) {
                try {
                    byteArrayInputStream.close();
                    inflaterInputStream.close();
                    byteArrayOutputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        byte[] toByteArray = byteArrayOutputStream.toByteArray();
        try {
            byteArrayInputStream.close();
            inflaterInputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        return toByteArray;
    }
}
