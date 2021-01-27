package huawei.com.android.server.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.HwFoldScreenState;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.IMonitor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.DefaultHwScreenOnProximityLock;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.HwScreenOnProximityLayout;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.IntentExEx;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.hardware.SystemSensorManagerEx;
import com.huawei.android.hardware.input.HwInputManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.HwNotchSizeUtil;
import com.huawei.android.view.ViewRootImplEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.hwextdevice.HWExtDeviceEvent;
import com.huawei.hwextdevice.HWExtDeviceEventListener;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.hwextdevice.devices.HWExtMotion;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import com.huawei.util.LogEx;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.view.HwWindowManager;
import huawei.com.android.server.policy.HwScreenOnProximityLock;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HwScreenOnProximityLock extends DefaultHwScreenOnProximityLock {
    private static final String APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.action.APS_RESOLUTION_CHANGE_ACTION";
    private static final String APS_RESOLUTION_CHANGE_PERSISSIONS = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
    private static final int AXIS_VALUE_LENGTH = 3;
    private static final int AXIS_X_INDEX = 0;
    private static final int AXIS_Y_INDEX = 1;
    private static final int AXIS_Z_INDEX = 2;
    private static final float AXIS_Z_THRESHOLD = 5.0f;
    private static final int DEFAULT_DIMAMBIENT_THRESHOLD = -1;
    private static final int DIMAMBIENT_THRESHOLD = -1;
    private static final int DYNAMIC_SENSOR_BIT = 8;
    private static final int DYNAMIC_SENSOR_INDEX = 2;
    private static final int DYNAMIC_SENSOR_LENGTH = 3;
    public static final int FARAWAY_SENSOR = 2;
    public static final int FORCE_QUIT = 0;
    public static final int HEADDOWN_LEAVE = 4;
    private static final int HEIGHT_SCALE = 4;
    private static final int HEIGHT_SCALE_FOLD = 8;
    private static final String HW_CURVED_SIDE = SystemPropertiesEx.get("ro.config.hw_curved_side_disp", BuildConfig.FLAVOR);
    private static final String HW_NOTCH_SIZE = SystemPropertiesEx.get("ro.config.hw_notch_size", BuildConfig.FLAVOR);
    private static final boolean IS_DEBUG = false;
    private static final boolean IS_FOLDABLE = HwFoldScreenState.isFoldScreenDevice();
    private static final boolean IS_HWFLOW = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(SCREENON_TAG, 4)));
    public static final int LOCK_GOAWAY = 3;
    private static final int MATH_POW_NUM = 2;
    private static final int MOTION_HEADDOWN_ENTER = 1;
    private static final int MOTION_HEADDOWN_LEAVE = 2;
    private static final int MOTION_VALID_LENGTH = 2;
    private static final int PFTR_SENSOR = 1;
    private static final int PFTR_TP = 2;
    private static final int PFTS_EXIST = 2;
    private static final int PFTS_START = 1;
    private static final float PITCH_HIGHER = 12.0f;
    private static final float PITCH_LOWER = 3.6f;
    private static final String POCKET_IN = "pattern [POCKET_IN]";
    private static final String POCKET_OUT = "pattern [POCKET_OUT]";
    private static final boolean PROXIMITY_ENABLE = SystemPropertiesEx.getBoolean("ro.product.proximityenable", true);
    private static final String PROXIMITY_WND_NAME = "Emui:ProximityWnd";
    private static final short P_F_T_R = 1;
    private static final short P_F_T_S = 0;
    private static final float ROLL_THRESHOLD = 6.0f;
    private static final String ROOT_CAUSE_RESOLVE_PATTERN_LOG = "/data/log/reliability/rootcauseresolve/pattern.log";
    private static final int ROOT_CAUSE_RESOLVE_PATTERN_PATTERN_FILE_SIZE = 10240;
    private static final String SCREENON_TAG = "ScreenOn";
    public static final int SCREEN_OFF = 1;
    private static final int SENSOR_DEVICE = 2050;
    private static final int SENSOR_FEATURE = 2049;
    private static final int SWING_FACE_TIME_OUT_MS = 500;
    private static final String TAG = "HwScreenOnProximityLock";
    private static final int TP_LOG = 907400033;
    private static final int TSA_EVENT_ANTI_TOUCH_DETECTED = 16777216;
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final int WIDTH_SCALE = 5;
    private AccSensorListener mAccListener = null;
    private BroadcastReceiver mApsResolutionChangeReceiver = new BroadcastReceiver() {
        /* class huawei.com.android.server.policy.HwScreenOnProximityLock.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.i(HwScreenOnProximityLock.TAG, "on receive apsResolutionChangeReceiver");
            HwScreenOnProximityLock.this.mIsNeedRefresh = true;
        }
    };
    private Context mContext;
    private CoverManager mCoverManager;
    private ContentObserver mFontScaleObserver = new ContentObserver(new Handler()) {
        /* class huawei.com.android.server.policy.HwScreenOnProximityLock.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            Log.i(HwScreenOnProximityLock.TAG, "font Scale changed");
            HwScreenOnProximityLock.this.prepareProximityView();
        }
    };
    private BallFrameView mFrameView;
    private Runnable mHandleForDetectFace = null;
    private Handler mHandler;
    private boolean mHasNotchInScreen = false;
    private int mHeadDownStatus = 2;
    private View mHintView;
    private HWExtDeviceEventListener mHwExtDeviceListener = new HWExtDeviceEventListener() {
        /* class huawei.com.android.server.policy.HwScreenOnProximityLock.AnonymousClass2 */

        public void onDeviceDataChanged(HWExtDeviceEvent hwextDeviceEvent) {
            float[] deviceValues = hwextDeviceEvent.getDeviceValues();
            if (deviceValues == null) {
                Log.e(HwScreenOnProximityLock.TAG, "onDeviceDataChanged deviceValues is null ");
            } else if (deviceValues.length < 2) {
                Log.e(HwScreenOnProximityLock.TAG, "hwextDeviceEvent data error");
            } else {
                boolean isUseSensorFeature = true;
                HwScreenOnProximityLock.this.mHeadDownStatus = (int) deviceValues[1];
                Log.i(HwScreenOnProximityLock.TAG, "onDeviceDataChanged mHeadDownStatus:" + HwScreenOnProximityLock.this.mHeadDownStatus);
                if (HwScreenOnProximityLock.this.mHeadDownStatus == 1) {
                    if (!HwScreenOnProximityLock.this.mIsSupportSensorFeature || HwScreenOnProximityLock.this.mIsProximityGen || !HwScreenOnProximityLock.this.mPhoneWindowManager.isKeyguardLocked()) {
                        isUseSensorFeature = false;
                    }
                    HwScreenOnProximityLock hwScreenOnProximityLock = HwScreenOnProximityLock.this;
                    hwScreenOnProximityLock.mIsProximity = isUseSensorFeature ? hwScreenOnProximityLock.mIsProximityDyn : hwScreenOnProximityLock.mIsProximityGen;
                    if (HwScreenOnProximityLock.this.mIsProximity) {
                        HwScreenOnProximityLock.this.addProximityView();
                    }
                } else if (HwScreenOnProximityLock.this.mHeadDownStatus == 2) {
                    HwScreenOnProximityLock hwScreenOnProximityLock2 = HwScreenOnProximityLock.this;
                    hwScreenOnProximityLock2.mIsProximity = hwScreenOnProximityLock2.mIsProximityGen;
                    if (!HwScreenOnProximityLock.this.mIsProximity || !HwScreenOnProximityLock.this.mIsDimAmbient) {
                        HwScreenOnProximityLock.this.releaseLockInternal(4);
                        Log.i(HwScreenOnProximityLock.TAG, "quit mistouch for head down leave");
                    }
                } else {
                    Log.d(HwScreenOnProximityLock.TAG, "not handle other headdown status");
                }
            }
        }
    };
    private HWExtDeviceManager mHwExtDeviceManager = null;
    private HWExtMotion mHwExtMotion = null;
    private HwInputManager mHwInputManager;
    private HwSwingFaceNumberFenceManager mHwSwingFaceNumberFenceManager;
    private HwInputManager.HwTHPEventListener mHwTHPEventListener = new HwInputManager.HwTHPEventListener() {
        /* class huawei.com.android.server.policy.HwScreenOnProximityLock.AnonymousClass6 */

        public void onHwTHPEvent(int event) {
            if (event == HwScreenOnProximityLock.TSA_EVENT_ANTI_TOUCH_DETECTED) {
                if (HwScreenOnProximityLock.this.mHwSwingFaceNumberFenceManager == null) {
                    Log.e(HwScreenOnProximityLock.TAG, "SwingFaceNumberFenceManager is null");
                } else if (!HwScreenOnProximityLock.this.checkPhoneState()) {
                    Log.i(HwScreenOnProximityLock.TAG, "not need add view since in PhoneCallState/HeadSetIsConnected");
                } else {
                    HwScreenOnProximityLock.this.mHandler.post(new Runnable() {
                        /* class huawei.com.android.server.policy.$$Lambda$HwScreenOnProximityLock$6$RdY7gw9tVSYw4i4EpeQNPqHTn0 */

                        @Override // java.lang.Runnable
                        public final void run() {
                            HwScreenOnProximityLock.AnonymousClass6.this.lambda$onHwTHPEvent$0$HwScreenOnProximityLock$6();
                        }
                    });
                }
            }
        }

        public /* synthetic */ void lambda$onHwTHPEvent$0$HwScreenOnProximityLock$6() {
            HwScreenOnProximityLock.this.handleHwThpEvent();
        }

        public void onHwTpEvent(int a, int b, String c) {
        }
    };
    private boolean mIsDeviceHeld = false;
    private boolean mIsDimAmbient = true;
    private boolean mIsHasSensorDevice = false;
    private boolean mIsNeedRefresh = true;
    private boolean mIsProximity;
    private boolean mIsProximityDyn;
    private boolean mIsProximityGen;
    private boolean mIsProximityHeld;
    private final boolean mIsProximityTop = SystemPropertiesEx.getBoolean("ro.config.proximity_top", false);
    private boolean mIsSupportSensorFeature = false;
    private final boolean mIsTouchHeadDown = SystemPropertiesEx.getBoolean("ro.config.touch.head_down", true);
    private boolean mIsUseSensorFeature = false;
    private boolean mIsViewAttached = false;
    private ProximitySensorListener mListener;
    private BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        /* class huawei.com.android.server.policy.HwScreenOnProximityLock.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.i(HwScreenOnProximityLock.TAG, "on receive localeChangeReceiver");
            HwScreenOnProximityLock.this.mIsNeedRefresh = true;
        }
    };
    private WindowManager.LayoutParams mParams = null;
    private HwPhoneWindowManager mPhoneWindowManager;
    private float mProximityThreshold;
    private HwScreenOnProximityLayout mProximityView = null;
    private int[] mReleaseReasons = {0, 1, 3};
    private int mRotation = 0;
    private SensorManager mSensorManager;
    private SystemSensorManagerEx mSystemSensorManager;
    private BroadcastReceiver mUserSwitchReceiver = new BroadcastReceiver() {
        /* class huawei.com.android.server.policy.HwScreenOnProximityLock.AnonymousClass5 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.i(HwScreenOnProximityLock.TAG, "on receive userSwitchReceiver");
            HwScreenOnProximityLock.this.mIsNeedRefresh = true;
        }
    };
    private WindowManager mWindowManager;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHwThpEvent() {
        if (!this.mHwSwingFaceNumberFenceManager.isRegistered()) {
            this.mHwSwingFaceNumberFenceManager.registerAwarenessFence(this.mHandleForDetectFace);
            Object lock = this.mHwSwingFaceNumberFenceManager.getLockObject();
            synchronized (lock) {
                try {
                    lock.wait(500);
                } catch (InterruptedException e) {
                    Log.e(TAG, "lock object wait error");
                }
            }
        }
        if (this.mHwSwingFaceNumberFenceManager.getFaceNumber() < 1) {
            Log.i(TAG, "add proximity view.");
            addProximityView();
            report(1, 2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkPhoneState() {
        boolean isPhoneCallState = checkPhoneOFFHOOK();
        return !isPhoneCallState || (isPhoneCallState && checkHeadSetIsConnected());
    }

    public boolean checkPhoneOFFHOOK() {
        int callState = ((TelephonyManager) this.mContext.getSystemService("phone")).getCallState();
        Log.i(TAG, "callState : " + callState);
        return callState == 2;
    }

    public boolean checkHeadSetIsConnected() {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        boolean isHeadSetConnected = false;
        if (audioManager == null) {
            return false;
        }
        if (audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn() || audioManager.isBluetoothScoOn()) {
            isHeadSetConnected = true;
        }
        Log.i(TAG, "checkHeadSetIsConnected : " + isHeadSetConnected);
        return isHeadSetConnected;
    }

    public HwScreenOnProximityLock(Context context, HwPhoneWindowManager phoneWindowManager, WindowManagerPolicyEx.WindowManagerFuncsEx windowFuncs, Handler handler) {
        super(context, phoneWindowManager, windowFuncs, handler);
        if (context == null) {
            Log.w(TAG, "HwScreenOnProximityLock context is null");
            return;
        }
        this.mContext = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mCoverManager = new CoverManager();
        this.mPhoneWindowManager = phoneWindowManager;
        this.mHwExtDeviceManager = HWExtDeviceManager.getInstance(this.mContext);
        this.mHwExtMotion = new HWExtMotion(1300);
        this.mListener = new ProximitySensorListener();
        if (this.mIsProximityTop) {
            this.mAccListener = new AccSensorListener();
        }
        this.mHandler = handler;
        this.mHasNotchInScreen = !TextUtils.isEmpty(HW_NOTCH_SIZE);
        init();
        this.mSystemSensorManager = new SystemSensorManagerEx(this.mContext, this.mHandler.getLooper());
        this.mIsSupportSensorFeature = this.mSystemSensorManager.supportSensorFeature((int) SENSOR_FEATURE);
        this.mIsHasSensorDevice = !this.mSystemSensorManager.supportSensorFeature((int) SENSOR_DEVICE);
        Settings.System.putInt(this.mContext.getContentResolver(), "support_sensor_feature", this.mIsSupportSensorFeature ? 1 : 0);
        this.mHwInputManager = HwInputManager.getInstance(this.mContext);
        if (!this.mIsHasSensorDevice) {
            this.mHwSwingFaceNumberFenceManager = HwSwingFaceNumberFenceManager.getInstance(this.mContext, this.mHandler);
            this.mHandleForDetectFace = new Runnable() {
                /* class huawei.com.android.server.policy.HwScreenOnProximityLock.AnonymousClass7 */

                @Override // java.lang.Runnable
                public void run() {
                    HwScreenOnProximityLock.this.removeProximityView();
                }
            };
        }
    }

    private void init() {
        registerBroadcastReceiver();
        registerContentObserver();
    }

    private void registerBroadcastReceiver() {
        ContextEx.registerReceiverAsUser(this.mContext, this.mApsResolutionChangeReceiver, UserHandleEx.ALL, new IntentFilter(APS_RESOLUTION_CHANGE_ACTION), APS_RESOLUTION_CHANGE_PERSISSIONS, (Handler) null);
        ContextEx.registerReceiverAsUser(this.mContext, this.mLocaleChangeReceiver, UserHandleEx.ALL, new IntentFilter("android.intent.action.LOCALE_CHANGED"), (String) null, (Handler) null);
        ContextEx.registerReceiverAsUser(this.mContext, this.mUserSwitchReceiver, UserHandleEx.ALL, new IntentFilter(IntentExEx.getActionUserSwitched()), (String) null, (Handler) null);
    }

    private void registerContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("font_scale"), true, this.mFontScaleObserver);
    }

    public void registerDeviceListener() {
        if (!this.mIsTouchHeadDown) {
            Log.w(TAG, "mIsTouchHeadDown is false,no need to registerDeviceListener");
        } else if (this.mIsDeviceHeld) {
            Log.w(TAG, "mIsDeviceHeld is true,register return");
        } else {
            HWExtDeviceManager hWExtDeviceManager = this.mHwExtDeviceManager;
            if (hWExtDeviceManager == null) {
                Log.e(TAG, "mHwExtDeviceManager is null,register return");
                return;
            }
            this.mIsDeviceHeld = hWExtDeviceManager.registerDeviceListener(this.mHwExtDeviceListener, this.mHwExtMotion, this.mHandler);
            Log.i(TAG, "registerDeviceListener result:" + this.mIsDeviceHeld);
        }
    }

    public void unregisterDeviceListener() {
        if (!this.mIsDeviceHeld) {
            Log.w(TAG, "mIsDeviceHeld is false,unregister return");
            return;
        }
        HWExtDeviceManager hWExtDeviceManager = this.mHwExtDeviceManager;
        if (hWExtDeviceManager == null) {
            Log.e(TAG, "mHwExtDeviceManager is null,unregister return");
            return;
        }
        hWExtDeviceManager.unregisterDeviceListener(this.mHwExtDeviceListener, this.mHwExtMotion);
        Log.i(TAG, "unregisterDeviceListener succeed");
        this.mIsDeviceHeld = false;
        this.mHeadDownStatus = 2;
    }

    private void registerProximityListener() {
        if (this.mIsProximityHeld) {
            Log.w(TAG, "mIsProximityHeld is true,registerProximityListener return");
            return;
        }
        Sensor sensor = this.mSensorManager.getDefaultSensor(8);
        if (sensor == null) {
            Log.w(TAG, "registerProximityListener return because of proximity sensor is not existed");
            return;
        }
        float maxRange = sensor.getMaximumRange();
        if (maxRange >= 5.0f) {
            this.mProximityThreshold = 5.0f;
        } else if (maxRange < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            this.mProximityThreshold = 5.0f;
        } else {
            this.mProximityThreshold = maxRange;
        }
        this.mIsProximityHeld = this.mSensorManager.registerListener(this.mListener, sensor, 3, this.mHandler);
        Log.i(TAG, "dimambient threshold:-1");
        if (this.mIsProximityHeld) {
            Log.i(TAG, "registerProximityListener success");
            AccSensorListener accSensorListener = this.mAccListener;
            if (accSensorListener != null) {
                accSensorListener.register();
            }
            registerDeviceListener();
            return;
        }
        Log.w(TAG, "registerProximityListener fail");
    }

    private boolean shouldReleaseProximity(int reason) {
        int len = this.mReleaseReasons.length;
        for (int i = 0; i < len; i++) {
            if (reason == this.mReleaseReasons[i]) {
                return true;
            }
        }
        return !this.mPhoneWindowManager.isKeyguardLocked();
    }

    private boolean shouldRegisterProximity(int mode) {
        if (!IS_FOLDABLE) {
            return true;
        }
        if (mode == 0 || mode == 3) {
            return false;
        }
        return true;
    }

    public void acquireLock(WindowManagerPolicyEx policy, int mode) {
        this.mHandler.post(new Runnable(policy, mode) {
            /* class huawei.com.android.server.policy.$$Lambda$HwScreenOnProximityLock$Z7qd2jqI97P_6OfQZTpgH3MJ650 */
            private final /* synthetic */ WindowManagerPolicyEx f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwScreenOnProximityLock.this.lambda$acquireLock$0$HwScreenOnProximityLock(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$acquireLock$0$HwScreenOnProximityLock(WindowManagerPolicyEx policy, int mode) {
        if (policy == null) {
            Log.w(TAG, "acquire Lock: return because get Window Manager policy is null");
            return;
        }
        if (shouldRegisterProximity(mode)) {
            if (this.mIsHasSensorDevice) {
                registerProximityListener();
            } else if (!this.mIsProximityHeld && this.mHwInputManager != null && this.mPhoneWindowManager.isKeyguardShowingOrOccluded()) {
                Log.i(TAG, "register TP Listener.");
                this.mHwInputManager.registerListener(this.mHwTHPEventListener, this.mHandler);
                this.mIsProximityHeld = true;
            }
        }
        if (!this.mPhoneWindowManager.isKeyguardLocked()) {
            Log.i(TAG, "keyguard not locked");
        }
    }

    public void releaseLock(int reason) {
        this.mHandler.post(new Runnable(reason) {
            /* class huawei.com.android.server.policy.$$Lambda$HwScreenOnProximityLock$A_H1GVrFj26vVVZpWIyFOtVlK8 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwScreenOnProximityLock.this.lambda$releaseLock$1$HwScreenOnProximityLock(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$releaseLock$1$HwScreenOnProximityLock(int reason) {
        if (this.mIsHasSensorDevice) {
            releaseLockInternal(reason);
            return;
        }
        removeProximityView();
        if (this.mIsProximityHeld && this.mHwInputManager != null) {
            Log.i(TAG, "unregister TP Listener.");
            this.mHwInputManager.unregisterListener(this.mHwTHPEventListener);
            this.mIsProximityHeld = false;
            HwSwingFaceNumberFenceManager hwSwingFaceNumberFenceManager = this.mHwSwingFaceNumberFenceManager;
            if (hwSwingFaceNumberFenceManager != null) {
                hwSwingFaceNumberFenceManager.unregisterAwarenessFence();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseLockInternal(int reason) {
        if (!this.mIsProximityHeld) {
            Log.w(TAG, "releaseLock: return because sensor listener is not held");
            return;
        }
        BallFrameView ballFrameView = this.mFrameView;
        if (ballFrameView != null) {
            ballFrameView.updateAnimCount(0);
        }
        removeProximityView();
        if (shouldReleaseProximity(reason)) {
            this.mIsProximityHeld = false;
            this.mIsDimAmbient = true;
            this.mSensorManager.unregisterListener(this.mListener);
            Log.i(TAG, "unregister proximity sensor listener");
            AccSensorListener accSensorListener = this.mAccListener;
            if (accSensorListener != null) {
                accSensorListener.unregister();
            }
            unregisterDeviceListener();
        }
    }

    public boolean isShowing() {
        return this.mProximityView != null && this.mIsViewAttached;
    }

    public void forceShowHint() {
        this.mHandler.post(new Runnable() {
            /* class huawei.com.android.server.policy.$$Lambda$HwScreenOnProximityLock$KREaw6On4HoqYKBGBHLtwfKDzn4 */

            @Override // java.lang.Runnable
            public final void run() {
                HwScreenOnProximityLock.this.lambda$forceShowHint$2$HwScreenOnProximityLock();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void prepareProximityView() {
        View notchView;
        removeProximityView();
        Log.i(TAG, "inflate View, config : " + this.mContext.getResources().getConfiguration());
        boolean isFoldFullScreen = IS_FOLDABLE && HwFoldScreenManagerEx.getDisplayMode() == 1;
        initProximityView(isFoldFullScreen);
        HwScreenOnProximityLayout hwScreenOnProximityLayout = this.mProximityView;
        if (hwScreenOnProximityLayout == null) {
            Log.e(TAG, "initProximityView null");
            return;
        }
        if (this.mHasNotchInScreen && (notchView = hwScreenOnProximityLayout.findViewById(HwPartResourceUtils.getResourceId("disable_touch_notchview"))) != null) {
            ViewGroup.LayoutParams notchParam = notchView.getLayoutParams();
            LinearLayout.LayoutParams params = null;
            if (notchParam instanceof LinearLayout.LayoutParams) {
                params = (LinearLayout.LayoutParams) notchParam;
            }
            if (params != null) {
                params.height = HwNotchSizeUtil.getNotchSize()[1];
                notchView.setLayoutParams(params);
            } else {
                Log.e(TAG, "prepareProximityView params is null");
            }
        }
        setHintLayout(isFoldFullScreen);
        setBottomView();
        setProximityViewParam();
        Log.i(TAG, "prepareProximityView addView ");
    }

    private View getProximityLayout() {
        if (this.mIsHasSensorDevice) {
            return View.inflate(this.mContext, 34013254, null);
        }
        int viewId = HwPartResourceUtils.getResourceId("screen_on_proximity_view_for_tp");
        if (viewId == -1) {
            return View.inflate(this.mContext, 34013254, null);
        }
        return View.inflate(this.mContext, viewId, null);
    }

    private void initProximityView(boolean isFoldFullScreen) {
        View view;
        if (!isLandScape() || isFoldFullScreen) {
            view = getProximityLayout();
            View hintView = view.findViewById(HwPartResourceUtils.getResourceId("disable_touch_hint"));
            LinearLayout ll = null;
            if (hintView instanceof LinearLayout) {
                ll = (LinearLayout) hintView;
            }
            if (ll == null) {
                Log.e(TAG, "initProximityView ll is null");
                return;
            }
            DisplayMetrics dm = new DisplayMetrics();
            this.mWindowManager.getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;
            int i = width * 5;
            ll.setLayoutParams(new LinearLayout.LayoutParams(width, isFoldFullScreen ? i / 8 : i / 4));
        } else {
            view = View.inflate(this.mContext, HwPartResourceUtils.getResourceId("screen_on_proximity_view_land"), null);
        }
        if (view == null) {
            Log.e(TAG, "view is null");
        } else if (!(view instanceof HwScreenOnProximityLayout)) {
            Log.e(TAG, "view not instanceof HwScreenOnProximityLayout");
        } else {
            this.mProximityView = (HwScreenOnProximityLayout) view;
            this.mHintView = this.mProximityView.findViewById(34603159);
            if (this.mIsHasSensorDevice) {
                this.mProximityView.setEventListener(new HwScreenOnProximityLayout.EventListener() {
                    /* class huawei.com.android.server.policy.HwScreenOnProximityLock.AnonymousClass8 */

                    public void onDownEvent(MotionEvent event) {
                        HwScreenOnProximityLock.this.lambda$forceShowHint$2$HwScreenOnProximityLock();
                        if (HwScreenOnProximityLock.this.mFrameView != null) {
                            HwScreenOnProximityLock.this.mFrameView.startTextViewAnimal();
                        }
                    }
                });
            }
        }
    }

    private void setProximityViewParam() {
        this.mParams = new WindowManager.LayoutParams(-1, -1, 2100, HwWindowManager.LayoutParams.FLAG_CAPTURE_KNUCKLES | 1280 | 134217728, -2);
        WindowManagerEx.LayoutParamsEx paramsEx = new WindowManagerEx.LayoutParamsEx(this.mParams);
        paramsEx.addInputFeatures(4);
        paramsEx.addPrivateFlags(WindowManagerEx.LayoutParamsEx.getPrivateFlagHideNaviBar() | WindowManagerEx.LayoutParamsEx.getPrivateFlagShowForAllUsers());
        this.mParams.setTitle(PROXIMITY_WND_NAME);
        if (this.mHasNotchInScreen) {
            WindowManagerEx.LayoutParamsEx.setDisplayCutoutModeAlways(this.mParams);
        }
        if (!BuildConfig.FLAVOR.equals(HW_CURVED_SIDE)) {
            new WindowManagerEx.LayoutParamsEx(this.mParams).setDisplaySideMode(1);
        }
    }

    private void setHintLayout(boolean isFoldFullScreen) {
        View hintView = this.mProximityView.findViewById(HwPartResourceUtils.getResourceId("mis_touch_hint_layout"));
        LinearLayout hintLayout = null;
        if (hintView instanceof LinearLayout) {
            hintLayout = (LinearLayout) hintView;
        }
        if (hintLayout != null) {
            hintLayout.setOnTouchListener(new View.OnTouchListener() {
                /* class huawei.com.android.server.policy.HwScreenOnProximityLock.AnonymousClass9 */

                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            if (isFoldFullScreen) {
                hintLayout.setPadding(hintLayout.getPaddingLeft(), (int) this.mContext.getResources().getDimension(HwPartResourceUtils.getResourceId("fold_disable_touch_padding_top")), hintLayout.getPaddingRight(), hintLayout.getPaddingBottom());
            }
        }
    }

    private void setBottomView() {
        View bottomView = this.mProximityView.findViewById(HwPartResourceUtils.getResourceId("mis_touch_bottom"));
        if (bottomView != null) {
            bottomView.setOnTouchListener(new View.OnTouchListener() {
                /* class huawei.com.android.server.policy.HwScreenOnProximityLock.AnonymousClass10 */

                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
    }

    private boolean isLandScape() {
        int i = this.mRotation;
        return i == 1 || i == 3;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* renamed from: showHintView */
    public void lambda$forceShowHint$2$HwScreenOnProximityLock() {
        View view;
        if (this.mProximityView != null && (view = this.mHintView) != null && view.getVisibility() != 0 && this.mIsViewAttached) {
            this.mHintView.setVisibility(0);
            BallFrameView ballFrameView = this.mFrameView;
            if (ballFrameView != null) {
                ballFrameView.setBallViewVisibal(0);
            }
            Log.i(TAG, "showHintView ");
            long token = Binder.clearCallingIdentity();
            try {
                SettingsEx.System.putIntForUser(this.mContext.getContentResolver(), "proximity_wnd_status", 1, ActivityManagerEx.getCurrentUser());
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addProximityView() {
        CoverManager coverManager;
        int i;
        int i2 = 0;
        if (this.mIsNeedRefresh) {
            prepareProximityView();
            this.mIsNeedRefresh = false;
        }
        if (this.mProximityView == null || this.mPhoneWindowManager.isKeyguardShortcutApps() || (this.mPhoneWindowManager.isLandscape() && this.mPhoneWindowManager.isLsKeyguardShortcutApps())) {
            Log.i(TAG, "no need to addProximityView");
        } else if (this.mIsTouchHeadDown && this.mHeadDownStatus != 1 && ((i = this.mRotation) == 1 || i == 3)) {
            Log.i(TAG, "no need to addProximityView when screen is horizontal not headdown");
        } else if (this.mProximityView != null && this.mParams != null && !this.mIsViewAttached && this.mIsProximityHeld && (coverManager = this.mCoverManager) != null && coverManager.isCoverOpen()) {
            AccSensorListener accSensorListener = this.mAccListener;
            if (accSensorListener == null || !accSensorListener.isFlat()) {
                writePocketMode(ROOT_CAUSE_RESOLVE_PATTERN_LOG, POCKET_IN);
                Log.i(TAG, "addProximityView ");
                this.mWindowManager.addView(this.mProximityView, this.mParams);
                this.mFrameView = this.mProximityView.findViewById(HwPartResourceUtils.getResourceId("close_layout"));
                BallFrameView ballFrameView = this.mFrameView;
                if (ballFrameView != null) {
                    ballFrameView.updateStart(0);
                }
                restoreHintTextView();
                View view = this.mHintView;
                if (view != null) {
                    view.setVisibility(PROXIMITY_ENABLE ? 8 : 0);
                    BallFrameView ballFrameView2 = this.mFrameView;
                    if (ballFrameView2 != null) {
                        if (PROXIMITY_ENABLE) {
                            i2 = 8;
                        }
                        ballFrameView2.setBallViewVisibal(i2);
                    }
                    this.mIsViewAttached = true;
                    if (!this.mIsHasSensorDevice) {
                        lambda$forceShowHint$2$HwScreenOnProximityLock();
                        BallFrameView ballFrameView3 = this.mFrameView;
                        if (ballFrameView3 != null) {
                            ballFrameView3.startTextViewAnimal();
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeProximityView() {
        BallFrameView ballFrameView = this.mFrameView;
        if (ballFrameView != null) {
            ballFrameView.updateAnimCount(0);
        }
        HwScreenOnProximityLayout hwScreenOnProximityLayout = this.mProximityView;
        if (hwScreenOnProximityLayout != null && this.mIsViewAttached) {
            ViewParent vp = hwScreenOnProximityLayout.getParent();
            if (vp == null) {
                writePocketMode(ROOT_CAUSE_RESOLVE_PATTERN_LOG, POCKET_OUT);
                this.mWindowManager.removeViewImmediate(this.mProximityView);
                this.mIsViewAttached = false;
                Log.i(TAG, "removeview directly ");
            } else if (this.mWindowManager == null || !ViewRootImplEx.isViewRootImpl(vp)) {
                Log.w(TAG, "removeView fail: mWindowManager = " + this.mWindowManager + ", viewparent = " + vp);
            } else {
                writePocketMode(ROOT_CAUSE_RESOLVE_PATTERN_LOG, POCKET_OUT);
                Log.i(TAG, "removeProximityView success vp " + vp);
                this.mWindowManager.removeViewImmediate(this.mProximityView);
                this.mIsViewAttached = false;
            }
            long token = Binder.clearCallingIdentity();
            try {
                SettingsEx.System.putIntForUser(this.mContext.getContentResolver(), "proximity_wnd_status", 0, ActivityManagerEx.getCurrentUser());
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public void refreshForRotationChange(int rotation) {
        this.mHandler.post(new Runnable(rotation) {
            /* class huawei.com.android.server.policy.$$Lambda$HwScreenOnProximityLock$WVkuuR9mZrz1HS0IP_EtnucGugA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwScreenOnProximityLock.this.lambda$refreshForRotationChange$3$HwScreenOnProximityLock(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$refreshForRotationChange$3$HwScreenOnProximityLock(int rotation) {
        this.mRotation = rotation;
        lambda$forceRefreshHintView$4$HwScreenOnProximityLock();
    }

    public void forceRefreshHintView() {
        this.mHandler.post(new Runnable() {
            /* class huawei.com.android.server.policy.$$Lambda$HwScreenOnProximityLock$uv2PglRJj4zDU2MmT1L6vWUfS8 */

            @Override // java.lang.Runnable
            public final void run() {
                HwScreenOnProximityLock.this.lambda$forceRefreshHintView$4$HwScreenOnProximityLock();
            }
        });
    }

    public void refreshHintTextView() {
        TextView animText = null;
        View animView = this.mProximityView.findViewById(HwPartResourceUtils.getResourceId("anim_text"));
        if (animView instanceof TextView) {
            animText = (TextView) animView;
        }
        if (animText == null) {
            Log.e(TAG, "refreshHintTextView animText is null");
            return;
        }
        String swipeText = this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("mistouch_prevention_quitnote2"));
        animText.setText(swipeText);
        animText.setContentDescription(swipeText);
        BallFrameView ballFrameView = this.mFrameView;
        if (ballFrameView != null) {
            ballFrameView.setContentDescription(swipeText);
        }
        Log.i(TAG, "refreshHintTextView: " + swipeText);
    }

    public void restoreHintTextView() {
        TextView animText = null;
        View animView = this.mProximityView.findViewById(HwPartResourceUtils.getResourceId("anim_text"));
        if (animView instanceof TextView) {
            animText = (TextView) animView;
        }
        if (animText == null) {
            Log.e(TAG, "restoreHintTextView animText is null");
            return;
        }
        String swipeText = this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("mistouch_prevention_quitnote"));
        animText.setText(swipeText);
        animText.setContentDescription(swipeText);
        BallFrameView ballFrameView = this.mFrameView;
        if (ballFrameView != null) {
            ballFrameView.setContentDescription(swipeText);
        }
        Log.i(TAG, "restoreHintTextView: " + swipeText);
    }

    public void swipeExitHintView() {
        releaseLock(0);
        if (this.mIsHasSensorDevice) {
            report(2, 1);
        } else {
            report(2, 2);
        }
        Log.i(TAG, "quit mistouch for swipe twice");
    }

    /* access modifiers changed from: private */
    /* renamed from: refreshHintViewInternal */
    public void lambda$forceRefreshHintView$4$HwScreenOnProximityLock() {
        View view = this.mHintView;
        if (view != null) {
            boolean isHeadUpStatus = true;
            this.mIsNeedRefresh = true;
            boolean shouldShow = view.getVisibility() == 0;
            if (this.mHeadDownStatus != 2) {
                isHeadUpStatus = false;
            }
            if (this.mIsProximityHeld && this.mIsProximity) {
                if (this.mIsDimAmbient || !isHeadUpStatus) {
                    Log.i(TAG, "refresh view, current screen rotation : " + this.mRotation);
                    addProximityView();
                    if (shouldShow) {
                        lambda$forceShowHint$2$HwScreenOnProximityLock();
                    }
                }
            }
        }
    }

    private void writePocketMode(String destPath, String content) {
        FileWriter fw;
        FileWriter fw2 = null;
        try {
            if (getFileSize(destPath) < ROOT_CAUSE_RESOLVE_PATTERN_PATTERN_FILE_SIZE) {
                fw = new FileWriter(destPath, true);
            } else {
                fw = new FileWriter(destPath);
            }
            String fileContent = "time [" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "]" + content + System.lineSeparator();
            fw.write(fileContent, 0, fileContent.length());
            fw.flush();
            Log.i(TAG, "Write" + fileContent + "success.");
            try {
                fw.close();
            } catch (IOException e) {
                Log.e(TAG, "close fw error");
            }
        } catch (IOException e2) {
            Log.e(TAG, "Failed to write" + content);
            if (0 != 0) {
                fw2.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fw2.close();
                } catch (IOException e3) {
                    Log.e(TAG, "close fw error");
                }
            }
            throw th;
        }
    }

    private int getFileSize(String path) {
        File file = new File(path);
        if (file.exists()) {
            return (int) file.length();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public class AccSensorListener implements SensorEventListener {
        private boolean mIsFlat;
        private boolean mIsListening;

        private AccSensorListener() {
            this.mIsListening = false;
            this.mIsFlat = false;
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            if (event != null && event.values != null && event.values.length >= 3) {
                boolean z = false;
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];
                float sqrt = (float) Math.sqrt((double) ((float) (Math.pow((double) axisX, 2.0d) + Math.pow((double) axisY, 2.0d) + Math.pow((double) axisZ, 2.0d))));
                float pitch = (float) (-Math.asin((double) (axisY / sqrt)));
                float roll = (float) Math.asin((double) (axisX / sqrt));
                if (((double) pitch) >= -0.8726646491148832d && ((double) pitch) <= 0.2617993877991494d && ((double) Math.abs(roll)) <= 0.5235987755982988d && axisZ >= 5.0f) {
                    z = true;
                }
                this.mIsFlat = z;
                if (this.mIsFlat) {
                    HwScreenOnProximityLock.this.removeProximityView();
                } else if (!HwScreenOnProximityLock.this.mIsProximity) {
                } else {
                    if (HwScreenOnProximityLock.this.mHeadDownStatus == 1 || HwScreenOnProximityLock.this.mIsDimAmbient) {
                        HwScreenOnProximityLock.this.addProximityView();
                    }
                }
            }
        }

        public void register() {
            Sensor sensor = HwScreenOnProximityLock.this.mSensorManager.getDefaultSensor(1);
            if (sensor == null) {
                Log.w(HwScreenOnProximityLock.TAG, "AccSensorListener register failed, because of orientation sensor is not existed");
                return;
            }
            Log.d(HwScreenOnProximityLock.TAG, "AccSensorListener sensortype " + sensor.getType());
            if (this.mIsListening) {
                Log.w(HwScreenOnProximityLock.TAG, "AccSensorListener already register");
                return;
            }
            Log.i(HwScreenOnProximityLock.TAG, "AccSensorListener register");
            this.mIsListening = HwScreenOnProximityLock.this.mSensorManager.registerListener(this, sensor, 3, HwScreenOnProximityLock.this.mHandler);
        }

        public void unregister() {
            if (!this.mIsListening) {
                Log.w(HwScreenOnProximityLock.TAG, "AccSensorListener not register yet");
                return;
            }
            Log.i(HwScreenOnProximityLock.TAG, "AccSensorListener unregister");
            HwScreenOnProximityLock.this.mSensorManager.unregisterListener(this);
            this.mIsListening = false;
        }

        public boolean isFlat() {
            Log.i(HwScreenOnProximityLock.TAG, "AccSensorListener mIsFlat " + this.mIsFlat);
            return this.mIsFlat;
        }
    }

    /* access modifiers changed from: private */
    public class ProximitySensorListener implements SensorEventListener {
        private ProximitySensorListener() {
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            boolean z = false;
            if (event.sensor.getType() == 8) {
                float d1 = event.values[0];
                int d2 = 0;
                if (event.values.length >= 3) {
                    d2 = (((int) event.values[2]) >> 8) & 1;
                }
                HwScreenOnProximityLock hwScreenOnProximityLock = HwScreenOnProximityLock.this;
                hwScreenOnProximityLock.mIsProximityGen = d1 >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && d1 < hwScreenOnProximityLock.mProximityThreshold;
                HwScreenOnProximityLock.this.mIsProximityDyn = d2 == 1;
                HwScreenOnProximityLock hwScreenOnProximityLock2 = HwScreenOnProximityLock.this;
                if (hwScreenOnProximityLock2.mIsSupportSensorFeature && !HwScreenOnProximityLock.this.mIsProximityGen && HwScreenOnProximityLock.this.mHeadDownStatus == 1 && HwScreenOnProximityLock.this.mPhoneWindowManager.isKeyguardLocked()) {
                    z = true;
                }
                hwScreenOnProximityLock2.mIsUseSensorFeature = z;
                HwScreenOnProximityLock hwScreenOnProximityLock3 = HwScreenOnProximityLock.this;
                hwScreenOnProximityLock3.mIsProximity = hwScreenOnProximityLock3.mIsUseSensorFeature ? HwScreenOnProximityLock.this.mIsProximityDyn : HwScreenOnProximityLock.this.mIsProximityGen;
                Log.i(HwScreenOnProximityLock.TAG, "handleSensorChanged, close to proximity: " + HwScreenOnProximityLock.this.mIsProximity + ",ambient:" + HwScreenOnProximityLock.this.mIsDimAmbient + ",mIsSupportSensorFeature:" + HwScreenOnProximityLock.this.mIsSupportSensorFeature + ",mHeadDownStatus:" + HwScreenOnProximityLock.this.mHeadDownStatus + ",mIsUseSensorFeature:" + HwScreenOnProximityLock.this.mIsUseSensorFeature);
            } else if (event.sensor.getType() == 5) {
                float lux = event.values[0];
                HwScreenOnProximityLock hwScreenOnProximityLock4 = HwScreenOnProximityLock.this;
                if (lux < -1.0f) {
                    z = true;
                }
                hwScreenOnProximityLock4.mIsDimAmbient = z;
            }
            handleSensorChanges();
        }

        private void handleSensorChanges() {
            if (HwScreenOnProximityLock.this.mCoverManager == null) {
                Log.e(HwScreenOnProximityLock.TAG, "mCoverManager is null");
                return;
            }
            boolean isCoverOpen = HwScreenOnProximityLock.this.mCoverManager.isCoverOpen();
            boolean isHeadDown = HwScreenOnProximityLock.this.mHeadDownStatus == 1;
            if (HwScreenOnProximityLock.this.mIsProximity && isCoverOpen && (isHeadDown || HwScreenOnProximityLock.this.mIsDimAmbient)) {
                HwScreenOnProximityLock.this.addProximityView();
                HwScreenOnProximityLock.this.report(1, 1);
            } else if (isCoverOpen || HwScreenOnProximityLock.this.mProximityView != null) {
                HwScreenOnProximityLock.this.releaseLockInternal(2);
            } else {
                Log.i(HwScreenOnProximityLock.TAG, "no need to releaseLock");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void report(int pfts, int pftr) {
        IMonitor.EventStream eventStream = IMonitor.openEventStream((int) TP_LOG);
        if (eventStream != null) {
            eventStream.setParam((short) P_F_T_S, pfts);
            eventStream.setParam((short) P_F_T_R, pftr);
            IMonitor.sendEvent(eventStream);
            IMonitor.closeEventStream(eventStream);
        }
    }
}
