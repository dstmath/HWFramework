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
import com.android.server.display.HwLightSensorListener;
import com.android.server.display.HwSmartBackLightXmlLoader;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.lights.LightsManagerEx;

public class HwSmartBackLightNormalizedController implements HwLightSensorListener.LightSensorCallbacks {
    public static final int BRIGHTNESS_UPDATE_END = 1;
    public static final int BRIGHTNESS_UPDATE_START = 0;
    private static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
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
    private final HwSmartBackLightXmlLoader.Data mData;
    private boolean mIsBrightnessUpdating;
    private boolean mLastAppSBLScene;
    private int mLastOutdoorScence = 0;
    private boolean mLightSensorEnable;
    private final HwLightSensorListener mLightSensorListener;
    private final LightsManager mLightsManager;
    private boolean mNeedUpdateSBL;
    private HwSmartBackLightOutdoorNormalizedDetector mOutdoorDetector;
    private int mOutdoorScene;
    private final ProcessTriggerHandler mProcessTriggerHandler;
    private final HandlerThread mSBLPolicyProcessThread;
    private Light mSmartBackLight;
    private boolean mSmartBackLightEnable;
    private boolean mSmartBackLightOn;
    private int mSmoothAmbientLux;
    private long mUpdateSBLBrightnessTimestamp;
    private boolean mVideoSceneEntered = false;
    private boolean mWarmUpFirstTime;

    public HwSmartBackLightNormalizedController(Context context, LightsManager lightsManager, SensorManager sensorManager) {
        this.mLightsManager = lightsManager;
        this.mData = HwSmartBackLightXmlLoader.getData();
        this.mSmartBackLight = this.mLightsManager.getLight((int) LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT);
        this.mOutdoorDetector = new HwSmartBackLightOutdoorNormalizedDetector();
        isSceneRecognitionEnable();
        this.mLightSensorListener = new HwLightSensorListener(context, this, sensorManager, this.mData.lightSensorRateMills);
        this.mSBLPolicyProcessThread = new HandlerThread(TAG);
        this.mSBLPolicyProcessThread.start();
        this.mProcessTriggerHandler = new ProcessTriggerHandler(this.mSBLPolicyProcessThread.getLooper());
    }

    private boolean isSceneRecognitionEnable() {
        return this.mData.sceneVideoEnable || this.mData.sceneGalleryEnable || this.mData.sceneCameraEnable;
    }

    public static boolean checkIfUsingHwSBL() {
        return false;
    }

    private static boolean wantScreenOn(int state) {
        if (state == 2 || state == 3) {
            return true;
        }
        return false;
    }

    public void updatePowerState(int state, boolean useSmartBacklight) {
        if (this.mSmartBackLightEnable || useSmartBacklight) {
            if (this.mSmartBackLightEnable != useSmartBacklight) {
                if (DEBUG) {
                    Slog.i(TAG, "mSmartBackLightEnable change " + this.mSmartBackLightEnable + " -> " + useSmartBacklight);
                }
                this.mSmartBackLightEnable = useSmartBacklight;
            }
            setLightSensorEnabled(wantScreenOn(state) && useSmartBacklight);
        }
    }

    public void updateBrightnessState(int state) {
        if (!this.mSmartBackLightEnable) {
            Slog.w(TAG, "updateBrightnessState sbl didn't enable");
            if (this.mIsBrightnessUpdating) {
                this.mIsBrightnessUpdating = false;
                if (DEBUG) {
                    Slog.i(TAG, "updateBrightnessState clear mIsBrightnessUpdating");
                }
            }
        } else if (state == 0) {
            if (!this.mIsBrightnessUpdating) {
                this.mIsBrightnessUpdating = true;
            }
        } else if (this.mIsBrightnessUpdating) {
            this.mIsBrightnessUpdating = false;
            if (this.mSmartBackLightOn) {
                this.mProcessTriggerHandler.sendEmptyMessage(3);
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
                this.mLightSensorListener.enableSensor();
            }
        } else if (this.mLightSensorEnable) {
            this.mLightSensorEnable = false;
            if (DEBUG) {
                Slog.i(TAG, "light sensor close");
            }
            this.mLightSensorListener.disableSensor();
            this.mProcessTriggerHandler.removeMessages(1);
            this.mProcessTriggerHandler.sendEmptyMessage(2);
            this.mOutdoorDetector.clearAmbientLightRingBuffer();
        }
    }

