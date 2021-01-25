package android.animation;

import android.animation.Keyframe;
import android.graphics.Path;
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

    @Override // android.animation.Keyframes
    public List<Keyframe> getKeyframes() {
        return this.mKeyframes;
    }

    public static KeyframeSet ofInt(int... values) {
        int numKeyframes = values.length;
        Keyframe.IntKeyframe[] keyframes = new Keyframe.IntKeyframe[Math.max(numKeyframes, 2)];
        if (numKeyframes == 1) {
            keyframes[0] = (Keyframe.IntKeyframe) Keyframe.ofInt(0.0f);
            keyframes[1] = (Keyframe.IntKeyframe) Keyframe.ofInt(1.0f, values[0]);
        } else {
            keyframes[0] = (Keyframe.IntKeyframe) Keyframe.ofInt(0.0f, values[0]);
            for (int i = 1; i < numKeyframes; i++) {
                keyframes[i] = (Keyframe.IntKeyframe) Keyframe.ofInt(((float) i) / ((float) (numKeyframes - 1)), values[i]);
            }
        }
        return new IntKeyframeSet(keyframes);
    }

    public static KeyframeSet ofFloat(float... values) {
        boolean badValue = false;
        int numKeyframes = values.length;
        Keyframe.FloatKeyframe[] keyframes = new Keyframe.FloatKeyframe[Math.max(numKeyframes, 2)];
        if (numKeyframes == 1) {
            keyframes[0] = (Keyframe.FloatKeyframe) Keyframe.ofFloat(0.0f);
            keyframes[1] = (Keyframe.FloatKeyframe) Keyframe.ofFloat(1.0f, values[0]);
            if (Float.isNaN(values[0])) {
                badValue = true;
            }
        } else {
            keyframes[0] = (Keyframe.FloatKeyframe) Keyframe.ofFloat(0.0f, values[0]);
            for (int i = 1; i < numKeyframes; i++) {
                keyframes[i] = (Keyframe.FloatKeyframe) Keyframe.ofFloat(((float) i) / ((float) (numKeyframes - 1)), values[i]);
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
        boolean hasInt = false;
        boolean hasOther = false;
        for (int i = 0; i < numKeyframes; i++) {
            if (keyframes[i] instanceof Keyframe.FloatKeyframe) {
                hasFloat = true;
            } else if (keyframes[i] instanceof Keyframe.IntKeyframe) {
                hasInt = true;
            } else {
                hasOther = true;
            }
        }
        if (hasFloat && !hasInt && !hasOther) {
            Keyframe.FloatKeyframe[] floatKeyframes = new Keyframe.FloatKeyframe[numKeyframes];
            for (int i2 = 0; i2 < numKeyframes; i2++) {
                floatKeyframes[i2] = (Keyframe.FloatKeyframe) keyframes[i2];
            }
            return new FloatKeyframeSet(floatKeyframes);
        } else if (!hasInt || hasFloat || hasOther) {
            return new KeyframeSet(keyframes);
        } else {
            Keyframe.IntKeyframe[] intKeyframes = new Keyframe.IntKeyframe[numKeyframes];
            for (int i3 = 0; i3 < numKeyframes; i3++) {
                intKeyframes[i3] = (Keyframe.IntKeyframe) keyframes[i3];
            }
            return new IntKeyframeSet(intKeyframes);
        }
    }

    public static KeyframeSet ofObject(Object... values) {
        int numKeyframes = values.length;
        Keyframe.ObjectKeyframe[] keyframes = new Keyframe.ObjectKeyframe[Math.max(numKeyframes, 2)];
        if (numKeyframes == 1) {
            keyframes[0] = (Keyframe.ObjectKeyframe) Keyframe.ofObject(0.0f);
            keyframes[1] = (Keyframe.ObjectKeyframe) Keyframe.ofObject(1.0f, values[0]);
        } else {
            keyframes[0] = (Keyframe.ObjectKeyframe) Keyframe.ofObject(0.0f, values[0]);
            for (int i = 1; i < numKeyframes; i++) {
                keyframes[i] = (Keyframe.ObjectKeyframe) Keyframe.ofObject(((float) i) / ((float) (numKeyframes - 1)), values[i]);
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

    @Override // android.animation.Keyframes
    public void setEvaluator(TypeEvaluator evaluator) {
        this.mEvaluator = evaluator;
    }

    @Override // android.animation.Keyframes
    public Class getType() {
        return this.mFirstKeyframe.getType();
    }

    @Override // android.animation.Keyframes, java.lang.Object
    public KeyframeSet clone() {
        List<Keyframe> keyframes = this.mKeyframes;
        int numKeyframes = this.mKeyframes.size();
        Keyframe[] newKeyframes = new Keyframe[numKeyframes];
        for (int i = 0; i < numKeyframes; i++) {
            newKeyframes[i] = keyframes.get(i).clone();
        }
        return new KeyframeSet(newKeyframes);
    }

    @Override // android.animation.Keyframes
    public Object getValue(float fraction) {
        int i = this.mNumKeyframes;
        if (i == 2) {
            TimeInterpolator timeInterpolator = this.mInterpolator;
            if (timeInterpolator != null) {
                fraction = timeInterpolator.getInterpolation(fraction);
            }
            return this.mEvaluator.evaluate(fraction, this.mFirstKeyframe.getValue(), this.mLastKeyframe.getValue());
        } else if (fraction <= 0.0f) {
            Keyframe nextKeyframe = this.mKeyframes.get(1);
            TimeInterpolator interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            float prevFraction = this.mFirstKeyframe.getFraction();
            return this.mEvaluator.evaluate((fraction - prevFraction) / (nextKeyframe.getFraction() - prevFraction), this.mFirstKeyframe.getValue(), nextKeyframe.getValue());
        } else if (fraction >= 1.0f) {
            Keyframe prevKeyframe = this.mKeyframes.get(i - 2);
            TimeInterpolator interpolator2 = this.mLastKeyframe.getInterpolator();
            if (interpolator2 != null) {
                fraction = interpolator2.getInterpolation(fraction);
            }
            float prevFraction2 = prevKeyframe.getFraction();
            return this.mEvaluator.evaluate((fraction - prevFraction2) / (this.mLastKeyframe.getFraction() - prevFraction2), prevKeyframe.getValue(), this.mLastKeyframe.getValue());
        } else {
            Keyframe prevKeyframe2 = this.mFirstKeyframe;
            for (int i2 = 1; i2 < this.mNumKeyframes; i2++) {
                Keyframe nextKeyframe2 = this.mKeyframes.get(i2);
                if (fraction < nextKeyframe2.getFraction()) {
                    TimeInterpolator interpolator3 = nextKeyframe2.getInterpolator();
                    float prevFraction3 = prevKeyframe2.getFraction();
                    float intervalFraction = (fraction - prevFraction3) / (nextKeyframe2.getFraction() - prevFraction3);
                    if (interpolator3 != null) {
                        intervalFraction = interpolator3.getInterpolation(intervalFraction);
                    }
                    return this.mEvaluator.evaluate(intervalFraction, prevKeyframe2.getValue(), nextKeyframe2.getValue());
                }
                prevKeyframe2 = nextKeyframe2;
            }
            return this.mLastKeyframe.getValue();
        }
    }

    @Override // java.lang.Object
    public String toString() {
        String returnVal = WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        for (int i = 0; i < this.mNumKeyframes; i++) {
            returnVal = returnVal + this.mKeyframes.get(i).getValue() + "  ";
        }
        return returnVal;
    }
}
