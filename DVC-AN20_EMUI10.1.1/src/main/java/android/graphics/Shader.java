package android.graphics;

import android.annotation.UnsupportedAppUsage;
import libcore.util.NativeAllocationRegistry;

public class Shader {
    private Runnable mCleaner;
    private final ColorSpace mColorSpace;
    private Matrix mLocalMatrix;
    private long mNativeInstance;

    /* access modifiers changed from: private */
    public static native long nativeGetFinalizer();

    /* access modifiers changed from: private */
    public static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = NativeAllocationRegistry.createMalloced(Shader.class.getClassLoader(), Shader.nativeGetFinalizer());

        private NoImagePreloadHolder() {
        }
    }

    @Deprecated
    public Shader() {
        this.mColorSpace = null;
    }

    public Shader(ColorSpace colorSpace) {
        this.mColorSpace = colorSpace;
        if (colorSpace != null) {
            this.mColorSpace.getNativeInstance();
            return;
        }
        throw new IllegalArgumentException("Use Shader() to create a Shader with no ColorSpace");
    }

    /* access modifiers changed from: protected */
    public ColorSpace colorSpace() {
        return this.mColorSpace;
    }

    public enum TileMode {
        CLAMP(0),
        REPEAT(1),
        MIRROR(2);
        
        @UnsupportedAppUsage
        final int nativeInt;

        private TileMode(int nativeInt2) {
            this.nativeInt = nativeInt2;
        }
    }

    public boolean getLocalMatrix(Matrix localM) {
        Matrix matrix = this.mLocalMatrix;
        if (matrix == null) {
            return false;
        }
        localM.set(matrix);
        return true;
    }

    public void setLocalMatrix(Matrix localM) {
        if (localM != null && !localM.isIdentity()) {
            Matrix matrix = this.mLocalMatrix;
            if (matrix == null) {
                this.mLocalMatrix = new Matrix(localM);
                discardNativeInstance();
            } else if (!matrix.equals(localM)) {
                this.mLocalMatrix.set(localM);
                discardNativeInstance();
            }
        } else if (this.mLocalMatrix != null) {
            this.mLocalMatrix = null;
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

    public final long getNativeInstance() {
        verifyNativeInstance();
        if (this.mNativeInstance == 0) {
            Matrix matrix = this.mLocalMatrix;
            this.mNativeInstance = createNativeInstance(matrix == null ? 0 : matrix.native_instance);
            if (this.mNativeInstance != 0) {
                this.mCleaner = NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeInstance);
            }
        }
        return this.mNativeInstance;
    }

    public static long[] convertColors(int[] colors) {
        if (colors.length >= 2) {
            long[] colorLongs = new long[colors.length];
            for (int i = 0; i < colors.length; i++) {
                colorLongs[i] = Color.pack(colors[i]);
            }
            return colorLongs;
        }
        throw new IllegalArgumentException("needs >= 2 number of colors");
    }

    public static ColorSpace detectColorSpace(long[] colors) {
        if (colors.length >= 2) {
            ColorSpace colorSpace = Color.colorSpace(colors[0]);
            for (int i = 1; i < colors.length; i++) {
                if (Color.colorSpace(colors[i]) != colorSpace) {
                    throw new IllegalArgumentException("All colors must be in the same ColorSpace!");
                }
            }
            return colorSpace;
        }
        throw new IllegalArgumentException("needs >= 2 number of colors");
    }
}
