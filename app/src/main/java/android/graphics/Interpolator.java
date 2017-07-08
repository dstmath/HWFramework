package android.graphics;

import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.telecom.AudioState;

public class Interpolator {
    private int mFrameCount;
    private int mValueCount;
    private long native_instance;

    public enum Result {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Interpolator.Result.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Interpolator.Result.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Interpolator.Result.<clinit>():void");
        }
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
        if (repeatCount >= 0.0f) {
            nativeSetRepeatMirror(this.native_instance, repeatCount, mirror);
        }
    }

    public Result timeToValues(float[] values) {
        return timeToValues((int) SystemClock.uptimeMillis(), values);
    }

    public Result timeToValues(int msec, float[] values) {
        if (values == null || values.length >= this.mValueCount) {
            switch (nativeTimeToValues(this.native_instance, msec, values)) {
                case TextToSpeech.SUCCESS /*0*/:
                    return Result.NORMAL;
                case AudioState.ROUTE_EARPIECE /*1*/:
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
