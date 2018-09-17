package android.util;

@Deprecated
public class FloatMath {
    private FloatMath() {
    }

    public static float floor(float value) {
        return (float) Math.floor((double) value);
    }

    public static float ceil(float value) {
        return (float) Math.ceil((double) value);
    }

    public static float sin(float angle) {
        return (float) Math.sin((double) angle);
    }

    public static float cos(float angle) {
        return (float) Math.cos((double) angle);
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt((double) value);
    }

    public static float exp(float value) {
        return (float) Math.exp((double) value);
    }

    public static float pow(float x, float y) {
        return (float) Math.pow((double) x, (double) y);
    }

    public static float hypot(float x, float y) {
        return (float) Math.hypot((double) x, (double) y);
    }
}
