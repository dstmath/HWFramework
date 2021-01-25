package com.android.server.pm;

import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

public class ShortcutDumpFiles {
    private static final boolean DEBUG = false;
    private static final String TAG = "ShortcutService";
    private final ShortcutService mService;

    public ShortcutDumpFiles(ShortcutService service) {
        this.mService = service;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0046, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0047, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004a, code lost:
        throw r6;
     */
    public boolean save(String filename, Consumer<PrintWriter> dumper) {
        File directory = this.mService.getDumpPath();
        directory.mkdirs();
        if (!directory.exists()) {
            Slog.e(TAG, "Failed to create directory: " + directory);
            return false;
        }
        PrintWriter pw = new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File(directory, filename))));
        dumper.accept(pw);
        try {
            $closeResource(null, pw);
            return true;
        } catch (IOException | RuntimeException e) {
            Slog.w(TAG, "Failed to create dump file: " + filename, e);
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

    public boolean save(String filename, byte[] utf8bytes) {
        return save(filename, new Consumer(utf8bytes) {
            /* class com.android.server.pm.$$Lambda$ShortcutDumpFiles$rwmVVp6PnQCcurF7D6VzrdNqEdk */
            private final /* synthetic */ byte[] f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((PrintWriter) obj).println(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(this.f$0)).toString());
            }
        });
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        $closeResource(null, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0064, code lost:
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0069, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x006a, code lost:
        $closeResource(r2, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006d, code lost:
        throw r3;
     */
    public void dumpAll(PrintWriter pw) {
        File directory = this.mService.getDumpPath();
        File[] files = directory.listFiles($$Lambda$ShortcutDumpFiles$v6wMz6MRa9pgSnEDM_9bjvrLaKY.INSTANCE);
        if (!directory.exists() || ArrayUtils.isEmpty(files)) {
            pw.print("  No dump files found.");
            return;
        }
        Arrays.sort(files, Comparator.comparing($$Lambda$ShortcutDumpFiles$stGgHzhhNVWPgDSwmH2ybAWRE8.INSTANCE));
        int length = files.length;
        int i = 0;
        while (i < length) {
            File path = files[i];
            pw.print("*** Dumping: ");
            pw.println(path.getName());
            pw.print("mtime: ");
            pw.println(ShortcutService.formatTime(path.lastModified()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    pw.println(line);
                } else {
                    try {
                        break;
                    } catch (IOException | RuntimeException e) {
                        Slog.w(TAG, "Failed to print dump files", e);
                        return;
                    }
                }
            }
        }
    }
}
