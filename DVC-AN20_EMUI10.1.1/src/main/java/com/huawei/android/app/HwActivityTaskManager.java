package com.huawei.android.app;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.HwRecentTaskInfo;
import android.app.IActivityController;
import android.app.IHwActivityNotifier;
import android.app.ITaskStackListener;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.RemoteException;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.app.IHwActivityTaskManager;
import java.util.ArrayList;
import java.util.List;

public class HwActivityTaskManager {
    public static final String HW_MULTIWINDOW_FREEFORM_FLOATIME = "float_ime_state";
    public static final String HW_MULTIWINDOW_IMETARGET_RECT = "ime_target_rect";
    public static final String HW_MULTIWINDOW_SPLITWIN_LEFTRIGHT = "is_leftright_split";
    public static final String HW_MUTILWINDOW_BLACKLIST_APP = "blacklist";
    public static final String HW_MUTILWINDOW_RECOMLIST_APP = "recomlist";
    public static final String HW_MUTILWINDOW_WHITELIST_APP = "whitelist";
    public static final String HW_SPLIT_SCREEN_PRIMARY_BOUNDS = "primaryBounds";
    public static final int HW_SPLIT_SCREEN_PRIMARY_EITHER = -1;
    public static final int HW_SPLIT_SCREEN_PRIMARY_LEFT = 1;
    public static final String HW_SPLIT_SCREEN_PRIMARY_POSITION = "primaryPosition";
    public static final int HW_SPLIT_SCREEN_PRIMARY_TOP = 0;
    public static final int HW_SPLIT_SCREEN_RATIO_DEFAULT = 0;
    public static final int HW_SPLIT_SCREEN_RATIO_PRAIMARY_LESS_THAN_DEFAULT = 1;
    public static final int HW_SPLIT_SCREEN_RATIO_PRAIMARY_MORE_THAN_DEFAULT = 2;
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
    private static final String TAG = "HwActivityTaskManager";

    public static IHwActivityTaskManager getService() {
        return IActivityTaskManagerSingleton.get();
    }

    public static void registerHwActivityNotifier(IHwActivityNotifier notifier, String reason) {
        if (notifier != null) {
            try {
                getService().registerHwActivityNotifier(notifier, reason);
            } catch (RemoteException e) {
                Log.e(TAG, "registerHwActivityNotifier failed", e);
            }
        }
    }

    public static void unregisterHwActivityNotifier(IHwActivityNotifier notifier) {
        if (notifier != null) {
            try {
                getService().unregisterHwActivityNotifier(notifier);
            } catch (RemoteException e) {
                Log.e(TAG, "unregisterHwActivityNotifier failed", e);
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
            Log.e(TAG, "getLastResumedActivity failed", e);
            return null;
        }
    }

    public static void registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback callback) {
        if (callback != null) {
            try {
                getService().registerAtmDAMonitorCallback(callback);
            } catch (RemoteException e) {
                Log.e(TAG, "registerAtmDAMonitorCallback failed", e);
            }
        }
    }

    public static boolean isInMultiWindowMode() {
        try {
            return getService().isInMultiWindowMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isInMultiWindowMode failed", e);
            return false;
        }
    }

