package com.android.server.display;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwLightSensorController.LightSensorCallbacks;
import com.android.server.display.HwSmartBackLightXmlLoader.Data;
import com.android.server.emcom.daemon.CommandsInterface;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;

public class HwSmartBackLightNormalizedController implements LightSensorCallbacks {
    public static final int BRIGHTNESS_UPDATE_END = 1;
    public static final int BRIGHTNESS_UPDATE_START = 0;
    private static final boolean DEBUG;
    private static final int DEFAULT = 0;
    private static final int INDOOR = 1;
    private static final int MSG_PROCESS_POLICY = 1;
    private static final int MSG_UNINIT_SBL = 2;
    private static final int MSG_UPDATE_SBL_BRIGHTNESS = 3;
    private static final int OUTDOOR = 2;
    private static final int REFRESH_FRAMES_NUM = 100;
    private static final String TAG = "HwSmartBackLightNormalizedController";
    private static final int UPDATE_BRIGHTNESS_INTERVAL = 500;
    private boolean mAppSBLScene;
    private int mCurrentLux;
    private final Data mData;
    private boolean mIsBrightnessUpdating;
    private boolean mLastAppSBLScene;
    private int mLastOutdoorScence = 0;
    private final HwLightSensorController mLightSensorController;
    private boolean mLightSensorEnable;
    private final LightsManager mLightsManager;
    private boolean mNeedUpdateSBL;
    private HwSmartBackLightOutdoorNormalizedDetector mOutdoorDetector;
    private int mOutdoorScene;
    private final ProcessTriggerHandler mProcessTriggerHandler;
    private final HandlerThread mSBLPolicyProcessThread;
    private SceneRecognition mSceneRecognition;
    private Light mSmartBackLight;
    private boolean mSmartBackLightEnable;
    private boolean mSmartBackLightOn;
    private int mSmoothAmbientLux;
    private long mUpdateSBLBrightnessTimestamp;
    private boolean mVideoSceneEntered = false;
    private boolean mWarmUpFirstTime;

