package com.android.server.wm;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.IDockedStackListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DockedDividerUtils;
import com.android.server.LocalServices;
import com.android.server.os.HwBootFail;
import java.io.PrintWriter;
import java.util.function.Consumer;

public class DockedStackDividerController implements DimLayerUser {
    private static final float CLIP_REVEAL_MEET_EARLIEST = 0.6f;
    private static final float CLIP_REVEAL_MEET_FRACTION_MAX = 0.8f;
    private static final float CLIP_REVEAL_MEET_FRACTION_MIN = 0.4f;
    private static final float CLIP_REVEAL_MEET_LAST = 1.0f;
    private static final int DIVIDER_WIDTH_INACTIVE_DP = 4;
    private static final long IME_ADJUST_ANIM_DURATION = 280;
    private static final long IME_ADJUST_DRAWN_TIMEOUT = 200;
    private static final Interpolator IME_ADJUST_ENTRY_INTERPOLATOR = new PathInterpolator(0.2f, 0.0f, 0.1f, 1.0f);
    private static final String TAG = "WindowManager";
    private boolean mAdjustedForDivider;
    private boolean mAdjustedForIme;
    private boolean mAnimatingForIme;
    private boolean mAnimatingForMinimizedDockedStack;
    private long mAnimationDuration;
    private float mAnimationStart;
    private boolean mAnimationStartDelayed;
    private long mAnimationStartTime;
    private boolean mAnimationStarted;
    private float mAnimationTarget;
    private WindowState mDelayedImeWin;
    protected final DimLayer mDimLayer;
    private final DisplayContent mDisplayContent;
    private float mDividerAnimationStart;
    private float mDividerAnimationTarget;
    private int mDividerInsets;
    private int mDividerWindowWidth;
    private int mDividerWindowWidthInactive;
    private final RemoteCallbackList<IDockedStackListener> mDockedStackListeners = new RemoteCallbackList();
    private final Handler mHandler = new Handler();
    private int mImeHeight;
    private boolean mImeHideRequested;
    float mLastAnimationProgress;
    float mLastDividerProgress;
    private final Rect mLastRect = new Rect();
    private boolean mLastVisibility = false;
    private float mMaximizeMeetFraction;
    private boolean mMinimizedDock;
    private final Interpolator mMinimizedDockInterpolator;
    private boolean mResizing;
    private int mRotation = 0;
    private final WindowManagerService mService;
    private final DividerSnapAlgorithm[] mSnapAlgorithmForRotation = new DividerSnapAlgorithm[4];
    private int mTaskHeightInMinimizedMode;
    private final Rect mTmpRect = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private final Rect mTmpRect3 = new Rect();
    private final Rect mTouchRegion = new Rect();
    private WindowState mWindow;

    DockedStackDividerController(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
        Context context = service.mContext;
        this.mDimLayer = new DimLayer(displayContent.mService, this, displayContent.getDisplayId(), "DockedStackDim");
        this.mMinimizedDockInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        loadDimens();
    }

    int getSmallestWidthDpForBounds(Rect bounds) {
        DisplayInfo di = this.mDisplayContent.getDisplayInfo();
        int baseDisplayWidth = this.mDisplayContent.mBaseDisplayWidth;
        int baseDisplayHeight = this.mDisplayContent.mBaseDisplayHeight;
        int minWidth = HwBootFail.STAGE_BOOT_SUCCESS;
        int rotation = 0;
        while (rotation < 4) {
            int i;
            int i2;
            int orientation;
            this.mTmpRect.set(bounds);
            this.mDisplayContent.rotateBounds(di.rotation, rotation, this.mTmpRect);
            boolean rotated = rotation == 1 || rotation == 3;
            Rect rect = this.mTmpRect2;
            if (rotated) {
                i = baseDisplayHeight;
            } else {
                i = baseDisplayWidth;
            }
            if (rotated) {
                i2 = baseDisplayWidth;
            } else {
                i2 = baseDisplayHeight;
            }
            rect.set(0, 0, i, i2);
            if (this.mTmpRect2.width() <= this.mTmpRect2.height()) {
                orientation = 1;
            } else {
                orientation = 2;
            }
            int dockSide = TaskStack.getDockSideUnchecked(this.mTmpRect, this.mTmpRect2, orientation);
            DockedDividerUtils.calculateBoundsForPosition(this.mSnapAlgorithmForRotation[rotation].calculateNonDismissingSnapTarget(DockedDividerUtils.calculatePositionForBounds(this.mTmpRect, dockSide, getContentWidth())).position, dockSide, this.mTmpRect, this.mTmpRect2.width(), this.mTmpRect2.height(), getContentWidth());
            this.mService.mPolicy.getStableInsetsLw(rotation, this.mTmpRect2.width(), this.mTmpRect2.height(), this.mTmpRect3);
            this.mService.intersectDisplayInsetBounds(this.mTmpRect2, this.mTmpRect3, this.mTmpRect);
            minWidth = Math.min(this.mTmpRect.width(), minWidth);
            rotation++;
        }
        return (int) (((float) minWidth) / this.mDisplayContent.getDisplayMetrics().density);
    }

