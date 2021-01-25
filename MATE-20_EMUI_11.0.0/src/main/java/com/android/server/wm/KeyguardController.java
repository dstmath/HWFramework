package com.android.server.wm;

import android.app.WindowConfiguration;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Trace;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.WindowManagerService;
import java.io.PrintWriter;
import java.util.HashMap;

/* access modifiers changed from: package-private */
public class KeyguardController {
    private static final String TAG = "ActivityTaskManager";
    private boolean mAodShowing;
    private int mBeforeUnoccludeTransit;
    private boolean mDismissalRequested;
    private final SparseArray<KeyguardDisplayState> mDisplayStates = new SparseArray<>();
    private boolean mIsExpandScreenTurningOn;
    private boolean mKeyguardGoingAway;
    private boolean mKeyguardShowing;
    private RootActivityContainer mRootActivityContainer;
    private int[] mSecondaryDisplayIdsShowing;
    private final ActivityTaskManagerService mService;
    private HashMap<ActivityRecord, Boolean> mShowWhileKeyguardShowingRecordMap = new HashMap<>();
    private final ActivityStackSupervisor mStackSupervisor;
    private int mVisibilityTransactionDepth;
    private WindowManagerService mWindowManager;

    KeyguardController(ActivityTaskManagerService service, ActivityStackSupervisor stackSupervisor) {
        this.mService = service;
        this.mStackSupervisor = stackSupervisor;
    }

