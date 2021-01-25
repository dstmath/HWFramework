package com.android.server.display;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.BackLightCommonData;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.hidata.wavemapping.cons.Constant;
import java.util.List;
import java.util.Locale;

/* access modifiers changed from: package-private */
public class BrightnessStateMonitor implements DisplayEffectMonitor.MonitorModule {
    private static final int CONFIDENCE_MAX_SIZE = 5;
    private static final int DEBUG_MODE_ACCELERATE_RATIO = 10;
    private static final int HOURS_PER_DAY = 24;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final int IMONITOR_SCENE_MISRECOGNITION_EVENT_ID = 932010107;
    private static final int IMONITOR_STATE_EVENT_ID = 932010105;
    private static final int MS_PER_SECOND = 1000;
    private static final String PARAM_BRIGHTNESS = "brightness";
    private static final String PARAM_CONFIDENCE = "confidence";
    private static final String PARAM_IS_THERMAL_LIMITED = "isLimited";
    private static final String PARAM_POWER_STATE = "powerState";
    private static final String PARAM_SCENE = "scene";
    private static final String PARAM_USER_SCENE = "userScene";
    private static final int STATE_TIMER_FIRST_DELAY_MILLIS = 60000;
    private static final int STATE_TIMER_PERIOD_MILLIS = 600000;
    private static final int STATE_UPLOAD_MIN_INTERVAL_MILLIS = 599000;
    private static final String TAG = "BrightnessStateMonitor";
    private static final String TYPE_ALGO_DISCOUNT_BRIGHTNESS = "algoDiscountBrightness";
    private static final String TYPE_POWER_STATE_UPDATE = "powerStateUpdate";
    private static final String TYPE_SCENE_RECOGNITION = "sceneRecognition";
    private static final String TYPE_THERMAL_LIMIT = "thermalLimit";
    private static final String TYPE_USER_SCENE_MISRECOGNITION = "userSceneMisrecognition";
    private static final int USER_SCENE_MISRECOGNITION_TIMES_LIMIT_PER_DAY = 10;
    private final BackLightCommonData mCommonData;
    private int mHourCount;
    private final boolean mIsInDebugMode;
    private final BackLightMonitorManager mManager;
    private final DisplayEffectMonitor mMonitor;
    private PowerState mPowerState = PowerState.OFF;
    private BackLightCommonData.Scene mScene;
    private long mStateUploadTime;
    private int mUserSceneMisrecognitionTimes;

