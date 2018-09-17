package com.android.server.display;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.display.HwLightSensorController.LightSensorCallbacks;
import com.android.server.display.HwSunlightReadabilityEnhancementXmlLoader.Data;
import com.android.server.emcom.daemon.CommandsInterface;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGPlug;

public class HwNormalizedHiACELightController implements LightSensorCallbacks {
    public static final int BRIGHTNESS_UPDATE_END = 1;
    public static final int BRIGHTNESS_UPDATE_START = 0;
    private static final int DEFAULT = 0;
    private static final int INDOOR = 1;
    private static final int MSG_PROCESS_POLICY = 1;
    private static final int MSG_UNINIT_SRE = 2;
    private static final int MSG_UPDATE_SRE_BRIGHTNESS = 3;
    private static final int OUTDOOR = 2;
    private static final int REFRESH_FRAMES_NUM = 100;
    private static String TAG = "HwNormalizedHiACELightController";
    private static final int UPDATE_BRIGHTNESS_INTERVAL = 500;
    private Light mAmbientLight = null;
    private boolean mAppSREScene;
    Data mData = HwSunlightReadabilityEnhancementXmlLoader.getData();
    private boolean mIsBrightnessUpdating;
    private boolean mLastAppSREScene;
    private int mLastOutdoorScence = 0;
    private HwLightSensorController mLightSensorController = null;
    private boolean mLightSensorEnable;
    private boolean mNeedUpdateSRE;
    private HwNormalizedSunlightReadabilityEnhancementOutdoorDetector mOutdoorDetector = null;
    private int mOutdoorScene;
    private ProcessTriggerHandler mProcessTriggerHandler = null;
    private Light mSRE = null;
    private int mSREAmbientLux;
    private boolean mSREEnable;
    private boolean mSREOn;
    private HandlerThread mSREPolicyProcessThread = null;
    private SceneRecognition mSceneRecognition = null;
    private int mSmoothAmbientLux;
    private long mUpdateSREBrightnessTimestamp = 0;
    private boolean mWarmUpFirstTime;

