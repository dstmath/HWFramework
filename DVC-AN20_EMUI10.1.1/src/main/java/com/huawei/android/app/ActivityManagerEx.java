package com.huawei.android.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.HardwareBuffer;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.HwSysResManager;
import android.util.Log;
import com.huawei.android.os.HwTransCodeEx;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

public class ActivityManagerEx {
    public static final String ANDROID_ACTIVITY_WINDOWING_MODE = "android.activity.windowingMode";
    private static final boolean HWMULTIWIN_ENABLED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final String HWRESOLVER_PACKAGENAME = "com.huawei.android.internal.app";
    public static final String HW_MULTIWINDOW_FREEFORM_FLOATIME = "float_ime_state";
    public static final int HW_MULTI_WINDOWING_MODE_FREEFORM = 102;
    public static final int HW_MULTI_WINDOWING_MODE_MAGIC = 103;
    public static final int HW_MULTI_WINDOWING_MODE_PRIMARY = 100;
    public static final int HW_MULTI_WINDOWING_MODE_SECONDARY = 101;
    public static final String HW_MUTILWINDOW_BLACKLIST_APP = "blacklist";
    public static final String HW_MUTILWINDOW_RECOMLIST_APP = "recomlist";
    public static final String HW_MUTILWINDOW_WHITELIST_APP = "whitelist";
    public static final String HW_SPLIT_SCREEN_PRIMARY_BOUNDS = "primaryBounds";
    public static final int HW_SPLIT_SCREEN_PRIMARY_LEFT = 1;
    public static final String HW_SPLIT_SCREEN_PRIMARY_POSITION = "primaryPosition";
    public static final int HW_SPLIT_SCREEN_PRIMARY_TOP = 0;
    public static final int HW_SPLIT_SCREEN_RATIO_DEFAULT = 0;
    public static final int HW_SPLIT_SCREEN_RATIO_PRAIMARY_LESS_THAN_DEFAULT = 1;
    public static final int HW_SPLIT_SCREEN_RATIO_PRAIMARY_MORE_THAN_DEFAULT = 2;
    public static final String HW_SPLIT_SCREEN_SECONDARY_BOUNDS = "secondaryBounds";
    public static final int MOVE_TASK_TO_BACK = 0;
    public static final int MOVE_TASK_TO_FRONT = 1;
    public static final int RECENT_IGNORE_HOME_AND_RECENTS_STACK_TASKS = 0;
    public static final int RECENT_INCLUDE_PROFILES = 0;
    public static final int RECENT_INGORE_DOCKED_STACK_TOP_TASK = 0;
    public static final int RECENT_INGORE_PINNED_STACK_TASKS = 0;
    public static final int REMOVE_TASK = 2;
    @HwSystemApi
    public static final int START_DELIVERED_TO_TOP = 3;
    @HwSystemApi
    public static final int START_RETURN_INTENT_TO_CALLER = 1;
    @HwSystemApi
    public static final int START_SUCCESS = 0;
    @HwSystemApi
    public static final int START_TASK_TO_FRONT = 2;
    private static final String TAG = "ActivityManagerEx";
    public static final int WINDOWING_MODE_FULLSCREEN = 1;

    public static class RunningAppProcessInfoEx {
        public static int FLAG_CANT_SAVE_STATE = 1;
        public static int FLAG_PERSISTENT = 2;
        public static int IMPORTANCE_CANT_SAVE_STATE = 350;
    }

    @Deprecated
    public static boolean isClonedProcess(int pid) {
        return false;
    }

