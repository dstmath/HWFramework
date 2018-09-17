package com.android.server.rms.record;

import android.content.Context;
import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.rms.io.IOFileRotator.Reader;
import com.android.server.rms.io.IOFileRotator.Rewriter;
import huawei.com.android.server.policy.stylus.StylusGestureSettings;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

public class AppUsageTime implements Reader, Rewriter {
    private static final String TAG = "RMS.AppUsageTime";
    private static AppUsageTime mAppUsageTime;
    private final Context mContext;
    private final int mTimeforHistoryInstalled;
    private final HashMap<String, UsageInfo> mUsageTimeMap = new HashMap();

    static final class UsageInfo {
        private static final int ENTRY_BYTE_SIZE = 18;
        private static final String FORMAT_UTF = "UTF-8";
        private long mHistoryTime;
        private String mPkg;
        private long mUsageTime;

        UsageInfo(String pkg, long time, long historyTime) {
            this.mPkg = pkg;
            this.mUsageTime = time;
            this.mHistoryTime = historyTime;
        }

        private long getTotalBytesNum() {
            int pkgByteSize = 0;
            try {
                pkgByteSize = this.mPkg != null ? this.mPkg.getBytes(FORMAT_UTF).length : 0;
            } catch (Exception ex) {
                Log.e(AppUsageTime.TAG, "getTotalBytesNum, msg: " + ex.getMessage());
            }
            return (long) (pkgByteSize + 18);
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
        for (Entry entry : this.mUsageTimeMap.entrySet()) {
            UsageInfo info = (UsageInfo) entry.getValue();
            if (info != null) {
                if (historyTimeDump) {
                    if (Utils.HWFLOW) {
                        Log.i(TAG, "dumpInfo: pkg " + info.mPkg + ", usageTime " + info.mUsageTime + " history time " + info.mHistoryTime);
                    }
                } else if (Utils.HWFLOW) {
                    Log.i(TAG, "dumpInfo: pkg " + info.mPkg + ", usageTime is " + ((info.mUsageTime - info.mHistoryTime) / 1000) + StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX);
                }
            }
        }
    }

    private boolean existUsageInfoLocked(String pkg) {
        if (pkg == null || ((UsageInfo) this.mUsageTimeMap.get(pkg)) == null) {
            return false;
        }
        return true;
    }

    private boolean addUsageInfoLocked(String pkg, long time, long historyTime) {
        if (pkg == null || ((UsageInfo) this.mUsageTimeMap.get(pkg)) != null) {
            return false;
        }
        this.mUsageTimeMap.put(pkg, new UsageInfo(pkg, time, historyTime));
        return true;
    }

    private void setUsageTimeLocked(String pkg, long time) {
        if (pkg != null) {
            UsageInfo info = (UsageInfo) this.mUsageTimeMap.get(pkg);
            if (info != null) {
                info.mUsageTime = time;
            }
        }
    }

    public int getUsageTimeforUpload(String pkg) {
        if (pkg == null) {
            return 0;
        }
        long appUsageTime;
        synchronized (this.mUsageTimeMap) {
            if (existUsageInfoLocked(pkg)) {
                appUsageTime = getRealUsageTimeLocked(pkg);
                if (appUsageTime <= ((long) this.mTimeforHistoryInstalled)) {
                    setUsageTimeLocked(pkg, ResourceUtils.getAppTime(this.mContext, pkg));
                    appUsageTime = getRealUsageTimeLocked(pkg);
                } else if (Utils.DEBUG) {
                    Log.d(TAG, "getUsageTimeforUpload: history installed, time " + appUsageTime);
                }
            } else {
                appUsageTime = ResourceUtils.getAppTime(this.mContext, pkg);
                addUsageInfoLocked(pkg, appUsageTime, 0);
            }
        }
        if (Utils.DEBUG) {
            mAppUsageTime.dumpInfo(true);
        }
        return (int) (appUsageTime / 1000);
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
                UsageInfo info = (UsageInfo) this.mUsageTimeMap.get(pkg);
                if (info != null) {
                    info.mUsageTime = historyTime;
                    info.mHistoryTime = historyTime;
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
        UsageInfo info = (UsageInfo) this.mUsageTimeMap.get(pkg);
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
            for (Entry entry : this.mUsageTimeMap.entrySet()) {
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
            if (this.mUsageTimeMap.isEmpty()) {
                return;
            }
            this.mUsageTimeMap.clear();
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
            for (Entry entry : this.mUsageTimeMap.entrySet()) {
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
