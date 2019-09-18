package com.android.server.am;

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

    public HwTaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, IVoiceInteractionSession _voiceSession, IVoiceInteractor _voiceInteractor) {
        super(service, _taskId, info, _intent, _voiceSession, _voiceInteractor);
        if (HwPCUtils.isPcCastModeInServer()) {
            this.mRootActivityInfo = info;
        }
    }

    public HwTaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, ActivityManager.TaskDescription _taskDescription) {
        super(service, _taskId, info, _intent, _taskDescription);
        if (HwPCUtils.isPcCastModeInServer()) {
            this.mRootActivityInfo = info;
        }
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public HwTaskRecord(ActivityManagerService service, int _taskId, Intent _intent, Intent _affinityIntent, String _affinity, String _rootAffinity, ComponentName _realActivity, ComponentName _origActivity, boolean _rootWasReset, boolean _autoRemoveRecents, boolean _askedCompatMode, int _userId, int _effectiveUid, String _lastDescription, ArrayList<ActivityRecord> activities, long lastTimeMoved, boolean neverRelinquishIdentity, ActivityManager.TaskDescription _lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean _realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        super(service, _taskId, _intent, _affinityIntent, _affinity, _rootAffinity, _realActivity, _origActivity, _rootWasReset, _autoRemoveRecents, _askedCompatMode, _userId, _effectiveUid, _lastDescription, activities, lastTimeMoved, neverRelinquishIdentity, _lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, _realActivitySuspended, userSetupComplete, minWidth, minHeight);
        if (HwPCUtils.isPcCastModeInServer() && this.mActivities.size() > 0) {
            this.mRootActivityInfo = ((ActivityRecord) this.mActivities.get(0)).info;
        }
    }

    @SuppressLint({"AvoidMax/Min"})
    public void overrideConfigOrienForFreeForm(Configuration config) {
        ActivityRecord topActivity = getTopActivity();
        if (topActivity != null) {
            int i = 1;
            if (inFreeformWindowingMode()) {
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
            } else if (inSplitScreenWindowingMode() && this.mService.getPackageManagerInternalLocked().isInMWPortraitWhiteList(topActivity.packageName)) {
                config.orientation = 1;
            }
        }
    }

    public void setWindowState(int state) {
        if (this.mWindowState != state) {
            HwPCUtils.log("HwPCMultiWindowManager", "setWindowState: " + Integer.toHexString(this.mWindowState) + " to " + Integer.toHexString(state));
            this.mWindowState = state;
            if (this.mService.mStackSupervisor instanceof HwActivityStackSupervisor) {
                this.mService.mStackSupervisor.scheduleReportPCWindowStateChangedLocked(this);
            }
            this.mService.getHwTaskChangeController().notifyTaskProfileLocked(this.taskId, this.mWindowState);
        }
    }

    /* access modifiers changed from: protected */
    public void updateHwOverrideConfiguration(Rect bounds) {
        int windowStateLayout;
        HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.mService);
        if (this.mStack != null && HwPCUtils.isExtDynamicStack(this.mStack.getStackId()) && multiWindowMgr != null) {
            if (bounds == null || bounds.isEmpty()) {
                windowStateLayout = 4;
            } else if (multiWindowMgr.getMaximizedBounds().equals(getOverrideBounds())) {
                windowStateLayout = 3;
            } else if (getOverrideBounds().width() > getOverrideBounds().height()) {
                windowStateLayout = 2;
            } else {
                windowStateLayout = 1;
            }
            int finalState = (this.mNextWindowState & 65280) | windowStateLayout;
            ActivityRecord topActivity = getTopActivity();
            if (topActivity != null && multiWindowMgr.isSpecialVideo(topActivity.packageName)) {
                finalState |= 65536;
            }
            if (this.mWindowState != finalState && !HwPCUtils.enabledInPad()) {
                HwPCUtils.log("HwPCMultiWindowManager", "force to update task:" + toString());
                multiWindowMgr.setForceUpdateTask(this.taskId);
            }
            setWindowState(finalState);
            multiWindowMgr.storeTaskSettings(this);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isMaximizedPortraitAppOnPCMode(String packageName) {
        if (!HwPCUtils.isPcCastModeInServer() || getStack() == null || !HwPCUtils.isValidExtDisplayId(getStack().mDisplayId) || !HwPCMultiWindowManager.getInstance(this.mService).mPortraitMaximizedPkgList.contains(packageName)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setStack(ActivityStack stack) {
        if (stack != null && HwPCUtils.isExtDynamicStack(stack.getStackId())) {
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
                this.mMinWidth = this.mMinWidth > halfWidth ? halfWidth : this.mMinWidth;
                this.mMinHeight = (int) (((float) this.mMinHeight) * ratio);
                this.mMinHeight = this.mMinHeight > halfHeight ? halfHeight : this.mMinHeight;
            }
        }
        HwTaskRecord.super.setStack(stack);
    }

    /* access modifiers changed from: protected */
    public Rect getLaunchBounds() {
        if (this.mStack != null && HwPCUtils.isExtDynamicStack(this.mStack.getStackId()) && this.mGetLaunchBoundsFirst && this.mRootActivityInfo != null) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.mService);
            if (multiWindowMgr != null) {
                this.mGetLaunchBoundsFirst = false;
                multiWindowMgr.restoreTaskWindowState(this);
                this.mLastNonFullscreenBounds = multiWindowMgr.getLaunchBounds(this);
                return this.mLastNonFullscreenBounds;
            }
        }
        return HwTaskRecord.super.getLaunchBounds();
    }

    /* access modifiers changed from: package-private */
    public boolean removeActivity(ActivityRecord r, boolean reparenting) {
        boolean z = false;
        if (r == null) {
            HwPCUtils.log("HwPCMultiWindowManager", "input parameter ActivityRecord is null");
            return false;
        }
        boolean ret = HwTaskRecord.super.removeActivity(r, reparenting);
        boolean changed = false;
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            changed = checkInKeyGuard();
            if (!changed && this.mIsInLockScreen && getTopActivity() != null) {
                if (r.canShowWhenLocked() == getTopActivity().canShowWhenLocked()) {
                    z = true;
                }
                changed = z;
            }
        }
        if (this.mStack != null && HwPCUtils.isExtDynamicStack(this.mStack.getStackId()) && getTopActivity() != null && (getTopActivity() instanceof HwActivityRecord) && (r instanceof HwActivityRecord) && ((getTopActivity().mCustomRequestedOrientation != ((HwActivityRecord) r).mCustomRequestedOrientation || changed) && (this.mService instanceof HwActivityManagerService))) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.mService);
            if (multiWindowMgr != null) {
                int customRequestedOrientation = getTopActivity().mCustomRequestedOrientation;
                HwPCUtils.log("HwPCMultiWindowManager", "removeActivity: (" + r.toString() + ")");
                HwPCUtils.log("HwPCMultiWindowManager", "newTopActivity: (" + getTopActivity().toString() + ")[" + customRequestedOrientation + "]");
                if (customRequestedOrientation == 0) {
                    this.mSaveBounds = true;
                    multiWindowMgr.restoreTaskWindowState(this);
                    multiWindowMgr.resizeTaskFromPC(this, multiWindowMgr.getLaunchBounds(this));
                } else if (HwPCUtils.enabledInPad()) {
                    multiWindowMgr.updateTaskByRequestedOrientation(this, customRequestedOrientation);
                }
            }
        }
        return ret;
    }

    /* access modifiers changed from: package-private */
    public void createWindowContainer(boolean onTop, boolean showForAllUsers) {
        HwTaskRecord.super.createWindowContainer(onTop, showForAllUsers);
        if (this.mRootActivityInfo != null && this.mStack != null && HwPCUtils.isExtDynamicStack(this.mStack.mStackId)) {
            this.mService.getHwTaskChangeController().notifyTaskCreated(this.taskId, this.mRootActivityInfo.getComponentName());
        }
    }

    /* access modifiers changed from: package-private */
    public void removeWindowContainer() {
        HwTaskRecord.super.removeWindowContainer();
        if (this.mStack != null && HwPCUtils.isExtDynamicStack(this.mStack.mStackId)) {
            this.mService.getHwTaskChangeController().notifyTaskRemoved(this.taskId);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isResizeable(boolean checkSupportsPip) {
        if (this.mStack == null || !HwPCUtils.isExtDynamicStack(this.mStack.getStackId())) {
            return HwTaskRecord.super.isResizeable(checkSupportsPip);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void adjustForMinimalTaskDimensions(Rect bounds) {
        if (SystemProperties.getInt("persist.sys.rog.configmode", 0) == 1) {
            this.mService.mStackSupervisor.reCalculateDefaultMinimalSizeOfResizeableTasks();
        }
        this.mDefaultMinSize = this.mService.mStackSupervisor.mDefaultMinSizeOfResizeableTask;
        Rect originBounds = new Rect();
        Point size = new Point();
        if (HwPCUtils.isExtDynamicStack(getStackId())) {
            originBounds.set(bounds);
            int densityDpi = getConfiguration().densityDpi;
            int serviceDpi = this.mService.getGlobalConfiguration().densityDpi;
            if (densityDpi > 0 && serviceDpi > 0) {
                this.mDefaultMinSize = (this.mDefaultMinSize * densityDpi) / serviceDpi;
                if (!(this.mService.mWindowManager == null || getStack() == null)) {
                    this.mService.mWindowManager.getBaseDisplaySize(getStack().mDisplayId, size);
                    int minSizePx = (int) (((float) (size.x < size.y ? size.x : size.y)) * 0.2f);
                    this.mDefaultMinSize = (this.mDefaultMinSize <= minSizePx || minSizePx == 0) ? this.mDefaultMinSize : minSizePx;
                }
            }
        }
        HwTaskRecord.super.adjustForMinimalTaskDimensions(bounds);
        if (HwPCUtils.isExtDynamicStack(getStackId())) {
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
        boolean tempIsInLockScreen = this.mIsInLockScreen;
        if (((KeyguardManager) this.mService.mContext.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
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

    /* access modifiers changed from: package-private */
    public void activityResumedInTop() {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            checkInKeyGuard();
            if (this.mWindowState != 4 && this.mIsInLockScreen) {
                HwPCUtils.log("HwPCMultiWindowManager", "activityResumedInTop WINDOW_FULLSCREEN");
                this.mWindowStateBeforeLockScreen = this.mWindowState;
                this.mWindowStateBeforeLockScreenValid = true;
                setWindowState(4);
                setBounds(null);
            } else if (!this.mIsInLockScreen && this.mWindowStateBeforeLockScreenValid && this.mWindowState != this.mWindowStateBeforeLockScreen) {
                setWindowState(this.mWindowStateBeforeLockScreen);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addActivityToTop(ActivityRecord r) {
        HwTaskRecord.super.addActivityToTop(r);
        if (HwPCUtils.isExtDynamicStack(getStackId()) && (this.mService instanceof HwActivityManagerService) && r.info != null && (getTopActivity() instanceof HwActivityRecord)) {
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
                int i = 1;
                if (HwPCMultiWindowCompatibility.getWindowStateLayout(this.mWindowState) != 1) {
                    i = 2;
                }
                int customRequestedOrientation = i;
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
