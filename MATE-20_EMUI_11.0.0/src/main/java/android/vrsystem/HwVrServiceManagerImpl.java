package android.vrsystem;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.vrsystem.IVRSystemService;
import com.huawei.android.hardware.display.HwDisplayManager;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.screenrecorder.activities.SurfaceControlEx;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class HwVrServiceManagerImpl extends DefaultHwVrServiceManager {
    private static final int DEFAULT_BATTERY = 0;
    private static final int DEFAULT_BRIGHTNESS = 0;
    private static final int DEFAULT_CONTAINER_LEN = 32;
    private static final int DISPLAY_HEIGHT_INDEX = 2;
    private static final int DISPLAY_ID_INDEX = 0;
    private static final int DISPLAY_WIDTH_INDEX = 1;
    private static final String EMPTY_STRING = "";
    private static final int EVENT_SLEEP_VR = 0;
    private static final int EVENT_WAKEUP_VR = 1;
    private static final String EXTRA_KEY_TARGET_DISPLAY = "target_display";
    private static final int GO_TO_SLEEP_REASON_POWER_BUTTON = 4;
    private static final int GO_TO_SLEEP_REASON_TIMEOUT = 2;
    private static final String PACKAGE_ANDROID = "android";
    private static final String PACKAGE_ANDROID_LAUNCHER = "com.huawei.android.launcher";
    private static final String PACKAGE_INSTALLER = "com.android.packageinstaller";
    private static final String PACKAGE_SYSTEMUI = "com.android.systemui";
    private static final String PACKAGE_VRINSTALL = "com.huawei.vrinstaller";
    private static final String PACKAGE_VRLAUNCHER = "com.huawei.vrlauncherx";
    private static final String PACKAGE_VRSERVICE = "com.huawei.vrservice";
    private static final String PACKAGE_VRVIRTUALSCREEN = "com.huawei.vrvirtualscreen";
    private static final String TAG = "HwVrServiceManagerImpl";
    private static final String VRSERVICE_PERMISSION = "com.huawei.vrservice.permission.VR";
    private static final int VR_BRIGHTNESS_MAX = 9;
    private static final int VR_BRIGHTNESS_MIN = 0;
    private static final String VR_DISPLAY_PATTERN = "HW-VR-Virtual-Display-\\d+-Src";
    private static final String VR_METADATA_NAME = "com.huawei.android.vr.application.mode";
    private static final String VR_METADATA_VALUE = "vr_only";
    private static final String VR_PERMISSION = "com.huawei.android.permission.VR";
    private static final boolean VR_SUB_SCREEN_SUPPORT = SystemPropertiesEx.getBoolean("ro.vr.mode", false);
    private static final boolean VR_SUPPORT = SystemPropertiesEx.getBoolean("ro.vr.surport", false);
    private static final String VR_VIRTUAL_LAUNCHER_ACTIVITY = "com.huawei.vrvirtualscreen.appdisplay.AppDisplayActivity";
    private static final String VR_VIRTUAL_LAUNCHER_START_PARAM = "displayId";
    private static final String WAKEUP_DETAIL_BLUETOOTH = "bluetooth.connected";
    private static final String WAKEUP_DETAIL_HEADSET = "headset.connected";
    private static final String WAKEUP_DETAIL_POWER = "android.policy:POWER";
    private static int sDisplayId = -1;
    private static boolean sIsVrMode = false;
    private static String sTargetPackageName;
    private int[] mDisplayParams;
    private boolean mIsVRDisplayConnected;
    private boolean mIsVirtualScreenMode;
    private boolean mIsVirtualScreenStarted;
    private int mNextVrVirtualDisplayId;
    private IVRSystemService mService;
    private HashSet<String> mVrLowPowerList;
    private List<String> mVrVirtualAppList;
    private List<Integer> mVrVirtualDisplayIdList;

    public static HwVrServiceManagerImpl getDefault() {
        return Instance.sInstance;
    }

    private static class Instance {
        private static HwVrServiceManagerImpl sInstance = new HwVrServiceManagerImpl();

        private Instance() {
        }
    }

    private HwVrServiceManagerImpl() {
        this.mNextVrVirtualDisplayId = 1000000;
        this.mDisplayParams = new int[3];
        this.mIsVRDisplayConnected = false;
        this.mIsVirtualScreenMode = false;
        this.mIsVirtualScreenStarted = false;
        this.mVrLowPowerList = new HashSet<>((int) DEFAULT_CONTAINER_LEN);
        this.mVrVirtualDisplayIdList = new ArrayList((int) DEFAULT_CONTAINER_LEN);
        this.mVrVirtualAppList = new ArrayList((int) DEFAULT_CONTAINER_LEN);
        this.mService = IVRSystemService.Stub.asInterface(ServiceManagerEx.getService("vr_system"));
    }

    public boolean isVRMode() {
        if (!isClientServiceAlive()) {
            Log.e(TAG, "client service is not alive.");
            return false;
        }
        try {
            return this.mService.isVRmode();
        } catch (RemoteException e) {
            Log.w(TAG, "vr state query exception!");
            return false;
        }
    }

    public boolean isVRApplication(Context context, String packageName) {
        if (isClientServiceAlive()) {
            return isVRApp(context, packageName);
        }
        Log.e(TAG, "client service is not alive.");
        return false;
    }

    public String getContactName(Context context, String num) {
        if (!checkParamsAndPermissionValid(context) || !checkIsHwVRLauncher(context) || TextUtils.isEmpty(num)) {
            Log.e(TAG, "params or permission is not valid in getContactName.");
            return "";
        }
        try {
            return this.mService.getContactName(num);
        } catch (RemoteException e) {
            Log.w(TAG, "vr state query exception!");
            return "";
        }
    }

    public void registerVRListener(Context context, IVRListener listener) {
        if (!checkParamsAndPermissionValid(context) || listener == null) {
            Log.e(TAG, "params or permission is not valid in registerVRListener.");
            return;
        }
        try {
            this.mService.addVRListener(listener);
        } catch (RemoteException e) {
            Log.w(TAG, "add listener exception.");
        }
    }

    public void registerExpandListener(Context context, IVRListener listener) {
        if (!checkParamsAndPermissionValid(context) || listener == null) {
            Log.e(TAG, "params or permission is not valid in registerExpandListener.");
            return;
        }
        try {
            this.mService.registerExpandListener(listener);
        } catch (RemoteException e) {
            Log.w(TAG, "add listener exception.");
        }
    }

    public void unregisterVRListener(Context context, IVRListener listener) {
        if (!checkParamsAndPermissionValid(context) || listener == null) {
            Log.e(TAG, "params or permission is not valid in unregisterVRListener.");
            return;
        }
        try {
            this.mService.deleteVRListener(listener);
        } catch (RemoteException e) {
            Log.w(TAG, "delete listener exception.");
        }
    }

    public void acceptInCall(Context context) {
        if (!checkParamsAndPermissionValid(context) || !checkIsHwVRLauncher(context)) {
            Log.e(TAG, "params or permission is not valid in acceptInCall.");
            return;
        }
        try {
            this.mService.acceptInCall();
        } catch (RemoteException e) {
            Log.w(TAG, "acceptInCall request exception!");
        }
    }

    public void endInCall(Context context) {
        if (!checkParamsAndPermissionValid(context) || !checkIsHwVRLauncher(context)) {
            Log.e(TAG, "params or permission is not valid in endInCall.");
            return;
        }
        try {
            this.mService.endInCall();
        } catch (RemoteException e) {
            Log.w(TAG, "acceptInCall request exception!");
        }
    }

    public int getHelmetBattery(Context context) {
        if (!checkParamsAndPermissionValid(context)) {
            Log.e(TAG, "params or permission is not valid in getHelmetBattery.");
            return 0;
        }
        try {
            return this.mService.getHelmetBattery();
        } catch (RemoteException e) {
            Log.w(TAG, "get Helmet battery exception.");
            return 0;
        }
    }

    public int getHelmetBrightness(Context context) {
        if (!checkParamsAndPermissionValid(context)) {
            Log.e(TAG, "params or permission is not valid in getHelmetBrightness.");
            return 0;
        }
        try {
            return this.mService.getHelmetBrightness();
        } catch (RemoteException e) {
            Log.w(TAG, "get Helmet brightness exception.");
            return 0;
        }
    }

    public void setHelmetBrightness(Context context, int brightness) {
        if (!checkParamsAndPermissionValid(context)) {
            Log.e(TAG, "params or permission is not valid in setHelmetBrightness.");
        } else if (brightness < 0 || brightness > VR_BRIGHTNESS_MAX) {
            Log.w(TAG, "error, invalid brightness " + brightness);
        } else {
            try {
                this.mService.setHelmetBrightness(brightness);
            } catch (RemoteException e) {
                Log.w(TAG, "set Helmet brightness exception.");
            }
        }
    }

    public void setVRDisplayConnected(boolean isConnected) {
        this.mIsVRDisplayConnected = isConnected;
    }

    public boolean isVRDisplayConnected() {
        return this.mIsVRDisplayConnected;
    }

    public void setVirtualScreenMode(boolean isVirtualScreenMode) {
        if (!isVirtualScreenMode && this.mIsVirtualScreenMode) {
            this.mIsVirtualScreenStarted = false;
            HwDisplayManager.destroyAllVrDisplay();
        }
        Log.i(TAG, "set framework virtual screen mode to " + isVirtualScreenMode);
        this.mIsVirtualScreenMode = isVirtualScreenMode;
    }

    public boolean isVirtualScreenMode() {
        return this.mIsVirtualScreenMode;
    }

    public void setVRDisplayID(int displayId, boolean mode) {
        if (!VR_SUB_SCREEN_SUPPORT) {
            Log.w(TAG, "current product not support sub screen mode.");
            return;
        }
        sDisplayId = displayId;
        sIsVrMode = mode;
    }

    public boolean isVRDisplay(int displayId, int width, int height) {
        if (displayId == -1 || displayId == 0) {
            return false;
        }
        return isHwVrDisplay(width, height);
    }

    public boolean isValidVRDisplayId(int displayId) {
        return VR_SUB_SCREEN_SUPPORT && displayId != 0 && displayId != -1 && displayId == sDisplayId;
    }

    public void setTargetComponentName(ComponentName componentName) {
        if (componentName != null) {
            sTargetPackageName = componentName.getPackageName();
        } else {
            sTargetPackageName = null;
        }
    }

    public boolean isVRDynamicStack(int stackId) {
        return VR_SUB_SCREEN_SUPPORT && stackId >= 1100000000;
    }

    public int getVRDisplayID() {
        if (!VR_SUB_SCREEN_SUPPORT) {
            return -1;
        }
        return sDisplayId;
    }

    public int getVRDisplayID(Context context) {
        DisplayManager displayManager;
        int displayId;
        if (!VR_SUB_SCREEN_SUPPORT || context == null || !isVRDeviceConnected()) {
            return -1;
        }
        if (PACKAGE_VRINSTALL.equals(sTargetPackageName)) {
            setTargetComponentName(null);
            return -1;
        }
        String packageName = context.getPackageName();
        if (!(isVRDeviceConnected() || PACKAGE_VRSERVICE.equals(packageName)) || PACKAGE_ANDROID.equals(packageName) || PACKAGE_VRVIRTUALSCREEN.equals(packageName) || !isVRApp(context) || (displayManager = (DisplayManager) context.getSystemService("display")) == null) {
            return -1;
        }
        Display[] displays = displayManager.getDisplays();
        for (Display display : displays) {
            if (display != null && sDisplayId == (displayId = display.getDisplayId())) {
                return displayId;
            }
        }
        setVRDisplayID(-1, false);
        return getVRDisplayID();
    }

    public void addVRLowPowerAppList(String packageName) {
        if (TextUtils.isEmpty(packageName) || this.mVrLowPowerList == null) {
            Log.e(TAG, "params is not valid in addVRLowPowerAppList.");
        } else if (!PACKAGE_SYSTEMUI.equals(packageName) && !PACKAGE_INSTALLER.equals(packageName) && !this.mVrLowPowerList.contains(packageName)) {
            this.mVrLowPowerList.add(packageName);
        }
    }

    public boolean isVRLowPowerApp(String packageName) {
        HashSet<String> hashSet = this.mVrLowPowerList;
        if (hashSet != null) {
            return hashSet.contains(packageName);
        }
        Log.e(TAG, "params is not valid in isVRLowPowerApp.");
        return false;
    }

    public boolean isVRDeviceConnected() {
        return VR_SUB_SCREEN_SUPPORT && sIsVrMode;
    }

    public void setVrDisplayInfo(DisplayInfoExt displayInfo, int displayId) {
        if (displayInfo == null || this.mDisplayParams == null) {
            Log.e(TAG, "displayInfo or mDisplayParams is null");
        } else if (isVRDisplay(displayId, displayInfo.getNaturalWidth(), displayInfo.getNaturalHeight())) {
            int[] iArr = this.mDisplayParams;
            if (iArr.length >= 3) {
                iArr[0] = displayId;
                iArr[1] = displayInfo.getNaturalWidth();
                this.mDisplayParams[2] = displayInfo.getNaturalHeight();
                setVRDisplayID(displayId, true);
                setVRDisplayConnected(true);
            }
        }
    }

    public int mirrorVRDisplayIfNeed(IBinder displayToken) {
        if (displayToken == null || !isVRDeviceConnected()) {
            Log.w(TAG, "displayToken is null or vr disconnect in mirrorVRDisplayIfNeed");
            return -1;
        }
        int displayId = getVRDisplayID();
        if (isVRMode() && displayId > 0) {
            Log.i(TAG, "performTraversalInTransactionLocked setDisplayLayerStack VR layer stack id:" + displayId);
            SurfaceControlEx.setDisplayLayerStack(displayToken, displayId);
        }
        return displayId;
    }

    public HashSet<String> getLowerAppList() {
        HashSet<String> hashSet = this.mVrLowPowerList;
        if (hashSet == null) {
            return new HashSet<>(0);
        }
        return hashSet;
    }

    public boolean allowDisplayFocusByID(int displayId) {
        if (!isVRDeviceConnected()) {
            return true;
        }
        if (isVirtualScreenMode() && displayId != getVRDisplayID()) {
            return true;
        }
        if (isVirtualScreenMode() || displayId != getVRDisplayID()) {
            return false;
        }
        return true;
    }

    public boolean handlePowerEventForVr(int eventType, int reason, String details) {
        if (!isVRDeviceConnected()) {
            return false;
        }
        if (eventType == 0) {
            return processSleepInVrMode(reason);
        }
        if (eventType == 1) {
            return processWakeupInVrMode(details);
        }
        Log.w(TAG, "other event type for vr unknown.");
        return false;
    }

    public int assignVrDisplayIdIfNeeded(boolean isDefault, int originDisplayId, String displayName) {
        if (isDefault || !isVrVirtualDisplay(displayName)) {
            return originDisplayId;
        }
        int i = this.mNextVrVirtualDisplayId;
        this.mNextVrVirtualDisplayId = i + 1;
        return i;
    }

    public boolean isVrVirtualDisplay(String displayName) {
        if (!TextUtils.isEmpty(displayName)) {
            return Pattern.compile(VR_DISPLAY_PATTERN).matcher(displayName).find();
        }
        Log.e(TAG, "params is not valid in isVrVirtualDisplay.");
        return false;
    }

    public boolean isVrVirtualDisplay(int displayId) {
        return displayId >= 1000000;
    }

    public void addVrVirtualDisplayId(int displayId) {
        List<Integer> list = this.mVrVirtualDisplayIdList;
        if (list != null) {
            list.add(Integer.valueOf(displayId));
        }
    }

    public void removeVrVirtualDisplayId(int displayId) {
        List<Integer> list = this.mVrVirtualDisplayIdList;
        if (list != null) {
            list.remove(Integer.valueOf(displayId));
        }
    }

    public void clearVrVirtualDisplay() {
        List<Integer> list = this.mVrVirtualDisplayIdList;
        if (list != null) {
            list.clear();
        }
    }

    public int getTopVrVirtualDisplayId() {
        List<Integer> list = this.mVrVirtualDisplayIdList;
        if (list == null || list.isEmpty()) {
            return 0;
        }
        List<Integer> list2 = this.mVrVirtualDisplayIdList;
        return list2.get(list2.size() - 1).intValue();
    }

    public List<Integer> getAllVrVirtualDisplayId() {
        List<Integer> list = this.mVrVirtualDisplayIdList;
        if (list == null) {
            return new ArrayList(0);
        }
        return list;
    }

    public boolean isVrCaredDisplay(int displayId) {
        return isVRDeviceConnected() && (isValidVRDisplayId(displayId) || isVrVirtualDisplay(displayId));
    }

    public void recordVirtualApp(String packageName) {
        List<String> list;
        if (packageName == null || (list = this.mVrVirtualAppList) == null || list.contains(packageName)) {
            Log.e(TAG, "params is not valid in recordVirtualApp.");
        } else if (PACKAGE_ANDROID_LAUNCHER.equals(packageName)) {
            Log.i(TAG, "do not record android launcher into virtual apps list.");
        } else {
            this.mVrVirtualAppList.add(packageName);
        }
    }

    public boolean isVirtualAppRecorded(String packageName) {
        if (this.mVrVirtualAppList != null && !TextUtils.isEmpty(packageName)) {
            return this.mVrVirtualAppList.contains(packageName);
        }
        Log.e(TAG, "params is not valid in isVirtualAppRecorded.");
        return false;
    }

    public void removeRecordedVirtualApp(String packageName) {
        if (this.mVrVirtualAppList == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "params is not valid in removeRecordedVirtualApp.");
        } else {
            this.mVrVirtualAppList.remove(packageName);
        }
    }

    public void clearRecordedVirtualAppList() {
        List<String> list = this.mVrVirtualAppList;
        if (list == null) {
            Log.e(TAG, "params is not valid in clearRecordedVirtualAppList.");
        } else {
            list.clear();
        }
    }

    public boolean isAbleToLaunchInVr(Context context, Intent intent, String callingPackage, ActivityInfo launchActivityInfo) {
        if (!isVRDeviceConnected() || context == null || intent == null || launchActivityInfo == null) {
            return true;
        }
        String launchPackage = launchActivityInfo.packageName;
        Log.i(TAG, "isAbleToLaunchInVr: " + callingPackage + " start " + launchPackage);
        if (isStartingNormalAppInVr(context, launchPackage)) {
            return false;
        }
        if (isStartingLauncherByVirtualApp(callingPackage, launchPackage)) {
            startVrVirtualLauncher(context);
            return false;
        }
        updateVrLowPowerAppList(context, launchPackage);
        updateVrVirtualScreenMode(intent, callingPackage, launchPackage);
        return true;
    }

    public int getVrPreferredDisplayId(String launchedFromPkg, String startingPkg, int preferredDisplayId) {
        if (isVRDeviceConnected() && !isVirtualScreenMode()) {
            return getVRDisplayID();
        }
        if (PACKAGE_VRVIRTUALSCREEN.equals(launchedFromPkg)) {
            this.mIsVirtualScreenStarted = true;
        }
        if (this.mIsVirtualScreenStarted || !PACKAGE_VRVIRTUALSCREEN.equals(startingPkg)) {
            return getVirtualPreferredId(launchedFromPkg, startingPkg, preferredDisplayId);
        }
        return getVRDisplayID();
    }

    public int[] getVrDisplayParams() {
        return this.mDisplayParams;
    }

    private boolean isPhoneSupportVR() {
        return VR_SUPPORT;
    }

    private boolean isClientServiceAlive() {
        if (!isPhoneSupportVR()) {
            return false;
        }
        this.mService = IVRSystemService.Stub.asInterface(ServiceManagerEx.getService("vr_system"));
        if (this.mService != null) {
            return true;
        }
        return false;
    }

    private boolean checkParamsAndPermissionValid(Context context) {
        if (!isClientServiceAlive() || !isVRApp(context) || checkPermission(context) == -1) {
            return false;
        }
        return true;
    }

    private boolean isVRApp(Context context) {
        if (context != null) {
            return isVRApp(context, context.getPackageName());
        }
        Log.e(TAG, "context is null in isVRApp.");
        return false;
    }

    private boolean isVRApp(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "params is invalid in isVRApp.");
            return false;
        } else if (PACKAGE_SYSTEMUI.equals(packageName) || VR_METADATA_VALUE.equals(getManifestMetadata(context, packageName, VR_METADATA_NAME))) {
            return true;
        } else {
            Log.w(TAG, "no vr app metadata ");
            return false;
        }
    }

    private String getManifestMetadata(Context context, String packageName, String name) {
        if (context == null || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(name)) {
            Log.w(TAG, "params is invalid in getManifestMetadata.");
            return "";
        }
        ApplicationInfo mAppInfo = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) {
                Log.w(TAG, "packageManager is null in getManifestMetadata.");
                return "";
            }
            mAppInfo = packageManager.getApplicationInfo(packageName, 128);
            if (mAppInfo == null || mAppInfo.metaData == null) {
                return "";
            }
            return mAppInfo.metaData.getString(name);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo exception ");
        }
    }

    private boolean checkIsHwVRLauncher(Context context) {
        if (context == null) {
            Log.e(TAG, "context is null.");
            return false;
        }
        String packageName = context.getPackageName();
        if (packageName == null) {
            Log.e(TAG, "packageName is null in checkIsHwVrLauncher.");
            return false;
        } else if (PACKAGE_VRLAUNCHER.equals(packageName)) {
            return true;
        } else {
            return false;
        }
    }

    private int checkPermission(Context context) {
        if (context == null) {
            Log.e(TAG, "context is null.");
            return -1;
        }
        String packageName = context.getPackageName();
        if (packageName == null) {
            Log.e(TAG, "packageName is null in checkPermission.");
            return -1;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            Log.e(TAG, "packageManager is null in checkPermission.");
            return -1;
        } else if (packageName.equals(PACKAGE_INSTALLER) || packageManager.checkPermission(VRSERVICE_PERMISSION, packageName) == 0 || packageManager.checkPermission(VR_PERMISSION, packageName) == 0) {
            return 0;
        } else {
            return -1;
        }
    }

    private boolean isHwVrDisplay(int width, int height) {
        return isWallexDisplay(width, height) || isHallidayDisplay(width, height);
    }

    private boolean isWallexDisplay(int width, int height) {
        return (width == 2880 && height == 1600) || (width == 1600 && height == 2880);
    }

    private boolean isHallidayDisplay(int width, int height) {
        return (width == 3200 && height == 1600) || (width == 1600 && height == 3200);
    }

    private boolean processSleepInVrMode(int reason) {
        return reason == GO_TO_SLEEP_REASON_POWER_BUTTON || reason == 2;
    }

    private boolean processWakeupInVrMode(String details) {
        if (WAKEUP_DETAIL_POWER.equals(details) || WAKEUP_DETAIL_HEADSET.equals(details) || WAKEUP_DETAIL_BLUETOOTH.equals(details)) {
            return true;
        }
        return false;
    }

    private boolean isStartingNormalAppInVr(Context context, String launchPackage) {
        return !isVirtualScreenMode() && !isVRApplication(context, launchPackage);
    }

    private boolean isStartingLauncherByVirtualApp(String callingPackage, String launchPackage) {
        return isVirtualScreenMode() && isVirtualAppRecorded(callingPackage) && !PACKAGE_VRVIRTUALSCREEN.equals(callingPackage) && PACKAGE_ANDROID_LAUNCHER.equals(launchPackage);
    }

    private void startVrVirtualLauncher(Context context) {
        Log.i(TAG, "startVrVirtualLauncher");
        int virtualDisplayId = getTopVrVirtualDisplayId();
        if (virtualDisplayId == 0 || virtualDisplayId == -1) {
            Log.w(TAG, "warning, record vr virtual display invalid, stop to start virtual launcher");
            return;
        }
        try {
            Intent intent = new Intent();
            intent.addFlags(268435456);
            intent.putExtra(VR_VIRTUAL_LAUNCHER_START_PARAM, virtualDisplayId);
            intent.setComponent(new ComponentName(PACKAGE_VRVIRTUALSCREEN, VR_VIRTUAL_LAUNCHER_ACTIVITY));
            ActivityOptions activityOptions = ActivityOptions.makeBasic();
            activityOptions.setLaunchDisplayId(virtualDisplayId);
            context.startActivity(intent, activityOptions.toBundle());
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "activity not found in startVrVirtualLauncher.");
        }
    }

    private void updateVrLowPowerAppList(Context context, String launchPackage) {
        if (isVRApplication(context, launchPackage)) {
            addVRLowPowerAppList(launchPackage);
        }
    }

    private void updateVrVirtualScreenMode(Intent intent, String callingPackage, String launchPackage) {
        if (PACKAGE_VRVIRTUALSCREEN.equals(launchPackage)) {
            setVirtualScreenMode(true);
        }
        if (PACKAGE_VRLAUNCHER.equals(launchPackage) && PACKAGE_VRVIRTUALSCREEN.equals(callingPackage) && intent.getIntExtra(EXTRA_KEY_TARGET_DISPLAY, -1) == -1) {
            setVirtualScreenMode(false);
        }
    }

    private int getVirtualPreferredId(String launchedFromPkg, String startingPkg, int preferredDisplayId) {
        if (getTopVrVirtualDisplayId() != 0) {
            return getMultiplePreferredId(launchedFromPkg, startingPkg, preferredDisplayId);
        }
        Log.d(TAG, "No virtual display now, start app on default display aways.");
        return 0;
    }

    private int getMultiplePreferredId(String launchedFromPkg, String startingPkg, int preferredDisplayId) {
        int newPreferredDisplayId;
        Log.d(TAG, launchedFromPkg + " start " + startingPkg);
        int recordVirtualDisplayId = getTopVrVirtualDisplayId();
        if (PACKAGE_ANDROID_LAUNCHER.equals(startingPkg)) {
            Log.i(TAG, "start android launcher on display 0");
            newPreferredDisplayId = 0;
        } else if (PACKAGE_VRVIRTUALSCREEN.equals(launchedFromPkg) || PACKAGE_ANDROID.equals(launchedFromPkg)) {
            Log.i(TAG, "start on display " + preferredDisplayId + " by virtualscreen or system.");
            newPreferredDisplayId = preferredDisplayId;
        } else if (isVirtualAppRecorded(launchedFromPkg)) {
            Log.i(TAG, "start on virtual display " + recordVirtualDisplayId);
            newPreferredDisplayId = recordVirtualDisplayId;
        } else {
            Log.i(TAG, "start on default display 0");
            newPreferredDisplayId = 0;
        }
        updateVirtualAppsList(startingPkg, newPreferredDisplayId);
        return newPreferredDisplayId;
    }

    private void updateVirtualAppsList(String startingPkg, int preferredDisplayId) {
        if (preferredDisplayId == getTopVrVirtualDisplayId()) {
            Log.d(TAG, "record virtual app " + startingPkg);
            recordVirtualApp(startingPkg);
            return;
        }
        Log.d(TAG, "remove recorded virtual app " + startingPkg);
        removeRecordedVirtualApp(startingPkg);
    }
}
