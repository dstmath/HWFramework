package com.android.server.display;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import android.util.Xml;
import com.android.server.LocalServices;
import com.android.server.display.HwLightSensorController.LightSensorCallbacks;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGPlug;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwNormalizedHiACELightController implements LightSensorCallbacks {
    public static final int BRIGHTNESS_UPDATE_END = 1;
    public static final int BRIGHTNESS_UPDATE_START = 0;
    private static final int DEFAULT = 0;
    private static final String HIACE_CONFIG_FILE = "/xml/lcd/HiACEConfig.xml";
    private static final int INDOOR = 1;
    private static final int LIGHT_SENSOR_RATE_MILLIS = 16;
    private static final int MSG_PROCESS_POLICY = 1;
    private static final int MSG_UNINIT_SRE = 2;
    private static final int MSG_UPDATE_SRE_BRIGHTNESS = 3;
    private static final int OUTDOOR = 2;
    private static final int REFRESH_FRAMES_NUM = 100;
    private static String TAG = null;
    private static final int UPDATE_BRIGHTNESS_INTERVAL = 500;
    private Light mAmbientLight;
    private boolean mAppSREScene;
    private String mConfigFilePath;
    private boolean mHasHiACE;
    private boolean mIsBrightnessUpdating;
    private boolean mLastAppSREScene;
    private int mLastOutdoorScence;
    private HwLightSensorController mLightSensorController;
    private boolean mLightSensorEnable;
    private int mLightSensorRateMillis;
    private boolean mNeedUpdateSRE;
    private HwNormalizedSunlightReadabilityEnhancementOutdoorDetector mOutdoorDetector;
    private int mOutdoorScene;
    private ProcessTriggerHandler mProcessTriggerHandler;
    private Light mSRE;
    private int mSREAmbientLux;
    private boolean mSREEnable;
    private boolean mSREOn;
    private HandlerThread mSREPolicyProcessThread;
    private boolean mSceneCameraEnable;
    private SceneRecognition mSceneRecognition;
    private boolean mSceneRecognitionEnable;
    private int mSmoothAmbientLux;
    private long mUpdateSREBrightnessTimestamp;
    private boolean mUsingBLC;
    private boolean mUsingSRE;
    private boolean mWarmUpFirstTime;

    private class ProcessTriggerHandler extends Handler {
        public ProcessTriggerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwNormalizedHiACELightController.MSG_PROCESS_POLICY /*1*/:
                    HwNormalizedHiACELightController.this.processPolicy();
                case HwNormalizedHiACELightController.OUTDOOR /*2*/:
                    HwNormalizedHiACELightController.this.uninitSRE();
                case HwNormalizedHiACELightController.MSG_UPDATE_SRE_BRIGHTNESS /*3*/:
                    HwNormalizedHiACELightController.this.updateSREBrightness();
                default:
                    Slog.e(HwNormalizedHiACELightController.TAG, "[effect] Invalid message");
            }
        }
    }

    private final class SceneRecognition implements IPGPlugCallbacks {
        private static final String TAG = "SceneRecognition";
        private boolean mInCameraScene;
        private PGPlug mPGPlug;

        public SceneRecognition(Context context) {
            Slog.i(TAG, "[effect] SceneRecognition");
            this.mPGPlug = new PGPlug(this, TAG);
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
                case 10007:
                    if (HwNormalizedHiACELightController.this.mSceneCameraEnable) {
                        this.mInCameraScene = true;
                        break;
                    }
                    break;
                case 10017:
                    if (HwNormalizedHiACELightController.this.mSceneCameraEnable) {
                        this.mInCameraScene = false;
                        break;
                    }
                    break;
            }
            HwNormalizedHiACELightController.this.mAppSREScene = this.mInCameraScene;
            if (HwNormalizedHiACELightController.this.mLightSensorEnable && HwNormalizedHiACELightController.this.mSREEnable) {
                HwNormalizedHiACELightController.this.mProcessTriggerHandler.sendEmptyMessage(HwNormalizedHiACELightController.MSG_PROCESS_POLICY);
            }
            return true;
        }

        public void onConnectedTimeout() {
            Slog.e(TAG, "[effect] error, SceneRecognition Client ConnectedTimeout!");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.HwNormalizedHiACELightController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.HwNormalizedHiACELightController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.HwNormalizedHiACELightController.<clinit>():void");
    }

    public HwNormalizedHiACELightController() {
        this.mAmbientLight = null;
        this.mSRE = null;
        this.mLightSensorController = null;
        this.mOutdoorDetector = null;
        this.mLastOutdoorScence = DEFAULT;
        this.mSceneRecognition = null;
        this.mSceneRecognitionEnable = false;
        this.mSREPolicyProcessThread = null;
        this.mProcessTriggerHandler = null;
        this.mUpdateSREBrightnessTimestamp = 0;
        this.mConfigFilePath = null;
        this.mHasHiACE = false;
        this.mUsingBLC = false;
        this.mUsingSRE = false;
        try {
            if (!getConfig()) {
                if (this.mHasHiACE) {
                    Slog.e(TAG, "[effect] getConfig failed! loadDefaultConfig");
                    loadDefaultConfig();
                    return;
                }
                clearConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
            clearConfig();
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
        this.mAmbientLight = lightsManager.getLight(12);
        this.mSRE = lightsManager.getLight(13);
        if (this.mAmbientLight == null || this.mSRE == null) {
            Slog.e(TAG, "[effect] mAmbientLight:" + this.mAmbientLight + ", mSRE:" + this.mSRE + ", start() failed!");
            return false;
        }
        try {
            this.mOutdoorDetector = new HwNormalizedSunlightReadabilityEnhancementOutdoorDetector(this.mConfigFilePath);
            if (this.mSceneRecognitionEnable) {
                this.mSceneRecognition = new SceneRecognition(context);
            }
            this.mLightSensorController = new HwLightSensorController(this, sensorManager, this.mLightSensorRateMillis);
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
        return this.mUsingBLC;
    }

    public boolean checkIfUsingSRE() {
        return this.mUsingSRE;
    }

    private static boolean wantScreenOn(int state) {
        switch (state) {
            case OUTDOOR /*2*/:
            case MSG_UPDATE_SRE_BRIGHTNESS /*3*/:
                return true;
            default:
                return false;
        }
    }

    private void disableSRE() {
        this.mProcessTriggerHandler.removeMessages(MSG_PROCESS_POLICY);
        this.mProcessTriggerHandler.sendEmptyMessage(OUTDOOR);
    }

    public void updatePowerState(int state, boolean enable) {
        if (this.mSREEnable != enable) {
            this.mSREEnable = enable;
            if (!this.mSREEnable) {
                disableSRE();
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
                    this.mProcessTriggerHandler.sendEmptyMessage(MSG_UPDATE_SRE_BRIGHTNESS);
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
        return this.mAppSREScene ? DEFAULT : this.mOutdoorDetector.getAmbientThresholdForSRE();
    }

    private void uninitSRE() {
        this.mSREOn = false;
        this.mSRE.sendSREWithRefreshFrames(DEFAULT, DEFAULT, DEFAULT, DEFAULT, false, DEFAULT, DEFAULT);
        if (this.mSceneRecognition != null) {
            this.mSceneRecognition.cleanScene();
        }
        this.mOutdoorScene = DEFAULT;
        this.mLastOutdoorScence = DEFAULT;
        this.mAppSREScene = false;
        this.mLastAppSREScene = false;
        this.mIsBrightnessUpdating = false;
    }

    private void turnOnSRE() {
        if (!this.mSREOn) {
            this.mSREOn = true;
            this.mSRE.sendSREWithRefreshFrames(MSG_PROCESS_POLICY, getAmbientThreashold(), this.mSREAmbientLux, REFRESH_FRAMES_NUM, false, DEFAULT, DEFAULT);
        }
    }

    private void turnOffSRE() {
        if (this.mSREOn) {
            this.mSREOn = false;
            this.mSRE.sendSREWithRefreshFrames(MSG_PROCESS_POLICY, getAmbientThreashold(), this.mSREAmbientLux, REFRESH_FRAMES_NUM, true, DEFAULT, DEFAULT);
        }
    }

    private void updateSRELux() {
        if (this.mSREOn) {
            this.mSRE.sendSREWithRefreshFrames(MSG_PROCESS_POLICY, getAmbientThreashold(), this.mSREAmbientLux, REFRESH_FRAMES_NUM, false, DEFAULT, DEFAULT);
        }
    }

    private void updateSREBrightness() {
        if (this.mSREOn) {
            long time = SystemClock.elapsedRealtime();
            if (this.mUpdateSREBrightnessTimestamp == 0 || time - this.mUpdateSREBrightnessTimestamp >= 500) {
                this.mUpdateSREBrightnessTimestamp = time;
                this.mSRE.sendSREWithRefreshFrames(MSG_PROCESS_POLICY, getAmbientThreashold(), this.mSREAmbientLux, REFRESH_FRAMES_NUM, false, DEFAULT, DEFAULT);
            }
        }
    }

    private void processPolicy() {
        if (this.mOutdoorScene == this.mLastOutdoorScence && this.mAppSREScene == this.mLastAppSREScene && !this.mNeedUpdateSRE && (!this.mIsBrightnessUpdating || !this.mSREOn)) {
            return;
        }
        if (!this.mWarmUpFirstTime) {
            if (this.mLastOutdoorScence == MSG_PROCESS_POLICY && this.mOutdoorScene == OUTDOOR) {
                if (this.mAppSREScene) {
                    updateSRELux();
                } else {
                    turnOnSRE();
                }
            } else if (this.mLastOutdoorScence == OUTDOOR && this.mOutdoorScene == MSG_PROCESS_POLICY) {
                if (this.mAppSREScene) {
                    updateSRELux();
                } else if (this.mSREOn) {
                    turnOffSRE();
                }
            } else if (this.mLastAppSREScene || !this.mAppSREScene) {
                if (!this.mLastAppSREScene || this.mAppSREScene) {
                    if (this.mNeedUpdateSRE) {
                        if (this.mSREOn) {
                            updateSRELux();
                        }
                    } else if (this.mIsBrightnessUpdating && this.mSREOn) {
                        updateSREBrightness();
                    }
                } else if (this.mOutdoorScene == MSG_PROCESS_POLICY && this.mSREOn) {
                    turnOffSRE();
                }
            } else if (this.mOutdoorScene == MSG_PROCESS_POLICY && !this.mSREOn) {
                turnOnSRE();
            }
            this.mNeedUpdateSRE = false;
            this.mLastOutdoorScence = this.mOutdoorScene;
            this.mLastAppSREScene = this.mAppSREScene;
        } else if (this.mOutdoorScene != 0) {
            this.mWarmUpFirstTime = false;
            if (this.mOutdoorScene == OUTDOOR || this.mAppSREScene) {
                turnOnSRE();
            }
            this.mNeedUpdateSRE = false;
            this.mLastOutdoorScence = this.mOutdoorScene;
            this.mLastAppSREScene = this.mAppSREScene;
        }
    }

    public void processSensorData(long timeInMs, int lux) {
        this.mOutdoorDetector.handleLightSensorEvent(timeInMs, (float) lux);
        this.mOutdoorScene = this.mOutdoorDetector.getIndoorOutdoorFlagForSRE();
        this.mNeedUpdateSRE = this.mOutdoorDetector.getLuxChangedFlagForSRE();
        this.mSmoothAmbientLux = (int) this.mOutdoorDetector.getAmbientLux();
        this.mSREAmbientLux = (int) this.mOutdoorDetector.getAmbientLuxForSRE();
        if (this.mLightSensorEnable && this.mSREEnable) {
            this.mProcessTriggerHandler.sendEmptyMessage(MSG_PROCESS_POLICY);
        }
        this.mAmbientLight.sendAmbientLight(this.mSmoothAmbientLux);
    }

    private boolean getConfig() throws IOException {
        FileNotFoundException e;
        IOException e2;
        Exception e3;
        Throwable th;
        File xmlFile = HwCfgFilePolicy.getCfgFile(HIACE_CONFIG_FILE, DEFAULT);
        if (xmlFile == null) {
            return false;
        }
        this.mHasHiACE = true;
        boolean ret = false;
        FileInputStream fileInputStream = null;
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            try {
                if (getConfigFromXML(inputStream)) {
                    this.mConfigFilePath = xmlFile.getAbsolutePath();
                    Slog.i(TAG, "[effect] get xmlFile :" + this.mConfigFilePath);
                    ret = true;
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                fileInputStream = inputStream;
            } catch (FileNotFoundException e4) {
                e = e4;
                fileInputStream = inputStream;
                e.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return ret;
            } catch (IOException e5) {
                e2 = e5;
                fileInputStream = inputStream;
                e2.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return ret;
            } catch (Exception e6) {
                e3 = e6;
                fileInputStream = inputStream;
                try {
                    e3.printStackTrace();
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return ret;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = inputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return ret;
        } catch (IOException e8) {
            e2 = e8;
            e2.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return ret;
        } catch (Exception e9) {
            e3 = e9;
            e3.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return ret;
        }
        return ret;
    }

    private void loadDefaultConfig() {
        Slog.i(TAG, "[effect] loadDefaultConfig");
        this.mLightSensorRateMillis = LIGHT_SENSOR_RATE_MILLIS;
        this.mSceneRecognitionEnable = true;
        this.mSceneCameraEnable = true;
        this.mUsingBLC = true;
        this.mUsingSRE = true;
    }

    private void clearConfig() {
        Slog.i(TAG, "[effect] clearConfig");
        this.mLightSensorRateMillis = LIGHT_SENSOR_RATE_MILLIS;
        this.mSceneRecognitionEnable = false;
        this.mSceneCameraEnable = false;
        this.mUsingBLC = false;
        this.mUsingSRE = false;
    }

    private boolean getConfigFromXML(InputStream inStream) {
        boolean lightSensorRateMillsLoaded = false;
        boolean sceneRecognitionLoaded = false;
        boolean sceneRecognitionLoadStarted = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != MSG_PROCESS_POLICY; eventType = parser.next()) {
                String name;
                switch (eventType) {
                    case OUTDOOR /*2*/:
                        name = parser.getName();
                        if (name != null && name.length() != 0) {
                            boolean z;
                            if (!name.equals("UsingBLC")) {
                                if (!name.equals("UsingSRE")) {
                                    if (!name.equals("LightSensorRateMills")) {
                                        if (!name.equals("SceneRecognition")) {
                                            if (name.equals("SceneCamera") && sceneRecognitionLoadStarted) {
                                                this.mSceneCameraEnable = Boolean.valueOf(parser.nextText()).booleanValue();
                                                break;
                                            }
                                        }
                                        sceneRecognitionLoadStarted = true;
                                        break;
                                    }
                                    this.mLightSensorRateMillis = Integer.parseInt(parser.nextText());
                                    lightSensorRateMillsLoaded = true;
                                    break;
                                }
                                if (Integer.parseInt(parser.nextText()) == MSG_PROCESS_POLICY) {
                                    z = true;
                                } else {
                                    z = false;
                                }
                                this.mUsingSRE = z;
                                break;
                            }
                            if (Integer.parseInt(parser.nextText()) == MSG_PROCESS_POLICY) {
                                z = true;
                            } else {
                                z = false;
                            }
                            this.mUsingBLC = z;
                            break;
                        }
                        return false;
                        break;
                    case MSG_UPDATE_SRE_BRIGHTNESS /*3*/:
                        name = parser.getName();
                        if (name != null && name.length() != 0) {
                            if (!name.equals("SceneRecognition")) {
                                break;
                            }
                            sceneRecognitionLoadStarted = false;
                            sceneRecognitionLoaded = true;
                            if (!this.mSceneCameraEnable) {
                                break;
                            }
                            this.mSceneRecognitionEnable = true;
                            break;
                        }
                        return false;
                        break;
                    default:
                        break;
                }
            }
            if (lightSensorRateMillsLoaded && sceneRecognitionLoaded) {
                Slog.i(TAG, "[effect] getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        Slog.e(TAG, "[effect] getConfigFromeXML failed!");
        return false;
    }
}
