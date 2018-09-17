package com.android.server.am;

import android.annotation.SuppressLint;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskThumbnailInfo;
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
import android.os.Process;
import android.os.SystemProperties;
import android.service.voice.IVoiceInteractionSession;
import android.util.HwPCUtils;
import com.android.internal.app.IVoiceInteractor;
import java.util.ArrayList;

public class HwTaskRecord extends TaskRecord {
    private boolean mGetLaunchBoundsFirst = true;
    private boolean mIsInLockScreen = false;
    boolean mSaveBounds = true;
    private int mWindowStateBeforeLockScreen = 4;
    private boolean mWindowStateBeforeLockScreenValid = false;

    public HwTaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, IVoiceInteractionSession _voiceSession, IVoiceInteractor _voiceInteractor, int type) {
        super(service, _taskId, info, _intent, _voiceSession, _voiceInteractor, type);
        if (HwPCUtils.isPcCastModeInServer()) {
            this.mRootActivityInfo = info;
        }
    }

    public HwTaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, TaskDescription _taskDescription, TaskThumbnailInfo _thumbnailInfo) {
        super(service, _taskId, info, _intent, _taskDescription, _thumbnailInfo);
        if (HwPCUtils.isPcCastModeInServer()) {
            this.mRootActivityInfo = info;
        }
    }

    public HwTaskRecord(ActivityManagerService service, int _taskId, Intent _intent, Intent _affinityIntent, String _affinity, String _rootAffinity, ComponentName _realActivity, ComponentName _origActivity, boolean _rootWasReset, boolean _autoRemoveRecents, boolean _askedCompatMode, int _taskType, int _userId, int _effectiveUid, String _lastDescription, ArrayList<ActivityRecord> activities, long _firstActiveTime, long _lastActiveTime, long lastTimeMoved, boolean neverRelinquishIdentity, TaskDescription _lastTaskDescription, TaskThumbnailInfo lastThumbnailInfo, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean privileged, boolean _realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        super(service, _taskId, _intent, _affinityIntent, _affinity, _rootAffinity, _realActivity, _origActivity, _rootWasReset, _autoRemoveRecents, _askedCompatMode, _taskType, _userId, _effectiveUid, _lastDescription, activities, _firstActiveTime, _lastActiveTime, lastTimeMoved, neverRelinquishIdentity, _lastTaskDescription, lastThumbnailInfo, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, privileged, _realActivitySuspended, userSetupComplete, minWidth, minHeight);
        if (HwPCUtils.isPcCastModeInServer() && this.mActivities.size() > 0) {
            this.mRootActivityInfo = ((ActivityRecord) this.mActivities.get(0)).info;
        }
    }

    @SuppressLint({"AvoidMax/Min"})
    public void overrideConfigOrienForFreeForm(Configuration config) {
        int i = 1;
        ActivityRecord topActivity = getTopActivity();
        if (topActivity != null) {
            if (getStack().mStackId == 2) {
                ApplicationInfo info = this.mService.getPackageManagerInternalLocked().getApplicationInfo(topActivity.packageName, 0, Process.myUid(), this.userId);
                if (info != null && (info.flags & 1) != 0) {
                    Resources res = this.mService.mContext.getResources();
                    Configuration serviceConfig = getParent().getConfiguration();
                    if (res.getConfiguration().orientation == 1) {
                        config.orientation = 1;
                    } else {
                        if (config.screenWidthDp > Math.min(serviceConfig.screenWidthDp, serviceConfig.screenHeightDp)) {
                            i = 2;
                        }
                        config.orientation = i;
                    }
                }
            } else if (!this.mFullscreen && ((getStack().mStackId == 1 || getStack().mStackId == 3) && this.mService.getPackageManagerInternalLocked().isInMWPortraitWhiteList(topActivity.packageName))) {
                config.orientation = 1;
            }
        }
    }

    public void setWindowState(int state) {
        if (this.mWindowState != state) {
            HwPCUtils.log("HwPCMultiWindowManager", "setWindowState: " + Integer.toHexString(this.mWindowState) + " to " + Integer.toHexString(state));
            this.mWindowState = state;
            if (this.mService.mStackSupervisor instanceof HwActivityStackSupervisor) {
                ((HwActivityStackSupervisor) this.mService.mStackSupervisor).scheduleReportPCWindowStateChangedLocked(this);
            }
            this.mService.getHwTaskChangeController().notifyTaskProfileLocked(this.taskId, this.mWindowState);
        }
    }

    protected void updateHwOverrideConfiguration() {
        HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.mService);
        if (this.mStack != null && HwPCUtils.isPcDynamicStack(this.mStack.getStackId()) && multiWindowMgr != null) {
            int windowStateLayout;
            if (this.mFullscreen) {
                windowStateLayout = 4;
            } else if (multiWindowMgr.getMaximizedBounds().equals(this.mBounds)) {
                windowStateLayout = 3;
            } else if (this.mBounds.width() > this.mBounds.height()) {
                windowStateLayout = 2;
            } else {
                windowStateLayout = 1;
            }
            int finalState = (this.mNextWindowState & 65280) | windowStateLayout;
            ActivityRecord topActivity = getTopActivity();
            if (topActivity != null && multiWindowMgr.isSpecialVideo(topActivity.packageName)) {
                finalState |= 65536;
            }
            if (!(this.mWindowState == finalState || (HwPCUtils.enabledInPad() ^ 1) == 0)) {
                HwPCUtils.log("HwPCMultiWindowManager", "force to update task:" + toString());
                multiWindowMgr.setForceUpdateTask(this.taskId);
            }
            setWindowState(finalState);
            multiWindowMgr.storeTaskSettings(this);
        }
    }

    protected boolean isMaximizedPortraitAppOnPCMode(String packageName) {
        if (HwPCUtils.isPcCastModeInServer() && getStack() != null && HwPCUtils.isValidExtDisplayId(getStack().mDisplayId) && HwPCMultiWindowManager.getInstance(this.mService).mPortraitMaximizedPkgList.contains(packageName)) {
            return true;
        }
        return false;
    }

    void setStack(ActivityStack stack) {
        if (stack != null && HwPCUtils.isPcDynamicStack(stack.getStackId())) {
            int density = this.mService.mWindowManager.getBaseDisplayDensity(0);
            int extDensity = this.mService.mWindowManager.getBaseDisplayDensity(stack.mDisplayId);
            Point size = new Point();
            this.mService.mWindowManager.getBaseDisplaySize(stack.mDisplayId, size);
            if (density > 0 && extDensity > 0) {
                float ratio = (((float) extDensity) * 1.0f) / ((float) density);
                if (this.mMinWidth == -1) {
                    this.mMinWidth = this.mService.mStackSupervisor.mDefaultMinSizeOfResizeableTask;
                }
                if (this.mMinHeight == -1) {
                    this.mMinHeight = this.mService.mStackSupervisor.mDefaultMinSizeOfResizeableTask;
                }
                int halfWidth = size.x / 2;
                int halfHeight = size.y / 2;
                this.mMinWidth = (int) (((float) this.mMinWidth) * ratio);
                if (this.mMinWidth <= halfWidth) {
                    halfWidth = this.mMinWidth;
                }
                this.mMinWidth = halfWidth;
                this.mMinHeight = (int) (((float) this.mMinHeight) * ratio);
                if (this.mMinHeight <= halfHeight) {
                    halfHeight = this.mMinHeight;
                }
                this.mMinHeight = halfHeight;
            }
        }
        super.setStack(stack);
    }

    protected Rect getLaunchBounds() {
        if (this.mStack != null && HwPCUtils.isPcDynamicStack(this.mStack.getStackId()) && this.mGetLaunchBoundsFirst && this.mRootActivityInfo != null) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.mService);
            if (multiWindowMgr != null) {
                this.mGetLaunchBoundsFirst = false;
                multiWindowMgr.restoreTaskWindowState(this);
                this.mLastNonFullscreenBounds = multiWindowMgr.getLaunchBounds(this);
                return this.mLastNonFullscreenBounds;
            }
        }
        return super.getLaunchBounds();
    }

    boolean removeActivity(ActivityRecord r, boolean reparenting) {
        if (r == null) {
            HwPCUtils.log("HwPCMultiWindowManager", "input parameter ActivityRecord is null");
            return false;
        }
        boolean ret = super.removeActivity(r, reparenting);
        boolean changed = false;
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            changed = checkInKeyGuard();
            if (!(changed || !this.mIsInLockScreen || getTopActivity() == null)) {
                changed = r.hasShowWhenLockedWindows() == getTopActivity().hasShowWhenLockedWindows();
            }
        }
        if (this.mStack != null && HwPCUtils.isPcDynamicStack(this.mStack.getStackId()) && getTopActivity() != null && (getTopActivity() instanceof HwActivityRecord) && (r instanceof HwActivityRecord) && ((((HwActivityRecord) getTopActivity()).mCustomRequestedOrientation != ((HwActivityRecord) r).mCustomRequestedOrientation || changed) && (this.mService instanceof HwActivityManagerService))) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.mService);
            if (multiWindowMgr != null) {
                int customRequestedOrientation = ((HwActivityRecord) getTopActivity()).mCustomRequestedOrientation;
                HwPCUtils.log("HwPCMultiWindowManager", "removeActivity: (" + r.toString() + ")");
                HwPCUtils.log("HwPCMultiWindowManager", "newTopActivity: (" + getTopActivity().toString() + ")" + "[" + customRequestedOrientation + "]");
                if (customRequestedOrientation == 0) {
                    this.mSaveBounds = true;
                    multiWindowMgr.restoreTaskWindowState(this);
                    multiWindowMgr.resizeTaskFromPC(this, multiWindowMgr.getLaunchBounds(this));
                } else {
                    multiWindowMgr.updateTaskByRequestedOrientation(this, customRequestedOrientation);
                }
            }
        }
        return ret;
    }

    void createWindowContainer(boolean onTop, boolean showForAllUsers) {
        super.createWindowContainer(onTop, showForAllUsers);
        if (this.mRootActivityInfo != null && this.mStack != null && HwPCUtils.isPcDynamicStack(this.mStack.mStackId)) {
            this.mService.getHwTaskChangeController().notifyTaskCreated(this.taskId, this.mRootActivityInfo.getComponentName());
        }
    }

    void removeWindowContainer() {
        super.removeWindowContainer();
        if (this.mStack != null && HwPCUtils.isPcDynamicStack(this.mStack.mStackId)) {
            this.mService.getHwTaskChangeController().notifyTaskRemoved(this.taskId);
        }
    }

    protected boolean isResizeable(boolean checkSupportsPip) {
        if (this.mStack == null || !HwPCUtils.isPcDynamicStack(this.mStack.getStackId())) {
            return super.isResizeable(checkSupportsPip);
        }
        return true;
    }

    protected void adjustForMinimalTaskDimensions(Rect bounds) {
        if (SystemProperties.getInt("persist.sys.rog.configmode", 0) == 1) {
            this.mService.mStackSupervisor.reCalculateDefaultMinimalSizeOfResizeableTasks();
        }
        this.mDefaultMinSize = this.mService.mStackSupervisor.mDefaultMinSizeOfResizeableTask;
        Rect originBounds = new Rect();
        Point size = new Point();
        if (HwPCUtils.isPcDynamicStack(getStackId())) {
            originBounds.set(bounds);
            int densityDpi = getConfiguration().densityDpi;
            int serviceDpi = this.mService.getGlobalConfiguration().densityDpi;
            if (densityDpi > 0 && serviceDpi > 0) {
                this.mDefaultMinSize = (this.mDefaultMinSize * densityDpi) / serviceDpi;
                if (!(this.mService.mWindowManager == null || getStack() == null)) {
                    this.mService.mWindowManager.getBaseDisplaySize(getStack().mDisplayId, size);
                    int minSizePx = (int) (((float) (size.x < size.y ? size.x : size.y)) * 0.2f);
                    if (this.mDefaultMinSize <= minSizePx || minSizePx == 0) {
                        minSizePx = this.mDefaultMinSize;
                    }
                    this.mDefaultMinSize = minSizePx;
                }
            }
        }
        super.adjustForMinimalTaskDimensions(bounds);
        if (HwPCUtils.isPcDynamicStack(getStackId())) {
            updateBoundsByRatio(originBounds, bounds, size);
        }
    }

    private void updateBoundsByRatio(Rect oldBound, Rect newBound, Point displaySize) {
        if (oldBound != null && !oldBound.isEmpty() && newBound != null && !newBound.isEmpty() && displaySize != null && displaySize.x > 0 && displaySize.y > 0) {
            int oldW = oldBound.right - oldBound.left;
            int oldH = oldBound.bottom - oldBound.top;
            if (oldW != 0 && oldH != 0) {
                int newW = newBound.right - newBound.left;
                int newH = newBound.bottom - newBound.top;
                if (newW != 0 && newH != 0) {
                    float ratio = ((float) oldW) / ((float) oldH);
                    if (newW != oldW) {
                        int tmpH = (int) (((float) newW) / ratio);
                        if (((float) tmpH) > ((float) displaySize.y) * 0.8f) {
                            newBound.set(oldBound);
                        } else {
                            newBound.bottom = newBound.top + tmpH;
                        }
                    } else if (newH != oldH) {
                        int tmpW = (int) (((float) newH) * ratio);
                        if (((float) tmpW) > ((float) displaySize.x) * 0.8f) {
                            newBound.set(oldBound);
                        } else {
                            newBound.right = newBound.left + tmpW;
                        }
                    }
                }
            }
        }
    }

    private boolean checkInKeyGuard() {
        HwPCUtils.log("HwPCMultiWindowManager", "checkInKeyGuard");
        KeyguardManager keyguardManager = (KeyguardManager) this.mService.mContext.getSystemService("keyguard");
        boolean tempIsInLockScreen = this.mIsInLockScreen;
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            HwPCUtils.log("HwPCMultiWindowManager", "checkInKeyGuard mIsInLockScreen true");
            this.mIsInLockScreen = true;
        } else {
            this.mIsInLockScreen = false;
            HwPCUtils.log("HwPCMultiWindowManager", "checkInKeyGuard mIsInLockScreen false");
        }
        if (tempIsInLockScreen != this.mIsInLockScreen) {
            return true;
        }
        return false;
    }

    void activityResumedInTop() {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            checkInKeyGuard();
            if (this.mWindowState != 4 && this.mIsInLockScreen) {
                HwPCUtils.log("HwPCMultiWindowManager", "activityResumedInTop WINDOW_FULLSCREEN");
                this.mWindowStateBeforeLockScreen = this.mWindowState;
                this.mWindowStateBeforeLockScreenValid = true;
                setWindowState(4);
            } else if (!this.mIsInLockScreen && this.mWindowStateBeforeLockScreenValid && this.mWindowState != this.mWindowStateBeforeLockScreen) {
                setWindowState(this.mWindowStateBeforeLockScreen);
            }
        }
    }

    void addActivityToTop(ActivityRecord r) {
        super.addActivityToTop(r);
        if (HwPCUtils.isPcDynamicStack(getStackId()) && (this.mService instanceof HwActivityManagerService) && r.info != null && (getTopActivity() instanceof HwActivityRecord)) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.mService);
            int requestedOrientation = r.info.screenOrientation;
            int newCustomRequestOrientation = 0;
            if (multiWindowMgr != null) {
                if (HwPCMultiWindowManager.isFixedOrientationPortrait(requestedOrientation)) {
                    newCustomRequestOrientation = 1;
                } else if (HwPCMultiWindowManager.isFixedOrientationLandscape(requestedOrientation)) {
                    newCustomRequestOrientation = 2;
                }
            }
            if (newCustomRequestOrientation != 0) {
                int customRequestedOrientation = HwPCMultiWindowCompatibility.getWindowStateLayout(this.mWindowState) == 1 ? 1 : 2;
                HwPCUtils.log("HwPCMultiWindowManager", "requestedOrientation:" + newCustomRequestOrientation + " oldRequestedOrientation:" + customRequestedOrientation);
                if (newCustomRequestOrientation != customRequestedOrientation) {
                    r.setRequestedOrientation(newCustomRequestOrientation);
                }
            }
            activityResumedInTop();
        }
    }

    public ArrayList<ActivityRecord> getActivities() {
        return this.mActivities;
    }
}
