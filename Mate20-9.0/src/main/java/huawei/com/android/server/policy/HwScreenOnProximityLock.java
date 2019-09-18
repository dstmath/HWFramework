package huawei.com.android.server.policy;

import android.app.ActivityManager;
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
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_CANCEL;
import com.huawei.android.util.HwNotchSizeUtil;
import com.huawei.hwextdevice.HWExtDeviceEvent;
import com.huawei.hwextdevice.HWExtDeviceEventListener;
import com.huawei.hwextdevice.HWExtDeviceManager;
import com.huawei.hwextdevice.devices.HWExtMotion;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;

public class HwScreenOnProximityLock {
    private static final String APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.action.APS_RESOLUTION_CHANGE_ACTION";
    private static final String APS_RESOLUTION_CHANGE_PERSISSIONS = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
    private static final boolean DEBUG = false;
    public static final int FARAWAY_SENSOR = 2;
    public static final int FORCE_QUIT = 0;
    public static final int HEADDOWN_LEAVE = 4;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(SCREENON_TAG, 4)));
    private static final boolean IS_FOLDABLE = HwFoldScreenState.isFoldScreenDevice();
    public static final int LOCK_GOAWAY = 3;
    private static final int MOTION_HEADDOWN_ENTER = 1;
    private static final int MOTION_HEADDOWN_LEAVE = 2;
    private static final int MSG_REFRESH_HINT_VIEW = 2;
    private static final int MSG_SHOW_HINT_VIEW = 1;
    private static final boolean PROXIMITY_ENABLE = SystemProperties.getBoolean("ro.product.proximityenable", true);
    private static final String SCREENON_TAG = "ScreenOn";
    public static final int SCREEN_OFF = 1;
    private static final int SENSOR_FEATURE = 2049;
    private static final String TAG = "HwScreenOnProximityLock";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final String mNotchProp = SystemProperties.get("ro.config.hw_notch_size", "");
    private static final String sProximityWndName = "Emui:ProximityWnd";
    private AccSensorListener mAccListener = null;
    private BroadcastReceiver mApsResolutionChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(HwScreenOnProximityLock.TAG, "on receive apsResolutionChangeReceiver");
            HwScreenOnProximityLock.this.preparePoriximityView();
        }
    };
    private Context mContext;
    /* access modifiers changed from: private */
    public CoverManager mCoverManager;
    private boolean mDeviceHeld = false;
    private final Object mDeviceLock = new Object();
    private boolean mFirstBoot = true;
    public ContentObserver mFontScaleObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.i(HwScreenOnProximityLock.TAG, "font Scale changed");
            HwScreenOnProximityLock.this.preparePoriximityView();
        }
    };
    private HWExtDeviceEventListener mHWEDListener = new HWExtDeviceEventListener() {
        public void onDeviceDataChanged(HWExtDeviceEvent hwextDeviceEvent) {
            Log.d(HwScreenOnProximityLock.TAG, "onDeviceDataChanged");
            float[] deviceValues = hwextDeviceEvent.getDeviceValues();
            if (deviceValues == null) {
                Log.e(HwScreenOnProximityLock.TAG, "onDeviceDataChanged  deviceValues is null ");
            } else if (deviceValues.length < 2) {
                Log.e(HwScreenOnProximityLock.TAG, "hwextDeviceEvent data error");
            } else {
                boolean useSensorFeature = true;
                int unused = HwScreenOnProximityLock.this.mHeadDownStatus = (int) deviceValues[1];
                Log.d(HwScreenOnProximityLock.TAG, "mHeadDownStatus:" + HwScreenOnProximityLock.this.mHeadDownStatus);
                if (HwScreenOnProximityLock.this.mHeadDownStatus == 1) {
                    if (!HwScreenOnProximityLock.this.mSupportSensorFeature || HwScreenOnProximityLock.this.mIsProximity1 || !HwScreenOnProximityLock.this.mPolicy.isKeyguardLocked()) {
                        useSensorFeature = false;
                    }
                    boolean unused2 = HwScreenOnProximityLock.this.mIsProximity = useSensorFeature ? HwScreenOnProximityLock.this.mIsProximity2 : HwScreenOnProximityLock.this.mIsProximity1;
                    if (HwScreenOnProximityLock.this.mIsProximity) {
                        HwScreenOnProximityLock.this.addProximityView();
                    }
                } else if (HwScreenOnProximityLock.this.mHeadDownStatus == 2) {
                    boolean unused3 = HwScreenOnProximityLock.this.mIsProximity = HwScreenOnProximityLock.this.mIsProximity1;
                    if (!HwScreenOnProximityLock.this.mIsProximity) {
                        HwScreenOnProximityLock.this.releaseLock(4);
                    }
                }
            }
        }
    };
    private HWExtDeviceManager mHWEDManager = null;
    private HWExtMotion mHWExtMotion = null;
    private Handler mHandler;
    private boolean mHasNotchInScreen = false;
    /* access modifiers changed from: private */
    public int mHeadDownStatus = 2;
    private boolean mHeld;
    private View mHintView;
    /* access modifiers changed from: private */
    public boolean mIsProximity;
    /* access modifiers changed from: private */
    public boolean mIsProximity1;
    /* access modifiers changed from: private */
    public boolean mIsProximity2;
    private int mLastRotation = 0;
    private ProximitySensorListener mListener;
    private BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(HwScreenOnProximityLock.TAG, "on receive localeChangeReceiver");
            HwScreenOnProximityLock.this.preparePoriximityView();
        }
    };
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private WindowManager.LayoutParams mParams = null;
    private HwPhoneWindowManager mPhoneWindowManager;
    /* access modifiers changed from: private */
    public WindowManagerPolicy mPolicy;
    /* access modifiers changed from: private */
    public float mProximityThreshold;
    private final boolean mProximityTop = "true".equals(SystemProperties.get("ro.config.proximity_top", "false"));
    /* access modifiers changed from: private */
    public FrameLayout mProximityView = null;
    private int[] mReleaseReasons = {0, 1, 3};
    private int mRotation = 0;
    /* access modifiers changed from: private */
    public SensorManager mSensorManager;
    /* access modifiers changed from: private */
    public boolean mSupportSensorFeature = false;
    private final boolean mTouchHeadDown = "true".equals(SystemProperties.get("ro.config.touch.head_down", "true"));
    /* access modifiers changed from: private */
    public boolean mUseSensorFeature = false;
    private BroadcastReceiver mUserSwitchReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(HwScreenOnProximityLock.TAG, "on receive userSwitchReceiver");
            HwScreenOnProximityLock.this.preparePoriximityView();
        }
    };
    private boolean mViewAttached = false;
    private WindowManager mWindowManager;

    private class AccSensorListener implements SensorEventListener {
        private boolean mFlat = false;
        private boolean mListening = false;

        public AccSensorListener() {
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event != null && event.values != null && event.values.length >= 3) {
                boolean z = false;
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];
                float sqrt = (float) Math.sqrt((double) ((float) (Math.pow((double) axisX, 2.0d) + Math.pow((double) axisY, 2.0d) + Math.pow((double) axisZ, 2.0d))));
                float pitch = (float) (-Math.asin((double) (axisY / sqrt)));
                float roll = (float) Math.asin((double) (axisX / sqrt));
                if (((double) pitch) >= -0.8726646491148832d && ((double) pitch) <= 0.2617993877991494d && ((double) Math.abs(roll)) <= 0.5235987755982988d && axisZ >= HwScreenOnProximityLock.TYPICAL_PROXIMITY_THRESHOLD) {
                    z = true;
                }
                this.mFlat = z;
                if (this.mFlat) {
                    HwScreenOnProximityLock.this.removeProximityView();
                } else if (HwScreenOnProximityLock.this.mIsProximity) {
                    HwScreenOnProximityLock.this.addProximityView();
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
            if (this.mListening) {
                Log.w(HwScreenOnProximityLock.TAG, "AccSensorListener already register");
                return;
            }
            Log.d(HwScreenOnProximityLock.TAG, "AccSensorListener register");
            this.mListening = HwScreenOnProximityLock.this.mSensorManager.registerListener(this, sensor, 3);
        }

        public void unregister() {
            if (!this.mListening) {
                Log.w(HwScreenOnProximityLock.TAG, "AccSensorListener not register yet");
                return;
            }
            Log.d(HwScreenOnProximityLock.TAG, "AccSensorListener unregister");
            HwScreenOnProximityLock.this.mSensorManager.unregisterListener(this);
            this.mListening = false;
        }

        public boolean isFlat() {
            Log.d(HwScreenOnProximityLock.TAG, "AccSensorListener mFlat " + this.mFlat);
            return this.mFlat;
        }
    }

    private class ProximitySensorListener implements SensorEventListener {
        public ProximitySensorListener() {
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            boolean z = false;
            float d = event.values[0];
            int d2 = (((int) event.values[2]) >> 8) & 1;
            boolean unused = HwScreenOnProximityLock.this.mIsProximity1 = d >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && d < HwScreenOnProximityLock.this.mProximityThreshold;
            boolean unused2 = HwScreenOnProximityLock.this.mIsProximity2 = d2 == 1;
            HwScreenOnProximityLock hwScreenOnProximityLock = HwScreenOnProximityLock.this;
            if (HwScreenOnProximityLock.this.mSupportSensorFeature && !HwScreenOnProximityLock.this.mIsProximity1 && HwScreenOnProximityLock.this.mHeadDownStatus == 1 && HwScreenOnProximityLock.this.mPolicy.isKeyguardLocked()) {
                z = true;
            }
            boolean unused3 = hwScreenOnProximityLock.mUseSensorFeature = z;
            Log.d(HwScreenOnProximityLock.TAG, "mSupportSensorFeature:" + HwScreenOnProximityLock.this.mSupportSensorFeature + ",mHeadDownStatus:" + HwScreenOnProximityLock.this.mHeadDownStatus + ",mUseSensorFeature:" + HwScreenOnProximityLock.this.mUseSensorFeature);
            boolean unused4 = HwScreenOnProximityLock.this.mIsProximity = HwScreenOnProximityLock.this.mUseSensorFeature ? HwScreenOnProximityLock.this.mIsProximity2 : HwScreenOnProximityLock.this.mIsProximity1;
            handleSensorChanges();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:20:0x006b, code lost:
            r4.this$0.releaseLock(2);
         */
        private void handleSensorChanges() {
            if (HwScreenOnProximityLock.this.mCoverManager == null) {
                Log.e(HwScreenOnProximityLock.TAG, "mCoverManager is null");
                return;
            }
            boolean isCoverOpen = HwScreenOnProximityLock.this.mCoverManager.isCoverOpen();
            Log.i(HwScreenOnProximityLock.TAG, "handleSensorChanged: close to sensor: " + HwScreenOnProximityLock.this.mIsProximity + ", isCoverOpen: " + isCoverOpen);
            if (!HwScreenOnProximityLock.this.mIsProximity || !isCoverOpen) {
                synchronized (HwScreenOnProximityLock.this.mLock) {
                    if (isCoverOpen) {
                        try {
                            if (HwScreenOnProximityLock.this.mProximityView == null) {
                                Log.i(HwScreenOnProximityLock.TAG, "no need to releaseLock");
                            }
                        } catch (Throwable th) {
                            while (true) {
                                throw th;
                            }
                        }
                    }
                }
            } else {
                HwScreenOnProximityLock.this.addProximityView();
            }
        }
    }

    private void registerBroadcastReceiver() {
        this.mContext.registerReceiverAsUser(this.mApsResolutionChangeReceiver, UserHandle.ALL, new IntentFilter(APS_RESOLUTION_CHANGE_ACTION), APS_RESOLUTION_CHANGE_PERSISSIONS, null);
        this.mContext.registerReceiverAsUser(this.mLocaleChangeReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.LOCALE_CHANGED"), null, null);
        this.mContext.registerReceiverAsUser(this.mUserSwitchReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_SWITCHED"), null, null);
    }

    private void registerContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("font_scale"), true, this.mFontScaleObserver);
    }

    public HwScreenOnProximityLock(Context context, WindowManagerPolicy policy, WindowManagerPolicy.WindowManagerFuncs windowFuncs) {
        if (context == null) {
            Log.w(TAG, "HwScreenOnProximityLock context is null");
            return;
        }
        this.mContext = context;
        this.mPolicy = policy;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mCoverManager = new CoverManager();
        this.mPhoneWindowManager = (HwPhoneWindowManager) policy;
        this.mHWEDManager = HWExtDeviceManager.getInstance(this.mContext);
        this.mHWExtMotion = new HWExtMotion(DeviceStatusConstant.TYPE_HEAD_DOWN);
        this.mListener = new ProximitySensorListener();
        if (this.mProximityTop) {
            this.mAccListener = new AccSensorListener();
        }
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        HwScreenOnProximityLock.this.showHintView();
                        return;
                    case 2:
                        HwScreenOnProximityLock.this.refreshHintView();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mHasNotchInScreen = true ^ TextUtils.isEmpty(mNotchProp);
        init();
        this.mSupportSensorFeature = this.mSensorManager.supportSensorFeature(SENSOR_FEATURE);
        Settings.System.putInt(this.mContext.getContentResolver(), "support_sensor_feature", this.mSupportSensorFeature ? 1 : 0);
    }

    private void init() {
        registerBroadcastReceiver();
        registerContentObserver();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
        return;
     */
    public void registerDeviceListener() {
        if (!this.mTouchHeadDown) {
            Log.w(TAG, "mTouchHeadDown is false,no need to registerDeviceListener");
            return;
        }
        synchronized (this.mDeviceLock) {
            if (this.mDeviceHeld) {
                Log.d(TAG, "mDeviceHeld is true,register return");
            } else if (this.mHWEDManager == null) {
                Log.e(TAG, "mHWEDManager is null,register return");
            } else {
                this.mDeviceHeld = this.mHWEDManager.registerDeviceListener(this.mHWEDListener, this.mHWExtMotion, this.mHandler);
                if (this.mDeviceHeld) {
                    Log.d(TAG, "registerDeviceListener succeed");
                } else {
                    Log.d(TAG, "registerDeviceListener fail");
                }
            }
        }
    }

    public void unregisterDeviceListener() {
        synchronized (this.mDeviceLock) {
            if (!this.mDeviceHeld) {
                Log.d(TAG, "mDeviceHeld is false,unregister return");
            } else if (this.mHWEDManager == null) {
                Log.e(TAG, "mHWEDManager is null,unregister return");
            } else {
                this.mHWEDManager.unregisterDeviceListener(this.mHWEDListener, this.mHWExtMotion);
                Log.d(TAG, "unregisterDeviceListener succeed");
                this.mDeviceHeld = false;
                this.mHeadDownStatus = 2;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0065, code lost:
        return;
     */
    private void registerProximityListener() {
        synchronized (this.mLock) {
            if (this.mHeld) {
                Log.w(TAG, "mHeld is true,registerProximityListener return");
                return;
            }
            Sensor sensor = this.mSensorManager.getDefaultSensor(8);
            if (sensor == null) {
                Log.w(TAG, "registerProximityListener return because of proximity sensor is not existed");
                return;
            }
            float maxRange = sensor.getMaximumRange();
            if (maxRange >= TYPICAL_PROXIMITY_THRESHOLD) {
                this.mProximityThreshold = TYPICAL_PROXIMITY_THRESHOLD;
            } else if (maxRange < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                this.mProximityThreshold = TYPICAL_PROXIMITY_THRESHOLD;
            } else {
                this.mProximityThreshold = maxRange;
            }
            this.mHeld = this.mSensorManager.registerListener(this.mListener, sensor, 3);
            if (this.mHeld) {
                Log.d(TAG, "registerProximityListener success");
                if (this.mAccListener != null) {
                    this.mAccListener.register();
                }
            } else {
                Log.d(TAG, "registerProximityListener fail");
            }
        }
    }

    private boolean shouldReleaseProximity(int reason) {
        for (int i : this.mReleaseReasons) {
            if (reason == i) {
                return true;
            }
        }
        Log.i(TAG, "isKeyguardLocked:" + this.mPolicy.isKeyguardLocked());
        return !this.mPolicy.isKeyguardLocked();
    }

    private boolean isShouldRegisterProximity(int mode) {
        if (!IS_FOLDABLE) {
            return true;
        }
        if (mode == 0 || 3 == mode) {
            return false;
        }
        return true;
    }

    public void acquireLock(WindowManagerPolicy policy, int mode) {
        if (policy == null) {
            Log.w(TAG, "acquire Lock: return because get Window Manager policy is null");
            return;
        }
        if (isShouldRegisterProximity(mode)) {
            registerProximityListener();
        }
        if (!this.mPolicy.isKeyguardLocked()) {
            Log.d(TAG, "keyguard not locked");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005e, code lost:
        return;
     */
    public void releaseLock(int reason) {
        synchronized (this.mLock) {
            if (!this.mHeld) {
                Log.w(TAG, "releaseLock: return because sensor listener is held = " + this.mHeld);
                return;
            }
            Log.i(TAG, "releaseLock,reason:" + reason);
            removeProximityView();
            if (shouldReleaseProximity(reason)) {
                this.mHeld = false;
                this.mSensorManager.unregisterListener(this.mListener);
                Log.i(TAG, "unregister proximity sensor listener");
                if (this.mAccListener != null) {
                    this.mAccListener.unregister();
                }
            }
        }
    }

    public static String reasonString(int reason) {
        switch (reason) {
            case 0:
                return "force quit";
            case 1:
                return "screen off";
            case 2:
                return "faraway sensor";
            default:
                return "unkown reason";
        }
    }

    public boolean isShowing() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mProximityView != null && this.mViewAttached;
        }
        return z;
    }

    public void forceShowHint() {
        this.mHandler.sendEmptyMessage(1);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00eb, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0071  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00ec A[DONT_GENERATE] */
    public void preparePoriximityView() {
        View view;
        removeProximityView();
        synchronized (this.mLock) {
            Log.i(TAG, "inflate View, config : " + this.mContext.getResources().getConfiguration());
            if (this.mRotation != 0) {
                if (this.mRotation != 2) {
                    view = View.inflate(this.mContext, 34013363, null);
                    if (!(view instanceof FrameLayout)) {
                        this.mProximityView = (FrameLayout) view;
                        this.mHintView = this.mProximityView.findViewById(34603159);
                        if (this.mHasNotchInScreen) {
                            View notchView = this.mProximityView.findViewById(34603047);
                            if (notchView != null) {
                                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) notchView.getLayoutParams();
                                params.height = HwNotchSizeUtil.getNotchSize()[1];
                                notchView.setLayoutParams(params);
                            }
                        }
                        this.mProximityView.setOnTouchListener(new View.OnTouchListener() {
                            public boolean onTouch(View v, MotionEvent event) {
                                HwScreenOnProximityLock.this.showHintView();
                                return false;
                            }
                        });
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2100, 134223104, -2);
                        this.mParams = layoutParams;
                        this.mParams.inputFeatures |= 4;
                        this.mParams.privateFlags |= RET_REG_CANCEL.ID;
                        this.mParams.setTitle(sProximityWndName);
                        if (this.mHasNotchInScreen) {
                            this.mParams.layoutInDisplayCutoutMode = 1;
                        }
                        if (HWFLOW) {
                            Log.i(TAG, "preparePoriximityView addView ");
                        }
                    } else {
                        return;
                    }
                }
            }
            view = View.inflate(this.mContext, 34013254, null);
            DisplayMetrics dm = new DisplayMetrics();
            this.mWindowManager.getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;
            ((LinearLayout) view.findViewById(34603046)).setLayoutParams(new LinearLayout.LayoutParams(width, (width * 5) / 4));
            if (!(view instanceof FrameLayout)) {
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0040, code lost:
        return;
     */
    public void showHintView() {
        synchronized (this.mLock) {
            if (this.mProximityView != null) {
                if (this.mHintView != null) {
                    if (this.mHintView.getVisibility() != 0 && this.mViewAttached) {
                        this.mHintView.setVisibility(0);
                        long token = Binder.clearCallingIdentity();
                        try {
                            Settings.System.putIntForUser(this.mContext.getContentResolver(), "proximity_wnd_status", 1, ActivityManager.getCurrentUser());
                            Binder.restoreCallingIdentity(token);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(token);
                            throw th;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00d1, code lost:
        return;
     */
    public void addProximityView() {
        if (this.mRotation != this.mLastRotation) {
            preparePoriximityView();
            this.mLastRotation = this.mRotation;
        }
        synchronized (this.mLock) {
            if (this.mProximityView != null && !this.mPhoneWindowManager.isKeyguardShortcutApps()) {
                if (!this.mPhoneWindowManager.isLandscape() || !this.mPhoneWindowManager.isLsKeyguardShortcutApps()) {
                    boolean z = true;
                    if (this.mProximityView == null || this.mParams == null || this.mViewAttached || !this.mHeld || this.mCoverManager == null || !this.mCoverManager.isCoverOpen() || (this.mAccListener != null && this.mAccListener.isFlat())) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("no need to addView:mProximityView = ");
                        sb.append(this.mProximityView);
                        sb.append("mParams null,");
                        if (this.mParams != null) {
                            z = false;
                        }
                        sb.append(z);
                        sb.append(",mViewAttached:");
                        sb.append(this.mViewAttached);
                        sb.append(",mHeld:");
                        sb.append(this.mHeld);
                        Log.w(TAG, sb.toString());
                    } else {
                        if (HWFLOW) {
                            Log.i(TAG, "addProximityView ");
                        }
                        if (this.mFirstBoot) {
                            Log.i(TAG, "first boot,prepare again");
                            preparePoriximityView();
                            this.mFirstBoot = false;
                        }
                        this.mWindowManager.addView(this.mProximityView, this.mParams);
                        if (this.mHintView != null) {
                            if (PROXIMITY_ENABLE) {
                                this.mHintView.setVisibility(8);
                            } else {
                                this.mHintView.setVisibility(0);
                            }
                            this.mViewAttached = true;
                        } else {
                            return;
                        }
                    }
                }
            }
            Log.i(TAG, "no need to addProximityView");
        }
    }

    /* access modifiers changed from: private */
    public void removeProximityView() {
        long token;
        synchronized (this.mLock) {
            if (this.mProximityView == null || !this.mViewAttached) {
                Log.w(TAG, "no need to removeView:mProximityView = " + this.mProximityView + ",mViewAttached" + this.mViewAttached);
            } else {
                ViewParent vp = this.mProximityView.getParent();
                if (vp == null) {
                    try {
                        this.mWindowManager.removeView(this.mProximityView);
                        this.mViewAttached = false;
                        Log.i(TAG, "removeview directly ");
                    } catch (RuntimeException e) {
                        Log.i(TAG, "removeView fail " + e.getMessage());
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                } else if (this.mWindowManager == null || !(vp instanceof ViewRootImpl)) {
                    Log.w(TAG, "removeView fail: mWindowManager = " + this.mWindowManager + ", viewparent = " + vp);
                } else {
                    if (HWFLOW) {
                        Log.i(TAG, "removeProximityView success vp " + vp);
                    }
                    this.mWindowManager.removeView(this.mProximityView);
                    this.mViewAttached = false;
                }
                token = Binder.clearCallingIdentity();
                Settings.System.putIntForUser(this.mContext.getContentResolver(), "proximity_wnd_status", 0, ActivityManager.getCurrentUser());
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public void refreshForRotationChange(int rotation) {
        this.mLastRotation = this.mRotation;
        this.mRotation = rotation;
        this.mHandler.sendEmptyMessage(2);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001a, code lost:
        addProximityView();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001d, code lost:
        if (r0 == false) goto L_0x0022;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001f, code lost:
        showHintView();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0022, code lost:
        return;
     */
    public void refreshHintView() {
        synchronized (this.mLock) {
            boolean shouldShow = this.mHintView.getVisibility() == 0;
            if (this.mHeld) {
                if (!this.mIsProximity) {
                }
            }
        }
    }
}
