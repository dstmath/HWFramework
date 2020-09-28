package com.android.internal.os;

import android.os.StrictMode;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.LongSparseLongArray;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.VisibleForTesting;
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0029, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002d, code lost:
        throw r6;
     */
    public void updateStats() {
        if (!this.mStatsDoNotExist) {
            long startTime = SystemClock.uptimeMillis();
            StrictMode.ThreadPolicy policy = StrictMode.allowThreadDiskReads();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(mSysfsFile));
                parseStats(reader);
                reader.close();
            } catch (FileNotFoundException e) {
                Slog.w(TAG, "No kernel memory bandwidth stats available");
                this.mBandwidthEntries.clear();
                this.mStatsDoNotExist = true;
            } catch (IOException e2) {
                Slog.e(TAG, "Failed to read memory bandwidth: " + e2.getMessage());
                this.mBandwidthEntries.clear();
            } catch (Throwable th) {
                StrictMode.setThreadPolicy(policy);
                throw th;
            }
            StrictMode.setThreadPolicy(policy);
            long readTime = SystemClock.uptimeMillis() - startTime;
            if (readTime > 100) {
                Slog.w(TAG, "Reading memory bandwidth file took " + readTime + "ms");
            }
        }
    }

    @VisibleForTesting
    public void parseStats(BufferedReader reader) throws IOException {
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
        this.mBandwidthEntries.clear();
        while (true) {
            String line = reader.readLine();
            if (line != null) {
                splitter.setString(line);
                splitter.next();
                int bandwidth = 0;
                do {
                    int index = this.mBandwidthEntries.indexOfKey((long) bandwidth);
                    if (index >= 0) {
                        LongSparseLongArray longSparseLongArray = this.mBandwidthEntries;
                        longSparseLongArray.put((long) bandwidth, longSparseLongArray.valueAt(index) + (Long.parseLong(splitter.next()) / TimeUtils.NANOS_PER_MS));
                    } else {
                        this.mBandwidthEntries.put((long) bandwidth, Long.parseLong(splitter.next()) / TimeUtils.NANOS_PER_MS);
                    }
                    bandwidth++;
                } while (splitter.hasNext());
            } else {
                return;
            }
        }
    }

    public LongSparseLongArray getBandwidthEntries() {
        return this.mBandwidthEntries;
    }
}
