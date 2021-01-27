package com.android.server.wm;

import android.animation.AnimationHandler;
import android.app.WindowConfiguration;
import android.common.HwFrameworkFactory;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.HwFoldScreenState;
import android.metrics.LogMaker;
import android.os.Binder;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.ISystemGestureExclusionListener;
import android.view.IWindow;
import android.view.InputApplicationHandle;
import android.view.InputDevice;
import android.view.InputWindowHandle;
import android.view.MagnificationSpec;
import android.view.RemoteAnimationDefinition;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowManagerPolicyConstants;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.ToBooleanFunction;
import com.android.internal.util.function.TriConsumer;
import com.android.internal.util.function.pooled.PooledConsumer;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.AnimationThread;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.input.InputManagerService;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.DisplayContent;
import com.android.server.wm.WindowContainer;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.utils.DisplayRotationUtil;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.android.server.wm.utils.RegionUtils;
import com.android.server.wm.utils.RotationCache;
import com.android.server.wm.utils.WmDisplayCutout;
import com.google.android.collect.Sets;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.server.wm.IHwDisplayRotationEx;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DisplayContent extends AbsDisplayContent implements WindowManagerPolicy.DisplayContentInfo {
    private static final float FLOAT_COMPARE_VALUE = 0.001f;
    static final int FORCE_SCALING_MODE_AUTO = 0;
    static final int FORCE_SCALING_MODE_DISABLED = 1;
    static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final int PAD_DISPLAY_ID = 100000;
    private static final String TAG = "WindowManager";
    private static final int TOP_ACTIVITY_NOTCH_STATE_UNKNOWN = 0;
    private static final int TOP_ACTIVITY_NOTCH_STATE_UNUSED = 1;
    private static final int TOP_ACTIVITY_NOTCH_STATE_USED = 2;
    private static final String UNIQUE_ID_PREFIX_LOCAL = "local:";
    private final int SPLIT_RESIZING_TIMEOUT_TIME = 600;
    @VisibleForTesting
    boolean isDefaultDisplay;
    protected final AboveAppWindowContainers mAboveAppWindowsContainers = new AboveAppWindowContainers("mAboveAppWindowsContainers", this.mWmService);
    ActivityDisplay mAcitvityDisplay;
    final AppTransition mAppTransition;
    final AppTransitionController mAppTransitionController;
    private final Consumer<WindowState> mApplyPostLayoutPolicy;
    private final Consumer<WindowState> mApplySurfaceChangesTransaction;
    int mBaseDisplayDensity = 0;
    int mBaseDisplayHeight = 0;
    private Rect mBaseDisplayRect = new Rect();
    int mBaseDisplayWidth = 0;
    private final NonAppWindowContainers mBelowAppWindowsContainers = new NonAppWindowContainers("mBelowAppWindowsContainers", this.mWmService);
    BoundsAnimationController mBoundsAnimationController;
    final ArraySet<AppWindowToken> mChangingApps = new ArraySet<>();
    @VisibleForTesting
    final float mCloseToSquareMaxAspectRatio;
    final ArraySet<AppWindowToken> mClosingApps = new ArraySet<>();
    private final DisplayMetrics mCompatDisplayMetrics = new DisplayMetrics();
    float mCompatibleScreenScale;
    private final Predicate<WindowState> mComputeImeTargetPredicate;
    WindowState mCurrentFocus = null;
    WindowState mCurrentFocusInHwPc = null;
    private int mDeferUpdateImeTargetCount;
    private boolean mDeferredRemoval;
    int mDeferredRotationPauseCount;
    protected final Display mDisplay;
    private final RotationCache<DisplayCutout, WmDisplayCutout> mDisplayCutoutCache = new RotationCache<>(new RotationCache.RotationDependentComputation() {
        /* class com.android.server.wm.$$Lambda$DisplayContent$fiC19lMyd_rvza7hhOSw6bOM8 */

        @Override // com.android.server.wm.utils.RotationCache.RotationDependentComputation
        public final Object compute(Object obj, int i) {
            return DisplayContent.this.calculateDisplayCutoutForRotationUncached((DisplayCutout) obj, i);
        }
    });
    DisplayFrames mDisplayFrames;
    protected int mDisplayId = -1;
    protected final DisplayInfo mDisplayInfo = new DisplayInfo();
    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    private final DisplayPolicy mDisplayPolicy;
    private boolean mDisplayReady = false;
    private DisplayRotation mDisplayRotation;
    boolean mDisplayScalingDisabled;
    final DockedStackDividerController mDividerControllerLocked;
    final ArrayList<WindowToken> mExitingTokens = new ArrayList<>();
    private final ToBooleanFunction<WindowState> mFindFocusedWindow;
    AppWindowToken mFocusedApp = null;
    private HwFoldScreenManagerInternal mFsmInternal;
    private boolean mHaveApp = false;
    private boolean mHaveBootMsg = false;
    private boolean mHaveKeyguard = true;
    private boolean mHaveWallpaper = false;
    public IHwDisplayContentEx mHwDisplayCotentEx = HwServiceExFactory.getHwDisplayContentEx();
    private SurfaceControl mHwSingleHandOverlayLayer;
    private boolean mIgnoreRotationForApps;
    private final NonAppWindowContainers mImeWindowsContainers = new NonAppWindowContainers("mImeWindowsContainers", this.mWmService);
    DisplayCutout mInitialDisplayCutout;
    int mInitialDisplayDensity = 0;
    int mInitialDisplayHeight = 0;
    int mInitialDisplayWidth = 0;
    WindowState mInputMethodTarget;
    boolean mInputMethodTargetWaitingAnim;
    WindowState mInputMethodWindow;
    private InputMonitor mInputMonitor;
    private final InsetsStateController mInsetsStateController;
    private boolean mIsFisrtLazyMode;
    private final boolean mIsLandScapeDefault;
    private boolean mIsRequestFullScreen;
    private int mLastDispatchedSystemUiVisibility = 0;
    WindowState mLastFocus = null;
    private boolean mLastHasContent;
    private int mLastKeyguardForcedOrientation = -1;
    private int mLastOrientation = -1;
    private int mLastStatusBarVisibility = 0;
    private boolean mLastWallpaperVisible = false;
    private int mLastWindowForcedOrientation = -1;
    private boolean mLayoutNeeded;
    int mLayoutSeq = 0;
    private Point mLocationInParentWindow = new Point();
    ArrayList<WindowState> mLosingFocus = new ArrayList<>();
    private MagnificationSpec mMagnificationSpec;
    private int mMaxUiWidth;
    private MetricsLogger mMetricsLogger;
    final List<IBinder> mNoAnimationNotifyOnTransitionFinished = new ArrayList();
    WindowToken mObserveToken;
    WindowState mObserveWin;
    String mObserveWinTitle;
    final ArraySet<AppWindowToken> mOpeningApps = new ArraySet<>();
    private SurfaceControl mOverlayLayer;
    private SurfaceControl mParentSurfaceControl;
    private WindowState mParentWindow;
    private final Region mPcTouchExcludeRegion = new Region();
    private final Consumer<WindowState> mPerformLayout;
    private final Consumer<WindowState> mPerformLayoutAttached;
    final PinnedStackController mPinnedStackControllerLocked;
    private final PointerEventDispatcher mPointerEventDispatcher;
    private InputWindowHandle mPortalWindowHandle;
    final DisplayMetrics mRealDisplayMetrics = new DisplayMetrics();
    private boolean mRemovingDisplay = false;
    private int mRotation = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    private DisplayRotationUtil mRotationUtil = new DisplayRotationUtil();
    private final Consumer<WindowState> mScheduleToastTimeout;
    private final SurfaceSession mSession = new SurfaceSession();
    boolean mShouldOverrideDisplayConfiguration = true;
    ScreenSideSurfaceBox mSideSurfaceBox;
    public IHwSingleHandContentEx mSingleHandContentEx;
    boolean mSkipAppTransitionAnimation = false;
    private final Region mSystemGestureExclusion = new Region();
    private int mSystemGestureExclusionLimit;
    private final RemoteCallbackList<ISystemGestureExclusionListener> mSystemGestureExclusionListeners = new RemoteCallbackList<>();
    @VisibleForTesting
    final TaskTapPointerEventListener mTapDetector;
    final ArraySet<WindowState> mTapExcludeProvidingWindows = new ArraySet<>();
    final ArrayList<WindowState> mTapExcludedWindows = new ArrayList<>();
    protected final TaskStackContainers mTaskStackContainers = new TaskStackContainers(this.mWmService);
    private final ApplySurfaceChangesTransactionState mTmpApplySurfaceChangesTransactionState = new ApplySurfaceChangesTransactionState();
    private final Rect mTmpBounds = new Rect();
    private final Configuration mTmpConfiguration = new Configuration();
    private final DisplayMetrics mTmpDisplayMetrics = new DisplayMetrics();
    private final float[] mTmpFloats = new float[9];
    private boolean mTmpInitial;
    private final Matrix mTmpMatrix = new Matrix();
    private boolean mTmpRecoveringMemory;
    private final Rect mTmpRect = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private final RectF mTmpRectF = new RectF();
    private final Region mTmpRegion = new Region();
    private final TaskForResizePointSearchResult mTmpTaskForResizePointSearchResult = new TaskForResizePointSearchResult();
    private final LinkedList<AppWindowToken> mTmpUpdateAllDrawn = new LinkedList<>();
    private WindowState mTmpWindow;
    private WindowState mTmpWindow2;
    private final HashMap<IBinder, WindowToken> mTokenMap = new HashMap<>();
    WindowToken mTopAboveAppToken;
    private Region mTouchExcludeRegion = new Region();
    final UnknownAppVisibilityController mUnknownAppVisibilityController;
    private boolean mUpdateImeTarget;
    private final Consumer<WindowState> mUpdateWallpaperForAnimator;
    private final Consumer<WindowState> mUpdateWindowsForAnimator;
    boolean mWaitingForConfig;
    WallpaperController mWallpaperController;
    boolean mWallpaperMayChange = false;
    final ArrayList<WindowState> mWinAddedSinceNullFocus = new ArrayList<>();
    boolean mWinEverCovered;
    final ArrayList<WindowState> mWinRemovedSinceNullFocus = new ArrayList<>();
    private final float mWindowCornerRadius;
    private SurfaceControl mWindowingLayer;
    int pendingLayoutChanges;

    @Retention(RetentionPolicy.SOURCE)
    @interface ForceScalingMode {
    }

    public /* synthetic */ void lambda$new$0$DisplayContent(WindowState w) {
        WindowStateAnimator winAnimator = w.mWinAnimator;
        AppWindowToken atoken = w.mAppToken;
        if (winAnimator.mDrawState != 3) {
            return;
        }
        if ((atoken == null || atoken.canShowWindows()) && w.performShowLocked()) {
            this.pendingLayoutChanges |= 8;
        }
    }

    static /* synthetic */ void lambda$new$1(WindowState w) {
        AnimationAdapter anim;
        int color;
        TaskStack stack;
        WindowStateAnimator winAnimator = w.mWinAnimator;
        if (winAnimator.mSurfaceController != null && winAnimator.hasSurface()) {
            if (w.mAppToken != null) {
                anim = w.mAppToken.getAnimation();
            } else {
                anim = w.getAnimation();
            }
            if (anim != null && (color = anim.getBackgroundColor()) != 0 && (stack = w.getStack()) != null) {
                stack.setAnimationBackground(winAnimator, color);
            }
        }
    }

    public /* synthetic */ void lambda$new$2$DisplayContent(WindowState w) {
        WindowState windowState = this.mTmpWindow;
        if (windowState != null) {
            int lostFocusUid = windowState.mOwnerUid;
            Handler handler = this.mWmService.mH;
            if (w.mAttrs.type == 2005 && w.mOwnerUid == lostFocusUid && !handler.hasMessages(52, w)) {
                handler.sendMessageDelayed(handler.obtainMessage(52, w), w.mAttrs.hideTimeoutMilliseconds);
            }
        }
    }

    public /* synthetic */ boolean lambda$new$3$DisplayContent(WindowState w) {
        AppWindowToken focusedApp = this.mFocusedApp;
        if (WindowManagerDebugConfig.DEBUG_FOCUS) {
            Slog.v(TAG, "Looking for focus: " + w + ", flags=" + w.mAttrs.flags + ", canReceive=" + w.canReceiveKeys());
        }
        if (!w.canReceiveKeys()) {
            return false;
        }
        if (HwPCUtils.isPcCastModeInServer() && !this.isDefaultDisplay) {
            if (!(!HwPCUtils.isHiCarCastMode() || w.mAttrs == null || w.mAttrs.getTitle() == null)) {
                String windowName = (String) w.mAttrs.getTitle();
                if (this.mWmService.mHwWMSEx.getCarFocusList().contains(windowName)) {
                    HwPCUtils.log(TAG, "FindFocusedWindow skip win:" + windowName);
                    return false;
                }
            }
            if (this.mWmService.getPCLauncherFocused() && w.mAttrs != null && w.mAttrs.type != 2103 && w.mAppToken != null) {
                return false;
            }
            if (HwPCUtils.enabledInPad() && !this.mWmService.mPolicy.isKeyguardLocked() && w.mAttrs != null && w.mAttrs.type == 2000) {
                HwPCUtils.log(TAG, "Skipping " + w + ", flags=" + w.mAttrs.flags + ", canReceive=" + w.canReceiveKeys());
                return false;
            }
        }
        AppWindowToken wtoken = w.mAppToken;
        if (w.inHwFreeFormWindowingMode() && focusedApp != 0 && focusedApp != wtoken) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                Slog.v(TAG, "skip unfocused huawei freefrom window.");
            }
            return false;
        } else if (w.getWindowingMode() == 5 && focusedApp != 0 && focusedApp != wtoken) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                Slog.v(TAG, "skip unfocused freefrom window.");
            }
            return false;
        } else if (wtoken != null && (wtoken.removed || wtoken.sendingToBottom)) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                StringBuilder sb = new StringBuilder();
                sb.append("Skipping ");
                sb.append(wtoken);
                sb.append(" because ");
                sb.append(wtoken.removed ? "removed" : "sendingToBottom");
                Slog.v(TAG, sb.toString());
            }
            return false;
        } else if (focusedApp == null) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v(TAG, "findFocusedWindow: focusedApp=null using new focus @ " + w);
            }
            this.mTmpWindow = w;
            return true;
        } else if (!focusedApp.windowsAreFocusable()) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v(TAG, "findFocusedWindow: focusedApp windows not focusable using new focus @ " + w);
            }
            this.mTmpWindow = w;
            return true;
        } else if (wtoken == null || w.mAttrs.type == 3 || focusedApp.compareTo((WindowContainer) wtoken) <= 0) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v(TAG, "findFocusedWindow: Found new focus @ " + w);
            }
            this.mTmpWindow = w;
            return true;
        } else {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v(TAG, "findFocusedWindow: " + wtoken + " below Reached focused app=" + focusedApp);
            }
            this.mTmpWindow = null;
            return true;
        }
    }

    public /* synthetic */ void lambda$new$4$DisplayContent(WindowState w) {
        boolean gone = (this.mTmpWindow != null && this.mWmService.mPolicy.canBeHiddenByKeyguardLw(w)) || w.isGoneForLayoutLw();
        if (WindowManagerDebugConfig.DEBUG_LAYOUT && !w.mLayoutAttached) {
            Slog.v(TAG, "1ST PASS " + w + ": gone=" + gone + " mHaveFrame=" + w.mHaveFrame + " mLayoutAttached=" + w.mLayoutAttached + " config reported=" + w.isLastConfigReportedToClient());
            AppWindowToken atoken = w.mAppToken;
            if (gone) {
                StringBuilder sb = new StringBuilder();
                sb.append("  GONE: mViewVisibility=");
                sb.append(w.mViewVisibility);
                sb.append(" mRelayoutCalled=");
                sb.append(w.mRelayoutCalled);
                sb.append(" hidden=");
                sb.append(w.mToken.isHidden());
                sb.append(" hiddenRequested=");
                sb.append(atoken != null && atoken.hiddenRequested);
                sb.append(" parentHidden=");
                sb.append(w.isParentWindowHidden());
                Slog.v(TAG, sb.toString());
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("  VIS: mViewVisibility=");
                sb2.append(w.mViewVisibility);
                sb2.append(" mRelayoutCalled=");
                sb2.append(w.mRelayoutCalled);
                sb2.append(" hidden=");
                sb2.append(w.mToken.isHidden());
                sb2.append(" hiddenRequested=");
                sb2.append(atoken != null && atoken.hiddenRequested);
                sb2.append(" parentHidden=");
                sb2.append(w.isParentWindowHidden());
                Slog.v(TAG, sb2.toString());
            }
        }
        if ((!gone || !w.mHaveFrame || w.mLayoutNeeded) && !w.mLayoutAttached) {
            if (this.mTmpInitial) {
                w.resetContentChanged();
            }
            if (w.mAttrs.type == 2023) {
                this.mTmpWindow = w;
            }
            w.mLayoutNeeded = false;
            w.prelayout();
            boolean firstLayout = !w.isLaidOut();
            getDisplayPolicy().layoutWindowLw(w, null, this.mDisplayFrames);
            w.mLayoutSeq = this.mLayoutSeq;
            if (firstLayout) {
                w.updateLastInsetValues();
            }
            if (w.mAppToken != null) {
                w.mAppToken.layoutLetterbox(w);
            }
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v(TAG, "  LAYOUT: mFrame=" + w.getFrameLw() + " mContainingFrame=" + w.getContainingFrame() + " mDisplayFrame=" + w.getDisplayFrameLw());
            }
        }
    }

    public /* synthetic */ void lambda$new$5$DisplayContent(WindowState w) {
        if (w.mLayoutAttached) {
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v(TAG, "2ND PASS " + w + " mHaveFrame=" + w.mHaveFrame + " mViewVisibility=" + w.mViewVisibility + " mRelayoutCalled=" + w.mRelayoutCalled);
            }
            if (this.mTmpWindow != null && this.mWmService.mPolicy.canBeHiddenByKeyguardLw(w)) {
                return;
            }
            if ((w.mViewVisibility != 8 && w.mRelayoutCalled) || !w.mHaveFrame || w.mLayoutNeeded) {
                if (this.mTmpInitial) {
                    w.resetContentChanged();
                }
                w.mLayoutNeeded = false;
                w.prelayout();
                getDisplayPolicy().layoutWindowLw(w, w.getParentWindow(), this.mDisplayFrames);
                w.mLayoutSeq = this.mLayoutSeq;
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v(TAG, " LAYOUT: mFrame=" + w.getFrameLw() + " mContainingFrame=" + w.getContainingFrame() + " mDisplayFrame=" + w.getDisplayFrameLw());
                }
            }
        } else if (w.mAttrs.type == 2023) {
            this.mTmpWindow = this.mTmpWindow2;
        }
    }

    public /* synthetic */ boolean lambda$new$6$DisplayContent(WindowState w) {
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD && this.mUpdateImeTarget) {
            Slog.i(TAG, "Checking window @" + w + " fl=0x" + Integer.toHexString(w.mAttrs.flags));
        }
        return w.canBeImeTarget();
    }

    public /* synthetic */ void lambda$new$7$DisplayContent(WindowState w) {
        getDisplayPolicy().applyPostLayoutPolicyLw(w, w.mAttrs, w.getParentWindow(), this.mInputMethodTarget);
    }

    /* JADX INFO: Multiple debug info for r5v1 com.android.server.wm.AppWindowToken: [D('atoken' com.android.server.wm.AppWindowToken), D('committed' boolean)] */
    public /* synthetic */ void lambda$new$8$DisplayContent(WindowState w) {
        WindowSurfacePlacer windowSurfacePlacer = this.mWmService.mWindowPlacerLocked;
        boolean obscuredChanged = w.mObscured != this.mTmpApplySurfaceChangesTransactionState.obscured;
        RootWindowContainer root = this.mWmService.mRoot;
        w.mObscured = this.mTmpApplySurfaceChangesTransactionState.obscured;
        if (!this.mTmpApplySurfaceChangesTransactionState.obscured) {
            boolean isDisplayed = w.isDisplayedLw();
            if (isDisplayed && w.isObscuringDisplay()) {
                root.mObscuringWindow = w;
                this.mTmpApplySurfaceChangesTransactionState.obscured = true;
            }
            this.mTmpApplySurfaceChangesTransactionState.displayHasContent |= root.handleNotObscuredLocked(w, this.mTmpApplySurfaceChangesTransactionState.obscured, this.mTmpApplySurfaceChangesTransactionState.syswin);
            if (w.mHasSurface && isDisplayed) {
                int type = w.mAttrs.type;
                if (type == 2008 || type == 2010 || (w.mAttrs.privateFlags & 1024) != 0) {
                    this.mTmpApplySurfaceChangesTransactionState.syswin = true;
                }
                if (this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate == 0.0f && w.mAttrs.preferredRefreshRate != 0.0f) {
                    this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate = w.mAttrs.preferredRefreshRate;
                }
                int preferredModeId = getDisplayPolicy().getRefreshRatePolicy().getPreferredModeId(w);
                if (this.mTmpApplySurfaceChangesTransactionState.preferredModeId == 0 && preferredModeId != 0) {
                    this.mTmpApplySurfaceChangesTransactionState.preferredModeId = preferredModeId;
                }
            }
        }
        if (obscuredChanged && w.isVisibleLw() && this.mWallpaperController.isWallpaperTarget(w)) {
            this.mWallpaperController.updateWallpaperVisibility();
        }
        w.handleWindowMovedIfNeeded();
        WindowStateAnimator winAnimator = w.mWinAnimator;
        w.resetContentChanged();
        if (w.mHasSurface) {
            boolean committed = winAnimator.commitFinishDrawingLocked();
            if (this.isDefaultDisplay && committed) {
                if (w.mAttrs.type == 2023) {
                    this.pendingLayoutChanges |= 1;
                }
                if ((w.mAttrs.flags & 1048576) != 0) {
                    if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                        Slog.v(TAG, "First draw done in potential wallpaper target " + w);
                    }
                    this.mWallpaperMayChange = true;
                    this.pendingLayoutChanges |= 4;
                }
            }
        }
        AppWindowToken atoken = w.mAppToken;
        if (atoken != null) {
            atoken.updateLetterboxSurface(w);
            if (atoken.updateDrawnWindowStates(w) && !this.mTmpUpdateAllDrawn.contains(atoken)) {
                this.mTmpUpdateAllDrawn.add(atoken);
            }
        }
        if (!this.mLosingFocus.isEmpty() && w.isFocused() && w.isDisplayedLw()) {
            this.mWmService.mH.obtainMessage(3, this).sendToTarget();
        }
        w.updateResizingWindowIfNeeded();
        if (!this.mIsRequestFullScreen && w.mAttrs.type != 3 && w.mAppToken != null && w.isVisible() && w.isRequestFullscreen()) {
            this.mIsRequestFullScreen = true;
        }
        this.mWmService.mHwWMSEx.travelAllWindow(w);
    }

    public DisplayContent(Display display, WindowManagerService service, ActivityDisplay activityDisplay) {
        super(service);
        this.mIsLandScapeDefault = SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90 == 1;
        this.mIsFisrtLazyMode = true;
        this.mUpdateWindowsForAnimator = new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$0yxrqH9eGY2qTjH1u_BvaVrXCSA */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$new$0$DisplayContent((WindowState) obj);
            }
        };
        this.mUpdateWallpaperForAnimator = $$Lambda$DisplayContent$GuCKVzKP141d6J0gfRAjKtuBJUU.INSTANCE;
        this.mScheduleToastTimeout = new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$hRKjZwmneu0T85LNNY6_Zcs4gKM */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$new$2$DisplayContent((WindowState) obj);
            }
        };
        this.mFindFocusedWindow = new ToBooleanFunction() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$7uZtakUXzuXqF_Qht5Uq7LUvubI */

            public final boolean apply(Object obj) {
                return DisplayContent.this.lambda$new$3$DisplayContent((WindowState) obj);
            }
        };
        this.mPerformLayout = new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$qT01Aq6xt_ZOs86A1yDQeqmPFQ */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$new$4$DisplayContent((WindowState) obj);
            }
        };
        this.mPerformLayoutAttached = new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$7voe_dEKk2BYMriCvPuvaznb9WQ */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$new$5$DisplayContent((WindowState) obj);
            }
        };
        this.mComputeImeTargetPredicate = new Predicate() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$TPj3OjTsuIg5GTLb5nMmFqIghA4 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.this.lambda$new$6$DisplayContent((WindowState) obj);
            }
        };
        this.mApplyPostLayoutPolicy = new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$JibsaX4YnJd0ta_wiDDdSpPjQk */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$new$7$DisplayContent((WindowState) obj);
            }
        };
        this.mApplySurfaceChangesTransaction = new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$qxt4izS31fb0LF2uo_OF9DMa7gc */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$new$8$DisplayContent((WindowState) obj);
            }
        };
        this.mObserveWin = null;
        this.mWinEverCovered = false;
        this.mObserveToken = null;
        this.mTopAboveAppToken = null;
        this.mObserveWinTitle = "FingerprintDialogView";
        this.mAcitvityDisplay = activityDisplay;
        if (service.mRoot.getDisplayContent(display.getDisplayId()) == null) {
            this.mDisplay = display;
            this.mDisplayId = display.getDisplayId();
            if (this.mDisplayId == 0 && HwDisplaySizeUtil.hasSideInScreen()) {
                this.mSideSurfaceBox = new ScreenSideSurfaceBox(this);
            }
            this.mWallpaperController = new WallpaperController(this.mWmService, this);
            display.getDisplayInfo(this.mDisplayInfo);
            display.getMetrics(this.mDisplayMetrics);
            this.mSystemGestureExclusionLimit = (this.mWmService.mSystemGestureExclusionLimitDp * this.mDisplayMetrics.densityDpi) / 160;
            this.isDefaultDisplay = this.mDisplayId == 0;
            int i = this.mDisplayId;
            DisplayInfo displayInfo = this.mDisplayInfo;
            this.mDisplayFrames = new DisplayFrames(i, displayInfo, calculateDisplayCutoutForRotation(displayInfo.rotation));
            if (!this.isDefaultDisplay && !"local:100000".equals(this.mDisplayInfo.uniqueId) && this.mIsLandScapeDefault) {
                this.mRotation = 0;
            }
            initializeDisplayBaseInfo();
            this.mAppTransition = new AppTransition(service.mContext, service, this);
            this.mAppTransition.registerListenerLocked(service.mActivityManagerAppTransitionNotifier);
            this.mAppTransitionController = new AppTransitionController(service, this);
            this.mUnknownAppVisibilityController = new UnknownAppVisibilityController(service, this);
            this.mBoundsAnimationController = new BoundsAnimationController(service.mContext, this.mAppTransition, AnimationThread.getHandler(), new AnimationHandler());
            InputManagerService inputManagerService = this.mWmService.mInputManager;
            this.mPointerEventDispatcher = new PointerEventDispatcher(inputManagerService.monitorInput("PointerEventDispatcher" + this.mDisplayId, this.mDisplayId));
            this.mTapDetector = new TaskTapPointerEventListener(this.mWmService, this);
            registerPointerEventListener(this.mTapDetector);
            registerPointerEventListener(this.mWmService.mMousePositionTracker);
            if (this.mWmService.mAtmService.getRecentTasks() != null) {
                registerPointerEventListener(this.mWmService.mAtmService.getRecentTasks().getInputListener());
            }
            this.mDisplayPolicy = new DisplayPolicy(service, this);
            this.mDisplayRotation = new DisplayRotation(service, this);
            this.mCloseToSquareMaxAspectRatio = service.mContext.getResources().getFloat(17105054);
            if (this.isDefaultDisplay) {
                this.mWmService.mPolicy.setDefaultDisplay(this);
            }
            if (this.mWmService.mDisplayReady) {
                this.mDisplayPolicy.onConfigurationChanged();
            }
            if (this.mWmService.mSystemReady) {
                this.mDisplayPolicy.systemReady();
            }
            this.mWindowCornerRadius = this.mDisplayPolicy.getWindowCornerRadius();
            this.mDividerControllerLocked = new DockedStackDividerController(service, this);
            this.mPinnedStackControllerLocked = new PinnedStackController(service, this);
            SurfaceControl.Builder b = this.mWmService.makeSurfaceBuilder(this.mSession).setOpaque(true).setContainerLayer();
            this.mWindowingLayer = b.setName("Display Root").build();
            this.mOverlayLayer = b.setName("Display Overlays").build();
            this.mHwSingleHandOverlayLayer = b.setName("Hw Display Overlays").build();
            getPendingTransaction().setLayer(this.mWindowingLayer, 0).setLayerStack(this.mWindowingLayer, this.mDisplayId).show(this.mWindowingLayer).setLayer(this.mOverlayLayer, 1).setLayerStack(this.mOverlayLayer, this.mDisplayId).show(this.mOverlayLayer).setLayer(this.mHwSingleHandOverlayLayer, 2).setLayerStack(this.mHwSingleHandOverlayLayer, this.mDisplayId).show(this.mHwSingleHandOverlayLayer);
            getPendingTransaction().apply();
            super.addChild((DisplayContent) this.mBelowAppWindowsContainers, (Comparator<DisplayContent>) null);
            super.addChild((DisplayContent) this.mTaskStackContainers, (Comparator<DisplayContent>) null);
            super.addChild((DisplayContent) this.mAboveAppWindowsContainers, (Comparator<DisplayContent>) null);
            super.addChild((DisplayContent) this.mImeWindowsContainers, (Comparator<DisplayContent>) null);
            this.mWmService.mRoot.addChild((RootWindowContainer) this, (Comparator<RootWindowContainer>) null);
            this.mDisplayReady = true;
            this.mWmService.mAnimator.addDisplayLocked(this.mDisplayId);
            this.mInputMonitor = new InputMonitor(service, this.mDisplayId);
            this.mInsetsStateController = new InsetsStateController(this);
            return;
        }
        throw new IllegalArgumentException("Display with ID=" + display.getDisplayId() + " already exists=" + service.mRoot.getDisplayContent(display.getDisplayId()) + " new=" + display);
    }

    /* access modifiers changed from: package-private */
    public boolean isReady() {
        return this.mWmService.mDisplayReady && this.mDisplayReady;
    }

    /* access modifiers changed from: package-private */
    public int getDisplayId() {
        return this.mDisplayId;
    }

    /* access modifiers changed from: package-private */
    public float getWindowCornerRadius() {
        return this.mWindowCornerRadius;
    }

    /* access modifiers changed from: package-private */
    public WindowToken getWindowToken(IBinder binder) {
        return this.mTokenMap.get(binder);
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken getAppWindowToken(IBinder binder) {
        WindowToken token = getWindowToken(binder);
        if (token == null) {
            return null;
        }
        return token.asAppWindowToken();
    }

    private void addWindowToken(IBinder binder, WindowToken token) {
        DisplayContent dc = this.mWmService.mRoot.getWindowTokenDisplay(token);
        if (dc != null) {
            throw new IllegalArgumentException("Can't map token=" + token + " to display=" + getName() + " already mapped to display=" + dc + " tokens=" + dc.mTokenMap);
        } else if (binder == null) {
            throw new IllegalArgumentException("Can't map token=" + token + " to display=" + getName() + " binder is null");
        } else if (token != null) {
            this.mTokenMap.put(binder, token);
            Slog.i(TAG, "addWindowToken: displayid:" + this.mDisplayId + " binder:" + binder + " token:" + token);
            if (token.asAppWindowToken() == null) {
                int i = token.windowType;
                if (i != 2040) {
                    if (i != 2103) {
                        switch (i) {
                            case 2011:
                            case 2012:
                                this.mImeWindowsContainers.addChild(token);
                                return;
                            case 2013:
                                break;
                            default:
                                this.mAboveAppWindowsContainers.addChild(token);
                                return;
                        }
                    }
                    this.mBelowAppWindowsContainers.addChild(token);
                    return;
                }
                this.mBelowAppWindowsContainers.addChild(token);
                reparentToHwOverlay(token.getPendingTransaction(), token.getSurfaceControl());
            }
        } else {
            throw new IllegalArgumentException("Can't map null token to display=" + getName() + " binder=" + binder);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowToken removeWindowToken(IBinder binder) {
        WindowToken token = this.mTokenMap.remove(binder);
        Slog.i(TAG, "removeWindowToken: displayid:" + this.mDisplayId + " binder:" + binder + " token:" + token);
        if (token != null && token.asAppWindowToken() == null) {
            token.setExiting();
        }
        return token;
    }

    /* access modifiers changed from: package-private */
    public void reParentWindowToken(WindowToken token) {
        DisplayContent prevDc = token.getDisplayContent();
        if (prevDc != this) {
            if (prevDc != null) {
                if (prevDc.mTokenMap.remove(token.token) != null && token.asAppWindowToken() == null) {
                    token.getParent().removeChild(token);
                }
                if (prevDc.mLastFocus == this.mCurrentFocus) {
                    prevDc.mLastFocus = null;
                }
            }
            addWindowToken(token.token, token);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeAppToken(IBinder binder) {
        WindowToken token = removeWindowToken(binder);
        if (token == null) {
            Slog.w(TAG, "removeAppToken: Attempted to remove non-existing token: " + binder);
            return;
        }
        AppWindowToken appToken = token.asAppWindowToken();
        if (appToken == null) {
            Slog.w(TAG, "Attempted to remove non-App token: " + binder + " token=" + token);
            return;
        }
        appToken.onRemovedFromDisplay();
    }

    public Display getDisplay() {
        return this.mDisplay;
    }

    /* access modifiers changed from: package-private */
    public DisplayInfo getDisplayInfo() {
        return this.mDisplayInfo;
    }

    /* access modifiers changed from: package-private */
    public DisplayMetrics getDisplayMetrics() {
        return this.mDisplayMetrics;
    }

    /* access modifiers changed from: package-private */
    public DisplayPolicy getDisplayPolicy() {
        return this.mDisplayPolicy;
    }

    public DisplayRotation getDisplayRotation() {
        return this.mDisplayRotation;
    }

    /* access modifiers changed from: package-private */
    public void setInsetProvider(int type, WindowState win, TriConsumer<DisplayFrames, WindowState, Rect> frameProvider) {
        this.mInsetsStateController.getSourceProvider(type).setWindow(win, frameProvider);
    }

    /* access modifiers changed from: package-private */
    public InsetsStateController getInsetsStateController() {
        return this.mInsetsStateController;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setDisplayRotation(DisplayRotation displayRotation) {
        this.mDisplayRotation = displayRotation;
    }

    public int getRotation() {
        return this.mRotation;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setRotation(int newRotation) {
        this.mRotation = newRotation;
        this.mDisplayRotation.setRotation(newRotation);
    }

    /* access modifiers changed from: package-private */
    public int getLastOrientation() {
        return this.mLastOrientation;
    }

    /* access modifiers changed from: package-private */
    public int getLastWindowForcedOrientation() {
        return this.mLastWindowForcedOrientation;
    }

    /* access modifiers changed from: package-private */
    public void registerRemoteAnimations(RemoteAnimationDefinition definition) {
        this.mAppTransitionController.registerRemoteAnimations(definition);
    }

    /* access modifiers changed from: package-private */
    public void pauseRotationLocked() {
        this.mDeferredRotationPauseCount++;
    }

    /* access modifiers changed from: package-private */
    public void resumeRotationLocked() {
        int i = this.mDeferredRotationPauseCount;
        if (i > 0) {
            this.mDeferredRotationPauseCount = i - 1;
            if (this.mDeferredRotationPauseCount == 0) {
                updateRotationAndSendNewConfigIfNeeded();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean rotationNeedsUpdate() {
        int lastOrientation = getLastOrientation();
        int oldRotation = getRotation();
        return oldRotation != this.mDisplayRotation.rotationForOrientation(lastOrientation, oldRotation);
    }

    /* access modifiers changed from: package-private */
    public void initializeDisplayOverrideConfiguration() {
        ActivityDisplay activityDisplay = this.mAcitvityDisplay;
        if (activityDisplay != null) {
            activityDisplay.onInitializeOverrideConfiguration(getRequestedOverrideConfiguration());
        }
    }

    /* access modifiers changed from: package-private */
    public void sendNewConfiguration() {
        this.mWmService.mH.obtainMessage(18, this).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean onDescendantOrientationChanged(IBinder freezeDisplayToken, ConfigurationContainer requestingContainer) {
        Configuration config = updateOrientationFromAppTokens(getRequestedOverrideConfiguration(), freezeDisplayToken, false);
        boolean handled = getDisplayRotation().respectAppRequestedOrientation();
        if (config == null) {
            return handled;
        }
        if (!handled || !(requestingContainer instanceof ActivityRecord)) {
            this.mWmService.mAtmService.updateDisplayOverrideConfigurationLocked(config, null, false, getDisplayId());
        } else {
            ActivityRecord activityRecord = (ActivityRecord) requestingContainer;
            boolean kept = this.mWmService.mAtmService.updateDisplayOverrideConfigurationLocked(config, activityRecord, false, getDisplayId());
            activityRecord.frozenBeforeDestroy = true;
            if (!kept) {
                this.mWmService.mAtmService.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
        }
        return handled;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean handlesOrientationChangeFromDescendant() {
        return getDisplayRotation().respectAppRequestedOrientation();
    }

    /* access modifiers changed from: package-private */
    public boolean updateOrientationFromAppTokens() {
        return updateOrientationFromAppTokens(false);
    }

    /* access modifiers changed from: package-private */
    public Configuration updateOrientationFromAppTokens(Configuration currentConfig, IBinder freezeDisplayToken, boolean forceUpdate) {
        AppWindowToken atoken;
        if (!this.mDisplayReady) {
            return null;
        }
        if (updateOrientationFromAppTokens(forceUpdate)) {
            if (!(freezeDisplayToken == null || this.mWmService.mRoot.mOrientationChangeComplete || (atoken = getAppWindowToken(freezeDisplayToken)) == null)) {
                atoken.startFreezingScreen();
            }
            Configuration config = new Configuration();
            computeScreenConfiguration(config);
            return config;
        } else if (currentConfig == null) {
            return null;
        } else {
            this.mTmpConfiguration.unset();
            this.mTmpConfiguration.updateFrom(currentConfig);
            computeScreenConfiguration(this.mTmpConfiguration);
            if (currentConfig.diff(this.mTmpConfiguration) == 0) {
                return null;
            }
            this.mWaitingForConfig = true;
            setLayoutNeeded();
            int[] anim = new int[2];
            getDisplayPolicy().selectRotationAnimationLw(anim);
            this.mWmService.startFreezingDisplayLocked(anim[0], anim[1], this);
            return new Configuration(this.mTmpConfiguration);
        }
    }

    private boolean updateOrientationFromAppTokens(boolean forceUpdate) {
        int req = getOrientation();
        IVRSystemServiceManager mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
        if (mVrMananger.isVRDeviceConnected() && mVrMananger.isValidVRDisplayId(this.mDisplayId)) {
            return false;
        }
        if (req == this.mLastOrientation && !forceUpdate) {
            return false;
        }
        getDisplayRotation().mHwDisplayRotationEx.startIntelliService(req);
        this.mLastOrientation = req;
        this.mDisplayRotation.setCurrentOrientation(req);
        return updateRotationUnchecked(forceUpdate);
    }

    /* access modifiers changed from: package-private */
    public boolean updateRotationAndSendNewConfigIfNeeded() {
        boolean changed = updateRotationUnchecked(false);
        if (changed) {
            sendNewConfiguration();
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public boolean updateRotationUnchecked() {
        return updateRotationUnchecked(false);
    }

    /* access modifiers changed from: package-private */
    public boolean updateRotationUnchecked(boolean forceUpdate) {
        int rotation;
        int rotationType;
        AppWindowToken appWindowToken;
        Task task;
        int i;
        if (this.mWmService.mAtmService.mHwATMSEx.isVirtualDisplayId(this.mDisplayId, "padCast")) {
            Slog.i(TAG, "It is pad cast display, terminal rotation");
            return false;
        }
        if (!forceUpdate) {
            if (this.mDeferredRotationPauseCount > 0) {
                Slog.i(TAG, "Deferring rotation, rotation is paused.");
                return false;
            }
            ScreenRotationAnimation screenRotationAnimation = this.mWmService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
            if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
                Slog.i(TAG, "Deferring rotation, animation in progress.");
                return false;
            } else if (this.mWmService.mDisplayFrozen) {
                Slog.i(TAG, "Deferring rotation, still finishing previous rotation");
                return false;
            }
        }
        if (!this.mWmService.mDisplayEnabled) {
            Slog.i(TAG, "Deferring rotation, display is not enabled.");
            return false;
        }
        int oldRotation = this.mRotation;
        int lastOrientation = this.mLastOrientation;
        if (!HwPCUtils.enabledInPad() || ((!HwPCUtils.isPcCastModeInServer() || ((i = this.mDisplayId) != 0 && !HwPCUtils.isValidExtDisplayId(i))) && (this.isDefaultDisplay || !"local:100000".equals(this.mDisplayInfo.uniqueId)))) {
            rotation = this.mDisplayRotation.rotationForOrientation(lastOrientation, oldRotation);
            if (HwMwUtils.ENABLED && HwMwUtils.performPolicy(61, new Object[]{Integer.valueOf(rotation)}).getBoolean("BUNDLE_IS_UPSIDEDOWN_ROTATION", false)) {
                return false;
            }
        } else {
            rotation = 1;
        }
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v(TAG, "Computed rotation=" + rotation + " for display id=" + this.mDisplayId + " based on lastOrientation=" + lastOrientation + " and oldRotation=" + oldRotation);
        }
        boolean mayRotateSeamlessly = this.mDisplayPolicy.shouldRotateSeamlessly(this.mDisplayRotation, oldRotation, rotation);
        if (this.mDividerControllerLocked.isResizing() || this.mWmService.isResizing()) {
            Slog.i(TAG, "it is now resizing, stop updateRotation");
            this.mWmService.setDockedStackDividerRotation(rotation);
            this.mWmService.mH.removeMessages(WindowManagerService.H.SPLIT_RESIZING_TIMEOUT);
            this.mWmService.mH.sendEmptyMessageDelayed(WindowManagerService.H.SPLIT_RESIZING_TIMEOUT, 600);
            return false;
        }
        if (mayRotateSeamlessly) {
            if (getWindow($$Lambda$DisplayContent$05CtqlkxQvjLanO8D5BmaCdILKQ.INSTANCE) != null && !forceUpdate) {
                return false;
            }
            if (hasPinnedStack()) {
                mayRotateSeamlessly = false;
            }
            int i2 = 0;
            while (true) {
                if (i2 >= this.mWmService.mSessions.size()) {
                    break;
                } else if (this.mWmService.mSessions.valueAt(i2).hasAlertWindowSurfaces()) {
                    mayRotateSeamlessly = false;
                    break;
                } else {
                    i2++;
                }
            }
        }
        if (this.mDisplayPolicy.isFromSeamlessLauncher() && (hasPinnedStack() || isHasAlertWindowSurfaces())) {
            mayRotateSeamlessly = true;
        }
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v(TAG, "Display id=" + this.mDisplayId + " selected orientation " + lastOrientation + ", got rotation " + rotation);
        }
        if (oldRotation == rotation) {
            Flog.i(308, "No changes, Selected orientation " + lastOrientation + ", got rotation " + rotation);
            if (this.mWmService.getWindowAnimator() != null && this.mWmService.getWindowAnimator().getScreenRotationAnimationLocked(0) == null) {
                this.mWmService.mHwWMSEx.setLandAnimationInfo(false, null);
            }
            return false;
        }
        if (!forceUpdate && HwFoldScreenState.isFoldScreenDevice()) {
            if (this.mFsmInternal == null) {
                this.mFsmInternal = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
            }
            HwFoldScreenManagerInternal hwFoldScreenManagerInternal = this.mFsmInternal;
            if (hwFoldScreenManagerInternal != null && hwFoldScreenManagerInternal.isPausedDispModeChange()) {
                Slog.i(TAG, "Deferring rotation, during display mode changing.");
                return false;
            }
        }
        this.mWmService.handlePauseDispModeChange();
        Flog.i(308, "Display id=" + this.mDisplayId + " rotation changed to " + rotation + " from " + oldRotation + ", lastOrientation=" + lastOrientation);
        if (deltaRotation(rotation, oldRotation) != 2) {
            this.mWaitingForConfig = true;
        }
        this.mRotation = rotation;
        setDisplayRotationFR(this.mRotation);
        this.mWmService.mWindowsFreezingScreen = 1;
        this.mWmService.mH.sendNewMessageDelayed(11, this, (long) WindowManagerService.WINDOW_FREEZE_TIMEOUT_DURATION);
        setLayoutNeeded();
        int[] anim = new int[2];
        this.mDisplayPolicy.selectRotationAnimationLw(anim);
        if (!mayRotateSeamlessly) {
            this.mWmService.startFreezingDisplayLocked(anim[0], anim[1], this);
        } else {
            this.mWmService.startSeamlessRotation();
        }
        this.mDisplayPolicy.notifyRotate(oldRotation, rotation);
        if (this.mCurrentFocus == null && (appWindowToken = this.mFocusedApp) != null && !appWindowToken.mHadTakenSnapShot && (task = this.mFocusedApp.getTask()) != null && task.isVisible() && task.getWindowingMode() == 1) {
            this.mWmService.getTaskSnapshotController().snapshotTasks(Sets.newArraySet(new Task[]{task}));
        }
        if (DisplayRotation.IS_SWING_ENABLED && (rotationType = this.mDisplayRotation.getHwDisplayRotationEx().getRotationType()) != 0) {
            IHwDisplayRotationEx hwDisplayRotationEx = this.mDisplayRotation.getHwDisplayRotationEx();
            AppWindowToken appWindowToken2 = this.mFocusedApp;
            hwDisplayRotationEx.reportRotation(rotationType, oldRotation, rotation, appWindowToken2 != null ? appWindowToken2.appPackageName : " ");
        }
        return true;
    }

    private boolean isHasAlertWindowSurfaces() {
        for (int i = 0; i < this.mWmService.mSessions.size(); i++) {
            if (this.mWmService.mSessions.valueAt(i).hasAlertWindowSurfaces()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void applyRotationLocked(int oldRotation, int rotation) {
        this.mDisplayRotation.setRotation(rotation);
        boolean rotateSeamlessly = this.mWmService.isRotatingSeamlessly();
        ScreenRotationAnimation screenRotationAnimation = rotateSeamlessly ? null : this.mWmService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
        updateDisplayAndOrientation(getConfiguration().uiMode, null);
        if (screenRotationAnimation != null && screenRotationAnimation.hasScreenshot() && screenRotationAnimation.setRotation(getPendingTransaction(), rotation, 10000, this.mWmService.getTransitionAnimationScaleLocked(), this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight)) {
            this.mWmService.scheduleAnimationLocked();
        }
        forAllWindows((Consumer<WindowState>) new Consumer(oldRotation, rotation, rotateSeamlessly) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$3g7y7M5XrDR3cz8tOp9f3pwWbyQ */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$applyRotationLocked$10$DisplayContent(this.f$1, this.f$2, this.f$3, (WindowState) obj);
            }
        }, true);
        this.mWmService.mDisplayManagerInternal.performTraversal(getPendingTransaction());
        scheduleAnimation();
        forAllWindows((Consumer<WindowState>) new Consumer(rotateSeamlessly) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$XeeexVnAosqA0zfHVCT_Txqwl8 */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$applyRotationLocked$11$DisplayContent(this.f$1, (WindowState) obj);
            }
        }, true);
        if (rotateSeamlessly) {
            int timeOutDuration = 2000;
            if (this.mDisplayPolicy.isFromSeamlessLauncher() && hasPinnedStack()) {
                timeOutDuration = 150;
            }
            this.mWmService.mH.sendNewMessageDelayed(54, this, (long) timeOutDuration);
            if (HwDisplaySizeUtil.hasSideInScreen()) {
                this.mWmService.getPolicy().notchControlFilletForSideScreen(this.mWmService.getFocusedWindow(), true);
            }
        }
        for (int i = this.mWmService.mRotationWatchers.size() - 1; i >= 0; i--) {
            WindowManagerService.RotationWatcher rotationWatcher = this.mWmService.mRotationWatchers.get(i);
            if (rotationWatcher.mDisplayId == this.mDisplayId) {
                try {
                    rotationWatcher.mWatcher.onRotationChanged(rotation);
                } catch (RemoteException e) {
                }
            }
        }
        if (screenRotationAnimation == null && this.mWmService.mAccessibilityController != null) {
            this.mWmService.mAccessibilityController.onRotationChangedLocked(this);
        }
        this.mWmService.mPolicy.notifyRotationChange(rotation);
        if (this.mSideSurfaceBox != null) {
            this.mWmService.setScreenSideBoxAndCornerVisibility(this.mDisplayId, false);
        }
    }

    public /* synthetic */ void lambda$applyRotationLocked$10$DisplayContent(int oldRotation, int rotation, boolean rotateSeamlessly, WindowState w) {
        w.seamlesslyRotateIfAllowed(getPendingTransaction(), oldRotation, rotation, rotateSeamlessly);
    }

    public /* synthetic */ void lambda$applyRotationLocked$11$DisplayContent(boolean rotateSeamlessly, WindowState w) {
        if (w.mHasSurface && !rotateSeamlessly) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Set mOrientationChanging of " + w);
            }
            w.setOrientationChanging(true);
            this.mWmService.mRoot.mOrientationChangeComplete = false;
            w.mLastFreezeDuration = 0;
        }
        w.mReportOrientationChanged = true;
    }

    /* access modifiers changed from: package-private */
    public void configureDisplayPolicy() {
        int longSize;
        int shortSize;
        if (this.mBaseDisplayDensity == 0) {
            Slog.e(TAG, "density is 0");
            return;
        }
        int width = this.mBaseDisplayWidth;
        int height = this.mBaseDisplayHeight;
        if (width > height) {
            shortSize = height;
            longSize = width;
        } else {
            shortSize = width;
            longSize = height;
        }
        int i = this.mBaseDisplayDensity;
        this.mDisplayPolicy.updateConfigurationAndScreenSizeDependentBehaviors();
        this.mDisplayRotation.configure(width, height, (shortSize * 160) / i, (longSize * 160) / i);
        DisplayFrames displayFrames = this.mDisplayFrames;
        DisplayInfo displayInfo = this.mDisplayInfo;
        displayFrames.onDisplayInfoUpdated(displayInfo, calculateDisplayCutoutForRotation(displayInfo.rotation));
        this.mIgnoreRotationForApps = isNonDecorDisplayCloseToSquare(0, width, height);
    }

    private boolean isNonDecorDisplayCloseToSquare(int rotation, int width, int height) {
        DisplayCutout displayCutout = calculateDisplayCutoutForRotation(rotation).getDisplayCutout();
        int uiMode = this.mWmService.mPolicy.getUiMode();
        int w = this.mDisplayPolicy.getNonDecorDisplayWidth(width, height, rotation, uiMode, displayCutout);
        int h = this.mDisplayPolicy.getNonDecorDisplayHeight(width, height, rotation, uiMode, displayCutout);
        return ((float) Math.max(w, h)) / ((float) Math.min(w, h)) <= this.mCloseToSquareMaxAspectRatio;
    }

    private DisplayInfo updateDisplayAndOrientation(int uiMode, Configuration outConfig) {
        int i = this.mRotation;
        boolean rotated = true;
        if (!(i == 1 || i == 3)) {
            rotated = false;
        }
        int dw = rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth;
        int dh = rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight;
        DisplayCutout displayCutout = calculateDisplayCutoutForRotation(this.mRotation).getDisplayCutout();
        int appWidth = this.mDisplayPolicy.getNonDecorDisplayWidth(dw, dh, this.mRotation, uiMode, displayCutout);
        int appHeight = this.mDisplayPolicy.getNonDecorDisplayHeight(dw, dh, this.mRotation, uiMode, displayCutout);
        DisplayInfo displayInfo = this.mDisplayInfo;
        displayInfo.rotation = this.mRotation;
        displayInfo.logicalWidth = dw;
        displayInfo.logicalHeight = dh;
        displayInfo.logicalDensityDpi = this.mBaseDisplayDensity;
        displayInfo.appWidth = appWidth;
        displayInfo.appHeight = appHeight;
        if (this.isDefaultDisplay) {
            displayInfo.getLogicalMetrics(this.mRealDisplayMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, (Configuration) null);
        }
        this.mDisplayInfo.displayCutout = displayCutout.isEmpty() ? null : displayCutout;
        this.mDisplayInfo.getAppMetrics(this.mDisplayMetrics);
        if (this.mDisplayScalingDisabled) {
            this.mDisplayInfo.flags |= 1073741824;
        } else {
            this.mDisplayInfo.flags &= -1073741825;
        }
        DisplayInfo overrideDisplayInfo = null;
        computeSizeRangesAndScreenLayout(this.mDisplayInfo, rotated, uiMode, dw, dh, this.mDisplayMetrics.density, outConfig);
        if (this.mShouldOverrideDisplayConfiguration) {
            overrideDisplayInfo = this.mDisplayInfo;
        }
        this.mWmService.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(this.mDisplayId, overrideDisplayInfo);
        this.mBaseDisplayRect.set(0, 0, dw, dh);
        if (this.isDefaultDisplay) {
            this.mCompatibleScreenScale = CompatibilityInfo.computeCompatibleScaling(this.mDisplayMetrics, this.mCompatDisplayMetrics);
        }
        return this.mDisplayInfo;
    }

    /* access modifiers changed from: package-private */
    public WmDisplayCutout calculateDisplayCutoutForRotation(int rotation) {
        if (HwDisplaySizeUtil.hasSideInScreen()) {
            this.mDisplayCutoutCache.clearCacheTable();
        }
        return this.mDisplayCutoutCache.getOrCompute(this.mInitialDisplayCutout, rotation);
    }

    /* access modifiers changed from: private */
    public WmDisplayCutout calculateDisplayCutoutForRotationUncached(DisplayCutout cutout, int rotation) {
        if (cutout == null || cutout == DisplayCutout.NO_CUTOUT) {
            return calculateDisplaySafeSideForRotation(WmDisplayCutout.NO_CUTOUT, rotation);
        }
        if (rotation == 0) {
            return calculateDisplaySafeSideForRotation(WmDisplayCutout.computeSafeInsets(cutout, this.mBaseDisplayWidth, this.mBaseDisplayHeight), rotation);
        }
        boolean rotated = true;
        if (!(rotation == 1 || rotation == 3)) {
            rotated = false;
        }
        return calculateDisplaySafeSideForRotation(WmDisplayCutout.computeSafeInsets(DisplayCutout.fromBounds(this.mRotationUtil.getRotatedBounds(WmDisplayCutout.computeSafeInsets(cutout, this.mBaseDisplayWidth, this.mBaseDisplayHeight).getDisplayCutout().getBoundingRectsAll(), rotation, this.mBaseDisplayWidth, this.mBaseDisplayHeight)), rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth, rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight), rotation);
    }

    private WmDisplayCutout calculateDisplaySafeSideForRotation(WmDisplayCutout wmCutout, int rotation) {
        if (HwDisplaySizeUtil.hasSideInScreen()) {
            Rect dsrRect = HwFrameworkFactory.getHwExtDisplaySizeUtil().getDisplaySideSafeInsets();
            if (rotation != 0) {
                if (rotation != 1) {
                    if (rotation != 2) {
                        if (rotation != 3) {
                            dsrRect = new Rect();
                        }
                    }
                }
                dsrRect = new Rect(dsrRect.top, dsrRect.right, dsrRect.bottom, dsrRect.left);
            }
            wmCutout.getDisplayCutout().setDisplaySideSafeInsets(dsrRect);
            wmCutout.getDisplayCutout().setDisplayWaterfallInsets(dsrRect);
        }
        return wmCutout;
    }

    /* access modifiers changed from: package-private */
    public void computeScreenConfiguration(Configuration config) {
        int i;
        int i2;
        int i3;
        boolean rotated;
        int i4;
        DisplayInfo displayInfo = updateDisplayAndOrientation(config.uiMode, config);
        calculateBounds(displayInfo, this.mTmpBounds);
        config.windowConfiguration.setBounds(this.mTmpBounds);
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        config.orientation = dw <= dh ? 1 : 2;
        config.windowConfiguration.setWindowingMode(getWindowingMode());
        config.windowConfiguration.setDisplayWindowingMode(getWindowingMode());
        config.windowConfiguration.setRotation(displayInfo.rotation);
        float density = this.mDisplayMetrics.density;
        config.screenWidthDp = (int) (((float) this.mDisplayPolicy.getConfigDisplayWidth(dw, dh, displayInfo.rotation, config.uiMode, displayInfo.displayCutout)) / density);
        config.screenHeightDp = (int) (((float) this.mDisplayPolicy.getConfigDisplayHeight(dw, dh, displayInfo.rotation, config.uiMode, displayInfo.displayCutout)) / density);
        this.mDisplayPolicy.getNonDecorInsetsLw(displayInfo.rotation, dw, dh, displayInfo.displayCutout, this.mTmpRect);
        int leftInset = this.mTmpRect.left;
        int topInset = this.mTmpRect.top;
        config.windowConfiguration.setAppBounds(leftInset, topInset, displayInfo.appWidth + leftInset, displayInfo.appHeight + topInset);
        boolean rotated2 = displayInfo.rotation == 1 || displayInfo.rotation == 3;
        int i5 = config.screenLayout & -769;
        if ((displayInfo.flags & 16) != 0) {
            i = 512;
        } else {
            i = 256;
        }
        config.screenLayout = i5 | i;
        config.compatScreenWidthDp = (int) (((float) config.screenWidthDp) / this.mCompatibleScreenScale);
        config.compatScreenHeightDp = (int) (((float) config.screenHeightDp) / this.mCompatibleScreenScale);
        config.compatSmallestScreenWidthDp = computeCompatSmallestWidth(rotated2, config.uiMode, dw, dh, displayInfo.displayCutout);
        config.densityDpi = displayInfo.logicalDensityDpi;
        if (!displayInfo.isHdr() || !this.mWmService.hasHdrSupport()) {
            i2 = 4;
        } else {
            i2 = 8;
        }
        if (!displayInfo.isWideColorGamut() || !this.mWmService.hasWideColorGamutSupport()) {
            i3 = 1;
        } else {
            i3 = 2;
        }
        config.colorMode = i2 | i3;
        config.touchscreen = 1;
        config.keyboard = 1;
        config.navigation = 1;
        int keyboardPresence = 0;
        int navigationPresence = 0;
        InputDevice[] devices = this.mWmService.mInputManager.getInputDevices();
        int len = devices != null ? devices.length : 0;
        int i6 = 0;
        while (i6 < len) {
            InputDevice device = devices[i6];
            if (device.isVirtual()) {
                rotated = rotated2;
            } else {
                rotated = rotated2;
                if (this.mWmService.mInputManager.canDispatchToDisplay(device.getId(), displayInfo.type == 5 ? 0 : this.mDisplayId)) {
                    int sources = device.getSources();
                    int presenceFlag = device.isExternal() ? 2 : 1;
                    if (!this.mWmService.mIsTouchDevice) {
                        config.touchscreen = 1;
                    } else if ((sources & 4098) == 4098) {
                        config.touchscreen = 3;
                    }
                    if ((sources & 65540) == 65540) {
                        config.navigation = 3;
                        navigationPresence |= presenceFlag;
                        i4 = 2;
                    } else if ((sources & 513) == 513 && config.navigation == 1) {
                        i4 = 2;
                        config.navigation = 2;
                        navigationPresence |= presenceFlag;
                    } else {
                        i4 = 2;
                    }
                    if (device.getKeyboardType() == i4) {
                        config.keyboard = i4;
                        keyboardPresence |= presenceFlag;
                    }
                }
            }
            i6++;
            rotated2 = rotated;
        }
        if (config.navigation == 1 && this.mWmService.mHasPermanentDpad) {
            config.navigation = 2;
            navigationPresence |= 1;
        }
        boolean hardKeyboardAvailable = config.keyboard != 1;
        if (hardKeyboardAvailable != this.mWmService.mHardKeyboardAvailable) {
            this.mWmService.mHardKeyboardAvailable = hardKeyboardAvailable;
            this.mWmService.mH.removeMessages(22);
            this.mWmService.mH.sendEmptyMessage(22);
        }
        this.mDisplayPolicy.updateConfigurationAndScreenSizeDependentBehaviors();
        config.keyboardHidden = 1;
        config.hardKeyboardHidden = 1;
        config.navigationHidden = 1;
        this.mWmService.mPolicy.adjustConfigurationLw(config, keyboardPresence, navigationPresence);
    }

    private int computeCompatSmallestWidth(boolean rotated, int uiMode, int dw, int dh, DisplayCutout displayCutout) {
        int unrotDh;
        int unrotDw;
        this.mTmpDisplayMetrics.setTo(this.mDisplayMetrics);
        DisplayMetrics tmpDm = this.mTmpDisplayMetrics;
        if (rotated) {
            unrotDw = dh;
            unrotDh = dw;
        } else {
            unrotDw = dw;
            unrotDh = dh;
        }
        return reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(0, 0, uiMode, tmpDm, unrotDw, unrotDh, displayCutout), 1, uiMode, tmpDm, unrotDh, unrotDw, displayCutout), 2, uiMode, tmpDm, unrotDw, unrotDh, displayCutout), 3, uiMode, tmpDm, unrotDh, unrotDw, displayCutout);
    }

    private int reduceCompatConfigWidthSize(int curSize, int rotation, int uiMode, DisplayMetrics dm, int dw, int dh, DisplayCutout displayCutout) {
        dm.noncompatWidthPixels = this.mDisplayPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode, displayCutout);
        dm.noncompatHeightPixels = this.mDisplayPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode, displayCutout);
        int size = (int) (((((float) dm.noncompatWidthPixels) / CompatibilityInfo.computeCompatibleScaling(dm, (DisplayMetrics) null)) / dm.density) + 0.5f);
        return (curSize == 0 || size < curSize) ? size : curSize;
    }

    private void computeSizeRangesAndScreenLayout(DisplayInfo displayInfo, boolean rotated, int uiMode, int dw, int dh, float density, Configuration outConfig) {
        int unrotDh;
        int unrotDw;
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
        adjustDisplaySizeRanges(displayInfo, 0, uiMode, unrotDw, unrotDh);
        adjustDisplaySizeRanges(displayInfo, 1, uiMode, unrotDh, unrotDw);
        adjustDisplaySizeRanges(displayInfo, 2, uiMode, unrotDw, unrotDh);
        adjustDisplaySizeRanges(displayInfo, 3, uiMode, unrotDh, unrotDw);
        if (outConfig != null) {
            int sl = reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(Configuration.resetScreenLayout(outConfig.screenLayout), 0, density, unrotDw, unrotDh, uiMode, displayInfo.displayCutout), 1, density, unrotDh, unrotDw, uiMode, displayInfo.displayCutout), 2, density, unrotDw, unrotDh, uiMode, displayInfo.displayCutout), 3, density, unrotDh, unrotDw, uiMode, displayInfo.displayCutout);
            outConfig.smallestScreenWidthDp = (int) (((float) displayInfo.smallestNominalAppWidth) / density);
            outConfig.screenLayout = sl;
        }
    }

    private int reduceConfigLayout(int curLayout, int rotation, float density, int dw, int dh, int uiMode, DisplayCutout displayCutout) {
        int longSize = this.mDisplayPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode, displayCutout);
        int shortSize = this.mDisplayPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode, displayCutout);
        if (longSize < shortSize) {
            longSize = shortSize;
            shortSize = longSize;
        }
        return Configuration.reduceScreenLayout(curLayout, (int) (((float) longSize) / density), (int) (((float) shortSize) / density));
    }

    private void adjustDisplaySizeRanges(DisplayInfo displayInfo, int rotation, int uiMode, int dw, int dh) {
        DisplayCutout displayCutout = calculateDisplayCutoutForRotation(rotation).getDisplayCutout();
        int width = this.mDisplayPolicy.getConfigDisplayWidth(dw, dh, rotation, uiMode, displayCutout);
        if (width < displayInfo.smallestNominalAppWidth) {
            displayInfo.smallestNominalAppWidth = width;
        }
        if (width > displayInfo.largestNominalAppWidth) {
            displayInfo.largestNominalAppWidth = width;
        }
        int height = this.mDisplayPolicy.getConfigDisplayHeight(dw, dh, rotation, uiMode, displayCutout);
        if (height < displayInfo.smallestNominalAppHeight) {
            displayInfo.smallestNominalAppHeight = height;
        }
        if (height > displayInfo.largestNominalAppHeight) {
            displayInfo.largestNominalAppHeight = height;
        }
    }

    /* access modifiers changed from: package-private */
    public int getPreferredOptionsPanelGravity() {
        int rotation = getRotation();
        if (this.mInitialDisplayWidth < this.mInitialDisplayHeight) {
            if (rotation != 1) {
                return (rotation == 2 || rotation != 3) ? 81 : 8388691;
            }
            return 85;
        } else if (rotation == 1) {
            return 81;
        } else {
            if (rotation != 2) {
                return rotation != 3 ? 85 : 81;
            }
            return 8388691;
        }
    }

    /* access modifiers changed from: package-private */
    public DockedStackDividerController getDockedDividerController() {
        return this.mDividerControllerLocked;
    }

    /* access modifiers changed from: package-private */
    public PinnedStackController getPinnedStackController() {
        return this.mPinnedStackControllerLocked;
    }

    /* access modifiers changed from: package-private */
    public boolean hasAccess(int uid) {
        return this.mDisplay.hasAccess(uid);
    }

    /* access modifiers changed from: package-private */
    public boolean isPrivate() {
        return (this.mDisplay.getFlags() & 4) != 0;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getHomeStack() {
        return this.mTaskStackContainers.getHomeStack();
    }

    /* access modifiers changed from: package-private */
    public TaskStack getSplitScreenPrimaryStack() {
        TaskStack stack = this.mTaskStackContainers.getSplitScreenPrimaryStack();
        if (stack == null || !stack.isVisible()) {
            return null;
        }
        return stack;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSplitScreenPrimaryStack() {
        return getSplitScreenPrimaryStack() != null;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getSplitScreenPrimaryStackIgnoringVisibility() {
        return this.mTaskStackContainers.getSplitScreenPrimaryStack();
    }

    /* access modifiers changed from: package-private */
    public TaskStack getVisibleHwMulitWindowStackLocked(int mode) {
        return this.mTaskStackContainers.getVisibleHwMulitWindowStackLocked(mode);
    }

    public WindowState findVisibleUnfloatingModeWindow() {
        Task topTask;
        TaskStack unFloatingStack = null;
        int i = this.mTaskStackContainers.getChildCount() - 1;
        while (true) {
            if (i < 0) {
                break;
            }
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (!(WindowConfiguration.isFloating(stack.getWindowingMode()) || stack.mActivityStack == null || stack.mActivityStack.topRunningActivityLocked(true) == null)) {
                unFloatingStack = stack;
                break;
            }
            i--;
        }
        if (unFloatingStack == null || (topTask = (Task) unFloatingStack.getTopChild()) == null) {
            return null;
        }
        WindowState win = topTask.getTopVisibleNonPermissionAppMainWindow();
        if (win == null && unFloatingStack.mActivityStack.shouldBeVisible(null)) {
            for (int i2 = topTask.getChildCount() - 1; i2 >= 0; i2--) {
                AppWindowToken appWindowToken = (AppWindowToken) topTask.getChildAt(i2);
                if (!appWindowToken.isPermissionApp()) {
                    return appWindowToken.findMainWindow();
                }
            }
        }
        return win;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getPinnedStack() {
        return this.mTaskStackContainers.getPinnedStack();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasPinnedStack() {
        return this.mTaskStackContainers.getPinnedStack() != null;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getTopStackInWindowingMode(int windowingMode) {
        return getStack(windowingMode, 0);
    }

    /* access modifiers changed from: package-private */
    public TaskStack getStack(int windowingMode, int activityType) {
        return this.mTaskStackContainers.getStack(windowingMode, activityType);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public WindowList<TaskStack> getStacks() {
        return this.mTaskStackContainers.mChildren;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public TaskStack getTopStack() {
        return this.mTaskStackContainers.getTopStack();
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Task> getVisibleTasks() {
        return this.mTaskStackContainers.getVisibleTasks();
    }

    /* access modifiers changed from: package-private */
    public void onStackWindowingModeChanged(TaskStack stack) {
        this.mTaskStackContainers.onStackWindowingModeChanged(stack);
    }

    @Override // com.android.server.wm.AbsDisplayContent, com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onRequestedOverrideConfigurationChanged(Configuration overrideConfiguration) {
        this.mWmService.mHwWMSEx.onRequestedOverrideConfigurationChanged(getDisplayId(), getConfiguration(), overrideConfiguration);
        super.onRequestedOverrideConfigurationChanged(overrideConfiguration);
    }

    @Override // com.android.server.wm.AbsDisplayContent, com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        int lastOrientation = getConfiguration().orientation;
        if (!HwPCUtils.enabledInPad() || HwPCUtils.isPcCastModeInServer() || this.isDefaultDisplay || !"local:100000".equals(this.mDisplayInfo.uniqueId) || this.mDisplayInfo.rotation != 1) {
            super.onConfigurationChanged(newParentConfig);
            DisplayPolicy displayPolicy = this.mDisplayPolicy;
            if (displayPolicy != null) {
                displayPolicy.onConfigurationChanged();
            }
            if (lastOrientation != getConfiguration().orientation) {
                getMetricsLogger().write(new LogMaker(1659).setSubtype(getConfiguration().orientation).addTaggedData(1660, Integer.valueOf(getDisplayId())));
            }
            if (this.mPinnedStackControllerLocked != null && !hasPinnedStack()) {
                this.mPinnedStackControllerLocked.onDisplayInfoChanged(getDisplayInfo());
                return;
            }
            return;
        }
        Slog.v(TAG, "onConfigurationChanged() not handle");
    }

    /* access modifiers changed from: package-private */
    public void preOnConfigurationChanged() {
        if (getDockedDividerController() != null) {
            getDockedDividerController().onConfigurationChanged();
        }
        if (getPinnedStackController() != null) {
            getPinnedStackController().onConfigurationChanged();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean fillsParent() {
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean isVisible() {
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onAppTransitionDone() {
        super.onAppTransitionDone();
        this.mWmService.mWindowsChanged = true;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void setWindowingMode(int windowingMode) {
        super.setWindowingMode(windowingMode);
        super.setDisplayWindowingMode(windowingMode);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public void setDisplayWindowingMode(int windowingMode) {
        setWindowingMode(windowingMode);
    }

    private boolean skipTraverseChild(WindowContainer child) {
        if (child != this.mImeWindowsContainers || this.mInputMethodTarget == null || hasSplitScreenPrimaryStack()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (traverseTopToBottom) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                DisplayChildWindowContainer child = (DisplayChildWindowContainer) this.mChildren.get(i);
                if (!skipTraverseChild(child) && child.forAllWindows(callback, traverseTopToBottom)) {
                    return true;
                }
            }
            return false;
        }
        int count = this.mChildren.size();
        for (int i2 = 0; i2 < count; i2++) {
            DisplayChildWindowContainer child2 = (DisplayChildWindowContainer) this.mChildren.get(i2);
            if (!skipTraverseChild(child2) && child2.forAllWindows(callback, traverseTopToBottom)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean forAllImeWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        return this.mImeWindowsContainers.forAllWindows(callback, traverseTopToBottom);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public int getOrientation() {
        WindowManagerPolicy policy = this.mWmService.mPolicy;
        if (this.mIgnoreRotationForApps && !HwFoldScreenState.isFoldScreenDevice()) {
            return 2;
        }
        if (!this.mWmService.mDisplayFrozen) {
            int orientation = this.mAboveAppWindowsContainers.getOrientation();
            if (orientation != -2) {
                return orientation;
            }
        } else if (this.mLastWindowForcedOrientation != -1) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Display id=" + this.mDisplayId + " is frozen, return " + this.mLastWindowForcedOrientation);
            }
            return this.mLastWindowForcedOrientation;
        } else if (policy.isKeyguardLocked()) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Display id=" + this.mDisplayId + " is frozen while keyguard locked, return " + this.mLastOrientation);
            }
            return this.mLastOrientation;
        }
        return this.mTaskStackContainers.getOrientation();
    }

    /* access modifiers changed from: package-private */
    public void updateDisplayInfo() {
        updateBaseDisplayMetricsIfNeeded();
        this.mDisplay.getDisplayInfo(this.mDisplayInfo);
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        onDisplayChanged(this);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onDisplayChanged(DisplayContent dc) {
        super.onDisplayChanged(dc);
        updateSystemGestureExclusionLimit();
    }

    /* access modifiers changed from: package-private */
    public void updateSystemGestureExclusionLimit() {
        this.mSystemGestureExclusionLimit = (this.mWmService.mSystemGestureExclusionLimitDp * this.mDisplayMetrics.densityDpi) / 160;
        updateSystemGestureExclusion();
    }

    /* access modifiers changed from: package-private */
    public void initializeDisplayBaseInfo() {
        DisplayInfo newDisplayInfo;
        DisplayManagerInternal displayManagerInternal = this.mWmService.mDisplayManagerInternal;
        if (!(displayManagerInternal == null || (newDisplayInfo = displayManagerInternal.getDisplayInfo(this.mDisplayId)) == null)) {
            this.mDisplayInfo.copyFrom(newDisplayInfo);
        }
        updateBaseDisplayMetrics(this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight, this.mDisplayInfo.logicalDensityDpi);
        this.mInitialDisplayWidth = this.mDisplayInfo.logicalWidth;
        this.mInitialDisplayHeight = this.mDisplayInfo.logicalHeight;
        this.mInitialDisplayDensity = this.mDisplayInfo.logicalDensityDpi;
        this.mInitialDisplayCutout = this.mDisplayInfo.displayCutout;
    }

    private void updateBaseDisplayMetricsIfNeeded() {
        DisplayInfo newDisplayInfo = new DisplayInfo();
        this.mWmService.mDisplayManagerInternal.getNonOverrideDisplayInfo(this.mDisplayId, newDisplayInfo);
        int orientation = newDisplayInfo.rotation;
        boolean isDisplayDensityForced = false;
        boolean rotated = orientation == 1 || orientation == 3;
        int newWidth = rotated ? newDisplayInfo.logicalHeight : newDisplayInfo.logicalWidth;
        int newHeight = rotated ? newDisplayInfo.logicalWidth : newDisplayInfo.logicalHeight;
        int newDensity = newDisplayInfo.logicalDensityDpi;
        DisplayCutout newCutout = newDisplayInfo.displayCutout;
        if ((this.mInitialDisplayWidth == newWidth && this.mInitialDisplayHeight == newHeight && this.mInitialDisplayDensity == newDensity && Objects.equals(this.mInitialDisplayCutout, newCutout)) ? false : true) {
            boolean isDisplaySizeForced = (this.mBaseDisplayWidth == this.mInitialDisplayWidth && this.mBaseDisplayHeight == this.mInitialDisplayHeight) ? false : true;
            if (this.mBaseDisplayDensity != this.mInitialDisplayDensity) {
                isDisplayDensityForced = true;
            }
            updateBaseDisplayMetrics(isDisplaySizeForced ? this.mBaseDisplayWidth : newWidth, isDisplaySizeForced ? this.mBaseDisplayHeight : newHeight, isDisplayDensityForced ? this.mBaseDisplayDensity : newDensity);
            this.mInitialDisplayWidth = newWidth;
            this.mInitialDisplayHeight = newHeight;
            if (HwActivityTaskManager.isCastDisplay(getDisplay().getUniqueId(), "padCast") && newWidth > newHeight && this.mRotation == 1) {
                this.mInitialDisplayWidth = newHeight;
                this.mInitialDisplayHeight = newWidth;
            }
            this.mInitialDisplayDensity = newDensity;
            this.mInitialDisplayCutout = newCutout;
            this.mWmService.reconfigureDisplayLocked(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void setMaxUiWidth(int width) {
        if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
            Slog.v(TAG, "Setting max ui width:" + width + " on display:" + getDisplayId());
        }
        this.mMaxUiWidth = width;
        updateBaseDisplayMetrics(this.mBaseDisplayWidth, this.mBaseDisplayHeight, this.mBaseDisplayDensity);
    }

    /* access modifiers changed from: package-private */
    public void updateBaseDisplayMetrics(int baseWidth, int baseHeight, int baseDensity) {
        int i;
        if (baseDensity == 0) {
            Slog.e(TAG, "density is 0", new Exception());
        }
        if (HwActivityTaskManager.isCastDisplay(getDisplay().getUniqueId(), "padCast")) {
            DisplayContent defaultDisplayContent = this.mWmService.getDefaultDisplayContentLocked();
            int defaultDisplayWidth = 0;
            int defaultDisplayHeight = 0;
            if (defaultDisplayContent != null) {
                defaultDisplayWidth = defaultDisplayContent.mBaseDisplayWidth;
                defaultDisplayHeight = defaultDisplayContent.mBaseDisplayHeight;
            }
            float defaultRatio2 = 0.0f;
            float baseRatio = baseWidth != 0 ? ((float) baseHeight) / ((float) baseWidth) : 0.0f;
            float defaultRatio1 = defaultDisplayWidth != 0 ? ((float) defaultDisplayHeight) / ((float) defaultDisplayWidth) : 0.0f;
            if (defaultDisplayHeight != 0) {
                defaultRatio2 = ((float) defaultDisplayWidth) / ((float) defaultDisplayHeight);
            }
            if (Math.abs(baseRatio - defaultRatio1) <= FLOAT_COMPARE_VALUE || Math.abs(baseRatio - defaultRatio2) <= FLOAT_COMPARE_VALUE) {
                this.mRotation = 0;
            } else {
                baseWidth = baseHeight;
                baseHeight = baseWidth;
                this.mRotation = 1;
            }
        }
        this.mBaseDisplayWidth = baseWidth;
        this.mBaseDisplayHeight = baseHeight;
        this.mBaseDisplayDensity = baseDensity;
        int i2 = this.mMaxUiWidth;
        if (i2 > 0 && (i = this.mBaseDisplayWidth) > i2) {
            this.mBaseDisplayHeight = (this.mBaseDisplayHeight * i2) / i;
            this.mBaseDisplayDensity = (this.mBaseDisplayDensity * i2) / i;
            this.mBaseDisplayWidth = i2;
            if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                Slog.v(TAG, "Applying config restraints:" + this.mBaseDisplayWidth + "x" + this.mBaseDisplayHeight + " at density:" + this.mBaseDisplayDensity + " on display:" + getDisplayId());
            }
        }
        this.mBaseDisplayRect.set(0, 0, this.mBaseDisplayWidth, this.mBaseDisplayHeight);
        updateBounds();
        this.mDisplayCutoutCache.clearCacheTable();
    }

    /* access modifiers changed from: package-private */
    public void setForcedDensity(int density, int userId) {
        boolean updateCurrent = true;
        if (density == this.mInitialDisplayDensity) {
        }
        if (userId != -2) {
            updateCurrent = false;
        }
        if (this.mWmService.mCurrentUserId == userId || updateCurrent) {
            if (density == 0) {
                Slog.e(TAG, "density is 0", new Exception());
            }
            this.mBaseDisplayDensity = density;
            this.mWmService.reconfigureDisplayLocked(this);
        }
        if (!updateCurrent) {
            if (density == this.mInitialDisplayDensity) {
                density = 0;
            }
            this.mWmService.mDisplayWindowSettings.setForcedDensity(this, density, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void setForcedScalingMode(int mode) {
        boolean z = true;
        if (mode != 1) {
            mode = 0;
        }
        if (mode == 0) {
            z = false;
        }
        this.mDisplayScalingDisabled = z;
        StringBuilder sb = new StringBuilder();
        sb.append("Using display scaling mode: ");
        sb.append(this.mDisplayScalingDisabled ? "off" : "auto");
        Slog.i(TAG, sb.toString());
        this.mWmService.reconfigureDisplayLocked(this);
        this.mWmService.mDisplayWindowSettings.setForcedScalingMode(this, mode);
    }

    /* access modifiers changed from: package-private */
    public void setForcedSize(int width, int height) {
        boolean clear = this.mInitialDisplayWidth == width && this.mInitialDisplayHeight == height;
        if (!clear) {
            width = Math.min(Math.max(width, 200), this.mInitialDisplayWidth * 2);
            height = Math.min(Math.max(height, 200), this.mInitialDisplayHeight * 2);
        }
        Slog.i(TAG, "Using new display size: " + width + "x" + height);
        updateBaseDisplayMetrics(width, height, this.mBaseDisplayDensity);
        this.mWmService.reconfigureDisplayLocked(this);
        this.mWmService.resetFoldScreenInfo();
        if (clear) {
            height = 0;
            width = 0;
        }
        this.mWmService.mDisplayWindowSettings.setForcedSize(this, width, height);
    }

    /* access modifiers changed from: package-private */
    public void getStableRect(Rect out) {
        out.set(this.mDisplayFrames.mStable);
    }

    /* access modifiers changed from: package-private */
    public void setStackOnDisplay(int stackId, boolean onTop, TaskStack stack) {
        this.mTaskStackContainers.addStackToDisplay(stack, onTop);
    }

    /* access modifiers changed from: package-private */
    public void moveStackToDisplay(TaskStack stack, boolean onTop) {
        DisplayContent prevDc = stack.getDisplayContent();
        if (prevDc == null) {
            throw new IllegalStateException("Trying to move stackId=" + stack.mStackId + " which is not currently attached to any display");
        } else if (prevDc.getDisplayId() != this.mDisplayId) {
            prevDc.mTaskStackContainers.removeChild(stack);
            this.mTaskStackContainers.addStackToDisplay(stack, onTop);
        } else {
            throw new IllegalArgumentException("Trying to move stackId=" + stack.mStackId + " to its current displayId=" + this.mDisplayId);
        }
    }

    /* access modifiers changed from: protected */
    public void addChild(DisplayChildWindowContainer child, Comparator<DisplayChildWindowContainer> comparator) {
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    /* access modifiers changed from: protected */
    public void addChild(DisplayChildWindowContainer child, int index) {
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    /* access modifiers changed from: protected */
    public void removeChild(DisplayChildWindowContainer child) {
        if (this.mRemovingDisplay) {
            super.removeChild((DisplayContent) child);
            return;
        }
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, DisplayChildWindowContainer child, boolean includingParents) {
        getParent().positionChildAt(position, this, includingParents);
    }

    /* access modifiers changed from: package-private */
    public void positionStackAt(int position, TaskStack child, boolean includingParents) {
        this.mTaskStackContainers.positionChildAt(position, child, includingParents);
        layoutAndAssignWindowLayersIfNeeded();
    }

    /* access modifiers changed from: package-private */
    public boolean pointWithinAppWindow(int x, int y) {
        int[] targetWindowType = {-1};
        Consumer fn = PooledLambda.obtainConsumer(new BiConsumer(targetWindowType, x, y) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$9GF6f8baPGZRvxJVeBknIuDUb_Y */
            private final /* synthetic */ int[] f$0;
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                DisplayContent.lambda$pointWithinAppWindow$12(this.f$0, this.f$1, this.f$2, (WindowState) obj, (Rect) obj2);
            }
        }, PooledLambda.__(WindowState.class), this.mTmpRect);
        forAllWindows((Consumer<WindowState>) fn, true);
        ((PooledConsumer) fn).recycle();
        return 1 <= targetWindowType[0] && targetWindowType[0] <= 99;
    }

    static /* synthetic */ void lambda$pointWithinAppWindow$12(int[] targetWindowType, int x, int y, WindowState w, Rect nonArg) {
        if (targetWindowType[0] == -1 && w.isOnScreen() && w.isVisibleLw() && w.getFrameLw().contains(x, y)) {
            targetWindowType[0] = w.mAttrs.type;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a0, code lost:
        if (r9.mTmpTaskForResizePointSearchResult.searchDone == false) goto L_0x0112;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a6, code lost:
        if (r9.mTmpTaskForResizePointSearchResult.taskForResize == null) goto L_0x0112;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b0, code lost:
        if (r9.mTmpTaskForResizePointSearchResult.taskForResize.inHwFreeFormWindowingMode() != false) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ba, code lost:
        if (r9.mTmpTaskForResizePointSearchResult.taskForResize.inFreeformWindowingMode() == false) goto L_0x0112;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00bc, code lost:
        r2 = r9.mAboveAppWindowsContainers.getChildCount() - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00c4, code lost:
        if (r2 < 0) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c6, code lost:
        r4 = (com.android.server.wm.WindowToken) r9.mAboveAppWindowsContainers.getChildAt(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00ce, code lost:
        if (r4 == null) goto L_0x0108;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d0, code lost:
        r5 = r4.getChildCount() - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d6, code lost:
        if (r5 < 0) goto L_0x0108;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00d8, code lost:
        r6 = (com.android.server.wm.WindowState) r4.getChildAt(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00de, code lost:
        if (r6 == null) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00e4, code lost:
        if (r6.isVisible() == false) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00ec, code lost:
        if ((r6.mAttrs.flags & 16) != 0) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00f4, code lost:
        if (r6.mAttrs.type == 2029) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00f6, code lost:
        r7 = new android.graphics.Region();
        r6.getTouchableRegion(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0102, code lost:
        if (r7.contains(r10, r11) == false) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0104, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0105, code lost:
        r5 = r5 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0108, code lost:
        r2 = r2 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x010f, code lost:
        if (isSupportDragInIme() != false) goto L_0x0112;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0111, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0116, code lost:
        if (r9.mTmpTaskForResizePointSearchResult.searchDone == false) goto L_0x011d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x011c, code lost:
        return r9.mTmpTaskForResizePointSearchResult.taskForResize;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x011d, code lost:
        return null;
     */
    public Task findTaskForResizePoint(int x, int y) {
        int resizeHandleDP = 30;
        if (HwPCUtils.isPcCastModeInServer()) {
            resizeHandleDP = 10;
        }
        int delta = WindowManagerService.dipToPixel(HwFreeFormUtils.isFreeFormEnable() ? 20 : resizeHandleDP, this.mDisplayMetrics);
        if (IS_HW_MULTIWINDOW_SUPPORTED) {
            delta = WindowManagerService.dipToPixel(20, this.mDisplayMetrics);
        }
        this.mTmpTaskForResizePointSearchResult.reset();
        int stackNdx = this.mTaskStackContainers.getChildCount();
        while (true) {
            stackNdx--;
            if (stackNdx >= 0) {
                TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(stackNdx);
                if (stack != null && (stack.getWindowConfiguration().canReceiveKeys() || stack.taskIdFromPoint(x, y) >= 0)) {
                    if (stack.getWindowConfiguration().canResizeTask() || ((stack.getWindowConfiguration().inHwFreeFormWindowingMode() && !stack.getWindowConfiguration().inHwTvMultiWindowingMode()) || (HwFreeFormUtils.isFreeFormEnable() && isStackVisible(5)))) {
                        stack.findTaskForResizePoint(x, y, delta, this.mTmpTaskForResizePointSearchResult);
                        if ((stack.getWindowingMode() == 102 && stack.taskIdFromPoint(x, y) > 0) || this.mTmpTaskForResizePointSearchResult.searchDone) {
                            break;
                        }
                    }
                }
            } else {
                break;
            }
        }
        return null;
    }

    private boolean isSupportDragInIme() {
        if (!isImeHolderForCurrentTask()) {
            return true;
        }
        int imeTop = (this.mInputMethodWindow.getDisplayFrameLw().top > this.mInputMethodWindow.getContentFrameLw().top ? this.mInputMethodWindow.getDisplayFrameLw() : this.mInputMethodWindow.getContentFrameLw()).top + this.mInputMethodWindow.getGivenContentInsetsLw().top;
        if (!this.mWmService.mHardKeyboardAvailable || getDisplayInfo().logicalHeight - imeTop >= getDisplayInfo().logicalHeight / 10) {
            return false;
        }
        return true;
    }

    public int getDragHeight() {
        return WindowManagerService.dipToPixel(90, this.mDisplayMetrics);
    }

    public int getInSizeOrOutWidth(int inOrOut) {
        if (inOrOut == 0) {
            return WindowManagerService.dipToPixel(5, this.mDisplayMetrics);
        }
        if (inOrOut == -1) {
            return WindowManagerService.dipToPixel(20, this.mDisplayMetrics);
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void updateTouchExcludeRegion() {
        TaskStack taskStack;
        Task task;
        AppWindowToken appWindowToken;
        WindowState popupWindowState;
        AppWindowToken appWindowToken2 = this.mFocusedApp;
        Task focusedTask = appWindowToken2 != null ? appWindowToken2.getTask() : null;
        if (focusedTask == null) {
            this.mTouchExcludeRegion.setEmpty();
        } else {
            this.mTouchExcludeRegion.set(this.mBaseDisplayRect);
            int delta = WindowManagerService.dipToPixel(HwFreeFormUtils.isFreeFormEnable() ? 20 : 30, this.mDisplayMetrics);
            this.mTmpRect2.setEmpty();
            for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).setTouchExcludeRegion(focusedTask, delta, this.mTouchExcludeRegion, this.mDisplayFrames.mContent, this.mTmpRect2);
            }
            if (!this.mTmpRect2.isEmpty()) {
                this.mTouchExcludeRegion.op(this.mTmpRect2, Region.Op.UNION);
            }
        }
        if (HwPCUtils.isPcCastModeInServer()) {
            if (!this.isDefaultDisplay) {
                this.mTouchExcludeRegion.setEmpty();
            }
            this.mPcTouchExcludeRegion.setEmpty();
        }
        WindowState windowState = this.mInputMethodWindow;
        if (windowState != null && windowState.isVisibleLw()) {
            this.mInputMethodWindow.getTouchableRegion(this.mTmpRegion);
            if (!HwPCUtils.isPcCastModeInServer()) {
                this.mTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
            } else if (this.mInputMethodWindow.getDisplayId() == this.mDisplayId) {
                this.mPcTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
            }
        }
        if (!(!HwPCUtils.isPcCastModeInServer() || this.mDisplayId != 0 || (taskStack = (TaskStack) this.mTaskStackContainers.getTopChild()) == null || (task = (Task) taskStack.getTopChild()) == null || (appWindowToken = (AppWindowToken) task.getTopChild()) == null || appWindowToken.appComponentName == null || !"com.huawei.desktop.systemui/com.huawei.systemui.mk.activity.ImitateActivity".equalsIgnoreCase(appWindowToken.appComponentName))) {
            Rect touchpadBounds = new Rect();
            task.getDimBounds(touchpadBounds);
            this.mPcTouchExcludeRegion.op(touchpadBounds, Region.Op.UNION);
            WindowState touchpadState = (WindowState) appWindowToken.getTopChild();
            if (!(touchpadState == null || (popupWindowState = (WindowState) touchpadState.getTopChild()) == null || popupWindowState.mAttrs.type != 1000)) {
                Region popupRegion = new Region();
                popupWindowState.getTouchableRegion(popupRegion);
                this.mPcTouchExcludeRegion.op(popupRegion, Region.Op.DIFFERENCE);
            }
        }
        for (int i = this.mTapExcludedWindows.size() - 1; i >= 0; i--) {
            WindowState win = this.mTapExcludedWindows.get(i);
            if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mDisplayId) || win.isVisible()) {
                win.getTouchableRegion(this.mTmpRegion);
                this.mTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
                if (HwPCUtils.isPcCastModeInServer() && this.mDisplayId == 0 && win.isVisible() && !this.mPcTouchExcludeRegion.isEmpty()) {
                    this.mPcTouchExcludeRegion.op(this.mTmpRegion, Region.Op.DIFFERENCE);
                }
            }
        }
        TaskTapPointerEventListener taskTapPointerEventListener = this.mTapDetector;
        if (taskTapPointerEventListener != null) {
            taskTapPointerEventListener.setHwPCTouchExcludeRegion(this.mPcTouchExcludeRegion);
        }
        amendWindowTapExcludeRegion(this.mTouchExcludeRegion);
        if (this.mDisplayId == 0 && getSplitScreenPrimaryStack() != null) {
            this.mDividerControllerLocked.getTouchRegion(this.mTmpRect);
            this.mTmpRegion.set(this.mTmpRect);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
        }
        this.mTapDetector.setTouchExcludeRegion(this.mTouchExcludeRegion);
    }

    /* access modifiers changed from: package-private */
    public void amendWindowTapExcludeRegion(Region inOutRegion) {
        for (int i = this.mTapExcludeProvidingWindows.size() - 1; i >= 0; i--) {
            this.mTapExcludeProvidingWindows.valueAt(i).amendTapExcludeRegion(inOutRegion);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void switchUser() {
        super.switchUser();
        this.mWmService.mWindowsChanged = true;
        this.mDisplayPolicy.switchUser();
    }

    private void resetAnimationBackgroundAnimator() {
        for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).resetAnimationBackgroundAnimator();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeIfPossible() {
        if (isAnimating()) {
            this.mDeferredRemoval = true;
        } else {
            removeImmediately();
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeImmediately() {
        this.mRemovingDisplay = true;
        try {
            this.mOpeningApps.clear();
            this.mClosingApps.clear();
            this.mChangingApps.clear();
            this.mUnknownAppVisibilityController.clear();
            this.mAppTransition.removeAppTransitionTimeoutCallbacks();
            handleAnimatingStoppedAndTransition();
            this.mWmService.stopFreezingDisplayLocked();
            super.removeImmediately();
            if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                Slog.v(TAG, "Removing display=" + this);
            }
            this.mPointerEventDispatcher.dispose();
            this.mWmService.mAnimator.removeDisplayLocked(this.mDisplayId);
            this.mWindowingLayer.release();
            this.mOverlayLayer.release();
            this.mHwSingleHandOverlayLayer.release();
            this.mInputMonitor.onDisplayRemoved();
            this.mDisplayReady = false;
            this.mRemovingDisplay = false;
            getPendingTransaction().apply();
            this.mWmService.mWindowPlacerLocked.requestTraversal();
        } catch (Throwable th) {
            this.mDisplayReady = false;
            this.mRemovingDisplay = false;
            getPendingTransaction().apply();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean checkCompleteDeferredRemoval() {
        if (super.checkCompleteDeferredRemoval() || !this.mDeferredRemoval) {
            return true;
        }
        removeImmediately();
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isRemovalDeferred() {
        return this.mDeferredRemoval;
    }

    /* access modifiers changed from: package-private */
    public boolean animateForIme(float interpolatedValue, float animationTarget, float dividerAnimationTarget) {
        boolean updated = false;
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (stack != null && stack.isAdjustedForIme()) {
                if (interpolatedValue >= 1.0f && animationTarget == 0.0f && dividerAnimationTarget == 0.0f) {
                    stack.resetAdjustedForIme(true);
                    updated = true;
                } else {
                    DockedStackDividerController dockedStackDividerController = this.mDividerControllerLocked;
                    dockedStackDividerController.mLastAnimationProgress = dockedStackDividerController.getInterpolatedAnimationValue(interpolatedValue);
                    DockedStackDividerController dockedStackDividerController2 = this.mDividerControllerLocked;
                    dockedStackDividerController2.mLastDividerProgress = dockedStackDividerController2.getInterpolatedDividerValue(interpolatedValue);
                    updated |= stack.updateAdjustForIme(this.mDividerControllerLocked.mLastAnimationProgress, this.mDividerControllerLocked.mLastDividerProgress, false);
                }
                if (interpolatedValue >= 1.0f) {
                    stack.endImeAdjustAnimation();
                }
            }
        }
        return updated;
    }

    /* access modifiers changed from: package-private */
    public boolean clearImeAdjustAnimation() {
        boolean changed = false;
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (stack != null && stack.isAdjustedForIme()) {
                stack.resetAdjustedForIme(true);
                changed = true;
            }
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public void beginImeAdjustAnimation() {
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (stack.isVisible() && stack.isAdjustedForIme()) {
                stack.beginImeAdjustAnimation();
            }
        }
    }

    private boolean isImeHolderForCurrentTask() {
        if (this.mCurrentFocus == null || this.mWmService == null || this.mWmService.mImeHolder == null || !isImeVisible()) {
            return false;
        }
        Task task = this.mCurrentFocus.getTask();
        TaskStack imeTargetStack = this.mWmService.mImeHolder.getStack();
        if (imeTargetStack == null) {
            return false;
        }
        return imeTargetStack.hasChild(task);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x015c  */
    public void adjustForImeIfNeeded() {
        TaskStack dockedStack;
        int imeDockSide;
        int imeHeight;
        boolean z;
        int i;
        boolean z2;
        WindowState imeWin = this.mInputMethodWindow;
        boolean imeVisible = imeWin != null && imeWin.isVisibleLw() && imeWin.isDisplayedLw() && !this.mDividerControllerLocked.isImeHideRequested();
        TaskStack imeTargetStack = this.mWmService.getImeFocusStackLocked();
        boolean isHwTopBottomSplitMode = imeTargetStack != null && imeTargetStack.inHwSplitScreenWindowingMode() && this.mWmService.mAtmService.mHwATMSEx.isSplitStackVisible(this.mAcitvityDisplay, 0);
        if (isHwTopBottomSplitMode) {
            dockedStack = getTopStackInWindowingMode(100);
        } else {
            dockedStack = getSplitScreenPrimaryStack();
        }
        boolean dockVisible = dockedStack != null;
        Task topDockedTask = dockVisible ? (Task) dockedStack.getTopChild() : null;
        if (!dockVisible || imeTargetStack == null) {
            imeDockSide = -1;
        } else if (!isHwTopBottomSplitMode || !imeTargetStack.inHwSplitScreenWindowingMode()) {
            imeDockSide = imeTargetStack.getDockSide();
        } else {
            imeDockSide = imeTargetStack.getHwDockSide();
        }
        boolean imeOnTop = imeDockSide == 2;
        int i2 = 4;
        boolean imeOnBottom = imeDockSide == 4;
        int imeHeight2 = this.mDisplayFrames.getInputMethodWindowVisibleHeight();
        boolean imeHeightChanged = imeVisible && imeHeight2 != this.mDividerControllerLocked.getImeHeightAdjustedFor();
        int i3 = this.mRotation;
        boolean rotated = i3 == 1 || i3 == 3;
        boolean dockMinimized = this.mDividerControllerLocked.isMinimizedDock() || (topDockedTask != null && imeOnBottom && !dockedStack.isAdjustedForIme() && dockedStack.getBounds().height() < topDockedTask.getBounds().height());
        if (imeVisible && dockVisible) {
            if (!imeOnTop && !imeOnBottom) {
                z = false;
                imeHeight = imeHeight2;
                for (i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
                    ((TaskStack) this.mTaskStackContainers.getChildAt(i)).resetAdjustedForIme(!dockVisible ? true : z);
                }
                DockedStackDividerController dockedStackDividerController = this.mDividerControllerLocked;
                if (imeVisible && dockVisible && rotated) {
                    z = true;
                }
                dockedStackDividerController.setAdjustedForIme(false, z, dockVisible, imeWin, imeHeight);
                this.mPinnedStackControllerLocked.setAdjustedForIme(imeVisible, imeHeight);
            } else if (!dockMinimized) {
                int i4 = this.mTaskStackContainers.getChildCount() - 1;
                while (i4 >= 0) {
                    TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i4);
                    if (stack.getDockSide() == i2) {
                    }
                    if (stack.isVisible() && imeOnBottom && !this.mInputMethodTargetWaitingAnim && ((stack.inSplitScreenWindowingMode() || stack.inHwSplitScreenWindowingMode()) && isImeHolderForCurrentTask())) {
                        stack.setAdjustedForIme(imeWin, imeOnBottom && imeHeightChanged);
                        z2 = false;
                    } else if (!this.mAcitvityDisplay.hasSplitScreenPrimaryStack()) {
                        stack.resetAdjustedForIme(!stack.inSplitScreenWindowingMode());
                        z2 = false;
                    } else {
                        z2 = false;
                        stack.resetAdjustedForIme(false);
                    }
                    i4--;
                    i2 = 4;
                }
                imeHeight = imeHeight2;
                this.mDividerControllerLocked.setAdjustedForIme(imeOnBottom, true, true, imeWin, imeHeight2);
                this.mPinnedStackControllerLocked.setAdjustedForIme(imeVisible, imeHeight);
            }
        }
        z = false;
        imeHeight = imeHeight2;
        while (i >= 0) {
        }
        DockedStackDividerController dockedStackDividerController2 = this.mDividerControllerLocked;
        z = true;
        dockedStackDividerController2.setAdjustedForIme(false, z, dockVisible, imeWin, imeHeight);
        this.mPinnedStackControllerLocked.setAdjustedForIme(imeVisible, imeHeight);
    }

    /* access modifiers changed from: package-private */
    public void prepareFreezingTaskBounds() {
        for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).prepareFreezingTaskBounds();
        }
    }

    /* access modifiers changed from: package-private */
    public void rotateBounds(int oldRotation, int newRotation, Rect bounds) {
        getBounds(this.mTmpRect, newRotation);
        rotateBounds(this.mTmpRect, oldRotation, newRotation, bounds);
    }

    /* access modifiers changed from: package-private */
    public void rotateBounds(Rect parentBounds, int oldRotation, int newRotation, Rect bounds) {
        createRotationMatrix(deltaRotation(newRotation, oldRotation), (float) parentBounds.width(), (float) parentBounds.height(), this.mTmpMatrix);
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
        if (rotation == 0) {
            outMatrix.reset();
        } else if (rotation == 1) {
            outMatrix.setRotate(90.0f, 0.0f, 0.0f);
            outMatrix.postTranslate(displayWidth, 0.0f);
            outMatrix.postTranslate(-rectTop, rectLeft);
        } else if (rotation == 2) {
            outMatrix.reset();
        } else if (rotation == 3) {
            outMatrix.setRotate(270.0f, 0.0f, 0.0f);
            outMatrix.postTranslate(0.0f, displayHeight);
            outMatrix.postTranslate(rectTop, 0.0f);
        }
    }

    @Override // com.android.server.wm.AbsDisplayContent, com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        if (logLevel != 2 || isVisible()) {
            long token = proto.start(fieldId);
            super.writeToProto(proto, 1146756268033L, logLevel);
            proto.write(1120986464258L, this.mDisplayId);
            for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).writeToProto(proto, 2246267895811L, logLevel);
            }
            this.mDividerControllerLocked.writeToProto(proto, 1146756268036L);
            this.mPinnedStackControllerLocked.writeToProto(proto, 1146756268037L);
            for (int i = this.mAboveAppWindowsContainers.getChildCount() - 1; i >= 0; i--) {
                ((WindowToken) this.mAboveAppWindowsContainers.getChildAt(i)).writeToProto(proto, 2246267895814L, logLevel);
            }
            for (int i2 = this.mBelowAppWindowsContainers.getChildCount() - 1; i2 >= 0; i2--) {
                ((WindowToken) this.mBelowAppWindowsContainers.getChildAt(i2)).writeToProto(proto, 2246267895815L, logLevel);
            }
            for (int i3 = this.mImeWindowsContainers.getChildCount() - 1; i3 >= 0; i3--) {
                ((WindowToken) this.mImeWindowsContainers.getChildAt(i3)).writeToProto(proto, 2246267895816L, logLevel);
            }
            proto.write(1120986464265L, this.mBaseDisplayDensity);
            this.mDisplayInfo.writeToProto(proto, 1146756268042L);
            proto.write(1120986464267L, this.mRotation);
            ScreenRotationAnimation screenRotationAnimation = this.mWmService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
            if (screenRotationAnimation != null) {
                screenRotationAnimation.writeToProto(proto, 1146756268044L);
            }
            this.mDisplayFrames.writeToProto(proto, 1146756268045L);
            this.mAppTransition.writeToProto(proto, 1146756268048L);
            AppWindowToken appWindowToken = this.mFocusedApp;
            if (appWindowToken != null) {
                appWindowToken.writeNameToProto(proto, 1138166333455L);
            }
            for (int i4 = this.mOpeningApps.size() - 1; i4 >= 0; i4--) {
                this.mOpeningApps.valueAt(i4).mActivityRecord.writeIdentifierToProto(proto, 2246267895825L);
            }
            for (int i5 = this.mClosingApps.size() - 1; i5 >= 0; i5--) {
                this.mClosingApps.valueAt(i5).mActivityRecord.writeIdentifierToProto(proto, 2246267895826L);
            }
            for (int i6 = this.mChangingApps.size() - 1; i6 >= 0; i6--) {
                this.mChangingApps.valueAt(i6).mActivityRecord.writeIdentifierToProto(proto, 2246267895827L);
            }
            proto.end(token);
        }
    }

    @Override // com.android.server.wm.WindowContainer
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        super.dump(pw, prefix, dumpAll);
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
        pw.print(subPrefix + "deferred=" + this.mDeferredRemoval + " mLayoutNeeded=" + this.mLayoutNeeded);
        StringBuilder sb = new StringBuilder();
        sb.append(" mTouchExcludeRegion=");
        sb.append(this.mTouchExcludeRegion);
        pw.println(sb.toString());
        pw.println();
        pw.print(prefix);
        pw.print("mLayoutSeq=");
        pw.println(this.mLayoutSeq);
        pw.print(prefix);
        pw.print("mDeferredRotationPauseCount=");
        pw.println(this.mDeferredRotationPauseCount);
        pw.print("  mCurrentFocus=");
        pw.println(this.mCurrentFocus);
        if (this.mLastFocus != this.mCurrentFocus) {
            pw.print("  mLastFocus=");
            pw.println(this.mLastFocus);
        }
        if (this.mLosingFocus.size() > 0) {
            pw.println();
            pw.println("  Windows losing focus:");
            for (int i = this.mLosingFocus.size() - 1; i >= 0; i--) {
                WindowState w = this.mLosingFocus.get(i);
                pw.print("  Losing #");
                pw.print(i);
                pw.print(' ');
                pw.print(w);
                if (dumpAll) {
                    pw.println(":");
                    w.dump(pw, "    ", true);
                } else {
                    pw.println();
                }
            }
        }
        pw.print("  mFocusedApp=");
        pw.println(this.mFocusedApp);
        if (this.mLastStatusBarVisibility != 0) {
            pw.print("  mLastStatusBarVisibility=0x");
            pw.println(Integer.toHexString(this.mLastStatusBarVisibility));
        }
        pw.println();
        this.mWallpaperController.dump(pw, "  ");
        pw.println();
        pw.print(prefix);
        pw.print("mDeferUpdateImeTargetCount=");
        pw.println(this.mDeferUpdateImeTargetCount);
        pw.println();
        pw.print("mSystemGestureExclusion=");
        if (this.mSystemGestureExclusionListeners.getRegisteredCallbackCount() > 0) {
            pw.println(this.mSystemGestureExclusion);
        } else {
            pw.println("<no lstnrs>");
        }
        pw.println();
        pw.println(prefix + "Application tokens in top down Z order:");
        for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx += -1) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).dump(pw, prefix + "  ", dumpAll);
        }
        pw.println();
        if (!this.mExitingTokens.isEmpty()) {
            pw.println();
            pw.println("  Exiting tokens:");
            for (int i2 = this.mExitingTokens.size() - 1; i2 >= 0; i2--) {
                WindowToken token = this.mExitingTokens.get(i2);
                pw.print("  Exiting #");
                pw.print(i2);
                pw.print(' ');
                pw.print(token);
                pw.println(':');
                token.dump(pw, "    ", dumpAll);
            }
        }
        pw.println();
        TaskStack homeStack = getHomeStack();
        if (homeStack != null) {
            pw.println(prefix + "homeStack=" + homeStack.getName());
        }
        TaskStack pinnedStack = getPinnedStack();
        if (pinnedStack != null) {
            pw.println(prefix + "pinnedStack=" + pinnedStack.getName());
        }
        TaskStack splitScreenPrimaryStack = getSplitScreenPrimaryStack();
        if (splitScreenPrimaryStack != null) {
            pw.println(prefix + "splitScreenPrimaryStack=" + splitScreenPrimaryStack.getName());
        }
        pw.println();
        this.mDividerControllerLocked.dump(prefix, pw);
        pw.println();
        this.mPinnedStackControllerLocked.dump(prefix, pw);
        pw.println();
        this.mDisplayFrames.dump(prefix, pw);
        pw.println();
        this.mDisplayPolicy.dump(prefix, pw);
        pw.println();
        this.mDisplayRotation.dump(prefix, pw);
        pw.println();
        this.mInputMonitor.dump(pw, "  ");
        pw.println();
        this.mInsetsStateController.dump(prefix, pw);
    }

    @Override // java.lang.Object
    public String toString() {
        return "Display " + this.mDisplayId + " info=" + this.mDisplayInfo + " stacks=" + this.mChildren;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public String getName() {
        return "Display " + this.mDisplayId + " name=\"" + this.mDisplayInfo.name + "\"";
    }

    /* access modifiers changed from: package-private */
    public boolean isStackVisible(int windowingMode) {
        TaskStack stack = getTopStackInWindowingMode(windowingMode);
        return stack != null && stack.isVisible();
    }

    /* access modifiers changed from: package-private */
    public WindowState getTouchableWinAtPointLocked(float xf, float yf) {
        return getWindow(new Predicate((int) xf, (int) yf) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$_XfE1uZ9VUv6i0SxWUvqu69FNb4 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.this.lambda$getTouchableWinAtPointLocked$13$DisplayContent(this.f$1, this.f$2, (WindowState) obj);
            }
        });
    }

    public /* synthetic */ boolean lambda$getTouchableWinAtPointLocked$13$DisplayContent(int x, int y, WindowState w) {
        int flags = w.mAttrs.flags;
        if (!w.isVisibleLw() || (flags & 16) != 0) {
            return false;
        }
        w.getVisibleBounds(this.mTmpRect);
        float scale = 1.0f;
        if (!(w.mAppToken == null || w.mAppToken.getStack() == null)) {
            scale = w.mAppToken.getStack().mHwStackScale;
        }
        Rect rect = this.mTmpRect;
        rect.set(rect.left, this.mTmpRect.top, (int) ((((float) (this.mTmpRect.right - this.mTmpRect.left)) * scale) + ((float) this.mTmpRect.left)), (int) ((((float) (this.mTmpRect.bottom - this.mTmpRect.top)) * scale) + ((float) this.mTmpRect.top)));
        if (!this.mTmpRect.contains(x, y) || this.mHwDisplayCotentEx.isPointOutsideMagicWindow(new WindowStateCommonEx(w), x, y) || (w.mAttrs.hwFlags & 262144) != 0) {
            return false;
        }
        w.getTouchableRegion(this.mTmpRegion);
        int touchFlags = flags & 40;
        if (this.mTmpRegion.contains(x, y) || touchFlags == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean canAddToastWindowForUid(int uid) {
        if (getWindow(new Predicate(uid) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$2VlyMN8z2sOPqE9yfz3peRMI */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.lambda$canAddToastWindowForUid$14(this.f$0, (WindowState) obj);
            }
        }) == null && getWindow(new Predicate(uid) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$JYsrGdifTPH6ASJDC3B9YWMD2pw */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.lambda$canAddToastWindowForUid$15(this.f$0, (WindowState) obj);
            }
        }) != null) {
            return false;
        }
        return true;
    }

    static /* synthetic */ boolean lambda$canAddToastWindowForUid$14(int uid, WindowState w) {
        return w.mOwnerUid == uid && w.isFocused();
    }

    static /* synthetic */ boolean lambda$canAddToastWindowForUid$15(int uid, WindowState w) {
        return w.mAttrs.type == 2005 && w.mOwnerUid == uid && !w.mPermanentlyHidden && !w.mWindowRemovalAllowed;
    }

    /* access modifiers changed from: package-private */
    public void scheduleToastWindowsTimeoutIfNeededLocked(WindowState oldFocus, WindowState newFocus) {
        if (oldFocus == null) {
            return;
        }
        if (newFocus == null || newFocus.mOwnerUid != oldFocus.mOwnerUid) {
            this.mTmpWindow = oldFocus;
            forAllWindows(this.mScheduleToastTimeout, false);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState findFocusedWindowIfNeeded(int topFocusedDisplayId) {
        if (this.mWmService.mPerDisplayFocusEnabled || topFocusedDisplayId == -1) {
            return findFocusedWindow();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowState findFocusedWindow() {
        this.mTmpWindow = null;
        forAllWindows(this.mFindFocusedWindow, true);
        WindowState windowState = this.mTmpWindow;
        if (windowState != null) {
            return windowState;
        }
        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
            Slog.v(TAG, "findFocusedWindow: No focusable windows.");
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0052, code lost:
        if (r16.mCurrentFocus != r4) goto L_0x0054;
     */
    public boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows, int topFocusedDisplayId) {
        WindowState newFocus = findFocusedWindowIfNeeded(topFocusedDisplayId);
        boolean hasOtherFocusedDisplay = false;
        if (this.mCurrentFocus == newFocus) {
            return false;
        }
        boolean imWindowChanged = false;
        if (this.mInputMethodWindow != null) {
            imWindowChanged = this.mInputMethodTarget != computeImeTarget(true);
            if (!(mode == 1 || mode == 3)) {
                assignWindowLayers(false);
            }
        }
        if (imWindowChanged) {
            this.mWmService.mWindowsChanged = true;
            setLayoutNeeded();
            newFocus = findFocusedWindowIfNeeded(topFocusedDisplayId);
        }
        if (this.mCurrentFocus != newFocus) {
            this.mWmService.mH.obtainMessage(2, this).sendToTarget();
        }
        if (!WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
            WindowManagerService windowManagerService = this.mWmService;
        }
        Slog.i(TAG, "Changing focus from " + this.mCurrentFocus + " to " + newFocus + " displayId=" + getDisplayId() + " Callers=" + Debug.getCallers(4));
        WindowState oldFocus = this.mCurrentFocus;
        this.mCurrentFocus = newFocus;
        if (this.mWmService.mRtgSchedSwitch) {
            this.mWmService.mHwWMSEx.sendFocusProcessToRMS(this.mCurrentFocus, oldFocus);
        }
        if (HwPCUtils.isPcCastModeInServer() && newFocus != null) {
            this.mCurrentFocusInHwPc = newFocus;
        }
        this.mLosingFocus.remove(newFocus);
        if (newFocus != null) {
            this.mWinAddedSinceNullFocus.clear();
            this.mWinRemovedSinceNullFocus.clear();
            if (newFocus.canReceiveKeys()) {
                newFocus.mToken.paused = false;
            }
        }
        int focusChanged = getDisplayPolicy().focusChangedLw(oldFocus, newFocus);
        if (imWindowChanged && oldFocus != this.mInputMethodWindow) {
            if (mode == 2) {
                performLayout(true, updateInputWindows);
                focusChanged &= -2;
            } else if (mode == 3) {
                assignWindowLayers(false);
            }
        }
        if ((focusChanged & 1) != 0) {
            setLayoutNeeded();
            if (mode == 2) {
                performLayout(true, updateInputWindows);
            } else if (mode == 4) {
                this.mWmService.mRoot.performSurfacePlacement(false);
            }
        }
        if (mode != 1) {
            getInputMonitor().setInputFocusLw(newFocus, updateInputWindows);
        }
        if (!this.mWmService.mPerDisplayFocusEnabled) {
            DisplayContent focusedContent = this.mWmService.mRoot.getDisplayContent(topFocusedDisplayId);
            if (this.mCurrentFocus == null && focusedContent != null) {
                hasOtherFocusedDisplay = true;
            }
            AppWindowTokenEx appWindowTokenEx = new AppWindowTokenEx();
            if (hasOtherFocusedDisplay) {
                WindowStateCommonEx windowStateEx = new WindowStateCommonEx(focusedContent.mCurrentFocus);
                appWindowTokenEx.setAppWindowToken(focusedContent.mFocusedApp);
                this.mHwDisplayCotentEx.focusWinZrHung(windowStateEx, appWindowTokenEx, focusedContent.getDisplayId());
            } else {
                WindowStateCommonEx windowStateEx2 = new WindowStateCommonEx(this.mCurrentFocus);
                appWindowTokenEx.setAppWindowToken(this.mFocusedApp);
                this.mHwDisplayCotentEx.focusWinZrHung(windowStateEx2, appWindowTokenEx, getDisplayId());
            }
        }
        adjustForImeIfNeeded();
        scheduleToastWindowsTimeoutIfNeededLocked(oldFocus, newFocus);
        if (mode != 2) {
            return true;
        }
        this.pendingLayoutChanges |= 8;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setFocusedApp(AppWindowToken newFocus) {
        AppWindowToken appWindowToken;
        DisplayContent appDisplay;
        if (newFocus != null && (appDisplay = newFocus.getDisplayContent()) != this) {
            StringBuilder sb = new StringBuilder();
            sb.append(newFocus);
            sb.append(" is not on ");
            sb.append(getName());
            sb.append(" but ");
            sb.append(appDisplay != null ? appDisplay.getName() : "none");
            throw new IllegalStateException(sb.toString());
        } else if (this.mFocusedApp == newFocus) {
            return false;
        } else {
            this.mFocusedApp = newFocus;
            this.mWmService.mAtmService.mHwATMSEx.onWindowFocusChangedForMultiDisplay(newFocus);
            if (HwPCUtils.isPcCastModeInServer() && (appWindowToken = this.mFocusedApp) != null && !appWindowToken.getDisplayContent().isDefaultDisplay) {
                this.mWmService.setPCLauncherFocused(false);
            }
            getInputMonitor().setFocusedAppLw(newFocus);
            updateTouchExcludeRegion();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void checkNeedNotifyFingerWinCovered() {
        boolean needNotify;
        boolean fingerWinCovered;
        WindowState topAboveAppWindow;
        WindowToken windowToken;
        WindowState windowState = this.mObserveWin;
        if (windowState != null && windowState.isVisibleOrAdding()) {
            for (int i = this.mAboveAppWindowsContainers.getChildCount() - 1; i >= 0; i--) {
                WindowToken windowToken2 = (WindowToken) this.mAboveAppWindowsContainers.getChildAt(i);
                if (windowToken2.mChildren.contains(this.mObserveWin)) {
                    this.mObserveToken = windowToken2;
                } else if (!(windowToken2.getTopChild() == null || !((WindowState) windowToken2.getTopChild()).isVisibleOrAdding() || ((WindowState) windowToken2.getTopChild()).getAttrs().type == 2000 || ((WindowState) windowToken2.getTopChild()).getAttrs().type == 2019 || ((windowToken = this.mTopAboveAppToken) != null && windowToken.getLayer() >= windowToken2.getLayer()))) {
                    this.mTopAboveAppToken = windowToken2;
                }
            }
            WindowToken windowToken3 = this.mObserveToken;
            if (windowToken3 == null || this.mTopAboveAppToken == null || windowToken3.getLayer() >= this.mTopAboveAppToken.getLayer()) {
                fingerWinCovered = false;
                needNotify = this.mWinEverCovered;
                this.mWinEverCovered = false;
            } else {
                fingerWinCovered = true;
                this.mWinEverCovered = true;
                needNotify = true;
            }
            if (needNotify) {
                Rect winFrame = new Rect();
                WindowToken windowToken4 = this.mTopAboveAppToken;
                if (!(windowToken4 == null || (topAboveAppWindow = (WindowState) windowToken4.getTopChild()) == null)) {
                    winFrame = topAboveAppWindow.getVisibleFrameLw();
                }
                this.mWmService.notifyFingerWinCovered(fingerWinCovered, winFrame);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void assignWindowLayers(boolean setLayoutNeeded) {
        Trace.traceBegin(32, "assignWindowLayers");
        assignChildLayers(getPendingTransaction());
        if (setLayoutNeeded) {
            setLayoutNeeded();
        }
        scheduleAnimation();
        Trace.traceEnd(32);
    }

    /* access modifiers changed from: package-private */
    public void layoutAndAssignWindowLayersIfNeeded() {
        this.mWmService.mWindowsChanged = true;
        setLayoutNeeded();
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(this.mDisplayId)) {
            this.mWmService.updateFocusedWindowLocked(3, false);
            assignWindowLayers(false);
        } else if (!this.mWmService.updateFocusedWindowLocked(3, false)) {
            assignWindowLayers(false);
        }
        this.mInputMonitor.setUpdateInputWindowsNeededLw();
        this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
        this.mInputMonitor.updateInputWindowsLw(false);
    }

    /* access modifiers changed from: package-private */
    public boolean destroyLeakedSurfaces() {
        this.mTmpWindow = null;
        forAllWindows((Consumer<WindowState>) new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$rF1ZhFUTWyZqcBK8Oea3g5uNlM */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$destroyLeakedSurfaces$16$DisplayContent((WindowState) obj);
            }
        }, false);
        return this.mTmpWindow != null;
    }

    public /* synthetic */ void lambda$destroyLeakedSurfaces$16$DisplayContent(WindowState w) {
        WindowStateAnimator wsa = w.mWinAnimator;
        if (wsa.mSurfaceController != null) {
            if (!this.mWmService.mSessions.contains(wsa.mSession)) {
                Slog.w(TAG, "LEAKED SURFACE (session doesn't exist): " + w + " surface=" + wsa.mSurfaceController + " token=" + w.mToken + " pid=" + w.mSession.mPid + " uid=" + w.mSession.mUid);
                wsa.destroySurface();
                this.mWmService.mForceRemoves.add(w);
                this.mTmpWindow = w;
            } else if (w.mAppToken != null && w.mAppToken.isClientHidden()) {
                Slog.w(TAG, "LEAKED SURFACE (app token hidden): " + w + " surface=" + wsa.mSurfaceController + " token=" + w.mAppToken);
                wsa.destroySurface();
                this.mTmpWindow = w;
            }
        }
    }

    private void updateSideScreenRoundCorner(WindowState win) {
        CharSequence focusedWindowTitle;
        if (win == null) {
            WindowState focusedWindow = getDisplayPolicy().getFocusedWindow();
            if (focusedWindow != null && this.mInputMethodTarget != null && (focusedWindowTitle = focusedWindow.getAttrs().getTitle()) != null && focusedWindowTitle.equals(this.mInputMethodTarget.getAttrs().getTitle())) {
                WindowState windowState = this.mInputMethodTarget;
                this.mWmService.getPolicy().notchControlFilletForSideScreen(windowState, !this.mDisplayPolicy.isCompressibleInputMethod(windowState));
            }
        } else if (!this.mDisplayPolicy.getHwDisplayPolicyEx().isNeedExceptDisplaySide(win.getAttrs(), win, this.mDisplayPolicy.getDisplayRotation())) {
            this.mWmService.getPolicy().notchControlFilletForSideScreen(win, !this.mDisplayPolicy.isCompressibleInputMethod(win));
        }
    }

    /* access modifiers changed from: package-private */
    public void setInputMethodWindowLocked(WindowState win) {
        WindowState windowState;
        if (win != null && (((windowState = this.mInputMethodWindow) == null || windowState != win) && this.mDisplayPolicy.isLeftRightSplitStackVisible())) {
            this.mDisplayPolicy.setFocusChangeIMEFrozenTag(false);
        }
        if (HwDisplaySizeUtil.hasSideInScreen()) {
            updateSideScreenRoundCorner(win);
        }
        this.mInputMethodWindow = win;
        WindowState windowState2 = this.mInputMethodWindow;
        if (windowState2 != null) {
            int imePid = windowState2.mSession.mPid;
            if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.enabledInPad() || !HwPCUtils.isValidExtDisplayId(this.mInputMethodWindow.getDisplayId())) {
                this.mWmService.mAtmInternal.onImeWindowSetOnDisplay(imePid, this.mInputMethodWindow.getDisplayId());
            } else {
                this.mWmService.mAtmInternal.onImeWindowSetOnDisplay(imePid, 0);
            }
        }
        computeImeTarget(true);
        this.mInsetsStateController.getSourceProvider(10).setWindow(win, null);
    }

    /* access modifiers changed from: package-private */
    public WindowState computeImeTarget(boolean updateImeTarget) {
        AppWindowToken token;
        WindowState betterTarget;
        AppWindowToken token2 = null;
        if (this.mInputMethodWindow == null) {
            if (updateImeTarget) {
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    Slog.w(TAG, "Moving IM target from " + this.mInputMethodTarget + " to null since mInputMethodWindow is null");
                }
                setInputMethodTarget(null, this.mInputMethodTargetWaitingAnim);
            }
            return null;
        }
        WindowState curTarget = this.mInputMethodTarget;
        if (!canUpdateImeTarget()) {
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.w(TAG, "Defer updating IME target:" + curTarget);
            }
            return curTarget;
        }
        this.mUpdateImeTarget = updateImeTarget;
        WindowState target = getWindow(this.mComputeImeTargetPredicate);
        if (!(target == null || target.mAttrs.type != 3 || (token = target.mAppToken) == null || (betterTarget = token.getImeTargetBelowWindow(target)) == null)) {
            target = betterTarget;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.v(TAG, "Proposed new IME target: " + target + " for display: " + getDisplayId() + " curTarget:" + curTarget);
        }
        if (curTarget == null || curTarget.mRemoved || !curTarget.isDisplayedLw() || !curTarget.isClosing() || !(target == null || curTarget.getWindowingMode() == target.getWindowingMode())) {
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.v(TAG, "Desired input method target=" + target + " updateImeTarget=" + updateImeTarget);
            }
            if (target == null) {
                if (updateImeTarget) {
                    if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                        Slog.w(TAG, "Moving IM target from " + curTarget + " to null.");
                    }
                    setInputMethodTarget(null, this.mInputMethodTargetWaitingAnim);
                }
                return null;
            }
            if (updateImeTarget) {
                if (curTarget != null) {
                    token2 = curTarget.mAppToken;
                }
                if (token2 != null) {
                    WindowState highestTarget = null;
                    if (token2.isSelfAnimating()) {
                        highestTarget = token2.getHighestAnimLayerWindow(curTarget);
                    }
                    if (highestTarget != null) {
                        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                            Slog.v(TAG, this.mAppTransition + " " + highestTarget + " animating=" + highestTarget.isAnimating());
                        }
                        if (this.mAppTransition.isTransitionSet()) {
                            setInputMethodTarget(highestTarget, true);
                            return highestTarget;
                        }
                    }
                }
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    Slog.w(TAG, "Moving IM target from " + curTarget + " to " + target + "");
                }
                setInputMethodTarget(target, false);
            }
            return target;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.v(TAG, "Not changing target till current window is closing and not removed");
        }
        return curTarget;
    }

    /* access modifiers changed from: package-private */
    public void computeImeTargetIfNeeded(AppWindowToken candidate) {
        WindowState windowState = this.mInputMethodTarget;
        if (windowState != null && windowState.mAppToken == candidate) {
            computeImeTarget(true);
        }
    }

    private void setInputMethodTarget(WindowState target, boolean targetWaitingAnim) {
        if (target != this.mInputMethodTarget || this.mInputMethodTargetWaitingAnim != targetWaitingAnim) {
            this.mInputMethodTarget = target;
            this.mDisplayPolicy.setInputMethodTargetWindow(target);
            this.mInputMethodTargetWaitingAnim = targetWaitingAnim;
            assignWindowLayers(false);
            this.mInsetsStateController.onImeTargetChanged(target);
            updateImeParent();
        } else if (target != null && target.inHwMagicWindowingMode()) {
            updateImeParent();
        }
    }

    private void updateImeParent() {
        SurfaceControl newParent = ((this.mMagnificationSpec != null) || this.mWmService.isInSubFoldScaleMode()) ? this.mWindowingLayer : computeImeParent();
        if (newParent != null) {
            getPendingTransaction().reparent(this.mImeWindowsContainers.mSurfaceControl, newParent);
            scheduleAnimation();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public SurfaceControl computeImeParent() {
        WindowState windowState = this.mInputMethodTarget;
        if (windowState == null || windowState.mAppToken == null || this.mInputMethodTarget.mAppToken.mActivityRecord == null || !this.mInputMethodTarget.mAppToken.mActivityRecord.isSplitMode() || this.mInputMethodTarget.mAppToken.mActivityRecord.isSplitBaseActivity()) {
            WindowState windowState2 = this.mInputMethodTarget;
            if (windowState2 == null || windowState2.mAppToken == null || this.mInputMethodTarget.getWindowingMode() != 1 || !this.mInputMethodTarget.mAppToken.matchParentBounds()) {
                return this.mWindowingLayer;
            }
            return this.mInputMethodTarget.mAppToken.getSurfaceControl();
        }
        Slog.i(TAG, "computeImeParent extend bounds for split mode");
        return this.mWindowingLayer;
    }

    /* access modifiers changed from: package-private */
    public boolean getNeedsMenu(WindowState top, WindowManagerPolicy.WindowState bottom) {
        if (top.mAttrs.needsMenuKey != 0) {
            return top.mAttrs.needsMenuKey == 1;
        }
        this.mTmpWindow = null;
        WindowState candidate = getWindow(new Predicate(top, bottom) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$jJlRHCiYzTPceX3tUkQ_1wUz71E */
            private final /* synthetic */ WindowState f$1;
            private final /* synthetic */ WindowManagerPolicy.WindowState f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.this.lambda$getNeedsMenu$17$DisplayContent(this.f$1, this.f$2, (WindowState) obj);
            }
        });
        return candidate != null && candidate.mAttrs.needsMenuKey == 1;
    }

    public /* synthetic */ boolean lambda$getNeedsMenu$17$DisplayContent(WindowState top, WindowManagerPolicy.WindowState bottom, WindowState w) {
        if (w == top) {
            this.mTmpWindow = w;
        }
        if (this.mTmpWindow == null) {
            return false;
        }
        if (w.mAttrs.needsMenuKey == 0 && w != bottom) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setLayoutNeeded() {
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.w(TAG, "setLayoutNeeded: callers=" + Debug.getCallers(3));
        }
        this.mLayoutNeeded = true;
    }

    private void clearLayoutNeeded() {
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.w(TAG, "clearLayoutNeeded: callers=" + Debug.getCallers(3));
        }
        this.mLayoutNeeded = false;
    }

    /* access modifiers changed from: package-private */
    public boolean isLayoutNeeded() {
        return this.mLayoutNeeded;
    }

    /* access modifiers changed from: package-private */
    public void dumpTokens(PrintWriter pw, boolean dumpAll) {
        if (!this.mTokenMap.isEmpty()) {
            pw.println("  Display #" + this.mDisplayId);
            for (WindowToken token : this.mTokenMap.values()) {
                pw.print("  ");
                pw.print(token);
                if (dumpAll) {
                    pw.println(':');
                    token.dump(pw, "    ", dumpAll);
                } else {
                    pw.println();
                }
            }
            if (!this.mOpeningApps.isEmpty() || !this.mClosingApps.isEmpty() || !this.mChangingApps.isEmpty()) {
                pw.println();
                if (this.mOpeningApps.size() > 0) {
                    pw.print("  mOpeningApps=");
                    pw.println(this.mOpeningApps);
                }
                if (this.mClosingApps.size() > 0) {
                    pw.print("  mClosingApps=");
                    pw.println(this.mClosingApps);
                }
                if (this.mChangingApps.size() > 0) {
                    pw.print("  mChangingApps=");
                    pw.println(this.mChangingApps);
                }
            }
            this.mUnknownAppVisibilityController.dump(pw, "  ");
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpWindowAnimators(PrintWriter pw, String subPrefix) {
        forAllWindows((Consumer<WindowState>) new Consumer(pw, subPrefix, new int[1]) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$iSsga4uJnJzBuUddn6uWEUo6xO8 */
            private final /* synthetic */ PrintWriter f$0;
            private final /* synthetic */ String f$1;
            private final /* synthetic */ int[] f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.lambda$dumpWindowAnimators$18(this.f$0, this.f$1, this.f$2, (WindowState) obj);
            }
        }, false);
    }

    static /* synthetic */ void lambda$dumpWindowAnimators$18(PrintWriter pw, String subPrefix, int[] index, WindowState w) {
        if (!w.toString().contains("hwSingleMode_window")) {
            WindowStateAnimator wAnim = w.mWinAnimator;
            pw.println(subPrefix + "Window #" + index[0] + ": " + wAnim);
            index[0] = index[0] + 1;
        }
    }

    /* access modifiers changed from: package-private */
    public void startKeyguardExitOnNonAppWindows(boolean onWallpaper, boolean goingToShade) {
        forAllWindows((Consumer<WindowState>) new Consumer(this.mWmService.mPolicy, onWallpaper, goingToShade) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$68_t1mHyvN9aDP5Tt_BKUPoYT8 */
            private final /* synthetic */ WindowManagerPolicy f$0;
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ boolean f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.lambda$startKeyguardExitOnNonAppWindows$19(this.f$0, this.f$1, this.f$2, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$startKeyguardExitOnNonAppWindows$19(WindowManagerPolicy policy, boolean onWallpaper, boolean goingToShade, WindowState w) {
        if (w.mAppToken == null && policy.canBeHiddenByKeyguardLw(w) && w.wouldBeVisibleIfPolicyIgnored() && !w.isVisible()) {
            w.startAnimation(policy.createHiddenByKeyguardExit(onWallpaper, goingToShade));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkWaitingForWindows() {
        this.mHaveBootMsg = false;
        this.mHaveApp = false;
        this.mHaveWallpaper = false;
        this.mHaveKeyguard = true;
        if (getWindow(new Predicate() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$BgTlvHbVclnASzMrvERWxyMVA */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.this.lambda$checkWaitingForWindows$20$DisplayContent((WindowState) obj);
            }
        }) != null) {
            return true;
        }
        boolean wallpaperEnabled = this.mWmService.mContext.getResources().getBoolean(17891453) && this.mWmService.mContext.getResources().getBoolean(17891394) && !this.mWmService.mOnlyCore;
        if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
            Slog.i(TAG, "******** booted=" + this.mWmService.mSystemBooted + " msg=" + this.mWmService.mShowingBootMessages + " haveBoot=" + this.mHaveBootMsg + " haveApp=" + this.mHaveApp + " haveWall=" + this.mHaveWallpaper + " wallEnabled=" + wallpaperEnabled + " haveKeyguard=" + this.mHaveKeyguard);
        }
        if (!this.mWmService.mSystemBooted && !this.mHaveBootMsg) {
            return true;
        }
        if (!this.mWmService.mSystemBooted || ((this.mHaveApp || this.mHaveKeyguard) && (!wallpaperEnabled || this.mHaveWallpaper))) {
            return false;
        }
        return true;
    }

    public /* synthetic */ boolean lambda$checkWaitingForWindows$20$DisplayContent(WindowState w) {
        if (w.isVisibleLw() && !w.mObscured && !w.isDrawnLw()) {
            return true;
        }
        if (!w.isDrawnLw()) {
            return false;
        }
        if (w.mAttrs.type == 2021) {
            this.mHaveBootMsg = true;
            return false;
        } else if (w.mAttrs.type == 2 || w.mAttrs.type == 4) {
            this.mHaveApp = true;
            return false;
        } else if (w.mAttrs.type == 2013) {
            this.mHaveWallpaper = true;
            return false;
        } else if (w.mAttrs.type != 2000) {
            return false;
        } else {
            this.mHaveKeyguard = this.mWmService.mPolicy.isKeyguardDrawnLw();
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateWindowsForAnimator() {
        forAllWindows(this.mUpdateWindowsForAnimator, true);
    }

    /* access modifiers changed from: package-private */
    public void updateBackgroundForAnimator() {
        resetAnimationBackgroundAnimator();
        forAllWindows(this.mUpdateWallpaperForAnimator, true);
    }

    /* access modifiers changed from: package-private */
    public boolean isInputMethodClientFocus(int uid, int pid) {
        WindowState imFocus = computeImeTarget(false);
        if (imFocus == null) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.i(TAG, "Desired input method target: " + imFocus);
            Slog.i(TAG, "Current focus: " + this.mCurrentFocus + " displayId=" + this.mDisplayId);
            Slog.i(TAG, "Last focus: " + this.mLastFocus + " displayId=" + this.mDisplayId);
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.i(TAG, "IM target uid/pid: " + imFocus.mSession.mUid + "/" + imFocus.mSession.mPid);
            Slog.i(TAG, "Requesting client uid/pid: " + uid + "/" + pid);
        }
        if (imFocus.mSession.mUid == uid && imFocus.mSession.mPid == pid) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSecureWindowOnScreen() {
        return getWindow($$Lambda$DisplayContent$5D_ifLpk7QwGe9ZLZynNnDca9g.INSTANCE) != null;
    }

    static /* synthetic */ boolean lambda$hasSecureWindowOnScreen$21(WindowState w) {
        return w.isOnScreen() && (w.mAttrs.flags & 8192) != 0;
    }

    /* access modifiers changed from: package-private */
    public void statusBarVisibilityChanged(int visibility) {
        this.mLastStatusBarVisibility = visibility;
        updateStatusBarVisibilityLocked(getDisplayPolicy().adjustSystemUiVisibilityLw(visibility));
    }

    private boolean updateStatusBarVisibilityLocked(int visibility) {
        int i = this.mLastDispatchedSystemUiVisibility;
        if (i == visibility || (visibility ^ i) == 16) {
            return false;
        }
        int globalDiff = (i ^ visibility) & 7 & (~visibility);
        this.mLastDispatchedSystemUiVisibility = visibility;
        if (this.isDefaultDisplay) {
            this.mWmService.mInputManager.setSystemUiVisibility(visibility);
        }
        updateSystemUiVisibility(visibility, globalDiff);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void updateSystemUiVisibility(int visibility, int globalDiff) {
        forAllWindows((Consumer<WindowState>) new Consumer(visibility, globalDiff) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$1C_u_mpQFfKL_O8K1VFzBgPg50 */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.lambda$updateSystemUiVisibility$22(this.f$0, this.f$1, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$updateSystemUiVisibility$22(int visibility, int globalDiff, WindowState w) {
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

    /* access modifiers changed from: package-private */
    public void reevaluateStatusBarVisibility() {
        if (updateStatusBarVisibilityLocked(getDisplayPolicy().adjustSystemUiVisibilityLw(this.mLastStatusBarVisibility))) {
            this.mWmService.mWindowPlacerLocked.requestTraversal();
        }
    }

    /* access modifiers changed from: package-private */
    public void onWindowFreezeTimeout() {
        Slog.w(TAG, "Window freeze timeout expired.");
        this.mWmService.mWindowsFreezingScreen = 2;
        forAllWindows((Consumer<WindowState>) new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$2HHBX1R6lnY5GedkE9LUBwsCPoE */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$onWindowFreezeTimeout$23$DisplayContent((WindowState) obj);
            }
        }, true);
        this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
    }

    public /* synthetic */ void lambda$onWindowFreezeTimeout$23$DisplayContent(WindowState w) {
        if (w.getOrientationChanging()) {
            w.orientationChangeTimedOut();
            w.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mWmService.mDisplayFreezeTime);
            Slog.w(TAG, "Force clearing orientation change: " + w);
        }
    }

    /* access modifiers changed from: package-private */
    public void waitForAllWindowsDrawn() {
        forAllWindows((Consumer<WindowState>) new Consumer(this.mWmService.mPolicy) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$oqhmXZMcpcvgI50swQTzosAcjac */
            private final /* synthetic */ WindowManagerPolicy f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$waitForAllWindowsDrawn$24$DisplayContent(this.f$1, (WindowState) obj);
            }
        }, true);
    }

    public /* synthetic */ void lambda$waitForAllWindowsDrawn$24$DisplayContent(WindowManagerPolicy policy, WindowState w) {
        boolean keyguard = policy.isKeyguardHostWindow(w.mAttrs);
        if (!w.isVisibleLw()) {
            return;
        }
        if (w.mAppToken != null || keyguard) {
            w.mWinAnimator.mDrawState = 1;
            w.resetLastContentInsets();
            this.mWmService.mWaitingForDrawn.add(w);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void applySurfaceChangesTransaction(boolean recoveringMemory) {
        WindowSurfacePlacer windowSurfacePlacer = this.mWmService.mWindowPlacerLocked;
        this.mTmpUpdateAllDrawn.clear();
        int repeats = 0;
        while (true) {
            repeats++;
            if (repeats > 6) {
                Slog.w(TAG, "Animation repeat aborted after too many iterations");
                clearLayoutNeeded();
                break;
            }
            if ((this.pendingLayoutChanges & 4) != 0) {
                this.mWallpaperController.adjustWallpaperWindows();
            }
            if ((this.pendingLayoutChanges & 2) != 0) {
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v(TAG, "Computing new config from layout");
                }
                if (updateOrientationFromAppTokens()) {
                    setLayoutNeeded();
                    sendNewConfiguration();
                }
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
            Trace.traceBegin(32, "applyPostLayoutPolicy");
            try {
                this.mDisplayPolicy.beginPostLayoutPolicyLw();
                forAllWindows(this.mApplyPostLayoutPolicy, true);
                this.pendingLayoutChanges |= this.mDisplayPolicy.finishPostLayoutPolicyLw();
                Trace.traceEnd(32);
                this.mInsetsStateController.onPostLayout();
                if (this.pendingLayoutChanges == 0) {
                    break;
                }
            } catch (Throwable th) {
                Trace.traceEnd(32);
                throw th;
            }
        }
        this.mTmpApplySurfaceChangesTransactionState.reset();
        this.mTmpRecoveringMemory = recoveringMemory;
        Trace.traceBegin(32, "applyWindowSurfaceChanges");
        try {
            this.mIsRequestFullScreen = false;
            forAllWindows(this.mApplySurfaceChangesTransaction, true);
            this.mWmService.mAtmService.mHwATMSEx.notifyFullScreenStateChange(this.mDisplayId, this.mIsRequestFullScreen);
            this.mWmService.mHwWMSEx.handleWindowsAfterTravel(getDisplayId());
            Trace.traceEnd(32);
            prepareSurfaces();
            this.mLastHasContent = this.mTmpApplySurfaceChangesTransactionState.displayHasContent;
            this.mWmService.mDisplayManagerInternal.setDisplayProperties(this.mDisplayId, this.mLastHasContent, this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate, this.mTmpApplySurfaceChangesTransactionState.preferredModeId, true);
            boolean wallpaperVisible = this.mWallpaperController.isWallpaperVisible();
            if (wallpaperVisible != this.mLastWallpaperVisible) {
                this.mLastWallpaperVisible = wallpaperVisible;
                this.mWmService.mWallpaperVisibilityListeners.notifyWallpaperVisibilityChanged(this);
            }
            while (!this.mTmpUpdateAllDrawn.isEmpty()) {
                this.mTmpUpdateAllDrawn.removeLast().updateAllDrawn();
            }
        } catch (Throwable th2) {
            Trace.traceEnd(32);
            throw th2;
        }
    }

    private void updateBounds() {
        if (this.mWmService.mAtmService.mHwATMSEx.isVirtualDisplayId(this.mDisplayId, "padCast") && this.mDisplayPolicy != null) {
            Configuration config = new Configuration();
            updateDisplayAndOrientation(config.uiMode, config);
        }
        calculateBounds(this.mDisplayInfo, this.mTmpBounds);
        setBounds(this.mTmpBounds);
        InputWindowHandle inputWindowHandle = this.mPortalWindowHandle;
        if (inputWindowHandle != null && this.mParentSurfaceControl != null) {
            inputWindowHandle.touchableRegion.getBounds(this.mTmpRect);
            if (!this.mTmpBounds.equals(this.mTmpRect)) {
                this.mPortalWindowHandle.touchableRegion.set(this.mTmpBounds);
                getPendingTransaction().setInputWindowInfo(this.mParentSurfaceControl, this.mPortalWindowHandle);
            }
        }
    }

    private void calculateBounds(DisplayInfo displayInfo, Rect out) {
        int rotation = displayInfo.rotation;
        boolean rotated = true;
        if (!(rotation == 1 || rotation == 3)) {
            rotated = false;
        }
        int physWidth = rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth;
        int physHeight = rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight;
        int width = displayInfo.logicalWidth;
        int left = (physWidth - width) / 2;
        int height = displayInfo.logicalHeight;
        int top = (physHeight - height) / 2;
        out.set(left, top, left + width, top + height);
    }

    private void getBounds(Rect out, int orientation) {
        getBounds(out);
        int rotationDelta = deltaRotation(this.mDisplayInfo.rotation, orientation);
        if (rotationDelta == 1 || rotationDelta == 3) {
            createRotationMatrix(rotationDelta, (float) this.mBaseDisplayWidth, (float) this.mBaseDisplayHeight, this.mTmpMatrix);
            this.mTmpRectF.set(out);
            this.mTmpMatrix.mapRect(this.mTmpRectF);
            this.mTmpRectF.round(out);
        }
    }

    /* access modifiers changed from: package-private */
    public int getNaturalOrientation() {
        if ((!this.mIsLandScapeDefault || this.mBaseDisplayWidth >= this.mBaseDisplayHeight) && this.mBaseDisplayWidth < this.mBaseDisplayHeight) {
            return 1;
        }
        return 2;
    }

    /* access modifiers changed from: package-private */
    public void performLayout(boolean initial, boolean updateInputWindows) {
        Trace.traceBegin(32, "performLayout");
        try {
            performLayoutNoTrace(initial, updateInputWindows);
        } finally {
            Trace.traceEnd(32);
        }
    }

    private void performLayoutNoTrace(boolean initial, boolean updateInputWindows) {
        if (isLayoutNeeded()) {
            clearLayoutNeeded();
            int dw = this.mDisplayInfo.logicalWidth;
            int dh = this.mDisplayInfo.logicalHeight;
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v(TAG, "-------------------------------------");
                Slog.v(TAG, "performLayout: needed=" + isLayoutNeeded() + " dw=" + dw + " dh=" + dh);
            }
            DisplayFrames displayFrames = this.mDisplayFrames;
            DisplayInfo displayInfo = this.mDisplayInfo;
            displayFrames.onDisplayInfoUpdated(displayInfo, calculateDisplayCutoutForRotation(displayInfo.rotation));
            this.mDisplayFrames.mRotation = this.mRotation;
            this.mDisplayPolicy.setNaviBarFlag(this.mFocusedApp, this.mInputMethodWindow);
            this.mDisplayPolicy.beginLayoutLw(this.mDisplayFrames, getConfiguration().uiMode);
            int seq = this.mLayoutSeq + 1;
            if (seq < 0) {
                seq = 0;
            }
            this.mLayoutSeq = seq;
            this.mTmpWindow = null;
            this.mTmpInitial = initial;
            forAllWindows(this.mPerformLayout, true);
            this.mTmpWindow2 = this.mTmpWindow;
            this.mTmpWindow = null;
            forAllWindows(this.mPerformLayoutAttached, true);
            this.mInputMonitor.layoutInputConsumers(dw, dh);
            this.mInputMonitor.setUpdateInputWindowsNeededLw();
            if (updateInputWindows) {
                this.mInputMonitor.updateInputWindowsLw(false);
            }
            this.mWmService.mH.sendEmptyMessage(41);
        }
    }

    /* access modifiers changed from: package-private */
    public Bitmap screenshotDisplayLocked(Bitmap.Config config) {
        if (!this.mWmService.mPolicy.isScreenOn()) {
            return null;
        }
        int dw = this.mDisplayInfo.logicalWidth;
        int dh = this.mDisplayInfo.logicalHeight;
        if (dw <= 0 || dh <= 0) {
            return null;
        }
        boolean inRotation = false;
        Rect frame = new Rect(0, 0, dw, dh);
        int rot = this.mDisplay.getRotation();
        int i = 3;
        if (rot == 1 || rot == 3) {
            if (rot != 1) {
                i = 1;
            }
            rot = i;
        }
        convertCropForSurfaceFlinger(frame, rot, dw, dh);
        ScreenRotationAnimation screenRotationAnimation = this.mWmService.mAnimator.getScreenRotationAnimationLocked(0);
        if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
            inRotation = true;
        }
        Bitmap bitmap = SurfaceControl.screenshot(frame, dw, dh, inRotation, rot);
        if (bitmap == null) {
            Slog.w(TAG, "Failed to take screenshot");
            return null;
        }
        Bitmap ret = bitmap.createAshmemBitmap(config);
        bitmap.recycle();
        return ret;
    }

    protected static void convertCropForSurfaceFlinger(Rect crop, int rot, int dw, int dh) {
        if (rot == 1) {
            int tmp = crop.top;
            crop.top = dw - crop.right;
            crop.right = crop.bottom;
            crop.bottom = dw - crop.left;
            crop.left = tmp;
        } else if (rot == 2) {
            int tmp2 = crop.top;
            crop.top = dh - crop.bottom;
            crop.bottom = dh - tmp2;
            int tmp3 = crop.right;
            crop.right = dw - crop.left;
            crop.left = dw - tmp3;
        } else if (rot == 3) {
            int tmp4 = crop.top;
            crop.top = crop.left;
            crop.left = dh - crop.bottom;
            crop.bottom = crop.right;
            crop.right = dh - tmp4;
        }
    }

    /* access modifiers changed from: package-private */
    public void onSeamlessRotationTimeout() {
        this.mTmpWindow = null;
        forAllWindows((Consumer<WindowState>) new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$vn2WRFHoZv7DB3bbwsmraKDpl0I */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$onSeamlessRotationTimeout$25$DisplayContent((WindowState) obj);
            }
        }, true);
        if (this.mTmpWindow != null) {
            this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
        }
        this.mWmService.handleResumeDispModeChange();
    }

    public /* synthetic */ void lambda$onSeamlessRotationTimeout$25$DisplayContent(WindowState w) {
        if (w.mSeamlesslyRotated) {
            this.mTmpWindow = w;
            w.setDisplayLayoutNeeded();
            w.finishSeamlessRotation(true);
            this.mWmService.markForSeamlessRotation(w, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void setExitingTokensHasVisible(boolean hasVisible) {
        for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
            this.mExitingTokens.get(i).hasVisible = hasVisible;
        }
        this.mTaskStackContainers.setExitingTokensHasVisible(hasVisible);
    }

    /* access modifiers changed from: package-private */
    public void removeExistingTokensIfPossible() {
        for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
            if (!this.mExitingTokens.get(i).hasVisible) {
                this.mExitingTokens.remove(i);
            }
        }
        this.mTaskStackContainers.removeExistingAppTokensIfPossible();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onDescendantOverrideConfigurationChanged() {
        setLayoutNeeded();
        this.mWmService.requestTraversal();
    }

    /* access modifiers changed from: package-private */
    public boolean okToDisplay() {
        if (this.mDisplayId == 0) {
            return !this.mWmService.mDisplayFrozen && this.mWmService.mDisplayEnabled && this.mWmService.mPolicy.isScreenOn();
        }
        if (this.mDisplayInfo.state != 0 || !"HUAWEI PAD PC Display".equals(this.mDisplayInfo.name)) {
            return this.mDisplayInfo.state == 2;
        }
        Slog.i(TAG, "okToDisplay, This is the Pad PC Display, return true. state = " + this.mDisplayInfo.state);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean okToAnimate() {
        return okToDisplay() && (this.mDisplayId != 0 || this.mWmService.mPolicy.okToAnimate());
    }

    /* access modifiers changed from: package-private */
    public static final class TaskForResizePointSearchResult {
        boolean searchDone;
        Task taskForResize;

        TaskForResizePointSearchResult() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.searchDone = false;
            this.taskForResize = null;
        }
    }

    private static final class ApplySurfaceChangesTransactionState {
        boolean displayHasContent;
        boolean obscured;
        int preferredModeId;
        float preferredRefreshRate;
        boolean syswin;

        private ApplySurfaceChangesTransactionState() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.displayHasContent = false;
            this.obscured = false;
            this.syswin = false;
            this.preferredRefreshRate = 0.0f;
            this.preferredModeId = 0;
        }
    }

    static final class ScreenshotApplicationState {
        WindowState appWin;
        int maxLayer;
        int minLayer;
        boolean screenshotReady;

        ScreenshotApplicationState() {
        }

        /* access modifiers changed from: package-private */
        public void reset(boolean screenshotReady2) {
            this.appWin = null;
            int i = 0;
            this.maxLayer = 0;
            this.minLayer = 0;
            this.screenshotReady = screenshotReady2;
            if (!screenshotReady2) {
                i = Integer.MAX_VALUE;
            }
            this.minLayer = i;
        }
    }

    /* access modifiers changed from: package-private */
    public static class DisplayChildWindowContainer<E extends WindowContainer> extends WindowContainer<E> {
        DisplayChildWindowContainer(WindowManagerService service) {
            super(service);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public boolean fillsParent() {
            return true;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public boolean isVisible() {
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public final class TaskStackContainers extends DisplayChildWindowContainer<TaskStack> {
        private boolean hasChanedImeLayerByHwFreeForm = false;
        SurfaceControl mAppAnimationLayer = null;
        SurfaceControl mAppHwFreeFormAnimationLayer = null;
        SurfaceControl mBoostedAppAnimationLayer = null;
        SurfaceControl mBoostedHwFreeFormAnimationLayer = null;
        private TaskStack mCoordinationPrimaryStack = null;
        SurfaceControl mHomeAppAnimationLayer = null;
        private TaskStack mHomeStack = null;
        private TaskStack mPinnedStack = null;
        SurfaceControl mSplitScreenDividerAnchor = null;
        private TaskStack mSplitScreenPrimaryStack = null;

        TaskStackContainers(WindowManagerService service) {
            super(service);
        }

        /* access modifiers changed from: package-private */
        public TaskStack getStack(int windowingMode, int activityType) {
            if (activityType == 2) {
                return this.mHomeStack;
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
            for (int i = DisplayContent.this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
                TaskStack stack = (TaskStack) DisplayContent.this.mTaskStackContainers.getChildAt(i);
                if (stack != null && ((activityType == 0 && windowingMode == stack.getWindowingMode()) || stack.isCompatible(windowingMode, activityType))) {
                    return stack;
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getVisibleHwMulitWindowStackLocked(int mode) {
            if (!(mode == 100 || mode == 101)) {
                return null;
            }
            for (int i = DisplayContent.this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
                TaskStack stack = (TaskStack) DisplayContent.this.mTaskStackContainers.getChildAt(i);
                if (stack != null) {
                    int windowingMode = stack.getWindowingMode();
                    if (windowingMode == 1) {
                        return null;
                    }
                    if (windowingMode == mode) {
                        return stack;
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public TaskStack getTopStack() {
            if (DisplayContent.this.mTaskStackContainers.getChildCount() > 0) {
                return (TaskStack) DisplayContent.this.mTaskStackContainers.getChildAt(DisplayContent.this.mTaskStackContainers.getChildCount() - 1);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getHomeStack() {
            if (this.mHomeStack == null && DisplayContent.this.mDisplayId == 0) {
                Slog.e(DisplayContent.TAG, "getHomeStack: Returning null from this=" + this);
            }
            return this.mHomeStack;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getPinnedStack() {
            return this.mPinnedStack;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getSplitScreenPrimaryStack() {
            return this.mSplitScreenPrimaryStack;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getCoordinationPrimaryStack() {
            return this.mCoordinationPrimaryStack;
        }

        /* access modifiers changed from: package-private */
        public ArrayList<Task> getVisibleTasks() {
            ArrayList<Task> visibleTasks = new ArrayList<>();
            forAllTasks(new Consumer(visibleTasks) {
                /* class com.android.server.wm.$$Lambda$DisplayContent$TaskStackContainers$rQnI0Y8R9ptQ09cGHwbCHDiG2FY */
                private final /* synthetic */ ArrayList f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DisplayContent.TaskStackContainers.lambda$getVisibleTasks$0(this.f$0, (Task) obj);
                }
            });
            return visibleTasks;
        }

        static /* synthetic */ void lambda$getVisibleTasks$0(ArrayList visibleTasks, Task task) {
            if (task.isVisible()) {
                visibleTasks.add(task);
            }
        }

        /* access modifiers changed from: package-private */
        public void addStackToDisplay(TaskStack stack, boolean onTop) {
            addStackReferenceIfNeeded(stack);
            addChild(stack, onTop);
            stack.onDisplayChanged(DisplayContent.this);
        }

        /* access modifiers changed from: package-private */
        public void onStackWindowingModeChanged(TaskStack stack) {
            removeStackReferenceIfNeeded(stack);
            addStackReferenceIfNeeded(stack);
            if (stack == this.mPinnedStack && getTopStack() != stack) {
                positionChildAt(Integer.MAX_VALUE, stack, false);
            }
        }

        private void addStackReferenceIfNeeded(TaskStack stack) {
            if (stack.isActivityTypeHome()) {
                if (this.mHomeStack == null) {
                    this.mHomeStack = stack;
                } else {
                    throw new IllegalArgumentException("addStackReferenceIfNeeded: home stack=" + this.mHomeStack + " already exist on display=" + this + " stack=" + stack);
                }
            }
            int windowingMode = stack.getWindowingMode();
            if (windowingMode == 2) {
                if (this.mPinnedStack == null) {
                    this.mPinnedStack = stack;
                    return;
                }
                throw new IllegalArgumentException("addStackReferenceIfNeeded: pinned stack=" + this.mPinnedStack + " already exist on display=" + this + " stack=" + stack);
            } else if (windowingMode == 3) {
                if (this.mSplitScreenPrimaryStack == null) {
                    this.mSplitScreenPrimaryStack = stack;
                    DisplayContent.this.mDividerControllerLocked.notifyDockedStackExistsChanged(true);
                    return;
                }
                throw new IllegalArgumentException("addStackReferenceIfNeeded: split-screen-primary stack=" + this.mSplitScreenPrimaryStack + " already exist on display=" + this + " stack=" + stack);
            } else if (windowingMode != 11) {
            } else {
                if (this.mCoordinationPrimaryStack == null) {
                    this.mCoordinationPrimaryStack = stack;
                    return;
                }
                throw new IllegalArgumentException("addStackReferenceIfNeeded: coordination_primary stack=" + this.mCoordinationPrimaryStack + " already exist on display=" + this + " stack=" + stack);
            }
        }

        private void removeStackReferenceIfNeeded(TaskStack stack) {
            if (stack == this.mHomeStack) {
                this.mHomeStack = null;
            } else if (stack == this.mPinnedStack) {
                this.mPinnedStack = null;
            } else if (stack == this.mSplitScreenPrimaryStack) {
                this.mSplitScreenPrimaryStack = null;
                this.mWmService.setDockedStackCreateStateLocked(0, null);
                DisplayContent.this.mDividerControllerLocked.notifyDockedStackExistsChanged(false);
            } else if (stack == this.mCoordinationPrimaryStack) {
                this.mCoordinationPrimaryStack = null;
                CoordinationModeUtils.getInstance(this.mWmService.mContext).setCoordinationCreateMode(0);
            }
        }

        private void addChild(TaskStack stack, boolean toTop) {
            addChild((TaskStackContainers) stack, findPositionForStack(toTop ? this.mChildren.size() : 0, stack, true));
            DisplayContent.this.setLayoutNeeded();
        }

        /* access modifiers changed from: protected */
        public void removeChild(TaskStack stack) {
            super.removeChild((TaskStackContainers) stack);
            removeStackReferenceIfNeeded(stack);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public boolean isOnTop() {
            return true;
        }

        /* access modifiers changed from: package-private */
        public void positionChildAt(int position, TaskStack child, boolean includingParents) {
            int topChildPosition;
            if (!child.getWindowConfiguration().isAlwaysOnTop() || position == Integer.MAX_VALUE) {
                int targetPosition = findPositionForStack(position, child, false);
                super.positionChildAt(targetPosition, (int) child, includingParents);
                if (includingParents && targetPosition < (topChildPosition = getChildCount() - 1) && position >= topChildPosition) {
                    getParent().positionChildAt(Integer.MAX_VALUE, this, true);
                }
                DisplayContent.this.setLayoutNeeded();
                return;
            }
            Slog.w(DisplayContent.TAG, "Ignoring move of always-on-top stack=" + this + " to bottom");
            super.positionChildAt(this.mChildren.indexOf(child), (int) child, false);
        }

        private int findPositionForStack(int requestedPosition, TaskStack stack, boolean adding) {
            if (stack.inPinnedWindowingMode()) {
                return Integer.MAX_VALUE;
            }
            int topChildPosition = this.mChildren.size() - 1;
            int belowAlwaysOnTopPosition = Integer.MIN_VALUE;
            int i = topChildPosition;
            while (true) {
                if (i >= 0) {
                    TaskStack curStack = DisplayContent.this.getStacks().get(i);
                    if (curStack != stack && !curStack.isAlwaysOnTop() && !DisplayContent.this.mAcitvityDisplay.isFreeformStackOnTop(curStack.mActivityStack)) {
                        belowAlwaysOnTopPosition = i;
                        break;
                    }
                    i--;
                } else {
                    break;
                }
            }
            int maxPosition = Integer.MAX_VALUE;
            int minPosition = Integer.MIN_VALUE;
            if (stack.isAlwaysOnTop()) {
                if (DisplayContent.this.hasPinnedStack()) {
                    maxPosition = DisplayContent.this.getStacks().indexOf(this.mPinnedStack) - 1;
                }
                minPosition = belowAlwaysOnTopPosition != Integer.MIN_VALUE ? belowAlwaysOnTopPosition : topChildPosition;
            } else {
                maxPosition = belowAlwaysOnTopPosition != Integer.MIN_VALUE ? belowAlwaysOnTopPosition : 0;
            }
            int targetPosition = Math.max(Math.min(requestedPosition, maxPosition), minPosition);
            int prevPosition = DisplayContent.this.getStacks().indexOf(stack);
            if (targetPosition == requestedPosition) {
                return targetPosition;
            }
            if (adding || targetPosition < prevPosition) {
                return targetPosition + 1;
            }
            return targetPosition;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
            if (traverseTopToBottom) {
                if (!super.forAllWindows(callback, traverseTopToBottom) && !forAllExitingAppTokenWindows(callback, traverseTopToBottom)) {
                    return false;
                }
                return true;
            } else if (!forAllExitingAppTokenWindows(callback, traverseTopToBottom) && !super.forAllWindows(callback, traverseTopToBottom)) {
                return false;
            } else {
                return true;
            }
        }

        private boolean forAllExitingAppTokenWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
            if (traverseTopToBottom) {
                for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                    AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                    for (int j = appTokens.size() - 1; j >= 0; j--) {
                        if (((AppWindowToken) appTokens.get(j)).forAllWindowsUnchecked(callback, traverseTopToBottom)) {
                            return true;
                        }
                    }
                }
                return false;
            }
            int count = this.mChildren.size();
            for (int i2 = 0; i2 < count; i2++) {
                AppTokenList appTokens2 = ((TaskStack) this.mChildren.get(i2)).mExitingAppTokens;
                int appTokensCount = appTokens2.size();
                for (int j2 = 0; j2 < appTokensCount; j2++) {
                    if (((AppWindowToken) appTokens2.get(j2)).forAllWindowsUnchecked(callback, traverseTopToBottom)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void setExitingTokensHasVisible(boolean hasVisible) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                for (int j = appTokens.size() - 1; j >= 0; j--) {
                    ((AppWindowToken) appTokens.get(j)).hasVisible = hasVisible;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void removeExistingAppTokensIfPossible() {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                for (int j = appTokens.size() - 1; j >= 0; j--) {
                    AppWindowToken token = (AppWindowToken) appTokens.get(j);
                    if (!token.hasVisible && !DisplayContent.this.mClosingApps.contains(token) && (!token.mIsExiting || token.isEmpty())) {
                        cancelAnimation();
                        Slog.v(DisplayContent.TAG, "performLayout: App token exiting now removed" + token);
                        token.removeIfPossible();
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public int getOrientation() {
            int orientation;
            if (DisplayContent.this.isStackVisible(3) || ((!HwFreeFormUtils.isFreeFormEnable() && DisplayContent.this.isStackVisible(5)) || (this.mDisplayContent != null && this.mWmService.mAtmService.mHwATMSEx.isSplitStackVisible(this.mDisplayContent.mAcitvityDisplay, -1)))) {
                TaskStack taskStack = this.mHomeStack;
                if (taskStack == null || !taskStack.isVisible() || !DisplayContent.this.mDividerControllerLocked.isMinimizedDock() || ((DisplayContent.this.mDividerControllerLocked.isHomeStackResizable() && this.mHomeStack.matchParentBounds()) || (orientation = this.mHomeStack.getOrientation()) == -2)) {
                    return -1;
                }
                return orientation;
            }
            int orientation2 = super.getOrientation();
            if (this.mWmService.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive")) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(DisplayContent.TAG, "Forcing UNSPECIFIED orientation in car for display id=" + DisplayContent.this.mDisplayId + ". Ignoring " + orientation2);
                }
                return -1;
            } else if (orientation2 == -2 || orientation2 == 3) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(DisplayContent.TAG, "No app is requesting an orientation, return " + DisplayContent.this.mLastOrientation + " for display id=" + DisplayContent.this.mDisplayId);
                }
                return DisplayContent.this.mLastOrientation;
            } else {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(DisplayContent.TAG, "App is requesting an orientation, return " + orientation2 + " for display id=" + DisplayContent.this.mDisplayId);
                }
                return orientation2;
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public void assignChildLayers(SurfaceControl.Transaction t) {
            assignStackOrdering(t);
            for (int i = 0; i < this.mChildren.size(); i++) {
                ((TaskStack) this.mChildren.get(i)).assignChildLayers(t);
            }
        }

        /* JADX INFO: Multiple debug info for r2v28 int: [D('layer' int), D('layerForHwFreeFormAnimationImeLayer' int)] */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Removed duplicated region for block: B:64:0x00eb  */
        /* JADX WARNING: Removed duplicated region for block: B:76:0x0113  */
        /* JADX WARNING: Removed duplicated region for block: B:90:0x014b  */
        /* JADX WARNING: Removed duplicated region for block: B:91:0x0154  */
        public void assignStackOrdering(SurfaceControl.Transaction t) {
            int NORMAL_STACK_STATE;
            int layer;
            TaskStack moveBackOrCloseStack;
            int layer2;
            int layer3;
            SurfaceControl surfaceControl;
            int HOME_STACK_STATE = 0;
            int NORMAL_STACK_STATE2 = 1;
            int layer4 = 0;
            int layerForAnimationLayer = 0;
            int layerForAnimationLayer2 = 0;
            int layerForHomeAnimationLayer = 0;
            boolean isInHwFreeFormAnimation = false;
            int layerForHwFreeFormAnimationLayer = 0;
            int layerForHwFreeFormAnimationLayer2 = 0;
            int layerForHwFreeFormAnimationImeLayer = 0;
            TaskStack moveBackOrCloseStack2 = null;
            int state = 0;
            while (state <= 2) {
                int layerForAnimationLayer3 = layer4;
                int i = 0;
                TaskStack moveBackOrCloseStack3 = moveBackOrCloseStack2;
                int layerForBoostedHwFreeFormAnimationLayer = layerForHwFreeFormAnimationImeLayer;
                int layerForHwFreeFormAnimationImeLayer2 = layerForHwFreeFormAnimationLayer2;
                int layerForHwFreeFormAnimationLayer3 = layerForHwFreeFormAnimationLayer;
                boolean isInHwFreeFormAnimation2 = isInHwFreeFormAnimation;
                int layerForBoostedAnimationLayer = layerForAnimationLayer2;
                int layerForAnimationLayer4 = layerForAnimationLayer;
                while (i < this.mChildren.size()) {
                    TaskStack s = (TaskStack) this.mChildren.get(i);
                    boolean notFreeform = s.isAlwaysOnTop() && HwFreeFormUtils.isFreeFormEnable() && !s.inFreeformWindowingMode() && !s.inHwFreeFormWindowingMode();
                    if ((state != 0 || s.isActivityTypeHome()) && ((state != 1 || ((!s.isActivityTypeHome() && !notFreeform) || s.inHwFreeFormMoveBackOrCloseState())) && (state != 2 || s.isAlwaysOnTop() || s.inHwFreeFormMoveBackOrCloseState() || s.inHwTvFreeFormWindowingMode()))) {
                        int layer5 = layerForAnimationLayer3 + 1;
                        s.assignLayer(t, layerForAnimationLayer3);
                        if (!s.inHwFreeFormMoveBackOrCloseState() || !s.inHwFreeFormWindowingMode()) {
                            moveBackOrCloseStack = moveBackOrCloseStack3;
                        } else {
                            moveBackOrCloseStack = s;
                        }
                        if (moveBackOrCloseStack != null && !s.inPinnedWindowingMode()) {
                            moveBackOrCloseStack.assignLayer(t, layer5);
                            layer5++;
                        }
                        if (!s.inSplitScreenWindowingMode() && !s.inHwSplitScreenWindowingMode() && !s.inHwMagicWindowingMode()) {
                            NORMAL_STACK_STATE = NORMAL_STACK_STATE2;
                        } else if (s.isVisible()) {
                            NORMAL_STACK_STATE = NORMAL_STACK_STATE2;
                            SurfaceControl surfaceControl2 = this.mSplitScreenDividerAnchor;
                            if (surfaceControl2 != null) {
                                layer2 = layer5 + 1;
                                t.setLayer(surfaceControl2, layer5);
                                if (s.isTaskAnimating() || s.isAppAnimating()) {
                                    if (state == 2) {
                                        if (s.inHwFreeFormWindowingMode()) {
                                            layer3 = layer2 + 1;
                                            layerForHwFreeFormAnimationLayer3 = layer2;
                                        } else {
                                            layer3 = layer2 + 1;
                                            layerForAnimationLayer4 = layer2;
                                        }
                                        if (!s.inHwMagicWindowingMode() || !s.isVisible() || (surfaceControl = this.mSplitScreenDividerAnchor) == null) {
                                            layer2 = layer3;
                                        } else {
                                            layer2 = layer3 + 1;
                                            t.setLayer(surfaceControl, layer3);
                                        }
                                    } else if (s.inHwFreeFormWindowingMode() && s.isAlwaysOnTop()) {
                                        int layerForHwFreeFormAnimationImeLayer3 = layer2 + 1;
                                        layerForHwFreeFormAnimationLayer3 = layer2;
                                        layer2 = layerForHwFreeFormAnimationImeLayer3 + 1;
                                        layerForHwFreeFormAnimationImeLayer2 = layerForHwFreeFormAnimationImeLayer3;
                                        isInHwFreeFormAnimation2 = true;
                                    }
                                }
                                if (s.inHwFreeFormWindowingMode() && s.getTopChild() != null && ((Task) s.getTopChild()).isHwFreeFormScaleAnimating()) {
                                    layerForAnimationLayer4 = layer2;
                                    layer2++;
                                    layerForHwFreeFormAnimationLayer3 = layerForAnimationLayer4;
                                }
                                layer = 2;
                                if (state == 2) {
                                    int layer6 = layer2 + 1;
                                    layerForBoostedAnimationLayer = layer2;
                                    layerForBoostedHwFreeFormAnimationLayer = layerForBoostedAnimationLayer;
                                    moveBackOrCloseStack3 = moveBackOrCloseStack;
                                    layerForAnimationLayer3 = layer6;
                                } else if (!s.inHwFreeFormWindowingMode() || !s.isAlwaysOnTop()) {
                                    moveBackOrCloseStack3 = moveBackOrCloseStack;
                                    layerForAnimationLayer3 = layer2;
                                } else {
                                    int layer7 = layer2 + 1;
                                    layerForBoostedHwFreeFormAnimationLayer = layer2;
                                    moveBackOrCloseStack3 = moveBackOrCloseStack;
                                    layerForAnimationLayer3 = layer7;
                                }
                            }
                        } else {
                            NORMAL_STACK_STATE = NORMAL_STACK_STATE2;
                        }
                        layer2 = layer5;
                        if (state == 2) {
                        }
                        layerForAnimationLayer4 = layer2;
                        layer2++;
                        layerForHwFreeFormAnimationLayer3 = layerForAnimationLayer4;
                        layer = 2;
                        if (state == 2) {
                        }
                    } else {
                        NORMAL_STACK_STATE = NORMAL_STACK_STATE2;
                        layer = 2;
                    }
                    i++;
                    HOME_STACK_STATE = HOME_STACK_STATE;
                    NORMAL_STACK_STATE2 = NORMAL_STACK_STATE;
                }
                if (state == 0) {
                    layer4 = layerForAnimationLayer3 + 1;
                    layerForHomeAnimationLayer = layerForAnimationLayer3;
                } else {
                    layer4 = layerForAnimationLayer3;
                }
                state++;
                layerForAnimationLayer = layerForAnimationLayer4;
                layerForAnimationLayer2 = layerForBoostedAnimationLayer;
                isInHwFreeFormAnimation = isInHwFreeFormAnimation2;
                layerForHwFreeFormAnimationLayer = layerForHwFreeFormAnimationLayer3;
                layerForHwFreeFormAnimationLayer2 = layerForHwFreeFormAnimationImeLayer2;
                layerForHwFreeFormAnimationImeLayer = layerForBoostedHwFreeFormAnimationLayer;
                moveBackOrCloseStack2 = moveBackOrCloseStack3;
                HOME_STACK_STATE = HOME_STACK_STATE;
                NORMAL_STACK_STATE2 = NORMAL_STACK_STATE2;
            }
            SurfaceControl surfaceControl3 = this.mAppAnimationLayer;
            if (surfaceControl3 != null) {
                t.setLayer(surfaceControl3, layerForAnimationLayer);
            }
            SurfaceControl surfaceControl4 = this.mBoostedAppAnimationLayer;
            if (surfaceControl4 != null) {
                t.setLayer(surfaceControl4, layerForAnimationLayer2);
            }
            SurfaceControl surfaceControl5 = this.mAppHwFreeFormAnimationLayer;
            if (surfaceControl5 != null) {
                t.setLayer(surfaceControl5, layerForHwFreeFormAnimationLayer);
                updateImeLayer(isInHwFreeFormAnimation, t, layerForHwFreeFormAnimationLayer2);
            }
            SurfaceControl surfaceControl6 = this.mBoostedHwFreeFormAnimationLayer;
            if (surfaceControl6 != null) {
                t.setLayer(surfaceControl6, layerForHwFreeFormAnimationImeLayer);
            }
            SurfaceControl surfaceControl7 = this.mHomeAppAnimationLayer;
            if (surfaceControl7 != null) {
                t.setLayer(surfaceControl7, layerForHomeAnimationLayer);
            }
        }

        private void updateImeLayer(boolean isInHwFreeFormAnimation, SurfaceControl.Transaction t, int layer) {
            if (DisplayContent.this.mImeWindowsContainers != null && t != null) {
                if (isInHwFreeFormAnimation) {
                    DisplayContent.this.mImeWindowsContainers.setLayer(t, layer);
                    this.hasChanedImeLayerByHwFreeForm = true;
                } else if (this.hasChanedImeLayerByHwFreeForm) {
                    SurfaceControl lastRelativeTo = DisplayContent.this.mImeWindowsContainers.getLayerLastRelativeTo();
                    if (lastRelativeTo != null && lastRelativeTo.isValid()) {
                        DisplayContent.this.mImeWindowsContainers.assignRelativeLayer(t, lastRelativeTo, 1);
                    }
                    this.hasChanedImeLayerByHwFreeForm = false;
                }
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public SurfaceControl getAppAnimationLayer(@WindowContainer.AnimationLayer int animationLayer) {
            if (animationLayer == 1) {
                return this.mBoostedAppAnimationLayer;
            }
            if (animationLayer == 2) {
                return this.mHomeAppAnimationLayer;
            }
            if (animationLayer == 10) {
                return this.mAppHwFreeFormAnimationLayer;
            }
            if (animationLayer != 11) {
                return this.mAppAnimationLayer;
            }
            return this.mBoostedHwFreeFormAnimationLayer;
        }

        /* access modifiers changed from: package-private */
        public SurfaceControl getSplitScreenDividerAnchor() {
            return this.mSplitScreenDividerAnchor;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
        public void onParentChanged() {
            super.onParentChanged();
            if (getParent() != null) {
                this.mAppAnimationLayer = makeChildSurface(null).setName("animationLayer").build();
                this.mBoostedAppAnimationLayer = makeChildSurface(null).setName("boostedAnimationLayer").build();
                this.mAppHwFreeFormAnimationLayer = makeChildSurface(null).setName("animationHwFreeFormLayer").build();
                this.mBoostedHwFreeFormAnimationLayer = makeChildSurface(null).setName("boostedHwFreeFormAnimationLayer").build();
                this.mHomeAppAnimationLayer = makeChildSurface(null).setName("homeAnimationLayer").build();
                this.mSplitScreenDividerAnchor = makeChildSurface(null).setName("splitScreenDividerAnchor").build();
                getPendingTransaction().show(this.mAppAnimationLayer).show(this.mBoostedAppAnimationLayer).show(this.mAppHwFreeFormAnimationLayer).show(this.mBoostedHwFreeFormAnimationLayer).show(this.mHomeAppAnimationLayer).show(this.mSplitScreenDividerAnchor);
                scheduleAnimation();
                return;
            }
            this.mAppAnimationLayer.remove();
            this.mAppAnimationLayer = null;
            this.mBoostedAppAnimationLayer.remove();
            this.mBoostedAppAnimationLayer = null;
            this.mAppHwFreeFormAnimationLayer.remove();
            this.mAppHwFreeFormAnimationLayer = null;
            this.mBoostedHwFreeFormAnimationLayer.remove();
            this.mBoostedHwFreeFormAnimationLayer = null;
            this.mHomeAppAnimationLayer.remove();
            this.mHomeAppAnimationLayer = null;
            this.mSplitScreenDividerAnchor.remove();
            this.mSplitScreenDividerAnchor = null;
        }
    }

    /* access modifiers changed from: protected */
    public final class AboveAppWindowContainers extends NonAppWindowContainers {
        AboveAppWindowContainers(String name, WindowManagerService service) {
            super(name, service);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public SurfaceControl.Builder makeChildSurface(WindowContainer child) {
            SurfaceControl.Builder builder = super.makeChildSurface(child);
            if ((child instanceof WindowToken) && ((WindowToken) child).mRoundedCornerOverlay) {
                builder.setParent(null);
            }
            return builder;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public void assignChildLayers(SurfaceControl.Transaction t) {
            assignChildLayers(t, null);
        }

        /* access modifiers changed from: package-private */
        public boolean isNeedIgnoreAssignIme(boolean needAssignIme) {
            if (!DisplayContent.IS_HW_MULTIWINDOW_SUPPORTED && needAssignIme && DisplayContent.this.getDisplayPolicy().isLeftRightSplitStackVisible() && DisplayContent.this.mInputMethodWindow != null && DisplayContent.this.getDisplayPolicy().isBaiDuOrSwiftkey(DisplayContent.this.mInputMethodWindow)) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void assignChildLayers(SurfaceControl.Transaction t, WindowContainer imeContainer) {
            boolean needAssignIme = (imeContainer == null || imeContainer.getSurfaceControl() == null) ? false : true;
            if (DisplayContent.this.mSideSurfaceBox != null) {
                DisplayContent.this.mSideSurfaceBox.setLayer(t, null, 0);
            }
            for (int j = 0; j < this.mChildren.size(); j++) {
                WindowToken wt = (WindowToken) this.mChildren.get(j);
                if (wt.windowType == 2034 || wt.windowType == 2029) {
                    wt.assignRelativeLayer(t, DisplayContent.this.mTaskStackContainers.getSplitScreenDividerAnchor(), 1);
                    if (isNeedIgnoreAssignIme(needAssignIme)) {
                        imeContainer.assignRelativeLayer(t, wt.getSurfaceControl(), 1);
                        needAssignIme = false;
                    }
                } else if (wt.mRoundedCornerOverlay) {
                    wt.assignLayer(t, 1073741826);
                } else {
                    wt.assignLayer(t, j);
                    wt.assignChildLayers(t);
                    DisplayContent.this.handleSideBoxLayerAndVisibility(t, wt, j);
                    int layer = this.mWmService.mPolicy.getWindowLayerFromTypeLw(wt.windowType, wt.mOwnerCanManageAppTokens);
                    if (needAssignIme && layer >= this.mWmService.mPolicy.getWindowLayerFromTypeLw(2012, true)) {
                        imeContainer.assignRelativeLayer(t, wt.getSurfaceControl(), -1);
                        needAssignIme = false;
                    }
                }
            }
            if (needAssignIme) {
                imeContainer.assignRelativeLayer(t, getSurfaceControl(), Integer.MAX_VALUE);
            }
            DisplayContent.this.checkNeedNotifyFingerWinCovered();
            DisplayContent displayContent = DisplayContent.this;
            displayContent.mObserveToken = null;
            displayContent.mTopAboveAppToken = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSideBoxLayerAndVisibility(SurfaceControl.Transaction transaction, WindowToken aboveAppWin, int layer) {
        WindowState win;
        ScreenSideSurfaceBox screenSideSurfaceBox = this.mSideSurfaceBox;
        if (screenSideSurfaceBox != null && screenSideSurfaceBox.isShowing()) {
            if (aboveAppWin.windowType == 2020) {
                WindowState win2 = (WindowState) aboveAppWin.getTopChild();
                if (win2 != null && "VolumeIndex".equals(win2.getAttrs().getTitle().toString()) && win2.isVisible()) {
                    this.mSideSurfaceBox.setLayer(transaction, this.mAboveAppWindowsContainers.getSurfaceControl(), layer - 1);
                }
            } else if (aboveAppWin.windowType == 2024 && (win = (WindowState) aboveAppWin.getTopChild()) != null && "com.huawei.android.extdisplay".equals(win.getOwningPackage()) && win.getAttrs().getTitle().toString().contains("SideView") && win.mWinAnimator != null && win.mWinAnimator.getShown()) {
                this.mSideSurfaceBox.hideSideBox();
            }
        }
    }

    /* access modifiers changed from: private */
    public class NonAppWindowContainers extends DisplayChildWindowContainer<WindowToken> {
        private final Dimmer mDimmer = new Dimmer(this);
        private final Predicate<WindowState> mGetOrientingWindow = $$Lambda$DisplayContent$NonAppWindowContainers$FI_O7m2qEDfIRZef3D32AxGrcs.INSTANCE;
        private final String mName;
        private final Rect mTmpDimBoundsRect = new Rect();
        private final Comparator<WindowToken> mWindowComparator = new Comparator() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$NonAppWindowContainers$nqCymC3xR9b3qaeohnnJJpSiajc */

            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return DisplayContent.NonAppWindowContainers.this.lambda$new$0$DisplayContent$NonAppWindowContainers((WindowToken) obj, (WindowToken) obj2);
            }
        };

        public /* synthetic */ int lambda$new$0$DisplayContent$NonAppWindowContainers(WindowToken token1, WindowToken token2) {
            return (this.mWmService.mPolicy.getWindowLayerFromTypeLw(token1.windowType, token1.mOwnerCanManageAppTokens) < this.mWmService.mPolicy.getWindowLayerFromTypeLw(token2.windowType, token2.mOwnerCanManageAppTokens) || isAddWallpaperAfterMagicWindow(token1, token2)) ? -1 : 1;
        }

        private boolean isAddWallpaperAfterMagicWindow(WindowToken wt1, WindowToken wt2) {
            if (!HwMwUtils.ENABLED || wt2.getTopChild() == null || wt1.windowType != 2013 || !((WindowState) wt2.getTopChild()).getName().toString().contains("MagicWindow")) {
                return false;
            }
            return true;
        }

        static /* synthetic */ boolean lambda$new$1(WindowState w) {
            int req;
            if (!w.isVisibleLw() || !w.mLegacyPolicyVisibilityAfterAnim || (req = w.mAttrs.screenOrientation) == -1 || req == 3 || req == -2) {
                return false;
            }
            return true;
        }

        NonAppWindowContainers(String name, WindowManagerService service) {
            super(service);
            this.mName = name;
        }

        /* access modifiers changed from: package-private */
        public void addChild(WindowToken token) {
            addChild((NonAppWindowContainers) token, (Comparator<NonAppWindowContainers>) this.mWindowComparator);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public int getOrientation() {
            WindowManagerPolicy policy = this.mWmService.mPolicy;
            WindowState win = getWindow(this.mGetOrientingWindow);
            if (win != null) {
                int req = win.mAttrs.screenOrientation;
                if (policy.isKeyguardHostWindow(win.mAttrs)) {
                    DisplayContent.this.mLastKeyguardForcedOrientation = req;
                    if (this.mWmService.mKeyguardGoingAway) {
                        DisplayContent.this.mLastWindowForcedOrientation = -1;
                        return -2;
                    }
                }
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(DisplayContent.TAG, win + " forcing orientation to " + req + " for display id=" + DisplayContent.this.mDisplayId);
                }
                return DisplayContent.this.mLastWindowForcedOrientation = req;
            }
            DisplayContent.this.mLastWindowForcedOrientation = -1;
            boolean isUnoccluding = DisplayContent.this.mAppTransition.getAppTransition() == 23 && DisplayContent.this.mUnknownAppVisibilityController.allResolved();
            if (policy.isKeyguardShowingAndNotOccluded() || isUnoccluding) {
                return DisplayContent.this.mLastKeyguardForcedOrientation;
            }
            return -2;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.ConfigurationContainer
        public String getName() {
            return this.mName;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public Dimmer getDimmer() {
            return this.mDimmer;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public void prepareSurfaces() {
            this.mDimmer.resetDimStates();
            super.prepareSurfaces();
            getBounds(this.mTmpDimBoundsRect);
            if (this.mDimmer.updateDims(getPendingTransaction(), this.mTmpDimBoundsRect)) {
                scheduleAnimation();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeSurface(SurfaceSession s) {
        return this.mWmService.makeSurfaceBuilder(s).setParent(this.mWindowingLayer);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public SurfaceSession getSession() {
        return this.mSession;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public SurfaceControl.Builder makeChildSurface(WindowContainer child) {
        SurfaceControl.Builder b = this.mWmService.makeSurfaceBuilder(child != null ? child.getSession() : getSession()).setContainerLayer();
        if (child == null) {
            return b;
        }
        return b.setName(child.getName()).setParent(this.mWindowingLayer);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeOverlay() {
        return this.mWmService.makeSurfaceBuilder(this.mSession).setParent(this.mOverlayLayer);
    }

    /* access modifiers changed from: package-private */
    public void reparentToOverlay(SurfaceControl.Transaction transaction, SurfaceControl surface) {
        transaction.reparent(surface, this.mOverlayLayer);
    }

    /* access modifiers changed from: package-private */
    public void reparentToHwOverlay(SurfaceControl.Transaction transaction, SurfaceControl surface) {
        transaction.reparent(surface, this.mHwSingleHandOverlayLayer);
    }

    /* access modifiers changed from: package-private */
    public void applyMagnificationSpec(MagnificationSpec spec) {
        if (((double) spec.scale) != 1.0d) {
            this.mMagnificationSpec = spec;
        } else {
            this.mMagnificationSpec = null;
        }
        updateImeParent();
        applyMagnificationSpec(getPendingTransaction(), spec);
        getPendingTransaction().apply();
    }

    /* access modifiers changed from: package-private */
    public void reapplyMagnificationSpec() {
        if (this.mMagnificationSpec != null) {
            applyMagnificationSpec(getPendingTransaction(), this.mMagnificationSpec);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onParentChanged() {
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void assignChildLayers(SurfaceControl.Transaction t) {
        this.mBelowAppWindowsContainers.assignLayer(t, 0);
        this.mTaskStackContainers.assignLayer(t, 1);
        this.mAboveAppWindowsContainers.assignLayer(t, 2);
        WindowState imeTarget = this.mInputMethodTarget;
        boolean needAssignIme = true;
        if (imeTarget != null && !imeTarget.inSplitScreenWindowingMode() && !imeTarget.inHwMagicWindowingMode() && !imeTarget.inHwSplitScreenWindowingMode() && !imeTarget.mToken.isAppAnimating() && imeTarget.getSurfaceControl() != null) {
            this.mImeWindowsContainers.assignRelativeLayer(t, imeTarget.getSurfaceControl(), 1);
            needAssignIme = false;
        }
        TaskStack topStack = (TaskStack) this.mTaskStackContainers.getTopChild();
        if (!(imeTarget == null || imeTarget.mAppToken == null || topStack == null || !topStack.inHwFreeFormWindowingMode() || topStack.getSurfaceControl() == null)) {
            this.mImeWindowsContainers.assignRelativeLayer(t, topStack.getSurfaceControl(), 1);
            needAssignIme = false;
        }
        this.mBelowAppWindowsContainers.assignChildLayers(t);
        this.mTaskStackContainers.assignChildLayers(t);
        this.mAboveAppWindowsContainers.assignChildLayers(t, needAssignIme ? this.mImeWindowsContainers : null);
        this.mImeWindowsContainers.assignChildLayers(t);
    }

    /* access modifiers changed from: package-private */
    public void assignRelativeLayerForImeTargetChild(SurfaceControl.Transaction t, WindowContainer child) {
        child.assignRelativeLayer(t, this.mImeWindowsContainers.getSurfaceControl(), 1);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void prepareSurfaces() {
        Trace.traceBegin(32, "prepareSurfaces");
        try {
            ScreenRotationAnimation screenRotationAnimation = this.mWmService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
            SurfaceControl.Transaction transaction = getPendingTransaction();
            if (this.mIsFisrtLazyMode && this.mSingleHandContentEx != null) {
                this.mSingleHandContentEx.handleSingleHandMode(transaction, this.mWindowingLayer, this.mHwSingleHandOverlayLayer);
                this.mIsFisrtLazyMode = false;
            }
            if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
                screenRotationAnimation.getEnterTransformation().getMatrix().getValues(this.mTmpFloats);
                if (this.mWmService.mHwWMSEx.isNeedLandAni() && this.mDisplayId == 0) {
                    resetMatrixValues(this.mTmpFloats);
                }
                float tmpScale = this.mWmService.getLazyMode() != 0 ? 0.75f : 1.0f;
                transaction.setMatrix(this.mWindowingLayer, this.mTmpFloats[0] * tmpScale, this.mTmpFloats[3], this.mTmpFloats[1], this.mTmpFloats[4] * tmpScale);
                if (this.mWmService.getLazyMode() != 0) {
                    Point origin = this.mWmService.getOriginPointForLazyMode(0.75f, this.mWmService.getLazyMode());
                    transaction.setPosition(this.mWindowingLayer, (float) origin.x, (float) origin.y);
                } else {
                    transaction.setPosition(this.mWindowingLayer, this.mTmpFloats[2], this.mTmpFloats[5]);
                }
                transaction.setAlpha(this.mWindowingLayer, screenRotationAnimation.getEnterTransformation().getAlpha());
                if (this.mWmService.mHwWMSEx.isNeedLandAni() && this.mDisplayId == 0) {
                    transaction.setAlpha(this.mWindowingLayer, 1.0f);
                    this.mWmService.mHwWMSEx.applyLandOpenAnimation();
                }
            }
            for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
                TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
                stack.updateSurfacePosition();
                stack.updateSurfaceSize(stack.getPendingTransaction());
            }
            super.prepareSurfaces();
            SurfaceControl.mergeToGlobalTransaction(transaction);
        } finally {
            Trace.traceEnd(32);
        }
    }

    /* access modifiers changed from: package-private */
    public void assignStackOrdering() {
        this.mTaskStackContainers.assignStackOrdering(getPendingTransaction());
    }

    /* access modifiers changed from: package-private */
    public void assignStackOrdering(SurfaceControl.Transaction t) {
        this.mTaskStackContainers.assignStackOrdering(t);
    }

    /* access modifiers changed from: package-private */
    public void deferUpdateImeTarget() {
        this.mDeferUpdateImeTargetCount++;
    }

    /* access modifiers changed from: package-private */
    public void continueUpdateImeTarget() {
        int i = this.mDeferUpdateImeTargetCount;
        if (i != 0) {
            this.mDeferUpdateImeTargetCount = i - 1;
            if (this.mDeferUpdateImeTargetCount == 0) {
                computeImeTarget(true);
            }
        }
    }

    private boolean canUpdateImeTarget() {
        return this.mDeferUpdateImeTargetCount == 0;
    }

    /* access modifiers changed from: package-private */
    public InputMonitor getInputMonitor() {
        return this.mInputMonitor;
    }

    /* access modifiers changed from: package-private */
    public boolean getLastHasContent() {
        return this.mLastHasContent;
    }

    /* access modifiers changed from: package-private */
    public void registerPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        this.mPointerEventDispatcher.registerInputEventListener(listener);
    }

    /* access modifiers changed from: package-private */
    public void unregisterPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        this.mPointerEventDispatcher.unregisterInputEventListener(listener);
    }

    /* access modifiers changed from: package-private */
    public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) {
        prepareAppTransition(transit, alwaysKeepCurrent, 0, false);
    }

    /* access modifiers changed from: package-private */
    public void prepareAppTransition(int transit, boolean alwaysKeepCurrent, int flags, boolean forceOverride) {
        if (this.mAppTransition.prepareAppTransitionLocked(transit, alwaysKeepCurrent, flags, forceOverride) && okToAnimate()) {
            this.mSkipAppTransitionAnimation = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void executeAppTransition() {
        if (this.mAppTransition.isTransitionSet()) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.w(TAG, "Execute app transition: " + this.mAppTransition + ", displayId: " + this.mDisplayId + " Callers=" + Debug.getCallers(5));
            }
            this.mAppTransition.setReady();
            this.mWmService.mWindowPlacerLocked.requestTraversal();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleAnimatingStoppedAndTransition() {
        this.mAppTransition.setIdle();
        for (int i = this.mNoAnimationNotifyOnTransitionFinished.size() - 1; i >= 0; i--) {
            this.mAppTransition.notifyAppTransitionFinishedLocked(this.mNoAnimationNotifyOnTransitionFinished.get(i));
        }
        this.mNoAnimationNotifyOnTransitionFinished.clear();
        this.mWallpaperController.hideDeferredWallpapersIfNeeded();
        onAppTransitionDone();
        int changes = 0 | 1;
        if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
            Slog.v(TAG, "Wallpaper layer changed: assigning layers + relayout");
        }
        computeImeTarget(true);
        this.mWallpaperMayChange = true;
        this.mWmService.mFocusMayChange = true;
        this.pendingLayoutChanges |= changes;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextTransitionForward() {
        int transit = this.mAppTransition.getAppTransition();
        return transit == 6 || transit == 8 || transit == 10;
    }

    /* access modifiers changed from: package-private */
    public boolean supportsSystemDecorations() {
        return (this.mWmService.mDisplayWindowSettings.shouldShowSystemDecorsLocked(this) || (this.mDisplay.getFlags() & 64) != 0 || (this.mWmService.mForceDesktopModeOnExternalDisplays && !isUntrustedVirtualDisplay())) && this.mDisplayId != this.mWmService.mVr2dDisplayId;
    }

    /* access modifiers changed from: package-private */
    public boolean isUntrustedVirtualDisplay() {
        return this.mDisplay.getType() == 5 && this.mDisplay.getOwnerUid() != 1000;
    }

    /* access modifiers changed from: package-private */
    public void reparentDisplayContent(WindowState win, SurfaceControl sc) {
        this.mParentWindow = win;
        this.mParentSurfaceControl = sc;
        if (this.mPortalWindowHandle == null) {
            this.mPortalWindowHandle = createPortalWindowHandle(sc.toString());
        }
        getPendingTransaction().setInputWindowInfo(sc, this.mPortalWindowHandle).reparent(this.mWindowingLayer, sc).reparent(this.mOverlayLayer, sc);
    }

    /* access modifiers changed from: package-private */
    public WindowState getParentWindow() {
        return this.mParentWindow;
    }

    /* access modifiers changed from: package-private */
    public void updateLocation(WindowState win, int x, int y) {
        if (this.mParentWindow != win) {
            throw new IllegalArgumentException("The given window is not the parent window of this display.");
        } else if (this.mLocationInParentWindow.x != x || this.mLocationInParentWindow.y != y) {
            Point point = this.mLocationInParentWindow;
            point.x = x;
            point.y = y;
            if (this.mWmService.mAccessibilityController != null) {
                this.mWmService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Point getLocationInParentWindow() {
        return this.mLocationInParentWindow;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public SurfaceControl getWindowingLayer() {
        return this.mWindowingLayer;
    }

    /* access modifiers changed from: package-private */
    public boolean updateSystemGestureExclusion() {
        if (this.mSystemGestureExclusionListeners.getRegisteredCallbackCount() == 0) {
            return false;
        }
        Region systemGestureExclusion = calculateSystemGestureExclusion();
        try {
            if (this.mSystemGestureExclusion.equals(systemGestureExclusion)) {
                return false;
            }
            this.mSystemGestureExclusion.set(systemGestureExclusion);
            for (int i = this.mSystemGestureExclusionListeners.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    this.mSystemGestureExclusionListeners.getBroadcastItem(i).onSystemGestureExclusionChanged(this.mDisplayId, systemGestureExclusion);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to notify SystemGestureExclusionListener", e);
                }
            }
            this.mSystemGestureExclusionListeners.finishBroadcast();
            systemGestureExclusion.recycle();
            return true;
        } finally {
            systemGestureExclusion.recycle();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Region calculateSystemGestureExclusion() {
        Throwable th;
        Region unhandled = Region.obtain();
        unhandled.set(0, 0, this.mDisplayFrames.mDisplayWidth, this.mDisplayFrames.mDisplayHeight);
        Rect leftEdge = this.mInsetsStateController.getSourceProvider(6).getSource().getFrame();
        Rect rightEdge = this.mInsetsStateController.getSourceProvider(7).getSource().getFrame();
        Region global = Region.obtain();
        Region touchableRegion = Region.obtain();
        Region local = Region.obtain();
        int i = this.mSystemGestureExclusionLimit;
        int[] remainingLeftRight = {i, i};
        synchronized (this.mWmService.getGlobalLock()) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                try {
                    forAllWindows((Consumer<WindowState>) new Consumer(unhandled, touchableRegion, local, remainingLeftRight, global, leftEdge, rightEdge) {
                        /* class com.android.server.wm.$$Lambda$DisplayContent$gsQrhBQL3vGbqvwErNuLHyt9FU4 */
                        private final /* synthetic */ Region f$1;
                        private final /* synthetic */ Region f$2;
                        private final /* synthetic */ Region f$3;
                        private final /* synthetic */ int[] f$4;
                        private final /* synthetic */ Region f$5;
                        private final /* synthetic */ Rect f$6;
                        private final /* synthetic */ Rect f$7;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                            this.f$5 = r6;
                            this.f$6 = r7;
                            this.f$7 = r8;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            DisplayContent.this.lambda$calculateSystemGestureExclusion$26$DisplayContent(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, (WindowState) obj);
                        }
                    }, true);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    local.recycle();
                    touchableRegion.recycle();
                    unhandled.recycle();
                    return global;
                } catch (Throwable th2) {
                    th = th2;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    public /* synthetic */ void lambda$calculateSystemGestureExclusion$26$DisplayContent(Region unhandled, Region touchableRegion, Region local, int[] remainingLeftRight, Region global, Rect leftEdge, Rect rightEdge, WindowState w) {
        if (!w.cantReceiveTouchInput() && w.isVisible() && (w.getAttrs().flags & 16) == 0 && !unhandled.isEmpty()) {
            w.getEffectiveTouchableRegion(touchableRegion);
            touchableRegion.op(unhandled, Region.Op.INTERSECT);
            if (w.isImplicitlyExcludingAllSystemGestures()) {
                local.set(touchableRegion);
            } else {
                RegionUtils.rectListToRegion(w.getSystemGestureExclusion(), local);
                local.scale(w.mGlobalScale);
                Rect frame = w.getWindowFrames().mFrame;
                local.translate(frame.left, frame.top);
                local.op(touchableRegion, Region.Op.INTERSECT);
            }
            if (needsGestureExclusionRestrictions(w, this.mLastDispatchedSystemUiVisibility)) {
                remainingLeftRight[0] = addToGlobalAndConsumeLimit(local, global, leftEdge, remainingLeftRight[0]);
                remainingLeftRight[1] = addToGlobalAndConsumeLimit(local, global, rightEdge, remainingLeftRight[1]);
                Region middle = Region.obtain(local);
                middle.op(leftEdge, Region.Op.DIFFERENCE);
                middle.op(rightEdge, Region.Op.DIFFERENCE);
                global.op(middle, Region.Op.UNION);
                middle.recycle();
            } else {
                global.op(local, Region.Op.UNION);
            }
            if (!w.isTrustedOverlayGestureExclusion()) {
                unhandled.op(touchableRegion, Region.Op.DIFFERENCE);
            }
        }
    }

    private static boolean needsGestureExclusionRestrictions(WindowState win, int sysUiVisibility) {
        int type = win.mAttrs.type;
        return (((sysUiVisibility & 4098) == 4098) || type == 2011 || type == 2000 || win.getActivityType() == 2) ? false : true;
    }

    private static int addToGlobalAndConsumeLimit(Region local, Region global, Rect edge, int limit) {
        Region r = Region.obtain(local);
        r.op(edge, Region.Op.INTERSECT);
        int[] remaining = {limit};
        RegionUtils.forEachRectReverse(r, new Consumer(remaining, global) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$fhmUsm87EmQG87z3F0uABEJwe8 */
            private final /* synthetic */ int[] f$0;
            private final /* synthetic */ Region f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.lambda$addToGlobalAndConsumeLimit$27(this.f$0, this.f$1, (Rect) obj);
            }
        });
        r.recycle();
        return remaining[0];
    }

    static /* synthetic */ void lambda$addToGlobalAndConsumeLimit$27(int[] remaining, Region global, Rect rect) {
        if (remaining[0] > 0) {
            int height = rect.height();
            if (height > remaining[0]) {
                rect.top = rect.bottom - remaining[0];
            }
            remaining[0] = remaining[0] - height;
            global.op(rect, Region.Op.UNION);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerSystemGestureExclusionListener(ISystemGestureExclusionListener listener) {
        boolean changed;
        this.mSystemGestureExclusionListeners.register(listener);
        if (this.mSystemGestureExclusionListeners.getRegisteredCallbackCount() == 1) {
            changed = updateSystemGestureExclusion();
        } else {
            changed = false;
        }
        if (!changed) {
            try {
                listener.onSystemGestureExclusionChanged(this.mDisplayId, this.mSystemGestureExclusion);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to notify SystemGestureExclusionListener during register", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterSystemGestureExclusionListener(ISystemGestureExclusionListener listener) {
        this.mSystemGestureExclusionListeners.unregister(listener);
    }

    private InputWindowHandle createPortalWindowHandle(String name) {
        InputWindowHandle portalWindowHandle = new InputWindowHandle((InputApplicationHandle) null, (IWindow) null, -1);
        portalWindowHandle.name = name;
        portalWindowHandle.token = new Binder();
        portalWindowHandle.layoutParamsFlags = 8388648;
        getBounds(this.mTmpBounds);
        portalWindowHandle.touchableRegion.set(this.mTmpBounds);
        portalWindowHandle.scaleFactor = 1.0f;
        portalWindowHandle.ownerPid = Process.myPid();
        portalWindowHandle.ownerUid = Process.myUid();
        portalWindowHandle.portalToDisplayId = this.mDisplayId;
        return portalWindowHandle;
    }

    public void setForwardedInsets(Insets insets) {
        if (insets == null) {
            insets = Insets.NONE;
        }
        if (!this.mDisplayPolicy.getForwardedInsets().equals(insets)) {
            this.mDisplayPolicy.setForwardedInsets(insets);
            setLayoutNeeded();
            this.mWmService.mWindowPlacerLocked.requestTraversal();
        }
    }

    /* access modifiers changed from: protected */
    public MetricsLogger getMetricsLogger() {
        if (this.mMetricsLogger == null) {
            this.mMetricsLogger = new MetricsLogger();
        }
        return this.mMetricsLogger;
    }

    public int getBaseDisplayHeight() {
        return this.mBaseDisplayHeight;
    }

    public int getAppTransition() {
        return this.mAppTransition.getAppTransition();
    }

    public boolean isImeVisible() {
        WindowState imeWin = this.mInputMethodWindow;
        return imeWin != null && imeWin.isVisibleLw() && imeWin.isDisplayedLw() && !this.mDividerControllerLocked.isImeHideRequested();
    }

    public TaskStack getCoordinationPrimaryStackIgnoringVisibility() {
        return this.mTaskStackContainers.getCoordinationPrimaryStack();
    }

    public int getTopActivityAdaptNotchState(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return 0;
        }
        synchronized (this.mWmService.getGlobalLock()) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(stackNdx);
                    for (int taskNdx = stack.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
                        WindowState win = ((Task) stack.mChildren.get(taskNdx)).getTopVisibleAppMainWindow();
                        if (win != null) {
                            if (win.getAttrs().type != 3) {
                                if (win.getAttrs().packageName != null && packageName.equals(win.getAttrs().packageName)) {
                                    if (this.mWmService.getService().getPolicy().isWindowNeedLayoutBelowNotch(win)) {
                                        return 1;
                                    }
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    return 2;
                                }
                            }
                        }
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return 0;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isHwMultiStackVisible(int windowingMode) {
        if (!WindowConfiguration.isHwMultiStackWindowingMode(windowingMode)) {
            return false;
        }
        synchronized (this.mWmService.getGlobalLock()) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
                    TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
                    if (stack != null) {
                        if (stack.getWindowingMode() == 1) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return false;
                        } else if (stack.getWindowingMode() == windowingMode) {
                            return stack.isVisible();
                        }
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void resetMatrixValues(float[] matrixValues) {
        if (matrixValues != null) {
            matrixValues[0] = 1.0f;
            matrixValues[1] = 0.0f;
            matrixValues[2] = 0.0f;
            matrixValues[3] = 0.0f;
            matrixValues[4] = 1.0f;
            matrixValues[5] = 0.0f;
            matrixValues[6] = 0.0f;
            matrixValues[7] = 0.0f;
            matrixValues[8] = 1.0f;
        }
    }
}
