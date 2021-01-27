package android.vrsystem;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.os.UserHandle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DefaultHwVrServiceManager implements IVRSystemServiceManager {
    private static final int DEFAULT_BATTERY = 0;
    private static final int DEFAULT_BRIGHTNESS = 0;
    private static final int DEFAULT_EMPTY_SIZE = 0;
    private static final String TAG = "DefaultHwVrServiceManager";

    private static class Instance {
        private static DefaultHwVrServiceManager sInstance = new DefaultHwVrServiceManager();

        private Instance() {
        }
    }

    public static DefaultHwVrServiceManager getDefault() {
        return Instance.sInstance;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVRMode() {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVRApplication(Context context, String packageName) {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public String getContactName(Context context, String num) {
        return "";
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void registerVRListener(Context context, IVRListener listener) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void registerExpandListener(Context context, IVRListener listener) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void unregisterVRListener(Context context, IVRListener listener) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void acceptInCall(Context context) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void endInCall(Context context) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public int getHelmetBattery(Context context) {
        return 0;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public int getHelmetBrightness(Context context) {
        return 0;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void setHelmetBrightness(Context context, int brightness) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void setVRDisplayConnected(boolean connected) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVRDisplayConnected() {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void setVirtualScreenMode(boolean mode) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVirtualScreenMode() {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void setVRDisplayID(int displayId, boolean mode) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVRDisplay(int displayId, int width, int height) {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isValidVRDisplayId(int displayId) {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void setTargetComponentName(ComponentName componentName) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVRDynamicStack(int stackId) {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public int getVRDisplayID() {
        return -1;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public int getVRDisplayID(Context context) {
        return -1;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void addVRLowPowerAppList(String packageName) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVRLowPowerApp(String packageName) {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVRDeviceConnected() {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void setVrDisplayInfo(DisplayInfoExt displayInfoEx, int displayID) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public int mirrorVRDisplayIfNeed(IBinder displayToken) {
        return -1;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public HashSet<String> getLowerAppList() {
        return new HashSet<>(0);
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean allowDisplayFocusByID(int displayId) {
        return true;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean handlePowerEventForVr(int eventType, int reason, String details) {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public int assignVrDisplayIdIfNeeded(boolean isDefault, int originDisplayId, String displayName) {
        return -1;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVrVirtualDisplay(String displayName) {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVrVirtualDisplay(int displayId) {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void addVrVirtualDisplayId(int displayId) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void removeVrVirtualDisplayId(int displayId) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void clearVrVirtualDisplay() {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public int getTopVrVirtualDisplayId() {
        return -1;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public List<Integer> getAllVrVirtualDisplayId() {
        return new ArrayList(0);
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVrCaredDisplay(int displayId) {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void recordVirtualApp(String packageName) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isVirtualAppRecorded(String packageName) {
        return false;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void removeRecordedVirtualApp(String packageName) {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public void clearRecordedVirtualAppList() {
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public boolean isAbleToLaunchInVr(Context context, Intent intent, String callingPackage, ActivityInfo launchActivityInfo) {
        return true;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public int getVrPreferredDisplayId(String launchedFromPkg, String startingPkg, int preferredDisplayId) {
        return -1;
    }

    @Override // android.vrsystem.IVRSystemServiceManager
    public int[] getVrDisplayParams() {
        return new int[0];
    }

    public boolean requestPermissionInVr(Context context, Intent intent, UserHandle user) {
        return false;
    }
}
