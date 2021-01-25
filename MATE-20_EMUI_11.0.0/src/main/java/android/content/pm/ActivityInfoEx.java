package android.content.pm;

import android.content.ComponentName;
import com.huawei.annotation.HwSystemApi;

public class ActivityInfoEx {
    public static final int CONFIG_DENSITY_SCALE = 131072;
    public static final int CONFIG_HWTHEME = 32768;
    public static final int CONFIG_SIMPLEUI = 65536;

    public static int activityInfoConfigToNative(int input) {
        return ActivityInfoExInner.activityInfoConfigToNative(input);
    }

    @HwSystemApi
    public static boolean isResizeableMode(int mode) {
        return ActivityInfo.isResizeableMode(mode);
    }

    @HwSystemApi
    public static boolean isFixedOrientationLandscape(int orientation) {
        return ActivityInfo.isFixedOrientationLandscape(orientation);
    }

    @HwSystemApi
    public static boolean isFixedOrientationPortrait(int orientation) {
        return ActivityInfo.isFixedOrientationPortrait(orientation);
    }

    @HwSystemApi
    public static ComponentName getComponentName(ActivityInfo activityInfo) {
        return activityInfo.getComponentName();
    }
}
