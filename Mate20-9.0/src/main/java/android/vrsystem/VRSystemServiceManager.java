package android.vrsystem;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.cover.CoverManager;
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
import java.util.HashSet;

public class VRSystemServiceManager implements IVRSystemServiceManager {
    private static final boolean IS_VR_ENABLE = SystemProperties.getBoolean("ro.vr.mode", false);
    private static final String PACKAGE_INSTALL = "com.android.packageinstaller";
    private static final String PACKAGE_INSTALLER = "com.android.packageinstaller";
    private static final int PERMISSION_ERROR = -1;
    private static final int PERMISSION_SUCCESS = 0;
    private static final String SYSTEMUI = "com.android.systemui";
    private static final String TAG = "VRSystemServiceManager";
    private static final String VRSERVICE_PERMISSION = "com.huawei.vrservice.permission.VR";
    private static final String VR_INSTALL = "com.huawei.vrinstaller";
    private static final String VR_LAUNCHER = "com.huawei.vrlauncherx";
    private static final String VR_METADATA_NAME = "com.huawei.android.vr.application.mode";
    private static final String VR_METADATA_VALUE = "vr_only";
    private static final String VR_PERMISSION = "com.huawei.android.permission.VR";
    private static final int VR_PROCESS_ARGS = 3;
    private static final String VR_SERVICE = "com.huawei.vrservice";
    private static final boolean VR_SWITCH = SystemProperties.getBoolean("ro.vr.surport", false);
    private static int sDisplayID = -1;
    private static boolean sIsVrMode = false;
    private static String sTargetPackageName;
    private boolean mIsVRDisplayConnected;
    private boolean mIsVirtualScreenMode;
    private IVRSystemService mVRM;
    private HashSet<String> sVRLowPowerList;

    private static class Instance {
        /* access modifiers changed from: private */
        public static VRSystemServiceManager sInstance = new VRSystemServiceManager();

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
        boolean isVR = false;
        try {
            isVR = this.mVRM.isVRmode();
        } catch (RemoteException ex) {
            Log.w(TAG, "vr state query exception! ", ex);
        }
        return isVR;
    }

    public boolean isVRApplication(Context context, String packageName) {
        if (isValid() && context != null && packageName != null && !packageName.equals("")) {
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
            String name = null;
            try {
                name = this.mVRM.getContactName(num);
            } catch (RemoteException ex) {
                Log.w(TAG, "vr state query exception! ", ex);
            }
            return name;
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
            int battery = 0;
            try {
                battery = this.mVRM.getHelmetBattery();
            } catch (RemoteException e) {
                Log.w(TAG, "get Helmet battery exception ", e);
            }
            return battery;
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
            int brightness = 0;
            try {
                brightness = this.mVRM.getHelmetBrightness();
            } catch (RemoteException e) {
                Log.w(TAG, "get Helmet brightness exception ", e);
            }
            return brightness;
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
        this.sVRLowPowerList = new HashSet<>();
        this.mVRM = IVRSystemService.Stub.asInterface(ServiceManager.getService("vr_system"));
    }

    private boolean checkServiceValid() {
        this.mVRM = IVRSystemService.Stub.asInterface(ServiceManager.getService("vr_system"));
        if (this.mVRM == null) {
            Log.w(TAG, "vr service is not alive");
            return false;
        }
        boolean valid = false;
        try {
            this.mVRM.isVRmode();
            valid = true;
        } catch (RemoteException ex) {
            Log.w(TAG, "vr service exception, please check ", ex);
        }
        return valid;
    }

    private boolean isValid() {
        return VR_SWITCH && checkServiceValid();
    }

    private boolean checkContext(Context context) {
        return context != null && isVRApp(context, context.getPackageName());
    }

    private boolean isVRApp(Context context, String packageName) {
        if ("com.android.systemui".equals(packageName)) {
            return true;
        }
        String vrOnly = getManifestMetadata(context, packageName, VR_METADATA_NAME);
        if (VR_METADATA_VALUE.equals(vrOnly)) {
            return true;
        }
        Log.w(TAG, "no vr app metadata " + vrOnly);
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
        if (!IS_VR_ENABLE || context == null || !isVRDeviceConnected()) {
            return -1;
        }
        if (VR_INSTALL.equals(sTargetPackageName)) {
            setTargetComponentName(null);
            return -1;
        }
        String packageName = context.getPackageName();
        if ((!isVRDeviceConnected() && !VR_SERVICE.equals(packageName)) || CoverManager.HALL_STATE_RECEIVER_DEFINE.equals(packageName) || "com.huawei.vrvirtualscreen".equals(packageName)) {
            return -1;
        }
        if (!isVRApp(context)) {
            setVRDisplayID(-1, false);
            return -1;
        }
        DisplayManager displayManager = (DisplayManager) context.getSystemService("display");
        if (displayManager == null) {
            return -1;
        }
        Display[] displays = displayManager.getDisplays();
        int i = 1;
        while (i < displays.length) {
            Display display = displays[i];
            DisplayInfo disInfo = new DisplayInfo();
            if (display == null || !display.getDisplayInfo(disInfo) || !isVRDisplay(display.getDisplayId(), disInfo.getNaturalWidth(), disInfo.getNaturalHeight())) {
                i++;
            } else {
                int displayID = display.getDisplayId();
                setVRDisplayID(displayID, true);
                return displayID;
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
        if ((width == 2880 && height == 1600) || (width == 1600 && height == 2880)) {
            return true;
        }
        return false;
    }

    public void addVRLowPowerAppList(String packageName) {
        if (packageName != null && !packageName.trim().equals("") && !packageName.equals("com.android.systemui") && !packageName.equals("com.android.packageinstaller") && !this.sVRLowPowerList.contains(packageName)) {
            this.sVRLowPowerList.add(packageName);
        }
    }

    public boolean isVRLowPowerApp(String packageName) {
        return this.sVRLowPowerList.contains(packageName);
    }

    private boolean isVRApp(Context context) {
        if (context == null) {
            return false;
        }
        String packageName = context.getPackageName();
        if (packageName == null) {
            return false;
        }
        if ("com.android.systemui".equals(packageName)) {
            return true;
        }
        String vrOnly = "";
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
            return;
        }
        if (isVRDisplay(displayID, displayInfo.getNaturalWidth(), displayInfo.getNaturalHeight())) {
            Log.i(TAG, "handleDisplayDeviceAddedLocked in vr mode");
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
            if (!isVRMode() || vrLayerStackId <= 0) {
                SurfaceControl.setDisplayLayerStack(displayToken, 0);
            } else {
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
        boolean z = true;
        if (!isVRDeviceConnected()) {
            return true;
        }
        if ((!isVirtualScreenMode() || displayId == getVRDisplayID()) && (isVirtualScreenMode() || displayId != getVRDisplayID())) {
            z = false;
        }
        return z;
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
}
