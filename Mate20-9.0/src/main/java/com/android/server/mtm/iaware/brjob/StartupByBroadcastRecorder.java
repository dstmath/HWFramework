package com.android.server.mtm.iaware.brjob;

import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public final class StartupByBroadcastRecorder {
    private static final long MAX_RECORD_PERIOD_INTERVAL = 86400000;
    private static final String TAG = "StartupByBroadcastRecorder";
    private static StartupByBroadcastRecorder mInstance;
    private boolean DEBUG = false;
    private Object mLock = new Object();
    private HashMap<String, ArraySet<Long>> mStartupRecordMaps = new HashMap<>();

    private StartupByBroadcastRecorder() {
    }

    public static synchronized StartupByBroadcastRecorder getInstance() {
        StartupByBroadcastRecorder startupByBroadcastRecorder;
        synchronized (StartupByBroadcastRecorder.class) {
            if (mInstance == null) {
                mInstance = new StartupByBroadcastRecorder();
            }
            startupByBroadcastRecorder = mInstance;
        }
        return startupByBroadcastRecorder;
    }

    public void recordStartupTimeByBroadcast(String packageName, String action, long time) {
        if (this.DEBUG) {
            AwareLog.i(TAG, "iaware_brjob record startup time by broadcast begin.");
        }
        if (packageName == null || action == null || time == 0) {
            AwareLog.e(TAG, "iaware_brjob record startup time error");
            return;
        }
        synchronized (this.mLock) {
            String key = packageName + "&" + action;
            ArraySet<Long> occurTimes = this.mStartupRecordMaps.get(key);
            if (occurTimes == null) {
                occurTimes = new ArraySet<>();
            }
            occurTimes.add(Long.valueOf(time));
            this.mStartupRecordMaps.put(key, occurTimes);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006a, code lost:
        return 0;
     */
    public int getStartupCountsByBroadcast(String packageName, String action, long time) {
        String str = action;
        if (packageName == null || str == null) {
            AwareLog.e(TAG, "iaware_brjob get startup time error");
            return 0;
        }
        String key = r2 + "&" + str;
        synchronized (this.mLock) {
            ArraySet<Long> occurTimes = this.mStartupRecordMaps.get(key);
            if (occurTimes != null) {
                if (occurTimes.size() != 0) {
                    int counts = 0;
                    long currentTime = System.currentTimeMillis();
                    for (int i = occurTimes.size() - 1; i >= 0; i--) {
                        long timeTemp = occurTimes.valueAt(i).longValue();
                        if (currentTime > timeTemp) {
                            if (currentTime - timeTemp < time) {
                                counts++;
                            } else if (currentTime - timeTemp > 86400000) {
                                occurTimes.removeAt(i);
                            }
                        }
                    }
                    return counts;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0051, code lost:
        return true;
     */
    public boolean minTimeInterval(String packageName, String action, long time) {
        if (packageName == null || action == null) {
            AwareLog.e(TAG, "iaware_brjob min time interval error");
            return true;
        }
        String key = packageName + "&" + action;
        synchronized (this.mLock) {
            ArraySet<Long> occurTimes = this.mStartupRecordMaps.get(key);
            if (occurTimes != null) {
                if (occurTimes.size() != 0) {
                    long currentTime = System.currentTimeMillis();
                    long timeTemp = occurTimes.valueAt(occurTimes.size() - 1).longValue();
                    if (currentTime <= timeTemp || currentTime - timeTemp <= time) {
                        return false;
                    }
                    return true;
                }
            }
        }
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            if (this.mStartupRecordMaps.size() == 0) {
                pw.println("        There is no startup record now");
            }
            pw.println("        There are startup records now:");
            for (Map.Entry<String, ArraySet<Long>> entry : this.mStartupRecordMaps.entrySet()) {
                pw.println("            " + entry.getKey() + ", time: " + entry.getValue());
            }
        }
    }
}
