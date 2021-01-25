package ohos.hiviewdfx;

import android.app.AppGlobals;
import android.app.Application;
import android.os.Debug;
import android.os.Environment;
import ark.system.Debugger;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import ohos.hiviewdfx.Debug;

public class DebugImpl {
    private static final HiLogLabel DEBUG_LABEL = new HiLogLabel(3, 218115338, "DEBUG_JAVA");
    private static final String DEFAULT_TRACE_BODY = "dmtrace";
    private static final String DEFAULT_TRACE_EXTENSION = ".trace";

    public static native long getNativeHeapAllocatedSize();

    public static native long getNativeHeapFreeSize();

    public static native long getNativeHeapSize();

    public static native long threadCpuTimeNanos();

    static {
        System.loadLibrary("debug_jni.z");
    }

    private DebugImpl() {
    }

    public static int getProcessPssSum() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getTotalPss();
    }

    public static int getProcessPrivateDirtySum() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getTotalPrivateDirty();
    }

    public static int getProcessPss(Debug.MemType memType) {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        int i = AnonymousClass1.$SwitchMap$ohos$hiviewdfx$Debug$MemType[memType.ordinal()];
        if (i == 1) {
            return memoryInfo.dalvikPss;
        }
        if (i == 2) {
            return memoryInfo.nativePss;
        }
        if (i != 3) {
            return 0;
        }
        return memoryInfo.otherPss;
    }

    public static int getProcessSwappablePss(Debug.MemType memType) {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        int i = AnonymousClass1.$SwitchMap$ohos$hiviewdfx$Debug$MemType[memType.ordinal()];
        if (i == 1) {
            return memoryInfo.dalvikSwappablePss;
        }
        if (i == 2) {
            return memoryInfo.nativeSwappablePss;
        }
        if (i != 3) {
            return 0;
        }
        return memoryInfo.otherSwappablePss;
    }

    public static int getProcessSwappablePssSum() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getTotalSwappablePss();
    }

    public static int getProcessRss(Debug.MemType memType) {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        int i = AnonymousClass1.$SwitchMap$ohos$hiviewdfx$Debug$MemType[memType.ordinal()];
        if (i == 1) {
            return memoryInfo.dalvikRss;
        }
        if (i == 2) {
            return memoryInfo.nativeRss;
        }
        if (i != 3) {
            return 0;
        }
        return memoryInfo.otherRss;
    }

    public static int getProcessRssSum() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getTotalRss();
    }

    public static int getProcessPrivateDirty(Debug.MemType memType) {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        int i = AnonymousClass1.$SwitchMap$ohos$hiviewdfx$Debug$MemType[memType.ordinal()];
        if (i == 1) {
            return memoryInfo.dalvikPrivateDirty;
        }
        if (i == 2) {
            return memoryInfo.nativePrivateDirty;
        }
        if (i != 3) {
            return 0;
        }
        return memoryInfo.otherPrivateDirty;
    }

    public static int getProcessSharedDirty(Debug.MemType memType) {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        int i = AnonymousClass1.$SwitchMap$ohos$hiviewdfx$Debug$MemType[memType.ordinal()];
        if (i == 1) {
            return memoryInfo.dalvikSharedDirty;
        }
        if (i == 2) {
            return memoryInfo.nativeSharedDirty;
        }
        if (i != 3) {
            return 0;
        }
        return memoryInfo.otherSharedDirty;
    }

    public static int getProcessSharedDirtySum() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getTotalSharedDirty();
    }

    public static int getProcessPrivateClean(Debug.MemType memType) {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        int i = AnonymousClass1.$SwitchMap$ohos$hiviewdfx$Debug$MemType[memType.ordinal()];
        if (i == 1) {
            return memoryInfo.dalvikPrivateClean;
        }
        if (i == 2) {
            return memoryInfo.nativePrivateClean;
        }
        if (i != 3) {
            return 0;
        }
        return memoryInfo.otherPrivateClean;
    }

    public static int getProcessPrivateCleanSum() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getTotalPrivateClean();
    }

    public static int getProcessSharedClean(Debug.MemType memType) {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        int i = AnonymousClass1.$SwitchMap$ohos$hiviewdfx$Debug$MemType[memType.ordinal()];
        if (i == 1) {
            return memoryInfo.dalvikSharedClean;
        }
        if (i == 2) {
            return memoryInfo.nativeSharedClean;
        }
        if (i != 3) {
            return 0;
        }
        return memoryInfo.otherSharedClean;
    }

    public static int getProcessSharedCleanSum() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getTotalSharedClean();
    }

    public static int getProcessSwappedOut(Debug.MemType memType) {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        int i = AnonymousClass1.$SwitchMap$ohos$hiviewdfx$Debug$MemType[memType.ordinal()];
        if (i == 1) {
            return memoryInfo.dalvikSwappedOut;
        }
        if (i == 2) {
            return memoryInfo.nativeSwappedOut;
        }
        if (i != 3) {
            return 0;
        }
        return memoryInfo.otherSwappedOut;
    }

    public static int getProcessSwappedOutSum() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getTotalSwappedOut();
    }

    public static int getProcessSwappedOutPss(Debug.MemType memType) {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        int i = AnonymousClass1.$SwitchMap$ohos$hiviewdfx$Debug$MemType[memType.ordinal()];
        if (i == 1) {
            return memoryInfo.dalvikSwappedOutPss;
        }
        if (i == 2) {
            return memoryInfo.nativeSwappedOutPss;
        }
        if (i != 3) {
            return 0;
        }
        return memoryInfo.otherSwappedOutPss;
    }

    public static int getProcessSwappedOutPssSum() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getTotalSwappedOutPss();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.hiviewdfx.DebugImpl$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$hiviewdfx$Debug$IpcType = new int[Debug.IpcType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$hiviewdfx$Debug$MemType = new int[Debug.MemType.values().length];

        static {
            try {
                $SwitchMap$ohos$hiviewdfx$Debug$IpcType[Debug.IpcType.LOCAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$hiviewdfx$Debug$IpcType[Debug.IpcType.REMOTE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$hiviewdfx$Debug$IpcType[Debug.IpcType.DESTRUCTION.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$hiviewdfx$Debug$MemType[Debug.MemType.RUNTIME.ordinal()] = 1;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$hiviewdfx$Debug$MemType[Debug.MemType.HEAP.ordinal()] = 2;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$hiviewdfx$Debug$MemType[Debug.MemType.OTHER.ordinal()] = 3;
            } catch (NoSuchFieldError unused6) {
            }
        }
    }

    public static int getIpcCount(Debug.IpcType ipcType) {
        int i = AnonymousClass1.$SwitchMap$ohos$hiviewdfx$Debug$IpcType[ipcType.ordinal()];
        if (i == 1) {
            return android.os.Debug.getBinderLocalObjectCount();
        }
        if (i == 2) {
            return android.os.Debug.getBinderProxyObjectCount();
        }
        if (i != 3) {
            return 0;
        }
        return android.os.Debug.getBinderDeathObjectCount();
    }

    public static int getIpcSentCount() {
        return android.os.Debug.getBinderSentTransactions();
    }

    public static int getIpcReceivedCount() {
        return android.os.Debug.getBinderReceivedTransactions();
    }

    public static String getMemoryStatistic(String str) {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getMemoryStat(str);
    }

    public static Map<String, String> getMemoryStatistics() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        android.os.Debug.getMemoryInfo(memoryInfo);
        return memoryInfo.getMemoryStats();
    }

    public static void functionTraceBegin() {
        Debugger.startMethodTracing(fixTracePath(null), 0, 0, false, 0);
    }

    public static void functionTraceBegin(String str) {
        functionTraceBegin(str, 0, 0);
    }

    public static void functionTraceBegin(String str, int i) {
        functionTraceBegin(str, i, 0);
    }

    public static void functionTraceBegin(String str, int i, int i2) {
        Debugger.startMethodTracing(fixTracePath(str), i, i2, false, 0);
    }

    private static String fixTracePath(String str) {
        File file;
        if (str == null || str.length() == 0 || str.charAt(0) != '/') {
            Application initialApplication = AppGlobals.getInitialApplication();
            if (initialApplication != null) {
                file = initialApplication.getExternalFilesDir(null);
            } else {
                file = Environment.getDataDirectory();
            }
            if (str != null) {
                try {
                    if (str.length() != 0) {
                        str = new File(file, str).getCanonicalPath();
                    }
                } catch (IOException unused) {
                    HiLog.error(DEBUG_LABEL, "fixTracePath error by IOexception", new Object[0]);
                    return str;
                }
            }
            str = new File(file, DEFAULT_TRACE_BODY).getCanonicalPath();
        }
        if (str.endsWith(DEFAULT_TRACE_EXTENSION)) {
            return str;
        }
        return str + DEFAULT_TRACE_EXTENSION;
    }

    public static void functionTraceSamplingBegin(String str, int i, int i2) {
        Debugger.startMethodTracing(fixTracePath(str), i, 0, true, i2);
    }

    public static void functionTraceEnd() {
        android.os.Debug.stopMethodTracing();
    }

    public static void dumpHeapFile(String str) throws IOException {
        Debugger.dumpHprofData(str, (FileDescriptor) null);
    }

    public static String getRuntimeStatistic(String str) {
        return Debugger.getRuntimeStat("art." + str);
    }

    public static Map<String, String> getRuntimeStatistics() {
        Map<String, String> runtimeStats = android.os.Debug.getRuntimeStats();
        HashMap hashMap = new HashMap();
        for (Map.Entry<String, String> entry : runtimeStats.entrySet()) {
            hashMap.put(entry.getKey().substring(4), entry.getValue());
        }
        return hashMap;
    }

    public static void conectToDebugger() {
        android.os.Debug.waitForDebugger();
    }

    public static boolean getDebuggerConnectStatus() {
        return android.os.Debug.isDebuggerConnected();
    }

    public static boolean isConectingToDebugger() {
        return android.os.Debug.waitingForDebugger();
    }

    public static int getCountofLoadClasses() {
        return Debugger.getLoadedClassCount();
    }

    public static void dumpLoadClasses(int i) {
        Debugger.printLoadedClasses(i);
    }

    public static void emulatorTraceBegin() {
        android.os.Debug.startNativeTracing();
    }

    public static void emulatorTraceEnd() {
        android.os.Debug.stopNativeTracing();
    }

    public static void emulatorTraceEnable() {
        Debugger.startEmulatorTracing();
    }

    public static void attachAgent(String str, String str2, ClassLoader classLoader) throws IOException {
        Preconditions.checkNotNull(str);
        Preconditions.checkArgument(!str.contains("="));
        if (str2 == null) {
            Debugger.attachAgent(str, classLoader);
            return;
        }
        Debugger.attachAgent(str + "=" + str2, classLoader);
    }
}
