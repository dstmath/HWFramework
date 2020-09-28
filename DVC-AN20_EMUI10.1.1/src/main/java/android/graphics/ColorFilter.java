package android.graphics;

import libcore.util.NativeAllocationRegistry;

public class ColorFilter {
    private Runnable mCleaner;
    private long mNativeInstance;

    /* access modifiers changed from: private */
    public static native long nativeGetFinalizer();

    /* access modifiers changed from: private */
    public static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = NativeAllocationRegistry.createMalloced(ColorFilter.class.getClassLoader(), ColorFilter.nativeGetFinalizer());

        private NoImagePreloadHolder() {
        }
    }

    /* access modifiers changed from: package-private */
    public long createNativeInstance() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void discardNativeInstance() {
        if (this.mNativeInstance != 0) {
            this.mCleaner.run();
            this.mCleaner = null;
            this.mNativeInstance = 0;
        }
    }

    public long getNativeInstance() {
        if (this.mNativeInstance == 0) {
            this.mNativeInstance = createNativeInstance();
            if (this.mNativeInstance != 0) {
                this.mCleaner = NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeInstance);
            }
        }
        return this.mNativeInstance;
    }
}
