package android.util;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.freeform.HwFreeFormManager;
import android.freeform.HwFreeFormUtils;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.text.TextUtils;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.server.LocalServices;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SplitNotificationUtils {
    private static final String DESKTOP_NAME = "com.huawei.android.launcher";
    public static final String FLOATING_WINDOW = "floating_window";
    public static final String FLOATING_WINDOW_NOTIFICATION = "floating_window_notification";
    public static final int FLOAT_IMS_LIST = 3;
    public static final int HTFF = 2;
    private static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final int IS_TOP_FULL_SCREEN_TOKEN = 206;
    public static final int NMS = 1;
    public static final int SPLIT_IMS_LIST = 2;
    public static final int SPLIT_VIDEO_LIST = 1;
    public static final String SPLIT_WINDOW = "split_window";
    private static int STATUS_BAR_HEIGHT = -1;
    private static final String TAG = "SplitNotificationUtils";
    private static List<String> floatingWindowImsListPkgNames = new ArrayList();
    private static SplitNotificationUtils mInstance;
    private static int mlnsets = 0;
    private static List<String> oneSplitScreenImsListPkgNames = new ArrayList();
    private static List<String> oneSplitScreenVideoListPkgNames = new ArrayList();
    private Context mContext;

    private SplitNotificationUtils(Context context) {
        this.mContext = context;
    }

    public static synchronized SplitNotificationUtils getInstance(Context context) {
        SplitNotificationUtils splitNotificationUtils;
        synchronized (SplitNotificationUtils.class) {
            if (mInstance == null) {
                mInstance = new SplitNotificationUtils(context);
            }
            splitNotificationUtils = mInstance;
        }
        return splitNotificationUtils;
    }

    public void addPkgName(String pkgName, int type) {
        if (type == 1) {
            oneSplitScreenVideoListPkgNames.add(pkgName);
        } else if (type == 2) {
            oneSplitScreenImsListPkgNames.add(pkgName);
        } else if (type == 3) {
            floatingWindowImsListPkgNames.add(pkgName);
        }
    }

    public List<String> getListPkgName(int type) {
        if (type == 1) {
            return oneSplitScreenVideoListPkgNames;
        }
        if (type == 2) {
            return oneSplitScreenImsListPkgNames;
        }
        if (type == 3) {
            return floatingWindowImsListPkgNames;
        }
        return new ArrayList();
    }

    public String getNotificationType(String imsPkgName, int caller) {
        ActivityManager.RunningTaskInfo runningTaskInfo;
        if (((TextUtils.isEmpty(imsPkgName) || !isSupportSplitScreen(imsPkgName)) && caller == 1) || isKeyguardLockedOrFreeformVisible() || (runningTaskInfo = getRunningTaskInfo()) == null || runningTaskInfo.configuration.windowConfiguration.getWindowingMode() != 1) {
            return "";
        }
        String dockableTopPkgName = "";
        try {
            if (WindowManagerGlobal.getWindowManagerService().getDockedStackSide() == -1 && !ActivityManager.getService().isInLockTaskMode()) {
                dockableTopPkgName = runningTaskInfo.topActivity.getPackageName();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "get dockside failed by RemoteException");
        }
        if (TextUtils.isEmpty(dockableTopPkgName)) {
            return "";
        }
        if (!HwFreeFormUtils.isFreeFormEnable() || caller != 1 || !floatingWindowImsListPkgNames.contains(imsPkgName) || !isTopFullscreen()) {
            if (shouldSkipTriggerFloatingWindow(imsPkgName, dockableTopPkgName, caller) || runningTaskInfo.supportsSplitScreenMultiWindow) {
                if (HwFreeFormUtils.isFreeFormEnable() || !oneSplitScreenImsListPkgNames.contains(imsPkgName.toLowerCase(Locale.getDefault())) || !oneSplitScreenVideoListPkgNames.contains(dockableTopPkgName.toLowerCase(Locale.getDefault())) || !isSupportSplitScreen(dockableTopPkgName)) {
                    return "";
                }
                return SPLIT_WINDOW;
            } else if (caller == 2) {
                HwFreeFormUtils.log(TAG, "calling from HwTripleFingersFreeForm");
                Context context = this.mContext;
                Flog.bdReport(context, 10064, "{ pkg:" + dockableTopPkgName + "}");
                return FLOATING_WINDOW;
            }
            return "";
        } else if (runningTaskInfo.topActivity.getPackageName().equals(imsPkgName) || shouldSkipTriggerFloatingWindow(imsPkgName, dockableTopPkgName, caller)) {
            return "";
        } else {
            HwFreeFormUtils.log(TAG, "calling from NotificationManagerService");
            return FLOATING_WINDOW_NOTIFICATION;
        }
    }

    public boolean shouldSkipTriggerFreeform(String pkgName, int userId) {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            HwFreeFormUtils.log(TAG, "It is power saving mode");
            return true;
        } else if (!TextUtils.isEmpty(Settings.Global.getString(this.mContext.getContentResolver(), SettingsEx.System.SINGLE_HAND_MODE))) {
            HwFreeFormUtils.log(TAG, "It is single hand mode");
            return true;
        } else {
            int simpleuiVal = Settings.System.getIntForUser(this.mContext.getContentResolver(), "simpleui_mode", 0, ActivityManager.getCurrentUser());
            if (simpleuiVal == 2 || simpleuiVal == 5) {
                HwFreeFormUtils.log(TAG, "It is simple mode simpleuiVal:" + simpleuiVal);
                return true;
            }
            if (!IS_HW_MULTIWINDOW_SUPPORTED) {
                ActivityManager.RunningTaskInfo runningTaskInfo = getRunningTaskInfo();
                String dockableTopPkgName = "";
                try {
                    if (WindowManagerGlobal.getWindowManagerService().getDockedStackSide() == -1 && !ActivityManager.getService().isInLockTaskMode() && runningTaskInfo != null && runningTaskInfo.topActivity != null) {
                        dockableTopPkgName = runningTaskInfo.topActivity.getPackageName();
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "get dockside failed by RemoteException");
                }
                if (isAppInLockList(pkgName, dockableTopPkgName, userId)) {
                    return true;
                }
            }
            return false;
        }
    }

    private ActivityManager.RunningTaskInfo getRunningTaskInfo() {
        ActivityManager am = (ActivityManager) this.mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = null;
        if (am != null) {
            tasks = am.getRunningTasks(1);
        }
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0);
    }

    private boolean isKeyguardLockedOrFreeformVisible() {
        KeyguardManager km = (KeyguardManager) this.mContext.getSystemService(Context.KEYGUARD_SERVICE);
        if (km != null && km.isKeyguardLocked()) {
            return true;
        }
        ActivityManager.StackInfo freeformStackInfo = null;
        ActivityManager.StackInfo pipStackInfo = null;
        try {
            freeformStackInfo = ActivityTaskManager.getService().getStackInfo(5, 1);
            pipStackInfo = ActivityTaskManager.getService().getStackInfo(2, 1);
        } catch (RemoteException e) {
        }
        if ((freeformStackInfo == null || !freeformStackInfo.visible) && (pipStackInfo == null || !pipStackInfo.visible)) {
            return false;
        }
        return true;
    }

    private boolean shouldSkipTriggerFloatingWindow(String imsPkgName, String dockableTopPkgName, int caller) {
        int simpleuiVal;
        if (!HwFreeFormUtils.isFreeFormEnable()) {
            return true;
        }
        if (caller == 2 && isSupportSplitScreen(dockableTopPkgName)) {
            return true;
        }
        if ("com.huawei.android.launcher".equals(dockableTopPkgName)) {
            if (caller == 2) {
                HwFreeFormManager.getInstance(this.mContext).showUnsupportedToast();
            }
            return true;
        } else if (!SystemProperties.getBoolean("sys.super_power_save", false) && TextUtils.isEmpty(Settings.Global.getString(this.mContext.getContentResolver(), SettingsEx.System.SINGLE_HAND_MODE)) && (simpleuiVal = Settings.System.getIntForUser(this.mContext.getContentResolver(), "simpleui_mode", 0, ActivityManager.getCurrentUser())) != 2 && simpleuiVal != 5 && !isAppInLockList(imsPkgName, dockableTopPkgName, -2)) {
            return false;
        } else {
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x006e, code lost:
        if (r0.contains(";" + r7 + ";") != false) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0070, code lost:
        android.freeform.HwFreeFormUtils.log(android.util.SplitNotificationUtils.TAG, "packageName userId:" + r8 + " dockableTopPkgName:" + r7 + " list:" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0098, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0050, code lost:
        if (r0.contains(";" + r6 + ";") == false) goto L_0x0052;
     */
    private boolean isAppInLockList(String imsPgkName, String dockableTopPkgName, int userId) {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "app_lock_func_status", 0) == 0) {
            return false;
        }
        String appLockList = ";" + Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "app_lock_list", userId);
        if (TextUtils.isEmpty(appLockList)) {
            return false;
        }
        if (!TextUtils.isEmpty(imsPgkName)) {
        }
        if (!TextUtils.isEmpty(dockableTopPkgName)) {
        }
        return false;
    }

    private boolean isSupportSplitScreen(String packageName) {
        ComponentName mainComponentName;
        int userId = ActivityManager.getCurrentUser();
        Intent mainIntent = getLaunchIntentForPackageAsUser(packageName, userId);
        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        if (!(mainIntent == null || (mainComponentName = mainIntent.getComponent()) == null)) {
            try {
                ActivityInfo activityInfo = packageManagerInternal.getActivityInfo(mainComponentName, 0, Binder.getCallingPid(), userId);
                if (activityInfo != null) {
                    return isResizeableMode(activityInfo.resizeMode);
                }
            } catch (RuntimeException e) {
                Slog.e(TAG, "get activityInfo failed by ComponentNameException");
            } catch (Exception e2) {
                Slog.e(TAG, "get activityInfo failed by ComponentNameException");
            }
        }
        return false;
    }

    private boolean isResizeableMode(int mode) {
        return mode == 2 || mode == 4 || mode == 1;
    }

    private Intent getLaunchIntentForPackageAsUser(String packageName, int userId) {
        PackageManager pm = this.mContext.getPackageManager();
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = pm.queryIntentActivitiesAsUser(intentToResolve, 0, userId);
        if (ris == null || ris.size() <= 0) {
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = pm.queryIntentActivitiesAsUser(intentToResolve, 0, userId);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(268435456);
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return intent;
    }

    private boolean isTopFullscreen() {
        int ret = 0;
        try {
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            if (wm == null) {
                return false;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            wm.asBinder().transact(206, data, reply, 0);
            ret = reply.readInt();
            if (ret > 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "isTopIsFullscreen", e);
        }
    }
}
