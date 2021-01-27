package android.animation;

public class FloatEvaluator implements TypeEvaluator<Number> {
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return Float.valueOf(((endValue.floatValue() - startFloat) * fraction) + startFloat);
    }
}
