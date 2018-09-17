package android.animation;

import android.graphics.Path;
import android.hardware.camera2.params.TonemapCurve;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import java.util.Arrays;
import java.util.List;

public class KeyframeSet implements Keyframes {
    TypeEvaluator mEvaluator;
    Keyframe mFirstKeyframe;
    TimeInterpolator mInterpolator = this.mLastKeyframe.getInterpolator();
    List<Keyframe> mKeyframes;
    Keyframe mLastKeyframe;
    int mNumKeyframes;

    public KeyframeSet(Keyframe... keyframes) {
        this.mNumKeyframes = keyframes.length;
        this.mKeyframes = Arrays.asList(keyframes);
        this.mFirstKeyframe = keyframes[0];
        this.mLastKeyframe = keyframes[this.mNumKeyframes - 1];
    }

    public List<Keyframe> getKeyframes() {
        return this.mKeyframes;
    }

    public static KeyframeSet ofInt(int... values) {
        int numKeyframes = values.length;
        IntKeyframe[] keyframes = new IntKeyframe[Math.max(numKeyframes, 2)];
        if (numKeyframes == 1) {
            keyframes[0] = (IntKeyframe) Keyframe.ofInt(TonemapCurve.LEVEL_BLACK);
            keyframes[1] = (IntKeyframe) Keyframe.ofInt(1.0f, values[0]);
        } else {
            keyframes[0] = (IntKeyframe) Keyframe.ofInt(TonemapCurve.LEVEL_BLACK, values[0]);
            for (int i = 1; i < numKeyframes; i++) {
                keyframes[i] = (IntKeyframe) Keyframe.ofInt(((float) i) / ((float) (numKeyframes - 1)), values[i]);
            }
        }
        return new IntKeyframeSet(keyframes);
    }

    public static KeyframeSet ofFloat(float... values) {
        boolean badValue = false;
        int numKeyframes = values.length;
        FloatKeyframe[] keyframes = new FloatKeyframe[Math.max(numKeyframes, 2)];
        if (numKeyframes == 1) {
            keyframes[0] = (FloatKeyframe) Keyframe.ofFloat(TonemapCurve.LEVEL_BLACK);
            keyframes[1] = (FloatKeyframe) Keyframe.ofFloat(1.0f, values[0]);
            if (Float.isNaN(values[0])) {
                badValue = true;
            }
        } else {
            keyframes[0] = (FloatKeyframe) Keyframe.ofFloat(TonemapCurve.LEVEL_BLACK, values[0]);
            for (int i = 1; i < numKeyframes; i++) {
                keyframes[i] = (FloatKeyframe) Keyframe.ofFloat(((float) i) / ((float) (numKeyframes - 1)), values[i]);
                if (Float.isNaN(values[i])) {
                    badValue = true;
                }
            }
        }
        if (badValue) {
            Log.w("Animator", "Bad value (NaN) in float animator");
        }
        return new FloatKeyframeSet(keyframes);
    }

    public static KeyframeSet ofKeyframe(Keyframe... keyframes) {
        int numKeyframes = keyframes.length;
        boolean hasFloat = false;
        int i = 0;
        while (i < numKeyframes) {
            if (!(keyframes[i] instanceof FloatKeyframe) && (keyframes[i] instanceof IntKeyframe)) {
                hasFloat = true;
                i++;
            } else {
                hasFloat = true;
                i++;
            }
        }
        if (hasFloat && (0 ^ 1) != 0 && (0 ^ 1) != 0) {
            FloatKeyframe[] floatKeyframes = new FloatKeyframe[numKeyframes];
            for (i = 0; i < numKeyframes; i++) {
                floatKeyframes[i] = (FloatKeyframe) keyframes[i];
            }
            return new FloatKeyframeSet(floatKeyframes);
        } else if (null == null || (hasFloat ^ 1) == 0 || (0 ^ 1) == 0) {
            return new KeyframeSet(keyframes);
        } else {
            IntKeyframe[] intKeyframes = new IntKeyframe[numKeyframes];
            for (i = 0; i < numKeyframes; i++) {
                intKeyframes[i] = (IntKeyframe) keyframes[i];
            }
            return new IntKeyframeSet(intKeyframes);
        }
    }

