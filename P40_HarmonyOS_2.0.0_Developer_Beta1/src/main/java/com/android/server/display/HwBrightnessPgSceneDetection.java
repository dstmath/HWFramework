package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.gesture.DefaultGestureNavConst;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.displayengine.IDisplayEngineService;
import java.util.ArrayList;
import java.util.List;

public class HwBrightnessPgSceneDetection {
    private static final int FAILED_RETURN_VALUE = -1;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final Object LOCK = new Object();
    private static final int LOW_LIGHT_MAX = 55;
    private static final int LOW_LIGHT_MIN = 20;
    private static final int MAX_DEFAULT_BRIGHTNESS = 255;
    private static final int MIN_DEFAULT_BRIGHTNESS = 4;
    private static final int MSG_REG_POWER_MODE_LISTENER = 2;
    private static final int MSG_UPDATE_PGBRIGHTNESS = 1;
    private static final int PG_DEFAULT_MODE = 2;
    private static final String PG_EXTREME_MODE_ACTION = "huawei.intent.action.PG_EXTREME_MODE_ENABLE_ACTION";
    private static final int PG_POWER_SAVE_MODE = 1;
    private static final int PG_SUPER_POWER_SAVE_MODE = 4;
    private static final int POWERKIT_DEFAULT_VALUE = 10000;
    private static final String POWER_MODE_CHANGED_ACTION = "huawei.intent.action.POWER_MODE_CHANGED_ACTION";
    private static final int POWER_MODE_MIN_LIGHT_VAL = 25;
    private static final String POWER_MODE_PROP = "persist.sys.smart_power";
    private static final int POWER_SAVE_MODE_BRIGHTNESS_RATIO = SystemProperties.getInt("ro.powersavemode.backlight_ratio", 56);
    private static final int POWER_SAVE_MODE_DEFAULT_BRIGHTNESS_RATIO = 56;
    private static final int POWER_SAVE_MODE_MAX_BRIGHTNESS_RATIO = 100;
    private static final float POWER_SAVE_MODE_RATIO = 100.0f;
    private static final float ROUND_UP_VALUE = 0.5f;
    private static final int SUPER_MODE_MAX_LIGHT_VAL = 35;
    private static final String TAG = "HwBrightnessPgSceneDetection";
    private static int sBrightnessRatioForPowerSavingMode;
    private static boolean sIsPowerSavingKeepQrCodeAppBrightness = false;
    private static int sLowBrightnessPowerSavingMode;
    private static int sQrCodeAppBrightnessByPgMode = -1;
    private int mAppType = -1;
    private List<Integer> mBacklightAppList = null;
    private HwBrightnessPgSceneDetectionCallbacks mCallbacks;
    private final Context mContext;
    private Handler mHandler = new Handler() {
        /* class com.android.server.display.HwBrightnessPgSceneDetection.AnonymousClass3 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwBrightnessPgSceneDetection.this.mCallbacks.updateStateRecognition(HwBrightnessPgSceneDetection.this.mIsShouldUpdateBlight, HwBrightnessPgSceneDetection.this.mAppType);
            } else if (i != 2) {
                Slog.e(HwBrightnessPgSceneDetection.TAG, "Invalid message");
            } else {
                HwBrightnessPgSceneDetection.this.registerPowerModeChangedListener();
            }
        }
    };
    private boolean mIsPgBrightnessListenerRegisted = false;
    private boolean mIsShouldUpdateBlight;
    private BroadcastReceiver mPgModeChangedBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.display.HwBrightnessPgSceneDetection.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.e(HwBrightnessPgSceneDetection.TAG, "onReceive() intent is NULL!");
                return;
            }
            String action = intent.getAction();
            if (HwBrightnessPgSceneDetection.POWER_MODE_CHANGED_ACTION.equals(action)) {
                HwBrightnessPgSceneDetection.this.sendUpdatePgBrightness(false);
            }
            if (HwBrightnessPgSceneDetection.PG_EXTREME_MODE_ACTION.equals(action)) {
                HwBrightnessPgSceneDetection.this.sendUpdatePgBrightness(false);
            }
        }
    };
    private int mPgSceneDetectionBrightenDelayTime = 500;
    private int mPgSceneDetectionDarkenDelayTime = DefaultGestureNavConst.CHECK_AFT_TIMEOUT;
    private String mPkgName = null;
    private PowerKit mPowerKit = null;
    private PowerKit.Sink mStateRecognitionListener = new PowerKit.Sink() {
        /* class com.android.server.display.HwBrightnessPgSceneDetection.AnonymousClass2 */

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            int appType = -1;
            boolean z = true;
            if (eventType == 1) {
                try {
                    appType = HwBrightnessPgSceneDetection.this.mPowerKit.getPkgType(HwBrightnessPgSceneDetection.this.mContext, pkg);
                    HwBrightnessPgSceneDetection.this.mAppType = appType;
                } catch (RemoteException e) {
                    Slog.w(HwBrightnessPgSceneDetection.TAG, "getPkgType failed!");
                }
                boolean isPdtCfgAdjApp = pkg == null ? false : PowerKit.isApkShouldAdjBackLight(pkg);
                boolean isPowerSavingKeepBrightnessAppEnable = HwBrightnessPgSceneDetection.getPowerSavingKeepBrightnessAppEnable(pkg);
                boolean isExempStateType = stateType == 10015;
                HwBrightnessPgSceneDetection.this.mPkgName = pkg;
                if (HwBrightnessPgSceneDetection.HWFLOW) {
                    Slog.i(HwBrightnessPgSceneDetection.TAG, "in onStateChanged, pkg=" + pkg + ",appType=" + appType + ",isPdtCfgAdjApp=" + isPdtCfgAdjApp + ",isPowerSavingKeepBrightnessAppEnable=" + isPowerSavingKeepBrightnessAppEnable);
                }
                if (pkg != null && HwBrightnessPgSceneDetection.this.mBacklightAppList != null) {
                    boolean isShouldUpdateBlight = !HwBrightnessPgSceneDetection.this.mBacklightAppList.contains(Integer.valueOf(appType)) || isPdtCfgAdjApp;
                    if (isPowerSavingKeepBrightnessAppEnable && isShouldUpdateBlight) {
                        isShouldUpdateBlight = false;
                    }
                    HwBrightnessPgSceneDetection hwBrightnessPgSceneDetection = HwBrightnessPgSceneDetection.this;
                    if (!isShouldUpdateBlight || isExempStateType) {
                        z = false;
                    }
                    hwBrightnessPgSceneDetection.mIsShouldUpdateBlight = z;
                    HwBrightnessPgSceneDetection hwBrightnessPgSceneDetection2 = HwBrightnessPgSceneDetection.this;
                    hwBrightnessPgSceneDetection2.sendUpdatePgBrightness(hwBrightnessPgSceneDetection2.mIsShouldUpdateBlight);
                }
            }
        }
    };

    public interface HwBrightnessPgSceneDetectionCallbacks {
        void updateStateRecognition(boolean z, int i);
    }

    static {
        int i = POWER_SAVE_MODE_BRIGHTNESS_RATIO;
        sBrightnessRatioForPowerSavingMode = i;
        sLowBrightnessPowerSavingMode = (int) (((float) (i * 55)) / POWER_SAVE_MODE_RATIO);
    }

    public HwBrightnessPgSceneDetection(HwBrightnessPgSceneDetectionCallbacks callbacks, int pgSceneDetectionDarkenDelayTime, int pgSceneDetectionBrightenDelayTime, Context context) {
        this.mCallbacks = callbacks;
        this.mContext = context;
        parsePowerSavingAppType(SystemProperties.get("ro.blight.exempt_app_type", ""));
        this.mHandler.sendEmptyMessage(2);
        this.mPgSceneDetectionDarkenDelayTime = pgSceneDetectionDarkenDelayTime;
        this.mPgSceneDetectionBrightenDelayTime = pgSceneDetectionBrightenDelayTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerPowerModeChangedListener() {
        IntentFilter intentFilter = new IntentFilter(POWER_MODE_CHANGED_ACTION);
        intentFilter.addAction(PG_EXTREME_MODE_ACTION);
        this.mContext.registerReceiver(this.mPgModeChangedBroadcastReceiver, intentFilter);
    }

    static int getAdjsutLightVal(int rawLight) {
        if (rawLight >= 20 && rawLight <= 55) {
            return (((rawLight - 20) * (sLowBrightnessPowerSavingMode - 20)) / 35) + 20;
        }
        if (rawLight > 55) {
            return (int) ((((float) (sBrightnessRatioForPowerSavingMode * rawLight)) / POWER_SAVE_MODE_RATIO) + 0.5f);
        }
        return rawLight;
    }

    public float getPgPowerModeRatio() {
        return ((float) sBrightnessRatioForPowerSavingMode) / POWER_SAVE_MODE_RATIO;
    }

    public void updateBrightnessRatioFromBattery(int brightnessRatio) {
        sBrightnessRatioForPowerSavingMode = brightnessRatio;
        sLowBrightnessPowerSavingMode = (int) (((float) (brightnessRatio * 55)) / POWER_SAVE_MODE_RATIO);
        if (sBrightnessRatioForPowerSavingMode >= 100) {
            Slog.w(TAG, "updateBrightnessRatioFromBattery sBrightnessRatioForPowerSavingMode=" + sBrightnessRatioForPowerSavingMode + "-->MaxRatio=100");
            sBrightnessRatioForPowerSavingMode = 100;
        }
        if (sLowBrightnessPowerSavingMode <= 20) {
            Slog.w(TAG, "updateBrightnessRatioFromBattery sLowBrightnessPowerSavingMode=" + sLowBrightnessPowerSavingMode + "-->LOW_LIGHT_MIN=20");
            sLowBrightnessPowerSavingMode = 20;
        }
        if (HWFLOW) {
            Slog.i(TAG, "updateBrightnessRatioFromBattery updateBrightnessRatioFromBattery ratio=" + brightnessRatio + ",sLowBrightnessPowerSavingMode=" + sLowBrightnessPowerSavingMode);
        }
    }

    /* access modifiers changed from: package-private */
    public int getPgPowerMode() {
        return SystemProperties.getInt(POWER_MODE_PROP, 2);
    }

    /* access modifiers changed from: package-private */
    public int getPowerSaveModeLightValFromRaw(int rawLightVal) {
        int retRatioVal = getAdjsutLightVal(rawLightVal);
        if (retRatioVal < 4) {
            retRatioVal = 4;
            Slog.w(TAG, "warning retRatioVal < min,retRatioVal=4");
        }
        if (retRatioVal <= 255) {
            return retRatioVal;
        }
        Slog.w(TAG, "warning retRatioVal > max,retRatioVal=255");
        return 255;
    }

    /* access modifiers changed from: package-private */
    public int getSuperPowerSaveModeArbLightVal(int rawLightVal) {
        int calRawVal = rawLightVal > 0 ? rawLightVal : 25;
        if (calRawVal < 35) {
            return calRawVal;
        }
        return 35;
    }

    public static void setQrCodeAppBrightnessNoPowerSaving(boolean isKeepEnable) {
        synchronized (LOCK) {
            if (isKeepEnable != sIsPowerSavingKeepQrCodeAppBrightness) {
                if (HWFLOW) {
                    Slog.i(TAG, "sIsPowerSavingKeepQrCodeAppBrightness=" + sIsPowerSavingKeepQrCodeAppBrightness + "-->isKeepEnable=" + isKeepEnable);
                }
                sIsPowerSavingKeepQrCodeAppBrightness = isKeepEnable;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getAdjustLightValByPgMode(int rawLightVal) {
        int retVal = rawLightVal;
        int nowPgMode = getPgPowerMode();
        if (nowPgMode == 1) {
            synchronized (LOCK) {
                retVal = updatePowerSaveModeLightVal(rawLightVal, nowPgMode);
            }
        }
        if (nowPgMode == 4) {
            retVal = getSuperPowerSaveModeArbLightVal(rawLightVal);
            if (HWFLOW) {
                Slog.i(TAG, "PG_SUPER_POWER_SAVE_MODE,nowPgMode=" + nowPgMode + ",rawLightVal=" + rawLightVal + ",retVal=" + retVal);
            }
        }
        return retVal;
    }

    private int updatePowerSaveModeLightVal(int rawLightVal, int nowPgMode) {
        if (!sIsPowerSavingKeepQrCodeAppBrightness) {
            sQrCodeAppBrightnessByPgMode = -1;
            int retVal = getPowerSaveModeLightValFromRaw(rawLightVal);
            if (HWFLOW) {
                Slog.i(TAG, "PG_POWER_SAVE_MODE,nowPgMode" + nowPgMode + ",rawLightVal=" + rawLightVal + ",retVal=" + retVal);
            }
            return retVal;
        }
        if (sQrCodeAppBrightnessByPgMode != rawLightVal) {
            if (HWFLOW) {
                Slog.i(TAG, "PG_POWER_SAVE_MODE, QRCodeAppBrightnessNoPowerSaving,rawLightVal=" + rawLightVal);
            }
            sQrCodeAppBrightnessByPgMode = rawLightVal;
        }
        return rawLightVal;
    }

    private void parsePowerSavingAppType(String exemptAppType) {
        String[] powerSavingAppType;
        if (exemptAppType == null || exemptAppType.isEmpty()) {
            Slog.w(TAG, "exemptAppType == null");
            return;
        }
        Slog.i(TAG, "PowerSaving exemptAppType=" + exemptAppType);
        List<Integer> list = this.mBacklightAppList;
        if (list != null) {
            list.clear();
        } else {
            this.mBacklightAppList = new ArrayList();
        }
        for (String str : exemptAppType.split(",")) {
            try {
                this.mBacklightAppList.add(Integer.valueOf(Integer.parseInt(str)));
            } catch (NumberFormatException e) {
                this.mBacklightAppList.clear();
                Slog.w(TAG, "parse mBacklightAppList error");
                return;
            }
        }
    }

    static boolean getPowerSavingKeepBrightnessAppEnable(String pkgName) {
        return HwPgSceneDetectionAppName.getPowerSavingKeepBrightnessAppEnable(pkgName);
    }

    public boolean isQrCodeAppBoostBrightness(int brightness) {
        return HwPgSceneDetectionAppName.isQrCodeAppBoostBrightness(this.mPkgName, brightness);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void sendUpdatePgBrightness(boolean isShouldUpdateBackLight) {
        this.mHandler.removeMessages(1);
        if (isShouldUpdateBackLight) {
            this.mHandler.sendEmptyMessageDelayed(1, (long) this.mPgSceneDetectionDarkenDelayTime);
        } else {
            this.mHandler.sendEmptyMessageDelayed(1, (long) this.mPgSceneDetectionBrightenDelayTime);
        }
    }

    public boolean registerPgRecognitionListener(Context context) {
        if (!this.mIsPgBrightnessListenerRegisted) {
            this.mPowerKit = PowerKit.getInstance();
            if (this.mPowerKit != null) {
                this.mIsPgBrightnessListenerRegisted = registerPowerKitListener();
            }
        }
        return this.mIsPgBrightnessListenerRegisted;
    }

    private boolean registerPowerKitListener() {
        PowerKit powerKit = this.mPowerKit;
        if (powerKit == null) {
            return false;
        }
        try {
            powerKit.enableStateEvent(this.mStateRecognitionListener, 10000);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_EBOOK_FRONT);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_MMS_FRONT);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_START);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_END);
            this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_CAMERA_END);
            return true;
        } catch (RemoteException e) {
            this.mPowerKit = null;
            Slog.e(TAG, "mPowerKit registerSink && enableStateEvent happend RemoteException ");
            return false;
        } catch (NullPointerException e2) {
            this.mPowerKit = null;
            Slog.e(TAG, "mPowerKit registerSink && enableStateEvent happend NullPointerException ");
            return false;
        }
    }

    public void unregisterPgRecognitionListener(Context context) {
        if (this.mIsPgBrightnessListenerRegisted) {
            this.mIsPgBrightnessListenerRegisted = !unregisterPowerKitListener();
        }
    }

    private boolean unregisterPowerKitListener() {
        PowerKit.Sink sink;
        PowerKit powerKit = this.mPowerKit;
        if (powerKit == null || (sink = this.mStateRecognitionListener) == null) {
            return true;
        }
        try {
            powerKit.disableStateEvent(sink, 10000);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_EBOOK_FRONT);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_MMS_FRONT);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_START);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_END);
            this.mPowerKit.disableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_CAMERA_END);
            return true;
        } catch (RemoteException e) {
            Slog.e(TAG, "callPG unRegisterListener happend RemoteException ");
            return false;
        }
    }

    public boolean getPgRecognitionListenerRegisted() {
        return this.mIsPgBrightnessListenerRegisted;
    }
}
