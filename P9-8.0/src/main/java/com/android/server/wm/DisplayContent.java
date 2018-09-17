package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.hardware.display.DisplayManagerInternal;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.MutableBoolean;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.InputDevice;
import android.view.SurfaceControl;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.WindowState;
import com.android.internal.util.ToBooleanFunction;
import com.android.internal.view.IInputMethodClient;
import com.android.server.HwServiceFactory;
import com.android.server.job.controllers.JobStatus;
import com.android.server.os.HwBootFail;
import com.android.server.power.IHwShutdownThread;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass10;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass11;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass12;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass13;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass14;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass15;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass16;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass17;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass18;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass19;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass20;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass21;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass22;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass23;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass24;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass25;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass26;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass27;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass28;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass29;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass30;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass31;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass32;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass33;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass34;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass7;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass8;
import com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI.AnonymousClass9;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DisplayContent extends AbsDisplayContent {
    private static final boolean IS_DEBUG_VERSION;
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final int PAD_DISPLAY_ID = 100000;
    private static final String TAG = "WindowManager";
    private static final String UNIQUE_ID_PREFIX_LOCAL = "local:";
    final int[] index = new int[1];
    boolean isDefaultDisplay;
    protected final NonAppWindowContainers mAboveAppWindowsContainers = new NonAppWindowContainers("mAboveAppWindowsContainers");
    private boolean mAltOrientation = false;
    private final Consumer<WindowState> mApplyPostLayoutPolicy = new AnonymousClass15(this);
    private final Consumer<WindowState> mApplySurfaceChangesTransaction = new AnonymousClass16(this);
    int mBaseDisplayDensity = 0;
    int mBaseDisplayHeight = 0;
    private Rect mBaseDisplayRect = new Rect();
    int mBaseDisplayWidth = 0;
    private final NonAppWindowContainers mBelowAppWindowsContainers = new NonAppWindowContainers("mBelowAppWindowsContainers");
    private final DisplayMetrics mCompatDisplayMetrics = new DisplayMetrics();
    float mCompatibleScreenScale;
    private final Predicate<WindowState> mComputeImeTargetPredicate = new AnonymousClass21(this);
    private Rect mContentRect = new Rect();
    private boolean mDeferredRemoval;
    DimLayerController mDimLayerController;
    protected final Display mDisplay;
    protected int mDisplayId = -1;
    protected final DisplayInfo mDisplayInfo = new DisplayInfo();
    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    private boolean mDisplayReady = false;
    boolean mDisplayScalingDisabled;
    final DockedStackDividerController mDividerControllerLocked;
    final ArrayList<WindowToken> mExitingTokens = new ArrayList();
    private final ToBooleanFunction<WindowState> mFindFocusedWindow = new AnonymousClass7(this);
    float mForceCompatibleScreenScale;
    private boolean mHaveApp = false;
    private boolean mHaveBootMsg = false;
    private boolean mHaveKeyguard = true;
    private boolean mHaveWallpaper = false;
    private TaskStack mHomeStack = null;
    private final NonAppWindowContainers mImeWindowsContainers = new NonAppWindowContainers("mImeWindowsContainers");
    int mInitialDisplayDensity = 0;
    int mInitialDisplayHeight = 0;
    int mInitialDisplayWidth = 0;
    int mInputMethodAnimLayerAdjustment;
    private int mLastKeyguardForcedOrientation = -1;
    private int mLastOrientation = -1;
    private int mLastWindowForcedOrientation = -1;
    private final WindowLayersController mLayersController;
    private boolean mLayoutNeeded;
    private int mMaxUiWidth;
    private final Region mPcTouchExcludeRegion = new Region();
    private final Consumer<WindowState> mPerformLayout = new AnonymousClass13(this);
    private final Consumer<WindowState> mPerformLayoutAttached = new AnonymousClass14(this);
    final PinnedStackController mPinnedStackControllerLocked;
    private final Consumer<WindowState> mPrepareWindowSurfaces = new AnonymousClass12(this);
    private boolean mQuickBoot = true;
    final DisplayMetrics mRealDisplayMetrics = new DisplayMetrics();
    private boolean mRemovingDisplay = false;
    private int mRotation = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    private final Consumer<WindowState> mScheduleToastTimeout = new AnonymousClass11(this);
    final ScreenshotApplicationState mScreenshotApplicationState = new ScreenshotApplicationState();
    WindowManagerService mService;
    TaskTapPointerEventListener mTapDetector;
    final ArrayList<WindowState> mTapExcludedWindows = new ArrayList();
    protected final TaskStackContainers mTaskStackContainers = new TaskStackContainers();
    private final ApplySurfaceChangesTransactionState mTmpApplySurfaceChangesTransactionState = new ApplySurfaceChangesTransactionState();
    private final DisplayMetrics mTmpDisplayMetrics = new DisplayMetrics();
    private boolean mTmpInitial;
    private final Matrix mTmpMatrix = new Matrix();
    private boolean mTmpRecoveringMemory;
    private final Rect mTmpRect = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private final RectF mTmpRectF = new RectF();
    private final Region mTmpRegion = new Region();
    private final TaskForResizePointSearchResult mTmpTaskForResizePointSearchResult = new TaskForResizePointSearchResult();
    private final LinkedList<AppWindowToken> mTmpUpdateAllDrawn = new LinkedList();
    private WindowState mTmpWindow;
    private WindowState mTmpWindow2;
    private WindowAnimator mTmpWindowAnimator;
    private final HashMap<IBinder, WindowToken> mTokenMap = new HashMap();
    private Region mTouchExcludeRegion = new Region();
    private boolean mUpdateImeTarget;
    private final Consumer<WindowState> mUpdateWallpaperForAnimator = new AnonymousClass10(this);
    private final Consumer<WindowState> mUpdateWindowsForAnimator = new AnonymousClass9(this);
    WallpaperController mWallpaperController;
    int pendingLayoutChanges;

    @FunctionalInterface
    private interface Screenshoter<E> {
        E screenshot(Rect rect, int i, int i2, int i3, int i4, boolean z, int i5);
    }

    private static final class ApplySurfaceChangesTransactionState {
        boolean displayHasContent;
        boolean focusDisplayed;
        boolean obscured;
        int preferredModeId;
        float preferredRefreshRate;
        boolean syswin;

        /* synthetic */ ApplySurfaceChangesTransactionState(ApplySurfaceChangesTransactionState -this0) {
            this();
        }

        private ApplySurfaceChangesTransactionState() {
        }

        void reset() {
            this.displayHasContent = false;
            this.obscured = false;
            this.syswin = false;
            this.focusDisplayed = false;
            this.preferredRefreshRate = 0.0f;
            this.preferredModeId = 0;
        }
    }

    static class DisplayChildWindowContainer<E extends WindowContainer> extends WindowContainer<E> {
        DisplayChildWindowContainer() {
        }

        int size() {
            return this.mChildren.size();
        }

        E get(int index) {
            return (WindowContainer) this.mChildren.get(index);
        }

        boolean fillsParent() {
            return true;
        }

        boolean isVisible() {
            return true;
        }
    }

    protected final class NonAppWindowContainers extends DisplayChildWindowContainer<WindowToken> {
        private final Predicate<WindowState> mGetOrientingWindow = new Predicate() {
            public final boolean test(Object obj) {
                return $m$0(obj);
            }
        };
        private final String mName;
        private final Comparator<WindowToken> mWindowComparator = new AnonymousClass8(this);

        /* synthetic */ int lambda$-com_android_server_wm_DisplayContent$NonAppWindowContainers_175004(WindowToken token1, WindowToken token2) {
            return DisplayContent.this.mService.mPolicy.getWindowLayerFromTypeLw(token1.windowType, token1.mOwnerCanManageAppTokens) < DisplayContent.this.mService.mPolicy.getWindowLayerFromTypeLw(token2.windowType, token2.mOwnerCanManageAppTokens) ? -1 : 1;
        }

        static /* synthetic */ boolean lambda$-com_android_server_wm_DisplayContent$NonAppWindowContainers_175443(WindowState w) {
            if (!w.isVisibleLw() || (w.mPolicyVisibilityAfterAnim ^ 1) != 0) {
                return false;
            }
            int req = w.mAttrs.screenOrientation;
            if (req == -1 || req == 3 || req == -2) {
                return false;
            }
            return true;
        }

        NonAppWindowContainers(String name) {
            this.mName = name;
        }

        void addChild(WindowToken token) {
            addChild((WindowContainer) token, this.mWindowComparator);
        }

        int getOrientation() {
            WindowManagerPolicy policy = DisplayContent.this.mService.mPolicy;
            WindowState win = getWindow(this.mGetOrientingWindow);
            if (win != null) {
                int req = win.mAttrs.screenOrientation;
                if (policy.isKeyguardHostWindow(win.mAttrs)) {
                    DisplayContent.this.mLastKeyguardForcedOrientation = req;
                    if (DisplayContent.this.mService.mKeyguardGoingAway) {
                        DisplayContent.this.mLastWindowForcedOrientation = -1;
                        return -2;
                    }
                }
                return DisplayContent.this.mLastWindowForcedOrientation = req;
            }
            DisplayContent.this.mLastWindowForcedOrientation = -1;
            if (policy.isKeyguardShowingAndNotOccluded()) {
                return DisplayContent.this.mLastKeyguardForcedOrientation;
            }
            return -2;
        }

        String getName() {
            return this.mName;
        }
    }

    static final class ScreenshotApplicationState {
        WindowState appWin;
        int maxLayer;
        int minLayer;
        boolean screenshotReady;

        ScreenshotApplicationState() {
        }

        void reset(boolean screenshotReady) {
            int i = 0;
            this.appWin = null;
            this.maxLayer = 0;
            this.minLayer = 0;
            this.screenshotReady = screenshotReady;
            if (!screenshotReady) {
                i = HwBootFail.STAGE_BOOT_SUCCESS;
            }
            this.minLayer = i;
        }
    }

    static final class TaskForResizePointSearchResult {
        boolean searchDone;
        Task taskForResize;

        TaskForResizePointSearchResult() {
        }

        void reset() {
            this.searchDone = false;
            this.taskForResize = null;
        }
    }

    protected final class TaskStackContainers extends DisplayChildWindowContainer<TaskStack> {
        protected TaskStackContainers() {
        }

        void addStackToDisplay(TaskStack stack, boolean onTop) {
            if (stack.mStackId == 0) {
                if (DisplayContent.this.mHomeStack != null) {
                    throw new IllegalArgumentException("attachStack: HOME_STACK_ID (0) not first.");
                }
                DisplayContent.this.mHomeStack = stack;
            }
            addChild(stack, onTop);
            stack.onDisplayChanged(DisplayContent.this);
        }

        void removeStackFromDisplay(TaskStack stack) {
            removeChild(stack);
            stack.onRemovedFromDisplay();
        }

        private void addChild(TaskStack stack, boolean toTop) {
            addChild((WindowContainer) stack, findPositionForStack(toTop ? this.mChildren.size() : 0, stack, true));
            DisplayContent.this.setLayoutNeeded();
        }

        void positionChildAt(int position, TaskStack child, boolean includingParents) {
            if (!StackId.isAlwaysOnTop(child.mStackId) || position == HwBootFail.STAGE_BOOT_SUCCESS) {
                super.positionChildAt(findPositionForStack(position, child, false), child, includingParents);
                DisplayContent.this.setLayoutNeeded();
                return;
            }
            Slog.w(DisplayContent.TAG, "Ignoring move of always-on-top stack=" + this + " to bottom");
            super.positionChildAt(this.mChildren.indexOf(child), child, false);
        }

        private int findPositionForStack(int requestedPosition, TaskStack stack, boolean adding) {
            int i = 1;
            int topChildPosition = this.mChildren.size() - 1;
            boolean toTop = requestedPosition == HwBootFail.STAGE_BOOT_SUCCESS;
            if (adding) {
                if (requestedPosition < topChildPosition + 1) {
                    i = 0;
                }
            } else if (requestedPosition < topChildPosition) {
                i = 0;
            }
            int targetPosition = requestedPosition;
            if (!(toTop | i) || stack.mStackId == 4 || DisplayContent.this.getStackById(4) == null) {
                return targetPosition;
            }
            if (((TaskStack) this.mChildren.get(topChildPosition)).mStackId == 4) {
                return adding ? topChildPosition : topChildPosition - 1;
            } else {
                throw new IllegalStateException("Pinned stack isn't top stack??? " + this.mChildren);
            }
        }

        boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
            if (traverseTopToBottom) {
                if (super.forAllWindows((ToBooleanFunction) callback, traverseTopToBottom) || forAllExitingAppTokenWindows(callback, traverseTopToBottom)) {
                    return true;
                }
            } else if (forAllExitingAppTokenWindows(callback, traverseTopToBottom) || super.forAllWindows((ToBooleanFunction) callback, traverseTopToBottom)) {
                return true;
            }
            return false;
        }

        private boolean forAllExitingAppTokenWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
            int i;
            AppTokenList appTokens;
            int j;
            if (traverseTopToBottom) {
                for (i = this.mChildren.size() - 1; i >= 0; i--) {
                    appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                    for (j = appTokens.size() - 1; j >= 0; j--) {
                        if (((AppWindowToken) appTokens.get(j)).forAllWindowsUnchecked(callback, traverseTopToBottom)) {
                            return true;
                        }
                    }
                }
            } else {
                int count = this.mChildren.size();
                for (i = 0; i < count; i++) {
                    appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                    int appTokensCount = appTokens.size();
                    for (j = 0; j < appTokensCount; j++) {
                        if (((AppWindowToken) appTokens.get(j)).forAllWindowsUnchecked(callback, traverseTopToBottom)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        void setExitingTokensHasVisible(boolean hasVisible) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                for (int j = appTokens.size() - 1; j >= 0; j--) {
                    ((AppWindowToken) appTokens.get(j)).hasVisible = hasVisible;
                }
            }
        }

        void removeExistingAppTokensIfPossible() {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                for (int j = appTokens.size() - 1; j >= 0; j--) {
                    AppWindowToken token = (AppWindowToken) appTokens.get(j);
                    if (!(token.hasVisible || (DisplayContent.this.mService.mClosingApps.contains(token) ^ 1) == 0 || (token.mIsExiting && !token.isEmpty()))) {
                        token.mAppAnimator.clearAnimation();
                        token.mAppAnimator.animating = false;
                        Slog.v(DisplayContent.TAG, "performLayout: App token exiting now removed" + token);
                        token.removeIfPossible();
                    }
                }
            }
        }

        int getOrientation() {
            int orientation;
            if (DisplayContent.this.isStackVisible(3) || DisplayContent.this.isStackVisible(2)) {
                if (DisplayContent.this.mHomeStack != null && DisplayContent.this.mHomeStack.isVisible() && DisplayContent.this.mDividerControllerLocked.isMinimizedDock()) {
                    orientation = DisplayContent.this.mHomeStack.getOrientation();
                    if (orientation != -2) {
                        return orientation;
                    }
                }
                return -1;
            }
            orientation = super.getOrientation();
            if (orientation == -2 || orientation == 3) {
                return DisplayContent.this.mLastOrientation;
            }
            return orientation;
        }
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    boolean isCoverOpen() {
        return this.mService.isCoverOpen();
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_18842(WindowState w) {
        WindowStateAnimator winAnimator = w.mWinAnimator;
        if (winAnimator.hasSurface()) {
            boolean wasAnimating = winAnimator.mWasAnimating;
            boolean nowAnimating = winAnimator.stepAnimationLocked(this.mTmpWindowAnimator.mCurrentTime);
            winAnimator.mWasAnimating = nowAnimating;
            this.mTmpWindowAnimator.orAnimating(nowAnimating);
            if (wasAnimating && (winAnimator.mAnimating ^ 1) != 0 && this.mWallpaperController.isWallpaperTarget(w)) {
                WindowAnimator windowAnimator = this.mTmpWindowAnimator;
                windowAnimator.mBulkUpdateParams |= 2;
                this.pendingLayoutChanges |= 4;
            }
        }
        AppWindowToken atoken = w.mAppToken;
        if (winAnimator.mDrawState == 3 && ((atoken == null || atoken.allDrawn) && w.performShowLocked())) {
            this.pendingLayoutChanges |= 8;
        }
        AppWindowAnimator appAnimator = winAnimator.mAppAnimator;
        if (appAnimator != null && appAnimator.thumbnail != null) {
            if (appAnimator.thumbnailTransactionSeq != this.mTmpWindowAnimator.mAnimTransactionSequence) {
                appAnimator.thumbnailTransactionSeq = this.mTmpWindowAnimator.mAnimTransactionSequence;
                appAnimator.thumbnailLayer = 0;
            }
            if (appAnimator.thumbnailLayer < winAnimator.mAnimLayer) {
                appAnimator.thumbnailLayer = winAnimator.mAnimLayer;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_21155(WindowState w) {
        WindowStateAnimator winAnimator = w.mWinAnimator;
        if (winAnimator.mSurfaceController != null && (winAnimator.hasSurface() ^ 1) == 0) {
            int color;
            TaskStack stack;
            int flags = w.mAttrs.flags;
            if (winAnimator.mAnimating) {
                if (winAnimator.mAnimation != null) {
                    if ((flags & DumpState.DUMP_DEXOPT) != 0 && winAnimator.mAnimation.getDetachWallpaper()) {
                        this.mTmpWindow = w;
                    }
                    color = winAnimator.mAnimation.getBackgroundColor();
                    if (color != 0) {
                        stack = w.getStack();
                        if (stack != null) {
                            stack.setAnimationBackground(winAnimator, color);
                        }
                    }
                }
                this.mTmpWindowAnimator.setAnimating(true);
            }
            AppWindowAnimator appAnimator = winAnimator.mAppAnimator;
            if (!(appAnimator == null || appAnimator.animation == null || !appAnimator.animating)) {
                if ((flags & DumpState.DUMP_DEXOPT) != 0 && appAnimator.animation.getDetachWallpaper()) {
                    this.mTmpWindow = w;
                }
                color = appAnimator.animation.getBackgroundColor();
                if (color != 0) {
                    stack = w.getStack();
                    if (stack != null) {
                        stack.setAnimationBackground(winAnimator, color);
                    }
                }
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_23104(WindowState w) {
        int lostFocusUid = this.mTmpWindow.mOwnerUid;
        Handler handler = this.mService.mH;
        if (w.mAttrs.type == 2005 && w.mOwnerUid == lostFocusUid && !handler.hasMessages(52, w)) {
            handler.sendMessageDelayed(handler.obtainMessage(52, w), w.mAttrs.hideTimeoutMilliseconds);
        }
    }

    /* synthetic */ boolean lambda$-com_android_server_wm_DisplayContent_23600(WindowState w) {
        AppWindowToken focusedApp = this.mService.mFocusedApp;
        if (!w.canReceiveKeys()) {
            return false;
        }
        if (HwPCUtils.isPcCastModeInServer() && (this.isDefaultDisplay ^ 1) != 0) {
            if (this.mService.getPCLauncherFocused() && w.mAttrs != null && w.mAttrs.type != 2103 && w.mAppToken != null) {
                return false;
            }
            if (HwPCUtils.enabledInPad() && !this.mService.mPolicy.isKeyguardLocked() && w.mAttrs != null && w.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
                HwPCUtils.log(TAG, "Skipping " + w + ", flags=" + w.mAttrs.flags + ", canReceive=" + w.canReceiveKeys());
                return false;
            }
        }
        AppWindowToken wtoken = w.mAppToken;
        if (wtoken != null && (wtoken.removed || wtoken.sendingToBottom)) {
            return false;
        }
        if (focusedApp == null) {
            this.mTmpWindow = w;
            return true;
        } else if (!focusedApp.windowsAreFocusable()) {
            this.mTmpWindow = w;
            return true;
        } else if (wtoken == null || w.mAttrs.type == 3 || focusedApp.compareTo((WindowContainer) wtoken) <= 0) {
            this.mTmpWindow = w;
            return true;
        } else {
            Slog.v(TAG, "findFocusedWindow: " + wtoken + " below Reached focused app=" + focusedApp);
            this.mTmpWindow = null;
            return true;
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_26653(WindowState w) {
        w.mWinAnimator.prepareSurfaceLocked(true);
        if (w.mWinAnimator.mLazyIsExiting || w.mWinAnimator.mLazyIsEntering) {
            this.mTmpWindowAnimator.setIsLazying(true);
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_27073(WindowState w) {
        boolean ggGone;
        boolean gone;
        int isCoverOpen = IS_TABLET ? !isCoverOpen() ? this.mService.mPowerManager.isScreenOn() : 1 : isCoverOpen();
        if (this.mTmpInitial && IS_DEBUG_VERSION) {
            ArrayMap<String, Object> params = new ArrayMap();
            params.put("checkType", "HighWindowLayerScene");
            params.put("number", Integer.valueOf(this.index[0]));
            params.put("windowState", w);
            if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                HwServiceFactory.getWinFreezeScreenMonitor().checkFreezeScreen(params);
            }
            this.index[0] = this.index[0] + 1;
        }
        if (this.mTmpWindow == null || !this.mService.mPolicy.canBeHiddenByKeyguardLw(w)) {
            ggGone = w.isGoneForLayoutLw();
        } else {
            ggGone = true;
        }
        if ((!ggGone && (isCoverOpen ^ 1) == 0) || w.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || w.mAttrs.type == 2101) {
            gone = false;
        } else if (w.mAttrs.type != 2100) {
            gone = true;
        } else {
            gone = false;
        }
        if (gone && (w.mHaveFrame ^ 1) == 0 && !w.mLayoutNeeded) {
            if ((!w.isConfigChanged() && !w.setReportResizeHints()) || (w.isGoneForLayoutLw() ^ 1) == 0) {
                return;
            }
            if ((w.mAttrs.privateFlags & 1024) == 0 && !(w.mHasSurface && w.mAppToken != null && w.mAppToken.layoutConfigChanges)) {
                return;
            }
        }
        if (!w.mLayoutAttached) {
            if (this.mTmpInitial) {
                w.mContentChanged = false;
            }
            if (w.mAttrs.type == 2023) {
                this.mTmpWindow = w;
            }
            w.mLayoutNeeded = false;
            w.prelayout();
            boolean firstLayout = w.isLaidOut() ^ 1;
            this.mService.mPolicy.layoutWindowLw(w, null);
            w.mLayoutSeq = this.mService.mLayoutSeq;
            if (firstLayout) {
                w.updateLastInsetValues();
            }
            Task task = w.getTask();
            if (task != null) {
                this.mDimLayerController.updateDimLayer(task);
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_32034(WindowState w) {
        if (w.mLayoutAttached) {
            if (this.mTmpWindow != null && this.mService.mPolicy.canBeHiddenByKeyguardLw(w)) {
                return;
            }
            if ((w.mViewVisibility != 8 && w.mRelayoutCalled) || (w.mHaveFrame ^ 1) != 0 || w.mLayoutNeeded) {
                if (this.mTmpInitial) {
                    w.mContentChanged = false;
                }
                w.mLayoutNeeded = false;
                w.prelayout();
                this.mService.mPolicy.layoutWindowLw(w, w.getParentWindow());
                w.mLayoutSeq = this.mService.mLayoutSeq;
            }
        } else if (w.mAttrs.type == 2023) {
            this.mTmpWindow = this.mTmpWindow2;
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_34058(WindowState w) {
        this.mService.mPolicy.applyPostLayoutPolicyLw(w, w.mAttrs, w.getParentWindow(), this.mService.mInputMethodTarget);
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_34263(WindowState w) {
        WindowSurfacePlacer surfacePlacer = this.mService.mWindowPlacerLocked;
        boolean obscuredChanged = w.mObscured != this.mTmpApplySurfaceChangesTransactionState.obscured;
        RootWindowContainer root = this.mService.mRoot;
        boolean someoneLosingFocus = this.mService.mLosingFocus.isEmpty() ^ 1;
        w.mObscured = this.mTmpApplySurfaceChangesTransactionState.obscured;
        if (!this.mTmpApplySurfaceChangesTransactionState.obscured) {
            boolean isDisplayed = w.isDisplayedLw();
            if (isDisplayed && w.isObscuringDisplay()) {
                root.mObscuringWindow = w;
                this.mTmpApplySurfaceChangesTransactionState.obscured = true;
            }
            ApplySurfaceChangesTransactionState applySurfaceChangesTransactionState = this.mTmpApplySurfaceChangesTransactionState;
            applySurfaceChangesTransactionState.displayHasContent |= root.handleNotObscuredLocked(w, this.mTmpApplySurfaceChangesTransactionState.obscured, this.mTmpApplySurfaceChangesTransactionState.syswin);
            if (w.mHasSurface && isDisplayed) {
                int type = w.mAttrs.type;
                if (type == 2008 || type == 2010 || (w.mAttrs.privateFlags & 1024) != 0) {
                    this.mTmpApplySurfaceChangesTransactionState.syswin = true;
                }
                if (this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate == 0.0f && w.mAttrs.preferredRefreshRate != 0.0f) {
                    this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate = w.mAttrs.preferredRefreshRate;
                }
                if (this.mTmpApplySurfaceChangesTransactionState.preferredModeId == 0 && w.mAttrs.preferredDisplayModeId != 0) {
                    this.mTmpApplySurfaceChangesTransactionState.preferredModeId = w.mAttrs.preferredDisplayModeId;
                }
            }
        }
        w.applyDimLayerIfNeeded();
        if (this.isDefaultDisplay && obscuredChanged && w.isVisibleLw() && this.mWallpaperController.isWallpaperTarget(w)) {
            this.mWallpaperController.updateWallpaperVisibility();
        }
        w.handleWindowMovedIfNeeded();
        WindowStateAnimator winAnimator = w.mWinAnimator;
        w.mContentChanged = false;
        if (w.mHasSurface) {
            boolean committed = winAnimator.commitFinishDrawingLocked();
            if (this.isDefaultDisplay && committed) {
                if (w.mAttrs.type == 2023) {
                    this.pendingLayoutChanges |= 1;
                }
                if ((w.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                    root.mWallpaperMayChange = true;
                    this.pendingLayoutChanges |= 4;
                }
            }
            TaskStack stack = w.getStack();
            if (!(winAnimator.isAnimationStarting() || (winAnimator.isWaitingForOpening() ^ 1) == 0) || (stack != null && stack.isAnimatingBounds())) {
                winAnimator.computeShownFrameLocked();
            }
            winAnimator.setSurfaceBoundariesLocked(this.mTmpRecoveringMemory);
        }
        AppWindowToken atoken = w.mAppToken;
        if (!(atoken == null || !atoken.updateDrawnWindowStates(w) || (this.mTmpUpdateAllDrawn.contains(atoken) ^ 1) == 0)) {
            this.mTmpUpdateAllDrawn.add(atoken);
        }
        if (this.isDefaultDisplay && someoneLosingFocus && w == this.mService.mCurrentFocus && w.isDisplayedLw()) {
            this.mTmpApplySurfaceChangesTransactionState.focusDisplayed = true;
        }
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(this.mDisplayId) && someoneLosingFocus && w == this.mService.mCurrentFocus && w.isDisplayedLw()) {
            this.mTmpApplySurfaceChangesTransactionState.focusDisplayed = true;
        }
        w.updateResizingWindowIfNeeded();
    }

    public DisplayContent(Display display, WindowManagerService service, WindowLayersController layersController, WallpaperController wallpaperController) {
        if (service.mRoot.getDisplayContent(display.getDisplayId()) != null) {
            throw new IllegalArgumentException("Display with ID=" + display.getDisplayId() + " already exists=" + service.mRoot.getDisplayContent(display.getDisplayId()) + " new=" + display);
        }
        this.mDisplay = display;
        this.mDisplayId = display.getDisplayId();
        this.mLayersController = layersController;
        this.mWallpaperController = wallpaperController;
        display.getDisplayInfo(this.mDisplayInfo);
        display.getMetrics(this.mDisplayMetrics);
        this.isDefaultDisplay = this.mDisplayId == 0;
        this.mService = service;
        if (!(this.isDefaultDisplay || ("local:100000".equals(this.mDisplayInfo.uniqueId) ^ 1) == 0 || SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90 != 1)) {
            this.mRotation = 0;
        }
        initializeDisplayBaseInfo();
        this.mDividerControllerLocked = new DockedStackDividerController(service, this);
        this.mPinnedStackControllerLocked = new PinnedStackController(service, this);
        this.mDimLayerController = new DimLayerController(this);
        super.addChild(this.mBelowAppWindowsContainers, null);
        super.addChild(this.mTaskStackContainers, null);
        super.addChild(this.mAboveAppWindowsContainers, null);
        super.addChild(this.mImeWindowsContainers, null);
        this.mService.mRoot.addChild((WindowContainer) this, null);
        this.mDisplayReady = true;
    }

    boolean isReady() {
        return this.mService.mDisplayReady ? this.mDisplayReady : false;
    }

    int getDisplayId() {
        return this.mDisplayId;
    }

    WindowToken getWindowToken(IBinder binder) {
        return (WindowToken) this.mTokenMap.get(binder);
    }

    AppWindowToken getAppWindowToken(IBinder binder) {
        WindowToken token = getWindowToken(binder);
        if (token == null) {
            return null;
        }
        return token.asAppWindowToken();
    }

    private void addWindowToken(IBinder binder, WindowToken token) {
        DisplayContent dc = this.mService.mRoot.getWindowTokenDisplay(token);
        if (dc != null) {
            throw new IllegalArgumentException("Can't map token=" + token + " to display=" + getName() + " already mapped to display=" + dc + " tokens=" + dc.mTokenMap);
        } else if (binder == null) {
            throw new IllegalArgumentException("Can't map token=" + token + " to display=" + getName() + " binder is null");
        } else if (token == null) {
            throw new IllegalArgumentException("Can't map null token to display=" + getName() + " binder=" + binder);
        } else {
            this.mTokenMap.put(binder, token);
            if (token.asAppWindowToken() == null) {
                switch (token.windowType) {
                    case 2011:
                    case 2012:
                        this.mImeWindowsContainers.addChild(token);
                        return;
                    case 2013:
                    case 2103:
                        this.mBelowAppWindowsContainers.addChild(token);
                        return;
                    default:
                        this.mAboveAppWindowsContainers.addChild(token);
                        return;
                }
            }
        }
    }

    WindowToken removeWindowToken(IBinder binder) {
        WindowToken token = (WindowToken) this.mTokenMap.remove(binder);
        if (token != null && token.asAppWindowToken() == null) {
            token.setExiting();
            if (token.isEmpty()) {
                token.removeImmediately();
            }
        }
        return token;
    }

    void reParentWindowToken(WindowToken token) {
        DisplayContent prevDc = token.getDisplayContent();
        if (prevDc != this) {
            if (!(prevDc == null || prevDc.mTokenMap.remove(token.token) == null || token.asAppWindowToken() != null)) {
                token.getParent().removeChild(token);
            }
            addWindowToken(token.token, token);
        }
    }

    void removeAppToken(IBinder binder) {
        WindowToken token = removeWindowToken(binder);
        if (token == null) {
            Slog.w(TAG, "removeAppToken: Attempted to remove non-existing token: " + binder);
            return;
        }
        AppWindowToken appToken = token.asAppWindowToken();
        if (appToken == null) {
            Slog.w(TAG, "Attempted to remove non-App token: " + binder + " token=" + token);
        } else {
            appToken.onRemovedFromDisplay();
        }
    }

    Display getDisplay() {
        return this.mDisplay;
    }

    DisplayInfo getDisplayInfo() {
        return this.mDisplayInfo;
    }

    DisplayMetrics getDisplayMetrics() {
        return this.mDisplayMetrics;
    }

    int getRotation() {
        return this.mRotation;
    }

    void setRotation(int newRotation) {
        this.mRotation = newRotation;
    }

    int getLastOrientation() {
        return this.mLastOrientation;
    }

    void setLastOrientation(int orientation) {
        this.mLastOrientation = orientation;
    }

    boolean getAltOrientation() {
        return this.mAltOrientation;
    }

    void setAltOrientation(boolean altOrientation) {
        this.mAltOrientation = altOrientation;
    }

    int getLastWindowForcedOrientation() {
        return this.mLastWindowForcedOrientation;
    }

    protected boolean updateRotationUnchecked(boolean inTransaction) {
        if (this.mService.mDeferredRotationPauseCount > 0) {
            Slog.i(TAG, "Deferring rotation, rotation is paused.");
            return false;
        }
        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
        if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
            return false;
        }
        if (!this.mService.mDisplayEnabled) {
            return false;
        }
        int oldRotation = this.mRotation;
        int lastOrientation = this.mLastOrientation;
        boolean oldAltOrientation = this.mAltOrientation;
        int rotation = this.mService.mPolicy.rotationForOrientationLw(lastOrientation, oldRotation);
        boolean rotateSeamlessly = this.mService.mPolicy.shouldRotateSeamlessly(oldRotation, rotation);
        if (rotateSeamlessly) {
            if (getWindow(new Predicate() {
                public final boolean test(Object obj) {
                    return $m$0(obj);
                }
            }) != null) {
                return false;
            }
        }
        boolean altOrientation = this.mService.mPolicy.rotationHasCompatibleMetricsLw(lastOrientation, rotation) ^ 1;
        Slog.i(TAG, "Selected orientation " + lastOrientation + ", got rotation " + rotation + " which has " + (altOrientation ? "incompatible" : "compatible") + " metrics");
        if (oldRotation == rotation && oldAltOrientation == altOrientation) {
            return false;
        }
        Slog.i(TAG, "Rotation changed to " + rotation + (altOrientation ? " (alt)" : "") + " from " + oldRotation + (oldAltOrientation ? " (alt)" : "") + ", lastOrientation=" + lastOrientation);
        if (deltaRotation(rotation, oldRotation) != 2) {
            this.mService.mWaitingForConfig = true;
        }
        this.mRotation = rotation;
        setDisplayRotationFR(this.mRotation);
        this.mAltOrientation = altOrientation;
        if (this.isDefaultDisplay) {
            this.mService.mPolicy.setRotationLw(rotation);
        }
        this.mService.mWindowsFreezingScreen = 1;
        this.mService.mH.removeMessages(11);
        this.mService.mH.sendEmptyMessageDelayed(11, 2000);
        setLayoutNeeded();
        int[] anim = new int[2];
        if (isDimming()) {
            anim[1] = 0;
            anim[0] = 0;
        } else {
            this.mService.mPolicy.selectRotationAnimationLw(anim);
        }
        if (rotateSeamlessly) {
            screenRotationAnimation = null;
            this.mService.mSeamlessRotationCount = 0;
        } else {
            this.mService.startFreezingDisplayLocked(inTransaction, anim[0], anim[1], this);
            screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
        }
        updateDisplayAndOrientation(getConfiguration().uiMode);
        if (!inTransaction) {
            this.mService.openSurfaceTransaction();
        }
        if (screenRotationAnimation != null) {
            try {
                if (screenRotationAnimation.hasScreenshot() && screenRotationAnimation.setRotationInTransaction(rotation, this.mService.mFxSession, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY, this.mService.getTransitionAnimationScaleLocked(), this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight)) {
                    this.mService.scheduleAnimationLocked();
                }
            } catch (RuntimeException e) {
                Slog.wtf(TAG, "Display exception in Window Manager", e);
                if (!inTransaction) {
                    this.mService.closeSurfaceTransaction();
                }
            } catch (Throwable th) {
                if (!inTransaction) {
                    this.mService.closeSurfaceTransaction();
                }
            }
        }
        if (rotateSeamlessly) {
            forAllWindows((Consumer) new AnonymousClass29(oldRotation, rotation), true);
        }
        this.mService.mDisplayManagerInternal.performTraversalInTransactionFromWindowManager();
        if (!inTransaction) {
            this.mService.closeSurfaceTransaction();
        }
        forAllWindows((Consumer) new AnonymousClass32(rotateSeamlessly, this), true);
        if (rotateSeamlessly) {
            this.mService.mH.removeMessages(54);
            this.mService.mH.sendEmptyMessageDelayed(54, 2000);
        }
        for (int i = this.mService.mRotationWatchers.size() - 1; i >= 0; i--) {
            RotationWatcher rotationWatcher = (RotationWatcher) this.mService.mRotationWatchers.get(i);
            if (rotationWatcher.mDisplayId == this.mDisplayId) {
                try {
                    rotationWatcher.mWatcher.onRotationChanged(rotation);
                } catch (RemoteException e2) {
                }
            }
        }
        if (screenRotationAnimation == null && this.mService.mAccessibilityController != null && this.isDefaultDisplay) {
            this.mService.mAccessibilityController.onRotationChangedLocked(this);
        }
        return true;
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_57485(boolean rotateSeamlessly, WindowState w) {
        if (w.mAppToken != null) {
            w.mAppToken.destroySavedSurfaces();
        }
        if (w.mHasSurface && (rotateSeamlessly ^ 1) != 0) {
            w.mOrientationChanging = true;
            this.mService.mRoot.mOrientationChangeComplete = false;
            w.mLastFreezeDuration = 0;
        }
        w.mReportOrientationChanged = true;
    }

    private DisplayInfo updateDisplayAndOrientation(int uiMode) {
        boolean rotated = this.mRotation == 1 || this.mRotation == 3;
        int realdw = rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth;
        int realdh = rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight;
        int dw = realdw;
        int dh = realdh;
        if (this.mAltOrientation) {
            if (realdw > realdh) {
                int maxw = (int) (((float) realdh) / 1.3f);
                if (maxw < realdw) {
                    dw = maxw;
                }
            } else {
                int maxh = (int) (((float) realdw) / 1.3f);
                if (maxh < realdh) {
                    dh = maxh;
                }
            }
        }
        int appWidth = this.mService.mPolicy.getNonDecorDisplayWidth(dw, dh, this.mRotation, uiMode, this.mDisplayId);
        int appHeight = this.mService.mPolicy.getNonDecorDisplayHeight(dw, dh, this.mRotation, uiMode, this.mDisplayId);
        this.mDisplayInfo.rotation = this.mRotation;
        this.mDisplayInfo.logicalWidth = dw;
        this.mDisplayInfo.logicalHeight = dh;
        this.mDisplayInfo.logicalDensityDpi = this.mBaseDisplayDensity;
        this.mDisplayInfo.appWidth = appWidth;
        this.mDisplayInfo.appHeight = appHeight;
        if (this.isDefaultDisplay) {
            this.mDisplayInfo.getLogicalMetrics(this.mRealDisplayMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
        }
        this.mDisplayInfo.getAppMetrics(this.mDisplayMetrics);
        DisplayInfo displayInfo;
        if (this.mDisplayScalingDisabled) {
            displayInfo = this.mDisplayInfo;
            displayInfo.flags |= 1073741824;
        } else {
            displayInfo = this.mDisplayInfo;
            displayInfo.flags &= -1073741825;
        }
        this.mService.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(this.mDisplayId, this.mDisplayInfo);
        this.mBaseDisplayRect.set(0, 0, dw, dh);
        if (this.isDefaultDisplay) {
            DisplayMetrics dm = this.mDisplayMetrics;
            this.mCompatibleScreenScale = CompatibilityInfo.computeCompatibleScaling(this.mDisplayMetrics, this.mCompatDisplayMetrics);
        }
        return this.mDisplayInfo;
    }

    void computeScreenConfiguration(Configuration config) {
        int i;
        DisplayInfo displayInfo = updateDisplayAndOrientation(config.uiMode);
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        if (dw <= dh) {
            i = 1;
        } else {
            i = 2;
        }
        config.orientation = i;
        config.screenWidthDp = (int) (((float) this.mService.mPolicy.getConfigDisplayWidth(dw, dh, displayInfo.rotation, config.uiMode, this.mDisplayId)) / this.mDisplayMetrics.density);
        config.screenHeightDp = (int) (((float) this.mService.mPolicy.getConfigDisplayHeight(dw, dh, displayInfo.rotation, config.uiMode, this.mDisplayId)) / this.mDisplayMetrics.density);
        this.mService.mPolicy.getNonDecorInsetsLw(displayInfo.rotation, dw, dh, this.mTmpRect);
        int leftInset = this.mTmpRect.left;
        int topInset = this.mTmpRect.top;
        config.setAppBounds(leftInset, topInset, displayInfo.appWidth + leftInset, displayInfo.appHeight + topInset);
        boolean rotated = displayInfo.rotation != 1 ? displayInfo.rotation == 3 : true;
        computeSizeRangesAndScreenLayout(displayInfo, this.mDisplayId, rotated, config.uiMode, dw, dh, this.mDisplayMetrics.density, config);
        int i2 = config.screenLayout & -769;
        if ((displayInfo.flags & 16) != 0) {
            i = 512;
        } else {
            i = 256;
        }
        config.screenLayout = i | i2;
        config.compatScreenWidthDp = (int) (((float) config.screenWidthDp) / this.mCompatibleScreenScale);
        config.compatScreenHeightDp = (int) (((float) config.screenHeightDp) / this.mCompatibleScreenScale);
        config.compatSmallestScreenWidthDp = computeCompatSmallestWidth(rotated, config.uiMode, dw, dh, this.mDisplayId);
        config.densityDpi = displayInfo.logicalDensityDpi;
        if (displayInfo.isHdr()) {
            i = 8;
        } else {
            i = 4;
        }
        if (displayInfo.isWideColorGamut()) {
            i2 = 2;
        } else {
            i2 = 1;
        }
        config.colorMode = i | i2;
        config.touchscreen = 1;
        config.keyboard = 1;
        config.navigation = 1;
        int keyboardPresence = 0;
        int navigationPresence = 0;
        InputDevice[] devices = this.mService.mInputManager.getInputDevices();
        int len = devices != null ? devices.length : 0;
        for (int i3 = 0; i3 < len; i3++) {
            InputDevice device = devices[i3];
            if (!device.isVirtual()) {
                int presenceFlag;
                int sources = device.getSources();
                if (device.isExternal()) {
                    presenceFlag = 2;
                } else {
                    presenceFlag = 1;
                }
                if (!this.mService.mIsTouchDevice) {
                    config.touchscreen = 1;
                } else if ((sources & 4098) == 4098) {
                    config.touchscreen = 3;
                }
                if ((65540 & sources) == 65540) {
                    config.navigation = 3;
                    navigationPresence |= presenceFlag;
                } else if ((sources & 513) == 513 && config.navigation == 1) {
                    config.navigation = 2;
                    navigationPresence |= presenceFlag;
                }
                if (device.getKeyboardType() == 2) {
                    config.keyboard = 2;
                    keyboardPresence |= presenceFlag;
                }
            }
        }
        if (config.navigation == 1 && this.mService.mHasPermanentDpad) {
            config.navigation = 2;
            navigationPresence |= 1;
        }
        boolean hardKeyboardAvailable = config.keyboard != 1;
        if (hardKeyboardAvailable != this.mService.mHardKeyboardAvailable) {
            this.mService.mHardKeyboardAvailable = hardKeyboardAvailable;
            this.mService.mH.removeMessages(22);
            this.mService.mH.sendEmptyMessage(22);
        }
        config.keyboardHidden = 1;
        config.hardKeyboardHidden = 1;
        config.navigationHidden = 1;
        this.mService.mPolicy.adjustConfigurationLw(config, keyboardPresence, navigationPresence);
    }

    private int computeCompatSmallestWidth(boolean rotated, int uiMode, int dw, int dh, int displayId) {
        int unrotDw;
        int unrotDh;
        this.mTmpDisplayMetrics.setTo(this.mDisplayMetrics);
        DisplayMetrics tmpDm = this.mTmpDisplayMetrics;
        if (rotated) {
            unrotDw = dh;
            unrotDh = dw;
        } else {
            unrotDw = dw;
            unrotDh = dh;
        }
        return reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(0, 0, uiMode, tmpDm, unrotDw, unrotDh, displayId), 1, uiMode, tmpDm, unrotDh, unrotDw, displayId), 2, uiMode, tmpDm, unrotDw, unrotDh, displayId), 3, uiMode, tmpDm, unrotDh, unrotDw, displayId);
    }

    private int reduceCompatConfigWidthSize(int curSize, int rotation, int uiMode, DisplayMetrics dm, int dw, int dh, int displayId) {
        dm.noncompatWidthPixels = this.mService.mPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode, displayId);
        dm.noncompatHeightPixels = this.mService.mPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode, displayId);
        int size = (int) (((((float) dm.noncompatWidthPixels) / CompatibilityInfo.computeCompatibleScaling(dm, null)) / dm.density) + 0.5f);
        if (curSize == 0 || size < curSize) {
            return size;
        }
        return curSize;
    }

    private void computeSizeRangesAndScreenLayout(DisplayInfo displayInfo, int displayId, boolean rotated, int uiMode, int dw, int dh, float density, Configuration outConfig) {
        int unrotDw;
        int unrotDh;
        if (rotated) {
            unrotDw = dh;
            unrotDh = dw;
        } else {
            unrotDw = dw;
            unrotDh = dh;
        }
        displayInfo.smallestNominalAppWidth = 1073741824;
        displayInfo.smallestNominalAppHeight = 1073741824;
        displayInfo.largestNominalAppWidth = 0;
        displayInfo.largestNominalAppHeight = 0;
        adjustDisplaySizeRanges(displayInfo, displayId, 0, uiMode, unrotDw, unrotDh);
        adjustDisplaySizeRanges(displayInfo, displayId, 1, uiMode, unrotDh, unrotDw);
        adjustDisplaySizeRanges(displayInfo, displayId, 2, uiMode, unrotDw, unrotDh);
        adjustDisplaySizeRanges(displayInfo, displayId, 3, uiMode, unrotDh, unrotDw);
        int sl = reduceConfigLayout(Configuration.resetScreenLayout(outConfig.screenLayout), 0, density, unrotDw, unrotDh, uiMode, displayId);
        sl = reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(sl, 1, density, unrotDh, unrotDw, uiMode, displayId), 2, density, unrotDw, unrotDh, uiMode, displayId), 3, density, unrotDh, unrotDw, uiMode, displayId);
        outConfig.smallestScreenWidthDp = (int) (((float) displayInfo.smallestNominalAppWidth) / density);
        outConfig.screenLayout = sl;
    }

    private int reduceConfigLayout(int curLayout, int rotation, float density, int dw, int dh, int uiMode, int displayId) {
        int w = this.mService.mPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode, displayId);
        int h = this.mService.mPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode, displayId);
        int longSize = w;
        int shortSize = h;
        if (w < h) {
            int tmp = w;
            longSize = h;
            shortSize = w;
        }
        return Configuration.reduceScreenLayout(curLayout, (int) (((float) longSize) / density), (int) (((float) shortSize) / density));
    }

    private void adjustDisplaySizeRanges(DisplayInfo displayInfo, int displayId, int rotation, int uiMode, int dw, int dh) {
        int width = this.mService.mPolicy.getConfigDisplayWidth(dw, dh, rotation, uiMode, displayId);
        if (width < displayInfo.smallestNominalAppWidth) {
            displayInfo.smallestNominalAppWidth = width;
        }
        if (width > displayInfo.largestNominalAppWidth) {
            displayInfo.largestNominalAppWidth = width;
        }
        int height = this.mService.mPolicy.getConfigDisplayHeight(dw, dh, rotation, uiMode, displayId);
        if (height < displayInfo.smallestNominalAppHeight) {
            displayInfo.smallestNominalAppHeight = height;
        }
        if (height > displayInfo.largestNominalAppHeight) {
            displayInfo.largestNominalAppHeight = height;
        }
    }

    DockedStackDividerController getDockedDividerController() {
        return this.mDividerControllerLocked;
    }

    PinnedStackController getPinnedStackController() {
        return this.mPinnedStackControllerLocked;
    }

    boolean hasAccess(int uid) {
        return this.mDisplay.hasAccess(uid);
    }

    boolean isPrivate() {
        return (this.mDisplay.getFlags() & 4) != 0;
    }

    TaskStack getHomeStack() {
        if (this.mHomeStack == null && this.mDisplayId == 0) {
            Slog.e(TAG, "getHomeStack: Returning null from this=" + this);
        }
        return this.mHomeStack;
    }

    TaskStack getStackById(int stackId) {
        for (int i = this.mTaskStackContainers.size() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.get(i);
            if (stack.mStackId == stackId) {
                return stack;
            }
        }
        return null;
    }

    int getStackCount() {
        return this.mTaskStackContainers.size();
    }

    int getStaskPosById(int stackId) {
        for (int i = this.mTaskStackContainers.size() - 1; i >= 0; i--) {
            if (((TaskStack) this.mTaskStackContainers.get(i)).mStackId == stackId) {
                return i;
            }
        }
        return -1;
    }

    void onConfigurationChanged(Configuration newParentConfig) {
        if (HwPCUtils.enabledInPad() && (HwPCUtils.isPcCastModeInServer() ^ 1) != 0 && (this.isDefaultDisplay ^ 1) != 0 && "local:100000".equals(this.mDisplayInfo.uniqueId) && this.mDisplayInfo.rotation == 1) {
            Slog.v(TAG, "onConfigurationChanged() not handle");
            return;
        }
        super.onConfigurationChanged(newParentConfig);
        this.mService.reconfigureDisplayLocked(this);
        getDockedDividerController().onConfigurationChanged();
        getPinnedStackController().onConfigurationChanged();
    }

    void updateStackBoundsAfterConfigChange(List<Integer> changedStackList) {
        for (int i = this.mTaskStackContainers.size() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.get(i);
            if (stack.updateBoundsAfterConfigChange()) {
                changedStackList.add(Integer.valueOf(stack.mStackId));
            }
        }
        if (getStackById(4) == null) {
            this.mPinnedStackControllerLocked.onDisplayInfoChanged();
        }
    }

    boolean fillsParent() {
        return true;
    }

    boolean isVisible() {
        return true;
    }

    void onAppTransitionDone() {
        super.onAppTransitionDone();
        this.mService.mWindowsChanged = true;
    }

    boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        int i;
        DisplayChildWindowContainer child;
        if (traverseTopToBottom) {
            for (i = this.mChildren.size() - 1; i >= 0; i--) {
                child = (DisplayChildWindowContainer) this.mChildren.get(i);
                if ((child != this.mImeWindowsContainers || this.mService.mInputMethodTarget == null) && child.forAllWindows((ToBooleanFunction) callback, traverseTopToBottom)) {
                    return true;
                }
            }
        } else {
            int count = this.mChildren.size();
            for (i = 0; i < count; i++) {
                child = (DisplayChildWindowContainer) this.mChildren.get(i);
                if ((child != this.mImeWindowsContainers || this.mService.mInputMethodTarget == null) && child.forAllWindows((ToBooleanFunction) callback, traverseTopToBottom)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean forAllImeWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        return this.mImeWindowsContainers.forAllWindows((ToBooleanFunction) callback, traverseTopToBottom);
    }

    int getOrientation() {
        WindowManagerPolicy policy = this.mService.mPolicy;
        if (!this.mService.mDisplayFrozen) {
            int orientation = this.mAboveAppWindowsContainers.getOrientation();
            if (orientation != -2) {
                return orientation;
            }
        } else if (this.mLastWindowForcedOrientation != -1) {
            Slog.v(TAG, "Display is frozen, return " + this.mLastWindowForcedOrientation);
            return this.mLastWindowForcedOrientation;
        } else if (policy.isKeyguardLocked()) {
            return this.mLastOrientation;
        }
        return this.mTaskStackContainers.getOrientation();
    }

    void updateDisplayInfo() {
        updateBaseDisplayMetricsIfNeeded();
        this.mDisplay.getDisplayInfo(this.mDisplayInfo);
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        for (int i = this.mTaskStackContainers.size() - 1; i >= 0; i--) {
            ((TaskStack) this.mTaskStackContainers.get(i)).updateDisplayInfo(null);
        }
    }

    void initializeDisplayBaseInfo() {
        DisplayManagerInternal displayManagerInternal = this.mService.mDisplayManagerInternal;
        if (displayManagerInternal != null) {
            DisplayInfo newDisplayInfo = displayManagerInternal.getDisplayInfo(this.mDisplayId);
            if (newDisplayInfo != null) {
                this.mDisplayInfo.copyFrom(newDisplayInfo);
            }
        }
        updateBaseDisplayMetrics(this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight, this.mDisplayInfo.logicalDensityDpi);
        this.mInitialDisplayWidth = this.mDisplayInfo.logicalWidth;
        this.mInitialDisplayHeight = this.mDisplayInfo.logicalHeight;
        this.mInitialDisplayDensity = this.mDisplayInfo.logicalDensityDpi;
    }

    void getLogicalDisplayRect(Rect out) {
        int orientation = this.mDisplayInfo.rotation;
        boolean rotated = orientation == 1 || orientation == 3;
        int physWidth = rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth;
        int physHeight = rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight;
        int width = this.mDisplayInfo.logicalWidth;
        int left = (physWidth - width) / 2;
        int height = this.mDisplayInfo.logicalHeight;
        int top = (physHeight - height) / 2;
        out.set(left, top, left + width, top + height);
    }

    private void getLogicalDisplayRect(Rect out, int orientation) {
        getLogicalDisplayRect(out);
        int rotationDelta = deltaRotation(this.mDisplayInfo.rotation, orientation);
        if (rotationDelta == 1 || rotationDelta == 3) {
            createRotationMatrix(rotationDelta, (float) this.mBaseDisplayWidth, (float) this.mBaseDisplayHeight, this.mTmpMatrix);
            this.mTmpRectF.set(out);
            this.mTmpMatrix.mapRect(this.mTmpRectF);
            this.mTmpRectF.round(out);
        }
    }

    private void updateBaseDisplayMetricsIfNeeded() {
        DisplayInfo newDisplayInfo = new DisplayInfo();
        this.mService.mDisplayManagerInternal.getNonOverrideDisplayInfo(this.mDisplayId, newDisplayInfo);
        int orientation = newDisplayInfo.rotation;
        boolean rotated = orientation == 1 || orientation == 3;
        int newWidth = rotated ? newDisplayInfo.logicalHeight : newDisplayInfo.logicalWidth;
        int newHeight = rotated ? newDisplayInfo.logicalWidth : newDisplayInfo.logicalHeight;
        int newDensity = newDisplayInfo.logicalDensityDpi;
        boolean displayMetricsChanged = (this.mInitialDisplayWidth == newWidth && this.mInitialDisplayHeight == newHeight) ? this.mInitialDisplayDensity != newDensity : true;
        if (displayMetricsChanged) {
            int i;
            int i2;
            int i3;
            Slog.v(TAG, "the display metrics changed.  mInitialDisplayWidth = " + this.mInitialDisplayWidth + "; mInitialDisplayHeight = " + this.mInitialDisplayHeight + "; mInitialDisplayDensity = " + this.mInitialDisplayDensity + "; newWidth = " + newWidth + "; newHeight = " + newHeight + "; newDensity = " + newDensity + "; mBaseDisplayWidth = " + this.mBaseDisplayWidth + "; mBaseDisplayHeight = " + this.mBaseDisplayHeight + "; mBaseDisplayDensity = " + this.mBaseDisplayDensity + "; rotated = " + rotated + "; mDisplayId = " + this.mDisplayId, new Exception());
            boolean isDisplaySizeForced = this.mBaseDisplayWidth == this.mInitialDisplayWidth ? this.mBaseDisplayHeight != this.mInitialDisplayHeight : true;
            boolean isDisplayDensityForced = this.mBaseDisplayDensity != this.mInitialDisplayDensity;
            if (isDisplaySizeForced) {
                i = this.mBaseDisplayWidth;
            } else {
                i = newWidth;
            }
            if (isDisplaySizeForced) {
                i2 = this.mBaseDisplayHeight;
            } else {
                i2 = newHeight;
            }
            if (isDisplayDensityForced) {
                i3 = this.mBaseDisplayDensity;
            } else {
                i3 = newDensity;
            }
            updateBaseDisplayMetrics(i, i2, i3);
            this.mInitialDisplayWidth = newWidth;
            this.mInitialDisplayHeight = newHeight;
            this.mInitialDisplayDensity = newDensity;
            this.mService.reconfigureDisplayLocked(this);
        }
    }

    void setMaxUiWidth(int width) {
        this.mMaxUiWidth = width;
        updateBaseDisplayMetrics(this.mBaseDisplayWidth, this.mBaseDisplayHeight, this.mBaseDisplayDensity);
    }

    void updateBaseDisplayMetrics(int baseWidth, int baseHeight, int baseDensity) {
        this.mBaseDisplayWidth = baseWidth;
        this.mBaseDisplayHeight = baseHeight;
        this.mBaseDisplayDensity = baseDensity;
        if (this.mMaxUiWidth > 0 && this.mBaseDisplayWidth > this.mMaxUiWidth) {
            this.mBaseDisplayHeight = (this.mMaxUiWidth * this.mBaseDisplayHeight) / this.mBaseDisplayWidth;
            this.mBaseDisplayDensity = (this.mMaxUiWidth * this.mBaseDisplayDensity) / this.mBaseDisplayWidth;
            this.mBaseDisplayWidth = this.mMaxUiWidth;
        }
        this.mBaseDisplayRect.set(0, 0, this.mBaseDisplayWidth, this.mBaseDisplayHeight);
    }

    void getContentRect(Rect out) {
        out.set(this.mContentRect);
    }

    TaskStack addStackToDisplay(int stackId, boolean onTop) {
        TaskStack stack = getStackById(stackId);
        if (stack != null) {
            int i;
            stack.mDeferRemoval = false;
            TaskStackContainers taskStackContainers = this.mTaskStackContainers;
            if (onTop) {
                i = HwBootFail.STAGE_BOOT_SUCCESS;
            } else {
                i = Integer.MIN_VALUE;
            }
            taskStackContainers.positionChildAt(i, stack, false);
        } else {
            if (HwPCUtils.isPcDynamicStack(stackId)) {
                stack = HwServiceFactory.createTaskStack(this.mService, stackId);
            } else {
                stack = new TaskStack(this.mService, stackId);
            }
            this.mTaskStackContainers.addStackToDisplay(stack, onTop);
        }
        if (stackId == 3) {
            this.mDividerControllerLocked.notifyDockedStackExistsChanged(true);
        }
        return stack;
    }

    void moveStackToDisplay(TaskStack stack, boolean onTop) {
        DisplayContent prevDc = stack.getDisplayContent();
        if (prevDc == null) {
            throw new IllegalStateException("Trying to move stackId=" + stack.mStackId + " which is not currently attached to any display");
        } else if (prevDc.getDisplayId() == this.mDisplayId) {
            throw new IllegalArgumentException("Trying to move stackId=" + stack.mStackId + " to its current displayId=" + this.mDisplayId);
        } else {
            prevDc.mTaskStackContainers.removeStackFromDisplay(stack);
            this.mTaskStackContainers.addStackToDisplay(stack, onTop);
        }
    }

    protected void addChild(DisplayChildWindowContainer child, Comparator<DisplayChildWindowContainer> comparator) {
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    protected void addChild(DisplayChildWindowContainer child, int index) {
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    protected void removeChild(DisplayChildWindowContainer child) {
        if (this.mRemovingDisplay) {
            super.removeChild(child);
            return;
        }
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    void positionChildAt(int position, DisplayChildWindowContainer child, boolean includingParents) {
        getParent().positionChildAt(position, this, includingParents);
    }

    int taskIdFromPoint(int x, int y) {
        for (int stackNdx = this.mTaskStackContainers.size() - 1; stackNdx >= 0; stackNdx--) {
            int taskId = ((TaskStack) this.mTaskStackContainers.get(stackNdx)).taskIdFromPoint(x, y);
            if (taskId != -1) {
                return taskId;
            }
        }
        return -1;
    }

    Task findTaskForResizePoint(int x, int y) {
        int delta = WindowManagerService.dipToPixel(10, this.mDisplayMetrics);
        this.mTmpTaskForResizePointSearchResult.reset();
        for (int stackNdx = this.mTaskStackContainers.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.get(stackNdx);
            if (stack != null) {
                if (!StackId.isTaskResizeAllowed(stack.mStackId)) {
                    return null;
                }
                stack.findTaskForResizePoint(x, y, delta, this.mTmpTaskForResizePointSearchResult);
                if (this.mTmpTaskForResizePointSearchResult.searchDone) {
                    return this.mTmpTaskForResizePointSearchResult.taskForResize;
                }
            }
        }
        return null;
    }

    void setTouchExcludeRegion(Task focusedTask) {
        if (focusedTask == null) {
            this.mTouchExcludeRegion.setEmpty();
        } else {
            this.mTouchExcludeRegion.set(this.mBaseDisplayRect);
            int delta = WindowManagerService.dipToPixel(10, this.mDisplayMetrics);
            this.mTmpRect2.setEmpty();
            for (int stackNdx = this.mTaskStackContainers.size() - 1; stackNdx >= 0; stackNdx--) {
                ((TaskStack) this.mTaskStackContainers.get(stackNdx)).setTouchExcludeRegion(focusedTask, delta, this.mTouchExcludeRegion, this.mContentRect, this.mTmpRect2);
            }
            if (!this.mTmpRect2.isEmpty()) {
                this.mTouchExcludeRegion.op(this.mTmpRect2, Op.UNION);
            }
        }
        if (HwPCUtils.isPcCastModeInServer()) {
            if (!this.isDefaultDisplay) {
                this.mTouchExcludeRegion.setEmpty();
            }
            this.mPcTouchExcludeRegion.setEmpty();
        }
        WindowState inputMethod = this.mService.mInputMethodWindow;
        if (inputMethod != null && inputMethod.isVisibleLw()) {
            inputMethod.getTouchableRegion(this.mTmpRegion);
            if (HwPCUtils.isPcCastModeInServer()) {
                if (inputMethod.getDisplayId() == this.mDisplayId) {
                    this.mPcTouchExcludeRegion.op(this.mTmpRegion, Op.UNION);
                }
            } else if (inputMethod.getDisplayId() == this.mDisplayId) {
                this.mTouchExcludeRegion.op(this.mTmpRegion, Op.UNION);
            } else {
                inputMethod.getDisplayContent().setTouchExcludeRegion(null);
            }
        }
        if (HwPCUtils.isPcCastModeInServer() && this.mDisplayId == 0) {
            TaskStack taskStack = (TaskStack) this.mTaskStackContainers.getTopChild();
            if (taskStack != null) {
                Task task = (Task) taskStack.getTopChild();
                if (task != null) {
                    AppWindowToken appWindowToken = (AppWindowToken) task.getTopChild();
                    if (!(appWindowToken == null || appWindowToken.appComponentName == null || !"com.huawei.desktop.systemui/com.huawei.systemui.mk.activity.ImitateActivity".equalsIgnoreCase(appWindowToken.appComponentName))) {
                        Rect touchpadBounds = new Rect();
                        task.getDimBounds(touchpadBounds);
                        this.mPcTouchExcludeRegion.op(touchpadBounds, Op.UNION);
                        WindowState touchpadState = (WindowState) appWindowToken.getTopChild();
                        if (touchpadState != null) {
                            WindowState popupWindowState = (WindowState) touchpadState.getTopChild();
                            if (popupWindowState != null && popupWindowState.mAttrs.type == 1000) {
                                Region popupRegion = new Region();
                                popupWindowState.getTouchableRegion(popupRegion);
                                this.mPcTouchExcludeRegion.op(popupRegion, Op.DIFFERENCE);
                            }
                        }
                    }
                }
            }
        }
        for (int i = this.mTapExcludedWindows.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) this.mTapExcludedWindows.get(i);
            if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mDisplayId) || win.isVisible()) {
                win.getTouchableRegion(this.mTmpRegion);
                this.mTouchExcludeRegion.op(this.mTmpRegion, Op.UNION);
                if (HwPCUtils.isPcCastModeInServer() && this.mDisplayId == 0 && win.isVisible() && (this.mPcTouchExcludeRegion.isEmpty() ^ 1) != 0) {
                    this.mPcTouchExcludeRegion.op(this.mTmpRegion, Op.DIFFERENCE);
                }
            }
        }
        if (this.mTapDetector != null) {
            this.mTapDetector.setHwPCTouchExcludeRegion(this.mPcTouchExcludeRegion);
        }
        if (this.mDisplayId == 0 && getDockedStackLocked() != null) {
            this.mDividerControllerLocked.getTouchRegion(this.mTmpRect);
            this.mTmpRegion.set(this.mTmpRect);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Op.UNION);
        }
        if (this.mTapDetector != null) {
            this.mTapDetector.setTouchExcludeRegion(this.mTouchExcludeRegion);
        }
    }

    void switchUser() {
        super.switchUser();
        this.mService.mWindowsChanged = true;
    }

    private void resetAnimationBackgroundAnimator() {
        for (int stackNdx = this.mTaskStackContainers.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mTaskStackContainers.get(stackNdx)).resetAnimationBackgroundAnimator();
        }
    }

    boolean animateDimLayers() {
        return this.mDimLayerController.animateDimLayers();
    }

    private void resetDimming() {
        this.mDimLayerController.resetDimming();
    }

    boolean isDimming() {
        return this.mDimLayerController.isDimming();
    }

    private void stopDimmingIfNeeded() {
        this.mDimLayerController.stopDimmingIfNeeded();
    }

    void removeIfPossible() {
        if (isAnimating()) {
            this.mDeferredRemoval = true;
        } else {
            removeImmediately();
        }
    }

    void removeImmediately() {
        this.mRemovingDisplay = true;
        try {
            super.removeImmediately();
            this.mDimLayerController.close();
            if (this.mDisplayId == 0 && this.mService.canDispatchPointerEvents()) {
                this.mService.unregisterPointerEventListener(this.mTapDetector);
                this.mService.unregisterPointerEventListener(this.mService.mMousePositionTracker);
            }
            if (HwPCUtils.enabled() && HwPCUtils.isValidExtDisplayId(this.mDisplayId) && this.mDisplayId != 0 && this.mService.canDispatchExternalPointerEvents() && this.mTapDetector != null) {
                this.mService.unregisterExternalPointerEventListener(this.mTapDetector);
                try {
                    this.mService.unregisterExternalPointerEventListener(this.mService.mMousePositionTracker);
                } catch (Exception e) {
                }
            }
            this.mRemovingDisplay = false;
        } catch (Throwable th) {
            this.mRemovingDisplay = false;
        }
    }

    boolean checkCompleteDeferredRemoval() {
        if (super.checkCompleteDeferredRemoval() || !this.mDeferredRemoval) {
            return true;
        }
        removeImmediately();
        this.mService.onDisplayRemoved(this.mDisplayId);
        return false;
    }

    boolean isRemovalDeferred() {
        return this.mDeferredRemoval;
    }

    boolean animateForIme(float interpolatedValue, float animationTarget, float dividerAnimationTarget) {
        boolean updated = false;
        for (int i = this.mTaskStackContainers.size() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.get(i);
            if (stack != null && (stack.isAdjustedForIme() ^ 1) == 0) {
                if (interpolatedValue >= 1.0f && animationTarget == 0.0f && dividerAnimationTarget == 0.0f) {
                    stack.resetAdjustedForIme(true);
                    updated = true;
                } else {
                    this.mDividerControllerLocked.mLastAnimationProgress = this.mDividerControllerLocked.getInterpolatedAnimationValue(interpolatedValue);
                    this.mDividerControllerLocked.mLastDividerProgress = this.mDividerControllerLocked.getInterpolatedDividerValue(interpolatedValue);
                    updated |= stack.updateAdjustForIme(this.mDividerControllerLocked.mLastAnimationProgress, this.mDividerControllerLocked.mLastDividerProgress, false);
                }
                if (interpolatedValue >= 1.0f) {
                    stack.endImeAdjustAnimation();
                }
            }
        }
        return updated;
    }

    boolean clearImeAdjustAnimation() {
        boolean changed = false;
        for (int i = this.mTaskStackContainers.size() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.get(i);
            if (stack != null && stack.isAdjustedForIme()) {
                stack.resetAdjustedForIme(true);
                changed = true;
            }
        }
        return changed;
    }

    void beginImeAdjustAnimation() {
        for (int i = this.mTaskStackContainers.size() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.get(i);
            if (stack.isVisible() && stack.isAdjustedForIme()) {
                stack.beginImeAdjustAnimation();
            }
        }
    }

    void adjustForImeIfNeeded() {
        boolean imeVisible;
        WindowState imeWin = this.mService.mInputMethodWindow;
        if (imeWin != null && imeWin.isVisibleLw() && imeWin.isDisplayedLw()) {
            imeVisible = this.mDividerControllerLocked.isImeHideRequested() ^ 1;
        } else {
            imeVisible = false;
        }
        boolean dockVisible = isStackVisible(3);
        TaskStack imeTargetStack = this.mService.getImeFocusStackLocked();
        int imeDockSide = (!dockVisible || imeTargetStack == null) ? -1 : imeTargetStack.getDockSide();
        boolean imeOnTop = imeDockSide == 2;
        boolean imeOnBottom = imeDockSide == 4;
        boolean dockMinimized = this.mDividerControllerLocked.isMinimizedDock();
        int imeHeight = this.mService.mPolicy.getInputMethodWindowVisibleHeightLw();
        boolean imeHeightChanged = imeVisible ? imeHeight != this.mDividerControllerLocked.getImeHeightAdjustedFor() : false;
        int i;
        if (imeVisible && dockVisible && ((imeOnTop || imeOnBottom) && (dockMinimized ^ 1) != 0)) {
            for (i = this.mTaskStackContainers.size() - 1; i >= 0; i--) {
                TaskStack stack = (TaskStack) this.mTaskStackContainers.get(i);
                boolean isDockedOnBottom = stack.getDockSide() == 4;
                if (stack.isVisible() && ((imeOnBottom || isDockedOnBottom) && StackId.isStackAffectedByDragResizing(stack.mStackId))) {
                    boolean z;
                    if (imeOnBottom) {
                        z = imeHeightChanged;
                    } else {
                        z = false;
                    }
                    stack.setAdjustedForIme(imeWin, z);
                } else {
                    stack.resetAdjustedForIme(false);
                }
            }
            this.mDividerControllerLocked.setAdjustedForIme(imeOnBottom, true, true, imeWin, imeHeight);
        } else {
            for (i = this.mTaskStackContainers.size() - 1; i >= 0; i--) {
                ((TaskStack) this.mTaskStackContainers.get(i)).resetAdjustedForIme(dockVisible ^ 1);
            }
            this.mDividerControllerLocked.setAdjustedForIme(false, false, dockVisible, imeWin, imeHeight);
        }
        this.mPinnedStackControllerLocked.setAdjustedForIme(imeVisible, imeHeight);
    }

    void setInputMethodAnimLayerAdjustment(int adj) {
        this.mInputMethodAnimLayerAdjustment = adj;
        assignWindowLayers(false);
    }

    int getLayerForAnimationBackground(WindowStateAnimator winAnimator) {
        WindowState visibleWallpaper = this.mBelowAppWindowsContainers.getWindow(new Predicate() {
            public final boolean test(Object obj) {
                return $m$0(obj);
            }
        });
        if (visibleWallpaper != null) {
            return visibleWallpaper.mWinAnimator.mAnimLayer;
        }
        return winAnimator.mAnimLayer;
    }

    static /* synthetic */ boolean lambda$-com_android_server_wm_DisplayContent_107222(WindowState w) {
        return w.mIsWallpaper ? w.isVisibleNow() : false;
    }

    void prepareFreezingTaskBounds() {
        for (int stackNdx = this.mTaskStackContainers.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mTaskStackContainers.get(stackNdx)).prepareFreezingTaskBounds();
        }
    }

    void rotateBounds(int oldRotation, int newRotation, Rect bounds) {
        getLogicalDisplayRect(this.mTmpRect, newRotation);
        createRotationMatrix(deltaRotation(newRotation, oldRotation), (float) this.mTmpRect.width(), (float) this.mTmpRect.height(), this.mTmpMatrix);
        this.mTmpRectF.set(bounds);
        this.mTmpMatrix.mapRect(this.mTmpRectF);
        this.mTmpRectF.round(bounds);
    }

    static int deltaRotation(int oldRotation, int newRotation) {
        int delta = newRotation - oldRotation;
        if (delta < 0) {
            return delta + 4;
        }
        return delta;
    }

    private static void createRotationMatrix(int rotation, float displayWidth, float displayHeight, Matrix outMatrix) {
        createRotationMatrix(rotation, 0.0f, 0.0f, displayWidth, displayHeight, outMatrix);
    }

    static void createRotationMatrix(int rotation, float rectLeft, float rectTop, float displayWidth, float displayHeight, Matrix outMatrix) {
        switch (rotation) {
            case 0:
                outMatrix.reset();
                return;
            case 1:
                outMatrix.setRotate(90.0f, 0.0f, 0.0f);
                outMatrix.postTranslate(displayWidth, 0.0f);
                outMatrix.postTranslate(-rectTop, rectLeft);
                return;
            case 2:
                outMatrix.reset();
                return;
            case 3:
                outMatrix.setRotate(270.0f, 0.0f, 0.0f);
                outMatrix.postTranslate(0.0f, displayHeight);
                outMatrix.postTranslate(rectTop, 0.0f);
                return;
            default:
                return;
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("Display: mDisplayId=");
        pw.println(this.mDisplayId);
        String subPrefix = "  " + prefix;
        pw.print(subPrefix);
        pw.print("init=");
        pw.print(this.mInitialDisplayWidth);
        pw.print("x");
        pw.print(this.mInitialDisplayHeight);
        pw.print(" ");
        pw.print(this.mInitialDisplayDensity);
        pw.print("dpi");
        if (!(this.mInitialDisplayWidth == this.mBaseDisplayWidth && this.mInitialDisplayHeight == this.mBaseDisplayHeight && this.mInitialDisplayDensity == this.mBaseDisplayDensity)) {
            pw.print(" base=");
            pw.print(this.mBaseDisplayWidth);
            pw.print("x");
            pw.print(this.mBaseDisplayHeight);
            pw.print(" ");
            pw.print(this.mBaseDisplayDensity);
            pw.print("dpi");
        }
        if (this.mDisplayScalingDisabled) {
            pw.println(" noscale");
        }
        pw.print(" cur=");
        pw.print(this.mDisplayInfo.logicalWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.logicalHeight);
        pw.print(" app=");
        pw.print(this.mDisplayInfo.appWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.appHeight);
        pw.print(" rng=");
        pw.print(this.mDisplayInfo.smallestNominalAppWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.smallestNominalAppHeight);
        pw.print("-");
        pw.print(this.mDisplayInfo.largestNominalAppWidth);
        pw.print("x");
        pw.println(this.mDisplayInfo.largestNominalAppHeight);
        pw.println(subPrefix + "deferred=" + this.mDeferredRemoval + " mLayoutNeeded=" + this.mLayoutNeeded);
        pw.println();
        pw.println(prefix + "Application tokens in top down Z order:");
        for (int stackNdx = this.mTaskStackContainers.size() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mTaskStackContainers.get(stackNdx)).dump(prefix + "  ", pw);
        }
        pw.println();
        if (!this.mExitingTokens.isEmpty()) {
            pw.println();
            pw.println("  Exiting tokens:");
            for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
                WindowToken token = (WindowToken) this.mExitingTokens.get(i);
                pw.print("  Exiting #");
                pw.print(i);
                pw.print(' ');
                pw.print(token);
                pw.println(':');
                token.dump(pw, "    ");
            }
        }
        pw.println();
        this.mDimLayerController.dump(prefix, pw);
        pw.println();
        this.mDividerControllerLocked.dump(prefix, pw);
        pw.println();
        this.mPinnedStackControllerLocked.dump(prefix, pw);
        if (this.mInputMethodAnimLayerAdjustment != 0) {
            pw.println(subPrefix + "mInputMethodAnimLayerAdjustment=" + this.mInputMethodAnimLayerAdjustment);
        }
    }

    public String toString() {
        return "Display " + this.mDisplayId + " info=" + this.mDisplayInfo + " stacks=" + this.mChildren;
    }

    String getName() {
        return "Display " + this.mDisplayId + " name=\"" + this.mDisplayInfo.name + "\"";
    }

    boolean isStackVisible(int stackId) {
        TaskStack stack = getStackById(stackId);
        return stack != null ? stack.isVisible() : false;
    }

    TaskStack getDockedStackLocked() {
        TaskStack stack = getStackById(3);
        return (stack == null || !stack.isVisible()) ? null : stack;
    }

    TaskStack getDockedStackIgnoringVisibility() {
        return getStackById(3);
    }

    WindowState getTouchableWinAtPointLocked(float xf, float yf) {
        return getWindow(new AnonymousClass31((int) xf, (int) yf, this));
    }

    /* synthetic */ boolean lambda$-com_android_server_wm_DisplayContent_113683(int x, int y, WindowState w) {
        boolean z = true;
        int flags = w.mAttrs.flags;
        if (!w.isVisibleLw() || (flags & 16) != 0) {
            return false;
        }
        w.getVisibleBounds(this.mTmpRect);
        if (!this.mTmpRect.contains(x, y)) {
            return false;
        }
        w.getTouchableRegion(this.mTmpRegion);
        int touchFlags = flags & 40;
        if (!(this.mTmpRegion.contains(x, y) || touchFlags == 0)) {
            z = false;
        }
        return z;
    }

    boolean canAddToastWindowForUid(int uid) {
        boolean z = true;
        if (getWindow(new AnonymousClass27(uid)) != null) {
            return true;
        }
        if (getWindow(new AnonymousClass28(uid)) != null) {
            z = false;
        }
        return z;
    }

    static /* synthetic */ boolean lambda$-com_android_server_wm_DisplayContent_114587(int uid, WindowState w) {
        return w.mOwnerUid == uid ? w.isFocused() : false;
    }

    static /* synthetic */ boolean lambda$-com_android_server_wm_DisplayContent_114766(int uid, WindowState w) {
        if (w.mAttrs.type == 2005 && w.mOwnerUid == uid && (w.mPermanentlyHidden ^ 1) != 0) {
            return w.mWindowRemovalAllowed ^ 1;
        }
        return false;
    }

    void scheduleToastWindowsTimeoutIfNeededLocked(WindowState oldFocus, WindowState newFocus) {
        if (oldFocus != null && (newFocus == null || newFocus.mOwnerUid != oldFocus.mOwnerUid)) {
            this.mTmpWindow = oldFocus;
            forAllWindows(this.mScheduleToastTimeout, false);
        }
    }

    WindowState findFocusedWindow() {
        this.mTmpWindow = null;
        forAllWindows(this.mFindFocusedWindow, true);
        if (this.mTmpWindow == null) {
            return null;
        }
        return this.mTmpWindow;
    }

    void assignWindowLayers(boolean setLayoutNeeded) {
        this.mLayersController.assignWindowLayers(this);
        if (setLayoutNeeded) {
            setLayoutNeeded();
        }
    }

    void layoutAndAssignWindowLayersIfNeeded() {
        this.mService.mWindowsChanged = true;
        setLayoutNeeded();
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(this.mDisplayId)) {
            this.mService.updateFocusedWindowLocked(3, false);
            assignWindowLayers(false);
        } else if (!this.mService.updateFocusedWindowLocked(3, false)) {
            assignWindowLayers(false);
        }
        this.mService.mInputMonitor.setUpdateInputWindowsNeededLw();
        this.mService.mWindowPlacerLocked.performSurfacePlacement();
        this.mService.mInputMonitor.updateInputWindowsLw(false);
    }

    boolean destroyLeakedSurfaces() {
        this.mTmpWindow = null;
        forAllWindows((Consumer) new AnonymousClass17(this), false);
        if (this.mTmpWindow != null) {
            return true;
        }
        return false;
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_117385(WindowState w) {
        WindowStateAnimator wsa = w.mWinAnimator;
        if (wsa.mSurfaceController != null) {
            if (!this.mService.mSessions.contains(wsa.mSession)) {
                Slog.w(TAG, "LEAKED SURFACE (session doesn't exist): " + w + " surface=" + wsa.mSurfaceController + " token=" + w.mToken + " pid=" + w.mSession.mPid + " uid=" + w.mSession.mUid);
                wsa.destroySurface();
                this.mService.mForceRemoves.add(w);
                this.mTmpWindow = w;
            } else if (w.mAppToken != null && w.mAppToken.isClientHidden()) {
                Slog.w(TAG, "LEAKED SURFACE (app token hidden): " + w + " surface=" + wsa.mSurfaceController + " token=" + w.mAppToken + " saved=" + w.hasSavedSurface());
                wsa.destroySurface();
                this.mTmpWindow = w;
            }
        }
    }

    WindowState computeImeTarget(boolean updateImeTarget) {
        if (this.mService.mInputMethodWindow == null) {
            if (updateImeTarget) {
                setInputMethodTarget(null, this.mService.mInputMethodTargetWaitingAnim, 0);
            }
            return null;
        }
        AppWindowToken token;
        this.mUpdateImeTarget = updateImeTarget;
        WindowState target = getWindow(this.mComputeImeTargetPredicate);
        if (target != null && target.mAttrs.type == 3) {
            token = target.mAppToken;
            if (token != null) {
                WindowState betterTarget = token.getImeTargetBelowWindow(target);
                if (betterTarget != null) {
                    target = betterTarget;
                }
            }
        }
        WindowState curTarget = this.mService.mInputMethodTarget;
        boolean isClosing = curTarget != null ? (curTarget.mAnimatingExit && curTarget.mWinAnimator.getShown()) ? true : curTarget.mService.mClosingApps.contains(curTarget.mAppToken) : false;
        if (curTarget != null && curTarget.isDisplayedLw() && isClosing && (target == null || curTarget.mWinAnimator.mAnimLayer > target.mWinAnimator.mAnimLayer)) {
            return curTarget;
        }
        if (target == null) {
            if (updateImeTarget) {
                setInputMethodTarget(null, this.mService.mInputMethodTargetWaitingAnim, 0);
            }
            return null;
        }
        if (updateImeTarget) {
            int animLayerAdjustment;
            token = curTarget == null ? null : curTarget.mAppToken;
            if (token != null) {
                WindowState highestTarget = null;
                if (token.mAppAnimator.animating || token.mAppAnimator.animation != null) {
                    highestTarget = token.getHighestAnimLayerWindow(curTarget);
                }
                if (highestTarget != null) {
                    if (this.mService.mAppTransition.isTransitionSet()) {
                        setInputMethodTarget(highestTarget, true, this.mInputMethodAnimLayerAdjustment);
                        return highestTarget;
                    } else if (highestTarget.mWinAnimator.isAnimationSet() && highestTarget.mWinAnimator.mAnimLayer > target.mWinAnimator.mAnimLayer) {
                        setInputMethodTarget(highestTarget, true, this.mInputMethodAnimLayerAdjustment);
                        return highestTarget;
                    }
                }
            }
            if (target.mAppToken != null) {
                animLayerAdjustment = target.mAppToken.getAnimLayerAdjustment();
            } else {
                animLayerAdjustment = 0;
            }
            setInputMethodTarget(target, false, animLayerAdjustment);
        }
        return target;
    }

    private void setInputMethodTarget(WindowState target, boolean targetWaitingAnim, int layerAdj) {
        if (target != this.mService.mInputMethodTarget || this.mService.mInputMethodTargetWaitingAnim != targetWaitingAnim || this.mInputMethodAnimLayerAdjustment != layerAdj) {
            if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mDisplayId) || this.mService.mInputMethodTarget == null || this.mService.mInputMethodTarget.getDisplayId() == this.mDisplayId) {
                this.mService.mInputMethodTarget = target;
                this.mService.mInputMethodTargetWaitingAnim = targetWaitingAnim;
                setInputMethodAnimLayerAdjustment(layerAdj);
                assignWindowLayers(false);
            }
        }
    }

    boolean getNeedsMenu(WindowState top, WindowState bottom) {
        boolean z = true;
        boolean z2 = false;
        if (top.mAttrs.needsMenuKey != 0) {
            if (top.mAttrs.needsMenuKey != 1) {
                z = false;
            }
            return z;
        }
        this.mTmpWindow = null;
        WindowState candidate = getWindow(new AnonymousClass26(this, top, bottom));
        if (candidate != null && candidate.mAttrs.needsMenuKey == 1) {
            z2 = true;
        }
        return z2;
    }

    /* synthetic */ boolean lambda$-com_android_server_wm_DisplayContent_126204(WindowState top, WindowState bottom, WindowState w) {
        if (w == top) {
            this.mTmpWindow = w;
        }
        if (this.mTmpWindow == null) {
            return false;
        }
        return w.mAttrs.needsMenuKey != 0 || w == bottom;
    }

    void setLayoutNeeded() {
        this.mLayoutNeeded = true;
    }

    private void clearLayoutNeeded() {
        this.mLayoutNeeded = false;
    }

    boolean isLayoutNeeded() {
        return this.mLayoutNeeded;
    }

    void dumpTokens(PrintWriter pw, boolean dumpAll) {
        if (!this.mTokenMap.isEmpty()) {
            pw.println("  Display #" + this.mDisplayId);
            for (WindowToken token : this.mTokenMap.values()) {
                pw.print("  ");
                pw.print(token);
                if (dumpAll) {
                    pw.println(':');
                    token.dump(pw, "    ");
                } else {
                    pw.println();
                }
            }
        }
    }

    void dumpWindowAnimators(PrintWriter pw, String subPrefix) {
        forAllWindows((Consumer) new AnonymousClass25(pw, subPrefix, new int[1]), false);
    }

    static /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_127971(PrintWriter pw, String subPrefix, int[] index, WindowState w) {
        if (!w.toString().contains("hwSingleMode_window")) {
            pw.println(subPrefix + "Window #" + index[0] + ": " + w.mWinAnimator);
            index[0] = index[0] + 1;
        }
    }

    void enableSurfaceTrace(FileDescriptor fd) {
        forAllWindows((Consumer) new AnonymousClass18(fd), true);
    }

    void disableSurfaceTrace() {
        forAllWindows((Consumer) new Consumer() {
            public final void accept(Object obj) {
                $m$0(obj);
            }
        }, true);
    }

    void startKeyguardExitOnNonAppWindows(boolean onWallpaper, boolean goingToShade) {
        forAllWindows((Consumer) new AnonymousClass33(onWallpaper, goingToShade, this.mService.mPolicy), true);
    }

    static /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_129043(WindowManagerPolicy policy, boolean onWallpaper, boolean goingToShade, WindowState w) {
        if (w.mAppToken == null && policy.canBeHiddenByKeyguardLw(w) && w.wouldBeVisibleIfPolicyIgnored() && (w.isVisible() ^ 1) != 0) {
            w.mWinAnimator.setAnimation(policy.createHiddenByKeyguardExit(onWallpaper, goingToShade));
        }
    }

    boolean checkWaitingForWindows() {
        this.mHaveBootMsg = false;
        this.mHaveApp = false;
        this.mHaveWallpaper = false;
        this.mHaveKeyguard = this.mService.mBootAnimationStopped;
        this.mQuickBoot = false;
        WindowState visibleWindow = getWindow(new AnonymousClass22(this));
        if (WindowManagerDebugConfig.DEBUG_BOOT) {
            Slog.i(TAG, "******** booted=" + this.mService.mSystemBooted + " msg=" + this.mService.mShowingBootMessages + " haveBoot=" + this.mHaveBootMsg + " haveApp=" + this.mHaveApp + " haveWall=" + this.mHaveWallpaper + " haveKeyguard=" + this.mHaveKeyguard + " mQuickBoot=" + this.mQuickBoot);
        }
        if (visibleWindow != null) {
            return true;
        }
        boolean wallpaperEnabled;
        if (this.mService.mContext.getResources().getBoolean(17956960)) {
            wallpaperEnabled = this.mService.mOnlyCore ^ 1;
        } else {
            wallpaperEnabled = false;
        }
        if (WindowManagerDebugConfig.DEBUG_BOOT) {
            Slog.i(TAG, " wallEnabled=" + wallpaperEnabled);
        }
        if (this.mService.mSystemBooted || (this.mHaveBootMsg ^ 1) == 0) {
            return this.mService.mSystemBooted && (!(this.mHaveApp || (this.mHaveKeyguard ^ 1) == 0) || (wallpaperEnabled && (this.mHaveWallpaper ^ 1) != 0));
        } else {
            return true;
        }
    }

    /* synthetic */ boolean lambda$-com_android_server_wm_DisplayContent_129718(WindowState w) {
        if (w.mAttrs.type != IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && w.mAttrs.type != 2013 && w.isVisibleLw() && (w.mObscured ^ 1) != 0 && (w.isDrawnLw() ^ 1) != 0) {
            return true;
        }
        if (w.isDrawnLw()) {
            if (w.mAttrs.type == 2021) {
                this.mHaveBootMsg = true;
            } else if (w.mAttrs.type == 2 || w.mAttrs.type == 4) {
                this.mHaveApp = true;
            } else if (w.mAttrs.type == 2013) {
                this.mHaveWallpaper = true;
            } else if (w.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
                this.mHaveKeyguard = this.mService.mPolicy.isKeyguardDrawnLw();
            }
        } else if (w.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
            this.mHaveKeyguard = this.mService.mPolicy.isKeyguardDrawnLw();
            this.mQuickBoot = true;
        } else if (w.mAttrs.type == 2013) {
            this.mHaveWallpaper = true;
            this.mQuickBoot = true;
        }
        return false;
    }

    void updateWindowsForAnimator(WindowAnimator animator) {
        this.mTmpWindowAnimator = animator;
        forAllWindows(this.mUpdateWindowsForAnimator, true);
    }

    void updateWallpaperForAnimator(WindowAnimator animator) {
        resetAnimationBackgroundAnimator();
        this.mTmpWindow = null;
        this.mTmpWindowAnimator = animator;
        forAllWindows(this.mUpdateWallpaperForAnimator, true);
        if (animator.mWindowDetachedWallpaper != this.mTmpWindow) {
            animator.mWindowDetachedWallpaper = this.mTmpWindow;
            animator.mBulkUpdateParams |= 2;
        }
    }

    void prepareWindowSurfaces() {
        forAllWindows(this.mPrepareWindowSurfaces, false);
    }

    boolean inputMethodClientHasFocus(IInputMethodClient client) {
        boolean z = false;
        WindowState imFocus = computeImeTarget(false);
        if (imFocus == null) {
            return false;
        }
        IInputMethodClient imeClient = imFocus.mSession.mClient;
        if (imeClient != null && imeClient.asBinder() == client.asBinder()) {
            z = true;
        }
        return z;
    }

    static /* synthetic */ boolean lambda$-com_android_server_wm_DisplayContent_134560(WindowState w) {
        return w.isOnScreen() && (w.mAttrs.flags & 8192) != 0;
    }

    boolean hasSecureWindowOnScreen() {
        return getWindow(new Predicate() {
            public final boolean test(Object obj) {
                return $m$0(obj);
            }
        }) != null;
    }

    void updateSystemUiVisibility(int visibility, int globalDiff) {
        forAllWindows((Consumer) new AnonymousClass30(visibility, globalDiff), true);
    }

    static /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_134746(int visibility, int globalDiff, WindowState w) {
        try {
            int curValue = w.mSystemUiVisibility;
            int diff = (curValue ^ visibility) & globalDiff;
            int newValue = ((~diff) & curValue) | (visibility & diff);
            if (newValue != curValue) {
                w.mSeq++;
                w.mSystemUiVisibility = newValue;
            }
            if (newValue != curValue || w.mAttrs.hasSystemUiListeners) {
                w.mClient.dispatchSystemUiVisibilityChanged(w.mSeq, visibility, newValue, diff);
            }
        } catch (RemoteException e) {
        }
    }

    void onWindowFreezeTimeout() {
        Slog.w(TAG, "Window freeze timeout expired.");
        this.mService.mWindowsFreezingScreen = 2;
        forAllWindows((Consumer) new AnonymousClass20(this), true);
        this.mService.mWindowPlacerLocked.performSurfacePlacement();
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_135678(WindowState w) {
        if (w.mOrientationChanging) {
            w.mOrientationChanging = false;
            w.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mService.mDisplayFreezeTime);
            Slog.w(TAG, "Force clearing orientation change: " + w);
        }
    }

    void waitForAllWindowsDrawn() {
        WindowManagerPolicy policy = this.mService.mPolicy;
        if (!this.isDefaultDisplay || (isCoverOpen() ^ 1) == 0) {
            forAllWindows((Consumer) new AnonymousClass24(this, policy), true);
            return;
        }
        Slog.w(TAG, "waitForAllWindowsDrawn cover is closed on default Display");
        forAllWindows((Consumer) new AnonymousClass23(this, policy), true);
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_136443(WindowManagerPolicy policy, WindowState w) {
        boolean keyguard = policy.isKeyguardHostWindow(w.mAttrs);
        if ((w.isVisibleLw() && (w.mAppToken != null || keyguard)) || w.mAttrs.type == 2100 || w.mAttrs.type == 2101) {
            w.mWinAnimator.mDrawState = 1;
            w.mLastContentInsets.set(-1, -1, -1, -1);
            this.mService.mWaitingForDrawn.add(w);
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_137112(WindowManagerPolicy policy, WindowState w) {
        boolean keyguard = policy.isKeyguardHostWindow(w.mAttrs);
        if (!w.isVisibleLw()) {
            return;
        }
        if (w.mAppToken != null || keyguard) {
            w.mWinAnimator.mDrawState = 1;
            w.mLastContentInsets.set(-1, -1, -1, -1);
            this.mService.mWaitingForDrawn.add(w);
        }
    }

    boolean applySurfaceChangesTransaction(boolean recoveringMemory) {
        int dw = this.mDisplayInfo.logicalWidth;
        int dh = this.mDisplayInfo.logicalHeight;
        WindowSurfacePlacer surfacePlacer = this.mService.mWindowPlacerLocked;
        this.mTmpUpdateAllDrawn.clear();
        int repeats = 0;
        while (true) {
            repeats++;
            if (repeats <= 6) {
                if (this.isDefaultDisplay && (this.pendingLayoutChanges & 4) != 0) {
                    this.mWallpaperController.adjustWallpaperWindows(this);
                }
                if (this.isDefaultDisplay && (this.pendingLayoutChanges & 2) != 0 && this.mService.updateOrientationFromAppTokensLocked(true, this.mDisplayId)) {
                    setLayoutNeeded();
                    this.mService.mH.obtainMessage(18, Integer.valueOf(this.mDisplayId)).sendToTarget();
                }
                if ((this.pendingLayoutChanges & 1) != 0) {
                    setLayoutNeeded();
                }
                if (repeats < 4) {
                    performLayout(repeats == 1, false);
                } else {
                    Slog.w(TAG, "Layout repeat skipped after too many iterations");
                }
                this.pendingLayoutChanges = 0;
                if (this.isDefaultDisplay) {
                    this.mService.mPolicy.beginPostLayoutPolicyLw(dw, dh);
                    forAllWindows(this.mApplyPostLayoutPolicy, true);
                    this.pendingLayoutChanges |= this.mService.mPolicy.finishPostLayoutPolicyLw();
                }
                if (this.pendingLayoutChanges == 0) {
                    break;
                }
            } else {
                Slog.w(TAG, "Animation repeat aborted after too many iterations");
                clearLayoutNeeded();
                break;
            }
        }
        this.mTmpApplySurfaceChangesTransactionState.reset();
        resetDimming();
        this.mTmpRecoveringMemory = recoveringMemory;
        forAllWindows(this.mApplySurfaceChangesTransaction, true);
        this.mService.mDisplayManagerInternal.setDisplayProperties(this.mDisplayId, this.mTmpApplySurfaceChangesTransactionState.displayHasContent, this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate, this.mTmpApplySurfaceChangesTransactionState.preferredModeId, true);
        stopDimmingIfNeeded();
        while (!this.mTmpUpdateAllDrawn.isEmpty()) {
            ((AppWindowToken) this.mTmpUpdateAllDrawn.removeLast()).updateAllDrawn();
        }
        return this.mTmpApplySurfaceChangesTransactionState.focusDisplayed;
    }

    void performLayout(boolean initial, boolean updateInputWindows) {
        if (isLayoutNeeded()) {
            clearLayoutNeeded();
            int dw = this.mDisplayInfo.logicalWidth;
            int dh = this.mDisplayInfo.logicalHeight;
            this.mService.setNaviBarFlag();
            this.mService.updateInputImmersiveMode();
            this.mService.mPolicy.beginLayoutLw(this.isDefaultDisplay, dw, dh, this.mRotation, getConfiguration().uiMode, this.mDisplayId);
            if (this.isDefaultDisplay) {
                this.mService.mSystemDecorLayer = this.mService.mPolicy.getSystemDecorLayerLw();
                this.mService.mScreenRect.set(0, 0, dw, dh);
            }
            this.mService.mPolicy.getContentRectLw(this.mContentRect);
            int seq = this.mService.mLayoutSeq + 1;
            if (seq < 0) {
                seq = 0;
            }
            this.mService.mLayoutSeq = seq;
            this.mTmpWindow = null;
            this.mTmpInitial = initial;
            forAllWindows(this.mPerformLayout, true);
            this.mTmpWindow2 = this.mTmpWindow;
            this.mTmpWindow = null;
            forAllWindows(this.mPerformLayoutAttached, true);
            this.mService.mInputMonitor.layoutInputConsumers(dw, dh);
            this.mService.mInputMonitor.setUpdateInputWindowsNeededLw();
            if (updateInputWindows) {
                this.mService.mInputMonitor.updateInputWindowsLw(false);
            }
            this.mService.mPolicy.finishLayoutLw();
            this.mService.mH.sendEmptyMessage(41);
        }
    }

    Bitmap screenshotApplications(IBinder appToken, int width, int height, boolean includeFullDisplay, float frameScale, Config config, boolean wallpaperOnly, boolean includeDecor) {
        Bitmap bitmap = (Bitmap) screenshotApplications(appToken, width, height, includeFullDisplay, frameScale, wallpaperOnly, includeDecor, new -$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI());
        if (bitmap == null) {
            return null;
        }
        Bitmap ret = bitmap.createAshmemBitmap(config);
        bitmap.recycle();
        return ret;
    }

    GraphicBuffer screenshotApplicationsToBuffer(IBinder appToken, int width, int height, boolean includeFullDisplay, float frameScale, boolean wallpaperOnly, boolean includeDecor) {
        return (GraphicBuffer) screenshotApplications(appToken, width, height, includeFullDisplay, frameScale, wallpaperOnly, includeDecor, new Screenshoter() {
            public final Object screenshot(Rect rect, int i, int i2, int i3, int i4, boolean z, int i5) {
                return $m$0(rect, i, i2, i3, i4, z, i5);
            }
        });
    }

    private <E> E screenshotApplications(IBinder appToken, int width, int height, boolean includeFullDisplay, float frameScale, boolean wallpaperOnly, boolean includeDecor, Screenshoter<E> screenshoter) {
        int dw = this.mDisplayInfo.logicalWidth;
        int dh = this.mDisplayInfo.logicalHeight;
        if (dw == 0 || dh == 0) {
            return null;
        }
        this.mScreenshotApplicationState.reset(appToken == null ? wallpaperOnly ^ 1 : false);
        Rect frame = new Rect();
        Rect stackBounds = new Rect();
        int aboveAppLayer = ((this.mService.mPolicy.getWindowLayerFromTypeLw(2) + 1) * 10000) + 1000;
        MutableBoolean mutableIncludeFullDisplay = new MutableBoolean(includeFullDisplay);
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mService.mPolicy.isScreenOn()) {
                    this.mScreenshotApplicationState.appWin = null;
                    forAllWindows(new AnonymousClass34(wallpaperOnly, includeDecor, aboveAppLayer, this, appToken, mutableIncludeFullDisplay, frame, stackBounds), true);
                    WindowState appWin = this.mScreenshotApplicationState.appWin;
                    boolean screenshotReady = this.mScreenshotApplicationState.screenshotReady;
                    int maxLayer = this.mScreenshotApplicationState.maxLayer;
                    int minLayer = this.mScreenshotApplicationState.minLayer;
                    if (appToken != null && appWin == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    } else if (!screenshotReady) {
                        Slog.i(TAG, "Failed to capture screenshot of " + appToken + " appWin=" + (appWin == null ? "null" : appWin + " drawState=" + appWin.mWinAnimator.mDrawState));
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    } else if (maxLayer == 0) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    } else {
                        int rot = this.mDisplay.getRotation();
                        if (rot == 1 || rot == 3) {
                            int tmp = width;
                            width = height;
                            height = tmp;
                        }
                        if (mutableIncludeFullDisplay.value) {
                            frame.set(0, 0, dw, dh);
                        } else if (!frame.intersect(0, 0, dw, dh)) {
                            frame.setEmpty();
                        }
                        if (frame.isEmpty()) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return null;
                        }
                        boolean inRotation;
                        if (width < 0) {
                            width = (int) (((float) frame.width()) * frameScale);
                        }
                        if (height < 0) {
                            height = (int) (((float) frame.height()) * frameScale);
                        }
                        Rect crop = new Rect(frame);
                        if (this.mService.mLazyModeOn != 0) {
                            this.mService.setCropOnSingleHandMode(this.mService.mLazyModeOn, false, dw, dh, crop);
                        } else {
                            if (((float) width) / ((float) frame.width()) < ((float) height) / ((float) frame.height())) {
                                crop.right = crop.left + ((int) ((((float) width) / ((float) height)) * ((float) frame.height())));
                                if (crop.right < frame.width()) {
                                    crop.right = frame.width();
                                }
                            } else {
                                crop.bottom = crop.top + ((int) ((((float) height) / ((float) width)) * ((float) frame.width())));
                            }
                        }
                        if (rot == 1 || rot == 3) {
                            rot = rot == 1 ? 3 : 1;
                        }
                        convertCropForSurfaceFlinger(crop, rot, dw, dh);
                        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(0);
                        if (screenRotationAnimation != null) {
                            inRotation = screenRotationAnimation.isAnimating();
                        } else {
                            inRotation = false;
                        }
                        Slog.i(TAG, "Taking screenshot from Surface with crop:[" + crop + "], width:[" + width + "], height:[" + height + "], minLayer:[" + minLayer + "], maxLayer:[" + maxLayer + "], inRotation:[" + inRotation + "], rot:[" + rot + "]");
                        SurfaceControl.openTransaction();
                        SurfaceControl.closeTransactionSync();
                        E bitmap = screenshoter.screenshot(crop, width, height, minLayer, maxLayer, inRotation, rot);
                        if (bitmap == null) {
                            Slog.w(TAG, "Screenshot failure taking screenshot for (" + dw + "x" + dh + ") to layer " + maxLayer);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return null;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return bitmap;
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return null;
    }

    /* synthetic */ boolean lambda$-com_android_server_wm_DisplayContent_148533(int aboveAppLayer, boolean wallpaperOnly, IBinder appToken, MutableBoolean mutableIncludeFullDisplay, boolean includeDecor, Rect frame, Rect stackBounds, WindowState w) {
        if (!w.mHasSurface) {
            return false;
        }
        if (w.mLayer >= aboveAppLayer) {
            return false;
        }
        if (wallpaperOnly && (w.mIsWallpaper ^ 1) != 0) {
            return false;
        }
        if (w.mIsImWindow) {
            return false;
        }
        if (w.mIsWallpaper) {
            if (wallpaperOnly) {
                this.mScreenshotApplicationState.appWin = w;
            }
            if (this.mScreenshotApplicationState.appWin == null) {
                return false;
            }
        } else if (appToken != null) {
            if (w.mAppToken == null || w.mAppToken.token != appToken) {
                return false;
            }
            this.mScreenshotApplicationState.appWin = w;
        }
        WindowStateAnimator winAnim = w.mWinAnimator;
        int layer = winAnim.mSurfaceController.getLayer();
        if (this.mScreenshotApplicationState.maxLayer < layer) {
            this.mScreenshotApplicationState.maxLayer = layer;
        }
        if (this.mScreenshotApplicationState.minLayer > layer) {
            this.mScreenshotApplicationState.minLayer = layer;
        }
        if (!(w.mIsWallpaper || (mutableIncludeFullDisplay.value ^ 1) == 0)) {
            if (includeDecor) {
                TaskStack stack = w.getStack();
                if (stack != null) {
                    stack.getBounds(frame);
                }
                frame.intersect(w.mFrame);
            } else {
                Rect wf = w.mFrame;
                Rect cr = w.mContentInsets;
                frame.union(wf.left + cr.left, wf.top + cr.top, wf.right - cr.right, wf.bottom - cr.bottom);
                w.getVisibleBounds(stackBounds);
                if (!Rect.intersects(frame, stackBounds)) {
                    frame.setEmpty();
                }
            }
        }
        boolean foundTargetWs = (w.mAppToken == null || w.mAppToken.token != appToken) ? this.mScreenshotApplicationState.appWin != null ? wallpaperOnly : false : true;
        if (foundTargetWs && winAnim.getShown()) {
            this.mScreenshotApplicationState.screenshotReady = true;
        }
        if (w.isObscuringDisplay()) {
            return true;
        }
        return false;
    }

    protected static void convertCropForSurfaceFlinger(Rect crop, int rot, int dw, int dh) {
        int tmp;
        if (rot == 1) {
            tmp = crop.top;
            crop.top = dw - crop.right;
            crop.right = crop.bottom;
            crop.bottom = dw - crop.left;
            crop.left = tmp;
        } else if (rot == 2) {
            tmp = crop.top;
            crop.top = dh - crop.bottom;
            crop.bottom = dh - tmp;
            tmp = crop.right;
            crop.right = dw - crop.left;
            crop.left = dw - tmp;
        } else if (rot == 3) {
            tmp = crop.top;
            crop.top = crop.left;
            crop.left = dh - crop.bottom;
            crop.bottom = crop.right;
            crop.right = dh - tmp;
        }
    }

    void onSeamlessRotationTimeout() {
        this.mTmpWindow = null;
        forAllWindows((Consumer) new AnonymousClass19(this), true);
        if (this.mTmpWindow != null) {
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_DisplayContent_161672(WindowState w) {
        if (w.mSeamlesslyRotated) {
            this.mTmpWindow = w;
            w.setDisplayLayoutNeeded();
            this.mService.markForSeamlessRotation(w, false);
        }
    }

    void setExitingTokensHasVisible(boolean hasVisible) {
        for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
            ((WindowToken) this.mExitingTokens.get(i)).hasVisible = hasVisible;
        }
        this.mTaskStackContainers.setExitingTokensHasVisible(hasVisible);
    }

    void removeExistingTokensIfPossible() {
        for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
            if (!((WindowToken) this.mExitingTokens.get(i)).hasVisible) {
                this.mExitingTokens.remove(i);
            }
        }
        this.mTaskStackContainers.removeExistingAppTokensIfPossible();
    }

    void onDescendantOverrideConfigurationChanged() {
        setLayoutNeeded();
        this.mService.requestTraversal();
    }
}
