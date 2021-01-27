package android.content.pm;

import android.content.ComponentName;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

public class ActivityInfoEx {
    public static final int CONFIG_ASSETS_PATHS = Integer.MIN_VALUE;
    public static final int CONFIG_DENSITY_SCALE = 131072;
    public static final int CONFIG_HWTHEME = 32768;
    public static final int CONFIG_SIMPLEUI = 65536;
    private static final String TAG = "ActivityInfoEx";
    private ActivityInfo activityInfo;

    @HwSystemApi
    public ActivityInfo getActivityInfo() {
        return this.activityInfo;
    }

    @HwSystemApi
    public void setActivityInfo(ActivityInfo activityInfo2) {
        this.activityInfo = activityInfo2;
    }

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
    public static ComponentName getComponentName(ActivityInfo activityInfo2) {
        return activityInfo2.getComponentName();
    }

    @HwSystemApi
    public static int getRealConfigChanged(ActivityInfo activityInfo2) {
        if (activityInfo2 != null) {
            return activityInfo2.getRealConfigChanged();
        }
        Log.e(TAG, "getRealConfigChanged input activityInfo is null.");
        return 0;
    }

    @HwSystemApi
    public void setProcessName(String process) {
        this.activityInfo.processName = process;
    }

    @HwSystemApi
    public void setTheme(int id) {
        this.activityInfo.theme = id;
    }

    @HwSystemApi
    public String getPackageName() {
        return this.activityInfo.packageName;
    }

    @HwSystemApi
    public String getName() {
        return this.activityInfo.name;
    }

    @HwSystemApi
    public ApplicationInfo getApplicationInfo() {
        return this.activityInfo.applicationInfo;
    }
}
