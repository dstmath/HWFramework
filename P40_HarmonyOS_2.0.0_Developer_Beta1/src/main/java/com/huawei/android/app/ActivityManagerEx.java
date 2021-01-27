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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
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
import java.util.Map;

public class ActivityManagerEx {
    public static final String ANDROID_ACTIVITY_WINDOWING_MODE = "android.activity.windowingMode";
    private static final String HI_MOVE_PACKAGE = "com.huawei.himovie";
    private static final boolean HWMULTIWIN_ENABLED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    public static final String HW_MULTIWINDOW_FREEFORM_FLOATIME = "float_ime_state";
    public static final int HW_MULTI_WINDOWING_MODE_FREEFORM = 102;
    public static final int HW_MULTI_WINDOWING_MODE_MAGIC = 103;
    public static final int HW_MULTI_WINDOWING_MODE_PRIMARY = 100;
    public static final int HW_MULTI_WINDOWING_MODE_SECONDARY = 101;
    public static final String HW_MUTILWINDOW_BLACKLIST_APP = "blacklist";
    public static final String HW_MUTILWINDOW_RECOMLIST_APP = "recomlist";
    public static final String HW_MUTILWINDOW_WHITELIST_APP = "whitelist";
    @HwSystemApi
    public static final String HW_MUTILWINDOW_WHITELIST_APP_FREEFORM_ONLY = "whitelist_freeform_only";
    public static final int HW_PC_MULTI_WINDOWING_MODE_FREEFORM = 105;
    public static final String HW_SPLIT_SCREEN_PRIMARY_BOUNDS = "primaryBounds";
    public static final int HW_SPLIT_SCREEN_PRIMARY_LEFT = 1;
    public static final String HW_SPLIT_SCREEN_PRIMARY_POSITION = "primaryPosition";
    public static final int HW_SPLIT_SCREEN_PRIMARY_TOP = 0;
    public static final int HW_SPLIT_SCREEN_RATIO_DEFAULT = 0;
    public static final int HW_SPLIT_SCREEN_RATIO_PRAIMARY_LESS_THAN_DEFAULT = 1;
    public static final int HW_SPLIT_SCREEN_RATIO_PRAIMARY_MORE_THAN_DEFAULT = 2;
    public static final int HW_SPLIT_SCREEN_RATIO_PRIMARY_FULL_RELATIVE = 5;
    public static final int HW_SPLIT_SCREEN_RATIO_SECONDARY_FULL_RELATIVE = 6;
    public static final String HW_SPLIT_SCREEN_SECONDARY_BOUNDS = "secondaryBounds";
    public static final int HW_TV_MULTI_WINDOWING_MODE_FREEFORM = 108;
    public static final int HW_TV_MULTI_WINDOWING_MODE_PRIMARY = 106;
    public static final int HW_TV_MULTI_WINDOWING_MODE_SECONDARY = 107;
    @HwSystemApi
    public static final int INTENT_SENDER_ACTIVITY = 2;
    private static final boolean IS_HW_MULTIWINDOW_MULTITASK_ONESTEPWIN = SystemProperties.getBoolean("hw_mc.multiwindow.multitask_onestepwin", true);
    public static final int MOVE_TASK_TO_BACK = 0;
    public static final int MOVE_TASK_TO_FRONT = 1;
    public static final String PAD_CAST = "padCast";
    public static final String PC_CAST = "pcCast";
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
    @HwSystemApi
    public static final int WINDOWING_MODE_UNDEFINED = 0;
    private static final String WPS_PACKAGE_NAME = "cn.wps.moffice_eng";

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

