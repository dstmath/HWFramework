package com.android.server.wm;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.HwPCMultiWindowCompatibility;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.service.voice.IVoiceInteractionSessionEx;
import android.util.HwPCUtils;
import android.util.Log;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.internal.app.IVoiceInteractorEx;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.server.HwPCFactory;
import java.util.ArrayList;

public class HwTaskRecord extends TaskRecordBridgeEx {
    public int dragFullMode = 0;
    private boolean mIsInLockScreen = false;
    private boolean mIsLaunchBoundsFirst = true;
    boolean mIsSaveBounds = true;
    private boolean mIsWindowStateBeforeLockScreenValid = false;
    private int mWindowStateBeforeLockScreen = 4;

    public HwTaskRecord(ActivityTaskManagerServiceEx service, int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSessionEx voiceSession, IVoiceInteractorEx voiceInteractor) {
        super(service, taskId, info, intent, voiceSession, voiceInteractor);
        if (HwPCUtils.isPcCastModeInServer()) {
            setRootActivityInfo(info);
        }
    }

    public HwTaskRecord(ActivityTaskManagerServiceEx service, int taskId, ActivityInfo info, Intent intent, ActivityManager.TaskDescription taskDescription) {
        super(service, taskId, info, intent, taskDescription);
        if (HwPCUtils.isPcCastModeInServer()) {
            setRootActivityInfo(info);
        }
    }

    public HwTaskRecord(ActivityTaskManagerServiceEx service, int taskId, Intent intent, Intent affinityIntent, String affinity, String rootAffinity, ComponentName realActivity, ComponentName origActivity, boolean isRootWasReset, boolean isAutoRemoveRecents, boolean isAskedCompatMode, int userId, int effectiveUid, String lastDescription, ArrayList<ActivityRecordEx> activities, long lastTimeMoved, boolean isNeverRelinquishIdentity, ActivityManager.TaskDescription lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean isSupportsPictureInPicture, boolean isRealActivitySuspended, boolean isUserSetupComplete, int minWidth, int minHeight) {
        super(service, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, isRootWasReset, isAutoRemoveRecents, isAskedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeMoved, isNeverRelinquishIdentity, lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, isSupportsPictureInPicture, isRealActivitySuspended, isUserSetupComplete, minWidth, minHeight);
        if (HwPCUtils.isPcCastModeInServer()) {
            new ArrayList();
            ArrayList<ActivityRecordEx> activityRecordExes = getActivityRecordExs();
            if (activityRecordExes.size() > 0) {
                setRootActivityInfo(activityRecordExes.get(0).getInfo());
            }
        }
    }

    @SuppressLint({"AvoidMax/Min"})
    public void overrideConfigOrienForFreeForm(Configuration config) {
        ActivityRecordEx topActivity = getTopActivity();
        if (topActivity != null && !topActivity.isEmpty()) {
            int i = 1;
            if (inFreeformWindowingMode()) {
                ApplicationInfo info = getApplicationInfo();
                if (info != null && (info.flags & 1) != 0) {
                    Resources res = getService().getContext().getResources();
                    Configuration serviceConfig = getConfiguration();
                    if (res.getConfiguration().orientation == 1) {
                        config.orientation = 1;
                    } else if (serviceConfig != null) {
                        if (config.screenWidthDp > Math.min(serviceConfig.screenWidthDp, serviceConfig.screenHeightDp)) {
                            i = 2;
                        }
                        config.orientation = i;
                    }
                }
            } else if (inSplitScreenWindowingMode()) {
                if (getService().isInMWPortraitWhiteList(topActivity.getPackageName())) {
                    config.orientation = 1;
                }
            } else if (Log.HWINFO) {
                Log.i("HwTaskRecord", "Config freeform for other mode");
            }
        }
    }

