package com.android.server.rms.record;

import android.content.Context;
import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.rms.io.IOFileRotator;
import huawei.com.android.server.policy.stylus.StylusGestureSettings;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AppUsageTime implements IOFileRotator.Reader, IOFileRotator.Rewriter {
    private static final String TAG = "RMS.AppUsageTime";
    private static AppUsageTime mAppUsageTime;
    private final Context mContext;
    private final int mTimeforHistoryInstalled;
    private final HashMap<String, UsageInfo> mUsageTimeMap = new HashMap<>();

    static final class UsageInfo {
        private static final int ENTRY_BYTE_SIZE = 18;
        private static final String FORMAT_UTF = "UTF-8";
        /* access modifiers changed from: private */
        public long mHistoryTime;
        /* access modifiers changed from: private */
        public String mPkg;
        /* access modifiers changed from: private */
        public long mUsageTime;

        UsageInfo(String pkg, long time, long historyTime) {
            this.mPkg = pkg;
            this.mUsageTime = time;
            this.mHistoryTime = historyTime;
        }

        /* access modifiers changed from: private */
        public long getTotalBytesNum() {
            int i = 0;
            int pkgByteSize = 0;
            try {
                if (this.mPkg != null) {
                    i = this.mPkg.getBytes(FORMAT_UTF).length;
                }
                pkgByteSize = i;
            } catch (Exception ex) {
                Log.e(AppUsageTime.TAG, "getTotalBytesNum, msg: " + ex.getMessage());
            }
            return (long) (18 + pkgByteSize);
        }
    }

    private AppUsageTime(Context context, int time) {
        this.mContext = context;
        this.mTimeforHistoryInstalled = time;
    }

    public static synchronized AppUsageTime getInstance(Context context, int time) {
        AppUsageTime appUsageTime;
        synchronized (AppUsageTime.class) {
            if (mAppUsageTime == null) {
                mAppUsageTime = new AppUsageTime(context, time);
            }
            appUsageTime = mAppUsageTime;
        }
        return appUsageTime;
    }

    public static synchronized AppUsageTime self() {
        AppUsageTime appUsageTime;
        synchronized (AppUsageTime.class) {
            appUsageTime = mAppUsageTime;
        }
        return appUsageTime;
    }

    public void dumpInfo(boolean historyTimeDump) {
        if (Utils.HWFLOW) {
            Log.i(TAG, "dumpInfo: there is " + this.mUsageTimeMap.size() + " applicaitions to be recorded!");
        }
        for (Map.Entry<String, UsageInfo> entry : this.mUsageTimeMap.entrySet()) {
            UsageInfo info = (UsageInfo) entry.getValue();
            if (info != null) {
                if (!historyTimeDump) {
                    if (Utils.HWFLOW) {
                        Log.i(TAG, "dumpInfo: pkg " + info.mPkg + ", usageTime is " + ((info.mUsageTime - info.mHistoryTime) / 1000) + StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX);
                    }
                } else if (Utils.HWFLOW) {
                    Log.i(TAG, "dumpInfo: pkg " + info.mPkg + ", usageTime " + info.mUsageTime + " history time " + info.mHistoryTime);
                }
            }
        }
    }

    private boolean existUsageInfoLocked(String pkg) {
        if (pkg == null || this.mUsageTimeMap.get(pkg) == null) {
            return false;
        }
        return true;
    }

    private boolean addUsageInfoLocked(String pkg, long time, long historyTime) {
        if (pkg == null || this.mUsageTimeMap.get(pkg) != null) {
            return false;
        }
        UsageInfo usageInfo = new UsageInfo(pkg, time, historyTime);
        this.mUsageTimeMap.put(pkg, usageInfo);
        return true;
    }

    private void setUsageTimeLocked(String pkg, long time) {
        if (pkg != null) {
            UsageInfo info = this.mUsageTimeMap.get(pkg);
            if (info != null) {
                long unused = info.mUsageTime = time;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005b, code lost:
        if (android.rms.utils.Utils.DEBUG == false) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005d, code lost:
        mAppUsageTime.dumpInfo(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0068, code lost:
        return (int) (r0 / 1000);
     */
    public int getUsageTimeforUpload(String pkg) {
        long appUsageTime;
        if (pkg == null) {
            return 0;
        }
        synchronized (this.mUsageTimeMap) {
            try {
                if (!existUsageInfoLocked(pkg)) {
                    long appUsageTime2 = ResourceUtils.getAppTime(this.mContext, pkg);
                    try {
                        addUsageInfoLocked(pkg, appUsageTime2, 0);
                        appUsageTime = appUsageTime2;
                    } catch (Throwable th) {
                        th = th;
                        long j = appUsageTime2;
                        throw th;
                    }
                } else {
                    appUsageTime = getRealUsageTimeLocked(pkg);
                    if (appUsageTime <= ((long) this.mTimeforHistoryInstalled)) {
                        setUsageTimeLocked(pkg, ResourceUtils.getAppTime(this.mContext, pkg));
                        appUsageTime = getRealUsageTimeLocked(pkg);
                    } else if (Utils.DEBUG) {
                        Log.d(TAG, "getUsageTimeforUpload: history installed, time " + appUsageTime);
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    public boolean isHistoryInstalledApp(String pkg) {
        if (getRealUsageTimeLocked(pkg) > ((long) this.mTimeforHistoryInstalled)) {
            return true;
        }
        return false;
    }

    public void setHistoryTime(String pkg, long historyTime) {
        if (pkg != null) {
            synchronized (this.mUsageTimeMap) {
                UsageInfo info = this.mUsageTimeMap.get(pkg);
                if (info != null) {
                    long unused = info.mUsageTime = historyTime;
                    long unused2 = info.mHistoryTime = historyTime;
                } else {
                    addUsageInfoLocked(pkg, historyTime, historyTime);
                }
            }
        }
    }

    public long getRealUsageTimeLocked(String pkg) {
        if (pkg == null) {
            return 0;
        }
        UsageInfo info = this.mUsageTimeMap.get(pkg);
        if (info != null) {
            return info.mUsageTime - info.mHistoryTime;
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "getRealUsageTimeLocked: this isn't a recorded app and we should check UsageStatsManger");
        }
        return ResourceUtils.getAppTime(this.mContext, pkg);
    }

    public int getTotalBytes() {
        int totalBytes = 0;
        synchronized (this.mUsageTimeMap) {
            for (Map.Entry<String, UsageInfo> entry : this.mUsageTimeMap.entrySet()) {
                UsageInfo info = (UsageInfo) entry.getValue();
                if (info != null) {
                    totalBytes = (int) (((long) totalBytes) + info.getTotalBytesNum());
                }
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "total bytes is " + totalBytes);
        }
        return totalBytes;
    }

    public void clear() {
        synchronized (this.mUsageTimeMap) {
            if (!this.mUsageTimeMap.isEmpty()) {
                this.mUsageTimeMap.clear();
            }
        }
    }

    public void read(InputStream in) throws IOException {
        if (in == null) {
            Log.e(TAG, "read, InputStream is null");
            return;
        }
        DataInputStream inputStream = new DataInputStream(in);
        while (inputStream.available() >= 18) {
            addUsageInfoLocked(inputStream.readUTF(), inputStream.readLong(), inputStream.readLong());
        }
    }

    public void reset() {
    }

    public boolean shouldWrite() {
        return true;
    }

    public void write(OutputStream out) throws IOException {
        if (!this.mUsageTimeMap.isEmpty()) {
            if (out == null) {
                Log.e(TAG, "write, OutputStream is null");
                return;
            }
            if (Utils.HWFLOW) {
                dumpInfo(true);
            }
            DataOutputStream dataOutStream = new DataOutputStream(out);
            for (Map.Entry<String, UsageInfo> entry : this.mUsageTimeMap.entrySet()) {
                UsageInfo info = (UsageInfo) entry.getValue();
                if (info != null && info.mUsageTime > 0) {
                    dataOutStream.writeUTF(info.mPkg != null ? info.mPkg : "");
                    dataOutStream.writeLong(info.mUsageTime);
                    dataOutStream.writeLong(info.mHistoryTime);
                }
            }
            dataOutStream.flush();
        }
    }
}
