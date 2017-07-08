package android.animation;

public class IntArrayEvaluator implements TypeEvaluator<int[]> {
    private int[] mArray;

    public IntArrayEvaluator(int[] reuseArray) {
        this.mArray = reuseArray;
    }

    public int[] evaluate(float fraction, int[] startValue, int[] endValue) {
        int[] array = this.mArray;
        if (array == null) {
            array = new int[startValue.length];
        }
        for (int i = 0; i < array.length; i++) {
            int start = startValue[i];
            array[i] = (int) (((float) start) + (((float) (endValue[i] - start)) * fraction));
        }
        return array;
    }
}