    @HwSystemApi
    public static void registerUserSwitchObserver(SynchronousUserSwitchObserverEx observer, String name) throws RemoteException {
        ActivityManager.getService().registerUserSwitchObserver(observer.getSynchronousUserSwitchObserver(), name);
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

    public static void moveTaskToFrontForMultiDisplay(int taskId) {
        HwActivityTaskManager.moveTaskToFrontForMultiDisplay(taskId);
    }

    public static void setFocusedTaskForMultiDisplay(int taskId) {
        HwActivityTaskManager.setFocusedTaskForMultiDisplay(taskId);
    }

    public static void hwResizeTaskForMultiDisplay(int taskId, Rect bounds) {
        HwActivityTaskManager.hwResizeTaskForMultiDisplay(taskId, bounds);
    }

    public static void moveTaskBackwardsForMultiDisplay(int taskId) {
        HwActivityTaskManager.moveTaskBackwardsForMultiDisplay(taskId);
    }

    public static void setPCFullSize(int fullWidth, int fullHeight, int phoneOrientation) {
        HwActivityTaskManager.setPCFullSize(fullWidth, fullHeight, phoneOrientation);
    }

    public static void setPCVirtualSize(int virtualWidth, int virtualHeight, int phoneOrientation) {
        HwActivityTaskManager.setPCVirtualSize(virtualWidth, virtualHeight, phoneOrientation);
    }

    public static void setPCMultiCastMode(boolean isPCMultiCastMode) {
        HwActivityTaskManager.setPCMultiCastMode(isPCMultiCastMode);
    }

    public static void registerMultiDisplayMessenger(Messenger messenger) {
        HwActivityTaskManager.registerMultiDisplayMessenger(messenger);
    }

    public static void unregisterMultiDisplayMessenger(Messenger messenger) {
        HwActivityTaskManager.unregisterMultiDisplayMessenger(messenger);
    }

    public static void hwTogglePCFloatWinodow(int taskId) {
        HwActivityTaskManager.hwTogglePCFloatWindow(taskId);
    }

    public static void hwTogglePhoneFullScreen(int taskId) {
        HwActivityTaskManager.hwTogglePhoneFullScreen(taskId);
    }

    public static List<Bundle> getTaskList() {
        return HwActivityTaskManager.getTaskList();
    }

    public static int getCurTopFullScreenTaskState() {
        return HwActivityTaskManager.getCurTopFullScreenTaskState();
    }

    public static int getCurPCWindowAreaNum() {
        return HwActivityTaskManager.getCurPCWindowAreaNum();
    }

    public static List<Bundle> getLastRencentTaskList() {
        return HwActivityTaskManager.getLastRencentTaskList();
    }

    public static int retrievePCMultiWinConfig(String configXML) {
        return HwActivityTaskManager.retrievePCMultiWinConfig(configXML);
    }

    public static void hwSetRequestedOrientation(int taskId, int requestedOrientation) {
        HwActivityTaskManager.hwSetRequestedOrientation(taskId, requestedOrientation);
    }

    public static void setPcSize(int pcWidth, int pcHeight) {
        HwActivityTaskManager.setPcSize(pcWidth, pcHeight);
    }

    public static void setMultiDisplayParamsWithType(int type, Bundle bundle) {
        HwActivityTaskManager.setMultiDisplayParamsWithType(type, bundle);
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
        int res = HwActivityTaskManager.getActivityWindowMode(activity.getActivityToken());
        if (res != 105 || HI_MOVE_PACKAGE.equals(activity.getPackageName())) {
            return res;
        }
        return 102;
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
    public static boolean moveHwFreeFormToBack(Activity activity, boolean nonRoot) {
        if (!(activity == null || activity.checkPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid()) != 0)) {
            return HwActivityTaskManager.minimizeHwFreeForm(activity.getActivityToken(), (String) null, nonRoot);
        }
        Log.e(TAG, "Permission Denide for moveTaskToBack");
        return false;
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

    public static void setForegroundFreeFormNum(int num) {
        HwActivityTaskManager.setForegroundFreeFormNum(num);
    }

    public static Map<String, Boolean> getAppUserAwarenessState(int displayId, List<String> packageNames) {
        return HwActivityTaskManager.getAppUserAwarenessState(displayId, packageNames);
    }

    public static List<ActivityManager.RecentTaskInfo> getFilteredTasks(int userId, int displayId, String packageName, int[] windowingModes, boolean isIgnoreVisible, int maxNum) {
        return HwActivityTaskManager.getFilteredTasks(userId, displayId, packageName, windowingModes, isIgnoreVisible, maxNum);
    }

    public static boolean removeTask(int taskId, IBinder token, String packageName, boolean isRemoveFromRecents) {
        return HwActivityTaskManager.removeTask(taskId, token, packageName, isRemoveFromRecents, "app-request");
    }

    public static void removeTasks(int[] taskIds) {
        HwActivityTaskManager.removeTasks(taskIds);
    }

    public static boolean setStackScale(int taskId, float scale) {
        return HwActivityTaskManager.setStackScale(taskId, scale);
    }

    public static int[] startActivitiesFromRecents(int[] taskIds, List<Bundle> bOptions, boolean divideSplitScreen, int flag) {
        return HwActivityTaskManager.startActivitiesFromRecents(taskIds, bOptions, divideSplitScreen, flag);
    }

    public static int[] startActivitiesFromRecents(int[] taskIds, List<Bundle> bOptions, boolean divideSplitScreen) {
        return HwActivityTaskManager.startActivitiesFromRecents(taskIds, bOptions, divideSplitScreen, 0);
    }

    public static void startActivityOneStepWindow(Context context, Intent intent, int hwMultiWindowingMode) {
        startActivityOneStep(context, intent, hwMultiWindowingMode);
    }

    @Deprecated
    public static void startActivityOneStep(Context context, Intent intent, int hwMultiWindowingMode) {
        Log.i(TAG, "startActivityOneStep,caller:" + context + ",intent:" + intent + ",mode:" + hwMultiWindowingMode);
        if (context == null || intent == null) {
            Log.e(TAG, "startActivityOneStep context is null or intent is null");
        } else if (!HWMULTIWIN_ENABLED) {
            context.startActivity(intent, ActivityOptions.makeBasic().toBundle());
        } else if (IS_HW_MULTIWINDOW_MULTITASK_ONESTEPWIN || WPS_PACKAGE_NAME.equals(context.getPackageName())) {
            intent.addFlags(268435456);
            switch (hwMultiWindowingMode) {
                case 100:
                case 101:
                    startActivityOneStepSplitScreen(context, intent, hwMultiWindowingMode);
                    return;
                case 102:
                    startActivityOneStepFreeform(context, intent, hwMultiWindowingMode);
                    return;
                default:
                    Log.e(TAG, "startActivityOneStep not support hwMultiWindowingMode:" + hwMultiWindowingMode);
                    return;
            }
        } else {
            context.startActivity(intent, ActivityOptions.makeBasic().toBundle());
        }
    }

    private static void startActivityOneStepFreeform(Context context, Intent intent, int hwMultiWindowingMode) {
        String dstPackageName;
        ResolveInfo ri = context.getPackageManager().resolveActivity(intent, 0);
        boolean isResolverPackage = ri != null && "com.huawei.android.internal.app".equals(ri.activityInfo.packageName);
        try {
            intent.addHwFlags(1048576);
            intent.putExtra("oneStepTime", System.currentTimeMillis());
            intent.putExtra("srcPackageName", context.getPackageName());
        } catch (BadParcelableException e) {
            Log.e(TAG, "startActivityOneStepFreeform exception");
        } catch (Exception e2) {
            Log.e(TAG, "startActivityOneStepFreeform exception");
        }
        if (isResolverPackage) {
            try {
                intent.putExtra(ANDROID_ACTIVITY_WINDOWING_MODE, hwMultiWindowingMode);
            } catch (BadParcelableException e3) {
                Log.e(TAG, "startActivityOneStepFreeform exception");
            } catch (Exception e4) {
                Log.e(TAG, "startActivityOneStepFreeform exception");
            }
            context.startActivity(intent, ActivityOptions.makeBasic().toBundle());
            return;
        }
        if (ri == null) {
            dstPackageName = null;
        } else {
            try {
                dstPackageName = ri.activityInfo.packageName;
            } catch (BadParcelableException e5) {
                Log.e(TAG, "startActivityOneStepFreeform exception");
            } catch (Exception e6) {
                Log.e(TAG, "startActivityOneStepFreeform exception");
            }
        }
        intent.putExtra("dstPackageName", dstPackageName);
        boolean isAppMainResizeable = true;
        PackageManager packageManager = context.getPackageManager();
        if (!(ri == null || ri.activityInfo.packageName == null)) {
            Intent appMainIntent = packageManager.getLaunchIntentForPackage(ri.activityInfo.packageName);
            if (appMainIntent != null) {
                ActivityInfo activityInfo = appMainIntent.resolveActivityInfo(packageManager, 786432);
                if (activityInfo == null || !HwActivityTaskManager.isResizableApp(activityInfo)) {
                    isAppMainResizeable = false;
                }
            } else {
                Log.e(TAG, "startActivityOneStepFreeform not found appMainIntent");
                isAppMainResizeable = false;
            }
        }
        ActivityOptions options = ActivityOptions.makeBasic();
        if (isAppMainResizeable) {
            options = ActivityOptions.makeCustomAnimation(context, 34209874, 0);
            options.setLaunchWindowingMode(hwMultiWindowingMode);
        } else {
            options.setLaunchWindowingMode(1);
        }
        context.startActivity(intent, options.toBundle());
    }

    private static void startActivityOneStepSplitScreen(Context context, Intent intent, int hwMultiWindowingMode) {
        boolean isResolverPackage = false;
        ResolveInfo ri = context.getPackageManager().resolveActivity(intent, 0);
        if (ri != null && "com.huawei.android.internal.app".equals(ri.activityInfo.packageName)) {
            isResolverPackage = true;
        }
        if (isResolverPackage) {
            try {
                intent.putExtra(ANDROID_ACTIVITY_WINDOWING_MODE, hwMultiWindowingMode);
            } catch (BadParcelableException e) {
                Log.e(TAG, "startActivityOneStepSplitScreen exception");
            } catch (Exception e2) {
                Log.e(TAG, "startActivityOneStepSplitScreen exception");
            }
        }
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setLaunchWindowingMode(hwMultiWindowingMode);
        context.startActivity(intent, options.toBundle());
    }

    public static boolean preloadApplicationForLauncher(String packageName, int userId, int preloadType) {
        return HwSysResManager.getInstance().preloadAppForLauncher(packageName, userId, preloadType);
    }

    @HwSystemApi
    public static void forceStopPackageAsUser(String packageName, int userId) throws RemoteException {
        ActivityManager.getService().forceStopPackage(packageName, userId);
    }

    @HwSystemApi
    public static int checkUidPermission(String permission, int uid) {
        return ActivityManager.checkUidPermission(permission, uid);
    }

    @HwSystemApi
    public static boolean clearApplicationUserData(String packageName) throws RemoteException {
        return ActivityManager.getService().clearApplicationUserData(packageName, false, (IPackageDataObserver) null, UserHandle.myUserId());
    }

    public static Rect resizeActivityStack(Activity activity, Rect bounds, boolean isAlwaysOnTop) {
        return HwActivityTaskManager.resizeActivityStack(activity.getActivityToken(), bounds, isAlwaysOnTop);
    }

    public static Rect getActivityStackBounds(Activity activity) {
        ActivityThread activityThread = ActivityThread.currentActivityThread();
        if (activityThread != null) {
            return activityThread.getActivityStackBounds(activity);
        }
        return null;
    }

    public static int getVirtualDisplayId(String castType) {
        return HwActivityTaskManager.getVirtualDisplayId(castType);
    }

    public static boolean moveStacksToDisplay(int fromDisplayId, int toDisplayId, boolean isOnlyFocus) {
        return HwActivityTaskManager.moveStacksToDisplay(fromDisplayId, toDisplayId, isOnlyFocus);
    }

    public static int getDisplayId(Context context) {
        return context.getDisplayId();
    }

    public static boolean removeTask(Activity activity, boolean isRemoveFromRecents) {
        if (activity == null) {
            return false;
        }
        return removeTask(-1, activity.getActivityToken(), null, isRemoveFromRecents);
    }

    public static boolean isDisplayHoldScreen(int displayId) {
        return HwActivityTaskManager.isDisplayHoldScreen(displayId);
    }

    public static boolean isPadCastMaxSizeEnable() {
        return HwActivityTaskManager.isPadCastMaxSizeEnable();
    }

    public static boolean isMirrorCast(String castType) {
        return HwActivityTaskManager.isMirrorCast(castType);
    }

    public static int getTopFocusedDisplayId() {
        return HwActivityTaskManager.getTopFocusedDisplayId();
    }

    public static void setStackWindowingMode(Activity activity, int windowingMode, Rect bounds) {
        HwActivityTaskManager.setStackWindowingMode(activity.getActivityToken(), windowingMode, bounds);
    }

    public static boolean isStatusBarPermenantlyShowing() {
        return HwActivityTaskManager.isStatusBarPermenantlyShowing();
    }

    @HwSystemApi
    public static void forceStopPackageAsUser(ActivityManager activityManager, String packageName, int userId) {
        activityManager.forceStopPackageAsUser(packageName, userId);
    }

    public static void notifyLauncherAction(String category, Bundle bundle) {
        HwActivityTaskManager.notifyLauncherAction(category, bundle);
    }

    public static void setLaunchTaskId(ActivityOptions options, int taskId, Context context) {
        Log.i(TAG, "setLaunchTaskId:" + taskId);
        if (taskId <= 0) {
            Log.e(TAG, "setLaunchTaskId, taskId must lager than 0");
            return;
        }
        if (context == null || options == null || context.checkPermission("android.permission.START_TASKS_FROM_RECENTS", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
            Log.e(TAG, "Permission Denied for setLaunchTaskId");
        } else {
            options.setLaunchTaskId(taskId);
        }
    }

    public static boolean setDockCallBackInfo(IHwDockCallBackEx callBackEx, int type) {
        if (callBackEx != null) {
            return HwActivityTaskManager.setDockCallBackInfo(callBackEx.getHwDockCallBack(), type);
        }
        return false;
    }

    public static void setTaskWindowingMode(Activity activity, int taskId, int windowingMode, Rect bounds, float scale) {
        IBinder iBinder;
        if (activity == null) {
            iBinder = null;
        } else {
            iBinder = activity.getActivityToken();
        }
        HwActivityTaskManager.setTaskWindowingMode(iBinder, taskId, windowingMode, bounds, scale);
    }

    public static boolean resizeStack(Activity activity, int taskId, Rect bounds, float scale) {
        IBinder iBinder;
        if (activity == null) {
            iBinder = null;
        } else {
            iBinder = activity.getActivityToken();
        }
        return HwActivityTaskManager.resizeStack(iBinder, taskId, bounds, scale);
    }

    public static void startActivityTvSplit(Intent intent, int tvSplitWindowingMode, Rect startBounds, float startScale, Rect otherBounds, float otherScale) {
        HwActivityTaskManager.startActivityTvSplit(intent, tvSplitWindowingMode, startBounds, startScale, otherBounds, otherScale);
    }

    public static void setTaskCombinedWindowingMode(int taskId1, int windowingMode1, Rect bounds1, float scale1, int taskId2, int windowingMode2, Rect bounds2, float scale2) {
        HwActivityTaskManager.setTaskCombinedWindowingMode(taskId1, windowingMode1, bounds1, scale1, taskId2, windowingMode2, bounds2, scale2);
    }

    public static void resizeCombinedStack(int taskId1, Rect bounds1, float scale1, int taskId2, Rect bounds2, float scale2) {
        HwActivityTaskManager.resizeCombinedStack(taskId1, bounds1, scale1, taskId2, bounds2, scale2);
    }

    public static int getTaskIdByWindowingMode(int windowingMode) {
        List<ActivityManager.RunningTaskInfo> taskInfos = getVisibleTasks();
        if (taskInfos == null) {
            Log.e(TAG, "taskInfos is null");
            return -1;
        }
        for (int i = 0; i < taskInfos.size(); i++) {
            if (taskInfos.get(i).windowMode == windowingMode) {
                return taskInfos.get(i).taskId;
            }
        }
        return -1;
    }

    public static void setFocusedTask(int taskId) {
        try {
            ActivityTaskManager.getService().setFocusedTask(taskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void minimizeTvFreeForm(int taskId) {
        HwActivityTaskManager.minimizeTvFreeForm(taskId);
    }

    public static void setStackScaleForActivityOptions(ActivityOptions options, float stackScale, Context context) {
        if (context == null || options == null || context.checkPermission("com.huawei.permission.SET_WINDOW_MODE", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
            Log.e(TAG, "Permission Denide for setStackScale");
        } else {
            options.setStackScale(stackScale);
        }
    }

    public static List<String> getVisibleCanShowWhenLockedPackages(int displayId) {
        return HwActivityTaskManager.getVisibleCanShowWhenLockedPackages(displayId);
    }

    public static void setFocusableStack(int taskId, boolean isSetFocusableStack) {
        HwActivityTaskManager.setFocusableStack(taskId, isSetFocusableStack);
    }
}
