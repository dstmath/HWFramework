package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IPowerManager.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.server.display.HwEyeProtectionXmlLoader.Data;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.pgmng.plug.PGSdk;
import com.huawei.pgmng.plug.PGSdk.Sink;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwEyeProtectionControllerImpl extends HwEyeProtectionController {
    private static final String ACTION_TURN_OFF_EYEPROTECTION = "com.android.server.action.ACTION_TURN_OFF_EYEPROTECTION";
    private static final boolean DEBUG = false;
    private static final String EYES_COMFORT_SCHEDULE_SWITCH = "eye_comfort_schedule_switch";
    private static final String KEY_EYES_PROTECTION = "eyes_protection_mode";
    private static final String LCD_PANEL_TYPE_PATH = "/sys/class/graphics/fb0/lcd_model";
    private static final float[] LuxDefaultLevel = new float[]{0.0f, 100.0f, 1000.0f, 3000.0f};
    private static final String SETTINGS_ACTION = "com.android.settings.EyeComfortSettings";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String TAG = "EyeProtectionControllerImpl";
    private static final int mLightSensorRate = 300;
    private static boolean mLoadLibraryFailed;
    private static int mMaxLightCtData = 10000;
    private static int mMinLightCtData = 2000;
    private boolean isCurrentSceneSupportNewColorTemperatureMode = true;
    private boolean isCurrentSceneSupportNewColorTemperatureModeBefore = true;
    private float mAmbientCct = -1.0f;
    private float mAmbientLux = -1.0f;
    private HwEyeProtectionAmbientLuxFilterAlgo mAmbientLuxFilterAlgo;
    private int mBlueLightFilterReal;
    private int mBluelightAnimationTarget;
    private int mBluelightAnimationTimes;
    private int mBluelightBeforeAnimation;
    private boolean mBootCompleted;
    private int mColorBeforeAnimation;
    private int mColorTemperatureCloudy;
    private int mColorTemperatureInDoor;
    private int mColorTemperatureNight;
    private int mColorTemperatureSunny;
    private int mColorTemperatureTarget;
    private int mColorTemperatureTimes = 1;
    private String mConfigFilePath = null;
    private Context mContext;
    private int mCurrentColorTemperature;
    private int mCurrentFilterValue;
    private int mCurrentUserId;
    private DisplayEngineManager mDisplayEngineManager;
    private long mEyeComfortValidValue = 0;
    private boolean mEyeProtectionControlFlag = false;
    private HwEyeProtectionDividedTimeControl mEyeProtectionDividedTimeControl;
    private boolean mEyeProtectionScreenOff = false;
    private int mEyeProtectionScreenOffMode = 0;
    private int mEyeProtectionTempMode = 0;
    private int mEyeScheduleBeginTime;
    private int mEyeScheduleEndTime;
    private int mEyeScheduleSwitchMode;
    private int mEyesProtectionMode;
    private String mFilterConfigFilePath = null;
    private boolean mFilterEnable;
    private Data mFilterParameters;
    private SmartDisplayHandler mHandler;
    private HandlerThread mHandlerThread;
    private HwNormalizedAutomaticColorTemperatureControllerV2 mHwNormalzedAutomaticColorTemperatureControllerV2;
    private int mKidsEyesProtectionMode = 1;
    private ContentObserver mKidsEyesProtectionModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwEyeProtectionControllerImpl.this.mKidsEyesProtectionMode = Global.getInt(HwEyeProtectionControllerImpl.this.mContext.getContentResolver(), Utils.KEY_KIDS_EYE_PROTECTION_MODE, 1);
            Slog.i(HwEyeProtectionControllerImpl.TAG, "eyes protection mode changed in Kids mode. mKidsEyesProtectionMode:" + HwEyeProtectionControllerImpl.this.mKidsEyesProtectionMode);
            HwEyeProtectionControllerImpl.this.updateGlobalSceneState();
        }
    };
    private int mKidsMode;
    private ContentObserver mKidsModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwEyeProtectionControllerImpl.this.mKidsMode = Global.getInt(HwEyeProtectionControllerImpl.this.mContext.getContentResolver(), Utils.KEY_KIDS_MODE, 0);
            Slog.i(HwEyeProtectionControllerImpl.TAG, "Kids mode changed. mKidsMode:" + HwEyeProtectionControllerImpl.this.mKidsMode);
            HwEyeProtectionControllerImpl.this.updateGlobalSceneState();
        }
    };
    private int mLastSceneFlag = 0;
    private String mLastSceneValue;
    private String mLcdPanelName = null;
    private int mLessWarm;
    private Sensor mLightSensor;
    private final SensorEventListener mLightSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            HwEyeProtectionControllerImpl.this.handleLightSensor(SystemClock.uptimeMillis(), event.values[0], event.values[1]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private ArrayList<Float> mLuxLevel = null;
    private int mMoreWarm;
    private PGSdk mPGSdk;
    private ContentObserver mProtectionModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            int tempProtectionMode = HwEyeProtectionControllerImpl.this.mEyesProtectionMode;
            HwEyeProtectionControllerImpl.this.mEyesProtectionMode = System.getIntForUser(HwEyeProtectionControllerImpl.this.mContext.getContentResolver(), "eyes_protection_mode", 0, -2);
            Slog.i(HwEyeProtectionControllerImpl.TAG, "Eyes-Protect mode in Settings changed, mEyesProtectionMode =" + HwEyeProtectionControllerImpl.this.mEyesProtectionMode + ", user =" + -2);
            HwEyeProtectionControllerImpl.this.setValidTimeOnProtectionMode(tempProtectionMode);
            if (HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 3) {
                HwEyeProtectionControllerImpl.this.updateGlobalSceneState();
                return;
            }
            if (tempProtectionMode == 3 && HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 0 && HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.getInDividedTimeFlag()) {
                HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setInDividedTimeFlag(false);
                HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setTimeControlAlarm(HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS, 0);
                HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setTimeControlAlarm(HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS, 1);
            }
            if (tempProtectionMode == 1 && HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 0) {
                if (HwEyeProtectionControllerImpl.this.mEyeScheduleSwitchMode == 1) {
                    HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setInDividedTimeFlag(false);
                    if (HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.isNeedDelay()) {
                        HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setTimeControlAlarm(HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS, 0);
                        HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setTimeControlAlarm(HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS, 1);
                    } else {
                        HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setTimeControlAlarm(0, 0);
                        HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setTimeControlAlarm(0, 1);
                    }
                } else {
                    HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setInDividedTimeFlag(false);
                    HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.cancelTimeControlAlarm(0);
                    HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.cancelTimeControlAlarm(1);
                }
            }
            if (tempProtectionMode == 0 && HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 1 && HwEyeProtectionControllerImpl.this.mEyeScheduleSwitchMode == 1) {
                HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setInDividedTimeFlag(false);
                HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.cancelTimeControlAlarm(0);
                HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.cancelTimeControlAlarm(1);
            }
            HwEyeProtectionControllerImpl.this.updateGlobalSceneState();
        }
    };
    private ContentObserver mScheduleBeginTimeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            int eyeScheduleBeginTime = HwEyeProtectionControllerImpl.this.mEyeScheduleBeginTime;
            HwEyeProtectionControllerImpl.this.mEyeScheduleBeginTime = System.getIntForUser(HwEyeProtectionControllerImpl.this.mContext.getContentResolver(), Utils.KEY_EYE_SCHEDULE_STARTTIME, -1, -2);
            Slog.i(HwEyeProtectionControllerImpl.TAG, "Eyes-schedule begin time changed");
            HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setValidTime(false);
            if (HwEyeProtectionControllerImpl.this.mEyeScheduleBeginTime != -1 && eyeScheduleBeginTime != HwEyeProtectionControllerImpl.this.mEyeScheduleBeginTime) {
                HwEyeProtectionControllerImpl.this.resetTimeControlAlarm();
                if (HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.getInDividedTimeFlag()) {
                    if (HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 0) {
                        HwEyeProtectionControllerImpl.this.setEyeScheduleSwitchToUserMode(3);
                    } else {
                        HwEyeProtectionControllerImpl.this.updateGlobalSceneState();
                    }
                } else if (HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 3) {
                    HwEyeProtectionControllerImpl.this.setEyeScheduleSwitchToUserMode(0);
                } else {
                    HwEyeProtectionControllerImpl.this.updateGlobalSceneState();
                }
            }
        }
    };
    private ContentObserver mScheduleEndTimeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            int eyeScheduleEndTime = HwEyeProtectionControllerImpl.this.mEyeScheduleEndTime;
            HwEyeProtectionControllerImpl.this.mEyeScheduleEndTime = System.getIntForUser(HwEyeProtectionControllerImpl.this.mContext.getContentResolver(), Utils.KEY_EYE_SCHEDULE_ENDTIME, -1, -2);
            Slog.i(HwEyeProtectionControllerImpl.TAG, "Eyes-schedule end time changed");
            HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setValidTime(false);
            if (HwEyeProtectionControllerImpl.this.mEyeScheduleEndTime != -1 && eyeScheduleEndTime != HwEyeProtectionControllerImpl.this.mEyeScheduleEndTime) {
                HwEyeProtectionControllerImpl.this.resetTimeControlAlarm();
                if (HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.getInDividedTimeFlag()) {
                    if (HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 0) {
                        HwEyeProtectionControllerImpl.this.setEyeScheduleSwitchToUserMode(3);
                    } else {
                        HwEyeProtectionControllerImpl.this.updateGlobalSceneState();
                    }
                } else if (HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 3) {
                    HwEyeProtectionControllerImpl.this.setEyeScheduleSwitchToUserMode(0);
                } else {
                    HwEyeProtectionControllerImpl.this.updateGlobalSceneState();
                }
            }
        }
    };
    private ContentObserver mScheduleModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwEyeProtectionControllerImpl.this.mEyeScheduleSwitchMode = System.getIntForUser(HwEyeProtectionControllerImpl.this.mContext.getContentResolver(), "eye_comfort_schedule_switch", 0, -2);
            Slog.i(HwEyeProtectionControllerImpl.TAG, "Eyes-schedule mode in Settings changed, mEyeScheduleSwitchMode =" + HwEyeProtectionControllerImpl.this.mEyeScheduleSwitchMode + ", user =" + -2);
            HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setValidTime(false);
            if (HwEyeProtectionControllerImpl.this.mEyeScheduleSwitchMode == 0) {
                if (HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.getInDividedTimeFlag() && HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 3) {
                    HwEyeProtectionControllerImpl.this.setEyeScheduleSwitchToUserMode(0);
                }
                HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.setInDividedTimeFlag(false);
                HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.cancelTimeControlAlarm(0);
                HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.cancelTimeControlAlarm(1);
            } else {
                HwEyeProtectionControllerImpl.this.resetTimeControlAlarm();
                if (HwEyeProtectionControllerImpl.this.mEyeProtectionDividedTimeControl.getInDividedTimeFlag() && HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 0) {
                    HwEyeProtectionControllerImpl.this.setEyeScheduleSwitchToUserMode(3);
                    return;
                }
            }
            HwEyeProtectionControllerImpl.this.updateGlobalSceneState();
        }
    };
    private ScreenStateReceiver mScreenStateReceiver;
    private SensorManager mSensorManager;
    private boolean mSetBlueFilterDirect = false;
    private ContentObserver mSetColorTempValueObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwEyeProtectionControllerImpl.this.mUserSetColorTempValue = System.getIntForUser(HwEyeProtectionControllerImpl.this.mContext.getContentResolver(), Utils.KEY_SET_COLOR_TEMP, 0, -2);
            Slog.i(HwEyeProtectionControllerImpl.TAG, "Eyes set warm mode in Settings changed, mUserSetColorTempValue =" + HwEyeProtectionControllerImpl.this.mUserSetColorTempValue + ", user =" + -2);
            HwEyeProtectionControllerImpl.this.mAmbientLux = -1.0f;
            if (HwEyeProtectionControllerImpl.this.mEyesProtectionMode != 0) {
                HwEyeProtectionControllerImpl.this.mSetBlueFilterDirect = true;
                HwEyeProtectionControllerImpl.this.setUserColorTemperature();
            }
        }
    };
    private boolean mSetForce3DColorTemp = true;
    private Sink mStateRecognitionListener = new Sink() {
        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            HwEyeProtectionControllerImpl.this.handleStateChangedEvent(stateType, eventType, pid, pkg, uid);
        }
    };
    private boolean mSuperPowerMode = false;
    private boolean mSupportAjustWithCt = false;
    private boolean mSupportDisplayEngine3DColorTemperature;
    private boolean mSupportDisplayEngineEyeProtection;
    private boolean mSupportSceneSwitch = false;
    private int mUserSetColorTempValue;
    private boolean mfirstSceneSwitchOn = true;

    private class ScreenStateReceiver extends BroadcastReceiver {
        public ScreenStateReceiver() {
            IntentFilter userSwitchFilter = new IntentFilter();
            userSwitchFilter.addAction("android.intent.action.USER_SWITCHED");
            HwEyeProtectionControllerImpl.this.mContext.registerReceiverAsUser(this, UserHandle.ALL, userSwitchFilter, null, null);
            IntentFilter bootCompletedFilter = new IntentFilter();
            bootCompletedFilter.addAction("android.intent.action.BOOT_COMPLETED");
            HwEyeProtectionControllerImpl.this.mContext.registerReceiverAsUser(this, UserHandle.ALL, bootCompletedFilter, null, null);
            IntentFilter superPowerFilter = new IntentFilter();
            superPowerFilter.addAction(Utils.ACTION_SUPER_POWERMODE);
            HwEyeProtectionControllerImpl.this.mContext.registerReceiverAsUser(this, UserHandle.ALL, superPowerFilter, null, null);
            IntentFilter timeChageFilter = new IntentFilter();
            timeChageFilter.addAction("android.intent.action.TIME_SET");
            timeChageFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            HwEyeProtectionControllerImpl.this.mContext.registerReceiverAsUser(this, UserHandle.ALL, timeChageFilter, null, null);
            IntentFilter screenOnChangeFilter = new IntentFilter();
            screenOnChangeFilter.addAction("android.intent.action.SCREEN_ON");
            HwEyeProtectionControllerImpl.this.mContext.registerReceiverAsUser(this, UserHandle.ALL, screenOnChangeFilter, null, null);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Slog.i(HwEyeProtectionControllerImpl.TAG, "onReceive intent action = " + intent.getAction());
                if (intent.getAction() != null) {
                    Message message = new Message();
                    if (Utils.ACTION_SUPER_POWERMODE.equals(intent.getAction())) {
                        message.what = 5;
                        if (intent.getBooleanExtra("enable", false)) {
                            message.arg1 = 1;
                        } else {
                            message.arg1 = 0;
                        }
                    } else if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                        message.what = 4;
                        message.arg1 = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    } else if ("android.intent.action.TIME_SET".equals(intent.getAction()) || "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                        message.what = 6;
                    } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                        message.what = 8;
                    } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                        HwEyeProtectionControllerImpl.this.mBootCompleted = true;
                        message.what = 9;
                    }
                    HwEyeProtectionControllerImpl.this.mHandler.sendMessage(message);
                }
            }
        }
    }

    private final class SmartDisplayHandler extends Handler {
        public SmartDisplayHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwEyeProtectionControllerImpl.this.colorTemperatureAnimationTo(HwEyeProtectionControllerImpl.this.mColorTemperatureTarget, 40);
                    return;
                case 1:
                    if (!HwEyeProtectionControllerImpl.this.mSetBlueFilterDirect) {
                        HwEyeProtectionControllerImpl.this.blueLightAnimationTo(HwEyeProtectionControllerImpl.this.mBluelightAnimationTarget, 40);
                        return;
                    }
                    return;
                case 2:
                    if (HwEyeProtectionControllerImpl.this.mAutomaticBrightnessController != null) {
                        HwEyeProtectionControllerImpl.this.mAutomaticBrightnessController.updateAutoBrightness(true);
                        Slog.i(HwEyeProtectionControllerImpl.TAG, "updateAutoBrightness.");
                        return;
                    }
                    return;
                case 3:
                    HwEyeProtectionControllerImpl.this.setBootEyeProtectionControlStatus();
                    return;
                case 4:
                    HwEyeProtectionControllerImpl.this.handleUserSwitch(msg.arg1);
                    return;
                case 5:
                    HwEyeProtectionControllerImpl.this.handleSuperPower(msg.arg1);
                    return;
                case 6:
                    HwEyeProtectionControllerImpl.this.handleTimeAndTimezoneChanged();
                    return;
                case 8:
                    HwEyeProtectionControllerImpl.this.setScreenOffEyeProtection();
                    HwEyeProtectionControllerImpl.this.resetEyeProtectionScreenTurnOffMode();
                    return;
                case 9:
                    if (HwEyeProtectionControllerImpl.this.mEyesProtectionMode == 1) {
                        HwEyeProtectionControllerImpl.this.startStateEvent();
                        return;
                    }
                    return;
                case 10:
                    HwEyeProtectionControllerImpl.this.mScreenStateReceiver = new ScreenStateReceiver();
                    return;
                case 11:
                    HwEyeProtectionControllerImpl.this.initDataState();
                    return;
                default:
                    Slog.e(HwEyeProtectionControllerImpl.TAG, "Invalid message");
                    return;
            }
        }
    }

    private static native void finalize_native();

    private static native void init_native();

    protected native int nativeFilterBlueLight(int i);

    protected native int nativeFilterBlueLight3DNew(int i);

    protected native int nativeGetDisplayFeatureSupported(int i);

    protected native int nativeSetColorTemperatureNew(int i);

    static {
        mLoadLibraryFailed = false;
        try {
            System.loadLibrary("eyeprotection_jni");
            Slog.i(TAG, "libeyeprotection_jni library load!");
        } catch (UnsatisfiedLinkError e) {
            mLoadLibraryFailed = true;
            Slog.w(TAG, "libeyeprotection_jni library not found!");
        }
    }

    protected void finalize() {
        if (!mLoadLibraryFailed) {
            finalize_native();
        }
        try {
            super.finalize();
        } catch (Throwable th) {
        }
    }

    private void handleStateChangedEvent(int stateType, int eventType, int pid, String pkg, int uid) {
        if (stateType == IDisplayEngineService.DE_ACTION_PG_VIDEO_END || stateType == IDisplayEngineService.DE_ACTION_PG_CAMERA_END) {
            this.isCurrentSceneSupportNewColorTemperatureMode = true;
        } else if (eventType == 1) {
            this.isCurrentSceneSupportNewColorTemperatureMode = false;
        } else {
            this.isCurrentSceneSupportNewColorTemperatureMode = true;
        }
        Slog.i(TAG, "result:" + this.isCurrentSceneSupportNewColorTemperatureMode);
    }

    private void startStateEvent() {
        if (this.mSupportSceneSwitch) {
            if (this.mPGSdk == null) {
                this.mPGSdk = PGSdk.getInstance();
            }
            try {
                if (this.mPGSdk != null) {
                    this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
                    this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
                    this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_START);
                    this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_END);
                    this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT);
                    this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_CAMERA_END);
                    this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT);
                } else {
                    Slog.i(TAG, "mPGSdk is null, init failed !!!");
                }
            } catch (RemoteException e) {
                this.mPGSdk = null;
                Slog.i(TAG, "enableStateEvent failed !!!");
            }
            return;
        }
        Slog.i(TAG, "startStateEvent mSupportSceneSwitch:" + this.mSupportSceneSwitch);
    }

    private void stopStateEvent() {
        if (this.mSupportSceneSwitch) {
            if (this.mPGSdk != null) {
                try {
                    this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
                    this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
                    this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_START);
                    this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_END);
                    this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT);
                    this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_CAMERA_END);
                    this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT);
                } catch (RemoteException e) {
                    Slog.i(TAG, "disableStateEvent failed !!!");
                } catch (Throwable th) {
                    this.mPGSdk = null;
                }
                this.mPGSdk = null;
            }
            return;
        }
        Slog.i(TAG, "stopStateEvent mSupportSceneSwitch:" + this.mSupportSceneSwitch);
    }

    private void setValidTimeOnProtectionMode(int tempProtectionMode) {
        if (this.mEyesProtectionMode != 2 && tempProtectionMode != 2) {
            if (tempProtectionMode == 3 && this.mEyesProtectionMode == 0 && this.mEyeProtectionDividedTimeControl.getInDividedTimeFlag()) {
                this.mEyeProtectionDividedTimeControl.setValidTime(true);
            } else if (tempProtectionMode == 1 && this.mEyesProtectionMode == 0 && this.mEyeScheduleSwitchMode == 1 && this.mEyeProtectionDividedTimeControl.isNeedDelay()) {
                this.mEyeProtectionDividedTimeControl.setValidTime(true);
            } else {
                this.mEyeProtectionDividedTimeControl.setValidTime(false);
            }
        }
    }

    private void registerLightSensor() {
        if (this.mSupportDisplayEngineEyeProtection) {
            Slog.i(TAG, "mSupportDisplayEngineEyeProtection=" + this.mSupportDisplayEngineEyeProtection);
            return;
        }
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, 300000, null);
    }

    private void unregisterLightSensor() {
        if (this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
        }
    }

    private void handleLightSensor(long time, float lux, float cct) {
        if (this.mSupportAjustWithCt) {
            handleLightSensorEvent(time, lux, cct);
        } else {
            handleLightSensorEvent(time, lux);
        }
    }

    private void handleLightSensorEvent(long time, float lux) {
        if (this.mfirstSceneSwitchOn && this.mEyeProtectionControlFlag) {
            if (this.mFilterEnable && this.mAmbientLuxFilterAlgo != null) {
                this.mAmbientLuxFilterAlgo.clear();
                lux = this.mAmbientLuxFilterAlgo.updateAmbientLux(lux);
            }
            this.mAmbientLux = lux;
            updateColorTemperature();
            this.mfirstSceneSwitchOn = false;
        } else if (!this.mEyeProtectionControlFlag) {
        } else {
            if (this.mFilterEnable && this.mAmbientLuxFilterAlgo != null) {
                lux = this.mAmbientLuxFilterAlgo.updateAmbientLux(lux);
                if (this.mAmbientLuxFilterAlgo.getNeedToBrighten() || this.mAmbientLuxFilterAlgo.getNeedToDarken()) {
                    this.mAmbientLux = lux;
                    updateColorTemperature();
                }
            } else if (lux > this.mAmbientLux * 1.2f || lux < this.mAmbientLux * 0.8f) {
                this.mAmbientLux = lux;
                updateColorTemperature();
            }
        }
    }

    private void handleLightSensorEvent(long time, float lux, float cct) {
        if (cct < ((float) mMinLightCtData) || cct > ((float) mMaxLightCtData)) {
            Slog.i(TAG, " cct data is abnormal, cct is " + cct);
            return;
        }
        if (this.mfirstSceneSwitchOn && this.mEyeProtectionControlFlag) {
            this.isCurrentSceneSupportNewColorTemperatureModeBefore = this.isCurrentSceneSupportNewColorTemperatureMode;
            this.mHwNormalzedAutomaticColorTemperatureControllerV2.isFirstAmbient();
            this.mHwNormalzedAutomaticColorTemperatureControllerV2.handleLightSensorEvent(time, lux, cct);
            this.mAmbientLux = lux;
            this.mAmbientCct = cct;
            Slog.i(TAG, "First lux is : lux " + lux + " ; cct = " + cct);
            this.mHwNormalzedAutomaticColorTemperatureControllerV2.updateColorTemp(this.mAmbientLux, this.mAmbientCct);
            this.mfirstSceneSwitchOn = false;
        } else if (this.mEyeProtectionControlFlag) {
            Slog.i(TAG, "SensorEvent : lux " + lux + " ; cct = " + cct);
            this.mHwNormalzedAutomaticColorTemperatureControllerV2.handleLightSensorEvent(time, lux, cct);
            if (this.isCurrentSceneSupportNewColorTemperatureModeBefore != this.isCurrentSceneSupportNewColorTemperatureMode && this.mSupportSceneSwitch) {
                this.isCurrentSceneSupportNewColorTemperatureModeBefore = this.isCurrentSceneSupportNewColorTemperatureMode;
                Slog.i(TAG, "isCurrentSceneSupportNewColorTemperatureMode changed : " + this.isCurrentSceneSupportNewColorTemperatureMode);
                if (this.isCurrentSceneSupportNewColorTemperatureMode) {
                    this.mHwNormalzedAutomaticColorTemperatureControllerV2.restore();
                } else {
                    this.mHwNormalzedAutomaticColorTemperatureControllerV2.reset();
                }
            } else if (this.mHwNormalzedAutomaticColorTemperatureControllerV2.needToUpdatePanelCct() && this.isCurrentSceneSupportNewColorTemperatureMode) {
                this.mAmbientLux = lux;
                this.mAmbientCct = cct;
                this.mHwNormalzedAutomaticColorTemperatureControllerV2.updateColorTemp();
            }
        } else {
            Slog.i(TAG, "handleLightSensorEvent: HwNormalzedAutomaticColorTemperatureControllerV2 reset");
            this.mHwNormalzedAutomaticColorTemperatureControllerV2.reset();
        }
    }

    private void updateColorTemperature() {
        if (this.mSupportDisplayEngineEyeProtection || (Utils.isFunctionExist(4) ^ 1) != 0) {
            Slog.i(TAG, "mSupportDisplayEngineEyeProtection=" + this.mSupportDisplayEngineEyeProtection);
            return;
        }
        float luxLevel0 = ((Float) this.mLuxLevel.get(0)).floatValue();
        float luxLevel1 = ((Float) this.mLuxLevel.get(1)).floatValue();
        float luxLevel2 = ((Float) this.mLuxLevel.get(2)).floatValue();
        float luxLevel3 = ((Float) this.mLuxLevel.get(3)).floatValue();
        if (this.mAmbientLux >= luxLevel0 && this.mAmbientLux < luxLevel1) {
            this.mColorTemperatureTarget = this.mColorTemperatureNight;
        }
        if (this.mAmbientLux >= luxLevel1 && this.mAmbientLux < luxLevel2) {
            this.mColorTemperatureTarget = this.mColorTemperatureInDoor;
        }
        if (this.mAmbientLux < 0.0f || (this.mAmbientLux >= luxLevel2 && this.mAmbientLux < luxLevel3)) {
            this.mColorTemperatureTarget = this.mColorTemperatureCloudy;
        }
        if (this.mAmbientLux >= luxLevel3) {
            this.mColorTemperatureTarget = this.mColorTemperatureSunny;
        }
        if (this.mColorTemperatureTarget != this.mCurrentColorTemperature) {
            this.mColorTemperatureTimes = 1;
            this.mColorBeforeAnimation = this.mCurrentColorTemperature;
            colorTemperatureAnimationTo(this.mColorTemperatureTarget, 40);
            Slog.i(TAG, "updateColorTemperature mAmbientLux = " + this.mAmbientLux + ", target =" + this.mColorTemperatureTarget);
        }
    }

    private void setColorTemperatureAccordingToSetting() {
        Slog.i(TAG, "setColorTemperatureAccordingToSetting");
        int operation;
        if (this.mSupportDisplayEngine3DColorTemperature || isDisplayFeatureSupported(1)) {
            Slog.i(TAG, "setColorTemperatureAccordingToSetting new.");
            try {
                String ctNewRGB = System.getStringForUser(this.mContext.getContentResolver(), Utils.COLOR_TEMPERATURE_RGB, -2);
                if (ctNewRGB != null) {
                    List<String> rgbarryList = new ArrayList(Arrays.asList(ctNewRGB.split(",")));
                    float red = Float.valueOf((String) rgbarryList.get(0)).floatValue();
                    float green = Float.valueOf((String) rgbarryList.get(1)).floatValue();
                    float blue = Float.valueOf((String) rgbarryList.get(2)).floatValue();
                    Slog.i(TAG, "ColorTemperature read from setting:" + ctNewRGB + red + green + blue);
                    updateRgbGamma(red, green, blue);
                } else {
                    operation = System.getIntForUser(this.mContext.getContentResolver(), Utils.COLOR_TEMPERATURE, 128, -2);
                    Slog.i(TAG, "ColorTemperature read from old setting:" + operation);
                    setColorTemperature(operation);
                }
            } catch (UnsatisfiedLinkError e) {
                Slog.w(TAG, "ColorTemperature read from setting exception!");
                updateRgbGamma(1.0f, 1.0f, 1.0f);
            }
        } else {
            operation = System.getIntForUser(this.mContext.getContentResolver(), Utils.COLOR_TEMPERATURE, 128, -2);
            Slog.i(TAG, "setColorTemperatureAccordingToSetting old:" + operation);
            setColorTemperature(operation);
        }
    }

    public boolean isDisplayFeatureSupported(int feature) {
        boolean z = false;
        Slog.i(TAG, "isDisplayFeatureSupported feature:" + feature);
        try {
            if (mLoadLibraryFailed) {
                Slog.i(TAG, "Display feature not supported because of library not found!");
                return false;
            }
            if (nativeGetDisplayFeatureSupported(feature) != 0) {
                z = true;
            }
            return z;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "Display feature not supported because of exception!");
            return false;
        }
    }

    private int updateRgbGamma(float red, float green, float blue) {
        Slog.i(TAG, "updateRgbGamma:red=" + red + " green=" + green + " blue=" + blue + "mSupportDisplayEngine3DColorTemperature=" + this.mSupportDisplayEngine3DColorTemperature);
        if (this.mSupportDisplayEngine3DColorTemperature) {
            int[] xccCoef = new int[]{(int) (red * 32768.0f), (int) (green * 32768.0f), (int) (blue * 32768.0f)};
            PersistableBundle bundle = new PersistableBundle();
            bundle.putIntArray("Buffer", xccCoef);
            bundle.putInt("BufferLength", 12);
            int ret = this.mDisplayEngineManager.setData(7, bundle);
            if (ret != 0) {
                Slog.e(TAG, "setData DATA_TYPE_3D_COLORTEMP failed, ret = " + ret);
            }
            return ret;
        }
        try {
            return Stub.asInterface(ServiceManager.getService("power")).updateRgbGamma(red, green, blue);
        } catch (RemoteException e) {
            return -1;
        }
    }

    private int setColorTemperature(int colorTemper) {
        if (this.mSupportDisplayEngineEyeProtection) {
            Slog.i(TAG, "mSupportDisplayEngineEyeProtection=" + this.mSupportDisplayEngineEyeProtection);
            return -1;
        }
        try {
            return Stub.asInterface(ServiceManager.getService("power")).setColorTemperature(colorTemper);
        } catch (RemoteException e) {
            return -1;
        }
    }

    private int setColorTemperatureNew(int colorTemper) {
        if (this.mSupportDisplayEngineEyeProtection) {
            Slog.i(TAG, "mSupportDisplayEngineEyeProtection=" + this.mSupportDisplayEngineEyeProtection);
            return -1;
        }
        try {
            if (!mLoadLibraryFailed) {
                return nativeSetColorTemperatureNew(colorTemper);
            }
            Slog.i(TAG, "nativeSetColorTemperatureNew not valid!");
            return 0;
        } catch (UnsatisfiedLinkError e) {
            Slog.w(TAG, "nativeSetColorTemperatureNew not found!");
            return -1;
        }
    }

    private int filterBlueLight(int value) {
        if (this.mSupportDisplayEngineEyeProtection) {
            Slog.i(TAG, "mSupportDisplayEngineEyeProtection=" + this.mSupportDisplayEngineEyeProtection);
            return -1;
        }
        try {
            if (mLoadLibraryFailed) {
                Slog.i(TAG, "filterBlueLight not valid!");
                return 0;
            }
            Slog.i(TAG, "filterBlueLight value is" + value);
            if (this.mSupportAjustWithCt) {
                return nativeFilterBlueLight3DNew(value);
            }
            return nativeFilterBlueLight(value);
        } catch (UnsatisfiedLinkError e) {
            Slog.w(TAG, "filterBlueLight not found!");
            return -1;
        }
    }

    private void updateBrightness() {
        if (Utils.isFunctionExist(2)) {
            this.mHandler.sendEmptyMessageDelayed(2, 200);
        }
    }

    public void updateGlobalSceneState() {
        updateProtectionControlFlag();
        Slog.i(TAG, "updateGlobalSceneState, mEyeProtectionControlFlag =" + this.mEyeProtectionControlFlag + ", mSupportDisplayEngineEyeProtection=" + this.mSupportDisplayEngineEyeProtection + ", mUserSetColorTempValue=" + this.mUserSetColorTempValue);
        this.mSetBlueFilterDirect = false;
        int ret;
        if (this.mEyeProtectionControlFlag) {
            this.mfirstSceneSwitchOn = true;
            if (this.mSupportDisplayEngineEyeProtection) {
                ret = this.mDisplayEngineManager.setScene(15, 16);
                if (ret != 0) {
                    Slog.e(TAG, "setScene DE_SCENE_EYEPROTECTION DE_ACTION_MODE_ON error:" + ret);
                }
                ret = this.mDisplayEngineManager.setScene(11, this.mUserSetColorTempValue);
                if (ret != 0) {
                    Slog.e(TAG, "setScene DE_SCENE_COLORTEMP mUserSetColorTempValue error: " + ret);
                }
            } else {
                Slog.i(TAG, "updateGlobalSceneState:  mBlueLightFilterReal = " + this.mBlueLightFilterReal + " ; mUserSetColorTempValue = " + this.mUserSetColorTempValue);
                updateBlueLightLevel(this.mBlueLightFilterReal + this.mUserSetColorTempValue);
                registerLightSensor();
            }
            if (this.mBootCompleted) {
                startStateEvent();
            }
        } else {
            this.mfirstSceneSwitchOn = false;
            if (this.mSupportDisplayEngineEyeProtection) {
                ret = this.mDisplayEngineManager.setScene(15, 17);
                if (this.mSupportDisplayEngine3DColorTemperature && this.mSetForce3DColorTemp) {
                    float red = 1.0f;
                    float green = 1.0f;
                    float blue = 1.0f;
                    try {
                        String ctNewRGB = System.getStringForUser(this.mContext.getContentResolver(), Utils.COLOR_TEMPERATURE_RGB, -2);
                        if (ctNewRGB != null) {
                            List<String> rgbarryList = new ArrayList(Arrays.asList(ctNewRGB.split(",")));
                            red = Float.valueOf((String) rgbarryList.get(0)).floatValue();
                            green = Float.valueOf((String) rgbarryList.get(1)).floatValue();
                            blue = Float.valueOf((String) rgbarryList.get(2)).floatValue();
                        } else {
                            Slog.e(TAG, "ColorTemperature read from setting failed, and set default values");
                        }
                        updateRgbGamma(red, green, blue);
                    } catch (UnsatisfiedLinkError e) {
                        Slog.i(TAG, "ColorTemperature read from setting exception:" + 1.0f + ", " + 1.0f + ", " + 1.0f);
                        updateRgbGamma(1.0f, 1.0f, 1.0f);
                    }
                }
                if (ret != 0) {
                    Slog.e(TAG, "setScene DE_SCENE_EYEPROTECTION  DE_ACTION_MODE_OFF error:" + ret);
                }
                this.mSetForce3DColorTemp = false;
            } else {
                updateBlueLightLevel(0);
                this.mAmbientLux = -1.0f;
                updateColorTemperature();
                if (this.mHwNormalzedAutomaticColorTemperatureControllerV2 != null) {
                    Slog.i(TAG, "updateGlobalSceneState:HwNormalzedAutomaticColorTemperatureControllerV2 reset ");
                    this.mHwNormalzedAutomaticColorTemperatureControllerV2.reset();
                }
                unregisterLightSensor();
            }
            stopStateEvent();
            this.isCurrentSceneSupportNewColorTemperatureMode = true;
        }
        updateBrightness();
    }

    private void updateBlueLightLevel(int level) {
        if (Utils.isFunctionExist(1)) {
            this.mBluelightAnimationTarget = level;
            this.mBluelightAnimationTimes = 1;
            this.mBluelightBeforeAnimation = this.mCurrentFilterValue;
            this.mHandler.sendEmptyMessageDelayed(1, 0);
        }
    }

    public void onScreenStateChanged(boolean powerStatus) {
        if (powerStatus && this.mEyeProtectionControlFlag) {
            registerLightSensor();
        } else if (this.mEyeProtectionControlFlag) {
            this.mHandler.removeMessages(0);
            unregisterLightSensor();
        }
    }

    private void setDefaultConfigValue() {
        this.mSupportAjustWithCt = false;
        this.mSupportSceneSwitch = false;
        this.mBlueLightFilterReal = 30;
        this.mColorTemperatureSunny = Utils.DEFAULT_COLOR_TEMPERATURE_SUNNY;
        this.mColorTemperatureCloudy = 127;
        this.mColorTemperatureInDoor = 64;
        this.mColorTemperatureNight = 0;
        if (this.mLuxLevel == null) {
            this.mLuxLevel = new ArrayList();
        } else {
            this.mLuxLevel.clear();
        }
        for (float f : LuxDefaultLevel) {
            this.mLuxLevel.add(new Float(f));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x012e  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0127  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0135  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getConfig() throws IOException {
        Throwable th;
        String version = SystemProperties.get("ro.build.version.emui", null);
        Slog.i(TAG, "HwEyeProtectionControllerImpl getConfig");
        if (TextUtils.isEmpty(version)) {
            Slog.w(TAG, "get ro.build.version.emui failed!");
            return false;
        }
        String[] versionSplited = version.split("EmotionUI_");
        if (versionSplited.length < 2) {
            Slog.w(TAG, "split failed! version = " + version);
            return false;
        }
        if (TextUtils.isEmpty(versionSplited[1])) {
            Slog.w(TAG, "get emuiVersion failed!");
            return false;
        }
        String lcdEyeProtectionConfigFile = "EyeProtectionConfig_" + this.mLcdPanelName + ".xml";
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", new Object[]{emuiVersion, Utils.HW_EYEPROTECTION_CONFIG_FILE}), 0);
        if (xmlFile == null) {
            xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s", new Object[]{Utils.HW_EYEPROTECTION_CONFIG_FILE}), 0);
            if (xmlFile == null) {
                xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", new Object[]{emuiVersion, lcdEyeProtectionConfigFile}), 0);
                if (xmlFile == null) {
                    String xmlPath = String.format("/xml/lcd/%s", new Object[]{lcdEyeProtectionConfigFile});
                    xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
                    if (xmlFile == null) {
                        Slog.w(TAG, "get xmlFile :" + xmlPath + " failed!");
                        return false;
                    }
                }
            }
        }
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(xmlFile);
            try {
                this.mFilterConfigFilePath = xmlFile.getAbsolutePath();
                if (getConfigFromXML(inputStream2)) {
                    this.mConfigFilePath = xmlFile.getAbsolutePath();
                }
                inputStream2.close();
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                return true;
            } catch (FileNotFoundException e) {
                inputStream = inputStream2;
                if (inputStream != null) {
                }
                return false;
            } catch (IOException e2) {
                inputStream = inputStream2;
                if (inputStream != null) {
                }
                return false;
            } catch (Exception e3) {
                inputStream = inputStream2;
                if (inputStream != null) {
                }
                return false;
            } catch (Throwable th2) {
                th = th2;
                inputStream = inputStream2;
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e4) {
            if (inputStream != null) {
                inputStream.close();
            }
            return false;
        } catch (IOException e5) {
            if (inputStream != null) {
                inputStream.close();
            }
            return false;
        } catch (Exception e6) {
            if (inputStream != null) {
                inputStream.close();
            }
            return false;
        } catch (Throwable th3) {
            th = th3;
            if (inputStream != null) {
                inputStream.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x001d A:{Catch:{ XmlPullParserException -> 0x004b, IOException -> 0x006b, NumberFormatException -> 0x0081, Exception -> 0x0097 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getConfigFromXML(InputStream inStream) {
        boolean configGroupLoadStarted = false;
        boolean luxLevelLoadStarted = false;
        boolean loadFinished = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                String name;
                switch (eventType) {
                    case 2:
                        name = parser.getName();
                        if (!name.equals(Utils.HW_EYEPROTECTION_CONFIG_FILE_NAME)) {
                            if (!name.equals("BlueLightFilterReal")) {
                                if (!name.equals("ColorTemperatureSunny")) {
                                    if (!name.equals("ColorTemperatureCloudy")) {
                                        if (!name.equals("ColorTemperatureInDoor")) {
                                            if (!name.equals("ColorTemperatureNight")) {
                                                if (!name.equals("LessWarm")) {
                                                    if (!name.equals("MoreWarm")) {
                                                        if (!name.equals("ColorTemperatureAdjustWithCT")) {
                                                            if (!name.equals("SupportSceneSwitch")) {
                                                                if (!name.equals("MinLightCtData")) {
                                                                    if (!name.equals("MaxLightCtData")) {
                                                                        if (!name.equals("LuxLevel")) {
                                                                            if (!name.equals("FilterEnable")) {
                                                                                if (name.equals("Value") && luxLevelLoadStarted) {
                                                                                    if (this.mLuxLevel == null) {
                                                                                        this.mLuxLevel = new ArrayList();
                                                                                    }
                                                                                    this.mLuxLevel.add(new Float(Float.parseFloat(parser.nextText())));
                                                                                    break;
                                                                                }
                                                                            }
                                                                            this.mFilterEnable = Boolean.parseBoolean(parser.nextText());
                                                                            if (this.mFilterEnable) {
                                                                                this.mFilterParameters = HwEyeProtectionXmlLoader.getData(this.mFilterConfigFilePath);
                                                                                break;
                                                                            }
                                                                        }
                                                                        luxLevelLoadStarted = true;
                                                                        break;
                                                                    }
                                                                    mMaxLightCtData = Integer.parseInt(parser.nextText());
                                                                    Slog.d(TAG, "mMaxLightCtData is:" + mMaxLightCtData);
                                                                    break;
                                                                }
                                                                mMinLightCtData = Integer.parseInt(parser.nextText());
                                                                Slog.d(TAG, "mMinLightCtData is:" + mMinLightCtData);
                                                                break;
                                                            }
                                                            this.mSupportSceneSwitch = Boolean.parseBoolean(parser.nextText());
                                                            Slog.d(TAG, "SupportSceneSwitch is:" + this.mSupportSceneSwitch);
                                                            break;
                                                        }
                                                        Slog.d(TAG, "ColorTemperatureAdjustWithCT exist");
                                                        this.mSupportAjustWithCt = Boolean.parseBoolean(parser.nextText());
                                                        Slog.d(TAG, "ColorTemperatureAdjustWithCT is:" + this.mSupportAjustWithCt);
                                                        break;
                                                    }
                                                    this.mMoreWarm = Integer.parseInt(parser.nextText());
                                                    break;
                                                }
                                                this.mLessWarm = Integer.parseInt(parser.nextText());
                                                break;
                                            }
                                            this.mColorTemperatureNight = Integer.parseInt(parser.nextText());
                                            break;
                                        }
                                        this.mColorTemperatureInDoor = Integer.parseInt(parser.nextText());
                                        break;
                                    }
                                    this.mColorTemperatureCloudy = Integer.parseInt(parser.nextText());
                                    break;
                                }
                                this.mColorTemperatureSunny = Integer.parseInt(parser.nextText());
                                break;
                            }
                            this.mBlueLightFilterReal = Integer.parseInt(parser.nextText());
                            break;
                        }
                        configGroupLoadStarted = true;
                        break;
                        break;
                    case 3:
                        name = parser.getName();
                        if (name.equals(Utils.HW_EYEPROTECTION_CONFIG_FILE_NAME) && configGroupLoadStarted) {
                            loadFinished = true;
                            configGroupLoadStarted = false;
                            break;
                        } else if (name.equals("LuxLevel")) {
                            luxLevelLoadStarted = false;
                            if (this.mLuxLevel != null) {
                                break;
                            }
                            Slog.e(TAG, "no luxlevel  loaded!");
                            return false;
                        }
                        break;
                }
                if (loadFinished) {
                    if (loadFinished) {
                        Slog.i(TAG, "getConfigFromeXML success!");
                        return true;
                    }
                    Slog.e(TAG, "getConfigFromeXML false!");
                    return false;
                }
            }
            if (loadFinished) {
            }
        } catch (XmlPullParserException e) {
        } catch (IOException e2) {
        } catch (NumberFormatException e3) {
        } catch (Exception e4) {
        }
        Slog.e(TAG, "getConfigFromeXML false!");
        return false;
    }

    private String getLcdPanelName() {
        String str = null;
        try {
            str = FileUtils.readTextFile(new File(LCD_PANEL_TYPE_PATH), 0, null).trim().replace(' ', '_');
            Slog.d(TAG, "panelName is:" + str);
            return str;
        } catch (IOException e) {
            Slog.e(TAG, "Error reading lcd panel name", e);
            return str;
        }
    }

    public HwEyeProtectionControllerImpl(Context context, HwNormalizedAutomaticBrightnessController automaticBrightnessController) {
        super(context, automaticBrightnessController);
        this.mContext = context;
        this.mLcdPanelName = getLcdPanelName();
        this.mDisplayEngineManager = new DisplayEngineManager();
        boolean z = this.mDisplayEngineManager.getSupported(17) == 1 || this.mDisplayEngineManager.getSupported(21) == 1;
        this.mSupportDisplayEngineEyeProtection = z;
        if (this.mDisplayEngineManager.getSupported(18) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mSupportDisplayEngine3DColorTemperature = z;
        Slog.i(TAG, "mSupportDisplayEngineEyeProtection=" + this.mSupportDisplayEngineEyeProtection + ", mSupportDisplayEngine3DColorTemperature=" + this.mSupportDisplayEngine3DColorTemperature);
        this.mFilterEnable = false;
        try {
            if (!getConfig()) {
                Slog.e(TAG, "getConfig failed!");
                setDefaultConfigValue();
            }
        } catch (Exception e) {
        }
        if (this.mFilterEnable && this.mFilterParameters != null) {
            this.mAmbientLuxFilterAlgo = new HwEyeProtectionAmbientLuxFilterAlgo(this.mFilterParameters);
        }
        initHwNormalizedAutomaticColorTemperatureControllerV2();
        this.mAmbientLux = -1.0f;
        this.mCurrentFilterValue = 0;
        this.mCurrentColorTemperature = this.mColorTemperatureCloudy;
        this.mColorTemperatureTarget = this.mColorTemperatureCloudy;
        Slog.i(TAG, "HwEyeProtectionControllerImpl");
        if (Utils.isFunctionExist(1) || Utils.isFunctionExist(4) || Utils.isFunctionExist(2)) {
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
            this.mHandler = new SmartDisplayHandler(this.mHandlerThread.getLooper());
            this.mEyeProtectionDividedTimeControl = new HwEyeProtectionDividedTimeControl(context, this);
            this.mHandler.sendEmptyMessage(11);
            this.mHandler.sendEmptyMessage(10);
            this.mHandler.sendEmptyMessage(3);
        }
        try {
            if (mLoadLibraryFailed) {
                Slog.w(TAG, "init_native not valid!");
            } else {
                init_native();
            }
        } catch (UnsatisfiedLinkError e2) {
            Slog.w(TAG, "init_native not found!");
        }
    }

    private void initDataState() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("eyes_protection_mode"), true, this.mProtectionModeObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(Utils.KEY_SET_COLOR_TEMP), true, this.mSetColorTempValueObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("eye_comfort_schedule_switch"), true, this.mScheduleModeObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(Utils.KEY_EYE_SCHEDULE_STARTTIME), true, this.mScheduleBeginTimeObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(Utils.KEY_EYE_SCHEDULE_ENDTIME), true, this.mScheduleEndTimeObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(Utils.KEY_KIDS_MODE), true, this.mKidsModeObserver);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(Utils.KEY_KIDS_EYE_PROTECTION_MODE), true, this.mKidsEyesProtectionModeObserver);
        this.mKidsMode = Global.getInt(this.mContext.getContentResolver(), Utils.KEY_KIDS_MODE, 0);
        this.mKidsEyesProtectionMode = Global.getInt(this.mContext.getContentResolver(), Utils.KEY_KIDS_EYE_PROTECTION_MODE, 1);
        this.mUserSetColorTempValue = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_SET_COLOR_TEMP, 0, -2);
        this.mEyeComfortValidValue = System.getLongForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_COMFORT_VALID, 0, -2);
        this.mEyesProtectionMode = System.getIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 0, -2);
        this.mEyeScheduleSwitchMode = System.getIntForUser(this.mContext.getContentResolver(), "eye_comfort_schedule_switch", 0, -2);
        this.mEyeScheduleBeginTime = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_SCHEDULE_STARTTIME, -1, -2);
        this.mEyeScheduleEndTime = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_SCHEDULE_ENDTIME, -1, -2);
        this.mEyeProtectionDividedTimeControl.setTime(this.mEyeScheduleBeginTime, 0);
        this.mEyeProtectionDividedTimeControl.setTime(this.mEyeScheduleEndTime, 1);
        setDefaultColorTemptureValue();
        this.mEyeProtectionDividedTimeControl.init();
    }

    private void initHwNormalizedAutomaticColorTemperatureControllerV2() {
        if (this.mSupportAjustWithCt) {
            this.mHwNormalzedAutomaticColorTemperatureControllerV2 = new HwNormalizedAutomaticColorTemperatureControllerV2(this.mContext, isDisplayFeatureSupported(1));
            Slog.i(TAG, "Support Adjust With Env Ct");
        }
    }

    private void blueLightAnimationTo(int target, int delayed) {
        this.mHandler.removeMessages(1);
        if (this.mSupportDisplayEngineEyeProtection) {
            Slog.i(TAG, "mSupportDisplayEngineEyeProtection=" + this.mSupportDisplayEngineEyeProtection);
            return;
        }
        int value = target;
        if (this.mBluelightAnimationTarget > this.mCurrentFilterValue) {
            value = (this.mBluelightAnimationTarget * this.mBluelightAnimationTimes) / 20;
            if (value > this.mBluelightAnimationTarget) {
                value = this.mBluelightAnimationTarget;
            }
        } else if (this.mBluelightAnimationTarget < this.mCurrentFilterValue) {
            value = (this.mBluelightBeforeAnimation * (20 - this.mBluelightAnimationTimes)) / 20;
            if (value < this.mBluelightAnimationTarget) {
                value = this.mBluelightAnimationTarget;
            }
        } else {
            Slog.w(TAG, "no need to set blueLightAnimationTo target is" + target);
            return;
        }
        filterBlueLight(value);
        this.mBluelightAnimationTimes++;
        this.mCurrentFilterValue = value;
        if (this.mBluelightAnimationTimes <= 20 && this.mBluelightAnimationTarget != this.mCurrentFilterValue) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, this.mBluelightAnimationTarget, this.mCurrentFilterValue), (long) delayed);
        }
    }

    private void colorTemperatureAnimationTo(int target, int delayed) {
        this.mHandler.removeMessages(0);
        if (this.mSupportDisplayEngineEyeProtection) {
            Slog.i(TAG, "mSupportDisplayEngineEyeProtection=" + this.mSupportDisplayEngineEyeProtection);
            return;
        }
        int value = target;
        if (this.mColorTemperatureTarget > this.mColorBeforeAnimation) {
            value = this.mColorBeforeAnimation + (((this.mColorTemperatureTarget - this.mColorBeforeAnimation) * this.mColorTemperatureTimes) / 20);
            if (value > this.mColorTemperatureTarget) {
                value = this.mColorTemperatureTarget;
            }
        } else if (this.mColorTemperatureTarget < this.mColorBeforeAnimation) {
            value = this.mColorBeforeAnimation - (((this.mColorBeforeAnimation - this.mColorTemperatureTarget) * this.mColorTemperatureTimes) / 20);
            if (value < this.mColorTemperatureTarget) {
                value = this.mColorTemperatureTarget;
            }
        }
        setColorTemperatureNew(value);
        this.mColorTemperatureTimes++;
        this.mCurrentColorTemperature = value;
        if (this.mColorTemperatureTimes <= 20 && this.mColorTemperatureTarget != this.mCurrentColorTemperature) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0, this.mColorTemperatureTarget, this.mCurrentColorTemperature), (long) delayed);
        } else if (this.mAmbientLux < 0.0f) {
            setColorTemperatureAccordingToSetting();
        }
    }

    private void handleUserSwitch(int userId) {
        this.mCurrentUserId = userId;
        Slog.i(TAG, "onReceive ACTION_USER_SWITCHED  mCurrentUserId= " + this.mCurrentUserId);
        setDefaultColorTemptureValue();
        this.mCurrentColorTemperature = this.mColorTemperatureCloudy;
        this.mKidsMode = Global.getInt(this.mContext.getContentResolver(), Utils.KEY_KIDS_MODE, 0);
        this.mKidsEyesProtectionMode = Global.getInt(this.mContext.getContentResolver(), Utils.KEY_KIDS_EYE_PROTECTION_MODE, 1);
        this.mEyesProtectionMode = System.getIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 0, this.mCurrentUserId);
        this.mEyeScheduleSwitchMode = System.getIntForUser(this.mContext.getContentResolver(), "eye_comfort_schedule_switch", 0, this.mCurrentUserId);
        this.mEyeScheduleBeginTime = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_SCHEDULE_STARTTIME, -1, this.mCurrentUserId);
        this.mEyeScheduleEndTime = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_SCHEDULE_ENDTIME, -1, this.mCurrentUserId);
        this.mUserSetColorTempValue = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_SET_COLOR_TEMP, 0, -2);
        this.mEyeProtectionDividedTimeControl.setTime(this.mEyeScheduleBeginTime, 0);
        this.mEyeProtectionDividedTimeControl.setTime(this.mEyeScheduleEndTime, 1);
        Slog.i(TAG, "onReceive  mEyesProtectionMode =" + this.mEyesProtectionMode + ",mEyeScheduleSwitchMode=" + this.mEyeScheduleSwitchMode);
        if ((this.mEyesProtectionMode == 0 && this.mEyeScheduleSwitchMode == 0) || this.mEyesProtectionMode == 1) {
            this.mEyeProtectionDividedTimeControl.setInDividedTimeFlag(false);
            this.mEyeProtectionDividedTimeControl.cancelTimeControlAlarm(0);
            this.mEyeProtectionDividedTimeControl.cancelTimeControlAlarm(1);
            updateGlobalSceneState();
            return;
        }
        if (this.mEyeScheduleSwitchMode == 1) {
            setEyeModeOnSwitchMode();
        }
    }

    private void handleSuperPower(int status) {
        Slog.i(TAG, "onReceive ACTION_SUPER_POWERMODE mEyesProtectionMode =" + this.mEyesProtectionMode + ",status =" + status);
        this.mSuperPowerMode = status == 1;
        if (this.mSuperPowerMode) {
            if (this.mEyesProtectionMode == 1 || this.mEyeScheduleSwitchMode == 1) {
                System.putIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 2, -2);
                this.mEyeProtectionTempMode = this.mEyesProtectionMode;
            }
        } else if (this.mEyesProtectionMode == 2) {
            if (this.mEyeProtectionTempMode == 1) {
                System.putIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 1, -2);
            } else if (this.mEyeScheduleSwitchMode == 1) {
                setEyeModeOnSwitchMode();
            }
            this.mEyeProtectionTempMode = 0;
        }
    }

    private void handleTimeAndTimezoneChanged() {
        this.mEyeScheduleSwitchMode = System.getIntForUser(this.mContext.getContentResolver(), "eye_comfort_schedule_switch", 0, this.mCurrentUserId);
        if (this.mEyeScheduleSwitchMode != 0 && this.mEyesProtectionMode != 1) {
            setEyeModeOnSwitchMode();
        }
    }

    private void setEyeModeOnSwitchMode() {
        boolean isScreenOn = ((PowerManager) this.mContext.getSystemService("power")).isScreenOn();
        this.mEyeProtectionDividedTimeControl.updateDiviedTimeFlag();
        if (!this.mEyeProtectionDividedTimeControl.getInDividedTimeFlag()) {
            if (isScreenOn) {
                if (this.mEyesProtectionMode == 0) {
                    updateGlobalSceneState();
                } else {
                    System.putIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 0, -2);
                }
            }
            resetTimeControlAlarm();
        } else if (eyeComfortTimeIsValid()) {
            this.mEyeProtectionDividedTimeControl.setInDividedTimeFlag(false);
            if (isScreenOn) {
                if (this.mEyesProtectionMode == 0) {
                    updateGlobalSceneState();
                } else {
                    System.putIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 0, -2);
                }
            }
            this.mEyeProtectionDividedTimeControl.setTimeControlAlarm(HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS, 0);
            this.mEyeProtectionDividedTimeControl.setTimeControlAlarm(HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS, 1);
        } else {
            if (isScreenOn) {
                if (this.mEyesProtectionMode == 3) {
                    updateGlobalSceneState();
                } else {
                    System.putIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 3, -2);
                }
            }
            resetTimeControlAlarm();
        }
    }

    protected void updateProtectionControlFlag() {
        boolean flag = false;
        if (this.mKidsMode == 1) {
            if (this.mKidsEyesProtectionMode == 1) {
                flag = true;
            } else {
                flag = false;
            }
            Slog.d(TAG, "updateProtectionControlFlag mKidsMode =" + this.mKidsMode + ",mKidsEyesProtectionMode=" + this.mKidsEyesProtectionMode);
        } else {
            if (this.mEyesProtectionMode == 1) {
                flag = true;
            }
            if (this.mEyeProtectionDividedTimeControl.getInDividedTimeFlag()) {
                flag = true;
            }
            if (this.mSuperPowerMode) {
                flag = false;
            }
            Slog.d(TAG, "updateProtectionControlFlag mEyesProtectionMode =" + this.mEyesProtectionMode + ",inDividedTimeFlag=" + this.mEyeProtectionDividedTimeControl.getInDividedTimeFlag());
        }
        this.mEyeProtectionControlFlag = flag;
        this.mAutomaticBrightnessController.setSplineEyeProtectionControlFlag(flag);
    }

    protected void resetDividedTimeStatus(boolean flag, boolean beginTimeNeedSetAlarm, boolean endTimeNeedSetAlarm) {
        Slog.d(TAG, "resetDividedTimeStatus flag=" + flag + ",beginTimeNeedSetAlarm= " + beginTimeNeedSetAlarm + ",endTimeNeedSetAlarm=" + endTimeNeedSetAlarm);
        if (flag) {
            this.mEyeScheduleBeginTime = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_SCHEDULE_STARTTIME, -1, -2);
            this.mEyeScheduleEndTime = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_SCHEDULE_ENDTIME, -1, -2);
            if (this.mEyeScheduleBeginTime < 0 || this.mEyeScheduleEndTime < 0) {
                return;
            }
        }
        if (beginTimeNeedSetAlarm) {
            this.mEyeProtectionDividedTimeControl.setTime(this.mEyeScheduleBeginTime, 0);
            this.mEyeProtectionDividedTimeControl.updateDiviedTimeFlag();
            this.mEyeProtectionDividedTimeControl.setTimeControlAlarm(0, 0);
        }
        if (endTimeNeedSetAlarm) {
            this.mEyeProtectionDividedTimeControl.setTime(this.mEyeScheduleEndTime, 1);
            this.mEyeProtectionDividedTimeControl.updateDiviedTimeFlag();
            this.mEyeProtectionDividedTimeControl.setTimeControlAlarm(0, 1);
        }
    }

    protected void resetTimeControlAlarm() {
        Slog.d(TAG, "resetTimeControlAlarm");
        this.mEyeScheduleBeginTime = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_SCHEDULE_STARTTIME, -1, -2);
        this.mEyeScheduleEndTime = System.getIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_SCHEDULE_ENDTIME, -1, -2);
        if (this.mEyeScheduleBeginTime >= 0 && this.mEyeScheduleEndTime >= 0 && this.mEyesProtectionMode != 1 && this.mEyeScheduleSwitchMode != 0) {
            this.mEyeProtectionDividedTimeControl.setTime(this.mEyeScheduleBeginTime, 0);
            this.mEyeProtectionDividedTimeControl.setTime(this.mEyeScheduleEndTime, 1);
            Slog.d(TAG, "resetDividedTimeStatus mEyeScheduleBeginTime =" + this.mEyeScheduleBeginTime + ",mEyeScheduleEndTime=" + this.mEyeScheduleEndTime);
            this.mEyeProtectionDividedTimeControl.reSetTimeControlAlarm();
        }
    }

    private void setBootEyeProtectionControlStatus() {
        Slog.i(TAG, "setBootEyeProtectionControlStatus ");
        if (this.mKidsMode == 1) {
            updateGlobalSceneState();
        } else if (this.mEyesProtectionMode == 1) {
            updateGlobalSceneState();
        } else {
            if (this.mEyeScheduleSwitchMode == 1) {
                setEyeModeOnSwitchMode();
            } else if (this.mEyesProtectionMode == 2) {
                System.putIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 1, -2);
            }
        }
    }

    public void setEyeScheduleSwitchToUserMode(int type) {
        Slog.i(TAG, "setEyeScheduleSwitchToUserMode type is " + type);
        if (!this.mSuperPowerMode) {
            System.putIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", type, -2);
        }
    }

    private void setDefaultColorTemptureValue() {
        if (this.mLessWarm != 0 || this.mMoreWarm != 0) {
            System.putIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_COMFORT_LESSWARM, this.mLessWarm, -2);
            System.putIntForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_COMFORT_MOREWARM, this.mMoreWarm, -2);
        }
    }

    public void setEyeProtectionScreenTurnOffMode(int mode) {
        this.mEyeProtectionScreenOff = true;
        this.mEyeProtectionScreenOffMode = mode;
    }

    private void resetEyeProtectionScreenTurnOffMode() {
        this.mEyeProtectionScreenOff = false;
        this.mEyeProtectionScreenOffMode = 0;
    }

    private void setScreenOffEyeProtection() {
        if (this.mEyeProtectionScreenOff && !this.mSuperPowerMode) {
            Slog.i(TAG, "setScreenOffEyeProtection mEyeProtectionScreenOffMode =" + this.mEyeProtectionScreenOffMode);
            if (this.mEyeProtectionScreenOffMode == 2) {
                this.mEyeProtectionDividedTimeControl.setInDividedTimeFlag(true);
                System.putIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 3, -2);
            } else if (this.mEyeProtectionScreenOffMode == 1) {
                this.mEyeProtectionDividedTimeControl.setInDividedTimeFlag(false);
                System.putIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 0, -2);
            }
        }
    }

    private boolean eyeComfortTimeIsValid() {
        this.mEyeComfortValidValue = System.getLongForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_COMFORT_VALID, 0, -2);
        Slog.i(TAG, "eyeComfortTimeIsValid mEyeComfortValidValue =" + this.mEyeComfortValidValue);
        return this.mEyeProtectionDividedTimeControl.testTimeIsValid(this.mEyeComfortValidValue);
    }

    private int setUserColorTemperature() {
        int i = -1;
        if (this.mSupportDisplayEngineEyeProtection) {
            int ret = this.mDisplayEngineManager.setScene(11, this.mUserSetColorTempValue);
            if (ret != 0) {
                Slog.e(TAG, "setScene DE_SCENE_COLORTEMP: " + this.mUserSetColorTempValue + ", ret=" + ret);
            }
            if (ret == 0) {
                i = 1;
            }
            return i;
        }
        try {
            if (mLoadLibraryFailed) {
                Slog.i(TAG, "setUserColorTemperature not valid!");
                return 0;
            }
            this.mCurrentFilterValue = this.mBlueLightFilterReal + this.mUserSetColorTempValue;
            if (this.mSupportAjustWithCt) {
                return nativeFilterBlueLight3DNew(this.mCurrentFilterValue);
            }
            return nativeFilterBlueLight(this.mCurrentFilterValue);
        } catch (UnsatisfiedLinkError e) {
            Slog.w(TAG, "setUserColorTemperature not found!");
            return -1;
        }
    }
}
