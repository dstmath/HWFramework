package ohos.os;

import android.os.Process;

public class ProcessManager {
    public static final long getStartUptime() {
        return Process.getStartUptimeMillis();
    }

    public static final long getStartRealtime() {
        return Process.getStartElapsedRealtime();
    }

    public static final long getPastCpuTime() {
        return Process.getElapsedCpuTime();
    }

    public static final int[] getAvailableCores() {
        return Process.getExclusiveCores();
    }

    public static final int getPid() {
        return Process.myPid();
    }

    public static final int getTid() {
        return Process.myTid();
    }

    public static final int getUid() {
        return Process.myUid();
    }

    public static final int getUidByName(String str) {
        return Process.getUidForName(str);
    }

    public static final int getGidByName(String str) {
        return Process.getGidForName(str);
    }

    public static final int getThreadPriority(int i) throws IllegalArgumentException {
        return Process.getThreadPriority(i);
    }

    public static final boolean is64Bit() {
        return Process.is64Bit();
    }

    public static boolean isAppUid(int i) {
        return Process.isApplicationUid(i);
    }

    public static final boolean isIsolatedProcess() {
        return Process.isIsolated();
    }

    public static final void setThreadPriority(int i) throws IllegalArgumentException, SecurityException {
        Process.setThreadPriority(i);
    }

    public static final void setThreadPriority(int i, int i2) throws IllegalArgumentException, SecurityException {
        Process.setThreadPriority(i, i2);
    }

    public static final void sendSignal(int i, int i2) {
        Process.sendSignal(i, i2);
    }

    public static final void kill(int i) {
        Process.killProcess(i);
    }
}
