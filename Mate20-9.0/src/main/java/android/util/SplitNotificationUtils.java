package android.util;

import android.app.ActivityManager;
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
import android.graphics.Rect;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.health.UidHealthStats;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.rms.AppAssociate;
import android.text.TextUtils;
import android.view.IWindowManager;
import android.view.WindowManager;
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
    private static final int IS_TOP_FULL_SCREEN_TOKEN = 206;
    public static final int NMS = 1;
    public static final int SHTFF = 0;
    public static final int SPLIT_IMS_LIST = 2;
    public static final int SPLIT_VIDEO_LIST = 1;
    public static final String SPLIT_WINDOW = "split_window";
    private static int STATUS_BAR_HEIGHT = -1;
    private static final String TAG = "SplitNotificationUtils";
    private static List<String> floatingWindowImsListPkgNames = new ArrayList();
    private static SplitNotificationUtils mInstance;
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
        if ((TextUtils.isEmpty(imsPkgName) || !isSupportSplitScreen(imsPkgName)) && caller == 1) {
            return "";
        }
        if (isKeyguardLockedOrFreeformVisible()) {
            return "";
        }
        ActivityManager.RunningTaskInfo runningTaskInfo = getRunningTaskInfo();
        if (runningTaskInfo == null || runningTaskInfo.configuration.windowConfiguration.getWindowingMode() != 1) {
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
        if (!HwFreeFormUtils.isFreeFormEnable() || caller != 1 || !floatingWindowImsListPkgNames.contains(imsPkgName) || !CheckCurrentIsFullScreen().booleanValue()) {
            if (caller != 2 || !CheckCurrentIsFullScreen().booleanValue() || !isTopFullscreen()) {
                if (shouldSkipTriggerFloatingWindow(imsPkgName, dockableTopPkgName, caller) || runningTaskInfo.supportsSplitScreenMultiWindow) {
                    if (!HwFreeFormUtils.isFreeFormEnable() && oneSplitScreenImsListPkgNames.contains(imsPkgName.toLowerCase(Locale.getDefault())) && oneSplitScreenVideoListPkgNames.contains(dockableTopPkgName.toLowerCase(Locale.getDefault())) && isSupportSplitScreen(dockableTopPkgName)) {
                        return SPLIT_WINDOW;
                    }
                } else if (caller == 2) {
                    HwFreeFormUtils.log(TAG, "calling from HwTripleFingersFreeForm");
                    Context context = this.mContext;
                    Flog.bdReport(context, (int) UidHealthStats.MEASUREMENT_CPU_POWER_MAMS, "{ pkg:" + dockableTopPkgName + "}");
                    return FLOATING_WINDOW;
                }
                return "";
            } else if (shouldSkipTriggerFloatingWindow(imsPkgName, dockableTopPkgName, 0)) {
                return "";
            } else {
                return FLOATING_WINDOW;
            }
        } else if (shouldSkipTriggerFloatingWindow(imsPkgName, dockableTopPkgName, caller)) {
            return "";
        } else {
            HwFreeFormUtils.log(TAG, "calling from NotificationManagerService");
            return FLOATING_WINDOW_NOTIFICATION;
        }
    }

    private Boolean CheckCurrentIsFullScreen() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService(AppAssociate.ASSOC_WINDOW)).getDefaultDisplay().getRealMetrics(metrics);
        int ScreenWidth = metrics.widthPixels;
        int ScreenHeight = metrics.heightPixels;
        Rect currentRect = null;
        try {
            currentRect = WindowManagerGlobal.getWindowManagerService().getFocuseWindowVisibleFrame();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (currentRect != null) {
            int CurrentTopWindowHeight = currentRect.bottom - currentRect.top;
            int CurrentTopWindowWidth = currentRect.right - currentRect.left;
            if (CurrentTopWindowHeight == ScreenHeight && CurrentTopWindowWidth == ScreenWidth) {
                return true;
            }
            Rect RepairedRect = RepairCurrentRect(currentRect, ScreenWidth, ScreenHeight);
            if (RepairedRect != null) {
                Slog.d(TAG, "RepairedRect is " + RepairedRect);
                int RepairedRectHeight = RepairedRect.bottom - RepairedRect.top;
                int RepairedRectWidth = RepairedRect.right - RepairedRect.left;
                if (RepairedRectHeight == ScreenHeight && RepairedRectWidth == ScreenWidth) {
                    return true;
                }
            } else {
                Slog.d(TAG, "RepairedRect is null");
            }
        } else {
            HwFreeFormUtils.log(TAG, "SplitNotificationUtils get the currentRect which is null");
        }
        return false;
    }

    private ActivityManager.RunningTaskInfo getRunningTaskInfo() {
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
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
        KeyguardManager km = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (km != null && km.isKeyguardLocked()) {
            return true;
        }
        ActivityManager.StackInfo freeformStackInfo = null;
        ActivityManager.StackInfo pipStackInfo = null;
        try {
            freeformStackInfo = ActivityManager.getService().getStackInfo(5, 1);
            pipStackInfo = ActivityManager.getService().getStackInfo(2, 1);
        } catch (RemoteException e) {
        }
        if ((freeformStackInfo == null || !freeformStackInfo.visible) && (pipStackInfo == null || !pipStackInfo.visible)) {
            return false;
        }
        return true;
    }

    private boolean shouldSkipTriggerFloatingWindow(String imsPkgName, String dockableTopPkgName, int caller) {
        if (!HwFreeFormUtils.isFreeFormEnable()) {
            return true;
        }
        if (caller == 2 && isSupportSplitScreen(dockableTopPkgName)) {
            return true;
        }
        if (DESKTOP_NAME.equals(dockableTopPkgName)) {
            if (caller == 2 || caller == 0) {
                HwFreeFormManager.getInstance(this.mContext).showUnsupportedToast();
            }
            return true;
        } else if (!SystemProperties.getBoolean("sys.super_power_save", false) && TextUtils.isEmpty(Settings.Global.getString(this.mContext.getContentResolver(), SettingsEx.System.SINGLE_HAND_MODE)) && Settings.System.getIntForUser(this.mContext.getContentResolver(), "simpleui_mode", 0, ActivityManager.getCurrentUser()) != 2 && !isAppInLockList(imsPkgName, dockableTopPkgName)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isAppInLockList(String imsPgkName, String dockableTopPkgName) {
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "app_lock_func_status", 0, -2) == 0) {
            return false;
        }
        String appLockList = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "app_lock_list", -2);
        if (TextUtils.isEmpty(appLockList)) {
            return false;
        }
        if ((TextUtils.isEmpty(imsPgkName) || !appLockList.contains(imsPgkName)) && !appLockList.contains(dockableTopPkgName)) {
            return false;
        }
        return true;
    }

    private boolean isSupportSplitScreen(String packageName) {
        int userId = ActivityManager.getCurrentUser();
        Intent mainIntent = getLaunchIntentForPackageAsUser(packageName, userId);
        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        if (mainIntent != null) {
            ComponentName mainComponentName = mainIntent.getComponent();
            if (mainComponentName != null) {
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
        }
        return false;
    }

    private boolean isResizeableMode(int mode) {
        return mode == 2 || mode == 4 || mode == 1;
    }

    private Intent getLaunchIntentForPackageAsUser(String packageName, int userId) {
        PackageManager pm = this.mContext.getPackageManager();
        Intent intentToResolve = new Intent("android.intent.action.MAIN");
        intentToResolve.addCategory("android.intent.category.INFO");
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = pm.queryIntentActivitiesAsUser(intentToResolve, 0, userId);
        if (ris == null || ris.size() <= 0) {
            intentToResolve.removeCategory("android.intent.category.INFO");
            intentToResolve.addCategory("android.intent.category.LAUNCHER");
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

    private Rect RepairCurrentRect(Rect CurrentWindowRect, int ScreenWidth, int ScreenHeight) {
        STATUS_BAR_HEIGHT = this.mContext.getResources().getDimensionPixelSize(17105318);
        if (STATUS_BAR_HEIGHT > 0) {
            Slog.d(TAG, "StatusBarHeight is " + STATUS_BAR_HEIGHT);
        } else {
            Slog.d(TAG, "StatusBarHeight is zero.");
        }
        if (CurrentWindowRect != null) {
            if (CurrentWindowRect.top != 0) {
                CurrentWindowRect.top -= STATUS_BAR_HEIGHT;
            }
            if (CurrentWindowRect.bottom != ScreenHeight) {
                CurrentWindowRect.bottom += STATUS_BAR_HEIGHT;
            }
            if (CurrentWindowRect.left != 0) {
                CurrentWindowRect.left -= STATUS_BAR_HEIGHT;
            }
            if (CurrentWindowRect.right != ScreenWidth) {
                CurrentWindowRect.right += STATUS_BAR_HEIGHT;
            }
        }
        return CurrentWindowRect;
    }

    private boolean isTopFullscreen() {
        boolean z = false;
        int ret = 0;
        try {
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService(AppAssociate.ASSOC_WINDOW));
            if (wm == null) {
                return false;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            wm.asBinder().transact(206, data, reply, 0);
            ret = reply.readInt();
            if (ret > 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "isTopIsFullscreen", e);
        }
    }
}
