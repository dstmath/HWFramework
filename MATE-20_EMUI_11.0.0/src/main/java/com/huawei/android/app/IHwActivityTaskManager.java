package com.huawei.android.app;

import android.app.ActivityManager;
import android.app.HwRecentTaskInfo;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.IHwActivityNotifier;
import android.app.IHwDockCallBack;
import android.app.ITaskStackListener;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IMWThirdpartyCallback;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.app.IGameObserverEx;
import com.huawei.android.app.IHwAtmDAMonitorCallback;
import java.util.List;
import java.util.Map;

public interface IHwActivityTaskManager extends IInterface {
    boolean addGameSpacePackageList(List<String> list) throws RemoteException;

    Rect adjustScreenShotRectForPCCast(Rect rect) throws RemoteException;

    boolean checkTaskId(int i) throws RemoteException;

    boolean delGameSpacePackageList(List<String> list) throws RemoteException;

    void dismissSplitScreenToFocusedStack() throws RemoteException;

    boolean enterCoordinationMode(Intent intent) throws RemoteException;

    boolean exitCoordinationMode(boolean z) throws RemoteException;

    int getActivityDisplayId(int i, int i2) throws RemoteException;

    Bundle getActivityOptionFromAppProcess(IApplicationThread iApplicationThread) throws RemoteException;

    ActivityManager.TaskSnapshot getActivityTaskSnapshot(IBinder iBinder, boolean z) throws RemoteException;

    int getActivityWindowMode(IBinder iBinder) throws RemoteException;

    Map getAppUserAwarenessState(int i, List<String> list) throws RemoteException;

    Bitmap getApplicationIcon(IBinder iBinder, boolean z) throws RemoteException;

    int getCaptionState(IBinder iBinder) throws RemoteException;

    int getCurPCWindowAreaNum() throws RemoteException;

    int getCurTopFullScreenTaskState() throws RemoteException;

    List<ActivityManager.RecentTaskInfo> getFilteredTasks(int i, int i2, String str, int[] iArr, boolean z, int i3) throws RemoteException;

    Bundle getFreeformBoundsInCenter(int i, int i2) throws RemoteException;

    List<String> getGameList() throws RemoteException;

    Bundle getHwMultiWindowAppControlLists() throws RemoteException;

    Bundle getHwMultiWindowState() throws RemoteException;

    HwRecentTaskInfo getHwRecentTaskInfo(int i) throws RemoteException;

    List<Bundle> getLastRencentTaskList() throws RemoteException;

    ActivityInfo getLastResumedActivity() throws RemoteException;

    Rect getLocalDisplayRectForMultiDisplay() throws RemoteException;

    Rect getLocalLayerRectForMultiDisplay() throws RemoteException;

    boolean getMultiWindowDisabled() throws RemoteException;

    int getPCFullHeight() throws RemoteException;

    int getPCFullWidth() throws RemoteException;

    Rect getPCTopTaskBounds(int i) throws RemoteException;

    int getPCVirtualHeight() throws RemoteException;

    int getPCVirtualWidth() throws RemoteException;

    int getPcHeight() throws RemoteException;

    int getPcWidth() throws RemoteException;

    Bundle getSplitStacksPos(int i, int i2) throws RemoteException;

    float getStackScale(int i) throws RemoteException;

    List<Bundle> getTaskList() throws RemoteException;

    ActivityManager.TaskSnapshot getTaskSnapshot(int i, boolean z) throws RemoteException;

    Bitmap getTaskThumbnailOnPCMode(int i) throws RemoteException;

    Bundle getTopActivity() throws RemoteException;

    int getTopFocusedDisplayId() throws RemoteException;

    int getTopTaskIdInDisplay(int i, String str, boolean z) throws RemoteException;

    int getVirtualDisplayId(String str) throws RemoteException;

    Rect getVirtualDisplayRectForMultiDisplay() throws RemoteException;

    Rect getVirtualLayerRectForMultiDisplay() throws RemoteException;

    List<String> getVisibleCanShowWhenLockedPackages(int i) throws RemoteException;

    List<String> getVisiblePackages() throws RemoteException;

    List<ActivityManager.RunningTaskInfo> getVisibleTasks() throws RemoteException;

    int getWindowState(IBinder iBinder) throws RemoteException;

    void handleMultiWindowSwitch(IBinder iBinder, Bundle bundle) throws RemoteException;

    void hwResizeTask(int i, Rect rect) throws RemoteException;

    void hwResizeTaskForMultiDisplay(int i, Rect rect) throws RemoteException;

    void hwRestoreTask(int i, float f, float f2) throws RemoteException;

    void hwSetRequestedOrientation(int i, int i2) throws RemoteException;

    void hwTogglePCFloatWindow(int i) throws RemoteException;

    void hwTogglePhoneFullScreen(int i) throws RemoteException;

    boolean isDisplayHoldScreen(int i) throws RemoteException;

    boolean isFreeFormVisible() throws RemoteException;

    boolean isFullScreen(IBinder iBinder) throws RemoteException;

    boolean isGameDndOn() throws RemoteException;

    boolean isGameDndOnEx() throws RemoteException;

    boolean isGameGestureDisabled() throws RemoteException;

    boolean isGameKeyControlOn() throws RemoteException;

    boolean isInGameSpace(String str) throws RemoteException;

    boolean isInMultiWindowMode() throws RemoteException;

    boolean isMirrorCast(String str) throws RemoteException;

    boolean isNeedAdapterCaptionView(String str) throws RemoteException;

    boolean isPadCastMaxSizeEnable() throws RemoteException;

    boolean isResizableApp(ActivityInfo activityInfo) throws RemoteException;

    boolean isStatusBarPermenantlyShowing() throws RemoteException;

    boolean isSupportDragForMultiWin(IBinder iBinder) throws RemoteException;

    boolean isSupportDragToSplitScreen(IBinder iBinder, boolean z) throws RemoteException;

    boolean isSupportsSplitScreenWindowingMode(IBinder iBinder) throws RemoteException;

    boolean isTaskSupportResize(int i, boolean z, boolean z2) throws RemoteException;

    boolean isTaskVisible(int i) throws RemoteException;

    boolean minimizeHwFreeForm(IBinder iBinder, String str, boolean z) throws RemoteException;

    boolean moveStacksToDisplay(int i, int i2, boolean z) throws RemoteException;

    void moveTaskBackwards(int i) throws RemoteException;

    void moveTaskBackwardsForMultiDisplay(int i) throws RemoteException;

    void moveTaskToFrontForMultiDisplay(int i) throws RemoteException;

    void notifyCameraStateForAtms(Bundle bundle) throws RemoteException;

    void notifyLauncherAction(String str, Bundle bundle) throws RemoteException;

    void notifyNotificationAnimationFinish(int i) throws RemoteException;

    void onCaptionDropAnimationDone(IBinder iBinder) throws RemoteException;

