package huawei.android.widget.appbar;

public class MathUtils {
    private MathUtils() {
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static int max(int aValue, int bValue) {
        return aValue > bValue ? aValue : bValue;
    }

    public static int min(int aValue, int bValue) {
        return aValue < bValue ? aValue : bValue;
    }
}
