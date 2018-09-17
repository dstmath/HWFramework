package android.animation;

public class IntEvaluator implements TypeEvaluator<Integer> {
    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
        int startInt = startValue.intValue();
        return Integer.valueOf((int) (((float) startInt) + (((float) (endValue.intValue() - startInt)) * fraction)));
    }
}
