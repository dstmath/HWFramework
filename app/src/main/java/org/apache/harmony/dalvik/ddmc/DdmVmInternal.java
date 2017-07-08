package org.apache.harmony.dalvik.ddmc;

public class DdmVmInternal {
    public static native void enableRecentAllocations(boolean z);

    public static native boolean getRecentAllocationStatus();

    public static native byte[] getRecentAllocations();

    public static native StackTraceElement[] getStackTraceById(int i);

    public static native byte[] getThreadStats();

    public static native boolean heapInfoNotify(int i);

    public static native boolean heapSegmentNotify(int i, int i2, boolean z);

    public static native void threadNotify(boolean z);

    private DdmVmInternal() {
    }
}
