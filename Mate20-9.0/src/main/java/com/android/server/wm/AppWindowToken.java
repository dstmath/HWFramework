package com.android.server.wm;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.graphics.GraphicBuffer;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Trace;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import android.view.IApplicationToken;
import android.view.RemoteAnimationDefinition;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.animation.Animation;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.input.InputApplicationHandle;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedThisRef;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

class AppWindowToken extends WindowToken implements WindowManagerService.AppFreezeListener {
    private static final int APP_ANIMATION_DURATION = 300;
    private static final String TAG = "WindowManager";
    private static final String TAG_VISIBILITY = "WindowManager_visibility";
    private static final int Z_BOOST_BASE = 800570000;
    boolean allDrawn;
    String appComponentName;
    String appPackageName;
    int appPid;
    String appProcessName;
    final IApplicationToken appToken;
    boolean deferClearAllDrawn;
    boolean firstWindowDrawn;
    boolean hiddenRequested;
    boolean inPendingTransaction;
    boolean layoutConfigChanges;
    private boolean mAlwaysFocusable;
    private AnimatingAppWindowTokenRegistry mAnimatingAppWindowTokenRegistry;
    boolean mAppStopped;
    private boolean mCanTurnScreenOn;
    private boolean mClientHidden;
    boolean mDeferHidingClient;
    private boolean mDisablePreviewScreenshots;
    boolean mEnteringAnimation;
    private boolean mFillsParent;
    private boolean mFreezingScreen;
    ArrayDeque<Rect> mFrozenBounds;
    ArrayDeque<Configuration> mFrozenMergedConfig;
    private boolean mHiddenSetFromTransferredStartingWindow;
    private int mHwGestureNavOptions;
    private boolean mHwNotchSupport;
    final InputApplicationHandle mInputApplicationHandle;
    long mInputDispatchingTimeoutNanos;
    boolean mIsExiting;
    private boolean mLastAllDrawn;
    private boolean mLastContainsDismissKeyguardWindow;
    private boolean mLastContainsShowWhenLockedWindow;
    private Task mLastParent;
    private boolean mLastSurfaceShowing;
    private long mLastTransactionSequence;
    boolean mLaunchTaskBehind;
    private Letterbox mLetterbox;
    private boolean mNeedsZBoost;
    private int mNumDrawnWindows;
    private int mNumInterestingWindows;
    int mPendingRelaunchCount;
    private RemoteAnimationDefinition mRemoteAnimationDefinition;
    private boolean mRemovingFromDisplay;
    private boolean mReparenting;
    private final WindowState.UpdateReportedVisibilityResults mReportedVisibilityResults;
    int mRotationAnimationHint;
    boolean mShouldDrawIcon;
    boolean mShowForAllUsers;
    boolean mShowWhenLocked;
    int mTargetSdk;
    private AppWindowThumbnail mThumbnail;
    private final Point mTmpPoint;
    private final Rect mTmpRect;
    private int mTransit;
    private int mTransitFlags;
    final boolean mVoiceInteraction;
    private boolean mWillCloseOrEnterPip;
    boolean navigationBarHide;
    boolean removed;
    private boolean reportedDrawn;
    boolean reportedVisible;
    StartingData startingData;
    boolean startingDisplayed;
    boolean startingMoved;
    WindowManagerPolicy.StartingSurface startingSurface;
    WindowState startingWindow;

    public boolean getHwNotchSupport() {
        return this.mHwNotchSupport;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    AppWindowToken(WindowManagerService service, IApplicationToken token, boolean voiceInteraction, DisplayContent dc, long inputDispatchingTimeoutNanos, boolean fullscreen, boolean showForAllUsers, int targetSdk, int orientation, int rotationAnimationHint, int configChanges, boolean launchTaskBehind, boolean alwaysFocusable, AppWindowContainerController controller, boolean naviBarHide, ActivityInfo info) {
        this(service, token, voiceInteraction, dc, fullscreen);
        ActivityInfo activityInfo = info;
        setController(controller);
        this.mInputDispatchingTimeoutNanos = inputDispatchingTimeoutNanos;
        this.mShowForAllUsers = showForAllUsers;
        this.mTargetSdk = targetSdk;
        this.mOrientation = orientation;
        this.navigationBarHide = naviBarHide;
        this.layoutConfigChanges = (configChanges & 1152) != 0;
        this.mLaunchTaskBehind = launchTaskBehind;
        this.mAlwaysFocusable = alwaysFocusable;
        this.mRotationAnimationHint = rotationAnimationHint;
        setHidden(true);
        this.hiddenRequested = true;
        this.mHwNotchSupport = activityInfo.hwNotchSupport;
        this.appPackageName = activityInfo.packageName;
        this.appComponentName = info.getComponentName().flattenToShortString();
        this.appProcessName = activityInfo.applicationInfo.processName;
        this.mHwGestureNavOptions = activityInfo.hwGestureNavOptions;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    AppWindowToken(WindowManagerService service, IApplicationToken token, boolean voiceInteraction, DisplayContent dc, boolean fillsParent) {
        super(service, token != null ? token.asBinder() : null, 2, true, dc, false);
        this.mShouldDrawIcon = false;
        this.mRemovingFromDisplay = false;
        this.mLastTransactionSequence = Long.MIN_VALUE;
        this.mReportedVisibilityResults = new WindowState.UpdateReportedVisibilityResults();
        this.mFrozenBounds = new ArrayDeque<>();
        this.mFrozenMergedConfig = new ArrayDeque<>();
        this.mCanTurnScreenOn = true;
        this.mLastSurfaceShowing = true;
        this.mTmpPoint = new Point();
        this.mTmpRect = new Rect();
        this.appToken = token;
        this.mVoiceInteraction = voiceInteraction;
        this.mFillsParent = fillsParent;
        this.mInputApplicationHandle = new InputApplicationHandle(this);
    }

    /* access modifiers changed from: package-private */
    public void onFirstWindowDrawn(WindowState win, WindowStateAnimator winAnimator) {
        this.firstWindowDrawn = true;
        removeDeadWindows();
        if (this.startingWindow != null) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Finish starting " + win.mToken + ": first real window is shown, no animation");
            }
            win.cancelAnimation();
            if (getController() != null) {
                getController().removeStartingWindow();
            }
        }
        updateReportedVisibilityLocked();
    }

