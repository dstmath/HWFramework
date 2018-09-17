package com.android.server.policy;

import android.aft.HwAftPolicyManager;
import android.aft.IHwAftPolicyService;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.input.InputManager;
import android.hdm.HwDeviceManager;
import android.hwcontrol.HwWidgetFactory;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.Trace;
import android.os.UserHandle;
import android.os.Vibrator;
import android.pc.IHwPCManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telecom.TelecomManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.HwSlog;
import android.util.HwStylusUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicy.KeyguardDismissDoneListener;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.view.WindowManagerPolicy.ScreenOnListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.WindowManagerPolicy.WindowState;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;
import android.widget.Toast;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.telephony.ITelephony;
import com.android.internal.view.IInputMethodManager;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.HwPCMultiWindowPolicy;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.emcom.SmartcareConstants;
import com.android.server.emcom.daemon.CommandsInterface;
import com.android.server.input.HwInputManagerService.HwInputManagerServiceInternal;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.notch.HwNotchScreenWhiteConfig;
import com.android.server.notch.HwNotchScreenWhiteConfig.NotchSwitchListener;
import com.android.server.policy.PhoneWindowManager.UpdateRotationRunnable;
import com.android.server.policy.SystemGesturesPointerEventListener.Callbacks;
import com.android.server.policy.keyguard.KeyguardStateMonitor.HwPCKeyguardShowingCallback;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.tsmagent.server.HttpConnectionBase;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wifipro.WifiProCHRManager;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.IntelliServiceManager;
import com.android.server.wm.IntelliServiceManager.FaceRotationCallback;
import com.huawei.android.app.IGameObserver.Stub;
import com.huawei.android.inputmethod.HwInputMethodManager;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.forcerotation.HwForceRotationManager;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import huawei.android.app.IHwWindowCallback;
import huawei.android.os.HwGeneralManager;
import huawei.android.provider.FingerSenseSettings;
import huawei.android.provider.FrontFingerPrintSettings;
import huawei.com.android.internal.widget.HwWidgetUtils;
import huawei.com.android.server.policy.HwFalseTouchMonitor;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.com.android.server.policy.HwScreenOnProximityLock;
import huawei.com.android.server.policy.fingersense.SystemWideActionsListener;
import huawei.com.android.server.policy.stylus.StylusGestureListener;
import huawei.cust.HwCustUtils;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class HwPhoneWindowManager extends PhoneWindowManager implements TouchExplorationStateChangeListener {
    private static final String ACTION_HUAWEI_VASSISTANT_SERVICE = "com.huawei.ziri.model.MODELSERVICE";
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
    static final String FINGERPRINT_STOP_ALARM = "fp_stop_alarm";
    private static final int FLOATING_MASK = Integer.MIN_VALUE;
    private static final int FORCE_STATUS_BAR = 1;
    public static final String FRONT_FINGERPRINT_BUTTON_LIGHT_MODE = "button_light_mode";
    public static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    public static final String HAPTIC_FEEDBACK_TRIKEY_SETTINGS = "physic_navi_haptic_feedback_enabled";
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
    private static final List KEYCODE_NOT_FOR_CLOUD = Arrays.asList(new Integer[]{Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(25), Integer.valueOf(24), Integer.valueOf(220), Integer.valueOf(221)});
    private static final String KEY_HWOUC_KEYGUARD_VIEW_ON_TOP = "hwouc_keyguard_view_on_top";
    private static final String KEY_TOUCH_DISABLE_MODE = "touch_disable_mode";
    private static final int MSG_BUTTON_LIGHT_TIMEOUT = 4099;
    private static final int MSG_FINGERSENSE_DISABLE = 102;
    private static final int MSG_FINGERSENSE_ENABLE = 101;
    private static final int MSG_NAVIBAR_DISABLE = 104;
    private static final int MSG_NAVIBAR_ENABLE = 103;
    private static final int MSG_TRIKEY_BACK_LONG_PRESS = 4097;
    private static final int MSG_TRIKEY_RECENT_LONG_PRESS = 4098;
    private static final int NOTCH_ROUND_CORNER_CODE = 8002;
    private static final int NOTCH_ROUND_CORNER_HIDE = 1;
    private static final int NOTCH_ROUND_CORNER_SHOW = 0;
    private static final String PKG_CALCULATOR = "com.android.calculator2";
    private static final String PKG_CAMERA = "com.huawei.camera";
    private static final String PKG_DESKCLOCK = "com.android.deskclock";
    private static final String PKG_GALLERY = "com.android.gallery3d";
    private static final String PKG_SCANNER = "com.huawei.scanner";
    private static final String PKG_SOUNDRECORDER = "com.android.soundrecorder";
    private static final long SCREENRECORDER_DEBOUNCE_DELAY_MILLIS = 150;
    private static final int SIM_CARD_1 = 0;
    private static final int SIM_CARD_2 = 1;
    private static final int SINGLE_HAND_STATE = 1989;
    private static final String SMARTKEY_CLICK = "Click";
    private static final String SMARTKEY_DCLICK = "DoubleClick";
    private static final long SMARTKEY_DOUBLE_CLICK_TIMEOUT = 400;
    private static final long SMARTKEY_LONG_PRESS_TIMEOUT = 500;
    private static final String SMARTKEY_LP = "LongPress";
    private static final String SMARTKEY_TAG = "command";
    private static final int START_MODE_QUICK_START_CALL = 2;
    static final int START_MODE_VOICE_WAKEUP_ONE_SHOT = 4;
    private static final long SYSTRACELOG_DEBOUNCE_DELAY_MILLIS = 150;
    private static final long SYSTRACELOG_FINGERPRINT_EFFECT_DELAY = 750;
    static final String TAG = "HwPhoneWindowManager";
    private static final boolean TOUCHPLUS_FORCE_VIBRATION = true;
    private static final int TOUCHPLUS_SETTINGS_DISABLED = 0;
    private static final int TOUCHPLUS_SETTINGS_ENABLED = 1;
    private static final String TOUCHPLUS_SETTINGS_VIBRATION = "hw_membrane_touch_vibrate_enabled";
    private static final long TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS = 150;
    private static final int TOUCH_EXPLR_NAVIGATION_BAR_COLOR = -16777216;
    private static final int TOUCH_EXPLR_STATUS_BAR_COLOR = -16777216;
    private static final long TOUCH_SPINNING_DELAY_MILLIS = 2000;
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final String UPDATE_NOTCH_SCREEN_ACTION = "huawei.intent.action.FWK_UPDATE_NOTCH_SCREEN_ACTION";
    private static final String UPDATE_NOTCH_SCREEN_PER = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String VIBRATE_ON_TOUCH = "vibrate_on_touch";
    private static final int VIBRATOR_LONG_PRESS_FOR_FRONT_FP = SystemProperties.getInt("ro.config.trikey_vibrate_press", 16);
    private static final int VIBRATOR_SHORT_PRESS_FOR_FRONT_FP = SystemProperties.getInt("ro.config.trikey_vibrate_touch", 8);
    private static String VOICE_ASSISTANT_ACTION = "com.huawei.action.VOICE_ASSISTANT";
    private static final long VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT = 400;
    private static final long VOLUMEDOWN_LONG_PRESS_TIMEOUT = 500;
    private static boolean mCustBeInit = false;
    private static boolean mCustUsed = false;
    static final boolean mIsHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private static final boolean mSupportGameAssist;
    private static boolean mTplusEnabled = SystemProperties.getBoolean("ro.config.hw_touchplus_enabled", false);
    private static int[] mUnableWakeKey;
    private static boolean mUsingHwNavibar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private boolean DEBUG_SMARTKEY = false;
    private int TRIKEY_NAVI_DEFAULT_MODE = -1;
    private AlertDialog alertDialog;
    FingerprintActionsListener fingerprintActionsListener;
    private HwNotchScreenWhiteConfig hwNotchScreenWhiteConfig;
    private boolean isAppWindow = false;
    private boolean isChanged = false;
    private boolean isFingerAnswerPhoneOn = false;
    private boolean isFingerShotCameraOn = false;
    private boolean isFingerStopAlarmOn = false;
    boolean isHomeAndDBothDown = false;
    boolean isHomeAndEBothDown = false;
    boolean isHomeAndLBothDown = false;
    boolean isHomePressDown = false;
    private boolean isHwOUCKeyguardViewOnTop = false;
    private boolean isNavibarHide;
    private boolean isNotchSwitchOpen;
    private boolean isNotchTemp;
    private boolean isOnlyOnce = false;
    private boolean isSwitchNotch = false;
    private boolean isSystemApp = false;
    private boolean isTouchDownUpLeftDoubleClick;
    private boolean isTouchDownUpRightDoubleClick;
    private boolean isVibrateImplemented = SystemProperties.getBoolean("ro.config.touch_vibrate", false);
    private boolean isVoiceRecognitionActive;
    private int lastDensityDpi = -1;
    private int mActionBarHeight;
    private HwActivityManagerService mAms;
    private boolean mBackKeyPress = false;
    private long mBackKeyPressTime = 0;
    private Light mBackLight = null;
    volatile boolean mBackTrikeyHandled;
    private int mBarVisibility = 1;
    boolean mBooted = false;
    private WakeLock mBroadcastWakeLock;
    private Light mButtonLight = null;
    private int mButtonLightMode = 1;
    private final Runnable mCancleInterceptFingerprintEvent = new Runnable() {
        public void run() {
            HwPhoneWindowManager.this.mNeedDropFingerprintEvent = false;
        }
    };
    private CoverManager mCoverManager = null;
    private boolean mCoverOpen = true;
    private int mCurUser;
    HwCustPhoneWindowManager mCust = ((HwCustPhoneWindowManager) HwCustUtils.createObj(HwCustPhoneWindowManager.class, new Object[0]));
    int mDesiredRotation = -1;
    private boolean mDeviceProvisioned = false;
    private boolean mEnableKeyInCurrentFgGameApp;
    private SystemGesturesPointerEventListener mExternalSystemGestures;
    FaceRotationCallback mFaceRotationCallback = new FaceRotationCallback() {
        public void onEvent(int faceRotation) {
            if (faceRotation == -2) {
                HwPhoneWindowManager.this.mHandler.post(new UpdateRotationRunnable(HwPhoneWindowManager.this, HwPhoneWindowManager.this.mScreenRotation));
            } else {
                HwPhoneWindowManager.this.mHandler.post(new UpdateRotationRunnable(HwPhoneWindowManager.this, faceRotation));
            }
        }
    };
    private HwFalseTouchMonitor mFalseTouchMonitor = null;
    private int mFingerPrintId = -1;
    boolean mFingerSenseEnabled = true;
    private ContentObserver mFingerprintObserver;
    private boolean mFirstSetCornerInLandNoNotch = true;
    private boolean mFirstSetCornerInLandNotch = true;
    private boolean mFirstSetCornerInPort = true;
    private final Runnable mHandleVolumeDownKey = new Runnable() {
        public void run() {
            if (HwPhoneWindowManager.this.isMusicActive()) {
                HwPhoneWindowManager.this.handleVolumeKey(3, 25);
            }
        }
    };
    private Handler mHandlerEx;
    private boolean mHapticEnabled = true;
    private boolean mHeadless;
    private boolean mHintShown;
    private ContentObserver mHwOUCObserver;
    private HwScreenOnProximityLock mHwScreenOnProximityLock;
    public IHwWindowCallback mIHwWindowCallback;
    private boolean mInputMethodWindowVisible;
    private boolean mIsHasActionBar;
    protected boolean mIsImmersiveMode = false;
    boolean mIsNavibarAlignLeftWhenLand;
    private boolean mIsProximity = false;
    private boolean mIsSmartKeyDoubleClick = false;
    private boolean mIsSmartKeyTripleOrMoreClick = false;
    private boolean mIsTouchExplrEnabled;
    private String[] mKeyguardShortcutApps = new String[]{"com.huawei.camera", PKG_GALLERY, PKG_SCANNER};
    private WindowState mLastColorWin;
    private String mLastFgPackageName;
    private int mLastIsEmuiLightStyle;
    private int mLastIsEmuiStyle;
    private boolean mLastKeyDownDropped;
    private int mLastKeyDownKeyCode;
    private long mLastKeyDownTime;
    private long mLastKeyPointerTime = 0;
    private int mLastNavigationBarColor;
    private long mLastSmartKeyDownTime;
    private long mLastStartVassistantServiceTime;
    private int mLastStatusBarColor;
    private long mLastTouchDownUpLeftKeyDownTime;
    private long mLastTouchDownUpRightKeyDownTime;
    private long mLastVolumeDownKeyDownTime;
    private ProximitySensorListener mListener = null;
    private PointerEventListener mLockScreenBuildInDisplayListener;
    private PointerEventListener mLockScreenListener;
    private String[] mLsKeyguardShortcutApps = new String[]{PKG_SOUNDRECORDER, PKG_CALCULATOR, PKG_DESKCLOCK};
    private boolean mMenuClickedOnlyOnce = false;
    private boolean mMenuKeyPress = false;
    private long mMenuKeyPressTime = 0;
    boolean mNavibarEnabled = false;
    final BarController mNavigationBarControllerExternal = new BarController("NavigationBarExternal", 134217728, 536870912, FLOATING_MASK, 2, 134217728, 32768);
    protected WindowState mNavigationBarExternal = null;
    int mNavigationBarHeightExternal = 0;
    int[] mNavigationBarHeightForRotationMax = new int[4];
    int[] mNavigationBarHeightForRotationMin = new int[4];
    protected NavigationBarPolicy mNavigationBarPolicy = null;
    int[] mNavigationBarWidthForRotationMax = new int[4];
    int[] mNavigationBarWidthForRotationMin = new int[4];
    private boolean mNeedDropFingerprintEvent = false;
    private NotchSwitchListener mNotchSwitchListener;
    OverscanTimeout mOverscanTimeout = new OverscanTimeout();
    private boolean mPowerKeyDisTouch;
    private long mPowerKeyDisTouchTime;
    private PowerManager mPowerManager;
    final Runnable mProximitySensorTimeoutRunnable = new Runnable() {
        public void run() {
            Log.i(HwPhoneWindowManager.TAG, "mProximitySensorTimeout, unRegisterListener");
            HwPhoneWindowManager.this.turnOffSensorListener();
        }
    };
    volatile boolean mRecentTrikeyHandled;
    private ContentResolver mResolver;
    private boolean mScreenOnForFalseTouch;
    private long mScreenRecorderPowerKeyTime;
    private boolean mScreenRecorderPowerKeyTriggered;
    private final Runnable mScreenRecorderRunnable = new Runnable() {
        public void run() {
            Intent intent = new Intent();
            intent.setAction(HwPhoneWindowManager.HUAWEI_SCREENRECORDER_ACTION);
            intent.setClassName(HwPhoneWindowManager.HUAWEI_SCREENRECORDER_PACKAGE, HwPhoneWindowManager.HUAWEI_SCREENRECORDER_START_MODE);
            HwPhoneWindowManager.this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
            Log.d(HwPhoneWindowManager.TAG, "start screen recorder service");
        }
    };
    private boolean mScreenRecorderVolumeDownKeyTriggered;
    private boolean mScreenRecorderVolumeUpKeyConsumed;
    private long mScreenRecorderVolumeUpKeyTime;
    private boolean mScreenRecorderVolumeUpKeyTriggered;
    private int mSecondToLastKeyDownKeyCode;
    private long mSecondToLastKeyDownTime;
    private SensorManager mSensorManager = null;
    private boolean mSensorRegisted = false;
    final Object mServiceAquireLock = new Object();
    private SettingsObserver mSettingsObserver;
    private final Runnable mSmartKeyClick = new Runnable() {
        public void run() {
            HwPhoneWindowManager.this.cancelSmartKeyClick();
            HwPhoneWindowManager.this.notifySmartKeyEvent(HwPhoneWindowManager.SMARTKEY_CLICK);
        }
    };
    private final Runnable mSmartKeyLongPressed = new Runnable() {
        public void run() {
            HwPhoneWindowManager.this.cancelSmartKeyLongPressed();
            HwPhoneWindowManager.this.notifySmartKeyEvent(HwPhoneWindowManager.SMARTKEY_LP);
        }
    };
    boolean mStatuBarObsecured;
    IStatusBarService mStatusBarService;
    private StylusGestureListener mStylusGestureListener = null;
    private StylusGestureListener mStylusGestureListener4PCMode = null;
    private boolean mSystraceLogCompleted = true;
    private long mSystraceLogFingerPrintTime = 0;
    private boolean mSystraceLogPowerKeyTriggered = false;
    private final Runnable mSystraceLogRunnable = new Runnable() {
        public void run() {
            HwPhoneWindowManager.this.systraceLogDialogThread = new HandlerThread("SystraceLogDialog");
            HwPhoneWindowManager.this.systraceLogDialogThread.start();
            HwPhoneWindowManager.this.systraceLogDialogHandler = new Handler(HwPhoneWindowManager.this.systraceLogDialogThread.getLooper()) {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 0) {
                        HwPhoneWindowManager.this.alertDialog = new Builder(HwPhoneWindowManager.this.mContext).setTitle(17039380).setMessage(HwPhoneWindowManager.this.mContext.getResources().getQuantityString(34406405, 10, new Object[]{Integer.valueOf(10)})).setCancelable(false).create();
                        HwPhoneWindowManager.this.alertDialog.getWindow().setType(DeviceStatusConstant.MSDP_DEVICE_STATUS_MOVEMENT);
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
                        if (!(HwPhoneWindowManager.this.systraceLogDialogHandler == null || (HwPhoneWindowManager.this.mSystraceLogCompleted ^ 1) == 0)) {
                            HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(i);
                        }
                        i++;
                    }
                }
            }).start();
            new Thread(new Runnable() {
                public void run() {
                    IBinder sJankService = ServiceManager.getService("jank");
                    if (sJankService != null) {
                        try {
                            Log.d(HwPhoneWindowManager.TAG, "sJankService is not null");
                            Parcel data = Parcel.obtain();
                            Parcel reply = Parcel.obtain();
                            data.writeInterfaceToken("android.os.IJankManager");
                            sJankService.transact(2, data, reply, 0);
                            Log.d(HwPhoneWindowManager.TAG, "sJankService.transact result = " + reply.readInt());
                        } catch (RemoteException e) {
                            Log.e(HwPhoneWindowManager.TAG, "sJankService.transact remote exception:" + e.getMessage());
                        } finally {
                            HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(10);
                        }
                    }
                    HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(10);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        Log.w(HwPhoneWindowManager.TAG, "sJankService transact not completed,interrupted");
                    } finally {
                        HwPhoneWindowManager.this.mSystraceLogCompleted = true;
                        HwPhoneWindowManager.this.systraceLogDialogThread.quitSafely();
                        Log.d(HwPhoneWindowManager.TAG, "has quit the systraceLogDialogThread" + HwPhoneWindowManager.this.systraceLogDialogThread.getId());
                    }
                }
            }).start();
        }
    };
    private boolean mSystraceLogVolumeDownKeyTriggered = false;
    private boolean mSystraceLogVolumeUpKeyConsumed = false;
    private long mSystraceLogVolumeUpKeyTime = 0;
    private boolean mSystraceLogVolumeUpKeyTriggered = false;
    private TouchCountPolicy mTouchCountPolicy = new TouchCountPolicy();
    private int mTouchDownUpLeftConsumeCount;
    private int mTouchDownUpRightConsumeCount;
    private int mTrikeyNaviMode = -1;
    private SystemVibrator mVibrator;
    private boolean mVolumeDownKeyDisTouch;
    private final Runnable mVolumeDownLongPressed = new Runnable() {
        public void run() {
            HwPhoneWindowManager.this.cancelVolumeDownKeyPressed();
            if ((!HwPhoneWindowManager.this.mIsProximity && HwPhoneWindowManager.this.mSensorRegisted) || (HwPhoneWindowManager.this.mSensorRegisted ^ 1) != 0) {
                HwPhoneWindowManager.this.notifyVassistantService("start", 2, null);
            }
            HwPhoneWindowManager.this.turnOffSensorListener();
            HwPhoneWindowManager.this.isVoiceRecognitionActive = true;
            HwPhoneWindowManager.this.mLastStartVassistantServiceTime = SystemClock.uptimeMillis();
        }
    };
    private WakeLock mVolumeDownWakeLock;
    private boolean mVolumeUpKeyConsumedByDisTouch;
    private boolean mVolumeUpKeyDisTouch;
    private long mVolumeUpKeyDisTouchTime;
    private BroadcastReceiver mWhitelistReceived = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || context == null || intent.getAction() == null) {
                Slog.i(HwPhoneWindowManager.TAG, "intent is " + intent + "context is " + context);
                return;
            }
            if (HwPhoneWindowManager.UPDATE_NOTCH_SCREEN_ACTION.equals(intent.getAction())) {
                String fileName = intent.getStringExtra("uri");
                Slog.i(HwPhoneWindowManager.TAG, "fileName:" + fileName);
                if (fileName != null) {
                    HwNotchScreenWhiteConfig.getInstance().updateWhitelistByHot(context, fileName);
                }
            }
        }
    };
    private HashSet<String> needDropSmartKeyActivities = new HashSet();
    SystemWideActionsListener systemWideActionsListener;
    private Handler systraceLogDialogHandler;
    private HandlerThread systraceLogDialogThread;

    class OverscanTimeout implements Runnable {
        OverscanTimeout() {
        }

        public void run() {
            Slog.i(HwPhoneWindowManager.TAG, "OverscanTimeout run");
            Global.putString(HwPhoneWindowManager.this.mContext.getContentResolver(), "single_hand_mode", "");
        }
    }

    private class PolicyHandlerEx extends Handler {
        /* synthetic */ PolicyHandlerEx(HwPhoneWindowManager this$0, PolicyHandlerEx -this1) {
            this();
        }

        private PolicyHandlerEx() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    HwPhoneWindowManager.this.enableSystemWideActions();
                    return;
                case 102:
                    HwPhoneWindowManager.this.disableSystemWideActions();
                    return;
                case 103:
                    HwPhoneWindowManager.this.disableFingerPrintActions();
                    return;
                case 104:
                    HwPhoneWindowManager.this.enableFingerPrintActions();
                    return;
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
                default:
                    return;
            }
        }
    }

    private class ProximitySensorListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent arg0) {
            boolean z = false;
            float[] its = arg0.values;
            if (its != null && arg0.sensor.getType() == 8 && its.length > 0) {
                Log.i(HwPhoneWindowManager.TAG, "sensor value: its[0] = " + its[0]);
                HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
                if (its[0] >= 0.0f && its[0] < HwPhoneWindowManager.TYPICAL_PROXIMITY_THRESHOLD) {
                    z = true;
                }
                hwPhoneWindowManager.mIsProximity = z;
            }
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ ScreenBroadcastReceiver(HwPhoneWindowManager this$0, ScreenBroadcastReceiver -this1) {
            this();
        }

        private ScreenBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                HwPhoneWindowManager.this.sendLightTimeoutMsg();
            }
        }
    }

    class SettingsObserver extends ContentObserver {
        final /* synthetic */ HwPhoneWindowManager this$0;

        SettingsObserver(HwPhoneWindowManager this$0, Handler handler) {
            boolean z = true;
            this.this$0 = this$0;
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            this$0.mDeviceProvisioned = Secure.getIntForUser(this$0.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            this$0.mTrikeyNaviMode = System.getIntForUser(this$0.mResolver, "swap_key_position", this$0.TRIKEY_NAVI_DEFAULT_MODE, ActivityManager.getCurrentUser());
            this$0.mButtonLightMode = System.getIntForUser(this$0.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
            if (System.getIntForUser(this$0.mResolver, "physic_navi_haptic_feedback_enabled", 1, ActivityManager.getCurrentUser()) == 0) {
                z = false;
            }
            this$0.mHapticEnabled = z;
        }

        public void registerContentObserver(int userId) {
            this.this$0.mResolver.registerContentObserver(System.getUriFor("swap_key_position"), false, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("device_provisioned"), false, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("button_light_mode"), false, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("physic_navi_haptic_feedback_enabled"), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z = true;
            this.this$0.mDeviceProvisioned = Secure.getIntForUser(this.this$0.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            this.this$0.mTrikeyNaviMode = System.getIntForUser(this.this$0.mResolver, "swap_key_position", this.this$0.TRIKEY_NAVI_DEFAULT_MODE, ActivityManager.getCurrentUser());
            this.this$0.mButtonLightMode = System.getIntForUser(this.this$0.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
            this.this$0.resetButtonLightStatus();
            Slog.i(HwPhoneWindowManager.TAG, "mTrikeyNaviMode is:" + this.this$0.mTrikeyNaviMode + " mButtonLightMode is:" + this.this$0.mButtonLightMode);
            HwPhoneWindowManager hwPhoneWindowManager = this.this$0;
            if (System.getIntForUser(this.this$0.mResolver, "physic_navi_haptic_feedback_enabled", 1, ActivityManager.getCurrentUser()) == 0) {
                z = false;
            }
            hwPhoneWindowManager.mHapticEnabled = z;
        }
    }

    static {
        boolean z;
        if (SystemProperties.getInt("ro.config.gameassist", 0) == 1) {
            z = true;
        } else {
            z = false;
        }
        mSupportGameAssist = z;
    }

    public void systemReady() {
        super.systemReady();
        this.mHandler.post(new Runnable() {
            public void run() {
                HwPhoneWindowManager.this.initQuickcall();
            }
        });
        if (IS_NOTCH_PROP) {
            this.hwNotchScreenWhiteConfig = HwNotchScreenWhiteConfig.getInstance();
        }
        this.mHwScreenOnProximityLock = new HwScreenOnProximityLock(this.mContext, this, this.mWindowManagerFuncs);
        if (mIsHwNaviBar) {
            this.mNavigationBarPolicy = new NavigationBarPolicy(this.mContext, this);
            this.mWindowManagerFuncs.registerPointerEventListener(new PointerEventListener() {
                public void onPointerEvent(MotionEvent motionEvent) {
                    if (HwPhoneWindowManager.this.mNavigationBarPolicy != null) {
                        HwPhoneWindowManager.this.mNavigationBarPolicy.addPointerEvent(motionEvent);
                    }
                }
            });
        }
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
            EasyWakeUpManager mWakeUpManager = EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate);
            ServiceManager.addService("easywakeup", mWakeUpManager);
            mWakeUpManager.saveTouchPointNodePath();
        }
        if (this.mListener == null) {
            this.mListener = new ProximitySensorListener();
        }
        this.mResolver = this.mContext.getContentResolver();
        this.TRIKEY_NAVI_DEFAULT_MODE = FrontFingerPrintSettings.getDefaultNaviMode();
        this.mSettingsObserver = new SettingsObserver(this, this.mHandler);
        this.mVibrator = (SystemVibrator) ((Vibrator) this.mContext.getSystemService("vibrator"));
        if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
            LightsManager lights = (LightsManager) LocalServices.getService(LightsManager.class);
            this.mButtonLight = lights.getLight(2);
            this.mBackLight = lights.getLight(0);
            if (this.mContext != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.SCREEN_ON");
                this.mContext.registerReceiver(new ScreenBroadcastReceiver(this, null), filter);
            }
        }
        this.mFalseTouchMonitor = new HwFalseTouchMonitor();
        if (mSupportGameAssist) {
            this.mAms = HwActivityManagerService.self();
            this.mFingerPrintId = SystemProperties.getInt("sys.fingerprint.deviceId", -1);
            Stub gameObserver = new Stub() {
                public void onGameStatusChanged(String packageName, int event) {
                    Log.i(HwPhoneWindowManager.TAG, "currentFgApp=" + packageName + ", mLastFgPackageName=" + HwPhoneWindowManager.this.mLastFgPackageName);
                    if (!(packageName == null || (packageName.equals(HwPhoneWindowManager.this.mLastFgPackageName) ^ 1) == 0)) {
                        HwPhoneWindowManager.this.mEnableKeyInCurrentFgGameApp = false;
                    }
                    HwPhoneWindowManager.this.mLastFgPackageName = packageName;
                }

                public void onGameListChanged() {
                }
            };
            if (this.mAms != null) {
                this.mAms.registerGameObserver(gameObserver);
            }
        }
        if (HwPCUtils.enabled()) {
            this.mExternalSystemGestures = new SystemGesturesPointerEventListener(this.mContext, new Callbacks() {
                public void onSwipeFromTop() {
                    if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
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
    }

    public void registerExternalPointerEventListener() {
        if (HwPCUtils.enabled() && HwPCUtils.isPcCastModeInServer()) {
            unRegisterExternalPointerEventListener();
            this.mLockScreenListener = new PointerEventListener() {
                public void onPointerEvent(MotionEvent motionEvent) {
                    if (motionEvent.getEventTime() - HwPhoneWindowManager.this.mLastKeyPointerTime > 500) {
                        HwPhoneWindowManager.this.mLastKeyPointerTime = motionEvent.getEventTime();
                        HwPhoneWindowManager.this.userActivityOnDesktop();
                    }
                }
            };
            this.mWindowManagerFuncs.registerExternalPointerEventListener(this.mLockScreenListener);
            this.mLockScreenBuildInDisplayListener = new PointerEventListener() {
                public void onPointerEvent(MotionEvent motionEvent) {
                    if (motionEvent.getEventTime() - HwPhoneWindowManager.this.mLastKeyPointerTime > 500) {
                        HwPhoneWindowManager.this.mLastKeyPointerTime = motionEvent.getEventTime();
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

    public void init(Context context, IWindowManager windowManager, WindowManagerFuncs windowManagerFuncs) {
        this.mHandlerEx = new PolicyHandlerEx(this, null);
        this.fingersense_enable = "fingersense_smartshot_enabled";
        this.fingersense_letters_enable = "fingersense_letters_enabled";
        this.line_gesture_enable = "fingersense_multiwindow_enabled";
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
        super.init(context, windowManager, windowManagerFuncs);
        this.mKeyguardDelegate.setHwPCKeyguardShowingCallback(new HwPCKeyguardShowingCallback() {
            public void onShowingChanged(boolean showing) {
                if (showing) {
                    HwPhoneWindowManager.this.lockScreen();
                }
            }
        });
        registerNotchListener();
        registerReceivers(context);
    }

    private void registerReceivers(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_NOTCH_SCREEN_ACTION);
        context.registerReceiverAsUser(this.mWhitelistReceived, UserHandle.ALL, filter, UPDATE_NOTCH_SCREEN_PER, null);
    }

    private void registerNotchListener() {
        boolean z = true;
        if (IS_NOTCH_PROP) {
            if (Secure.getIntForUser(this.mContext.getContentResolver(), HwNotchScreenWhiteConfig.DISPLAY_NOTCH_STATUS, 0, this.mCurUser) != 1) {
                z = false;
            }
            this.isNotchSwitchOpen = z;
            this.mNotchSwitchListener = new NotchSwitchListener() {
                public void onChange() {
                    boolean z = true;
                    HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
                    if (Secure.getIntForUser(HwPhoneWindowManager.this.mContext.getContentResolver(), HwNotchScreenWhiteConfig.DISPLAY_NOTCH_STATUS, 0, HwPhoneWindowManager.this.mCurUser) != 1) {
                        z = false;
                    }
                    hwPhoneWindowManager.isNotchTemp = z;
                    if (HwPhoneWindowManager.this.isNotchSwitchOpen != HwPhoneWindowManager.this.isNotchTemp) {
                        HwPhoneWindowManager.this.isNotchSwitchOpen = HwPhoneWindowManager.this.isNotchTemp;
                    }
                }
            };
            HwNotchScreenWhiteConfig.registerNotchSwitchListener(this.mContext, this.mNotchSwitchListener);
            try {
                ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                    public void onUserSwitching(int newUserId) throws RemoteException {
                        boolean z = true;
                        HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
                        if (Secure.getIntForUser(HwPhoneWindowManager.this.mContext.getContentResolver(), HwNotchScreenWhiteConfig.DISPLAY_NOTCH_STATUS, 0, HwPhoneWindowManager.this.mCurUser) != 1) {
                            z = false;
                        }
                        hwPhoneWindowManager.isNotchSwitchOpen = z;
                    }
                }, TAG);
            } catch (RemoteException e) {
                Log.i(TAG, "registerUserSwitchObserver fail", e);
            }
        }
    }

    private void updateFingerprintNav() {
        boolean z;
        boolean z2 = true;
        if (Secure.getIntForUser(this.mResolver, FINGERPRINT_CAMERA_SWITCH, 1, this.mCurUser) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isFingerShotCameraOn = z;
        if (Secure.getIntForUser(this.mResolver, FINGERPRINT_STOP_ALARM, 0, this.mCurUser) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isFingerStopAlarmOn = z;
        if (Secure.getIntForUser(this.mResolver, FINGERPRINT_ANSWER_CALL, 0, this.mCurUser) != 1) {
            z2 = false;
        }
        this.isFingerAnswerPhoneOn = z2;
    }

    private void registerFingerprintObserver(int userId) {
        this.mResolver.registerContentObserver(Secure.getUriFor(FINGERPRINT_CAMERA_SWITCH), true, this.mFingerprintObserver, userId);
        this.mResolver.registerContentObserver(Secure.getUriFor(FINGERPRINT_STOP_ALARM), true, this.mFingerprintObserver, userId);
        this.mResolver.registerContentObserver(Secure.getUriFor(FINGERPRINT_ANSWER_CALL), true, this.mFingerprintObserver, userId);
    }

    public void setCurrentUser(int userId, int[] currentProfileIds) {
        this.mCurUser = userId;
        registerFingerprintObserver(userId);
        this.mFingerprintObserver.onChange(true);
    }

    private void registerHwOUCObserver(int userId) {
        Log.d(TAG, "register HwOUC Observer");
        this.mResolver.registerContentObserver(Secure.getUriFor(KEY_HWOUC_KEYGUARD_VIEW_ON_TOP), true, this.mHwOUCObserver, userId);
    }

    private void updateHwOUCKeyguardViewState() {
        boolean z = true;
        if (1 != Secure.getIntForUser(this.mResolver, KEY_HWOUC_KEYGUARD_VIEW_ON_TOP, 0, this.mCurUser)) {
            z = false;
        }
        this.isHwOUCKeyguardViewOnTop = z;
    }

    private boolean supportActivityForbidSpecialKey(int keyCode) {
        if (this.isHwOUCKeyguardViewOnTop && (3 == keyCode || 4 == keyCode || 187 == keyCode)) {
            return true;
        }
        return false;
    }

    public int checkAddPermission(LayoutParams attrs, int[] outAppOp) {
        if (attrs.type == 2101) {
            return 0;
        }
        return super.checkAddPermission(attrs, outAppOp);
    }

    public int getWindowLayerFromTypeLw(int type, boolean canAddInternalSystemWindow) {
        switch (type) {
            case 2100:
                return 33;
            case 2101:
                return 34;
            default:
                int ret = super.getWindowLayerFromTypeLw(type, canAddInternalSystemWindow);
                if (ret >= 33) {
                    ret += 2;
                }
                return ret;
        }
    }

    public void freezeOrThawRotation(int rotation) {
        this.mDesiredRotation = rotation;
    }

    public boolean rotationHasCompatibleMetricsLw(int orientation, int rotation) {
        if (this.mDesiredRotation != 0) {
            return super.rotationHasCompatibleMetricsLw(orientation, rotation);
        }
        Slog.d(TAG, "desired rotation is rotation 0");
        return true;
    }

    public int rotationForOrientationLw(int orientation, int lastRotation) {
        if (isDefaultOrientationForced()) {
            return super.rotationForOrientationLw(orientation, lastRotation);
        }
        int desiredRotation = this.mDesiredRotation;
        if (desiredRotation < 0) {
            return super.rotationForOrientationLw(orientation, lastRotation);
        }
        Slog.i(TAG, "mDesiredRotation:" + this.mDesiredRotation);
        return desiredRotation;
    }

    public void beginPostLayoutPolicyLw(int displayWidth, int displayHeight) {
        super.beginPostLayoutPolicyLw(displayWidth, displayHeight);
        this.mStatuBarObsecured = false;
    }

    public void applyPostLayoutPolicyLw(WindowState win, LayoutParams attrs, WindowState attached, WindowState imeTarget) {
        super.applyPostLayoutPolicyLw(win, attrs, attached, imeTarget);
        if (win.isVisibleLw() && win.getSurfaceLayer() > this.mStatusBarLayer && isStatusBarObsecuredByWin(win)) {
            this.mStatuBarObsecured = true;
        }
    }

    protected void setHasAcitionBar(boolean hasActionBar) {
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
        boolean flag = false;
        try {
            return MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (NoExtAPIException e) {
            Log.w(TAG, "CoverManagerService->isMultiSimEnabled->NoExtAPIException!");
            return flag;
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
        boolean isOffhook = isPhoneInCall();
        boolean isSecure = isKeyguardSecure(this.mCurrentUserId);
        if (!isOffhook) {
            return true;
        }
        if (!isSecure || why == 3) {
            return false;
        }
        if (why != 6) {
            return true;
        }
        return false;
    }

    public boolean needTurnOffWithDismissFlag(WindowState topAppWin) {
        if (!(topAppWin == null || (topAppWin.getAttrs().flags & 524288) == 0 || (isKeyguardSecure(this.mCurrentUserId) ^ 1) == 0)) {
            Log.w(TAG, "TurnOffWithDismissFlag " + topAppWin);
        }
        return true;
    }

    public boolean needTurnOffWithDismissFlag() {
        return true;
    }

    protected boolean isWakeKeyWhenScreenOff(int keyCode) {
        if (!mCustUsed) {
            return super.isWakeKeyWhenScreenOff(keyCode);
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

    void startDockOrHome(boolean fromHomeKey, boolean awakenFromDreams) {
        if (fromHomeKey) {
            HwInputManagerServiceInternal inputManager = (HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerServiceInternal.class);
            if (inputManager != null) {
                inputManager.notifyHomeLaunching();
            }
        }
        super.startDockOrHome(fromHomeKey, awakenFromDreams);
    }

    private void getKeycodeFromCust() {
        String unableCustomizedWakeKey = null;
        try {
            unableCustomizedWakeKey = Systemex.getString(this.mContext.getContentResolver(), "unable_wake_up_key");
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
            return super.interceptMotionBeforeQueueingNonInteractive(whenNanos, policyFlags);
        }
        Slog.i(TAG, "interceptMotionBeforeQueueingNonInteractive policyFlags: " + policyFlags);
        Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
        return 0;
    }

    protected int getSingleHandState() {
        int windowManagerService = WindowManagerGlobal.getWindowManagerService();
        IBinder windowManagerBinder = windowManagerService.asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (windowManagerBinder != null) {
            try {
                data.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(1990, data, reply, 0);
                reply.readException();
                windowManagerService = reply.readInt();
                return windowManagerService;
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

    protected void unlockScreenPinningTest() {
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
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService->threw remote exception");
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public void finishedGoingToSleep(int why) {
        this.mHandler.removeCallbacks(this.mOverscanTimeout);
        this.mHandler.postDelayed(this.mOverscanTimeout, 200);
        super.finishedGoingToSleep(why);
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

    private void interceptScreenRecorder() {
        int i = 0;
        if (this.mScreenRecorderVolumeUpKeyTriggered && this.mScreenRecorderPowerKeyTriggered && (this.mScreenRecorderVolumeDownKeyTriggered ^ 1) != 0 && (SystemProperties.getBoolean("sys.super_power_save", false) ^ 1) != 0 && (keyguardIsShowingTq() ^ 1) != 0 && checkPackageInstalled(HUAWEI_SCREENRECORDER_PACKAGE)) {
            if (this.mCust == null || this.mCust.isSosAllowed()) {
                if (HWRIDEMODE_FEATURE_SUPPORTED) {
                    i = SystemProperties.getBoolean("sys.ride_mode", false);
                }
                if ((i ^ 1) != 0) {
                    long now = SystemClock.uptimeMillis();
                    if (now <= this.mScreenRecorderVolumeUpKeyTime + 150 && now <= this.mScreenRecorderPowerKeyTime + 150) {
                        this.mScreenRecorderVolumeUpKeyConsumed = true;
                        cancelPendingPowerKeyActionForDistouch();
                        this.mHandler.postDelayed(this.mScreenRecorderRunnable, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                    }
                }
            }
        }
    }

    private void cancelPendingScreenRecorderAction() {
        this.mHandler.removeCallbacks(this.mScreenRecorderRunnable);
    }

    boolean isVoiceCall() {
        boolean z = true;
        IAudioService audioService = getAudioService();
        if (audioService != null) {
            try {
                int mode = audioService.getMode();
                if (!(mode == 3 || mode == 2)) {
                    z = false;
                }
                return z;
            } catch (RemoteException e) {
                Log.w(TAG, "getMode exception");
            }
        }
        return false;
    }

    private void sendKeyEvent(int keycode) {
        int[] actions = new int[]{0, 1};
        for (int keyEvent : actions) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, keyEvent, keycode, 0, 0, -1, 0, 8, CommandsInterface.EMCOM_SD_XENGINE_START_ACC), 0);
        }
    }

    private boolean isExcluedScene() {
        String pkgName = ((ActivityManagerService) ServiceManager.getService("activity")).topAppName();
        String pkg_alarm = "com.android.deskclock/.alarmclock.LockAlarmFullActivity";
        boolean isSuperPowerMode = SystemProperties.getBoolean("sys.super_power_save", false);
        if (pkgName == null) {
            return false;
        }
        boolean z;
        if (pkgName.equals(pkg_alarm) || isSuperPowerMode || (this.mDeviceProvisioned ^ 1) != 0) {
            z = true;
        } else {
            z = keyguardOn();
        }
        return z;
    }

    private boolean isExcluedBackScene() {
        if (this.mTrikeyNaviMode == 1) {
            return isExcluedScene();
        }
        return this.mDeviceProvisioned ^ 1;
    }

    private boolean isExcluedRecentScene() {
        if (this.mTrikeyNaviMode == 1) {
            return this.mDeviceProvisioned ^ 1;
        }
        return isExcluedScene();
    }

    public void setCurrentUserLw(int newUserId) {
        super.setCurrentUserLw(newUserId);
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(true);
        if (this.fingerprintActionsListener != null) {
            this.fingerprintActionsListener.setCurrentUser(newUserId);
        }
        Slog.i(TAG, "setCurrentUserLw :" + newUserId);
    }

    private void resetButtonLightStatus() {
        if (this.mButtonLight != null) {
            if (this.mDeviceProvisioned) {
                Slog.i(TAG, "resetButtonLightStatus");
                this.mHandlerEx.removeMessages(MSG_BUTTON_LIGHT_TIMEOUT);
                if (this.mTrikeyNaviMode < 0) {
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
            } else {
                setButtonLightTimeout(false);
                this.mButtonLight.setBrightness(0);
            }
        }
    }

    private void setButtonLightTimeout(boolean timeout) {
        SystemProperties.set("sys.button.light.timeout", String.valueOf(timeout));
    }

    private void sendLightTimeoutMsg() {
        if (this.mButtonLight != null && (this.mDeviceProvisioned ^ 1) == 0) {
            this.mHandlerEx.removeMessages(MSG_BUTTON_LIGHT_TIMEOUT);
            if (this.mTrikeyNaviMode >= 0) {
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
            } else {
                setButtonLightTimeout(false);
                this.mButtonLight.setBrightness(0);
            }
        }
    }

    /* JADX WARNING: Missing block: B:6:0x001f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startHwVibrate(int vibrateMode) {
        if (!(isKeyguardLocked() || (this.mHapticEnabled ^ 1) != 0 || StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get("runtime.mmitest.isrunning", StorageUtils.SDCARD_RWMOUNTED_STATE)) || this.mVibrator == null)) {
            Log.d(TAG, "startVibrateWithConfigProp:" + vibrateMode);
            this.mVibrator.vibrate((long) vibrateMode);
        }
    }

    private boolean isMMITesting() {
        return StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get("runtime.mmitest.isrunning", StorageUtils.SDCARD_RWMOUNTED_STATE));
    }

    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        this.mTouchCountPolicy.updateTouchCountInfo();
        boolean down = event.getAction() == 0;
        int keyCode = event.getKeyCode();
        int flags = event.getFlags();
        int deviceID = event.getDeviceId();
        if (supportActivityForbidSpecialKey(keyCode)) {
            Log.d(TAG, "has intercept Key for block " + keyCode + ", some  ssssuper activity is on top now.");
            return 0;
        } else if (this.mSystraceLogCompleted) {
            if (this.mCust != null) {
                this.mCust.processCustInterceptKey(keyCode, down, this.mContext);
            }
            int origKeyCode = event.getOrigKeyCode();
            Flog.i(WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManager has intercept Key : " + keyCode + ", isdown : " + down + ", flags : " + flags);
            boolean isScreenOn = (536870912 & policyFlags) != 0;
            if ((keyCode == 26 || keyCode == 6 || keyCode == 187) && this.mFocusedWindow != null && (this.mFocusedWindow.getAttrs().hwFlags & FLOATING_MASK) == FLOATING_MASK) {
                Log.d(TAG, "power and endcall key received and passsing to user.");
                return 1;
            }
            int i;
            int result;
            if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
                if (EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate).handleWakeUpKey(event, isScreenOn ? -1 : this.mScreenOffReason)) {
                    Log.d(TAG, "EasyWakeUpManager has handled the keycode : " + event.getKeyCode());
                    return 0;
                }
            }
            if (down && event.getRepeatCount() == 0 && SystemProperties.get(VIBRATE_ON_TOUCH, StorageUtils.SDCARD_RWMOUNTED_STATE).equals(StorageUtils.SDCARD_ROMOUNTED_STATE) && ((keyCode == 82 && (268435456 & flags) == 0) || keyCode == 3 || keyCode == 4 || (policyFlags & 2) != 0)) {
                performHapticFeedbackLw(null, 1, false);
            }
            if ((origKeyCode == 305 || origKeyCode == 306 || origKeyCode == 307) && mTplusEnabled && (isRinging() ^ 1) != 0) {
                ContentResolver resolver = this.mContext.getContentResolver();
                int value = System.getInt(resolver, "hw_membrane_touch_enabled", 0);
                if (value == 0 && down) {
                    notifyTouchplusService(4, 0);
                }
                int navibaron = System.getInt(resolver, "hw_membrane_touch_navbar_enabled", 0);
                if (down && 1 == value && 1 == navibaron) {
                    if (System.getInt(resolver, TOUCHPLUS_SETTINGS_VIBRATION, 1) == 1) {
                        Log.v(TAG, "vibration is not disabled by user");
                        performHapticFeedbackLw(null, 1, true);
                    }
                }
            }
            boolean isInjected = (HwGlobalActionsData.FLAG_SHUTDOWN & policyFlags) != 0;
            boolean isWakeKeyFun = isWakeKeyFun(keyCode);
            if ((policyFlags & 1) != 0) {
                i = 1;
            } else {
                i = 0;
            }
            boolean isWakeKey = isWakeKeyFun | i;
            if ((!isScreenOn || (this.mHeadless ^ 1) == 0) && (!isInjected || (isWakeKey ^ 1) == 0)) {
                result = 0;
                if (down && isWakeKey) {
                    boolean isWakeKeyWhenScreenOff = isWakeKeyWhenScreenOff(keyCode);
                }
            } else {
                result = 1;
            }
            if (this.mFocusedWindow != null && (this.mFocusedWindow.getAttrs().hwFlags & 8) == 8 && ((keyCode == 25 || keyCode == 24) && StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get("runtime.mmitest.isrunning", StorageUtils.SDCARD_RWMOUNTED_STATE)))) {
                Log.i(TAG, "Prevent hard key volume event to mmi test before queueing.");
                return result & -2;
            } else if (lightScreenOnPcMode(keyCode)) {
                return 0;
            } else {
                switch (keyCode) {
                    case 3:
                    case 4:
                    case 187:
                        if (down) {
                            if (this.mFalseTouchMonitor != null) {
                                this.mFalseTouchMonitor.handleKeyEvent(event);
                            }
                            if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && (this.mHintShown ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                                Log.d(TAG, "keycode: " + keyCode + " is comsumed by disable touch mode.");
                                this.mHwScreenOnProximityLock.forceShowHint();
                                this.mHintShown = true;
                                break;
                            }
                        }
                        boolean isFrontFpNavi = FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION;
                        boolean isSupportTrikey = FrontFingerPrintSettings.isSupportTrikey();
                        boolean isMMITest = isMMITesting();
                        Flog.i(WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManagerinterceptKeyBeforeQueueing deviceID:" + deviceID + " isFrontFpNavi:" + isFrontFpNavi + " isSupportTrikey:" + isSupportTrikey + " isMMITest:" + isMMITest);
                        if (deviceID > 0 && isFrontFpNavi && isSupportTrikey && (isMMITest ^ 1) != 0 && keyCode == 4) {
                            if (isTrikeyNaviKeycodeFromLON(isInjected, isExcluedBackScene())) {
                                return 0;
                            }
                            sendLightTimeoutMsg();
                            if (down) {
                                this.mBackTrikeyHandled = false;
                                Message msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_BACK_LONG_PRESS);
                                msg.setAsynchronous(true);
                                this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                                if (this.mTrikeyNaviMode == 1) {
                                    return 0;
                                }
                            }
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
                        if (!this.mHasNavigationBar && keyCode == 4 && down) {
                            if (!isScreenInLockTaskMode()) {
                                this.mBackKeyPress = false;
                                this.mBackKeyPressTime = 0;
                                break;
                            }
                            this.mBackKeyPress = true;
                            this.mBackKeyPressTime = event.getDownTime();
                            interceptBackandMenuKey();
                            break;
                        }
                    case 24:
                    case 25:
                    case 164:
                        if (HwDeviceManager.disallowOp(38)) {
                            if (down) {
                                this.mHandler.post(new Runnable() {
                                    public void run() {
                                        Toast toast = Toast.makeText(HwPhoneWindowManager.this.mContext, 33685973, 0);
                                        toast.getWindowParams().type = 2010;
                                        LayoutParams windowParams = toast.getWindowParams();
                                        windowParams.privateFlags |= 16;
                                        toast.show();
                                    }
                                });
                            }
                            return result & -2;
                        } else if (keyCode == 25) {
                            if (down) {
                                if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && (this.mVolumeDownKeyDisTouch ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                                    Log.d(TAG, "keycode: KEYCODE_VOLUME_DOWN is comsumed by disable touch mode.");
                                    this.mVolumeDownKeyDisTouch = true;
                                    if (!this.mHintShown) {
                                        this.mHwScreenOnProximityLock.forceShowHint();
                                        this.mHintShown = true;
                                        break;
                                    }
                                }
                                if (isScreenOn && (this.mScreenRecorderVolumeDownKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                                    cancelPendingPowerKeyActionForDistouch();
                                    this.mScreenRecorderVolumeDownKeyTriggered = true;
                                    cancelPendingScreenRecorderAction();
                                }
                                if (isScreenOn && (this.mSystraceLogVolumeDownKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                                    this.mSystraceLogVolumeDownKeyTriggered = true;
                                    this.mSystraceLogFingerPrintTime = 0;
                                    this.mSystraceLogVolumeUpKeyTriggered = false;
                                }
                            } else {
                                this.mVolumeDownKeyDisTouch = false;
                                this.mScreenRecorderVolumeDownKeyTriggered = false;
                                cancelPendingScreenRecorderAction();
                                this.mSystraceLogVolumeDownKeyTriggered = false;
                            }
                            boolean keyguardShow = keyguardIsShowingTq();
                            Log.d(TAG, "interceptVolumeDownKey down=" + down + " keyguardShow=" + keyguardShow + " policyFlags=" + Integer.toHexString(policyFlags));
                            if ((!isScreenOn || (keyguardShow ^ 1) == 0) && !isInjected && (event.getFlags() & 1024) == 0) {
                                if (isDeviceProvisioned()) {
                                    if (!down) {
                                        if (event.getEventTime() - event.getDownTime() < 500) {
                                            cancelPendingQuickCallChordAction();
                                            break;
                                        }
                                        resetVolumeDownKeyLongPressed();
                                        break;
                                    }
                                    boolean isIntercept = false;
                                    boolean isVolumeDownDoubleClick = false;
                                    boolean isVoiceCall = isVoiceCall();
                                    boolean isMusicOrFMOrVoiceCallActive = !isMusicActive() ? isVoiceCall : true;
                                    boolean isPhoneActive = (isMusicActive() || (isPhoneIdle() ^ 1) != 0) ? true : isVoiceCall;
                                    if (this.isVoiceRecognitionActive) {
                                        long interval = event.getEventTime() - this.mLastStartVassistantServiceTime;
                                        if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MAX) {
                                            this.isVoiceRecognitionActive = false;
                                        } else if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MIN) {
                                            this.isVoiceRecognitionActive = AudioSystem.isSourceActive(6);
                                        }
                                    }
                                    Log.i(TAG, "isMusicOrFMOrVoiceCallActive=" + isMusicOrFMOrVoiceCallActive + " isVoiceRecognitionActive=" + this.isVoiceRecognitionActive);
                                    if (!(isMusicOrFMOrVoiceCallActive || (this.isVoiceRecognitionActive ^ 1) == 0 || (SystemProperties.getBoolean("sys.super_power_save", false) ^ 1) == 0)) {
                                        long timediff = event.getEventTime() - this.mLastVolumeDownKeyDownTime;
                                        this.mLastVolumeDownKeyDownTime = event.getEventTime();
                                        if (timediff < 400) {
                                            isVolumeDownDoubleClick = true;
                                            if (this.mListener == null) {
                                                this.mListener = new ProximitySensorListener();
                                            }
                                            turnOnSensorListener();
                                            if ((!this.mIsProximity && this.mSensorRegisted) || (this.mSensorRegisted ^ 1) != 0) {
                                                Log.i(TAG, "mIsProximity " + this.mIsProximity + ", mSensorRegisted " + this.mSensorRegisted);
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
                                        }
                                        if (!isScreenOn || isVolumeDownDoubleClick) {
                                            isIntercept = true;
                                        }
                                    }
                                    if (!(isPhoneActive || (isScreenOn ^ 1) == 0 || (isVolumeDownDoubleClick ^ 1) == 0)) {
                                        if (checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE)) {
                                            notifyVassistantService("wakeup", 2, event);
                                            if (this.mListener == null) {
                                                this.mListener = new ProximitySensorListener();
                                            }
                                            turnOnSensorListener();
                                            interceptQuickCallChord();
                                            isIntercept = true;
                                        }
                                    }
                                    Log.i(TAG, "intercept volume down key, isIntercept=" + isIntercept + " now=" + SystemClock.uptimeMillis() + " EventTime=" + event.getEventTime());
                                    if (!isInterceptAndCheckRinging(isIntercept)) {
                                        if (getTelecommService().isInCall() && (result & 1) == 0 && this.mCust != null && this.mCust.isVolumnkeyWakeup()) {
                                            this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                            break;
                                        }
                                    }
                                    return result;
                                }
                                Log.i(TAG, "Device is not Provisioned");
                                break;
                            }
                        } else if (keyCode == 24) {
                            if (down) {
                                if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && (this.mVolumeUpKeyDisTouch ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
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
                                    break;
                                }
                                if (isScreenOn && (this.mScreenRecorderVolumeUpKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                                    cancelPendingPowerKeyActionForDistouch();
                                    this.mScreenRecorderVolumeUpKeyTriggered = true;
                                    this.mScreenRecorderVolumeUpKeyTime = event.getDownTime();
                                    this.mScreenRecorderVolumeUpKeyConsumed = false;
                                    interceptScreenRecorder();
                                }
                                Log.d(TAG, "isScreenOn=" + isScreenOn + " mSystraceLogVolumeUpKeyTriggered=" + this.mSystraceLogVolumeUpKeyTriggered + " mScreenRecorderVolumeUpKeyConsumed=" + this.mScreenRecorderVolumeUpKeyConsumed);
                                if (Jlog.isEnable() && Jlog.isBetaUser() && isScreenOn && (this.mSystraceLogVolumeUpKeyTriggered ^ 1) != 0 && (this.mSystraceLogPowerKeyTriggered ^ 1) != 0 && (this.mSystraceLogVolumeDownKeyTriggered ^ 1) != 0 && (this.mScreenRecorderVolumeUpKeyConsumed ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                                    this.mSystraceLogVolumeUpKeyTriggered = true;
                                    this.mSystraceLogVolumeUpKeyTime = event.getDownTime();
                                    this.mSystraceLogVolumeUpKeyConsumed = false;
                                    interceptSystraceLog();
                                    Log.d(TAG, "volumeup process: fingerprint first, then volumeup");
                                    if (this.mSystraceLogVolumeUpKeyConsumed) {
                                        return result & -2;
                                    }
                                }
                                if (getTelecommService().isInCall() && (result & 1) == 0 && this.mCust != null && this.mCust.isVolumnkeyWakeup()) {
                                    this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                }
                            } else {
                                this.mVolumeUpKeyDisTouch = false;
                                this.mScreenRecorderVolumeUpKeyTriggered = false;
                                cancelPendingScreenRecorderAction();
                                this.mSystraceLogVolumeUpKeyTriggered = false;
                            }
                            if (this.mCust != null) {
                                if (this.mCust.interceptVolumeUpKey(event, this.mContext, isScreenOn, keyguardIsShowingTq(), !isMusicActive() ? isVoiceCall() : true, isInjected, down)) {
                                    return result;
                                }
                            }
                        }
                        break;
                    case 26:
                        cancelSmartKeyLongPressed();
                        if (!down) {
                            this.mPowerKeyDisTouch = false;
                            this.mScreenRecorderPowerKeyTriggered = false;
                            cancelPendingScreenRecorderAction();
                            this.mSystraceLogPowerKeyTriggered = false;
                            break;
                        }
                        if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && (this.mPowerKeyDisTouch ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                            this.mPowerKeyDisTouch = true;
                            this.mPowerKeyDisTouchTime = event.getDownTime();
                            interceptTouchDisableMode();
                        }
                        if (isScreenOn && (this.mScreenRecorderPowerKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                            this.mScreenRecorderPowerKeyTriggered = true;
                            this.mScreenRecorderPowerKeyTime = event.getDownTime();
                            interceptScreenRecorder();
                        }
                        if (isScreenOn && (this.mSystraceLogPowerKeyTriggered ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
                            this.mSystraceLogPowerKeyTriggered = true;
                            this.mSystraceLogFingerPrintTime = 0;
                            this.mSystraceLogVolumeUpKeyTriggered = false;
                            break;
                        }
                    case 82:
                        if (!this.mHasNavigationBar && down) {
                            if (!isScreenInLockTaskMode()) {
                                this.mMenuKeyPress = false;
                                this.mMenuKeyPressTime = 0;
                                break;
                            }
                            this.mMenuKeyPress = true;
                            this.mMenuKeyPressTime = event.getDownTime();
                            interceptBackandMenuKey();
                            break;
                        }
                    case MemoryConstant.MSG_DIRECT_SWAPPINESS /*303*/:
                        if (mTplusEnabled && down) {
                            if (event.getEventTime() - this.mLastTouchDownUpLeftKeyDownTime < 400) {
                                this.isTouchDownUpLeftDoubleClick = true;
                                this.mTouchDownUpLeftConsumeCount = 2;
                                notifyTouchplusService(MemoryConstant.MSG_DIRECT_SWAPPINESS, 1);
                            }
                            this.mLastTouchDownUpLeftKeyDownTime = event.getEventTime();
                            break;
                        }
                    case MemoryConstant.MSG_PROTECTLRU_SET_FILENODE /*304*/:
                        if (mTplusEnabled && down) {
                            if (event.getEventTime() - this.mLastTouchDownUpRightKeyDownTime < 400) {
                                this.isTouchDownUpRightDoubleClick = true;
                                this.mTouchDownUpRightConsumeCount = 2;
                                notifyTouchplusService(MemoryConstant.MSG_PROTECTLRU_SET_FILENODE, 1);
                            }
                            this.mLastTouchDownUpRightKeyDownTime = event.getEventTime();
                            break;
                        }
                    case MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE /*308*/:
                        Log.i(TAG, "KeyEvent.KEYCODE_SMARTKEY in");
                        if (down) {
                            if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && (this.mHintShown ^ 1) != 0 && (event.getFlags() & 1024) == 0) {
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
                            handleSmartKey(this.mContext, event, this.mHandler, isScreenOn);
                            return 0;
                        }
                        break;
                    case AwareJobSchedulerService.MSG_JOB_EXPIRED /*401*/:
                    case AwareJobSchedulerService.MSG_CHECK_JOB /*402*/:
                    case AwareJobSchedulerService.MSG_REMOVE_JOB /*403*/:
                    case AwareJobSchedulerService.MSG_CONTROLLER_CHANGED /*404*/:
                    case 405:
                        processing_KEYCODE_SOUNDTRIGGER_EVENT(keyCode, this.mContext, isMusicActive(), down, keyguardIsShowingTq());
                        break;
                    case 501:
                    case 502:
                    case 511:
                    case 512:
                    case 513:
                    case CommandsInterface.EMCOM_DS_HTTP_INFO /*514*/:
                    case WifiProCommonUtils.RESP_CODE_UNSTABLE /*601*/:
                        Log.d(TAG, "event.flags=" + flags + " previous mSystraceLogFingerPrintTime=" + this.mSystraceLogFingerPrintTime);
                        if (flags == 8) {
                            if (!Jlog.isEnable() || (Jlog.isBetaUser() ^ 1) != 0 || (down ^ 1) != 0 || (isScreenOn ^ 1) != 0 || this.mSystraceLogPowerKeyTriggered || this.mSystraceLogVolumeDownKeyTriggered) {
                                return result & -2;
                            }
                            this.mSystraceLogFingerPrintTime = event.getDownTime();
                            return result & -2;
                        }
                        break;
                }
                return super.interceptKeyBeforeQueueing(event, policyFlags);
            }
        } else {
            Log.d(TAG, " has intercept Key for block : " + keyCode + ", isdown : " + down + ", flags : " + flags);
            return 0;
        }
    }

    private boolean lightScreenOnPcMode(int keyCode) {
        if (HwPCUtils.isPcCastModeInServer() && (keyCode == 26 || keyCode == 502 || keyCode == 511 || keyCode == 512 || keyCode == 513 || keyCode == CommandsInterface.EMCOM_DS_HTTP_INFO || keyCode == 501 || keyCode == WifiProCommonUtils.RESP_CODE_UNSTABLE || keyCode == 515 || keyCode == 161 || keyCode == 21 || keyCode == 22 || keyCode == 3 || keyCode == 4)) {
            boolean keyHandled = false;
            try {
                IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
                if (!(pcMgr == null || (pcMgr.isScreenPowerOn() ^ 1) == 0)) {
                    HwPCUtils.log(TAG, "some key set screen from OFF to ON");
                    pcMgr.setScreenPower(true);
                    keyHandled = true;
                    if (keyCode == 26) {
                        cancelPendingPowerKeyActionForDistouch();
                    }
                }
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "lightScreenOnPcMode " + e);
            }
            if (this.mPowerManagerInternal.isUserActivityScreenDimOrDream() || keyHandled) {
                this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
                return true;
            }
        }
        return false;
    }

    boolean isRinging() {
        TelecomManager telecomManager = getTelecommService();
        return (telecomManager == null || !telecomManager.isRinging()) ? false : "1".equals(SystemProperties.get("persist.sys.show_incallscreen", "0"));
    }

    public KeyEvent dispatchUnhandledKey(WindowState win, KeyEvent event, int policyFlags) {
        int keyCode = event.getKeyCode();
        boolean isScreenOn = (536870912 & policyFlags) != 0;
        switch (keyCode) {
            case MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE /*308*/:
                if (event.getRepeatCount() != 0) {
                    Log.d(TAG, "event.getRepeatCount() != 0 so just break");
                    return null;
                } else if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", false) && (needDropSmartKey() ^ 1) == 0) {
                    return null;
                } else {
                    handleSmartKey(this.mContext, event, this.mHandler, isScreenOn);
                    return null;
                }
            default:
                return super.dispatchUnhandledKey(win, event, policyFlags);
        }
    }

    private boolean isHardwareKeyboardConnected() {
        Log.i(TAG, "isHardwareKeyboardConnected--begin");
        int[] devices = InputDevice.getDeviceIds();
        boolean isConnected = false;
        for (int device : devices) {
            InputDevice device2 = InputDevice.getDevice(device);
            if (device2 != null) {
                if (device2.getProductId() != 4817 || device2.getVendorId() != 1455) {
                    if (device2.isExternal() && (device2.getSources() & CommandsInterface.EMCOM_SD_XENGINE_START_ACC) != 0) {
                        isConnected = true;
                        break;
                    }
                }
                isConnected = true;
                break;
            }
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
        if (this.mStylusGestureListener != null) {
            this.mStylusGestureListener.setToolType();
        }
    }

    public long interceptKeyBeforeDispatching(WindowState win, KeyEvent event, int policyFlags) {
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int flags = event.getFlags();
        int origKeyCode = event.getOrigKeyCode();
        boolean down = event.getAction() == 0;
        int deviceID = event.getDeviceId();
        boolean isInjected = (HwGlobalActionsData.FLAG_SHUTDOWN & policyFlags) != 0;
        Flog.i(WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManagerinterceptKeyTi keyCode=" + keyCode + " down=" + down + " repeatCount=" + repeatCount + " isInjected=" + isInjected + " origKeyCode=" + origKeyCode);
        if (HwPCUtils.isPcCastModeInServer() && event.getEventTime() - this.mLastKeyPointerTime > 500) {
            this.mLastKeyPointerTime = event.getEventTime();
            userActivityOnDesktop();
        }
        try {
            if (this.mIHwWindowCallback != null) {
                this.mIHwWindowCallback.interceptKeyBeforeDispatching(event, policyFlags);
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
                        Toast toast = Toast.makeText(HwPhoneWindowManager.this.mContext, 33685949, 0);
                        toast.getWindowParams().type = 2010;
                        LayoutParams windowParams = toast.getWindowParams();
                        windowParams.privateFlags |= 16;
                        toast.show();
                    }
                });
            }
            return (long) result;
        }
        if (isRightKey(keyCode) && isHardwareKeyboardConnected()) {
            String lastIme = Secure.getString(this.mContext.getContentResolver(), "default_input_method");
            if (lastIme != null && lastIme.contains("com.visionobjects.stylusmobile.v3_2_huawei")) {
                HwInputMethodManager.setDefaultIme("");
                setToolType();
            }
        }
        int result1 = getGameControlKeyReslut(event);
        if (-2 != result1) {
            Log.i(TAG, "getGameControlKeyReslut return !");
            return (long) result1;
        } else if ((keyCode == 3 || keyCode == 187) && win != null && (win.getAttrs().hwFlags & FLOATING_MASK) == FLOATING_MASK) {
            return 0;
        } else {
            long now;
            long timeoutTime;
            if ((origKeyCode == 305 || origKeyCode == 306 || origKeyCode == 307) && mTplusEnabled) {
                ContentResolver resolver = this.mContext.getContentResolver();
                int touchPlusOn = System.getInt(resolver, "hw_membrane_touch_enabled", 0);
                int value = System.getInt(resolver, "hw_membrane_touch_navbar_enabled", 0);
                if (touchPlusOn == 0 || value == 0 || (isRinging() && origKeyCode != 307)) {
                    return -1;
                }
            }
            if (keyCode == 303 && mTplusEnabled) {
                if (this.isTouchDownUpLeftDoubleClick) {
                    if (!down) {
                        this.mTouchDownUpLeftConsumeCount--;
                        if (this.mTouchDownUpLeftConsumeCount == 0) {
                            this.isTouchDownUpLeftDoubleClick = false;
                        }
                    }
                    return -1;
                } else if (repeatCount == 0) {
                    now = SystemClock.uptimeMillis();
                    timeoutTime = event.getEventTime() + 400;
                    if (now < timeoutTime) {
                        return timeoutTime - now;
                    }
                }
            }
            if (keyCode == 304 && mTplusEnabled) {
                if (this.isTouchDownUpRightDoubleClick) {
                    if (!down) {
                        this.mTouchDownUpRightConsumeCount--;
                        if (this.mTouchDownUpRightConsumeCount == 0) {
                            this.isTouchDownUpRightDoubleClick = false;
                        }
                    }
                    return -1;
                } else if (repeatCount == 0) {
                    now = SystemClock.uptimeMillis();
                    timeoutTime = event.getEventTime() + 400;
                    if (now < timeoutTime) {
                        return timeoutTime - now;
                    }
                }
            }
            if (keyCode == 82) {
                if (mTplusEnabled && origKeyCode == 307 && 1 == System.getInt(this.mContext.getContentResolver(), "hw_membrane_touch_navbar_enabled", 0)) {
                    if (down) {
                        if (repeatCount == 0) {
                            this.mMenuClickedOnlyOnce = true;
                        } else if (repeatCount == 1) {
                            this.mMenuClickedOnlyOnce = false;
                            transactToStatusBarService(101, "resentapp", "resentapp", 0);
                        }
                    } else if (this.mMenuClickedOnlyOnce) {
                        this.mMenuClickedOnlyOnce = false;
                        if (this.mLastFocusNeedsMenu) {
                            sendHwMenuKeyEvent();
                        } else {
                            toggleRecentApps();
                        }
                    }
                    return -1;
                } else if (!this.mHasNavigationBar && (268435456 & flags) == 0) {
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
                }
            }
            if (this.mVolumeUpKeyDisTouch && (this.mPowerKeyDisTouch ^ 1) != 0 && (flags & 1024) == 0) {
                now = SystemClock.uptimeMillis();
                timeoutTime = this.mVolumeUpKeyDisTouchTime + 150;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
                return -1;
            }
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
            if (handleDesktopKeyEvent(event)) {
                return -1;
            }
            if ("tablet".equals(SystemProperties.get("ro.build.characteristics", "")) && keyCode == 62 && down && event.isShiftPressed()) {
                IInputMethodManager inputMethodManager = IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method"));
                if (inputMethodManager != null) {
                    try {
                        inputMethodManager.showInputMethodPickerFromClient(null, 2);
                    } catch (RemoteException e) {
                        Log.e(TAG, "showInputMethodPicker failed");
                    }
                }
                return -1;
            }
            switch (keyCode) {
                case 3:
                case 4:
                case 25:
                case 187:
                    if (this.mHintShown) {
                        if (!down) {
                            this.mHintShown = false;
                        }
                        return -1;
                    }
                    boolean isFrontFpNavi = FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION;
                    boolean isSupportTrikey = FrontFingerPrintSettings.isSupportTrikey();
                    boolean isMMITest = isMMITesting();
                    Flog.i(WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManagerdeviceID:" + deviceID + " isFrontFpNavi:" + isFrontFpNavi + " isSupportTrikey:" + isSupportTrikey + " isMMITest:" + isMMITest);
                    if (deviceID > 0 && isFrontFpNavi && isSupportTrikey && (isMMITest ^ 1) != 0 && keyCode == 187) {
                        if (isTrikeyNaviKeycodeFromLON(isInjected, isExcluedRecentScene())) {
                            return -1;
                        }
                        sendLightTimeoutMsg();
                        if (!down) {
                            boolean handled = this.mRecentTrikeyHandled;
                            if (!this.mRecentTrikeyHandled) {
                                this.mRecentTrikeyHandled = true;
                                this.mHandlerEx.removeMessages(MSG_TRIKEY_RECENT_LONG_PRESS);
                            }
                            if (!handled) {
                                if (this.mTrikeyNaviMode == 1) {
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
                            this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                            if (this.mTrikeyNaviMode == 0) {
                                preloadRecentApps();
                            }
                        }
                        return -1;
                    }
            }
            if ((flags & 1024) == 0) {
                if (this.mScreenRecorderVolumeUpKeyTriggered && (this.mScreenRecorderPowerKeyTriggered ^ 1) != 0) {
                    now = SystemClock.uptimeMillis();
                    timeoutTime = this.mScreenRecorderVolumeUpKeyTime + 150;
                    if (now < timeoutTime) {
                        return timeoutTime - now;
                    }
                }
                if (keyCode == 24 && this.mScreenRecorderVolumeUpKeyConsumed) {
                    if (!down) {
                        this.mScreenRecorderVolumeUpKeyConsumed = false;
                    }
                    return -1;
                }
                if (this.mSystraceLogVolumeUpKeyTriggered) {
                    now = SystemClock.uptimeMillis();
                    timeoutTime = this.mSystraceLogVolumeUpKeyTime + 150;
                    if (now < timeoutTime) {
                        Log.d(TAG, "keyCode=" + keyCode + " down=" + down + " in queue: now=" + now + " timeout=" + timeoutTime);
                        return timeoutTime - now;
                    }
                }
                if (keyCode == 24 && this.mSystraceLogVolumeUpKeyConsumed) {
                    if (!down) {
                        this.mSystraceLogVolumeUpKeyConsumed = false;
                    }
                    Log.d(TAG, "systracelog volumeup down=" + down + " leave queue");
                    return -1;
                }
            }
            return super.interceptKeyBeforeDispatching(win, event, policyFlags);
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

    private void lockScreen() {
        if (this.mWindowManagerInternal != null) {
            this.mWindowManagerInternal.setFocusedDisplayId(0, "lockScreen");
        }
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.lockScreen();
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

    private void userActivityOnDesktop() {
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
        int[] actions = new int[]{0, 1};
        for (int keyEvent : actions) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, keyEvent, 82, 0, 0, -1, 0, 268435464, CommandsInterface.EMCOM_SD_XENGINE_START_ACC), 0);
        }
    }

    protected void launchAssistAction(String hint, int deviceId) {
        if (checkPackageInstalled("com.google.android.googlequicksearchbox")) {
            super.launchAssistAction(hint, deviceId);
            return;
        }
        sendCloseSystemWindows();
        boolean enableVoiceAssistant = Secure.getInt(this.mContext.getContentResolver(), "hw_long_home_voice_assistant", 0) == 1;
        if (IS_LONG_HOME_VASSITANT && enableVoiceAssistant) {
            performHapticFeedbackLw(null, 0, false);
            try {
                String intent = "android.intent.action.ASSIST";
                if (checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE)) {
                    intent = VOICE_ASSISTANT_ACTION;
                }
                this.mContext.startActivity(new Intent(intent).setFlags(268435456));
            } catch (ActivityNotFoundException anfe) {
                Slog.w(TAG, "No activity to handle voice assistant action.", anfe);
            }
        }
    }

    private boolean checkPackageInstalled(String packageName) {
        try {
            this.mContext.getPackageManager().getPackageInfo(packageName, 128);
            return true;
        } catch (NameNotFoundException e) {
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

    boolean isMusicActive() {
        if (((AudioManager) this.mContext.getSystemService("audio")) != null) {
            return AudioSystem.isStreamActive(3, 0);
        }
        Log.w(TAG, "isMusicActive: couldn't get AudioManager reference");
        return false;
    }

    boolean isDeviceProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    void handleVolumeKey(int stream, int keycode) {
        IAudioService audioService = getAudioService();
        if (audioService != null) {
            try {
                int i;
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.acquire();
                }
                if (keycode == 24) {
                    i = 1;
                } else {
                    i = -1;
                }
                audioService.adjustStreamVolume(stream, i, 0, this.mContext.getOpPackageName());
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.release();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "IAudioService.adjust*StreamVolume() threw RemoteException");
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.release();
                }
            } catch (Throwable th) {
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.release();
                }
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

    private void cancelVolumeDownKeyPressed() {
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

    private void initQuickcall() {
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
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
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
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
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService four params->threw remote exception");
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    protected void transactToStatusBarService(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle) {
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
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService->threw remote exception");
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public void updateSystemUiColorLw(WindowState win) {
        if (win != null) {
            if (isCoverWindow(win)) {
                Slog.i(TAG, "updateSystemUiColorLw isCoverWindow return " + win);
                return;
            }
            LayoutParams attrs = win.getAttrs();
            if (this.mLastColorWin != win || this.mLastStatusBarColor != attrs.statusBarColor || this.mLastNavigationBarColor != attrs.navigationBarColor) {
                boolean colorChanged;
                boolean isFloating = getFloatingValue(attrs.isEmuiStyle);
                boolean isPopup = (attrs.type == 1000 || attrs.type == 1002 || attrs.type == 2009 || attrs.type == 2010) ? true : attrs.type == 2003;
                if (attrs.type == 3) {
                }
                boolean isTouchExplrEnabled = this.mAccessibilityManager.isTouchExplorationEnabled();
                int isEmuiStyle = getEmuiStyleValue(attrs.isEmuiStyle);
                int statusBarColor = attrs.statusBarColor;
                int navigationBarColor = attrs.navigationBarColor;
                int isEmuiLightStyle = getEmuiLightStyleValue(attrs.hwFlags);
                if (isTouchExplrEnabled) {
                    colorChanged = isTouchExplrEnabled != this.mIsTouchExplrEnabled;
                    isEmuiStyle = -2;
                    isEmuiLightStyle = -1;
                } else if (this.mLastStatusBarColor != statusBarColor) {
                    colorChanged = true;
                } else if (this.mLastNavigationBarColor != navigationBarColor) {
                    colorChanged = true;
                } else {
                    colorChanged = false;
                }
                boolean styleChanged = isEmuiStyleChanged(isEmuiStyle, isEmuiLightStyle);
                boolean isStatusBarOrKeyGuard = (win == this.mStatusBar || attrs.type == 2024) ? true : isKeyguardHostWindow(attrs);
                int ignoreWindow = (isStatusBarOrKeyGuard || isFloating || isPopup || (attrs.type == 2034)) ? 1 : win.getStackId() != 3 ? win.isInMultiWindowMode() : false;
                boolean changed = (!styleChanged || (ignoreWindow ^ 1) == 0) ? (styleChanged || (ignoreWindow ^ 1) == 0) ? false : colorChanged : true;
                if (ignoreWindow == 0) {
                    win.setCanCarryColors(true);
                }
                if (changed) {
                    int i;
                    if (isTouchExplrEnabled) {
                        i = -16777216;
                    } else {
                        i = statusBarColor;
                    }
                    this.mLastStatusBarColor = i;
                    if (isTouchExplrEnabled) {
                        i = -16777216;
                    } else {
                        i = navigationBarColor;
                    }
                    this.mLastNavigationBarColor = i;
                    this.mLastIsEmuiStyle = isEmuiStyle;
                    this.mIsTouchExplrEnabled = isTouchExplrEnabled;
                    this.mLastColorWin = win;
                    this.mLastIsEmuiLightStyle = isEmuiLightStyle;
                    Slog.v(TAG, "updateSystemUiColorLw window=" + win + ",EmuiStyle=" + isEmuiStyle + ",StatusBarColor=0x" + Integer.toHexString(statusBarColor) + ",NavigationBarColor=0x" + Integer.toHexString(navigationBarColor) + ", mLastIsEmuiLightStyle=" + this.mLastIsEmuiLightStyle + ", mForceNotchStatusBar=" + this.mForceNotchStatusBar);
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            if ((!HwPhoneWindowManager.this.isNotchSwitchOpen && (HwPhoneWindowManager.this.mForceNotchStatusBar ^ 1) != 0) || HwPhoneWindowManager.this.isNotchSwitchOpen) {
                                HwPhoneWindowManager.this.transactToStatusBarService(106, "setSystemUIColor", HwPhoneWindowManager.this.mLastIsEmuiStyle, HwPhoneWindowManager.this.mLastStatusBarColor, HwPhoneWindowManager.this.mLastNavigationBarColor, HwPhoneWindowManager.this.mLastIsEmuiLightStyle);
                            }
                        }
                    });
                }
            }
        }
    }

    protected int getEmuiStyleValue(int styleValue) {
        return styleValue == -1 ? -1 : SmartcareConstants.INVALID & styleValue;
    }

    protected int getEmuiLightStyleValue(int styleValue) {
        return (styleValue & 16) != 0 ? 1 : -1;
    }

    protected boolean isEmuiStyleChanged(int isEmuiStyle, int isEmuiLightStyle) {
        return (this.mLastIsEmuiStyle == isEmuiStyle && this.mLastIsEmuiLightStyle == isEmuiLightStyle) ? false : true;
    }

    protected boolean getFloatingValue(int styleValue) {
        return styleValue != -1 && (styleValue & FLOATING_MASK) == FLOATING_MASK;
    }

    public void onTouchExplorationStateChanged(boolean enabled) {
        updateSystemUiColorLw(getCurrentWin());
    }

    protected void hwInit() {
        this.mAccessibilityManager.addTouchExplorationStateChangeListener(this);
    }

    IStatusBarService getHWStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            }
            iStatusBarService = this.mStatusBarService;
        }
        return iStatusBarService;
    }

    private void interceptTouchDisableMode() {
        if (this.mVolumeUpKeyDisTouch && this.mPowerKeyDisTouch && (this.mVolumeDownKeyDisTouch ^ 1) != 0) {
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
        if (audioManager == null) {
            return false;
        }
        boolean headSetConnectedState = (audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn()) ? true : audioManager.isBluetoothScoOn();
        Log.d(TAG, "checkHeadSetIsConnected : " + headSetConnectedState);
        return headSetConnectedState;
    }

    public void screenTurningOn(ScreenOnListener screenOnListener) {
        super.screenTurningOn(screenOnListener);
        if (this.mContext == null) {
            Log.d(TAG, "Context object is null.");
            return;
        }
        if (!(this.mFalseTouchMonitor == null || !this.mFalseTouchMonitor.isFalseTouchFeatureOn() || (this.mScreenOnForFalseTouch ^ 1) == 0)) {
            this.mScreenOnForFalseTouch = true;
            this.mWindowManagerFuncs.registerPointerEventListener(this.mFalseTouchMonitor.getEventListener());
        }
        int isModeEnabled;
        if (System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1, ActivityManager.getCurrentUser()) > 0) {
            isModeEnabled = "factory".equals(SystemProperties.get("ro.runmode", "normal")) ^ 1;
        } else {
            isModeEnabled = 0;
        }
        if (!(this.mHwScreenOnProximityLock == null || isModeEnabled == 0)) {
            boolean isPhoneCallState = checkPhoneOFFHOOK();
            if (!isPhoneCallState || (isPhoneCallState && checkHeadSetIsConnected())) {
                this.mHwScreenOnProximityLock.acquireLock(this);
            }
        }
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
            EasyWakeUpManager mWakeUpManager = EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate);
            if (mWakeUpManager != null) {
                mWakeUpManager.turnOffSensorListener();
            }
        }
    }

    public void screenTurnedOn() {
        super.screenTurnedOn();
        if (this.mSystemReady) {
            Slog.d(TAG, "screenTurnedOn");
            if (this.mBooted) {
                PickUpWakeScreenManager.getInstance(this.mContext, this.mHandler, this.mWindowManagerFuncs, this.mKeyguardDelegate).enablePickupMotionOrNot(false);
            }
        }
    }

    public void screenTurnedOff() {
        super.screenTurnedOff();
        if (this.mFalseTouchMonitor != null && this.mFalseTouchMonitor.isFalseTouchFeatureOn() && this.mScreenOnForFalseTouch) {
            this.mScreenOnForFalseTouch = false;
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.mFalseTouchMonitor.getEventListener());
        }
        if (this.mHwScreenOnProximityLock != null) {
            this.mHwScreenOnProximityLock.releaseLock(1);
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

    public int selectAnimationLw(WindowState win, int transit) {
        int i = 0;
        if (win != this.mNavigationBar || this.mNavigationBarPosition == 0 || (transit != 1 && transit != 3)) {
            return super.selectAnimationLw(win, transit);
        }
        if (!mIsHwNaviBar) {
            i = 17432616;
        }
        return i;
    }

    public void onConfigurationChanged() {
        super.onConfigurationChanged();
        if (this.mNavigationBarPolicy == null) {
            return;
        }
        if (this.mNavigationBarPolicy.mMinNavigationBar) {
            this.mNavigationBarHeightForRotationDefault = (int[]) this.mNavigationBarHeightForRotationMin.clone();
            this.mNavigationBarWidthForRotationDefault = (int[]) this.mNavigationBarWidthForRotationMin.clone();
            return;
        }
        this.mNavigationBarHeightForRotationDefault = (int[]) this.mNavigationBarHeightForRotationMax.clone();
        this.mNavigationBarWidthForRotationDefault = (int[]) this.mNavigationBarWidthForRotationMax.clone();
    }

    public void updateNavigationBar(boolean minNaviBar) {
        if (this.mNavigationBarPolicy != null) {
            if (minNaviBar) {
                this.mNavigationBarHeightForRotationDefault = (int[]) this.mNavigationBarHeightForRotationMin.clone();
                this.mNavigationBarWidthForRotationDefault = (int[]) this.mNavigationBarWidthForRotationMin.clone();
            } else {
                this.mNavigationBarHeightForRotationDefault = (int[]) this.mNavigationBarHeightForRotationMax.clone();
                this.mNavigationBarWidthForRotationDefault = (int[]) this.mNavigationBarWidthForRotationMax.clone();
            }
            this.mNavigationBarPolicy.updateNavigationBar(minNaviBar);
        }
    }

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        if ((uiMode & 15) == 3) {
            return super.getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode, displayId);
        }
        if (displayId == 0 && this.mHasNavigationBar) {
            if (this.mNavigationBarCanMove && fullWidth > fullHeight) {
                if (this.mNavigationBarPolicy != null && this.mNavigationBarPolicy.mMinNavigationBar) {
                    return fullWidth - this.mNavigationBarWidthForRotationMin[rotation];
                }
                int nonDecorDisplayWidth;
                if (getNavibarAlignLeftWhenLand()) {
                    nonDecorDisplayWidth = fullWidth - this.mContext.getResources().getDimensionPixelSize(34472115);
                } else {
                    nonDecorDisplayWidth = fullWidth - this.mNavigationBarWidthForRotationMax[rotation];
                }
                return nonDecorDisplayWidth;
            }
        } else if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            return fullWidth;
        } else {
            return fullWidth;
        }
        return fullWidth;
    }

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        if ((uiMode & 15) == 3) {
            return super.getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayId);
        }
        if (displayId == 0 && this.mHasNavigationBar) {
            if (!this.mNavigationBarCanMove || fullWidth < fullHeight) {
                if (this.mNavigationBarPolicy == null || !this.mNavigationBarPolicy.mMinNavigationBar) {
                    return fullHeight - this.mNavigationBarHeightForRotationMax[rotation];
                }
                return fullHeight - this.mNavigationBarHeightForRotationMin[rotation];
            }
        } else if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId) && getNavigationBarExternal() != null && getNavigationBarExternal().isVisibleLw()) {
            return fullHeight - getNavigationBarHeightExternal();
        } else {
            return fullHeight;
        }
        return fullHeight;
    }

    public void setInputMethodWindowVisible(boolean visible) {
        this.mInputMethodWindowVisible = visible;
    }

    public void setNaviBarFlag(boolean flag) {
        if (flag != this.isNavibarHide) {
            this.isNavibarHide = flag;
            HwSlog.d(TAG, "setNeedHideWindow setFlag isNavibarHide is " + this.isNavibarHide);
        }
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
        super.setInitialDisplaySize(display, width, height, density);
        if (this.mContext != null) {
            initNavigationBarHightExternal(display, width, height);
            Resources res = this.mContext.getResources();
            ContentResolver resolver = this.mContext.getContentResolver();
            int[] iArr = this.mNavigationBarHeightForRotationMax;
            int i = this.mPortraitRotation;
            int dimensionPixelSize = res.getDimensionPixelSize(17105141);
            this.mNavigationBarHeightForRotationMax[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarHeightForRotationMax;
            i = this.mLandscapeRotation;
            dimensionPixelSize = res.getDimensionPixelSize(17105143);
            this.mNavigationBarHeightForRotationMax[this.mSeascapeRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarHeightForRotationMin;
            i = this.mPortraitRotation;
            dimensionPixelSize = System.getInt(resolver, "navigationbar_height_min", 0);
            this.mNavigationBarHeightForRotationMin[this.mSeascapeRotation] = dimensionPixelSize;
            this.mNavigationBarHeightForRotationMin[this.mLandscapeRotation] = dimensionPixelSize;
            this.mNavigationBarHeightForRotationMin[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarWidthForRotationMax;
            i = this.mPortraitRotation;
            dimensionPixelSize = res.getDimensionPixelSize(17105146);
            this.mNavigationBarWidthForRotationMax[this.mSeascapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMax[this.mLandscapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMax[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarWidthForRotationMin;
            i = this.mPortraitRotation;
            dimensionPixelSize = System.getInt(resolver, "navigationbar_width_min", 0);
            this.mNavigationBarWidthForRotationMin[this.mSeascapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMin[this.mLandscapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMin[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
        }
    }

    protected boolean computeNaviBarFlag() {
        boolean z = false;
        LayoutParams focusAttrs = this.mFocusedWindow != null ? this.mFocusedWindow.getAttrs() : null;
        int type = focusAttrs != null ? focusAttrs.type : 0;
        boolean forceNavibar = focusAttrs != null ? (focusAttrs.hwFlags & 1) == 1 : false;
        boolean keyguardOn = type != 2101 ? type == 2100 : true;
        boolean iskeyguardDialog = type == 2009 ? keyguardOn() : false;
        boolean dreamOn = focusAttrs != null && focusAttrs.type == 2023;
        boolean isNeedHideNaviBarWin = (focusAttrs == null || (focusAttrs.privateFlags & FLOATING_MASK) == 0) ? false : true;
        if (this.mStatusBar == this.mFocusedWindow) {
            return false;
        }
        if (iskeyguardDialog && (forceNavibar ^ 1) != 0) {
            return true;
        }
        if (dreamOn) {
            return false;
        }
        if (keyguardOn || isNeedHideNaviBarWin) {
            return true;
        }
        if (this.isNavibarHide) {
            z = this.mInputMethodWindowVisible ^ 1;
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
        if (Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0) {
            return true;
        }
        if (!mIsHwNaviBar) {
            return false;
        }
        if (isLastImmersiveMode()) {
            requestHwTransientBars(this.mStatusBar);
        } else {
            requestTransientStatusBars();
        }
        return true;
    }

    public boolean swipeFromBottom() {
        if (Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0) {
            return true;
        }
        if (!mIsHwNaviBar || !isLastImmersiveMode() || this.mNavigationBar == null || this.mNavigationBarPosition != 0) {
            return false;
        }
        requestHwTransientBars(this.mNavigationBar);
        return true;
    }

    public boolean swipeFromRight() {
        if (!mIsHwNaviBar || !isLastImmersiveMode() || this.mNavigationBar == null || this.mNavigationBarPosition == 0) {
            return false;
        }
        requestHwTransientBars(this.mNavigationBar);
        return true;
    }

    public boolean isGestureIsolated() {
        WindowState win = this.mFocusedWindow != null ? this.mFocusedWindow : this.mTopFullscreenOpaqueWindowState;
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
        boolean z = false;
        try {
            if (this.mFocusedWindow == null) {
                z = this.mTopIsFullscreen;
            } else if ((this.mFocusedWindow.getAttrs().flags & 1024) != 0) {
                z = true;
            }
            return z;
        } catch (NullPointerException e) {
            Log.e(TAG, "isTopIsFullscreen catch null pointer");
            return this.mTopIsFullscreen;
        }
    }

    public boolean okToShowTransientBar() {
        boolean z = false;
        BarController barController = getStatusBarController();
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
        boolean touchDisableModeOpen = System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1, -2) == 1;
        if (this.mCoverOpen && (this.mSensorRegisted ^ 1) != 0 && this.mListener != null && touchDisableModeOpen) {
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
        super.updateSettings();
        updateFingerSenseSettings();
        setFingerSenseState();
        setNaviBarState();
    }

    private void updateFingerSenseSettings() {
        ContentResolver cr = this.mContext.getContentResolver();
        FingerSenseSettings.updateSmartshotEnabled(cr);
        FingerSenseSettings.updateLineGestureEnabled(cr);
        FingerSenseSettings.updateDrawGestureEnabled(cr);
    }

    public void enableScreenAfterBoot() {
        super.enableScreenAfterBoot();
        this.mBooted = true;
        enableSystemWideAfterBoot(this.mContext);
        enableFingerPrintActionsAfterBoot(this.mContext);
        enableStylusAfterBoot(this.mContext);
    }

    public WindowState getFocusedWindow() {
        return this.mFocusedWindow;
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

    private void enableStylusAction() {
        if (this.mStylusGestureListener == null) {
            this.mStylusGestureListener = new StylusGestureListener(this.mContext, this);
            this.mWindowManagerFuncs.registerPointerEventListener(this.mStylusGestureListener);
        }
    }

    public boolean isNavigationBarVisible() {
        return (!this.mHasNavigationBar || this.mNavigationBar == null) ? false : this.mNavigationBar.isVisibleLw();
    }

    protected void enableSystemWideActions() {
        if (SystemProperties.getBoolean("ro.config.finger_joint", false)) {
            Flog.i(1503, "FingerSense enableSystemWideActions");
            if (this.systemWideActionsListener == null) {
                this.systemWideActionsListener = new SystemWideActionsListener(this.mContext, this);
                this.mWindowManagerFuncs.registerPointerEventListener(this.systemWideActionsListener);
                this.systemWideActionsListener.createPointerLocationView();
            }
            SystemProperties.set("persist.sys.fingersense", "1");
            return;
        }
        Flog.i(1503, "Can not enable fingersense, ro.config.finger_joint is set to false");
    }

    protected void disableSystemWideActions() {
        Flog.i(1503, "FingerSense disableSystemWideActions");
        if (this.systemWideActionsListener != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.systemWideActionsListener);
            this.systemWideActionsListener.destroyPointerLocationView();
            this.systemWideActionsListener = null;
        }
        SystemProperties.set("persist.sys.fingersense", "0");
    }

    protected void enableFingerPrintActions() {
        if (this.fingerprintActionsListener == null) {
            this.fingerprintActionsListener = new FingerprintActionsListener(this.mContext, this);
            this.mWindowManagerFuncs.registerPointerEventListener(this.fingerprintActionsListener);
            this.fingerprintActionsListener.createSearchPanelView();
            this.fingerprintActionsListener.createMultiWinArrowView();
        }
    }

    protected void disableFingerPrintActions() {
        if (this.fingerprintActionsListener != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.fingerprintActionsListener);
            this.fingerprintActionsListener.destroySearchPanelView();
            this.fingerprintActionsListener.destroyMultiWinArrowView();
            this.fingerprintActionsListener = null;
        }
    }

    protected void enableFingerPrintActionsAfterBoot(Context context) {
        final ContentResolver resolver = context.getContentResolver();
        this.mHandler.post(new Runnable() {
            public void run() {
                if (FrontFingerPrintSettings.isNaviBarEnabled(resolver)) {
                    HwPhoneWindowManager.this.disableFingerPrintActions();
                } else {
                    HwPhoneWindowManager.this.enableFingerPrintActions();
                }
            }
        });
    }

    protected void setNaviBarState() {
        boolean navibarEnable = FrontFingerPrintSettings.isNaviBarEnabled(this.mContext.getContentResolver());
        if (this.mNavibarEnabled != navibarEnable) {
            int i;
            this.mNavibarEnabled = navibarEnable;
            Handler handler = this.mHandlerEx;
            if (navibarEnable) {
                i = 103;
            } else {
                i = 104;
            }
            handler.sendEmptyMessage(i);
        }
    }

    protected void updateSplitScreenView() {
        if (!((HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) || this.fingerprintActionsListener == null)) {
            this.fingerprintActionsListener.createMultiWinArrowView();
        }
    }

    protected void enableSystemWideAfterBoot(Context context) {
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

    protected void setFingerSenseState() {
        boolean fingersense = FingerSenseSettings.isFingerSenseEnabled(this.mContext.getContentResolver());
        if (this.mFingerSenseEnabled != fingersense) {
            int i;
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
        this.mContext = context;
        ITelephony telephonyService = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        switch (keyCode) {
            case AwareJobSchedulerService.MSG_JOB_EXPIRED /*401*/:
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
                }
                return;
            case AwareJobSchedulerService.MSG_CHECK_JOB /*402*/:
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
                }
                return;
            default:
                return;
        }
    }

    private boolean isTOPActivity(String appnames) {
        try {
            List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks == null || tasks.isEmpty()) {
                return false;
            }
            for (RunningTaskInfo info : tasks) {
                Log.i(TAG, "info.topActivity.getPackageName() is " + info.topActivity.getPackageName());
                if (info.topActivity.getPackageName().equals(appnames) && info.baseActivity.getPackageName().equals(appnames)) {
                    return true;
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
        return (KEYCODE_NOT_FOR_CLOUD.contains(Integer.valueOf(keyCode)) || !HwPCUtils.isPcCastModeInServer()) ? false : isCloudOnPCTOP();
    }

    private boolean isCloudOnPCTOP() {
        try {
            List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks == null || tasks.isEmpty()) {
                return false;
            }
            for (RunningTaskInfo info : tasks) {
                if (info.topActivity == null || info.baseActivity == null) {
                    return false;
                }
                if ("com.huawei.cloud".equals(info.topActivity.getPackageName()) && "com.huawei.cloud".equals(info.baseActivity.getPackageName()) && HwPCUtils.isPcDynamicStack(info.stackId) && "com.huawei.ahdp.session.VmActivity".equals(info.topActivity.getClassName())) {
                    return true;
                }
            }
            return false;
        } catch (RuntimeException e) {
            HwPCUtils.log(TAG, "isCloudOnPCTOP->RuntimeException happened");
        } catch (Exception e2) {
            HwPCUtils.log(TAG, "isCloudOnPCTOP->other exception happened");
        }
    }

    private void notifyVassistantService(String command, int mode, KeyEvent event) {
        Intent intent = new Intent(ACTION_HUAWEI_VASSISTANT_SERVICE);
        intent.putExtra(HUAWEI_VASSISTANT_EXTRA_START_MODE, mode);
        intent.putExtra(SMARTKEY_TAG, command);
        if (event != null) {
            intent.putExtra("KeyEvent", event);
        }
        intent.setPackage(HUAWEI_VASSISTANT_PACKAGE);
        this.mContext.startService(intent);
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
        if (this.mActivityManagerInternal.isSystemReady()) {
            if (this.DEBUG_SMARTKEY) {
                Log.d(TAG, "handleSmartKey keycode = " + keyCode + " down = " + down + " isScreenOn = " + isScreenOn);
            }
            switch (keyCode) {
                case MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE /*308*/:
                    if (!down) {
                        if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", false)) {
                            this.mHandler.postDelayed(this.mCancleInterceptFingerprintEvent, 400);
                        }
                        if (!this.mIsSmartKeyDoubleClick && (this.mIsSmartKeyTripleOrMoreClick ^ 1) != 0 && event.getEventTime() - event.getDownTime() < 500) {
                            cancelSmartKeyLongPressed();
                            sendSmartKeyEvent(SMARTKEY_CLICK);
                            break;
                        }
                        cancelSmartKeyLongPressed();
                        break;
                    }
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
                    if (timediff < 400) {
                        if (!this.mIsSmartKeyDoubleClick && (this.mIsSmartKeyTripleOrMoreClick ^ 1) != 0) {
                            cancelSmartKeyClick();
                            cancelSmartKeyLongPressed();
                            sendSmartKeyEvent(SMARTKEY_DCLICK);
                            this.mIsSmartKeyDoubleClick = true;
                            break;
                        }
                        this.mIsSmartKeyTripleOrMoreClick = true;
                        this.mIsSmartKeyDoubleClick = false;
                        break;
                    }
                    this.mIsSmartKeyTripleOrMoreClick = false;
                    this.mIsSmartKeyDoubleClick = false;
                    break;
                    break;
            }
            return;
        }
        Log.d(TAG, "System is not ready, just discard it this time.");
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

    private void cancelSmartKeyClick() {
        this.mHandler.removeCallbacks(this.mSmartKeyClick);
    }

    private void cancelSmartKeyLongPressed() {
        this.mHandler.removeCallbacks(this.mSmartKeyLongPressed);
    }

    private void notifySmartKeyEvent(String strType) {
        Intent intent = new Intent(HUAWEI_SMARTKEY_PACKAGE);
        intent.setFlags(268435456);
        intent.putExtra(SMARTKEY_TAG, strType);
        this.mContext.sendBroadcast(intent);
        Log.i(TAG, "send smart key " + strType);
        if ((!this.mIsProximity && this.mSensorRegisted) || (this.mSensorRegisted ^ 1) != 0 || (isPhoneInCall() && SMARTKEY_LP.equals(strType))) {
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
        return ((ActivityManagerService) ServiceManager.getService("activity")).topAppName();
    }

    private void initDropSmartKey() {
        String dropSmartKeyActivity = Systemex.getString(this.mResolver, DROP_SMARTKEY_ACTIVITY);
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
        return pkgName != null ? pkgName.startsWith("com.huawei.camera") : false;
    }

    public boolean isKeyguardShortcutApps() {
        String topActivity = getTopActivity();
        if (topActivity == null) {
            return false;
        }
        for (String startsWith : this.mKeyguardShortcutApps) {
            if (topActivity.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLsKeyguardShortcutApps() {
        String topActivity = getTopActivity();
        if (topActivity == null) {
            return false;
        }
        for (String startsWith : this.mLsKeyguardShortcutApps) {
            if (topActivity.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInCallUIAndRinging() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        return telecomManager != null ? telecomManager.isRinging() : false;
    }

    private boolean isAlarm(int user) {
        return ((HwActivityManagerService) ServiceManager.getService("activity")).serviceIsRunning(ComponentName.unflattenFromString("com.android.deskclock/.alarmclock.AlarmKlaxon"), user);
    }

    public void waitKeyguardDismissDone(KeyguardDismissDoneListener listener) {
        synchronized (this.mLock) {
            this.mKeyguardDismissListener = listener;
        }
        this.mWindowManagerInternal.waitForKeyguardDismissDone(this.mKeyguardDismissDoneCallback, 300);
    }

    public void cancelWaitKeyguardDismissDone() {
        synchronized (this.mLock) {
            this.mKeyguardDismissListener = null;
        }
    }

    protected void finishKeyguardDismissDone() {
        KeyguardDismissDoneListener listener;
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
        boolean isScreenLocked = false;
        try {
            return ActivityManagerNative.getDefault().isInLockTaskMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isScreenInLockTaskMode  ", e);
            return isScreenLocked;
        }
    }

    public boolean isStatusBarObsecured() {
        return this.mStatuBarObsecured;
    }

    /* JADX WARNING: Missing block: B:4:0x0007, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isStatusBarObsecuredByWin(WindowState win) {
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
        super.adjustConfigurationLw(config, keyboardPresence, navigationPresence);
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
        super.setRotationLw(rotation);
        Log.d(TAG, "PhoneWindowManager setRotationLw(" + rotation + ")");
        IHwAftPolicyService hwAft = HwAftPolicyManager.getService();
        if (hwAft != null) {
            try {
                hwAft.notifyOrientationChange(rotation);
            } catch (RemoteException e) {
                Log.e(TAG, "setRotationLw throw " + e);
            }
        }
    }

    public boolean performHapticFeedbackLw(WindowState win, int effectId, boolean always) {
        if (effectId != 1 || !this.isVibrateImplemented || (always ^ 1) == 0) {
            return super.performHapticFeedbackLw(win, effectId, always);
        }
        if (1 != System.getInt(this.mContext.getContentResolver(), "touch_vibrate_mode", 1)) {
            return false;
        }
        HwGeneralManager.getInstance().playIvtEffect("VIRTUAL_KEY");
        return true;
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
            return telephonyService.isIdleForSubscriber(0, this.mContext.getPackageName()) ? telephonyService.isIdleForSubscriber(1, this.mContext.getPackageName()) : false;
        } catch (RemoteException ex) {
            Log.w(TAG, "ITelephony threw RemoteException", ex);
            return false;
        }
    }

    public int getDisabledKeyEventResult(int keyCode) {
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
            case 187:
                if (!HwDeviceManager.disallowOp(15)) {
                    return -2;
                }
                Log.i(TAG, "the device's task key has been disabled for the user.");
                return 0;
            default:
                return -2;
        }
    }

    private int getGameControlKeyReslut(KeyEvent event) {
        int result = -2;
        if (!mSupportGameAssist || this.mAms == null) {
            return -2;
        }
        int keyCode = event.getKeyCode();
        boolean isGameKeyControlOn = (this.mEnableKeyInCurrentFgGameApp || !this.mAms.isGameKeyControlOn()) ? this.mLastKeyDownDropped : true;
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
        boolean z = false;
        int result = -2;
        boolean isSingleKeyMode = naviMode == 0 || naviMode == 2;
        int keyCode = event.getKeyCode();
        long keyTime = event.getEventTime();
        boolean isKeyDown = event.getAction() == 0;
        if (this.mLastKeyDownDropped && keyTime - this.mLastKeyDownTime > 300) {
            this.mLastKeyDownDropped = false;
        }
        switch (keyCode) {
            case 3:
                if (!isSingleKeyMode) {
                    if (!isKeyDown) {
                        result = getClickResult(keyTime, keyCode);
                        break;
                    }
                }
                return -2;
                break;
            case 4:
                if (isKeyDown) {
                    result = getClickResult(keyTime, keyCode);
                    if (result != -2) {
                        z = true;
                    }
                    this.mLastKeyDownDropped = z;
                    break;
                } else if (this.mLastKeyDownDropped) {
                    Log.d(TAG, "drop key up for last event beacause down dropped.");
                    this.mLastKeyDownDropped = false;
                    return -1;
                }
                break;
            case 187:
                if (!isSingleKeyMode) {
                    if (!isKeyDown) {
                        result = getClickResult(keyTime, keyCode);
                        this.mHandlerEx.removeMessages(MSG_TRIKEY_RECENT_LONG_PRESS);
                        break;
                    }
                }
                return -1;
                break;
        }
        return result;
    }

    private int getClickResult(long eventTime, int keyCode) {
        int result;
        if (eventTime - this.mLastKeyDownTime >= 300 || this.mLastKeyDownKeyCode != keyCode || this.mLastKeyDownTime - this.mSecondToLastKeyDownTime >= 300 || this.mSecondToLastKeyDownKeyCode != this.mLastKeyDownKeyCode) {
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
                Toast toast = Toast.makeText(HwPhoneWindowManager.this.mContext, 33686069, 0);
                toast.getWindowParams().type = 2010;
                LayoutParams windowParams = toast.getWindowParams();
                windowParams.privateFlags |= 16;
                toast.show();
            }
        });
    }

    public int[] getTouchCountInfo() {
        return this.mTouchCountPolicy.getTouchCountInfo();
    }

    public int[] getDefaultTouchCountInfo() {
        return this.mTouchCountPolicy.getDefaultTouchCountInfo();
    }

    private boolean isTrikeyNaviKeycodeFromLON(boolean isInjected, boolean excluded) {
        int frontFpNaviTriKey = FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
        Log.d(TAG, "frontFpNaviTriKey:" + frontFpNaviTriKey + " isInjected:" + isInjected + " mTrikeyNaviMode:" + this.mTrikeyNaviMode + " excluded:" + excluded);
        return (frontFpNaviTriKey == 0 || (!isInjected && this.mTrikeyNaviMode < 0)) ? true : excluded;
    }

    public boolean isSupportCover() {
        String GSETTINGS_COVER_ENABLED = "cover_enabled";
        if (Global.getInt(this.mContext.getContentResolver(), "cover_enabled", 1) != 0) {
            return true;
        }
        return false;
    }

    public boolean isSmartCoverMode() {
        String SETTINGS_COVER_TYPE = "cover_type";
        if (Global.getInt(this.mContext.getContentResolver(), "cover_type", 0) == 1) {
            return true;
        }
        return false;
    }

    public boolean isInCallActivity() {
        String pkgName = getTopActivity();
        return pkgName != null ? pkgName.startsWith("com.android.incallui") : false;
    }

    public boolean isInterceptAndCheckRinging(boolean isIntercept) {
        TelecomManager telecomManager = getTelecommService();
        if (isIntercept) {
            int isRinging;
            if (telecomManager != null) {
                isRinging = telecomManager.isRinging();
            } else {
                isRinging = 0;
            }
            if ((isRinging ^ 1) != 0) {
                return true;
            }
        }
        return false;
    }

    public int getSingAppKeyEventResult(int keyCode) {
        String packageName = HwDeviceManager.getString(34);
        if (packageName == null || (packageName.isEmpty() ^ 1) == 0) {
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
        boolean[] results = new boolean[]{false, false};
        if (activityManager != null) {
            try {
                List<RunningTaskInfo> runningTask = activityManager.getRunningTasks(2);
                if (runningTask != null && runningTask.size() > 0) {
                    ComponentName cn = ((RunningTaskInfo) runningTask.get(0)).topActivity;
                    if (cn != null) {
                        String currentAppName = cn.getPackageName();
                        String currentActivityName = cn.getClassName();
                        PackageManager pm = this.mContext.getPackageManager();
                        String mainClassName = pm.getLaunchIntentForPackage(packageName).resolveActivity(pm).getClassName();
                        if (mainClassName != null && mainClassName.equals(currentActivityName) && packageName.equals(currentAppName)) {
                            results[0] = true;
                        }
                    }
                    Object nextAppName = null;
                    if (runningTask.size() > 1) {
                        nextAppName = ((RunningTaskInfo) runningTask.get(1)).topActivity.getPackageName();
                    }
                    if ((((RunningTaskInfo) runningTask.get(0)).numActivities <= 1 && (runningTask.size() <= 1 || (packageName.equals(nextAppName) ^ 1) != 0)) || "com.android.systemui".equals(nextAppName)) {
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

    protected void notifyPowerkeyInteractive(boolean bool) {
        AwareFakeActivityRecg.self().notifyPowerkeyInteractive(true);
    }

    public void setNavigationBarExternal(WindowState state) {
        this.mNavigationBarExternal = state;
    }

    public WindowState getNavigationBarExternal() {
        return this.mNavigationBarExternal;
    }

    public void removeWindowLw(WindowState win) {
        super.removeWindowLw(win);
        if (HwPCUtils.enabled() && getNavigationBarExternal() == win) {
            setNavigationBarExternal(null);
            this.mNavigationBarControllerExternal.setWindow(null);
        }
    }

    public int getConfigDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode, int displayId) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            return getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayId);
        }
        return super.getConfigDisplayHeight(fullWidth, fullHeight, rotation, uiMode, displayId);
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        super.dump(prefix, pw, args);
        if (HwPCUtils.isPcCastModeInServer() && getNavigationBarExternal() != null) {
            pw.print(prefix);
            pw.print("mNavigationBarExternal=");
            pw.println(getNavigationBarExternal());
        }
        if (HwPCUtils.isPcCastModeInServer() && this.mNavigationBarControllerExternal != null) {
            this.mNavigationBarControllerExternal.dump(pw, prefix);
        }
    }

    public int focusChangedLw(WindowState lastFocus, WindowState newFocus) {
        if (HwPCUtils.isPcCastModeInServer() && newFocus != null && HwPCUtils.isValidExtDisplayId(newFocus.getDisplayId())) {
            return 1;
        }
        if (this.mFalseTouchMonitor != null) {
            this.mFalseTouchMonitor.handleFocusChanged(lastFocus, newFocus);
        }
        return super.focusChangedLw(lastFocus, newFocus);
    }

    public void layoutWindowLw(WindowState win, WindowState attached) {
        if (!HwPCUtils.isPcCastModeInServer() || win != getNavigationBarExternal()) {
            super.layoutWindowLw(win, attached);
        }
    }

    public void beginLayoutLw(boolean isDefaultDisplay, int displayWidth, int displayHeight, int displayRotation, int uiMode, int displayId) {
        super.beginLayoutLw(isDefaultDisplay, displayWidth, displayHeight, displayRotation, uiMode, displayId);
        if (HwPCUtils.isPcCastModeInServer() && (isDefaultDisplay ^ 1) != 0 && getNavigationBarExternal() != null && HwPCUtils.isValidExtDisplayId(displayId)) {
            if (this.mNavigationBarHeightExternal == 0 && this.mContext != null) {
                initNavigationBarHightExternal(((DisplayManager) this.mContext.getSystemService("display")).getDisplay(displayId), displayWidth, displayHeight);
            }
            Rect pf = mTmpParentFrame;
            Rect df = mTmpDisplayFrame;
            Rect of = mTmpOverscanFrame;
            Rect vf = mTmpVisibleFrame;
            Rect dcf = mTmpDecorFrame;
            int i = this.mDockLeft;
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
            layoutNavigationBarExternal(displayWidth, displayHeight, displayRotation, uiMode, 0, 0, 0, dcf, true, false, false, false, false);
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
            vf.left = this.mStableLeft;
            vf.top = this.mStableTop;
            vf.right = this.mStableRight;
            vf.bottom = this.mStableBottom;
            if (HwPCUtils.enabledInPad()) {
                layoutStatusBarExternal(pf, df, of, vf, dcf);
            }
        }
    }

    private boolean layoutStatusBarExternal(Rect pf, Rect df, Rect of, Rect vf, Rect dcf) {
        if (this.mStatusBar != null) {
            int i = this.mUnrestrictedScreenLeft;
            of.left = i;
            df.left = i;
            pf.left = i;
            i = this.mUnrestrictedScreenWidth + this.mUnrestrictedScreenLeft;
            of.right = i;
            df.right = i;
            pf.right = i;
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
            this.mStatusBar.computeFrameLw(pf, df, vf, vf, vf, dcf, vf, vf);
        }
        return false;
    }

    protected boolean layoutNavigationBarExternal(int displayWidth, int displayHeight, int displayRotation, int uiMode, int overscanLeft, int overscanRight, int overscanBottom, Rect dcf, boolean navVisible, boolean navTranslucent, boolean navAllowedHidden, boolean isKeyguardOn, boolean statusBarExpandedNotKeyguard) {
        this.mNavigationBarPosition = 0;
        mTmpNavigationFrame.set(0, (displayHeight - overscanBottom) - getNavigationBarHeightExternal(), displayWidth, displayHeight - overscanBottom);
        int i = mTmpNavigationFrame.top;
        this.mStableFullscreenBottom = i;
        this.mStableBottom = i;
        this.mNavigationBarControllerExternal.setBarShowingLw(true);
        this.mDockBottom = this.mStableBottom;
        this.mRestrictedScreenHeight = this.mDockBottom - this.mRestrictedScreenTop;
        this.mRestrictedOverscanScreenHeight = this.mDockBottom - this.mRestrictedOverscanScreenTop;
        this.mSystemBottom = this.mStableBottom;
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
        getNavigationBarExternal().computeFrameLw(mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, dcf, mTmpNavigationFrame, mTmpNavigationFrame);
        return false;
    }

    protected int getNavigationBarHeightExternal() {
        return this.mNavigationBarHeightExternal;
    }

    public int prepareAddWindowLw(WindowState win, LayoutParams attrs) {
        if (!HwPCUtils.isPcCastModeInServer() || attrs.type != 2019 || !HwPCUtils.isValidExtDisplayId(win.getDisplayId())) {
            return super.prepareAddWindowLw(win, attrs);
        }
        if (getNavigationBarExternal() != null && getNavigationBarExternal().isAlive()) {
            return -7;
        }
        setNavigationBarExternal(win);
        this.mNavigationBarControllerExternal.setWindow(win);
        return 0;
    }

    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets, int displayId) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            outInsets.setEmpty();
            getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, outInsets, displayId);
            outInsets.top = 0;
            return;
        }
        getStableInsetsLw(displayRotation, displayWidth, displayHeight, outInsets);
    }

    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight, Rect outInsets, int displayId) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
            outInsets.setEmpty();
            if (this.mHasNavigationBar) {
                if (getNavigationBarExternal() == null || !getNavigationBarExternal().isVisibleLw()) {
                    outInsets.bottom = 0;
                } else {
                    outInsets.bottom = getNavigationBarHeightExternal();
                }
            }
            return;
        }
        getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, outInsets);
    }

    public void showTopBar() {
        if (HwPCUtils.enabled()) {
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
        if (!HwPCUtils.isValidExtDisplayId(displayId) && (HwPCUtils.enabledInPad() ^ 1) != 0) {
            this.isHomePressDown = false;
            this.isHomeAndEBothDown = false;
            this.isHomeAndLBothDown = false;
            this.isHomeAndDBothDown = false;
            return false;
        } else if (HwPCUtils.enabledInPad() && handleExclusiveKeykoard(event).booleanValue()) {
            return true;
        } else {
            if (keyCode == WifiProCHRManager.WIFI_PORTAL_SAMPLES_COLLECTE && down && repeatCount == 0) {
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
                lockScreen();
                return true;
            } else if (keyCode == 32 && down && this.isHomePressDown && repeatCount == 0) {
                this.isHomeAndDBothDown = true;
                toggleHome();
                return true;
            } else {
                if (down && repeatCount == 0 && keyCode == 61) {
                    if (this.mRecentAppsHeldModifiers == 0 && (keyguardOn() ^ 1) != 0 && isUserSetupComplete()) {
                        int shiftlessModifiers = event.getModifiers() & -194;
                        if (KeyEvent.metaStateHasModifiers(shiftlessModifiers, 2)) {
                            this.mRecentAppsHeldModifiers = shiftlessModifiers;
                            triggerSwitchTaskView(true);
                            return true;
                        }
                    }
                } else if (!(down || this.mRecentAppsHeldModifiers == 0 || (event.getMetaState() & this.mRecentAppsHeldModifiers) != 0)) {
                    this.mRecentAppsHeldModifiers = 0;
                    triggerSwitchTaskView(false);
                }
                if ((HwPCUtils.enabledInPad() || keyCode != 3) && keyCode != CPUFeature.MSG_RESET_TOP_APP_CPUSET && keyCode != 118) {
                    return false;
                }
                if (down) {
                    this.isHomePressDown = true;
                } else {
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
                }
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
                return Boolean.valueOf(true);
            case 4:
                if (down && repeatCount == 0) {
                    dispatchKeyEventForExclusiveKeyboard(event);
                    return Boolean.valueOf(false);
                }
            case 62:
                if (down && event.isShiftPressed()) {
                    dispatchKeyEventForExclusiveKeyboard(event);
                    return Boolean.valueOf(true);
                }
            case 118:
                if (down) {
                    dispatchKeyEventForExclusiveKeyboard(event);
                }
                return Boolean.valueOf(true);
            case 187:
                if (isScreenLocked()) {
                    HwPCUtils.log(TAG, "ScreenLocked! Not handle" + event);
                    return Boolean.valueOf(true);
                }
                dispatchKeyEventForExclusiveKeyboard(event);
                return Boolean.valueOf(true);
            case 220:
            case 221:
                if (!isScreenLocked()) {
                    dispatchKeyEventForExclusiveKeyboard(event);
                    break;
                }
                HwPCUtils.log(TAG, "ScreenLocked! Not handle" + event);
                return Boolean.valueOf(true);
            default:
                return Boolean.valueOf(false);
        }
        return Boolean.valueOf(false);
    }

    public void overrideRectForForceRotation(WindowState win, Rect pf, Rect df, Rect of, Rect cf, Rect vf, Rect dcf) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (forceRotationManager.isForceRotationSupported() && forceRotationManager.isForceRotationSwitchOpen(this.mContext) && win != null && win.getAppToken() != null && win.getAttrs() != null) {
            String winTitle = String.valueOf(win.getAttrs().getTitle());
            if (!TextUtils.isEmpty(winTitle) && !winTitle.startsWith("SurfaceView") && !winTitle.startsWith("PopupWindow")) {
                if (win.isInMultiWindowMode()) {
                    Slog.v(TAG, "window is in multiwindow mode");
                    return;
                }
                Display defDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                DisplayMetrics dm = new DisplayMetrics();
                defDisplay.getMetrics(dm);
                if (dm.widthPixels >= dm.heightPixels) {
                    Rect tmpRect = new Rect(vf);
                    if (forceRotationManager.isAppForceLandRotatable(win.getAttrs().packageName, win.getAppToken().asBinder())) {
                        forceRotationManager.applyForceRotationLayout(win.getAppToken().asBinder(), tmpRect);
                        if (!tmpRect.equals(vf)) {
                            int i = tmpRect.left;
                            vf.left = i;
                            cf.left = i;
                            df.left = i;
                            pf.left = i;
                            dcf.left = i;
                            of.left = i;
                            i = tmpRect.right;
                            vf.right = i;
                            cf.right = i;
                            df.right = i;
                            pf.right = i;
                            dcf.right = i;
                            of.right = i;
                        }
                        LayoutParams attrs = win.getAttrs();
                        attrs.privateFlags |= 64;
                    }
                }
            }
        }
    }

    private void initNavigationBarHightExternal(Display display, int width, int height) {
        if (display == null || this.mContext == null) {
            Log.e(TAG, "fail to ini nav, display or context is null");
            return;
        }
        if (HwPCUtils.enabled() && HwPCUtils.isValidExtDisplayId(display.getDisplayId())) {
            this.mNavigationBarHeightExternal = this.mContext.createDisplayContext(display).getResources().getDimensionPixelSize(34472195);
            if (this.mExternalSystemGestures != null) {
                this.mExternalSystemGestures.screenHeight = height;
                this.mExternalSystemGestures.screenWidth = width;
            }
        }
    }

    public void resetCurrentNaviBarHeightExternal() {
        HwPCUtils.log(TAG, "resetCurrentNaviBarHeightExternal");
        if (HwPCUtils.enabled() && this.mNavigationBarHeightExternal != 0) {
            this.mNavigationBarHeightExternal = 0;
        }
    }

    public void layoutStatusBarForNotch(Rect pf, Rect df, Rect of) {
        boolean z = false;
        if (IS_NOTCH_PROP) {
            if (!(this.mFocusedWindow == null || (this.mFocusedWindow.getAttrs().hwFlags & 16384) == 0)) {
                z = true;
            }
            this.isMMITestDefaultShap = z;
            if (!this.isMMITestDefaultShap) {
                int i;
                if (this.mDisplayRotation == 1) {
                    i = this.mUnrestrictedScreenLeft + this.mNotchPropSize;
                    of.left = i;
                    df.left = i;
                    pf.left = i;
                } else if (this.mDisplayRotation == 3) {
                    i = (this.mUnrestrictedScreenWidth + this.mUnrestrictedScreenLeft) - this.mNotchPropSize;
                    of.right = i;
                    df.right = i;
                    pf.right = i;
                }
            }
        }
    }

    public void layoutWindowLwForNotch(WindowState win, WindowState attached, LayoutParams attrs, int fl, Rect pf, Rect df, Rect of, Rect cf) {
        if (IS_NOTCH_PROP) {
            boolean isNotchSupport;
            boolean isWallpaper = attrs.type == 2013;
            boolean z = attrs.type >= 1 ? attrs.type <= 99 : false;
            this.isAppWindow = z;
            boolean isApplicationStarting = attrs.type == 3;
            boolean isIME = attrs.type == 2011;
            boolean isHwFullScreenWindow = win.toString().contains("SetFullScreenWindow");
            boolean isPointerLocation = win.toString().contains("PointerLocation");
            boolean isTypeDream = attrs.type == 2023;
            boolean isPortraitShow = (this.isMMITestDefaultShap || !this.isAppWindow || (isWallpaper ^ 1) == 0 || (isApplicationStarting ^ 1) == 0 || !this.isChanged) ? false : attached == null;
            int sysUiFl = PolicyControl.getSystemUiVisibility(win, null);
            boolean isHwSDK = win.toString().contains("com.huawei.hms.game");
            if ((win.getAttrs().hwFlags & 65536) == 0) {
                isNotchSupport = win.getHwNotchSupport();
            } else {
                isNotchSupport = true;
            }
            boolean isNotchSupportDefault;
            if (!isNotchSupport || this.isNotchSwitchOpen) {
                isNotchSupportDefault = this.hwNotchScreenWhiteConfig.notchSupportWindow(attrs) ^ 1;
            } else {
                isNotchSupportDefault = false;
            }
            notchControlFillet(isNotchSupport, win);
            int i;
            switch (this.mDisplayRotation) {
                case 1:
                    if (!(this.isMMITestDefaultShap || (this.isOnlyOnce ^ 1) == 0 || (isHwFullScreenWindow ^ 1) == 0 || attached != null || (isPointerLocation ^ 1) == 0 || !isNotchSupportDefault)) {
                        pf.left += this.mNotchPropSize;
                        if (win.toString().contains("com.sina.weibo/com.sina.weibo.video.feed.VideoFeedActivity") || win.toString().contains("com.android.deskclock/com.android.deskclock.alarmclock.LockAlarmFullActivity")) {
                            df.left = pf.left;
                        }
                        adjustForYouTuBeDodge(win, pf);
                    }
                    this.isOnlyOnce ^= 1;
                    return;
                case 2:
                    if (isPortraitShow && (isHwFullScreenWindow ^ 1) != 0 && (isNotchSupport ^ 1) != 0) {
                        i = cf.bottom - this.mNotchPropSize;
                        cf.bottom = i;
                        of.bottom = i;
                        df.bottom = i;
                        pf.bottom = i;
                        this.isChanged = false;
                        return;
                    }
                    return;
                case 3:
                    if (!this.isMMITestDefaultShap && (this.isOnlyOnce ^ 1) != 0 && isNotchSupportDefault) {
                        adjustForYouTuBeDodge(win, pf);
                        if ((this.isAppWindow || isIME) && (isHwFullScreenWindow ^ 1) != 0) {
                            if (!(this.mNavigationBar == null || (this.mNavigationBar.isVisibleLw() ^ 1) == 0) || pf.right > this.mStableRight) {
                                pf.right -= this.mNotchPropSize;
                                if ((fl & 512) == 0 || attrs.type == 2010 || (win.isInMultiWindowMode() && (win.toString().contains("com.huawei.android.launcher") ^ 1) != 0)) {
                                    df.right -= this.mNotchPropSize;
                                }
                            }
                        } else if (isWallpaper) {
                            i = of.right - this.mNotchPropSize;
                            of.right = i;
                            df.right = i;
                            pf.right = i;
                        } else if (attached == null && (isTypeDream ^ 1) != 0) {
                            pf.right -= this.mNotchPropSize;
                        }
                        this.isOnlyOnce ^= 1;
                        return;
                    }
                    return;
                default:
                    if (this.isNotchSwitchOpen && (isHwFullScreenWindow ^ 1) != 0 && (isNotchSupport ^ 1) != 0) {
                        notchSwitchOpen(win, attached, attrs, sysUiFl, fl, pf, df, of, cf);
                        return;
                    } else if ((isPortraitShow && (isHwFullScreenWindow ^ 1) != 0 && (isNotchSupport ^ 1) != 0) || isHwSDK) {
                        i = this.mStableTop;
                        cf.top = i;
                        of.top = i;
                        df.top = i;
                        pf.top = i;
                        this.isChanged = false;
                        return;
                    } else {
                        return;
                    }
            }
        }
    }

    private void notchControlFillet(boolean isNotchSupport, WindowState win) {
        if (!this.isNotchSwitchOpen) {
            if (this.mDisplayRotation == 1 || this.mDisplayRotation == 3) {
                boolean splashScreen = win.toString().contains("Splash Screen");
                this.mFirstSetCornerInPort = true;
                if (!splashScreen && this.isAppWindow && isNotchSupport && this.mFirstSetCornerInLandNoNotch) {
                    this.mFirstSetCornerInLandNoNotch = false;
                    this.mFirstSetCornerInLandNotch = true;
                    transferSwitchStatusToSurfaceFlinger(0);
                    return;
                } else if (!splashScreen && this.isAppWindow && (isNotchSupport ^ 1) != 0 && this.mFirstSetCornerInLandNotch) {
                    if (this.mFirstSetCornerInLandNoNotch || (win.toString().contains("com.huawei.intelligent.Workspace") ^ 1) != 0) {
                        this.mFirstSetCornerInLandNotch = false;
                        this.mFirstSetCornerInLandNoNotch = true;
                        transferSwitchStatusToSurfaceFlinger(1);
                        return;
                    }
                    return;
                } else {
                    return;
                }
            }
            this.mFirstSetCornerInLandNoNotch = true;
            this.mFirstSetCornerInLandNotch = true;
            if (this.mFirstSetCornerInPort) {
                this.mFirstSetCornerInPort = false;
                transferSwitchStatusToSurfaceFlinger(0);
            }
        }
    }

    public static void transferSwitchStatusToSurfaceFlinger(final int value) {
        int val = value;
        new Thread(new Runnable() {
            public void run() {
                Parcel dataIn = Parcel.obtain();
                try {
                    IBinder sfBinder = ServiceManager.getService("SurfaceFlinger");
                    dataIn.writeInt(value);
                    if (!(sfBinder == null || (sfBinder.transact(HwPhoneWindowManager.NOTCH_ROUND_CORNER_CODE, dataIn, null, 1) ^ 1) == 0)) {
                        Slog.d(HwPhoneWindowManager.TAG, "transferSwitchStatusToSurfaceFlinger error!");
                    }
                    dataIn.recycle();
                } catch (RemoteException e) {
                    Slog.d(HwPhoneWindowManager.TAG, "transferSwitchStatusToSurfaceFlinger RemoteException on notify screen rotation animation end");
                    dataIn.recycle();
                } catch (Throwable th) {
                    dataIn.recycle();
                    throw th;
                }
            }
        }).start();
    }

    private void notchSwitchOpen(WindowState win, WindowState attached, LayoutParams attrs, int sysUiFl, int fl, Rect pf, Rect df, Rect of, Rect cf) {
        boolean isSnapshotStartingWindow = win.toString().contains("SnapshotStartingWindow");
        boolean isLauncher = win.toString().contains("com.huawei.android.launcher");
        boolean isNotchFullScreenWindow = !this.hwNotchScreenWhiteConfig.isNotchAppHideInfo(win) ? !this.hwNotchScreenWhiteConfig.isNoneNotchAppHideInfo(win) ? this.isAppWindow && !((fl & 1024) == 0 && (sysUiFl & 4) == 0) : true : false;
        boolean isHwSDK = win.toString().contains("com.huawei.hms.game.welcome");
        if (isSnapshotStartingWindow && this.isSwitchNotch) {
            cf.top = 0;
            of.top = 0;
            df.top = 0;
            pf.top = 0;
        } else if (((!isLauncher && isNotchFullScreenWindow) || isHwSDK) && this.isSwitchNotch) {
            int i = this.mNotchPropSize;
            cf.top = i;
            of.top = i;
            df.top = i;
            pf.top = i;
        }
        this.isSwitchNotch ^= 1;
    }

    public void adjustWindowParamsLwForNotch(WindowState win, LayoutParams attrs) {
        if (IS_NOTCH_PROP) {
            boolean z = this.mFocusedWindow != null ? (this.mFocusedWindow.getAttrs().hwFlags & 16384) != 0 : false;
            this.isMMITestDefaultShap = z;
            this.isChanged = false;
            int sysUiFl = PolicyControl.getSystemUiVisibility(win, null);
            this.isSystemApp = this.hwNotchScreenWhiteConfig.isSystemAppInfo(win.toString());
            boolean isPortrait = this.mDisplayRotation == 0 || this.mDisplayRotation == 2;
            this.isOnlyOnce = isPortrait;
            boolean isApplicationStarting = attrs.type == 3;
            boolean isAppWindow = attrs.type >= 1 ? attrs.type <= 99 : false;
            int isKeyguardShowing = !isKeyguardShowingOrOccluded() ? this.mKeyguardDelegate == null ? 0 : this.mKeyguardDelegate.isOccluded() : 1;
            this.isSwitchNotch = this.isNotchSwitchOpen;
            if (this.hwNotchScreenWhiteConfig.isNotchAppInfo(win)) {
                this.isChanged = false;
            } else if (!this.isMMITestDefaultShap && isPortrait && this.hwNotchScreenWhiteConfig.isNoneNotchAppInfo(win)) {
                this.isChanged = true;
            } else if ((attrs.hwFlags & 32768) == 0 || (this.isSystemApp ^ 1) == 0 || !isAppWindow || (isKeyguardShowing ^ 1) == 0) {
                if (win.toString().contains("SnapshotStartingWindow")) {
                    this.isChanged = false;
                } else if ((attrs.flags & -2080373760) != 0 && isPortrait && (this.isSystemApp ^ 1) != 0 && (isApplicationStarting ^ 1) != 0 && (this.isMMITestDefaultShap ^ 1) != 0) {
                    if ((sysUiFl & 4) != 0) {
                        this.isChanged = true;
                    } else if ((sysUiFl & 7168) == 0) {
                        this.isChanged = true;
                    } else if ((attrs.flags & 1024) == 0) {
                        this.isChanged = false;
                    } else {
                        this.isChanged = true;
                    }
                }
            } else if ((attrs.flags & 1024) != 0 || (sysUiFl & 4) != 0) {
                this.isChanged = true;
            }
        }
    }

    public boolean isIntelliServiceEnabledFR(int orientatin) {
        return IntelliServiceManager.isIntelliServiceEnabled(this.mContext, orientatin, this.mCurrentUserId);
    }

    public int getRotationFromSensorOrFaceFR(int orientation, int lastRotation) {
        int sensorRotation;
        if (!IntelliServiceManager.isIntelliServiceEnabled(this.mContext, orientation, this.mCurrentUserId)) {
            Slog.d(TAG, "face_rotation: not enabled,use sensor real");
            sensorRotation = getRotationFromRealSensorFR(lastRotation);
        } else if (IntelliServiceManager.getInstance(this.mContext).isKeepPortrait()) {
            Slog.d(TAG, "face_rotation: use rotation_0");
            sensorRotation = 0;
        } else if (IntelliServiceManager.getInstance(this.mContext).getFaceRotaion() != -2) {
            Slog.d(TAG, "face_rotation: use face_rotation");
            sensorRotation = IntelliServiceManager.getInstance(this.mContext).getFaceRotaion();
        } else {
            Slog.d(TAG, "face_rotation: time out use real sensor");
            sensorRotation = getRotationFromRealSensorFR(lastRotation);
        }
        Slog.d(TAG, "face_rotation: rotation = " + sensorRotation);
        return sensorRotation;
    }

    public int getRotationFromRealSensorFR(int lastRotation) {
        int sensorRotation = this.mOrientationListener.getProposedRotation();
        Slog.d(TAG, "face_rotation:real sensor rotation = " + sensorRotation);
        if (sensorRotation < 0) {
            return lastRotation;
        }
        return sensorRotation;
    }

    public void setSensorRotationFR(int rotation) {
        IntelliServiceManager.setSensorRotation(rotation);
    }

    public void startIntelliServiceFR() {
        IntelliServiceManager.getInstance(this.mContext).startIntelliService(this.mFaceRotationCallback);
    }

    public void setSwitchingUser(boolean switching) {
        if (switching) {
            Slog.d(TAG, "face_rotation: switchUser unbindIntelliService");
            IntelliServiceManager.getInstance(this.mContext).unbindIntelliService();
        }
        super.setSwitchingUser(switching);
    }

    public void layoutWindowForPadPCMode(WindowState win, Rect pf, Rect df, Rect cf, Rect vf, int mContentBottom) {
        HwPCMultiWindowPolicy.layoutWindowForPadPCMode(win, pf, df, cf, vf, mContentBottom);
    }

    public boolean hideNotchStatusBar(int fl) {
        boolean hideNotchStatusBar = true;
        this.mBarVisibility = 1;
        boolean isNotchSupport = this.mFocusedWindow != null ? (this.mFocusedWindow.getAttrs().hwFlags & 65536) == 0 ? this.mFocusedWindow.getHwNotchSupport() : true : false;
        if (!IS_NOTCH_PROP || this.mDisplayRotation != 0) {
            return true;
        }
        int i;
        if (this.mFocusedWindow == null) {
            i = 0;
        } else if (this.mFocusedWindow.toString().contains("com.huawei.intelligent")) {
            i = 1;
        } else {
            i = this.mFocusedWindow.toString().contains("com.huawei.android.launcher");
        }
        if ((i ^ 1) == 0) {
            return true;
        }
        if (this.isNotchSwitchOpen) {
            boolean statusBarFocused = this.mFocusedWindow != null ? (PolicyControl.getWindowFlags(null, this.mFocusedWindow.getAttrs()) & 2048) != 0 : false;
            if (!((fl & 1024) == 0 && (this.mLastSystemUiFlags & 4) == 0 && (this.mFocusedWindow == null || (!this.mFocusedWindow.toString().contains("HwGlobalActions") && !this.mFocusedWindow.toString().contains("Sys2023:dream"))))) {
                boolean z;
                hideNotchStatusBar = false;
                if (this.mForceNotchStatusBar || !statusBarFocused) {
                    z = true;
                } else {
                    z = false;
                }
                this.mForceNotchStatusBar = z;
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
            if (this.mFocusedWindow != null) {
                i = this.hwNotchScreenWhiteConfig.isNotchAppInfo(this.mFocusedWindow);
            } else {
                i = 0;
            }
            if ((i ^ 1) == 0) {
                return true;
            }
            if (this.mFocusedWindow != null && (this.hwNotchScreenWhiteConfig.isNoneNotchAppWithStatusbarInfo(this.mFocusedWindow) || (((this.mFocusedWindow.getAttrs().hwFlags & 32768) != 0 && ((fl & 1024) != 0 || (this.mLastSystemUiFlags & 4) != 0)) || (this.mForceNotchStatusBar && (this.mFocusedWindow.toString().contains("SearchPanel") || (!this.mTopFullscreenOpaqueWindowState.toString().contains("Splash Screen") && (this.mTopFullscreenOpaqueWindowState.toString().equals(this.mFocusedWindow.toString()) ^ 1) != 0)))))) {
                this.mForceNotchStatusBar = true;
                hideNotchStatusBar = false;
                this.mBarVisibility = 0;
            } else if (this.mForceNotchStatusBar && this.mFocusedWindow != null && this.mFocusedWindow.getAttrs().type == 2 && ((PolicyControl.getWindowFlags(null, this.mFocusedWindow.getAttrs()) & 1024) != 0 || ((this.mFocusedWindow.getAttrs().hwFlags & 32768) != 0 && (this.mLastSystemUiFlags & 1024) != 0))) {
                this.mForceNotchStatusBar = true;
                hideNotchStatusBar = false;
                this.mBarVisibility = 0;
            } else if ((this.mFocusedWindow == null && this.mForceNotchStatusBar && !(((fl & 1024) == 0 && (this.mLastSystemUiFlags & 4) == 0) || (this.mTopFullscreenOpaqueWindowState.toString().contains("Splash Screen") ^ 1) == 0)) || !(this.mTopFullscreenOpaqueWindowState.getAttrs().type == 3 || (fl & 1024) == 0 || !this.mTopFullscreenOpaqueWindowState.toString().contains("Splash Screen"))) {
                this.mForceNotchStatusBar = true;
                hideNotchStatusBar = false;
                this.mBarVisibility = 0;
            }
            boolean isKeyguardShowing = !isKeyguardShowingOrOccluded() ? this.mKeyguardDelegate == null ? false : this.mKeyguardDelegate.isOccluded() : true;
            if (!isKeyguardShowing) {
                return hideNotchStatusBar;
            }
            this.mForceNotchStatusBar = false;
            this.mBarVisibility = 1;
            return true;
        }
    }

    public void notchStatusBarColorUpdate(int statusbarStateFlag) {
        notchTransactToStatusBarService(121, "notchTransactToStatusBarService", this.mLastIsEmuiStyle, this.mLastStatusBarColor, this.mLastNavigationBarColor, this.mLastIsEmuiLightStyle, statusbarStateFlag, this.mBarVisibility);
    }

    public void notchTransactToStatusBarService(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle, int statusbarStateFlag, int barVisibility) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getHWStatusBarService() != null) {
                IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
                if (statusBarServiceBinder != null) {
                    Log.d(TAG, "set statusbarColor:" + statusbarColor + " to status bar service");
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
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "notchTransactToStatusBarService->threw remote exception");
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    private void adjustForYouTuBeDodge(WindowState win, Rect pf) {
        if (win.toString().contains("com.google.android.youtube/com.google.android.apps.youtube.app.WatchWhileActivity")) {
            int magin;
            switch (this.mDisplayRotation) {
                case 1:
                    int count = (pf.bottom * 2) + this.mNotchPropSize;
                    if (count > pf.right) {
                        magin = count - pf.right;
                        if (magin < 10) {
                            pf.left = this.mNotchPropSize - magin;
                            break;
                        }
                    }
                    break;
                case 3:
                    magin = this.mNotchPropSize - (pf.right - (pf.bottom * 2));
                    if (magin > 0 && magin < 10) {
                        this.mNotchPropSize -= magin;
                        break;
                    }
            }
        }
    }
}
