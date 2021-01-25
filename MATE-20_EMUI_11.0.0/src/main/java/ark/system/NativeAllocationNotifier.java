package ark.system;

import libcore.util.NativeAllocationRegistry;

public final class NativeAllocationNotifier {
    public static Runnable notifyMallocAllocation(ClassLoader classLoader, long j, long j2, Object obj, long j3) {
        return NativeAllocationRegistry.createMalloced(classLoader, j, j2).registerNativeAllocation(obj, j3);
    }

    public static Runnable notifyNonmallocAllocation(ClassLoader classLoader, long j, long j2, Object obj, long j3) {
        return NativeAllocationRegistry.createNonmalloced(classLoader, j, j2).registerNativeAllocation(obj, j3);
    }
}
