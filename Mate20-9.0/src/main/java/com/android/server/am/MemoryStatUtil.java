package com.android.server.am;

import android.os.FileUtils;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MemoryStatUtil {
    private static final Pattern CACHE_IN_BYTES = Pattern.compile("total_cache (\\d+)");
    private static final String MEMCG_TEST_PATH = "/dev/memcg/apps/memory.stat";
    private static final String MEMORY_STAT_FILE_FMT = "/dev/memcg/apps/uid_%d/pid_%d/memory.stat";
    private static final Pattern PGFAULT = Pattern.compile("total_pgfault (\\d+)");
    private static final int PGFAULT_INDEX = 9;
    private static final Pattern PGMAJFAULT = Pattern.compile("total_pgmajfault (\\d+)");
    private static final int PGMAJFAULT_INDEX = 11;
    private static final String PROC_STAT_FILE_FMT = "/proc/%d/stat";
    private static final Pattern RSS_IN_BYTES = Pattern.compile("total_rss (\\d+)");
    private static final int RSS_IN_BYTES_INDEX = 23;
    private static final Pattern SWAP_IN_BYTES = Pattern.compile("total_swap (\\d+)");
    private static final String TAG = "ActivityManager";
    private static volatile Boolean sDeviceHasMemCg;

    static final class MemoryStat {
        long cacheInBytes;
        long pgfault;
        long pgmajfault;
        long rssInBytes;
        long swapInBytes;

        MemoryStat() {
        }
    }

    private MemoryStatUtil() {
    }

    static MemoryStat readMemoryStatFromFilesystem(int uid, int pid) {
        return hasMemcg() ? readMemoryStatFromMemcg(uid, pid) : readMemoryStatFromProcfs(pid);
    }

    static MemoryStat readMemoryStatFromMemcg(int uid, int pid) {
        return parseMemoryStatFromMemcg(readFileContents(String.format(Locale.US, MEMORY_STAT_FILE_FMT, new Object[]{Integer.valueOf(uid), Integer.valueOf(pid)})));
    }

    static MemoryStat readMemoryStatFromProcfs(int pid) {
        return parseMemoryStatFromProcfs(readFileContents(String.format(Locale.US, PROC_STAT_FILE_FMT, new Object[]{Integer.valueOf(pid)})));
    }

    private static String readFileContents(String path) {
        File file = new File(path);
        if (!file.exists()) {
            if (ActivityManagerDebugConfig.DEBUG_METRICS) {
                Slog.i("ActivityManager", path + " not found");
            }
            return null;
        }
        try {
            return FileUtils.readTextFile(file, 0, null);
        } catch (IOException e) {
            Slog.e("ActivityManager", "Failed to read file:", e);
            return null;
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    static MemoryStat parseMemoryStatFromMemcg(String memoryStatContents) {
        if (memoryStatContents == null || memoryStatContents.isEmpty()) {
            return null;
        }
        MemoryStat memoryStat = new MemoryStat();
        Matcher m = PGFAULT.matcher(memoryStatContents);
        long j = 0;
        memoryStat.pgfault = m.find() ? Long.valueOf(m.group(1)).longValue() : 0;
        Matcher m2 = PGMAJFAULT.matcher(memoryStatContents);
        memoryStat.pgmajfault = m2.find() ? Long.valueOf(m2.group(1)).longValue() : 0;
        Matcher m3 = RSS_IN_BYTES.matcher(memoryStatContents);
        memoryStat.rssInBytes = m3.find() ? Long.valueOf(m3.group(1)).longValue() : 0;
        Matcher m4 = CACHE_IN_BYTES.matcher(memoryStatContents);
        memoryStat.cacheInBytes = m4.find() ? Long.valueOf(m4.group(1)).longValue() : 0;
        Matcher m5 = SWAP_IN_BYTES.matcher(memoryStatContents);
        if (m5.find()) {
            j = Long.valueOf(m5.group(1)).longValue();
        }
        memoryStat.swapInBytes = j;
        return memoryStat;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    static MemoryStat parseMemoryStatFromProcfs(String procStatContents) {
        if (procStatContents == null || procStatContents.isEmpty()) {
            return null;
        }
        String[] splits = procStatContents.split(" ");
        if (splits.length < 24) {
            return null;
        }
        MemoryStat memoryStat = new MemoryStat();
        memoryStat.pgfault = Long.valueOf(splits[9]).longValue();
        memoryStat.pgmajfault = Long.valueOf(splits[11]).longValue();
        memoryStat.rssInBytes = Long.valueOf(splits[23]).longValue();
        return memoryStat;
    }

    static boolean hasMemcg() {
        if (sDeviceHasMemCg == null) {
            sDeviceHasMemCg = Boolean.valueOf(new File(MEMCG_TEST_PATH).exists());
        }
        return sDeviceHasMemCg.booleanValue();
    }
}
