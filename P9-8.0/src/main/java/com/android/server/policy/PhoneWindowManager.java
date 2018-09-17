package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerInternal.SleepToken;
import android.app.AppOpsManager;
import android.app.IUiModeManager;
import android.app.IUiModeManager.Stub;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.UiModeManager;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiPlaybackClient.OneTouchPlayCallback;
import android.hardware.input.InputManager;
import android.hardware.input.InputManagerInternal;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.hwcontrol.HwWidgetFactory;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.session.MediaSessionLegacyHelper;
import android.net.dhcp.DhcpPacket;
import android.os.Binder;
import android.os.Bundle;
import android.os.FactoryTest;
import android.os.Handler;
import android.os.IAodStateCallback;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.pc.IHwPCManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.service.dreams.DreamManagerInternal;
import android.service.dreams.IDreamManager;
import android.service.vr.IPersistentVrStateCallbacks;
import android.telecom.TelecomManager;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.MutableBoolean;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.IApplicationToken;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.KeyCharacterMap;
import android.view.KeyCharacterMap.FallbackAction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.InputConsumer;
import android.view.WindowManagerPolicy.KeyguardDismissDoneListener;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;
import android.view.WindowManagerPolicy.ScreenOffListener;
import android.view.WindowManagerPolicy.ScreenOnListener;
import android.view.WindowManagerPolicy.StartingSurface;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.WindowManagerPolicy.WindowState;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.autofill.AutofillManagerInternal;
import android.view.inputmethod.InputMethodManagerInternal;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IShortcutService;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ScreenShapeHelper;
import com.android.internal.widget.PointerLocationView;
import com.android.server.GestureLauncherService;
import com.android.server.LocalServices;
import com.android.server.SystemServiceManager;
import com.android.server.audio.AudioService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.LightsManager;
import com.android.server.os.HwBootFail;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.android.server.policy.keyguard.KeyguardServiceDelegate.DrawnListener;
import com.android.server.policy.keyguard.KeyguardStateMonitor.StateCallback;
import com.android.server.power.IHwShutdownThread;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.vr.VrManagerInternal;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowManagerService.H;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Pattern;