    public static KeyframeSet ofObject(Object... values) {
        int numKeyframes = values.length;
        ObjectKeyframe[] keyframes = new ObjectKeyframe[Math.max(numKeyframes, 2)];
        if (numKeyframes == 1) {
            keyframes[0] = (ObjectKeyframe) Keyframe.ofObject(TonemapCurve.LEVEL_BLACK);
            keyframes[1] = (ObjectKeyframe) Keyframe.ofObject(1.0f, values[0]);
        } else {
            keyframes[0] = (ObjectKeyframe) Keyframe.ofObject(TonemapCurve.LEVEL_BLACK, values[0]);
            for (int i = 1; i < numKeyframes; i++) {
                keyframes[i] = (ObjectKeyframe) Keyframe.ofObject(((float) i) / ((float) (numKeyframes - 1)), values[i]);
            }
        }
        return new KeyframeSet(keyframes);
    }

    public static PathKeyframes ofPath(Path path) {
        return new PathKeyframes(path);
    }

    public static PathKeyframes ofPath(Path path, float error) {
        return new PathKeyframes(path, error);
    }

    public void setEvaluator(TypeEvaluator evaluator) {
        this.mEvaluator = evaluator;
    }

    public Class getType() {
        return this.mFirstKeyframe.getType();
    }

    public KeyframeSet clone() {
        List<Keyframe> keyframes = this.mKeyframes;
        int numKeyframes = this.mKeyframes.size();
        Keyframe[] newKeyframes = new Keyframe[numKeyframes];
        for (int i = 0; i < numKeyframes; i++) {
            newKeyframes[i] = ((Keyframe) keyframes.get(i)).clone();
        }
        return new KeyframeSet(newKeyframes);
    }

    public Object getValue(float fraction) {
        Keyframe nextKeyframe;
        TimeInterpolator interpolator;
        float prevFraction;
        Keyframe prevKeyframe;
        if (this.mNumKeyframes == 2) {
            if (this.mInterpolator != null) {
                fraction = this.mInterpolator.getInterpolation(fraction);
            }
            return this.mEvaluator.evaluate(fraction, this.mFirstKeyframe.getValue(), this.mLastKeyframe.getValue());
        } else if (fraction <= TonemapCurve.LEVEL_BLACK) {
            nextKeyframe = (Keyframe) this.mKeyframes.get(1);
            interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            prevFraction = this.mFirstKeyframe.getFraction();
            return this.mEvaluator.evaluate((fraction - prevFraction) / (nextKeyframe.getFraction() - prevFraction), this.mFirstKeyframe.getValue(), nextKeyframe.getValue());
        } else if (fraction >= 1.0f) {
            prevKeyframe = (Keyframe) this.mKeyframes.get(this.mNumKeyframes - 2);
            interpolator = this.mLastKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            prevFraction = prevKeyframe.getFraction();
            return this.mEvaluator.evaluate((fraction - prevFraction) / (this.mLastKeyframe.getFraction() - prevFraction), prevKeyframe.getValue(), this.mLastKeyframe.getValue());
        } else {
            prevKeyframe = this.mFirstKeyframe;
            for (int i = 1; i < this.mNumKeyframes; i++) {
                nextKeyframe = (Keyframe) this.mKeyframes.get(i);
                if (fraction < nextKeyframe.getFraction()) {
                    interpolator = nextKeyframe.getInterpolator();
                    prevFraction = prevKeyframe.getFraction();
                    float intervalFraction = (fraction - prevFraction) / (nextKeyframe.getFraction() - prevFraction);
                    if (interpolator != null) {
                        intervalFraction = interpolator.getInterpolation(intervalFraction);
                    }
                    return this.mEvaluator.evaluate(intervalFraction, prevKeyframe.getValue(), nextKeyframe.getValue());
                }
                prevKeyframe = nextKeyframe;
            }
            return this.mLastKeyframe.getValue();
        }
    }

    public String toString() {
        String returnVal = WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        for (int i = 0; i < this.mNumKeyframes; i++) {
            returnVal = returnVal + ((Keyframe) this.mKeyframes.get(i)).getValue() + "  ";
        }
        return returnVal;
    }
}
