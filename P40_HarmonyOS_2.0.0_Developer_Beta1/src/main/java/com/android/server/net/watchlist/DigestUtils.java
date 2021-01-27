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

    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0015, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0018, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        r2 = move-exception;
     */
    public static byte[] getSha256Hash(File apkFile) throws IOException, NoSuchAlgorithmException {
        InputStream stream = new FileInputStream(apkFile);
        byte[] sha256Hash = getSha256Hash(stream);
        stream.close();
        return sha256Hash;
    }

    public static byte[] getSha256Hash(InputStream stream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digester = MessageDigest.getInstance("SHA256");
        byte[] buf = new byte[16384];
        while (true) {
            int bytesRead = stream.read(buf);
            if (bytesRead < 0) {
                return digester.digest();
            }
            digester.update(buf, 0, bytesRead);
        }
    }
}
