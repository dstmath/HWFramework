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
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import android.vrsystem.IVRSystemService;
import com.huawei.android.hardware.display.HwDisplayManager;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class VRSystemServiceManager implements IVRSystemServiceManager {
    private static final int EVENT_SLEEP_VR = 0;
    private static final int EVENT_WAKEUP_VR = 1;
    private static final String EXTRA_KEY_TARGET_DISPLAY = "target_display";
    private static final boolean IS_VR_ENABLE = SystemProperties.getBoolean("ro.vr.mode", false);
    private static final String PACKAGE_INSTALL = "com.android.packageinstaller";
    private static final String PACKAGE_INSTALLER = "com.android.packageinstaller";
    private static final int PERMISSION_ERROR = -1;
    private static final int PERMISSION_SUCCESS = 0;
    private static final String SYSTEMUI = "com.android.systemui";
    private static final String TAG = "VRSystemServiceManager";
    private static final String VRSERVICE_PERMISSION = "com.huawei.vrservice.permission.VR";
    private static final String VR_DISPLAY_PATTERN = "HW-VR-Virtual-Display-\\d+-Src";
    private static final String VR_INSTALL = "com.huawei.vrinstaller";
    private static final String VR_LAUNCHER = "com.huawei.vrlauncherx";
    private static final String VR_METADATA_NAME = "com.huawei.android.vr.application.mode";
    private static final String VR_METADATA_VALUE = "vr_only";
    private static final String VR_PERMISSION = "com.huawei.android.permission.VR";
    private static final int VR_PROCESS_ARGS = 3;
    private static final String VR_SERVICE = "com.huawei.vrservice";
    private static final boolean VR_SWITCH = SystemProperties.getBoolean("ro.vr.surport", false);
    private static final String WAKEUP_DETAIL_BLUETOOTH = "bluetooth.connected";
    private static final String WAKEUP_DETAIL_HEADSET = "headset.connected";
    private static final String WAKEUP_DETAIL_POWER = "android.policy:POWER";
    private static int sDisplayID = -1;
    private static boolean sIsVrMode = false;
    private static String sTargetPackageName;
    private int[] mDisplayparams;
    private boolean mIsVRDisplayConnected;
    private boolean mIsVirtualScreenMode;
    private boolean mIsVirtualScreenStarted;
    private int mNextVrVirtualDisplayId;
    private IVRSystemService mVRM;
    private List<String> mVrVirtualAppList;
    private List<Integer> mVrVirtualDisplayIdList;
    private HashSet<String> sVRLowPowerList;

    /* access modifiers changed from: private */
    public static class Instance {
        private static VRSystemServiceManager sInstance = new VRSystemServiceManager();

        private Instance() {
        }
    }

    public static VRSystemServiceManager getInstance() {
        return Instance.sInstance;
    }

    public boolean isVRMode() {
        if (!isValid()) {
            return false;
        }
        try {
            return this.mVRM.isVRmode();
        } catch (RemoteException ex) {
            Log.w(TAG, "vr state query exception! ", ex);
            return false;
        }
    }

    public boolean isVRApplication(Context context, String packageName) {
        if (isValid() && context != null && packageName != null && !packageName.equals(BuildConfig.FLAVOR)) {
            return isVRApp(context, packageName);
        }
        return false;
    }

    public String getContactName(Context context, String num) {
        if (!isValid()) {
            return null;
        }
        if (!checkContext(context)) {
            Log.i(TAG, "Client is not vr");
            return null;
        } else if (!checkIsHwVrLauncher(context)) {
            Log.w(TAG, "getContactName is not supprot.");
            return null;
        } else if (-1 == checkPermission(context)) {
            Log.w(TAG, "aidl permission denied!");
            return null;
        } else {
            try {
                return this.mVRM.getContactName(num);
            } catch (RemoteException ex) {
                Log.w(TAG, "vr state query exception! ", ex);
                return null;
            }
        }
    }

    public void registerVRListener(Context context, IVRListener vrlistener) {
        if (isValid()) {
            if (!checkContext(context)) {
                Log.i(TAG, "Client is not vr");
            } else if (-1 == checkPermission(context)) {
                Log.w(TAG, "aidl permission denied!");
            } else {
                try {
                    this.mVRM.addVRListener(vrlistener);
                } catch (RemoteException e) {
                    Log.w(TAG, "add listener exception ", e);
                }
            }
        }
    }

    public void registerExpandListener(Context context, IVRListener vrlistener) {
        if (isValid()) {
            if (!checkContext(context)) {
                Log.i(TAG, "Client is not vr");
            } else if (-1 == checkPermission(context)) {
                Log.w(TAG, "aidl permission denied!");
            } else {
                try {
                    this.mVRM.registerExpandListener(vrlistener);
                } catch (RemoteException e) {
                    Log.w(TAG, "add listener exception ", e);
                }
            }
        }
    }

    public void unregisterVRListener(Context context, IVRListener vrlistener) {
        if (isValid()) {
            if (!checkContext(context)) {
                Log.i(TAG, "Client is not vr");
            } else if (-1 == checkPermission(context)) {
                Log.w(TAG, "aidl permission denied!");
            } else {
                try {
                    this.mVRM.deleteVRListener(vrlistener);
                } catch (RemoteException e) {
                    Log.w(TAG, "delete listener exception ", e);
                }
            }
        }
    }

    public void acceptInCall(Context context) {
        if (isValid()) {
            if (!checkContext(context)) {
                Log.i(TAG, "Client is not vr");
            } else if (!checkIsHwVrLauncher(context)) {
                Log.w(TAG, "acceptInCall is not supprot.");
            } else if (-1 == checkPermission(context)) {
                Log.w(TAG, "aidl permission denied!");
            } else {
                try {
                    this.mVRM.acceptInCall();
                } catch (RemoteException ex) {
                    Log.w(TAG, "acceptInCall request exception!", ex);
                }
            }
        }
    }

    public void endInCall(Context context) {
        if (isValid()) {
            if (!checkContext(context)) {
                Log.i(TAG, "Client is not vr");
            } else if (!checkIsHwVrLauncher(context)) {
                Log.w(TAG, "endInCall is not supprot.");
            } else if (-1 == checkPermission(context)) {
                Log.w(TAG, "aidl permission denied!");
            } else {
                try {
                    this.mVRM.endInCall();
                } catch (RemoteException ex) {
                    Log.w(TAG, "acceptInCall request exception!", ex);
                }
            }
        }
    }

    public int getHelmetBattery(Context context) {
        if (!isValid()) {
            return 0;
        }
        if (!checkContext(context)) {
            Log.i(TAG, "Client is not vr");
            return 0;
        } else if (-1 == checkPermission(context)) {
            Log.w(TAG, "aidl permission denied!");
            return 0;
        } else {
            try {
                return this.mVRM.getHelmetBattery();
            } catch (RemoteException e) {
                Log.w(TAG, "get Helmet battery exception ", e);
                return 0;
            }
        }
    }

    public int getHelmetBrightness(Context context) {
        if (!isValid()) {
            return 0;
        }
        if (!checkContext(context)) {
            Log.i(TAG, "Client is not vr");
            return 0;
        } else if (-1 == checkPermission(context)) {
            Log.w(TAG, "aidl permission denied!");
            return 0;
        } else {
            try {
                return this.mVRM.getHelmetBrightness();
            } catch (RemoteException e) {
                Log.w(TAG, "get Helmet brightness exception ", e);
                return 0;
            }
        }
    }

    public void setHelmetBrightness(Context context, int brightness) {
        if (isValid()) {
            if (!checkContext(context)) {
                Log.i(TAG, "Client is not vr");
            } else if (-1 == checkPermission(context)) {
                Log.w(TAG, "aidl permission denied!");
            } else {
                try {
                    this.mVRM.setHelmetBrightness(brightness);
                } catch (RemoteException e) {
                    Log.w(TAG, "set Helmet brightness exception ", e);
                }
            }
        }
    }

    private VRSystemServiceManager() {
        this.mIsVRDisplayConnected = false;
        this.mIsVirtualScreenMode = false;
        this.mIsVirtualScreenStarted = false;
        this.sVRLowPowerList = new HashSet<>();
        this.mNextVrVirtualDisplayId = 1000000;
        this.mDisplayparams = new int[3];
        this.mVrVirtualAppList = new ArrayList(32);
        this.mVrVirtualDisplayIdList = new ArrayList(16);
        this.mVRM = IVRSystemService.Stub.asInterface(ServiceManager.getService("vr_system"));
    }

    private boolean checkServiceValid() {
        this.mVRM = IVRSystemService.Stub.asInterface(ServiceManager.getService("vr_system"));
        IVRSystemService iVRSystemService = this.mVRM;
        if (iVRSystemService == null) {
            Log.w(TAG, "vr service is not alive");
            return false;
        }
        try {
            iVRSystemService.isVRmode();
            return true;
        } catch (RemoteException ex) {
            Log.w(TAG, "vr service exception, please check ", ex);
            return false;
        }
    }

    private boolean isValid() {
        return VR_SWITCH && checkServiceValid();
    }

    private boolean checkContext(Context context) {
        return context != null && isVRApp(context, context.getPackageName());
    }

    private boolean isVRApp(Context context, String packageName) {
        if (SYSTEMUI.equals(packageName) || VR_METADATA_VALUE.equals(getManifestMetadata(context, packageName, VR_METADATA_NAME))) {
            return true;
        }
        Log.w(TAG, "no vr app metadata vr_only");
        return false;
    }

    private String getManifestMetadata(Context context, String packageName, String name) {
        ApplicationInfo appinfo = null;
        try {
            appinfo = context.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo exception ", e);
        }
        if (appinfo == null || appinfo.metaData == null) {
            return null;
        }
        return appinfo.metaData.getString(name);
    }

    public void setVRDisplayConnected(boolean connected) {
        Instance.sInstance.mIsVRDisplayConnected = connected;
    }

    public boolean isVRDisplayConnected() {
        return Instance.sInstance.mIsVRDisplayConnected;
    }

    public void setVirtualScreenMode(boolean mode) {
        if (!mode && Instance.sInstance.mIsVirtualScreenMode) {
            this.mIsVirtualScreenStarted = false;
            HwDisplayManager.destroyAllVrDisplay();
        }
        Log.i(TAG, "set framework virtual screen mode to " + mode);
        Instance.sInstance.mIsVirtualScreenMode = mode;
    }

    public boolean isVirtualScreenMode() {
        return Instance.sInstance.mIsVirtualScreenMode;
    }

    public boolean isVRDynamicStack(int stackId) {
        return IS_VR_ENABLE && stackId >= 1100000000;
    }

    public void setVRDisplayID(int displayid, boolean vrmode) {
        if (IS_VR_ENABLE) {
            sDisplayID = displayid;
            sIsVrMode = vrmode;
        }
    }

    public boolean isVRDeviceConnected() {
        return sIsVrMode && IS_VR_ENABLE;
    }

    public void setTargetComponentName(ComponentName componentName) {
        if (componentName != null) {
            sTargetPackageName = componentName.getPackageName();
        } else {
            sTargetPackageName = null;
        }
    }

    public int getVRDisplayID(Context context) {
        DisplayManager displayManager;
        if (!IS_VR_ENABLE || context == null || !isVRDeviceConnected()) {
            return -1;
        }
        if (VR_INSTALL.equals(sTargetPackageName)) {
            setTargetComponentName(null);
            return -1;
        }
        String packageName = context.getPackageName();
        if (!(isVRDeviceConnected() || VR_SERVICE.equals(packageName)) || "android".equals(packageName) || "com.huawei.vrvirtualscreen".equals(packageName) || !isVRApp(context) || (displayManager = (DisplayManager) context.getSystemService("display")) == null) {
            return -1;
        }
        Display[] displays = displayManager.getDisplays();
        for (int i = 1; i < displays.length; i++) {
            Display display = displays[i];
            if (display != null) {
                int displayID = display.getDisplayId();
                DisplayInfo disInfo = new DisplayInfo();
                if (display.getDisplayInfo(disInfo) && (isVRDisplay(displayID, disInfo.getNaturalWidth(), disInfo.getNaturalHeight()) || sDisplayID == displayID)) {
                    setVRDisplayID(displayID, true);
                    return displayID;
                }
            }
        }
        setVRDisplayID(-1, false);
        return getVRDisplayID();
    }

    public int getVRDisplayID() {
        if (!IS_VR_ENABLE) {
            return -1;
        }
        return sDisplayID;
    }

    public boolean isValidVRDisplayId(int displayid) {
        return displayid != 0 && IS_VR_ENABLE && displayid != -1 && displayid == sDisplayID;
    }

    public boolean isVRDisplay(int displayid, int width, int height) {
        if (displayid == -1 || displayid == 0) {
            return false;
        }
        if (isWallexDisplay(width, height) || isHallidayDisplay(width, height)) {
            return true;
        }
        return false;
    }

    private boolean isWallexDisplay(int width, int height) {
        return (width == 2880 && height == 1600) || (width == 1600 && height == 2880);
    }

    private boolean isHallidayDisplay(int width, int height) {
        return (width == 3200 && height == 1600) || (width == 1600 && height == 3200);
    }

    public void addVRLowPowerAppList(String packageName) {
        if (packageName != null && !packageName.trim().equals(BuildConfig.FLAVOR) && !packageName.equals(SYSTEMUI) && !packageName.equals("com.android.packageinstaller") && !this.sVRLowPowerList.contains(packageName)) {
            this.sVRLowPowerList.add(packageName);
        }
    }

    public boolean isVRLowPowerApp(String packageName) {
        return this.sVRLowPowerList.contains(packageName);
    }

    private boolean isVRApp(Context context) {
        String packageName;
        if (context == null || (packageName = context.getPackageName()) == null) {
            return false;
        }
        if (SYSTEMUI.equals(packageName)) {
            return true;
        }
        String vrOnly = BuildConfig.FLAVOR;
        ApplicationInfo appinfo = null;
        try {
            appinfo = context.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo exception ", e);
        }
        if (!(appinfo == null || appinfo.metaData == null)) {
            vrOnly = appinfo.metaData.getString(VR_METADATA_NAME);
        }
        if (VR_METADATA_VALUE.equals(vrOnly)) {
            return true;
        }
        Log.w(TAG, "no vr app metadata " + vrOnly);
        return false;
    }

    public void setVRDispalyInfo(DisplayInfo displayInfo, int displayID) {
        if (displayInfo == null) {
            Log.w(TAG, "displayInfo is null in setVRDispalyInfo");
        } else if (isVRDisplay(displayID, displayInfo.getNaturalWidth(), displayInfo.getNaturalHeight())) {
            Log.i(TAG, "handleDisplayDeviceAddedLocked in vr mode");
            int[] iArr = this.mDisplayparams;
            iArr[0] = displayID;
            iArr[1] = displayInfo.getNaturalWidth();
            this.mDisplayparams[2] = displayInfo.getNaturalHeight();
            setVRDisplayID(displayID, true);
            setVRDisplayConnected(true);
        }
    }

    public int mirrorVRDisplayIfNeed(IBinder displayToken) {
        if (displayToken == null) {
            Log.w(TAG, "displayToken is null in mirrorVRDisplayIfNeed");
            return -1;
        } else if (!isVRDisplayConnected()) {
            return -1;
        } else {
            int vrLayerStackId = getVRDisplayID();
            if (isVRMode() && vrLayerStackId > 0) {
                Log.i(TAG, "performTraversalInTransactionLocked setDisplayLayerStack VR layer stack id:" + vrLayerStackId);
                SurfaceControl.setDisplayLayerStack(displayToken, vrLayerStackId);
            }
            return vrLayerStackId;
        }
    }

    public HashSet<String> getLowerAppList() {
        return this.sVRLowPowerList;
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
        } else if (packageName.equals("com.android.packageinstaller") || packageManager.checkPermission(VRSERVICE_PERMISSION, packageName) == 0 || packageManager.checkPermission(VR_PERMISSION, packageName) == 0) {
            return 0;
        } else {
            return -1;
        }
    }

    private boolean checkIsHwVrLauncher(Context context) {
        if (context == null) {
            Log.e(TAG, "context is null.");
            return false;
        }
        String packageName = context.getPackageName();
        if (packageName == null) {
            Log.e(TAG, "packageName is null in checkIsHwVrLauncher.");
            return false;
        } else if (VR_LAUNCHER.equals(packageName)) {
            return true;
        } else {
            return false;
        }
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

    private boolean processSleepInVrMode(int reason) {
        return 4 == reason || 2 == reason;
    }

    private boolean processWakeupInVrMode(String details) {
        if (WAKEUP_DETAIL_POWER.equals(details) || WAKEUP_DETAIL_HEADSET.equals(details) || WAKEUP_DETAIL_BLUETOOTH.equals(details)) {
            return true;
        }
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
        return Pattern.compile(VR_DISPLAY_PATTERN).matcher(displayName).find();
    }

    public boolean isVrVirtualDisplay(int displayId) {
        return displayId >= 1000000;
    }

    public void addVrVirtualDisplayId(int displayId) {
        this.mVrVirtualDisplayIdList.add(Integer.valueOf(displayId));
    }

    public void removeVrVirtualDisplayId(int displayId) {
        this.mVrVirtualDisplayIdList.remove(Integer.valueOf(displayId));
    }

    public void clearVrVirtualDisplay() {
        this.mVrVirtualDisplayIdList.clear();
    }

    public int getTopVrVirtualDisplayId() {
        if (this.mVrVirtualDisplayIdList.isEmpty()) {
            return 0;
        }
        List<Integer> list = this.mVrVirtualDisplayIdList;
        return list.get(list.size() - 1).intValue();
    }

    public List<Integer> getAllVrVirtualDisplayId() {
        return this.mVrVirtualDisplayIdList;
    }

    public boolean isVrCaredDisplay(int displayId) {
        return isVRDeviceConnected() && (isValidVRDisplayId(displayId) || isVrVirtualDisplay(displayId));
    }

    public void recordVirtualApp(String packageName) {
        if (packageName != null && !this.mVrVirtualAppList.contains(packageName)) {
            if ("com.huawei.android.launcher".equals(packageName)) {
                Log.i(TAG, "do not record android launcher into virtual apps list.");
            } else {
                this.mVrVirtualAppList.add(packageName);
            }
        }
    }

    public boolean isVirtualAppRecorded(String packageName) {
        return this.mVrVirtualAppList.contains(packageName);
    }

    public void removeRecordedVirtualApp(String packageName) {
        this.mVrVirtualAppList.remove(packageName);
    }

    public void clearRecordedVirtualAppList() {
        this.mVrVirtualAppList.clear();
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

    private boolean isStartingNormalAppInVr(Context context, String launchPackage) {
        return !isVirtualScreenMode() && !isVRApplication(context, launchPackage);
    }

    private boolean isStartingLauncherByVirtualApp(String callingPackage, String launchPackage) {
        return isVirtualScreenMode() && isVirtualAppRecorded(callingPackage) && !"com.huawei.vrvirtualscreen".equals(callingPackage) && "com.huawei.android.launcher".equals(launchPackage);
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
            intent.putExtra("displayId", virtualDisplayId);
            intent.setComponent(new ComponentName("com.huawei.vrvirtualscreen", "com.huawei.vrvirtualscreen.appdisplay.AppDisplayActivity"));
            ActivityOptions activityOptions = ActivityOptions.makeBasic();
            activityOptions.setLaunchDisplayId(virtualDisplayId);
            context.startActivity(intent, activityOptions.toBundle());
        } catch (ActivityNotFoundException exception) {
            Log.e(TAG, "error! virtual launcher com.huawei.vrvirtualscreen/com.huawei.vrvirtualscreen.appdisplay.AppDisplayActivity not found! " + exception.getMessage());
        }
    }

    private void updateVrLowPowerAppList(Context context, String launchPackage) {
        if (isVRApplication(context, launchPackage)) {
            addVRLowPowerAppList(launchPackage);
        }
    }

    private void updateVrVirtualScreenMode(Intent intent, String callingPackage, String launchPackage) {
        if ("com.huawei.vrvirtualscreen".equals(launchPackage)) {
            setVirtualScreenMode(true);
        }
        if (VR_LAUNCHER.equals(launchPackage) && "com.huawei.vrvirtualscreen".equals(callingPackage) && intent.getIntExtra(EXTRA_KEY_TARGET_DISPLAY, -1) == -1) {
            setVirtualScreenMode(false);
        }
    }

    public int getVrPreferredDisplayId(String launchedFromPkg, String startingPkg, int preferredDisplayId) {
        if (isVRDeviceConnected() && !isVirtualScreenMode()) {
            return getVRDisplayID();
        }
        if ("com.huawei.vrvirtualscreen".equals(launchedFromPkg)) {
            this.mIsVirtualScreenStarted = true;
        }
        if (this.mIsVirtualScreenStarted || !"com.huawei.vrvirtualscreen".equals(startingPkg)) {
            return getVirtualPreferredId(launchedFromPkg, startingPkg, preferredDisplayId);
        }
        return getVRDisplayID();
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
        if ("com.huawei.android.launcher".equals(startingPkg)) {
            Log.i(TAG, "start android launcher on display 0");
            newPreferredDisplayId = 0;
        } else if ("com.huawei.vrvirtualscreen".equals(launchedFromPkg) || "android".equals(launchedFromPkg)) {
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

    public int[] getVrDisplayParams() {
        return this.mDisplayparams;
    }
}
