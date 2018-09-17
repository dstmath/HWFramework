package android.view;

import android.util.Pools.SynchronizedPool;
import android.view.WindowManager.LayoutParams;
import com.android.internal.widget.AutoScrollHelper;

public final class VelocityTracker {
    private static final int ACTIVE_POINTER_ID = -1;
    private static final SynchronizedPool<VelocityTracker> sPool = null;
    private long mPtr;
    private final String mStrategy;

    public static final class Estimator {
        private static final int MAX_DEGREE = 4;
        public float confidence;
        public int degree;
        public final float[] xCoeff;
        public final float[] yCoeff;

        public Estimator() {
            this.xCoeff = new float[5];
            this.yCoeff = new float[5];
        }

        public float estimateX(float time) {
            return estimate(time, this.xCoeff);
        }

        public float estimateY(float time) {
            return estimate(time, this.yCoeff);
        }

        public float getXCoeff(int index) {
            return index <= this.degree ? this.xCoeff[index] : 0.0f;
        }

        public float getYCoeff(int index) {
            return index <= this.degree ? this.yCoeff[index] : 0.0f;
        }

        private float estimate(float time, float[] c) {
            float a = 0.0f;
            float scale = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            for (int i = 0; i <= this.degree; i++) {
                a += c[i] * scale;
                scale *= time;
            }
            return a;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.VelocityTracker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.VelocityTracker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.VelocityTracker.<clinit>():void");
    }

    private static native void nativeAddMovement(long j, MotionEvent motionEvent);

    private static native void nativeClear(long j);

    private static native void nativeComputeCurrentVelocity(long j, int i, float f);

    private static native void nativeDispose(long j);

    private static native boolean nativeGetEstimator(long j, int i, Estimator estimator);

    private static native float nativeGetXVelocity(long j, int i);

    private static native float nativeGetYVelocity(long j, int i);

    private static native long nativeInitialize(String str);

    public static VelocityTracker obtain() {
        VelocityTracker instance = (VelocityTracker) sPool.acquire();
        return instance != null ? instance : new VelocityTracker(null);
    }

    public static VelocityTracker obtain(String strategy) {
        if (strategy == null) {
            return obtain();
        }
        return new VelocityTracker(strategy);
    }

    public void recycle() {
        if (this.mStrategy == null) {
            clear();
            sPool.release(this);
        }
    }

    private VelocityTracker(String strategy) {
        this.mPtr = nativeInitialize(strategy);
        this.mStrategy = strategy;
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mPtr != 0) {
                nativeDispose(this.mPtr);
                this.mPtr = 0;
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public void clear() {
        nativeClear(this.mPtr);
    }

    public void addMovement(MotionEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        nativeAddMovement(this.mPtr, event);
    }

    public void computeCurrentVelocity(int units) {
        nativeComputeCurrentVelocity(this.mPtr, units, AutoScrollHelper.NO_MAX);
    }

    public void computeCurrentVelocity(int units, float maxVelocity) {
        nativeComputeCurrentVelocity(this.mPtr, units, maxVelocity);
    }

    public float getXVelocity() {
        return nativeGetXVelocity(this.mPtr, ACTIVE_POINTER_ID);
    }

    public float getYVelocity() {
        return nativeGetYVelocity(this.mPtr, ACTIVE_POINTER_ID);
    }

    public float getXVelocity(int id) {
        return nativeGetXVelocity(this.mPtr, id);
    }

    public float getYVelocity(int id) {
        return nativeGetYVelocity(this.mPtr, id);
    }

    public boolean getEstimator(int id, Estimator outEstimator) {
        if (outEstimator != null) {
            return nativeGetEstimator(this.mPtr, id, outEstimator);
        }
        throw new IllegalArgumentException("outEstimator must not be null");
    }
}
