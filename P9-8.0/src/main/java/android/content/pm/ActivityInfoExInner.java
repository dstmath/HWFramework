package android.content.pm;

public class ActivityInfoExInner {
    public static final int CONFIG_DENSITY_SCALE = 131072;
    public static final int CONFIG_HWTHEME = 32768;
    static final int[] CONFIG_NATIVE_BITS = new int[]{1, 2, 4, 8, 16, 32, 64, 128, 2048, 4096, 512, 8192, 256, 16384, 0, 32768};
    public static final int CONFIG_SIMPLEUI = 65536;

    public static int activityInfoConfigToNative(int input) {
        int output = 0;
        for (int i = 0; i < CONFIG_NATIVE_BITS.length; i++) {
            if (((1 << i) & input) != 0) {
                output |= CONFIG_NATIVE_BITS[i];
            }
        }
        return output;
    }
}
