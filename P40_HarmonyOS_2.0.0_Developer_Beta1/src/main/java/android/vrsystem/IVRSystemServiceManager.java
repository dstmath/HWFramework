package android.vrsystem;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;
import java.util.HashSet;
import java.util.List;

public interface IVRSystemServiceManager {
    public static final String ANDROID_LAUNCHER_PKG = "com.huawei.android.launcher";
    public static final String ANDROID_SYSTEM = "android";
    public static final String HW_VR_DISPLAY_NAME = "HUAWEI VR Display";
    public static final String HW_VR_SUFFIX = "-Src";
    public static final String SYSTEM_PACKAGE = "android";
    public static final int VR_DISPLAY_PARAMS_LENGTH = 3;
    public static final int VR_DYNAMIC_STACK_ID = 1100000000;
    public static final int VR_HALLIDAY_DISPLAY_HEIGHT = 1600;
    public static final int VR_HALLIDAY_DISPLAY_WIDTH = 3200;
    public static final int VR_HEIGHTY = -1500;
    public static final String VR_LAUNCHER_PACKAGE = "com.huawei.vrlauncherx";
    public static final String VR_MANAGER = "vr_system";
    public static final int VR_VIRTUAL_DEFAULT_DISPLAY_ID = 1000000;
    public static final String VR_VIRTUAL_LAUNCHER_ACTIVITY = "com.huawei.vrvirtualscreen.appdisplay.AppDisplayActivity";
    public static final String VR_VIRTUAL_LAUNCHER_START_PARAM = "displayId";
    public static final String VR_VIRTUAL_SCREEN = "HW-VR-Virtual-Screen";
    public static final String VR_VIRTUAL_SCREEN_PACKAGE = "com.huawei.vrvirtualscreen";
    public static final int VR_WALLEX_DISPLAY_HEIGHT = 1600;
    public static final int VR_WALLEX_DISPLAY_WIDTH = 2880;
    public static final int VR_WINDOWX = -1500;

    void acceptInCall(Context context);

    void addVRLowPowerAppList(String str);

    void addVrVirtualDisplayId(int i);

    boolean allowDisplayFocusByID(int i);

    int assignVrDisplayIdIfNeeded(boolean z, int i, String str);

    void clearRecordedVirtualAppList();

    void clearVrVirtualDisplay();

    void endInCall(Context context);

    List<Integer> getAllVrVirtualDisplayId();

    String getContactName(Context context, String str);

    int getHelmetBattery(Context context);

    int getHelmetBrightness(Context context);

    HashSet<String> getLowerAppList();

    int getTopVrVirtualDisplayId();

    int getVRDisplayID();

    int getVRDisplayID(Context context);

    int[] getVrDisplayParams();

    int getVrPreferredDisplayId(String str, String str2, int i);

    boolean handlePowerEventForVr(int i, int i2, String str);

    boolean isAbleToLaunchInVr(Context context, Intent intent, String str, ActivityInfo activityInfo);

    boolean isVRApplication(Context context, String str);

    boolean isVRDeviceConnected();

    boolean isVRDisplay(int i, int i2, int i3);

    boolean isVRDisplayConnected();

    boolean isVRDynamicStack(int i);

    boolean isVRLowPowerApp(String str);

    boolean isVRMode();

    boolean isValidVRDisplayId(int i);

    boolean isVirtualAppRecorded(String str);

    boolean isVirtualScreenMode();

    boolean isVrCaredDisplay(int i);

    boolean isVrVirtualDisplay(int i);

    boolean isVrVirtualDisplay(String str);

    int mirrorVRDisplayIfNeed(IBinder iBinder);

    void recordVirtualApp(String str);

    void registerExpandListener(Context context, IVRListener iVRListener);

    void registerVRListener(Context context, IVRListener iVRListener);

    void removeRecordedVirtualApp(String str);

    void removeVrVirtualDisplayId(int i);

    void setHelmetBrightness(Context context, int i);

    void setTargetComponentName(ComponentName componentName);

    void setVRDisplayConnected(boolean z);

    void setVRDisplayID(int i, boolean z);

    void setVirtualScreenMode(boolean z);

    void setVrDisplayInfo(DisplayInfoExt displayInfoExt, int i);

    void unregisterVRListener(Context context, IVRListener iVRListener);
}
