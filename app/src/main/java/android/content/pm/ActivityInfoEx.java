package android.content.pm;

public class ActivityInfoEx {
    public static final int CONFIG_DENSITY_SCALE = 131072;
    public static final int CONFIG_HWTHEME = 32768;
    public static final int CONFIG_SIMPLEUI = 65536;

    public static int activityInfoConfigToNative(int input) {
        return ActivityInfoExInner.activityInfoConfigToNative(input);
    }
}