    /* access modifiers changed from: package-private */
    public void setWindowManager(WindowManagerService windowManager) {
        this.mWindowManager = windowManager;
        this.mRootActivityContainer = this.mService.mRootActivityContainer;
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardOrAodShowing(int displayId) {
        return (this.mKeyguardShowing || this.mAodShowing) && !this.mKeyguardGoingAway && !isDisplayOccluded(displayId);
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardUnoccludedOrAodShowing(int displayId) {
        if (displayId != 0 || !this.mAodShowing) {
            return isKeyguardOrAodShowing(displayId);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardShowing(int displayId) {
        return this.mKeyguardShowing && !this.mKeyguardGoingAway && !isDisplayOccluded(displayId);
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardLocked() {
        return this.mKeyguardShowing && !this.mKeyguardGoingAway;
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardGoingAway() {
        return this.mKeyguardGoingAway && this.mKeyguardShowing;
    }

    /* access modifiers changed from: package-private */
    public void setKeyguardShown(boolean keyguardShowing, boolean aodShowing) {
        boolean aodChanged = true;
        boolean keyguardChanged = keyguardShowing != this.mKeyguardShowing || (this.mKeyguardGoingAway && keyguardShowing);
        if (aodShowing == this.mAodShowing) {
            aodChanged = false;
        }
        if (keyguardChanged || aodChanged) {
            if (ActivityTaskManagerDebugConfig.DEBUG_KEYGUARD) {
                Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "last mKeyguardShowing:" + this.mKeyguardShowing + " mAodShowing:" + this.mAodShowing + " mSecondaryDisplayShowing:" + this.mSecondaryDisplayIdsShowing);
            }
            this.mKeyguardShowing = keyguardShowing;
            this.mAodShowing = aodShowing;
            this.mWindowManager.setAodShowing(aodShowing);
            if (keyguardChanged) {
                dismissDockedStackIfNeeded();
                setKeyguardGoingAway(false);
                if (keyguardShowing) {
                    this.mDismissalRequested = false;
                }
            }
            this.mWindowManager.setKeyguardOrAodShowingOnDefaultDisplay(isKeyguardOrAodShowing(0));
            updateKeyguardSleepToken();
            this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void keyguardGoingAway(int flags) {
        if (this.mKeyguardShowing) {
            Trace.traceBegin(64, "keyguardGoingAway");
            this.mWindowManager.deferSurfaceLayout();
            try {
                setKeyguardGoingAway(true);
                this.mRootActivityContainer.getDefaultDisplay().mDisplayContent.prepareAppTransition(20, false, convertTransitFlags(flags), false);
                updateKeyguardSleepToken();
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                if (!this.mService.mHwATMSEx.isActivityVisiableInFingerBoost(this.mService.mLastResumedActivity)) {
                    this.mRootActivityContainer.addStartingWindowsForVisibleActivities(true);
                }
                this.mWindowManager.executeAppTransition();
            } finally {
                Trace.traceBegin(64, "keyguardGoingAway: surfaceLayout");
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
                Trace.traceEnd(64);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dismissKeyguard(IBinder token, IKeyguardDismissCallback callback, CharSequence message) {
        ActivityRecord activityRecord = ActivityRecord.forTokenLocked(token);
        if (activityRecord == null || !activityRecord.visibleIgnoringKeyguard) {
            failCallback(callback);
            return;
        }
        Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "Activity requesting to dismiss Keyguard: " + activityRecord);
        if (activityRecord.getTurnScreenOnFlag() && activityRecord.isTopRunningActivity()) {
            this.mStackSupervisor.wakeUp("dismissKeyguard");
        }
        this.mWindowManager.dismissKeyguard(callback, message);
    }

    private void setKeyguardGoingAway(boolean keyguardGoingAway) {
        if (ActivityTaskManagerDebugConfig.DEBUG_KEYGUARD) {
            Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "change mKeyguardGoingAway from:" + this.mKeyguardGoingAway + " to:" + keyguardGoingAway);
        }
        this.mKeyguardGoingAway = keyguardGoingAway;
        this.mWindowManager.setKeyguardGoingAway(keyguardGoingAway);
    }

    private void failCallback(IKeyguardDismissCallback callback) {
        try {
            callback.onDismissError();
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to call callback", e);
        }
    }

    private int convertTransitFlags(int keyguardGoingAwayFlags) {
        int result = 0;
        if ((keyguardGoingAwayFlags & 1) != 0) {
            result = 0 | 1;
        }
        if ((keyguardGoingAwayFlags & 2) != 0) {
            result |= 2;
        }
        if ((keyguardGoingAwayFlags & 4) != 0) {
            return result | 4;
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public void beginActivityVisibilityUpdate() {
        this.mVisibilityTransactionDepth++;
    }

    /* access modifiers changed from: package-private */
    public void endActivityVisibilityUpdate() {
        this.mVisibilityTransactionDepth--;
        if (this.mVisibilityTransactionDepth == 0) {
            visibilitiesUpdated();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canShowActivityWhileKeyguardShowing(ActivityRecord r, boolean dismissKeyguard) {
        boolean isLauncherRecord = r.packageName != null && r.packageName.contains(DisplayPolicy.LAUNCHER_PACKAGE_NAME);
        boolean isExpandScreenTurningOn = isExpandScreenTurningOn();
        if (isDisplayOccluded(r.getDisplayId()) || !this.mService.mHwATMSEx.isActivityVisiableInFingerBoost(r) || isExpandScreenTurningOn) {
            updateKeyguardShowingRecords(r, isLauncherRecord, false);
            Slog.d(TAG, "canShowActivityWhileKeyguardShowing false r=" + r);
            if (this.mService.mVrMananger != null && this.mService.mVrMananger.isVRDeviceConnected() && this.mService.mVrMananger.isValidVRDisplayId(r.getDisplayId())) {
                return true;
            }
            if (!dismissKeyguard || !canDismissKeyguard() || this.mAodShowing) {
                return false;
            }
            return this.mDismissalRequested || (r.canShowWhenLocked() && getDisplay(r.getDisplayId()).mDismissingKeyguardActivity != r);
        }
        updateKeyguardShowingRecords(r, isLauncherRecord, true);
        Slog.d(TAG, "canShowActivityWhileKeyguardShowing true r=" + r);
        return true;
    }

    private void updateKeyguardShowingRecords(ActivityRecord r, boolean isLauncherRecord, boolean isBoost) {
        if (isLauncherRecord) {
            this.mShowWhileKeyguardShowingRecordMap.put(r, Boolean.valueOf(isBoost));
        }
    }

    public HashMap<ActivityRecord, Boolean> getShowWhileKeyguardShowingRecordMap() {
        return this.mShowWhileKeyguardShowingRecordMap;
    }

    public void setExpandScreenTurningOn(boolean isExpandScreenTurningOn) {
        this.mIsExpandScreenTurningOn = isExpandScreenTurningOn;
        if (isExpandScreenTurningOn) {
            Slog.i(TAG, "setExpandScreenTurningOn ensureActivitiesVisible begin");
            this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
            Slog.i(TAG, "setExpandScreenTurningOn ensureActivitiesVisible end");
        }
    }

    private boolean isExpandScreenTurningOn() {
        return this.mIsExpandScreenTurningOn;
    }

    /* access modifiers changed from: package-private */
    public boolean canShowWhileOccluded(boolean dismissKeyguard, boolean showWhenLocked) {
        return showWhenLocked || (dismissKeyguard && !this.mWindowManager.isKeyguardSecure(this.mService.getCurrentUserId()));
    }

    private void visibilitiesUpdated() {
        boolean requestDismissKeyguard = false;
        for (int displayNdx = this.mRootActivityContainer.getChildCount() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mRootActivityContainer.getChildAt(displayNdx);
            KeyguardDisplayState state = getDisplay(display.mDisplayId);
            state.visibilitiesUpdated(this, display);
            requestDismissKeyguard |= state.mRequestDismissKeyguard;
        }
        if (requestDismissKeyguard) {
            handleDismissKeyguard();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOccludedChanged(int displayId) {
        Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "handleOccludedChanged mOccluded: " + isDisplayOccluded(displayId));
        boolean isPcModeInPad = HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer();
        Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "handleOccludedChanged displayId: " + displayId + ",isPcModeInPad: " + isPcModeInPad);
        if (displayId == 0 || isPcModeInPad) {
            this.mWindowManager.onKeyguardOccludedChanged(isDisplayOccluded(displayId));
            if (isKeyguardLocked()) {
                this.mWindowManager.deferSurfaceLayout();
                try {
                    this.mRootActivityContainer.getDefaultDisplay().mDisplayContent.prepareAppTransition(resolveOccludeTransit(), false, 0, true);
                    updateKeyguardSleepToken(displayId);
                    this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                    this.mWindowManager.executeAppTransition();
                } finally {
                    this.mWindowManager.continueSurfaceLayout();
                }
            }
            dismissDockedStackIfNeeded();
            return;
        }
        updateKeyguardSleepToken(displayId);
    }

    private void handleDismissKeyguard() {
        if (this.mWindowManager.isKeyguardSecure(this.mService.getCurrentUserId())) {
            Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "handleDismissKeyguard");
            this.mWindowManager.dismissKeyguard(null, null);
            this.mDismissalRequested = true;
            DisplayContent dc = this.mRootActivityContainer.getDefaultDisplay().mDisplayContent;
            if (this.mKeyguardShowing && canDismissKeyguard() && dc.mAppTransition.getAppTransition() == 23) {
                dc.prepareAppTransition(this.mBeforeUnoccludeTransit, false, 0, true);
                this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                this.mWindowManager.executeAppTransition();
            }
        }
    }

    private boolean isDisplayOccluded(int displayId) {
        return getDisplay(displayId).mOccluded;
    }

    /* access modifiers changed from: package-private */
    public boolean isOccludedShowWhenKeyguard(int displayId) {
        return HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && getDisplay(displayId).mOccluded;
    }

    /* access modifiers changed from: package-private */
    public boolean canDismissKeyguard() {
        return this.mWindowManager.isKeyguardTrusted() || !this.mWindowManager.isKeyguardSecure(this.mService.getCurrentUserId());
    }

    private int resolveOccludeTransit() {
        DisplayContent dc = this.mService.mRootActivityContainer.getDefaultDisplay().mDisplayContent;
        if (this.mBeforeUnoccludeTransit != -1 && dc.mAppTransition.getAppTransition() == 23 && isDisplayOccluded(0)) {
            return this.mBeforeUnoccludeTransit;
        }
        if (isDisplayOccluded(0)) {
            return 22;
        }
        this.mBeforeUnoccludeTransit = dc.mAppTransition.getAppTransition();
        return 23;
    }

    private void dismissDockedStackIfNeeded() {
        ActivityStack stack;
        if (this.mKeyguardShowing && isDisplayOccluded(0) && (stack = this.mRootActivityContainer.getDefaultDisplay().getSplitScreenPrimaryStack()) != null) {
            this.mStackSupervisor.moveTasksToFullscreenStackLocked(stack, stack.isFocusedStackOnDisplay());
        }
    }

    private void updateKeyguardSleepToken() {
        for (int displayNdx = this.mRootActivityContainer.getChildCount() - 1; displayNdx >= 0; displayNdx--) {
            updateKeyguardSleepToken(this.mRootActivityContainer.getChildAt(displayNdx).mDisplayId);
        }
    }

    private void updateKeyguardSleepToken(int displayId) {
        if (!HwPCUtils.isHiCarCastMode() || !HwPCUtils.isValidExtDisplayId(displayId)) {
            KeyguardDisplayState state = getDisplay(displayId);
            if (isKeyguardUnoccludedOrAodShowing(displayId) && state.mSleepToken == null) {
                if (ActivityTaskManagerDebugConfig.DEBUG_KEYGUARD) {
                    Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "acquireSleepToken");
                }
                state.acquiredSleepToken();
            } else if (!isKeyguardUnoccludedOrAodShowing(displayId) && state.mSleepToken != null) {
                if (ActivityTaskManagerDebugConfig.DEBUG_KEYGUARD) {
                    Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "releaseSleepToken");
                }
                state.releaseSleepToken();
            }
        }
    }

    private KeyguardDisplayState getDisplay(int displayId) {
        KeyguardDisplayState state = this.mDisplayStates.get(displayId);
        if (state != null) {
            return state;
        }
        KeyguardDisplayState state2 = new KeyguardDisplayState(this.mService, displayId);
        this.mDisplayStates.append(displayId, state2);
        return state2;
    }

    /* access modifiers changed from: package-private */
    public void onDisplayRemoved(int displayId) {
        KeyguardDisplayState state = this.mDisplayStates.get(displayId);
        if (state != null) {
            state.onRemoved();
            this.mDisplayStates.remove(displayId);
        }
    }

    /* access modifiers changed from: private */
    public static class KeyguardDisplayState {
        private ActivityRecord mDismissingKeyguardActivity;
        private final int mDisplayId;
        private boolean mOccluded;
        private boolean mRequestDismissKeyguard;
        private final ActivityTaskManagerService mService;
        private ActivityTaskManagerInternal.SleepToken mSleepToken;

        KeyguardDisplayState(ActivityTaskManagerService service, int displayId) {
            this.mService = service;
            this.mDisplayId = displayId;
        }

        /* access modifiers changed from: package-private */
        public void onRemoved() {
            this.mDismissingKeyguardActivity = null;
            releaseSleepToken();
        }

        /* access modifiers changed from: package-private */
        public void acquiredSleepToken() {
            if (this.mSleepToken == null) {
                this.mSleepToken = this.mService.acquireSleepToken("keyguard", this.mDisplayId);
            }
        }

        /* access modifiers changed from: package-private */
        public void releaseSleepToken() {
            ActivityTaskManagerInternal.SleepToken sleepToken = this.mSleepToken;
            if (sleepToken != null) {
                sleepToken.release();
                this.mSleepToken = null;
            }
        }

        /* access modifiers changed from: package-private */
        public void visibilitiesUpdated(KeyguardController controller, ActivityDisplay display) {
            boolean lastOccluded = this.mOccluded;
            ActivityRecord lastDismissActivity = this.mDismissingKeyguardActivity;
            this.mRequestDismissKeyguard = false;
            this.mOccluded = false;
            this.mDismissingKeyguardActivity = null;
            ActivityStack stack = getStackForControllingOccluding(display);
            if (stack != null) {
                ActivityRecord topDismissing = stack.getTopDismissingKeyguardActivity();
                this.mOccluded = stack.topActivityOccludesKeyguard() || (topDismissing != null && !WindowConfiguration.isHwPCFreeFormWindowingMode(topDismissing.getWindowingMode()) && stack.topRunningActivityLocked() == topDismissing && controller.canShowWhileOccluded(true, false));
                if (stack.getTopDismissingKeyguardActivity() != null) {
                    this.mDismissingKeyguardActivity = stack.getTopDismissingKeyguardActivity();
                }
                if (this.mDisplayId != 0) {
                    this.mOccluded |= stack.canShowWithInsecureKeyguard() && controller.canDismissKeyguard();
                    if (this.mService.mVrMananger != null && this.mService.mVrMananger.isVRDeviceConnected()) {
                        this.mOccluded = false;
                    }
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_KEYGUARD) {
                    Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "topDismissing:" + topDismissing + " mOccluded:" + this.mOccluded);
                }
            }
            if (this.mDisplayId == 0) {
                this.mOccluded |= controller.mWindowManager.isShowingDream();
            }
            if (lastOccluded != this.mOccluded) {
                controller.handleOccludedChanged(this.mDisplayId);
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_KEYGUARD && this.mDismissingKeyguardActivity != null) {
                Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "dismissingKeyguardActivity:" + this.mDismissingKeyguardActivity);
            }
            ActivityRecord activityRecord = this.mDismissingKeyguardActivity;
            if (lastDismissActivity != activityRecord && !this.mOccluded && activityRecord != null && controller.mWindowManager.isKeyguardSecure(controller.mService.getCurrentUserId())) {
                this.mRequestDismissKeyguard = true;
            }
        }

        private ActivityStack getStackForControllingOccluding(ActivityDisplay display) {
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                if (!(stack == null || !stack.isFocusableAndVisible() || stack.inPinnedWindowingMode() || stack.inHwPCMultiStackWindowingMode())) {
                    return stack;
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public void dumpStatus(PrintWriter pw, String prefix) {
            pw.println(prefix + "  Occluded=" + this.mOccluded + " DismissingKeyguardActivity=" + this.mDismissingKeyguardActivity + " at display=" + this.mDisplayId);
        }

        /* access modifiers changed from: package-private */
        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1120986464257L, this.mDisplayId);
            proto.write(1133871366146L, this.mOccluded);
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "KeyguardController:");
        pw.println(prefix + "  mKeyguardShowing=" + this.mKeyguardShowing);
        pw.println(prefix + "  mAodShowing=" + this.mAodShowing);
        pw.println(prefix + "  mKeyguardGoingAway=" + this.mKeyguardGoingAway);
        dumpDisplayStates(pw, prefix);
        pw.println(prefix + "  mDismissalRequested=" + this.mDismissalRequested);
        pw.println(prefix + "  mVisibilityTransactionDepth=" + this.mVisibilityTransactionDepth);
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1133871366147L, this.mAodShowing);
        proto.write(1133871366145L, this.mKeyguardShowing);
        writeDisplayStatesToProto(proto, 2246267895810L);
        proto.end(token);
    }

    private void dumpDisplayStates(PrintWriter pw, String prefix) {
        for (int i = 0; i < this.mDisplayStates.size(); i++) {
            this.mDisplayStates.valueAt(i).dumpStatus(pw, prefix);
        }
    }

    private void writeDisplayStatesToProto(ProtoOutputStream proto, long fieldId) {
        for (int i = 0; i < this.mDisplayStates.size(); i++) {
            this.mDisplayStates.valueAt(i).writeToProto(proto, fieldId);
        }
    }
}
