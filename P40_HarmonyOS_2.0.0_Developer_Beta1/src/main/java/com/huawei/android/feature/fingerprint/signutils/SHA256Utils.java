package com.huawei.android.feature.fingerprint.signutils;

import android.util.Log;
import com.huawei.android.feature.utils.CommonUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class SHA256Utils {
    private static final int BUFF_SIZE = 4096;
    private static final String TAG = "SHA256Utils";

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0025, code lost:
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0042, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0043, code lost:
        r2 = r1;
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x005c, code lost:
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x005f, code lost:
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0062, code lost:
        r0 = r1;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0024 A[ExcHandler: NoSuchAlgorithmException (e java.security.NoSuchAlgorithmException), Splitter:B:12:0x0014] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0042 A[ExcHandler: all (r1v3 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:1:0x0004] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x004d A[ExcHandler: IOException | NoSuchAlgorithmException (e java.lang.Throwable), Splitter:B:1:0x0004] */
    public static byte[] digest(File file) {
        BufferedInputStream bufferedInputStream = null;
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bufferedInputStream2 = new BufferedInputStream(new FileInputStream(file));
            try {
                byte[] bArr = new byte[BUFF_SIZE];
                int i = 0;
                while (true) {
                    int read = bufferedInputStream2.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    i += read;
                    instance.update(bArr, 0, read);
                }
                if (i > 0) {
                    byte[] digest = instance.digest();
                    CommonUtils.closeQuietly((InputStream) bufferedInputStream2);
                    return digest;
                }
                CommonUtils.closeQuietly((InputStream) bufferedInputStream2);
                return new byte[0];
            } catch (NoSuchAlgorithmException e) {
            } catch (IOException e2) {
                bufferedInputStream = bufferedInputStream2;
            }
        } catch (IOException | NoSuchAlgorithmException e3) {
        } catch (IOException e4) {
            try {
                Log.e(TAG, "An exception occurred while computing file 'SHA-256'.");
                CommonUtils.closeQuietly((InputStream) bufferedInputStream);
                return new byte[0];
            } catch (Throwable th) {
                Throwable th2 = th;
                BufferedInputStream bufferedInputStream3 = bufferedInputStream;
                CommonUtils.closeQuietly((InputStream) bufferedInputStream3);
                throw th2;
            }
        } catch (Throwable th3) {
        }
    }

    public static byte[] digest(byte[] bArr) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bArr);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException" + e.getMessage());
            return new byte[0];
        }
    }
}
