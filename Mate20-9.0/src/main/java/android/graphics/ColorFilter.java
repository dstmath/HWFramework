package android.graphics;

import libcore.util.NativeAllocationRegistry;

public class ColorFilter {
    private Runnable mCleaner;
    private long mNativeInstance;

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry;

        private NoImagePreloadHolder() {
        }

        static {
            NativeAllocationRegistry nativeAllocationRegistry = new NativeAllocationRegistry(ColorFilter.class.getClassLoader(), ColorFilter.nativeGetFinalizer(), 50);
            sRegistry = nativeAllocationRegistry;
        }
    }

    /* access modifiers changed from: private */
    public static native long nativeGetFinalizer();

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
