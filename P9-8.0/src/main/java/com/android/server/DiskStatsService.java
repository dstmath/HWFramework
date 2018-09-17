package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.DumpUtils;
import com.android.server.storage.DiskStatsFileLogger;
import com.android.server.storage.DiskStatsLoggingService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import libcore.io.IoUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class DiskStatsService extends Binder {
    private static final String DISKSTATS_DUMP_FILE = "/data/system/diskstats_cache.json";
    private static final String TAG = "DiskStatsService";
    private final Context mContext;

    public DiskStatsService(Context context) {
        this.mContext = context;
        DiskStatsLoggingService.schedule(context);
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0108  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x006e  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0136  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00c7  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0151  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x015d  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00de  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e9 A:{SYNTHETIC, Splitter: B:42:0x00e9} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x006e  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0108  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00c7  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0136  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0151  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x015d  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00de  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00f4 A:{SYNTHETIC, Splitter: B:47:0x00f4} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IOException e;
        boolean protoFormat;
        ProtoOutputStream proto;
        boolean fileBased;
        Throwable th;
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, pw)) {
            long after;
            boolean blockBased;
            byte[] junk = new byte[512];
            for (int i = 0; i < junk.length; i++) {
                junk[i] = (byte) i;
            }
            File file = new File(Environment.getDataDirectory(), "system/perftest.tmp");
            FileOutputStream fos = null;
            IOException error = null;
            long before = SystemClock.uptimeMillis();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                try {
                    fileOutputStream.write(junk);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e2) {
                        }
                    }
                    fos = fileOutputStream;
                } catch (IOException e3) {
                    e = e3;
                    fos = fileOutputStream;
                    error = e;
                    if (fos != null) {
                    }
                    after = SystemClock.uptimeMillis();
                    if (file.exists()) {
                    }
                    protoFormat = hasOption(args, "--proto");
                    proto = null;
                    if (protoFormat) {
                    }
                    reportFreeSpace(Environment.getDataDirectory(), "Data", pw, proto, 0);
                    reportFreeSpace(Environment.getDownloadCacheDirectory(), "Cache", pw, proto, 1);
                    reportFreeSpace(new File("/system"), "System", pw, proto, 2);
                    fileBased = StorageManager.isFileEncryptedNativeOnly();
                    if (fileBased) {
                    }
                    if (protoFormat) {
                    }
                    if (protoFormat) {
                    }
                    if (protoFormat) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fos = fileOutputStream;
                    if (fos != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e = e4;
                error = e;
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e5) {
                    }
                }
                after = SystemClock.uptimeMillis();
                if (file.exists()) {
                }
                protoFormat = hasOption(args, "--proto");
                proto = null;
                if (protoFormat) {
                }
                reportFreeSpace(Environment.getDataDirectory(), "Data", pw, proto, 0);
                reportFreeSpace(Environment.getDownloadCacheDirectory(), "Cache", pw, proto, 1);
                reportFreeSpace(new File("/system"), "System", pw, proto, 2);
                fileBased = StorageManager.isFileEncryptedNativeOnly();
                if (fileBased) {
                }
                if (protoFormat) {
                }
                if (protoFormat) {
                }
                if (protoFormat) {
                }
            } catch (Throwable th3) {
                th = th3;
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
            after = SystemClock.uptimeMillis();
            if (file.exists()) {
                file.delete();
            }
            protoFormat = hasOption(args, "--proto");
            proto = null;
            if (protoFormat) {
                proto = new ProtoOutputStream(fd);
                pw = null;
                proto.write(1155346202625L, error != null);
                if (error != null) {
                    proto.write(1159641169922L, error.toString());
                } else {
                    proto.write(1112396529667L, after - before);
                }
            } else if (error != null) {
                pw.print("Test-Error: ");
                pw.println(error.toString());
            } else {
                pw.print("Latency: ");
                pw.print(after - before);
                pw.println("ms [512B Data Write]");
            }
            reportFreeSpace(Environment.getDataDirectory(), "Data", pw, proto, 0);
            reportFreeSpace(Environment.getDownloadCacheDirectory(), "Cache", pw, proto, 1);
            reportFreeSpace(new File("/system"), "System", pw, proto, 2);
            fileBased = StorageManager.isFileEncryptedNativeOnly();
            blockBased = fileBased ? false : StorageManager.isBlockEncrypted();
            if (protoFormat) {
                if (fileBased) {
                    proto.write(1168231104517L, 3);
                } else if (blockBased) {
                    proto.write(1168231104517L, 2);
                } else {
                    proto.write(1168231104517L, 1);
                }
            } else if (fileBased) {
                pw.println("File-based Encryption: true");
            }
            if (protoFormat) {
                reportCachedValuesProto(proto);
            } else {
                reportCachedValues(pw);
            }
            if (protoFormat) {
                proto.flush();
            }
        }
    }

    private void reportFreeSpace(File path, String name, PrintWriter pw, ProtoOutputStream proto, int folderType) {
        try {
            StatFs statfs = new StatFs(path.getPath());
            long bsize = (long) statfs.getBlockSize();
            long avail = (long) statfs.getAvailableBlocks();
            long total = (long) statfs.getBlockCount();
            if (bsize <= 0 || total <= 0) {
                throw new IllegalArgumentException("Invalid stat: bsize=" + bsize + " avail=" + avail + " total=" + total);
            }
            if (proto != null) {
                long freeSpaceToken = proto.start(2272037699588L);
                proto.write(1168231104513L, folderType);
                proto.write(1116691496962L, (avail * bsize) / 1024);
                proto.write(1116691496963L, (total * bsize) / 1024);
                proto.end(freeSpaceToken);
            } else {
                pw.print(name);
                pw.print("-Free: ");
                pw.print((avail * bsize) / 1024);
                pw.print("K / ");
                pw.print((total * bsize) / 1024);
                pw.print("K total = ");
                pw.print((100 * avail) / total);
                pw.println("% free");
            }
        } catch (IllegalArgumentException e) {
            if (proto == null) {
                pw.print(name);
                pw.print("-Error: ");
                pw.println(e.toString());
            }
        }
    }

    private boolean hasOption(String[] args, String arg) {
        for (String opt : args) {
            if (arg.equals(opt)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x00bd A:{Splitter: B:0:0x0000, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:2:0x00bd, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:3:0x00be, code:
            android.util.Log.w(TAG, "exception reading diskstats cache file", r0);
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void reportCachedValues(PrintWriter pw) {
        try {
            JSONObject json = new JSONObject(IoUtils.readFileAsString("/data/system/diskstats_cache.json"));
            pw.print("App Size: ");
            pw.println(json.getLong(DiskStatsFileLogger.APP_SIZE_AGG_KEY));
            pw.print("App Cache Size: ");
            pw.println(json.getLong(DiskStatsFileLogger.APP_CACHE_AGG_KEY));
            pw.print("Photos Size: ");
            pw.println(json.getLong(DiskStatsFileLogger.PHOTOS_KEY));
            pw.print("Videos Size: ");
            pw.println(json.getLong(DiskStatsFileLogger.VIDEOS_KEY));
            pw.print("Audio Size: ");
            pw.println(json.getLong(DiskStatsFileLogger.AUDIO_KEY));
            pw.print("Downloads Size: ");
            pw.println(json.getLong(DiskStatsFileLogger.DOWNLOADS_KEY));
            pw.print("System Size: ");
            pw.println(json.getLong(DiskStatsFileLogger.SYSTEM_KEY));
            pw.print("Other Size: ");
            pw.println(json.getLong(DiskStatsFileLogger.MISC_KEY));
            pw.print("Package Names: ");
            pw.println(json.getJSONArray(DiskStatsFileLogger.PACKAGE_NAMES_KEY));
            pw.print("App Sizes: ");
            pw.println(json.getJSONArray(DiskStatsFileLogger.APP_SIZES_KEY));
            pw.print("Cache Sizes: ");
            pw.println(json.getJSONArray(DiskStatsFileLogger.APP_CACHES_KEY));
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x015f A:{Splitter: B:0:0x0000, ExcHandler: java.io.IOException (r10_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:10:0x015f, code:
            r10 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0160, code:
            android.util.Log.w(TAG, "exception reading diskstats cache file", r10);
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void reportCachedValuesProto(ProtoOutputStream proto) {
        try {
            JSONObject json = new JSONObject(IoUtils.readFileAsString("/data/system/diskstats_cache.json"));
            long cachedValuesToken = proto.start(1172526071814L);
            proto.write(1116691496961L, json.getLong(DiskStatsFileLogger.APP_SIZE_AGG_KEY));
            proto.write(1116691496962L, json.getLong(DiskStatsFileLogger.APP_CACHE_AGG_KEY));
            proto.write(1116691496963L, json.getLong(DiskStatsFileLogger.PHOTOS_KEY));
            proto.write(1116691496964L, json.getLong(DiskStatsFileLogger.VIDEOS_KEY));
            proto.write(1116691496965L, json.getLong(DiskStatsFileLogger.AUDIO_KEY));
            proto.write(1116691496966L, json.getLong(DiskStatsFileLogger.DOWNLOADS_KEY));
            proto.write(1116691496967L, json.getLong(DiskStatsFileLogger.SYSTEM_KEY));
            proto.write(1116691496968L, json.getLong(DiskStatsFileLogger.MISC_KEY));
            JSONArray packageNamesArray = json.getJSONArray(DiskStatsFileLogger.PACKAGE_NAMES_KEY);
            JSONArray appSizesArray = json.getJSONArray(DiskStatsFileLogger.APP_SIZES_KEY);
            JSONArray cacheSizesArray = json.getJSONArray(DiskStatsFileLogger.APP_CACHES_KEY);
            int len = packageNamesArray.length();
            if (len == appSizesArray.length() && len == cacheSizesArray.length()) {
                for (int i = 0; i < len; i++) {
                    long packageToken = proto.start(2272037699593L);
                    proto.write(1159641169921L, packageNamesArray.getString(i));
                    proto.write(1116691496962L, appSizesArray.getLong(i));
                    proto.write(1116691496963L, cacheSizesArray.getLong(i));
                    proto.end(packageToken);
                }
            } else {
                Slog.wtf(TAG, "Sizes of packageNamesArray, appSizesArray and cacheSizesArray are not the same");
            }
            proto.end(cachedValuesToken);
        } catch (Exception e) {
        }
    }
}
