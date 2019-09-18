package com.android.server.display;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.BackLightCommonData;
import com.android.server.display.BackLightMonitorManager;
import com.android.server.display.DisplayEffectMonitor;
import com.huawei.dubai.DubaiConstants;
import java.util.List;

class BrightnessStateMonitor implements DisplayEffectMonitor.MonitorModule {
    private static final int HOURS_PER_DAY = 24;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final String PARAM_BRIGHTNESS = "brightness";
    private static final String PARAM_CONFIDENCE = "confidence";
    private static final String PARAM_IS_THERMAL_LIMITED = "isLimited";
    private static final String PARAM_POWER_STATE = "powerState";
    private static final String PARAM_SCENE = "scene";
    private static final String PARAM_USER_SCENE = "userScene";
    private static final long STATE_TIMER_FIRST_DELAY_MILLIS = 60000;
    private static final long STATE_TIMER_PERIOD_MILLIS = 600000;
    private static final long STATE_UPLOAD_MIN_INTERVAL_MILLIS = 599000;
    private static final String TAG = "BrightnessStateMonitor";
    private static final String TYPE_ALGO_DISCOUNT_BRIGHTNESS = "algoDiscountBrightness";
    private static final String TYPE_POWER_STATE_UPDATE = "powerStateUpdate";
    private static final String TYPE_SCENE_RECOGNITION = "sceneRecognition";
    private static final String TYPE_STATE_TIMER = "stateTimer";
    private static final String TYPE_THERMAL_LIMIT = "thermalLimit";
    private static final String TYPE_USER_SCENE_MISRECOGNITION = "userSceneMisrecognition";
    private static final int USER_SCENE_MISRECOGNITION_TIMES_LIMIT_PER_DAY = 10;
    private final BackLightCommonData mCommonData;
    private final boolean mDebugMode;
    private int mHourCount;
    private final BackLightMonitorManager mManager;
    private final DisplayEffectMonitor mMonitor;
    private PowerState mPowerState = PowerState.OFF;
    private BackLightCommonData.Scene mScene;
    private long mStateUploadTime;
    private int mUserSceneMisrecognitionTimes;

