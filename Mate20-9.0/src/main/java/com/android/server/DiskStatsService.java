package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.IStoraged;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.DumpUtils;
import com.android.server.storage.DiskStatsFileLogger;
import com.android.server.storage.DiskStatsLoggingService;
import com.android.server.utils.PriorityDump;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import libcore.io.IoUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DiskStatsService extends Binder {
    private static final String DISKSTATS_DUMP_FILE = "/data/system/diskstats_cache.json";
    private static final String TAG = "DiskStatsService";
    private final Context mContext;

    public DiskStatsService(Context context) {
        this.mContext = context;
        DiskStatsLoggingService.schedule(context);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        PrintWriter pw2;
        ProtoOutputStream proto;
        PrintWriter printWriter = pw;
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, printWriter)) {
            byte[] junk = new byte[512];
            for (int i = 0; i < junk.length; i++) {
                junk[i] = (byte) i;
            }
            File tmp = new File(Environment.getDataDirectory(), "system/perftest.tmp");
            FileOutputStream fos = null;
            IOException error = null;
            long before = SystemClock.uptimeMillis();
            try {
                fos = new FileOutputStream(tmp);
                fos.write(junk);
                try {
                    fos.close();
                } catch (IOException e) {
                }
            } catch (IOException e2) {
                error = e2;
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Throwable th) {
                FileOutputStream fos2 = fos;
                Throwable th2 = th;
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (IOException e4) {
                    }
                }
                throw th2;
            }
            FileOutputStream fileOutputStream = fos;
            IOException error2 = error;
            long after = SystemClock.uptimeMillis();
            if (tmp.exists()) {
                tmp.delete();
            }
            boolean protoFormat = hasOption(args, PriorityDump.PROTO_ARG);
            if (protoFormat) {
                ProtoOutputStream proto2 = new ProtoOutputStream(fd);
                proto2.write(1133871366145L, error2 != null);
                if (error2 != null) {
                    proto2.write(1138166333442L, error2.toString());
                    File file = tmp;
                } else {
                    File file2 = tmp;
                    proto2.write(1120986464259L, after - before);
                }
                pw2 = null;
                proto = proto2;
            } else {
                FileDescriptor fileDescriptor = fd;
                File file3 = tmp;
                if (error2 != null) {
                    printWriter.print("Test-Error: ");
                    printWriter.println(error2.toString());
                } else {
                    printWriter.print("Latency: ");
                    printWriter.print(after - before);
                    printWriter.println("ms [512B Data Write]");
                }
                pw2 = printWriter;
                proto = null;
            }
            if (protoFormat) {
                reportDiskWriteSpeedProto(proto);
            } else {
                reportDiskWriteSpeed(pw2);
            }
            PrintWriter printWriter2 = pw2;
            IOException iOException = error2;
            ProtoOutputStream protoOutputStream = proto;
            reportFreeSpace(Environment.getDataDirectory(), "Data", printWriter2, protoOutputStream, 0);
            reportFreeSpace(Environment.getDownloadCacheDirectory(), "Cache", printWriter2, protoOutputStream, 1);
            reportFreeSpace(new File("/system"), "System", printWriter2, protoOutputStream, 2);
            boolean fileBased = StorageManager.isFileEncryptedNativeOnly();
            boolean blockBased = fileBased ? false : StorageManager.isBlockEncrypted();
            if (protoFormat) {
                if (fileBased) {
                    proto.write(1159641169925L, 3);
                } else if (blockBased) {
                    proto.write(1159641169925L, 2);
                } else {
                    proto.write(1159641169925L, 1);
                }
            } else if (fileBased) {
                pw2.println("File-based Encryption: true");
            }
            if (protoFormat) {
                reportCachedValuesProto(proto);
            } else {
                reportCachedValues(pw2);
            }
            if (protoFormat) {
                proto.flush();
            }
        }
    }

    private void reportFreeSpace(File path, String name, PrintWriter pw, ProtoOutputStream proto, int folderType) {
        String str = name;
        PrintWriter printWriter = pw;
        ProtoOutputStream protoOutputStream = proto;
        try {
            StatFs statfs = new StatFs(path.getPath());
            long bsize = (long) statfs.getBlockSize();
            long avail = (long) statfs.getAvailableBlocks();
            long total = (long) statfs.getBlockCount();
            if (bsize <= 0 || total <= 0) {
                throw new IllegalArgumentException("Invalid stat: bsize=" + bsize + " avail=" + avail + " total=" + total);
            }
            if (protoOutputStream != null) {
                long freeSpaceToken = protoOutputStream.start(2246267895812L);
                protoOutputStream.write(1159641169921L, folderType);
                protoOutputStream.write(1112396529666L, (avail * bsize) / 1024);
                protoOutputStream.write(1112396529667L, (total * bsize) / 1024);
                protoOutputStream.end(freeSpaceToken);
            } else {
                printWriter.print(str);
                printWriter.print("-Free: ");
                printWriter.print((avail * bsize) / 1024);
                printWriter.print("K / ");
                printWriter.print((total * bsize) / 1024);
                printWriter.print("K total = ");
                printWriter.print((100 * avail) / total);
                printWriter.println("% free");
            }
        } catch (IllegalArgumentException e) {
            if (protoOutputStream == null) {
                printWriter.print(str);
                printWriter.print("-Error: ");
                printWriter.println(e.toString());
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

    private void reportCachedValues(PrintWriter pw) {
        try {
            JSONObject json = new JSONObject(IoUtils.readFileAsString("/data/system/diskstats_cache.json"));
            pw.print("App Size: ");
            pw.println(json.getLong(DiskStatsFileLogger.APP_SIZE_AGG_KEY));
            pw.print("App Data Size: ");
            pw.println(json.getLong(DiskStatsFileLogger.APP_DATA_SIZE_AGG_KEY));
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
            pw.print("App Data Sizes: ");
            pw.println(json.getJSONArray(DiskStatsFileLogger.APP_DATA_KEY));
            pw.print("Cache Sizes: ");
            pw.println(json.getJSONArray(DiskStatsFileLogger.APP_CACHES_KEY));
        } catch (IOException | JSONException e) {
            Log.w(TAG, "exception reading diskstats cache file", e);
        }
    }

    private void reportCachedValuesProto(ProtoOutputStream proto) {
        ProtoOutputStream protoOutputStream = proto;
        try {
            JSONObject json = new JSONObject(IoUtils.readFileAsString("/data/system/diskstats_cache.json"));
            long cachedValuesToken = protoOutputStream.start(1146756268038L);
            protoOutputStream.write(1112396529665L, json.getLong(DiskStatsFileLogger.APP_SIZE_AGG_KEY));
            protoOutputStream.write(1112396529674L, json.getLong(DiskStatsFileLogger.APP_DATA_SIZE_AGG_KEY));
            protoOutputStream.write(1112396529666L, json.getLong(DiskStatsFileLogger.APP_CACHE_AGG_KEY));
            protoOutputStream.write(1112396529667L, json.getLong(DiskStatsFileLogger.PHOTOS_KEY));
            protoOutputStream.write(1112396529668L, json.getLong(DiskStatsFileLogger.VIDEOS_KEY));
            protoOutputStream.write(1112396529669L, json.getLong(DiskStatsFileLogger.AUDIO_KEY));
            protoOutputStream.write(1112396529670L, json.getLong(DiskStatsFileLogger.DOWNLOADS_KEY));
            protoOutputStream.write(1112396529671L, json.getLong(DiskStatsFileLogger.SYSTEM_KEY));
            protoOutputStream.write(1112396529672L, json.getLong(DiskStatsFileLogger.MISC_KEY));
            JSONArray packageNamesArray = json.getJSONArray(DiskStatsFileLogger.PACKAGE_NAMES_KEY);
            JSONArray appSizesArray = json.getJSONArray(DiskStatsFileLogger.APP_SIZES_KEY);
            JSONArray appDataSizesArray = json.getJSONArray(DiskStatsFileLogger.APP_DATA_KEY);
            JSONArray cacheSizesArray = json.getJSONArray(DiskStatsFileLogger.APP_CACHES_KEY);
            int len = packageNamesArray.length();
            if (len == appSizesArray.length() && len == appDataSizesArray.length() && len == cacheSizesArray.length()) {
                int i = 0;
                while (i < len) {
                    long packageToken = protoOutputStream.start(2246267895817L);
                    protoOutputStream.write(1138166333441L, packageNamesArray.getString(i));
                    protoOutputStream.write(1112396529666L, appSizesArray.getLong(i));
                    protoOutputStream.write(1112396529668L, appDataSizesArray.getLong(i));
                    protoOutputStream.write(1112396529667L, cacheSizesArray.getLong(i));
                    protoOutputStream.end(packageToken);
                    i++;
                    packageNamesArray = packageNamesArray;
                    appSizesArray = appSizesArray;
                }
                JSONArray jSONArray = appSizesArray;
            } else {
                JSONArray jSONArray2 = appSizesArray;
                Slog.wtf(TAG, "Sizes of packageNamesArray, appSizesArray, appDataSizesArray  and cacheSizesArray are not the same");
            }
            protoOutputStream.end(cachedValuesToken);
        } catch (IOException | JSONException e) {
            Log.w(TAG, "exception reading diskstats cache file", e);
        }
    }

    private int getRecentPerf() throws RemoteException, IllegalStateException {
        IBinder binder = ServiceManager.getService("storaged");
        if (binder != null) {
            return IStoraged.Stub.asInterface(binder).getRecentPerf();
        }
        throw new IllegalStateException("storaged not found");
    }

    private void reportDiskWriteSpeed(PrintWriter pw) {
        try {
            long perf = (long) getRecentPerf();
            if (perf != 0) {
                pw.print("Recent Disk Write Speed (kB/s) = ");
                pw.println(perf);
                return;
            }
            pw.println("Recent Disk Write Speed data unavailable");
            Log.w(TAG, "Recent Disk Write Speed data unavailable!");
        } catch (RemoteException | IllegalStateException e) {
            pw.println(e.toString());
            Log.e(TAG, e.toString());
        }
    }

    private void reportDiskWriteSpeedProto(ProtoOutputStream proto) {
        try {
            long perf = (long) getRecentPerf();
            if (perf != 0) {
                proto.write(1120986464263L, perf);
            } else {
                Log.w(TAG, "Recent Disk Write Speed data unavailable!");
            }
        } catch (RemoteException | IllegalStateException e) {
            Log.e(TAG, e.toString());
        }
    }
}
