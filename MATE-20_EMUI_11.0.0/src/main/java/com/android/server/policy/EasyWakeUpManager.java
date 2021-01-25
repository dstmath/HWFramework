package com.android.server.policy;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.cover.CoverManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.HwFoldScreenState;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Flog;
import android.util.HwLog;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.android.server.policy.EasyWakeUpView;
import com.huawei.android.app.WindowManagerExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.IntentExEx;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.hidl.IServiceManagerHidlAdapter;
import com.huawei.android.hidl.IServiceNotificationHidlAdapter;
import com.huawei.android.hidl.ITouchscreenHidlAdapter;
import com.huawei.android.internal.policy.IKeyguardDismissCallbackEx;
import com.huawei.android.internal.widget.LockPatternUtilsEx;
import com.huawei.android.os.HandlerEx;
import com.huawei.android.os.HwBinderEx;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.server.HwPCFactory;
import com.huawei.server.hwmultidisplay.DefaultHwMultiDisplayUtils;
import com.huawei.server.policy.keyguard.KeyguardServiceDelegateEx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class EasyWakeUpManager extends DefaultEasyWakeUpManager implements EasyWakeUpView.EasyWakeUpCallback, SensorEventListener {
    private static final int ACC_CHECK_TIMES = 6;
    private static final long ACC_WATCH_TIME = 50;
    private static final String[] APP_START_LATTERS = {NAME_APP_BROWSER, NAME_APP_FLASHLIGHT};
    private static final int AXIS_VALUE_LENGTH = 3;
    private static final int AXIS_X_INDEX = 0;
    private static final int AXIS_Y_INDEX = 1;
    private static final int AXIS_Z_INDEX = 2;
    private static final float AXIS_Z_THRESHOLD = 5.0f;
    private static final int BIT_MOVE_16 = 16;
    private static final String CAMERA_PACKAGE_NAME = "com.huawei.camera";
    private static final String CONSTANTS_USER = "root";
    private static final int DEFAULT_KEY_CODE = -1;
    private static final int DEFAULT_LETTER_WAKE_UP_POINT_LIST_SIZE = 6;
    private static final int DEFAULT_NORMAL_START_INFO_LENGTH = 2;
    private static final int DEFAULT_SENSOR_CHECK_TIMES = 6;
    private static final float DEFAULT_SENSOR_FAR = 5.0f;
    private static final float DEFAULT_SENSOR_NEAR = 0.0f;
    private static final float DEFAULT_SENSOR_VALUE = -1.0f;
    private static final long DEFAULT_SENSOR_WATCH_TIME = 50;
    private static final int DEFAULT_WAKEUP_DOUBLE_STATE = -1;
    private static final long DEFAULT_WAKE_LOCK_TIME_1000 = 1000;
    private static final int DELAY_TIME_100_MS = 100;
    private static final int DEVIDE_EQUALY_FACTOR = 2;
    private static final int D_INT = 4;
    private static final String EASYWAKEUP = "easywakeup";
    private static final String EASYWAKEUP_SHOWNAVIBAR_ACTION = "com.huawei.android.easywakeup.SHOWNAVIBAR";
    private static final String EASYWAKE_ENABLE_FLAG = "persist.sys.easyflag";
    private static final String EASYWAKE_ENABLE_SURPPORT_FLAG = "persist.sys.surpport.easyflag";
    private static final String FCDT_EASY_WAKEUP = "FCDT-EASYWAKEUP";
    private static final int HEX_RADIX_CODE = 16;
    private static final int HIGH_BYTES_MASK = -65536;
    private static final Intent INSECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA").addFlags(268435456).addFlags(536870912).addFlags(67108864).setPackage(CAMERA_PACKAGE_NAME);
    private static final float INVALID_SENSOR_VALUE = -1.0f;
    private static final String INWARD_FOLD_GESTURE_PATH1 = "sys/touchscreen0/easy_wakeup_gesture";
    private static final String INWARD_FOLD_GESTURE_PATH2 = "sys/touchscreen1/easy_wakeup_gesture";
    private static final boolean IS_NEED_OPEN_FACE = SystemPropertiesEx.getBoolean("ro.config.hw_easywakeup_openface", false);
    private static final boolean IS_PROXIMITY_TOP = SystemPropertiesEx.getBoolean("ro.config.proximity_top", false);
    private static final String KEY_EASYWAKE_GESTURE = "com.huawei.easywakeup.gesture";
    private static final String KEY_EASYWAKE_POSITION = "com.huawei.easywakeup.position";
    private static final String[] KEY_WAKEUPS = {"persist.sys.easywakeup.up", "persist.sys.easywakeup.down", "persist.sys.easywakeup.left", "persist.sys.easywakeup.right", "persist.sys.easywakeup.o", "persist.sys.easywakeup.c", "persist.sys.easywakeup.e", "persist.sys.easywakeup.m", "persist.sys.easywakeup.w", "persist.sys.easywakeup.v", "persist.sys.easywakeup.s"};
    private static final int LOW_BYTES_MASK = 65535;
    private static final int MATH_POW_NUM = 2;
    private static final int MAX_ANIMATETIME = 10000;
    private static final int MAX_TIMES_CHECK_KEYGUARD = 10;
    private static final int MIN_POINT_LIST_SIZE = 2;
    private static final String NAME_APP_BROWSER = "com.android.browser;com.android.browser.BrowserActivity";
    private static final String NAME_APP_CAMERA = "com.huawei.camera;com.huawei.camera";
    private static final String NAME_APP_FLASHLIGHT = "com.android.systemui;com.android.systemui.flashlight.FlashlightActivity";
    private static final float PITCH_HIGHER = 12.0f;
    private static final float PITCH_LOWER = 3.6f;
    private static final float ROLL_THRESHOLD = 6.0f;
    private static final Intent SECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").addFlags(8388608).addFlags(268435456).addFlags(536870912).addFlags(67108864).setPackage(CAMERA_PACKAGE_NAME);
    private static final int SENSOR_DELAY_SECOND = 1000000;
    private static final int SINGLE_TOUCH_ENABLE_FLAG = 32768;
    private static final String STARTFLG = "startflg";
    private static final String TAG = "EasyWakeUpManager";
    private static final int THREAD_SLEEP_TIME_50 = 50;
    private static final int TP_ENABLE = 1;
    private static final int TP_HAL_DEATH_COOKIE = 1001;
    private static final int TP_STOP = 0;
    private static final String WAKEUP_DOUBLE_SWITCH_STATE = "wakeup_double_switch_state";
    private static final String WAKEUP_GESTURE_STATUS_PATH = "/sys/devices/platform/huawei_touch/easy_wakeup_gesture";
    private static final String WAKEUP_REASON = "WakeUpReason";
    private static int sCoverScreenKeyCode = -1;
    private static int sDoubleTouchKeyCode = -1;
    private static String sEasyWakeDataPath = "/sys/devices/platform/huawei_touch/touch_gesture_wakeup_position";
    private static EasyWakeUpManager sEasywakeupmanager;
    private static int sFlickDownKeyCode = -1;
    private static int sFlickLetfKeyCode = -1;
    private static int sFlickRightKeyCode = -1;
    private static int sFlickUpKeyCode = -1;
    private static DefaultHwMultiDisplayUtils sHwMultiDisplayUtils = HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwMultiDisplayUtils();
    private static int sLetterCcKeyCode = -1;
    private static int sLetterEeKeyCode = -1;
    private static int sLetterMmKeyCode = -1;
    private static int sLetterWwKeyCode = -1;
    private static int sMaxKeyCode = -1;
    private static int sMinKeyCode = -1;
    private static int sSensorCheckTimes = 6;
    private static float sSensorFar = 5.0f;
    private static float sSensorNear = 0.0f;
    private static long sSensorWatchTime = 50;
    private static int sSingleTouchKeyCode = -1;
    private AccSensorListener mAccSensorListener;
    private Runnable mAnimateRunable = new Runnable() {
        /* class com.android.server.policy.EasyWakeUpManager.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            EasyWakeUpManager.this.mIsAnimate = false;
        }
    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.EasyWakeUpManager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.w(EasyWakeUpManager.TAG, "onReceive, the intent is null!");
                return;
            }
            String action = intent.getAction();
            if ("com.huawei.android.cover.STATE".equals(action)) {
                EasyWakeUpManager.this.mIsCoverOpen = intent.getBooleanExtra("coverOpen", true);
                Log.i(EasyWakeUpManager.TAG, "COVER_STATE change to " + EasyWakeUpManager.this.mIsCoverOpen);
                if (!EasyWakeUpManager.this.mIsCoverOpen) {
                    EasyWakeUpManager.this.turnOffSensorListener();
                }
            } else if (IntentExEx.getActionUserSwitched().equals(action)) {
                EasyWakeUpManager.this.saveTouchPointNodePath();
            }
        }
    };
    private Context mContext;
    private CoverManager mCoverManager;
    private EasyWakeUpAnimationCallback mEasyWakeUpAnimationCallback;
    private EasyWakeUpView mEasyWakeUpView = null;
    private HwFoldScreenManagerInternal mFoldScreenManagerService;
    private Handler mHandler;
    private boolean mIsActive = false;
    private boolean mIsAnimate = false;
    private boolean mIsCoverOpen = true;
    private boolean mIsPowerOptimizeSwitchOn = false;
    private boolean mIsSensorUnRegisted = false;
    private boolean mIsSensorforHandleKey = false;
    private boolean mIsSensorforHandleTp = false;
    private boolean mIsSuccessProcessEasyWakeUp = true;
    private boolean mIsSupportProximity = true;
    private boolean mIsVibratorFirs = true;
    private KeyguardServiceDelegateEx mKeyguardDelegate;
    private final Object mLock = new Object();
    private PowerManager mPowerManager = null;
    private ITouchscreenHidlAdapter mProxy = null;
    private SensorManager mSensorManager = null;
    private float mSensorVaule = -1.0f;
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private int mTouchPanelWakeupGestureStatus = 0;
    private Vibrator mVibrator;
    private int mWakeIndex = 0;
    private WindowManager mWindowManager = null;
    private LockPatternUtilsEx mlockpatternutils;

    public interface EasyWakeUpAnimationCallback {
        void afterTrackAnimation();
    }

    /* access modifiers changed from: private */
    public class AccSensorListener implements SensorEventListener {
        private boolean mIsAccChecked;
        private boolean mIsFlat;
        private boolean mIsListening;

        private AccSensorListener() {
            this.mIsListening = false;
            this.mIsFlat = false;
            this.mIsAccChecked = false;
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
                this.mIsAccChecked = true;
            }
        }

        public void register() {
            Sensor sensor = EasyWakeUpManager.this.mSensorManager.getDefaultSensor(1);
            if (sensor == null) {
                Log.w(EasyWakeUpManager.TAG, "AccSensorListener register failed, because of orientation sensor is not existed");
            } else if (this.mIsListening) {
                Log.w(EasyWakeUpManager.TAG, "AccSensorListener already register");
            } else {
                Log.i(EasyWakeUpManager.TAG, "AccSensorListener register");
                this.mIsListening = EasyWakeUpManager.this.mSensorManager.registerListener(this, sensor, 0, EasyWakeUpManager.this.mHandler);
                this.mIsAccChecked = false;
                this.mIsFlat = false;
            }
        }

        public void unregister() {
            if (!this.mIsListening) {
                Log.w(EasyWakeUpManager.TAG, "AccSensorListener not register yet");
                return;
            }
            Log.i(EasyWakeUpManager.TAG, "AccSensorListener unregister");
            EasyWakeUpManager.this.mSensorManager.unregisterListener(this);
            this.mIsListening = false;
        }

        public boolean isFlat() {
            Log.i(EasyWakeUpManager.TAG, "AccSensorListener mIsFlat : " + this.mIsFlat);
            return this.mIsFlat;
        }

        public boolean getAccChecked() {
            Log.i(EasyWakeUpManager.TAG, "AccSensorListener mIsAccChecked : " + this.mIsAccChecked);
            return this.mIsAccChecked;
        }
    }

    public EasyWakeUpManager(Context context, Handler handler, KeyguardServiceDelegateEx keyguardDelegate) {
        super(context, handler, keyguardDelegate);
        this.mContext = context;
        this.mKeyguardDelegate = keyguardDelegate;
        this.mHandler = handler;
        this.mlockpatternutils = new LockPatternUtilsEx(context);
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mCoverManager = new CoverManager();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.android.cover.STATE");
        filter.addAction(IntentExEx.getActionUserSwitched());
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        try {
            if (!IServiceManagerHidlAdapter.getService().registerForNotifications("vendor.huawei.hardware.tp@1.0::ITouchscreen", "", this.mServiceNotification)) {
                Log.e(TAG, "Failed to register service start notification");
            }
            connectToProxy();
            this.mIsSupportProximity = supportProximity();
            if (IS_PROXIMITY_TOP) {
                this.mAccSensorListener = new AccSensorListener();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register service start notification");
        }
    }

    public static EasyWakeUpManager getInstance(Context context, Handler handler, KeyguardServiceDelegateEx keyguardDelegate) {
        EasyWakeUpManager easyWakeUpManager;
        synchronized (EasyWakeUpManager.class) {
            if (sEasywakeupmanager == null) {
                sEasywakeupmanager = new EasyWakeUpManager(context, handler, keyguardDelegate);
            }
            easyWakeUpManager = sEasywakeupmanager;
        }
        return easyWakeUpManager;
    }

    private boolean supportProximity() {
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager == null || sensorManager.getDefaultSensor(8) == null) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToProxy() {
        synchronized (this.mLock) {
            if (this.mProxy != null) {
                Log.i(TAG, "mProxy has registered, donnot regitster again");
                return;
            }
            try {
                this.mProxy = ITouchscreenHidlAdapter.getService();
                if (this.mProxy != null) {
                    Log.i(TAG, "connectToProxy: mProxy get success.");
                    this.mProxy.linkToDeath(new DeathRecipient(), (int) TP_HAL_DEATH_COOKIE);
                } else {
                    Log.i(TAG, "connectToProxy: mProxy get failed.");
                }
            } catch (NoSuchElementException e) {
                Log.e(TAG, "connectToProxy: tp hal service not found. Did the service fail to start?", e);
            } catch (RemoteException e2) {
                Log.e(TAG, "connectToProxy: tp hal service not responding");
            }
        }
    }

    private void setEasyWeakupGestureReportEnableHal(boolean isEnabled) {
        synchronized (this.mLock) {
            if (this.mProxy == null) {
                Log.e(TAG, "mProxy is null, return");
                return;
            }
            try {
                if (!this.mProxy.hwTsSetEasyWeakupGestureReportEnable(isEnabled)) {
                    Log.i(TAG, "hwTsSetEasyWeakupGestureReportEnable error");
                } else {
                    Log.i(TAG, "hwTsSetEasyWeakupGestureReportEnable success");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to set glove mode:");
            }
        }
    }

    private void setEasyWakeupGestureHal(int status) {
        synchronized (this.mLock) {
            if (this.mProxy == null) {
                Log.e(TAG, "mProxy is null, return");
                return;
            }
            try {
                if (!this.mProxy.hwTsSetEasyWeakupGesture(status)) {
                    Log.i(TAG, "hwTsSetEasyWeakupGesture error");
                } else {
                    Log.i(TAG, "hwTsSetEasyWeakupGesture success");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to set glove mode:");
            }
        }
    }

    private int getWakeupIndex(int keyCode) {
        if (keyCode == sFlickUpKeyCode) {
            return 0;
        }
        if (keyCode == sFlickDownKeyCode) {
            return 1;
        }
        if (keyCode == sFlickLetfKeyCode) {
            return 2;
        }
        if (keyCode == sFlickRightKeyCode) {
            return 3;
        }
        if (keyCode == sDoubleTouchKeyCode) {
            return -1;
        }
        if (keyCode == sSingleTouchKeyCode) {
            return 9;
        }
        if (keyCode == sLetterCcKeyCode) {
            return 5;
        }
        if (keyCode == sLetterEeKeyCode) {
            return 6;
        }
        if (keyCode == sLetterMmKeyCode) {
            return 7;
        }
        if (keyCode == sLetterWwKeyCode) {
            return 8;
        }
        return -2;
    }

    private void processCoverScreenKeyCode(boolean isHapticFeedback) {
        PowerManager powerManager = this.mPowerManager;
        if (powerManager != null && powerManager.isScreenOn() && this.mIsVibratorFirs) {
            Log.e(TAG, "sCoverScreenKeyCode will vibrator mIsVibratorFirs = " + this.mIsVibratorFirs);
            Vibrator vibrator = this.mVibrator;
            if (vibrator != null && isHapticFeedback) {
                vibrator.vibrate(50);
            }
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.policy.EasyWakeUpManager.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    if (EasyWakeUpManager.this.mPowerManager.isScreenOn()) {
                        PowerManagerEx.goToSleep(EasyWakeUpManager.this.mPowerManager, SystemClock.uptimeMillis(), 0, 0);
                    }
                    EasyWakeUpManager.this.mIsVibratorFirs = true;
                }
            }, 100);
            this.mIsVibratorFirs = false;
        }
    }

    public boolean processEasyWakeUp(int keyCode) {
        PowerManager powerManager;
        Log.v(TAG, " processEasyWakeUp and the keyCode from driver is : " + keyCode);
        boolean isHapticFeedback = SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 1, -2) != 0;
        if (keyCode == sCoverScreenKeyCode) {
            processCoverScreenKeyCode(isHapticFeedback);
            return false;
        }
        int wakeIndex = getWakeupIndex(keyCode);
        if (wakeIndex == -2) {
            return false;
        }
        if ((this.mIsActive || ((powerManager = this.mPowerManager) != null && powerManager.isScreenOn() && sHwMultiDisplayUtils.isScreenOnForHwMultiDisplay())) && !HwFoldScreenState.isFoldScreenDevice()) {
            Log.v(TAG, "processEasyWakeUp return false for mIsActive is : " + this.mIsActive);
            return false;
        }
        PowerManager powerManager2 = this.mPowerManager;
        if (powerManager2 != null) {
            powerManager2.newWakeLock(1, TAG).acquire(DEFAULT_WAKE_LOCK_TIME_1000);
        }
        this.mWakeIndex = wakeIndex;
        if (wakeIndex == -1 || wakeIndex == 9) {
            Log.v(TAG, "process double or single click screen wakeup index: " + wakeIndex);
            if (HwFoldScreenState.isFoldScreenDevice()) {
                synchronized (this.mLock) {
                    try {
                        this.mProxy.hwTsGetEasyWeakupGuestureData(new ITouchscreenHidlAdapter.HwTsGetEasyWeakupGuestureDataCallbackAdapter(new ITouchscreenHidlAdapter()) {
                            /* class com.android.server.policy.EasyWakeUpManager.AnonymousClass4 */

                            {
                                Objects.requireNonNull(x0);
                            }

                            public void onValues(boolean isRet, ArrayList<Integer> gestureDataList) {
                                if (gestureDataList == null || gestureDataList.size() == 0) {
                                    Log.e(EasyWakeUpManager.TAG, "no data upload");
                                } else {
                                    EasyWakeUpManager.this.handleGuestureData(gestureDataList);
                                }
                            }
                        });
                    } catch (RemoteException e) {
                        Log.e(TAG, "onvalues error");
                    }
                }
            } else {
                sHwMultiDisplayUtils.lightScreenOnForHwMultiDisplay();
                PowerManagerEx.wakeUp(this.mPowerManager, SystemClock.uptimeMillis(), TAG);
                Log.i(TAG, "bdReport TYPE_SINGLE_TOUCH_LIGHTUP_SCREEN");
                Flog.bdReport(991311015);
                doFaceRecognizeIfNeeded();
                HwLog.dubaie("DUBAI_TAG_SCREEN_ON_EVENT", "event=ScreenTap");
            }
            lockOnPadAssistMode();
            return true;
        }
        unLockScreen(this.mlockpatternutils.isLockScreenDisabled(0));
        Log.i(TAG, "easywakeup processEasyWakeUp and startTrackAnimation start");
        startTrackAnimation(wakeIndex);
        String startInfo = SettingsEx.System.getStringForUser(this.mContext.getContentResolver(), KEY_WAKEUPS[this.mWakeIndex], -2);
        if (startInfo != null && !checkAppNeedStart(startInfo)) {
            startActivity(startInfo);
        }
        return true;
    }

    private void lockOnPadAssistMode() {
        boolean isPadAssistMode = HwPCUtils.isPadAssistantMode();
        Log.d(TAG, "IS_PAD_ASSIST_MODE is " + isPadAssistMode);
        if (isPadAssistMode) {
            boolean isScreenPowerOn = false;
            try {
                IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
                if (pcMgr != null && !pcMgr.isScreenPowerOn()) {
                    Log.d(TAG, "isScreenPowerOn: true.");
                    isScreenPowerOn = true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Caught an exception when get screen status.");
                isScreenPowerOn = false;
            }
            if (!isScreenPowerOn) {
                try {
                    WindowManagerExt.lockNow((Bundle) null);
                    Log.d(TAG, "LOCK device succesful.");
                } catch (RemoteException e2) {
                    Log.e(TAG, "Error while trying to lock device.", e2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGuestureData(ArrayList<Integer> gustureData) {
        int valueX = (gustureData.get(0).intValue() & HIGH_BYTES_MASK) >> 16;
        int valueY = gustureData.get(0).intValue() & 65535;
        Log.i(TAG, "gustureData is " + gustureData.get(0) + "valueX=" + valueX + " valueY=" + valueY);
        if (this.mFoldScreenManagerService == null) {
            this.mFoldScreenManagerService = (HwFoldScreenManagerInternal) LocalServicesExt.getService(HwFoldScreenManagerInternal.class);
        }
        Bundle extra = new Bundle();
        Point clickPosition = new Point(valueX, valueY);
        extra.putInt("uid", Binder.getCallingUid());
        extra.putString("opPackageName", ContextEx.getOpPackageName(this.mContext));
        extra.putInt("reason", 4);
        extra.putString("details", "magnetic.wakeUp");
        extra.putParcelable("position", clickPosition);
        this.mFoldScreenManagerService.onDoubleClick(this.mPowerManager.isScreenOn(), extra);
        if (HwFoldScreenState.isInwardFoldDevice()) {
            doFaceRecognizeIfNeeded();
        }
    }

    private void doFaceRecognizeIfNeeded() {
        HwPhoneWindowManager policy;
        if (IS_NEED_OPEN_FACE && (policy = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager()) != null) {
            Log.i(TAG, "doFaceRecognize for FCDT-EASYWAKEUP");
            Settings.Global.putString(this.mContext.getContentResolver(), WAKEUP_REASON, FCDT_EASY_WAKEUP);
            policy.doFaceRecognize(true, FCDT_EASY_WAKEUP);
        }
    }

    private boolean checkAppNeedStart(String startInfo) {
        for (String appStartLetter : APP_START_LATTERS) {
            if (appStartLetter.equals(startInfo)) {
                return true;
            }
        }
        return false;
    }

    public boolean setEasyWakeUpFlag(int flag) {
        this.mContext.enforceCallingPermission("android.permission.EASY_WAKE_UP", "set EasyWakeUp Flag");
        Log.i(TAG, "setEasyWakeUpFlag flag: " + flag);
        setGestureValue(EasyWakeUpXmlParse.getDriverGesturePath(), flag);
        return true;
    }

    public void startEasyWakeUpActivity() {
        try {
            KeyguardManager keyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
            int times = 0;
            if (keyguardManager != null) {
                while (keyguardManager.inKeyguardRestrictedInputMode() && times < 10) {
                    Log.i(TAG, "startEasyWakeUpActivity and waiting for conplete screen on");
                    Thread.sleep(50);
                    times++;
                }
                if (keyguardManager.inKeyguardRestrictedInputMode()) {
                    this.mKeyguardDelegate.dismiss((IKeyguardDismissCallbackEx) null, (CharSequence) null);
                }
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Error on Thread sleep");
        }
        if (this.mWakeIndex >= 0) {
            String startInfo = SettingsEx.System.getStringForUser(this.mContext.getContentResolver(), KEY_WAKEUPS[this.mWakeIndex], -2);
            if (startInfo != null && checkAppNeedStart(startInfo)) {
                startActivity(startInfo);
            }
            this.mIsActive = false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0058, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005d, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005e, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0061, code lost:
        throw r5;
     */
    public void setGestureValue(String path, int flag) {
        int singleTouchEnable = EasyWakeUpXmlParse.getTouchEnableValue(EasyWakeUpXmlParse.SINGLE_TOUCH);
        if (singleTouchEnable == 0) {
            singleTouchEnable = 32768;
        }
        if ((singleTouchEnable & flag) != 0) {
            flag &= ~singleTouchEnable;
        }
        Log.i(TAG, "singleTouchEnable:" + singleTouchEnable + ",setGestureValue flag:" + flag + ",path: " + path);
        try {
            FileOutputStream file = new FileOutputStream(new File(path));
            file.write(String.valueOf(flag).getBytes(Charset.forName("UTF-8")));
            file.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException happened when set gesture to file ");
        }
        if (HwFoldScreenState.isFoldScreenDevice() && HwFoldScreenState.isInwardFoldDevice()) {
            FileOutputStream file2 = null;
            FileOutputStream file1 = null;
            try {
                file2 = new FileOutputStream(new File(INWARD_FOLD_GESTURE_PATH1));
                file2.write(String.valueOf(flag).getBytes(Charset.forName("UTF-8")));
                file1 = new FileOutputStream(new File(INWARD_FOLD_GESTURE_PATH2));
                file1.write(String.valueOf(flag).getBytes(Charset.forName("UTF-8")));
            } catch (IOException e2) {
                Log.e(TAG, "IOException happened when set gesture to file in fold screen device");
            } catch (Throwable th) {
                closeFileOutputStream(file2);
                closeFileOutputStream(file1);
                throw th;
            }
            closeFileOutputStream(file2);
            closeFileOutputStream(file1);
        }
    }

    private void closeFileOutputStream(FileOutputStream fileOutputStream) {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "closeFileOutputStream error!");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<Point> getFlickTouchPointData(int wakeIndex) {
        List<Point> dataList = new ArrayList<>();
        int disX = this.mContext.getResources().getDisplayMetrics().widthPixels;
        int disY = this.mContext.getResources().getDisplayMetrics().heightPixels;
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            disX = disY;
            disY = disX;
        }
        if (wakeIndex == 0) {
            dataList.add(new Point(disX / 2, disY));
            dataList.add(new Point(disX / 2, 0));
        } else if (wakeIndex == 1) {
            dataList.add(new Point(disX / 2, 0));
            dataList.add(new Point(disX / 2, disY));
        } else if (wakeIndex == 2) {
            dataList.add(new Point(disX, disY / 2));
            dataList.add(new Point(0, disY / 2));
        } else if (wakeIndex == 3) {
            dataList.add(new Point(0, disY / 2));
            dataList.add(new Point(disX, disY / 2));
        }
        return dataList;
    }

    private boolean startTrackAnimation(final int wakeIndex) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.EasyWakeUpManager.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                Log.i(EasyWakeUpManager.TAG, "easywakeup wake up the CPU in main thread");
                EasyWakeUpManager.this.mPowerManager.newWakeLock(1, EasyWakeUpManager.TAG).acquire(10000);
                List<Point> pointList = EasyWakeUpManager.this.getTouchPointData(wakeIndex);
                if (!checkPointList(pointList, wakeIndex)) {
                    int i = wakeIndex;
                    if (i < 4) {
                        pointList = EasyWakeUpManager.this.getFlickTouchPointData(i);
                        if (pointList.size() < 2) {
                            EasyWakeUpManager.this.mIsSuccessProcessEasyWakeUp = false;
                            Log.v(EasyWakeUpManager.TAG, "flick startTrackAnimation and return false, pointList is empty or size <2");
                            return;
                        }
                        Log.i(EasyWakeUpManager.TAG, "flick startTrackAnimation");
                    } else {
                        Log.i(EasyWakeUpManager.TAG, "startTrackAnimation and return false for the pointList is Null");
                        EasyWakeUpManager.this.mIsAnimate = false;
                        return;
                    }
                }
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1);
                lp.type = 2010;
                lp.flags = 1280;
                new WindowManagerEx.LayoutParamsEx(lp).addPrivateFlags(WindowManagerEx.LayoutParamsEx.getPrivateFlagHideNaviBar());
                lp.format = -1;
                lp.setTitle("EasyWakeUp");
                lp.screenOrientation = 1;
                if (!EasyWakeUpManager.this.mIsActive && EasyWakeUpManager.this.mWindowManager != null) {
                    if (EasyWakeUpManager.this.mEasyWakeUpView == null) {
                        EasyWakeUpManager easyWakeUpManager = EasyWakeUpManager.this;
                        easyWakeUpManager.mEasyWakeUpView = new EasyWakeUpView(easyWakeUpManager.mContext, EasyWakeUpManager.this.mWakeIndex);
                        EasyWakeUpManager.this.mEasyWakeUpView.setEasyWakeUpCallback(EasyWakeUpManager.this);
                        if (EasyWakeUpManager.this.mPowerManager != null) {
                            EasyWakeUpManager.this.mEasyWakeUpView.setPowerManager(EasyWakeUpManager.this.mPowerManager);
                        }
                    }
                    EasyWakeUpManager.this.mWindowManager.addView(EasyWakeUpManager.this.mEasyWakeUpView, lp);
                    EasyWakeUpManager.this.mIsActive = true;
                }
                EasyWakeUpManager.this.mEasyWakeUpView.startTrackAnimation(pointList, wakeIndex);
            }

            private boolean checkPointList(List<Point> pointList, int wakeIndex) {
                if (pointList == null || pointList.size() == 0) {
                    Log.e(EasyWakeUpManager.TAG, "startTrackAnimation and return false for the pointList is empty");
                    return false;
                } else if (wakeIndex >= 4 && pointList.size() == 6) {
                    return true;
                } else {
                    if (wakeIndex < 4 && pointList.size() == 2) {
                        return true;
                    }
                    Log.e(EasyWakeUpManager.TAG, "startTrackAnimation and return false for the size of pointList is error");
                    return false;
                }
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadcast() {
        Log.i(TAG, "sendBroadcast showNaviBar");
        this.mContext.sendBroadcastAsUser(new Intent(EASYWAKEUP_SHOWNAVIBAR_ACTION), UserHandleEx.ALL, "com.huawei.easywakeup.permission.RECV_EASYWAKEUP_SHOWNAVIBAR");
    }

    @Override // com.android.server.policy.EasyWakeUpView.EasyWakeUpCallback
    public void disappearTrackAnimation() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.policy.EasyWakeUpManager.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                EasyWakeUpManager.this.removeEasyWakeUpView();
                if (EasyWakeUpManager.this.mEasyWakeUpAnimationCallback != null) {
                    EasyWakeUpManager.this.mEasyWakeUpAnimationCallback.afterTrackAnimation();
                }
                EasyWakeUpManager.this.startEasyWakeUpActivity();
                EasyWakeUpManager.this.sendBroadcast();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeEasyWakeUpView() {
        EasyWakeUpView easyWakeUpView;
        WindowManager windowManager;
        if (this.mIsActive && (easyWakeUpView = this.mEasyWakeUpView) != null && (windowManager = this.mWindowManager) != null) {
            windowManager.removeView(easyWakeUpView);
            this.mEasyWakeUpView = null;
            Log.i(TAG, "EasyWakeUpView is removed.");
            this.mIsActive = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x007a A[SYNTHETIC, Splitter:B:38:0x007a] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00a8 A[SYNTHETIC, Splitter:B:57:0x00a8] */
    private List<Point> getTouchPointData(int wakeIndex) {
        Throwable th;
        int len;
        List<Point> dataList = new ArrayList<>();
        if (wakeIndex < 0 || wakeIndex > 8) {
            Log.i(TAG, "getTouchPointData return null for the wakeIndex is not available");
            return dataList;
        }
        int len2 = 2;
        if (wakeIndex >= 4) {
            len2 = 6;
        }
        RandomAccessFile indexFile = null;
        try {
            indexFile = new RandomAccessFile(sEasyWakeDataPath, "r");
            String data = indexFile.readLine();
            if (data != null) {
                int index = 0;
                while (true) {
                    len = len2 - 1;
                    if (len2 <= 0) {
                        break;
                    }
                    try {
                        int index2 = index + 4;
                        index = index2 + 4;
                        dataList.add(new Point(Integer.parseInt(data.substring(index, index + 4), 16), Integer.parseInt(data.substring(index2, index2 + 4), 16)));
                        len2 = len;
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "getTouchPointData FileNotFoundException for read file");
                        if (indexFile != null) {
                            indexFile.close();
                        }
                        return dataList;
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "getTouchPointData NumberFormatException for read file");
                        if (indexFile != null) {
                            indexFile.close();
                        }
                        return dataList;
                    } catch (IOException e3) {
                        try {
                            Log.e(TAG, "get IOException when read file");
                            if (indexFile != null) {
                                try {
                                    indexFile.close();
                                } catch (FileNotFoundException e4) {
                                    Log.i(TAG, "getTouchPointData the file(NODE for driver) is not be found while close file");
                                } catch (IOException e5) {
                                    Log.i(TAG, "getTouchPointData Exception for IOException while close file");
                                }
                            }
                            return dataList;
                        } catch (Throwable th2) {
                            th = th2;
                            if (indexFile != null) {
                                try {
                                    indexFile.close();
                                } catch (FileNotFoundException e6) {
                                    Log.i(TAG, "getTouchPointData the file(NODE for driver) is not be found while close file");
                                } catch (IOException e7) {
                                    Log.i(TAG, "getTouchPointData Exception for IOException while close file");
                                }
                            }
                            throw th;
                        }
                    }
                }
                len2 = len;
            }
            try {
                indexFile.close();
            } catch (FileNotFoundException e8) {
                Log.i(TAG, "getTouchPointData the file(NODE for driver) is not be found while close file");
            } catch (IOException e9) {
                Log.i(TAG, "getTouchPointData Exception for IOException while close file");
            }
        } catch (FileNotFoundException e10) {
            Log.e(TAG, "getTouchPointData FileNotFoundException for read file");
            if (indexFile != null) {
            }
            return dataList;
        } catch (NumberFormatException e11) {
            Log.e(TAG, "getTouchPointData NumberFormatException for read file");
            if (indexFile != null) {
            }
            return dataList;
        } catch (IOException e12) {
            Log.e(TAG, "get IOException when read file");
            if (indexFile != null) {
            }
            return dataList;
        } catch (Throwable th3) {
            th = th3;
            if (indexFile != null) {
            }
            throw th;
        }
        return dataList;
    }

    public void saveTouchPointNodePath() {
        SettingsEx.Secure.putIntForUser(this.mContext.getContentResolver(), EASYWAKE_ENABLE_SURPPORT_FLAG, EasyWakeUpXmlParse.getDefaultSupportValueFromCust(), -2);
        SettingsEx.Secure.putStringForUser(this.mContext.getContentResolver(), KEY_EASYWAKE_POSITION, EasyWakeUpXmlParse.getDriverPostionPath(), -2);
        String gesturePath = EasyWakeUpXmlParse.getDriverGesturePath();
        SettingsEx.Secure.putStringForUser(this.mContext.getContentResolver(), KEY_EASYWAKE_GESTURE, gesturePath, -2);
        saveParsedItemsToDb();
        int flag = getFlagValue();
        int doubleTouchState = getDoubleTouchState();
        int gesture = EasyWakeUpXmlParse.getDefaultValueFromCust();
        Log.i(TAG, "init getFlagValue:" + flag + ",getDoubleTouchState:" + doubleTouchState + ",gesture:" + gesture);
        if (!shouldResetTouchValue(flag, doubleTouchState)) {
            Log.i(TAG, "setCemwValuesForDriver set gesture value: " + flag);
            if (flag > 0) {
                setGestureValue(gesturePath, setCemwValuesForDriver(flag));
            } else {
                setFlagValue(gesture);
                setGestureValue(gesturePath, doubleTouchState);
            }
        } else {
            setFlagValue(gesture);
            saveParsedItemsToDb();
            Log.i(TAG, "set user flag value: " + gesture + ",doubleTouchState:" + doubleTouchState);
            int value = setCemwValuesForDriver(gesture);
            setDoubleTouchState(value);
            Log.i(TAG, "setDoubleTouchState:" + value);
        }
        readDatabaseItems();
    }

    private boolean shouldResetTouchValue(int value, int doubleTouchState) {
        if (value <= 0 && doubleTouchState == -1) {
            return true;
        }
        int flag = setCemwValuesForDriver(value);
        Log.i(TAG, "setCemwValuesForDriver flag: " + flag);
        if ((EasyWakeUpXmlParse.getTouchEnableValue(EasyWakeUpXmlParse.SINGLE_TOUCH) & flag) != 0 && !EasyWakeUpXmlParse.checkTouchSupport(EasyWakeUpXmlParse.SINGLE_TOUCH)) {
            return true;
        }
        if ((EasyWakeUpXmlParse.getTouchEnableValue(EasyWakeUpXmlParse.DOUBLE_TOUCH) & flag) == 0 || EasyWakeUpXmlParse.checkTouchSupport(EasyWakeUpXmlParse.DOUBLE_TOUCH)) {
            return false;
        }
        return true;
    }

    private int getIndexFrmoDatabase(String str) {
        try {
            return SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), str + "_index", -2);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "SettingNotFoundException happened when getIndexFrmoDatabase");
            return -1;
        }
    }

    private int setCemwValuesForDriver(int allValue) {
        int value = allValue;
        int allIndex = getIndexFrmoDatabase(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_ALL);
        int letterIndexC = getIndexFrmoDatabase(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_C);
        int letterIndexE = getIndexFrmoDatabase(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_E);
        int letterIndexM = getIndexFrmoDatabase(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_M);
        int letterIndexW = getIndexFrmoDatabase(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_W);
        int flickAllIndex = getIndexFrmoDatabase(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_ALL);
        int upIndex = getIndexFrmoDatabase(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_UP);
        int downIndex = getIndexFrmoDatabase(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_DOWN);
        int leftIndex = getIndexFrmoDatabase(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_LEFT);
        int rightIndex = getIndexFrmoDatabase(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_RIGHT);
        if (((1 << allIndex) & allValue) == 0) {
            value = (~((1 << letterIndexC) | (1 << letterIndexE) | (1 << letterIndexM) | (1 << letterIndexW))) & allValue;
        }
        if (((1 << flickAllIndex) & allValue) != 0) {
            return value;
        }
        return value & (~((1 << upIndex) | (1 << downIndex) | (1 << leftIndex) | (1 << rightIndex)));
    }

    private int getFlagValue() {
        try {
            return SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EASYWAKE_ENABLE_FLAG, -2);
        } catch (Settings.SettingNotFoundException e) {
            Log.i(TAG, "SettingNotFoundException happened when getting flag from settings");
            return 0;
        }
    }

    private void setFlagValue(int flag) {
        SettingsEx.Secure.putIntForUser(this.mContext.getContentResolver(), EASYWAKE_ENABLE_FLAG, flag, -2);
    }

    private int getDoubleTouchState() {
        try {
            return SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), WAKEUP_DOUBLE_SWITCH_STATE, -2);
        } catch (Settings.SettingNotFoundException e) {
            Log.i(TAG, "SettingNotFoundException happened when getting double touch from settings");
            return -1;
        }
    }

    private void setDoubleTouchState(int state) {
        SettingsEx.Secure.putIntForUser(this.mContext.getContentResolver(), WAKEUP_DOUBLE_SWITCH_STATE, state, -2);
    }

    private boolean isCalling() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager == null || telephonyManager.getCallState() == 0) {
            return false;
        }
        return true;
    }

    private boolean processSensorChange() {
        if (!this.mIsSupportProximity) {
            return false;
        }
        turnOffSensorListener();
        processAccChangeIfNeeded();
        AccSensorListener accSensorListener = this.mAccSensorListener;
        if (accSensorListener == null || !accSensorListener.isFlat()) {
            this.mIsSensorforHandleKey = true;
            SensorManager sensorManager = this.mSensorManager;
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(8), 0);
            this.mIsSensorUnRegisted = false;
            for (int i = 0; i < 6; i++) {
                try {
                    Thread.sleep(50);
                    if (this.mSensorVaule != -1.0f) {
                        break;
                    }
                } catch (InterruptedException e) {
                    Log.i(TAG, "thread sleep InterruptedException");
                }
            }
            this.mSensorManager.unregisterListener(this);
            this.mIsSensorforHandleKey = false;
            this.mIsSensorUnRegisted = true;
            float f = this.mSensorVaule;
            if (f < 5.0f && f != -1.0f) {
                Log.e(TAG, "do nothing for easywakeup because of PROXIMITY is " + this.mSensorVaule);
                turnOnSensorListener();
                setEasyWeakupGestureReportEnableHal(true);
                this.mSensorVaule = -1.0f;
                return true;
            }
        }
        this.mSensorVaule = -1.0f;
        return false;
    }

    private void handleWakeUpKeyInternal(int code) {
        if (this.mSensorManager == null || code == sCoverScreenKeyCode || !processSensorChange()) {
            if (!(code == sDoubleTouchKeyCode || code == sSingleTouchKeyCode)) {
                if (isCalling()) {
                    Log.e(TAG, "do nothing because Calling !");
                    setEasyWeakupGestureReportEnableHal(true);
                    return;
                } else if (code != sCoverScreenKeyCode) {
                    this.mIsAnimate = true;
                    this.mHandler.postDelayed(this.mAnimateRunable, 10000);
                }
            }
            this.mEasyWakeUpAnimationCallback = new EasyWakeUpAnimationCallback() {
                /* class com.android.server.policy.EasyWakeUpManager.AnonymousClass7 */

                @Override // com.android.server.policy.EasyWakeUpManager.EasyWakeUpAnimationCallback
                public void afterTrackAnimation() {
                    if (HandlerEx.hasCallbacks(EasyWakeUpManager.this.mHandler, EasyWakeUpManager.this.mAnimateRunable)) {
                        EasyWakeUpManager.this.mHandler.removeCallbacks(EasyWakeUpManager.this.mAnimateRunable);
                    }
                    EasyWakeUpManager.this.mIsAnimate = false;
                }
            };
            this.mIsSuccessProcessEasyWakeUp = true;
            if (processEasyWakeUp(code) && !this.mIsSuccessProcessEasyWakeUp) {
                Log.i(TAG, "write flick node 1 to driver because of fail EasyWakeUp");
                setEasyWeakupGestureReportEnableHal(true);
            }
        }
    }

    private void processAccChangeIfNeeded() {
        AccSensorListener accSensorListener = this.mAccSensorListener;
        if (accSensorListener != null) {
            accSensorListener.register();
            for (int i = 0; i < 6; i++) {
                try {
                    Thread.sleep(50);
                    if (this.mAccSensorListener.getAccChecked()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    Log.i(TAG, "thread sleep InterruptedException");
                }
            }
            this.mAccSensorListener.unregister();
        }
    }

    public boolean handleWakeUpKey(KeyEvent event, int screenOffReason) {
        PowerManager powerManager;
        Log.e(TAG, "enter wakeUpDoing !");
        if (!this.mIsAnimate || this.mEasyWakeUpView == null) {
            int code = event.getKeyCode();
            Log.i(TAG, "keycode: " + code + " minKeyCode: " + sMinKeyCode + " maxKeyCode: " + sMaxKeyCode);
            if (event.getAction() != 0 || ((code < sMinKeyCode || code > sMaxKeyCode) && code != sSingleTouchKeyCode)) {
                return false;
            }
            if (code != sCoverScreenKeyCode && (powerManager = this.mPowerManager) != null && powerManager.isScreenOn() && sHwMultiDisplayUtils.isScreenOnForHwMultiDisplay() && (!HwFoldScreenState.isFoldScreenDevice() || (HwFoldScreenState.isFoldScreenDevice() && code != sDoubleTouchKeyCode && code != sSingleTouchKeyCode))) {
                return false;
            }
            if (screenOffReason == WindowManagerPolicyEx.OFF_BECAUSE_OF_PROX_SENSOR) {
                setEasyWeakupGestureReportEnableHal(true);
                Log.e(TAG, "Off screen beacuse sensor !");
                return true;
            }
            handleWakeUpKeyInternal(code);
            return true;
        }
        Log.e(TAG, "easywakeup is animate !");
        return true;
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent arg0) {
        float[] its = arg0.values;
        Log.i(TAG, "proximity onSensorChanged");
        if (its != null && arg0.sensor.getType() == 8 && its.length > 0) {
            this.mSensorVaule = its[0];
            Log.i(TAG, "sensor value: its[0] = " + this.mSensorVaule);
        }
        if (this.mIsSensorforHandleTp && !this.mIsSensorforHandleKey) {
            int tmpWakeupEnable = 0;
            if (this.mSensorVaule >= sSensorFar) {
                tmpWakeupEnable = 1;
            }
            if (this.mTouchPanelWakeupGestureStatus != tmpWakeupEnable) {
                this.mTouchPanelWakeupGestureStatus = tmpWakeupEnable;
                Log.e(TAG, "told kernel of TP , " + this.mTouchPanelWakeupGestureStatus);
                setEasyWakeupGestureHal(this.mTouchPanelWakeupGestureStatus);
            }
        }
    }

    private void readDatabaseItems() {
        sCoverScreenKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.COVER_SCREEN + "_Keycode", -1, -2);
        sDoubleTouchKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.DOUBLE_TOUCH + "_Keycode", -1, -2);
        sFlickUpKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.EASYWAKEUP_FLICK_UP + "_Keycode", -1, -2);
        sFlickDownKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.EASYWAKEUP_FLICK_DOWN + "_Keycode", -1, -2);
        sFlickLetfKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.EASYWAKEUP_FLICK_LEFT + "_Keycode", -1, -2);
        sFlickRightKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.EASYWAKEUP_FLICK_RIGHT + "_Keycode", -1, -2);
        sLetterCcKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.EASYWAKEUP_LETTER_C + "_Keycode", -1, -2);
        sLetterEeKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.EASYWAKEUP_LETTER_E + "_Keycode", -1, -2);
        sLetterMmKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.EASYWAKEUP_LETTER_M + "_Keycode", -1, -2);
        sLetterWwKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.EASYWAKEUP_LETTER_W + "_Keycode", -1, -2);
        sMaxKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), "maxKeyCode", -1, -2);
        sMinKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), "minKeyCode", -1, -2);
        sEasyWakeDataPath = SettingsEx.Secure.getStringForUser(this.mContext.getContentResolver(), KEY_EASYWAKE_POSITION, -2);
        sSensorNear = SettingsEx.Secure.getFloatForUser(this.mContext.getContentResolver(), "sSensorNear", 0.0f, -2);
        sSensorFar = SettingsEx.Secure.getFloatForUser(this.mContext.getContentResolver(), "sSensorFar", 5.0f, -2);
        sSensorWatchTime = SettingsEx.Secure.getLongForUser(this.mContext.getContentResolver(), "sSensorWatchTime", 50, -2);
        sSensorCheckTimes = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), "sSensorCheckTimes", 6, -2);
        boolean z = false;
        if (SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), "power_optimize", 0, -2) == 1) {
            z = true;
        }
        this.mIsPowerOptimizeSwitchOn = z;
        sSingleTouchKeyCode = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), EasyWakeUpXmlParse.SINGLE_TOUCH + "_Keycode", -1, -2);
    }

    private void saveParsedItemsToDb() {
        setWakeUpIndexToDatabase();
        setDatabaseValue(EasyWakeUpXmlParse.COVER_SCREEN + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.COVER_SCREEN));
        setDatabaseValue(EasyWakeUpXmlParse.DOUBLE_TOUCH + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.DOUBLE_TOUCH));
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_UP + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_UP));
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_DOWN + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_DOWN));
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_LEFT + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_LEFT));
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_RIGHT + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_RIGHT));
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_C + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_C));
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_E + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_E));
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_M + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_M));
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_W + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_W));
        setDatabaseValue(EasyWakeUpXmlParse.SINGLE_TOUCH + "_Keycode", EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.SINGLE_TOUCH));
        Log.i(TAG, "sSingleTouchKeyCode: " + EasyWakeUpXmlParse.getKeyCodeByString(EasyWakeUpXmlParse.SINGLE_TOUCH));
        setDatabaseValue("maxKeyCode", EasyWakeUpXmlParse.getKeyCodeByString("maxKeyCode"));
        setDatabaseValue("minKeyCode", EasyWakeUpXmlParse.getKeyCodeByString("minKeyCode"));
        setDatabaseValue("DriverFileLength", EasyWakeUpXmlParse.getDriverFileLength());
        SettingsEx.Secure.putFloatForUser(this.mContext.getContentResolver(), "sSensorNear", EasyWakeUpXmlParse.getSensorNearValue(), -2);
        SettingsEx.Secure.putFloatForUser(this.mContext.getContentResolver(), "sSensorFar", EasyWakeUpXmlParse.getSensorFarValue(), -2);
        SettingsEx.Secure.putLongForUser(this.mContext.getContentResolver(), "sSensorWatchTime", EasyWakeUpXmlParse.getSensorWatchTime(), -2);
        setDatabaseValue("sSensorCheckTimes", EasyWakeUpXmlParse.getSensorCheckTimes());
        setDatabaseValue("power_optimize", EasyWakeUpXmlParse.getPowerOptimizeState());
    }

    private void setWakeUpIndexToDatabase() {
        setDatabaseValue(EasyWakeUpXmlParse.COVER_SCREEN + "_index", EasyWakeUpXmlParse.getCoverScreenIndex());
        setDatabaseValue(EasyWakeUpXmlParse.DOUBLE_TOUCH + "_index", EasyWakeUpXmlParse.getDoubleTouchIndex());
        setDatabaseValue(EasyWakeUpXmlParse.SINGLE_TOUCH + "_index", EasyWakeUpXmlParse.getSingleTouchIndex());
        Log.i(TAG, "Single_Touch_index: " + EasyWakeUpXmlParse.getSingleTouchIndex());
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_ALL + "_index", EasyWakeUpXmlParse.getIndexOfFlickAll());
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_UP + "_index", EasyWakeUpXmlParse.getIndexOfFlickUp());
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_DOWN + "_index", EasyWakeUpXmlParse.getIndexOfFlickDownE());
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_LEFT + "_index", EasyWakeUpXmlParse.getIndexOfFlickLeft());
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_FLICK_RIGHT + "_index", EasyWakeUpXmlParse.getIndexOfFlickRight());
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_ALL + "_index", EasyWakeUpXmlParse.getIndexOfLetterAll());
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_C + "_index", EasyWakeUpXmlParse.getIndexOfLetterC());
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_E + "_index", EasyWakeUpXmlParse.getIndexOfLetterE());
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_M + "_index", EasyWakeUpXmlParse.getIndexOfLetterM());
        setDatabaseValue(EasyWakeUpXmlParse.EASYWAKEUP_LETTER_W + "_index", EasyWakeUpXmlParse.getIndexOfLetterW());
    }

    private void setDatabaseValue(String str, int value) {
        SettingsEx.Secure.putIntForUser(this.mContext.getContentResolver(), str, value, -2);
    }

    private boolean isEasyWakeupEnabledByKernel() {
        BufferedReader reader = null;
        boolean isResultEanbled = false;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(WAKEUP_GESTURE_STATUS_PATH), Charset.defaultCharset()));
            String line = reader2.readLine();
            if (!(line == null || Integer.parseInt(line.trim().replaceAll("^0[x|X]", ""), 16) == 0)) {
                isResultEanbled = true;
            }
            try {
                reader2.close();
            } catch (IOException e) {
                Log.e(TAG, "isEasyWakeupEnabledByKernel Exception");
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "isEasyWakeupEnabledByKernel FileNotFoundException");
            if (0 != 0) {
                reader.close();
            }
        } catch (IOException e3) {
            Log.e(TAG, "isEasyWakeupEnabledByKernel IOException");
            if (0 != 0) {
                reader.close();
            }
        } catch (NumberFormatException e4) {
            Log.e(TAG, "isEasyWakeupEnabledByKernel NumberFormatException");
            if (0 != 0) {
                reader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e5) {
                    Log.e(TAG, "isEasyWakeupEnabledByKernel Exception");
                }
            }
            throw th;
        }
        Log.e(TAG, "isEasyWakeupEnabledByKernel " + isResultEanbled);
        return isResultEanbled;
    }

    private void unLockScreen(boolean isKeyguardable) {
        if (!isKeyguardable) {
            Log.w(TAG, "EasyWakeUpManager dismiss keyguard");
            this.mKeyguardDelegate.dismiss((IKeyguardDismissCallbackEx) null, (CharSequence) null);
        }
    }

    private boolean checkAppNeedStartCamera(String startInfo) {
        return NAME_APP_CAMERA.equals(startInfo);
    }

    private void startActivity(String startInfo) {
        String[] startInfos = startInfo.split(AwarenessInnerConstants.SEMI_COLON_KEY);
        if (startInfos.length == 2) {
            Log.v(TAG, "startEasyWakeUpActivity and this app is : " + startInfos[0] + " ; " + startInfos[1]);
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setClassName(startInfos[0], startInfos[1]);
            intent.putExtra(STARTFLG, EASYWAKEUP);
            intent.addFlags(805306368);
            try {
                if (checkAppNeedStartCamera(startInfo)) {
                    this.mContext.startActivity(INSECURE_CAMERA_INTENT);
                } else {
                    this.mContext.startActivity(intent);
                }
            } catch (IllegalStateException | SecurityException e) {
                Log.e(TAG, "exception is thrown when trying to startActivity");
            }
        }
    }

    public void turnOnSensorListener() {
        if (!this.mIsSupportProximity) {
            setEasyWakeupGestureHal(1);
            return;
        }
        CoverManager coverManager = this.mCoverManager;
        if (coverManager != null) {
            this.mIsCoverOpen = coverManager.isCoverOpen();
        }
        boolean tpEnabledByKernel = isEasyWakeupEnabledByKernel();
        Log.i(TAG, "turnOnSensorListener, registerListener , mIsCoverOpen " + this.mIsCoverOpen + ", tpEnabledByKernel + tpEnabledByKernel, mIsPowerOptimizeSwitchOn " + this.mIsPowerOptimizeSwitchOn);
        if (!this.mIsCoverOpen || !tpEnabledByKernel || !this.mIsPowerOptimizeSwitchOn) {
            this.mIsSensorforHandleTp = false;
        } else {
            SensorManager sensorManager = this.mSensorManager;
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(8), SENSOR_DELAY_SECOND);
            this.mIsSensorforHandleTp = true;
        }
        this.mIsSensorUnRegisted = false;
        this.mTouchPanelWakeupGestureStatus = 0;
    }

    public void turnOffSensorListener() {
        if (!this.mIsSupportProximity) {
            setEasyWakeupGestureHal(0);
            return;
        }
        if (this.mIsSensorUnRegisted || (!this.mIsSensorforHandleTp && !this.mIsSensorforHandleKey)) {
            Log.i(TAG, "turnOffSensorListener, already unregisterListener ");
        } else {
            Log.i(TAG, "turnOffSensorListener, unregisterListener ");
            this.mSensorManager.unregisterListener(this);
        }
        this.mIsSensorforHandleTp = false;
        this.mIsSensorUnRegisted = true;
    }

    /* access modifiers changed from: package-private */
    public final class DeathRecipient extends HwBinderEx.DeathRecipientEx {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1001) {
                Log.e(EasyWakeUpManager.TAG, "tp hal service died cookie: " + cookie);
                synchronized (EasyWakeUpManager.this.mLock) {
                    EasyWakeUpManager.this.mProxy = null;
                }
            }
        }
    }

    final class ServiceNotification extends IServiceNotificationHidlAdapter {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean isPreexisting) {
            Log.e(EasyWakeUpManager.TAG, "tp hal service started " + fqName + " " + name);
            EasyWakeUpManager.this.connectToProxy();
        }
    }
}
