package ohos.agp.render.opengl;

public class Utils {
    public static final float EPSILON = 1.0E-6f;

    public static boolean greatNotEqual(float f, float f2) {
        return f - f2 > 1.0E-6f;
    }

    public static boolean greatOrEqual(float f, float f2) {
        return f - f2 > -1.0E-6f;
    }

    public static boolean lessNotEqual(float f, float f2) {
        return f - f2 < -1.0E-6f;
    }

    public static boolean lessOrEqual(float f, float f2) {
        return f - f2 < 1.0E-6f;
    }

    public static boolean nearEqual(float f, float f2, float f3) {
        return Math.abs(f - f2) <= f3;
    }

    public static boolean nearZero(float f, float f2) {
        return nearEqual(f, 0.0f, f2);
    }

    public static boolean nearEqual(float f, float f2) {
        return nearEqual(f, f2, 1.0E-6f);
    }

    public static boolean nearZero(float f) {
        return nearZero(f, 1.0E-6f);
    }
}
