package com.android.server.policy;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Flog;
import android.util.HwPCUtils;
import android.view.MotionEvent;
import com.android.server.am.PointerEventListenerEx;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.IntentExEx;
import com.huawei.android.os.HandlerEx;
import com.huawei.android.os.HwPowerManager;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.HwLogEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwextdevice.HWExtDeviceEvent;
import com.huawei.hwextdevice.HWExtDeviceEventListener;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.hwextdevice.devices.HWExtMotion;
import com.huawei.motiondetection.MRUtils;
import com.huawei.server.HwPCFactory;
import com.huawei.server.hwmultidisplay.DefaultHwMultiDisplayUtils;
import com.huawei.server.policy.DefaultPickUpWakeScreenManager;
import com.huawei.server.policy.keyguard.KeyguardServiceDelegateEx;
import huawei.android.security.facerecognition.FaceReportEventToIaware;
import java.util.ArrayList;
import java.util.List;

public class PickUpWakeScreenManager extends DefaultPickUpWakeScreenManager {
    private static final int DEFAULT_PICK_UP_MOTION = -1;
    private static final Uri HWMOTIONS_CONTENT_URI = Uri.parse("content://com.huawei.providers.motions/hwmotions");
    private static final boolean IS_SIMPLE_COVER = SystemPropertiesEx.getBoolean("ro.config.hw_simplecover", false);
    private static final boolean IS_SUPPORT_FACE_RECOGNITION = SystemPropertiesEx.getBoolean("ro.config.face_recognition", false);
    private static final boolean IS_SUPPORT_PICKUP_PROP = SystemPropertiesEx.getBoolean("ro.config.hw_wakeup_device", false);
    private static final boolean IS_TABLET = "tablet".equals(SystemPropertiesEx.get("ro.build.characteristics", "default"));
    private static final String MOTION_PICKUP_WAKEUP_DEVICE = "motion_pickup_wakeup_device";
    private static final int MSG_RECORD_WAKEUP = 2;
    private static final int MSG_SET_TURNOFF_CONTROLLER = 4;
    private static final int MSG_UPDATE_DEVICE_LISTENER = 3;
    private static final int MSG_WAIT_PROXIMITY_SENSOR_TIMEOUT = 1;
    private static final int[] PICKUP_SENSORS = {1};
    private static final int SCREEN_OFF_TIME_WITHOUT_TOUCH = 5000;
    private static final String TAG = "PickUpWakeScreenManager";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final long WAIT_PROXIMITY_SENSOR_TIMEOUT = 500;
    private static final String WAKEUP_REASON = "WakeUpReason";
    private static DefaultHwMultiDisplayUtils sHwMultiDisplayUtils = HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwMultiDisplayUtils();
    private static boolean sIsNeedControlTurnOff = false;
    private static boolean sIsSupportPickUpSensor = true;
    private static PickUpWakeScreenManager sPickUpWakeScreenManager;
    private Context mContext;
    private CoverManager mCoverManager;
    private HWExtMotion mHwExtMotion = null;
    private HWExtDeviceEventListener mHwedListener = null;
    private HWExtDeviceManager mHwedManager = null;
    private boolean mIsFirstBoot;
    private boolean mIsInFastScreenOn;
    private boolean mIsInitCompleted;
    private boolean mIsPickupSwitchOn;
    private boolean mIsProximitySensorEnabled;
    private boolean mIsTurnOffTimeOutEnabled;
    private KeyguardManager mKeyguardManager;
    private KeyguardServiceDelegateEx mKgDelegate;
    private Handler mPickUpHandler;
    private ContentObserver mPickupEnabledObserver;
    private final Runnable mPickupMotionTimeoutRunnable = new Runnable() {
        /* class com.android.server.policy.PickUpWakeScreenManager.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            PowerManager pm = (PowerManager) PickUpWakeScreenManager.this.mContext.getSystemService("power");
            if (pm != null && pm.isInteractive()) {
                SlogEx.i(PickUpWakeScreenManager.TAG, "ScreenOff because no user activty after pickup motion");
                PowerManagerEx.goToSleep(pm, SystemClock.uptimeMillis(), 0, 0);
            }
            PickUpWakeScreenManager.this.setTurnOffController(false);
        }
    };
    private Sensor mProximitySensor;
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        /* class com.android.server.policy.PickUpWakeScreenManager.AnonymousClass2 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            if (PickUpWakeScreenManager.this.mIsProximitySensorEnabled) {
                boolean isPositive = false;
                float distance = event.values[0];
                if (distance >= 0.0f && distance < PickUpWakeScreenManager.this.mProximityThreshold) {
                    isPositive = true;
                }
                SlogEx.i(PickUpWakeScreenManager.TAG, "onSensorChanged distance=" + distance + ", isPositive = " + isPositive + ",sensorEnabled=" + PickUpWakeScreenManager.this.mIsProximitySensorEnabled);
                PickUpWakeScreenManager.this.handleProximitySensorEvent(isPositive);
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private float mProximityThreshold;
    private ContentResolver mResolver;
    private ScreenTouchEventListener mScreenTouchEventListener = null;
    private SensorManager mSensorManager;
    private BroadcastReceiver mUserSwitchReceiver;
    private WindowManagerPolicyEx.WindowManagerFuncsEx mWindowManagerFuncs;

    private PickUpWakeScreenManager() {
        boolean z = false;
        this.mIsTurnOffTimeOutEnabled = false;
        this.mIsPickupSwitchOn = false;
        this.mIsInFastScreenOn = false;
        this.mIsFirstBoot = true;
        this.mIsInitCompleted = false;
        this.mPickupEnabledObserver = new ContentObserver(new Handler()) {
            /* class com.android.server.policy.PickUpWakeScreenManager.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                SlogEx.i(PickUpWakeScreenManager.TAG, "PickupEnabledObserver");
                PickUpWakeScreenManager.this.updatePickupSwitchState();
            }
        };
        this.mUserSwitchReceiver = new BroadcastReceiver() {
            /* class com.android.server.policy.PickUpWakeScreenManager.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                SlogEx.i(PickUpWakeScreenManager.TAG, "on receive userSwitchReceiver");
                PickUpWakeScreenManager.this.updatePickupSwitchState();
            }
        };
        if (IS_SUPPORT_PICKUP_PROP && !isSupportFaceDectect()) {
            z = true;
        }
        sIsNeedControlTurnOff = z;
        SlogEx.i(TAG, "IS_SUPPORT_PICKUP:" + IS_SUPPORT_PICKUP_PROP + ", mIsNeedControlTurnOff:" + sIsNeedControlTurnOff);
    }

    public static synchronized PickUpWakeScreenManager getInstance() {
        PickUpWakeScreenManager pickUpWakeScreenManager;
        synchronized (PickUpWakeScreenManager.class) {
            if (sPickUpWakeScreenManager == null) {
                sPickUpWakeScreenManager = new PickUpWakeScreenManager();
            }
            pickUpWakeScreenManager = sPickUpWakeScreenManager;
        }
        return pickUpWakeScreenManager;
    }

    public void initIfNeed(Context context, Handler handler, WindowManagerPolicyEx.WindowManagerFuncsEx windowManagerFuncs, KeyguardServiceDelegateEx keyguardDelegate) {
        this.mContext = context;
        sIsSupportPickUpSensor = isSupportPickUpSensor(this.mContext);
        SlogEx.i(TAG, "isSupportPickUpSensor:" + sIsSupportPickUpSensor);
        if (isSupportPickUp()) {
            this.mWindowManagerFuncs = windowManagerFuncs;
            this.mKgDelegate = keyguardDelegate;
            this.mResolver = this.mContext.getContentResolver();
            this.mPickUpHandler = new PickUpHandler(handler.getLooper());
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
            this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
            Sensor sensor = this.mProximitySensor;
            if (sensor != null) {
                float maximumRange = sensor.getMaximumRange();
                float f = TYPICAL_PROXIMITY_THRESHOLD;
                if (maximumRange < TYPICAL_PROXIMITY_THRESHOLD) {
                    f = this.mProximitySensor.getMaximumRange();
                }
                this.mProximityThreshold = f;
            }
            ContextEx.registerReceiverAsUser(this.mContext, this.mUserSwitchReceiver, UserHandleEx.ALL, new IntentFilter(IntentExEx.getActionUserSwitched()), (String) null, (Handler) null);
            ContentResolverExt.registerContentObserver(this.mContext.getContentResolver(), HWMOTIONS_CONTENT_URI, true, this.mPickupEnabledObserver, (int) DEFAULT_PICK_UP_MOTION);
            this.mHwedManager = HWExtDeviceManager.getInstance(this.mContext);
            this.mHwExtMotion = new HWExtMotion(100);
            this.mCoverManager = new CoverManager();
            this.mIsInitCompleted = true;
        }
    }

    public void enablePickupMotionOrNot(boolean isScreenOff) {
        int motionPickUp;
        if (isSupportPickUp() && this.mIsInitCompleted) {
            if (this.mIsFirstBoot && (motionPickUp = MRUtils.getMotionEnableStateAsUser(this.mContext, MOTION_PICKUP_WAKEUP_DEVICE, ActivityManagerEx.getCurrentUser())) != DEFAULT_PICK_UP_MOTION) {
                boolean z = true;
                if (motionPickUp != 1) {
                    z = false;
                }
                this.mIsPickupSwitchOn = z;
                this.mIsFirstBoot = false;
            }
            if (!this.mIsPickupSwitchOn && !HwPCUtils.isInWindowsCastMode()) {
                return;
            }
            if (Settings.Secure.getInt(this.mResolver, "device_provisioned", 0) == 0) {
                SlogEx.i(TAG, "Device is in Provision");
                return;
            }
            SlogEx.i(TAG, "isScreenOff:" + isScreenOff + ", mIsPickupSwitchOn:" + this.mIsPickupSwitchOn);
            if (isScreenOff) {
                scheduleUpdateDeviceListener();
            }
        }
    }

    public void stopTurnOffController() {
        setTurnOffController(false);
    }

    /* access modifiers changed from: private */
    public static boolean isSupportPickUp() {
        return IS_SUPPORT_PICKUP_PROP && sIsSupportPickUpSensor;
    }

    private static boolean isSupportFaceDectect() {
        return SystemPropertiesEx.getInt("ro.config.face_detect", 0) == 1 || SystemPropertiesEx.getBoolean("ro.config.face_recognition", false) || SystemPropertiesEx.getBoolean("ro.config.face_smart_keepon", false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setTurnOffController(boolean enable) {
        if (isSupportPickUp() && sIsNeedControlTurnOff) {
            KeyguardManager keyguardManager = this.mKeyguardManager;
            if (keyguardManager == null || !keyguardManager.isKeyguardLocked()) {
                SlogEx.i(TAG, "keyguard not locked enable:" + enable);
                Handler handler = this.mPickUpHandler;
                if (handler != null && HandlerEx.hasCallbacks(handler, this.mPickupMotionTimeoutRunnable)) {
                    SlogEx.i(TAG, "remove pick up motion timeout runnable");
                    this.mPickUpHandler.removeCallbacks(this.mPickupMotionTimeoutRunnable);
                    return;
                }
                return;
            }
            Handler handler2 = this.mPickUpHandler;
            if (handler2 != null) {
                handler2.removeMessages(MSG_SET_TURNOFF_CONTROLLER);
                this.mPickUpHandler.sendMessage(this.mPickUpHandler.obtainMessage(MSG_SET_TURNOFF_CONTROLLER, enable ? 1 : 0, 0));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTurnOffController(boolean enable) {
        SlogEx.i(TAG, "handleTurnOffController enable:" + enable + ", turnOffTimeOutEnabled:" + this.mIsTurnOffTimeOutEnabled);
        if (HandlerEx.hasCallbacks(this.mPickUpHandler, this.mPickupMotionTimeoutRunnable)) {
            this.mPickUpHandler.removeCallbacks(this.mPickupMotionTimeoutRunnable);
        }
        if (enable) {
            this.mPickUpHandler.postDelayed(this.mPickupMotionTimeoutRunnable, 5000);
        }
        this.mIsTurnOffTimeOutEnabled = enable;
        setPointerEventListenerEnable(enable);
    }

    private boolean isSupportPickUpSensor(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService("sensor");
        if (sensorManager == null) {
            return true;
        }
        List<Sensor> listSensors = sensorManager.getSensorList(DEFAULT_PICK_UP_MOTION);
        List<Integer> sensorTypeList = new ArrayList<>(listSensors.size());
        for (Sensor sensor : listSensors) {
            sensorTypeList.add(Integer.valueOf(sensor.getType()));
        }
        for (int sensorType : PICKUP_SENSORS) {
            if (!sensorTypeList.contains(Integer.valueOf(sensorType))) {
                return false;
            }
        }
        return true;
    }

    private void setPointerEventListenerEnable(boolean enable) {
        if (sIsNeedControlTurnOff) {
            if (!enable) {
                ScreenTouchEventListener screenTouchEventListener = this.mScreenTouchEventListener;
                if (screenTouchEventListener != null) {
                    this.mWindowManagerFuncs.unregisterPointerEventListener(screenTouchEventListener, 0);
                    this.mScreenTouchEventListener = null;
                    SlogEx.i(TAG, "unregisterPointerEventListener");
                }
            } else if (this.mScreenTouchEventListener == null) {
                this.mScreenTouchEventListener = new ScreenTouchEventListener();
                this.mWindowManagerFuncs.registerPointerEventListener(this.mScreenTouchEventListener, 0);
                SlogEx.i(TAG, "registerPointerEventListener");
            }
        }
    }

    private void triggerFaceDetect(String reason) {
        KeyguardServiceDelegateEx keyguardServiceDelegateEx = this.mKgDelegate;
        if (keyguardServiceDelegateEx != null) {
            keyguardServiceDelegateEx.doFaceRecognize(true, reason);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wakeupScreenOnIfNeeded(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService("power");
        if (pm == null) {
            SlogEx.e(TAG, "pm is null");
        } else if (!pm.isInteractive() || !sHwMultiDisplayUtils.isScreenOnForHwMultiDisplay()) {
            if (!HwPCUtils.isDisallowLockScreenForHwMultiDisplay()) {
                sHwMultiDisplayUtils.lightScreenOnForHwMultiDisplay();
            }
            FaceReportEventToIaware.reportEventToIaware(context, 20025);
            triggerFaceDetect("PICKUP:SC_OFF");
            wakeUpWaitForProximity();
        } else {
            SlogEx.i(TAG, "screen is already on");
            triggerFaceDetect("PICKUP:SC_ON");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setProximitySensorEnabled(boolean isEnabled) {
        if (isEnabled) {
            if (!this.mIsProximitySensorEnabled) {
                this.mIsProximitySensorEnabled = true;
                this.mSensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, MSG_UPDATE_DEVICE_LISTENER, this.mPickUpHandler);
                this.mPickUpHandler.sendEmptyMessageDelayed(1, WAIT_PROXIMITY_SENSOR_TIMEOUT);
                SlogEx.i(TAG, "proximity sensor registered.");
            }
        } else if (this.mIsProximitySensorEnabled) {
            this.mIsProximitySensorEnabled = false;
            this.mPickUpHandler.removeMessages(1);
            this.mSensorManager.unregisterListener(this.mProximitySensorListener);
            SlogEx.i(TAG, "proximity sensor unregistered.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleProximitySensorEvent(boolean isPositive) {
        if (this.mIsProximitySensorEnabled) {
            if (isPositive) {
                stopWakeUpReady(this.mContext, false);
            } else {
                stopWakeUpReady(this.mContext, true);
            }
            setProximitySensorEnabled(false);
        }
    }

    private void wakeUpWaitForProximity() {
        if (this.mIsInFastScreenOn) {
            SlogEx.w(TAG, "wakeUpWaitForProximity already in processing");
            return;
        }
        startWakeUpReady(this.mContext);
        setProximitySensorEnabled(true);
    }

    private void startWakeUpReady(Context context) {
        SlogEx.i(TAG, "wakeUpWaitForProximity start");
        if (!HwPCUtils.isDisallowLockScreenForHwMultiDisplay()) {
            HwPowerManager.startWakeUpReady(context, SystemClock.uptimeMillis());
        }
        this.mIsInFastScreenOn = true;
    }

    private void lockOnPadAssistMode(boolean isTurnOn) {
        if (isTurnOn && HwPCUtils.isPadAssistantMode()) {
            this.mWindowManagerFuncs.lockDeviceNow();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopWakeUpReady(Context context, boolean isTurnOn) {
        if (!this.mIsInFastScreenOn) {
            SlogEx.w(TAG, "stopWakeUpReady, not in fast screen on");
            return;
        }
        SlogEx.i(TAG, "stopWakeUpReady isTurnOn = " + isTurnOn);
        lockOnPadAssistMode(isTurnOn);
        if (!HwPCUtils.isDisallowLockScreenForHwMultiDisplay()) {
            HwPowerManager.stopWakeUpReady(context, SystemClock.uptimeMillis(), isTurnOn);
        } else if (isTurnOn) {
            SlogEx.i(TAG, "lightScreenOnForHwMultiDisplay");
            sHwMultiDisplayUtils.lightScreenOnForHwMultiDisplay();
            PowerManagerEx.userActivity((PowerManager) this.mContext.getSystemService("power"), SystemClock.uptimeMillis(), 0, 0);
        }
        this.mIsInFastScreenOn = false;
        if (isTurnOn) {
            setTurnOffController(true);
            if (IS_SUPPORT_FACE_RECOGNITION) {
                Handler handler = this.mPickUpHandler;
                handler.obtainMessage(MSG_RECORD_WAKEUP, 0, 0, "android.policy:HWPICKUP#" + Binder.getCallingUid()).sendToTarget();
            }
            boolean isBdReportSuccess = Flog.bdReport(this.mContext, 991310500);
            SlogEx.d(TAG, "report no proximity ScreenOn waked by pickup motion. EventId:991310500 isBdReportSuccess:" + isBdReportSuccess);
            HwLogEx.dubaie("DUBAI_TAG_SCREEN_ON_EVENT", "event=PickUp");
        }
    }

    private boolean getPickupSwitchOn() {
        return MRUtils.getMotionEnableStateAsUser(this.mContext, MOTION_PICKUP_WAKEUP_DEVICE, ActivityManagerEx.getCurrentUser()) == 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePickupSwitchState() {
        this.mIsPickupSwitchOn = getPickupSwitchOn();
        scheduleUpdateDeviceListener();
    }

    private void scheduleUpdateDeviceListener() {
        Handler handler = this.mPickUpHandler;
        if (handler != null && !handler.hasMessages(MSG_UPDATE_DEVICE_LISTENER)) {
            this.mPickUpHandler.sendEmptyMessage(MSG_UPDATE_DEVICE_LISTENER);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDeviceListener() {
        if (isSupportPickUp() && this.mHwedManager != null) {
            SlogEx.i(TAG, "updateDeviceListener mIsPickupSwitchOn:" + this.mIsPickupSwitchOn + ", isInWindowsCastMode:" + HwPCUtils.isInWindowsCastMode());
            if (!this.mIsPickupSwitchOn && !HwPCUtils.isInWindowsCastMode()) {
                HWExtDeviceEventListener hWExtDeviceEventListener = this.mHwedListener;
                if (hWExtDeviceEventListener != null) {
                    this.mHwedManager.unregisterDeviceListener(hWExtDeviceEventListener, this.mHwExtMotion);
                    this.mHwedListener = null;
                    SlogEx.i(TAG, "unregister device listener");
                }
            } else if (this.mHwedListener == null) {
                this.mHwedListener = new DeviceMotionListener();
                this.mHwedManager.registerDeviceListener(this.mHwedListener, this.mHwExtMotion, this.mPickUpHandler);
                SlogEx.i(TAG, "register device listener");
            }
        }
    }

    private class PickUpHandler extends Handler {
        PickUpHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            String str;
            int i = msg.what;
            boolean z = false;
            if (i == 1) {
                SlogEx.i(PickUpWakeScreenManager.TAG, "wait proximity sensor timeout(more than 500ms).");
                PickUpWakeScreenManager.this.setProximitySensorEnabled(false);
                PickUpWakeScreenManager pickUpWakeScreenManager = PickUpWakeScreenManager.this;
                pickUpWakeScreenManager.stopWakeUpReady(pickUpWakeScreenManager.mContext, true);
            } else if (i == PickUpWakeScreenManager.MSG_RECORD_WAKEUP) {
                SlogEx.i(PickUpWakeScreenManager.TAG, "Face Dectect wakeUpInternal type:" + msg.obj);
                ContentResolver contentResolver = PickUpWakeScreenManager.this.mContext.getContentResolver();
                if (msg.obj == null) {
                    str = "unknow";
                } else {
                    str = msg.obj.toString();
                }
                Settings.Global.putString(contentResolver, PickUpWakeScreenManager.WAKEUP_REASON, str);
            } else if (i == PickUpWakeScreenManager.MSG_UPDATE_DEVICE_LISTENER) {
                PickUpWakeScreenManager.this.updateDeviceListener();
            } else if (i == PickUpWakeScreenManager.MSG_SET_TURNOFF_CONTROLLER) {
                PickUpWakeScreenManager pickUpWakeScreenManager2 = PickUpWakeScreenManager.this;
                if (msg.arg1 == 1) {
                    z = true;
                }
                pickUpWakeScreenManager2.handleTurnOffController(z);
            }
        }
    }

    /* access modifiers changed from: private */
    public class DeviceMotionListener implements HWExtDeviceEventListener {
        private DeviceMotionListener() {
        }

        public void onDeviceDataChanged(HWExtDeviceEvent hwextDeviceEvent) {
            SlogEx.i(PickUpWakeScreenManager.TAG, "onDeviceDataChanged");
            if (PickUpWakeScreenManager.isSupportPickUp()) {
                if (hwextDeviceEvent.getDeviceValues() == null) {
                    SlogEx.e(PickUpWakeScreenManager.TAG, "onDeviceDataChanged deviceValues is null");
                } else if (!PickUpWakeScreenManager.this.mIsPickupSwitchOn && !HwPCUtils.isInWindowsCastMode()) {
                    SlogEx.w(PickUpWakeScreenManager.TAG, "mIsPickupSwitchOn is off");
                } else if (!PickUpWakeScreenManager.IS_TABLET || !PickUpWakeScreenManager.IS_SIMPLE_COVER || PickUpWakeScreenManager.this.mProximitySensor != null || PickUpWakeScreenManager.this.mCoverManager == null || PickUpWakeScreenManager.this.mCoverManager.isCoverOpen()) {
                    PickUpWakeScreenManager pickUpWakeScreenManager = PickUpWakeScreenManager.this;
                    pickUpWakeScreenManager.wakeupScreenOnIfNeeded(pickUpWakeScreenManager.mContext);
                } else {
                    SlogEx.i(PickUpWakeScreenManager.TAG, "has no proxmimity and cover is closed");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class ScreenTouchEventListener extends PointerEventListenerEx {
        private ScreenTouchEventListener() {
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            if (PickUpWakeScreenManager.this.mIsTurnOffTimeOutEnabled && motionEvent.getAction() == 0) {
                PickUpWakeScreenManager.this.setTurnOffController(false);
            }
        }
    }
}
