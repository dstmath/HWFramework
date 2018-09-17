package android.animation;

public class ArgbEvaluator implements TypeEvaluator {
    private static final ArgbEvaluator sInstance = new ArgbEvaluator();

    public static ArgbEvaluator getInstance() {
        return sInstance;
    }

    public Object evaluate(float fraction, Object startValue, Object endValue) {
        int startInt = ((Integer) startValue).intValue();
        float startA = ((float) ((startInt >> 24) & 255)) / 255.0f;
        float startR = ((float) ((startInt >> 16) & 255)) / 255.0f;
        float startG = ((float) ((startInt >> 8) & 255)) / 255.0f;
        float startB = ((float) (startInt & 255)) / 255.0f;
        int endInt = ((Integer) endValue).intValue();
        startR = (float) Math.pow((double) startR, 2.2d);
        startG = (float) Math.pow((double) startG, 2.2d);
        startB = (float) Math.pow((double) startB, 2.2d);
        return Integer.valueOf((((Math.round((startA + (((((float) ((endInt >> 24) & 255)) / 255.0f) - startA) * fraction)) * 255.0f) << 24) | (Math.round(((float) Math.pow((double) (startR + ((((float) Math.pow((double) (((float) ((endInt >> 16) & 255)) / 255.0f), 2.2d)) - startR) * fraction)), 0.45454545454545453d)) * 255.0f) << 16)) | (Math.round(((float) Math.pow((double) (startG + ((((float) Math.pow((double) (((float) ((endInt >> 8) & 255)) / 255.0f), 2.2d)) - startG) * fraction)), 0.45454545454545453d)) * 255.0f) << 8)) | Math.round(((float) Math.pow((double) (startB + ((((float) Math.pow((double) (((float) (endInt & 255)) / 255.0f), 2.2d)) - startB) * fraction)), 0.45454545454545453d)) * 255.0f));
    }
}
