package com.android.server.wm;

import android.app.ActivityManager;
import android.app.WindowConfiguration;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.graphics.GraphicBuffer;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import android.view.IApplicationToken;
import android.view.InputApplicationHandle;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationDefinition;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.animation.Animation;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.AttributeCache;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.display.color.ColorDisplayService;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.RemoteAnimationController;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.huawei.server.wm.IHwDisplayPolicyEx;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedThisRef;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/* access modifiers changed from: package-private */
public class AppWindowToken extends WindowToken implements WindowManagerService.AppFreezeListener, ConfigurationContainerListener {
    private static final int APP_ANIMATION_DURATION = 300;
    private static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static final boolean IS_HW_TV_MULTI_TASK_ENABLE = ("tv".equals(SystemProperties.get("ro.build.characteristics", "")) && SystemProperties.getBoolean("hw_mc.tvlauncher.multitask_enable", false));
    private static final boolean IS_PHONE;
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", ""));
    private static final int RES_ID_FLAG_MASK = -16777216;
    private static final int RES_ID_FLAG_SYSTEM = 16777216;
    private static final int STARTING_WINDOW_TYPE_NONE = 0;
    private static final int STARTING_WINDOW_TYPE_SNAPSHOT = 1;
    private static final int STARTING_WINDOW_TYPE_SPLASH_SCREEN = 2;
    private static final String TAG = "WindowManager";
    private static final String WIN_DIALTACTSACTIVITY = "com.android.contacts.activities.DialtactsActivity";
    private static final String WIN_PEOPLEACTIVITY = "com.android.contacts.activities.PeopleActivity";
    @VisibleForTesting
    static final int Z_BOOST_BASE = 800570000;
    private static Set<String> sDisableSnapShots = new HashSet();
    private static Set<String> sSkipStartingWindowActivitys = new HashSet();
    boolean allDrawn;
    String appComponentName;
    String appPackageName;
    int appPid;
    String appProcessName;
    final IApplicationToken appToken;
    boolean deferClearAllDrawn;
    boolean firstWindowDrawn;
    private boolean hasAddedSplitShadow;
    boolean hiddenRequested;
    boolean inPendingTransaction;
    boolean isSystemUiFullScreenWindowShow;
    final ComponentName mActivityComponent;
    ActivityRecord mActivityRecord;
    private final Runnable mAddStartingWindow;
    private boolean mAlwaysFocusable;
    private AnimatingAppWindowTokenRegistry mAnimatingAppWindowTokenRegistry;
    SurfaceControl mAnimationBoundsLayer;
    boolean mAppStopped;
    private BackgroundSurface mBackgroundSurface;
    private boolean mCanTurnScreenOn;
    private boolean mClientHidden;
    private final ColorDisplayService.ColorTransformController mColorTransformController;
    boolean mDeferHidingClient;
    private boolean mDisablePreviewScreenshots;
    boolean mEnteringAnimation;
    private boolean mFillsParent;
    SurfaceControl mFloatWindwoIconSurfaceControl;
    private boolean mFreezingScreen;
    ArrayDeque<Rect> mFrozenBounds;
    ArrayDeque<Configuration> mFrozenMergedConfig;
    boolean mHadTakenSnapShot;
    boolean mHasIconLayer;
    private boolean mHiddenSetFromTransferredStartingWindow;
    boolean mHideActivityInFingerBoost;
    IHwAppWindowTokenEx mHwAppWinTokenEx;
    private int mHwGestureNavOptions;
    private boolean mHwNotchSupport;
    final InputApplicationHandle mInputApplicationHandle;
    long mInputDispatchingTimeoutNanos;
    boolean mIsExiting;
    protected boolean mIsNeedBackgroundSurface;
    private boolean mIsNeedChangeAnimationCorner;
    private boolean mIsTvUsedPreSnapshot;
    private boolean mLastAllDrawn;
    private AppSaturationInfo mLastAppSaturationInfo;
    private boolean mLastContainsDismissKeyguardWindow;
    private boolean mLastContainsShowWhenLockedWindow;
    private Task mLastParent;
    private boolean mLastSurfaceShowing;
    private long mLastTransactionSequence;
    boolean mLaunchTaskBehind;
    private WindowManager.LayoutParams mLayoutParams;
    private Letterbox mLetterbox;
    private BackgroundSurface mMagicWindowSurface;
    boolean mNeedsAnimationBoundsLayer;
    @VisibleForTesting
    boolean mNeedsZBoost;
    private int mNumDrawnWindows;
    private int mNumInterestingWindows;
    int mPendingRelaunchCount;
    private int mPrevWinMode;
    private RemoteAnimationDefinition mRemoteAnimationDefinition;
    private boolean mRemovingFromDisplay;
    private boolean mReparenting;
    private final WindowState.UpdateReportedVisibilityResults mReportedVisibilityResults;
    int mRotationAnimationHint;
    boolean mShouldDrawIcon;
    boolean mShowForAllUsers;
    private Rect mSizeCompatBounds;
    private float mSizeCompatScale;
    StartingData mStartingData;
    int mTargetSdk;
    private AppWindowThumbnail mThumbnail;
    private final Point mTmpPoint;
    private final Rect mTmpPrevBounds;
    private final Rect mTmpRect;
    private int mTransit;
    private SurfaceControl mTransitChangeLeash;
    private int mTransitFlags;
    private final Rect mTransitStartRect;
    private boolean mUseTransferredAnimation;
    final boolean mVoiceInteraction;
    private boolean mWillCloseOrEnterPip;
    boolean navigationBarHide;
    boolean removed;
    private boolean reportedDrawn;
    boolean reportedVisible;
    boolean startingDisplayed;
    boolean startingMoved;
    WindowManagerPolicy.StartingSurface startingSurface;
    WindowState startingWindow;

    static {
        boolean z = true;
        if (!"phone".equals(SystemProperties.get("ro.build.characteristics", "")) && !"default".equals(SystemProperties.get("ro.build.characteristics", ""))) {
            z = false;
        }
        IS_PHONE = z;
        sSkipStartingWindowActivitys.add("com.tencent.mm/.plugin.voip.ui.VideoActivity");
        sSkipStartingWindowActivitys.add("com.tencent.mobileqq/com.tencent.av.ui.VideoInviteFull");
        sSkipStartingWindowActivitys.add("com.tencent.tim/com.tencent.av.ui.VideoInviteFull");
        sSkipStartingWindowActivitys.add("com.tencent.mobileqq/com.tencent.av.ui.VideoInviteFull");
        sSkipStartingWindowActivitys.add("com.tencent.mobileqq/com.tencent.av.ui.VChatActivity");
        sSkipStartingWindowActivitys.add("com.huawei.android.launcher/.powersavemode.PowerSaveModeLauncher");
        sDisableSnapShots.add("com.android.contacts");
        sDisableSnapShots.add("com.huawei.contacts");
        sDisableSnapShots.add("com.huawei.camera");
        sDisableSnapShots.add("com.android.incallui");
        sDisableSnapShots.add("com.huawei.systemmanager");
    }

    public boolean getHwNotchSupport() {
        return this.mHwNotchSupport;
    }

    public /* synthetic */ void lambda$new$1$AppWindowToken(float[] matrix, float[] translation) {
        this.mWmService.mH.post(new Runnable(matrix, translation) {
            /* class com.android.server.wm.$$Lambda$AppWindowToken$fbAn0RqOBB6FcyKBQMtQpZ1Ec */
            private final /* synthetic */ float[] f$1;
            private final /* synthetic */ float[] f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AppWindowToken.this.lambda$new$0$AppWindowToken(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$AppWindowToken(float[] matrix, float[] translation) {
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mLastAppSaturationInfo == null) {
                    this.mLastAppSaturationInfo = new AppSaturationInfo();
                }
                this.mLastAppSaturationInfo.setSaturation(matrix, translation);
                updateColorTransform();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    AppWindowToken(WindowManagerService service, IApplicationToken token, ComponentName activityComponent, boolean voiceInteraction, DisplayContent dc, long inputDispatchingTimeoutNanos, boolean fullscreen, boolean showForAllUsers, int targetSdk, int orientation, int rotationAnimationHint, boolean launchTaskBehind, boolean alwaysFocusable, ActivityRecord activityRecord, boolean naviBarHide, ActivityInfo info) {
        this(service, token, activityComponent, voiceInteraction, dc, fullscreen);
        this.mActivityRecord = activityRecord;
        this.mActivityRecord.registerConfigurationChangeListener(this);
        this.mInputDispatchingTimeoutNanos = inputDispatchingTimeoutNanos;
        this.mShowForAllUsers = showForAllUsers;
        this.mTargetSdk = targetSdk;
        this.mOrientation = orientation;
        this.navigationBarHide = naviBarHide;
        this.mLaunchTaskBehind = launchTaskBehind;
        this.mAlwaysFocusable = alwaysFocusable;
        this.mRotationAnimationHint = rotationAnimationHint;
        setHidden(true);
        this.hiddenRequested = true;
        this.mHwNotchSupport = info.hwNotchSupport;
        this.appPackageName = info.packageName;
        this.appComponentName = info.getComponentName().flattenToShortString();
        this.appProcessName = info.applicationInfo.processName;
        this.mHwGestureNavOptions = info.hwGestureNavOptions;
        this.isSystemUiFullScreenWindowShow = activityRecord.isSystemUiFullScreenWindowShow();
        ((ColorDisplayService.ColorDisplayServiceInternal) LocalServices.getService(ColorDisplayService.ColorDisplayServiceInternal.class)).attachColorTransformController(activityRecord.packageName, activityRecord.mUserId, new WeakReference(this.mColorTransformController));
        this.mHwAppWinTokenEx = HwServiceFactory.getHwAppWindowTokenEx();
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    AppWindowToken(WindowManagerService service, IApplicationToken token, ComponentName activityComponent, boolean voiceInteraction, DisplayContent dc, boolean fillsParent) {
        super(service, token != null ? token.asBinder() : null, 2, true, dc, false);
        this.mHadTakenSnapShot = false;
        this.mRemovingFromDisplay = false;
        this.mLastTransactionSequence = Long.MIN_VALUE;
        this.mPrevWinMode = 0;
        this.mReportedVisibilityResults = new WindowState.UpdateReportedVisibilityResults();
        this.mFrozenBounds = new ArrayDeque<>();
        this.mFrozenMergedConfig = new ArrayDeque<>();
        this.isSystemUiFullScreenWindowShow = false;
        this.mSizeCompatScale = 1.0f;
        this.mCanTurnScreenOn = true;
        this.mLastSurfaceShowing = true;
        this.mTransitStartRect = new Rect();
        this.mTransitChangeLeash = null;
        this.mTmpPoint = new Point();
        this.mTmpRect = new Rect();
        this.mTmpPrevBounds = new Rect();
        this.mHasIconLayer = false;
        this.mShouldDrawIcon = false;
        this.mIsNeedChangeAnimationCorner = false;
        this.hasAddedSplitShadow = false;
        this.mColorTransformController = new ColorDisplayService.ColorTransformController() {
            /* class com.android.server.wm.$$Lambda$AppWindowToken$cwsF3cyeJjO4UiuaM07w8TBc698 */

            public final void applyAppSaturation(float[] fArr, float[] fArr2) {
                AppWindowToken.this.lambda$new$1$AppWindowToken(fArr, fArr2);
            }
        };
        this.mAddStartingWindow = new Runnable() {
            /* class com.android.server.wm.AppWindowToken.AnonymousClass2 */

            /* JADX WARNING: Removed duplicated region for block: B:34:0x00d0 A[Catch:{ all -> 0x0108 }] */
            @Override // java.lang.Runnable
            public void run() {
                StartingData startingData;
                synchronized (AppWindowToken.this.mWmService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        AppWindowToken.this.mWmService.mAnimationHandler.removeCallbacks(this);
                        if (AppWindowToken.this.mStartingData == null) {
                            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                Slog.v(AppWindowToken.TAG, "startingData was nulled out before handling mAddStartingWindow: " + AppWindowToken.this);
                            }
                            return;
                        }
                        startingData = AppWindowToken.this.mStartingData;
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                Flog.i(301, "Prepare StartingSurface for " + this + ": startingData=" + startingData);
                WindowManagerPolicy.StartingSurface surface = null;
                try {
                    surface = startingData.createStartingSurface(AppWindowToken.this);
                } catch (Exception e) {
                    Slog.w(AppWindowToken.TAG, "Exception when adding starting window", e);
                }
                if (surface != null) {
                    boolean abort = false;
                    synchronized (AppWindowToken.this.mWmService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (!AppWindowToken.this.removed) {
                                if (AppWindowToken.this.mStartingData != null) {
                                    AppWindowToken.this.startingSurface = surface;
                                    if (!abort) {
                                        Flog.i(301, "Finish adding starting window for " + AppWindowToken.this + ": startingWindow=" + AppWindowToken.this.startingWindow + " startingView=" + AppWindowToken.this.startingSurface);
                                    }
                                }
                            }
                            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                                Slog.v(AppWindowToken.TAG, "Aborted starting " + AppWindowToken.this + ": removed=" + AppWindowToken.this.removed + " startingData=" + AppWindowToken.this.mStartingData);
                            }
                            AppWindowToken.this.startingWindow = null;
                            AppWindowToken.this.mStartingData = null;
                            abort = true;
                            if (!abort) {
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (abort) {
                        surface.remove();
                        return;
                    }
                    return;
                }
                Flog.i(301, "StartingSurface was null: " + AppWindowToken.this);
            }
        };
        this.appToken = token;
        this.mActivityComponent = activityComponent;
        this.mVoiceInteraction = voiceInteraction;
        this.mFillsParent = fillsParent;
        this.mInputApplicationHandle = new InputApplicationHandle(this.appToken.asBinder());
    }

    /* access modifiers changed from: package-private */
    public void onFirstWindowDrawn(WindowState win, WindowStateAnimator winAnimator) {
        this.firstWindowDrawn = true;
        removeDeadWindows();
        if (this.startingWindow != null) {
            Flog.i(301, "Remove startingwindow for " + win.mToken + " as first real window is shown, no animation");
            win.cancelAnimation();
        }
        removeStartingWindow();
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
            for (int i = 0; i < count; i++) {
                ((WindowState) this.mChildren.get(i)).updateReportedVisibility(this.mReportedVisibilityResults);
            }
            int numInteresting = this.mReportedVisibilityResults.numInteresting;
            int numVisible = this.mReportedVisibilityResults.numVisible;
            int numDrawn = this.mReportedVisibilityResults.numDrawn;
            boolean nowGone = this.mReportedVisibilityResults.nowGone;
            boolean nowVisible = false;
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
            if (nowDrawn != this.reportedDrawn) {
                ActivityRecord activityRecord = this.mActivityRecord;
                if (activityRecord != null) {
                    activityRecord.onWindowsDrawn(nowDrawn, SystemClock.uptimeMillis());
                }
                this.reportedDrawn = nowDrawn;
            }
            if (nowVisible != this.reportedVisible) {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG, "Visibility changed in " + this + ": vis=" + nowVisible);
                }
                this.reportedVisible = nowVisible;
                if (this.mActivityRecord == null) {
                    return;
                }
                if (nowVisible) {
                    onWindowsVisible();
                    return;
                }
                if (toString().contains("com.android.incallui/.InCallActivity")) {
                    Slog.i(TAG, "InCallActivity windowsGone, numInteresting=" + numInteresting + " numVisible=" + numVisible + " numDrawn=" + numDrawn + " nowGone=" + nowGone + " callers=" + Debug.getCallers(4));
                }
                onWindowsGone();
            }
        }
    }

    private void onWindowsGone() {
        if (this.mActivityRecord != null) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "Reporting gone in " + this.mActivityRecord.appToken);
            }
            this.mActivityRecord.onWindowsGone();
        }
    }

