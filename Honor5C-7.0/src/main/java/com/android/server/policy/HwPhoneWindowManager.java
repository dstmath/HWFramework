package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.CompatibilityInfo;
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
import android.os.Looper;
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
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telecom.TelecomManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Flog;
import android.util.HwSlog;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicy.KeyguardDismissDoneListener;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.view.WindowManagerPolicy.ScreenOnListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.WindowManagerPolicy.WindowState;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.server.LocalServices;
import com.android.server.PPPOEStateMachine;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.HwActivityManagerService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.location.HwGnssLogErrorCode;
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.util.NoExtAPIException;
import huawei.android.app.IHwWindowCallback;
import huawei.android.os.HwGeneralManager;
import huawei.android.provider.FingerSenseSettings;
import huawei.android.provider.FrontFingerPrintSettings;
import huawei.com.android.internal.widget.HwWidgetUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.com.android.server.policy.HwScreenOnProximityLock;
import huawei.com.android.server.policy.fingersense.SystemWideActionsListener;
import huawei.cust.HwCustUtils;
import java.util.HashSet;
import java.util.List;

public class HwPhoneWindowManager extends PhoneWindowManager implements TouchExplorationStateChangeListener {
    private static final String ACTION_HUAWEI_VASSISTANT_SERVICE = "com.huawei.ziri.model.MODELSERVICE";
    static final boolean DEBUG = false;
    static final boolean DEBUG_IMMERSION = false;
    private static final int DEFAULT_RESULT_VALUE = -2;
    private static final long DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MAX = 15000;
    private static final long DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MIN = 1500;
    static final String DROP_SMARTKEY_ACTIVITY = "drop_smartkey_activity";
    static final String FINGERPRINT_ANSWER_CALL = "fp_answer_call";
    static final String FINGERPRINT_CAMERA_SWITCH = "fp_take_photo";
    static final String FINGERPRINT_STOP_ALARM = "fp_stop_alarm";
    private static final int FLOATING_MASK = Integer.MIN_VALUE;
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
    private static final boolean IS_LONG_HOME_VASSITANT;
    private static final int JACK_DEVICE_ID = 16777216;
    private static final String KEY_TOUCH_DISABLE_MODE = "touch_disable_mode";
    private static final int MSG_BUTTON_LIGHT_TIMEOUT = 4099;
    private static final int MSG_DISPATCH_INTERNET_AUDIOKEY_WITH_WAKE_LOCK = 11;
    private static final int MSG_FINGERSENSE_DISABLE = 102;
    private static final int MSG_FINGERSENSE_ENABLE = 101;
    private static final int MSG_NAVIBAR_DISABLE = 104;
    private static final int MSG_NAVIBAR_ENABLE = 103;
    private static final int MSG_TRIKEY_BACK_LONG_PRESS = 4097;
    private static final int MSG_TRIKEY_RECENT_LONG_PRESS = 4098;
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
    private static final String VIBRATE_ON_TOUCH = "vibrate_on_touch";
    private static final int VIBRATOR_LONG_PRESS_FOR_FRONT_FP = 12;
    private static final int VIBRATOR_SHORT_PRESS_FOR_FRONT_FP = 11;
    private static String VOICE_ASSISTANT_ACTION = null;
    private static final long VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT = 400;
    private static final long VOLUMEDOWN_LONG_PRESS_TIMEOUT = 500;
    private static boolean mCustBeInit;
    private static boolean mCustUsed;
    static final boolean mIsHwNaviBar;
    private static boolean mTplusEnabled;
    private static int[] mUnableWakeKey;
    private static boolean mUsingHwNavibar;
    private boolean DEBUG_SMARTKEY;
    private int TRIKEY_NAVI_DEFAULT_MODE;
    private AlertDialog alertDialog;
    FingerprintActionsListener fingerprintActionsListener;
    private boolean isFingerAnswerPhoneOn;
    private boolean isFingerShotCameraOn;
    private boolean isFingerStopAlarmOn;
    private boolean isNavibarHide;
    private boolean isTouchDownUpLeftDoubleClick;
    private boolean isTouchDownUpRightDoubleClick;
    private boolean isVibrateImplemented;
    private boolean isVoiceRecognitionActive;
    private int lastDensityDpi;
    private int mActionBarHeight;
    private boolean mBackKeyPress;
    private long mBackKeyPressTime;
    private Light mBackLight;
    volatile boolean mBackTrikeyHandled;
    private WakeLock mBroadcastWakeLock;
    private Light mButtonLight;
    private int mButtonLightMode;
    private final Runnable mCancleInterceptFingerprintEvent;
    private CoverManager mCoverManager;
    private boolean mCoverOpen;
    private int mCurUser;
    HwCustPhoneWindowManager mCust;
    int mDesiredRotation;
    private boolean mDeviceProvisioned;
    boolean mFingerSenseEnabled;
    private ContentObserver mFingerprintObserver;
    private final Runnable mHandleVolumeDownKey;
    private Handler mHandlerEx;
    private boolean mHapticEnabled;
    private boolean mHeadless;
    private boolean mHintShown;
    private HwScreenOnProximityLock mHwScreenOnProximityLock;
    public IHwWindowCallback mIHwWindowCallback;
    private boolean mInputMethodWindowVisible;
    private boolean mIsHasActionBar;
    protected boolean mIsImmersiveMode;
    boolean mIsNavibarAlignLeftWhenLand;
    private boolean mIsProximity;
    private boolean mIsSmartKeyDoubleClick;
    private boolean mIsSmartKeyTripleOrMoreClick;
    private boolean mIsTouchExplrEnabled;
    private WindowState mLastColorWin;
    private int mLastIsEmuiLightStyle;
    private int mLastIsEmuiStyle;
    private int mLastNavigationBarColor;
    private long mLastSmartKeyDownTime;
    private long mLastStartVassistantServiceTime;
    private int mLastStatusBarColor;
    private long mLastTouchDownUpLeftKeyDownTime;
    private long mLastTouchDownUpRightKeyDownTime;
    private long mLastVolumeDownKeyDownTime;
    private ProximitySensorListener mListener;
    private boolean mMenuClickedOnlyOnce;
    private boolean mMenuKeyPress;
    private long mMenuKeyPressTime;
    boolean mNavibarEnabled;
    int[] mNavigationBarHeightForRotationMax;
    int[] mNavigationBarHeightForRotationMin;
    protected NavigationBarPolicy mNavigationBarPolicy;
    int[] mNavigationBarWidthForRotationMax;
    int[] mNavigationBarWidthForRotationMin;
    private boolean mNeedDropFingerprintEvent;
    OverscanTimeout mOverscanTimeout;
    private boolean mPowerKeyDisTouch;
    private long mPowerKeyDisTouchTime;
    private PowerManager mPowerManager;
    final Runnable mProximitySensorTimeoutRunnable;
    volatile boolean mRecentTrikeyHandled;
    private ContentResolver mResolver;
    private long mScreenRecorderPowerKeyTime;
    private boolean mScreenRecorderPowerKeyTriggered;
    private final Runnable mScreenRecorderRunnable;
    private boolean mScreenRecorderVolumeDownKeyTriggered;
    private boolean mScreenRecorderVolumeUpKeyConsumed;
    private long mScreenRecorderVolumeUpKeyTime;
    private boolean mScreenRecorderVolumeUpKeyTriggered;
    private SensorManager mSensorManager;
    private boolean mSensorRegisted;
    final Object mServiceAquireLock;
    private SettingsObserver mSettingsObserver;
    private final Runnable mSmartKeyClick;
    private final Runnable mSmartKeyLongPressed;
    boolean mStatuBarObsecured;
    IStatusBarService mStatusBarService;
    private boolean mSystraceLogCompleted;
    private long mSystraceLogFingerPrintTime;
    private boolean mSystraceLogPowerKeyTriggered;
    private final Runnable mSystraceLogRunnable;
    private boolean mSystraceLogVolumeDownKeyTriggered;
    private boolean mSystraceLogVolumeUpKeyConsumed;
    private long mSystraceLogVolumeUpKeyTime;
    private boolean mSystraceLogVolumeUpKeyTriggered;
    private TouchCountPolicy mTouchCountPolicy;
    private int mTouchDownUpLeftConsumeCount;
    private int mTouchDownUpRightConsumeCount;
    private int mTrikeyNaviMode;
    private SystemVibrator mVibrator;
    private boolean mVolumeDownKeyDisTouch;
    private final Runnable mVolumeDownLongPressed;
    private WakeLock mVolumeDownWakeLock;
    private boolean mVolumeUpKeyConsumedByDisTouch;
    private boolean mVolumeUpKeyDisTouch;
    private long mVolumeUpKeyDisTouchTime;
    private HashSet<String> needDropSmartKeyActivities;
    SystemWideActionsListener systemWideActionsListener;
    private Handler systraceLogDialogHandler;
    private HandlerThread systraceLogDialogThread;

    /* renamed from: com.android.server.policy.HwPhoneWindowManager.11 */
    class AnonymousClass11 extends ContentObserver {
        AnonymousClass11(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            HwPhoneWindowManager.this.updateFingerprintNav();
        }
    }

    /* renamed from: com.android.server.policy.HwPhoneWindowManager.13 */
    class AnonymousClass13 implements Runnable {
        final /* synthetic */ ContentResolver val$resolver;

        AnonymousClass13(ContentResolver val$resolver) {
            this.val$resolver = val$resolver;
        }

        public void run() {
            if (FrontFingerPrintSettings.isNaviBarEnabled(this.val$resolver)) {
                HwPhoneWindowManager.this.disableFingerPrintActions();
            } else {
                HwPhoneWindowManager.this.enableFingerPrintActions();
            }
        }
    }

    /* renamed from: com.android.server.policy.HwPhoneWindowManager.14 */
    class AnonymousClass14 implements Runnable {
        final /* synthetic */ ContentResolver val$resolver;

        AnonymousClass14(ContentResolver val$resolver) {
            this.val$resolver = val$resolver;
        }

        public void run() {
            if (FingerSenseSettings.isFingerSenseEnabled(this.val$resolver)) {
                HwPhoneWindowManager.this.enableSystemWideActions();
            } else {
                HwPhoneWindowManager.this.disableSystemWideActions();
            }
        }
    }

    class OverscanTimeout implements Runnable {
        OverscanTimeout() {
        }

        public void run() {
            Slog.i(HwPhoneWindowManager.TAG, "OverscanTimeout run");
            Global.putString(HwPhoneWindowManager.this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
        }
    }