    private class ProcessTriggerHandler extends Handler {
        public ProcessTriggerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwSmartBackLightNormalizedController.this.processPolicy();
                    return;
                case 2:
                    HwSmartBackLightNormalizedController.this.uninitSBL();
                    return;
                case 3:
                    HwSmartBackLightNormalizedController.this.updateSBLBrightness();
                    return;
                default:
                    Slog.e(HwSmartBackLightNormalizedController.TAG, "Invalid message");
                    return;
            }
        }
    }

    private final class SceneRecognition implements IPGPlugCallbacks {
        private static final String TAG = "SceneRecognition";
        private boolean mInCameraScene;
        private boolean mInGalleryScene;
        private boolean mInVideoScene;
        private PGPlug mPGPlug = new PGPlug(this, TAG);

        public SceneRecognition(Context context) {
            Slog.i(TAG, TAG);
            new Thread(this.mPGPlug, TAG).start();
        }

        public void cleanScene() {
            this.mInVideoScene = false;
            this.mInCameraScene = false;
            this.mInGalleryScene = false;
        }

        public boolean isInAppScene() {
            if (HwSmartBackLightNormalizedController.this.mData.sceneVideoEnable && this.mInVideoScene) {
                return true;
            }
            if (HwSmartBackLightNormalizedController.this.mData.sceneCameraEnable && this.mInCameraScene) {
                return true;
            }
            if (HwSmartBackLightNormalizedController.this.mData.sceneGalleryEnable && this.mInGalleryScene) {
                return true;
            }
            return false;
        }

        public void onDaemonConnected() {
            Slog.i(TAG, "SceneRecognition Client Connected");
        }

        public boolean onEvent(int actionID, String value) {
            boolean z = false;
            switch (actionID) {
                case IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT /*10004*/:
                    this.mInGalleryScene = true;
                    break;
                case IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT /*10007*/:
                    this.mInCameraScene = true;
                    break;
                case IDisplayEngineService.DE_ACTION_PG_VIDEO_START /*10015*/:
                    this.mInVideoScene = true;
                    break;
                case IDisplayEngineService.DE_ACTION_PG_VIDEO_END /*10016*/:
                    this.mInVideoScene = false;
                    break;
                case IDisplayEngineService.DE_ACTION_PG_CAMERA_END /*10017*/:
                    this.mInCameraScene = false;
                    break;
                default:
                    if (this.mInGalleryScene && 1 == PGAction.checkActionType(actionID)) {
                        this.mInGalleryScene = false;
                        break;
                    }
            }
            HwSmartBackLightNormalizedController.this.mAppSBLScene = isInAppScene();
            HwSmartBackLightNormalizedController hwSmartBackLightNormalizedController = HwSmartBackLightNormalizedController.this;
            if (HwSmartBackLightNormalizedController.this.mData.sceneVideoEnable) {
                z = this.mInVideoScene;
            }
            hwSmartBackLightNormalizedController.mVideoSceneEntered = z;
            if (HwSmartBackLightNormalizedController.this.mLightSensorEnable) {
                HwSmartBackLightNormalizedController.this.mProcessTriggerHandler.sendEmptyMessage(1);
            }
            return true;
        }

        public void onConnectedTimeout() {
            Slog.e(TAG, "error, SceneRecognition Client ConnectedTimeout!");
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwSmartBackLightNormalizedController(Context context, LightsManager lightsManager, SensorManager sensorManager) {
        this.mLightsManager = lightsManager;
        this.mData = HwSmartBackLightXmlLoader.getData();
        this.mSmartBackLight = this.mLightsManager.getLight(CommandsInterface.EMCOM_SD_XENGINE_START_ACC);
        try {
            this.mOutdoorDetector = new HwSmartBackLightOutdoorNormalizedDetector();
            if (isSceneRecognitionEnable()) {
                this.mSceneRecognition = new SceneRecognition(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mLightSensorController = new HwLightSensorController(this, sensorManager, this.mData.lightSensorRateMills);
        this.mSBLPolicyProcessThread = new HandlerThread(TAG);
        this.mSBLPolicyProcessThread.start();
        this.mProcessTriggerHandler = new ProcessTriggerHandler(this.mSBLPolicyProcessThread.getLooper());
    }

    private boolean isSceneRecognitionEnable() {
        return (this.mData.sceneVideoEnable || this.mData.sceneGalleryEnable) ? true : this.mData.sceneCameraEnable;
    }

    public static boolean checkIfUsingHwSBL() {
        return true;
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

    public void updatePowerState(int state, boolean useSmartBacklight) {
        if (this.mSmartBackLightEnable || (useSmartBacklight ^ 1) == 0) {
            if (this.mSmartBackLightEnable != useSmartBacklight) {
                if (DEBUG) {
                    Slog.i(TAG, "mSmartBackLightEnable change " + this.mSmartBackLightEnable + " -> " + useSmartBacklight);
                }
                this.mSmartBackLightEnable = useSmartBacklight;
            }
            if (!wantScreenOn(state)) {
                useSmartBacklight = false;
            }
            setLightSensorEnabled(useSmartBacklight);
        }
    }

    public void updateBrightnessState(int state) {
        if (this.mSmartBackLightEnable) {
            if (state == 0) {
                if (!this.mIsBrightnessUpdating) {
                    this.mIsBrightnessUpdating = true;
                }
            } else if (this.mIsBrightnessUpdating) {
                this.mIsBrightnessUpdating = false;
                if (this.mSmartBackLightOn) {
                    this.mProcessTriggerHandler.sendEmptyMessage(3);
                }
            }
            return;
        }
        Slog.w(TAG, "updateBrightnessState sbl didn't enable");
        if (this.mIsBrightnessUpdating) {
            this.mIsBrightnessUpdating = false;
            if (DEBUG) {
                Slog.i(TAG, "updateBrightnessState clear mIsBrightnessUpdating");
            }
        }
    }

    private void setLightSensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mLightSensorEnable) {
                this.mLightSensorEnable = true;
                if (DEBUG) {
                    Slog.i(TAG, "light sensor open");
                }
                this.mWarmUpFirstTime = true;
                this.mLightSensorController.enableSensor();
            }
        } else if (this.mLightSensorEnable) {
            this.mLightSensorEnable = false;
            if (DEBUG) {
                Slog.i(TAG, "light sensor close");
            }
            this.mLightSensorController.disableSensor();
            this.mProcessTriggerHandler.removeMessages(1);
            this.mProcessTriggerHandler.sendEmptyMessage(2);
            this.mOutdoorDetector.clearAmbientLightRingBuffer();
        }
    }

    private void initSBL() {
        if (this.mSmartBackLightOn) {
            Slog.w(TAG, "initSBL but sbl is already on");
            return;
        }
        if (DEBUG) {
            Slog.i(TAG, "initSBL");
        }
        this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(0, this.mData.apicalADLevel, 0, 0, false, 0, 0);
    }

    private void uninitSBL() {
        this.mSmartBackLightOn = false;
        if (DEBUG) {
            Slog.i(TAG, "uninitSBL");
        }
        this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(0, this.mData.apicalADLevel, 0, 0, false, 0, 0);
        if (this.mSceneRecognition != null) {
            this.mSceneRecognition.cleanScene();
        }
        this.mOutdoorScene = 0;
        this.mLastOutdoorScence = 0;
        this.mAppSBLScene = false;
        this.mLastAppSBLScene = false;
        this.mIsBrightnessUpdating = false;
    }

    private void turnOnSBL() {
        if (this.mSmartBackLightOn) {
            Slog.w(TAG, "turnOnSBL but sbl is already on");
            return;
        }
        this.mSmartBackLightOn = true;
        if (DEBUG) {
            Slog.i(TAG, "turnOnSBL, mSmoothAmbientLux = " + this.mSmoothAmbientLux);
        }
        this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(1, getApicalADLevel(), getSmoothAmbientLux(), 100, false, 0, 0);
    }

    private void turnOffSBL() {
        if (this.mSmartBackLightOn) {
            this.mSmartBackLightOn = false;
            if (DEBUG) {
                Slog.i(TAG, "turnOffSBL");
            }
            if (this.mLastAppSBLScene && (this.mAppSBLScene ^ 1) != 0 && this.mData.videoSceneEnhanceEnabled) {
                this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(0, this.mData.apicalADLevel, 0, 100, false, 0, 0);
                return;
            } else {
                this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(1, this.mData.apicalADLevel, 0, 100, true, 0, 0);
                return;
            }
        }
        Slog.w(TAG, "turnOffSBL but sbl is already off");
    }

    private void updateSBLLux() {
        if (this.mSmartBackLightOn) {
            if (DEBUG) {
                Slog.i(TAG, "updateSBLLux, mSmoothAmbientLux = " + this.mSmoothAmbientLux);
            }
            this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(1, getApicalADLevel(), getSmoothAmbientLux(), 100, false, 0, 0);
            return;
        }
        Slog.w(TAG, "updateSBLLux but sbl is off");
    }

    private void updateSBLBrightness() {
        if (this.mSmartBackLightOn) {
            long time = SystemClock.elapsedRealtime();
            if (this.mUpdateSBLBrightnessTimestamp == 0 || time - this.mUpdateSBLBrightnessTimestamp >= 500) {
                this.mUpdateSBLBrightnessTimestamp = time;
                if (DEBUG) {
                    Slog.i(TAG, "updateSBLBrightness");
                }
                this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(1, getApicalADLevel(), getSmoothAmbientLux(), 100, false, 0, 0);
                return;
            }
            return;
        }
        Slog.w(TAG, "updateSBLBrightness but sbl is off");
    }

    private void processPolicy() {
        if (needMakePolicy()) {
            boolean isOutdoor = this.mOutdoorScene == 2;
            if (DEBUG && (this.mIsBrightnessUpdating ^ 1) != 0) {
                Slog.i(TAG, "OutdoorScence = " + isOutdoor + ", AppSBLScene = " + this.mAppSBLScene + ", UpdateSBL = " + this.mNeedUpdateSBL + ", CurrentLux = " + this.mCurrentLux + ", SmoothLux = " + this.mSmoothAmbientLux + ", UpdataBright = " + this.mIsBrightnessUpdating);
            }
            if (!this.mWarmUpFirstTime) {
                makePolicy();
            } else if (this.mOutdoorScene == 0) {
                if (DEBUG) {
                    Slog.i(TAG, "WARM UP, wait outdoor scene detect result");
                }
                return;
            } else {
                this.mWarmUpFirstTime = false;
                makeWarmUpPolicy();
            }
            this.mNeedUpdateSBL = false;
            this.mLastOutdoorScence = this.mOutdoorScene;
            this.mLastAppSBLScene = this.mAppSBLScene;
        }
    }

    private boolean needMakePolicy() {
        if (this.mOutdoorScene != this.mLastOutdoorScence || this.mAppSBLScene != this.mLastAppSBLScene || this.mNeedUpdateSBL) {
            return true;
        }
        if (this.mIsBrightnessUpdating && this.mSmartBackLightOn) {
            return true;
        }
        return false;
    }

    private void makeWarmUpPolicy() {
        if (this.mOutdoorScene == 2 || (this.mOutdoorScene == 1 && this.mAppSBLScene)) {
            if (DEBUG) {
                Slog.i(TAG, "WARM UP, turnOnSBL");
            }
            turnOnSBL();
            return;
        }
        if (DEBUG) {
            Slog.i(TAG, "WARM UP, initSBL");
        }
        initSBL();
    }

    private void makePolicy() {
        if (this.mLastOutdoorScence == 1 && this.mOutdoorScene == 2) {
            policyIndoorToOutdoor();
        } else if (this.mLastOutdoorScence == 2 && this.mOutdoorScene == 1) {
            policyOutdoorToIndoor();
        } else if (!this.mLastAppSBLScene && this.mAppSBLScene) {
            policyEnterAppScene();
        } else if (this.mLastAppSBLScene && (this.mAppSBLScene ^ 1) != 0) {
            policyExitAppScene();
        } else if (this.mNeedUpdateSBL) {
            policyLuxChanged();
        } else if (this.mIsBrightnessUpdating) {
            policySceenBrightnessChanged();
        }
    }

    private void policyIndoorToOutdoor() {
        if (this.mAppSBLScene) {
            if (DEBUG) {
                Slog.i(TAG, "INDOOR -> OUTDOOR, in app SBL scene, SBL already on");
            }
            updateSBLLux();
        } else if (this.mSmartBackLightOn) {
            Slog.w(TAG, "INDOOR -> OUTDOOR, not in app SBL scene, but SBL already on, error!");
        } else {
            if (DEBUG) {
                Slog.i(TAG, "INDOOR -> OUTDOOR, enableSBL");
            }
            turnOnSBL();
        }
    }

    private void policyOutdoorToIndoor() {
        if (this.mAppSBLScene) {
            if (DEBUG) {
                Slog.i(TAG, "OUTDOOR -> INDOOR, in app SBL scene, needn't turn off SBL");
            }
            updateSBLLux();
        } else if (this.mSmartBackLightOn) {
            if (DEBUG) {
                Slog.i(TAG, "OUTDOOR -> INDOOR, turnOffSBL");
            }
            turnOffSBL();
        } else {
            Slog.w(TAG, "OUTDOOR -> INDOOR, but SBL was off, error!");
        }
    }

    private void policyEnterAppScene() {
        if (this.mOutdoorScene != 1) {
            return;
        }
        if (this.mSmartBackLightOn) {
            Slog.w(TAG, "INDOOR, wasn't app SBL scene, but SBL was on, error!");
            return;
        }
        if (DEBUG) {
            Slog.i(TAG, "INDOOR, enter app SBL scene, turnOnSBL");
        }
        turnOnSBL();
    }

    private void policyExitAppScene() {
        if (this.mOutdoorScene != 1) {
            return;
        }
        if (this.mSmartBackLightOn) {
            if (DEBUG) {
                Slog.i(TAG, "INDOOR, exit app SBL scene, turnOffSBL");
            }
            turnOffSBL();
            return;
        }
        Slog.w(TAG, "INDOOR, in app SBL scene, but SBL was off, error!");
    }

    private void policyLuxChanged() {
        if (this.mSmartBackLightOn) {
            updateSBLLux();
        }
    }

    private void policySceenBrightnessChanged() {
        if (this.mSmartBackLightOn) {
            updateSBLBrightness();
        }
    }

    private int getApicalADLevel() {
        if (!this.mData.videoSceneEnhanceEnabled || !this.mVideoSceneEntered) {
            return this.mData.apicalADLevel;
        }
        if (this.mCurrentLux <= this.mData.videoSceneDarknessThreshold) {
            return this.mData.darknessApicalADLevel;
        }
        if (this.mCurrentLux <= this.mData.videoSceneDarknessThreshold || this.mCurrentLux > this.mData.videoSceneIndoorThreshold) {
            return this.mData.outdoorApicalADLevel;
        }
        return this.mData.indoorApicalADLevel;
    }

    private int getSmoothAmbientLux() {
        if (!this.mData.videoSceneEnhanceEnabled || !this.mVideoSceneEntered) {
            return this.mSmoothAmbientLux;
        }
        if (this.mCurrentLux <= this.mData.videoSceneDarknessThreshold) {
            return this.mSmoothAmbientLux + this.mData.darknessAmbidentBrightnessShift;
        }
        if (this.mCurrentLux <= this.mData.videoSceneDarknessThreshold || this.mCurrentLux > this.mData.videoSceneIndoorThreshold) {
            return this.mSmoothAmbientLux + this.mData.outdoorAmbidentBrightnessShift;
        }
        return this.mSmoothAmbientLux + this.mData.indoorAmbidentBrightnessShift;
    }

    public void processSensorData(long timeInMs, int lux, int cct) {
        this.mCurrentLux = lux;
        this.mOutdoorDetector.handleLightSensorEvent(timeInMs, (float) lux);
        this.mOutdoorScene = this.mOutdoorDetector.getIndoorOutdoorFlagForSBL();
        this.mNeedUpdateSBL = this.mOutdoorDetector.getLuxChangedFlagForSBL();
        this.mSmoothAmbientLux = (int) this.mOutdoorDetector.getAmbientLuxForSBL();
        if (this.mLightSensorEnable) {
            this.mProcessTriggerHandler.sendEmptyMessage(1);
        }
    }
}