    @Deprecated
    public static String getPackageNameForPid(int pid) {
        String res = null;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            data.writeInt(pid);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.GET_PACKAGE_NAME_FOR_PID_TRANSACTION, data, reply, 0);
            reply.readException();
            res = reply.readString();
            data.recycle();
            reply.recycle();
            return res;
        } catch (Exception e) {
            Log.e(TAG, "getPackageNameForPid", e);
            return res;
        }
    }

    public static int getFocusedStackId() throws RemoteException {
        ActivityManager.StackInfo info = ActivityManager.getService().getFocusedStackInfo();
        if (info != null) {
            return info.stackId;
        }
        return -1;
    }

    public static int getCurrentUser() {
        return ActivityManager.getCurrentUser();
    }

    public static int checkComponentPermission(String permission, int uid, int owningUid, boolean exported) {
        return ActivityManager.checkComponentPermission(permission, uid, owningUid, exported);
    }

    public static void registerUserSwitchObserver(UserSwitchObserverEx observer, String name) throws RemoteException {
        ActivityManager.getService().registerUserSwitchObserver(observer.getUserSwitchObserver(), name);
    }

    public static void setProcessForeground(IBinder token, int pid, boolean isForeground) throws RemoteException {
        ActivityManager.getService().setProcessImportant(token, pid, isForeground, "");
    }

    public static Configuration getConfiguration() throws RemoteException {
        return ActivityManagerNative.getDefault().getConfiguration();
    }

    @HwSystemApi
    public static boolean updateConfiguration(Configuration curConfig) throws RemoteException {
        return ActivityManagerNative.getDefault().updateConfiguration(curConfig);
    }

    public static void updatePersistentConfiguration(Configuration values) throws RemoteException {
        ActivityManagerNative.getDefault().updatePersistentConfiguration(values);
    }

    public static void resumeAppSwitches() throws RemoteException {
        ActivityManagerNative.getDefault().resumeAppSwitches();
    }

    public static void stopAppSwitches() throws RemoteException {
        ActivityManagerNative.getDefault().stopAppSwitches();
    }

    public static List<ActivityManager.RecentTaskInfo> getRecentTasksForUser(ActivityManager am, int maxNum, int flags, int userId) throws SecurityException {
        try {
            return ActivityManager.getService().getRecentTasks(maxNum, flags, userId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean startUserInBackground(int userId) throws RemoteException {
        return ActivityManagerNative.getDefault().startUserInBackground(userId);
    }

    public static boolean isUserRunning(int userId) throws RemoteException {
        return ActivityManagerNative.getDefault().isUserRunning(userId, 0);
    }

    public static long getLastActiveTime(ActivityManager.RecentTaskInfo info) {
        if (info != null) {
            return info.lastActiveTime;
        }
        return 0;
    }

    public static long getUserId(ActivityManager.RecentTaskInfo info) {
        if (info != null) {
            return (long) info.userId;
        }
        return 0;
    }

    public static void switchUser(int userId) throws RemoteException {
        ActivityManagerNative.getDefault().switchUser(userId);
    }

    public static boolean requestContentNode(ComponentName componentName, Bundle bundle, int token) {
        return HwActivityTaskManager.requestContentNode(componentName, bundle, token);
    }

    public static boolean requestContentOther(ComponentName componentName, Bundle bundle, int token) {
        return HwActivityTaskManager.requestContentOther(componentName, bundle, token);
    }

    public static boolean addGameSpacePackageList(List<String> packageList) {
        return HwActivityTaskManager.addGameSpacePackageList(packageList);
    }

    public static boolean delGameSpacePackageList(List<String> packageList) {
        return HwActivityTaskManager.delGameSpacePackageList(packageList);
    }

    public static void registerGameObserver(IGameObserver observer) {
        HwActivityTaskManager.registerGameObserver(observer);
    }

    public static void unregisterGameObserver(IGameObserver observer) {
        HwActivityTaskManager.unregisterGameObserver(observer);
    }

    public static void registerGameObserverEx(IGameObserverEx observer) {
        HwActivityTaskManager.registerGameObserverEx(observer);
    }

    public static void unregisterGameObserverEx(IGameObserverEx observer) {
        HwActivityTaskManager.unregisterGameObserverEx(observer);
    }

    public static boolean isInGameSpace(String packageName) {
        return HwActivityTaskManager.isInGameSpace(packageName);
    }

    public static List<String> getGameList() {
        return HwActivityTaskManager.getGameList();
    }

    public static boolean isGameDndOn() {
        return HwActivityTaskManager.isGameDndOn();
    }

    public static boolean isGameDndOnEx() {
        return HwActivityTaskManager.isGameDndOnEx();
    }

    public static boolean isGameKeyControlOn() {
        return HwActivityTaskManager.isGameKeyControlOn();
    }

    public static boolean isGameGestureDisabled() {
        return HwActivityTaskManager.isGameGestureDisabled();
    }

    public static void registerProcessObserver(IProcessObserverEx observerEx) throws RemoteException {
        if (observerEx != null) {
            ActivityManager.getService().registerProcessObserver(observerEx.getIProcessObserver());
        }
    }

    public static void unregisterProcessObserver(IProcessObserverEx observerEx) throws RemoteException {
        if (observerEx != null) {
            ActivityManager.getService().unregisterProcessObserver(observerEx.getIProcessObserver());
        }
    }

    public static int startActivityFromRecents(int taskId, Bundle bOptions) {
        try {
            return ActivityManagerNative.getDefault().startActivityFromRecents(taskId, bOptions);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long[] getProcessPss(int[] pids) throws RemoteException {
        return ActivityManager.getService().getProcessPss(pids);
    }

    public static boolean clearApplicationUserData(String packageName, IPackageDataObserver observer) throws RemoteException {
        return ActivityManager.getService().clearApplicationUserData(packageName, false, observer, UserHandle.myUserId());
    }

    public static void forceStopPackage(String pkgname, int userId) throws RemoteException {
        ActivityManager.getService().forceStopPackage(pkgname, userId);
    }

    public static boolean isFreeFormVisible() {
        return HwActivityTaskManager.isFreeFormVisible();
    }

    public static void registerHwActivityNotifier(IHwActivityNotifierEx notifier, String reason) {
        if (notifier != null) {
            HwActivityTaskManager.registerHwActivityNotifier(notifier.getHwActivityNotifier(), reason);
        }
    }

    public static void unregisterHwActivityNotifier(IHwActivityNotifierEx notifier) {
        if (notifier != null) {
            HwActivityTaskManager.unregisterHwActivityNotifier(notifier.getHwActivityNotifier());
        }
    }

    public static Bundle getTopActivity() {
        return HwActivityTaskManager.getTopActivity();
    }

    public static ActivityInfo getLastResumedActivity() {
        return HwActivityTaskManager.getLastResumedActivity();
    }

    @Deprecated
    public static void moveTaskByType(int taskId, int flags, Bundle bOptions, int type) {
        Log.w(TAG, "new moveTaskByType function will instead");
    }

    public static void moveTaskByType(Context context, int taskId, int flags, Bundle bOptions, int type) {
        if (type != 1) {
            if (type == 2) {
                try {
                    ActivityManagerNative.getDefault().removeTask(taskId);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        } else if (context == null) {
            Log.i(TAG, "moveTaskByType context is null,cannot be processed");
        } else {
            ActivityManagerNative.getDefault().moveTaskToFront((IApplicationThread) null, context.getPackageName(), taskId, flags, bOptions);
        }
    }

    public static List<ActivityManager.RunningTaskInfo> getTasks(int num) {
        try {
            return ActivityManagerNative.getDefault().getTasks(num);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int getStackId(ActivityManager.RunningTaskInfo taskInfo) {
        return taskInfo.stackId;
    }

    public static boolean isHighEndGfx() {
        return ActivityManager.isHighEndGfx();
    }

    public static Bitmap getThumbnail(int taskId, boolean isReducedResolution) {
        long before = System.currentTimeMillis();
        int pid = Process.myPid();
        int uid = Process.myUid();
        ActivityManager.TaskSnapshot snapshot = HwActivityTaskManager.getTaskSnapshot(taskId, isReducedResolution);
        long after = System.currentTimeMillis();
        Log.i(TAG, "getThumbnail pid = " + pid + " Uid = " + uid + "cost time(ms) = " + (after - before));
        if (snapshot != null) {
            return Bitmap.wrapHardwareBuffer(HardwareBuffer.createFromGraphicBuffer(snapshot.getSnapshot()), snapshot.getColorSpace());
        }
        return null;
    }

    public static void setLaunchWindowingMode(ActivityOptions options, int windowMode, Context context) {
        if (context == null || options == null || context.checkPermission("com.huawei.permission.SET_WINDOW_MODE", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
            Log.e(TAG, "Permission Denide for setLaunchWindowingMode");
            return;
        }
        options.setLaunchWindowingMode(windowMode);
        Log.i(TAG, "setLaunchWindowingMode: " + windowMode);
    }

    public static void setTaskWindowingMode(int taskId, int windowingMode, boolean toTop) {
        try {
            ActivityTaskManager.getService().setTaskWindowingMode(taskId, windowingMode, toTop);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static List<ActivityManager.RunningTaskInfo> getVisibleTasks() {
        return HwActivityTaskManager.getVisibleTasks();
    }

    public static int[] setFreeformStackVisibility(int displayId, int[] stackIdArray, boolean isVisible) {
        return HwActivityTaskManager.setFreeformStackVisibility(displayId, stackIdArray, isVisible);
    }

    public static void dismissSplitScreenToFocusedStack() {
        HwActivityTaskManager.dismissSplitScreenToFocusedStack();
    }

    public static int getActivityWindowMode(Activity activity) {
        if (activity == null) {
            return 0;
        }
        return HwActivityTaskManager.getActivityWindowMode(activity.getActivityToken());
    }

    public static Bundle getSplitStacksPos(Context context, int splitRatio) {
        if (context == null) {
            return null;
        }
        return HwActivityTaskManager.getSplitStacksPos(context.getDisplayId(), splitRatio);
    }

    public static boolean enterCoordinationMode(Intent intent) {
        return HwActivityTaskManager.enterCoordinationMode(intent);
    }

    public static boolean exitCoordinationMode(boolean toTop) {
        return HwActivityTaskManager.exitCoordinationMode(toTop);
    }

    public static boolean isResizeable(ActivityInfo activityInfo) {
        if (activityInfo == null) {
            return false;
        }
        if (activityInfo.resizeMode == 2 || activityInfo.resizeMode == 4 || activityInfo.resizeMode == 1) {
            return true;
        }
        return false;
    }

    public static void setSplitBarVisibility(boolean isVisibility) {
        HwActivityTaskManager.setSplitBarVisibility(isVisibility);
    }

    public static Bundle getHwMultiWindowAppControlLists() {
        return HwActivityTaskManager.getHwMultiWindowAppControlLists();
    }

    @HwSystemApi
    public static List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException {
        if (ActivityManager.getService() != null) {
            return ActivityManager.getService().getRunningAppProcesses();
        }
        Log.e(TAG, "getRunningAppProcesses: ActivityManager.getService() is null!");
        return new ArrayList(0);
    }

    @HwSystemApi
    public static void broadcastStickyIntent(Intent intent, int appOp, int userId) {
        ActivityManager.broadcastStickyIntent(intent, appOp, userId);
    }

    public static List<String> getVisiblePackages() {
        return HwActivityTaskManager.getVisiblePackages();
    }

    @HwSystemApi
    public static int getTaskForActivity(IBinder token, boolean onlyRoot) throws RemoteException {
        return ActivityManager.getService().getTaskForActivity(token, onlyRoot);
    }

    @HwSystemApi
    public static boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) throws RemoteException {
        return ActivityManager.getService().moveActivityTaskToBack(token, nonRoot);
    }

    @HwSystemApi
    public static boolean isStartResultSuccessful(int errCode) {
        return ActivityManager.isStartResultSuccessful(errCode);
    }

    @HwSystemApi
    public static int startActivity(String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, Bundle bOptions) throws RemoteException {
        return ActivityManager.getService().startActivity(ActivityThread.currentActivityThread().getApplicationThread(), callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, startFlags, (ProfilerInfo) null, bOptions);
    }

    public static Bundle getHwMultiWindowState() {
        return HwActivityTaskManager.getHwMultiWindowState();
    }

    public static void startActivityOneStep(Context context, Intent intent, int hwMultiWindowingMode) {
        Log.i(TAG, "startActivityOneStep,caller:" + context + ",intent:" + intent + ",mode:" + hwMultiWindowingMode);
        if (context == null || intent == null) {
            Log.e(TAG, "startActivityOneStep context is null or intent is null");
        } else if (!HWMULTIWIN_ENABLED) {
            context.startActivity(intent, ActivityOptions.makeBasic().toBundle());
        } else {
            boolean isResolverPackage = false;
            ResolveInfo ri = context.getPackageManager().resolveActivity(intent, 0);
            if (ri != null && HWRESOLVER_PACKAGENAME.equals(ri.activityInfo.packageName)) {
                isResolverPackage = true;
            }
            intent.addFlags(268435456);
            switch (hwMultiWindowingMode) {
                case 100:
                case 101:
                    if (isResolverPackage) {
                        try {
                            intent.putExtra(ANDROID_ACTIVITY_WINDOWING_MODE, hwMultiWindowingMode);
                        } catch (BadParcelableException e) {
                            Log.e(TAG, "startActivityOneStep exception");
                        }
                    }
                    ActivityOptions options = ActivityOptions.makeBasic();
                    options.setLaunchWindowingMode(hwMultiWindowingMode);
                    context.startActivity(intent, options.toBundle());
                    return;
                case 102:
                    if (isResolverPackage) {
                        try {
                            intent.putExtra(ANDROID_ACTIVITY_WINDOWING_MODE, hwMultiWindowingMode);
                        } catch (BadParcelableException e2) {
                            Log.e(TAG, "startActivityOneStep exception");
                        }
                        context.startActivity(intent);
                        return;
                    }
                    ActivityOptions options2 = ActivityOptions.makeBasic();
                    options2.setLaunchWindowingMode(hwMultiWindowingMode);
                    context.startActivity(intent, options2.toBundle());
                    return;
                default:
                    Log.e(TAG, "startActivityOneStep not support hwMultiWindowingMode:" + hwMultiWindowingMode);
                    return;
            }
        }
    }

    public static boolean preloadApplicationForLauncher(String packageName, int userId, int preloadType) {
        return HwSysResManager.getInstance().preloadAppForLauncher(packageName, userId, preloadType);
    }
}
