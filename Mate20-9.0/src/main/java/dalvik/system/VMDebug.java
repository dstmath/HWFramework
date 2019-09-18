package dalvik.system;

import android.icu.impl.UCharacterProperty;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class VMDebug {
    private static final int KIND_ALLOCATED_BYTES = 2;
    private static final int KIND_ALLOCATED_OBJECTS = 1;
    public static final int KIND_ALL_COUNTS = -1;
    private static final int KIND_CLASS_INIT_COUNT = 32;
    private static final int KIND_CLASS_INIT_TIME = 64;
    private static final int KIND_EXT_ALLOCATED_BYTES = 8192;
    private static final int KIND_EXT_ALLOCATED_OBJECTS = 4096;
    private static final int KIND_EXT_FREED_BYTES = 32768;
    private static final int KIND_EXT_FREED_OBJECTS = 16384;
    private static final int KIND_FREED_BYTES = 8;
    private static final int KIND_FREED_OBJECTS = 4;
    private static final int KIND_GC_INVOCATIONS = 16;
    public static final int KIND_GLOBAL_ALLOCATED_BYTES = 2;
    public static final int KIND_GLOBAL_ALLOCATED_OBJECTS = 1;
    public static final int KIND_GLOBAL_CLASS_INIT_COUNT = 32;
    public static final int KIND_GLOBAL_CLASS_INIT_TIME = 64;
    public static final int KIND_GLOBAL_EXT_ALLOCATED_BYTES = 8192;
    public static final int KIND_GLOBAL_EXT_ALLOCATED_OBJECTS = 4096;
    public static final int KIND_GLOBAL_EXT_FREED_BYTES = 32768;
    public static final int KIND_GLOBAL_EXT_FREED_OBJECTS = 16384;
    public static final int KIND_GLOBAL_FREED_BYTES = 8;
    public static final int KIND_GLOBAL_FREED_OBJECTS = 4;
    public static final int KIND_GLOBAL_GC_INVOCATIONS = 16;
    public static final int KIND_THREAD_ALLOCATED_BYTES = 131072;
    public static final int KIND_THREAD_ALLOCATED_OBJECTS = 65536;
    public static final int KIND_THREAD_CLASS_INIT_COUNT = 2097152;
    public static final int KIND_THREAD_CLASS_INIT_TIME = 4194304;
    public static final int KIND_THREAD_EXT_ALLOCATED_BYTES = 536870912;
    public static final int KIND_THREAD_EXT_ALLOCATED_OBJECTS = 268435456;
    public static final int KIND_THREAD_EXT_FREED_BYTES = Integer.MIN_VALUE;
    public static final int KIND_THREAD_EXT_FREED_OBJECTS = 1073741824;
    public static final int KIND_THREAD_FREED_BYTES = 524288;
    public static final int KIND_THREAD_FREED_OBJECTS = 262144;
    public static final int KIND_THREAD_GC_INVOCATIONS = 1048576;
    public static final int TRACE_COUNT_ALLOCS = 1;
    private static final HashMap<String, Integer> runtimeStatsMap = new HashMap<>();

    public static native void allowHiddenApiReflectionFrom(Class<?> cls);

    public static native boolean cacheRegisterMap(String str);

    public static native long countInstancesOfClass(Class cls, boolean z);

    public static native long[] countInstancesOfClasses(Class[] clsArr, boolean z);

    public static native void crash();

    private static native void dumpHprofData(String str, int i) throws IOException;

    public static native void dumpHprofDataDdms();

    public static native void dumpReferenceTables();

    public static native int getAllocCount(int i);

    public static native void getHeapSpaceStats(long[] jArr);

    public static native Object[][] getInstancesOfClasses(Class[] clsArr, boolean z);

    public static native void getInstructionCount(int[] iArr);

    public static native int getLoadedClassCount();

    public static native int getMethodTracingMode();

    private static native String getRuntimeStatInternal(int i);

    private static native String[] getRuntimeStatsInternal();

    public static native String[] getVmFeatureList();

    public static native void infopoint(int i);

    public static native boolean isDebuggerConnected();

    public static native boolean isDebuggingEnabled();

    public static native long lastDebuggerActivity();

    private static native void nativeAttachAgent(String str, ClassLoader classLoader) throws IOException;

    public static native void printLoadedClasses(int i);

    public static native void resetAllocCount(int i);

    public static native void resetInstructionCount();

    public static native void startAllocCounting();

    public static native void startEmulatorTracing();

    public static native void startInstructionCounting();

    private static native void startMethodTracingDdmsImpl(int i, int i2, boolean z, int i3);

    private static native void startMethodTracingFd(String str, int i, int i2, int i3, boolean z, int i4, boolean z2);

    private static native void startMethodTracingFilename(String str, int i, int i2, boolean z, int i3);

    public static native void stopAllocCounting();

    public static native void stopEmulatorTracing();

    public static native void stopInstructionCounting();

    public static native void stopMethodTracing();

    public static native long threadCpuTimeNanos();

    private VMDebug() {
    }

    @Deprecated
    public static void startMethodTracing() {
        throw new UnsupportedOperationException();
    }

    public static void startMethodTracing(String traceFileName, int bufferSize, int flags, boolean samplingEnabled, int intervalUs) {
        startMethodTracingFilename(traceFileName, checkBufferSize(bufferSize), flags, samplingEnabled, intervalUs);
    }

    public static void startMethodTracing(String traceFileName, FileDescriptor fd, int bufferSize, int flags, boolean samplingEnabled, int intervalUs) {
        startMethodTracing(traceFileName, fd, bufferSize, flags, samplingEnabled, intervalUs, false);
    }

    public static void startMethodTracing(String traceFileName, FileDescriptor fd, int bufferSize, int flags, boolean samplingEnabled, int intervalUs, boolean streamingOutput) {
        if (fd != null) {
            startMethodTracingFd(traceFileName, fd.getInt$(), checkBufferSize(bufferSize), flags, samplingEnabled, intervalUs, streamingOutput);
            return;
        }
        throw new NullPointerException("fd == null");
    }

    public static void startMethodTracingDdms(int bufferSize, int flags, boolean samplingEnabled, int intervalUs) {
        startMethodTracingDdmsImpl(checkBufferSize(bufferSize), flags, samplingEnabled, intervalUs);
    }

    private static int checkBufferSize(int bufferSize) {
        if (bufferSize == 0) {
            bufferSize = UCharacterProperty.SCRIPT_X_WITH_INHERITED;
        }
        if (bufferSize >= 1024) {
            return bufferSize;
        }
        throw new IllegalArgumentException("buffer size < 1024: " + bufferSize);
    }

    @Deprecated
    public static int setAllocationLimit(int limit) {
        return -1;
    }

    @Deprecated
    public static int setGlobalAllocationLimit(int limit) {
        return -1;
    }

    public static void dumpHprofData(String filename) throws IOException {
        if (filename != null) {
            dumpHprofData(filename, (FileDescriptor) null);
            return;
        }
        throw new NullPointerException("filename == null");
    }

    public static void dumpHprofData(String fileName, FileDescriptor fd) throws IOException {
        dumpHprofData(fileName, fd != null ? fd.getInt$() : -1);
    }

    private static void startGC() {
    }

    private static void startClassPrep() {
    }

    static {
        runtimeStatsMap.put("art.gc.gc-count", 0);
        runtimeStatsMap.put("art.gc.gc-time", 1);
        runtimeStatsMap.put("art.gc.bytes-allocated", 2);
        runtimeStatsMap.put("art.gc.bytes-freed", 3);
        runtimeStatsMap.put("art.gc.blocking-gc-count", 4);
        runtimeStatsMap.put("art.gc.blocking-gc-time", 5);
        runtimeStatsMap.put("art.gc.gc-count-rate-histogram", 6);
        runtimeStatsMap.put("art.gc.blocking-gc-count-rate-histogram", 7);
        if (System.getenv("MAPLE_RUNTIME") != null) {
            runtimeStatsMap.put("mpl.mem.gc-count", 8);
            runtimeStatsMap.put("mpl.mem.gc-max-time", 9);
            runtimeStatsMap.put("mpl.mem.leak-avg", 10);
            runtimeStatsMap.put("mpl.mem.leak-peak", 11);
            runtimeStatsMap.put("mpl.mem.allocation-utilization", 12);
            runtimeStatsMap.put("mpl.mem.allocation-abnormal-count", 13);
            runtimeStatsMap.put("mpl.mem.rc-abnormal-count", 14);
            runtimeStatsMap.put("mpl.ref.global-water-line", 15);
            runtimeStatsMap.put("mpl.ref.weak-water-line", 16);
            runtimeStatsMap.put("mpl.ref.threads-local-water-line", 17);
            runtimeStatsMap.put("mpl.ref.native-table-size", 18);
            runtimeStatsMap.put("mpl.mem.consum-mpl-files", 19);
            runtimeStatsMap.put("mpl.mem.consum-class-locator", 20);
            runtimeStatsMap.put("mpl.mem.reflect-manage-heap", 21);
            runtimeStatsMap.put("mpl.mem.gc-manage-heap", 22);
            runtimeStatsMap.put("mpl.mem.cycle-pattern", 23);
        }
    }

    public static String getRuntimeStat(String statName) {
        if (statName != null) {
            Integer statId = runtimeStatsMap.get(statName);
            if (statId != null) {
                return getRuntimeStatInternal(statId.intValue());
            }
            return null;
        }
        throw new NullPointerException("statName == null");
    }

    public static Map<String, String> getRuntimeStats() {
        HashMap<String, String> map = new HashMap<>();
        String[] values = getRuntimeStatsInternal();
        for (String name : runtimeStatsMap.keySet()) {
            map.put(name, values[runtimeStatsMap.get(name).intValue()]);
        }
        return map;
    }

    public static void attachAgent(String agent) throws IOException {
        attachAgent(agent, null);
    }

    public static void attachAgent(String agent, ClassLoader classLoader) throws IOException {
        nativeAttachAgent(agent, classLoader);
    }
}
