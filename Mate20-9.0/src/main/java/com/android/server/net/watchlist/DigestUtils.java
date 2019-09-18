package com.android.server.net.watchlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {
    private static final int FILE_READ_BUFFER_SIZE = 16384;

    private DigestUtils() {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x000d, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000e, code lost:
        r2 = null;
     */
    public static byte[] getSha256Hash(File apkFile) throws IOException, NoSuchAlgorithmException {
        InputStream stream = new FileInputStream(apkFile);
        byte[] sha256Hash = getSha256Hash(stream);
        stream.close();
        return sha256Hash;
        throw th;
        if (r2 != null) {
            try {
                stream.close();
            } catch (Throwable th) {
                r2.addSuppressed(th);
            }
        } else {
            stream.close();
        }
        throw th;
    }

    public static byte[] getSha256Hash(InputStream stream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digester = MessageDigest.getInstance("SHA256");
        byte[] buf = new byte[16384];
        while (true) {
            int read = stream.read(buf);
            int bytesRead = read;
            if (read < 0) {
                return digester.digest();
            }
            digester.update(buf, 0, bytesRead);
        }
    }
}
