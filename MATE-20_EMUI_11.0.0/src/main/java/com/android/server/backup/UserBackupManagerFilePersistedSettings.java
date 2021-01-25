package com.android.server.backup;

import android.util.Slog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/* access modifiers changed from: package-private */
public final class UserBackupManagerFilePersistedSettings {
    private static final String BACKUP_ENABLE_FILE = "backup_enabled";

    UserBackupManagerFilePersistedSettings() {
    }

    static boolean readBackupEnableState(int userId) {
        return readBackupEnableState(UserBackupManagerFiles.getBaseStateDir(userId));
    }

    static void writeBackupEnableState(int userId, boolean enable) {
        writeBackupEnableState(UserBackupManagerFiles.getBaseStateDir(userId), enable);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0025, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0026, code lost:
        $closeResource(r4, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0029, code lost:
        throw r5;
     */
    private static boolean readBackupEnableState(File baseDir) {
        File enableFile = new File(baseDir, BACKUP_ENABLE_FILE);
        if (enableFile.exists()) {
            try {
                FileInputStream fin = new FileInputStream(enableFile);
                boolean z = fin.read() != 0;
                $closeResource(null, fin);
                return z;
            } catch (IOException e) {
                Slog.e(BackupManagerService.TAG, "Cannot read enable state; assuming disabled");
            }
        } else {
            Slog.i(BackupManagerService.TAG, "isBackupEnabled() => false due to absent settings file");
            return false;
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0028, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0029, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
        throw r4;
     */
    private static void writeBackupEnableState(File baseDir, boolean enable) {
        File enableFile = new File(baseDir, BACKUP_ENABLE_FILE);
        File stage = new File(baseDir, "backup_enabled-stage");
        FileOutputStream fout = new FileOutputStream(stage);
        fout.write(enable ? 1 : 0);
        fout.close();
        stage.renameTo(enableFile);
        try {
            $closeResource(null, fout);
        } catch (IOException | RuntimeException e) {
            Slog.e(BackupManagerService.TAG, "Unable to record backup enable state; reverting to disabled: " + e.getMessage());
            enableFile.delete();
            stage.delete();
        }
    }
}
