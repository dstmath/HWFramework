package com.android.server.backup.utils;

import android.util.Slog;
import com.android.server.backup.BackupManagerService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public final class RandomAccessFileUtils {
    private static RandomAccessFile getRandomAccessFile(File file) throws FileNotFoundException {
        return new RandomAccessFile(file, "rwd");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000f, code lost:
        if (r0 != null) goto L_0x0011;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0011, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0014, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000e, code lost:
        r2 = move-exception;
     */
    public static void writeBoolean(File file, boolean b) {
        try {
            RandomAccessFile af = getRandomAccessFile(file);
            af.writeBoolean(b);
            $closeResource(null, af);
        } catch (IOException e) {
            Slog.w(BackupManagerService.TAG, "Error writing file:" + file.getAbsolutePath(), e);
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000f, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0010, code lost:
        if (r0 != null) goto L_0x0012;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0012, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0015, code lost:
        throw r2;
     */
    public static boolean readBoolean(File file, boolean def) {
        try {
            RandomAccessFile af = getRandomAccessFile(file);
            boolean readBoolean = af.readBoolean();
            $closeResource(null, af);
            return readBoolean;
        } catch (IOException e) {
            Slog.w(BackupManagerService.TAG, "Error reading file:" + file.getAbsolutePath(), e);
            return def;
        }
    }
}