    public static boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) {
        try {
            return getService().registerThirdPartyCallBack(aCallBackHandler);
        } catch (RemoteException e) {
            Log.e(TAG, "registerThirdPartyCallBack failed: catch RemoteException!");
            return false;
        }
    }

    public static boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) {
        try {
            return getService().unregisterThirdPartyCallBack(aCallBackHandler);
        } catch (RemoteException e) {
            Log.e(TAG, "registerThirdPartyCallBack failed: catch RemoteException!");
            return false;
        }
    }

    public static void setWarmColdSwitch(boolean enable) {
        if (getService() != null) {
            long token = Binder.clearCallingIdentity();
            try {
                getService().setWarmColdSwitch(enable);
            } catch (RemoteException e) {
                Log.e(TAG, "setWarmColdSwitch failed: catch RemoteException!");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public static int getTopTaskIdInDisplay(int displayId, String pkgName, boolean invisibleAlso) {
        try {
            return getService().getTopTaskIdInDisplay(displayId, pkgName, invisibleAlso);
        } catch (RemoteException e) {
            Log.e(TAG, "getTopTaskIdInDisplay failed", e);
            return -1;
        }
    }

    public static boolean isTaskSupportResize(int taskId, boolean isFullscreen, boolean isMaximized) {
        try {
            return getService().isTaskSupportResize(taskId, isFullscreen, isMaximized);
        } catch (RemoteException e) {
            Log.e(TAG, "isTaskSupportResize failed", e);
            return false;
        }
    }

    public static Rect getPCTopTaskBounds(int displayId) {
        try {
            return getService().getPCTopTaskBounds(displayId);
        } catch (RemoteException e) {
            Log.e(TAG, "getPCTopTaskBounds failed", e);
            return null;
        }
    }

    public static void hwRestoreTask(int taskId, float xPos, float yPos) {
        try {
            getService().hwRestoreTask(taskId, xPos, yPos);
        } catch (RemoteException e) {
            Log.e(TAG, "hwRestoreTask failed", e);
        }
    }

    public static void hwResizeTask(int taskId, Rect bounds) {
        try {
            getService().hwResizeTask(taskId, bounds);
        } catch (RemoteException e) {
            Log.e(TAG, "hwResizeTask failed", e);
        }
    }

    public static int getWindowState(IBinder token) {
        try {
            return getService().getWindowState(token);
        } catch (RemoteException e) {
            Log.e(TAG, "getWindowState failed", e);
            return -1;
        }
    }

    public static HwRecentTaskInfo getHwRecentTaskInfo(int taskId) {
        try {
            return getService().getHwRecentTaskInfo(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "getHwRecentTaskInfo failed", e);
            return null;
        }
    }

    public static void togglePCMode(boolean pcMode, int displayId) {
        try {
            getService().togglePCMode(pcMode, displayId);
        } catch (RemoteException e) {
            Log.e(TAG, "togglePCMode failed", e);
        }
    }

    public static void toggleHome() {
        try {
            getService().toggleHome();
        } catch (RemoteException e) {
            Log.e(TAG, "toggleHome failed", e);
        }
    }

    public static void unRegisterHwTaskStackListener(ITaskStackListener listener) {
        try {
            getService().unRegisterHwTaskStackListener(listener);
        } catch (RemoteException e) {
            Log.e(TAG, "unRegisterHwTaskStackListener failed", e);
        }
    }

    public static void unRegisterHwTaskStackListener(TaskStackListenerEx listenerEx) {
        try {
            getService().unRegisterHwTaskStackListener(listenerEx.getTaskStackListener());
        } catch (RemoteException e) {
            Log.e(TAG, "unRegisterHwTaskStackListener failed", e);
        }
    }

    public static void registerHwTaskStackListener(ITaskStackListener listener) {
        try {
            getService().registerHwTaskStackListener(listener);
        } catch (RemoteException e) {
            Log.e(TAG, "registerHwTaskStackListener failed", e);
        }
    }

    public static void registerHwTaskStackListener(TaskStackListenerEx listenerEx) {
        try {
            getService().registerHwTaskStackListener(listenerEx.getTaskStackListener());
        } catch (RemoteException e) {
            Log.e(TAG, "registerHwTaskStackListener failed", e);
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
            Log.e(TAG, "requestContentNode failed", e);
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
            Log.e(TAG, "requestContentOther failed", e);
            return false;
        }
    }

    public static boolean addGameSpacePackageList(List<String> packageList) {
        try {
            return getService().addGameSpacePackageList(packageList);
        } catch (RemoteException e) {
            Log.e(TAG, "addGameSpacePackageList failed", e);
            return false;
        }
    }

    public static boolean delGameSpacePackageList(List<String> packageList) {
        try {
            return getService().delGameSpacePackageList(packageList);
        } catch (RemoteException e) {
            Log.e(TAG, "delGameSpacePackageList failed", e);
            return false;
        }
    }

    public static void registerGameObserver(IGameObserver observer) {
        try {
            getService().registerGameObserver(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "registerGameObserver failed", e);
        }
    }

    public static void unregisterGameObserver(IGameObserver observer) {
        try {
            getService().unregisterGameObserver(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterGameObserver failed", e);
        }
    }

    public static void registerGameObserverEx(IGameObserverEx observer) {
        try {
            getService().registerGameObserverEx(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "registerGameObserverEx failed", e);
        }
    }

    public static void unregisterGameObserverEx(IGameObserverEx observer) {
        try {
            getService().unregisterGameObserverEx(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterGameObserverEx failed", e);
        }
    }

    public static boolean isInGameSpace(String packageName) {
        try {
            return getService().isInGameSpace(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "isInGameSpace failed", e);
            return false;
        }
    }

    public static List<String> getGameList() {
        try {
            return getService().getGameList();
        } catch (RemoteException e) {
            Log.e(TAG, "getGameList failed", e);
            return null;
        }
    }

    public static boolean isGameDndOn() {
        try {
            return getService().isGameDndOn();
        } catch (RemoteException e) {
            Log.e(TAG, "isGameDndOn failed", e);
            return false;
        }
    }

    public static boolean isGameDndOnEx() {
        try {
            return getService().isGameDndOnEx();
        } catch (RemoteException e) {
            Log.e(TAG, "isGameDndOnEx failed", e);
            return false;
        }
    }

    public static boolean isGameKeyControlOn() {
        try {
            return getService().isGameKeyControlOn();
        } catch (RemoteException e) {
            Log.e(TAG, "isGameKeyControlOn failed", e);
            return false;
        }
    }

    public static boolean isGameGestureDisabled() {
        try {
            return getService().isGameGestureDisabled();
        } catch (RemoteException e) {
            Log.e(TAG, "isGameGestureDisabled failed", e);
            return false;
        }
    }

    public static boolean isFreeFormVisible() {
        try {
            return getService().isFreeFormVisible();
        } catch (RemoteException e) {
            Log.e(TAG, "isFreeFormVisible failed", e);
            return false;
        }
    }

    public static boolean isTaskVisible(int id) {
        try {
            return getService().isTaskVisible(id);
        } catch (RemoteException e) {
            Log.e(TAG, "isTaskVisible failed", e);
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

    public static ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) {
        try {
            return getService().getTaskSnapshot(taskId, reducedResolution);
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
            Log.e(TAG, "handleMultiWindowSwitch failed", e);
        }
    }

    public static Bundle getSplitStacksPos(int displayId, int splitRatio) {
        try {
            return getService().getSplitStacksPos(displayId, splitRatio);
        } catch (RemoteException e) {
            Log.e(TAG, "getHwSplitStacksPos failed", e);
            return null;
        }
    }

    public static boolean enterCoordinationMode(Intent intent) {
        try {
            return getService().enterCoordinationMode(intent);
        } catch (RemoteException e) {
            Log.e(TAG, "enterCoordinationMode failed", e);
            return false;
        }
    }

    public static boolean exitCoordinationMode(boolean toTop) {
        try {
            return getService().exitCoordinationMode(toTop);
        } catch (RemoteException e) {
            Log.e(TAG, "exitCoordinationMode failed", e);
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
            Log.e(TAG, "check isSupportDragForMultiWin failed", e);
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

    public static boolean setMultiWindowDisabled(boolean disabled) {
        try {
            return getService().setMultiWindowDisabled(disabled);
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
            Log.e(TAG, "getHwMultiWindowState failed: " + e.getMessage());
            return new Bundle();
        }
    }
}
