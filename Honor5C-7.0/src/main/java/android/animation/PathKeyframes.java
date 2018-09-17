package android.animation;

import android.animation.Keyframes.FloatKeyframes;
import android.animation.Keyframes.IntKeyframes;
import android.graphics.Path;
import android.graphics.PointF;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.speech.tts.TextToSpeech.Engine;
import java.util.ArrayList;

public class PathKeyframes implements Keyframes {
    private static final ArrayList<Keyframe> EMPTY_KEYFRAMES = null;
    private static final int FRACTION_OFFSET = 0;
    private static final int NUM_COMPONENTS = 3;
    private static final int X_OFFSET = 1;
    private static final int Y_OFFSET = 2;
    private float[] mKeyframeData;
    private PointF mTempPointF;

    private static abstract class SimpleKeyframes implements Keyframes {
        private SimpleKeyframes() {
        }

        public void setEvaluator(TypeEvaluator evaluator) {
        }

        public void invalidateCache() {
        }

        public ArrayList<Keyframe> getKeyframes() {
            return PathKeyframes.EMPTY_KEYFRAMES;
        }

        public Keyframes clone() {
            Keyframes clone = null;
            try {
                return (Keyframes) super.clone();
            } catch (CloneNotSupportedException e) {
                return clone;
            }
        }
    }

    static abstract class FloatKeyframesBase extends SimpleKeyframes implements FloatKeyframes {
        FloatKeyframesBase() {
            super();
        }

        public Class getType() {
            return Float.class;
        }

        public Object getValue(float fraction) {
            return Float.valueOf(getFloatValue(fraction));
        }
    }

    static abstract class IntKeyframesBase extends SimpleKeyframes implements IntKeyframes {
        IntKeyframesBase() {
            super();
        }

        public Class getType() {
            return Integer.class;
        }

        public Object getValue(float fraction) {
            return Integer.valueOf(getIntValue(fraction));
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.animation.PathKeyframes.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.animation.PathKeyframes.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.animation.PathKeyframes.<clinit>():void");
    }

    public PathKeyframes(Path path) {
        this(path, NetworkHistoryUtils.RECOVERY_PERCENTAGE);
    }

    public PathKeyframes(Path path, float error) {
        this.mTempPointF = new PointF();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("The path must not be null or empty");
        }
        this.mKeyframeData = path.approximate(error);
    }

    public ArrayList<Keyframe> getKeyframes() {
        return EMPTY_KEYFRAMES;
    }

    public Object getValue(float fraction) {
        int numPoints = this.mKeyframeData.length / NUM_COMPONENTS;
        if (fraction < 0.0f) {
            return interpolateInRange(fraction, FRACTION_OFFSET, X_OFFSET);
        }
        if (fraction > Engine.DEFAULT_VOLUME) {
            return interpolateInRange(fraction, numPoints - 2, numPoints - 1);
        }
        if (fraction == 0.0f) {
            return pointForIndex(FRACTION_OFFSET);
        }
        if (fraction == Engine.DEFAULT_VOLUME) {
            return pointForIndex(numPoints - 1);
        }
        int low = FRACTION_OFFSET;
        int high = numPoints - 1;
        while (low <= high) {
            int mid = (low + high) / Y_OFFSET;
            float midFraction = this.mKeyframeData[(mid * NUM_COMPONENTS) + FRACTION_OFFSET];
            if (fraction < midFraction) {
                high = mid - 1;
            } else if (fraction <= midFraction) {
                return pointForIndex(mid);
            } else {
                low = mid + X_OFFSET;
            }
        }
        return interpolateInRange(fraction, high, low);
    }

    private PointF interpolateInRange(float fraction, int startIndex, int endIndex) {
        int startBase = startIndex * NUM_COMPONENTS;
        int endBase = endIndex * NUM_COMPONENTS;
        float startFraction = this.mKeyframeData[startBase + FRACTION_OFFSET];
        float intervalFraction = (fraction - startFraction) / (this.mKeyframeData[endBase + FRACTION_OFFSET] - startFraction);
        float startX = this.mKeyframeData[startBase + X_OFFSET];
        float endX = this.mKeyframeData[endBase + X_OFFSET];
        float startY = this.mKeyframeData[startBase + Y_OFFSET];
        float endY = this.mKeyframeData[endBase + Y_OFFSET];
        this.mTempPointF.set(interpolate(intervalFraction, startX, endX), interpolate(intervalFraction, startY, endY));
        return this.mTempPointF;
    }

    public void invalidateCache() {
    }

    public void setEvaluator(TypeEvaluator evaluator) {
    }

    public Class getType() {
        return PointF.class;
    }

    public Keyframes clone() {
        Keyframes clone = null;
        try {
            return (Keyframes) super.clone();
        } catch (CloneNotSupportedException e) {
            return clone;
        }
    }

    private PointF pointForIndex(int index) {
        int base = index * NUM_COMPONENTS;
        this.mTempPointF.set(this.mKeyframeData[base + X_OFFSET], this.mKeyframeData[base + Y_OFFSET]);
        return this.mTempPointF;
    }

    private static float interpolate(float fraction, float startValue, float endValue) {
        return ((endValue - startValue) * fraction) + startValue;
    }

    public FloatKeyframes createXFloatKeyframes() {
        return new FloatKeyframesBase() {
            public float getFloatValue(float fraction) {
                return ((PointF) PathKeyframes.this.getValue(fraction)).x;
            }
        };
    }

    public FloatKeyframes createYFloatKeyframes() {
        return new FloatKeyframesBase() {
            public float getFloatValue(float fraction) {
                return ((PointF) PathKeyframes.this.getValue(fraction)).y;
            }
        };
    }

    public IntKeyframes createXIntKeyframes() {
        return new IntKeyframesBase() {
            public int getIntValue(float fraction) {
                return Math.round(((PointF) PathKeyframes.this.getValue(fraction)).x);
            }
        };
    }

    public IntKeyframes createYIntKeyframes() {
        return new IntKeyframesBase() {
            public int getIntValue(float fraction) {
                return Math.round(((PointF) PathKeyframes.this.getValue(fraction)).y);
            }
        };
    }
}
