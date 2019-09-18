package android.graphics;

import libcore.util.NativeAllocationRegistry;

public class Shader {
    private Runnable mCleaner;
    private Matrix mLocalMatrix;
    private long mNativeInstance;

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry;

        private NoImagePreloadHolder() {
        }

        static {
            NativeAllocationRegistry nativeAllocationRegistry = new NativeAllocationRegistry(Shader.class.getClassLoader(), Shader.nativeGetFinalizer(), 50);
            sRegistry = nativeAllocationRegistry;
        }
    }

    public enum TileMode {
        CLAMP(0),
        REPEAT(1),
        MIRROR(2);
        
        final int nativeInt;

        private TileMode(int nativeInt2) {
            this.nativeInt = nativeInt2;
        }
    }

    /* access modifiers changed from: private */
    public static native long nativeGetFinalizer();

    public boolean getLocalMatrix(Matrix localM) {
        if (this.mLocalMatrix == null) {
            return false;
        }
        localM.set(this.mLocalMatrix);
        return true;
    }

    public void setLocalMatrix(Matrix localM) {
        if (localM == null || localM.isIdentity()) {
            if (this.mLocalMatrix != null) {
                this.mLocalMatrix = null;
                discardNativeInstance();
            }
        } else if (this.mLocalMatrix == null) {
            this.mLocalMatrix = new Matrix(localM);
            discardNativeInstance();
        } else if (!this.mLocalMatrix.equals(localM)) {
            this.mLocalMatrix.set(localM);
            discardNativeInstance();
        }
    }

    /* access modifiers changed from: package-private */
    public long createNativeInstance(long nativeMatrix) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public final void discardNativeInstance() {
        if (this.mNativeInstance != 0) {
            this.mCleaner.run();
            this.mCleaner = null;
            this.mNativeInstance = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void verifyNativeInstance() {
    }

    /* access modifiers changed from: protected */
    public Shader copy() {
        Shader copy = new Shader();
        copyLocalMatrix(copy);
        return copy;
    }

    /* access modifiers changed from: protected */
    public void copyLocalMatrix(Shader dest) {
        dest.mLocalMatrix.set(this.mLocalMatrix);
    }

    public final long getNativeInstance() {
        long j;
        verifyNativeInstance();
        if (this.mNativeInstance == 0) {
            if (this.mLocalMatrix == null) {
                j = 0;
            } else {
                j = this.mLocalMatrix.native_instance;
            }
            this.mNativeInstance = createNativeInstance(j);
            if (this.mNativeInstance != 0) {
                this.mCleaner = NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeInstance);
            }
        }
        return this.mNativeInstance;
    }
}