    private void onWindowsVisible() {
        if (this.mActivityRecord != null) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "Reporting visible in " + this.mActivityRecord.appToken);
            }
            this.mActivityRecord.onWindowsVisible();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isClientHidden() {
        return this.mClientHidden;
    }

    /* access modifiers changed from: package-private */
    public void setClientHidden(boolean hideClient) {
        if (this.mClientHidden == hideClient) {
            return;
        }
        if (!hideClient || !this.mDeferHidingClient) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "setClientHidden: " + this + " clientHidden=" + hideClient + " Callers=" + Debug.getCallers(5));
            }
            this.mClientHidden = hideClient;
            sendAppVisibilityToClients();
        }
    }

    private void pendingShowIfNeed(boolean visible) {
        ActivityRecord activityRecord;
        ActivityStack stack;
        if (visible && (activityRecord = this.mActivityRecord) != null && (stack = activityRecord.getActivityStack()) != null) {
            this.mWmService.mAtmService.mHwATMSEx.setTaskStackHide(stack, true);
            this.mWmService.mAtmService.mHwATMSEx.updateHwFreeformNotificationState(stack.mDisplayId, "hide");
        }
    }

    /* access modifiers changed from: package-private */
    public void setVisibility(boolean visible, boolean deferHidingClient) {
        WindowState win;
        AppWindowToken focusedToken;
        AppTransition appTransition = getDisplayContent().mAppTransition;
        if (visible || !this.hiddenRequested) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "setAppVisibility(" + this.appToken + ", visible=" + visible + "): " + appTransition + " hidden=" + isHidden() + " hiddenRequested=" + this.hiddenRequested + " Callers=" + Debug.getCallers(6));
            }
            DisplayContent displayContent = getDisplayContent();
            displayContent.mOpeningApps.remove(this);
            displayContent.mClosingApps.remove(this);
            if (isInChangeTransition()) {
                if (appTransition.getAppTransition() != 51 || getStack().isTvSplitExitTop()) {
                    clearChangeLeash(getPendingTransaction(), true);
                } else {
                    Slog.v(TAG, "no clearChangeLeash " + appTransition.getAppTransition() + ", flag:" + getStack().isTvSplitExitTop());
                }
            }
            displayContent.mChangingApps.remove(this);
            this.waitingToShow = false;
            this.hiddenRequested = !visible;
            this.mDeferHidingClient = deferHidingClient;
            pendingShowIfNeed(visible);
            if (!visible) {
                removeDeadWindows();
            } else {
                if (!appTransition.isTransitionSet() && appTransition.isReady()) {
                    displayContent.mOpeningApps.add(this);
                }
                this.startingMoved = false;
                if (isHidden() || this.mAppStopped) {
                    clearAllDrawn();
                    if (isHidden()) {
                        this.waitingToShow = true;
                        forAllWindows((Consumer<WindowState>) new Consumer() {
                            /* class com.android.server.wm.$$Lambda$AppWindowToken$l3nhOHgNM6mJqZvaw_yRzIRixU */

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                AppWindowToken.lambda$setVisibility$2(DisplayContent.this, (WindowState) obj);
                            }
                        }, true);
                    }
                }
                setClientHidden(false);
                requestUpdateWallpaperIfNeeded();
                this.mAppStopped = false;
                transferStartingWindowFromHiddenAboveTokenIfNeeded();
            }
            if (!okToAnimate() || !appTransition.isTransitionSet()) {
                commitVisibility(null, visible, -1, true, this.mVoiceInteraction);
                updateReportedVisibilityLocked();
                return;
            }
            this.inPendingTransaction = true;
            if (visible) {
                displayContent.mOpeningApps.add(this);
                this.mEnteringAnimation = true;
            } else {
                displayContent.mClosingApps.add(this);
                this.mEnteringAnimation = false;
            }
            if (!(appTransition.getAppTransition() != 16 || (win = getDisplayContent().findFocusedWindow()) == null || (focusedToken = win.mAppToken) == null)) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.d(TAG, "TRANSIT_TASK_OPEN_BEHIND,  adding " + focusedToken + " to mOpeningApps");
                }
                focusedToken.setHidden(true);
                displayContent.mOpeningApps.add(focusedToken);
            }
            reportDescendantOrientationChangeIfNeeded();
        } else if (!deferHidingClient && this.mDeferHidingClient) {
            this.mDeferHidingClient = deferHidingClient;
            setClientHidden(true);
        }
    }

    static /* synthetic */ void lambda$setVisibility$2(DisplayContent displayContent, WindowState w) {
        if (w.mWinAnimator.mDrawState == 4) {
            w.mWinAnimator.resetDrawState();
            displayContent.setLayoutNeeded();
            w.resetLastContentInsets();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean commitVisibility(WindowManager.LayoutParams lp, boolean visible, int transit, boolean performLayout, boolean isVoiceInteraction) {
        boolean z;
        boolean delayed = false;
        this.inPendingTransaction = false;
        this.mHiddenSetFromTransferredStartingWindow = false;
        boolean visibilityChanged = false;
        if (isHidden() == visible || ((isHidden() && this.mIsExiting) || ((visible && waitingForReplacement()) || isAllowAnimForLauncherInFingerBoost(isHidden(), visible)))) {
            AccessibilityController accessibilityController = this.mWmService.mAccessibilityController;
            boolean changed = false;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Changing app " + this + " hidden=" + isHidden() + " performLayout=" + performLayout);
            }
            boolean runningAppAnimation = false;
            if (transit != -1) {
                if (this.mUseTransferredAnimation) {
                    runningAppAnimation = isReallyAnimating();
                } else if (applyAnimationLocked(lp, transit, visible, isVoiceInteraction)) {
                    runningAppAnimation = true;
                }
                delayed = runningAppAnimation;
                WindowState window = findMainWindow();
                if (!(window == null || accessibilityController == null)) {
                    accessibilityController.onAppWindowTransitionLocked(window, transit);
                }
                changed = true;
            }
            int windowsCount = this.mChildren.size();
            for (int i = 0; i < windowsCount; i++) {
                changed |= ((WindowState) this.mChildren.get(i)).onAppVisibilityChanged(visible, runningAppAnimation);
            }
            setHidden(!visible);
            this.hiddenRequested = !visible;
            visibilityChanged = true;
            if (!visible) {
                stopFreezingScreen(true, true);
            } else {
                WindowState windowState = this.startingWindow;
                if (windowState != null && !windowState.isDrawnLw() && (!this.mWmService.mHwWMSEx.isNeedLandAni() || this.mWmService.mAnimator.getScreenRotationAnimationLocked(0) == null)) {
                    this.startingWindow.clearPolicyVisibilityFlag(1);
                    this.startingWindow.mLegacyPolicyVisibilityAfterAnim = false;
                }
                WindowManagerService windowManagerService = this.mWmService;
                Objects.requireNonNull(windowManagerService);
                forAllWindows((Consumer<WindowState>) new Consumer() {
                    /* class com.android.server.wm.$$Lambda$2KrtdmjrY7Nagc4IRqzCk9gDuQU */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        WindowManagerService.this.makeWindowFreezingScreenIfNeededLocked((WindowState) obj);
                    }
                }, true);
            }
            Slog.v(TAG, "commitVisibility: " + this + ": hidden=" + isHidden() + " hiddenRequested=" + this.hiddenRequested);
            if (changed) {
                getDisplayContent().getInputMonitor().setUpdateInputWindowsNeededLw();
                if (performLayout) {
                    z = false;
                    this.mWmService.updateFocusedWindowLocked(3, false);
                    this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
                } else {
                    z = false;
                }
                getDisplayContent().getInputMonitor().updateInputWindowsLw(z);
            } else {
                z = false;
            }
        } else {
            z = false;
        }
        this.mUseTransferredAnimation = z;
        if (isReallyAnimating()) {
            delayed = true;
        } else {
            onAnimationFinished();
        }
        for (int i2 = this.mChildren.size() - 1; i2 >= 0 && !delayed; i2--) {
            if (((WindowState) this.mChildren.get(i2)).isSelfOrChildAnimating()) {
                delayed = true;
            }
        }
        if (visibilityChanged) {
            if (visible && !delayed) {
                this.mEnteringAnimation = true;
                this.mWmService.mActivityManagerAppTransitionNotifier.onAppTransitionFinishedLocked(this.token);
            }
            if (visible || !isReallyAnimating()) {
                setClientHidden(!visible);
            }
            if (!getDisplayContent().mClosingApps.contains(this) && !getDisplayContent().mOpeningApps.contains(this)) {
                getDisplayContent().getDockedDividerController().notifyAppVisibilityChanged();
                this.mWmService.mTaskSnapshotController.notifyAppVisibilityChanged(this, visible);
            }
            if (isHidden() && !delayed && !getDisplayContent().mAppTransition.isTransitionSet()) {
                SurfaceControl.openTransaction();
                for (int i3 = this.mChildren.size() - 1; i3 >= 0; i3--) {
                    ((WindowState) this.mChildren.get(i3)).mWinAnimator.hide("immediately hidden");
                }
                SurfaceControl.closeTransaction();
            }
            reportDescendantOrientationChangeIfNeeded();
        }
        return delayed;
    }

    private void reportDescendantOrientationChangeIfNeeded() {
        if (this.mActivityRecord.getRequestedConfigurationOrientation() != getConfiguration().orientation && getOrientationIgnoreVisibility() != -2) {
            ActivityRecord activityRecord = this.mActivityRecord;
            onDescendantOrientationChanged(activityRecord.mayFreezeScreenLocked(activityRecord.app) ? this.mActivityRecord.appToken : null, this.mActivityRecord);
        }
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
    public WindowState getTopVisibleWindow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) this.mChildren.get(i);
            if (win != null && win.isVisible()) {
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
        int type;
        WindowState candidate = null;
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            WindowState win = (WindowState) this.mChildren.get(j);
            if ((!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || win != null) && ((type = win.mAttrs.type) == 1 || (includeStartingApp && type == 3))) {
                if (!win.mAnimatingExit) {
                    return win;
                }
                candidate = win;
            }
        }
        return candidate;
    }

    /* access modifiers changed from: package-private */
    public boolean windowsAreFocusable() {
        if (this.mTargetSdk < 29) {
            ActivityRecord activityRecord = this.mActivityRecord;
            AppWindowToken topFocusedAppOfMyProcess = this.mWmService.mRoot.mTopFocusedAppByProcess.get(Integer.valueOf((activityRecord == null || activityRecord.app == null) ? 0 : this.mActivityRecord.app.getPid()));
            if (!(topFocusedAppOfMyProcess == null || topFocusedAppOfMyProcess == this || (getStack() != null && getStack().canResumeWithFocusByCompat(topFocusedAppOfMyProcess.getStack())))) {
                return false;
            }
        }
        if (getWindowConfiguration().canReceiveKeys() || this.mAlwaysFocusable) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean isVisible() {
        return !isHidden();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowToken, com.android.server.wm.WindowContainer
    public void removeImmediately() {
        onRemovedFromDisplay();
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.unregisterConfigurationChangeListener(this);
        }
        super.removeImmediately();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeIfPossible() {
        this.mIsExiting = false;
        removeAllWindowsIfPossible();
        removeImmediately();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
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
            boolean delayed = commitVisibility(null, false, -1, true, this.mVoiceInteraction);
            getDisplayContent().mOpeningApps.remove(this);
            getDisplayContent().mChangingApps.remove(this);
            getDisplayContent().mUnknownAppVisibilityController.appRemovedOrHidden(this);
            this.mWmService.mTaskSnapshotController.onAppRemoved(this);
            this.waitingToShow = false;
            if (getDisplayContent().mClosingApps.contains(this)) {
                delayed = true;
            } else if (getDisplayContent().mAppTransition.isTransitionSet() && !inHwMagicWindowingMode()) {
                getDisplayContent().mClosingApps.add(this);
                delayed = true;
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Removing app " + this + " delayed=" + delayed + " animation=" + getAnimation() + " animating=" + isSelfAnimating());
            }
            Slog.v(TAG, "removeAppToken: " + this + " delayed=" + delayed + " Callers=" + Debug.getCallers(9));
            if (this.mStartingData != null) {
                removeStartingWindow();
            }
            if (isSelfAnimating()) {
                getDisplayContent().mNoAnimationNotifyOnTransitionFinished.add(this.token);
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
            DisplayContent dc = getDisplayContent();
            if (dc.mFocusedApp == this) {
                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                    Slog.v(TAG, "Removing focused app token:" + this + " displayId=" + dc.getDisplayId());
                }
                dc.setFocusedApp(null);
                this.mWmService.updateFocusedWindowLocked(0, true);
            }
            Letterbox letterbox = this.mLetterbox;
            if (letterbox != null) {
                letterbox.destroy();
                this.mLetterbox = null;
            }
            BackgroundSurface backgroundSurface = this.mBackgroundSurface;
            if (backgroundSurface != null) {
                backgroundSurface.destroyBackgroundSurfaceLocked(getPendingTransaction());
                this.mBackgroundSurface = null;
            }
            if (this.mMagicWindowSurface != null) {
                destroyMagicWindowSurface();
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
        this.mHadTakenSnapShot = false;
        setCanTurnScreenOn(true);
        if (!wasStopped) {
            destroySurfaces(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyAppStopped() {
        Slog.i(TAG, "notifyAppStopped: " + this);
        this.mAppStopped = true;
        this.mHadTakenSnapShot = false;
        destroySurfaces();
        removeStartingWindow();
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
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onParentChanged() {
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry;
        super.onParentChanged();
        Task task = getTask();
        if (!this.mReparenting) {
            if (task == null) {
                getDisplayContent().mClosingApps.remove(this);
            } else {
                Task task2 = this.mLastParent;
                if (!(task2 == null || task2.mStack == null)) {
                    task.mStack.mExitingAppTokens.remove(this);
                }
            }
        }
        TaskStack stack = getStack();
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry2 = this.mAnimatingAppWindowTokenRegistry;
        if (animatingAppWindowTokenRegistry2 != null) {
            animatingAppWindowTokenRegistry2.notifyFinished(this);
        }
        if (stack != null) {
            animatingAppWindowTokenRegistry = stack.getAnimatingAppWindowTokenRegistry();
        } else {
            animatingAppWindowTokenRegistry = null;
        }
        this.mAnimatingAppWindowTokenRegistry = animatingAppWindowTokenRegistry;
        this.mLastParent = task;
        updateColorTransform();
    }

    /* access modifiers changed from: package-private */
    public void postWindowRemoveStartingWindowCleanup(WindowState win) {
        if (this.startingWindow == win) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Notify removed startingWindow " + win);
            }
            removeStartingWindow();
        } else if (this.mChildren.size() == 0) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Nulling last startingData");
            }
            this.mStartingData = null;
            if (this.mHiddenSetFromTransferredStartingWindow) {
                setHidden(true);
            }
        } else if (this.mChildren.size() == 1 && this.startingSurface != null && !isRelaunching()) {
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Last window, removing starting window " + win);
            }
            removeStartingWindow();
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
        int i = this.mPendingRelaunchCount;
        if (i > 0) {
            this.mPendingRelaunchCount = i - 1;
            if (this.mFreezingScreen && isInChangeTransition() && !isRelaunching()) {
                Slog.i(TAG, "Finish relaunching and force clearing freeze: " + this);
                stopFreezingScreen(false, true);
                return;
            }
            return;
        }
        checkKeyguardFlagsChanged();
    }

    /* access modifiers changed from: package-private */
    public void clearRelaunching() {
        if (this.mPendingRelaunchCount != 0) {
            unfreezeBounds();
            this.mPendingRelaunchCount = 0;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowToken
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
    @Override // com.android.server.wm.WindowToken
    public void addWindow(WindowState w) {
        super.addWindow(w);
        boolean gotReplacementWindow = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            gotReplacementWindow |= ((WindowState) this.mChildren.get(i)).setReplacementWindowIfNeeded(w);
        }
        if (gotReplacementWindow) {
            this.mWmService.scheduleWindowReplacementTimeouts(this);
        }
        checkKeyguardFlagsChanged();
    }

    /* access modifiers changed from: package-private */
    public void removeChild(WindowState child) {
        if (this.mChildren.contains(child)) {
            super.removeChild((AppWindowToken) child);
            checkKeyguardFlagsChanged();
            updateLetterboxSurface(child);
        }
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
        Slog.i(TAG, "reparent: moving app token=" + this + " to task=" + task.mTaskId + " at " + position);
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
            getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
        } else {
            throw new IllegalArgumentException("window token=" + this + " current task=" + currentTask + " belongs to a different stack than " + task);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowToken, com.android.server.wm.WindowContainer
    public void onDisplayChanged(DisplayContent dc) {
        Task task;
        DisplayContent prevDc = this.mDisplayContent;
        super.onDisplayChanged(dc);
        if (prevDc != null && prevDc != this.mDisplayContent) {
            if (prevDc.mOpeningApps.remove(this)) {
                this.mDisplayContent.mOpeningApps.add(this);
                this.mDisplayContent.prepareAppTransition(prevDc.mAppTransition.getAppTransition(), true);
                this.mDisplayContent.executeAppTransition();
            }
            if (prevDc.mChangingApps.remove(this)) {
                clearChangeLeash(getPendingTransaction(), true);
            }
            prevDc.mClosingApps.remove(this);
            if (prevDc.mFocusedApp == this) {
                prevDc.setFocusedApp(null);
                TaskStack stack = dc.getTopStack();
                if (!(stack == null || (task = (Task) stack.getTopChild()) == null || task.getTopChild() != this)) {
                    dc.setFocusedApp(this);
                }
            }
            Letterbox letterbox = this.mLetterbox;
            if (letterbox != null) {
                letterbox.onMovedToDisplay(this.mDisplayContent.getDisplayId());
            }
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
            this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
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
                this.mWmService.registerAppFreezeListener(this);
                this.mWmService.mAppsFreezingScreen++;
                if (this.mWmService.mAppsFreezingScreen == 1) {
                    this.mWmService.startFreezingDisplayLocked(0, 0, getDisplayContent());
                    this.mWmService.mH.removeMessages(17);
                    long delayTime = 2000;
                    if (this.mWmService.mShouldResetTime) {
                        this.mWmService.mShouldResetTime = false;
                        delayTime = 600;
                    }
                    this.mWmService.mH.sendEmptyMessageDelayed(17, delayTime);
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
                this.mWmService.unregisterAppFreezeListener(this);
                WindowManagerService windowManagerService = this.mWmService;
                windowManagerService.mAppsFreezingScreen--;
                this.mWmService.mLastFinishedFreezeSource = this;
            }
            if (unfreezeSurfaceNow) {
                if (unfrozeWindows) {
                    this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
                }
                this.mWmService.stopFreezingDisplayLocked();
            }
        }
    }

    @Override // com.android.server.wm.WindowManagerService.AppFreezeListener
    public void onAppFreezeTimeout() {
        Slog.w(TAG, "Force clearing freeze: " + this);
        stopFreezingScreen(true, true);
    }

    /* access modifiers changed from: package-private */
    public void transferStartingWindowFromHiddenAboveTokenIfNeeded() {
        Task task = getTask();
        if (task != null) {
            for (int i = task.mChildren.size() - 1; i >= 0; i--) {
                AppWindowToken fromToken = (AppWindowToken) task.mChildren.get(i);
                if (fromToken == this) {
                    return;
                }
                if (fromToken.hiddenRequested && transferStartingWindow(fromToken.token)) {
                    return;
                }
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
            getDisplayContent().mSkipAppTransitionAnimation = true;
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Moving existing starting " + tStartingWindow + " from " + fromToken + " to " + this);
            }
            long origId = Binder.clearCallingIdentity();
            try {
                this.mStartingData = fromToken.mStartingData;
                this.startingSurface = fromToken.startingSurface;
                this.startingDisplayed = fromToken.startingDisplayed;
                fromToken.startingDisplayed = false;
                this.startingWindow = tStartingWindow;
                this.reportedVisible = fromToken.reportedVisible;
                fromToken.mStartingData = null;
                fromToken.startingSurface = null;
                fromToken.startingWindow = null;
                fromToken.startingMoved = true;
                tStartingWindow.mToken = this;
                tStartingWindow.mAppToken = this;
                Flog.i(301, "Removing starting " + tStartingWindow + " from " + fromToken + " to " + this);
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
                this.mUseTransferredAnimation = true;
                this.mWmService.updateFocusedWindowLocked(3, true);
                getDisplayContent().setLayoutNeeded();
                this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
                return true;
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else if (fromToken.mStartingData == null) {
            return false;
        } else {
            Flog.i(301, "Moving pending starting from " + fromToken + " to " + this);
            this.mStartingData = fromToken.mStartingData;
            fromToken.mStartingData = null;
            fromToken.startingMoved = true;
            scheduleAddStartingWindow();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLastWindow(WindowState win) {
        return this.mChildren.size() == 1 && this.mChildren.get(0) == win;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onAppTransitionDone() {
        this.sendingToBottom = false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public int getOrientation(int candidate) {
        int magicOrientation;
        if (HwMwUtils.ENABLED && (magicOrientation = HwMwUtils.performPolicy(9, new Object[]{this.appToken}).getInt("BUNDLE_RESULT_ORIENTATION", -3)) != -3) {
            return magicOrientation;
        }
        if (candidate == 3) {
            return this.mOrientation;
        }
        if (this.sendingToBottom || getDisplayContent().mClosingApps.contains(this)) {
            return -2;
        }
        if (isVisible() || getDisplayContent().mOpeningApps.contains(this)) {
            return this.mOrientation;
        }
        return -2;
    }

    /* access modifiers changed from: package-private */
    public int getOrientationIgnoreVisibility() {
        return this.mOrientation;
    }

    /* access modifiers changed from: package-private */
    public boolean inSizeCompatMode() {
        return this.mSizeCompatBounds != null && !inHwMagicWindowingMode();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowToken
    public float getSizeCompatScale() {
        return inSizeCompatMode() ? this.mSizeCompatScale : super.getSizeCompatScale();
    }

    /* access modifiers changed from: package-private */
    public Rect getResolvedOverrideBounds() {
        return getResolvedOverrideConfiguration().windowConfiguration.getBounds();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        Rect stackBounds;
        this.mPrevWinMode = getWindowingMode();
        this.mTmpPrevBounds.set(getBounds());
        super.onConfigurationChanged(newParentConfig);
        Task task = getTask();
        Rect overrideBounds = getResolvedOverrideBounds();
        boolean isInVirtualDisplay = false;
        if (this.mDisplayContent != null) {
            isInVirtualDisplay = this.mWmService.mAtmService.mHwATMSEx.isVirtualDisplayId(this.mDisplayContent.mDisplayId, "padCast");
        }
        if (task != null && !overrideBounds.isEmpty() && !HwFoldScreenState.isFoldScreenDevice() && !inHwMagicWindowingMode() && !isInVirtualDisplay && (task.mTaskRecord == null || task.mTaskRecord.getConfiguration().orientation == newParentConfig.orientation)) {
            Rect taskBounds = task.getBounds();
            if (overrideBounds.width() != taskBounds.width() || overrideBounds.height() > taskBounds.height()) {
                calculateCompatBoundsTransformation(newParentConfig);
                updateSurfacePosition();
            } else if (this.mSizeCompatBounds != null) {
                this.mSizeCompatBounds = null;
                this.mSizeCompatScale = 1.0f;
                updateSurfacePosition();
            }
        }
        if ((overrideBounds.isEmpty() && this.mSizeCompatBounds != null) || isInVirtualDisplay) {
            this.mSizeCompatBounds = null;
            this.mSizeCompatScale = 1.0f;
            updateSurfacePosition();
        }
        int winMode = getWindowingMode();
        int i = this.mPrevWinMode;
        if (i != winMode) {
            if (i != 0 && winMode == 2) {
                this.mDisplayContent.mPinnedStackControllerLocked.resetReentrySnapFraction(this);
            } else if (this.mPrevWinMode == 2 && winMode != 0 && !isHidden()) {
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
            } else if (shouldStartChangeTransition(this.mPrevWinMode, winMode)) {
                if ((!WindowConfiguration.isHwTvFreeFormWindowingMode(this.mPrevWinMode) || winMode != 1) && ((this.mPrevWinMode != 1 || !WindowConfiguration.isHwTvFreeFormWindowingMode(winMode)) && ((!getStack().isTvSplitExitTop() || !WindowConfiguration.isHwTvSplitWindowingMode(this.mPrevWinMode) || winMode != 1) && (this.mPrevWinMode != 1 || !WindowConfiguration.isHwTvSplitWindowingMode(winMode))))) {
                    this.mIsTvUsedPreSnapshot = false;
                } else {
                    Slog.i(TAG, "TvUsedPreWindowMode: " + this.mPrevWinMode + ", TvUsedCurrentWindowMode: " + winMode);
                    this.mIsTvUsedPreSnapshot = true;
                }
                initializeChangeTransition(this.mTmpPrevBounds);
            }
        }
    }

    private boolean shouldStartChangeTransition(int mPrevWinMode2, int newWinMode) {
        if (this.mWmService.mDisableTransitionAnimation || !isVisible() || ((getDisplayContent().mAppTransition.isTransitionSet() && !getDisplayContent().mAppTransition.isTvTransitionSet()) || getSurfaceControl() == null)) {
            return false;
        }
        if (mPrevWinMode2 == 1 && WindowConfiguration.isHwTvMultiWindowingMode(newWinMode)) {
            return true;
        }
        if (WindowConfiguration.isHwTvMultiWindowingMode(mPrevWinMode2) && newWinMode == 1) {
            return true;
        }
        this.mIsNeedChangeAnimationCorner = WindowConfiguration.isHwFreeFormWindowingMode(mPrevWinMode2) && !WindowConfiguration.isHwMultiStackWindowingMode(newWinMode);
        if (this.mIsNeedChangeAnimationCorner) {
            return true;
        }
        if ((mPrevWinMode2 == 5) != (newWinMode == 5)) {
            return true;
        }
        if (WindowConfiguration.isHwMultiStackWindowingMode(mPrevWinMode2) && !WindowConfiguration.isHwMultiStackWindowingMode(newWinMode)) {
            return true;
        }
        if (getStack() == null || !this.mWmService.mAtmService.mHwATMSEx.isPadCastStack(getStack().mActivityStack) || WindowConfiguration.isHwMultiStackWindowingMode(mPrevWinMode2) || !WindowConfiguration.isHwMultiStackWindowingMode(newWinMode)) {
            return false;
        }
        return true;
    }

    private void initializeChangeTransition(Rect startBounds) {
        this.mDisplayContent.prepareAppTransition(27, false, 0, false);
        this.mDisplayContent.mChangingApps.add(this);
        this.mTransitStartRect.set(startBounds);
        int transitStartLeft = this.mTransitStartRect.left;
        int transitStartTop = this.mTransitStartRect.top;
        this.mTransitStartRect.scale(getStack().mHwStackScale);
        this.mTransitStartRect.offsetTo(transitStartLeft, transitStartTop);
        SurfaceControl.Builder parent = makeAnimationLeash().setParent(getAnimationLeashParent());
        SurfaceControl.Builder builder = parent.setName(getSurfaceControl() + " - interim-change-leash");
        this.mWmService.mHwWMSEx.setLandAnimationInfo(false, null);
        this.mTransitChangeLeash = builder.build();
        SurfaceControl.Transaction t = getPendingTransaction();
        if (this.mIsTvUsedPreSnapshot) {
            ActivityManager.TaskSnapshot snapshot = this.mWmService.getTaskSnapshot(getTask().mTaskId, getTask().mUserId, false, false);
            if (snapshot != null) {
                t.setMatrix(this.mTransitChangeLeash, (((float) snapshot.getSnapshot().getWidth()) * 1.0f) / ((float) getDisplayContent().getDisplayInfo().logicalWidth), 0.0f, 0.0f, (((float) snapshot.getSnapshot().getHeight()) * 1.0f) / ((float) getDisplayContent().getDisplayInfo().logicalHeight));
                t.setPosition(this.mTransitChangeLeash, (float) startBounds.left, (float) startBounds.top);
            }
        } else if (getStack().isTvSplitExitTop() || !WindowConfiguration.isHwTvSplitWindowingMode(this.mPrevWinMode)) {
            t.setWindowCrop(this.mTransitChangeLeash, this.mTransitStartRect.width(), this.mTransitStartRect.height());
            t.setPosition(this.mTransitChangeLeash, (float) startBounds.left, (float) startBounds.top);
        } else {
            t.setAlpha(this.mTransitChangeLeash, 0.0f);
        }
        t.show(this.mTransitChangeLeash);
        t.reparent(getSurfaceControl(), this.mTransitChangeLeash);
        onAnimationLeashCreated(t, this.mTransitChangeLeash);
        ArraySet<Integer> activityTypes = new ArraySet<>();
        activityTypes.add(Integer.valueOf(getActivityType()));
        RemoteAnimationAdapter adapter = this.mDisplayContent.mAppTransitionController.getRemoteAnimationOverride(this, 27, activityTypes);
        if (adapter != null && !adapter.getChangeNeedsSnapshot()) {
            return;
        }
        if (getStack().isTvSplitExitTop() || !WindowConfiguration.isHwTvSplitWindowingMode(this.mPrevWinMode) || getWindowingMode() != 1) {
            Task task = getTask();
            if (this.mThumbnail == null && task != null && !hasCommittedReparentToAnimationLeash()) {
                if (this.mIsTvUsedPreSnapshot) {
                    ActivityManager.TaskSnapshot snapshot2 = this.mWmService.getTaskSnapshot(getTask().mTaskId, getTask().mUserId, false, false);
                    Slog.i(TAG, "getTaskSnapshot: " + snapshot2 + ", mIsTvUsedPreSnapshot: " + this.mIsTvUsedPreSnapshot);
                    if (snapshot2 != null) {
                        this.mThumbnail = new AppWindowThumbnail(t, this, snapshot2.getSnapshot(), false);
                        this.mThumbnail.createInnerThumbnailLeash(this.mTransitStartRect, t);
                        return;
                    }
                    return;
                }
                SurfaceControl.ScreenshotGraphicBuffer snapshot3 = this.mWmService.mTaskSnapshotController.createTaskSnapshot(task, 1.0f);
                if (snapshot3 != null) {
                    this.mThumbnail = new AppWindowThumbnail(t, this, snapshot3.getGraphicBuffer(), true);
                    return;
                }
                return;
            }
            return;
        }
        Slog.d(TAG, "initializeChangeTransition: no need to create thumbnail");
    }

    /* access modifiers changed from: package-private */
    public boolean isInChangeTransition() {
        return this.mTransitChangeLeash != null || AppTransition.isChangeTransit(this.mTransit);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public AppWindowThumbnail getThumbnail() {
        return this.mThumbnail;
    }

    private void calculateCompatBoundsTransformation(Configuration newParentConfig) {
        Rect parentAppBounds = newParentConfig.windowConfiguration.getAppBounds();
        Rect parentBounds = newParentConfig.windowConfiguration.getBounds();
        Rect viewportBounds = parentAppBounds != null ? parentAppBounds : parentBounds;
        Rect appBounds = getWindowConfiguration().getAppBounds();
        Rect contentBounds = appBounds != null ? appBounds : getResolvedOverrideBounds();
        float contentW = (float) contentBounds.width();
        float contentH = (float) contentBounds.height();
        float viewportW = (float) viewportBounds.width();
        float viewportH = (float) viewportBounds.height();
        this.mSizeCompatScale = (contentW > viewportW || contentH > viewportH) ? Math.min(viewportW / contentW, viewportH / contentH) : 1.0f;
        int offsetX = ((int) (((viewportW - (this.mSizeCompatScale * contentW)) + 1.0f) * 0.5f)) + viewportBounds.left;
        if (this.mSizeCompatBounds == null) {
            this.mSizeCompatBounds = new Rect();
        }
        this.mSizeCompatBounds.set(contentBounds);
        this.mSizeCompatBounds.offsetTo(0, 0);
        this.mSizeCompatBounds.scale(this.mSizeCompatScale);
        this.mSizeCompatBounds.top = parentBounds.top;
        this.mSizeCompatBounds.bottom += viewportBounds.top;
        this.mSizeCompatBounds.left += offsetX;
        this.mSizeCompatBounds.right += offsetX;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public Rect getBounds() {
        if (this.mSizeCompatBounds == null || inHwMagicWindowingMode()) {
            return super.getBounds();
        }
        return this.mSizeCompatBounds;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public boolean matchParentBounds() {
        WindowContainer parent;
        if (!super.matchParentBounds() && (parent = getParent()) != null && !parent.getBounds().equals(getResolvedOverrideBounds())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void checkAppWindowsReadyToShow() {
        boolean z = this.allDrawn;
        if (z != this.mLastAllDrawn) {
            this.mLastAllDrawn = z;
            if (z) {
                if (this.mFreezingScreen) {
                    showAllWindowsLocked();
                    stopFreezingScreen(false, true);
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.i(TAG, "Setting mOrientationChangeComplete=true because wtoken " + this + " numInteresting=" + this.mNumInterestingWindows + " numDrawn=" + this.mNumDrawnWindows);
                    }
                    setAppLayoutChanges(4, "checkAppWindowsReadyToShow: freezingScreen");
                    return;
                }
                setAppLayoutChanges(8, "checkAppWindowsReadyToShow");
                if (!getDisplayContent().mOpeningApps.contains(this) && canShowWindows()) {
                    showAllWindowsLocked();
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
        int numInteresting;
        if (!this.allDrawn && (numInteresting = this.mNumInterestingWindows) > 0 && allDrawnStatesConsidered() && this.mNumDrawnWindows >= numInteresting && !isRelaunching()) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "allDrawn: " + this + " interesting=" + numInteresting + " drawn=" + this.mNumDrawnWindows);
            }
            this.allDrawn = true;
            if (this.mDisplayContent != null) {
                this.mDisplayContent.setLayoutNeeded();
            }
            this.mWmService.mH.obtainMessage(32, this.token).sendToTarget();
            TaskStack pinnedStack = this.mDisplayContent.getPinnedStack();
            if (pinnedStack != null) {
                pinnedStack.onAllWindowsDrawn();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean keyDispatchingTimedOut(String reason, int windowPid) {
        ActivityRecord activityRecord = this.mActivityRecord;
        return activityRecord != null && activityRecord.keyDispatchingTimedOut(reason, windowPid);
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
        if (this.mLastTransactionSequence != ((long) this.mWmService.mTransactionSequence)) {
            this.mLastTransactionSequence = (long) this.mWmService.mTransactionSequence;
            this.mNumDrawnWindows = 0;
            this.startingDisplayed = false;
            this.mNumInterestingWindows = findMainWindow(false) != null ? 1 : 0;
        }
        WindowStateAnimator winAnimator = w.mWinAnimator;
        if (this.allDrawn || !w.mightAffectAllDrawn()) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v(TAG, "Eval win " + w + ": isDrawn=" + w.isDrawnLw() + ", isAnimationSet=" + isSelfAnimating());
            if (!w.isDrawnLw()) {
                Slog.v(TAG, "Not displayed: s=" + winAnimator.mSurfaceController + " pv=" + w.isVisibleByPolicy() + " mDrawState=" + winAnimator.drawStateToString() + " ph=" + w.isParentWindowHidden() + " th=" + this.hiddenRequested + " a=" + isSelfAnimating());
            }
        }
        if (w != this.startingWindow) {
            if (!w.isInteresting()) {
                return false;
            }
            if (findMainWindow(false) != w) {
                this.mNumInterestingWindows++;
            }
            if (!w.isDrawnLw()) {
                return false;
            }
            this.mNumDrawnWindows++;
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "tokenMayBeDrawn: " + this + " w=" + w + " numInteresting=" + this.mNumInterestingWindows + " freezingScreen=" + this.mFreezingScreen + " mAppFreezing=" + w.mAppFreezing);
            }
            return true;
        } else if (!w.isDrawnLw()) {
            return false;
        } else {
            ActivityRecord activityRecord = this.mActivityRecord;
            if (activityRecord != null) {
                activityRecord.onStartingWindowDrawn(SystemClock.uptimeMillis());
            }
            this.startingDisplayed = true;
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void layoutLetterbox(WindowState winHint) {
        Rect spaceToFill;
        WindowState w = findMainWindow();
        if (w == null) {
            return;
        }
        if (winHint == null || w == winHint) {
            boolean surfaceReady = w.isDrawnLw() || w.mWinAnimator.mSurfaceDestroyDeferred || w.isDragResizeChanged();
            if (!(surfaceReady && w.isLetterboxedAppWindow() && fillsParent()) || this.mIsNeedBackgroundSurface) {
                Letterbox letterbox = this.mLetterbox;
                if (letterbox != null) {
                    letterbox.hide();
                }
            } else {
                if (this.mLetterbox == null) {
                    this.mLetterbox = new Letterbox(new Supplier<SurfaceControl.Builder>() {
                        /* class com.android.server.wm.AppWindowToken.AnonymousClass1 */

                        @Override // java.util.function.Supplier
                        @RCUnownedThisRef
                        public SurfaceControl.Builder get() {
                            return AppWindowToken.this.makeChildSurface(null);
                        }
                    });
                    this.mLetterbox.attachInput(w);
                }
                getPosition(this.mTmpPoint);
                if (inMultiWindowMode() || getStack() == null) {
                    spaceToFill = getTask().getDisplayedBounds();
                } else {
                    spaceToFill = getStack().getDisplayedBounds();
                }
                this.mLetterbox.layout(spaceToFill, w.getFrameLw(), this.mTmpPoint);
                if (HwDisplaySizeUtil.hasSideInScreen()) {
                    Rect insets = this.mLetterbox.getInsets();
                    if (insets.left == insets.right && insets.left != 0 && insets.left == HwDisplaySizeUtil.getInstance(this.mWmService).getSafeSideWidth()) {
                        this.mLetterbox.hide(false, true, false, true);
                    } else if (insets.top == insets.bottom && insets.top != 0 && insets.top == HwDisplaySizeUtil.getInstance(this.mWmService).getSafeSideWidth()) {
                        this.mLetterbox.hide(true, false, true, false);
                    }
                }
            }
            if (!this.mIsNeedBackgroundSurface || !surfaceReady) {
                BackgroundSurface backgroundSurface = this.mBackgroundSurface;
                if (backgroundSurface != null) {
                    backgroundSurface.updateBackgroundSurfaceLocked(getPendingTransaction(), false);
                }
            } else {
                if (this.mBackgroundSurface == null) {
                    getPosition(this.mTmpPoint);
                    this.mBackgroundSurface = new BackgroundSurface(this.mWmService);
                    this.mBackgroundSurface.createBackgroundSurfaceLocked(getSurfaceControl(), getStack() == null ? getBounds() : getStack().getDisplayedBounds(), this.mTmpPoint);
                    if (this.mDisplayContent != null) {
                        this.mBackgroundSurface.drawBackgroundBitmapLocked(this.mWmService.mAtmService.mHwATMSEx.getPadCastWallpaperBitmap(this.mDisplayContent.mDisplayId));
                    }
                }
                this.mBackgroundSurface.updateBackgroundSurfaceLocked(getPendingTransaction(), true);
            }
            createAndDrawMagicWindowSurface(surfaceReady);
        }
    }

    private void createAndDrawMagicWindowSurface(boolean surfaceReady) {
        if (!surfaceReady || !inHwMagicWindowingMode() || getTask() == null) {
            destroyMagicWindowSurface();
            return;
        }
        Bundle bundle = HwMwUtils.performPolicy((int) WindowManagerService.H.SET_LAUNCHER_VISIBLE_IN_FINGER_BOOST, new Object[]{getTask().mTaskRecord, this.mDisplayContent.getDisplayInfo()});
        int bgColor = bundle.getInt("bgColor", -1);
        Parcelable bgRectObj = bundle.getParcelable("bgRect");
        Parcelable bgPointObj = bundle.getParcelable("bgPoint");
        Parcelable masterObj = bundle.getParcelable("masterBounds");
        if (bgColor == -1 || !(masterObj instanceof Rect) || !(bgRectObj instanceof Rect) || !(bgPointObj instanceof Point) || !((Rect) masterObj).equals(getRequestedOverrideBounds())) {
            destroyMagicWindowSurface();
        } else {
            createAndDrawMagicWindowSurface(bgColor, (Rect) bgRectObj, (Point) bgPointObj);
        }
    }

    private void createAndDrawMagicWindowSurface(int bgColor, Rect magicWinBgRect, Point magicWinBgPoint) {
        if (this.mMagicWindowSurface == null) {
            this.mMagicWindowSurface = new BackgroundSurface(this.mWmService);
            this.mMagicWindowSurface.createBackgroundSurfaceLocked(getSurfaceControl(), magicWinBgRect, magicWinBgPoint);
            Slog.d(TAG, "HWMW_Surface createMagicWindowSurface rect=" + magicWinBgRect + " pos=" + magicWinBgPoint + "bgColor=" + bgColor);
        }
        this.mMagicWindowSurface.drawBackgroundColorLocked(bgColor);
        this.mMagicWindowSurface.updateBackgroundSurfaceLocked(getPendingTransaction(), true);
    }

    private void destroyMagicWindowSurface() {
        BackgroundSurface backgroundSurface = this.mMagicWindowSurface;
        if (backgroundSurface != null) {
            backgroundSurface.updateBackgroundSurfaceLocked(getPendingTransaction(), false);
            this.mMagicWindowSurface.destroyBackgroundSurfaceLocked(getPendingTransaction());
            this.mMagicWindowSurface = null;
            Slog.d(TAG, "HWMW_Surface destroyMagicWindowSurface appWindowToken=" + this);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateLetterboxSurface(WindowState winHint) {
        WindowState w = findMainWindow();
        if (w == winHint || winHint == null || w == null) {
            layoutLetterbox(winHint);
            Letterbox letterbox = this.mLetterbox;
            if (letterbox != null && letterbox.needsApplySurfaceChanges()) {
                if (isInChangeTransition()) {
                    this.mLetterbox.destroy();
                    this.mLetterbox = null;
                    return;
                }
                this.mLetterbox.applySurfaceChanges(getPendingTransaction());
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (!this.mIsExiting || waitingForReplacement()) {
            return forAllWindowsUnchecked(callback, traverseTopToBottom);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void forAllAppWindows(Consumer<AppWindowToken> callback) {
        callback.accept(this);
    }

    /* access modifiers changed from: package-private */
    public boolean forAllWindowsUnchecked(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        return super.forAllWindows(callback, traverseTopToBottom);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowToken
    public AppWindowToken asAppWindowToken() {
        return this;
    }

    private String startingWindowTypeToString(int type) {
        if (type == 0) {
            return "NONE";
        }
        if (type == 1) {
            return "SNAPSHOT";
        }
        if (type != 2) {
            return Integer.toString(type);
        }
        return "SPLASH_SCREEN";
    }

    /* access modifiers changed from: package-private */
    public boolean addStartingWindow(String pkg, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, IBinder transferFrom, boolean newTask, boolean taskSwitch, boolean processRunning, boolean allowTaskSnapshot, boolean activityCreated, boolean fromRecents) {
        IBinder transferFrom2;
        int windowFlags2;
        boolean hwStartWindow;
        if (!okToDisplay() && !this.mWmService.mHwWMSEx.isNeedLandAni()) {
            return false;
        }
        ActivityStack stack = this.mActivityRecord.getActivityStack();
        if (stack != null && stack.isPendingShow()) {
            return false;
        }
        if (this.mStartingData != null) {
            Flog.i(301, "App already has startingData for " + this);
            return false;
        }
        WindowState mainWin = findMainWindow();
        if (mainWin == null || !mainWin.mWinAnimator.getShown()) {
            try {
                if (sSkipStartingWindowActivitys.contains(this.appToken.getName())) {
                    return false;
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "fail to getName for " + this.appToken);
            }
            ActivityManager.TaskSnapshot snapshot = this.mWmService.mTaskSnapshotController.getSnapshot(getTask().mTaskId, getTask().mUserId, false, false);
            int type = getStartingWindowType(newTask, taskSwitch, processRunning, allowTaskSnapshot, activityCreated, fromRecents, snapshot);
            if (snapshot != null && (snapshot.getWindowingMode() == 103 || inHwMagicWindowingMode())) {
                type = 0;
            }
            Flog.i(301, "Get startingwindow type:" + startingWindowTypeToString(type) + " for " + this);
            if (type == 1 && shouldCreateSnapshot(pkg, snapshot)) {
                return createSnapshot(snapshot);
            }
            if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                Slog.v(TAG, "Checking theme of starting window: 0x" + Integer.toHexString(theme));
            }
            if (theme != 0) {
                AttributeCache.Entry ent = AttributeCache.instance().get(pkg, theme, R.styleable.Window, this.mWmService.mCurrentUserId);
                if (ent == null) {
                    return false;
                }
                boolean windowIsTranslucent = ent.array.getBoolean(5, false);
                boolean windowIsFloating = ent.array.getBoolean(4, false);
                boolean windowShowWallpaper = ent.array.getBoolean(14, false);
                boolean windowDisableStarting = ent.array.getBoolean(12, false);
                if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
                    Slog.v(TAG, "Translucent=" + windowIsTranslucent + " Floating=" + windowIsFloating + " ShowWallpaper=" + windowShowWallpaper);
                }
                if (!DisplayPolicy.LAUNCHER_PACKAGE_NAME.equals(pkg)) {
                    if (!this.mWmService.isSplitMode()) {
                        if (HwServiceFactory.getHwAppWindowTokenEx() == null) {
                            hwStartWindow = false;
                        } else {
                            hwStartWindow = HwServiceFactory.getHwAppWindowTokenEx().isHwStartWindowEnabled(pkg, compatInfo);
                        }
                        int currentNightMode = getMergedOverrideConfiguration().uiMode & 48;
                        if (hwStartWindow && currentNightMode != 32) {
                            int result = HwServiceFactory.getHwAppWindowTokenEx().continueHwStartWindow(compatInfo.mAppInfo, transferFrom, this.appToken, new boolean[]{processRunning, windowIsFloating, windowIsTranslucent, windowDisableStarting, taskSwitch, windowShowWallpaper, fromRecents});
                            if (result < 0) {
                                return false;
                            }
                            if (result > 0) {
                                transferFrom2 = HwServiceFactory.getHwAppWindowTokenEx().getTransferFrom(compatInfo.mAppInfo);
                            } else {
                                transferFrom2 = transferFrom;
                            }
                        } else if (windowIsTranslucent || windowIsFloating || windowDisableStarting) {
                            return false;
                        } else {
                            transferFrom2 = transferFrom;
                        }
                        if (windowShowWallpaper) {
                            if (getDisplayContent().mWallpaperController.getWallpaperTarget() == null) {
                                windowFlags2 = windowFlags | 1048576;
                            } else if (!hwStartWindow) {
                                return false;
                            }
                        }
                        windowFlags2 = windowFlags;
                    }
                }
                return false;
            }
            windowFlags2 = windowFlags;
            transferFrom2 = transferFrom;
            if (transferStartingWindow(transferFrom2)) {
                return true;
            }
            if (type != 2) {
                Flog.i(301, "There is no existing starting window");
                return false;
            }
            Flog.i(301, "Creating SplashScreenStartingData for " + this);
            this.mStartingData = new SplashScreenStartingData(this.mWmService, pkg, theme, compatInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags2, getMergedOverrideConfiguration());
            scheduleAddStartingWindow();
            return true;
        }
        Flog.i(301, "App already has a visible window for " + this);
        return false;
    }

    private boolean createSnapshot(ActivityManager.TaskSnapshot snapshot) {
        if (snapshot == null) {
            return false;
        }
        Flog.i(301, "Creating SnapshotStartingData for " + this);
        this.mStartingData = new SnapshotStartingData(this.mWmService, snapshot);
        scheduleAddStartingWindow();
        return true;
    }

    private boolean shouldCreateSnapshot(String pkg, ActivityManager.TaskSnapshot snapshot) {
        if (IS_HW_TV_MULTI_TASK_ENABLE || sDisableSnapShots.contains(pkg) || !getTask().isSamePackageInTask()) {
            return false;
        }
        int windowingMode = snapshot.getWindowingMode();
        if (windowingMode == 5 && !getTask().inFreeformWindowingMode()) {
            return false;
        }
        if (WindowConfiguration.isHwMultiStackWindowingMode(windowingMode)) {
            if (windowingMode != getTask().getWindowingMode()) {
                return false;
            }
            if (snapshot.getSnapshot() != null && Math.abs((((float) snapshot.getSnapshot().getWidth()) / ((float) snapshot.getSnapshot().getHeight())) - (((float) getTask().getBounds().width()) / ((float) getTask().getBounds().height()))) > 0.01f) {
                return false;
            }
        }
        if ((windowingMode == 1 || windowingMode == 0) && getTask().inHwMultiStackWindowingMode()) {
            return false;
        }
        return true;
    }

    private boolean isFreeformExit(int transit, WindowState windowState) {
        if (transit == 11) {
            return true;
        }
        if ((transit == 9 || (isActivityCloseTransit(transit) && isAllActivityExit(windowState))) && this.mWmService.mHwWMSEx.isLastOneApp(getDisplayContent())) {
            return true;
        }
        return false;
    }

    private boolean isAllActivityExit(WindowState windowState) {
        if (windowState == null || windowState.getTask() == null || windowState.getTask().mTaskRecord == null || windowState.getTask().mTaskRecord.getTopActivity() == null) {
            return true;
        }
        return false;
    }

    private boolean isActivityCloseTransit(int transit) {
        return transit == 7 || transit == 26 || transit == 25;
    }

    /* access modifiers changed from: package-private */
    public void scheduleAddStartingWindow() {
        if (!this.mWmService.mAnimationHandler.hasCallbacks(this.mAddStartingWindow)) {
            Flog.i(301, "Enqueueing ADD_STARTING for " + this);
            this.mWmService.mAnimationHandler.postAtFrontOfQueue(this.mAddStartingWindow);
            this.mWmService.mHwWMSEx.setRtgThreadForAnimation(true);
        }
    }

    private int getStartingWindowType(boolean newTask, boolean taskSwitch, boolean processRunning, boolean allowTaskSnapshot, boolean activityCreated, boolean fromRecents, ActivityManager.TaskSnapshot snapshot) {
        if (getDisplayContent().mAppTransition.getAppTransition() == 19) {
            return 0;
        }
        int nType = 2;
        if (newTask || !processRunning || (taskSwitch && !activityCreated)) {
            return 2;
        }
        if (!taskSwitch || !allowTaskSnapshot) {
            return 0;
        }
        if (this.mWmService.mLowRamTaskSnapshotsAndRecents) {
            return 2;
        }
        if (snapshot == null) {
            nType = 0;
        } else if (snapshotOrientationSameAsTask(snapshot) || fromRecents) {
            nType = 1;
        }
        if (snapshot != null && !snapshotOrientationSameAsTask(snapshot) && fromRecents) {
            Slog.d(TAG, "Skip adding snapshot startingWindow to avoid using incorrect snapshot, " + this);
            nType = 0;
        }
        if (1 != nType || isContainedOnlyOneVisibleWindow()) {
            return nType;
        }
        Slog.d(TAG, "Skip adding snapshot startingWindow for activity with more than one window, " + this);
        return 0;
    }

    private boolean snapshotOrientationSameAsTask(ActivityManager.TaskSnapshot snapshot) {
        if (snapshot != null && getTask().getConfiguration().orientation == snapshot.getOrientation()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void removeStartingWindow() {
        if (this.startingWindow == null) {
            if (this.mStartingData != null) {
                Flog.i(301, "Clearing mStartingData for token=" + this);
                this.mStartingData = null;
            }
        } else if (this.mStartingData != null) {
            WindowManagerPolicy.StartingSurface surface = this.startingSurface;
            this.mStartingData = null;
            this.startingSurface = null;
            this.startingWindow = null;
            this.startingDisplayed = false;
            if (surface == null) {
                Flog.i(301, "startingWindow was set but startingSurface==null, couldn't remove");
                return;
            }
            Flog.i(301, "Schedule remove starting " + this + " startingWindow=" + this.startingWindow + " startingView=" + this.startingSurface);
            this.mWmService.mHwWMSEx.setRtgThreadForAnimation(false);
            this.mWmService.mAnimationHandler.post(new Runnable(surface) {
                /* class com.android.server.wm.$$Lambda$AppWindowToken$JR7Cr936i7DlrzcjMaXdnVHq3NU */
                private final /* synthetic */ WindowManagerPolicy.StartingSurface f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    AppWindowToken.lambda$removeStartingWindow$3(this.f$0);
                }
            });
        } else {
            Flog.i(301, "Tried to remove starting window but startingWindow was null:" + this);
        }
    }

    static /* synthetic */ void lambda$removeStartingWindow$3(WindowManagerPolicy.StartingSurface surface) {
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v(TAG, "Removing startingView=" + surface);
        }
        try {
            surface.remove();
        } catch (Exception e) {
            Slog.w(TAG, "Exception when removing starting window", e);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
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
            if ((((WindowState) this.mChildren.get(i)).mAttrs.flags & 4194304) != 0) {
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
        if (this.mChildren.size() != 0 || !this.mWmService.mPolicy.isKeyguardOccluded()) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                if (!WindowConfiguration.isHwPCFreeFormWindowingMode(((WindowState) this.mChildren.get(i)).getWindowingMode()) && (((WindowState) this.mChildren.get(i)).mAttrs.flags & 524288) != 0) {
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
            this.mWmService.notifyKeyguardFlagsChanged(null, getDisplayContent().getDisplayId());
        }
        this.mLastContainsDismissKeyguardWindow = containsDismissKeyguard;
        this.mLastContainsShowWhenLockedWindow = containsShowWhenLocked;
    }

    /* access modifiers changed from: package-private */
    public WindowState getImeTargetBelowWindow(WindowState w) {
        int index = this.mChildren.indexOf(w);
        if (index <= 0) {
            return null;
        }
        WindowState target = (WindowState) this.mChildren.get(index - 1);
        if (target.canBeImeTarget()) {
            return target;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowState getHighestAnimLayerWindow(WindowState currentTarget) {
        WindowState candidate = null;
        for (int i = this.mChildren.indexOf(currentTarget); i >= 0; i--) {
            WindowState w = (WindowState) this.mChildren.get(i);
            if (!w.mRemoved && candidate == null) {
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

    static /* synthetic */ boolean lambda$shouldUseAppThemeSnapshot$4(WindowState w) {
        return (w.mAttrs.flags & 8192) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldUseAppThemeSnapshot() {
        return this.mDisablePreviewScreenshots || forAllWindows($$Lambda$AppWindowToken$NwyjcPHhaxqkEKaJxeRVkzuWYXc.INSTANCE, true);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl getAppAnimationLayer() {
        int i = 0;
        if (HwServiceFactory.getHwAppWindowTokenEx() == null ? false : HwServiceFactory.getHwAppWindowTokenEx().isHwMwAnimationBelowStack(this)) {
            return getAppAnimationLayer(2);
        }
        if (!inHwFreeFormWindowingMode() || isActivityTypeHome()) {
            if (isActivityTypeHome()) {
                i = 2;
            } else if (needsZBoost()) {
                i = 1;
            }
            return getAppAnimationLayer(i);
        }
        return getAppAnimationLayer(needsZBoost() ? 11 : 10);
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public SurfaceControl getAnimationLeashParent() {
        if (!inPinnedWindowingMode()) {
            return getAppAnimationLayer();
        }
        return getStack().getSurfaceControl();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldAnimate(int transit) {
        boolean isSplitScreenPrimary = getWindowingMode() == 3;
        boolean allowSplitScreenPrimaryAnimation = transit != 13;
        RecentsAnimationController controller = this.mWmService.getRecentsAnimationController();
        if (controller == null || !controller.isAnimatingTask(getTask()) || !controller.shouldCancelWithDeferredScreenshot()) {
            return !isSplitScreenPrimary || allowSplitScreenPrimaryAnimation;
        }
        return false;
    }

    private SurfaceControl createAnimationBoundsLayer(SurfaceControl.Transaction t) {
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.i(TAG, "Creating animation bounds layer");
        }
        SurfaceControl.Builder parent = makeAnimationLeash().setParent(getAnimationLeashParent());
        SurfaceControl boundsLayer = parent.setName(getSurfaceControl() + " - animation-bounds").build();
        t.show(boundsLayer);
        return boundsLayer;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public Rect getDisplayedBounds() {
        Task task = getTask();
        if (task != null) {
            Rect overrideDisplayedBounds = task.getOverrideDisplayedBounds();
            if (!overrideDisplayedBounds.isEmpty()) {
                return overrideDisplayedBounds;
            }
        }
        return getBounds();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Rect getAnimationBounds(int appStackClipMode) {
        if (appStackClipMode == 1 && getStack() != null) {
            return getStack().getBounds();
        }
        if (getTask() == null) {
            return getBounds();
        }
        Rect visibleRect = new Rect(getTask().getBounds());
        float initScale = getStack().mHwStackScale;
        int left = visibleRect.left;
        int top = visibleRect.top;
        visibleRect.scale(initScale);
        visibleRect.offsetTo(left, top);
        return visibleRect;
    }

    /* access modifiers changed from: package-private */
    public boolean isTransitionAnimationCanceled(int transit) {
        if (Float.compare(this.mWmService.getTransitionAnimationScaleLocked(), 0.0f) != 0) {
            return false;
        }
        if (transit == 7 || transit == 6 || transit == 8 || transit == 9) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean applyAnimationLocked(WindowManager.LayoutParams lp, int transit, boolean enter, boolean isVoiceInteraction) {
        boolean z;
        AnimationAdapter adapter;
        IHwAppWindowTokenEx iHwAppWindowTokenEx;
        float animationRadius;
        float animationRadius2;
        WindowState window;
        WindowState win;
        if (this.mWmService.mDisableTransitionAnimation || !shouldAnimate(transit) || isTransitionAnimationCanceled(transit)) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: transition animation is disabled or skipped. atoken=" + this);
            }
            cancelAnimation();
            return false;
        } else if (!this.mWmService.mHwWMSEx.isNeedLandAni() || this.mWmService.mAnimator.getScreenRotationAnimationLocked(0) == null) {
            ActivityStack activityStack = this.mActivityRecord.getActivityStack();
            if (activityStack == null || !activityStack.isPendingShow()) {
                Trace.traceBegin(32, "AWT#applyAnimationLocked");
                if (okToAnimate()) {
                    AnimationAdapter thumbnailAdapter = null;
                    int appStackClipMode = getDisplayContent().mAppTransition.getAppStackClipMode();
                    this.mTmpRect.set(getAnimationBounds(appStackClipMode));
                    this.mTmpPoint.set(this.mTmpRect.left, this.mTmpRect.top);
                    TaskStack stack = getStack();
                    if (stack != null) {
                        DisplayContent displayContent = stack.getDisplayContent();
                        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayContent.mDisplayId) && (win = findMainWindow()) != null) {
                            this.mTmpRect.set(win.getDisplayFrameLw());
                        }
                        if (HwFoldScreenState.isFoldScreenDevice() && this.mWmService.isInSubFoldScaleMode() && inMultiWindowMode() && !HwPCUtils.isValidExtDisplayId(displayContent.mDisplayId) && !this.mWmService.mAtmService.mHwATMSEx.isVirtualDisplayId(displayContent.mDisplayId, "padCast")) {
                            this.mTmpPoint.set(stack.mLastSurfacePosition.x, stack.mLastSurfacePosition.y);
                        }
                    }
                    this.mTmpRect.offsetTo(0, 0);
                    Rect rect = null;
                    if (!(transit != 12 || this.mDisplayContent == null || this.mDisplayContent.mOpeningApps == null || !this.mDisplayContent.mOpeningApps.contains(this) || (window = findMainWindow()) == null || window.mWinAnimator == null)) {
                        window.mWinAnimator.setWindowIconInfo(0, 0, 0, null);
                    }
                    if (this.mHasIconLayer) {
                        this.mWmService.mHwWMSEx.clearHwFreeWindowFloatIconLayer(this);
                    }
                    boolean isChanging = AppTransition.isChangeTransit(transit) && enter && getDisplayContent().mChangingApps.contains(this);
                    if (getDisplayContent().mAppTransition.getRemoteAnimationController() != null && !this.mSurfaceAnimator.isAnimationStartDelayed()) {
                        RemoteAnimationController remoteAnimationController = getDisplayContent().mAppTransition.getRemoteAnimationController();
                        Point point = this.mTmpPoint;
                        Rect rect2 = this.mTmpRect;
                        if (isChanging) {
                            rect = this.mTransitStartRect;
                        }
                        RemoteAnimationController.RemoteAnimationRecord adapters = remoteAnimationController.createRemoteAnimationRecord(this, point, rect2, rect);
                        adapter = adapters.mAdapter;
                        thumbnailAdapter = adapters.mThumbnailAdapter;
                        z = true;
                    } else if (isChanging) {
                        float durationScale = this.mWmService.getTransitionAnimationScaleLocked();
                        this.mTmpRect.offsetTo(this.mTmpPoint.x, this.mTmpPoint.y);
                        adapter = new LocalAnimationAdapter(this.mWmService.mHwWMSEx.getChangingAnimationSpec(this.mTransitStartRect, this.mTmpRect, getDisplayContent().getDisplayInfo(), durationScale, true, false), this.mWmService.mSurfaceAnimationRunner);
                        if (this.mThumbnail != null) {
                            thumbnailAdapter = new LocalAnimationAdapter(this.mWmService.mHwWMSEx.getChangingAnimationSpec(this.mTransitStartRect, this.mTmpRect, getDisplayContent().getDisplayInfo(), durationScale, true, true), this.mWmService.mSurfaceAnimationRunner);
                        }
                        this.mTransit = transit;
                        this.mTransitFlags = getDisplayContent().mAppTransition.getTransitFlags();
                        z = true;
                    } else {
                        this.mNeedsAnimationBoundsLayer = appStackClipMode == 0;
                        Animation a = loadAnimation(lp, transit, enter, isVoiceInteraction);
                        if (a != null) {
                            int lazyMode = this.mWmService.getLazyMode();
                            WindowState window2 = findMainWindow();
                            Rect setClipRectBounds = new Rect();
                            boolean isLazyMode = (lazyMode == 0 || inMultiWindowMode() || window2 == null) ? false : true;
                            if (isLazyMode || (this.mTmpPoint.x == 0 && this.mTmpPoint.y == 0 && window2 != null && inHwMagicWindowingMode())) {
                                setClipRectBounds = window2.getFrameLw();
                            } else {
                                setClipRectBounds.setEmpty();
                            }
                            HwFrameworkFactory.getHwAnimationManager().setAnimationBounds(a, new Point(setClipRectBounds.left, setClipRectBounds.top));
                            if (!inMultiWindowMode()) {
                                animationRadius = getDisplayContent().getWindowCornerRadius();
                            } else {
                                animationRadius = 0.0f;
                            }
                            this.mLayoutParams = lp;
                            if (isCrossAppTransit(transit)) {
                                a.setHasRoundedCorners(true);
                                animationRadius2 = this.mWmService.mHwWMSEx.getCrossAppTransitAnimRoundCornerRadius(this);
                            } else {
                                animationRadius2 = animationRadius;
                            }
                            Rect contractedAnimationBounds = getContractedAnimationBounds(transit, isLazyMode, setClipRectBounds, false);
                            adjustAnimationBoundsForIme(contractedAnimationBounds, window2);
                            WindowAnimationSpec windowAnimationSpec = new WindowAnimationSpec(a, this.mTmpPoint, contractedAnimationBounds, getDisplayContent().mAppTransition.canSkipFirstFrame(), appStackClipMode, true, animationRadius2);
                            if (isCrossAppTransit(transit)) {
                                z = true;
                                this.mWmService.mHwWMSEx.setCrossAppTransitDynamicRoundCorner(windowAnimationSpec, enter, animationRadius2, isLazyMode, this);
                            } else {
                                z = true;
                            }
                            this.mWmService.mHwWMSEx.setDynamicCornerRadiusInfo(this, transit, windowAnimationSpec);
                            this.mWmService.mHwWMSEx.setClipRectDynamicRoundCornerIfNeeded(windowAnimationSpec, this, transit, enter);
                            adapter = new LocalAnimationAdapter(windowAnimationSpec, this.mWmService.mSurfaceAnimationRunner);
                            if (a.getZAdjustment() == z) {
                                this.mNeedsZBoost = z;
                            }
                            this.mTransit = transit;
                            this.mTransitFlags = getDisplayContent().mAppTransition.getTransitFlags();
                        } else {
                            z = true;
                            adapter = null;
                            if (this.mTransitChangeLeash != null) {
                                clearChangeLeash(getPendingTransaction(), true);
                            }
                        }
                    }
                    if (adapter != null) {
                        if (enter && (iHwAppWindowTokenEx = this.mHwAppWinTokenEx) != null) {
                            iHwAppWindowTokenEx.cancelInputMethodRetractAnimation(this.mWmService.mRoot.getCurrentInputMethodWindow());
                        }
                        startAnimation(getPendingTransaction(), adapter, (((enter || !this.mIsExiting || !isReallyAnimating()) ? false : z) || isVisible()) ? false : z);
                        if (adapter.getShowWallpaper()) {
                            this.mDisplayContent.pendingLayoutChanges |= 4;
                        }
                        if (isCrossAppTransit(transit) && adapter.getShowWallpaper()) {
                            startWallpaperAnimaiton(lp, 34209854);
                        }
                        if (thumbnailAdapter != null) {
                            this.mThumbnail.startAnimation(getPendingTransaction(), thumbnailAdapter, isVisible() ^ z);
                        }
                    }
                } else {
                    cancelAnimation();
                }
                Trace.traceEnd(32);
                return isReallyAnimating();
            }
            Slog.i(TAG, "disable applyAnimationLocked when stack isPendingShow");
            return false;
        } else {
            Slog.i(TAG, "disable applyAnimationLocked when land startup");
            return false;
        }
    }

    private void adjustAnimationBoundsForIme(Rect animationBounds, WindowState window) {
        if (window != null && animationBounds != null && this.mWmService.mHwWMSEx.isHwFreeWindowFloatDrawBackScene(this, this.mTransit, getDisplayContent())) {
            Rect visibleBounds = window.getContainingFrame();
            Rect realBounds = window.getBounds();
            animationBounds.offset(visibleBounds.left - realBounds.left, visibleBounds.top - realBounds.top);
        }
    }

    /* JADX INFO: Multiple debug info for r8v35 int: [D('left' int), D('surfaceInsets' android.graphics.Rect)] */
    private Animation loadAnimation(WindowManager.LayoutParams lp, int transit, boolean enter, boolean isVoiceInteraction) {
        int height;
        Rect surfaceInsets;
        boolean enter2;
        String str;
        int containingWidth;
        int width;
        int i;
        DisplayContent displayContent = getTask().getDisplayContent();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        int width2 = displayInfo.appWidth;
        int height2 = displayInfo.appHeight;
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "applyAnimation: atoken=" + this);
        }
        WindowState win = findMainWindow();
        if ((enter && transit == 13 && win != null && win.toString().contains("splitscreen.SplitScreenAppActivity")) || CoordinationModeUtils.getInstance(this.mWmService.mContext).isExitingCoordinationMode()) {
            return null;
        }
        boolean freeform = false;
        boolean inSecondaryCoordination = win != null && win.inCoordinationSecondaryWindowingMode();
        if (enter || !inSecondaryCoordination) {
            Rect frame = new Rect(0, 0, width2, height2);
            Rect displayFrame = new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
            Rect insets = new Rect();
            Rect stableInsets = new Rect();
            if (win != null && win.inFreeformWindowingMode()) {
                freeform = true;
            }
            if (win != null) {
                if (freeform || (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayContent.mDisplayId))) {
                    frame.set(win.getFrameLw());
                } else if (win.isLetterboxedAppWindow()) {
                    frame.set(getTask().getBounds());
                } else if (win.isDockedResizing() || inHwMagicWindowingMode()) {
                    frame.set(getTask().getParent().getBounds());
                } else {
                    frame.set(win.getContainingFrame());
                }
                Rect surfaceInsets2 = win.getAttrs().surfaceInsets;
                int left = frame.left;
                int top = frame.top;
                height = height2;
                frame.scale(getStack().mHwStackScale);
                frame.offsetTo(left, top);
                win.getContentInsets(insets);
                win.getStableInsets(stableInsets);
                surfaceInsets = surfaceInsets2;
            } else {
                height = height2;
                surfaceInsets = null;
            }
            if (this.mLaunchTaskBehind) {
                enter2 = false;
            } else {
                enter2 = enter;
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.d(TAG, "Loading animation for app transition. transit=" + AppTransition.appTransitionToString(transit) + " enter=" + enter2 + " frame=" + frame + " insets=" + insets + " surfaceInsets=" + surfaceInsets);
            }
            Configuration displayConfig = displayContent.getConfiguration();
            Animation a = getDisplayContent().mAppTransition.loadAnimation(lp, transit, enter2, displayConfig.uiMode, displayConfig.orientation, frame, displayFrame, insets, surfaceInsets, stableInsets, isVoiceInteraction, freeform, getTask().mTaskId);
            if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayContent.mDisplayId)) {
                a = getDisplayContent().mAppTransition.loadAnimationRes(lp, enter2 ? 17432576 : 17432577);
                a.setDuration(300);
            } else if (freeform && HwFreeFormUtils.isFreeFormEnable()) {
                AppTransition appTransition = getDisplayContent().mAppTransition;
                if (enter2) {
                    i = 34209856;
                } else {
                    i = 34209869;
                }
                a = appTransition.loadAnimationRes(lp, i);
                a.setDuration(50);
            } else if (inHwFreeFormWindowingMode() && !enter2 && isFreeformExit(transit, win)) {
                a = getDisplayContent().mAppTransition.loadAnimationRes(lp, 34209871);
            } else if (inHwFreeFormWindowingMode() && transit == 10 && enter2) {
                a = getDisplayContent().mAppTransition.loadAnimationRes(lp, 34209874);
            }
            int height3 = height;
            Animation a2 = this.mWmService.mHwWMSEx.loadTvMultiWindowAnimation(this.mWmService.mHwWMSEx.createCardClipRevealAnimation(this.mWmService.mHwWMSEx.loadHwFreeWindowFloatOpenSceneAnimation(this.mWmService.mHwWMSEx.loadHwFreeWindowFloatDrawBackAnimation(this.mWmService.mHwWMSEx.loadHwAssociateFullScreenBackgroundAnimation(this.mWmService.mHwWMSEx.reloadHwSplitScreenOpeningAnimation(this.mWmService.mHwWMSEx.loadAppWindowExitToLauncherAnimation(this.mWmService.mHwWMSEx.getMagicWindowAnimation(a, enter2, transit, this, frame), transit, frame, this), this, this.mDisplayContent != null ? this.mDisplayContent.mOpeningApps : null, enter2), transit, this), transit, this), transit, this), enter2, transit, this, frame), enter2, transit, this, frame);
            if (a2 != null) {
                if (WindowManagerDebugConfig.DEBUG_ANIM) {
                    str = TAG;
                    WindowManagerService.logWithStack(str, "Loaded animation " + a2 + " for " + this);
                } else {
                    str = TAG;
                }
                int containingWidth2 = frame.width();
                int containingHeight = frame.height();
                if (shouldContractBoundsForAnimation(transit)) {
                    int rotation = this.mWmService.getDefaultDisplayRotation();
                    int sideSize = HwDisplaySizeUtil.getInstance(this.mWmService).getSafeSideWidth();
                    if (rotation == 1 || rotation == 3) {
                        containingHeight += sideSize;
                        height3 -= sideSize;
                        width = width2;
                    } else {
                        containingWidth2 += sideSize;
                        width = width2 - sideSize;
                    }
                    a2.initialize(containingWidth2, containingHeight, width, height3);
                } else {
                    int winMode = getWindowingMode();
                    int rotation2 = this.mWmService.getDefaultDisplayRotation();
                    boolean isNotchSwitchOpen = this.mWmService.getPolicy().isNotchDisplayDisabled();
                    if ((rotation2 == 1 || rotation2 == 3) && isNotchSwitchOpen && winMode == 1) {
                        containingWidth = displayFrame.width();
                        containingHeight = displayFrame.height();
                    } else {
                        containingWidth = containingWidth2;
                    }
                    a2.initialize(containingWidth, containingHeight, width2, height3);
                }
                if (win == null || (!win.toString().contains(WIN_PEOPLEACTIVITY) && !win.toString().contains(WIN_DIALTACTSACTIVITY))) {
                    a2.scaleCurrentDuration(this.mWmService.getTransitionAnimationScaleLocked());
                } else {
                    Slog.v(str, "Reduce startup animation time to 70% for contact and dialer");
                    a2.scaleCurrentDuration(this.mWmService.getTransitionAnimationScaleLocked() * 0.7f);
                }
            }
            return a2;
        }
        Slog.v(TAG, "skip applyAnimation: atoken=" + this);
        return null;
    }

    @Override // com.android.server.wm.SurfaceAnimator.Animatable
    public boolean shouldDeferAnimationFinish(Runnable endDeferFinishCallback) {
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry = this.mAnimatingAppWindowTokenRegistry;
        return animatingAppWindowTokenRegistry != null && animatingAppWindowTokenRegistry.notifyAboutToFinish(this, endDeferFinishCallback);
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public void onAnimationLeashLost(SurfaceControl.Transaction t) {
        super.onAnimationLeashLost(t);
        SurfaceControl surfaceControl = this.mAnimationBoundsLayer;
        if (surfaceControl != null) {
            t.remove(surfaceControl);
            this.mAnimationBoundsLayer = null;
        }
        AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry = this.mAnimatingAppWindowTokenRegistry;
        if (animatingAppWindowTokenRegistry != null) {
            animatingAppWindowTokenRegistry.notifyFinished(this);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowContainer
    public void setLayer(SurfaceControl.Transaction t, int layer) {
        if (!this.mSurfaceAnimator.hasLeash()) {
            t.setLayer(this.mSurfaceControl, layer);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowContainer
    public void setRelativeLayer(SurfaceControl.Transaction t, SurfaceControl relativeTo, int layer) {
        if (!this.mSurfaceAnimator.hasLeash()) {
            t.setRelativeLayer(this.mSurfaceControl, relativeTo, layer);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowContainer
    public void reparentSurfaceControl(SurfaceControl.Transaction t, SurfaceControl newParent) {
        if (!this.mSurfaceAnimator.hasLeash()) {
            t.reparent(this.mSurfaceControl, newParent);
        }
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
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
        if (!this.mNeedsAnimationBoundsLayer) {
            leash.setLayer(layer);
        }
        getDisplayContent().assignStackOrdering();
        SurfaceControl surfaceControl = this.mTransitChangeLeash;
        if (leash != surfaceControl) {
            if (surfaceControl != null) {
                clearChangeLeash(t, false);
            }
            AnimatingAppWindowTokenRegistry animatingAppWindowTokenRegistry = this.mAnimatingAppWindowTokenRegistry;
            if (animatingAppWindowTokenRegistry != null) {
                animatingAppWindowTokenRegistry.notifyStarting(this);
            }
            if (this.mNeedsAnimationBoundsLayer) {
                this.mTmpRect.setEmpty();
                Task task = getTask();
                if (getDisplayContent().mAppTransitionController.isTransitWithinTask(getTransit(), task)) {
                    task.getBounds(this.mTmpRect);
                } else {
                    TaskStack stack = getStack();
                    if (stack != null) {
                        stack.getBounds(this.mTmpRect);
                    } else {
                        return;
                    }
                }
                this.mAnimationBoundsLayer = createAnimationBoundsLayer(t);
                ActivityRecord activityRecord = this.mActivityRecord;
                boolean isHwFreeWindowFLoatToWindowScene = true;
                boolean isSplitSubActivity = activityRecord != null && activityRecord.isShowInSplit() && !this.mActivityRecord.isSplitBaseActivity() && !isTopVisibleInFIFOList(this.mActivityRecord);
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.d(TAG, "onAnimationLeashCreated mActivityRecord:" + this.mActivityRecord + ", XX isSplitSubActivity:" + isSplitSubActivity, new Exception());
                }
                if (isSplitSubActivity && !this.hasAddedSplitShadow) {
                    this.hasAddedSplitShadow = true;
                    SurfaceControl shadowSurfaceControl = makeChildSurface(null).setColorLayer().setParent(leash).setName("Shadow Layer for - " + getName()).build();
                    t.setLayer(shadowSurfaceControl, Integer.MIN_VALUE);
                    t.show(shadowSurfaceControl);
                }
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    printFIFOList();
                }
                WindowState window = findMainWindow();
                if (!this.mWmService.mHwWMSEx.isHwFreeWindowFloatDrawBackScene(this, this.mTransit, getDisplayContent()) && !this.mWmService.mHwWMSEx.isHwFreeWindowFloatOpenScene(this, this.mTransit, getDisplayContent())) {
                    isHwFreeWindowFLoatToWindowScene = false;
                }
                Rect rect = getContractedAnimationBounds(this.mTransit, false, null, isHwFreeWindowFLoatToWindowScene);
                cropRectForSplitMode(rect, window);
                Rect destRect = new Rect();
                destRect.set(rect);
                if (!isHwFreeWindowFLoatToWindowScene) {
                    destRect.scale(getStack().mHwStackScale);
                }
                destRect.offsetTo(rect.left, rect.top);
                t.setWindowCrop(this.mAnimationBoundsLayer, destRect);
                int lazyMode = this.mWmService.getLazyMode();
                if (window != null && window.inHwFreeFormWindowingMode() && lazyMode == 0) {
                    float radius = this.mWmService.mAtmService.mHwATMSEx.getHwMultiWinCornerRadius(getWindowingMode()) * getStack().mHwStackScale;
                    if (!isHwFreeWindowFLoatToWindowScene) {
                        t.setCornerRadius(this.mAnimationBoundsLayer, radius);
                    }
                }
                t.setLayer(this.mAnimationBoundsLayer, layer);
                t.reparent(leash, this.mAnimationBoundsLayer);
            } else if (this.mIsNeedChangeAnimationCorner && !this.mIsTvUsedPreSnapshot) {
                t.setCornerRadius(leash, this.mWmService.mAtmService.mHwATMSEx.getHwMultiWinCornerRadius(WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT));
                this.mIsNeedChangeAnimationCorner = false;
            }
        }
    }

    private void cropRectForSplitMode(Rect rect, WindowState mainWindow) {
        if (isNeedCropRectForSplitMode()) {
            if (rect == null) {
                Slog.w(TAG, "cropRectForSplitMode rect null");
            } else if (mainWindow == null) {
                Slog.w(TAG, "cropRectForSplitMode mainWindow null");
            } else {
                Rect frameRect = mainWindow.getFrameLw();
                if (frameRect == null) {
                    Slog.w(TAG, "cropRectForSplitMode frameRect null");
                    return;
                }
                Slog.i(TAG, "cropRectForSplitMode frameRect " + frameRect);
                rect.intersect(frameRect);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void showAllWindowsLocked() {
        forAllWindows((Consumer<WindowState>) $$Lambda$AppWindowToken$jUUD3GIacKfmiVanGs_Dyy3FFqI.INSTANCE, false);
    }

    static /* synthetic */ void lambda$showAllWindowsLocked$5(WindowState windowState) {
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG, "performing show on: " + windowState);
        }
        windowState.performShowLocked();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowContainer
    public void onAnimationFinished() {
        super.onAnimationFinished();
        Trace.traceBegin(32, "AWT#onAnimationFinished");
        this.mTransit = -1;
        this.mTransitFlags = 0;
        this.mNeedsZBoost = false;
        this.mNeedsAnimationBoundsLayer = false;
        this.mIsNeedChangeAnimationCorner = false;
        if (this.mHwAppWinTokenEx != null && !this.appPackageName.contains(DisplayPolicy.LAUNCHER_PACKAGE_NAME)) {
            this.mHwAppWinTokenEx.cancelInputMethodRetractAnimation(this.mWmService.mRoot.getCurrentInputMethodWindow());
        }
        setAppLayoutChanges(12, "AppWindowToken");
        clearThumbnail();
        Slog.i(TAG, "Animation done in " + this + " isHidden:" + isHidden() + " hiddenRequested:" + this.hiddenRequested);
        setClientHidden(isHidden() && this.hiddenRequested);
        getDisplayContent().computeImeTargetIfNeeded(this);
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "Animation done in " + this + ": reportedVisible=" + this.reportedVisible + " okToDisplay=" + okToDisplay() + " okToAnimate=" + okToAnimate() + " startingDisplayed=" + this.startingDisplayed);
        }
        AppWindowThumbnail appWindowThumbnail = this.mThumbnail;
        if (appWindowThumbnail != null) {
            appWindowThumbnail.destroy();
            this.mThumbnail = null;
        }
        new ArrayList<>(this.mChildren).forEach($$Lambda$01bPtngJg5AqEoOWfW3rWfV7MH4.INSTANCE);
        if (this.mHasIconLayer) {
            this.mWmService.mHwWMSEx.clearHwFreeWindowFloatIconLayer(this);
        }
        this.hasAddedSplitShadow = false;
        if (getStack() != null && getStack().inHwFreeFormMoveBackOrCloseState()) {
            getStack().setInHwFreeFormMoveBackOrCloseState(false);
        }
        getDisplayContent().mAppTransition.notifyAppTransitionFinishedLocked(this.token);
        scheduleAnimation();
        this.mActivityRecord.onAnimationFinished();
        Trace.traceEnd(32);
        if (this.mIsTvUsedPreSnapshot) {
            int taskId = getWindowingMode();
            if (taskId == 108 || taskId == 106) {
                this.mWmService.mAtmService.mHwATMSEx.notifyWindowStateChange("first", "onAnimationFinished", getWindowingMode(), getTask().getBounds(), 0.0f, getTask().mTaskId, "", 0, null, 0.0f, -1);
            }
            if (taskId == 107 || taskId == 1) {
                this.mWmService.mAtmService.mHwATMSEx.notifyWindowStateChange("second", "onAnimationFinished", getWindowingMode(), getTask().getBounds(), 0.0f, getTask().mTaskId, "", 0, null, 0.0f, -1);
            }
            Slog.i(TAG, "notifyWindowStateChange: onAnimationFinished");
            this.mIsTvUsedPreSnapshot = false;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean isAppAnimating() {
        return isSelfAnimating();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean isSelfAnimating() {
        return isWaitingForTransitionStart() || isReallyAnimating();
    }

    private boolean isReallyAnimating() {
        return super.isSelfAnimating();
    }

    private void clearChangeLeash(SurfaceControl.Transaction t, boolean cancel) {
        if (this.mTransitChangeLeash != null) {
            if (cancel) {
                clearThumbnail();
                SurfaceControl sc = getSurfaceControl();
                if (!(getParentSurfaceControl() == null || sc == null)) {
                    t.reparent(sc, getParentSurfaceControl());
                }
            }
            t.hide(this.mTransitChangeLeash);
            t.remove(this.mTransitChangeLeash);
            this.mTransitChangeLeash = null;
            if (cancel) {
                onAnimationLeashLost(t);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void cancelAnimation() {
        cancelAnimationOnly();
        clearThumbnail();
        clearChangeLeash(getPendingTransaction(), true);
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimationOnly() {
        super.cancelAnimation();
    }

    /* access modifiers changed from: package-private */
    public boolean isWaitingForTransitionStart() {
        return getDisplayContent().mAppTransition.isTransitionSet() && (getDisplayContent().mOpeningApps.contains(this) || getDisplayContent().mClosingApps.contains(this) || getDisplayContent().mChangingApps.contains(this));
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
            GraphicBuffer thumbnailHeader = getDisplayContent().mAppTransition.getAppTransitionThumbnailHeader(taskId);
            if (thumbnailHeader != null) {
                clearThumbnail();
                this.mThumbnail = new AppWindowThumbnail(getPendingTransaction(), this, thumbnailHeader);
                this.mThumbnail.startAnimation(getPendingTransaction(), loadThumbnailAnimation(thumbnailHeader));
            } else if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.d(TAG, "No thumbnail header bitmap for: " + taskId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void attachCrossProfileAppsThumbnailAnimation() {
        int thumbnailDrawableRes;
        if (isReallyAnimating()) {
            clearThumbnail();
            WindowState win = findMainWindow();
            if (win != null) {
                Rect frame = win.getFrameLw();
                if (getTask().mUserId == this.mWmService.mCurrentUserId) {
                    thumbnailDrawableRes = 17302284;
                } else {
                    thumbnailDrawableRes = 17302374;
                }
                GraphicBuffer thumbnail = getDisplayContent().mAppTransition.createCrossProfileAppsThumbnail(thumbnailDrawableRes, frame);
                if (thumbnail != null) {
                    this.mThumbnail = new AppWindowThumbnail(getPendingTransaction(), this, thumbnail);
                    this.mThumbnail.startAnimation(getPendingTransaction(), getDisplayContent().mAppTransition.createCrossProfileAppsThumbnailAnimationLocked(win.getFrameLw()), new Point(frame.left, frame.top));
                }
            }
        }
    }

    private Animation loadThumbnailAnimation(GraphicBuffer thumbnailHeader) {
        Rect appRect;
        DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
        WindowState win = findMainWindow();
        if (win != null) {
            appRect = win.getContentFrameLw();
        } else {
            appRect = new Rect(0, 0, displayInfo.appWidth, displayInfo.appHeight);
        }
        Rect insets = win != null ? win.getContentInsets() : null;
        Configuration displayConfig = this.mDisplayContent.getConfiguration();
        return getDisplayContent().mAppTransition.createThumbnailAspectScaleAnimationLocked(appRect, insets, thumbnailHeader, getTask().mTaskId, displayConfig.uiMode, displayConfig.orientation);
    }

    private void clearThumbnail() {
        AppWindowThumbnail appWindowThumbnail = this.mThumbnail;
        if (appWindowThumbnail != null) {
            appWindowThumbnail.destroy();
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
    @Override // com.android.server.wm.WindowToken, com.android.server.wm.WindowContainer
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        String str;
        super.dump(pw, prefix, dumpAll);
        if (this.appToken != null) {
            pw.println(prefix + "app=true mVoiceInteraction=" + this.mVoiceInteraction);
        }
        pw.println(prefix + "component=" + this.mActivityComponent.flattenToShortString());
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
            str = "";
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
        if (this.mStartingData != null || this.removed || this.firstWindowDrawn || this.mIsExiting) {
            pw.print(prefix);
            pw.print("startingData=");
            pw.print(this.mStartingData);
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
        if (!(this.mSizeCompatScale == 1.0f && this.mSizeCompatBounds == null)) {
            pw.println(prefix + "mSizeCompatScale=" + this.mSizeCompatScale + " mSizeCompatBounds=" + this.mSizeCompatBounds);
        }
        if (this.mRemovingFromDisplay) {
            pw.println(prefix + "mRemovingFromDisplay=" + this.mRemovingFromDisplay);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowToken
    public void setHidden(boolean hidden) {
        super.setHidden(hidden);
        if (hidden) {
            this.mDisplayContent.mPinnedStackControllerLocked.resetReentrySnapFraction(this);
        }
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void prepareSurfaces() {
        boolean show = !isHidden() || super.isSelfAnimating();
        if (this.mSurfaceControl != null) {
            if (show && !this.mLastSurfaceShowing) {
                getPendingTransaction().show(this.mSurfaceControl);
            } else if (!show && this.mLastSurfaceShowing) {
                getPendingTransaction().hide(this.mSurfaceControl);
            }
        }
        AppWindowThumbnail appWindowThumbnail = this.mThumbnail;
        if (appWindowThumbnail != null) {
            appWindowThumbnail.setShowing(getPendingTransaction(), show);
        }
        this.mLastSurfaceShowing = show;
        super.prepareSurfaces();
    }

    /* access modifiers changed from: package-private */
    public void setAlpha(SurfaceControl.Transaction t, float alpha) {
        if (t == null || this.mSurfaceControl == null) {
            Slog.w(TAG, "setAlpha return, t or mSurfaceControl is null");
            return;
        }
        t.setAlpha(this.mSurfaceControl, alpha);
        t.apply();
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
    @Override // com.android.server.wm.WindowContainer
    public boolean needsZBoost() {
        return this.mNeedsZBoost || super.needsZBoost();
    }

    @Override // com.android.server.wm.WindowToken, com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        if (logLevel != 2 || isVisible()) {
            long token = proto.start(fieldId);
            writeNameToProto(proto, 1138166333441L);
            super.writeToProto(proto, 1146756268034L, logLevel);
            proto.write(1133871366147L, this.mLastSurfaceShowing);
            proto.write(1133871366148L, isWaitingForTransitionStart());
            proto.write(1133871366149L, isReallyAnimating());
            AppWindowThumbnail appWindowThumbnail = this.mThumbnail;
            if (appWindowThumbnail != null) {
                appWindowThumbnail.writeToProto(proto, 1146756268038L);
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
            WindowState windowState = this.startingWindow;
            if (windowState != null) {
                windowState.writeIdentifierToProto(proto, 1146756268051L);
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
    }

    /* access modifiers changed from: package-private */
    public void writeNameToProto(ProtoOutputStream proto, long fieldId) {
        IApplicationToken iApplicationToken = this.appToken;
        if (iApplicationToken != null) {
            try {
                proto.write(fieldId, iApplicationToken.getName());
            } catch (RemoteException e) {
                Slog.e(TAG, e.toString());
            }
        }
    }

    @Override // com.android.server.wm.WindowToken, java.lang.Object
    public String toString() {
        if (this.stringName == null) {
            this.stringName = "AppWindowToken{" + Integer.toHexString(System.identityHashCode(this)) + " token=" + this.token + '}';
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.stringName);
        sb.append(this.mIsExiting ? " mIsExiting=" : "");
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public Rect getLetterboxInsets() {
        Letterbox letterbox = this.mLetterbox;
        if (letterbox != null) {
            return letterbox.getInsets();
        }
        return new Rect();
    }

    /* access modifiers changed from: package-private */
    public void getLetterboxInnerBounds(Rect outBounds) {
        if (this.mLetterbox != null) {
            this.mWmService.getLazyMode();
            outBounds.set(this.mLetterbox.getInnerFrame());
            return;
        }
        outBounds.setEmpty();
    }

    /* access modifiers changed from: package-private */
    public boolean isLetterboxOverlappingWith(Rect rect) {
        Letterbox letterbox = this.mLetterbox;
        return letterbox != null && letterbox.isOverlappingWith(rect);
    }

    /* access modifiers changed from: package-private */
    public void setWillCloseOrEnterPip(boolean willCloseOrEnterPip) {
        this.mWillCloseOrEnterPip = willCloseOrEnterPip;
    }

    /* access modifiers changed from: package-private */
    public boolean isClosingOrEnteringPip() {
        return (isAnimating() && this.hiddenRequested) || this.mWillCloseOrEnterPip;
    }

    /* access modifiers changed from: package-private */
    public boolean canShowWindows() {
        if (!IS_EMUI_LITE) {
            return this.allDrawn;
        }
        return this.allDrawn && (!isReallyAnimating() || !hasNonDefaultColorWindow());
    }

    static /* synthetic */ boolean lambda$hasHwSecureWindowOnScreen$6(WindowState w) {
        return ((w.mAttrs.flags & 8192) == 0 && (w.mAttrs.hwFlags & 4096) == 0 && (w.mAttrs.hwFlags & 8192) == 0) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean hasHwSecureWindowOnScreen() {
        WindowState win = getWindow($$Lambda$AppWindowToken$eeumM6G_pO5oMG_AJI6x6c6QlE.INSTANCE);
        Slog.i(TAG, "secure window is " + win);
        return win != null;
    }

    private boolean hasNonDefaultColorWindow() {
        return forAllWindows((ToBooleanFunction<WindowState>) $$Lambda$AppWindowToken$fPUApbLk_vYcjY_mIHRDEOCqbZU.INSTANCE, true);
    }

    static /* synthetic */ boolean lambda$hasNonDefaultColorWindow$7(WindowState ws) {
        return ws.mAttrs.getColorMode() != 0;
    }

    public int getHwGestureNavOptions() {
        return this.mHwGestureNavOptions;
    }

    private void updateColorTransform() {
        if (this.mSurfaceControl != null && this.mLastAppSaturationInfo != null) {
            getPendingTransaction().setColorTransform(this.mSurfaceControl, this.mLastAppSaturationInfo.mMatrix, this.mLastAppSaturationInfo.mTranslation);
            this.mWmService.scheduleAnimationLocked();
        }
    }

    /* access modifiers changed from: private */
    public static class AppSaturationInfo {
        float[] mMatrix;
        float[] mTranslation;

        private AppSaturationInfo() {
            this.mMatrix = new float[9];
            this.mTranslation = new float[3];
        }

        /* access modifiers changed from: package-private */
        public void setSaturation(float[] matrix, float[] translation) {
            float[] fArr = this.mMatrix;
            System.arraycopy(matrix, 0, fArr, 0, fArr.length);
            float[] fArr2 = this.mTranslation;
            System.arraycopy(translation, 0, fArr2, 0, fArr2.length);
        }
    }

    private boolean isContainedOnlyOneVisibleWindow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) this.mChildren.get(i);
            if (!((win.mAttrs.flags & 2) == 0 && (win.mAttrs.flags & 4) == 0)) {
                return false;
            }
        }
        return true;
    }

    private Rect getContractedAnimationBounds(int transit, boolean isLazyMode, Rect lazyModeRect, boolean isHwFreeDomFloatToWindowScene) {
        Rect outBounds = new Rect(this.mTmpRect);
        if (isHwFreeDomFloatToWindowScene) {
            outBounds = new Rect(getStack().getParent().getBounds());
        }
        if (isCrossAppTransit(transit)) {
            return this.mWmService.mHwWMSEx.getCrossAppTransitAnimBounds(outBounds, lazyModeRect, isLazyMode, this);
        }
        if (!shouldContractBoundsForAnimation(transit)) {
            return outBounds;
        }
        int rotation = this.mWmService.getDefaultDisplayRotation();
        int sideSize = HwDisplaySizeUtil.getInstance(this.mWmService).getSafeSideWidth();
        if (rotation == 1 || rotation == 3) {
            outBounds.top += sideSize;
            outBounds.bottom -= sideSize;
        } else {
            outBounds.left += sideSize;
            outBounds.right -= sideSize;
        }
        return outBounds;
    }

    private boolean shouldContractBoundsForAnimation(int transit) {
        WindowState win;
        DisplayPolicy displayPolicy;
        IHwDisplayPolicyEx hwDisplayPolicyEx;
        if (!this.mNeedsAnimationBoundsLayer) {
            return false;
        }
        if ((transit != 6 && transit != 7 && transit != 8 && transit != 9) || !HwDisplaySizeUtil.hasSideInScreen()) {
            return false;
        }
        Task task = getTask();
        if ((task != null && ((HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(task.getDisplayContent().mDisplayId)) || task.inHwFreeFormWindowingMode())) || (win = findMainWindow()) == null || (displayPolicy = this.mWmService.getDefaultDisplayContentLocked().getDisplayPolicy()) == null || (hwDisplayPolicyEx = displayPolicy.getHwDisplayPolicyEx()) == null) {
            return false;
        }
        return hwDisplayPolicyEx.isNeedExceptDisplaySide(win.getAttrs(), win, this.mWmService.getDefaultDisplayRotation());
    }

    public void resize() {
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                setBounds(getRequestedOverrideBounds());
                getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isForcedPortraitOrientation() {
        return this.mOrientation == 1 || this.mOrientation == 7 || this.mOrientation == 9 || this.mOrientation == 12;
    }

    private void startWallpaperAnimaiton(WindowManager.LayoutParams lp, int id) {
        Animation wallpaperAnim = getDisplayContent().mAppTransition.loadAnimationRes(lp, id);
        if (wallpaperAnim != null) {
            getDisplayContent().mWallpaperController.startWallpaperAnimation(wallpaperAnim);
        }
    }

    private boolean isCrossAppTransit(int transit) {
        if (getDisplayContent() != null && getDisplayContent().mAppTransition.isNextAppTransitCustom()) {
            return false;
        }
        WindowManager.LayoutParams layoutParams = this.mLayoutParams;
        if (layoutParams != null) {
            int windowAnimations = layoutParams.windowAnimations;
            if ((RES_ID_FLAG_MASK & windowAnimations) != RES_ID_FLAG_SYSTEM) {
                Slog.d(TAG, "isCrossAppTransit, animations " + windowAnimations + " is not system transition");
                return false;
            }
        }
        if (!isSupportDevice()) {
            return false;
        }
        if ((transit == 9 || transit == 8 || transit == 10 || transit == 11) && !isInPcCastMode()) {
            return true;
        }
        return false;
    }

    private boolean isSupportDevice() {
        return IS_PHONE || IS_TABLET;
    }

    private boolean isInPcCastMode() {
        DisplayContent displayContent = getTask() != null ? getTask().getDisplayContent() : null;
        if (displayContent == null) {
            Slog.w(TAG, "isInPcCastMode display content is null");
            return false;
        } else if (!HwPCUtils.isPcCastMode() || !HwPCUtils.isValidExtDisplayId(displayContent.mDisplayId)) {
            return false;
        } else {
            return true;
        }
    }

    private void offsetForSafeSide(Rect rect) {
        int lazyMode = this.mWmService.getLazyMode();
        if (HwDisplaySizeUtil.hasSideInScreen()) {
            int sideSize = HwDisplaySizeUtil.getInstance(this.mWmService).getSafeSideWidth();
            if (lazyMode == 1) {
                rect.offsetTo(rect.left + sideSize, rect.top);
            } else if (lazyMode == 2) {
                rect.offsetTo(rect.left - sideSize, rect.top);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState findSecureWindow() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            WindowState win = (WindowState) this.mChildren.get(j);
            if (win.isVisible() && win.isSecureWindow()) {
                return win;
            }
        }
        return null;
    }

    private boolean isAllowAnimForLauncherInFingerBoost(boolean isHidden, boolean isVisible) {
        return !isHidden && isVisible && this.mWmService.isLauncherInFingerBoost() && this.mWmService.isKeyguardGoingAwayWhileLauncherShowing();
    }

    /* access modifiers changed from: package-private */
    public boolean isPermissionApp() {
        ActivityRecord activityRecord = this.mActivityRecord;
        return activityRecord != null && "com.android.permissioncontroller".equals(activityRecord.packageName);
    }

    private boolean isNeedCropRectForSplitMode() {
        ActivityRecord activityRecord = this.mActivityRecord;
        return activityRecord != null && activityRecord.isShowInSplit() && !this.mActivityRecord.isSplitBaseActivity() && !isTopInFIFOList(this.mActivityRecord);
    }

    private boolean isTopVisibleInFIFOList(ActivityRecord record) {
        ArrayList<ActivityRecord> recordList = getStack().mActivityStack.mFIFOActivities;
        for (int i = recordList.size() - 1; i >= 0; i--) {
            ActivityRecord tempRecord = recordList.get(i);
            if (tempRecord != null && ((tempRecord == record || !tempRecord.finishing || tempRecord.mDrawn) && (tempRecord.isSplitMode() || !tempRecord.isTransluent()))) {
                return tempRecord.fullscreen || tempRecord == record;
            }
        }
        return false;
    }

    private boolean isTopInFIFOList(ActivityRecord record) {
        ArrayList<ActivityRecord> recordList = getStack().mActivityStack.mFIFOActivities;
        if (recordList.size() <= 0) {
            return true;
        }
        ActivityRecord tempRecord = recordList.get(recordList.size() - 1);
        if ((tempRecord == null || !tempRecord.fullscreen) && tempRecord != record) {
            return false;
        }
        return true;
    }

    private void printFIFOList() {
        ArrayList<ActivityRecord> recordList = getStack().mActivityStack.mFIFOActivities;
        for (int i = recordList.size() - 1; i >= 0; i--) {
            ActivityRecord record = recordList.get(i);
            if (!(record == null || record.mAppWindowToken == null)) {
                Slog.d(TAG, "printFIFOList record=" + record.toString() + ", record.finishing:" + record.finishing + ", record.mDrawn:" + record.mDrawn);
                for (int j = record.mAppWindowToken.mChildren.size() + -1; j >= 0; j--) {
                    WindowState win = (WindowState) record.mAppWindowToken.mChildren.get(j);
                    if (!(win == null || win.mWindowFrames == null || win.mWindowFrames.mFrame == null)) {
                        Slog.d(TAG, "onAnimationLeashCreated win=" + win.toString() + " mFrame=" + win.mWindowFrames.mFrame.toString());
                    }
                }
            }
        }
    }
}