    private class ProcessTriggerHandler extends Handler {
        public ProcessTriggerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwNormalizedHiACELightController.this.processPolicy();
                    return;
                case 2:
                    HwNormalizedHiACELightController.this.uninitSRE();
                    return;
                case 3:
                    HwNormalizedHiACELightController.this.updateSREBrightness();
                    return;
                default:
                    Slog.e(HwNormalizedHiACELightController.TAG, "[effect] Invalid message");
                    return;
            }
        }
    }

    private final class SceneRecognition implements IPGPlugCallbacks {
        private static final String TAG = "SceneRecognition";
        private boolean mInCameraScene;
        private PGPlug mPGPlug = new PGPlug(this, TAG);

        public SceneRecognition(Context context) {
            Slog.i(TAG, "[effect] SceneRecognition");
            new Thread(this.mPGPlug, TAG).start();
        }

        public void cleanScene() {
            this.mInCameraScene = false;
        }

        public void onDaemonConnected() {
            Slog.i(TAG, "[effect] SceneRecognition Client Connected");
        }

        public boolean onEvent(int actionID, String value) {
            switch (actionID) {
                case IDisplayEngineService.DE_ACTION_PG_CAMERA_FRONT /*10007*/:
                    if (HwNormalizedHiACELightController.this.mData.sceneCameraEnable) {
                        this.mInCameraScene = true;
                        break;
                    }
                    break;
                case IDisplayEngineService.DE_ACTION_PG_CAMERA_END /*10017*/:
                    if (HwNormalizedHiACELightController.this.mData.sceneCameraEnable) {
                        this.mInCameraScene = false;
                        break;
                    }
                    break;
            }
            HwNormalizedHiACELightController.this.mAppSREScene = this.mInCameraScene;
            if (HwNormalizedHiACELightController.this.mLightSensorEnable && HwNormalizedHiACELightController.this.mSREEnable) {
                HwNormalizedHiACELightController.this.mProcessTriggerHandler.sendEmptyMessage(1);
            }
            return true;
        }

        public void onConnectedTimeout() {
            Slog.e(TAG, "[effect] error, SceneRecognition Client ConnectedTimeout!");
        }
    }

    public boolean start(Context context, SensorManager sensorManager) {
        if (context == null || sensorManager == null) {
            Slog.e(TAG, "[effect] context=" + context + ", sensorManager=" + sensorManager + ", start() failed!");
            return false;
        }
        LightsManager lightsManager = (LightsManager) LocalServices.getService(LightsManager.class);
        if (lightsManager == null) {
            Slog.e(TAG, "[effect] LightsManager is null, start() failed!");
            return false;
        }
        this.mAmbientLight = lightsManager.getLight(CommandsInterface.EMCOM_SD_STOP_UDP_RETRAN);
        this.mSRE = lightsManager.getLight(CommandsInterface.EMCOM_SD_XENGINE_CONFIG_MPIP);
        if (this.mAmbientLight == null || this.mSRE == null) {
            Slog.e(TAG, "[effect] mAmbientLight:" + this.mAmbientLight + ", mSRE:" + this.mSRE + ", start() failed!");
            return false;
        }
        try {
            this.mOutdoorDetector = new HwNormalizedSunlightReadabilityEnhancementOutdoorDetector();
            if (this.mData.sceneCameraEnable) {
                this.mSceneRecognition = new SceneRecognition(context);
            }
            this.mLightSensorController = new HwLightSensorController(this, sensorManager, this.mData.lightSensorRateMills);
            this.mSREPolicyProcessThread = new HandlerThread(TAG);
            this.mSREPolicyProcessThread.start();
            this.mProcessTriggerHandler = new ProcessTriggerHandler(this.mSREPolicyProcessThread.getLooper());
            return true;
        } catch (Exception e) {
            Slog.e(TAG, "[effect] start() failed!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkIfUsingBLC() {
        return this.mData.usingBLC;
    }

    public boolean checkIfUsingSRE() {
        return this.mData.usingSRE;
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

    private void disableSRE() {
        this.mProcessTriggerHandler.removeMessages(1);
        this.mProcessTriggerHandler.sendEmptyMessage(2);
    }

    public void updatePowerState(int state, boolean enable) {
        if (this.mSREEnable != enable) {
            this.mSREEnable = enable;
            if (!this.mSREEnable) {
                disableSRE();
            } else if (this.mOutdoorScene == 2 || this.mAppSREScene) {
                turnOnSRE();
            }
        }
        setLightSensorEnabled(wantScreenOn(state));
    }

    public void updateBrightnessState(int state) {
        if (this.mSREEnable) {
            if (state == 0) {
                if (!this.mIsBrightnessUpdating) {
                    this.mIsBrightnessUpdating = true;
                }
            } else if (this.mIsBrightnessUpdating) {
                this.mIsBrightnessUpdating = false;
                if (this.mSREOn) {
                    this.mProcessTriggerHandler.sendEmptyMessage(3);
                }
            }
            return;
        }
        if (this.mIsBrightnessUpdating) {
            this.mIsBrightnessUpdating = false;
        }
    }

    private void setLightSensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mLightSensorEnable) {
                this.mLightSensorEnable = true;
                this.mWarmUpFirstTime = true;
                this.mLightSensorController.enableSensor();
            }
        } else if (this.mLightSensorEnable) {
            this.mLightSensorEnable = false;
            this.mLightSensorController.disableSensor();
            disableSRE();
            this.mOutdoorDetector.clearAmbientLightRingBuffer();
        }
    }

    private int getAmbientThreashold() {
        return this.mAppSREScene ? 0 : this.mOutdoorDetector.getAmbientThresholdForSRE();
    }

    private void uninitSRE() {
        this.mSREOn = false;
        this.mSRE.sendSREWithRefreshFrames(0, 0, 0, 0, false, 0, 0);
        if (this.mSceneRecognition != null) {
            this.mSceneRecognition.cleanScene();
        }
        this.mOutdoorScene = 0;
        this.mLastOutdoorScence = 0;
        this.mAppSREScene = false;
        this.mLastAppSREScene = false;
        this.mIsBrightnessUpdating = false;
    }

    private void turnOnSRE() {
        if (!this.mSREOn) {
            this.mSREOn = true;
            this.mSRE.sendSREWithRefreshFrames(1, getAmbientThreashold(), this.mSREAmbientLux, 100, false, 0, 0);
        }
    }

    private void turnOffSRE() {
        if (this.mSREOn) {
            this.mSREOn = false;
            this.mSRE.sendSREWithRefreshFrames(1, getAmbientThreashold(), this.mSREAmbientLux, 100, true, 0, 0);
        }
    }

    private void updateSRELux() {
        if (this.mSREOn) {
            this.mSRE.sendSREWithRefreshFrames(1, getAmbientThreashold(), this.mSREAmbientLux, 100, false, 0, 0);
        }
    }

    private void updateSREBrightness() {
        if (this.mSREOn) {
            long time = SystemClock.elapsedRealtime();
            if (this.mUpdateSREBrightnessTimestamp == 0 || time - this.mUpdateSREBrightnessTimestamp >= 500) {
                this.mUpdateSREBrightnessTimestamp = time;
                this.mSRE.sendSREWithRefreshFrames(1, getAmbientThreashold(), this.mSREAmbientLux, 100, false, 0, 0);
            }
        }
    }

    private void processPolicy() {
        if (needMakePolicy()) {
            if (!this.mWarmUpFirstTime) {
                makePolicy();
            } else if (this.mOutdoorScene != 0) {
                this.mWarmUpFirstTime = false;
                if (this.mOutdoorScene == 2 || this.mAppSREScene) {
                    turnOnSRE();
                }
            } else {
                return;
            }
            this.mNeedUpdateSRE = false;
            this.mLastOutdoorScence = this.mOutdoorScene;
            this.mLastAppSREScene = this.mAppSREScene;
        }
    }

    private boolean needMakePolicy() {
        if (this.mOutdoorScene != this.mLastOutdoorScence || this.mAppSREScene != this.mLastAppSREScene || this.mNeedUpdateSRE) {
            return true;
        }
        if (this.mIsBrightnessUpdating && this.mSREOn) {
            return true;
        }
        return false;
    }

    private void makePolicy() {
        if (this.mLastOutdoorScence == 1 && this.mOutdoorScene == 2) {
            policyIndoorToOutdoor();
        } else if (this.mLastOutdoorScence == 2 && this.mOutdoorScene == 1) {
            policyOutdoorToIndoor();
        } else if (!this.mLastAppSREScene && this.mAppSREScene) {
            policyEnterAppScene();
        } else if (this.mLastAppSREScene && (this.mAppSREScene ^ 1) != 0) {
            policyExitAppScene();
        } else if (this.mNeedUpdateSRE) {
            policyLuxChanged();
        } else if (this.mIsBrightnessUpdating) {
            policySceenBrightnessChanged();
        }
    }

    private void policyIndoorToOutdoor() {
        if (this.mAppSREScene) {
            updateSRELux();
        } else {
            turnOnSRE();
        }
    }

    private void policyOutdoorToIndoor() {
        if (this.mAppSREScene) {
            updateSRELux();
        } else if (this.mSREOn) {
            turnOffSRE();
        }
    }

    private void policyEnterAppScene() {
        if (this.mOutdoorScene == 1 && !this.mSREOn) {
            turnOnSRE();
        }
    }

    private void policyExitAppScene() {
        if (this.mOutdoorScene == 1 && this.mSREOn) {
            turnOffSRE();
        }
    }

    private void policyLuxChanged() {
        if (this.mSREOn) {
            updateSRELux();
        }
    }

    private void policySceenBrightnessChanged() {
        if (this.mSREOn) {
            updateSREBrightness();
        }
    }

    public void processSensorData(long timeInMs, int lux, int cct) {
        this.mOutdoorDetector.handleLightSensorEvent(timeInMs, (float) lux);
        this.mOutdoorScene = this.mOutdoorDetector.getIndoorOutdoorFlagForSRE();
        this.mNeedUpdateSRE = this.mOutdoorDetector.getLuxChangedFlagForSRE();
        this.mSmoothAmbientLux = (int) this.mOutdoorDetector.getAmbientLux();
        this.mSREAmbientLux = (int) this.mOutdoorDetector.getAmbientLuxForSRE();
        if (this.mLightSensorEnable && this.mSREEnable) {
            this.mProcessTriggerHandler.sendEmptyMessage(1);
        }
        this.mAmbientLight.sendAmbientLight(this.mSmoothAmbientLux);
    }
}
