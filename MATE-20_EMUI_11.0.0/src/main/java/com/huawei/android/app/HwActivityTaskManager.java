package com.huawei.android.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.ActivityThread;
import android.app.HwRecentTaskInfo;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.IHwActivityNotifier;
import android.app.IHwDockCallBack;
import android.app.ITaskStackListener;
import android.app.ResourcesManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Singleton;
import android.util.Slog;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import com.huawei.android.app.IHwActivityTaskManager;
import com.huawei.android.fsm.HwFoldScreenManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwActivityTaskManager {
    public static final String ANDROID_ACTIVITY_WINDOWING_MODE = "android.activity.windowingMode";
    public static final int APP_EVENTTYPE_EXIT = 1102;
    public static final int APP_EVENTTYPE_SCREEN_SWITCH = 1103;
    public static final int APP_EVENTTYPE_START = 1101;
    public static final String DISPLAY_NAME_PAD_CAST = "HiSightPCDisplay";
    public static final String DST_PACKAGE_NAME = "dstPackageName";
    public static final int GET_FOCUS_FREEFORM_STACK_SCALE = -100;
    public static final String HWRESOLVER_PACKAGENAME = "com.huawei.android.internal.app";
    public static final String HW_FREEFORM_CENTER_BOUNDS = "hwFreeFormCenterBounds";
    public static final String HW_FREEFORM_CENTER_SCALE_RATIO = "hwFreeFormCenterScaleRatio";
    public static final String HW_FREEFORM_CENTER_VISUAL_BOUNDS = "hwFreeFormCenterVisualBounds";
    public static final String HW_MULTIWINDOW_FREEFORM_FLOATIME = "float_ime_state";
    public static final String HW_MULTIWINDOW_GLOBLESCALE = "globleScale";
    public static final String HW_MULTIWINDOW_IMETARGET_RECT = "ime_target_rect";
    public static final String HW_MULTIWINDOW_SPLITWIN_LEFTRIGHT = "is_leftright_split";
    public static final String HW_MUTILWINDOW_BLACKLIST_APP = "blacklist";
    public static final String HW_MUTILWINDOW_RECOMLIST_APP = "recomlist";
    public static final String HW_MUTILWINDOW_WHITELIST_APP = "whitelist";
    public static final String HW_MUTILWINDOW_WHITELIST_APP_FREEFORM_ONLY = "whitelist_freeform_only";
    public static final String HW_PC_MUTILCAST_WHITELIST_APP = "pc_multicast_whitelist";
    public static final String HW_SPLIT_SCREEN_PRIMARY_BOUNDS = "primaryBounds";
    public static final int HW_SPLIT_SCREEN_PRIMARY_EITHER = -1;
    public static final int HW_SPLIT_SCREEN_PRIMARY_LEFT = 1;
    public static final String HW_SPLIT_SCREEN_PRIMARY_POSITION = "primaryPosition";
    public static final int HW_SPLIT_SCREEN_PRIMARY_TOP = 0;
    public static final int HW_SPLIT_SCREEN_RATIO_DEFAULT = 0;
    public static final int HW_SPLIT_SCREEN_RATIO_PRAIMARY_LESS_THAN_DEFAULT = 1;
    public static final int HW_SPLIT_SCREEN_RATIO_PRAIMARY_MORE_THAN_DEFAULT = 2;
    public static final int HW_SPLIT_SCREEN_RATIO_PRIMARY_FULL_RELATIVE = 5;
    public static final int HW_SPLIT_SCREEN_RATIO_SECONDARY_FULL_RELATIVE = 6;
    public static final String HW_SPLIT_SCREEN_SECONDARY_BOUNDS = "secondaryBounds";
    private static final Singleton<IHwActivityTaskManager> IActivityTaskManagerSingleton = new Singleton<IHwActivityTaskManager>() {
        /* class com.huawei.android.app.HwActivityTaskManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwActivityTaskManager create() {
            try {
                return IHwActivityTaskManager.Stub.asInterface(ActivityTaskManager.getService().getHwInnerService());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final int INVALID_ID = -1;
    public static final boolean IS_HW_MULTIWINDOW_APPCOMPACT_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_appcompact", true);
    public static final String KEY_TASK_ID = "key_task_id";
    public static final String KEY_WINDOW_BOUND = "key_window_bound";
    public static final String KEY_WINDOW_MODE = "key_window_mode";
    public static final String MULTI_DISPLAY_MODE = "hw_multidisplay_mode";
    public static final int MULTI_DISPLAY_NON_CAST_MODE = 0;
    public static final int MULTI_DISPLAY_PAD_CAST_MODE = 1;
    public static final String ONE_STEP_TIME = "oneStepTime";
    public static final String PAD_CAST = "padCast";
    public static final String PAD_CAST_MODE_KEY = "hw.multidisplay.mode.pad";
    public static final String PC_CAST = "pcCast";
    public static final int PC_CAST_CONTROL_DEVICE_ID = -1431655681;
    public static final String PC_CAST_MODE_KEY = "hw.multidisplay.mode.pc";
    public static final String SRC_PACKAGE_NAME = "srcPackageName";
    private static final String TAG = "HwActivityTaskManager";
    public static final String UNIQUE_KEY_OF_VIRTUAL_DISPLAY = "HiSightPCDisplay";

    public static IHwActivityTaskManager getService() {
        return IActivityTaskManagerSingleton.get();
    }

    public static void registerHwActivityNotifier(IHwActivityNotifier notifier, String reason) {
        if (notifier != null) {
            try {
                getService().registerHwActivityNotifier(notifier, reason);
            } catch (RemoteException e) {
                Log.e(TAG, "registerHwActivityNotifier failed");
            }
        }
    }

    public static void unregisterHwActivityNotifier(IHwActivityNotifier notifier) {
        if (notifier != null) {
            try {
                getService().unregisterHwActivityNotifier(notifier);
            } catch (RemoteException e) {
                Log.e(TAG, "unregisterHwActivityNotifier failed");
            }
        }
    }

    public static Bundle getTopActivity() {
        try {
            return getService().getTopActivity();
        } catch (RemoteException e) {
            Log.e(TAG, "getTopActivity failed");
            return new Bundle();
        }
    }

    public static ActivityInfo getLastResumedActivity() {
        try {
            return getService().getLastResumedActivity();
        } catch (RemoteException e) {
            Log.e(TAG, "getLastResumedActivity failed");
            return null;
        }
    }

    public static void registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback callback) {
        if (callback != null) {
            try {
                getService().registerAtmDAMonitorCallback(callback);
            } catch (RemoteException e) {
                Log.e(TAG, "registerAtmDAMonitorCallback failed");
            }
        }
    }

    public static boolean isInMultiWindowMode() {
        try {
            return getService().isInMultiWindowMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isInMultiWindowMode failed");
            return false;
        }
    }

    public static boolean registerThirdPartyCallBack(IMWThirdpartyCallback callBackHandler) {
        try {
            return getService().registerThirdPartyCallBack(callBackHandler);
        } catch (RemoteException e) {
            Log.e(TAG, "registerThirdPartyCallBack failed: catch RemoteException!");
            return false;
        }
    }

    public static boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback callBackHandler) {
        try {
            return getService().unregisterThirdPartyCallBack(callBackHandler);
        } catch (RemoteException e) {
            Log.e(TAG, "registerThirdPartyCallBack failed: catch RemoteException!");
            return false;
        }
    }

    public static void setWarmColdSwitch(boolean isEnable) {
        if (getService() != null) {
            long token = Binder.clearCallingIdentity();
            try {
                getService().setWarmColdSwitch(isEnable);
            } catch (RemoteException e) {
                Log.e(TAG, "setWarmColdSwitch failed: catch RemoteException!");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public static int getTopTaskIdInDisplay(int displayId, String pkgName, boolean isInvisible) {
        try {
            return getService().getTopTaskIdInDisplay(displayId, pkgName, isInvisible);
        } catch (RemoteException e) {
            Log.e(TAG, "getTopTaskIdInDisplay failed");
            return -1;
        }
    }

    public static boolean isTaskSupportResize(int taskId, boolean isFullscreen, boolean isMaximized) {
        try {
            return getService().isTaskSupportResize(taskId, isFullscreen, isMaximized);
        } catch (RemoteException e) {
            Log.e(TAG, "isTaskSupportResize failed");
            return false;
        }
    }

    public static boolean isSupportsSplitScreenWindowingMode(IBinder activityToken) {
        try {
            return getService().isSupportsSplitScreenWindowingMode(activityToken);
        } catch (RemoteException e) {
            Log.e(TAG, "isSupportsSplitScreenWindowingMode failed");
            return false;
        }
    }

    public static Rect getPCTopTaskBounds(int displayId) {
        try {
            return getService().getPCTopTaskBounds(displayId);
        } catch (RemoteException e) {
            Log.e(TAG, "getPCTopTaskBounds failed");
            return null;
        }
    }

    public static void hwRestoreTask(int taskId, float posX, float posY) {
        try {
            getService().hwRestoreTask(taskId, posX, posY);
        } catch (RemoteException e) {
            Log.e(TAG, "hwRestoreTask failed");
        }
    }

    public static void hwResizeTask(int taskId, Rect bounds) {
        try {
            getService().hwResizeTask(taskId, bounds);
        } catch (RemoteException e) {
            Log.e(TAG, "hwResizeTask failed");
        }
    }

    public static int getWindowState(IBinder token) {
        try {
            return getService().getWindowState(token);
        } catch (RemoteException e) {
            Log.e(TAG, "getWindowState failed");
            return -1;
        }
    }

    public static HwRecentTaskInfo getHwRecentTaskInfo(int taskId) {
        try {
            return getService().getHwRecentTaskInfo(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "getHwRecentTaskInfo failed");
            return null;
        }
    }

    public static void togglePCMode(boolean isPcMode, int displayId) {
        try {
            getService().togglePCMode(isPcMode, displayId);
        } catch (RemoteException e) {
            Log.e(TAG, "togglePCMode failed");
        }
    }

    public static void toggleHome() {
        try {
            getService().toggleHome();
        } catch (RemoteException e) {
            Log.e(TAG, "toggleHome failed");
        }
    }

    public static void unRegisterHwTaskStackListener(ITaskStackListener listener) {
        try {
            getService().unRegisterHwTaskStackListener(listener);
        } catch (RemoteException e) {
            Log.e(TAG, "unRegisterHwTaskStackListener failed");
        }
    }

    public static void unRegisterHwTaskStackListener(TaskStackListenerEx listenerEx) {
        try {
            getService().unRegisterHwTaskStackListener(listenerEx.getTaskStackListener());
        } catch (RemoteException e) {
            Log.e(TAG, "unRegisterHwTaskStackListener failed");
        }
    }

    public static void registerHwTaskStackListener(ITaskStackListener listener) {
        try {
            getService().registerHwTaskStackListener(listener);
        } catch (RemoteException e) {
            Log.e(TAG, "registerHwTaskStackListener failed");
        }
    }

    public static void registerHwTaskStackListener(TaskStackListenerEx listenerEx) {
        try {
            getService().registerHwTaskStackListener(listenerEx.getTaskStackListener());
        } catch (RemoteException e) {
            Log.e(TAG, "registerHwTaskStackListener failed");
        }
    }

    public static boolean checkTaskId(int taskId) {
        try {
            return getService().checkTaskId(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "checkTaskId failed: catch RemoteException!");
            return false;
        }
    }

    public static void moveTaskBackwards(int taskId) {
        try {
            getService().moveTaskBackwards(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "moveTaskBackwards failed: catch RemoteException!");
        }
    }

    public static Bitmap getTaskThumbnailOnPCMode(int taskId) {
        try {
            return getService().getTaskThumbnailOnPCMode(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "moveTaskBackwards failed: catch RemoteException!");
            return null;
        }
    }

    public static boolean requestContentNode(ComponentName componentName, Bundle bundle, int token) {
        if (componentName == null) {
            return false;
        }
        try {
            return getService().requestContentNode(componentName, bundle, token);
        } catch (RemoteException e) {
            Log.e(TAG, "requestContentNode failed");
            return false;
        }
    }

    public static boolean requestContentOther(ComponentName componentName, Bundle bundle, int token) {
        if (componentName == null) {
            return false;
        }
        try {
            return getService().requestContentOther(componentName, bundle, token);
        } catch (RemoteException e) {
            Log.e(TAG, "requestContentOther failed");
            return false;
        }
    }

    public static boolean addGameSpacePackageList(List<String> packageList) {
        try {
            return getService().addGameSpacePackageList(packageList);
        } catch (RemoteException e) {
            Log.e(TAG, "addGameSpacePackageList failed");
            return false;
        }
    }

    public static boolean delGameSpacePackageList(List<String> packageList) {
        try {
            return getService().delGameSpacePackageList(packageList);
        } catch (RemoteException e) {
            Log.e(TAG, "delGameSpacePackageList failed");
            return false;
        }
    }

    public static void registerGameObserver(IGameObserver observer) {
        try {
            getService().registerGameObserver(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "registerGameObserver failed");
        }
    }

    public static void unregisterGameObserver(IGameObserver observer) {
        try {
            getService().unregisterGameObserver(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterGameObserver failed");
        }
    }

    public static void registerGameObserverEx(IGameObserverEx observer) {
        try {
            getService().registerGameObserverEx(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "registerGameObserverEx failed");
        }
    }

    public static void unregisterGameObserverEx(IGameObserverEx observer) {
        try {
            getService().unregisterGameObserverEx(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterGameObserverEx failed");
        }
    }

    public static boolean isInGameSpace(String packageName) {
        try {
            return getService().isInGameSpace(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "isInGameSpace failed");
            return false;
        }
    }

    public static List<String> getGameList() {
        try {
            return getService().getGameList();
        } catch (RemoteException e) {
            Log.e(TAG, "getGameList failed");
            return null;
        }
    }

    public static boolean isGameDndOn() {
        try {
            return getService().isGameDndOn();
        } catch (RemoteException e) {
            Log.e(TAG, "isGameDndOn failed");
            return false;
        }
    }

    public static boolean isGameDndOnEx() {
        try {
            return getService().isGameDndOnEx();
        } catch (RemoteException e) {
            Log.e(TAG, "isGameDndOnEx failed");
            return false;
        }
    }

    public static boolean isGameKeyControlOn() {
        try {
            return getService().isGameKeyControlOn();
        } catch (RemoteException e) {
            Log.e(TAG, "isGameKeyControlOn failed");
            return false;
        }
    }

    public static boolean isGameGestureDisabled() {
        try {
            return getService().isGameGestureDisabled();
        } catch (RemoteException e) {
            Log.e(TAG, "isGameGestureDisabled failed");
            return false;
        }
    }

    public static boolean isFreeFormVisible() {
        try {
            return getService().isFreeFormVisible();
        } catch (RemoteException e) {
            Log.e(TAG, "isFreeFormVisible failed");
            return false;
        }
    }

    public static boolean isTaskVisible(int id) {
        try {
            return getService().isTaskVisible(id);
        } catch (RemoteException e) {
            Log.e(TAG, "isTaskVisible failed");
            return false;
        }
    }

    public static void updateFreeFormOutLine(int state) {
        try {
            getService().updateFreeFormOutLine(state);
        } catch (RemoteException e) {
            Log.e(TAG, "updateFreeFormOutLine failed");
        }
    }

    public static void updateFreeFormOutLineForFloating(IBinder token, int state) {
        try {
            getService().updateFreeFormOutLineForFloating(token, state);
        } catch (RemoteException e) {
            Log.e(TAG, "updateFreeFormOutLineForFloating failed ");
        }
    }

    public static boolean isFullScreen(IBinder token) {
        try {
            return getService().isFullScreen(token);
        } catch (RemoteException e) {
            Log.e(TAG, "isFullScreen failed ");
            return false;
        }
    }

    public static int getCaptionState(IBinder token) {
        try {
            return getService().getCaptionState(token);
        } catch (RemoteException e) {
            Log.e(TAG, "getCaptionState failed");
            return 0;
        }
    }

    public static int getActivityWindowMode(IBinder token) {
        try {
            return getService().getActivityWindowMode(token);
        } catch (RemoteException e) {
            Log.e(TAG, "getActivityWindowMode failed");
            return 0;
        }
    }

    public static void dismissSplitScreenToFocusedStack() {
        try {
            getService().dismissSplitScreenToFocusedStack();
        } catch (RemoteException e) {
            Log.e(TAG, "dismissSplitScreenToFocusedStack failed");
        }
    }

    public static void onCaptionDropAnimationDone(IBinder activityToken) {
        try {
            getService().onCaptionDropAnimationDone(activityToken);
        } catch (RemoteException e) {
            Log.e(TAG, "onCaptionDropAnimationDone failed: " + e.getMessage());
        }
    }

    public static List<ActivityManager.RunningTaskInfo> getVisibleTasks() {
        try {
            return getService().getVisibleTasks();
        } catch (RemoteException e) {
            Log.e(TAG, "getVisibleTasks failed");
            return null;
        }
    }

    public static ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean isReducedResolution) {
        try {
            return getService().getTaskSnapshot(taskId, isReducedResolution);
        } catch (RemoteException e) {
            Log.e(TAG, "getTaskSnapshot failed");
            return null;
        }
    }

    public static ActivityManager.TaskSnapshot getActivityTaskSnapshot(IBinder activityToken, boolean isReducedResolution) {
        try {
            return getService().getActivityTaskSnapshot(activityToken, isReducedResolution);
        } catch (RemoteException e) {
            Log.e(TAG, "getTaskSnapshot failed");
            return null;
        }
    }

    public static int[] setFreeformStackVisibility(int displayId, int[] stackIdArray, boolean isVisible) {
        try {
            return getService().setFreeformStackVisibility(displayId, stackIdArray, isVisible);
        } catch (RemoteException e) {
            Log.e(TAG, "setFreeformStackVisibility failed");
            return new int[0];
        }
    }

    public static void handleMultiWindowSwitch(IBinder activityToken, Bundle info) {
        try {
            getService().handleMultiWindowSwitch(activityToken, info);
        } catch (RemoteException e) {
            Log.e(TAG, "handleMultiWindowSwitch failed");
        }
    }

    public static Bundle getSplitStacksPos(int displayId, int splitRatio) {
        try {
            return getService().getSplitStacksPos(displayId, splitRatio);
        } catch (RemoteException e) {
            Log.e(TAG, "getHwSplitStacksPos failed");
            return null;
        }
    }

    public static boolean enterCoordinationMode(Intent intent) {
        try {
            return getService().enterCoordinationMode(intent);
        } catch (RemoteException e) {
            Log.e(TAG, "enterCoordinationMode failed");
            return false;
        }
    }

    public static boolean exitCoordinationMode(boolean isTop) {
        try {
            return getService().exitCoordinationMode(isTop);
        } catch (RemoteException e) {
            Log.e(TAG, "exitCoordinationMode failed");
            return false;
        }
    }

    public static void setSplitBarVisibility(boolean isVisibility) {
        try {
            getService().setSplitBarVisibility(isVisibility);
        } catch (RemoteException e) {
            Log.e(TAG, "setSplitBarVisibility failed");
        }
    }

    public static boolean setCustomActivityController(IActivityController controller) {
        try {
            return getService().setCustomActivityController(controller);
        } catch (RemoteException e) {
            Log.e(TAG, "setCustomActivityController failed");
            return false;
        }
    }

    public static boolean isResizableApp(ActivityInfo activityInfo) {
        try {
            return getService().isResizableApp(activityInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "isResizableApp failed");
            return false;
        }
    }

    public static Bundle getHwMultiWindowAppControlLists() {
        try {
            return getService().getHwMultiWindowAppControlLists();
        } catch (RemoteException e) {
            Log.e(TAG, "getHwMultiWindowAppControlLists failed");
            return null;
        }
    }

    public static boolean isNeedAdapterCaptionView(String packageName) {
        try {
            return getService().isNeedAdapterCaptionView(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "isNeedAdapterCaptionView failed");
            return false;
        }
    }

    public static void saveMultiWindowTipState(String tipKey, int state) {
        try {
            getService().saveMultiWindowTipState(tipKey, state);
        } catch (RemoteException e) {
            Log.e(TAG, "saveMultiWindowTipState failed");
        }
    }

    public static boolean isSupportDragForMultiWin(IBinder token) {
        try {
            return getService().isSupportDragForMultiWin(token);
        } catch (RemoteException e) {
            Log.e(TAG, "check isSupportDragForMultiWin failed");
            return false;
        }
    }

    public static List<String> getVisiblePackages() {
        try {
            return getService().getVisiblePackages();
        } catch (RemoteException e) {
            Log.e(TAG, "getVisiblePackages failed");
            return new ArrayList();
        }
    }

    public static boolean setMultiWindowDisabled(boolean isDisabled) {
        try {
            return getService().setMultiWindowDisabled(isDisabled);
        } catch (RemoteException e) {
            Log.e(TAG, "setMultiWindowDisabled failed");
            return false;
        }
    }

    public static boolean getMultiWindowDisabled() {
        try {
            return getService().getMultiWindowDisabled();
        } catch (RemoteException e) {
            Log.e(TAG, "getMultiWindowDisabled failed");
            return false;
        }
    }

    public static Bundle getHwMultiWindowState() {
        try {
            return getService().getHwMultiWindowState();
        } catch (RemoteException e) {
            Log.e(TAG, "getHwMultiWindowState failed");
            return new Bundle();
        }
    }

    public static void setForegroundFreeFormNum(int num) {
        try {
            getService().setForegroundFreeFormNum(num);
        } catch (RemoteException e) {
            Log.e(TAG, "setForegroundFreeFormNum failed");
        }
    }

    public static Map<String, Boolean> getAppUserAwarenessState(int displayId, List<String> packageNames) {
        try {
            return getService().getAppUserAwarenessState(displayId, packageNames);
        } catch (RemoteException e) {
            Log.e(TAG, "setForegroundFreeFormNum failed");
            return new HashMap();
        }
    }

    public static List<ActivityManager.RecentTaskInfo> getFilteredTasks(int userId, int displayId, String packageName, int[] windowingModes, boolean isIgnoreVisible, int maxNum) {
        try {
            return getService().getFilteredTasks(userId, displayId, packageName, windowingModes, isIgnoreVisible, maxNum);
        } catch (RemoteException e) {
            Log.e(TAG, "getFilteredTasks failed");
            return null;
        }
    }

    public static boolean removeTask(int taskId, IBinder token, String packageName, boolean isRemoveFromRecents, String reason) {
        try {
            return getService().removeTask(taskId, token, packageName, isRemoveFromRecents, reason);
        } catch (RemoteException e) {
            Log.e(TAG, "removeTask failed");
            return false;
        }
    }

    public static void removeTasks(int[] taskIds) {
        try {
            getService().removeTasks(taskIds);
        } catch (RemoteException e) {
            Log.e(TAG, "removeTasks failed");
        }
    }

    public static void toggleFreeformWindowingMode(IBinder appToken, String packageName) {
        try {
            getService().toggleFreeformWindowingMode(appToken, packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "toggleFreeformWindowingMode failed");
        }
    }

    public static boolean setDockCallBackInfo(IHwDockCallBack callBack, int type) {
        try {
            return getService().setDockCallBackInfo(callBack, type);
        } catch (RemoteException e) {
            Log.e(TAG, "setDockCallBasckInfo failed ");
            return false;
        }
    }

    public static int[] startActivitiesFromRecents(int[] taskIds, List<Bundle> options, boolean isDivideSplitScreen, int flag) {
        try {
            return getService().startActivitiesFromRecents(taskIds, options, isDivideSplitScreen, flag);
        } catch (RemoteException e) {
            Log.e(TAG, "startActivitiesFromRecents failed ");
            return new int[0];
        }
    }

    public static Rect resizeActivityStack(IBinder token, Rect bounds, boolean isAlwaysOnTop) {
        try {
            return getService().resizeActivityStack(token, bounds, isAlwaysOnTop);
        } catch (RemoteException e) {
            Log.e(TAG, "resizeActivityStack failed: " + e.getMessage());
            return bounds;
        }
    }

    public static Bundle hookStartActivityOptions(Context context, Bundle options) {
        return hookStartActivityOptionsInPCMultiCastMode(context, hookStartActivityOptionsInPadCastMode(context, options));
    }

    private static Bundle hookStartActivityOptionsInPadCastMode(Context context, Bundle options) {
        if (ActivityThread.currentActivityThread() == null || !ActivityThread.currentActivityThread().hasActivities() || !SystemProperties.getBoolean(PAD_CAST_MODE_KEY, false)) {
            return options;
        }
        ActivityOptions activityOptions = ActivityOptions.fromBundle(options);
        if (activityOptions == null) {
            activityOptions = ActivityOptions.makeBasic();
        }
        if (activityOptions.getLaunchDisplayId() != -1) {
            return options;
        }
        if (context instanceof Activity) {
            activityOptions.setLaunchDisplayId(context.getDisplayId());
        } else {
            activityOptions.setLaunchDisplayId(getActivityDisplayId(Process.myPid(), Process.myUid()));
        }
        return activityOptions.toBundle();
    }

    public static boolean isCastDisplay(String uniqueId, String castType) {
        if (!PAD_CAST.equals(castType) || TextUtils.isEmpty(uniqueId) || !uniqueId.contains("HiSightPCDisplay") || !HwPCUtils.isPadAssistantMode()) {
            return false;
        }
        return true;
    }

    public static int getVirtualDisplayId(String castType) {
        try {
            return getService().getVirtualDisplayId(castType);
        } catch (RemoteException e) {
            Log.e(TAG, "getVirtualDisplayId failed: " + e.getMessage());
            return -1;
        }
    }

    public static boolean moveStacksToDisplay(int fromDisplayId, int toDisplayId, boolean isOnlyFocus) {
        try {
            return getService().moveStacksToDisplay(fromDisplayId, toDisplayId, isOnlyFocus);
        } catch (RemoteException e) {
            Log.e(TAG, "moveStacksToDisplay failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean isDisplayHoldScreen(int displayId) {
        try {
            return getService().isDisplayHoldScreen(displayId);
        } catch (RemoteException e) {
            Log.e(TAG, "isDisplayHoldScreen failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean isPadCastMaxSizeEnable() {
        try {
            return getService().isPadCastMaxSizeEnable();
        } catch (RemoteException e) {
            Log.e(TAG, "isPadCastMaxSizeEnable failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean isMirrorCast(String castType) {
        try {
            return getService().isMirrorCast(castType);
        } catch (RemoteException e) {
            Log.e(TAG, "isMirrorCast failed: " + e.getMessage());
            return true;
        }
    }

    public static int getTopFocusedDisplayId() {
        try {
            return getService().getTopFocusedDisplayId();
        } catch (RemoteException e) {
            Log.e(TAG, "getTopFocusedDisplayId failed: " + e.getMessage());
            return -1;
        }
    }

    public static int getActivityDisplayId(int pid, int uid) {
        try {
            return getService().getActivityDisplayId(pid, uid);
        } catch (RemoteException e) {
            Log.e(TAG, "getDisplayIdForActivityThread failed: " + e.getMessage());
            return 0;
        }
    }

    public static Display getCurrentDisplay(Context context, Display display) {
        int curDisplayId;
        if (display == null || ActivityThread.currentActivityThread() == null || !ActivityThread.currentActivityThread().hasActivities() || !SystemProperties.getBoolean(PAD_CAST_MODE_KEY, false) || display.getDisplayId() == (curDisplayId = getActivityDisplayId(Process.myPid(), Process.myUid()))) {
            return display;
        }
        return ResourcesManager.getInstance().getAdjustedDisplay(curDisplayId, context.getResources());
    }

    public static void setStackWindowingMode(IBinder token, int windowingMode, Rect bounds) {
        try {
            getService().setStackWindowingMode(token, windowingMode, bounds);
        } catch (RemoteException e) {
            Log.e(TAG, "setStackWindowingMode failed: " + e.getMessage());
        }
    }

    public static boolean isStatusBarPermenantlyShowing() {
        try {
            return getService().isStatusBarPermenantlyShowing();
        } catch (RemoteException e) {
            Log.e(TAG, "get isStatusBarPermenantlyShowing failed ");
            return true;
        }
    }

    public static Bundle getActivityOptionFromAppProcess(IApplicationThread appthread) {
        try {
            return getService().getActivityOptionFromAppProcess(appthread);
        } catch (RemoteException e) {
            Log.e(TAG, "getTaskRecordInfo failed: catch RemoteException!");
            return null;
        }
    }

    public static void moveTaskToFrontForMultiDisplay(int taskId) {
        try {
            getService().moveTaskToFrontForMultiDisplay(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "moveTaskToFrontForMultiDisplay failed: catch RemoteException!");
        }
    }

    public static void moveTaskBackwardsForMultiDisplay(int taskId) {
        try {
            getService().moveTaskBackwardsForMultiDisplay(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "moveTaskBackwardsForMultiDisplay failed: catch RemoteException!");
        }
    }

    public static void hwResizeTaskForMultiDisplay(int taskId, Rect bounds) {
        try {
            getService().hwResizeTaskForMultiDisplay(taskId, bounds);
        } catch (RemoteException e) {
            Log.e(TAG, "hwResizeTaskForMultiDisplay failed", e);
        }
    }

    public static void setFocusedTaskForMultiDisplay(int taskId) {
        try {
            getService().setFocusedTaskForMultiDisplay(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "setFocusedTaskForMultiDisplay failed: catch RemoteException!");
        }
    }

    public static void setCurOrientation(int curOrientation) {
        try {
            getService().setCurOrientation(curOrientation);
        } catch (RemoteException e) {
            Log.e(TAG, "setCurOrientation failed: catch RemoteException!");
        }
    }

    public static void setPCFullSize(int fullWidth, int fullHeight, int phoneOrientation) {
        try {
            getService().setPCFullSize(fullWidth, fullHeight, phoneOrientation);
        } catch (RemoteException e) {
            Log.e(TAG, "setPCFullSize failed: catch RemoteException!");
        }
    }

    public static void setPCVirtualSize(int virtualWidth, int virtualHeight, int phoneOrientation) {
        try {
            getService().setPCVirtualSize(virtualWidth, virtualHeight, phoneOrientation);
        } catch (RemoteException e) {
            Log.e(TAG, "setPCVirtualSize failed: catch RemoteException!");
        }
    }

    public static void setPCMultiCastMode(boolean isPCMultiCastMode) {
        try {
            getService().setPCMultiCastMode(isPCMultiCastMode);
        } catch (RemoteException e) {
            Log.e(TAG, "setPCMultiCastMode failed: catch RemoteException!");
        }
    }

    public static int getPCVirtualWidth() {
        try {
            return getService().getPCVirtualWidth();
        } catch (RemoteException e) {
            Log.e(TAG, "getPCVirtualWidth failed: catch RemoteException!");
            return 0;
        }
    }

    public static int getPCVirtualHeight() {
        try {
            return getService().getPCVirtualHeight();
        } catch (RemoteException e) {
            Log.e(TAG, "getPCVirtualHeight failed: catch RemoteException!");
            return 0;
        }
    }

    public static int getPCFullWidth() {
        try {
            return getService().getPCFullWidth();
        } catch (RemoteException e) {
            Log.e(TAG, "getPCFullWidth failed: catch RemoteException!");
            return 0;
        }
    }

    public static int getPCFullHeight() {
        try {
            return getService().getPCFullHeight();
        } catch (RemoteException e) {
            Log.e(TAG, "getPCFullHeight failed: catch RemoteException!");
            return 0;
        }
    }

    public static void registerMultiDisplayMessenger(Messenger messenger) {
        try {
            getService().registerMultiDisplayMessenger(messenger);
        } catch (RemoteException e) {
            Log.e(TAG, "registHwSystemUIController failed: catch RemoteException!");
        }
    }

    public static void unregisterMultiDisplayMessenger(Messenger messenger) {
        try {
            getService().unregisterMultiDisplayMessenger(messenger);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterMultiDisplayMessenger failed: catch RemoteException!");
        }
    }

    public static void hwTogglePhoneFullScreen(int taskId) {
        try {
            getService().hwTogglePhoneFullScreen(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "hwTogglePhoneFullScreen failed: catch RemoteException!");
        }
    }

    public static void hwTogglePCFloatWindow(int taskId) {
        try {
            getService().hwTogglePCFloatWindow(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "hwTogglePCFloatWindow failed: catch RemoteException!");
        }
    }

    public static List<Bundle> getTaskList() {
        try {
            return getService().getTaskList();
        } catch (RemoteException e) {
            Log.e(TAG, "getTaskList failed: catch RemoteException!");
            return new ArrayList();
        }
    }

    public static float getStackScale(int taskId) {
        try {
            return getService().getStackScale(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "getStackScale failed ");
            return 1.0f;
        }
    }

    public static int getCurTopFullScreenTaskState() {
        try {
            return getService().getCurTopFullScreenTaskState();
        } catch (RemoteException e) {
            Log.e(TAG, "getCurTopFullScreenTaskState failed: catch RemoteException!");
            return -1;
        }
    }

    public static int getCurPCWindowAreaNum() {
        try {
            return getService().getCurPCWindowAreaNum();
        } catch (RemoteException e) {
            Log.e(TAG, "getCurPCWindowAreaNum failed: catch RemoteException!");
            return -1;
        }
    }

    public static List<Bundle> getLastRencentTaskList() {
        try {
            return getService().getLastRencentTaskList();
        } catch (RemoteException e) {
            Log.e(TAG, "getLastRencentTaskList failed: catch RemoteException!");
            return new ArrayList();
        }
    }

    public static int retrievePCMultiWinConfig(String configXml) {
        Log.d(TAG, "retrievePCMultiWinConfig: " + configXml);
        try {
            return getService().retrievePCMultiWinConfig(configXml);
        } catch (RemoteException e) {
            Log.e(TAG, "retrievePCMultiWinConfig failed: catch RemoteException!");
            return 1;
        }
    }

    public static void setPcSize(int pcWidth, int pcHeight) {
        try {
            getService().setPcSize(pcWidth, pcHeight);
        } catch (RemoteException e) {
            Log.e(TAG, "setPcSize failed: catch RemoteException!");
        }
    }

    public static int getPcWidth() {
        try {
            return getService().getPcWidth();
        } catch (RemoteException e) {
            Log.e(TAG, "getPcWidth failed: catch RemoteException!");
            return 0;
        }
    }

    public static int getPcHeight() {
        try {
            return getService().getPcHeight();
        } catch (RemoteException e) {
            Log.e(TAG, "getPcHeight failed: catch RemoteException!");
            return 0;
        }
    }

    public static void setMultiDisplayParamsWithType(int type, Bundle bundle) {
        try {
            getService().setMultiDisplayParamsWithType(type, bundle);
        } catch (RemoteException e) {
            Log.e(TAG, "setMultiDisplayParamsWithType failed: catch RemoteException!");
        }
    }

    public static Rect getLocalLayerRectForMultiDisplay() {
        try {
            return getService().getLocalLayerRectForMultiDisplay();
        } catch (RemoteException e) {
            Log.e(TAG, "getLocalLayerRectForMultiDisplay failed: " + e.getMessage());
            return new Rect();
        }
    }

    public static Rect getLocalDisplayRectForMultiDisplay() {
        try {
            return getService().getLocalDisplayRectForMultiDisplay();
        } catch (RemoteException e) {
            Log.e(TAG, "getLocalDisplayRectForMultiDisplay failed: " + e.getMessage());
            return new Rect();
        }
    }

    public static Rect getVirtualLayerRectForMultiDisplay() {
        try {
            return getService().getVirtualLayerRectForMultiDisplay();
        } catch (RemoteException e) {
            Log.e(TAG, "getVirtualLayerRectForMultiDisplay failed: " + e.getMessage());
            return new Rect();
        }
    }

    public static Rect getVirtualDisplayRectForMultiDisplay() {
        try {
            return getService().getVirtualDisplayRectForMultiDisplay();
        } catch (RemoteException e) {
            Log.e(TAG, "getVirtualDisplayRectForMultiDisplay failed: " + e.getMessage());
            return new Rect();
        }
    }

    public static int getRogWidth() {
        return SystemProperties.getInt("persist.sys.rog.width", 0);
    }

    public static int getRogHeight() {
        return SystemProperties.getInt("persist.sys.rog.height", 0);
    }

    public static void adjustConfigForPCCast(Configuration config) {
        Rect bounds;
        if (config != null && config.windowConfiguration.inHwPCFreeFormWindowingMode() && isPCMultiCastMode() && (bounds = config.windowConfiguration.getBounds()) != null && !bounds.isEmpty()) {
            double density = (double) (((float) config.densityDpi) * 0.00625f);
            if (bounds.width() > bounds.height()) {
                config.orientation = 2;
                config.windowConfiguration.setRotation(1);
            } else {
                config.orientation = 1;
                config.windowConfiguration.setRotation(0);
            }
            int width = (int) (((double) bounds.width()) / density);
            config.screenWidthDp = width;
            config.compatScreenWidthDp = width;
            int height = (int) (((double) bounds.height()) / density);
            config.screenHeightDp = height;
            config.compatScreenHeightDp = height;
        }
    }

    public static boolean isPCMultiCastMode() {
        return SystemProperties.getBoolean(PC_CAST_MODE_KEY, false);
    }

    public static void adjustRectForPCCast(String uniqueId, Rect layerStackRect, Rect displayRect, Rect currentLayerStackRect, Rect currentDisplayRect) {
        if (isPCMultiCastMode()) {
            int i = 1;
            if (!HwFoldScreenManager.isFoldable() || !HwFoldScreenState.isOutFoldDevice() || HwFoldScreenManager.getDisplayMode() == 1) {
                if (uniqueId.equals("local:0")) {
                    if (displayRect.width() < displayRect.height()) {
                        i = 0;
                    }
                    setCurOrientation(i);
                }
                if (uniqueId.contains("HiSightPCDisplay")) {
                    Rect virtualLayerRect = getVirtualLayerRectForMultiDisplay();
                    Rect virtualDisplayRect = getVirtualDisplayRectForMultiDisplay();
                    if (!virtualLayerRect.isEmpty() && !virtualDisplayRect.isEmpty()) {
                        layerStackRect.set(virtualLayerRect);
                        displayRect.set(virtualDisplayRect);
                        currentLayerStackRect.set(virtualLayerRect);
                        currentDisplayRect.set(virtualDisplayRect);
                    }
                    Log.i(TAG, "setRectForMultiDisplay new virtual mCurrentLayerStackRect=" + currentLayerStackRect + " mCurrentDisplayRect =" + currentDisplayRect + "layerStackRect =" + layerStackRect + "displayRect =" + displayRect);
                }
                if (uniqueId.contains("local:0")) {
                    Rect localLayerRect = getLocalLayerRectForMultiDisplay();
                    Rect localDisplayRect = getLocalDisplayRectForMultiDisplay();
                    if (!localLayerRect.isEmpty() && !localDisplayRect.isEmpty()) {
                        layerStackRect.set(localLayerRect);
                        displayRect.set(localDisplayRect);
                        currentLayerStackRect.set(localLayerRect);
                        currentDisplayRect.set(localDisplayRect);
                    }
                    Log.i(TAG, "setRectForMultiDisplay new local mCurrentLayerStackRect=" + currentLayerStackRect + "mCurrentDisplayRect =" + currentDisplayRect + " layerStackRect = " + layerStackRect + " displayRect = " + displayRect);
                }
            }
        }
    }

    public static boolean adjustParamsForPCCast(View view, WindowManager.LayoutParams params, boolean modeChange) {
        Configuration config;
        if (view == null || view.getContext() == null || view.getContext().getResources() == null || params == null || params.type < 1000 || params.type > 2999 || (config = view.getContext().getResources().getConfiguration()) == null) {
            return false;
        }
        if ((!config.windowConfiguration.inHwPCFreeFormWindowingMode() && !modeChange) || config.equals(Configuration.EMPTY)) {
            return false;
        }
        params.mOverrideDisplayFrame.set(config.windowConfiguration.getAppBounds());
        return true;
    }

    private static Bundle hookStartActivityOptionsInPCMultiCastMode(Context context, Bundle options) {
        IApplicationThread caller;
        if (!isPCMultiCastMode() || (caller = context.getIApplicationThread()) == null) {
            return options;
        }
        int callerAppWindowMode = 0;
        Rect callerRect = null;
        Bundle taskRecordBundle = getActivityOptionFromAppProcess(caller);
        if (taskRecordBundle != null) {
            callerAppWindowMode = taskRecordBundle.getInt(KEY_WINDOW_MODE);
            if (callerAppWindowMode != 105) {
                return options;
            }
            callerRect = (Rect) taskRecordBundle.getParcelable(KEY_WINDOW_BOUND);
        }
        ActivityOptions activityOptions = ActivityOptions.fromBundle(options);
        if (activityOptions == null) {
            activityOptions = ActivityOptions.makeBasic();
        }
        Log.i(TAG, "hook multi display activity options callerApp != null, callerAppWindowMode: " + callerAppWindowMode + ",caller app rect : " + callerRect);
        if (callerRect != null) {
            activityOptions.setLaunchBounds(callerRect);
        }
        if (callerAppWindowMode != 0) {
            activityOptions.setLaunchWindowingMode(callerAppWindowMode);
        }
        return activityOptions.toBundle();
    }

    public static Rect adjustScreenShotRectForPCCast(Rect sourceCrop) {
        if (!isPCMultiCastMode()) {
            return sourceCrop;
        }
        try {
            return getService().adjustScreenShotRectForPCCast(sourceCrop);
        } catch (RemoteException e) {
            Log.e(TAG, "adjustScreenShotRectForPCCast failed.");
            return sourceCrop;
        }
    }

    public static void hwSetRequestedOrientation(int taskId, int requestedOrientation) {
        if (isPCMultiCastMode()) {
            try {
                getService().hwSetRequestedOrientation(taskId, requestedOrientation);
            } catch (RemoteException e) {
                Log.e(TAG, "hwSetRequestedOrientation failed.");
            }
        }
    }

    public static void updateFloatingBallPos(Rect pos) {
        try {
            getService().updateFloatingBallPos(pos);
        } catch (RemoteException e) {
            Log.e(TAG, "updateFloatingBallPos failed ");
        }
    }

    public static boolean minimizeHwFreeForm(IBinder token, String packageName, boolean nonRoot) {
        try {
            return getService().minimizeHwFreeForm(token, packageName, nonRoot);
        } catch (RemoteException e) {
            Log.e(TAG, "minimizeHwFreeForm failed ");
            return false;
        }
    }

    public static boolean setStackScale(int taskId, float scale) {
        try {
            return getService().setStackScale(taskId, scale);
        } catch (RemoteException e) {
            Log.e(TAG, "setStackScale failed ");
            return false;
        }
    }

    public static void notifyCameraStateForAtms(Bundle options) {
        try {
            getService().notifyCameraStateForAtms(options);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyCameraStateForAtms fail with RemoteException");
        }
    }

    public static boolean isAdjustConfig(Configuration config) {
        if (!IS_HW_MULTIWINDOW_APPCOMPACT_SUPPORTED) {
            return false;
        }
        if (!(config == null || config.windowConfiguration.getAppBounds() == null)) {
            if (config.windowConfiguration.inHwFreeFormWindowingMode() || config.windowConfiguration.inHwMagicWindowingMode()) {
                return true;
            }
            if (!(config.windowConfiguration.getWindowingMode() != 1 || config.windowConfiguration.getBounds() == null || (config.windowConfiguration.getBounds().left == 0 && config.windowConfiguration.getBounds().top == 0))) {
                return true;
            }
        }
        return false;
    }

    public static void notifyLauncherAction(String category, Bundle bundle) {
        try {
            getService().notifyLauncherAction(category, bundle);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyLauncherAction failed");
        }
    }

    public static int getDisplayEdge(Display at) {
        int rogWidth = SystemProperties.getInt("persist.sys.rog.width", 0);
        int rogHeight = SystemProperties.getInt("persist.sys.rog.height", 0);
        if (at != null) {
            DisplayMetrics metric = new DisplayMetrics();
            at.getRealMetrics(metric);
            rogWidth = metric.widthPixels;
            rogHeight = metric.heightPixels;
            Slog.i(TAG, "use default display metric : " + metric);
        }
        Slog.i(TAG, "get rog width:" + rogWidth + ", rog height : " + rogHeight);
        return Math.max(rogWidth, rogHeight);
    }

    public static void adjustGlobalConfigIfNeeded(Configuration config) {
        if (config != null && ActivityThread.currentApplication() != null && ActivityThread.currentApplication().getResources() != null) {
            Configuration oldConfiguration = ActivityThread.currentApplication().getResources().getConfiguration();
            if (oldConfiguration.windowConfiguration.inHwPCFreeFormWindowingMode() && !config.windowConfiguration.inHwPCFreeFormWindowingMode()) {
                config.setMultiWindowConfigTo(oldConfiguration);
            }
        }
    }

    public static boolean isSupportsHwFreeForm(ActivityInfo activityInfo) {
        return isResizableApp(activityInfo);
    }

    public static Bitmap getApplicationIcon(IBinder activityToken, boolean isCheckAppLock) {
        try {
            return getService().getApplicationIcon(activityToken, isCheckAppLock);
        } catch (RemoteException e) {
            Log.e(TAG, "getApplicationIcon failed");
            return null;
        }
    }

    public static boolean isSupportDragToSplitScreen(IBinder token, boolean isCheckAppLock) {
        try {
            return getService().isSupportDragToSplitScreen(token, isCheckAppLock);
        } catch (RemoteException e) {
            Log.e(TAG, "isSupportDragToSplitScreen failed");
            return false;
        }
    }

    public static List<String> getVisibleCanShowWhenLockedPackages(int displayId) {
        try {
            return getService().getVisibleCanShowWhenLockedPackages(displayId);
        } catch (RemoteException e) {
            Log.e(TAG, "getVisibleCanShowWhenLockedPackages failed");
            return new ArrayList();
        }
    }

    public static Bundle getFreeformBoundsInCenter(int displayId, int centerX) {
        try {
            return getService().getFreeformBoundsInCenter(displayId, centerX);
        } catch (RemoteException e) {
            Log.e(TAG, "getFreeformBoundsInCenter RemoteException");
            return null;
        }
    }

    public static void notifyNotificationAnimationFinish(int displayId) {
        try {
            getService().notifyNotificationAnimationFinish(displayId);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyNotificationAnimationFinish RemoteException");
        }
    }
}
