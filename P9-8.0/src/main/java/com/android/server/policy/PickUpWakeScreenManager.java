package com.android.server.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import com.android.server.am.HwActivityManagerService;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.huawei.hwextdevice.HWExtDeviceEvent;
import com.huawei.hwextdevice.HWExtDeviceEventListener;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.hwextdevice.devices.HWExtMotion;
import com.huawei.motiondetection.MRUtils;
import java.util.ArrayList;
import java.util.List;

public class PickUpWakeScreenManager {
    private static final int CAN_DISABLE_MOTION = 0;
    private static final int CAN_ENBALE_MOTION = 1;
    private static final boolean CHECK_PROXIMITY_SENSOR_ABSENT = true;
    private static final boolean DEBUG;
    private static final Uri HWMOTIONS_CONTENT_URI = Uri.parse("content://com.huawei.providers.motions/hwmotions");
    public static final String MOTION_PICKUP_WAKEUP_DEVICE = "motion_pickup_wakeup_device";
    private static final int MSG_RECORD_WAKEUP = 2;
    private static final int MSG_WAIT_PROXIMITY_SENSOR_TIMEOUT = 1;
    private static int SCREEN_OFF_TIME_WITHOUT_TOUCH = 5000;
    private static final String TAG = "PickUpWakeScreenManager";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final long WAIT_PROXIMITY_SENSOR_TIMEOUT = 500;
    private static final String WAKEUP_REASON = "WakeUpReason";
    private static Object mLock = new Object();
    private static PickUpWakeScreenManager mPickUpWakeScreenManager;
    private static final int[] mSensorsPickup = new int[]{1};
    private static final boolean sSupportFaceRecognition = SystemProperties.getBoolean("ro.config.face_recognition", false);
    private Context mContext;
    private boolean mFirstBoot = true;
    private HWExtDeviceEventListener mHWEDListener = new HWExtDeviceEventListener() {
        public void onDeviceDataChanged(HWExtDeviceEvent hwextDeviceEvent) {
            Slog.d(PickUpWakeScreenManager.TAG, "onDeviceDataChanged");
            if (!PickUpWakeScreenManager.this.mIsPickupSupport) {
                Slog.d(PickUpWakeScreenManager.TAG, "mIsPickupSwitchOn is disable");
            } else if (hwextDeviceEvent.getDeviceValues() == null) {
                Slog.d(PickUpWakeScreenManager.TAG, "onDeviceDataChanged  deviceValues is null ");
            } else if (PickUpWakeScreenManager.this.mIsPickupSwitchOn) {
                PickUpWakeScreenManager.this.wakeupScreenOnIfNeeded(PickUpWakeScreenManager.this.mContext);
            } else {
                Slog.d(PickUpWakeScreenManager.TAG, "mIsPickupSwitchOn is off");
            }
        }
    };
    private HWExtDeviceManager mHWEDManager = null;
    private HWExtMotion mHWExtMotion = null;
    private Handler mHandler;
    private boolean mInFastScreenOn = false;
    private boolean mIsPickupSupport = false;
    private boolean mIsPickupSwitchOn = false;
    private int mMotionRegistState = 1;
    private boolean mNeedControlTurnOff = false;
    private ContentObserver mPickupEnabledObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Slog.d(PickUpWakeScreenManager.TAG, "PickupEnabledObserver");
            PickUpWakeScreenManager.this.observePickupSwitchState();
        }
    };
    public final Runnable mPickupMotionTimeoutRunnable = new Runnable() {
        public void run() {
            PowerManager pm = (PowerManager) PickUpWakeScreenManager.this.mContext.getSystemService("power");
            if (pm != null && pm.isScreenOn()) {
                pm.goToSleep(SystemClock.uptimeMillis());
                Slog.d(PickUpWakeScreenManager.TAG, "ScreenOff because pickup motion");
            }
        }
    };
    private boolean mPointerListerInitState = false;
    private Handler mProximityHandler;
    private Sensor mProximitySensor;
    private boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (PickUpWakeScreenManager.this.mProximitySensorEnabled) {
                float distance = event.values[0];
                boolean positive = distance >= 0.0f && distance < PickUpWakeScreenManager.this.mProximityThreshold;
                if (PickUpWakeScreenManager.DEBUG) {
                    Slog.d(PickUpWakeScreenManager.TAG, "onSensorChanged distance=" + distance + ",positive=" + positive + ",sensorEnabled=" + PickUpWakeScreenManager.this.mProximitySensorEnabled);
                }
                PickUpWakeScreenManager.this.handleProximitySensorEvent(positive);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private float mProximityThreshold;
    private ContentResolver mResolver;
    private ScreenTouchEventListener mScreenTouchEventListener = null;
    private final SensorManager mSensorManager;
    private BroadcastReceiver mUserSwitchReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Slog.d(PickUpWakeScreenManager.TAG, "on receive userSwitchReceiver");
            PickUpWakeScreenManager.this.observePickupSwitchState();
        }
    };
    private WindowManagerFuncs mWindowManagerFuncs;
    private boolean trunOffTimeOutEnable = false;

    private class ProximityHandler extends Handler {
        public ProximityHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Slog.i(PickUpWakeScreenManager.TAG, "wait proximity sensor timeout(more than 500ms).");
                    PickUpWakeScreenManager.this.setProximitySensorEnabled(false);
                    PickUpWakeScreenManager.this.stopWakeUpReady(PickUpWakeScreenManager.this.mContext, true);
                    return;
                case 2:
                    Slog.d(PickUpWakeScreenManager.TAG, "Face Dectect wakeUpInternal type:" + msg.obj);
                    Global.putString(PickUpWakeScreenManager.this.mContext.getContentResolver(), PickUpWakeScreenManager.WAKEUP_REASON, msg.obj == null ? "unknow" : msg.obj.toString());
                    return;
                default:
                    return;
            }
        }
    }

    private class ScreenTouchEventListener implements PointerEventListener {
        /* synthetic */ ScreenTouchEventListener(PickUpWakeScreenManager this$0, ScreenTouchEventListener -this1) {
            this();
        }

        private ScreenTouchEventListener() {
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            if (PickUpWakeScreenManager.this.mPickupMotionTimeoutRunnable != null && PickUpWakeScreenManager.this.trunOffTimeOutEnable) {
                PickUpWakeScreenManager.this.mHandler.removeCallbacks(PickUpWakeScreenManager.this.mPickupMotionTimeoutRunnable);
                PickUpWakeScreenManager.this.trunOffTimeOutEnable = false;
                Slog.d(PickUpWakeScreenManager.TAG, "ScreenTouchEventListener remove");
            }
        }
    }

    static {
        boolean z;
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 4);
        } else {
            z = false;
        }
        DEBUG = z;
    }

    public static PickUpWakeScreenManager getInstance(Context context, Handler handler, WindowManagerFuncs windowManagerFuncs, KeyguardServiceDelegate KeyguardDelegate) {
        PickUpWakeScreenManager pickUpWakeScreenManager;
        synchronized (mLock) {
            if (mPickUpWakeScreenManager == null) {
                mPickUpWakeScreenManager = new PickUpWakeScreenManager(context, handler, windowManagerFuncs, KeyguardDelegate);
            }
            pickUpWakeScreenManager = mPickUpWakeScreenManager;
        }
        return pickUpWakeScreenManager;
    }

    public static PickUpWakeScreenManager getInstance() {
        PickUpWakeScreenManager pickUpWakeScreenManager;
        synchronized (mLock) {
            pickUpWakeScreenManager = mPickUpWakeScreenManager;
        }
        return pickUpWakeScreenManager;
    }

    public void stopTrunOffScrren() {
        if (this.mPickupMotionTimeoutRunnable != null && this.trunOffTimeOutEnable) {
            this.mHandler.removeCallbacks(this.mPickupMotionTimeoutRunnable);
            this.trunOffTimeOutEnable = false;
            Slog.d(TAG, "stopTrunOffScrren remove mPickupMotionTimeout");
        }
    }

    public PickUpWakeScreenManager(Context context, Handler handler, WindowManagerFuncs windowManagerFuncs, KeyguardServiceDelegate KeyguardDelegate) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mResolver = this.mContext.getContentResolver();
        this.mProximityHandler = new ProximityHandler(handler.getLooper());
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mProximitySensor = this.mSensorManager.getDefaultSensor(8);
        if (this.mProximitySensor != null) {
            this.mProximityThreshold = this.mProximitySensor.getMaximumRange() < TYPICAL_PROXIMITY_THRESHOLD ? this.mProximitySensor.getMaximumRange() : TYPICAL_PROXIMITY_THRESHOLD;
        }
        this.mContext.registerReceiverAsUser(this.mUserSwitchReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_SWITCHED"), null, null);
    }

    public void pickUpWakeScreenInit() {
        this.mIsPickupSupport = getPickupWakeEnabled();
        Slog.d(TAG, "mIsPickupSupport = " + this.mIsPickupSupport);
        this.mNeedControlTurnOff = getSupportFaceDectect() ^ 1;
        Slog.d(TAG, "mNeedControlTurnOff = " + this.mNeedControlTurnOff);
        MRUtils.observerMotionEnableStateChange(this.mContext, this.mPickupEnabledObserver, -1);
        if (this.mIsPickupSupport) {
            this.mHWEDManager = HWExtDeviceManager.getInstance(this.mContext);
            this.mHWExtMotion = new HWExtMotion(100);
            this.mScreenTouchEventListener = new ScreenTouchEventListener(this, null);
        }
    }

    public static boolean isPickupSensorSupport(Context context) {
        int i;
        boolean isSensorSupport = true;
        SensorManager mSensorManager = (SensorManager) context.getSystemService("sensor");
        ArrayList<Integer> mSensorTypeList = new ArrayList();
        List<Sensor> list = mSensorManager.getSensorList(-1);
        for (i = 0; i < list.size(); i++) {
            mSensorTypeList.add(Integer.valueOf(((Sensor) list.get(i)).getType()));
        }
        for (int valueOf : mSensorsPickup) {
            if (!mSensorTypeList.contains(Integer.valueOf(valueOf))) {
                isSensorSupport = false;
                break;
            }
        }
        Slog.d(TAG, "isPickupSensorSupport isSensorSupport = " + isSensorSupport);
        return isSensorSupport;
    }

    public void enablePickupMotionOrNot(boolean isLocked) {
        boolean z = true;
        if (this.mIsPickupSupport) {
            if (this.mFirstBoot) {
                int motionPickUp = MRUtils.getMotionEnableStateAsUser(this.mContext, MOTION_PICKUP_WAKEUP_DEVICE, ActivityManager.getCurrentUser());
                if (motionPickUp != -1) {
                    if (motionPickUp != 1) {
                        z = false;
                    }
                    this.mIsPickupSwitchOn = z;
                    this.mFirstBoot = false;
                }
            }
            Slog.d(TAG, "mIsPickupSwitchOn = " + this.mIsPickupSwitchOn);
            if (Secure.getIntForUser(this.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) == 0) {
                Slog.d(TAG, "Device is in Provision");
                return;
            } else if (this.mIsPickupSwitchOn) {
                Slog.d(TAG, "isLocked = " + isLocked);
                if (isLocked) {
                    enablePickupMotion(this.mHandler);
                } else {
                    disablePickupMotion();
                }
                return;
            } else {
                return;
            }
        }
        Slog.d(TAG, "mIsPickupSwitchOn is disable");
    }

    private void enablePickupMotion(Handler handler) {
        if (this.mMotionRegistState == 0) {
            Slog.d(TAG, "can not regist twice");
        } else if (this.mHWEDManager != null) {
            this.mHWEDManager.registerDeviceListener(this.mHWEDListener, this.mHWExtMotion, handler);
            Slog.d(TAG, "regist listener");
            if (this.mScreenTouchEventListener != null) {
                if (this.mPointerListerInitState) {
                    this.mWindowManagerFuncs.unregisterPointerEventListener(this.mScreenTouchEventListener);
                } else {
                    this.mPointerListerInitState = true;
                }
                this.mScreenTouchEventListener = null;
                Slog.d(TAG, "unregisterPointerEventListener");
                this.mMotionRegistState = 0;
            }
        } else {
            Slog.d(TAG, "mHWEDManager is null, return");
        }
    }

    private void disablePickupMotion() {
        if (this.mMotionRegistState == 1) {
            Slog.d(TAG, "can not unregist without regist");
        } else if (this.mHWEDManager != null) {
            if (this.mScreenTouchEventListener == null) {
                this.mScreenTouchEventListener = new ScreenTouchEventListener(this, null);
            }
            this.mWindowManagerFuncs.registerPointerEventListener(this.mScreenTouchEventListener);
            Slog.d(TAG, "registerPointerEventListener");
            this.mMotionRegistState = 1;
        } else {
            Slog.d(TAG, " mHWEDManager is null,return ");
        }
    }

    private void wakeupScreenOnIfNeeded(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService("power");
        if (pm == null) {
            Slog.d(TAG, "pm is null");
        } else if (pm.isScreenOn()) {
            Slog.d(TAG, "screen on =" + pm.isScreenOn());
        } else {
            wakeUpWaitForProximity();
            if (this.mNeedControlTurnOff) {
                this.mHandler.removeCallbacks(this.mPickupMotionTimeoutRunnable);
                this.mHandler.postDelayed(this.mPickupMotionTimeoutRunnable, (long) SCREEN_OFF_TIME_WITHOUT_TOUCH);
                this.trunOffTimeOutEnable = true;
            }
        }
    }

    private void setProximitySensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mProximitySensorEnabled) {
                this.mProximitySensorEnabled = true;
                this.mSensorManager.registerListener(this.mProximitySensorListener, this.mProximitySensor, 3, this.mProximityHandler);
                this.mProximityHandler.sendEmptyMessageDelayed(1, 500);
                if (DEBUG) {
                    Slog.d(TAG, "proximity sensor registered.");
                }
            }
        } else if (this.mProximitySensorEnabled) {
            this.mProximitySensorEnabled = false;
            this.mProximityHandler.removeMessages(1);
            this.mSensorManager.unregisterListener(this.mProximitySensorListener);
            if (DEBUG) {
                Slog.d(TAG, "proximity sensor unregistered.");
            }
        }
    }

    private void handleProximitySensorEvent(boolean positive) {
        if (this.mProximitySensorEnabled) {
            if (positive) {
                stopWakeUpReady(this.mContext, false);
            } else {
                stopWakeUpReady(this.mContext, true);
            }
            setProximitySensorEnabled(false);
        }
    }

    private void wakeUpWaitForProximity() {
        if (this.mInFastScreenOn) {
            Slog.d(TAG, "wakeUpWaitForProximity already in processing");
            return;
        }
        startWakeUpReady(this.mContext);
        setProximitySensorEnabled(true);
    }

    private void startWakeUpReady(Context context) {
        PowerManager powerManager = null;
        if (context != null) {
            powerManager = (PowerManager) context.getSystemService("power");
        }
        if (powerManager == null) {
            Slog.w(TAG, "startWakeUpReady powermanager is null");
            return;
        }
        Slog.w(TAG, "wakeUpWaitForProximity start");
        try {
            powerManager.getClass().getMethod("startWakeUpReady", new Class[]{Long.TYPE}).invoke(powerManager, new Object[]{Long.valueOf(SystemClock.uptimeMillis())});
            this.mInFastScreenOn = true;
        } catch (NoSuchMethodException e) {
            Slog.e(TAG, "PowerManager Value: System hasn't startWakeUpReady method " + e);
        } catch (IllegalArgumentException e2) {
            Slog.e(TAG, "PowerManager Value: startWakeUpReady method has wrong parameter " + e2);
        } catch (Exception e3) {
            Slog.e(TAG, "PowerManager Value: other reflect exception " + e3);
        }
    }

    private void stopWakeUpReady(Context context, boolean turnOn) {
        if (this.mInFastScreenOn) {
            PowerManager powerManager = null;
            if (context != null) {
                powerManager = (PowerManager) context.getSystemService("power");
            }
            if (powerManager == null) {
                Slog.w(TAG, "stopWakeUpReady powermanager is null");
                this.mInFastScreenOn = false;
                return;
            }
            Slog.w(TAG, "stopWakeUpReady turnon=" + turnOn);
            try {
                powerManager.getClass().getMethod("stopWakeUpReady", new Class[]{Long.TYPE, Boolean.TYPE}).invoke(powerManager, new Object[]{Long.valueOf(SystemClock.uptimeMillis()), Boolean.valueOf(turnOn)});
            } catch (NoSuchMethodException e) {
                Slog.e(TAG, "PowerManager Value: System hasn't stopWakeUpReady method " + e);
            } catch (IllegalArgumentException e2) {
                Slog.e(TAG, "PowerManager Value: stopWakeUpReady method has wrong parameter " + e2);
            } catch (Exception e3) {
                Slog.e(TAG, "PowerManager Value: other reflect exception " + e3);
            }
            this.mInFastScreenOn = false;
            if (turnOn) {
                if (sSupportFaceRecognition) {
                    this.mProximityHandler.obtainMessage(2, 0, 0, "android.policy:HWPICKUP#" + Binder.getCallingUid()).sendToTarget();
                }
                Slog.d(TAG, "report no proximity ScreenOn waked by pickup motion. EventId:500 ret:" + Flog.bdReport(this.mContext, HwActivityManagerService.SERVICE_ADJ));
            }
            return;
        }
        Slog.w(TAG, "stopWakeUpReady, not in fast screen on");
    }

    private boolean getPickupWakeEnabled() {
        return SystemProperties.getBoolean("ro.config.hw_wakeup_device", false);
    }

    private boolean getSupportFaceDectect() {
        if (1 == SystemProperties.getInt("ro.config.face_detect", 0) || SystemProperties.getBoolean("ro.config.face_recognition", false)) {
            return true;
        }
        return SystemProperties.getBoolean("ro.config.face_smart_keepon", false);
    }

    private boolean getPickupSwitchOn() {
        return MRUtils.getMotionEnableStateAsUser(this.mContext, MOTION_PICKUP_WAKEUP_DEVICE, ActivityManager.getCurrentUser()) == 1;
    }

    private void observePickupSwitchState() {
        this.mIsPickupSwitchOn = getPickupSwitchOn();
        Slog.d(TAG, "observePickupSwitchState mIsPickupSwitchOn:" + this.mIsPickupSwitchOn);
        if (this.mIsPickupSupport && (this.mIsPickupSwitchOn ^ 1) != 0 && this.mHWEDManager != null) {
            this.mHWEDManager.unregisterDeviceListener(this.mHWEDListener, this.mHWExtMotion);
            Slog.d(TAG, "unregist listener");
        }
    }
}
