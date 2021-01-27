package ark.system;

public final class VMRuntime {
    public static void notifyNativeAllocation(long j) {
        dalvik.system.VMRuntime.getRuntime().registerNativeAllocation(j);
    }

    public static void notifyNativeFree(long j) {
        dalvik.system.VMRuntime.getRuntime().registerNativeFree(j);
    }

    public static long getFinalizerTimeoutMs() {
        return dalvik.system.VMRuntime.getRuntime().getFinalizerTimeoutMs();
    }
}
