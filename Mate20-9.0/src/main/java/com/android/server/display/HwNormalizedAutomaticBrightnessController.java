package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.BrightnessConfiguration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.HwNormalizedSpline;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.display.AutomaticBrightnessController;
import com.android.server.display.DarkAdaptDetector;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.HwBrightnessPgSceneDetection;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.gesture.GestureNavConst;
import com.android.server.lights.LightsManager;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.huawei.displayengine.DisplayEngineManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class HwNormalizedAutomaticBrightnessController extends AutomaticBrightnessController implements HwBrightnessPgSceneDetection.HwBrightnessPgSceneDetectionCallbacks {
    private static final int BACK_SENSOR_COVER_MODE_BEIGHTNESS = -3;
    private static final int BRIGHTNESS_FOR_PROXIMITY_POSITIVE = -2;
    private static final int BRIGHTNESS_FOR_SENSOR_NOT_READY_WHEN_WAKEUP = -1;
    private static final boolean HWDEBUG;
    /* access modifiers changed from: private */
    public static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String HW_CUSTOMIZATION_SCREEN_BRIGHTNESS_MODE = "hw_customization_screen_brightness_mode";
    private static final String KEY_DC_BRIGHTNESS_DIMMING_SWITCH = "hw_dc_brightness_dimming_switch";
    private static final String KEY_READING_MODE_SWITCH = "hw_reading_mode_display_switch";
    private static final int MAXDEFAULTBRIGHTNESS = 255;
    private static final int MINDEFAULTBRIGHTNESS = 4;
    private static final int MODE_DEFAULT = 0;
    private static final int MODE_TOP_GAME = 1;
    private static final int MSG_CoverMode_DEBOUNCED = 4;
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 3;
    private static final int MSG_REPORT_PROXIMITY_STATE = 2;
    private static final int MSG_UPDATE_AMBIENT_LUX = 1;
    private static final int MSG_UPDATE_AUTO_BRIGHTNESS = 6;
    private static final int MSG_UPDATE_LANDSCAPE = 5;
    private static final long POWER_ON_LUX_ABANDON_COUNT_MAX = 3;
    private static final int POWER_ON_LUX_COUNT_MAX = 8;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_UNKNOWN = -1;
    public static final String SCREEN_BRIGHTNESS_MODE_LAST = "screen_brightness_mode_last";
    /* access modifiers changed from: private */
    public static String TAG = "HwNormalizedAutomaticBrightnessController";
    private static final int TIME_DELAYED_USING_PROXIMITY_STATE = 500;
    private static final int TIME_PRINT_SENSOR_VALUE_INTERVAL = 4000;
    private static final int TIME_SENSOR_REPORT_NONE_VALUE = 400;
    private static int mDeviceActualBrightnessLevel;
    private static int mDeviceActualBrightnessNit;
    private static int mDeviceStandardBrightnessNit;
    private static HwNormalizedSpline mHwNormalizedScreenAutoBrightnessSpline;
    private static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public int SENSOR_OPTION;
    private long gameModeEnterTimestamp;
    private long gameModeQuitTimestamp;
    private boolean mAllowLabcUseProximity;
    private float mAmbientLuxLast;
    private float mAmbientLuxLastLast;
    private float mAmbientLuxOffset;
    private boolean mAnimationGameChangeEnable;
    private float mAutoBrightnessOut;
    private final HandlerThread mAutoBrightnessProcessThread;
    private boolean mAutoPowerSavingAnimationEnable;
    private boolean mAutoPowerSavingBrighnessLineDisableForDemo;
    private boolean mBrightnessChageUpStatus;
    private int mBrightnessOutForLog;
    private boolean mCameraModeChangeAnimationEnable;
    private boolean mCameraModeEnable;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCoverModeFastResponseTimeDelay;
    private boolean mCoverStateFast;
    private CryogenicPowerProcessor mCryogenicProcessor;
    /* access modifiers changed from: private */
    public int mCurrentAutoBrightness;
    private boolean mCurrentUserChanging;
    /* access modifiers changed from: private */
    public int mCurrentUserId;
    private int mCurveLevel;
    private DarkAdaptDetector mDarkAdaptDetector;
    private boolean mDarkAdaptDimmingEnable;
    private DarkAdaptDetector.AdaptState mDarkAdaptState;
    private final HwBrightnessXmlLoader.Data mData;
    private boolean mDcModeBrightnessEnable;
    private DcModeObserver mDcModeObserver;
    private boolean mDcModeObserverInitialize;
    private float mDefaultBrightness;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private volatile boolean mDragFinished;
    /* access modifiers changed from: private */
    public int mDualSensorRawAmbient;
    /* access modifiers changed from: private */
    public int mEyeProtectionMode;
    private ContentObserver mEyeProtectionModeObserver;
    private int mGameLevel;
    private boolean mGameModeEnableForOffset;
    private HwRingBuffer mHwAmbientLightRingBuffer;
    private HwRingBuffer mHwAmbientLightRingBufferTrace;
    private HwAmbientLuxFilterAlgo mHwAmbientLuxFilterAlgo;
    private HwBrightnessMapping mHwBrightnessMapping;
    /* access modifiers changed from: private */
    public HwBrightnessPgSceneDetection mHwBrightnessPgSceneDetection;
    private HwBrightnessPowerSavingCurve mHwBrightnessPowerSavingCurve;
    /* access modifiers changed from: private */
    public HwDualSensorEventListenerImpl mHwDualSensorEventListenerImpl;
    private HwEyeProtectionControllerImpl mHwEyeProtectionController;
    private int mHwLastReportedSensorValue;
    private long mHwLastReportedSensorValueTime;
    private int mHwLastSensorValue;
    /* access modifiers changed from: private */
    public final HwNormalizedAutomaticBrightnessHandler mHwNormalizedAutomaticBrightnessHandler;
    private long mHwPrintLogTime;
    private int mHwRateMillis;
    private boolean mHwReportValueWhenSensorOnChange;
    private boolean mIntervenedAutoBrightnessEnable;
    private boolean mIsBrightnessLimitedByThermal;
    private boolean mIsProximitySceneModeOpened;
    private boolean mIsVideoPlay;
    private boolean mIsclosed;
    /* access modifiers changed from: private */
    public boolean mLandScapeModeState;
    private LandscapeStateReceiver mLandscapeStateReceiver;
    private long mLastAmbientLightToMonitorTime;
    private int mLastDefaultBrightness;
    private DisplayEngineManager mManager;
    private Runnable mMaxBrightnessFromCryogenicDelayedRunnable;
    private Handler mMaxBrightnessFromCryogenicHandler;
    private boolean mPolicyChangeFromDim;
    private long mPowerOffVehicleTimestamp;
    private boolean mPowerOnEnable;
    private int mPowerOnLuxAbandonCount;
    private int mPowerOnLuxCount;
    private boolean mPowerOnOffStatus;
    private long mPowerOnVehicleTimestamp;
    private int mPowerPolicy;
    private String mPowerStateNameForMonitor;
    private boolean mPowerStatus;
    private int mProximity;
    /* access modifiers changed from: private */
    public boolean mProximityPositive;
    /* access modifiers changed from: private */
    public long mProximityReportTime;
    private final Sensor mProximitySensor;
    /* access modifiers changed from: private */
    public boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener;
    /* access modifiers changed from: private */
    public int mReadingMode;
    private boolean mReadingModeChangeAnimationEnable;
    private boolean mReadingModeEnable;
    private ContentObserver mReadingModeObserver;
    private int mResetAmbientLuxDisableBrightnessOffset;
    private int mSceneLevel;
    private volatile int mScreenBrightnessBeforeAdj;
    private ScreenStateReceiver mScreenStateReceiver;
    private boolean mScreenStatus;
    private SensorObserver mSensorObserver;
    private long mSetCurrentAutoBrightnessTime;
    private SettingsObserver mSettingsObserver;
    private boolean mSettingsObserverInitialize;
    private TouchProximityDetector mTouchProximityDetector;
    private boolean mVehicleModeQuitEnable;
    private boolean mWakeupCoverBrightnessEnable;

    private final class DcModeObserver extends ContentObserver {
        public DcModeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            HwNormalizedAutomaticBrightnessController.this.handleDcModeSettingsChange();
        }
    }

    private final class HwNormalizedAutomaticBrightnessHandler extends Handler {
        public HwNormalizedAutomaticBrightnessHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwNormalizedAutomaticBrightnessController.this.handleUpdateAmbientLuxMsg();
                    return;
                case 2:
                    HwNormalizedAutomaticBrightnessController.this.handleProximitySensorEvent();
                    return;
                case 3:
                    HwNormalizedAutomaticBrightnessController.this.debounceProximitySensor();
                    return;
                case 4:
                    HwNormalizedAutomaticBrightnessController.this.setCoverModeFastResponseFlag();
                    return;
                case 5:
                    HwNormalizedAutomaticBrightnessController.this.updateLandScapeMode(HwNormalizedAutomaticBrightnessController.this.mLandScapeModeState);
                    return;
                case 6:
                    HwNormalizedAutomaticBrightnessController.this.updateCurrentAutoBrightness(HwNormalizedAutomaticBrightnessController.this.mCurrentAutoBrightness);
                    return;
                default:
                    return;
            }
        }
    }

    private class LandscapeStateReceiver extends BroadcastReceiver {
        public LandscapeStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            filter.setPriority(1000);
            HwNormalizedAutomaticBrightnessController.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Slog.e(HwNormalizedAutomaticBrightnessController.TAG, "LandscapeStateReceiver Invalid input parameter!");
                return;
            }
            if ("android.intent.action.CONFIGURATION_CHANGED".equals(intent.getAction())) {
                int ori = HwNormalizedAutomaticBrightnessController.this.mContext.getResources().getConfiguration().orientation;
                if (ori == 2) {
                    boolean unused = HwNormalizedAutomaticBrightnessController.this.mLandScapeModeState = true;
                } else if (ori == 1) {
                    boolean unused2 = HwNormalizedAutomaticBrightnessController.this.mLandScapeModeState = false;
                }
                if (HwNormalizedAutomaticBrightnessController.HWFLOW) {
                    String access$300 = HwNormalizedAutomaticBrightnessController.TAG;
                    Slog.d(access$300, "LandScapeBrightMode MSG_UPDATE_LANDSCAPE mLandScapeModeState=" + HwNormalizedAutomaticBrightnessController.this.mLandScapeModeState);
                }
                HwNormalizedAutomaticBrightnessController.this.sendLandScapeStateUpdate(HwNormalizedAutomaticBrightnessController.this.mLandScapeModeState);
            }
        }
    }

    private class ScreenStateReceiver extends BroadcastReceiver {
        public ScreenStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.setPriority(1000);
            HwNormalizedAutomaticBrightnessController.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Slog.e(HwNormalizedAutomaticBrightnessController.TAG, "Invalid input parameter!");
                return;
            }
            String action = intent.getAction();
            String access$300 = HwNormalizedAutomaticBrightnessController.TAG;
            Slog.i(access$300, "BroadcastReceiver.onReceive() action:" + action);
            if ("android.intent.action.BOOT_COMPLETED".equals(action) && !HwNormalizedAutomaticBrightnessController.this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted()) {
                HwNormalizedAutomaticBrightnessController.this.mHwBrightnessPgSceneDetection.registerPgBLightSceneListener(HwNormalizedAutomaticBrightnessController.this.mContext);
                if (HwNormalizedAutomaticBrightnessController.HWFLOW) {
                    String access$3002 = HwNormalizedAutomaticBrightnessController.TAG;
                    Slog.d(access$3002, "BOOT_COMPLETED: auto in registerPgBLightSceneChangedListener,=" + HwNormalizedAutomaticBrightnessController.this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted());
                }
            }
        }
    }

    private class SensorObserver implements Observer {
        public SensorObserver() {
        }

        public void update(Observable o, Object arg) {
            long[] data = (long[]) arg;
            int lux = (int) data[0];
            long systemTimeStamp = data[2];
            long sensorTimeStamp = data[3];
            if (HwNormalizedAutomaticBrightnessController.HWFLOW && systemTimeStamp - HwNormalizedAutomaticBrightnessController.this.mLightSensorEnableTime < 4000) {
                String access$300 = HwNormalizedAutomaticBrightnessController.TAG;
                Slog.d(access$300, "ambient lux=" + lux + ",systemTimeStamp =" + systemTimeStamp);
            }
            if ((!HwServiceFactory.shouldFilteInvalidSensorVal((float) lux) || AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE != 0) && !HwNormalizedAutomaticBrightnessController.this.interceptHandleLightSensorEvent(sensorTimeStamp, (float) lux)) {
                HwNormalizedAutomaticBrightnessController.this.handleLightSensorEvent(systemTimeStamp, (float) lux);
                int access$1900 = HwNormalizedAutomaticBrightnessController.this.SENSOR_OPTION;
                HwDualSensorEventListenerImpl unused = HwNormalizedAutomaticBrightnessController.this.mHwDualSensorEventListenerImpl;
                if (access$1900 == 2) {
                    int unused2 = HwNormalizedAutomaticBrightnessController.this.mDualSensorRawAmbient = (int) data[4];
                }
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            HwNormalizedAutomaticBrightnessController.this.handleBrightnessSettingsChange();
        }
    }

    static {
        boolean z = true;
        if (!Log.HWLog && (!Log.HWModuleLog || !Log.isLoggable(TAG, 3))) {
            z = false;
        }
        HWDEBUG = z;
        mDeviceActualBrightnessLevel = 0;
        mDeviceActualBrightnessNit = 0;
        mDeviceStandardBrightnessNit = 0;
        mDeviceActualBrightnessLevel = getDeviceActualBrightnessLevel();
        mDeviceActualBrightnessNit = getDeviceActualBrightnessNit();
        mDeviceStandardBrightnessNit = getDeviceStandardBrightnessNit();
        if (HWFLOW) {
            Slog.i(TAG, "DeviceActualLevel=" + mDeviceActualBrightnessLevel + ",DeviceActualBrightnessNit=" + mDeviceActualBrightnessNit + ",DeviceStandardBrightnessNit=" + mDeviceStandardBrightnessNit);
        }
    }

    private static int getDeviceActualBrightnessLevel() {
        try {
            return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceActualBrightnessLevel();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int getDeviceActualBrightnessNit() {
        try {
            return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceActualBrightnessNit();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int getDeviceStandardBrightnessNit() {
        try {
            return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceStandardBrightnessNit();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static HwNormalizedSpline createHwNormalizedAutoBrightnessSpline(Context context) {
        try {
            mHwNormalizedScreenAutoBrightnessSpline = HwNormalizedSpline.createHwNormalizedSpline(context, mDeviceActualBrightnessLevel, mDeviceActualBrightnessNit, mDeviceStandardBrightnessNit);
            return mHwNormalizedScreenAutoBrightnessSpline;
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, "Could not create auto-brightness spline.", ex);
            return null;
        }
    }

    public void updateStateRecognition(boolean usePwrBLightCurve, int appType) {
        if (this.mLightSensorEnabled && !this.mAutoPowerSavingBrighnessLineDisableForDemo) {
            if (this.mGameLevel != 21 || !mHwNormalizedScreenAutoBrightnessSpline.getPowerSavingBrighnessLineEnable()) {
                if (this.mData.autoPowerSavingUseManualAnimationTimeEnable) {
                    if (mHwNormalizedScreenAutoBrightnessSpline.getPowerSavingModeBrightnessChangeEnable(this.mAmbientLux, usePwrBLightCurve)) {
                        this.mAutoPowerSavingAnimationEnable = true;
                    } else {
                        this.mAutoPowerSavingAnimationEnable = false;
                    }
                }
                mHwNormalizedScreenAutoBrightnessSpline.setPowerSavingModeEnable(usePwrBLightCurve);
                if (this.mHwBrightnessPowerSavingCurve != null) {
                    this.mHwBrightnessPowerSavingCurve.setPowerSavingEnable(usePwrBLightCurve);
                }
                updateAutoBrightness(true);
            } else {
                Slog.i(TAG, "GameBrightMode no orig powerSaving");
            }
        }
    }

    private static boolean isDemoVersion() {
        String vendor2 = SystemProperties.get("ro.hw.vendor", "");
        String country = SystemProperties.get("ro.hw.country", "");
        String str = TAG;
        Slog.i(str, "vendor:" + vendor2 + ",country:" + country);
        return "demo".equalsIgnoreCase(vendor2) || "demo".equalsIgnoreCase(country);
    }

    public boolean getAnimationGameChangeEnable() {
        boolean animationEnable = this.mAnimationGameChangeEnable && this.mData.gameModeEnable;
        if (!this.mHwAmbientLuxFilterAlgo.getProximityPositiveEnable()) {
            this.mAnimationGameChangeEnable = false;
        }
        if (HWFLOW && animationEnable != this.mAnimationGameChangeEnable) {
            String str = TAG;
            Slog.d(str, "GameBrightMode set dimming animationEnable=" + this.mAnimationGameChangeEnable);
        }
        return animationEnable;
    }

    public boolean getGameModeEnable() {
        return this.mData.gameModeEnable;
    }

    public boolean getAutoPowerSavingUseManualAnimationTimeEnable() {
        return this.mData.autoPowerSavingUseManualAnimationTimeEnable;
    }

    public boolean getAutoPowerSavingAnimationEnable() {
        return this.mAutoPowerSavingAnimationEnable;
    }

    public void setAutoPowerSavingAnimationEnable(boolean enable) {
        this.mAutoPowerSavingAnimationEnable = enable;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public HwNormalizedAutomaticBrightnessController(AutomaticBrightnessController.Callbacks callbacks, Looper looper, SensorManager sensorManager, BrightnessMappingStrategy mapper, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, int initialLightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, HysteresisLevels hysteresisLevels, Context context) {
        super(callbacks, looper, sensorManager, mapper, lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounceConfig, darkeningLightDebounceConfig, resetAmbientLuxAfterWarmUpConfig, hysteresisLevels);
        SensorManager sensorManager2 = sensorManager;
        Context context2 = context;
        this.mPowerStatus = false;
        this.mScreenStatus = false;
        this.mPowerOnLuxCount = 0;
        this.mPowerOnLuxAbandonCount = 0;
        this.mCurrentUserId = 0;
        this.mCurrentUserChanging = false;
        this.mHwRateMillis = 300;
        this.mHwPrintLogTime = -1;
        this.mHwLastSensorValue = -1;
        this.mHwLastReportedSensorValue = -1;
        this.mHwLastReportedSensorValueTime = -1;
        this.mHwReportValueWhenSensorOnChange = true;
        this.mHwAmbientLightRingBuffer = new HwRingBuffer(10);
        this.mHwAmbientLightRingBufferTrace = new HwRingBuffer(50);
        this.mPowerPolicy = 0;
        this.mPolicyChangeFromDim = false;
        this.mIntervenedAutoBrightnessEnable = false;
        this.mProximity = -1;
        this.mCoverModeFastResponseTimeDelay = GestureNavConst.CHECK_AFT_TIMEOUT;
        this.mCoverStateFast = false;
        this.mIsclosed = false;
        this.mCameraModeEnable = false;
        this.mReadingModeEnable = false;
        this.mCameraModeChangeAnimationEnable = false;
        this.mReadingModeChangeAnimationEnable = false;
        this.mDefaultBrightness = -1.0f;
        this.mBrightnessOutForLog = -1;
        this.mAmbientLuxOffset = -1.0f;
        this.SENSOR_OPTION = -1;
        this.mDualSensorRawAmbient = -1;
        this.mScreenStateReceiver = null;
        this.mScreenBrightnessBeforeAdj = -1;
        this.mDragFinished = true;
        this.mReadingMode = 0;
        this.mEyeProtectionMode = 0;
        this.mCurveLevel = -1;
        this.mSceneLevel = -1;
        this.mGameLevel = -1;
        this.gameModeEnterTimestamp = 0;
        this.gameModeQuitTimestamp = 0;
        this.mGameModeEnableForOffset = false;
        this.mAnimationGameChangeEnable = false;
        this.mDcModeObserver = null;
        this.mDcModeObserverInitialize = true;
        this.mDcModeBrightnessEnable = false;
        this.mAutoPowerSavingBrighnessLineDisableForDemo = false;
        this.mLandscapeStateReceiver = null;
        this.mLandScapeModeState = false;
        this.mSettingsObserver = null;
        this.mSettingsObserverInitialize = true;
        this.mCurrentAutoBrightness = 0;
        this.mResetAmbientLuxDisableBrightnessOffset = 0;
        this.mBrightnessChageUpStatus = false;
        this.mSetCurrentAutoBrightnessTime = -1;
        this.mIsProximitySceneModeOpened = false;
        this.mIsVideoPlay = false;
        this.mAutoPowerSavingAnimationEnable = false;
        this.mReadingModeObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                int unused = HwNormalizedAutomaticBrightnessController.this.mReadingMode = Settings.System.getIntForUser(HwNormalizedAutomaticBrightnessController.this.mContext.getContentResolver(), HwNormalizedAutomaticBrightnessController.KEY_READING_MODE_SWITCH, 0, HwNormalizedAutomaticBrightnessController.this.mCurrentUserId);
                String access$300 = HwNormalizedAutomaticBrightnessController.TAG;
                Slog.i(access$300, "mReadingMode is " + HwNormalizedAutomaticBrightnessController.this.mReadingMode + ", mEyeProtectionMode is " + HwNormalizedAutomaticBrightnessController.this.mEyeProtectionMode);
                if (HwNormalizedAutomaticBrightnessController.this.mEyeProtectionMode != 1 && HwNormalizedAutomaticBrightnessController.this.mEyeProtectionMode != 3) {
                    return;
                }
                if (HwNormalizedAutomaticBrightnessController.this.mReadingMode == 1) {
                    HwNormalizedAutomaticBrightnessController.this.setReadingModeBrightnessLineEnable(true);
                } else {
                    HwNormalizedAutomaticBrightnessController.this.setReadingModeBrightnessLineEnable(false);
                }
            }
        };
        this.mEyeProtectionModeObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                int unused = HwNormalizedAutomaticBrightnessController.this.mEyeProtectionMode = Settings.System.getIntForUser(HwNormalizedAutomaticBrightnessController.this.mContext.getContentResolver(), "eyes_protection_mode", 0, HwNormalizedAutomaticBrightnessController.this.mCurrentUserId);
                String access$300 = HwNormalizedAutomaticBrightnessController.TAG;
                Slog.i(access$300, "mEyeProtectionMode is " + HwNormalizedAutomaticBrightnessController.this.mEyeProtectionMode + ", mReadingMode is " + HwNormalizedAutomaticBrightnessController.this.mReadingMode);
                if (HwNormalizedAutomaticBrightnessController.this.mReadingMode != 1) {
                    return;
                }
                if (HwNormalizedAutomaticBrightnessController.this.mEyeProtectionMode == 1 || HwNormalizedAutomaticBrightnessController.this.mEyeProtectionMode == 3) {
                    HwNormalizedAutomaticBrightnessController.this.setReadingModeBrightnessLineEnable(true);
                } else {
                    HwNormalizedAutomaticBrightnessController.this.setReadingModeBrightnessLineEnable(false);
                }
            }
        };
        this.mPowerOnOffStatus = false;
        this.mPowerOnVehicleTimestamp = 0;
        this.mPowerOffVehicleTimestamp = 0;
        this.mVehicleModeQuitEnable = false;
        this.mWakeupCoverBrightnessEnable = false;
        this.mProximitySensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if (HwNormalizedAutomaticBrightnessController.this.mProximitySensorEnabled) {
                    boolean z = false;
                    float distance = event.values[0];
                    long unused = HwNormalizedAutomaticBrightnessController.this.mProximityReportTime = SystemClock.uptimeMillis();
                    HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController = HwNormalizedAutomaticBrightnessController.this;
                    if (distance >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && distance < 5.0f) {
                        z = true;
                    }
                    boolean unused2 = hwNormalizedAutomaticBrightnessController.mProximityPositive = z;
                    if (HwNormalizedAutomaticBrightnessController.HWFLOW) {
                        String access$300 = HwNormalizedAutomaticBrightnessController.TAG;
                        Slog.d(access$300, "mProximitySensorListener: time = " + HwNormalizedAutomaticBrightnessController.this.mProximityReportTime + "; distance = " + distance);
                    }
                    if (!HwNormalizedAutomaticBrightnessController.this.mWakeupFromSleep && HwNormalizedAutomaticBrightnessController.this.mProximityReportTime - HwNormalizedAutomaticBrightnessController.this.mLightSensorEnableTime > 500) {
                        HwNormalizedAutomaticBrightnessController.this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessage(2);
                    }
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mPowerOnEnable = false;
        this.mHwAmbientLuxFilterAlgo = new HwAmbientLuxFilterAlgo(lightSensorRate);
        this.mScreenAutoBrightnessSpline = createHwNormalizedAutoBrightnessSpline(context);
        this.mAutoBrightnessProcessThread = new HandlerThread(TAG);
        this.mAutoBrightnessProcessThread.start();
        this.mHwNormalizedAutomaticBrightnessHandler = new HwNormalizedAutomaticBrightnessHandler(this.mAutoBrightnessProcessThread.getLooper());
        this.mHwReportValueWhenSensorOnChange = this.mHwAmbientLuxFilterAlgo.reportValueWhenSensorOnChange();
        this.mProximitySensor = sensorManager2.getDefaultSensor(8);
        this.mAllowLabcUseProximity = this.mHwAmbientLuxFilterAlgo.needToUseProximity();
        if (SystemProperties.getInt("ro.config.hw_eyes_protection", 7) != 0) {
            this.mHwEyeProtectionController = new HwEyeProtectionControllerImpl(context2, this);
        }
        this.mData = HwBrightnessXmlLoader.getData();
        this.mContext = context2;
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(context);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        sendXmlConfigToMonitor();
        this.mHwBrightnessPgSceneDetection = new HwBrightnessPgSceneDetection(this, this.mData.pgSceneDetectionDarkenDelayTime, this.mData.pgSceneDetectionBrightenDelayTime, this.mContext);
        this.mHwDualSensorEventListenerImpl = HwDualSensorEventListenerImpl.getInstance(sensorManager2, this.mContext);
        this.SENSOR_OPTION = this.mHwDualSensorEventListenerImpl.getModuleSensorOption(TAG);
        if (this.mData.darkAdapterEnable) {
            this.mDarkAdaptDetector = new DarkAdaptDetector(this.mData);
        }
        this.mHwBrightnessMapping = new HwBrightnessMapping(this.mData.brightnessMappingPoints);
        if (this.mData.pgReregisterScene) {
            this.mScreenStateReceiver = new ScreenStateReceiver();
        }
        if (this.mData.landScapeBrightnessModeEnable) {
            this.mLandscapeStateReceiver = new LandscapeStateReceiver();
        }
        this.mHwBrightnessPowerSavingCurve = new HwBrightnessPowerSavingCurve(this.mData.manualBrightnessMaxLimit, this.mData.screenBrightnessMinNit, this.mData.screenBrightnessMaxNit);
        if (this.mData.cryogenicEnable) {
            this.mMaxBrightnessFromCryogenicHandler = new Handler();
            this.mMaxBrightnessFromCryogenicDelayedRunnable = new Runnable() {
                public void run() {
                    HwNormalizedAutomaticBrightnessController.this.setMaxBrightnessFromCryogenicDelayed();
                }
            };
        }
        if (this.mData.readingModeEnable) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_READING_MODE_SWITCH), true, this.mReadingModeObserver, -1);
            this.mReadingMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_READING_MODE_SWITCH, 0, this.mCurrentUserId);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("eyes_protection_mode"), true, this.mEyeProtectionModeObserver, -1);
            this.mEyeProtectionMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 0, this.mCurrentUserId);
            Slog.i(TAG, "readingModeEnable enable ...");
        }
        if (this.mData.touchProximityEnable) {
            this.mTouchProximityDetector = new TouchProximityDetector(this.mData);
        }
        if (this.mData.autoPowerSavingBrighnessLineDisableForDemo) {
            this.mAutoPowerSavingBrighnessLineDisableForDemo = isDemoVersion();
        }
        if (this.mData.luxlinePointsForBrightnessLevelEnable) {
            this.mSettingsObserver = new SettingsObserver(this.mHwNormalizedAutomaticBrightnessHandler);
        }
        if (this.mData.dcModeEnable) {
            this.mDcModeObserver = new DcModeObserver(this.mHwNormalizedAutomaticBrightnessHandler);
        }
        if (this.mData.brightnessOffsetLuxModeEnable) {
            initBrightnessOffsetPara();
        }
    }

    private void initBrightnessOffsetPara() {
        if (mHwNormalizedScreenAutoBrightnessSpline != null) {
            mHwNormalizedScreenAutoBrightnessSpline.initBrightenOffsetLux(this.mData.brightnessOffsetLuxModeEnable, this.mData.brightenOffsetLuxTh1, this.mData.brightenOffsetLuxTh2, this.mData.brightenOffsetLuxTh3);
            mHwNormalizedScreenAutoBrightnessSpline.initBrightenOffsetNoValidDarkenLux(this.mData.brightenOffsetEffectMinLuxEnable, this.mData.brightenOffsetNoValidDarkenLuxTh1, this.mData.brightenOffsetNoValidDarkenLuxTh2, this.mData.brightenOffsetNoValidDarkenLuxTh3, this.mData.brightenOffsetNoValidDarkenLuxTh4);
            mHwNormalizedScreenAutoBrightnessSpline.initBrightenOffsetNoValidBrightenLux(this.mData.brightenOffsetNoValidBrightenLuxTh1, this.mData.brightenOffsetNoValidBrightenLuxTh2, this.mData.brightenOffsetNoValidBrightenLuxTh3, this.mData.brightenOffsetNoValidBrightenLuxTh4);
            mHwNormalizedScreenAutoBrightnessSpline.initDarkenOffsetLux(this.mData.darkenOffsetLuxTh1, this.mData.darkenOffsetLuxTh2, this.mData.darkenOffsetLuxTh3);
            mHwNormalizedScreenAutoBrightnessSpline.initDarkenOffsetNoValidBrightenLux(this.mData.darkenOffsetNoValidBrightenLuxTh1, this.mData.darkenOffsetNoValidBrightenLuxTh2, this.mData.darkenOffsetNoValidBrightenLuxTh3, this.mData.darkenOffsetNoValidBrightenLuxTh4);
            mHwNormalizedScreenAutoBrightnessSpline.initBrightnessOffsetTmpValidPara(this.mData.brightnessOffsetTmpValidEnable, this.mData.brightenOffsetNoValidSavedLuxTh1, this.mData.brightenOffsetNoValidSavedLuxTh2);
        }
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public HwNormalizedAutomaticBrightnessController(AutomaticBrightnessController.Callbacks callbacks, Looper looper, SensorManager sensorManager, BrightnessMappingStrategy mapper, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, Context context) {
        this(callbacks, looper, sensorManager, mapper, lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, 0, 0, 0, 0, false, null, context);
        if (SystemProperties.getInt("ro.config.hw_eyes_protection", 7) != 0) {
            this.mHwEyeProtectionController = new HwEyeProtectionControllerImpl(context, this);
            return;
        }
        Context context2 = context;
    }

    public void configure(boolean enable, float adjustment, boolean dozing) {
    }

    public void configure(boolean enable, BrightnessConfiguration configuration, float brightness, boolean userChangedBrightness, float adjustment, boolean userChangedAutoBrightnessAdjustment, int displayPolicy) {
        boolean dozing = displayPolicy == 1;
        if (this.mLightSensorEnabled && !enable) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
            this.mHwAmbientLuxFilterAlgo.clear();
            if (!this.mHwReportValueWhenSensorOnChange) {
                clearSensorData();
            }
            this.mLastAmbientLightToMonitorTime = 0;
            if (this.mDarkAdaptDetector != null) {
                this.mDarkAdaptDetector.setAutoModeOff();
                this.mDarkAdaptDimmingEnable = false;
            }
        }
        if (!enable) {
            this.mHwAmbientLuxFilterAlgo.setPowerStatus(false);
        }
        HwNormalizedAutomaticBrightnessController.super.configure(enable, configuration, brightness, userChangedBrightness, adjustment, userChangedAutoBrightnessAdjustment, displayPolicy);
        if (this.mLightSensorEnabled && -1 == this.mHwPrintLogTime) {
            this.mHwPrintLogTime = this.mLightSensorEnableTime;
        }
        if (enable && !dozing && !this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted()) {
            this.mHwBrightnessPgSceneDetection.registerPgBLightSceneListener(this.mContext);
            if (HWFLOW) {
                String str = TAG;
                Slog.d(str, "PowerSaving auto in registerPgBLightSceneChangedListener,=" + this.mHwBrightnessPgSceneDetection.getPGBLListenerRegisted());
            }
        }
        if (this.mData.proximitySceneModeEnable) {
            setProximitySensorEnabled(this.mIsProximitySceneModeOpened && enable && !dozing);
        } else {
            setProximitySensorEnabled(this.mAllowLabcUseProximity && enable && !dozing);
        }
        if (this.mTouchProximityDetector != null) {
            if (enable) {
                this.mTouchProximityDetector.enable();
            } else {
                this.mTouchProximityDetector.disable();
            }
        }
        if (this.mData.luxlinePointsForBrightnessLevelEnable && this.mSettingsObserverInitialize) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_auto_brightness"), false, this.mSettingsObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_brightness"), false, this.mSettingsObserver, -1);
            this.mSettingsObserverInitialize = false;
            Slog.i(TAG, "mSettingsObserver Initialize");
        }
        if (this.mData.dcModeEnable && this.mDcModeObserverInitialize) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_DC_BRIGHTNESS_DIMMING_SWITCH), true, this.mDcModeObserver, -1);
            this.mDcModeObserverInitialize = false;
            Slog.i(TAG, "DcModeObserver Initialize");
            updateDcMode();
        }
    }

    public int getAutomaticScreenBrightness() {
        if (this.mWakeupFromSleep && SystemClock.uptimeMillis() - this.mLightSensorEnableTime < 200) {
            if (HWFLOW) {
                String str = TAG;
                Slog.d(str, "mWakeupFromSleep= " + this.mWakeupFromSleep + ",currentTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableTime=" + this.mLightSensorEnableTime);
            }
            return -1;
        } else if (needToSetBrightnessBaseIntervened()) {
            return HwNormalizedAutomaticBrightnessController.super.getAutomaticScreenBrightness();
        } else {
            if (needToSetBrightnessBaseProximity()) {
                return -2;
            }
            return HwNormalizedAutomaticBrightnessController.super.getAutomaticScreenBrightness();
        }
    }

    private boolean needToSetBrightnessBaseIntervened() {
        return this.mIntervenedAutoBrightnessEnable && (this.mAllowLabcUseProximity || this.mData.proximitySceneModeEnable);
    }

    public int getAutoBrightnessBaseInOutDoorLimit(int brightness) {
        int tmpBrightnessOut = brightness;
        if (this.mAmbientLux >= ((float) this.mData.outDoorThreshold) || !this.mData.autoModeInOutDoorLimitEnble) {
            return tmpBrightnessOut;
        }
        return tmpBrightnessOut < this.mData.manualBrightnessMaxLimit ? tmpBrightnessOut : this.mData.manualBrightnessMaxLimit;
    }

    public void setPersonalizedBrightnessCurveLevel(int curveLevel) {
        int gameLevel;
        if (!(curveLevel == 19 || curveLevel == 18 || curveLevel == 20 || curveLevel == 21)) {
            if (curveLevel != this.mCurveLevel) {
                if (mHwNormalizedScreenAutoBrightnessSpline != null) {
                    mHwNormalizedScreenAutoBrightnessSpline.setPersonalizedBrightnessCurveLevel(curveLevel);
                } else {
                    Slog.e(TAG, "NewCurveMode setPersonalizedBrightnessCurveLevel fail,mHwNormalizedScreenAutoBrightnessSpline==null");
                }
                if (HWFLOW) {
                    String str = TAG;
                    Slog.d(str, "NewCurveMode setPersonalizedBrightnessCurveLevel curveLevel=" + curveLevel);
                }
            }
            this.mCurveLevel = curveLevel;
        }
        if (!(!this.mData.gameModeEnable || curveLevel == 19 || curveLevel == 18)) {
            if (curveLevel == 21) {
                gameLevel = 21;
            } else {
                gameLevel = 20;
            }
            if (gameLevel != this.mGameLevel) {
                mHwNormalizedScreenAutoBrightnessSpline.setGameCurveLevel(gameLevel);
                this.mAnimationGameChangeEnable = true;
                if (gameLevel == 21) {
                    this.mGameModeEnableForOffset = true;
                    this.mHwAmbientLuxFilterAlgo.setGameModeEnable(true);
                    setProximitySceneMode(true);
                    this.gameModeEnterTimestamp = SystemClock.elapsedRealtime();
                    long timeDelta = this.gameModeEnterTimestamp - this.gameModeQuitTimestamp;
                    if (timeDelta > this.mData.gameModeClearOffsetTime) {
                        float ambientLuxOffset = mHwNormalizedScreenAutoBrightnessSpline.getGameModeAmbientLuxForOffset();
                        if (!this.mData.gameModeOffsetValidAmbientLuxEnable || ambientLuxOffset == -1.0f) {
                            mHwNormalizedScreenAutoBrightnessSpline.clearGameOffsetDelta();
                        } else {
                            mHwNormalizedScreenAutoBrightnessSpline.resetGameModeOffsetFromHumanFactor((int) calculateOffsetMinBrightness(ambientLuxOffset, 1), (int) calculateOffsetMaxBrightness(ambientLuxOffset, 1));
                        }
                        mHwNormalizedScreenAutoBrightnessSpline.resetGameBrightnessLimitation();
                        String str2 = TAG;
                        Slog.i(str2, "GameBrightMode enterGame timeDelta=" + timeDelta);
                    }
                } else {
                    this.mHwAmbientLuxFilterAlgo.setGameModeEnable(false);
                    setProximitySceneMode(false);
                    this.mGameModeEnableForOffset = false;
                    this.gameModeQuitTimestamp = SystemClock.elapsedRealtime();
                }
                String str3 = TAG;
                Slog.i(str3, "GameBrightMode updateAutoBrightness,gameLevel=" + gameLevel);
                updateAutoBrightness(true);
            }
            this.mGameLevel = gameLevel;
        }
        if (this.mData.vehicleModeEnable && (curveLevel == 19 || curveLevel == 18)) {
            if (mHwNormalizedScreenAutoBrightnessSpline != null) {
                mHwNormalizedScreenAutoBrightnessSpline.setSceneCurveLevel(curveLevel);
                if (curveLevel == 19) {
                    this.mVehicleModeQuitEnable = false;
                    long timDelta = SystemClock.elapsedRealtime() - this.mPowerOnVehicleTimestamp;
                    if (timDelta > this.mData.vehicleModeEnterTimeForPowerOn) {
                        updateAutoBrightness(true);
                        if (HWFLOW) {
                            String str4 = TAG;
                            Slog.i(str4, "VehicleBrightMode updateAutoBrightness curveLevel=" + curveLevel + ",timDelta=" + timDelta);
                        }
                    }
                } else if (curveLevel == 18 && this.mVehicleModeQuitEnable) {
                    long timDelta2 = SystemClock.elapsedRealtime() - this.mPowerOnVehicleTimestamp;
                    boolean vehicleModeBrightnessEnable = mHwNormalizedScreenAutoBrightnessSpline.getVehicleModeBrightnessEnable();
                    if (timDelta2 < this.mData.vehicleModeQuitTimeForPowerOn && vehicleModeBrightnessEnable) {
                        mHwNormalizedScreenAutoBrightnessSpline.setVehicleModeQuitEnable();
                        String str5 = TAG;
                        Slog.i(str5, "VehicleBrightMode mVehicleModeQuitEnable timDelta=" + timDelta2);
                    }
                }
                if (HWFLOW && this.mSceneLevel != curveLevel) {
                    String str6 = TAG;
                    Slog.d(str6, "VehicleBrightMode set curveLevel=" + curveLevel);
                }
                this.mSceneLevel = curveLevel;
            } else {
                Slog.e(TAG, "VehicleBrightMode setSceneCurveLevel fail,mHwNormalizedScreenAutoBrightnessSpline==null");
            }
        }
    }

    public void updateNewBrightnessCurveTmp() {
        if (mHwNormalizedScreenAutoBrightnessSpline != null) {
            mHwNormalizedScreenAutoBrightnessSpline.updateNewBrightnessCurveTmp();
            sendPersonalizedCurveAndParamToMonitor(mHwNormalizedScreenAutoBrightnessSpline.getPersonalizedDefaultCurve(), mHwNormalizedScreenAutoBrightnessSpline.getPersonalizedAlgoParam());
            return;
        }
        Slog.e(TAG, "NewCurveMode updateNewBrightnessCurveTmp fail,mHwNormalizedScreenAutoBrightnessSpline==null");
    }

    public void updateNewBrightnessCurve() {
        if (mHwNormalizedScreenAutoBrightnessSpline != null) {
            mHwNormalizedScreenAutoBrightnessSpline.updateNewBrightnessCurve();
        } else {
            Slog.e(TAG, "NewCurveMode updateNewBrightnessCurve fail,mHwNormalizedScreenAutoBrightnessSpline==null");
        }
    }

    public List<PointF> getCurrentDefaultNewCurveLine() {
        List<PointF> brighntessList = new ArrayList<>();
        if (mHwNormalizedScreenAutoBrightnessSpline != null) {
            return mHwNormalizedScreenAutoBrightnessSpline.getCurrentDefaultNewCurveLine();
        }
        return brighntessList;
    }

    public void updateIntervenedAutoBrightness(int brightness) {
        this.mAutoBrightnessOut = (float) brightness;
        this.mIntervenedAutoBrightnessEnable = true;
        if (this.mData.cryogenicEnable) {
            this.mMaxBrightnessSetByCryogenicBypass = true;
        }
        if (!this.mData.manualMode) {
            HwNormalizedAutomaticBrightnessController.super.updateIntervenedAutoBrightness((int) this.mAutoBrightnessOut);
            return;
        }
        if (this.mDragFinished) {
            this.mScreenBrightnessBeforeAdj = getAutomaticScreenBrightness();
            this.mDragFinished = false;
        }
        this.mDefaultBrightness = mHwNormalizedScreenAutoBrightnessSpline.getCurrentDefaultBrightnessNoOffset();
        float lux = mHwNormalizedScreenAutoBrightnessSpline.getCurrentAmbientLuxForBrightness();
        if (HWFLOW) {
            String str = TAG;
            Slog.i(str, "HwAutoBrightnessIn=" + brightness + ",defaultBrightness=" + this.mDefaultBrightness + ",lux=" + lux);
        }
        if (this.mAutoBrightnessOut >= ((float) this.mData.manualBrightnessMaxLimit)) {
            if (lux > ((float) this.mData.outDoorThreshold)) {
                int autoBrightnessOutTmp = ((int) this.mAutoBrightnessOut) < this.mData.manualBrightnessMaxLimit ? (int) this.mAutoBrightnessOut : this.mData.manualBrightnessMaxLimit;
                this.mAutoBrightnessOut = autoBrightnessOutTmp > ((int) this.mDefaultBrightness) ? (float) autoBrightnessOutTmp : (float) ((int) this.mDefaultBrightness);
            } else {
                this.mAutoBrightnessOut = this.mAutoBrightnessOut < ((float) this.mData.manualBrightnessMaxLimit) ? this.mAutoBrightnessOut : (float) this.mData.manualBrightnessMaxLimit;
            }
        }
        if (HWFLOW) {
            String str2 = TAG;
            Slog.i(str2, "HwAutoBrightnessOut=" + this.mAutoBrightnessOut);
        }
        HwNormalizedAutomaticBrightnessController.super.updateIntervenedAutoBrightness((int) this.mAutoBrightnessOut);
    }

    private int getSensorData() {
        synchronized (this.mHwAmbientLightRingBuffer) {
            long time = SystemClock.uptimeMillis();
            int N = this.mHwAmbientLightRingBuffer.size();
            if (N > 0) {
                int sum = 0;
                for (int i = N - 1; i >= 0; i--) {
                    sum = (int) (((float) sum) + this.mHwAmbientLightRingBuffer.getLux(i));
                }
                int i2 = sum / N;
                if (i2 >= 0) {
                    this.mHwLastSensorValue = i2;
                }
                this.mHwAmbientLightRingBuffer.clear();
                if (time - this.mHwPrintLogTime > 4000) {
                    int N2 = this.mHwAmbientLightRingBufferTrace.size();
                    if (HWFLOW) {
                        Slog.d("lux trace:", this.mHwAmbientLightRingBufferTrace.toString(N2));
                    }
                    this.mHwAmbientLightRingBufferTrace.clear();
                    this.mHwPrintLogTime = time;
                }
                int i3 = this.mHwLastSensorValue;
                return i3;
            } else if (time - this.mHwLastReportedSensorValueTime < 400) {
                int i4 = this.mHwLastSensorValue;
                return i4;
            } else {
                int i5 = this.mHwLastReportedSensorValue;
                return i5;
            }
        }
    }

    private void clearSensorData() {
        synchronized (this.mHwAmbientLightRingBuffer) {
            this.mHwAmbientLightRingBuffer.clear();
            int N = this.mHwAmbientLightRingBufferTrace.size();
            if (HWFLOW) {
                Slog.d("lux trace:", this.mHwAmbientLightRingBufferTrace.toString(N));
            }
            this.mHwAmbientLightRingBufferTrace.clear();
            this.mHwLastReportedSensorValueTime = -1;
            this.mHwLastReportedSensorValue = -1;
            this.mHwLastSensorValue = -1;
            this.mHwPrintLogTime = -1;
        }
    }

    private float getTouchProximityProcessedLux(boolean isFirstLux, float lux) {
        if (this.mTouchProximityDetector == null) {
            return lux;
        }
        if (!isFirstLux) {
            boolean isCurrentLuxValid = this.mTouchProximityDetector.isCurrentLuxValid();
            if (bypassTouchProximityResult()) {
                isCurrentLuxValid = true;
            }
            boolean needUseLastLux = false;
            updateTouchProximityState(!isCurrentLuxValid);
            float lastLux = this.mAmbientLuxLastLast > this.mAmbientLuxLast ? this.mAmbientLuxLastLast : this.mAmbientLuxLast;
            if (!isCurrentLuxValid && lux < lastLux) {
                needUseLastLux = true;
            }
            if (needUseLastLux) {
                lux = lastLux;
            }
        }
        this.mTouchProximityDetector.startNextLux();
        this.mAmbientLuxLastLast = isFirstLux ? lux : this.mAmbientLuxLast;
        this.mAmbientLuxLast = lux;
        return lux;
    }

    private void reportLightSensorEventToAlgo(long time, float lux) {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
        float lux2 = getTouchProximityProcessedLux(!this.mAmbientLuxValid, lux);
        if (!this.mAmbientLuxValid) {
            this.mWakeupFromSleep = false;
            this.mAmbientLuxValid = true;
            this.mHwAmbientLuxFilterAlgo.isFirstAmbientLux(true);
            if (this.mData.dayModeAlgoEnable || this.mData.offsetResetEnable) {
                this.mHwAmbientLuxFilterAlgo.setAutoModeEnableFirstLux(lux2);
                this.mHwAmbientLuxFilterAlgo.setDayModeEnable();
                if (this.mData.dayModeAlgoEnable) {
                    mHwNormalizedScreenAutoBrightnessSpline.setDayModeEnable(this.mHwAmbientLuxFilterAlgo.getDayModeEnable());
                }
                if (this.mData.offsetResetEnable) {
                    this.mAmbientLuxOffset = mHwNormalizedScreenAutoBrightnessSpline.getCurrentAmbientLuxForOffset();
                    if (this.mAmbientLuxOffset != -1.0f) {
                        int mOffsetScreenBrightnessMinByAmbientLux = (int) calculateOffsetMinBrightness(this.mAmbientLuxOffset, 0);
                        int mOffsetScreenBrightnessMaxByAmbientLux = (int) calculateOffsetMaxBrightness(this.mAmbientLuxOffset, 0);
                        if (mHwNormalizedScreenAutoBrightnessSpline.getPersonalizedBrightnessCurveEnable()) {
                            float defaultBrightness = mHwNormalizedScreenAutoBrightnessSpline.getDefaultBrightness(this.mAmbientLuxOffset);
                            float currentBrightness = mHwNormalizedScreenAutoBrightnessSpline.getNewCurrentBrightness(this.mAmbientLuxOffset);
                            mOffsetScreenBrightnessMinByAmbientLux += ((int) currentBrightness) - ((int) defaultBrightness);
                            mOffsetScreenBrightnessMaxByAmbientLux += ((int) currentBrightness) - ((int) defaultBrightness);
                            String str = TAG;
                            Slog.i(str, "NewCurveMode new offset MinByAmbientLux=" + mOffsetScreenBrightnessMinByAmbientLux + ",maxByAmbientLux" + mOffsetScreenBrightnessMaxByAmbientLux);
                        }
                        mHwNormalizedScreenAutoBrightnessSpline.reSetOffsetFromHumanFactor(this.mHwAmbientLuxFilterAlgo.getOffsetResetEnable(), mOffsetScreenBrightnessMinByAmbientLux, mOffsetScreenBrightnessMaxByAmbientLux);
                    }
                    unlockDarkAdaptLine();
                }
                if (HWFLOW) {
                    String str2 = TAG;
                    Slog.d(str2, "DayMode:dayModeEnable=" + this.mHwAmbientLuxFilterAlgo.getDayModeEnable() + ",offsetEnable=" + this.mHwAmbientLuxFilterAlgo.getOffsetResetEnable());
                }
            }
            if (HWFLOW) {
                String str3 = TAG;
                Slog.d(str3, "mAmbientLuxValid=" + this.mAmbientLuxValid + ",mWakeupFromSleep= " + this.mWakeupFromSleep);
            }
        }
        this.mHwAmbientLuxFilterAlgo.handleLightSensorEvent(time, lux2);
        this.mAmbientLux = this.mHwAmbientLuxFilterAlgo.getCurrentAmbientLux();
        boolean isDarkAdaptStateChanged = handleDarkAdaptDetector(lux2);
        if (this.mHwAmbientLuxFilterAlgo.needToUpdateBrightness() || isDarkAdaptStateChanged) {
            if (HWFLOW) {
                String str4 = TAG;
                Slog.d(str4, "need to update brightness: mAmbientLux=" + this.mAmbientLux);
            }
            this.mHwAmbientLuxFilterAlgo.brightnessUpdated();
            if (this.mData.darkTimeDelayEnable) {
                float defaultBrightness2 = mHwNormalizedScreenAutoBrightnessSpline.getNewDefaultBrightness(this.mAmbientLux);
                if (this.mAmbientLux < this.mData.darkTimeDelayLuxThreshold || defaultBrightness2 >= this.mData.darkTimeDelayBrightness) {
                    this.mHwAmbientLuxFilterAlgo.setDarkTimeDelayFromBrightnessEnable(false);
                } else {
                    if (HWFLOW) {
                        String str5 = TAG;
                        Slog.d(str5, "DarkTimeDelay mAmbientLux=" + this.mAmbientLux + ",defaultBrightness=" + defaultBrightness2 + ",thresh=" + this.mData.darkTimeDelayBrightness);
                    }
                    this.mHwAmbientLuxFilterAlgo.setDarkTimeDelayFromBrightnessEnable(true);
                }
            }
            updateAutoBrightness(true);
        }
        if (!this.mHwReportValueWhenSensorOnChange) {
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(1, (long) this.mHwRateMillis);
        }
        if (!this.mProximityPositive) {
            sendAmbientLightToMonitor(time, lux2);
        } else {
            this.mLastAmbientLightToMonitorTime = 0;
        }
        sendDefaultBrightnessToMonitor();
    }

    private float calculateOffsetMinBrightness(float amLux, int mode) {
        List<HwXmlAmPoint> brightnessPoints;
        if (amLux < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            Slog.w(TAG, "amlux<0, return offsetMIN");
            return 4.0f;
        }
        float offsetMinBrightness = 4.0f;
        HwXmlAmPoint temp1 = null;
        if (mode == 1) {
            brightnessPoints = this.mData.gameModeAmbientLuxValidBrightnessPoints;
        } else {
            brightnessPoints = this.mData.ambientLuxValidBrightnessPoints;
        }
        Iterator iter = brightnessPoints.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            HwXmlAmPoint temp = iter.next();
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                HwXmlAmPoint temp2 = temp;
                if (temp2.x <= temp1.x) {
                    offsetMinBrightness = 4.0f;
                    if (HWFLOW) {
                        String str = TAG;
                        Slog.i(str, "OffsetMinBrightness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    }
                } else {
                    offsetMinBrightness = (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                }
            } else {
                temp1 = temp;
                offsetMinBrightness = temp1.y;
            }
        }
        return offsetMinBrightness;
    }

    private float calculateOffsetMaxBrightness(float amLux, int mode) {
        List<HwXmlAmPoint> brightnessPoints;
        if (amLux < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            Slog.w(TAG, "amlux<0, return offsetMAX");
            return 255.0f;
        }
        float offsetMaxBrightness = 255.0f;
        HwXmlAmPoint temp1 = null;
        if (mode == 1) {
            brightnessPoints = this.mData.gameModeAmbientLuxValidBrightnessPoints;
        } else {
            brightnessPoints = this.mData.ambientLuxValidBrightnessPoints;
        }
        Iterator iter = brightnessPoints.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            HwXmlAmPoint temp = iter.next();
            if (temp1 == null) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                HwXmlAmPoint temp2 = temp;
                if (temp2.x <= temp1.x) {
                    offsetMaxBrightness = 255.0f;
                    if (HWFLOW) {
                        String str = TAG;
                        Slog.i(str, "OffsetMaxBrightness_temp1.x <= temp2.x,x" + temp.x + ", z = " + temp.z);
                    }
                } else {
                    offsetMaxBrightness = (((temp2.z - temp1.z) / (temp2.x - temp1.x)) * (amLux - temp1.x)) + temp1.z;
                }
            } else {
                temp1 = temp;
                offsetMaxBrightness = temp1.z;
            }
        }
        return offsetMaxBrightness;
    }

    private void sendAmbientLightToMonitor(long time, float lux) {
        if (this.mDisplayEffectMonitor != null) {
            if (this.mLastAmbientLightToMonitorTime == 0 || time <= this.mLastAmbientLightToMonitorTime) {
                this.mLastAmbientLightToMonitorTime = time;
                return;
            }
            this.mLastAmbientLightToMonitorTime = time;
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "ambientLightCollection");
            params.put("lightValue", Integer.valueOf((int) lux));
            params.put("durationInMs", Integer.valueOf((int) (time - this.mLastAmbientLightToMonitorTime)));
            params.put("brightnessMode", "AUTO");
            if (this.mDualSensorRawAmbient >= 0) {
                params.put("rawLightValue", Integer.valueOf(this.mDualSensorRawAmbient));
            }
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    private void sendDefaultBrightnessToMonitor() {
        if (this.mDisplayEffectMonitor != null) {
            int defaultBrightness = (int) mHwNormalizedScreenAutoBrightnessSpline.getCurrentDefaultBrightnessNoOffset();
            if (this.mLastDefaultBrightness != defaultBrightness) {
                this.mLastDefaultBrightness = defaultBrightness;
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "algoDefaultBrightness");
                params.put("lightValue", Integer.valueOf((int) mHwNormalizedScreenAutoBrightnessSpline.getCurrentAmbientLuxForBrightness()));
                params.put("brightness", Integer.valueOf(defaultBrightness));
                params.put("brightnessMode", "AUTO");
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    private void sendPowerStateToMonitor(int policy) {
        String newStateName;
        if (this.mDisplayEffectMonitor != null) {
            switch (policy) {
                case 0:
                case 1:
                    newStateName = AwareJobSchedulerConstants.BAR_STATUS_OFF;
                    break;
                case 2:
                    newStateName = "DIM";
                    break;
                case 3:
                    newStateName = AwareJobSchedulerConstants.BAR_STATUS_ON;
                    break;
                case 4:
                    newStateName = "VR";
                    break;
                default:
                    newStateName = AwareJobSchedulerConstants.BAR_STATUS_OFF;
                    break;
            }
            if (this.mPowerStateNameForMonitor != newStateName) {
                this.mPowerStateNameForMonitor = newStateName;
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "powerStateUpdate");
                params.put("powerState", newStateName);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    private void sendXmlConfigToMonitor() {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "xmlConfig");
            params.put("enable", Boolean.valueOf(this.mData.monitorEnable));
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    private void sendPersonalizedCurveAndParamToMonitor(List<Short> curve, List<Float> algoParam) {
        if (this.mDisplayEffectMonitor != null && curve != null && !curve.isEmpty() && algoParam != null && !algoParam.isEmpty()) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "personalizedCurveAndParam");
            params.put("personalizedCurve", curve);
            params.put("personalizedParam", algoParam);
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    /* access modifiers changed from: protected */
    public void setBrightnessLimitedByThermal(boolean isLimited) {
        sendThermalLimitToMonitor(isLimited);
    }

    private void sendThermalLimitToMonitor(boolean isLimited) {
        if (this.mDisplayEffectMonitor != null && this.mIsBrightnessLimitedByThermal != isLimited) {
            this.mIsBrightnessLimitedByThermal = isLimited;
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "thermalLimit");
            params.put("isLimited", Boolean.valueOf(isLimited));
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    /* access modifiers changed from: protected */
    public void handleLightSensorEvent(long time, float lux) {
        if (mHwNormalizedScreenAutoBrightnessSpline.getCalibrationTestEable()) {
            this.mSetbrightnessImmediateEnable = true;
            getLightSensorFromDB();
            return;
        }
        this.mSetbrightnessImmediateEnable = false;
        if (!this.mAmbientLuxValid || this.mHwReportValueWhenSensorOnChange) {
            reportLightSensorEventToAlgo(time, lux);
            if (!this.mHwReportValueWhenSensorOnChange) {
                synchronized (mLock) {
                    this.mHwLastReportedSensorValue = (int) lux;
                    this.mHwLastReportedSensorValueTime = time;
                }
                return;
            }
            return;
        }
        synchronized (this.mHwAmbientLightRingBuffer) {
            this.mHwAmbientLightRingBuffer.push(time, lux);
            this.mHwAmbientLightRingBufferTrace.push(time, lux);
            this.mHwLastReportedSensorValue = (int) lux;
            this.mHwLastReportedSensorValueTime = time;
        }
    }

    /* access modifiers changed from: protected */
    public void getLightSensorFromDB() {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
        this.mAmbientLuxValid = true;
        float ambientLux = mHwNormalizedScreenAutoBrightnessSpline.getAmbientValueFromDB();
        if (((int) (ambientLux * 10.0f)) != ((int) (this.mAmbientLux * 10.0f))) {
            this.mAmbientLux = ambientLux;
            if (HWFLOW) {
                String str = TAG;
                Slog.d(str, "setAmbientLuxDB=" + this.mAmbientLux);
            }
            updateAutoBrightness(true);
        }
    }

    /* access modifiers changed from: private */
    public void setMaxBrightnessFromCryogenicDelayed() {
        if (HWFLOW) {
            Slog.d(TAG, "mMaxBrightnessSetByCryogenicBypassDelayed=false");
        }
        this.mMaxBrightnessSetByCryogenicBypassDelayed = false;
        if (this.mMaxBrightnessSetByCryogenic < 255 && this.mLightSensorEnabled) {
            String str = TAG;
            Slog.i(str, "Cryogenic set mMaxBrightnessSetByCryogenic=" + this.mMaxBrightnessSetByCryogenic);
            this.mCallbacks.updateBrightness();
        }
    }

    public void setPowerStatus(boolean powerStatus) {
        if (this.mData.cryogenicEnable) {
            if (powerStatus) {
                this.mPowerOnTimestamp = SystemClock.elapsedRealtime();
                if (this.mPowerOnTimestamp - this.mPowerOffTimestamp > this.mData.cryogenicActiveScreenOffIntervalInMillis) {
                    if (HWFLOW) {
                        String str = TAG;
                        Slog.d(str, "mPowerOnTimestamp - mPowerOffTimestamp=" + (this.mPowerOnTimestamp - this.mPowerOffTimestamp) + ", apply Cryogenic brightness limit(" + this.mMaxBrightnessSetByCryogenic + ")!");
                    }
                    this.mMaxBrightnessSetByCryogenicBypass = false;
                }
                if (HWFLOW) {
                    String str2 = TAG;
                    Slog.d(str2, "mMaxBrightnessSetByCryogenicBypass=" + this.mMaxBrightnessSetByCryogenicBypass + " mMaxBrightnessSetByCryogenicBypassDelayed=" + this.mMaxBrightnessSetByCryogenicBypassDelayed);
                }
                if (this.mMaxBrightnessSetByCryogenic == 255) {
                    this.mMaxBrightnessSetByCryogenicBypassDelayed = true;
                    if (HWFLOW) {
                        String str3 = TAG;
                        Slog.d(str3, "No Cryogenic brightness limit! Then it should be active " + (((double) this.mData.cryogenicLagTimeInMillis) / 60000.0d) + "min later!");
                    }
                    if (!(this.mMaxBrightnessFromCryogenicHandler == null || this.mMaxBrightnessFromCryogenicDelayedRunnable == null)) {
                        this.mMaxBrightnessFromCryogenicHandler.removeCallbacks(this.mMaxBrightnessFromCryogenicDelayedRunnable);
                        this.mMaxBrightnessFromCryogenicHandler.postDelayed(this.mMaxBrightnessFromCryogenicDelayedRunnable, this.mData.cryogenicLagTimeInMillis);
                    }
                }
            } else {
                this.mPowerOffTimestamp = SystemClock.elapsedRealtime();
                if (this.mCryogenicProcessor != null) {
                    this.mCryogenicProcessor.onScreenOff();
                }
            }
        }
        if (HWFLOW && this.mPowerStatus != powerStatus) {
            String str4 = TAG;
            Slog.d(str4, "set power status:mPowerStatus=" + this.mPowerStatus + ",powerStatus=" + powerStatus);
        }
        if (this.mPowerStatus != powerStatus && powerStatus && this.mData.coverModeDayEnable) {
            updateCoverModeDayBrightness();
        }
        if (this.mPowerOnOffStatus != powerStatus) {
            if (!powerStatus) {
                boolean enableTmp = mHwNormalizedScreenAutoBrightnessSpline.getNewCurveEableTmp();
                boolean newCurveEnable = mHwNormalizedScreenAutoBrightnessSpline.setNewCurveEnable(enableTmp);
                if (enableTmp) {
                    String str5 = TAG;
                    Slog.d(str5, "NewCurveMode poweroff updateNewCurve(tem--real),enableTmp=" + enableTmp + ",powerStatus=" + powerStatus);
                }
            }
            mHwNormalizedScreenAutoBrightnessSpline.setPowerStatus(powerStatus);
            if (this.mData.vehicleModeEnable) {
                if (powerStatus) {
                    this.mPowerOnVehicleTimestamp = SystemClock.elapsedRealtime();
                    if (this.mPowerOnVehicleTimestamp - this.mPowerOffVehicleTimestamp > this.mData.vehicleModeDisableTimeMillis) {
                        boolean vehicleEnable = mHwNormalizedScreenAutoBrightnessSpline.getVehicleModeBrightnessEnable();
                        boolean vehicleQuitEnable = mHwNormalizedScreenAutoBrightnessSpline.getVehicleModeQuitForPowerOnEnable();
                        if (vehicleEnable && vehicleQuitEnable) {
                            mHwNormalizedScreenAutoBrightnessSpline.setVehicleModeQuitEnable();
                            if (HWFLOW) {
                                Slog.d(TAG, "VehicleBrightMode quit from lastOnScreen");
                            }
                        }
                        this.mVehicleModeQuitEnable = true;
                        if (HWFLOW) {
                            String str6 = TAG;
                            Slog.d(str6, "VehicleBrightMode mVehicleModeQuitEnable OnOfftime=" + (this.mPowerOnVehicleTimestamp - this.mPowerOffVehicleTimestamp));
                        }
                    }
                } else {
                    this.mVehicleModeQuitEnable = false;
                    this.mPowerOffVehicleTimestamp = SystemClock.elapsedRealtime();
                }
            }
        }
        this.mPowerOnOffStatus = powerStatus;
        this.mPowerStatus = powerStatus;
        this.mScreenStatus = powerStatus;
        this.mWakeupFromSleep = powerStatus;
        this.mHwAmbientLuxFilterAlgo.setPowerStatus(powerStatus);
        if (!this.mPowerStatus) {
            this.mPowerOnLuxAbandonCount = 0;
            this.mPowerOnLuxCount = 0;
            this.mWakeupCoverBrightnessEnable = false;
        }
        if (this.mHwEyeProtectionController != null) {
            this.mHwEyeProtectionController.onScreenStateChanged(powerStatus);
        }
    }

    private void updateCoverModeDayBrightness() {
        boolean isClosed = HwServiceFactory.isCoverClosed();
        int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mCurrentUserId);
        if (isClosed && brightnessMode == 1) {
            int openHour = Calendar.getInstance().get(11);
            if (openHour >= this.mData.converModeDayBeginTime && openHour < this.mData.coverModeDayEndTime) {
                setCoverModeDayEnable(true);
                this.mWakeupCoverBrightnessEnable = true;
                String str = TAG;
                Slog.i(str, "LabcCoverMode,isClosed=" + isClosed + ",openHour=" + openHour + ",coverModeBrightness=" + this.mData.coverModeDayBrightness);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean interceptHandleLightSensorEvent(long time, float lux) {
        if (SystemClock.uptimeMillis() < ((long) this.mLightSensorWarmUpTimeConfig) + this.mLightSensorEnableTime) {
            Slog.i(TAG, "sensor not ready yet at time " + time);
            return true;
        } else if (this.mCurrentUserChanging) {
            return true;
        } else {
            if (this.mPowerStatus) {
                this.mPowerOnLuxAbandonCount++;
                this.mPowerOnLuxCount++;
                if (this.mPowerOnLuxCount > getpowerOnFastResponseLuxNum()) {
                    if (HWFLOW) {
                        Slog.d(TAG, "set power status:false,powerOnFastResponseLuxNum=" + getpowerOnFastResponseLuxNum());
                    }
                    this.mPowerStatus = false;
                    this.mHwAmbientLuxFilterAlgo.setPowerStatus(false);
                }
                if (this.mLightSensorEnableElapsedTimeNanos - time > 0) {
                    if (HWFLOW) {
                        Slog.d(TAG, "abandon handleLightSensorEvent:" + lux);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
        this.mIntervenedAutoBrightnessEnable = false;
        float lux = this.mHwAmbientLuxFilterAlgo.getOffsetValidAmbientLux();
        if (this.mData.offsetValidAmbientLuxEnable) {
            float luxCurrent = this.mHwAmbientLuxFilterAlgo.getCurrentAmbientLux();
            boolean proximityPositiveEnable = this.mHwAmbientLuxFilterAlgo.getProximityPositiveEnable();
            float positionBrightness = adjustFactor * 255.0f;
            float defautBrightness = mHwNormalizedScreenAutoBrightnessSpline.getCurrentDefaultBrightnessNoOffset();
            if (proximityPositiveEnable && ((int) positionBrightness) > ((int) defautBrightness)) {
                lux = luxCurrent;
            }
            this.mHwAmbientLuxFilterAlgo.setCurrentAmbientLux(lux);
        }
        if (HWFLOW) {
            String str = TAG;
            Slog.i(str, "AdjustPositionBrightness=" + ((int) (adjustFactor * 255.0f)) + ",lux=" + lux);
        }
        if (this.mGameModeEnableForOffset) {
            mHwNormalizedScreenAutoBrightnessSpline.updateLevelGameWithLux(adjustFactor * 255.0f, lux);
        } else {
            mHwNormalizedScreenAutoBrightnessSpline.updateLevelWithLux(adjustFactor * 255.0f, lux);
        }
        int brightnessOffset = (int) (255.0f * adjustFactor);
        if (this.mResetAmbientLuxDisableBrightnessOffset == 0 && brightnessOffset > this.mData.resetAmbientLuxDisableBrightnessOffset) {
            updateBrightnessModeChangeManualState(false);
        }
        if (brightnessOffset > this.mData.resetAmbientLuxDisableBrightnessOffset) {
            this.mResetAmbientLuxDisableBrightnessOffset = brightnessOffset;
        } else {
            this.mResetAmbientLuxDisableBrightnessOffset = 0;
        }
    }

    /* access modifiers changed from: protected */
    public boolean setAutoBrightnessAdjustment(float adjustment) {
        return false;
    }

    public void saveOffsetAlgorithmParas() {
    }

    /* access modifiers changed from: private */
    public void handleUpdateAmbientLuxMsg() {
        reportLightSensorEventToAlgo(SystemClock.uptimeMillis(), (float) getSensorData());
    }

    /* access modifiers changed from: protected */
    public void updateBrightnessIfNoAmbientLuxReported() {
        if (this.mWakeupFromSleep) {
            this.mWakeupFromSleep = false;
            this.mCallbacks.updateBrightness();
            this.mFirstAutoBrightness = false;
            this.mUpdateAutoBrightnessCount++;
            if (HWFLOW) {
                Slog.d(TAG, "sensor doesn't report lux in 200ms");
            }
        }
    }

    public void updateCurrentUserId(int userId) {
        if (userId != this.mCurrentUserId) {
            if (HWFLOW) {
                String str = TAG;
                Slog.d(str, "user change from  " + this.mCurrentUserId + " into " + userId);
            }
            this.mCurrentUserId = userId;
            this.mCurrentUserChanging = true;
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
            this.mAmbientLuxValid = false;
            this.mHwAmbientLuxFilterAlgo.clear();
            mHwNormalizedScreenAutoBrightnessSpline.updateCurrentUserId(userId);
            this.mCurrentUserChanging = false;
            updateDcMode();
        }
    }

    private void setProximitySensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mProximitySensorEnabled) {
                this.mProximitySensorEnabled = true;
                getSensorManager().registerListener(this.mProximitySensorListener, this.mProximitySensor, 3, this.mHwNormalizedAutomaticBrightnessHandler);
                if (HWFLOW) {
                    Slog.d(TAG, "open proximity sensor");
                }
            }
        } else if (this.mProximitySensorEnabled) {
            this.mProximitySensorEnabled = false;
            this.mProximity = -1;
            getSensorManager().unregisterListener(this.mProximitySensorListener);
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(3);
            this.mCallbacks.updateProximityState(false);
            if (HWFLOW) {
                Slog.d(TAG, "close proximity sensor");
            }
        }
    }

    private void processProximityState() {
        int proximity = this.mHwAmbientLuxFilterAlgo.getProximityState();
        if (this.mProximity != proximity) {
            if (HWFLOW) {
                String str = TAG;
                Slog.d(str, "mProximity=" + this.mProximity + ",proximity=" + proximity);
            }
            if (this.mProximity == 1 && proximity == 0) {
                this.mFirstBrightnessAfterProximityNegative = true;
            }
            this.mProximity = proximity;
            if (this.mProximity != -1) {
                if (this.mProximity == 1) {
                    this.mCallbacks.updateProximityState(true);
                } else if (this.mProximity == 0) {
                    this.mCallbacks.updateProximityState(false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleProximitySensorEvent() {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(3);
        this.mHwAmbientLuxFilterAlgo.handleProximitySensorEvent(this.mProximityReportTime, this.mProximityPositive);
        processProximityState();
        if (this.mHwAmbientLuxFilterAlgo.needToSendProximityDebounceMsg()) {
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageAtTime(3, this.mHwAmbientLuxFilterAlgo.getPendingProximityDebounceTime());
        }
    }

    /* access modifiers changed from: private */
    public void debounceProximitySensor() {
        if (HWFLOW) {
            Slog.d(TAG, "process MSG_PROXIMITY_SENSOR_DEBOUNCED");
        }
        this.mHwAmbientLuxFilterAlgo.debounceProximitySensor();
        processProximityState();
    }

    public void updatePowerPolicy(int policy) {
        boolean powerOnEnable = wantScreenOn(policy);
        if (powerOnEnable != this.mPowerOnEnable) {
            setPowerStatus(powerOnEnable);
        }
        this.mPowerOnEnable = powerOnEnable;
        if (this.mPowerPolicy != 2 || policy == 2) {
            this.mPolicyChangeFromDim = false;
        } else {
            this.mPolicyChangeFromDim = true;
        }
        this.mPowerPolicy = policy;
        sendPowerStateToMonitor(policy);
    }

    private static boolean wantScreenOn(int state) {
        switch (state) {
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }

    private boolean needToSetBrightnessBaseProximity() {
        boolean z = true;
        if (this.mProximity != 1 || this.mBrightnessEnlarge || this.mUpdateAutoBrightnessCount <= 1 || this.mPowerPolicy == 2 || this.mPolicyChangeFromDim) {
            z = false;
        }
        boolean needToSet = z;
        if (HWFLOW && needToSet) {
            String str = TAG;
            Slog.d(str, "mProximity= " + this.mProximity + ",mBrightnessEnlarge=" + this.mBrightnessEnlarge + ",mUpdateAutoBrightnessCount=" + this.mUpdateAutoBrightnessCount + ",mPowerPolicy=" + this.mPowerPolicy + ",mPolicyChangeFromDim=" + this.mPolicyChangeFromDim);
        }
        return needToSet;
    }

    public void setSplineEyeProtectionControlFlag(boolean flag) {
        if (mHwNormalizedScreenAutoBrightnessSpline != null) {
            mHwNormalizedScreenAutoBrightnessSpline.setEyeProtectionControlFlag(flag);
        }
    }

    public void setReadingModeBrightnessLineEnable(boolean readingMode) {
        if (mHwNormalizedScreenAutoBrightnessSpline != null && getReadingModeBrightnessLineEnable()) {
            mHwNormalizedScreenAutoBrightnessSpline.setReadingModeEnable(readingMode);
            if ((readingMode || !this.mReadingModeEnable) && (!readingMode || this.mReadingModeEnable)) {
                this.mReadingModeChangeAnimationEnable = false;
            } else {
                this.mReadingModeChangeAnimationEnable = true;
                updateAutoBrightness(true);
            }
            if (HWFLOW) {
                String str = TAG;
                Slog.d(str, "setReadingModeControlFlag: " + readingMode + ", mReadingModeChangeAnimationEnable: " + this.mReadingModeChangeAnimationEnable);
            }
            this.mReadingModeEnable = readingMode;
        }
    }

    public boolean getPowerStatus() {
        return this.mPowerStatus;
    }

    public boolean getScreenStatus() {
        return this.mScreenStatus;
    }

    public void setCoverModeStatus(boolean isclosed) {
        if (isclosed) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(4);
        }
        if (!isclosed && this.mIsclosed) {
            this.mCoverStateFast = true;
            this.mHwAmbientLuxFilterAlgo.setCoverModeFastResponseFlag(this.mCoverStateFast);
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(4, (long) this.mCoverModeFastResponseTimeDelay);
        }
        this.mIsclosed = isclosed;
        this.mHwAmbientLuxFilterAlgo.setCoverModeStatus(isclosed);
    }

    public void setCoverModeFastResponseFlag() {
        this.mCoverStateFast = false;
        this.mHwAmbientLuxFilterAlgo.setCoverModeFastResponseFlag(this.mCoverStateFast);
        mHwNormalizedScreenAutoBrightnessSpline.setNoOffsetEnable(this.mCoverStateFast);
        if (HWFLOW) {
            String str = TAG;
            Slog.i(str, "LabcCoverMode FastResponseFlag =" + this.mCoverStateFast);
        }
    }

    public boolean getCoverModeFastResponseFlag() {
        return this.mCoverStateFast;
    }

    public void setBackSensorCoverModeBrightness(int brightness) {
        if (this.mData.backSensorCoverModeEnable && brightness > 0) {
            this.mScreenAutoBrightness = brightness;
            this.mHwAmbientLuxFilterAlgo.setBackSensorCoverModeBrightness(brightness);
        }
    }

    public int getpowerOnFastResponseLuxNum() {
        if (this.mHwAmbientLuxFilterAlgo == null) {
            return 8;
        }
        return this.mHwAmbientLuxFilterAlgo.getpowerOnFastResponseLuxNum();
    }

    public void updateAutoDBWhenSameBrightness(int brightness) {
        int brightnessAutoDB = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", 0, this.mCurrentUserId);
        int brightnessManualDB = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", 0, this.mCurrentUserId);
        if (brightnessAutoDB != brightness && brightnessManualDB == brightness) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", brightness, this.mCurrentUserId);
            if (HWFLOW) {
                String str = TAG;
                Slog.d(str, "OrigAutoDB=" + brightnessAutoDB + ",ManualDB=" + brightnessManualDB + ",brightness=" + brightness + ",mScreenAutoBrightness=" + this.mScreenAutoBrightness);
            }
        }
    }

    public boolean getCameraModeBrightnessLineEnable() {
        if (this.mHwAmbientLuxFilterAlgo == null) {
            return false;
        }
        return this.mHwAmbientLuxFilterAlgo.getCameraModeBrightnessLineEnable();
    }

    public boolean getReadingModeBrightnessLineEnable() {
        if (this.mHwAmbientLuxFilterAlgo == null) {
            return false;
        }
        return this.mHwAmbientLuxFilterAlgo.getReadingModeBrightnessLineEnable();
    }

    public void setCameraModeBrightnessLineEnable(boolean cameraModeEnable) {
        if (mHwNormalizedScreenAutoBrightnessSpline != null && getCameraModeBrightnessLineEnable()) {
            mHwNormalizedScreenAutoBrightnessSpline.setCameraModeEnable(cameraModeEnable);
            if (this.mCameraModeEnable != cameraModeEnable) {
                if (HWFLOW) {
                    String str = TAG;
                    Slog.d(str, "CameraModeEnable change cameraModeEnable=" + cameraModeEnable);
                }
                this.mCameraModeChangeAnimationEnable = true;
                updateAutoBrightness(true);
            }
            this.mCameraModeEnable = cameraModeEnable;
        }
    }

    public boolean getCameraModeChangeAnimationEnable() {
        boolean animationEnable = this.mCameraModeChangeAnimationEnable;
        this.mCameraModeChangeAnimationEnable = false;
        return animationEnable;
    }

    public boolean getReadingModeChangeAnimationEnable() {
        boolean mStatus = this.mReadingModeChangeAnimationEnable;
        this.mReadingModeChangeAnimationEnable = false;
        return mStatus;
    }

    public void setKeyguardLockedStatus(boolean isLocked) {
        if (this.mHwAmbientLuxFilterAlgo == null) {
            Slog.e(TAG, "mHwAmbientLuxFilterAlgo=null");
        } else {
            this.mHwAmbientLuxFilterAlgo.setKeyguardLockedStatus(isLocked);
        }
    }

    public boolean getRebootAutoModeEnable() {
        if (TextUtils.isEmpty(SystemProperties.get("ro.product.EcotaVersion", "")) || Settings.System.getIntForUser(this.mContext.getContentResolver(), HW_CUSTOMIZATION_SCREEN_BRIGHTNESS_MODE, 1, this.mCurrentUserId) != 0) {
            return this.mData.rebootAutoModeEnable;
        }
        Slog.i(TAG, "Brightness mode has been customized to manual mode");
        return false;
    }

    public boolean getOutdoorAnimationFlag() {
        return this.mHwAmbientLuxFilterAlgo.getOutdoorAnimationFlag();
    }

    public int getCoverModeBrightnessFromLastScreenBrightness() {
        if (this.mData.backSensorCoverModeEnable) {
            return -3;
        }
        int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mCurrentUserId);
        if (this.mWakeupCoverBrightnessEnable && !this.mHwAmbientLuxFilterAlgo.getCoverModeDayEnable()) {
            this.mWakeupCoverBrightnessEnable = false;
        }
        if (brightnessMode != 1 || this.mWakeupCoverBrightnessEnable) {
            return this.mData.coverModeDayBrightness;
        }
        return this.mHwAmbientLuxFilterAlgo.getCoverModeBrightnessFromLastScreenBrightness();
    }

    public void setCoverModeDayEnable(boolean coverModeDayEnable) {
        this.mHwAmbientLuxFilterAlgo.setCoverModeDayEnable(coverModeDayEnable);
    }

    public int setScreenBrightnessMappingtoIndoorMax(int brightness) {
        int brightnessOut;
        if (brightness == -1 || !this.mData.manualMode || brightness > 255) {
            HwBrightnessPgSceneDetection.setQRCodeAppBrightnessNoPowerSaving(false);
            return brightness;
        }
        if (brightness < 4) {
            HwBrightnessPgSceneDetection.setQRCodeAppBrightnessNoPowerSaving(false);
            brightnessOut = 4;
            if (!(HWFLOW == 0 || this.mBrightnessOutForLog == 4)) {
                this.mBrightnessOutForLog = 4;
                String str = TAG;
                Slog.d(str, "mScreenBrightnessOverrideFromWindowManagerMapping brightnessIn=" + brightness + ",brightnessOut=" + 4);
            }
        } else {
            if (this.mData.QRCodeBrightnessminLimit <= 0 || !this.mHwBrightnessPgSceneDetection.isQrCodeAppBoostBrightness(brightness) || this.mIsVideoPlay) {
                HwBrightnessPgSceneDetection.setQRCodeAppBrightnessNoPowerSaving(false);
                brightnessOut = (((brightness - 4) * (this.mData.manualBrightnessMaxLimit - 4)) / 251) + 4;
            } else {
                HwBrightnessPgSceneDetection.setQRCodeAppBrightnessNoPowerSaving(true);
                brightnessOut = this.mData.manualBrightnessMaxLimit;
                if (this.mBrightnessOutForLog != brightnessOut && HWFLOW) {
                    String str2 = TAG;
                    Slog.i(str2, "QrCodeBrightness=" + brightness + "-->brightnessOut=" + brightnessOut);
                }
            }
            if (!(HWFLOW == 0 || brightness == brightnessOut || this.mBrightnessOutForLog == brightnessOut)) {
                this.mBrightnessOutForLog = brightnessOut;
                String str3 = TAG;
                Slog.d(str3, "mScreenBrightnessOverrideFromWindowManagerMapping brightnessIn=" + brightness + ",brightnessOut=" + brightnessOut);
            }
        }
        return brightnessOut;
    }

    public void setManualModeEnableForPg(boolean manualModeEnableForPg) {
    }

    public void setMaxBrightnessFromThermal(int brightness) {
        int mappingBrightness = brightness;
        if (brightness > 0) {
            if (this.mData.thermalModeBrightnessMappingEnable) {
                mappingBrightness = this.mHwBrightnessMapping.getMappingBrightnessForRealNit(brightness);
            }
            this.mMaxBrightnessSetByThermal = mappingBrightness;
        } else {
            this.mMaxBrightnessSetByThermal = 255;
        }
        if (this.mLightSensorEnabled) {
            String str = TAG;
            Slog.i(str, "ThermalMode set auto MaxBrightness=" + brightness + ",mappingBrightness=" + mappingBrightness);
            this.mCallbacks.updateBrightness();
        }
    }

    public void setMaxBrightnessFromCryogenic(int brightness) {
        if (this.mData.cryogenicEnable) {
            int mappingBrightness = brightness;
            if (brightness > 0) {
                if (this.mData.cryogenicModeBrightnessMappingEnable) {
                    mappingBrightness = this.mHwBrightnessMapping.getMappingBrightnessForRealNit(brightness);
                }
                this.mMaxBrightnessSetByCryogenic = mappingBrightness;
            } else {
                this.mMaxBrightnessSetByCryogenic = 255;
            }
            if (this.mLightSensorEnabled) {
                String str = TAG;
                Slog.i(str, "Cryogenic set auto MaxBrightness=" + brightness + ",mMaxBrightnessSetByCryogenic=" + this.mMaxBrightnessSetByCryogenic);
                this.mCallbacks.updateBrightness();
            }
        }
    }

    public boolean getRebootFirstBrightnessAnimationEnable() {
        return this.mData.rebootFirstBrightnessAnimationEnable;
    }

    public int getAdjustLightValByPgMode(int rawLightVal) {
        if (this.mHwBrightnessPgSceneDetection != null) {
            int mPgBrightness = this.mHwBrightnessPgSceneDetection.getAdjustLightValByPgMode(rawLightVal);
            if (this.mData.pgModeBrightnessMappingEnable && rawLightVal > this.mData.manualBrightnessMaxLimit) {
                mPgBrightness = this.mHwBrightnessMapping.getMappingBrightnessForRealNit(mPgBrightness);
                if (HWFLOW) {
                    String str = TAG;
                    Slog.d(str, "PG_POWER_SAVE_MODE auto mPgBrightness=" + mPgBrightness + ",rawLightVal=" + rawLightVal);
                }
            }
            return mPgBrightness;
        }
        Slog.w(TAG, "mHwBrightnessPgSceneDetection=null");
        return rawLightVal;
    }

    public int getPowerSavingBrightness(int brightness) {
        if ((mHwNormalizedScreenAutoBrightnessSpline == null || mHwNormalizedScreenAutoBrightnessSpline.getPersonalizedBrightnessCurveEnable()) && this.mHwBrightnessPowerSavingCurve != null && mHwNormalizedScreenAutoBrightnessSpline != null && mHwNormalizedScreenAutoBrightnessSpline.getPowerSavingBrighnessLineEnable()) {
            return this.mHwBrightnessPowerSavingCurve.getPowerSavingBrightness(brightness);
        }
        return brightness;
    }

    public void setBrightnessNoLimit(int brightness, int time) {
        if (brightness <= 0 || brightness > 255) {
            this.mBrightnessNoLimitSetByApp = -1;
        } else {
            this.mBrightnessNoLimitSetByApp = brightness;
        }
        if (this.mLightSensorEnabled) {
            String str = TAG;
            Slog.i(str, "setBrightnessNoLimit set auto Brightness=" + brightness + ",time=" + time);
            this.mCallbacks.updateBrightness();
        }
    }

    /* access modifiers changed from: protected */
    public boolean setLightSensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mLightSensorEnabled) {
                this.mLightSensorEnabled = true;
                this.mFirstAutoBrightness = true;
                this.mUpdateAutoBrightnessCount = 0;
                this.mLightSensorEnableTime = SystemClock.uptimeMillis();
                this.mLightSensorEnableElapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
                this.mCurrentLightSensorRate = this.mInitialLightSensorRate;
                int i = this.SENSOR_OPTION;
                HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl = this.mHwDualSensorEventListenerImpl;
                if (i == -1) {
                    this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, this.mCurrentLightSensorRate * 1000, this.mHandler);
                } else {
                    int i2 = this.SENSOR_OPTION;
                    HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl2 = this.mHwDualSensorEventListenerImpl;
                    if (i2 == 0) {
                        this.mSensorObserver = new SensorObserver();
                        this.mHwDualSensorEventListenerImpl.attachFrontSensorData(this.mSensorObserver);
                    } else {
                        int i3 = this.SENSOR_OPTION;
                        HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl3 = this.mHwDualSensorEventListenerImpl;
                        if (i3 == 1) {
                            this.mSensorObserver = new SensorObserver();
                            this.mHwDualSensorEventListenerImpl.attachBackSensorData(this.mSensorObserver);
                        } else {
                            int i4 = this.SENSOR_OPTION;
                            HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl4 = this.mHwDualSensorEventListenerImpl;
                            if (i4 == 2) {
                                this.mSensorObserver = new SensorObserver();
                                this.mHwDualSensorEventListenerImpl.attachFusedSensorData(this.mSensorObserver);
                            }
                        }
                    }
                }
                if (this.mWakeupFromSleep) {
                    this.mHandler.sendEmptyMessageAtTime(4, this.mLightSensorEnableTime + 200);
                }
                if (HWFLOW) {
                    String str = TAG;
                    Slog.d(str, "Enable LightSensor at time:mLightSensorEnableTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableElapsedTimeNanos=" + this.mLightSensorEnableElapsedTimeNanos);
                }
                this.mScreenBrightnessBeforeAdj = -1;
                this.mDragFinished = true;
                return true;
            }
        } else if (this.mLightSensorEnabled) {
            this.mLightSensorEnabled = false;
            this.mFirstAutoBrightness = false;
            Slog.i(TAG, "Disable LightSensor starting...");
            this.mRecentLightSamples = 0;
            this.mAmbientLightRingBuffer.clear();
            clearFilterAlgoParas();
            if (NEED_NEW_FILTER_ALGORITHM) {
                this.mAmbientLightRingBufferFilter.clear();
            }
            this.mCurrentLightSensorRate = -1;
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(4);
            int i5 = this.SENSOR_OPTION;
            HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl5 = this.mHwDualSensorEventListenerImpl;
            if (i5 == -1) {
                this.mSensorManager.unregisterListener(this.mLightSensorListener);
            } else {
                int i6 = this.SENSOR_OPTION;
                HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl6 = this.mHwDualSensorEventListenerImpl;
                if (i6 != 0) {
                    int i7 = this.SENSOR_OPTION;
                    HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl7 = this.mHwDualSensorEventListenerImpl;
                    if (i7 != 1) {
                        int i8 = this.SENSOR_OPTION;
                        HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl8 = this.mHwDualSensorEventListenerImpl;
                        if (i8 == 2 && this.mSensorObserver != null) {
                            this.mHwDualSensorEventListenerImpl.detachFusedSensorData(this.mSensorObserver);
                        }
                    } else if (this.mSensorObserver != null) {
                        this.mHwDualSensorEventListenerImpl.detachBackSensorData(this.mSensorObserver);
                    }
                } else if (this.mSensorObserver != null) {
                    this.mHwDualSensorEventListenerImpl.detachFrontSensorData(this.mSensorObserver);
                }
            }
            this.mAmbientLuxValid = !this.mResetAmbientLuxAfterWarmUpConfig;
            if (HWFLOW) {
                String str2 = TAG;
                Slog.d(str2, "Disable LightSensor at time:" + SystemClock.uptimeMillis());
            }
        }
        return false;
    }

    private boolean handleDarkAdaptDetector(float lux) {
        HwNormalizedSpline.DarkAdaptState splineDarkAdaptState;
        if (this.mDarkAdaptDetector == null || this.mCoverStateFast || this.mHwAmbientLuxFilterAlgo.getProximityPositiveEnable()) {
            return false;
        }
        this.mDarkAdaptDetector.updateLux(lux, this.mAmbientLux);
        DarkAdaptDetector.AdaptState newState = this.mDarkAdaptDetector.getState();
        if (this.mDarkAdaptState == newState) {
            return false;
        }
        if (this.mDarkAdaptState == DarkAdaptDetector.AdaptState.UNADAPTED && newState == DarkAdaptDetector.AdaptState.ADAPTING) {
            this.mDarkAdaptDimmingEnable = true;
        } else {
            this.mDarkAdaptDimmingEnable = false;
        }
        this.mDarkAdaptState = newState;
        switch (newState) {
            case UNADAPTED:
                splineDarkAdaptState = HwNormalizedSpline.DarkAdaptState.UNADAPTED;
                break;
            case ADAPTING:
                splineDarkAdaptState = HwNormalizedSpline.DarkAdaptState.ADAPTING;
                break;
            case ADAPTED:
                splineDarkAdaptState = HwNormalizedSpline.DarkAdaptState.ADAPTED;
                break;
            default:
                splineDarkAdaptState = null;
                break;
        }
        mHwNormalizedScreenAutoBrightnessSpline.setDarkAdaptState(splineDarkAdaptState);
        return true;
    }

    private void unlockDarkAdaptLine() {
        if (this.mDarkAdaptDetector != null && this.mHwAmbientLuxFilterAlgo.getOffsetResetEnable()) {
            mHwNormalizedScreenAutoBrightnessSpline.unlockDarkAdaptLine();
        }
    }

    public boolean getDarkAdaptDimmingEnable() {
        return this.mDarkAdaptDetector != null && this.mDarkAdaptDimmingEnable && this.mLightSensorEnabled;
    }

    public void clearDarkAdaptDimmingEnable() {
        this.mDarkAdaptDimmingEnable = false;
    }

    public void getUserDragInfo(Bundle data) {
        boolean isDeltaValid = mHwNormalizedScreenAutoBrightnessSpline.isDeltaValid();
        mHwNormalizedScreenAutoBrightnessSpline.resetUserDragLimitFlag();
        int targetBL = getAutomaticScreenBrightness();
        int i = -3;
        if (this.mMaxBrightnessSetByCryogenic < 255) {
            targetBL = targetBL >= this.mMaxBrightnessSetByCryogenic ? -3 : targetBL;
            String str = TAG;
            Slog.i(str, "mMaxBrightnessSetByCryogenic=" + this.mMaxBrightnessSetByCryogenic);
        }
        if (this.mMaxBrightnessSetByThermal < 255) {
            if (targetBL < this.mMaxBrightnessSetByThermal) {
                i = targetBL;
            }
            targetBL = i;
            String str2 = TAG;
            Slog.i(str2, "mMaxBrightnessSetByThermal=" + this.mMaxBrightnessSetByThermal);
        }
        String str3 = TAG;
        Slog.i(str3, "getUserDragInfo startBL=" + this.mScreenBrightnessBeforeAdj + ", targetBL=" + targetBL);
        data.putBoolean("DeltaValid", isDeltaValid && !this.mDragFinished);
        data.putInt("StartBrightness", this.mScreenBrightnessBeforeAdj);
        data.putInt("EndBrightness", targetBL);
        data.putInt("FilteredAmbientLight", (int) (this.mAmbientLux + 0.5f));
        data.putBoolean("ProximityPositive", this.mProximityPositive);
        this.mDragFinished = true;
    }

    public void setVideoPlayStatus(boolean isVideoPlay) {
        this.mIsVideoPlay = isVideoPlay;
        String str = TAG;
        Slog.d(str, "setVideoPlayStatus, mIsVideoPlay= " + this.mIsVideoPlay);
    }

    public void registerCryogenicProcessor(CryogenicPowerProcessor processor) {
        this.mCryogenicProcessor = processor;
    }

    public float getAutomaticScreenBrightnessAdjustmentNew(int brightness) {
        return MathUtils.constrain(((((float) brightness) * 2.0f) / ((float) this.mData.manualBrightnessMaxLimit)) - 1.0f, -1.0f, 1.0f);
    }

    /* access modifiers changed from: private */
    public void sendLandScapeStateUpdate(boolean enable) {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(5);
        if (enable) {
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(5, (long) this.mData.landScapeModeEnterDelayTime);
        } else {
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(5, (long) this.mData.landScapeModeQuitDelayTime);
        }
        if (HWDEBUG) {
            String str = TAG;
            Slog.i(str, "LandScapeBrightMode MSG_UPDATE_LANDSCAPE mLandScapeModeState=" + this.mLandScapeModeState + ",bTime=" + this.mData.landScapeModeEnterDelayTime + ",dTime=" + this.mData.landScapeModeQuitDelayTime);
        }
    }

    /* access modifiers changed from: private */
    public void updateLandScapeMode(boolean enable) {
        if (this.mHwAmbientLuxFilterAlgo != null) {
            this.mHwAmbientLuxFilterAlgo.updateLandScapeMode(enable);
            if (HWFLOW) {
                String str = TAG;
                Slog.i(str, "LandScapeBrightMode real LandScapeState,ModeState=" + enable);
            }
        }
    }

    private void updateTouchProximityState(boolean touchProximityState) {
        if (this.mHwAmbientLuxFilterAlgo != null) {
            if (HWDEBUG) {
                String str = TAG;
                Slog.i(str, "LandScapeBrightMode touchProximityState=" + touchProximityState + ",bypassTouchProximityResult=" + bypassTouchProximityResult());
            }
            this.mHwAmbientLuxFilterAlgo.updateTouchProximityState(touchProximityState);
        }
    }

    private boolean bypassTouchProximityResult() {
        if (this.mData.landScapeModeUseTouchProximity) {
            return !this.mLandScapeModeState;
        }
        return false;
    }

    public void updateBrightnessModeChangeManualState(boolean enable) {
        if (this.mHwAmbientLuxFilterAlgo != null) {
            this.mHwAmbientLuxFilterAlgo.updateBrightnessModeChangeManualState(enable);
        }
    }

    /* access modifiers changed from: private */
    public void handleBrightnessSettingsChange() {
        int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId);
        if (brightnessMode == 1) {
            handleAutoBrightnessSettingsChange();
        } else if (brightnessMode == 0) {
            handleManualBrightnessSettingsChange();
        }
    }

    private void handleManualBrightnessSettingsChange() {
        int manualBrightnessDB = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", 0, this.mCurrentUserId);
        if (this.mCurrentAutoBrightness != manualBrightnessDB) {
            int autoBrightnessDB = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", 0, this.mCurrentUserId);
            if (autoBrightnessDB != manualBrightnessDB && autoBrightnessDB > 0) {
                Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", manualBrightnessDB, this.mCurrentUserId);
            }
            updateCurrentAutoBrightness(manualBrightnessDB);
            if (HWDEBUG) {
                String str = TAG;
                Slog.i(str, "updateCurrentAutoBrightness from manualBrightnessDB=" + manualBrightnessDB + ",autoBrightnessDB=" + autoBrightnessDB);
            }
            this.mCurrentAutoBrightness = manualBrightnessDB;
        }
    }

    private void handleAutoBrightnessSettingsChange() {
        boolean brightnessChageUpStatus = false;
        int autoBrightnessDB = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", 0, this.mCurrentUserId);
        long time = SystemClock.uptimeMillis();
        if (this.mCurrentAutoBrightness != autoBrightnessDB) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(6);
            if (autoBrightnessDB > this.mCurrentAutoBrightness) {
                brightnessChageUpStatus = true;
            }
            if (autoBrightnessDB == 0 || this.mCurrentAutoBrightness == 0 || brightnessChageUpStatus != this.mBrightnessChageUpStatus || ((autoBrightnessDB > this.mCurrentAutoBrightness && time - this.mSetCurrentAutoBrightnessTime > ((long) this.mData.brightnessChageUpDelayTime)) || (autoBrightnessDB < this.mCurrentAutoBrightness && time - this.mSetCurrentAutoBrightnessTime > ((long) this.mData.brightnessChageDownDelayTime)))) {
                this.mSetCurrentAutoBrightnessTime = time;
                this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessage(6);
                if (HWFLOW && (autoBrightnessDB == 0 || this.mCurrentAutoBrightness == 0)) {
                    String str = TAG;
                    Slog.i(str, "updateCurrentAutoBrightness now,brightness=" + autoBrightnessDB);
                }
            } else {
                this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(6, (long) this.mData.brightnessChageDefaultDelayTime);
            }
            this.mCurrentAutoBrightness = autoBrightnessDB;
            this.mBrightnessChageUpStatus = brightnessChageUpStatus;
        }
    }

    public void setProximitySceneMode(boolean enable) {
        if (this.mData.proximitySceneModeEnable && enable != this.mIsProximitySceneModeOpened) {
            if (HWDEBUG) {
                String str = TAG;
                Slog.i(str, "setProximitySceneMode enable=" + enable + "mData.proximitySceneModeEnable =" + this.mData.proximitySceneModeEnable);
            }
            this.mIsProximitySceneModeOpened = enable;
            setProximitySensorEnabled(enable);
            if (!enable) {
                this.mHwAmbientLuxFilterAlgo.clearProximity();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleDcModeSettingsChange() {
        updateDcMode();
    }

    private void updateDcMode() {
        boolean z = true;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_DC_BRIGHTNESS_DIMMING_SWITCH, 0, this.mCurrentUserId) != 1) {
            z = false;
        }
        this.mDcModeBrightnessEnable = z;
        if (this.mHwAmbientLuxFilterAlgo != null) {
            this.mHwAmbientLuxFilterAlgo.setDcModeBrightnessEnable(this.mDcModeBrightnessEnable);
        }
        if (HWFLOW) {
            Slog.i(TAG, "DcModeBrightnessEnable=" + this.mDcModeBrightnessEnable + ",dcMode=" + dcMode);
        }
    }

    /* access modifiers changed from: private */
    public void updateCurrentAutoBrightness(int brightness) {
        if (this.mHwAmbientLuxFilterAlgo != null) {
            if (HWDEBUG) {
                String str = TAG;
                Slog.d(str, "updateCurrentAutoBrightness realBrightness=" + brightness);
            }
            this.mHwAmbientLuxFilterAlgo.setCurrentAutoBrightness(brightness);
        }
    }

    public boolean getFastDarkenDimmingEnable() {
        int brightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId);
        if (this.mHwAmbientLuxFilterAlgo == null || brightnessMode != 1) {
            return false;
        }
        return this.mHwAmbientLuxFilterAlgo.getFastDarkenDimmingEnable();
    }
}
