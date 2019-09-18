package com.android.server.policy;

import android.aft.HwAftPolicyManager;
import android.aft.IHwAftPolicyService;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.SynchronousUserSwitchObserver;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.freeform.HwFreeFormManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.HwFoldScreenState;
import android.hardware.input.InputManager;
import android.hdm.HwDeviceManager;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.hwcontrol.HwWidgetFactory;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.Trace;
import android.os.UserHandle;
import android.os.Vibrator;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.telecom.TelecomManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.HwStylusUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicyConstants;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.telephony.ITelephony;
import com.android.server.CoordinationStackDividerManager;
import com.android.server.LocalServices;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.HwPCMultiWindowPolicy;
import com.android.server.foldscreenview.SubScreenViewEntry;
import com.android.server.gesture.GestureNavConst;
import com.android.server.gesture.GestureNavManager;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.mplink.HwMpLinkServiceImpl;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.android.server.input.HwInputManagerService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.notch.HwNotchScreenWhiteConfig;
import com.android.server.pc.HwPCDataReporter;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.SystemGesturesPointerEventListener;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.keyguard.KeyguardStateMonitor;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.feature.StartWindowFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.tsmagent.server.HttpConnectionBase;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.DisplayFrames;
import com.android.server.wm.HwGestureNavWhiteConfig;
import com.android.server.wm.HwStartWindowRecord;
import com.android.server.wm.IntelliServiceManager;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.gameassist.HwGameAssistManager;
import com.huawei.android.inputmethod.HwInputMethodManager;
import com.huawei.android.statistical.StatisticalUtils;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.featurelayer.HwFeatureLoader;
import com.huawei.featurelayer.featureframework.IFeature;
import com.huawei.featurelayer.featureframework.IFeatureFramework;
import com.huawei.featurelayer.systemservicefeature.HwGestureAction.IHwSystemWideActionsListenerEx;
import com.huawei.forcerotation.HwForceRotationManager;
import huawei.android.aod.HwAodManager;
import huawei.android.app.IHwWindowCallback;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;
import huawei.android.hwutil.HwFullScreenDisplay;
import huawei.android.provider.FingerSenseSettings;
import huawei.android.provider.FrontFingerPrintSettings;
import huawei.android.security.facerecognition.FaceReportEventToIaware;
import huawei.com.android.internal.widget.HwWidgetUtils;
import huawei.com.android.server.fingerprint.FingerViewController;
import huawei.com.android.server.policy.HwFalseTouchMonitor;
import huawei.com.android.server.policy.HwScreenOnProximityLock;
import huawei.com.android.server.policy.fingersense.KnuckGestureSetting;
import huawei.com.android.server.policy.stylus.StylusGestureListener;
import huawei.cust.HwCustUtils;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class HwPhoneWindowManager extends PhoneWindowManager implements AccessibilityManager.TouchExplorationStateChangeListener {
    private static final String ACTION_HUAWEI_VASSISTANT_SERVICE = "com.huawei.ziri.model.MODELSERVICE";
    private static final String ACTIVITY_NAME_EMERGENCY_SIMPLIFIEDINFO = "com.android.emergency/.view.ViewSimplifiedInfoActivity";
    private static final int BACK_HOME_RECENT_DOUBLE_CLICK_TIMEOUT = 300;
    static final boolean DEBUG = false;
    static final boolean DEBUG_IMMERSION = false;
    private static final int DEFAULT_RESULT_VALUE = -2;
    private static final long DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MAX = 15000;
    private static final long DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MIN = 1500;
    static final String DROP_SMARTKEY_ACTIVITY = "drop_smartkey_activity";
    private static final int EVENT_DURING_MIN_TIME = 500;
    static final String FINGERPRINT_ANSWER_CALL = "fp_answer_call";
    static final String FINGERPRINT_CAMERA_SWITCH = "fp_take_photo";
    private static final int FINGERPRINT_HARDWARE_OPTICAL = 1;
    static final String FINGERPRINT_STOP_ALARM = "fp_stop_alarm";
    private static final int FLOATING_MASK = Integer.MIN_VALUE;
    private static final int FORCE_STATUS_BAR = 1;
    public static final String FRONT_FINGERPRINT_BUTTON_LIGHT_MODE = "button_light_mode";
    private static final boolean FRONT_FINGERPRINT_GESTURE_NAVIGATION_SUPPORTED = SystemProperties.getBoolean("ro.config.gesture_front_support", false);
    public static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    private static final String GESTURE_NAVIGATION = "secure_gesture_navigation";
    public static final String HAPTIC_FEEDBACK_TRIKEY_SETTINGS = "physic_navi_haptic_feedback_enabled";
    private static final String HIVOICE_PRESS_TYPE_HOMO = "homo";
    private static final String HIVOICE_PRESS_TYPE_POWER = "power";
    private static final String HOMOAI_EVENT_TAG = "event_type";
    private static final String HOMOAI_PRESS_TAG = "press_type";
    private static final int HOMOKEY_CLICK_EVENT = 2;
    private static final int HOMOKEY_DOWN_EVENT = 5;
    private static final int HOMOKEY_LONGPRESS_EVENT = 1;
    private static final int HOMOKEY_LONGUP_EVENT = 4;
    private static final int HOMOKEY_SUPERLONGPRESS_EVENT = 3;
    private static final String HUAWEI_HIACTION_ACTION = "com.huawei.hiaction.HOMOAI";
    private static final String HUAWEI_HIACTION_PACKAGE = "com.huawei.hiaction";
    private static final String HUAWEI_RAPIDCAPTURE_START_MODE = "com.huawei.RapidCapture";
    private static final String HUAWEI_SCREENRECORDER_ACTION = "com.huawei.screenrecorder.Start";
    private static final String HUAWEI_SCREENRECORDER_PACKAGE = "com.huawei.screenrecorder";
    private static final String HUAWEI_SCREENRECORDER_START_MODE = "com.huawei.screenrecorder.ScreenRecordService";
    private static final String HUAWEI_SMARTKEY_PACKAGE = "com.huawei.smartkey";
    private static final String HUAWEI_SMARTKEY_SERVICE = "com.android.huawei.smartkey";
    private static final String HUAWEI_VASSISTANT_EXTRA_START_MODE = "com.huawei.vassistant.extra.SERVICE_START_MODE";
    private static final String HUAWEI_VASSISTANT_PACKAGE = "com.huawei.vassistant";
    private static final String HUAWEI_VOICE_DEBUG_BETACLUB = "com.huawei.betaclub";
    private static final String HUAWEI_VOICE_SOUNDTRIGGER_ACTIVITY = "com.mmc.soundtrigger.MainActivity";
    private static final String HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST = "com.mmc.SOUNDTRIGGER";
    private static final String HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE = "com.mmc.soundtrigger";
    private static final boolean HWRIDEMODE_FEATURE_SUPPORTED = SystemProperties.getBoolean("ro.config.ride_mode", false);
    private static final boolean IS_LONG_HOME_VASSITANT = SystemProperties.getBoolean("ro.hw.long.home.vassistant", true);
    private static final boolean IS_POWER_HIACTION_KEY = SystemProperties.getBoolean("ro.config.hw_power_voice_key", true);
    private static final List KEYCODE_NOT_FOR_CLOUD = Arrays.asList(new Integer[]{4, 3, 25, 24, 220, Integer.valueOf(HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_AVAILABLE)});
    private static final String KEY_DOUBLE_TAP_PAY = "double_tap_enable_pay";
    private static final String KEY_HIVOICE_PRESS_TYPE = "invoke_hivoice_keypress_type";
    private static final String KEY_HWOUC_KEYGUARD_VIEW_ON_TOP = "hwouc_keyguard_view_on_top";
    private static final String KEY_TOAST_POWER_OFF = "toast_power_off";
    private static final String KEY_TOUCH_DISABLE_MODE = "touch_disable_mode";
    private static final int MAX_POWERKEY_COUNTDOWN = 5;
    private static final int MAX_POWEROFF_TOAST_SHOW = 2;
    private static final int MSG_BUTTON_LIGHT_TIMEOUT = 4099;
    private static final int MSG_FINGERSENSE_DISABLE = 102;
    private static final int MSG_FINGERSENSE_ENABLE = 101;
    private static final int MSG_FREEZE_POWER_KEY = 4103;
    private static final int MSG_NAVIBAR_DISABLE = 104;
    private static final int MSG_NAVIBAR_ENABLE = 103;
    private static final int MSG_NOTIFY_FINGER_OPTICAL = 4100;
    private static final int MSG_TRIKEY_BACK_LONG_PRESS = 4097;
    private static final int MSG_TRIKEY_RECENT_LONG_PRESS = 4098;
    public static final int NAVIGATION_BAR_HEIGHT_MAX = 1;
    public static final int NAVIGATION_BAR_HEIGHT_MIN = 0;
    public static final int NAVIGATION_BAR_WIDTH_MAX = 2;
    public static final int NAVIGATION_BAR_WIDTH_MIN = 3;
    private static final boolean NEED_TAILORED = SystemProperties.getBoolean("ro.config.need_tailored", false);
    private static final int NOTCH_ROUND_CORNER_CODE = 8002;
    private static final int NOTCH_ROUND_CORNER_HIDE = 0;
    private static final int NOTCH_ROUND_CORNER_SHOW = 1;
    private static final int NOTCH_STATUS_BAR_BLACK = 1;
    private static final int NOTCH_STATUS_BAR_DEFAULT = 0;
    private static final String NOTEEDITOR_ACTIVITY_NAME = "com.example.android.notepad/com.huawei.android.notepad.views.SketchActivity";
    private static final int NOTIFY_HIACTION_LONGPRESS_TYPE_HOMO = 1;
    private static final int NOTIFY_HIACTION_LONGPRESS_TYPE_NONE = 0;
    private static final int NOTIFY_HIACTION_LONGPRESS_TYPE_POWER = 2;
    private static final boolean OPEN_PROXIMITY_DISPALY = SystemProperties.getBoolean("ro.config.open_proximity_display", false);
    private static final String PKG_CALCULATOR = "com.android.calculator2";
    private static final String PKG_CAMERA = "com.huawei.camera";
    private static final String PKG_GALLERY = "com.android.gallery3d";
    private static final String PKG_HWNOTEPAD = "com.example.android.notepad";
    private static final String PKG_NAME_EMERGENCY = "com.android.emergency";
    private static final String PKG_SCANNER = "com.huawei.scanner";
    private static final String PKG_SOUNDRECORDER = "com.android.soundrecorder";
    private static final long POWERKEY_LONG_PRESS_TIMEOUT = 700;
    private static final String POWERKEY_QUICKPAY_ACTION = "com.huawei.oto.intent.action.QUICKPAY";
    private static final String POWERKEY_QUICKPAY_PACKAGE = "com.huawei.wallet";
    private static final long POWERKEY_SUPERLONG_PRESS_TIMEOUT = 3000;
    private static final long POWER_SOS_MISTOUCH_THRESHOLD = 300;
    private static final long POWER_SOS_TIMEOUT = 500;
    private static final long SCREENRECORDER_DEBOUNCE_DELAY_MILLIS = 150;
    private static final int SIDE_POWER_FP_COMB = 3;
    private static final int SIM_CARD_1 = 0;
    private static final int SIM_CARD_2 = 1;
    private static final int SINGLE_HAND_STATE = 1989;
    private static final String SMARTKEY_CLICK = "Click";
    private static final String SMARTKEY_DCLICK = "DoubleClick";
    private static final long SMARTKEY_DOUBLE_CLICK_TIMEOUT = 400;
    private static final long SMARTKEY_LONG_PRESS_TIMEOUT = 500;
    private static final String SMARTKEY_LP = "LongPress";
    private static final long SMARTKEY_SUPERLONG_PRESS_TIMEOUT = 1000;
    private static final String SMARTKEY_TAG = "command";
    private static final int START_MODE_QUICK_START_CALL = 2;
    static final int START_MODE_VOICE_WAKEUP_ONE_SHOT = 4;
    private static final long SYSTRACELOG_DEBOUNCE_DELAY_MILLIS = 150;
    private static final long SYSTRACELOG_FINGERPRINT_EFFECT_DELAY = 750;
    static final String TAG = "HwPhoneWindowManager";
    private static final int TOAST_POWER_OFF_TIMEOUT = 1000;
    private static final int TOAST_TYPE_COVER_SCREEN = 2101;
    private static final boolean TOUCHPLUS_FORCE_VIBRATION = true;
    private static final int TOUCHPLUS_SETTINGS_DISABLED = 0;
    private static final int TOUCHPLUS_SETTINGS_ENABLED = 1;
    private static final String TOUCHPLUS_SETTINGS_VIBRATION = "hw_membrane_touch_vibrate_enabled";
    private static final long TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS = 150;
    private static final int TOUCH_EXPLR_NAVIGATION_BAR_COLOR = -16777216;
    private static final int TOUCH_EXPLR_STATUS_BAR_COLOR = -16777216;
    private static final long TOUCH_SPINNING_DELAY_MILLIS = 2000;
    private static final int TP_HAL_DEATH_COOKIE = 1001;
    private static final String TP_KEEP_FILE = "/sys/touchscreen/touch_switch";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final int UNDEFINED_TYPE = -1;
    private static final String UPDATE_GESTURE_NAV_ACTION = "huawei.intent.action.FWK_UPDATE_GESTURE_NAV_ACTION";
    private static final String UPDATE_NOTCH_SCREEN_ACTION = "huawei.intent.action.FWK_UPDATE_NOTCH_SCREEN_ACTION";
    private static final String UPDATE_NOTCH_SCREEN_PER = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String VIBRATE_ON_TOUCH = "vibrate_on_touch";
    /* access modifiers changed from: private */
    public static final int VIBRATOR_LONG_PRESS_FOR_FRONT_FP = SystemProperties.getInt("ro.config.trikey_vibrate_press", 16);
    private static final int VIBRATOR_SHORT_PRESS_FOR_FRONT_FP = SystemProperties.getInt("ro.config.trikey_vibrate_touch", 8);
    private static String VOICE_ASSISTANT_ACTION = "com.huawei.action.VOICE_ASSISTANT";
    private static final long VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT = 400;
    private static final long VOLUMEDOWN_LONG_PRESS_TIMEOUT = 500;
    private static final int WAKEUP_NOTEEDITOR_TIMEOUT = 500;
    private static boolean isSwich = false;
    private static final boolean isVibrateImplemented = false;
    private static boolean mCustBeInit = false;
    private static boolean mCustUsed = false;
    private static IFeature mIFeature = null;
    static final boolean mIsHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private static boolean mIsSidePowerFpComb = false;
    private static final boolean mSupportDoubleTapPay = SystemProperties.getBoolean("ro.config.support_doubletap_pay", false);
    private static final boolean mSupportGameAssist = (SystemProperties.getInt("ro.config.gameassist", 0) == 1);
    private static int[] mUnableWakeKey;
    private boolean DEBUG_SMARTKEY = false;
    /* access modifiers changed from: private */
    public int TRIKEY_NAVI_DEFAULT_MODE = -1;
    /* access modifiers changed from: private */
    public AlertDialog alertDialog;
    FingerprintActionsListener fingerprintActionsListener;
    private HwNotchScreenWhiteConfig hwNotchScreenWhiteConfig;
    private boolean isAppWindow = false;
    private boolean isFingerAnswerPhoneOn = false;
    private boolean isFingerShotCameraOn = false;
    private boolean isFingerStopAlarmOn = false;
    boolean isHomeAndDBothDown;
    boolean isHomeAndEBothDown;
    boolean isHomeAndLBothDown;
    boolean isHomePressDown;
    private boolean isHwOUCKeyguardViewOnTop = false;
    /* access modifiers changed from: private */
    public boolean isVoiceRecognitionActive;
    private int lastDensityDpi;
    private final Runnable mAIHomoLongPressed;
    private final Runnable mAIHomoSuperLongPressed;
    private final Runnable mAIPowerLongPressed;
    private final Runnable mAIPowerSuperLongPressed;
    private int mActionBarHeight;
    private HwActivityManagerService mAms;
    private final IZrHung mAppEyeBackKey = HwFrameworkFactory.getZrHung("appeye_backkey");
    private final IZrHung mAppEyeHomeKey = HwFrameworkFactory.getZrHung("appeye_homekey");
    private boolean mBackKeyPress = false;
    private long mBackKeyPressTime = 0;
    private Light mBackLight = null;
    volatile boolean mBackTrikeyHandled;
    private int mBarVisibility = 1;
    boolean mBooted = false;
    private PowerManager.WakeLock mBroadcastWakeLock;
    /* access modifiers changed from: private */
    public Light mButtonLight = null;
    /* access modifiers changed from: private */
    public int mButtonLightMode = 1;
    private final Runnable mCancleInterceptFingerprintEvent;
    private CoverManager mCoverManager;
    private boolean mCoverOpen;
    private int mCurUser;
    private int mCurrentRotation;
    HwCustPhoneWindowManager mCust = ((HwCustPhoneWindowManager) HwCustUtils.createObj(HwCustPhoneWindowManager.class, new Object[0]));
    int mDesiredRotation;
    /* access modifiers changed from: private */
    public boolean mDeviceProvisioned = false;
    /* access modifiers changed from: private */
    public boolean mDoubleTapPay;
    /* access modifiers changed from: private */
    public boolean mEnableKeyInCurrentFgGameApp;
    private SystemGesturesPointerEventListener mExternalSystemGestures;
    IntelliServiceManager.FaceRotationCallback mFaceRotationCallback;
    private HwFalseTouchMonitor mFalseTouchMonitor;
    private final Object mFeatureLock;
    private int mFingerPrintId;
    boolean mFingerSenseEnabled = true;
    private int mFingerprintHardwareType;
    private ContentObserver mFingerprintObserver;
    private boolean mFirstSetCornerDefault = true;
    private boolean mFirstSetCornerInLandNoNotch = true;
    private boolean mFirstSetCornerInLandNotch = true;
    private boolean mFirstSetCornerInPort = true;
    private boolean mFirstSetCornerInReversePortait = true;
    private boolean mFocusWindowUsingNotch;
    HwFoldScreenManagerInternal mFoldScreenManagerService;
    /* access modifiers changed from: private */
    public GestureNavManager mGestureNavManager;
    private final Runnable mHandleVolumeDownKey;
    private Handler mHandlerEx;
    /* access modifiers changed from: private */
    public boolean mHapticEnabled = true;
    private boolean mHeadless;
    /* access modifiers changed from: private */
    public String mHiVoiceKeyType;
    private boolean mHintShown;
    private boolean mHomoAIKey;
    private ContentObserver mHwOUCObserver;
    /* access modifiers changed from: private */
    public HwScreenOnProximityLock mHwScreenOnProximityLock;
    public IHwWindowCallback mIHwWindowCallback;
    private boolean mInputMethodWindowVisible;
    /* access modifiers changed from: private */
    public boolean mIsForceSetStatusBar = false;
    /* access modifiers changed from: private */
    public boolean mIsFreezePowerkey;
    /* access modifiers changed from: private */
    public boolean mIsGestureNavEnable;
    private boolean mIsHasActionBar;
    protected boolean mIsImmersiveMode;
    boolean mIsNavibarAlignLeftWhenLand;
    /* access modifiers changed from: private */
    public boolean mIsProximity;
    private boolean mIsRestoreStatusBar = false;
    private boolean mIsSmartKeyDoubleClick = false;
    private boolean mIsSmartKeyTripleOrMoreClick = false;
    private boolean mIsTouchExplrEnabled;
    private String[] mKeyguardShortcutApps = {"com.huawei.camera", PKG_GALLERY, PKG_SCANNER};
    private WindowManagerPolicy.WindowState mLastColorWin;
    /* access modifiers changed from: private */
    public String mLastFgPackageName;
    /* access modifiers changed from: private */
    public int mLastIsEmuiStyle;
    private boolean mLastKeyDownDropped;
    private int mLastKeyDownKeyCode;
    private long mLastKeyDownTime;
    /* access modifiers changed from: private */
    public long mLastKeyPointerTime;
    /* access modifiers changed from: private */
    public int mLastNavigationBarColor;
    private long mLastPowerKeyDownTime;
    /* access modifiers changed from: private */
    public long mLastPowerWalletDownTime;
    private long mLastSmartKeyDownTime;
    /* access modifiers changed from: private */
    public long mLastStartVassistantServiceTime;
    /* access modifiers changed from: private */
    public int mLastStatusBarColor;
    private long mLastVolumeDownKeyDownTime;
    private long mLastVolumeKeyDownTime;
    private long mLastWakeupTime;
    WindowManagerPolicy.WindowState mLighterDrawView;
    private ProximitySensorListener mListener;
    private WindowManagerPolicyConstants.PointerEventListener mLockScreenBuildInDisplayListener;
    private WindowManagerPolicyConstants.PointerEventListener mLockScreenListener;
    private String[] mLsKeyguardShortcutApps = {PKG_SOUNDRECORDER, PKG_CALCULATOR, PKG_HWNOTEPAD};
    private boolean mMenuClickedOnlyOnce;
    private boolean mMenuKeyPress = false;
    private long mMenuKeyPressTime = 0;
    boolean mNaviBarStateInited = false;
    boolean mNavibarEnabled = false;
    final BarController mNavigationBarControllerExternal;
    protected WindowManagerPolicy.WindowState mNavigationBarExternal;
    int mNavigationBarHeightExternal;
    int[] mNavigationBarHeightForRotationMax;
    int[] mNavigationBarHeightForRotationMin;
    protected NavigationBarPolicy mNavigationBarPolicy;
    int[] mNavigationBarWidthForRotationMax;
    int[] mNavigationBarWidthForRotationMin;
    /* access modifiers changed from: private */
    public boolean mNeedDropFingerprintEvent = false;
    private HwNotchScreenWhiteConfig.NotchSwitchListener mNotchSwitchListener;
    OverscanTimeout mOverscanTimeout;
    private int mPowerKeyCount;
    private boolean mPowerKeyDisTouch;
    private long mPowerKeyDisTouchTime;
    private final Runnable mPowerKeyStartWallet;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    private final Runnable mPowerOffRunner;
    /* access modifiers changed from: private */
    public int mPowerWalletCount;
    private boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener;
    final Runnable mProximitySensorTimeoutRunnable;
    private boolean mProximityTop = SystemProperties.getBoolean("ro.config.proximity_top", false);
    volatile boolean mRecentTrikeyHandled;
    /* access modifiers changed from: private */
    public ContentResolver mResolver;
    private boolean mScreenOnForFalseTouch;
    private long mScreenRecorderPowerKeyTime;
    private boolean mScreenRecorderPowerKeyTriggered;
    private final Runnable mScreenRecorderRunnable;
    private boolean mScreenRecorderVolumeDownKeyTriggered;
    private boolean mScreenRecorderVolumeUpKeyConsumed;
    private long mScreenRecorderVolumeUpKeyTime;
    private boolean mScreenRecorderVolumeUpKeyTriggered;
    private int mSecondToLastKeyDownKeyCode;
    private long mSecondToLastKeyDownTime;
    private SensorManager mSensorManager;
    /* access modifiers changed from: private */
    public boolean mSensorRegisted;
    final Object mServiceAquireLock = new Object();
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private SettingsObserver mSettingsObserver;
    private final Runnable mSmartKeyClick;
    private final Runnable mSmartKeyLongPressed;
    boolean mStatuBarObsecured;
    IStatusBarService mStatusBarService;
    private StylusGestureListener mStylusGestureListener;
    private StylusGestureListener mStylusGestureListener4PCMode;
    private SubScreenViewEntry mSubScreenViewEntry;
    WindowManagerPolicyConstants.PointerEventListener mSystemWideActionsListenerPointerEventListener;
    /* access modifiers changed from: private */
    public boolean mSystraceLogCompleted;
    private long mSystraceLogFingerPrintTime;
    private boolean mSystraceLogPowerKeyTriggered;
    private final Runnable mSystraceLogRunnable;
    private boolean mSystraceLogVolumeDownKeyTriggered;
    private boolean mSystraceLogVolumeUpKeyConsumed;
    private long mSystraceLogVolumeUpKeyTime;
    private boolean mSystraceLogVolumeUpKeyTriggered;
    private TouchCountPolicy mTouchCountPolicy = new TouchCountPolicy();
    private int mTpDeviceId;
    /* access modifiers changed from: private */
    public ITouchscreen mTpTouchSwitch = null;
    /* access modifiers changed from: private */
    public int mTrikeyNaviMode = -1;
    private SystemVibrator mVibrator;
    private boolean mVolumeDownKeyDisTouch;
    private final Runnable mVolumeDownLongPressed;
    private PowerManager.WakeLock mVolumeDownWakeLock;
    private boolean mVolumeUpKeyConsumedByDisTouch;
    private boolean mVolumeUpKeyDisTouch;
    private long mVolumeUpKeyDisTouchTime;
    private BroadcastReceiver mWhitelistReceived;
    private HashSet<String> needDropSmartKeyActivities = new HashSet<>();
    /* access modifiers changed from: private */
    public int showPowerOffToastTimes;
    IHwSystemWideActionsListenerEx systemWideActionsListener;
    /* access modifiers changed from: private */
    public Handler systraceLogDialogHandler;
    /* access modifiers changed from: private */
    public HandlerThread systraceLogDialogThread;

    private final class DeathRecipient implements IHwBinder.DeathRecipient {
        private DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1001) {
                Slog.e(HwPhoneWindowManager.TAG, "tp hal service died cookie: " + cookie);
                synchronized (HwPhoneWindowManager.this.mLock) {
                    ITouchscreen unused = HwPhoneWindowManager.this.mTpTouchSwitch = null;
                    if (HwPhoneWindowManager.this.mTpKeepListener != null) {
                        HwPhoneWindowManager.this.mTpKeepListener.setTpKeep(false);
                    }
                }
            }
        }
    }

    class OverscanTimeout implements Runnable {
        OverscanTimeout() {
        }

        public void run() {
            Slog.i(HwPhoneWindowManager.TAG, "OverscanTimeout run");
            Settings.Global.putString(HwPhoneWindowManager.this.mContext.getContentResolver(), "single_hand_mode", "");
        }
    }

    private class PolicyHandlerEx extends Handler {
        private PolicyHandlerEx() {
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != HwPhoneWindowManager.MSG_FREEZE_POWER_KEY) {
                switch (i) {
                    case 101:
                        HwPhoneWindowManager.this.enableSystemWideActions();
                        return;
                    case 102:
                        HwPhoneWindowManager.this.disableSystemWideActions();
                        return;
                    case 103:
                        HwPhoneWindowManager.this.enableFingerPrintActions();
                        return;
                    case 104:
                        HwPhoneWindowManager.this.disableFingerPrintActions();
                        return;
                    default:
                        switch (i) {
                            case HwPhoneWindowManager.MSG_TRIKEY_BACK_LONG_PRESS /*4097*/:
                                HwPhoneWindowManager.this.mBackTrikeyHandled = true;
                                if (HwPhoneWindowManager.this.mTrikeyNaviMode == 1) {
                                    HwPhoneWindowManager.this.startHwVibrate(HwPhoneWindowManager.VIBRATOR_LONG_PRESS_FOR_FRONT_FP);
                                    Log.i(HwPhoneWindowManager.TAG, "LEFT->RECENT; RIGHT->BACK, handle longpress with recentTrikey and toggleSplitScreen");
                                    ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
                                    return;
                                } else if (HwPhoneWindowManager.this.mTrikeyNaviMode == 0) {
                                    Log.i(HwPhoneWindowManager.TAG, "LEFT->BACK; RIGHT->RECENT, handle longpress with backTrikey and unlockScreenPinningTest");
                                    HwPhoneWindowManager.this.unlockScreenPinningTest();
                                    return;
                                } else {
                                    return;
                                }
                            case HwPhoneWindowManager.MSG_TRIKEY_RECENT_LONG_PRESS /*4098*/:
                                HwPhoneWindowManager.this.mRecentTrikeyHandled = true;
                                if (HwPhoneWindowManager.this.mTrikeyNaviMode == 0) {
                                    HwPhoneWindowManager.this.startHwVibrate(HwPhoneWindowManager.VIBRATOR_LONG_PRESS_FOR_FRONT_FP);
                                    Log.i(HwPhoneWindowManager.TAG, "LEFT->BACK; RIGHT->RECENT, handle longpress with recentTrikey and toggleSplitScreen");
                                    ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
                                    return;
                                } else if (HwPhoneWindowManager.this.mTrikeyNaviMode == 1) {
                                    Log.i(HwPhoneWindowManager.TAG, "LEFT->RECENT; RIGHT->BACK, handle longpress with backTrikey and unlockScreenPinningTest");
                                    HwPhoneWindowManager.this.unlockScreenPinningTest();
                                    return;
                                } else {
                                    return;
                                }
                            case HwPhoneWindowManager.MSG_BUTTON_LIGHT_TIMEOUT /*4099*/:
                                if (HwPhoneWindowManager.this.mButtonLight == null) {
                                    return;
                                }
                                if (HwPhoneWindowManager.this.mPowerManager == null || !HwPhoneWindowManager.this.mPowerManager.isScreenOn()) {
                                    HwPhoneWindowManager.this.setButtonLightTimeout(false);
                                    return;
                                }
                                HwPhoneWindowManager.this.mButtonLight.setBrightness(0);
                                HwPhoneWindowManager.this.setButtonLightTimeout(true);
                                return;
                            case HwPhoneWindowManager.MSG_NOTIFY_FINGER_OPTICAL /*4100*/:
                                HwPhoneWindowManager.this.notifyFingerOptical();
                                return;
                            default:
                                return;
                        }
                }
            } else {
                Log.d(HwPhoneWindowManager.TAG, "Emergency power FreezePowerkey timeout.");
                boolean unused = HwPhoneWindowManager.this.mIsFreezePowerkey = false;
            }
        }
    }

    private class ProximitySensorListener implements SensorEventListener {
        public ProximitySensorListener() {
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent arg0) {
            float[] its = arg0.values;
            if (its != null && arg0.sensor.getType() == 8 && its.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("sensor value: its[0] = ");
                boolean z = false;
                sb.append(its[0]);
                Log.i(HwPhoneWindowManager.TAG, sb.toString());
                HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
                if (its[0] >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && its[0] < HwPhoneWindowManager.TYPICAL_PROXIMITY_THRESHOLD) {
                    z = true;
                }
                boolean unused = hwPhoneWindowManager.mIsProximity = z;
            }
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private ScreenBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                HwPhoneWindowManager.this.sendLightTimeoutMsg();
            }
        }
    }

    private final class ServiceNotification extends IServiceNotification.Stub {
        private ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            Slog.i(HwPhoneWindowManager.TAG, "tp hal service started " + fqName + " " + name);
            HwPhoneWindowManager.this.connectToProxy();
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            boolean z = true;
            boolean unused = HwPhoneWindowManager.this.mDeviceProvisioned = Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            int unused2 = HwPhoneWindowManager.this.mTrikeyNaviMode = Settings.System.getIntForUser(HwPhoneWindowManager.this.mResolver, "swap_key_position", HwPhoneWindowManager.this.TRIKEY_NAVI_DEFAULT_MODE, ActivityManager.getCurrentUser());
            int unused3 = HwPhoneWindowManager.this.mButtonLightMode = Settings.System.getIntForUser(HwPhoneWindowManager.this.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
            boolean unused4 = HwPhoneWindowManager.this.mHapticEnabled = Settings.System.getIntForUser(HwPhoneWindowManager.this.mResolver, "physic_navi_haptic_feedback_enabled", 1, ActivityManager.getCurrentUser()) != 0;
            boolean unused5 = HwPhoneWindowManager.this.mIsGestureNavEnable = Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, "secure_gesture_navigation", 0, ActivityManager.getCurrentUser()) != 0;
            String unused6 = HwPhoneWindowManager.this.mHiVoiceKeyType = Settings.Secure.getStringForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.KEY_HIVOICE_PRESS_TYPE, ActivityManager.getCurrentUser());
            boolean unused7 = HwPhoneWindowManager.this.mDoubleTapPay = Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.KEY_DOUBLE_TAP_PAY, 0, ActivityManager.getCurrentUser()) != 1 ? false : z;
            int unused8 = HwPhoneWindowManager.this.showPowerOffToastTimes = Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.KEY_TOAST_POWER_OFF, 0, ActivityManager.getCurrentUser());
        }

        public void registerContentObserver(int userId) {
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.System.getUriFor("swap_key_position"), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.System.getUriFor("device_provisioned"), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.System.getUriFor("button_light_mode"), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.System.getUriFor("physic_navi_haptic_feedback_enabled"), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.Secure.getUriFor("secure_gesture_navigation"), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwPhoneWindowManager.KEY_HIVOICE_PRESS_TYPE), false, this, userId);
            HwPhoneWindowManager.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(HwPhoneWindowManager.KEY_DOUBLE_TAP_PAY), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z = true;
            boolean unused = HwPhoneWindowManager.this.mDeviceProvisioned = Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            int unused2 = HwPhoneWindowManager.this.mTrikeyNaviMode = Settings.System.getIntForUser(HwPhoneWindowManager.this.mResolver, "swap_key_position", HwPhoneWindowManager.this.TRIKEY_NAVI_DEFAULT_MODE, ActivityManager.getCurrentUser());
            int unused3 = HwPhoneWindowManager.this.mButtonLightMode = Settings.System.getIntForUser(HwPhoneWindowManager.this.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
            boolean unused4 = HwPhoneWindowManager.this.mIsGestureNavEnable = Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, "secure_gesture_navigation", 0, ActivityManager.getCurrentUser()) != 0;
            HwPhoneWindowManager.this.resetButtonLightStatus();
            Slog.i(HwPhoneWindowManager.TAG, "mTrikeyNaviMode is:" + HwPhoneWindowManager.this.mTrikeyNaviMode + " mButtonLightMode is:" + HwPhoneWindowManager.this.mButtonLightMode + " mIsGestureNavEnable is:" + HwPhoneWindowManager.this.mIsGestureNavEnable);
            boolean unused5 = HwPhoneWindowManager.this.mHapticEnabled = Settings.System.getIntForUser(HwPhoneWindowManager.this.mResolver, "physic_navi_haptic_feedback_enabled", 1, ActivityManager.getCurrentUser()) != 0;
            String unused6 = HwPhoneWindowManager.this.mHiVoiceKeyType = Settings.Secure.getStringForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.KEY_HIVOICE_PRESS_TYPE, ActivityManager.getCurrentUser());
            Slog.i(HwPhoneWindowManager.TAG, "onChange mHiVoiceKeyType is:" + HwPhoneWindowManager.this.mHiVoiceKeyType);
            HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
            if (Settings.Secure.getIntForUser(HwPhoneWindowManager.this.mResolver, HwPhoneWindowManager.KEY_DOUBLE_TAP_PAY, 0, ActivityManager.getCurrentUser()) != 1) {
                z = false;
            }
            boolean unused7 = hwPhoneWindowManager.mDoubleTapPay = z;
        }
    }

    public HwPhoneWindowManager() {
        BarController barController = new BarController("NavigationBarExternal", 134217728, 536870912, FLOATING_MASK, 2, 134217728, 32768);
        this.mNavigationBarControllerExternal = barController;
        this.mLastKeyPointerTime = 0;
        this.mFalseTouchMonitor = null;
        this.mStylusGestureListener = null;
        this.mFingerPrintId = -2;
        this.mStylusGestureListener4PCMode = null;
        this.mFingerprintHardwareType = -1;
        this.mFocusWindowUsingNotch = true;
        this.mCurrentRotation = SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90;
        this.mTpDeviceId = -1;
        this.mHomoAIKey = SystemProperties.getBoolean("ro.config.hw_homo_key", false);
        this.mIsGestureNavEnable = false;
        this.mFeatureLock = new Object();
        this.mDoubleTapPay = false;
        this.showPowerOffToastTimes = 0;
        this.mWhitelistReceived = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null || context == null || intent.getAction() == null) {
                    Slog.i(HwPhoneWindowManager.TAG, "intent is " + intent + "context is " + context);
                    return;
                }
                if (PhoneWindowManager.IS_NOTCH_PROP && HwPhoneWindowManager.UPDATE_NOTCH_SCREEN_ACTION.equals(intent.getAction())) {
                    String fileName = intent.getStringExtra("uri");
                    Slog.i(HwPhoneWindowManager.TAG, "fileName:" + fileName);
                    if (fileName != null) {
                        HwNotchScreenWhiteConfig.getInstance().updateWhitelistByHot(context, fileName);
                    }
                }
                if (HwPhoneWindowManager.UPDATE_GESTURE_NAV_ACTION.equals(intent.getAction())) {
                    String fileName2 = intent.getStringExtra("uri");
                    Slog.i(HwPhoneWindowManager.TAG, "fileName:" + fileName2);
                    if (fileName2 != null) {
                        HwGestureNavWhiteConfig.getInstance().updateWhitelistByHot(context, fileName2);
                    }
                }
            }
        };
        this.mFoldScreenManagerService = null;
        this.mDesiredRotation = -1;
        this.mMenuClickedOnlyOnce = false;
        this.mOverscanTimeout = new OverscanTimeout();
        this.mSystraceLogVolumeUpKeyTime = 0;
        this.mSystraceLogVolumeUpKeyConsumed = false;
        this.mSystraceLogVolumeUpKeyTriggered = false;
        this.mSystraceLogVolumeDownKeyTriggered = false;
        this.mSystraceLogFingerPrintTime = 0;
        this.mSystraceLogPowerKeyTriggered = false;
        this.mSystraceLogCompleted = true;
        this.mSystraceLogRunnable = new Runnable() {
            public void run() {
                HandlerThread unused = HwPhoneWindowManager.this.systraceLogDialogThread = new HandlerThread("SystraceLogDialog");
                HwPhoneWindowManager.this.systraceLogDialogThread.start();
                Handler unused2 = HwPhoneWindowManager.this.systraceLogDialogHandler = new Handler(HwPhoneWindowManager.this.systraceLogDialogThread.getLooper()) {
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == 0) {
                            AlertDialog unused = HwPhoneWindowManager.this.alertDialog = new AlertDialog.Builder(HwPhoneWindowManager.this.mContext).setTitle(17039380).setMessage(HwPhoneWindowManager.this.mContext.getResources().getQuantityString(34406405, 10, new Object[]{10})).setCancelable(false).create();
                            HwPhoneWindowManager.this.alertDialog.getWindow().setType(2003);
                            HwPhoneWindowManager.this.alertDialog.getWindow().setFlags(128, 128);
                            HwPhoneWindowManager.this.alertDialog.show();
                        } else if (msg.what <= 0 || msg.what >= 10) {
                            HwPhoneWindowManager.this.alertDialog.dismiss();
                        } else {
                            HwPhoneWindowManager.this.alertDialog.setMessage(HwPhoneWindowManager.this.mContext.getResources().getQuantityString(34406405, 10 - msg.what, new Object[]{Integer.valueOf(10 - msg.what)}));
                            TelecomManager telecomManager = (TelecomManager) HwPhoneWindowManager.this.mContext.getSystemService("telecom");
                            if (telecomManager != null && telecomManager.isRinging()) {
                                HwPhoneWindowManager.this.alertDialog.dismiss();
                            }
                        }
                    }
                };
                HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(0);
                new Thread(new Runnable() {
                    public void run() {
                        int i = 1;
                        while (!HwPhoneWindowManager.this.mSystraceLogCompleted && i < 11) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Log.w(HwPhoneWindowManager.TAG, "systrace log not completed,interrupted");
                            }
                            if (HwPhoneWindowManager.this.systraceLogDialogHandler != null && !HwPhoneWindowManager.this.mSystraceLogCompleted) {
                                HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(i);
                            }
                            i++;
                        }
                    }
                }).start();
                new Thread(new Runnable() {
                    public void run() {
                        String str;
                        StringBuilder sb;
                        IBinder sJankService = ServiceManager.getService("jank");
                        if (sJankService != null) {
                            try {
                                Log.d(HwPhoneWindowManager.TAG, "sJankService is not null");
                                Parcel data = Parcel.obtain();
                                Parcel reply = Parcel.obtain();
                                data.writeInterfaceToken("android.os.IJankManager");
                                sJankService.transact(2, data, reply, 0);
                                int result = reply.readInt();
                                Log.d(HwPhoneWindowManager.TAG, "sJankService.transact result = " + result);
                            } catch (RemoteException e) {
                                Log.e(HwPhoneWindowManager.TAG, "sJankService.transact remote exception:" + e.getMessage());
                            } catch (Throwable th) {
                                HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(10);
                                throw th;
                            }
                        }
                        HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(10);
                        try {
                            Thread.sleep(1000);
                            boolean unused = HwPhoneWindowManager.this.mSystraceLogCompleted = true;
                            HwPhoneWindowManager.this.systraceLogDialogThread.quitSafely();
                            str = HwPhoneWindowManager.TAG;
                            sb = new StringBuilder();
                        } catch (InterruptedException e2) {
                            Log.w(HwPhoneWindowManager.TAG, "sJankService transact not completed,interrupted");
                            boolean unused2 = HwPhoneWindowManager.this.mSystraceLogCompleted = true;
                            HwPhoneWindowManager.this.systraceLogDialogThread.quitSafely();
                            str = HwPhoneWindowManager.TAG;
                            sb = new StringBuilder();
                        } catch (Throwable th2) {
                            boolean unused3 = HwPhoneWindowManager.this.mSystraceLogCompleted = true;
                            HwPhoneWindowManager.this.systraceLogDialogThread.quitSafely();
                            Log.d(HwPhoneWindowManager.TAG, "has quit the systraceLogDialogThread" + HwPhoneWindowManager.this.systraceLogDialogThread.getId());
                            throw th2;
                        }
                        sb.append("has quit the systraceLogDialogThread");
                        sb.append(HwPhoneWindowManager.this.systraceLogDialogThread.getId());
                        Log.d(str, sb.toString());
                    }
                }).start();
            }
        };
        this.mScreenRecorderRunnable = new Runnable() {
            public void run() {
                Intent intent = new Intent();
                intent.setAction(HwPhoneWindowManager.HUAWEI_SCREENRECORDER_ACTION);
                intent.setClassName(HwPhoneWindowManager.HUAWEI_SCREENRECORDER_PACKAGE, HwPhoneWindowManager.HUAWEI_SCREENRECORDER_START_MODE);
                HwPhoneWindowManager.this.powerPressBDReport(987);
                try {
                    HwPhoneWindowManager.this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
                } catch (Exception e) {
                    Slog.e(HwPhoneWindowManager.TAG, "unable to start screenrecorder service: " + intent, e);
                }
                Log.d(HwPhoneWindowManager.TAG, "start screen recorder service");
            }
        };
        this.mPowerKeyCount = 0;
        this.mLastPowerKeyDownTime = 0;
        this.mLastVolumeKeyDownTime = 0;
        this.mIsFreezePowerkey = false;
        this.mPowerWalletCount = 0;
        this.mLastPowerWalletDownTime = 0;
        this.mPowerKeyStartWallet = new Runnable() {
            public void run() {
                int unused = HwPhoneWindowManager.this.mPowerWalletCount = 0;
                long unused2 = HwPhoneWindowManager.this.mLastPowerWalletDownTime = 0;
                HwPhoneWindowManager.this.powerPressBDReport(985);
                HwPhoneWindowManager.this.notifyWallet();
            }
        };
        this.isHomePressDown = false;
        this.isHomeAndEBothDown = false;
        this.isHomeAndLBothDown = false;
        this.isHomeAndDBothDown = false;
        this.mLastWakeupTime = 0;
        this.mHandleVolumeDownKey = new Runnable() {
            public void run() {
                if (HwPhoneWindowManager.this.isMusicActive()) {
                    HwPhoneWindowManager.this.handleVolumeKey(3, 25);
                }
            }
        };
        this.mVolumeDownLongPressed = new Runnable() {
            public void run() {
                HwPhoneWindowManager.this.cancelVolumeDownKeyPressed();
                if ((!HwPhoneWindowManager.this.mIsProximity && HwPhoneWindowManager.this.mSensorRegisted) || !HwPhoneWindowManager.this.mSensorRegisted) {
                    HwPhoneWindowManager.this.notifyVassistantService("start", 2, null);
                }
                HwPhoneWindowManager.this.turnOffSensorListener();
                boolean unused = HwPhoneWindowManager.this.isVoiceRecognitionActive = true;
                long unused2 = HwPhoneWindowManager.this.mLastStartVassistantServiceTime = SystemClock.uptimeMillis();
            }
        };
        this.mNavigationBarPolicy = null;
        this.mNavigationBarHeightForRotationMin = new int[4];
        this.mNavigationBarHeightForRotationMax = new int[4];
        this.mNavigationBarWidthForRotationMax = new int[4];
        this.mNavigationBarWidthForRotationMin = new int[4];
        this.mNavigationBarHeightExternal = 0;
        this.mSensorManager = null;
        this.mCoverManager = null;
        this.mCoverOpen = true;
        this.mSensorRegisted = false;
        this.mListener = null;
        this.mIsProximity = false;
        this.mProximitySensorTimeoutRunnable = new Runnable() {
            public void run() {
                Log.i(HwPhoneWindowManager.TAG, "mProximitySensorTimeout, unRegisterListener");
                HwPhoneWindowManager.this.turnOffSensorListener();
            }
        };
        this.mSystemWideActionsListenerPointerEventListener = new WindowManagerPolicyConstants.PointerEventListener() {
            public void onPointerEvent(MotionEvent motionEvent) {
                HwPhoneWindowManager.this.systemWideActionsListener.onPointerEvent(motionEvent);
            }
        };
        this.mCancleInterceptFingerprintEvent = new Runnable() {
            public void run() {
                boolean unused = HwPhoneWindowManager.this.mNeedDropFingerprintEvent = false;
            }
        };
        this.mSmartKeyLongPressed = new Runnable() {
            public void run() {
                HwPhoneWindowManager.this.cancelSmartKeyLongPressed();
                HwPhoneWindowManager.this.notifySmartKeyEvent(HwPhoneWindowManager.SMARTKEY_LP);
            }
        };
        this.mSmartKeyClick = new Runnable() {
            public void run() {
                HwPhoneWindowManager.this.cancelSmartKeyClick();
                HwPhoneWindowManager.this.notifySmartKeyEvent(HwPhoneWindowManager.SMARTKEY_CLICK);
            }
        };
        this.mAIPowerLongPressed = new Runnable() {
            public void run() {
                Log.i(HwPhoneWindowManager.TAG, "handle power long press");
                HwPhoneWindowManager.this.mPowerKeyHandledByHiaction = true;
                HwPhoneWindowManager.this.handleHomoAiKeyLongPress(2);
            }
        };
        this.mAIPowerSuperLongPressed = new Runnable() {
            public void run() {
                HwPhoneWindowManager.this.handleHomoAiKeySuperLongPress(2);
            }
        };
        this.mAIHomoLongPressed = new Runnable() {
            public void run() {
                Log.i(HwPhoneWindowManager.TAG, "handle long click");
                HwPhoneWindowManager.this.handleHomoAiKeyLongPress(1);
            }
        };
        this.mAIHomoSuperLongPressed = new Runnable() {
            public void run() {
                Log.i(HwPhoneWindowManager.TAG, "handle super long click");
                HwPhoneWindowManager.this.handleHomoAiKeySuperLongPress(1);
            }
        };
        this.mPowerOffRunner = new Runnable() {
            public void run() {
                HwPhoneWindowManager.this.powerOffToast();
            }
        };
        this.mIsImmersiveMode = false;
        this.lastDensityDpi = -1;
        this.mNavigationBarExternal = null;
        this.mFaceRotationCallback = new IntelliServiceManager.FaceRotationCallback() {
            public void onEvent(int faceRotation) {
                if (faceRotation == -2) {
                    HwPhoneWindowManager.this.mHandler.post(new PhoneWindowManager.UpdateRunnable(HwPhoneWindowManager.this, HwPhoneWindowManager.this.mScreenRotation));
                } else {
                    HwPhoneWindowManager.this.mHandler.post(new PhoneWindowManager.UpdateRunnable(HwPhoneWindowManager.this, faceRotation));
                }
            }
        };
        this.mProximitySensorEnabled = false;
        this.mProximitySensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    /* JADX WARNING: type inference failed for: r0v17, types: [com.android.server.policy.EasyWakeUpManager, android.os.IBinder] */
    public void systemReady() {
        HwPhoneWindowManager.super.systemReady();
        this.mHandler.post(new Runnable() {
            public void run() {
                HwPhoneWindowManager.this.initQuickcall();
            }
        });
        if (IS_NOTCH_PROP) {
            this.hwNotchScreenWhiteConfig = HwNotchScreenWhiteConfig.getInstance();
            HwFullScreenDisplay.setNotchHeight(this.mContext.getResources().getDimensionPixelSize(17105318));
        }
        this.mHwScreenOnProximityLock = new HwScreenOnProximityLock(this.mContext, this, this.mWindowManagerFuncs);
        if (mIsHwNaviBar) {
            this.mNavigationBarPolicy = new NavigationBarPolicy(this.mContext, this);
            this.mWindowManagerFuncs.registerPointerEventListener(new WindowManagerPolicyConstants.PointerEventListener() {
                public void onPointerEvent(MotionEvent motionEvent) {
                    if (HwPhoneWindowManager.this.mNavigationBarPolicy != null) {
                        HwPhoneWindowManager.this.mNavigationBarPolicy.addPointerEvent(motionEvent);
                    }
                }
            });
        }
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
            ? instance = EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate);
            ServiceManager.addService("easywakeup", instance);
            instance.saveTouchPointNodePath();
        }
        if (this.mListener == null) {
            this.mListener = new ProximitySensorListener();
        }
        this.mResolver = this.mContext.getContentResolver();
        this.TRIKEY_NAVI_DEFAULT_MODE = FrontFingerPrintSettings.getDefaultNaviMode();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
            LightsManager lights = (LightsManager) LocalServices.getService(LightsManager.class);
            this.mButtonLight = lights.getLight(2);
            this.mBackLight = lights.getLight(0);
            if (this.mContext != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.SCREEN_ON");
                this.mContext.registerReceiver(new ScreenBroadcastReceiver(), filter);
            }
        }
        this.mFalseTouchMonitor = new HwFalseTouchMonitor();
        if (mSupportGameAssist) {
            this.mAms = HwActivityManagerService.self();
            if (this.mNavigationBarPolicy != null) {
                this.mNavigationBarPolicy.setHwAms(this.mAms);
            }
            this.mFingerPrintId = SystemProperties.getInt("sys.fingerprint.deviceId", -2);
            IGameObserver.Stub gameObserver = new IGameObserver.Stub() {
                public void onGameStatusChanged(String packageName, int event) {
                    Log.i(HwPhoneWindowManager.TAG, "currentFgApp=" + packageName + ", mLastFgPackageName=" + HwPhoneWindowManager.this.mLastFgPackageName);
                    if (packageName != null && !packageName.equals(HwPhoneWindowManager.this.mLastFgPackageName)) {
                        boolean unused = HwPhoneWindowManager.this.mEnableKeyInCurrentFgGameApp = false;
                        if (HwPhoneWindowManager.this.mNavigationBarPolicy != null) {
                            HwPhoneWindowManager.this.mNavigationBarPolicy.setEnableSwipeInCurrentGameApp(false);
                        }
                    }
                    String unused2 = HwPhoneWindowManager.this.mLastFgPackageName = packageName;
                }

                public void onGameListChanged() {
                }
            };
            if (this.mAms != null) {
                this.mAms.registerGameObserver(gameObserver);
            }
        }
        if (this.mGestureNavManager != null) {
            this.mGestureNavManager.systemReady();
        }
        if (this.mSubScreenViewEntry != null) {
            this.mSubScreenViewEntry.init();
        }
        if (HwPCUtils.enabled()) {
            this.mExternalSystemGestures = new SystemGesturesPointerEventListener(this.mContext, new SystemGesturesPointerEventListener.Callbacks() {
                public void onSwipeFromTop() {
                    if (HwPCUtils.isPcCastModeInServer()) {
                        HwPCUtils.log(HwPhoneWindowManager.TAG, "mExternalSystemGestures onSwipeFromTop");
                        onMouseHoverAtTop();
                    }
                }

                public void onSwipeFromBottom() {
                }

                public void onSwipeFromRight() {
                }

                public void onSwipeFromLeft() {
                }

                public void onFling(int durationMs) {
                }

                public void onDown() {
                }

                public void onUpOrCancel() {
                }

                public void onMouseHoverAtTop() {
                    HwPhoneWindowManager.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            HwPhoneWindowManager.this.showTopBar();
                        }
                    }, 200);
                }

                public void onMouseHoverAtBottom() {
                }

                public void onMouseLeaveFromEdge() {
                }

                public void onDebug() {
                }
            });
            this.mWindowManagerFuncs.registerExternalPointerEventListener(this.mExternalSystemGestures);
            this.mExternalSystemGestures.systemReady();
        }
        if (PickUpWakeScreenManager.isPickupSensorSupport(this.mContext)) {
            PickUpWakeScreenManager.getInstance(this.mContext, this.mHandler, this.mWindowManagerFuncs, this.mKeyguardDelegate).pickUpWakeScreenInit();
        }
        this.mTpDeviceId = getInputDeviceId(MSG_TRIKEY_RECENT_LONG_PRESS);
        Message msg = this.mHandlerEx.obtainMessage(MSG_NOTIFY_FINGER_OPTICAL);
        msg.setAsynchronous(true);
        this.mHandlerEx.sendMessage(msg);
    }

    public void registerExternalPointerEventListener() {
        if (HwPCUtils.enabled() && HwPCUtils.isPcCastModeInServer()) {
            unRegisterExternalPointerEventListener();
            this.mLockScreenListener = new WindowManagerPolicyConstants.PointerEventListener() {
                public void onPointerEvent(MotionEvent motionEvent) {
                    if (motionEvent.getEventTime() - HwPhoneWindowManager.this.mLastKeyPointerTime > 500) {
                        long unused = HwPhoneWindowManager.this.mLastKeyPointerTime = motionEvent.getEventTime();
                        HwPhoneWindowManager.this.userActivityOnDesktop();
                    }
                }
            };
            this.mWindowManagerFuncs.registerExternalPointerEventListener(this.mLockScreenListener);
            this.mLockScreenBuildInDisplayListener = new WindowManagerPolicyConstants.PointerEventListener() {
                public void onPointerEvent(MotionEvent motionEvent) {
                    if (motionEvent.getEventTime() - HwPhoneWindowManager.this.mLastKeyPointerTime > 500) {
                        long unused = HwPhoneWindowManager.this.mLastKeyPointerTime = motionEvent.getEventTime();
                        HwPhoneWindowManager.this.userActivityOnDesktop();
                    }
                }
            };
            this.mWindowManagerFuncs.registerPointerEventListener(this.mLockScreenBuildInDisplayListener);
        }
        if (HwPCUtils.enabledInPad() && HwStylusUtils.hasStylusFeature(this.mContext) && HwPCUtils.isPcCastModeInServer() && this.mStylusGestureListener4PCMode == null) {
            this.mStylusGestureListener4PCMode = new StylusGestureListener(this.mContext, this);
            this.mStylusGestureListener4PCMode.setForPCModeOnly(true);
            Log.i(TAG, "HPWM Set For PC Mode Only Flag");
            this.mWindowManagerFuncs.registerExternalPointerEventListener(this.mStylusGestureListener4PCMode);
        }
    }

    public void unRegisterExternalPointerEventListener() {
        if (HwPCUtils.enabled() && HwPCUtils.isPcCastModeInServer()) {
            if (this.mLockScreenListener != null) {
                this.mWindowManagerFuncs.unregisterExternalPointerEventListener(this.mLockScreenListener);
                this.mLockScreenListener = null;
            }
            if (this.mLockScreenBuildInDisplayListener != null) {
                this.mWindowManagerFuncs.unregisterPointerEventListener(this.mLockScreenBuildInDisplayListener);
                this.mLockScreenBuildInDisplayListener = null;
            }
        }
        if (HwPCUtils.enabledInPad() && HwStylusUtils.hasStylusFeature(this.mContext) && HwPCUtils.isPcCastModeInServer() && this.mStylusGestureListener4PCMode != null) {
            this.mWindowManagerFuncs.unregisterExternalPointerEventListener(this.mStylusGestureListener4PCMode);
            this.mStylusGestureListener4PCMode = null;
        }
    }

    public void addPointerEvent(MotionEvent motionEvent) {
        if (this.mNavigationBarPolicy != null) {
            this.mNavigationBarPolicy.addPointerEvent(motionEvent);
        }
    }

    public void init(Context context, IWindowManager windowManager, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs) {
        this.mHandlerEx = new PolicyHandlerEx();
        this.fingersense_enable = "fingersense_smartshot_enabled";
        this.fingersense_letters_enable = "fingersense_letters_enabled";
        this.line_gesture_enable = "fingersense_multiwindow_enabled";
        this.fingersense_screenrecord_enable = "fingersense_screen_recording_enabled";
        Flog.i(1503, "init fingersense_letters_enable with " + this.fingersense_letters_enable);
        this.navibar_enable = "enable_navbar";
        this.mCurUser = ActivityManager.getCurrentUser();
        this.mResolver = context.getContentResolver();
        this.mFingerprintObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                HwPhoneWindowManager.this.updateFingerprintNav();
            }
        };
        registerFingerprintObserver(this.mCurUser);
        updateFingerprintNav();
        this.mHwOUCObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                HwPhoneWindowManager.this.updateHwOUCKeyguardViewState();
            }
        };
        registerHwOUCObserver(this.mCurUser);
        updateHwOUCKeyguardViewState();
        initDropSmartKey();
        HwPhoneWindowManager.super.init(context, windowManager, windowManagerFuncs);
        this.mGestureNavManager = new GestureNavManager(context);
        if (HwFoldScreenState.isFoldScreenDevice()) {
            this.mSubScreenViewEntry = new SubScreenViewEntry(context);
        }
        this.mKeyguardDelegate.setHwPCKeyguardShowingCallback(new KeyguardStateMonitor.HwPCKeyguardShowingCallback() {
            public void onShowingChanged(boolean showing) {
                if (HwPCUtils.isPcCastModeInServer()) {
                    HwPhoneWindowManager.this.lockScreen(showing);
                }
                if (!showing) {
                    HwPhoneWindowManager.this.mHwScreenOnProximityLock.releaseLock(3);
                }
                if (HwPhoneWindowManager.this.mInputManagerInternal != null) {
                    HwPhoneWindowManager.this.mInputManagerInternal.onKeyguardStateChanged(showing);
                }
                if (HwPhoneWindowManager.this.mGestureNavManager != null) {
                    HwPhoneWindowManager.this.mGestureNavManager.onKeyguardShowingChanged(showing);
                }
            }
        });
        registerNotchListener();
        registerReceivers(context);
        if (this.mAppEyeBackKey != null) {
            this.mAppEyeBackKey.init(null);
        }
        if (this.mAppEyeHomeKey != null) {
            this.mAppEyeHomeKey.init(null);
        }
        initTpKeepParamters();
        mIsSidePowerFpComb = getPowerFpType();
    }

    private boolean getPowerFpType() {
        String[] fpType = SystemProperties.get("ro.config.hw_fp_type").split(",");
        if (fpType.length == 4 && Integer.parseInt(fpType[0]) == 3) {
            return true;
        }
        return false;
    }

    private void registerReceivers(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_NOTCH_SCREEN_ACTION);
        filter.addAction(UPDATE_GESTURE_NAV_ACTION);
        context.registerReceiverAsUser(this.mWhitelistReceived, UserHandle.ALL, filter, UPDATE_NOTCH_SCREEN_PER, null);
    }

    private void registerNotchListener() {
        if (IS_NOTCH_PROP) {
            updateNotchSwitchStatus(true);
            this.mNotchSwitchListener = new HwNotchScreenWhiteConfig.NotchSwitchListener() {
                public void onChange() {
                    HwPhoneWindowManager.this.updateNotchSwitchStatus(false);
                }
            };
            HwNotchScreenWhiteConfig.registerNotchSwitchListener(this.mContext, this.mNotchSwitchListener);
            try {
                ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                    public void onUserSwitching(int newUserId) throws RemoteException {
                        HwPhoneWindowManager.this.updateNotchSwitchStatus(true);
                    }
                }, TAG);
            } catch (RemoteException e) {
                Log.i(TAG, "registerUserSwitchObserver fail", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateNotchSwitchStatus(boolean forceUpdate) {
        boolean oldStatus = this.mIsNotchSwitchOpen;
        boolean z = true;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "display_notch_status", 0, this.mCurUser) != 1) {
            z = false;
        }
        this.mIsNotchSwitchOpen = z;
        if (this.mIsNotchSwitchOpen != oldStatus || forceUpdate) {
            HwNotchScreenWhiteConfig.getInstance().setNotchSwitchStatus(this.mIsNotchSwitchOpen);
        }
    }

    /* access modifiers changed from: private */
    public void updateFingerprintNav() {
        boolean z = false;
        this.isFingerShotCameraOn = Settings.Secure.getIntForUser(this.mResolver, FINGERPRINT_CAMERA_SWITCH, 1, this.mCurUser) == 1;
        this.isFingerStopAlarmOn = Settings.Secure.getIntForUser(this.mResolver, FINGERPRINT_STOP_ALARM, 0, this.mCurUser) == 1;
        if (Settings.Secure.getIntForUser(this.mResolver, FINGERPRINT_ANSWER_CALL, 0, this.mCurUser) == 1) {
            z = true;
        }
        this.isFingerAnswerPhoneOn = z;
    }

    private void registerFingerprintObserver(int userId) {
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor(FINGERPRINT_CAMERA_SWITCH), true, this.mFingerprintObserver, userId);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor(FINGERPRINT_STOP_ALARM), true, this.mFingerprintObserver, userId);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor(FINGERPRINT_ANSWER_CALL), true, this.mFingerprintObserver, userId);
    }

    public void setCurrentUser(int userId, int[] currentProfileIds) {
        this.mCurUser = userId;
        registerFingerprintObserver(userId);
        this.mFingerprintObserver.onChange(true);
        this.mSettingsObserver.registerContentObserver(userId);
        this.mSettingsObserver.onChange(true);
        if (this.fingerprintActionsListener != null) {
            this.fingerprintActionsListener.setCurrentUser(userId);
        }
        if (this.mGestureNavManager != null) {
            this.mGestureNavManager.onUserChanged(userId);
        }
    }

    private void registerHwOUCObserver(int userId) {
        Log.d(TAG, "register HwOUC Observer");
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor(KEY_HWOUC_KEYGUARD_VIEW_ON_TOP), true, this.mHwOUCObserver, userId);
    }

    /* access modifiers changed from: private */
    public void updateHwOUCKeyguardViewState() {
        boolean z = true;
        if (1 != Settings.Secure.getIntForUser(this.mResolver, KEY_HWOUC_KEYGUARD_VIEW_ON_TOP, 0, this.mCurUser)) {
            z = false;
        }
        this.isHwOUCKeyguardViewOnTop = z;
    }

    private boolean supportActivityForbidSpecialKey(int keyCode) {
        if (!this.isHwOUCKeyguardViewOnTop || (3 != keyCode && 4 != keyCode && 187 != keyCode)) {
            return false;
        }
        return true;
    }

    private boolean handleFsmFlipPosture(int keyCode) {
        if (!HwFoldScreenState.isFoldScreenDevice() || keyCode != 707) {
            return false;
        }
        if (this.mFoldScreenManagerService == null) {
            this.mFoldScreenManagerService = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        }
        this.mFoldScreenManagerService.notifyFlip();
        return true;
    }

    public int checkAddPermission(WindowManager.LayoutParams attrs, int[] outAppOp) {
        int type = attrs.type;
        if (type == TOAST_TYPE_COVER_SCREEN || type == 2104) {
            return 0;
        }
        return HwPhoneWindowManager.super.checkAddPermission(attrs, outAppOp);
    }

    public int getLastSystemUiFlags() {
        return this.mLastSystemUiFlags;
    }

    public int getWindowLayerFromTypeLw(int type, boolean canAddInternalSystemWindow) {
        int i;
        switch (type) {
            case 2100:
                return 33;
            case TOAST_TYPE_COVER_SCREEN /*2101*/:
                return 34;
            case 2104:
                return 35;
            case FingerViewController.TYPE_FINGER_VIEW /*2105*/:
                return 34;
            default:
                int ret = HwPhoneWindowManager.super.getWindowLayerFromTypeLw(type, canAddInternalSystemWindow);
                if (ret >= 33) {
                    i = ret + 2;
                } else {
                    i = ret;
                }
                return i;
        }
    }

    public void freezeOrThawRotation(int rotation) {
        this.mDesiredRotation = rotation;
    }

    public boolean rotationHasCompatibleMetricsLw(int orientation, int rotation) {
        if (this.mDesiredRotation != 0) {
            return HwPhoneWindowManager.super.rotationHasCompatibleMetricsLw(orientation, rotation);
        }
        Slog.d(TAG, "desired rotation is rotation 0");
        return true;
    }

    public int rotationForOrientationLw(int orientation, int lastRotation, boolean defaultDisplay) {
        if (isDefaultOrientationForced()) {
            return HwPhoneWindowManager.super.rotationForOrientationLw(orientation, lastRotation, defaultDisplay);
        }
        int desiredRotation = this.mDesiredRotation;
        if (desiredRotation < 0) {
            return HwPhoneWindowManager.super.rotationForOrientationLw(orientation, lastRotation, defaultDisplay);
        }
        Slog.i(TAG, "mDesiredRotation:" + this.mDesiredRotation);
        return desiredRotation;
    }

    public void beginPostLayoutPolicyLw(int displayWidth, int displayHeight) {
        HwPhoneWindowManager.super.beginPostLayoutPolicyLw(displayWidth, displayHeight);
        this.mStatuBarObsecured = false;
    }

    public void applyPostLayoutPolicyLw(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs, WindowManagerPolicy.WindowState attached, WindowManagerPolicy.WindowState imeTarget) {
        HwPhoneWindowManager.super.applyPostLayoutPolicyLw(win, attrs, attached, imeTarget);
        if (win.isVisibleLw() && win.getSurfaceLayer() > this.mStatusBarLayer && isStatusBarObsecuredByWin(win)) {
            this.mStatuBarObsecured = true;
        }
    }

    /* access modifiers changed from: protected */
    public void setHasAcitionBar(boolean hasActionBar) {
        this.mIsHasActionBar = hasActionBar;
    }

    private View getActionBarView(Context context, int theme) {
        context.setTheme(theme);
        View tmp = new View(context);
        int color = HwWidgetFactory.getPrimaryColor(context);
        Slog.d(TAG, "Starting window for " + context.getPackageName() + " ActionBarView color=0x" + Integer.toHexString(color));
        if (HwWidgetUtils.isActionbarBackgroundThemed(this.mContext) || Color.alpha(color) == 0) {
            return null;
        }
        tmp.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        tmp.setBackgroundDrawable(new ColorDrawable(color));
        return tmp;
    }

    private static boolean isMultiSimEnabled() {
        try {
            return MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (NoExtAPIException e) {
            Log.w(TAG, "CoverManagerService->isMultiSimEnabled->NoExtAPIException!");
            return false;
        }
    }

    private boolean isPhoneInCall() {
        if (isMultiSimEnabled()) {
            int phoneCount = MSimTelephonyManager.getDefault().getPhoneCount();
            for (int i = 0; i < phoneCount; i++) {
                if (MSimTelephonyManager.getDefault().getCallState(i) != 0) {
                    return true;
                }
            }
            return false;
        } else if (TelephonyManager.getDefault().getCallState(SubscriptionManager.getDefaultSubscriptionId()) != 0) {
            return true;
        } else {
            return false;
        }
    }

    static ITelephony getTelephonyService() {
        return ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
    }

    public boolean needTurnOff(int why) {
        return !isPhoneInCall() || !(!isKeyguardSecure(this.mCurrentUserId) || why == 3 || why == 6);
    }

    public boolean needTurnOffWithDismissFlag(WindowManagerPolicy.WindowState topAppWin) {
        if (!(topAppWin == null || (topAppWin.getAttrs().flags & 524288) == 0 || isKeyguardSecure(this.mCurrentUserId))) {
            Log.w(TAG, "TurnOffWithDismissFlag " + topAppWin);
        }
        return true;
    }

    public boolean needTurnOffWithDismissFlag() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isWakeKeyWhenScreenOff(int keyCode) {
        if (!mCustUsed) {
            return HwPhoneWindowManager.super.isWakeKeyWhenScreenOff(keyCode);
        }
        for (int i : mUnableWakeKey) {
            if (keyCode == i) {
                return false;
            }
        }
        return true;
    }

    public boolean isWakeKeyFun(int keyCode) {
        if (!mCustBeInit) {
            getKeycodeFromCust();
        }
        if (!mCustUsed) {
            return false;
        }
        for (int i : mUnableWakeKey) {
            if (keyCode == i) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void startDockOrHome(boolean fromHomeKey, boolean awakenFromDreams) {
        if (fromHomeKey) {
            HwInputManagerService.HwInputManagerServiceInternal inputManager = (HwInputManagerService.HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerService.HwInputManagerServiceInternal.class);
            if (inputManager != null) {
                inputManager.notifyHomeLaunching();
            }
        }
        HwPhoneWindowManager.super.startDockOrHome(fromHomeKey, awakenFromDreams);
    }

    private void getKeycodeFromCust() {
        String unableCustomizedWakeKey = null;
        try {
            unableCustomizedWakeKey = SettingsEx.Systemex.getString(this.mContext.getContentResolver(), "unable_wake_up_key");
        } catch (Exception e) {
            Log.e(TAG, "Exception when got name value", e);
        }
        if (unableCustomizedWakeKey != null) {
            String[] unableWakeKeyArray = unableCustomizedWakeKey.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            if (!(unableWakeKeyArray == null || unableWakeKeyArray.length == 0)) {
                mUnableWakeKey = new int[unableWakeKeyArray.length];
                int i = 0;
                while (i < mUnableWakeKey.length) {
                    try {
                        mUnableWakeKey[i] = Integer.parseInt(unableWakeKeyArray[i]);
                        i++;
                    } catch (Exception e2) {
                        Log.e(TAG, "Exception when copy the translated value from sting array to int array", e2);
                    }
                }
                mCustUsed = true;
            }
        }
        mCustBeInit = true;
    }

    public int interceptMotionBeforeQueueingNonInteractive(long whenNanos, int policyFlags) {
        if ((FLOATING_MASK & policyFlags) == 0) {
            return HwPhoneWindowManager.super.interceptMotionBeforeQueueingNonInteractive(whenNanos, policyFlags);
        }
        Slog.i(TAG, "interceptMotionBeforeQueueingNonInteractive policyFlags: " + policyFlags);
        Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getSingleHandState() {
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (windowManagerBinder != null) {
            try {
                data.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(1990, data, reply, 0);
                reply.readException();
                return reply.readInt();
            } catch (RemoteException e) {
                return 0;
            } finally {
                data.recycle();
                reply.recycle();
            }
        } else {
            data.recycle();
            reply.recycle();
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void unlockScreenPinningTest() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getHWStatusBarService() != null) {
                IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
                if (statusBarServiceBinder != null) {
                    Log.d(TAG, "Transact unlockScreenPinningTest to status bar service!");
                    data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    statusBarServiceBinder.transact(111, data, reply, 0);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    public void finishedGoingToSleep(int why) {
        this.mHandler.removeCallbacks(this.mOverscanTimeout);
        this.mHandler.postDelayed(this.mOverscanTimeout, 200);
        HwPhoneWindowManager.super.finishedGoingToSleep(why);
    }

    private void interceptSystraceLog() {
        long now = SystemClock.uptimeMillis();
        Log.d(TAG, "now=" + now + " mSystraceLogVolumeUpKeyTime=" + this.mSystraceLogVolumeUpKeyTime + " mSystraceLogFingerPrintTime=" + this.mSystraceLogFingerPrintTime);
        if (now <= this.mSystraceLogVolumeUpKeyTime + 150 && now <= this.mSystraceLogFingerPrintTime + SYSTRACELOG_FINGERPRINT_EFFECT_DELAY && this.mSystraceLogCompleted) {
            this.mSystraceLogCompleted = false;
            this.mSystraceLogVolumeUpKeyConsumed = true;
            this.mSystraceLogFingerPrintTime = 0;
            this.mSystraceLogVolumeUpKeyTriggered = false;
            this.mScreenRecorderVolumeUpKeyTriggered = false;
            Trace.traceBegin(8, "invoke_systrace_log_dump");
            Jlog.d(MemoryConstant.MSG_COMPRESS_GPU, "HwPhoneWindowManager Systrace triggered");
            Trace.traceEnd(8);
            Log.d(TAG, "Systrace triggered");
            this.mHandler.postDelayed(this.mSystraceLogRunnable, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
        }
    }

    /* access modifiers changed from: private */
    public void powerPressBDReport(int eventId) {
        if (Log.HWINFO) {
            Flog.bdReport(this.mContext, eventId);
        }
    }

    private void interceptScreenRecorder() {
        if (this.mScreenRecorderVolumeUpKeyTriggered && this.mScreenRecorderPowerKeyTriggered && !this.mScreenRecorderVolumeDownKeyTriggered && !SystemProperties.getBoolean("sys.super_power_save", false) && !keyguardIsShowingTq() && checkPackageInstalled(HUAWEI_SCREENRECORDER_PACKAGE)) {
            if (this.mCust != null && !this.mCust.isSosAllowed()) {
                return;
            }
            if (!HWRIDEMODE_FEATURE_SUPPORTED || !SystemProperties.getBoolean("sys.ride_mode", false)) {
                long now = SystemClock.uptimeMillis();
                if (now <= this.mScreenRecorderVolumeUpKeyTime + 150 && now <= this.mScreenRecorderPowerKeyTime + 150) {
                    this.mScreenRecorderVolumeUpKeyConsumed = true;
                    cancelPendingPowerKeyActionForDistouch();
                    this.mHandler.postDelayed(this.mScreenRecorderRunnable, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                }
            }
        }
    }

    private void cancelPendingScreenRecorderAction() {
        this.mHandler.removeCallbacks(this.mScreenRecorderRunnable);
    }

    /* access modifiers changed from: package-private */
    public boolean isVoiceCall() {
        IAudioService audioService = getAudioService();
        boolean z = false;
        if (audioService != null) {
            try {
                int mode = audioService.getMode();
                if (mode == 3 || mode == 2) {
                    z = true;
                }
                return z;
            } catch (RemoteException e) {
                Log.w(TAG, "getMode exception");
            }
        }
        return false;
    }

    private void sendKeyEvent(int keycode) {
        int[] actions = {0, 1};
        for (int keyEvent : actions) {
            long curTime = SystemClock.uptimeMillis();
            KeyEvent ev = new KeyEvent(curTime, curTime, keyEvent, keycode, 0, 0, -1, 0, 8, 257);
            InputManager.getInstance().injectInputEvent(ev, 0);
        }
    }

    private boolean isExcluedScene() {
        String pkgName = ServiceManager.getService("activity").topAppName();
        boolean z = false;
        boolean isSuperPowerMode = SystemProperties.getBoolean("sys.super_power_save", false);
        if (pkgName == null) {
            return false;
        }
        if (pkgName.equals("com.android.deskclock/.alarmclock.LockAlarmFullActivity") || isSuperPowerMode || !this.mDeviceProvisioned || keyguardOn()) {
            z = true;
        }
        return z;
    }

    private boolean isExcluedBackScene() {
        if (this.mTrikeyNaviMode == 1) {
            return isExcluedScene();
        }
        return !this.mDeviceProvisioned;
    }

    private boolean isExcluedRecentScene() {
        if (this.mTrikeyNaviMode == 1) {
            return !this.mDeviceProvisioned;
        }
        return isExcluedScene();
    }

    private boolean isFrontFPGestureNavEnable() {
        return FRONT_FINGERPRINT_GESTURE_NAVIGATION_SUPPORTED && this.mIsGestureNavEnable;
    }

    /* access modifiers changed from: private */
    public void resetButtonLightStatus() {
        if (this.mButtonLight != null) {
            if (!this.mDeviceProvisioned) {
                setButtonLightTimeout(false);
                this.mButtonLight.setBrightness(0);
                return;
            }
            Slog.i(TAG, "resetButtonLightStatus");
            this.mHandlerEx.removeMessages(MSG_BUTTON_LIGHT_TIMEOUT);
            if (this.mTrikeyNaviMode < 0 || isFrontFPGestureNavEnable()) {
                setButtonLightTimeout(false);
                this.mButtonLight.setBrightness(0);
            } else if (this.mButtonLightMode != 0) {
                setButtonLightTimeout(false);
                this.mButtonLight.setBrightness(this.mBackLight.getCurrentBrightness());
            } else if (this.mButtonLight.getCurrentBrightness() > 0) {
                setButtonLightTimeout(false);
                Message msg = this.mHandlerEx.obtainMessage(MSG_BUTTON_LIGHT_TIMEOUT);
                msg.setAsynchronous(true);
                this.mHandlerEx.sendMessageDelayed(msg, 5000);
            } else {
                setButtonLightTimeout(true);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setButtonLightTimeout(boolean timeout) {
        SystemProperties.set("sys.button.light.timeout", String.valueOf(timeout));
    }

    /* access modifiers changed from: private */
    public void sendLightTimeoutMsg() {
        if (this.mButtonLight != null && this.mDeviceProvisioned) {
            this.mHandlerEx.removeMessages(MSG_BUTTON_LIGHT_TIMEOUT);
            if (this.mTrikeyNaviMode < 0 || isFrontFPGestureNavEnable()) {
                setButtonLightTimeout(false);
                this.mButtonLight.setBrightness(0);
            } else {
                int curButtonBrightness = this.mButtonLight.getCurrentBrightness();
                int curBackBrightness = this.mBackLight.getCurrentBrightness();
                if (this.mButtonLightMode == 0) {
                    if (SystemProperties.getBoolean("sys.button.light.timeout", false) && curButtonBrightness == 0) {
                        this.mButtonLight.setBrightness(curBackBrightness);
                    }
                    setButtonLightTimeout(false);
                    Message msg = this.mHandlerEx.obtainMessage(MSG_BUTTON_LIGHT_TIMEOUT);
                    msg.setAsynchronous(true);
                    this.mHandlerEx.sendMessageDelayed(msg, 5000);
                } else if (curButtonBrightness == 0) {
                    this.mButtonLight.setBrightness(curBackBrightness);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void startHwVibrate(int vibrateMode) {
        if (!isKeyguardLocked() && this.mHapticEnabled && !"true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false")) && this.mVibrator != null) {
            Log.d(TAG, "startVibrateWithConfigProp:" + vibrateMode);
            this.mVibrator.vibrate((long) vibrateMode);
        }
    }

    private boolean isMMITesting() {
        return "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false"));
    }

    private void cancelPowerKeyStartWallet() {
        this.mHandler.removeCallbacks(this.mPowerKeyStartWallet);
        Log.i(TAG, "cancelPowerKeyStartWallet");
    }

    /* access modifiers changed from: private */
    public void notifyWallet() {
        Intent intent = new Intent(POWERKEY_QUICKPAY_ACTION);
        intent.setPackage(POWERKEY_QUICKPAY_PACKAGE);
        intent.putExtra("channel", "doubleClickPowerBtn");
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (Exception e) {
            Log.e(TAG, "start wallet server err");
        }
        Log.i(TAG, "notifyWallet");
    }

    private boolean isNeedNotifyWallet() {
        if (!mSupportDoubleTapPay) {
            return false;
        }
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            if (HWFLOW) {
                Log.i(TAG, "PC mode");
            }
            return false;
        } else if (mIsSidePowerFpComb && this.mHwPWMEx.isPowerFpForbidGotoSleep()) {
            if (HWFLOW) {
                Log.d(TAG, "wallet Is Side powerFp comb");
            }
            return false;
        } else if (!this.mDoubleTapPay) {
            return false;
        } else {
            return true;
        }
    }

    private void startWallet(int eventAction) {
        if (isNeedNotifyWallet()) {
            long now = SystemClock.uptimeMillis();
            boolean down = eventAction == 0;
            if (HWFLOW) {
                Log.i(TAG, "Down " + down + " eventAction " + eventAction);
            }
            if (down) {
                Log.i(TAG, "mPowerWalletCount " + this.mPowerWalletCount + " now " + now + " last " + this.mLastPowerWalletDownTime);
                if (this.mPowerWalletCount <= 0 || now - this.mLastPowerWalletDownTime <= 500) {
                    this.mPowerWalletCount++;
                    this.mLastPowerWalletDownTime = now;
                    if (this.mPowerWalletCount == 2) {
                        cancelAIPowerLongPressed();
                        this.mHandler.postDelayed(this.mPowerKeyStartWallet, 500);
                    } else if (this.mPowerWalletCount > 2) {
                        cancelPowerKeyStartWallet();
                    }
                    return;
                }
                this.mPowerWalletCount = 1;
                this.mLastPowerWalletDownTime = now;
            }
        }
    }

    private void wakeupSOSPage(int keycode) {
        if (keycode == 25 || keycode == 24) {
            this.mLastVolumeKeyDownTime = SystemClock.uptimeMillis();
            return;
        }
        if (keycode == 26) {
            long now = SystemClock.uptimeMillis();
            if (this.mPowerKeyCount <= 0 || (now - this.mLastPowerKeyDownTime <= 500 && now - this.mLastVolumeKeyDownTime >= POWER_SOS_MISTOUCH_THRESHOLD)) {
                if (now - this.mLastNotifyWalletTime < 500) {
                    this.mHwPWMEx.cancelWalletSwipe(this.mHandler);
                }
                this.mPowerKeyCount++;
                this.mLastPowerKeyDownTime = now;
                if (this.mPowerKeyCount == 5) {
                    resetSOS();
                    this.mIsFreezePowerkey = false;
                    PowerManager powerManager = (PowerManager) this.mContext.getSystemService(HIVOICE_PRESS_TYPE_POWER);
                    powerPressBDReport(986);
                    try {
                        String pkgName = getTopActivity();
                        Log.d(TAG, "get Emergency power TopActivity is:" + pkgName);
                        if (!"com.android.emergency/.view.ViewCountDownActivity".equals(pkgName) && !"com.android.emergency/.view.EmergencyNumberActivity".equals(pkgName)) {
                            if (!ACTIVITY_NAME_EMERGENCY_SIMPLIFIEDINFO.equals(pkgName)) {
                                Intent intent = new Intent();
                                intent.setPackage(PKG_NAME_EMERGENCY);
                                intent.setAction("android.emergency.COUNT_DOWN");
                                intent.addCategory("android.intent.category.DEFAULT");
                                if (isSoSEmergencyInstalled(intent)) {
                                    this.mIsFreezePowerkey = true;
                                    this.mHandlerEx.removeMessages(MSG_FREEZE_POWER_KEY);
                                    Log.d(TAG, "Emergency power start activity");
                                    this.mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
                                }
                                if (this.mIsFreezePowerkey) {
                                    powerManager.wakeUp(SystemClock.uptimeMillis());
                                    this.mHandlerEx.removeMessages(MSG_FREEZE_POWER_KEY);
                                    Message msg = this.mHandlerEx.obtainMessage(MSG_FREEZE_POWER_KEY);
                                    msg.setAsynchronous(true);
                                    this.mHandlerEx.sendMessageDelayed(msg, 1000);
                                }
                            }
                        }
                        Log.d(TAG, "current topActivity is emergency, return ");
                        if (powerManager != null && !powerManager.isScreenOn()) {
                            powerManager.wakeUp(SystemClock.uptimeMillis());
                        }
                    } catch (ActivityNotFoundException ex) {
                        Log.e(TAG, "ActivityNotFoundException failed message : " + ex);
                        this.mIsFreezePowerkey = false;
                    } catch (Exception e) {
                        Log.e(TAG, "StartActivity Exception : " + e);
                        this.mIsFreezePowerkey = false;
                    }
                }
            } else {
                this.mPowerKeyCount = 1;
                this.mLastPowerKeyDownTime = now;
            }
        }
    }

    private void resetSOS() {
        this.mLastPowerKeyDownTime = 0;
        this.mPowerKeyCount = 0;
    }

    private boolean isSoSEmergencyInstalled(Intent intent) {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null || intent.resolveActivity(packageManager) == null) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        int i;
        boolean isFrontFpNavi;
        boolean isSupportTrikey;
        boolean isMMITest;
        boolean isIntercept;
        boolean isVolumeDownDoubleClick;
        KeyEvent keyEvent = event;
        boolean isPhoneActive = true;
        boolean down = event.getAction() == 0;
        int keyCode = event.getKeyCode();
        int flags = event.getFlags();
        int deviceID = event.getDeviceId();
        if (supportActivityForbidSpecialKey(keyCode)) {
            Log.d(TAG, "has intercept Key for block " + keyCode + ", some  ssssuper activity is on top now.");
            return 0;
        } else if (!down || !handleFsmFlipPosture(keyCode)) {
            if (down) {
                if (keyCode == 4 && this.mAppEyeBackKey != null) {
                    ZrHungData arg = new ZrHungData();
                    arg.putLong("downTime", event.getDownTime());
                    this.mAppEyeBackKey.check(arg);
                }
                if (keyCode == 3 && this.mAppEyeHomeKey != null) {
                    ZrHungData arg2 = new ZrHungData();
                    arg2.putLong("downTime", event.getDownTime());
                    this.mAppEyeHomeKey.check(arg2);
                }
            }
            if (!this.mSystraceLogCompleted) {
                Log.d(TAG, " has intercept Key for block : " + keyCode + ", isdown : " + down + ", flags : " + flags);
                return 0;
            } else if (handleInputEventInPCCastMode(event)) {
                return 0;
            } else {
                if (this.mCust != null) {
                    this.mCust.processCustInterceptKey(keyCode, down, this.mContext);
                }
                Flog.i(WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManager has intercept Key : " + keyCode + ", isdown : " + down + ", flags : " + flags);
                reportMonitorData(keyCode, down);
                HwFreeFormManager.getInstance(this.mContext).removeFloatListView();
                boolean isInjected = (policyFlags & 16777216) != 0;
                wakeupNoteEditor(keyEvent, isInjected);
                if (keyCode == 26 && this.mIsFreezePowerkey) {
                    return 1;
                }
                boolean isScreenOn = (policyFlags & 536870912) != 0;
                if (!(keyCode == 26 || keyCode == 308 || keyCode == 6 || keyCode == 187) || this.mFocusedWindow == null || (this.mFocusedWindow.getAttrs().hwFlags & FLOATING_MASK) != FLOATING_MASK) {
                    if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
                        if (EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate).handleWakeUpKey(keyEvent, isScreenOn ? -1 : this.mScreenOffReason)) {
                            Log.d(TAG, "EasyWakeUpManager has handled the keycode : " + event.getKeyCode());
                            return 0;
                        }
                    }
                    if (down && event.getRepeatCount() == 0 && SystemProperties.get(VIBRATE_ON_TOUCH, "false").equals("true") && ((keyCode == 82 && (268435456 & flags) == 0) || keyCode == 3 || keyCode == 4 || (policyFlags & 2) != 0)) {
                        performHapticFeedbackLw(null, 1, false);
                    }
                    boolean isWakeKey = isWakeKeyFun(keyCode) | ((policyFlags & 1) != 0);
                    if ((!isScreenOn || this.mHeadless) && (!isInjected || isWakeKey)) {
                        i = 0;
                        if (down && isWakeKey) {
                            isWakeKeyWhenScreenOff(keyCode);
                        }
                    } else {
                        i = 1;
                    }
                    int result = i;
                    if (this.mFocusedWindow != null && (this.mFocusedWindow.getAttrs().hwFlags & 8) == 8 && ((keyCode == 25 || keyCode == 24) && "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false")))) {
                        Log.i(TAG, "Prevent hard key volume event to mmi test before queueing.");
                        return result & -2;
                    } else if (lightScreenOnPcMode(keyCode)) {
                        return 0;
                    } else {
                        switch (keyCode) {
                            case 3:
                            case 4:
                            case 187:
                                boolean isScreenOn2 = isScreenOn;
                                boolean isInjected2 = isInjected;
                                int deviceID2 = deviceID;
                                int i2 = flags;
                                int keyCode2 = keyCode;
                                boolean down2 = down;
                                if (down2) {
                                    if (this.mFalseTouchMonitor != null) {
                                        this.mFalseTouchMonitor.handleKeyEvent(keyEvent);
                                    }
                                    if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn2 && !this.mHintShown && (event.getFlags() & 1024) == 0) {
                                        Log.d(TAG, "keycode: " + keyCode2 + " is comsumed by disable touch mode.");
                                        this.mHwScreenOnProximityLock.forceShowHint();
                                        this.mHintShown = true;
                                        break;
                                    }
                                }
                                Flog.i(WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManagerinterceptKeyBeforeQueueing deviceID:" + deviceID2 + " isFrontFpNavi:" + isFrontFpNavi + " isSupportTrikey:" + isSupportTrikey + " isMMITest:" + isMMITest);
                                if (deviceID2 > 0 && isFrontFpNavi && isSupportTrikey && !isMMITest && keyCode2 == 4) {
                                    if (isTrikeyNaviKeycodeFromLON(isInjected2, isExcluedBackScene())) {
                                        return 0;
                                    }
                                    sendLightTimeoutMsg();
                                    if (down2) {
                                        this.mBackTrikeyHandled = false;
                                        Message msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_BACK_LONG_PRESS);
                                        msg.setAsynchronous(true);
                                        this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                                        if (this.mTrikeyNaviMode == 1) {
                                            return 0;
                                        }
                                    } else {
                                        boolean handled = this.mBackTrikeyHandled;
                                        if (!this.mBackTrikeyHandled) {
                                            this.mBackTrikeyHandled = true;
                                            this.mHandlerEx.removeMessages(MSG_TRIKEY_BACK_LONG_PRESS);
                                        }
                                        if (handled) {
                                            return 0;
                                        }
                                        startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                        if (this.mTrikeyNaviMode == 1) {
                                            Flog.bdReport(this.mContext, 16);
                                            sendKeyEvent(187);
                                            return 0;
                                        }
                                    }
                                }
                                if (!this.mHasNavigationBar && keyCode2 == 4 && down2) {
                                    if (!isScreenInLockTaskMode()) {
                                        this.mBackKeyPress = false;
                                        this.mBackKeyPressTime = 0;
                                        break;
                                    } else {
                                        this.mBackKeyPress = true;
                                        this.mBackKeyPressTime = event.getDownTime();
                                        interceptBackandMenuKey();
                                        break;
                                    }
                                }
                            case 24:
                            case 25:
                            case 164:
                                if (!HwDeviceManager.disallowOp(38)) {
                                    if (keyCode != 25) {
                                        if (keyCode == 24) {
                                            if (!down) {
                                                this.mVolumeUpKeyDisTouch = false;
                                                this.mScreenRecorderVolumeUpKeyTriggered = false;
                                                cancelPendingScreenRecorderAction();
                                                this.mSystraceLogVolumeUpKeyTriggered = false;
                                            } else if (this.mHwScreenOnProximityLock == null || !this.mHwScreenOnProximityLock.isShowing() || !isScreenOn || this.mVolumeUpKeyDisTouch || (event.getFlags() & 1024) != 0) {
                                                if (isScreenOn && !this.mScreenRecorderVolumeUpKeyTriggered && (event.getFlags() & 1024) == 0) {
                                                    cancelPendingPowerKeyActionForDistouch();
                                                    this.mScreenRecorderVolumeUpKeyTriggered = true;
                                                    this.mScreenRecorderVolumeUpKeyTime = event.getDownTime();
                                                    this.mScreenRecorderVolumeUpKeyConsumed = false;
                                                    interceptScreenRecorder();
                                                }
                                                Log.d(TAG, "isScreenOn=" + isScreenOn + " mSystraceLogVolumeUpKeyTriggered=" + this.mSystraceLogVolumeUpKeyTriggered + " mScreenRecorderVolumeUpKeyConsumed=" + this.mScreenRecorderVolumeUpKeyConsumed);
                                                if (Jlog.isEnable() && Jlog.isBetaUser() && isScreenOn && !this.mSystraceLogVolumeUpKeyTriggered && !this.mSystraceLogPowerKeyTriggered && !this.mSystraceLogVolumeDownKeyTriggered && !this.mScreenRecorderVolumeUpKeyConsumed && (event.getFlags() & 1024) == 0) {
                                                    this.mSystraceLogVolumeUpKeyTriggered = true;
                                                    this.mSystraceLogVolumeUpKeyTime = event.getDownTime();
                                                    this.mSystraceLogVolumeUpKeyConsumed = false;
                                                    interceptSystraceLog();
                                                    Log.d(TAG, "volumeup process: fingerprint first, then volumeup");
                                                    if (this.mSystraceLogVolumeUpKeyConsumed) {
                                                        return result & -2;
                                                    }
                                                }
                                                if (getTelecommService().isInCall() && (result & 1) == 0 && this.mCust != null && this.mCust.isVolumnkeyWakeup(this.mContext)) {
                                                    this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                                }
                                            } else {
                                                Log.d(TAG, "keycode: KEYCODE_VOLUME_UP is comsumed by disable touch mode.");
                                                this.mVolumeUpKeyDisTouch = true;
                                                this.mVolumeUpKeyDisTouchTime = event.getDownTime();
                                                this.mVolumeUpKeyConsumedByDisTouch = false;
                                                if (!this.mHintShown) {
                                                    this.mHwScreenOnProximityLock.forceShowHint();
                                                    this.mHintShown = true;
                                                }
                                                cancelPendingPowerKeyActionForDistouch();
                                                interceptTouchDisableMode();
                                            }
                                            if (this.mCust != null) {
                                                HwCustPhoneWindowManager hwCustPhoneWindowManager = this.mCust;
                                                Context context = this.mContext;
                                                boolean keyguardIsShowingTq = keyguardIsShowingTq();
                                                if (!isMusicActive() && !isVoiceCall()) {
                                                    isPhoneActive = false;
                                                }
                                                int deviceID3 = deviceID;
                                                boolean z = keyguardIsShowingTq;
                                                int i3 = flags;
                                                boolean z2 = isPhoneActive;
                                                int keyCode3 = keyCode;
                                                boolean z3 = down;
                                                if (!hwCustPhoneWindowManager.interceptVolumeUpKey(keyEvent, context, isScreenOn, z, z2, isInjected, down)) {
                                                    int i4 = keyCode3;
                                                    int i5 = deviceID3;
                                                    break;
                                                } else {
                                                    return result;
                                                }
                                            }
                                        }
                                        boolean z4 = isInjected;
                                        int i6 = flags;
                                        boolean z5 = down;
                                        int i7 = deviceID;
                                        int i8 = keyCode;
                                        break;
                                    } else {
                                        if (!down) {
                                            this.mVolumeDownKeyDisTouch = false;
                                            this.mScreenRecorderVolumeDownKeyTriggered = false;
                                            cancelPendingScreenRecorderAction();
                                            this.mSystraceLogVolumeDownKeyTriggered = false;
                                        } else if (this.mHwScreenOnProximityLock == null || !this.mHwScreenOnProximityLock.isShowing() || !isScreenOn || this.mVolumeDownKeyDisTouch || (event.getFlags() & 1024) != 0) {
                                            if (isScreenOn && !this.mScreenRecorderVolumeDownKeyTriggered && (event.getFlags() & 1024) == 0) {
                                                cancelPendingPowerKeyActionForDistouch();
                                                this.mScreenRecorderVolumeDownKeyTriggered = true;
                                                cancelPendingScreenRecorderAction();
                                            }
                                            if (isScreenOn && !this.mSystraceLogVolumeDownKeyTriggered && (event.getFlags() & 1024) == 0) {
                                                this.mSystraceLogVolumeDownKeyTriggered = true;
                                                this.mSystraceLogFingerPrintTime = 0;
                                                this.mSystraceLogVolumeUpKeyTriggered = false;
                                            }
                                        } else {
                                            Log.d(TAG, "keycode: KEYCODE_VOLUME_DOWN is comsumed by disable touch mode.");
                                            this.mVolumeDownKeyDisTouch = true;
                                            if (!this.mHintShown) {
                                                this.mHwScreenOnProximityLock.forceShowHint();
                                                this.mHintShown = true;
                                            }
                                        }
                                        boolean keyguardShow = keyguardIsShowingTq();
                                        Log.d(TAG, "interceptVolumeDownKey down=" + down + " keyguardShow=" + keyguardShow + " policyFlags=" + Integer.toHexString(policyFlags));
                                        if ((!isScreenOn || keyguardShow) && !isInjected && (event.getFlags() & 1024) == 0) {
                                            if (!isDeviceProvisioned()) {
                                                Log.i(TAG, "Device is not Provisioned");
                                            } else if (down) {
                                                boolean isVoiceCall = isVoiceCall();
                                                boolean isMusicOrFMOrVoiceCallActive = isMusicActive() || isVoiceCall;
                                                if (!isMusicActive() && isPhoneIdle() && !isVoiceCall) {
                                                    isPhoneActive = false;
                                                }
                                                if (this.isVoiceRecognitionActive) {
                                                    isIntercept = false;
                                                    long interval = event.getEventTime() - this.mLastStartVassistantServiceTime;
                                                    if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MAX) {
                                                        this.isVoiceRecognitionActive = false;
                                                    } else if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MIN) {
                                                        this.isVoiceRecognitionActive = AudioSystem.isSourceActive(6);
                                                    }
                                                } else {
                                                    isIntercept = false;
                                                }
                                                Log.i(TAG, "isMusicOrFMOrVoiceCallActive=" + isMusicOrFMOrVoiceCallActive + " isVoiceRecognitionActive=" + this.isVoiceRecognitionActive);
                                                if (isMusicOrFMOrVoiceCallActive || this.isVoiceRecognitionActive || SystemProperties.getBoolean("sys.super_power_save", false)) {
                                                    boolean z6 = isVoiceCall;
                                                    isVolumeDownDoubleClick = false;
                                                } else {
                                                    boolean z7 = isVoiceCall;
                                                    long timediff = event.getEventTime() - this.mLastVolumeDownKeyDownTime;
                                                    this.mLastVolumeDownKeyDownTime = event.getEventTime();
                                                    if (timediff < 400) {
                                                        isVolumeDownDoubleClick = true;
                                                        if (this.mListener == null) {
                                                            this.mListener = new ProximitySensorListener();
                                                        }
                                                        turnOnSensorListener();
                                                        if ((this.mIsProximity || !this.mSensorRegisted) && this.mSensorRegisted) {
                                                        } else {
                                                            StringBuilder sb = new StringBuilder();
                                                            boolean z8 = keyguardShow;
                                                            sb.append("mIsProximity ");
                                                            sb.append(this.mIsProximity);
                                                            sb.append(", mSensorRegisted ");
                                                            sb.append(this.mSensorRegisted);
                                                            Log.i(TAG, sb.toString());
                                                            notifyRapidCaptureService("start");
                                                        }
                                                        turnOffSensorListener();
                                                        result &= -2;
                                                    } else {
                                                        notifyRapidCaptureService("wakeup");
                                                        if (this.mListener == null) {
                                                            this.mListener = new ProximitySensorListener();
                                                        }
                                                        turnOnSensorListener();
                                                        isVolumeDownDoubleClick = false;
                                                    }
                                                    if (!isScreenOn || isVolumeDownDoubleClick) {
                                                        isIntercept = true;
                                                    }
                                                }
                                                if (!isPhoneActive && !isScreenOn && !isVolumeDownDoubleClick && checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE)) {
                                                    wakeupVassistantService(event);
                                                    if (this.mListener == null) {
                                                        this.mListener = new ProximitySensorListener();
                                                    }
                                                    turnOnSensorListener();
                                                    interceptQuickCallChord();
                                                    isIntercept = true;
                                                }
                                                boolean isIntercept2 = isIntercept;
                                                StringBuilder sb2 = new StringBuilder();
                                                sb2.append("intercept volume down key, isIntercept=");
                                                sb2.append(isIntercept2);
                                                sb2.append(" now=");
                                                boolean z9 = isVolumeDownDoubleClick;
                                                sb2.append(SystemClock.uptimeMillis());
                                                sb2.append(" EventTime=");
                                                sb2.append(event.getEventTime());
                                                Log.i(TAG, sb2.toString());
                                                if (isInterceptAndCheckRinging(isIntercept2)) {
                                                    return result;
                                                }
                                                if (getTelecommService().isInCall() && (result & 1) == 0 && this.mCust != null && this.mCust.isVolumnkeyWakeup(this.mContext)) {
                                                    this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                                }
                                            } else {
                                                if (event.getEventTime() - event.getDownTime() >= 500) {
                                                    resetVolumeDownKeyLongPressed();
                                                } else {
                                                    cancelPendingQuickCallChordAction();
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (down) {
                                        this.mHandler.post(new Runnable() {
                                            public void run() {
                                                Toast toast = Toast.makeText(HwPhoneWindowManager.this.mContext, 33685973, 0);
                                                toast.getWindowParams().type = HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL;
                                                toast.getWindowParams().privateFlags |= 16;
                                                toast.show();
                                            }
                                        });
                                    }
                                    return result & -2;
                                }
                                break;
                            case 26:
                                cancelSmartKeyLongPressed();
                                notifyPowerKeyEventToHiAction(this.mContext, keyEvent);
                                if (down) {
                                    setIsNeedNotifyWallet(isNeedNotifyWallet());
                                    this.mPowerOffToastShown = false;
                                    showPowerOffToast(isScreenOn);
                                    powerPressBDReport(980);
                                    if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mPowerKeyDisTouch && (event.getFlags() & 1024) == 0) {
                                        this.mPowerKeyDisTouch = true;
                                        this.mPowerKeyDisTouchTime = event.getDownTime();
                                        interceptTouchDisableMode();
                                    }
                                    if (isScreenOn && !this.mScreenRecorderPowerKeyTriggered && (event.getFlags() & 1024) == 0) {
                                        this.mScreenRecorderPowerKeyTriggered = true;
                                        this.mScreenRecorderPowerKeyTime = event.getDownTime();
                                        interceptScreenRecorder();
                                    }
                                    if (isScreenOn && !this.mSystraceLogPowerKeyTriggered && (event.getFlags() & 1024) == 0) {
                                        this.mSystraceLogPowerKeyTriggered = true;
                                        this.mSystraceLogFingerPrintTime = 0;
                                        this.mSystraceLogVolumeUpKeyTriggered = false;
                                    }
                                } else {
                                    powerPressBDReport(981);
                                    this.mPowerKeyDisTouch = false;
                                    this.mScreenRecorderPowerKeyTriggered = false;
                                    cancelPendingScreenRecorderAction();
                                    this.mSystraceLogPowerKeyTriggered = false;
                                    cancelPowerOffToast();
                                }
                                break;
                            case 82:
                                if (!this.mHasNavigationBar && down) {
                                    if (isScreenInLockTaskMode()) {
                                        this.mMenuKeyPress = true;
                                        this.mMenuKeyPressTime = event.getDownTime();
                                        interceptBackandMenuKey();
                                    } else {
                                        this.mMenuKeyPress = false;
                                        this.mMenuKeyPressTime = 0;
                                    }
                                }
                                break;
                            case MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE /*308*/:
                                Log.i(TAG, "KeyEvent.KEYCODE_SMARTKEY in");
                                if (down) {
                                    if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mHintShown && (event.getFlags() & 1024) == 0) {
                                        Log.d(TAG, "keycode: " + keyCode + " is comsumed by disable touch mode.");
                                        this.mHwScreenOnProximityLock.forceShowHint();
                                        this.mHintShown = true;
                                        return 0;
                                    }
                                } else if (this.mHintShown) {
                                    this.mHintShown = false;
                                    return 0;
                                }
                                if (!isScreenOn) {
                                    handleSmartKey(this.mContext, keyEvent, this.mHandler, isScreenOn);
                                    return 0;
                                }
                                break;
                            case AwareJobSchedulerService.MSG_JOB_EXPIRED:
                            case AwareJobSchedulerService.MSG_CHECK_JOB:
                            case AwareJobSchedulerService.MSG_REMOVE_JOB:
                            case AwareJobSchedulerService.MSG_CONTROLLER_CHANGED:
                            case 405:
                                processing_KEYCODE_SOUNDTRIGGER_EVENT(keyCode, this.mContext, isMusicActive(), down, keyguardIsShowingTq());
                                break;
                            case 501:
                            case 502:
                            case 511:
                            case 512:
                            case 513:
                            case 514:
                            case WifiProCommonUtils.RESP_CODE_UNSTABLE /*601*/:
                                Log.d(TAG, "event.flags=" + flags + " previous mSystraceLogFingerPrintTime=" + this.mSystraceLogFingerPrintTime);
                                if (flags == 8) {
                                    if (!Jlog.isEnable() || !Jlog.isBetaUser() || !down || !isScreenOn || this.mSystraceLogPowerKeyTriggered || this.mSystraceLogVolumeDownKeyTriggered) {
                                        return result & -2;
                                    }
                                    this.mSystraceLogFingerPrintTime = event.getDownTime();
                                    return result & -2;
                                }
                                break;
                            case 702:
                                handleGameEvent(event);
                                break;
                            case 708:
                            case 709:
                                setTpKeep(keyCode, down);
                                break;
                            default:
                                boolean z10 = isScreenOn;
                                boolean z11 = isInjected;
                                int i9 = deviceID;
                                int i10 = flags;
                                int i11 = keyCode;
                                boolean z12 = down;
                                break;
                        }
                        return HwPhoneWindowManager.super.interceptKeyBeforeQueueing(event, policyFlags);
                    }
                } else {
                    Log.i(TAG, "power & smartkey & endcall key received and passsing to user.");
                    return 1;
                }
            }
        } else {
            Log.i(TAG, "Fsm_common the flip key has been processed keycode:" + keyCode);
            return 0;
        }
    }

    private void reportMonitorData(int keyCode, boolean down) {
        TurnOnWakeScreenManager turnOnWakeScreenManager = TurnOnWakeScreenManager.getInstance(this.mContext);
        if (keyCode == 26 && !down && turnOnWakeScreenManager != null && turnOnWakeScreenManager.isTurnOnSensorSupport()) {
            TurnOnWakeScreenManager.getInstance(this.mContext).reportMonitorData("action=pressPowerKey");
        }
    }

    private boolean getIsSwich() {
        return isSwich;
    }

    private void setIsSwich(boolean isSwich2) {
        isSwich = isSwich2;
    }

    private boolean lightScreenOnPcMode(int keyCode) {
        boolean isDreaming = this.mPowerManagerInternal.isUserActivityScreenDimOrDream();
        if (HwPCUtils.isPcCastModeInServer() || HwPCUtils.getPhoneDisplayID() != -1) {
            if (keyCode == 601) {
                setIsSwich(isDreaming);
            }
            if (getIsSwich() && (keyCode == 3 || keyCode == 4)) {
                isSwich = false;
                return true;
            }
        }
        if ((HwPCUtils.isPcCastModeInServer() || HwPCUtils.getPhoneDisplayID() != -1) && (keyCode == 26 || keyCode == 502 || keyCode == 511 || keyCode == 512 || keyCode == 513 || keyCode == 514 || keyCode == 501 || keyCode == 601 || keyCode == 515)) {
            boolean keyHandled = false;
            try {
                IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
                if (pcMgr != null && !pcMgr.isScreenPowerOn()) {
                    HwPCUtils.log(TAG, "some key set screen from OFF to ON");
                    pcMgr.setScreenPower(true);
                    if (!(HwPCUtils.getPhoneDisplayID() != -1 && isScreenLocked() && keyCode == 26)) {
                        keyHandled = true;
                        if (keyCode == 26) {
                            cancelPendingPowerKeyActionForDistouch();
                        }
                    }
                }
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "lightScreenOnPcMode " + e);
                HwPCDataReporter.getInstance().reportFailLightScreen(1, keyCode, "");
            }
            if (isDreaming || keyHandled) {
                this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isRinging() {
        TelecomManager telecomManager = getTelecommService();
        return telecomManager != null && telecomManager.isRinging() && "1".equals(SystemProperties.get("persist.sys.show_incallscreen", "0"));
    }

    public KeyEvent dispatchUnhandledKey(WindowManagerPolicy.WindowState win, KeyEvent event, int policyFlags) {
        int keyCode = event.getKeyCode();
        boolean isScreenOn = (536870912 & policyFlags) != 0;
        if (keyCode != 308) {
            return HwPhoneWindowManager.super.dispatchUnhandledKey(win, event, policyFlags);
        }
        if (event.getRepeatCount() != 0) {
            Log.d(TAG, "event.getRepeatCount() != 0 so just break");
            return null;
        } else if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", false) && needDropSmartKey()) {
            return null;
        } else {
            handleSmartKey(this.mContext, event, this.mHandler, isScreenOn);
            return null;
        }
    }

    private boolean isHardwareKeyboardConnected() {
        Log.i(TAG, "isHardwareKeyboardConnected--begin");
        int[] devices = InputDevice.getDeviceIds();
        boolean isConnected = false;
        int i = 0;
        while (true) {
            if (i >= devices.length) {
                break;
            }
            InputDevice device = InputDevice.getDevice(devices[i]);
            if (device != null) {
                if (device.getProductId() != 4817 || device.getVendorId() != 1455) {
                    if (device.isExternal() && (device.getSources() & 257) != 0) {
                        isConnected = true;
                        break;
                    }
                } else {
                    isConnected = true;
                    break;
                }
            }
            i++;
        }
        Log.i(TAG, "isHardwareKeyboardConnected--end");
        return isConnected;
    }

    private boolean isRightKey(int keyCode) {
        if ((keyCode < 7 || keyCode > 16) && (keyCode < 29 || keyCode > 54)) {
            return false;
        }
        return true;
    }

    private void setToolType() {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && this.mStylusGestureListener4PCMode != null) {
            this.mStylusGestureListener4PCMode.setToolType();
        } else if (this.mStylusGestureListener != null) {
            this.mStylusGestureListener.setToolType();
        }
    }

    private void handleGameEvent(KeyEvent event) {
        if (!(event.getAction() == 0) && event.getEventTime() - event.getDownTime() <= 500) {
            Slog.d(TAG, "gamekey arrive, notify GameAssist");
            HwGameAssistManager.notifyKeyEvent();
        }
    }

    private void wakeupNoteEditor(KeyEvent event, boolean isInjected) {
        if (!this.mSystemReady) {
            Log.d(TAG, "system not ready, return");
        } else if (!isInjected) {
            int keyCode = event.getKeyCode();
            boolean isWakeupTimeout = false;
            boolean down = event.getAction() == 0;
            if (keyCode != 704 || !down) {
                if (keyCode == 705 && down) {
                    Log.d(TAG, "recieved KEYCODE_STYLUS_POWERON");
                    PowerManager powerManager = (PowerManager) this.mContext.getSystemService(HIVOICE_PRESS_TYPE_POWER);
                    if (powerManager != null) {
                        if (!powerManager.isScreenOn()) {
                            powerManager.wakeUp(SystemClock.uptimeMillis());
                        } else {
                            if (Settings.Global.getInt(this.mContext.getContentResolver(), "stylus_state_activate", 0) == 1) {
                                isWakeupTimeout = true;
                            }
                            if (!isWakeupTimeout) {
                                Settings.Global.putInt(this.mContext.getContentResolver(), "stylus_state_activate", 1);
                                Log.d(TAG, "recieve stylus signal and activate stylus.");
                            }
                        }
                    }
                } else if (!this.mDeviceProvisioned) {
                    Log.d(TAG, "Device not Provisioned, return");
                } else {
                    if (down) {
                        wakeupSOSPage(keyCode);
                        if (this.mIsFreezePowerkey && keyCode == 26) {
                            powerPressBDReport(980);
                        }
                    } else if (this.mIsFreezePowerkey && keyCode == 26) {
                        powerPressBDReport(981);
                    }
                }
            } else if (!this.mDeviceProvisioned) {
                Log.d(TAG, "Device not Provisioned, return");
            } else {
                if (SystemClock.uptimeMillis() - this.mLastWakeupTime >= 500) {
                    isWakeupTimeout = true;
                }
                PowerManager power = (PowerManager) this.mContext.getSystemService(HIVOICE_PRESS_TYPE_POWER);
                if (!power.isScreenOn() && isWakeupTimeout) {
                    Log.d(TAG, "wakeup screen and NoteEditor");
                    Intent notePadEditorIntent = new Intent("android.huawei.intent.action.note.handwriting");
                    notePadEditorIntent.setPackage(PKG_HWNOTEPAD);
                    try {
                        String pkgName = getTopActivity();
                        Log.d(TAG, "getTopActivity NoteEditor is:" + pkgName);
                        if (pkgName == null || !pkgName.equals(NOTEEDITOR_ACTIVITY_NAME)) {
                            this.mContext.startActivityAsUser(notePadEditorIntent, UserHandle.CURRENT_OR_SELF);
                        } else {
                            power.wakeUp(SystemClock.uptimeMillis());
                        }
                    } catch (ActivityNotFoundException ex) {
                        Log.e(TAG, "startActivity failed message : " + ex.getMessage());
                    } catch (Exception e) {
                        Log.e(TAG, "startActivityAsUser(): Exception = " + e);
                    }
                    this.mLastWakeupTime = SystemClock.uptimeMillis();
                }
            }
        }
    }

    private boolean setGSensorEnabled(int keyCode, boolean down, int deviceID) {
        boolean z = false;
        if (keyCode != 703 || deviceID != this.mTpDeviceId) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("keyCode=");
        sb.append(keyCode);
        sb.append(" down=");
        sb.append(down);
        sb.append(" isFromTP=");
        if (deviceID == this.mTpDeviceId) {
            z = true;
        }
        sb.append(z);
        Log.i(TAG, sb.toString());
        if (down) {
            TurnOnWakeScreenManager.getInstance(this.mContext).setGSensorEnabled(true);
        }
        return true;
    }

    public long interceptKeyBeforeDispatching(WindowManagerPolicy.WindowState win, KeyEvent event, int policyFlags) {
        int i;
        KeyEvent keyEvent = event;
        int i2 = policyFlags;
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int flags = event.getFlags();
        boolean down = event.getAction() == 0;
        int deviceID = event.getDeviceId();
        boolean isInjected = (16777216 & i2) != 0;
        Flog.i(WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManagerinterceptKeyTi keyCode=" + keyCode + " down=" + down + " repeatCount=" + repeatCount + " isInjected=" + isInjected);
        if (setGSensorEnabled(keyCode, down, deviceID)) {
            return -1;
        }
        if (HwPCUtils.isPcCastModeInServer() && event.getEventTime() - this.mLastKeyPointerTime > 500) {
            this.mLastKeyPointerTime = event.getEventTime();
            userActivityOnDesktop();
        }
        if (handleInputEventInPCCastMode(keyEvent)) {
            return -1;
        }
        try {
            if (this.mIHwWindowCallback != null) {
                this.mIHwWindowCallback.interceptKeyBeforeDispatching(keyEvent, i2);
            }
        } catch (Exception ex) {
            Log.w(TAG, "mIHwWindowCallback interceptKeyBeforeDispatching threw RemoteException", ex);
        }
        int singleAppResult = getSingAppKeyEventResult(keyCode);
        if (-2 != singleAppResult) {
            return (long) singleAppResult;
        }
        int result = getDisabledKeyEventResult(keyCode);
        if (-2 != result) {
            if (down) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(HwPhoneWindowManager.this.mContext, 33686096, 0);
                        toast.getWindowParams().type = HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL;
                        toast.getWindowParams().privateFlags |= 16;
                        toast.show();
                    }
                });
            }
            return (long) result;
        }
        if (isRightKey(keyCode) && isHardwareKeyboardConnected()) {
            String lastIme = Settings.Secure.getString(this.mContext.getContentResolver(), "default_input_method");
            if (lastIme != null && lastIme.contains("com.visionobjects.stylusmobile.v3_2_huawei")) {
                HwInputMethodManager.setDefaultIme("");
                setToolType();
            }
        }
        int result1 = getGameControlKeyReslut(keyEvent);
        if (-2 != result1) {
            Log.i(TAG, "getGameControlKeyReslut return !");
            return (long) result1;
        } else if ((keyCode == 3 || keyCode == 187) && win != null && (win.getAttrs().hwFlags & FLOATING_MASK) == FLOATING_MASK) {
            return 0;
        } else {
            if (keyCode == 82 && !this.mHasNavigationBar && (268435456 & flags) == 0) {
                if (!down) {
                    if (this.mMenuClickedOnlyOnce) {
                        this.mMenuClickedOnlyOnce = false;
                        sendHwMenuKeyEvent();
                    }
                    cancelPreloadRecentApps();
                } else if (repeatCount == 0) {
                    this.mMenuClickedOnlyOnce = true;
                    preloadRecentApps();
                } else if (repeatCount == 1) {
                    this.mMenuClickedOnlyOnce = false;
                    toggleRecentApps();
                }
                return -1;
            } else if (!this.mVolumeUpKeyDisTouch || this.mPowerKeyDisTouch || (flags & 1024) != 0) {
                if (keyCode == 24) {
                    if (this.mVolumeUpKeyConsumedByDisTouch) {
                        if (!down) {
                            this.mVolumeUpKeyConsumedByDisTouch = false;
                            this.mHintShown = false;
                        }
                        return -1;
                    } else if (this.mHintShown) {
                        if (!down) {
                            this.mHintShown = false;
                        }
                        return -1;
                    }
                }
                if (isNeedPassEventToCloud(keyCode)) {
                    return 0;
                }
                boolean handleResult = handleDesktopKeyEvent(keyEvent);
                if (handleResult) {
                    return -1;
                }
                if (!(keyCode == 25 || keyCode == 187)) {
                    switch (keyCode) {
                        case 3:
                        case 4:
                            break;
                        default:
                            int i3 = singleAppResult;
                            int i4 = repeatCount;
                            boolean z = handleResult;
                            break;
                    }
                }
                if (this.mHintShown) {
                    if (!down) {
                        this.mHintShown = false;
                    }
                    return -1;
                }
                boolean isFrontFpNavi = FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION;
                boolean isSupportTrikey = FrontFingerPrintSettings.isSupportTrikey();
                boolean isMMITest = isMMITesting();
                StringBuilder sb = new StringBuilder();
                int i5 = singleAppResult;
                sb.append("HwPhoneWindowManagerdeviceID:");
                sb.append(deviceID);
                sb.append(" isFrontFpNavi:");
                sb.append(isFrontFpNavi);
                sb.append(" isSupportTrikey:");
                sb.append(isSupportTrikey);
                sb.append(" isMMITest:");
                sb.append(isMMITest);
                Flog.i(WifiProCommonUtils.RESP_CODE_INVALID_URL, sb.toString());
                if (deviceID <= 0 || !isFrontFpNavi || !isSupportTrikey || isMMITest || keyCode != 187) {
                    boolean z2 = handleResult;
                    if ((flags & 1024) == 0) {
                        if (this.mScreenRecorderVolumeUpKeyTriggered && !this.mScreenRecorderPowerKeyTriggered) {
                            long now = SystemClock.uptimeMillis();
                            long timeoutTime = this.mScreenRecorderVolumeUpKeyTime + 150;
                            if (now < timeoutTime) {
                                return timeoutTime - now;
                            }
                        }
                        if (keyCode != 24 || !this.mScreenRecorderVolumeUpKeyConsumed) {
                            if (this.mSystraceLogVolumeUpKeyTriggered) {
                                long now2 = SystemClock.uptimeMillis();
                                long timeoutTime2 = this.mSystraceLogVolumeUpKeyTime + 150;
                                if (now2 < timeoutTime2) {
                                    Log.d(TAG, "keyCode=" + keyCode + " down=" + down + " in queue: now=" + now2 + " timeout=" + timeoutTime2);
                                    return timeoutTime2 - now2;
                                }
                            }
                            if (keyCode == 24 && this.mSystraceLogVolumeUpKeyConsumed) {
                                if (!down) {
                                    this.mSystraceLogVolumeUpKeyConsumed = false;
                                }
                                Log.d(TAG, "systracelog volumeup down=" + down + " leave queue");
                                return -1;
                            }
                        } else {
                            if (!down) {
                                this.mScreenRecorderVolumeUpKeyConsumed = false;
                            }
                            return -1;
                        }
                    }
                    return HwPhoneWindowManager.super.interceptKeyBeforeDispatching(win, event, policyFlags);
                } else if (isTrikeyNaviKeycodeFromLON(isInjected, isExcluedRecentScene())) {
                    return -1;
                } else {
                    sendLightTimeoutMsg();
                    if (!down) {
                        boolean z3 = handleResult;
                        boolean z4 = isFrontFpNavi;
                        int repeatCount2 = this.mRecentTrikeyHandled;
                        if (!this.mRecentTrikeyHandled) {
                            i = 1;
                            this.mRecentTrikeyHandled = true;
                            this.mHandlerEx.removeMessages(MSG_TRIKEY_RECENT_LONG_PRESS);
                        } else {
                            i = 1;
                        }
                        if (repeatCount2 == 0) {
                            if (this.mTrikeyNaviMode == i) {
                                startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                sendKeyEvent(4);
                            } else if (this.mTrikeyNaviMode == 0) {
                                Flog.bdReport(this.mContext, 17);
                                startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                toggleRecentApps();
                            }
                        }
                    } else if (repeatCount == 0) {
                        this.mRecentTrikeyHandled = false;
                        Message msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_RECENT_LONG_PRESS);
                        msg.setAsynchronous(true);
                        int i6 = repeatCount;
                        boolean z5 = handleResult;
                        boolean z6 = isFrontFpNavi;
                        this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                        if (this.mTrikeyNaviMode == 0) {
                            preloadRecentApps();
                        }
                    } else {
                        boolean z7 = handleResult;
                        boolean z8 = isFrontFpNavi;
                    }
                    return -1;
                }
            } else {
                long now3 = SystemClock.uptimeMillis();
                int i7 = result;
                long timeoutTime3 = this.mVolumeUpKeyDisTouchTime + 150;
                if (now3 < timeoutTime3) {
                    return timeoutTime3 - now3;
                }
                return -1;
            }
        }
    }

    private int getInputDeviceId(int inputSource) {
        for (int devId : InputDevice.getDeviceIds()) {
            if (InputDevice.getDevice(devId).supportsSource(inputSource)) {
                return devId;
            }
        }
        return 0;
    }

    private void initTpKeepParamters() {
        boolean ret = false;
        try {
            IServiceManager serviceManager = IServiceManager.getService();
            if (serviceManager != null) {
                ret = serviceManager.registerForNotifications("vendor.huawei.hardware.tp@1.0::ITouchscreen", "", this.mServiceNotification);
            }
            if (!ret) {
                Slog.e(TAG, "Failed to register service start notification");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to register service start notification", e);
        }
        connectToProxy();
    }

    /* access modifiers changed from: private */
    public void connectToProxy() {
        synchronized (this.mLock) {
            if (this.mTpTouchSwitch == null) {
                try {
                    this.mTpTouchSwitch = ITouchscreen.getService();
                    if (this.mTpTouchSwitch != null) {
                        this.mTpTouchSwitch.linkToDeath(new DeathRecipient(), 1001);
                    }
                } catch (NoSuchElementException e) {
                    Slog.e(TAG, "connectToProxy: tp hal service not found. Did the service fail to start?", e);
                } catch (RemoteException e2) {
                    Slog.e(TAG, "connectToProxy: tp hal service not responding", e2);
                }
            }
        }
    }

    private void setTpKeep(int keyCode, boolean down) {
        if (this.mProximityTop && down && this.mTpKeepListener != null) {
            if (keyCode == 708) {
                this.mTpKeepListener.setTpKeep(true);
            } else if (keyCode == 709) {
                this.mTpKeepListener.setTpKeep(false);
            }
        }
    }

    public void setTPDozeMode(int scene, int mode) {
        if (this.mTpTouchSwitch == null) {
            Slog.d(TAG, "get touch service failed");
            return;
        }
        try {
            boolean isSuccess = this.mTpTouchSwitch.hwTsSetDozeMode(scene, mode, 0);
            if (Log.isLoggable(TAG, 3)) {
                Slog.d(TAG, "set parameter scene:" + scene + ",mode:" + mode + "  sucess:" + isSuccess);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "set doze mode RemoteException:" + e.getMessage());
        } catch (Exception e2) {
            Slog.e(TAG, "get service  error: " + e2.getMessage());
        }
    }

    private void showStartMenu() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.showStartMenu();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException showStartMenu");
            }
        }
    }

    private void screenshotPc() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.screenshotPc();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException screenshotPc");
            }
        }
    }

    private void closeTopWindow() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.closeTopWindow();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException closeTopWindow");
            }
        }
    }

    private void triggerSwitchTaskView(boolean show) {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.triggerSwitchTaskView(show);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException triggerSwitchTaskView");
            }
        }
    }

    /* access modifiers changed from: private */
    public void lockScreen(boolean lock) {
        if (this.mWindowManagerInternal != null) {
            this.mWindowManagerInternal.setFocusedDisplayId(0, "lockScreen");
        }
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.lockScreen(lock);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException lockScreen");
            }
        }
    }

    private void toggleHome() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.toggleHome();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException toggleHome");
            }
        }
    }

    private void dispatchKeyEventForExclusiveKeyboard(KeyEvent ke) {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.dispatchKeyEventForExclusiveKeyboard(ke);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException dispatchKeyEvent");
            }
        }
    }

    /* access modifiers changed from: private */
    public void userActivityOnDesktop() {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.userActivityOnDesktop();
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "RemoteException userActivityOnDesktop");
            }
        }
    }

    private final void sendHwMenuKeyEvent() {
        int[] actions = {0, 1};
        for (int keyEvent : actions) {
            long curTime = SystemClock.uptimeMillis();
            KeyEvent ev = new KeyEvent(curTime, curTime, keyEvent, 82, 0, 0, -1, 0, 268435464, 257);
            InputManager.getInstance().injectInputEvent(ev, 0);
        }
    }

    /* access modifiers changed from: protected */
    public void launchAssistAction(String hint, int deviceId) {
        if (!checkPackageInstalled("com.google.android.googlequicksearchbox")) {
            sendCloseSystemWindows();
            boolean z = true;
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), "hw_long_home_voice_assistant", 0) != 1) {
                z = false;
            }
            boolean enableVoiceAssistant = z;
            if (IS_LONG_HOME_VASSITANT && enableVoiceAssistant) {
                performHapticFeedbackLw(null, 0, false);
                String intent = "android.intent.action.ASSIST";
                try {
                    if (checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE)) {
                        intent = VOICE_ASSISTANT_ACTION;
                    }
                    this.mContext.startActivity(new Intent(intent).setFlags(268435456));
                } catch (ActivityNotFoundException anfe) {
                    Slog.w(TAG, "No activity to handle voice assistant action.", anfe);
                }
            }
            return;
        }
        HwPhoneWindowManager.super.launchAssistAction(hint, deviceId);
    }

    private boolean checkPackageInstalled(String packageName) {
        try {
            this.mContext.getPackageManager().getPackageInfo(packageName, 128);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isHwDarkTheme(Context context, int themeId) {
        boolean z = false;
        try {
            if (context.getResources().getResourceName(themeId).indexOf("Emui.Dark") >= 0) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isMusicActive() {
        if (((AudioManager) this.mContext.getSystemService("audio")) != null) {
            return AudioSystem.isStreamActive(3, 0);
        }
        Log.w(TAG, "isMusicActive: couldn't get AudioManager reference");
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0024, code lost:
        if (r4.mBroadcastWakeLock != null) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0033, code lost:
        if (r4.mBroadcastWakeLock == null) goto L_0x003a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0035, code lost:
        r4.mBroadcastWakeLock.release();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003a, code lost:
        return;
     */
    public void handleVolumeKey(int stream, int keycode) {
        int i;
        IAudioService audioService = getAudioService();
        if (audioService != null) {
            try {
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.acquire();
                }
                if (keycode == 24) {
                    i = 1;
                } else {
                    i = -1;
                }
                audioService.adjustStreamVolume(stream, i, 0, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.e(TAG, "IAudioService.adjust*StreamVolume() threw RemoteException");
            } catch (Throwable th) {
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.release();
                }
                throw th;
            }
        }
    }

    static IAudioService getAudioService() {
        IAudioService audioService = IAudioService.Stub.asInterface(ServiceManager.checkService("audio"));
        if (audioService == null) {
            Log.w(TAG, "Unable to find IAudioService interface.");
        }
        return audioService;
    }

    private void sendVolumeDownKeyPressed() {
        this.mHandler.postDelayed(this.mHandleVolumeDownKey, 500);
    }

    /* access modifiers changed from: private */
    public void cancelVolumeDownKeyPressed() {
        this.mHandler.removeCallbacks(this.mHandleVolumeDownKey);
    }

    private void resetVolumeDownKeyPressed() {
        if (this.mHandler.hasCallbacks(this.mHandleVolumeDownKey)) {
            this.mHandler.removeCallbacks(this.mHandleVolumeDownKey);
            this.mHandler.post(this.mHandleVolumeDownKey);
        }
    }

    private void interceptQuickCallChord() {
        this.mHandler.postDelayed(this.mVolumeDownLongPressed, 500);
    }

    private void cancelPendingQuickCallChordAction() {
        this.mHandler.removeCallbacks(this.mVolumeDownLongPressed);
        resetVolumeDownKeyPressed();
    }

    private void resetVolumeDownKeyLongPressed() {
        if (this.mHandler.hasCallbacks(this.mVolumeDownLongPressed)) {
            this.mHandler.removeCallbacks(this.mVolumeDownLongPressed);
            this.mHandler.post(this.mVolumeDownLongPressed);
        }
    }

    /* access modifiers changed from: private */
    public void initQuickcall() {
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(HIVOICE_PRESS_TYPE_POWER);
        if (this.mPowerManager != null) {
            this.mBroadcastWakeLock = this.mPowerManager.newWakeLock(1, "HwPhoneWindowManager.mBroadcastWakeLock");
            this.mVolumeDownWakeLock = this.mPowerManager.newWakeLock(1, "HwPhoneWindowManager.mVolumeDownWakeLock");
        }
        this.mHeadless = "1".equals(SystemProperties.get("ro.config.headless", "0"));
    }

    private void notifyRapidCaptureService(String command) {
        if (this.mSystemReady) {
            Intent intent = new Intent(HUAWEI_RAPIDCAPTURE_START_MODE);
            intent.setPackage("com.huawei.camera");
            intent.putExtra(SMARTKEY_TAG, command);
            try {
                this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
            } catch (Exception e) {
                Slog.e(TAG, "unable to start service:" + intent, e);
            }
            if (this.mVolumeDownWakeLock != null) {
                this.mVolumeDownWakeLock.acquire(500);
            }
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Log.d(TAG, "start Rapid Capture Service, command:" + extras.get(SMARTKEY_TAG));
            }
        }
    }

    public void showHwTransientBars() {
        if (this.mStatusBar != null) {
            requestHwTransientBars(this.mStatusBar);
        }
    }

    private void notifyTouchplusService(int kcode, int kval) {
        Intent intent = new Intent("com.huawei.membranetouch.action.MT_MANAGER");
        intent.putExtra("keycode", kcode);
        intent.putExtra("keyvalue", kval);
        intent.setPackage("com.huawei.membranetouch");
        this.mContext.startService(intent);
    }

    public void transactToStatusBarService(int code, String transactName, String paramName, int paramValue) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
            if (statusBarServiceBinder != null) {
                data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                if (paramName != null) {
                    data.writeInt(paramValue);
                }
                statusBarServiceBinder.transact(code, data, reply, 0);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService four params->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    /* access modifiers changed from: protected */
    public void transactToStatusBarService(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getHWStatusBarService() != null) {
                IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
                if (statusBarServiceBinder != null) {
                    Log.d(TAG, "Transact:" + transactName + " to status bar service");
                    data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    data.writeInt(isEmuiStyle);
                    data.writeInt(statusbarColor);
                    data.writeInt(navigationBarColor);
                    data.writeInt(isEmuiLightStyle);
                    statusBarServiceBinder.transact(code, data, reply, 0);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    public void updateSystemUiColorLw(WindowManagerPolicy.WindowState win) {
        boolean colorChanged;
        WindowManagerPolicy.WindowState windowState = win;
        if (windowState != null) {
            if (isCoverWindow(win)) {
                Slog.i(TAG, "updateSystemUiColorLw isCoverWindow return " + windowState);
                return;
            }
            WindowManager.LayoutParams attrs = win.getAttrs();
            if (this.mLastColorWin != windowState || this.mLastStatusBarColor != attrs.statusBarColor || this.mLastNavigationBarColor != attrs.navigationBarColor) {
                boolean isFloating = getFloatingValue(attrs.isEmuiStyle);
                boolean isPopup = attrs.type == 1000 || attrs.type == 1002 || attrs.type == 2009 || attrs.type == 2010 || attrs.type == 2003;
                if (attrs.type == 3) {
                }
                boolean isTouchExplrEnabled = this.mAccessibilityManager.isTouchExplorationEnabled();
                int isEmuiStyle = getEmuiStyleValue(attrs.isEmuiStyle);
                int statusBarColor = attrs.statusBarColor;
                int navigationBarColor = attrs.navigationBarColor;
                if (!isTouchExplrEnabled) {
                    colorChanged = (this.mLastStatusBarColor == statusBarColor && this.mLastNavigationBarColor == navigationBarColor) ? false : true;
                } else {
                    colorChanged = isTouchExplrEnabled != this.mIsTouchExplrEnabled;
                    isEmuiStyle = -2;
                }
                boolean styleChanged = isEmuiStyleChanged(isEmuiStyle);
                boolean ignoreWindow = (windowState == this.mStatusBar || attrs.type == 2024 || isKeyguardHostWindow(attrs)) || isFloating || isPopup || (attrs.type == 2034) || (win.getWindowingMode() != 3 && win.isInMultiWindowMode());
                boolean changed = (styleChanged && !ignoreWindow) || (!styleChanged && !ignoreWindow && colorChanged);
                if (!ignoreWindow) {
                    WindowManager.LayoutParams layoutParams = attrs;
                    windowState.setCanCarryColors(true);
                } else {
                    WindowManager.LayoutParams layoutParams2 = attrs;
                }
                this.mLastNavigationBarColor = isTouchExplrEnabled ? -16777216 : navigationBarColor;
                if (changed) {
                    this.mLastStatusBarColor = isTouchExplrEnabled ? -16777216 : statusBarColor;
                    this.mLastIsEmuiStyle = isEmuiStyle;
                    this.mIsTouchExplrEnabled = isTouchExplrEnabled;
                    this.mLastColorWin = windowState;
                    boolean z = isFloating;
                    StringBuilder sb = new StringBuilder();
                    boolean z2 = isPopup;
                    sb.append("updateSystemUiColorLw window=");
                    sb.append(windowState);
                    sb.append(",EmuiStyle=");
                    sb.append(isEmuiStyle);
                    sb.append(",StatusBarColor=0x");
                    sb.append(Integer.toHexString(statusBarColor));
                    sb.append(",NavigationBarColor=0x");
                    sb.append(Integer.toHexString(navigationBarColor));
                    sb.append(", mForceNotchStatusBar=");
                    sb.append(this.mForceNotchStatusBar);
                    Slog.v(TAG, sb.toString());
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            if ((!HwPhoneWindowManager.this.mIsNotchSwitchOpen && !HwPhoneWindowManager.this.mForceNotchStatusBar && !HwPhoneWindowManager.this.mIsForceSetStatusBar) || HwPhoneWindowManager.this.mIsNotchSwitchOpen) {
                                if (HwPhoneWindowManager.this.mLastIsEmuiStyle == -1) {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        Slog.v(HwPhoneWindowManager.TAG, "InterruptedException is happed in method updateSystemUiColorLw");
                                    }
                                }
                                HwPhoneWindowManager.this.transactToStatusBarService(106, "setSystemUIColor", HwPhoneWindowManager.this.mLastIsEmuiStyle, HwPhoneWindowManager.this.mLastStatusBarColor, HwPhoneWindowManager.this.mLastNavigationBarColor, -1);
                            }
                        }
                    });
                } else {
                    boolean z3 = isPopup;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getEmuiStyleValue(int styleValue) {
        if (styleValue == -1) {
            return -1;
        }
        return Integer.MAX_VALUE & styleValue;
    }

    /* access modifiers changed from: protected */
    public boolean isEmuiStyleChanged(int isEmuiStyle) {
        return this.mLastIsEmuiStyle != isEmuiStyle;
    }

    /* access modifiers changed from: protected */
    public boolean getFloatingValue(int styleValue) {
        return styleValue != -1 && (styleValue & FLOATING_MASK) == FLOATING_MASK;
    }

    public void onTouchExplorationStateChanged(boolean enabled) {
        updateSystemUiColorLw(getCurrentWin());
    }

    /* access modifiers changed from: protected */
    public void hwInit() {
        this.mAccessibilityManager.addTouchExplorationStateChangeListener(this);
    }

    /* access modifiers changed from: package-private */
    public IStatusBarService getHWStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            }
            iStatusBarService = this.mStatusBarService;
        }
        return iStatusBarService;
    }

    /* access modifiers changed from: private */
    public void notifyFingerOptical() {
        Log.i(TAG, "system ready, register pointer event listenr for UD Optical fingerprint");
        this.mFingerprintHardwareType = FingerprintManagerEx.getHardwareType();
        if (this.mFingerprintHardwareType == 1) {
            this.mWindowManagerFuncs.registerPointerEventListener(new WindowManagerPolicyConstants.PointerEventListener() {
                public void onPointerEvent(MotionEvent motionEvent) {
                    if (motionEvent.getActionMasked() == 1) {
                        FingerViewController.getInstance(HwPhoneWindowManager.this.mContext).notifyTouchUp(motionEvent.getRawX(), motionEvent.getRawY());
                    } else if (motionEvent.getActionMasked() == 6) {
                        int actionIndex = motionEvent.getActionIndex();
                        FingerViewController.getInstance(HwPhoneWindowManager.this.mContext).notifyTouchUp(motionEvent.getX(actionIndex), motionEvent.getY(actionIndex));
                    }
                }
            });
        }
    }

    private void interceptTouchDisableMode() {
        if (this.mVolumeUpKeyDisTouch && this.mPowerKeyDisTouch && !this.mVolumeDownKeyDisTouch) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mVolumeUpKeyDisTouchTime + 150 && now <= this.mPowerKeyDisTouchTime + 150) {
                this.mVolumeUpKeyConsumedByDisTouch = true;
                cancelPendingPowerKeyActionForDistouch();
                if (this.mHwScreenOnProximityLock != null) {
                    this.mHwScreenOnProximityLock.releaseLock(0);
                }
            }
        }
    }

    public boolean checkPhoneOFFHOOK() {
        int callState = ((TelephonyManager) this.mContext.getSystemService("phone")).getCallState();
        Log.d(TAG, "callState : " + callState);
        return callState == 2;
    }

    public boolean checkHeadSetIsConnected() {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        boolean headSetConnectedState = false;
        if (audioManager == null) {
            return false;
        }
        if (audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn() || audioManager.isBluetoothScoOn()) {
            headSetConnectedState = true;
        }
        Log.d(TAG, "checkHeadSetIsConnected : " + headSetConnectedState);
        return headSetConnectedState;
    }

    public void screenTurningOn(WindowManagerPolicy.ScreenOnListener screenOnListener) {
        HwPhoneWindowManager.super.screenTurningOn(screenOnListener);
        if (this.mContext == null) {
            Log.d(TAG, "Context object is null.");
            return;
        }
        setProximitySensorEnabled(true);
        if (this.mFalseTouchMonitor != null && this.mFalseTouchMonitor.isFalseTouchFeatureOn() && !this.mScreenOnForFalseTouch) {
            this.mScreenOnForFalseTouch = true;
            this.mWindowManagerFuncs.registerPointerEventListener(this.mFalseTouchMonitor.getEventListener());
        }
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
            EasyWakeUpManager mWakeUpManager = EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate);
            if (mWakeUpManager != null) {
                mWakeUpManager.turnOffSensorListener();
            }
        }
    }

    public void screenTurnedOn() {
        HwPhoneWindowManager.super.screenTurnedOn();
        FaceReportEventToIaware.reportEventToIaware(this.mContext, 20023);
        if (this.mSystemReady) {
            Slog.d(TAG, "screenTurnedOn");
            if (this.mBooted) {
                PickUpWakeScreenManager.getInstance(this.mContext, this.mHandler, this.mWindowManagerFuncs, this.mKeyguardDelegate).enablePickupMotionOrNot(false);
            }
            if (isAcquireProximityLock()) {
                this.mHwScreenOnProximityLock.acquireLock(this, 0);
                this.mHwScreenOnProximityLock.registerDeviceListener();
            }
        }
    }

    public void screenTurnedOff() {
        HwPhoneWindowManager.super.screenTurnedOff();
        FaceReportEventToIaware.reportEventToIaware(this.mContext, 90023);
        setProximitySensorEnabled(false);
        if (this.mFalseTouchMonitor != null && this.mFalseTouchMonitor.isFalseTouchFeatureOn() && this.mScreenOnForFalseTouch) {
            this.mScreenOnForFalseTouch = false;
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.mFalseTouchMonitor.getEventListener());
        }
        if (this.mHwScreenOnProximityLock != null) {
            this.mHwScreenOnProximityLock.releaseLock(1);
            this.mHwScreenOnProximityLock.unregisterDeviceListener();
        }
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
            EasyWakeUpManager mWakeUpManager = EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate);
            if (mWakeUpManager != null) {
                mWakeUpManager.turnOnSensorListener();
            }
        }
        try {
            if (this.mIHwWindowCallback != null) {
                this.mIHwWindowCallback.screenTurnedOff();
            }
        } catch (Exception ex) {
            Log.w(TAG, "mIHwWindowCallback threw RemoteException", ex);
        }
        if (this.mSystemReady) {
            Slog.d(TAG, "screenTurnedOff");
            PickUpWakeScreenManager.getInstance(this.mContext, this.mHandler, this.mWindowManagerFuncs, this.mKeyguardDelegate).enablePickupMotionOrNot(true);
        }
    }

    public int selectAnimationLw(WindowManagerPolicy.WindowState win, int transit) {
        if (win != this.mNavigationBar || this.mNavigationBarPosition == 4 || (transit != 1 && transit != 3)) {
            return HwPhoneWindowManager.super.selectAnimationLw(win, transit);
        }
        return mIsHwNaviBar ? 0 : 17432621;
    }

    public void onConfigurationChanged() {
        HwPhoneWindowManager.super.onConfigurationChanged();
        if (this.mNavigationBarPolicy != null) {
            if (this.mNavigationBarPolicy.mMinNavigationBar) {
                this.mNavigationBarHeightForRotationDefault = (int[]) this.mNavigationBarHeightForRotationMin.clone();
                this.mNavigationBarWidthForRotationDefault = (int[]) this.mNavigationBarWidthForRotationMin.clone();
            } else {
                this.mNavigationBarHeightForRotationDefault = (int[]) this.mNavigationBarHeightForRotationMax.clone();
                this.mNavigationBarWidthForRotationDefault = (int[]) this.mNavigationBarWidthForRotationMax.clone();
            }
        }
        if (this.mGestureNavManager != null) {
            this.mGestureNavManager.onConfigurationChanged();
        }
    }

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId, DisplayCutout displayCutout) {
        int width = fullWidth;
        if ((uiMode & 15) == 3) {
            width = HwPhoneWindowManager.super.getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode, displayId, displayCutout);
        } else if (displayId == 0 && this.mHasNavigationBar) {
            if (this.mNavigationBarCanMove && fullWidth > fullHeight) {
                width = (this.mNavigationBarPolicy == null || !this.mNavigationBarPolicy.mMinNavigationBar) ? !getNavibarAlignLeftWhenLand() ? width - this.mNavigationBarWidthForRotationMax[rotation] : width - this.mContext.getResources().getDimensionPixelSize(34472115) : width - this.mNavigationBarWidthForRotationMin[rotation];
            }
            if (displayCutout != null) {
                width -= displayCutout.getSafeInsetLeft() + displayCutout.getSafeInsetRight();
            }
        } else if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) {
            return width;
        } else {
            return width;
        }
        return width;
    }

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId, DisplayCutout displayCutout) {
        int height = fullHeight;
        if ((uiMode & 15) == 3) {
            return HwPhoneWindowManager.super.getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayId, displayCutout);
        }
        if (displayId == 0 && this.mHasNavigationBar) {
            if (!this.mNavigationBarCanMove || fullWidth < fullHeight) {
                if (this.mNavigationBarPolicy == null || !this.mNavigationBarPolicy.mMinNavigationBar) {
                    height -= this.mNavigationBarHeightForRotationMax[rotation];
                } else {
                    height -= this.mNavigationBarHeightForRotationMin[rotation];
                }
            }
            if (displayCutout != null) {
                return height - (displayCutout.getSafeInsetTop() + displayCutout.getSafeInsetBottom());
            }
            return height;
        } else if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId) || getNavigationBarExternal() == null || !getNavigationBarExternal().isVisibleLw()) {
            return height;
        } else {
            return height - getNavigationBarHeightExternal();
        }
    }

    public void setInputMethodWindowVisible(boolean visible) {
        this.mInputMethodWindowVisible = visible;
    }

    public int getNaviBarHeightForRotationMin(int index) {
        return this.mNavigationBarHeightForRotationMin[index];
    }

    public int getNaviBarWidthForRotationMin(int index) {
        return this.mNavigationBarWidthForRotationMin[index];
    }

    public int getNaviBarHeightForRotationMax(int index) {
        return this.mNavigationBarHeightForRotationMax[index];
    }

    public int getNaviBarWidthForRotationMax(int index) {
        return this.mNavigationBarWidthForRotationMax[index];
    }

    public void setInitialDisplaySize(Display display, int width, int height, int density) {
        if (density == 0) {
            Log.e(TAG, "density is 0");
            return;
        }
        HwPhoneWindowManager.super.setInitialDisplaySize(display, width, height, density);
        if (this.mContext != null) {
            initNavigationBarHightExternal(display, width, height);
            Resources res = this.mContext.getResources();
            ContentResolver resolver = this.mContext.getContentResolver();
            int[] iArr = this.mNavigationBarHeightForRotationMax;
            int i = this.mPortraitRotation;
            int[] iArr2 = this.mNavigationBarHeightForRotationMax;
            int i2 = this.mUpsideDownRotation;
            int dimensionPixelSize = res.getDimensionPixelSize(17105186);
            iArr2[i2] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            int[] iArr3 = this.mNavigationBarHeightForRotationMax;
            int i3 = this.mLandscapeRotation;
            int[] iArr4 = this.mNavigationBarHeightForRotationMax;
            int i4 = this.mSeascapeRotation;
            int dimensionPixelSize2 = res.getDimensionPixelSize(17105188);
            iArr4[i4] = dimensionPixelSize2;
            iArr3[i3] = dimensionPixelSize2;
            int[] iArr5 = this.mNavigationBarHeightForRotationMin;
            int i5 = this.mPortraitRotation;
            int[] iArr6 = this.mNavigationBarHeightForRotationMin;
            int i6 = this.mUpsideDownRotation;
            int[] iArr7 = this.mNavigationBarHeightForRotationMin;
            int i7 = this.mLandscapeRotation;
            int[] iArr8 = this.mNavigationBarHeightForRotationMin;
            int i8 = this.mSeascapeRotation;
            int i9 = Settings.System.getInt(resolver, "navigationbar_height_min", 0);
            iArr8[i8] = i9;
            iArr7[i7] = i9;
            iArr6[i6] = i9;
            iArr5[i5] = i9;
            int[] iArr9 = this.mNavigationBarWidthForRotationMax;
            int i10 = this.mPortraitRotation;
            int[] iArr10 = this.mNavigationBarWidthForRotationMax;
            int i11 = this.mUpsideDownRotation;
            int[] iArr11 = this.mNavigationBarWidthForRotationMax;
            int i12 = this.mLandscapeRotation;
            int[] iArr12 = this.mNavigationBarWidthForRotationMax;
            int i13 = this.mSeascapeRotation;
            int dimensionPixelSize3 = res.getDimensionPixelSize(17105191);
            iArr12[i13] = dimensionPixelSize3;
            iArr11[i12] = dimensionPixelSize3;
            iArr10[i11] = dimensionPixelSize3;
            iArr9[i10] = dimensionPixelSize3;
            int[] iArr13 = this.mNavigationBarWidthForRotationMin;
            int i14 = this.mPortraitRotation;
            int[] iArr14 = this.mNavigationBarWidthForRotationMin;
            int i15 = this.mUpsideDownRotation;
            int[] iArr15 = this.mNavigationBarWidthForRotationMin;
            int i16 = this.mLandscapeRotation;
            int[] iArr16 = this.mNavigationBarWidthForRotationMin;
            int i17 = this.mSeascapeRotation;
            int i18 = Settings.System.getInt(resolver, "navigationbar_width_min", 0);
            iArr16[i17] = i18;
            iArr15[i16] = i18;
            iArr14[i15] = i18;
            iArr13[i14] = i18;
        }
    }

    /* access modifiers changed from: protected */
    public boolean computeNaviBarFlag() {
        WindowManager.LayoutParams focusAttrs = this.mFocusedWindow != null ? this.mFocusedWindow.getAttrs() : null;
        boolean z = false;
        int type = focusAttrs != null ? focusAttrs.type : 0;
        boolean forceNavibar = focusAttrs != null && (focusAttrs.hwFlags & 1) == 1;
        boolean keyguardOn = type == TOAST_TYPE_COVER_SCREEN || type == 2100;
        boolean iskeyguardDialog = type == 2009 && keyguardOn();
        boolean dreamOn = focusAttrs != null && focusAttrs.type == 2023;
        boolean isNeedHideNaviBarWin = (focusAttrs == null || (focusAttrs.privateFlags & FLOATING_MASK) == 0) ? false : true;
        if (this.mHwPWMEx.getFPAuthState()) {
            return true;
        }
        if (this.mStatusBar == this.mFocusedWindow) {
            return false;
        }
        if (iskeyguardDialog && !forceNavibar) {
            return true;
        }
        if (dreamOn) {
            return false;
        }
        if (keyguardOn || isNeedHideNaviBarWin) {
            return true;
        }
        if (this.mHwPWMEx.getNaviBarFlag() && !this.mInputMethodWindowVisible) {
            z = true;
        }
        return z;
    }

    public boolean isNaviBarMini() {
        if (this.mNavigationBarPolicy == null || !this.mNavigationBarPolicy.mMinNavigationBar) {
            return false;
        }
        return true;
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
            requestHwTransientBars(this.mStatusBar);
        }
        return true;
    }

    public boolean swipeFromBottom() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0 || this.mIsGestureNavEnable) {
            return true;
        }
        if (!mIsHwNaviBar || !isLastImmersiveMode() || this.mNavigationBar == null || this.mNavigationBarPosition != 4) {
            return false;
        }
        if (this.mNavigationBarPolicy == null || !this.mNavigationBarPolicy.getGameControlReslut(2)) {
            requestHwTransientBars(this.mNavigationBar);
        }
        return true;
    }

    public boolean swipeFromRight() {
        if (this.mIsGestureNavEnable) {
            return true;
        }
        if (!mIsHwNaviBar || !isLastImmersiveMode() || this.mNavigationBar == null || this.mNavigationBarPosition == 4) {
            return false;
        }
        if (this.mNavigationBarPolicy == null || !this.mNavigationBarPolicy.getGameControlReslut(3)) {
            requestHwTransientBars(this.mNavigationBar);
        }
        return true;
    }

    public boolean isGestureIsolated() {
        WindowManagerPolicy.WindowState win = this.mFocusedWindow != null ? this.mFocusedWindow : this.mTopFullscreenOpaqueWindowState;
        if (win == null || (win.getAttrs().hwFlags & 512) != 512) {
            return false;
        }
        return true;
    }

    public void requestTransientStatusBars() {
        synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
            BarController barController = getStatusBarController();
            boolean sb = false;
            if (barController != null) {
                sb = barController.checkShowTransientBarLw();
            }
            if (sb && barController != null) {
                barController.showTransient();
            }
            ImmersiveModeConfirmation immer = getImmersiveModeConfirmation();
            if (immer != null) {
                immer.confirmCurrentPrompt();
            }
            updateHwSystemUiVisibilityLw();
        }
    }

    public boolean isTopIsFullscreen() {
        boolean z;
        try {
            if (this.mFocusedWindow != null) {
                z = (this.mFocusedWindow.getAttrs().flags & 1024) != 0;
            } else {
                z = this.mTopIsFullscreen;
            }
            return z;
        } catch (NullPointerException e) {
            Log.e(TAG, "isTopIsFullscreen catch null pointer");
            return this.mTopIsFullscreen;
        }
    }

    public boolean okToShowTransientBar() {
        BarController barController = getStatusBarController();
        boolean z = false;
        if (barController == null) {
            return false;
        }
        if (barController.checkShowTransientBarLw()) {
            z = true;
        }
        return z;
    }

    private void turnOnSensorListener() {
        if (this.mSensorManager == null) {
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        }
        if (this.mCoverManager == null) {
            this.mCoverManager = new CoverManager();
        }
        if (this.mCoverManager != null) {
            this.mCoverOpen = this.mCoverManager.isCoverOpen();
        }
        boolean touchDisableModeOpen = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1, -2) == 1;
        if (this.mCoverOpen && !this.mSensorRegisted && this.mListener != null && touchDisableModeOpen) {
            Log.i(TAG, "turnOnSensorListener, registerListener");
            this.mSensorManager.registerListener(this.mListener, this.mSensorManager.getDefaultSensor(8), 0);
            this.mSensorRegisted = true;
            this.mHandler.removeCallbacks(this.mProximitySensorTimeoutRunnable);
            this.mHandler.postDelayed(this.mProximitySensorTimeoutRunnable, 1000);
        }
    }

    public void turnOffSensorListener() {
        if (this.mSensorRegisted && this.mListener != null) {
            Log.i(TAG, "turnOffSensorListener, unregisterListener ");
            this.mSensorManager.unregisterListener(this.mListener);
            this.mHandler.removeCallbacks(this.mProximitySensorTimeoutRunnable);
            this.mIsProximity = false;
        }
        this.mSensorRegisted = false;
    }

    public void setHwWindowCallback(IHwWindowCallback hwWindowCallback) {
        Log.i(TAG, "setHwWindowCallback=" + hwWindowCallback);
        this.mIHwWindowCallback = hwWindowCallback;
    }

    public IHwWindowCallback getHwWindowCallback() {
        return this.mIHwWindowCallback;
    }

    public void updateSettings() {
        Flog.i(1503, "updateSettings");
        HwPhoneWindowManager.super.updateSettings();
        updateFingerSenseSettings();
        setFingerSenseState();
        setNaviBarState();
    }

    private void updateFingerSenseSettings() {
        ContentResolver cr = this.mContext.getContentResolver();
        FingerSenseSettings.updateSmartshotEnabled(cr);
        FingerSenseSettings.updateLineGestureEnabled(cr);
        if (NEED_TAILORED) {
            FingerSenseSettings.updateScreenRecordEnabled(cr);
        } else {
            FingerSenseSettings.updateDrawGestureEnabled(cr);
        }
    }

    public void enableScreenAfterBoot() {
        HwPhoneWindowManager.super.enableScreenAfterBoot();
        this.mBooted = true;
        enableSystemWideAfterBoot(this.mContext);
        enableFingerPrintActionsAfterBoot(this.mContext);
        enableStylusAfterBoot(this.mContext);
    }

    public WindowManagerPolicy.WindowState getFocusedWindow() {
        return this.mFocusedWindow;
    }

    public WindowManagerPolicy.WindowState getTopFullscreenWindow() {
        return this.mTopFullscreenOpaqueWindowState;
    }

    public int getRestrictedScreenHeight() {
        return this.mRestrictedScreenHeight;
    }

    private void enableStylusAfterBoot(Context context) {
        if (HwStylusUtils.hasStylusFeature(context)) {
            Log.i(TAG, "enable stylus gesture feature.");
            this.mHandler.post(new Runnable() {
                public void run() {
                    HwPhoneWindowManager.this.enableStylusAction();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void enableStylusAction() {
        if (this.mStylusGestureListener == null) {
            this.mStylusGestureListener = new StylusGestureListener(this.mContext, this);
            this.mWindowManagerFuncs.registerPointerEventListener(this.mStylusGestureListener);
        }
    }

    public boolean isNavigationBarVisible() {
        return this.mHasNavigationBar && this.mNavigationBar != null && this.mNavigationBar.isVisibleLw();
    }

    /* access modifiers changed from: protected */
    public void enableSystemWideActions() {
        if (!SystemProperties.getBoolean("ro.config.finger_joint", false)) {
            Flog.i(1503, "Can not enable fingersense, ro.config.finger_joint is set to false");
            return;
        }
        Flog.i(1503, "FingerSense enableSystemWideActions");
        if (this.systemWideActionsListener == null) {
            synchronized (this.mFeatureLock) {
                Flog.i(1503, "FingerSense new enableSystemWideActions ++++");
                IFeatureFramework iFeatureFwk = HwFeatureLoader.SystemServiceFeature.getFeatureFramework();
                if (iFeatureFwk != null && mIFeature == null) {
                    mIFeature = iFeatureFwk.loadFeature("com.huawei.featurelayer.systemservicefeature.HwGestureAction", IHwSystemWideActionsListenerEx.class.getCanonicalName());
                    Flog.i(1503, "loadFeature: " + IHwSystemWideActionsListenerEx.class.getCanonicalName());
                }
                if (mIFeature != null) {
                    this.systemWideActionsListener = mIFeature;
                    this.systemWideActionsListener.create(this.mContext);
                    this.mWindowManagerFuncs.registerPointerEventListener(this.mSystemWideActionsListenerPointerEventListener);
                    Flog.i(1503, "IHwSystemWideActionsListenerEx init end ---");
                } else {
                    Flog.i(1503, "loadFeature fail! ");
                }
            }
        }
        SystemProperties.set("persist.sys.fingersense", "1");
    }

    /* access modifiers changed from: protected */
    public void disableSystemWideActions() {
        Flog.i(1503, "FingerSense disableSystemWideActions");
        if (this.systemWideActionsListener != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.mSystemWideActionsListenerPointerEventListener);
            this.systemWideActionsListener.destroyPointerLocationView();
            this.systemWideActionsListener = null;
        }
        SystemProperties.set("persist.sys.fingersense", "0");
    }

    /* access modifiers changed from: protected */
    public void enableFingerPrintActions() {
        Log.d(TAG, "enableFingerPrintActions()");
        if (this.fingerprintActionsListener != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.fingerprintActionsListener);
            this.fingerprintActionsListener.destroySearchPanelView();
            this.fingerprintActionsListener.destroyMultiWinArrowView();
            this.fingerprintActionsListener = null;
        }
        this.fingerprintActionsListener = new FingerprintActionsListener(this.mContext, this);
        this.mWindowManagerFuncs.registerPointerEventListener(this.fingerprintActionsListener);
        this.fingerprintActionsListener.createSearchPanelView();
        this.fingerprintActionsListener.createMultiWinArrowView();
    }

    /* access modifiers changed from: protected */
    public void disableFingerPrintActions() {
        if (this.fingerprintActionsListener != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.fingerprintActionsListener);
            this.fingerprintActionsListener.destroySearchPanelView();
            this.fingerprintActionsListener.destroyMultiWinArrowView();
            this.fingerprintActionsListener = null;
        }
    }

    /* access modifiers changed from: protected */
    public void enableFingerPrintActionsAfterBoot(Context context) {
        final ContentResolver resolver = context.getContentResolver();
        this.mHandler.post(new Runnable() {
            public void run() {
                if (!FrontFingerPrintSettings.isNaviBarEnabled(resolver) || (FrontFingerPrintSettings.isSingleVirtualNavbarEnable(resolver) && !FrontFingerPrintSettings.isSingleNavBarAIEnable(resolver))) {
                    HwPhoneWindowManager.this.enableFingerPrintActions();
                } else {
                    HwPhoneWindowManager.this.disableFingerPrintActions();
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setNaviBarState() {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean navibarEnable = FrontFingerPrintSettings.isNaviBarEnabled(resolver);
        boolean z = false;
        if ((FrontFingerPrintSettings.isSingleVirtualNavbarEnable(resolver) && !FrontFingerPrintSettings.isSingleNavBarAIEnable(resolver)) || !navibarEnable) {
            z = true;
        }
        boolean navibarEnable2 = z;
        Log.d(TAG, "setNaviBarState()--navibarEnable:" + navibarEnable2 + ";mNavibarEnabled:" + this.mNavibarEnabled + ";singleNavBarEnable:" + singleNavBarEnable + ";singleNavBarAiEnable:" + singleNavBarAiEnable + ";singEnarAiEnable:" + singEnarAiEnable);
        int i = 104;
        if (!this.mNaviBarStateInited) {
            if (this.mBooted) {
                this.mNavibarEnabled = navibarEnable2;
                Handler handler = this.mHandlerEx;
                if (navibarEnable2) {
                    i = 103;
                }
                handler.sendEmptyMessage(i);
                this.mNaviBarStateInited = true;
            }
        } else if (this.mNavibarEnabled != navibarEnable2) {
            Log.d(TAG, "setNaviBarState()--" + this.mNavibarEnabled);
            this.mNavibarEnabled = navibarEnable2;
            Handler handler2 = this.mHandlerEx;
            if (navibarEnable2) {
                i = 103;
            }
            handler2.sendEmptyMessage(i);
        }
    }

    /* access modifiers changed from: protected */
    public void updateSplitScreenView() {
        if ((!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) && this.fingerprintActionsListener != null) {
            this.fingerprintActionsListener.createMultiWinArrowView();
        }
    }

    /* access modifiers changed from: protected */
    public void enableSystemWideAfterBoot(Context context) {
        final ContentResolver resolver = context.getContentResolver();
        this.mHandler.post(new Runnable() {
            public void run() {
                if (FingerSenseSettings.isFingerSenseEnabled(resolver)) {
                    HwPhoneWindowManager.this.enableSystemWideActions();
                } else {
                    HwPhoneWindowManager.this.disableSystemWideActions();
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setFingerSenseState() {
        int i;
        boolean fingersense = FingerSenseSettings.isFingerSenseEnabled(this.mContext.getContentResolver());
        if (this.mFingerSenseEnabled != fingersense) {
            this.mFingerSenseEnabled = fingersense;
            Flog.i(1503, "setFingerSenseState to " + fingersense);
            Handler handler = this.mHandlerEx;
            if (fingersense) {
                i = 101;
            } else {
                i = 102;
            }
            handler.sendEmptyMessage(i);
        }
    }

    public void processing_KEYCODE_SOUNDTRIGGER_EVENT(int keyCode, Context context, boolean isMusicOrFMActive, boolean down, boolean keyguardShow) {
        Log.d(TAG, "intercept DSP WAKEUP EVENT" + keyCode + " down=" + down + " keyguardShow=" + keyguardShow);
        ITelephony asInterface = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        switch (keyCode) {
            case AwareJobSchedulerService.MSG_JOB_EXPIRED:
                if (down) {
                    Log.i(TAG, "soundtrigger wakeup.");
                    if (isTOPActivity(HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE)) {
                        Log.i(TAG, "start SoundTiggerTest");
                        notifySoundTriggerTest();
                        return;
                    } else if (isTOPActivity(HUAWEI_VOICE_DEBUG_BETACLUB)) {
                        Log.i(TAG, "soundtrigger debug during betaclub.");
                        notifySoundTriggerTest();
                        return;
                    } else {
                        Log.i(TAG, "start VA");
                        notifyVassistantService("start", 4, null);
                        return;
                    }
                } else {
                    return;
                }
            case AwareJobSchedulerService.MSG_CHECK_JOB:
                if (down) {
                    Log.i(TAG, "command that find my phone.");
                    if (isTOPActivity(HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE)) {
                        Log.i(TAG, "looking for my phone during SoundTiggerTest");
                        return;
                    } else if (isTOPActivity(HUAWEI_VOICE_DEBUG_BETACLUB)) {
                        Log.i(TAG, "looking for my phone during betaclub.");
                        return;
                    } else {
                        Log.i(TAG, "findphone.");
                        notifyVassistantService("findphone", 4, null);
                        return;
                    }
                } else {
                    return;
                }
            default:
                return;
        }
    }

    private boolean isTOPActivity(String appnames) {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks != null) {
                if (!tasks.isEmpty()) {
                    for (ActivityManager.RunningTaskInfo info : tasks) {
                        Log.i(TAG, "info.topActivity.getPackageName() is " + info.topActivity.getPackageName());
                        if (info.topActivity.getPackageName().equals(appnames) && info.baseActivity.getPackageName().equals(appnames)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            return false;
        } catch (RuntimeException e) {
            Log.e(TAG, "isTOPActivity->RuntimeException happened");
        } catch (Exception e2) {
            Log.e(TAG, "isTOPActivity->other exception happened");
        }
    }

    private boolean isNeedPassEventToCloud(int keyCode) {
        return !KEYCODE_NOT_FOR_CLOUD.contains(Integer.valueOf(keyCode)) && HwPCUtils.isPcCastModeInServer() && isCloudOnPCTOP();
    }

    private boolean isCloudOnPCTOP() {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks != null) {
                if (!tasks.isEmpty()) {
                    for (ActivityManager.RunningTaskInfo info : tasks) {
                        if (info.topActivity != null) {
                            if (info.baseActivity != null) {
                                if ("com.huawei.cloud".equals(info.topActivity.getPackageName()) && "com.huawei.cloud".equals(info.baseActivity.getPackageName()) && HwPCUtils.isPcDynamicStack(info.stackId) && "com.huawei.ahdp.session.VmActivity".equals(info.topActivity.getClassName())) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                    return false;
                }
            }
            return false;
        } catch (RuntimeException e) {
            HwPCUtils.log(TAG, "isCloudOnPCTOP->RuntimeException happened");
        } catch (Exception e2) {
            HwPCUtils.log(TAG, "isCloudOnPCTOP->other exception happened");
        }
    }

    /* access modifiers changed from: private */
    public void notifyVassistantService(String command, int mode, KeyEvent event) {
        Intent intent = new Intent(ACTION_HUAWEI_VASSISTANT_SERVICE);
        intent.putExtra(HUAWEI_VASSISTANT_EXTRA_START_MODE, mode);
        intent.putExtra(SMARTKEY_TAG, command);
        if (event != null) {
            intent.putExtra("KeyEvent", event);
        }
        intent.setPackage(HUAWEI_VASSISTANT_PACKAGE);
        try {
            this.mContext.startService(intent);
        } catch (Exception e) {
            Slog.e(TAG, "unable to start service:" + intent, e);
        }
        if (this.mVolumeDownWakeLock != null) {
            this.mVolumeDownWakeLock.acquire(500);
        }
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d(TAG, "start VASSISTANT Service, state:" + extras.get(HUAWEI_VASSISTANT_EXTRA_START_MODE) + " command:" + extras.get(SMARTKEY_TAG));
        }
    }

    private void notifySoundTriggerTest() {
        try {
            this.mContext.sendBroadcast(new Intent(HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST));
            Log.i(TAG, "start up HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST");
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "No receiver to handle HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST intent", e);
        }
    }

    public void handleSmartKey(Context context, KeyEvent event, Handler handler, boolean isScreenOn) {
        boolean down = event.getAction() == 0;
        int keyCode = event.getKeyCode();
        if (!this.mActivityManagerInternal.isSystemReady()) {
            Log.d(TAG, "System is not ready, just discard it this time.");
            return;
        }
        if (this.DEBUG_SMARTKEY) {
            Log.d(TAG, "handleSmartKey keycode = " + keyCode + " down = " + down + " isScreenOn = " + isScreenOn);
        }
        if (keyCode == 308) {
            if (down) {
                if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", false)) {
                    this.mNeedDropFingerprintEvent = true;
                    this.mHandler.removeCallbacks(this.mCancleInterceptFingerprintEvent);
                }
                if (!isScreenOn) {
                    if (this.mListener == null) {
                        this.mListener = new ProximitySensorListener();
                    }
                    turnOnSensorListener();
                }
                sendSmartKeyEvent(SMARTKEY_LP);
                long timediff = event.getEventTime() - this.mLastSmartKeyDownTime;
                this.mLastSmartKeyDownTime = event.getEventTime();
                if (timediff >= 400) {
                    this.mIsSmartKeyTripleOrMoreClick = false;
                    this.mIsSmartKeyDoubleClick = false;
                } else if (this.mIsSmartKeyDoubleClick || this.mIsSmartKeyTripleOrMoreClick) {
                    this.mIsSmartKeyTripleOrMoreClick = true;
                    this.mIsSmartKeyDoubleClick = false;
                } else {
                    cancelSmartKeyClick();
                    cancelSmartKeyLongPressed();
                    sendSmartKeyEvent(SMARTKEY_DCLICK);
                    this.mIsSmartKeyDoubleClick = true;
                }
            } else {
                if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", false)) {
                    this.mHandler.postDelayed(this.mCancleInterceptFingerprintEvent, 400);
                }
                if (this.mIsSmartKeyDoubleClick || this.mIsSmartKeyTripleOrMoreClick || event.getEventTime() - event.getDownTime() >= 500) {
                    cancelSmartKeyLongPressed();
                } else {
                    cancelSmartKeyLongPressed();
                    sendSmartKeyEvent(SMARTKEY_CLICK);
                }
            }
        }
    }

    private void cancelAIPowerLongPressed() {
        this.mHandler.removeCallbacks(this.mAIPowerLongPressed);
        Log.i(TAG, "cancel power long press");
    }

    private void cancelAIPowerSuperLongPressed() {
        this.mHandler.removeCallbacks(this.mAIPowerSuperLongPressed);
        Log.i(TAG, "cancel power superLong press");
    }

    private void cancelAIHomeLongPressed() {
        this.mHandler.removeCallbacks(this.mAIHomoLongPressed);
        Log.i(TAG, "cancel long click");
    }

    private void cancelAIHomeSuperLongPressed() {
        this.mHandler.removeCallbacks(this.mAIHomoSuperLongPressed);
        Log.i(TAG, "cancel super long click");
    }

    private void sendSmartKeyEvent(String Type) {
        if (SMARTKEY_LP.equals(Type)) {
            this.mHandler.postDelayed(this.mSmartKeyLongPressed, 500);
        } else if (SMARTKEY_DCLICK.equals(Type)) {
            notifySmartKeyEvent(SMARTKEY_DCLICK);
        } else {
            this.mHandler.postDelayed(this.mSmartKeyClick, 400);
        }
    }

    /* access modifiers changed from: private */
    public void cancelSmartKeyClick() {
        this.mHandler.removeCallbacks(this.mSmartKeyClick);
    }

    /* access modifiers changed from: private */
    public void cancelSmartKeyLongPressed() {
        this.mHandler.removeCallbacks(this.mSmartKeyLongPressed);
    }

    private void processPowerKey(Context context, int eventAction, int eventCode, long eventDnTime, long eventTime) {
        if (!mIsSidePowerFpComb || !this.mHwPWMEx.isPowerFpForbidGotoSleep()) {
            boolean down = eventAction == 0;
            if (eventCode != 26) {
                Log.d(TAG, "Not POWER Key." + eventCode);
                return;
            }
            if (down) {
                this.mPowerKeyHandledByHiaction = false;
                this.mHandler.postDelayed(this.mAIPowerLongPressed, POWERKEY_LONG_PRESS_TIMEOUT);
                this.mHandler.postDelayed(this.mAIPowerSuperLongPressed, 3000);
            } else {
                Log.i(TAG, "Power up eventTime " + eventTime + " DnTime " + eventDnTime);
                cancelAIPowerLongPressed();
                cancelAIPowerSuperLongPressed();
            }
            return;
        }
        Log.d(TAG, "Is Side PowerFpComb ");
    }

    private void wakeupVassistantService(KeyEvent event) {
        notifyVassistantService("wakeup", 2, event);
    }

    public void processHomoAiKey(Context context, int eventAction, int eventCode, long eventDnTime, long eventTime) {
        int i = eventCode;
        long j = eventDnTime;
        long j2 = eventTime;
        boolean down = eventAction == 0;
        long timeDiff = j2 - j;
        if (!this.mSystemReady) {
            Log.i(TAG, "System is not ready, just discard it this time.");
            return;
        }
        if (this.DEBUG_SMARTKEY) {
            Log.i(TAG, "handle AiHome keycode = " + i + " down = " + down + " downTime = " + j);
        }
        if (i != 308) {
            Log.d(TAG, "Not SmartKey." + i);
            return;
        }
        if (down) {
            handleHomoAiKeyDownEvent();
            this.mHandler.postDelayed(this.mAIHomoLongPressed, 500);
            this.mHandler.postDelayed(this.mAIHomoSuperLongPressed, 1000);
        } else {
            Log.i(TAG, "AiHome up eventTime " + j2 + "DnTime" + j);
            if (timeDiff < 500) {
                cancelAIHomeLongPressed();
                cancelAIHomeSuperLongPressed();
                handleHomoAiKeyShortPress();
            } else if (timeDiff < 1000) {
                cancelAIHomeSuperLongPressed();
                handleHomoAiKeyLongup();
            } else {
                handleHomoAiKeyLongup();
            }
        }
    }

    private void notifyHomoAiKeyEventToHiAction(Context context, KeyEvent event) {
        if (HIVOICE_PRESS_TYPE_HOMO.equals(this.mHiVoiceKeyType)) {
            processHomoAiKey(this.mContext, event.getAction(), event.getKeyCode(), event.getDownTime(), event.getEventTime());
        }
    }

    /* access modifiers changed from: private */
    public void powerOffToast() {
        Toast toast = Toast.makeText(this.mContext, this.mContext.getString(33686247, new Object[]{3}), 1);
        toast.getWindowParams().type = TOAST_TYPE_COVER_SCREEN;
        toast.getWindowParams().privateFlags |= 16;
        toast.show();
        this.showPowerOffToastTimes++;
        this.mPowerOffToastShown = true;
        Settings.Secure.putIntForUser(this.mResolver, KEY_TOAST_POWER_OFF, this.showPowerOffToastTimes, ActivityManager.getCurrentUser());
    }

    private void cancelPowerOffToast() {
        this.mHandler.removeCallbacks(this.mPowerOffRunner);
    }

    private void showPowerOffToast(boolean isScreenOn) {
        if (isScreenOn && this.mBooted) {
            if ((!IS_POWER_HIACTION_KEY || !HIVOICE_PRESS_TYPE_POWER.equals(this.mHiVoiceKeyType)) && this.showPowerOffToastTimes < 2) {
                this.mHandler.postDelayed(this.mPowerOffRunner, 1000);
            }
        }
    }

    private void notifyHomoAiKeyEvent(int homoType, int pressType) {
        Intent intent = new Intent(HUAWEI_HIACTION_ACTION);
        intent.setPackage(HUAWEI_HIACTION_PACKAGE);
        intent.putExtra(HOMOAI_EVENT_TAG, homoType);
        intent.putExtra(HOMOAI_PRESS_TAG, pressType);
        if (homoType == 1) {
            powerPressBDReport(984);
        }
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } catch (Exception e) {
            Log.e(TAG, "start HiAction server err");
        }
        Log.i(TAG, "send HomoAi key " + homoType + " pressType " + pressType);
    }

    /* access modifiers changed from: private */
    public void handleHomoAiKeyLongPress(int pressType) {
        notifyHomoAiKeyEvent(1, pressType);
    }

    private void handleHomoAiKeyShortPress() {
        notifyHomoAiKeyEvent(2, 0);
    }

    /* access modifiers changed from: private */
    public void handleHomoAiKeySuperLongPress(int pressType) {
        notifyHomoAiKeyEvent(3, pressType);
    }

    private void handleHomoAiKeyDownEvent() {
        notifyHomoAiKeyEvent(5, 0);
    }

    private void handleHomoAiKeyLongup() {
        notifyHomoAiKeyEvent(4, 0);
    }

    private void notifyPowerKeyEventToHiAction(Context context, KeyEvent event) {
        if (IS_POWER_HIACTION_KEY && HIVOICE_PRESS_TYPE_POWER.equals(this.mHiVoiceKeyType)) {
            processPowerKey(this.mContext, event.getAction(), event.getKeyCode(), event.getDownTime(), event.getEventTime());
        }
    }

    /* access modifiers changed from: private */
    public void notifySmartKeyEvent(String strType) {
        Intent intent = new Intent(HUAWEI_SMARTKEY_PACKAGE);
        intent.setFlags(268435456);
        intent.putExtra(SMARTKEY_TAG, strType);
        this.mContext.sendBroadcast(intent);
        Log.i(TAG, "send smart key " + strType);
        if ((!this.mIsProximity && this.mSensorRegisted) || !this.mSensorRegisted || (isPhoneInCall() && SMARTKEY_LP.equals(strType))) {
            intent.setPackage(HUAWEI_SMARTKEY_SERVICE);
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
            Log.i(TAG, "notify smartkey service " + strType);
        }
        turnOffSensorListener();
    }

    public boolean getNeedDropFingerprintEvent() {
        return this.mNeedDropFingerprintEvent;
    }

    private String getTopActivity() {
        return ServiceManager.getService("activity").topAppName();
    }

    private void initDropSmartKey() {
        String dropSmartKeyActivity = SettingsEx.Systemex.getString(this.mResolver, DROP_SMARTKEY_ACTIVITY);
        if (TextUtils.isEmpty(dropSmartKeyActivity)) {
            Log.w(TAG, "dropSmartKeyActivity not been configured in hw_defaults.xml!");
            return;
        }
        for (String str : dropSmartKeyActivity.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
            this.needDropSmartKeyActivities.add(str);
        }
    }

    private boolean needDropSmartKey() {
        boolean result = false;
        String topActivityName = getTopActivity();
        if (this.needDropSmartKeyActivities != null && this.needDropSmartKeyActivities.contains(topActivityName)) {
            result = true;
            Log.d(TAG, "drop smartkey event because of conflict with fingerprint authentication!");
        }
        if ((!isCamera() || !this.isFingerShotCameraOn) && ((!isInCallUIAndRinging() || !this.isFingerAnswerPhoneOn) && (!isAlarm(this.mCurUser) || !this.isFingerStopAlarmOn))) {
            return result;
        }
        Log.d(TAG, "drop smartkey event because of conflict with fingerprint longpress event!");
        return true;
    }

    private boolean isCamera() {
        String pkgName = getTopActivity();
        return pkgName != null && pkgName.startsWith("com.huawei.camera");
    }

    public boolean isKeyguardShortcutApps() {
        try {
            String focusPackageName = this.mFocusedWindow.getAttrs().packageName;
            if (focusPackageName == null) {
                return false;
            }
            for (String startsWith : this.mKeyguardShortcutApps) {
                if (focusPackageName.startsWith(startsWith)) {
                    return true;
                }
            }
            return false;
        } catch (NullPointerException e) {
            Log.e(TAG, "isKeyguardShortcutApps error : " + e);
            return false;
        }
    }

    public boolean isLsKeyguardShortcutApps() {
        try {
            String focusPackageName = this.mFocusedWindow.getAttrs().packageName;
            if (focusPackageName == null) {
                return false;
            }
            for (String startsWith : this.mLsKeyguardShortcutApps) {
                if (focusPackageName.startsWith(startsWith)) {
                    return true;
                }
            }
            return false;
        } catch (NullPointerException e) {
            Log.e(TAG, "isLsKeyguardShortcutApps error : " + e);
            return false;
        }
    }

    public void onProximityPositive() {
        Log.i(TAG, "onProximityPositive");
        this.mHwScreenOnProximityLock.releaseLock(1);
        this.mHwScreenOnProximityLock.unregisterDeviceListener();
    }

    private boolean isInCallUIAndRinging() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        return telecomManager != null && telecomManager.isRinging();
    }

    private boolean isAlarm(int user) {
        return ((HwActivityManagerService) ServiceManager.getService("activity")).serviceIsRunning(ComponentName.unflattenFromString("com.android.deskclock/.alarmclock.AlarmKlaxon"), user);
    }

    public void waitKeyguardDismissDone(WindowManagerPolicy.KeyguardDismissDoneListener listener) {
        synchronized (this.mLock) {
            this.mKeyguardDismissListener = listener;
        }
        this.mWindowManagerInternal.waitForKeyguardDismissDone(this.mKeyguardDismissDoneCallback, POWER_SOS_MISTOUCH_THRESHOLD);
    }

    public void cancelWaitKeyguardDismissDone() {
        synchronized (this.mLock) {
            this.mKeyguardDismissListener = null;
        }
    }

    /* access modifiers changed from: protected */
    public void finishKeyguardDismissDone() {
        WindowManagerPolicy.KeyguardDismissDoneListener listener;
        synchronized (this.mLock) {
            listener = this.mKeyguardDismissListener;
            this.mKeyguardDismissListener = null;
        }
        if (listener != null) {
            listener.onKeyguardDismissDone();
        }
    }

    public void setInterceptInputForWaitBrightness(boolean intercept) {
        this.mInterceptInputForWaitBrightness = intercept;
    }

    public boolean getInterceptInputForWaitBrightness() {
        return this.mInterceptInputForWaitBrightness;
    }

    private void interceptBackandMenuKey() {
        long now = SystemClock.uptimeMillis();
        if (isScreenInLockTaskMode() && this.mBackKeyPress && this.mMenuKeyPress && now <= this.mBackKeyPressTime + TOUCH_SPINNING_DELAY_MILLIS && now <= this.mMenuKeyPressTime + TOUCH_SPINNING_DELAY_MILLIS) {
            this.mBackKeyPress = false;
            this.mMenuKeyPress = false;
            this.mBackKeyPressTime = 0;
            this.mMenuKeyPressTime = 0;
        }
    }

    private boolean isScreenInLockTaskMode() {
        try {
            return ActivityManagerNative.getDefault().isInLockTaskMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isScreenInLockTaskMode  ", e);
            return false;
        }
    }

    public boolean isStatusBarObsecured() {
        return this.mStatuBarObsecured;
    }

    /* access modifiers changed from: package-private */
    public boolean isStatusBarObsecuredByWin(WindowManagerPolicy.WindowState win) {
        boolean z = false;
        if (win == null || this.mStatusBar == null || (win.getAttrs().flags & 16) != 0 || win.toString().contains("hwSingleMode_window")) {
            return false;
        }
        Rect winFrame = win.getFrameLw();
        Rect statusbarFrame = this.mStatusBar.getFrameLw();
        if (winFrame.top <= statusbarFrame.top && winFrame.bottom >= statusbarFrame.bottom && winFrame.left <= statusbarFrame.left && winFrame.right >= statusbarFrame.right) {
            z = true;
        }
        return z;
    }

    public void setNaviImmersiveMode(boolean mode) {
        if (this.mNavigationBarPolicy != null) {
            this.mNavigationBarPolicy.setImmersiveMode(mode);
        }
        this.mIsImmersiveMode = mode;
    }

    public boolean getImmersiveMode() {
        return this.mIsImmersiveMode;
    }

    public void adjustConfigurationLw(Configuration config, int keyboardPresence, int navigationPresence) {
        HwPhoneWindowManager.super.adjustConfigurationLw(config, keyboardPresence, navigationPresence);
        int tempDpi = config.densityDpi;
        if (tempDpi != this.lastDensityDpi) {
            if (this.systemWideActionsListener != null) {
                this.systemWideActionsListener.updateConfiguration();
                this.lastDensityDpi = tempDpi;
            }
            if (this.mStylusGestureListener != null) {
                this.mStylusGestureListener.updateConfiguration();
                this.lastDensityDpi = tempDpi;
            }
        }
    }

    public void setRotationLw(int rotation) {
        HwPhoneWindowManager.super.setRotationLw(rotation);
        this.mCurrentRotation = rotation;
        Log.d(TAG, "PhoneWindowManager setRotationLw(" + rotation + ")");
        IHwAftPolicyService hwAft = HwAftPolicyManager.getService();
        if (hwAft != null) {
            try {
                hwAft.notifyOrientationChange(rotation);
            } catch (RemoteException e) {
                Log.e(TAG, "setRotationLw throw " + e);
            }
        }
        if (this.mGestureNavManager != null) {
            this.mGestureNavManager.onRotationChanged(rotation);
        }
        if (this.mSubScreenViewEntry != null) {
            this.mSubScreenViewEntry.onRotationChanged(rotation);
        }
    }

    public boolean performHapticFeedbackLw(WindowManagerPolicy.WindowState win, int effectId, boolean always) {
        return HwPhoneWindowManager.super.performHapticFeedbackLw(win, effectId, always);
    }

    public void setNavibarAlignLeftWhenLand(boolean isLeft) {
        this.mIsNavibarAlignLeftWhenLand = isLeft;
    }

    public boolean getNavibarAlignLeftWhenLand() {
        return this.mIsNavibarAlignLeftWhenLand;
    }

    public boolean isPhoneIdle() {
        ITelephony telephonyService = getTelephonyService();
        if (telephonyService == null) {
            return false;
        }
        try {
            if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
                return telephonyService.isIdle(this.mContext.getPackageName());
            }
            boolean isIdle = false;
            boolean isIdleForSubscriberSub1 = telephonyService.isIdleForSubscriber(0, this.mContext.getPackageName());
            boolean isIdleForSubscriberSub2 = telephonyService.isIdleForSubscriber(1, this.mContext.getPackageName());
            if (isIdleForSubscriberSub1 && isIdleForSubscriberSub2) {
                isIdle = true;
            }
            return isIdle;
        } catch (RemoteException ex) {
            Log.w(TAG, "ITelephony threw RemoteException", ex);
            return false;
        }
    }

    public int getDisabledKeyEventResult(int keyCode) {
        if (keyCode != 187) {
            switch (keyCode) {
                case 3:
                    if ((this.mCust == null || !this.mCust.disableHomeKey(this.mContext)) && !HwDeviceManager.disallowOp(14)) {
                        return -2;
                    }
                    Log.i(TAG, "the device's home key has been disabled for the user.");
                    return 0;
                case 4:
                    if (!HwDeviceManager.disallowOp(16)) {
                        return -2;
                    }
                    Log.i(TAG, "the device's back key has been disabled for the user.");
                    return -1;
                default:
                    return -2;
            }
        } else if (!HwDeviceManager.disallowOp(15)) {
            return -2;
        } else {
            Log.i(TAG, "the device's task key has been disabled for the user.");
            return 0;
        }
    }

    private int getGameControlKeyReslut(KeyEvent event) {
        int result = -2;
        if (!mSupportGameAssist || this.mAms == null) {
            return -2;
        }
        int keyCode = event.getKeyCode();
        boolean isGameKeyControlOn = (!this.mEnableKeyInCurrentFgGameApp && this.mAms.isGameKeyControlOn()) || this.mLastKeyDownDropped;
        Log.d(TAG, "deviceId:" + event.getDeviceId() + " mFingerPrintId:" + this.mFingerPrintId + " isGameKeyControlOn:" + isGameKeyControlOn + ",EnableKey=" + this.mEnableKeyInCurrentFgGameApp + ",mLastKeyDownDropped=" + this.mLastKeyDownDropped);
        if (isGameKeyControlOn) {
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                if (this.mTrikeyNaviMode < 0) {
                    Log.d(TAG, "trikey single mode.");
                    result = performGameMode(event, 0);
                } else {
                    Log.d(TAG, "trikey three mode.");
                    result = performGameMode(event, 1);
                }
            } else if (!FrontFingerPrintSettings.isNaviBarEnabled(this.mResolver)) {
                Log.d(TAG, "single key.");
                result = performGameMode(event, 2);
            } else if (keyCode == 3 && event.getDeviceId() == this.mFingerPrintId) {
                Log.d(TAG, "NaviBarEnabled KEYCODE_HOME !");
                result = performGameMode(event, 3);
            }
        }
        return result;
    }

    private int performGameMode(KeyEvent event, int naviMode) {
        int result = -2;
        boolean z = true;
        boolean isSingleKeyMode = naviMode == 0 || naviMode == 2;
        int keyCode = event.getKeyCode();
        long keyTime = event.getEventTime();
        boolean isKeyDown = event.getAction() == 0;
        boolean initialDown = isKeyDown && event.getRepeatCount() == 0;
        if (this.mLastKeyDownDropped && initialDown) {
            this.mLastKeyDownDropped = false;
        }
        if (keyCode != 187) {
            switch (keyCode) {
                case 3:
                    if (!isSingleKeyMode) {
                        if (!isKeyDown) {
                            result = getClickResult(keyTime, keyCode);
                            break;
                        }
                    } else {
                        return -2;
                    }
                    break;
                case 4:
                    if (initialDown) {
                        result = getClickResult(keyTime, keyCode);
                        if (result == -2) {
                            z = false;
                        }
                        this.mLastKeyDownDropped = z;
                        break;
                    } else if (this.mLastKeyDownDropped) {
                        Log.d(TAG, "drop key up for last event beacause down dropped.");
                        if (!isKeyDown) {
                            this.mLastKeyDownDropped = false;
                        }
                        return -1;
                    }
                    break;
            }
        } else if (isSingleKeyMode) {
            return -1;
        } else {
            if (!isKeyDown) {
                result = getClickResult(keyTime, keyCode);
                this.mHandlerEx.removeMessages(MSG_TRIKEY_RECENT_LONG_PRESS);
            }
        }
        return result;
    }

    private int getClickResult(long eventTime, int keyCode) {
        int result;
        if (this.mEnableKeyInCurrentFgGameApp || eventTime - this.mLastKeyDownTime >= POWER_SOS_MISTOUCH_THRESHOLD || this.mLastKeyDownKeyCode != keyCode || this.mLastKeyDownTime - this.mSecondToLastKeyDownTime >= POWER_SOS_MISTOUCH_THRESHOLD || this.mSecondToLastKeyDownKeyCode != this.mLastKeyDownKeyCode) {
            result = -1;
        } else {
            Log.i(TAG, "Navigation keys unlocked.");
            result = -1;
            this.mEnableKeyInCurrentFgGameApp = true;
            showKeyEnableToast();
            Flog.bdReport(this.mContext, HttpConnectionBase.SERVER_OVERLOAD_ERRORCODE);
        }
        this.mSecondToLastKeyDownTime = this.mLastKeyDownTime;
        this.mSecondToLastKeyDownKeyCode = this.mLastKeyDownKeyCode;
        this.mLastKeyDownTime = eventTime;
        this.mLastKeyDownKeyCode = keyCode;
        Log.i(TAG, "getClickResult result:" + result + ",keyCode:" + keyCode + ",EnableKey:" + this.mEnableKeyInCurrentFgGameApp);
        return result;
    }

    private void showKeyEnableToast() {
        this.mHandler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(HwPhoneWindowManager.this.mContext, 33686245, 0);
                toast.getWindowParams().type = HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL;
                toast.getWindowParams().privateFlags |= 16;
                toast.show();
            }
        });
    }

    public void onPointDown() {
        this.mTouchCountPolicy.updateTouchCountInfo();
    }

    public int[] getTouchCountInfo() {
        return this.mTouchCountPolicy.getTouchCountInfo();
    }

    public int[] getDefaultTouchCountInfo() {
        return this.mTouchCountPolicy.getDefaultTouchCountInfo();
    }

    private boolean isTrikeyNaviKeycodeFromLON(boolean isInjected, boolean excluded) {
        int frontFpNaviTriKey = FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
        Log.d(TAG, "frontFpNaviTriKey:" + frontFpNaviTriKey + " isInjected:" + isInjected + " mTrikeyNaviMode:" + this.mTrikeyNaviMode + " excluded:" + excluded + " isFrontFPGestureNavEnable:" + isFrontFPGestureNavEnable());
        return frontFpNaviTriKey == 0 || (!isInjected && this.mTrikeyNaviMode < 0) || excluded || isFrontFPGestureNavEnable();
    }

    public boolean isSupportCover() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "cover_enabled", 1) != 0;
    }

    public boolean isSmartCoverMode() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "cover_type", 0) == 1;
    }

    public boolean isInCallActivity() {
        String pkgName = getTopActivity();
        return pkgName != null && pkgName.startsWith("com.android.incallui");
    }

    public boolean isInterceptAndCheckRinging(boolean isIntercept) {
        TelecomManager telecomManager = getTelecommService();
        if (!isIntercept || (telecomManager != null && telecomManager.isRinging())) {
            return false;
        }
        return true;
    }

    public int getSingAppKeyEventResult(int keyCode) {
        String packageName = HwDeviceManager.getString(34);
        if (packageName == null || packageName.isEmpty()) {
            return -2;
        }
        boolean[] results = isNeedStartSingleApp(packageName);
        switch (keyCode) {
            case 3:
                if (!results[0]) {
                    Log.i(TAG, "Single app model running, start the single app's main activity.");
                    startSingleApp(packageName);
                }
                return 0;
            case 4:
                if (results[0]) {
                    return -1;
                }
                if (!results[1]) {
                    return -2;
                }
                Log.i(TAG, "Single app model running, start the single app's main activity.");
                startSingleApp(packageName);
                return -1;
            default:
                return -2;
        }
    }

    private boolean[] isNeedStartSingleApp(String packageName) {
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        boolean[] results = {false, false};
        if (activityManager != null) {
            try {
                List<ActivityManager.RunningTaskInfo> runningTask = activityManager.getRunningTasks(2);
                if (runningTask != null && runningTask.size() > 0) {
                    ComponentName cn = runningTask.get(0).topActivity;
                    if (cn != null) {
                        String currentAppName = cn.getPackageName();
                        String currentActivityName = cn.getClassName();
                        PackageManager pm = this.mContext.getPackageManager();
                        ComponentName ac = pm.getLaunchIntentForPackage(packageName).resolveActivity(pm);
                        String mainClassName = ac != null ? ac.getClassName() : null;
                        if (mainClassName != null && mainClassName.equals(currentActivityName) && packageName.equals(currentAppName)) {
                            results[0] = true;
                        }
                    }
                    String nextAppName = null;
                    if (runningTask.size() > 1) {
                        nextAppName = runningTask.get(1).topActivity.getPackageName();
                    }
                    if ((runningTask.get(0).numActivities <= 1 && (runningTask.size() <= 1 || !packageName.equals(nextAppName))) || FingerViewController.PKGNAME_OF_KEYGUARD.equals(nextAppName)) {
                        results[1] = true;
                    }
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "isTopApp->RuntimeException happened");
            } catch (Exception e2) {
                Log.e(TAG, "isTopApp->other exception happened");
            }
        }
        return results;
    }

    private void startSingleApp(String packageName) {
        Intent launchIntent = this.mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            this.mContext.startActivity(launchIntent);
        }
    }

    public boolean isKeyguardOccluded() {
        return this.mKeyguardOccluded;
    }

    /* access modifiers changed from: protected */
    public void notifyPowerkeyInteractive(boolean bool) {
        AwareFakeActivityRecg.self().notifyPowerkeyInteractive(true);
    }

    public void setNavigationBarExternal(WindowManagerPolicy.WindowState state) {
        this.mNavigationBarExternal = state;
    }

    public WindowManagerPolicy.WindowState getNavigationBarExternal() {
        return this.mNavigationBarExternal;
    }

    public void removeWindowLw(WindowManagerPolicy.WindowState win) {
        HwPhoneWindowManager.super.removeWindowLw(win);
        if (!HwPCUtils.enabled()) {
            return;
        }
        if (getNavigationBarExternal() == win) {
            setNavigationBarExternal(null);
            this.mNavigationBarControllerExternal.setWindow(null);
        } else if (this.mLighterDrawView == win) {
            this.mLighterDrawView = null;
        }
    }

    public int getConfigDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId, DisplayCutout displayCutout) {
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) {
            return HwPhoneWindowManager.super.getConfigDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayId, displayCutout);
        }
        return getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayId, displayCutout);
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        HwPhoneWindowManager.super.dump(prefix, pw, args);
        if (HwPCUtils.isPcCastModeInServer() && getNavigationBarExternal() != null) {
            pw.print(prefix);
            pw.print("mNavigationBarExternal=");
            pw.println(getNavigationBarExternal());
        }
        if (HwPCUtils.isPcCastModeInServer() && this.mNavigationBarControllerExternal != null) {
            this.mNavigationBarControllerExternal.dump(pw, prefix);
        }
        if (this.mGestureNavManager != null) {
            this.mGestureNavManager.dump(prefix, pw, args);
        }
    }

    public int focusChangedLw(WindowManagerPolicy.WindowState lastFocus, WindowManagerPolicy.WindowState newFocus) {
        if (HwPCUtils.isPcCastModeInServer() && newFocus != null && HwPCUtils.isValidExtDisplayId(newFocus.getDisplayId())) {
            return 1;
        }
        if (this.mFalseTouchMonitor != null) {
            this.mFalseTouchMonitor.handleFocusChanged(lastFocus, newFocus);
        }
        if (this.mGestureNavManager != null) {
            this.mFocusWindowUsingNotch = this.mGestureNavManager.onFocusWindowChanged(lastFocus, newFocus);
        }
        return HwPhoneWindowManager.super.focusChangedLw(lastFocus, newFocus);
    }

    public void layoutWindowLw(WindowManagerPolicy.WindowState win, WindowManagerPolicy.WindowState attached, DisplayFrames displayFrames) {
        if (!HwPCUtils.isPcCastModeInServer() || win != getNavigationBarExternal()) {
            HwPhoneWindowManager.super.layoutWindowLw(win, attached, displayFrames);
            if (IS_NOTCH_PROP && this.mIsGestureNavEnable && isFocusedWindow(win)) {
                boolean oldUsingNotch = this.mFocusWindowUsingNotch;
                this.mFocusWindowUsingNotch = !this.mLayoutBeyondDisplayCutout;
                if (oldUsingNotch != this.mFocusWindowUsingNotch) {
                    onLayoutInDisplayCutoutModeChanged(win, oldUsingNotch, this.mFocusWindowUsingNotch);
                }
            }
        }
    }

    public void beginLayoutLw(DisplayFrames displayFrames, int uiMode) {
        DisplayFrames displayFrames2 = displayFrames;
        HwPhoneWindowManager.super.beginLayoutLw(displayFrames, uiMode);
        if (!HwPCUtils.isPcCastModeInServer() || getNavigationBarExternal() == null || !HwPCUtils.isValidExtDisplayId(displayFrames2.mDisplayId)) {
            DisplayFrames displayFrames3 = displayFrames2;
            return;
        }
        if (this.mNavigationBarHeightExternal == 0 && this.mContext != null) {
            initNavigationBarHightExternal(((DisplayManager) this.mContext.getSystemService("display")).getDisplay(displayFrames2.mDisplayId), displayFrames2.mDisplayWidth, displayFrames2.mDisplayHeight);
        }
        Rect pf = mTmpParentFrame;
        Rect df = mTmpDisplayFrame;
        Rect of = mTmpOverscanFrame;
        Rect vf = mTmpVisibleFrame;
        Rect dcf = mTmpDecorFrame;
        int i = displayFrames2.mDock.left;
        vf.left = i;
        of.left = i;
        df.left = i;
        pf.left = i;
        int i2 = displayFrames2.mDock.top;
        vf.top = i2;
        of.top = i2;
        df.top = i2;
        pf.top = i2;
        int i3 = displayFrames2.mDock.right;
        vf.right = i3;
        of.right = i3;
        df.right = i3;
        pf.right = i3;
        int i4 = displayFrames2.mDock.bottom;
        vf.bottom = i4;
        of.bottom = i4;
        df.bottom = i4;
        pf.bottom = i4;
        dcf.setEmpty();
        Rect dcf2 = dcf;
        Rect vf2 = vf;
        layoutNavigationBarExternal(displayFrames2.mDisplayWidth, displayFrames2.mDisplayHeight, displayFrames2.mRotation, uiMode, 0, 0, 0, dcf, true, false, false, false, false, displayFrames);
        DisplayFrames displayFrames4 = displayFrames;
        int i5 = displayFrames4.mUnrestricted.left;
        Rect of2 = of;
        of2.left = i5;
        Rect df2 = df;
        df2.left = i5;
        Rect pf2 = pf;
        pf2.left = i5;
        int i6 = displayFrames4.mUnrestricted.top;
        of2.top = i6;
        df2.top = i6;
        pf2.top = i6;
        int i7 = displayFrames4.mUnrestricted.right;
        of2.right = i7;
        df2.right = i7;
        pf2.right = i7;
        int i8 = displayFrames4.mUnrestricted.bottom;
        of2.bottom = i8;
        df2.bottom = i8;
        pf2.bottom = i8;
        Rect vf3 = vf2;
        vf3.left = displayFrames4.mStable.left;
        vf3.top = displayFrames4.mStable.top;
        vf3.right = displayFrames4.mStable.right;
        vf3.bottom = displayFrames4.mStable.bottom;
        if (HwPCUtils.enabledInPad()) {
            layoutStatusBarExternal(pf2, df2, of2, vf3, dcf2, displayFrames4);
        }
    }

    private boolean layoutStatusBarExternal(Rect pf, Rect df, Rect of, Rect vf, Rect dcf, DisplayFrames displayFrames) {
        Rect rect = pf;
        Rect rect2 = df;
        Rect rect3 = of;
        Rect rect4 = vf;
        DisplayFrames displayFrames2 = displayFrames;
        if (this.mStatusBar != null) {
            int i = displayFrames2.mUnrestricted.left;
            rect3.left = i;
            rect2.left = i;
            rect.left = i;
            int i2 = displayFrames2.mUnrestricted.right;
            rect3.right = i2;
            rect2.right = i2;
            rect.right = i2;
            int i3 = displayFrames2.mUnrestricted.top;
            rect3.top = i3;
            rect2.top = i3;
            rect.top = i3;
            int i4 = displayFrames2.mUnrestricted.bottom;
            rect3.bottom = i4;
            rect2.bottom = i4;
            rect.bottom = i4;
            rect4.left = displayFrames2.mStable.left;
            rect4.top = displayFrames2.mStable.top;
            rect4.right = displayFrames2.mStable.right;
            rect4.bottom = displayFrames2.mStable.bottom;
            this.mStatusBar.computeFrameLw(rect, rect2, rect4, rect4, rect4, dcf, rect4, rect4, displayFrames2.mDisplayCutout, false);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean layoutNavigationBarExternal(int displayWidth, int displayHeight, int displayRotation, int uiMode, int overscanLeft, int overscanRight, int overscanBottom, Rect dcf, boolean navVisible, boolean navTranslucent, boolean navAllowedHidden, boolean isKeyguardOn, boolean statusBarExpandedNotKeyguard, DisplayFrames displayFrames) {
        DisplayFrames displayFrames2 = displayFrames;
        this.mNavigationBarPosition = 4;
        mTmpNavigationFrame.set(0, (displayHeight - overscanBottom) - getNavigationBarHeightExternal(), displayWidth, displayHeight - overscanBottom);
        Rect rect = displayFrames2.mStable;
        Rect rect2 = displayFrames2.mStableFullscreen;
        int i = mTmpNavigationFrame.top;
        rect2.bottom = i;
        rect.bottom = i;
        this.mNavigationBarControllerExternal.setBarShowingLw(true);
        displayFrames2.mDock.bottom = displayFrames2.mStable.bottom;
        displayFrames2.mRestricted.bottom = displayFrames2.mStable.bottom;
        displayFrames2.mRestrictedOverscan.bottom = displayFrames2.mDock.bottom;
        displayFrames2.mSystem.bottom = displayFrames2.mStable.bottom;
        displayFrames2.mContent.set(displayFrames2.mDock);
        displayFrames2.mVoiceContent.set(displayFrames2.mDock);
        displayFrames2.mCurrent.set(displayFrames2.mDock);
        getNavigationBarExternal().computeFrameLw(mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, dcf, mTmpNavigationFrame, mTmpNavigationFrame, displayFrames2.mDisplayCutout, false);
        return false;
    }

    /* access modifiers changed from: protected */
    public int getNavigationBarHeightExternal() {
        return this.mNavigationBarHeightExternal;
    }

    public int prepareAddWindowLw(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs) {
        if (HwPCUtils.isPcCastModeInServer()) {
            if (attrs.type != 2019 || !HwPCUtils.isValidExtDisplayId(win.getDisplayId())) {
                if (attrs.type == 2104 && HwPCUtils.isValidExtDisplayId(win.getDisplayId())) {
                    if (this.mLighterDrawView != null) {
                        return -7;
                    }
                    this.mLighterDrawView = win;
                    return 0;
                }
            } else if (getNavigationBarExternal() != null && getNavigationBarExternal().isAlive()) {
                return -7;
            } else {
                setNavigationBarExternal(win);
                this.mNavigationBarControllerExternal.setWindow(win);
                return 0;
            }
        }
        return HwPhoneWindowManager.super.prepareAddWindowLw(win, attrs);
    }

    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets, int displayId, DisplayCutout displayCutout) {
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) {
            getStableInsetsLw(displayRotation, displayWidth, displayHeight, displayCutout, outInsets);
            return;
        }
        outInsets.setEmpty();
        getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, outInsets, displayId, displayCutout);
        outInsets.top = 0;
    }

    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets, int displayId, DisplayCutout displayCutout) {
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) {
            getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, displayCutout, outInsets);
            return;
        }
        outInsets.setEmpty();
        if (this.mHasNavigationBar) {
            if (getNavigationBarExternal() == null || !getNavigationBarExternal().isVisibleLw()) {
                outInsets.bottom = 0;
            } else {
                outInsets.bottom = getNavigationBarHeightExternal();
            }
        }
    }

    public void showTopBar() {
        if (HwPCUtils.enabled() && !isCloudOnPCTOP()) {
            try {
                IHwPCManager pcManager = HwPCUtils.getHwPCManager();
                if (pcManager != null) {
                    pcManager.showTopBar();
                }
            } catch (Exception e) {
                Log.e(TAG, "RemoteException");
            }
        }
    }

    private boolean isScreenLocked() {
        KeyguardManager km = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (km == null || !km.isKeyguardLocked()) {
            return false;
        }
        return true;
    }

    private boolean handleDesktopKeyEvent(KeyEvent event) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return false;
        }
        int keyCode = event.getKeyCode();
        boolean down = event.getAction() == 0;
        int repeatCount = event.getRepeatCount();
        int displayId = this.mWindowManagerInternal.getFocusedDisplayId();
        if (!HwPCUtils.isValidExtDisplayId(displayId) && !HwPCUtils.enabledInPad()) {
            this.isHomePressDown = false;
            this.isHomeAndEBothDown = false;
            this.isHomeAndLBothDown = false;
            this.isHomeAndDBothDown = false;
            return false;
        } else if (HwPCUtils.enabledInPad() && handleExclusiveKeykoard(event).booleanValue()) {
            return true;
        } else {
            if (keyCode == 120 && down && repeatCount == 0) {
                screenshotPc();
                return true;
            } else if (keyCode == 134 && down && event.isAltPressed() && repeatCount == 0) {
                closeTopWindow();
                return true;
            } else if (keyCode == 33 && down && this.isHomePressDown && repeatCount == 0) {
                this.isHomeAndEBothDown = true;
                ComponentName componentName = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.activities.MainActivity");
                Intent intent = new Intent();
                intent.setFlags(402653184);
                intent.setComponent(componentName);
                this.mContext.createDisplayContext(DisplayManagerGlobal.getInstance().getRealDisplay(displayId)).startActivity(intent);
                return true;
            } else if (keyCode == 40 && down && this.isHomePressDown && repeatCount == 0) {
                this.isHomeAndLBothDown = true;
                lockScreen(true);
                return true;
            } else if (keyCode != 32 || !down || !this.isHomePressDown || repeatCount != 0) {
                if (down && repeatCount == 0 && keyCode == 61) {
                    if (this.mRecentAppsHeldModifiers == 0 && !keyguardOn() && isUserSetupComplete()) {
                        int shiftlessModifiers = event.getModifiers() & -194;
                        if (KeyEvent.metaStateHasModifiers(shiftlessModifiers, 2)) {
                            this.mRecentAppsHeldModifiers = shiftlessModifiers;
                            triggerSwitchTaskView(true);
                            return true;
                        }
                    }
                } else if (!down && this.mRecentAppsHeldModifiers != 0 && (event.getMetaState() & this.mRecentAppsHeldModifiers) == 0) {
                    this.mRecentAppsHeldModifiers = 0;
                    triggerSwitchTaskView(false);
                }
                if ((HwPCUtils.enabledInPad() || keyCode != 3) && keyCode != 117 && keyCode != 118) {
                    return false;
                }
                if (!down) {
                    if (this.isHomeAndEBothDown) {
                        this.isHomeAndEBothDown = false;
                    } else if (this.isHomeAndLBothDown) {
                        this.isHomeAndLBothDown = false;
                    } else if (this.isHomeAndDBothDown) {
                        this.isHomeAndDBothDown = false;
                    } else {
                        showStartMenu();
                    }
                    this.isHomePressDown = false;
                } else {
                    this.isHomePressDown = true;
                }
                return true;
            } else {
                this.isHomeAndDBothDown = true;
                toggleHome();
                return true;
            }
        }
    }

    private Boolean handleExclusiveKeykoard(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean down = event.getAction() == 0;
        int repeatCount = event.getRepeatCount();
        switch (keyCode) {
            case 3:
                dispatchKeyEventForExclusiveKeyboard(event);
                return true;
            case 4:
                if (down && repeatCount == 0) {
                    dispatchKeyEventForExclusiveKeyboard(event);
                    return false;
                }
            case WMStateCons.MSG_FREQUENTLOCATIONSTATE_WIFI_UPDATE_SCAN_RESULT:
                if (down && event.isShiftPressed()) {
                    dispatchKeyEventForExclusiveKeyboard(event);
                    return true;
                }
            case CPUFeature.MSG_UNIPERF_BOOST_ON /*118*/:
                if (down) {
                    dispatchKeyEventForExclusiveKeyboard(event);
                }
                return true;
            case 187:
                if (isScreenLocked()) {
                    HwPCUtils.log(TAG, "ScreenLocked! Not handle" + event);
                    return true;
                }
                dispatchKeyEventForExclusiveKeyboard(event);
                return true;
            case 220:
            case HwMpLinkServiceImpl.MPLINK_MSG_MOBILE_DATA_AVAILABLE:
                if (!isScreenLocked()) {
                    dispatchKeyEventForExclusiveKeyboard(event);
                    break;
                } else {
                    HwPCUtils.log(TAG, "ScreenLocked! Not handle" + event);
                    return true;
                }
            default:
                return false;
        }
        return false;
    }

    public void overrideRectForForceRotation(WindowManagerPolicy.WindowState win, Rect pf, Rect df, Rect of, Rect cf, Rect vf, Rect dcf) {
        Rect rect = pf;
        Rect rect2 = df;
        Rect rect3 = of;
        Rect rect4 = cf;
        Rect rect5 = vf;
        Rect rect6 = dcf;
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (forceRotationManager.isForceRotationSupported() && forceRotationManager.isForceRotationSwitchOpen(this.mContext) && win != null && win.getAppToken() != null && win.getAttrs() != null) {
            String winTitle = String.valueOf(win.getAttrs().getTitle());
            if (TextUtils.isEmpty(winTitle) || winTitle.startsWith("SurfaceView") || winTitle.startsWith("PopupWindow")) {
                return;
            }
            if (win.isInMultiWindowMode()) {
                Slog.v(TAG, "window is in multiwindow mode");
                return;
            }
            Display defDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            defDisplay.getMetrics(dm);
            if (dm.widthPixels >= dm.heightPixels) {
                Rect tmpRect = new Rect(rect5);
                if (forceRotationManager.isAppForceLandRotatable(win.getAttrs().packageName, win.getAppToken().asBinder())) {
                    forceRotationManager.applyForceRotationLayout(win.getAppToken().asBinder(), tmpRect);
                    if (!tmpRect.equals(rect5)) {
                        int i = tmpRect.left;
                        rect5.left = i;
                        rect4.left = i;
                        rect2.left = i;
                        rect.left = i;
                        rect6.left = i;
                        rect3.left = i;
                        int i2 = tmpRect.right;
                        rect5.right = i2;
                        rect4.right = i2;
                        rect2.right = i2;
                        rect.right = i2;
                        rect6.right = i2;
                        rect3.right = i2;
                    }
                    win.getAttrs().privateFlags |= 64;
                }
            }
        }
    }

    public boolean isIntelliServiceEnabledFR(int orientatin) {
        return IntelliServiceManager.isIntelliServiceEnabled(this.mContext, orientatin, this.mCurrentUserId);
    }

    public void notifyRotationChange(int rotation) {
        this.mHwScreenOnProximityLock.refreshForRotationChange(rotation);
        if (CoordinationStackDividerManager.getInstance(this.mContext).isVisible()) {
            CoordinationStackDividerManager.getInstance(this.mContext).updateDividerView(isLandscape(rotation));
        }
    }

    private boolean isLandscape(int rotation) {
        return rotation == 1 || rotation == 3;
    }

    public int getRotationFromSensorOrFaceFR(int orientation, int lastRotation) {
        if (!IntelliServiceManager.isIntelliServiceEnabled(this.mContext, orientation, this.mCurrentUserId)) {
            return getRotationFromRealSensorFR(lastRotation);
        }
        if (IntelliServiceManager.getInstance(this.mContext).isKeepPortrait()) {
            Slog.d(IntelliServiceManager.TAG, "portraitRotaion:" + 0);
            return 0;
        } else if (IntelliServiceManager.getInstance(this.mContext).getFaceRotaion() != -2) {
            int sensorRotation = IntelliServiceManager.getInstance(this.mContext).getFaceRotaion();
            Slog.d(IntelliServiceManager.TAG, "faceRotaion:" + sensorRotation);
            return sensorRotation;
        } else {
            int sensorRotation2 = getRotationFromRealSensorFR(lastRotation);
            Slog.d(IntelliServiceManager.TAG, "sensorRotaion:" + sensorRotation2);
            return sensorRotation2;
        }
    }

    public int getRotationFromRealSensorFR(int lastRotation) {
        int sensorRotation = this.mOrientationListener.getProposedRotation();
        Slog.d(TAG, "sensorRotation = " + sensorRotation + " lastRotation:" + lastRotation);
        if (sensorRotation < 0) {
            return lastRotation;
        }
        return sensorRotation;
    }

    public void setSensorRotationFR(int rotation) {
        IntelliServiceManager.setSensorRotation(rotation);
    }

    /* access modifiers changed from: protected */
    public void notifyFingerSense(int rotation) {
        KnuckGestureSetting.getInstance().setOrientation(rotation);
        if (this.systemWideActionsListener != null) {
            this.systemWideActionsListener.setOrientation(rotation);
        } else if (mIFeature != null) {
            mIFeature.setOrientation(rotation);
            Flog.i(1503, "notifyFingerSense");
        }
    }

    public void startIntelliServiceFR() {
        IntelliServiceManager.getInstance(this.mContext).startIntelliService(this.mFaceRotationCallback);
    }

    public void resetCurrentNaviBarHeightExternal() {
        HwPCUtils.log(TAG, "resetCurrentNaviBarHeightExternal");
        if (HwPCUtils.enabled() && this.mNavigationBarHeightExternal != 0) {
            this.mNavigationBarHeightExternal = 0;
        }
    }

    private void initNavigationBarHightExternal(Display display, int width, int height) {
        if (display == null || this.mContext == null) {
            Log.e(TAG, "fail to ini nav, display or context is null");
            return;
        }
        if (HwPCUtils.enabled() && HwPCUtils.isValidExtDisplayId(display.getDisplayId())) {
            this.mNavigationBarHeightExternal = this.mContext.createDisplayContext(display).getResources().getDimensionPixelSize(34472195);
            HwPCUtils.log(TAG, "initNavigationBarHightExternal : mNavigationBarHeightExternal = " + this.mNavigationBarHeightExternal);
            if (this.mExternalSystemGestures != null) {
                this.mExternalSystemGestures.screenHeight = height;
                this.mExternalSystemGestures.screenWidth = width;
            }
        }
    }

    public void layoutWindowLwForNotch(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs) {
        if (IS_NOTCH_PROP) {
            boolean isNeverMode = false;
            this.isAppWindow = attrs.type >= 1 && attrs.type <= 99;
            this.mIsNoneNotchAppInHideMode = this.hwNotchScreenWhiteConfig.isNoneNotchAppHideInfo(win);
            boolean isSnapshotStartingWindow = win.toString().contains("SnapshotStartingWindow");
            notchControlFillet(win);
            if (this.mDisplayRotation == 0 && !this.mIsNotchSwitchOpen) {
                if ((this.mFocusedWindow != null ? this.hwNotchScreenWhiteConfig.getAppUseNotchMode(this.mFocusedWindow.getOwningPackage()) : 0) == 2) {
                    isNeverMode = true;
                }
                setStatusBarColorForNotchMode(isNeverMode, isSnapshotStartingWindow);
            }
        }
    }

    public boolean canLayoutInDisplayCutout(WindowManagerPolicy.WindowState win) {
        boolean canLayoutInDisplayCutout = true;
        if (!IS_NOTCH_PROP) {
            return true;
        }
        int mode = this.mFocusedWindow != null ? this.hwNotchScreenWhiteConfig.getAppUseNotchMode(this.mFocusedWindow.getOwningPackage()) : 0;
        boolean isNeverMode = mode == 2;
        boolean isAlwaysMode = mode == 1;
        if (isNeverMode) {
            canLayoutInDisplayCutout = false;
        } else if (isAlwaysMode) {
            canLayoutInDisplayCutout = true;
        } else if ((this.mIsNotchSwitchOpen || !this.hwNotchScreenWhiteConfig.isNotchAppInfo(win)) && ((!this.mIsNotchSwitchOpen || !this.hwNotchScreenWhiteConfig.isNotchAppHideInfo(win)) && (win.getAttrs().hwFlags & 65536) == 0 && !win.getHwNotchSupport() && win.getAttrs().layoutInDisplayCutoutMode != 1)) {
            canLayoutInDisplayCutout = false;
        }
        return canLayoutInDisplayCutout;
    }

    private void setStatusBarColorForNotchMode(boolean isNeverMode, boolean isSnapshotStartingWindow) {
        if (!isSnapshotStartingWindow && this.isAppWindow && isNeverMode && !this.mIsForceSetStatusBar) {
            notchStatusBarColorUpdate(1);
            this.mIsForceSetStatusBar = true;
            this.mIsRestoreStatusBar = false;
        } else if (!isSnapshotStartingWindow && this.isAppWindow && !this.mIsNotchSwitchOpen && !this.mIsRestoreStatusBar && !isNeverMode) {
            this.mIsForceSetStatusBar = false;
            this.mIsRestoreStatusBar = true;
            notchStatusBarColorUpdate(0);
        }
    }

    private void hideNotchRoundCorner() {
        this.mFirstSetCornerInLandNoNotch = false;
        this.mFirstSetCornerInLandNotch = true;
        this.mHwPWMEx.setIntersectCutoutForNotch(false);
        transferSwitchStatusToSurfaceFlinger(0);
    }

    private void showNotchRoundCorner() {
        this.mFirstSetCornerInLandNotch = false;
        this.mFirstSetCornerInLandNoNotch = true;
        this.mHwPWMEx.setIntersectCutoutForNotch(true);
        transferSwitchStatusToSurfaceFlinger(1);
    }

    private void notchControlFillet(WindowManagerPolicy.WindowState win) {
        boolean rightSplit = false;
        if (!this.mIsNotchSwitchOpen) {
            if (this.mDisplayRotation == 1 || this.mDisplayRotation == 3) {
                this.mFirstSetCornerInPort = true;
                boolean splashScreen = win.toString().contains("Splash Screen");
                boolean workSpace = win.toString().contains("com.huawei.intelligent.Workspace");
                boolean isNotchSupport = canLayoutInDisplayCutout(win);
                boolean flagGroup = this.isAppWindow && (this.mFocusedWindow != null && this.mFocusedWindow.toString().equals(win.toString())) && !splashScreen;
                if (win.isInMultiWindowMode()) {
                    boolean flagGroup2 = !splashScreen && this.isAppWindow;
                    boolean leftSplit = win.getWindowingMode() == 3 && this.mDisplayRotation == 1;
                    if (win.getWindowingMode() == 4 && this.mDisplayRotation == 3) {
                        rightSplit = true;
                    }
                    if (!leftSplit && !rightSplit) {
                        return;
                    }
                    if (flagGroup2 && isNotchSupport && this.mFirstSetCornerInLandNoNotch) {
                        hideNotchRoundCorner();
                    } else if (flagGroup2 && !isNotchSupport && this.mFirstSetCornerInLandNotch) {
                        showNotchRoundCorner();
                    }
                } else if (flagGroup && isNotchSupport && this.mFirstSetCornerInLandNoNotch) {
                    hideNotchRoundCorner();
                } else if (flagGroup && !isNotchSupport && this.mFirstSetCornerInLandNotch) {
                    if (this.mFirstSetCornerInLandNoNotch || !workSpace) {
                        showNotchRoundCorner();
                    }
                }
            } else {
                this.mFirstSetCornerInLandNoNotch = true;
                this.mFirstSetCornerInLandNotch = true;
                if (this.mFirstSetCornerInPort) {
                    this.mFirstSetCornerInPort = false;
                    transferSwitchStatusToSurfaceFlinger(0);
                }
            }
        } else if (this.mDisplayRotation == 2) {
            boolean splashScreen2 = win.toString().contains("Splash Screen");
            boolean isTopWindow = this.mFocusedWindow != null && this.mFocusedWindow.toString().equals(win.toString());
            if (!splashScreen2 && this.isAppWindow && this.mFirstSetCornerInReversePortait && isTopWindow) {
                this.mFirstSetCornerInReversePortait = false;
                this.mFirstSetCornerDefault = true;
                transferSwitchStatusToSurfaceFlinger(0);
            }
        } else if (this.mFirstSetCornerDefault) {
            this.mFirstSetCornerInReversePortait = true;
            this.mFirstSetCornerDefault = false;
            transferSwitchStatusToSurfaceFlinger(1);
        }
    }

    private void transferSwitchStatusToSurfaceFlinger(int value) {
        int val = value;
        Slog.d(TAG, "Window issued fillet display val = " + val + ", mIsNotchSwitchOpen = " + this.mIsNotchSwitchOpen + ", mDisplayRotation = " + this.mDisplayRotation);
        Parcel dataIn = Parcel.obtain();
        try {
            IBinder sfBinder = ServiceManager.getService("SurfaceFlinger");
            dataIn.writeInt(val);
            if (sfBinder != null && !sfBinder.transact(NOTCH_ROUND_CORNER_CODE, dataIn, null, 1)) {
                Slog.d(TAG, "transferSwitchStatusToSurfaceFlinger error!");
            }
        } catch (RemoteException e) {
            Slog.d(TAG, "transferSwitchStatusToSurfaceFlinger RemoteException on notify screen rotation animation end");
        } catch (Throwable th) {
            dataIn.recycle();
            throw th;
        }
        dataIn.recycle();
    }

    public void layoutWindowForPadPCMode(WindowManagerPolicy.WindowState win, Rect pf, Rect df, Rect cf, Rect vf, int mContentBottom) {
        HwPCMultiWindowPolicy.layoutWindowForPadPCMode(win, pf, df, cf, vf, mContentBottom);
    }

    public void setSwitchingUser(boolean switching) {
        if (switching) {
            Slog.d(TAG, "face_rotation: switchUser unbindIntelliService");
            IntelliServiceManager.getInstance(this.mContext).unbindIntelliService();
        }
        HwPhoneWindowManager.super.setSwitchingUser(switching);
    }

    public boolean hideNotchStatusBar(int fl) {
        boolean hideNotchStatusBar = true;
        this.mBarVisibility = 1;
        boolean isNotchSupport = this.mFocusedWindow != null ? canLayoutInDisplayCutout(this.mFocusedWindow) : false;
        if (!IS_NOTCH_PROP || this.mDisplayRotation != 0) {
            return true;
        }
        if (this.mFocusedWindow != null && this.mFocusedWindow.toString().contains("com.huawei.intelligent")) {
            return true;
        }
        if (this.mIsNotchSwitchOpen && isRightNotchState()) {
            boolean statusBarFocused = (this.mFocusedWindow == null || (PolicyControl.getWindowFlags(null, this.mFocusedWindow.getAttrs()) & 2048) == 0) ? false : true;
            if ((((fl & 1024) == 0 && (this.mLastSystemUiFlags & 4) == 0) ? false : true) || isWhiteFocusedWindow()) {
                hideNotchStatusBar = false;
                this.mForceNotchStatusBar = this.mForceNotchStatusBar || !statusBarFocused;
                this.mBarVisibility = 0;
            }
            if (!this.mForceNotchStatusBar || !statusBarFocused) {
                return hideNotchStatusBar;
            }
            this.mBarVisibility = 1;
            this.mForceNotchStatusBar = false;
            this.notchStatusBarColorLw = 0;
            notchStatusBarColorUpdate(1);
            return false;
        } else if (isNotchSupport) {
            return true;
        } else {
            if (this.mFocusedWindow != null && (this.hwNotchScreenWhiteConfig.isNotchAppInfo(this.mFocusedWindow) || this.mFocusedWindow.toString().contains(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE))) {
                return true;
            }
            if (this.mFocusedWindow != null && (this.hwNotchScreenWhiteConfig.isNoneNotchAppWithStatusbarInfo(this.mFocusedWindow) || (((this.mFocusedWindow.getAttrs().hwFlags & 32768) != 0 && ((fl & 1024) != 0 || (this.mLastSystemUiFlags & 4) != 0)) || (this.mForceNotchStatusBar && (this.mFocusedWindow.toString().contains("SearchPanel") || (!this.mTopFullscreenOpaqueWindowState.toString().contains("Splash Screen") && !this.mTopFullscreenOpaqueWindowState.toString().equals(this.mFocusedWindow.toString()))))))) {
                this.mForceNotchStatusBar = true;
                hideNotchStatusBar = false;
                this.mBarVisibility = 0;
            } else if (this.mForceNotchStatusBar && this.mFocusedWindow != null && this.mFocusedWindow.getAttrs().type == 2 && ((PolicyControl.getWindowFlags(null, this.mFocusedWindow.getAttrs()) & 1024) != 0 || ((this.mFocusedWindow.getAttrs().hwFlags & 32768) != 0 && (this.mLastSystemUiFlags & 1024) != 0))) {
                this.mForceNotchStatusBar = true;
                hideNotchStatusBar = false;
                this.mBarVisibility = 0;
            } else if ((this.mFocusedWindow == null && this.mForceNotchStatusBar && (!((fl & 1024) == 0 && (this.mLastSystemUiFlags & 4) == 0) && !this.mTopFullscreenOpaqueWindowState.toString().contains("Splash Screen"))) || !(this.mTopFullscreenOpaqueWindowState.getAttrs().type == 3 || (fl & 1024) == 0 || !this.mTopFullscreenOpaqueWindowState.toString().contains("Splash Screen"))) {
                this.mForceNotchStatusBar = true;
                hideNotchStatusBar = false;
                this.mBarVisibility = 0;
            }
            if (!(isKeyguardShowingOrOccluded() || (this.mKeyguardDelegate != null && this.mKeyguardDelegate.isOccluded()))) {
                return hideNotchStatusBar;
            }
            this.mForceNotchStatusBar = false;
            this.mBarVisibility = 1;
            return true;
        }
    }

    private boolean isRightNotchState() {
        boolean isTopWindowLauncher = this.mTopFullscreenOpaqueWindowState.toString().contains(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE);
        boolean isFocusedWindowNullAndTopWindowLauncher = this.mFocusedWindow == null && isTopWindowLauncher;
        boolean isFocusedWindowNotNullAndTopWindowHwRead = this.mFocusedWindow != null && this.mFocusedWindow.toString().contains("com.huawei.hwread.al/com.shuqi.y4.ReadActivity");
        if (!isTopWindowLauncher || isFocusedWindowNullAndTopWindowLauncher || isFocusedWindowNotNullAndTopWindowHwRead) {
            return true;
        }
        return false;
    }

    private boolean isWhiteFocusedWindow() {
        String focusedWindowString = this.mFocusedWindow != null ? this.mFocusedWindow.toString() : "";
        if (focusedWindowString.contains("HwGlobalActions") || focusedWindowString.contains("com.huawei.hwread") || focusedWindowString.contains("Sys2023:dream")) {
            return true;
        }
        return false;
    }

    public void notchStatusBarColorUpdate(int statusbarStateFlag) {
        if (!this.mIsForceSetStatusBar) {
            if (this.mFocusedWindow != null) {
                WindowManager.LayoutParams attrs = this.mFocusedWindow.getAttrs();
                this.mLastNavigationBarColor = attrs.navigationBarColor;
                this.mLastStatusBarColor = attrs.statusBarColor;
                this.mLastIsEmuiStyle = getEmuiStyleValue(attrs.isEmuiStyle);
            }
            notchTransactToStatusBarService(121, "notchTransactToStatusBarService", this.mLastIsEmuiStyle, this.mLastStatusBarColor, this.mLastNavigationBarColor, -1, statusbarStateFlag, this.mBarVisibility);
        }
    }

    /* access modifiers changed from: protected */
    public void wakeUpFromPowerKey(long eventTime) {
        doFaceRecognize(true, "FCDT-POWERKEY");
        HwPhoneWindowManager.super.wakeUpFromPowerKey(eventTime);
    }

    public void doFaceRecognize(boolean detect, String reason) {
        if (this.mKeyguardDelegate != null) {
            FaceReportEventToIaware.reportEventToIaware(this.mContext, 20025);
            this.mKeyguardDelegate.doFaceRecognize(detect, reason);
        }
    }

    public void notchTransactToStatusBarService(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle, int statusbarStateFlag, int barVisibility) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getHWStatusBarService() != null) {
                IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
                if (statusBarServiceBinder != null) {
                    Log.d(TAG, "set statusbarColor:" + statusbarColor + ", barVisibility: " + barVisibility + " to status bar service");
                    data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    data.writeInt(isEmuiStyle);
                    data.writeInt(statusbarColor);
                    data.writeInt(navigationBarColor);
                    data.writeInt(isEmuiLightStyle);
                    data.writeInt(statusbarStateFlag);
                    data.writeInt(barVisibility);
                    statusBarServiceBinder.transact(code, data, reply, 0);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "notchTransactToStatusBarService->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    private boolean handleInputEventInPCCastMode(KeyEvent event) {
        if (HwPCUtils.isPcCastModeInServer() && this.mLighterDrawView != null && this.mLighterDrawView.isVisibleLw()) {
            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
            if (pcManager != null) {
                try {
                    return pcManager.shouldInterceptInputEvent(event, false);
                } catch (RemoteException e) {
                    Log.e(TAG, "interceptInputEventInPCCastMode()");
                }
            }
        }
        return false;
    }

    public void setPowerState(int powerState) {
        HwAodManager.getInstance().setPowerState(powerState);
    }

    public void pause() {
        HwAodManager.getInstance().pause();
    }

    public int getDeviceNodeFD() {
        return HwAodManager.getInstance().getDeviceNodeFD();
    }

    public int getHardwareType() {
        return FingerprintManagerEx.getHardwareType();
    }

    public boolean isWindowSupportKnuckle() {
        return this.mFocusedWindow == null || (this.mFocusedWindow.getAttrs().flags & 4096) == 0;
    }

    /* access modifiers changed from: protected */
    public void uploadKeyEvent(int keyEvent) {
        if (this.mContext != null) {
            switch (keyEvent) {
                case 3:
                    StatisticalUtils.reportc(this.mContext, 173);
                    break;
                case 4:
                    StatisticalUtils.reportc(this.mContext, 172);
                    break;
            }
        }
    }

    public boolean isHwStartWindowEnabled(int type) {
        if (type == 0) {
            return StartWindowFeature.isStartWindowEnable();
        }
        if (type == 1) {
            return StartWindowFeature.isRotateOptEnabled();
        }
        return false;
    }

    public Context addHwStartWindow(ApplicationInfo appInfo, Context overrideContext, Context context, TypedArray typedArray, int windowFlags) {
        if (overrideContext == null || context == null || typedArray == null || appInfo == null) {
            return null;
        }
        boolean addHwStartWindowFlag = false;
        boolean windowIsTranslucent = typedArray.getBoolean(5, false);
        boolean windowDisableStarting = typedArray.getBoolean(12, false);
        boolean windowShowWallpaper = typedArray.getBoolean(14, false);
        if ((windowDisableStarting || windowIsTranslucent || (windowShowWallpaper && (windowFlags & HighBitsCompModeID.MODE_COLOR_ENHANCE) != 1048576)) && HwStartWindowRecord.getInstance().checkStartWindowApp(Integer.valueOf(appInfo.uid))) {
            addHwStartWindowFlag = true;
        }
        if (!addHwStartWindowFlag) {
            return null;
        }
        overrideContext.setTheme(overrideContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.NoActionBar", null, null));
        Slog.d(TAG, "addHwStartWindow set default on app : " + appInfo.packageName);
        return overrideContext;
    }

    public Object getNavigationBarPolicy() {
        return this.mNavigationBarPolicy;
    }

    public int[] getNavigationBarValueForRotation(int index) {
        switch (index) {
            case 0:
                return (int[]) this.mNavigationBarHeightForRotationMin.clone();
            case 1:
                return (int[]) this.mNavigationBarHeightForRotationMax.clone();
            case 2:
                return (int[]) this.mNavigationBarWidthForRotationMax.clone();
            case 3:
                return (int[]) this.mNavigationBarWidthForRotationMin.clone();
            default:
                return new int[4];
        }
    }

    public void setNavigationBarValueForRotation(int index, int type, int value) {
        switch (index) {
            case 0:
                this.mNavigationBarHeightForRotationMin[type] = value;
                break;
            case 1:
                this.mNavigationBarHeightForRotationMax[type] = value;
                break;
            case 2:
                this.mNavigationBarWidthForRotationMax[type] = value;
                break;
            case 3:
                this.mNavigationBarWidthForRotationMin[type] = value;
                break;
            default:
                return;
        }
    }

    public void setGestureNavMode(String packageName, int uid, int leftMode, int rightMode, int bottomMode) {
        if (this.mGestureNavManager != null) {
            this.mGestureNavManager.setGestureNavMode(packageName, uid, leftMode, rightMode, bottomMode);
        }
    }

    public int getCurrentRotation() {
        return this.mCurrentRotation;
    }

    /* access modifiers changed from: protected */
    public void cancelPendingPowerKeyAction() {
        HwPhoneWindowManager.super.cancelPendingPowerKeyAction();
        cancelAIPowerLongPressed();
        cancelPowerOffToast();
    }

    private boolean isFocusedWindow(WindowManagerPolicy.WindowState win) {
        WindowManagerPolicy.WindowState focusedWindow = this.mFocusedWindow;
        if (win == null || focusedWindow == null || win.getAttrs() == null || focusedWindow.getAttrs() == null) {
            return false;
        }
        return focusedWindow.getAttrs().getTitle().toString().equals(win.getAttrs().getTitle().toString());
    }

    public void onLayoutInDisplayCutoutModeChanged(WindowManagerPolicy.WindowState win, boolean oldUsingNotch, boolean newUsingNotch) {
        if (this.mGestureNavManager != null) {
            this.mGestureNavManager.onLayoutInDisplayCutoutModeChanged(win, oldUsingNotch, newUsingNotch);
        }
    }

    private void setProximitySensorEnabled(boolean enable) {
        if (OPEN_PROXIMITY_DISPALY) {
            if (this.mSensorManager == null) {
                this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            }
            if (enable) {
                if (!this.mProximitySensorEnabled) {
                    this.mProximitySensorEnabled = true;
                    this.mSensorManager.registerListener(this.mProximitySensorListener, this.mSensorManager.getDefaultSensor(8), 3);
                }
            } else if (this.mProximitySensorEnabled) {
                this.mProximitySensorEnabled = false;
                this.mSensorManager.unregisterListener(this.mProximitySensorListener);
            }
        }
    }

    private boolean isAcquireProximityLock() {
        boolean isAcquire = false;
        if (this.mHwScreenOnProximityLock == null) {
            return false;
        }
        boolean isModeEnabled = true;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1, ActivityManager.getCurrentUser()) <= 0 || "factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            isModeEnabled = false;
        }
        if (isModeEnabled && this.mDeviceProvisioned) {
            boolean isPhoneCallState = checkPhoneOFFHOOK();
            if (!isPhoneCallState || (isPhoneCallState && checkHeadSetIsConnected())) {
                isAcquire = true;
            }
        }
        return isAcquire;
    }

    public void setDisplayMode(int mode) {
        if (this.mHwScreenOnProximityLock != null) {
            if (3 == mode) {
                this.mHwScreenOnProximityLock.releaseLock(0);
            } else if (isAcquireProximityLock() && isKeyguardLocked()) {
                this.mHwScreenOnProximityLock.acquireLock(this, mode);
            }
        }
    }
}