    private enum PowerState {
        OFF,
        ON,
        DIM,
        VR
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public BrightnessStateMonitor(DisplayEffectMonitor monitor, BackLightMonitorManager manager) {
        this.mMonitor = monitor;
        this.mManager = manager;
        this.mCommonData = manager.getBackLightCommonData();
        this.mDebugMode = SystemProperties.getBoolean("persist.display.monitor.debug", false);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public boolean isParamOwner(String paramType) {
        char c;
        switch (paramType.hashCode()) {
            case -1382856991:
                if (paramType.equals(TYPE_USER_SCENE_MISRECOGNITION)) {
                    c = 5;
                    break;
                }
            case -663211403:
                if (paramType.equals(TYPE_POWER_STATE_UPDATE)) {
                    c = 0;
                    break;
                }
            case -238058188:
                if (paramType.equals(TYPE_STATE_TIMER)) {
                    c = 2;
                    break;
                }
            case 871202699:
                if (paramType.equals(TYPE_SCENE_RECOGNITION)) {
                    c = 3;
                    break;
                }
            case 1567901733:
                if (paramType.equals(TYPE_ALGO_DISCOUNT_BRIGHTNESS)) {
                    c = 4;
                    break;
                }
            case 1671621220:
                if (paramType.equals(TYPE_THERMAL_LIMIT)) {
                    c = 1;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    public void sendMonitorParam(ArrayMap<String, Object> params) {
        if (params == null || !(params.get(DisplayEffectMonitor.MonitorModule.PARAM_TYPE) instanceof String)) {
            Slog.e(TAG, "sendMonitorParam() input params format error!");
            return;
        }
        String paramType = (String) params.get(DisplayEffectMonitor.MonitorModule.PARAM_TYPE);
        char c = 65535;
        switch (paramType.hashCode()) {
            case -1382856991:
                if (paramType.equals(TYPE_USER_SCENE_MISRECOGNITION)) {
                    c = 5;
                    break;
                }
                break;
            case -663211403:
                if (paramType.equals(TYPE_POWER_STATE_UPDATE)) {
                    c = 0;
                    break;
                }
                break;
            case -238058188:
                if (paramType.equals(TYPE_STATE_TIMER)) {
                    c = 2;
                    break;
                }
                break;
            case 871202699:
                if (paramType.equals(TYPE_SCENE_RECOGNITION)) {
                    c = 3;
                    break;
                }
                break;
            case 1567901733:
                if (paramType.equals(TYPE_ALGO_DISCOUNT_BRIGHTNESS)) {
                    c = 4;
                    break;
                }
                break;
            case 1671621220:
                if (paramType.equals(TYPE_THERMAL_LIMIT)) {
                    c = 1;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                powerStateUpdate(params);
                break;
            case 1:
                thermalLimit(params);
                break;
            case 2:
                stateTimer();
                break;
            case 3:
                sceneRecognition(params);
                break;
            case 4:
                algoDiscountBrightness(params);
                break;
            case 5:
                userSceneMisrecognition(params);
                break;
            default:
                Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
                break;
        }
    }

    public void triggerUploadTimer() {
        int i = this.mHourCount + 1;
        this.mHourCount = i;
        if (i >= 24) {
            this.mHourCount = 0;
            this.mUserSceneMisrecognitionTimes = 0;
        }
    }

    private void powerStateUpdate(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && powerStateUpdateCheckParamValid(params)) {
            String powerStateName = (String) params.get(PARAM_POWER_STATE);
            try {
                PowerState newState = PowerState.valueOf(powerStateName);
                if (this.mPowerState != newState) {
                    if (HWDEBUG) {
                        Slog.d(TAG, "powerStateUpdate " + newState);
                    }
                    powerStateProcess(newState);
                    this.mPowerState = newState;
                }
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "powerStateUpdate() input error! powerState=" + powerStateName);
            }
        }
    }

    private boolean powerStateUpdateCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (params.get(PARAM_POWER_STATE) instanceof String) {
            return true;
        }
        Slog.e(TAG, "powerStateUpdateCheckParamValid() can't get param: powerState");
        return false;
    }

    private void powerStateProcess(PowerState newState) {
        if (newState == PowerState.OFF) {
            if (HWDEBUG) {
                Slog.d(TAG, "powerStateProcess removeMonitorMsg()");
            }
            this.mManager.removeMonitorMsg(BackLightMonitorManager.MsgType.STATE_TIMER);
        }
        if (newState == PowerState.ON && this.mPowerState == PowerState.OFF) {
            setStateTimer(60000);
        }
    }

    private void setStateTimer(long delayMillis) {
        if (this.mDebugMode) {
            delayMillis /= 10;
        }
        if (HWDEBUG) {
            Slog.d(TAG, "setStateTimer delay " + (delayMillis / 1000) + " s");
        }
        ArrayMap<String, Object> params = new ArrayMap<>();
        params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, TYPE_STATE_TIMER);
        this.mManager.sendMonitorMsgDelayed(BackLightMonitorManager.MsgType.STATE_TIMER, params, delayMillis);
    }

    private void thermalLimit(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && thermalLimitCheckParamValid(params)) {
            boolean isThermalLimited = ((Boolean) params.get(PARAM_IS_THERMAL_LIMITED)).booleanValue();
            this.mCommonData.setThermalLimited(isThermalLimited);
            if (HWFLOW) {
                Slog.i(TAG, "thermalLimit " + isThermalLimited);
            }
        }
    }

    private boolean thermalLimitCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (params.get(PARAM_IS_THERMAL_LIMITED) instanceof Boolean) {
            return true;
        }
        Slog.e(TAG, "thermalLimitCheckParamValid() can't get param: isLimited");
        return false;
    }

    private void stateTimer() {
        if (this.mPowerState != PowerState.OFF) {
            setStateTimer(600000);
        }
        stateProcess();
    }

    private void stateProcess() {
        if (needUploadState()) {
            long currentTime = SystemClock.elapsedRealtime();
            long minIntervalMillis = this.mDebugMode ? 59900 : STATE_UPLOAD_MIN_INTERVAL_MILLIS;
            if (this.mStateUploadTime == 0 || currentTime - this.mStateUploadTime >= minIntervalMillis) {
                this.mStateUploadTime = currentTime;
                stateUpload(this.mScene, this.mCommonData.getSmoothAmbientLight(), this.mCommonData.getDiscountBrightness());
            }
        }
    }

    private boolean needUploadState() {
        if (this.mScene == null || this.mCommonData.getBrightnessMode() != BackLightCommonData.BrightnessMode.AUTO || this.mPowerState != PowerState.ON || this.mCommonData.isThermalLimited()) {
            return false;
        }
        if (this.mScene == BackLightCommonData.Scene.VIDEO || !this.mCommonData.isWindowManagerBrightnessMode()) {
            return true;
        }
        return false;
    }