    private void initSBL() {
        if (!this.mSmartBackLightOn) {
            if (DEBUG) {
                Slog.i(TAG, "initSBL");
            }
            this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(0, this.mData.apicalADLevel, 0, 0, false, 0, 0);
            return;
        }
        Slog.w(TAG, "initSBL but sbl is already on");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uninitSBL() {
        this.mSmartBackLightOn = false;
        if (DEBUG) {
            Slog.i(TAG, "uninitSBL");
        }
        this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(0, this.mData.apicalADLevel, 0, 0, false, 0, 0);
        this.mOutdoorScene = 0;
        this.mLastOutdoorScence = 0;
        this.mAppSBLScene = false;
        this.mLastAppSBLScene = false;
        this.mIsBrightnessUpdating = false;
    }

    private void turnOnSBL() {
        if (!this.mSmartBackLightOn) {
            this.mSmartBackLightOn = true;
            if (DEBUG) {
                Slog.i(TAG, "turnOnSBL, mSmoothAmbientLux = " + this.mSmoothAmbientLux);
            }
            this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(1, getApicalADLevel(), getSmoothAmbientLux(), 100, false, 0, 0);
            return;
        }
        Slog.w(TAG, "turnOnSBL but sbl is already on");
    }

    private void turnOffSBL() {
        if (this.mSmartBackLightOn) {
            this.mSmartBackLightOn = false;
            if (DEBUG) {
                Slog.i(TAG, "turnOffSBL");
            }
            if (!this.mLastAppSBLScene || this.mAppSBLScene || !this.mData.videoSceneEnhanceEnabled) {
                this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(1, this.mData.apicalADLevel, 0, 100, true, 0, 0);
            } else {
                this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(0, this.mData.apicalADLevel, 0, 100, false, 0, 0);
            }
        } else {
            Slog.w(TAG, "turnOffSBL but sbl is already off");
        }
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSBLBrightness() {
        if (this.mSmartBackLightOn) {
            long time = SystemClock.elapsedRealtime();
            long j = this.mUpdateSBLBrightnessTimestamp;
            if (j == 0 || time - j >= 500) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processPolicy() {
        if (needMakePolicy()) {
            boolean isOutdoor = this.mOutdoorScene == 2;
            if (DEBUG && !this.mIsBrightnessUpdating) {
                Slog.i(TAG, "OutdoorScence = " + isOutdoor + ", AppSBLScene = " + this.mAppSBLScene + ", UpdateSBL = " + this.mNeedUpdateSBL + ", CurrentLux = " + this.mCurrentLux + ", SmoothLux = " + this.mSmoothAmbientLux + ", UpdataBright = " + this.mIsBrightnessUpdating);
            }
            if (!this.mWarmUpFirstTime) {
                makePolicy();
            } else if (this.mOutdoorScene != 0) {
                this.mWarmUpFirstTime = false;
                makeWarmUpPolicy();
            } else if (DEBUG) {
                Slog.i(TAG, "WARM UP, wait outdoor scene detect result");
                return;
            } else {
                return;
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
        if (!this.mIsBrightnessUpdating || !this.mSmartBackLightOn) {
            return false;
        }
        return true;
    }

    private void makeWarmUpPolicy() {
        int i = this.mOutdoorScene;
        if (i == 2 || (i == 1 && this.mAppSBLScene)) {
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
        } else if (this.mLastAppSBLScene && !this.mAppSBLScene) {
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
        } else if (!this.mSmartBackLightOn) {
            if (DEBUG) {
                Slog.i(TAG, "INDOOR -> OUTDOOR, enableSBL");
            }
            turnOnSBL();
        } else {
            Slog.w(TAG, "INDOOR -> OUTDOOR, not in app SBL scene, but SBL already on, error!");
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
        if (!this.mSmartBackLightOn) {
            if (DEBUG) {
                Slog.i(TAG, "INDOOR, enter app SBL scene, turnOnSBL");
            }
            turnOnSBL();
            return;
        }
        Slog.w(TAG, "INDOOR, wasn't app SBL scene, but SBL was on, error!");
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

    /* access modifiers changed from: private */
    public class ProcessTriggerHandler extends Handler {
        public ProcessTriggerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwSmartBackLightNormalizedController.this.processPolicy();
            } else if (i == 2) {
                HwSmartBackLightNormalizedController.this.uninitSBL();
            } else if (i != 3) {
                Slog.e(HwSmartBackLightNormalizedController.TAG, "Invalid message");
            } else {
                HwSmartBackLightNormalizedController.this.updateSBLBrightness();
            }
        }
    }

    @Override // com.android.server.display.HwLightSensorListener.LightSensorCallbacks
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
