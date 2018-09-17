package com.android.internal.os;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.LongSparseLongArray;
import android.util.Slog;
import android.util.TimeUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class KernelMemoryBandwidthStats {
    private static final boolean DEBUG = false;
    private static final String TAG = "KernelMemoryBandwidthStats";
    private static final String mSysfsFile = "/sys/kernel/memory_state_time/show_stat";
    protected final LongSparseLongArray mBandwidthEntries = new LongSparseLongArray();
    private boolean mStatsDoNotExist = false;

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0044 A:{SYNTHETIC, Splitter: B:27:0x0044} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x007f A:{SYNTHETIC, Splitter: B:42:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0049 A:{SYNTHETIC, Splitter: B:30:0x0049} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateStats() {
        ThreadPolicy policy;
        IOException e;
        Throwable th;
        Throwable th2 = null;
        if (!this.mStatsDoNotExist) {
            policy = StrictMode.allowThreadDiskReads();
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(mSysfsFile));
                try {
                    parseStats(reader2);
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (FileNotFoundException e2) {
                            reader = reader2;
                        } catch (IOException e3) {
                            e = e3;
                            Slog.e(TAG, "Failed to read memory bandwidth: " + e.getMessage());
                            this.mBandwidthEntries.clear();
                            StrictMode.setThreadPolicy(policy);
                        } catch (Throwable th4) {
                            th = th4;
                            reader = reader2;
                            StrictMode.setThreadPolicy(policy);
                            throw th;
                        }
                    }
                    StrictMode.setThreadPolicy(policy);
                } catch (Throwable th5) {
                    th = th5;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Throwable th6) {
                            if (th2 == null) {
                                th2 = th6;
                            } else if (th2 != th6) {
                                th2.addSuppressed(th6);
                            }
                        }
                    }
                    if (th2 == null) {
                        try {
                            throw th2;
                        } catch (FileNotFoundException e4) {
                        } catch (IOException e5) {
                            e = e5;
                            Slog.e(TAG, "Failed to read memory bandwidth: " + e.getMessage());
                            this.mBandwidthEntries.clear();
                            StrictMode.setThreadPolicy(policy);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th7) {
                th = th7;
                if (reader != null) {
                }
                if (th2 == null) {
                }
            }
        } else {
            return;
        }
        try {
            Slog.w(TAG, "No kernel memory bandwidth stats available");
            this.mBandwidthEntries.clear();
            this.mStatsDoNotExist = true;
            StrictMode.setThreadPolicy(policy);
        } catch (Throwable th8) {
            th = th8;
            StrictMode.setThreadPolicy(policy);
            throw th;
        }
    }

    public void parseStats(BufferedReader reader) throws IOException {
        SimpleStringSplitter splitter = new SimpleStringSplitter(' ');
        this.mBandwidthEntries.clear();
        while (true) {
            String line = reader.readLine();
            if (line != null) {
                splitter.setString(line);
                splitter.next();
                int bandwidth = 0;
                while (true) {
                    int index = this.mBandwidthEntries.indexOfKey((long) bandwidth);
                    if (index >= 0) {
                        this.mBandwidthEntries.put((long) bandwidth, this.mBandwidthEntries.valueAt(index) + (Long.parseLong(splitter.next()) / TimeUtils.NANOS_PER_MS));
                    } else {
                        this.mBandwidthEntries.put((long) bandwidth, Long.parseLong(splitter.next()) / TimeUtils.NANOS_PER_MS);
                    }
                    bandwidth++;
                    if (splitter.hasNext()) {
                    }
                }
            } else {
                return;
            }
        }
    }

    public LongSparseLongArray getBandwidthEntries() {
        return this.mBandwidthEntries;
    }
}
