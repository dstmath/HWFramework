package com.android.server.wm;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.IDockedStackListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DockedDividerUtils;
import com.android.server.LocalServices;
import java.io.PrintWriter;
import java.util.function.Consumer;

public class DockedStackDividerController {
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
    private TaskStack mDimmedStack;
    private final DisplayContent mDisplayContent;
    private float mDividerAnimationStart;
    private float mDividerAnimationTarget;
    private int mDividerInsets;
    private int mDividerWindowWidth;
    private int mDividerWindowWidthInactive;
    private final RemoteCallbackList<IDockedStackListener> mDockedStackListeners = new RemoteCallbackList<>();
    private final Handler mHandler = new Handler();
    private int mImeHeight;
    private boolean mImeHideRequested;
    float mLastAnimationProgress;
    private float mLastDimLayerAlpha;
    private final Rect mLastDimLayerRect = new Rect();
    float mLastDividerProgress;
    private final Rect mLastRect = new Rect();
    private boolean mLastVisibility = false;
    private float mMaximizeMeetFraction;
    private boolean mMinimizedDock;
    private final Interpolator mMinimizedDockInterpolator;
    private int mOriginalDockedSide = -1;
    private boolean mResizing;
    private int mRotation = 0;
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
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
        this.mMinimizedDockInterpolator = AnimationUtils.loadInterpolator(service.mContext, 17563661);
        loadDimens();
    }

    /* access modifiers changed from: package-private */
    public int getSmallestWidthDpForBounds(Rect bounds) {
        DisplayInfo di = this.mDisplayContent.getDisplayInfo();
        int baseDisplayWidth = this.mDisplayContent.mBaseDisplayWidth;
        int baseDisplayHeight = this.mDisplayContent.mBaseDisplayHeight;
        int minWidth = Integer.MAX_VALUE;
        int rotation = 0;
        while (rotation < 4) {
            this.mTmpRect.set(bounds);
            this.mDisplayContent.rotateBounds(di.rotation, rotation, this.mTmpRect);
            int orientation = 1;
            boolean rotated = rotation == 1 || rotation == 3;
            this.mTmpRect2.set(0, 0, rotated ? baseDisplayHeight : baseDisplayWidth, rotated ? baseDisplayWidth : baseDisplayHeight);
            if (this.mTmpRect2.width() > this.mTmpRect2.height()) {
                orientation = 2;
            }
            int dockSide = getDockSide(this.mTmpRect, this.mTmpRect2, orientation);
            int position = DockedDividerUtils.calculatePositionForBounds(this.mTmpRect, dockSide, getContentWidth());
            DisplayCutout displayCutout = this.mDisplayContent.calculateDisplayCutoutForRotation(rotation).getDisplayCutout();
            int snappedPosition = this.mSnapAlgorithmForRotation[rotation].calculateNonDismissingSnapTarget(position).position;
            DockedDividerUtils.calculateBoundsForPosition(snappedPosition, dockSide, this.mTmpRect, this.mTmpRect2.width(), this.mTmpRect2.height(), getContentWidth());
            int i = snappedPosition;
            int i2 = position;
            int i3 = dockSide;
            this.mService.mPolicy.getStableInsetsLw(rotation, this.mTmpRect2.width(), this.mTmpRect2.height(), displayCutout, this.mTmpRect3);
            this.mService.intersectDisplayInsetBounds(this.mTmpRect2, this.mTmpRect3, this.mTmpRect);
            minWidth = Math.min(this.mTmpRect.width(), minWidth);
            rotation++;
        }
        Rect rect = bounds;
        return (int) (((float) minWidth) / this.mDisplayContent.getDisplayMetrics().density);
    }

    /* access modifiers changed from: package-private */
    public int getDockSide(Rect bounds, Rect displayRect, int orientation) {
        int i = 3;
        if (this.mService.mHwWMSEx.isInFoldFullDisplayMode()) {
            int diff = (displayRect.right - bounds.right) - (bounds.left - displayRect.left);
            if (diff > 0) {
                return 1;
            }
            if (diff < 0) {
                return 3;
            }
            if (canPrimaryStackDockTo(1)) {
                i = 1;
            }
            return i;
        }
        int diff2 = 2;
        if (orientation == 1) {
            int diff3 = (displayRect.bottom - bounds.bottom) - (bounds.top - displayRect.top);
            if (diff3 > 0) {
                return 2;
            }
            if (diff3 < 0) {
                return 4;
            }
            if (!canPrimaryStackDockTo(2)) {
                diff2 = 4;
            }
            return diff2;
        } else if (orientation != 2) {
            return -1;
        } else {
            int diff4 = (displayRect.right - bounds.right) - (bounds.left - displayRect.left);
            if (diff4 > 0) {
                return 1;
            }
            if (diff4 < 0) {
                return 3;
            }
            if (canPrimaryStackDockTo(1)) {
                i = 1;
            }
            return i;
        }
    }

    /* access modifiers changed from: package-private */
    public void getHomeStackBoundsInDockedMode(Rect outBounds) {
        DisplayInfo di = this.mDisplayContent.getDisplayInfo();
        this.mService.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, di.displayCutout, this.mTmpRect);
        int dividerSize = this.mDividerWindowWidth - (2 * this.mDividerInsets);
        if (this.mDisplayContent.getConfiguration().orientation != 1 || this.mService.mHwWMSEx.isInFoldFullDisplayMode()) {
            TaskStack stack = this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility();
            int primaryTaskWidth = this.mTaskHeightInMinimizedMode + dividerSize + this.mTmpRect.top;
            int left = this.mTmpRect.left;
            int right = di.logicalWidth - this.mTmpRect.right;
            if (stack != null) {
                if (stack.getDockSide() == 1) {
                    left += primaryTaskWidth;
                } else if (stack.getDockSide() == 3) {
                    right -= primaryTaskWidth;
                }
            }
            outBounds.set(left, 0, right, di.logicalHeight);
            return;
        }
        outBounds.set(0, this.mTaskHeightInMinimizedMode + dividerSize + this.mTmpRect.top, di.logicalWidth, di.logicalHeight);
    }

    /* access modifiers changed from: package-private */
    public boolean isHomeStackResizable() {
        TaskStack homeStack = this.mDisplayContent.getHomeStack();
        boolean z = false;
        if (homeStack == null) {
            return false;
        }
        Task homeTask = homeStack.findHomeTask();
        if (homeTask != null && homeTask.isResizeable()) {
            z = true;
        }
        return z;
    }

    private void initSnapAlgorithmForRotations() {
        int i;
        int i2;
        Configuration baseConfig = this.mDisplayContent.getConfiguration();
        Configuration config = new Configuration();
        int rotation = 0;
        while (rotation < 4) {
            boolean rotated = rotation == 1 || rotation == 3;
            if (rotated) {
                i = this.mDisplayContent.mBaseDisplayHeight;
            } else {
                i = this.mDisplayContent.mBaseDisplayWidth;
            }
            int dw = i;
            if (rotated) {
                i2 = this.mDisplayContent.mBaseDisplayWidth;
            } else {
                i2 = this.mDisplayContent.mBaseDisplayHeight;
            }
            int dh = i2;
            DisplayCutout displayCutout = this.mDisplayContent.calculateDisplayCutoutForRotation(rotation).getDisplayCutout();
            this.mService.mPolicy.getStableInsetsLw(rotation, dw, dh, displayCutout, this.mTmpRect);
            config.unset();
            config.orientation = dw <= dh ? 1 : 2;
            int displayId = this.mDisplayContent.getDisplayId();
            int i3 = dw;
            int i4 = dh;
            int i5 = rotation;
            int i6 = displayId;
            DisplayCutout displayCutout2 = displayCutout;
            int appWidth = this.mService.mPolicy.getNonDecorDisplayWidth(i3, i4, i5, baseConfig.uiMode, i6, displayCutout2);
            int appHeight = this.mService.mPolicy.getNonDecorDisplayHeight(i3, i4, i5, baseConfig.uiMode, i6, displayCutout2);
            this.mService.mPolicy.getNonDecorInsetsLw(rotation, dw, dh, displayCutout, this.mTmpRect);
            int leftInset = this.mTmpRect.left;
            int topInset = this.mTmpRect.top;
            config.windowConfiguration.setAppBounds(leftInset, topInset, leftInset + appWidth, topInset + appHeight);
            int i7 = dw;
            int i8 = dh;
            int i9 = rotation;
            float density = this.mDisplayContent.getDisplayMetrics().density;
            int i10 = topInset;
            int topInset2 = displayId;
            int i11 = leftInset;
            DisplayCutout displayCutout3 = displayCutout;
            config.screenWidthDp = (int) (((float) this.mService.mPolicy.getConfigDisplayWidth(i7, i8, i9, baseConfig.uiMode, topInset2, displayCutout3)) / density);
            config.screenHeightDp = (int) (((float) this.mService.mPolicy.getConfigDisplayHeight(i7, i8, i9, baseConfig.uiMode, topInset2, displayCutout3)) / density);
            Context rotationContext = this.mService.mContext.createConfigurationContext(config);
            DividerSnapAlgorithm[] dividerSnapAlgorithmArr = this.mSnapAlgorithmForRotation;
            DividerSnapAlgorithm dividerSnapAlgorithm = new DividerSnapAlgorithm(rotationContext.getResources(), dw, dh, getContentWidth(), config.orientation == 1, this.mTmpRect);
            dividerSnapAlgorithmArr[rotation] = dividerSnapAlgorithm;
            rotation++;
        }
    }

    private void loadDimens() {
        Context context = this.mService.mContext;
        this.mDividerWindowWidth = context.getResources().getDimensionPixelSize(17105035);
        this.mDividerInsets = context.getResources().getDimensionPixelSize(17105034);
        this.mDividerWindowWidthInactive = WindowManagerService.dipToPixel(4, this.mDisplayContent.getDisplayMetrics());
        this.mTaskHeightInMinimizedMode = context.getResources().getDimensionPixelSize(17105326);
        initSnapAlgorithmForRotations();
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationChanged() {
        loadDimens();
    }

    /* access modifiers changed from: package-private */
    public boolean isResizing() {
        return this.mResizing;
    }

    /* access modifiers changed from: package-private */
    public int getContentWidth() {
        return this.mDividerWindowWidth - (2 * this.mDividerInsets);
    }

    /* access modifiers changed from: package-private */
    public int getContentInsets() {
        return this.mDividerInsets;
    }

    /* access modifiers changed from: package-private */
    public int getContentWidthInactive() {
        return this.mDividerWindowWidthInactive;
    }

    /* access modifiers changed from: package-private */
    public void setResizing(boolean resizing) {
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

    /* access modifiers changed from: package-private */
    public void setTouchRegion(Rect touchRegion) {
        this.mTouchRegion.set(touchRegion);
    }

    /* access modifiers changed from: package-private */
    public void getTouchRegion(Rect outRegion) {
        outRegion.set(this.mTouchRegion);
        outRegion.offset(this.mWindow.getFrameLw().left, this.mWindow.getFrameLw().top);
    }

    private void resetDragResizingChangeReported() {
        this.mDisplayContent.forAllWindows((Consumer<WindowState>) $$Lambda$vhwCXwzYksBgFM46tASKUCeQRc.INSTANCE, true);
    }

    /* access modifiers changed from: package-private */
    public void setWindow(WindowState window) {
        this.mWindow = window;
        reevaluateVisibility(false);
    }

    /* access modifiers changed from: package-private */
    public void reevaluateVisibility(boolean force) {
        if (this.mWindow != null) {
            boolean visible = this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility() != null;
            if (this.mLastVisibility != visible || force) {
                this.mLastVisibility = visible;
                notifyDockedDividerVisibilityChanged(visible);
                if (!visible) {
                    setResizeDimLayer(false, 0, 0.0f);
                }
            }
        }
    }

    private boolean wasVisible() {
        return this.mLastVisibility;
    }

    /* access modifiers changed from: package-private */
    public void setAdjustedForIme(boolean adjustedForIme, boolean adjustedForDivider, boolean animate, WindowState imeWin, int imeHeight) {
        if (this.mAdjustedForIme != adjustedForIme || ((adjustedForIme && this.mImeHeight != imeHeight) || this.mAdjustedForDivider != adjustedForDivider)) {
            if (!animate || this.mAnimatingForMinimizedDockedStack) {
                notifyAdjustedForImeChanged(adjustedForIme || adjustedForDivider, 0);
            } else {
                startImeAdjustAnimation(adjustedForIme, adjustedForDivider, imeWin);
            }
            this.mAdjustedForIme = adjustedForIme;
            this.mImeHeight = imeHeight;
            this.mAdjustedForDivider = adjustedForDivider;
        }
    }

    /* access modifiers changed from: package-private */
    public int getImeHeightAdjustedFor() {
        return this.mImeHeight;
    }

    /* access modifiers changed from: package-private */
    public void positionDockedStackedDivider(Rect frame) {
        TaskStack stack = this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility();
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
                    this.mDockedStackListeners.getBroadcastItem(i).onDividerVisibilityChanged(visible);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error delivering divider visibility changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canPrimaryStackDockTo(int dockSide) {
        DisplayInfo di = this.mDisplayContent.getDisplayInfo();
        return this.mService.mPolicy.isDockSideAllowed(dockSide, this.mOriginalDockedSide, di.logicalWidth, di.logicalHeight, di.rotation);
    }

    /* access modifiers changed from: package-private */
    public void notifyDockedStackExistsChanged(boolean exists) {
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    this.mDockedStackListeners.getBroadcastItem(i).onDockedStackExistsChanged(exists);
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
            this.mOriginalDockedSide = this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility().getDockSideForDisplay(this.mDisplayContent);
            return;
        }
        this.mOriginalDockedSide = -1;
        setMinimizedDockedStack(false, false);
        if (this.mDimmedStack != null) {
            this.mDimmedStack.stopDimming();
            this.mDimmedStack = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void resetImeHideRequested() {
        this.mImeHideRequested = false;
    }

    /* access modifiers changed from: package-private */
    public boolean isImeHideRequested() {
        return this.mImeHideRequested;
    }

    private void notifyDockedStackMinimizedChanged(boolean minimizedDock, boolean animate, boolean isHomeStackResizable) {
        long transitionDuration;
        long animDuration = 0;
        if (animate) {
            TaskStack stack = this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility();
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
        this.mService.mH.obtainMessage(53, minimizedDock, 0).sendToTarget();
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    this.mDockedStackListeners.getBroadcastItem(i).onDockedStackMinimizedChanged(minimizedDock, animDuration, isHomeStackResizable);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error delivering minimized dock changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyDockSideChanged(int newDockSide) {
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    this.mDockedStackListeners.getBroadcastItem(i).onDockSideChanged(newDockSide);
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
                    this.mDockedStackListeners.getBroadcastItem(i).onAdjustedForImeChanged(adjustedForIme, animDuration);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error delivering adjusted for ime changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    /* access modifiers changed from: package-private */
    public void registerDockedStackListener(IDockedStackListener listener) {
        boolean z;
        synchronized (this.mDockedStackListeners) {
            this.mDockedStackListeners.register(listener);
        }
        notifyDockedDividerVisibilityChanged(wasVisible());
        if (this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility() != null) {
            z = true;
        } else {
            z = false;
        }
        notifyDockedStackExistsChanged(z);
        notifyDockedStackMinimizedChanged(this.mMinimizedDock, false, isHomeStackResizable());
        notifyAdjustedForImeChanged(this.mAdjustedForIme, 0);
    }

    /* access modifiers changed from: package-private */
    public void setResizeDimLayer(boolean visible, int targetWindowingMode, float alpha) {
        TaskStack stack;
        if (targetWindowingMode != 0) {
            stack = this.mDisplayContent.getTopStackInWindowingMode(targetWindowingMode);
        } else {
            stack = null;
        }
        boolean visibleAndValid = (!visible || stack == null || this.mDisplayContent.getSplitScreenPrimaryStack() == null) ? false : true;
        if (!(this.mDimmedStack == null || this.mDimmedStack == stack)) {
            this.mDimmedStack.stopDimming();
            this.mDimmedStack = null;
        }
        if (visibleAndValid) {
            this.mDimmedStack = stack;
            stack.dim(alpha);
        }
        if (!visibleAndValid && stack != null) {
            this.mDimmedStack = null;
            stack.stopDimming();
        }
    }

    private int getResizeDimLayer() {
        if (this.mWindow != null) {
            return this.mWindow.mLayer - 1;
        }
        return 1;
    }

    /* access modifiers changed from: package-private */
    public void notifyAppVisibilityChanged() {
        checkMinimizeChanged(false);
    }

    /* access modifiers changed from: package-private */
    public void notifyAppTransitionStarting(ArraySet<AppWindowToken> openingApps, int appTransition) {
        boolean wasMinimized = this.mMinimizedDock;
        checkMinimizeChanged(true);
        if (wasMinimized && this.mMinimizedDock && containsAppInDockedStack(openingApps) && appTransition != 0 && !AppTransition.isKeyguardGoingAwayTransit(appTransition) && !this.mService.mAmInternal.isRecentsComponentHomeActivity(this.mService.mCurrentUserId)) {
            if (this.mService.mAppTransition.mIgnoreShowRecentApps) {
                Slog.d(TAG, "notifyAppTransitionStarting Ignore Show RecentApps " + this.mService.mAppTransition.mIgnoreShowRecentApps);
                return;
            }
            this.mService.showRecentApps();
        }
    }

    private boolean containsAppInDockedStack(ArraySet<AppWindowToken> apps) {
        for (int i = apps.size() - 1; i >= 0; i--) {
            AppWindowToken token = apps.valueAt(i);
            if (token.getTask() != null && token.inSplitScreenPrimaryWindowingMode()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isMinimizedDock() {
        return this.mMinimizedDock;
    }

    /* access modifiers changed from: package-private */
    public void checkMinimizeChanged(boolean animate) {
        if (this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility() != null) {
            TaskStack homeStack = this.mDisplayContent.getHomeStack();
            if (homeStack != null) {
                Task homeTask = homeStack.findHomeTask();
                if (homeTask != null && isWithinDisplay(homeTask)) {
                    if (!this.mMinimizedDock || !this.mService.mKeyguardOrAodShowingOnDefaultDisplay) {
                        TaskStack topSecondaryStack = this.mDisplayContent.getTopStackInWindowingMode(4);
                        RecentsAnimationController recentsAnim = this.mService.getRecentsAnimationController();
                        boolean z = false;
                        boolean minimizedForRecentsAnimation = recentsAnim != null && recentsAnim.isSplitScreenMinimized();
                        boolean homeVisible = homeTask.getTopVisibleAppToken() != null;
                        if (homeVisible && topSecondaryStack != null) {
                            homeVisible = homeStack.compareTo(topSecondaryStack) >= 0;
                        }
                        if (homeVisible || minimizedForRecentsAnimation) {
                            z = true;
                        }
                        setMinimizedDockedStack(z, animate);
                    }
                }
            }
        }
    }

    private boolean isWithinDisplay(Task task) {
        task.getBounds(this.mTmpRect);
        this.mDisplayContent.getBounds(this.mTmpRect2);
        return this.mTmpRect.intersect(this.mTmpRect2);
    }

    private void setMinimizedDockedStack(boolean minimizedDock, boolean animate) {
        boolean wasMinimized = this.mMinimizedDock;
        this.mMinimizedDock = minimizedDock;
        if (minimizedDock != wasMinimized) {
            boolean imeChanged = clearImeAdjustAnimation();
            boolean minimizedChange = false;
            if (isHomeStackResizable()) {
                notifyDockedStackMinimizedChanged(minimizedDock, animate, true);
                minimizedChange = true;
            } else if (minimizedDock) {
                if (animate) {
                    startAdjustAnimation(0.0f, 1.0f);
                } else {
                    minimizedChange = false | setMinimizedDockedStack(true);
                }
            } else if (animate) {
                startAdjustAnimation(1.0f, 0.0f);
            } else {
                minimizedChange = false | setMinimizedDockedStack(false);
            }
            if (imeChanged || minimizedChange) {
                if (imeChanged && !minimizedChange) {
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
        float f = 0.0f;
        if (!this.mAnimatingForIme) {
            this.mAnimationStart = this.mAdjustedForIme ? 1.0f : 0.0f;
            this.mDividerAnimationStart = this.mAdjustedForDivider ? 1.0f : 0.0f;
            this.mLastAnimationProgress = this.mAnimationStart;
            this.mLastDividerProgress = this.mDividerAnimationStart;
        } else {
            this.mAnimationStart = this.mLastAnimationProgress;
            this.mDividerAnimationStart = this.mLastDividerProgress;
        }
        boolean z = true;
        this.mAnimatingForIme = true;
        this.mAnimationStarted = false;
        this.mAnimationTarget = adjustedForIme ? 1.0f : 0.0f;
        if (adjustedForDivider) {
            f = 1.0f;
        }
        this.mDividerAnimationTarget = f;
        this.mDisplayContent.beginImeAdjustAnimation();
        if (!this.mService.mWaitingForDrawn.isEmpty()) {
            this.mService.mH.removeMessages(24);
            this.mService.mH.sendEmptyMessageDelayed(24, IME_ADJUST_DRAWN_TIMEOUT);
            this.mAnimationStartDelayed = true;
            if (imeWin != null) {
                if (this.mDelayedImeWin != null) {
                    this.mDelayedImeWin.endDelayingAnimationStart();
                }
                this.mDelayedImeWin = imeWin;
                imeWin.startDelayingAnimationStart();
            }
            if (this.mService.mWaitingForDrawnCallback != null) {
                this.mService.mWaitingForDrawnCallback.run();
            }
            this.mService.mWaitingForDrawnCallback = new Runnable(adjustedForIme, adjustedForDivider) {
                private final /* synthetic */ boolean f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    DockedStackDividerController.lambda$startImeAdjustAnimation$0(DockedStackDividerController.this, this.f$1, this.f$2);
                }
            };
            return;
        }
        if (!adjustedForIme && !adjustedForDivider) {
            z = false;
        }
        notifyAdjustedForImeChanged(z, IME_ADJUST_ANIM_DURATION);
    }

    public static /* synthetic */ void lambda$startImeAdjustAnimation$0(DockedStackDividerController dockedStackDividerController, boolean adjustedForIme, boolean adjustedForDivider) {
        synchronized (dockedStackDividerController.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                boolean z = false;
                dockedStackDividerController.mAnimationStartDelayed = false;
                if (dockedStackDividerController.mDelayedImeWin != null) {
                    dockedStackDividerController.mDelayedImeWin.endDelayingAnimationStart();
                }
                long duration = 0;
                if (dockedStackDividerController.mAdjustedForIme == adjustedForIme && dockedStackDividerController.mAdjustedForDivider == adjustedForDivider) {
                    duration = IME_ADJUST_ANIM_DURATION;
                } else {
                    Slog.w(TAG, "IME adjust changed while waiting for drawn: adjustedForIme=" + adjustedForIme + " adjustedForDivider=" + adjustedForDivider + " mAdjustedForIme=" + dockedStackDividerController.mAdjustedForIme + " mAdjustedForDivider=" + dockedStackDividerController.mAdjustedForDivider);
                }
                if (!dockedStackDividerController.mAdjustedForIme) {
                    if (!dockedStackDividerController.mAdjustedForDivider) {
                        dockedStackDividerController.notifyAdjustedForImeChanged(z, duration);
                    }
                }
                z = true;
                dockedStackDividerController.notifyAdjustedForImeChanged(z, duration);
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    private boolean setMinimizedDockedStack(boolean minimized) {
        TaskStack stack = this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility();
        notifyDockedStackMinimizedChanged(minimized, false, isHomeStackResizable());
        if (stack == null) {
            return false;
        }
        if (stack.setAdjustedForMinimizedDock(minimized ? 1.0f : 0.0f)) {
            return true;
        }
        return false;
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
        return false;
    }

    private boolean animateForIme(long now) {
        if (!this.mAnimationStarted || this.mAnimationStartDelayed) {
            this.mAnimationStarted = true;
            this.mAnimationStartTime = now;
            this.mAnimationDuration = (long) (280.0f * this.mService.getWindowAnimationScaleLocked());
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
        TaskStack stack = this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility();
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

    /* access modifiers changed from: package-private */
    public float getInterpolatedAnimationValue(float t) {
        return (this.mAnimationTarget * t) + ((1.0f - t) * this.mAnimationStart);
    }

    /* access modifiers changed from: package-private */
    public float getInterpolatedDividerValue(float t) {
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
        if (!isAnimationMaximizing() || stack == null || !this.mService.mAppTransition.hadClipRevealAnimation()) {
            return 1.0f;
        }
        return CLIP_REVEAL_MEET_EARLIEST + ((1.0f - Math.max(0.0f, Math.min(1.0f, ((((float) Math.abs(this.mService.mAppTransition.getLastClipRevealMaxTranslation())) / ((float) stack.getMinimizeDistance())) - CLIP_REVEAL_MEET_FRACTION_MIN) / CLIP_REVEAL_MEET_FRACTION_MIN))) * 0.39999998f);
    }

    public String toShortString() {
        return TAG;
    }

    /* access modifiers changed from: package-private */
    public WindowState getWindow() {
        return this.mWindow;
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + "DockedStackDividerController");
        pw.println(prefix + "  mLastVisibility=" + this.mLastVisibility);
        pw.println(prefix + "  mMinimizedDock=" + this.mMinimizedDock);
        pw.println(prefix + "  mAdjustedForIme=" + this.mAdjustedForIme);
        pw.println(prefix + "  mAdjustedForDivider=" + this.mAdjustedForDivider);
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1133871366145L, this.mMinimizedDock);
        proto.end(token);
    }

    public void setDockedStackDividerRotation(int rotation) {
        this.mRotation = rotation;
    }

    public void adjustBoundsForSingleHand() {
    }

    /* access modifiers changed from: package-private */
    public void positionCoordinationStackDivider(Rect frame) {
        TaskStack stack = this.mDisplayContent.getCoordinationPrimaryStackIgnoringVisibility();
        if (stack == null) {
            frame.set(this.mLastRect);
            return;
        }
        stack.getRawBounds(this.mTmpRect);
        switch (CoordinationModeUtils.getInstance(this.mService.mContext).getCoordinationCreateMode()) {
            case 3:
                if (!isPortrait()) {
                    frame.set(frame.left, this.mTmpRect.top - frame.height(), this.mTmpRect.right, this.mTmpRect.top);
                    break;
                } else {
                    frame.set(this.mTmpRect.right, frame.top, this.mTmpRect.right + frame.width(), frame.bottom);
                    break;
                }
            case 4:
                if (!isPortrait()) {
                    frame.set(frame.left, this.mTmpRect.bottom, this.mTmpRect.right, this.mTmpRect.bottom + frame.height());
                    break;
                } else {
                    frame.set(this.mTmpRect.left - frame.width(), frame.top, this.mTmpRect.left, frame.bottom);
                    break;
                }
        }
        this.mLastRect.set(frame);
    }

    private boolean isPortrait() {
        int rotation = this.mDisplayContent.getDisplayInfo().rotation;
        if (rotation == 0 || rotation == 2) {
            return true;
        }
        return false;
    }
}
