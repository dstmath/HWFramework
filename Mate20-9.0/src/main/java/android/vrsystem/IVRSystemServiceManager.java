package android.vrsystem;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.view.DisplayInfo;
import java.util.HashSet;

public interface IVRSystemServiceManager {
    public static final String ANDROID_SYSTEM = "android";
    public static final String VIRTUAL_SCREEN_PKG = "com.huawei.vrvirtualscreen";
    public static final int VR_DISPLAY_HEIGHT = 1600;
    public static final int VR_DISPLAY_WIDTH = 2880;
    public static final int VR_DYNAMIC_STACK_ID = 1100000000;
    public static final String VR_LAUNCHER_PACKAGE = "com.huawei.vrlauncherx";
    public static final String VR_MANAGER = "vr_system";
    public static final String VR_VIRTUAL_SCREEN = "HW-VR-Virtual-Screen";
    public static final String VR_VIRTUAL_SCREEN_PACKAGE = "com.huawei.vrvirtualscreen";

    void acceptInCall(Context context);

    void addVRLowPowerAppList(String str);

    boolean allowDisplayFocusByID(int i);

    void endInCall(Context context);

    String getContactName(Context context, String str);

    int getHelmetBattery(Context context);

    int getHelmetBrightness(Context context);

    HashSet<String> getLowerAppList();

    int getVRDisplayID();

    int getVRDisplayID(Context context);

    boolean isVRApplication(Context context, String str);

    boolean isVRDeviceConnected();

    boolean isVRDisplay(int i, int i2, int i3);

    boolean isVRDisplayConnected();

    boolean isVRDynamicStack(int i);

    boolean isVRLowPowerApp(String str);

    boolean isVRMode();

    boolean isValidVRDisplayId(int i);

    boolean isVirtualScreenMode();

    int mirrorVRDisplayIfNeed(IBinder iBinder);

    void registerExpandListener(Context context, IVRListener iVRListener);

    void registerVRListener(Context context, IVRListener iVRListener);

    void setHelmetBrightness(Context context, int i);

    void setTargetComponentName(ComponentName componentName);

    void setVRDispalyInfo(DisplayInfo displayInfo, int i);

    void setVRDisplayConnected(boolean z);

    void setVRDisplayID(int i, boolean z);

    void setVirtualScreenMode(boolean z);

    void unregisterVRListener(Context context, IVRListener iVRListener);
}
