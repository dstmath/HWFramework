package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.HwRecentTaskInfo;
import android.app.IActivityController;
import android.app.IHwActivityNotifier;
import android.app.IHwDockCallBack;
import android.app.ITaskStackListener;
import android.app.usage.UsageStatsManagerInternal;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.Messenger;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.app.IGameObserverEx;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IHwActivityTaskManagerServiceEx {
    boolean addGameSpacePackageList(List<String> list);

    void addStackReferenceIfNeeded(ActivityStack activityStack);

    void addSurfaceInNotchIfNeed();

    void adjustActivityOptionsForPCCast(ActivityRecord activityRecord, ActivityOptions activityOptions);

    void adjustHwFreeformPosIfNeed(DisplayContent displayContent, boolean z);

    void adjustProcessGlobalConfigLocked(TaskRecord taskRecord, Rect rect, int i);

    Rect adjustScreenShotRectForPCCast(Rect rect);

    boolean blockSwipeFromTop(MotionEvent motionEvent, DisplayContent displayContent);

    void calcHwMultiWindowStackBoundsForConfigChange(ActivityStack activityStack, Rect rect, Rect rect2, int i, int i2, int i3, int i4, boolean z);

    void call(Bundle bundle);

    int canAppBoost(ActivityInfo activityInfo, boolean z);

    boolean canCleanTaskRecord(String str, int i, String str2);

    void captureScreenToPc(SurfaceControl.ScreenshotGraphicBuffer screenshotGraphicBuffer);

    Intent changeStartActivityIfNeed(Intent intent);

    Rect checkBoundInheritFromSource(ActivityRecord activityRecord, TaskRecord taskRecord);

    boolean checkTaskId(int i);

    boolean computeBounds(ActivityRecord activityRecord, Rect rect);

    boolean customActivityResuming(String str);

    boolean delGameSpacePackageList(List<String> list);

    void dismissSplitScreenModeWithFinish(ActivityRecord activityRecord);

    void dismissSplitScreenToFocusedStack();

    void dispatchActivityLifeState(ActivityRecord activityRecord, String str);

    void dispatchFreeformBallLifeState(List<TaskRecord> list, String str);

    void doReplaceSplitStack(ActivityStack activityStack);

    void dumpHwFreeFormBoundsRecords(PrintWriter printWriter, String str, String[] strArr);

    void ensureTaskRemovedForPCCast(int i);

    void enterCoordinationMode();

    boolean enterCoordinationMode(Intent intent);

    void exitCoordinationMode();

    boolean exitCoordinationMode(boolean z, boolean z2);

    void exitSingleHandMode();

    List<ActivityStack> findCombinedSplitScreenStacks(ActivityStack activityStack);

    void finishRootActivity(ActivityRecord activityRecord);

    void focusStackChange(int i, int i2, ActivityStack activityStack, ActivityStack activityStack2);

    ActivityManager.TaskSnapshot getActivityTaskSnapshot(IBinder iBinder, boolean z);

    int getActivityWindowMode(IBinder iBinder);

    Map<String, Boolean> getAppUserAwarenessState(int i, List<String> list);

    Bitmap getApplicationIcon(IBinder iBinder, boolean z);

    float getAspectRatioWithUserSet(String str, String str2, ActivityInfo activityInfo);

    int getCaptionState(IBinder iBinder);

    int[] getCombinedSplitScreenTaskIds(ActivityStack activityStack);

    int getCurPCWindowAreaNum();

    int getCurTopFullScreenTaskState();

    Point getDragBarCenterPoint(Rect rect, ActivityStack activityStack);

    List<ActivityManager.RecentTaskInfo> getFilteredTasks(int i, int i2, String str, int[] iArr, boolean z, int i3);

    Bundle getFreeformBoundsInCenter(int i, int i2);

    List<String> getGameList();

    Rect getHwMagicWinMiddleBounds(int i);

    float getHwMultiWinCornerRadius(int i);

    Bundle getHwMultiWindowAppControlLists();

    Bundle getHwMultiWindowState();

    HwRecentTaskInfo getHwRecentTaskInfo(int i);

    TaskChangeNotificationController getHwTaskChangeController();

    List<Bundle> getLastRencentTaskList();

    Rect getLocalDisplayRectForMultiDisplay();

    Rect getLocalLayerRectForMultiDisplay();

    boolean getMultiWindowDisabled();

    int getPCFullHeight();

    int getPCFullWidth();

    Rect getPCTopTaskBounds(int i);

    int getPCVirtualHeight();

    int getPCVirtualWidth();

    Map<String, String> getPackageNameRotations();

    Bitmap getPadCastWallpaperBitmap();

    int getPcHeight();

    int getPcWidth();

    HashMap<String, Integer> getPkgDisplayMaps();

    int getPreferedDisplayId(ActivityRecord activityRecord, ActivityOptions activityOptions, int i);

    float getReusableHwFreeFormBounds(String str, int i, Rect rect);

    float getReusableHwFreeFormBoundsById(int i, Rect rect);

    float[] getScaleRange(ActivityStack activityStack);

    Bundle getSplitStacksPos(int i, int i2);

    float getStackScale(int i);

    List<Bundle> getTaskList();

    ActivityManager.TaskSnapshot getTaskSnapshot(int i, boolean z);

    Bitmap getTaskThumbnailOnPCMode(int i);

    Bundle getTopActivity();

    int getTopTaskIdInDisplay(int i, String str, boolean z);

    int getVirtualDisplayId(String str);

    Rect getVirtualDisplayRectForMultiDisplay();

    Rect getVirtualLayerRectForMultiDisplay();

    List<String> getVisibleCanShowWhenLockedPackages(int i);

    List<String> getVisiblePackages();

    List<ActivityManager.RunningTaskInfo> getVisibleTasks();

    int getWindowState(IBinder iBinder);

    void handleActivityResumedForPCCast(ActivityRecord activityRecord);

    void handleMultiWindowSwitch(IBinder iBinder, Bundle bundle);

    Rect handleStackFromOneStep(ActivityRecord activityRecord, ActivityStack activityStack, ActivityRecord activityRecord2);

    void hwResizeTask(int i, Rect rect);

    void hwResizeTaskForMultiDisplay(int i, Rect rect);

    void hwRestoreTask(int i, float f, float f2);

    void hwSetRequestedOrientation(int i, int i2);

    void hwTogglePCFloatWindow(int i);

    void hwTogglePhoneFullScreen(int i);

    void hwTogglePhoneFullScreenFromLauncherOrRecent(int i);

    boolean isActivityVisiableInFingerBoost(ActivityRecord activityRecord);

    boolean isAllowToStartActivity(Context context, String str, ActivityInfo activityInfo, boolean z, ActivityInfo activityInfo2);

    boolean isDisplayHoldScreen(int i);

    boolean isExSplashEnable(Bundle bundle);

    boolean isFreeFormVisible();

    boolean isFullScreen(IBinder iBinder);

    boolean isGameDndOn();

    boolean isGameDndOnEx();

    boolean isGameGestureDisabled();

    boolean isGameKeyControlOn();

    boolean isHwFreeFormOnlyApp(String str);

    boolean isInDisplaySurfaceScaled();

    boolean isInGameSpace(String str);

    boolean isInMultiWindowMode();

    boolean isMagicWinExcludeTaskFromRecents(TaskRecord taskRecord);

    boolean isMagicWinSkipRemoveFromRecentTasks(TaskRecord taskRecord, TaskRecord taskRecord2);

    boolean isMaximizedPortraitAppOnPCMode(ActivityRecord activityRecord);

    boolean isMirrorCast(String str);

    boolean isNeedAdapterCaptionView(String str);

    boolean isNeedSkipForceStopForHwMultiWindow(String str, int i, String str2);

    boolean isNerverUseSizeCompateMode(String str);

    boolean isNewPcMultiCastMode();

    boolean isOverrideConfigByMagicWin(Configuration configuration);

    boolean isPadCastMaxSizeEnable();

    boolean isPadCastStack(ActivityStack activityStack);

    boolean isPhoneLandscape(DisplayContent displayContent);

    boolean isResizableApp(String str, int i);

    boolean isSpecialVideoForPCMode(ActivityRecord activityRecord);

    boolean isSplitStackVisible(ActivityDisplay activityDisplay, int i);

    boolean isStartAppLock(String str, String str2);

    boolean isStatusBarPermenantlyShowing();

    boolean isSupportDragForMultiWin(IBinder iBinder);

    boolean isSupportDragToSplitScreen(IBinder iBinder, boolean z);

    boolean isSupportsSplitScreenWindowingMode(IBinder iBinder);

    boolean isSwitchToMagicWin(int i, boolean z, int i2);

    boolean isTaskNotResizeableEx(TaskRecord taskRecord, Rect rect);

    boolean isTaskSupportResize(int i, boolean z, boolean z2);

    boolean isTaskVisible(int i);

    boolean isVideosNeedFullScreenInConfig(String str);

    boolean isVirtualDisplayId(int i, String str);

    void loadPadCastBlackList(String str);

    void maximizeHwFreeForm();

    boolean minimizeHwFreeForm(IBinder iBinder, String str, boolean z);

    void moveActivityTaskToBackEx(IBinder iBinder);

    void moveStackToFrontEx(ActivityOptions activityOptions, ActivityStack activityStack, ActivityRecord activityRecord, ActivityRecord activityRecord2, Rect rect);

    boolean moveStacksToDisplay(int i, int i2, boolean z);

    void moveTaskBackwards(int i);

    void moveTaskBackwardsForMultiDisplay(int i);

    void moveTaskToFrontForMultiDisplay(int i);

    void noteActivityDisplayed(String str, int i, int i2, boolean z);

    boolean noteActivityInitializing(ActivityRecord activityRecord, ActivityRecord activityRecord2);

    void noteActivityStart(String str, String str2, String str3, int i, int i2, boolean z);

    void notifyActivityState(ActivityRecord activityRecord, String str);

    void notifyCameraStateForAtms(Bundle bundle);

    void notifyDisplayModeChange(int i, int i2);

    void notifyDisplayStacksEmpty(int i);

    void notifyFullScreenStateChange(int i, boolean z);

    void notifyHoldScreenStateChange(String str, int i, int i2, int i3, String str2);

    void notifyLauncherAction(String str, Bundle bundle);

    void notifyNotificationAnimationFinish(int i);

    void notifyPopupCamera(String str);

    void notifySecureStateChange(int i, boolean z);

    void onCaptionDropAnimationDone(IBinder iBinder);

    void onDisplayConfigurationChanged(int i);

    void onEnteringPipForMultiDisplay(int i);

    void onEnteringSingleHandForMultiDisplay();

    void onMultiWindowModeChanged(boolean z);

    void onSystemReady();

    void onTaskStackChangedForMultiDisplay();

    void onWindowFocusChangedForMultiDisplay(AppWindowToken appWindowToken);

    void onWindowModeChange(int i, Rect rect);

    void oneStepHwMultiWindowBdReport(ActivityRecord activityRecord, int i, ActivityOptions activityOptions);

    void recordHwFreeFormBounds(TaskRecord taskRecord, boolean z);

    void registerBroadcastReceiver();

    void registerGameObserver(IGameObserver iGameObserver);

    void registerGameObserverEx(IGameObserverEx iGameObserverEx);

    void registerHwActivityNotifier(IHwActivityNotifier iHwActivityNotifier, String str);

    void registerHwTaskStackListener(ITaskStackListener iTaskStackListener);

    void registerMultiDisplayMessenger(Messenger messenger);

    boolean registerThirdPartyCallBack(IMWThirdpartyCallback iMWThirdpartyCallback);

    Rect relocateOffScreenWindow(Rect rect, ActivityStack activityStack, float f);

    void removeFreeformBallWhenDestroy(ActivityRecord activityRecord, TaskRecord taskRecord);

    void removeHwFreeFormBoundsRecord(String str, int i);

    void removeHwFreeFormBoundsRecordById(int i);

    void removeStackReferenceIfNeeded(ActivityStack activityStack);

    boolean removeTask(int i, IBinder iBinder, String str, boolean z, String str2);

    void removeTasks(int[] iArr);

    void reportAppWindowMode(int i, ActivityRecord activityRecord, int i2, String str);

    void reportAppWindowVisibleOrGone(ActivityRecord activityRecord);

    void reportHomeProcess(WindowProcessController windowProcessController);

    void reportPreviousInfo(int i, WindowProcessController windowProcessController);

    boolean requestContentNode(ComponentName componentName, Bundle bundle, int i);

    boolean requestContentOther(ComponentName componentName, Bundle bundle, int i);

    void resetHwFreeFormBoundsRecords(int i);

    Rect resizeActivityStack(IBinder iBinder, Rect rect, boolean z);

    void resumeCoordinationPrimaryStack(ActivityRecord activityRecord);

    int retrievePCMultiWinConfig(String str);

    void saveMultiWindowTipState(String str, int i);

    void setAlwaysOnTopOnly(ActivityDisplay activityDisplay, ActivityStack activityStack, boolean z, boolean z2);

    void setCallingPkg(String str);

    void setCurOrientation(int i);

    boolean setCustomActivityController(IActivityController iActivityController);

    boolean setDockCallBackInfo(IHwDockCallBack iHwDockCallBack, int i);

    void setFocusedTaskForMultiDisplay(int i);

    void setForegroundFreeFormNum(int i);

    int[] setFreeformStackVisibility(int i, int[] iArr, boolean z);

    void setHwWinCornerRaduis(WindowState windowState, SurfaceControl surfaceControl);

    void setMultiDisplayParamsWithType(int i, Bundle bundle);

    boolean setMultiWindowDisabled(boolean z);

    void setPCFullSize(int i, int i2, int i3);

    void setPCMultiCastMode(boolean z);

    void setPCVirtualSize(int i, int i2, int i3);

    void setPcSize(int i, int i2);

    void setRequestedOrientation(int i);

    void setResumedActivityUncheckLocked(ActivityRecord activityRecord, ActivityRecord activityRecord2, String str);

    void setSplitBarVisibility(boolean z);

    boolean setStackScale(int i, float f);

    void setStackWindowingMode(IBinder iBinder, int i, Rect rect);

    void setTaskStackHide(ActivityStack activityStack, boolean z);

    boolean shouldAbortSelfLaunchWhenReturnHome(String str, int i, int i2);

    boolean shouldPreventSendBroadcast(Intent intent, String str, int i, int i2, String str2, int i3);

    boolean shouldPreventStartActivity(ActivityInfo activityInfo, int i, int i2, String str, int i3, Intent intent, WindowProcessController windowProcessController, ActivityOptions activityOptions);

    boolean shouldPreventStartProvider(ProviderInfo providerInfo, int i, int i2, String str, int i3);

    boolean shouldPreventStartService(ServiceInfo serviceInfo, int i, int i2, String str, int i3);

    boolean shouldResumeCoordinationPrimaryStack();

    boolean showIncompatibleAppDialog(ActivityInfo activityInfo, String str);

    void showUninstallLauncherDialog(String str);

    boolean skipOverridePendingTransitionForMagicWindow(ActivityRecord activityRecord);

    boolean skipOverridePendingTransitionForPC(ActivityRecord activityRecord);

    void stackCreated(ActivityStack activityStack, ActivityRecord activityRecord);

    int[] startActivitiesFromRecents(int[] iArr, List<Bundle> list, boolean z, int i);

    void startExSplash(Bundle bundle, ActivityOptions activityOptions);

    void stopInterceptionWhenBackHome();

    void toggleFreeformWindowingMode(IBinder iBinder, String str);

    void toggleFreeformWindowingModeEx(ActivityRecord activityRecord);

    void toggleHome();

    void togglePCMode(boolean z, int i);

    void unRegisterHwTaskStackListener(ITaskStackListener iTaskStackListener);

    void unregisterGameObserver(IGameObserver iGameObserver);

    void unregisterGameObserverEx(IGameObserverEx iGameObserverEx);

    void unregisterHwActivityNotifier(IHwActivityNotifier iHwActivityNotifier);

    void unregisterMultiDisplayMessenger(Messenger messenger);

    boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback iMWThirdpartyCallback);

    void updateCameraRotatio(String str, int i, int i2);

    void updateDragFreeFormPos(ActivityStack activityStack);

    void updateFloatingBallPos(Rect rect);

    void updateFreeFormOutLine(int i);

    void updateFreeFormOutLineForFloating(IBinder iBinder, int i);

    void updateHwFreeformNotificationState(int i, String str);

    void updatePictureInPictureMode(ActivityRecord activityRecord, boolean z);

    void updateSplitBarPosForIm(int i, int i2);

    void updateTaskByRequestedOrientationForPCCast(int i, int i2);

    ActivityOptions updateToHwFreeFormIfNeeded(Intent intent, ActivityInfo activityInfo, TaskRecord taskRecord, int i, ActivityRecord activityRecord, ActivityOptions activityOptions);

    void updateUsageStatsForPCMode(ActivityRecord activityRecord, boolean z, UsageStatsManagerInternal usageStatsManagerInternal);

    void updateWindowForPcFreeForm(ActivityManager.RunningTaskInfo runningTaskInfo);
}
