package android.graphics;

import android.hardware.camera2.params.TonemapCurve;
import android.os.SystemClock;

public class Interpolator {
    private int mFrameCount;
    private int mValueCount;
    private long native_instance;

    public enum Result {
        NORMAL,
        FREEZE_START,
        FREEZE_END
    }

    private static native long nativeConstructor(int i, int i2);

    private static native void nativeDestructor(long j);

    private static native void nativeReset(long j, int i, int i2);

    private static native void nativeSetKeyFrame(long j, int i, int i2, float[] fArr, float[] fArr2);

    private static native void nativeSetRepeatMirror(long j, float f, boolean z);

    private static native int nativeTimeToValues(long j, int i, float[] fArr);

    public Interpolator(int valueCount) {
        this.mValueCount = valueCount;
        this.mFrameCount = 2;
        this.native_instance = nativeConstructor(valueCount, 2);
    }

    public Interpolator(int valueCount, int frameCount) {
        this.mValueCount = valueCount;
        this.mFrameCount = frameCount;
        this.native_instance = nativeConstructor(valueCount, frameCount);
    }

    public void reset(int valueCount) {
        reset(valueCount, 2);
    }

    public void reset(int valueCount, int frameCount) {
        this.mValueCount = valueCount;
        this.mFrameCount = frameCount;
        nativeReset(this.native_instance, valueCount, frameCount);
    }

    public final int getKeyFrameCount() {
        return this.mFrameCount;
    }

    public final int getValueCount() {
        return this.mValueCount;
    }

    public void setKeyFrame(int index, int msec, float[] values) {
        setKeyFrame(index, msec, values, null);
    }

    public void setKeyFrame(int index, int msec, float[] values, float[] blend) {
        if (index < 0 || index >= this.mFrameCount) {
            throw new IndexOutOfBoundsException();
        } else if (values.length < this.mValueCount) {
            throw new ArrayStoreException();
        } else if (blend == null || blend.length >= 4) {
            nativeSetKeyFrame(this.native_instance, index, msec, values, blend);
        } else {
            throw new ArrayStoreException();
        }
    }

    public void setRepeatMirror(float repeatCount, boolean mirror) {
        if (repeatCount >= TonemapCurve.LEVEL_BLACK) {
            nativeSetRepeatMirror(this.native_instance, repeatCount, mirror);
        }
    }

    public Result timeToValues(float[] values) {
        return timeToValues((int) SystemClock.uptimeMillis(), values);
    }

    public Result timeToValues(int msec, float[] values) {
        if (values == null || values.length >= this.mValueCount) {
            switch (nativeTimeToValues(this.native_instance, msec, values)) {
                case 0:
                    return Result.NORMAL;
                case 1:
                    return Result.FREEZE_START;
                default:
                    return Result.FREEZE_END;
            }
        }
        throw new ArrayStoreException();
    }

    protected void finalize() throws Throwable {
        nativeDestructor(this.native_instance);
        this.native_instance = 0;
    }
}
