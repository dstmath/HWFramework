package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.app.IUiModeManager;
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
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.input.InputManager;
import android.hardware.input.InputManagerInternal;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.media.AudioAttributes;
import android.media.AudioManagerInternal;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.session.MediaSessionLegacyHelper;
import android.net.util.NetworkConstants;
import android.os.Binder;
import android.os.Bundle;
import android.os.FactoryTest;
import android.os.Handler;
import android.os.IAodStateCallback;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.service.dreams.DreamManagerInternal;
import android.service.dreams.IDreamManager;
import android.service.vr.IPersistentVrStateCallbacks;
import android.telecom.TelecomManager;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.MutableBoolean;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.IApplicationToken;
import android.view.IHwRotateObserver;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.autofill.AutofillManagerInternal;
import android.view.inputmethod.InputMethodManagerInternal;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.accessibility.AccessibilityShortcutController;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IShortcutService;
import com.android.internal.policy.KeyguardDismissCallback;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.ScreenShapeHelper;
import com.android.internal.util.ScreenshotHelper;
import com.android.internal.widget.PointerLocationView;
import com.android.server.GestureLauncherService;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.NetworkManagementService;
import com.android.server.NsdService;
import com.android.server.SystemServiceManager;
import com.android.server.UiModeManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.audio.AudioService;
import com.android.server.display.DisplayTransformManager;
import com.android.server.lights.LightsManager;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.policy.BarController;
import com.android.server.policy.SystemGesturesPointerEventListener;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.android.server.policy.keyguard.KeyguardStateMonitor;
import com.android.server.power.IHwShutdownThread;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.usb.descriptors.UsbDescriptor;
import com.android.server.vr.VrManagerInternal;
import com.android.server.wm.AppTransition;
import com.android.server.wm.DisplayFrames;
import com.android.server.wm.WindowManagerInternal;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneWindowManager extends AbsPhoneWindowManager implements WindowManagerPolicy, IHwPhoneWindowManagerInner {
    private static final String ACTION_ACTURAL_SHUTDOWN = "com.android.internal.app.SHUTDOWNBROADCAST";
    public static final String ACTIVITY_NAME_EMERGENCY_COUNTDOWN = "com.android.emergency/.view.ViewCountDownActivity";
    public static final String ACTIVITY_NAME_EMERGENCY_NUMBER = "com.android.emergency/.view.EmergencyNumberActivity";
    static final boolean ALTERNATE_CAR_MODE_NAV_SIZE = false;
    private static final int BRIGHTNESS_STEPS = 10;
    private static final long BUGREPORT_TV_GESTURE_TIMEOUT_MILLIS = 1000;
    static final boolean DEBUG = false;
    static final boolean DEBUG_INPUT = false;
    static final boolean DEBUG_KEYGUARD = false;
    static final boolean DEBUG_LAYOUT = false;
    static final boolean DEBUG_SPLASH_SCREEN = false;
    static final boolean DEBUG_VOLUME_KEY = true;
    static final boolean DEBUG_WAKEUP = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int DEFAULT_ROTATION = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    public static final long DELAY_LAUNCH_WALLET_TIME = 500;
    static final boolean DISABLE_VOLUME_HUSH = true;
    private static final int DISPALY_SCREEN_OFF = 7;
    private static final int DISPALY_SCREEN_ON = 6;
    static final int DOUBLE_TAP_HOME_NOTHING = 0;
    static final int DOUBLE_TAP_HOME_RECENT_SYSTEM_UI = 1;
    static final boolean ENABLE_DESK_DOCK_HOME_CAPTURE = false;
    static final boolean ENABLE_VR_HEADSET_HOME_CAPTURE = true;
    private static final String FINGER_PRINT_ENABLE = "fp_keyguard_enable";
    private static final String FOCUSED_SPLIT_APP_ACTIVITY = "com.huawei.android.launcher/.splitscreen.SplitScreenAppActivity";
    private static final int FP_SWITCH_OFF = 0;
    private static final int FP_SWITCH_ON = 1;
    /* access modifiers changed from: private */
    public static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final String HUAWEI_PRE_CAMERA_START_MODE = "com.huawei.RapidCapture";
    private static final String HUAWEI_SHUTDOWN_PERMISSION = "huawei.android.permission.HWSHUTDOWN";
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    public static final int HW_ROTATE_APP_OPT_ENABLED = 1;
    public static final int HW_STARTWINDOW_OPT_ENABLED = 0;
    private static final int INVALID_HARDWARE_TYPE = -1;
    private static final int IN_SCREEN_OPTIC_TYPE = 1;
    private static final int IN_SCREEN_ULTRA_TYPE = 2;
    protected static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS).equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
    private static final boolean IS_lOCK_UNNATURAL_ORIENTATION = SystemProperties.getBoolean("ro.config.lock_land_screen", false);
    private static final float KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER = 2.5f;
    static final int LAST_LONG_PRESS_HOME_BEHAVIOR = 2;
    static final int LONG_PRESS_BACK_GO_TO_VOICE_ASSIST = 1;
    static final int LONG_PRESS_BACK_NOTHING = 0;
    static final int LONG_PRESS_HOME_ALL_APPS = 1;
    static final int LONG_PRESS_HOME_ASSIST = 2;
    static final int LONG_PRESS_HOME_NOTHING = 0;
    static final int LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
    static final int LONG_PRESS_POWER_GO_TO_VOICE_ASSIST = 4;
    static final int LONG_PRESS_POWER_NOTHING = 0;
    static final int LONG_PRESS_POWER_SHUT_OFF = 2;
    static final int LONG_PRESS_POWER_SHUT_OFF_NO_CONFIRM = 3;
    private static final int MSG_ACCESSIBILITY_SHORTCUT = 20;
    private static final int MSG_ACCESSIBILITY_TV = 22;
    private static final int MSG_BACK_LONG_PRESS = 18;
    private static final int MSG_BUGREPORT_TV = 21;
    private static final int MSG_DISABLE_POINTER_LOCATION = 2;
    private static final int MSG_DISPATCH_BACK_KEY_TO_AUTOFILL = 23;
    private static final int MSG_DISPATCH_MEDIA_KEY_REPEAT_WITH_WAKE_LOCK = 4;
    private static final int MSG_DISPATCH_MEDIA_KEY_WITH_WAKE_LOCK = 3;
    private static final int MSG_DISPATCH_SHOW_GLOBAL_ACTIONS = 10;
    private static final int MSG_DISPATCH_SHOW_RECENTS = 9;
    private static final int MSG_DISPOSE_INPUT_CONSUMER = 19;
    private static final int MSG_ENABLE_POINTER_LOCATION = 1;
    private static final int MSG_HANDLE_ALL_APPS = 25;
    private static final int MSG_HIDE_BOOT_MESSAGE = 11;
    private static final int MSG_KEYGUARD_DRAWN_COMPLETE = 5;
    private static final int MSG_KEYGUARD_DRAWN_TIMEOUT = 6;
    private static final int MSG_LAUNCH_ASSIST = 26;
    private static final int MSG_LAUNCH_ASSIST_LONG_PRESS = 27;
    private static final int MSG_LAUNCH_VOICE_ASSIST_WITH_WAKE_LOCK = 12;
    private static final int MSG_MENU_SCREEN_DELAY_TME = 15000;
    private static final int MSG_MENU_SCREEN_REMIND = 31;
    private static final int MSG_NOTIFY_USER_ACTIVITY = 29;
    private static final int MSG_POWER_DELAYED_PRESS = 13;
    private static final int MSG_POWER_LONG_PRESS = 14;
    private static final int MSG_POWER_VERY_LONG_PRESS = 28;
    private static final int MSG_REQUEST_TRANSIENT_BARS = 16;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_NAVIGATION = 1;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_STATUS = 0;
    private static final int MSG_RINGER_TOGGLE_CHORD = 30;
    private static final int MSG_SHOW_PICTURE_IN_PICTURE_MENU = 17;
    private static final int MSG_SYSTEM_KEY_PRESS = 24;
    private static final int MSG_UPDATE_DREAMING_SLEEP_TOKEN = 15;
    private static final int MSG_WINDOW_MANAGER_DRAWN_COMPLETE = 7;
    static final int MULTI_PRESS_POWER_BRIGHTNESS_BOOST = 2;
    static final int MULTI_PRESS_POWER_HUAWEI_PAY = 3;
    static final int MULTI_PRESS_POWER_NOTHING = 0;
    static final int MULTI_PRESS_POWER_THEATER_MODE = 1;
    static final int NAV_BAR_OPAQUE_WHEN_FREEFORM_OR_DOCKED = 0;
    static final int NAV_BAR_TRANSLUCENT_WHEN_FREEFORM_OPAQUE_OTHERWISE = 1;
    public static final String NAV_TAG = "NavigationBar";
    private static final String NEED_START_AOD_WHEN_SCREEN_OFF = "needStartAODWhenScreenOff";
    private static final int NaviHide = 1;
    private static final int NaviInit = -1;
    private static final int NaviShow = 0;
    private static final int NaviTransientShow = 2;
    private static final long PANIC_GESTURE_EXPIRATION = 30000;
    static final int PENDING_KEY_NULL = -1;
    private static final int POWERDOWN_MAX_TIMEOUT = 200;
    private static final int POWER_PICKUP_TO_PAUSE_AOD = 5;
    private static final int POWER_SCREENOF_AFTER_TURNINGON = 9;
    private static final int POWER_STARTED_GOING_TO_SLEEP = 1;
    private static final int POWER_STARTED_WAKING_UP = 0;
    static final boolean PRINT_ANIM = false;
    public static final int ROTATION_LANDACAPE_DEF = 0;
    public static final int ROTATION_LANDACAPE_OTHER = 1;
    public static final int ROTATION_PORTRAIT_DEF = 2;
    public static final int ROTATION_PORTRAIT_OTHER = 3;
    private static final long SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS = 150;
    private static final int SCREENSHOT_DELAY = 0;
    private static final String SCREEN_SHOT_VENT_NAME = "com.huawei.screenshot.intent.action.KeyScreenshot";
    private static final String SEC_IME_PACKAGE = "com.huawei.secime";
    private static final int SEC_IME_RAISE_HEIGHT = SystemProperties.getInt("ro.config.sec_ime_raise_height_dp", 140);
    private static final int SET_FINGER_INIT_STATUS = 8;
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
    private static final String SINGLE_VIRTAL_NAVBAR = "ai_navigationbar";
    private static final String SINGLE_VIRTAL_NAVBAR_SWITCH = "ai_enable";
    public static final int START_AOD_BOOT = 1;
    public static final int START_AOD_GOING_TO_SLEEP = 7;
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
    public static final String SYSTEM_DIALOG_REASON_SCREENSHOT = "screenshot";
    static final int SYSTEM_UI_CHANGING_LAYOUT = -1073709042;
    private static final String SYSUI_PACKAGE = "com.android.systemui";
    private static final String SYSUI_SCREENSHOT_ERROR_RECEIVER = "com.android.systemui.screenshot.ScreenshotServiceErrorReceiver";
    private static final String SYSUI_SCREENSHOT_SERVICE = "com.android.systemui.screenshot.TakeScreenshotService";
    static final String TAG = "WindowManager";
    public static final int TOAST_WINDOW_TIMEOUT = 3500;
    private static final int USER_ACTIVITY_NOTIFICATION_DELAY = 200;
    static final int VERY_LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
    static final int VERY_LONG_PRESS_POWER_NOTHING = 0;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    static final int WAITING_FOR_DRAWN_TIMEOUT = 1000;
    static final int WAITING_FOR_KEYGUARD_DISMISS_TIMEOUT = 300;
    private static final int[] WINDOW_TYPES_WHERE_HOME_DOESNT_WORK = {2003, 2010};
    /* access modifiers changed from: private */
    public static boolean bHasFrontFp = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    static final int deviceGlobalActionKeyTimeout = 3000;
    private static boolean isBoPD = SystemProperties.getBoolean("sys.bopd", false);
    static final boolean localLOGV = false;
    private static boolean mIsTablet = "tablet".equals(SystemProperties.get("ro.build.characteristics", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
    public static boolean mSupporInputMethodFilletAdaptation = SystemProperties.getBoolean("ro.config.support_inputmethod_fillet_adaptation", false);
    /* access modifiers changed from: private */
    public static final boolean mSupportAod = "1".equals(SystemProperties.get("ro.config.support_aod", null));
    static final Rect mTmpContentFrame = new Rect();
    static final Rect mTmpDecorFrame = new Rect();
    private static final Rect mTmpDisplayCutoutSafeExceptMaybeBarsRect = new Rect();
    static final Rect mTmpDisplayFrame = new Rect();
    static final Rect mTmpNavigationFrame = new Rect();
    static final Rect mTmpOutsetFrame = new Rect();
    static final Rect mTmpOverscanFrame = new Rect();
    static final Rect mTmpParentFrame = new Rect();
    private static final Rect mTmpRect = new Rect();
    static final Rect mTmpStableFrame = new Rect();
    static final Rect mTmpVisibleFrame = new Rect();
    private static boolean mUsingHwNavibar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    public static final long notifyWalletInterval = 1000;
    static SparseArray<String> sApplicationLaunchKeyCategories = new SparseArray<>();
    private static final boolean sIsChinaRegion = "CN".equals(SystemProperties.get("ro.product.locale.region", "CN"));
    private static final boolean sIsChineseLanguage = "zh".equals(SystemProperties.get("ro.product.locale.language", "zh"));
    private static boolean sIsLayaPorschePorduct = false;
    private static boolean sIsPorscheProduct = SystemProperties.getBoolean("ro.config.pd_font_enable", false);
    private static String sProductBrandString = SystemProperties.get("ro.product.board", "UNKOWN");
    private static final String sProximityWndName = "Emui:ProximityWnd";
    boolean ifBootMessageShowing;
    private boolean isNeedNotifyWallet = false;
    private boolean mA11yShortcutChordVolumeUpKeyConsumed;
    private long mA11yShortcutChordVolumeUpKeyTime;
    private boolean mA11yShortcutChordVolumeUpKeyTriggered;
    private int mAODState;
    AccessibilityManager mAccessibilityManager;
    /* access modifiers changed from: private */
    public AccessibilityShortcutController mAccessibilityShortcutController;
    private boolean mAccessibilityTvKey1Pressed;
    private boolean mAccessibilityTvKey2Pressed;
    private boolean mAccessibilityTvScheduled;
    private final Runnable mAcquireSleepTokenRunnable;
    ActivityManagerInternal mActivityManagerInternal;
    int mAllowAllRotations = -1;
    boolean mAllowLockscreenWhenOn;
    boolean mAllowStartActivityForLongPressOnPowerDuringSetup;
    private boolean mAllowTheaterModeWakeFromCameraLens;
    private boolean mAllowTheaterModeWakeFromKey;
    private boolean mAllowTheaterModeWakeFromLidSwitch;
    private boolean mAllowTheaterModeWakeFromMotion;
    private boolean mAllowTheaterModeWakeFromMotionWhenNotDreaming;
    private boolean mAllowTheaterModeWakeFromPowerKey;
    /* access modifiers changed from: private */
    public boolean mAllowTheaterModeWakeFromWakeGesture;
    private boolean mAodShowing;
    /* access modifiers changed from: private */
    public int mAodSwitch;
    /* access modifiers changed from: private */
    public AodSwitchObserver mAodSwitchObserver;
    AppOpsManager mAppOpsManager;
    AudioManagerInternal mAudioManagerInternal;
    AutofillManagerInternal mAutofillManagerInternal;
    volatile boolean mAwake;
    volatile boolean mBackKeyHandled;
    volatile boolean mBeganFromNonInteractive;
    boolean mBootMessageNeedsHiding;
    ProgressDialog mBootMsgDialog;
    PowerManager.WakeLock mBroadcastWakeLock;
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
    /* access modifiers changed from: private */
    public final Runnable mClearHideNavigationFlag;
    boolean mConsumeSearchKeyUp;
    Context mContext;
    int mCurrentAppOrientation = -1;
    protected int mCurrentUserId;
    private HwCustPhoneWindowManager mCust = ((HwCustPhoneWindowManager) HwCustUtils.createObj(HwCustPhoneWindowManager.class, new Object[0]));
    public int mDefaultNavBarHeight;
    int mDemoHdmiRotation;
    boolean mDemoHdmiRotationLock;
    int mDemoRotation;
    boolean mDemoRotationLock;
    boolean mDeskDockEnablesAccelerometer;
    Intent mDeskDockIntent;
    int mDeskDockRotation;
    private int mDeviceNodeFD;
    private volatile boolean mDismissImeOnBackKeyPressed;
    Display mDisplay;
    int mDisplayRotation;
    int mDockLayer;
    int mDockMode = 0;
    BroadcastReceiver mDockReceiver;
    final Rect mDockedStackBounds = new Rect();
    int mDoublePressOnPowerBehavior;
    private int mDoubleTapOnHomeBehavior;
    DreamManagerInternal mDreamManagerInternal;
    BroadcastReceiver mDreamReceiver;
    boolean mDreamingLockscreen;
    ActivityManagerInternal.SleepToken mDreamingSleepToken;
    boolean mDreamingSleepTokenNeeded;
    private boolean mEnableCarDockHomeCapture = true;
    boolean mEnableShiftMenuBugReports = false;
    volatile boolean mEndCallKeyHandled;
    private final Runnable mEndCallLongPress;
    int mEndcallBehavior;
    private final SparseArray<KeyCharacterMap.FallbackAction> mFallbackActions = new SparseArray<>();
    private int mFingerprintType = -1;
    /* access modifiers changed from: private */
    public boolean mFirstIncall;
    IApplicationToken mFocusedApp;
    WindowManagerPolicy.WindowState mFocusedWindow;
    int mForceClearedSystemUiFlags = 0;
    private boolean mForceDefaultOrientation = false;
    protected boolean mForceNotchStatusBar = false;
    boolean mForceShowSystemBars;
    boolean mForceStatusBar;
    boolean mForceStatusBarFromKeyguard;
    private boolean mForceStatusBarTransparent;
    WindowManagerPolicy.WindowState mForceStatusBarTransparentWin;
    boolean mForcingShowNavBar;
    int mForcingShowNavBarLayer;
    private boolean mFrozen;
    GlobalActions mGlobalActions;
    private GlobalKeyManager mGlobalKeyManager;
    private boolean mGoToSleepOnButtonPressTheaterMode;
    volatile boolean mGoingToSleep;
    private UEventObserver mHDMIObserver = new UEventObserver() {
        public void onUEvent(UEventObserver.UEvent event) {
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
    private final Runnable mHiddenNavPanic;
    boolean mHomeConsumed;
    boolean mHomeDoubleTapPending;
    private final Runnable mHomeDoubleTapTimeoutRunnable;
    Intent mHomeIntent;
    boolean mHomePressed;
    private boolean mHwFullScreenWinVisibility = false;
    private WindowManagerPolicy.WindowState mHwFullScreenWindow = null;
    /* access modifiers changed from: private */
    public boolean mHwNavColor;
    IHwPhoneWindowManagerEx mHwPWMEx;
    private RemoteCallbackList<IHwRotateObserver> mHwRotateObservers = new RemoteCallbackList<>();
    IAodStateCallback mIAodStateCallback;
    private ImmersiveModeConfirmation mImmersiveModeConfirmation;
    boolean mImmersiveStatusChanged;
    int mIncallBackBehavior;
    int mIncallPowerBehavior;
    int mInitialMetaState;
    WindowManagerPolicy.InputConsumer mInputConsumer = null;
    protected InputManagerInternal mInputManagerInternal;
    InputMethodManagerInternal mInputMethodManagerInternal;
    public boolean mInputMethodMovedUp;
    private WindowManagerPolicy.WindowState mInputMethodTarget;
    protected boolean mInterceptInputForWaitBrightness = false;
    /* access modifiers changed from: private */
    public boolean mIsActuralShutDown = false;
    private boolean mIsFingerprintEnabledBySettings;
    public boolean mIsFloatIME;
    protected boolean mIsNoneNotchAppInHideMode = false;
    protected boolean mIsNotchSwitchOpen = false;
    private boolean mKeyguardBound;
    KeyguardServiceDelegate mKeyguardDelegate;
    final Runnable mKeyguardDismissDoneCallback = new Runnable() {
        public void run() {
            Slog.i(PhoneWindowManager.TAG, "keyguard dismiss done!");
            PhoneWindowManager.this.finishKeyguardDismissDone();
        }
    };
    WindowManagerPolicy.KeyguardDismissDoneListener mKeyguardDismissListener;
    boolean mKeyguardDrawComplete;
    final KeyguardServiceDelegate.DrawnListener mKeyguardDrawnCallback = new KeyguardServiceDelegate.DrawnListener() {
        public void onDrawn() {
            Slog.d(PhoneWindowManager.TAG, "UL_Power mKeyguardDelegate.ShowListener.onDrawn.");
            if (PhoneWindowManager.this.mFirstIncall || !PhoneWindowManager.this.isSupportCover() || !PhoneWindowManager.this.isSmartCoverMode() || HwFrameworkFactory.getCoverManager().isCoverOpen() || !PhoneWindowManager.this.isInCallActivity()) {
                PhoneWindowManager.this.mHandler.sendEmptyMessage(5);
                return;
            }
            boolean unused = PhoneWindowManager.this.mFirstIncall = true;
            PhoneWindowManager.this.mHandler.sendEmptyMessageDelayed(5, 300);
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
    private boolean mLastHwNavColor;
    WindowManagerPolicy.WindowState mLastInputMethodTargetWindow = null;
    WindowManagerPolicy.WindowState mLastInputMethodWindow = null;
    int mLastNaviStatus = -1;
    final Rect mLastNonDockedStackBounds = new Rect();
    public long mLastNotifyWalletTime;
    int mLastShowNaviDockBottom = 0;
    private boolean mLastShowingDream;
    private WindowManagerPolicy.WindowState mLastStartingWindow;
    int mLastSystemUiFlags;
    int mLastSystemUiFlagsTmp;
    int mLastTransientNaviDockBottom = 0;
    private boolean mLastWindowSleepTokenNeeded;
    protected boolean mLayoutBeyondDisplayCutout = false;
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
    MetricsLogger mLogger;
    int mLongPressOnBackBehavior;
    private int mLongPressOnHomeBehavior;
    int mLongPressOnPowerBehavior;
    long[] mLongPressVibePattern;
    int mMetaState;
    BroadcastReceiver mMultiuserReceiver;
    int mNavBarOpacityMode = 0;
    volatile boolean mNavBarVirtualKeyHapticFeedbackEnabled = true;
    private final BarController.OnBarVisibilityChangedListener mNavBarVisibilityListener;
    WindowManagerPolicy.WindowState mNavigationBar = null;
    boolean mNavigationBarCanMove = false;
    /* access modifiers changed from: private */
    public final BarController mNavigationBarController;
    int[] mNavigationBarHeightForRotationDefault = new int[4];
    int[] mNavigationBarHeightForRotationInCarMode = new int[4];
    int mNavigationBarPosition = 4;
    int[] mNavigationBarWidthForRotationDefault = new int[4];
    int[] mNavigationBarWidthForRotationInCarMode = new int[4];
    private boolean mNeedPauseAodWhileSwitchUser;
    final Rect mNonDockedStackBounds = new Rect();
    private boolean mNotifyUserActivity;
    MyOrientationListener mOrientationListener;
    boolean mOrientationSensorEnabled = false;
    boolean mPendingCapsLockToggle;
    private boolean mPendingKeyguardOccluded;
    boolean mPendingMetaAction;
    /* access modifiers changed from: private */
    public long mPendingPanicGestureUptime;
    volatile int mPendingWakeKey = -1;
    /* access modifiers changed from: private */
    public volatile boolean mPersistentVrModeEnabled;
    final IPersistentVrStateCallbacks mPersistentVrModeListener = new IPersistentVrStateCallbacks.Stub() {
        public void onPersistentVrStateChanged(boolean enabled) {
            boolean unused = PhoneWindowManager.this.mPersistentVrModeEnabled = enabled;
            Log.d(PhoneWindowManager.TAG, "onPersistentVrStateChanged enabled:" + PhoneWindowManager.this.mPersistentVrModeEnabled);
        }
    };
    /* access modifiers changed from: private */
    public boolean mPickUpFlag;
    volatile boolean mPictureInPictureVisible;
    int mPointerLocationMode = 0;
    PointerLocationView mPointerLocationView;
    int mPortraitRotation = 0;
    volatile boolean mPowerKeyHandled;
    volatile boolean mPowerKeyHandledByHiaction;
    volatile int mPowerKeyPressCounter;
    PowerManager.WakeLock mPowerKeyWakeLock;
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    volatile boolean mPowerOffToastShown;
    boolean mPreloadedRecentApps;
    int mRecentAppsHeldModifiers;
    volatile boolean mRecentsVisible;
    private final Runnable mReleaseSleepTokenRunnable;
    volatile boolean mRequestedOrGoingToSleep;
    int mResettingSystemUiFlags = 0;
    int mRestrictedScreenHeight;
    private int mRingerToggleChord = 0;
    boolean mSafeMode;
    long[] mSafeModeEnabledVibePattern;
    private final ArraySet<WindowManagerPolicy.WindowState> mScreenDecorWindows = new ArraySet<>();
    ScreenLockTimeout mScreenLockTimeout;
    protected int mScreenOffReason = -1;
    ActivityManagerInternal.SleepToken mScreenOffSleepToken;
    boolean mScreenOnEarly;
    boolean mScreenOnFully;
    WindowManagerPolicy.ScreenOnListener mScreenOnListener;
    protected int mScreenRotation = 0;
    private boolean mScreenshotChordEnabled;
    private long mScreenshotChordPowerKeyTime;
    private boolean mScreenshotChordPowerKeyTriggered;
    private boolean mScreenshotChordVolumeDownKeyConsumed;
    private long mScreenshotChordVolumeDownKeyTime;
    private boolean mScreenshotChordVolumeDownKeyTriggered;
    /* access modifiers changed from: private */
    public ScreenshotHelper mScreenshotHelper;
    private final ScreenshotRunnable mScreenshotRunnable;
    boolean mSearchKeyShortcutPending;
    SearchManager mSearchManager;
    int mSeascapeRotation = 0;
    final Object mServiceAquireLock = new Object();
    SettingsObserver mSettingsObserver;
    int mShortPressOnPowerBehavior;
    int mShortPressOnSleepBehavior;
    int mShortPressOnWindowBehavior;
    private LongSparseArray<IShortcutService> mShortcutKeyServices = new LongSparseArray<>();
    ShortcutManager mShortcutManager;
    int mShowRotationSuggestions;
    boolean mShowingDream;
    BroadcastReceiver mShutdownReceiver;
    WindowManagerPolicy.WindowState mStatusBar = null;
    private final StatusBarController mStatusBarController = new StatusBarController();
    private final int[] mStatusBarHeightForRotation = new int[4];
    int mStatusBarLayer;
    StatusBarManagerInternal mStatusBarManagerInternal;
    IStatusBarService mStatusBarService;
    boolean mSupportAutoRotation;
    private boolean mSupportLongPressPowerWhenNonInteractive;
    private boolean mSyncPowerStateFlag;
    boolean mSystemBooted;
    @VisibleForTesting
    SystemGesturesPointerEventListener mSystemGestures;
    boolean mSystemNavigationKeysEnabled;
    boolean mSystemReady;
    private final MutableBoolean mTmpBoolean = new MutableBoolean(false);
    WindowManagerPolicy.WindowState mTopDockedOpaqueOrDimmingWindowState;
    WindowManagerPolicy.WindowState mTopDockedOpaqueWindowState;
    WindowManagerPolicy.WindowState mTopFullscreenOpaqueOrDimmingWindowState;
    WindowManagerPolicy.WindowState mTopFullscreenOpaqueWindowState;
    boolean mTopIsFullscreen;
    protected WindowManagerPolicy.TpKeepListener mTpKeepListener = null;
    boolean mTranslucentDecorEnabled = true;
    int mTriplePressOnPowerBehavior;
    int mUiMode;
    IUiModeManager mUiModeManager;
    int mUndockedHdmiRotation;
    int mUpsideDownRotation = 0;
    boolean mUseTvRouting;
    int mUserRotation = 0;
    int mUserRotationMode = 0;
    int mVeryLongPressOnPowerBehavior;
    int mVeryLongPressTimeout;
    Vibrator mVibrator;
    Intent mVrHeadsetHomeIntent;
    volatile VrManagerInternal mVrManagerInternal;
    boolean mWakeGestureEnabledSetting;
    MyWakeGestureListener mWakeGestureListener;
    IWindowManager mWindowManager;
    final Runnable mWindowManagerDrawCallback = new Runnable() {
        public void run() {
            Slog.i(PhoneWindowManager.TAG, "UL_Power All windows ready for display!");
            PhoneWindowManager.this.mHandler.sendEmptyMessage(7);
        }
    };
    boolean mWindowManagerDrawComplete;
    WindowManagerPolicy.WindowManagerFuncs mWindowManagerFuncs;
    WindowManagerInternal mWindowManagerInternal;
    @GuardedBy("mHandler")
    private ActivityManagerInternal.SleepToken mWindowSleepToken;
    private boolean mWindowSleepTokenNeeded;
    protected int notchStatusBarColorLw = 0;
    boolean notchWindowChange = false;
    boolean notchWindowChangeState = false;
    /* access modifiers changed from: private */
    public int otaStateForOverSeaPdLaya;
    private int splitNavigationBarDp;

    class AodSwitchObserver extends ContentObserver {
        AodSwitchObserver(Handler handler) {
            super(handler);
            int unused = PhoneWindowManager.this.mAodSwitch = getAodSwitch();
            int unused2 = PhoneWindowManager.this.otaStateForOverSeaPdLaya = getPorscheLYAOtaState();
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            Slog.i(PhoneWindowManager.TAG, "AOD AodSwitchObserver observe");
            if (PhoneWindowManager.mSupportAod) {
                ContentResolver resolver = PhoneWindowManager.this.mContext.getContentResolver();
                resolver.registerContentObserver(Settings.Secure.getUriFor("aod_switch"), false, this, -1);
                resolver.registerContentObserver(Settings.Secure.getUriFor("aod_oversea_pd_laya_ota_state"), false, this, -1);
            }
        }

        public void onChange(boolean selfChange) {
            int unused = PhoneWindowManager.this.mAodSwitch = Settings.Secure.getIntForUser(PhoneWindowManager.this.mContext.getContentResolver(), "aod_switch", 0, ActivityManager.getCurrentUser());
            int unused2 = PhoneWindowManager.this.otaStateForOverSeaPdLaya = Settings.Secure.getIntForUser(PhoneWindowManager.this.mContext.getContentResolver(), "aod_oversea_pd_laya_ota_state", 0, ActivityManager.getCurrentUser());
        }

        private int getAodSwitch() {
            Slog.i(PhoneWindowManager.TAG, "AOD getAodSwitch ");
            if (!PhoneWindowManager.mSupportAod) {
                return 0;
            }
            int unused = PhoneWindowManager.this.mAodSwitch = Settings.Secure.getIntForUser(PhoneWindowManager.this.mContext.getContentResolver(), "aod_switch", 0, ActivityManager.getCurrentUser());
            return PhoneWindowManager.this.mAodSwitch;
        }

        private int getPorscheLYAOtaState() {
            Slog.i(PhoneWindowManager.TAG, "AOD getPorscheLYAOtaState ");
            if (!PhoneWindowManager.mSupportAod) {
                return 0;
            }
            int unused = PhoneWindowManager.this.otaStateForOverSeaPdLaya = Settings.Secure.getIntForUser(PhoneWindowManager.this.mContext.getContentResolver(), "aod_oversea_pd_laya_ota_state", 0, ActivityManager.getCurrentUser());
            return PhoneWindowManager.this.otaStateForOverSeaPdLaya;
        }
    }

    private final class AppDeathRecipient implements IBinder.DeathRecipient {
        AppDeathRecipient() {
        }

        public void binderDied() {
            Slog.i(PhoneWindowManager.TAG, "Death received in " + this + " for thread " + PhoneWindowManager.this.mIAodStateCallback.asBinder());
            if (PhoneWindowManager.mSupportAod) {
                PhoneWindowManager.this.unregeditAodStateCallback(PhoneWindowManager.this.mIAodStateCallback);
                PhoneWindowManager.this.mPowerManager.setDozeOverrideFromAod(1, 0, null);
                PhoneWindowManager.this.mPowerManager.setAodState(-1, 0);
                PhoneWindowManager.this.mPowerManager.setAodState(-1, 2);
                PhoneWindowManager.this.startAodService(7);
            }
        }
    }

    private static class HdmiControl {
        private final HdmiPlaybackClient mClient;

        private HdmiControl(HdmiPlaybackClient client) {
            this.mClient = client;
        }

        public void turnOnTv() {
            if (this.mClient != null) {
                this.mClient.oneTouchPlay(new HdmiPlaybackClient.OneTouchPlayCallback() {
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

        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0061, code lost:
            if (r2 == false) goto L_0x006e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
            r9.this$0.mWindowManagerFuncs.reevaluateStatusBarVisibility();
         */
        public void onInputEvent(InputEvent event, int displayId) {
            try {
                if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0 && ((MotionEvent) event).getAction() == 0) {
                    boolean changed = false;
                    synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                        if (PhoneWindowManager.this.mInputConsumer == null) {
                            finishInputEvent(event, false);
                            return;
                        }
                        int newVal = PhoneWindowManager.this.mResettingSystemUiFlags | 2 | 1 | 4;
                        if (PhoneWindowManager.this.mResettingSystemUiFlags != newVal) {
                            PhoneWindowManager.this.mResettingSystemUiFlags = newVal;
                            changed = true;
                        }
                        int newVal2 = PhoneWindowManager.this.mForceClearedSystemUiFlags | 2;
                        if (PhoneWindowManager.this.mForceClearedSystemUiFlags != newVal2) {
                            PhoneWindowManager.this.mForceClearedSystemUiFlags = newVal2;
                            changed = true;
                            PhoneWindowManager.this.mHandler.postDelayed(PhoneWindowManager.this.mClearHideNavigationFlag, 1000);
                        }
                    }
                }
                finishInputEvent(event, false);
            } catch (Throwable th) {
                finishInputEvent(event, false);
                throw th;
            }
        }
    }

    class MyOrientationListener extends WindowOrientationListener {
        private SparseArray<Runnable> mRunnableCache = new SparseArray<>(5);

        MyOrientationListener(Context context, Handler handler) {
            super(context, handler);
        }

        public void onProposedRotationChanged(int rotation) {
            PhoneWindowManager.this.mScreenRotation = rotation;
            PhoneWindowManager.this.notifyFingerSense(rotation);
            Slog.i(PhoneWindowManager.TAG, "onProposedRotationChanged, rotation=" + rotation);
            Jlog.d(57, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            PhoneWindowManager.this.setSensorRotationFR(rotation);
            Runnable r = this.mRunnableCache.get(rotation, null);
            if (r == null) {
                r = new UpdateRunnable(rotation);
                this.mRunnableCache.put(rotation, r);
            }
            if (PhoneWindowManager.this.isIntelliServiceEnabledFR(PhoneWindowManager.this.mCurrentAppOrientation)) {
                PhoneWindowManager.this.startIntelliServiceFR();
            } else {
                PhoneWindowManager.this.mHandler.post(r);
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
                    boolean unused = PhoneWindowManager.this.wakeUp(SystemClock.uptimeMillis(), PhoneWindowManager.this.mAllowTheaterModeWakeFromWakeGesture, "android.policy:GESTURE");
                }
            }
        }
    }

    private class PolicyHandler extends Handler {
        private PolicyHandler() {
        }

        public void handleMessage(Message msg) {
            boolean z = true;
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
                        Slog.w(PhoneWindowManager.TAG, "UL_Power Setting mKeyguardDrawComplete");
                    }
                    PhoneWindowManager.this.finishKeyguardDrawn();
                    return;
                case 6:
                    Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Keyguard drawn timeout. Setting mKeyguardDrawComplete");
                    PhoneWindowManager.this.finishKeyguardDrawn();
                    return;
                case 7:
                    if (PhoneWindowManager.DEBUG_WAKEUP) {
                        Slog.w(PhoneWindowManager.TAG, "UL_Power Setting mWindowManagerDrawComplete");
                    }
                    PhoneWindowManager.this.finishWindowsDrawn();
                    return;
                case 9:
                    PhoneWindowManager.this.showRecentApps(false);
                    return;
                case 10:
                    PhoneWindowManager.this.showGlobalActionsInternal();
                    return;
                case 11:
                    PhoneWindowManager.this.handleHideBootMessage();
                    return;
                case 12:
                    PhoneWindowManager.this.launchVoiceAssistWithWakeLock();
                    return;
                case 13:
                    PhoneWindowManager phoneWindowManager = PhoneWindowManager.this;
                    long longValue = ((Long) msg.obj).longValue();
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    phoneWindowManager.powerPress(longValue, z, msg.arg2);
                    PhoneWindowManager.this.finishPowerKeyPress();
                    return;
                case 14:
                    PhoneWindowManager.this.powerLongPress();
                    return;
                case 15:
                    PhoneWindowManager phoneWindowManager2 = PhoneWindowManager.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    phoneWindowManager2.updateDreamingSleepToken(z);
                    return;
                case 16:
                    WindowManagerPolicy.WindowState targetBar = msg.arg1 == 0 ? PhoneWindowManager.this.mStatusBar : PhoneWindowManager.this.mNavigationBar;
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
                    return;
                case 19:
                    PhoneWindowManager.this.disposeInputConsumer((WindowManagerPolicy.InputConsumer) msg.obj);
                    return;
                case 20:
                    PhoneWindowManager.this.accessibilityShortcutActivated();
                    return;
                case 21:
                    PhoneWindowManager.this.requestFullBugreport();
                    return;
                case 22:
                    if (PhoneWindowManager.this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(false)) {
                        PhoneWindowManager.this.accessibilityShortcutActivated();
                        return;
                    }
                    return;
                case 23:
                    PhoneWindowManager.this.mAutofillManagerInternal.onBackKeyPressed();
                    return;
                case 24:
                    PhoneWindowManager.this.sendSystemKeyToStatusBar(msg.arg1);
                    return;
                case 25:
                    PhoneWindowManager.this.launchAllAppsAction();
                    return;
                case 26:
                    PhoneWindowManager.this.launchAssistAction((String) msg.obj, msg.arg1);
                    return;
                case PhoneWindowManager.MSG_LAUNCH_ASSIST_LONG_PRESS /*27*/:
                    PhoneWindowManager.this.launchAssistLongPressAction();
                    return;
                case 28:
                    PhoneWindowManager.this.powerVeryLongPress();
                    return;
                case 29:
                    removeMessages(29);
                    Intent intent = new Intent("android.intent.action.USER_ACTIVITY_NOTIFICATION");
                    intent.addFlags(1073741824);
                    PhoneWindowManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.USER_ACTIVITY");
                    return;
                case 30:
                    PhoneWindowManager.this.handleRingerChordGesture();
                    return;
                case 31:
                    removeMessages(31);
                    if (!DecisionUtil.bindServiceToAidsEngine(PhoneWindowManager.this.mContext, PhoneWindowManager.SCREEN_SHOT_VENT_NAME)) {
                        Slog.i(PhoneWindowManager.TAG, "bindServiceToAidsEngine: error.");
                        return;
                    }
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

        public void setLockOptions(Bundle options2) {
            this.options = options2;
        }
    }

    private class ScreenshotRunnable implements Runnable {
        private int mScreenshotType;

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
            PhoneWindowManager.this.mHandler.sendEmptyMessageDelayed(31, 15000);
            ScreenshotHelper access$3000 = PhoneWindowManager.this.mScreenshotHelper;
            int i = this.mScreenshotType;
            boolean z = false;
            boolean z2 = PhoneWindowManager.this.mStatusBar != null && PhoneWindowManager.this.mStatusBar.isVisibleLw();
            if (PhoneWindowManager.this.mNavigationBar != null && PhoneWindowManager.this.mNavigationBar.isVisibleLw()) {
                z = true;
            }
            access$3000.takeScreenshot(i, z2, z, PhoneWindowManager.this.mHandler);
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

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver resolver = PhoneWindowManager.this.mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor("end_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("incall_power_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("incall_back_button_behavior"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("wake_gesture_enabled"), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor("accelerometer_rotation"), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor("user_rotation"), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor("screen_off_timeout"), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor("pointer_location"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("default_input_method"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("immersive_mode_confirmations"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("show_rotation_suggestions"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("volume_hush_gesture"), false, this, -1);
            resolver.registerContentObserver(Settings.Global.getUriFor("policy_control"), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor("system_navigation_keys_enabled"), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor(PhoneWindowManager.this.fingersense_enable), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor(PhoneWindowManager.this.fingersense_letters_enable), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor(PhoneWindowManager.this.line_gesture_enable), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor(PhoneWindowManager.this.fingersense_screenrecord_enable), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor(PhoneWindowManager.this.navibar_enable), false, this, -1);
            resolver.registerContentObserver(Settings.Secure.getUriFor(PhoneWindowManager.FINGER_PRINT_ENABLE), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor(PhoneWindowManager.SINGLE_VIRTAL_NAVBAR), false, this, -1);
            resolver.registerContentObserver(Settings.System.getUriFor(PhoneWindowManager.SINGLE_VIRTAL_NAVBAR_SWITCH), false, this, -1);
            PhoneWindowManager.this.updateSettings();
        }

        public void onChange(boolean selfChange) {
            PhoneWindowManager.this.updateSettings();
            PhoneWindowManager.this.updateRotation(false);
        }
    }

    protected final class UpdateRunnable implements Runnable {
        private final int mRotation;

        public UpdateRunnable(int rotation) {
            this.mRotation = rotation;
        }

        public void run() {
            PhoneWindowManager.this.mPowerManagerInternal.powerHint(2, 0);
            if (PhoneWindowManager.this.mWindowManagerInternal.isDockedDividerResizing()) {
                PhoneWindowManager.this.mWindowManagerInternal.setDockedStackDividerRotation(this.mRotation);
            } else {
                Slog.i(PhoneWindowManager.TAG, "MyOrientationListener: updateRotation.");
                if (PhoneWindowManager.this.isRotationChoicePossible(PhoneWindowManager.this.mCurrentAppOrientation)) {
                    PhoneWindowManager.this.sendProposedRotationChangeToStatusBarInternal(this.mRotation, PhoneWindowManager.this.isValidRotationChoice(PhoneWindowManager.this.mCurrentAppOrientation, this.mRotation));
                } else {
                    PhoneWindowManager.this.updateRotation(false);
                }
            }
            if (PhoneWindowManager.bHasFrontFp) {
                PhoneWindowManager.this.updateSplitScreenView();
            }
        }
    }

    public PhoneWindowManager() {
        BarController barController = new BarController(NAV_TAG, 134217728, 536870912, Integer.MIN_VALUE, 2, 134217728, 32768);
        this.mNavigationBarController = barController;
        this.mNavBarVisibilityListener = new BarController.OnBarVisibilityChangedListener() {
            public void onBarVisibilityChanged(boolean visible) {
                if (PhoneWindowManager.bHasFrontFp && PhoneWindowManager.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                    visible = false;
                }
                Slog.i(PhoneWindowManager.TAG, "notifyAccessibilityButtonVisibilityChanged visible:" + visible);
                PhoneWindowManager.this.mAccessibilityManager.notifyAccessibilityButtonVisibilityChanged(visible);
            }
        };
        this.mAcquireSleepTokenRunnable = new Runnable() {
            public final void run() {
                PhoneWindowManager.lambda$new$0(PhoneWindowManager.this);
            }
        };
        this.mReleaseSleepTokenRunnable = new Runnable() {
            public final void run() {
                PhoneWindowManager.lambda$new$1(PhoneWindowManager.this);
            }
        };
        this.mEndCallLongPress = new Runnable() {
            public void run() {
                PhoneWindowManager.this.mEndCallKeyHandled = true;
                PhoneWindowManager.this.performHapticFeedbackLw(null, 0, false);
                PhoneWindowManager.this.showGlobalActionsInternal();
            }
        };
        this.mScreenshotRunnable = new ScreenshotRunnable();
        this.mHomeDoubleTapTimeoutRunnable = new Runnable() {
            public void run() {
                if (PhoneWindowManager.this.mHomeDoubleTapPending) {
                    PhoneWindowManager.this.mHomeDoubleTapPending = false;
                    PhoneWindowManager.this.handleShortPressOnHome();
                }
            }
        };
        this.mClearHideNavigationFlag = new Runnable() {
            public void run() {
                synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                    PhoneWindowManager.this.mForceClearedSystemUiFlags &= -3;
                }
                PhoneWindowManager.this.mWindowManagerFuncs.reevaluateStatusBarVisibility();
            }
        };
        this.mFrozen = false;
        this.mDockReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.DOCK_EVENT".equals(intent.getAction())) {
                    PhoneWindowManager.this.mDockMode = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                } else {
                    try {
                        IUiModeManager uiModeService = IUiModeManager.Stub.asInterface(ServiceManager.getService("uimode"));
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
        this.mShutdownReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (PhoneWindowManager.ACTION_ACTURAL_SHUTDOWN.equals(intent.getAction())) {
                    PhoneWindowManager.this.mOrientationListener.disable();
                    PhoneWindowManager.this.mOrientationSensorEnabled = false;
                    boolean unused = PhoneWindowManager.this.mIsActuralShutDown = true;
                    PhoneWindowManager.this.notifyFingerSense(-1);
                }
            }
        };
        this.mDreamReceiver = new BroadcastReceiver() {
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
        this.mMultiuserReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                    PhoneWindowManager.this.mSettingsObserver.onChange(false);
                    if (PhoneWindowManager.mSupportAod) {
                        Slog.i(PhoneWindowManager.TAG, "AOD mAodSwitchObserver.onChange");
                        PhoneWindowManager.this.mAodSwitchObserver.onChange(false);
                        if (!PhoneWindowManager.this.mScreenOnEarly && !PhoneWindowManager.this.mScreenOnFully) {
                            PhoneWindowManager.this.startAodService(6);
                        }
                    }
                    synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                        PhoneWindowManager.this.mLastSystemUiFlags = 0;
                        int unused = PhoneWindowManager.this.updateSystemUiVisibilityLw();
                    }
                }
            }
        };
        this.mHiddenNavPanic = new Runnable() {
            /* JADX WARNING: Code restructure failed: missing block: B:11:0x0030, code lost:
                return;
             */
            public void run() {
                synchronized (PhoneWindowManager.this.mWindowManagerFuncs.getWindowManagerLock()) {
                    if (PhoneWindowManager.this.isUserSetupComplete()) {
                        long unused = PhoneWindowManager.this.mPendingPanicGestureUptime = SystemClock.uptimeMillis();
                        if (!PhoneWindowManager.isNavBarEmpty(PhoneWindowManager.this.mLastSystemUiFlags)) {
                            PhoneWindowManager.this.mNavigationBarController.showTransient();
                        }
                    }
                }
            }
        };
        this.mBootMsgDialog = null;
        this.ifBootMessageShowing = false;
        this.mScreenLockTimeout = new ScreenLockTimeout();
        this.mLastHwNavColor = false;
        this.mHwNavColor = false;
        this.mPickUpFlag = false;
        this.mAODState = 1;
        this.mIsFingerprintEnabledBySettings = false;
        this.mDeviceNodeFD = -2147483647;
        this.mNeedPauseAodWhileSwitchUser = false;
        this.mSyncPowerStateFlag = false;
        this.mAodSwitch = 1;
        this.otaStateForOverSeaPdLaya = 0;
        this.mHwPWMEx = null;
    }

    static {
        sApplicationLaunchKeyCategories.append(64, "android.intent.category.APP_BROWSER");
        sApplicationLaunchKeyCategories.append(65, "android.intent.category.APP_EMAIL");
        sApplicationLaunchKeyCategories.append(207, "android.intent.category.APP_CONTACTS");
        sApplicationLaunchKeyCategories.append(208, "android.intent.category.APP_CALENDAR");
        sApplicationLaunchKeyCategories.append(209, "android.intent.category.APP_MUSIC");
        sApplicationLaunchKeyCategories.append(NetworkManagementService.NetdResponseCode.TetherStatusResult, "android.intent.category.APP_CALCULATOR");
        sIsLayaPorschePorduct = false;
        if (sIsPorscheProduct) {
            String board = sProductBrandString.toUpperCase(Locale.US);
            if (board.contains("LYA") || board.contains("LAYA")) {
                sIsLayaPorschePorduct = true;
            }
            Slog.i(TAG, "HwAodManagerService sIsPorscheProduct " + sIsLayaPorschePorduct);
        }
    }

    public boolean isLandscape() {
        return this.mScreenRotation == 1 || this.mScreenRotation == 3;
    }

    public static /* synthetic */ void lambda$new$0(PhoneWindowManager phoneWindowManager) {
        if (phoneWindowManager.mWindowSleepToken == null) {
            phoneWindowManager.mWindowSleepToken = phoneWindowManager.mActivityManagerInternal.acquireSleepToken("WindowSleepToken", 0);
        }
    }

    public static /* synthetic */ void lambda$new$1(PhoneWindowManager phoneWindowManager) {
        if (phoneWindowManager.mWindowSleepToken != null) {
            phoneWindowManager.mWindowSleepToken.release();
            phoneWindowManager.mWindowSleepToken = null;
        }
    }

    /* access modifiers changed from: private */
    public void handleRingerChordGesture() {
        if (this.mRingerToggleChord != 0) {
            getAudioManagerInternal();
            this.mAudioManagerInternal.silenceRingerModeInternal("volume_hush");
            Settings.Secure.putInt(this.mContext.getContentResolver(), "hush_gesture_used", 1);
            this.mLogger.action(1440, this.mRingerToggleChord);
        }
    }

    /* access modifiers changed from: package-private */
    public IStatusBarService getStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            }
            iStatusBarService = this.mStatusBarService;
        }
        return iStatusBarService;
    }

    /* access modifiers changed from: package-private */
    public StatusBarManagerInternal getStatusBarManagerInternal() {
        StatusBarManagerInternal statusBarManagerInternal;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarManagerInternal == null) {
                this.mStatusBarManagerInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
            }
            statusBarManagerInternal = this.mStatusBarManagerInternal;
        }
        return statusBarManagerInternal;
    }

    /* access modifiers changed from: package-private */
    public AudioManagerInternal getAudioManagerInternal() {
        AudioManagerInternal audioManagerInternal;
        synchronized (this.mServiceAquireLock) {
            if (this.mAudioManagerInternal == null) {
                this.mAudioManagerInternal = (AudioManagerInternal) LocalServices.getService(AudioManagerInternal.class);
            }
            audioManagerInternal = this.mAudioManagerInternal;
        }
        return audioManagerInternal;
    }

    /* access modifiers changed from: package-private */
    public boolean needSensorRunningLp() {
        boolean z = true;
        if (this.mSupportAutoRotation && (this.mCurrentAppOrientation == 4 || this.mCurrentAppOrientation == 10 || this.mCurrentAppOrientation == 7 || this.mCurrentAppOrientation == 6)) {
            return true;
        }
        if ((this.mCarDockEnablesAccelerometer && this.mDockMode == 2) || (this.mDeskDockEnablesAccelerometer && (this.mDockMode == 1 || this.mDockMode == 3 || this.mDockMode == 4))) {
            return true;
        }
        if (IS_lOCK_UNNATURAL_ORIENTATION || this.mUserRotationMode != 1) {
            return this.mSupportAutoRotation;
        }
        if (!this.mSupportAutoRotation || this.mShowRotationSuggestions != 1) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void updateOrientationListenerLp() {
        if (this.mOrientationListener.canDetectOrientation()) {
            boolean disable = true;
            if (this.mScreenOnEarly && this.mAwake && this.mKeyguardDrawComplete && this.mWindowManagerDrawComplete && needSensorRunningLp()) {
                disable = false;
                if (!this.mOrientationSensorEnabled) {
                    this.mOrientationListener.enable(true);
                    this.mOrientationSensorEnabled = true;
                }
            }
            if (disable && this.mOrientationSensorEnabled) {
                this.mOrientationListener.disable();
                this.mOrientationSensorEnabled = false;
                notifyFingerSense(-1);
            }
        }
    }

    private void interceptBackKeyDown() {
        MetricsLogger.count(this.mContext, "key_back_down", 1);
        this.mBackKeyHandled = false;
        if (hasLongPressOnBackBehavior()) {
            Message msg = this.mHandler.obtainMessage(18);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
        }
    }

    private boolean interceptBackKeyUp(KeyEvent event) {
        boolean handled = this.mBackKeyHandled;
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
            this.mHandler.sendMessage(this.mHandler.obtainMessage(23));
        }
        return handled;
    }

    private void interceptPowerKeyDown(KeyEvent event, boolean interactive) {
        boolean z;
        boolean z2 = interactive;
        HwFrameworkFactory.getHwApsImpl().StopSdrForSpecial("powerdown", 26);
        long startTime = SystemClock.elapsedRealtime();
        if (!this.mPowerKeyWakeLock.isHeld()) {
            this.mPowerKeyWakeLock.acquire();
        }
        long startTime2 = printTimeoutLog("PowerManager_screenOn", startTime, "acquire wakeLock timeout", DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE);
        if (this.mPowerKeyPressCounter != 0) {
            this.mHandler.removeMessages(13);
        }
        if (this.mImmersiveModeConfirmation.onPowerKeyDown(z2, SystemClock.elapsedRealtime(), isImmersiveMode(this.mLastSystemUiFlags), isNavBarEmpty(this.mLastSystemUiFlags))) {
            this.mHandler.post(this.mHiddenNavPanic);
        }
        Handler handler = this.mHandler;
        WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
        Objects.requireNonNull(windowManagerFuncs);
        handler.post(new Runnable() {
            public final void run() {
                WindowManagerPolicy.WindowManagerFuncs.this.triggerAnimationFailsafe();
            }
        });
        if (z2 && !this.mScreenshotChordPowerKeyTriggered && (event.getFlags() & 1024) == 0) {
            this.mScreenshotChordPowerKeyTriggered = true;
            this.mScreenshotChordPowerKeyTime = event.getDownTime();
            interceptScreenshotChord();
            interceptRingerToggleChord();
        }
        TelecomManager telecomManager = getTelecommService();
        boolean hungUp = false;
        if (telecomManager != null) {
            if (telecomManager.isRinging()) {
                telecomManager.silenceRinger();
            } else if ((this.mIncallPowerBehavior & 2) != 0 && telecomManager.isInCall() && z2) {
                hungUp = telecomManager.endCall();
            }
        }
        boolean hungUp2 = hungUp;
        long startTime3 = printTimeoutLog("PowerManager_screenOn", startTime2, "telecomManager timeout", DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE);
        GestureLauncherService gestureService = (GestureLauncherService) LocalServices.getService(GestureLauncherService.class);
        boolean gesturedServiceIntercepted = false;
        if (gestureService != null) {
            boolean gesturedServiceIntercepted2 = gestureService.interceptPowerKeyDown(event, z2, this.mTmpBoolean);
            if (this.mTmpBoolean.value && this.mRequestedOrGoingToSleep) {
                this.mCameraGestureTriggeredDuringGoingToSleep = true;
            }
            long startTime4 = printTimeoutLog("PowerManager_screenOn", startTime3, "GestureLauncherService timeout", DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE);
            gesturedServiceIntercepted = gesturedServiceIntercepted2;
        } else {
            KeyEvent keyEvent = event;
        }
        this.mHwPWMEx.sendPowerKeyToFingerprint(event.getKeyCode(), true, z2);
        sendSystemKeyToStatusBarAsync(event.getKeyCode());
        boolean z3 = false;
        this.mPowerKeyHandledByHiaction = false;
        this.mPowerOffToastShown = false;
        Slog.i(TAG, "hungUp=" + hungUp2 + ", mScreenshotChordVolumeDownKeyTriggered=" + this.mScreenshotChordVolumeDownKeyTriggered + ", mA11yShortcutChordVolumeUpKeyTriggered=" + this.mA11yShortcutChordVolumeUpKeyTriggered + ", gesturedServiceIntercepted=" + gesturedServiceIntercepted);
        if (hungUp2 || this.mScreenshotChordVolumeDownKeyTriggered || this.mA11yShortcutChordVolumeUpKeyTriggered || gesturedServiceIntercepted) {
            z3 = true;
        }
        this.mPowerKeyHandled = z3;
        if (this.mPowerKeyHandled) {
        } else if (z2) {
            if (hasLongPressOnPowerBehavior()) {
                if ((event.getFlags() & 128) != 0) {
                    powerLongPress();
                    TelecomManager telecomManager2 = telecomManager;
                } else {
                    Message msg = this.mHandler.obtainMessage(14);
                    msg.setAsynchronous(true);
                    this.mHandler.sendMessageDelayed(msg, 3000);
                    if (hasVeryLongPressOnPowerBehavior()) {
                        Message longMsg = this.mHandler.obtainMessage(28);
                        longMsg.setAsynchronous(true);
                        TelecomManager telecomManager3 = telecomManager;
                        this.mHandler.sendMessageDelayed(longMsg, (long) this.mVeryLongPressTimeout);
                    }
                }
                notifyPowerkeyInteractive(true);
            }
            notifyPowerkeyInteractive(true);
        } else {
            wakeUpFromPowerKey(event.getDownTime());
            if (this.mSupportLongPressPowerWhenNonInteractive && hasLongPressOnPowerBehavior()) {
                if ((event.getFlags() & 128) != 0) {
                    powerLongPress();
                    z = true;
                } else {
                    Message msg2 = this.mHandler.obtainMessage(14);
                    z = true;
                    msg2.setAsynchronous(true);
                    this.mHandler.sendMessageDelayed(msg2, 3000);
                    if (hasVeryLongPressOnPowerBehavior()) {
                        Message longMsg2 = this.mHandler.obtainMessage(28);
                        longMsg2.setAsynchronous(true);
                        this.mHandler.sendMessageDelayed(longMsg2, (long) this.mVeryLongPressTimeout);
                    }
                }
                this.mBeganFromNonInteractive = z;
            } else if (getMaxMultiPressPowerCount() <= 1) {
                this.mPowerKeyHandled = true;
            } else {
                this.mBeganFromNonInteractive = true;
            }
        }
    }

    private void interceptPowerKeyUp(KeyEvent event, boolean interactive, boolean canceled) {
        boolean handled = canceled || this.mPowerKeyHandled || this.mPowerKeyHandledByHiaction || this.mPowerOffToastShown;
        this.mScreenshotChordPowerKeyTriggered = false;
        cancelPendingScreenshotChordAction();
        cancelPendingPowerKeyAction();
        if (!handled) {
            this.mPowerKeyPressCounter++;
            int maxCount = getMaxMultiPressPowerCount();
            long eventTime = event.getDownTime();
            if (this.mPowerKeyPressCounter < maxCount) {
                Message msg = this.mHandler.obtainMessage(13, interactive, this.mPowerKeyPressCounter, Long.valueOf(eventTime));
                msg.setAsynchronous(true);
                this.mHandler.sendMessageDelayed(msg, (long) ViewConfiguration.getMultiPressTimeout());
                return;
            }
            powerPress(eventTime, interactive, this.mPowerKeyPressCounter);
        }
        this.mHwPWMEx.sendPowerKeyToFingerprint(event.getKeyCode(), false, interactive);
        finishPowerKeyPress();
    }

    /* access modifiers changed from: private */
    public void finishPowerKeyPress() {
        this.mBeganFromNonInteractive = false;
        this.mPowerKeyPressCounter = 0;
        if (this.mPowerKeyWakeLock.isHeld()) {
            this.mPowerKeyWakeLock.release();
        }
    }

    /* access modifiers changed from: protected */
    public void cancelPendingPowerKeyAction() {
        if (!this.mPowerKeyHandled) {
            this.mPowerKeyHandled = true;
            this.mHandler.removeMessages(14);
        }
        if (hasVeryLongPressOnPowerBehavior()) {
            this.mHandler.removeMessages(28);
        }
    }

    private void cancelPendingBackKeyAction() {
        if (!this.mBackKeyHandled) {
            this.mBackKeyHandled = true;
            this.mHandler.removeMessages(18);
        }
    }

    private void powerPressBDReport(int eventId) {
        if (Log.HWINFO) {
            Flog.bdReport(this.mContext, eventId);
        }
    }

    /* access modifiers changed from: private */
    public void powerPress(long eventTime, boolean interactive, int count) {
        if (!this.mScreenOnEarly || this.mScreenOnFully || (count == 2 && getIsNeedNotifyWallet())) {
            Slog.d(TAG, "powerPress: eventTime=" + eventTime + " interactive=" + interactive + " count=" + count + " beganFromNonInteractive=" + this.mBeganFromNonInteractive + " mShortPressOnPowerBehavior=" + this.mShortPressOnPowerBehavior);
            if (count != 2) {
                if (count != 3) {
                    if (interactive && !this.mBeganFromNonInteractive) {
                        switch (this.mShortPressOnPowerBehavior) {
                            case 1:
                                powerPressBDReport(983);
                                goToSleep(eventTime, 4, 0);
                                break;
                            case 2:
                                powerPressBDReport(983);
                                goToSleep(eventTime, 4, 1);
                                break;
                            case 3:
                                powerPressBDReport(983);
                                goToSleep(eventTime, 4, 1);
                                launchHomeFromHotKey();
                                break;
                            case 4:
                                shortPressPowerGoHome();
                                break;
                            case 5:
                                if (!this.mDismissImeOnBackKeyPressed) {
                                    shortPressPowerGoHome();
                                    break;
                                } else {
                                    if (this.mInputMethodManagerInternal == null) {
                                        this.mInputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
                                    }
                                    if (this.mInputMethodManagerInternal != null) {
                                        this.mInputMethodManagerInternal.hideCurrentInputMethod();
                                        break;
                                    }
                                }
                                break;
                        }
                    }
                } else {
                    powerMultiPressAction(eventTime, interactive, this.mTriplePressOnPowerBehavior);
                }
            } else {
                powerMultiPressAction(eventTime, interactive, this.mDoublePressOnPowerBehavior);
            }
            return;
        }
        Slog.i(TAG, "Suppressed redundant power key press while already in the process of turning the screen on.");
    }

    private void goToSleep(long eventTime, int reason, int flags) {
        if (reason != 4 || !this.mHwPWMEx.isPowerFpForbidGotoSleep()) {
            this.mRequestedOrGoingToSleep = true;
            this.mPowerManager.goToSleep(eventTime, reason, flags);
            return;
        }
        Log.i(TAG, "goToSleep return, because of isPowerFpAndAppAuthenticating");
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
                if (isUserSetupComplete()) {
                    if (!isTheaterModeEnabled()) {
                        Slog.i(TAG, "Toggling theater mode on.");
                        Settings.Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 1);
                        if (this.mGoToSleepOnButtonPressTheaterMode && interactive) {
                            goToSleep(eventTime, 4, 0);
                            break;
                        }
                    } else {
                        Slog.i(TAG, "Toggling theater mode off.");
                        Settings.Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 0);
                        if (!interactive) {
                            wakeUpFromPowerKey(eventTime);
                            break;
                        }
                    }
                } else {
                    Slog.i(TAG, "Ignoring toggling theater mode - device not setup.");
                    break;
                }
                break;
            case 2:
                Slog.i(TAG, "Starting brightness boost.");
                if (!interactive) {
                    wakeUpFromPowerKey(eventTime);
                }
                this.mPowerManager.boostScreenBrightness(eventTime);
                break;
            case 3:
                if (eventTime - this.mLastNotifyWalletTime >= 1000) {
                    this.mHwPWMEx.launchWalletSwipe(this.mHandler, eventTime);
                    this.mLastNotifyWalletTime = eventTime;
                    break;
                } else {
                    Slog.i(TAG, "wallet haweiPay have lanunched.");
                    return;
                }
        }
    }

    private int getMaxMultiPressPowerCount() {
        if (this.mTriplePressOnPowerBehavior != 0) {
            return 3;
        }
        if (this.mDoublePressOnPowerBehavior == 0 || !getIsNeedNotifyWallet()) {
            return 1;
        }
        return 2;
    }

    /* access modifiers changed from: private */
    public void powerLongPress() {
        int behavior = getResolvedLongPressOnPowerBehavior();
        boolean z = true;
        boolean keyguardActive = false;
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
                WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
                if (behavior != 2) {
                    z = false;
                }
                windowManagerFuncs.shutdown(z);
                return;
            case 4:
                this.mPowerKeyHandled = true;
                performHapticFeedbackLw(null, 0, false);
                if (this.mKeyguardDelegate != null) {
                    keyguardActive = this.mKeyguardDelegate.isShowing();
                }
                if (!keyguardActive) {
                    Intent intent = new Intent("android.intent.action.VOICE_ASSIST");
                    if (this.mAllowStartActivityForLongPressOnPowerDuringSetup) {
                        this.mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
                        return;
                    } else {
                        startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
                        return;
                    }
                } else {
                    return;
                }
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    public void powerVeryLongPress() {
        switch (this.mVeryLongPressOnPowerBehavior) {
            case 1:
                this.mPowerKeyHandled = true;
                performHapticFeedbackLw(null, 0, false);
                showGlobalActionsInternal();
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    public void backLongPress() {
        boolean keyguardActive;
        this.mBackKeyHandled = true;
        switch (this.mLongPressOnBackBehavior) {
            case 1:
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

    /* access modifiers changed from: private */
    public void accessibilityShortcutActivated() {
        this.mAccessibilityShortcutController.performAccessibilityShortcut();
    }

    /* access modifiers changed from: private */
    public void disposeInputConsumer(WindowManagerPolicy.InputConsumer inputConsumer) {
        if (inputConsumer != null) {
            inputConsumer.dismiss();
        }
        this.mInputConsumer = null;
    }

    private void sleepPress() {
        if (this.mShortPressOnSleepBehavior == 1) {
            launchHomeFromHotKey(false, true);
        }
    }

    private void sleepRelease(long eventTime) {
        switch (this.mShortPressOnSleepBehavior) {
            case 0:
            case 1:
                Slog.i(TAG, "sleepRelease() calling goToSleep(GO_TO_SLEEP_REASON_SLEEP_BUTTON)");
                goToSleep(eventTime, 6, 0);
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

    private boolean hasVeryLongPressOnPowerBehavior() {
        return this.mVeryLongPressOnPowerBehavior != 0;
    }

    private boolean hasLongPressOnBackBehavior() {
        return this.mLongPressOnBackBehavior != 0;
    }

    private void interceptScreenshotChord() {
        if (HWFLOW) {
            Slog.i(TAG, "takeScreenshot enabled  " + this.mScreenshotChordEnabled + "  VolumeDownKeyTriggered  " + this.mScreenshotChordVolumeDownKeyTriggered + " PowerKeyTriggered  " + this.mScreenshotChordPowerKeyTriggered + " VolumeUpKeyTriggered  " + this.mA11yShortcutChordVolumeUpKeyTriggered);
        }
        if (this.mScreenshotChordEnabled && this.mScreenshotChordVolumeDownKeyTriggered && this.mScreenshotChordPowerKeyTriggered && !this.mA11yShortcutChordVolumeUpKeyTriggered) {
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
        if (this.mAccessibilityShortcutController.isAccessibilityShortcutAvailable(isKeyguardLocked()) && this.mScreenshotChordVolumeDownKeyTriggered && this.mA11yShortcutChordVolumeUpKeyTriggered && !this.mScreenshotChordPowerKeyTriggered) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS && now <= this.mA11yShortcutChordVolumeUpKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {
                this.mScreenshotChordVolumeDownKeyConsumed = true;
                this.mA11yShortcutChordVolumeUpKeyConsumed = true;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(20), getAccessibilityShortcutTimeout());
            }
        }
    }

    private void interceptRingerToggleChord() {
    }

    private long getAccessibilityShortcutTimeout() {
        ViewConfiguration config = ViewConfiguration.get(this.mContext);
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_shortcut_dialog_shown", 0, this.mCurrentUserId) == 0) {
            return config.getAccessibilityShortcutKeyTimeout();
        }
        return config.getAccessibilityShortcutKeyTimeoutAfterConfirmation();
    }

    private long getScreenshotChordLongPressDelay() {
        if (this.mKeyguardDelegate.isShowing()) {
            return (long) (KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER * ((float) ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout()));
        }
        return 0;
    }

    private long getRingerToggleChordDelay() {
        return (long) ViewConfiguration.getTapTimeout();
    }

    private void cancelPendingScreenshotChordAction() {
        if (HWFLOW) {
            Slog.i(TAG, "takeScreenshot cancelPendingScreenshotChordAction");
        }
        this.mHandler.removeCallbacks(this.mScreenshotRunnable);
    }

    private void cancelPendingAccessibilityShortcutAction() {
        this.mHandler.removeMessages(20);
    }

    private void cancelPendingRingerToggleChordAction() {
        this.mHandler.removeMessages(30);
    }

    public void showGlobalActions() {
        this.mHandler.removeMessages(10);
        this.mHandler.sendEmptyMessage(10);
    }

    /* access modifiers changed from: package-private */
    public void showGlobalActionsInternal() {
        if (HwDeviceManager.disallowOp(37)) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(PhoneWindowManager.this.mContext, 33685954, 0);
                    toast.getWindowParams().type = 2010;
                    toast.getWindowParams().privateFlags |= 16;
                    toast.show();
                }
            });
        } else if (!HwPolicyFactory.ifUseHwGlobalActions()) {
            if (this.mGlobalActions == null) {
                this.mGlobalActions = new GlobalActions(this.mContext, this.mWindowManagerFuncs);
            }
            boolean keyguardShowing = isKeyguardShowingAndNotOccluded();
            this.mGlobalActions.showDialog(keyguardShowing, isDeviceProvisioned());
            if (keyguardShowing) {
                this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
            }
        } else if (!isEmergencySoSActivity()) {
            HwPolicyFactory.showHwGlobalActionsFragment(this.mContext, this.mWindowManagerFuncs, this.mPowerManager, isKeyguardShowingAndNotOccluded(), isKeyguardSecure(this.mCurrentUserId), isDeviceProvisioned());
        }
    }

    private boolean isEmergencySoSActivity() {
        ActivityManagerService am = (ActivityManagerService) ServiceManager.getService("activity");
        if (am == null || ((!ACTIVITY_NAME_EMERGENCY_COUNTDOWN.equals(am.topAppName()) && !ACTIVITY_NAME_EMERGENCY_NUMBER.equals(am.topAppName())) || this.mFocusedWindow == null || (this.mFocusedWindow.getAttrs().hwFlags & Integer.MIN_VALUE) != Integer.MIN_VALUE)) {
            return false;
        }
        Log.d(TAG, "Emergency power focusWindow is Emergency");
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isUserSetupComplete() {
        boolean z = false;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0) {
            z = true;
        }
        boolean isSetupComplete = z;
        if (this.mHasFeatureLeanback) {
            return isSetupComplete & isTvUserSetupComplete();
        }
        return isSetupComplete;
    }

    private boolean isTvUserSetupComplete() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "tv_user_setup_complete", 0, -2) != 0;
    }

    /* access modifiers changed from: private */
    public void handleShortPressOnHome() {
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
            this.mHdmiControl = new HdmiControl(client);
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

    /* access modifiers changed from: private */
    public void launchAllAppsAction() {
        Intent intent = new Intent("android.intent.action.ALL_APPS");
        if (this.mHasFeatureLeanback) {
            PackageManager pm = this.mContext.getPackageManager();
            Intent intentLauncher = new Intent("android.intent.action.MAIN");
            intentLauncher.addCategory("android.intent.category.HOME");
            ResolveInfo resolveInfo = pm.resolveActivityAsUser(intentLauncher, DumpState.DUMP_DEXOPT, this.mCurrentUserId);
            if (resolveInfo != null) {
                intent.setPackage(resolveInfo.activityInfo.packageName);
            }
        }
        startActivityAsUser(intent, UserHandle.CURRENT);
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

    /* access modifiers changed from: private */
    public void showPictureInPictureMenuInternal() {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showPictureInPictureMenu();
        }
    }

    private boolean isRoundWindow() {
        return this.mContext.getResources().getConfiguration().isScreenRound();
    }

    public void init(Context context, IWindowManager windowManager, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs) {
        int maxVertical;
        int minVertical;
        int maxHorizontal;
        int maxRadius;
        int minHorizontal;
        Context context2 = context;
        this.mContext = context2;
        this.mWindowManager = windowManager;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mHwPWMEx = HwServiceExFactory.getHwPhoneWindowManagerEx(this, context);
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        this.mDreamManagerInternal = (DreamManagerInternal) LocalServices.getService(DreamManagerInternal.class);
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mHasFeatureWatch = this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        this.mHasFeatureLeanback = this.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
        this.mAccessibilityShortcutController = new AccessibilityShortcutController(this.mContext, new Handler(), this.mCurrentUserId);
        this.mLogger = new MetricsLogger();
        boolean burnInProtectionEnabled = context.getResources().getBoolean(17956950);
        boolean burnInProtectionDevMode = SystemProperties.getBoolean("persist.debug.force_burn_in", false);
        if (burnInProtectionEnabled || burnInProtectionDevMode) {
            if (burnInProtectionDevMode) {
                minHorizontal = -8;
                maxHorizontal = 8;
                minVertical = -8;
                maxVertical = -4;
                maxRadius = isRoundWindow() ? 6 : -1;
            } else {
                Resources resources = context.getResources();
                int minHorizontal2 = resources.getInteger(17694750);
                int maxHorizontal2 = resources.getInteger(17694747);
                int minVertical2 = resources.getInteger(17694751);
                int maxVertical2 = resources.getInteger(17694749);
                maxRadius = resources.getInteger(17694748);
                minHorizontal = minHorizontal2;
                maxHorizontal = maxHorizontal2;
                minVertical = minVertical2;
                maxVertical = maxVertical2;
            }
            BurnInProtectionHelper burnInProtectionHelper = r2;
            BurnInProtectionHelper burnInProtectionHelper2 = new BurnInProtectionHelper(context2, minHorizontal, maxHorizontal, minVertical, maxVertical, maxRadius);
            this.mBurnInProtectionHelper = burnInProtectionHelper;
        }
        this.mHandler = new PolicyHandler();
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
        this.mShortcutManager = new ShortcutManager(context2);
        this.mUiMode = context.getResources().getInteger(17694773);
        this.mHomeIntent = new Intent("android.intent.action.MAIN", null);
        this.mHomeIntent.addCategory("android.intent.category.HOME");
        this.mHomeIntent.addFlags(270533120);
        this.mEnableCarDockHomeCapture = context.getResources().getBoolean(17956951);
        this.mCarDockIntent = new Intent("android.intent.action.MAIN", null);
        this.mCarDockIntent.addCategory("android.intent.category.CAR_DOCK");
        this.mCarDockIntent.addFlags(270532608);
        this.mDeskDockIntent = new Intent("android.intent.action.MAIN", null);
        this.mDeskDockIntent.addCategory("android.intent.category.DESK_DOCK");
        this.mDeskDockIntent.addFlags(270532608);
        this.mVrHeadsetHomeIntent = new Intent("android.intent.action.MAIN", null);
        this.mVrHeadsetHomeIntent.addCategory("android.intent.category.VR_HOME");
        this.mVrHeadsetHomeIntent.addFlags(270532608);
        this.mPowerManager = (PowerManager) context2.getSystemService("power");
        boolean z = true;
        this.mBroadcastWakeLock = this.mPowerManager.newWakeLock(1, "PhoneWindowManager.mBroadcastWakeLock");
        this.mPowerKeyWakeLock = this.mPowerManager.newWakeLock(1, "PhoneWindowManager.mPowerKeyWakeLock");
        this.mEnableShiftMenuBugReports = "1".equals(SystemProperties.get("ro.debuggable"));
        this.mSupportAutoRotation = this.mContext.getResources().getBoolean(17957034);
        this.mLidOpenRotation = readRotation(17694797);
        this.mCarDockRotation = readRotation(17694755);
        this.mDeskDockRotation = readRotation(17694776);
        this.mUndockedHdmiRotation = readRotation(17694876);
        this.mCarDockEnablesAccelerometer = this.mContext.getResources().getBoolean(17956910);
        this.mDeskDockEnablesAccelerometer = this.mContext.getResources().getBoolean(17956923);
        this.mLidKeyboardAccessibility = this.mContext.getResources().getInteger(17694795);
        this.mLidNavigationAccessibility = this.mContext.getResources().getInteger(17694796);
        this.mLidControlsScreenLock = this.mContext.getResources().getBoolean(17956986);
        this.mLidControlsSleep = this.mContext.getResources().getBoolean(17956987);
        this.mTranslucentDecorEnabled = this.mContext.getResources().getBoolean(17956966);
        this.mAllowTheaterModeWakeFromKey = this.mContext.getResources().getBoolean(17956880);
        this.mAllowTheaterModeWakeFromPowerKey = this.mAllowTheaterModeWakeFromKey || this.mContext.getResources().getBoolean(17956884);
        this.mAllowTheaterModeWakeFromMotion = this.mContext.getResources().getBoolean(17956882);
        this.mAllowTheaterModeWakeFromMotionWhenNotDreaming = this.mContext.getResources().getBoolean(17956883);
        this.mAllowTheaterModeWakeFromCameraLens = this.mContext.getResources().getBoolean(17956877);
        this.mAllowTheaterModeWakeFromLidSwitch = this.mContext.getResources().getBoolean(17956881);
        this.mAllowTheaterModeWakeFromWakeGesture = this.mContext.getResources().getBoolean(17956879);
        this.mGoToSleepOnButtonPressTheaterMode = this.mContext.getResources().getBoolean(17956978);
        this.mSupportLongPressPowerWhenNonInteractive = this.mContext.getResources().getBoolean(17957037);
        this.mLongPressOnBackBehavior = this.mContext.getResources().getInteger(17694800);
        this.mShortPressOnPowerBehavior = this.mContext.getResources().getInteger(17694864);
        this.mLongPressOnPowerBehavior = this.mContext.getResources().getInteger(17694802);
        this.mVeryLongPressOnPowerBehavior = this.mContext.getResources().getInteger(17694878);
        this.mDoublePressOnPowerBehavior = this.mContext.getResources().getInteger(17694778);
        this.mTriplePressOnPowerBehavior = this.mContext.getResources().getInteger(17694875);
        this.mShortPressOnSleepBehavior = this.mContext.getResources().getInteger(17694865);
        this.mVeryLongPressTimeout = this.mContext.getResources().getInteger(17694879);
        this.mAllowStartActivityForLongPressOnPowerDuringSetup = this.mContext.getResources().getBoolean(17956876);
        if (AudioSystem.getPlatformType(this.mContext) != 2) {
            z = false;
        }
        this.mUseTvRouting = z;
        this.mHandleVolumeKeysInWM = this.mContext.getResources().getBoolean(17956980);
        readConfigurationDependentBehaviors();
        this.mAccessibilityManager = (AccessibilityManager) context2.getSystemService("accessibility");
        IntentFilter filter = new IntentFilter();
        filter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_ENTER_DESK_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_DESK_MODE);
        filter.addAction("android.intent.action.DOCK_EVENT");
        Intent intent = context2.registerReceiver(this.mDockReceiver, filter);
        context2.registerReceiver(this.mShutdownReceiver, new IntentFilter(ACTION_ACTURAL_SHUTDOWN), HUAWEI_SHUTDOWN_PERMISSION, null);
        if (intent != null) {
            this.mDockMode = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
        }
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.DREAMING_STARTED");
        filter2.addAction("android.intent.action.DREAMING_STOPPED");
        context2.registerReceiver(this.mDreamReceiver, filter2);
        context2.registerReceiver(this.mMultiuserReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
        this.mSystemGestures = new SystemGesturesPointerEventListener(context2, new SystemGesturesPointerEventListener.Callbacks() {
            public void onSwipeFromTop() {
                if (!PhoneWindowManager.this.isGestureIsolated() && Settings.Secure.getInt(PhoneWindowManager.this.mContext.getContentResolver(), "device_provisioned", 1) != 0 && !PhoneWindowManager.this.swipeFromTop() && PhoneWindowManager.this.mStatusBar != null) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mStatusBar);
                }
            }

            public void onSwipeFromBottom() {
                if (!PhoneWindowManager.this.isGestureIsolated() && Settings.Secure.getInt(PhoneWindowManager.this.mContext.getContentResolver(), "device_provisioned", 1) != 0 && !PhoneWindowManager.this.swipeFromBottom() && PhoneWindowManager.this.mNavigationBar != null && PhoneWindowManager.this.mNavigationBarPosition == 4) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onSwipeFromRight() {
                if (!PhoneWindowManager.this.isGestureIsolated() && !PhoneWindowManager.this.swipeFromRight() && PhoneWindowManager.this.mNavigationBar != null && PhoneWindowManager.this.mNavigationBarPosition == 2) {
                    PhoneWindowManager.this.requestTransientBars(PhoneWindowManager.this.mNavigationBar);
                }
            }

            public void onSwipeFromLeft() {
                if (PhoneWindowManager.this.mNavigationBar != null && PhoneWindowManager.this.mNavigationBarPosition == 1) {
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
                PhoneWindowManager.this.onPointDown();
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
        this.mVibrator = (Vibrator) context2.getSystemService("vibrator");
        this.mLongPressVibePattern = getLongIntArray(this.mContext.getResources(), 17236015);
        this.mCalendarDateVibePattern = getLongIntArray(this.mContext.getResources(), 17235990);
        this.mSafeModeEnabledVibePattern = getLongIntArray(this.mContext.getResources(), 17236030);
        this.mScreenshotChordEnabled = this.mContext.getResources().getBoolean(17956965);
        this.mGlobalKeyManager = new GlobalKeyManager(this.mContext);
        initializeHdmiState();
        if (!this.mPowerManager.isInteractive()) {
            startedGoingToSleep(2);
            finishedGoingToSleep(2);
        }
        this.mWindowManagerInternal.registerAppTransitionListener(this.mStatusBarController.getAppTransitionListener());
        this.mWindowManagerInternal.registerAppTransitionListener(new WindowManagerInternal.AppTransitionListener() {
            public int onAppTransitionStartingLocked(int transit, IBinder openToken, IBinder closeToken, long duration, long statusBarAnimationStartTime, long statusBarAnimationDuration) {
                return PhoneWindowManager.this.handleStartTransitionForKeyguardLw(transit, duration);
            }

            public void onAppTransitionCancelledLocked(int transit) {
                int unused = PhoneWindowManager.this.handleStartTransitionForKeyguardLw(transit, 0);
            }
        });
        this.mKeyguardDelegate = new KeyguardServiceDelegate(this.mContext, new KeyguardStateMonitor.StateCallback() {
            public void onTrustedChanged() {
                PhoneWindowManager.this.mWindowManagerFuncs.notifyKeyguardTrustedChanged();
            }

            public void onShowingChanged() {
                PhoneWindowManager.this.mWindowManagerFuncs.onKeyguardShowingAndNotOccludedChanged();
            }
        });
        this.mScreenshotHelper = new ScreenshotHelper(this.mContext);
        hwInit();
        this.splitNavigationBarDp = Settings.System.getInt(this.mContext.getContentResolver(), "hw_split_navigation_bar_dp", 600);
    }

    private void readConfigurationDependentBehaviors() {
        Resources res = this.mContext.getResources();
        this.mLongPressOnHomeBehavior = res.getInteger(17694801);
        if (this.mLongPressOnHomeBehavior < 0 || this.mLongPressOnHomeBehavior > 2) {
            this.mLongPressOnHomeBehavior = 0;
        }
        this.mDoubleTapOnHomeBehavior = res.getInteger(17694779);
        if (this.mDoubleTapOnHomeBehavior < 0 || this.mDoubleTapOnHomeBehavior > 1) {
            this.mDoubleTapOnHomeBehavior = 0;
        }
        this.mShortPressOnWindowBehavior = 0;
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.picture_in_picture")) {
            this.mShortPressOnWindowBehavior = 1;
        }
        this.mNavBarOpacityMode = res.getInteger(17694824);
    }

    public void setInitialDisplaySize(Display display, int width, int height, int density) {
        int longSize;
        int shortSize;
        int i = width;
        int i2 = height;
        if (this.mContext == null || display.getDisplayId() != 0) {
            Display display2 = display;
            return;
        }
        this.mDisplay = display;
        Resources res = this.mContext.getResources();
        boolean z = false;
        if (i > i2) {
            shortSize = i2;
            longSize = i;
            this.mLandscapeRotation = 0;
            this.mSeascapeRotation = 2;
            if (res.getBoolean(17957010)) {
                this.mPortraitRotation = 1;
                this.mUpsideDownRotation = 3;
            } else {
                this.mPortraitRotation = 3;
                this.mUpsideDownRotation = 1;
            }
        } else {
            shortSize = i;
            longSize = i2;
            this.mPortraitRotation = 0;
            this.mUpsideDownRotation = 2;
            if (res.getBoolean(17957010)) {
                this.mLandscapeRotation = 3;
                this.mSeascapeRotation = 1;
            } else {
                this.mLandscapeRotation = 1;
                this.mSeascapeRotation = 3;
            }
        }
        int shortSizeDp = (shortSize * 160) / density;
        int longSizeDp = (longSize * 160) / density;
        this.mNavigationBarCanMove = i != i2 && shortSizeDp <= this.splitNavigationBarDp;
        if (this.mNavigationBarCanMove && (DEFAULT_ROTATION == 1 || DEFAULT_ROTATION == 3)) {
            this.mNavigationBarCanMove = false;
        }
        this.mHasNavigationBar = res.getBoolean(17957019);
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
        boolean isCar = this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive");
        boolean isTv = this.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
        if (((longSizeDp >= 960 && shortSizeDp >= 720) || isCar || isTv) && res.getBoolean(17956975) && !"true".equals(SystemProperties.get("config.override_forced_orient"))) {
            z = true;
        }
        this.mForceDefaultOrientation = z;
    }

    private boolean canHideNavigationBar() {
        return this.mHasNavigationBar;
    }

    public boolean isDefaultOrientationForced() {
        return this.mForceDefaultOrientation;
    }

    public void updateSettings() {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean updateRotation = false;
        synchronized (this.mLock) {
            int i = 2;
            this.mEndcallBehavior = Settings.System.getIntForUser(resolver, "end_button_behavior", 2, -2);
            this.mIncallPowerBehavior = Settings.Secure.getIntForUser(resolver, "incall_power_button_behavior", 1, -2);
            boolean z = false;
            this.mIncallBackBehavior = Settings.Secure.getIntForUser(resolver, "incall_back_button_behavior", 0, -2);
            this.mSystemNavigationKeysEnabled = Settings.Secure.getIntForUser(resolver, "system_navigation_keys_enabled", 0, -2) == 1;
            this.mRingerToggleChord = Settings.Secure.getIntForUser(resolver, "volume_hush_gesture", 0, -2);
            if (!this.mContext.getResources().getBoolean(17957069)) {
                this.mRingerToggleChord = 0;
            }
            int showRotationSuggestions = Settings.Secure.getIntForUser(resolver, "show_rotation_suggestions", 1, -2);
            if (this.mShowRotationSuggestions != showRotationSuggestions) {
                this.mShowRotationSuggestions = showRotationSuggestions;
                updateOrientationListenerLp();
            }
            boolean wakeGestureEnabledSetting = Settings.Secure.getIntForUser(resolver, "wake_gesture_enabled", 0, -2) != 0;
            if (this.mWakeGestureEnabledSetting != wakeGestureEnabledSetting) {
                this.mWakeGestureEnabledSetting = wakeGestureEnabledSetting;
                updateWakeGestureListenerLp();
            }
            int userRotation = Settings.System.getIntForUser(resolver, "user_rotation", 0, -2);
            if (this.mUserRotation != userRotation) {
                this.mUserRotation = userRotation;
                updateRotation = true;
            }
            int userRotationMode = Settings.System.getIntForUser(resolver, "accelerometer_rotation", 0, -2) != 0 ? 0 : 1;
            if (this.mUserRotationMode != userRotationMode) {
                this.mUserRotationMode = userRotationMode;
                updateRotation = true;
                updateOrientationListenerLp();
            }
            if (this.mSystemReady) {
                int pointerLocation = Settings.System.getIntForUser(resolver, "pointer_location", 0, -2);
                if (this.mPointerLocationMode != pointerLocation) {
                    this.mPointerLocationMode = pointerLocation;
                    Handler handler = this.mHandler;
                    if (pointerLocation != 0) {
                        i = 1;
                    }
                    handler.sendEmptyMessage(i);
                }
            }
            this.mLockScreenTimeout = Settings.System.getIntForUser(resolver, "screen_off_timeout", 0, -2);
            String imId = Settings.Secure.getStringForUser(resolver, "default_input_method", -2);
            boolean hasSoftInput = imId != null && imId.length() > 0;
            if (this.mHasSoftInput != hasSoftInput) {
                this.mHasSoftInput = hasSoftInput;
                updateRotation = true;
            }
            if (this.mImmersiveModeConfirmation != null) {
                this.mImmersiveModeConfirmation.loadSetting(this.mCurrentUserId);
            }
            if (Settings.Secure.getIntForUser(resolver, FINGER_PRINT_ENABLE, 0, -2) == 1) {
                z = true;
            }
            this.mIsFingerprintEnabledBySettings = z;
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

    /* access modifiers changed from: private */
    public boolean shouldEnableWakeGestureLp() {
        return this.mWakeGestureEnabledSetting && !this.mAwake && (!this.mLidControlsSleep || this.mLidState != 0) && this.mWakeGestureListener.isSupported();
    }

    /* access modifiers changed from: private */
    public void enablePointerLocation() {
        if (this.mPointerLocationView == null) {
            this.mPointerLocationView = new PointerLocationView(this.mContext);
            this.mPointerLocationView.setPrintCoords(false);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1);
            lp.type = 2015;
            lp.flags = 1304;
            lp.layoutInDisplayCutoutMode = 1;
            if (ActivityManager.isHighEndGfx()) {
                lp.flags |= DumpState.DUMP_SERVICE_PERMISSIONS;
                lp.privateFlags |= 2;
            }
            lp.format = -3;
            lp.setTitle("PointerLocation");
            lp.inputFeatures |= 2;
            ((WindowManager) this.mContext.getSystemService("window")).addView(this.mPointerLocationView, lp);
            this.mWindowManagerFuncs.registerPointerEventListener(this.mPointerLocationView);
        }
    }

    /* access modifiers changed from: private */
    public void disablePointerLocation() {
        if (this.mPointerLocationView != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.mPointerLocationView);
            ((WindowManager) this.mContext.getSystemService("window")).removeView(this.mPointerLocationView);
            this.mPointerLocationView = null;
        }
    }

    private int readRotation(int resID) {
        try {
            int rotation = this.mContext.getResources().getInteger(resID);
            if (rotation == 0) {
                return 0;
            }
            if (rotation == 90) {
                return 1;
            }
            if (rotation == 180) {
                return 2;
            }
            if (rotation == 270) {
                return 3;
            }
            return -1;
        } catch (Resources.NotFoundException e) {
        }
    }

    public int checkAddPermission(WindowManager.LayoutParams attrs, int[] outAppOp) {
        ApplicationInfo appInfo;
        int type = attrs.type;
        int i = 0;
        if (((attrs.privateFlags & DumpState.DUMP_DEXOPT) != 0) && this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") != 0) {
            return -8;
        }
        outAppOp[0] = -1;
        if ((type < 1 || type > 99) && ((type < 1000 || type > 1999) && (type < 2000 || type > 2999))) {
            return -10;
        }
        if (type < 2000 || type > 2999) {
            return 0;
        }
        if (WindowManager.LayoutParams.isSystemAlertWindowType(type)) {
            outAppOp[0] = 24;
            int callingUid = Binder.getCallingUid();
            if (UserHandle.getAppId(callingUid) == 1000) {
                return 0;
            }
            try {
                appInfo = this.mContext.getPackageManager().getApplicationInfoAsUser(attrs.packageName, 0, UserHandle.getUserId(callingUid));
            } catch (PackageManager.NameNotFoundException e) {
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
            switch (this.mAppOpsManager.noteOpNoThrow(outAppOp[0], callingUid, attrs.packageName)) {
                case 0:
                case 1:
                    return 0;
                case 2:
                    if (appInfo.targetSdkVersion < 23) {
                        return 0;
                    }
                    return -8;
                default:
                    if (this.mContext.checkCallingOrSelfPermission("android.permission.SYSTEM_ALERT_WINDOW") != 0) {
                        i = -8;
                    }
                    return i;
            }
        } else if (type != 2005) {
            if (!(type == 2011 || type == 2013 || type == 2023 || type == 2035 || type == 2037 || type == 2103)) {
                switch (type) {
                    case 2030:
                    case 2031:
                    case 2032:
                        break;
                    default:
                        if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") != 0) {
                            i = -8;
                        }
                        return i;
                }
            }
            return 0;
        } else {
            outAppOp[0] = 45;
            return 0;
        }
    }

    public boolean checkShowToOwnerOnly(WindowManager.LayoutParams attrs) {
        int i = attrs.type;
        boolean z = true;
        if (!(i == 3 || i == 2014 || i == 2024 || i == 2030 || i == 2034 || i == 2037)) {
            switch (i) {
                case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /*2000*/:
                case 2001:
                case 2002:
                    break;
                default:
                    switch (i) {
                        case 2007:
                        case 2008:
                        case 2009:
                            break;
                        default:
                            switch (i) {
                                case 2017:
                                case 2018:
                                case 2019:
                                case 2020:
                                case 2021:
                                case 2022:
                                    break;
                                default:
                                    switch (i) {
                                        case 2026:
                                        case 2027:
                                            break;
                                        default:
                                            if ((attrs.privateFlags & 16) == 0) {
                                                return true;
                                            }
                                            break;
                                    }
                            }
                    }
            }
        }
        if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") == 0) {
            z = false;
        }
        return z;
    }

    public void adjustWindowParamsLw(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs, boolean hasStatusBarServicePermission) {
        boolean isScreenDecor = (attrs.privateFlags & DumpState.DUMP_CHANGES) != 0;
        if (this.mScreenDecorWindows.contains(win)) {
            if (!isScreenDecor) {
                this.mScreenDecorWindows.remove(win);
            }
        } else if (isScreenDecor && hasStatusBarServicePermission) {
            this.mScreenDecorWindows.add(win);
        }
        int i = attrs.type;
        if (i != 2000) {
            if (i != 2013) {
                if (i != 2015) {
                    if (i != 2023) {
                        if (i != 2036) {
                            switch (i) {
                                case 2005:
                                    if (attrs.hideTimeoutMilliseconds < 0 || attrs.hideTimeoutMilliseconds > 3500) {
                                        attrs.hideTimeoutMilliseconds = 3500;
                                    }
                                    attrs.windowAnimations = 16973828;
                                    break;
                                case 2006:
                                    break;
                            }
                        } else {
                            attrs.flags |= 8;
                        }
                    }
                }
                attrs.flags |= 24;
                attrs.flags &= -262145;
            }
            attrs.layoutInDisplayCutoutMode = 1;
        } else if (this.mKeyguardOccluded) {
            attrs.flags &= -1048577;
            attrs.privateFlags &= -1025;
        }
        if (attrs.type != 2000) {
            attrs.privateFlags &= -1025;
        }
    }

    private int getImpliedSysUiFlagsForLayout(WindowManager.LayoutParams attrs) {
        int impliedFlags = 0;
        if ((attrs.flags & Integer.MIN_VALUE) != 0) {
            impliedFlags = 0 | 512;
        }
        boolean forceWindowDrawsStatusBarBackground = (attrs.privateFlags & 131072) != 0;
        if ((Integer.MIN_VALUE & attrs.flags) != 0 || (forceWindowDrawsStatusBarBackground && attrs.height == -1 && attrs.width == -1)) {
            return impliedFlags | 1024;
        }
        return impliedFlags;
    }

    /* access modifiers changed from: package-private */
    public void readLidState() {
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
        this.mHaveBuiltInKeyboard = (keyboardPresence & 1) != 0;
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

    public boolean getLayoutBeyondDisplayCutout() {
        return this.mLayoutBeyondDisplayCutout;
    }

    public void onOverlayChangedLw() {
        onConfigurationChanged();
    }

    public void onConfigurationChanged() {
        Resources res = getSystemUiContext().getResources();
        int[] iArr = this.mStatusBarHeightForRotation;
        int i = this.mPortraitRotation;
        int[] iArr2 = this.mStatusBarHeightForRotation;
        int i2 = this.mUpsideDownRotation;
        int dimensionPixelSize = res.getDimensionPixelSize(17105320);
        iArr2[i2] = dimensionPixelSize;
        iArr[i] = dimensionPixelSize;
        int[] iArr3 = this.mStatusBarHeightForRotation;
        int i3 = this.mLandscapeRotation;
        int[] iArr4 = this.mStatusBarHeightForRotation;
        int i4 = this.mSeascapeRotation;
        int dimensionPixelSize2 = res.getDimensionPixelSize(17105319);
        iArr4[i4] = dimensionPixelSize2;
        iArr3[i3] = dimensionPixelSize2;
        int[] iArr5 = this.mNavigationBarHeightForRotationDefault;
        int i5 = this.mPortraitRotation;
        int[] iArr6 = this.mNavigationBarHeightForRotationDefault;
        int i6 = this.mUpsideDownRotation;
        int dimensionPixelSize3 = res.getDimensionPixelSize(17105186);
        iArr6[i6] = dimensionPixelSize3;
        iArr5[i5] = dimensionPixelSize3;
        int[] iArr7 = this.mNavigationBarHeightForRotationDefault;
        int i7 = this.mLandscapeRotation;
        int[] iArr8 = this.mNavigationBarHeightForRotationDefault;
        int i8 = this.mSeascapeRotation;
        int dimensionPixelSize4 = res.getDimensionPixelSize(17105188);
        iArr8[i8] = dimensionPixelSize4;
        iArr7[i7] = dimensionPixelSize4;
        int[] iArr9 = this.mNavigationBarWidthForRotationDefault;
        int i9 = this.mPortraitRotation;
        int[] iArr10 = this.mNavigationBarWidthForRotationDefault;
        int i10 = this.mUpsideDownRotation;
        int[] iArr11 = this.mNavigationBarWidthForRotationDefault;
        int i11 = this.mLandscapeRotation;
        int[] iArr12 = this.mNavigationBarWidthForRotationDefault;
        int i12 = this.mSeascapeRotation;
        int dimensionPixelSize5 = res.getDimensionPixelSize(17105191);
        iArr12[i12] = dimensionPixelSize5;
        iArr11[i11] = dimensionPixelSize5;
        iArr10[i10] = dimensionPixelSize5;
        iArr9[i9] = dimensionPixelSize5;
        this.mDefaultNavBarHeight = res.getDimensionPixelSize(17105186);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Context getSystemUiContext() {
        return ActivityThread.currentActivityThread().getSystemUiContext();
    }

    public int getMaxWallpaperLayer() {
        return getWindowLayerFromTypeLw(IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
    }

    private int getNavigationBarWidth(int rotation, int uiMode) {
        return this.mNavigationBarWidthForRotationDefault[rotation];
    }

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId, DisplayCutout displayCutout) {
        int width = fullWidth;
        if (displayId == 0 && this.mHasNavigationBar && this.mNavigationBarCanMove && fullWidth > fullHeight) {
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

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId, DisplayCutout displayCutout) {
        int height = fullHeight;
        if (displayId == 0 && this.mHasNavigationBar && (!this.mNavigationBarCanMove || fullWidth < fullHeight)) {
            height -= getNavigationBarHeight(rotation, uiMode);
        }
        if (displayCutout != null) {
            return height - (displayCutout.getSafeInsetTop() + displayCutout.getSafeInsetBottom());
        }
        return height;
    }

    public int getConfigDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId, DisplayCutout displayCutout) {
        return getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode, displayId, displayCutout);
    }

    public int getConfigDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId, DisplayCutout displayCutout) {
        if (displayId != 0) {
            return fullHeight;
        }
        int statusBarHeight = this.mStatusBarHeightForRotation[rotation];
        if (displayCutout != null) {
            statusBarHeight = Math.max(0, statusBarHeight - displayCutout.getSafeInsetTop());
        }
        return getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayId, displayCutout) - statusBarHeight;
    }

    public boolean isKeyguardHostWindow(WindowManager.LayoutParams attrs) {
        return attrs.type == 2000;
    }

    public boolean canBeHiddenByKeyguardLw(WindowManagerPolicy.WindowState win) {
        int i = win.getAttrs().type;
        boolean z = false;
        if (i == 2000 || i == 2013 || i == 2019 || i == 2023) {
            return false;
        }
        if (getWindowLayerLw(win) < getWindowLayerFromTypeLw(IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME)) {
            z = true;
        }
        return z;
    }

    private boolean shouldBeHiddenByKeyguard(WindowManagerPolicy.WindowState win, WindowManagerPolicy.WindowState imeTarget) {
        boolean z = false;
        if (win.getAppToken() != null) {
            return false;
        }
        WindowManager.LayoutParams attrs = win.getAttrs();
        boolean allowWhenLocked = (win.isInputMethodWindow() || imeTarget == this) && (imeTarget != null && imeTarget.isVisibleLw() && ((imeTarget.getAttrs().flags & DumpState.DUMP_FROZEN) != 0 || !canBeHiddenByKeyguardLw(imeTarget)));
        if (isKeyguardLocked() && isKeyguardOccluded()) {
            allowWhenLocked |= ((524288 & attrs.flags) == 0 && (attrs.privateFlags & 256) == 0) ? false : true;
        }
        boolean keyguardLocked = isKeyguardLocked();
        boolean hideDockDivider = attrs.type == 2034 && !this.mWindowManagerInternal.isStackVisible(3);
        boolean hideIme = win.isInputMethodWindow() && (this.mAodShowing || !this.mWindowManagerDrawComplete);
        if ((keyguardLocked && !allowWhenLocked && win.getDisplayId() == 0) || hideDockDivider || hideIme) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: type inference failed for: r19v8, types: [boolean] */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x023e, code lost:
        if (r4.getParent() != null) goto L_0x024b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0240, code lost:
        android.util.Log.w(TAG, "view not successfully added to wm, removing view");
        r3.removeViewImmediate(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x024b, code lost:
        return r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x024c, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x024f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0250, code lost:
        r1 = r5;
        r5 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x0254, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0255, code lost:
        r1 = r5;
        r5 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x0259, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x025a, code lost:
        r4 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x025e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x025f, code lost:
        r1 = r5;
        r5 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x0264, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x0265, code lost:
        r1 = r5;
        r5 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x026a, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x026b, code lost:
        r6 = r34;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x026e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x026f, code lost:
        r6 = r34;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x0272, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0273, code lost:
        r6 = r34;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x0289, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x028b, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x028c, code lost:
        r1 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x028e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x028f, code lost:
        r1 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x02ec, code lost:
        if (r5.getParent() == null) goto L_0x02ee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x02ee, code lost:
        android.util.Log.w(TAG, "view not successfully added to wm, removing view");
        r3.removeViewImmediate(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x032a, code lost:
        if (r5.getParent() == null) goto L_0x02ee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0133, code lost:
        if (android.hwcontrol.HwWidgetFactory.isHwDarkTheme(r1) == false) goto L_0x0139;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0135, code lost:
        r5 = 134217728 | r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0139, code lost:
        r2.setFlags(((r5 | 16) | 8) | 131072, ((r5 | 16) | 8) | 131072);
        r2.setDefaultIcon(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:?, code lost:
        r2.setDefaultLogo(r34);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x015c, code lost:
        if ((android.os.SystemProperties.getInt("persist.sys.navigationbar.mode", 0) & 2) == 0) goto L_0x0178;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x015e, code lost:
        r2.getDecorView().setSystemUiVisibility(r2.getDecorView().getSystemUiVisibility() | 512);
        r2.addFlags(Integer.MIN_VALUE);
        r2.setNavigationBarColor(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0178, code lost:
        r2.setLayout(-1, -1);
        r0 = r2.getAttributes();
        r0.token = r8;
        r0.packageName = r9;
        r24 = r3;
        r0.windowAnimations = r2.getWindowStyle().getResourceId(8, 0);
        r0.privateFlags |= 1;
        r0.privateFlags |= 16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01a3, code lost:
        if (r30.supportsScreen() != false) goto L_0x01ab;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x01a5, code lost:
        r0.privateFlags |= 128;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01ab, code lost:
        r0.setTitle("Splash Screen " + r9);
        addSplashscreenContent(r2, r1);
        r3 = (android.view.WindowManager) r1.getSystemService("window");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:?, code lost:
        r4 = r2.getDecorView();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:?, code lost:
        r14 = new java.lang.StringBuilder();
        r25 = r1;
        r14.append("addStartingWindow ");
        r14.append(r9);
        r14.append(": nonLocalizedLabel=");
        r14.append(r11);
        r14.append(" theme=");
        r14.append(java.lang.Integer.toHexString(r29));
        r14.append(" windowFlags=");
        r14.append(java.lang.Integer.toHexString(r5));
        r14.append(" isFloating=");
        r14.append(r2.isFloating());
        r14.append(" appToken=");
        r14.append(r8);
        android.util.Flog.i(301, r14.toString());
        setHasAcitionBar(r2.hasFeature(8));
        r3.addView(r4, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x022b, code lost:
        if (r4.getParent() == null) goto L_0x0235;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x0232, code lost:
        r20 = new com.android.server.policy.SplashScreenSurface(r4, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0235, code lost:
        r20 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0238, code lost:
        if (r4 == null) goto L_0x024b;
     */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x02e8  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x0326  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0103 A[Catch:{ BadTokenException -> 0x02a8, RuntimeException -> 0x029e, all -> 0x0293 }] */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x010f A[Catch:{ BadTokenException -> 0x02a8, RuntimeException -> 0x029e, all -> 0x0293 }] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0113 A[Catch:{ BadTokenException -> 0x02a8, RuntimeException -> 0x029e, all -> 0x0293 }] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0122 A[SYNTHETIC, Splitter:B:69:0x0122] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public WindowManagerPolicy.StartingSurface addSplashScreen(IBinder appToken, String packageName, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, Configuration overrideConfig, int displayId) {
        View view;
        WindowManager wm;
        int windowFlags2;
        int windowFlags3;
        WindowManager wm2;
        View view2;
        boolean z;
        CharSequence label;
        int windowFlags4;
        Context overrideContext;
        TypedArray typedArray;
        IBinder iBinder = appToken;
        String str = packageName;
        int i = theme;
        CharSequence charSequence = nonLocalizedLabel;
        int i2 = labelRes;
        final int i3 = icon;
        Configuration configuration = overrideConfig;
        if (str == null) {
            return null;
        }
        WindowManager wm3 = null;
        View view3 = null;
        try {
            Context displayContext = getDisplayContext(this.mContext, displayId);
            if (displayContext == null) {
                if (view3 != null && view3.getParent() == null) {
                    Log.w(TAG, "view not successfully added to wm, removing view");
                    wm3.removeViewImmediate(view3);
                }
                return null;
            }
            Context context = displayContext;
            if (!(i == context.getThemeResId() && i2 == 0)) {
                try {
                    context = context.createPackageContext(str, 4);
                    context.setTheme(i);
                } catch (PackageManager.NameNotFoundException e) {
                } catch (WindowManager.BadTokenException e2) {
                    e = e2;
                    CompatibilityInfo compatibilityInfo = compatInfo;
                    windowFlags3 = windowFlags;
                    wm = wm3;
                    int i4 = logo;
                    Log.w(TAG, iBinder + " already running, starting window not displayed. " + e.getMessage());
                    if (view3 != null) {
                    }
                    return null;
                } catch (RuntimeException e3) {
                    e = e3;
                    CompatibilityInfo compatibilityInfo2 = compatInfo;
                    windowFlags2 = windowFlags;
                    wm = wm3;
                    int i5 = logo;
                    try {
                        Log.w(TAG, iBinder + " failed creating starting window", e);
                        if (view3 != null) {
                        }
                        return null;
                    } catch (Throwable th) {
                        th = th;
                        view = view3;
                        int i6 = windowFlags2;
                        Log.w(TAG, "view not successfully added to wm, removing view");
                        wm.removeViewImmediate(view);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    CompatibilityInfo compatibilityInfo3 = compatInfo;
                    view = view3;
                    wm = wm3;
                    int i7 = logo;
                    int i8 = windowFlags;
                    Log.w(TAG, "view not successfully added to wm, removing view");
                    wm.removeViewImmediate(view);
                    throw th;
                }
            }
            Context context2 = context;
            Context contextStartWindow = null;
            if (configuration != null) {
                if (!configuration.equals(Configuration.EMPTY)) {
                    Context overrideContext2 = context2.createConfigurationContext(configuration);
                    overrideContext2.setTheme(i);
                    TypedArray typedArray2 = overrideContext2.obtainStyledAttributes(R.styleable.Window);
                    ? isHwStartWindowEnabled = isHwStartWindowEnabled(0);
                    if (isHwStartWindowEnabled != 0) {
                        try {
                            WindowManager wm4 = isHwStartWindowEnabled;
                            WindowManager wm5 = isHwStartWindowEnabled;
                            WindowManager wm6 = isHwStartWindowEnabled;
                            overrideContext = overrideContext2;
                            TypedArray typedArray3 = typedArray2;
                            z = true;
                            view2 = view3;
                            WindowManager windowManager = wm3;
                            try {
                                wm4 = windowManager;
                                wm5 = windowManager;
                                wm6 = windowManager;
                                contextStartWindow = addHwStartWindow(compatInfo.mAppInfo, overrideContext, context2, typedArray3, windowFlags);
                                if (contextStartWindow != null) {
                                    context2 = contextStartWindow;
                                    typedArray = typedArray3;
                                    typedArray.recycle();
                                    wm2 = windowManager;
                                } else {
                                    typedArray = typedArray3;
                                    wm2 = windowManager;
                                }
                            } catch (WindowManager.BadTokenException e4) {
                                e = e4;
                                int i9 = logo;
                                windowFlags3 = windowFlags;
                                wm2 = wm4;
                                view3 = view2;
                                wm = wm2;
                                Log.w(TAG, iBinder + " already running, starting window not displayed. " + e.getMessage());
                                if (view3 != null) {
                                }
                                return null;
                            } catch (RuntimeException e5) {
                                e = e5;
                                int i10 = logo;
                                windowFlags2 = windowFlags;
                                wm2 = wm5;
                                view3 = view2;
                                wm = wm2;
                                Log.w(TAG, iBinder + " failed creating starting window", e);
                                if (view3 != null) {
                                }
                                return null;
                            } catch (Throwable th3) {
                                th = th3;
                                int i11 = logo;
                                int i12 = windowFlags;
                                wm2 = wm6;
                                view = view2;
                                wm = wm2;
                                Log.w(TAG, "view not successfully added to wm, removing view");
                                wm.removeViewImmediate(view);
                                throw th;
                            }
                        } catch (WindowManager.BadTokenException e6) {
                            e = e6;
                            View view4 = view3;
                            WindowManager windowManager2 = wm3;
                            int i13 = logo;
                            windowFlags3 = windowFlags;
                            wm = windowManager2;
                            Log.w(TAG, iBinder + " already running, starting window not displayed. " + e.getMessage());
                            if (view3 != null) {
                            }
                            return null;
                        } catch (RuntimeException e7) {
                            e = e7;
                            View view5 = view3;
                            WindowManager windowManager3 = wm3;
                            int i14 = logo;
                            windowFlags2 = windowFlags;
                            wm = windowManager3;
                            Log.w(TAG, iBinder + " failed creating starting window", e);
                            if (view3 != null) {
                            }
                            return null;
                        } catch (Throwable th4) {
                            th = th4;
                            View view6 = view3;
                            WindowManager windowManager4 = wm3;
                            int i15 = logo;
                            int i16 = windowFlags;
                            view = view6;
                            wm = windowManager4;
                            Log.w(TAG, "view not successfully added to wm, removing view");
                            wm.removeViewImmediate(view);
                            throw th;
                        }
                    } else {
                        CompatibilityInfo compatibilityInfo4 = compatInfo;
                        overrideContext = overrideContext2;
                        typedArray = typedArray2;
                        view2 = view3;
                        wm2 = wm3;
                        z = true;
                    }
                    if (contextStartWindow == null) {
                        int resId = typedArray.getResourceId(z, 0);
                        if (resId != 0) {
                            Context overrideContext3 = overrideContext;
                            if (overrideContext3.getDrawable(resId) != null) {
                                context2 = overrideContext3;
                            }
                        }
                        typedArray.recycle();
                    }
                    Context context3 = context2;
                    final PhoneWindow win = HwPolicyFactory.getHwPhoneWindow(context3);
                    win.setIsStartingWindow(z);
                    label = context3.getResources().getText(i2, null);
                    if (i3 != 0) {
                        new Thread("iconPreloadingThread") {
                            public void run() {
                                try {
                                    win.getContext().getDrawable(i3);
                                } catch (Resources.NotFoundException e) {
                                    Log.w(PhoneWindowManager.TAG, i3 + " NotFoundException");
                                }
                            }
                        }.start();
                    }
                    if (label == null) {
                        win.setTitle(label, z);
                    } else {
                        win.setTitle(charSequence, false);
                    }
                    win.setType(3);
                    synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
                        try {
                            if (this.mKeyguardOccluded) {
                                windowFlags4 = windowFlags | DumpState.DUMP_FROZEN;
                            } else {
                                windowFlags4 = windowFlags;
                            }
                            try {
                            } catch (Throwable th5) {
                                th = th5;
                                int i17 = logo;
                                Context context4 = context3;
                                CharSequence charSequence2 = label;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th6) {
                                        th = th6;
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            int i18 = logo;
                            Context context5 = context3;
                            CharSequence charSequence3 = label;
                            windowFlags4 = windowFlags;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                }
            }
            CompatibilityInfo compatibilityInfo5 = compatInfo;
            view2 = view3;
            wm2 = wm3;
            z = true;
            Context context32 = context2;
            final PhoneWindow win2 = HwPolicyFactory.getHwPhoneWindow(context32);
            win2.setIsStartingWindow(z);
            label = context32.getResources().getText(i2, null);
            if (i3 != 0) {
            }
            if (label == null) {
            }
            win2.setType(3);
            synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
            }
        } catch (WindowManager.BadTokenException e8) {
            e = e8;
            CompatibilityInfo compatibilityInfo6 = compatInfo;
            View view42 = view3;
            WindowManager windowManager22 = wm3;
            int i132 = logo;
            windowFlags3 = windowFlags;
            wm = windowManager22;
            Log.w(TAG, iBinder + " already running, starting window not displayed. " + e.getMessage());
            if (view3 != null) {
            }
            return null;
        } catch (RuntimeException e9) {
            e = e9;
            CompatibilityInfo compatibilityInfo7 = compatInfo;
            View view52 = view3;
            WindowManager windowManager32 = wm3;
            int i142 = logo;
            windowFlags2 = windowFlags;
            wm = windowManager32;
            Log.w(TAG, iBinder + " failed creating starting window", e);
            if (view3 != null) {
            }
            return null;
        } catch (Throwable th8) {
            th = th8;
            CompatibilityInfo compatibilityInfo8 = compatInfo;
            View view62 = view3;
            WindowManager windowManager42 = wm3;
            int i152 = logo;
            int i162 = windowFlags;
            view = view62;
            wm = windowManager42;
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                wm.removeViewImmediate(view);
            }
            throw th;
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002f, code lost:
        if (r0 != 2033) goto L_0x009d;
     */
    public int prepareAddWindowLw(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs) {
        if ((attrs.privateFlags & DumpState.DUMP_CHANGES) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
            this.mScreenDecorWindows.add(win);
        }
        int i = attrs.type;
        if (i != 2000) {
            if (!(i == 2014 || i == 2017)) {
                if (i == 2019) {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                    if (this.mNavigationBar != null && this.mNavigationBar.isAlive()) {
                        return -7;
                    }
                    this.mNavigationBar = win;
                    this.mNavigationBarController.setWindow(win);
                    this.mNavigationBarController.setOnBarVisibilityChangedListener(this.mNavBarVisibilityListener, true);
                    Flog.i(303, "mNavigationBar:" + this.mNavigationBar);
                } else if (i != 2024) {
                }
            }
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
        } else {
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
            if (this.mStatusBar != null && this.mStatusBar.isAlive()) {
                return -7;
            }
            this.mStatusBar = win;
            this.mStatusBarController.setWindow(win);
            setKeyguardOccludedLw(this.mKeyguardOccluded, true);
        }
        return 0;
    }

    public void removeWindowLw(WindowManagerPolicy.WindowState win) {
        if (this.mStatusBar == win) {
            this.mStatusBar = null;
            this.mStatusBarController.setWindow(null);
        } else if (this.mNavigationBar == win) {
            this.mNavigationBar = null;
            this.mNavigationBarController.setWindow(null);
        }
        this.mScreenDecorWindows.remove(win);
    }

    public int selectAnimationLw(WindowManagerPolicy.WindowState win, int transit) {
        if (win == this.mStatusBar) {
            boolean isKeyguard = (win.getAttrs().privateFlags & 1024) != 0;
            boolean expanded = win.getAttrs().height == -1 && win.getAttrs().width == -1;
            if (isKeyguard || expanded || this.mIsNotchSwitchOpen) {
                return -1;
            }
            if (transit == 2 || transit == 4) {
                return 17432624;
            }
            if (transit == 1 || transit == 3) {
                return 17432623;
            }
        } else if (win == this.mNavigationBar) {
            if (win.getAttrs().windowAnimations != 0) {
                return 0;
            }
            if (this.mNavigationBarPosition == 4) {
                if (transit == 2 || transit == 4) {
                    if (isKeyguardShowingAndNotOccluded()) {
                        return 17432618;
                    }
                    return 17432617;
                } else if (transit == 1 || transit == 3) {
                    return 17432616;
                }
            } else if (this.mNavigationBarPosition == 2) {
                if (transit == 2 || transit == 4) {
                    return 17432622;
                }
                if (transit == 1 || transit == 3) {
                    return 17432621;
                }
            } else if (this.mNavigationBarPosition == 1) {
                if (transit == 2 || transit == 4) {
                    return 17432620;
                }
                if (transit == 1 || transit == 3) {
                    return 17432619;
                }
            }
        } else if (win.getAttrs().type == 2034) {
            if ((win.getAttrs().flags & 536870912) != 0) {
                return selectDockedDividerAnimationLw(win, transit);
            }
            return 0;
        }
        if (transit != 5) {
            return (win.getAttrs().type == 2023 && this.mDreamingLockscreen && transit == 1) ? -1 : 0;
        }
        if (win.hasAppShownWindows()) {
            return 17432597;
        }
    }

    private int selectDockedDividerAnimationLw(WindowManagerPolicy.WindowState win, int transit) {
        int insets = this.mWindowManagerFuncs.getDockedDividerInsetsLw();
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
        if (!this.mScreenOnFully || !okToAnimate()) {
            anim[0] = 17432696;
            anim[1] = 17432695;
            return;
        }
        if (this.mTopFullscreenOpaqueWindowState != null) {
            int animationHint = this.mTopFullscreenOpaqueWindowState.getRotationAnimationHint();
            if (animationHint < 0 && this.mTopIsFullscreen) {
                animationHint = this.mTopFullscreenOpaqueWindowState.getAttrs().rotationAnimation;
            }
            switch (animationHint) {
                case 1:
                case 3:
                    anim[0] = 17432697;
                    anim[1] = 17432695;
                    break;
                case 2:
                    anim[0] = 17432696;
                    anim[1] = 17432695;
                    break;
                default:
                    anim[1] = 0;
                    anim[0] = 0;
                    break;
            }
        } else {
            anim[1] = 0;
            anim[0] = 0;
        }
    }

    public boolean validateRotationAnimationLw(int exitAnimId, int enterAnimId, boolean forceDefault) {
        boolean z = true;
        switch (exitAnimId) {
            case 17432696:
            case 17432697:
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
        int i;
        if (goingToNotificationShade) {
            return AnimationUtils.loadAnimation(this.mContext, 17432667);
        }
        Context context = this.mContext;
        if (onWallpaper) {
            i = 17432668;
        } else {
            i = 17432666;
        }
        return (AnimationSet) AnimationUtils.loadAnimation(context, i);
    }

    public Animation createKeyguardWallpaperExit(boolean goingToNotificationShade) {
        if (goingToNotificationShade) {
            return null;
        }
        return AnimationUtils.loadAnimation(this.mContext, 17432672);
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

    /* access modifiers changed from: package-private */
    public TelecomManager getTelecommService() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }

    static IAudioService getAudioService() {
        IAudioService audioService = IAudioService.Stub.asInterface(ServiceManager.checkService("audio"));
        if (audioService == null) {
            Log.w(TAG, "Unable to find IAudioService interface.");
        }
        return audioService;
    }

    /* access modifiers changed from: package-private */
    public boolean keyguardOn() {
        return isKeyguardShowingAndNotOccluded() || inKeyguardRestrictedKeyInputMode();
    }

    public long interceptKeyBeforeDispatching(WindowManagerPolicy.WindowState win, KeyEvent event, int policyFlags) {
        long j;
        KeyEvent keyEvent = event;
        boolean keyguardOn = keyguardOn();
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int metaState = event.getMetaState();
        int flags = event.getFlags();
        boolean down = event.getAction() == 0;
        boolean canceled = event.isCanceled();
        if (HWFLOW) {
            Log.i(TAG, "interceptKeyTi keyCode=" + keyCode + " down=" + down + " repeatCount=" + repeatCount + " keyguardOn=" + keyguardOn + " mHomePressed=" + this.mHomePressed + " canceled=" + canceled);
        }
        if (this.mScreenshotChordEnabled && (flags & 1024) == 0) {
            if (this.mScreenshotChordVolumeDownKeyTriggered && !this.mScreenshotChordPowerKeyTriggered) {
                long now = SystemClock.uptimeMillis();
                long timeoutTime = this.mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
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
            if (this.mScreenshotChordVolumeDownKeyTriggered ^ this.mA11yShortcutChordVolumeUpKeyTriggered) {
                long now2 = SystemClock.uptimeMillis();
                long timeoutTime2 = (this.mScreenshotChordVolumeDownKeyTriggered ? this.mScreenshotChordVolumeDownKeyTime : this.mA11yShortcutChordVolumeUpKeyTime) + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now2 < timeoutTime2) {
                    return timeoutTime2 - now2;
                }
            }
            if (keyCode == 25 && this.mScreenshotChordVolumeDownKeyConsumed) {
                if (!down) {
                    this.mScreenshotChordVolumeDownKeyConsumed = false;
                }
                Log.d(TAG, "volume_down is consumed");
                return -1;
            } else if (keyCode == 24 && this.mA11yShortcutChordVolumeUpKeyConsumed) {
                if (!down) {
                    this.mA11yShortcutChordVolumeUpKeyConsumed = false;
                }
                Log.d(TAG, "volume_up is consumed");
                return -1;
            }
        }
        if (this.mRingerToggleChord != 0 && (flags & 1024) == 0) {
            if (this.mA11yShortcutChordVolumeUpKeyTriggered && !this.mScreenshotChordPowerKeyTriggered) {
                long now3 = SystemClock.uptimeMillis();
                long timeoutTime3 = this.mA11yShortcutChordVolumeUpKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now3 < timeoutTime3) {
                    return timeoutTime3 - now3;
                }
            }
            if (keyCode == 24 && this.mA11yShortcutChordVolumeUpKeyConsumed) {
                if (!down) {
                    this.mA11yShortcutChordVolumeUpKeyConsumed = false;
                }
                Log.d(TAG, "volume_up is consumed by A11");
                return -1;
            }
        }
        HwFrameworkFactory.getHwApsImpl().StopSdrForSpecial("interceptKeyBeforeDispatching", keyCode);
        if (this.mPendingMetaAction && !KeyEvent.isMetaKey(keyCode)) {
            this.mPendingMetaAction = false;
        }
        if (this.mPendingCapsLockToggle && !KeyEvent.isMetaKey(keyCode) && !KeyEvent.isAltKey(keyCode)) {
            this.mPendingCapsLockToggle = false;
        }
        if (keyCode != 3) {
            int type = 2;
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
                        Jlog.d(NetworkConstants.ICMPV6_NEIGHBOR_SOLICITATION, "JLID_SYSTEMUI_START_RECENT");
                        toggleRecentApps();
                    }
                }
                return -1;
            } else if (keyCode != 42 || !event.isMetaPressed()) {
                if (keyCode != 47 || !event.isMetaPressed() || !event.isCtrlPressed()) {
                    if (keyCode != 76 || !event.isMetaPressed()) {
                        if (keyCode == 219) {
                            Slog.wtf(TAG, "KEYCODE_ASSIST should be handled in interceptKeyBeforeQueueing");
                            return -1;
                        } else if (keyCode == 231) {
                            Slog.wtf(TAG, "KEYCODE_VOICE_ASSIST should be handled in interceptKeyBeforeQueueing");
                            return -1;
                        } else if (keyCode == 120) {
                            if (down && repeatCount == 0) {
                                this.mScreenshotRunnable.setScreenshotType(1);
                                this.mHandler.post(this.mScreenshotRunnable);
                            }
                            return -1;
                        } else {
                            if (keyCode == 221) {
                            } else if (keyCode == 220) {
                                int i = flags;
                            } else if (keyCode == 24 || keyCode == 25 || keyCode == 164) {
                                if (this.mUseTvRouting) {
                                } else if (this.mHandleVolumeKeysInWM) {
                                    int i2 = flags;
                                } else if (this.mPersistentVrModeEnabled) {
                                    InputDevice d = event.getDevice();
                                    if (d != null && !d.isExternal()) {
                                        Log.d(TAG, "volume key is consumed by VrMode");
                                        return -1;
                                    }
                                }
                                dispatchDirectAudioEvent(keyEvent);
                                Log.d(TAG, "volume key is consumed tvRouting:" + this.mUseTvRouting + ",KeysInWM:" + this.mHandleVolumeKeysInWM);
                                return -1;
                            } else if (keyCode == 61 && event.isMetaPressed()) {
                                return 0;
                            } else {
                                if (this.mHasFeatureLeanback && interceptBugreportGestureTv(keyCode, down)) {
                                    return -1;
                                }
                                if (this.mHasFeatureLeanback && interceptAccessibilityGestureTv(keyCode, down)) {
                                    return -1;
                                }
                                if (keyCode == 284) {
                                    if (!down) {
                                        this.mHandler.removeMessages(25);
                                        Message msg = this.mHandler.obtainMessage(25);
                                        msg.setAsynchronous(true);
                                        msg.sendToTarget();
                                    }
                                    return -1;
                                }
                            }
                            if (down) {
                                int direction = keyCode == 221 ? 1 : -1;
                                if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -3) != 0) {
                                    Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -3);
                                }
                                int min = this.mPowerManager.getMinimumScreenBrightnessSetting();
                                int max = this.mPowerManager.getMaximumScreenBrightnessSetting();
                                int i3 = direction;
                                Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness", Math.max(min, Math.min(max, Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", this.mPowerManager.getDefaultScreenBrightnessSetting(), -3) + (((((max - min) + 10) - 1) / 10) * direction))), -3);
                                startActivityAsUser(new Intent("com.android.intent.action.SHOW_BRIGHTNESS_DIALOG"), UserHandle.CURRENT_OR_SELF);
                            }
                            return -1;
                        }
                    } else if (down && repeatCount == 0 && !isKeyguardLocked()) {
                        toggleKeyboardShortcutsMenu(event.getDeviceId());
                    }
                } else if (down && repeatCount == 0) {
                    if (!event.isShiftPressed()) {
                        type = 1;
                    }
                    this.mScreenshotRunnable.setScreenshotType(type);
                    this.mHandler.post(this.mScreenshotRunnable);
                    return -1;
                }
            } else if (down) {
                IStatusBarService service = getStatusBarService();
                if (service != null) {
                    try {
                        service.expandNotificationsPanel();
                    } catch (RemoteException e) {
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
            boolean actionTriggered2 = actionTriggered;
            this.mMetaState = metaState;
            if (actionTriggered2) {
                return -1;
            }
            if (KeyEvent.isMetaKey(keyCode)) {
                if (down) {
                    this.mPendingMetaAction = true;
                } else if (this.mPendingMetaAction) {
                    if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
                        Slog.w(TAG, "Not start voice assistant because on OOBE.");
                        return -1;
                    }
                    j = -1;
                    launchAssistAction("android.intent.extra.ASSIST_INPUT_HINT_KEYBOARD", event.getDeviceId());
                    return j;
                }
                j = -1;
                return j;
            }
            if (this.mSearchKeyShortcutPending) {
                KeyCharacterMap kcm = event.getKeyCharacterMap();
                if (kcm.isPrintingKey(keyCode)) {
                    this.mConsumeSearchKeyUp = true;
                    this.mSearchKeyShortcutPending = false;
                    if (!down || repeatCount != 0 || keyguardOn) {
                    } else {
                        Intent shortcutIntent = this.mShortcutManager.getIntent(kcm, keyCode, metaState);
                        if (shortcutIntent != null) {
                            shortcutIntent.addFlags(268435456);
                            try {
                                startActivityAsUser(shortcutIntent, UserHandle.CURRENT);
                                dismissKeyboardShortcutsMenu();
                                int i4 = flags;
                            } catch (ActivityNotFoundException ex) {
                                StringBuilder sb = new StringBuilder();
                                int i5 = flags;
                                sb.append("Dropping shortcut key combination because the activity to which it is registered was not found: SEARCH+");
                                sb.append(KeyEvent.keyCodeToString(keyCode));
                                Slog.w(TAG, sb.toString(), ex);
                            }
                        } else {
                            Slog.i(TAG, "Dropping unregistered shortcut key combination: SEARCH+" + KeyEvent.keyCodeToString(keyCode));
                        }
                    }
                    return -1;
                }
            }
            if (down && repeatCount == 0 && !keyguardOn && (65536 & metaState) != 0) {
                KeyCharacterMap kcm2 = event.getKeyCharacterMap();
                if (kcm2.isPrintingKey(keyCode)) {
                    Intent shortcutIntent2 = this.mShortcutManager.getIntent(kcm2, keyCode, -458753 & metaState);
                    if (shortcutIntent2 != null) {
                        shortcutIntent2.addFlags(268435456);
                        try {
                            startActivityAsUser(shortcutIntent2, UserHandle.CURRENT);
                            dismissKeyboardShortcutsMenu();
                        } catch (ActivityNotFoundException ex2) {
                            Slog.w(TAG, "Dropping shortcut key combination because the activity to which it is registered was not found: META+" + KeyEvent.keyCodeToString(keyCode), ex2);
                        }
                        return -1;
                    }
                }
            }
            if (down && repeatCount == 0 && !keyguardOn) {
                String category = sApplicationLaunchKeyCategories.get(keyCode);
                if (category != null) {
                    Intent intent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", category);
                    intent.setFlags(268435456);
                    if (ActivityManager.isUserAMonkey()) {
                        intent.addFlags(DumpState.DUMP_COMPILER_STATS);
                    }
                    try {
                        startActivityAsUser(intent, UserHandle.CURRENT);
                        dismissKeyboardShortcutsMenu();
                    } catch (ActivityNotFoundException ex3) {
                        Slog.w(TAG, "Dropping application launch key because the activity to which it is registered was not found: keyCode=" + keyCode + ", category=" + category, ex3);
                    }
                    return -1;
                }
            }
            if (down && repeatCount == 0 && keyCode == 61) {
                if (this.mRecentAppsHeldModifiers == 0 && !keyguardOn && isUserSetupComplete()) {
                    int shiftlessModifiers = event.getModifiers() & -194;
                    if (KeyEvent.metaStateHasModifiers(shiftlessModifiers, 2)) {
                        this.mRecentAppsHeldModifiers = shiftlessModifiers;
                        showRecentApps(true);
                        return -1;
                    }
                }
            } else if (!down && this.mRecentAppsHeldModifiers != 0 && (this.mRecentAppsHeldModifiers & metaState) == 0) {
                this.mRecentAppsHeldModifiers = 0;
                hideRecentApps(true, false);
            }
            if (down && repeatCount == 0 && keyCode == 62 && (metaState & 28672) != 0) {
                this.mWindowManagerFuncs.switchKeyboardLayout(event.getDeviceId(), (metaState & HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS) != 0 ? -1 : 1);
                return -1;
            } else if (down && repeatCount == 0 && (keyCode == 204 || (keyCode == 62 && (458752 & metaState) != 0))) {
                this.mWindowManagerFuncs.switchInputMethod((metaState & HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS) == 0);
                return -1;
            } else if (this.mLanguageSwitchKeyPressed && !down && (keyCode == 204 || keyCode == 62)) {
                this.mLanguageSwitchKeyPressed = false;
                return -1;
            } else if (isValidGlobalKey(keyCode) && this.mGlobalKeyManager.handleGlobalKey(this.mContext, keyCode, keyEvent)) {
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
                    IShortcutService shortcutService = this.mShortcutKeyServices.get(shortcutCode);
                    if (shortcutService != null) {
                        try {
                            if (isUserSetupComplete()) {
                                shortcutService.notifyShortcutKeyPressed(shortcutCode);
                            }
                        } catch (RemoteException e2) {
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
        } else if (!down) {
            cancelPreloadRecentApps();
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
                Jlog.d(382, "JLID_HOME_KEY_PRESS");
                handleShortPressOnHome();
                try {
                    if (!(this.mWindowManager == null || -1 == this.mWindowManager.getDockedStackSide())) {
                        uploadKeyEvent(3);
                    }
                } catch (RemoteException e3) {
                    Slog.e(TAG, "Remote Exception failed getting dockside!");
                }
                return -1;
            }
        } else {
            WindowManager.LayoutParams attrs = win != null ? win.getAttrs() : null;
            if (attrs != null) {
                int type2 = attrs.type;
                if (type2 == 2009 || (attrs.privateFlags & 1024) != 0) {
                    return 0;
                }
                for (int i6 : WINDOW_TYPES_WHERE_HOME_DOESNT_WORK) {
                    if (type2 == i6) {
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
            } else if ((event.getFlags() & 128) != 0 && !keyguardOn) {
                handleLongPressOnHome(event.getDeviceId());
            }
            return -1;
        }
    }

    private boolean interceptBugreportGestureTv(int keyCode, boolean down) {
        if (keyCode == 23) {
            this.mBugreportTvKey1Pressed = down;
        } else if (keyCode == 4) {
            this.mBugreportTvKey2Pressed = down;
        }
        if (!this.mBugreportTvKey1Pressed || !this.mBugreportTvKey2Pressed) {
            if (this.mBugreportTvScheduled) {
                this.mHandler.removeMessages(21);
                this.mBugreportTvScheduled = false;
            }
        } else if (!this.mBugreportTvScheduled) {
            this.mBugreportTvScheduled = true;
            Message msg = Message.obtain(this.mHandler, 21);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, 1000);
        }
        return this.mBugreportTvScheduled;
    }

    private boolean interceptAccessibilityGestureTv(int keyCode, boolean down) {
        if (keyCode == 4) {
            this.mAccessibilityTvKey1Pressed = down;
        } else if (keyCode == 20) {
            this.mAccessibilityTvKey2Pressed = down;
        }
        if (!this.mAccessibilityTvKey1Pressed || !this.mAccessibilityTvKey2Pressed) {
            if (this.mAccessibilityTvScheduled) {
                this.mHandler.removeMessages(22);
                this.mAccessibilityTvScheduled = false;
            }
        } else if (!this.mAccessibilityTvScheduled) {
            this.mAccessibilityTvScheduled = true;
            Message msg = Message.obtain(this.mHandler, 22);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(msg, getAccessibilityShortcutTimeout());
        }
        return this.mAccessibilityTvScheduled;
    }

    /* access modifiers changed from: private */
    public void requestFullBugreport() {
        if ("1".equals(SystemProperties.get("ro.debuggable")) || Settings.Global.getInt(this.mContext.getContentResolver(), "development_settings_enabled", 0) == 1) {
            try {
                ActivityManager.getService().requestBugReport(0);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error taking bugreport", e);
            }
        }
    }

    public KeyEvent dispatchUnhandledKey(WindowManagerPolicy.WindowState win, KeyEvent event, int policyFlags) {
        KeyCharacterMap.FallbackAction fallbackAction;
        KeyEvent fallbackEvent = null;
        if ((event.getFlags() & 1024) == 0) {
            KeyCharacterMap kcm = event.getKeyCharacterMap();
            int keyCode = event.getKeyCode();
            int metaState = event.getMetaState();
            boolean initialDown = event.getAction() == 0 && event.getRepeatCount() == 0;
            if (initialDown) {
                fallbackAction = kcm.getFallbackAction(keyCode, metaState);
            } else {
                fallbackAction = this.mFallbackActions.get(keyCode);
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
                return fallbackEvent;
            }
        }
        WindowManagerPolicy.WindowState windowState = win;
        int i = policyFlags;
        return fallbackEvent;
    }

    private boolean interceptFallback(WindowManagerPolicy.WindowState win, KeyEvent fallbackEvent, int policyFlags) {
        if ((interceptKeyBeforeQueueing(fallbackEvent, policyFlags) & 1) == 0 || interceptKeyBeforeDispatching(win, fallbackEvent, policyFlags) != 0) {
            return false;
        }
        return true;
    }

    public void registerShortcutKey(long shortcutCode, IShortcutService shortcutService) throws RemoteException {
        synchronized (this.mLock) {
            IShortcutService service = this.mShortcutKeyServices.get(shortcutCode);
            if (service != null) {
                if (service.asBinder().pingBinder()) {
                    throw new RemoteException("Key already exists.");
                }
            }
            this.mShortcutKeyServices.put(shortcutCode, shortcutService);
        }
    }

    public void onKeyguardOccludedChangedLw(boolean occluded) {
        if (this.mKeyguardDelegate == null || !this.mKeyguardDelegate.isShowing()) {
            if (!occluded && this.mKeyguardOccludedChanged && this.mPendingKeyguardOccluded) {
                this.mKeyguardOccludedChanged = false;
                Slog.d(TAG, "Change mKeyguardOccludedChanged false");
            }
            setKeyguardOccludedLw(occluded, false);
            return;
        }
        this.mPendingKeyguardOccluded = occluded;
        this.mKeyguardOccludedChanged = true;
        if (!occluded) {
            Slog.d(TAG, "onKeyguardOccludedChangedLw do force update.");
            setKeyguardOccludedLw(occluded, true);
        }
    }

    /* access modifiers changed from: private */
    public int handleStartTransitionForKeyguardLw(int transit, long duration) {
        if (this.mKeyguardOccludedChanged) {
            this.mKeyguardOccludedChanged = false;
            if (setKeyguardOccludedLw(this.mPendingKeyguardOccluded, false)) {
                return 5;
            }
        }
        if (AppTransition.isKeyguardGoingAwayTransit(transit)) {
            startKeyguardExitAnimation(SystemClock.uptimeMillis(), duration);
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public void launchAssistLongPressAction() {
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

    /* access modifiers changed from: protected */
    public void launchAssistAction() {
        launchAssistAction(null, Integer.MIN_VALUE);
    }

    private void launchAssistAction(String hint) {
        launchAssistAction(hint, Integer.MIN_VALUE);
    }

    /* access modifiers changed from: protected */
    public void launchAssistAction(String hint, int deviceId) {
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
            return;
        }
        Slog.i(TAG, "Ignoring HOME or Not starting activity because user setup is in progress: " + intent);
    }

    private SearchManager getSearchManager() {
        if (this.mSearchManager == null) {
            this.mSearchManager = (SearchManager) this.mContext.getSystemService("search");
        }
        return this.mSearchManager;
    }

    /* access modifiers changed from: protected */
    public void preloadRecentApps() {
        this.mPreloadedRecentApps = true;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.preloadRecentApps();
        }
    }

    /* access modifiers changed from: protected */
    public void cancelPreloadRecentApps() {
        if (this.mPreloadedRecentApps) {
            this.mPreloadedRecentApps = false;
            StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
            if (statusbar != null) {
                statusbar.cancelPreloadRecentApps();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void toggleRecentApps() {
        this.mPreloadedRecentApps = false;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.toggleRecentApps();
        }
    }

    public void showRecentApps() {
        this.mHandler.removeMessages(9);
        this.mHandler.obtainMessage(9).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void showRecentApps(boolean triggeredFromAltTab) {
        this.mPreloadedRecentApps = false;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showRecentApps(triggeredFromAltTab);
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

    /* access modifiers changed from: package-private */
    public void launchHomeFromHotKey() {
        launchHomeFromHotKey(true, true);
    }

    private void noticeHomePressed() {
        Intent intent = new Intent("com.huawei.action.HOME_PRESSED");
        intent.addFlags(1342177280);
        intent.putExtra("extra_type", "delay_as_locked");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    /* access modifiers changed from: package-private */
    public void launchHomeFromHotKey(final boolean awakenFromDreams, boolean respectKeyguard) {
        Handler handler = this.mHandler;
        WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
        Objects.requireNonNull(windowManagerFuncs);
        handler.post(new Runnable() {
            public final void run() {
                WindowManagerPolicy.WindowManagerFuncs.this.triggerAnimationFailsafe();
            }
        });
        if (respectKeyguard) {
            if (isKeyguardShowingAndNotOccluded()) {
                Log.w(TAG, "Ignoring HOME; keyguard is showing");
                return;
            } else if (this.mKeyguardOccluded && this.mKeyguardDelegate.isShowing()) {
                noticeHomePressed();
                this.mKeyguardDelegate.dismiss(new KeyguardDismissCallback() {
                    public void onDismissSucceeded() throws RemoteException {
                        PhoneWindowManager.this.mHandler.post(new Runnable(awakenFromDreams) {
                            private final /* synthetic */ boolean f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                PhoneWindowManager.this.startDockOrHome(true, this.f$1);
                            }
                        });
                    }
                }, null);
                return;
            } else if (!this.mKeyguardOccluded && this.mKeyguardDelegate.isInputRestricted()) {
                Slog.w(TAG, "Ignoring HOME ? verify unlock before launching");
                this.mKeyguardDelegate.verifyUnlock(new WindowManagerPolicy.OnKeyguardExitResult() {
                    public void onKeyguardExitResult(boolean success) {
                        Slog.w(PhoneWindowManager.TAG, "Ignoring HOME ? onKeyguardExitResult is ? " + success);
                        if (success) {
                            long origId = Binder.clearCallingIdentity();
                            try {
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
        if (this.mRecentsVisible) {
            try {
                ActivityManager.getService().stopAppSwitches();
            } catch (RemoteException e) {
            }
            if (awakenFromDreams) {
                awakenDreams();
            }
            if (HWFLOW) {
                Log.i(TAG, "hideRecentApps from home");
            }
            hideRecentApps(false, true);
        } else {
            startDockOrHome(true, awakenFromDreams);
        }
    }

    public void setRecentsVisibilityLw(boolean visible) {
        this.mRecentsVisible = visible;
    }

    public void setPipVisibilityLw(boolean visible) {
        this.mPictureInPictureVisible = visible;
    }

    public void setNavBarVirtualKeyHapticFeedbackEnabledLw(boolean enabled) {
        this.mNavBarVirtualKeyHapticFeedbackEnabled = enabled;
    }

    public int adjustSystemUiVisibilityLw(int visibility) {
        this.mStatusBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mNavigationBarController.adjustSystemUiVisibilityLw(this.mLastSystemUiFlags, visibility);
        this.mResettingSystemUiFlags &= visibility;
        return (~this.mResettingSystemUiFlags) & visibility & (~this.mForceClearedSystemUiFlags);
    }

    public boolean getLayoutHintLw(WindowManager.LayoutParams attrs, Rect taskBounds, DisplayFrames displayFrames, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper outDisplayCutout) {
        int availRight;
        int availRight2;
        WindowManager.LayoutParams layoutParams = attrs;
        Rect rect = taskBounds;
        DisplayFrames displayFrames2 = displayFrames;
        Rect rect2 = outFrame;
        Rect rect3 = outContentInsets;
        Rect rect4 = outStableInsets;
        Rect rect5 = outOutsets;
        DisplayCutout.ParcelableWrapper parcelableWrapper = outDisplayCutout;
        int fl = PolicyControl.getWindowFlags(null, layoutParams);
        int pfl = layoutParams.privateFlags;
        int requestedSysUiVis = PolicyControl.getSystemUiVisibility(null, layoutParams);
        int sysUiVis = getImpliedSysUiFlagsForLayout(attrs) | requestedSysUiVis;
        int displayRotation = displayFrames2.mRotation;
        int displayWidth = displayFrames2.mDisplayWidth;
        int displayHeight = displayFrames2.mDisplayHeight;
        int i = requestedSysUiVis;
        if (rect5 != null && shouldUseOutsets(layoutParams, fl)) {
            int outset = ScreenShapeHelper.getWindowOutsetBottomPx(this.mContext.getResources());
            if (outset > 0) {
                if (displayRotation == 0) {
                    rect5.bottom += outset;
                } else if (displayRotation == 1) {
                    rect5.right += outset;
                } else if (displayRotation == 2) {
                    rect5.top += outset;
                } else if (displayRotation == 3) {
                    rect5.left += outset;
                }
            }
        }
        boolean layoutInScreen = (fl & 256) != 0;
        boolean layoutInScreenAndInsetDecor = layoutInScreen && (65536 & fl) != 0;
        boolean screenDecor = (pfl & DumpState.DUMP_CHANGES) != 0;
        if (!layoutInScreenAndInsetDecor || screenDecor) {
            boolean z = layoutInScreenAndInsetDecor;
            int i2 = pfl;
            int i3 = displayRotation;
            if (layoutInScreen) {
                rect2.set(displayFrames2.mUnrestricted);
            } else {
                rect2.set(displayFrames2.mStable);
            }
            if (rect != null) {
                rect2.intersect(rect);
            }
            outContentInsets.setEmpty();
            outStableInsets.setEmpty();
            parcelableWrapper.set(DisplayCutout.NO_CUTOUT);
            return this.mForceShowSystemBars;
        }
        if (!canHideNavigationBar() || (sysUiVis & 512) == 0) {
            rect2.set(displayFrames2.mRestricted);
            int availRight3 = displayFrames2.mRestricted.right;
            availRight2 = displayFrames2.mRestricted.bottom;
            availRight = availRight3;
        } else {
            rect2.set(displayFrames2.mUnrestricted);
            availRight = displayFrames2.mUnrestricted.right;
            availRight2 = displayFrames2.mUnrestricted.bottom;
        }
        boolean z2 = layoutInScreenAndInsetDecor;
        int i4 = pfl;
        int i5 = displayRotation;
        boolean z3 = layoutInScreen;
        rect4.set(displayFrames2.mStable.left, displayFrames2.mStable.top, availRight - displayFrames2.mStable.right, availRight2 - displayFrames2.mStable.bottom);
        if ((sysUiVis & 256) != 0) {
            if ((fl & 1024) != 0) {
                rect3.set(displayFrames2.mStableFullscreen.left, displayFrames2.mStableFullscreen.top, availRight - displayFrames2.mStableFullscreen.right, availRight2 - displayFrames2.mStableFullscreen.bottom);
            } else {
                outContentInsets.set(outStableInsets);
            }
        } else if ((fl & 1024) == 0 && (33554432 & fl) == 0) {
            rect3.set(displayFrames2.mCurrent.left, displayFrames2.mCurrent.top, availRight - displayFrames2.mCurrent.right, availRight2 - displayFrames2.mCurrent.bottom);
        } else {
            outContentInsets.setEmpty();
        }
        if (rect != null) {
            calculateRelevantTaskInsets(rect, rect3, displayWidth, displayHeight);
            calculateRelevantTaskInsets(rect, rect4, displayWidth, displayHeight);
            rect2.intersect(rect);
        }
        parcelableWrapper.set(displayFrames2.mDisplayCutout.calculateRelativeTo(rect2).getDisplayCutout());
        return this.mForceShowSystemBars;
    }

    private void calculateRelevantTaskInsets(Rect taskBounds, Rect inOutInsets, int displayWidth, int displayHeight) {
        mTmpRect.set(0, 0, displayWidth, displayHeight);
        mTmpRect.inset(inOutInsets);
        mTmpRect.intersect(taskBounds);
        inOutInsets.set(mTmpRect.left - taskBounds.left, mTmpRect.top - taskBounds.top, taskBounds.right - mTmpRect.right, taskBounds.bottom - mTmpRect.bottom);
    }

    private boolean shouldUseOutsets(WindowManager.LayoutParams attrs, int fl) {
        return attrs.type == 2013 || (33555456 & fl) != 0;
    }

    public void beginLayoutLw(DisplayFrames displayFrames, int uiMode) {
        DisplayFrames displayFrames2;
        Rect pf;
        Rect df;
        Rect dcf;
        int type;
        boolean navVisible;
        DisplayFrames displayFrames3 = displayFrames;
        displayFrames.onBeginLayout();
        this.mDisplayRotation = displayFrames3.mRotation;
        this.mSystemGestures.screenWidth = displayFrames3.mUnrestricted.width();
        SystemGesturesPointerEventListener systemGesturesPointerEventListener = this.mSystemGestures;
        int height = displayFrames3.mUnrestricted.height();
        systemGesturesPointerEventListener.screenHeight = height;
        this.mRestrictedScreenHeight = height;
        this.mDockLayer = 268435456;
        this.mStatusBarLayer = -1;
        Rect pf2 = mTmpParentFrame;
        Rect df2 = mTmpDisplayFrame;
        Rect of = mTmpOverscanFrame;
        Rect vf = mTmpVisibleFrame;
        Rect dcf2 = mTmpDecorFrame;
        vf.set(displayFrames3.mDock);
        of.set(displayFrames3.mDock);
        df2.set(displayFrames3.mDock);
        pf2.set(displayFrames3.mDock);
        dcf2.setEmpty();
        if (displayFrames3.mDisplayId == 0) {
            int sysui = this.mLastSystemUiFlags;
            boolean navVisible2 = (sysui & 2) == 0;
            boolean navTranslucent = (-2147450880 & sysui) != 0;
            boolean immersive = (sysui & 2048) != 0;
            boolean immersiveSticky = (sysui & 4096) != 0;
            WindowManager.LayoutParams focusAttrs = this.mFocusedWindow != null ? this.mFocusedWindow.getAttrs() : null;
            boolean navAllowedHidden = immersive || immersiveSticky || (focusAttrs != null && (focusAttrs.privateFlags & Integer.MIN_VALUE) != 0);
            boolean navTranslucent2 = navTranslucent & (!immersiveSticky);
            boolean isKeyguardShowing = isStatusBarKeyguard() && !this.mKeyguardOccluded;
            if (!isKeyguardShowing) {
                navTranslucent2 &= areTranslucentBarsAllowed();
            }
            boolean navTranslucent3 = navTranslucent2;
            boolean statusBarExpandedNotKeyguard = !isKeyguardShowing && this.mStatusBar != null && !HwPolicyFactory.isHwGlobalActionsShowing() && this.mStatusBar.getAttrs().height == -1 && this.mStatusBar.getAttrs().width == -1 && !this.mHwPWMEx.getFPAuthState();
            if (navVisible2 || navAllowedHidden) {
                if (this.mInputConsumer != null) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(19, this.mInputConsumer));
                }
            } else if (this.mInputConsumer == null && this.mStatusBar != null && canHideNavigationBar()) {
                this.mInputConsumer = this.mWindowManagerFuncs.createInputConsumer(this.mHandler.getLooper(), "nav_input_consumer", new InputEventReceiver.Factory() {
                    public final InputEventReceiver createInputEventReceiver(InputChannel inputChannel, Looper looper) {
                        return PhoneWindowManager.lambda$beginLayoutLw$2(PhoneWindowManager.this, inputChannel, looper);
                    }
                });
                InputManager.getInstance().setPointerIconType(0);
            }
            boolean navVisible3 = (!canHideNavigationBar()) | navVisible2;
            if (navVisible3 && mUsingHwNavibar) {
                navVisible3 = navVisible3 && !computeNaviBarFlag();
            }
            String windowName = null;
            if (this.mFocusedWindow != null) {
                windowName = this.mFocusedWindow.toString();
            }
            String windowName2 = windowName;
            int type2 = focusAttrs != null ? focusAttrs.type : 0;
            int flag = focusAttrs != null ? focusAttrs.privateFlags : 0;
            int i = flag;
            boolean isKeyguardOn = (type2 == 2000 && (flag & 1024) != 0) || type2 == 2101 || type2 == 2100;
            if (windowName2 == null || !windowName2.contains("com.google.android.gms/com.google.android.gms.auth.login.ShowErrorActivity")) {
                navVisible = navVisible3;
                type = type2;
            } else {
                navVisible = navVisible3;
                type = type2;
                if (Settings.Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0) {
                    navVisible = true;
                }
            }
            int i2 = type;
            String str = windowName2;
            Object obj = "com.google.android.gms/com.google.android.gms.auth.login.ShowErrorActivity";
            WindowManager.LayoutParams layoutParams = focusAttrs;
            int sysui2 = sysui;
            dcf = dcf2;
            df = df2;
            pf = pf2;
            displayFrames2 = displayFrames3;
            if (layoutNavigationBar(displayFrames3, uiMode, dcf2, navVisible, navTranslucent3, navAllowedHidden, isKeyguardOn, statusBarExpandedNotKeyguard) || layoutStatusBar(displayFrames3, pf2, df2, of, vf, dcf, sysui2, isKeyguardShowing)) {
                updateSystemUiVisibilityLw();
            }
        } else {
            dcf = dcf2;
            Rect rect = vf;
            Rect rect2 = of;
            df = df2;
            pf = pf2;
            displayFrames2 = displayFrames3;
        }
        layoutScreenDecorWindows(displayFrames2, pf, df, dcf);
        if (displayFrames2.mDisplayCutoutSafe.top > displayFrames2.mUnrestricted.top) {
            displayFrames2.mDisplayCutoutSafe.top = Math.max(displayFrames2.mDisplayCutoutSafe.top, displayFrames2.mStable.top);
        }
    }

    public static /* synthetic */ InputEventReceiver lambda$beginLayoutLw$2(PhoneWindowManager phoneWindowManager, InputChannel channel, Looper looper) {
        return new HideNavInputEventReceiver(channel, looper);
    }

    private void layoutScreenDecorWindows(DisplayFrames displayFrames, Rect pf, Rect df, Rect dcf) {
        DisplayFrames displayFrames2 = displayFrames;
        if (!this.mScreenDecorWindows.isEmpty()) {
            int displayId = displayFrames2.mDisplayId;
            Rect dockFrame = displayFrames2.mDock;
            int displayHeight = displayFrames2.mDisplayHeight;
            int displayWidth = displayFrames2.mDisplayWidth;
            for (int i = this.mScreenDecorWindows.size() - 1; i >= 0; i--) {
                WindowManagerPolicy.WindowState w = this.mScreenDecorWindows.valueAt(i);
                if (w.getDisplayId() == displayId && w.isVisibleLw()) {
                    w.computeFrameLw(pf, df, df, df, df, dcf, df, df, displayFrames2.mDisplayCutout, false);
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
            }
            displayFrames2.mRestricted.set(dockFrame);
            displayFrames2.mCurrent.set(dockFrame);
            displayFrames2.mVoiceContent.set(dockFrame);
            displayFrames2.mSystem.set(dockFrame);
            displayFrames2.mContent.set(dockFrame);
            displayFrames2.mRestrictedOverscan.set(dockFrame);
        }
    }

    private boolean layoutStatusBar(DisplayFrames displayFrames, Rect pf, Rect df, Rect of, Rect vf, Rect dcf, int sysui, boolean isKeyguardShowing) {
        DisplayFrames displayFrames2 = displayFrames;
        Rect rect = pf;
        Rect rect2 = df;
        Rect rect3 = of;
        Rect rect4 = vf;
        boolean z = false;
        if (this.mStatusBar == null) {
            return false;
        }
        rect3.set(displayFrames2.mUnrestricted);
        rect2.set(displayFrames2.mUnrestricted);
        rect.set(displayFrames2.mUnrestricted);
        rect4.set(displayFrames2.mStable);
        if (CoordinationModeUtils.isFoldable() && !isLandscape()) {
            CoordinationModeUtils utils = CoordinationModeUtils.getInstance(this.mContext);
            if (utils.getCoordinationCreateMode() == 3) {
                int foldScreenSubWidth = CoordinationModeUtils.getFoldScreenSubWidth();
                rect4.right = foldScreenSubWidth;
                rect.right = foldScreenSubWidth;
                rect2.right = foldScreenSubWidth;
                rect3.right = foldScreenSubWidth;
            } else if (utils.getCoordinationCreateMode() == 4) {
                int foldScreenEdgeWidth = CoordinationModeUtils.getFoldScreenEdgeWidth() + CoordinationModeUtils.getFoldScreenSubWidth();
                rect4.left = foldScreenEdgeWidth;
                rect.left = foldScreenEdgeWidth;
                rect2.left = foldScreenEdgeWidth;
                rect3.left = foldScreenEdgeWidth;
            }
        }
        this.mStatusBarLayer = this.mStatusBar.getSurfaceLayer();
        this.mStatusBar.computeFrameLw(rect, rect2, rect4, rect4, rect4, dcf, rect4, rect4, displayFrames2.mDisplayCutout, false);
        displayFrames2.mStable.top = displayFrames2.mUnrestricted.top + this.mStatusBarHeightForRotation[displayFrames2.mRotation];
        displayFrames2.mStable.top = Math.max(displayFrames2.mStable.top, displayFrames2.mDisplayCutoutSafe.top);
        mTmpRect.set(this.mStatusBar.getContentFrameLw());
        mTmpRect.intersect(displayFrames2.mDisplayCutoutSafe);
        mTmpRect.top = this.mStatusBar.getContentFrameLw().top;
        mTmpRect.bottom = displayFrames2.mStable.top;
        this.mStatusBarController.setContentFrame(mTmpRect);
        boolean statusBarTransient = (sysui & 67108864) != 0;
        if ((sysui & 1073741832) != 0) {
            z = true;
        }
        boolean statusBarTranslucent = z;
        if (!isKeyguardShowing) {
            statusBarTranslucent &= areTranslucentBarsAllowed();
        }
        if (this.mStatusBar.isVisibleLw() && !statusBarTransient) {
            Rect dockFrame = displayFrames2.mDock;
            dockFrame.top = displayFrames2.mStable.top;
            displayFrames2.mContent.set(dockFrame);
            displayFrames2.mVoiceContent.set(dockFrame);
            displayFrames2.mCurrent.set(dockFrame);
            if (!this.mStatusBar.isAnimatingLw() && !statusBarTranslucent && !this.mStatusBarController.wasRecentlyTranslucent()) {
                displayFrames2.mSystem.top = displayFrames2.mStable.top;
            }
        }
        return this.mStatusBarController.checkHiddenLw();
    }

    /* JADX WARNING: Removed duplicated region for block: B:84:0x029a  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x02a4  */
    private boolean layoutNavigationBar(DisplayFrames displayFrames, int uiMode, Rect dcf, boolean navVisible, boolean navTranslucent, boolean navAllowedHidden, boolean isKeyguardOn, boolean statusBarExpandedNotKeyguard) {
        Rect cutoutSafeUnrestricted;
        int displayWidth;
        int top;
        int bottom;
        int top2;
        int bottom2;
        int displayWidth2;
        DisplayFrames displayFrames2 = displayFrames;
        int i = uiMode;
        boolean z = statusBarExpandedNotKeyguard;
        if (this.mNavigationBar == null) {
            return false;
        }
        boolean transientNavBarShowing = this.mNavigationBarController.isTransientShowing();
        int rotation = displayFrames2.mRotation;
        int displayHeight = displayFrames2.mDisplayHeight;
        int displayWidth3 = displayFrames2.mDisplayWidth;
        Rect dockFrame = displayFrames2.mDock;
        this.mNavigationBarPosition = navigationBarPosition(displayWidth3, displayHeight, rotation);
        Rect cutoutSafeUnrestricted2 = mTmpRect;
        cutoutSafeUnrestricted2.set(displayFrames2.mUnrestricted);
        if (this.mHwPWMEx.isIntersectCutoutForNotch(displayFrames2, this.mIsNotchSwitchOpen)) {
            cutoutSafeUnrestricted2.intersectUnchecked(displayFrames2.mDisplayCutoutSafe);
        }
        if (this.mNavigationBarPosition == 4) {
            int top3 = cutoutSafeUnrestricted2.bottom - getNavigationBarHeight(rotation, i);
            int left = 0;
            int right = displayWidth3;
            if (CoordinationModeUtils.isFoldable()) {
                CoordinationModeUtils utils = CoordinationModeUtils.getInstance(this.mContext);
                displayWidth2 = displayWidth3;
                if (utils.getCoordinationCreateMode() == 3) {
                    left = 0;
                    right = CoordinationModeUtils.getFoldScreenSubWidth();
                } else if (utils.getCoordinationCreateMode() == 4) {
                    left = CoordinationModeUtils.getFoldScreenEdgeWidth() + CoordinationModeUtils.getFoldScreenSubWidth();
                    right = left + CoordinationModeUtils.getFoldScreenMainWidth();
                }
            } else {
                displayWidth2 = displayWidth3;
            }
            Rect cutoutSafeUnrestricted3 = cutoutSafeUnrestricted2;
            mTmpNavigationFrame.set(left, top3, right, (displayFrames2.mUnrestricted.bottom + getNaviBarHeightForRotationMax(rotation)) - this.mNavigationBarHeightForRotationDefault[rotation]);
            if (isNaviBarMini()) {
                Rect rect = displayFrames2.mStable;
                Rect rect2 = displayFrames2.mStableFullscreen;
                int naviBarHeightForRotationMin = displayHeight - getNaviBarHeightForRotationMin(rotation);
                rect2.bottom = naviBarHeightForRotationMin;
                rect.bottom = naviBarHeightForRotationMin;
            } else {
                Rect rect3 = displayFrames2.mStable;
                displayFrames2.mStableFullscreen.bottom = top3;
                rect3.bottom = top3;
            }
            if (transientNavBarShowing) {
                this.mLastNaviStatus = 2;
                this.mLastTransientNaviDockBottom = dockFrame.bottom;
                this.mNavigationBarController.setBarShowingLw(true);
            } else if (navVisible) {
                if (this.mNavigationBarController.isTransientHiding()) {
                    Slog.v(TAG, "navigationbar is visible, but transientBarState is hiding, so reset a portrait screen");
                    this.mNavigationBarController.sethwTransientBarState(0);
                }
                this.mNavigationBarController.setBarShowingLw(true);
                Rect rect4 = displayFrames2.mRestricted;
                Rect rect5 = displayFrames2.mRestrictedOverscan;
                int i2 = displayFrames2.mStable.bottom;
                rect5.bottom = i2;
                rect4.bottom = i2;
                dockFrame.bottom = i2;
                this.mRestrictedScreenHeight = dockFrame.bottom - displayFrames2.mRestricted.top;
                this.mLastNaviStatus = 0;
                this.mLastShowNaviDockBottom = dockFrame.bottom;
            } else {
                this.mNavigationBarController.setBarShowingLw(z);
                if (isKeyguardOn) {
                    switch (this.mLastNaviStatus) {
                        case 0:
                            if (this.mLastShowNaviDockBottom != 0) {
                                dockFrame.bottom = this.mLastShowNaviDockBottom;
                                this.mRestrictedScreenHeight = dockFrame.bottom - displayFrames2.mRestrictedOverscan.top;
                                displayFrames2.mRestricted.bottom = dockFrame.bottom;
                                displayFrames2.mRestrictedOverscan.bottom = dockFrame.bottom;
                                break;
                            }
                            break;
                        case 1:
                            if (this.mLastHideNaviDockBottom != 0) {
                                dockFrame.bottom = this.mLastHideNaviDockBottom;
                                this.mRestrictedScreenHeight = dockFrame.bottom - displayFrames2.mRestrictedOverscan.top;
                                displayFrames2.mRestricted.bottom = dockFrame.bottom;
                                displayFrames2.mRestrictedOverscan.bottom = dockFrame.bottom;
                                break;
                            }
                            break;
                        case 2:
                            if (this.mLastTransientNaviDockBottom != 0) {
                                dockFrame.bottom = this.mLastTransientNaviDockBottom;
                                this.mRestrictedScreenHeight = dockFrame.bottom - displayFrames2.mRestrictedOverscan.top;
                                displayFrames2.mRestricted.bottom = dockFrame.bottom;
                                displayFrames2.mRestrictedOverscan.bottom = dockFrame.bottom;
                                break;
                            }
                            break;
                        default:
                            Slog.v(TAG, "keyguard mLastNaviStatus is init");
                            break;
                    }
                } else {
                    this.mLastNaviStatus = 1;
                    this.mLastHideNaviDockBottom = dockFrame.bottom;
                }
            }
            if (navVisible && !navTranslucent && !navAllowedHidden && !this.mNavigationBar.isAnimatingLw() && !this.mNavigationBarController.wasRecentlyTranslucent() && !mUsingHwNavibar) {
                displayFrames2.mSystem.bottom = displayFrames2.mStable.bottom;
            }
            int i3 = displayWidth2;
            Rect rect6 = cutoutSafeUnrestricted3;
        } else {
            int displayWidth4 = displayWidth3;
            Rect cutoutSafeUnrestricted4 = cutoutSafeUnrestricted2;
            if (this.mNavigationBarPosition == 2) {
                boolean isShowLeftNavBar = getNavibarAlignLeftWhenLand();
                Rect cutoutSafeUnrestricted5 = cutoutSafeUnrestricted4;
                int left2 = cutoutSafeUnrestricted5.right - getNavigationBarWidth(rotation, uiMode);
                if (!isShowLeftNavBar) {
                    int bottom3 = displayHeight;
                    if (CoordinationModeUtils.isFoldable()) {
                        CoordinationModeUtils utils2 = CoordinationModeUtils.getInstance(this.mContext);
                        top2 = 0;
                        if (utils2.getCoordinationCreateMode() == 3) {
                            top = CoordinationModeUtils.getFoldScreenEdgeWidth() + CoordinationModeUtils.getFoldScreenMainWidth();
                            bottom2 = top + CoordinationModeUtils.getFoldScreenSubWidth();
                        } else if (utils2.getCoordinationCreateMode() == 4) {
                            top = 0;
                            bottom2 = 0 + CoordinationModeUtils.getFoldScreenMainWidth();
                        }
                        bottom = bottom2;
                        cutoutSafeUnrestricted = cutoutSafeUnrestricted5;
                        mTmpNavigationFrame.set(left2, top, (displayFrames2.mUnrestricted.right + getNaviBarWidthForRotationMax(rotation)) - this.mNavigationBarWidthForRotationDefault[rotation], bottom);
                    } else {
                        top2 = 0;
                    }
                    bottom = bottom3;
                    top = top2;
                    cutoutSafeUnrestricted = cutoutSafeUnrestricted5;
                    mTmpNavigationFrame.set(left2, top, (displayFrames2.mUnrestricted.right + getNaviBarWidthForRotationMax(rotation)) - this.mNavigationBarWidthForRotationDefault[rotation], bottom);
                } else {
                    cutoutSafeUnrestricted = cutoutSafeUnrestricted5;
                    mTmpNavigationFrame.set(0, 0, this.mContext.getResources().getDimensionPixelSize(34472115), displayHeight);
                }
                if (isNaviBarMini() != 0) {
                    Rect rect7 = displayFrames2.mStable;
                    Rect rect8 = displayFrames2.mStableFullscreen;
                    int naviBarWidthForRotationMin = displayWidth4 - getNaviBarWidthForRotationMin(rotation);
                    rect8.right = naviBarWidthForRotationMin;
                    rect7.right = naviBarWidthForRotationMin;
                } else if (!isShowLeftNavBar) {
                    Rect rect9 = displayFrames2.mStable;
                    displayFrames2.mStableFullscreen.right = left2;
                    rect9.right = left2;
                } else {
                    Rect rect10 = displayFrames2.mStable;
                    Rect rect11 = displayFrames2.mStableFullscreen;
                    int i4 = mTmpNavigationFrame.right;
                    rect11.left = i4;
                    rect10.left = i4;
                    displayWidth = displayWidth4;
                    displayFrames2.mStable.right = displayWidth;
                    if (!navVisible && this.mLastSystemUiFlags != this.mLastSystemUiFlagsTmp) {
                        this.mLastSystemUiFlagsTmp = this.mLastSystemUiFlags;
                        Flog.i(303, "transientNavBarShowing:" + transientNavBarShowing + ",statusBarExpandedNotKeyguard:" + z + ",mLastSystemUiFlags:" + Integer.toHexString(this.mLastSystemUiFlags));
                    }
                    if (!transientNavBarShowing) {
                        this.mNavigationBarController.setBarShowingLw(true);
                        this.mLastNaviStatus = 2;
                    } else if (navVisible) {
                        if (this.mNavigationBarController.isTransientHiding()) {
                            Slog.v(TAG, "navigationbar is visible, but transientBarState is hiding, so reset a landscape screen");
                            this.mNavigationBarController.sethwTransientBarState(0);
                        }
                        this.mNavigationBarController.setBarShowingLw(true);
                        dockFrame.right = displayFrames2.mStable.right;
                        if (!isShowLeftNavBar) {
                            displayFrames2.mRestricted.right = (displayFrames2.mRestricted.left + dockFrame.right) - displayFrames2.mRestricted.left;
                            displayFrames2.mRestrictedOverscan.right = dockFrame.right - displayFrames2.mRestrictedOverscan.left;
                        } else {
                            Rect rect12 = displayFrames2.mRestricted;
                            int i5 = displayFrames2.mStable.left;
                            dockFrame.left = i5;
                            rect12.left = i5;
                            displayFrames2.mRestricted.right = displayFrames2.mRestricted.left + displayFrames2.mStable.right;
                            displayFrames2.mRestricted.right = displayFrames2.mRestricted.left + displayFrames2.mStable.right;
                        }
                        this.mLastNaviStatus = 0;
                    } else {
                        this.mNavigationBarController.setBarShowingLw(z);
                        this.mLastNaviStatus = 1;
                    }
                    if (navVisible && !navTranslucent && !navAllowedHidden && !this.mNavigationBar.isAnimatingLw() && !this.mNavigationBarController.wasRecentlyTranslucent() && !mUsingHwNavibar) {
                        displayFrames2.mSystem.right = displayFrames2.mStable.right;
                    }
                    int i6 = displayWidth;
                    Rect rect13 = cutoutSafeUnrestricted;
                }
                displayWidth = displayWidth4;
                this.mLastSystemUiFlagsTmp = this.mLastSystemUiFlags;
                Flog.i(303, "transientNavBarShowing:" + transientNavBarShowing + ",statusBarExpandedNotKeyguard:" + z + ",mLastSystemUiFlags:" + Integer.toHexString(this.mLastSystemUiFlags));
                if (!transientNavBarShowing) {
                }
                displayFrames2.mSystem.right = displayFrames2.mStable.right;
                int i62 = displayWidth;
                Rect rect132 = cutoutSafeUnrestricted;
            } else {
                int displayWidth5 = displayWidth4;
                Rect cutoutSafeUnrestricted6 = cutoutSafeUnrestricted4;
                if (this.mNavigationBarPosition == 1) {
                    int right2 = cutoutSafeUnrestricted6.left + getNavigationBarWidth(rotation, uiMode);
                    int i7 = displayWidth5;
                    mTmpNavigationFrame.set(displayFrames2.mUnrestricted.left, 0, right2, displayHeight);
                    Rect rect14 = displayFrames2.mStable;
                    displayFrames2.mStableFullscreen.left = right2;
                    rect14.left = right2;
                    if (transientNavBarShowing) {
                        this.mNavigationBarController.setBarShowingLw(true);
                    } else if (navVisible) {
                        this.mNavigationBarController.setBarShowingLw(true);
                        Rect rect15 = displayFrames2.mRestricted;
                        displayFrames2.mRestrictedOverscan.left = right2;
                        rect15.left = right2;
                        dockFrame.left = right2;
                    } else {
                        this.mNavigationBarController.setBarShowingLw(z);
                    }
                    if (navVisible && !navTranslucent && !navAllowedHidden && !this.mNavigationBar.isAnimatingLw() && !this.mNavigationBarController.wasRecentlyTranslucent()) {
                        displayFrames2.mSystem.left = right2;
                    }
                } else {
                    Rect rect16 = cutoutSafeUnrestricted6;
                    int i8 = uiMode;
                }
                displayFrames2.mCurrent.set(dockFrame);
                displayFrames2.mVoiceContent.set(dockFrame);
                displayFrames2.mContent.set(dockFrame);
                this.mStatusBarLayer = this.mNavigationBar.getSurfaceLayer();
                this.mNavigationBar.computeFrameLw(mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, displayFrames2.mDisplayCutoutSafe, mTmpNavigationFrame, dcf, mTmpNavigationFrame, displayFrames2.mDisplayCutoutSafe, displayFrames2.mDisplayCutout, false);
                this.mNavigationBarController.setContentFrame(this.mNavigationBar.getContentFrameLw());
                return this.mNavigationBarController.checkHiddenLw();
            }
        }
        int i9 = uiMode;
        displayFrames2.mCurrent.set(dockFrame);
        displayFrames2.mVoiceContent.set(dockFrame);
        displayFrames2.mContent.set(dockFrame);
        this.mStatusBarLayer = this.mNavigationBar.getSurfaceLayer();
        this.mNavigationBar.computeFrameLw(mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, displayFrames2.mDisplayCutoutSafe, mTmpNavigationFrame, dcf, mTmpNavigationFrame, displayFrames2.mDisplayCutoutSafe, displayFrames2.mDisplayCutout, false);
        this.mNavigationBarController.setContentFrame(this.mNavigationBar.getContentFrameLw());
        return this.mNavigationBarController.checkHiddenLw();
    }

    private int navigationBarPosition(int displayWidth, int displayHeight, int displayRotation) {
        if (!this.mNavigationBarCanMove || displayWidth <= displayHeight) {
            return 4;
        }
        return displayRotation == 3 ? 2 : 2;
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

    private void setAttachedWindowFrames(WindowManagerPolicy.WindowState win, int fl, int adjust, WindowManagerPolicy.WindowState attached, boolean insetDecors, Rect pf, Rect df, Rect of, Rect cf, Rect vf, DisplayFrames displayFrames) {
        if (win.isInputMethodTarget() || !attached.isInputMethodTarget()) {
            if (adjust != 16) {
                cf.set((1073741824 & fl) != 0 ? attached.getContentFrameLw() : attached.getOverscanFrameLw());
                if (attached.getAttrs().type == 2011 && cf.bottom > attached.getContentFrameLw().bottom) {
                    cf.bottom = attached.getContentFrameLw().bottom;
                }
            } else {
                cf.set(attached.getContentFrameLw());
                if (attached.isVoiceInteraction()) {
                    cf.intersectUnchecked(displayFrames.mVoiceContent);
                } else if (win.isInputMethodTarget() || attached.isInputMethodTarget()) {
                    cf.intersectUnchecked(displayFrames.mContent);
                }
            }
            df.set(insetDecors ? attached.getDisplayFrameLw() : cf);
            of.set(insetDecors ? attached.getOverscanFrameLw() : cf);
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

    private boolean canReceiveInput(WindowManagerPolicy.WindowState win) {
        if (!(((win.getAttrs().flags & 8) != 0) ^ ((win.getAttrs().flags & 131072) != 0))) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:260:0x05fb, code lost:
        if (r12 <= 1999) goto L_0x0606;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:262:0x05ff, code lost:
        if (r12 == 2020) goto L_0x0606;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:306:0x06ca, code lost:
        r3 = r62;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:307:0x06d1, code lost:
        if ((r3.flags & Integer.MIN_VALUE) != 0) goto L_0x06fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:309:0x06d8, code lost:
        if ((r3.flags & 67108864) != 0) goto L_0x06fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:311:0x06dc, code lost:
        if ((r6 & 3076) != 0) goto L_0x06fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:313:0x06e1, code lost:
        if (r3.type == 3) goto L_0x06fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:315:0x06e7, code lost:
        if (r3.type == 2038) goto L_0x06fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:316:0x06e9, code lost:
        r0 = r10.mUnrestricted.top + r13.mStatusBarHeightForRotation[r10.mRotation];
        r15.top = r0;
        r2 = r63;
        r2.top = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:317:0x06fb, code lost:
        r2 = r63;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:441:0x09a9, code lost:
        if (r6.type != 2013) goto L_0x09ae;
     */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x0256  */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x0262  */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x0288  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0375  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x037c  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x0382  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x0389  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x03e6  */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x03f5  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x0672  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0707  */
    /* JADX WARNING: Removed duplicated region for block: B:326:0x0716  */
    /* JADX WARNING: Removed duplicated region for block: B:329:0x0723  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0736  */
    /* JADX WARNING: Removed duplicated region for block: B:412:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:413:0x0957  */
    /* JADX WARNING: Removed duplicated region for block: B:463:0x0a7e  */
    /* JADX WARNING: Removed duplicated region for block: B:464:0x0a84  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x024c  */
    public void layoutWindowLw(WindowManagerPolicy.WindowState win, WindowManagerPolicy.WindowState attached, DisplayFrames displayFrames) {
        boolean hasNavBar;
        int sysUiFl;
        Rect dcf;
        Rect sf;
        Rect vf;
        Rect cf;
        Rect of;
        Rect df;
        Rect pf;
        int type;
        WindowManager.LayoutParams attrs;
        int type2;
        int fl;
        boolean z;
        Rect dcf2;
        DisplayFrames displayFrames2;
        Rect pf2;
        Rect df2;
        boolean parentFrameWasClippedByDisplayCutout;
        Rect of2;
        int fl2;
        int type3;
        int cutoutMode;
        int fl3;
        boolean z2;
        WindowManager.LayoutParams attrs2;
        int type4;
        int type5;
        int sysUiFl2;
        int fl4;
        DisplayFrames displayFrames3;
        int sysUiFl3;
        int fl5;
        int sysUiFl4;
        Rect cf2;
        Rect of3;
        Rect df3;
        Rect pf3;
        WindowManager.LayoutParams attrs3;
        int fl6;
        int sysUiFl5;
        int sysUiFl6;
        int fl7;
        WindowManager.LayoutParams attrs4;
        Rect cf3;
        int type6;
        DisplayFrames displayFrames4;
        Rect of4;
        Rect df4;
        Rect pf4;
        int type7;
        int fl8;
        int type8;
        int type9;
        int type10;
        WindowManager.LayoutParams attrs5;
        int sysUiFl7;
        int fl9;
        DisplayFrames displayFrames5;
        Rect of5;
        Rect df5;
        Rect pf5;
        int sysUiFl8;
        Rect cf4;
        int adjust;
        WindowManager.LayoutParams attrs6;
        Rect dcf3;
        int i;
        int i2;
        Rect rect;
        Rect dcf4;
        Rect vf2;
        Rect sf2;
        int type11;
        int fl10;
        int adjust2;
        int sysUiFl9;
        WindowManager.LayoutParams attrs7;
        DisplayFrames displayFrames6;
        Rect sf3;
        PhoneWindowManager phoneWindowManager = this;
        WindowManagerPolicy.WindowState windowState = win;
        DisplayFrames displayFrames7 = displayFrames;
        if ((windowState != phoneWindowManager.mStatusBar || canReceiveInput(win)) && windowState != phoneWindowManager.mNavigationBar && !phoneWindowManager.mScreenDecorWindows.contains(windowState)) {
            WindowManager.LayoutParams attrs8 = win.getAttrs();
            boolean isDefaultDisplay = win.isDefaultDisplay();
            if (isDefaultDisplay && windowState == phoneWindowManager.mLastInputMethodTargetWindow && phoneWindowManager.mLastInputMethodWindow != null) {
                phoneWindowManager.offsetInputMethodWindowLw(phoneWindowManager.mLastInputMethodWindow, displayFrames7);
            }
            int type12 = attrs8.type;
            int fl11 = PolicyControl.getWindowFlags(windowState, attrs8);
            int pfl = attrs8.privateFlags;
            int sim = attrs8.softInputMode;
            int requestedSysUiFl = PolicyControl.getSystemUiVisibility(null, attrs8);
            int sysUiFl10 = requestedSysUiFl | phoneWindowManager.getImpliedSysUiFlagsForLayout(attrs8);
            Rect pf6 = mTmpParentFrame;
            Rect df6 = mTmpDisplayFrame;
            Rect of6 = mTmpOverscanFrame;
            Rect cf5 = mTmpContentFrame;
            Rect vf3 = mTmpVisibleFrame;
            Rect vf4 = mTmpDecorFrame;
            int type13 = type12;
            Rect sf4 = mTmpStableFrame;
            Rect osf = null;
            vf4.setEmpty();
            boolean isNeedHideNaviBarWin = mUsingHwNavibar && (attrs8.privateFlags & Integer.MIN_VALUE) != 0;
            boolean isPCDisplay = false;
            if (HwPCUtils.isPcCastModeInServer()) {
                isPCDisplay = HwPCUtils.isValidExtDisplayId(win.getDisplayId());
            }
            if (!HwPCUtils.isPcCastModeInServer() || isDefaultDisplay || !isPCDisplay) {
                hasNavBar = isDefaultDisplay && phoneWindowManager.mHasNavigationBar && phoneWindowManager.mNavigationBar != null && phoneWindowManager.mNavigationBar.isVisibleLw();
            } else {
                isNeedHideNaviBarWin = false;
                hasNavBar = phoneWindowManager.mHasNavigationBar && getNavigationBarExternal() != null && getNavigationBarExternal().isVisibleLw();
            }
            boolean isNeedHideNaviBarWin2 = isNeedHideNaviBarWin;
            boolean hasNavBar2 = hasNavBar;
            boolean isBaiDuOrSwiftkey = isBaiDuOrSwiftkey(win);
            boolean isLandScapeMultiWindowMode = isLandScapeMultiWindowMode();
            boolean isDocked = false;
            if (attrs8.type == 2) {
                isDocked = isLandScapeMultiWindowMode && windowState != phoneWindowManager.mInputMethodTarget;
            }
            int adjust3 = (!isLandScapeMultiWindowMode || windowState == phoneWindowManager.mInputMethodTarget) ? sim & 240 : 48;
            boolean requestedFullscreen = ((fl11 & 1024) == 0 && (requestedSysUiFl & 4) == 0) ? false : true;
            Rect of7 = of6;
            boolean layoutInScreen = (fl11 & 256) == 256;
            boolean layoutInsetDecor = (65536 & fl11) == 65536;
            sf4.set(displayFrames7.mStable);
            int sim2 = sim;
            if (!HwPCUtils.isPcCastModeInServer() || !isPCDisplay) {
                int sysUiFl11 = sysUiFl10;
                int adjust4 = adjust3;
                int fl12 = fl11;
                Rect dcf5 = vf4;
                WindowManager.LayoutParams attrs9 = attrs8;
                cf = cf5;
                vf = vf3;
                of = of7;
                int i3 = sim2;
                pf = pf6;
                int pfl2 = pfl;
                sf = sf4;
                df = df6;
                int adjust5 = type13;
                if (adjust5 == 2011) {
                    vf.set(displayFrames7.mDock);
                    cf.set(displayFrames7.mDock);
                    of.set(displayFrames7.mDock);
                    df.set(displayFrames7.mDock);
                    pf.set(displayFrames7.mDock);
                    boolean isPopIME = (phoneWindowManager.mInputMethodTarget == null || (win.getAttrs().hwFlags & DumpState.DUMP_DEXOPT) == 0) ? false : true;
                    if (isLandScapeMultiWindowMode && phoneWindowManager.mInputMethodTarget != null && isBaiDuOrSwiftkey && !isPopIME) {
                        if (!phoneWindowManager.mFrozen) {
                            if (phoneWindowManager.mInputMethodTarget.getAttrs().type == 2) {
                                rect = phoneWindowManager.mInputMethodTarget.getDisplayFrameLw();
                            } else {
                                rect = phoneWindowManager.mInputMethodTarget.getContentFrameLw();
                            }
                            int i4 = rect.left;
                            vf.left = i4;
                            cf.left = i4;
                            of.left = i4;
                            df.left = i4;
                            pf.left = i4;
                            int i5 = rect.right;
                            vf.right = i5;
                            cf.right = i5;
                            of.right = i5;
                            df.right = i5;
                            pf.right = i5;
                        } else {
                            return;
                        }
                    }
                    int min = Math.min(displayFrames7.mUnrestricted.bottom, displayFrames7.mDisplayCutoutSafe.bottom);
                    of.bottom = min;
                    df.bottom = min;
                    pf.bottom = min;
                    int i6 = displayFrames7.mStable.bottom;
                    vf.bottom = i6;
                    cf.bottom = i6;
                    if (!mSupporInputMethodFilletAdaptation || hasNavBar2 || phoneWindowManager.mDisplayRotation != 0 || !win.isImeWithHwFlag()) {
                        attrs = attrs9;
                    } else {
                        attrs = attrs9;
                        if ((attrs.hwFlags & DumpState.DUMP_DEXOPT) == 0) {
                            int i7 = displayFrames7.mStable.bottom - (phoneWindowManager.mDefaultNavBarHeight / 2);
                            vf.bottom = i7;
                            cf.bottom = i7;
                            phoneWindowManager.mInputMethodMovedUp = true;
                            WindowManager.LayoutParams focusAttrs = phoneWindowManager.mFocusedWindow == null ? phoneWindowManager.mFocusedWindow.getAttrs() : null;
                            WindowManager.LayoutParams inputMethodAttrs = phoneWindowManager.mInputMethodTarget == null ? phoneWindowManager.mInputMethodTarget.getAttrs() : null;
                            if (!(focusAttrs == null || (focusAttrs.privateFlags & 1024) == 0 || hasNavBar2)) {
                                phoneWindowManager.mInputMethodMovedUp = false;
                                int i8 = displayFrames7.mSystem.bottom;
                                of.bottom = i8;
                                vf.bottom = i8;
                                cf.bottom = i8;
                                df.bottom = i8;
                                pf.bottom = i8;
                            }
                            if (!(inputMethodAttrs == null || !SEC_IME_PACKAGE.equals(win.getOwningPackage()) || (inputMethodAttrs.hwFlags & 32) == 0)) {
                                int calculateSecImeRaisePx = displayFrames7.mSystem.bottom - phoneWindowManager.calculateSecImeRaisePx(phoneWindowManager.mContext);
                                of.bottom = calculateSecImeRaisePx;
                                vf.bottom = calculateSecImeRaisePx;
                                cf.bottom = calculateSecImeRaisePx;
                                df.bottom = calculateSecImeRaisePx;
                                pf.bottom = calculateSecImeRaisePx;
                            }
                            if (phoneWindowManager.mStatusBar != null && phoneWindowManager.mFocusedWindow == phoneWindowManager.mStatusBar && phoneWindowManager.canReceiveInput(phoneWindowManager.mStatusBar)) {
                                if (phoneWindowManager.mNavigationBarPosition != 2) {
                                    int i9 = displayFrames7.mStable.right;
                                    vf.right = i9;
                                    cf.right = i9;
                                    of.right = i9;
                                    df.right = i9;
                                    pf.right = i9;
                                } else if (phoneWindowManager.mNavigationBarPosition == 1) {
                                    int i10 = displayFrames7.mStable.left;
                                    vf.left = i10;
                                    cf.left = i10;
                                    of.left = i10;
                                    df.left = i10;
                                    pf.left = i10;
                                }
                            }
                            attrs.gravity = 80;
                            phoneWindowManager.mDockLayer = win.getSurfaceLayer();
                            type2 = adjust5;
                            type = adjust4;
                            fl = fl12;
                            dcf = dcf5;
                            sysUiFl = sysUiFl11;
                        }
                    }
                    phoneWindowManager.mInputMethodMovedUp = false;
                    WindowManager.LayoutParams focusAttrs2 = phoneWindowManager.mFocusedWindow == null ? phoneWindowManager.mFocusedWindow.getAttrs() : null;
                    WindowManager.LayoutParams inputMethodAttrs2 = phoneWindowManager.mInputMethodTarget == null ? phoneWindowManager.mInputMethodTarget.getAttrs() : null;
                    phoneWindowManager.mInputMethodMovedUp = false;
                    int i82 = displayFrames7.mSystem.bottom;
                    of.bottom = i82;
                    vf.bottom = i82;
                    cf.bottom = i82;
                    df.bottom = i82;
                    pf.bottom = i82;
                    int calculateSecImeRaisePx2 = displayFrames7.mSystem.bottom - phoneWindowManager.calculateSecImeRaisePx(phoneWindowManager.mContext);
                    of.bottom = calculateSecImeRaisePx2;
                    vf.bottom = calculateSecImeRaisePx2;
                    cf.bottom = calculateSecImeRaisePx2;
                    df.bottom = calculateSecImeRaisePx2;
                    pf.bottom = calculateSecImeRaisePx2;
                    if (phoneWindowManager.mNavigationBarPosition != 2) {
                    }
                    attrs.gravity = 80;
                    phoneWindowManager.mDockLayer = win.getSurfaceLayer();
                    type2 = adjust5;
                    type = adjust4;
                    fl = fl12;
                    dcf = dcf5;
                    sysUiFl = sysUiFl11;
                } else {
                    attrs = attrs9;
                    if (adjust5 == 2031) {
                        of.set(displayFrames7.mUnrestricted);
                        df.set(displayFrames7.mUnrestricted);
                        pf.set(displayFrames7.mUnrestricted);
                        int type14 = adjust4;
                        if (type14 != 16) {
                            cf.set(displayFrames7.mDock);
                        } else {
                            cf.set(displayFrames7.mContent);
                        }
                        if (type14 != 48) {
                            vf.set(displayFrames7.mCurrent);
                        } else {
                            vf.set(cf);
                        }
                        fl = fl12;
                        dcf = dcf5;
                        sysUiFl = sysUiFl11;
                        int i11 = adjust5;
                        type = type14;
                        type2 = i11;
                    } else {
                        int adjust6 = adjust4;
                        if (adjust5 == 2013) {
                            type4 = adjust5;
                            type5 = adjust6;
                            attrs2 = attrs;
                            phoneWindowManager.layoutWallpaper(displayFrames7, pf, df, of, cf);
                        } else {
                            attrs2 = attrs;
                            type4 = adjust5;
                            type5 = adjust6;
                            if (windowState == phoneWindowManager.mStatusBar) {
                                of.set(displayFrames7.mUnrestricted);
                                df.set(displayFrames7.mUnrestricted);
                                pf.set(displayFrames7.mUnrestricted);
                                cf.set(displayFrames7.mStable);
                                vf.set(displayFrames7.mStable);
                                if (type5 == 16) {
                                    cf.bottom = displayFrames7.mContent.bottom;
                                } else {
                                    cf.bottom = displayFrames7.mDock.bottom;
                                    vf.bottom = displayFrames7.mContent.bottom;
                                }
                            } else {
                                Rect dcf6 = dcf5;
                                dcf6.set(displayFrames7.mSystem);
                                WindowManager.LayoutParams attrs10 = attrs2;
                                boolean inheritTranslucentDecor = (attrs10.privateFlags & 512) != 0;
                                int type15 = type4;
                                boolean isAppWindow = type15 >= 1 && type15 <= 99;
                                boolean topAtRest = windowState == phoneWindowManager.mTopFullscreenOpaqueWindowState && !win.isAnimatingLw();
                                if (!isAppWindow || inheritTranslucentDecor || topAtRest) {
                                    fl4 = fl12;
                                    sysUiFl2 = sysUiFl11;
                                } else {
                                    sysUiFl2 = sysUiFl11;
                                    if ((sysUiFl2 & 4) == 0) {
                                        fl4 = fl12;
                                        if ((fl4 & 1024) == 0 && (67108864 & fl4) == 0 && (fl4 & Integer.MIN_VALUE) == 0 && (pfl2 & 131072) == 0 && phoneWindowManager.mStatusBar != null && phoneWindowManager.mStatusBar.isVisibleLw() && (phoneWindowManager.mLastSystemUiFlags & 67108864) == 0) {
                                            dcf6.top = displayFrames7.mStable.top;
                                        }
                                    } else {
                                        fl4 = fl12;
                                    }
                                    if ((134217728 & fl4) == 0 && (sysUiFl2 & 2) == 0 && !isNeedHideNaviBarWin2 && (fl4 & Integer.MIN_VALUE) == 0) {
                                        dcf6.bottom = displayFrames7.mStable.bottom;
                                        dcf6.right = displayFrames7.mStable.right;
                                    }
                                }
                                if (!layoutInScreen || !layoutInsetDecor) {
                                    dcf = dcf6;
                                    int sysUiFl12 = sysUiFl2;
                                    WindowManager.LayoutParams attrs11 = attrs10;
                                    Rect cf6 = cf;
                                    int fl13 = fl4;
                                    DisplayFrames displayFrames8 = displayFrames7;
                                    int type16 = type15;
                                    int adjust7 = type5;
                                    Rect pf7 = pf;
                                    Rect df7 = df;
                                    Rect of8 = of;
                                    DisplayFrames displayFrames9 = displayFrames8;
                                    if (layoutInScreen) {
                                        sysUiFl4 = sysUiFl12;
                                        fl5 = fl13;
                                        attrs4 = attrs11;
                                        cf3 = cf6;
                                        type6 = type16;
                                        displayFrames4 = displayFrames9;
                                        of4 = of8;
                                        df4 = df7;
                                        pf4 = pf7;
                                        type7 = adjust7;
                                    } else if ((sysUiFl12 & 1536) == 0 || ((attrs11.flags & Integer.MIN_VALUE) != 0 && phoneWindowManager.getEmuiStyleValue(attrs11.isEmuiStyle) == 1)) {
                                        if (attached != null) {
                                            Rect cf7 = cf6;
                                            int sysUiFl13 = sysUiFl12;
                                            Rect pf8 = pf7;
                                            Rect df8 = df7;
                                            Rect of9 = of8;
                                            fl8 = fl13;
                                            int type17 = type16;
                                            displayFrames3 = displayFrames;
                                            phoneWindowManager.setAttachedWindowFrames(windowState, fl13, adjust7, attached, false, pf7, df7, of8, cf7, vf, displayFrames3);
                                            type10 = type17;
                                            attrs5 = attrs11;
                                            type9 = adjust7;
                                            cf2 = cf7;
                                            sysUiFl7 = sysUiFl13;
                                            pf3 = pf8;
                                            df3 = df8;
                                            of3 = of9;
                                        } else {
                                            WindowManager.LayoutParams attrs12 = attrs11;
                                            int adjust8 = adjust7;
                                            Rect cf8 = cf6;
                                            int sysUiFl14 = sysUiFl12;
                                            Rect pf9 = pf7;
                                            Rect df9 = df7;
                                            Rect of10 = of8;
                                            fl8 = fl13;
                                            int type18 = type16;
                                            if (type18 == 2014) {
                                                displayFrames3 = displayFrames;
                                                cf2 = cf8;
                                                cf2.set(displayFrames3.mRestricted);
                                                of3 = of10;
                                                of3.set(displayFrames3.mRestricted);
                                                df3 = df9;
                                                df3.set(displayFrames3.mRestricted);
                                                pf3 = pf9;
                                                pf3.set(displayFrames3.mRestricted);
                                                type10 = type18;
                                                attrs5 = attrs12;
                                                type9 = adjust8;
                                            } else {
                                                displayFrames3 = displayFrames;
                                                cf2 = cf8;
                                                pf3 = pf9;
                                                df3 = df9;
                                                of3 = of10;
                                                if (type18 == 2005) {
                                                    type8 = adjust8;
                                                } else if (type18 == 2003) {
                                                    type8 = adjust8;
                                                } else {
                                                    pf3.set(displayFrames3.mContent);
                                                    if (isDocked) {
                                                        pf3.bottom = displayFrames3.mDock.bottom;
                                                    }
                                                    if (win.isVoiceInteraction()) {
                                                        cf2.set(displayFrames3.mVoiceContent);
                                                        of3.set(displayFrames3.mVoiceContent);
                                                        df3.set(displayFrames3.mVoiceContent);
                                                        type9 = adjust8;
                                                    } else {
                                                        type9 = adjust8;
                                                        if (type9 != 16) {
                                                            cf2.set(displayFrames3.mDock);
                                                            of3.set(displayFrames3.mDock);
                                                            df3.set(displayFrames3.mDock);
                                                        } else {
                                                            cf2.set(displayFrames3.mContent);
                                                            of3.set(displayFrames3.mContent);
                                                            df3.set(displayFrames3.mContent);
                                                        }
                                                    }
                                                    if (type9 != 48) {
                                                        vf.set(displayFrames3.mCurrent);
                                                    } else {
                                                        vf.set(cf2);
                                                    }
                                                    type10 = type18;
                                                    attrs5 = attrs12;
                                                }
                                                cf2.set(displayFrames3.mStable);
                                                of3.set(displayFrames3.mStable);
                                                df3.set(displayFrames3.mStable);
                                                attrs3 = attrs12;
                                                if (attrs3.type == 2003) {
                                                    pf3.set(displayFrames3.mCurrent);
                                                } else {
                                                    pf3.set(displayFrames3.mStable);
                                                }
                                                if (CoordinationModeUtils.isFoldable()) {
                                                    type2 = type18;
                                                    phoneWindowManager = this;
                                                    int createState = CoordinationModeUtils.getInstance(phoneWindowManager.mContext).getCoordinationCreateMode();
                                                    if (!isLandscape()) {
                                                        if (createState == 3) {
                                                            int foldScreenSubWidth = CoordinationModeUtils.getFoldScreenSubWidth();
                                                            vf.right = foldScreenSubWidth;
                                                            pf3.right = foldScreenSubWidth;
                                                            df3.right = foldScreenSubWidth;
                                                            of3.right = foldScreenSubWidth;
                                                        } else if (createState == 4) {
                                                            int foldScreenEdgeWidth = CoordinationModeUtils.getFoldScreenEdgeWidth() + CoordinationModeUtils.getFoldScreenSubWidth();
                                                            vf.left = foldScreenEdgeWidth;
                                                            pf3.left = foldScreenEdgeWidth;
                                                            df3.left = foldScreenEdgeWidth;
                                                            of3.left = foldScreenEdgeWidth;
                                                        }
                                                    } else if (createState == 3) {
                                                        int foldScreenSubWidth2 = CoordinationModeUtils.getFoldScreenSubWidth();
                                                        vf.top = foldScreenSubWidth2;
                                                        pf3.top = foldScreenSubWidth2;
                                                        df3.top = foldScreenSubWidth2;
                                                        of3.top = foldScreenSubWidth2;
                                                    } else if (createState == 4) {
                                                        int foldScreenEdgeWidth2 = CoordinationModeUtils.getFoldScreenEdgeWidth() + CoordinationModeUtils.getFoldScreenSubWidth();
                                                        vf.top = foldScreenEdgeWidth2;
                                                        pf3.top = foldScreenEdgeWidth2;
                                                        df3.top = foldScreenEdgeWidth2;
                                                        of3.top = foldScreenEdgeWidth2;
                                                    }
                                                    sysUiFl3 = sysUiFl14;
                                                    fl = fl8;
                                                } else {
                                                    type2 = type18;
                                                    phoneWindowManager = this;
                                                    sysUiFl3 = sysUiFl14;
                                                    fl = fl8;
                                                }
                                            }
                                            sysUiFl7 = sysUiFl14;
                                        }
                                        fl = fl8;
                                        phoneWindowManager = this;
                                    } else {
                                        sysUiFl4 = sysUiFl12;
                                        fl5 = fl13;
                                        attrs4 = attrs11;
                                        cf3 = cf6;
                                        type6 = type16;
                                        displayFrames4 = displayFrames9;
                                        of4 = of8;
                                        df4 = df7;
                                        pf4 = pf7;
                                        type7 = adjust7;
                                    }
                                    if (type2 == 2014) {
                                        sysUiFl6 = sysUiFl4;
                                        fl7 = fl5;
                                    } else if (type2 == 2017) {
                                        sysUiFl6 = sysUiFl4;
                                        fl7 = fl5;
                                    } else {
                                        if (type2 == 2019) {
                                            sysUiFl5 = sysUiFl4;
                                            fl6 = fl5;
                                        } else if (type2 == 2024) {
                                            sysUiFl5 = sysUiFl4;
                                            fl6 = fl5;
                                        } else {
                                            if (type2 == 2015 || type2 == 2036) {
                                                fl6 = fl5;
                                                if ((fl6 & 1024) != 0) {
                                                    cf2.set(displayFrames3.mOverscan);
                                                    of3.set(displayFrames3.mOverscan);
                                                    df3.set(displayFrames3.mOverscan);
                                                    pf3.set(displayFrames3.mOverscan);
                                                }
                                                if (type2 != 2021) {
                                                    cf2.set(displayFrames3.mOverscan);
                                                    of3.set(displayFrames3.mOverscan);
                                                    df3.set(displayFrames3.mOverscan);
                                                    pf3.set(displayFrames3.mOverscan);
                                                } else if ((33554432 & fl6) == 0 || type2 < 1 || type2 > 1999) {
                                                    if (canHideNavigationBar()) {
                                                        sysUiFl5 = sysUiFl4;
                                                        if ((sysUiFl5 & 512) != 0 && (type2 == 2000 || type2 == 2005 || type2 == 2034 || type2 == 2033 || (type2 >= 1 && type2 <= 1999))) {
                                                            cf2.set(displayFrames3.mUnrestricted);
                                                            of3.set(displayFrames3.mUnrestricted);
                                                            df3.set(displayFrames3.mUnrestricted);
                                                            pf3.set(displayFrames3.mUnrestricted);
                                                            phoneWindowManager.applyStableConstraints(sysUiFl3, fl, cf2, displayFrames3);
                                                            if (type == 48) {
                                                                vf.set(displayFrames3.mCurrent);
                                                            } else {
                                                                vf.set(cf2);
                                                            }
                                                        }
                                                    } else {
                                                        sysUiFl5 = sysUiFl4;
                                                    }
                                                    if (mUsingHwNavibar) {
                                                    }
                                                    if (!isNeedHideNaviBarWin2) {
                                                        if ("com.huawei.systemui.mk.lighterdrawer.LighterDrawView".equals(attrs3.getTitle())) {
                                                            cf2.set(displayFrames3.mUnrestricted);
                                                            of3.set(displayFrames3.mUnrestricted);
                                                            df3.set(displayFrames3.mUnrestricted);
                                                            pf3.set(displayFrames3.mUnrestricted);
                                                            vf.set(displayFrames3.mUnrestricted);
                                                        } else if ((sysUiFl5 & 1024) != 0) {
                                                            of3.set(displayFrames3.mRestricted);
                                                            df3.set(displayFrames3.mRestricted);
                                                            pf3.set(displayFrames3.mRestricted);
                                                            if (type != 16) {
                                                                cf2.set(displayFrames3.mDock);
                                                            } else {
                                                                cf2.set(displayFrames3.mContent);
                                                            }
                                                        } else {
                                                            cf2.set(displayFrames3.mRestricted);
                                                            of3.set(displayFrames3.mRestricted);
                                                            df3.set(displayFrames3.mRestricted);
                                                            pf3.set(displayFrames3.mRestricted);
                                                        }
                                                        phoneWindowManager.applyStableConstraints(sysUiFl3, fl, cf2, displayFrames3);
                                                        if (type == 48) {
                                                        }
                                                    }
                                                    cf2.set(displayFrames3.mUnrestricted);
                                                    of3.set(displayFrames3.mUnrestricted);
                                                    df3.set(displayFrames3.mUnrestricted);
                                                    pf3.set(displayFrames3.mUnrestricted);
                                                    phoneWindowManager.applyStableConstraints(sysUiFl3, fl, cf2, displayFrames3);
                                                    if (type == 48) {
                                                    }
                                                } else {
                                                    cf2.set(displayFrames3.mOverscan);
                                                    of3.set(displayFrames3.mOverscan);
                                                    df3.set(displayFrames3.mOverscan);
                                                    pf3.set(displayFrames3.mOverscan);
                                                }
                                            } else {
                                                fl6 = fl5;
                                                if (type2 != 2021) {
                                                }
                                            }
                                            sysUiFl5 = sysUiFl4;
                                            phoneWindowManager.applyStableConstraints(sysUiFl3, fl, cf2, displayFrames3);
                                            if (type == 48) {
                                            }
                                        }
                                        of3.set(displayFrames3.mUnrestricted);
                                        df3.set(displayFrames3.mUnrestricted);
                                        pf3.set(displayFrames3.mUnrestricted);
                                        phoneWindowManager.applyStableConstraints(sysUiFl3, fl, cf2, displayFrames3);
                                        if (type == 48) {
                                        }
                                    }
                                    cf2.set(displayFrames3.mUnrestricted);
                                    of3.set(displayFrames3.mUnrestricted);
                                    df3.set(displayFrames3.mUnrestricted);
                                    pf3.set(displayFrames3.mUnrestricted);
                                    if (hasNavBar2) {
                                        int i12 = displayFrames3.mDock.left;
                                        cf2.left = i12;
                                        of3.left = i12;
                                        df3.left = i12;
                                        pf3.left = i12;
                                        int i13 = displayFrames3.mRestricted.right;
                                        cf2.right = i13;
                                        of3.right = i13;
                                        df3.right = i13;
                                        pf3.right = i13;
                                        int i14 = displayFrames3.mRestricted.bottom;
                                        cf2.bottom = i14;
                                        of3.bottom = i14;
                                        df3.bottom = i14;
                                        pf3.bottom = i14;
                                    }
                                    phoneWindowManager.applyStableConstraints(sysUiFl3, fl, cf2, displayFrames3);
                                    if (type == 48) {
                                    }
                                } else if (attached != null) {
                                    int sysUiFl15 = sysUiFl2;
                                    fl = fl4;
                                    Rect dcf7 = dcf6;
                                    displayFrames3 = displayFrames;
                                    phoneWindowManager.setAttachedWindowFrames(windowState, fl, type5, attached, true, pf, df, of, cf, vf, displayFrames3);
                                    sysUiFl3 = sysUiFl15;
                                    attrs3 = attrs10;
                                    dcf = dcf7;
                                    type2 = type15;
                                    type = type5;
                                    pf3 = pf;
                                    df3 = df;
                                    of3 = of;
                                    cf2 = cf;
                                } else {
                                    int fl14 = fl4;
                                    int sysUiFl16 = sysUiFl2;
                                    WindowManager.LayoutParams attrs13 = attrs10;
                                    Rect dcf8 = dcf6;
                                    int adjust9 = type5;
                                    Rect pf10 = pf;
                                    Rect df10 = df;
                                    Rect of11 = of;
                                    Rect cf9 = cf;
                                    int type19 = type15;
                                    if (type19 == 2014) {
                                        displayFrames5 = displayFrames;
                                        fl9 = fl14;
                                        sysUiFl8 = sysUiFl16;
                                        pf5 = pf10;
                                        df5 = df10;
                                        of5 = of11;
                                    } else if (type19 == 2017) {
                                        displayFrames5 = displayFrames;
                                        fl9 = fl14;
                                        sysUiFl8 = sysUiFl16;
                                        pf5 = pf10;
                                        df5 = df10;
                                        of5 = of11;
                                    } else {
                                        fl9 = fl14;
                                        if ((33554432 & fl9) == 0 || type19 < 1 || type19 > 1999) {
                                            displayFrames5 = displayFrames;
                                            pf5 = pf10;
                                            df5 = df10;
                                            of5 = of11;
                                            if (canHideNavigationBar()) {
                                                sysUiFl8 = sysUiFl16;
                                                if ((sysUiFl8 & 512) != 0) {
                                                    if (type19 >= 1) {
                                                    }
                                                }
                                            } else {
                                                sysUiFl8 = sysUiFl16;
                                            }
                                            if (!isNeedHideNaviBarWin2) {
                                                df5.set(displayFrames5.mRestrictedOverscan);
                                                pf5.set(displayFrames5.mRestrictedOverscan);
                                                of5.set(displayFrames5.mUnrestricted);
                                                if ((fl9 & 1024) != 0) {
                                                    if (win.isVoiceInteraction()) {
                                                        cf4 = cf9;
                                                        cf4.set(displayFrames5.mVoiceContent);
                                                        adjust = adjust9;
                                                    } else {
                                                        cf4 = cf9;
                                                        adjust = adjust9;
                                                        if (adjust == 16) {
                                                            cf4.set(displayFrames5.mContent);
                                                        } else if (!HwFreeFormUtils.isFreeFormEnable() || win.getWindowingMode() != 5) {
                                                            cf4.set(displayFrames5.mDock);
                                                        } else {
                                                            cf4.set(displayFrames5.mContent);
                                                        }
                                                    }
                                                    synchronized (phoneWindowManager.mWindowManagerFuncs.getWindowManagerLock()) {
                                                        try {
                                                            if (!win.hasDrawnLw()) {
                                                                try {
                                                                    cf4.top = displayFrames5.mUnrestricted.top + phoneWindowManager.mStatusBarHeightForRotation[displayFrames5.mRotation];
                                                                } catch (Throwable th) {
                                                                    th = th;
                                                                    WindowManager.LayoutParams layoutParams = attrs13;
                                                                    Rect rect2 = dcf8;
                                                                }
                                                            }
                                                        } catch (Throwable th2) {
                                                            th = th2;
                                                            WindowManager.LayoutParams layoutParams2 = attrs13;
                                                            Rect rect3 = dcf8;
                                                            while (true) {
                                                                try {
                                                                    break;
                                                                } catch (Throwable th3) {
                                                                    th = th3;
                                                                }
                                                            }
                                                            throw th;
                                                        }
                                                    }
                                                } else {
                                                    attrs6 = attrs13;
                                                    dcf3 = dcf8;
                                                    adjust = adjust9;
                                                    cf4 = cf9;
                                                    cf4.set(displayFrames5.mRestricted);
                                                }
                                                if (isNeedHideNaviBarWin2) {
                                                    cf4.bottom = displayFrames5.mUnrestricted.bottom;
                                                }
                                                phoneWindowManager.applyStableConstraints(sysUiFl8, fl9, cf4, displayFrames5);
                                                if (adjust == 48) {
                                                    vf.set(displayFrames5.mCurrent);
                                                } else {
                                                    vf.set(cf4);
                                                }
                                                dcf = dcf3;
                                                sysUiFl3 = sysUiFl8;
                                                attrs3 = attrs6;
                                                fl = fl9;
                                                cf2 = cf4;
                                                type2 = type19;
                                                displayFrames3 = displayFrames5;
                                                of3 = of5;
                                                df3 = df5;
                                                pf3 = pf5;
                                                type = adjust;
                                            }
                                            df5.set(displayFrames5.mOverscan);
                                            pf5.set(displayFrames5.mOverscan);
                                            of5.set(displayFrames5.mUnrestricted);
                                            if ((fl9 & 1024) != 0) {
                                            }
                                            if (isNeedHideNaviBarWin2) {
                                            }
                                            phoneWindowManager.applyStableConstraints(sysUiFl8, fl9, cf4, displayFrames5);
                                            if (adjust == 48) {
                                            }
                                            dcf = dcf3;
                                            sysUiFl3 = sysUiFl8;
                                            attrs3 = attrs6;
                                            fl = fl9;
                                            cf2 = cf4;
                                            type2 = type19;
                                            displayFrames3 = displayFrames5;
                                            of3 = of5;
                                            df3 = df5;
                                            pf3 = pf5;
                                            type = adjust;
                                        } else {
                                            displayFrames5 = displayFrames;
                                            of5 = of11;
                                            of5.set(displayFrames5.mOverscan);
                                            df5 = df10;
                                            df5.set(displayFrames5.mOverscan);
                                            pf5 = pf10;
                                            pf5.set(displayFrames5.mOverscan);
                                            sysUiFl8 = sysUiFl16;
                                            if ((fl9 & 1024) != 0) {
                                            }
                                            if (isNeedHideNaviBarWin2) {
                                            }
                                            phoneWindowManager.applyStableConstraints(sysUiFl8, fl9, cf4, displayFrames5);
                                            if (adjust == 48) {
                                            }
                                            dcf = dcf3;
                                            sysUiFl3 = sysUiFl8;
                                            attrs3 = attrs6;
                                            fl = fl9;
                                            cf2 = cf4;
                                            type2 = type19;
                                            displayFrames3 = displayFrames5;
                                            of3 = of5;
                                            df3 = df5;
                                            pf3 = pf5;
                                            type = adjust;
                                        }
                                    }
                                    int i15 = (hasNavBar2 ? displayFrames5.mDock : displayFrames5.mUnrestricted).left;
                                    of5.left = i15;
                                    df5.left = i15;
                                    pf5.left = i15;
                                    int i16 = displayFrames5.mUnrestricted.top;
                                    of5.top = i16;
                                    df5.top = i16;
                                    pf5.top = i16;
                                    if (hasNavBar2) {
                                        i = displayFrames5.mRestricted.right;
                                    } else {
                                        i = displayFrames5.mUnrestricted.right;
                                    }
                                    of5.right = i;
                                    df5.right = i;
                                    pf5.right = i;
                                    if (hasNavBar2) {
                                        i2 = displayFrames5.mRestricted.bottom;
                                    } else {
                                        i2 = displayFrames5.mUnrestricted.bottom;
                                    }
                                    of5.bottom = i2;
                                    df5.bottom = i2;
                                    pf5.bottom = i2;
                                    if ((fl9 & 1024) != 0) {
                                    }
                                    if (isNeedHideNaviBarWin2) {
                                    }
                                    phoneWindowManager.applyStableConstraints(sysUiFl8, fl9, cf4, displayFrames5);
                                    if (adjust == 48) {
                                    }
                                    dcf = dcf3;
                                    sysUiFl3 = sysUiFl8;
                                    attrs3 = attrs6;
                                    fl = fl9;
                                    cf2 = cf4;
                                    type2 = type19;
                                    displayFrames3 = displayFrames5;
                                    of3 = of5;
                                    df3 = df5;
                                    pf3 = pf5;
                                    type = adjust;
                                }
                                if (phoneWindowManager.mHwFullScreenWindow != null && phoneWindowManager.mHwFullScreenWinVisibility && phoneWindowManager.mDisplayRotation == 0 && !phoneWindowManager.isAboveFullScreen(windowState, phoneWindowManager.mHwFullScreenWindow) && !win.toString().contains("GestureNavBottom") && !win.toString().contains("ChargingAnimView") && !win.toString().contains("ProximityWnd") && !win.toString().contains("fingerprint_alpha_layer") && !win.toString().contains("fingerprintview_button") && !win.toString().contains("fingerprint_mask")) {
                                    if (win.getOwningPackage() == null || (!win.getOwningPackage().contains("com.huawei.android.launcher") && !win.getOwningPackage().contains("com.huawei.aod"))) {
                                        Rect cf_fullWinBtn = phoneWindowManager.mHwFullScreenWindow.getContentFrameLw();
                                        if (cf_fullWinBtn.top > 0 && cf_fullWinBtn.left == 0 && cf.bottom > cf_fullWinBtn.top) {
                                            sysUiFl = sysUiFl3;
                                            Rect rect4 = cf_fullWinBtn;
                                            int i17 = (displayFrames3.mUnrestricted.top + phoneWindowManager.mRestrictedScreenHeight) - (cf.bottom - cf_fullWinBtn.top);
                                            cf.bottom = i17;
                                            of.bottom = i17;
                                            df.bottom = i17;
                                            pf.bottom = i17;
                                        }
                                    } else {
                                        sysUiFl = sysUiFl3;
                                    }
                                }
                                sysUiFl = sysUiFl3;
                            }
                        }
                        fl = fl12;
                        dcf = dcf5;
                        sysUiFl = sysUiFl11;
                        type2 = type4;
                        attrs = attrs2;
                    }
                }
            } else {
                if (attached != null) {
                    Rect of12 = of7;
                    Rect df11 = df6;
                    Rect pf11 = pf6;
                    sysUiFl9 = sysUiFl10;
                    adjust2 = adjust3;
                    int i18 = sim2;
                    int i19 = pfl;
                    fl10 = fl11;
                    sf2 = sf4;
                    type11 = type13;
                    dcf4 = vf4;
                    Rect cf10 = cf5;
                    vf2 = vf3;
                    attrs7 = attrs8;
                    displayFrames6 = displayFrames;
                    phoneWindowManager.setAttachedWindowFrames(windowState, fl11, adjust3, attached, true, pf11, df11, of12, cf10, vf2, displayFrames6);
                    of = of12;
                    df = df11;
                    pf = pf11;
                    cf = cf10;
                } else {
                    sysUiFl9 = sysUiFl10;
                    adjust2 = adjust3;
                    fl10 = fl11;
                    sf2 = sf4;
                    dcf4 = vf4;
                    attrs7 = attrs8;
                    vf2 = vf3;
                    type11 = type13;
                    Rect of13 = of7;
                    int i20 = sim2;
                    int i21 = pfl;
                    displayFrames6 = displayFrames;
                    int i22 = displayFrames6.mOverscan.left;
                    cf = cf5;
                    cf.left = i22;
                    of = of13;
                    of.left = i22;
                    df = df6;
                    df.left = i22;
                    pf = pf6;
                    pf.left = i22;
                    int i23 = displayFrames6.mOverscan.top;
                    cf.top = i23;
                    of.top = i23;
                    df.top = i23;
                    pf.top = i23;
                    int i24 = displayFrames6.mOverscan.right;
                    cf.right = i24;
                    of.right = i24;
                    df.right = i24;
                    pf.right = i24;
                    int i25 = displayFrames6.mOverscan.bottom;
                    cf.bottom = i25;
                    of.bottom = i25;
                    df.bottom = i25;
                    pf.bottom = i25;
                }
                if (attrs7.type == 2011) {
                    int i26 = displayFrames6.mDock.left;
                    Rect vf5 = vf2;
                    vf5.left = i26;
                    cf.left = i26;
                    of.left = i26;
                    df.left = i26;
                    pf.left = i26;
                    int i27 = displayFrames6.mDock.top;
                    vf5.top = i27;
                    cf.top = i27;
                    of.top = i27;
                    df.top = i27;
                    pf.top = i27;
                    int i28 = displayFrames6.mDock.right;
                    vf5.right = i28;
                    cf.right = i28;
                    of.right = i28;
                    df.right = i28;
                    pf.right = i28;
                    int height = displayFrames6.mUnrestricted.top + displayFrames6.mUnrestricted.height();
                    of.bottom = height;
                    df.bottom = height;
                    pf.bottom = height;
                    int i29 = displayFrames6.mStable.bottom;
                    vf5.bottom = i29;
                    cf.bottom = i29;
                    attrs7.gravity = 80;
                    phoneWindowManager.mDockLayer = win.getSurfaceLayer();
                    attrs = attrs7;
                    sysUiFl = sysUiFl9;
                    fl = fl10;
                    type2 = type11;
                    sf = sf2;
                    dcf = dcf4;
                    vf = vf5;
                    type = adjust2;
                } else {
                    Rect vf6 = vf2;
                    int sysUiFl17 = sysUiFl9;
                    if ((sysUiFl17 & 2) != 0) {
                        sf3 = sf2;
                    } else if (getNavigationBarExternal() == null || getNavigationBarExternal().isVisibleLw()) {
                        sf3 = sf2;
                        cf.set(sf3);
                        if (attrs7.type == 2008) {
                            cf.bottom = displayFrames6.mContent.bottom;
                        }
                        if (attrs7.gravity == 80) {
                            attrs7.gravity = 17;
                        }
                        if ((attrs7.softInputMode & 240) != 16) {
                            sf = sf3;
                            WindowManager.LayoutParams attrs14 = attrs7;
                            vf = vf6;
                            phoneWindowManager.layoutWindowForPadPCMode(windowState, pf, df, cf, vf6, displayFrames6.mContent.bottom);
                            type = adjust2;
                            fl = fl10;
                            type2 = type11;
                            dcf = dcf4;
                            sysUiFl = sysUiFl17;
                            attrs = attrs14;
                        } else {
                            sf = sf3;
                            WindowManager.LayoutParams layoutParams3 = attrs7;
                            vf = vf6;
                            sysUiFl = sysUiFl17;
                            type = adjust2;
                            fl = fl10;
                            type2 = type11;
                            dcf = dcf4;
                            attrs = layoutParams3;
                        }
                    } else {
                        sf3 = sf2;
                    }
                    cf.set(df);
                    if (attrs7.type == 2008) {
                    }
                    if (attrs7.gravity == 80) {
                    }
                    if ((attrs7.softInputMode & 240) != 16) {
                    }
                }
            }
            int i30 = sysUiFl;
            int fl15 = fl;
            Rect rect5 = df;
            Rect df12 = df;
            int type20 = type2;
            Rect rect6 = of;
            Rect of14 = of;
            WindowManager.LayoutParams attrs15 = attrs;
            int i31 = type;
            Rect pf12 = pf;
            phoneWindowManager.overrideRectForForceRotation(windowState, pf, rect5, rect6, cf, vf, dcf);
            if ((isNeedHideNaviBarWin2 || !mUsingHwNavibar) && attrs15.type == 2004) {
                z = false;
                vf.top = 0;
                cf.top = 0;
                int i32 = pf12.bottom;
                vf.bottom = i32;
                cf.bottom = i32;
            } else {
                z = false;
            }
            if (!mUsingHwNavibar) {
                dcf2 = dcf;
            } else if (isNeedHideNaviBarWin2 || phoneWindowManager.mHwPWMEx.getNaviBarFlag()) {
                int i33 = pf12.bottom;
                vf.bottom = i33;
                cf.bottom = i33;
                dcf2 = dcf;
                dcf2.bottom = i33;
            } else {
                dcf2 = dcf;
            }
            boolean parentFrameWasClippedByDisplayCutout2 = false;
            int cutoutMode2 = attrs15.layoutInDisplayCutoutMode;
            boolean attachedInParent = (attached == null || layoutInScreen) ? z : true;
            boolean requestedHideNavigation = (requestedSysUiFl & 2) != 0 ? true : z;
            boolean floatingInScreenWindow = (attrs15.isFullscreen() || !layoutInScreen || type20 == 1) ? z : true;
            phoneWindowManager.layoutWindowLwForNotch(windowState, attrs15);
            boolean isNoneNotchAppInHideMode = false;
            if (IS_NOTCH_PROP) {
                if (!phoneWindowManager.mIsNotchSwitchOpen) {
                    phoneWindowManager.mLayoutBeyondDisplayCutout = !canLayoutInDisplayCutout(win);
                } else if (phoneWindowManager.mDisplayRotation == 1 || phoneWindowManager.mDisplayRotation == 3) {
                    phoneWindowManager.mLayoutBeyondDisplayCutout = !((win.toString().contains("com.qeexo.smartshot.CropActivity") || win.toString().contains("com.huawei.ucd.walllpaper1.GLWallpaperService") || win.toString().contains("com.huawei.android.launcher")) ? true : z) ? true : z;
                } else {
                    isNoneNotchAppInHideMode = phoneWindowManager.mIsNoneNotchAppInHideMode;
                    if (!phoneWindowManager.mIsNoneNotchAppInHideMode && canLayoutInDisplayCutout(win)) {
                        z2 = z;
                    } else {
                        z2 = true;
                    }
                    phoneWindowManager.mLayoutBeyondDisplayCutout = z2;
                }
                if (phoneWindowManager.mLayoutBeyondDisplayCutout && !win.toString().contains("PointerLocation")) {
                    z = true;
                }
                phoneWindowManager.mLayoutBeyondDisplayCutout = z;
            }
            boolean isNoneNotchAppInHideMode2 = isNoneNotchAppInHideMode;
            if (phoneWindowManager.mLayoutBeyondDisplayCutout || (!IS_NOTCH_PROP && cutoutMode2 != 1)) {
                Rect displayCutoutSafeExceptMaybeBars = mTmpDisplayCutoutSafeExceptMaybeBarsRect;
                pf2 = pf12;
                displayFrames2 = displayFrames;
                displayCutoutSafeExceptMaybeBars.set(displayFrames2.mDisplayCutoutSafe);
                if (layoutInScreen && layoutInsetDecor && !requestedFullscreen && cutoutMode2 == 0 && !isNoneNotchAppInHideMode2) {
                    displayCutoutSafeExceptMaybeBars.top = Integer.MIN_VALUE;
                }
                if (layoutInScreen && layoutInsetDecor && !requestedHideNavigation && cutoutMode2 == 0) {
                    int i34 = phoneWindowManager.mNavigationBarPosition;
                    if (i34 != 4) {
                        switch (i34) {
                            case 1:
                                displayCutoutSafeExceptMaybeBars.left = Integer.MIN_VALUE;
                                break;
                            case 2:
                                if (!IS_NOTCH_PROP || phoneWindowManager.mDisplayRotation != 3) {
                                    displayCutoutSafeExceptMaybeBars.right = HwBootFail.STAGE_BOOT_SUCCESS;
                                    break;
                                }
                        }
                    } else {
                        displayCutoutSafeExceptMaybeBars.bottom = HwBootFail.STAGE_BOOT_SUCCESS;
                    }
                }
                if (type20 == 2011 && phoneWindowManager.mNavigationBarPosition == 4) {
                    displayCutoutSafeExceptMaybeBars.bottom = HwBootFail.STAGE_BOOT_SUCCESS;
                }
                if (!attachedInParent && !floatingInScreenWindow) {
                    mTmpRect.set(pf2);
                    pf2.intersectUnchecked(displayCutoutSafeExceptMaybeBars);
                    parentFrameWasClippedByDisplayCutout2 = false | (!mTmpRect.equals(pf2));
                }
                df2 = df12;
                df2.intersectUnchecked(displayCutoutSafeExceptMaybeBars);
                parentFrameWasClippedByDisplayCutout = parentFrameWasClippedByDisplayCutout2;
            } else {
                parentFrameWasClippedByDisplayCutout = false;
                pf2 = pf12;
                df2 = df12;
                displayFrames2 = displayFrames;
            }
            cf.intersectUnchecked(displayFrames2.mDisplayCutoutSafe);
            if (!HwPCUtils.isPcCastModeInServer() || !isPCDisplay) {
                of2 = of14;
                fl2 = fl15;
                if (!((fl2 & 512) == 0 || type20 == 2010 || (win.isInMultiWindowMode() && !win.toString().contains("com.huawei.android.launcher")))) {
                    df2.top = -10000;
                    df2.left = -10000;
                    df2.bottom = 10000;
                    df2.right = 10000;
                    if (type20 != 2013) {
                        vf.top = -10000;
                        vf.left = -10000;
                        cf.top = -10000;
                        cf.left = -10000;
                        of2.top = -10000;
                        of2.left = -10000;
                        vf.bottom = 10000;
                        vf.right = 10000;
                        cf.bottom = 10000;
                        cf.right = 10000;
                        of2.bottom = 10000;
                        of2.right = 10000;
                    }
                }
            } else if (attrs15.type == 2005) {
                int i35 = displayFrames2.mStable.left;
                cf.left = i35;
                of2 = of14;
                of2.left = i35;
                df2.left = i35;
                pf2.left = i35;
                int i36 = displayFrames2.mStable.top;
                cf.top = i36;
                of2.top = i36;
                df2.top = i36;
                pf2.top = i36;
                int i37 = displayFrames2.mStable.right;
                cf.right = i37;
                of2.right = i37;
                df2.right = i37;
                pf2.right = i37;
                int i38 = displayFrames2.mStable.bottom;
                cf.bottom = i38;
                of2.bottom = i38;
                df2.bottom = i38;
                pf2.bottom = i38;
                fl2 = fl15;
            } else {
                of2 = of14;
                fl2 = fl15;
            }
            boolean useOutsets = phoneWindowManager.shouldUseOutsets(attrs15, fl2);
            if (!isDefaultDisplay || !useOutsets) {
                fl3 = fl2;
                cutoutMode = cutoutMode2;
                type3 = type20;
            } else {
                Rect osf2 = mTmpOutsetFrame;
                fl3 = fl2;
                cutoutMode = cutoutMode2;
                type3 = type20;
                osf2.set(cf.left, cf.top, cf.right, cf.bottom);
                int outset = ScreenShapeHelper.getWindowOutsetBottomPx(phoneWindowManager.mContext.getResources());
                if (outset > 0) {
                    int rotation = displayFrames2.mRotation;
                    if (rotation == 0) {
                        osf2.bottom += outset;
                    } else if (rotation == 1) {
                        osf2.right += outset;
                    } else if (rotation == 2) {
                        osf2.top -= outset;
                    } else if (rotation == 3) {
                        osf2.left -= outset;
                    }
                }
                osf = osf2;
            }
            if (win.toString().contains("DividerMenusView")) {
                pf2.top = displayFrames2.mUnrestricted.top;
                pf2.bottom = displayFrames2.mUnrestricted.bottom;
            }
            if ((!HwPCUtils.isPcCastModeInServer() || !isPCDisplay) && win.toString().contains("com.android.packageinstaller.permission.ui.GrantPermissionsActivity")) {
                int i39 = displayFrames2.mUnrestricted.top + phoneWindowManager.mStatusBarHeightForRotation[displayFrames2.mRotation];
                vf.top = i39;
                cf.top = i39;
                of2.top = i39;
                df2.top = i39;
                pf2.top = i39;
            }
            int i40 = fl3;
            Rect rect7 = df2;
            Rect rect8 = pf2;
            int i41 = cutoutMode;
            Rect rect9 = dcf2;
            Rect rect10 = vf;
            int type21 = type3;
            Rect rect11 = of2;
            WindowManager.LayoutParams layoutParams4 = attrs15;
            Rect rect12 = cf;
            windowState.computeFrameLw(pf2, df2, of2, cf, vf, dcf2, sf, osf, displayFrames2.mDisplayCutout, parentFrameWasClippedByDisplayCutout);
            if (type21 == 2011 && win.isVisibleLw() && !win.getGivenInsetsPendingLw()) {
                phoneWindowManager.setLastInputMethodWindowLw(null, null);
                phoneWindowManager.offsetInputMethodWindowLw(windowState, displayFrames2);
            }
            if (type21 == 2031 && win.isVisibleLw() && !win.getGivenInsetsPendingLw()) {
                phoneWindowManager.offsetVoiceInputWindowLw(windowState, displayFrames2);
            }
        }
    }

    private int calculateSecImeRaisePx(Context context) {
        int lcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
        float dpiScale = (((float) lcdDpi) * 1.0f) / ((float) SystemProperties.getInt("persist.sys.dpi", lcdDpi));
        return (int) ((((float) SEC_IME_RAISE_HEIGHT) * context.getResources().getDisplayMetrics().density * dpiScale) + 0.5f);
    }

    private boolean isAboveFullScreen(WindowManagerPolicy.WindowState win, WindowManagerPolicy.WindowState fullScreenWin) {
        boolean z = false;
        if (!win.isInAboveAppWindows()) {
            return false;
        }
        if (fullScreenWin.getLayer() < win.getLayer()) {
            z = true;
        }
        return z;
    }

    private boolean isBaiDuOrSwiftkey(WindowManagerPolicy.WindowState win) {
        String packageName = win.getAttrs().packageName;
        if (packageName == null || !packageName.contains("com.baidu.input_huawei")) {
            return false;
        }
        return true;
    }

    public void setInputMethodTargetWindow(WindowManagerPolicy.WindowState target) {
        this.mInputMethodTarget = target;
    }

    public void setFocusChangeIMEFrozenTag(boolean frozen) {
        this.mFrozen = frozen;
    }

    private boolean isLandScapeMultiWindowMode() {
        int dockSide = -1;
        try {
            dockSide = this.mWindowManager.getDockedStackSide();
        } catch (Exception e) {
            Log.w(TAG, "Failed to get dock side: " + e.getMessage());
        }
        if (!(this.mDisplayRotation == 1 || this.mDisplayRotation == 3) || dockSide == -1) {
            return false;
        }
        return true;
    }

    private void layoutWallpaper(DisplayFrames displayFrames, Rect pf, Rect df, Rect of, Rect cf) {
        df.set(displayFrames.mOverscan);
        pf.set(displayFrames.mOverscan);
        cf.set(displayFrames.mUnrestricted);
        of.set(displayFrames.mUnrestricted);
    }

    private void offsetInputMethodWindowLw(WindowManagerPolicy.WindowState win, DisplayFrames displayFrames) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top) + win.getGivenContentInsetsLw().top;
        adjustInsetSurfaceState(win, displayFrames, top);
        displayFrames.mContent.bottom = Math.min(displayFrames.mContent.bottom, top);
        displayFrames.mVoiceContent.bottom = Math.min(displayFrames.mVoiceContent.bottom, top);
        int top2 = win.getVisibleFrameLw().top + win.getGivenVisibleInsetsLw().top;
        displayFrames.mCurrent.bottom = Math.min(displayFrames.mCurrent.bottom, top2);
    }

    private void offsetVoiceInputWindowLw(WindowManagerPolicy.WindowState win, DisplayFrames displayFrames) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top) + win.getGivenContentInsetsLw().top;
        displayFrames.mVoiceContent.bottom = Math.min(displayFrames.mVoiceContent.bottom, top);
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
        this.mWindowSleepTokenNeeded = false;
        this.mHasCoverView = false;
    }

    public void applyPostLayoutPolicyLw(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs, WindowManagerPolicy.WindowState attached, WindowManagerPolicy.WindowState imeTarget) {
        boolean affectsSystemUi = win.canAffectSystemUiFlags();
        applyKeyguardPolicyLw(win, imeTarget);
        int fl = PolicyControl.getWindowFlags(win, attrs);
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
                this.mForceStatusBarTransparentWin = win;
            }
        }
        boolean inFullScreenOrSplitScreenSecondaryWindowingMode = false;
        boolean appWindow = (attrs.type >= 1 && attrs.type < 2000) || attrs.type == 2100 || attrs.type == 2101;
        int windowingMode = win.getWindowingMode();
        if (windowingMode == 1 || windowingMode == 4) {
            inFullScreenOrSplitScreenSecondaryWindowingMode = true;
        }
        if (this.mTopFullscreenOpaqueWindowState == null && (attrs.type == 2100 || attrs.type == 2101)) {
            this.mHasCoverView = !sProximityWndName.equals(attrs.getTitle());
        } else if (this.mTopFullscreenOpaqueWindowState == null && affectsSystemUi) {
            if ((fl & 2048) != 0) {
                this.mForceStatusBar = true;
            }
            if (attrs.type == 2023 && (!this.mDreamingLockscreen || (win.isVisibleLw() && win.hasDrawnLw()))) {
                this.mShowingDream = true;
                appWindow = true;
            }
            if (appWindow && attached == null && attrs.isFullscreen() && inFullScreenOrSplitScreenSecondaryWindowingMode) {
                if (!this.mHasCoverView || isCoverWindow(win)) {
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
        if (this.mTopFullscreenOpaqueOrDimmingWindowState == null && affectsSystemUi && win.isDimming() && inFullScreenOrSplitScreenSecondaryWindowingMode) {
            this.mTopFullscreenOpaqueOrDimmingWindowState = win;
        }
        if (this.mTopDockedOpaqueWindowState == null && affectsSystemUi && appWindow && attached == null && attrs.isFullscreen() && windowingMode == 3) {
            this.mTopDockedOpaqueWindowState = win;
            if (this.mTopDockedOpaqueOrDimmingWindowState == null) {
                this.mTopDockedOpaqueOrDimmingWindowState = win;
            }
        }
        if (this.mTopDockedOpaqueOrDimmingWindowState == null && affectsSystemUi && win.isDimming() && windowingMode == 3) {
            this.mTopDockedOpaqueOrDimmingWindowState = win;
        }
        if (win.isVisibleLw() && (attrs.privateFlags & DumpState.DUMP_COMPILER_STATS) != 0 && win.canAcquireSleepToken()) {
            this.mWindowSleepTokenNeeded = true;
        }
    }

    private void applyKeyguardPolicyLw(WindowManagerPolicy.WindowState win, WindowManagerPolicy.WindowState imeTarget) {
        if (!canBeHiddenByKeyguardLw(win)) {
            return;
        }
        if (shouldBeHiddenByKeyguard(win, imeTarget)) {
            win.hideLw(false);
        } else {
            win.showLw(false);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCoverWindow(WindowManagerPolicy.WindowState win) {
        WindowManager.LayoutParams attrs = win == null ? null : win.getAttrs();
        int type = attrs == null ? 0 : attrs.type;
        if (type == 2100 || type == 2101) {
            return true;
        }
        return false;
    }

    private boolean isFullscreen(WindowManager.LayoutParams attrs) {
        return attrs.x == 0 && attrs.y == 0 && attrs.width == -1 && attrs.height == -1;
    }

    public int finishPostLayoutPolicyLw() {
        int changes = 0;
        boolean topIsFullscreen = false;
        if (this.mTopFullscreenOpaqueWindowState != null) {
            WindowManager.LayoutParams attrs = this.mTopFullscreenOpaqueWindowState.getAttrs();
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
            if (!(this.mForceStatusBarTransparent && !this.mForceStatusBar && !this.mForceStatusBarFromKeyguard)) {
                this.mStatusBarController.setShowTransparent(false);
            } else if (!this.mStatusBar.isVisibleLw()) {
                this.mStatusBarController.setShowTransparent(true);
            }
            WindowManager.LayoutParams statusBarAttrs = this.mStatusBar.getAttrs();
            boolean statusBarExpanded = statusBarAttrs.height == -1 && statusBarAttrs.width == -1;
            boolean topAppHidesStatusBar = topAppHidesStatusBar();
            if (this.mForceStatusBar || this.mForceStatusBarFromKeyguard || this.mForceStatusBarTransparent || ((this.mForceStatusBarTransparent && this.mForceStatusBarTransparentWin != null && this.mForceStatusBarTransparentWin.isVisibleLw()) || statusBarExpanded)) {
                if (this.mStatusBarController.setBarShowingLw(true)) {
                    changes = 0 | 1;
                }
                topIsFullscreen = this.mTopIsFullscreen && this.mStatusBar.isAnimatingLw();
                if ((this.mForceStatusBarFromKeyguard || statusBarExpanded) && this.mStatusBarController.isTransientShowing()) {
                    this.mStatusBarController.updateVisibilityLw(false, this.mLastSystemUiFlags, this.mLastSystemUiFlags);
                }
                if (statusBarExpanded && this.mNavigationBar != null && !computeNaviBarFlag() && this.mNavigationBarController.setBarShowingLw(true)) {
                    changes |= 1;
                }
            } else if (this.mTopFullscreenOpaqueWindowState != null) {
                topIsFullscreen = topAppHidesStatusBar;
                if (IS_NOTCH_PROP && this.mDisplayRotation == 0) {
                    int notchStatusBarColorStatus = this.mForceNotchStatusBar;
                    this.notchWindowChangeState = this.notchStatusBarColorLw != notchStatusBarColorStatus;
                    if (this.notchWindowChangeState) {
                        this.notchStatusBarColorLw = notchStatusBarColorStatus;
                        notchStatusBarColorUpdate(notchStatusBarColorStatus);
                        this.notchWindowChange = true;
                    } else if (this.mFocusedWindow != null && !this.mTopFullscreenOpaqueWindowState.toString().equals(this.mFocusedWindow.toString()) && !this.notchWindowChangeState && this.mForceNotchStatusBar && this.notchWindowChange) {
                        notchStatusBarColorUpdate((int) notchStatusBarColorStatus);
                        this.notchWindowChange = false;
                    }
                }
                if (this.mStatusBarController.isTransientShowing()) {
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 0 | 1;
                    }
                } else if (!topIsFullscreen || ((!HwFreeFormUtils.isFreeFormEnable() && this.mWindowManagerInternal.isStackVisible(5)) || this.mWindowManagerInternal.isStackVisible(3))) {
                    if (this.mStatusBarController.isTransientHiding()) {
                        Slog.v(TAG, "not fullscreen but transientBarState is hiding, so reset");
                        this.mStatusBarController.sethwTransientBarState(0);
                    }
                    if (this.mStatusBarController.setBarShowingLw(true)) {
                        changes = 0 | 1;
                    }
                    topAppHidesStatusBar = false;
                } else if (this.mStatusBarController.setBarShowingLw(false)) {
                    changes = 0 | 1;
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
        if (this.mShowingDream != this.mLastShowingDream) {
            this.mLastShowingDream = this.mShowingDream;
            this.mWindowManagerFuncs.notifyShowingDreamChanged();
        }
        updateWindowSleepToken();
        updateLockScreenTimeout();
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
        boolean z = false;
        if (this.mTopFullscreenOpaqueWindowState == null) {
            return false;
        }
        int fl = PolicyControl.getWindowFlags(null, this.mTopFullscreenOpaqueWindowState.getAttrs());
        if (IS_NOTCH_PROP) {
            if (!hideNotchStatusBar(fl) && (this.mFocusedWindow == null || this.mFocusedWindow.getWindowingMode() != 5)) {
                return false;
            }
            this.mForceNotchStatusBar = false;
        }
        if (!((fl & 1024) == 0 && (this.mLastSystemUiFlags & 4) == 0)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean setKeyguardOccludedLw(boolean isOccluded, boolean force) {
        boolean wasOccluded = this.mKeyguardOccluded;
        boolean showing = this.mKeyguardDelegate.isShowing();
        boolean changed = wasOccluded != isOccluded || force;
        Slog.d(TAG, "setKeyguardOccluded occluded=" + isOccluded + " showing=" + showing + " changed=" + changed);
        if (!isOccluded && changed && showing) {
            this.mKeyguardOccluded = false;
            this.mKeyguardDelegate.setOccluded(false, true);
            if (this.mStatusBar != null) {
                this.mStatusBar.getAttrs().privateFlags |= 1024;
                if (!this.mKeyguardDelegate.hasLockscreenWallpaper()) {
                    this.mStatusBar.getAttrs().flags |= DumpState.DUMP_DEXOPT;
                }
            }
            return true;
        } else if (isOccluded && changed && showing) {
            this.mKeyguardOccluded = true;
            this.mKeyguardDelegate.setOccluded(true, false);
            if (this.mStatusBar != null) {
                this.mStatusBar.getAttrs().privateFlags &= -1025;
                this.mStatusBar.getAttrs().flags &= -1048577;
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
        return (this.mStatusBar == null || (this.mStatusBar.getAttrs().privateFlags & 1024) == 0) ? false : true;
    }

    public boolean allowAppAnimationsLw() {
        return !this.mShowingDream;
    }

    public int focusChangedLw(WindowManagerPolicy.WindowState lastFocus, WindowManagerPolicy.WindowState newFocus) {
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
        int newLidState = lidOpen;
        if (newLidState != this.mLidState) {
            this.mLidState = (int) newLidState;
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
        boolean keyguardActive;
        Intent intent;
        int lensCoverState = lensCovered;
        if (this.mCameraLensCoverState != lensCoverState) {
            if (this.mCameraLensCoverState == 1 && lensCoverState == 0) {
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
            this.mCameraLensCoverState = (int) lensCoverState;
        }
    }

    /* access modifiers changed from: package-private */
    public void setHdmiPlugged(boolean plugged) {
        if (this.mHdmiPlugged != plugged) {
            this.mHdmiPlugged = plugged;
            updateRotation(true, true);
            Intent intent = new Intent("android.intent.action.HDMI_PLUGGED");
            intent.addFlags(67108864);
            intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, plugged);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: package-private */
    public void initializeHdmiState() {
        int oldMask = StrictMode.allowThreadDiskReadsMask();
        try {
            initializeHdmiStateInternal();
        } finally {
            StrictMode.setThreadPolicyMask(oldMask);
        }
    }

    /* access modifiers changed from: package-private */
    public void initializeHdmiStateInternal() {
        boolean plugged = false;
        boolean z = false;
        if (new File("/sys/devices/virtual/switch/hdmi/state").exists()) {
            this.mHDMIObserver.startObserving("DEVPATH=/devices/virtual/switch/hdmi");
            FileReader reader = null;
            try {
                FileReader reader2 = new FileReader("/sys/class/switch/hdmi/state");
                char[] buf = new char[15];
                int n = reader2.read(buf);
                if (n > 1) {
                    plugged = Integer.parseInt(new String(buf, 0, n + -1)) != 0;
                }
                try {
                    reader2.close();
                } catch (IOException e) {
                }
            } catch (IOException ex) {
                Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex);
                if (reader != null) {
                    reader.close();
                }
            } catch (NumberFormatException ex2) {
                Slog.w(TAG, "Couldn't read hdmi state from /sys/class/switch/hdmi/state: " + ex2);
                if (reader != null) {
                    reader.close();
                }
            } catch (Throwable th) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e2) {
                    }
                }
                throw th;
            }
        }
        if (!plugged) {
            z = true;
        }
        this.mHdmiPlugged = z;
        setHdmiPlugged(!this.mHdmiPlugged);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x01e9, code lost:
        r17 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x01ed, code lost:
        r17 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x0229, code lost:
        if (android.media.session.MediaSessionLegacyHelper.getHelper(r1.mContext).isGlobalPriorityActive() == false) goto L_0x022d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x022b, code lost:
        r14 = r14 & -2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x022f, code lost:
        if ((r14 & 1) != 0) goto L_0x01fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x0231, code lost:
        r1.mBroadcastWakeLock.acquire();
        r0 = r1.mHandler.obtainMessage(3, new android.view.KeyEvent(r2));
        r0.setAsynchronous(true);
        r0.sendToTarget();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x025e, code lost:
        if (r9 != 25) goto L_0x0293;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0260, code lost:
        if (r7 == false) goto L_0x0289;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x0262, code lost:
        cancelPendingRingerToggleChordAction();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x0265, code lost:
        if (r6 == false) goto L_0x02cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x0269, code lost:
        if (r1.mScreenshotChordVolumeDownKeyTriggered != false) goto L_0x02cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x0271, code lost:
        if ((r19.getFlags() & 1024) != 0) goto L_0x02cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x0273, code lost:
        r1.mScreenshotChordVolumeDownKeyTriggered = true;
        r1.mScreenshotChordVolumeDownKeyTime = r19.getDownTime();
        r1.mScreenshotChordVolumeDownKeyConsumed = false;
        cancelPendingPowerKeyAction();
        interceptScreenshotChord();
        interceptAccessibilityShortcutChord();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x0289, code lost:
        r1.mScreenshotChordVolumeDownKeyTriggered = false;
        cancelPendingScreenshotChordAction();
        cancelPendingAccessibilityShortcutAction();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x0295, code lost:
        if (r9 != 24) goto L_0x02cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x0297, code lost:
        if (r7 == false) goto L_0x02c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x0299, code lost:
        if (r6 == false) goto L_0x02cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x029d, code lost:
        if (r1.mA11yShortcutChordVolumeUpKeyTriggered != false) goto L_0x02cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x02a5, code lost:
        if ((r19.getFlags() & 1024) != 0) goto L_0x02cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x02a7, code lost:
        r1.mA11yShortcutChordVolumeUpKeyTriggered = true;
        r1.mA11yShortcutChordVolumeUpKeyTime = r19.getDownTime();
        r1.mA11yShortcutChordVolumeUpKeyConsumed = false;
        cancelPendingPowerKeyAction();
        cancelPendingScreenshotChordAction();
        cancelPendingRingerToggleChordAction();
        interceptAccessibilityShortcutChord();
        interceptRingerToggleChord();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x02c3, code lost:
        r1.mA11yShortcutChordVolumeUpKeyTriggered = false;
        cancelPendingScreenshotChordAction();
        cancelPendingAccessibilityShortcutAction();
        cancelPendingRingerToggleChordAction();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x02d1, code lost:
        if (r7 == false) goto L_0x032c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x02d3, code lost:
        sendSystemKeyToStatusBarAsync(r19.getKeyCode());
        r4 = getTelecommService();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x02de, code lost:
        if (r4 == null) goto L_0x02f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x02e2, code lost:
        if (r1.mHandleVolumeKeysInWM != false) goto L_0x02f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x02e8, code lost:
        if (r4.isRinging() == false) goto L_0x02f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x02ea, code lost:
        android.util.Log.i(TAG, "interceptKeyBeforeQueueing: VOLUME key-down while ringing: Silence ringer!");
        r4.silenceRinger();
        r14 = r14 & -2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x02f8, code lost:
        r10 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x0302, code lost:
        r10 = getAudioService().getMode();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x0304, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x0305, code lost:
        android.util.Log.e(TAG, "Error getting AudioService in interceptKeyBeforeQueueing.", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x015a, code lost:
        r17 = r11;
     */
    /* JADX WARNING: Removed duplicated region for block: B:255:0x0434  */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x043b  */
    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        int keyCode;
        boolean z;
        int result;
        boolean isWakeKey;
        boolean useHapticFeedback;
        boolean z2;
        TelecomManager telecomManager;
        int audioMode;
        KeyEvent keyEvent = event;
        if (!this.mSystemBooted) {
            Log.d(TAG, "we have not yet booted, don't let key events do anything.");
            return 0;
        }
        boolean interactive = (policyFlags & 536870912) != 0;
        boolean down = event.getAction() == 0;
        boolean canceled = event.isCanceled();
        keyCode = event.getKeyCode();
        if (isBoPD && (keyCode == 187 || keyCode == 3 || keyCode == 515)) {
            return 0;
        }
        boolean isInjected = (policyFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0;
        if (this.mKeyguardDelegate == null) {
            z = false;
        } else {
            z = interactive ? isKeyguardShowingAndNotOccluded() : this.mKeyguardDelegate.isShowing();
        }
        boolean keyguardActive = z;
        if (HWFLOW) {
            Log.i(TAG, "interceptKeyTq keycode=" + keyCode + " interactive=" + interactive + " keyguardActive=" + keyguardActive + " policyFlags=" + Integer.toHexString(policyFlags) + " down " + down + " canceled " + canceled);
        }
        boolean isWakeKey2 = (policyFlags & 1) != 0 || event.isWakeKey();
        if (interactive || (isInjected && !isWakeKey2)) {
            result = 1;
            isWakeKey2 = false;
            if (interactive) {
                if (keyCode == this.mPendingWakeKey && !down) {
                    result = 0;
                }
                this.mPendingWakeKey = -1;
            }
        } else if (interactive || !shouldDispatchInputWhenNonInteractive(event)) {
            result = 0;
            if (isWakeKey2 && (!down || !isWakeKeyWhenScreenOff(keyCode))) {
                isWakeKey2 = false;
            }
            if (isWakeKey2 && down) {
                this.mPendingWakeKey = keyCode;
            }
        } else {
            result = 1;
            this.mPendingWakeKey = -1;
        }
        isWakeKey = isWakeKey2;
        if (!isValidGlobalKey(keyCode) || !this.mGlobalKeyManager.shouldHandleGlobalKey(keyCode, keyEvent)) {
            useHapticFeedback = down && (policyFlags & 2) != 0 && (!((event.getFlags() & 64) != 0) || this.mNavBarVirtualKeyHapticFeedbackEnabled) && event.getRepeatCount() == 0;
            switch (keyCode) {
                case 4:
                    if (!down) {
                        Jlog.d(381, "JLID_BACK_KEY_PRESS");
                        if (interceptBackKeyUp(event)) {
                            result &= -2;
                        }
                        boolean isMiniDock = this.mWindowManagerInternal.isMinimizedDock();
                        WindowManagerPolicy.WindowState imeWin = this.mWindowManagerFuncs.getInputMethodWindowLw();
                        if (canceled || !isMiniDock || ((imeWin == null || imeWin.isVisibleLw()) && imeWin != null)) {
                            if (isEqualBottomApp()) {
                                z2 = false;
                                hideRecentApps(false, false);
                                break;
                            }
                        } else {
                            try {
                                ActivityManager.getService().dismissSplitScreenMode(false);
                                uploadKeyEvent(4);
                            } catch (Exception e) {
                                Slog.i(TAG, "back press error!");
                            }
                        }
                        z2 = false;
                        break;
                    } else {
                        interceptBackKeyDown();
                    }
                case 5:
                    if (down) {
                        TelecomManager telecomManager2 = getTelecommService();
                        if (telecomManager2 != null && telecomManager2.isRinging()) {
                            Log.i(TAG, "interceptKeyBeforeQueueing: CALL key-down while ringing: Answer the call!");
                            telecomManager2.acceptRingingCall();
                            result &= -2;
                            break;
                        }
                    }
                    z2 = false;
                    break;
                case 6:
                    result &= -2;
                    if (!down) {
                        if (!this.mEndCallKeyHandled) {
                            this.mHandler.removeCallbacks(this.mEndCallLongPress);
                            if (!canceled && (((this.mEndcallBehavior & 1) == 0 || !goHome()) && (this.mEndcallBehavior & 2) != 0)) {
                                goToSleep(event.getEventTime(), 4, 0);
                                isWakeKey = false;
                                break;
                            }
                        }
                    } else {
                        TelecomManager telecomManager3 = getTelecommService();
                        boolean hungUp = false;
                        if (telecomManager3 != null) {
                            hungUp = telecomManager3.endCall();
                        }
                        if (interactive && !hungUp) {
                            this.mEndCallKeyHandled = false;
                            this.mHandler.postDelayed(this.mEndCallLongPress, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                            break;
                        } else {
                            this.mEndCallKeyHandled = true;
                            break;
                        }
                    }
                    break;
                default:
                    switch (keyCode) {
                        case 24:
                        case 25:
                            break;
                        case 26:
                            cancelPendingAccessibilityShortcutAction();
                            result &= -2;
                            isWakeKey = false;
                            if (!down) {
                                interceptPowerKeyUp(keyEvent, interactive, canceled);
                                break;
                            } else {
                                interceptPowerKeyDown(keyEvent, interactive);
                                break;
                            }
                        default:
                            switch (keyCode) {
                                case HdmiCecKeycode.CEC_KEYCODE_INITIAL_CONFIGURATION:
                                case HdmiCecKeycode.CEC_KEYCODE_SELECT_BROADCAST_TYPE:
                                case HdmiCecKeycode.CEC_KEYCODE_SELECT_SOUND_PRESENTATION:
                                case 88:
                                case 89:
                                case 90:
                                case 91:
                                    break;
                                default:
                                    switch (keyCode) {
                                        case 126:
                                        case 127:
                                            break;
                                        default:
                                            switch (keyCode) {
                                                case NetworkManagementService.NetdResponseCode.DnsProxyQueryResult:
                                                    break;
                                                case NetworkManagementService.NetdResponseCode.ClatdStatusResult:
                                                    result &= -2;
                                                    isWakeKey = false;
                                                    if (!this.mPowerManager.isInteractive()) {
                                                        useHapticFeedback = false;
                                                    }
                                                    if (!down) {
                                                        sleepRelease(event.getEventTime());
                                                        break;
                                                    } else {
                                                        sleepPress();
                                                        break;
                                                    }
                                                case UsbDescriptor.CLASSID_WIRELESS /*224*/:
                                                    result &= -2;
                                                    isWakeKey = true;
                                                    break;
                                                default:
                                                    switch (keyCode) {
                                                        case 280:
                                                        case 281:
                                                        case 282:
                                                        case 283:
                                                            result &= -2;
                                                            interceptSystemNavigationKey(event);
                                                            break;
                                                        default:
                                                            switch (keyCode) {
                                                                case HdmiCecKeycode.CEC_KEYCODE_RESERVED:
                                                                case 130:
                                                                    break;
                                                                case 164:
                                                                    break;
                                                                case 171:
                                                                    if (this.mShortPressOnWindowBehavior == 1 && this.mPictureInPictureVisible) {
                                                                        if (!down) {
                                                                            showPictureInPictureMenu(event);
                                                                        }
                                                                        result &= -2;
                                                                        break;
                                                                    }
                                                                case 219:
                                                                    boolean longPressed = event.getRepeatCount() > 0;
                                                                    if (down && longPressed) {
                                                                        Message msg = this.mHandler.obtainMessage(MSG_LAUNCH_ASSIST_LONG_PRESS);
                                                                        msg.setAsynchronous(true);
                                                                        msg.sendToTarget();
                                                                    }
                                                                    if (down || longPressed) {
                                                                    } else {
                                                                        boolean z3 = isInjected;
                                                                        Message msg2 = this.mHandler.obtainMessage(26, event.getDeviceId(), 0, null);
                                                                        msg2.setAsynchronous(true);
                                                                        msg2.sendToTarget();
                                                                    }
                                                                    result &= -2;
                                                                    break;
                                                                case 231:
                                                                    if (!down) {
                                                                        this.mBroadcastWakeLock.acquire();
                                                                        Message msg3 = this.mHandler.obtainMessage(12);
                                                                        msg3.setAsynchronous(true);
                                                                        msg3.sendToTarget();
                                                                    }
                                                                    result &= -2;
                                                                    boolean z4 = isInjected;
                                                                    break;
                                                                case 276:
                                                                    result &= -2;
                                                                    isWakeKey = false;
                                                                    if (!down) {
                                                                        this.mPowerManagerInternal.setUserInactiveOverrideFromWindowManager();
                                                                    }
                                                                    z2 = false;
                                                                    boolean z5 = isInjected;
                                                                    break;
                                                                case 701:
                                                                    if (down && !SystemProperties.getBoolean("sys.super_power_save", false)) {
                                                                        quickOpenCameraService("flip");
                                                                        break;
                                                                    }
                                                            }
                                                            break;
                                                    }
                                            }
                                    }
                            }
                    }
            }
        } else {
            if (isWakeKey) {
                wakeUp(event.getEventTime(), this.mAllowTheaterModeWakeFromKey, "android.policy:KEY");
            }
            Log.d(TAG, "key: " + keyCode + " , handled globally, just return the result " + result);
            return result;
        }
        if (((telecomManager != null && telecomManager.isInCall()) || audioMode == 3) && (result & 1) == 0) {
            MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(keyEvent, Integer.MIN_VALUE, false);
            z2 = false;
            if (useHapticFeedback) {
                performHapticFeedbackLw(null, 1, z2);
            }
            if (isWakeKey) {
                wakeUp(event.getEventTime(), this.mAllowTheaterModeWakeFromKey, "android.policy:KEY");
            }
            Log.d(TAG, "interceptKeyBeforeQueueing: key " + keyCode + " , result : " + result);
            return result;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Volume key pass to user ");
        sb.append((result & 1) != 0);
        Log.i(TAG, sb.toString());
        if (this.mUseTvRouting || this.mHandleVolumeKeysInWM) {
            result |= 1;
            z2 = false;
            if (useHapticFeedback) {
            }
            if (isWakeKey) {
            }
            Log.d(TAG, "interceptKeyBeforeQueueing: key " + keyCode + " , result : " + result);
            return result;
        }
        if ((result & 1) == 0) {
            MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(keyEvent, Integer.MIN_VALUE, true);
        }
        z2 = false;
        if (useHapticFeedback) {
        }
        if (isWakeKey) {
        }
        Log.d(TAG, "interceptKeyBeforeQueueing: key " + keyCode + " , result : " + result);
        return result;
    }

    public boolean isEqualBottomApp() {
        try {
            return FOCUSED_SPLIT_APP_ACTIVITY.equals(this.mWindowManager.getFocusedAppComponentName());
        } catch (RemoteException e) {
            Log.e(TAG, "Error get Focused App ComponentName.");
            return false;
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
        if ((!this.mAccessibilityManager.isEnabled() || !this.mAccessibilityManager.sendFingerprintGesture(event.getKeyCode())) && this.mSystemNavigationKeysEnabled) {
            sendSystemKeyToStatusBarAsync(event.getKeyCode());
        }
    }

    /* access modifiers changed from: private */
    public void sendSystemKeyToStatusBar(int keyCode) {
        IStatusBarService statusBar = getStatusBarService();
        if (statusBar != null) {
            try {
                statusBar.handleSystemKey(keyCode);
            } catch (RemoteException e) {
            }
        }
    }

    private void sendSystemKeyToStatusBarAsync(int keyCode) {
        Message message = this.mHandler.obtainMessage(24, keyCode, 0);
        message.setAsynchronous(true);
        this.mHandler.sendMessage(message);
    }

    /* access modifiers changed from: private */
    public void sendProposedRotationChangeToStatusBarInternal(int rotation, boolean isValid) {
        StatusBarManagerInternal statusBar = getStatusBarManagerInternal();
        if (statusBar != null) {
            statusBar.onProposedRotationChanged(rotation, isValid);
        }
    }

    private static boolean isValidGlobalKey(int keyCode) {
        if (keyCode != 26) {
            switch (keyCode) {
                case NetworkManagementService.NetdResponseCode.ClatdStatusResult:
                case UsbDescriptor.CLASSID_WIRELESS /*224*/:
                    break;
                default:
                    return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isWakeKeyWhenScreenOff(int keyCode) {
        boolean z = false;
        if (!(keyCode == MSG_LAUNCH_ASSIST_LONG_PRESS || keyCode == 79 || keyCode == 130)) {
            if (keyCode != 164) {
                if (keyCode != 222) {
                    switch (keyCode) {
                        case 24:
                        case 25:
                            break;
                        default:
                            switch (keyCode) {
                                case HdmiCecKeycode.CEC_KEYCODE_INITIAL_CONFIGURATION:
                                case HdmiCecKeycode.CEC_KEYCODE_SELECT_BROADCAST_TYPE:
                                case HdmiCecKeycode.CEC_KEYCODE_SELECT_SOUND_PRESENTATION:
                                case 88:
                                case 89:
                                case 90:
                                case 91:
                                    break;
                                default:
                                    switch (keyCode) {
                                        case 126:
                                        case 127:
                                            break;
                                        default:
                                            return true;
                                    }
                            }
                    }
                }
            }
            if (this.mDockMode != 0) {
                z = true;
            }
            return z;
        }
        return false;
    }

    public int interceptMotionBeforeQueueingNonInteractive(long whenNanos, int policyFlags) {
        if ((policyFlags & 1) != 0 && wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromMotion, "android.policy:MOTION")) {
            return 0;
        }
        if (shouldDispatchInputWhenNonInteractive(null) && !this.mInterceptInputForWaitBrightness) {
            return 1;
        }
        if (isTheaterModeEnabled() && (policyFlags & 1) != 0) {
            wakeUp(whenNanos / 1000000, this.mAllowTheaterModeWakeFromMotionWhenNotDreaming, "android.policy:MOTION");
        }
        return 0;
    }

    private boolean shouldDispatchInputWhenNonInteractive(KeyEvent event) {
        boolean displayOff = this.mDisplay == null || this.mDisplay.getState() == 1;
        if (displayOff && !this.mHasFeatureWatch) {
            return false;
        }
        if (isKeyguardShowingAndNotOccluded() && !displayOff) {
            return true;
        }
        if (this.mHasFeatureWatch && event != null && (event.getKeyCode() == 4 || event.getKeyCode() == 264)) {
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
            if (keyCode != 164) {
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
                }
            } else {
                try {
                    if (event.getRepeatCount() == 0) {
                        getAudioService().adjustSuggestedStreamVolume(101, Integer.MIN_VALUE, 4101, pkgName, TAG);
                    }
                } catch (Exception e3) {
                    Log.e(TAG, "Error dispatching mute in dispatchTvAudioEvent.", e3);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchMediaKeyWithWakeLock(KeyEvent event) {
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

    /* access modifiers changed from: package-private */
    public void dispatchMediaKeyRepeatWithWakeLock(KeyEvent event) {
        this.mHavePendingMediaKeyRepeatWithWakeLock = false;
        KeyEvent repeatEvent = KeyEvent.changeTimeRepeat(event, SystemClock.uptimeMillis(), 1, event.getFlags() | 128);
        Slog.d(TAG, "dispatchMediaKeyRepeatWithWakeLock: dispatch media key with long press");
        dispatchMediaKeyWithWakeLockToAudioService(repeatEvent);
        this.mBroadcastWakeLock.release();
    }

    /* access modifiers changed from: package-private */
    public void dispatchMediaKeyWithWakeLockToAudioService(KeyEvent event) {
        if (this.mActivityManagerInternal.isSystemReady()) {
            MediaSessionLegacyHelper.getHelper(this.mContext).sendMediaButtonEvent(event, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void launchVoiceAssistWithWakeLock() {
        IDeviceIdleController dic;
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_ASSIST);
        if (!keyguardOn()) {
            dic = new Intent("android.speech.action.WEB_SEARCH");
        } else {
            IDeviceIdleController dic2 = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
            if (dic2 != null) {
                try {
                    dic2.exitIdle("voice-search");
                } catch (RemoteException e) {
                }
            }
            IDeviceIdleController voiceIntent = new Intent("android.speech.action.VOICE_SEARCH_HANDS_FREE");
            voiceIntent.putExtra("android.speech.extras.EXTRA_SECURE", true);
            dic = voiceIntent;
        }
        startActivityAsUser(dic, UserHandle.CURRENT_OR_SELF);
        this.mBroadcastWakeLock.release();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004b, code lost:
        return;
     */
    public void requestTransientBars(WindowManagerPolicy.WindowState swipeTarget) {
        synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
            if (isUserSetupComplete()) {
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

    public void startedGoingToSleep(int why) {
        Slog.i(TAG, "UL_Power Started going to sleep... (why=" + why + ")");
        if (mSupportAod && !this.mIsActuralShutDown) {
            startAodService(7);
        }
        this.mGoingToSleep = true;
        this.mRequestedOrGoingToSleep = true;
        if (this.mCurrentUserId == 0) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "lock_screen_state", 0);
        }
        if (this.mKeyguardDelegate != null && needTurnOff(why) && needTurnOffWithDismissFlag(this.mTopFullscreenOpaqueWindowState)) {
            Flog.i(305, "call onScreenTurnedOff(" + why + ")");
            this.mKeyguardDelegate.onStartedGoingToSleep(why);
        }
    }

    public void finishedGoingToSleep(int why) {
        EventLog.writeEvent(70000, 0);
        Flog.i(NsdService.NativeResponseCode.SERVICE_LOST, "UL_Power Finished going to sleep... (why=" + why + ")");
        MetricsLogger.histogram(this.mContext, "screen_timeout", this.mLockScreenTimeout / 1000);
        this.mGoingToSleep = false;
        this.mRequestedOrGoingToSleep = false;
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
        removeFreeFormStackIfNeed();
        if (CoordinationModeUtils.isFoldable()) {
            this.mActivityManagerInternal.exitCoordinationModeInner(true, false);
        }
    }

    public void startedWakingUp() {
        EventLog.writeEvent(70000, 1);
        Slog.i(TAG, "UL_Power Started waking up...");
        if (mSupportAod) {
            if (this.mPickUpFlag) {
                this.mPickUpFlag = false;
                Slog.i(TAG, "startedWakingUp mPickUpFlag:" + this.mPickUpFlag);
            }
            startAodService(4);
        }
        synchronized (this.mLock) {
            this.mAwake = true;
            updateWakeGestureListenerLp();
            updateOrientationListenerLp();
            updateLockScreenTimeout();
        }
        if (this.mCurrentUserId == 0) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "lock_screen_state", 1);
        }
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.onStartedWakingUp();
        }
    }

    public void finishedWakingUp() {
        if (DEBUG_WAKEUP) {
            Slog.i(TAG, "UL_PowerFinished waking up...");
        }
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.onFinishedWakingUp();
        }
    }

    public boolean isStatusBarKeyguardShowing() {
        return isStatusBarKeyguard() && !this.mKeyguardOccluded;
    }

    /* access modifiers changed from: protected */
    public void wakeUpFromPowerKey(long eventTime) {
        Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Wakeing Up From PowerKey...");
        if (Jlog.isPerfTest()) {
            Jlog.i(2201, "JL_PWRSCRON_PWM_GETMESSAGE");
        }
        powerPressBDReport(982);
        wakeUp(eventTime, this.mAllowTheaterModeWakeFromPowerKey, "android.policy:POWER");
    }

    /* access modifiers changed from: private */
    public boolean wakeUp(long wakeTime, boolean wakeInTheaterMode, String reason) {
        boolean theaterModeEnabled = isTheaterModeEnabled();
        Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Wake Up wakeInTheaterMode=" + wakeInTheaterMode + ", theaterModeEnabled=" + theaterModeEnabled);
        if (!wakeInTheaterMode && theaterModeEnabled) {
            return false;
        }
        if (theaterModeEnabled) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "theater_mode_on", 0);
        }
        if (!"android.policy:POWER".equals(reason) || !this.mHwPWMEx.isNeedWaitForAuthenticate()) {
            this.mPowerManager.wakeUp(wakeTime, reason);
        } else {
            Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "isNeedWaitForAuthenticate");
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void finishKeyguardDrawn() {
        synchronized (this.mLock) {
            if (this.mScreenOnEarly) {
                if (!this.mKeyguardDrawComplete) {
                    this.mKeyguardDrawComplete = true;
                    if (this.mKeyguardDelegate != null) {
                        this.mHandler.removeMessages(6);
                    }
                    this.mWindowManagerDrawComplete = false;
                    Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power finishKeyguardDrawn -> waitForAllWindowsDrawn");
                    this.mWindowManagerInternal.waitForAllWindowsDrawn(this.mWindowManagerDrawCallback, 1000);
                }
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
            if (mSupportAod && !this.mIsActuralShutDown) {
                startAodService(3);
            }
        }
        reportScreenStateToVrManager(false);
    }

    private long getKeyguardDrawnTimeout() {
        return ((SystemServiceManager) LocalServices.getService(SystemServiceManager.class)).isBootCompleted() ? 1000 : 5000;
    }

    public void screenTurningOn(WindowManagerPolicy.ScreenOnListener screenOnListener) {
        Slog.i(TAG, "UL_Power Screen turning on...");
        updateScreenOffSleepToken(false);
        boolean isKeyguardDrawComplete = false;
        synchronized (this.mLock) {
            Slog.i(TAG, "UL_Power screen Turning On begin...");
            if (mSupportAod) {
                startAodService(5);
            }
            this.mScreenOnEarly = true;
            this.mScreenOnFully = false;
            this.mKeyguardDrawComplete = false;
            this.mWindowManagerDrawComplete = false;
            this.mScreenOnListener = screenOnListener;
            if (this.mKeyguardDelegate == null || !this.mKeyguardDelegate.hasKeyguard()) {
                Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, " null mKeyguardDelegate: setting mKeyguardDrawComplete.");
                isKeyguardDrawComplete = true;
            } else {
                this.mHandler.removeMessages(6);
                this.mHandler.sendEmptyMessageDelayed(6, getKeyguardDrawnTimeout());
                this.mKeyguardDelegate.onScreenTurningOn(this.mKeyguardDrawnCallback);
            }
        }
        if (isKeyguardDrawComplete) {
            finishKeyguardDrawn();
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
        if (!this.mWindowManagerInternal.isMinimizedDock()) {
            String topWindowName = this.mWindowManagerInternal.getFullStackTopWindow();
            if (topWindowName != null && topWindowName.contains("SplitScreenAppActivity")) {
                Flog.i(300, "start home for minimize dock.");
                startDockOrHome(true, false);
            }
        }
        reportScreenStateToVrManager(true);
    }

    public void screenTurningOff(WindowManagerPolicy.ScreenOffListener screenOffListener) {
        this.mWindowManagerFuncs.screenTurningOff(screenOffListener);
        synchronized (this.mLock) {
            if (this.mKeyguardDelegate != null) {
                this.mKeyguardDelegate.onScreenTurningOff();
            }
        }
    }

    public void setTpKeep(WindowManagerPolicy.TpKeepListener tpKeepListener) {
        this.mTpKeepListener = tpKeepListener;
    }

    public void setTPDozeMode(int scene, int mode) {
    }

    private void reportScreenStateToVrManager(boolean isScreenOn) {
        if (this.mVrManagerInternal != null) {
            this.mVrManagerInternal.onScreenStateChanged(isScreenOn);
        }
    }

    /* access modifiers changed from: private */
    public void finishWindowsDrawn() {
        synchronized (this.mLock) {
            if (DEBUG_WAKEUP) {
                Slog.i(TAG, "UL_Power finish Windows Drawn begin...");
            }
            if (this.mScreenOnEarly) {
                if (!this.mWindowManagerDrawComplete) {
                    this.mWindowManagerDrawComplete = true;
                    finishScreenTurningOn();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0095, code lost:
        if (r0 == null) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0097, code lost:
        r0.onScreenOn();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x009a, code lost:
        if (r2 == false) goto L_0x00a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        r5.mWindowManager.enableScreenIfNeeded();
     */
    private void finishScreenTurningOn() {
        boolean enableScreen;
        synchronized (this.mLock) {
            if (DEBUG_WAKEUP) {
                Slog.i(TAG, "UL_Power finish Screen Turning On begin...");
            }
            updateOrientationListenerLp();
        }
        synchronized (this.mLock) {
            if (DEBUG_WAKEUP) {
                Slog.d(TAG, "UL_Power finishScreenTurningOn: mAwake=" + this.mAwake + ", mScreenOnEarly=" + this.mScreenOnEarly + ", mScreenOnFully=" + this.mScreenOnFully + ", mKeyguardDrawComplete=" + this.mKeyguardDrawComplete + ", mWindowManagerDrawComplete=" + this.mWindowManagerDrawComplete);
            }
            if (!this.mScreenOnFully && this.mScreenOnEarly && this.mWindowManagerDrawComplete) {
                if (!this.mAwake || this.mKeyguardDrawComplete) {
                    Slog.i(TAG, "UL_Power Finished screen turning on...");
                    WindowManagerPolicy.ScreenOnListener listener = this.mScreenOnListener;
                    this.mScreenOnListener = null;
                    this.mScreenOnFully = true;
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
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000f, code lost:
        if (r2.ifBootMessageShowing == false) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0013, code lost:
        if (DEBUG_WAKEUP == false) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0015, code lost:
        android.util.Slog.d(TAG, "HOTA handleHideBootMessage: dismissing");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        r2.mHandler.post(new com.android.server.policy.PhoneWindowManager.AnonymousClass22(r2));
        r2.ifBootMessageShowing = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002b, code lost:
        if (r2.mBootMsgDialog == null) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002f, code lost:
        if (DEBUG_WAKEUP == false) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0031, code lost:
        android.util.Slog.d(TAG, "handleHideBootMessage: dismissing");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        r2.mBootMsgDialog.dismiss();
        r2.mBootMsgDialog = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0040, code lost:
        return;
     */
    public void handleHideBootMessage() {
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

    public boolean okToAnimate() {
        return this.mAwake && !this.mGoingToSleep;
    }

    public void enableKeyguard(boolean enabled) {
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.setKeyguardEnabled(enabled);
        }
    }

    public void exitKeyguardSecurely(WindowManagerPolicy.OnKeyguardExitResult callback) {
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.verifyUnlock(callback);
        }
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        boolean z = false;
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        if (this.mKeyguardDelegate.isShowing() && !this.mKeyguardOccluded) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean keyguardIsShowingTq() {
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

    public void dismissKeyguardLw(IKeyguardDismissCallback callback, CharSequence message) {
        if (this.mKeyguardDelegate != null && this.mKeyguardDelegate.isShowing()) {
            this.mKeyguardDelegate.dismiss(callback, message);
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
            if (!this.mKeyguardDrawnOnce) {
                if (!this.mKeyguardDrawComplete) {
                    z = false;
                }
            }
            z = true;
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

    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight, DisplayCutout displayCutout, Rect outInsets) {
        outInsets.setEmpty();
        getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, displayCutout, outInsets);
        outInsets.top = Math.max(outInsets.top, this.mStatusBarHeightForRotation[displayRotation]);
    }

    public void onProximityPositive() {
    }

    public void notifyRotationChange(int rotation) {
    }

    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight, DisplayCutout displayCutout, Rect outInsets) {
        outInsets.setEmpty();
        if (this.mHasNavigationBar) {
            int position = navigationBarPosition(displayWidth, displayHeight, displayRotation);
            if (position == 4) {
                outInsets.bottom = getNavigationBarHeight(displayRotation, this.mUiMode);
            } else if (position == 2) {
                outInsets.right = getNavigationBarWidth(displayRotation, this.mUiMode);
            } else if (position == 1) {
                outInsets.left = getNavigationBarWidth(displayRotation, this.mUiMode);
            }
        }
        if (displayCutout != null) {
            outInsets.left += displayCutout.getSafeInsetLeft();
            outInsets.top += displayCutout.getSafeInsetTop();
            outInsets.right += displayCutout.getSafeInsetRight();
            outInsets.bottom += displayCutout.getSafeInsetBottom();
        }
    }

    public boolean isNavBarForcedShownLw(WindowManagerPolicy.WindowState windowState) {
        return this.mForceShowSystemBars;
    }

    public int getNavBarPosition() {
        return this.mNavigationBarPosition;
    }

    public boolean isDockSideAllowed(int dockSide, int originalDockSide, int displayWidth, int displayHeight, int displayRotation) {
        return isDockSideAllowed(dockSide, originalDockSide, navigationBarPosition(displayWidth, displayHeight, displayRotation), this.mNavigationBarCanMove);
    }

    @VisibleForTesting
    static boolean isDockSideAllowed(int dockSide, int originalDockSide, int navBarPosition, boolean navigationBarCanMove) {
        boolean z = true;
        if (dockSide == 2) {
            return true;
        }
        if (((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).isInFoldFullDisplayMode()) {
            if (!(dockSide == 1 || dockSide == 3)) {
                z = false;
            }
            return z;
        } else if (navigationBarCanMove) {
            if (!((dockSide == 1 && navBarPosition == 2) || (dockSide == 3 && navBarPosition == 1))) {
                z = false;
            }
            return z;
        } else if (dockSide == originalDockSide) {
            return true;
        } else {
            if (dockSide == 1 && originalDockSide == 2) {
                return true;
            }
            if (mIsTablet && dockSide == 1 && originalDockSide == -1 && navBarPosition == 4) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void sendCloseSystemWindows() {
        PhoneWindow.sendCloseSystemWindows(this.mContext, null);
    }

    /* access modifiers changed from: package-private */
    public void sendCloseSystemWindows(String reason) {
        PhoneWindow.sendCloseSystemWindows(this.mContext, reason);
    }

    /* JADX WARNING: Removed duplicated region for block: B:117:0x015f A[Catch:{ RemoteException -> 0x00cc }] */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x0162 A[Catch:{ RemoteException -> 0x00cc }] */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0171 A[Catch:{ RemoteException -> 0x00cc }] */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x0175 A[Catch:{ RemoteException -> 0x00cc }] */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x0181 A[Catch:{ RemoteException -> 0x00cc }] */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x018d A[Catch:{ RemoteException -> 0x00cc }] */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x01a1 A[Catch:{ RemoteException -> 0x00cc }] */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x01b5 A[Catch:{ RemoteException -> 0x00cc }] */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x01c1 A[Catch:{ RemoteException -> 0x00cc }] */
    public int rotationForOrientationLw(int orientation, int lastRotation, boolean defaultDisplay) {
        int preferredRotation;
        int preferredRotation2;
        if (!this.mForceDefaultOrientation) {
            synchronized (this.mLock) {
                int sensorRotation = getRotationFromSensorOrFaceFR(orientation, lastRotation);
                int preferredRotation3 = -1;
                if (!defaultDisplay) {
                    preferredRotation3 = 0;
                } else if (this.mLidState == 1 && this.mLidOpenRotation >= 0) {
                    preferredRotation3 = this.mLidOpenRotation;
                } else if (this.mDockMode != 2 || (!this.mCarDockEnablesAccelerometer && this.mCarDockRotation < 0)) {
                    if (this.mDockMode == 1 || this.mDockMode == 3 || this.mDockMode == 4) {
                        if (!this.mDeskDockEnablesAccelerometer) {
                            if (this.mDeskDockRotation >= 0) {
                            }
                        }
                        if (this.mDeskDockEnablesAccelerometer) {
                            preferredRotation2 = sensorRotation;
                        } else {
                            preferredRotation2 = this.mDeskDockRotation;
                        }
                    }
                    if (this.mHdmiPlugged && this.mDemoHdmiRotationLock) {
                        preferredRotation3 = this.mDemoHdmiRotation;
                    } else if (this.mHdmiPlugged && this.mDockMode == 0 && this.mUndockedHdmiRotation >= 0) {
                        preferredRotation3 = this.mUndockedHdmiRotation;
                    } else if (this.mDemoRotationLock) {
                        preferredRotation3 = this.mDemoRotation;
                    } else if (this.mPersistentVrModeEnabled) {
                        preferredRotation3 = this.mPortraitRotation;
                    } else if (orientation == 14) {
                        preferredRotation3 = lastRotation;
                    } else if (!this.mSupportAutoRotation) {
                        preferredRotation3 = -1;
                    } else {
                        if (!((this.mUserRotationMode == 0 && (orientation == 2 || orientation == -1 || orientation == 11 || orientation == 12 || orientation == 13)) || orientation == 4 || orientation == 10 || orientation == 6)) {
                            if (orientation != 7) {
                                if (this.mUserRotationMode == 1 && orientation != 5) {
                                    int dockSide = -1;
                                    try {
                                        dockSide = this.mWindowManager.getDockedStackSide();
                                    } catch (RemoteException e) {
                                        Slog.e(TAG, "Remote Exception while getting dockside!");
                                    }
                                    if (dockSide != -1) {
                                        Slog.i(TAG, "Keep last Rotate as preferred in DockMode while auto-rotation off with lastRotation = " + lastRotation);
                                        preferredRotation = lastRotation;
                                    } else {
                                        preferredRotation = this.mUserRotation;
                                    }
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("rotationForOrientationLw orientation=");
                                    sb.append(orientation);
                                    sb.append(", lastRotation=");
                                    sb.append(lastRotation);
                                    sb.append(", sensorRotation=");
                                    sb.append(sensorRotation);
                                    sb.append(", preferredRotation=");
                                    sb.append(preferredRotation3);
                                    sb.append("); user=");
                                    sb.append(this.mUserRotation);
                                    sb.append(" ");
                                    sb.append(this.mUserRotationMode == 1 ? "USER_ROTATION_LOCKED" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                                    Flog.i(308, sb.toString());
                                    switch (orientation) {
                                        case 0:
                                            break;
                                        case 1:
                                            break;
                                        case 6:
                                        case 11:
                                            break;
                                        case 7:
                                        case 12:
                                            break;
                                        case 8:
                                            break;
                                        case 9:
                                            break;
                                    }
                                } else {
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append("rotationForOrientationLw orientation=");
                                    sb2.append(orientation);
                                    sb2.append(", lastRotation=");
                                    sb2.append(lastRotation);
                                    sb2.append(", sensorRotation=");
                                    sb2.append(sensorRotation);
                                    sb2.append(", preferredRotation=");
                                    sb2.append(preferredRotation3);
                                    sb2.append("); user=");
                                    sb2.append(this.mUserRotation);
                                    sb2.append(" ");
                                    sb2.append(this.mUserRotationMode == 1 ? "USER_ROTATION_LOCKED" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                                    Flog.i(308, sb2.toString());
                                    switch (orientation) {
                                        case 0:
                                            if (isLandscapeOrSeascape(preferredRotation3)) {
                                                return preferredRotation3;
                                            }
                                            int i = this.mLandscapeRotation;
                                            return i;
                                        case 1:
                                            if (isAnyPortrait(preferredRotation3)) {
                                                return preferredRotation3;
                                            }
                                            int i2 = this.mPortraitRotation;
                                            return i2;
                                        case 6:
                                        case 11:
                                            if (isLandscapeOrSeascape(preferredRotation3)) {
                                                return preferredRotation3;
                                            }
                                            if (isLandscapeOrSeascape(lastRotation)) {
                                                return lastRotation;
                                            }
                                            int i3 = this.mLandscapeRotation;
                                            return i3;
                                        case 7:
                                        case 12:
                                            if (isAnyPortrait(preferredRotation3)) {
                                                return preferredRotation3;
                                            }
                                            if (isAnyPortrait(lastRotation)) {
                                                return lastRotation;
                                            }
                                            int i4 = this.mPortraitRotation;
                                            return i4;
                                        case 8:
                                            if (isLandscapeOrSeascape(preferredRotation3)) {
                                                return preferredRotation3;
                                            }
                                            int i5 = this.mSeascapeRotation;
                                            return i5;
                                        case 9:
                                            if (isAnyPortrait(preferredRotation3)) {
                                                return preferredRotation3;
                                            }
                                            int i6 = this.mUpsideDownRotation;
                                            return i6;
                                        default:
                                            if (preferredRotation3 >= 0) {
                                                return preferredRotation3;
                                            }
                                            int i7 = DEFAULT_ROTATION;
                                            return i7;
                                    }
                                }
                            }
                        }
                        if (this.mAllowAllRotations < 0) {
                            this.mAllowAllRotations = this.mContext.getResources().getBoolean(17956870) ? 1 : 0;
                        }
                        if (!(sensorRotation != 2 || this.mAllowAllRotations == 1 || orientation == 10)) {
                            if (orientation != 13) {
                                preferredRotation3 = lastRotation;
                            }
                        }
                        preferredRotation3 = sensorRotation;
                    }
                } else {
                    preferredRotation3 = this.mCarDockEnablesAccelerometer ? sensorRotation : this.mCarDockRotation;
                }
                StringBuilder sb22 = new StringBuilder();
                sb22.append("rotationForOrientationLw orientation=");
                sb22.append(orientation);
                sb22.append(", lastRotation=");
                sb22.append(lastRotation);
                sb22.append(", sensorRotation=");
                sb22.append(sensorRotation);
                sb22.append(", preferredRotation=");
                sb22.append(preferredRotation3);
                sb22.append("); user=");
                sb22.append(this.mUserRotation);
                sb22.append(" ");
                sb22.append(this.mUserRotationMode == 1 ? "USER_ROTATION_LOCKED" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                Flog.i(308, sb22.toString());
                switch (orientation) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 6:
                    case 11:
                        break;
                    case 7:
                    case 12:
                        break;
                    case 8:
                        break;
                    case 9:
                        break;
                }
            }
        } else {
            Flog.i(308, "rotationForOrientationLw DEFAULT_ROTATION " + DEFAULT_ROTATION);
            return DEFAULT_ROTATION;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:5:0x000c, code lost:
        return isAnyPortrait(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0011, code lost:
        return isLandscapeOrSeascape(r3);
     */
    public boolean rotationHasCompatibleMetricsLw(int orientation, int rotation) {
        switch (orientation) {
            case 0:
                break;
            case 1:
                break;
            default:
                switch (orientation) {
                    case 6:
                    case 8:
                        break;
                    case 7:
                    case 9:
                        break;
                    default:
                        return true;
                }
        }
    }

    public void setRotationLw(int rotation) {
        this.mOrientationListener.setCurrentRotation(rotation);
    }

    public boolean isRotationChoicePossible(int orientation) {
        if (this.mUserRotationMode != 1 || this.mForceDefaultOrientation) {
            return false;
        }
        if (this.mLidState == 1 && this.mLidOpenRotation >= 0) {
            return false;
        }
        if (this.mDockMode == 2 && !this.mCarDockEnablesAccelerometer) {
            return false;
        }
        if ((this.mDockMode == 1 || this.mDockMode == 3 || this.mDockMode == 4) && !this.mDeskDockEnablesAccelerometer) {
            return false;
        }
        if (this.mHdmiPlugged && this.mDemoHdmiRotationLock) {
            return false;
        }
        if ((this.mHdmiPlugged && this.mDockMode == 0 && this.mUndockedHdmiRotation >= 0) || this.mDemoRotationLock || this.mPersistentVrModeEnabled || !this.mSupportAutoRotation) {
            return false;
        }
        if (!(orientation == -1 || orientation == 2)) {
            switch (orientation) {
                case 11:
                case 12:
                case 13:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public boolean isValidRotationChoice(int orientation, int preferredRotation) {
        boolean z = true;
        if (orientation == -1 || orientation == 2) {
            if (preferredRotation < 0 || preferredRotation == this.mUpsideDownRotation) {
                z = false;
            }
            return z;
        }
        switch (orientation) {
            case 11:
                return isLandscapeOrSeascape(preferredRotation);
            case 12:
                if (preferredRotation != this.mPortraitRotation) {
                    z = false;
                }
                return z;
            case 13:
                if (preferredRotation < 0) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    private boolean isLandscapeOrSeascape(int rotation) {
        return rotation == this.mLandscapeRotation || rotation == this.mSeascapeRotation;
    }

    private boolean isAnyPortrait(int rotation) {
        return rotation == this.mPortraitRotation || rotation == this.mUpsideDownRotation;
    }

    public int getUserRotationMode() {
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "accelerometer_rotation", 0, -2) != 0) {
            return 0;
        }
        return 1;
    }

    public void setUserRotationMode(int mode, int rot) {
        ContentResolver res = this.mContext.getContentResolver();
        if (mode == 1) {
            Settings.System.putIntForUser(res, "user_rotation", rot, -2);
            Settings.System.putIntForUser(res, "accelerometer_rotation", 0, -2);
            return;
        }
        Settings.System.putIntForUser(res, "accelerometer_rotation", 1, -2);
    }

    public void setSafeMode(boolean safeMode) {
        this.mSafeMode = safeMode;
        if (safeMode) {
            performHapticFeedbackLw(null, 10001, true);
        }
    }

    static long[] getLongIntArray(Resources r, int resid) {
        return ArrayUtils.convertToLongArray(r.getIntArray(resid));
    }

    private void bindKeyguard() {
        synchronized (this.mLock) {
            if (!this.mKeyguardBound) {
                this.mKeyguardBound = true;
                this.mKeyguardDelegate.bindService(this.mContext);
            }
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
        this.mSystemGestures.systemReady();
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
                    int theme;
                    if (PhoneWindowManager.this.mBootMsgDialog == null) {
                        if (PhoneWindowManager.this.mContext.getPackageManager().hasSystemFeature("android.software.leanback")) {
                            theme = 16974829;
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
                            PhoneWindowManager.this.mBootMsgDialog.setTitle(17039587);
                        } else {
                            PhoneWindowManager.this.mBootMsgDialog.setTitle(17039579);
                        }
                        PhoneWindowManager.this.mBootMsgDialog.setProgressStyle(0);
                        PhoneWindowManager.this.mBootMsgDialog.setIndeterminate(true);
                        PhoneWindowManager.this.mBootMsgDialog.getWindow().setType(2021);
                        PhoneWindowManager.this.mBootMsgDialog.getWindow().addFlags(LightsManager.LIGHT_ID_AUTOCUSTOMBACKLIGHT);
                        PhoneWindowManager.this.mBootMsgDialog.getWindow().setDimAmount(1.0f);
                        WindowManager.LayoutParams lp = PhoneWindowManager.this.mBootMsgDialog.getWindow().getAttributes();
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

    public void requestUserActivityNotification() {
        if (!this.mNotifyUserActivity && !this.mHandler.hasMessages(29)) {
            this.mNotifyUserActivity = true;
        }
    }

    public void userActivity() {
        synchronized (this.mScreenLockTimeout) {
            if (this.mLockScreenTimerActive) {
                this.mHandler.removeCallbacks(this.mScreenLockTimeout);
                this.mHandler.postDelayed(this.mScreenLockTimeout, (long) this.mLockScreenTimeout);
            }
        }
        if (this.mAwake && this.mNotifyUserActivity) {
            this.mHandler.sendEmptyMessageDelayed(29, 200);
            this.mNotifyUserActivity = false;
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
            boolean enable = this.mAllowLockscreenWhenOn && this.mAwake && this.mKeyguardDelegate != null && this.mKeyguardDelegate.isSecure(this.mCurrentUserId);
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

    /* access modifiers changed from: private */
    public void updateDreamingSleepToken(boolean acquire) {
        if (acquire) {
            if (this.mDreamingSleepToken == null) {
                this.mDreamingSleepToken = this.mActivityManagerInternal.acquireSleepToken("Dream", 0);
            }
        } else if (this.mDreamingSleepToken != null) {
            this.mDreamingSleepToken.release();
            this.mDreamingSleepToken = null;
        }
    }

    private void updateScreenOffSleepToken(boolean acquire) {
        if (acquire) {
            if (this.mScreenOffSleepToken == null) {
                this.mScreenOffSleepToken = this.mActivityManagerInternal.acquireSleepToken("ScreenOff", 0);
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
            goToSleep(SystemClock.uptimeMillis(), 3, 1);
        } else if (this.mLidState == 0 && this.mLidControlsScreenLock) {
            this.mWindowManagerFuncs.lockDeviceNow();
        }
        synchronized (this.mLock) {
            updateWakeGestureListenerLp();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateUiMode() {
        if (this.mUiModeManager == null) {
            this.mUiModeManager = IUiModeManager.Stub.asInterface(ServiceManager.getService("uimode"));
        }
        try {
            this.mUiMode = this.mUiModeManager.getCurrentModeType();
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void updateRotation(boolean alwaysSendConfiguration) {
        try {
            this.mWindowManager.updateRotation(alwaysSendConfiguration, false);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) {
        try {
            this.mWindowManager.updateRotation(alwaysSendConfiguration, forceRelayout);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public Intent createHomeDockIntent() {
        Intent intent = null;
        if (this.mUiMode == 3) {
            if (this.mEnableCarDockHomeCapture) {
                intent = this.mCarDockIntent;
            }
        } else if (this.mUiMode != 2) {
            if (this.mUiMode == 6 && (this.mDockMode == 1 || this.mDockMode == 4 || this.mDockMode == 3)) {
                intent = this.mDeskDockIntent;
            } else if (this.mUiMode == 7) {
                intent = this.mVrHeadsetHomeIntent;
            }
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

    /* access modifiers changed from: package-private */
    public void startDockOrHome(boolean fromHomeKey, boolean awakenFromDreams) {
        Intent intent;
        try {
            ActivityManager.getService().stopAppSwitches();
        } catch (RemoteException e) {
        }
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_HOME_KEY);
        if (awakenFromDreams) {
            awakenDreams();
        }
        Intent dock = createHomeDockIntent();
        if (dock != null) {
            if (fromHomeKey) {
                try {
                    dock.putExtra("android.intent.extra.FROM_HOME_KEY", fromHomeKey);
                } catch (ActivityNotFoundException e2) {
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

    /* access modifiers changed from: package-private */
    public boolean goHome() {
        if (!isUserSetupComplete()) {
            Slog.i(TAG, "Not going home because user setup is in progress.");
            return false;
        }
        try {
            if (SystemProperties.getInt("persist.sys.uts-test-mode", 0) == 1) {
                Log.d(TAG, "UTS-TEST-MODE");
            } else {
                ActivityManager.getService().stopAppSwitches();
                sendCloseSystemWindows();
                Intent dock = createHomeDockIntent();
                if (dock != null) {
                    if (ActivityManager.getService().startActivityAsUser(null, null, dock, dock.resolveTypeIfNeeded(this.mContext.getContentResolver()), null, null, 0, 1, null, null, -2) == 1) {
                        return false;
                    }
                }
            }
            if (ActivityManager.getService().startActivityAsUser(null, null, this.mHomeIntent, this.mHomeIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), null, null, 0, 1, null, null, -2) == 1) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
        }
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
        return Settings.Global.getInt(this.mContext.getContentResolver(), "theater_mode_on", 0) == 1;
    }

    public boolean performHapticFeedbackLw(WindowManagerPolicy.WindowState win, int effectId, boolean always) {
        String owningPackage;
        int owningUid;
        if (this.mVibrator == null) {
            this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        }
        if (this.mVibrator == null || this.mKeyguardDelegate == null || !this.mVibrator.hasVibrator()) {
            return false;
        }
        if ((Settings.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) == 0) && !always) {
            return false;
        }
        VibrationEffect effect = getVibrationEffect(effectId);
        if (effect == null) {
            return false;
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0025, code lost:
        return android.os.VibrationEffect.get(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002b, code lost:
        return android.os.VibrationEffect.get(5);
     */
    private VibrationEffect getVibrationEffect(int effectId) {
        long[] pattern;
        if (effectId != 10001) {
            switch (effectId) {
                case 0:
                    break;
                case 1:
                    break;
                default:
                    switch (effectId) {
                        case 3:
                        case 12:
                        case 15:
                        case 16:
                            break;
                        case 4:
                        case 6:
                            return VibrationEffect.get(2);
                        case 5:
                            pattern = this.mCalendarDateVibePattern;
                            break;
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 13:
                            return VibrationEffect.get(2, false);
                        case 14:
                            break;
                        case 17:
                            return VibrationEffect.get(1);
                        default:
                            return null;
                    }
            }
        } else {
            pattern = this.mSafeModeEnabledVibePattern;
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

    /* access modifiers changed from: private */
    public int updateSystemUiVisibilityLw() {
        WindowManagerPolicy.WindowState winCandidate;
        if (this.mFocusedWindow == null || this.mFocusedWindow.getWindowingMode() == 5) {
            winCandidate = this.mTopFullscreenOpaqueWindowState;
        } else {
            winCandidate = this.mFocusedWindow;
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
        WindowManagerPolicy.WindowState winCandidate2 = winCandidate;
        WindowManagerPolicy.WindowState win = winCandidate2;
        if (win.getAttrs().type == 3 && this.mLastStartingWindow != win) {
            this.mLastStartingWindow = win;
        }
        if ((win.getAttrs().privateFlags & 1024) != 0 && this.mKeyguardOccluded) {
            return 0;
        }
        int tmpVisibility = PolicyControl.getSystemUiVisibility(win, null) & (~this.mResettingSystemUiFlags) & (~this.mForceClearedSystemUiFlags);
        if (this.mForcingShowNavBar && win.getSurfaceLayer() < this.mForcingShowNavBarLayer) {
            tmpVisibility &= ~PolicyControl.adjustClearableFlags(win, 7);
        }
        int fullscreenVisibility = updateLightStatusBarLw(0, this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState);
        int dockedVisibility = updateLightStatusBarLw(0, this.mTopDockedOpaqueWindowState, this.mTopDockedOpaqueOrDimmingWindowState);
        this.mWindowManagerFuncs.getStackBounds(0, 2, this.mNonDockedStackBounds);
        boolean z = true;
        this.mWindowManagerFuncs.getStackBounds(3, 1, this.mDockedStackBounds);
        int visibility = updateSystemBarsLw(win, this.mLastSystemUiFlags, tmpVisibility);
        int visSysui = (win.getAttrs().privateFlags & Integer.MIN_VALUE) != 0 ? visibility | 2 : visibility;
        int diff = visSysui ^ this.mLastSystemUiFlags;
        int fullscreenDiff = fullscreenVisibility ^ this.mLastFullscreenStackSysUiFlags;
        int dockedDiff = dockedVisibility ^ this.mLastDockedStackSysUiFlags;
        if ((diff & 4096) == 0) {
            z = false;
        }
        this.mImmersiveStatusChanged = z;
        boolean needsMenu = win.getNeedsMenuLw(this.mTopFullscreenOpaqueWindowState);
        if (diff == 0 && fullscreenDiff == 0 && dockedDiff == 0 && this.mLastFocusNeedsMenu == needsMenu && this.mFocusedApp == win.getAppToken() && this.mLastNonDockedStackBounds.equals(this.mNonDockedStackBounds) && this.mLastDockedStackBounds.equals(this.mDockedStackBounds) && this.mLastHwNavColor == this.mHwNavColor) {
            return 0;
        }
        Pattern pattern = Pattern.compile("[0-9]++");
        Matcher matcherForWin = pattern.matcher(win.getAttrs().getTitle());
        if (!matcherForWin.find() && diff != 0) {
            Flog.i(303, "Policy setSystemUiVisibility, vis=" + Integer.toHexString(visibility) + ",lastVis=" + Integer.toHexString(this.mLastSystemUiFlags) + ",diff=" + Integer.toHexString(diff) + ",win=" + win.toString());
        }
        this.mLastSystemUiFlags = visSysui;
        this.mLastHwNavColor = this.mHwNavColor;
        this.mLastFullscreenStackSysUiFlags = fullscreenVisibility;
        this.mLastDockedStackSysUiFlags = dockedVisibility;
        this.mLastFocusNeedsMenu = needsMenu;
        this.mFocusedApp = win.getAppToken();
        final Rect fullscreenStackBounds = new Rect(this.mNonDockedStackBounds);
        final Rect dockedStackBounds = new Rect(this.mDockedStackBounds);
        WindowManagerPolicy.WindowState windowState = winCandidate2;
        Handler handler = this.mHandler;
        Matcher matcher = matcherForWin;
        Pattern pattern2 = pattern;
        final int i = visibility;
        boolean needsMenu2 = needsMenu;
        final int i2 = fullscreenVisibility;
        int diff2 = diff;
        final int diff3 = dockedVisibility;
        int i3 = visSysui;
        final WindowManagerPolicy.WindowState windowState2 = win;
        int i4 = visibility;
        final boolean z2 = needsMenu2;
        AnonymousClass26 r0 = new Runnable() {
            public void run() {
                StatusBarManagerInternal statusbar = PhoneWindowManager.this.getStatusBarManagerInternal();
                if (statusbar != null) {
                    int result = i;
                    if (PhoneWindowManager.this.mHwNavColor) {
                        result = i | 16;
                        Flog.i(303, "Policy setSystemUiVisibility add SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR ,visibility=" + Integer.toHexString(i) + ",result=" + Integer.toHexString(result));
                    }
                    statusbar.setSystemUiVisibility(result, i2, diff3, -1, fullscreenStackBounds, dockedStackBounds, windowState2.toString());
                    statusbar.topAppWindowChanged(z2);
                }
            }
        };
        handler.post(r0);
        if (!(win.getAttrs().type == 3 || this.mFocusedWindow == null)) {
            updateSystemUiColorLw(win);
        }
        return diff2;
    }

    public void updateSystemUiColorLw(WindowManagerPolicy.WindowState win) {
    }

    private int updateLightStatusBarLw(int vis, WindowManagerPolicy.WindowState opaque, WindowManagerPolicy.WindowState opaqueOrDimming) {
        boolean onKeyguard = isStatusBarKeyguard() && !this.mKeyguardOccluded;
        WindowManagerPolicy.WindowState statusColorWin = onKeyguard ? this.mStatusBar : opaqueOrDimming;
        if (statusColorWin != null && (statusColorWin == opaque || onKeyguard)) {
            return (vis & -8193) | (PolicyControl.getSystemUiVisibility(statusColorWin, null) & 8192);
        }
        if (statusColorWin == null || !statusColorWin.isDimming()) {
            return vis;
        }
        return vis & -8193;
    }

    @VisibleForTesting
    static WindowManagerPolicy.WindowState chooseNavigationColorWindowLw(WindowManagerPolicy.WindowState opaque, WindowManagerPolicy.WindowState opaqueOrDimming, WindowManagerPolicy.WindowState imeWindow, int navBarPosition) {
        WindowManagerPolicy.WindowState windowState = null;
        boolean imeWindowCanNavColorWindow = imeWindow != null && imeWindow.isVisibleLw() && navBarPosition == 4 && (PolicyControl.getWindowFlags(imeWindow, null) & Integer.MIN_VALUE) != 0;
        if (opaque != null && opaqueOrDimming == opaque) {
            return imeWindowCanNavColorWindow ? imeWindow : opaque;
        } else if (opaqueOrDimming == null || !opaqueOrDimming.isDimming()) {
            if (imeWindowCanNavColorWindow) {
                windowState = imeWindow;
            }
            return windowState;
        } else if (imeWindowCanNavColorWindow && WindowManager.LayoutParams.mayUseInputMethod(PolicyControl.getWindowFlags(opaqueOrDimming, null))) {
            return imeWindow;
        } else {
            return opaqueOrDimming;
        }
    }

    @VisibleForTesting
    static int updateLightNavigationBarLw(int vis, WindowManagerPolicy.WindowState opaque, WindowManagerPolicy.WindowState opaqueOrDimming, WindowManagerPolicy.WindowState imeWindow, WindowManagerPolicy.WindowState navColorWin) {
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

    /* JADX WARNING: Removed duplicated region for block: B:103:0x015a  */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x018c  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x018e  */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0195  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0197  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00d8  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00df  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00ee  */
    private int updateSystemBarsLw(WindowManagerPolicy.WindowState win, int oldVis, int vis) {
        WindowManagerPolicy.WindowState fullscreenTransWin;
        boolean hideStatusBarWM;
        boolean hideNavBarSysui;
        boolean transientStatusBarAllowed;
        boolean pendingPanic;
        boolean denyTransientStatus;
        boolean immersiveSticky;
        boolean oldImmersiveMode;
        int i = oldVis;
        boolean dockedStackVisible = this.mWindowManagerInternal.isStackVisible(3);
        boolean freeformStackVisible = HwFreeFormUtils.isFreeFormEnable() ? false : this.mWindowManagerInternal.isStackVisible(5);
        boolean resizing = this.mWindowManagerInternal.isDockedDividerResizing();
        this.mForceShowSystemBars = dockedStackVisible || freeformStackVisible || resizing;
        boolean forceOpaqueStatusBar = this.mForceShowSystemBars && !this.mForceStatusBarFromKeyguard;
        if (!isStatusBarKeyguard() || this.mKeyguardOccluded) {
            fullscreenTransWin = this.mTopFullscreenOpaqueWindowState;
        } else {
            fullscreenTransWin = this.mStatusBar;
        }
        int vis2 = this.mNavigationBarController.applyTranslucentFlagLw(fullscreenTransWin, this.mStatusBarController.applyTranslucentFlagLw(fullscreenTransWin, vis, i), i);
        int dockedVis = this.mStatusBarController.applyTranslucentFlagLw(this.mTopDockedOpaqueWindowState, 0, 0);
        boolean fullscreenDrawsStatusBarBackground = drawsStatusBarBackground(vis2, this.mTopFullscreenOpaqueWindowState);
        boolean dockedDrawsStatusBarBackground = drawsStatusBarBackground(dockedVis, this.mTopDockedOpaqueWindowState);
        boolean statusBarHasFocus = win.getAttrs().type == 2000;
        if (statusBarHasFocus && !isStatusBarKeyguard()) {
            int flags = 14342;
            if (this.mKeyguardOccluded) {
                flags = 14342 | -1073741824;
            }
            int flags2 = flags;
            vis2 = ((~flags2) & vis2) | (i & flags2);
        }
        if (fullscreenDrawsStatusBarBackground && dockedDrawsStatusBarBackground) {
            vis2 = (vis2 | 8) & -1073741825;
        } else if ((!areTranslucentBarsAllowed() && fullscreenTransWin != this.mStatusBar) || forceOpaqueStatusBar) {
            vis2 &= -1073741833;
        }
        int vis3 = configureNavBarOpacity(vis2, dockedStackVisible, freeformStackVisible, resizing);
        boolean immersiveSticky2 = (vis3 & 4096) != 0;
        if (this.mTopFullscreenOpaqueWindowState != null) {
            boolean z = dockedStackVisible;
            if ((PolicyControl.getWindowFlags(this.mTopFullscreenOpaqueWindowState, null) & 1024) != 0) {
                hideStatusBarWM = true;
                boolean hideStatusBarSysui = (vis3 & 4) == 0;
                hideNavBarSysui = (vis3 & 2) == 0;
                boolean z2 = freeformStackVisible;
                if (this.mCust == null) {
                    boolean z3 = resizing;
                    boolean z4 = forceOpaqueStatusBar;
                    vis3 = this.mCust.updateSystemBarsLw(this.mContext, this.mFocusedWindow, vis3);
                } else {
                    boolean z5 = forceOpaqueStatusBar;
                }
                transientStatusBarAllowed = this.mStatusBar == null && (statusBarHasFocus || (!this.mForceShowSystemBars && ((hideStatusBarWM && !this.mImmersiveStatusChanged) || (hideStatusBarSysui && immersiveSticky2))));
                boolean transientNavBarAllowed = this.mNavigationBar == null && ((!this.mForceShowSystemBars && hideNavBarSysui && hideStatusBarWM) || (!this.mForceShowSystemBars && hideNavBarSysui && immersiveSticky2));
                boolean z6 = immersiveSticky2;
                int i2 = dockedVis;
                boolean z7 = fullscreenDrawsStatusBarBackground;
                pendingPanic = this.mPendingPanicGestureUptime == 0 && SystemClock.uptimeMillis() - this.mPendingPanicGestureUptime <= 30000;
                if (pendingPanic && hideNavBarSysui && !isStatusBarKeyguard() && this.mKeyguardDrawComplete) {
                    this.mPendingPanicGestureUptime = 0;
                    this.mStatusBarController.showTransient();
                    if (!isNavBarEmpty(vis3)) {
                        this.mNavigationBarController.showTransient();
                    }
                }
                denyTransientStatus = !this.mStatusBarController.isTransientShowRequested() && !transientStatusBarAllowed && hideStatusBarSysui;
                boolean denyTransientNav = !this.mNavigationBarController.isTransientShowRequested() && !transientNavBarAllowed;
                if (denyTransientStatus || denyTransientNav || this.mForceShowSystemBars) {
                    clearClearableFlagsLw();
                    vis3 &= -8;
                }
                boolean immersive = (vis3 & 2048) == 0;
                boolean z8 = hideStatusBarWM;
                immersiveSticky = (vis3 & 4096) == 0;
                boolean navAllowedHidden = !immersive || immersiveSticky;
                if (hideNavBarSysui || navAllowedHidden) {
                    boolean z9 = pendingPanic;
                } else {
                    boolean z10 = immersiveSticky;
                    boolean z11 = pendingPanic;
                    if (getWindowLayerLw(win) > getWindowLayerFromTypeLw(2022)) {
                        vis3 &= -3;
                    }
                }
                int vis4 = this.mStatusBarController.updateVisibilityLw(transientStatusBarAllowed, i, vis3);
                oldImmersiveMode = isImmersiveMode(i);
                boolean newImmersiveMode = isImmersiveMode(vis4);
                setNaviImmersiveMode(newImmersiveMode);
                if (win != null || oldImmersiveMode == newImmersiveMode) {
                    boolean z12 = oldImmersiveMode;
                    boolean z13 = denyTransientStatus;
                    WindowManagerPolicy.WindowState windowState = fullscreenTransWin;
                } else {
                    boolean z14 = transientStatusBarAllowed;
                    boolean z15 = oldImmersiveMode;
                    boolean z16 = denyTransientStatus;
                    WindowManagerPolicy.WindowState windowState2 = fullscreenTransWin;
                    this.mImmersiveModeConfirmation.immersiveModeChangedLw(win.getOwningPackage(), newImmersiveMode, isUserSetupComplete(), isNavBarEmpty(win.getSystemUiVisibility()));
                }
                int vis5 = this.mNavigationBarController.updateVisibilityLw(transientNavBarAllowed, i, vis4);
                WindowManagerPolicy.WindowState navColorWin = chooseNavigationColorWindowLw(this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState, this.mWindowManagerFuncs.getInputMethodWindowLw(), this.mNavigationBarPosition);
                int vis6 = updateLightNavigationBarLw(vis5, this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState, this.mWindowManagerFuncs.getInputMethodWindowLw(), navColorWin);
                if (win == null && (win.getAttrs().hwFlags & 16) != 0 && navColorWin == this.mTopFullscreenOpaqueWindowState) {
                    this.mHwNavColor = true;
                } else {
                    this.mHwNavColor = false;
                }
                return vis6;
            }
        }
        hideStatusBarWM = false;
        if ((vis3 & 4) == 0) {
        }
        if ((vis3 & 2) == 0) {
        }
        boolean z22 = freeformStackVisible;
        if (this.mCust == null) {
        }
        if (this.mStatusBar == null) {
        }
        if (this.mNavigationBar == null) {
        }
        boolean z62 = immersiveSticky2;
        int i22 = dockedVis;
        boolean z72 = fullscreenDrawsStatusBarBackground;
        if (this.mPendingPanicGestureUptime == 0) {
        }
        this.mPendingPanicGestureUptime = 0;
        this.mStatusBarController.showTransient();
        if (!isNavBarEmpty(vis3)) {
        }
        if (!this.mStatusBarController.isTransientShowRequested()) {
        }
        if (!this.mNavigationBarController.isTransientShowRequested()) {
        }
        clearClearableFlagsLw();
        vis3 &= -8;
        if ((vis3 & 2048) == 0) {
        }
        boolean z82 = hideStatusBarWM;
        if ((vis3 & 4096) == 0) {
        }
        if (!immersive) {
        }
        if (hideNavBarSysui) {
        }
        boolean z92 = pendingPanic;
        int vis42 = this.mStatusBarController.updateVisibilityLw(transientStatusBarAllowed, i, vis3);
        oldImmersiveMode = isImmersiveMode(i);
        boolean newImmersiveMode2 = isImmersiveMode(vis42);
        setNaviImmersiveMode(newImmersiveMode2);
        if (win != null) {
        }
        boolean z122 = oldImmersiveMode;
        boolean z132 = denyTransientStatus;
        WindowManagerPolicy.WindowState windowState3 = fullscreenTransWin;
        int vis52 = this.mNavigationBarController.updateVisibilityLw(transientNavBarAllowed, i, vis42);
        WindowManagerPolicy.WindowState navColorWin2 = chooseNavigationColorWindowLw(this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState, this.mWindowManagerFuncs.getInputMethodWindowLw(), this.mNavigationBarPosition);
        int vis62 = updateLightNavigationBarLw(vis52, this.mTopFullscreenOpaqueWindowState, this.mTopFullscreenOpaqueOrDimmingWindowState, this.mWindowManagerFuncs.getInputMethodWindowLw(), navColorWin2);
        if (win == null) {
        }
        this.mHwNavColor = false;
        return vis62;
    }

    private boolean drawsStatusBarBackground(int vis, WindowManagerPolicy.WindowState win) {
        if (!this.mStatusBarController.isTransparentAllowed(win)) {
            return false;
        }
        boolean z = true;
        if (win == null) {
            return true;
        }
        boolean drawsSystemBars = (win.getAttrs().flags & Integer.MIN_VALUE) != 0;
        if (!((win.getAttrs().privateFlags & 131072) != 0) && (!drawsSystemBars || (1073741824 & vis) != 0)) {
            z = false;
        }
        return z;
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
        if (!areTranslucentBarsAllowed()) {
            return visibility & HwBootFail.STAGE_BOOT_SUCCESS;
        }
        return visibility;
    }

    private int setNavBarOpaqueFlag(int visibility) {
        int i = 2147450879 & visibility;
        int visibility2 = i;
        return i;
    }

    private int setNavBarTranslucentFlag(int visibility) {
        int i = Integer.MIN_VALUE | (visibility & -32769);
        int visibility2 = i;
        return i;
    }

    private void clearClearableFlagsLw() {
        int newVal = this.mResettingSystemUiFlags | 7;
        if (newVal != this.mResettingSystemUiFlags) {
            this.mResettingSystemUiFlags = newVal;
            this.mWindowManagerFuncs.reevaluateStatusBarVisibility();
        }
    }

    private boolean isImmersiveMode(int vis) {
        return (this.mNavigationBar == null || (vis & 2) == 0 || (vis & 6144) == 0 || !canHideNavigationBar()) ? false : true;
    }

    /* access modifiers changed from: private */
    public static boolean isNavBarEmpty(int systemUiFlags) {
        return (systemUiFlags & 23068672) == 23068672;
    }

    private boolean areTranslucentBarsAllowed() {
        return this.mTranslucentDecorEnabled;
    }

    public boolean hasNavigationBar() {
        return this.mHasNavigationBar;
    }

    public void setLastInputMethodWindowLw(WindowManagerPolicy.WindowState ime, WindowManagerPolicy.WindowState target) {
        this.mLastInputMethodWindow = ime;
        this.mLastInputMethodTargetWindow = target;
    }

    public void setDismissImeOnBackKeyPressed(boolean newValue) {
        this.mDismissImeOnBackKeyPressed = newValue;
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

    public boolean shouldRotateSeamlessly(int oldRotation, int newRotation) {
        if (oldRotation == this.mUpsideDownRotation || newRotation == this.mUpsideDownRotation || !this.mNavigationBarCanMove) {
            return false;
        }
        int delta = newRotation - oldRotation;
        if (delta < 0) {
            delta += 4;
        }
        notifyRotate(oldRotation, newRotation);
        if (delta == 2) {
            return false;
        }
        WindowManagerPolicy.WindowState w = this.mTopFullscreenOpaqueWindowState;
        if (w == this.mFocusedWindow && w != null && !w.isAnimatingLw() && (w.getAttrs().rotationAnimation == 2 || w.getAttrs().rotationAnimation == 3)) {
            return true;
        }
        return false;
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, this.mLastSystemUiFlags);
        proto.write(1159641169922L, this.mUserRotationMode);
        proto.write(1159641169923L, this.mUserRotation);
        proto.write(1159641169924L, this.mCurrentAppOrientation);
        proto.write(1133871366149L, this.mScreenOnFully);
        proto.write(1133871366150L, this.mKeyguardDrawComplete);
        proto.write(1133871366151L, this.mWindowManagerDrawComplete);
        if (this.mFocusedApp != null) {
            proto.write(1138166333448L, this.mFocusedApp.toString());
        }
        if (this.mFocusedWindow != null) {
            this.mFocusedWindow.writeIdentifierToProto(proto, 1146756268041L);
        }
        if (this.mTopFullscreenOpaqueWindowState != null) {
            this.mTopFullscreenOpaqueWindowState.writeIdentifierToProto(proto, 1146756268042L);
        }
        if (this.mTopFullscreenOpaqueOrDimmingWindowState != null) {
            this.mTopFullscreenOpaqueOrDimmingWindowState.writeIdentifierToProto(proto, 1146756268043L);
        }
        proto.write(1133871366156L, this.mKeyguardOccluded);
        proto.write(1133871366157L, this.mKeyguardOccludedChanged);
        proto.write(1133871366158L, this.mPendingKeyguardOccluded);
        proto.write(1133871366159L, this.mForceStatusBar);
        proto.write(1133871366160L, this.mForceStatusBarFromKeyguard);
        this.mStatusBarController.writeToProto(proto, 1146756268049L);
        this.mNavigationBarController.writeToProto(proto, 1146756268050L);
        if (this.mOrientationListener != null) {
            this.mOrientationListener.writeToProto(proto, 1146756268051L);
        }
        if (this.mKeyguardDelegate != null) {
            this.mKeyguardDelegate.writeToProto(proto, 1146756268052L);
        }
        proto.end(token);
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
        pw.print(WindowManagerPolicy.WindowManagerFuncs.lidStateToString(this.mLidState));
        pw.print(" mLidOpenRotation=");
        pw.println(Surface.rotationToString(this.mLidOpenRotation));
        pw.print(prefix);
        pw.print("mCameraLensCoverState=");
        pw.print(WindowManagerPolicy.WindowManagerFuncs.cameraLensStateToString(this.mCameraLensCoverState));
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
        pw.print(this.mSupportAutoRotation);
        pw.print(" mOrientationSensorEnabled=");
        pw.println(this.mOrientationSensorEnabled);
        pw.print(prefix);
        pw.print("mUiMode=");
        pw.print(Configuration.uiModeToString(this.mUiMode));
        pw.print(" mDockMode=");
        pw.println(Intent.dockStateToString(this.mDockMode));
        pw.print(prefix);
        pw.print("mEnableCarDockHomeCapture=");
        pw.print(this.mEnableCarDockHomeCapture);
        pw.print(" mCarDockRotation=");
        pw.print(Surface.rotationToString(this.mCarDockRotation));
        pw.print(" mDeskDockRotation=");
        pw.println(Surface.rotationToString(this.mDeskDockRotation));
        pw.print(prefix);
        pw.print("mUserRotationMode=");
        pw.print(WindowManagerPolicy.userRotationModeToString(this.mUserRotationMode));
        pw.print(" mUserRotation=");
        pw.print(Surface.rotationToString(this.mUserRotation));
        pw.print(" mAllowAllRotations=");
        pw.println(allowAllRotationsToString(this.mAllowAllRotations));
        pw.print(prefix);
        pw.print("mCurrentAppOrientation=");
        pw.println(ActivityInfo.screenOrientationToString(this.mCurrentAppOrientation));
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
        pw.print(prefix);
        pw.print("mLidControlsSleep=");
        pw.println(this.mLidControlsSleep);
        pw.print(prefix);
        pw.print("mLongPressOnBackBehavior=");
        pw.println(longPressOnBackBehaviorToString(this.mLongPressOnBackBehavior));
        pw.print(prefix);
        pw.print("mLongPressOnHomeBehavior=");
        pw.println(longPressOnHomeBehaviorToString(this.mLongPressOnHomeBehavior));
        pw.print(prefix);
        pw.print("mDoubleTapOnHomeBehavior=");
        pw.println(doubleTapOnHomeBehaviorToString(this.mDoubleTapOnHomeBehavior));
        pw.print(prefix);
        pw.print("mShortPressOnPowerBehavior=");
        pw.println(shortPressOnPowerBehaviorToString(this.mShortPressOnPowerBehavior));
        pw.print(prefix);
        pw.print("mLongPressOnPowerBehavior=");
        pw.println(longPressOnPowerBehaviorToString(this.mLongPressOnPowerBehavior));
        pw.print(prefix);
        pw.print("mVeryLongPressOnPowerBehavior=");
        pw.println(veryLongPressOnPowerBehaviorToString(this.mVeryLongPressOnPowerBehavior));
        pw.print(prefix);
        pw.print("mDoublePressOnPowerBehavior=");
        pw.println(multiPressOnPowerBehaviorToString(this.mDoublePressOnPowerBehavior));
        pw.print(prefix);
        pw.print("mTriplePressOnPowerBehavior=");
        pw.println(multiPressOnPowerBehaviorToString(this.mTriplePressOnPowerBehavior));
        pw.print(prefix);
        pw.print("mShortPressOnSleepBehavior=");
        pw.println(shortPressOnSleepBehaviorToString(this.mShortPressOnSleepBehavior));
        pw.print(prefix);
        pw.print("mShortPressOnWindowBehavior=");
        pw.println(shortPressOnWindowBehaviorToString(this.mShortPressOnWindowBehavior));
        pw.print(prefix);
        pw.print("mAllowStartActivityForLongPressOnPowerDuringSetup=");
        pw.println(this.mAllowStartActivityForLongPressOnPowerDuringSetup);
        pw.print(prefix);
        pw.print("mHasSoftInput=");
        pw.print(this.mHasSoftInput);
        pw.print(" mDismissImeOnBackKeyPressed=");
        pw.println(this.mDismissImeOnBackKeyPressed);
        pw.print(prefix);
        pw.print("mIncallPowerBehavior=");
        pw.print(incallPowerBehaviorToString(this.mIncallPowerBehavior));
        pw.print(" mIncallBackBehavior=");
        pw.print(incallBackBehaviorToString(this.mIncallBackBehavior));
        pw.print(" mEndcallBehavior=");
        pw.println(endcallBehaviorToString(this.mEndcallBehavior));
        pw.print(prefix);
        pw.print("mHomePressed=");
        pw.println(this.mHomePressed);
        pw.print(prefix);
        pw.print("mAwake=");
        pw.print(this.mAwake);
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
        pw.print(prefix);
        pw.print("mKeyguardOccludedChanged=");
        pw.print(this.mKeyguardOccludedChanged);
        pw.print(" mPendingKeyguardOccluded=");
        pw.println(this.mPendingKeyguardOccluded);
        pw.print(prefix);
        pw.print("mForceStatusBar=");
        pw.print(this.mForceStatusBar);
        pw.print(" mForceStatusBarFromKeyguard=");
        pw.println(this.mForceStatusBarFromKeyguard);
        pw.print(prefix);
        pw.print("mAllowLockscreenWhenOn=");
        pw.print(this.mAllowLockscreenWhenOn);
        pw.print(" mLockScreenTimeout=");
        pw.print(this.mLockScreenTimeout);
        pw.print(" mLockScreenTimerActive=");
        pw.println(this.mLockScreenTimerActive);
        pw.print(prefix);
        pw.print("mLandscapeRotation=");
        pw.print(Surface.rotationToString(this.mLandscapeRotation));
        pw.print(" mSeascapeRotation=");
        pw.println(Surface.rotationToString(this.mSeascapeRotation));
        pw.print(prefix);
        pw.print("mPortraitRotation=");
        pw.print(Surface.rotationToString(this.mPortraitRotation));
        pw.print(" mUpsideDownRotation=");
        pw.println(Surface.rotationToString(this.mUpsideDownRotation));
        pw.print(prefix);
        pw.print("mDemoHdmiRotation=");
        pw.print(Surface.rotationToString(this.mDemoHdmiRotation));
        pw.print(" mDemoHdmiRotationLock=");
        pw.println(this.mDemoHdmiRotationLock);
        pw.print(prefix);
        pw.print("mUndockedHdmiRotation=");
        pw.println(Surface.rotationToString(this.mUndockedHdmiRotation));
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
        pw.print(prefix);
        pw.println("Looper state:");
        Looper looper = this.mHandler.getLooper();
        PrintWriterPrinter printWriterPrinter = new PrintWriterPrinter(pw);
        looper.dump(printWriterPrinter, prefix + "  ");
    }

    private static String allowAllRotationsToString(int allowAll) {
        switch (allowAll) {
            case -1:
                return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            case 0:
                return "false";
            case 1:
                return "true";
            default:
                return Integer.toString(allowAll);
        }
    }

    private static String endcallBehaviorToString(int behavior) {
        StringBuilder sb = new StringBuilder();
        if ((behavior & 1) != 0) {
            sb.append("home|");
        }
        if ((behavior & 2) != 0) {
            sb.append("sleep|");
        }
        int N = sb.length();
        if (N == 0) {
            return "<nothing>";
        }
        return sb.substring(0, N - 1);
    }

    private static String incallPowerBehaviorToString(int behavior) {
        if ((behavior & 2) != 0) {
            return "hangup";
        }
        return "sleep";
    }

    private static String incallBackBehaviorToString(int behavior) {
        if ((behavior & 1) != 0) {
            return "hangup";
        }
        return "<nothing>";
    }

    private static String longPressOnBackBehaviorToString(int behavior) {
        switch (behavior) {
            case 0:
                return "LONG_PRESS_BACK_NOTHING";
            case 1:
                return "LONG_PRESS_BACK_GO_TO_VOICE_ASSIST";
            default:
                return Integer.toString(behavior);
        }
    }

    private static String longPressOnHomeBehaviorToString(int behavior) {
        switch (behavior) {
            case 0:
                return "LONG_PRESS_HOME_NOTHING";
            case 1:
                return "LONG_PRESS_HOME_ALL_APPS";
            case 2:
                return "LONG_PRESS_HOME_ASSIST";
            default:
                return Integer.toString(behavior);
        }
    }

    private static String doubleTapOnHomeBehaviorToString(int behavior) {
        switch (behavior) {
            case 0:
                return "DOUBLE_TAP_HOME_NOTHING";
            case 1:
                return "DOUBLE_TAP_HOME_RECENT_SYSTEM_UI";
            default:
                return Integer.toString(behavior);
        }
    }

    private static String shortPressOnPowerBehaviorToString(int behavior) {
        switch (behavior) {
            case 0:
                return "SHORT_PRESS_POWER_NOTHING";
            case 1:
                return "SHORT_PRESS_POWER_GO_TO_SLEEP";
            case 2:
                return "SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP";
            case 3:
                return "SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP_AND_GO_HOME";
            case 4:
                return "SHORT_PRESS_POWER_GO_HOME";
            case 5:
                return "SHORT_PRESS_POWER_CLOSE_IME_OR_GO_HOME";
            default:
                return Integer.toString(behavior);
        }
    }

    private static String longPressOnPowerBehaviorToString(int behavior) {
        switch (behavior) {
            case 0:
                return "LONG_PRESS_POWER_NOTHING";
            case 1:
                return "LONG_PRESS_POWER_GLOBAL_ACTIONS";
            case 2:
                return "LONG_PRESS_POWER_SHUT_OFF";
            case 3:
                return "LONG_PRESS_POWER_SHUT_OFF_NO_CONFIRM";
            default:
                return Integer.toString(behavior);
        }
    }

    private static String veryLongPressOnPowerBehaviorToString(int behavior) {
        switch (behavior) {
            case 0:
                return "VERY_LONG_PRESS_POWER_NOTHING";
            case 1:
                return "VERY_LONG_PRESS_POWER_GLOBAL_ACTIONS";
            default:
                return Integer.toString(behavior);
        }
    }

    private static String multiPressOnPowerBehaviorToString(int behavior) {
        switch (behavior) {
            case 0:
                return "MULTI_PRESS_POWER_NOTHING";
            case 1:
                return "MULTI_PRESS_POWER_THEATER_MODE";
            case 2:
                return "MULTI_PRESS_POWER_BRIGHTNESS_BOOST";
            default:
                return Integer.toString(behavior);
        }
    }

    private static String shortPressOnSleepBehaviorToString(int behavior) {
        switch (behavior) {
            case 0:
                return "SHORT_PRESS_SLEEP_GO_TO_SLEEP";
            case 1:
                return "SHORT_PRESS_SLEEP_GO_TO_SLEEP_AND_GO_HOME";
            default:
                return Integer.toString(behavior);
        }
    }

    private static String shortPressOnWindowBehaviorToString(int behavior) {
        switch (behavior) {
            case 0:
                return "SHORT_PRESS_WINDOW_NOTHING";
            case 1:
                return "SHORT_PRESS_WINDOW_PICTURE_IN_PICTURE";
            default:
                return Integer.toString(behavior);
        }
    }

    public void onLockTaskStateChangedLw(int lockTaskState) {
        this.mImmersiveModeConfirmation.onLockTaskModeChangedLw(lockTaskState);
    }

    public boolean setAodShowing(boolean aodShowing) {
        if (this.mAodShowing == aodShowing) {
            return false;
        }
        this.mAodShowing = aodShowing;
        return true;
    }

    /* access modifiers changed from: protected */
    public void cancelPendingPowerKeyActionForDistouch() {
        cancelPendingPowerKeyAction();
    }

    public boolean isTopIsFullscreen() {
        return this.mTopIsFullscreen;
    }

    public boolean isLastImmersiveMode() {
        return isImmersiveMode(this.mLastSystemUiFlags);
    }

    /* access modifiers changed from: protected */
    public BarController getStatusBarController() {
        return this.mStatusBarController;
    }

    /* access modifiers changed from: protected */
    public ImmersiveModeConfirmation getImmersiveModeConfirmation() {
        return this.mImmersiveModeConfirmation;
    }

    /* access modifiers changed from: protected */
    public void updateHwSystemUiVisibilityLw() {
        updateSystemUiVisibilityLw();
    }

    /* access modifiers changed from: protected */
    public void requestHwTransientBars(WindowManagerPolicy.WindowState swipeTarget) {
        requestTransientBars(swipeTarget);
    }

    /* access modifiers changed from: protected */
    public WindowManagerPolicy.WindowState getCurrentWin() {
        if (this.mFocusedWindow != null) {
            return this.mFocusedWindow;
        }
        return this.mTopFullscreenOpaqueWindowState;
    }

    /* access modifiers changed from: protected */
    public void hwInit() {
    }

    /* access modifiers changed from: protected */
    public int getEmuiStyleValue(int styleValue) {
        return styleValue;
    }

    /* access modifiers changed from: protected */
    public boolean getFloatingValue(int styleValue) {
        return false;
    }

    public void setPickUpFlag() {
        if (mSupportAod) {
            this.mPickUpFlag = true;
            Slog.i(TAG, "setPickUpFlag mPickUpFlag:" + this.mPickUpFlag);
        }
    }

    public void setSyncPowerStateFlag() {
        if (mSupportAod && getSupportSensorHub()) {
            this.mSyncPowerStateFlag = true;
            Slog.i(TAG, "setSyncPowerStateFlag mSyncPowerStateFlag:" + this.mSyncPowerStateFlag);
        }
    }

    public void onPowerStateChange(int state) {
        if (mSupportAod && getSupportSensorHub()) {
            Slog.i(TAG, "onPowerStateChange state:" + state + " mSyncPowerStateFlag:" + this.mSyncPowerStateFlag);
            if (this.mSyncPowerStateFlag) {
                this.mSyncPowerStateFlag = false;
            }
        }
    }

    private boolean isInScreenFingerprint(int type) {
        return 1 == type || 2 == type;
    }

    private boolean getSupportSensorHub() {
        Slog.i(TAG, "getSupportSensorHub mDeviceNodeFD = " + this.mDeviceNodeFD);
        return this.mDeviceNodeFD > 0;
    }

    private void pauseSensorhubAOD() {
        if (getSupportSensorHub()) {
            pause();
        }
    }

    private boolean isExecOtaPdLaya() {
        return this.otaStateForOverSeaPdLaya == 0;
    }

    private boolean isOverSeaVersion() {
        return !sIsChineseLanguage || !sIsChinaRegion;
    }

    private boolean isOverSeaLyaNeedOta() {
        return isOverSeaVersion() && sIsLayaPorschePorduct && isExecOtaPdLaya();
    }

    public boolean startAodService(int startState) {
        Slog.i(TAG, "AOD startAodService mAodSwitch=" + this.mAodSwitch + " mAODState=" + this.mAODState + " startState=" + startState + " mIsFingerprintEnabledBySettings=" + this.mIsFingerprintEnabledBySettings + " mFingerprintType=" + this.mFingerprintType + " mPickUpFlag" + this.mPickUpFlag);
        boolean pickUpToPauseAod = this.mPickUpFlag && startState == 5 && this.mAODState != 4;
        this.mNeedPauseAodWhileSwitchUser = this.mNeedPauseAodWhileSwitchUser || (startState == 6 && this.mAODState == 3);
        if (startState == 7 && this.mDeviceNodeFD == -2147483647) {
            this.mDeviceNodeFD = getDeviceNodeFD();
        }
        if (!mSupportAod) {
            return false;
        }
        if (-1 == this.mFingerprintType) {
            this.mFingerprintType = getHardwareType();
        }
        if (this.mAodSwitch == 0 && !isOverSeaLyaNeedOta()) {
            if (!isInScreenFingerprint(this.mFingerprintType)) {
                this.mAODState = startState;
                return false;
            } else if (!this.mIsFingerprintEnabledBySettings && !this.mNeedPauseAodWhileSwitchUser) {
                this.mAODState = startState;
                return false;
            }
        }
        if (this.mAODState == startState) {
            return false;
        }
        this.mAODState = startState;
        if (startState == 4) {
            setPowerState(6);
            setPowerState(0);
            pauseSensorhubAOD();
            this.mNeedPauseAodWhileSwitchUser = false;
        }
        if (startState == 5) {
            setPowerState(6);
            if (pickUpToPauseAod) {
                setPowerState(5);
                this.mNeedPauseAodWhileSwitchUser = false;
            }
        }
        if (startState == 7) {
            setPowerState(7);
            setPowerState(8);
        }
        if (startState == 3) {
            setPowerState(7);
            if (this.mPickUpFlag) {
                setPowerState(9);
            }
        }
        handleAodState(startState, this.mPickUpFlag);
        if (this.mPickUpFlag && startState == 3) {
            Slog.w(TAG, "reset mPickUpFlag to false after handleAodState");
            this.mPickUpFlag = false;
        }
        return true;
    }

    private void handleAodState(final int startState, final boolean pickUpFlag) {
        this.mHandler.post(new Runnable() {
            public void run() {
                Intent intent = null;
                switch (startState) {
                    case 1:
                        intent = new Intent("com.huawei.aod.action.AOD_SERVICE_START");
                        break;
                    case 2:
                        synchronized (this) {
                            if (PhoneWindowManager.this.mIAodStateCallback != null) {
                                try {
                                    PhoneWindowManager.this.mIAodStateCallback.onScreenOn();
                                } catch (RemoteException e) {
                                }
                            } else {
                                intent = new Intent("com.huawei.aod.action.AOD_SCREEN_ON");
                            }
                        }
                        break;
                    case 3:
                        synchronized (this) {
                            Slog.i(PhoneWindowManager.TAG, "handleAodState START_AOD_SCREEN_OFF mPickUpFlag:" + PhoneWindowManager.this.mPickUpFlag + ", pickUpFlag " + pickUpFlag);
                            if (PhoneWindowManager.this.mIAodStateCallback != null) {
                                try {
                                    if (pickUpFlag) {
                                        PhoneWindowManager.this.mIAodStateCallback.onScreenOffWhenRaisePhone();
                                    } else {
                                        PhoneWindowManager.this.mIAodStateCallback.onScreenOff();
                                    }
                                } catch (RemoteException e2) {
                                }
                            } else {
                                intent = new Intent("com.huawei.aod.action.AOD_SCREEN_OFF");
                                intent.putExtra(PhoneWindowManager.NEED_START_AOD_WHEN_SCREEN_OFF, pickUpFlag);
                            }
                        }
                        break;
                    case 4:
                        synchronized (this) {
                            if (PhoneWindowManager.this.mIAodStateCallback != null) {
                                try {
                                    PhoneWindowManager.this.mIAodStateCallback.onWakingUp();
                                } catch (RemoteException e3) {
                                }
                            } else {
                                intent = new Intent("com.huawei.aod.action.AOD_WAKE_UP");
                            }
                        }
                        break;
                    case 5:
                        synchronized (this) {
                            if (PhoneWindowManager.this.mIAodStateCallback != null) {
                                try {
                                    PhoneWindowManager.this.mIAodStateCallback.onTurningOn();
                                } catch (RemoteException e4) {
                                }
                            }
                        }
                        break;
                    case 6:
                        intent = new Intent("com.huawei.aod.action.AOD_SCREEN_OFF");
                        break;
                    case 7:
                        PhoneWindowManager.this.setPowerState(1);
                        synchronized (this) {
                            if (PhoneWindowManager.this.mIAodStateCallback != null) {
                                try {
                                    PhoneWindowManager.this.mIAodStateCallback.startedGoingToSleep();
                                } catch (RemoteException e5) {
                                }
                            } else {
                                intent = new Intent("com.huawei.aod.action.AOD_GOING_TO_SLEEP");
                            }
                        }
                        break;
                }
                if (intent != null) {
                    intent.setComponent(new ComponentName("com.huawei.aod", "com.huawei.aod.AODService"));
                    PhoneWindowManager.this.mContext.startService(intent);
                }
            }
        });
    }

    public void regeditAodStateCallback(IAodStateCallback callback) {
        Slog.i(TAG, "AOD regeditAodStateCallback ");
        if (mSupportAod) {
            synchronized (this) {
                this.mIAodStateCallback = callback;
            }
            if (callback != null) {
                try {
                    callback.asBinder().linkToDeath(new AppDeathRecipient(), 0);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void unregeditAodStateCallback(IAodStateCallback callback) {
        Slog.i(TAG, "AOD unregeditAodStateCallback ");
        if (mSupportAod) {
            synchronized (this) {
                this.mIAodStateCallback = null;
            }
        }
    }

    private long printTimeoutLog(String functionName, long startTime, String type, int timeout) {
        long endTime = SystemClock.elapsedRealtime();
        if (endTime - startTime > ((long) timeout)) {
            Log.i(TAG, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + functionName + " " + type + " duration: " + (endTime - startTime));
        }
        return endTime;
    }

    public boolean isNotchDisplayDisabled() {
        return this.mIsNotchSwitchOpen;
    }

    public int getRestrictedScreenHeight() {
        return 0;
    }

    public boolean isWindowSupportKnuckle() {
        return false;
    }

    public boolean isNavigationBarVisible() {
        return false;
    }

    public boolean isKeyguardShowingOrOccluded() {
        if (this.mKeyguardDelegate == null) {
            return false;
        }
        return this.mKeyguardDelegate.isShowing();
    }

    public void setFullScreenWinVisibile(boolean visible) {
        this.mHwFullScreenWinVisibility = visible;
    }

    public void setFullScreenWindow(WindowManagerPolicy.WindowState win) {
        this.mHwFullScreenWindow = win;
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

    private void notifyRotate(int oldRotation, int newRotation) {
        if (this.mHwRotateObservers != null && newRotation != oldRotation) {
            int i = this.mHwRotateObservers.beginBroadcast();
            while (i > 0) {
                i--;
                IHwRotateObserver observer = this.mHwRotateObservers.getBroadcastItem(i);
                try {
                    observer.onRotate(oldRotation, newRotation);
                    Log.i(TAG, "musa3 notifyRotate() to observer " + observer);
                } catch (RemoteException e) {
                    Log.w(TAG, "RemoteException in notifyRotate(), remove observer " + observer);
                    unregisterRotateObserver(observer);
                }
            }
            this.mHwRotateObservers.finishBroadcast();
        }
    }

    public int getDefaultNavBarHeight() {
        return this.mDefaultNavBarHeight;
    }

    public boolean isInputMethodMovedUp() {
        return this.mInputMethodMovedUp;
    }

    public boolean isNavBarVisible() {
        return this.mHasNavigationBar && this.mNavigationBar != null && this.mNavigationBar.isVisibleLw();
    }

    public boolean isPendingLock() {
        return this.mKeyguardDelegate.isPendingLock();
    }

    private void adjustInsetSurfaceState(final WindowManagerPolicy.WindowState win, DisplayFrames displayFrames, int top) {
        if (mSupporInputMethodFilletAdaptation && this.mDisplayRotation == 0 && win.isImeWithHwFlag() && win.getAttrs().type == 2011) {
            if (displayFrames.mContent.bottom - top > 100) {
                if (this.mIsFloatIME && this.mInputMethodMovedUp) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            win.showInsetSurfaceOverlayImmediately();
                        }
                    });
                }
                this.mIsFloatIME = false;
            } else {
                if (!this.mIsFloatIME) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            win.hideInsetSurfaceOverlayImmediately();
                        }
                    });
                }
                this.mIsFloatIME = true;
            }
        }
    }

    public IHwPhoneWindowManagerEx getPhoneWindowManagerEx() {
        return this.mHwPWMEx;
    }

    public void setNaviBarFlag(boolean flag) {
        this.mHwPWMEx.setNaviBarFlag(flag);
    }

    public void updateNavigationBar(boolean minNaviBar) {
        this.mHwPWMEx.updateNavigationBar(minNaviBar);
    }

    public Object getNavigationBarPolicy() {
        return null;
    }

    public int[] getNavigationBarValueForRotation(int index) {
        return null;
    }

    public void setNavigationBarValueForRotation(int index, int type, int value) {
    }

    public void setNavigationBarHeightDef(int[] values) {
        this.mNavigationBarHeightForRotationDefault = values;
    }

    public void setNavigationBarWidthDef(int[] values) {
        this.mNavigationBarWidthForRotationDefault = values;
    }

    /* access modifiers changed from: package-private */
    public void removeFreeFormStackIfNeed() {
        this.mHwPWMEx.removeFreeFormStackIfNeed(this.mWindowManagerInternal);
    }

    public int getRotationValueByType(int type) {
        switch (type) {
            case 0:
                return this.mLandscapeRotation;
            case 1:
                return this.mSeascapeRotation;
            case 2:
                return this.mPortraitRotation;
            case 3:
                return this.mUpsideDownRotation;
            default:
                return 0;
        }
    }

    public void setDisplayMode(int mode) {
    }

    public void setIsNeedNotifyWallet(boolean isNeedNotify) {
        this.isNeedNotifyWallet = isNeedNotify;
    }

    public boolean getIsNeedNotifyWallet() {
        return this.isNeedNotifyWallet;
    }
}