    void registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback iHwAtmDAMonitorCallback) throws RemoteException;

    void registerGameObserver(IGameObserver iGameObserver) throws RemoteException;

    void registerGameObserverEx(IGameObserverEx iGameObserverEx) throws RemoteException;

    void registerHwActivityNotifier(IHwActivityNotifier iHwActivityNotifier, String str) throws RemoteException;

    void registerHwTaskStackListener(ITaskStackListener iTaskStackListener) throws RemoteException;

    void registerMultiDisplayMessenger(Messenger messenger) throws RemoteException;

    boolean registerThirdPartyCallBack(IMWThirdpartyCallback iMWThirdpartyCallback) throws RemoteException;

    boolean removeTask(int i, IBinder iBinder, String str, boolean z, String str2) throws RemoteException;

    void removeTasks(int[] iArr) throws RemoteException;

    boolean requestContentNode(ComponentName componentName, Bundle bundle, int i) throws RemoteException;

    boolean requestContentOther(ComponentName componentName, Bundle bundle, int i) throws RemoteException;

    Rect resizeActivityStack(IBinder iBinder, Rect rect, boolean z) throws RemoteException;

    int retrievePCMultiWinConfig(String str) throws RemoteException;

    void saveMultiWindowTipState(String str, int i) throws RemoteException;

    void setCurOrientation(int i) throws RemoteException;

    boolean setCustomActivityController(IActivityController iActivityController) throws RemoteException;

    boolean setDockCallBackInfo(IHwDockCallBack iHwDockCallBack, int i) throws RemoteException;

    void setFocusedTaskForMultiDisplay(int i) throws RemoteException;

    void setForegroundFreeFormNum(int i) throws RemoteException;

    int[] setFreeformStackVisibility(int i, int[] iArr, boolean z) throws RemoteException;

    void setMultiDisplayParamsWithType(int i, Bundle bundle) throws RemoteException;

    boolean setMultiWindowDisabled(boolean z) throws RemoteException;

    void setPCFullSize(int i, int i2, int i3) throws RemoteException;

    void setPCMultiCastMode(boolean z) throws RemoteException;

    void setPCVirtualSize(int i, int i2, int i3) throws RemoteException;

    void setPcSize(int i, int i2) throws RemoteException;

    void setSplitBarVisibility(boolean z) throws RemoteException;

    boolean setStackScale(int i, float f) throws RemoteException;

    void setStackWindowingMode(IBinder iBinder, int i, Rect rect) throws RemoteException;

    void setWarmColdSwitch(boolean z) throws RemoteException;

    int[] startActivitiesFromRecents(int[] iArr, List<Bundle> list, boolean z, int i) throws RemoteException;

    void toggleFreeformWindowingMode(IBinder iBinder, String str) throws RemoteException;

    void toggleHome() throws RemoteException;

    void togglePCMode(boolean z, int i) throws RemoteException;

    void unRegisterHwTaskStackListener(ITaskStackListener iTaskStackListener) throws RemoteException;

    void unregisterGameObserver(IGameObserver iGameObserver) throws RemoteException;

    void unregisterGameObserverEx(IGameObserverEx iGameObserverEx) throws RemoteException;

    void unregisterHwActivityNotifier(IHwActivityNotifier iHwActivityNotifier) throws RemoteException;

    void unregisterMultiDisplayMessenger(Messenger messenger) throws RemoteException;

    boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback iMWThirdpartyCallback) throws RemoteException;

    void updateFloatingBallPos(Rect rect) throws RemoteException;

    void updateFreeFormOutLine(int i) throws RemoteException;

    void updateFreeFormOutLineForFloating(IBinder iBinder, int i) throws RemoteException;

    public static class Default implements IHwActivityTaskManager {
        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerHwActivityNotifier(IHwActivityNotifier notifier, String reason) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void unregisterHwActivityNotifier(IHwActivityNotifier notifier) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public ActivityInfo getLastResumedActivity() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bundle getTopActivity() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback callback) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setWarmColdSwitch(boolean enable) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isInMultiWindowMode() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getTopTaskIdInDisplay(int displayId, String pkgName, boolean invisibleAlso) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isTaskSupportResize(int taskId, boolean isFullscreen, boolean isMaximized) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isSupportsSplitScreenWindowingMode(IBinder activityToken) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Rect getPCTopTaskBounds(int displayId) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void hwRestoreTask(int taskId, float xPos, float yPos) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void hwResizeTask(int taskId, Rect bounds) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getWindowState(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void togglePCMode(boolean pcMode, int displayId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void toggleHome() throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void unRegisterHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean checkTaskId(int taskId) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void moveTaskBackwards(int taskId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bitmap getTaskThumbnailOnPCMode(int taskId) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean requestContentNode(ComponentName componentName, Bundle bundle, int token) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean requestContentOther(ComponentName componentName, Bundle bundle, int token) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean addGameSpacePackageList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean delGameSpacePackageList(List<String> list) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerGameObserver(IGameObserver observer) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void unregisterGameObserver(IGameObserver observer) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerGameObserverEx(IGameObserverEx observer) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void unregisterGameObserverEx(IGameObserverEx observer) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isInGameSpace(String packageName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public List<String> getGameList() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isGameDndOn() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isGameDndOnEx() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isGameKeyControlOn() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isGameGestureDisabled() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isFreeFormVisible() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isTaskVisible(int id) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void updateFreeFormOutLine(int state) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void updateFreeFormOutLineForFloating(IBinder token, int state) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isFullScreen(IBinder token) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getCaptionState(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getActivityWindowMode(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void onCaptionDropAnimationDone(IBinder token) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public List<ActivityManager.RunningTaskInfo> getVisibleTasks() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public ActivityManager.TaskSnapshot getActivityTaskSnapshot(IBinder activityToken, boolean reducedResolution) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int[] setFreeformStackVisibility(int displayId, int[] stackIdArray, boolean isVisible) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void dismissSplitScreenToFocusedStack() throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void handleMultiWindowSwitch(IBinder token, Bundle info) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bundle getSplitStacksPos(int displayId, int splitRatio) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean enterCoordinationMode(Intent intent) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean exitCoordinationMode(boolean toTop) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setSplitBarVisibility(boolean isVisibility) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean setCustomActivityController(IActivityController controller) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isResizableApp(ActivityInfo activityInfo) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bundle getHwMultiWindowAppControlLists() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isNeedAdapterCaptionView(String packageName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void saveMultiWindowTipState(String tipKey, int state) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isSupportDragForMultiWin(IBinder token) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public List<String> getVisiblePackages() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean setMultiWindowDisabled(boolean disabled) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean getMultiWindowDisabled() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bundle getHwMultiWindowState() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setForegroundFreeFormNum(int num) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Map getAppUserAwarenessState(int displayId, List<String> list) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public List<ActivityManager.RecentTaskInfo> getFilteredTasks(int userId, int displayId, String packageName, int[] windowingModes, boolean isIgnoreVisible, int maxNum) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean removeTask(int taskId, IBinder token, String packageName, boolean isRemoveFromRecents, String reason) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void removeTasks(int[] taskIds) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void toggleFreeformWindowingMode(IBinder appToken, String packageName) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean setStackScale(int taskId, float scale) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean setDockCallBackInfo(IHwDockCallBack callBack, int type) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int[] startActivitiesFromRecents(int[] taskIds, List<Bundle> list, boolean divideSplitScreen, int flag) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Rect resizeActivityStack(IBinder token, Rect bounds, boolean isAlwaysOnTop) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getVirtualDisplayId(String castType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean moveStacksToDisplay(int fromDisplayId, int toDisplayId, boolean isOnlyFocus) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isDisplayHoldScreen(int displayId) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isPadCastMaxSizeEnable() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isMirrorCast(String castType) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getTopFocusedDisplayId() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getActivityDisplayId(int pid, int uid) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isStatusBarPermenantlyShowing() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void moveTaskToFrontForMultiDisplay(int taskId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void hwResizeTaskForMultiDisplay(int taskId, Rect bounds) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void moveTaskBackwardsForMultiDisplay(int taskId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setFocusedTaskForMultiDisplay(int taskId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setPCFullSize(int fullWidth, int fullHeight, int phoneOrientation) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setPCVirtualSize(int virtualWidth, int virtualHeight, int phoneOrientation) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setPCMultiCastMode(boolean isPCMultiCastMode) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setCurOrientation(int curOrientation) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getPCVirtualWidth() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getPCVirtualHeight() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getPCFullWidth() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getPCFullHeight() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void registerMultiDisplayMessenger(Messenger messenger) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void unregisterMultiDisplayMessenger(Messenger messenger) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void hwTogglePCFloatWindow(int taskId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void hwTogglePhoneFullScreen(int taskId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public List<Bundle> getTaskList() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getCurTopFullScreenTaskState() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getCurPCWindowAreaNum() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public List<Bundle> getLastRencentTaskList() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bundle getActivityOptionFromAppProcess(IApplicationThread thread) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public float getStackScale(int taskId) throws RemoteException {
            return 0.0f;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int retrievePCMultiWinConfig(String config) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getPcWidth() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public int getPcHeight() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setPcSize(int pcWidth, int pcHeight) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setMultiDisplayParamsWithType(int type, Bundle bundle) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Rect getLocalLayerRectForMultiDisplay() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Rect getLocalDisplayRectForMultiDisplay() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Rect getVirtualLayerRectForMultiDisplay() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Rect getVirtualDisplayRectForMultiDisplay() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Rect adjustScreenShotRectForPCCast(Rect sourceCrop) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void hwSetRequestedOrientation(int taskId, int requestedOrientation) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void setStackWindowingMode(IBinder token, int windowingMode, Rect bounds) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void updateFloatingBallPos(Rect pos) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void notifyCameraStateForAtms(Bundle options) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean minimizeHwFreeForm(IBinder token, String packageName, boolean nonRoot) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void notifyLauncherAction(String category, Bundle bundle) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bitmap getApplicationIcon(IBinder activityToken, boolean isCheckAppLock) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public boolean isSupportDragToSplitScreen(IBinder token, boolean isCheckAppLock) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public List<String> getVisibleCanShowWhenLockedPackages(int displayId) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public Bundle getFreeformBoundsInCenter(int displayId, int centerX) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityTaskManager
        public void notifyNotificationAnimationFinish(int displayId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwActivityTaskManager {
        private static final String DESCRIPTOR = "com.huawei.android.app.IHwActivityTaskManager";
        static final int TRANSACTION_addGameSpacePackageList = 27;
        static final int TRANSACTION_adjustScreenShotRectForPCCast = 116;
        static final int TRANSACTION_checkTaskId = 22;
        static final int TRANSACTION_delGameSpacePackageList = 28;
        static final int TRANSACTION_dismissSplitScreenToFocusedStack = 51;
        static final int TRANSACTION_enterCoordinationMode = 54;
        static final int TRANSACTION_exitCoordinationMode = 55;
        static final int TRANSACTION_getActivityDisplayId = 83;
        static final int TRANSACTION_getActivityOptionFromAppProcess = 105;
        static final int TRANSACTION_getActivityTaskSnapshot = 49;
        static final int TRANSACTION_getActivityWindowMode = 45;
        static final int TRANSACTION_getAppUserAwarenessState = 68;
        static final int TRANSACTION_getApplicationIcon = 123;
        static final int TRANSACTION_getCaptionState = 44;
        static final int TRANSACTION_getCurPCWindowAreaNum = 103;
        static final int TRANSACTION_getCurTopFullScreenTaskState = 102;
        static final int TRANSACTION_getFilteredTasks = 69;
        static final int TRANSACTION_getFreeformBoundsInCenter = 126;
        static final int TRANSACTION_getGameList = 34;
        static final int TRANSACTION_getHwMultiWindowAppControlLists = 59;
        static final int TRANSACTION_getHwMultiWindowState = 66;
        static final int TRANSACTION_getHwRecentTaskInfo = 17;
        static final int TRANSACTION_getLastRencentTaskList = 104;
        static final int TRANSACTION_getLastResumedActivity = 3;
        static final int TRANSACTION_getLocalDisplayRectForMultiDisplay = 113;
        static final int TRANSACTION_getLocalLayerRectForMultiDisplay = 112;
        static final int TRANSACTION_getMultiWindowDisabled = 65;
        static final int TRANSACTION_getPCFullHeight = 96;
        static final int TRANSACTION_getPCFullWidth = 95;
        static final int TRANSACTION_getPCTopTaskBounds = 13;
        static final int TRANSACTION_getPCVirtualHeight = 94;
        static final int TRANSACTION_getPCVirtualWidth = 93;
        static final int TRANSACTION_getPcHeight = 109;
        static final int TRANSACTION_getPcWidth = 108;
        static final int TRANSACTION_getSplitStacksPos = 53;
        static final int TRANSACTION_getStackScale = 106;
        static final int TRANSACTION_getTaskList = 101;
        static final int TRANSACTION_getTaskSnapshot = 48;
        static final int TRANSACTION_getTaskThumbnailOnPCMode = 24;
        static final int TRANSACTION_getTopActivity = 4;
        static final int TRANSACTION_getTopFocusedDisplayId = 82;
        static final int TRANSACTION_getTopTaskIdInDisplay = 10;
        static final int TRANSACTION_getVirtualDisplayId = 77;
        static final int TRANSACTION_getVirtualDisplayRectForMultiDisplay = 115;
        static final int TRANSACTION_getVirtualLayerRectForMultiDisplay = 114;
        static final int TRANSACTION_getVisibleCanShowWhenLockedPackages = 125;
        static final int TRANSACTION_getVisiblePackages = 63;
        static final int TRANSACTION_getVisibleTasks = 47;
        static final int TRANSACTION_getWindowState = 16;
        static final int TRANSACTION_handleMultiWindowSwitch = 52;
        static final int TRANSACTION_hwResizeTask = 15;
        static final int TRANSACTION_hwResizeTaskForMultiDisplay = 86;
        static final int TRANSACTION_hwRestoreTask = 14;
        static final int TRANSACTION_hwSetRequestedOrientation = 117;
        static final int TRANSACTION_hwTogglePCFloatWindow = 99;
        static final int TRANSACTION_hwTogglePhoneFullScreen = 100;
        static final int TRANSACTION_isDisplayHoldScreen = 79;
        static final int TRANSACTION_isFreeFormVisible = 39;
        static final int TRANSACTION_isFullScreen = 43;
        static final int TRANSACTION_isGameDndOn = 35;
        static final int TRANSACTION_isGameDndOnEx = 36;
        static final int TRANSACTION_isGameGestureDisabled = 38;
        static final int TRANSACTION_isGameKeyControlOn = 37;
        static final int TRANSACTION_isInGameSpace = 33;
        static final int TRANSACTION_isInMultiWindowMode = 7;
        static final int TRANSACTION_isMirrorCast = 81;
        static final int TRANSACTION_isNeedAdapterCaptionView = 60;
        static final int TRANSACTION_isPadCastMaxSizeEnable = 80;
        static final int TRANSACTION_isResizableApp = 58;
        static final int TRANSACTION_isStatusBarPermenantlyShowing = 84;
        static final int TRANSACTION_isSupportDragForMultiWin = 62;
        static final int TRANSACTION_isSupportDragToSplitScreen = 124;
        static final int TRANSACTION_isSupportsSplitScreenWindowingMode = 12;
        static final int TRANSACTION_isTaskSupportResize = 11;
        static final int TRANSACTION_isTaskVisible = 40;
        static final int TRANSACTION_minimizeHwFreeForm = 121;
        static final int TRANSACTION_moveStacksToDisplay = 78;
        static final int TRANSACTION_moveTaskBackwards = 23;
        static final int TRANSACTION_moveTaskBackwardsForMultiDisplay = 87;
        static final int TRANSACTION_moveTaskToFrontForMultiDisplay = 85;
        static final int TRANSACTION_notifyCameraStateForAtms = 120;
        static final int TRANSACTION_notifyLauncherAction = 122;
        static final int TRANSACTION_notifyNotificationAnimationFinish = 127;
        static final int TRANSACTION_onCaptionDropAnimationDone = 46;
        static final int TRANSACTION_registerAtmDAMonitorCallback = 5;
        static final int TRANSACTION_registerGameObserver = 29;
        static final int TRANSACTION_registerGameObserverEx = 31;
        static final int TRANSACTION_registerHwActivityNotifier = 1;
        static final int TRANSACTION_registerHwTaskStackListener = 21;
        static final int TRANSACTION_registerMultiDisplayMessenger = 97;
        static final int TRANSACTION_registerThirdPartyCallBack = 8;
        static final int TRANSACTION_removeTask = 70;
        static final int TRANSACTION_removeTasks = 71;
        static final int TRANSACTION_requestContentNode = 25;
        static final int TRANSACTION_requestContentOther = 26;
        static final int TRANSACTION_resizeActivityStack = 76;
        static final int TRANSACTION_retrievePCMultiWinConfig = 107;
        static final int TRANSACTION_saveMultiWindowTipState = 61;
        static final int TRANSACTION_setCurOrientation = 92;
        static final int TRANSACTION_setCustomActivityController = 57;
        static final int TRANSACTION_setDockCallBackInfo = 74;
        static final int TRANSACTION_setFocusedTaskForMultiDisplay = 88;
        static final int TRANSACTION_setForegroundFreeFormNum = 67;
        static final int TRANSACTION_setFreeformStackVisibility = 50;
        static final int TRANSACTION_setMultiDisplayParamsWithType = 111;
        static final int TRANSACTION_setMultiWindowDisabled = 64;
        static final int TRANSACTION_setPCFullSize = 89;
        static final int TRANSACTION_setPCMultiCastMode = 91;
        static final int TRANSACTION_setPCVirtualSize = 90;
        static final int TRANSACTION_setPcSize = 110;
        static final int TRANSACTION_setSplitBarVisibility = 56;
        static final int TRANSACTION_setStackScale = 73;
        static final int TRANSACTION_setStackWindowingMode = 118;
        static final int TRANSACTION_setWarmColdSwitch = 6;
        static final int TRANSACTION_startActivitiesFromRecents = 75;
        static final int TRANSACTION_toggleFreeformWindowingMode = 72;
        static final int TRANSACTION_toggleHome = 19;
        static final int TRANSACTION_togglePCMode = 18;
        static final int TRANSACTION_unRegisterHwTaskStackListener = 20;
        static final int TRANSACTION_unregisterGameObserver = 30;
        static final int TRANSACTION_unregisterGameObserverEx = 32;
        static final int TRANSACTION_unregisterHwActivityNotifier = 2;
        static final int TRANSACTION_unregisterMultiDisplayMessenger = 98;
        static final int TRANSACTION_unregisterThirdPartyCallBack = 9;
        static final int TRANSACTION_updateFloatingBallPos = 119;
        static final int TRANSACTION_updateFreeFormOutLine = 41;
        static final int TRANSACTION_updateFreeFormOutLineForFloating = 42;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwActivityTaskManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwActivityTaskManager)) {
                return new Proxy(obj);
            }
            return (IHwActivityTaskManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "registerHwActivityNotifier";
                case 2:
                    return "unregisterHwActivityNotifier";
                case 3:
                    return "getLastResumedActivity";
                case 4:
                    return "getTopActivity";
                case 5:
                    return "registerAtmDAMonitorCallback";
                case 6:
                    return "setWarmColdSwitch";
                case 7:
                    return "isInMultiWindowMode";
                case 8:
                    return "registerThirdPartyCallBack";
                case 9:
                    return "unregisterThirdPartyCallBack";
                case 10:
                    return "getTopTaskIdInDisplay";
                case 11:
                    return "isTaskSupportResize";
                case 12:
                    return "isSupportsSplitScreenWindowingMode";
                case 13:
                    return "getPCTopTaskBounds";
                case 14:
                    return "hwRestoreTask";
                case 15:
                    return "hwResizeTask";
                case 16:
                    return "getWindowState";
                case 17:
                    return "getHwRecentTaskInfo";
                case 18:
                    return "togglePCMode";
                case 19:
                    return "toggleHome";
                case 20:
                    return "unRegisterHwTaskStackListener";
                case 21:
                    return "registerHwTaskStackListener";
                case 22:
                    return "checkTaskId";
                case 23:
                    return "moveTaskBackwards";
                case 24:
                    return "getTaskThumbnailOnPCMode";
                case 25:
                    return "requestContentNode";
                case 26:
                    return "requestContentOther";
                case 27:
                    return "addGameSpacePackageList";
                case 28:
                    return "delGameSpacePackageList";
                case 29:
                    return "registerGameObserver";
                case 30:
                    return "unregisterGameObserver";
                case 31:
                    return "registerGameObserverEx";
                case 32:
                    return "unregisterGameObserverEx";
                case 33:
                    return "isInGameSpace";
                case 34:
                    return "getGameList";
                case 35:
                    return "isGameDndOn";
                case 36:
                    return "isGameDndOnEx";
                case 37:
                    return "isGameKeyControlOn";
                case 38:
                    return "isGameGestureDisabled";
                case 39:
                    return "isFreeFormVisible";
                case 40:
                    return "isTaskVisible";
                case 41:
                    return "updateFreeFormOutLine";
                case 42:
                    return "updateFreeFormOutLineForFloating";
                case 43:
                    return "isFullScreen";
                case 44:
                    return "getCaptionState";
                case 45:
                    return "getActivityWindowMode";
                case 46:
                    return "onCaptionDropAnimationDone";
                case 47:
                    return "getVisibleTasks";
                case 48:
                    return "getTaskSnapshot";
                case 49:
                    return "getActivityTaskSnapshot";
                case 50:
                    return "setFreeformStackVisibility";
                case 51:
                    return "dismissSplitScreenToFocusedStack";
                case 52:
                    return "handleMultiWindowSwitch";
                case 53:
                    return "getSplitStacksPos";
                case 54:
                    return "enterCoordinationMode";
                case 55:
                    return "exitCoordinationMode";
                case 56:
                    return "setSplitBarVisibility";
                case 57:
                    return "setCustomActivityController";
                case 58:
                    return "isResizableApp";
                case 59:
                    return "getHwMultiWindowAppControlLists";
                case 60:
                    return "isNeedAdapterCaptionView";
                case 61:
                    return "saveMultiWindowTipState";
                case 62:
                    return "isSupportDragForMultiWin";
                case 63:
                    return "getVisiblePackages";
                case 64:
                    return "setMultiWindowDisabled";
                case 65:
                    return "getMultiWindowDisabled";
                case 66:
                    return "getHwMultiWindowState";
                case 67:
                    return "setForegroundFreeFormNum";
                case 68:
                    return "getAppUserAwarenessState";
                case 69:
                    return "getFilteredTasks";
                case 70:
                    return "removeTask";
                case 71:
                    return "removeTasks";
                case 72:
                    return "toggleFreeformWindowingMode";
                case 73:
                    return "setStackScale";
                case 74:
                    return "setDockCallBackInfo";
                case 75:
                    return "startActivitiesFromRecents";
                case 76:
                    return "resizeActivityStack";
                case 77:
                    return "getVirtualDisplayId";
                case 78:
                    return "moveStacksToDisplay";
                case 79:
                    return "isDisplayHoldScreen";
                case 80:
                    return "isPadCastMaxSizeEnable";
                case 81:
                    return "isMirrorCast";
                case 82:
                    return "getTopFocusedDisplayId";
                case 83:
                    return "getActivityDisplayId";
                case 84:
                    return "isStatusBarPermenantlyShowing";
                case 85:
                    return "moveTaskToFrontForMultiDisplay";
                case 86:
                    return "hwResizeTaskForMultiDisplay";
                case 87:
                    return "moveTaskBackwardsForMultiDisplay";
                case 88:
                    return "setFocusedTaskForMultiDisplay";
                case 89:
                    return "setPCFullSize";
                case 90:
                    return "setPCVirtualSize";
                case 91:
                    return "setPCMultiCastMode";
                case 92:
                    return "setCurOrientation";
                case 93:
                    return "getPCVirtualWidth";
                case 94:
                    return "getPCVirtualHeight";
                case 95:
                    return "getPCFullWidth";
                case 96:
                    return "getPCFullHeight";
                case 97:
                    return "registerMultiDisplayMessenger";
                case 98:
                    return "unregisterMultiDisplayMessenger";
                case 99:
                    return "hwTogglePCFloatWindow";
                case 100:
                    return "hwTogglePhoneFullScreen";
                case 101:
                    return "getTaskList";
                case 102:
                    return "getCurTopFullScreenTaskState";
                case 103:
                    return "getCurPCWindowAreaNum";
                case 104:
                    return "getLastRencentTaskList";
                case 105:
                    return "getActivityOptionFromAppProcess";
                case 106:
                    return "getStackScale";
                case 107:
                    return "retrievePCMultiWinConfig";
                case 108:
                    return "getPcWidth";
                case 109:
                    return "getPcHeight";
                case 110:
                    return "setPcSize";
                case 111:
                    return "setMultiDisplayParamsWithType";
                case 112:
                    return "getLocalLayerRectForMultiDisplay";
                case 113:
                    return "getLocalDisplayRectForMultiDisplay";
                case 114:
                    return "getVirtualLayerRectForMultiDisplay";
                case 115:
                    return "getVirtualDisplayRectForMultiDisplay";
                case 116:
                    return "adjustScreenShotRectForPCCast";
                case 117:
                    return "hwSetRequestedOrientation";
                case 118:
                    return "setStackWindowingMode";
                case 119:
                    return "updateFloatingBallPos";
                case 120:
                    return "notifyCameraStateForAtms";
                case 121:
                    return "minimizeHwFreeForm";
                case 122:
                    return "notifyLauncherAction";
                case 123:
                    return "getApplicationIcon";
                case 124:
                    return "isSupportDragToSplitScreen";
                case 125:
                    return "getVisibleCanShowWhenLockedPackages";
                case 126:
                    return "getFreeformBoundsInCenter";
                case 127:
                    return "notifyNotificationAnimationFinish";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Rect _arg1;
            ComponentName _arg0;
            Bundle _arg12;
            ComponentName _arg02;
            Bundle _arg13;
            Bundle _arg14;
            Intent _arg03;
            ActivityInfo _arg04;
            Rect _arg15;
            Rect _arg16;
            Messenger _arg05;
            Messenger _arg06;
            Bundle _arg17;
            Rect _arg07;
            Rect _arg2;
            Rect _arg08;
            Bundle _arg09;
            Bundle _arg18;
            if (code != 1598968902) {
                boolean _arg19 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        registerHwActivityNotifier(IHwActivityNotifier.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterHwActivityNotifier(IHwActivityNotifier.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        ActivityInfo _result = getLastResumedActivity();
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result2 = getTopActivity();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        setWarmColdSwitch(_arg19);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInMultiWindowMode = isInMultiWindowMode();
                        reply.writeNoException();
                        reply.writeInt(isInMultiWindowMode ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerThirdPartyCallBack = registerThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerThirdPartyCallBack ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterThirdPartyCallBack = unregisterThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterThirdPartyCallBack ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        String _arg110 = data.readString();
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        int _result3 = getTopTaskIdInDisplay(_arg010, _arg110, _arg19);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        boolean _arg111 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        boolean isTaskSupportResize = isTaskSupportResize(_arg011, _arg111, _arg19);
                        reply.writeNoException();
                        reply.writeInt(isTaskSupportResize ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSupportsSplitScreenWindowingMode = isSupportsSplitScreenWindowingMode(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(isSupportsSplitScreenWindowingMode ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _result4 = getPCTopTaskBounds(data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        hwRestoreTask(data.readInt(), data.readFloat(), data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg012 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        hwResizeTask(_arg012, _arg1);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getWindowState(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        HwRecentTaskInfo _result6 = getHwRecentTaskInfo(data.readInt());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        togglePCMode(_arg19, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        toggleHome();
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        unRegisterHwTaskStackListener(ITaskStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        registerHwTaskStackListener(ITaskStackListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkTaskId = checkTaskId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(checkTaskId ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        moveTaskBackwards(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        Bitmap _result7 = getTaskThumbnailOnPCMode(data.readInt());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean requestContentNode = requestContentNode(_arg0, _arg12, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(requestContentNode ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean requestContentOther = requestContentOther(_arg02, _arg13, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(requestContentOther ? 1 : 0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addGameSpacePackageList = addGameSpacePackageList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(addGameSpacePackageList ? 1 : 0);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        boolean delGameSpacePackageList = delGameSpacePackageList(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(delGameSpacePackageList ? 1 : 0);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        registerGameObserver(IGameObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterGameObserver(IGameObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        registerGameObserverEx(IGameObserverEx.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterGameObserverEx(IGameObserverEx.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInGameSpace = isInGameSpace(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isInGameSpace ? 1 : 0);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result8 = getGameList();
                        reply.writeNoException();
                        reply.writeStringList(_result8);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGameDndOn = isGameDndOn();
                        reply.writeNoException();
                        reply.writeInt(isGameDndOn ? 1 : 0);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGameDndOnEx = isGameDndOnEx();
                        reply.writeNoException();
                        reply.writeInt(isGameDndOnEx ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGameKeyControlOn = isGameKeyControlOn();
                        reply.writeNoException();
                        reply.writeInt(isGameKeyControlOn ? 1 : 0);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGameGestureDisabled = isGameGestureDisabled();
                        reply.writeNoException();
                        reply.writeInt(isGameGestureDisabled ? 1 : 0);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFreeFormVisible = isFreeFormVisible();
                        reply.writeNoException();
                        reply.writeInt(isFreeFormVisible ? 1 : 0);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTaskVisible = isTaskVisible(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isTaskVisible ? 1 : 0);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        updateFreeFormOutLine(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        updateFreeFormOutLineForFloating(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFullScreen = isFullScreen(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(isFullScreen ? 1 : 0);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getCaptionState(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getActivityWindowMode(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        onCaptionDropAnimationDone(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RunningTaskInfo> _result11 = getVisibleTasks();
                        reply.writeNoException();
                        reply.writeTypedList(_result11);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        ActivityManager.TaskSnapshot _result12 = getTaskSnapshot(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        ActivityManager.TaskSnapshot _result13 = getActivityTaskSnapshot(data.readStrongBinder(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result13 != null) {
                            reply.writeInt(1);
                            _result13.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg013 = data.readInt();
                        int[] _arg112 = data.createIntArray();
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        int[] _result14 = setFreeformStackVisibility(_arg013, _arg112, _arg19);
                        reply.writeNoException();
                        reply.writeIntArray(_result14);
                        reply.writeIntArray(_arg112);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        dismissSplitScreenToFocusedStack();
                        reply.writeNoException();
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg014 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg14 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        handleMultiWindowSwitch(_arg014, _arg14);
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result15 = getSplitStacksPos(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result15 != null) {
                            reply.writeInt(1);
                            _result15.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        boolean enterCoordinationMode = enterCoordinationMode(_arg03);
                        reply.writeNoException();
                        reply.writeInt(enterCoordinationMode ? 1 : 0);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        boolean exitCoordinationMode = exitCoordinationMode(_arg19);
                        reply.writeNoException();
                        reply.writeInt(exitCoordinationMode ? 1 : 0);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        setSplitBarVisibility(_arg19);
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        boolean customActivityController = setCustomActivityController(IActivityController.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(customActivityController ? 1 : 0);
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ActivityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        boolean isResizableApp = isResizableApp(_arg04);
                        reply.writeNoException();
                        reply.writeInt(isResizableApp ? 1 : 0);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result16 = getHwMultiWindowAppControlLists();
                        reply.writeNoException();
                        if (_result16 != null) {
                            reply.writeInt(1);
                            _result16.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNeedAdapterCaptionView = isNeedAdapterCaptionView(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isNeedAdapterCaptionView ? 1 : 0);
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        saveMultiWindowTipState(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSupportDragForMultiWin = isSupportDragForMultiWin(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(isSupportDragForMultiWin ? 1 : 0);
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result17 = getVisiblePackages();
                        reply.writeNoException();
                        reply.writeStringList(_result17);
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        boolean multiWindowDisabled = setMultiWindowDisabled(_arg19);
                        reply.writeNoException();
                        reply.writeInt(multiWindowDisabled ? 1 : 0);
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        boolean multiWindowDisabled2 = getMultiWindowDisabled();
                        reply.writeNoException();
                        reply.writeInt(multiWindowDisabled2 ? 1 : 0);
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result18 = getHwMultiWindowState();
                        reply.writeNoException();
                        if (_result18 != null) {
                            reply.writeInt(1);
                            _result18.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        setForegroundFreeFormNum(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result19 = getAppUserAwarenessState(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeMap(_result19);
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        List<ActivityManager.RecentTaskInfo> _result20 = getFilteredTasks(data.readInt(), data.readInt(), data.readString(), data.createIntArray(), data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result20);
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeTask = removeTask(data.readInt(), data.readStrongBinder(), data.readString(), data.readInt() != 0, data.readString());
                        reply.writeNoException();
                        reply.writeInt(removeTask ? 1 : 0);
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        removeTasks(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        toggleFreeformWindowingMode(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stackScale = setStackScale(data.readInt(), data.readFloat());
                        reply.writeNoException();
                        reply.writeInt(stackScale ? 1 : 0);
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        boolean dockCallBackInfo = setDockCallBackInfo(IHwDockCallBack.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(dockCallBackInfo ? 1 : 0);
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _arg015 = data.createIntArray();
                        List<Bundle> _arg113 = data.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        int[] _result21 = startActivitiesFromRecents(_arg015, _arg113, _arg19, data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result21);
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg016 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        Rect _result22 = resizeActivityStack(_arg016, _arg15, data.readInt() != 0);
                        reply.writeNoException();
                        if (_result22 != null) {
                            reply.writeInt(1);
                            _result22.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = getVirtualDisplayId(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg017 = data.readInt();
                        int _arg114 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        boolean moveStacksToDisplay = moveStacksToDisplay(_arg017, _arg114, _arg19);
                        reply.writeNoException();
                        reply.writeInt(moveStacksToDisplay ? 1 : 0);
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDisplayHoldScreen = isDisplayHoldScreen(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDisplayHoldScreen ? 1 : 0);
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPadCastMaxSizeEnable = isPadCastMaxSizeEnable();
                        reply.writeNoException();
                        reply.writeInt(isPadCastMaxSizeEnable ? 1 : 0);
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isMirrorCast = isMirrorCast(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isMirrorCast ? 1 : 0);
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        int _result24 = getTopFocusedDisplayId();
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = getActivityDisplayId(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isStatusBarPermenantlyShowing = isStatusBarPermenantlyShowing();
                        reply.writeNoException();
                        reply.writeInt(isStatusBarPermenantlyShowing ? 1 : 0);
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        moveTaskToFrontForMultiDisplay(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg018 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        hwResizeTaskForMultiDisplay(_arg018, _arg16);
                        reply.writeNoException();
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        moveTaskBackwardsForMultiDisplay(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 88:
                        data.enforceInterface(DESCRIPTOR);
                        setFocusedTaskForMultiDisplay(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 89:
                        data.enforceInterface(DESCRIPTOR);
                        setPCFullSize(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 90:
                        data.enforceInterface(DESCRIPTOR);
                        setPCVirtualSize(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 91:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        setPCMultiCastMode(_arg19);
                        reply.writeNoException();
                        return true;
                    case 92:
                        data.enforceInterface(DESCRIPTOR);
                        setCurOrientation(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 93:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = getPCVirtualWidth();
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 94:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = getPCVirtualHeight();
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 95:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = getPCFullWidth();
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 96:
                        data.enforceInterface(DESCRIPTOR);
                        int _result29 = getPCFullHeight();
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 97:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        registerMultiDisplayMessenger(_arg05);
                        reply.writeNoException();
                        return true;
                    case 98:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        unregisterMultiDisplayMessenger(_arg06);
                        reply.writeNoException();
                        return true;
                    case 99:
                        data.enforceInterface(DESCRIPTOR);
                        hwTogglePCFloatWindow(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 100:
                        data.enforceInterface(DESCRIPTOR);
                        hwTogglePhoneFullScreen(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 101:
                        data.enforceInterface(DESCRIPTOR);
                        List<Bundle> _result30 = getTaskList();
                        reply.writeNoException();
                        reply.writeTypedList(_result30);
                        return true;
                    case 102:
                        data.enforceInterface(DESCRIPTOR);
                        int _result31 = getCurTopFullScreenTaskState();
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 103:
                        data.enforceInterface(DESCRIPTOR);
                        int _result32 = getCurPCWindowAreaNum();
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    case 104:
                        data.enforceInterface(DESCRIPTOR);
                        List<Bundle> _result33 = getLastRencentTaskList();
                        reply.writeNoException();
                        reply.writeTypedList(_result33);
                        return true;
                    case 105:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result34 = getActivityOptionFromAppProcess(IApplicationThread.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result34 != null) {
                            reply.writeInt(1);
                            _result34.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 106:
                        data.enforceInterface(DESCRIPTOR);
                        float _result35 = getStackScale(data.readInt());
                        reply.writeNoException();
                        reply.writeFloat(_result35);
                        return true;
                    case 107:
                        data.enforceInterface(DESCRIPTOR);
                        int _result36 = retrievePCMultiWinConfig(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result36);
                        return true;
                    case 108:
                        data.enforceInterface(DESCRIPTOR);
                        int _result37 = getPcWidth();
                        reply.writeNoException();
                        reply.writeInt(_result37);
                        return true;
                    case 109:
                        data.enforceInterface(DESCRIPTOR);
                        int _result38 = getPcHeight();
                        reply.writeNoException();
                        reply.writeInt(_result38);
                        return true;
                    case 110:
                        data.enforceInterface(DESCRIPTOR);
                        setPcSize(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 111:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg019 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg17 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        setMultiDisplayParamsWithType(_arg019, _arg17);
                        reply.writeNoException();
                        return true;
                    case 112:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _result39 = getLocalLayerRectForMultiDisplay();
                        reply.writeNoException();
                        if (_result39 != null) {
                            reply.writeInt(1);
                            _result39.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 113:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _result40 = getLocalDisplayRectForMultiDisplay();
                        reply.writeNoException();
                        if (_result40 != null) {
                            reply.writeInt(1);
                            _result40.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 114:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _result41 = getVirtualLayerRectForMultiDisplay();
                        reply.writeNoException();
                        if (_result41 != null) {
                            reply.writeInt(1);
                            _result41.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 115:
                        data.enforceInterface(DESCRIPTOR);
                        Rect _result42 = getVirtualDisplayRectForMultiDisplay();
                        reply.writeNoException();
                        if (_result42 != null) {
                            reply.writeInt(1);
                            _result42.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 116:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        Rect _result43 = adjustScreenShotRectForPCCast(_arg07);
                        reply.writeNoException();
                        if (_result43 != null) {
                            reply.writeInt(1);
                            _result43.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 117:
                        data.enforceInterface(DESCRIPTOR);
                        hwSetRequestedOrientation(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 118:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg020 = data.readStrongBinder();
                        int _arg115 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        setStackWindowingMode(_arg020, _arg115, _arg2);
                        reply.writeNoException();
                        return true;
                    case 119:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        updateFloatingBallPos(_arg08);
                        reply.writeNoException();
                        return true;
                    case 120:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        notifyCameraStateForAtms(_arg09);
                        reply.writeNoException();
                        return true;
                    case 121:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg021 = data.readStrongBinder();
                        String _arg116 = data.readString();
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        boolean minimizeHwFreeForm = minimizeHwFreeForm(_arg021, _arg116, _arg19);
                        reply.writeNoException();
                        reply.writeInt(minimizeHwFreeForm ? 1 : 0);
                        return true;
                    case 122:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg022 = data.readString();
                        if (data.readInt() != 0) {
                            _arg18 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        notifyLauncherAction(_arg022, _arg18);
                        reply.writeNoException();
                        return true;
                    case 123:
                        data.enforceInterface(DESCRIPTOR);
                        Bitmap _result44 = getApplicationIcon(data.readStrongBinder(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result44 != null) {
                            reply.writeInt(1);
                            _result44.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 124:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg023 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        boolean isSupportDragToSplitScreen = isSupportDragToSplitScreen(_arg023, _arg19);
                        reply.writeNoException();
                        reply.writeInt(isSupportDragToSplitScreen ? 1 : 0);
                        return true;
                    case 125:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result45 = getVisibleCanShowWhenLockedPackages(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result45);
                        return true;
                    case 126:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result46 = getFreeformBoundsInCenter(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result46 != null) {
                            reply.writeInt(1);
                            _result46.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 127:
                        data.enforceInterface(DESCRIPTOR);
                        notifyNotificationAnimationFinish(data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwActivityTaskManager {
            public static IHwActivityTaskManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerHwActivityNotifier(IHwActivityNotifier notifier, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(notifier != null ? notifier.asBinder() : null);
                    _data.writeString(reason);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerHwActivityNotifier(notifier, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void unregisterHwActivityNotifier(IHwActivityNotifier notifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(notifier != null ? notifier.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterHwActivityNotifier(notifier);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public ActivityInfo getLastResumedActivity() throws RemoteException {
                ActivityInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastResumedActivity();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bundle getTopActivity() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTopActivity();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerAtmDAMonitorCallback(IHwAtmDAMonitorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerAtmDAMonitorCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setWarmColdSwitch(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWarmColdSwitch(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isInMultiWindowMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInMultiWindowMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(aCallBackHandler != null ? aCallBackHandler.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerThirdPartyCallBack(aCallBackHandler);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(aCallBackHandler != null ? aCallBackHandler.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterThirdPartyCallBack(aCallBackHandler);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getTopTaskIdInDisplay(int displayId, String pkgName, boolean invisibleAlso) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeString(pkgName);
                    _data.writeInt(invisibleAlso ? 1 : 0);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTopTaskIdInDisplay(displayId, pkgName, invisibleAlso);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isTaskSupportResize(int taskId, boolean isFullscreen, boolean isMaximized) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    boolean _result = true;
                    _data.writeInt(isFullscreen ? 1 : 0);
                    _data.writeInt(isMaximized ? 1 : 0);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTaskSupportResize(taskId, isFullscreen, isMaximized);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isSupportsSplitScreenWindowingMode(IBinder activityToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportsSplitScreenWindowingMode(activityToken);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Rect getPCTopTaskBounds(int displayId) throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPCTopTaskBounds(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void hwRestoreTask(int taskId, float xPos, float yPos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeFloat(xPos);
                    _data.writeFloat(yPos);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwRestoreTask(taskId, xPos, yPos);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void hwResizeTask(int taskId, Rect bounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwResizeTask(taskId, bounds);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getWindowState(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWindowState(token);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) throws RemoteException {
                HwRecentTaskInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwRecentTaskInfo(taskId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = HwRecentTaskInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void togglePCMode(boolean pcMode, int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pcMode ? 1 : 0);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().togglePCMode(pcMode, displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void toggleHome() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().toggleHome();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void unRegisterHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unRegisterHwTaskStackListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerHwTaskStackListener(ITaskStackListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerHwTaskStackListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean checkTaskId(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkTaskId(taskId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void moveTaskBackwards(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveTaskBackwards(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bitmap getTaskThumbnailOnPCMode(int taskId) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTaskThumbnailOnPCMode(taskId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean requestContentNode(ComponentName componentName, Bundle bundle, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestContentNode(componentName, bundle, token);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean requestContentOther(ComponentName componentName, Bundle bundle, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestContentOther(componentName, bundle, token);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean addGameSpacePackageList(List<String> packageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageList);
                    boolean _result = false;
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addGameSpacePackageList(packageList);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean delGameSpacePackageList(List<String> packageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageList);
                    boolean _result = false;
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().delGameSpacePackageList(packageList);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerGameObserver(IGameObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerGameObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void unregisterGameObserver(IGameObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterGameObserver(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerGameObserverEx(IGameObserverEx observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerGameObserverEx(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void unregisterGameObserverEx(IGameObserverEx observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterGameObserverEx(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isInGameSpace(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInGameSpace(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public List<String> getGameList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGameList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isGameDndOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGameDndOn();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isGameDndOnEx() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGameDndOnEx();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isGameKeyControlOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGameKeyControlOn();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isGameGestureDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGameGestureDisabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isFreeFormVisible() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFreeFormVisible();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isTaskVisible(int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    boolean _result = false;
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTaskVisible(id);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void updateFreeFormOutLine(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateFreeFormOutLine(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void updateFreeFormOutLineForFloating(IBinder token, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(state);
                    if (this.mRemote.transact(42, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateFreeFormOutLineForFloating(token, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isFullScreen(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFullScreen(token);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getCaptionState(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCaptionState(token);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getActivityWindowMode(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActivityWindowMode(token);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void onCaptionDropAnimationDone(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(46, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCaptionDropAnimationDone(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public List<ActivityManager.RunningTaskInfo> getVisibleTasks() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVisibleTasks();
                    }
                    _reply.readException();
                    List<ActivityManager.RunningTaskInfo> _result = _reply.createTypedArrayList(ActivityManager.RunningTaskInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) throws RemoteException {
                ActivityManager.TaskSnapshot _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(reducedResolution ? 1 : 0);
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTaskSnapshot(taskId, reducedResolution);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityManager.TaskSnapshot.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public ActivityManager.TaskSnapshot getActivityTaskSnapshot(IBinder activityToken, boolean reducedResolution) throws RemoteException {
                ActivityManager.TaskSnapshot _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    _data.writeInt(reducedResolution ? 1 : 0);
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActivityTaskSnapshot(activityToken, reducedResolution);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityManager.TaskSnapshot.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int[] setFreeformStackVisibility(int displayId, int[] stackIdArray, boolean isVisible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeIntArray(stackIdArray);
                    _data.writeInt(isVisible ? 1 : 0);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setFreeformStackVisibility(displayId, stackIdArray, isVisible);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.readIntArray(stackIdArray);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void dismissSplitScreenToFocusedStack() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dismissSplitScreenToFocusedStack();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void handleMultiWindowSwitch(IBinder token, Bundle info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().handleMultiWindowSwitch(token, info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bundle getSplitStacksPos(int displayId, int splitRatio) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(splitRatio);
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSplitStacksPos(displayId, splitRatio);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean enterCoordinationMode(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enterCoordinationMode(intent);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean exitCoordinationMode(boolean toTop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(toTop ? 1 : 0);
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().exitCoordinationMode(toTop);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setSplitBarVisibility(boolean isVisibility) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isVisibility ? 1 : 0);
                    if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSplitBarVisibility(isVisibility);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean setCustomActivityController(IActivityController controller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(controller != null ? controller.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setCustomActivityController(controller);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isResizableApp(ActivityInfo activityInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (activityInfo != null) {
                        _data.writeInt(1);
                        activityInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isResizableApp(activityInfo);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bundle getHwMultiWindowAppControlLists() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(59, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwMultiWindowAppControlLists();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isNeedAdapterCaptionView(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNeedAdapterCaptionView(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void saveMultiWindowTipState(String tipKey, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(tipKey);
                    _data.writeInt(state);
                    if (this.mRemote.transact(61, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().saveMultiWindowTipState(tipKey, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isSupportDragForMultiWin(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportDragForMultiWin(token);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public List<String> getVisiblePackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(63, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVisiblePackages();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean setMultiWindowDisabled(boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(disabled ? 1 : 0);
                    if (!this.mRemote.transact(64, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMultiWindowDisabled(disabled);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean getMultiWindowDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(65, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMultiWindowDisabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bundle getHwMultiWindowState() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(66, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwMultiWindowState();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setForegroundFreeFormNum(int num) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(num);
                    if (this.mRemote.transact(67, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setForegroundFreeFormNum(num);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Map getAppUserAwarenessState(int displayId, List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeStringList(packageNames);
                    if (!this.mRemote.transact(68, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppUserAwarenessState(displayId, packageNames);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public List<ActivityManager.RecentTaskInfo> getFilteredTasks(int userId, int displayId, String packageName, int[] windowingModes, boolean isIgnoreVisible, int maxNum) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(userId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(displayId);
                        try {
                            _data.writeString(packageName);
                            try {
                                _data.writeIntArray(windowingModes);
                                _data.writeInt(isIgnoreVisible ? 1 : 0);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                            try {
                                _data.writeInt(maxNum);
                            } catch (Throwable th4) {
                                th = th4;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(69, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            List<ActivityManager.RecentTaskInfo> _result = _reply.createTypedArrayList(ActivityManager.RecentTaskInfo.CREATOR);
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        List<ActivityManager.RecentTaskInfo> filteredTasks = Stub.getDefaultImpl().getFilteredTasks(userId, displayId, packageName, windowingModes, isIgnoreVisible, maxNum);
                        _reply.recycle();
                        _data.recycle();
                        return filteredTasks;
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean removeTask(int taskId, IBinder token, String packageName, boolean isRemoveFromRecents, String reason) throws RemoteException {
                Throwable th;
                boolean _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(taskId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStrongBinder(token);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                        _result = true;
                        _data.writeInt(isRemoveFromRecents ? 1 : 0);
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(reason);
                        try {
                            if (this.mRemote.transact(70, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean removeTask = Stub.getDefaultImpl().removeTask(taskId, token, packageName, isRemoveFromRecents, reason);
                            _reply.recycle();
                            _data.recycle();
                            return removeTask;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void removeTasks(int[] taskIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(taskIds);
                    if (this.mRemote.transact(71, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeTasks(taskIds);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void toggleFreeformWindowingMode(IBinder appToken, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appToken);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(72, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().toggleFreeformWindowingMode(appToken, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean setStackScale(int taskId, float scale) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeFloat(scale);
                    boolean _result = false;
                    if (!this.mRemote.transact(73, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setStackScale(taskId, scale);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean setDockCallBackInfo(IHwDockCallBack callBack, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    _data.writeInt(type);
                    boolean _result = false;
                    if (!this.mRemote.transact(74, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDockCallBackInfo(callBack, type);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int[] startActivitiesFromRecents(int[] taskIds, List<Bundle> bOptions, boolean divideSplitScreen, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(taskIds);
                    _data.writeTypedList(bOptions);
                    _data.writeInt(divideSplitScreen ? 1 : 0);
                    _data.writeInt(flag);
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startActivitiesFromRecents(taskIds, bOptions, divideSplitScreen, flag);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Rect resizeActivityStack(IBinder token, Rect bounds, boolean isAlwaysOnTop) throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    int i = 1;
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!isAlwaysOnTop) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(76, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resizeActivityStack(token, bounds, isAlwaysOnTop);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getVirtualDisplayId(String castType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(castType);
                    if (!this.mRemote.transact(77, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVirtualDisplayId(castType);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean moveStacksToDisplay(int fromDisplayId, int toDisplayId, boolean isOnlyFocus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fromDisplayId);
                    _data.writeInt(toDisplayId);
                    boolean _result = true;
                    _data.writeInt(isOnlyFocus ? 1 : 0);
                    if (!this.mRemote.transact(78, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().moveStacksToDisplay(fromDisplayId, toDisplayId, isOnlyFocus);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isDisplayHoldScreen(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    boolean _result = false;
                    if (!this.mRemote.transact(79, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDisplayHoldScreen(displayId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isPadCastMaxSizeEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(80, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPadCastMaxSizeEnable();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isMirrorCast(String castType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(castType);
                    boolean _result = false;
                    if (!this.mRemote.transact(81, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isMirrorCast(castType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getTopFocusedDisplayId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(82, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTopFocusedDisplayId();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getActivityDisplayId(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(83, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActivityDisplayId(pid, uid);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isStatusBarPermenantlyShowing() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(84, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isStatusBarPermenantlyShowing();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void moveTaskToFrontForMultiDisplay(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(85, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveTaskToFrontForMultiDisplay(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void hwResizeTaskForMultiDisplay(int taskId, Rect bounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(86, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwResizeTaskForMultiDisplay(taskId, bounds);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void moveTaskBackwardsForMultiDisplay(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(87, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveTaskBackwardsForMultiDisplay(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setFocusedTaskForMultiDisplay(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(88, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFocusedTaskForMultiDisplay(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setPCFullSize(int fullWidth, int fullHeight, int phoneOrientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fullWidth);
                    _data.writeInt(fullHeight);
                    _data.writeInt(phoneOrientation);
                    if (this.mRemote.transact(89, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPCFullSize(fullWidth, fullHeight, phoneOrientation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setPCVirtualSize(int virtualWidth, int virtualHeight, int phoneOrientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(virtualWidth);
                    _data.writeInt(virtualHeight);
                    _data.writeInt(phoneOrientation);
                    if (this.mRemote.transact(90, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPCVirtualSize(virtualWidth, virtualHeight, phoneOrientation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setPCMultiCastMode(boolean isPCMultiCastMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isPCMultiCastMode ? 1 : 0);
                    if (this.mRemote.transact(91, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPCMultiCastMode(isPCMultiCastMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setCurOrientation(int curOrientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(curOrientation);
                    if (this.mRemote.transact(92, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCurOrientation(curOrientation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getPCVirtualWidth() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(93, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPCVirtualWidth();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getPCVirtualHeight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(94, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPCVirtualHeight();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getPCFullWidth() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(95, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPCFullWidth();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getPCFullHeight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(96, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPCFullHeight();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void registerMultiDisplayMessenger(Messenger messenger) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(97, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerMultiDisplayMessenger(messenger);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void unregisterMultiDisplayMessenger(Messenger messenger) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(98, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterMultiDisplayMessenger(messenger);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void hwTogglePCFloatWindow(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(99, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwTogglePCFloatWindow(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void hwTogglePhoneFullScreen(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(100, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwTogglePhoneFullScreen(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public List<Bundle> getTaskList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(101, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTaskList();
                    }
                    _reply.readException();
                    List<Bundle> _result = _reply.createTypedArrayList(Bundle.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getCurTopFullScreenTaskState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(102, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurTopFullScreenTaskState();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getCurPCWindowAreaNum() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(103, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurPCWindowAreaNum();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public List<Bundle> getLastRencentTaskList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(104, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastRencentTaskList();
                    }
                    _reply.readException();
                    List<Bundle> _result = _reply.createTypedArrayList(Bundle.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bundle getActivityOptionFromAppProcess(IApplicationThread thread) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(thread != null ? thread.asBinder() : null);
                    if (!this.mRemote.transact(105, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActivityOptionFromAppProcess(thread);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public float getStackScale(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (!this.mRemote.transact(106, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStackScale(taskId);
                    }
                    _reply.readException();
                    float _result = _reply.readFloat();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int retrievePCMultiWinConfig(String config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(config);
                    if (!this.mRemote.transact(107, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().retrievePCMultiWinConfig(config);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getPcWidth() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(108, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPcWidth();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public int getPcHeight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(109, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPcHeight();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setPcSize(int pcWidth, int pcHeight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pcWidth);
                    _data.writeInt(pcHeight);
                    if (this.mRemote.transact(110, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPcSize(pcWidth, pcHeight);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setMultiDisplayParamsWithType(int type, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(111, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMultiDisplayParamsWithType(type, bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Rect getLocalLayerRectForMultiDisplay() throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(112, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLocalLayerRectForMultiDisplay();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Rect getLocalDisplayRectForMultiDisplay() throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(113, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLocalDisplayRectForMultiDisplay();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Rect getVirtualLayerRectForMultiDisplay() throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(114, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVirtualLayerRectForMultiDisplay();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Rect getVirtualDisplayRectForMultiDisplay() throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(115, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVirtualDisplayRectForMultiDisplay();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Rect adjustScreenShotRectForPCCast(Rect sourceCrop) throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sourceCrop != null) {
                        _data.writeInt(1);
                        sourceCrop.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(116, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().adjustScreenShotRectForPCCast(sourceCrop);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void hwSetRequestedOrientation(int taskId, int requestedOrientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(requestedOrientation);
                    if (this.mRemote.transact(117, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hwSetRequestedOrientation(taskId, requestedOrientation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void setStackWindowingMode(IBinder token, int windowingMode, Rect bounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(windowingMode);
                    if (bounds != null) {
                        _data.writeInt(1);
                        bounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(118, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setStackWindowingMode(token, windowingMode, bounds);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void updateFloatingBallPos(Rect pos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pos != null) {
                        _data.writeInt(1);
                        pos.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(119, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateFloatingBallPos(pos);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void notifyCameraStateForAtms(Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(120, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyCameraStateForAtms(options);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean minimizeHwFreeForm(IBinder token, String packageName, boolean nonRoot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(nonRoot ? 1 : 0);
                    if (!this.mRemote.transact(121, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().minimizeHwFreeForm(token, packageName, nonRoot);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void notifyLauncherAction(String category, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(category);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(122, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyLauncherAction(category, bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bitmap getApplicationIcon(IBinder activityToken, boolean isCheckAppLock) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activityToken);
                    _data.writeInt(isCheckAppLock ? 1 : 0);
                    if (!this.mRemote.transact(123, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApplicationIcon(activityToken, isCheckAppLock);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public boolean isSupportDragToSplitScreen(IBinder token, boolean isCheckAppLock) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = true;
                    _data.writeInt(isCheckAppLock ? 1 : 0);
                    if (!this.mRemote.transact(124, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportDragToSplitScreen(token, isCheckAppLock);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public List<String> getVisibleCanShowWhenLockedPackages(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(125, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVisibleCanShowWhenLockedPackages(displayId);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public Bundle getFreeformBoundsInCenter(int displayId, int centerX) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(centerX);
                    if (!this.mRemote.transact(126, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFreeformBoundsInCenter(displayId, centerX);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityTaskManager
            public void notifyNotificationAnimationFinish(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(127, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyNotificationAnimationFinish(displayId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwActivityTaskManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwActivityTaskManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
