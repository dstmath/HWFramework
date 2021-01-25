package com.android.server.am;

import android.os.FileUtils;
import android.os.SystemProperties;
import android.system.Os;
import android.system.OsConstants;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wm.ActivityTaskManagerDebugConfig;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MemoryStatUtil {
    static final int BYTES_IN_KILOBYTE = 1024;
    private static final Pattern CACHE_IN_BYTES = Pattern.compile("total_cache (\\d+)");
    private static final String DEBUG_SYSTEM_ION_HEAP_FILE = "/sys/kernel/debug/ion/heaps/system";
    private static final boolean DEVICE_HAS_PER_APP_MEMCG = SystemProperties.getBoolean("ro.config.per_app_memcg", false);
    private static final Pattern ION_HEAP_SIZE_IN_BYTES = Pattern.compile("\n\\s*total\\s*(\\d+)\\s*\n");
    static final long JIFFY_NANOS = (1000000000 / Os.sysconf(OsConstants._SC_CLK_TCK));
    private static final String MEMORY_STAT_FILE_FMT = "/dev/memcg/apps/uid_%d/pid_%d/memory.stat";
    private static final Pattern PGFAULT = Pattern.compile("total_pgfault (\\d+)");
    private static final int PGFAULT_INDEX = 9;
    private static final Pattern PGMAJFAULT = Pattern.compile("total_pgmajfault (\\d+)");
    private static final int PGMAJFAULT_INDEX = 11;
    private static final Pattern PROCESS_ION_HEAP_SIZE_IN_BYTES = Pattern.compile("\n\\s+\\S+\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern PROCFS_ANON_RSS_IN_KILOBYTES = Pattern.compile("RssAnon:\\s*(\\d+)\\s*kB");
    private static final Pattern PROCFS_RSS_IN_KILOBYTES = Pattern.compile("VmRSS:\\s*(\\d+)\\s*kB");
    private static final Pattern PROCFS_SWAP_IN_KILOBYTES = Pattern.compile("VmSwap:\\s*(\\d+)\\s*kB");
    private static final String PROC_CMDLINE_FILE_FMT = "/proc/%d/cmdline";
    private static final String PROC_STATUS_FILE_FMT = "/proc/%d/status";
    private static final String PROC_STAT_FILE_FMT = "/proc/%d/stat";
    private static final Pattern RSS_HIGH_WATERMARK_IN_KILOBYTES = Pattern.compile("VmHWM:\\s*(\\d+)\\s*kB");
    private static final Pattern RSS_IN_BYTES = Pattern.compile("total_rss (\\d+)");
    private static final int START_TIME_INDEX = 21;
    private static final Pattern SWAP_IN_BYTES = Pattern.compile("total_swap (\\d+)");
    private static final String TAG = "ActivityManager";

    public static final class MemoryStat {
        public long anonRssInBytes;
        public long cacheInBytes;
        public long pgfault;
        public long pgmajfault;
        public long rssInBytes;
        public long startTimeNanos;
        public long swapInBytes;
    }

    private MemoryStatUtil() {
    }

    public static MemoryStat readMemoryStatFromFilesystem(int uid, int pid) {
        return hasMemcg() ? readMemoryStatFromMemcg(uid, pid) : readMemoryStatFromProcfs(pid);
    }

    static MemoryStat readMemoryStatFromMemcg(int uid, int pid) {
        return parseMemoryStatFromMemcg(readFileContents(String.format(Locale.US, MEMORY_STAT_FILE_FMT, Integer.valueOf(uid), Integer.valueOf(pid))));
    }

    public static MemoryStat readMemoryStatFromProcfs(int pid) {
        return parseMemoryStatFromProcfs(readFileContents(String.format(Locale.US, PROC_STAT_FILE_FMT, Integer.valueOf(pid))), readFileContents(String.format(Locale.US, PROC_STATUS_FILE_FMT, Integer.valueOf(pid))));
    }

    public static long readRssHighWaterMarkFromProcfs(int pid) {
        return parseVmHWMFromProcfs(readFileContents(String.format(Locale.US, PROC_STATUS_FILE_FMT, Integer.valueOf(pid))));
    }

    public static String readCmdlineFromProcfs(int pid) {
        return parseCmdlineFromProcfs(readFileContents(String.format(Locale.US, PROC_CMDLINE_FILE_FMT, Integer.valueOf(pid))));
    }

    public static long readSystemIonHeapSizeFromDebugfs() {
        return parseIonHeapSizeFromDebugfs(readFileContents(DEBUG_SYSTEM_ION_HEAP_FILE));
    }

    public static List<IonAllocations> readProcessSystemIonHeapSizesFromDebugfs() {
        return parseProcessIonHeapSizesFromDebugfs(readFileContents(DEBUG_SYSTEM_ION_HEAP_FILE));
    }

    private static String readFileContents(String path) {
        File file = new File(path);
        if (!file.exists()) {
            if (ActivityTaskManagerDebugConfig.DEBUG_METRICS) {
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

    @VisibleForTesting
    static MemoryStat parseMemoryStatFromMemcg(String memoryStatContents) {
        if (memoryStatContents == null || memoryStatContents.isEmpty()) {
            return null;
        }
        MemoryStat memoryStat = new MemoryStat();
        memoryStat.pgfault = tryParseLong(PGFAULT, memoryStatContents);
        memoryStat.pgmajfault = tryParseLong(PGMAJFAULT, memoryStatContents);
        memoryStat.rssInBytes = tryParseLong(RSS_IN_BYTES, memoryStatContents);
        memoryStat.cacheInBytes = tryParseLong(CACHE_IN_BYTES, memoryStatContents);
        memoryStat.swapInBytes = tryParseLong(SWAP_IN_BYTES, memoryStatContents);
        return memoryStat;
    }

    @VisibleForTesting
    static MemoryStat parseMemoryStatFromProcfs(String procStatContents, String procStatusContents) {
        if (procStatContents == null || procStatContents.isEmpty() || procStatusContents == null || procStatusContents.isEmpty()) {
            return null;
        }
        String[] splits = procStatContents.split(" ");
        if (splits.length < 24) {
            return null;
        }
        try {
            MemoryStat memoryStat = new MemoryStat();
            memoryStat.pgfault = Long.parseLong(splits[9]);
            memoryStat.pgmajfault = Long.parseLong(splits[11]);
            memoryStat.rssInBytes = tryParseLong(PROCFS_RSS_IN_KILOBYTES, procStatusContents) * 1024;
            memoryStat.anonRssInBytes = tryParseLong(PROCFS_ANON_RSS_IN_KILOBYTES, procStatusContents) * 1024;
            memoryStat.swapInBytes = tryParseLong(PROCFS_SWAP_IN_KILOBYTES, procStatusContents) * 1024;
            memoryStat.startTimeNanos = Long.parseLong(splits[21]) * JIFFY_NANOS;
            return memoryStat;
        } catch (NumberFormatException e) {
            Slog.e("ActivityManager", "Failed to parse value", e);
            return null;
        }
    }

    @VisibleForTesting
    static long parseVmHWMFromProcfs(String procStatusContents) {
        if (procStatusContents == null || procStatusContents.isEmpty()) {
            return 0;
        }
        return tryParseLong(RSS_HIGH_WATERMARK_IN_KILOBYTES, procStatusContents) * 1024;
    }

    @VisibleForTesting
    static String parseCmdlineFromProcfs(String cmdline) {
        if (cmdline == null) {
            return "";
        }
        int firstNullByte = cmdline.indexOf("\u0000");
        if (firstNullByte == -1) {
            return cmdline;
        }
        return cmdline.substring(0, firstNullByte);
    }

    @VisibleForTesting
    static long parseIonHeapSizeFromDebugfs(String contents) {
        if (contents == null || contents.isEmpty()) {
            return 0;
        }
        return tryParseLong(ION_HEAP_SIZE_IN_BYTES, contents);
    }

    @VisibleForTesting
    static List<IonAllocations> parseProcessIonHeapSizesFromDebugfs(String contents) {
        if (contents == null || contents.isEmpty()) {
            return Collections.emptyList();
        }
        Matcher m = PROCESS_ION_HEAP_SIZE_IN_BYTES.matcher(contents);
        SparseArray<IonAllocations> entries = new SparseArray<>();
        while (m.find()) {
            try {
                int pid = Integer.parseInt(m.group(1));
                long sizeInBytes = Long.parseLong(m.group(2));
                IonAllocations allocations = entries.get(pid);
                if (allocations == null) {
                    allocations = new IonAllocations();
                    entries.put(pid, allocations);
                }
                allocations.pid = pid;
                allocations.totalSizeInBytes += sizeInBytes;
                allocations.count++;
                allocations.maxSizeInBytes = Math.max(allocations.maxSizeInBytes, sizeInBytes);
            } catch (NumberFormatException e) {
                Slog.e("ActivityManager", "Failed to parse value", e);
            }
        }
        List<IonAllocations> result = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            result.add(entries.valueAt(i));
        }
        return result;
    }

    static boolean hasMemcg() {
        return DEVICE_HAS_PER_APP_MEMCG;
    }

    private static long tryParseLong(Pattern pattern, String input) {
        Matcher m = pattern.matcher(input);
        try {
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
            return 0;
        } catch (NumberFormatException e) {
            Slog.e("ActivityManager", "Failed to parse value", e);
            return 0;
        }
    }

    public static final class IonAllocations {
        public int count;
        public long maxSizeInBytes;
        public int pid;
        public long totalSizeInBytes;

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            IonAllocations that = (IonAllocations) o;
            if (this.pid == that.pid && this.totalSizeInBytes == that.totalSizeInBytes && this.count == that.count && this.maxSizeInBytes == that.maxSizeInBytes) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.pid), Long.valueOf(this.totalSizeInBytes), Integer.valueOf(this.count), Long.valueOf(this.maxSizeInBytes));
        }

        public String toString() {
            return "IonAllocations{pid=" + this.pid + ", totalSizeInBytes=" + this.totalSizeInBytes + ", count=" + this.count + ", maxSizeInBytes=" + this.maxSizeInBytes + '}';
        }
    }
}
