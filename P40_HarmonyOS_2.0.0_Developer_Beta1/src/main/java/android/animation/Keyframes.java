package android.animation;

import java.util.List;

public interface Keyframes extends Cloneable {

    public interface FloatKeyframes extends Keyframes {
        float getFloatValue(float f);
    }

    public interface IntKeyframes extends Keyframes {
        int getIntValue(float f);
    }

    @Override // java.lang.Object
    Keyframes clone();

    List<Keyframe> getKeyframes();

    Class getType();

    Object getValue(float f);

    void setEvaluator(TypeEvaluator typeEvaluator);
}
