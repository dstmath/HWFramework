package android.graphics;

import libcore.util.NativeAllocationRegistry;

public class Shader {
    private Runnable mCleaner;
    private Matrix mLocalMatrix;
    private long mNativeInstance;

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = new NativeAllocationRegistry(Shader.class.getClassLoader(), Shader.nativeGetFinalizer(), 50);

        private NoImagePreloadHolder() {
        }
    }

    public enum TileMode {
        CLAMP(0),
        REPEAT(1),
        MIRROR(2);
        
        final int nativeInt;

        private TileMode(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    private static native long nativeGetFinalizer();

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

    long createNativeInstance(long nativeMatrix) {
        return 0;
    }

    protected final void discardNativeInstance() {
        if (this.mNativeInstance != 0) {
            this.mCleaner.run();
            this.mCleaner = null;
            this.mNativeInstance = 0;
        }
    }

    protected void verifyNativeInstance() {
    }

    protected Shader copy() {
        Shader copy = new Shader();
        copyLocalMatrix(copy);
        return copy;
    }

    protected void copyLocalMatrix(Shader dest) {
        dest.mLocalMatrix.set(this.mLocalMatrix);
    }

    public final long getNativeInstance() {
        long j = 0;
        verifyNativeInstance();
        if (this.mNativeInstance == 0) {
            if (this.mLocalMatrix != null) {
                j = this.mLocalMatrix.native_instance;
            }
            this.mNativeInstance = createNativeInstance(j);
            this.mCleaner = NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeInstance);
        }
        return this.mNativeInstance;
    }
}
