package dalvik.system;

import android.icu.impl.UCharacterProperty;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.traversal.NodeFilter;

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
    private static final HashMap<String, Integer> runtimeStatsMap = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: dalvik.system.VMDebug.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: dalvik.system.VMDebug.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.VMDebug.<clinit>():void");
    }

    public static native boolean cacheRegisterMap(String str);

    public static native long countInstancesOfClass(Class cls, boolean z);

    public static native long[] countInstancesOfClasses(Class[] clsArr, boolean z);

    public static native void crash();

    public static native void dumpHprofData(String str, FileDescriptor fileDescriptor) throws IOException;

    public static native void dumpHprofDataDdms();

    public static native void dumpReferenceTables();

    public static native int getAllocCount(int i);

    public static native void getHeapSpaceStats(long[] jArr);

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

    public static native void printLoadedClasses(int i);

    public static native void resetAllocCount(int i);

    public static native void resetInstructionCount();

    public static native void startAllocCounting();

    public static native void startEmulatorTracing();

    public static native void startInstructionCounting();

    private static native void startMethodTracingDdmsImpl(int i, int i2, boolean z, int i3);

    private static native void startMethodTracingFd(String str, FileDescriptor fileDescriptor, int i, int i2, boolean z, int i3);

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
        if (fd == null) {
            throw new NullPointerException("fd == null");
        }
        startMethodTracingFd(traceFileName, fd, checkBufferSize(bufferSize), flags, samplingEnabled, intervalUs);
    }

    public static void startMethodTracingDdms(int bufferSize, int flags, boolean samplingEnabled, int intervalUs) {
        startMethodTracingDdmsImpl(checkBufferSize(bufferSize), flags, samplingEnabled, intervalUs);
    }

    private static int checkBufferSize(int bufferSize) {
        if (bufferSize == 0) {
            bufferSize = UCharacterProperty.SCRIPT_X_WITH_INHERITED;
        }
        if (bufferSize >= NodeFilter.SHOW_DOCUMENT_FRAGMENT) {
            return bufferSize;
        }
        throw new IllegalArgumentException("buffer size < 1024: " + bufferSize);
    }

    @Deprecated
    public static int setAllocationLimit(int limit) {
        return KIND_ALL_COUNTS;
    }

    @Deprecated
    public static int setGlobalAllocationLimit(int limit) {
        return KIND_ALL_COUNTS;
    }

    public static void dumpHprofData(String filename) throws IOException {
        if (filename == null) {
            throw new NullPointerException("filename == null");
        }
        dumpHprofData(filename, null);
    }

    private static void startGC() {
    }

    private static void startClassPrep() {
    }

    public static String getRuntimeStat(String statName) {
        if (statName == null) {
            throw new NullPointerException("statName == null");
        }
        Integer statId = (Integer) runtimeStatsMap.get(statName);
        if (statId != null) {
            return getRuntimeStatInternal(statId.intValue());
        }
        return null;
    }

    public static Map<String, String> getRuntimeStats() {
        HashMap<String, String> map = new HashMap();
        String[] values = getRuntimeStatsInternal();
        for (String name : runtimeStatsMap.keySet()) {
            map.put(name, values[((Integer) runtimeStatsMap.get(name)).intValue()]);
        }
        return map;
    }
}