    /* access modifiers changed from: private */
    public enum PowerState {
        OFF,
        ON,
        DIM,
        VR
    }

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    BrightnessStateMonitor(DisplayEffectMonitor monitor, BackLightMonitorManager manager) {
        this.mMonitor = monitor;
        this.mManager = manager;
        this.mCommonData = manager.getBackLightCommonData();
        this.mIsInDebugMode = SystemProperties.getBoolean("persist.display.monitor.debug", false);
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public boolean isParamOwner(String paramType) {
        if (paramType == null) {
            return false;
        }
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
                if (paramType.equals(BackLightMonitorManager.TYPE_STATE_TIMER)) {
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
        if (c == 0 || c == 1 || c == 2 || c == 3 || c == 4 || c == 5) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public void sendMonitorParam(String paramType, ArrayMap<String, Object> params) {
        if (paramType == null) {
            Slog.e(TAG, "paramType is null");
            return;
        }
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
                if (paramType.equals(BackLightMonitorManager.TYPE_STATE_TIMER)) {
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
        if (c == 0) {
            receiveParamForPowerState(params);
        } else if (c == 1) {
            receiveParamForThermalLimit(params);
        } else if (c == 2) {
            receiveParamForStateTimer();
        } else if (c == 3) {
            receiveParamForSceneRecognition(params);
        } else if (c == 4) {
            receiveParamForAlgoDiscountBrightness(params);
        } else if (c != 5) {
            Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
        } else {
            receiveParamForUserSceneMisrecognition(params);
        }
    }

    @Override // com.android.server.display.DisplayEffectMonitor.MonitorModule
    public void triggerUploadTimer() {
        int i = this.mHourCount + 1;
        this.mHourCount = i;
        if (i >= 24) {
            this.mHourCount = 0;
            this.mUserSceneMisrecognitionTimes = 0;
        }
    }

    private void receiveParamForPowerState(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object powerStateNameObj = params.get(PARAM_POWER_STATE);
            if (!(powerStateNameObj instanceof String)) {
                Slog.e(TAG, "receiveParamForPowerState() can't get param: powerState");
                return;
            }
            String powerStateName = (String) powerStateNameObj;
            try {
                PowerState newState = PowerState.valueOf(powerStateName);
                if (this.mPowerState != newState) {
                    if (HWDEBUG) {
                        Slog.d(TAG, "receiveParamForPowerState " + newState);
                    }
                    controlStateTimerByPowerState(newState);
                    this.mPowerState = newState;
                }
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "receiveParamForPowerState() input error! powerState=" + powerStateName);
            }
        }
    }

    private void controlStateTimerByPowerState(PowerState newState) {
        if (newState == PowerState.OFF) {
            if (HWDEBUG) {
                Slog.d(TAG, "powerStateProcess resetStateTimer()");
            }
            this.mManager.resetStateTimer();
        }
        if (newState == PowerState.ON && this.mPowerState == PowerState.OFF) {
            setStateTimer(60000);
        }
    }

    private void setStateTimer(long delayMillis) {
        long delayTimeInMs = this.mIsInDebugMode ? delayMillis / 10 : delayMillis;
        if (HWDEBUG) {
            Slog.d(TAG, "setStateTimer delay " + (delayTimeInMs / 1000) + " s");
        }
        this.mManager.setStateTimer(delayTimeInMs);
    }

    private void receiveParamForThermalLimit(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object isThermalLimitedObj = params.get(PARAM_IS_THERMAL_LIMITED);
            if (!(isThermalLimitedObj instanceof Boolean)) {
                Slog.e(TAG, "receiveParamForThermalLimit() can't get param: isLimited");
                return;
            }
            boolean isThermalLimited = ((Boolean) isThermalLimitedObj).booleanValue();
            this.mCommonData.setThermalLimited(isThermalLimited);
            if (HWFLOW) {
                Slog.i(TAG, "receiveParamForThermalLimit " + isThermalLimited);
            }
        }
    }

    private void receiveParamForStateTimer() {
        if (this.mPowerState != PowerState.OFF) {
            setStateTimer(Constant.MAX_TRAIN_MODEL_TIME);
        }
        if (needUploadState()) {
            long currentTime = SystemClock.elapsedRealtime();
            long minIntervalMillis = this.mIsInDebugMode ? 59900 : 599000;
            long j = this.mStateUploadTime;
            if (j == 0 || currentTime - j >= minIntervalMillis) {
                this.mStateUploadTime = currentTime;
                uploadState(this.mScene, this.mCommonData.getSmoothAmbientLight(), this.mCommonData.getDiscountBrightness());
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

    private void uploadState(BackLightCommonData.Scene scene, int ambient, int backlight) {
        IMonitor.EventStream stream = IMonitor.openEventStream((int) IMONITOR_STATE_EVENT_ID);
        stream.setParam("Scene", (byte) scene.getSceneValue());
        int i = 32767;
        stream.setParam("Ambient", (short) (ambient < 32767 ? ambient : 32767));
        if (backlight < 32767) {
            i = backlight;
        }
        stream.setParam("Backlight", (short) i);
        IMonitor.sendEvent(stream);
        IMonitor.closeEventStream(stream);
        if (HWFLOW) {
            Slog.i(TAG, "uploadState() scene=" + scene + ", ambient=" + ambient + ", backlight=" + backlight);
        }
    }

    private void receiveParamForSceneRecognition(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object sceneNameObj = params.get(PARAM_SCENE);
            if (!(sceneNameObj instanceof String)) {
                Slog.e(TAG, "receiveParamForSceneRecognition() can't get param: scene");
                return;
            }
            String sceneName = (String) sceneNameObj;
            try {
                this.mScene = BackLightCommonData.Scene.valueOf(sceneName);
                if (HWDEBUG) {
                    Slog.d(TAG, "receiveParamForSceneRecognition " + this.mScene);
                }
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "receiveParamForSceneRecognition() input error! scene=" + sceneName);
            }
        }
    }

    private void receiveParamForAlgoDiscountBrightness(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion()) {
            Object algoDiscountBrightnessObj = params.get(PARAM_BRIGHTNESS);
            if (!(algoDiscountBrightnessObj instanceof Integer)) {
                Slog.e(TAG, "receiveParamForAlgoDiscountBrightness() can't get param: brightness");
                return;
            }
            int algoDiscountBrightness = ((Integer) algoDiscountBrightnessObj).intValue();
            this.mCommonData.setDiscountBrightness(algoDiscountBrightness);
            if (HWDEBUG) {
                Slog.d(TAG, "receiveParamForAlgoDiscountBrightness " + algoDiscountBrightness);
            }
        }
    }

    private void receiveParamForUserSceneMisrecognition(ArrayMap<String, Object> params) {
        if (this.mCommonData.isProductEnable() && !this.mCommonData.isCommercialVersion() && this.mUserSceneMisrecognitionTimes < 10) {
            List<Short> confidenceList = BackLightMonitorManager.getParamList(params, Short.class, PARAM_CONFIDENCE, 5);
            if (!confidenceList.isEmpty()) {
                Object userSceneObj = params.get(PARAM_USER_SCENE);
                if (!(userSceneObj instanceof Integer)) {
                    Slog.e(TAG, "receiveParamForUserSceneMisrecognition() can't get param: userScene");
                    return;
                }
                int userScene = ((Integer) userSceneObj).intValue();
                uploadUserSceneMisrecognition(confidenceList, userScene);
                this.mUserSceneMisrecognitionTimes++;
                if (HWFLOW) {
                    Slog.i(TAG, "receiveParamForUserSceneMisrecognition() times=" + this.mUserSceneMisrecognitionTimes + ", userScene=" + userScene + ", confidenceList=" + confidenceList);
                }
            }
        }
    }

    private void uploadUserSceneMisrecognition(List<Short> confidenceList, int userScene) {
        IMonitor.EventStream stream = IMonitor.openEventStream((int) IMONITOR_SCENE_MISRECOGNITION_EVENT_ID);
        int i = 32767;
        if (userScene < 32767) {
            i = userScene;
        }
        stream.setParam("Scene", (short) i);
        int index = 1;
        for (Short confidence : confidenceList) {
            stream.setParam(String.format(Locale.ROOT, "C%d", Integer.valueOf(index)), confidence.shortValue());
            index++;
        }
        IMonitor.sendEvent(stream);
        IMonitor.closeEventStream(stream);
        if (HWFLOW) {
            Slog.i(TAG, "uploadUserSceneMisrecognition done");
        }
    }
}