    private class PolicyHandlerEx extends Handler {
        private PolicyHandlerEx() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwPhoneWindowManager.VIBRATOR_SHORT_PRESS_FOR_FRONT_FP /*11*/:
                    HwPhoneWindowManager.this.dispatchInternetAudioKeyWithWakeLock((KeyEvent) msg.obj);
                case HwPhoneWindowManager.MSG_FINGERSENSE_ENABLE /*101*/:
                    HwPhoneWindowManager.this.enableSystemWideActions();
                case HwPhoneWindowManager.MSG_FINGERSENSE_DISABLE /*102*/:
                    HwPhoneWindowManager.this.disableSystemWideActions();
                case HwPhoneWindowManager.MSG_NAVIBAR_ENABLE /*103*/:
                    HwPhoneWindowManager.this.disableFingerPrintActions();
                case HwPhoneWindowManager.MSG_NAVIBAR_DISABLE /*104*/:
                    HwPhoneWindowManager.this.enableFingerPrintActions();
                case HwPhoneWindowManager.MSG_TRIKEY_BACK_LONG_PRESS /*4097*/:
                    HwPhoneWindowManager.this.mBackTrikeyHandled = HwPhoneWindowManager.TOUCHPLUS_FORCE_VIBRATION;
                    if (HwPhoneWindowManager.this.mTrikeyNaviMode == HwPhoneWindowManager.TOUCHPLUS_SETTINGS_ENABLED) {
                        HwPhoneWindowManager.this.startHwVibrate(HwPhoneWindowManager.VIBRATOR_LONG_PRESS_FOR_FRONT_FP);
                        ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
                    } else if (HwPhoneWindowManager.this.mTrikeyNaviMode == 0) {
                        HwPhoneWindowManager.this.unlockScreenPinningTest();
                    }
                case HwPhoneWindowManager.MSG_TRIKEY_RECENT_LONG_PRESS /*4098*/:
                    HwPhoneWindowManager.this.mRecentTrikeyHandled = HwPhoneWindowManager.TOUCHPLUS_FORCE_VIBRATION;
                    if (HwPhoneWindowManager.this.mTrikeyNaviMode == 0) {
                        HwPhoneWindowManager.this.startHwVibrate(HwPhoneWindowManager.VIBRATOR_LONG_PRESS_FOR_FRONT_FP);
                        ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
                    } else if (HwPhoneWindowManager.this.mTrikeyNaviMode == HwPhoneWindowManager.TOUCHPLUS_SETTINGS_ENABLED) {
                        HwPhoneWindowManager.this.unlockScreenPinningTest();
                    }
                case HwPhoneWindowManager.MSG_BUTTON_LIGHT_TIMEOUT /*4099*/:
                    if (HwPhoneWindowManager.this.mButtonLight == null) {
                        return;
                    }
                    if (HwPhoneWindowManager.this.mPowerManager == null || !HwPhoneWindowManager.this.mPowerManager.isScreenOn()) {
                        HwPhoneWindowManager.this.setButtonLightTimeout(HwPhoneWindowManager.IS_LONG_HOME_VASSITANT);
                        return;
                    }
                    HwPhoneWindowManager.this.mButtonLight.setBrightness(HwPhoneWindowManager.TOUCHPLUS_SETTINGS_DISABLED);
                    HwPhoneWindowManager.this.setButtonLightTimeout(HwPhoneWindowManager.TOUCHPLUS_FORCE_VIBRATION);
                default:
            }
        }
    }

    private class ProximitySensorListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent arg0) {
            boolean z = HwPhoneWindowManager.IS_LONG_HOME_VASSITANT;
            float[] its = arg0.values;
            if (its != null && arg0.sensor.getType() == 8 && its.length > 0) {
                Log.i(HwPhoneWindowManager.TAG, "sensor value: its[0] = " + its[HwPhoneWindowManager.TOUCHPLUS_SETTINGS_DISABLED]);
                HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
                if (its[HwPhoneWindowManager.TOUCHPLUS_SETTINGS_DISABLED] >= 0.0f && its[HwPhoneWindowManager.TOUCHPLUS_SETTINGS_DISABLED] < HwPhoneWindowManager.TYPICAL_PROXIMITY_THRESHOLD) {
                    z = HwPhoneWindowManager.TOUCHPLUS_FORCE_VIBRATION;
                }
                hwPhoneWindowManager.mIsProximity = z;
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

    class SettingsObserver extends ContentObserver {
        final /* synthetic */ HwPhoneWindowManager this$0;

        SettingsObserver(HwPhoneWindowManager this$0, Handler handler) {
            boolean z;
            boolean z2 = HwPhoneWindowManager.TOUCHPLUS_FORCE_VIBRATION;
            this.this$0 = this$0;
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            if (Secure.getIntForUser(this$0.mResolver, "device_provisioned", HwPhoneWindowManager.TOUCHPLUS_SETTINGS_DISABLED, ActivityManager.getCurrentUser()) != 0) {
                z = HwPhoneWindowManager.TOUCHPLUS_FORCE_VIBRATION;
            } else {
                z = HwPhoneWindowManager.IS_LONG_HOME_VASSITANT;
            }
            this$0.mDeviceProvisioned = z;
            this$0.mTrikeyNaviMode = System.getIntForUser(this$0.mResolver, HwPhoneWindowManager.FRONT_FINGERPRINT_SWAP_KEY_POSITION, this$0.TRIKEY_NAVI_DEFAULT_MODE, ActivityManager.getCurrentUser());
            this$0.mButtonLightMode = System.getIntForUser(this$0.mResolver, HwPhoneWindowManager.FRONT_FINGERPRINT_BUTTON_LIGHT_MODE, HwPhoneWindowManager.TOUCHPLUS_SETTINGS_ENABLED, ActivityManager.getCurrentUser());
            if (System.getIntForUser(this$0.mResolver, HwPhoneWindowManager.HAPTIC_FEEDBACK_TRIKEY_SETTINGS, HwPhoneWindowManager.TOUCHPLUS_SETTINGS_ENABLED, ActivityManager.getCurrentUser()) == 0) {
                z2 = HwPhoneWindowManager.IS_LONG_HOME_VASSITANT;
            }
            this$0.mHapticEnabled = z2;
        }

        public void registerContentObserver(int userId) {
            this.this$0.mResolver.registerContentObserver(System.getUriFor(HwPhoneWindowManager.FRONT_FINGERPRINT_SWAP_KEY_POSITION), HwPhoneWindowManager.IS_LONG_HOME_VASSITANT, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("device_provisioned"), HwPhoneWindowManager.IS_LONG_HOME_VASSITANT, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor(HwPhoneWindowManager.FRONT_FINGERPRINT_BUTTON_LIGHT_MODE), HwPhoneWindowManager.IS_LONG_HOME_VASSITANT, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor(HwPhoneWindowManager.HAPTIC_FEEDBACK_TRIKEY_SETTINGS), HwPhoneWindowManager.IS_LONG_HOME_VASSITANT, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z;
            boolean z2 = HwPhoneWindowManager.TOUCHPLUS_FORCE_VIBRATION;
            HwPhoneWindowManager hwPhoneWindowManager = this.this$0;
            if (Secure.getIntForUser(this.this$0.mResolver, "device_provisioned", HwPhoneWindowManager.TOUCHPLUS_SETTINGS_DISABLED, ActivityManager.getCurrentUser()) != 0) {
                z = HwPhoneWindowManager.TOUCHPLUS_FORCE_VIBRATION;
            } else {
                z = HwPhoneWindowManager.IS_LONG_HOME_VASSITANT;
            }
            hwPhoneWindowManager.mDeviceProvisioned = z;
            this.this$0.mTrikeyNaviMode = System.getIntForUser(this.this$0.mResolver, HwPhoneWindowManager.FRONT_FINGERPRINT_SWAP_KEY_POSITION, this.this$0.TRIKEY_NAVI_DEFAULT_MODE, ActivityManager.getCurrentUser());
            this.this$0.mButtonLightMode = System.getIntForUser(this.this$0.mResolver, HwPhoneWindowManager.FRONT_FINGERPRINT_BUTTON_LIGHT_MODE, HwPhoneWindowManager.TOUCHPLUS_SETTINGS_ENABLED, ActivityManager.getCurrentUser());
            this.this$0.resetButtonLightStatus();
            Slog.i(HwPhoneWindowManager.TAG, "mTrikeyNaviMode is:" + this.this$0.mTrikeyNaviMode + " mButtonLightMode is:" + this.this$0.mButtonLightMode);
            HwPhoneWindowManager hwPhoneWindowManager2 = this.this$0;
            if (System.getIntForUser(this.this$0.mResolver, HwPhoneWindowManager.HAPTIC_FEEDBACK_TRIKEY_SETTINGS, HwPhoneWindowManager.TOUCHPLUS_SETTINGS_ENABLED, ActivityManager.getCurrentUser()) == 0) {
                z2 = HwPhoneWindowManager.IS_LONG_HOME_VASSITANT;
            }
            hwPhoneWindowManager2.mHapticEnabled = z2;
        }
    }

    public HwPhoneWindowManager() {
        this.DEBUG_SMARTKEY = IS_LONG_HOME_VASSITANT;
        this.mIsSmartKeyDoubleClick = IS_LONG_HOME_VASSITANT;
        this.mIsSmartKeyTripleOrMoreClick = IS_LONG_HOME_VASSITANT;
        this.mNeedDropFingerprintEvent = IS_LONG_HOME_VASSITANT;
        this.needDropSmartKeyActivities = new HashSet();
        this.isFingerShotCameraOn = IS_LONG_HOME_VASSITANT;
        this.isFingerStopAlarmOn = IS_LONG_HOME_VASSITANT;
        this.isFingerAnswerPhoneOn = IS_LONG_HOME_VASSITANT;
        this.mServiceAquireLock = new Object();
        this.mCust = (HwCustPhoneWindowManager) HwCustUtils.createObj(HwCustPhoneWindowManager.class, new Object[TOUCHPLUS_SETTINGS_DISABLED]);
        this.mFingerSenseEnabled = TOUCHPLUS_FORCE_VIBRATION;
        this.mBackKeyPress = IS_LONG_HOME_VASSITANT;
        this.mMenuKeyPress = IS_LONG_HOME_VASSITANT;
        this.mMenuKeyPressTime = 0;
        this.mBackKeyPressTime = 0;
        this.isVibrateImplemented = SystemProperties.getBoolean("ro.config.touch_vibrate", IS_LONG_HOME_VASSITANT);
        this.mNavibarEnabled = IS_LONG_HOME_VASSITANT;
        this.mTrikeyNaviMode = -1;
        this.TRIKEY_NAVI_DEFAULT_MODE = -1;
        this.mDeviceProvisioned = IS_LONG_HOME_VASSITANT;
        this.mButtonLightMode = TOUCHPLUS_SETTINGS_ENABLED;
        this.mButtonLight = null;
        this.mBackLight = null;
        this.mHapticEnabled = TOUCHPLUS_FORCE_VIBRATION;
        this.mTouchCountPolicy = new TouchCountPolicy();
        this.mDesiredRotation = -1;
        this.mMenuClickedOnlyOnce = IS_LONG_HOME_VASSITANT;
        this.mOverscanTimeout = new OverscanTimeout();
        this.mSystraceLogVolumeUpKeyTime = 0;
        this.mSystraceLogVolumeUpKeyConsumed = IS_LONG_HOME_VASSITANT;
        this.mSystraceLogVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
        this.mSystraceLogVolumeDownKeyTriggered = IS_LONG_HOME_VASSITANT;
        this.mSystraceLogFingerPrintTime = 0;
        this.mSystraceLogPowerKeyTriggered = IS_LONG_HOME_VASSITANT;
        this.mSystraceLogCompleted = TOUCHPLUS_FORCE_VIBRATION;
        this.mSystraceLogRunnable = new Runnable() {

            /* renamed from: com.android.server.policy.HwPhoneWindowManager.1.1 */
            class AnonymousClass1 extends Handler {
                AnonymousClass1(Looper $anonymous0) {
                    super($anonymous0);
                }

                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    Object[] objArr;
                    if (msg.what == 0) {
                        HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
                        Builder title = new Builder(HwPhoneWindowManager.this.mContext).setTitle(17039380);
                        Resources resources = HwPhoneWindowManager.this.mContext.getResources();
                        objArr = new Object[HwPhoneWindowManager.TOUCHPLUS_SETTINGS_ENABLED];
                        objArr[HwPhoneWindowManager.TOUCHPLUS_SETTINGS_DISABLED] = Integer.valueOf(10);
                        hwPhoneWindowManager.alertDialog = title.setMessage(resources.getQuantityString(34734088, 10, objArr)).setCancelable(HwPhoneWindowManager.IS_LONG_HOME_VASSITANT).create();
                        HwPhoneWindowManager.this.alertDialog.getWindow().setType(2003);
                        HwPhoneWindowManager.this.alertDialog.getWindow().setFlags(HwSecDiagnoseConstant.BIT_VERIFYBOOT, HwSecDiagnoseConstant.BIT_VERIFYBOOT);
                        HwPhoneWindowManager.this.alertDialog.show();
                    } else if (msg.what <= 0 || msg.what >= 10) {
                        HwPhoneWindowManager.this.alertDialog.dismiss();
                    } else {
                        AlertDialog -get1 = HwPhoneWindowManager.this.alertDialog;
                        Resources resources2 = HwPhoneWindowManager.this.mContext.getResources();
                        int i = 10 - msg.what;
                        objArr = new Object[HwPhoneWindowManager.TOUCHPLUS_SETTINGS_ENABLED];
                        objArr[HwPhoneWindowManager.TOUCHPLUS_SETTINGS_DISABLED] = Integer.valueOf(10 - msg.what);
                        -get1.setMessage(resources2.getQuantityString(34734088, i, objArr));
                        TelecomManager telecomManager = (TelecomManager) HwPhoneWindowManager.this.mContext.getSystemService("telecom");
                        if (telecomManager != null && telecomManager.isRinging()) {
                            HwPhoneWindowManager.this.alertDialog.dismiss();
                        }
                    }
                }
            }

            public void run() {
                HwPhoneWindowManager.this.systraceLogDialogThread = new HandlerThread("SystraceLogDialog");
                HwPhoneWindowManager.this.systraceLogDialogThread.start();
                HwPhoneWindowManager.this.systraceLogDialogHandler = new AnonymousClass1(HwPhoneWindowManager.this.systraceLogDialogThread.getLooper());
                HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(HwPhoneWindowManager.TOUCHPLUS_SETTINGS_DISABLED);
                new Thread(new Runnable() {
                    public void run() {
                        int i = HwPhoneWindowManager.TOUCHPLUS_SETTINGS_ENABLED;
                        while (!HwPhoneWindowManager.this.mSystraceLogCompleted && i < HwPhoneWindowManager.VIBRATOR_SHORT_PRESS_FOR_FRONT_FP) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Log.w(HwPhoneWindowManager.TAG, "systrace log not completed,interrupted");
                            }
                            if (!(HwPhoneWindowManager.this.systraceLogDialogHandler == null || HwPhoneWindowManager.this.mSystraceLogCompleted)) {
                                HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(i);
                            }
                            i += HwPhoneWindowManager.TOUCHPLUS_SETTINGS_ENABLED;
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
                                sJankService.transact(HwPhoneWindowManager.START_MODE_QUICK_START_CALL, data, reply, HwPhoneWindowManager.TOUCHPLUS_SETTINGS_DISABLED);
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
                            HwPhoneWindowManager.this.mSystraceLogCompleted = HwPhoneWindowManager.TOUCHPLUS_FORCE_VIBRATION;
                            HwPhoneWindowManager.this.systraceLogDialogThread.quitSafely();
                            Log.d(HwPhoneWindowManager.TAG, "has quit the systraceLogDialogThread" + HwPhoneWindowManager.this.systraceLogDialogThread.getId());
                        }
                    }
                }).start();
            }
        };
        this.mScreenRecorderRunnable = new Runnable() {
            public void run() {
                Intent intent = new Intent();
                intent.setAction(HwPhoneWindowManager.HUAWEI_SCREENRECORDER_ACTION);
                intent.setClassName(HwPhoneWindowManager.HUAWEI_SCREENRECORDER_PACKAGE, HwPhoneWindowManager.HUAWEI_SCREENRECORDER_START_MODE);
                HwPhoneWindowManager.this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
                Log.d(HwPhoneWindowManager.TAG, "start screen recorder service");
            }
        };
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
                    HwPhoneWindowManager.this.notifyVassistantService("start", HwPhoneWindowManager.START_MODE_QUICK_START_CALL, null);
                }
                HwPhoneWindowManager.this.turnOffSensorListener();
                HwPhoneWindowManager.this.isVoiceRecognitionActive = HwPhoneWindowManager.TOUCHPLUS_FORCE_VIBRATION;
                HwPhoneWindowManager.this.mLastStartVassistantServiceTime = SystemClock.uptimeMillis();
            }
        };
        this.mNavigationBarPolicy = null;
        this.mNavigationBarHeightForRotationMin = new int[START_MODE_VOICE_WAKEUP_ONE_SHOT];
        this.mNavigationBarHeightForRotationMax = new int[START_MODE_VOICE_WAKEUP_ONE_SHOT];
        this.mNavigationBarWidthForRotationMax = new int[START_MODE_VOICE_WAKEUP_ONE_SHOT];
        this.mNavigationBarWidthForRotationMin = new int[START_MODE_VOICE_WAKEUP_ONE_SHOT];
        this.mSensorManager = null;
        this.mCoverManager = null;
        this.mCoverOpen = TOUCHPLUS_FORCE_VIBRATION;
        this.mSensorRegisted = IS_LONG_HOME_VASSITANT;
        this.mListener = null;
        this.mIsProximity = IS_LONG_HOME_VASSITANT;
        this.mProximitySensorTimeoutRunnable = new Runnable() {
            public void run() {
                Log.i(HwPhoneWindowManager.TAG, "mProximitySensorTimeout, unRegisterListener");
                HwPhoneWindowManager.this.turnOffSensorListener();
            }
        };
        this.mCancleInterceptFingerprintEvent = new Runnable() {
            public void run() {
                HwPhoneWindowManager.this.mNeedDropFingerprintEvent = HwPhoneWindowManager.IS_LONG_HOME_VASSITANT;
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
        this.mIsImmersiveMode = IS_LONG_HOME_VASSITANT;
        this.lastDensityDpi = -1;
    }

    static {
        mTplusEnabled = SystemProperties.getBoolean("ro.config.hw_touchplus_enabled", IS_LONG_HOME_VASSITANT);
        mCustBeInit = IS_LONG_HOME_VASSITANT;
        mCustUsed = IS_LONG_HOME_VASSITANT;
        IS_LONG_HOME_VASSITANT = SystemProperties.getBoolean("ro.hw.long.home.vassistant", TOUCHPLUS_FORCE_VIBRATION);
        VOICE_ASSISTANT_ACTION = "com.huawei.action.VOICE_ASSISTANT";
        mIsHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", IS_LONG_HOME_VASSITANT);
        mUsingHwNavibar = SystemProperties.getBoolean("ro.config.hw_navigationbar", IS_LONG_HOME_VASSITANT);
    }

    public void systemReady() {
        super.systemReady();
        this.mHandler.post(new Runnable() {
            public void run() {
                HwPhoneWindowManager.this.initQuickcall();
            }
        });
        this.mHwScreenOnProximityLock = new HwScreenOnProximityLock(this.mContext);
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
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", IS_LONG_HOME_VASSITANT) && this.mSystemReady) {
            EasyWakeUpManager mWakeUpManager = EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate);
            ServiceManager.addService("easywakeup", mWakeUpManager);
            mWakeUpManager.saveTouchPointNodePath();
        }
        this.mListener = new ProximitySensorListener();
        this.mResolver = this.mContext.getContentResolver();
        this.TRIKEY_NAVI_DEFAULT_MODE = FrontFingerPrintSettings.getDefaultNaviMode();
        this.mSettingsObserver = new SettingsObserver(this, this.mHandler);
        this.mVibrator = (SystemVibrator) ((Vibrator) this.mContext.getSystemService("vibrator"));
        if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == TOUCHPLUS_SETTINGS_ENABLED) {
            LightsManager lights = (LightsManager) LocalServices.getService(LightsManager.class);
            this.mButtonLight = lights.getLight(START_MODE_QUICK_START_CALL);
            this.mBackLight = lights.getLight(TOUCHPLUS_SETTINGS_DISABLED);
            if (this.mContext != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.SCREEN_ON");
                this.mContext.registerReceiver(new ScreenBroadcastReceiver(), filter);
            }
        }
    }

    public void addPointerEvent(MotionEvent motionEvent) {
        if (this.mNavigationBarPolicy != null) {
            this.mNavigationBarPolicy.addPointerEvent(motionEvent);
        }
    }

    public void init(Context context, IWindowManager windowManager, WindowManagerFuncs windowManagerFuncs) {
        this.mHandlerEx = new PolicyHandlerEx();
        this.fingersense_enable = "fingersense_smartshot_enabled";
        this.fingersense_letters_enable = "fingersense_letters_enabled";
        this.line_gesture_enable = "fingersense_multiwindow_enabled";
        Flog.i(1503, "init fingersense_letters_enable with " + this.fingersense_letters_enable);
        this.navibar_enable = "enable_navbar";
        this.mCurUser = ActivityManager.getCurrentUser();
        this.mResolver = context.getContentResolver();
        this.mFingerprintObserver = new AnonymousClass11(this.mHandler);
        registerFingerprintObserver(this.mCurUser);
        updateFingerprintNav();
        initDropSmartKey();
        super.init(context, windowManager, windowManagerFuncs);
    }

    private void updateFingerprintNav() {
        boolean z;
        boolean z2 = TOUCHPLUS_FORCE_VIBRATION;
        if (Secure.getIntForUser(this.mResolver, FINGERPRINT_CAMERA_SWITCH, TOUCHPLUS_SETTINGS_ENABLED, this.mCurUser) == TOUCHPLUS_SETTINGS_ENABLED) {
            z = TOUCHPLUS_FORCE_VIBRATION;
        } else {
            z = IS_LONG_HOME_VASSITANT;
        }
        this.isFingerShotCameraOn = z;
        if (Secure.getIntForUser(this.mResolver, FINGERPRINT_STOP_ALARM, TOUCHPLUS_SETTINGS_DISABLED, this.mCurUser) == TOUCHPLUS_SETTINGS_ENABLED) {
            z = TOUCHPLUS_FORCE_VIBRATION;
        } else {
            z = IS_LONG_HOME_VASSITANT;
        }
        this.isFingerStopAlarmOn = z;
        if (Secure.getIntForUser(this.mResolver, FINGERPRINT_ANSWER_CALL, TOUCHPLUS_SETTINGS_DISABLED, this.mCurUser) != TOUCHPLUS_SETTINGS_ENABLED) {
            z2 = IS_LONG_HOME_VASSITANT;
        }
        this.isFingerAnswerPhoneOn = z2;
    }

    private void registerFingerprintObserver(int userId) {
        this.mResolver.registerContentObserver(Secure.getUriFor(FINGERPRINT_CAMERA_SWITCH), TOUCHPLUS_FORCE_VIBRATION, this.mFingerprintObserver, userId);
        this.mResolver.registerContentObserver(Secure.getUriFor(FINGERPRINT_STOP_ALARM), TOUCHPLUS_FORCE_VIBRATION, this.mFingerprintObserver, userId);
        this.mResolver.registerContentObserver(Secure.getUriFor(FINGERPRINT_ANSWER_CALL), TOUCHPLUS_FORCE_VIBRATION, this.mFingerprintObserver, userId);
    }

    public void setCurrentUser(int userId, int[] currentProfileIds) {
        this.mCurUser = userId;
        registerFingerprintObserver(userId);
        this.mFingerprintObserver.onChange(TOUCHPLUS_FORCE_VIBRATION);
    }

    public int checkAddPermission(LayoutParams attrs, int[] outAppOp) {
        if (attrs.type == 2101) {
            return TOUCHPLUS_SETTINGS_DISABLED;
        }
        return super.checkAddPermission(attrs, outAppOp);
    }

    public int windowTypeToLayerLw(int type) {
        switch (type) {
            case 2100:
                return 30;
            case 2101:
                return 31;
            default:
                return super.windowTypeToLayerLw(type);
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
        return TOUCHPLUS_FORCE_VIBRATION;
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

    public View addStartingWindow(IBinder appToken, String packageName, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, Configuration overrideConfig) {
        Context context = this.mContext;
        try {
            context = this.mContext.createPackageContext(packageName, TOUCHPLUS_SETTINGS_DISABLED);
            ViewGroup docview = (ViewGroup) super.addStartingWindow(appToken, packageName, theme, compatInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags, overrideConfig);
            if (docview == null) {
                return null;
            }
            if (((context.getApplicationInfo() == null ? TOUCHPLUS_SETTINGS_DISABLED : context.getApplicationInfo().flags) & TOUCHPLUS_SETTINGS_ENABLED) == 0 || !this.mIsHasActionBar || (theme >>> 24) != START_MODE_QUICK_START_CALL || isHwDarkTheme(context, theme)) {
                return docview;
            }
            Slog.d(TAG, "starting window system app and have actionbar");
            this.mIsHasActionBar = IS_LONG_HOME_VASSITANT;
            LayoutParams lp = new LayoutParams(-1, DEFAULT_RESULT_VALUE);
            lp.flags = 24;
            lp.format = 3;
            lp.width = -1;
            lp.height = this.mActionBarHeight + this.mStatusBarHeight;
            View tmp = getActionBarView(context, theme);
            if (tmp == null) {
                Slog.d(TAG, "action bar view is null");
                return docview;
            }
            docview.addView(tmp, lp);
            return docview;
        } catch (NameNotFoundException e) {
            return super.addStartingWindow(appToken, packageName, theme, compatInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags, overrideConfig);
        }
    }

    public void beginPostLayoutPolicyLw(int displayWidth, int displayHeight) {
        super.beginPostLayoutPolicyLw(displayWidth, displayHeight);
        this.mStatuBarObsecured = IS_LONG_HOME_VASSITANT;
    }

    public void applyPostLayoutPolicyLw(WindowState win, LayoutParams attrs, WindowState attached) {
        super.applyPostLayoutPolicyLw(win, attrs, attached);
        if (win.isVisibleLw() && win.getSurfaceLayer() > this.mStatusBarLayer && isStatusBarObsecuredByWin(win)) {
            this.mStatuBarObsecured = TOUCHPLUS_FORCE_VIBRATION;
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
        boolean flag = IS_LONG_HOME_VASSITANT;
        try {
            flag = MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (NoExtAPIException e) {
            Log.w(TAG, "CoverManagerService->isMultiSimEnabled->NoExtAPIException!");
        }
        return flag;
    }

    private boolean isPhoneInCall() {
        if (isMultiSimEnabled()) {
            for (int i = TOUCHPLUS_SETTINGS_DISABLED; i < MSimTelephonyManager.getDefault().getPhoneCount(); i += TOUCHPLUS_SETTINGS_ENABLED) {
                if (MSimTelephonyManager.getDefault().getCallState(i) != 0) {
                    return TOUCHPLUS_FORCE_VIBRATION;
                }
            }
            return IS_LONG_HOME_VASSITANT;
        } else if (TelephonyManager.getDefault().getCallState(SubscriptionManager.getDefaultSubscriptionId()) != 0) {
            return TOUCHPLUS_FORCE_VIBRATION;
        } else {
            return IS_LONG_HOME_VASSITANT;
        }
    }

    static ITelephony getTelephonyService() {
        return Stub.asInterface(ServiceManager.checkService("phone"));
    }

    public boolean needTurnOff(int why) {
        boolean isOffhook = isPhoneInCall();
        boolean isSecure = isKeyguardSecure(this.mCurrentUserId);
        if (!isOffhook) {
            return TOUCHPLUS_FORCE_VIBRATION;
        }
        if (!isSecure || why == 3) {
            return IS_LONG_HOME_VASSITANT;
        }
        if (why != 6) {
            return TOUCHPLUS_FORCE_VIBRATION;
        }
        return IS_LONG_HOME_VASSITANT;
    }

    public boolean needTurnOffWithDismissFlag() {
        if (this.mDismissKeyguard == 0 || isKeyguardSecure(this.mCurrentUserId)) {
            return TOUCHPLUS_FORCE_VIBRATION;
        }
        return IS_LONG_HOME_VASSITANT;
    }

    protected boolean isWakeKeyWhenScreenOff(int keyCode) {
        if (!mCustUsed) {
            return super.isWakeKeyWhenScreenOff(keyCode);
        }
        for (int i = TOUCHPLUS_SETTINGS_DISABLED; i < mUnableWakeKey.length; i += TOUCHPLUS_SETTINGS_ENABLED) {
            if (keyCode == mUnableWakeKey[i]) {
                return IS_LONG_HOME_VASSITANT;
            }
        }
        return TOUCHPLUS_FORCE_VIBRATION;
    }

    public boolean isWakeKeyFun(int keyCode) {
        if (!mCustBeInit) {
            getKeycodeFromCust();
        }
        if (!mCustUsed) {
            return IS_LONG_HOME_VASSITANT;
        }
        for (int i = TOUCHPLUS_SETTINGS_DISABLED; i < mUnableWakeKey.length; i += TOUCHPLUS_SETTINGS_ENABLED) {
            if (keyCode == mUnableWakeKey[i]) {
                return IS_LONG_HOME_VASSITANT;
            }
        }
        return TOUCHPLUS_FORCE_VIBRATION;
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
                int i = TOUCHPLUS_SETTINGS_DISABLED;
                while (i < mUnableWakeKey.length) {
                    try {
                        mUnableWakeKey[i] = Integer.parseInt(unableWakeKeyArray[i]);
                        i += TOUCHPLUS_SETTINGS_ENABLED;
                    } catch (Exception e2) {
                        Log.e(TAG, "Exception when copy the translated value from sting array to int array", e2);
                    }
                }
                mCustUsed = TOUCHPLUS_FORCE_VIBRATION;
            }
        }
        mCustBeInit = TOUCHPLUS_FORCE_VIBRATION;
    }

    public int interceptMotionBeforeQueueingNonInteractive(long whenNanos, int policyFlags) {
        if ((FLOATING_MASK & policyFlags) == 0) {
            return super.interceptMotionBeforeQueueingNonInteractive(whenNanos, policyFlags);
        }
        Slog.i(TAG, "interceptMotionBeforeQueueingNonInteractive policyFlags: " + policyFlags);
        Global.putString(this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
        return TOUCHPLUS_SETTINGS_DISABLED;
    }

    protected int getSingleHandState() {
        int windowManagerService = WindowManagerGlobal.getWindowManagerService();
        IBinder windowManagerBinder = windowManagerService.asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (windowManagerBinder != null) {
            try {
                data.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(1990, data, reply, TOUCHPLUS_SETTINGS_DISABLED);
                reply.readException();
                windowManagerService = reply.readInt();
                return windowManagerService;
            } catch (RemoteException e) {
                return TOUCHPLUS_SETTINGS_DISABLED;
            } finally {
                data.recycle();
                reply.recycle();
            }
        } else {
            data.recycle();
            reply.recycle();
            return TOUCHPLUS_SETTINGS_DISABLED;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void unlockScreenPinningTest() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getHWStatusBarService() != null) {
                IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
                if (statusBarServiceBinder != null) {
                    Log.d(TAG, "Transact unlockScreenPinningTest to status bar service!");
                    data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    statusBarServiceBinder.transact(111, data, reply, TOUCHPLUS_SETTINGS_DISABLED);
                }
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
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
        if (now <= this.mSystraceLogVolumeUpKeyTime + TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS && now <= this.mSystraceLogFingerPrintTime + SYSTRACELOG_FINGERPRINT_EFFECT_DELAY && this.mSystraceLogCompleted) {
            this.mSystraceLogCompleted = IS_LONG_HOME_VASSITANT;
            this.mSystraceLogVolumeUpKeyConsumed = TOUCHPLUS_FORCE_VIBRATION;
            this.mSystraceLogFingerPrintTime = 0;
            this.mSystraceLogVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
            this.mScreenRecorderVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
            Trace.traceBegin(8, "invoke_systrace_log_dump");
            Jlog.d(313, "HwPhoneWindowManager Systrace triggered");
            Trace.traceEnd(8);
            Log.d(TAG, "Systrace triggered");
            this.mHandler.postDelayed(this.mSystraceLogRunnable, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
        }
    }

    private void interceptScreenRecorder() {
        if (this.mScreenRecorderVolumeUpKeyTriggered && this.mScreenRecorderPowerKeyTriggered && !this.mScreenRecorderVolumeDownKeyTriggered && !SystemProperties.getBoolean("sys.super_power_save", IS_LONG_HOME_VASSITANT) && !keyguardIsShowingTq() && checkPackageInstalled(HUAWEI_SCREENRECORDER_PACKAGE)) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mScreenRecorderVolumeUpKeyTime + TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS && now <= this.mScreenRecorderPowerKeyTime + TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS) {
                this.mScreenRecorderVolumeUpKeyConsumed = TOUCHPLUS_FORCE_VIBRATION;
                cancelPendingPowerKeyActionForDistouch();
                this.mHandler.postDelayed(this.mScreenRecorderRunnable, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
            }
        }
    }

    private void cancelPendingScreenRecorderAction() {
        this.mHandler.removeCallbacks(this.mScreenRecorderRunnable);
    }

    boolean isVoiceCall() {
        boolean z = TOUCHPLUS_FORCE_VIBRATION;
        IAudioService audioService = getAudioService();
        if (audioService != null) {
            try {
                int mode = audioService.getMode();
                if (!(mode == 3 || mode == START_MODE_QUICK_START_CALL)) {
                    z = IS_LONG_HOME_VASSITANT;
                }
                return z;
            } catch (RemoteException e) {
                Log.w(TAG, "getMode exception");
            }
        }
        return IS_LONG_HOME_VASSITANT;
    }

    private void sendKeyEvent(int keycode) {
        int[] actions = new int[]{TOUCHPLUS_SETTINGS_DISABLED, TOUCHPLUS_SETTINGS_ENABLED};
        for (int i = TOUCHPLUS_SETTINGS_DISABLED; i < actions.length; i += TOUCHPLUS_SETTINGS_ENABLED) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, actions[i], keycode, TOUCHPLUS_SETTINGS_DISABLED, TOUCHPLUS_SETTINGS_DISABLED, -1, TOUCHPLUS_SETTINGS_DISABLED, 8, 257), TOUCHPLUS_SETTINGS_DISABLED);
        }
    }

    private boolean isExcluedScene() {
        String pkgName = ((ActivityManagerService) ServiceManager.getService("activity")).topAppName();
        String pkg_alarm = "com.android.deskclock/.alarmclock.LockAlarmFullActivity";
        boolean isSuperPowerMode = SystemProperties.getBoolean("sys.super_power_save", IS_LONG_HOME_VASSITANT);
        if (pkgName == null) {
            return IS_LONG_HOME_VASSITANT;
        }
        boolean z;
        if (pkgName.equals(pkg_alarm) || isSuperPowerMode || !this.mDeviceProvisioned) {
            z = TOUCHPLUS_FORCE_VIBRATION;
        } else {
            z = keyguardOn();
        }
        return z;
    }

    private boolean isExcluedBackScene() {
        boolean z = TOUCHPLUS_FORCE_VIBRATION;
        if (this.mTrikeyNaviMode == TOUCHPLUS_SETTINGS_ENABLED) {
            return isExcluedScene();
        }
        if (this.mDeviceProvisioned) {
            z = IS_LONG_HOME_VASSITANT;
        }
        return z;
    }

    private boolean isExcluedRecentScene() {
        boolean z = TOUCHPLUS_FORCE_VIBRATION;
        if (this.mTrikeyNaviMode != TOUCHPLUS_SETTINGS_ENABLED) {
            return isExcluedScene();
        }
        if (this.mDeviceProvisioned) {
            z = IS_LONG_HOME_VASSITANT;
        }
        return z;
    }

    public void setCurrentUserLw(int newUserId) {
        super.setCurrentUserLw(newUserId);
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(TOUCHPLUS_FORCE_VIBRATION);
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
                    setButtonLightTimeout(IS_LONG_HOME_VASSITANT);
                    this.mButtonLight.setBrightness(TOUCHPLUS_SETTINGS_DISABLED);
                } else if (this.mButtonLightMode != 0) {
                    setButtonLightTimeout(IS_LONG_HOME_VASSITANT);
                    this.mButtonLight.setBrightness(this.mBackLight.getCurrentBrightness());
                } else if (this.mButtonLight.getCurrentBrightness() > 0) {
                    setButtonLightTimeout(IS_LONG_HOME_VASSITANT);
                    Message msg = this.mHandlerEx.obtainMessage(MSG_BUTTON_LIGHT_TIMEOUT);
                    msg.setAsynchronous(TOUCHPLUS_FORCE_VIBRATION);
                    this.mHandlerEx.sendMessageDelayed(msg, 5000);
                } else {
                    setButtonLightTimeout(TOUCHPLUS_FORCE_VIBRATION);
                }
            } else {
                setButtonLightTimeout(IS_LONG_HOME_VASSITANT);
                this.mButtonLight.setBrightness(TOUCHPLUS_SETTINGS_DISABLED);
            }
        }
    }

    private void setButtonLightTimeout(boolean timeout) {
        SystemProperties.set("sys.button.light.timeout", String.valueOf(timeout));
    }

    private void sendLightTimeoutMsg() {
        if (this.mButtonLight != null && this.mDeviceProvisioned) {
            this.mHandlerEx.removeMessages(MSG_BUTTON_LIGHT_TIMEOUT);
            if (this.mTrikeyNaviMode >= 0) {
                int curButtonBrightness = this.mButtonLight.getCurrentBrightness();
                int curBackBrightness = this.mBackLight.getCurrentBrightness();
                if (this.mButtonLightMode == 0) {
                    if (SystemProperties.getBoolean("sys.button.light.timeout", IS_LONG_HOME_VASSITANT) && curButtonBrightness == 0) {
                        this.mButtonLight.setBrightness(curBackBrightness);
                    }
                    setButtonLightTimeout(IS_LONG_HOME_VASSITANT);
                    Message msg = this.mHandlerEx.obtainMessage(MSG_BUTTON_LIGHT_TIMEOUT);
                    msg.setAsynchronous(TOUCHPLUS_FORCE_VIBRATION);
                    this.mHandlerEx.sendMessageDelayed(msg, 5000);
                } else if (curButtonBrightness == 0) {
                    this.mButtonLight.setBrightness(curBackBrightness);
                }
            } else {
                setButtonLightTimeout(IS_LONG_HOME_VASSITANT);
                this.mButtonLight.setBrightness(TOUCHPLUS_SETTINGS_DISABLED);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startHwVibrate(int vibrateMode) {
        if (!(isKeyguardLocked() || !this.mHapticEnabled || "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false")) || this.mVibrator == null)) {
            this.mVibrator.hwVibrate(null, vibrateMode);
        }
    }

    private boolean isMMITesting() {
        return "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false"));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        this.mTouchCountPolicy.updateTouchCountInfo();
        boolean down = event.getAction() == 0 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
        int keyCode = event.getKeyCode();
        int flags = event.getFlags();
        int deviceID = event.getDeviceId();
        if (this.mSystraceLogCompleted) {
            if (this.mCust != null) {
                this.mCust.processCustInterceptKey(keyCode, down, this.mContext);
            }
            int origKeyCode = event.getOrigKeyCode();
            Flog.i(WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManager has intercept Key : " + keyCode + ", isdown : " + down + ", flags : " + flags);
            boolean isScreenOn = (536870912 & policyFlags) != 0 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
            if ((keyCode == 26 || keyCode == 6 || keyCode == 187) && this.mFocusedWindow != null && (this.mFocusedWindow.getAttrs().hwFlags & FLOATING_MASK) == FLOATING_MASK) {
                Log.d(TAG, "power and endcall key received and passsing to user.");
                return TOUCHPLUS_SETTINGS_ENABLED;
            }
            boolean isInjected;
            boolean isWakeKeyFun;
            int i;
            boolean isWakeKey;
            int result;
            boolean handled;
            Message msg;
            boolean keyguardShow;
            boolean isIntercept;
            boolean isVolumeDownDoubleClick;
            boolean isVoiceCall;
            boolean z;
            boolean z2;
            long interval;
            long timediff;
            KeyEvent newEvent;
            if (SystemProperties.getBoolean("ro.config.hw_easywakeup", IS_LONG_HOME_VASSITANT) && this.mSystemReady) {
                if (EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate).handleWakeUpKey(event, isScreenOn ? -1 : this.mScreenOffReason)) {
                    Log.d(TAG, "EasyWakeUpManager has handled the keycode : " + event.getKeyCode());
                    return TOUCHPLUS_SETTINGS_DISABLED;
                }
            }
            if (down && event.getRepeatCount() == 0 && SystemProperties.get(VIBRATE_ON_TOUCH, "false").equals("true")) {
                if (!((keyCode == 82 && (268435456 & flags) == 0) || keyCode == 3 || keyCode == START_MODE_VOICE_WAKEUP_ONE_SHOT)) {
                    if ((policyFlags & START_MODE_QUICK_START_CALL) != 0) {
                    }
                }
                performHapticFeedbackLw(null, TOUCHPLUS_SETTINGS_ENABLED, IS_LONG_HOME_VASSITANT);
            }
            if (!(origKeyCode == 305 || origKeyCode == 306)) {
                if (origKeyCode == 307) {
                }
                isInjected = (JACK_DEVICE_ID & policyFlags) == 0 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
                isWakeKeyFun = isWakeKeyFun(keyCode);
                if ((policyFlags & TOUCHPLUS_SETTINGS_ENABLED) == 0) {
                    i = TOUCHPLUS_SETTINGS_ENABLED;
                } else {
                    i = TOUCHPLUS_SETTINGS_DISABLED;
                }
                isWakeKey = isWakeKeyFun | i;
                if ((isScreenOn || this.mHeadless) && (!isInjected || isWakeKey)) {
                    result = TOUCHPLUS_SETTINGS_DISABLED;
                    if (down) {
                        if (isWakeKey) {
                        }
                    }
                } else {
                    result = TOUCHPLUS_SETTINGS_ENABLED;
                }
                if (this.mFocusedWindow == null && (this.mFocusedWindow.getAttrs().hwFlags & 8) == 8 && ((keyCode == 25 || keyCode == 24) && "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false")))) {
                    if (isJackDeviceEvent(event.getDeviceId(), keyCode)) {
                        Log.i(TAG, "Pass jack volume event to mmi test before queueing.");
                        return TOUCHPLUS_SETTINGS_ENABLED;
                    }
                    Log.i(TAG, "Prevent hard key volume event to mmi test before queueing.");
                    return result & DEFAULT_RESULT_VALUE;
                } else if (isJackDeviceEvent(event.getDeviceId(), keyCode)) {
                    switch (keyCode) {
                        case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                        case START_MODE_VOICE_WAKEUP_ONE_SHOT /*4*/:
                        case 187:
                            if (!down && this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mHintShown && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                Log.d(TAG, "keycode: " + keyCode + " is comsumed by disable touch mode.");
                                this.mHwScreenOnProximityLock.forceShowHint();
                                this.mHintShown = TOUCHPLUS_FORCE_VIBRATION;
                                break;
                            }
                            if (deviceID > 0 && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettings.isSupportTrikey() && !isMMITesting() && keyCode == START_MODE_VOICE_WAKEUP_ONE_SHOT) {
                                if (!isTrikeyNaviKeycodeFromLON(isInjected, isExcluedBackScene())) {
                                    return TOUCHPLUS_SETTINGS_DISABLED;
                                }
                                sendLightTimeoutMsg();
                                if (down) {
                                    handled = this.mBackTrikeyHandled;
                                    if (!this.mBackTrikeyHandled) {
                                        this.mBackTrikeyHandled = TOUCHPLUS_FORCE_VIBRATION;
                                        this.mHandlerEx.removeMessages(MSG_TRIKEY_BACK_LONG_PRESS);
                                    }
                                    if (handled) {
                                        return TOUCHPLUS_SETTINGS_DISABLED;
                                    }
                                    startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                    if (this.mTrikeyNaviMode == TOUCHPLUS_SETTINGS_ENABLED) {
                                        Flog.bdReport(this.mContext, 16);
                                        sendKeyEvent(187);
                                        return TOUCHPLUS_SETTINGS_DISABLED;
                                    }
                                }
                                this.mBackTrikeyHandled = IS_LONG_HOME_VASSITANT;
                                msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_BACK_LONG_PRESS);
                                msg.setAsynchronous(TOUCHPLUS_FORCE_VIBRATION);
                                this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                                if (this.mTrikeyNaviMode == TOUCHPLUS_SETTINGS_ENABLED) {
                                    return TOUCHPLUS_SETTINGS_DISABLED;
                                }
                            }
                            if (!this.mHasNavigationBar && keyCode == START_MODE_VOICE_WAKEUP_ONE_SHOT && down) {
                                if (!isScreenInLockTaskMode()) {
                                    this.mBackKeyPress = TOUCHPLUS_FORCE_VIBRATION;
                                    this.mBackKeyPressTime = event.getDownTime();
                                    interceptBackandMenuKey();
                                    break;
                                }
                                this.mBackKeyPress = IS_LONG_HOME_VASSITANT;
                                this.mBackKeyPressTime = 0;
                                break;
                            }
                            break;
                        case HwGnssLogHandlerMsgID.INJECT_EXTRA_PARAM /*24*/:
                        case HwGnssLogErrorCode.GPS_DAILY_CNT_REPORT_FAILD /*25*/:
                        case 164:
                            if (keyCode == 25) {
                                if (down) {
                                    if (this.mHwScreenOnProximityLock == null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mVolumeDownKeyDisTouch && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                        Log.d(TAG, "keycode: KEYCODE_VOLUME_DOWN is comsumed by disable touch mode.");
                                        this.mVolumeDownKeyDisTouch = TOUCHPLUS_FORCE_VIBRATION;
                                        if (!this.mHintShown) {
                                            this.mHwScreenOnProximityLock.forceShowHint();
                                            this.mHintShown = TOUCHPLUS_FORCE_VIBRATION;
                                            break;
                                        }
                                    }
                                    if (isScreenOn && !this.mScreenRecorderVolumeDownKeyTriggered && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                        cancelPendingPowerKeyActionForDistouch();
                                        this.mScreenRecorderVolumeDownKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                                        cancelPendingScreenRecorderAction();
                                    }
                                    if (isScreenOn && !this.mSystraceLogVolumeDownKeyTriggered && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                        this.mSystraceLogVolumeDownKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                                        this.mSystraceLogFingerPrintTime = 0;
                                        this.mSystraceLogVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
                                    }
                                } else {
                                    this.mVolumeDownKeyDisTouch = IS_LONG_HOME_VASSITANT;
                                    this.mScreenRecorderVolumeDownKeyTriggered = IS_LONG_HOME_VASSITANT;
                                    cancelPendingScreenRecorderAction();
                                    this.mSystraceLogVolumeDownKeyTriggered = IS_LONG_HOME_VASSITANT;
                                }
                                keyguardShow = keyguardIsShowingTq();
                                Log.d(TAG, "interceptVolumeDownKey down=" + down + " keyguardShow=" + keyguardShow + " policyFlags=" + Integer.toHexString(policyFlags));
                                if ((!isScreenOn || keyguardShow) && !isInjected && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                    if (!isDeviceProvisioned()) {
                                        if (down) {
                                            if (event.getEventTime() - event.getDownTime() < VOLUMEDOWN_LONG_PRESS_TIMEOUT) {
                                                cancelPendingQuickCallChordAction();
                                                break;
                                            }
                                            resetVolumeDownKeyLongPressed();
                                            break;
                                        }
                                        isIntercept = IS_LONG_HOME_VASSITANT;
                                        isVolumeDownDoubleClick = IS_LONG_HOME_VASSITANT;
                                        isVoiceCall = isVoiceCall();
                                        z = isMusicActive() ? isVoiceCall : TOUCHPLUS_FORCE_VIBRATION;
                                        z2 = (isMusicActive() || !isPhoneIdle()) ? TOUCHPLUS_FORCE_VIBRATION : isVoiceCall;
                                        if (this.isVoiceRecognitionActive) {
                                            interval = event.getEventTime() - this.mLastStartVassistantServiceTime;
                                            if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MAX) {
                                                this.isVoiceRecognitionActive = IS_LONG_HOME_VASSITANT;
                                            } else if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MIN) {
                                                this.isVoiceRecognitionActive = AudioSystem.isSourceActive(6);
                                            }
                                        }
                                        Log.i(TAG, "isMusicOrFMOrVoiceCallActive=" + z + " isVoiceRecognitionActive=" + this.isVoiceRecognitionActive);
                                        if (!(z || this.isVoiceRecognitionActive || SystemProperties.getBoolean("sys.super_power_save", IS_LONG_HOME_VASSITANT))) {
                                            timediff = event.getEventTime() - this.mLastVolumeDownKeyDownTime;
                                            this.mLastVolumeDownKeyDownTime = event.getEventTime();
                                            if (timediff < VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT) {
                                                isVolumeDownDoubleClick = TOUCHPLUS_FORCE_VIBRATION;
                                                if (this.mListener == null) {
                                                    this.mListener = new ProximitySensorListener();
                                                }
                                                turnOnSensorListener();
                                                if ((!this.mIsProximity && this.mSensorRegisted) || !this.mSensorRegisted) {
                                                    Log.i(TAG, "mIsProximity " + this.mIsProximity + ", mSensorRegisted " + this.mSensorRegisted);
                                                    notifyRapidCaptureService("start");
                                                }
                                                turnOffSensorListener();
                                                result &= DEFAULT_RESULT_VALUE;
                                            } else {
                                                notifyRapidCaptureService("wakeup");
                                                if (this.mListener == null) {
                                                    this.mListener = new ProximitySensorListener();
                                                }
                                                turnOnSensorListener();
                                            }
                                            if (!isScreenOn || isVolumeDownDoubleClick) {
                                                isIntercept = TOUCHPLUS_FORCE_VIBRATION;
                                            }
                                        }
                                        if (!(z2 || isScreenOn || isVolumeDownDoubleClick || !checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE))) {
                                            notifyVassistantService("wakeup", START_MODE_QUICK_START_CALL, event);
                                            if (this.mListener == null) {
                                                this.mListener = new ProximitySensorListener();
                                            }
                                            turnOnSensorListener();
                                            interceptQuickCallChord();
                                            isIntercept = TOUCHPLUS_FORCE_VIBRATION;
                                        }
                                        Log.i(TAG, "intercept volume down key, isIntercept=" + isIntercept + " now=" + SystemClock.uptimeMillis() + " EventTime=" + event.getEventTime());
                                        if (isIntercept) {
                                            if (getTelecommService().isInCall() && (result & TOUCHPLUS_SETTINGS_ENABLED) == 0 && this.mCust != null && this.mCust.isVolumnkeyWakeup()) {
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
                                    if (this.mHwScreenOnProximityLock == null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mVolumeUpKeyDisTouch && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                        Log.d(TAG, "keycode: KEYCODE_VOLUME_UP is comsumed by disable touch mode.");
                                        this.mVolumeUpKeyDisTouch = TOUCHPLUS_FORCE_VIBRATION;
                                        this.mVolumeUpKeyDisTouchTime = event.getDownTime();
                                        this.mVolumeUpKeyConsumedByDisTouch = IS_LONG_HOME_VASSITANT;
                                        if (!this.mHintShown) {
                                            this.mHwScreenOnProximityLock.forceShowHint();
                                            this.mHintShown = TOUCHPLUS_FORCE_VIBRATION;
                                        }
                                        cancelPendingPowerKeyActionForDistouch();
                                        interceptTouchDisableMode();
                                        break;
                                    }
                                    if (isScreenOn && !this.mScreenRecorderVolumeUpKeyTriggered && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                        cancelPendingPowerKeyActionForDistouch();
                                        this.mScreenRecorderVolumeUpKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                                        this.mScreenRecorderVolumeUpKeyTime = event.getDownTime();
                                        this.mScreenRecorderVolumeUpKeyConsumed = IS_LONG_HOME_VASSITANT;
                                        interceptScreenRecorder();
                                    }
                                    Log.d(TAG, "isScreenOn=" + isScreenOn + " mSystraceLogVolumeUpKeyTriggered=" + this.mSystraceLogVolumeUpKeyTriggered + " mScreenRecorderVolumeUpKeyConsumed=" + this.mScreenRecorderVolumeUpKeyConsumed);
                                    if (Jlog.isEnable() && Jlog.isBetaUser() && isScreenOn && !this.mSystraceLogVolumeUpKeyTriggered && !this.mSystraceLogPowerKeyTriggered && !this.mSystraceLogVolumeDownKeyTriggered && !this.mScreenRecorderVolumeUpKeyConsumed && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                        this.mSystraceLogVolumeUpKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                                        this.mSystraceLogVolumeUpKeyTime = event.getDownTime();
                                        this.mSystraceLogVolumeUpKeyConsumed = IS_LONG_HOME_VASSITANT;
                                        interceptSystraceLog();
                                        Log.d(TAG, "volumeup process: fingerprint first, then volumeup");
                                        if (this.mSystraceLogVolumeUpKeyConsumed) {
                                            return result & DEFAULT_RESULT_VALUE;
                                        }
                                    }
                                    if (getTelecommService().isInCall() && (result & TOUCHPLUS_SETTINGS_ENABLED) == 0 && this.mCust != null && this.mCust.isVolumnkeyWakeup()) {
                                        this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                    }
                                } else {
                                    this.mVolumeUpKeyDisTouch = IS_LONG_HOME_VASSITANT;
                                    this.mScreenRecorderVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
                                    cancelPendingScreenRecorderAction();
                                    this.mSystraceLogVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
                                }
                                if (this.mCust != null) {
                                    if (this.mCust.interceptVolumeUpKey(event, this.mContext, isScreenOn, keyguardIsShowingTq(), isMusicActive() ? isVoiceCall() : TOUCHPLUS_FORCE_VIBRATION, isInjected, down)) {
                                        return result;
                                    }
                                }
                            }
                            break;
                        case HwGnssLogErrorCode.GPS_NTP_WRONG /*26*/:
                            cancelSmartKeyLongPressed();
                            if (!down) {
                                if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mPowerKeyDisTouch && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                    this.mPowerKeyDisTouch = TOUCHPLUS_FORCE_VIBRATION;
                                    this.mPowerKeyDisTouchTime = event.getDownTime();
                                    interceptTouchDisableMode();
                                }
                                if (isScreenOn && !this.mScreenRecorderPowerKeyTriggered && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                    this.mScreenRecorderPowerKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                                    this.mScreenRecorderPowerKeyTime = event.getDownTime();
                                    interceptScreenRecorder();
                                }
                                if (isScreenOn && !this.mSystraceLogPowerKeyTriggered && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                    this.mSystraceLogPowerKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                                    this.mSystraceLogFingerPrintTime = 0;
                                    this.mSystraceLogVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
                                    break;
                                }
                            }
                            this.mPowerKeyDisTouch = IS_LONG_HOME_VASSITANT;
                            this.mScreenRecorderPowerKeyTriggered = IS_LONG_HOME_VASSITANT;
                            cancelPendingScreenRecorderAction();
                            this.mSystraceLogPowerKeyTriggered = IS_LONG_HOME_VASSITANT;
                            break;
                        case 82:
                            if (!this.mHasNavigationBar && down) {
                                if (!isScreenInLockTaskMode()) {
                                    this.mMenuKeyPress = TOUCHPLUS_FORCE_VIBRATION;
                                    this.mMenuKeyPressTime = event.getDownTime();
                                    interceptBackandMenuKey();
                                    break;
                                }
                                this.mMenuKeyPress = IS_LONG_HOME_VASSITANT;
                                this.mMenuKeyPressTime = 0;
                                break;
                            }
                        case MemoryConstant.MSG_DIRECT_SWAPPINESS /*303*/:
                            if (mTplusEnabled && down) {
                                if (event.getEventTime() - this.mLastTouchDownUpLeftKeyDownTime < VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT) {
                                    this.isTouchDownUpLeftDoubleClick = TOUCHPLUS_FORCE_VIBRATION;
                                    this.mTouchDownUpLeftConsumeCount = START_MODE_QUICK_START_CALL;
                                    notifyTouchplusService(MemoryConstant.MSG_DIRECT_SWAPPINESS, TOUCHPLUS_SETTINGS_ENABLED);
                                }
                                this.mLastTouchDownUpLeftKeyDownTime = event.getEventTime();
                                break;
                            }
                        case MemoryConstant.MSG_FILECACHE_NODE_SET_PROTECT_LRU /*304*/:
                            if (mTplusEnabled && down) {
                                if (event.getEventTime() - this.mLastTouchDownUpRightKeyDownTime < VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT) {
                                    this.isTouchDownUpRightDoubleClick = TOUCHPLUS_FORCE_VIBRATION;
                                    this.mTouchDownUpRightConsumeCount = START_MODE_QUICK_START_CALL;
                                    notifyTouchplusService(MemoryConstant.MSG_FILECACHE_NODE_SET_PROTECT_LRU, TOUCHPLUS_SETTINGS_ENABLED);
                                }
                                this.mLastTouchDownUpRightKeyDownTime = event.getEventTime();
                                break;
                            }
                        case 308:
                            Log.i(TAG, "KeyEvent.KEYCODE_SMARTKEY in");
                            if (!down) {
                                if (this.mHintShown) {
                                    this.mHintShown = IS_LONG_HOME_VASSITANT;
                                    return TOUCHPLUS_SETTINGS_DISABLED;
                                }
                            } else if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mHintShown && (event.getFlags() & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
                                Log.d(TAG, "keycode: " + keyCode + " is comsumed by disable touch mode.");
                                this.mHwScreenOnProximityLock.forceShowHint();
                                this.mHintShown = TOUCHPLUS_FORCE_VIBRATION;
                                return TOUCHPLUS_SETTINGS_DISABLED;
                            }
                            if (!isScreenOn) {
                                handleSmartKey(this.mContext, event, this.mHandler, isScreenOn);
                                return TOUCHPLUS_SETTINGS_DISABLED;
                            }
                            break;
                        case 401:
                        case 402:
                        case 403:
                        case 404:
                        case 405:
                            processing_KEYCODE_SOUNDTRIGGER_EVENT(keyCode, this.mContext, isMusicActive(), down, keyguardIsShowingTq());
                            break;
                        case 501:
                        case 502:
                        case 511:
                        case HwGlobalActionsData.FLAG_SILENTMODE_VIBRATE /*512*/:
                        case 513:
                        case 514:
                        case WifiProCommonUtils.RESP_CODE_UNSTABLE /*601*/:
                            Log.d(TAG, "event.flags=" + flags + " previous mSystraceLogFingerPrintTime=" + this.mSystraceLogFingerPrintTime);
                            if (flags == 8) {
                                if (Jlog.isEnable() || !Jlog.isBetaUser() || !down || !isScreenOn || this.mSystraceLogPowerKeyTriggered || this.mSystraceLogVolumeDownKeyTriggered) {
                                    return result & DEFAULT_RESULT_VALUE;
                                }
                                this.mSystraceLogFingerPrintTime = event.getDownTime();
                                return result & DEFAULT_RESULT_VALUE;
                            }
                            break;
                    }
                    return super.interceptKeyBeforeQueueing(event, policyFlags);
                } else {
                    newEvent = new KeyEvent(event.getDownTime(), event.getEventTime(), event.getAction(), event.getKeyCode(), event.getRepeatCount(), event.getMetaState(), event.getDeviceId() & -16777217, event.getScanCode(), event.getFlags(), event.getSource());
                    Log.d(TAG, "transfer volume event to internet audio :" + event);
                    msg = this.mHandlerEx.obtainMessage(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP, newEvent);
                    msg.setAsynchronous(TOUCHPLUS_FORCE_VIBRATION);
                    msg.sendToTarget();
                    return result & DEFAULT_RESULT_VALUE;
                }
            }
            if (mTplusEnabled && !isRinging()) {
                ContentResolver resolver = this.mContext.getContentResolver();
                int value = System.getInt(resolver, "hw_membrane_touch_enabled", TOUCHPLUS_SETTINGS_DISABLED);
                if (value == 0 && down) {
                    notifyTouchplusService(START_MODE_VOICE_WAKEUP_ONE_SHOT, TOUCHPLUS_SETTINGS_DISABLED);
                }
                int navibaron = System.getInt(resolver, "hw_membrane_touch_navbar_enabled", TOUCHPLUS_SETTINGS_DISABLED);
                if (down && TOUCHPLUS_SETTINGS_ENABLED == value && TOUCHPLUS_SETTINGS_ENABLED == navibaron) {
                    if (System.getInt(resolver, TOUCHPLUS_SETTINGS_VIBRATION, TOUCHPLUS_SETTINGS_ENABLED) == TOUCHPLUS_SETTINGS_ENABLED) {
                        Log.v(TAG, "vibration is not disabled by user");
                        performHapticFeedbackLw(null, TOUCHPLUS_SETTINGS_ENABLED, TOUCHPLUS_FORCE_VIBRATION);
                    }
                }
            }
            if ((JACK_DEVICE_ID & policyFlags) == 0) {
            }
            isWakeKeyFun = isWakeKeyFun(keyCode);
            if ((policyFlags & TOUCHPLUS_SETTINGS_ENABLED) == 0) {
                i = TOUCHPLUS_SETTINGS_DISABLED;
            } else {
                i = TOUCHPLUS_SETTINGS_ENABLED;
            }
            isWakeKey = isWakeKeyFun | i;
            if (isScreenOn) {
            }
            result = TOUCHPLUS_SETTINGS_DISABLED;
            if (down) {
                if (isWakeKey) {
                }
            }
            if (this.mFocusedWindow == null) {
            }
            if (isJackDeviceEvent(event.getDeviceId(), keyCode)) {
                switch (keyCode) {
                    case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                    case START_MODE_VOICE_WAKEUP_ONE_SHOT /*4*/:
                    case 187:
                        if (!down) {
                            break;
                        }
                        if (!isTrikeyNaviKeycodeFromLON(isInjected, isExcluedBackScene())) {
                            sendLightTimeoutMsg();
                            if (down) {
                                handled = this.mBackTrikeyHandled;
                                if (this.mBackTrikeyHandled) {
                                    this.mBackTrikeyHandled = TOUCHPLUS_FORCE_VIBRATION;
                                    this.mHandlerEx.removeMessages(MSG_TRIKEY_BACK_LONG_PRESS);
                                }
                                if (handled) {
                                    return TOUCHPLUS_SETTINGS_DISABLED;
                                }
                                startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                if (this.mTrikeyNaviMode == TOUCHPLUS_SETTINGS_ENABLED) {
                                    Flog.bdReport(this.mContext, 16);
                                    sendKeyEvent(187);
                                    return TOUCHPLUS_SETTINGS_DISABLED;
                                }
                            }
                            this.mBackTrikeyHandled = IS_LONG_HOME_VASSITANT;
                            msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_BACK_LONG_PRESS);
                            msg.setAsynchronous(TOUCHPLUS_FORCE_VIBRATION);
                            this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                            if (this.mTrikeyNaviMode == TOUCHPLUS_SETTINGS_ENABLED) {
                                return TOUCHPLUS_SETTINGS_DISABLED;
                            }
                            if (!isScreenInLockTaskMode()) {
                                this.mBackKeyPress = IS_LONG_HOME_VASSITANT;
                                this.mBackKeyPressTime = 0;
                                break;
                            }
                            this.mBackKeyPress = TOUCHPLUS_FORCE_VIBRATION;
                            this.mBackKeyPressTime = event.getDownTime();
                            interceptBackandMenuKey();
                            break;
                            break;
                        }
                        return TOUCHPLUS_SETTINGS_DISABLED;
                        break;
                    case HwGnssLogHandlerMsgID.INJECT_EXTRA_PARAM /*24*/:
                    case HwGnssLogErrorCode.GPS_DAILY_CNT_REPORT_FAILD /*25*/:
                    case 164:
                        if (keyCode == 25) {
                            if (down) {
                                if (this.mHwScreenOnProximityLock == null) {
                                    break;
                                }
                                cancelPendingPowerKeyActionForDistouch();
                                this.mScreenRecorderVolumeDownKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                                cancelPendingScreenRecorderAction();
                                this.mSystraceLogVolumeDownKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                                this.mSystraceLogFingerPrintTime = 0;
                                this.mSystraceLogVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
                                break;
                            }
                            this.mVolumeDownKeyDisTouch = IS_LONG_HOME_VASSITANT;
                            this.mScreenRecorderVolumeDownKeyTriggered = IS_LONG_HOME_VASSITANT;
                            cancelPendingScreenRecorderAction();
                            this.mSystraceLogVolumeDownKeyTriggered = IS_LONG_HOME_VASSITANT;
                            keyguardShow = keyguardIsShowingTq();
                            Log.d(TAG, "interceptVolumeDownKey down=" + down + " keyguardShow=" + keyguardShow + " policyFlags=" + Integer.toHexString(policyFlags));
                            if (!isDeviceProvisioned()) {
                                if (down) {
                                    if (event.getEventTime() - event.getDownTime() < VOLUMEDOWN_LONG_PRESS_TIMEOUT) {
                                        resetVolumeDownKeyLongPressed();
                                        break;
                                    }
                                    cancelPendingQuickCallChordAction();
                                    break;
                                }
                                isIntercept = IS_LONG_HOME_VASSITANT;
                                isVolumeDownDoubleClick = IS_LONG_HOME_VASSITANT;
                                isVoiceCall = isVoiceCall();
                                if (isMusicActive()) {
                                }
                                if (!isMusicActive()) {
                                    break;
                                }
                                if (this.isVoiceRecognitionActive) {
                                    interval = event.getEventTime() - this.mLastStartVassistantServiceTime;
                                    if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MAX) {
                                        this.isVoiceRecognitionActive = IS_LONG_HOME_VASSITANT;
                                    } else if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MIN) {
                                        this.isVoiceRecognitionActive = AudioSystem.isSourceActive(6);
                                    }
                                }
                                Log.i(TAG, "isMusicOrFMOrVoiceCallActive=" + z + " isVoiceRecognitionActive=" + this.isVoiceRecognitionActive);
                                timediff = event.getEventTime() - this.mLastVolumeDownKeyDownTime;
                                this.mLastVolumeDownKeyDownTime = event.getEventTime();
                                if (timediff < VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT) {
                                    isVolumeDownDoubleClick = TOUCHPLUS_FORCE_VIBRATION;
                                    if (this.mListener == null) {
                                        this.mListener = new ProximitySensorListener();
                                    }
                                    turnOnSensorListener();
                                    Log.i(TAG, "mIsProximity " + this.mIsProximity + ", mSensorRegisted " + this.mSensorRegisted);
                                    notifyRapidCaptureService("start");
                                    turnOffSensorListener();
                                    result &= DEFAULT_RESULT_VALUE;
                                    break;
                                }
                                notifyRapidCaptureService("wakeup");
                                if (this.mListener == null) {
                                    this.mListener = new ProximitySensorListener();
                                }
                                turnOnSensorListener();
                                break;
                                isIntercept = TOUCHPLUS_FORCE_VIBRATION;
                                notifyVassistantService("wakeup", START_MODE_QUICK_START_CALL, event);
                                if (this.mListener == null) {
                                    this.mListener = new ProximitySensorListener();
                                }
                                turnOnSensorListener();
                                interceptQuickCallChord();
                                isIntercept = TOUCHPLUS_FORCE_VIBRATION;
                                Log.i(TAG, "intercept volume down key, isIntercept=" + isIntercept + " now=" + SystemClock.uptimeMillis() + " EventTime=" + event.getEventTime());
                                if (isIntercept) {
                                    this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                    break;
                                }
                                return result;
                                break;
                            }
                            Log.i(TAG, "Device is not Provisioned");
                            break;
                            break;
                        } else if (keyCode == 24) {
                            if (down) {
                                if (this.mHwScreenOnProximityLock == null) {
                                    break;
                                }
                                cancelPendingPowerKeyActionForDistouch();
                                this.mScreenRecorderVolumeUpKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                                this.mScreenRecorderVolumeUpKeyTime = event.getDownTime();
                                this.mScreenRecorderVolumeUpKeyConsumed = IS_LONG_HOME_VASSITANT;
                                interceptScreenRecorder();
                                Log.d(TAG, "isScreenOn=" + isScreenOn + " mSystraceLogVolumeUpKeyTriggered=" + this.mSystraceLogVolumeUpKeyTriggered + " mScreenRecorderVolumeUpKeyConsumed=" + this.mScreenRecorderVolumeUpKeyConsumed);
                                this.mSystraceLogVolumeUpKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                                this.mSystraceLogVolumeUpKeyTime = event.getDownTime();
                                this.mSystraceLogVolumeUpKeyConsumed = IS_LONG_HOME_VASSITANT;
                                interceptSystraceLog();
                                Log.d(TAG, "volumeup process: fingerprint first, then volumeup");
                                if (this.mSystraceLogVolumeUpKeyConsumed) {
                                    return result & DEFAULT_RESULT_VALUE;
                                }
                                this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                break;
                            }
                            this.mVolumeUpKeyDisTouch = IS_LONG_HOME_VASSITANT;
                            this.mScreenRecorderVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
                            cancelPendingScreenRecorderAction();
                            this.mSystraceLogVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
                            if (this.mCust != null) {
                                if (isMusicActive()) {
                                }
                                if (this.mCust.interceptVolumeUpKey(event, this.mContext, isScreenOn, keyguardIsShowingTq(), isMusicActive() ? isVoiceCall() : TOUCHPLUS_FORCE_VIBRATION, isInjected, down)) {
                                    return result;
                                }
                            }
                        }
                        break;
                    case HwGnssLogErrorCode.GPS_NTP_WRONG /*26*/:
                        cancelSmartKeyLongPressed();
                        if (!down) {
                            this.mPowerKeyDisTouch = IS_LONG_HOME_VASSITANT;
                            this.mScreenRecorderPowerKeyTriggered = IS_LONG_HOME_VASSITANT;
                            cancelPendingScreenRecorderAction();
                            this.mSystraceLogPowerKeyTriggered = IS_LONG_HOME_VASSITANT;
                            break;
                        }
                        this.mPowerKeyDisTouch = TOUCHPLUS_FORCE_VIBRATION;
                        this.mPowerKeyDisTouchTime = event.getDownTime();
                        interceptTouchDisableMode();
                        this.mScreenRecorderPowerKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                        this.mScreenRecorderPowerKeyTime = event.getDownTime();
                        interceptScreenRecorder();
                        this.mSystraceLogPowerKeyTriggered = TOUCHPLUS_FORCE_VIBRATION;
                        this.mSystraceLogFingerPrintTime = 0;
                        this.mSystraceLogVolumeUpKeyTriggered = IS_LONG_HOME_VASSITANT;
                        break;
                    case 82:
                        if (!isScreenInLockTaskMode()) {
                            this.mMenuKeyPress = IS_LONG_HOME_VASSITANT;
                            this.mMenuKeyPressTime = 0;
                            break;
                        }
                        this.mMenuKeyPress = TOUCHPLUS_FORCE_VIBRATION;
                        this.mMenuKeyPressTime = event.getDownTime();
                        interceptBackandMenuKey();
                        break;
                        break;
                    case MemoryConstant.MSG_DIRECT_SWAPPINESS /*303*/:
                        if (event.getEventTime() - this.mLastTouchDownUpLeftKeyDownTime < VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT) {
                            this.isTouchDownUpLeftDoubleClick = TOUCHPLUS_FORCE_VIBRATION;
                            this.mTouchDownUpLeftConsumeCount = START_MODE_QUICK_START_CALL;
                            notifyTouchplusService(MemoryConstant.MSG_DIRECT_SWAPPINESS, TOUCHPLUS_SETTINGS_ENABLED);
                        }
                        this.mLastTouchDownUpLeftKeyDownTime = event.getEventTime();
                        break;
                    case MemoryConstant.MSG_FILECACHE_NODE_SET_PROTECT_LRU /*304*/:
                        if (event.getEventTime() - this.mLastTouchDownUpRightKeyDownTime < VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT) {
                            this.isTouchDownUpRightDoubleClick = TOUCHPLUS_FORCE_VIBRATION;
                            this.mTouchDownUpRightConsumeCount = START_MODE_QUICK_START_CALL;
                            notifyTouchplusService(MemoryConstant.MSG_FILECACHE_NODE_SET_PROTECT_LRU, TOUCHPLUS_SETTINGS_ENABLED);
                        }
                        this.mLastTouchDownUpRightKeyDownTime = event.getEventTime();
                        break;
                    case 308:
                        Log.i(TAG, "KeyEvent.KEYCODE_SMARTKEY in");
                        if (!down) {
                            Log.d(TAG, "keycode: " + keyCode + " is comsumed by disable touch mode.");
                            this.mHwScreenOnProximityLock.forceShowHint();
                            this.mHintShown = TOUCHPLUS_FORCE_VIBRATION;
                            return TOUCHPLUS_SETTINGS_DISABLED;
                        } else if (this.mHintShown) {
                            this.mHintShown = IS_LONG_HOME_VASSITANT;
                            return TOUCHPLUS_SETTINGS_DISABLED;
                        }
                        if (isScreenOn) {
                            handleSmartKey(this.mContext, event, this.mHandler, isScreenOn);
                            return TOUCHPLUS_SETTINGS_DISABLED;
                        }
                        break;
                    case 401:
                    case 402:
                    case 403:
                    case 404:
                    case 405:
                        processing_KEYCODE_SOUNDTRIGGER_EVENT(keyCode, this.mContext, isMusicActive(), down, keyguardIsShowingTq());
                        break;
                    case 501:
                    case 502:
                    case 511:
                    case HwGlobalActionsData.FLAG_SILENTMODE_VIBRATE /*512*/:
                    case 513:
                    case 514:
                    case WifiProCommonUtils.RESP_CODE_UNSTABLE /*601*/:
                        Log.d(TAG, "event.flags=" + flags + " previous mSystraceLogFingerPrintTime=" + this.mSystraceLogFingerPrintTime);
                        if (flags == 8) {
                            if (Jlog.isEnable()) {
                                break;
                            }
                            return result & DEFAULT_RESULT_VALUE;
                        }
                        break;
                }
                return super.interceptKeyBeforeQueueing(event, policyFlags);
            }
            newEvent = new KeyEvent(event.getDownTime(), event.getEventTime(), event.getAction(), event.getKeyCode(), event.getRepeatCount(), event.getMetaState(), event.getDeviceId() & -16777217, event.getScanCode(), event.getFlags(), event.getSource());
            Log.d(TAG, "transfer volume event to internet audio :" + event);
            msg = this.mHandlerEx.obtainMessage(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP, newEvent);
            msg.setAsynchronous(TOUCHPLUS_FORCE_VIBRATION);
            msg.sendToTarget();
            return result & DEFAULT_RESULT_VALUE;
        }
        Log.d(TAG, " has intercept Key for block : " + keyCode + ", isdown : " + down + ", flags : " + flags);
        return TOUCHPLUS_SETTINGS_DISABLED;
    }

    boolean isRinging() {
        TelecomManager telecomManager = getTelecommService();
        return (telecomManager == null || !telecomManager.isRinging()) ? IS_LONG_HOME_VASSITANT : PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get("persist.sys.show_incallscreen", PPPOEStateMachine.PHASE_DEAD));
    }

    public KeyEvent dispatchUnhandledKey(WindowState win, KeyEvent event, int policyFlags) {
        int keyCode = event.getKeyCode();
        boolean isScreenOn = (536870912 & policyFlags) != 0 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
        switch (keyCode) {
            case 308:
                if (event.getRepeatCount() != 0) {
                    Log.d(TAG, "event.getRepeatCount() != 0 so just break");
                    return null;
                } else if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", IS_LONG_HOME_VASSITANT) && needDropSmartKey()) {
                    return null;
                } else {
                    handleSmartKey(this.mContext, event, this.mHandler, isScreenOn);
                    return null;
                }
            default:
                return super.dispatchUnhandledKey(win, event, policyFlags);
        }
    }

    public long interceptKeyBeforeDispatching(WindowState win, KeyEvent event, int policyFlags) {
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int flags = event.getFlags();
        int origKeyCode = event.getOrigKeyCode();
        boolean down = event.getAction() == 0 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
        int deviceID = event.getDeviceId();
        boolean isInjected = (JACK_DEVICE_ID & policyFlags) != 0 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
        try {
            if (this.mIHwWindowCallback != null) {
                this.mIHwWindowCallback.interceptKeyBeforeDispatching(event, policyFlags);
            }
        } catch (Exception ex) {
            Log.w(TAG, "mIHwWindowCallback interceptKeyBeforeDispatching threw RemoteException", ex);
        }
        int result = getDisabledKeyEventResult(keyCode);
        if (DEFAULT_RESULT_VALUE != result) {
            return (long) result;
        }
        if ((keyCode == 3 || keyCode == 187) && win != null) {
            if ((win.getAttrs().hwFlags & FLOATING_MASK) == FLOATING_MASK) {
                return 0;
            }
        }
        if (win != null) {
            if ((win.getAttrs().hwFlags & 8) == 8 && ((keyCode == 25 || keyCode == 24) && "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false")))) {
                if (isJackDeviceEvent(event.getDeviceId(), keyCode)) {
                    Log.i(TAG, "Pass jack volume event to mmi test before dispatching.");
                    return 0;
                }
                Log.i(TAG, "Prevent hard key volume event to mmi test before dispatching.");
                return -1;
            }
        }
        if (isJackDeviceEvent(event.getDeviceId(), keyCode)) {
            Log.d(TAG, "skip volume event for jack device");
            return -1;
        }
        long now;
        long timeoutTime;
        if ((origKeyCode == 305 || origKeyCode == 306 || origKeyCode == 307) && mTplusEnabled) {
            ContentResolver resolver = this.mContext.getContentResolver();
            int touchPlusOn = System.getInt(resolver, "hw_membrane_touch_enabled", TOUCHPLUS_SETTINGS_DISABLED);
            int value = System.getInt(resolver, "hw_membrane_touch_navbar_enabled", TOUCHPLUS_SETTINGS_DISABLED);
            if (!(touchPlusOn == 0 || value == 0)) {
                if (isRinging() && origKeyCode != 307) {
                }
            }
            return -1;
        }
        if (keyCode == 303 && mTplusEnabled) {
            if (this.isTouchDownUpLeftDoubleClick) {
                if (!down) {
                    this.mTouchDownUpLeftConsumeCount--;
                    if (this.mTouchDownUpLeftConsumeCount == 0) {
                        this.isTouchDownUpLeftDoubleClick = IS_LONG_HOME_VASSITANT;
                    }
                }
                return -1;
            } else if (repeatCount == 0) {
                now = SystemClock.uptimeMillis();
                timeoutTime = event.getEventTime() + VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT;
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
                        this.isTouchDownUpRightDoubleClick = IS_LONG_HOME_VASSITANT;
                    }
                }
                return -1;
            } else if (repeatCount == 0) {
                now = SystemClock.uptimeMillis();
                timeoutTime = event.getEventTime() + VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
        }
        if (keyCode == 82) {
            if (mTplusEnabled && origKeyCode == 307 && TOUCHPLUS_SETTINGS_ENABLED == System.getInt(this.mContext.getContentResolver(), "hw_membrane_touch_navbar_enabled", TOUCHPLUS_SETTINGS_DISABLED)) {
                if (down) {
                    if (repeatCount == 0) {
                        this.mMenuClickedOnlyOnce = TOUCHPLUS_FORCE_VIBRATION;
                    } else if (repeatCount == TOUCHPLUS_SETTINGS_ENABLED) {
                        this.mMenuClickedOnlyOnce = IS_LONG_HOME_VASSITANT;
                        transactToStatusBarService(MSG_FINGERSENSE_ENABLE, "resentapp", "resentapp", TOUCHPLUS_SETTINGS_DISABLED);
                    }
                } else if (this.mMenuClickedOnlyOnce) {
                    this.mMenuClickedOnlyOnce = IS_LONG_HOME_VASSITANT;
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
                        this.mMenuClickedOnlyOnce = IS_LONG_HOME_VASSITANT;
                        sendHwMenuKeyEvent();
                    }
                    cancelPreloadRecentApps();
                } else if (repeatCount == 0) {
                    this.mMenuClickedOnlyOnce = TOUCHPLUS_FORCE_VIBRATION;
                    preloadRecentApps();
                } else if (repeatCount == TOUCHPLUS_SETTINGS_ENABLED) {
                    this.mMenuClickedOnlyOnce = IS_LONG_HOME_VASSITANT;
                    toggleRecentApps();
                }
                return -1;
            }
        }
        if (this.mVolumeUpKeyDisTouch && !this.mPowerKeyDisTouch && (flags & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
            now = SystemClock.uptimeMillis();
            timeoutTime = this.mVolumeUpKeyDisTouchTime + TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS;
            if (now < timeoutTime) {
                return timeoutTime - now;
            }
            return -1;
        }
        if (keyCode == 24) {
            if (this.mVolumeUpKeyConsumedByDisTouch) {
                if (!down) {
                    this.mVolumeUpKeyConsumedByDisTouch = IS_LONG_HOME_VASSITANT;
                    this.mHintShown = IS_LONG_HOME_VASSITANT;
                }
                return -1;
            } else if (this.mHintShown) {
                if (!down) {
                    this.mHintShown = IS_LONG_HOME_VASSITANT;
                }
                return -1;
            }
        }
        switch (keyCode) {
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
            case START_MODE_VOICE_WAKEUP_ONE_SHOT /*4*/:
            case HwGnssLogErrorCode.GPS_DAILY_CNT_REPORT_FAILD /*25*/:
            case 187:
                if (this.mHintShown) {
                    if (!down) {
                        this.mHintShown = IS_LONG_HOME_VASSITANT;
                    }
                    return -1;
                } else if (deviceID > 0 && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettings.isSupportTrikey() && !isMMITesting() && keyCode == 187) {
                    if (isTrikeyNaviKeycodeFromLON(isInjected, isExcluedRecentScene())) {
                        return -1;
                    }
                    sendLightTimeoutMsg();
                    if (!down) {
                        boolean handled = this.mRecentTrikeyHandled;
                        if (!this.mRecentTrikeyHandled) {
                            this.mRecentTrikeyHandled = TOUCHPLUS_FORCE_VIBRATION;
                            this.mHandlerEx.removeMessages(MSG_TRIKEY_RECENT_LONG_PRESS);
                        }
                        if (!handled) {
                            int i = this.mTrikeyNaviMode;
                            if (r0 == TOUCHPLUS_SETTINGS_ENABLED) {
                                startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                sendKeyEvent(START_MODE_VOICE_WAKEUP_ONE_SHOT);
                            } else if (this.mTrikeyNaviMode == 0) {
                                Flog.bdReport(this.mContext, 17);
                                startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                toggleRecentApps();
                            }
                        }
                    } else if (repeatCount == 0) {
                        this.mRecentTrikeyHandled = IS_LONG_HOME_VASSITANT;
                        Message msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_RECENT_LONG_PRESS);
                        msg.setAsynchronous(TOUCHPLUS_FORCE_VIBRATION);
                        this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                        if (this.mTrikeyNaviMode == 0) {
                            preloadRecentApps();
                        }
                    }
                    return -1;
                }
        }
        if ((flags & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) == 0) {
            if (this.mScreenRecorderVolumeUpKeyTriggered && !this.mScreenRecorderPowerKeyTriggered) {
                now = SystemClock.uptimeMillis();
                timeoutTime = this.mScreenRecorderVolumeUpKeyTime + TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
            if (keyCode == 24 && this.mScreenRecorderVolumeUpKeyConsumed) {
                if (!down) {
                    this.mScreenRecorderVolumeUpKeyConsumed = IS_LONG_HOME_VASSITANT;
                }
                return -1;
            }
            if (this.mSystraceLogVolumeUpKeyTriggered) {
                now = SystemClock.uptimeMillis();
                timeoutTime = this.mSystraceLogVolumeUpKeyTime + TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS;
                if (now < timeoutTime) {
                    Log.d(TAG, "keyCode=" + keyCode + " down=" + down + " in queue: now=" + now + " timeout=" + timeoutTime);
                    return timeoutTime - now;
                }
            }
            if (keyCode == 24 && this.mSystraceLogVolumeUpKeyConsumed) {
                if (!down) {
                    this.mSystraceLogVolumeUpKeyConsumed = IS_LONG_HOME_VASSITANT;
                }
                Log.d(TAG, "systracelog volumeup down=" + down + " leave queue");
                return -1;
            }
        }
        return super.interceptKeyBeforeDispatching(win, event, policyFlags);
    }

    private final void sendHwMenuKeyEvent() {
        int[] actions = new int[]{TOUCHPLUS_SETTINGS_DISABLED, TOUCHPLUS_SETTINGS_ENABLED};
        for (int i = TOUCHPLUS_SETTINGS_DISABLED; i < actions.length; i += TOUCHPLUS_SETTINGS_ENABLED) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, actions[i], 82, TOUCHPLUS_SETTINGS_DISABLED, TOUCHPLUS_SETTINGS_DISABLED, -1, TOUCHPLUS_SETTINGS_DISABLED, 268435464, 257), TOUCHPLUS_SETTINGS_DISABLED);
        }
    }

    protected void launchAssistAction(String hint, int deviceId) {
        if (checkPackageInstalled("com.google.android.googlequicksearchbox")) {
            super.launchAssistAction(hint, deviceId);
            return;
        }
        sendCloseSystemWindows();
        boolean enableVoiceAssistant = Secure.getInt(this.mContext.getContentResolver(), "hw_long_home_voice_assistant", TOUCHPLUS_SETTINGS_DISABLED) == TOUCHPLUS_SETTINGS_ENABLED ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
        if (IS_LONG_HOME_VASSITANT && enableVoiceAssistant) {
            performHapticFeedbackLw(null, TOUCHPLUS_SETTINGS_DISABLED, IS_LONG_HOME_VASSITANT);
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
            this.mContext.getPackageManager().getPackageInfo(packageName, HwSecDiagnoseConstant.BIT_VERIFYBOOT);
            return TOUCHPLUS_FORCE_VIBRATION;
        } catch (NameNotFoundException e) {
            return IS_LONG_HOME_VASSITANT;
        }
    }

    private boolean isHwDarkTheme(Context context, int themeId) {
        boolean z = IS_LONG_HOME_VASSITANT;
        try {
            if (context.getResources().getResourceName(themeId).indexOf("Emui.Dark") >= 0) {
                z = TOUCHPLUS_FORCE_VIBRATION;
            }
            return z;
        } catch (Exception e) {
            return IS_LONG_HOME_VASSITANT;
        }
    }

    boolean isMusicActive() {
        if (((AudioManager) this.mContext.getSystemService("audio")) != null) {
            return AudioSystem.isStreamActive(3, TOUCHPLUS_SETTINGS_DISABLED);
        }
        Log.w(TAG, "isMusicActive: couldn't get AudioManager reference");
        return IS_LONG_HOME_VASSITANT;
    }

    boolean isDeviceProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", TOUCHPLUS_SETTINGS_DISABLED) != 0) {
            return TOUCHPLUS_FORCE_VIBRATION;
        }
        return IS_LONG_HOME_VASSITANT;
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
                    i = TOUCHPLUS_SETTINGS_ENABLED;
                } else {
                    i = -1;
                }
                audioService.adjustStreamVolume(stream, i, TOUCHPLUS_SETTINGS_DISABLED, this.mContext.getOpPackageName());
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
        this.mHandler.postDelayed(this.mHandleVolumeDownKey, VOLUMEDOWN_LONG_PRESS_TIMEOUT);
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
        this.mHandler.postDelayed(this.mVolumeDownLongPressed, VOLUMEDOWN_LONG_PRESS_TIMEOUT);
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
            this.mBroadcastWakeLock = this.mPowerManager.newWakeLock(TOUCHPLUS_SETTINGS_ENABLED, "HwPhoneWindowManager.mBroadcastWakeLock");
            this.mVolumeDownWakeLock = this.mPowerManager.newWakeLock(TOUCHPLUS_SETTINGS_ENABLED, "HwPhoneWindowManager.mVolumeDownWakeLock");
        }
        this.mHeadless = PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get("ro.config.headless", PPPOEStateMachine.PHASE_DEAD));
    }

    private void notifyRapidCaptureService(String command) {
        if (this.mSystemReady) {
            Intent intent = new Intent(HUAWEI_RAPIDCAPTURE_START_MODE);
            intent.setPackage("com.huawei.camera");
            intent.putExtra(SMARTKEY_TAG, command);
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
            if (this.mVolumeDownWakeLock != null) {
                this.mVolumeDownWakeLock.acquire(VOLUMEDOWN_LONG_PRESS_TIMEOUT);
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
        intent.putExtra(SearchPanelView.INTENT_KEY, kcode);
        intent.putExtra("keyvalue", kval);
        intent.setPackage("com.huawei.membranetouch");
        this.mContext.startService(intent);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                statusBarServiceBinder.transact(code, data, reply, TOUCHPLUS_SETTINGS_DISABLED);
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService four params->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    statusBarServiceBinder.transact(code, data, reply, TOUCHPLUS_SETTINGS_DISABLED);
                }
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void updateSystemUiColorLw(WindowState win) {
        if (win != null) {
            LayoutParams attrs = win.getAttrs();
            WindowState windowState = this.mLastColorWin;
            if (r0 != win || this.mLastStatusBarColor != attrs.statusBarColor || this.mLastNavigationBarColor != attrs.navigationBarColor) {
                boolean isPopup;
                boolean isTouchExplrEnabled;
                int isEmuiStyle;
                int statusBarColor;
                int navigationBarColor;
                int isEmuiLightStyle;
                boolean colorChanged;
                boolean z;
                boolean styleChanged;
                boolean isStatusBarOrKeyGuard;
                boolean isInMultiWindowMode;
                boolean z2;
                boolean isFloating = getFloatingValue(attrs.isEmuiStyle);
                int i = attrs.type;
                if (r0 != 1000) {
                    i = attrs.type;
                    if (r0 != 1002) {
                        i = attrs.type;
                        if (r0 != 2009) {
                            i = attrs.type;
                            if (r0 != 2010) {
                                i = attrs.type;
                                isPopup = r0 == 2003 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
                                i = attrs.type;
                                if (r0 != 3) {
                                }
                                isTouchExplrEnabled = this.mAccessibilityManager.isTouchExplorationEnabled();
                                isEmuiStyle = getEmuiStyleValue(attrs.isEmuiStyle);
                                statusBarColor = attrs.statusBarColor;
                                navigationBarColor = attrs.navigationBarColor;
                                isEmuiLightStyle = getEmuiLightStyleValue(attrs.hwFlags);
                                if (isTouchExplrEnabled) {
                                    i = this.mLastStatusBarColor;
                                    if (r0 != statusBarColor) {
                                        i = this.mLastNavigationBarColor;
                                        if (r0 == navigationBarColor) {
                                            colorChanged = TOUCHPLUS_FORCE_VIBRATION;
                                        } else {
                                            colorChanged = IS_LONG_HOME_VASSITANT;
                                        }
                                    } else {
                                        colorChanged = TOUCHPLUS_FORCE_VIBRATION;
                                    }
                                } else {
                                    z = this.mIsTouchExplrEnabled;
                                    colorChanged = isTouchExplrEnabled == r0 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
                                    isEmuiStyle = DEFAULT_RESULT_VALUE;
                                    isEmuiLightStyle = -1;
                                }
                                styleChanged = isEmuiStyleChanged(isEmuiStyle, isEmuiLightStyle);
                                if (win != this.mStatusBar) {
                                    i = attrs.type;
                                    if (r0 != 2024) {
                                        isStatusBarOrKeyGuard = isKeyguardHostWindow(attrs);
                                        i = attrs.type;
                                        isInMultiWindowMode = (isStatusBarOrKeyGuard || isFloating || isPopup || (r0 != 2034 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT)) ? TOUCHPLUS_FORCE_VIBRATION : win.getStackId() == 3 ? win.isInMultiWindowMode() : IS_LONG_HOME_VASSITANT;
                                        z2 = (styleChanged || isInMultiWindowMode) ? (styleChanged || isInMultiWindowMode) ? IS_LONG_HOME_VASSITANT : colorChanged : TOUCHPLUS_FORCE_VIBRATION;
                                        if (!isInMultiWindowMode) {
                                            win.setCanCarryColors(TOUCHPLUS_FORCE_VIBRATION);
                                        }
                                        if (z2) {
                                            if (isTouchExplrEnabled) {
                                                i = statusBarColor;
                                            } else {
                                                i = TOUCH_EXPLR_STATUS_BAR_COLOR;
                                            }
                                            this.mLastStatusBarColor = i;
                                            if (isTouchExplrEnabled) {
                                                i = navigationBarColor;
                                            } else {
                                                i = TOUCH_EXPLR_STATUS_BAR_COLOR;
                                            }
                                            this.mLastNavigationBarColor = i;
                                            this.mLastIsEmuiStyle = isEmuiStyle;
                                            this.mIsTouchExplrEnabled = isTouchExplrEnabled;
                                            this.mLastColorWin = win;
                                            this.mLastIsEmuiLightStyle = isEmuiLightStyle;
                                            Slog.v(TAG, "updateSystemUiColorLw window=" + win + ",EmuiStyle=" + isEmuiStyle + ",StatusBarColor=0x" + Integer.toHexString(statusBarColor) + ",NavigationBarColor=0x" + Integer.toHexString(navigationBarColor) + ", mLastIsEmuiLightStyle=" + this.mLastIsEmuiLightStyle);
                                            this.mHandler.post(new Runnable() {
                                                public void run() {
                                                    HwPhoneWindowManager.this.transactToStatusBarService(CPUFeature.MSG_PROCESS_GROUP_CHANGE, "setSystemUIColor", HwPhoneWindowManager.this.mLastIsEmuiStyle, HwPhoneWindowManager.this.mLastStatusBarColor, HwPhoneWindowManager.this.mLastNavigationBarColor, HwPhoneWindowManager.this.mLastIsEmuiLightStyle);
                                                }
                                            });
                                        }
                                    }
                                }
                                isStatusBarOrKeyGuard = TOUCHPLUS_FORCE_VIBRATION;
                                i = attrs.type;
                                if (r0 != 2034) {
                                }
                                if (win.getStackId() == 3) {
                                }
                                if (!isStatusBarOrKeyGuard) {
                                }
                                if (styleChanged) {
                                }
                                if (!styleChanged) {
                                }
                                if (isInMultiWindowMode) {
                                    win.setCanCarryColors(TOUCHPLUS_FORCE_VIBRATION);
                                }
                                if (z2) {
                                    if (isTouchExplrEnabled) {
                                        i = statusBarColor;
                                    } else {
                                        i = TOUCH_EXPLR_STATUS_BAR_COLOR;
                                    }
                                    this.mLastStatusBarColor = i;
                                    if (isTouchExplrEnabled) {
                                        i = navigationBarColor;
                                    } else {
                                        i = TOUCH_EXPLR_STATUS_BAR_COLOR;
                                    }
                                    this.mLastNavigationBarColor = i;
                                    this.mLastIsEmuiStyle = isEmuiStyle;
                                    this.mIsTouchExplrEnabled = isTouchExplrEnabled;
                                    this.mLastColorWin = win;
                                    this.mLastIsEmuiLightStyle = isEmuiLightStyle;
                                    Slog.v(TAG, "updateSystemUiColorLw window=" + win + ",EmuiStyle=" + isEmuiStyle + ",StatusBarColor=0x" + Integer.toHexString(statusBarColor) + ",NavigationBarColor=0x" + Integer.toHexString(navigationBarColor) + ", mLastIsEmuiLightStyle=" + this.mLastIsEmuiLightStyle);
                                    this.mHandler.post(new Runnable() {
                                        public void run() {
                                            HwPhoneWindowManager.this.transactToStatusBarService(CPUFeature.MSG_PROCESS_GROUP_CHANGE, "setSystemUIColor", HwPhoneWindowManager.this.mLastIsEmuiStyle, HwPhoneWindowManager.this.mLastStatusBarColor, HwPhoneWindowManager.this.mLastNavigationBarColor, HwPhoneWindowManager.this.mLastIsEmuiLightStyle);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
                isPopup = TOUCHPLUS_FORCE_VIBRATION;
                i = attrs.type;
                if (r0 != 3) {
                }
                isTouchExplrEnabled = this.mAccessibilityManager.isTouchExplorationEnabled();
                isEmuiStyle = getEmuiStyleValue(attrs.isEmuiStyle);
                statusBarColor = attrs.statusBarColor;
                navigationBarColor = attrs.navigationBarColor;
                isEmuiLightStyle = getEmuiLightStyleValue(attrs.hwFlags);
                if (isTouchExplrEnabled) {
                    z = this.mIsTouchExplrEnabled;
                    if (isTouchExplrEnabled == r0) {
                    }
                    isEmuiStyle = DEFAULT_RESULT_VALUE;
                    isEmuiLightStyle = -1;
                } else {
                    i = this.mLastStatusBarColor;
                    if (r0 != statusBarColor) {
                        colorChanged = TOUCHPLUS_FORCE_VIBRATION;
                    } else {
                        i = this.mLastNavigationBarColor;
                        if (r0 == navigationBarColor) {
                            colorChanged = IS_LONG_HOME_VASSITANT;
                        } else {
                            colorChanged = TOUCHPLUS_FORCE_VIBRATION;
                        }
                    }
                }
                styleChanged = isEmuiStyleChanged(isEmuiStyle, isEmuiLightStyle);
                if (win != this.mStatusBar) {
                    i = attrs.type;
                    if (r0 != 2024) {
                        isStatusBarOrKeyGuard = isKeyguardHostWindow(attrs);
                        i = attrs.type;
                        if (r0 != 2034) {
                        }
                        if (win.getStackId() == 3) {
                        }
                        if (isStatusBarOrKeyGuard) {
                        }
                        if (styleChanged) {
                        }
                        if (styleChanged) {
                        }
                        if (isInMultiWindowMode) {
                            win.setCanCarryColors(TOUCHPLUS_FORCE_VIBRATION);
                        }
                        if (z2) {
                            if (isTouchExplrEnabled) {
                                i = TOUCH_EXPLR_STATUS_BAR_COLOR;
                            } else {
                                i = statusBarColor;
                            }
                            this.mLastStatusBarColor = i;
                            if (isTouchExplrEnabled) {
                                i = TOUCH_EXPLR_STATUS_BAR_COLOR;
                            } else {
                                i = navigationBarColor;
                            }
                            this.mLastNavigationBarColor = i;
                            this.mLastIsEmuiStyle = isEmuiStyle;
                            this.mIsTouchExplrEnabled = isTouchExplrEnabled;
                            this.mLastColorWin = win;
                            this.mLastIsEmuiLightStyle = isEmuiLightStyle;
                            Slog.v(TAG, "updateSystemUiColorLw window=" + win + ",EmuiStyle=" + isEmuiStyle + ",StatusBarColor=0x" + Integer.toHexString(statusBarColor) + ",NavigationBarColor=0x" + Integer.toHexString(navigationBarColor) + ", mLastIsEmuiLightStyle=" + this.mLastIsEmuiLightStyle);
                            this.mHandler.post(new Runnable() {
                                public void run() {
                                    HwPhoneWindowManager.this.transactToStatusBarService(CPUFeature.MSG_PROCESS_GROUP_CHANGE, "setSystemUIColor", HwPhoneWindowManager.this.mLastIsEmuiStyle, HwPhoneWindowManager.this.mLastStatusBarColor, HwPhoneWindowManager.this.mLastNavigationBarColor, HwPhoneWindowManager.this.mLastIsEmuiLightStyle);
                                }
                            });
                        }
                    }
                }
                isStatusBarOrKeyGuard = TOUCHPLUS_FORCE_VIBRATION;
                i = attrs.type;
                if (r0 != 2034) {
                }
                if (win.getStackId() == 3) {
                }
                if (isStatusBarOrKeyGuard) {
                }
                if (styleChanged) {
                }
                if (styleChanged) {
                }
                if (isInMultiWindowMode) {
                    win.setCanCarryColors(TOUCHPLUS_FORCE_VIBRATION);
                }
                if (z2) {
                    if (isTouchExplrEnabled) {
                        i = statusBarColor;
                    } else {
                        i = TOUCH_EXPLR_STATUS_BAR_COLOR;
                    }
                    this.mLastStatusBarColor = i;
                    if (isTouchExplrEnabled) {
                        i = navigationBarColor;
                    } else {
                        i = TOUCH_EXPLR_STATUS_BAR_COLOR;
                    }
                    this.mLastNavigationBarColor = i;
                    this.mLastIsEmuiStyle = isEmuiStyle;
                    this.mIsTouchExplrEnabled = isTouchExplrEnabled;
                    this.mLastColorWin = win;
                    this.mLastIsEmuiLightStyle = isEmuiLightStyle;
                    Slog.v(TAG, "updateSystemUiColorLw window=" + win + ",EmuiStyle=" + isEmuiStyle + ",StatusBarColor=0x" + Integer.toHexString(statusBarColor) + ",NavigationBarColor=0x" + Integer.toHexString(navigationBarColor) + ", mLastIsEmuiLightStyle=" + this.mLastIsEmuiLightStyle);
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            HwPhoneWindowManager.this.transactToStatusBarService(CPUFeature.MSG_PROCESS_GROUP_CHANGE, "setSystemUIColor", HwPhoneWindowManager.this.mLastIsEmuiStyle, HwPhoneWindowManager.this.mLastStatusBarColor, HwPhoneWindowManager.this.mLastNavigationBarColor, HwPhoneWindowManager.this.mLastIsEmuiLightStyle);
                        }
                    });
                }
            }
        }
    }

    protected int getEmuiStyleValue(int styleValue) {
        return styleValue == -1 ? -1 : Integer.MAX_VALUE & styleValue;
    }

    protected int getEmuiLightStyleValue(int styleValue) {
        return (styleValue & 16) != 0 ? TOUCHPLUS_SETTINGS_ENABLED : -1;
    }

    protected boolean isEmuiStyleChanged(int isEmuiStyle, int isEmuiLightStyle) {
        return (this.mLastIsEmuiStyle == isEmuiStyle && this.mLastIsEmuiLightStyle == isEmuiLightStyle) ? IS_LONG_HOME_VASSITANT : TOUCHPLUS_FORCE_VIBRATION;
    }

    protected boolean getFloatingValue(int styleValue) {
        return (styleValue != -1 && (styleValue & FLOATING_MASK) == FLOATING_MASK) ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
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

    private boolean isJackDeviceEvent(int deviceId, int keyCode) {
        if (deviceId == -1 || (JACK_DEVICE_ID & deviceId) == 0) {
            return IS_LONG_HOME_VASSITANT;
        }
        return (25 == keyCode || 24 == keyCode) ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
    }

    private void dispatchInternetAudioKeyWithWakeLock(KeyEvent event) {
        IInternetAudioService internetAudioService = getInternetAudioService();
        if (internetAudioService != null) {
            try {
                internetAudioService.dispatchMediaKeyEvent(event);
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "dispatchInternetMediaKeyEvent threw RemoteException");
                return;
            }
        }
        Intent service = new Intent();
        service.setClassName("com.huawei.internetaudioservice", "com.huawei.internetaudioservice.InternetAudioService");
        this.mContext.startService(service);
    }

    private static IInternetAudioService getInternetAudioService() {
        IInternetAudioService audioService = IInternetAudioService.Stub.asInterface(ServiceManager.checkService("internet_audio"));
        if (audioService == null) {
            Log.w(TAG, "Unable to find IInternetAudioService interface, or the service is killed, so restart it!");
        }
        return audioService;
    }

    private void interceptTouchDisableMode() {
        if (this.mVolumeUpKeyDisTouch && this.mPowerKeyDisTouch && !this.mVolumeDownKeyDisTouch) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mVolumeUpKeyDisTouchTime + TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS && now <= this.mPowerKeyDisTouchTime + TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS) {
                this.mVolumeUpKeyConsumedByDisTouch = TOUCHPLUS_FORCE_VIBRATION;
                cancelPendingPowerKeyActionForDistouch();
                if (this.mHwScreenOnProximityLock != null) {
                    this.mHwScreenOnProximityLock.releaseLock();
                }
            }
        }
    }

    public void screenTurningOn(ScreenOnListener screenOnListener) {
        super.screenTurningOn(screenOnListener);
        if (this.mContext == null) {
            Log.d(TAG, "Context object is null.");
            return;
        }
        boolean isModeEnabled = System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, TOUCHPLUS_SETTINGS_ENABLED, ActivityManager.getCurrentUser()) > 0 ? "factory".equals(SystemProperties.get("ro.runmode", "normal")) ? IS_LONG_HOME_VASSITANT : TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
        if (this.mHwScreenOnProximityLock != null && isModeEnabled) {
            this.mHwScreenOnProximityLock.acquireLock(this);
        }
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", IS_LONG_HOME_VASSITANT) && this.mSystemReady) {
            EasyWakeUpManager mWakeUpManager = EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate);
            if (mWakeUpManager != null) {
                mWakeUpManager.turnOffSensorListener();
            }
        }
    }

    public void screenTurnedOff() {
        super.screenTurnedOff();
        if (this.mHwScreenOnProximityLock != null) {
            this.mHwScreenOnProximityLock.releaseLock();
        }
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", IS_LONG_HOME_VASSITANT) && this.mSystemReady) {
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
    }

    public int selectAnimationLw(WindowState win, int transit) {
        if (win != this.mNavigationBar || this.mNavigationBarOnBottom || (transit != TOUCHPLUS_SETTINGS_ENABLED && transit != 3)) {
            return super.selectAnimationLw(win, transit);
        }
        return mIsHwNaviBar ? TOUCHPLUS_SETTINGS_DISABLED : 17432615;
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

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode) {
        if ((uiMode & 15) == 3) {
            return super.getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode);
        }
        if (!this.mHasNavigationBar || !this.mNavigationBarCanMove || fullWidth <= fullHeight) {
            return fullWidth;
        }
        if (this.mNavigationBarPolicy != null && this.mNavigationBarPolicy.mMinNavigationBar) {
            return fullWidth - this.mNavigationBarWidthForRotationMin[rotation];
        }
        int nonDecorDisplayWidth;
        if (getNavibarAlignLeftWhenLand()) {
            nonDecorDisplayWidth = fullWidth - this.mContext.getResources().getDimensionPixelSize(34472123);
        } else {
            nonDecorDisplayWidth = fullWidth - this.mNavigationBarWidthForRotationMax[rotation];
        }
        return nonDecorDisplayWidth;
    }

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode) {
        if ((uiMode & 15) == 3) {
            return super.getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode);
        }
        if (!this.mHasNavigationBar || (this.mNavigationBarCanMove && fullWidth >= fullHeight)) {
            return fullHeight;
        }
        if (this.mNavigationBarPolicy == null || !this.mNavigationBarPolicy.mMinNavigationBar) {
            return fullHeight - this.mNavigationBarHeightForRotationMax[rotation];
        }
        return fullHeight - this.mNavigationBarHeightForRotationMin[rotation];
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
        super.setInitialDisplaySize(display, width, height, density);
        if (this.mContext != null) {
            Resources res = this.mContext.getResources();
            ContentResolver resolver = this.mContext.getContentResolver();
            int[] iArr = this.mNavigationBarHeightForRotationMax;
            int i = this.mPortraitRotation;
            int dimensionPixelSize = res.getDimensionPixelSize(17104920);
            this.mNavigationBarHeightForRotationMax[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarHeightForRotationMax;
            i = this.mLandscapeRotation;
            dimensionPixelSize = res.getDimensionPixelSize(17104921);
            this.mNavigationBarHeightForRotationMax[this.mSeascapeRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarHeightForRotationMin;
            i = this.mPortraitRotation;
            dimensionPixelSize = System.getInt(resolver, "navigationbar_height_min", TOUCHPLUS_SETTINGS_DISABLED);
            this.mNavigationBarHeightForRotationMin[this.mSeascapeRotation] = dimensionPixelSize;
            this.mNavigationBarHeightForRotationMin[this.mLandscapeRotation] = dimensionPixelSize;
            this.mNavigationBarHeightForRotationMin[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarWidthForRotationMax;
            i = this.mPortraitRotation;
            dimensionPixelSize = res.getDimensionPixelSize(17104922);
            this.mNavigationBarWidthForRotationMax[this.mSeascapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMax[this.mLandscapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMax[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarWidthForRotationMin;
            i = this.mPortraitRotation;
            dimensionPixelSize = System.getInt(resolver, "navigationbar_width_min", TOUCHPLUS_SETTINGS_DISABLED);
            this.mNavigationBarWidthForRotationMin[this.mSeascapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMin[this.mLandscapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMin[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
        }
    }

    protected boolean computeNaviBarFlag() {
        boolean z = IS_LONG_HOME_VASSITANT;
        LayoutParams focusAttrs = null;
        if (this.mFocusedWindow != null) {
            focusAttrs = this.mFocusedWindow.getAttrs();
        }
        int type = focusAttrs != null ? focusAttrs.type : TOUCHPLUS_SETTINGS_DISABLED;
        boolean forceNavibar = focusAttrs != null ? (focusAttrs.hwFlags & TOUCHPLUS_SETTINGS_ENABLED) == TOUCHPLUS_SETTINGS_ENABLED ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT : IS_LONG_HOME_VASSITANT;
        boolean keyguardOn = type != 2101 ? type == 2100 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT : TOUCHPLUS_FORCE_VIBRATION;
        boolean keyguardOn2 = type == 2009 ? keyguardOn() : IS_LONG_HOME_VASSITANT;
        boolean dreamOn = (focusAttrs == null || focusAttrs.type != 2023) ? IS_LONG_HOME_VASSITANT : TOUCHPLUS_FORCE_VIBRATION;
        boolean isNeedHideNaviBarWin = (focusAttrs == null || (focusAttrs.privateFlags & FLOATING_MASK) == 0) ? IS_LONG_HOME_VASSITANT : TOUCHPLUS_FORCE_VIBRATION;
        if (this.mStatusBar == this.mFocusedWindow) {
            return IS_LONG_HOME_VASSITANT;
        }
        if (keyguardOn2 && !forceNavibar) {
            return TOUCHPLUS_FORCE_VIBRATION;
        }
        if (dreamOn) {
            return IS_LONG_HOME_VASSITANT;
        }
        if (keyguardOn || isNeedHideNaviBarWin) {
            return TOUCHPLUS_FORCE_VIBRATION;
        }
        if (this.isNavibarHide && !this.mInputMethodWindowVisible) {
            z = TOUCHPLUS_FORCE_VIBRATION;
        }
        return z;
    }

    public boolean isNaviBarMini() {
        if (this.mNavigationBarPolicy == null || !this.mNavigationBarPolicy.mMinNavigationBar) {
            return IS_LONG_HOME_VASSITANT;
        }
        return TOUCHPLUS_FORCE_VIBRATION;
    }

    public boolean swipeFromTop() {
        if (Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", TOUCHPLUS_SETTINGS_ENABLED) == 0) {
            return TOUCHPLUS_FORCE_VIBRATION;
        }
        if (!mIsHwNaviBar) {
            return IS_LONG_HOME_VASSITANT;
        }
        if (isLastImmersiveMode()) {
            requestHwTransientBars(this.mStatusBar);
        } else {
            requestTransientStatusBars();
        }
        return TOUCHPLUS_FORCE_VIBRATION;
    }

    public boolean swipeFromBottom() {
        if (Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", TOUCHPLUS_SETTINGS_ENABLED) == 0) {
            return TOUCHPLUS_FORCE_VIBRATION;
        }
        if (!mIsHwNaviBar || !isLastImmersiveMode() || this.mNavigationBar == null || !this.mNavigationBarOnBottom) {
            return IS_LONG_HOME_VASSITANT;
        }
        requestHwTransientBars(this.mNavigationBar);
        return TOUCHPLUS_FORCE_VIBRATION;
    }

    public boolean swipeFromRight() {
        if (!mIsHwNaviBar || !isLastImmersiveMode() || this.mNavigationBar == null || this.mNavigationBarOnBottom) {
            return IS_LONG_HOME_VASSITANT;
        }
        requestHwTransientBars(this.mNavigationBar);
        return TOUCHPLUS_FORCE_VIBRATION;
    }

    public boolean isGestureIsolated() {
        WindowState win = this.mFocusedWindow != null ? this.mFocusedWindow : this.mTopFullscreenOpaqueWindowState;
        if (win == null || (win.getAttrs().hwFlags & HwGlobalActionsData.FLAG_SILENTMODE_VIBRATE) != HwGlobalActionsData.FLAG_SILENTMODE_VIBRATE) {
            return IS_LONG_HOME_VASSITANT;
        }
        return TOUCHPLUS_FORCE_VIBRATION;
    }

    public void requestTransientStatusBars() {
        synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
            BarController barController = getStatusBarController();
            boolean sb = IS_LONG_HOME_VASSITANT;
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
        return this.mTopIsFullscreen;
    }

    public boolean okToShowTransientBar() {
        boolean z = IS_LONG_HOME_VASSITANT;
        BarController barController = getStatusBarController();
        if (barController == null) {
            return IS_LONG_HOME_VASSITANT;
        }
        if (barController.checkShowTransientBarLw()) {
            z = TOUCHPLUS_FORCE_VIBRATION;
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
        boolean touchDisableModeOpen = System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, TOUCHPLUS_SETTINGS_ENABLED, DEFAULT_RESULT_VALUE) == TOUCHPLUS_SETTINGS_ENABLED ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
        if (this.mCoverOpen && !this.mSensorRegisted && this.mListener != null && touchDisableModeOpen) {
            Log.i(TAG, "turnOnSensorListener, registerListener");
            this.mSensorManager.registerListener(this.mListener, this.mSensorManager.getDefaultSensor(8), TOUCHPLUS_SETTINGS_DISABLED);
            this.mSensorRegisted = TOUCHPLUS_FORCE_VIBRATION;
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
        this.mSensorRegisted = IS_LONG_HOME_VASSITANT;
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
        enableSystemWideAfterBoot(this.mContext);
        enableFingerPrintActionsAfterBoot(this.mContext);
    }

    public WindowState getFocusedWindow() {
        return this.mFocusedWindow;
    }

    public int getRestrictedScreenHeight() {
        return this.mRestrictedScreenHeight;
    }

    public boolean isNavigationBarVisible() {
        return (!this.mHasNavigationBar || this.mNavigationBar == null) ? IS_LONG_HOME_VASSITANT : this.mNavigationBar.isVisibleLw();
    }

    protected void enableSystemWideActions() {
        if (SystemProperties.getBoolean("ro.config.finger_joint", IS_LONG_HOME_VASSITANT)) {
            Flog.i(1503, "FingerSense enableSystemWideActions");
            if (this.systemWideActionsListener == null) {
                this.systemWideActionsListener = new SystemWideActionsListener(this.mContext, this);
                this.mWindowManagerFuncs.registerPointerEventListener(this.systemWideActionsListener);
                this.systemWideActionsListener.createPointerLocationView();
            }
            SystemProperties.set("persist.sys.fingersense", PPPOEStateMachine.PHASE_INITIALIZE);
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
        SystemProperties.set("persist.sys.fingersense", PPPOEStateMachine.PHASE_DEAD);
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
        this.mHandler.post(new AnonymousClass13(context.getContentResolver()));
    }

    protected void setNaviBarState() {
        boolean navibarEnable = FrontFingerPrintSettings.isNaviBarEnabled(this.mContext.getContentResolver());
        if (this.mNavibarEnabled != navibarEnable) {
            int i;
            this.mNavibarEnabled = navibarEnable;
            Handler handler = this.mHandlerEx;
            if (navibarEnable) {
                i = MSG_NAVIBAR_ENABLE;
            } else {
                i = MSG_NAVIBAR_DISABLE;
            }
            handler.sendEmptyMessage(i);
        }
    }

    protected void updateSplitScreenView() {
        if (this.fingerprintActionsListener != null) {
            this.fingerprintActionsListener.createMultiWinArrowView();
        }
    }

    protected void enableSystemWideAfterBoot(Context context) {
        this.mHandler.post(new AnonymousClass14(context.getContentResolver()));
    }

    protected void setFingerSenseState() {
        boolean fingersense = FingerSenseSettings.isFingerSenseEnabled(this.mContext.getContentResolver());
        if (this.mFingerSenseEnabled != fingersense) {
            int i;
            this.mFingerSenseEnabled = fingersense;
            Flog.i(1503, "setFingerSenseState to " + fingersense);
            Handler handler = this.mHandlerEx;
            if (fingersense) {
                i = MSG_FINGERSENSE_ENABLE;
            } else {
                i = MSG_FINGERSENSE_DISABLE;
            }
            handler.sendEmptyMessage(i);
        }
    }

    public void processing_KEYCODE_SOUNDTRIGGER_EVENT(int keyCode, Context context, boolean isMusicOrFMActive, boolean down, boolean keyguardShow) {
        Log.d(TAG, "intercept DSP WAKEUP EVENT" + keyCode + " down=" + down + " keyguardShow=" + keyguardShow);
        this.mContext = context;
        ITelephony telephonyService = Stub.asInterface(ServiceManager.checkService("phone"));
        switch (keyCode) {
            case 401:
                if (down) {
                    Log.i(TAG, "soundtrigger wakeup.");
                    if (isTOPActivity(HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE)) {
                        Log.i(TAG, "start SoundTiggerTest");
                        notifySoundTriggerTest();
                    } else if (isTOPActivity(HUAWEI_VOICE_DEBUG_BETACLUB)) {
                        Log.i(TAG, "soundtrigger debug during betaclub.");
                        notifySoundTriggerTest();
                    } else {
                        Log.i(TAG, "start VA");
                        notifyVassistantService("start", START_MODE_VOICE_WAKEUP_ONE_SHOT, null);
                    }
                }
            case 402:
                if (down) {
                    Log.i(TAG, "command that find my phone.");
                    if (telephonyService != null) {
                    }
                    if (isTOPActivity(HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE)) {
                        Log.i(TAG, "looking for my phone during SoundTiggerTest");
                    } else if (isTOPActivity(HUAWEI_VOICE_DEBUG_BETACLUB)) {
                        Log.i(TAG, "looking for my phone during betaclub.");
                    } else {
                        Log.i(TAG, "findphone.");
                        notifyVassistantService("findphone", START_MODE_VOICE_WAKEUP_ONE_SHOT, null);
                    }
                }
            default:
        }
    }

    private boolean isTOPActivity(String appnames) {
        try {
            List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(TOUCHPLUS_SETTINGS_ENABLED);
            if (tasks == null || tasks.isEmpty()) {
                return IS_LONG_HOME_VASSITANT;
            }
            for (RunningTaskInfo info : tasks) {
                Log.i(TAG, "info.topActivity.getPackageName() is " + info.topActivity.getPackageName());
                if (info.topActivity.getPackageName().equals(appnames) && info.baseActivity.getPackageName().equals(appnames)) {
                    return TOUCHPLUS_FORCE_VIBRATION;
                }
            }
            return IS_LONG_HOME_VASSITANT;
        } catch (RuntimeException e) {
            Log.e(TAG, "isTOPActivity->RuntimeException happened");
        } catch (Exception e2) {
            Log.e(TAG, "isTOPActivity->other exception happened");
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
            this.mVolumeDownWakeLock.acquire(VOLUMEDOWN_LONG_PRESS_TIMEOUT);
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
        boolean down = event.getAction() == 0 ? TOUCHPLUS_FORCE_VIBRATION : IS_LONG_HOME_VASSITANT;
        int keyCode = event.getKeyCode();
        if (ActivityManagerNative.getDefault().testIsSystemReady()) {
            if (this.DEBUG_SMARTKEY) {
                Log.d(TAG, "handleSmartKey keycode = " + keyCode + " down = " + down + " isScreenOn = " + isScreenOn);
            }
            switch (keyCode) {
                case 308:
                    if (!down) {
                        if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", IS_LONG_HOME_VASSITANT)) {
                            this.mHandler.postDelayed(this.mCancleInterceptFingerprintEvent, VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT);
                        }
                        if (!this.mIsSmartKeyDoubleClick && !this.mIsSmartKeyTripleOrMoreClick && event.getEventTime() - event.getDownTime() < VOLUMEDOWN_LONG_PRESS_TIMEOUT) {
                            cancelSmartKeyLongPressed();
                            sendSmartKeyEvent(SMARTKEY_CLICK);
                            break;
                        }
                        cancelSmartKeyLongPressed();
                        break;
                    }
                    if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", IS_LONG_HOME_VASSITANT)) {
                        this.mNeedDropFingerprintEvent = TOUCHPLUS_FORCE_VIBRATION;
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
                    if (timediff < VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT) {
                        if (!this.mIsSmartKeyDoubleClick && !this.mIsSmartKeyTripleOrMoreClick) {
                            cancelSmartKeyClick();
                            cancelSmartKeyLongPressed();
                            sendSmartKeyEvent(SMARTKEY_DCLICK);
                            this.mIsSmartKeyDoubleClick = TOUCHPLUS_FORCE_VIBRATION;
                            break;
                        }
                        this.mIsSmartKeyTripleOrMoreClick = TOUCHPLUS_FORCE_VIBRATION;
                        this.mIsSmartKeyDoubleClick = IS_LONG_HOME_VASSITANT;
                        break;
                    }
                    this.mIsSmartKeyTripleOrMoreClick = IS_LONG_HOME_VASSITANT;
                    this.mIsSmartKeyDoubleClick = IS_LONG_HOME_VASSITANT;
                    break;
                    break;
            }
            return;
        }
        Log.d(TAG, "System is not ready, just discard it this time.");
    }

    private void sendSmartKeyEvent(String Type) {
        if (SMARTKEY_LP.equals(Type)) {
            this.mHandler.postDelayed(this.mSmartKeyLongPressed, VOLUMEDOWN_LONG_PRESS_TIMEOUT);
        } else if (SMARTKEY_DCLICK.equals(Type)) {
            notifySmartKeyEvent(SMARTKEY_DCLICK);
        } else {
            this.mHandler.postDelayed(this.mSmartKeyClick, VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT);
        }
    }

    private void cancelSmartKeyClick() {
        this.mHandler.removeCallbacks(this.mSmartKeyClick);
    }

    private void cancelSmartKeyLongPressed() {
        this.mHandler.removeCallbacks(this.mSmartKeyLongPressed);
    }

    private void notifySmartKeyEvent(String strType) {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", TOUCHPLUS_SETTINGS_DISABLED) != 0) {
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
        String[] dropSmartKeyArray = dropSmartKeyActivity.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        int length = dropSmartKeyArray.length;
        for (int i = TOUCHPLUS_SETTINGS_DISABLED; i < length; i += TOUCHPLUS_SETTINGS_ENABLED) {
            this.needDropSmartKeyActivities.add(dropSmartKeyArray[i]);
        }
    }

    private boolean needDropSmartKey() {
        boolean result = IS_LONG_HOME_VASSITANT;
        String topActivityName = getTopActivity();
        if (this.needDropSmartKeyActivities != null && this.needDropSmartKeyActivities.contains(topActivityName)) {
            result = TOUCHPLUS_FORCE_VIBRATION;
            Log.d(TAG, "drop smartkey event because of conflict with fingerprint authentication!");
        }
        if ((!isCamera() || !this.isFingerShotCameraOn) && ((!isInCallUIAndRinging() || !this.isFingerAnswerPhoneOn) && (!isAlarm(this.mCurUser) || !this.isFingerStopAlarmOn))) {
            return result;
        }
        Log.d(TAG, "drop smartkey event because of conflict with fingerprint longpress event!");
        return TOUCHPLUS_FORCE_VIBRATION;
    }

    private boolean isCamera() {
        String pkgName = getTopActivity();
        return pkgName != null ? pkgName.startsWith("com.huawei.camera") : IS_LONG_HOME_VASSITANT;
    }

    private boolean isInCallUIAndRinging() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        return telecomManager != null ? telecomManager.isRinging() : IS_LONG_HOME_VASSITANT;
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
        synchronized (this.mLock) {
            KeyguardDismissDoneListener listener = this.mKeyguardDismissListener;
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
            this.mBackKeyPress = IS_LONG_HOME_VASSITANT;
            this.mMenuKeyPress = IS_LONG_HOME_VASSITANT;
            this.mBackKeyPressTime = 0;
            this.mMenuKeyPressTime = 0;
        }
    }

    private boolean isScreenInLockTaskMode() {
        boolean isScreenLocked = IS_LONG_HOME_VASSITANT;
        try {
            isScreenLocked = ActivityManagerNative.getDefault().isInLockTaskMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isScreenInLockTaskMode  ", e);
        }
        return isScreenLocked;
    }

    public boolean isStatusBarObsecured() {
        return this.mStatuBarObsecured;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isStatusBarObsecuredByWin(WindowState win) {
        boolean z = IS_LONG_HOME_VASSITANT;
        if (win == null || this.mStatusBar == null || (win.getAttrs().flags & 16) != 0 || win.toString().contains("hwSingleMode_window")) {
            return IS_LONG_HOME_VASSITANT;
        }
        Rect winFrame = win.getFrameLw();
        Rect statusbarFrame = this.mStatusBar.getFrameLw();
        if (winFrame.top <= statusbarFrame.top && winFrame.bottom >= statusbarFrame.bottom && winFrame.left <= statusbarFrame.left && winFrame.right >= statusbarFrame.right) {
            z = TOUCHPLUS_FORCE_VIBRATION;
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
        if (tempDpi != this.lastDensityDpi && this.systemWideActionsListener != null) {
            this.systemWideActionsListener.updateConfiguration();
            this.lastDensityDpi = tempDpi;
        }
    }

    public boolean performHapticFeedbackLw(WindowState win, int effectId, boolean always) {
        if (effectId != TOUCHPLUS_SETTINGS_ENABLED || !this.isVibrateImplemented || always) {
            return super.performHapticFeedbackLw(win, effectId, always);
        }
        if (TOUCHPLUS_SETTINGS_ENABLED != System.getInt(this.mContext.getContentResolver(), "touch_vibrate_mode", TOUCHPLUS_SETTINGS_ENABLED)) {
            return IS_LONG_HOME_VASSITANT;
        }
        HwGeneralManager.getInstance().playIvtEffect("VIRTUAL_KEY");
        return TOUCHPLUS_FORCE_VIBRATION;
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
            return IS_LONG_HOME_VASSITANT;
        }
        try {
            if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
                return telephonyService.isIdle(this.mContext.getPackageName());
            }
            return telephonyService.isIdleForSubscriber(TOUCHPLUS_SETTINGS_DISABLED, this.mContext.getPackageName()) ? telephonyService.isIdleForSubscriber(TOUCHPLUS_SETTINGS_ENABLED, this.mContext.getPackageName()) : IS_LONG_HOME_VASSITANT;
        } catch (RemoteException ex) {
            Log.w(TAG, "ITelephony threw RemoteException", ex);
            return IS_LONG_HOME_VASSITANT;
        }
    }

    public int getDisabledKeyEventResult(int keyCode) {
        switch (keyCode) {
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                if ((this.mCust == null || !this.mCust.disableHomeKey(this.mContext)) && !HwDeviceManager.disallowOp(14)) {
                    return DEFAULT_RESULT_VALUE;
                }
                Log.i(TAG, "the device's home key has been disabled for the user.");
                return TOUCHPLUS_SETTINGS_DISABLED;
            case START_MODE_VOICE_WAKEUP_ONE_SHOT /*4*/:
                if (!HwDeviceManager.disallowOp(16)) {
                    return DEFAULT_RESULT_VALUE;
                }
                Log.i(TAG, "the device's back key has been disabled for the user.");
                return -1;
            case 187:
                if (!HwDeviceManager.disallowOp(15)) {
                    return DEFAULT_RESULT_VALUE;
                }
                Log.i(TAG, "the device's task key has been disabled for the user.");
                return TOUCHPLUS_SETTINGS_DISABLED;
            default:
                return DEFAULT_RESULT_VALUE;
        }
    }

    public int[] getTouchCountInfo() {
        return this.mTouchCountPolicy.getTouchCountInfo();
    }

    public int[] getDefaultTouchCountInfo() {
        return this.mTouchCountPolicy.getDefaultTouchCountInfo();
    }

    private boolean isTrikeyNaviKeycodeFromLON(boolean isInjected, boolean excluded) {
        return (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0 || (!isInjected && this.mTrikeyNaviMode < 0)) ? TOUCHPLUS_FORCE_VIBRATION : excluded;
    }
}
