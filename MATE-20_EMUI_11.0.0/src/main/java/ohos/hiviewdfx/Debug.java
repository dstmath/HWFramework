package ohos.hiviewdfx;

import java.io.IOException;
import java.util.Map;

public final class Debug {
    private static final HiLogLabel DEBUG_LABEL = new HiLogLabel(3, 218115338, "DEBUG_JAVA");
    public static final int SHOW_CLASSLOADER = 2;
    public static final int SHOW_FULL_DETAIL = 1;
    public static final int SHOW_INITIALIZED = 4;
    public static final int TRACE_COUNT_ALLOCS = 1;

    public static class HeapInfo {
        public long nativeHeapAllocatedSize;
        public long nativeHeapFreeSize;
        public long nativeHeapSize;
    }

    public enum IpcType {
        LOCAL,
        REMOTE,
        DESTRUCTION
    }

    public enum MemType {
        RUNTIME,
        HEAP,
        OTHER
    }

    public static int getProcessPss(MemType memType) {
        return DebugImpl.getProcessPss(memType);
    }

    public static int getProcessPrivateDirtySum() {
        return DebugImpl.getProcessPrivateDirtySum();
    }

    public static int getProcessPssSum() {
        return DebugImpl.getProcessPssSum();
    }

    public static void getNativeHeapInfo(HeapInfo heapInfo) {
        heapInfo.nativeHeapSize = DebugImpl.getNativeHeapSize();
        heapInfo.nativeHeapAllocatedSize = DebugImpl.getNativeHeapAllocatedSize();
        heapInfo.nativeHeapFreeSize = DebugImpl.getNativeHeapFreeSize();
        HiLog.debug(DEBUG_LABEL, "Heap size=%d, Heap allocated size=%d, Heap free size=%d", Long.valueOf(heapInfo.nativeHeapSize), Long.valueOf(heapInfo.nativeHeapAllocatedSize), Long.valueOf(heapInfo.nativeHeapFreeSize));
    }

    public static long getCpuTime() {
        return DebugImpl.threadCpuTimeNanos();
    }

    public static int getProcessSwappablePss(MemType memType) {
        return DebugImpl.getProcessSwappablePss(memType);
    }

    public static int getProcessSwappablePssSum() {
        return DebugImpl.getProcessSwappablePssSum();
    }

    public static int getProcessRss(MemType memType) {
        return DebugImpl.getProcessRss(memType);
    }

    public static int getProcessRssSum() {
        return DebugImpl.getProcessRssSum();
    }

    public static int getProcessPrivateDirty(MemType memType) {
        return DebugImpl.getProcessPrivateDirty(memType);
    }

    public static int getProcessSharedDirty(MemType memType) {
        return DebugImpl.getProcessSharedDirty(memType);
    }

    public static int getProcessSharedDirtySum() {
        return DebugImpl.getProcessSharedDirtySum();
    }

    public static int getProcessPrivateClean(MemType memType) {
        return DebugImpl.getProcessPrivateClean(memType);
    }

    public static int getProcessPrivateCleanSum() {
        return DebugImpl.getProcessPrivateCleanSum();
    }

    public static int getProcessSharedClean(MemType memType) {
        return DebugImpl.getProcessSharedClean(memType);
    }

    public static int getProcessSharedCleanSum() {
        return DebugImpl.getProcessSharedCleanSum();
    }

    public static int getProcessSwappedOut(MemType memType) {
        return DebugImpl.getProcessSwappedOut(memType);
    }

    public static int getProcessSwappedOutSum() {
        return DebugImpl.getProcessSwappedOutSum();
    }

    public static int getProcessSwappedOutPss(MemType memType) {
        return DebugImpl.getProcessSwappedOutPss(memType);
    }

    public static int getProcessSwappedOutPssSum() {
        return DebugImpl.getProcessSwappedOutPssSum();
    }

    public static int getIpcCount(IpcType ipcType) {
        return DebugImpl.getIpcCount(ipcType);
    }

    public static int getIpcSentCount() {
        return DebugImpl.getIpcSentCount();
    }

    public static int getIpcReceivedCount() {
        return DebugImpl.getIpcReceivedCount();
    }

    public static String getMemoryStatistic(String str) {
        return DebugImpl.getMemoryStatistic(str);
    }

    public static Map<String, String> getMemoryStatistics() {
        return DebugImpl.getMemoryStatistics();
    }

    public static void functionTraceBegin() {
        DebugImpl.functionTraceBegin();
    }

    public static void functionTraceBegin(String str) {
        DebugImpl.functionTraceBegin(str);
    }

    public static void functionTraceBegin(String str, int i) {
        DebugImpl.functionTraceBegin(str, i);
    }

    public static void functionTraceBegin(String str, int i, int i2) {
        DebugImpl.functionTraceBegin(str, i, i2);
    }

    public static void functionTraceSamplingBegin(String str, int i, int i2) {
        DebugImpl.functionTraceSamplingBegin(str, i, i2);
    }

    public static void functionTraceEnd() {
        DebugImpl.functionTraceEnd();
    }

    public static void dumpHeapFile(String str) throws IOException {
        DebugImpl.dumpHeapFile(str);
    }

    public static String getRuntimeStatistic(String str) {
        return DebugImpl.getRuntimeStatistic(str);
    }

    public static Map<String, String> getRuntimeStatistics() {
        return DebugImpl.getRuntimeStatistics();
    }

    public static void conectToDebugger() {
        DebugImpl.conectToDebugger();
    }

    public static boolean getDebuggerConnectStatus() {
        return DebugImpl.getDebuggerConnectStatus();
    }

    public static boolean isConectingToDebugger() {
        return DebugImpl.isConectingToDebugger();
    }

    public static int getCountofLoadClasses() {
        return DebugImpl.getCountofLoadClasses();
    }

    public static void dumpLoadClasses(int i) {
        DebugImpl.dumpLoadClasses(i);
    }

    public static void emulatorTraceBegin() {
        DebugImpl.emulatorTraceBegin();
    }

    public static void emulatorTraceEnd() {
        DebugImpl.emulatorTraceEnd();
    }

    public static void emulatorTraceEnable() {
        DebugImpl.emulatorTraceEnable();
    }

    public static void attachAgent(String str, String str2, ClassLoader classLoader) throws IOException {
        HiLog.debug(DEBUG_LABEL, "attachAgent start.", new Object[0]);
        DebugImpl.attachAgent(str, str2, classLoader);
    }
}