    /* access modifiers changed from: package-private */
    public void updateReportedVisibilityLocked() {
        if (this.appToken != null) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "Update reported visibility: " + this);
            }
            int count = this.mChildren.size();
            this.mReportedVisibilityResults.reset();
            boolean nowVisible = false;
            for (int i = 0; i < count; i++) {
                ((WindowState) this.mChildren.get(i)).updateReportedVisibility(this.mReportedVisibilityResults);
            }
            int numInteresting = this.mReportedVisibilityResults.numInteresting;
            int numVisible = this.mReportedVisibilityResults.numVisible;
            int numDrawn = this.mReportedVisibilityResults.numDrawn;
            boolean nowGone = this.mReportedVisibilityResults.nowGone;
            boolean nowDrawn = numInteresting > 0 && numDrawn >= numInteresting;
            if (numInteresting > 0 && numVisible >= numInteresting && !isHidden()) {
                nowVisible = true;
            }
            if (!nowGone) {
                if (!nowDrawn) {
                    nowDrawn = this.reportedDrawn;
                }
                if (!nowVisible) {
                    nowVisible = this.reportedVisible;
                }
            }
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "VIS " + this + ": interesting=" + numInteresting + " visible=" + numVisible);
            }
            AppWindowContainerController controller = getController();
            if (nowDrawn != this.reportedDrawn) {
                if (nowDrawn && controller != null) {
                    controller.reportWindowsDrawn();
                }
                this.reportedDrawn = nowDrawn;
            }
            if (nowVisible != this.reportedVisible) {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG, "Visibility changed in " + this + ": vis=" + nowVisible);
                }
                this.reportedVisible = nowVisible;
                if (controller != null) {
                    if (nowVisible) {
                        controller.reportWindowsVisible();
                    } else {
                        if (toString().contains("com.android.incallui/.InCallActivity")) {
                            Slog.i(TAG, "InCallActivity windowsGone, numInteresting=" + numInteresting + " numVisible=" + numVisible + " numDrawn=" + numDrawn + " nowGone=" + nowGone + " callers=" + Debug.getCallers(4));
                        }
                        controller.reportWindowsGone();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isClientHidden() {
        return this.mClientHidden;
    }

    /* access modifiers changed from: package-private */
    public void setClientHidden(boolean hideClient) {
        if (this.mClientHidden != hideClient && (!hideClient || !this.mDeferHidingClient)) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "setClientHidden: " + this + " clientHidden=" + hideClient + " Callers=" + Debug.getCallers(5));
            }
            this.mClientHidden = hideClient;
            sendAppVisibilityToClients();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setVisibility(WindowManager.LayoutParams lp, boolean visible, int transit, boolean performLayout, boolean isVoiceInteraction) {
        boolean z = visible;
        int i = transit;
        boolean z2 = performLayout;
        boolean delayed = false;
        this.inPendingTransaction = false;
        this.mHiddenSetFromTransferredStartingWindow = false;
        boolean visibilityChanged = false;
        if (isHidden() == z || ((isHidden() && this.mIsExiting) || (z && waitingForReplacement()))) {
            AccessibilityController accessibilityController = this.mService.mAccessibilityController;
            boolean changed = false;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Changing app " + this + " hidden=" + isHidden() + " performLayout=" + z2);
            }
            boolean runningAppAnimation = false;
            if (i != -1) {
                if (applyAnimationLocked(lp, i, z, isVoiceInteraction)) {
                    runningAppAnimation = true;
                    delayed = true;
                }
                WindowState window = findMainWindow();
                if (!(window == null || accessibilityController == null || getDisplayContent().getDisplayId() != 0)) {
                    accessibilityController.onAppWindowTransitionLocked(window, i);
                }
                changed = true;
            } else {
                WindowManager.LayoutParams layoutParams = lp;
                boolean z3 = isVoiceInteraction;
            }
            int windowsCount = this.mChildren.size();
            boolean changed2 = changed;
            for (int i2 = 0; i2 < windowsCount; i2++) {
                changed2 |= ((WindowState) this.mChildren.get(i2)).onAppVisibilityChanged(z, runningAppAnimation);
            }
            setHidden(!z);
            this.hiddenRequested = !z;
            visibilityChanged = true;
            if (!z) {
                stopFreezingScreen(true, true);
            } else {
                if (this.startingWindow != null && !this.startingWindow.isDrawnLw()) {
                    this.startingWindow.mPolicyVisibility = false;
                    this.startingWindow.mPolicyVisibilityAfterAnim = false;
                }
                WindowManagerService windowManagerService = this.mService;
                Objects.requireNonNull(windowManagerService);
                forAllWindows((Consumer<WindowState>) new Consumer() {
                    public final void accept(Object obj) {
                        WindowManagerService.this.makeWindowFreezingScreenIfNeededLocked((WindowState) obj);
                    }
                }, true);
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "setVisibility: " + this + ": hidden=" + isHidden() + " hiddenRequested=" + this.hiddenRequested);
            }
            if (changed2) {
                this.mService.mInputMonitor.setUpdateInputWindowsNeededLw();
                if (z2) {
                    this.mService.updateFocusedWindowLocked(3, false);
                    this.mService.mWindowPlacerLocked.performSurfacePlacement();
                }
                this.mService.mInputMonitor.updateInputWindowsLw(false);
            }
        } else {
            WindowManager.LayoutParams layoutParams2 = lp;
            boolean z4 = isVoiceInteraction;
        }
        if (isReallyAnimating()) {
            delayed = true;
        } else {
            onAnimationFinished();
        }
        for (int i3 = this.mChildren.size() - 1; i3 >= 0 && !delayed; i3--) {
            if (((WindowState) this.mChildren.get(i3)).isSelfOrChildAnimating()) {
                delayed = true;
            }
        }
        if (visibilityChanged) {
            if (z && !delayed) {
                this.mEnteringAnimation = true;
                this.mService.mActivityManagerAppTransitionNotifier.onAppTransitionFinishedLocked(this.token);
            }
            if (z || !isReallyAnimating()) {
                setClientHidden(!z);
            }
            if (!this.mService.mClosingApps.contains(this) && !this.mService.mOpeningApps.contains(this)) {
                this.mService.getDefaultDisplayContentLocked().getDockedDividerController().notifyAppVisibilityChanged();
                this.mService.mTaskSnapshotController.notifyAppVisibilityChanged(this, z);
            }
            if (isHidden() && !delayed && !this.mService.mAppTransition.isTransitionSet()) {
                SurfaceControl.openTransaction();
                for (int i4 = this.mChildren.size() - 1; i4 >= 0; i4--) {
                    ((WindowState) this.mChildren.get(i4)).mWinAnimator.hide("immediately hidden");
                }
                SurfaceControl.closeTransaction();
            }
        }
        return delayed;
    }

    /* access modifiers changed from: package-private */
    public WindowState getTopFullscreenWindow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) this.mChildren.get(i);
            if (win != null && win.mAttrs.isFullscreen()) {
                return win;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowState findMainWindow() {
        return findMainWindow(true);
    }

    /* access modifiers changed from: package-private */
    public WindowState findMainWindow(boolean includeStartingApp) {
        WindowState candidate = null;
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            WindowState win = (WindowState) this.mChildren.get(j);
            if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || win != null) {
                int type = win.mAttrs.type;
                if (type == 1 || (includeStartingApp && type == 3)) {
                    if (!win.mAnimatingExit) {
                        return win;
                    }
                    candidate = win;
                }
            }
        }
        return candidate;
    }

    /* access modifiers changed from: package-private */
    public boolean windowsAreFocusable() {
        return getWindowConfiguration().canReceiveKeys() || this.mAlwaysFocusable;
    }

    /* access modifiers changed from: package-private */
    public AppWindowContainerController getController() {
        WindowContainerController controller = super.getController();
        if (controller != null) {
            return (AppWindowContainerController) controller;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean isVisible() {
        return !isHidden();
    }

    /* access modifiers changed from: package-private */
    public void removeImmediately() {
        onRemovedFromDisplay();
        super.removeImmediately();
    }

    /* access modifiers changed from: package-private */
    public void removeIfPossible() {
        this.mIsExiting = false;
        removeAllWindowsIfPossible();
        removeImmediately();
    }

    /* access modifiers changed from: package-private */
    public boolean checkCompleteDeferredRemoval() {
        if (this.mIsExiting) {
            removeIfPossible();
        }
        return super.checkCompleteDeferredRemoval();
    }

    /* access modifiers changed from: package-private */
    public void onRemovedFromDisplay() {
        if (!this.mRemovingFromDisplay) {
            this.mRemovingFromDisplay = true;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Removing app token: " + this);
            }
            boolean delayed = setVisibility(null, false, -1, true, this.mVoiceInteraction);
            this.mService.mOpeningApps.remove(this);
            this.mService.mUnknownAppVisibilityController.appRemovedOrHidden(this);
            this.mService.mTaskSnapshotController.onAppRemoved(this);
            this.waitingToShow = false;
            if (this.mService.mClosingApps.contains(this)) {
                delayed = true;
            } else if (this.mService.mAppTransition.isTransitionSet()) {
                this.mService.mClosingApps.add(this);
                delayed = true;
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Removing app " + this + " delayed=" + delayed + " animation=" + getAnimation() + " animating=" + isSelfAnimating());
            }
            Slog.v(TAG, "removeAppToken: " + this + " delayed=" + delayed + " Callers=" + Debug.getCallers(4));
            if (!(this.startingData == null || getController() == null)) {
                getController().removeStartingWindow();
            }
            if (isSelfAnimating()) {
                this.mService.mNoAnimationNotifyOnTransitionFinished.add(this.token);
            }
            TaskStack stack = getStack();
            if (!delayed || isEmpty()) {
                cancelAnimation();
                if (stack != null) {
                    stack.mExitingAppTokens.remove(this);
                }
                removeIfPossible();
            } else {
                Slog.v(TAG, "removeAppToken make exiting: " + this);
                if (stack != null) {
                    stack.mExitingAppTokens.add(this);
                }
                this.mIsExiting = true;
            }
            this.removed = true;
            stopFreezingScreen(true, true);
            if (this.mService.mFocusedApp == this) {
                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                    Slog.v(TAG, "Removing focused app token:" + this);
                }
                this.mService.mFocusedApp = null;
                this.mService.updateFocusedWindowLocked(0, true);
                this.mService.mInputMonitor.setFocusedAppLw(null);
            }
            if (!delayed) {
                updateReportedVisibilityLocked();
            }
            this.mRemovingFromDisplay = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAnimatingFlags() {
        boolean wallpaperMightChange = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            wallpaperMightChange |= ((WindowState) this.mChildren.get(i)).clearAnimatingFlags();
        }
        if (wallpaperMightChange) {
            requestUpdateWallpaperIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void destroySurfaces() {
        destroySurfaces(false);
    }

    private void destroySurfaces(boolean cleanupOnResume) {
        boolean destroyedSomething = false;
        ArrayList<WindowState> children = new ArrayList<>(this.mChildren);
        for (int i = children.size() - 1; i >= 0; i--) {
            destroyedSomething |= children.get(i).destroySurface(cleanupOnResume, this.mAppStopped);
        }
        if (destroyedSomething) {
            getDisplayContent().assignWindowLayers(true);
            updateLetterboxSurface(null);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyAppResumed(boolean wasStopped) {
        Slog.v(TAG, "notifyAppResumed: wasStopped=" + wasStopped + " " + this);
        this.mAppStopped = false;
        setCanTurnScreenOn(true);
        if (!wasStopped) {
            destroySurfaces(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyAppStopped() {
        Slog.v(TAG, "notifyAppStopped: " + this);
        this.mAppStopped = true;
        destroySurfaces();
        if (getController() != null) {
            getController().removeStartingWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAllDrawn() {
        this.allDrawn = false;
        this.deferClearAllDrawn = false;
    }

    /* access modifiers changed from: package-private */
    public Task getTask() {
        return (Task) getParent();
    }

    /* access modifiers changed from: package-private */
    public TaskStack getStack() {
        Task task = getTask();
        if (task != null) {
            return task.mStack;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void onParentSet() {
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry;
        super.onParentSet();
        Task task = getTask();
        if (!this.mReparenting) {
            if (task == null) {
                this.mService.mClosingApps.remove(this);
            } else if (!(this.mLastParent == null || this.mLastParent.mStack == null)) {
                task.mStack.mExitingAppTokens.remove(this);
            }
        }
        TaskStack stack = getStack();
        if (this.mAnimatingAppWindowTokenRegistry != null) {
            this.mAnimatingAppWindowTokenRegistry.notifyFinished(this);
        }
        if (stack != null) {
            animatingAppWindowTokenRegistry = stack.getAnimatingAppWindowTokenRegistry();
        } else {
            animatingAppWindowTokenRegistry = null;
        }
        this.mAnimatingAppWindowTokenRegistry = animatingAppWindowTokenRegistry;
        this.mLastParent = task;
    }

    /* access modifiers changed from: package-private */
    public void postWindowRemoveStartingWindowCleanup(WindowState win) {
        if (this.startingWindow == win) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Notify removed startingWindow " + win);
            }
            if (getController() != null) {
                getController().removeStartingWindow();
            }
        } else if (this.mChildren.size() == 0) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Nulling last startingData");
            }
            this.startingData = null;
            if (this.mHiddenSetFromTransferredStartingWindow) {
                setHidden(true);
            }
        } else if (this.mChildren.size() == 1 && this.startingSurface != null && !isRelaunching()) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Last window, removing starting window " + win);
            }
            if (getController() != null) {
                getController().removeStartingWindow();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeDeadWindows() {
        for (int winNdx = this.mChildren.size() - 1; winNdx >= 0; winNdx--) {
            WindowState win = (WindowState) this.mChildren.get(winNdx);
            if (win.mAppDied) {
                Slog.w(TAG, "removeDeadWindows: " + win);
                win.mDestroying = true;
                win.removeIfPossible();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasWindowsAlive() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (!((WindowState) this.mChildren.get(i)).mAppDied) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setWillReplaceWindows(boolean animate) {
        Slog.d(TAG, "Marking app token " + this + " with replacing windows.");
        for (int i = this.mChildren.size() + -1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceWindow(animate);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWillReplaceChildWindows() {
        Slog.d(TAG, "Marking app token " + this + " with replacing child windows.");
        for (int i = this.mChildren.size() + -1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceChildWindows();
        }
    }

    /* access modifiers changed from: package-private */
    public void clearWillReplaceWindows() {
        Slog.d(TAG, "Resetting app token " + this + " of replacing window marks.");
        for (int i = this.mChildren.size() + -1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).clearWillReplaceWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public void requestUpdateWallpaperIfNeeded() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).requestUpdateWallpaperIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isRelaunching() {
        return this.mPendingRelaunchCount > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldFreezeBounds() {
        Task task = getTask();
        if (task == null || task.inFreeformWindowingMode() || task.inHwPCFreeformWindowingMode()) {
            return false;
        }
        return getTask().isDragResizing();
    }

    /* access modifiers changed from: package-private */
    public void startRelaunching() {
        if (shouldFreezeBounds()) {
            freezeBounds();
        }
        detachChildren();
        this.mPendingRelaunchCount++;
    }

    /* access modifiers changed from: package-private */
    public void detachChildren() {
        SurfaceControl.openTransaction();
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).mWinAnimator.detachChildren();
        }
        SurfaceControl.closeTransaction();
    }

    /* access modifiers changed from: package-private */
    public void finishRelaunching() {
        unfreezeBounds();
        if (this.mPendingRelaunchCount > 0) {
            this.mPendingRelaunchCount--;
        } else {
            checkKeyguardFlagsChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void clearRelaunching() {
        if (this.mPendingRelaunchCount != 0) {
            unfreezeBounds();
            this.mPendingRelaunchCount = 0;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isFirstChildWindowGreaterThanSecond(WindowState newWindow, WindowState existingWindow) {
        int type1 = newWindow.mAttrs.type;
        int type2 = existingWindow.mAttrs.type;
        if (type1 == 1 && type2 != 1) {
            return false;
        }
        if (type1 == 1 || type2 != 1) {
            return (type1 == 3 && type2 != 3) || type1 == 3 || type2 != 3;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void addWindow(WindowState w) {
        super.addWindow(w);
        boolean gotReplacementWindow = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            gotReplacementWindow |= ((WindowState) this.mChildren.get(i)).setReplacementWindowIfNeeded(w);
        }
        if (gotReplacementWindow) {
            this.mService.scheduleWindowReplacementTimeouts(this);
        }
        checkKeyguardFlagsChanged();
    }

    /* access modifiers changed from: package-private */
    public void removeChild(WindowState child) {
        super.removeChild(child);
        checkKeyguardFlagsChanged();
        updateLetterboxSurface(child);
    }

    private boolean waitingForReplacement() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).waitingForReplacement()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void onWindowReplacementTimeout() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onWindowReplacementTimeout();
        }
    }

    /* access modifiers changed from: package-private */
    public void reparent(Task task, int position) {
        Task currentTask = getTask();
        if (task == currentTask) {
            throw new IllegalArgumentException("window token=" + this + " already child of task=" + currentTask);
        } else if (currentTask.mStack == task.mStack) {
            Slog.i(TAG, "reParentWindowToken: removing window token=" + this + " from task=" + currentTask);
            DisplayContent prevDisplayContent = getDisplayContent();
            this.mReparenting = true;
            getParent().removeChild(this);
            task.addChild(this, position);
            this.mReparenting = false;
            DisplayContent displayContent = task.getDisplayContent();
            displayContent.setLayoutNeeded();
            if (prevDisplayContent != displayContent) {
                onDisplayChanged(displayContent);
                prevDisplayContent.setLayoutNeeded();
            }
        } else {
            throw new IllegalArgumentException("window token=" + this + " current task=" + currentTask + " belongs to a different stack than " + task);
        }
    }

    private void freezeBounds() {
        Task task = getTask();
        this.mFrozenBounds.offer(new Rect(task.mPreparedFrozenBounds));
        if (task.mPreparedFrozenMergedConfig.equals(Configuration.EMPTY)) {
            this.mFrozenMergedConfig.offer(new Configuration(task.getConfiguration()));
        } else {
            this.mFrozenMergedConfig.offer(new Configuration(task.mPreparedFrozenMergedConfig));
        }
        task.mPreparedFrozenMergedConfig.unset();
    }

    private void unfreezeBounds() {
        if (!this.mFrozenBounds.isEmpty()) {
            this.mFrozenBounds.remove();
            if (!this.mFrozenMergedConfig.isEmpty()) {
                this.mFrozenMergedConfig.remove();
            }
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                ((WindowState) this.mChildren.get(i)).onUnfreezeBounds();
            }
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    /* access modifiers changed from: package-private */
    public void setAppLayoutChanges(int changes, String reason) {
        if (!this.mChildren.isEmpty()) {
            getDisplayContent().pendingLayoutChanges |= changes;
        }
    }

    /* access modifiers changed from: package-private */
    public void removeReplacedWindowIfNeeded(WindowState replacement) {
        int i = this.mChildren.size() - 1;
        while (i >= 0 && !((WindowState) this.mChildren.get(i)).removeReplacedWindowIfNeeded(replacement)) {
            i--;
        }
    }

    /* access modifiers changed from: package-private */
    public void startFreezingScreen() {
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            WindowManagerService.logWithStack(TAG, "Set freezing of " + this.appToken + ": hidden=" + isHidden() + " freezing=" + this.mFreezingScreen + " hiddenRequested=" + this.hiddenRequested);
        }
        if (!this.hiddenRequested) {
            if (!this.mFreezingScreen) {
                this.mFreezingScreen = true;
                this.mService.registerAppFreezeListener(this);
                this.mService.mAppsFreezingScreen++;
                if (this.mService.mAppsFreezingScreen == 1) {
                    this.mService.startFreezingDisplayLocked(0, 0, getDisplayContent());
                    this.mService.mH.removeMessages(17);
                    long delayTime = 2000;
                    if (this.mService.mShouldResetTime) {
                        this.mService.mShouldResetTime = false;
                        delayTime = 600;
                    }
                    this.mService.mH.sendEmptyMessageDelayed(17, delayTime);
                }
            }
            int count = this.mChildren.size();
            for (int i = 0; i < count; i++) {
                ((WindowState) this.mChildren.get(i)).onStartFreezingScreen();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stopFreezingScreen(boolean unfreezeSurfaceNow, boolean force) {
        if (this.mFreezingScreen) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Clear freezing of " + this + " force=" + force);
            }
            int count = this.mChildren.size();
            boolean unfrozeWindows = false;
            for (int i = 0; i < count; i++) {
                unfrozeWindows |= ((WindowState) this.mChildren.get(i)).onStopFreezingScreen();
            }
            if (force || unfrozeWindows) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(TAG, "No longer freezing: " + this);
                }
                this.mFreezingScreen = false;
                this.mService.unregisterAppFreezeListener(this);
                WindowManagerService windowManagerService = this.mService;
                windowManagerService.mAppsFreezingScreen--;
                this.mService.mLastFinishedFreezeSource = this;
            }
            if (unfreezeSurfaceNow) {
                if (unfrozeWindows) {
                    this.mService.mWindowPlacerLocked.performSurfacePlacement();
                }
                this.mService.stopFreezingDisplayLocked();
            }
        }
    }

    public void onAppFreezeTimeout() {
        Slog.w(TAG, "Force clearing freeze: " + this);
        stopFreezingScreen(true, true);
    }

    /* access modifiers changed from: package-private */
    public void transferStartingWindowFromHiddenAboveTokenIfNeeded() {
        Task task = getTask();
        int i = task.mChildren.size() - 1;
        while (i >= 0) {
            AppWindowToken fromToken = (AppWindowToken) task.mChildren.get(i);
            if (fromToken != this) {
                if (!fromToken.hiddenRequested || !transferStartingWindow(fromToken.token)) {
                    i--;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean transferStartingWindow(IBinder transferFrom) {
        AppWindowToken fromToken = getDisplayContent().getAppWindowToken(transferFrom);
        if (fromToken == null || this.startingWindow != null) {
            return false;
        }
        WindowState tStartingWindow = fromToken.startingWindow;
        if (tStartingWindow != null && fromToken.startingSurface != null) {
            this.mService.mSkipAppTransitionAnimation = true;
            Flog.i(301, "Moving existing starting " + tStartingWindow + " from " + fromToken + " to " + this);
            long origId = Binder.clearCallingIdentity();
            try {
                this.startingData = fromToken.startingData;
                this.startingSurface = fromToken.startingSurface;
                this.startingDisplayed = fromToken.startingDisplayed;
                fromToken.startingDisplayed = false;
                this.startingWindow = tStartingWindow;
                this.reportedVisible = fromToken.reportedVisible;
                fromToken.startingData = null;
                fromToken.startingSurface = null;
                fromToken.startingWindow = null;
                fromToken.startingMoved = true;
                tStartingWindow.mToken = this;
                tStartingWindow.mAppToken = this;
                Slog.v(TAG, "Removing starting " + tStartingWindow + " from " + fromToken);
                fromToken.removeChild(tStartingWindow);
                fromToken.postWindowRemoveStartingWindowCleanup(tStartingWindow);
                fromToken.mHiddenSetFromTransferredStartingWindow = false;
                addWindow(tStartingWindow);
                if (fromToken.allDrawn) {
                    this.allDrawn = true;
                    this.deferClearAllDrawn = fromToken.deferClearAllDrawn;
                }
                if (fromToken.firstWindowDrawn) {
                    this.firstWindowDrawn = true;
                }
                if (!fromToken.isHidden()) {
                    setHidden(false);
                    this.hiddenRequested = false;
                    this.mHiddenSetFromTransferredStartingWindow = true;
                }
                setClientHidden(fromToken.mClientHidden);
                transferAnimation(fromToken);
                this.mService.mOpeningApps.remove(this);
                this.mService.updateFocusedWindowLocked(3, true);
                getDisplayContent().setLayoutNeeded();
                this.mService.mWindowPlacerLocked.performSurfacePlacement();
                return true;
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else if (fromToken.startingData == null) {
            return false;
        } else {
            Flog.i(301, "Moving pending starting from " + fromToken + " to " + this);
            if (toString().contains("com.eg.android.AlipayGphone/.AlipayLogin")) {
                return false;
            }
            this.startingData = fromToken.startingData;
            fromToken.startingData = null;
            fromToken.startingMoved = true;
            if (getController() != null) {
                getController().scheduleAddStartingWindow();
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLastWindow(WindowState win) {
        return this.mChildren.size() == 1 && this.mChildren.get(0) == win;
    }

    /* access modifiers changed from: package-private */
    public void onAppTransitionDone() {
        this.sendingToBottom = false;
    }

    /* access modifiers changed from: package-private */
    public int getOrientation(int candidate) {
        if (this.mService.checkAppOrientationForForceRotation(this)) {
            return -1;
        }
        if (candidate == 3) {
            return this.mOrientation;
        }
        if (this.sendingToBottom || this.mService.mClosingApps.contains(this) || (!isVisible() && !this.mService.mOpeningApps.contains(this))) {
            return -2;
        }
        return this.mOrientation;
    }

    /* access modifiers changed from: package-private */
    public int getOrientationIgnoreVisibility() {
        return this.mOrientation;
    }

    public void onConfigurationChanged(Configuration newParentConfig) {
        Rect stackBounds;
        int prevWinMode = getWindowingMode();
        super.onConfigurationChanged(newParentConfig);
        int winMode = getWindowingMode();
        if (prevWinMode != winMode) {
            if (prevWinMode != 0 && winMode == 2) {
                this.mDisplayContent.mPinnedStackControllerLocked.resetReentrySnapFraction(this);
            } else if (prevWinMode == 2 && winMode != 0 && !isHidden()) {
                TaskStack pinnedStack = this.mDisplayContent.getPinnedStack();
                if (pinnedStack != null) {
                    if (pinnedStack.lastAnimatingBoundsWasToFullscreen()) {
                        stackBounds = pinnedStack.mPreAnimationBounds;
                    } else {
                        stackBounds = this.mTmpRect;
                        pinnedStack.getBounds(stackBounds);
                    }
                    this.mDisplayContent.mPinnedStackControllerLocked.saveReentrySnapFraction(this, stackBounds);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void checkAppWindowsReadyToShow() {
        if (this.allDrawn != this.mLastAllDrawn) {
            this.mLastAllDrawn = this.allDrawn;
            if (this.allDrawn) {
                if (this.mFreezingScreen) {
                    showAllWindowsLocked();
                    stopFreezingScreen(false, true);
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.i(TAG, "Setting mOrientationChangeComplete=true because wtoken " + this + " numInteresting=" + this.mNumInterestingWindows + " numDrawn=" + this.mNumDrawnWindows);
                    }
                    setAppLayoutChanges(4, "checkAppWindowsReadyToShow: freezingScreen");
                } else {
                    setAppLayoutChanges(8, "checkAppWindowsReadyToShow");
                    if (!this.mService.mOpeningApps.contains(this)) {
                        showAllWindowsLocked();
                    }
                }
            }
        }
    }

    private boolean allDrawnStatesConsidered() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState child = (WindowState) this.mChildren.get(i);
            if (child.mightAffectAllDrawn() && !child.getDrawnStateEvaluated()) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void updateAllDrawn() {
        Slog.v(TAG_VISIBILITY, "updateAllDrawn allDrawn=" + this.allDrawn + " interesting=" + this.mNumInterestingWindows + " drawn=" + this.mNumDrawnWindows + " relaunchCount=" + this.mPendingRelaunchCount + " for " + this);
        if (!this.allDrawn) {
            int numInteresting = this.mNumInterestingWindows;
            if (numInteresting > 0 && allDrawnStatesConsidered() && this.mNumDrawnWindows >= numInteresting && !isRelaunching()) {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG, "allDrawn: " + this + " interesting=" + numInteresting + " drawn=" + this.mNumDrawnWindows);
                }
                if (this.appComponentName != null && this.appComponentName.contains("splitscreen.SplitScreenAppActivity")) {
                    this.mService.mShouldShowWallpaper = false;
                }
                this.allDrawn = true;
                if (this.mDisplayContent != null) {
                    this.mDisplayContent.setLayoutNeeded();
                }
                this.mService.mH.obtainMessage(32, this.token).sendToTarget();
                TaskStack pinnedStack = this.mDisplayContent.getPinnedStack();
                if (pinnedStack != null) {
                    pinnedStack.onAllWindowsDrawn();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateDrawnWindowStates(WindowState w) {
        w.setDrawnStateEvaluated(true);
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW_VERBOSE && w == this.startingWindow) {
            Slog.d(TAG, "updateWindows: starting " + w + " isOnScreen=" + w.isOnScreen() + " allDrawn=" + this.allDrawn + " freezingScreen=" + this.mFreezingScreen);
        }
        if (this.allDrawn && !this.mFreezingScreen) {
            return false;
        }
        if (this.mLastTransactionSequence != ((long) this.mService.mTransactionSequence)) {
            this.mLastTransactionSequence = (long) this.mService.mTransactionSequence;
            this.mNumDrawnWindows = 0;
            this.startingDisplayed = false;
            this.mNumInterestingWindows = findMainWindow(false) != null ? 1 : 0;
        }
        WindowStateAnimator winAnimator = w.mWinAnimator;
        boolean isInterestingAndDrawn = false;
        if (!this.allDrawn && w.mightAffectAllDrawn()) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Eval win " + w + ": isDrawn=" + w.isDrawnLw() + ", isAnimationSet=" + isSelfAnimating());
                if (!w.isDrawnLw()) {
                    Slog.v(TAG, "Not displayed: s=" + winAnimator.mSurfaceController + " pv=" + w.mPolicyVisibility + " mDrawState=" + winAnimator.drawStateToString() + " ph=" + w.isParentWindowHidden() + " th=" + this.hiddenRequested + " a=" + isSelfAnimating());
                }
            }
            if (w != this.startingWindow) {
                if (w.isInteresting()) {
                    if (findMainWindow(false) != w) {
                        this.mNumInterestingWindows++;
                    }
                    if (w.isDrawnLw()) {
                        this.mNumDrawnWindows++;
                        if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                            Slog.v(TAG, "tokenMayBeDrawn: " + this + " w=" + w + " numInteresting=" + this.mNumInterestingWindows + " freezingScreen=" + this.mFreezingScreen + " mAppFreezing=" + w.mAppFreezing);
                        }
                        isInterestingAndDrawn = true;
                    }
                }
            } else if (w.isDrawnLw()) {
                if (getController() != null) {
                    getController().reportStartingWindowDrawn();
                }
                this.startingDisplayed = true;
            }
        }
        return isInterestingAndDrawn;
    }

    /* access modifiers changed from: package-private */
    public void layoutLetterbox(WindowState winHint) {
        WindowState w = findMainWindow();
        if (w != null && (winHint == null || w == winHint)) {
            boolean needsLetterbox = true;
            boolean surfaceReady = w.isDrawnLw() || w.mWinAnimator.mSurfaceDestroyDeferred || w.isDragResizeChanged();
            if (!w.isLetterboxedAppWindow() || !fillsParent() || !surfaceReady) {
                needsLetterbox = false;
            }
            if (needsLetterbox) {
                if (this.mLetterbox == null) {
                    this.mLetterbox = new Letterbox(new Supplier<SurfaceControl.Builder>() {
                        @RCUnownedThisRef
                        public SurfaceControl.Builder get() {
                            return AppWindowToken.this.makeChildSurface(null);
                        }
                    });
                }
                int lazyMode = this.mService.getLazyMode();
                if (lazyMode == 0) {
                    this.mLetterbox.layout(getParent().getBounds(), w.mFrame);
                } else {
                    this.mLetterbox.layout(updateLazyModeRect(lazyMode, getParent().getBounds()), updateLazyModeRect(lazyMode, w.mFrame));
                }
            } else if (this.mLetterbox != null) {
                this.mLetterbox.hide();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateLetterboxSurface(WindowState winHint) {
        WindowState w = findMainWindow();
        if (w == winHint || winHint == null || w == null) {
            layoutLetterbox(winHint);
            if (this.mLetterbox != null && this.mLetterbox.needsApplySurfaceChanges()) {
                this.mLetterbox.applySurfaceChanges(this.mPendingTransaction);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (!this.mIsExiting || waitingForReplacement()) {
            return forAllWindowsUnchecked(callback, traverseTopToBottom);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean forAllWindowsUnchecked(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        return super.forAllWindows(callback, traverseTopToBottom);
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken asAppWindowToken() {
        return this;
    }

    /* access modifiers changed from: package-private */
    public boolean fillsParent() {
        return this.mFillsParent;
    }

    /* access modifiers changed from: package-private */
    public void setFillsParent(boolean fillsParent) {
        this.mFillsParent = fillsParent;
    }

    /* access modifiers changed from: package-private */
    public boolean containsDismissKeyguardWindow() {
        if (isRelaunching()) {
            return this.mLastContainsDismissKeyguardWindow;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if ((((WindowState) this.mChildren.get(i)).mAttrs.flags & DumpState.DUMP_CHANGES) != 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean containsShowWhenLockedWindow() {
        if (isRelaunching()) {
            return this.mLastContainsShowWhenLockedWindow;
        }
        if (this.mChildren.size() != 0 || !this.mService.mPolicy.isKeyguardOccluded()) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                if ((((WindowState) this.mChildren.get(i)).mAttrs.flags & DumpState.DUMP_FROZEN) != 0) {
                    return true;
                }
            }
            return false;
        }
        Slog.w(TAG, "Keyguard is occluded and there is no window in " + this);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void checkKeyguardFlagsChanged() {
        boolean containsDismissKeyguard = containsDismissKeyguardWindow();
        boolean containsShowWhenLocked = containsShowWhenLockedWindow();
        if (!(containsDismissKeyguard == this.mLastContainsDismissKeyguardWindow && containsShowWhenLocked == this.mLastContainsShowWhenLockedWindow)) {
            this.mService.notifyKeyguardFlagsChanged(null);
        }
        this.mLastContainsDismissKeyguardWindow = containsDismissKeyguard;
        this.mLastContainsShowWhenLockedWindow = containsShowWhenLocked;
    }

    /* access modifiers changed from: package-private */
    public WindowState getImeTargetBelowWindow(WindowState w) {
        int index = this.mChildren.indexOf(w);
        if (index > 0) {
            WindowState target = (WindowState) this.mChildren.get(index - 1);
            if (target.canBeImeTarget()) {
                return target;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getLowestAnimLayer() {
        for (int i = 0; i < this.mChildren.size(); i++) {
            WindowState w = (WindowState) this.mChildren.get(i);
            if (!w.mRemoved) {
                return w.mWinAnimator.mAnimLayer;
            }
        }
        return HwBootFail.STAGE_BOOT_SUCCESS;
    }

    /* access modifiers changed from: package-private */
    public WindowState getHighestAnimLayerWindow(WindowState currentTarget) {
        WindowState candidate = null;
        for (int i = this.mChildren.indexOf(currentTarget); i >= 0; i--) {
            WindowState w = (WindowState) this.mChildren.get(i);
            if (!w.mRemoved && (candidate == null || w.mWinAnimator.mAnimLayer > candidate.mWinAnimator.mAnimLayer)) {
                candidate = w;
            }
        }
        return candidate;
    }

    /* access modifiers changed from: package-private */
    public void setDisablePreviewScreenshots(boolean disable) {
        this.mDisablePreviewScreenshots = disable;
    }

    /* access modifiers changed from: package-private */
    public void setCanTurnScreenOn(boolean canTurnScreenOn) {
        this.mCanTurnScreenOn = canTurnScreenOn;
    }

    /* access modifiers changed from: package-private */
    public boolean canTurnScreenOn() {
        return this.mCanTurnScreenOn;
    }

    static /* synthetic */ boolean lambda$shouldUseAppThemeSnapshot$0(WindowState w) {
        return (w.mAttrs.flags & 8192) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldUseAppThemeSnapshot() {
        return this.mDisablePreviewScreenshots || forAllWindows($$Lambda$AppWindowToken$2UD7NFLKmEj_oClH71T4_KXv7aU.INSTANCE, true);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl getAppAnimationLayer() {
        int i;
        if (isActivityTypeHome()) {
            i = 2;
        } else if (needsZBoost()) {
            i = 1;
        } else {
            i = 0;
        }
        return getAppAnimationLayer(i);
    }

    public SurfaceControl getAnimationLeashParent() {
        if (!inPinnedWindowingMode()) {
            return getAppAnimationLayer();
        }
        return getStack().getSurfaceControl();
    }

    private boolean shouldAnimate(int transit) {
        boolean isSplitScreenPrimary = getWindowingMode() == 3;
        boolean allowSplitScreenPrimaryAnimation = transit != 13;
        if (!isSplitScreenPrimary || allowSplitScreenPrimaryAnimation) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean applyAnimationLocked(WindowManager.LayoutParams lp, int transit, boolean enter, boolean isVoiceInteraction) {
        AnimationAdapter adapter;
        int i = transit;
        if (this.mService.mDisableTransitionAnimation || !shouldAnimate(i)) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "applyAnimation: transition animation is disabled or skipped. atoken=" + this);
            }
            cancelAnimation();
            return false;
        } else if (!toString().contains("com.eg.android.AlipayGphone/.AlipayLogin") || this.mService.mClosingApps.size() <= 0 || !this.mService.mClosingApps.toString().contains("com.eg.android.AlipayGphone/.ResultActivity")) {
            Trace.traceBegin(32, "AWT#applyAnimationLocked");
            if (okToAnimate()) {
                TaskStack stack = getStack();
                this.mTmpPoint.set(0, 0);
                this.mTmpRect.setEmpty();
                if (stack != null) {
                    stack.getRelativePosition(this.mTmpPoint);
                    stack.getBounds(this.mTmpRect);
                    if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(getTask().getDisplayContent().mDisplayId)) {
                        WindowState win = findMainWindow();
                        if (win != null) {
                            this.mTmpRect.set(win.mFrame);
                            this.mTmpPoint.set(this.mTmpRect.left, this.mTmpRect.top);
                        }
                    }
                    if (this.mService.getLazyMode() != 0 && inMultiWindowMode()) {
                        this.mTmpPoint.set(stack.mLastSurfacePosition.x, stack.mLastSurfacePosition.y);
                    }
                    if (HwFoldScreenState.isFoldScreenDevice() && this.mService.isInSubFoldScaleMode() && inMultiWindowMode()) {
                        this.mTmpPoint.set(stack.mLastSurfacePosition.x, stack.mLastSurfacePosition.y);
                    }
                    if (stack.getTopChild() != null && HwFreeFormUtils.isFreeFormEnable() && inFreeformWindowingMode()) {
                        Rect taskBounds = ((Task) stack.getTopChild()).getBounds();
                        this.mTmpPoint.set(taskBounds.left, taskBounds.top);
                    }
                    this.mTmpRect.offsetTo(0, 0);
                }
                if (this.mService.mAppTransition.getRemoteAnimationController() == null || this.mSurfaceAnimator.isAnimationStartDelayed()) {
                    Animation a = loadAnimation(lp, transit, enter, isVoiceInteraction);
                    if (a != null) {
                        WindowAnimationSpec windowAnimationSpec = new WindowAnimationSpec(a, this.mTmpPoint, this.mTmpRect, this.mService.mAppTransition.canSkipFirstFrame(), this.mService.mAppTransition.getAppStackClipMode(), true);
                        AnimationAdapter adapter2 = new LocalAnimationAdapter(windowAnimationSpec, this.mService.mSurfaceAnimationRunner);
                        if (a.getZAdjustment() == 1) {
                            this.mNeedsZBoost = true;
                        }
                        this.mTransit = i;
                        this.mTransitFlags = this.mService.mAppTransition.getTransitFlags();
                        adapter = adapter2;
                    } else {
                        adapter = null;
                    }
                } else {
                    adapter = this.mService.mAppTransition.getRemoteAnimationController().createAnimationAdapter(this, this.mTmpPoint, this.mTmpRect);
                }
                if (adapter != null) {
                    startAnimation(getPendingTransaction(), adapter, true ^ isVisible());
                    if (adapter.getShowWallpaper()) {
                        this.mDisplayContent.pendingLayoutChanges |= 4;
                    }
                }
            } else {
                cancelAnimation();
            }
            Trace.traceEnd(32);
            return isReallyAnimating();
        } else {
            cancelAnimation();
            return false;
        }
    }

    private Animation loadAnimation(WindowManager.LayoutParams lp, int transit, boolean enter, boolean isVoiceInteraction) {
        Rect surfaceInsets;
        Rect stableInsets;
        WindowManager.LayoutParams layoutParams = lp;
        int i = transit;
        DisplayContent displayContent = getTask().getDisplayContent();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        int width = displayInfo.appWidth;
        int height = displayInfo.appHeight;
        if (!this.mService.isDisplayOkForAnimation(width, height, i, this)) {
            return null;
        }
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v(TAG, "applyAnimation: atoken=" + this);
        }
        WindowState win = findMainWindow();
        if (win != null && win.toString().contains("splitscreen.SplitScreenAppActivity") && i == 15) {
            i = 9;
        }
        int transit2 = i;
        boolean z = false;
        Rect frame = new Rect(0, 0, width, height);
        Rect displayFrame = new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        Rect insets = new Rect();
        Rect stableInsets2 = new Rect();
        boolean freeform = win != null && win.inFreeformWindowingMode();
        if (win != null && win.inCoordinationSecondaryWindowingMode()) {
            z = true;
        }
        boolean inSecondaryCoordination = z;
        if (enter || !inSecondaryCoordination) {
            if (win != null) {
                if (freeform || (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayContent.mDisplayId))) {
                    frame.set(win.mFrame);
                } else if (win.isLetterboxedAppWindow()) {
                    frame.set(getTask().getBounds());
                } else if (win.isDockedResizing()) {
                    frame.set(getTask().getParent().getBounds());
                } else {
                    frame.set(win.mContainingFrame);
                }
                Rect surfaceInsets2 = win.getAttrs().surfaceInsets;
                insets.set(win.mContentInsets);
                stableInsets2.set(win.mStableInsets);
                surfaceInsets = surfaceInsets2;
            } else {
                surfaceInsets = null;
            }
            boolean enter2 = this.mLaunchTaskBehind ? false : enter;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                StringBuilder sb = new StringBuilder();
                stableInsets = stableInsets2;
                sb.append("Loading animation for app transition. transit=");
                sb.append(AppTransition.appTransitionToString(transit2));
                sb.append(" enter=");
                sb.append(enter2);
                sb.append(" frame=");
                sb.append(frame);
                sb.append(" insets=");
                sb.append(insets);
                sb.append(" surfaceInsets=");
                sb.append(surfaceInsets);
                Slog.d(TAG, sb.toString());
            } else {
                stableInsets = stableInsets2;
            }
            Configuration displayConfig = displayContent.getConfiguration();
            boolean enter3 = enter2;
            Configuration configuration = displayConfig;
            Rect frame2 = frame;
            int transit3 = transit2;
            WindowState win2 = win;
            int height2 = height;
            int width2 = width;
            DisplayInfo displayInfo2 = displayInfo;
            DisplayContent displayContent2 = displayContent;
            Animation a = this.mService.mAppTransition.loadAnimation(layoutParams, transit2, enter3, displayConfig.uiMode, displayConfig.orientation, frame, displayFrame, insets, surfaceInsets, stableInsets, isVoiceInteraction, freeform, getTask().mTaskId);
            if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayContent2.mDisplayId)) {
                WindowManager.LayoutParams layoutParams2 = lp;
                if (freeform && HwFreeFormUtils.isFreeFormEnable()) {
                    a = this.mService.mAppTransition.loadAnimationRes(layoutParams2, enter3 ? 34209891 : 34209892);
                    a.setDuration(50);
                }
            } else {
                DisplayContent displayContent3 = displayContent2;
                a = this.mService.mAppTransition.loadAnimationRes(lp, enter3 ? 17432576 : 17432577);
                a.setDuration(300);
            }
            Rect frame3 = frame2;
            Animation a2 = tryToOverrideAppExitToLancherAnimation(a, transit3, frame3);
            if (a2 != null) {
                int containingWidth = frame3.width();
                int containingHeight = frame3.height();
                if (this.mService.getLazyMode() != 0) {
                    WindowManagerService windowManagerService = this.mService;
                    containingWidth = (int) (((float) containingWidth) * 0.75f);
                    WindowManagerService windowManagerService2 = this.mService;
                    containingHeight = (int) (((float) containingHeight) * 0.75f);
                }
                a2.initialize(containingWidth, containingHeight, width2, height2);
                WindowState win3 = win2;
                if (win3 == null || (!win3.toString().contains("com.android.contacts.activities.PeopleActivity") && !win3.toString().contains("com.android.contacts.activities.DialtactsActivity"))) {
                    a2.scaleCurrentDuration(this.mService.getTransitionAnimationScaleLocked());
                } else {
                    a2.scaleCurrentDuration(this.mService.getTransitionAnimationScaleLocked() * 0.7f);
                }
            } else {
                int i2 = height2;
                int i3 = width2;
            }
            return a2;
        }
        Slog.v(TAG, "skip applyAnimation: atoken=" + this);
        return null;
    }

    public boolean shouldDeferAnimationFinish(Runnable endDeferFinishCallback) {
        return this.mAnimatingAppWindowTokenRegistry != null && this.mAnimatingAppWindowTokenRegistry.notifyAboutToFinish(this, endDeferFinishCallback);
    }

    public void onAnimationLeashDestroyed(SurfaceControl.Transaction t) {
        super.onAnimationLeashDestroyed(t);
        if (this.mAnimatingAppWindowTokenRegistry != null) {
            this.mAnimatingAppWindowTokenRegistry.notifyFinished(this);
        }
    }

    /* access modifiers changed from: protected */
    public void setLayer(SurfaceControl.Transaction t, int layer) {
        if (!this.mSurfaceAnimator.hasLeash()) {
            t.setLayer(this.mSurfaceControl, layer);
        }
    }

    /* access modifiers changed from: protected */
    public void setRelativeLayer(SurfaceControl.Transaction t, SurfaceControl relativeTo, int layer) {
        if (!this.mSurfaceAnimator.hasLeash()) {
            t.setRelativeLayer(this.mSurfaceControl, relativeTo, layer);
        }
    }

    /* access modifiers changed from: protected */
    public void reparentSurfaceControl(SurfaceControl.Transaction t, SurfaceControl newParent) {
        if (!this.mSurfaceAnimator.hasLeash()) {
            t.reparent(this.mSurfaceControl, newParent.getHandle());
        }
    }

    public void onAnimationLeashCreated(SurfaceControl.Transaction t, SurfaceControl leash) {
        int layer;
        if (!inPinnedWindowingMode()) {
            layer = getPrefixOrderIndex();
        } else {
            layer = getParent().getPrefixOrderIndex();
        }
        if (this.mNeedsZBoost) {
            layer += Z_BOOST_BASE;
        }
        leash.setLayer(layer);
        getDisplayContent().assignStackOrdering();
        if (this.mAnimatingAppWindowTokenRegistry != null) {
            this.mAnimatingAppWindowTokenRegistry.notifyStarting(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void showAllWindowsLocked() {
        forAllWindows((Consumer<WindowState>) $$Lambda$AppWindowToken$CktbwxOBKAFpM_9Vy73X9oy5Nrg.INSTANCE, false);
    }

    static /* synthetic */ void lambda$showAllWindowsLocked$1(WindowState windowState) {
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG, "performing show on: " + windowState);
        }
        windowState.performShowLocked();
    }

    /* access modifiers changed from: protected */
    public void onAnimationFinished() {
        super.onAnimationFinished();
        this.mTransit = -1;
        boolean z = false;
        this.mTransitFlags = 0;
        this.mNeedsZBoost = false;
        setAppLayoutChanges(12, "AppWindowToken");
        clearThumbnail();
        if (isHidden() && this.hiddenRequested) {
            z = true;
        }
        setClientHidden(z);
        if (this.mService.mInputMethodTarget != null && this.mService.mInputMethodTarget.mAppToken == this) {
            getDisplayContent().computeImeTarget(true);
        }
        new ArrayList<>(this.mChildren).forEach($$Lambda$01bPtngJg5AqEoOWfW3rWfV7MH4.INSTANCE);
        this.mService.mAppTransition.notifyAppTransitionFinishedLocked(this.token);
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    public boolean isAppAnimating() {
        return isSelfAnimating();
    }

    /* access modifiers changed from: package-private */
    public boolean isSelfAnimating() {
        return isWaitingForTransitionStart() || isReallyAnimating();
    }

    private boolean isReallyAnimating() {
        return super.isSelfAnimating();
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimation() {
        super.cancelAnimation();
        clearThumbnail();
    }

    /* access modifiers changed from: package-private */
    public boolean isWaitingForTransitionStart() {
        return this.mService.mAppTransition.isTransitionSet() && (this.mService.mOpeningApps.contains(this) || this.mService.mClosingApps.contains(this));
    }

    public int getTransit() {
        return this.mTransit;
    }

    /* access modifiers changed from: package-private */
    public int getTransitFlags() {
        return this.mTransitFlags;
    }

    /* access modifiers changed from: package-private */
    public void attachThumbnailAnimation() {
        if (isReallyAnimating()) {
            int taskId = getTask().mTaskId;
            GraphicBuffer thumbnailHeader = this.mService.mAppTransition.getAppTransitionThumbnailHeader(taskId);
            if (thumbnailHeader == null) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.d(TAG, "No thumbnail header bitmap for: " + taskId);
                }
                return;
            }
            clearThumbnail();
            this.mThumbnail = new AppWindowThumbnail(getPendingTransaction(), this, thumbnailHeader);
            this.mThumbnail.startAnimation(getPendingTransaction(), loadThumbnailAnimation(thumbnailHeader));
        }
    }

    /* access modifiers changed from: package-private */
    public void attachCrossProfileAppsThumbnailAnimation() {
        int thumbnailDrawableRes;
        if (isReallyAnimating()) {
            clearThumbnail();
            WindowState win = findMainWindow();
            if (win != null) {
                Rect frame = win.mFrame;
                if (getTask().mUserId == this.mService.mCurrentUserId) {
                    thumbnailDrawableRes = 17302260;
                } else {
                    thumbnailDrawableRes = 17302337;
                }
                GraphicBuffer thumbnail = this.mService.mAppTransition.createCrossProfileAppsThumbnail(thumbnailDrawableRes, frame);
                if (thumbnail != null) {
                    this.mThumbnail = new AppWindowThumbnail(getPendingTransaction(), this, thumbnail);
                    this.mThumbnail.startAnimation(getPendingTransaction(), this.mService.mAppTransition.createCrossProfileAppsThumbnailAnimationLocked(win.mFrame), new Point(frame.left, frame.top));
                }
            }
        }
    }

    private Animation loadThumbnailAnimation(GraphicBuffer thumbnailHeader) {
        Rect rect;
        DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
        WindowState win = findMainWindow();
        if (win != null) {
            rect = win.getContentFrameLw();
        } else {
            rect = new Rect(0, 0, displayInfo.appWidth, displayInfo.appHeight);
        }
        Rect appRect = rect;
        Rect insets = win != null ? win.mContentInsets : null;
        Configuration displayConfig = this.mDisplayContent.getConfiguration();
        return this.mService.mAppTransition.createThumbnailAspectScaleAnimationLocked(appRect, insets, thumbnailHeader, getTask().mTaskId, displayConfig.uiMode, displayConfig.orientation);
    }

    private void clearThumbnail() {
        if (this.mThumbnail != null) {
            this.mThumbnail.destroy();
            this.mThumbnail = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void registerRemoteAnimations(RemoteAnimationDefinition definition) {
        this.mRemoteAnimationDefinition = definition;
    }

    /* access modifiers changed from: package-private */
    public RemoteAnimationDefinition getRemoteAnimationDefinition() {
        return this.mRemoteAnimationDefinition;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        String str;
        super.dump(pw, prefix, dumpAll);
        if (this.appToken != null) {
            pw.println(prefix + "app=true mVoiceInteraction=" + this.mVoiceInteraction);
        }
        pw.print(prefix);
        pw.print("task=");
        pw.println(getTask());
        pw.print(prefix);
        pw.print(" mFillsParent=");
        pw.print(this.mFillsParent);
        pw.print(" mOrientation=");
        pw.println(this.mOrientation);
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("hiddenRequested=");
        sb.append(this.hiddenRequested);
        sb.append(" mClientHidden=");
        sb.append(this.mClientHidden);
        if (this.mDeferHidingClient) {
            str = " mDeferHidingClient=" + this.mDeferHidingClient;
        } else {
            str = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        sb.append(str);
        sb.append(" reportedDrawn=");
        sb.append(this.reportedDrawn);
        sb.append(" reportedVisible=");
        sb.append(this.reportedVisible);
        pw.println(sb.toString());
        if (this.paused) {
            pw.print(prefix);
            pw.print("paused=");
            pw.println(this.paused);
        }
        if (this.mAppStopped) {
            pw.print(prefix);
            pw.print("mAppStopped=");
            pw.println(this.mAppStopped);
        }
        if (this.mNumInterestingWindows != 0 || this.mNumDrawnWindows != 0 || this.allDrawn || this.mLastAllDrawn) {
            pw.print(prefix);
            pw.print("mNumInterestingWindows=");
            pw.print(this.mNumInterestingWindows);
            pw.print(" mNumDrawnWindows=");
            pw.print(this.mNumDrawnWindows);
            pw.print(" inPendingTransaction=");
            pw.print(this.inPendingTransaction);
            pw.print(" allDrawn=");
            pw.print(this.allDrawn);
            pw.print(" lastAllDrawn=");
            pw.print(this.mLastAllDrawn);
            pw.println(")");
        }
        if (this.inPendingTransaction) {
            pw.print(prefix);
            pw.print("inPendingTransaction=");
            pw.println(this.inPendingTransaction);
        }
        if (this.startingData != null || this.removed || this.firstWindowDrawn || this.mIsExiting) {
            pw.print(prefix);
            pw.print("startingData=");
            pw.print(this.startingData);
            pw.print(" removed=");
            pw.print(this.removed);
            pw.print(" firstWindowDrawn=");
            pw.print(this.firstWindowDrawn);
            pw.print(" mIsExiting=");
            pw.println(this.mIsExiting);
        }
        if (this.startingWindow != null || this.startingSurface != null || this.startingDisplayed || this.startingMoved || this.mHiddenSetFromTransferredStartingWindow) {
            pw.print(prefix);
            pw.print("startingWindow=");
            pw.print(this.startingWindow);
            pw.print(" startingSurface=");
            pw.print(this.startingSurface);
            pw.print(" startingDisplayed=");
            pw.print(this.startingDisplayed);
            pw.print(" startingMoved=");
            pw.print(this.startingMoved);
            pw.println(" mHiddenSetFromTransferredStartingWindow=" + this.mHiddenSetFromTransferredStartingWindow);
        }
        if (!this.mFrozenBounds.isEmpty()) {
            pw.print(prefix);
            pw.print("mFrozenBounds=");
            pw.println(this.mFrozenBounds);
            pw.print(prefix);
            pw.print("mFrozenMergedConfig=");
            pw.println(this.mFrozenMergedConfig);
        }
        if (this.mPendingRelaunchCount != 0) {
            pw.print(prefix);
            pw.print("mPendingRelaunchCount=");
            pw.println(this.mPendingRelaunchCount);
        }
        if (getController() != null) {
            pw.print(prefix);
            pw.print("controller=");
            pw.println(getController());
        }
        if (this.mRemovingFromDisplay) {
            pw.println(prefix + "mRemovingFromDisplay=" + this.mRemovingFromDisplay);
        }
    }

    /* access modifiers changed from: package-private */
    public void setHidden(boolean hidden) {
        super.setHidden(hidden);
        if (hidden) {
            this.mDisplayContent.mPinnedStackControllerLocked.resetReentrySnapFraction(this);
        }
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    public void prepareSurfaces() {
        boolean show = !isHidden() || super.isSelfAnimating();
        if (show && !this.mLastSurfaceShowing) {
            this.mPendingTransaction.show(this.mSurfaceControl);
        } else if (!show && this.mLastSurfaceShowing) {
            this.mPendingTransaction.hide(this.mSurfaceControl);
        }
        if (this.mThumbnail != null) {
            this.mThumbnail.setShowing(this.mPendingTransaction, show);
        }
        this.mLastSurfaceShowing = show;
        super.prepareSurfaces();
    }

    /* access modifiers changed from: package-private */
    public boolean isSurfaceShowing() {
        return this.mLastSurfaceShowing;
    }

    /* access modifiers changed from: package-private */
    public boolean isFreezingScreen() {
        return this.mFreezingScreen;
    }

    /* access modifiers changed from: package-private */
    public boolean needsZBoost() {
        return this.mNeedsZBoost || super.needsZBoost();
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, boolean trim) {
        long token = proto.start(fieldId);
        writeNameToProto(proto, 1138166333441L);
        super.writeToProto(proto, 1146756268034L, trim);
        proto.write(1133871366147L, this.mLastSurfaceShowing);
        proto.write(1133871366148L, isWaitingForTransitionStart());
        proto.write(1133871366149L, isReallyAnimating());
        if (this.mThumbnail != null) {
            this.mThumbnail.writeToProto(proto, 1146756268038L);
        }
        proto.write(1133871366151L, this.mFillsParent);
        proto.write(1133871366152L, this.mAppStopped);
        proto.write(1133871366153L, this.hiddenRequested);
        proto.write(1133871366154L, this.mClientHidden);
        proto.write(1133871366155L, this.mDeferHidingClient);
        proto.write(1133871366156L, this.reportedDrawn);
        proto.write(1133871366157L, this.reportedVisible);
        proto.write(1120986464270L, this.mNumInterestingWindows);
        proto.write(1120986464271L, this.mNumDrawnWindows);
        proto.write(1133871366160L, this.allDrawn);
        proto.write(1133871366161L, this.mLastAllDrawn);
        proto.write(1133871366162L, this.removed);
        if (this.startingWindow != null) {
            this.startingWindow.writeIdentifierToProto(proto, 1146756268051L);
        }
        proto.write(1133871366164L, this.startingDisplayed);
        proto.write(1133871366165L, this.startingMoved);
        proto.write(1133871366166L, this.mHiddenSetFromTransferredStartingWindow);
        Iterator<Rect> it = this.mFrozenBounds.iterator();
        while (it.hasNext()) {
            it.next().writeToProto(proto, 2246267895831L);
        }
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public void writeNameToProto(ProtoOutputStream proto, long fieldId) {
        if (this.appToken != null) {
            try {
                proto.write(fieldId, this.appToken.getName());
            } catch (RemoteException e) {
                Slog.e(TAG, e.toString());
            }
        }
    }

    public String toString() {
        if (this.stringName == null) {
            this.stringName = "AppWindowToken{" + Integer.toHexString(System.identityHashCode(this)) + " token=" + this.token + '}';
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.stringName);
        sb.append(this.mIsExiting ? " mIsExiting=" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public Rect getLetterboxInsets() {
        if (this.mLetterbox != null) {
            return this.mLetterbox.getInsets();
        }
        return new Rect();
    }

    /* access modifiers changed from: package-private */
    public boolean isLetterboxOverlappingWith(Rect rect) {
        return this.mLetterbox != null && this.mLetterbox.isOverlappingWith(rect);
    }

    /* access modifiers changed from: package-private */
    public void setWillCloseOrEnterPip(boolean willCloseOrEnterPip) {
        this.mWillCloseOrEnterPip = willCloseOrEnterPip;
    }

    /* access modifiers changed from: package-private */
    public boolean isClosingOrEnteringPip() {
        return (isAnimating() && this.hiddenRequested) || this.mWillCloseOrEnterPip;
    }

    private boolean isAppExitToLauncher(AppWindowToken atoken, int transit) {
        if (atoken == null) {
            Slog.w(TAG, "app exit to launcher find no app window token!");
            return false;
        }
        WindowState window = atoken.findMainWindow();
        AppWindowToken topOpeningApp = this.mService.getTopOpeningApp();
        Flog.i(310, "is app exit to launcher info: transit = " + transit + ", app = " + atoken + ", window = " + window + "mClosingApps = " + this.mService.mClosingApps + ", topOpeningApp = " + topOpeningApp + ", mExitIconBitmap = " + this.mService.mExitIconBitmap + ", mExitIconHeight = " + this.mService.mExitIconHeight + ", mExitIconWidth = " + this.mService.mExitIconWidth);
        if (window != null && transit == 13 && this.mService.mClosingApps != null && this.mService.mClosingApps.contains(atoken) && !atoken.toString().contains("com.android.stk/.StkDialogActivity") && topOpeningApp != null && this.mService.mExitIconBitmap != null && this.mService.mExitIconHeight > 0 && this.mService.mExitIconWidth > 0) {
            boolean isOpeningUniLauncherCmpName = topOpeningApp.toString().contains("com.huawei.android.launcher/.unihome.UniHomeLauncher");
            boolean isOpeningDrawerLauncherCmpName = topOpeningApp.toString().contains("com.huawei.android.launcher/.drawer.DrawerLauncher");
            boolean isOpeningNewSimpleLauncherCmpName = topOpeningApp.toString().contains("com.huawei.android.launcher/.newsimpleui.NewSimpleLauncher");
            if (isOpeningUniLauncherCmpName || isOpeningDrawerLauncherCmpName || isOpeningNewSimpleLauncherCmpName) {
                if (window.mAttrs == null || (window.mAttrs.flags & DumpState.DUMP_FROZEN) != 524288 || (window.mAttrs.flags & DumpState.DUMP_CHANGES) != 4194304) {
                    return true;
                }
                Slog.d(TAG, "app to launcher window flag = " + window.mAttrs.flags);
                return false;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0037, code lost:
        if (r1.contains("com.huawei.android.launcher/.newsimpleui.NewSimpleLauncher") != false) goto L_0x0039;
     */
    private boolean isLauncherOpen(AppWindowToken atoken, int transit) {
        if (atoken == null) {
            Slog.w(TAG, "find no atoken when check is Launcher open");
            return false;
        }
        if (transit == 13) {
            String appWindowToken = atoken.toString();
            WindowManagerService windowManagerService = this.mService;
            if (!appWindowToken.contains("com.huawei.android.launcher/.unihome.UniHomeLauncher")) {
                String appWindowToken2 = atoken.toString();
                WindowManagerService windowManagerService2 = this.mService;
                if (!appWindowToken2.contains("com.huawei.android.launcher/.drawer.DrawerLauncher")) {
                    String appWindowToken3 = atoken.toString();
                    WindowManagerService windowManagerService3 = this.mService;
                }
            }
            if (this.mService.mClosingApps != null && this.mService.mClosingApps.size() > 0 && this.mService.mOpeningApps != null && this.mService.mOpeningApps.contains(atoken)) {
                Slog.i(TAG, this.mService.mClosingApps + " is closing and " + this.mService.mOpeningApps + "is opening");
                return true;
            }
        }
        return false;
    }

    private Animation tryToOverrideAppExitToLancherAnimation(Animation a, int transit, Rect frame) {
        WindowManagerService windowManagerService = this.mService;
        if (!WindowManagerService.HW_SUPPORT_LAUNCHER_EXIT_ANIM) {
            return a;
        }
        if (isAppExitToLauncher(this, transit) && frame != null) {
            Animation appExitToIconAnimation = this.mService.mAppTransition.createAppExitToIconAnimation(this, frame.height(), this.mService.mExitIconWidth, this.mService.mExitIconHeight, this.mService.mExitPivotX, this.mService.mExitPivotY, this.mService.mExitIconBitmap);
            if (appExitToIconAnimation != null) {
                return appExitToIconAnimation;
            }
            return a;
        } else if (!isLauncherOpen(this, transit) || frame == null || this.mService.mExitFlag == -2) {
            return a;
        } else {
            Animation launcherEnterAnimation = this.mService.mAppTransition.createLauncherEnterAnimation(this, frame.height(), this.mService.mExitIconWidth, this.mService.mExitIconHeight, this.mService.mExitPivotX, this.mService.mExitPivotY);
            if (launcherEnterAnimation != null) {
                return launcherEnterAnimation;
            }
            return a;
        }
    }

    public int getHwGestureNavOptions() {
        return this.mHwGestureNavOptions;
    }

    private Rect updateLazyModeRect(int lazyMode, Rect defaultFrame) {
        Point lazyModePoint = this.mService.updateLazyModePoint(lazyMode, new Point(defaultFrame.left, defaultFrame.top));
        Rect lazyModeFrame = new Rect();
        lazyModeFrame.left = lazyModePoint.x;
        lazyModeFrame.bottom = (int) (((float) lazyModePoint.y) + (((float) defaultFrame.height()) * this.mService.mHwWMSEx.getLazyModeScale()));
        lazyModeFrame.top = lazyModePoint.y;
        lazyModeFrame.right = (int) (((float) lazyModePoint.x) + (((float) defaultFrame.width()) * this.mService.mHwWMSEx.getLazyModeScale()));
        return lazyModeFrame;
    }

    public void setShowWhenLocked(boolean showWhenLocked) {
        this.mShowWhenLocked = showWhenLocked;
    }
}
