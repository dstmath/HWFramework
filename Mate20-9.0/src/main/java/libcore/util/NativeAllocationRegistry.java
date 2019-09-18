package libcore.util;

import dalvik.system.VMRuntime;
import sun.misc.Cleaner;

public class NativeAllocationRegistry {
    private final ClassLoader classLoader;
    /* access modifiers changed from: private */
    public final long freeFunction;
    /* access modifiers changed from: private */
    public final long size;

    public interface Allocator {
        long allocate();
    }

    private static class CleanerRunner implements Runnable {
        private final Cleaner cleaner;

        public CleanerRunner(Cleaner cleaner2) {
            this.cleaner = cleaner2;
        }

        public void run() {
            this.cleaner.clean();
        }
    }

    private class CleanerThunk implements Runnable {
        private long nativePtr = 0;

        public CleanerThunk() {
        }

        public void run() {
            if (this.nativePtr != 0) {
                NativeAllocationRegistry.applyFreeFunction(NativeAllocationRegistry.this.freeFunction, this.nativePtr);
                NativeAllocationRegistry.registerNativeFree(NativeAllocationRegistry.this.size);
            }
        }

        public void setNativePtr(long nativePtr2) {
            this.nativePtr = nativePtr2;
        }
    }

    public static native void applyFreeFunction(long j, long j2);

    public NativeAllocationRegistry(ClassLoader classLoader2, long freeFunction2, long size2) {
        if (size2 >= 0) {
            this.classLoader = classLoader2;
            this.freeFunction = freeFunction2;
            this.size = size2;
            return;
        }
        throw new IllegalArgumentException("Invalid native allocation size: " + size2);
    }

    public Runnable registerNativeAllocation(Object referent, long nativePtr) {
        if (referent == null) {
            throw new IllegalArgumentException("referent is null");
        } else if (nativePtr != 0) {
            try {
                CleanerThunk thunk = new CleanerThunk();
                CleanerRunner result = new CleanerRunner(Cleaner.create(referent, thunk));
                registerNativeAllocation(this.size);
                thunk.setNativePtr(nativePtr);
                return result;
            } catch (VirtualMachineError vme) {
                applyFreeFunction(this.freeFunction, nativePtr);
                throw vme;
            }
        } else {
            throw new IllegalArgumentException("nativePtr is null");
        }
    }

    public Runnable registerNativeAllocation(Object referent, Allocator allocator) {
        if (referent != null) {
            CleanerThunk thunk = new CleanerThunk();
            Cleaner cleaner = Cleaner.create(referent, thunk);
            CleanerRunner result = new CleanerRunner(cleaner);
            long nativePtr = allocator.allocate();
            if (nativePtr == 0) {
                cleaner.clean();
                return null;
            }
            registerNativeAllocation(this.size);
            thunk.setNativePtr(nativePtr);
            return result;
        }
        throw new IllegalArgumentException("referent is null");
    }

    private static void registerNativeAllocation(long size2) {
        VMRuntime.getRuntime().registerNativeAllocation((int) Math.min(size2, 2147483647L));
    }

    /* access modifiers changed from: private */
    public static void registerNativeFree(long size2) {
        VMRuntime.getRuntime().registerNativeFree((int) Math.min(size2, 2147483647L));
    }
}