public class PhoneWindowManager extends AbsPhoneWindowManager implements WindowManagerPolicy {
    private static final String ACTION_ACTURAL_SHUTDOWN = "com.android.internal.app.SHUTDOWNBROADCAST";
    static final boolean ALTERNATE_CAR_MODE_NAV_SIZE = false;
    private static final int BRIGHTNESS_STEPS = 10;
    private static final long BUGREPORT_TV_GESTURE_TIMEOUT_MILLIS = 1000;
    static final boolean DEBUG = false;
    static final boolean DEBUG_INPUT = false;
    static final boolean DEBUG_KEYGUARD = false;
    static final boolean DEBUG_LAYOUT = false;
    static final boolean DEBUG_SPLASH_SCREEN = false;
    static final boolean DEBUG_WAKEUP;
    static final int DOUBLE_TAP_HOME_NOTHING = 0;
    static final int DOUBLE_TAP_HOME_RECENT_SYSTEM_UI = 1;
    static final boolean ENABLE_DESK_DOCK_HOME_CAPTURE = false;
    static final boolean ENABLE_VR_HEADSET_HOME_CAPTURE = true;
    private static final String FOCUSED_SPLIT_APP_ACTIVITY = "com.huawei.android.launcher/.splitscreen.SplitScreenAppActivity";
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    static final boolean HISI_PERF_OPT = SystemProperties.getBoolean("build.hisi_perf_opt", false);
    private static final String HUAWEI_PRE_CAMERA_START_MODE = "com.huawei.RapidCapture";
    private static final String HUAWEI_SHUTDOWN_PERMISSION = "huawei.android.permission.HWSHUTDOWN";
    protected static final boolean HWFLOW;
    protected static final boolean IS_NOTCH_PROP;
    private static final boolean IS_lOCK_UNNATURAL_ORIENTATION = SystemProperties.getBoolean("ro.config.lock_land_screen", false);
    private static final float KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER = 2.5f;
    static final int LAST_LONG_PRESS_HOME_BEHAVIOR = 2;
    static final int LONG_PRESS_BACK_GO_TO_VOICE_ASSIST = 1;
    static final int LONG_PRESS_BACK_NOTHING = 0;
    static final int LONG_PRESS_HOME_ALL_APPS = 1;
    static final int LONG_PRESS_HOME_ASSIST = 2;
    static final int LONG_PRESS_HOME_NOTHING = 0;
    static final int LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
    static final int LONG_PRESS_POWER_NOTHING = 0;
    static final int LONG_PRESS_POWER_SHUT_OFF = 2;
    static final int LONG_PRESS_POWER_SHUT_OFF_NO_CONFIRM = 3;
    private static final int MSG_ACCESSIBILITY_SHORTCUT = 21;
    private static final int MSG_ACCESSIBILITY_TV = 23;
    private static final int MSG_BACK_DELAYED_PRESS = 20;
    private static final int MSG_BACK_LONG_PRESS = 18;
    private static final int MSG_BUGREPORT_TV = 22;
    private static final int MSG_DISABLE_POINTER_LOCATION = 2;
    private static final int MSG_DISPATCH_BACK_KEY_TO_AUTOFILL = 24;
    private static final int MSG_DISPATCH_MEDIA_KEY_REPEAT_WITH_WAKE_LOCK = 4;
    private static final int MSG_DISPATCH_MEDIA_KEY_WITH_WAKE_LOCK = 3;
    private static final int MSG_DISPATCH_SHOW_GLOBAL_ACTIONS = 10;
    private static final int MSG_DISPATCH_SHOW_RECENTS = 9;
    private static final int MSG_DISPOSE_INPUT_CONSUMER = 19;
    private static final int MSG_ENABLE_POINTER_LOCATION = 1;
    private static final int MSG_HIDE_BOOT_MESSAGE = 11;
    private static final int MSG_KEYGUARD_DRAWN_COMPLETE = 5;
    private static final int MSG_KEYGUARD_DRAWN_TIMEOUT = 6;
    private static final int MSG_LAUNCH_VOICE_ASSIST_WITH_WAKE_LOCK = 12;
    private static final int MSG_POWER_DELAYED_PRESS = 13;
    private static final int MSG_POWER_LONG_PRESS = 14;
    private static final int MSG_REQUEST_TRANSIENT_BARS = 16;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_NAVIGATION = 1;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_STATUS = 0;
    private static final int MSG_SHOW_PICTURE_IN_PICTURE_MENU = 17;
    private static final int MSG_UPDATE_DREAMING_SLEEP_TOKEN = 15;
    private static final int MSG_WINDOW_MANAGER_DRAWN_COMPLETE = 7;
    static final int MULTI_PRESS_POWER_BRIGHTNESS_BOOST = 2;
    static final int MULTI_PRESS_POWER_NOTHING = 0;
    static final int MULTI_PRESS_POWER_THEATER_MODE = 1;
    static final int NAV_BAR_BOTTOM = 0;
    static final int NAV_BAR_LEFT = 2;
    static final int NAV_BAR_OPAQUE_WHEN_FREEFORM_OR_DOCKED = 0;
    static final int NAV_BAR_RIGHT = 1;
    static final int NAV_BAR_TRANSLUCENT_WHEN_FREEFORM_OPAQUE_OTHERWISE = 1;
    private static final int NaviHide = 1;
    private static final int NaviInit = -1;
    private static final int NaviShow = 0;
    private static final int NaviTransientShow = 2;
    private static final long PANIC_GESTURE_EXPIRATION = 30000;
    static final int PANIC_PRESS_BACK_COUNT = 4;
    static final int PANIC_PRESS_BACK_HOME = 1;
    static final int PANIC_PRESS_BACK_NOTHING = 0;
    static final int PENDING_KEY_NULL = -1;
    private static final int POWERDOWN_MAX_TIMEOUT = 200;
    static final boolean PRINT_ANIM = false;
    private static final long SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS = 150;
    private static final int SCREENSHOT_DELAY = 0;
    static final int SHORT_PRESS_POWER_CLOSE_IME_OR_GO_HOME = 5;
    static final int SHORT_PRESS_POWER_GO_HOME = 4;
    static final int SHORT_PRESS_POWER_GO_TO_SLEEP = 1;
    static final int SHORT_PRESS_POWER_NOTHING = 0;
    static final int SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP = 2;
    static final int SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP_AND_GO_HOME = 3;
    static final int SHORT_PRESS_SLEEP_GO_TO_SLEEP = 0;
    static final int SHORT_PRESS_SLEEP_GO_TO_SLEEP_AND_GO_HOME = 1;
    static final int SHORT_PRESS_WINDOW_NOTHING = 0;
    static final int SHORT_PRESS_WINDOW_PICTURE_IN_PICTURE = 1;
    static final boolean SHOW_SPLASH_SCREENS = true;
    public static final int START_AOD_BOOT = 1;
    public static final int START_AOD_SCREEN_OFF = 3;
    public static final int START_AOD_SCREEN_ON = 2;
    public static final int START_AOD_TURNING_ON = 5;
    public static final int START_AOD_USER_SWITCHED = 6;
    public static final int START_AOD_WAKE_UP = 4;
    public static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
    public static final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    public static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    public static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    public static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    static final int SYSTEM_UI_CHANGING_LAYOUT = -1073709042;
    private static final String SYSUI_PACKAGE = "com.android.systemui";
    private static final String SYSUI_SCREENSHOT_ERROR_RECEIVER = "com.android.systemui.screenshot.ScreenshotServiceErrorReceiver";
    private static final String SYSUI_SCREENSHOT_SERVICE = "com.android.systemui.screenshot.TakeScreenshotService";
    static final String TAG = "WindowManager";
    public static final int TOAST_WINDOW_TIMEOUT = 3500;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    static final int WAITING_FOR_DRAWN_TIMEOUT = 1000;
    static final int WAITING_FOR_KEYGUARD_DISMISS_TIMEOUT = 300;
    private static final int[] WINDOW_TYPES_WHERE_HOME_DOESNT_WORK = new int[]{2003, 2010};
    private static boolean bHasFrontFp = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    static final int deviceGlobalActionKeyTimeout = 1000;
    static final boolean localLOGV = false;
    private static final boolean mSupportAod = "1".equals(SystemProperties.get("ro.config.support_aod", null));
    static final Rect mTmpContentFrame = new Rect();
    static final Rect mTmpDecorFrame = new Rect();
    static final Rect mTmpDisplayFrame = new Rect();
    static final Rect mTmpNavigationFrame = new Rect();
    static final Rect mTmpOutsetFrame = new Rect();
    static final Rect mTmpOverscanFrame = new Rect();
    static final Rect mTmpParentFrame = new Rect();
    private static final Rect mTmpRect = new Rect();
    static final Rect mTmpStableFrame = new Rect();
    static final Rect mTmpVisibleFrame = new Rect();
    private static boolean mUsingHwNavibar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    static SparseArray<String> sApplicationLaunchKeyCategories = new SparseArray();
    private static final String sProximityWndName = "Emui:ProximityWnd";
    boolean ifBootMessageShowing = false;
    protected boolean isMMITestDefaultShap = true;
    private boolean mA11yShortcutChordVolumeUpKeyConsumed;
    private long mA11yShortcutChordVolumeUpKeyTime;
    private boolean mA11yShortcutChordVolumeUpKeyTriggered;
    private int mAODState = 1;
    boolean mAccelerometerDefault;
    AccessibilityManager mAccessibilityManager;
    private AccessibilityShortcutController mAccessibilityShortcutController;
    private boolean mAccessibilityTvKey1Pressed;
    private boolean mAccessibilityTvKey2Pressed;
    private boolean mAccessibilityTvScheduled;
    ActivityManagerInternal mActivityManagerInternal;
    int mAllowAllRotations = -1;
    boolean mAllowLockscreenWhenOn;
    private boolean mAllowTheaterModeWakeFromCameraLens;
    private boolean mAllowTheaterModeWakeFromKey;
    private boolean mAllowTheaterModeWakeFromLidSwitch;
    private boolean mAllowTheaterModeWakeFromMotion;
    private boolean mAllowTheaterModeWakeFromMotionWhenNotDreaming;
    private boolean mAllowTheaterModeWakeFromPowerKey;
    private boolean mAllowTheaterModeWakeFromWakeGesture;
    private int mAodSwitch = 1;
    private AodSwitchObserver mAodSwitchObserver;
    private int mAodTimerSwitch = 1;
    AppOpsManager mAppOpsManager;
    boolean mAssistKeyLongPressed;
    AutofillManagerInternal mAutofillManagerInternal;
    boolean mAwake;
    volatile boolean mBackKeyHandled;
    volatile int mBackKeyPressCounter;
    volatile boolean mBeganFromNonInteractive;
    boolean mBootMessageNeedsHiding;
    ProgressDialog mBootMsgDialog = null;
    WakeLock mBroadcastWakeLock;
    private boolean mBugreportTvKey1Pressed;
    private boolean mBugreportTvKey2Pressed;
    private boolean mBugreportTvScheduled;
    BurnInProtectionHelper mBurnInProtectionHelper;
    long[] mCalendarDateVibePattern;
    volatile boolean mCameraGestureTriggeredDuringGoingToSleep;
    int mCameraLensCoverState = -1;
    boolean mCarDockEnablesAccelerometer;
    Intent mCarDockIntent;
    int mCarDockRotation;
    private final Runnable mClearHideNavigationFlag = new Runnable() {
        public void run() {
            synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                PhoneWindowManager phoneWindowManager = PhoneWindowManager.this;
                phoneWindowManager.mForceClearedSystemUiFlags &= -3;
            }
            PhoneWindowManager.this.mWindowManagerFuncs.reevaluateStatusBarVisibility();
        }
    };
    long[] mClockTickVibePattern;
    boolean mConsumeSearchKeyUp;
    int mContentBottom;
    int mContentLeft;
    int mContentRight;
    int mContentTop;
    Context mContext;
    long[] mContextClickVibePattern;
    int mCurBottom;
    int mCurLeft;
    int mCurRight;
    int mCurTop;
    int mCurrentAppOrientation = -1;
    protected int mCurrentUserId;
    private HwCustPhoneWindowManager mCust = ((HwCustPhoneWindowManager) HwCustUtils.createObj(HwCustPhoneWindowManager.class, new Object[0]));
    int mDemoHdmiRotation;
    boolean mDemoHdmiRotationLock;
    int mDemoRotation;
    boolean mDemoRotationLock;
    boolean mDeskDockEnablesAccelerometer;
    Intent mDeskDockIntent;
    int mDeskDockRotation;
    private volatile boolean mDismissImeOnBackKeyPressed;
    Display mDisplay;
    int mDisplayRotation;
    int mDockBottom;
    int mDockLayer;
    int mDockLeft;
    int mDockMode = 0;
    BroadcastReceiver mDockReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.DOCK_EVENT".equals(intent.getAction())) {
                PhoneWindowManager.this.mDockMode = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
            } else {
                try {
                    IUiModeManager uiModeService = Stub.asInterface(ServiceManager.getService("uimode"));
                    PhoneWindowManager.this.mUiMode = uiModeService.getCurrentModeType();
                } catch (RemoteException e) {
                }
            }
            PhoneWindowManager.this.updateRotation(true);
            synchronized (PhoneWindowManager.this.mLock) {
                PhoneWindowManager.this.updateOrientationListenerLp();
            }
        }
    };
    int mDockRight;
    int mDockTop;
    final Rect mDockedStackBounds = new Rect();
    int mDoublePressOnPowerBehavior;
    private int mDoubleTapOnHomeBehavior;
    DreamManagerInternal mDreamManagerInternal;
    BroadcastReceiver mDreamReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.DREAMING_STARTED".equals(intent.getAction())) {
                if (PhoneWindowManager.this.mKeyguardDelegate != null) {
                    PhoneWindowManager.this.mKeyguardDelegate.onDreamingStarted();
                }
            } else if ("android.intent.action.DREAMING_STOPPED".equals(intent.getAction()) && PhoneWindowManager.this.mKeyguardDelegate != null) {
                PhoneWindowManager.this.mKeyguardDelegate.onDreamingStopped();
            }
        }
    };
    boolean mDreamingLockscreen;
    SleepToken mDreamingSleepToken;
    boolean mDreamingSleepTokenNeeded;
    private boolean mEnableCarDockHomeCapture = true;
    boolean mEnableShiftMenuBugReports = false;
    volatile boolean mEndCallKeyHandled;
    private final Runnable mEndCallLongPress = new Runnable() {
        public void run() {
            PhoneWindowManager.this.mEndCallKeyHandled = true;
            PhoneWindowManager.this.performHapticFeedbackLw(null, 0, false);
            PhoneWindowManager.this.showGlobalActionsInternal();
        }
    };
    int mEndcallBehavior;
    private final SparseArray<FallbackAction> mFallbackActions = new SparseArray();
    private boolean mFirstIncall;
    IApplicationToken mFocusedApp;
    WindowState mFocusedWindow;
    int mForceClearedSystemUiFlags = 0;
    private boolean mForceDefaultOrientation = false;
    protected boolean mForceNotchStatusBar = false;
    boolean mForceShowSystemBars;
    boolean mForceStatusBar;
    boolean mForceStatusBarFromKeyguard;
    private boolean mForceStatusBarTransparent;
    WindowState mForceStatusBarTransparentWin;
    boolean mForcingShowNavBar;
    int mForcingShowNavBarLayer;
    GlobalActions mGlobalActions;
    private GlobalKeyManager mGlobalKeyManager;
    private boolean mGoToSleepOnButtonPressTheaterMode;
    volatile boolean mGoingToSleep;
    private UEventObserver mHDMIObserver = new UEventObserver() {
        public void onUEvent(UEvent event) {
            PhoneWindowManager.this.setHdmiPlugged("1".equals(event.get("SWITCH_STATE")));
        }
    };
    private boolean mHandleVolumeKeysInWM;
    Handler mHandler;
    protected boolean mHasCoverView = false;
    private boolean mHasFeatureLeanback;
    private boolean mHasFeatureWatch;
    boolean mHasNavigationBar = false;
    boolean mHasSoftInput = false;
    boolean mHaveBuiltInKeyboard;
    boolean mHavePendingMediaKeyRepeatWithWakeLock;
    HdmiControl mHdmiControl;
    boolean mHdmiPlugged;
    private final Runnable mHiddenNavPanic = new Runnable() {
        /* JADX WARNING: Missing block: B:12:0x0030, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                if (PhoneWindowManager.this.isUserSetupComplete()) {
                    PhoneWindowManager.this.mPendingPanicGestureUptime = SystemClock.uptimeMillis();
                    if (!PhoneWindowManager.isNavBarEmpty(PhoneWindowManager.this.mLastSystemUiFlags)) {
                        PhoneWindowManager.this.mNavigationBarController.showTransient();
                    }
                }
            }
        }
    };
    boolean mHomeConsumed;
    boolean mHomeDoubleTapPending;
    private final Runnable mHomeDoubleTapTimeoutRunnable = new Runnable() {
        public void run() {
            if (PhoneWindowManager.this.mHomeDoubleTapPending) {
                PhoneWindowManager.this.mHomeDoubleTapPending = false;
                PhoneWindowManager.this.handleShortPressOnHome();
            }
        }
    };
    Intent mHomeIntent;
    boolean mHomePressed;
    IAodStateCallback mIAodStateCallback;
    private ImmersiveModeConfirmation mImmersiveModeConfirmation;
    boolean mImmersiveStatusChanged;
    int mIncallBackBehavior;
    int mIncallPowerBehavior;
    int mInitialMetaState;
    InputConsumer mInputConsumer = null;
    InputManagerInternal mInputManagerInternal;
    InputMethodManagerInternal mInputMethodManagerInternal;
    protected boolean mInterceptInputForWaitBrightness = false;
    long[] mKeyboardTapVibePattern;
    private boolean mKeyguardBound;
    KeyguardServiceDelegate mKeyguardDelegate;
    final Runnable mKeyguardDismissDoneCallback = new Runnable() {
        public void run() {
            Slog.i(PhoneWindowManager.TAG, "keyguard dismiss done!");
            PhoneWindowManager.this.finishKeyguardDismissDone();
        }
    };
    KeyguardDismissDoneListener mKeyguardDismissListener;
    boolean mKeyguardDrawComplete;
    final DrawnListener mKeyguardDrawnCallback = new DrawnListener() {
        public void onDrawn() {
            Slog.d(PhoneWindowManager.TAG, "mKeyguardDelegate.ShowListener.onDrawn.");
            if (!PhoneWindowManager.this.mFirstIncall && PhoneWindowManager.this.isSupportCover() && PhoneWindowManager.this.isSmartCoverMode() && (HwFrameworkFactory.getCoverManager().isCoverOpen() ^ 1) != 0 && PhoneWindowManager.this.isInCallActivity()) {
                PhoneWindowManager.this.mFirstIncall = true;
                PhoneWindowManager.this.mHandler.sendEmptyMessageDelayed(5, 300);
                return;
            }
            PhoneWindowManager.this.mHandler.sendEmptyMessage(5);
        }
    };
    private boolean mKeyguardDrawnOnce;
    volatile boolean mKeyguardOccluded;
    private boolean mKeyguardOccludedChanged;
    int mLandscapeRotation = 0;
    boolean mLanguageSwitchKeyPressed;
    final Rect mLastDockedStackBounds = new Rect();
    int mLastDockedStackSysUiFlags;
    public boolean mLastFocusNeedsMenu = false;
    int mLastFullscreenStackSysUiFlags;
    int mLastHideNaviDockBottom = 0;
    WindowState mLastInputMethodTargetWindow = null;
    WindowState mLastInputMethodWindow = null;
    int mLastNaviStatus = -1;
    final Rect mLastNonDockedStackBounds = new Rect();
    int mLastShowNaviDockBottom = 0;
    private boolean mLastShowingDream;
    private WindowState mLastStartingWindow;
    int mLastSystemUiFlags;
    int mLastTransientNaviDockBottom = 0;
    boolean mLidControlsScreenLock;
    boolean mLidControlsSleep;
    int mLidKeyboardAccessibility;
    int mLidNavigationAccessibility;
    int mLidOpenRotation;
    int mLidState = -1;
    protected final Object mLock = new Object();
    int mLockScreenTimeout;
    boolean mLockScreenTimerActive;
    private final LogDecelerateInterpolator mLogDecelerateInterpolator = new LogDecelerateInterpolator(100, 0);
    int mLongPressOnBackBehavior;
    private int mLongPressOnHomeBehavior;
    int mLongPressOnPowerBehavior;
    long[] mLongPressVibePattern;
    int mMetaState;
    BroadcastReceiver mMultiuserReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                PhoneWindowManager.this.mSettingsObserver.onChange(false);
                if (PhoneWindowManager.mSupportAod) {
                    Slog.i(PhoneWindowManager.TAG, "AOD mAodSwitchObserver.onChange");
                    PhoneWindowManager.this.mAodSwitchObserver.onChange(false);
                    if (!(PhoneWindowManager.this.mScreenOnEarly || (PhoneWindowManager.this.mScreenOnFully ^ 1) == 0)) {
                        PhoneWindowManager.this.startAodService(6);
                    }
                }
                synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                    PhoneWindowManager.this.mLastSystemUiFlags = 0;
                    PhoneWindowManager.this.updateSystemUiVisibilityLw();
                }
            }
        }
    };
    int mNavBarOpacityMode = 0;
    private final OnBarVisibilityChangedListener mNavBarVisibilityListener = new OnBarVisibilityChangedListener() {
        public void onBarVisibilityChanged(boolean visible) {
            if (PhoneWindowManager.bHasFrontFp && PhoneWindowManager.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                visible = false;
            }
            Slog.i(PhoneWindowManager.TAG, "notifyAccessibilityButtonVisibilityChanged visible:" + visible);
            PhoneWindowManager.this.mAccessibilityManager.notifyAccessibilityButtonVisibilityChanged(visible);
        }
    };
    WindowState mNavigationBar = null;
    boolean mNavigationBarCanMove = false;
    private final BarController mNavigationBarController = new BarController("NavigationBar", 134217728, 536870912, Integer.MIN_VALUE, 2, 134217728, 32768);
    int[] mNavigationBarHeightForRotationDefault = new int[4];
    int[] mNavigationBarHeightForRotationInCarMode = new int[4];
    int mNavigationBarPosition = 0;
    int[] mNavigationBarWidthForRotationDefault = new int[4];
    int[] mNavigationBarWidthForRotationInCarMode = new int[4];
    final Rect mNonDockedStackBounds = new Rect();
    protected int mNotchPropSize = 0;
    MyOrientationListener mOrientationListener;
    boolean mOrientationSensorEnabled = false;
    int mOverscanBottom = 0;
    int mOverscanLeft = 0;
    int mOverscanRight = 0;
    int mOverscanScreenHeight;
    int mOverscanScreenLeft;
    int mOverscanScreenTop;
    int mOverscanScreenWidth;
    int mOverscanTop = 0;
    private final HashMap<String, Boolean> mPackages = new HashMap();
    int mPanicPressOnBackBehavior;
    boolean mPendingCapsLockToggle;
    private boolean mPendingKeyguardOccluded;
    boolean mPendingMetaAction;
    private long mPendingPanicGestureUptime;
    volatile int mPendingWakeKey = -1;
    private volatile boolean mPersistentVrModeEnabled;
    final IPersistentVrStateCallbacks mPersistentVrModeListener = new IPersistentVrStateCallbacks.Stub() {
        public void onPersistentVrStateChanged(boolean enabled) {
            PhoneWindowManager.this.mPersistentVrModeEnabled = enabled;
        }
    };
    volatile boolean mPictureInPictureVisible;
    int mPointerLocationMode = 0;
    PointerLocationView mPointerLocationView;
    int mPortraitRotation = 0;
    volatile boolean mPowerKeyHandled;
    volatile int mPowerKeyPressCounter;
    WakeLock mPowerKeyWakeLock;
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    boolean mPreloadedRecentApps;
    int mRecentAppsHeldModifiers;
    volatile boolean mRecentsVisible;
    int mResettingSystemUiFlags = 0;
    int mRestrictedOverscanScreenHeight;
    int mRestrictedOverscanScreenLeft;
    int mRestrictedOverscanScreenTop;
    int mRestrictedOverscanScreenWidth;
    int mRestrictedScreenHeight;
    int mRestrictedScreenLeft;
    int mRestrictedScreenTop;
    int mRestrictedScreenWidth;
    boolean mSafeMode;
    long[] mSafeModeDisabledVibePattern;
    long[] mSafeModeEnabledVibePattern;
    ScreenLockTimeout mScreenLockTimeout = new ScreenLockTimeout();
    protected int mScreenOffReason = -1;
    SleepToken mScreenOffSleepToken;
    boolean mScreenOnEarly;
    boolean mScreenOnFully;
    ScreenOnListener mScreenOnListener;
    protected int mScreenRotation = 0;
    private boolean mScreenshotChordEnabled;
    private long mScreenshotChordPowerKeyTime;
    private boolean mScreenshotChordPowerKeyTriggered;
    private boolean mScreenshotChordVolumeDownKeyConsumed;
    private long mScreenshotChordVolumeDownKeyTime;
    private boolean mScreenshotChordVolumeDownKeyTriggered;
    ServiceConnection mScreenshotConnection = null;
    final Object mScreenshotLock = new Object();
    private final ScreenshotRunnable mScreenshotRunnable = new ScreenshotRunnable(this, null);
    final Runnable mScreenshotTimeout = new Runnable() {
        public void run() {
            synchronized (PhoneWindowManager.this.mScreenshotLock) {
                if (PhoneWindowManager.this.mScreenshotConnection != null) {
                    if (PhoneWindowManager.HWFLOW) {
                        Slog.i(PhoneWindowManager.TAG, "takeScreenshot  screenshot timeout");
                    }
                    PhoneWindowManager.this.mContext.unbindService(PhoneWindowManager.this.mScreenshotConnection);
                    PhoneWindowManager.this.mScreenshotConnection = null;
                    PhoneWindowManager.this.notifyScreenshotError();
                }
            }
        }
    };
    boolean mSearchKeyShortcutPending;
    SearchManager mSearchManager;
    int mSeascapeRotation = 0;
    final Object mServiceAquireLock = new Object();
    SettingsObserver mSettingsObserver;
    int mShortPressOnPowerBehavior;
    int mShortPressOnSleepBehavior;
    int mShortPressWindowBehavior;
    private LongSparseArray<IShortcutService> mShortcutKeyServices = new LongSparseArray();
    ShortcutManager mShortcutManager;
    boolean mShowingDream;
    BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (PhoneWindowManager.ACTION_ACTURAL_SHUTDOWN.equals(intent.getAction())) {
                PhoneWindowManager.this.mOrientationListener.disable();
                PhoneWindowManager.this.mOrientationSensorEnabled = false;
            }
        }
    };
    int mStableBottom;
    int mStableFullscreenBottom;
    int mStableFullscreenLeft;
    int mStableFullscreenRight;
    int mStableFullscreenTop;
    int mStableLeft;
    int mStableRight;
    int mStableTop;
    WindowState mStatusBar = null;
    private final StatusBarController mStatusBarController = new StatusBarController();
    int mStatusBarHeight;
    int mStatusBarLayer;
    StatusBarManagerInternal mStatusBarManagerInternal;
    IStatusBarService mStatusBarService;
    boolean mSupportAutoRotation;
    private boolean mSupportLongPressPowerWhenNonInteractive;
    boolean mSystemBooted;
    int mSystemBottom;
    private SystemGesturesPointerEventListener mSystemGestures;
    int mSystemLeft;
    boolean mSystemReady;
    int mSystemRight;
    int mSystemTop;
    private final MutableBoolean mTmpBoolean = new MutableBoolean(false);
    WindowState mTopDockedOpaqueOrDimmingWindowState;
    WindowState mTopDockedOpaqueWindowState;
    WindowState mTopFullscreenOpaqueOrDimmingWindowState;
    WindowState mTopFullscreenOpaqueWindowState;
    boolean mTopIsFullscreen;
    boolean mTranslucentDecorEnabled = true;
    int mTriplePressOnPowerBehavior;
    int mUiMode;
    IUiModeManager mUiModeManager;
    int mUndockedHdmiRotation;
    int mUnrestrictedScreenHeight;
    int mUnrestrictedScreenLeft;
    int mUnrestrictedScreenTop;
    int mUnrestrictedScreenWidth;
    int mUpsideDownRotation = 0;
    boolean mUseTvRouting;
    int mUserRotation = 0;
    int mUserRotationMode = 0;
    Vibrator mVibrator;
    long[] mVirtualKeyVibePattern;
    int mVoiceContentBottom;
    int mVoiceContentLeft;
    int mVoiceContentRight;
    int mVoiceContentTop;
    Intent mVrHeadsetHomeIntent;
    volatile VrManagerInternal mVrManagerInternal;
    boolean mWakeGestureEnabledSetting;
    MyWakeGestureListener mWakeGestureListener;
    IWindowManager mWindowManager;
    final Runnable mWindowManagerDrawCallback = new Runnable() {
        public void run() {
            Slog.i(PhoneWindowManager.TAG, "All windows ready for display!");
            PhoneWindowManager.this.mHandler.sendEmptyMessage(7);
        }
    };
    boolean mWindowManagerDrawComplete;
    WindowManagerFuncs mWindowManagerFuncs;
    WindowManagerInternal mWindowManagerInternal;
    protected int notchStatusBarColorLw = 0;
    boolean notchWindowChange = false;
    boolean notchWindowChangeState = false;

    class AodSwitchObserver extends ContentObserver {
        AodSwitchObserver(Handler handler) {
            super(handler);
            PhoneWindowManager.this.mAodSwitch = getAodSwitch();
            PhoneWindowManager.this.mAodTimerSwitch = getAodTimerSwitch();
        }

        void observe() {
            Slog.i(PhoneWindowManager.TAG, "AOD AodSwitchObserver observe");
            if (PhoneWindowManager.mSupportAod) {
                ContentResolver resolver = PhoneWindowManager.this.mContext.getContentResolver();
                resolver.registerContentObserver(Secure.getUriFor("aod_switch"), false, this, -1);
                resolver.registerContentObserver(Secure.getUriFor("aod_scheduled_switch"), false, this, -1);
            }
        }

        public void onChange(boolean selfChange) {
            PhoneWindowManager.this.mAodSwitch = Secure.getIntForUser(PhoneWindowManager.this.mContext.getContentResolver(), "aod_switch", 1, ActivityManager.getCurrentUser());
            PhoneWindowManager.this.mAodTimerSwitch = Secure.getIntForUser(PhoneWindowManager.this.mContext.getContentResolver(), "aod_scheduled_switch", 1, ActivityManager.getCurrentUser());
        }

        private int getAodSwitch() {
            Slog.i(PhoneWindowManager.TAG, "AOD getAodSwitch ");
            if (!PhoneWindowManager.mSupportAod) {
                return 0;
            }
            PhoneWindowManager.this.mAodSwitch = Secure.getIntForUser(PhoneWindowManager.this.mContext.getContentResolver(), "aod_switch", 1, ActivityManager.getCurrentUser());
            return PhoneWindowManager.this.mAodSwitch;
        }

        private int getAodTimerSwitch() {
            Slog.i(PhoneWindowManager.TAG, "AOD getAodTimerSwitch ");
            return Secure.getIntForUser(PhoneWindowManager.this.mContext.getContentResolver(), "aod_scheduled_switch", 1, ActivityManager.getCurrentUser());
        }
    }

    private final class AppDeathRecipient implements DeathRecipient {
        AppDeathRecipient() {
        }

        public void binderDied() {
            Slog.i(PhoneWindowManager.TAG, "Death received in " + this + " for thread " + PhoneWindowManager.this.mIAodStateCallback.asBinder());
            if (PhoneWindowManager.mSupportAod) {
                PhoneWindowManager.this.unregeditAodStateCallback(PhoneWindowManager.this.mIAodStateCallback);
                PhoneWindowManager.this.mIAodStateCallback = null;
                PhoneWindowManager.this.mPowerManager.setDozeOverrideFromAod(1, 0, null);
                PhoneWindowManager.this.mPowerManager.setAodState(-1, 0);
                PhoneWindowManager.this.mPowerManager.setAodState(-1, 2);
            }
        }
    }

    private static class HdmiControl {
        private final HdmiPlaybackClient mClient;

        /* synthetic */ HdmiControl(HdmiPlaybackClient client, HdmiControl -this1) {
            this(client);
        }

        private HdmiControl(HdmiPlaybackClient client) {
            this.mClient = client;
        }

        public void turnOnTv() {
            if (this.mClient != null) {
                this.mClient.oneTouchPlay(new OneTouchPlayCallback() {
                    public void onComplete(int result) {
                        if (result != 0) {
                            Log.w(PhoneWindowManager.TAG, "One touch play failed: " + result);
                        }
                    }
                });
            }
        }
    }

    final class HideNavInputEventReceiver extends InputEventReceiver {
        public HideNavInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        /* JADX WARNING: Missing block: B:25:0x0062, code:
            if (r2 == false) goto L_0x006b;
     */
        /* JADX WARNING: Missing block: B:26:0x0064, code:
            r12.this$0.mWindowManagerFuncs.reevaluateStatusBarVisibility();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onInputEvent(InputEvent event) {
            try {
                if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0 && ((MotionEvent) event).getAction() == 0) {
                    boolean changed = false;
                    synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                        if (PhoneWindowManager.this.mInputConsumer != null) {
                            int newVal = ((PhoneWindowManager.this.mResettingSystemUiFlags | 2) | 1) | 4;
                            if (PhoneWindowManager.this.mResettingSystemUiFlags != newVal) {
                                PhoneWindowManager.this.mResettingSystemUiFlags = newVal;
                                changed = true;
                            }
                            newVal = PhoneWindowManager.this.mForceClearedSystemUiFlags | 2;
                            if (PhoneWindowManager.this.mForceClearedSystemUiFlags != newVal) {
                                PhoneWindowManager.this.mForceClearedSystemUiFlags = newVal;
                                changed = true;
                                PhoneWindowManager.this.mHandler.postDelayed(PhoneWindowManager.this.mClearHideNavigationFlag, 1000);
                            }
                        }
                    }
                }
                finishInputEvent(event, false);
            } finally {
                finishInputEvent(event, false);
            }
        }
    }

    class MyOrientationListener extends WindowOrientationListener {
        MyOrientationListener(Context context, Handler handler) {
            super(context, handler);
        }

        public void onProposedRotationChanged(int rotation) {
            PhoneWindowManager.this.mScreenRotation = rotation;
            Slog.i(PhoneWindowManager.TAG, "onProposedRotationChanged, rotation=" + rotation);
            Jlog.d(57, "");
            PhoneWindowManager.this.setSensorRotationFR(rotation);
            if (PhoneWindowManager.this.isIntelliServiceEnabledFR(PhoneWindowManager.this.mCurrentAppOrientation)) {
                PhoneWindowManager.this.startIntelliServiceFR();
            } else {
                PhoneWindowManager.this.mHandler.post(new UpdateRotationRunnable(rotation));
            }
        }
    }

    class MyWakeGestureListener extends WakeGestureListener {
        MyWakeGestureListener(Context context, Handler handler) {
            super(context, handler);
        }

        public void onWakeUp() {
            synchronized (PhoneWindowManager.this.mLock) {
                if (PhoneWindowManager.this.shouldEnableWakeGestureLp()) {
                    PhoneWindowManager.this.performHapticFeedbackLw(null, 1, false);
                    PhoneWindowManager.this.wakeUp(SystemClock.uptimeMillis(), PhoneWindowManager.this.mAllowTheaterModeWakeFromWakeGesture, "android.policy:GESTURE");
                }
            }
        }
    }

    private class PolicyHandler extends Handler {
        /* synthetic */ PolicyHandler(PhoneWindowManager this$0, PolicyHandler -this1) {
            this();
        }

        private PolicyHandler() {
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            PhoneWindowManager phoneWindowManager;
            switch (msg.what) {
                case 1:
                    PhoneWindowManager.this.enablePointerLocation();
                    return;
                case 2:
                    PhoneWindowManager.this.disablePointerLocation();
                    return;
                case 3:
                    PhoneWindowManager.this.dispatchMediaKeyWithWakeLock((KeyEvent) msg.obj);
                    return;
                case 4:
                    PhoneWindowManager.this.dispatchMediaKeyRepeatWithWakeLock((KeyEvent) msg.obj);
                    return;
                case 5:
                    if (PhoneWindowManager.DEBUG_WAKEUP) {
                        Slog.w(PhoneWindowManager.TAG, "Setting mKeyguardDrawComplete");
                    }
                    PhoneWindowManager.this.finishKeyguardDrawn();
                    return;
                case 6:
                    Flog.i(NativeResponseCode.SERVICE_FOUND, "WindowManager Keyguard drawn timeout. Setting mKeyguardDrawComplete");
                    PhoneWindowManager.this.finishKeyguardDrawn();
                    return;
                case 7:
                    if (PhoneWindowManager.DEBUG_WAKEUP) {
                        Slog.w(PhoneWindowManager.TAG, "Setting mWindowManagerDrawComplete");
                    }
                    PhoneWindowManager.this.finishWindowsDrawn();
                    return;
                case 9:
                    PhoneWindowManager.this.showRecentApps(false, msg.arg1 != 0);
                    return;
                case 10:
                    PhoneWindowManager.this.showGlobalActionsInternal();
                    return;
                case 11:
                    PhoneWindowManager.this.handleHideBootMessage();
                    return;
                case 12:
                    phoneWindowManager = PhoneWindowManager.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    phoneWindowManager.launchVoiceAssistWithWakeLock(z);
                    return;
                case 13:
                    PhoneWindowManager phoneWindowManager2 = PhoneWindowManager.this;
                    long longValue = ((Long) msg.obj).longValue();
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    phoneWindowManager2.powerPress(longValue, z, msg.arg2);
                    PhoneWindowManager.this.finishPowerKeyPress();
                    return;
                case 14:
                    PhoneWindowManager.this.powerLongPress();
                    return;
                case 15:
                    phoneWindowManager = PhoneWindowManager.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    phoneWindowManager.updateDreamingSleepToken(z);
                    return;
                case 16:
                    WindowState targetBar = msg.arg1 == 0 ? PhoneWindowManager.this.mStatusBar : PhoneWindowManager.this.mNavigationBar;
                    if (targetBar != null) {
                        PhoneWindowManager.this.requestTransientBars(targetBar);
                        return;
                    }
                    return;
                case 17:
                    PhoneWindowManager.this.showPictureInPictureMenuInternal();
                    return;
                case 18:
                    PhoneWindowManager.this.backLongPress();
                    PhoneWindowManager.this.finishBackKeyPress();
                    return;
                case 19:
                    PhoneWindowManager.this.disposeInputConsumer((InputConsumer) msg.obj);
                    return;
                case 20:
                    PhoneWindowManager.this.backMultiPressAction(((Long) msg.obj).longValue(), msg.arg1);
                    PhoneWindowManager.this.finishBackKeyPress();
                    return;
                case 21:
                    PhoneWindowManager.this.accessibilityShortcutActivated();
                    return;
                case 22:
                    PhoneWindowManager.this.takeBugreport();
                    return;
                case 23:
                    if (PhoneWindowManager.this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(false)) {
                        PhoneWindowManager.this.accessibilityShortcutActivated();
                        return;
                    }
                    return;
                case 24:
                    PhoneWindowManager.this.mAutofillManagerInternal.onBackKeyPressed();
                    return;
                default:
                    return;
            }
        }
    }

    class ScreenLockTimeout implements Runnable {
        Bundle options;

        ScreenLockTimeout() {
        }

        public void run() {
            synchronized (this) {
                if (PhoneWindowManager.this.mKeyguardDelegate != null) {
                    PhoneWindowManager.this.mKeyguardDelegate.doKeyguardTimeout(this.options);
                }
                PhoneWindowManager.this.mLockScreenTimerActive = false;
                this.options = null;
            }
        }

        public void setLockOptions(Bundle options) {
            this.options = options;
        }
    }

    private class ScreenshotRunnable implements Runnable {
        private int mScreenshotType;

        /* synthetic */ ScreenshotRunnable(PhoneWindowManager this$0, ScreenshotRunnable -this1) {
            this();
        }

        private ScreenshotRunnable() {
            this.mScreenshotType = 1;
        }

        public void setScreenshotType(int screenshotType) {
            this.mScreenshotType = screenshotType;
        }

        public void run() {
            if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                IHwPCManager pcManager = HwPCUtils.getHwPCManager();
                if (pcManager != null) {
                    try {
                        pcManager.screenshotPc();
                    } catch (RemoteException e) {
                        HwPCUtils.log(PhoneWindowManager.TAG, "RemoteException screenshotPc");
                    }
                    return;
                }
            }
            if (PhoneWindowManager.HWFLOW) {
                Slog.i(PhoneWindowManager.TAG, "hardware_keys ScreenShot enter.");
            }
            sendScreenshotNotification();
            PhoneWindowManager.this.takeScreenshot(this.mScreenshotType);
        }

        private void sendScreenshotNotification() {
            Intent screenshotIntent = new Intent("com.huawei.recsys.action.RECEIVE_EVENT");
            screenshotIntent.putExtra("eventOperator", "sysScreenShot");
            screenshotIntent.putExtra("eventItem", "hardware_keys");
            screenshotIntent.setPackage("com.huawei.recsys");
            PhoneWindowManager.this.mContext.sendBroadcastAsUser(screenshotIntent, UserHandle.CURRENT, "com.huawei.tips.permission.SHOW_TIPS");
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = PhoneWindowManager.this.mContext.getContentResolver();
            resolver.registerContentObserver(System.getUriFor("end_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("incall_power_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("incall_back_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("wake_gesture_enabled"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("accelerometer_rotation"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("user_rotation"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("screen_off_timeout"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor("pointer_location"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("default_input_method"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("immersive_mode_confirmations"), false, this, -1);
            resolver.registerContentObserver(Global.getUriFor("policy_control"), false, this, -1);
            resolver.registerContentObserver(System.getUriFor(PhoneWindowManager.this.fingersense_enable), false, this, -1);
            resolver.registerContentObserver(System.getUriFor(PhoneWindowManager.this.fingersense_letters_enable), false, this, -1);
            resolver.registerContentObserver(System.getUriFor(PhoneWindowManager.this.line_gesture_enable), false, this, -1);
            resolver.registerContentObserver(System.getUriFor(PhoneWindowManager.this.navibar_enable), false, this, -1);
            PhoneWindowManager.this.updateSettings();
        }

        public void onChange(boolean selfChange) {
            PhoneWindowManager.this.updateSettings();
            PhoneWindowManager.this.updateRotation(false);
        }
    }

    protected final class UpdateRotationRunnable implements Runnable {
        private final int mRotation;

        public UpdateRotationRunnable(int rotation) {
            this.mRotation = rotation;
        }

        public void run() {
            PhoneWindowManager.this.mPowerManagerInternal.powerHint(2, 0);
            if (PhoneWindowManager.this.mWindowManagerInternal.isDockedDividerResizing()) {
                PhoneWindowManager.this.mWindowManagerInternal.setDockedStackDividerRotation(this.mRotation);
            } else {
                Slog.i(PhoneWindowManager.TAG, "MyOrientationListener: updateRotation.");
                PhoneWindowManager.this.updateRotation(false);
            }
            if (PhoneWindowManager.bHasFrontFp) {
                PhoneWindowManager.this.updateSplitScreenView();
            }
        }
    }

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG_WAKEUP = isLoggable;
        isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
        if (SystemProperties.get("ro.config.hw_notch_size", "").equals("")) {
            z = false;
        }
        IS_NOTCH_PROP = z;
        sApplicationLaunchKeyCategories.append(64, "android.intent.category.APP_BROWSER");
        sApplicationLaunchKeyCategories.append(65, "android.intent.category.APP_EMAIL");
        sApplicationLaunchKeyCategories.append(207, "android.intent.category.APP_CONTACTS");
        sApplicationLaunchKeyCategories.append(208, "android.intent.category.APP_CALENDAR");
        sApplicationLaunchKeyCategories.append(209, "android.intent.category.APP_MUSIC");
        sApplicationLaunchKeyCategories.append(210, "android.intent.category.APP_CALCULATOR");
    }

    public boolean isLandscape() {
        return this.mScreenRotation == 1 || this.mScreenRotation == 3;
    }

    IStatusBarService getStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            }
            iStatusBarService = this.mStatusBarService;
        }
        return iStatusBarService;
    }

    StatusBarManagerInternal getStatusBarManagerInternal() {
        StatusBarManagerInternal statusBarManagerInternal;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarManagerInternal == null) {
                this.mStatusBarManagerInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
            }
            statusBarManagerInternal = this.mStatusBarManagerInternal;
        }
        return statusBarManagerInternal;
    }

    boolean needSensorRunningLp() {
        if (this.mSupportAutoRotation && (this.mCurrentAppOrientation == 4 || this.mCurrentAppOrientation == 10 || this.mCurrentAppOrientation == 7 || this.mCurrentAppOrientation == 6)) {
            return true;
        }
        if ((this.mCarDockEnablesAccelerometer && this.mDockMode == 2) || (this.mDeskDockEnablesAccelerometer && (this.mDockMode == 1 || this.mDockMode == 3 || this.mDockMode == 4))) {
            return true;
        }
        if (IS_lOCK_UNNATURAL_ORIENTATION || this.mUserRotationMode != 1) {
            return this.mSupportAutoRotation;
        }
        return false;
    }

    void updateOrientationListenerLp() {
        if (this.mOrientationListener.canDetectOrientation()) {
            boolean keyguardGoingAway = this.mWindowManagerInternal.isKeyguardGoingAway();
            boolean disable = true;
            if (this.mScreenOnEarly && this.mAwake && (((this.mKeyguardDrawComplete && this.mWindowManagerDrawComplete) || keyguardGoingAway) && needSensorRunningLp())) {
                disable = false;
                if (!this.mOrientationSensorEnabled) {
                    this.mOrientationListener.enable(keyguardGoingAway ^ 1);
                    this.mOrientationSensorEnabled = true;
                }
            }
            if (disable && this.mOrientationSensorEnabled) {
                this.mOrientationListener.disable();
                this.mOrientationSensorEnabled = false;
            }
        }
    }

    private void interceptBackKeyDown() {
        MetricsLogger.count(this.mContext, "key_back_down", 1);
        this.mBackKeyHandled = false;
        if (hasPanicPressOnBackBehavior() && this.mBackKeyPressCounter != 0 && this.mBackKeyPressCounter < 4) {
            this.mHandler.removeMessages(20);
        }
        if (hasLongPressOnBackBehavior()) {
            Message msg = this.mHandler.obtainMessage(18);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
        }
    }

    private boolean interceptBackKeyUp(KeyEvent event) {
        boolean handled = this.mBackKeyHandled;
        if (hasPanicPressOnBackBehavior()) {
            this.mBackKeyPressCounter++;
            long eventTime = event.getDownTime();
            if (this.mBackKeyPressCounter <= 4) {
                Message msg = this.mHandler.obtainMessage(20, this.mBackKeyPressCounter, 0, Long.valueOf(eventTime));
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, (long) ViewConfiguration.getMultiPressTimeout());
            }
        }
        cancelPendingBackKeyAction();
        if (this.mHasFeatureWatch) {
            TelecomManager telecomManager = getTelecommService();
            if (telecomManager != null) {
                if (telecomManager.isRinging()) {
                    telecomManager.silenceRinger();
                    return false;
                } else if ((this.mIncallBackBehavior & 1) != 0 && telecomManager.isInCall()) {
                    return telecomManager.endCall();
                }
            }
        }
        if (this.mAutofillManagerInternal != null && event.getKeyCode() == 4) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(24));
        }
        return handled;
    }

    private void interceptPowerKeyDown(KeyEvent event, boolean interactive) {
        HwFrameworkFactory.getHwNsdImpl().StopSdrForSpecial("powerdown", 26);
        long startTime = SystemClock.elapsedRealtime();
        if (!this.mPowerKeyWakeLock.isHeld()) {
            this.mPowerKeyWakeLock.acquire();
        }
        startTime = printTimeoutLog("PowerManager_screenOn", startTime, "acquire wakeLock timeout", 200);
        if (this.mPowerKeyPressCounter != 0) {
            this.mHandler.removeMessages(13);
        }
        if (this.mImmersiveModeConfirmation.onPowerKeyDown(interactive, SystemClock.elapsedRealtime(), isImmersiveMode(this.mLastSystemUiFlags), isNavBarEmpty(this.mLastSystemUiFlags))) {
            this.mHandler.post(this.mHiddenNavPanic);
        }
        if (interactive && (this.mScreenshotChordPowerKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
            this.mScreenshotChordPowerKeyTriggered = true;
            this.mScreenshotChordPowerKeyTime = event.getDownTime();
            interceptScreenshotChord();
        }
        TelecomManager telecomManager = getTelecommService();
        boolean hungUp = false;
        if (telecomManager != null) {
            if (telecomManager.isRinging()) {
                telecomManager.silenceRinger();
            } else if ((this.mIncallPowerBehavior & 2) != 0 && telecomManager.isInCall() && interactive) {
                hungUp = telecomManager.endCall();
            }
        }
        startTime = printTimeoutLog("PowerManager_screenOn", startTime, "telecomManager timeout", 200);
        GestureLauncherService gestureService = (GestureLauncherService) LocalServices.getService(GestureLauncherService.class);
        boolean gesturedServiceIntercepted = false;
        if (gestureService != null) {
            gesturedServiceIntercepted = gestureService.interceptPowerKeyDown(event, interactive, this.mTmpBoolean);
            if (this.mTmpBoolean.value && this.mGoingToSleep) {
                this.mCameraGestureTriggeredDuringGoingToSleep = true;
            }
            startTime = printTimeoutLog("PowerManager_screenOn", startTime, "GestureLauncherService timeout", 200);
        }
        Slog.i(TAG, "hungUp=" + hungUp + ", mScreenshotChordVolumeDownKeyTriggered=" + this.mScreenshotChordVolumeDownKeyTriggered + ", mA11yShortcutChordVolumeUpKeyTriggered=" + this.mA11yShortcutChordVolumeUpKeyTriggered + ", gesturedServiceIntercepted=" + gesturedServiceIntercepted);
        if (hungUp || this.mScreenshotChordVolumeDownKeyTriggered || this.mA11yShortcutChordVolumeUpKeyTriggered) {
            gesturedServiceIntercepted = true;
        }
        this.mPowerKeyHandled = gesturedServiceIntercepted;
        if (!this.mPowerKeyHandled) {
            Message msg;
            if (interactive) {
                if (hasLongPressOnPowerBehavior()) {
                    msg = this.mHandler.obtainMessage(14);
                    msg.setAsynchronous(true);
                    this.mHandler.sendMessageDelayed(msg, 1000);
                }
                notifyPowerkeyInteractive(true);
                return;
            }
            wakeUpFromPowerKey(event.getDownTime());
            if (this.mSupportLongPressPowerWhenNonInteractive && hasLongPressOnPowerBehavior()) {
                msg = this.mHandler.obtainMessage(14);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, 1000);
                this.mBeganFromNonInteractive = true;
            } else if (getMaxMultiPressPowerCount() <= 1) {
                this.mPowerKeyHandled = true;
            } else {
                this.mBeganFromNonInteractive = true;
            }
        }
    }

    private void interceptPowerKeyUp(KeyEvent event, boolean interactive, boolean canceled) {
        int i = 0;
        boolean handled = !canceled ? this.mPowerKeyHandled : true;
        this.mScreenshotChordPowerKeyTriggered = false;
        cancelPendingScreenshotChordAction();
        cancelPendingPowerKeyAction();
        if (!handled) {
            this.mPowerKeyPressCounter++;
            int maxCount = getMaxMultiPressPowerCount();
            long eventTime = event.getDownTime();
            if (this.mPowerKeyPressCounter < maxCount) {
                Handler handler = this.mHandler;
                if (interactive) {
                    i = 1;
                }
                Message msg = handler.obtainMessage(13, i, this.mPowerKeyPressCounter, Long.valueOf(eventTime));
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, (long) ViewConfiguration.getDoubleTapTimeout());
                return;
            }
            powerPress(eventTime, interactive, this.mPowerKeyPressCounter);
        }
        finishPowerKeyPress();
    }

    private void finishPowerKeyPress() {
        this.mBeganFromNonInteractive = false;
        this.mPowerKeyPressCounter = 0;
        if (this.mPowerKeyWakeLock.isHeld()) {
            this.mPowerKeyWakeLock.release();
        }
    }

    private void finishBackKeyPress() {
        this.mBackKeyPressCounter = 0;
    }

    private void cancelPendingPowerKeyAction() {
        if (!this.mPowerKeyHandled) {
            this.mPowerKeyHandled = true;
            this.mHandler.removeMessages(14);
        }
    }

    private void cancelPendingBackKeyAction() {
        if (!this.mBackKeyHandled) {
            this.mBackKeyHandled = true;
            this.mHandler.removeMessages(18);
        }
    }

    private void backMultiPressAction(long eventTime, int count) {
        if (count >= 4) {
            switch (this.mPanicPressOnBackBehavior) {
                case 1:
                    launchHomeFromHotKey();
                    return;
                default:
                    return;
            }
        }
    }

    private void powerPress(long eventTime, boolean interactive, int count) {
        if (!this.mScreenOnEarly || (this.mScreenOnFully ^ 1) == 0) {
            if (count != 2) {
                if (count != 3) {
                    if (interactive && (this.mBeganFromNonInteractive ^ 1) != 0) {
                        switch (this.mShortPressOnPowerBehavior) {
                            case 1:
                                this.mPowerManager.goToSleep(eventTime, 4, 0);
                                break;
                            case 2:
                                this.mPowerManager.goToSleep(eventTime, 4, 1);
                                break;
                            case 3:
                                this.mPowerManager.goToSleep(eventTime, 4, 1);
                                launchHomeFromHotKey();
                                break;
                            case 4:
                                shortPressPowerGoHome();
                                break;
                            case 5:
                                if (!this.mDismissImeOnBackKeyPressed) {
                                    shortPressPowerGoHome();
                                    break;
                                }
                                if (this.mInputMethodManagerInternal == null) {
                                    this.mInputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
                                }
                                if (this.mInputMethodManagerInternal != null) {
                                    this.mInputMethodManagerInternal.hideCurrentInputMethod();
                                    break;
                                }
                                break;
                        }
                    }
                }
                powerMultiPressAction(eventTime, interactive, this.mTriplePressOnPowerBehavior);
            } else {
                powerMultiPressAction(eventTime, interactive, this.mDoublePressOnPowerBehavior);
            }
            return;
        }
        Slog.i(TAG, "Suppressed redundant power key press while already in the process of turning the screen on.");
    }

    private void shortPressPowerGoHome() {
        launchHomeFromHotKey(true, false);
        if (isKeyguardShowingAndNotOccluded()) {
            this.mKeyguardDelegate.onShortPowerPressedGoHome();
        }
    }

    private void powerMultiPressAction(long eventTime, boolean interactive, int behavior) {
        switch (behavior) {
            case 1:
                if (!isUserSetupComplete()) {
                    Slog.i(TAG, "Ignoring toggling theater mode - device not setup.");
                    return;
                } else if (isTheaterModeEnabled()) {
                    Slog.i(TAG, "Toggling theater mode off.");
                    Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 0);
                    if (!interactive) {
                        wakeUpFromPowerKey(eventTime);
                        return;
                    }
                    return;
                } else {
                    Slog.i(TAG, "Toggling theater mode on.");
                    Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 1);
                    if (this.mGoToSleepOnButtonPressTheaterMode && interactive) {
                        this.mPowerManager.goToSleep(eventTime, 4, 0);
                        return;
                    }
                    return;
                }
            case 2:
                Slog.i(TAG, "Starting brightness boost.");
                if (!interactive) {
                    wakeUpFromPowerKey(eventTime);
                }
                this.mPowerManager.boostScreenBrightness(eventTime);
                return;
            default:
                return;
        }
    }

    private int getMaxMultiPressPowerCount() {
        if (this.mTriplePressOnPowerBehavior != 0) {
            return 3;
        }
        if (this.mDoublePressOnPowerBehavior != 0) {
            return 2;
        }
        return 1;
    }

    private void powerLongPress() {
        boolean z = true;
        int behavior = getResolvedLongPressOnPowerBehavior();
        switch (behavior) {
            case 1:
                this.mPowerKeyHandled = true;
                performHapticFeedbackLw(null, 0, false);
                showGlobalActionsInternal();
                return;
            case 2:
            case 3:
                this.mPowerKeyHandled = true;
                performHapticFeedbackLw(null, 0, false);
                sendCloseSystemWindows(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS);
                WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
                if (behavior != 2) {
                    z = false;
                }
                windowManagerFuncs.shutdown(z);
                return;
            default:
                return;
        }
    }

    private void backLongPress() {
        this.mBackKeyHandled = true;
        switch (this.mLongPressOnBackBehavior) {
            case 1:
                boolean keyguardActive;
                if (this.mKeyguardDelegate == null) {
                    keyguardActive = false;
                } else {
                    keyguardActive = this.mKeyguardDelegate.isShowing();
                }
                if (!keyguardActive) {
                    startActivityAsUser(new Intent("android.intent.action.VOICE_ASSIST"), UserHandle.CURRENT_OR_SELF);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void accessibilityShortcutActivated() {
        this.mAccessibilityShortcutController.performAccessibilityShortcut();
    }

    private void disposeInputConsumer(InputConsumer inputConsumer) {
        if (inputConsumer != null) {
            inputConsumer.dismiss();
        }
    }

    private void sleepPress(long eventTime) {
        if (this.mShortPressOnSleepBehavior == 1) {
            launchHomeFromHotKey(false, true);
        }
    }

    private void sleepRelease(long eventTime) {
        switch (this.mShortPressOnSleepBehavior) {
            case 0:
            case 1:
                Slog.i(TAG, "sleepRelease() calling goToSleep(GO_TO_SLEEP_REASON_SLEEP_BUTTON)");
                this.mPowerManager.goToSleep(eventTime, 6, 0);
                return;
            default:
                return;
        }
    }

    private int getResolvedLongPressOnPowerBehavior() {
        if (FactoryTest.isLongPressOnPowerOffEnabled()) {
            return 3;
        }
        return this.mLongPressOnPowerBehavior;
    }

    private boolean hasLongPressOnPowerBehavior() {
        return getResolvedLongPressOnPowerBehavior() != 0;
    }

    private boolean hasLongPressOnBackBehavior() {
        return this.mLongPressOnBackBehavior != 0;
    }

    private boolean hasPanicPressOnBackBehavior() {
        return this.mPanicPressOnBackBehavior != 0;
    }

    private void interceptScreenshotChord() {
        if (HWFLOW) {
            Slog.i(TAG, "takeScreenshot enabled  " + this.mScreenshotChordEnabled + "  VolumeDownKeyTriggered  " + this.mScreenshotChordVolumeDownKeyTriggered + " PowerKeyTriggered  " + this.mScreenshotChordPowerKeyTriggered + " VolumeUpKeyTriggered  " + this.mA11yShortcutChordVolumeUpKeyTriggered);
        }
        if (this.mScreenshotChordEnabled && this.mScreenshotChordVolumeDownKeyTriggered && this.mScreenshotChordPowerKeyTriggered && (this.mA11yShortcutChordVolumeUpKeyTriggered ^ 1) != 0) {
            long now = SystemClock.uptimeMillis();
            if (HWFLOW) {
                Slog.i(TAG, "takeScreenshot downKeyTime=  " + this.mScreenshotChordVolumeDownKeyTime + " powerKeyTime=  " + this.mScreenshotChordPowerKeyTime + " now = " + now);
            }
            if (now <= this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS && now <= this.mScreenshotChordPowerKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {
                this.mScreenshotChordVolumeDownKeyConsumed = true;
                cancelPendingPowerKeyAction();
                this.mScreenshotRunnable.setScreenshotType(1);
                Flog.bdReport(this.mContext, 71);
                this.mHandler.postDelayed(this.mScreenshotRunnable, getScreenshotChordLongPressDelay());
            }
        }
    }

    private void interceptAccessibilityShortcutChord() {
        if (this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(isKeyguardLocked()) && this.mScreenshotChordVolumeDownKeyTriggered && this.mA11yShortcutChordVolumeUpKeyTriggered && (this.mScreenshotChordPowerKeyTriggered ^ 1) != 0) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS && now <= this.mA11yShortcutChordVolumeUpKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {
                this.mScreenshotChordVolumeDownKeyConsumed = true;
                this.mA11yShortcutChordVolumeUpKeyConsumed = true;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(21), ViewConfiguration.get(this.mContext).getAccessibilityShortcutKeyTimeout());
            }
        }
    }

    private long getScreenshotChordLongPressDelay() {
        if (this.mKeyguardDelegate.isShowing()) {
            return (long) (((float) ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout()) * KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER);
        }
        return 0;
    }

    private void cancelPendingScreenshotChordAction() {
        if (HWFLOW) {
            Slog.i(TAG, "takeScreenshot cancelPendingScreenshotChordAction");
        }
        this.mHandler.removeCallbacks(this.mScreenshotRunnable);
    }

    private void cancelPendingAccessibilityShortcutAction() {
        this.mHandler.removeMessages(21);
    }

    public void showGlobalActions() {
        this.mHandler.removeMessages(10);
        this.mHandler.sendEmptyMessage(10);
    }

    void showGlobalActionsInternal() {
        if (HwDeviceManager.disallowOp(37)) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(PhoneWindowManager.this.mContext, 33685954, 0);
                    toast.getWindowParams().type = 2010;
                    LayoutParams windowParams = toast.getWindowParams();
                    windowParams.privateFlags |= 16;
                    toast.show();
                }
            });
            return;
        }
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS);
        if (HwPolicyFactory.ifUseHwGlobalActions()) {
            HwPolicyFactory.showHwGlobalActionsFragment(this.mContext, this.mWindowManagerFuncs, this.mPowerManager, isKeyguardShowingAndNotOccluded(), isKeyguardSecure(this.mCurrentUserId), isDeviceProvisioned());
            return;
        }
        if (this.mGlobalActions == null) {
            this.mGlobalActions = new GlobalActions(this.mContext, this.mWindowManagerFuncs);
        }
        boolean keyguardShowing = isKeyguardShowingAndNotOccluded();
        this.mGlobalActions.showDialog(keyguardShowing, isDeviceProvisioned());
        if (keyguardShowing) {
            this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }
    }

    boolean isDeviceProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    boolean isUserSetupComplete() {
        boolean isSetupComplete = Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0;
        if (this.mHasFeatureLeanback) {
            return isSetupComplete & isTvUserSetupComplete();
        }
        return isSetupComplete;
    }

    private boolean isTvUserSetupComplete() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "tv_user_setup_complete", 0, -2) != 0;
    }

    private void handleShortPressOnHome() {
        HdmiControl hdmiControl = getHdmiControl();
        if (hdmiControl != null) {
            hdmiControl.turnOnTv();
        }
        if (this.mDreamManagerInternal == null || !this.mDreamManagerInternal.isDreaming()) {
            launchHomeFromHotKey();
        } else {
            this.mDreamManagerInternal.stopDream(false);
        }
    }

    private HdmiControl getHdmiControl() {
        if (this.mHdmiControl == null) {
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.hdmi.cec")) {
                return null;
            }
            HdmiControlManager manager = (HdmiControlManager) this.mContext.getSystemService("hdmi_control");
            HdmiPlaybackClient client = null;
            if (manager != null) {
                client = manager.getPlaybackClient();
            }
            this.mHdmiControl = new HdmiControl(client, null);
        }
        return this.mHdmiControl;
    }

    private void handleLongPressOnHome(int deviceId) {
        if (this.mLongPressOnHomeBehavior != 0) {
            this.mHomeConsumed = true;
            performHapticFeedbackLw(null, 0, false);
            switch (this.mLongPressOnHomeBehavior) {
                case 1:
                    launchAllAppsAction();
                    break;
                case 2:
                    launchAssistAction(null, deviceId);
                    break;
                default:
                    Log.w(TAG, "Undefined home long press behavior: " + this.mLongPressOnHomeBehavior);
                    break;
            }
        }
    }

    private void launchAllAppsAction() {
        startActivityAsUser(new Intent("android.intent.action.ALL_APPS"), UserHandle.CURRENT);
    }

    private void handleDoubleTapOnHome() {
        if (this.mDoubleTapOnHomeBehavior == 1) {
            this.mHomeConsumed = true;
            toggleRecentApps();
        }
    }

    private void showPictureInPictureMenu(KeyEvent event) {
        this.mHandler.removeMessages(17);
        Message msg = this.mHandler.obtainMessage(17);
        msg.setAsynchronous(true);
        msg.sendToTarget();
    }

    private void showPictureInPictureMenuInternal() {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showPictureInPictureMenu();
        }
    }

    private boolean isRoundWindow() {
        return this.mContext.getResources().getConfiguration().isScreenRound();
    }

    public void init(Context context, IWindowManager windowManager, WindowManagerFuncs windowManagerFuncs) {
        boolean z;
        this.mContext = context;
        this.mWindowManager = windowManager;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        this.mDreamManagerInternal = (DreamManagerInternal) LocalServices.getService(DreamManagerInternal.class);
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mHasFeatureWatch = this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        this.mHasFeatureLeanback = this.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
        this.mAccessibilityShortcutController = new AccessibilityShortcutController(this.mContext, new Handler(), this.mCurrentUserId);
        boolean burnInProtectionEnabled = context.getResources().getBoolean(17956942);
        boolean burnInProtectionDevMode = SystemProperties.getBoolean("persist.debug.force_burn_in", false);
        if (burnInProtectionEnabled || burnInProtectionDevMode) {
            int minHorizontal;
            int maxHorizontal;
            int minVertical;
            int maxVertical;
            int maxRadius;
            if (burnInProtectionDevMode) {
                minHorizontal = -8;
                maxHorizontal = 8;
                minVertical = -8;
                maxVertical = -4;
                if (isRoundWindow()) {
                    maxRadius = 6;
                } else {
                    maxRadius = -1;
                }
            } else {
                Resources resources = context.getResources();
                minHorizontal = resources.getInteger(17694752);
                maxHorizontal = resources.getInteger(17694749);
                minVertical = resources.getInteger(17694753);
                maxVertical = resources.getInteger(17694751);
                maxRadius = resources.getInteger(17694750);
            }
            this.mBurnInProtectionHelper = new BurnInProtectionHelper(context, minHorizontal, maxHorizontal, minVertical, maxVertical, maxRadius);
        }
        this.mHandler = new PolicyHandler(this, null);
        this.mWakeGestureListener = new MyWakeGestureListener(this.mContext, this.mHandler);
        this.mOrientationListener = new MyOrientationListener(this.mContext, this.mHandler);
        try {
            this.mOrientationListener.setCurrentRotation(windowManager.getDefaultDisplayRotation());
        } catch (RemoteException e) {
        }
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mSettingsObserver.observe();
        if (mSupportAod) {
            Slog.i(TAG, "AOD mAodSwitchObserver.observe");
            this.mAodSwitchObserver = new AodSwitchObserver(this.mHandler);
            this.mAodSwitchObserver.observe();
        }
        this.mShortcutManager = new ShortcutManager(context);
        this.mUiMode = context.getResources().getInteger(17694772);
        this.mHomeIntent = new Intent("android.intent.action.MAIN", null);
        this.mHomeIntent.addCategory("android.intent.category.HOME");
        this.mHomeIntent.addFlags(270533120);
        this.mEnableCarDockHomeCapture = context.getResources().getBoolean(17956943);
        this.mCarDockIntent = new Intent("android.intent.action.MAIN", null);
        this.mCarDockIntent.addCategory("android.intent.category.CAR_DOCK");
        this.mCarDockIntent.addFlags(270532608);
        this.mDeskDockIntent = new Intent("android.intent.action.MAIN", null);
        this.mDeskDockIntent.addCategory("android.intent.category.DESK_DOCK");
        this.mDeskDockIntent.addFlags(270532608);
        this.mVrHeadsetHomeIntent = new Intent("android.intent.action.MAIN", null);
        this.mVrHeadsetHomeIntent.addCategory("android.intent.category.VR_HOME");
        this.mVrHeadsetHomeIntent.addFlags(270532608);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mBroadcastWakeLock = this.mPowerManager.newWakeLock(1, "PhoneWindowManager.mBroadcastWakeLock");
        this.mPowerKeyWakeLock = this.mPowerManager.newWakeLock(1, "PhoneWindowManager.mPowerKeyWakeLock");
        this.mEnableShiftMenuBugReports = "1".equals(SystemProperties.get("ro.debuggable"));
        this.mSupportAutoRotation = this.mContext.getResources().getBoolean(17957017);
        this.mLidOpenRotation = readRotation(17694797);
        this.mCarDockRotation = readRotation(17694756);
        this.mDeskDockRotation = readRotation(17694775);
        this.mUndockedHdmiRotation = readRotation(17694858);
        this.mCarDockEnablesAccelerometer = this.mContext.getResources().getBoolean(17956908);
        this.mDeskDockEnablesAccelerometer = this.mContext.getResources().getBoolean(17956919);
        this.mLidKeyboardAccessibility = this.mContext.getResources().getInteger(17694795);
        this.mLidNavigationAccessibility = this.mContext.getResources().getInteger(17694796);
        this.mLidControlsScreenLock = this.mContext.getResources().getBoolean(17956975);
        this.mLidControlsSleep = this.mContext.getResources().getBoolean(17956976);
        this.mTranslucentDecorEnabled = this.mContext.getResources().getBoolean(17956959);
        this.mAllowTheaterModeWakeFromKey = this.mContext.getResources().getBoolean(17956879);
        if (this.mAllowTheaterModeWakeFromKey) {
            z = true;
        } else {
            z = this.mContext.getResources().getBoolean(17956883);
        }
        this.mAllowTheaterModeWakeFromPowerKey = z;
        this.mAllowTheaterModeWakeFromMotion = this.mContext.getResources().getBoolean(17956881);
        this.mAllowTheaterModeWakeFromMotionWhenNotDreaming = this.mContext.getResources().getBoolean(17956882);
        this.mAllowTheaterModeWakeFromCameraLens = this.mContext.getResources().getBoolean(17956876);
        this.mAllowTheaterModeWakeFromLidSwitch = this.mContext.getResources().getBoolean(17956880);
        this.mAllowTheaterModeWakeFromWakeGesture = this.mContext.getResources().getBoolean(17956878);
        this.mGoToSleepOnButtonPressTheaterMode = this.mContext.getResources().getBoolean(17956968);
        this.mSupportLongPressPowerWhenNonInteractive = this.mContext.getResources().getBoolean(17957019);
        this.mLongPressOnBackBehavior = this.mContext.getResources().getInteger(17694800);
        this.mPanicPressOnBackBehavior = this.mContext.getResources().getInteger(17694740);
        this.mShortPressOnPowerBehavior = this.mContext.getResources().getInteger(17694851);
        this.mLongPressOnPowerBehavior = this.mContext.getResources().getInteger(17694802);
        this.mDoublePressOnPowerBehavior = this.mContext.getResources().getInteger(17694777);
        this.mTriplePressOnPowerBehavior = this.mContext.getResources().getInteger(17694857);
        this.mShortPressOnSleepBehavior = this.mContext.getResources().getInteger(17694852);
        this.mUseTvRouting = AudioSystem.getPlatformType(this.mContext) == 2;
        this.mHandleVolumeKeysInWM = this.mContext.getResources().getBoolean(17956970);
        readConfigurationDependentBehaviors();
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
        IntentFilter filter = new IntentFilter();
        filter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_ENTER_DESK_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_DESK_MODE);
        filter.addAction("android.intent.action.DOCK_EVENT");
        Intent intent = context.registerReceiver(this.mDockReceiver, filter);
        context.registerReceiver(this.mShutdownReceiver, new IntentFilter(ACTION_ACTURAL_SHUTDOWN), HUAWEI_SHUTDOWN_PERMISSION, null);
        if (intent != null) {
            this.mDockMode = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
        }
        filter = new IntentFilter();
        filter.addAction("android.intent.action.DREAMING_STARTED");
        filter.addAction("android.intent.action.DREAMING_STOPPED");
        context.registerReceiver(this.mDreamReceiver, filter);
        context.registerReceiver(this.mMultiuserReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
        this.mSystemGestures = new SystemGesturesPointerEventListener(context, new Callbacks() {
            public void onSwipeFromTop() {
                if (!(PhoneWindowManager.this.isGestureIsolated() || Secure.getInt(PhoneWindowManager.this.mContext.getContentResolver(), "device_provisioned", 1) == 0 || PhoneWindowManager.this.swipeFromTop() || PhoneWindowManager.this.mStatusBar == null)) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mStatusBar);
                }
            }

            public void onSwipeFromBottom() {
                if (!(PhoneWindowManager.this.isGestureIsolated() || Secure.getInt(PhoneWindowManager.this.mContext.getContentResolver(), "device_provisioned", 1) == 0 || PhoneWindowManager.this.swipeFromBottom() || PhoneWindowManager.this.mNavigationBar == null || PhoneWindowManager.this.mNavigationBarPosition != 0)) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onSwipeFromRight() {
                if (!(PhoneWindowManager.this.isGestureIsolated() || PhoneWindowManager.this.swipeFromRight() || PhoneWindowManager.this.mNavigationBar == null || PhoneWindowManager.this.mNavigationBarPosition != 1)) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onSwipeFromLeft() {
                if (PhoneWindowManager.this.mNavigationBar != null && PhoneWindowManager.this.mNavigationBarPosition == 2) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onFling(int duration) {
                if (PhoneWindowManager.this.mPowerManagerInternal != null) {
                    PhoneWindowManager.this.mPowerManagerInternal.powerHint(2, duration);
                }
            }

            public void onDebug() {
            }

            public void onDown() {
                PhoneWindowManager.this.mOrientationListener.onTouchStart();
            }

            public void onUpOrCancel() {
                PhoneWindowManager.this.mOrientationListener.onTouchEnd();
            }

            public void onMouseHoverAtTop() {
                PhoneWindowManager.this.mHandler.removeMessages(16);
                Message msg = PhoneWindowManager.this.mHandler.obtainMessage(16);
                msg.arg1 = 0;
                PhoneWindowManager.this.mHandler.sendMessageDelayed(msg, 500);
            }

            public void onMouseHoverAtBottom() {
                PhoneWindowManager.this.mHandler.removeMessages(16);
                Message msg = PhoneWindowManager.this.mHandler.obtainMessage(16);
                msg.arg1 = 1;
                PhoneWindowManager.this.mHandler.sendMessageDelayed(msg, 500);
            }

            public void onMouseLeaveFromEdge() {
                PhoneWindowManager.this.mHandler.removeMessages(16);
            }
        });
        this.mImmersiveModeConfirmation = new ImmersiveModeConfirmation(this.mContext);
        this.mWindowManagerFuncs.registerPointerEventListener(this.mSystemGestures);
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        this.mLongPressVibePattern = getLongIntArray(this.mContext.getResources(), 17236015);
        this.mVirtualKeyVibePattern = getLongIntArray(this.mContext.getResources(), 17236046);
        this.mKeyboardTapVibePattern = getLongIntArray(this.mContext.getResources(), 17236013);
        this.mClockTickVibePattern = getLongIntArray(this.mContext.getResources(), 17235996);
        this.mCalendarDateVibePattern = getLongIntArray(this.mContext.getResources(), 17235990);
        this.mSafeModeDisabledVibePattern = getLongIntArray(this.mContext.getResources(), 17236026);
        this.mSafeModeEnabledVibePattern = getLongIntArray(this.mContext.getResources(), 17236027);
        this.mContextClickVibePattern = getLongIntArray(this.mContext.getResources(), 17235997);
        this.mScreenshotChordEnabled = this.mContext.getResources().getBoolean(17956958);
        this.mGlobalKeyManager = new GlobalKeyManager(this.mContext);
        initializeHdmiState();
        if (!this.mPowerManager.isInteractive()) {
            startedGoingToSleep(2);
            finishedGoingToSleep(2);
        }
        this.mWindowManagerInternal.registerAppTransitionListener(this.mStatusBarController.getAppTransitionListener());
        this.mWindowManagerInternal.registerAppTransitionListener(new AppTransitionListener() {
            public int onAppTransitionStartingLocked(int transit, IBinder openToken, IBinder closeToken, Animation openAnimation, Animation closeAnimation) {
                return PhoneWindowManager.this.handleStartTransitionForKeyguardLw(transit, openAnimation);
            }

            public void onAppTransitionCancelledLocked(int transit) {
                PhoneWindowManager.this.handleStartTransitionForKeyguardLw(transit, null);
            }
        });
        this.mKeyguardDelegate = new KeyguardServiceDelegate(this.mContext, new StateCallback() {
            public void onTrustedChanged() {
                PhoneWindowManager.this.mWindowManagerFuncs.notifyKeyguardTrustedChanged();
            }
        });
        hwInit();
    }

    private void readConfigurationDependentBehaviors() {
        Resources res = this.mContext.getResources();
        this.mLongPressOnHomeBehavior = res.getInteger(17694801);
        if (this.mLongPressOnHomeBehavior < 0 || this.mLongPressOnHomeBehavior > 2) {
            this.mLongPressOnHomeBehavior = 0;
        }
        this.mDoubleTapOnHomeBehavior = res.getInteger(17694778);
        if (this.mDoubleTapOnHomeBehavior < 0 || this.mDoubleTapOnHomeBehavior > 1) {
            this.mDoubleTapOnHomeBehavior = 0;
        }
        this.mShortPressWindowBehavior = 0;
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.picture_in_picture")) {
            this.mShortPressWindowBehavior = 1;
        }
        this.mNavBarOpacityMode = res.getInteger(17694816);
    }

    public void setInitialDisplaySize(Display display, int width, int height, int density) {
        if (this.mContext != null && display.getDisplayId() == 0) {
            int shortSize;
            int longSize;
            this.mDisplay = display;
            Resources res = this.mContext.getResources();
            if (width > height) {
                shortSize = height;
                longSize = width;
                this.mLandscapeRotation = 0;
                this.mSeascapeRotation = 2;
                if (res.getBoolean(17956997)) {
                    this.mPortraitRotation = 1;
                    this.mUpsideDownRotation = 3;
                } else {
                    this.mPortraitRotation = 3;
                    this.mUpsideDownRotation = 1;
                }
            } else {
                shortSize = width;
                longSize = height;
                this.mPortraitRotation = 0;
                this.mUpsideDownRotation = 2;
                if (res.getBoolean(17956997)) {
                    this.mLandscapeRotation = 3;
                    this.mSeascapeRotation = 1;
                } else {
                    this.mLandscapeRotation = 1;
                    this.mSeascapeRotation = 3;
                }
            }
            int shortSizeDp = (shortSize * 160) / density;
            int longSizeDp = (longSize * 160) / density;
            boolean z = width != height && shortSizeDp <= System.getInt(this.mContext.getContentResolver(), "hw_split_navigation_bar_dp", 600);
            this.mNavigationBarCanMove = z;
            if (this.mNavigationBarCanMove) {
                int defaultRotation = SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90;
                if (defaultRotation == 1 || defaultRotation == 3) {
                    this.mNavigationBarCanMove = false;
                }
            }
            this.mHasNavigationBar = res.getBoolean(17957005);
            String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                this.mHasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                this.mHasNavigationBar = true;
            }
            if ("portrait".equals(SystemProperties.get("persist.demo.hdmirotation"))) {
                this.mDemoHdmiRotation = this.mPortraitRotation;
            } else {
                this.mDemoHdmiRotation = this.mLandscapeRotation;
            }
            this.mDemoHdmiRotationLock = SystemProperties.getBoolean("persist.demo.hdmirotationlock", false);
            if ("portrait".equals(SystemProperties.get("persist.demo.remoterotation"))) {
                this.mDemoRotation = this.mPortraitRotation;
            } else {
                this.mDemoRotation = this.mLandscapeRotation;
            }
            this.mDemoRotationLock = SystemProperties.getBoolean("persist.demo.rotationlock", false);
            z = (longSizeDp < 960 || shortSizeDp < 720 || !res.getBoolean(17956965)) ? false : "true".equals(SystemProperties.get("config.override_forced_orient")) ^ 1;
            this.mForceDefaultOrientation = z;
        }
    }

    private boolean canHideNavigationBar() {
        return this.mHasNavigationBar;
    }

    public boolean isDefaultOrientationForced() {
        return this.mForceDefaultOrientation;
    }

    public void setDisplayOverscan(Display display, int left, int top, int right, int bottom) {
        if (display.getDisplayId() == 0) {
            this.mOverscanLeft = left;
            this.mOverscanTop = top;
            this.mOverscanRight = right;
            this.mOverscanBottom = bottom;
        }
    }

    public void updateSettings() {
        int i = 2;
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean updateRotation = false;
        synchronized (this.mLock) {
            int userRotationMode;
            this.mEndcallBehavior = System.getIntForUser(resolver, "end_button_behavior", 2, -2);
            this.mIncallPowerBehavior = Secure.getIntForUser(resolver, "incall_power_button_behavior", 1, -2);
            this.mIncallBackBehavior = Secure.getIntForUser(resolver, "incall_back_button_behavior", 0, -2);
            boolean wakeGestureEnabledSetting = Secure.getIntForUser(resolver, "wake_gesture_enabled", 0, -2) != 0;
            if (this.mWakeGestureEnabledSetting != wakeGestureEnabledSetting) {
                this.mWakeGestureEnabledSetting = wakeGestureEnabledSetting;
                updateWakeGestureListenerLp();
            }
            int userRotation = System.getIntForUser(resolver, "user_rotation", 0, -2);
            if (this.mUserRotation != userRotation) {
                this.mUserRotation = userRotation;
                updateRotation = true;
            }
            if (System.getIntForUser(resolver, "accelerometer_rotation", 0, -2) != 0) {
                userRotationMode = 0;
            } else {
                userRotationMode = 1;
            }
            if (this.mUserRotationMode != userRotationMode) {
                this.mUserRotationMode = userRotationMode;
                updateRotation = true;
                updateOrientationListenerLp();
            }
            if (this.mSystemReady) {
                int pointerLocation = System.getIntForUser(resolver, "pointer_location", 0, -2);
                if (this.mPointerLocationMode != pointerLocation) {
                    this.mPointerLocationMode = pointerLocation;
                    Handler handler = this.mHandler;
                    if (pointerLocation != 0) {
                        i = 1;
                    }
                    handler.sendEmptyMessage(i);
                }
            }
            this.mLockScreenTimeout = System.getIntForUser(resolver, "screen_off_timeout", 0, -2);
            String imId = Secure.getStringForUser(resolver, "default_input_method", -2);
            boolean hasSoftInput = imId != null && imId.length() > 0;
            if (this.mHasSoftInput != hasSoftInput) {
                this.mHasSoftInput = hasSoftInput;
                updateRotation = true;
            }
            if (this.mImmersiveModeConfirmation != null) {
                this.mImmersiveModeConfirmation.loadSetting(this.mCurrentUserId);
            }
        }
        synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
            PolicyControl.reloadFromSetting(this.mContext);
        }
        if (updateRotation) {
            updateRotation(true);
        }
    }

    private void updateWakeGestureListenerLp() {
        if (shouldEnableWakeGestureLp()) {
            this.mWakeGestureListener.requestWakeUpTrigger();
        } else {
            this.mWakeGestureListener.cancelWakeUpTrigger();
        }
    }

    private boolean shouldEnableWakeGestureLp() {
        if (!this.mWakeGestureEnabledSetting || (this.mAwake ^ 1) == 0) {
            return false;
        }
        if (this.mLidControlsSleep && this.mLidState == 0) {
            return false;
        }
        return this.mWakeGestureListener.isSupported();
    }

    private void enablePointerLocation() {
        if (this.mPointerLocationView == null) {
            this.mPointerLocationView = new PointerLocationView(this.mContext);
            this.mPointerLocationView.setPrintCoords(false);
            LayoutParams lp = new LayoutParams(-1, -1);
            lp.type = 2015;
            lp.flags = 1304;
            if (ActivityManager.isHighEndGfx()) {
                lp.flags |= 16777216;
                lp.privateFlags |= 2;
            }
            lp.format = -3;
            lp.setTitle("PointerLocation");
            WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
            lp.inputFeatures |= 2;
            wm.addView(this.mPointerLocationView, lp);
            this.mWindowManagerFuncs.registerPointerEventListener(this.mPointerLocationView);
        }
    }

    private void disablePointerLocation() {
        if (this.mPointerLocationView != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.mPointerLocationView);
            ((WindowManager) this.mContext.getSystemService("window")).removeView(this.mPointerLocationView);
            this.mPointerLocationView = null;
        }
    }

    private int readRotation(int resID) {
        try {
            switch (this.mContext.getResources().getInteger(resID)) {
                case 0:
                    return 0;
                case 90:
                    return 1;
                case 180:
                    return 2;
                case 270:
                    return 3;
            }
        } catch (NotFoundException e) {
        }
        return -1;
    }

    public int checkAddPermission(LayoutParams attrs, int[] outAppOp) {
        int i = 0;
        int type = attrs.type;
        outAppOp[0] = -1;
        if ((type < 1 || type > 99) && ((type < 1000 || type > 1999) && (type < IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || type > 2999))) {
            return -10;
        }
        if (type < IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || type > 2999) {
            return 0;
        }
        if (LayoutParams.isSystemAlertWindowType(type)) {
            outAppOp[0] = 24;
            int callingUid = Binder.getCallingUid();
            if (UserHandle.getAppId(callingUid) == 1000) {
                return 0;
            }
            ApplicationInfo appInfo;
            try {
                appInfo = this.mContext.getPackageManager().getApplicationInfoAsUser(attrs.packageName, 0, UserHandle.getUserId(callingUid));
            } catch (NameNotFoundException e) {
                appInfo = null;
            }
            if (HwSystemManager.checkWindowType(attrs.privateFlags) && appInfo != null && appInfo.targetSdkVersion < 26) {
                return 0;
            }
            if (appInfo == null || (type != 2038 && appInfo.targetSdkVersion >= 26)) {
                Slog.w(TAG, "This alert window type is deprecated, Pls use TYPE_APPLICATION_OVERLAY type.");
                if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") != 0) {
                    i = -8;
                }
                return i;
            }
            switch (this.mAppOpsManager.checkOpNoThrow(outAppOp[0], callingUid, attrs.packageName)) {
                case 0:
                case 1:
                    return 0;
                case 2:
                    return appInfo.targetSdkVersion < 23 ? 0 : -8;
                default:
                    if (this.mContext.checkCallingOrSelfPermission("android.permission.SYSTEM_ALERT_WINDOW") != 0) {
                        i = -8;
                    }
                    return i;
            }
        }
        switch (type) {
            case 2005:
                outAppOp[0] = 45;
                return 0;
            case 2011:
            case 2013:
            case 2023:
            case 2030:
            case 2031:
            case 2032:
            case 2035:
            case 2037:
            case 2102:
            case 2103:
                return 0;
            default:
                if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") != 0) {
                    i = -8;
                }
                return i;
        }
    }

    public boolean checkShowToOwnerOnly(LayoutParams attrs) {
        boolean z = true;
        switch (attrs.type) {
            case 3:
            case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /*2000*/:
            case 2001:
            case 2002:
            case 2007:
            case 2008:
            case 2009:
            case 2014:
            case 2017:
            case 2018:
            case 2019:
            case 2020:
            case 2021:
            case 2022:
            case 2024:
            case 2026:
            case 2027:
            case 2030:
            case 2034:
            case 2037:
                break;
            default:
                if ((attrs.privateFlags & 16) == 0) {
                    return true;
                }
                break;
        }
        if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") == 0) {
            z = false;
        }
        return z;
    }

    public void adjustWindowParamsLw(LayoutParams attrs) {
        switch (attrs.type) {
            case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /*2000*/:
                if (this.mKeyguardOccluded) {
                    attrs.flags &= -1048577;
                    attrs.privateFlags &= -1025;
                    break;
                }
                break;
            case 2005:
                if (attrs.hideTimeoutMilliseconds < 0 || attrs.hideTimeoutMilliseconds > 3500) {
                    attrs.hideTimeoutMilliseconds = 3500;
                }
                attrs.windowAnimations = 16973828;
                break;
            case 2006:
            case 2015:
                attrs.flags |= 24;
                attrs.flags &= -262145;
                break;
            case 2036:
                attrs.flags |= 8;
                break;
        }
        if (attrs.type != IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
            attrs.privateFlags &= -1025;
        }
        if (ActivityManager.isHighEndGfx()) {
            if ((attrs.flags & Integer.MIN_VALUE) != 0) {
                attrs.subtreeSystemUiVisibility |= 512;
            }
            boolean forceWindowDrawsStatusBarBackground = (attrs.privateFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0;
            if ((attrs.flags & Integer.MIN_VALUE) != 0 || (forceWindowDrawsStatusBarBackground && attrs.height == -1 && attrs.width == -1)) {
                attrs.subtreeSystemUiVisibility |= 1024;
            }
        }
    }

    void readLidState() {
        this.mLidState = this.mWindowManagerFuncs.getLidState();
    }

    private void readCameraLensCoverState() {
        this.mCameraLensCoverState = this.mWindowManagerFuncs.getCameraLensCoverState();
    }

    private boolean isHidden(int accessibilityMode) {
        boolean z = true;
        switch (accessibilityMode) {
            case 1:
                if (this.mLidState != 0) {
                    z = false;
                }
                return z;
            case 2:
                if (this.mLidState != 1) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public void adjustConfigurationLw(Configuration config, int keyboardPresence, int navigationPresence) {
        boolean z = false;
        if ((keyboardPresence & 1) != 0) {
            z = true;
        }
        this.mHaveBuiltInKeyboard = z;
        readConfigurationDependentBehaviors();
        readLidState();
        if (config.keyboard == 1 || (keyboardPresence == 1 && isHidden(this.mLidKeyboardAccessibility))) {
            config.hardKeyboardHidden = 2;
            if (!this.mHasSoftInput) {
                config.keyboardHidden = 2;
            }
        }
        if (config.navigation == 1 || (navigationPresence == 1 && isHidden(this.mLidNavigationAccessibility))) {
            config.navigationHidden = 2;
        }
    }

    public void onConfigurationChanged() {
        Resources res = this.mContext.getResources();
        this.mStatusBarHeight = res.getDimensionPixelSize(17105234);
        int[] iArr = this.mNavigationBarHeightForRotationDefault;
        int i = this.mPortraitRotation;
        int dimensionPixelSize = res.getDimensionPixelSize(17105141);
        this.mNavigationBarHeightForRotationDefault[this.mUpsideDownRotation] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
        iArr = this.mNavigationBarHeightForRotationDefault;
        i = this.mLandscapeRotation;
        dimensionPixelSize = res.getDimensionPixelSize(17105143);
        this.mNavigationBarHeightForRotationDefault[this.mSeascapeRotation] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
        iArr = this.mNavigationBarWidthForRotationDefault;
        i = this.mPortraitRotation;
        dimensionPixelSize = res.getDimensionPixelSize(17105146);
        this.mNavigationBarWidthForRotationDefault[this.mSeascapeRotation] = dimensionPixelSize;
        this.mNavigationBarWidthForRotationDefault[this.mLandscapeRotation] = dimensionPixelSize;
        this.mNavigationBarWidthForRotationDefault[this.mUpsideDownRotation] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
    }

    public int getMaxWallpaperLayer() {
        return getWindowLayerFromTypeLw(IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
    }

    private int getNavigationBarWidth(int rotation, int uiMode) {
        return this.mNavigationBarWidthForRotationDefault[rotation];
    }

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        if (displayId == 0 && this.mHasNavigationBar && this.mNavigationBarCanMove && fullWidth > fullHeight) {
            return fullWidth - getNavigationBarWidth(rotation, uiMode);
        }
        return fullWidth;
    }

    private int getNavigationBarHeight(int rotation, int uiMode) {
        return this.mNavigationBarHeightForRotationDefault[rotation];
    }

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        if (displayId == 0 && this.mHasNavigationBar && (!this.mNavigationBarCanMove || fullWidth < fullHeight)) {
            return fullHeight - getNavigationBarHeight(rotation, uiMode);
        }
        return fullHeight;
    }

    public int getConfigDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        return getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode, displayId);
    }

    public int getConfigDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        if (displayId == 0) {
            return getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayId) - this.mStatusBarHeight;
        }
        return fullHeight;
    }

    public boolean isKeyguardHostWindow(LayoutParams attrs) {
        return attrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME;
    }

    public boolean canBeHiddenByKeyguardLw(WindowState win) {
        boolean z = false;
        switch (win.getAttrs().type) {
            case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /*2000*/:
            case 2013:
            case 2019:
            case 2023:
            case 2102:
                return false;
            default:
                if (getWindowLayerLw(win) < getWindowLayerFromTypeLw(IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME)) {
                    z = true;
                }
                return z;
        }
    }

    private boolean shouldBeHiddenByKeyguard(WindowState win, WindowState imeTarget) {
        boolean z = true;
        int i = 0;
        if (win.getAppToken() != null) {
            return false;
        }
        int showImeOverKeyguard;
        int allowWhenLocked;
        boolean hideDockDivider;
        LayoutParams attrs = win.getAttrs();
        if (imeTarget == null || !imeTarget.isVisibleLw()) {
            showImeOverKeyguard = 0;
        } else if ((imeTarget.getAttrs().flags & DumpState.DUMP_FROZEN) == 0) {
            showImeOverKeyguard = canBeHiddenByKeyguardLw(imeTarget) ^ 1;
        } else {
            showImeOverKeyguard = 1;
        }
        if (win.isInputMethodWindow() || imeTarget == this) {
            allowWhenLocked = showImeOverKeyguard;
        } else {
            allowWhenLocked = 0;
        }
        if (isKeyguardLocked() && isKeyguardOccluded()) {
            if ((attrs.flags & DumpState.DUMP_FROZEN) != 0) {
                i = 1;
            } else if ((attrs.privateFlags & 256) != 0) {
                i = 1;
            }
            allowWhenLocked |= i;
        }
        boolean keyguardLocked = isKeyguardLocked();
        if (attrs.type == 2034) {
            hideDockDivider = this.mWindowManagerInternal.isStackVisible(3) ^ 1;
        } else {
            hideDockDivider = false;
        }
        if (!(keyguardLocked && (allowWhenLocked ^ 1) != 0 && win.getDisplayId() == 0)) {
            z = hideDockDivider;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:82:0x02f3, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public StartingSurface addSplashScreen(IBinder appToken, String packageName, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, Configuration overrideConfig, int displayId) {
        if (packageName == null) {
            return null;
        }
        View view = null;
        try {
            Context displayContext = getDisplayContext(this.mContext, displayId);
            if (displayContext == null) {
                return null;
            }
            StartingSurface startingSurface;
            Context context = displayContext;
            if (!(theme == displayContext.getThemeResId() && labelRes == 0)) {
                try {
                    context = displayContext.createPackageContext(packageName, 4);
                    context.setTheme(theme);
                } catch (NameNotFoundException e) {
                }
            }
            if (overrideConfig != null) {
                if ((overrideConfig.equals(Configuration.EMPTY) ^ 1) != 0) {
                    Context overrideContext = context.createConfigurationContext(overrideConfig);
                    overrideContext.setTheme(theme);
                    TypedArray typedArray = overrideContext.obtainStyledAttributes(R.styleable.Window);
                    int resId = typedArray.getResourceId(1, 0);
                    if (!(resId == 0 || overrideContext.getDrawable(resId) == null)) {
                        context = overrideContext;
                    }
                    typedArray.recycle();
                }
            }
            PhoneWindow win = (PhoneWindow) HwPolicyFactory.getHwPhoneWindow(context);
            win.setIsStartingWindow(true);
            TypedArray ta = win.getWindowStyle();
            if (ta.getBoolean(12, false) || ta.getBoolean(14, false)) {
                if (!HISI_PERF_OPT) {
                    return null;
                }
                boolean accel = false;
                try {
                    accel = this.mWindowManager.getAccelPackages(packageName);
                } catch (RemoteException e2) {
                }
                Slog.i(TAG, "addStartingWindow pkgname " + packageName + ".accel " + accel);
                if (!accel || ta.getBoolean(14, false) || ta.getResourceId(1, 0) == 0) {
                    return null;
                }
            }
            CharSequence label = context.getResources().getText(labelRes, null);
            if (label != null) {
                win.setTitle(label, true);
            } else {
                win.setTitle(nonLocalizedLabel, false);
            }
            win.setType(3);
            synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
                if (this.mKeyguardOccluded) {
                    windowFlags |= DumpState.DUMP_FROZEN;
                }
            }
            if (HwWidgetFactory.isHwDarkTheme(context)) {
                windowFlags |= 134217728;
            }
            win.setFlags(((windowFlags | 16) | 8) | DumpState.DUMP_INTENT_FILTER_VERIFIERS, ((windowFlags | 16) | 8) | DumpState.DUMP_INTENT_FILTER_VERIFIERS);
            win.setDefaultIcon(icon);
            win.setDefaultLogo(logo);
            win.setLayout(-1, -1);
            LayoutParams params = win.getAttributes();
            params.token = appToken;
            params.packageName = packageName;
            params.windowAnimations = win.getWindowStyle().getResourceId(8, 0);
            params.privateFlags |= 1;
            params.privateFlags |= 16;
            if (!compatInfo.supportsScreen()) {
                params.privateFlags |= 128;
            }
            params.setTitle("Splash Screen " + packageName);
            addSplashscreenContent(win, context);
            WindowManager wm = (WindowManager) context.getSystemService("window");
            view = win.getDecorView();
            Flog.i(301, "addStartingWindow " + packageName + ": nonLocalizedLabel=" + nonLocalizedLabel + " theme=" + Integer.toHexString(theme) + " windowFlags=" + Integer.toHexString(windowFlags) + " isFloating=" + win.isFloating() + " appToken=" + appToken);
            setHasAcitionBar(win.hasFeature(8));
            wm.addView(view, params);
            if (view.getParent() != null) {
                SplashScreenSurface splashScreenSurface = new SplashScreenSurface(view, appToken);
            } else {
                startingSurface = null;
            }
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                wm.removeViewImmediate(view);
            }
            return startingSurface;
        } catch (BadTokenException e3) {
            Log.w(TAG, appToken + " already running, starting window not displayed. " + e3.getMessage());
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                null.removeViewImmediate(view);
            }
        } catch (RuntimeException e4) {
            Log.w(TAG, appToken + " failed creating starting window", e4);
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                null.removeViewImmediate(view);
            }
        } catch (Throwable th) {
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                null.removeViewImmediate(view);
            }
        }
    }

    private void addSplashscreenContent(PhoneWindow win, Context ctx) {
        TypedArray a = ctx.obtainStyledAttributes(R.styleable.Window);
        int resId = a.getResourceId(48, 0);
        a.recycle();
        if (resId != 0) {
            Drawable drawable = ctx.getDrawable(resId);
            if (drawable != null) {
                View v = new View(ctx);
                v.setBackground(drawable);
                win.setContentView(v);
            }
        }
    }

    private Context getDisplayContext(Context context, int displayId) {
        if (displayId == 0) {
            return context;
        }
        Display targetDisplay = ((DisplayManager) context.getSystemService("display")).getDisplay(displayId);
        if (targetDisplay == null) {
            return null;
        }
        return context.createDisplayContext(targetDisplay);
    }

    public int prepareAddWindowLw(WindowState win, LayoutParams attrs) {
        switch (attrs.type) {
            case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /*2000*/:
                this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                if (this.mStatusBar == null || !this.mStatusBar.isAlive()) {
                    this.mStatusBar = win;
                    this.mStatusBarController.setWindow(win);
                    setKeyguardOccludedLw(this.mKeyguardOccluded, true);
                    break;
                }
                return -7;
            case 2014:
            case 2017:
            case 2024:
            case 2033:
                this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                break;
            case 2019:
                this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                if (this.mNavigationBar == null || !this.mNavigationBar.isAlive()) {
                    this.mNavigationBar = win;
                    this.mNavigationBarController.setWindow(win);
                    this.mNavigationBarController.setOnBarVisibilityChangedListener(this.mNavBarVisibilityListener, true);
                    break;
                }
                return -7;
        }
        return 0;
    }

    public void removeWindowLw(WindowState win) {
        if (this.mStatusBar == win) {
            this.mStatusBar = null;
            this.mStatusBarController.setWindow(null);
        } else if (this.mNavigationBar == win) {
            this.mNavigationBar = null;
            this.mNavigationBarController.setWindow(null);
        }
    }

    public int selectAnimationLw(WindowState win, int transit) {
        if (win == this.mStatusBar) {
            boolean isKeyguard = (win.getAttrs().privateFlags & 1024) != 0;
            boolean expanded = win.getAttrs().height == -1 ? win.getAttrs().width == -1 : false;
            if (isKeyguard || expanded) {
                return -1;
            }
            if (transit == 2 || transit == 4) {
                return 17432619;
            }
            if (transit == 1 || transit == 3) {
                return 17432618;
            }
        } else if (win == this.mNavigationBar) {
            if (win.getAttrs().windowAnimations != 0) {
                return 0;
            }
            if (this.mNavigationBarPosition == 0) {
                if (transit == 2 || transit == 4) {
                    if (isKeyguardShowingAndNotOccluded()) {
                        return 17432613;
                    }
                    return 17432612;
                } else if (transit == 1 || transit == 3) {
                    return 17432611;
                }
            } else if (this.mNavigationBarPosition == 1) {
                if (transit == 2 || transit == 4) {
                    if (IS_NOTCH_PROP && this.mDisplayRotation == 3) {
                        return 0;
                    }
                    return 17432617;
                } else if (transit == 1 || transit == 3) {
                    return 17432616;
                }
            } else if (this.mNavigationBarPosition == 2) {
                if (transit == 2 || transit == 4) {
                    return 17432615;
                }
                if (transit == 1 || transit == 3) {
                    return 17432614;
                }
            }
        } else if (win.getAttrs().type == 2034) {
            if ((win.getAttrs().flags & 536870912) != 0) {
                return selectDockedDividerAnimationLw(win, transit);
            }
            return 0;
        }
        if (transit == 5) {
            if (win.hasAppShownWindows()) {
                return 17432593;
            }
        } else if (win.getAttrs().type == 2023 && this.mDreamingLockscreen && transit == 1) {
            return -1;
        } else {
            if (this.mCust != null && this.mCust.isChargingAlbumSupported() && win.getAttrs().type == 2102 && this.mDreamingLockscreen) {
                return this.mCust.selectAnimationLw(transit);
            }
        }
        return 0;
    }

    private int selectDockedDividerAnimationLw(WindowState win, int transit) {
        int insets = this.mWindowManagerFuncs.getDockedDividerInsetsLw();
        Rect frame = win.getFrameLw();
        boolean behindNavBar = this.mNavigationBar != null ? ((this.mNavigationBarPosition != 0 || frame.top + insets < this.mNavigationBar.getFrameLw().top) && (this.mNavigationBarPosition != 1 || frame.left + insets < this.mNavigationBar.getFrameLw().left)) ? this.mNavigationBarPosition == 2 ? frame.right - insets <= this.mNavigationBar.getFrameLw().right : false : true : false;
        boolean landscape = frame.height() > frame.width();
        boolean offscreenLandscape = landscape ? frame.right - insets > 0 ? frame.left + insets >= win.getDisplayFrameLw().right : true : false;
        boolean offscreenPortrait = !landscape ? frame.top - insets > 0 ? frame.bottom + insets >= win.getDisplayFrameLw().bottom : true : false;
        boolean offscreen = !offscreenLandscape ? offscreenPortrait : true;
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
        if (this.mTopFullscreenOpaqueWindowState != null) {
            int animationHint = this.mTopFullscreenOpaqueWindowState.getRotationAnimationHint();
            if (animationHint < 0 && this.mTopIsFullscreen) {
                animationHint = this.mTopFullscreenOpaqueWindowState.getAttrs().rotationAnimation;
            }
            switch (animationHint) {
                case 1:
                case 3:
                    anim[0] = 17432687;
                    anim[1] = 17432685;
                    return;
                case 2:
                    anim[0] = 17432686;
                    anim[1] = 17432685;
                    return;
                default:
                    anim[1] = 0;
                    anim[0] = 0;
                    return;
            }
        }
        anim[1] = 0;
        anim[0] = 0;
    }

    public boolean validateRotationAnimationLw(int exitAnimId, int enterAnimId, boolean forceDefault) {
        boolean z = true;
        switch (exitAnimId) {
            case 17432686:
            case 17432687:
                if (forceDefault) {
                    return false;
                }
                int[] anim = new int[2];
                selectRotationAnimationLw(anim);
                if (!(exitAnimId == anim[0] && enterAnimId == anim[1])) {
                    z = false;
                }
                return z;
            default:
                return true;
        }
    }

    public Animation createHiddenByKeyguardExit(boolean onWallpaper, boolean goingToNotificationShade) {
        if (goingToNotificationShade) {
            return AnimationUtils.loadAnimation(this.mContext, 17432661);
        }
        int i;
        Context context = this.mContext;
        if (onWallpaper) {
            i = 17432662;
        } else {
            i = 17432660;
        }
        return (AnimationSet) AnimationUtils.loadAnimation(context, i);
    }

    public Animation createKeyguardWallpaperExit(boolean goingToNotificationShade) {
        if (goingToNotificationShade) {
            return null;
        }
        return AnimationUtils.loadAnimation(this.mContext, 17432666);
    }

    private static void awakenDreams() {
        IDreamManager dreamManager = getDreamManager();
        if (dreamManager != null) {
            try {
                dreamManager.awaken();
            } catch (RemoteException e) {
            }
        }
    }

    static IDreamManager getDreamManager() {
        return IDreamManager.Stub.asInterface(ServiceManager.checkService("dreams"));
    }

    TelecomManager getTelecommService() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }

    static IAudioService getAudioService() {
        IAudioService audioService = IAudioService.Stub.asInterface(ServiceManager.checkService("audio"));
        if (audioService == null) {
            Log.w(TAG, "Unable to find IAudioService interface.");
        }
        return audioService;
    }

    boolean keyguardOn() {
        return !isKeyguardShowingAndNotOccluded() ? inKeyguardRestrictedKeyInputMode() : true;
    }

    public long interceptKeyBeforeDispatching(WindowState win, KeyEvent event, int policyFlags) {
        long now;
        long timeoutTime;
        boolean keyguardOn = keyguardOn();
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int metaState = event.getMetaState();
        int flags = event.getFlags();
        boolean down = event.getAction() == 0;
        boolean canceled = event.isCanceled();
        if (HWFLOW) {
            Log.d(TAG, "interceptKeyTi keyCode=" + keyCode + " down=" + down + " repeatCount=" + repeatCount + " keyguardOn=" + keyguardOn + " mHomePressed=" + this.mHomePressed + " canceled=" + canceled);
        }
        if (this.mScreenshotChordEnabled && (flags & 1024) == 0) {
            if (this.mScreenshotChordVolumeDownKeyTriggered && (this.mScreenshotChordPowerKeyTriggered ^ 1) != 0) {
                now = SystemClock.uptimeMillis();
                timeoutTime = this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
            if (keyCode == 25 && this.mScreenshotChordVolumeDownKeyConsumed) {
                if (!down) {
                    this.mScreenshotChordVolumeDownKeyConsumed = false;
                }
                return -1;
            }
        }
        if (this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(false) && (flags & 1024) == 0) {
            if ((this.mScreenshotChordVolumeDownKeyTriggered ^ this.mA11yShortcutChordVolumeUpKeyTriggered) != 0) {
                now = SystemClock.uptimeMillis();
                timeoutTime = (this.mScreenshotChordVolumeDownKeyTriggered ? this.mScreenshotChordVolumeDownKeyTime : this.mA11yShortcutChordVolumeUpKeyTime) + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
            if (keyCode == 25 && this.mScreenshotChordVolumeDownKeyConsumed) {
                if (!down) {
                    this.mScreenshotChordVolumeDownKeyConsumed = false;
                }
                return -1;
            } else if (keyCode == 24 && this.mA11yShortcutChordVolumeUpKeyConsumed) {
                if (!down) {
                    this.mA11yShortcutChordVolumeUpKeyConsumed = false;
                }
                return -1;
            }
        }
        HwFrameworkFactory.getHwNsdImpl().StopSdrForSpecial("interceptKeyBeforeDispatching", keyCode);
        if (this.mPendingMetaAction && (KeyEvent.isMetaKey(keyCode) ^ 1) != 0) {
            this.mPendingMetaAction = false;
        }
        if (!(!this.mPendingCapsLockToggle || (KeyEvent.isMetaKey(keyCode) ^ 1) == 0 || (KeyEvent.isAltKey(keyCode) ^ 1) == 0)) {
            this.mPendingCapsLockToggle = false;
        }
        int type;
        if (keyCode != 3) {
            if (keyCode == 82) {
                if (down && repeatCount == 0 && this.mEnableShiftMenuBugReports && (metaState & 1) == 1) {
                    this.mContext.sendOrderedBroadcastAsUser(new Intent("android.intent.action.BUG_REPORT"), UserHandle.CURRENT, null, null, null, 0, null, null);
                    return -1;
                }
            } else if (keyCode == 84) {
                if (!down) {
                    this.mSearchKeyShortcutPending = false;
                    if (this.mConsumeSearchKeyUp) {
                        this.mConsumeSearchKeyUp = false;
                        return -1;
                    }
                } else if (repeatCount == 0) {
                    this.mSearchKeyShortcutPending = true;
                    this.mConsumeSearchKeyUp = false;
                }
                return 0;
            } else if (keyCode == 187) {
                if (!keyguardOn) {
                    if (down && repeatCount == 0) {
                        preloadRecentApps();
                    } else if (!down) {
                        toggleRecentApps();
                    }
                }
                return -1;
            } else if (keyCode == 42 && event.isMetaPressed()) {
                if (down) {
                    IStatusBarService service = getStatusBarService();
                    if (service != null) {
                        try {
                            service.expandNotificationsPanel();
                        } catch (RemoteException e) {
                        }
                    }
                }
            } else if (keyCode == 47 && event.isMetaPressed() && event.isCtrlPressed()) {
                if (down && repeatCount == 0) {
                    if (event.isShiftPressed()) {
                        type = 2;
                    } else {
                        type = 1;
                    }
                    this.mScreenshotRunnable.setScreenshotType(type);
                    this.mHandler.post(this.mScreenshotRunnable);
                    return -1;
                }
            } else if (keyCode == 76 && event.isMetaPressed()) {
                if (down && repeatCount == 0 && (isKeyguardLocked() ^ 1) != 0) {
                    toggleKeyboardShortcutsMenu(event.getDeviceId());
                }
            } else if (keyCode == 219) {
                if (down) {
                    if (repeatCount == 0) {
                        this.mAssistKeyLongPressed = false;
                    } else if (repeatCount == 1) {
                        this.mAssistKeyLongPressed = true;
                        if (!keyguardOn) {
                            launchAssistLongPressAction();
                        }
                    }
                } else if (this.mAssistKeyLongPressed) {
                    this.mAssistKeyLongPressed = false;
                } else if (!keyguardOn) {
                    launchAssistAction(null, event.getDeviceId());
                }
                return -1;
            } else if (keyCode == 231) {
                if (!down) {
                    Intent intent;
                    if (keyguardOn) {
                        IDeviceIdleController dic = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
                        if (dic != null) {
                            try {
                                dic.exitIdle("voice-search");
                            } catch (RemoteException e2) {
                            }
                        }
                        intent = new Intent("android.speech.action.VOICE_SEARCH_HANDS_FREE");
                        intent.putExtra("android.speech.extras.EXTRA_SECURE", true);
                    } else {
                        intent = new Intent("android.speech.action.WEB_SEARCH");
                    }
                    startActivityAsUser(voiceIntent, UserHandle.CURRENT_OR_SELF);
                }
            } else if (keyCode == 120) {
                if (down && repeatCount == 0) {
                    this.mScreenshotRunnable.setScreenshotType(1);
                    this.mHandler.post(this.mScreenshotRunnable);
                }
                return -1;
            } else if (keyCode == 221 || keyCode == 220) {
                if (down) {
                    int direction = keyCode == 221 ? 1 : -1;
                    if (System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -3) != 0) {
                        System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -3);
                    }
                    int min = this.mPowerManager.getMinimumScreenBrightnessSetting();
                    int max = this.mPowerManager.getMaximumScreenBrightnessSetting();
                    System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness", Math.max(min, Math.min(max, System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", this.mPowerManager.getDefaultScreenBrightnessSetting(), -3) + (((((max - min) + 10) - 1) / 10) * direction))), -3);
                    startActivityAsUser(new Intent("com.android.intent.action.SHOW_BRIGHTNESS_DIALOG"), UserHandle.CURRENT_OR_SELF);
                }
                return -1;
            } else if (keyCode == 24 || keyCode == 25 || keyCode == 164) {
                if (this.mUseTvRouting || this.mHandleVolumeKeysInWM) {
                    dispatchDirectAudioEvent(event);
                    return -1;
                } else if (this.mPersistentVrModeEnabled) {
                    return -1;
                }
            } else if (keyCode == 61 && event.isMetaPressed()) {
                return 0;
            } else {
                if (this.mHasFeatureLeanback && interceptBugreportGestureTv(keyCode, down)) {
                    return -1;
                }
                if (this.mHasFeatureLeanback && keyCode == 23) {
                    this.mAccessibilityTvKey1Pressed = down;
                    if (interceptAccessibilityGestureTv()) {
                        return -1;
                    }
                }
            }
            boolean actionTriggered = false;
            if (KeyEvent.isModifierKey(keyCode)) {
                if (!this.mPendingCapsLockToggle) {
                    this.mInitialMetaState = this.mMetaState;
                    this.mPendingCapsLockToggle = true;
                } else if (event.getAction() == 1) {
                    int altOnMask = this.mMetaState & 50;
                    int metaOnMask = this.mMetaState & 458752;
                    if (!(metaOnMask == 0 || altOnMask == 0 || this.mInitialMetaState != (this.mMetaState ^ (altOnMask | metaOnMask)))) {
                        this.mInputManagerInternal.toggleCapsLock(event.getDeviceId());
                        actionTriggered = true;
                    }
                    this.mPendingCapsLockToggle = false;
                }
            }
            this.mMetaState = metaState;
            if (actionTriggered) {
                return -1;
            }
            if (KeyEvent.isMetaKey(keyCode)) {
                if (down) {
                    this.mPendingMetaAction = true;
                } else if (this.mPendingMetaAction) {
                    launchAssistAction("android.intent.extra.ASSIST_INPUT_HINT_KEYBOARD", event.getDeviceId());
                }
                return -1;
            }
            KeyCharacterMap kcm;
            Intent shortcutIntent;
            if (this.mSearchKeyShortcutPending) {
                kcm = event.getKeyCharacterMap();
                if (kcm.isPrintingKey(keyCode)) {
                    this.mConsumeSearchKeyUp = true;
                    this.mSearchKeyShortcutPending = false;
                    if (down && repeatCount == 0 && (keyguardOn ^ 1) != 0) {
                        shortcutIntent = this.mShortcutManager.getIntent(kcm, keyCode, metaState);
                        if (shortcutIntent != null) {
                            shortcutIntent.addFlags(268435456);
                            try {
                                startActivityAsUser(shortcutIntent, UserHandle.CURRENT);
                                dismissKeyboardShortcutsMenu();
                            } catch (Throwable ex) {
                                Slog.w(TAG, "Dropping shortcut key combination because the activity to which it is registered was not found: SEARCH+" + KeyEvent.keyCodeToString(keyCode), ex);
                            }
                        } else {
                            Slog.i(TAG, "Dropping unregistered shortcut key combination: SEARCH+" + KeyEvent.keyCodeToString(keyCode));
                        }
                    }
                    return -1;
                }
            }
            if (down && repeatCount == 0 && (keyguardOn ^ 1) != 0 && (65536 & metaState) != 0) {
                kcm = event.getKeyCharacterMap();
                if (kcm.isPrintingKey(keyCode)) {
                    shortcutIntent = this.mShortcutManager.getIntent(kcm, keyCode, -458753 & metaState);
                    if (shortcutIntent != null) {
                        shortcutIntent.addFlags(268435456);
                        try {
                            startActivityAsUser(shortcutIntent, UserHandle.CURRENT);
                            dismissKeyboardShortcutsMenu();
                        } catch (Throwable ex2) {
                            Slog.w(TAG, "Dropping shortcut key combination because the activity to which it is registered was not found: META+" + KeyEvent.keyCodeToString(keyCode), ex2);
                        }
                        return -1;
                    }
                }
            }
            if (down && repeatCount == 0 && (keyguardOn ^ 1) != 0) {
                String category = (String) sApplicationLaunchKeyCategories.get(keyCode);
                if (category != null) {
                    Intent intent2 = Intent.makeMainSelectorActivity("android.intent.action.MAIN", category);
                    intent2.setFlags(268435456);
                    if (ActivityManager.isUserAMonkey()) {
                        intent2.addFlags(DumpState.DUMP_COMPILER_STATS);
                    }
                    try {
                        startActivityAsUser(intent2, UserHandle.CURRENT);
                        dismissKeyboardShortcutsMenu();
                    } catch (Throwable ex22) {
                        Slog.w(TAG, "Dropping application launch key because the activity to which it is registered was not found: keyCode=" + keyCode + ", category=" + category, ex22);
                    }
                    return -1;
                }
            }
            if (down && repeatCount == 0 && keyCode == 61) {
                if (this.mRecentAppsHeldModifiers == 0 && (keyguardOn ^ 1) != 0 && isUserSetupComplete()) {
                    int shiftlessModifiers = event.getModifiers() & -194;
                    if (KeyEvent.metaStateHasModifiers(shiftlessModifiers, 2)) {
                        this.mRecentAppsHeldModifiers = shiftlessModifiers;
                        showRecentApps(true, false);
                        return -1;
                    }
                }
            } else if (!(down || this.mRecentAppsHeldModifiers == 0 || (this.mRecentAppsHeldModifiers & metaState) != 0)) {
                this.mRecentAppsHeldModifiers = 0;
                hideRecentApps(true, false);
            }
            if (down && repeatCount == 0 && (keyCode == 204 || (keyCode == 62 && (458752 & metaState) != 0))) {
                this.mWindowManagerFuncs.switchInputMethod((metaState & HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS) == 0);
                return -1;
            } else if (this.mLanguageSwitchKeyPressed && (down ^ 1) != 0 && (keyCode == 204 || keyCode == 62)) {
                this.mLanguageSwitchKeyPressed = false;
                return -1;
            } else if (isValidGlobalKey(keyCode) && this.mGlobalKeyManager.handleGlobalKey(this.mContext, keyCode, event)) {
                return -1;
            } else {
                if (down) {
                    long shortcutCode = (long) keyCode;
                    if (event.isCtrlPressed()) {
                        shortcutCode |= 17592186044416L;
                    }
                    if (event.isAltPressed()) {
                        shortcutCode |= 8589934592L;
                    }
                    if (event.isShiftPressed()) {
                        shortcutCode |= 4294967296L;
                    }
                    if (event.isMetaPressed()) {
                        shortcutCode |= 281474976710656L;
                    }
                    IShortcutService shortcutService = (IShortcutService) this.mShortcutKeyServices.get(shortcutCode);
                    if (shortcutService != null) {
                        try {
                            if (isUserSetupComplete()) {
                                shortcutService.notifyShortcutKeyPressed(shortcutCode);
                            }
                        } catch (RemoteException e3) {
                            this.mShortcutKeyServices.delete(shortcutCode);
                        }
                        return -1;
                    }
                }
                if ((65536 & metaState) != 0) {
                    return -1;
                }
                return 0;
            }
        } else if (down) {
            LayoutParams attrs = win != null ? win.getAttrs() : null;
            if (attrs != null) {
                type = attrs.type;
                if (type == 2009 || (attrs.privateFlags & 1024) != 0) {
                    return 0;
                }
                for (int i : WINDOW_TYPES_WHERE_HOME_DOESNT_WORK) {
                    if (type == i) {
                        return -1;
                    }
                }
            }
            if (repeatCount == 0) {
                this.mHomePressed = true;
                if (this.mHomeDoubleTapPending) {
                    this.mHomeDoubleTapPending = false;
                    this.mHandler.removeCallbacks(this.mHomeDoubleTapTimeoutRunnable);
                    handleDoubleTapOnHome();
                } else if (this.mDoubleTapOnHomeBehavior == 1) {
                    preloadRecentApps();
                }
            } else if ((event.getFlags() & 128) != 0) {
                if (this.mHasFeatureLeanback) {
                    this.mAccessibilityTvKey2Pressed = down;
                    if (interceptAccessibilityGestureTv()) {
                        return -1;
                    }
                }
                if (!keyguardOn) {
                    handleLongPressOnHome(event.getDeviceId());
                }
            }
            return -1;
        } else {
            cancelPreloadRecentApps();
            if (this.mHasFeatureLeanback) {
                this.mAccessibilityTvKey2Pressed = down;
            }
            this.mHomePressed = false;
            if (this.mHomeConsumed) {
                this.mHomeConsumed = false;
                Log.w(TAG, "Ignoring HOME; event consumed");
                return -1;
            } else if (canceled) {
                Log.w(TAG, "Ignoring HOME; event canceled.");
                return -1;
            } else if (this.mDoubleTapOnHomeBehavior != 0) {
                this.mHandler.removeCallbacks(this.mHomeDoubleTapTimeoutRunnable);
                this.mHomeDoubleTapPending = true;
                this.mHandler.postDelayed(this.mHomeDoubleTapTimeoutRunnable, (long) ViewConfiguration.getDoubleTapTimeout());
                Log.w(TAG, "Ignoring HOME; doubleTap home");
                return -1;
            } else {
                handleShortPressOnHome();
                return -1;
            }
        }
    }

    private boolean interceptBugreportGestureTv(int keyCode, boolean down) {
        if (keyCode == 23) {
            this.mBugreportTvKey1Pressed = down;
        } else if (keyCode == 4) {
            this.mBugreportTvKey2Pressed = down;
        }
        if (this.mBugreportTvKey1Pressed && this.mBugreportTvKey2Pressed) {
            if (!this.mBugreportTvScheduled) {
                this.mBugreportTvScheduled = true;
                Message msg = Message.obtain(this.mHandler, 22);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, 1000);
            }
        } else if (this.mBugreportTvScheduled) {
            this.mHandler.removeMessages(22);
            this.mBugreportTvScheduled = false;
        }
        return this.mBugreportTvScheduled;
    }

    private boolean interceptAccessibilityGestureTv() {
        if (this.mAccessibilityTvKey1Pressed && this.mAccessibilityTvKey2Pressed) {
            if (!this.mAccessibilityTvScheduled) {
                this.mAccessibilityTvScheduled = true;
                Message msg = Message.obtain(this.mHandler, 23);
                msg.setAsynchronous(true);
                this.mHandler.sendMessage(msg);
            }
        } else if (this.mAccessibilityTvScheduled) {
            this.mHandler.removeMessages(23);
            this.mAccessibilityTvScheduled = false;
        }
        return this.mAccessibilityTvScheduled;
    }

    private void takeBugreport() {
        if ("1".equals(SystemProperties.get("ro.debuggable")) || Global.getInt(this.mContext.getContentResolver(), "development_settings_enabled", 0) == 1) {
            try {
                ActivityManager.getService().requestBugReport(1);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error taking bugreport", e);
            }
        }
    }

    public KeyEvent dispatchUnhandledKey(WindowState win, KeyEvent event, int policyFlags) {
        KeyEvent fallbackEvent = null;
        if ((event.getFlags() & 1024) == 0) {
            FallbackAction fallbackAction;
            KeyCharacterMap kcm = event.getKeyCharacterMap();
            int keyCode = event.getKeyCode();
            int metaState = event.getMetaState();
            boolean initialDown = event.getAction() == 0 ? event.getRepeatCount() == 0 : false;
            if (initialDown) {
                fallbackAction = kcm.getFallbackAction(keyCode, metaState);
            } else {
                fallbackAction = (FallbackAction) this.mFallbackActions.get(keyCode);
            }
            if (fallbackAction != null) {
                fallbackEvent = KeyEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), fallbackAction.keyCode, event.getRepeatCount(), fallbackAction.metaState, event.getDeviceId(), event.getScanCode(), event.getFlags() | 1024, event.getSource(), null);
                if (!interceptFallback(win, fallbackEvent, policyFlags)) {
                    fallbackEvent.recycle();
                    fallbackEvent = null;
                }
                if (initialDown) {
                    this.mFallbackActions.put(keyCode, fallbackAction);
                } else if (event.getAction() == 1) {
                    this.mFallbackActions.remove(keyCode);
                    fallbackAction.recycle();
                }
            }
        }
        return fallbackEvent;
    }

    private boolean interceptFallback(WindowState win, KeyEvent fallbackEvent, int policyFlags) {
        if ((interceptKeyBeforeQueueing(fallbackEvent, policyFlags) & 1) == 0 || interceptKeyBeforeDispatching(win, fallbackEvent, policyFlags) != 0) {
            return false;
        }
        return true;
    }

    public void registerShortcutKey(long shortcutCode, IShortcutService shortcutService) throws RemoteException {
        synchronized (this.mLock) {
            IShortcutService service = (IShortcutService) this.mShortcutKeyServices.get(shortcutCode);
            if (service == null || !service.asBinder().pingBinder()) {
                this.mShortcutKeyServices.put(shortcutCode, shortcutService);
            } else {
                throw new RemoteException("Key already exists.");
            }
        }
    }

    public void onKeyguardOccludedChangedLw(boolean occluded) {
        if (this.mKeyguardDelegate == null || !this.mKeyguardDelegate.isShowing()) {
            setKeyguardOccludedLw(occluded, false);
            return;
        }
        this.mPendingKeyguardOccluded = occluded;
        this.mKeyguardOccludedChanged = true;
    }

    private int handleStartTransitionForKeyguardLw(int transit, Animation anim) {
        if (this.mKeyguardOccludedChanged) {
            this.mKeyguardOccludedChanged = false;
            if (setKeyguardOccludedLw(this.mPendingKeyguardOccluded, false)) {
                return 5;
            }
        }
        if (AppTransition.isKeyguardGoingAwayTransit(transit)) {
            long startTime;
            long duration;
            if (anim != null) {
                startTime = SystemClock.uptimeMillis() + anim.getStartOffset();
            } else {
                startTime = SystemClock.uptimeMillis();
            }
            if (anim != null) {
                duration = anim.getDuration();
            } else {
                duration = 0;
            }
            startKeyguardExitAnimation(startTime, duration);
        }
        return 0;
    }

    private void launchAssistLongPressAction() {
        performHapticFeedbackLw(null, 0, false);
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_ASSIST);
        Intent intent = new Intent("android.intent.action.SEARCH_LONG_PRESS");
        intent.setFlags(268435456);
        try {
            SearchManager searchManager = getSearchManager();
            if (searchManager != null) {
                searchManager.stopSearch();
            }
            startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            Slog.w(TAG, "No activity to handle assist long press action.", e);
        }
    }

    protected void launchAssistAction() {
        launchAssistAction(null, Integer.MIN_VALUE);
    }

    private void launchAssistAction(String hint) {
        launchAssistAction(hint, Integer.MIN_VALUE);
    }

    protected void launchAssistAction(String hint, int deviceId) {
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_ASSIST);
        if (isUserSetupComplete()) {
            Bundle args = null;
            if (deviceId > Integer.MIN_VALUE) {
                args = new Bundle();
                args.putInt("android.intent.extra.ASSIST_INPUT_DEVICE_ID", deviceId);
            }
            if ((this.mContext.getResources().getConfiguration().uiMode & 15) == 4) {
                ((SearchManager) this.mContext.getSystemService("search")).launchLegacyAssist(hint, UserHandle.myUserId(), args);
            } else {
                if (hint != null) {
                    if (args == null) {
                        args = new Bundle();
                    }
                    args.putBoolean(hint, true);
                }
                StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
                if (statusbar != null) {
                    statusbar.startAssist(args);
                }
            }
        }
    }

    private void startActivityAsUser(Intent intent, UserHandle handle) {
        if (isUserSetupComplete()) {
            this.mContext.startActivityAsUser(intent, handle);
        } else {
            Slog.i(TAG, "Not starting activity because user setup is in progress: " + intent);
        }
    }

    private SearchManager getSearchManager() {
        if (this.mSearchManager == null) {
            this.mSearchManager = (SearchManager) this.mContext.getSystemService("search");
        }
        return this.mSearchManager;
    }

    protected void preloadRecentApps() {
        this.mPreloadedRecentApps = true;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.preloadRecentApps();
        }
    }

    protected void cancelPreloadRecentApps() {
        if (this.mPreloadedRecentApps) {
            this.mPreloadedRecentApps = false;
            StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
            if (statusbar != null) {
                statusbar.cancelPreloadRecentApps();
            }
        }
    }

    protected void toggleRecentApps() {
        this.mPreloadedRecentApps = false;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.toggleRecentApps();
        }
    }

    public void showRecentApps(boolean fromHome) {
        int i;
        this.mHandler.removeMessages(9);
        Handler handler = this.mHandler;
        if (fromHome) {
            i = 1;
        } else {
            i = 0;
        }
        handler.obtainMessage(9, i, 0).sendToTarget();
    }

    private void showRecentApps(boolean triggeredFromAltTab, boolean fromHome) {
        this.mPreloadedRecentApps = false;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showRecentApps(triggeredFromAltTab, fromHome);
        }
    }

    private void toggleKeyboardShortcutsMenu(int deviceId) {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.toggleKeyboardShortcutsMenu(deviceId);
        }
    }

    private void dismissKeyboardShortcutsMenu() {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.dismissKeyboardShortcutsMenu();
        }
    }

    private void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHome) {
        this.mPreloadedRecentApps = false;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.hideRecentApps(triggeredFromAltTab, triggeredFromHome);
        }
    }

    void launchHomeFromHotKey() {
        launchHomeFromHotKey(true, true);
    }

    void launchHomeFromHotKey(final boolean awakenFromDreams, boolean respectKeyguard) {
        if (respectKeyguard) {
            if (isKeyguardShowingAndNotOccluded()) {
                Log.w(TAG, "Ignoring HOME; keyguard is showing");
                return;
            } else if (!this.mKeyguardOccluded && this.mKeyguardDelegate.isInputRestricted()) {
                Slog.w(TAG, "launchHomeFromHotKey verify unlock before launching home");
                this.mKeyguardDelegate.verifyUnlock(new OnKeyguardExitResult() {
                    public void onKeyguardExitResult(boolean success) {
                        Slog.w(PhoneWindowManager.TAG, "launchHomeFromHotKey onKeyguardExitResult success ? " + success);
                        if (success) {
                            try {
                                ActivityManager.getService().stopAppSwitches();
                            } catch (RemoteException e) {
                            }
                            long origId = Binder.clearCallingIdentity();
                            try {
                                PhoneWindowManager.this.sendCloseSystemWindows(PhoneWindowManager.SYSTEM_DIALOG_REASON_HOME_KEY);
                                PhoneWindowManager.this.startDockOrHome(true, awakenFromDreams);
                            } finally {
                                Binder.restoreCallingIdentity(origId);
                            }
                        }
                    }
                });
                return;
            }
        }
        try {
            ActivityManager.getService().stopAppSwitches();
        } catch (RemoteException e) {
        }
        if (this.mRecentsVisible) {
            if (awakenFromDreams) {
                awakenDreams();
            }
            if (HWFLOW) {
                Log.i(TAG, "hideRecentApps from home");
            }
            hideRecentApps(false, true);
        } else {
            sendCloseSystemWindows(SYSTEM_DIALOG_REASON_HOME_KEY);
            startDockOrHome(true, awakenFromDreams);
        }
    }

    public void setRecentsVisibilityLw(boolean visible) {
        this.mRecentsVisible = visible;
    }

    public void setPipVisibilityLw(boolean visible) {
        this.mPictureInPictureVisible = visible;
    }

    public int adjustSystemUiVisibilityLw(int visibility) {
        this.mStatusBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mNavigationBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mResettingSystemUiFlags &= visibility;
        return ((~this.mResettingSystemUiFlags) & visibility) & (~this.mForceClearedSystemUiFlags);
    }

    public boolean getInsetHintLw(LayoutParams attrs, Rect taskBounds, int displayRotation, int displayWidth, int displayHeight, Rect outContentInsets, Rect outStableInsets, Rect outOutsets) {
        int fl = PolicyControl.getWindowFlags(null, attrs);
        int systemUiVisibility = PolicyControl.getSystemUiVisibility(null, attrs) | attrs.subtreeSystemUiVisibility;
        if (outOutsets != null ? shouldUseOutsets(attrs, fl) : false) {
            int outset = ScreenShapeHelper.getWindowOutsetBottomPx(this.mContext.getResources());
            if (outset > 0) {
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
        }
        if ((65792 & fl) == 65792) {
            int availRight;
            int availBottom;
            if (!canHideNavigationBar() || (systemUiVisibility & 512) == 0) {
                availRight = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                availBottom = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
            } else {
                availRight = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                availBottom = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
            }
            if ((systemUiVisibility & 256) != 0) {
                if ((fl & 1024) != 0) {
                    outContentInsets.set(this.mStableFullscreenLeft, this.mStableFullscreenTop, availRight - this.mStableFullscreenRight, availBottom - this.mStableFullscreenBottom);
                } else {
                    outContentInsets.set(this.mStableLeft, this.mStableTop, availRight - this.mStableRight, availBottom - this.mStableBottom);
                }
            } else if ((fl & 1024) != 0 || (33554432 & fl) != 0) {
                outContentInsets.setEmpty();
            } else if ((systemUiVisibility & 1028) == 0) {
                outContentInsets.set(this.mCurLeft, this.mCurTop, availRight - this.mCurRight, availBottom - this.mCurBottom);
            } else {
                outContentInsets.set(this.mCurLeft, this.mCurTop, availRight - this.mCurRight, availBottom - this.mCurBottom);
            }
            outStableInsets.set(this.mStableLeft, this.mStableTop, availRight - this.mStableRight, availBottom - this.mStableBottom);
            if (taskBounds != null) {
                calculateRelevantTaskInsets(taskBounds, outContentInsets, displayWidth, displayHeight);
                calculateRelevantTaskInsets(taskBounds, outStableInsets, displayWidth, displayHeight);
            }
            return this.mForceShowSystemBars;
        }
        outContentInsets.setEmpty();
        outStableInsets.setEmpty();
        return this.mForceShowSystemBars;
    }

    private void calculateRelevantTaskInsets(Rect taskBounds, Rect inOutInsets, int displayWidth, int displayHeight) {
        mTmpRect.set(0, 0, displayWidth, displayHeight);
        mTmpRect.inset(inOutInsets);
        mTmpRect.intersect(taskBounds);
        inOutInsets.set(mTmpRect.left - taskBounds.left, mTmpRect.top - taskBounds.top, taskBounds.right - mTmpRect.right, taskBounds.bottom - mTmpRect.bottom);
    }

    private boolean shouldUseOutsets(LayoutParams attrs, int fl) {
        return attrs.type == 2013 || (33555456 & fl) != 0;
    }

    public void beginLayoutLw(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation, int uiMode, int displayId) {
        beginLayoutLw(isDefaultDisplay, displayWidth, displayHeight, displayRotation, uiMode);
    }

    public void beginLayoutLw(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation, int uiMode) {
        int overscanLeft;
        int overscanTop;
        int overscanRight;
        int overscanBottom;
        this.mDisplayRotation = displayRotation;
        if (isDefaultDisplay) {
            switch (displayRotation) {
                case 1:
                    overscanLeft = this.mOverscanTop;
                    overscanTop = this.mOverscanRight;
                    overscanRight = this.mOverscanBottom;
                    overscanBottom = this.mOverscanLeft;
                    break;
                case 2:
                    overscanLeft = this.mOverscanRight;
                    overscanTop = this.mOverscanBottom;
                    overscanRight = this.mOverscanLeft;
                    overscanBottom = this.mOverscanTop;
                    break;
                case 3:
                    overscanLeft = this.mOverscanBottom;
                    overscanTop = this.mOverscanLeft;
                    overscanRight = this.mOverscanTop;
                    overscanBottom = this.mOverscanRight;
                    break;
                default:
                    overscanLeft = this.mOverscanLeft;
                    overscanTop = this.mOverscanTop;
                    overscanRight = this.mOverscanRight;
                    overscanBottom = this.mOverscanBottom;
                    break;
            }
        }
        overscanLeft = 0;
        overscanTop = 0;
        overscanRight = 0;
        overscanBottom = 0;
        this.mRestrictedOverscanScreenLeft = 0;
        this.mOverscanScreenLeft = 0;
        this.mRestrictedOverscanScreenTop = 0;
        this.mOverscanScreenTop = 0;
        this.mRestrictedOverscanScreenWidth = displayWidth;
        this.mOverscanScreenWidth = displayWidth;
        this.mRestrictedOverscanScreenHeight = displayHeight;
        this.mOverscanScreenHeight = displayHeight;
        this.mSystemLeft = 0;
        this.mSystemTop = 0;
        this.mSystemRight = displayWidth;
        this.mSystemBottom = displayHeight;
        this.mUnrestrictedScreenLeft = overscanLeft;
        this.mUnrestrictedScreenTop = overscanTop;
        this.mUnrestrictedScreenWidth = (displayWidth - overscanLeft) - overscanRight;
        this.mUnrestrictedScreenHeight = (displayHeight - overscanTop) - overscanBottom;
        this.mRestrictedScreenLeft = this.mUnrestrictedScreenLeft;
        this.mRestrictedScreenTop = this.mUnrestrictedScreenTop;
        int i = this.mUnrestrictedScreenWidth;
        this.mSystemGestures.screenWidth = i;
        this.mRestrictedScreenWidth = i;
        i = this.mUnrestrictedScreenHeight;
        this.mSystemGestures.screenHeight = i;
        this.mRestrictedScreenHeight = i;
        i = this.mUnrestrictedScreenLeft;
        this.mCurLeft = i;
        this.mStableFullscreenLeft = i;
        this.mStableLeft = i;
        this.mVoiceContentLeft = i;
        this.mContentLeft = i;
        this.mDockLeft = i;
        i = this.mUnrestrictedScreenTop;
        this.mCurTop = i;
        this.mStableFullscreenTop = i;
        this.mStableTop = i;
        this.mVoiceContentTop = i;
        this.mContentTop = i;
        this.mDockTop = i;
        i = displayWidth - overscanRight;
        this.mCurRight = i;
        this.mStableFullscreenRight = i;
        this.mStableRight = i;
        this.mVoiceContentRight = i;
        this.mContentRight = i;
        this.mDockRight = i;
        i = displayHeight - overscanBottom;
        this.mCurBottom = i;
        this.mStableFullscreenBottom = i;
        this.mStableBottom = i;
        this.mVoiceContentBottom = i;
        this.mContentBottom = i;
        this.mDockBottom = i;
        this.mDockLayer = 268435456;
        this.mStatusBarLayer = -1;
        Rect pf = mTmpParentFrame;
        Rect df = mTmpDisplayFrame;
        Rect of = mTmpOverscanFrame;
        Rect vf = mTmpVisibleFrame;
        Rect dcf = mTmpDecorFrame;
        i = this.mDockLeft;
        vf.left = i;
        of.left = i;
        df.left = i;
        pf.left = i;
        i = this.mDockTop;
        vf.top = i;
        of.top = i;
        df.top = i;
        pf.top = i;
        i = this.mDockRight;
        vf.right = i;
        of.right = i;
        df.right = i;
        pf.right = i;
        i = this.mDockBottom;
        vf.bottom = i;
        of.bottom = i;
        df.bottom = i;
        pf.bottom = i;
        dcf.setEmpty();
        if (IS_NOTCH_PROP) {
            this.mNotchPropSize = this.mContext.getResources().getDimensionPixelSize(17105234);
        }
        if (isDefaultDisplay) {
            int sysui = this.mLastSystemUiFlags;
            boolean navVisible = (sysui & 2) == 0;
            boolean navTranslucent = (-2147450880 & sysui) != 0;
            boolean immersive = (sysui & 2048) != 0;
            boolean immersiveSticky = (sysui & 4096) != 0;
            LayoutParams focusAttrs = this.mFocusedWindow != null ? this.mFocusedWindow.getAttrs() : null;
            boolean isNeedHideNaviBarWin = focusAttrs != null ? (focusAttrs.privateFlags & Integer.MIN_VALUE) != 0 : false;
            boolean navAllowedHidden = (immersive || immersiveSticky) ? true : isNeedHideNaviBarWin;
            navTranslucent &= immersiveSticky ^ 1;
            boolean isKeyguardShowing = isStatusBarKeyguard() ? this.mKeyguardOccluded ^ 1 : false;
            if (!isKeyguardShowing) {
                navTranslucent &= areTranslucentBarsAllowed();
            }
            boolean statusBarExpandedNotKeyguard = (isKeyguardShowing || this.mStatusBar == null || (HwPolicyFactory.isHwGlobalActionsShowing() ^ 1) == 0 || this.mStatusBar.getAttrs().height != -1) ? false : this.mStatusBar.getAttrs().width == -1;
            if (navVisible || navAllowedHidden) {
                if (this.mInputConsumer != null) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(19, this.mInputConsumer));
                    this.mInputConsumer = null;
                }
            } else if (this.mInputConsumer == null) {
                this.mInputConsumer = this.mWindowManagerFuncs.createInputConsumer(this.mHandler.getLooper(), "nav_input_consumer", new -$Lambda$Nd7e3Murb8x7RqelLk3bI3c3rfY(this));
                InputManager.getInstance().setPointerIconType(0);
            }
            navVisible |= canHideNavigationBar() ^ 1;
            if (navVisible && mUsingHwNavibar) {
                navVisible = navVisible ? computeNaviBarFlag() ^ 1 : false;
            }
            String activityName = "com.google.android.gms/com.google.android.gms.auth.login.ShowErrorActivity";
            String windowName = null;
            if (this.mFocusedWindow != null) {
                windowName = this.mFocusedWindow.toString();
            }
            int type = focusAttrs != null ? focusAttrs.type : 0;
            boolean isKeyguardOn = ((type != 2000 || ((focusAttrs != null ? focusAttrs.privateFlags : 0) & 1024) == 0) && type != 2101) ? type == 2100 : true;
            if (windowName != null && windowName.contains(activityName) && Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0) {
                navVisible = true;
            }
            if (layoutNavigationBar(displayWidth, displayHeight, displayRotation, uiMode, overscanLeft, overscanRight, overscanBottom, dcf, navVisible, navTranslucent, navAllowedHidden, isKeyguardOn, statusBarExpandedNotKeyguard) | layoutStatusBar(pf, df, of, vf, dcf, sysui, isKeyguardShowing)) {
                updateSystemUiVisibilityLw();
            }
        }
    }

    /* synthetic */ InputEventReceiver lambda$-com_android_server_policy_PhoneWindowManager_235201(InputChannel channel, Looper looper) {
        return new HideNavInputEventReceiver(channel, looper);
    }

    private boolean layoutStatusBar(Rect pf, Rect df, Rect of, Rect vf, Rect dcf, int sysui, boolean isKeyguardShowing) {
        if (this.mStatusBar != null) {
            int i = this.mUnrestrictedScreenLeft;
            of.left = i;
            df.left = i;
            pf.left = i;
            i = this.mUnrestrictedScreenWidth + this.mUnrestrictedScreenLeft;
            of.right = i;
            df.right = i;
            pf.right = i;
            if (this.mStatusBar.getDisplayId() == 0) {
                layoutStatusBarForNotch(pf, df, of);
            }
            i = this.mUnrestrictedScreenTop;
            of.top = i;
            df.top = i;
            pf.top = i;
            i = this.mUnrestrictedScreenHeight + this.mUnrestrictedScreenTop;
            of.bottom = i;
            df.bottom = i;
            pf.bottom = i;
            vf.left = this.mStableLeft;
            vf.top = this.mStableTop;
            vf.right = this.mStableRight;
            vf.bottom = this.mStableBottom;
            this.mStatusBarLayer = this.mStatusBar.getSurfaceLayer();
            this.mStatusBar.computeFrameLw(pf, df, vf, vf, vf, dcf, vf, vf);
            this.mStableTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
            boolean statusBarTransient = (67108864 & sysui) != 0;
            int statusBarTranslucent = (1073741832 & sysui) != 0 ? 1 : 0;
            if (!isKeyguardShowing) {
                statusBarTranslucent &= areTranslucentBarsAllowed();
            }
            if (this.mStatusBar.isVisibleLw() && (statusBarTransient ^ 1) != 0) {
                this.mDockTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
                i = this.mDockTop;
                this.mCurTop = i;
                this.mVoiceContentTop = i;
                this.mContentTop = i;
                i = this.mDockBottom;
                this.mCurBottom = i;
                this.mVoiceContentBottom = i;
                this.mContentBottom = i;
                i = this.mDockLeft;
                this.mCurLeft = i;
                this.mVoiceContentLeft = i;
                this.mContentLeft = i;
                i = this.mDockRight;
                this.mCurRight = i;
                this.mVoiceContentRight = i;
                this.mContentRight = i;
            }
            if (!(!this.mStatusBar.isVisibleLw() || (this.mStatusBar.isAnimatingLw() ^ 1) == 0 || (statusBarTransient ^ 1) == 0 || (statusBarTranslucent ^ 1) == 0 || (this.mStatusBarController.wasRecentlyTranslucent() ^ 1) == 0)) {
                this.mSystemTop = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
            }
            if (this.mStatusBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private boolean layoutNavigationBar(int displayWidth, int displayHeight, int displayRotation, int uiMode, int overscanLeft, int overscanRight, int overscanBottom, Rect dcf, boolean navVisible, boolean navTranslucent, boolean navAllowedHidden, boolean isKeyguardOn, boolean statusBarExpandedNotKeyguard) {
        if (this.mNavigationBar != null) {
            int naviBarHeightForRotationMin;
            boolean transientNavBarShowing = this.mNavigationBarController.isTransientShowing();
            this.mNavigationBarPosition = navigationBarPosition(displayWidth, displayHeight, displayRotation);
            boolean z = this.mFocusedWindow != null ? (this.mFocusedWindow.getAttrs().hwFlags & 16384) != 0 : false;
            this.isMMITestDefaultShap = z;
            if (this.mNavigationBarPosition == 0) {
                int top = (displayHeight - overscanBottom) - getNavigationBarHeight(displayRotation, uiMode);
                if (IS_NOTCH_PROP && this.mNavigationBar.getDisplayId() == 0 && (this.isMMITestDefaultShap ^ 1) != 0 && this.mDisplayRotation == 2) {
                    mTmpNavigationFrame.set(0, top - this.mNotchPropSize, displayWidth, (((displayHeight - overscanBottom) + getNaviBarHeightForRotationMax(displayRotation)) - this.mNavigationBarHeightForRotationDefault[displayRotation]) - this.mNotchPropSize);
                } else {
                    mTmpNavigationFrame.set(0, top, displayWidth, ((displayHeight - overscanBottom) + getNaviBarHeightForRotationMax(displayRotation)) - this.mNavigationBarHeightForRotationDefault[displayRotation]);
                }
                if (isNaviBarMini()) {
                    naviBarHeightForRotationMin = displayHeight - getNaviBarHeightForRotationMin(displayRotation);
                    this.mStableFullscreenBottom = naviBarHeightForRotationMin;
                    this.mStableBottom = naviBarHeightForRotationMin;
                } else {
                    naviBarHeightForRotationMin = mTmpNavigationFrame.top;
                    this.mStableFullscreenBottom = naviBarHeightForRotationMin;
                    this.mStableBottom = naviBarHeightForRotationMin;
                }
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mLastNaviStatus = 2;
                    this.mLastTransientNaviDockBottom = this.mDockBottom;
                } else if (navVisible) {
                    if (this.mNavigationBarController.isTransientHiding()) {
                        Slog.v(TAG, "navigationbar is visible, but transientBarState is hiding, so reset a portrait screen");
                        this.mNavigationBarController.sethwTransientBarState(0);
                    }
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockBottom = this.mStableBottom;
                    this.mRestrictedScreenHeight = this.mDockBottom - this.mRestrictedScreenTop;
                    this.mRestrictedOverscanScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
                    this.mLastNaviStatus = 0;
                    this.mLastShowNaviDockBottom = this.mDockBottom;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                    if (isKeyguardOn) {
                        switch (this.mLastNaviStatus) {
                            case 0:
                                if (this.mLastShowNaviDockBottom != 0) {
                                    this.mDockBottom = this.mLastShowNaviDockBottom;
                                    this.mRestrictedScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
                                    this.mRestrictedOverscanScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
                                    break;
                                }
                                break;
                            case 1:
                                if (this.mLastHideNaviDockBottom != 0) {
                                    this.mDockBottom = this.mLastHideNaviDockBottom;
                                    this.mRestrictedScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
                                    this.mRestrictedOverscanScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
                                    break;
                                }
                                break;
                            case 2:
                                if (this.mLastTransientNaviDockBottom != 0) {
                                    this.mDockBottom = this.mLastTransientNaviDockBottom;
                                    this.mRestrictedScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
                                    this.mRestrictedOverscanScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
                                    break;
                                }
                                break;
                            default:
                                Slog.v(TAG, "keyguard mLastNaviStatus is init");
                                break;
                        }
                    }
                    this.mLastNaviStatus = 1;
                    this.mLastHideNaviDockBottom = this.mDockBottom;
                }
                if (!(!navVisible || (navTranslucent ^ 1) == 0 || (navAllowedHidden ^ 1) == 0 || (this.mNavigationBar.isAnimatingLw() ^ 1) == 0 || (this.mNavigationBarController.wasRecentlyTranslucent() ^ 1) == 0 || (mUsingHwNavibar ^ 1) == 0)) {
                    this.mSystemBottom = this.mStableBottom;
                }
            } else if (this.mNavigationBarPosition == 1) {
                boolean isShowLeftNavBar = getNavibarAlignLeftWhenLand();
                if (isShowLeftNavBar) {
                    mTmpNavigationFrame.set(0, 0, this.mContext.getResources().getDimensionPixelSize(34472115), displayHeight);
                } else {
                    int left = (displayWidth - overscanRight) - getNavigationBarWidth(displayRotation, uiMode);
                    if (this.mNavigationBar.getDisplayId() == 0 && IS_NOTCH_PROP && (this.isMMITestDefaultShap ^ 1) != 0 && this.mDisplayRotation == 3) {
                        if (this.mFocusedWindow != null && this.mFocusedWindow.toString().contains("com.google.android.youtube/com.google.android.apps.youtube.app.WatchWhileActivity")) {
                            int magin = this.mNotchPropSize - (displayWidth - (this.mFocusedWindow.getFrameLw().bottom * 2));
                            if (magin > 0 && magin < 10) {
                                this.mNotchPropSize -= magin;
                            }
                        }
                        mTmpNavigationFrame.set(left - this.mNotchPropSize, 0, (((displayWidth - overscanRight) + getNaviBarWidthForRotationMax(displayRotation)) - this.mNavigationBarWidthForRotationDefault[displayRotation]) - this.mNotchPropSize, displayHeight);
                    } else {
                        mTmpNavigationFrame.set(left, 0, ((displayWidth - overscanRight) + getNaviBarWidthForRotationMax(displayRotation)) - this.mNavigationBarWidthForRotationDefault[displayRotation], displayHeight);
                    }
                }
                if (isNaviBarMini()) {
                    naviBarHeightForRotationMin = displayWidth - getNaviBarWidthForRotationMin(displayRotation);
                    this.mStableFullscreenRight = naviBarHeightForRotationMin;
                    this.mStableRight = naviBarHeightForRotationMin;
                } else if (isShowLeftNavBar) {
                    naviBarHeightForRotationMin = mTmpNavigationFrame.right;
                    this.mStableFullscreenLeft = naviBarHeightForRotationMin;
                    this.mStableLeft = naviBarHeightForRotationMin;
                    this.mStableRight = displayWidth;
                } else {
                    naviBarHeightForRotationMin = mTmpNavigationFrame.left;
                    this.mStableFullscreenRight = naviBarHeightForRotationMin;
                    this.mStableRight = naviBarHeightForRotationMin;
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
                    this.mDockRight = this.mStableRight;
                    if (isShowLeftNavBar) {
                        naviBarHeightForRotationMin = this.mStableLeft;
                        this.mDockLeft = naviBarHeightForRotationMin;
                        this.mRestrictedScreenLeft = naviBarHeightForRotationMin;
                        this.mRestrictedScreenWidth = this.mStableRight;
                        this.mRestrictedOverscanScreenWidth = this.mStableRight;
                    } else {
                        this.mRestrictedScreenWidth = this.mDockRight - this.mRestrictedScreenLeft;
                        this.mRestrictedOverscanScreenWidth = this.mDockRight - this.mRestrictedOverscanScreenLeft;
                    }
                    this.mLastNaviStatus = 0;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                    this.mLastNaviStatus = 1;
                }
                if (!(!navVisible || (navTranslucent ^ 1) == 0 || (navAllowedHidden ^ 1) == 0 || (this.mNavigationBar.isAnimatingLw() ^ 1) == 0 || (this.mNavigationBarController.wasRecentlyTranslucent() ^ 1) == 0 || (mUsingHwNavibar ^ 1) == 0)) {
                    this.mSystemRight = this.mStableRight;
                }
            } else if (this.mNavigationBarPosition == 2) {
                mTmpNavigationFrame.set(overscanLeft, 0, overscanLeft + getNavigationBarWidth(displayRotation, uiMode), displayHeight);
                naviBarHeightForRotationMin = mTmpNavigationFrame.right;
                this.mStableFullscreenLeft = naviBarHeightForRotationMin;
                this.mStableLeft = naviBarHeightForRotationMin;
                if (transientNavBarShowing) {
                    this.mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    this.mNavigationBarController.setBarShowingLw(true);
                    this.mDockLeft = mTmpNavigationFrame.right;
                    naviBarHeightForRotationMin = this.mDockLeft;
                    this.mRestrictedOverscanScreenLeft = naviBarHeightForRotationMin;
                    this.mRestrictedScreenLeft = naviBarHeightForRotationMin;
                    this.mRestrictedScreenWidth = this.mDockRight - this.mRestrictedScreenLeft;
                    this.mRestrictedOverscanScreenWidth = this.mDockRight - this.mRestrictedOverscanScreenLeft;
                } else {
                    this.mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                }
                if (!(!navVisible || (navTranslucent ^ 1) == 0 || (navAllowedHidden ^ 1) == 0 || (this.mNavigationBar.isAnimatingLw() ^ 1) == 0 || (this.mNavigationBarController.wasRecentlyTranslucent() ^ 1) == 0)) {
                    this.mSystemLeft = mTmpNavigationFrame.right;
                }
            }
            naviBarHeightForRotationMin = this.mDockTop;
            this.mCurTop = naviBarHeightForRotationMin;
            this.mVoiceContentTop = naviBarHeightForRotationMin;
            this.mContentTop = naviBarHeightForRotationMin;
            naviBarHeightForRotationMin = this.mDockBottom;
            this.mCurBottom = naviBarHeightForRotationMin;
            this.mVoiceContentBottom = naviBarHeightForRotationMin;
            this.mContentBottom = naviBarHeightForRotationMin;
            naviBarHeightForRotationMin = this.mDockLeft;
            this.mCurLeft = naviBarHeightForRotationMin;
            this.mVoiceContentLeft = naviBarHeightForRotationMin;
            this.mContentLeft = naviBarHeightForRotationMin;
            naviBarHeightForRotationMin = this.mDockRight;
            this.mCurRight = naviBarHeightForRotationMin;
            this.mVoiceContentRight = naviBarHeightForRotationMin;
            this.mContentRight = naviBarHeightForRotationMin;
            this.mStatusBarLayer = this.mNavigationBar.getSurfaceLayer();
            this.mNavigationBar.computeFrameLw(mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, dcf, mTmpNavigationFrame, mTmpNavigationFrame);
            if (this.mNavigationBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private int navigationBarPosition(int displayWidth, int displayHeight, int displayRotation) {
        if (!this.mNavigationBarCanMove || displayWidth <= displayHeight) {
            return 0;
        }
        return displayRotation == 3 ? 1 : 1;
    }

    public int getSystemDecorLayerLw() {
        if (this.mStatusBar != null && this.mStatusBar.isVisibleLw()) {
            return this.mStatusBar.getSurfaceLayer();
        }
        if (this.mNavigationBar == null || !this.mNavigationBar.isVisibleLw()) {
            return 0;
        }
        return this.mNavigationBar.getSurfaceLayer();
    }

    public void getContentRectLw(Rect r) {
        r.set(this.mContentLeft, this.mContentTop, this.mContentRight, this.mContentBottom);
    }

    void setAttachedWindowFrames(WindowState win, int fl, int adjust, WindowState attached, boolean insetDecors, Rect pf, Rect df, Rect of, Rect cf, Rect vf) {
        if (win.getSurfaceLayer() <= this.mDockLayer || attached.getSurfaceLayer() >= this.mDockLayer) {
            Rect displayFrameLw;
            if (adjust != 16) {
                cf.set((1073741824 & fl) != 0 ? attached.getContentFrameLw() : attached.getOverscanFrameLw());
                if (attached.getAttrs().type == 2011 && cf.bottom > attached.getContentFrameLw().bottom) {
                    cf.bottom = attached.getContentFrameLw().bottom;
                }
            } else {
                cf.set(attached.getContentFrameLw());
                if (attached.isVoiceInteraction()) {
                    if (cf.left < this.mVoiceContentLeft) {
                        cf.left = this.mVoiceContentLeft;
                    }
                    if (cf.top < this.mVoiceContentTop) {
                        cf.top = this.mVoiceContentTop;
                    }
                    if (cf.right > this.mVoiceContentRight) {
                        cf.right = this.mVoiceContentRight;
                    }
                    if (cf.bottom > this.mVoiceContentBottom) {
                        cf.bottom = this.mVoiceContentBottom;
                    }
                } else if (attached.getSurfaceLayer() < this.mDockLayer) {
                    if (cf.left < this.mContentLeft) {
                        cf.left = this.mContentLeft;
                    }
                    if (cf.top < this.mContentTop) {
                        cf.top = this.mContentTop;
                    }
                    if (cf.right > this.mContentRight) {
                        cf.right = this.mContentRight;
                    }
                    if (cf.bottom > this.mContentBottom) {
                        cf.bottom = this.mContentBottom;
                    }
                }
            }
            if (insetDecors) {
                displayFrameLw = attached.getDisplayFrameLw();
            } else {
                displayFrameLw = cf;
            }
            df.set(displayFrameLw);
            if (insetDecors) {
                cf = attached.getOverscanFrameLw();
            }
            of.set(cf);
            vf.set(attached.getVisibleFrameLw());
        } else {
            int i = this.mDockLeft;
            vf.left = i;
            cf.left = i;
            of.left = i;
            df.left = i;
            i = this.mDockTop;
            vf.top = i;
            cf.top = i;
            of.top = i;
            df.top = i;
            i = this.mDockRight;
            vf.right = i;
            cf.right = i;
            of.right = i;
            df.right = i;
            i = this.mDockBottom;
            vf.bottom = i;
            cf.bottom = i;
            of.bottom = i;
            df.bottom = i;
        }
        if ((fl & 256) == 0) {
            df = attached.getFrameLw();
        }
        pf.set(df);
    }

    private void applyStableConstraints(int sysui, int fl, Rect r) {
        if ((sysui & 256) == 0) {
            return;
        }
        if ((fl & 1024) != 0) {
            if (r.left < this.mStableFullscreenLeft) {
                r.left = this.mStableFullscreenLeft;
            }
            if (r.top < this.mStableFullscreenTop) {
                r.top = this.mStableFullscreenTop;
            }
            if (r.right > this.mStableFullscreenRight) {
                r.right = this.mStableFullscreenRight;
            }
            if (r.bottom > this.mStableFullscreenBottom) {
                r.bottom = this.mStableFullscreenBottom;
                return;
            }
            return;
        }
        if (r.left < this.mStableLeft) {
            r.left = this.mStableLeft;
        }
        if (r.top < this.mStableTop) {
            r.top = this.mStableTop;
        }
        if (r.right > this.mStableRight) {
            r.right = this.mStableRight;
        }
        if (r.bottom > this.mStableBottom) {
            r.bottom = this.mStableBottom;
        }
    }

    private boolean canReceiveInput(WindowState win) {
        return (((win.getAttrs().flags & 8) != 0) ^ ((win.getAttrs().flags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0)) ^ 1;
    }

    /* JADX WARNING: Missing block: B:295:0x086e, code:
            if (getEmuiStyleValue(r23.isEmuiStyle) != 1) goto L_0x0870;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void layoutWindowLw(WindowState win, WindowState attached) {
        if ((win != this.mStatusBar || (canReceiveInput(win) ^ 1) == 0) && win != this.mNavigationBar) {
            int hasNavBar;
            int i;
            LayoutParams attrs = win.getAttrs();
            adjustWindowParamsLwForNotch(win, attrs);
            boolean isDefaultDisplay = win.isDefaultDisplay();
            boolean needsToOffsetInputMethodTarget = isDefaultDisplay ? win == this.mLastInputMethodTargetWindow && this.mLastInputMethodWindow != null : false;
            if (needsToOffsetInputMethodTarget) {
                offsetInputMethodWindowLw(this.mLastInputMethodWindow);
            }
            int fl = PolicyControl.getWindowFlags(win, attrs);
            int pfl = attrs.privateFlags;
            int sysUiFl = PolicyControl.getSystemUiVisibility(win, null);
            int sim = attrs.softInputMode;
            Rect pf = mTmpParentFrame;
            Rect df = mTmpDisplayFrame;
            Rect of = mTmpOverscanFrame;
            Rect cf = mTmpContentFrame;
            Rect vf = mTmpVisibleFrame;
            Rect dcf = mTmpDecorFrame;
            Rect sf = mTmpStableFrame;
            Rect osf = null;
            dcf.setEmpty();
            boolean isNeedHideNaviBarWin = mUsingHwNavibar && (attrs.privateFlags & Integer.MIN_VALUE) != 0;
            boolean isPCDisplay = false;
            if (HwPCUtils.isPcCastModeInServer()) {
                isPCDisplay = HwPCUtils.isValidExtDisplayId(win.getDisplayId());
            }
            if (HwPCUtils.isPcCastModeInServer() && (isDefaultDisplay ^ 1) != 0 && isPCDisplay) {
                isNeedHideNaviBarWin = false;
                hasNavBar = (!this.mHasNavigationBar || getNavigationBarExternal() == null) ? 0 : getNavigationBarExternal().isVisibleLw();
            } else {
                hasNavBar = (isDefaultDisplay && this.mHasNavigationBar && this.mNavigationBar != null) ? this.mNavigationBar.isVisibleLw() : 0;
            }
            int adjust = sim & 240;
            if (isDefaultDisplay) {
                sf.set(this.mStableLeft, this.mStableTop, this.mStableRight, this.mStableBottom);
            } else {
                sf.set(this.mOverscanLeft, this.mOverscanTop, this.mOverscanRight, this.mOverscanBottom);
            }
            if (HwPCUtils.isPcCastModeInServer() && isPCDisplay) {
                sf.set(this.mStableLeft, this.mStableTop, this.mStableRight, this.mStableBottom);
            }
            if (!isDefaultDisplay) {
                if (attached != null) {
                    setAttachedWindowFrames(win, fl, adjust, attached, true, pf, df, of, cf, vf);
                } else {
                    i = this.mOverscanScreenLeft;
                    cf.left = i;
                    of.left = i;
                    df.left = i;
                    pf.left = i;
                    i = this.mOverscanScreenTop;
                    cf.top = i;
                    of.top = i;
                    df.top = i;
                    pf.top = i;
                    i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                    cf.right = i;
                    of.right = i;
                    df.right = i;
                    pf.right = i;
                    i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                    cf.bottom = i;
                    of.bottom = i;
                    df.bottom = i;
                    pf.bottom = i;
                }
                if (HwPCUtils.isPcCastModeInServer() && isPCDisplay) {
                    if (attrs.type == 2011) {
                        i = this.mDockLeft;
                        vf.left = i;
                        cf.left = i;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mDockTop;
                        vf.top = i;
                        cf.top = i;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        i = this.mDockRight;
                        vf.right = i;
                        cf.right = i;
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                        i = this.mStableBottom;
                        vf.bottom = i;
                        cf.bottom = i;
                        attrs.gravity = 80;
                        this.mDockLayer = win.getSurfaceLayer();
                    } else {
                        if ((sysUiFl & 2) == 0 && (getNavigationBarExternal() == null || (getNavigationBarExternal().isVisibleLw() ^ 1) == 0)) {
                            cf.set(sf);
                        } else {
                            cf.set(df);
                        }
                        if (attrs.gravity == 80) {
                            attrs.gravity = 17;
                        }
                        layoutWindowForPadPCMode(win, pf, df, cf, vf, this.mContentBottom);
                    }
                }
            } else if (attrs.type == 2011) {
                i = this.mDockLeft;
                vf.left = i;
                cf.left = i;
                of.left = i;
                df.left = i;
                pf.left = i;
                i = this.mDockTop;
                vf.top = i;
                cf.top = i;
                of.top = i;
                df.top = i;
                pf.top = i;
                i = this.mDockRight;
                vf.right = i;
                cf.right = i;
                of.right = i;
                df.right = i;
                pf.right = i;
                if (mUsingHwNavibar) {
                    i = this.mDockBottom;
                    vf.bottom = i;
                    cf.bottom = i;
                    df.bottom = i;
                    pf.bottom = i;
                    LayoutParams focusAttrs = this.mFocusedWindow != null ? this.mFocusedWindow.getAttrs() : null;
                    if (!(focusAttrs == null || (focusAttrs.privateFlags & 1024) == 0 || (hasNavBar ^ 1) == 0)) {
                        i = this.mSystemBottom;
                        of.bottom = i;
                        vf.bottom = i;
                        cf.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                    }
                } else {
                    i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                    of.bottom = i;
                    df.bottom = i;
                    pf.bottom = i;
                    i = this.mStableBottom;
                    vf.bottom = i;
                    cf.bottom = i;
                    if (this.mStatusBar != null && this.mFocusedWindow == this.mStatusBar) {
                        if (canReceiveInput(this.mStatusBar)) {
                            if (this.mNavigationBarPosition == 1) {
                                i = this.mStableRight;
                                vf.right = i;
                                cf.right = i;
                                of.right = i;
                                df.right = i;
                                pf.right = i;
                            } else if (this.mNavigationBarPosition == 2) {
                                i = this.mStableLeft;
                                vf.left = i;
                                cf.left = i;
                                of.left = i;
                                df.left = i;
                                pf.left = i;
                            }
                        }
                    }
                }
                attrs.gravity = 80;
                this.mDockLayer = win.getSurfaceLayer();
            } else if (attrs.type == 2031) {
                i = this.mUnrestrictedScreenLeft;
                of.left = i;
                df.left = i;
                pf.left = i;
                i = this.mUnrestrictedScreenTop;
                of.top = i;
                df.top = i;
                pf.top = i;
                i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                of.right = i;
                df.right = i;
                pf.right = i;
                i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                of.bottom = i;
                df.bottom = i;
                pf.bottom = i;
                if (adjust != 16) {
                    cf.left = this.mDockLeft;
                    cf.top = this.mDockTop;
                    cf.right = this.mDockRight;
                    cf.bottom = this.mDockBottom;
                } else {
                    cf.left = this.mContentLeft;
                    cf.top = this.mContentTop;
                    cf.right = this.mContentRight;
                    cf.bottom = this.mContentBottom;
                }
                if (adjust != 48) {
                    vf.left = this.mCurLeft;
                    vf.top = this.mCurTop;
                    vf.right = this.mCurRight;
                    vf.bottom = this.mCurBottom;
                } else {
                    vf.set(cf);
                }
            } else if (attrs.type == 2013) {
                layoutWallpaper(win, pf, df, of, cf);
            } else if (win == this.mStatusBar) {
                i = this.mUnrestrictedScreenLeft;
                of.left = i;
                df.left = i;
                pf.left = i;
                i = this.mUnrestrictedScreenTop;
                of.top = i;
                df.top = i;
                pf.top = i;
                i = this.mUnrestrictedScreenWidth + this.mUnrestrictedScreenLeft;
                of.right = i;
                df.right = i;
                pf.right = i;
                i = this.mUnrestrictedScreenHeight + this.mUnrestrictedScreenTop;
                of.bottom = i;
                df.bottom = i;
                pf.bottom = i;
                i = this.mStableLeft;
                vf.left = i;
                cf.left = i;
                i = this.mStableTop;
                vf.top = i;
                cf.top = i;
                i = this.mStableRight;
                vf.right = i;
                cf.right = i;
                vf.bottom = this.mStableBottom;
                if (adjust == 16) {
                    cf.bottom = this.mContentBottom;
                } else {
                    cf.bottom = this.mDockBottom;
                    vf.bottom = this.mContentBottom;
                }
            } else {
                dcf.left = this.mSystemLeft;
                dcf.top = this.mSystemTop;
                dcf.right = this.mSystemRight;
                dcf.bottom = this.mSystemBottom;
                boolean inheritTranslucentDecor = (attrs.privateFlags & 512) != 0;
                boolean isAppWindow = attrs.type >= 1 ? attrs.type <= 99 : false;
                int topAtRest = win == this.mTopFullscreenOpaqueWindowState ? win.isAnimatingLw() ^ 1 : 0;
                if (!(!isAppWindow || (inheritTranslucentDecor ^ 1) == 0 || (topAtRest ^ 1) == 0)) {
                    if ((sysUiFl & 4) == 0 && (fl & 1024) == 0 && (67108864 & fl) == 0 && (Integer.MIN_VALUE & fl) == 0 && (DumpState.DUMP_INTENT_FILTER_VERIFIERS & pfl) == 0 && this.mStatusBar != null && this.mStatusBar.isVisibleLw() && (this.mLastSystemUiFlags & 67108864) == 0) {
                        dcf.top = this.mStableTop;
                    }
                    if ((134217728 & fl) == 0 && (sysUiFl & 2) == 0 && (isNeedHideNaviBarWin ^ 1) != 0 && (Integer.MIN_VALUE & fl) == 0) {
                        dcf.bottom = this.mStableBottom;
                        dcf.right = this.mStableRight;
                    }
                }
                if ((65792 & fl) != 65792) {
                    if ((fl & 256) == 0) {
                        if ((sysUiFl & 1536) != 0) {
                            if ((attrs.flags & Integer.MIN_VALUE) != 0) {
                            }
                        }
                        if (attached != null) {
                            setAttachedWindowFrames(win, fl, adjust, attached, false, pf, df, of, cf, vf);
                        } else if (attrs.type == 2014 || attrs.type == 2020) {
                            i = this.mRestrictedScreenLeft;
                            cf.left = i;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mRestrictedScreenTop;
                            cf.top = i;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                            cf.right = i;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                            cf.bottom = i;
                            of.bottom = i;
                            df.bottom = i;
                            pf.bottom = i;
                        } else if (attrs.type == 2005 || attrs.type == 2003) {
                            i = this.mStableLeft;
                            cf.left = i;
                            of.left = i;
                            df.left = i;
                            pf.left = i;
                            i = this.mStableTop;
                            cf.top = i;
                            of.top = i;
                            df.top = i;
                            pf.top = i;
                            i = this.mStableRight;
                            cf.right = i;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                            if (attrs.type == 2003) {
                                i = this.mCurBottom;
                                cf.bottom = i;
                                of.bottom = i;
                                df.bottom = i;
                                pf.bottom = i;
                            } else {
                                i = this.mStableBottom;
                                cf.bottom = i;
                                of.bottom = i;
                                df.bottom = i;
                                pf.bottom = i;
                            }
                        } else {
                            pf.left = this.mContentLeft;
                            pf.top = this.mContentTop;
                            pf.right = this.mContentRight;
                            pf.bottom = this.mContentBottom;
                            if (win.isVoiceInteraction()) {
                                i = this.mVoiceContentLeft;
                                cf.left = i;
                                of.left = i;
                                df.left = i;
                                i = this.mVoiceContentTop;
                                cf.top = i;
                                of.top = i;
                                df.top = i;
                                i = this.mVoiceContentRight;
                                cf.right = i;
                                of.right = i;
                                df.right = i;
                                i = this.mVoiceContentBottom;
                                cf.bottom = i;
                                of.bottom = i;
                                df.bottom = i;
                            } else if (adjust != 16) {
                                i = this.mDockLeft;
                                cf.left = i;
                                of.left = i;
                                df.left = i;
                                i = this.mDockTop;
                                cf.top = i;
                                of.top = i;
                                df.top = i;
                                i = this.mDockRight;
                                cf.right = i;
                                of.right = i;
                                df.right = i;
                                i = this.mDockBottom;
                                cf.bottom = i;
                                of.bottom = i;
                                df.bottom = i;
                            } else {
                                i = this.mContentLeft;
                                cf.left = i;
                                of.left = i;
                                df.left = i;
                                i = this.mContentTop;
                                cf.top = i;
                                of.top = i;
                                df.top = i;
                                i = this.mContentRight;
                                cf.right = i;
                                of.right = i;
                                df.right = i;
                                i = this.mContentBottom;
                                cf.bottom = i;
                                of.bottom = i;
                                df.bottom = i;
                            }
                            if (adjust != 48) {
                                vf.left = this.mCurLeft;
                                vf.top = this.mCurTop;
                                vf.right = this.mCurRight;
                                vf.bottom = this.mCurBottom;
                            } else {
                                vf.set(cf);
                            }
                        }
                    }
                    if (attrs.type == 2014 || attrs.type == 2017 || attrs.type == 2020) {
                        i = hasNavBar != 0 ? this.mDockLeft : this.mUnrestrictedScreenLeft;
                        cf.left = i;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mUnrestrictedScreenTop;
                        cf.top = i;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        if (hasNavBar != 0) {
                            i = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                        } else {
                            i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                        }
                        cf.right = i;
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        if (hasNavBar != 0) {
                            i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                        } else {
                            i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                        }
                        cf.bottom = i;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                    } else if (attrs.type == 2019 || attrs.type == 2024) {
                        i = this.mUnrestrictedScreenLeft;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mUnrestrictedScreenTop;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                    } else if ((attrs.type == 2015 || attrs.type == 2021 || attrs.type == 2036) && (fl & 1024) != 0) {
                        i = this.mOverscanScreenLeft;
                        cf.left = i;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mOverscanScreenTop;
                        cf.top = i;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                        cf.right = i;
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                        cf.bottom = i;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                    } else if (attrs.type == 2021) {
                        i = this.mOverscanScreenLeft;
                        cf.left = i;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mOverscanScreenTop;
                        cf.top = i;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                        cf.right = i;
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                        cf.bottom = i;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                    } else if ((33554432 & fl) != 0 && attrs.type >= 1 && attrs.type <= 1999) {
                        i = this.mOverscanScreenLeft;
                        cf.left = i;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mOverscanScreenTop;
                        cf.top = i;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                        cf.right = i;
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                        cf.bottom = i;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                    } else if ((canHideNavigationBar() && (sysUiFl & 512) != 0 && (attrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || attrs.type == 2005 || attrs.type == 2034 || attrs.type == 2033 || (attrs.type >= 1 && attrs.type <= 1999))) || ((mUsingHwNavibar && attrs.type == 2013) || isNeedHideNaviBarWin)) {
                        i = this.mUnrestrictedScreenLeft;
                        cf.left = i;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mUnrestrictedScreenTop;
                        cf.top = i;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                        cf.right = i;
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                        cf.bottom = i;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                    } else if ((sysUiFl & 1024) != 0) {
                        i = this.mRestrictedScreenLeft;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mRestrictedScreenTop;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        i = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                        if (adjust != 16) {
                            cf.left = this.mDockLeft;
                            cf.top = this.mDockTop;
                            cf.right = this.mDockRight;
                            cf.bottom = this.mDockBottom;
                        } else {
                            cf.left = this.mContentLeft;
                            cf.top = this.mContentTop;
                            cf.right = this.mContentRight;
                            cf.bottom = this.mContentBottom;
                        }
                    } else {
                        i = this.mRestrictedScreenLeft;
                        cf.left = i;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mRestrictedScreenTop;
                        cf.top = i;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        i = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                        cf.right = i;
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                        cf.bottom = i;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                    }
                    applyStableConstraints(sysUiFl, fl, cf);
                    if (adjust != 48) {
                        vf.left = this.mCurLeft;
                        vf.top = this.mCurTop;
                        vf.right = this.mCurRight;
                        vf.bottom = this.mCurBottom;
                    } else {
                        vf.set(cf);
                    }
                } else if (attached != null) {
                    setAttachedWindowFrames(win, fl, adjust, attached, true, pf, df, of, cf, vf);
                } else {
                    if (attrs.type == 2014 || attrs.type == 2017) {
                        i = hasNavBar != 0 ? this.mDockLeft : this.mUnrestrictedScreenLeft;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mUnrestrictedScreenTop;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        if (hasNavBar != 0) {
                            i = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                        } else {
                            i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                        }
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        if (hasNavBar != 0) {
                            i = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                        } else {
                            i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                        }
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                    } else if ((33554432 & fl) != 0 && attrs.type >= 1 && attrs.type <= 1999) {
                        i = this.mOverscanScreenLeft;
                        of.left = i;
                        df.left = i;
                        pf.left = i;
                        i = this.mOverscanScreenTop;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                        of.right = i;
                        df.right = i;
                        pf.right = i;
                        i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                    } else if ((!canHideNavigationBar() || (sysUiFl & 512) == 0 || attrs.type < 1 || attrs.type > 1999) && !isNeedHideNaviBarWin) {
                        i = this.mRestrictedOverscanScreenLeft;
                        df.left = i;
                        pf.left = i;
                        i = this.mRestrictedOverscanScreenTop;
                        df.top = i;
                        pf.top = i;
                        i = this.mRestrictedOverscanScreenLeft + this.mRestrictedOverscanScreenWidth;
                        df.right = i;
                        pf.right = i;
                        i = this.mRestrictedOverscanScreenTop + this.mRestrictedOverscanScreenHeight;
                        df.bottom = i;
                        pf.bottom = i;
                        of.left = this.mUnrestrictedScreenLeft;
                        of.top = this.mUnrestrictedScreenTop;
                        of.right = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                        of.bottom = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                    } else {
                        i = this.mOverscanScreenLeft;
                        df.left = i;
                        pf.left = i;
                        i = this.mOverscanScreenTop;
                        df.top = i;
                        pf.top = i;
                        i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
                        df.right = i;
                        pf.right = i;
                        i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
                        df.bottom = i;
                        pf.bottom = i;
                        of.left = this.mUnrestrictedScreenLeft;
                        of.top = this.mUnrestrictedScreenTop;
                        of.right = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
                        of.bottom = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                    }
                    if ((fl & 1024) == 0) {
                        if (win.isVoiceInteraction()) {
                            cf.left = this.mVoiceContentLeft;
                            cf.top = this.mVoiceContentTop;
                            cf.right = this.mVoiceContentRight;
                            cf.bottom = this.mVoiceContentBottom;
                        } else if (adjust != 16) {
                            cf.left = this.mDockLeft;
                            cf.top = this.mDockTop;
                            cf.right = this.mDockRight;
                            cf.bottom = this.mDockBottom;
                        } else {
                            cf.left = this.mContentLeft;
                            cf.top = this.mContentTop;
                            cf.right = this.mContentRight;
                            cf.bottom = this.mContentBottom;
                        }
                        synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
                            if (!win.hasDrawnLw()) {
                                cf.top = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
                            }
                        }
                        if ((attrs.flags & Integer.MIN_VALUE) == 0 && (attrs.flags & 67108864) == 0 && (sysUiFl & 3076) == 0 && attrs.type != 3) {
                            i = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
                            vf.top = i;
                            dcf.top = i;
                        }
                    } else {
                        cf.left = this.mRestrictedScreenLeft;
                        cf.top = this.mRestrictedScreenTop;
                        cf.right = this.mRestrictedScreenLeft + this.mRestrictedScreenWidth;
                        cf.bottom = this.mRestrictedScreenTop + this.mRestrictedScreenHeight;
                    }
                    if (isNeedHideNaviBarWin) {
                        cf.bottom = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
                    }
                    applyStableConstraints(sysUiFl, fl, cf);
                    if (adjust != 48) {
                        vf.left = this.mCurLeft;
                        vf.top = this.mCurTop;
                        vf.right = this.mCurRight;
                        vf.bottom = this.mCurBottom;
                    } else {
                        vf.set(cf);
                    }
                }
                if (win.getDisplayId() == 0) {
                    layoutWindowLwForNotch(win, attached, attrs, fl, pf, df, of, cf);
                }
            }
            overrideRectForForceRotation(win, pf, df, of, cf, vf, dcf);
            if ((isNeedHideNaviBarWin || (mUsingHwNavibar ^ 1) != 0) && attrs.type == 2004) {
                vf.top = 0;
                cf.top = 0;
                i = pf.bottom;
                vf.bottom = i;
                cf.bottom = i;
            }
            if (mUsingHwNavibar && (isNeedHideNaviBarWin || this.isNavibarHide)) {
                i = pf.bottom;
                vf.bottom = i;
                cf.bottom = i;
                dcf.bottom = i;
            }
            if (!((HwPCUtils.isPcCastModeInServer() && isPCDisplay) || (fl & 512) == 0 || attrs.type == 2010 || (win.isInMultiWindowMode() && !win.toString().contains("com.huawei.android.launcher")))) {
                df.top = -10000;
                df.left = -10000;
                df.bottom = 10000;
                df.right = 10000;
                if (attrs.type != 2013) {
                    vf.top = -10000;
                    vf.left = -10000;
                    cf.top = -10000;
                    cf.left = -10000;
                    of.top = -10000;
                    of.left = -10000;
                    vf.bottom = 10000;
                    vf.right = 10000;
                    cf.bottom = 10000;
                    cf.right = 10000;
                    of.bottom = 10000;
                    of.right = 10000;
                }
            }
            boolean useOutsets = shouldUseOutsets(attrs, fl);
            if (isDefaultDisplay && useOutsets) {
                osf = mTmpOutsetFrame;
                osf.set(cf.left, cf.top, cf.right, cf.bottom);
                int outset = ScreenShapeHelper.getWindowOutsetBottomPx(this.mContext.getResources());
                if (outset > 0) {
                    int rotation = this.mDisplayRotation;
                    if (rotation == 0) {
                        osf.bottom += outset;
                    } else if (rotation == 1) {
                        osf.right += outset;
                    } else if (rotation == 2) {
                        osf.top -= outset;
                    } else if (rotation == 3) {
                        osf.left -= outset;
                    }
                }
            }
            if (win.toString().contains("DividerMenusView")) {
                pf.top = this.mUnrestrictedScreenTop;
                pf.bottom = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
            }
            if (!(HwPCUtils.isPcCastModeInServer() && isPCDisplay)) {
                if (win.toString().contains("com.android.packageinstaller.permission.ui.GrantPermissionsActivity")) {
                    i = this.mUnrestrictedScreenTop + this.mStatusBarHeight;
                    vf.top = i;
                    cf.top = i;
                    of.top = i;
                    df.top = i;
                    pf.top = i;
                }
                if (win.getDisplayId() == 0) {
                    layoutWindowLwForNotch(win, attached, attrs, fl, pf, df, of, cf);
                }
            }
            win.computeFrameLw(pf, df, of, cf, vf, dcf, sf, osf);
            if (attrs.type == 2011 && win.isVisibleLw() && (win.getGivenInsetsPendingLw() ^ 1) != 0) {
                setLastInputMethodWindowLw(null, null);
                offsetInputMethodWindowLw(win);
            }
            if (attrs.type == 2031 && win.isVisibleLw() && (win.getGivenInsetsPendingLw() ^ 1) != 0) {
                offsetVoiceInputWindowLw(win);
            }
        }
    }

    private void layoutWallpaper(WindowState win, Rect pf, Rect df, Rect of, Rect cf) {
        int i = this.mOverscanScreenLeft;
        df.left = i;
        pf.left = i;
        i = this.mOverscanScreenTop;
        df.top = i;
        pf.top = i;
        i = this.mOverscanScreenLeft + this.mOverscanScreenWidth;
        df.right = i;
        pf.right = i;
        i = this.mOverscanScreenTop + this.mOverscanScreenHeight;
        df.bottom = i;
        pf.bottom = i;
        i = this.mUnrestrictedScreenLeft;
        cf.left = i;
        of.left = i;
        i = this.mUnrestrictedScreenTop;
        cf.top = i;
        of.top = i;
        i = this.mUnrestrictedScreenLeft + this.mUnrestrictedScreenWidth;
        cf.right = i;
        of.right = i;
        i = this.mUnrestrictedScreenTop + this.mUnrestrictedScreenHeight;
        cf.bottom = i;
        of.bottom = i;
    }

    private void offsetInputMethodWindowLw(WindowState win) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top) + win.getGivenContentInsetsLw().top;
        if (this.mContentBottom > top) {
            this.mContentBottom = top;
        }
        if (this.mVoiceContentBottom > top) {
            this.mVoiceContentBottom = top;
        }
        top = win.getVisibleFrameLw().top + win.getGivenVisibleInsetsLw().top;
        if (this.mCurBottom > top) {
            this.mCurBottom = top;
        }
    }

    private void offsetVoiceInputWindowLw(WindowState win) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top) + win.getGivenContentInsetsLw().top;
        if (this.mVoiceContentBottom > top) {
            this.mVoiceContentBottom = top;
        }
    }

    public void finishLayoutLw() {
    }

    public void beginPostLayoutPolicyLw(int displayWidth, int displayHeight) {
        this.mTopFullscreenOpaqueWindowState = null;
        this.mTopFullscreenOpaqueOrDimmingWindowState = null;
        this.mTopDockedOpaqueWindowState = null;
        this.mTopDockedOpaqueOrDimmingWindowState = null;
        this.mForceStatusBarTransparentWin = null;
        this.mForceStatusBar = false;
        this.mForceStatusBarFromKeyguard = false;
        this.mForceStatusBarTransparent = false;
        this.mForcingShowNavBar = false;
        this.mForcingShowNavBarLayer = -1;
        this.mAllowLockscreenWhenOn = false;
        this.mShowingDream = false;
        this.mHasCoverView = false;
    }

    public void applyPostLayoutPolicyLw(WindowState win, LayoutParams attrs, WindowState attached, WindowState imeTarget) {
        boolean affectsSystemUi = win.canAffectSystemUiFlags();
        applyKeyguardPolicyLw(win, imeTarget);
        int fl = PolicyControl.getWindowFlags(win, attrs);
        if (this.mTopFullscreenOpaqueWindowState == null && affectsSystemUi && attrs.type == 2011) {
            this.mForcingShowNavBar = true;
            this.mForcingShowNavBarLayer = win.getSurfaceLayer();
        }
        if (attrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
            if ((attrs.privateFlags & 1024) != 0) {
                this.mForceStatusBarFromKeyguard = true;
            }
            if ((attrs.privateFlags & 4096) != 0) {
                this.mForceStatusBarTransparent = true;
                this.mForceStatusBarTransparentWin = win;
            }
        }
        boolean appWindow = ((attrs.type < 1 || attrs.type >= IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) && attrs.type != 2100) ? attrs.type == 2101 : true;
        int stackId = win.getStackId();
        if (this.mTopFullscreenOpaqueWindowState == null && (attrs.type == 2100 || attrs.type == 2101)) {
            this.mHasCoverView = sProximityWndName.equals(attrs.getTitle()) ^ 1;
        } else if (this.mTopFullscreenOpaqueWindowState == null && affectsSystemUi) {
            if ((fl & 2048) != 0) {
                this.mForceStatusBar = true;
            }
            if ((attrs.type == 2023 || attrs.type == 2102) && (!this.mDreamingLockscreen || (win.isVisibleLw() && win.hasDrawnLw()))) {
                this.mShowingDream = true;
                appWindow = true;
            }
            if (appWindow && attached == null && isFullscreen(attrs) && StackId.normallyFullscreenWindows(stackId)) {
                if (!this.mHasCoverView || (isCoverWindow(win) ^ 1) == 0) {
                    this.mTopFullscreenOpaqueWindowState = win;
                } else {
                    Slog.i(TAG, "skip show window when cover added. Target: " + win);
                }
                if (this.mTopFullscreenOpaqueOrDimmingWindowState == null) {
                    this.mTopFullscreenOpaqueOrDimmingWindowState = win;
                }
                if ((fl & 1) != 0) {
                    this.mAllowLockscreenWhenOn = true;
                }
            }
        }
        if (affectsSystemUi && win.getAttrs().type == 2031) {
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
        if (this.mTopFullscreenOpaqueOrDimmingWindowState == null && affectsSystemUi && win.isDimming() && StackId.normallyFullscreenWindows(stackId)) {
            this.mTopFullscreenOpaqueOrDimmingWindowState = win;
        }
        if (this.mTopDockedOpaqueWindowState == null && affectsSystemUi && appWindow && attached == null && isFullscreen(attrs) && stackId == 3) {
            this.mTopDockedOpaqueWindowState = win;
            if (this.mTopDockedOpaqueOrDimmingWindowState == null) {
                this.mTopDockedOpaqueOrDimmingWindowState = win;
            }
        }
        if (this.mTopDockedOpaqueOrDimmingWindowState == null && affectsSystemUi && win.isDimming() && stackId == 3) {
            this.mTopDockedOpaqueOrDimmingWindowState = win;
        }
    }

    private void applyKeyguardPolicyLw(WindowState win, WindowState imeTarget) {
        if (!canBeHiddenByKeyguardLw(win)) {
            return;
        }
        if (shouldBeHiddenByKeyguard(win, imeTarget)) {
            win.hideLw(false);
        } else {
            win.showLw(false);
        }
    }

    boolean isCoverWindow(WindowState win) {
        LayoutParams attrs = win == null ? null : win.getAttrs();
        int type = attrs == null ? 0 : attrs.type;
        if (type == 2100 || type == 2101) {
            return true;
        }
        return false;
    }

    private boolean isFullscreen(LayoutParams attrs) {
        if (attrs.x == 0 && attrs.y == 0 && attrs.width == -1 && attrs.height == -1) {
            return true;
        }
        return false;
    }

    public int finishPostLayoutPolicyLw() {
        LayoutParams lp;
        int changes = 0;
        boolean topIsFullscreen = false;
        if (this.mTopFullscreenOpaqueWindowState != null) {
            lp = this.mTopFullscreenOpaqueWindowState.getAttrs();
        } else {
            lp = null;
        }
        if (!this.mShowingDream) {
            this.mDreamingLockscreen = isKeyguardShowingAndNotOccluded();
            if (this.mDreamingSleepTokenNeeded) {
                this.mDreamingSleepTokenNeeded = false;
                this.mHandler.obtainMessage(15, 0, 1).sendToTarget();
            }
        } else if (!this.mDreamingSleepTokenNeeded) {
            this.mDreamingSleepTokenNeeded = true;
            this.mHandler.obtainMessage(15, 1, 1).sendToTarget();
        }
        if (this.mStatusBar != null) {
            int shouldBeTransparent;
            if (!this.mForceStatusBarTransparent || (this.mForceStatusBar ^ 1) == 0) {
                shouldBeTransparent = 0;
            } else {
                shouldBeTransparent = this.mForceStatusBarFromKeyguard ^ 1;
            }
            if (shouldBeTransparent == 0) {
                this.mStatusBarController.setShowTransparent(false);
            } else if (!this.mStatusBar.isVisibleLw()) {
                this.mStatusBarController.setShowTransparent(true);
            }
            LayoutParams statusBarAttrs = this.mStatusBar.getAttrs();
            boolean statusBarExpanded = statusBarAttrs.height == -1 ? statusBarAttrs.width == -1 : false;
            if (this.mForceStatusBar || this.mForceStatusBarFromKeyguard || this.mForceStatusBarTransparent || ((this.mForceStatusBarTransparent && this.mForceStatusBarTransparentWin != null && this.mForceStatusBarTransparentWin.isVisibleLw()) || statusBarExpanded)) {
                if (this.mStatusBarController.setBarShowingLw(true)) {
                    changes = 1;
                }
                topIsFullscreen = this.mTopIsFullscreen ? this.mStatusBar.isAnimatingLw() : false;
                if ((this.mForceStatusBarFromKeyguard || statusBarExpanded) && this.mStatusBarController.isTransientShowing()) {
                    this.mStatusBarController.updateVisibilityLw(false, this.mLastSystemUiFlags, this.mLastSystemUiFlags);
                }
                if (statusBarExpanded && this.mNavigationBar != null && !computeNaviBarFlag() && this.mNavigationBarController.setBarShowingLw(true)) {
                    changes |= 1;
                }
            } else if (this.mTopFullscreenOpaqueWindowState != null) {
                int fl = PolicyControl.getWindowFlags(null, lp);
                topIsFullscreen = (fl & 1024) == 0 ? (this.mLastSystemUiFlags & 4) != 0 : true;
                if (IS_NOTCH_PROP) {
                    if (hideNotchStatusBar(fl)) {
                        this.mForceNotchStatusBar = false;
                    } else {
                        topIsFullscreen = false;
                    }
                }
                if (IS_NOTCH_PROP && this.mDisplayRotation == 0) {
                    boolean z;
                    int notchStatusBarColorStatus = this.mForceNotchStatusBar ? 1 : 0;
                    if (this.notchStatusBarColorLw != notchStatusBarColorStatus) {
                        z = true;
                    } else {
                        z = false;
                    }
                    this.notchWindowChangeState = z;
                    if (this.notchWindowChangeState) {
                        this.notchStatusBarColorLw = notchStatusBarColorStatus;
                        notchStatusBarColorUpdate(notchStatusBarColorStatus);
                        this.notchWindowChange = true;
                    } else if (!(this.mFocusedWindow == null || (this.mTopFullscreenOpaqueWindowState.toString().equals(this.mFocusedWindow.toString()) ^ 1) == 0 || (this.notchWindowChangeState ^ 1) == 0 || !this.mForceNotchStatusBar || !this.notchWindowChange)) {
                        notchStatusBarColorUpdate(notchStatusBarColorStatus);
                        this.notchWindowChange = false;
                    }
                }
                if (this.mStatusBarController.isTransientShowing()) {
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 1;
                    }
                } else if (!topIsFullscreen || (this.mWindowManagerInternal.isStackVisible(2) ^ 1) == 0 || (this.mWindowManagerInternal.isStackVisible(3) ^ 1) == 0) {
                    if (this.mStatusBarController.isTransientHiding()) {
                        Slog.v(TAG, "not fullscreen but transientBarState is hiding, so reset");
                        this.mStatusBarController.sethwTransientBarState(0);
                    }
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 1;
                    }
                } else if (this.mStatusBarController.setBarShowingLw(false)) {
                    changes = 1;
                }
            }
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
        if (this.mShowingDream != this.mLastShowingDream) {
            this.mLastShowingDream = this.mShowingDream;
            this.mWindowManagerFuncs.notifyShowingDreamChanged();
        }
        updateLockScreenTimeout();
        return changes;
    }

    protected boolean setKeyguardOccludedLw(boolean isOccluded, boolean force) {
        boolean wasOccluded = this.mKeyguardOccluded;
        boolean showing = this.mKeyguardDelegate.isShowing();
        boolean changed = wasOccluded == isOccluded ? force : true;
        LayoutParams attrs;
        if (!isOccluded && changed && showing) {
            this.mKeyguardOccluded = false;
            this.mKeyguardDelegate.setOccluded(false, true);
            if (this.mStatusBar != null) {
                attrs = this.mStatusBar.getAttrs();
                attrs.privateFlags |= 1024;
                if (!this.mKeyguardDelegate.hasLockscreenWallpaper()) {
                    attrs = this.mStatusBar.getAttrs();
                    attrs.flags |= DumpState.DUMP_DEXOPT;
                }
            }
            return true;
        } else if (isOccluded && changed && showing) {
            this.mKeyguardOccluded = true;
            this.mKeyguardDelegate.setOccluded(true, false);
            if (this.mStatusBar != null) {
                attrs = this.mStatusBar.getAttrs();
                attrs.privateFlags &= -1025;
                attrs = this.mStatusBar.getAttrs();
                attrs.flags &= -1048577;
            }
            return true;
        } else if (!changed) {
            return false;
        } else {
            this.mKeyguardOccluded = isOccluded;
            this.mKeyguardDelegate.setOccluded(isOccluded, false);
            return false;
        }
    }

    private boolean isStatusBarKeyguard() {
        if (this.mStatusBar == null || (this.mStatusBar.getAttrs().privateFlags & 1024) == 0) {
            return false;
        }
        return true;
    }

    public boolean allowAppAnimationsLw() {
        if (this.mShowingDream) {
            return false;
        }
        return true;
    }

    public int focusChangedLw(WindowState lastFocus, WindowState newFocus) {
        this.mFocusedWindow = newFocus;
        if (this.mFocusedWindow != null) {
            if (IS_NOTCH_PROP && this.mFocusedWindow.toString().contains("com.huawei.android.launcher")) {
                this.mForceNotchStatusBar = false;
            }
            updateSystemUiColorLw(this.mFocusedWindow);
            if (this.mLastStartingWindow != null && this.mLastStartingWindow.isVisibleLw() && this.mFocusedWindow.getAttrs() != null && this.mFocusedWindow.getAttrs().type == 2003) {
                updateSystemUiColorLw(this.mLastStartingWindow);
            }
        }
        if ((updateSystemUiVisibilityLw() & SYSTEM_UI_CHANGING_LAYOUT) != 0) {
            return 1;
        }
        return 0;
    }

    public void notifyLidSwitchChanged(long whenNanos, boolean lidOpen) {
        int newLidState = lidOpen ? 1 : 0;
        if (newLidState != this.mLidState) {
            this.mLidState = newLidState;
            applyLidSwitchState();
            updateRotation(true);
            if (lidOpen) {
                wakeUp(SystemClock.uptimeMillis(), this.mAllowTheaterModeWakeFromLidSwitch, "android.policy:LID");
            } else if (!this.mLidControlsSleep) {
                this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
            }
        }
    }

    public void notifyCameraLensCoverSwitchChanged(long whenNanos, boolean lensCovered) {
        int lensCoverState = lensCovered ? 1 : 0;
        if (this.mCameraLensCoverState != lensCoverState) {
            if (this.mCameraLensCoverState == 1 && lensCoverState == 0) {
                boolean keyguardActive;
                Intent intent;
                if (this.mKeyguardDelegate == null) {
                    keyguardActive = false;
                } else {
                    keyguardActive = this.mKeyguardDelegate.isShowing();
                }
                if (keyguardActive) {
                    intent = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE");
                } else {
                    intent = new Intent("android.media.action.STILL_IMAGE_CAMERA");
                }
                wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromCameraLens, "android.policy:CAMERA_COVER");
                startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            }
            this.mCameraLensCoverState = lensCoverState;
        }
    }

    void setHdmiPlugged(boolean plugged) {
        if (this.mHdmiPlugged != plugged) {
            this.mHdmiPlugged = plugged;
            updateRotation(true, true);
            Intent intent = new Intent("android.intent.action.HDMI_PLUGGED");
            intent.addFlags(67108864);
            intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, plugged);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0092 A:{SYNTHETIC, Splitter: B:30:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006f A:{SYNTHETIC, Splitter: B:23:0x006f} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009b A:{SYNTHETIC, Splitter: B:35:0x009b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void initializeHdmiState() {
        IOException ex;
        NumberFormatException ex2;
        Throwable th;
        boolean plugged = false;
        if (new File("/sys/devices/virtual/switch/hdmi/state").exists()) {
            this.mHDMIObserver.startObserving("DEVPATH=/devices/virtual/switch/hdmi");
            String filename = "/sys/class/switch/hdmi/state";
            FileReader reader = null;
            try {
                FileReader reader2 = new FileReader("/sys/class/switch/hdmi/state");
                try {
                    char[] buf = new char[15];
                    int n = reader2.read(buf);
                    if (n > 1) {
                        plugged = Integer.parseInt(new String(buf, 0, n + -1)) != 0;
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e) {
                        }
                    }
                } catch (IOException e2) {
                    ex = e2;
                    reader = reader2;
                    Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex);
                    if (reader != null) {
                    }
                    this.mHdmiPlugged = plugged ^ 1;
                    setHdmiPlugged(this.mHdmiPlugged ^ 1);
                } catch (NumberFormatException e3) {
                    ex2 = e3;
                    reader = reader2;
                    try {
                        Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex2);
                        if (reader != null) {
                        }
                        this.mHdmiPlugged = plugged ^ 1;
                        setHdmiPlugged(this.mHdmiPlugged ^ 1);
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                ex = e5;
                Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e6) {
                    }
                }
                this.mHdmiPlugged = plugged ^ 1;
                setHdmiPlugged(this.mHdmiPlugged ^ 1);
            } catch (NumberFormatException e7) {
                ex2 = e7;
                Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex2);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e8) {
                    }
                }
                this.mHdmiPlugged = plugged ^ 1;
                setHdmiPlugged(this.mHdmiPlugged ^ 1);
            }
        }
        this.mHdmiPlugged = plugged ^ 1;
        setHdmiPlugged(this.mHdmiPlugged ^ 1);
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return;
     */
    /* JADX WARNING: Missing block: B:18:0x0054, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void takeScreenshot(final int screenshotType) {
        synchronized (this.mScreenshotLock) {
            if (this.mScreenshotConnection == null) {
                ComponentName serviceComponent = new ComponentName(SYSUI_PACKAGE, SYSUI_SCREENSHOT_SERVICE);
                Intent serviceIntent = new Intent();
                serviceIntent.setComponent(serviceComponent);
                ServiceConnection conn = new ServiceConnection() {
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        synchronized (PhoneWindowManager.this.mScreenshotLock) {
                            if (PhoneWindowManager.this.mScreenshotConnection != this) {
                                return;
                            }
                            Messenger messenger = new Messenger(service);
                            Message msg = Message.obtain(null, screenshotType);
                            msg.replyTo = new Messenger(new Handler(PhoneWindowManager.this.mHandler.getLooper()) {
                                public void handleMessage(Message msg) {
                                    synchronized (PhoneWindowManager.this.mScreenshotLock) {
                                        if (PhoneWindowManager.this.mScreenshotConnection == this) {
                                            PhoneWindowManager.this.mContext.unbindService(PhoneWindowManager.this.mScreenshotConnection);
                                            if (PhoneWindowManager.HWFLOW) {
                                                Slog.i(PhoneWindowManager.TAG, "takeScreenshot  set mScreenshotConnection to null");
                                            }
                                            PhoneWindowManager.this.mScreenshotConnection = null;
                                            PhoneWindowManager.this.mHandler.removeCallbacks(PhoneWindowManager.this.mScreenshotTimeout);
                                        }
                                    }
                                }
                            });
                            msg.arg2 = 0;
                            msg.arg1 = 0;
                            if (PhoneWindowManager.this.mStatusBar != null && PhoneWindowManager.this.mStatusBar.isVisibleLw()) {
                                msg.arg1 = 1;
                            }
                            if (PhoneWindowManager.mUsingHwNavibar) {
                                if (!PhoneWindowManager.this.isNaviBarMini()) {
                                    msg.arg2 = 1;
                                }
                            } else if (PhoneWindowManager.this.mNavigationBar != null && PhoneWindowManager.this.mNavigationBar.isVisibleLw()) {
                                msg.arg2 = 1;
                            }
                            try {
                                messenger.send(msg);
                            } catch (RemoteException e) {
                            }
                        }
                    }

                    public void onServiceDisconnected(ComponentName name) {
                        synchronized (PhoneWindowManager.this.mScreenshotLock) {
                            if (PhoneWindowManager.this.mScreenshotConnection != null) {
                                PhoneWindowManager.this.mContext.unbindService(PhoneWindowManager.this.mScreenshotConnection);
                                PhoneWindowManager.this.mScreenshotConnection = null;
                                PhoneWindowManager.this.mHandler.removeCallbacks(PhoneWindowManager.this.mScreenshotTimeout);
                                PhoneWindowManager.this.notifyScreenshotError();
                            }
                        }
                    }
                };
                if (HWFLOW) {
                    Slog.i(TAG, "takeScreenshot  bindServiceAsUser");
                }
                if (this.mContext.bindServiceAsUser(serviceIntent, conn, 33554433, UserHandle.CURRENT)) {
                    this.mScreenshotConnection = conn;
                    this.mHandler.postDelayed(this.mScreenshotTimeout, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                }
            } else if (HWFLOW) {
                Slog.i(TAG, "takeScreenshot  not start");
            }
        }
    }

    private void notifyScreenshotError() {
        ComponentName errorComponent = new ComponentName(SYSUI_PACKAGE, SYSUI_SCREENSHOT_ERROR_RECEIVER);
        Intent errorIntent = new Intent("android.intent.action.USER_PRESENT");
        errorIntent.setComponent(errorComponent);
        errorIntent.addFlags(335544320);
        this.mContext.sendBroadcastAsUser(errorIntent, UserHandle.CURRENT);
    }

    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        if (this.mSystemBooted) {
            boolean keyguardActive;
            int isWakeKey;
            int result;
            boolean interactive = (536870912 & policyFlags) != 0;
            boolean down = event.getAction() == 0;
            boolean canceled = event.isCanceled();
            int keyCode = event.getKeyCode();
            boolean isInjected = (16777216 & policyFlags) != 0;
            if (this.mKeyguardDelegate == null) {
                keyguardActive = false;
            } else if (interactive) {
                keyguardActive = isKeyguardShowingAndNotOccluded();
            } else {
                keyguardActive = this.mKeyguardDelegate.isShowing();
            }
            if (HWFLOW) {
                Log.d(TAG, "interceptKeyTq keycode=" + keyCode + " interactive=" + interactive + " keyguardActive=" + keyguardActive + " policyFlags=" + Integer.toHexString(policyFlags) + " down " + down + " canceled " + canceled);
            }
            if ((policyFlags & 1) == 0) {
                isWakeKey = event.isWakeKey();
            } else {
                isWakeKey = 1;
            }
            if (interactive || (isInjected && (isWakeKey ^ 1) != 0)) {
                result = 1;
                isWakeKey = 0;
                if (interactive) {
                    if (keyCode == this.mPendingWakeKey && (down ^ 1) != 0) {
                        result = 0;
                    }
                    this.mPendingWakeKey = -1;
                }
            } else if (interactive || !shouldDispatchInputWhenNonInteractive(event)) {
                result = 0;
                if (!(isWakeKey == 0 || (down && (isWakeKeyWhenScreenOff(keyCode) ^ 1) == 0))) {
                    isWakeKey = 0;
                }
                if (isWakeKey != 0 && down) {
                    this.mPendingWakeKey = keyCode;
                }
            } else {
                result = 1;
                this.mPendingWakeKey = -1;
            }
            if (isValidGlobalKey(keyCode) && this.mGlobalKeyManager.shouldHandleGlobalKey(keyCode, event)) {
                if (isWakeKey != 0) {
                    wakeUp(event.getEventTime(), this.mAllowTheaterModeWakeFromKey, "android.policy:KEY");
                }
                Log.d(TAG, "key: " + keyCode + " , handled globally, just return the result " + result);
                return result;
            }
            boolean useHapticFeedback = (!down || (policyFlags & 2) == 0) ? false : event.getRepeatCount() == 0;
            TelecomManager telecomManager;
            Message msg;
            switch (keyCode) {
                case 4:
                    if (!down) {
                        if (interceptBackKeyUp(event)) {
                            result &= -2;
                        }
                        boolean isMiniDock = this.mWindowManagerInternal.isMinimizedDock();
                        if (canceled || !isMiniDock) {
                            if (isEqualBottomApp()) {
                                hideRecentApps(false, true);
                                break;
                            }
                        }
                        try {
                            ActivityManager.getService().moveTasksToFullscreenStack(3, false);
                            break;
                        } catch (Exception e) {
                            Slog.i(TAG, "back press error!");
                            break;
                        }
                    }
                    interceptBackKeyDown();
                    break;
                    break;
                case 5:
                    if (down) {
                        telecomManager = getTelecommService();
                        if (telecomManager != null && telecomManager.isRinging()) {
                            Log.i(TAG, "interceptKeyBeforeQueueing: CALL key-down while ringing: Answer the call!");
                            telecomManager.acceptRingingCall();
                            result &= -2;
                            break;
                        }
                    }
                    break;
                case 6:
                    result &= -2;
                    if (!down) {
                        if (!this.mEndCallKeyHandled) {
                            this.mHandler.removeCallbacks(this.mEndCallLongPress);
                            if (!canceled && (((this.mEndcallBehavior & 1) == 0 || !goHome()) && (this.mEndcallBehavior & 2) != 0)) {
                                this.mPowerManager.goToSleep(event.getEventTime(), 4, 0);
                                isWakeKey = 0;
                                break;
                            }
                        }
                    }
                    telecomManager = getTelecommService();
                    int hungUp = 0;
                    if (telecomManager != null) {
                        hungUp = telecomManager.endCall();
                    }
                    if (interactive && (hungUp ^ 1) != 0) {
                        this.mEndCallKeyHandled = false;
                        this.mHandler.postDelayed(this.mEndCallLongPress, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                        break;
                    }
                    this.mEndCallKeyHandled = true;
                    break;
                    break;
                case 24:
                case 25:
                case 164:
                    if (keyCode == 25) {
                        if (!down) {
                            this.mScreenshotChordVolumeDownKeyTriggered = false;
                            cancelPendingScreenshotChordAction();
                            cancelPendingAccessibilityShortcutAction();
                        } else if (interactive && (this.mScreenshotChordVolumeDownKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                            this.mScreenshotChordVolumeDownKeyTriggered = true;
                            this.mScreenshotChordVolumeDownKeyTime = event.getDownTime();
                            this.mScreenshotChordVolumeDownKeyConsumed = false;
                            cancelPendingPowerKeyAction();
                            interceptScreenshotChord();
                            interceptAccessibilityShortcutChord();
                        }
                    } else if (keyCode == 24) {
                        if (!down) {
                            this.mA11yShortcutChordVolumeUpKeyTriggered = false;
                            cancelPendingScreenshotChordAction();
                            cancelPendingAccessibilityShortcutAction();
                        } else if (interactive && (this.mA11yShortcutChordVolumeUpKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                            this.mA11yShortcutChordVolumeUpKeyTriggered = true;
                            this.mA11yShortcutChordVolumeUpKeyTime = event.getDownTime();
                            this.mA11yShortcutChordVolumeUpKeyConsumed = false;
                            cancelPendingPowerKeyAction();
                            cancelPendingScreenshotChordAction();
                            interceptAccessibilityShortcutChord();
                        }
                    }
                    if (down) {
                        telecomManager = getTelecommService();
                        if (telecomManager != null && telecomManager.isRinging()) {
                            Log.i(TAG, "interceptKeyBeforeQueueing: VOLUME key-down while ringing: Silence ringer!");
                            telecomManager.silenceRinger();
                            result &= -2;
                            break;
                        }
                        int audioMode = 0;
                        try {
                            audioMode = getAudioService().getMode();
                        } catch (Exception e2) {
                            Log.e(TAG, "Error getting AudioService in interceptKeyBeforeQueueing.", e2);
                        }
                        boolean isInCall = (telecomManager == null || !telecomManager.isInCall()) ? audioMode == 3 : true;
                        if (isInCall && (result & 1) == 0) {
                            MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(event, Integer.MIN_VALUE, false);
                            break;
                        }
                    }
                    Log.i(TAG, "Volume key pass to user " + ((result & 1) != 0));
                    if (!this.mUseTvRouting && !this.mHandleVolumeKeysInWM) {
                        if ((result & 1) == 0) {
                            MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(event, Integer.MIN_VALUE, true);
                            break;
                        }
                    }
                    result |= 1;
                    break;
                    break;
                case H.DO_ANIMATION_CALLBACK /*26*/:
                    cancelPendingAccessibilityShortcutAction();
                    result &= -2;
                    isWakeKey = 0;
                    if (!down) {
                        interceptPowerKeyUp(event, interactive, canceled);
                        break;
                    }
                    interceptPowerKeyDown(event, interactive);
                    break;
                case HdmiCecKeycode.CEC_KEYCODE_RESERVED /*79*/:
                case HdmiCecKeycode.CEC_KEYCODE_INITIAL_CONFIGURATION /*85*/:
                case HdmiCecKeycode.CEC_KEYCODE_SELECT_BROADCAST_TYPE /*86*/:
                case HdmiCecKeycode.CEC_KEYCODE_SELECT_SOUND_PRESENTATION /*87*/:
                case 88:
                case 89:
                case 90:
                case 91:
                case 126:
                case 127:
                case 130:
                case NetdResponseCode.DnsProxyQueryResult /*222*/:
                    if (MediaSessionLegacyHelper.getHelper(this.mContext).isGlobalPriorityActive()) {
                        result &= -2;
                    }
                    if ((result & 1) == 0) {
                        this.mBroadcastWakeLock.acquire();
                        msg = this.mHandler.obtainMessage(3, new KeyEvent(event));
                        msg.setAsynchronous(true);
                        msg.sendToTarget();
                        break;
                    }
                    break;
                case 171:
                    if (this.mShortPressWindowBehavior == 1 && this.mPictureInPictureVisible) {
                        if (!down) {
                            showPictureInPictureMenu(event);
                        }
                        result &= -2;
                        break;
                    }
                case NetdResponseCode.ClatdStatusResult /*223*/:
                    result &= -2;
                    isWakeKey = 0;
                    if (!this.mPowerManager.isInteractive()) {
                        useHapticFeedback = false;
                    }
                    if (!down) {
                        sleepRelease(event.getEventTime());
                        break;
                    }
                    sleepPress(event.getEventTime());
                    break;
                case 224:
                    result &= -2;
                    isWakeKey = 1;
                    break;
                case 231:
                    if ((result & 1) == 0 && (down ^ 1) != 0) {
                        this.mBroadcastWakeLock.acquire();
                        msg = this.mHandler.obtainMessage(12, keyguardActive ? 1 : 0, 0);
                        msg.setAsynchronous(true);
                        msg.sendToTarget();
                        break;
                    }
                case 276:
                    result &= -2;
                    isWakeKey = 0;
                    if (!down) {
                        this.mPowerManagerInternal.setUserInactiveOverrideFromWindowManager();
                        break;
                    }
                    break;
                case 280:
                case 281:
                case 282:
                case 283:
                    result &= -2;
                    interceptSystemNavigationKey(event);
                    break;
                case 701:
                    if (down && !SystemProperties.getBoolean("sys.super_power_save", false)) {
                        quickOpenCameraService("flip");
                        break;
                    }
            }
            if (useHapticFeedback) {
                performHapticFeedbackLw(null, 1, false);
            }
            if (isWakeKey != 0) {
                wakeUp(event.getEventTime(), this.mAllowTheaterModeWakeFromKey, "android.policy:KEY");
            }
            Log.d(TAG, "interceptKeyBeforeQueueing: key " + keyCode + " , result : " + result);
            return result;
        }
        Log.d(TAG, "we have not yet booted, don't let key events do anything.");
        return 0;
    }

    public boolean isEqualBottomApp() {
        boolean isFocusedSplitAppList = false;
        try {
            return FOCUSED_SPLIT_APP_ACTIVITY.equals(this.mWindowManager.getFocusedAppComponentName());
        } catch (RemoteException e) {
            Log.e(TAG, "Error get Focused App ComponentName.");
            return isFocusedSplitAppList;
        }
    }

    private void quickOpenCameraService(String command) {
        Intent intent = new Intent(HUAWEI_PRE_CAMERA_START_MODE);
        intent.setPackage("com.huawei.camera");
        intent.putExtra("command", command);
        this.mContext.startService(intent);
    }

    private void interceptSystemNavigationKey(KeyEvent event) {
        if (event.getAction() != 1) {
            return;
        }
        if (!(this.mAccessibilityManager.isEnabled() && (this.mAccessibilityManager.sendFingerprintGesture(event.getKeyCode()) ^ 1) == 0) && areSystemNavigationKeysEnabled()) {
            IStatusBarService sbar = getStatusBarService();
            if (sbar != null) {
                try {
                    sbar.handleSystemNavigationKey(event.getKeyCode());
                } catch (RemoteException e) {
                }
            }
        }
    }

    private static boolean isValidGlobalKey(int keyCode) {
        switch (keyCode) {
            case H.DO_ANIMATION_CALLBACK /*26*/:
            case NetdResponseCode.ClatdStatusResult /*223*/:
            case 224:
                return false;
            default:
                return true;
        }
    }

    protected boolean isWakeKeyWhenScreenOff(int keyCode) {
        boolean z = true;
        switch (keyCode) {
            case 24:
            case 25:
            case 164:
                if (this.mDockMode == 0) {
                    z = false;
                }
                return z;
            case 27:
            case HdmiCecKeycode.CEC_KEYCODE_RESERVED /*79*/:
            case HdmiCecKeycode.CEC_KEYCODE_INITIAL_CONFIGURATION /*85*/:
            case HdmiCecKeycode.CEC_KEYCODE_SELECT_BROADCAST_TYPE /*86*/:
            case HdmiCecKeycode.CEC_KEYCODE_SELECT_SOUND_PRESENTATION /*87*/:
            case 88:
            case 89:
            case 90:
            case 91:
            case 126:
            case 127:
            case 130:
            case NetdResponseCode.DnsProxyQueryResult /*222*/:
                return false;
            default:
                return true;
        }
    }

    public int interceptMotionBeforeQueueingNonInteractive(long whenNanos, int policyFlags) {
        if ((policyFlags & 1) != 0 && wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromMotion, "android.policy:MOTION")) {
            return 0;
        }
        if (shouldDispatchInputWhenNonInteractive(null) && (this.mInterceptInputForWaitBrightness ^ 1) != 0) {
            return 1;
        }
        if (isTheaterModeEnabled() && (policyFlags & 1) != 0) {
            wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromMotionWhenNotDreaming, "android.policy:MOTION");
        }
        return 0;
    }

    private boolean shouldDispatchInputWhenNonInteractive(KeyEvent event) {
        boolean displayOff = this.mDisplay == null || this.mDisplay.getState() == 1;
        if (displayOff && (this.mHasFeatureWatch ^ 1) != 0) {
            return false;
        }
        if (isKeyguardShowingAndNotOccluded() && (displayOff ^ 1) != 0) {
            return true;
        }
        if (this.mHasFeatureWatch && event != null && (event.getKeyCode() == 4 || event.getKeyCode() == DhcpPacket.MIN_PACKET_LENGTH_L3)) {
            return false;
        }
        IDreamManager dreamManager = getDreamManager();
        if (dreamManager != null) {
            try {
                if (dreamManager.isDreaming()) {
                    return true;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException when checking if dreaming", e);
            }
        }
        return false;
    }

    private void dispatchDirectAudioEvent(KeyEvent event) {
        if (event.getAction() == 0) {
            int keyCode = event.getKeyCode();
            String pkgName = this.mContext.getOpPackageName();
            switch (keyCode) {
                case 24:
                    try {
                        getAudioService().adjustSuggestedStreamVolume(1, Integer.MIN_VALUE, 4101, pkgName, TAG);
                        break;
                    } catch (Exception e) {
                        Log.e(TAG, "Error dispatching volume up in dispatchTvAudioEvent.", e);
                        break;
                    }
                case 25:
                    try {
                        getAudioService().adjustSuggestedStreamVolume(-1, Integer.MIN_VALUE, 4101, pkgName, TAG);
                        break;
                    } catch (Exception e2) {
                        Log.e(TAG, "Error dispatching volume down in dispatchTvAudioEvent.", e2);
                        break;
                    }
                case 164:
                    try {
                        if (event.getRepeatCount() == 0) {
                            getAudioService().adjustSuggestedStreamVolume(101, Integer.MIN_VALUE, 4101, pkgName, TAG);
                            break;
                        }
                    } catch (Exception e22) {
                        Log.e(TAG, "Error dispatching mute in dispatchTvAudioEvent.", e22);
                        break;
                    }
                    break;
            }
        }
    }

    void dispatchMediaKeyWithWakeLock(KeyEvent event) {
        Slog.d(TAG, "dispatchMediaKeyWithWakeLock: action=" + event.getAction() + ", keyCode=" + event.getKeyCode() + ", flags=" + event.getFlags() + ", repeatCount=" + event.getRepeatCount());
        if (this.mHavePendingMediaKeyRepeatWithWakeLock) {
            Slog.d(TAG, "dispatchMediaKeyWithWakeLock: canceled repeat");
            this.mHandler.removeMessages(4);
            this.mHavePendingMediaKeyRepeatWithWakeLock = false;
            this.mBroadcastWakeLock.release();
        }
        dispatchMediaKeyWithWakeLockToAudioService(event);
        if (event.getAction() == 0 && event.getRepeatCount() == 0) {
            this.mHavePendingMediaKeyRepeatWithWakeLock = true;
            Slog.d(TAG, "dispatchMediaKeyWithWakeLock: send repeat event");
            Message msg = this.mHandler.obtainMessage(4, event);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, (long) ViewConfiguration.getKeyRepeatTimeout());
            return;
        }
        this.mBroadcastWakeLock.release();
    }

    void dispatchMediaKeyRepeatWithWakeLock(KeyEvent event) {
        this.mHavePendingMediaKeyRepeatWithWakeLock = false;
        KeyEvent repeatEvent = KeyEvent.changeTimeRepeat(event, SystemClock.uptimeMillis(), 1, event.getFlags() | 128);
        Slog.d(TAG, "dispatchMediaKeyRepeatWithWakeLock: dispatch media key with long press");
        dispatchMediaKeyWithWakeLockToAudioService(repeatEvent);
        this.mBroadcastWakeLock.release();
    }

    void dispatchMediaKeyWithWakeLockToAudioService(KeyEvent event) {
        if (this.mActivityManagerInternal.isSystemReady()) {
            MediaSessionLegacyHelper.getHelper(this.mContext).sendMediaButtonEvent(event, true);
        }
    }

    void launchVoiceAssistWithWakeLock(boolean keyguardActive) {
        IDeviceIdleController dic = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        if (dic != null) {
            try {
                dic.exitIdle("voice-search");
            } catch (RemoteException e) {
            }
        }
        Intent voiceIntent = new Intent("android.speech.action.VOICE_SEARCH_HANDS_FREE");
        voiceIntent.putExtra("android.speech.extras.EXTRA_SECURE", keyguardActive);
        startActivityAsUser(voiceIntent, UserHandle.CURRENT_OR_SELF);
        this.mBroadcastWakeLock.release();
    }

    /* JADX WARNING: Missing block: B:26:0x004a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void requestTransientBars(WindowState swipeTarget) {
        synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
            if (isUserSetupComplete()) {
                int nb;
                boolean sb = this.mStatusBarController.checkShowTransientBarLw();
                if (this.mNavigationBarController.checkShowTransientBarLw()) {
                    nb = isNavBarEmpty(this.mLastSystemUiFlags) ^ 1;
                } else {
                    nb = 0;
                }
                if (sb || nb != 0) {
                    if (nb == 0 && swipeTarget == this.mNavigationBar) {
                        return;
                    }
                    if (sb) {
                        this.mStatusBarController.showTransient();
                    }
                    if (nb != 0) {
                        this.mNavigationBarController.showTransient();
                    }
                    this.mImmersiveModeConfirmation.confirmCurrentPrompt();
                    updateSystemUiVisibilityLw();
                }
            }
        }
    }

    public void startedGoingToSleep(int why) {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Started going to sleep... (why=" + why + ")");
        }
        this.mCameraGestureTriggeredDuringGoingToSleep = false;
        this.mGoingToSleep = true;
        if (this.mCurrentUserId == 0) {
            Secure.putInt(this.mContext.getContentResolver(), "lock_screen_state", 0);
        }
        if (this.mKeyguardDelegate != null && needTurnOff(why) && needTurnOffWithDismissFlag(this.mTopFullscreenOpaqueWindowState)) {
            Flog.i(305, "call onScreenTurnedOff(" + why + ")");
            this.mKeyguardDelegate.onStartedGoingToSleep(why);
        }
    }

    public void finishedGoingToSleep(int why) {
        EventLog.writeEvent(70000, 0);
        Flog.i(305, "Finished going to sleep... (why=" + why + ")");
        MetricsLogger.histogram(this.mContext, "screen_timeout", this.mLockScreenTimeout / 1000);
        this.mGoingToSleep = false;
        synchronized (this.mLock) {
            this.mAwake = false;
            updateWakeGestureListenerLp();
            updateOrientationListenerLp();
            updateLockScreenTimeout();
        }
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.onFinishedGoingToSleep(why, this.mCameraGestureTriggeredDuringGoingToSleep);
        }
        this.mCameraGestureTriggeredDuringGoingToSleep = false;
    }

    public void startedWakingUp() {
        EventLog.writeEvent(70000, 1);
        Flog.i(305, "Started waking up...");
        if (mSupportAod) {
            startAodService(4);
        }
        synchronized (this.mLock) {
            this.mAwake = true;
            updateWakeGestureListenerLp();
            updateOrientationListenerLp();
            updateLockScreenTimeout();
        }
        if (this.mCurrentUserId == 0) {
            Secure.putInt(this.mContext.getContentResolver(), "lock_screen_state", 1);
        }
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.onStartedWakingUp();
        }
    }

    public void finishedWakingUp() {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Finished waking up...");
        }
    }

    public boolean isStatusBarKeyguardShowing() {
        return isStatusBarKeyguard() ? this.mKeyguardOccluded ^ 1 : false;
    }

    private void wakeUpFromPowerKey(long eventTime) {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "Wakeing Up From PowerKey...");
        if (Jlog.isPerfTest()) {
            Jlog.i(2201, "JL_PWRSCRON_PWM_GETMESSAGE");
        }
        wakeUp(eventTime, this.mAllowTheaterModeWakeFromPowerKey, "android.policy:POWER");
    }

    private boolean wakeUp(long wakeTime, boolean wakeInTheaterMode, String reason) {
        boolean theaterModeEnabled = isTheaterModeEnabled();
        Flog.i(NativeResponseCode.SERVICE_FOUND, "Wake Up wakeInTheaterMode=" + wakeInTheaterMode + ", theaterModeEnabled=" + theaterModeEnabled);
        if (!wakeInTheaterMode && theaterModeEnabled) {
            return false;
        }
        if (theaterModeEnabled) {
            Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 0);
        }
        this.mPowerManager.wakeUp(wakeTime, reason);
        return true;
    }

    /* JADX WARNING: Missing block: B:8:0x000c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishKeyguardDrawn() {
        synchronized (this.mLock) {
            if (!this.mScreenOnEarly || this.mKeyguardDrawComplete) {
            } else {
                this.mKeyguardDrawComplete = true;
                if (this.mKeyguardDelegate != null) {
                    this.mHandler.removeMessages(6);
                }
                this.mWindowManagerDrawComplete = false;
                Flog.i(NativeResponseCode.SERVICE_FOUND, "finishKeyguardDrawn -> waitForAllWindowsDrawn");
                this.mWindowManagerInternal.waitForAllWindowsDrawn(this.mWindowManagerDrawCallback, 1000);
            }
        }
    }

    public void screenTurnedOff() {
        Flog.i(305, "Screen turned off...");
        updateScreenOffSleepToken(true);
        synchronized (this.mLock) {
            this.mScreenOnEarly = false;
            this.mScreenOnFully = false;
            this.mKeyguardDrawComplete = false;
            this.mWindowManagerDrawComplete = false;
            this.mScreenOnListener = null;
            updateOrientationListenerLp();
            if (this.mKeyguardDelegate != null) {
                this.mKeyguardDelegate.onScreenTurnedOff();
            }
            if (mSupportAod) {
                startAodService(3);
            }
        }
        if (this.mWindowManagerInternal.isStackVisible(3) && (this.mWindowManagerInternal.isMinimizedDock() ^ 1) != 0) {
            String topWindowName = this.mWindowManagerInternal.getFullStackTopWindow();
            if (topWindowName != null && topWindowName.contains("SplitScreenAppActivity")) {
                Flog.i(300, "start home for minimize dock.");
                startDockOrHome(true, false);
            }
        }
        reportScreenStateToVrManager(false);
    }

    private long getKeyguardDrawnTimeout() {
        return (long) (((SystemServiceManager) LocalServices.getService(SystemServiceManager.class)).isBootCompleted() ? 1000 : 5000);
    }

    public void screenTurningOn(ScreenOnListener screenOnListener) {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "Screen turning on...");
        }
        updateScreenOffSleepToken(false);
        synchronized (this.mLock) {
            if (DEBUG_WAKEUP) {
                Slog.i(TAG, "screen Turning On begin...");
            }
            if (mSupportAod) {
                startAodService(5);
            }
            this.mScreenOnEarly = true;
            this.mScreenOnFully = false;
            this.mKeyguardDrawComplete = false;
            this.mWindowManagerDrawComplete = false;
            this.mScreenOnListener = screenOnListener;
            if (this.mKeyguardDelegate != null) {
                this.mHandler.removeMessages(6);
                this.mHandler.sendEmptyMessageDelayed(6, getKeyguardDrawnTimeout());
                this.mKeyguardDelegate.onScreenTurningOn(this.mKeyguardDrawnCallback);
            } else {
                Flog.i(NativeResponseCode.SERVICE_FOUND, " null mKeyguardDelegate: setting mKeyguardDrawComplete.");
                finishKeyguardDrawn();
            }
        }
    }

    public void screenTurnedOn() {
        synchronized (this.mLock) {
            if (this.mKeyguardDelegate != null) {
                this.mKeyguardDelegate.onScreenTurnedOn();
            }
            if (mSupportAod) {
                startAodService(2);
            }
        }
        reportScreenStateToVrManager(true);
    }

    public void screenTurningOff(ScreenOffListener screenOffListener) {
        this.mWindowManagerFuncs.screenTurningOff(screenOffListener);
    }

    private void reportScreenStateToVrManager(boolean isScreenOn) {
        if (this.mVrManagerInternal != null) {
            this.mVrManagerInternal.onScreenStateChanged(isScreenOn);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0019, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishWindowsDrawn() {
        synchronized (this.mLock) {
            if (DEBUG_WAKEUP) {
                Slog.i(TAG, "finish Windows Drawn begin...");
            }
            if (!this.mScreenOnEarly || this.mWindowManagerDrawComplete) {
            } else {
                this.mWindowManagerDrawComplete = true;
                finishScreenTurningOn();
            }
        }
    }

    /* JADX WARNING: Missing block: B:26:0x0086, code:
            return;
     */
    /* JADX WARNING: Missing block: B:39:0x00b2, code:
            if (r1 == null) goto L_0x00b7;
     */
    /* JADX WARNING: Missing block: B:40:0x00b4, code:
            r1.onScreenOn();
     */
    /* JADX WARNING: Missing block: B:41:0x00b7, code:
            if (r0 == false) goto L_0x00be;
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            r7.mWindowManager.enableScreenIfNeeded();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishScreenTurningOn() {
        synchronized (this.mLock) {
            if (DEBUG_WAKEUP) {
                Slog.i(TAG, "finish Screen Turning On begin...");
            }
            updateOrientationListenerLp();
        }
        synchronized (this.mLock) {
            if (DEBUG_WAKEUP) {
                Slog.d(TAG, "finishScreenTurningOn: mAwake=" + this.mAwake + ", mScreenOnEarly=" + this.mScreenOnEarly + ", mScreenOnFully=" + this.mScreenOnFully + ", mKeyguardDrawComplete=" + this.mKeyguardDrawComplete + ", mWindowManagerDrawComplete=" + this.mWindowManagerDrawComplete);
            }
            if (this.mScreenOnFully || (this.mScreenOnEarly ^ 1) != 0 || (this.mWindowManagerDrawComplete ^ 1) != 0 || (this.mAwake && (this.mKeyguardDrawComplete ^ 1) != 0)) {
            } else {
                Slog.i(TAG, "Finished screen turning on...");
                ScreenOnListener listener = this.mScreenOnListener;
                this.mScreenOnListener = null;
                this.mScreenOnFully = true;
                boolean enableScreen;
                if (this.mKeyguardDrawnOnce || !this.mAwake) {
                    enableScreen = false;
                } else {
                    this.mKeyguardDrawnOnce = true;
                    enableScreen = true;
                    if (this.mBootMessageNeedsHiding) {
                        this.mBootMessageNeedsHiding = false;
                        hideBootMessages();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0010, code:
            if (r3.ifBootMessageShowing == false) goto L_0x002c;
     */
    /* JADX WARNING: Missing block: B:12:0x0014, code:
            if (DEBUG_WAKEUP == false) goto L_0x001f;
     */
    /* JADX WARNING: Missing block: B:13:0x0016, code:
            android.util.Slog.d(TAG, "HOTA handleHideBootMessage: dismissing");
     */
    /* JADX WARNING: Missing block: B:14:0x001f, code:
            r3.mHandler.post(new com.android.server.policy.PhoneWindowManager.AnonymousClass22(r3));
            r3.ifBootMessageShowing = false;
     */
    /* JADX WARNING: Missing block: B:16:0x002e, code:
            if (r3.mBootMsgDialog == null) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:18:0x0032, code:
            if (DEBUG_WAKEUP == false) goto L_0x003d;
     */
    /* JADX WARNING: Missing block: B:19:0x0034, code:
            android.util.Slog.d(TAG, "handleHideBootMessage: dismissing");
     */
    /* JADX WARNING: Missing block: B:20:0x003d, code:
            r3.mBootMsgDialog.dismiss();
            r3.mBootMsgDialog = null;
     */
    /* JADX WARNING: Missing block: B:21:0x0044, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleHideBootMessage() {
        synchronized (this.mLock) {
            if (!this.mKeyguardDrawnOnce) {
                this.mBootMessageNeedsHiding = true;
            }
        }
    }

    public boolean isScreenOn() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mScreenOnEarly;
        }
        return z;
    }

    public void enableKeyguard(boolean enabled) {
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.setKeyguardEnabled(enabled);
        }
    }

    public void exitKeyguardSecurely(OnKeyguardExitResult callback) {
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.verifyUnlock(callback);
        }
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        boolean z = false;
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        if (this.mKeyguardDelegate.isShowing()) {
            z = this.mKeyguardOccluded ^ 1;
        }
        return z;
    }

    protected boolean keyguardIsShowingTq() {
        return isKeyguardShowingAndNotOccluded();
    }

    public boolean isKeyguardTrustedLw() {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardDelegate.isTrusted();
    }

    public boolean isKeyguardLocked() {
        return keyguardOn();
    }

    public boolean isKeyguardSecure(int userId) {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardDelegate.isSecure(userId);
    }

    public boolean isKeyguardOccluded() {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardOccluded;
    }

    public boolean inKeyguardRestrictedKeyInputMode() {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardDelegate.isInputRestricted();
    }

    public void dismissKeyguardLw(IKeyguardDismissCallback callback) {
        if (this.mKeyguardDelegate != null && this.mKeyguardDelegate.isShowing()) {
            this.mKeyguardDelegate.dismiss(callback);
        } else if (callback != null) {
            try {
                callback.onDismissError();
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to call callback", e);
            }
        }
    }

    public boolean isKeyguardDrawnLw() {
        boolean z;
        synchronized (this.mLock) {
            z = !this.mKeyguardDrawnOnce ? this.mKeyguardDrawComplete : true;
        }
        return z;
    }

    public boolean isShowingDreamLw() {
        return this.mShowingDream;
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }
    }

    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets) {
        outInsets.setEmpty();
        getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, outInsets);
        outInsets.top = this.mStatusBarHeight;
    }

    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets) {
        outInsets.setEmpty();
        if (this.mHasNavigationBar) {
            int position = navigationBarPosition(displayWidth, displayHeight, displayRotation);
            if (position == 0) {
                outInsets.bottom = getNavigationBarHeight(displayRotation, this.mUiMode);
            } else if (position == 1) {
                outInsets.right = getNavigationBarWidth(displayRotation, this.mUiMode);
            } else if (position == 2) {
                outInsets.left = getNavigationBarWidth(displayRotation, this.mUiMode);
            }
        }
    }

    public boolean isNavBarForcedShownLw(WindowState windowState) {
        return this.mForceShowSystemBars;
    }

    public boolean isDockSideAllowed(int dockSide) {
        boolean z = true;
        if (this.mNavigationBarCanMove) {
            if (!(dockSide == 2 || dockSide == 1)) {
                z = false;
            }
            return z;
        } else if (this.mContext.getResources().getBoolean(17956870)) {
            if (!(dockSide == 2 || dockSide == 1)) {
                z = false;
            }
            return z;
        } else {
            if (!(dockSide == 2 || dockSide == 1 || dockSide == 3)) {
                z = false;
            }
            return z;
        }
    }

    void sendCloseSystemWindows() {
        PhoneWindow.sendCloseSystemWindows(this.mContext, null);
    }

    void sendCloseSystemWindows(String reason) {
        PhoneWindow.sendCloseSystemWindows(this.mContext, reason);
    }

    public int rotationForOrientationLw(int orientation, int lastRotation) {
        Slog.i(TAG, "rotationForOrientationLw(orient=" + orientation + ", last=" + lastRotation + "); user=" + this.mUserRotation + " " + (this.mUserRotationMode == 1 ? "USER_ROTATION_LOCKED" : ""));
        int defaultRotation = SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90;
        if (this.mForceDefaultOrientation) {
            return defaultRotation;
        }
        synchronized (this.mLock) {
            int preferredRotation;
            int i;
            int sensorRotation = getRotationFromSensorOrFaceFR(orientation, lastRotation);
            if (this.mLidState == 1 && this.mLidOpenRotation >= 0) {
                preferredRotation = this.mLidOpenRotation;
            } else if (this.mDockMode == 2 && (this.mCarDockEnablesAccelerometer || this.mCarDockRotation >= 0)) {
                preferredRotation = this.mCarDockEnablesAccelerometer ? sensorRotation : this.mCarDockRotation;
            } else if ((this.mDockMode == 1 || this.mDockMode == 3 || this.mDockMode == 4) && (this.mDeskDockEnablesAccelerometer || this.mDeskDockRotation >= 0)) {
                preferredRotation = this.mDeskDockEnablesAccelerometer ? sensorRotation : this.mDeskDockRotation;
            } else if (this.mHdmiPlugged && this.mDemoHdmiRotationLock) {
                preferredRotation = this.mDemoHdmiRotation;
            } else if (this.mHdmiPlugged && this.mDockMode == 0 && this.mUndockedHdmiRotation >= 0) {
                preferredRotation = this.mUndockedHdmiRotation;
            } else if (this.mDemoRotationLock) {
                preferredRotation = this.mDemoRotation;
            } else if (orientation == 14) {
                preferredRotation = lastRotation;
            } else if (!this.mSupportAutoRotation) {
                preferredRotation = -1;
            } else if ((this.mUserRotationMode == 0 && (orientation == 2 || orientation == -1 || orientation == 11 || orientation == 12 || orientation == 13)) || orientation == 4 || orientation == 10 || orientation == 6 || orientation == 7) {
                if (this.mAllowAllRotations < 0) {
                    if (this.mContext.getResources().getBoolean(17956870)) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    this.mAllowAllRotations = i;
                }
                preferredRotation = (sensorRotation != 2 || this.mAllowAllRotations == 1 || orientation == 10 || orientation == 13) ? this.mPersistentVrModeEnabled ? 0 : sensorRotation : lastRotation;
            } else {
                preferredRotation = (this.mUserRotationMode != 1 || orientation == 5) ? -1 : this.mUserRotation;
            }
            switch (orientation) {
                case 0:
                    if (isLandscapeOrSeascape(preferredRotation)) {
                        return preferredRotation;
                    }
                    i = this.mLandscapeRotation;
                    return i;
                case 1:
                    if (isAnyPortrait(preferredRotation)) {
                        return preferredRotation;
                    }
                    i = this.mPortraitRotation;
                    return i;
                case 6:
                case 11:
                    if (isLandscapeOrSeascape(preferredRotation)) {
                        return preferredRotation;
                    } else if (isLandscapeOrSeascape(lastRotation)) {
                        return lastRotation;
                    } else {
                        i = this.mLandscapeRotation;
                        return i;
                    }
                case 7:
                case 12:
                    if (isAnyPortrait(preferredRotation)) {
                        return preferredRotation;
                    } else if (isAnyPortrait(lastRotation)) {
                        return lastRotation;
                    } else {
                        i = this.mPortraitRotation;
                        return i;
                    }
                case 8:
                    if (isLandscapeOrSeascape(preferredRotation)) {
                        return preferredRotation;
                    }
                    i = this.mSeascapeRotation;
                    return i;
                case 9:
                    if (isAnyPortrait(preferredRotation)) {
                        return preferredRotation;
                    }
                    i = this.mUpsideDownRotation;
                    return i;
                default:
                    if (preferredRotation >= 0) {
                        return preferredRotation;
                    }
                    return defaultRotation;
            }
        }
    }

    public boolean rotationHasCompatibleMetricsLw(int orientation, int rotation) {
        switch (orientation) {
            case 0:
            case 6:
            case 8:
                return isLandscapeOrSeascape(rotation);
            case 1:
            case 7:
            case 9:
                return isAnyPortrait(rotation);
            default:
                return true;
        }
    }

    public void setRotationLw(int rotation) {
        this.mOrientationListener.setCurrentRotation(rotation);
    }

    private boolean isLandscapeOrSeascape(int rotation) {
        return rotation == this.mLandscapeRotation || rotation == this.mSeascapeRotation;
    }

    private boolean isAnyPortrait(int rotation) {
        return rotation == this.mPortraitRotation || rotation == this.mUpsideDownRotation;
    }

    public int getUserRotationMode() {
        if (System.getIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 0, -2) != 0) {
            return 0;
        }
        return 1;
    }

    public void setUserRotationMode(int mode, int rot) {
        ContentResolver res = this.mContext.getContentResolver();
        if (mode == 1) {
            System.putIntForUser(res, "user_rotation", rot, -2);
            System.putIntForUser(res, "accelerometer_rotation", 0, -2);
            return;
        }
        System.putIntForUser(res, "accelerometer_rotation", 1, -2);
    }

    public void setSafeMode(boolean safeMode) {
        int i;
        this.mSafeMode = safeMode;
        if (safeMode) {
            i = 10001;
        } else {
            i = 10000;
        }
        performHapticFeedbackLw(null, i, true);
    }

    static long[] getLongIntArray(Resources r, int resid) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return null;
        }
        long[] out = new long[ar.length];
        for (int i = 0; i < ar.length; i++) {
            out[i] = (long) ar[i];
        }
        return out;
    }

    private void bindKeyguard() {
        synchronized (this.mLock) {
            if (this.mKeyguardBound) {
                return;
            }
            this.mKeyguardBound = true;
            this.mKeyguardDelegate.bindService(this.mContext);
        }
    }

    public void onSystemUiStarted() {
        bindKeyguard();
    }

    public void systemReady() {
        this.mKeyguardDelegate.onSystemReady();
        this.mVrManagerInternal = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
        if (this.mVrManagerInternal != null) {
            this.mVrManagerInternal.addPersistentVrModeStateListener(this.mPersistentVrModeListener);
        }
        readCameraLensCoverState();
        updateUiMode();
        synchronized (this.mLock) {
            updateOrientationListenerLp();
            this.mSystemReady = true;
            this.mHandler.post(new Runnable() {
                public void run() {
                    PhoneWindowManager.this.updateSettings();
                }
            });
            if (this.mSystemBooted) {
                this.mKeyguardDelegate.onBootCompleted();
            }
        }
        this.mSystemGestures.systemReady(this);
        this.mImmersiveModeConfirmation.systemReady();
        this.mAutofillManagerInternal = (AutofillManagerInternal) LocalServices.getService(AutofillManagerInternal.class);
    }

    public void systemBooted() {
        bindKeyguard();
        synchronized (this.mLock) {
            this.mSystemBooted = true;
            if (this.mSystemReady) {
                this.mKeyguardDelegate.onBootCompleted();
            }
        }
        startedWakingUp();
        screenTurningOn(null);
        screenTurnedOn();
    }

    public boolean canDismissBootAnimation() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mKeyguardDrawComplete;
        }
        return z;
    }

    public void showBootMessage(final CharSequence msg, boolean always) {
        final String[] s = msg.toString().split(":");
        if (s[0].equals("HOTA") && s.length == 3) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    int curr = 0;
                    int total = 0;
                    try {
                        curr = Integer.parseInt(s[1]);
                        total = Integer.parseInt(s[2]);
                    } catch (NumberFormatException e) {
                        Log.e(PhoneWindowManager.TAG, "showBootMessage->NumberFormatException happened");
                    }
                    HwPolicyFactory.showBootMessage(PhoneWindowManager.this.mContext, curr, total);
                }
            });
            this.ifBootMessageShowing = true;
        } else if (!this.ifBootMessageShowing) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (PhoneWindowManager.this.mBootMsgDialog == null) {
                        int theme;
                        if (PhoneWindowManager.this.mContext.getPackageManager().hasSystemFeature("android.software.leanback")) {
                            theme = 16974818;
                        } else {
                            theme = 0;
                        }
                        PhoneWindowManager.this.mBootMsgDialog = new ProgressDialog(PhoneWindowManager.this.mContext, theme) {
                            public boolean dispatchKeyEvent(KeyEvent event) {
                                return true;
                            }

                            public boolean dispatchKeyShortcutEvent(KeyEvent event) {
                                return true;
                            }

                            public boolean dispatchTouchEvent(MotionEvent ev) {
                                return true;
                            }

                            public boolean dispatchTrackballEvent(MotionEvent ev) {
                                return true;
                            }

                            public boolean dispatchGenericMotionEvent(MotionEvent ev) {
                                return true;
                            }

                            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                                return true;
                            }
                        };
                        if (PhoneWindowManager.this.mContext.getPackageManager().isUpgrade()) {
                            PhoneWindowManager.this.mBootMsgDialog.setTitle(17039580);
                        } else {
                            PhoneWindowManager.this.mBootMsgDialog.setTitle(17039572);
                        }
                        PhoneWindowManager.this.mBootMsgDialog.setProgressStyle(0);
                        PhoneWindowManager.this.mBootMsgDialog.setIndeterminate(true);
                        PhoneWindowManager.this.mBootMsgDialog.getWindow().setType(2021);
                        PhoneWindowManager.this.mBootMsgDialog.getWindow().addFlags(LightsManager.LIGHT_ID_AUTOCUSTOMBACKLIGHT);
                        PhoneWindowManager.this.mBootMsgDialog.getWindow().setDimAmount(1.0f);
                        LayoutParams lp = PhoneWindowManager.this.mBootMsgDialog.getWindow().getAttributes();
                        lp.screenOrientation = 5;
                        PhoneWindowManager.this.mBootMsgDialog.getWindow().setAttributes(lp);
                        PhoneWindowManager.this.mBootMsgDialog.setCancelable(false);
                        PhoneWindowManager.this.mBootMsgDialog.show();
                    }
                    PhoneWindowManager.this.mBootMsgDialog.setMessage(msg);
                }
            });
        }
    }

    public void hideBootMessages() {
        this.mHandler.sendEmptyMessage(11);
    }

    public void userActivity() {
        synchronized (this.mScreenLockTimeout) {
            if (this.mLockScreenTimerActive) {
                this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                this.mHandler.postDelayed(this.mScreenLockTimeout, (long) this.mLockScreenTimeout);
            }
        }
    }

    public void lockNow(Bundle options) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        this.mHandler.removeCallbacks(this.mScreenLockTimeout);
        if (options != null) {
            this.mScreenLockTimeout.setLockOptions(options);
        }
        this.mHandler.post(this.mScreenLockTimeout);
    }

    private void updateLockScreenTimeout() {
        synchronized (this.mScreenLockTimeout) {
            boolean enable;
            if (this.mAllowLockscreenWhenOn && this.mAwake && this.mKeyguardDelegate != null) {
                enable = this.mKeyguardDelegate.isSecure(this.mCurrentUserId);
            } else {
                enable = false;
            }
            if (this.mLockScreenTimerActive != enable) {
                if (enable) {
                    this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                    this.mHandler.postDelayed(this.mScreenLockTimeout, (long) this.mLockScreenTimeout);
                } else {
                    this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                }
                this.mLockScreenTimerActive = enable;
            }
        }
    }

    private void updateDreamingSleepToken(boolean acquire) {
        if (acquire) {
            if (this.mDreamingSleepToken == null) {
                this.mDreamingSleepToken = this.mActivityManagerInternal.acquireSleepToken("Dream");
            }
        } else if (this.mDreamingSleepToken != null) {
            this.mDreamingSleepToken.release();
            this.mDreamingSleepToken = null;
        }
    }

    private void updateScreenOffSleepToken(boolean acquire) {
        if (acquire) {
            if (this.mScreenOffSleepToken == null) {
                this.mScreenOffSleepToken = this.mActivityManagerInternal.acquireSleepToken("ScreenOff");
            }
        } else if (this.mScreenOffSleepToken != null) {
            this.mScreenOffSleepToken.release();
            this.mScreenOffSleepToken = null;
        }
    }

    public void enableScreenAfterBoot() {
        readLidState();
        applyLidSwitchState();
    }

    private void applyLidSwitchState() {
        if (this.mLidState == 0 && this.mLidControlsSleep) {
            this.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 3, 1);
        } else if (this.mLidState == 0 && this.mLidControlsScreenLock) {
            this.mWindowManagerFuncs.lockDeviceNow();
        }
        synchronized (this.mLock) {
            updateWakeGestureListenerLp();
        }
    }

    void updateUiMode() {
        if (this.mUiModeManager == null) {
            this.mUiModeManager = Stub.asInterface(ServiceManager.getService("uimode"));
        }
        try {
            this.mUiMode = this.mUiModeManager.getCurrentModeType();
        } catch (RemoteException e) {
        }
    }

    void updateRotation(boolean alwaysSendConfiguration) {
        try {
            this.mWindowManager.updateRotation(alwaysSendConfiguration, false);
        } catch (RemoteException e) {
        }
    }

    void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) {
        try {
            this.mWindowManager.updateRotation(alwaysSendConfiguration, forceRelayout);
        } catch (RemoteException e) {
        }
    }

    Intent createHomeDockIntent() {
        Intent intent;
        if (this.mUiMode == 3) {
            if (this.mEnableCarDockHomeCapture) {
                intent = this.mCarDockIntent;
            }
            intent = null;
        } else {
            if (this.mUiMode != 2) {
                if (this.mUiMode == 6 && (this.mDockMode == 1 || this.mDockMode == 4 || this.mDockMode == 3)) {
                    intent = this.mDeskDockIntent;
                } else if (this.mUiMode == 7) {
                    intent = this.mVrHeadsetHomeIntent;
                }
            }
            intent = null;
        }
        if (intent == null) {
            return null;
        }
        ActivityInfo ai = null;
        ResolveInfo info = this.mContext.getPackageManager().resolveActivityAsUser(intent, 65664, this.mCurrentUserId);
        if (info != null) {
            ai = info.activityInfo;
        }
        if (ai == null || ai.metaData == null || !ai.metaData.getBoolean("android.dock_home")) {
            return null;
        }
        Intent intent2 = new Intent(intent);
        intent2.setClassName(ai.packageName, ai.name);
        return intent2;
    }

    void startDockOrHome(boolean fromHomeKey, boolean awakenFromDreams) {
        Intent intent;
        if (awakenFromDreams) {
            awakenDreams();
        }
        Intent dock = createHomeDockIntent();
        if (dock != null) {
            if (fromHomeKey) {
                try {
                    dock.putExtra("android.intent.extra.FROM_HOME_KEY", fromHomeKey);
                } catch (ActivityNotFoundException e) {
                }
            }
            startActivityAsUser(dock, UserHandle.CURRENT);
            return;
        }
        if (fromHomeKey) {
            intent = new Intent(this.mHomeIntent);
            intent.putExtra("android.intent.extra.FROM_HOME_KEY", fromHomeKey);
        } else {
            intent = this.mHomeIntent;
        }
        startActivityAsUser(intent, UserHandle.CURRENT);
    }

    boolean goHome() {
        if (isUserSetupComplete()) {
            try {
                if (SystemProperties.getInt("persist.sys.uts-test-mode", 0) == 1) {
                    Log.d(TAG, "UTS-TEST-MODE");
                } else {
                    ActivityManager.getService().stopAppSwitches();
                    sendCloseSystemWindows();
                    Intent dock = createHomeDockIntent();
                    if (dock != null && ActivityManager.getService().startActivityAsUser(null, null, dock, dock.resolveTypeIfNeeded(this.mContext.getContentResolver()), null, null, 0, 1, null, null, -2) == 1) {
                        return false;
                    }
                }
                if (ActivityManager.getService().startActivityAsUser(null, null, this.mHomeIntent, this.mHomeIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), null, null, 0, 1, null, null, -2) == 1) {
                    return false;
                }
            } catch (RemoteException e) {
            }
            return true;
        }
        Slog.i(TAG, "Not going home because user setup is in progress.");
        return false;
    }

    public void setCurrentOrientationLw(int newOrientation) {
        synchronized (this.mLock) {
            if (newOrientation != this.mCurrentAppOrientation) {
                this.mCurrentAppOrientation = newOrientation;
                updateOrientationListenerLp();
            }
        }
    }

    private boolean isTheaterModeEnabled() {
        return Global.getInt(this.mContext.getContentResolver(), "theater_mode_on", 0) == 1;
    }

    private boolean areSystemNavigationKeysEnabled() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "system_navigation_keys_enabled", 0, -2) == 1;
    }

    /* JADX WARNING: Missing block: B:7:0x001a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean performHapticFeedbackLw(WindowState win, int effectId, boolean always) {
        if (this.mVibrator == null) {
            this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        }
        if (this.mVibrator == null || this.mKeyguardDelegate == null || !this.mVibrator.hasVibrator()) {
            return false;
        }
        if ((System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) == 0) && (always ^ 1) != 0) {
            return false;
        }
        VibrationEffect effect = getVibrationEffect(effectId);
        if (effect == null) {
            return false;
        }
        int owningUid;
        String owningPackage;
        if (win != null) {
            owningUid = win.getOwningUid();
            owningPackage = win.getOwningPackage();
        } else {
            owningUid = Process.myUid();
            owningPackage = this.mContext.getOpPackageName();
        }
        this.mVibrator.vibrate(owningUid, owningPackage, effect, VIBRATION_ATTRIBUTES);
        return true;
    }

    private VibrationEffect getVibrationEffect(int effectId) {
        long[] pattern;
        switch (effectId) {
            case 0:
                pattern = this.mLongPressVibePattern;
                break;
            case 1:
                return VibrationEffect.get(0);
            case 3:
                return VibrationEffect.get(0);
            case 4:
                pattern = this.mClockTickVibePattern;
                break;
            case 5:
                pattern = this.mCalendarDateVibePattern;
                break;
            case 6:
                pattern = this.mContextClickVibePattern;
                break;
            case 10000:
                pattern = this.mSafeModeDisabledVibePattern;
                break;
            case 10001:
                pattern = this.mSafeModeEnabledVibePattern;
                break;
            default:
                return null;
        }
        if (pattern.length == 0) {
            return null;
        }
        if (pattern.length == 1) {
            return VibrationEffect.createOneShot(pattern[0], -1);
        }
        return VibrationEffect.createWaveform(pattern, -1);
    }

    public void keepScreenOnStartedLw() {
    }

    public void keepScreenOnStoppedLw() {
        if (isKeyguardShowingAndNotOccluded()) {
            this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }
    }

    private int updateSystemUiVisibilityLw() {
        WindowState winCandidate;
        if (this.mFocusedWindow != null) {
            winCandidate = this.mFocusedWindow;
        } else {
            winCandidate = this.mTopFullscreenOpaqueWindowState;
        }
        if (winCandidate == null) {
            return 0;
        }
        if (winCandidate.getAttrs().token == this.mImmersiveModeConfirmation.getWindowToken()) {
            winCandidate = isStatusBarKeyguard() ? this.mStatusBar : this.mTopFullscreenOpaqueWindowState;
            if (winCandidate == null) {
                return 0;
            }
        }
        final WindowState win = winCandidate;
        if (winCandidate.getAttrs().type == 3 && this.mLastStartingWindow != win) {
            this.mLastStartingWindow = win;
        }
        if ((win.getAttrs().privateFlags & 1024) != 0 && this.mKeyguardOccluded) {
            return 0;
        }
        int tmpVisibility = (PolicyControl.getSystemUiVisibility(win, null) & (~this.mResettingSystemUiFlags)) & (~this.mForceClearedSystemUiFlags);
        if (this.mForcingShowNavBar && win.getSurfaceLayer() < this.mForcingShowNavBarLayer) {
            tmpVisibility &= ~PolicyControl.adjustClearableFlags(win, 7);
        }
        final int fullscreenVisibility = updateLightStatusBarLw(0, this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState);
        final int dockedVisibility = updateLightStatusBarLw(0, this.mTopDockedOpaqueWindowState, this.mTopDockedOpaqueOrDimmingWindowState);
        this.mWindowManagerFuncs.getStackBounds(0, this.mNonDockedStackBounds);
        this.mWindowManagerFuncs.getStackBounds(3, this.mDockedStackBounds);
        final int visibility = updateSystemBarsLw(win, this.mLastSystemUiFlags, tmpVisibility);
        int visSysui = (win.getAttrs().privateFlags & Integer.MIN_VALUE) != 0 ? visibility | 2 : visibility;
        int diff = visSysui ^ this.mLastSystemUiFlags;
        int fullscreenDiff = fullscreenVisibility ^ this.mLastFullscreenStackSysUiFlags;
        int dockedDiff = dockedVisibility ^ this.mLastDockedStackSysUiFlags;
        this.mImmersiveStatusChanged = (diff & 4096) != 0;
        final boolean needsMenu = win.getNeedsMenuLw(this.mTopFullscreenOpaqueWindowState);
        if (diff == 0 && fullscreenDiff == 0 && dockedDiff == 0 && this.mLastFocusNeedsMenu == needsMenu && this.mFocusedApp == win.getAppToken() && this.mLastNonDockedStackBounds.equals(this.mNonDockedStackBounds) && this.mLastDockedStackBounds.equals(this.mDockedStackBounds)) {
            return 0;
        }
        if (!(Pattern.compile("[0-9]++").matcher(win.getAttrs().getTitle()).find() || diff == 0)) {
            Flog.i(303, "Policy setSystemUiVisibility, vis=" + Integer.toHexString(visibility) + ",lastVis=" + Integer.toHexString(this.mLastSystemUiFlags) + ",diff=" + Integer.toHexString(diff) + ",win=" + win.toString());
        }
        this.mLastSystemUiFlags = visSysui;
        this.mLastFullscreenStackSysUiFlags = fullscreenVisibility;
        this.mLastDockedStackSysUiFlags = dockedVisibility;
        this.mLastFocusNeedsMenu = needsMenu;
        this.mFocusedApp = win.getAppToken();
        final Rect fullscreenStackBounds = new Rect(this.mNonDockedStackBounds);
        final Rect dockedStackBounds = new Rect(this.mDockedStackBounds);
        this.mHandler.post(new Runnable() {
            public void run() {
                StatusBarManagerInternal statusbar = PhoneWindowManager.this.getStatusBarManagerInternal();
                if (statusbar != null) {
                    statusbar.setSystemUiVisibility(visibility, fullscreenVisibility, dockedVisibility, -1, fullscreenStackBounds, dockedStackBounds, win.toString());
                    statusbar.topAppWindowChanged(needsMenu);
                }
            }
        });
        if (win.getAttrs().type != 3) {
            updateSystemUiColorLw(win);
        }
        return diff;
    }

    public void updateSystemUiColorLw(WindowState win) {
    }

    private int updateLightStatusBarLw(int vis, WindowState opaque, WindowState opaqueOrDimming) {
        WindowState statusColorWin;
        if (!isStatusBarKeyguard() || (this.mKeyguardOccluded ^ 1) == 0) {
            statusColorWin = opaqueOrDimming;
        } else {
            statusColorWin = this.mStatusBar;
        }
        if (statusColorWin == null) {
            return vis;
        }
        if (statusColorWin == opaque) {
            return (vis & -8193) | (PolicyControl.getSystemUiVisibility(statusColorWin, null) & 8192);
        }
        if (statusColorWin == null || !statusColorWin.isDimming()) {
            return vis;
        }
        return vis & -8193;
    }

    private int updateLightNavigationBarLw(int vis, WindowState opaque, WindowState opaqueOrDimming) {
        WindowState navColorWin;
        WindowState imeWin = this.mWindowManagerFuncs.getInputMethodWindowLw();
        if (imeWin == null || !imeWin.isVisibleLw()) {
            navColorWin = opaqueOrDimming;
        } else {
            navColorWin = imeWin;
        }
        if (navColorWin == null) {
            return vis;
        }
        if (navColorWin == opaque) {
            return (vis & -17) | (PolicyControl.getSystemUiVisibility(navColorWin, null) & 16);
        }
        if (navColorWin.isDimming() || navColorWin == imeWin) {
            return vis & -17;
        }
        return vis;
    }

    private boolean drawsSystemBarBackground(WindowState win) {
        return win == null || (win.getAttrs().flags & Integer.MIN_VALUE) != 0;
    }

    private boolean forcesDrawStatusBarBackground(WindowState win) {
        return win == null || (win.getAttrs().privateFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0;
    }

    private int updateSystemBarsLw(WindowState win, int oldVis, int vis) {
        WindowState fullscreenTransWin;
        boolean fullscreenDrawsStatusBarBackground;
        boolean denyTransientStatus;
        boolean dockedStackVisible = this.mWindowManagerInternal.isStackVisible(3);
        boolean freeformStackVisible = this.mWindowManagerInternal.isStackVisible(2);
        boolean resizing = this.mWindowManagerInternal.isDockedDividerResizing();
        boolean z = (dockedStackVisible || freeformStackVisible) ? true : resizing;
        this.mForceShowSystemBars = z;
        int forceOpaqueStatusBar = this.mForceShowSystemBars ? this.mForceStatusBarFromKeyguard ^ 1 : 0;
        if (!isStatusBarKeyguard() || (this.mKeyguardOccluded ^ 1) == 0) {
            fullscreenTransWin = this.mTopFullscreenOpaqueWindowState;
        } else {
            fullscreenTransWin = this.mStatusBar;
        }
        vis = this.mNavigationBarController.applyTranslucentFlagLw(fullscreenTransWin, this.mStatusBarController.applyTranslucentFlagLw(fullscreenTransWin, vis, oldVis), oldVis);
        if (!(forceOpaqueStatusBar == 0 || win.getAttrs().isEmuiStyle == 0)) {
            vis |= 1073741824;
        }
        int dockedVis = this.mStatusBarController.applyTranslucentFlagLw(this.mTopDockedOpaqueWindowState, 0, 0);
        if (drawsSystemBarBackground(this.mTopFullscreenOpaqueWindowState) && (1073741824 & vis) == 0) {
            fullscreenDrawsStatusBarBackground = true;
        } else {
            fullscreenDrawsStatusBarBackground = forcesDrawStatusBarBackground(this.mTopFullscreenOpaqueWindowState);
        }
        boolean dockedDrawsStatusBarBackground;
        if (drawsSystemBarBackground(this.mTopDockedOpaqueWindowState) && (1073741824 & dockedVis) == 0) {
            dockedDrawsStatusBarBackground = true;
        } else {
            dockedDrawsStatusBarBackground = forcesDrawStatusBarBackground(this.mTopDockedOpaqueWindowState);
        }
        boolean statusBarHasFocus = win.getAttrs().type == 2000;
        if (statusBarHasFocus && (isStatusBarKeyguard() ^ 1) != 0) {
            int flags = 14342;
            if (this.mKeyguardOccluded) {
                flags = -1073727482;
            }
            vis = ((~flags) & vis) | (oldVis & flags);
        }
        if (fullscreenDrawsStatusBarBackground && dockedDrawsStatusBarBackground) {
            vis = (vis | 8) & -1073741825;
        } else if (!(areTranslucentBarsAllowed() || fullscreenTransWin == this.mStatusBar)) {
            vis &= -1073741833;
        }
        vis = configureNavBarOpacity(vis, dockedStackVisible, freeformStackVisible, resizing);
        boolean immersiveSticky = (vis & 4096) != 0;
        boolean hideStatusBarWM = this.mTopFullscreenOpaqueWindowState != null ? (PolicyControl.getWindowFlags(this.mTopFullscreenOpaqueWindowState, null) & 1024) != 0 : false;
        boolean hideStatusBarSysui = (vis & 4) != 0;
        boolean hideNavBarSysui = (vis & 2) != 0;
        if (this.mCust != null) {
            vis = this.mCust.updateSystemBarsLw(this.mContext, this.mFocusedWindow, vis);
        }
        boolean transientStatusBarAllowed = this.mStatusBar != null ? !statusBarHasFocus ? !this.mForceShowSystemBars ? (!hideStatusBarWM || (this.mImmersiveStatusChanged ^ 1) == 0) ? hideStatusBarSysui ? immersiveSticky : false : true : false : true : false;
        boolean transientNavBarAllowed = this.mNavigationBar != null ? (!this.mForceShowSystemBars && hideNavBarSysui && hideStatusBarWM) ? true : (this.mForceShowSystemBars || !hideNavBarSysui) ? false : immersiveSticky : false;
        boolean pendingPanic = this.mPendingPanicGestureUptime != 0 ? SystemClock.uptimeMillis() - this.mPendingPanicGestureUptime <= PANIC_GESTURE_EXPIRATION : false;
        if (pendingPanic && hideNavBarSysui && (isStatusBarKeyguard() ^ 1) != 0 && this.mKeyguardDrawComplete) {
            this.mPendingPanicGestureUptime = 0;
            this.mStatusBarController.showTransient();
            if (!isNavBarEmpty(vis)) {
                this.mNavigationBarController.showTransient();
            }
        }
        if (!this.mStatusBarController.isTransientShowRequested() || (transientStatusBarAllowed ^ 1) == 0) {
            denyTransientStatus = false;
        } else {
            denyTransientStatus = hideStatusBarSysui;
        }
        int denyTransientNav;
        if (this.mNavigationBarController.isTransientShowRequested()) {
            denyTransientNav = transientNavBarAllowed ^ 1;
        } else {
            denyTransientNav = 0;
        }
        if (denyTransientStatus || denyTransientNav != 0 || this.mForceShowSystemBars) {
            clearClearableFlagsLw();
            vis &= -8;
        }
        int navAllowedHidden = !((vis & 2048) != 0) ? (vis & 4096) != 0 : 1;
        if (hideNavBarSysui && (navAllowedHidden ^ 1) != 0 && getWindowLayerLw(win) > getWindowLayerFromTypeLw(2022)) {
            vis &= -3;
        }
        vis = this.mStatusBarController.updateVisibilityLw(transientStatusBarAllowed, oldVis, vis);
        boolean oldImmersiveMode = isImmersiveMode(oldVis);
        boolean newImmersiveMode = isImmersiveMode(vis);
        setNaviImmersiveMode(newImmersiveMode);
        if (!(win == null || oldImmersiveMode == newImmersiveMode)) {
            this.mImmersiveModeConfirmation.immersiveModeChangedLw(win.getOwningPackage(), newImmersiveMode, isUserSetupComplete(), isNavBarEmpty(win.getSystemUiVisibility()));
        }
        return updateLightNavigationBarLw(this.mNavigationBarController.updateVisibilityLw(transientNavBarAllowed, oldVis, vis), this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState);
    }

    private int configureNavBarOpacity(int visibility, boolean dockedStackVisible, boolean freeformStackVisible, boolean isDockedDividerResizing) {
        if (this.mNavBarOpacityMode == 0) {
            if (dockedStackVisible || freeformStackVisible || isDockedDividerResizing) {
                visibility = setNavBarOpaqueFlag(visibility);
            }
        } else if (this.mNavBarOpacityMode == 1) {
            if (isDockedDividerResizing) {
                visibility = setNavBarOpaqueFlag(visibility);
            } else if (freeformStackVisible) {
                visibility = setNavBarTranslucentFlag(visibility);
            } else {
                visibility = setNavBarOpaqueFlag(visibility);
            }
        }
        if (areTranslucentBarsAllowed()) {
            return visibility;
        }
        return visibility & HwBootFail.STAGE_BOOT_SUCCESS;
    }

    private int setNavBarOpaqueFlag(int visibility) {
        return visibility & 2147450879;
    }

    private int setNavBarTranslucentFlag(int visibility) {
        return (visibility & -32769) | Integer.MIN_VALUE;
    }

    private void clearClearableFlagsLw() {
        int newVal = this.mResettingSystemUiFlags | 7;
        if (newVal != this.mResettingSystemUiFlags) {
            this.mResettingSystemUiFlags = newVal;
            this.mWindowManagerFuncs.reevaluateStatusBarVisibility();
        }
    }

    private boolean isImmersiveMode(int vis) {
        if (this.mNavigationBar == null || (vis & 2) == 0 || (vis & 6144) == 0) {
            return false;
        }
        return canHideNavigationBar();
    }

    private static boolean isNavBarEmpty(int systemUiFlags) {
        return (systemUiFlags & 23068672) == 23068672;
    }

    private boolean areTranslucentBarsAllowed() {
        return this.mTranslucentDecorEnabled;
    }

    public boolean hasNavigationBar() {
        return this.mHasNavigationBar;
    }

    public void setLastInputMethodWindowLw(WindowState ime, WindowState target) {
        this.mLastInputMethodWindow = ime;
        this.mLastInputMethodTargetWindow = target;
    }

    public void setDismissImeOnBackKeyPressed(boolean newValue) {
        this.mDismissImeOnBackKeyPressed = newValue;
    }

    public int getInputMethodWindowVisibleHeightLw() {
        return this.mDockBottom - this.mCurBottom;
    }

    public void setCurrentUserLw(int newUserId) {
        this.mCurrentUserId = newUserId;
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.setCurrentUser(newUserId);
        }
        if (this.mAccessibilityShortcutController != null) {
            this.mAccessibilityShortcutController.setCurrentUser(newUserId);
        }
        StatusBarManagerInternal statusBar = getStatusBarManagerInternal();
        if (statusBar != null) {
            statusBar.setCurrentUser(newUserId);
        }
        setLastInputMethodWindowLw(null, null);
    }

    public void setSwitchingUser(boolean switching) {
        this.mKeyguardDelegate.setSwitchingUser(switching);
    }

    public boolean canMagnifyWindow(int windowType) {
        switch (windowType) {
            case 2011:
            case 2012:
            case 2019:
            case 2027:
                return false;
            default:
                return true;
        }
    }

    public boolean isTopLevelWindow(int windowType) {
        boolean z = true;
        if (windowType < 1000 || windowType > 1999) {
            return true;
        }
        if (windowType != 1003) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:4:0x000a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean shouldRotateSeamlessly(int oldRotation, int newRotation) {
        if (oldRotation == this.mUpsideDownRotation || newRotation == this.mUpsideDownRotation || !this.mNavigationBarCanMove) {
            return false;
        }
        int delta = newRotation - oldRotation;
        if (delta < 0) {
            delta += 4;
        }
        if (delta == 2) {
            return false;
        }
        WindowState w = this.mTopFullscreenOpaqueWindowState;
        if (w == this.mFocusedWindow && w != null && (w.isAnimatingLw() ^ 1) != 0 && (w.getAttrs().rotationAnimation == 2 || w.getAttrs().rotationAnimation == 3)) {
            return true;
        }
        return false;
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        pw.print(prefix);
        pw.print("mSafeMode=");
        pw.print(this.mSafeMode);
        pw.print(" mSystemReady=");
        pw.print(this.mSystemReady);
        pw.print(" mSystemBooted=");
        pw.println(this.mSystemBooted);
        pw.print(prefix);
        pw.print("mLidState=");
        pw.print(this.mLidState);
        pw.print(" mLidOpenRotation=");
        pw.print(this.mLidOpenRotation);
        pw.print(" mCameraLensCoverState=");
        pw.print(this.mCameraLensCoverState);
        pw.print(" mHdmiPlugged=");
        pw.println(this.mHdmiPlugged);
        if (!(this.mLastSystemUiFlags == 0 && this.mResettingSystemUiFlags == 0 && this.mForceClearedSystemUiFlags == 0)) {
            pw.print(prefix);
            pw.print("mLastSystemUiFlags=0x");
            pw.print(Integer.toHexString(this.mLastSystemUiFlags));
            pw.print(" mResettingSystemUiFlags=0x");
            pw.print(Integer.toHexString(this.mResettingSystemUiFlags));
            pw.print(" mForceClearedSystemUiFlags=0x");
            pw.println(Integer.toHexString(this.mForceClearedSystemUiFlags));
        }
        if (this.mLastFocusNeedsMenu) {
            pw.print(prefix);
            pw.print("mLastFocusNeedsMenu=");
            pw.println(this.mLastFocusNeedsMenu);
        }
        pw.print(prefix);
        pw.print("mWakeGestureEnabledSetting=");
        pw.println(this.mWakeGestureEnabledSetting);
        pw.print(prefix);
        pw.print("mSupportAutoRotation=");
        pw.println(this.mSupportAutoRotation);
        pw.print(prefix);
        pw.print("mUiMode=");
        pw.print(this.mUiMode);
        pw.print(" mDockMode=");
        pw.print(this.mDockMode);
        pw.print(" mEnableCarDockHomeCapture=");
        pw.print(this.mEnableCarDockHomeCapture);
        pw.print(" mCarDockRotation=");
        pw.print(this.mCarDockRotation);
        pw.print(" mDeskDockRotation=");
        pw.println(this.mDeskDockRotation);
        pw.print(prefix);
        pw.print("mUserRotationMode=");
        pw.print(this.mUserRotationMode);
        pw.print(" mUserRotation=");
        pw.print(this.mUserRotation);
        pw.print(" mAllowAllRotations=");
        pw.println(this.mAllowAllRotations);
        pw.print(prefix);
        pw.print("mCurrentAppOrientation=");
        pw.println(this.mCurrentAppOrientation);
        pw.print(prefix);
        pw.print("mCarDockEnablesAccelerometer=");
        pw.print(this.mCarDockEnablesAccelerometer);
        pw.print(" mDeskDockEnablesAccelerometer=");
        pw.println(this.mDeskDockEnablesAccelerometer);
        pw.print(prefix);
        pw.print("mLidKeyboardAccessibility=");
        pw.print(this.mLidKeyboardAccessibility);
        pw.print(" mLidNavigationAccessibility=");
        pw.print(this.mLidNavigationAccessibility);
        pw.print(" mLidControlsScreenLock=");
        pw.println(this.mLidControlsScreenLock);
        pw.print(" mLidControlsSleep=");
        pw.println(this.mLidControlsSleep);
        pw.print(prefix);
        pw.print(" mLongPressOnBackBehavior=");
        pw.println(this.mLongPressOnBackBehavior);
        pw.print(prefix);
        pw.print("mShortPressOnPowerBehavior=");
        pw.print(this.mShortPressOnPowerBehavior);
        pw.print(" mLongPressOnPowerBehavior=");
        pw.println(this.mLongPressOnPowerBehavior);
        pw.print(prefix);
        pw.print("mDoublePressOnPowerBehavior=");
        pw.print(this.mDoublePressOnPowerBehavior);
        pw.print(" mTriplePressOnPowerBehavior=");
        pw.println(this.mTriplePressOnPowerBehavior);
        pw.print(prefix);
        pw.print("mHasSoftInput=");
        pw.println(this.mHasSoftInput);
        pw.print(prefix);
        pw.print("mAwake=");
        pw.println(this.mAwake);
        pw.print(prefix);
        pw.print("mScreenOnEarly=");
        pw.print(this.mScreenOnEarly);
        pw.print(" mScreenOnFully=");
        pw.println(this.mScreenOnFully);
        pw.print(prefix);
        pw.print("mKeyguardDrawComplete=");
        pw.print(this.mKeyguardDrawComplete);
        pw.print(" mWindowManagerDrawComplete=");
        pw.println(this.mWindowManagerDrawComplete);
        pw.print(prefix);
        pw.print("mOrientationSensorEnabled=");
        pw.println(this.mOrientationSensorEnabled);
        pw.print(prefix);
        pw.print("mOverscanScreen=(");
        pw.print(this.mOverscanScreenLeft);
        pw.print(",");
        pw.print(this.mOverscanScreenTop);
        pw.print(") ");
        pw.print(this.mOverscanScreenWidth);
        pw.print("x");
        pw.println(this.mOverscanScreenHeight);
        if (!(this.mOverscanLeft == 0 && this.mOverscanTop == 0 && this.mOverscanRight == 0 && this.mOverscanBottom == 0)) {
            pw.print(prefix);
            pw.print("mOverscan left=");
            pw.print(this.mOverscanLeft);
            pw.print(" top=");
            pw.print(this.mOverscanTop);
            pw.print(" right=");
            pw.print(this.mOverscanRight);
            pw.print(" bottom=");
            pw.println(this.mOverscanBottom);
        }
        pw.print(prefix);
        pw.print("mRestrictedOverscanScreen=(");
        pw.print(this.mRestrictedOverscanScreenLeft);
        pw.print(",");
        pw.print(this.mRestrictedOverscanScreenTop);
        pw.print(") ");
        pw.print(this.mRestrictedOverscanScreenWidth);
        pw.print("x");
        pw.println(this.mRestrictedOverscanScreenHeight);
        pw.print(prefix);
        pw.print("mUnrestrictedScreen=(");
        pw.print(this.mUnrestrictedScreenLeft);
        pw.print(",");
        pw.print(this.mUnrestrictedScreenTop);
        pw.print(") ");
        pw.print(this.mUnrestrictedScreenWidth);
        pw.print("x");
        pw.println(this.mUnrestrictedScreenHeight);
        pw.print(prefix);
        pw.print("mRestrictedScreen=(");
        pw.print(this.mRestrictedScreenLeft);
        pw.print(",");
        pw.print(this.mRestrictedScreenTop);
        pw.print(") ");
        pw.print(this.mRestrictedScreenWidth);
        pw.print("x");
        pw.println(this.mRestrictedScreenHeight);
        pw.print(prefix);
        pw.print("mStableFullscreen=(");
        pw.print(this.mStableFullscreenLeft);
        pw.print(",");
        pw.print(this.mStableFullscreenTop);
        pw.print(")-(");
        pw.print(this.mStableFullscreenRight);
        pw.print(",");
        pw.print(this.mStableFullscreenBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mStable=(");
        pw.print(this.mStableLeft);
        pw.print(",");
        pw.print(this.mStableTop);
        pw.print(")-(");
        pw.print(this.mStableRight);
        pw.print(",");
        pw.print(this.mStableBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mSystem=(");
        pw.print(this.mSystemLeft);
        pw.print(",");
        pw.print(this.mSystemTop);
        pw.print(")-(");
        pw.print(this.mSystemRight);
        pw.print(",");
        pw.print(this.mSystemBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mCur=(");
        pw.print(this.mCurLeft);
        pw.print(",");
        pw.print(this.mCurTop);
        pw.print(")-(");
        pw.print(this.mCurRight);
        pw.print(",");
        pw.print(this.mCurBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mContent=(");
        pw.print(this.mContentLeft);
        pw.print(",");
        pw.print(this.mContentTop);
        pw.print(")-(");
        pw.print(this.mContentRight);
        pw.print(",");
        pw.print(this.mContentBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mVoiceContent=(");
        pw.print(this.mVoiceContentLeft);
        pw.print(",");
        pw.print(this.mVoiceContentTop);
        pw.print(")-(");
        pw.print(this.mVoiceContentRight);
        pw.print(",");
        pw.print(this.mVoiceContentBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mDock=(");
        pw.print(this.mDockLeft);
        pw.print(",");
        pw.print(this.mDockTop);
        pw.print(")-(");
        pw.print(this.mDockRight);
        pw.print(",");
        pw.print(this.mDockBottom);
        pw.println(")");
        pw.print(prefix);
        pw.print("mDockLayer=");
        pw.print(this.mDockLayer);
        pw.print(" mStatusBarLayer=");
        pw.println(this.mStatusBarLayer);
        pw.print(prefix);
        pw.print("mShowingDream=");
        pw.print(this.mShowingDream);
        pw.print(" mDreamingLockscreen=");
        pw.print(this.mDreamingLockscreen);
        pw.print(" mDreamingSleepToken=");
        pw.println(this.mDreamingSleepToken);
        if (this.mLastInputMethodWindow != null) {
            pw.print(prefix);
            pw.print("mLastInputMethodWindow=");
            pw.println(this.mLastInputMethodWindow);
        }
        if (this.mLastInputMethodTargetWindow != null) {
            pw.print(prefix);
            pw.print("mLastInputMethodTargetWindow=");
            pw.println(this.mLastInputMethodTargetWindow);
        }
        pw.print(prefix);
        pw.print("mDismissImeOnBackKeyPressed=");
        pw.println(this.mDismissImeOnBackKeyPressed);
        if (this.mStatusBar != null) {
            pw.print(prefix);
            pw.print("mStatusBar=");
            pw.print(this.mStatusBar);
            pw.print(" isStatusBarKeyguard=");
            pw.println(isStatusBarKeyguard());
        }
        if (this.mNavigationBar != null) {
            pw.print(prefix);
            pw.print("mNavigationBar=");
            pw.println(this.mNavigationBar);
        }
        if (this.mFocusedWindow != null) {
            pw.print(prefix);
            pw.print("mFocusedWindow=");
            pw.println(this.mFocusedWindow);
        }
        if (this.mFocusedApp != null) {
            pw.print(prefix);
            pw.print("mFocusedApp=");
            pw.println(this.mFocusedApp);
        }
        if (this.mTopFullscreenOpaqueWindowState != null) {
            pw.print(prefix);
            pw.print("mTopFullscreenOpaqueWindowState=");
            pw.println(this.mTopFullscreenOpaqueWindowState);
        }
        if (this.mTopFullscreenOpaqueOrDimmingWindowState != null) {
            pw.print(prefix);
            pw.print("mTopFullscreenOpaqueOrDimmingWindowState=");
            pw.println(this.mTopFullscreenOpaqueOrDimmingWindowState);
        }
        if (this.mForcingShowNavBar) {
            pw.print(prefix);
            pw.print("mForcingShowNavBar=");
            pw.println(this.mForcingShowNavBar);
            pw.print("mForcingShowNavBarLayer=");
            pw.println(this.mForcingShowNavBarLayer);
        }
        pw.print(prefix);
        pw.print("mTopIsFullscreen=");
        pw.print(this.mTopIsFullscreen);
        pw.print(" mKeyguardOccluded=");
        pw.println(this.mKeyguardOccluded);
        pw.print(" mKeyguardOccludedChanged=");
        pw.println(this.mKeyguardOccludedChanged);
        pw.print(" mPendingKeyguardOccluded=");
        pw.println(this.mPendingKeyguardOccluded);
        pw.print(prefix);
        pw.print("mForceStatusBar=");
        pw.print(this.mForceStatusBar);
        pw.print(" mForceStatusBarFromKeyguard=");
        pw.println(this.mForceStatusBarFromKeyguard);
        pw.print(prefix);
        pw.print("mHomePressed=");
        pw.println(this.mHomePressed);
        pw.print(prefix);
        pw.print("mAllowLockscreenWhenOn=");
        pw.print(this.mAllowLockscreenWhenOn);
        pw.print(" mLockScreenTimeout=");
        pw.print(this.mLockScreenTimeout);
        pw.print(" mLockScreenTimerActive=");
        pw.println(this.mLockScreenTimerActive);
        pw.print(prefix);
        pw.print("mEndcallBehavior=");
        pw.print(this.mEndcallBehavior);
        pw.print(" mIncallPowerBehavior=");
        pw.print(this.mIncallPowerBehavior);
        pw.print(" mIncallBackBehavior=");
        pw.print(this.mIncallBackBehavior);
        pw.print(" mLongPressOnHomeBehavior=");
        pw.println(this.mLongPressOnHomeBehavior);
        pw.print(prefix);
        pw.print("mLandscapeRotation=");
        pw.print(this.mLandscapeRotation);
        pw.print(" mSeascapeRotation=");
        pw.println(this.mSeascapeRotation);
        pw.print(prefix);
        pw.print("mPortraitRotation=");
        pw.print(this.mPortraitRotation);
        pw.print(" mUpsideDownRotation=");
        pw.println(this.mUpsideDownRotation);
        pw.print(prefix);
        pw.print("mDemoHdmiRotation=");
        pw.print(this.mDemoHdmiRotation);
        pw.print(" mDemoHdmiRotationLock=");
        pw.println(this.mDemoHdmiRotationLock);
        pw.print(prefix);
        pw.print("mUndockedHdmiRotation=");
        pw.println(this.mUndockedHdmiRotation);
        if (this.mHasFeatureLeanback) {
            pw.print(prefix);
            pw.print("mAccessibilityTvKey1Pressed=");
            pw.println(this.mAccessibilityTvKey1Pressed);
            pw.print(prefix);
            pw.print("mAccessibilityTvKey2Pressed=");
            pw.println(this.mAccessibilityTvKey2Pressed);
            pw.print(prefix);
            pw.print("mAccessibilityTvScheduled=");
            pw.println(this.mAccessibilityTvScheduled);
        }
        this.mGlobalKeyManager.dump(prefix, pw);
        this.mStatusBarController.dump(pw, prefix);
        this.mNavigationBarController.dump(pw, prefix);
        PolicyControl.dump(prefix, pw);
        if (this.mWakeGestureListener != null) {
            this.mWakeGestureListener.dump(pw, prefix);
        }
        if (this.mOrientationListener != null) {
            this.mOrientationListener.dump(pw, prefix);
        }
        if (this.mBurnInProtectionHelper != null) {
            this.mBurnInProtectionHelper.dump(prefix, pw);
        }
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.dump(prefix, pw);
        }
    }

    protected void cancelPendingPowerKeyActionForDistouch() {
        cancelPendingPowerKeyAction();
    }

    public boolean isTopIsFullscreen() {
        return this.mTopIsFullscreen;
    }

    public boolean isLastImmersiveMode() {
        return isImmersiveMode(this.mLastSystemUiFlags);
    }

    protected BarController getStatusBarController() {
        return this.mStatusBarController;
    }

    protected ImmersiveModeConfirmation getImmersiveModeConfirmation() {
        return this.mImmersiveModeConfirmation;
    }

    protected void updateHwSystemUiVisibilityLw() {
        updateSystemUiVisibilityLw();
    }

    protected void requestHwTransientBars(WindowState swipeTarget) {
        requestTransientBars(swipeTarget);
    }

    protected WindowState getCurrentWin() {
        if (this.mFocusedWindow != null) {
            return this.mFocusedWindow;
        }
        return this.mTopFullscreenOpaqueWindowState;
    }

    protected void hwInit() {
    }

    protected int getEmuiStyleValue(int styleValue) {
        return styleValue;
    }

    protected boolean getFloatingValue(int styleValue) {
        return false;
    }

    public boolean startAodService(int startState) {
        Slog.i(TAG, "AOD startAodService mAodSwitch=" + this.mAodSwitch + " mAodTimerSwitch=" + this.mAodTimerSwitch + " mAODState=" + this.mAODState + " startState=" + startState);
        if (!mSupportAod) {
            return false;
        }
        if (this.mAodSwitch == 0 && this.mAodTimerSwitch == 0) {
            this.mAODState = startState;
            return false;
        } else if (this.mAODState == startState) {
            return false;
        } else {
            this.mAODState = startState;
            this.mHandler.post(new Runnable() {
                public void run() {
                    Intent intent = null;
                    switch (PhoneWindowManager.this.mAODState) {
                        case 1:
                            intent = new Intent("com.huawei.aod.action.AOD_SERVICE_START");
                            break;
                        case 2:
                            if (PhoneWindowManager.this.mIAodStateCallback == null) {
                                intent = new Intent("com.huawei.aod.action.AOD_SCREEN_ON");
                                break;
                            }
                            try {
                                PhoneWindowManager.this.mIAodStateCallback.onScreenOn();
                                break;
                            } catch (RemoteException e) {
                                break;
                            }
                        case 3:
                            if (PhoneWindowManager.this.mIAodStateCallback == null) {
                                intent = new Intent("com.huawei.aod.action.AOD_SCREEN_OFF");
                                break;
                            }
                            try {
                                PhoneWindowManager.this.mIAodStateCallback.onScreenOff();
                                break;
                            } catch (RemoteException e2) {
                                break;
                            }
                        case 4:
                            if (PhoneWindowManager.this.mIAodStateCallback != null) {
                                try {
                                    PhoneWindowManager.this.mIAodStateCallback.onWakingUp();
                                    break;
                                } catch (RemoteException e3) {
                                    break;
                                }
                            }
                            break;
                        case 5:
                            if (PhoneWindowManager.this.mIAodStateCallback != null) {
                                try {
                                    PhoneWindowManager.this.mIAodStateCallback.onTurningOn();
                                    break;
                                } catch (RemoteException e4) {
                                    break;
                                }
                            }
                            break;
                        case 6:
                            intent = new Intent("com.huawei.aod.action.AOD_SCREEN_OFF");
                            break;
                    }
                    if (intent != null) {
                        intent.setComponent(new ComponentName("com.huawei.aod", "com.huawei.aod.AODService"));
                        PhoneWindowManager.this.mContext.startService(intent);
                    }
                }
            });
            return true;
        }
    }

    public void regeditAodStateCallback(IAodStateCallback callback) {
        Slog.i(TAG, "AOD regeditAodStateCallback ");
        if (mSupportAod) {
            this.mIAodStateCallback = callback;
            try {
                callback.asBinder().linkToDeath(new AppDeathRecipient(), 0);
            } catch (RemoteException e) {
            }
        }
    }

    public void unregeditAodStateCallback(IAodStateCallback callback) {
        Slog.i(TAG, "AOD unregeditAodStateCallback ");
        if (mSupportAod) {
            this.mIAodStateCallback = null;
        }
    }

    private long printTimeoutLog(String functionName, long startTime, String type, int timeout) {
        long endTime = SystemClock.elapsedRealtime();
        if (endTime - startTime > ((long) timeout)) {
            Log.i(TAG, "" + functionName + " " + type + " duration: " + (endTime - startTime));
        }
        return endTime;
    }

    public boolean isKeyguardShowingOrOccluded() {
        return this.mKeyguardDelegate == null ? false : this.mKeyguardDelegate.isShowing();
    }
}
