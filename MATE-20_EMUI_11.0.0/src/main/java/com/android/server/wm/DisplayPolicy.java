package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.LoadedApk;
import android.app.ResourcesManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.freeform.HwFreeFormUtils;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.HwFoldScreenState;
import android.hardware.input.InputManager;
import android.hdm.HwDeviceManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.view.DisplayCutout;
import android.view.IApplicationToken;
import android.view.IHwRotateObserver;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.MotionEvent;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.internal.util.ScreenShapeHelper;
import com.android.internal.util.ScreenshotHelper;
import com.android.internal.util.ToBooleanFunction;
import com.android.internal.util.function.TriConsumer;
import com.android.internal.widget.PointerLocationView;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.policy.IHwPhoneWindowManagerEx;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.WindowOrientationListener;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wallpaper.WallpaperManagerInternal;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.BarController;
import com.android.server.wm.SystemGesturesPointerEventListener;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.android.server.wm.utils.InsetUtils;
import com.huawei.server.wm.IHwDisplayPolicyEx;
import com.huawei.server.wm.IHwDisplayPolicyInner;
import java.io.PrintWriter;

public class DisplayPolicy implements IHwDisplayPolicyInner {
    private static final boolean ALTERNATE_CAR_MODE_NAV_SIZE = false;
    private static final boolean DEBUG = false;
    private static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    public static final String LAUNCHER_PACKAGE_NAME = "com.huawei.android.launcher";
    private static final int MSG_DISABLE_POINTER_LOCATION = 5;
    private static final int MSG_DISPOSE_INPUT_CONSUMER = 3;
    private static final int MSG_ENABLE_POINTER_LOCATION = 4;
    private static final int MSG_REQUEST_TRANSIENT_BARS = 2;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_NAVIGATION = 1;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_STATUS = 0;
    private static final int MSG_UPDATE_DREAMING_SLEEP_TOKEN = 1;
    private static final int NAV_BAR_FORCE_TRANSPARENT = 2;
    private static final int NAV_BAR_OPAQUE_WHEN_FREEFORM_OR_DOCKED = 0;
    private static final int NAV_BAR_TRANSLUCENT_WHEN_FREEFORM_OPAQUE_OTHERWISE = 1;
    private static final int NaviHide = 1;
    private static final int NaviInit = -1;
    private static final int NaviShow = 0;
    private static final int NaviTransientShow = 2;
    private static final long PANIC_GESTURE_EXPIRATION = 30000;
    private static final String SEC_IME_PACKAGE = "com.huawei.secime";
    private static final int SEC_IME_RAISE_HEIGHT = SystemProperties.getInt("ro.config.sec_ime_raise_height_dp", 140);
    private static final int SYSTEM_UI_CHANGING_LAYOUT = -1073709042;
    private static final String TAG = "WindowManager";
    private static final boolean mIsHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private static boolean mUsingHwNavibar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private static final Rect sTmpDisplayCutoutSafeExceptMaybeBarsRect = new Rect();
    private static final Rect sTmpDockedFrame = new Rect();
    private static final Rect sTmpLastParentFrame = new Rect();
    private static final Rect sTmpNavFrame = new Rect();
    private static final Rect sTmpRect = new Rect();
    private boolean isHwFullScreenWinVisibility;
    private final AccessibilityManager mAccessibilityManager;
    private final Runnable mAcquireSleepTokenRunnable;
    private boolean mAllowLockscreenWhenOn;
    private volatile boolean mAllowSeamlessRotationDespiteNavBarMoving;
    WindowState mAloneTarget;
    private volatile boolean mAwake;
    private int mBottomGestureAdditionalInset;
    private final boolean mCarDockEnablesAccelerometer;
    private final Runnable mClearHideNavigationFlag;
    private final Context mContext;
    private Resources mCurrentUserResources;
    private final boolean mDeskDockEnablesAccelerometer;
    private final DisplayContent mDisplayContent;
    int mDisplayRotation;
    private volatile int mDockMode = 0;
    private final Rect mDockedStackBounds;
    private boolean mDreamingLockscreen;
    @GuardedBy({"mHandler"})
    private ActivityTaskManagerInternal.SleepToken mDreamingSleepToken;
    private boolean mDreamingSleepTokenNeeded;
    IApplicationToken mFocusedApp;
    private WindowState mFocusedWindow;
    private int mForceClearedSystemUiFlags;
    protected boolean mForceNotchStatusBar = false;
    private boolean mForceShowSystemBars;
    private boolean mForceShowSystemBarsFromExternal;
    private boolean mForceStatusBar;
    private boolean mForceStatusBarFromKeyguard;
    private boolean mForceStatusBarTransparent;
    private boolean mForcingShowNavBar;
    private int mForcingShowNavBarLayer;
    private Insets mForwardedInsets;
    private boolean mFrozen;
    private final Handler mHandler;
    private volatile boolean mHasNavigationBar;
    private volatile boolean mHasStatusBar;
    private volatile boolean mHdmiPlugged;
    private final Runnable mHiddenNavPanic;
    protected IHwDisplayPolicyEx mHwDisplayPolicyEx;
    private WindowState mHwFullScreenWindow;
    private boolean mHwNavColor;
    private final RemoteCallbackList<IHwRotateObserver> mHwRotateObservers = new RemoteCallbackList<>();
    private final ImmersiveModeConfirmation mImmersiveModeConfirmation;
    private WindowManagerPolicy.InputConsumer mInputConsumer;
    private WindowState mInputMethodTarget;
    private volatile boolean mKeyguardDrawComplete;
    private final Rect mLastDockedStackBounds;
    private int mLastDockedStackSysUiFlags;
    private boolean mLastFocusNeedsMenu;
    private WindowState mLastFocusedWindow;
    private int mLastFullscreenStackSysUiFlags;
    int mLastHideNaviDockBottom;
    private boolean mLastHwNavColor;
    int mLastNaviStatus;
    private final Rect mLastNonDockedStackBounds;
    int mLastShowNaviDockBottom;
    private boolean mLastShowingDream;
    private WindowState mLastStartingWindow;
    int mLastSystemUiFlags;
    int mLastTransientNaviDockBottom;
    private boolean mLastWindowSleepTokenNeeded;
    private volatile int mLidState = -1;
    private final Object mLock;
    private int mNavBarOpacityMode;
    private final BarController.OnBarVisibilityChangedListener mNavBarVisibilityListener;
    private WindowState mNavigationBar;
    private volatile boolean mNavigationBarAlwaysShowOnSideGesture;
    private volatile boolean mNavigationBarCanMove;
    private final BarController mNavigationBarController;
    private int[] mNavigationBarFrameHeightForRotationDefault;
    private int[] mNavigationBarHeightForRotationDefault;
    private int[] mNavigationBarHeightForRotationInCarMode;
    private volatile boolean mNavigationBarLetsThroughTaps;
    private int mNavigationBarPosition;
    private int[] mNavigationBarWidthForRotationDefault;
    private int[] mNavigationBarWidthForRotationInCarMode;
    private final Rect mNonDockedStackBounds;
    protected int mNotchStatusBarColorLw = 0;
    private long mPendingPanicGestureUptime;
    private volatile boolean mPersistentVrModeEnabled;
    private PointerLocationView mPointerLocationView;
    private RefreshRatePolicy mRefreshRatePolicy;
    private final Runnable mReleaseSleepTokenRunnable;
    private int mResettingSystemUiFlags;
    int mRestrictedScreenHeight;
    private final ArraySet<WindowState> mScreenDecorWindows = new ArraySet<>();
    private volatile boolean mScreenOnEarly;
    private volatile boolean mScreenOnFully;
    private volatile WindowManagerPolicy.ScreenOnListener mScreenOnListener;
    private final ScreenshotHelper mScreenshotHelper;
    private final WindowManagerService mService;
    private final Object mServiceAcquireLock = new Object();
    private boolean mShowingDream;
    private int mSideGestureInset;
    private WindowState mStatusBar;
    private final StatusBarController mStatusBarController;
    private final int[] mStatusBarHeightForRotation;
    private StatusBarManagerInternal mStatusBarManagerInternal;
    private final SystemGesturesPointerEventListener mSystemGestures;
    private WindowState mTopDockedOpaqueOrDimmingWindowState;
    private WindowState mTopDockedOpaqueWindowState;
    private WindowState mTopFullscreenOpaqueOrDimmingWindowState;
    private WindowState mTopFullscreenOpaqueWindowState;
    private boolean mTopIsFullscreen;
    private volatile boolean mWindowManagerDrawComplete;
    private int mWindowOutsetBottom;
    @GuardedBy({"mHandler"})
    private ActivityTaskManagerInternal.SleepToken mWindowSleepToken;
    private boolean mWindowSleepTokenNeeded;
    boolean notchWindowChange = false;
    boolean notchWindowChangeState = false;

    static /* synthetic */ int access$1972(DisplayPolicy x0, int x1) {
        int i = x0.mForceClearedSystemUiFlags & x1;
        x0.mForceClearedSystemUiFlags = i;
        return i;
    }

    private StatusBarManagerInternal getStatusBarManagerInternal() {
        StatusBarManagerInternal statusBarManagerInternal;
        synchronized (this.mServiceAcquireLock) {
            if (this.mStatusBarManagerInternal == null) {
                this.mStatusBarManagerInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
            }
            statusBarManagerInternal = this.mStatusBarManagerInternal;
        }
        return statusBarManagerInternal;
    }