    public void setWindowState(int state) {
        if (getWindowState() != state) {
            HwPCUtils.log("HwPCMultiWindowManager", "setWindowState: " + Integer.toHexString(getWindowState()) + " to " + Integer.toHexString(state));
            setWindowStateEx(state);
            if (getService().isHwActivityStackSupervisor()) {
                scheduleReportPCWindowStateChangedLocked(buildTaskRecordEx());
            }
            notifyTaskProfileLocked(getTaskId(), getWindowState());
        }
    }

    /* access modifiers changed from: protected */
    public void updateHwPCMultiCastOverrideConfiguration(Rect bounds) {
        HwMultiDisplayManager multiDisplayManager = getHwMultiDisplayManager();
        ActivityStackEx activityStackEx = getActivityStack();
        if (activityStackEx != null && multiDisplayManager != null && HwActivityTaskManager.isPCMultiCastMode()) {
            adjustProcessGlobalConfigLocked(this, bounds, aospGetStack().getWindowingMode());
            if (!activityStackEx.inHwSplitScreenWindowingMode()) {
                onNotifyTaskModeChange(aospGetTaskId(), bounds);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateHwOverrideConfiguration(Rect bounds) {
        int windowStateLayout;
        DefaultHwPCMultiWindowManager multiWindowMgr = getHwPCMultiWindowManager(buildAtmsEx());
        if (getActivityStack() != null && !getActivityStack().isActivityStackNull() && HwPCUtils.isExtDynamicStack(getActivityStack().getStackId()) && multiWindowMgr != null) {
            if (bounds == null || bounds.isEmpty()) {
                setLastNonFullscreenBounds(aospGetRequestedOverrideBounds());
                windowStateLayout = 4;
            } else if (multiWindowMgr.getMaximizedBounds().equals(aospGetRequestedOverrideBounds())) {
                windowStateLayout = 3;
            } else if (multiWindowMgr.getSplitLeftWindowBounds().equals(aospGetRequestedOverrideBounds())) {
                setLastNonFullscreenBounds(aospGetRequestedOverrideBounds());
                windowStateLayout = 5;
            } else if (multiWindowMgr.getSplitRightWindowBounds().equals(aospGetRequestedOverrideBounds())) {
                setLastNonFullscreenBounds(aospGetRequestedOverrideBounds());
                windowStateLayout = 6;
            } else if (aospGetRequestedOverrideBounds().width() > aospGetRequestedOverrideBounds().height()) {
                windowStateLayout = 2;
            } else {
                windowStateLayout = 1;
            }
            int finalState = (getNextWindowState() & 65280) | windowStateLayout;
            ActivityRecordEx topActivity = getTopActivity();
            if (topActivity != null && multiWindowMgr.isSpecialVideo(topActivity.getPackageName())) {
                finalState |= AwarenessConstants.MSDP_ENVIRONMENT_TYPE_HOME;
            }
            if (getWindowState() != finalState && !HwPCUtils.enabledInPad()) {
                if (Log.HWINFO) {
                    HwPCUtils.log("HwPCMultiWindowManager", "force to update task:" + toString());
                }
                multiWindowMgr.setForceUpdateTask(getTaskId());
            }
            setWindowState(finalState);
            multiWindowMgr.storeTaskSettings(buildTaskRecordEx());
        }
    }

    /* access modifiers changed from: protected */
    public boolean isMaximizedPortraitAppOnPCMode(String packageName) {
        if (!HwPCUtils.isPcCastModeInServer() || getStackEx() == null || !HwPCUtils.isValidExtDisplayId(getStackEx().getDisplayId()) || !getHwPCMultiWindowManager(getService()).getPortraitMaximizedPkgList().contains(packageName)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setStack(ActivityStackEx stack) {
        if (stack != null && !stack.isActivityStackNull() && HwPCUtils.isExtDynamicStack(stack.getStackId())) {
            int density = getService().getWindowManager().getBaseDisplayDensity(0);
            int extDensity = getService().getWindowManager().getBaseDisplayDensity(stack.getDisplayId());
            Point size = new Point();
            getService().getWindowManager().getBaseDisplaySize(stack.getDisplayId(), size);
            if (density > 0 && extDensity > 0) {
                float ratio = (((float) extDensity) * 1.0f) / ((float) density);
                if (getMinWidth() == -1) {
                    setMinWidth(getService().getRootActivityContainer().getDefaultMinSizeOfResizeableTaskDp());
                }
                if (getMinHeight() == -1) {
                    setMinHeight(getService().getRootActivityContainer().getDefaultMinSizeOfResizeableTaskDp());
                }
                int halfWidth = size.x / 2;
                setMinWidth((int) (((float) getMinWidth()) * ratio));
                setMinWidth(getMinWidth() > halfWidth ? halfWidth : getMinWidth());
                int halfHeight = size.y / 2;
                setMinHeight((int) (((float) getMinHeight()) * ratio));
                setMinHeight(getMinHeight() > halfHeight ? halfHeight : getMinHeight());
            }
        }
        setStackEx(stack);
    }

    public Rect getLaunchBounds() {
        DefaultHwPCMultiWindowManager multiWindowMgr;
        if (getStackEx() == null || !HwPCUtils.isExtDynamicStack(getStackEx().getStackId()) || !this.mIsLaunchBoundsFirst || getRootActivityInfo() == null || (multiWindowMgr = getHwPCMultiWindowManager(buildAtmsEx())) == null) {
            return getLaunchBoundsEx();
        }
        this.mIsLaunchBoundsFirst = false;
        multiWindowMgr.restoreTaskWindowState(buildTaskRecordEx());
        setLastNonFullscreenBounds(multiWindowMgr.getLaunchBounds(buildTaskRecordEx()));
        return getLastNonFullscreenBounds();
    }

    /* access modifiers changed from: package-private */
    public boolean removeActivity(ActivityRecordEx activityRecord, boolean isReparenting) {
        DefaultHwPCMultiWindowManager multiWindowMgr;
        boolean isPrecheckRst = false;
        if (activityRecord == null || activityRecord.isEmpty()) {
            HwPCUtils.log("HwPCMultiWindowManager", "input parameter ActivityRecord is null");
            return false;
        }
        boolean isRemoveSuccess = removeActivityEx(activityRecord, isReparenting);
        boolean isChanged = false;
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && !(isChanged = checkInKeyGuard()) && this.mIsInLockScreen && getTopActivity() != null) {
            isChanged = activityRecord.canShowWhenLocked() == getTopActivity().canShowWhenLocked();
        }
        if (((getStackEx() != null && HwPCUtils.isExtDynamicStack(getStackEx().getStackId())) && getTopActivity() != null && isTopActivityBeHwActivityRecord() && isHwActivityRecord(activityRecord)) && (comparemCustomRequestedOrientation(activityRecord) || isChanged)) {
            isPrecheckRst = true;
        }
        if (isPrecheckRst && (getService() instanceof ActivityTaskManagerServiceEx) && (multiWindowMgr = getHwPCMultiWindowManager(getService())) != null) {
            int customRequestedOrientation = getCustomRequestedOrientation();
            if (Log.HWINFO) {
                HwPCUtils.log("HwPCMultiWindowManager", "removeActivity: (" + activityRecord.toString() + ")");
                HwPCUtils.log("HwPCMultiWindowManager", "newTopActivity: (" + getTopActivity().toString() + ")[" + customRequestedOrientation + "]");
            }
            TaskRecordEx taskRecordEx = buildTaskRecordEx();
            if (customRequestedOrientation == 0) {
                this.mIsSaveBounds = true;
                multiWindowMgr.restoreTaskWindowState(taskRecordEx);
                multiWindowMgr.resizeTaskFromPC(taskRecordEx, multiWindowMgr.getLaunchBounds(taskRecordEx));
            } else if (HwPCUtils.enabledInPad()) {
                multiWindowMgr.updateTaskByRequestedOrientation(taskRecordEx, customRequestedOrientation);
            }
        }
        return isRemoveSuccess;
    }

    /* access modifiers changed from: package-private */
    public void createTask(boolean isOnTop, boolean isShowForAllUsers) {
        createTaskEx(isOnTop, isShowForAllUsers);
        if (getRootActivityInfo() != null && getActivityStack() != null && !getActivityStack().isActivityStackNull() && HwPCUtils.isExtDynamicStack(getActivityStack().getStackId())) {
            getService().notifyTaskCreated(getTaskId(), getRootActivityInfo().getComponentName());
        }
    }

    /* access modifiers changed from: package-private */
    public void removeWindowContainer() {
        removeWindowContainerEx();
        if (getActivityStack() != null && !getActivityStack().isActivityStackNull() && HwPCUtils.isExtDynamicStack(getActivityStack().getStackId())) {
            getService().notifyTaskRemoved(getTaskId());
        }
    }

    /* access modifiers changed from: protected */
    public boolean isResizeable(boolean isSupportsPip) {
        if (getActivityStack() == null || getActivityStack().isActivityStackNull() || !HwPCUtils.isExtDynamicStack(getActivityStack().getStackId())) {
            return isResizeableEx(isSupportsPip);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void adjustForMinimalTaskDimensions(Rect bounds, Rect previousBounds) {
        if (SystemProperties.getInt("persist.sys.rog.configmode", 0) == 1) {
            reCalculateDefaultMinimalSizeOfResizeableTasks();
        }
        setDefaultMinSize(getService().getRootActivityContainer().getDefaultMinSizeOfResizeableTaskDp());
        Rect originBounds = new Rect();
        Point size = new Point();
        if (HwPCUtils.isExtDynamicStack(getStackId())) {
            originBounds.set(bounds);
            int densityDpi = getConfiguration().densityDpi;
            int serviceDpi = getService().getGlobalConfiguration().densityDpi;
            if (densityDpi > 0 && serviceDpi > 0) {
                setDefaultMinSize((getDefaultMinSize() * densityDpi) / serviceDpi);
                if (!(getService() == null || getService().getWindowManager() == null || getStackEx() == null)) {
                    getService().getWindowManager().getBaseDisplaySize(getStackEx().getDisplayId(), size);
                    int minSizePx = (int) (((float) (size.x < size.y ? size.x : size.y)) * 0.2f);
                    setDefaultMinSize((getDefaultMinSize() <= minSizePx || minSizePx == 0) ? getDefaultMinSize() : minSizePx);
                }
            }
        }
        adjustForMinimalTaskDimensionsEx(bounds, previousBounds);
        if (HwPCUtils.isExtDynamicStack(getStackId())) {
            updateBoundsByRatio(originBounds, bounds, size);
        }
    }

    private void updateBoundsByRatio(Rect oldBound, Rect newBound, Point displaySize) {
        boolean isPreCheckDisplay = false;
        boolean isPreCheckBound = oldBound == null || oldBound.isEmpty() || newBound == null || newBound.isEmpty();
        if (displaySize == null || displaySize.x <= 0 || displaySize.y <= 0) {
            isPreCheckDisplay = true;
        }
        if (!isPreCheckBound && !isPreCheckDisplay) {
            int oldWidth = oldBound.right - oldBound.left;
            int oldHeight = oldBound.bottom - oldBound.top;
            if (oldWidth != 0 && oldHeight != 0) {
                int newWidth = newBound.right - newBound.left;
                int newHeight = newBound.bottom - newBound.top;
                if (newWidth != 0 && newHeight != 0) {
                    float ratio = ((float) oldWidth) / ((float) oldHeight);
                    if (newWidth != oldWidth) {
                        int tmpHeight = (int) (((float) newWidth) / ratio);
                        if (((float) tmpHeight) > ((float) displaySize.y) * 0.8f) {
                            newBound.set(oldBound);
                        } else {
                            newBound.bottom = newBound.top + tmpHeight;
                        }
                    } else if (newHeight != oldHeight) {
                        int tmpWidth = (int) (((float) newHeight) * ratio);
                        if (((float) tmpWidth) > ((float) displaySize.x) * 0.8f) {
                            newBound.set(oldBound);
                        } else {
                            newBound.right = newBound.left + tmpWidth;
                        }
                    }
                }
            }
        }
    }

    private boolean checkInKeyGuard() {
        HwPCUtils.log("HwPCMultiWindowManager", "checkInKeyGuard");
        boolean isInLockScreen = this.mIsInLockScreen;
        if (((KeyguardManager) getService().getContext().getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
            HwPCUtils.log("HwPCMultiWindowManager", "checkInKeyGuard mIsInLockScreen true");
            this.mIsInLockScreen = true;
        } else {
            this.mIsInLockScreen = false;
            HwPCUtils.log("HwPCMultiWindowManager", "checkInKeyGuard mIsInLockScreen false");
        }
        return isInLockScreen != this.mIsInLockScreen;
    }

    /* access modifiers changed from: package-private */
    public void activityResumedInTop() {
        int i;
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            checkInKeyGuard();
            if (getWindowState() != 4 && this.mIsInLockScreen) {
                HwPCUtils.log("HwPCMultiWindowManager", "activityResumedInTop WINDOW_FULLSCREEN");
                this.mWindowStateBeforeLockScreen = getWindowState();
                this.mIsWindowStateBeforeLockScreenValid = true;
                setWindowState(4);
                setBounds(null);
            } else if (this.mIsInLockScreen || !this.mIsWindowStateBeforeLockScreenValid || getWindowState() == (i = this.mWindowStateBeforeLockScreen)) {
                HwPCUtils.log("HwPCMultiWindowManager", "Resumed in top for activity for other mode.");
            } else {
                setWindowState(i);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addActivityToTop(ActivityRecordEx activityRecord) {
        HwTaskRecord.super.addActivityToTopEx(activityRecord);
        if (HwPCUtils.isExtDynamicStack(getStackId()) && (getService() instanceof ActivityTaskManagerServiceEx) && activityRecord.getInfo() != null && isTopActivityBeHwActivityRecord()) {
            DefaultHwPCMultiWindowManager multiWindowMgr = getHwPCMultiWindowManager(buildAtmsEx());
            int requestedOrientation = activityRecord.getInfo().screenOrientation;
            int newCustomRequestOrientation = 0;
            if (multiWindowMgr != null) {
                if (multiWindowMgr.isFixedOrientationPortrait(requestedOrientation)) {
                    newCustomRequestOrientation = 1;
                } else if (multiWindowMgr.isFixedOrientationLandscape(requestedOrientation)) {
                    newCustomRequestOrientation = 2;
                }
            }
            if (newCustomRequestOrientation != 0) {
                int customRequestedOrientation = 1;
                if (HwPCMultiWindowCompatibility.getWindowStateLayout(getWindowState()) != 1) {
                    customRequestedOrientation = 2;
                }
                HwPCUtils.log("HwPCMultiWindowManager", "requestedOrientation:" + newCustomRequestOrientation + " oldRequestedOrientation:" + customRequestedOrientation);
                if (newCustomRequestOrientation != customRequestedOrientation) {
                    activityRecord.setRequestedOrientation(newCustomRequestOrientation);
                }
            }
            activityResumedInTop();
        }
    }

    public ArrayList<ActivityRecordEx> getActivities() {
        return getActivityRecordExs();
    }

    private ActivityTaskManagerServiceEx buildAtmsEx() {
        return getService();
    }

    private DefaultHwPCMultiWindowManager getHwPCMultiWindowManager(ActivityTaskManagerServiceEx atmsEx) {
        return HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwPCMultiWindowManager(atmsEx);
    }

    public boolean isSaveBounds() {
        return this.mIsSaveBounds;
    }

    public void setSaveBounds(boolean isSaveBounds) {
        this.mIsSaveBounds = isSaveBounds;
    }

    public int getDragFullMode() {
        return this.dragFullMode;
    }

    public void setDragFullMode(int mode) {
        this.dragFullMode = mode;
    }
}
