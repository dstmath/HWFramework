package com.android.server.backup.utils;

import android.util.Slog;
import com.android.server.backup.BackupManagerService;
import java.io.File;
import java.io.IOException;

public final class FileUtils {
    public static File createNewFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            Slog.w(BackupManagerService.TAG, "Failed to create file:" + file.getAbsolutePath(), e);
        }
        return file;
    }
}
