package com.android.server.mtm.iaware.brjob;

import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public final class StartupByBroadcastRecorder {
    private static final boolean DEBUG = false;
    private static final Object LOCK = new Object();
    private static final long MAX_RECORD_PERIOD_INTERVAL = 86400000;
    private static final String TAG = "StartupByBroadcastRecorder";
    private static StartupByBroadcastRecorder sInstance;
    private HashMap<String, ArraySet<Long>> mStartupRecordMaps = new HashMap<>();

    private StartupByBroadcastRecorder() {
    }

    public static StartupByBroadcastRecorder getInstance() {
        StartupByBroadcastRecorder startupByBroadcastRecorder;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new StartupByBroadcastRecorder();
            }
            startupByBroadcastRecorder = sInstance;
        }
        return startupByBroadcastRecorder;
    }

    public void recordStartupTimeByBroadcast(String packageName, String action, long time) {
        if (packageName == null || action == null || time == 0) {
            AwareLog.e(TAG, "iaware_brjob record startup time error");
            return;
        }
        synchronized (LOCK) {
            String key = packageName + "&" + action;
            ArraySet<Long> occurTimes = this.mStartupRecordMaps.get(key);
            if (occurTimes == null) {
                occurTimes = new ArraySet<>();
            }
            occurTimes.add(Long.valueOf(time));
            this.mStartupRecordMaps.put(key, occurTimes);
        }
    }

    public int getStartupCountsByBroadcast(String packageName, String action, long time) {
        if (packageName != null) {
            if (action != null) {
                String key = packageName + "&" + action;
                synchronized (LOCK) {
                    ArraySet<Long> occurTimes = this.mStartupRecordMaps.get(key);
                    if (occurTimes != null) {
                        if (occurTimes.size() != 0) {
                            int counts = 0;
                            long currentTime = System.currentTimeMillis();
                            for (int i = occurTimes.size() - 1; i >= 0; i--) {
                                long timeDelta = currentTime - occurTimes.valueAt(i).longValue();
                                if (timeDelta > 0) {
                                    if (timeDelta < time) {
                                        counts++;
                                    } else if (timeDelta > MAX_RECORD_PERIOD_INTERVAL) {
                                        occurTimes.removeAt(i);
                                    }
                                }
                            }
                            return counts;
                        }
                    }
                    return 0;
                }
            }
        }
        AwareLog.e(TAG, "iaware_brjob get startup time error");
        return 0;
    }

    public boolean minTimeInterval(String packageName, String action, long time) {
        if (packageName == null || action == null) {
            AwareLog.e(TAG, "iaware_brjob min time interval error");
            return true;
        }
        String key = packageName + "&" + action;
        synchronized (LOCK) {
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
            return true;
        }
    }

    public void dump(PrintWriter pw) {
        synchronized (LOCK) {
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
