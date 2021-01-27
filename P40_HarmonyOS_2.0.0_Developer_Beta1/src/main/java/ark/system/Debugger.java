package ark.system;

import dalvik.system.VMDebug;
import java.io.FileDescriptor;
import java.io.IOException;

public class Debugger {
    public static final int ALLOC_COUNT_FLAG = 1;

    public static boolean isDebuggingEnabled() {
        return VMDebug.isDebuggingEnabled();
    }

    public static long lastDebuggerActivity() {
        return VMDebug.lastDebuggerActivity();
    }

    public static boolean isDebuggerConnected() {
        return VMDebug.isDebuggerConnected();
    }

    public static String[] getVmFeatureList() {
        return VMDebug.getVmFeatureList();
    }

    public static void startEmulatorTracing() {
        VMDebug.startEmulatorTracing();
    }

    public static void stopEmulatorTracing() {
        VMDebug.stopEmulatorTracing();
    }

    public static void startMethodTracing(String str, int i, int i2, boolean z, int i3) {
        VMDebug.startMethodTracing(str, i, i2, z, i3);
    }

    public static void startMethodTracing(String str, FileDescriptor fileDescriptor, int i, int i2, boolean z, int i3) {
        VMDebug.startMethodTracing(str, fileDescriptor, i, i2, z, i3);
    }

    public static void startMethodTracing(String str, FileDescriptor fileDescriptor, int i, int i2, boolean z, int i3, boolean z2) {
        VMDebug.startMethodTracing(str, fileDescriptor, i, i2, z, i3, z2);
    }

    public static void startMethodTracingDdms(int i, int i2, boolean z, int i3) {
        VMDebug.startMethodTracingDdms(i, i2, z, i3);
    }

    public static int getMethodTracingMode() {
        return VMDebug.getMethodTracingMode();
    }

    public static long threadCpuTimeNanos() {
        return VMDebug.threadCpuTimeNanos();
    }

    public static String getRuntimeStat(String str) {
        return VMDebug.getRuntimeStat(str);
    }

    public static void printLoadedClasses(int i) {
        VMDebug.printLoadedClasses(i);
    }

    public static int getLoadedClassCount() {
        return VMDebug.getLoadedClassCount();
    }

    public static void dumpHprofData(String str, FileDescriptor fileDescriptor) throws IOException {
        VMDebug.dumpHprofData(str, fileDescriptor);
    }

    public static void attachAgent(String str) throws IOException {
        VMDebug.attachAgent(str);
    }

    public static void attachAgent(String str, ClassLoader classLoader) throws IOException {
        VMDebug.attachAgent(str, classLoader);
    }
}