    void getHomeStackBoundsInDockedMode(Rect outBounds) {
        DisplayInfo di = this.mDisplayContent.getDisplayInfo();
        this.mService.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, this.mTmpRect);
        int dividerSize = this.mDividerWindowWidth - (this.mDividerInsets * 2);
        if (this.mDisplayContent.getConfiguration().orientation == 1) {
            outBounds.set(0, (this.mTaskHeightInMinimizedMode + dividerSize) + this.mTmpRect.top, di.logicalWidth, di.logicalHeight);
        } else {
            outBounds.set(((this.mTaskHeightInMinimizedMode + dividerSize) + this.mTmpRect.left) + this.mTmpRect.top, 0, di.logicalWidth, di.logicalHeight);
        }
    }

    boolean isHomeStackResizable() {
        boolean z = false;
        TaskStack homeStack = this.mDisplayContent.getHomeStack();
        if (homeStack == null) {
            return false;
        }
        Task homeTask = homeStack.findHomeTask();
        if (homeTask != null) {
            z = homeTask.isResizeable();
        }
        return z;
    }

    private void initSnapAlgorithmForRotations() {
        Configuration baseConfig = this.mDisplayContent.getConfiguration();
        Configuration config = new Configuration();
        int rotation = 0;
        while (rotation < 4) {
            int dw;
            int dh;
            boolean rotated = rotation == 1 || rotation == 3;
            if (rotated) {
                dw = this.mDisplayContent.mBaseDisplayHeight;
            } else {
                dw = this.mDisplayContent.mBaseDisplayWidth;
            }
            if (rotated) {
                dh = this.mDisplayContent.mBaseDisplayWidth;
            } else {
                dh = this.mDisplayContent.mBaseDisplayHeight;
            }
            this.mService.mPolicy.getStableInsetsLw(rotation, dw, dh, this.mTmpRect);
            config.unset();
            config.orientation = dw <= dh ? 1 : 2;
            int displayId = this.mDisplayContent.getDisplayId();
            int appWidth = this.mService.mPolicy.getNonDecorDisplayWidth(dw, dh, rotation, baseConfig.uiMode, displayId);
            int appHeight = this.mService.mPolicy.getNonDecorDisplayHeight(dw, dh, rotation, baseConfig.uiMode, displayId);
            this.mService.mPolicy.getNonDecorInsetsLw(rotation, dw, dh, this.mTmpRect);
            int leftInset = this.mTmpRect.left;
            int topInset = this.mTmpRect.top;
            config.setAppBounds(leftInset, topInset, leftInset + appWidth, topInset + appHeight);
            config.screenWidthDp = (int) (((float) this.mService.mPolicy.getConfigDisplayWidth(dw, dh, rotation, baseConfig.uiMode, displayId)) / this.mDisplayContent.getDisplayMetrics().density);
            config.screenHeightDp = (int) (((float) this.mService.mPolicy.getConfigDisplayHeight(dw, dh, rotation, baseConfig.uiMode, displayId)) / this.mDisplayContent.getDisplayMetrics().density);
            this.mSnapAlgorithmForRotation[rotation] = new DividerSnapAlgorithm(this.mService.mContext.createConfigurationContext(config).getResources(), dw, dh, getContentWidth(), config.orientation == 1, this.mTmpRect);
            rotation++;
        }
    }

    private void loadDimens() {
        Context context = this.mService.mContext;
        this.mDividerWindowWidth = context.getResources().getDimensionPixelSize(17105014);
        this.mDividerInsets = context.getResources().getDimensionPixelSize(17105013);
        this.mDividerWindowWidthInactive = WindowManagerService.dipToPixel(4, this.mDisplayContent.getDisplayMetrics());
        this.mTaskHeightInMinimizedMode = context.getResources().getDimensionPixelSize(17105240);
        initSnapAlgorithmForRotations();
    }

    void onConfigurationChanged() {
        loadDimens();
    }

    boolean isResizing() {
        return this.mResizing;
    }

    int getContentWidth() {
        return this.mDividerWindowWidth - (this.mDividerInsets * 2);
    }

    int getContentInsets() {
        return this.mDividerInsets;
    }

    int getContentWidthInactive() {
        return this.mDividerWindowWidthInactive;
    }

    void setResizing(boolean resizing) {
        if (this.mResizing != resizing) {
            this.mResizing = resizing;
            resetDragResizingChangeReported();
            if (!this.mResizing && this.mRotation != this.mDisplayContent.getDisplay().getRotation()) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        DockedStackDividerController.this.mService.updateRotation(false, false);
                    }
                });
            }
        }
    }

    void setTouchRegion(Rect touchRegion) {
        this.mTouchRegion.set(touchRegion);
    }

    void getTouchRegion(Rect outRegion) {
        outRegion.set(this.mTouchRegion);
        outRegion.offset(this.mWindow.getFrameLw().left, this.mWindow.getFrameLw().top);
    }

    private void resetDragResizingChangeReported() {
        this.mDisplayContent.forAllWindows((Consumer) new -$Lambda$HFbYE8vr4-NA3Y6wUONVd47OH54(), true);
    }

    void setWindow(WindowState window) {
        this.mWindow = window;
        reevaluateVisibility(false);
    }

    void reevaluateVisibility(boolean force) {
        if (this.mWindow != null) {
            boolean visible = this.mDisplayContent.getDockedStackIgnoringVisibility() != null;
            if (this.mLastVisibility != visible || (force ^ 1) == 0) {
                this.mLastVisibility = visible;
                notifyDockedDividerVisibilityChanged(visible);
                if (!visible) {
                    setResizeDimLayer(false, -1, 0.0f);
                }
            }
        }
    }

    private boolean wasVisible() {
        return this.mLastVisibility;
    }

    void setAdjustedForIme(boolean adjustedForIme, boolean adjustedForDivider, boolean animate, WindowState imeWin, int imeHeight) {
        if (this.mAdjustedForIme != adjustedForIme || ((adjustedForIme && this.mImeHeight != imeHeight) || this.mAdjustedForDivider != adjustedForDivider)) {
            if (!animate || (this.mAnimatingForMinimizedDockedStack ^ 1) == 0) {
                notifyAdjustedForImeChanged(!adjustedForIme ? adjustedForDivider : true, 0);
            } else {
                startImeAdjustAnimation(adjustedForIme, adjustedForDivider, imeWin);
            }
            this.mAdjustedForIme = adjustedForIme;
            this.mImeHeight = imeHeight;
            this.mAdjustedForDivider = adjustedForDivider;
        }
    }

    int getImeHeightAdjustedFor() {
        return this.mImeHeight;
    }

    void positionDockedStackedDivider(Rect frame) {
        TaskStack stack = this.mDisplayContent.getDockedStackLocked();
        if (stack == null) {
            frame.set(this.mLastRect);
            return;
        }
        stack.getDimBounds(this.mTmpRect);
        switch (stack.getDockSide()) {
            case 1:
                frame.set(this.mTmpRect.right - this.mDividerInsets, frame.top, (this.mTmpRect.right + frame.width()) - this.mDividerInsets, frame.bottom);
                break;
            case 2:
                frame.set(frame.left, this.mTmpRect.bottom - this.mDividerInsets, this.mTmpRect.right, (this.mTmpRect.bottom + frame.height()) - this.mDividerInsets);
                break;
            case 3:
                frame.set((this.mTmpRect.left - frame.width()) + this.mDividerInsets, frame.top, this.mTmpRect.left + this.mDividerInsets, frame.bottom);
                break;
            case 4:
                frame.set(frame.left, (this.mTmpRect.top - frame.height()) + this.mDividerInsets, frame.right, this.mTmpRect.top + this.mDividerInsets);
                break;
        }
        this.mLastRect.set(frame);
    }

    private void notifyDockedDividerVisibilityChanged(boolean visible) {
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    ((IDockedStackListener) this.mDockedStackListeners.getBroadcastItem(i)).onDividerVisibilityChanged(visible);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error delivering divider visibility changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    void notifyDockedStackExistsChanged(boolean exists) {
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    ((IDockedStackListener) this.mDockedStackListeners.getBroadcastItem(i)).onDockedStackExistsChanged(exists);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error delivering docked stack exists changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
        if (exists) {
            InputMethodManagerInternal inputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
            if (inputMethodManagerInternal != null) {
                inputMethodManagerInternal.hideCurrentInputMethod();
                this.mImeHideRequested = true;
            }
        }
        setMinimizedDockedStack(false, false);
    }

    void resetImeHideRequested() {
        this.mImeHideRequested = false;
    }

    boolean isImeHideRequested() {
        return this.mImeHideRequested;
    }

    private void notifyDockedStackMinimizedChanged(boolean minimizedDock, boolean animate, boolean isHomeStackResizable) {
        long animDuration = 0;
        if (animate) {
            long transitionDuration;
            TaskStack stack = this.mDisplayContent.getStackById(3);
            if (isAnimationMaximizing()) {
                transitionDuration = this.mService.mAppTransition.getLastClipRevealTransitionDuration();
            } else {
                transitionDuration = 250;
            }
            this.mAnimationDuration = (long) (((float) transitionDuration) * this.mService.getTransitionAnimationScaleLocked());
            this.mMaximizeMeetFraction = getClipRevealMeetFraction(stack);
            animDuration = (long) (((float) this.mAnimationDuration) * this.mMaximizeMeetFraction);
        }
        this.mService.mH.removeMessages(53);
        this.mService.mH.obtainMessage(53, minimizedDock ? 1 : 0, 0).sendToTarget();
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    ((IDockedStackListener) this.mDockedStackListeners.getBroadcastItem(i)).onDockedStackMinimizedChanged(minimizedDock, animDuration, isHomeStackResizable);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error delivering minimized dock changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    void notifyDockSideChanged(int newDockSide) {
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    ((IDockedStackListener) this.mDockedStackListeners.getBroadcastItem(i)).onDockSideChanged(newDockSide);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error delivering dock side changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    private void notifyAdjustedForImeChanged(boolean adjustedForIme, long animDuration) {
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    ((IDockedStackListener) this.mDockedStackListeners.getBroadcastItem(i)).onAdjustedForImeChanged(adjustedForIme, animDuration);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error delivering adjusted for ime changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    void registerDockedStackListener(IDockedStackListener listener) {
        boolean z;
        synchronized (this.mDockedStackListeners) {
            this.mDockedStackListeners.register(listener);
        }
        notifyDockedDividerVisibilityChanged(wasVisible());
        if (this.mDisplayContent.getDockedStackIgnoringVisibility() != null) {
            z = true;
        } else {
            z = false;
        }
        notifyDockedStackExistsChanged(z);
        notifyDockedStackMinimizedChanged(this.mMinimizedDock, false, isHomeStackResizable());
        notifyAdjustedForImeChanged(this.mAdjustedForIme, 0);
    }

    void setResizeDimLayer(boolean visible, int targetStackId, float alpha) {
        this.mService.openSurfaceTransaction();
        TaskStack stack = this.mDisplayContent.getStackById(targetStackId);
        boolean visibleAndValid = (!visible || stack == null || this.mDisplayContent.getDockedStackLocked() == null) ? false : true;
        if (visibleAndValid) {
            stack.getDimBounds(this.mTmpRect);
            if (this.mTmpRect.height() <= 0 || this.mTmpRect.width() <= 0) {
                visibleAndValid = false;
            } else {
                this.mDimLayer.setBounds(this.mTmpRect);
                this.mDimLayer.show(getResizeDimLayer(), alpha, 0);
            }
        }
        if (!visibleAndValid) {
            this.mDimLayer.hide();
        }
        this.mService.closeSurfaceTransaction();
    }

    private int getResizeDimLayer() {
        return this.mWindow != null ? this.mWindow.mLayer - 1 : 1;
    }

    void notifyAppVisibilityChanged() {
        checkMinimizeChanged(false);
    }

    void notifyAppTransitionStarting(ArraySet<AppWindowToken> openingApps, int appTransition) {
        boolean wasMinimized = this.mMinimizedDock;
        checkMinimizeChanged(true);
        if (wasMinimized && this.mMinimizedDock && containsAppInDockedStack(openingApps) && appTransition != 0 && (AppTransition.isKeyguardGoingAwayTransit(appTransition) ^ 1) != 0) {
            this.mService.showRecentApps(true);
        }
    }

    private boolean containsAppInDockedStack(ArraySet<AppWindowToken> apps) {
        for (int i = apps.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) apps.valueAt(i);
            if (token.getTask() != null && token.getTask().mStack.mStackId == 3) {
                return true;
            }
        }
        return false;
    }

    boolean isMinimizedDock() {
        return this.mMinimizedDock;
    }

    private void checkMinimizeChanged(boolean animate) {
        if (this.mDisplayContent.getDockedStackIgnoringVisibility() != null) {
            TaskStack homeStack = this.mDisplayContent.getHomeStack();
            if (homeStack != null) {
                Task homeTask = homeStack.findHomeTask();
                if (homeTask != null && (isWithinDisplay(homeTask) ^ 1) == 0) {
                    if (!this.mMinimizedDock || !this.mService.mPolicy.isKeyguardShowingAndNotOccluded()) {
                        int homeBehind;
                        TaskStack fullscreenStack = this.mDisplayContent.getStackById(1);
                        boolean homeVisible = homeTask.getTopVisibleAppToken() != null;
                        if (fullscreenStack == null || !fullscreenStack.isVisible()) {
                            homeBehind = homeStack.hasMultipleTaskWithHomeTaskNotTop();
                        } else {
                            homeBehind = 1;
                        }
                        setMinimizedDockedStack(homeVisible ? homeBehind ^ 1 : false, animate);
                    }
                }
            }
        }
    }

    private boolean isWithinDisplay(Task task) {
        task.mStack.getBounds(this.mTmpRect);
        this.mDisplayContent.getLogicalDisplayRect(this.mTmpRect2);
        return this.mTmpRect.intersect(this.mTmpRect2);
    }

    private void setMinimizedDockedStack(boolean minimizedDock, boolean animate) {
        boolean wasMinimized = this.mMinimizedDock;
        this.mMinimizedDock = minimizedDock;
        if (minimizedDock != wasMinimized) {
            boolean imeChanged = clearImeAdjustAnimation();
            boolean minimizedChange = false;
            if (isHomeStackResizable()) {
                notifyDockedStackMinimizedChanged(minimizedDock, true, true);
                minimizedChange = true;
            } else if (minimizedDock) {
                if (animate) {
                    startAdjustAnimation(0.0f, 1.0f);
                } else {
                    minimizedChange = setMinimizedDockedStack(true);
                }
            } else if (animate) {
                startAdjustAnimation(1.0f, 0.0f);
            } else {
                minimizedChange = setMinimizedDockedStack(false);
            }
            if (imeChanged || minimizedChange) {
                if (imeChanged && (minimizedChange ^ 1) != 0) {
                    Slog.d(TAG, "setMinimizedDockedStack: IME adjust changed due to minimizing, minimizedDock=" + minimizedDock + " minimizedChange=" + minimizedChange);
                }
                this.mService.mWindowPlacerLocked.performSurfacePlacement();
            }
        }
    }

    private boolean clearImeAdjustAnimation() {
        boolean changed = this.mDisplayContent.clearImeAdjustAnimation();
        this.mAnimatingForIme = false;
        return changed;
    }

    private void startAdjustAnimation(float from, float to) {
        this.mAnimatingForMinimizedDockedStack = true;
        this.mAnimationStarted = false;
        this.mAnimationStart = from;
        this.mAnimationTarget = to;
    }

    private void startImeAdjustAnimation(boolean adjustedForIme, boolean adjustedForDivider, WindowState imeWin) {
        int i;
        int i2 = 0;
        boolean z = true;
        if (this.mAnimatingForIme) {
            this.mAnimationStart = this.mLastAnimationProgress;
            this.mDividerAnimationStart = this.mLastDividerProgress;
        } else {
            this.mAnimationStart = (float) (this.mAdjustedForIme ? 1 : 0);
            if (this.mAdjustedForDivider) {
                i = 1;
            } else {
                i = 0;
            }
            this.mDividerAnimationStart = (float) i;
            this.mLastAnimationProgress = this.mAnimationStart;
            this.mLastDividerProgress = this.mDividerAnimationStart;
        }
        this.mAnimatingForIme = true;
        this.mAnimationStarted = false;
        if (adjustedForIme) {
            i = 1;
        } else {
            i = 0;
        }
        this.mAnimationTarget = (float) i;
        if (adjustedForDivider) {
            i2 = 1;
        }
        this.mDividerAnimationTarget = (float) i2;
        this.mDisplayContent.beginImeAdjustAnimation();
        if (this.mService.mWaitingForDrawn.isEmpty()) {
            if (!adjustedForIme) {
                z = adjustedForDivider;
            }
            notifyAdjustedForImeChanged(z, IME_ADJUST_ANIM_DURATION);
            return;
        }
        this.mService.mH.removeMessages(24);
        this.mService.mH.sendEmptyMessageDelayed(24, IME_ADJUST_DRAWN_TIMEOUT);
        this.mAnimationStartDelayed = true;
        if (imeWin != null) {
            if (this.mDelayedImeWin != null) {
                this.mDelayedImeWin.mWinAnimator.endDelayingAnimationStart();
            }
            this.mDelayedImeWin = imeWin;
            imeWin.mWinAnimator.startDelayingAnimationStart();
        }
        if (this.mService.mWaitingForDrawnCallback != null) {
            this.mService.mWaitingForDrawnCallback.run();
        }
        this.mService.mWaitingForDrawnCallback = new com.android.server.wm.-$Lambda$HFbYE8vr4-NA3Y6wUONVd47OH54.AnonymousClass1(adjustedForIme, adjustedForDivider, this);
    }

    /* synthetic */ void lambda$-com_android_server_wm_DockedStackDividerController_33860(boolean adjustedForIme, boolean adjustedForDivider) {
        this.mAnimationStartDelayed = false;
        if (this.mDelayedImeWin != null) {
            this.mDelayedImeWin.mWinAnimator.endDelayingAnimationStart();
        }
        long duration = 0;
        if (this.mAdjustedForIme == adjustedForIme && this.mAdjustedForDivider == adjustedForDivider) {
            duration = IME_ADJUST_ANIM_DURATION;
        } else {
            Slog.w(TAG, "IME adjust changed while waiting for drawn: adjustedForIme=" + adjustedForIme + " adjustedForDivider=" + adjustedForDivider + " mAdjustedForIme=" + this.mAdjustedForIme + " mAdjustedForDivider=" + this.mAdjustedForDivider);
        }
        notifyAdjustedForImeChanged(!this.mAdjustedForIme ? this.mAdjustedForDivider : true, duration);
    }

    private boolean setMinimizedDockedStack(boolean minimized) {
        TaskStack stack = this.mDisplayContent.getDockedStackIgnoringVisibility();
        notifyDockedStackMinimizedChanged(minimized, false, isHomeStackResizable());
        if (stack == null) {
            return false;
        }
        return stack.setAdjustedForMinimizedDock(minimized ? 1.0f : 0.0f);
    }

    private boolean isAnimationMaximizing() {
        return this.mAnimationTarget == 0.0f;
    }

    public boolean animate(long now) {
        if (this.mWindow == null) {
            return false;
        }
        if (this.mAnimatingForMinimizedDockedStack) {
            return animateForMinimizedDockedStack(now);
        }
        if (this.mAnimatingForIme) {
            return animateForIme(now);
        }
        if (this.mDimLayer != null && this.mDimLayer.isDimming()) {
            this.mDimLayer.setLayer(getResizeDimLayer());
        }
        return false;
    }

    private boolean animateForIme(long now) {
        if (!this.mAnimationStarted || this.mAnimationStartDelayed) {
            this.mAnimationStarted = true;
            this.mAnimationStartTime = now;
            this.mAnimationDuration = (long) (this.mService.getWindowAnimationScaleLocked() * 280.0f);
        }
        float t = (this.mAnimationTarget == 1.0f ? IME_ADJUST_ENTRY_INTERPOLATOR : AppTransition.TOUCH_RESPONSE_INTERPOLATOR).getInterpolation(Math.min(1.0f, ((float) (now - this.mAnimationStartTime)) / ((float) this.mAnimationDuration)));
        if (this.mDisplayContent.animateForIme(t, this.mAnimationTarget, this.mDividerAnimationTarget)) {
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        }
        if (t < 1.0f) {
            return true;
        }
        this.mLastAnimationProgress = this.mAnimationTarget;
        this.mLastDividerProgress = this.mDividerAnimationTarget;
        this.mAnimatingForIme = false;
        return false;
    }

    private boolean animateForMinimizedDockedStack(long now) {
        TaskStack stack = this.mDisplayContent.getStackById(3);
        if (!this.mAnimationStarted) {
            this.mAnimationStarted = true;
            this.mAnimationStartTime = now;
            notifyDockedStackMinimizedChanged(this.mMinimizedDock, true, isHomeStackResizable());
        }
        float t = (isAnimationMaximizing() ? AppTransition.TOUCH_RESPONSE_INTERPOLATOR : this.mMinimizedDockInterpolator).getInterpolation(Math.min(1.0f, ((float) (now - this.mAnimationStartTime)) / ((float) this.mAnimationDuration)));
        if (stack != null && stack.setAdjustedForMinimizedDock(getMinimizeAmount(stack, t))) {
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        }
        if (t < 1.0f) {
            return true;
        }
        this.mAnimatingForMinimizedDockedStack = false;
        return false;
    }

    float getInterpolatedAnimationValue(float t) {
        return (this.mAnimationTarget * t) + ((1.0f - t) * this.mAnimationStart);
    }

    float getInterpolatedDividerValue(float t) {
        return (this.mDividerAnimationTarget * t) + ((1.0f - t) * this.mDividerAnimationStart);
    }

    private float getMinimizeAmount(TaskStack stack, float t) {
        float naturalAmount = getInterpolatedAnimationValue(t);
        if (isAnimationMaximizing()) {
            return adjustMaximizeAmount(stack, t, naturalAmount);
        }
        return naturalAmount;
    }

    private float adjustMaximizeAmount(TaskStack stack, float t, float naturalAmount) {
        if (this.mMaximizeMeetFraction == 1.0f) {
            return naturalAmount;
        }
        float amountPrime = (this.mAnimationTarget * t) + ((1.0f - t) * (((float) this.mService.mAppTransition.getLastClipRevealMaxTranslation()) / ((float) stack.getMinimizeDistance())));
        float t2 = Math.min(t / this.mMaximizeMeetFraction, 1.0f);
        return (amountPrime * t2) + ((1.0f - t2) * naturalAmount);
    }

    private float getClipRevealMeetFraction(TaskStack stack) {
        if (!isAnimationMaximizing() || stack == null || (this.mService.mAppTransition.hadClipRevealAnimation() ^ 1) != 0) {
            return 1.0f;
        }
        return ((1.0f - Math.max(0.0f, Math.min(1.0f, ((((float) Math.abs(this.mService.mAppTransition.getLastClipRevealMaxTranslation())) / ((float) stack.getMinimizeDistance())) - CLIP_REVEAL_MEET_FRACTION_MIN) / CLIP_REVEAL_MEET_FRACTION_MIN))) * 0.39999998f) + CLIP_REVEAL_MEET_EARLIEST;
    }

    public boolean dimFullscreen() {
        return false;
    }

    public DisplayInfo getDisplayInfo() {
        return this.mDisplayContent.getDisplayInfo();
    }

    public boolean isAttachedToDisplay() {
        return this.mDisplayContent != null;
    }

    public void getDimBounds(Rect outBounds) {
    }

    public String toShortString() {
        return TAG;
    }

    WindowState getWindow() {
        return this.mWindow;
    }

    void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + "DockedStackDividerController");
        pw.println(prefix + "  mLastVisibility=" + this.mLastVisibility);
        pw.println(prefix + "  mMinimizedDock=" + this.mMinimizedDock);
        pw.println(prefix + "  mAdjustedForIme=" + this.mAdjustedForIme);
        pw.println(prefix + "  mAdjustedForDivider=" + this.mAdjustedForDivider);
        if (this.mDimLayer.isDimming()) {
            pw.println(prefix + "  Dim layer is dimming: ");
            this.mDimLayer.printTo(prefix + "    ", pw);
        }
    }

    public void setDockedStackDividerRotation(int rotation) {
        this.mRotation = rotation;
    }

    public void adjustBoundsForSingleHand() {
        this.mDimLayer.adjustBoundsForSingleHand();
    }
}
