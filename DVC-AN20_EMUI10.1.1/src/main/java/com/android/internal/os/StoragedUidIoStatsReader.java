package com.android.internal.os;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class StoragedUidIoStatsReader {
    private static final String TAG = StoragedUidIoStatsReader.class.getSimpleName();
    private static String sUidIoFile = "/proc/uid_io/stats";

    public interface Callback {
        void onUidStorageStats(int i, long j, long j2, long j3, long j4, long j5, long j6, long j7, long j8, long j9, long j10);
    }

    public StoragedUidIoStatsReader() {
    }

    @VisibleForTesting
    public StoragedUidIoStatsReader(String file) {
        sUidIoFile = file;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00d1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00d3, code lost:
        if (r0 != null) goto L_0x00d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00d9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00da, code lost:
        r0.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00de, code lost:
        throw r0;
     */
    public void readAbsolute(Callback callback) {
        int oldMask = StrictMode.allowThreadDiskReadsMask();
        try {
            BufferedReader reader = Files.newBufferedReader(new File(sUidIoFile).toPath());
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    reader.close();
                    break;
                }
                String[] fields = TextUtils.split(line, WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (fields.length != 11) {
                    String str = TAG;
                    Slog.e(str, "Malformed entry in " + sUidIoFile + ": " + line);
                } else {
                    try {
                        String str2 = fields[0];
                        callback.onUidStorageStats(Integer.parseInt(fields[0], 10), Long.parseLong(fields[1], 10), Long.parseLong(fields[2], 10), Long.parseLong(fields[3], 10), Long.parseLong(fields[4], 10), Long.parseLong(fields[5], 10), Long.parseLong(fields[6], 10), Long.parseLong(fields[7], 10), Long.parseLong(fields[8], 10), Long.parseLong(fields[9], 10), Long.parseLong(fields[10], 10));
                    } catch (NumberFormatException e) {
                        String str3 = TAG;
                        Slog.e(str3, "Could not parse entry in " + sUidIoFile + ": " + e.getMessage());
                    }
                }
            }
        } catch (IOException e2) {
            String str4 = TAG;
            Slog.e(str4, "Failed to read " + sUidIoFile + ": " + e2.getMessage());
        } catch (Throwable th) {
            StrictMode.setThreadPolicyMask(oldMask);
            throw th;
        }
        StrictMode.setThreadPolicyMask(oldMask);
    }
}
