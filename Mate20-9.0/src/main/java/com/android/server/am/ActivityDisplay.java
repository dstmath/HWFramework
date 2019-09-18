package com.android.server.am;

import android.app.ActivityManagerInternal;
import android.app.ActivityOptions;
import android.app.WindowConfiguration;
import android.freeform.HwFreeFormUtils;
import android.graphics.Point;
import android.util.HwPCUtils;
import android.util.IntArray;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.HwServiceFactory;
import com.android.server.wm.ConfigurationContainer;
import com.android.server.wm.DisplayWindowController;
import com.android.server.wm.WindowContainerListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class ActivityDisplay extends ConfigurationContainer<ActivityStack> implements WindowContainerListener {
    static final int POSITION_BOTTOM = Integer.MIN_VALUE;
    static final int POSITION_TOP = Integer.MAX_VALUE;
    private static final String TAG = "ActivityManager";
    private static final String TAG_KEYGUARD = "ActivityManager_keyguard";
    private static final String TAG_STACK = "ActivityManager";
    private static int sNextFreeStackId = 0;
    final ArrayList<ActivityManagerInternal.SleepToken> mAllSleepTokens;
    private ActivityStack mCoordinationPrimaryStack;
    Display mDisplay;
    private IntArray mDisplayAccessUIDs;
    int mDisplayId;
    private ActivityStack mHomeStack;
    ActivityManagerInternal.SleepToken mOffToken;
    private ActivityStack mPinnedStack;
    private ActivityStack mRecentsStack;
    private boolean mSleeping;
    private ActivityStack mSplitScreenPrimaryStack;
    private ArrayList<OnStackOrderChangedListener> mStackOrderChangedCallbacks;
    final ArrayList<ActivityStack> mStacks;
    private ActivityStackSupervisor mSupervisor;
    private Point mTmpDisplaySize;
    private DisplayWindowController mWindowContainerController;

    interface OnStackOrderChangedListener {
        void onStackOrderChanged();
    }

    @VisibleForTesting
    ActivityDisplay(ActivityStackSupervisor supervisor, int displayId) {
        this(supervisor, supervisor.mDisplayManager.getDisplay(displayId));
    }

    ActivityDisplay(ActivityStackSupervisor supervisor, Display display) {
        this.mStacks = new ArrayList<>();
        this.mStackOrderChangedCallbacks = new ArrayList<>();
        this.mDisplayAccessUIDs = new IntArray();
        this.mAllSleepTokens = new ArrayList<>();
        this.mHomeStack = null;
        this.mRecentsStack = null;
        this.mPinnedStack = null;
        this.mSplitScreenPrimaryStack = null;
        this.mCoordinationPrimaryStack = null;
        this.mTmpDisplaySize = new Point();
        this.mSupervisor = supervisor;
        this.mDisplayId = display.getDisplayId();
        this.mDisplay = display;
        this.mWindowContainerController = createWindowContainerController();
        updateBounds();
    }

    /* access modifiers changed from: protected */
    public DisplayWindowController createWindowContainerController() {
        return new DisplayWindowController(this.mDisplay, this);
    }

    /* access modifiers changed from: package-private */
    public void updateBounds() {
        this.mDisplay.getSize(this.mTmpDisplaySize);
        setBounds(0, 0, this.mTmpDisplaySize.x, this.mTmpDisplaySize.y);
    }

    /* access modifiers changed from: package-private */
    public void addChild(ActivityStack stack, int position) {
        if (position == Integer.MIN_VALUE) {
            position = 0;
        } else if (position == Integer.MAX_VALUE) {
            position = this.mStacks.size();
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(ActivityManagerService.TAG, "addChild: attaching " + stack + " to displayId=" + this.mDisplayId + " position=" + position);
        } else {
            Slog.v(ActivityManagerService.TAG, "addChild: attaching stackId=" + stack.mStackId + " to displayId=" + this.mDisplayId + " position=" + position);
        }
        addStackReferenceIfNeeded(stack);
        positionChildAt(stack, position);
        this.mSupervisor.mService.updateSleepIfNeededLocked();
    }

    /* access modifiers changed from: package-private */
    public void removeChild(ActivityStack stack) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(ActivityManagerService.TAG, "removeChild: detaching " + stack + " from displayId=" + this.mDisplayId);
        } else {
            Slog.v(ActivityManagerService.TAG, "removeChild: detaching stackId=" + stack.mStackId + " from displayId=" + this.mDisplayId);
        }
        this.mStacks.remove(stack);
        removeStackReferenceIfNeeded(stack);
        this.mSupervisor.mService.updateSleepIfNeededLocked();
        onStackOrderChanged();
    }

    /* access modifiers changed from: package-private */
    public void positionChildAtTop(ActivityStack stack) {
        positionChildAt(stack, this.mStacks.size());
    }

    /* access modifiers changed from: package-private */
    public void positionChildAtBottom(ActivityStack stack) {
        positionChildAt(stack, 0);
    }

    private void positionChildAt(ActivityStack stack, int position) {
        this.mStacks.remove(stack);
        int insertPosition = getTopInsertPosition(stack, position);
        this.mStacks.add(insertPosition, stack);
        if (insertPosition == this.mStacks.size() - 1) {
            Slog.v(ActivityManagerService.TAG, "positionChild stackId=" + stack.mStackId + " to top.");
            insertPosition = Integer.MAX_VALUE;
        }
        this.mWindowContainerController.positionChildAt(stack.getWindowContainerController(), insertPosition);
        onStackOrderChanged();
    }

    private int getTopInsertPosition(ActivityStack stack, int candidatePosition) {
        int position = this.mStacks.size();
        if (position > 0) {
            ActivityStack topStack = this.mStacks.get(position - 1);
            if (topStack.getWindowConfiguration().isAlwaysOnTop() && topStack != stack) {
                position--;
            }
        }
        return Math.min(position, candidatePosition);
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getStack(int stackId) {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ActivityStack stack = this.mStacks.get(i);
            if (stack.mStackId == stackId) {
                return stack;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getStack(int windowingMode, int activityType) {
        if (activityType == 2) {
            return this.mHomeStack;
        }
        if (activityType == 3) {
            return this.mRecentsStack;
        }
        if (windowingMode == 2) {
            return this.mPinnedStack;
        }
        if (windowingMode == 3) {
            return this.mSplitScreenPrimaryStack;
        }
        if (windowingMode == 11) {
            return this.mCoordinationPrimaryStack;
        }
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ActivityStack stack = this.mStacks.get(i);
            if (stack.isCompatible(windowingMode, activityType)) {
                return stack;
            }
        }
        return null;
    }

    private boolean alwaysCreateStack(int windowingMode, int activityType) {
        return activityType == 1 && (windowingMode == 1 || windowingMode == 5 || windowingMode == 4 || windowingMode == 12 || windowingMode == 10);
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getOrCreateStack(int windowingMode, int activityType, boolean onTop) {
        if (!alwaysCreateStack(windowingMode, activityType)) {
            T stack = getStack(windowingMode, activityType);
            if (stack != null) {
                return stack;
            }
        }
        return createStack(windowingMode, activityType, onTop);
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getOrCreateStack(ActivityRecord r, ActivityOptions options, TaskRecord candidateTask, int activityType, boolean onTop) {
        return getOrCreateStack(resolveWindowingMode(r, options, candidateTask, activityType), activityType, onTop);
    }

    private int getNextStackId() {
        int i = sNextFreeStackId;
        sNextFreeStackId = i + 1;
        return i;
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T createStack(int windowingMode, int activityType, boolean onTop) {
        if (activityType == 0) {
            activityType = 1;
        }
        if (activityType != 1) {
            T stack = getStack(0, activityType);
            if (stack != null) {
                throw new IllegalArgumentException("Stack=" + stack + " of activityType=" + activityType + " already on display=" + this + ". Can't have multiple.");
            }
        }
        ActivityManagerService service = this.mSupervisor.mService;
        if (isWindowingModeSupported(windowingMode, service.mSupportsMultiWindow, service.mSupportsSplitScreenMultiWindow, service.mSupportsFreeformWindowManagement, service.mSupportsPictureInPicture, activityType)) {
            if (windowingMode == 0) {
                windowingMode = getWindowingMode();
                if (windowingMode == 0) {
                    windowingMode = 1;
                }
            }
            return createStackUnchecked(windowingMode, activityType, getNextStackId(), onTop);
        }
        throw new IllegalArgumentException("Can't create stack for unsupported windowingMode=" + windowingMode);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public <T extends ActivityStack> T createStackUnchecked(int windowingMode, int activityType, int stackId, boolean onTop) {
        if (windowingMode == 2) {
            return new PinnedActivityStack(this, stackId, this.mSupervisor, onTop);
        }
        return HwServiceFactory.createActivityStack(this, stackId, this.mSupervisor, windowingMode, activityType, onTop);
    }

    /* access modifiers changed from: package-private */
    public void removeStacksInWindowingModes(int... windowingModes) {
        if (windowingModes != null && windowingModes.length != 0) {
            for (int j = windowingModes.length - 1; j >= 0; j--) {
                int windowingMode = windowingModes[j];
                for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                    ActivityStack stack = this.mStacks.get(i);
                    if (stack.isActivityTypeStandardOrUndefined() && stack.getWindowingMode() == windowingMode) {
                        if (HwFreeFormUtils.isFreeFormEnable() && stack.getWindowingMode() == 5) {
                            stack.setFreeFormStackVisible(false);
                        }
                        this.mSupervisor.removeStack(stack);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeStacksWithActivityTypes(int... activityTypes) {
        if (activityTypes != null && activityTypes.length != 0) {
            for (int j = activityTypes.length - 1; j >= 0; j--) {
                int activityType = activityTypes[j];
                for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                    ActivityStack stack = this.mStacks.get(i);
                    if (stack.getActivityType() == activityType) {
                        this.mSupervisor.removeStack(stack);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onStackWindowingModeChanged(ActivityStack stack) {
        removeStackReferenceIfNeeded(stack);
        addStackReferenceIfNeeded(stack);
    }

    private void addStackReferenceIfNeeded(ActivityStack stack) {
        int activityType = stack.getActivityType();
        int windowingMode = stack.getWindowingMode();
        if (activityType == 2) {
            if (this.mHomeStack == null || this.mHomeStack == stack) {
                this.mHomeStack = stack;
            } else {
                throw new IllegalArgumentException("addStackReferenceIfNeeded: home stack=" + this.mHomeStack + " already exist on display=" + this + " stack=" + stack);
            }
        } else if (activityType == 3) {
            if (this.mRecentsStack == null || this.mRecentsStack == stack) {
                this.mRecentsStack = stack;
            } else {
                throw new IllegalArgumentException("addStackReferenceIfNeeded: recents stack=" + this.mRecentsStack + " already exist on display=" + this + " stack=" + stack);
            }
        }
        if (windowingMode == 2) {
            if (this.mPinnedStack == null || this.mPinnedStack == stack) {
                this.mPinnedStack = stack;
                return;
            }
            throw new IllegalArgumentException("addStackReferenceIfNeeded: pinned stack=" + this.mPinnedStack + " already exist on display=" + this + " stack=" + stack);
        } else if (windowingMode == 3) {
            if (this.mSplitScreenPrimaryStack == null || this.mSplitScreenPrimaryStack == stack) {
                this.mSplitScreenPrimaryStack = stack;
                onSplitScreenModeActivated();
                return;
            }
            throw new IllegalArgumentException("addStackReferenceIfNeeded: split-screen-primary stack=" + this.mSplitScreenPrimaryStack + " already exist on display=" + this + " stack=" + stack);
        } else if (windowingMode != 11) {
        } else {
            if (this.mCoordinationPrimaryStack == null || this.mCoordinationPrimaryStack == stack) {
                this.mCoordinationPrimaryStack = stack;
                onCoordinationModeActivated();
                return;
            }
            throw new IllegalArgumentException("addStackReferenceIfNeeded: coordination_primary stack=" + this.mCoordinationPrimaryStack + " already exist on display=" + this + " stack=" + stack);
        }
    }

    private void removeStackReferenceIfNeeded(ActivityStack stack) {
        if (stack == this.mHomeStack) {
            this.mHomeStack = null;
        } else if (stack == this.mRecentsStack) {
            this.mRecentsStack = null;
        } else if (stack == this.mPinnedStack) {
            this.mPinnedStack = null;
        } else if (stack == this.mSplitScreenPrimaryStack) {
            this.mSplitScreenPrimaryStack = null;
            onSplitScreenModeDismissed();
        } else if (stack == this.mCoordinationPrimaryStack) {
            this.mCoordinationPrimaryStack = null;
            onCoordinationModeDismissed();
        }
    }

    private void onSplitScreenModeDismissed() {
        this.mSupervisor.mWindowManager.deferSurfaceLayout();
        boolean shouldLaunchSplitActivity = false;
        ActivityStack splitscreenStack = null;
        try {
            for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                ActivityStack otherStack = this.mStacks.get(i);
                ActivityRecord otherTopAR = otherStack.getTopActivity();
                if (otherTopAR != null && otherTopAR.toString().contains("splitscreen.SplitScreenAppActivity")) {
                    splitscreenStack = otherStack;
                } else if (otherStack.inSplitScreenSecondaryWindowingMode()) {
                    otherStack.setWindowingMode(1, false, false, false, true);
                }
            }
            ActivityStack focusStack = this.mSupervisor.getFocusedStack();
            boolean changeFocusFromSplitScreen = false;
            if (focusStack != null) {
                ActivityRecord focusTopAR = focusStack.getTopActivity();
                if (focusTopAR != null && focusTopAR.toString().contains("splitscreen.SplitScreenAppActivity")) {
                    changeFocusFromSplitScreen = true;
                }
            }
            if (splitscreenStack != null && changeFocusFromSplitScreen) {
                Slog.w(ActivityManagerService.TAG, "dismiss split mode and enter home");
                shouldLaunchSplitActivity = true;
            }
            try {
                ActivityStack topFullscreenStack = getTopStackInWindowingMode(1);
                if (!(topFullscreenStack == null || this.mHomeStack == null || isTopStack(this.mHomeStack))) {
                    if (shouldLaunchSplitActivity) {
                        positionChildAtTop(this.mHomeStack);
                    } else {
                        this.mHomeStack.moveToFront("onSplitScreenModeDismissed");
                        topFullscreenStack.moveToFront("onSplitScreenModeDismissed");
                    }
                }
            } finally {
                this.mSupervisor.mWindowManager.continueSurfaceLayout();
            }
        } catch (Throwable th) {
            ActivityStack topFullscreenStack2 = getTopStackInWindowingMode(1);
            if (!(topFullscreenStack2 == null || this.mHomeStack == null || isTopStack(this.mHomeStack))) {
                if (0 != 0) {
                    positionChildAtTop(this.mHomeStack);
                } else {
                    this.mHomeStack.moveToFront("onSplitScreenModeDismissed");
                    topFullscreenStack2.moveToFront("onSplitScreenModeDismissed");
                }
            }
            throw th;
        } finally {
            this.mSupervisor.mWindowManager.continueSurfaceLayout();
        }
    }

    private void onSplitScreenModeActivated() {
        this.mSupervisor.mWindowManager.deferSurfaceLayout();
        try {
            for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                ActivityStack otherStack = this.mStacks.get(i);
                if (otherStack != this.mSplitScreenPrimaryStack) {
                    if (otherStack.affectedBySplitScreenResize()) {
                        otherStack.setWindowingMode(4, false, false, true, true);
                    }
                }
            }
        } finally {
            this.mSupervisor.mWindowManager.continueSurfaceLayout();
        }
    }

    private boolean isWindowingModeSupported(int windowingMode, boolean supportsMultiWindow, boolean supportsSplitScreen, boolean supportsFreeform, boolean supportsPip, int activityType) {
        boolean z = true;
        if (windowingMode == 11 || windowingMode == 12 || windowingMode == 0 || windowingMode == 1) {
            return true;
        }
        if (!supportsMultiWindow) {
            return false;
        }
        if (windowingMode == 3 || windowingMode == 4) {
            if (!supportsSplitScreen || !WindowConfiguration.supportSplitScreenWindowingMode(activityType)) {
                z = false;
            }
            return z;
        } else if (!supportsFreeform && windowingMode == 5) {
            return false;
        } else {
            if (windowingMode == 10 && !HwPCUtils.isPcCastModeInServer()) {
                return false;
            }
            if (supportsPip || windowingMode != 2) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public int resolveWindowingMode(ActivityRecord r, ActivityOptions options, TaskRecord task, int activityType) {
        int i;
        int windowingMode = options != null ? options.getLaunchWindowingMode() : 0;
        if (windowingMode == 0) {
            if (task != null) {
                windowingMode = task.getWindowingMode();
            }
            if (windowingMode == 0 && r != null) {
                windowingMode = r.getWindowingMode();
            }
            if (windowingMode == 0) {
                windowingMode = getWindowingMode();
            }
        }
        ActivityManagerService service = this.mSupervisor.mService;
        boolean supportsMultiWindow = service.mSupportsMultiWindow;
        boolean supportsSplitScreen = service.mSupportsSplitScreenMultiWindow;
        boolean supportsFreeform = service.mSupportsFreeformWindowManagement;
        boolean supportsPip = service.mSupportsPictureInPicture;
        if (supportsMultiWindow) {
            if (task != null) {
                supportsMultiWindow = task.isResizeable();
                supportsSplitScreen = task.supportsSplitScreenWindowingMode();
            } else if (r != null) {
                supportsMultiWindow = r.isResizeable();
                supportsSplitScreen = r.supportsSplitScreenWindowingMode();
                supportsFreeform = r.supportsFreeform();
                supportsPip = r.supportsPictureInPicture();
            }
        }
        boolean supportsMultiWindow2 = supportsMultiWindow;
        boolean supportsSplitScreen2 = supportsSplitScreen;
        boolean supportsFreeform2 = supportsFreeform;
        boolean supportsPip2 = supportsPip;
        boolean inSplitScreenMode = hasSplitScreenPrimaryStack();
        if (!inSplitScreenMode && windowingMode == 4) {
            windowingMode = 1;
        } else if (inSplitScreenMode && windowingMode == 1 && supportsSplitScreen2) {
            windowingMode = 4;
        }
        if (windowingMode != 0) {
            i = 1;
            if (isWindowingModeSupported(windowingMode, supportsMultiWindow2, supportsSplitScreen2, supportsFreeform2, supportsPip2, activityType)) {
                return windowingMode;
            }
        } else {
            i = 1;
        }
        int windowingMode2 = getWindowingMode();
        if (windowingMode2 != 0) {
            i = windowingMode2;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getTopStack() {
        if (this.mStacks.isEmpty()) {
            return null;
        }
        return this.mStacks.get(this.mStacks.size() - 1);
    }

    /* access modifiers changed from: package-private */
    public boolean isTopStack(ActivityStack stack) {
        return stack == getTopStack();
    }

    /* access modifiers changed from: package-private */
    public boolean isTopNotPinnedStack(ActivityStack stack) {
        boolean z = true;
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ActivityStack current = this.mStacks.get(i);
            if (!current.inPinnedWindowingMode()) {
                if (current != stack) {
                    z = false;
                }
                return z;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getTopStackInWindowingMode(int windowingMode) {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ActivityStack current = this.mStacks.get(i);
            if (windowingMode == current.getWindowingMode()) {
                return current;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getIndexOf(ActivityStack stack) {
        return this.mStacks.indexOf(stack);
    }

    /* access modifiers changed from: package-private */
    public void onLockTaskPackagesUpdated() {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            this.mStacks.get(i).onLockTaskPackagesUpdated();
        }
    }

    /* access modifiers changed from: package-private */
    public void onExitingSplitScreenMode() {
        this.mSplitScreenPrimaryStack = null;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getSplitScreenPrimaryStack() {
        return this.mSplitScreenPrimaryStack;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSplitScreenPrimaryStack() {
        return this.mSplitScreenPrimaryStack != null;
    }

    /* access modifiers changed from: package-private */
    public PinnedActivityStack getPinnedStack() {
        return (PinnedActivityStack) this.mPinnedStack;
    }

    /* access modifiers changed from: package-private */
    public boolean hasPinnedStack() {
        return this.mPinnedStack != null;
    }

    public String toString() {
        return "ActivityDisplay={" + this.mDisplayId + " numStacks=" + this.mStacks.size() + "}";
    }

    /* access modifiers changed from: protected */
    public int getChildCount() {
        return this.mStacks.size();
    }

    /* access modifiers changed from: protected */
    public ActivityStack getChildAt(int index) {
        return this.mStacks.get(index);
    }

    /* access modifiers changed from: protected */
    public ConfigurationContainer getParent() {
        return this.mSupervisor;
    }

    /* access modifiers changed from: package-private */
    public boolean isPrivate() {
        return (this.mDisplay.getFlags() & 4) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isUidPresent(int uid) {
        Iterator<ActivityStack> it = this.mStacks.iterator();
        while (it.hasNext()) {
            if (it.next().isUidPresent(uid)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void remove() {
        boolean destroyContentOnRemoval = shouldDestroyContentOnRemove();
        while (getChildCount() > 0) {
            ActivityStack stack = getChildAt(0);
            if (destroyContentOnRemoval) {
                stack.onOverrideConfigurationChanged(stack.getConfiguration());
                this.mSupervisor.moveStackToDisplayLocked(stack.mStackId, 0, false);
                stack.finishAllActivitiesLocked(true);
            } else {
                this.mSupervisor.moveTasksToFullscreenStackLocked(stack, true);
            }
        }
        this.mWindowContainerController.removeContainer();
        this.mWindowContainerController = null;
    }

    /* access modifiers changed from: package-private */
    public IntArray getPresentUIDs() {
        this.mDisplayAccessUIDs.clear();
        Iterator<ActivityStack> it = this.mStacks.iterator();
        while (it.hasNext()) {
            it.next().getPresentUIDs(this.mDisplayAccessUIDs);
        }
        return this.mDisplayAccessUIDs;
    }

    private boolean shouldDestroyContentOnRemove() {
        return this.mDisplay.getRemoveMode() == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldSleep() {
        ActivityManagerService service = this.mSupervisor.mService;
        boolean z = false;
        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || service.mHwAMSEx == null) {
            if ((this.mStacks.isEmpty() || !this.mAllSleepTokens.isEmpty()) && service.mRunningVoice == null) {
                z = true;
            }
            return z;
        }
        if (service.mHwAMSEx.canSleepForPCMode() && service.mRunningVoice == null) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getStackAbove(ActivityStack stack) {
        int stackIndex = this.mStacks.indexOf(stack) + 1;
        if (stackIndex < this.mStacks.size()) {
            return this.mStacks.get(stackIndex);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void moveStackBehindBottomMostVisibleStack(ActivityStack stack) {
        if (!stack.shouldBeVisible(null)) {
            positionChildAtBottom(stack);
            int numStacks = this.mStacks.size();
            int stackNdx = 0;
            while (true) {
                if (stackNdx >= numStacks) {
                    break;
                }
                ActivityStack s = this.mStacks.get(stackNdx);
                if (s != stack) {
                    int winMode = s.getWindowingMode();
                    boolean isValidWindowingMode = true;
                    if (!(winMode == 1 || winMode == 4)) {
                        isValidWindowingMode = false;
                    }
                    if (s.shouldBeVisible(null) && isValidWindowingMode) {
                        positionChildAt(stack, Math.max(0, stackNdx - 1));
                        break;
                    }
                }
                stackNdx++;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void moveStackBehindStack(ActivityStack stack, ActivityStack behindStack) {
        if (behindStack != null && behindStack != stack) {
            int stackIndex = this.mStacks.indexOf(stack);
            int behindStackIndex = this.mStacks.indexOf(behindStack);
            positionChildAt(stack, Math.max(0, stackIndex <= behindStackIndex ? behindStackIndex - 1 : behindStackIndex));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSleeping() {
        return this.mSleeping;
    }

    /* access modifiers changed from: package-private */
    public void setIsSleeping(boolean asleep) {
        if (ActivityManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.v(TAG_KEYGUARD, "set asleep:" + asleep, new Exception());
        }
        this.mSleeping = asleep;
    }

    /* access modifiers changed from: package-private */
    public void registerStackOrderChangedListener(OnStackOrderChangedListener listener) {
        if (!this.mStackOrderChangedCallbacks.contains(listener)) {
            this.mStackOrderChangedCallbacks.add(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterStackOrderChangedListener(OnStackOrderChangedListener listener) {
        this.mStackOrderChangedCallbacks.remove(listener);
    }

    private void onStackOrderChanged() {
        for (int i = this.mStackOrderChangedCallbacks.size() - 1; i >= 0; i--) {
            this.mStackOrderChangedCallbacks.get(i).onStackOrderChanged();
        }
    }

    public void deferUpdateImeTarget() {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.deferUpdateImeTarget();
            return;
        }
        Slog.e(ActivityManagerService.TAG, "controller is null when deferUpdateImeTarget for displayId:" + this.mDisplayId);
    }

    public void continueUpdateImeTarget() {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.continueUpdateImeTarget();
            return;
        }
        Slog.e(ActivityManagerService.TAG, "controller is null when continueUpdateImeTarget for displayId:" + this.mDisplayId);
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "displayId=" + this.mDisplayId + " stacks=" + this.mStacks.size());
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(" ");
        String myPrefix = sb.toString();
        if (this.mHomeStack != null) {
            pw.println(myPrefix + "mHomeStack=" + this.mHomeStack);
        }
        if (this.mRecentsStack != null) {
            pw.println(myPrefix + "mRecentsStack=" + this.mRecentsStack);
        }
        if (this.mPinnedStack != null) {
            pw.println(myPrefix + "mPinnedStack=" + this.mPinnedStack);
        }
        if (this.mSplitScreenPrimaryStack != null) {
            pw.println(myPrefix + "mSplitScreenPrimaryStack=" + this.mSplitScreenPrimaryStack);
        }
    }

    public void dumpStacks(PrintWriter pw) {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            pw.print(this.mStacks.get(i).mStackId);
            if (i > 0) {
                pw.print(",");
            }
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, false);
        proto.write(1120986464258L, this.mDisplayId);
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            this.mStacks.get(stackNdx).writeToProto(proto, 2246267895811L);
        }
        proto.end(token);
    }

    private void onCoordinationModeDismissed() {
        this.mSupervisor.mWindowManager.deferSurfaceLayout();
        try {
            for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                ActivityStack otherStack = this.mStacks.get(i);
                if (otherStack.inCoordinationSecondaryWindowingMode()) {
                    otherStack.setWindowingMode(1, false, false, false, true);
                }
            }
        } finally {
            this.mSupervisor.mWindowManager.continueSurfaceLayout();
        }
    }

    private void onCoordinationModeActivated() {
        this.mSupervisor.mWindowManager.deferSurfaceLayout();
        try {
            for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                ActivityStack otherStack = this.mStacks.get(i);
                if (otherStack != this.mCoordinationPrimaryStack) {
                    otherStack.setWindowingMode(12, false, false, false, true);
                }
            }
        } finally {
            this.mSupervisor.mWindowManager.continueSurfaceLayout();
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getCoordinationPrimaryStack() {
        return this.mCoordinationPrimaryStack;
    }

    /* access modifiers changed from: package-private */
    public boolean hasCoordinationPrimaryStack() {
        return this.mCoordinationPrimaryStack != null;
    }
}