    private void stateUpload(BackLightCommonData.Scene scene, int ambient, int backlight) {
        IMonitor.EventStream stream = IMonitor.openEventStream(932010105);
        stream.setParam("Scene", (byte) scene.ordinal());
        int i = DubaiConstants.MASK_MODULE_ALL;
        stream.setParam("Ambient", (short) (ambient < 32767 ? ambient : 32767));
        if (backlight < 32767) {
            i = backlight;
        }
        stream.setParam("Backlight", (short) i);
        IMonitor.sendEvent(stream);
        IMonitor.closeEventStream(stream);
        if (HWFLOW) {
            Slog.i(TAG, "stateUpload() scene=" + scene + ", ambient=" + ambient + ", backlight=" + backlight);
        }
    }

    private void sceneRecognition(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && sceneRecognitionCheckParamValid(params)) {
            String sceneName = (String) params.get("scene");
            try {
                this.mScene = BackLightCommonData.Scene.valueOf(sceneName);
                if (HWDEBUG) {
                    Slog.d(TAG, "sceneRecognition " + this.mScene);
                }
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "sceneRecognition() input error! scene=" + sceneName);
            }
        }
    }

    private boolean sceneRecognitionCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (params.get("scene") instanceof String) {
            return true;
        }
        Slog.e(TAG, "sceneRecognitionCheckParamValid() can't get param: scene");
        return false;
    }

    private void algoDiscountBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && algoDiscountBrightnessCheckParamValid(params)) {
            int algoDiscountBrightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
            this.mCommonData.setDiscountBrightness(algoDiscountBrightness);
            if (HWDEBUG) {
                Slog.d(TAG, "mAlgoDiscountBrightness " + algoDiscountBrightness);
            }
        }
    }

    private boolean algoDiscountBrightnessCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (params.get(PARAM_BRIGHTNESS) instanceof Integer) {
            return true;
        }
        Slog.e(TAG, "algoDiscountBrightnessCheckParamValid() can't get param: brightness");
        return false;
    }

    private void userSceneMisrecognition(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && userSceneMisrecognitionCheckParamValid(params)) {
            int i = this.mUserSceneMisrecognitionTimes + 1;
            this.mUserSceneMisrecognitionTimes = i;
            if (i <= 10) {
                List<Short> confidenceList = (List) params.get(PARAM_CONFIDENCE);
                int userScene = ((Integer) params.get(PARAM_USER_SCENE)).intValue();
                userSceneMisrecognitionUpload(confidenceList, userScene);
                if (HWFLOW) {
                    Slog.i(TAG, "userSceneMisrecognition() times=" + this.mUserSceneMisrecognitionTimes + ", userScene=" + userScene + ", confidenceList=" + confidenceList);
                }
            }
        }
    }

    private boolean userSceneMisrecognitionCheckParamValid(ArrayMap<String, Object> params) {
        if (params == null) {
            return false;
        }
        if (!(params.get(PARAM_USER_SCENE) instanceof Integer)) {
            Slog.e(TAG, "sceneRecognitionCheckParamValid() can't get param: userScene");
            return false;
        } else if (!(params.get(PARAM_CONFIDENCE) instanceof List)) {
            Slog.e(TAG, "userSceneMisrecognitionCheckParamValid() can't get param: confidence");
            return false;
        } else {
            List<Short> confidenceList = (List) params.get(PARAM_CONFIDENCE);
            if (confidenceList == null) {
                Slog.e(TAG, "userSceneMisrecognitionCheckParamValid() error confidenceList is null");
                return false;
            } else if (confidenceList.size() > 5) {
                Slog.e(TAG, "userSceneMisrecognitionCheckParamValid() confidenceList size error: " + confidenceList);
                return false;
            } else {
                try {
                    for (Object obj : confidenceList) {
                        if (!(obj instanceof Short)) {
                            Slog.e(TAG, "userSceneMisrecognitionCheckParamValid() confidenceList type error obj = " + obj);
                            return false;
                        }
                    }
                    return true;
                } catch (ClassCastException e) {
                    Slog.e(TAG, "userSceneMisrecognitionCheckParamValid() confidenceList type error");
                    return false;
                }
            }
        }
    }

    private void userSceneMisrecognitionUpload(List<Short> confidenceList, int userScene) {
        IMonitor.EventStream stream = IMonitor.openEventStream(932010107);
        int i = DubaiConstants.MASK_MODULE_ALL;
        if (userScene < 32767) {
            i = userScene;
        }
        stream.setParam("Scene", (short) i);
        int i2 = 0;
        for (Short confidence : confidenceList) {
            i2++;
            stream.setParam(String.format("C%d", new Object[]{Integer.valueOf(i2)}), confidence.shortValue());
        }
        IMonitor.sendEvent(stream);
        IMonitor.closeEventStream(stream);
        if (HWFLOW) {
            Slog.i(TAG, "userSceneMisrecognitionUpload done");
        }
    }
}
