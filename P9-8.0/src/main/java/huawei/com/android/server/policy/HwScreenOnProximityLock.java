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
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.widget.FrameLayout;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_CANCEL;

public class HwScreenOnProximityLock {
    private static final String APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.action.APS_RESOLUTION_CHANGE_ACTION";
    private static final String APS_RESOLUTION_CHANGE_PERSISSIONS = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
    private static final boolean DEBUG = false;
    private static final long DELAY = 1000;
    public static final int DISABLE_MODE = 2;
    public static final int FARAWAY_SENSOR = 3;
    public static final int FORCE_QUIT = 0;
    private static final boolean HWFLOW;
    private static final String KEY_TOUCH_DISABLE_MODE = "touch_disable_mode";
    private static final int MSG_FIRST_PROXIMITY_IN_TIME = 1;
    private static final int MSG_SHOW_HINT_VIEW = 2;
    private static final String SCREENON_TAG = "ScreenOn";
    public static final int SCREEN_OFF = 1;
    public static final int SENSOR_TIMEOUT = 4;
    private static final String TAG = "HwScreenOnProximityLock";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final String sProximityWndName = "Emui:ProximityWnd";
    private BroadcastReceiver mApsResolutionChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(HwScreenOnProximityLock.TAG, "on receive apsResolutionChangeReceiver");
            HwScreenOnProximityLock.this.preparePoriximityView();
        }
    };
    private Context mContext;
    private CoverManager mCoverManager;
    private boolean mFontScaleFlag = false;
    public ContentObserver mFontScaleObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.i(HwScreenOnProximityLock.TAG, "font Scale changed");
            HwScreenOnProximityLock.this.preparePoriximityView();
        }
    };
    private Handler mHandler;
    private boolean mHeld;
    private ProximitySensorListener mListener;
    private BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(HwScreenOnProximityLock.TAG, "on receive localeChangeReceiver");
            HwScreenOnProximityLock.this.preparePoriximityView();
        }
    };
    private final Object mLock = new Object();
    LayoutParams mParams = null;
    HwPhoneWindowManager mPhoneWindowManager;
    private WindowManagerPolicy mPolicy;
    private float mProximityThreshold;
    private FrameLayout mProximityView = null;
    private SensorManager mSensorManager;
    public ContentObserver mTouchDisableObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (!(System.getIntForUser(HwScreenOnProximityLock.this.mContext.getContentResolver(), HwScreenOnProximityLock.KEY_TOUCH_DISABLE_MODE, 1, ActivityManager.getCurrentUser()) != 0) && HwScreenOnProximityLock.this.isShowing()) {
                HwScreenOnProximityLock.this.releaseLock(2);
            }
        }
    };
    private BroadcastReceiver mUserPresentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (HwScreenOnProximityLock.this.mHeld) {
                Log.i(HwScreenOnProximityLock.TAG, "on receive userPresentReceiver");
                HwScreenOnProximityLock.this.removeProximityView();
                HwScreenOnProximityLock.this.mSensorManager.unregisterListener(HwScreenOnProximityLock.this.mListener);
                HwScreenOnProximityLock.this.mContext.getContentResolver().unregisterContentObserver(HwScreenOnProximityLock.this.mTouchDisableObserver);
                HwScreenOnProximityLock.this.mHeld = false;
            }
        }
    };
    private boolean mViewAttached = false;
    private WindowManager mWindowManager;

    private class ProximitySensorListener implements SensorEventListener {
        private boolean mIsProximity;

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            boolean z = false;
            float d = event.values[0];
            if (d >= 0.0f && d < HwScreenOnProximityLock.this.mProximityThreshold) {
                z = true;
            }
            this.mIsProximity = z;
            handleSensorChanges();
        }

        /* JADX WARNING: Missing block: B:41:0x00be, code:
            r5.this$0.releaseLock(3);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void handleSensorChanges() {
            if (HwScreenOnProximityLock.this.mCoverManager == null) {
                Log.e(HwScreenOnProximityLock.TAG, "mCoverManager is null");
                return;
            }
            boolean isCoverOpen = HwScreenOnProximityLock.this.mCoverManager.isCoverOpen();
            Log.i(HwScreenOnProximityLock.TAG, "handleSensorChanged: close to sensor: " + this.mIsProximity + ", isCoverOpen: " + isCoverOpen);
            if (this.mIsProximity && isCoverOpen) {
                if (HwScreenOnProximityLock.this.mHandler.hasMessages(1)) {
                    HwScreenOnProximityLock.this.mHandler.removeMessages(1);
                }
                synchronized (HwScreenOnProximityLock.this.mLock) {
                    if (HwScreenOnProximityLock.this.mProximityView == null || HwScreenOnProximityLock.this.mPhoneWindowManager.isKeyguardShortcutApps() || (HwScreenOnProximityLock.this.mPhoneWindowManager.isLandscape() && HwScreenOnProximityLock.this.mPhoneWindowManager.isLsKeyguardShortcutApps())) {
                        Log.i(HwScreenOnProximityLock.TAG, "no need to addProximityView");
                        return;
                    }
                    HwScreenOnProximityLock.this.addProximityView();
                }
            } else {
                synchronized (HwScreenOnProximityLock.this.mLock) {
                    if (isCoverOpen) {
                        if (HwScreenOnProximityLock.this.mProximityView == null) {
                            Log.i(HwScreenOnProximityLock.TAG, "no need to releaseLock");
                        }
                    }
                }
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(SCREENON_TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    private void registerBroadcastReceiver() {
        this.mContext.registerReceiverAsUser(this.mUserPresentReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_PRESENT"), null, null);
        this.mContext.registerReceiverAsUser(this.mApsResolutionChangeReceiver, UserHandle.ALL, new IntentFilter(APS_RESOLUTION_CHANGE_ACTION), APS_RESOLUTION_CHANGE_PERSISSIONS, null);
        this.mContext.registerReceiverAsUser(this.mLocaleChangeReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.LOCALE_CHANGED"), null, null);
    }

    private void registerContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("font_scale"), true, this.mFontScaleObserver);
    }

    public HwScreenOnProximityLock(Context context, WindowManagerPolicy policy, WindowManagerFuncs windowFuncs) {
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
        this.mListener = new ProximitySensorListener();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        HwScreenOnProximityLock.this.releaseLock(4);
                        return;
                    case 2:
                        HwScreenOnProximityLock.this.showHintView();
                        return;
                    default:
                        return;
                }
            }
        };
        init();
        preparePoriximityView();
    }

    private void init() {
        registerBroadcastReceiver();
        registerContentObserver();
    }

    /* JADX WARNING: Missing block: B:43:0x00df, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void acquireLock(WindowManagerPolicy policy) {
        if (!(System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1, ActivityManager.getCurrentUser()) > 0)) {
            Log.d(TAG, "failed into acquireLock  beacuse the button status is closed");
        } else if (System.getIntForUser(this.mContext.getContentResolver(), "device_provisioned", 1, ActivityManager.getCurrentUser()) == 0) {
            Log.i(TAG, "in device provision process");
        } else {
            synchronized (this.mLock) {
                if (this.mHeld) {
                    Log.w(TAG, "acquire Lock: return because sensor listener has been held  = " + this.mHeld);
                } else if (policy == null) {
                    Log.w(TAG, "acquire Lock: return because get Window Manager policy is null");
                } else {
                    this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_TOUCH_DISABLE_MODE), true, this.mTouchDisableObserver);
                    Sensor sensor = this.mSensorManager.getDefaultSensor(8);
                    if (sensor == null) {
                        Log.w(TAG, "acquire Lock: return because of proximity sensor is not existed");
                        return;
                    }
                    float maxRange = sensor.getMaximumRange();
                    if (maxRange >= TYPICAL_PROXIMITY_THRESHOLD) {
                        this.mProximityThreshold = TYPICAL_PROXIMITY_THRESHOLD;
                    } else if (maxRange < 0.0f) {
                        this.mProximityThreshold = TYPICAL_PROXIMITY_THRESHOLD;
                    } else {
                        this.mProximityThreshold = maxRange;
                    }
                    this.mHeld = this.mSensorManager.registerListener(this.mListener, sensor, 3);
                    if (this.mHeld) {
                        if (!this.mPolicy.isKeyguardLocked()) {
                            Log.d(TAG, "keyguard not locked");
                        }
                        Log.i(TAG, "acquireLock begin proximity sensor event listening");
                        this.mHandler.sendEmptyMessageDelayed(1, 1000);
                    } else {
                        Log.w(TAG, "registerListener fail");
                    }
                }
            }
        }
    }

    public void releaseLock(int reason) {
        synchronized (this.mLock) {
            if (this.mHeld) {
                Log.i(TAG, "releaseLock,reason:" + reason);
                removeProximityView();
                if (!this.mPolicy.isKeyguardLocked() || reason == 1 || reason == 0 || reason == 2 || reason == 4) {
                    this.mHeld = false;
                    this.mSensorManager.unregisterListener(this.mListener);
                    this.mContext.getContentResolver().unregisterContentObserver(this.mTouchDisableObserver);
                    Log.i(TAG, "unregister proximity sensor listener");
                }
                this.mHandler.removeCallbacksAndMessages(null);
                return;
            }
            Log.w(TAG, "releaseLock: return because sensor listener is held = " + this.mHeld);
        }
    }

    public static String reasonString(int reason) {
        switch (reason) {
            case 0:
                return "force quit";
            case 1:
                return "screen off";
            case 2:
                return "disable mode";
            case 3:
                return "faraway sensor";
            case 4:
                return "sensor event timeout";
            default:
                return "unkown reason";
        }
    }

    public boolean isShowing() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mProximityView != null ? this.mViewAttached : false;
        }
        return z;
    }

    public void forceShowHint() {
        this.mHandler.sendEmptyMessage(2);
    }

    /* JADX WARNING: Missing block: B:9:0x0059, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void preparePoriximityView() {
        removeProximityView();
        synchronized (this.mLock) {
            View view = View.inflate(this.mContext, 34013254, null);
            if (view instanceof FrameLayout) {
                this.mProximityView = (FrameLayout) view;
                this.mProximityView.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        HwScreenOnProximityLock.this.showHintView();
                        return false;
                    }
                });
                this.mParams = new LayoutParams(-1, -1, 2100, 134223104, -2);
                LayoutParams layoutParams = this.mParams;
                layoutParams.inputFeatures |= 4;
                layoutParams = this.mParams;
                layoutParams.privateFlags |= RET_REG_CANCEL.ID;
                this.mParams.setTitle(sProximityWndName);
                if (HWFLOW) {
                    Log.i(TAG, "preparePoriximityView addView ");
                }
            }
        }
    }

    private void showHintView() {
        synchronized (this.mLock) {
            if (this.mProximityView == null) {
                return;
            }
            View hintView = this.mProximityView.findViewById(34603159);
            if (hintView == null) {
                return;
            }
            hintView.setVisibility(0);
        }
    }

    private void addProximityView() {
        if (this.mProximityView == null || this.mParams == null || (this.mViewAttached ^ 1) == 0 || !this.mHeld) {
            Log.w(TAG, "no need to addView:mProximityView = " + this.mProximityView + "mParams=" + this.mParams + ",mViewAttached:" + this.mViewAttached + ",mHeld:" + this.mHeld);
        } else {
            if (HWFLOW) {
                Log.i(TAG, "addProximityView ");
            }
            if (!(this.mFontScaleFlag || System.getFloatForUser(this.mContext.getContentResolver(), "font_scale", 1.0f, ActivityManager.getCurrentUser()) == 1.0f)) {
                Log.i(TAG, "font changed ");
                preparePoriximityView();
                this.mFontScaleFlag = true;
            }
            this.mWindowManager.addView(this.mProximityView, this.mParams);
            View hintView = this.mProximityView.findViewById(34603159);
            if (hintView != null) {
                hintView.setVisibility(8);
                this.mViewAttached = true;
            }
        }
    }

    private void removeProximityView() {
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
            }
        }
    }
}
