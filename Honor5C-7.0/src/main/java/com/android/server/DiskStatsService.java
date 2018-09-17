package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class DiskStatsService extends Binder {
    private static final String TAG = "DiskStatsService";
    private final Context mContext;

    public DiskStatsService(Context context) {
        this.mContext = context;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IOException e;
        long after;
        Throwable th;
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        byte[] junk = new byte[DumpState.DUMP_MESSAGES];
        for (int i = 0; i < junk.length; i++) {
            junk[i] = (byte) i;
        }
        File tmp = new File(Environment.getDataDirectory(), "system/perftest.tmp");
        FileOutputStream fileOutputStream = null;
        IOException error = null;
        long before = SystemClock.uptimeMillis();
        try {
            FileOutputStream fos = new FileOutputStream(tmp);
            try {
                fos.write(junk);
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e2) {
                    }
                }
                fileOutputStream = fos;
            } catch (IOException e3) {
                e = e3;
                fileOutputStream = fos;
                error = e;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e4) {
                    }
                }
                after = SystemClock.uptimeMillis();
                if (tmp.exists()) {
                    tmp.delete();
                }
                if (error != null) {
                    pw.print("Test-Error: ");
                    pw.println(error.toString());
                } else {
                    pw.print("Latency: ");
                    pw.print(after - before);
                    pw.println("ms [512B Data Write]");
                }
                reportFreeSpace(Environment.getDataDirectory(), "Data", pw);
                reportFreeSpace(Environment.getDownloadCacheDirectory(), "Cache", pw);
                reportFreeSpace(new File("/system"), "System", pw);
                if (!StorageManager.isFileEncryptedNativeOnly()) {
                    pw.println("File-based Encryption: true");
                }
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = fos;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        } catch (IOException e6) {
            e = e6;
            error = e;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            after = SystemClock.uptimeMillis();
            if (tmp.exists()) {
                tmp.delete();
            }
            if (error != null) {
                pw.print("Latency: ");
                pw.print(after - before);
                pw.println("ms [512B Data Write]");
            } else {
                pw.print("Test-Error: ");
                pw.println(error.toString());
            }
            reportFreeSpace(Environment.getDataDirectory(), "Data", pw);
            reportFreeSpace(Environment.getDownloadCacheDirectory(), "Cache", pw);
            reportFreeSpace(new File("/system"), "System", pw);
            if (!StorageManager.isFileEncryptedNativeOnly()) {
                pw.println("File-based Encryption: true");
            }
        } catch (Throwable th3) {
            th = th3;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            throw th;
        }
        after = SystemClock.uptimeMillis();
        if (tmp.exists()) {
            tmp.delete();
        }
        if (error != null) {
            pw.print("Test-Error: ");
            pw.println(error.toString());
        } else {
            pw.print("Latency: ");
            pw.print(after - before);
            pw.println("ms [512B Data Write]");
        }
        reportFreeSpace(Environment.getDataDirectory(), "Data", pw);
        reportFreeSpace(Environment.getDownloadCacheDirectory(), "Cache", pw);
        reportFreeSpace(new File("/system"), "System", pw);
        if (!StorageManager.isFileEncryptedNativeOnly()) {
            pw.println("File-based Encryption: true");
        }
    }

    private void reportFreeSpace(File path, String name, PrintWriter pw) {
        try {
            StatFs statfs = new StatFs(path.getPath());
            long bsize = (long) statfs.getBlockSize();
            long avail = (long) statfs.getAvailableBlocks();
            long total = (long) statfs.getBlockCount();
            if (bsize <= 0 || total <= 0) {
                throw new IllegalArgumentException("Invalid stat: bsize=" + bsize + " avail=" + avail + " total=" + total);
            }
            pw.print(name);
            pw.print("-Free: ");
            pw.print((avail * bsize) / 1024);
            pw.print("K / ");
            pw.print((total * bsize) / 1024);
            pw.print("K total = ");
            pw.print((100 * avail) / total);
            pw.println("% free");
        } catch (IllegalArgumentException e) {
            pw.print(name);
            pw.print("-Error: ");
            pw.println(e.toString());
        }
    }
}