    private class PolicyHandler extends Handler {
        PolicyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean z = true;
            if (i == 1) {
                DisplayPolicy displayPolicy = DisplayPolicy.this;
                if (msg.arg1 == 0) {
                    z = false;
                }
                displayPolicy.updateDreamingSleepToken(z);
            } else if (i == 2) {
                WindowState targetBar = msg.arg1 == 0 ? DisplayPolicy.this.mStatusBar : DisplayPolicy.this.mNavigationBar;
                if (targetBar != null) {
                    DisplayPolicy.this.requestTransientBars(targetBar);
                }
            } else if (i == 3) {
                DisplayPolicy.this.disposeInputConsumer((WindowManagerPolicy.InputConsumer) msg.obj);
            } else if (i == 4) {
                DisplayPolicy.this.enablePointerLocation();
            } else if (i == 5) {
                DisplayPolicy.this.disablePointerLocation();
            }
        }
    }

    DisplayPolicy(WindowManagerService service, DisplayContent displayContent) {
        Context context;
        ScreenshotHelper screenshotHelper = null;
        this.mStatusBar = null;
        this.mStatusBarHeightForRotation = new int[4];
        this.mNavigationBar = null;
        this.mNavigationBarPosition = 4;
        this.mNavigationBarHeightForRotationDefault = new int[4];
        this.mNavigationBarWidthForRotationDefault = new int[4];
        this.mNavigationBarHeightForRotationInCarMode = new int[4];
        this.mNavigationBarWidthForRotationInCarMode = new int[4];
        this.mNavigationBarFrameHeightForRotationDefault = new int[4];
        this.mNavBarVisibilityListener = new BarController.OnBarVisibilityChangedListener() {
            /* class com.android.server.wm.DisplayPolicy.AnonymousClass1 */

            @Override // com.android.server.wm.BarController.OnBarVisibilityChangedListener
            public void onBarVisibilityChanged(boolean visible) {
                if (DisplayPolicy.this.mAccessibilityManager != null) {
                    DisplayPolicy.this.mAccessibilityManager.notifyAccessibilityButtonVisibilityChanged(visible);
                }
            }
        };
        this.mResettingSystemUiFlags = 0;
        this.mForceClearedSystemUiFlags = 0;
        this.mNonDockedStackBounds = new Rect();
        this.mDockedStackBounds = new Rect();
        this.mLastNonDockedStackBounds = new Rect();
        this.mLastDockedStackBounds = new Rect();
        this.mLastFocusNeedsMenu = false;
        this.mNavBarOpacityMode = 0;
        this.mInputConsumer = null;
        this.mForwardedInsets = Insets.NONE;
        this.mLastShowNaviDockBottom = 0;
        this.mLastHideNaviDockBottom = 0;
        this.mLastTransientNaviDockBottom = 0;
        this.mLastNaviStatus = -1;
        this.mHwFullScreenWindow = null;
        this.isHwFullScreenWinVisibility = false;
        this.mClearHideNavigationFlag = new Runnable() {
            /* class com.android.server.wm.DisplayPolicy.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (DisplayPolicy.this.mLock) {
                    DisplayPolicy.access$1972(DisplayPolicy.this, -3);
                    DisplayPolicy.this.mDisplayContent.reevaluateStatusBarVisibility();
                }
            }
        };
        this.mFrozen = false;
        this.mLastHwNavColor = false;
        this.mHwNavColor = false;
        this.mHiddenNavPanic = new Runnable() {
            /* class com.android.server.wm.DisplayPolicy.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (DisplayPolicy.this.mLock) {
                    if (DisplayPolicy.this.mService.mPolicy.isUserSetupComplete()) {
                        DisplayPolicy.this.mPendingPanicGestureUptime = SystemClock.uptimeMillis();
                        if (!DisplayPolicy.isNavBarEmpty(DisplayPolicy.this.mLastSystemUiFlags)) {
                            DisplayPolicy.this.mNavigationBarController.showTransient();
                        }
                    }
                }
            }
        };
        this.mService = service;
        if (displayContent.isDefaultDisplay) {
            context = service.mContext;
        } else {
            context = service.mContext.createDisplayContext(displayContent.getDisplay());
        }
        this.mContext = context;
        this.mDisplayContent = displayContent;
        this.mLock = service.getWindowManagerLock();
        int displayId = displayContent.getDisplayId();
        this.mStatusBarController = new StatusBarController(displayId);
        this.mNavigationBarController = new BarController("NavigationBar", displayId, 134217728, 536870912, Integer.MIN_VALUE, 2, 134217728, 32768);
        Resources r = this.mContext.getResources();
        this.mCarDockEnablesAccelerometer = r.getBoolean(17891386);
        this.mDeskDockEnablesAccelerometer = r.getBoolean(17891401);
        this.mForceShowSystemBarsFromExternal = r.getBoolean(17891459);
        this.mAccessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        if (!displayContent.isDefaultDisplay) {
            this.mAwake = true;
            this.mScreenOnEarly = true;
            this.mScreenOnFully = true;
        }
        Looper looper = UiThread.getHandler().getLooper();
        this.mHandler = new PolicyHandler(looper);
        this.mSystemGestures = new SystemGesturesPointerEventListener(this.mContext, this.mHandler, new SystemGesturesPointerEventListener.Callbacks() {
            /* class com.android.server.wm.DisplayPolicy.AnonymousClass2 */

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onSwipeFromTop() {
                if (!DisplayPolicy.this.mHwDisplayPolicyEx.isGestureIsolated(DisplayPolicy.this.mFocusedWindow, DisplayPolicy.this.mTopFullscreenOpaqueWindowState)) {
                    if (!DisplayPolicy.this.mTopIsFullscreen || !HwDeviceManager.disallowOp(71)) {
                        DisplayPolicy.this.mHwDisplayPolicyEx.showTopBar(DisplayPolicy.this.mHandler, DisplayPolicy.this.getDisplayId());
                        DisplayPolicy.this.mHwDisplayPolicyEx.swipeFromTop();
                        if (!DisplayPolicy.this.swipeFromTop() && DisplayPolicy.this.mStatusBar != null) {
                            DisplayPolicy displayPolicy = DisplayPolicy.this;
                            displayPolicy.requestTransientBars(displayPolicy.mStatusBar);
                        }
                    }
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onSwipeFromBottom() {
                if (!DisplayPolicy.this.mHwDisplayPolicyEx.isGestureIsolated(DisplayPolicy.this.mFocusedWindow, DisplayPolicy.this.mTopFullscreenOpaqueWindowState) && !DisplayPolicy.this.mHwDisplayPolicyEx.swipeFromBottom() && DisplayPolicy.this.mNavigationBar != null && DisplayPolicy.this.mNavigationBarPosition == 4) {
                    DisplayPolicy displayPolicy = DisplayPolicy.this;
                    displayPolicy.requestTransientBars(displayPolicy.mNavigationBar);
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onSwipeFromRight() {
                Region excludedRegion;
                if (!DisplayPolicy.this.mHwDisplayPolicyEx.isGestureIsolated(DisplayPolicy.this.mFocusedWindow, DisplayPolicy.this.mTopFullscreenOpaqueWindowState) && !DisplayPolicy.this.mHwDisplayPolicyEx.swipeFromRight()) {
                    synchronized (DisplayPolicy.this.mLock) {
                        excludedRegion = DisplayPolicy.this.mDisplayContent.calculateSystemGestureExclusion();
                    }
                    boolean sideAllowed = DisplayPolicy.this.mNavigationBarAlwaysShowOnSideGesture || DisplayPolicy.this.mNavigationBarPosition == 2;
                    if (DisplayPolicy.this.mNavigationBar != null && sideAllowed && !DisplayPolicy.this.mSystemGestures.currentGestureStartedInRegion(excludedRegion)) {
                        DisplayPolicy displayPolicy = DisplayPolicy.this;
                        displayPolicy.requestTransientBars(displayPolicy.mNavigationBar);
                    }
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onSwipeFromLeft() {
                Region excludedRegion;
                if (!DisplayPolicy.this.mHwDisplayPolicyEx.swipeFromLeft()) {
                    synchronized (DisplayPolicy.this.mLock) {
                        excludedRegion = DisplayPolicy.this.mDisplayContent.calculateSystemGestureExclusion();
                    }
                    boolean sideAllowed = true;
                    if (!DisplayPolicy.this.mNavigationBarAlwaysShowOnSideGesture && DisplayPolicy.this.mNavigationBarPosition != 1) {
                        sideAllowed = false;
                    }
                    if (DisplayPolicy.this.mNavigationBar != null && sideAllowed && !DisplayPolicy.this.mSystemGestures.currentGestureStartedInRegion(excludedRegion)) {
                        DisplayPolicy displayPolicy = DisplayPolicy.this;
                        displayPolicy.requestTransientBars(displayPolicy.mNavigationBar);
                    }
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onFling(int duration) {
                if (DisplayPolicy.this.mService.mPowerManagerInternal != null) {
                    DisplayPolicy.this.mService.mPowerManagerInternal.powerHint(2, duration);
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onDebug() {
            }

            private WindowOrientationListener getOrientationListener() {
                DisplayRotation rotation = DisplayPolicy.this.mDisplayContent.getDisplayRotation();
                if (rotation != null) {
                    return rotation.getOrientationListener();
                }
                return null;
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onDown() {
                WindowOrientationListener listener = getOrientationListener();
                if (listener != null) {
                    listener.onTouchStart();
                }
                DisplayPolicy.this.mHwDisplayPolicyEx.onPointDown();
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onUpOrCancel() {
                WindowOrientationListener listener = getOrientationListener();
                if (listener != null) {
                    listener.onTouchEnd();
                }
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onMouseHoverAtTop() {
                DisplayPolicy.this.mHandler.removeMessages(2);
                Message msg = DisplayPolicy.this.mHandler.obtainMessage(2);
                msg.arg1 = 0;
                DisplayPolicy.this.mHandler.sendMessageDelayed(msg, 500);
                DisplayPolicy.this.mHwDisplayPolicyEx.showTopBar(DisplayPolicy.this.mHandler, DisplayPolicy.this.getDisplayId());
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onMouseHoverAtBottom() {
                DisplayPolicy.this.mHandler.removeMessages(2);
                Message msg = DisplayPolicy.this.mHandler.obtainMessage(2);
                msg.arg1 = 1;
                DisplayPolicy.this.mHandler.sendMessageDelayed(msg, 500);
            }

            @Override // com.android.server.wm.SystemGesturesPointerEventListener.Callbacks
            public void onMouseLeaveFromEdge() {
                DisplayPolicy.this.mHandler.removeMessages(2);
            }
        });
        this.mSystemGestures.setDisplayContent(this.mDisplayContent);
        displayContent.registerPointerEventListener(this.mSystemGestures);
        displayContent.mAppTransition.registerListenerLocked(this.mStatusBarController.getAppTransitionListener());
        this.mImmersiveModeConfirmation = new ImmersiveModeConfirmation(this.mContext, looper, this.mService.mVrModeEnabled);
        this.mAcquireSleepTokenRunnable = new Runnable(service, displayId) {
            /* class com.android.server.wm.$$Lambda$DisplayPolicy$j3sY1jb4WFF_F3wOT9D2fB2mOts */
            private final /* synthetic */ WindowManagerService f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                DisplayPolicy.this.lambda$new$0$DisplayPolicy(this.f$1, this.f$2);
            }
        };
        this.mReleaseSleepTokenRunnable = new Runnable() {
            /* class com.android.server.wm.$$Lambda$DisplayPolicy$_FsvHpVUigbWmSpT009cJNNmgM */

            @Override // java.lang.Runnable
            public final void run() {
                DisplayPolicy.this.lambda$new$1$DisplayPolicy();
            }
        };
        this.mScreenshotHelper = displayContent.isDefaultDisplay ? new ScreenshotHelper(this.mContext) : screenshotHelper;
        this.mHwDisplayPolicyEx = HwServiceExFactory.getHwDisplayPolicyEx(this.mService, this, this.mDisplayContent, this.mContext, displayContent.isDefaultDisplay);
        if (this.mDisplayContent.isDefaultDisplay) {
            this.mHasStatusBar = true;
            this.mHasNavigationBar = this.mContext.getResources().getBoolean(17891515);
            String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                this.mHasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                this.mHasNavigationBar = true;
            }
        } else {
            this.mHasStatusBar = false;
            this.mHasNavigationBar = this.mDisplayContent.supportsSystemDecorations();
        }
        this.mRefreshRatePolicy = new RefreshRatePolicy(this.mService, this.mDisplayContent.getDisplayInfo(), this.mService.mHighRefreshRateBlacklist);
    }

    public /* synthetic */ void lambda$new$0$DisplayPolicy(WindowManagerService service, int displayId) {
        if (this.mWindowSleepToken == null) {
            ActivityTaskManagerInternal activityTaskManagerInternal = service.mAtmInternal;
            this.mWindowSleepToken = activityTaskManagerInternal.acquireSleepToken("WindowSleepTokenOnDisplay" + displayId, displayId);
        }
    }

    public /* synthetic */ void lambda$new$1$DisplayPolicy() {
        ActivityTaskManagerInternal.SleepToken sleepToken = this.mWindowSleepToken;
        if (sleepToken != null) {
            sleepToken.release();
            this.mWindowSleepToken = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mSystemGestures.systemReady();
        this.mHwDisplayPolicyEx.systemReadyEx();
        if (this.mService.mPointerLocationEnabled) {
            if (!(HwPCUtils.enabledInPad() && "HUAWEI PAD PC Display".equals(this.mDisplayContent.getDisplayInfo().name))) {
                setPointerLocationEnabled(true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDisplayId() {
        return this.mDisplayContent.getDisplayId();
    }

    public void setHdmiPlugged(boolean plugged) {
        setHdmiPlugged(plugged, false);
    }

    public void setHdmiPlugged(boolean plugged, boolean force) {
        if (force || this.mHdmiPlugged != plugged) {
            this.mHdmiPlugged = plugged;
            this.mService.updateRotation(true, true);
            Intent intent = new Intent("android.intent.action.HDMI_PLUGGED");
            intent.addFlags(67108864);
            intent.putExtra("state", plugged);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isHdmiPlugged() {
        return this.mHdmiPlugged;
    }

    /* access modifiers changed from: package-private */
    public boolean isCarDockEnablesAccelerometer() {
        return this.mCarDockEnablesAccelerometer;
    }

    /* access modifiers changed from: package-private */
    public boolean isDeskDockEnablesAccelerometer() {
        return this.mDeskDockEnablesAccelerometer;
    }

    public void setPersistentVrModeEnabled(boolean persistentVrModeEnabled) {
        this.mPersistentVrModeEnabled = persistentVrModeEnabled;
    }

    public boolean isPersistentVrModeEnabled() {
        return this.mPersistentVrModeEnabled;
    }

    public void setDockMode(int dockMode) {
        this.mDockMode = dockMode;
    }

    public int getDockMode() {
        return this.mDockMode;
    }

    /* access modifiers changed from: package-private */
    public void setForceShowSystemBars(boolean forceShowSystemBars) {
        this.mForceShowSystemBarsFromExternal = forceShowSystemBars;
    }

    public boolean hasNavigationBar() {
        return this.mHasNavigationBar;
    }

    public boolean hasStatusBar() {
        return this.mHasStatusBar;
    }

    public boolean navigationBarCanMove() {
        return this.mNavigationBarCanMove;
    }

    public void setLidState(int lidState) {
        this.mLidState = lidState;
    }

    public int getLidState() {
        return this.mLidState;
    }

    public void setAwake(boolean awake) {
        this.mAwake = awake;
    }

    public boolean isAwake() {
        return this.mAwake;
    }

    public boolean isScreenOnEarly() {
        return this.mScreenOnEarly;
    }

    public boolean isScreenOnFully() {
        return this.mScreenOnFully;
    }

    public boolean isKeyguardDrawComplete() {
        return this.mKeyguardDrawComplete;
    }

    public boolean isWindowManagerDrawComplete() {
        return this.mWindowManagerDrawComplete;
    }

    public WindowManagerPolicy.ScreenOnListener getScreenOnListener() {
        return this.mScreenOnListener;
    }

    public void screenTurnedOn(WindowManagerPolicy.ScreenOnListener screenOnListener) {
        synchronized (this.mLock) {
            this.mScreenOnEarly = true;
            this.mScreenOnFully = false;
            this.mKeyguardDrawComplete = false;
            this.mWindowManagerDrawComplete = false;
            this.mScreenOnListener = screenOnListener;
        }
    }

    public void screenTurnedOff() {
        synchronized (this.mLock) {
            this.mScreenOnEarly = false;
            this.mScreenOnFully = false;
            this.mKeyguardDrawComplete = false;
            this.mWindowManagerDrawComplete = false;
            this.mScreenOnListener = null;
        }
    }

    public boolean finishKeyguardDrawn() {
        synchronized (this.mLock) {
            if (this.mScreenOnEarly) {
                if (!this.mKeyguardDrawComplete) {
                    this.mKeyguardDrawComplete = true;
                    this.mWindowManagerDrawComplete = false;
                    return true;
                }
            }
            return false;
        }
    }

    public boolean finishWindowsDrawn() {
        synchronized (this.mLock) {
            if (this.mScreenOnEarly) {
                if (!this.mWindowManagerDrawComplete) {
                    this.mWindowManagerDrawComplete = true;
                    return true;
                }
            }
            return false;
        }
    }

    public boolean finishScreenTurningOn() {
        synchronized (this.mLock) {
            if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                Slog.d(TAG, "finishScreenTurningOn: mAwake=" + this.mAwake + ", mScreenOnEarly=" + this.mScreenOnEarly + ", mScreenOnFully=" + this.mScreenOnFully + ", mKeyguardDrawComplete=" + this.mKeyguardDrawComplete + ", mWindowManagerDrawComplete=" + this.mWindowManagerDrawComplete);
            }
            if (!this.mScreenOnFully && this.mScreenOnEarly && this.mWindowManagerDrawComplete) {
                if (!this.mAwake || this.mKeyguardDrawComplete) {
                    if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                        Slog.i(TAG, "Finished screen turning on...");
                    }
                    this.mScreenOnListener = null;
                    this.mScreenOnFully = true;
                    return true;
                }
            }
            return false;
        }
    }

    private boolean hasStatusBarServicePermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.STATUS_BAR_SERVICE", pid, uid) == 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0044, code lost:
        if (r2 != 2006) goto L_0x00b0;
     */
    public void adjustWindowParamsLw(WindowState win, WindowManager.LayoutParams attrs, int callingPid, int callingUid) {
        boolean isScreenDecor = (attrs.privateFlags & 4194304) != 0;
        if (this.mScreenDecorWindows.contains(win)) {
            if (!isScreenDecor) {
                this.mScreenDecorWindows.remove(win);
            }
        } else if (isScreenDecor && hasStatusBarServicePermission(callingPid, callingUid)) {
            this.mScreenDecorWindows.add(win);
        }
        int i = attrs.type;
        if (i != 2000) {
            if (i != 2013) {
                if (i != 2015) {
                    if (i != 2023) {
                        if (i == 2036) {
                            attrs.flags |= 8;
                        } else if (i == 2005) {
                            if (attrs.hideTimeoutMilliseconds < 0 || attrs.hideTimeoutMilliseconds > 3500) {
                                attrs.hideTimeoutMilliseconds = 3500;
                            }
                            attrs.hideTimeoutMilliseconds = (long) this.mAccessibilityManager.getRecommendedTimeoutMillis((int) attrs.hideTimeoutMilliseconds, 2);
                            attrs.windowAnimations = 16973828;
                            if (canToastShowWhenLocked(callingPid)) {
                                attrs.flags |= 524288;
                            }
                            attrs.flags |= 16;
                        }
                    }
                }
                attrs.flags |= 24;
                attrs.flags &= -262145;
            }
            attrs.layoutInDisplayCutoutMode = 1;
        } else if (this.mService.mPolicy.isKeyguardOccluded()) {
            attrs.flags &= -1048577;
            attrs.privateFlags &= -1025;
        }
        if (attrs.type != 2000) {
            attrs.privateFlags &= -1025;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canToastShowWhenLocked(int callingPid) {
        return this.mDisplayContent.forAllWindows((ToBooleanFunction<WindowState>) new ToBooleanFunction(callingPid) {
            /* class com.android.server.wm.$$Lambda$DisplayPolicy$pqtzqy0ticsynvTP9P1eQUEgE */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean apply(Object obj) {
                return DisplayPolicy.lambda$canToastShowWhenLocked$2(this.f$0, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ boolean lambda$canToastShowWhenLocked$2(int callingPid, WindowState w) {
        return callingPid == w.mSession.mPid && w.isVisible() && w.canShowWhenLocked();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0038, code lost:
        if (r0 != 2033) goto L_0x00ed;
     */
    public int prepareAddWindowLw(WindowState win, WindowManager.LayoutParams attrs) {
        if (this.mHwDisplayPolicyEx.prepareAddWindowForPC(win, attrs) == 0) {
            return 0;
        }
        if ((attrs.privateFlags & 4194304) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "DisplayPolicy");
            this.mScreenDecorWindows.add(win);
        }
        int i = attrs.type;
        if (i != 2000) {
            if (!(i == 2014 || i == 2017)) {
                if (i == 2019) {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "DisplayPolicy");
                    WindowState windowState = this.mNavigationBar;
                    if (windowState != null && windowState.isAlive()) {
                        return -7;
                    }
                    this.mNavigationBar = win;
                    this.mNavigationBarController.setWindow(win);
                    this.mNavigationBarController.setOnBarVisibilityChangedListener(this.mNavBarVisibilityListener, true);
                    this.mDisplayContent.setInsetProvider(1, win, null);
                    this.mDisplayContent.setInsetProvider(5, win, new TriConsumer() {
                        /* class com.android.server.wm.$$Lambda$DisplayPolicy$52bg3qYmo5Unt8Q07j9d6hFQG2o */

                        public final void accept(Object obj, Object obj2, Object obj3) {
                            DisplayPolicy.this.lambda$prepareAddWindowLw$4$DisplayPolicy((DisplayFrames) obj, (WindowState) obj2, (Rect) obj3);
                        }
                    });
                    this.mDisplayContent.setInsetProvider(6, win, new TriConsumer() {
                        /* class com.android.server.wm.$$Lambda$DisplayPolicy$XeqRJzc7ac4NU1zAF74Hsb20Oyg */

                        public final void accept(Object obj, Object obj2, Object obj3) {
                            DisplayPolicy.this.lambda$prepareAddWindowLw$5$DisplayPolicy((DisplayFrames) obj, (WindowState) obj2, (Rect) obj3);
                        }
                    });
                    this.mDisplayContent.setInsetProvider(7, win, new TriConsumer() {
                        /* class com.android.server.wm.$$Lambda$DisplayPolicy$2VfPB7jRHi3x9grU1pG8ihi_Ga4 */

                        public final void accept(Object obj, Object obj2, Object obj3) {
                            DisplayPolicy.this.lambda$prepareAddWindowLw$6$DisplayPolicy((DisplayFrames) obj, (WindowState) obj2, (Rect) obj3);
                        }
                    });
                    this.mDisplayContent.setInsetProvider(9, win, new TriConsumer() {
                        /* class com.android.server.wm.$$Lambda$DisplayPolicy$LmU9vcWscAr5f4KqPLDYJTaZBVU */

                        public final void accept(Object obj, Object obj2, Object obj3) {
                            DisplayPolicy.this.lambda$prepareAddWindowLw$7$DisplayPolicy((DisplayFrames) obj, (WindowState) obj2, (Rect) obj3);
                        }
                    });
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.i(TAG, "NAVIGATION BAR: " + this.mNavigationBar);
                    }
                } else if (i != 2024) {
                }
            }
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "DisplayPolicy");
        } else {
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "DisplayPolicy");
            WindowState windowState2 = this.mStatusBar;
            if (windowState2 != null && windowState2.isAlive()) {
                return -7;
            }
            this.mStatusBar = win;
            this.mStatusBarController.setWindow(win);
            if (this.mDisplayContent.isDefaultDisplay) {
                this.mService.mPolicy.setKeyguardCandidateLw(win);
            }
            TriConsumer<DisplayFrames, WindowState, Rect> frameProvider = new TriConsumer() {
                /* class com.android.server.wm.$$Lambda$DisplayPolicy$sDsfACJdM5Dc_VvZ4b6PthimRJY */

                public final void accept(Object obj, Object obj2, Object obj3) {
                    DisplayPolicy.this.lambda$prepareAddWindowLw$3$DisplayPolicy((DisplayFrames) obj, (WindowState) obj2, (Rect) obj3);
                }
            };
            this.mDisplayContent.setInsetProvider(0, win, frameProvider);
            this.mDisplayContent.setInsetProvider(4, win, frameProvider);
            this.mDisplayContent.setInsetProvider(8, win, frameProvider);
        }
        return 0;
    }

    public /* synthetic */ void lambda$prepareAddWindowLw$3$DisplayPolicy(DisplayFrames displayFrames, WindowState windowState, Rect rect) {
        rect.top = 0;
        rect.bottom = getStatusBarHeight(displayFrames);
    }

    public /* synthetic */ void lambda$prepareAddWindowLw$4$DisplayPolicy(DisplayFrames displayFrames, WindowState windowState, Rect inOutFrame) {
        inOutFrame.top -= this.mBottomGestureAdditionalInset;
    }

    public /* synthetic */ void lambda$prepareAddWindowLw$5$DisplayPolicy(DisplayFrames displayFrames, WindowState windowState, Rect inOutFrame) {
        inOutFrame.left = 0;
        inOutFrame.top = 0;
        inOutFrame.bottom = displayFrames.mDisplayHeight;
        inOutFrame.right = displayFrames.mUnrestricted.left + this.mSideGestureInset;
    }

    public /* synthetic */ void lambda$prepareAddWindowLw$6$DisplayPolicy(DisplayFrames displayFrames, WindowState windowState, Rect inOutFrame) {
        inOutFrame.left = displayFrames.mUnrestricted.right - this.mSideGestureInset;
        inOutFrame.top = 0;
        inOutFrame.bottom = displayFrames.mDisplayHeight;
        inOutFrame.right = displayFrames.mDisplayWidth;
    }

    public /* synthetic */ void lambda$prepareAddWindowLw$7$DisplayPolicy(DisplayFrames displayFrames, WindowState windowState, Rect inOutFrame) {
        if ((windowState.getAttrs().flags & 16) != 0 || this.mNavigationBarLetsThroughTaps) {
            inOutFrame.setEmpty();
        }
    }

    public void removeWindowLw(WindowState win) {
        if (this.mStatusBar == win) {
            this.mStatusBar = null;
            this.mStatusBarController.setWindow(null);
            if (this.mDisplayContent.isDefaultDisplay) {
                this.mService.mPolicy.setKeyguardCandidateLw((WindowManagerPolicy.WindowState) null);
            }
            this.mDisplayContent.setInsetProvider(0, null, null);
        } else if (this.mNavigationBar == win) {
            this.mNavigationBar = null;
            this.mNavigationBarController.setWindow(null);
            this.mDisplayContent.setInsetProvider(1, null, null);
        }
        if (this.mLastFocusedWindow == win) {
            this.mLastFocusedWindow = null;
        }
        this.mScreenDecorWindows.remove(win);
        this.mHwDisplayPolicyEx.removeWindowForPC(win);
    }

    private int getStatusBarHeight(DisplayFrames displayFrames) {
        return Math.max(this.mStatusBarHeightForRotation[displayFrames.mRotation], displayFrames.mDisplayCutoutSafe.top);
    }

    public int selectAnimationLw(WindowState win, int transit) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.i(TAG, "selectAnimation in " + win + ": transit=" + transit);
        }
        if (win == this.mStatusBar) {
            boolean isKeyguard = (win.getAttrs().privateFlags & 1024) != 0;
            boolean expanded = win.getAttrs().height == -1 && win.getAttrs().width == -1;
            if (isKeyguard || expanded) {
                return -1;
            }
            if (transit == 2 || transit == 4) {
                return 17432759;
            }
            if (transit == 1 || transit == 3) {
                return 17432758;
            }
        } else if (win == this.mNavigationBar) {
            if (win.getAttrs().windowAnimations != 0) {
                return 0;
            }
            int i = this.mNavigationBarPosition;
            if (i == 4) {
                if (transit == 2 || transit == 4) {
                    if (this.mService.mPolicy.isKeyguardShowingAndNotOccluded()) {
                        return 17432753;
                    }
                    return 17432752;
                } else if (transit == 1 || transit == 3) {
                    return 17432751;
                }
            } else if (i == 2) {
                if (transit == 2 || transit == 4) {
                    return 17432757;
                }
                if (transit == 1 || transit == 3) {
                    return 17432756;
                }
            } else if (i == 1) {
                if (transit == 2 || transit == 4) {
                    return 17432755;
                }
                if (transit == 1 || transit == 3) {
                    return 17432754;
                }
            }
        } else if (win.getAttrs().type == 2034) {
            return selectDockedDividerAnimationLw(win, transit);
        }
        if (transit == 5) {
            if (win.hasAppShownWindows()) {
                if (!WindowManagerDebugConfig.DEBUG_ANIM) {
                    return 17432732;
                }
                Slog.i(TAG, "**** STARTING EXIT");
                return 17432732;
            }
        } else if (win.getAttrs().type == 2023 && this.mDreamingLockscreen && transit == 1) {
            return -1;
        }
        return 0;
    }

    private int selectDockedDividerAnimationLw(WindowState win, int transit) {
        int insets = this.mDisplayContent.getDockedDividerController().getContentInsets();
        Rect frame = win.getFrameLw();
        boolean behindNavBar = this.mNavigationBar != null && ((this.mNavigationBarPosition == 4 && frame.top + insets >= this.mNavigationBar.getFrameLw().top) || ((this.mNavigationBarPosition == 2 && frame.left + insets >= this.mNavigationBar.getFrameLw().left) || (this.mNavigationBarPosition == 1 && frame.right - insets <= this.mNavigationBar.getFrameLw().right)));
        boolean landscape = frame.height() > frame.width();
        boolean offscreen = (landscape && (frame.right - insets <= 0 || frame.left + insets >= win.getDisplayFrameLw().right)) || (!landscape && (frame.top - insets <= 0 || frame.bottom + insets >= win.getDisplayFrameLw().bottom));
        if (behindNavBar || offscreen) {
            return 0;
        }
        if (transit == 1 || transit == 3) {
            return 17432576;
        }
        if (transit == 2) {
            return 17432577;
        }
        return 0;
    }

    public void selectRotationAnimationLw(int[] anim) {
        boolean forceJumpcut = !this.mScreenOnFully || !this.mService.mPolicy.okToAnimate();
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            StringBuilder sb = new StringBuilder();
            sb.append("selectRotationAnimation mTopFullscreen=");
            sb.append(this.mTopFullscreenOpaqueWindowState);
            sb.append(" rotationAnimation=");
            WindowState windowState = this.mTopFullscreenOpaqueWindowState;
            sb.append(windowState == null ? "0" : Integer.valueOf(windowState.getAttrs().rotationAnimation));
            sb.append(" forceJumpcut=");
            sb.append(forceJumpcut);
            Slog.i(TAG, sb.toString());
        }
        if (forceJumpcut) {
            anim[0] = 17432853;
            anim[1] = 17432852;
            return;
        }
        WindowState windowState2 = this.mTopFullscreenOpaqueWindowState;
        if (windowState2 != null) {
            int animationHint = windowState2.getRotationAnimationHint();
            if (animationHint < 0 && this.mTopIsFullscreen) {
                animationHint = this.mTopFullscreenOpaqueWindowState.getAttrs().rotationAnimation;
            }
            if (animationHint != 1) {
                if (animationHint == 2) {
                    anim[0] = 17432853;
                    anim[1] = 17432852;
                    return;
                } else if (animationHint != 3) {
                    anim[1] = 0;
                    anim[0] = 0;
                    return;
                }
            }
            anim[0] = 17432854;
            anim[1] = 17432852;
            return;
        }
        anim[1] = 0;
        anim[0] = 0;
    }

    public boolean validateRotationAnimationLw(int exitAnimId, int enterAnimId, boolean forceDefault) {
        switch (exitAnimId) {
            case 17432853:
            case 17432854:
                if (forceDefault) {
                    return false;
                }
                int[] anim = new int[2];
                selectRotationAnimationLw(anim);
                if (exitAnimId == anim[0] && enterAnimId == anim[1]) {
                    return true;
                }
                return false;
            default:
                return true;
        }
    }

    public int adjustSystemUiVisibilityLw(int visibility) {
        this.mStatusBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mNavigationBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mResettingSystemUiFlags &= visibility;
        return (~this.mResettingSystemUiFlags) & visibility & (~this.mForceClearedSystemUiFlags);
    }

    public boolean areSystemBarsForcedShownLw(WindowState windowState) {
        return this.mForceShowSystemBars;
    }

    public boolean getLayoutHintLw(WindowManager.LayoutParams attrs, Rect taskBounds, DisplayFrames displayFrames, boolean floatingStack, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper outDisplayCutout) {
        Rect sf;
        Rect cf;
        int outset;
        int fl = PolicyControl.getWindowFlags(null, attrs);
        int pfl = attrs.privateFlags;
        int sysUiVis = getImpliedSysUiFlagsForLayout(attrs) | PolicyControl.getSystemUiVisibility(null, attrs);
        int displayRotation = displayFrames.mRotation;
        boolean screenDecor = true;
        if ((outOutsets != null && shouldUseOutsets(attrs, fl)) && (outset = this.mWindowOutsetBottom) > 0) {
            if (displayRotation == 0) {
                outOutsets.bottom += outset;
            } else if (displayRotation == 1) {
                outOutsets.right += outset;
            } else if (displayRotation == 2) {
                outOutsets.top += outset;
            } else if (displayRotation == 3) {
                outOutsets.left += outset;
            }
        }
        boolean layoutInScreen = (fl & 256) != 0;
        boolean layoutInScreenAndInsetDecor = layoutInScreen && (65536 & fl) != 0;
        if ((pfl & 4194304) == 0) {
            screenDecor = false;
        }
        if (!layoutInScreenAndInsetDecor || screenDecor) {
            if (layoutInScreen) {
                outFrame.set(displayFrames.mUnrestricted);
            } else {
                outFrame.set(displayFrames.mStable);
            }
            if (taskBounds != null) {
                outFrame.intersect(taskBounds);
            }
            outContentInsets.setEmpty();
            outStableInsets.setEmpty();
            outDisplayCutout.set(DisplayCutout.NO_CUTOUT);
            return this.mForceShowSystemBars;
        }
        if ((sysUiVis & 512) != 0) {
            outFrame.set(displayFrames.mUnrestricted);
        } else {
            outFrame.set(displayFrames.mRestricted);
        }
        if (floatingStack) {
            sf = null;
        } else {
            sf = displayFrames.mStable;
        }
        if (floatingStack) {
            cf = null;
        } else if ((sysUiVis & 256) != 0) {
            if ((fl & 1024) != 0) {
                cf = displayFrames.mStableFullscreen;
            } else {
                cf = displayFrames.mStable;
            }
        } else if ((fl & 1024) == 0 && (33554432 & fl) == 0) {
            cf = displayFrames.mCurrent;
        } else {
            cf = displayFrames.mOverscan;
        }
        if (taskBounds != null) {
            outFrame.intersect(taskBounds);
        }
        InsetUtils.insetsBetweenFrames(outFrame, cf, outContentInsets);
        InsetUtils.insetsBetweenFrames(outFrame, sf, outStableInsets);
        outDisplayCutout.set(displayFrames.mDisplayCutout.calculateRelativeTo(outFrame).getDisplayCutout());
        return this.mForceShowSystemBars;
    }

    private static int getImpliedSysUiFlagsForLayout(WindowManager.LayoutParams attrs) {
        int impliedFlags = 0;
        if ((attrs.flags & Integer.MIN_VALUE) != 0) {
            impliedFlags = 0 | 512;
        }
        boolean forceWindowDrawsBarBackgrounds = (attrs.privateFlags & 131072) != 0 && attrs.height == -1 && attrs.width == -1;
        if ((Integer.MIN_VALUE & attrs.flags) != 0 || forceWindowDrawsBarBackgrounds) {
            return impliedFlags | 1024;
        }
        return impliedFlags;
    }

    private static boolean shouldUseOutsets(WindowManager.LayoutParams attrs, int fl) {
        return attrs.type == 2013 || (33555456 & fl) != 0;
    }

    private final class HideNavInputEventReceiver extends InputEventReceiver {
        HideNavInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent event) {
            try {
                if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0 && ((MotionEvent) event).getAction() == 0) {
                    boolean changed = false;
                    synchronized (DisplayPolicy.this.mLock) {
                        if (DisplayPolicy.this.mInputConsumer != null) {
                            int newVal = DisplayPolicy.this.mResettingSystemUiFlags | 2 | 1 | 4;
                            if (DisplayPolicy.this.mResettingSystemUiFlags != newVal) {
                                DisplayPolicy.this.mResettingSystemUiFlags = newVal;
                                changed = true;
                            }
                            int newVal2 = DisplayPolicy.this.mForceClearedSystemUiFlags | 2;
                            if (DisplayPolicy.this.mForceClearedSystemUiFlags != newVal2) {
                                DisplayPolicy.this.mForceClearedSystemUiFlags = newVal2;
                                changed = true;
                                DisplayPolicy.this.mHandler.postDelayed(DisplayPolicy.this.mClearHideNavigationFlag, 1000);
                            }
                            if (changed) {
                                DisplayPolicy.this.mDisplayContent.reevaluateStatusBarVisibility();
                            }
                        } else {
                            return;
                        }
                    }
                }
                finishInputEvent(event, false);
            } finally {
                finishInputEvent(event, false);
            }
        }
    }

    public void beginLayoutLw(DisplayFrames displayFrames, int uiMode) {
        boolean isNeedHideNaviBarWin;
        String windowName;
        boolean navVisible;
        WindowState windowState;
        WindowState windowState2;
        displayFrames.onBeginLayout();
        this.mDisplayRotation = displayFrames.mRotation;
        this.mSystemGestures.screenWidth = displayFrames.mUnrestricted.width();
        SystemGesturesPointerEventListener systemGesturesPointerEventListener = this.mSystemGestures;
        int height = displayFrames.mUnrestricted.height();
        systemGesturesPointerEventListener.screenHeight = height;
        this.mRestrictedScreenHeight = height;
        int sysui = this.mLastSystemUiFlags;
        boolean navVisible2 = (sysui & 2) == 0;
        boolean navTranslucent = (-2147450880 & sysui) != 0;
        boolean immersive = (sysui & 2048) != 0;
        boolean immersiveSticky = (sysui & 4096) != 0;
        WindowState windowState3 = this.mFocusedWindow;
        WindowManager.LayoutParams focusAttrs = windowState3 != null ? windowState3.getAttrs() : null;
        if (focusAttrs != null) {
            isNeedHideNaviBarWin = (focusAttrs.privateFlags & Integer.MIN_VALUE) != 0;
        } else {
            isNeedHideNaviBarWin = false;
        }
        boolean navAllowedHidden = immersive || immersiveSticky || isNeedHideNaviBarWin;
        boolean navTranslucent2 = navTranslucent & (!immersiveSticky);
        boolean isKeyguardShowing = isStatusBarKeyguard() && !this.mService.mPolicy.isKeyguardOccluded();
        IHwPhoneWindowManagerEx hwPWMEx = this.mService.getPolicy().getPhoneWindowManagerEx();
        boolean statusBarForcesShowingNavigation = !isKeyguardShowing && (windowState2 = this.mStatusBar) != null && (windowState2.getAttrs().privateFlags & 8388608) != 0 && (hwPWMEx == null || !hwPWMEx.getFPAuthState());
        if (navVisible2 || navAllowedHidden) {
            WindowManagerPolicy.InputConsumer inputConsumer = this.mInputConsumer;
            if (inputConsumer != null) {
                Handler handler = this.mHandler;
                handler.sendMessage(handler.obtainMessage(3, inputConsumer));
            }
        } else if (this.mInputConsumer == null && this.mStatusBar != null && canHideNavigationBar()) {
            this.mInputConsumer = this.mService.createInputConsumer(this.mHandler.getLooper(), "nav_input_consumer", (InputEventReceiver.Factory) new InputEventReceiver.Factory() {
                /* class com.android.server.wm.$$Lambda$DisplayPolicy$FpQuLkFb2EnHvk4Uzhr9G5Rn_xI */

                public final InputEventReceiver createInputEventReceiver(InputChannel inputChannel, Looper looper) {
                    return DisplayPolicy.this.lambda$beginLayoutLw$8$DisplayPolicy(inputChannel, looper);
                }
            }, displayFrames.mDisplayId);
            InputManager.getInstance().setPointerIconType(0);
        }
        boolean navVisible3 = navVisible2 | (!canHideNavigationBar());
        if (navVisible3 && mUsingHwNavibar) {
            navVisible3 = navVisible3 && !this.mHwDisplayPolicyEx.computeNaviBarFlag();
        }
        WindowState windowState4 = this.mFocusedWindow;
        if (windowState4 != null) {
            windowName = windowState4.toString();
        } else {
            windowName = null;
        }
        int type = focusAttrs != null ? focusAttrs.type : 0;
        boolean isKeyguardOn = (type == 2000 && ((focusAttrs != null ? focusAttrs.privateFlags : 0) & 1024) != 0) || type == 2101 || type == 2100;
        if (windowName == null || !windowName.contains("com.google.android.gms/com.google.android.gms.auth.login.ShowErrorActivity")) {
            navVisible = navVisible3;
        } else {
            navVisible = navVisible3;
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0) {
                navVisible = true;
            }
        }
        boolean updateSysUiVisibility = layoutNavigationBar(displayFrames, uiMode, navVisible, navTranslucent2, navAllowedHidden, isKeyguardOn, statusBarForcesShowingNavigation);
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.i(TAG, "mDock rect:" + displayFrames.mDock);
        }
        if (updateSysUiVisibility || layoutStatusBar(displayFrames, sysui, isKeyguardShowing)) {
            updateSystemUiVisibilityLw();
        }
        layoutScreenDecorWindows(displayFrames);
        if (displayFrames.mDisplayCutoutSafe.top > displayFrames.mUnrestricted.top) {
            displayFrames.mDisplayCutoutSafe.top = Math.max(displayFrames.mDisplayCutoutSafe.top, displayFrames.mStable.top);
        }
        if (HwMwUtils.ENABLED && (windowState = this.mFocusedWindow) != null) {
            HwMwUtils.performPolicy((int) WindowManagerService.H.PC_FREEZE_TIMEOUT, new Object[]{windowState});
        }
        this.mHwDisplayPolicyEx.beginLayoutForPC(displayFrames);
        displayFrames.mCurrent.inset(this.mForwardedInsets);
        displayFrames.mContent.inset(this.mForwardedInsets);
    }

    public /* synthetic */ InputEventReceiver lambda$beginLayoutLw$8$DisplayPolicy(InputChannel x$0, Looper x$1) {
        return new HideNavInputEventReceiver(x$0, x$1);
    }

    private void layoutScreenDecorWindows(DisplayFrames displayFrames) {
        DisplayPolicy displayPolicy = this;
        if (!displayPolicy.mScreenDecorWindows.isEmpty()) {
            sTmpRect.setEmpty();
            int displayId = displayFrames.mDisplayId;
            Rect dockFrame = displayFrames.mDock;
            int displayHeight = displayFrames.mDisplayHeight;
            int displayWidth = displayFrames.mDisplayWidth;
            int i = displayPolicy.mScreenDecorWindows.size() - 1;
            while (i >= 0) {
                WindowState w = displayPolicy.mScreenDecorWindows.valueAt(i);
                if (w.getDisplayId() == displayId && w.isVisibleLw()) {
                    w.getWindowFrames().setFrames(displayFrames.mUnrestricted, displayFrames.mUnrestricted, displayFrames.mUnrestricted, displayFrames.mUnrestricted, displayFrames.mUnrestricted, sTmpRect, displayFrames.mUnrestricted, displayFrames.mUnrestricted);
                    w.getWindowFrames().setDisplayCutout(displayFrames.mDisplayCutout);
                    w.computeFrameLw();
                    Rect frame = w.getFrameLw();
                    if (frame.left > 0 || frame.top > 0) {
                        if (frame.right < displayWidth || frame.bottom < displayHeight) {
                            Slog.w(TAG, "layoutScreenDecorWindows: Ignoring decor win=" + w + " not docked on one of the sides of the display. frame=" + frame + " displayWidth=" + displayWidth + " displayHeight=" + displayHeight);
                        } else if (frame.top <= 0) {
                            dockFrame.right = Math.min(frame.left, dockFrame.right);
                        } else if (frame.left <= 0) {
                            dockFrame.bottom = Math.min(frame.top, dockFrame.bottom);
                        } else {
                            Slog.w(TAG, "layoutScreenDecorWindows: Ignoring decor win=" + w + " not docked on right or bottom of display. frame=" + frame + " displayWidth=" + displayWidth + " displayHeight=" + displayHeight);
                        }
                    } else if (frame.bottom >= displayHeight) {
                        dockFrame.left = Math.max(frame.right, dockFrame.left);
                    } else if (frame.right >= displayWidth) {
                        dockFrame.top = Math.max(frame.bottom, dockFrame.top);
                    } else {
                        Slog.w(TAG, "layoutScreenDecorWindows: Ignoring decor win=" + w + " not docked on left or top of display. frame=" + frame + " displayWidth=" + displayWidth + " displayHeight=" + displayHeight);
                    }
                }
                i--;
                displayPolicy = this;
            }
            displayFrames.mRestricted.set(dockFrame);
            displayFrames.mCurrent.set(dockFrame);
            displayFrames.mVoiceContent.set(dockFrame);
            displayFrames.mSystem.set(dockFrame);
            displayFrames.mContent.set(dockFrame);
            displayFrames.mRestrictedOverscan.set(dockFrame);
        }
    }

    private boolean layoutStatusBar(DisplayFrames displayFrames, int sysui, boolean isKeyguardShowing) {
        if (this.mStatusBar == null) {
            return false;
        }
        sTmpRect.setEmpty();
        Rect offsetRect = new Rect(displayFrames.mUnrestricted);
        boolean isLandscape = HwDisplaySizeUtil.hasSideInScreen() && (displayFrames.mRotation == 1 || displayFrames.mRotation == 3);
        if (isLandscape) {
            if (displayFrames.mDisplaySideSafe.top != Integer.MIN_VALUE) {
                offsetRect.top += displayFrames.mDisplaySideSafe.top;
                offsetRect.bottom -= displayFrames.mDisplaySideSafe.top;
            } else {
                int sideWidth = HwDisplaySizeUtil.getInstance(this.mService).getSafeSideWidth();
                offsetRect.top += sideWidth;
                offsetRect.bottom -= sideWidth;
                Slog.e(TAG, "layoutStatusBar mDisplaySideSafe is invaild " + displayFrames.mDisplaySideSafe);
            }
        }
        WindowFrames windowFrames = this.mStatusBar.getWindowFrames();
        windowFrames.setFrames(displayFrames.mUnrestricted, offsetRect, displayFrames.mStable, displayFrames.mStable, displayFrames.mStable, sTmpRect, displayFrames.mStable, displayFrames.mStable);
        windowFrames.setDisplayCutout(displayFrames.mDisplayCutout);
        this.mStatusBar.computeFrameLw();
        displayFrames.mStable.top = displayFrames.mUnrestricted.top + this.mStatusBarHeightForRotation[displayFrames.mRotation];
        displayFrames.mStable.top = Math.max(displayFrames.mStable.top, displayFrames.mDisplayCutoutSafe.top);
        if (isLandscape) {
            if (displayFrames.mDisplaySideSafe.top != Integer.MIN_VALUE) {
                displayFrames.mStable.top += displayFrames.mDisplaySideSafe.top;
            } else {
                displayFrames.mStable.top += HwDisplaySizeUtil.getInstance(this.mService).getSafeSideWidth();
            }
        }
        sTmpRect.set(this.mStatusBar.getContentFrameLw());
        sTmpRect.intersect(displayFrames.mDisplayCutoutSafe);
        sTmpRect.top = this.mStatusBar.getContentFrameLw().top;
        sTmpRect.bottom = displayFrames.mStable.top;
        this.mStatusBarController.setContentFrame(sTmpRect);
        boolean statusBarTransient = (sysui & 67108864) != 0;
        boolean statusBarTranslucent = (sysui & 1073741832) != 0;
        if (this.mStatusBar.isVisibleLw() && !statusBarTransient) {
            Rect dockFrame = displayFrames.mDock;
            dockFrame.top = displayFrames.mStable.top;
            displayFrames.mContent.set(dockFrame);
            displayFrames.mVoiceContent.set(dockFrame);
            displayFrames.mCurrent.set(dockFrame);
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v(TAG, "Status bar: " + String.format("dock=%s content=%s cur=%s", dockFrame.toString(), displayFrames.mContent.toString(), displayFrames.mCurrent.toString()));
            }
            if (!statusBarTranslucent && !this.mStatusBarController.wasRecentlyTranslucent() && !this.mStatusBar.isAnimatingLw()) {
                displayFrames.mSystem.top = displayFrames.mStable.top;
            }
        }
        return this.mStatusBarController.checkHiddenLw();
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x00dc  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00f1  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x012a  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0139  */
    private boolean layoutNavigationBar(DisplayFrames displayFrames, int uiMode, boolean navVisible, boolean navTranslucent, boolean navAllowedHidden, boolean isKeyguardOn, boolean statusBarForcesShowingNavigation) {
        IHwPhoneWindowManagerEx hwPWMEx;
        int displayWidth;
        int bottom;
        int top;
        int bottom2;
        int top2;
        int displayWidth2;
        int right;
        int left;
        if (this.mNavigationBar == null) {
            return false;
        }
        Rect navigationFrame = sTmpNavFrame;
        boolean transientNavBarShowing = this.mNavigationBarController.isTransientShowing();
        int rotation = displayFrames.mRotation;
        int displayHeight = displayFrames.mDisplayHeight;
        int displayWidth3 = displayFrames.mDisplayWidth;
        Rect dockFrame = displayFrames.mDock;
        this.mNavigationBarPosition = navigationBarPosition(displayWidth3, displayHeight, rotation);
        Rect cutoutSafeUnrestricted = sTmpRect;
        cutoutSafeUnrestricted.set(displayFrames.mUnrestricted);
        IHwPhoneWindowManagerEx hwPWMEx2 = this.mService.getPolicy().getPhoneWindowManagerEx();
        boolean isNotchSwitchOpen = this.mService.getPolicy().isNotchDisplayDisabled();
        if (hwPWMEx2 == null || hwPWMEx2.isIntersectCutoutForNotch(displayFrames, isNotchSwitchOpen)) {
            cutoutSafeUnrestricted.intersectUnchecked(displayFrames.mDisplayCutoutSafe);
        }
        int i = this.mNavigationBarPosition;
        if (i == 4) {
            int top3 = cutoutSafeUnrestricted.bottom - getNavigationBarHeight(rotation, uiMode);
            int topNavBar = cutoutSafeUnrestricted.bottom - getNavigationBarFrameHeight(rotation, uiMode);
            if (CoordinationModeUtils.isFoldable()) {
                CoordinationModeUtils utils = CoordinationModeUtils.getInstance(this.mContext);
                hwPWMEx = hwPWMEx2;
                displayWidth2 = displayWidth3;
                if (utils.getCoordinationCreateMode() == 3) {
                    left = 0;
                    right = CoordinationModeUtils.getFoldScreenSubWidth();
                } else if ((utils.getCoordinationCreateMode() == 4 || utils.getCoordinationCreateMode() == 2) && utils.getCoordinationState() != 1) {
                    int left2 = CoordinationModeUtils.getFoldScreenEdgeWidth() + CoordinationModeUtils.getFoldScreenSubWidth();
                    left = left2;
                    right = left2 + CoordinationModeUtils.getFoldScreenMainWidth();
                }
                navigationFrame.set(left, topNavBar, right, (displayFrames.mUnrestricted.bottom + this.mHwDisplayPolicyEx.getNaviBarHeightForRotationMax(rotation)) - this.mNavigationBarHeightForRotationDefault[rotation]);
                if (!this.mHwDisplayPolicyEx.isNaviBarMini()) {
                    Rect rect = displayFrames.mStable;
                    Rect rect2 = displayFrames.mStableFullscreen;
                    int naviBarHeightForRotationMin = displayHeight - this.mHwDisplayPolicyEx.getNaviBarHeightForRotationMin(rotation);
                    rect2.bottom = naviBarHeightForRotationMin;
                    rect.bottom = naviBarHeightForRotationMin;
                } else {
                    if (HwDisplaySizeUtil.hasSideInScreen()) {
                        if (displayFrames.mRotation == 1 || displayFrames.mRotation == 3) {
                            int width = HwDisplaySizeUtil.getInstance(this.mService).getSafeSideWidth();
                            Rect rect3 = displayFrames.mStable;
                            int topNavBar2 = top3 - width;
                            displayFrames.mStableFullscreen.bottom = topNavBar2;
                            rect3.bottom = topNavBar2;
                        }
                    }
                    Rect rect4 = displayFrames.mStable;
                    displayFrames.mStableFullscreen.bottom = top3;
                    rect4.bottom = top3;
                }
                if (!transientNavBarShowing) {
                    this.mLastNaviStatus = 2;
                    this.mLastTransientNaviDockBottom = dockFrame.bottom;
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    if (this.mNavigationBarController.isTransientHiding()) {
                        Slog.v(TAG, "navigationbar is visible, but transientBarState is hiding, so reset a portrait screen");
                        this.mNavigationBarController.sethwTransientBarState(0);
                    }
                    this.mNavigationBarController.setBarShowingLw(true);
                    Rect rect5 = displayFrames.mRestricted;
                    Rect rect6 = displayFrames.mRestrictedOverscan;
                    int i2 = displayFrames.mStable.bottom;
                    rect6.bottom = i2;
                    rect5.bottom = i2;
                    dockFrame.bottom = i2;
                    this.mRestrictedScreenHeight = dockFrame.bottom - displayFrames.mRestricted.top;
                    this.mLastNaviStatus = 0;
                    this.mLastShowNaviDockBottom = dockFrame.bottom;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarForcesShowingNavigation);
                    if (isKeyguardOn) {
                        int i3 = this.mLastNaviStatus;
                        if (i3 == 0) {
                            int i4 = this.mLastShowNaviDockBottom;
                            if (i4 != 0) {
                                dockFrame.bottom = i4;
                                this.mRestrictedScreenHeight = dockFrame.bottom - displayFrames.mRestrictedOverscan.top;
                                displayFrames.mRestricted.bottom = dockFrame.bottom;
                                displayFrames.mRestrictedOverscan.bottom = dockFrame.bottom;
                            }
                        } else if (i3 == 1) {
                            int i5 = this.mLastHideNaviDockBottom;
                            if (i5 != 0) {
                                dockFrame.bottom = i5;
                                this.mRestrictedScreenHeight = dockFrame.bottom - displayFrames.mRestrictedOverscan.top;
                                displayFrames.mRestricted.bottom = dockFrame.bottom;
                                displayFrames.mRestrictedOverscan.bottom = dockFrame.bottom;
                            }
                        } else if (i3 != 2) {
                            Slog.v(TAG, "keyguard mLastNaviStatus is init");
                        } else {
                            int i6 = this.mLastTransientNaviDockBottom;
                            if (i6 != 0) {
                                dockFrame.bottom = i6;
                                this.mRestrictedScreenHeight = dockFrame.bottom - displayFrames.mRestrictedOverscan.top;
                                displayFrames.mRestricted.bottom = dockFrame.bottom;
                                displayFrames.mRestrictedOverscan.bottom = dockFrame.bottom;
                            }
                        }
                    } else {
                        this.mLastNaviStatus = 1;
                        this.mLastHideNaviDockBottom = dockFrame.bottom;
                    }
                }
                if (navVisible && !navTranslucent && !navAllowedHidden && !this.mNavigationBar.isAnimatingLw() && !this.mNavigationBarController.wasRecentlyTranslucent() && !mUsingHwNavibar) {
                    displayFrames.mSystem.bottom = displayFrames.mStable.bottom;
                }
                displayWidth = displayWidth2;
            } else {
                hwPWMEx = hwPWMEx2;
                displayWidth2 = displayWidth3;
            }
            left = 0;
            right = displayWidth3;
            navigationFrame.set(left, topNavBar, right, (displayFrames.mUnrestricted.bottom + this.mHwDisplayPolicyEx.getNaviBarHeightForRotationMax(rotation)) - this.mNavigationBarHeightForRotationDefault[rotation]);
            if (!this.mHwDisplayPolicyEx.isNaviBarMini()) {
            }
            if (!transientNavBarShowing) {
            }
            displayFrames.mSystem.bottom = displayFrames.mStable.bottom;
            displayWidth = displayWidth2;
        } else {
            hwPWMEx = hwPWMEx2;
            if (i == 2) {
                boolean isShowLeftNavBar = this.mService.getPolicy().getNavibarAlignLeftWhenLand();
                int left3 = cutoutSafeUnrestricted.right - getNavigationBarWidth(rotation, uiMode);
                if (!isShowLeftNavBar) {
                    if (CoordinationModeUtils.isFoldable()) {
                        CoordinationModeUtils utils2 = CoordinationModeUtils.getInstance(this.mContext);
                        top2 = 0;
                        bottom2 = displayHeight;
                        if (utils2.getCoordinationCreateMode() == 3) {
                            top = CoordinationModeUtils.getFoldScreenMainWidth() + CoordinationModeUtils.getFoldScreenEdgeWidth();
                            bottom = CoordinationModeUtils.getFoldScreenSubWidth() + top;
                        } else if (utils2.getCoordinationCreateMode() == 4 || utils2.getCoordinationCreateMode() == 2) {
                            top = 0;
                            bottom = CoordinationModeUtils.getFoldScreenMainWidth() + 0;
                        }
                        navigationFrame.set(left3, top, (displayFrames.mUnrestricted.right + this.mHwDisplayPolicyEx.getNaviBarWidthForRotationMax(rotation)) - this.mNavigationBarWidthForRotationDefault[rotation], bottom);
                    } else {
                        top2 = 0;
                        bottom2 = displayHeight;
                    }
                    top = top2;
                    bottom = bottom2;
                    navigationFrame.set(left3, top, (displayFrames.mUnrestricted.right + this.mHwDisplayPolicyEx.getNaviBarWidthForRotationMax(rotation)) - this.mNavigationBarWidthForRotationDefault[rotation], bottom);
                } else {
                    navigationFrame.set(0, 0, this.mContext.getResources().getDimensionPixelSize(34472115), displayHeight);
                }
                if (this.mHwDisplayPolicyEx.isNaviBarMini()) {
                    Rect rect7 = displayFrames.mStable;
                    Rect rect8 = displayFrames.mStableFullscreen;
                    int naviBarWidthForRotationMin = displayWidth3 - this.mHwDisplayPolicyEx.getNaviBarWidthForRotationMin(rotation);
                    rect8.right = naviBarWidthForRotationMin;
                    rect7.right = naviBarWidthForRotationMin;
                    displayWidth = displayWidth3;
                } else if (!isShowLeftNavBar) {
                    Rect rect9 = displayFrames.mStable;
                    displayFrames.mStableFullscreen.right = left3;
                    rect9.right = left3;
                    displayWidth = displayWidth3;
                } else {
                    Rect rect10 = displayFrames.mStable;
                    Rect rect11 = displayFrames.mStableFullscreen;
                    int i7 = navigationFrame.right;
                    rect11.left = i7;
                    rect10.left = i7;
                    displayWidth = displayWidth3;
                    displayFrames.mStable.right = displayWidth;
                }
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mLastNaviStatus = 2;
                } else if (navVisible) {
                    if (this.mNavigationBarController.isTransientHiding()) {
                        Slog.v(TAG, "navigationbar is visible, but transientBarState is hiding, so reset a landscape screen");
                        this.mNavigationBarController.sethwTransientBarState(0);
                    }
                    this.mNavigationBarController.setBarShowingLw(true);
                    dockFrame.right = displayFrames.mStable.right;
                    if (!isShowLeftNavBar) {
                        displayFrames.mRestricted.right = (displayFrames.mRestricted.left + dockFrame.right) - displayFrames.mRestricted.left;
                        displayFrames.mRestrictedOverscan.right = dockFrame.right - displayFrames.mRestrictedOverscan.left;
                    } else {
                        Rect rect12 = displayFrames.mRestricted;
                        int i8 = displayFrames.mStable.left;
                        dockFrame.left = i8;
                        rect12.left = i8;
                        displayFrames.mRestricted.right = displayFrames.mRestricted.left + displayFrames.mStable.right;
                        displayFrames.mRestricted.right = displayFrames.mRestricted.left + displayFrames.mStable.right;
                    }
                    this.mLastNaviStatus = 0;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarForcesShowingNavigation);
                    this.mLastNaviStatus = 1;
                }
                if (navVisible && !navTranslucent && !navAllowedHidden && !this.mNavigationBar.isAnimatingLw() && !this.mNavigationBarController.wasRecentlyTranslucent() && !mUsingHwNavibar) {
                    displayFrames.mSystem.right = displayFrames.mStable.right;
                }
            } else {
                displayWidth = displayWidth3;
                if (i == 1) {
                    int right2 = cutoutSafeUnrestricted.left + getNavigationBarWidth(rotation, uiMode);
                    navigationFrame.set(displayFrames.mUnrestricted.left, 0, right2, displayHeight);
                    Rect rect13 = displayFrames.mStable;
                    displayFrames.mStableFullscreen.left = right2;
                    rect13.left = right2;
                    if (transientNavBarShowing) {
                        this.mNavigationBarController.setBarShowingLw(true);
                    } else if (navVisible) {
                        this.mNavigationBarController.setBarShowingLw(true);
                        Rect rect14 = displayFrames.mRestricted;
                        displayFrames.mRestrictedOverscan.left = right2;
                        rect14.left = right2;
                        dockFrame.left = right2;
                    } else {
                        this.mNavigationBarController.setBarShowingLw(statusBarForcesShowingNavigation);
                    }
                    if (navVisible && !navTranslucent && !navAllowedHidden && !this.mNavigationBar.isAnimatingLw() && !this.mNavigationBarController.wasRecentlyTranslucent()) {
                        displayFrames.mSystem.left = right2;
                    }
                }
            }
        }
        displayFrames.mCurrent.set(dockFrame);
        displayFrames.mVoiceContent.set(dockFrame);
        displayFrames.mContent.set(dockFrame);
        sTmpRect.setEmpty();
        this.mNavigationBar.getWindowFrames().setFrames(navigationFrame, navigationFrame, navigationFrame, displayFrames.mDisplayCutoutSafe, navigationFrame, sTmpRect, navigationFrame, displayFrames.mDisplayCutoutSafe);
        this.mNavigationBar.getWindowFrames().setDisplayCutout(displayFrames.mDisplayCutout);
        this.mNavigationBar.computeFrameLw();
        this.mNavigationBarController.setContentFrame(this.mNavigationBar.getContentFrameLw());
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.i(TAG, "mNavigationBar frame: " + navigationFrame);
        }
        return this.mNavigationBarController.checkHiddenLw();
    }

    private void setAttachedWindowFrames(WindowState win, int fl, int adjust, WindowState attached, boolean insetDecors, Rect pf, Rect df, Rect of, Rect cf, Rect vf, DisplayFrames displayFrames) {
        if (win.isInputMethodTarget() || !attached.isInputMethodTarget()) {
            Rect parentDisplayFrame = attached.getDisplayFrameLw();
            Rect parentOverscan = attached.getOverscanFrameLw();
            WindowManager.LayoutParams attachedAttrs = attached.mAttrs;
            if ((attachedAttrs.privateFlags & 131072) != 0 && (attachedAttrs.flags & Integer.MIN_VALUE) == 0 && (attachedAttrs.systemUiVisibility & 512) == 0) {
                parentOverscan = new Rect(parentOverscan);
                parentOverscan.intersect(displayFrames.mRestrictedOverscan);
                parentDisplayFrame = new Rect(parentDisplayFrame);
                parentDisplayFrame.intersect(displayFrames.mRestrictedOverscan);
            }
            if (adjust != 16) {
                cf.set((1073741824 & fl) != 0 ? attached.getContentFrameLw() : parentOverscan);
            } else {
                cf.set(attached.getContentFrameLw());
                if (attached.isVoiceInteraction()) {
                    cf.intersectUnchecked(displayFrames.mVoiceContent);
                } else if (win.isInputMethodTarget() || attached.isInputMethodTarget()) {
                    cf.intersectUnchecked(displayFrames.mContent);
                }
            }
            df.set(insetDecors ? parentDisplayFrame : cf);
            of.set(insetDecors ? parentOverscan : cf);
            vf.set(attached.getVisibleFrameLw());
        } else {
            vf.set(displayFrames.mDock);
            cf.set(displayFrames.mDock);
            of.set(displayFrames.mDock);
            df.set(displayFrames.mDock);
        }
        pf.set((fl & 256) == 0 ? attached.getFrameLw() : df);
    }

    private void applyStableConstraints(int sysui, int fl, Rect r, DisplayFrames displayFrames) {
        if ((sysui & 256) != 0) {
            if ((fl & 1024) != 0) {
                r.intersectUnchecked(displayFrames.mStableFullscreen);
            } else {
                r.intersectUnchecked(displayFrames.mStable);
            }
        }
    }

    private boolean canReceiveInput(WindowState win) {
        return !(((win.getAttrs().flags & 8) != 0) ^ ((win.getAttrs().flags & 131072) != 0));
    }

    private boolean isSameImeHolder(WindowState win) {
        WindowState imeWin;
        if (this.mService.mImeHolder == null || (imeWin = this.mDisplayContent.mInputMethodWindow) == null || !imeWin.isVisibleLw()) {
            return false;
        }
        Task task = win.getTask();
        TaskStack imeTargetStack = this.mService.mImeHolder.getStack();
        if (imeTargetStack != null) {
            return imeTargetStack.hasChild(task);
        }
        Slog.e(TAG, "DisplayPolicy isSameImeHolder imeTargetStack is null");
        return false;
    }

    /* JADX INFO: Multiple debug info for r6v1 android.graphics.Rect: [D('sysUiFl' int), D('vf' android.graphics.Rect)] */
    /* JADX INFO: Multiple debug info for r6v2 android.graphics.Rect: [D('dcf' android.graphics.Rect), D('vf' android.graphics.Rect)] */
    /* JADX INFO: Multiple debug info for r10v2 android.graphics.Rect: [D('type' int), D('sf' android.graphics.Rect)] */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x0760, code lost:
        if (r13 <= 1999) goto L_0x0772;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:296:0x0768, code lost:
        if (r13 == 2009) goto L_0x0772;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:488:0x0c0e, code lost:
        if (r12.type != 2013) goto L_0x0c13;
     */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x0293  */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x029f  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x02a9  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x02b4  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x02f2  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0337  */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x0513  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x07f5  */
    /* JADX WARNING: Removed duplicated region for block: B:363:0x08a2  */
    /* JADX WARNING: Removed duplicated region for block: B:365:0x08b8  */
    /* JADX WARNING: Removed duplicated region for block: B:368:0x08c5  */
    /* JADX WARNING: Removed duplicated region for block: B:369:0x08df  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:517:0x0d25  */
    /* JADX WARNING: Removed duplicated region for block: B:518:0x0d2b  */
    /* JADX WARNING: Removed duplicated region for block: B:560:0x0e2c  */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0e45  */
    /* JADX WARNING: Removed duplicated region for block: B:573:0x0e58  */
    /* JADX WARNING: Removed duplicated region for block: B:574:0x0e5a  */
    /* JADX WARNING: Removed duplicated region for block: B:583:0x0e76  */
    /* JADX WARNING: Removed duplicated region for block: B:625:0x0f24  */
    /* JADX WARNING: Removed duplicated region for block: B:646:0x0f74  */
    /* JADX WARNING: Removed duplicated region for block: B:656:0x0f91  */
    /* JADX WARNING: Removed duplicated region for block: B:711:0x1087  */
    /* JADX WARNING: Removed duplicated region for block: B:712:0x1090  */
    /* JADX WARNING: Removed duplicated region for block: B:718:0x10ad  */
    /* JADX WARNING: Removed duplicated region for block: B:749:0x1192  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x013a  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x013c  */
    /* JADX WARNING: Removed duplicated region for block: B:777:0x123a  */
    /* JADX WARNING: Removed duplicated region for block: B:778:0x12f3  */
    /* JADX WARNING: Removed duplicated region for block: B:781:0x12ff  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0146  */
    /* JADX WARNING: Removed duplicated region for block: B:808:0x139f  */
    /* JADX WARNING: Removed duplicated region for block: B:809:0x13a7  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0148  */
    public void layoutWindowLw(WindowState win, WindowState attached, DisplayFrames displayFrames) {
        boolean isNeedHideNaviBarWin;
        boolean isPCDisplay;
        boolean hasNavBar;
        boolean isNeedHideNaviBarWin2;
        Rect of;
        int fl;
        Rect dcf;
        WindowFrames windowFrames;
        Rect sf;
        int sim;
        DisplayPolicy displayPolicy;
        DisplayFrames displayFrames2;
        int type;
        WindowManager.LayoutParams attrs;
        Rect of2;
        WindowState windowState;
        Rect pf;
        Rect df;
        Rect vf;
        int i;
        Rect dcf2;
        int cutoutMode;
        Rect dcf3;
        int fl2;
        WindowFrames windowFrames2;
        boolean layoutBelowNotch;
        int cutoutMode2;
        Rect of3;
        boolean layoutBelowNotch2;
        Rect dcf4;
        WindowState windowState2;
        WindowState windowState3;
        WindowFrames windowFrames3;
        WindowState windowState4;
        IHwDisplayPolicyEx iHwDisplayPolicyEx;
        boolean skipSetUnLimitHeight;
        Rect displayCutoutSafeExceptMaybeBars;
        int i2;
        int i3;
        Rect pf2;
        int fl3;
        int adjust;
        int fl4;
        int i4;
        int adjust2;
        Rect of4;
        int fl5;
        int i5;
        int sysUiFl;
        int sysUiFl2;
        int fl6;
        Rect of5;
        Rect df2;
        Rect pf3;
        int sysUiFl3;
        int i6;
        Rect cf;
        int adjust3;
        DisplayPolicy displayPolicy2;
        int i7;
        WindowManager.LayoutParams attrs2;
        Rect vf2;
        Throwable th;
        int i8;
        int i9;
        Rect pf4;
        Rect dcf5;
        WindowState windowState5;
        Rect rect;
        WindowState windowState6;
        WindowManager.LayoutParams attrs3;
        int type2;
        int fl7;
        int adjust4;
        WindowFrames windowFrames4;
        Rect dcf6;
        Rect vf3;
        int sysUiFl4;
        Rect cf2;
        Rect of6;
        Rect df3;
        Rect sf2;
        WindowState windowState7;
        if (!this.mHwDisplayPolicyEx.layoutWindowForPCNavigationBar(win)) {
            if (win == this.mStatusBar && !canReceiveInput(win)) {
                return;
            }
            if (win == this.mNavigationBar) {
                return;
            }
            if (!this.mScreenDecorWindows.contains(win)) {
                WindowManager.LayoutParams attrs4 = win.getAttrs();
                boolean isDefaultDisplay = win.isDefaultDisplay();
                int type3 = attrs4.type;
                int fl8 = PolicyControl.getWindowFlags(win, attrs4);
                int pfl = attrs4.privateFlags;
                int sim2 = attrs4.softInputMode;
                int requestedSysUiFl = PolicyControl.getSystemUiVisibility(null, attrs4);
                int sysUiFl5 = requestedSysUiFl | getImpliedSysUiFlagsForLayout(attrs4);
                WindowFrames windowFrames5 = win.getWindowFrames();
                windowFrames5.setHasOutsets(false);
                sTmpLastParentFrame.set(windowFrames5.mParentFrame);
                Rect pf5 = windowFrames5.mParentFrame;
                Rect df4 = windowFrames5.mDisplayFrame;
                Rect of7 = windowFrames5.mOverscanFrame;
                Rect cf3 = windowFrames5.mContentFrame;
                Rect vf4 = windowFrames5.mVisibleFrame;
                Rect vf5 = windowFrames5.mDecorFrame;
                Rect sf3 = windowFrames5.mStableFrame;
                vf5.setEmpty();
                windowFrames5.setParentFrameWasClippedByDisplayCutout(false);
                windowFrames5.setDisplayCutout(displayFrames.mDisplayCutout);
                if (mUsingHwNavibar) {
                    if ((attrs4.privateFlags & Integer.MIN_VALUE) != 0) {
                        isNeedHideNaviBarWin = true;
                        if (!HwPCUtils.isPcCastModeInServer()) {
                            isPCDisplay = HwPCUtils.isValidExtDisplayId(win.getDisplayId());
                        } else {
                            isPCDisplay = false;
                        }
                        if (HwPCUtils.isPcCastModeInServer() || isDefaultDisplay || !isPCDisplay) {
                            isNeedHideNaviBarWin2 = isNeedHideNaviBarWin;
                            hasNavBar = !hasNavigationBar() && (windowState7 = this.mNavigationBar) != null && windowState7.isVisibleLw();
                        } else {
                            isNeedHideNaviBarWin2 = false;
                            hasNavBar = hasNavigationBar() && getNavigationBarExternal() != null && getNavigationBarExternal().isVisibleLw();
                        }
                        boolean isBaiDuOrSwiftkey = isBaiDuOrSwiftkey(win);
                        boolean isLeftRightSplitStackVisible = isLeftRightSplitStackVisible();
                        boolean isDocked = false;
                        if (attrs4.type == 2) {
                            isDocked = isLeftRightSplitStackVisible && win != this.mAloneTarget;
                        }
                        boolean isLeftRightSplitStack = !isLeftRightSplitStackVisible && (win.inHwSplitScreenWindowingMode() || win.inSplitScreenWindowingMode());
                        int adjust5 = (isLeftRightSplitStackVisible || isSameImeHolder(win)) ? sim2 & 240 : 48;
                        boolean requestedFullscreen = (fl8 & 1024) == 0 || (requestedSysUiFl & 4) != 0;
                        boolean layoutInScreen = (fl8 & 256) != 256;
                        boolean layoutInsetDecor = (65536 & fl8) != 65536;
                        sf3.set(displayFrames.mStable);
                        if (HwPCUtils.isPcCastModeInServer() || !isPCDisplay) {
                            sim = sim2;
                            displayFrames2 = displayFrames;
                            if (type3 != 2011) {
                                WindowState windowState8 = this.mInputMethodTarget;
                                if (!(windowState8 == null || !windowState8.inHwMagicWindowingMode() || (windowState6 = this.mFocusedWindow) == null || windowState6.getAttrs() == null || (this.mFocusedWindow.getAttrs().flags & 1024) == 0)) {
                                    displayFrames2.mStable.top = 0;
                                    sf3.top = 0;
                                    Rect rect2 = displayFrames2.mContent;
                                    Rect rect3 = displayFrames2.mCurrent;
                                    displayFrames2.mDock.top = 0;
                                    rect3.top = 0;
                                    rect2.top = 0;
                                }
                                vf4.set(displayFrames2.mDock);
                                cf3.set(displayFrames2.mDock);
                                of7.set(displayFrames2.mDock);
                                df4.set(displayFrames2.mDock);
                                windowFrames5.mParentFrame.set(displayFrames2.mDock);
                                boolean isPopIME = (this.mAloneTarget == null || (win.getAttrs().hwFlags & 1048576) == 0) ? false : true;
                                if (!isLeftRightSplitStackVisible || (windowState5 = this.mAloneTarget) == null || !isBaiDuOrSwiftkey || isPopIME) {
                                    pf4 = pf5;
                                } else if (!windowState5.inSplitScreenWindowingMode() && !this.mAloneTarget.inHwSplitScreenWindowingMode()) {
                                    pf4 = pf5;
                                } else if (IS_HW_MULTIWINDOW_SUPPORTED && !this.mService.mAtmService.mHwATMSEx.isPhoneLandscape(win.getDisplayContent())) {
                                    pf4 = pf5;
                                } else if (!this.mFrozen) {
                                    if (this.mAloneTarget.getAttrs().type == 2) {
                                        rect = this.mAloneTarget.getDisplayFrameLw();
                                    } else {
                                        rect = this.mAloneTarget.getContentFrameLw();
                                    }
                                    int i10 = rect.left;
                                    vf4.left = i10;
                                    cf3.left = i10;
                                    of7.left = i10;
                                    df4.left = i10;
                                    pf4 = pf5;
                                    pf4.left = i10;
                                    int i11 = rect.right;
                                    vf4.right = i11;
                                    cf3.right = i11;
                                    of7.right = i11;
                                    df4.right = i11;
                                    pf4.right = i11;
                                } else {
                                    return;
                                }
                                int min = Math.min(displayFrames2.mUnrestricted.bottom, displayFrames2.mDisplayCutoutSafe.bottom);
                                of7.bottom = min;
                                df4.bottom = min;
                                pf4.bottom = min;
                                int i12 = displayFrames2.mStable.bottom;
                                vf4.bottom = i12;
                                cf3.bottom = i12;
                                WindowState windowState9 = this.mFocusedWindow;
                                WindowManager.LayoutParams focusAttrs = windowState9 != null ? windowState9.getAttrs() : null;
                                WindowState windowState10 = this.mInputMethodTarget;
                                WindowManager.LayoutParams inputMethodAttrs = (windowState10 == null && (windowState10 = this.mStatusBar) == null) ? null : windowState10.getAttrs();
                                if (!(focusAttrs == null || (focusAttrs.privateFlags & 1024) == 0 || hasNavBar)) {
                                    int i13 = displayFrames2.mSystem.bottom;
                                    of7.bottom = i13;
                                    vf4.bottom = i13;
                                    cf3.bottom = i13;
                                    df4.bottom = i13;
                                    pf4.bottom = i13;
                                }
                                if (!(inputMethodAttrs == null || !SEC_IME_PACKAGE.equals(win.getOwningPackage()) || (inputMethodAttrs.hwFlags & 32) == 0)) {
                                    int calculateSecImeRaisePx = displayFrames2.mSystem.bottom - calculateSecImeRaisePx(this.mContext);
                                    of7.bottom = calculateSecImeRaisePx;
                                    vf4.bottom = calculateSecImeRaisePx;
                                    cf3.bottom = calculateSecImeRaisePx;
                                    df4.bottom = calculateSecImeRaisePx;
                                    pf4.bottom = calculateSecImeRaisePx;
                                }
                                WindowState windowState11 = this.mStatusBar;
                                if (windowState11 != null && this.mFocusedWindow == windowState11 && canReceiveInput(windowState11)) {
                                    int i14 = this.mNavigationBarPosition;
                                    if (i14 == 2) {
                                        int i15 = displayFrames2.mStable.right;
                                        vf4.right = i15;
                                        cf3.right = i15;
                                        of7.right = i15;
                                        df4.right = i15;
                                        pf4.right = i15;
                                    } else if (i14 == 1) {
                                        int i16 = displayFrames2.mStable.left;
                                        vf4.left = i16;
                                        cf3.left = i16;
                                        of7.left = i16;
                                        df4.left = i16;
                                        pf4.left = i16;
                                    }
                                }
                                if (this.mNavigationBarPosition == 4) {
                                    int rotation = displayFrames2.mRotation;
                                    int uimode = this.mService.mPolicy.getUiMode();
                                    int navHeightOffset = getNavigationBarFrameHeight(rotation, uimode) - getNavigationBarHeight(rotation, uimode);
                                    if (navHeightOffset > 0) {
                                        cf3.bottom -= navHeightOffset;
                                        sf3.bottom -= navHeightOffset;
                                        vf4.bottom -= navHeightOffset;
                                        dcf5 = vf5;
                                        dcf5.bottom -= navHeightOffset;
                                    } else {
                                        dcf5 = vf5;
                                    }
                                } else {
                                    dcf5 = vf5;
                                }
                                attrs4.gravity = 80;
                                dcf = dcf5;
                                windowFrames = windowFrames5;
                                of = of7;
                                vf = cf3;
                                sf = sf3;
                                fl = fl8;
                                i = 1;
                                attrs = attrs4;
                                of2 = vf4;
                                df = df4;
                                displayPolicy = this;
                                windowState = win;
                                type = type3;
                                pf = pf4;
                            } else if (type3 == 2031) {
                                of7.set(displayFrames2.mUnrestricted);
                                df4.set(displayFrames2.mUnrestricted);
                                pf5.set(displayFrames2.mUnrestricted);
                                if (adjust5 != 16) {
                                    cf3.set(displayFrames2.mDock);
                                } else {
                                    cf3.set(displayFrames2.mContent);
                                }
                                if (adjust5 != 48) {
                                    vf4.set(displayFrames2.mCurrent);
                                    dcf = vf5;
                                    windowFrames = windowFrames5;
                                    of = of7;
                                    vf = cf3;
                                    sf = sf3;
                                    fl = fl8;
                                    i = 1;
                                    attrs = attrs4;
                                    of2 = vf4;
                                    df = df4;
                                    displayPolicy = this;
                                    windowState = win;
                                    type = type3;
                                    pf = pf5;
                                } else {
                                    vf4.set(cf3);
                                    dcf = vf5;
                                    windowFrames = windowFrames5;
                                    of = of7;
                                    vf = cf3;
                                    sf = sf3;
                                    fl = fl8;
                                    i = 1;
                                    attrs = attrs4;
                                    of2 = vf4;
                                    df = df4;
                                    displayPolicy = this;
                                    windowState = win;
                                    type = type3;
                                    pf = pf5;
                                }
                            } else {
                                if (type3 == 2013) {
                                    dcf = vf5;
                                    windowFrames = windowFrames5;
                                    vf = cf3;
                                    sf = sf3;
                                    fl3 = fl8;
                                    attrs = attrs4;
                                    displayPolicy = this;
                                    type = type3;
                                    pf = pf5;
                                    pf2 = of7;
                                    of2 = vf4;
                                    df = df4;
                                    i3 = adjust5;
                                    adjust = sysUiFl5;
                                } else if (type3 != 2103 || HwPCUtils.isPcCastModeInServer()) {
                                    sf = sf3;
                                    if (win == this.mStatusBar) {
                                        of7.set(displayFrames2.mUnrestricted);
                                        df4.set(displayFrames2.mUnrestricted);
                                        pf5.set(displayFrames2.mUnrestricted);
                                        cf3.set(displayFrames2.mStable);
                                        vf4.set(displayFrames2.mStable);
                                        if (adjust5 == 16) {
                                            cf3.bottom = displayFrames2.mContent.bottom;
                                            dcf = vf5;
                                            windowFrames = windowFrames5;
                                            of = of7;
                                            vf = cf3;
                                            displayPolicy = this;
                                            fl = fl8;
                                            i = 1;
                                            attrs = attrs4;
                                            of2 = vf4;
                                            type = type3;
                                            df = df4;
                                            windowState = win;
                                            pf = pf5;
                                        } else {
                                            cf3.bottom = displayFrames2.mDock.bottom;
                                            vf4.bottom = displayFrames2.mContent.bottom;
                                            dcf = vf5;
                                            windowFrames = windowFrames5;
                                            of = of7;
                                            vf = cf3;
                                            displayPolicy = this;
                                            fl = fl8;
                                            i = 1;
                                            attrs = attrs4;
                                            of2 = vf4;
                                            type = type3;
                                            df = df4;
                                            windowState = win;
                                            pf = pf5;
                                        }
                                    } else {
                                        vf5.set(displayFrames2.mSystem);
                                        boolean inheritTranslucentDecor = (attrs4.privateFlags & 512) != 0;
                                        boolean isAppWindow = type3 >= 1 && type3 <= 99;
                                        boolean topAtRest = win == this.mTopFullscreenOpaqueWindowState && !win.isAnimatingLw();
                                        if (!isAppWindow || inheritTranslucentDecor || topAtRest) {
                                            fl4 = fl8;
                                            i4 = Integer.MIN_VALUE;
                                        } else {
                                            if ((sysUiFl5 & 4) == 0) {
                                                fl4 = fl8;
                                                if ((fl4 & 1024) == 0 && (67108864 & fl4) == 0) {
                                                    i4 = Integer.MIN_VALUE;
                                                    if ((fl4 & Integer.MIN_VALUE) == 0 && (pfl & 131072) == 0 && !isLeftRightSplitStack) {
                                                        vf5.top = displayFrames2.mStable.top;
                                                    }
                                                } else {
                                                    i4 = Integer.MIN_VALUE;
                                                }
                                            } else {
                                                fl4 = fl8;
                                                i4 = Integer.MIN_VALUE;
                                            }
                                            if ((134217728 & fl4) == 0 && (sysUiFl5 & 2) == 0 && (fl4 & i4) == 0 && (pfl & 131072) == 0) {
                                                vf5.bottom = displayFrames2.mStable.bottom;
                                                vf5.right = displayFrames2.mStable.right;
                                            }
                                        }
                                        if (!layoutInScreen || !layoutInsetDecor) {
                                            dcf = vf5;
                                            windowFrames = windowFrames5;
                                            type = type3;
                                            if (layoutInScreen) {
                                                sysUiFl2 = sysUiFl5;
                                                pf = pf5;
                                                fl6 = fl4;
                                                displayPolicy = this;
                                                adjust2 = adjust5;
                                                vf = cf3;
                                                df = df4;
                                                of4 = of7;
                                                of2 = vf4;
                                                attrs = attrs4;
                                            } else if ((sysUiFl5 & 1536) != 0) {
                                                sysUiFl2 = sysUiFl5;
                                                pf = pf5;
                                                fl6 = fl4;
                                                displayPolicy = this;
                                                adjust2 = adjust5;
                                                vf = cf3;
                                                df = df4;
                                                of4 = of7;
                                                of2 = vf4;
                                                attrs = attrs4;
                                            } else if (attached != null) {
                                                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                                    Slog.v(TAG, "layoutWindowLw(" + ((Object) attrs4.getTitle()) + "): attached to " + attached);
                                                }
                                                of2 = vf4;
                                                setAttachedWindowFrames(win, fl4, adjust5, attached, false, pf5, df4, of7, cf3, of2, displayFrames);
                                                of4 = of7;
                                                vf = cf3;
                                                fl5 = fl4;
                                                attrs = attrs4;
                                                adjust2 = adjust5;
                                                sysUiFl = sysUiFl5;
                                                pf = pf5;
                                                df = df4;
                                                i5 = 1;
                                                displayPolicy = this;
                                            } else {
                                                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                                    Slog.v(TAG, "layoutWindowLw(" + ((Object) attrs4.getTitle()) + "): normal window");
                                                }
                                                if (type == 2014) {
                                                    vf = cf3;
                                                    vf.set(displayFrames2.mRestricted);
                                                    of7.set(displayFrames2.mRestricted);
                                                    df = df4;
                                                    df.set(displayFrames2.mRestricted);
                                                    pf = pf5;
                                                    pf.set(displayFrames2.mRestricted);
                                                    of4 = of7;
                                                    of2 = vf4;
                                                    fl5 = fl4;
                                                    attrs = attrs4;
                                                    adjust2 = adjust5;
                                                    sysUiFl = sysUiFl5;
                                                    i5 = 1;
                                                    displayPolicy = this;
                                                } else {
                                                    vf = cf3;
                                                    pf = pf5;
                                                    df = df4;
                                                    if (type == 2005) {
                                                        of2 = vf4;
                                                        adjust2 = adjust5;
                                                    } else if (type == 2003) {
                                                        of2 = vf4;
                                                        adjust2 = adjust5;
                                                    } else {
                                                        pf.set(displayFrames2.mContent);
                                                        if (isDocked) {
                                                            pf.bottom = displayFrames2.mDock.bottom;
                                                        }
                                                        if (win.isVoiceInteraction()) {
                                                            vf.set(displayFrames2.mVoiceContent);
                                                            of7.set(displayFrames2.mVoiceContent);
                                                            df.set(displayFrames2.mVoiceContent);
                                                            adjust2 = adjust5;
                                                        } else {
                                                            adjust2 = adjust5;
                                                            if (adjust2 != 16) {
                                                                vf.set(displayFrames2.mDock);
                                                                of7.set(displayFrames2.mDock);
                                                                df.set(displayFrames2.mDock);
                                                            } else {
                                                                vf.set(displayFrames2.mContent);
                                                                of7.set(displayFrames2.mContent);
                                                                df.set(displayFrames2.mContent);
                                                            }
                                                        }
                                                        if (adjust2 != 48) {
                                                            of2 = vf4;
                                                            of2.set(displayFrames2.mCurrent);
                                                            of4 = of7;
                                                            fl5 = fl4;
                                                            attrs = attrs4;
                                                            sysUiFl = sysUiFl5;
                                                            i5 = 1;
                                                            displayPolicy = this;
                                                        } else {
                                                            of2 = vf4;
                                                            of2.set(vf);
                                                            of4 = of7;
                                                            fl5 = fl4;
                                                            attrs = attrs4;
                                                            sysUiFl = sysUiFl5;
                                                            i5 = 1;
                                                            displayPolicy = this;
                                                        }
                                                    }
                                                    vf.set(displayFrames2.mStable);
                                                    of7.set(displayFrames2.mStable);
                                                    df.set(displayFrames2.mStable);
                                                    attrs = attrs4;
                                                    if (attrs.type == 2003) {
                                                        pf.set(displayFrames2.mCurrent);
                                                    } else {
                                                        pf.set(displayFrames2.mStable);
                                                    }
                                                    if (CoordinationModeUtils.isFoldable()) {
                                                        of4 = of7;
                                                        displayPolicy = this;
                                                        int createState = CoordinationModeUtils.getInstance(displayPolicy.mContext).getCoordinationCreateMode();
                                                        if (displayFrames2.mRotation == 0 || displayFrames2.mRotation == 2) {
                                                            if (createState == 3) {
                                                                int foldScreenSubWidth = CoordinationModeUtils.getFoldScreenSubWidth();
                                                                of2.right = foldScreenSubWidth;
                                                                pf.right = foldScreenSubWidth;
                                                                df.right = foldScreenSubWidth;
                                                                of4.right = foldScreenSubWidth;
                                                            } else if (createState == 4 || createState == 2) {
                                                                int foldScreenEdgeWidth = CoordinationModeUtils.getFoldScreenEdgeWidth() + CoordinationModeUtils.getFoldScreenSubWidth();
                                                                of2.left = foldScreenEdgeWidth;
                                                                pf.left = foldScreenEdgeWidth;
                                                                df.left = foldScreenEdgeWidth;
                                                                of4.left = foldScreenEdgeWidth;
                                                            }
                                                        } else if (createState == 3) {
                                                            int foldScreenSubWidth2 = CoordinationModeUtils.getFoldScreenSubWidth();
                                                            of2.top = foldScreenSubWidth2;
                                                            pf.top = foldScreenSubWidth2;
                                                            df.top = foldScreenSubWidth2;
                                                            of4.top = foldScreenSubWidth2;
                                                        } else if (createState == 4 || createState == 2) {
                                                            int foldScreenEdgeWidth2 = CoordinationModeUtils.getFoldScreenEdgeWidth() + CoordinationModeUtils.getFoldScreenSubWidth();
                                                            of2.top = foldScreenEdgeWidth2;
                                                            pf.top = foldScreenEdgeWidth2;
                                                            df.top = foldScreenEdgeWidth2;
                                                            of4.top = foldScreenEdgeWidth2;
                                                        }
                                                        fl5 = fl4;
                                                        sysUiFl = sysUiFl5;
                                                        i5 = 1;
                                                    } else {
                                                        of4 = of7;
                                                        displayPolicy = this;
                                                        fl5 = fl4;
                                                        sysUiFl = sysUiFl5;
                                                        i5 = 1;
                                                    }
                                                }
                                            }
                                            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                                Slog.v(TAG, "layoutWindowLw(" + ((Object) attrs.getTitle()) + "): IN_SCREEN");
                                            }
                                            if (type == 2014) {
                                                fl5 = fl6;
                                                sysUiFl = sysUiFl2;
                                                i5 = 1;
                                            } else if (type == 2017) {
                                                fl5 = fl6;
                                                sysUiFl = sysUiFl2;
                                                i5 = 1;
                                            } else {
                                                if (type == 2019) {
                                                    fl5 = fl6;
                                                    sysUiFl = sysUiFl2;
                                                    i5 = 1;
                                                } else if (type == 2024) {
                                                    fl5 = fl6;
                                                    sysUiFl = sysUiFl2;
                                                    i5 = 1;
                                                } else {
                                                    if (type == 2015 || type == 2036) {
                                                        fl5 = fl6;
                                                        if ((fl5 & 1024) != 0) {
                                                            vf.set(displayFrames2.mOverscan);
                                                            of4.set(displayFrames2.mOverscan);
                                                            df.set(displayFrames2.mOverscan);
                                                            pf.set(displayFrames2.mOverscan);
                                                            sysUiFl = sysUiFl2;
                                                            i5 = 1;
                                                            displayPolicy.applyStableConstraints(sysUiFl, fl5, vf, displayFrames2);
                                                            if (adjust2 == 48) {
                                                                of2.set(displayFrames2.mCurrent);
                                                            } else {
                                                                of2.set(vf);
                                                            }
                                                        }
                                                    } else {
                                                        fl5 = fl6;
                                                    }
                                                    if (type == 2021) {
                                                        vf.set(displayFrames2.mOverscan);
                                                        of4.set(displayFrames2.mOverscan);
                                                        df.set(displayFrames2.mOverscan);
                                                        pf.set(displayFrames2.mOverscan);
                                                        sysUiFl = sysUiFl2;
                                                        i5 = 1;
                                                    } else {
                                                        if ((33554432 & fl5) != 0) {
                                                            i5 = 1;
                                                            if (type >= 1 && type <= 1999) {
                                                                vf.set(displayFrames2.mOverscan);
                                                                of4.set(displayFrames2.mOverscan);
                                                                df.set(displayFrames2.mOverscan);
                                                                pf.set(displayFrames2.mOverscan);
                                                                sysUiFl = sysUiFl2;
                                                            }
                                                        } else {
                                                            i5 = 1;
                                                        }
                                                        if (canHideNavigationBar()) {
                                                            sysUiFl = sysUiFl2;
                                                            if ((sysUiFl & 512) != 0 && (type == 2000 || type == 2005 || type == 2034 || type == 2033 || (type >= i5 && type <= 1999))) {
                                                                vf.set(displayFrames2.mUnrestricted);
                                                                of4.set(displayFrames2.mUnrestricted);
                                                                df.set(displayFrames2.mUnrestricted);
                                                                pf.set(displayFrames2.mUnrestricted);
                                                            }
                                                        } else {
                                                            sysUiFl = sysUiFl2;
                                                        }
                                                        if (mUsingHwNavibar) {
                                                        }
                                                        if (!isNeedHideNaviBarWin2) {
                                                            if ("com.huawei.systemui.mk.lighterdrawer.LighterDrawView".equals(attrs.getTitle())) {
                                                                vf.set(displayFrames2.mUnrestricted);
                                                                of4.set(displayFrames2.mUnrestricted);
                                                                df.set(displayFrames2.mUnrestricted);
                                                                pf.set(displayFrames2.mUnrestricted);
                                                                of2.set(displayFrames2.mUnrestricted);
                                                            } else if ((sysUiFl & 1024) != 0) {
                                                                of4.set(displayFrames2.mRestricted);
                                                                df.set(displayFrames2.mRestricted);
                                                                pf.set(displayFrames2.mRestricted);
                                                                if (ViewRootImpl.sNewInsetsMode == 0 && adjust2 == 16) {
                                                                    vf.set(displayFrames2.mContent);
                                                                } else {
                                                                    vf.set(displayFrames2.mDock);
                                                                }
                                                            } else {
                                                                vf.set(displayFrames2.mRestricted);
                                                                of4.set(displayFrames2.mRestricted);
                                                                df.set(displayFrames2.mRestricted);
                                                                pf.set(displayFrames2.mRestricted);
                                                            }
                                                        }
                                                        vf.set(displayFrames2.mUnrestricted);
                                                        of4.set(displayFrames2.mUnrestricted);
                                                        df.set(displayFrames2.mUnrestricted);
                                                        pf.set(displayFrames2.mUnrestricted);
                                                    }
                                                    displayPolicy.applyStableConstraints(sysUiFl, fl5, vf, displayFrames2);
                                                    if (adjust2 == 48) {
                                                    }
                                                }
                                                of4.set(displayFrames2.mUnrestricted);
                                                df.set(displayFrames2.mUnrestricted);
                                                pf.set(displayFrames2.mUnrestricted);
                                                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                                    Slog.v(TAG, "Laying out navigation bar window: " + pf);
                                                }
                                                displayPolicy.applyStableConstraints(sysUiFl, fl5, vf, displayFrames2);
                                                if (adjust2 == 48) {
                                                }
                                            }
                                            vf.set(displayFrames2.mUnrestricted);
                                            of4.set(displayFrames2.mUnrestricted);
                                            df.set(displayFrames2.mUnrestricted);
                                            pf.set(displayFrames2.mUnrestricted);
                                            if (hasNavBar) {
                                                int i17 = displayFrames2.mDock.left;
                                                vf.left = i17;
                                                of4.left = i17;
                                                df.left = i17;
                                                pf.left = i17;
                                                int i18 = displayFrames2.mRestricted.right;
                                                vf.right = i18;
                                                of4.right = i18;
                                                df.right = i18;
                                                pf.right = i18;
                                                int i19 = displayFrames2.mRestricted.bottom;
                                                vf.bottom = i19;
                                                of4.bottom = i19;
                                                df.bottom = i19;
                                                pf.bottom = i19;
                                            }
                                            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                                Slog.v(TAG, "Laying out IN_SCREEN status bar window: " + pf);
                                            }
                                            displayPolicy.applyStableConstraints(sysUiFl, fl5, vf, displayFrames2);
                                            if (adjust2 == 48) {
                                            }
                                        } else {
                                            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                                Slog.v(TAG, "layoutWindowLw(" + ((Object) attrs4.getTitle()) + "): IN_SCREEN, INSET_DECOR");
                                            }
                                            if (attached != null) {
                                                windowFrames = windowFrames5;
                                                type = type3;
                                                of2 = vf4;
                                                setAttachedWindowFrames(win, fl4, adjust5, attached, true, pf5, df4, of7, cf3, of2, displayFrames);
                                                fl5 = fl4;
                                                pf = pf5;
                                                adjust2 = adjust5;
                                                sysUiFl = sysUiFl5;
                                                attrs = attrs4;
                                                dcf = vf5;
                                                df = df4;
                                                of4 = of7;
                                                vf = cf3;
                                                i5 = 1;
                                                displayPolicy = this;
                                            } else {
                                                windowFrames = windowFrames5;
                                                type = type3;
                                                if (type == 2014) {
                                                    pf3 = pf5;
                                                    sysUiFl3 = sysUiFl5;
                                                    df2 = df4;
                                                    of5 = of7;
                                                    i6 = 1;
                                                } else if (type == 2017) {
                                                    pf3 = pf5;
                                                    sysUiFl3 = sysUiFl5;
                                                    df2 = df4;
                                                    of5 = of7;
                                                    i6 = 1;
                                                } else if ((33554432 & fl4) == 0 || type < 1 || type > 1999) {
                                                    pf3 = pf5;
                                                    df2 = df4;
                                                    of5 = of7;
                                                    if (canHideNavigationBar()) {
                                                        sysUiFl3 = sysUiFl5;
                                                        if ((sysUiFl3 & 512) != 0) {
                                                            i6 = 1;
                                                            if (type >= 1) {
                                                            }
                                                            if (type != 2020) {
                                                            }
                                                            df2.set(displayFrames2.mOverscan);
                                                            pf3.set(displayFrames2.mOverscan);
                                                            of5.set(displayFrames2.mUnrestricted);
                                                            if ((fl4 & 1024) == 0) {
                                                                if (win.isVoiceInteraction()) {
                                                                    cf = cf3;
                                                                    cf.set(displayFrames2.mVoiceContent);
                                                                    adjust3 = adjust5;
                                                                } else {
                                                                    cf = cf3;
                                                                    if (ViewRootImpl.sNewInsetsMode == 0) {
                                                                        adjust3 = adjust5;
                                                                        if (adjust3 == 16) {
                                                                            cf.set(displayFrames2.mContent);
                                                                        }
                                                                    } else {
                                                                        adjust3 = adjust5;
                                                                    }
                                                                    if (!HwFreeFormUtils.isFreeFormEnable() || win.getWindowingMode() != 5) {
                                                                        cf.set(displayFrames2.mDock);
                                                                    } else {
                                                                        cf.set(displayFrames2.mContent);
                                                                    }
                                                                }
                                                                displayPolicy2 = this;
                                                                i7 = i6;
                                                                synchronized (displayPolicy2.mService.getWindowManagerLock()) {
                                                                    try {
                                                                        if (!win.hasDrawnLw()) {
                                                                            try {
                                                                                cf.top = displayFrames2.mUnrestricted.top + displayPolicy2.mStatusBarHeightForRotation[displayFrames2.mRotation];
                                                                            } catch (Throwable th2) {
                                                                                th = th2;
                                                                            }
                                                                        }
                                                                    } catch (Throwable th3) {
                                                                        th = th3;
                                                                        while (true) {
                                                                            try {
                                                                                break;
                                                                            } catch (Throwable th4) {
                                                                                th = th4;
                                                                            }
                                                                        }
                                                                        throw th;
                                                                    }
                                                                }
                                                                attrs2 = attrs4;
                                                                if ((attrs2.flags & Integer.MIN_VALUE) != 0 || (attrs2.flags & 67108864) != 0 || (sysUiFl3 & 3076) != 0 || attrs2.type == 3 || attrs2.type == 2038 || isLeftRightSplitStack) {
                                                                    vf2 = vf4;
                                                                    dcf = vf5;
                                                                } else {
                                                                    int i20 = displayFrames2.mUnrestricted.top + displayPolicy2.mStatusBarHeightForRotation[displayFrames2.mRotation];
                                                                    vf2 = vf4;
                                                                    vf2.top = i20;
                                                                    vf5.top = i20;
                                                                    dcf = vf5;
                                                                }
                                                            } else {
                                                                displayPolicy2 = this;
                                                                i7 = i6;
                                                                vf2 = vf4;
                                                                adjust3 = adjust5;
                                                                attrs2 = attrs4;
                                                                dcf = vf5;
                                                                cf = cf3;
                                                                cf.set(displayFrames2.mRestricted);
                                                            }
                                                            if (isNeedHideNaviBarWin2) {
                                                                cf.bottom = displayFrames2.mUnrestricted.bottom;
                                                            }
                                                            displayPolicy2.applyStableConstraints(sysUiFl3, fl4, cf, displayFrames2);
                                                            if (adjust3 != 48) {
                                                                vf2.set(displayFrames2.mCurrent);
                                                                of2 = vf2;
                                                                sysUiFl = sysUiFl3;
                                                                pf = pf3;
                                                                adjust2 = adjust3;
                                                                vf = cf;
                                                                df = df2;
                                                                attrs = attrs2;
                                                                i5 = i7;
                                                                fl5 = fl4;
                                                                displayPolicy = displayPolicy2;
                                                                of4 = of5;
                                                            } else {
                                                                vf2.set(cf);
                                                                of2 = vf2;
                                                                sysUiFl = sysUiFl3;
                                                                pf = pf3;
                                                                adjust2 = adjust3;
                                                                vf = cf;
                                                                df = df2;
                                                                attrs = attrs2;
                                                                i5 = i7;
                                                                fl5 = fl4;
                                                                displayPolicy = displayPolicy2;
                                                                of4 = of5;
                                                            }
                                                        } else {
                                                            i6 = 1;
                                                        }
                                                    } else {
                                                        sysUiFl3 = sysUiFl5;
                                                        i6 = 1;
                                                    }
                                                    if (!isNeedHideNaviBarWin2) {
                                                        df2.set(displayFrames2.mRestrictedOverscan);
                                                        pf3.set(displayFrames2.mRestrictedOverscan);
                                                        of5.set(displayFrames2.mUnrestricted);
                                                        if ((fl4 & 1024) == 0) {
                                                        }
                                                        if (isNeedHideNaviBarWin2) {
                                                        }
                                                        displayPolicy2.applyStableConstraints(sysUiFl3, fl4, cf, displayFrames2);
                                                        if (adjust3 != 48) {
                                                        }
                                                    }
                                                    df2.set(displayFrames2.mOverscan);
                                                    pf3.set(displayFrames2.mOverscan);
                                                    of5.set(displayFrames2.mUnrestricted);
                                                    if ((fl4 & 1024) == 0) {
                                                    }
                                                    if (isNeedHideNaviBarWin2) {
                                                    }
                                                    displayPolicy2.applyStableConstraints(sysUiFl3, fl4, cf, displayFrames2);
                                                    if (adjust3 != 48) {
                                                    }
                                                } else {
                                                    of5 = of7;
                                                    of5.set(displayFrames2.mOverscan);
                                                    df2 = df4;
                                                    df2.set(displayFrames2.mOverscan);
                                                    pf3 = pf5;
                                                    pf3.set(displayFrames2.mOverscan);
                                                    sysUiFl3 = sysUiFl5;
                                                    i6 = 1;
                                                    if ((fl4 & 1024) == 0) {
                                                    }
                                                    if (isNeedHideNaviBarWin2) {
                                                    }
                                                    displayPolicy2.applyStableConstraints(sysUiFl3, fl4, cf, displayFrames2);
                                                    if (adjust3 != 48) {
                                                    }
                                                }
                                                int i21 = (hasNavBar ? displayFrames2.mDock : displayFrames2.mUnrestricted).left;
                                                of5.left = i21;
                                                df2.left = i21;
                                                pf3.left = i21;
                                                int i22 = displayFrames2.mUnrestricted.top;
                                                of5.top = i22;
                                                df2.top = i22;
                                                pf3.top = i22;
                                                if (hasNavBar) {
                                                    i8 = displayFrames2.mRestricted.right;
                                                } else {
                                                    i8 = displayFrames2.mUnrestricted.right;
                                                }
                                                of5.right = i8;
                                                df2.right = i8;
                                                pf3.right = i8;
                                                if (hasNavBar) {
                                                    i9 = displayFrames2.mRestricted.bottom;
                                                } else {
                                                    i9 = displayFrames2.mUnrestricted.bottom;
                                                }
                                                of5.bottom = i9;
                                                df2.bottom = i9;
                                                pf3.bottom = i9;
                                                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                                    Slog.v(TAG, "Laying out status bar window: " + pf3);
                                                }
                                                if ((fl4 & 1024) == 0) {
                                                }
                                                if (isNeedHideNaviBarWin2) {
                                                }
                                                displayPolicy2.applyStableConstraints(sysUiFl3, fl4, cf, displayFrames2);
                                                if (adjust3 != 48) {
                                                }
                                            }
                                        }
                                        if (win.mAppToken == null || win.mAppToken.isSystemUiFullScreenWindowShow) {
                                            WindowState windowState12 = displayPolicy.mHwFullScreenWindow;
                                            if (windowState12 == null || !displayPolicy.isHwFullScreenWinVisibility || displayPolicy.mDisplayRotation != 0) {
                                                fl = fl5;
                                                of = of4;
                                                i = 1;
                                                windowState = win;
                                            } else if (!displayPolicy.isAboveFullScreen(win, windowState12)) {
                                                if (win.getOwningPackage() != null) {
                                                    if (win.getOwningPackage().contains(LAUNCHER_PACKAGE_NAME)) {
                                                        i = i5;
                                                        fl = fl5;
                                                        of = of4;
                                                        windowState = win;
                                                    } else if (win.getOwningPackage().contains("com.huawei.aod")) {
                                                        i = i5;
                                                        fl = fl5;
                                                        of = of4;
                                                        windowState = win;
                                                    }
                                                }
                                                Rect fullWinBtn = displayPolicy.mHwFullScreenWindow.getContentFrameLw();
                                                if (((fullWinBtn.top <= 0 || fullWinBtn.left != 0) ? 0 : i5) != 0 && vf.bottom > fullWinBtn.top) {
                                                    int i23 = (displayFrames2.mUnrestricted.top + displayPolicy.mRestrictedScreenHeight) - (vf.bottom - fullWinBtn.top);
                                                    vf.bottom = i23;
                                                    of4.bottom = i23;
                                                    df.bottom = i23;
                                                    pf.bottom = i23;
                                                    fl = fl5;
                                                    of = of4;
                                                    i = 1;
                                                    windowState = win;
                                                } else {
                                                    fl = fl5;
                                                    of = of4;
                                                    i = 1;
                                                    windowState = win;
                                                }
                                            } else {
                                                fl = fl5;
                                                of = of4;
                                                i = 1;
                                                windowState = win;
                                            }
                                        } else {
                                            i = i5;
                                            fl = fl5;
                                            of = of4;
                                            windowState = win;
                                        }
                                    }
                                } else {
                                    dcf = vf5;
                                    windowFrames = windowFrames5;
                                    vf = cf3;
                                    sf = sf3;
                                    fl3 = fl8;
                                    attrs = attrs4;
                                    displayPolicy = this;
                                    type = type3;
                                    pf = pf5;
                                    pf2 = of7;
                                    of2 = vf4;
                                    df = df4;
                                    i3 = adjust5;
                                    adjust = sysUiFl5;
                                }
                                i = 1;
                                windowState = win;
                                fl = fl3;
                                of = pf2;
                                layoutWallpaper(displayFrames, pf, df, pf2, vf);
                            }
                        } else {
                            if (attached != null) {
                                adjust4 = adjust5;
                                windowFrames4 = windowFrames5;
                                sysUiFl4 = sysUiFl5;
                                vf3 = vf4;
                                dcf6 = vf5;
                                sim = sim2;
                                fl7 = fl8;
                                type2 = type3;
                                attrs3 = attrs4;
                                sf = sf3;
                                displayFrames2 = displayFrames;
                                setAttachedWindowFrames(win, fl8, adjust5, attached, true, pf5, df4, of7, cf3, vf3, displayFrames);
                                of6 = of7;
                                cf2 = cf3;
                                df3 = df4;
                                pf = pf5;
                            } else {
                                windowFrames4 = windowFrames5;
                                adjust4 = adjust5;
                                sim = sim2;
                                fl7 = fl8;
                                sf = sf3;
                                attrs3 = attrs4;
                                displayFrames2 = displayFrames;
                                sysUiFl4 = sysUiFl5;
                                vf3 = vf4;
                                type2 = type3;
                                dcf6 = vf5;
                                int i24 = displayFrames2.mOverscan.left;
                                cf2 = cf3;
                                cf2.left = i24;
                                of6 = of7;
                                of6.left = i24;
                                df3 = df4;
                                df3.left = i24;
                                pf = pf5;
                                pf.left = i24;
                                int i25 = displayFrames2.mOverscan.top;
                                cf2.top = i25;
                                of6.top = i25;
                                df3.top = i25;
                                pf.top = i25;
                                int i26 = displayFrames2.mOverscan.right;
                                cf2.right = i26;
                                of6.right = i26;
                                df3.right = i26;
                                pf.right = i26;
                                int i27 = displayFrames2.mOverscan.bottom;
                                cf2.bottom = i27;
                                of6.bottom = i27;
                                df3.bottom = i27;
                                pf.bottom = i27;
                            }
                            if (attrs3.type == 2011) {
                                int i28 = displayFrames2.mDock.left;
                                vf3.left = i28;
                                cf2.left = i28;
                                of6.left = i28;
                                df3.left = i28;
                                pf.left = i28;
                                int i29 = displayFrames2.mDock.top;
                                vf3.top = i29;
                                cf2.top = i29;
                                of6.top = i29;
                                df3.top = i29;
                                pf.top = i29;
                                int inputMethodRightForHwMultiDisplay = this.mHwDisplayPolicyEx.getInputMethodRightForHwMultiDisplay(displayFrames2.mDock.left, displayFrames2.mDock.right);
                                vf3.right = inputMethodRightForHwMultiDisplay;
                                cf2.right = inputMethodRightForHwMultiDisplay;
                                of6.right = inputMethodRightForHwMultiDisplay;
                                df3.right = inputMethodRightForHwMultiDisplay;
                                pf.right = inputMethodRightForHwMultiDisplay;
                                int height = displayFrames2.mUnrestricted.top + displayFrames2.mUnrestricted.height();
                                of6.bottom = height;
                                df3.bottom = height;
                                pf.bottom = height;
                                int i30 = displayFrames2.mStable.bottom;
                                vf3.bottom = i30;
                                cf2.bottom = i30;
                                attrs3.gravity = 80;
                                of = of6;
                                dcf = dcf6;
                                windowFrames = windowFrames4;
                                fl = fl7;
                                i = 1;
                                of2 = vf3;
                                vf = cf2;
                                displayPolicy = this;
                                type = type2;
                                attrs = attrs3;
                                df = df3;
                                windowState = win;
                            } else {
                                if ((sysUiFl4 & 2) != 0) {
                                    sf2 = sf;
                                } else if (getNavigationBarExternal() == null || getNavigationBarExternal().isVisibleLw()) {
                                    sf2 = sf;
                                    cf2.set(sf2);
                                    if (attrs3.type == 2008) {
                                        cf2.bottom = displayFrames2.mContent.bottom;
                                    }
                                    if (attrs3.gravity == 80) {
                                        attrs3.gravity = 17;
                                    }
                                    if (adjust4 != 48) {
                                        vf3.set(displayFrames2.mCurrent);
                                    }
                                    if (HwPCUtils.isHiCarCastMode()) {
                                        vf = cf2;
                                        layoutInputWindowForPCMode(win, this.mInputMethodTarget, this.mDisplayContent.mInputMethodWindow, pf, df3, vf, vf3, displayFrames2.mContent.bottom);
                                        df = df3;
                                        of = of6;
                                        sf = sf2;
                                        of2 = vf3;
                                        dcf = dcf6;
                                        windowFrames = windowFrames4;
                                        fl = fl7;
                                        attrs = attrs3;
                                        pf = pf;
                                        i = 1;
                                        windowState = win;
                                        displayPolicy = this;
                                        type = type2;
                                    } else {
                                        df = df3;
                                        of = of6;
                                        vf = cf2;
                                        sf = sf2;
                                        of2 = vf3;
                                        dcf = dcf6;
                                        windowFrames = windowFrames4;
                                        fl = fl7;
                                        attrs = attrs3;
                                        i = 1;
                                        windowState = win;
                                        displayPolicy = this;
                                        type = type2;
                                    }
                                } else {
                                    sf2 = sf;
                                }
                                cf2.set(df3);
                                if (attrs3.type == 2008) {
                                }
                                if (attrs3.gravity == 80) {
                                }
                                if (adjust4 != 48) {
                                }
                                if (HwPCUtils.isHiCarCastMode()) {
                                }
                            }
                        }
                        if (mUsingHwNavibar) {
                            dcf2 = dcf;
                        } else if (isNeedHideNaviBarWin2 || displayPolicy.mHwDisplayPolicyEx.getNaviBarFlag()) {
                            int i31 = pf.bottom;
                            of2.bottom = i31;
                            vf.bottom = i31;
                            dcf2 = dcf;
                            dcf2.bottom = i31;
                        } else {
                            dcf2 = dcf;
                        }
                        cutoutMode = attrs.layoutInDisplayCutoutMode;
                        int i32 = (attached != null || layoutInScreen) ? 0 : i;
                        int i33 = (requestedSysUiFl & 2) == 0 ? i : 0;
                        int i34 = (!attrs.isFullscreen() || !layoutInScreen || type == i) ? 0 : i;
                        win.getWindowingMode();
                        if (!HwDisplaySizeUtil.hasSideInScreen()) {
                            windowState.mIsNeedExceptDisplaySide = displayPolicy.mHwDisplayPolicyEx.isNeedExceptDisplaySide(attrs, windowState, displayPolicy.mDisplayRotation);
                            if (windowState.mIsNeedExceptDisplaySide) {
                                Rect displaySideSafeExceptMaybeBars = new Rect();
                                displaySideSafeExceptMaybeBars.set(displayFrames2.mDisplaySideSafe);
                                int i35 = displayPolicy.mNavigationBarPosition;
                                if (i35 == i) {
                                    displaySideSafeExceptMaybeBars.left = Integer.MIN_VALUE;
                                } else if (i35 == 2) {
                                    displaySideSafeExceptMaybeBars.right = Integer.MAX_VALUE;
                                } else if (i35 == 4) {
                                    if (((displayFrames2.mRotation == i || displayFrames2.mRotation == 3) ? i : 0) == 0) {
                                        displaySideSafeExceptMaybeBars.bottom = Integer.MAX_VALUE;
                                    }
                                }
                                if (i32 == 0 && i34 == 0) {
                                    sTmpRect.set(pf);
                                    pf.intersectUnchecked(displaySideSafeExceptMaybeBars);
                                    windowFrames2 = windowFrames;
                                    windowFrames2.setParentFrameWasClippedByDisplayCutout(sTmpRect.equals(pf) ^ i);
                                } else {
                                    windowFrames2 = windowFrames;
                                }
                                df.intersectUnchecked(displaySideSafeExceptMaybeBars);
                                if (isPCDisplay || !layoutInScreen || !layoutInsetDecor) {
                                    dcf3 = dcf2;
                                    fl2 = fl;
                                } else {
                                    fl2 = fl;
                                    if ((fl2 & 1024) != 0) {
                                        dcf3 = dcf2;
                                    } else if (!win.inMultiWindowMode()) {
                                        int i36 = displayPolicy.mDisplayRotation;
                                        if (i36 == 1 || i36 == 3) {
                                            dcf3 = dcf2;
                                            displaySideSafeExceptMaybeBars.top += displayPolicy.mStatusBarHeightForRotation[displayFrames2.mRotation];
                                            vf.top = displaySideSafeExceptMaybeBars.top;
                                        } else {
                                            dcf3 = dcf2;
                                        }
                                    } else {
                                        dcf3 = dcf2;
                                    }
                                }
                            } else {
                                dcf3 = dcf2;
                                windowFrames2 = windowFrames;
                                fl2 = fl;
                            }
                        } else {
                            dcf3 = dcf2;
                            windowFrames2 = windowFrames;
                            fl2 = fl;
                        }
                        displayPolicy.mService.getPolicy().layoutWindowLwForNotch(windowState, attrs);
                        layoutBelowNotch = displayPolicy.mService.getPolicy().isWindowNeedLayoutBelowNotch(windowState);
                        if (layoutBelowNotch || (!IS_NOTCH_PROP && cutoutMode != 1)) {
                            displayCutoutSafeExceptMaybeBars = sTmpDisplayCutoutSafeExceptMaybeBarsRect;
                            displayCutoutSafeExceptMaybeBars.set(displayFrames2.mDisplayCutoutSafe);
                            if (layoutInScreen && layoutInsetDecor && !requestedFullscreen && cutoutMode == 0 && !displayPolicy.mService.getPolicy().isWindowNeedLayoutBelowWhenHideNotch()) {
                                displayCutoutSafeExceptMaybeBars.top = Integer.MIN_VALUE;
                            }
                            if (layoutInScreen && layoutInsetDecor && i33 == 0 && cutoutMode == 0) {
                                i2 = displayPolicy.mNavigationBarPosition;
                                if (i2 != 1) {
                                    displayCutoutSafeExceptMaybeBars.left = Integer.MIN_VALUE;
                                } else if (i2 != 2) {
                                    if (i2 == 4) {
                                        displayCutoutSafeExceptMaybeBars.bottom = Integer.MAX_VALUE;
                                    }
                                } else if (!IS_NOTCH_PROP || displayPolicy.mDisplayRotation != 3) {
                                    displayCutoutSafeExceptMaybeBars.right = Integer.MAX_VALUE;
                                }
                            }
                            if (type == 2011 && displayPolicy.mNavigationBarPosition == 4) {
                                displayCutoutSafeExceptMaybeBars.bottom = Integer.MAX_VALUE;
                            }
                            if (i32 == 0 && i34 == 0) {
                                sTmpRect.set(pf);
                                pf.intersectUnchecked(displayCutoutSafeExceptMaybeBars);
                                windowFrames2.setParentFrameWasClippedByDisplayCutout(!sTmpRect.equals(pf));
                            }
                            df.intersectUnchecked(displayCutoutSafeExceptMaybeBars);
                        }
                        vf.intersectUnchecked(displayFrames2.mDisplayCutoutSafe);
                        if (HwPCUtils.isPcCastModeInServer() || !isPCDisplay) {
                            of3 = of;
                            if ((fl2 & 512) != 0 || type == 2010) {
                                cutoutMode2 = cutoutMode;
                            } else if (!win.inMultiWindowMode() || win.toString().contains(LAUNCHER_PACKAGE_NAME)) {
                                boolean skipSetUnLimitWidth = HwDisplaySizeUtil.hasSideInScreen() && win.toString().contains("hwSingleMode_window");
                                if (!HwDisplaySizeUtil.hasSideInScreen() || type != 2013) {
                                    cutoutMode2 = cutoutMode;
                                } else {
                                    int i37 = displayPolicy.mDisplayRotation;
                                    cutoutMode2 = cutoutMode;
                                    if (i37 == 1 || i37 == 3) {
                                        skipSetUnLimitHeight = true;
                                        if (!skipSetUnLimitWidth) {
                                            df.top = -10000;
                                            df.bottom = 10000;
                                        } else if (skipSetUnLimitHeight) {
                                            df.left = -10000;
                                            df.right = 10000;
                                        } else {
                                            df.top = -10000;
                                            df.left = -10000;
                                            df.bottom = 10000;
                                            df.right = 10000;
                                        }
                                        if (type != 2013) {
                                            of2.top = -10000;
                                            of2.left = -10000;
                                            vf.top = -10000;
                                            vf.left = -10000;
                                            of3.top = -10000;
                                            of3.left = -10000;
                                            of2.bottom = 10000;
                                            of2.right = 10000;
                                            vf.bottom = 10000;
                                            vf.right = 10000;
                                            of3.bottom = 10000;
                                            of3.right = 10000;
                                        }
                                    }
                                }
                                skipSetUnLimitHeight = false;
                                if (!skipSetUnLimitWidth) {
                                }
                                if (type != 2013) {
                                }
                            } else {
                                cutoutMode2 = cutoutMode;
                            }
                        } else {
                            if (attrs.type == 2005) {
                                int i38 = displayFrames2.mStable.left;
                                vf.left = i38;
                                of3 = of;
                                of3.left = i38;
                                df.left = i38;
                                pf.left = i38;
                                int i39 = displayFrames2.mStable.top;
                                vf.top = i39;
                                of3.top = i39;
                                df.top = i39;
                                pf.top = i39;
                                int i40 = displayFrames2.mStable.right;
                                vf.right = i40;
                                of3.right = i40;
                                df.right = i40;
                                pf.right = i40;
                                int i41 = displayFrames2.mStable.bottom;
                                vf.bottom = i41;
                                of3.bottom = i41;
                                df.bottom = i41;
                                pf.bottom = i41;
                            } else {
                                of3 = of;
                            }
                            if (HwPCUtils.enabledInPad() && (fl2 & 512) != 0 && win.toString().contains("com.huawei.associateassistant")) {
                                df.top = -10000;
                                df.left = -10000;
                                df.bottom = 10000;
                                df.right = 10000;
                            }
                            IHwDisplayPolicyEx iHwDisplayPolicyEx2 = displayPolicy.mHwDisplayPolicyEx;
                            if (iHwDisplayPolicyEx2 != null) {
                                iHwDisplayPolicyEx2.updateWindowDisplayFrame(windowState, type, df);
                                cutoutMode2 = cutoutMode;
                            } else {
                                cutoutMode2 = cutoutMode;
                            }
                        }
                        if (!win.inHwMagicWindowingMode() || "MagicWindowGuideDialog".equals(attrs.getTitle())) {
                            layoutBelowNotch2 = layoutBelowNotch;
                            HwMwUtils.performPolicy((int) WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT, new Object[]{windowState, new Rect[]{df, pf, vf, of2, sf}, displayPolicy.mNavigationBar, Boolean.valueOf(displayPolicy.mHwDisplayPolicyEx.isNaviBarMini())});
                        } else {
                            layoutBelowNotch2 = layoutBelowNotch;
                        }
                        boolean useOutsets = shouldUseOutsets(attrs, fl2);
                        if (!isDefaultDisplay && useOutsets) {
                            Rect osf = windowFrames2.mOutsetFrame;
                            osf.set(vf.left, vf.top, vf.right, vf.bottom);
                            windowFrames2.setHasOutsets(true);
                            int outset = displayPolicy.mWindowOutsetBottom;
                            if (outset > 0) {
                                int rotation2 = displayFrames2.mRotation;
                                if (rotation2 == 0) {
                                    osf.bottom += outset;
                                } else if (rotation2 == 1) {
                                    osf.right += outset;
                                } else if (rotation2 == 2) {
                                    osf.top -= outset;
                                } else if (rotation2 == 3) {
                                    osf.left -= outset;
                                }
                                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                                    Slog.v(TAG, "applying bottom outset of " + outset + " with rotation " + rotation2 + ", result: " + osf);
                                }
                            }
                        }
                        if (win.toString().contains("DividerMenusView")) {
                            pf.top = displayFrames2.mUnrestricted.top;
                            pf.bottom = displayFrames2.mUnrestricted.bottom;
                        }
                        if (HwFoldScreenState.isFoldScreenDevice() && type == 2039) {
                            int i42 = displayFrames2.mUnrestricted.bottom;
                            of2.bottom = i42;
                            vf.bottom = i42;
                            of3.bottom = i42;
                            df.bottom = i42;
                            pf.bottom = i42;
                        }
                        if ((!HwPCUtils.isPcCastModeInServer() || !isPCDisplay) && win.toString().contains("com.android.packageinstaller.permission.ui.GrantPermissionsActivity")) {
                            int i43 = displayFrames2.mUnrestricted.top + displayPolicy.mStatusBarHeightForRotation[displayFrames2.mRotation];
                            of2.top = i43;
                            vf.top = i43;
                            of3.top = i43;
                            df.top = i43;
                            pf.top = i43;
                        }
                        if (hasNavBar && win.inHwSplitScreenWindowingMode() && type >= 1 && type <= 99) {
                            df.bottom = Math.min(df.bottom, displayFrames2.mRestrictedOverscan.bottom);
                            df.right = Math.min(df.right, displayFrames2.mRestrictedOverscan.right);
                            pf.bottom = Math.min(pf.bottom, displayFrames2.mRestrictedOverscan.bottom);
                            pf.right = Math.min(pf.right, displayFrames2.mRestrictedOverscan.right);
                        }
                        if (type == 2020 || !"VolumeDialogImpl".equals(attrs.getTitle())) {
                            dcf4 = dcf3;
                        } else {
                            dcf4 = dcf3;
                            dcf4.top = 0;
                        }
                        if (!WindowManagerDebugConfig.DEBUG_LAYOUT) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Compute frame ");
                            sb.append((Object) attrs.getTitle());
                            sb.append(": sim=#");
                            sb.append(Integer.toHexString(sim));
                            sb.append(" attach=");
                            windowState3 = attached;
                            sb.append(windowState3);
                            sb.append(" type=");
                            sb.append(type);
                            sb.append(String.format(" flags=0x%08x", Integer.valueOf(fl2)));
                            sb.append(" pf=");
                            sb.append(pf.toShortString());
                            sb.append(" df=");
                            sb.append(df.toShortString());
                            sb.append(" of=");
                            sb.append(of3.toShortString());
                            sb.append(" cf=");
                            sb.append(vf.toShortString());
                            sb.append(" vf=");
                            sb.append(of2.toShortString());
                            sb.append(" dcf=");
                            sb.append(dcf4.toShortString());
                            sb.append(" sf=");
                            sb.append(sf.toShortString());
                            sb.append(" osf=");
                            sb.append(windowFrames2.mOutsetFrame.toShortString());
                            sb.append(" ");
                            windowState2 = win;
                            sb.append(windowState2);
                            Slog.v(TAG, sb.toString());
                        } else {
                            windowState2 = win;
                            windowState3 = attached;
                        }
                        if (!sTmpLastParentFrame.equals(pf)) {
                            windowFrames2.setContentChanged(true);
                        }
                        WindowManagerService windowManagerService = displayPolicy.mService;
                        if (WindowManagerService.IS_TABLET || !IS_NOTCH_PROP) {
                            windowFrames3 = windowFrames2;
                            windowState4 = windowState3;
                        } else if (!HwPCUtils.isPcCastModeInServer() || !isPCDisplay) {
                            windowFrames3 = windowFrames2;
                            windowState4 = windowState3;
                            displayPolicy.mHwDisplayPolicyEx.updateDisplayFrames(win, displayFrames, displayPolicy.mLastSystemUiFlags, vf, displayPolicy.getNavigationBarHeight(displayPolicy.mDisplayRotation, displayPolicy.mService.mPolicy.getUiMode()));
                        } else {
                            windowFrames3 = windowFrames2;
                            windowState4 = windowState3;
                        }
                        displayPolicy.mHwDisplayPolicyEx.updateWindowFramesForPC(windowFrames3, pf, df, vf, of2, isPCDisplay);
                        win.computeFrameLw();
                        if (type == 2011 && win.isVisibleLw() && !win.getGivenInsetsPendingLw()) {
                            displayPolicy.offsetInputMethodWindowLw(windowState2, displayFrames2);
                        }
                        if (type == 2031 && win.isVisibleLw() && !win.getGivenInsetsPendingLw()) {
                            displayPolicy.offsetVoiceInputWindowLw(windowState2, displayFrames2);
                        }
                        iHwDisplayPolicyEx = displayPolicy.mHwDisplayPolicyEx;
                        if (iHwDisplayPolicyEx == null) {
                            iHwDisplayPolicyEx.layoutWindowLw(windowState2, windowState4, displayPolicy.mFocusedWindow, layoutBelowNotch2);
                            return;
                        }
                        return;
                    }
                }
                isNeedHideNaviBarWin = false;
                if (!HwPCUtils.isPcCastModeInServer()) {
                }
                if (HwPCUtils.isPcCastModeInServer()) {
                }
                isNeedHideNaviBarWin2 = isNeedHideNaviBarWin;
                hasNavBar = !hasNavigationBar() && (windowState7 = this.mNavigationBar) != null && windowState7.isVisibleLw();
                boolean isBaiDuOrSwiftkey2 = isBaiDuOrSwiftkey(win);
                boolean isLeftRightSplitStackVisible2 = isLeftRightSplitStackVisible();
                boolean isDocked2 = false;
                if (attrs4.type == 2) {
                }
                if (!isLeftRightSplitStackVisible2) {
                }
                if (isLeftRightSplitStackVisible2) {
                }
                if ((fl8 & 1024) == 0) {
                }
                if ((fl8 & 256) != 256) {
                }
                if ((65536 & fl8) != 65536) {
                }
                sf3.set(displayFrames.mStable);
                if (HwPCUtils.isPcCastModeInServer()) {
                }
                sim = sim2;
                displayFrames2 = displayFrames;
                if (type3 != 2011) {
                }
                if (mUsingHwNavibar) {
                }
                cutoutMode = attrs.layoutInDisplayCutoutMode;
                if (attached != null) {
                }
                if ((requestedSysUiFl & 2) == 0) {
                }
                if (!attrs.isFullscreen()) {
                }
                win.getWindowingMode();
                if (!HwDisplaySizeUtil.hasSideInScreen()) {
                }
                displayPolicy.mService.getPolicy().layoutWindowLwForNotch(windowState, attrs);
                layoutBelowNotch = displayPolicy.mService.getPolicy().isWindowNeedLayoutBelowNotch(windowState);
                displayCutoutSafeExceptMaybeBars = sTmpDisplayCutoutSafeExceptMaybeBarsRect;
                displayCutoutSafeExceptMaybeBars.set(displayFrames2.mDisplayCutoutSafe);
                displayCutoutSafeExceptMaybeBars.top = Integer.MIN_VALUE;
                i2 = displayPolicy.mNavigationBarPosition;
                if (i2 != 1) {
                }
                displayCutoutSafeExceptMaybeBars.bottom = Integer.MAX_VALUE;
                sTmpRect.set(pf);
                pf.intersectUnchecked(displayCutoutSafeExceptMaybeBars);
                windowFrames2.setParentFrameWasClippedByDisplayCutout(!sTmpRect.equals(pf));
                df.intersectUnchecked(displayCutoutSafeExceptMaybeBars);
                vf.intersectUnchecked(displayFrames2.mDisplayCutoutSafe);
                if (HwPCUtils.isPcCastModeInServer()) {
                }
                of3 = of;
                if ((fl2 & 512) != 0) {
                }
                cutoutMode2 = cutoutMode;
                if (!win.inHwMagicWindowingMode()) {
                }
                layoutBelowNotch2 = layoutBelowNotch;
                HwMwUtils.performPolicy((int) WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT, new Object[]{windowState, new Rect[]{df, pf, vf, of2, sf}, displayPolicy.mNavigationBar, Boolean.valueOf(displayPolicy.mHwDisplayPolicyEx.isNaviBarMini())});
                boolean useOutsets2 = shouldUseOutsets(attrs, fl2);
                if (!isDefaultDisplay) {
                }
                if (win.toString().contains("DividerMenusView")) {
                }
                int i422 = displayFrames2.mUnrestricted.bottom;
                of2.bottom = i422;
                vf.bottom = i422;
                of3.bottom = i422;
                df.bottom = i422;
                pf.bottom = i422;
                int i432 = displayFrames2.mUnrestricted.top + displayPolicy.mStatusBarHeightForRotation[displayFrames2.mRotation];
                of2.top = i432;
                vf.top = i432;
                of3.top = i432;
                df.top = i432;
                pf.top = i432;
                df.bottom = Math.min(df.bottom, displayFrames2.mRestrictedOverscan.bottom);
                df.right = Math.min(df.right, displayFrames2.mRestrictedOverscan.right);
                pf.bottom = Math.min(pf.bottom, displayFrames2.mRestrictedOverscan.bottom);
                pf.right = Math.min(pf.right, displayFrames2.mRestrictedOverscan.right);
                if (type == 2020) {
                }
                dcf4 = dcf3;
                if (!WindowManagerDebugConfig.DEBUG_LAYOUT) {
                }
                if (!sTmpLastParentFrame.equals(pf)) {
                }
                WindowManagerService windowManagerService2 = displayPolicy.mService;
                if (WindowManagerService.IS_TABLET) {
                }
                windowFrames3 = windowFrames2;
                windowState4 = windowState3;
                displayPolicy.mHwDisplayPolicyEx.updateWindowFramesForPC(windowFrames3, pf, df, vf, of2, isPCDisplay);
                win.computeFrameLw();
                displayPolicy.offsetInputMethodWindowLw(windowState2, displayFrames2);
                displayPolicy.offsetVoiceInputWindowLw(windowState2, displayFrames2);
                iHwDisplayPolicyEx = displayPolicy.mHwDisplayPolicyEx;
                if (iHwDisplayPolicyEx == null) {
                }
            }
        }
    }

    private boolean isAboveFullScreen(WindowState win, WindowState fullScreenWin) {
        if (win.isInAboveAppWindows() && fullScreenWin.getLayer() < win.getLayer()) {
            return true;
        }
        return false;
    }

    private int calculateSecImeRaisePx(Context context) {
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
        float f = context.getResources().getDisplayMetrics().density;
        return (int) ((((float) SEC_IME_RAISE_HEIGHT) * f * ((((float) lcdDpi) * 1.0f) / ((float) SystemProperties.getInt("persist.sys.dpi", lcdDpi)))) + 0.5f);
    }

    /* access modifiers changed from: package-private */
    public boolean isBaiDuOrSwiftkey(WindowState win) {
        String packageName = win.getAttrs().packageName;
        if (packageName == null || !packageName.contains("com.baidu.input_huawei")) {
            return false;
        }
        return true;
    }

    public void setInputMethodTargetWindow(WindowState target) {
        this.mInputMethodTarget = target;
    }

    public void setFocusChangeIMEFrozenTag(boolean frozen) {
        this.mFrozen = frozen;
    }

    private void layoutWallpaper(DisplayFrames displayFrames, Rect pf, Rect df, Rect of, Rect cf) {
        df.set(displayFrames.mOverscan);
        pf.set(displayFrames.mOverscan);
        cf.set(displayFrames.mUnrestricted);
        of.set(displayFrames.mUnrestricted);
    }

    private void offsetInputMethodWindowLw(WindowState win, DisplayFrames displayFrames) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top) + win.getGivenContentInsetsLw().top;
        displayFrames.mContent.bottom = Math.min(displayFrames.mContent.bottom, top);
        displayFrames.mVoiceContent.bottom = Math.min(displayFrames.mVoiceContent.bottom, top);
        int top2 = win.getVisibleFrameLw().top + win.getGivenVisibleInsetsLw().top;
        displayFrames.mCurrent.bottom = Math.min(displayFrames.mCurrent.bottom, top2);
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.v(TAG, "Input method: mDockBottom=" + displayFrames.mDock.bottom + " mContentBottom=" + displayFrames.mContent.bottom + " mCurBottom=" + displayFrames.mCurrent.bottom);
        }
    }

    private void offsetVoiceInputWindowLw(WindowState win, DisplayFrames displayFrames) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top) + win.getGivenContentInsetsLw().top;
        displayFrames.mVoiceContent.bottom = Math.min(displayFrames.mVoiceContent.bottom, top);
    }

    public void beginPostLayoutPolicyLw() {
        this.mTopFullscreenOpaqueWindowState = null;
        this.mTopFullscreenOpaqueOrDimmingWindowState = null;
        this.mTopDockedOpaqueWindowState = null;
        this.mTopDockedOpaqueOrDimmingWindowState = null;
        this.mForceStatusBar = false;
        this.mForceStatusBarFromKeyguard = false;
        this.mForceStatusBarTransparent = false;
        this.mForcingShowNavBar = false;
        this.mForcingShowNavBarLayer = -1;
        this.mAllowLockscreenWhenOn = false;
        this.mShowingDream = false;
        this.mWindowSleepTokenNeeded = false;
    }

    public void applyPostLayoutPolicyLw(WindowState win, WindowManager.LayoutParams attrs, WindowState attached, WindowState imeTarget) {
        boolean affectsSystemUi = win.canAffectSystemUiFlags();
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.i(TAG, "Win " + win + ": affectsSystemUi=" + affectsSystemUi);
        }
        this.mService.mPolicy.applyKeyguardPolicyLw(win, imeTarget);
        int fl = PolicyControl.getWindowFlags(win, attrs);
        boolean isPrimaryWindowingMode = true;
        if (this.mTopFullscreenOpaqueWindowState == null && affectsSystemUi && attrs.type == 2011) {
            this.mForcingShowNavBar = true;
            this.mForcingShowNavBarLayer = win.getSurfaceLayer();
        }
        if (attrs.type == 2000) {
            if ((attrs.privateFlags & 1024) != 0) {
                this.mForceStatusBarFromKeyguard = true;
            }
            if ((attrs.privateFlags & 4096) != 0) {
                this.mForceStatusBarTransparent = true;
            }
        }
        boolean appWindow = attrs.type >= 1 && attrs.type < 2000;
        int windowingMode = win.getWindowingMode();
        boolean inFullScreenOrSplitScreenSecondaryWindowingMode = windowingMode == 1 || windowingMode == 4 || windowingMode == 101;
        if (this.mTopFullscreenOpaqueWindowState == null && affectsSystemUi) {
            if ((fl & 2048) != 0 && !isLeftRightSplitStackVisible()) {
                this.mForceStatusBar = true;
            }
            if (attrs.type == 2023 && (!this.mDreamingLockscreen || (win.isVisibleLw() && win.hasDrawnLw()))) {
                this.mShowingDream = true;
                appWindow = true;
            }
            if (appWindow && attached == null && attrs.isFullscreen() && inFullScreenOrSplitScreenSecondaryWindowingMode) {
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v(TAG, "Fullscreen window: " + win);
                }
                this.mTopFullscreenOpaqueWindowState = win;
                if (this.mTopFullscreenOpaqueOrDimmingWindowState == null) {
                    this.mTopFullscreenOpaqueOrDimmingWindowState = win;
                }
                if ((fl & 1) != 0) {
                    this.mAllowLockscreenWhenOn = true;
                }
            }
        }
        if (affectsSystemUi && attrs.type == 2031) {
            if (this.mTopFullscreenOpaqueWindowState == null) {
                this.mTopFullscreenOpaqueWindowState = win;
                if (this.mTopFullscreenOpaqueOrDimmingWindowState == null) {
                    this.mTopFullscreenOpaqueOrDimmingWindowState = win;
                }
            }
            if (this.mTopDockedOpaqueWindowState == null) {
                this.mTopDockedOpaqueWindowState = win;
                if (this.mTopDockedOpaqueOrDimmingWindowState == null) {
                    this.mTopDockedOpaqueOrDimmingWindowState = win;
                }
            }
        }
        if (this.mTopFullscreenOpaqueOrDimmingWindowState == null && affectsSystemUi && win.isDimming() && inFullScreenOrSplitScreenSecondaryWindowingMode) {
            this.mTopFullscreenOpaqueOrDimmingWindowState = win;
        }
        if (!(windowingMode == 3 || windowingMode == 100)) {
            isPrimaryWindowingMode = false;
        }
        if (this.mTopDockedOpaqueWindowState == null && affectsSystemUi && appWindow && attached == null && attrs.isFullscreen() && isPrimaryWindowingMode) {
            this.mTopDockedOpaqueWindowState = win;
            if (this.mTopDockedOpaqueOrDimmingWindowState == null) {
                this.mTopDockedOpaqueOrDimmingWindowState = win;
            }
        }
        if (this.mTopDockedOpaqueOrDimmingWindowState == null && affectsSystemUi && win.isDimming() && isPrimaryWindowingMode) {
            this.mTopDockedOpaqueOrDimmingWindowState = win;
        }
        if (windowingMode != 103 || this.mTopFullscreenOpaqueWindowState != null) {
            return;
        }
        if (!((win.getAttrs().flags & 1024) == 0 && (win.getAttrs().flags & 2048) == 0) && win.canAffectSystemUiFlags()) {
            this.mTopFullscreenOpaqueWindowState = win;
            if (this.mTopFullscreenOpaqueOrDimmingWindowState == null) {
                this.mTopFullscreenOpaqueOrDimmingWindowState = win;
            }
        }
    }

    /* JADX WARN: Type inference failed for: r8v12, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public int finishPostLayoutPolicyLw() {
        int i;
        int changes = 0;
        boolean topIsFullscreen = false;
        boolean z = true;
        if (!this.mShowingDream) {
            this.mDreamingLockscreen = this.mService.mPolicy.isKeyguardShowingAndNotOccluded();
            if (this.mDreamingSleepTokenNeeded) {
                this.mDreamingSleepTokenNeeded = false;
                this.mHandler.obtainMessage(1, 0, 1).sendToTarget();
            }
        } else if (!this.mDreamingSleepTokenNeeded) {
            this.mDreamingSleepTokenNeeded = true;
            this.mHandler.obtainMessage(1, 1, 1).sendToTarget();
        }
        if (this.mStatusBar != null) {
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.i(TAG, "force=" + this.mForceStatusBar + " forcefkg=" + this.mForceStatusBarFromKeyguard + " top=" + this.mTopFullscreenOpaqueWindowState);
            }
            if (!(this.mForceStatusBarTransparent && !this.mForceStatusBar && !this.mForceStatusBarFromKeyguard)) {
                this.mStatusBarController.setShowTransparent(false);
            } else if (!this.mStatusBar.isVisibleLw()) {
                this.mStatusBarController.setShowTransparent(true);
            }
            boolean statusBarForcesShowingNavigation = (this.mStatusBar.getAttrs().privateFlags & 8388608) != 0;
            boolean topAppHidesStatusBar = topAppHidesStatusBar();
            if (this.mForceStatusBar || this.mForceStatusBarFromKeyguard || this.mForceStatusBarTransparent || statusBarForcesShowingNavigation) {
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v(TAG, "Showing status bar: forced");
                }
                if (this.mStatusBarController.setBarShowingLw(true)) {
                    changes = 0 | 1;
                }
                if (!this.mTopIsFullscreen || !this.mStatusBar.isAnimatingLw()) {
                    z = false;
                }
                topIsFullscreen = z;
                if ((this.mForceStatusBarFromKeyguard || statusBarForcesShowingNavigation) && this.mStatusBarController.isTransientShowing()) {
                    StatusBarController statusBarController = this.mStatusBarController;
                    int i2 = this.mLastSystemUiFlags;
                    statusBarController.updateVisibilityLw(false, i2, i2);
                }
            } else if (this.mTopFullscreenOpaqueWindowState != null) {
                topIsFullscreen = topAppHidesStatusBar;
                if (IS_NOTCH_PROP && this.mDisplayRotation == 0) {
                    ?? r8 = this.mForceNotchStatusBar;
                    this.notchWindowChangeState = this.mNotchStatusBarColorLw != r8;
                    if (this.notchWindowChangeState) {
                        this.mNotchStatusBarColorLw = r8;
                        this.mService.getPolicy().notchStatusBarColorUpdate((int) r8);
                        this.notchWindowChange = true;
                    } else if (this.mFocusedWindow != null && !this.mTopFullscreenOpaqueWindowState.toString().equals(this.mFocusedWindow.toString()) && !this.notchWindowChangeState && this.mForceNotchStatusBar && this.notchWindowChange) {
                        this.mService.getPolicy().notchStatusBarColorUpdate(r8 == true ? 1 : 0);
                        this.notchWindowChange = false;
                    }
                }
                boolean isHideStatusbarInFreeform = this.mService.getPolicy().isNotchDisplayDisabled() && ((i = this.mDisplayRotation) == 0 || i == 2);
                if (this.mStatusBarController.isTransientShowing()) {
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 0 | 1;
                    }
                } else if ((!topIsFullscreen || (((!HwFreeFormUtils.isFreeFormEnable() || isHideStatusbarInFreeform) && this.mDisplayContent.isStackVisible(5)) || this.mDisplayContent.isStackVisible(3) || this.mService.mAtmService.mHwATMSEx.isSplitStackVisible(this.mDisplayContent.mAcitvityDisplay, 0))) && !isLeftRightSplitStackVisible()) {
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.v(TAG, "** SHOWING status bar: top is not fullscreen");
                    }
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 0 | 1;
                    }
                    topAppHidesStatusBar = false;
                } else {
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.v(TAG, "** HIDING status bar");
                    }
                    if (this.mStatusBarController.setBarShowingLw(false)) {
                        changes = 0 | 1;
                    } else if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.v(TAG, "Status bar already hiding");
                    }
                }
            }
            this.mStatusBarController.setTopAppHidesStatusBar(topAppHidesStatusBar);
        }
        if (this.mTopIsFullscreen != topIsFullscreen) {
            if (!topIsFullscreen) {
                changes |= 1;
            }
            this.mTopIsFullscreen = topIsFullscreen;
        }
        if ((updateSystemUiVisibilityLw() & SYSTEM_UI_CHANGING_LAYOUT) != 0) {
            changes |= 1;
        }
        boolean z2 = this.mShowingDream;
        if (z2 != this.mLastShowingDream) {
            this.mLastShowingDream = z2;
            this.mService.notifyShowingDreamChanged();
        }
        updateWindowSleepToken();
        this.mService.mPolicy.setAllowLockscreenWhenOn(getDisplayId(), this.mAllowLockscreenWhenOn);
        return changes;
    }

    private void updateWindowSleepToken() {
        if (this.mWindowSleepTokenNeeded && !this.mLastWindowSleepTokenNeeded) {
            this.mHandler.removeCallbacks(this.mReleaseSleepTokenRunnable);
            this.mHandler.post(this.mAcquireSleepTokenRunnable);
        } else if (!this.mWindowSleepTokenNeeded && this.mLastWindowSleepTokenNeeded) {
            this.mHandler.removeCallbacks(this.mAcquireSleepTokenRunnable);
            this.mHandler.post(this.mReleaseSleepTokenRunnable);
        }
        this.mLastWindowSleepTokenNeeded = this.mWindowSleepTokenNeeded;
    }

    private boolean topAppHidesStatusBar() {
        boolean isFreeformHideStatusBar;
        if (this.mTopFullscreenOpaqueWindowState == null) {
            return false;
        }
        if (isLeftRightSplitStackVisible()) {
            return true;
        }
        int fl = PolicyControl.getWindowFlags(null, this.mTopFullscreenOpaqueWindowState.getAttrs());
        if (IS_NOTCH_PROP) {
            if (this.mService.getPolicy().isNotchDisplayDisabled()) {
                isFreeformHideStatusBar = true;
            } else {
                WindowState windowState = this.mFocusedWindow;
                isFreeformHideStatusBar = windowState == null || !windowState.inFreeformWindowingMode();
            }
            if (!this.mService.getPolicy().hideNotchStatusBar(fl) && isFreeformHideStatusBar) {
                return false;
            }
            this.mForceNotchStatusBar = false;
        }
        if ((fl & 1024) == 0 && (this.mLastSystemUiFlags & 4) == 0) {
            return false;
        }
        return true;
    }

    public void switchUser() {
        updateCurrentUserResources();
    }

    public void onOverlayChangedLw() {
        updateCurrentUserResources();
        onConfigurationChanged();
        this.mSystemGestures.onConfigurationChanged();
    }

    public void onConfigurationChanged() {
        DisplayRotation displayRotation = this.mDisplayContent.getDisplayRotation();
        Resources res = getCurrentUserResources();
        int portraitRotation = displayRotation.getPortraitRotation();
        int upsideDownRotation = displayRotation.getUpsideDownRotation();
        int landscapeRotation = displayRotation.getLandscapeRotation();
        int seascapeRotation = displayRotation.getSeascapeRotation();
        int uiMode = this.mService.mPolicy.getUiMode();
        if (hasStatusBar()) {
            int[] iArr = this.mStatusBarHeightForRotation;
            int dimensionPixelSize = res.getDimensionPixelSize(17105447);
            iArr[upsideDownRotation] = dimensionPixelSize;
            iArr[portraitRotation] = dimensionPixelSize;
            int[] iArr2 = this.mStatusBarHeightForRotation;
            int dimensionPixelSize2 = res.getDimensionPixelSize(17105446);
            iArr2[seascapeRotation] = dimensionPixelSize2;
            iArr2[landscapeRotation] = dimensionPixelSize2;
        } else {
            int[] iArr3 = this.mStatusBarHeightForRotation;
            iArr3[seascapeRotation] = 0;
            iArr3[landscapeRotation] = 0;
            iArr3[upsideDownRotation] = 0;
            iArr3[portraitRotation] = 0;
        }
        int[] iArr4 = this.mNavigationBarHeightForRotationDefault;
        int dimensionPixelSize3 = res.getDimensionPixelSize(17105307);
        iArr4[upsideDownRotation] = dimensionPixelSize3;
        iArr4[portraitRotation] = dimensionPixelSize3;
        int[] iArr5 = this.mNavigationBarHeightForRotationDefault;
        int dimensionPixelSize4 = res.getDimensionPixelSize(17105309);
        iArr5[seascapeRotation] = dimensionPixelSize4;
        iArr5[landscapeRotation] = dimensionPixelSize4;
        int[] iArr6 = this.mNavigationBarFrameHeightForRotationDefault;
        int dimensionPixelSize5 = res.getDimensionPixelSize(17105304);
        iArr6[upsideDownRotation] = dimensionPixelSize5;
        iArr6[portraitRotation] = dimensionPixelSize5;
        int[] iArr7 = this.mNavigationBarFrameHeightForRotationDefault;
        int dimensionPixelSize6 = res.getDimensionPixelSize(17105305);
        iArr7[seascapeRotation] = dimensionPixelSize6;
        iArr7[landscapeRotation] = dimensionPixelSize6;
        int[] iArr8 = this.mNavigationBarWidthForRotationDefault;
        int dimensionPixelSize7 = res.getDimensionPixelSize(17105312);
        iArr8[seascapeRotation] = dimensionPixelSize7;
        iArr8[landscapeRotation] = dimensionPixelSize7;
        iArr8[upsideDownRotation] = dimensionPixelSize7;
        iArr8[portraitRotation] = dimensionPixelSize7;
        this.mNavBarOpacityMode = res.getInteger(17694850);
        this.mSideGestureInset = res.getDimensionPixelSize(17105049);
        this.mNavigationBarLetsThroughTaps = res.getBoolean(17891486);
        this.mNavigationBarAlwaysShowOnSideGesture = res.getBoolean(17891483);
        this.mBottomGestureAdditionalInset = res.getDimensionPixelSize(17105306) - getNavigationBarFrameHeight(portraitRotation, uiMode);
        updateConfigurationAndScreenSizeDependentBehaviors();
        this.mWindowOutsetBottom = ScreenShapeHelper.getWindowOutsetBottomPx(this.mContext.getResources());
        IHwDisplayPolicyEx iHwDisplayPolicyEx = this.mHwDisplayPolicyEx;
        if (iHwDisplayPolicyEx != null) {
            iHwDisplayPolicyEx.initialNavigationSize(this.mDisplayContent.getDisplay(), this.mDisplayContent.mBaseDisplayWidth, this.mDisplayContent.mBaseDisplayHeight, this.mDisplayContent.mBaseDisplayDensity);
            this.mHwDisplayPolicyEx.onConfigurationChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateConfigurationAndScreenSizeDependentBehaviors() {
        Resources res = getCurrentUserResources();
        this.mNavigationBarCanMove = this.mDisplayContent.mBaseDisplayWidth != this.mDisplayContent.mBaseDisplayHeight && res.getBoolean(17891484);
        this.mAllowSeamlessRotationDespiteNavBarMoving = res.getBoolean(17891346);
    }

    private void updateCurrentUserResources() {
        int userId = this.mService.mAmInternal.getCurrentUserId();
        Context uiContext = getSystemUiContext();
        if (userId == 0) {
            this.mCurrentUserResources = uiContext.getResources();
            return;
        }
        LoadedApk pi = ActivityThread.currentActivityThread().getPackageInfo(uiContext.getPackageName(), (CompatibilityInfo) null, 0, userId);
        this.mCurrentUserResources = ResourcesManager.getInstance().getResources((IBinder) null, pi.getResDir(), (String[]) null, pi.getOverlayDirs(), pi.getApplicationInfo().sharedLibraryFiles, this.mDisplayContent.getDisplayId(), (Configuration) null, uiContext.getResources().getCompatibilityInfo(), (ClassLoader) null);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Resources getCurrentUserResources() {
        if (this.mCurrentUserResources == null) {
            updateCurrentUserResources();
        }
        return this.mCurrentUserResources;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Context getContext() {
        return this.mContext;
    }

    private Context getSystemUiContext() {
        Context uiContext = ActivityThread.currentActivityThread().getSystemUiContext();
        if (this.mDisplayContent.isDefaultDisplay) {
            return uiContext;
        }
        return uiContext.createDisplayContext(this.mDisplayContent.getDisplay());
    }

    private int getNavigationBarWidth(int rotation, int uiMode) {
        return this.mNavigationBarWidthForRotationDefault[rotation];
    }

    /* access modifiers changed from: package-private */
    public void notifyDisplayReady() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$DisplayPolicy$mUPXUZKrPpeFUjrauzoJMNbYjM */

            @Override // java.lang.Runnable
            public final void run() {
                DisplayPolicy.this.lambda$notifyDisplayReady$9$DisplayPolicy();
            }
        });
    }

    public /* synthetic */ void lambda$notifyDisplayReady$9$DisplayPolicy() {
        int displayId = getDisplayId();
        StatusBarManagerInternal statusBar = getStatusBarManagerInternal();
        if (statusBar != null) {
            statusBar.onDisplayReady(displayId);
        }
        ((WallpaperManagerInternal) LocalServices.getService(WallpaperManagerInternal.class)).onDisplayReady(displayId);
    }

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, DisplayCutout displayCutout) {
        int navBarPosition;
        int widthForCar = this.mHwDisplayPolicyEx.getNonDecorDisplayWidthForExtraDisplay(fullWidth, getDisplayId());
        if (widthForCar != -1) {
            return widthForCar;
        }
        int width = fullWidth;
        if (hasNavigationBar() && ((navBarPosition = navigationBarPosition(fullWidth, fullHeight, rotation)) == 1 || navBarPosition == 2)) {
            width -= getNavigationBarWidth(rotation, uiMode);
        }
        if (displayCutout != null) {
            return width - (displayCutout.getSafeInsetLeft() + displayCutout.getSafeInsetRight());
        }
        return width;
    }

    private int getNavigationBarHeight(int rotation, int uiMode) {
        return this.mNavigationBarHeightForRotationDefault[rotation];
    }

    private int getNavigationBarFrameHeight(int rotation, int uiMode) {
        return this.mNavigationBarFrameHeightForRotationDefault[rotation];
    }

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, DisplayCutout displayCutout) {
        int height = fullHeight;
        if ((uiMode & 15) != 3 && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(getDisplayId())) {
            return this.mHwDisplayPolicyEx.getNonDecorDisplayHeight(fullHeight, getDisplayId());
        }
        if (hasNavigationBar() && navigationBarPosition(fullWidth, fullHeight, rotation) == 4) {
            height -= getNavigationBarHeight(rotation, uiMode);
        }
        if (displayCutout != null) {
            return height - (displayCutout.getSafeInsetTop() + displayCutout.getSafeInsetBottom());
        }
        return height;
    }

    public int getConfigDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, DisplayCutout displayCutout) {
        return getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode, displayCutout);
    }

    public int getConfigDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, DisplayCutout displayCutout) {
        int statusBarHeight = this.mStatusBarHeightForRotation[rotation];
        if (displayCutout != null) {
            statusBarHeight = Math.max(0, statusBarHeight - displayCutout.getSafeInsetTop());
        }
        return getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayCutout) - statusBarHeight;
    }

    /* access modifiers changed from: package-private */
    public float getWindowCornerRadius() {
        if (this.mDisplayContent.getDisplay().getType() == 1) {
            return ScreenDecorationsUtils.getWindowCornerRadius(this.mContext.getResources());
        }
        return 0.0f;
    }

    /* access modifiers changed from: package-private */
    public boolean isShowingDreamLw() {
        return this.mShowingDream;
    }

    /* access modifiers changed from: package-private */
    public void convertNonDecorInsetsToStableInsets(Rect inOutInsets, int rotation) {
        inOutInsets.top = Math.max(inOutInsets.top, this.mStatusBarHeightForRotation[rotation]);
    }

    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight, DisplayCutout displayCutout, Rect outInsets) {
        if (!this.mHwDisplayPolicyEx.getStableInsetsForPC(outInsets, getDisplayId())) {
            outInsets.setEmpty();
            getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, displayCutout, outInsets);
            convertNonDecorInsetsToStableInsets(outInsets, displayRotation);
        }
    }

    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight, DisplayCutout displayCutout, Rect outInsets) {
        if (!this.mHwDisplayPolicyEx.getNonDecorInsetsForPC(outInsets, getDisplayId())) {
            outInsets.setEmpty();
            if (hasNavigationBar()) {
                int uiMode = this.mService.mPolicy.getUiMode();
                int position = navigationBarPosition(displayWidth, displayHeight, displayRotation);
                if (position == 4) {
                    outInsets.bottom = getNavigationBarHeight(displayRotation, uiMode);
                } else if (position == 2) {
                    outInsets.right = getNavigationBarWidth(displayRotation, uiMode);
                } else if (position == 1) {
                    outInsets.left = getNavigationBarWidth(displayRotation, uiMode);
                }
            }
            if (displayCutout != null) {
                outInsets.left += displayCutout.getSafeInsetLeft();
                outInsets.top += displayCutout.getSafeInsetTop();
                outInsets.right += displayCutout.getSafeInsetRight();
                outInsets.bottom += displayCutout.getSafeInsetBottom();
            }
        }
    }

    public void setForwardedInsets(Insets forwardedInsets) {
        this.mForwardedInsets = forwardedInsets;
    }

    public Insets getForwardedInsets() {
        return this.mForwardedInsets;
    }

    /* access modifiers changed from: package-private */
    public int navigationBarPosition(int displayWidth, int displayHeight, int displayRotation) {
        if (!navigationBarCanMove() || displayWidth <= displayHeight) {
            return 4;
        }
        if (displayRotation == 3 || displayRotation == 1) {
            return 2;
        }
        return 4;
    }

    public int getNavBarPosition() {
        return this.mNavigationBarPosition;
    }

    public int focusChangedLw(WindowState lastFocus, WindowState newFocus) {
        if (this.mHwDisplayPolicyEx.focusChangedLwForPC(newFocus)) {
            return 1;
        }
        IHwDisplayPolicyEx iHwDisplayPolicyEx = this.mHwDisplayPolicyEx;
        if (iHwDisplayPolicyEx != null) {
            iHwDisplayPolicyEx.focusChangedLw(lastFocus, newFocus);
        }
        this.mFocusedWindow = newFocus;
        this.mLastFocusedWindow = lastFocus;
        if (this.mDisplayContent.isDefaultDisplay) {
            this.mService.mPolicy.onDefaultDisplayFocusChangedLw(newFocus);
        }
        WindowState windowState = this.mFocusedWindow;
        if (windowState != null) {
            if (IS_NOTCH_PROP && windowState.toString().contains(LAUNCHER_PACKAGE_NAME)) {
                this.mForceNotchStatusBar = false;
            }
            updateSystemUiColorLw(this.mFocusedWindow);
            WindowState windowState2 = this.mLastStartingWindow;
            if (windowState2 != null && windowState2.isVisibleLw() && this.mFocusedWindow.getAttrs() != null && this.mFocusedWindow.getAttrs().type == 2003) {
                updateSystemUiColorLw(this.mLastStartingWindow);
            }
        }
        if ((updateSystemUiVisibilityLw() & SYSTEM_UI_CHANGING_LAYOUT) != 0) {
            return 1;
        }
        return 0;
    }

    public boolean allowAppAnimationsLw() {
        return !this.mShowingDream;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDreamingSleepToken(boolean acquire) {
        if (acquire) {
            int displayId = getDisplayId();
            if (this.mDreamingSleepToken == null) {
                ActivityTaskManagerInternal activityTaskManagerInternal = this.mService.mAtmInternal;
                this.mDreamingSleepToken = activityTaskManagerInternal.acquireSleepToken("DreamOnDisplay" + displayId, displayId);
                return;
            }
            return;
        }
        ActivityTaskManagerInternal.SleepToken sleepToken = this.mDreamingSleepToken;
        if (sleepToken != null) {
            sleepToken.release();
            this.mDreamingSleepToken = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestTransientBars(WindowState swipeTarget) {
        synchronized (this.mLock) {
            if (this.mService.mPolicy.isUserSetupComplete()) {
                boolean sb = this.mStatusBarController.checkShowTransientBarLw();
                boolean nb = this.mNavigationBarController.checkShowTransientBarLw() && !isNavBarEmpty(this.mLastSystemUiFlags);
                if (sb || nb) {
                    if (nb || swipeTarget != this.mNavigationBar) {
                        if (sb) {
                            this.mStatusBarController.showTransient();
                        }
                        if (nb) {
                            this.mNavigationBarController.showTransient();
                        }
                        this.mImmersiveModeConfirmation.confirmCurrentPrompt();
                        updateSystemUiVisibilityLw();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disposeInputConsumer(WindowManagerPolicy.InputConsumer inputConsumer) {
        if (inputConsumer != null) {
            inputConsumer.dismiss();
        }
        this.mInputConsumer = null;
    }

    private boolean isStatusBarKeyguard() {
        WindowState windowState = this.mStatusBar;
        return (windowState == null || (windowState.getAttrs().privateFlags & 1024) == 0) ? false : true;
    }

    private boolean isKeyguardOccluded() {
        return this.mService.mPolicy.isKeyguardOccluded();
    }

    /* access modifiers changed from: package-private */
    public void resetSystemUiVisibilityLw() {
        this.mLastSystemUiFlags = 0;
        updateSystemUiVisibilityLw();
    }

    private boolean shouldSkipUpdateSystemUiVisibility(WindowState win) {
        return win != null && (win.inFreeformWindowingMode() || win.inHwFreeFormWindowingMode() || (win.getAttrs().hwFlags & 256) != 0);
    }

    private int updateSystemUiVisibilityLw() {
        WindowState winCandidate;
        WindowState winCandidate2;
        int tmpVisibility;
        WindowState winCandidate3;
        WindowState windowState = this.mFocusedWindow;
        if (windowState == null || shouldSkipUpdateSystemUiVisibility(windowState)) {
            winCandidate = this.mTopFullscreenOpaqueWindowState;
        } else {
            winCandidate = this.mFocusedWindow;
        }
        if (winCandidate == null) {
            return 0;
        }
        if (winCandidate.getAttrs().token == this.mImmersiveModeConfirmation.getWindowToken()) {
            WindowState windowState2 = this.mLastFocusedWindow;
            boolean lastFocusCanReceiveKeys = windowState2 != null && windowState2.canReceiveKeys();
            if (isStatusBarKeyguard()) {
                winCandidate3 = this.mStatusBar;
            } else if (lastFocusCanReceiveKeys) {
                winCandidate3 = this.mLastFocusedWindow;
            } else {
                winCandidate3 = this.mTopFullscreenOpaqueWindowState;
            }
            if (winCandidate3 == null) {
                return 0;
            }
            winCandidate2 = winCandidate3;
        } else {
            winCandidate2 = winCandidate;
        }
        if (winCandidate2.getAttrs().type == 3 && this.mLastStartingWindow != winCandidate2) {
            this.mLastStartingWindow = winCandidate2;
        }
        if ((winCandidate2.getAttrs().privateFlags & 1024) != 0 && isKeyguardOccluded()) {
            return 0;
        }
        this.mDisplayContent.getInsetsStateController().onBarControllingWindowChanged(this.mTopFullscreenOpaqueWindowState);
        int tmpVisibility2 = PolicyControl.getSystemUiVisibility(winCandidate2, null) & (~this.mResettingSystemUiFlags) & (~this.mForceClearedSystemUiFlags);
        if (!this.mForcingShowNavBar || winCandidate2.getSurfaceLayer() >= this.mForcingShowNavBarLayer) {
            tmpVisibility = tmpVisibility2;
        } else {
            tmpVisibility = tmpVisibility2 & (~PolicyControl.adjustClearableFlags(winCandidate2, 7));
        }
        int fullscreenVisibility = updateLightStatusBarLw(0, this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState);
        int dockedVisibility = updateLightStatusBarLw(0, this.mTopDockedOpaqueWindowState, this.mTopDockedOpaqueOrDimmingWindowState);
        this.mService.getStackBounds(0, 2, this.mNonDockedStackBounds);
        this.mService.getStackBounds(3, 1, this.mDockedStackBounds);
        if (IS_HW_MULTIWINDOW_SUPPORTED && this.mDockedStackBounds.isEmpty()) {
            synchronized (this.mService.getGlobalLock()) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskStack stack = this.mService.getRoot().getStack(100, 1);
                    if (stack == null || !stack.isVisible()) {
                        this.mDockedStackBounds.setEmpty();
                    } else {
                        stack.getBounds(this.mDockedStackBounds);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
        Pair<Integer, Boolean> result = updateSystemBarsLw(winCandidate2, this.mLastSystemUiFlags, tmpVisibility);
        int visibility = ((Integer) result.first).intValue();
        int diff = visibility ^ this.mLastSystemUiFlags;
        int fullscreenDiff = fullscreenVisibility ^ this.mLastFullscreenStackSysUiFlags;
        int dockedDiff = dockedVisibility ^ this.mLastDockedStackSysUiFlags;
        boolean needsMenu = winCandidate2.getNeedsMenuLw(this.mTopFullscreenOpaqueWindowState);
        if (diff == 0 && fullscreenDiff == 0 && dockedDiff == 0 && this.mLastFocusNeedsMenu == needsMenu && this.mFocusedApp == winCandidate2.getAppToken() && this.mLastNonDockedStackBounds.equals(this.mNonDockedStackBounds) && this.mLastDockedStackBounds.equals(this.mDockedStackBounds) && this.mLastHwNavColor == this.mHwNavColor) {
            return 0;
        }
        this.mLastSystemUiFlags = visibility;
        this.mLastHwNavColor = this.mHwNavColor;
        this.mLastFullscreenStackSysUiFlags = fullscreenVisibility;
        this.mLastDockedStackSysUiFlags = dockedVisibility;
        this.mLastFocusNeedsMenu = needsMenu;
        this.mFocusedApp = winCandidate2.getAppToken();
        this.mLastNonDockedStackBounds.set(this.mNonDockedStackBounds);
        this.mLastDockedStackBounds.set(this.mDockedStackBounds);
        this.mHandler.post(new Runnable(visibility, winCandidate2, fullscreenVisibility, dockedVisibility, new Rect(this.mNonDockedStackBounds), new Rect(this.mDockedStackBounds), ((Boolean) result.second).booleanValue(), needsMenu) {
            /* class com.android.server.wm.$$Lambda$DisplayPolicy$WwV5WoEtlIbZTXpSEJQf7ozaI */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ WindowState f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ int f$4;
            private final /* synthetic */ Rect f$5;
            private final /* synthetic */ Rect f$6;
            private final /* synthetic */ boolean f$7;
            private final /* synthetic */ boolean f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
            }

            @Override // java.lang.Runnable
            public final void run() {
                DisplayPolicy.this.lambda$updateSystemUiVisibilityLw$10$DisplayPolicy(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        });
        return diff;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0032: APUT  
      (r3v2 java.lang.Object[])
      (2 ??[int, float, short, byte, char])
      (wrap: java.lang.Boolean : 0x002e: INVOKE  (r4v3 java.lang.Boolean) = (r4v2 boolean) type: STATIC call: java.lang.Boolean.valueOf(boolean):java.lang.Boolean)
     */
    public /* synthetic */ void lambda$updateSystemUiVisibilityLw$10$DisplayPolicy(int visibility, WindowState win, int fullscreenVisibility, int dockedVisibility, Rect fullscreenStackBounds, Rect dockedStackBounds, boolean isNavbarColorManagedByIme, boolean needsMenu) {
        int vis;
        StatusBarManagerInternal statusBar = getStatusBarManagerInternal();
        if (statusBar != null) {
            int displayId = getDisplayId();
            int vis2 = visibility;
            if (this.mHwNavColor) {
                vis2 = visibility | 16;
            }
            if (win.inHwMagicWindowingMode()) {
                Object[] objArr = new Object[3];
                boolean z = false;
                objArr[0] = win;
                objArr[1] = Integer.valueOf(vis2);
                if (this.mNavigationBarPosition == 4) {
                    z = true;
                }
                objArr[2] = Boolean.valueOf(z);
                vis = HwMwUtils.performPolicy((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, objArr).getInt("RESULT_UPDATE_SYSUIVISIBILITY", vis2);
            } else {
                vis = vis2;
            }
            statusBar.setSystemUiVisibility(displayId, vis, fullscreenVisibility, dockedVisibility, -1, fullscreenStackBounds, dockedStackBounds, isNavbarColorManagedByIme, win.toString());
            statusBar.topAppWindowChanged(displayId, needsMenu);
        }
    }

    public void updateSystemUiColorLw(WindowState win) {
        this.mService.getPolicy().updateSystemUiColorLw(win);
    }

    private int updateLightStatusBarLw(int vis, WindowState opaque, WindowState opaqueOrDimming) {
        boolean onKeyguard = isStatusBarKeyguard() && !isKeyguardOccluded();
        WindowState statusColorWin = onKeyguard ? this.mStatusBar : opaqueOrDimming;
        if (statusColorWin != null && (statusColorWin == opaque || onKeyguard)) {
            return (vis & -8193) | (PolicyControl.getSystemUiVisibility(statusColorWin, null) & 8192);
        }
        if (statusColorWin == null || !statusColorWin.isDimming()) {
            return vis;
        }
        return vis & -8193;
    }

    @VisibleForTesting
    static WindowState chooseNavigationColorWindowLw(WindowState opaque, WindowState opaqueOrDimming, WindowState imeWindow, int navBarPosition) {
        boolean imeWindowCanNavColorWindow = imeWindow != null && imeWindow.isVisibleLw() && navBarPosition == 4 && (PolicyControl.getWindowFlags(imeWindow, null) & Integer.MIN_VALUE) != 0;
        if (opaque != null && opaqueOrDimming == opaque) {
            return imeWindowCanNavColorWindow ? imeWindow : opaque;
        }
        if (opaqueOrDimming == null || !opaqueOrDimming.isDimming()) {
            if (imeWindowCanNavColorWindow) {
                return imeWindow;
            }
            return null;
        } else if (imeWindowCanNavColorWindow && WindowManager.LayoutParams.mayUseInputMethod(PolicyControl.getWindowFlags(opaqueOrDimming, null))) {
            return imeWindow;
        } else {
            return opaqueOrDimming;
        }
    }

    @VisibleForTesting
    static int updateLightNavigationBarLw(int vis, WindowState opaque, WindowState opaqueOrDimming, WindowState imeWindow, WindowState navColorWin) {
        if (navColorWin == null) {
            return vis;
        }
        if (navColorWin == imeWindow || navColorWin == opaque) {
            int vis2 = vis & -17;
            if (navColorWin == imeWindow && (navColorWin.getAttrs().hwFlags & 16) != 0) {
                vis2 |= 16;
                Slog.i(TAG, "navColorWin was set to default navbar color so should add SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR " + Integer.toHexString(vis2));
            }
            return vis2 | (PolicyControl.getSystemUiVisibility(navColorWin, null) & 16);
        } else if (navColorWin != opaqueOrDimming || !navColorWin.isDimming()) {
            return vis;
        } else {
            return vis & -17;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:123:0x01b5, code lost:
        if (r37.mForceShowSystemBars != false) goto L_0x01ba;
     */
    private Pair<Integer, Boolean> updateSystemBarsLw(WindowState win, int oldVis, int vis) {
        boolean freeformStackVisible;
        WindowState fullscreenTransWin;
        int vis2;
        boolean z;
        boolean isManagedByIme;
        boolean dockedStackVisible = this.mDisplayContent.isStackVisible(3) || this.mDisplayContent.isStackVisible(100);
        if (HwFreeFormUtils.isFreeFormEnable()) {
            freeformStackVisible = false;
        } else {
            freeformStackVisible = this.mDisplayContent.isStackVisible(5);
        }
        boolean resizing = this.mDisplayContent.getDockedDividerController().isResizing();
        this.mForceShowSystemBars = dockedStackVisible || freeformStackVisible || resizing || this.mForceShowSystemBarsFromExternal;
        boolean forceOpaqueStatusBar = this.mForceShowSystemBars && !this.mForceStatusBarFromKeyguard;
        if (!isStatusBarKeyguard() || isKeyguardOccluded()) {
            fullscreenTransWin = this.mTopFullscreenOpaqueWindowState;
        } else {
            fullscreenTransWin = this.mStatusBar;
        }
        int vis3 = this.mNavigationBarController.applyTranslucentFlagLw(fullscreenTransWin, this.mStatusBarController.applyTranslucentFlagLw(fullscreenTransWin, vis, oldVis), oldVis);
        int dockedVis = this.mNavigationBarController.applyTranslucentFlagLw(this.mTopDockedOpaqueWindowState, this.mStatusBarController.applyTranslucentFlagLw(this.mTopDockedOpaqueWindowState, 0, 0), 0);
        boolean fullscreenDrawsStatusBarBackground = drawsStatusBarBackground(vis3, this.mTopFullscreenOpaqueWindowState);
        boolean dockedDrawsStatusBarBackground = drawsStatusBarBackground(dockedVis, this.mTopDockedOpaqueWindowState);
        boolean fullscreenDrawsNavBarBackground = drawsNavigationBarBackground(vis3, this.mTopFullscreenOpaqueWindowState);
        boolean dockedDrawsNavigationBarBackground = drawsNavigationBarBackground(dockedVis, this.mTopDockedOpaqueWindowState);
        boolean statusBarHasFocus = win.getAttrs().type == 2000;
        if (statusBarHasFocus && !isStatusBarKeyguard()) {
            int flags = 14342;
            if (isKeyguardOccluded()) {
                flags = 14342 | -1073741824;
            }
            vis3 = ((~flags) & vis3) | (oldVis & flags);
        }
        if (fullscreenDrawsStatusBarBackground && dockedDrawsStatusBarBackground) {
            vis2 = (vis3 | 8) & -1073741825;
        } else if (forceOpaqueStatusBar) {
            vis2 = vis3 & -1073741833;
        } else {
            vis2 = vis3;
        }
        int vis4 = configureNavBarOpacity(vis2, dockedStackVisible, freeformStackVisible, resizing, fullscreenDrawsNavBarBackground, dockedDrawsNavigationBarBackground);
        boolean immersiveSticky = (vis4 & 4096) != 0;
        boolean isLeftRightSplitStackVisible = false;
        if (dockedStackVisible) {
            isLeftRightSplitStackVisible = isLeftRightSplitStackVisible();
        }
        WindowState windowState = this.mTopFullscreenOpaqueWindowState;
        boolean hideStatusBarWM = !(windowState == null || (PolicyControl.getWindowFlags(windowState, null) & 1024) == 0) || isLeftRightSplitStackVisible;
        boolean hideStatusBarSysui = (vis4 & 4) != 0;
        boolean hideNavBarSysui = (vis4 & 2) != 0;
        boolean transientStatusBarAllowed = this.mStatusBar != null && (statusBarHasFocus || ((!this.mForceShowSystemBars && (hideStatusBarWM || (hideStatusBarSysui && immersiveSticky))) || isLeftRightSplitStackVisible));
        boolean transientNavBarAllowed = this.mNavigationBar != null && ((!this.mForceShowSystemBars && hideNavBarSysui && hideStatusBarWM) || (!this.mForceShowSystemBars && hideNavBarSysui && immersiveSticky));
        long now = SystemClock.uptimeMillis();
        long j = this.mPendingPanicGestureUptime;
        boolean pendingPanic = j != 0 && now - j <= PANIC_GESTURE_EXPIRATION;
        DisplayPolicy defaultDisplayPolicy = this.mService.getDefaultDisplayContentLocked().getDisplayPolicy();
        if (pendingPanic && hideNavBarSysui && !isStatusBarKeyguard() && defaultDisplayPolicy.isKeyguardDrawComplete()) {
            this.mPendingPanicGestureUptime = 0;
            this.mStatusBarController.showTransient();
            if (!isNavBarEmpty(vis4)) {
                this.mNavigationBarController.showTransient();
            }
        }
        boolean denyTransientStatus = this.mStatusBarController.isTransientShowRequested() && !transientStatusBarAllowed && hideStatusBarSysui;
        boolean denyTransientNav = this.mNavigationBarController.isTransientShowRequested() && !transientNavBarAllowed;
        if (denyTransientStatus || denyTransientNav) {
        }
        clearClearableFlagsLw();
        vis4 &= -8;
        boolean navAllowedHidden = ((vis4 & 2048) != 0) || ((vis4 & 4096) != 0);
        if (hideNavBarSysui && !navAllowedHidden) {
            if (this.mService.mPolicy.getWindowLayerLw(win) > this.mService.mPolicy.getWindowLayerFromTypeLw(2022)) {
                vis4 &= -3;
            }
        }
        if (this.mService.mAtmService.mHwATMSEx.isSplitStackVisible(this.mDisplayContent.mAcitvityDisplay, 0) && !win.toString().contains("HwGlobalActions")) {
            vis4 &= -8201;
        }
        int vis5 = this.mStatusBarController.updateVisibilityLw(transientStatusBarAllowed, oldVis, vis4);
        boolean oldImmersiveMode = isImmersiveMode(oldVis);
        boolean newImmersiveMode = isImmersiveMode(vis5);
        this.mHwDisplayPolicyEx.setNaviImmersiveMode(newImmersiveMode);
        if (oldImmersiveMode != newImmersiveMode) {
            this.mImmersiveModeConfirmation.immersiveModeChangedLw(win.getOwningPackage(), newImmersiveMode, this.mService.mPolicy.isUserSetupComplete(), isNavBarEmpty(win.getSystemUiVisibility()));
        }
        int vis6 = this.mNavigationBarController.updateVisibilityLw(transientNavBarAllowed, oldVis, vis5);
        WindowState navColorWin = chooseNavigationColorWindowLw(this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState, this.mDisplayContent.mInputMethodWindow, this.mNavigationBarPosition);
        int vis7 = updateLightNavigationBarLw(vis6, this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState, this.mDisplayContent.mInputMethodWindow, navColorWin);
        if ((win.getAttrs().hwFlags & 16) == 0 || navColorWin != this.mTopFullscreenOpaqueWindowState) {
            isManagedByIme = true;
            z = false;
            this.mHwNavColor = false;
        } else {
            isManagedByIme = true;
            this.mHwNavColor = true;
            z = false;
        }
        if (navColorWin == null || navColorWin != this.mDisplayContent.mInputMethodWindow) {
            isManagedByIme = z;
        }
        return Pair.create(Integer.valueOf(vis7), Boolean.valueOf(isManagedByIme));
    }

    private boolean drawsBarBackground(int vis, WindowState win, BarController controller, int translucentFlag) {
        if (!controller.isTransparentAllowed(win)) {
            return false;
        }
        if (win == null) {
            return true;
        }
        boolean drawsSystemBars = (win.getAttrs().flags & Integer.MIN_VALUE) != 0;
        if ((win.getAttrs().privateFlags & 131072) != 0) {
            return true;
        }
        if (!drawsSystemBars || (vis & translucentFlag) != 0) {
            return false;
        }
        return true;
    }

    private boolean drawsStatusBarBackground(int vis, WindowState win) {
        return drawsBarBackground(vis, win, this.mStatusBarController, 67108864);
    }

    private boolean drawsNavigationBarBackground(int vis, WindowState win) {
        return drawsBarBackground(vis, win, this.mNavigationBarController, 134217728);
    }

    private int configureNavBarOpacity(int visibility, boolean dockedStackVisible, boolean freeformStackVisible, boolean isDockedDividerResizing, boolean fullscreenDrawsBackground, boolean dockedDrawsNavigationBarBackground) {
        int i = this.mNavBarOpacityMode;
        if (i == 2) {
            if (fullscreenDrawsBackground && dockedDrawsNavigationBarBackground) {
                return setNavBarTransparentFlag(visibility);
            }
            if (dockedStackVisible) {
                return setNavBarOpaqueFlag(visibility);
            }
            return visibility;
        } else if (i == 0) {
            if (dockedStackVisible || freeformStackVisible || isDockedDividerResizing) {
                return setNavBarOpaqueFlag(visibility);
            }
            if (fullscreenDrawsBackground) {
                return setNavBarTransparentFlag(visibility);
            }
            return visibility;
        } else if (i != 1) {
            return visibility;
        } else {
            if (isDockedDividerResizing) {
                return setNavBarOpaqueFlag(visibility);
            }
            if (freeformStackVisible) {
                return setNavBarTranslucentFlag(visibility);
            }
            return setNavBarOpaqueFlag(visibility);
        }
    }

    private int setNavBarOpaqueFlag(int visibility) {
        return 2147450879 & visibility;
    }

    private int setNavBarTranslucentFlag(int visibility) {
        return Integer.MIN_VALUE | (visibility & -32769);
    }

    private int setNavBarTransparentFlag(int visibility) {
        return 32768 | (visibility & Integer.MAX_VALUE);
    }

    private void clearClearableFlagsLw() {
        int i = this.mResettingSystemUiFlags;
        int newVal = i | 7;
        if (newVal != i) {
            this.mResettingSystemUiFlags = newVal;
            this.mDisplayContent.reevaluateStatusBarVisibility();
        }
    }

    private boolean isImmersiveMode(int vis) {
        return (this.mNavigationBar == null || (vis & 2) == 0 || (vis & 6144) == 0 || !canHideNavigationBar()) ? false : true;
    }

    private boolean canHideNavigationBar() {
        return hasNavigationBar();
    }

    /* access modifiers changed from: private */
    public static boolean isNavBarEmpty(int systemUiFlags) {
        return (systemUiFlags & 23068672) == 23068672;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldRotateSeamlessly(DisplayRotation displayRotation, int oldRotation, int newRotation) {
        WindowState w;
        if (oldRotation == displayRotation.getUpsideDownRotation() || newRotation == displayRotation.getUpsideDownRotation()) {
            return false;
        }
        if ((!(isFromSeamlessLauncher() && !navigationBarCanMove()) && !navigationBarCanMove() && !this.mAllowSeamlessRotationDespiteNavBarMoving) || (w = this.mTopFullscreenOpaqueWindowState) == null || w != this.mFocusedWindow) {
            return false;
        }
        if ((w.mAppToken == null || w.mAppToken.matchParentBounds()) && !w.isAnimatingLw() && w.getAttrs().rotationAnimation == 3) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isFromSeamlessLauncher() {
        WindowState windowState = this.mTopFullscreenOpaqueWindowState;
        return windowState != null && windowState.getAttrs() != null && LAUNCHER_PACKAGE_NAME.equals(this.mTopFullscreenOpaqueWindowState.getAttrs().packageName) && this.mTopFullscreenOpaqueWindowState.getAttrs().rotationAnimation == 3;
    }

    /* access modifiers changed from: package-private */
    public void onPowerKeyDown(boolean isScreenOn) {
        if (this.mImmersiveModeConfirmation.onPowerKeyDown(isScreenOn, SystemClock.elapsedRealtime(), isImmersiveMode(this.mLastSystemUiFlags), isNavBarEmpty(this.mLastSystemUiFlags))) {
            this.mHandler.post(this.mHiddenNavPanic);
        }
    }

    /* access modifiers changed from: package-private */
    public void onVrStateChangedLw(boolean enabled) {
        this.mImmersiveModeConfirmation.onVrStateChangedLw(enabled);
    }

    public void onLockTaskStateChangedLw(int lockTaskState) {
        this.mImmersiveModeConfirmation.onLockTaskModeChangedLw(lockTaskState);
        IHwDisplayPolicyEx iHwDisplayPolicyEx = this.mHwDisplayPolicyEx;
        if (iHwDisplayPolicyEx != null) {
            iHwDisplayPolicyEx.onLockTaskStateChangedLw(lockTaskState);
        }
    }

    public void takeScreenshot(int screenshotType) {
        ScreenshotHelper screenshotHelper = this.mScreenshotHelper;
        if (screenshotHelper != null) {
            WindowState windowState = this.mStatusBar;
            boolean z = true;
            boolean z2 = windowState != null && windowState.isVisibleLw();
            WindowState windowState2 = this.mNavigationBar;
            if (windowState2 == null || !windowState2.isVisibleLw()) {
                z = false;
            }
            screenshotHelper.takeScreenshot(screenshotType, z2, z, this.mHandler);
        }
    }

    /* access modifiers changed from: package-private */
    public RefreshRatePolicy getRefreshRatePolicy() {
        return this.mRefreshRatePolicy;
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("DisplayPolicy");
        String prefix2 = prefix + "  ";
        pw.print(prefix2);
        pw.print("mCarDockEnablesAccelerometer=");
        pw.print(this.mCarDockEnablesAccelerometer);
        pw.print(" mDeskDockEnablesAccelerometer=");
        pw.println(this.mDeskDockEnablesAccelerometer);
        pw.print(prefix2);
        pw.print("mDockMode=");
        pw.print(Intent.dockStateToString(this.mDockMode));
        pw.print(" mLidState=");
        pw.println(WindowManagerPolicy.WindowManagerFuncs.lidStateToString(this.mLidState));
        pw.print(prefix2);
        pw.print("mAwake=");
        pw.print(this.mAwake);
        pw.print(" mScreenOnEarly=");
        pw.print(this.mScreenOnEarly);
        pw.print(" mScreenOnFully=");
        pw.println(this.mScreenOnFully);
        pw.print(prefix2);
        pw.print("mKeyguardDrawComplete=");
        pw.print(this.mKeyguardDrawComplete);
        pw.print(" mWindowManagerDrawComplete=");
        pw.println(this.mWindowManagerDrawComplete);
        pw.print(prefix2);
        pw.print("mHdmiPlugged=");
        pw.println(this.mHdmiPlugged);
        if (!(this.mLastSystemUiFlags == 0 && this.mResettingSystemUiFlags == 0 && this.mForceClearedSystemUiFlags == 0)) {
            pw.print(prefix2);
            pw.print("mLastSystemUiFlags=0x");
            pw.print(Integer.toHexString(this.mLastSystemUiFlags));
            pw.print(" mResettingSystemUiFlags=0x");
            pw.print(Integer.toHexString(this.mResettingSystemUiFlags));
            pw.print(" mForceClearedSystemUiFlags=0x");
            pw.println(Integer.toHexString(this.mForceClearedSystemUiFlags));
        }
        if (this.mLastFocusNeedsMenu) {
            pw.print(prefix2);
            pw.print("mLastFocusNeedsMenu=");
            pw.println(this.mLastFocusNeedsMenu);
        }
        pw.print(prefix2);
        pw.print("mShowingDream=");
        pw.print(this.mShowingDream);
        pw.print(" mDreamingLockscreen=");
        pw.print(this.mDreamingLockscreen);
        pw.print(" mDreamingSleepToken=");
        pw.println(this.mDreamingSleepToken);
        if (this.mStatusBar != null) {
            pw.print(prefix2);
            pw.print("mStatusBar=");
            pw.print(this.mStatusBar);
            pw.print(" isStatusBarKeyguard=");
            pw.println(isStatusBarKeyguard());
        }
        if (this.mNavigationBar != null) {
            pw.print(prefix2);
            pw.print("mNavigationBar=");
            pw.println(this.mNavigationBar);
            pw.print(prefix2);
            pw.print("mNavBarOpacityMode=");
            pw.println(this.mNavBarOpacityMode);
            pw.print(prefix2);
            pw.print("mNavigationBarCanMove=");
            pw.println(this.mNavigationBarCanMove);
            pw.print(prefix2);
            pw.print("mNavigationBarPosition=");
            pw.println(this.mNavigationBarPosition);
        }
        if (this.mFocusedWindow != null) {
            pw.print(prefix2);
            pw.print("mFocusedWindow=");
            pw.println(this.mFocusedWindow);
        }
        if (this.mFocusedApp != null) {
            pw.print(prefix2);
            pw.print("mFocusedApp=");
            pw.println(this.mFocusedApp);
        }
        if (this.mTopFullscreenOpaqueWindowState != null) {
            pw.print(prefix2);
            pw.print("mTopFullscreenOpaqueWindowState=");
            pw.println(this.mTopFullscreenOpaqueWindowState);
        }
        if (this.mTopFullscreenOpaqueOrDimmingWindowState != null) {
            pw.print(prefix2);
            pw.print("mTopFullscreenOpaqueOrDimmingWindowState=");
            pw.println(this.mTopFullscreenOpaqueOrDimmingWindowState);
        }
        if (this.mForcingShowNavBar) {
            pw.print(prefix2);
            pw.print("mForcingShowNavBar=");
            pw.println(this.mForcingShowNavBar);
            pw.print(prefix2);
            pw.print("mForcingShowNavBarLayer=");
            pw.println(this.mForcingShowNavBarLayer);
        }
        pw.print(prefix2);
        pw.print("mTopIsFullscreen=");
        pw.print(this.mTopIsFullscreen);
        pw.print(prefix2);
        pw.print("mForceStatusBar=");
        pw.print(this.mForceStatusBar);
        pw.print(" mForceStatusBarFromKeyguard=");
        pw.println(this.mForceStatusBarFromKeyguard);
        pw.print(" mForceShowSystemBarsFromExternal=");
        pw.println(this.mForceShowSystemBarsFromExternal);
        pw.print(prefix2);
        pw.print("mAllowLockscreenWhenOn=");
        pw.println(this.mAllowLockscreenWhenOn);
        this.mStatusBarController.dump(pw, prefix2);
        this.mNavigationBarController.dump(pw, prefix2);
        pw.print(prefix2);
        pw.println("Looper state:");
        this.mHandler.getLooper().dump(new PrintWriterPrinter(pw), prefix2 + "  ");
        this.mHwDisplayPolicyEx.dumpPC(prefix2, pw);
    }

    @Override // com.huawei.server.wm.IHwDisplayPolicyInner
    public WindowState getStatusBar() {
        return this.mStatusBar;
    }

    public WindowState getNavigationBar() {
        return this.mNavigationBar;
    }

    @Override // com.huawei.server.wm.IHwDisplayPolicyInner
    public WindowState getFocusedWindow() {
        return this.mFocusedWindow;
    }

    public WindowState getInputMethodWindow() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent == null) {
            return null;
        }
        return displayContent.mInputMethodWindow;
    }

    public WindowState getLastFocusedWindow() {
        return this.mLastFocusedWindow;
    }

    public WindowState getTopFullscreenOpaqueWindowState() {
        return this.mTopFullscreenOpaqueWindowState;
    }

    public int getLastSystemUiFlags() {
        return this.mLastSystemUiFlags;
    }

    @Override // com.huawei.server.wm.IHwDisplayPolicyInner
    public IHwDisplayPolicyEx getHwDisplayPolicyEx() {
        return this.mHwDisplayPolicyEx;
    }

    private WindowState getNavigationBarExternal() {
        return this.mHwDisplayPolicyEx.getNavigationBarExternal();
    }

    public void layoutInputWindowForPCMode(WindowState win, WindowState inputTargetWin, WindowState imeWin, Rect pf, Rect df, Rect cf, Rect vf, int contentBottom) {
        this.mService.mHwWMSEx.layoutWindowForPadPCMode(win, inputTargetWin, imeWin, pf, df, cf, vf, contentBottom);
    }

    public void registerExternalPointerEventListener() {
        this.mHwDisplayPolicyEx.registerExternalPointerEventListener();
    }

    public void unRegisterExternalPointerEventListener() {
        this.mHwDisplayPolicyEx.unRegisterExternalPointerEventListener();
    }

    public void setNaviBarFlag(AppWindowToken focuseApp, WindowState inputMethodWindow) {
        this.mHwDisplayPolicyEx.setInputMethodWindowVisible(inputMethodWindow == null ? false : inputMethodWindow.isVisibleLw());
        if (focuseApp != null) {
            this.mHwDisplayPolicyEx.setNaviBarFlag(focuseApp.navigationBarHide);
        }
    }

    public boolean isTopIsFullscreen() {
        return this.mTopIsFullscreen;
    }

    public void registerRotateObserver(IHwRotateObserver observer) {
        synchronized (this.mHwRotateObservers) {
            this.mHwRotateObservers.register(observer);
        }
    }

    public void unregisterRotateObserver(IHwRotateObserver observer) {
        synchronized (this.mHwRotateObservers) {
            this.mHwRotateObservers.unregister(observer);
        }
    }

    public void requestHwTransientBars(WindowState swipeTarget) {
        requestTransientBars(swipeTarget);
    }

    public void notifyRotate(int oldRotation, int newRotation) {
        RemoteCallbackList<IHwRotateObserver> remoteCallbackList = this.mHwRotateObservers;
        if (remoteCallbackList != null) {
            int i = remoteCallbackList.beginBroadcast();
            while (i > 0) {
                i--;
                IHwRotateObserver observer = this.mHwRotateObservers.getBroadcastItem(i);
                try {
                    observer.onRotate(oldRotation, newRotation);
                    Slog.i(TAG, "notifyRotate() to observer " + observer);
                } catch (Exception e) {
                    Slog.w(TAG, "Exception in notifyRotate(), remove observer " + observer);
                    unregisterRotateObserver(observer);
                }
            }
            this.mHwRotateObservers.finishBroadcast();
        }
    }

    @Override // com.huawei.server.wm.IHwDisplayPolicyInner
    public void setNavigationBarHeightDef(int[] values) {
        this.mNavigationBarHeightForRotationDefault = values;
        this.mNavigationBarFrameHeightForRotationDefault = values;
    }

    public Point getGestureStartedPoint() {
        return this.mSystemGestures.getGestureStartedPoint();
    }

    @Override // com.huawei.server.wm.IHwDisplayPolicyInner
    public void setNavigationBarWidthDef(int[] values) {
        this.mNavigationBarWidthForRotationDefault = values;
    }

    public void updateNavigationBar(boolean minNaviBar) {
        this.mHwDisplayPolicyEx.updateNavigationBar(minNaviBar);
    }

    public boolean isLastImmersiveMode() {
        return isImmersiveMode(this.mLastSystemUiFlags);
    }

    public boolean swipeFromTop() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0) {
            return true;
        }
        if (!mIsHwNaviBar) {
            return false;
        }
        if (!isLastImmersiveMode()) {
            requestTransientStatusBars();
        } else {
            requestTransientBars(this.mStatusBar);
        }
        return true;
    }

    public void requestTransientStatusBars() {
        synchronized (this.mService.getGlobalLock()) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                BarController barController = this.mStatusBarController;
                boolean sb = false;
                if (barController != null) {
                    sb = barController.checkShowTransientBarLw();
                }
                if (sb && barController != null) {
                    barController.showTransient();
                }
                if (this.mImmersiveModeConfirmation != null) {
                    this.mImmersiveModeConfirmation.confirmCurrentPrompt();
                }
                updateSystemUiVisibilityLw();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getRestrictedScreenHeight() {
        return this.mRestrictedScreenHeight;
    }

    public int getDisplayRotation() {
        return this.mDisplayRotation;
    }

    public boolean getForceNotchStatusBar() {
        return this.mForceNotchStatusBar;
    }

    public void setForceNotchStatusBar(boolean forceNotchStatusBar) {
        this.mForceNotchStatusBar = forceNotchStatusBar;
    }

    public void setNotchStatusBarColorLw(int color) {
        this.mNotchStatusBarColorLw = color;
    }

    public int getWindowFlags(WindowState win, WindowManager.LayoutParams attrs) {
        return PolicyControl.getWindowFlags(win, attrs);
    }

    private boolean supportsPointerLocation() {
        return this.mDisplayContent.isDefaultDisplay || !this.mDisplayContent.isPrivate();
    }

    /* access modifiers changed from: package-private */
    public void setPointerLocationEnabled(boolean pointerLocationEnabled) {
        if (supportsPointerLocation()) {
            this.mHandler.sendEmptyMessage(pointerLocationEnabled ? 4 : 5);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enablePointerLocation() {
        if (this.mPointerLocationView == null) {
            this.mPointerLocationView = new PointerLocationView(this.mContext);
            this.mPointerLocationView.setPrintCoords(false);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1);
            lp.type = 2015;
            lp.flags = 1304;
            lp.layoutInDisplayCutoutMode = 1;
            lp.layoutInDisplaySideMode = 1;
            if (ActivityManager.isHighEndGfx()) {
                lp.flags |= 16777216;
                lp.privateFlags |= 2;
            }
            lp.format = -3;
            lp.setTitle("PointerLocation - display " + getDisplayId());
            lp.inputFeatures = lp.inputFeatures | 2;
            ((WindowManager) this.mContext.getSystemService(WindowManager.class)).addView(this.mPointerLocationView, lp);
            this.mDisplayContent.registerPointerEventListener(this.mPointerLocationView);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disablePointerLocation() {
        WindowManagerPolicyConstants.PointerEventListener pointerEventListener = this.mPointerLocationView;
        if (pointerEventListener != null) {
            this.mDisplayContent.unregisterPointerEventListener(pointerEventListener);
            ((WindowManager) this.mContext.getSystemService(WindowManager.class)).removeView(this.mPointerLocationView);
            this.mPointerLocationView = null;
        }
    }

    public boolean isLeftRightSplitStackVisible() {
        if (IS_HW_MULTIWINDOW_SUPPORTED) {
            return this.mService.mAtmService.mHwATMSEx.isSplitStackVisible(this.mDisplayContent.mAcitvityDisplay, 1);
        }
        int i = this.mDisplayRotation;
        if ((i == 1 || i == 3) && this.mDisplayContent.isStackVisible(3)) {
            return true;
        }
        return false;
    }

    public void setFullScreenWinVisibile(boolean visible) {
        this.isHwFullScreenWinVisibility = visible;
    }

    public void setFullScreenWindow(WindowState win) {
        this.mHwFullScreenWindow = win;
    }

    public boolean isAppNeedExpand(String packageName) {
        return this.mHwDisplayPolicyEx.isAppNeedExpand(packageName);
    }

    public boolean checkShowTransientBarLw() {
        StatusBarController statusBarController = this.mStatusBarController;
        if (statusBarController != null) {
            return statusBarController.checkShowTransientBarLw();
        }
        return false;
    }

    public int getRealTimeRotation() {
        return this.mDisplayContent.getRotation();
    }
}
