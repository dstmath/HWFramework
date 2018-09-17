package com.android.server.display;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import com.android.server.am.HwActivityManagerService;
import com.android.server.display.HwLightSensorController.LightSensorCallbacks;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.wifipro.WifiProCHRManager;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwSmartBackLightNormalizedController implements LightSensorCallbacks {
    public static final int BRIGHTNESS_UPDATE_END = 1;
    public static final int BRIGHTNESS_UPDATE_START = 0;
    private static boolean DEBUG = false;
    private static final int DEFAULT = 0;
    private static final int DEFAULT_APICAL_LEVEL = 128;
    private static final String HW_SBL_CONFIG_FILE = "SBLConfig.xml";
    private static final int INDOOR = 1;
    private static final int LIGHT_SENSOR_RATE_MILLIS = 300;
    private static final int MSG_PROCESS_POLICY = 1;
    private static final int MSG_UNINIT_SBL = 2;
    private static final int MSG_UPDATE_SBL_BRIGHTNESS = 3;
    private static final int OUTDOOR = 2;
    private static final int REFRESH_FRAMES_NUM = 100;
    private static String TAG = null;
    private static final int UPDATE_BRIGHTNESS_INTERVAL = 500;
    private int mApicalADLevel;
    private boolean mAppSBLScene;
    private String mConfigFilePath;
    private int mCurrentLux;
    private int mDarknessApicalADLevel;
    private int mDarknessLuxShift;
    private int mDarknessThreshold;
    private int mIndoorApicalADLevel;
    private int mIndoorLuxShift;
    private int mIndoorThreshold;
    private boolean mIsBrightnessUpdating;
    private boolean mLastAppSBLScene;
    private int mLastOutdoorScence;
    private final HwLightSensorController mLightSensorController;
    private boolean mLightSensorEnable;
    private int mLightSensorRateMillis;
    private final LightsManager mLightsManager;
    private boolean mNeedUpdateSBL;
    private int mOutdoorApicalADLevel;
    private HwSmartBackLightOutdoorNormalizedDetector mOutdoorDetector;
    private int mOutdoorLuxShift;
    private int mOutdoorScene;
    private final ProcessTriggerHandler mProcessTriggerHandler;
    private final HandlerThread mSBLPolicyProcessThread;
    private boolean mSceneCameraEnable;
    private boolean mSceneGalleryEnable;
    private SceneRecognition mSceneRecognition;
    private boolean mSceneRecognitionEnable;
    private boolean mSceneVideoEnable;
    private Light mSmartBackLight;
    private boolean mSmartBackLightEnable;
    private boolean mSmartBackLightOn;
    private int mSmoothAmbientLux;
    private long mUpdateSBLBrightnessTimestamp;
    private boolean mVideoSceneEnhanceEnabled;
    private boolean mVideoSceneEntered;
    private boolean mWarmUpFirstTime;

    private class ProcessTriggerHandler extends Handler {
        public ProcessTriggerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwSmartBackLightNormalizedController.MSG_PROCESS_POLICY /*1*/:
                    HwSmartBackLightNormalizedController.this.processPolicy();
                case HwSmartBackLightNormalizedController.OUTDOOR /*2*/:
                    HwSmartBackLightNormalizedController.this.uninitSBL();
                case HwSmartBackLightNormalizedController.MSG_UPDATE_SBL_BRIGHTNESS /*3*/:
                    HwSmartBackLightNormalizedController.this.updateSBLBrightness();
                default:
                    Slog.e(HwSmartBackLightNormalizedController.TAG, "Invalid message");
            }
        }
    }

    private final class SceneRecognition implements IPGPlugCallbacks {
        private static final String TAG = "SceneRecognition";
        private boolean mInCameraScene;
        private boolean mInGalleryScene;
        private boolean mInVideoScene;
        private PGPlug mPGPlug;

        public SceneRecognition(Context context) {
            Slog.i(TAG, TAG);
            this.mPGPlug = new PGPlug(this, TAG);
            new Thread(this.mPGPlug, TAG).start();
        }

        public void cleanScene() {
            this.mInVideoScene = false;
            this.mInCameraScene = false;
            this.mInGalleryScene = false;
        }

        public void onDaemonConnected() {
            Slog.i(TAG, "SceneRecognition Client Connected");
        }

        public boolean onEvent(int actionID, String value) {
            switch (actionID) {
                case 10004:
                    if (HwSmartBackLightNormalizedController.this.mSceneGalleryEnable) {
                        if (HwSmartBackLightNormalizedController.DEBUG) {
                            Slog.d(TAG, "PG_ID_GALLERY_FRONT");
                        }
                        this.mInGalleryScene = true;
                        break;
                    }
                    break;
                case 10007:
                    if (HwSmartBackLightNormalizedController.this.mSceneCameraEnable) {
                        if (HwSmartBackLightNormalizedController.DEBUG) {
                            Slog.d(TAG, "PG_ID_CAMERA_FRONT");
                        }
                        this.mInCameraScene = true;
                        break;
                    }
                    break;
                case 10015:
                    if (HwSmartBackLightNormalizedController.this.mSceneVideoEnable) {
                        if (HwSmartBackLightNormalizedController.DEBUG) {
                            Slog.d(TAG, "PG_ID_VIDEO_START");
                        }
                        this.mInVideoScene = true;
                        break;
                    }
                    break;
                case 10016:
                    if (HwSmartBackLightNormalizedController.this.mSceneVideoEnable) {
                        if (HwSmartBackLightNormalizedController.DEBUG) {
                            Slog.d(TAG, "PG_ID_VIDEO_END");
                        }
                        this.mInVideoScene = false;
                        break;
                    }
                    break;
                case 10017:
                    if (HwSmartBackLightNormalizedController.this.mSceneCameraEnable) {
                        if (HwSmartBackLightNormalizedController.DEBUG) {
                            Slog.d(TAG, "PG_ID_CAMERA_END");
                        }
                        this.mInCameraScene = false;
                        break;
                    }
                    break;
                default:
                    if (HwSmartBackLightNormalizedController.this.mSceneGalleryEnable && this.mInGalleryScene && HwSmartBackLightNormalizedController.MSG_PROCESS_POLICY == PGAction.checkActionType(actionID)) {
                        if (HwSmartBackLightNormalizedController.DEBUG) {
                            Slog.d(TAG, "PG_ID_GALLERY_END");
                        }
                        this.mInGalleryScene = false;
                        break;
                    }
            }
            HwSmartBackLightNormalizedController hwSmartBackLightNormalizedController = HwSmartBackLightNormalizedController.this;
            boolean z = (this.mInVideoScene || this.mInCameraScene) ? true : this.mInGalleryScene;
            hwSmartBackLightNormalizedController.mAppSBLScene = z;
            HwSmartBackLightNormalizedController.this.mVideoSceneEntered = this.mInVideoScene;
            if (HwSmartBackLightNormalizedController.this.mLightSensorEnable) {
                HwSmartBackLightNormalizedController.this.mProcessTriggerHandler.sendEmptyMessage(HwSmartBackLightNormalizedController.MSG_PROCESS_POLICY);
            }
            return true;
        }

        public void onConnectedTimeout() {
            Slog.e(TAG, "error, SceneRecognition Client ConnectedTimeout!");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.HwSmartBackLightNormalizedController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.HwSmartBackLightNormalizedController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.HwSmartBackLightNormalizedController.<clinit>():void");
    }

    public HwSmartBackLightNormalizedController(Context context, LightsManager lightsManager, SensorManager sensorManager) {
        this.mLastOutdoorScence = DEFAULT;
        this.mConfigFilePath = null;
        this.mVideoSceneEnhanceEnabled = false;
        this.mVideoSceneEntered = false;
        this.mLightsManager = lightsManager;
        try {
            if (!getConfig()) {
                Slog.e(TAG, "getConfig failed! loadDefaultConfig");
                loadDefaultConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadDefaultConfig();
        }
        this.mSmartBackLight = this.mLightsManager.getLight(8);
        try {
            this.mOutdoorDetector = new HwSmartBackLightOutdoorNormalizedDetector(this.mConfigFilePath);
            if (this.mSceneRecognitionEnable) {
                this.mSceneRecognition = new SceneRecognition(context);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        this.mLightSensorController = new HwLightSensorController(this, sensorManager, this.mLightSensorRateMillis);
        this.mSBLPolicyProcessThread = new HandlerThread(TAG);
        this.mSBLPolicyProcessThread.start();
        this.mProcessTriggerHandler = new ProcessTriggerHandler(this.mSBLPolicyProcessThread.getLooper());
    }

    public static boolean checkIfUsingHwSBL() {
        return true;
    }

    private static boolean wantScreenOn(int state) {
        switch (state) {
            case OUTDOOR /*2*/:
            case MSG_UPDATE_SBL_BRIGHTNESS /*3*/:
                return true;
            default:
                return false;
        }
    }

    public void updatePowerState(int state, boolean useSmartBacklight) {
        if (this.mSmartBackLightEnable || useSmartBacklight) {
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
                    this.mProcessTriggerHandler.sendEmptyMessage(MSG_UPDATE_SBL_BRIGHTNESS);
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

    private boolean getConfig() throws IOException {
        Exception e;
        Throwable th;
        String version = SystemProperties.get("ro.build.version.emui", null);
        if (version == null || version.length() == 0) {
            Slog.e(TAG, "get ro.build.version.emui failed!");
            return false;
        }
        String[] versionSplited = version.split("EmotionUI_");
        if (versionSplited == null || versionSplited.length < OUTDOOR) {
            Slog.e(TAG, "split failed! version = " + version);
            return false;
        }
        String emuiVersion = version.split("EmotionUI_")[MSG_PROCESS_POLICY];
        if (emuiVersion == null || emuiVersion.length() == 0) {
            Slog.e(TAG, "get emuiVersion failed!");
            return false;
        }
        Object[] objArr = new Object[OUTDOOR];
        objArr[DEFAULT] = emuiVersion;
        objArr[MSG_PROCESS_POLICY] = HW_SBL_CONFIG_FILE;
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", objArr), DEFAULT);
        if (xmlFile == null) {
            objArr = new Object[MSG_PROCESS_POLICY];
            objArr[DEFAULT] = HW_SBL_CONFIG_FILE;
            String xmlPath = String.format("/xml/lcd/%s", objArr);
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, DEFAULT);
            if (xmlFile == null) {
                Slog.e(TAG, "get xmlFile :" + xmlPath + " failed!");
                return false;
            }
        }
        FileInputStream fileInputStream = null;
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            try {
                if (getConfigFromXML(inputStream)) {
                    if (DEBUG) {
                        printConfigFromXML();
                    }
                    this.mConfigFilePath = xmlFile.getAbsolutePath();
                    Slog.i(TAG, "get xmlFile :" + this.mConfigFilePath);
                    inputStream.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    return true;
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                fileInputStream = inputStream;
                return false;
            } catch (Exception e2) {
                e = e2;
                fileInputStream = inputStream;
                try {
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return false;
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
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        }
    }

    private void printConfigFromXML() {
        Slog.i(TAG, "LightSensorRateMills = " + this.mLightSensorRateMillis + "ms");
        Slog.i(TAG, "ApicalADLevel = " + this.mApicalADLevel);
        Slog.i(TAG, "mSceneRecognitionEnable = " + this.mSceneRecognitionEnable);
        if (this.mSceneRecognitionEnable) {
            Slog.i(TAG, "mSceneVideoEnable = " + this.mSceneVideoEnable);
            Slog.i(TAG, "mSceneGalleryEnable = " + this.mSceneGalleryEnable);
            Slog.i(TAG, "mSceneCameraEnable = " + this.mSceneCameraEnable);
        }
        if (this.mVideoSceneEnhanceEnabled) {
            Slog.i(TAG, "mDarknessThreshold = " + this.mDarknessThreshold);
            Slog.i(TAG, "mIndoorThreshold = " + this.mIndoorThreshold);
            Slog.i(TAG, "mDarknessApicalADLevel = " + this.mDarknessApicalADLevel);
            Slog.i(TAG, "mDarknessLuxShift = " + this.mDarknessLuxShift);
            Slog.i(TAG, "mIndoorApicalADLevel = " + this.mIndoorApicalADLevel);
            Slog.i(TAG, "mIndoorLuxShift = " + this.mIndoorLuxShift);
            Slog.i(TAG, "mOutdoorApicalADLevel = " + this.mOutdoorApicalADLevel);
            Slog.i(TAG, "mOutdoorLuxShift = " + this.mOutdoorLuxShift);
        }
    }

    private void loadDefaultConfig() {
        Slog.i(TAG, "loadDefaultConfig");
        this.mLightSensorRateMillis = LIGHT_SENSOR_RATE_MILLIS;
        this.mApicalADLevel = DEFAULT_APICAL_LEVEL;
        this.mSceneRecognitionEnable = false;
        this.mSceneVideoEnable = false;
        this.mSceneGalleryEnable = false;
        this.mSceneCameraEnable = false;
        if (this.mVideoSceneEnhanceEnabled) {
            this.mDarknessThreshold = 50;
            this.mIndoorThreshold = IOTController.TYPE_MASTER;
            this.mDarknessApicalADLevel = WifiProCHRManager.WIFI_PORTAL_SAMPLES_COLLECTE;
            this.mDarknessLuxShift = UPDATE_BRIGHTNESS_INTERVAL;
            this.mIndoorApicalADLevel = 132;
            this.mIndoorLuxShift = IOTController.TYPE_MASTER;
            this.mOutdoorApicalADLevel = 133;
            this.mOutdoorLuxShift = HwActivityManagerService.SERVICE_B_ADJ;
        }
    }

    private boolean getConfigFromXML(InputStream inStream) {
        boolean lightSensorRateMillsLoaded = false;
        boolean apicalADLevelLoaded = false;
        boolean sceneRecognitionLoaded = false;
        boolean sceneRecognitionLoadStarted = false;
        boolean videoSceneEnhanceLoadedStarted = false;
        boolean videoSceneRecognitionLoadedStarted = false;
        boolean darknessConfigLoadedStarted = false;
        boolean indoorConfigLoadedStarted = false;
        boolean outdoorConfigLoadedStarted = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            String name = AppHibernateCst.INVALID_PKG;
            for (int eventType = parser.getEventType(); eventType != MSG_PROCESS_POLICY; eventType = parser.next()) {
                switch (eventType) {
                    case OUTDOOR /*2*/:
                        name = parser.getName();
                        if (name != null && name.length() != 0) {
                            if (!name.equals("LightSensorRateMills")) {
                                if (!name.equals("ApicalADLevel")) {
                                    if (!name.equals("SceneRecognition")) {
                                        if (!name.equals("SceneVideo") || !sceneRecognitionLoadStarted) {
                                            if (!name.equals("SceneGallery") || !sceneRecognitionLoadStarted) {
                                                if (!name.equals("SceneCamera") || !sceneRecognitionLoadStarted) {
                                                    if (this.mSceneVideoEnable) {
                                                        if (name.equals("VideoSceneEnhance")) {
                                                            videoSceneEnhanceLoadedStarted = true;
                                                            break;
                                                        }
                                                    }
                                                    if (videoSceneEnhanceLoadedStarted) {
                                                        if (name.equals("VideoSceneEnhanceEnabled")) {
                                                            this.mVideoSceneEnhanceEnabled = true;
                                                            break;
                                                        }
                                                    }
                                                    if (videoSceneEnhanceLoadedStarted) {
                                                        if (name.equals("VideoSceneRecognition")) {
                                                            videoSceneRecognitionLoadedStarted = true;
                                                            break;
                                                        }
                                                    }
                                                    if (videoSceneRecognitionLoadedStarted) {
                                                        if (name.equals("VideoSceneDarknessThreshold")) {
                                                            this.mDarknessThreshold = Integer.parseInt(parser.nextText());
                                                            break;
                                                        }
                                                    }
                                                    if (videoSceneRecognitionLoadedStarted) {
                                                        if (name.equals("VideoSceneIndoorThreshold")) {
                                                            this.mIndoorThreshold = Integer.parseInt(parser.nextText());
                                                            break;
                                                        }
                                                    }
                                                    if (videoSceneEnhanceLoadedStarted) {
                                                        if (name.equals("DarknessConfig")) {
                                                            darknessConfigLoadedStarted = true;
                                                            break;
                                                        }
                                                    }
                                                    if (darknessConfigLoadedStarted) {
                                                        if (name.equals("DarknessApicalADLevel")) {
                                                            this.mDarknessApicalADLevel = Integer.parseInt(parser.nextText());
                                                            break;
                                                        }
                                                    }
                                                    if (darknessConfigLoadedStarted) {
                                                        if (name.equals("DarknessAmbidentBrightnessShift")) {
                                                            this.mDarknessLuxShift = Integer.parseInt(parser.nextText());
                                                            break;
                                                        }
                                                    }
                                                    if (videoSceneEnhanceLoadedStarted) {
                                                        if (name.equals("IndoorConfig")) {
                                                            indoorConfigLoadedStarted = true;
                                                            break;
                                                        }
                                                    }
                                                    if (indoorConfigLoadedStarted) {
                                                        if (name.equals("IndoorApicalADLevel")) {
                                                            this.mIndoorApicalADLevel = Integer.parseInt(parser.nextText());
                                                            break;
                                                        }
                                                    }
                                                    if (indoorConfigLoadedStarted) {
                                                        if (name.equals("IndoorAmbidentBrightnessShift")) {
                                                            this.mIndoorLuxShift = Integer.parseInt(parser.nextText());
                                                            break;
                                                        }
                                                    }
                                                    if (videoSceneEnhanceLoadedStarted) {
                                                        if (name.equals("OutdoorConfig")) {
                                                            outdoorConfigLoadedStarted = true;
                                                            break;
                                                        }
                                                    }
                                                    if (outdoorConfigLoadedStarted) {
                                                        if (name.equals("OutdoorApicalADLevel")) {
                                                            this.mOutdoorApicalADLevel = Integer.parseInt(parser.nextText());
                                                            break;
                                                        }
                                                    }
                                                    if (!outdoorConfigLoadedStarted) {
                                                        break;
                                                    }
                                                    if (!name.equals("OutdoorAmbidentBrightnessShift")) {
                                                        break;
                                                    }
                                                    this.mOutdoorLuxShift = Integer.parseInt(parser.nextText());
                                                    break;
                                                }
                                                this.mSceneCameraEnable = Boolean.valueOf(parser.nextText()).booleanValue();
                                                break;
                                            }
                                            this.mSceneGalleryEnable = Boolean.valueOf(parser.nextText()).booleanValue();
                                            break;
                                        }
                                        this.mSceneVideoEnable = Boolean.valueOf(parser.nextText()).booleanValue();
                                        break;
                                    }
                                    sceneRecognitionLoadStarted = true;
                                    break;
                                }
                                this.mApicalADLevel = Integer.parseInt(parser.nextText());
                                apicalADLevelLoaded = true;
                                break;
                            }
                            this.mLightSensorRateMillis = Integer.parseInt(parser.nextText());
                            lightSensorRateMillsLoaded = true;
                            break;
                        }
                        return false;
                    case MSG_UPDATE_SBL_BRIGHTNESS /*3*/:
                        name = parser.getName();
                        if (!name.equals("SceneRecognition")) {
                            if (!name.equals("DarknessConfig")) {
                                if (!name.equals("IndoorConfig")) {
                                    if (!name.equals("OutdoorConfig")) {
                                        if (!name.equals("VideoSceneEnhance")) {
                                            break;
                                        }
                                        videoSceneEnhanceLoadedStarted = false;
                                        break;
                                    }
                                    outdoorConfigLoadedStarted = false;
                                    break;
                                }
                                indoorConfigLoadedStarted = false;
                                break;
                            }
                            darknessConfigLoadedStarted = false;
                            break;
                        }
                        sceneRecognitionLoadStarted = false;
                        sceneRecognitionLoaded = true;
                        if (!this.mSceneVideoEnable && !this.mSceneGalleryEnable && !this.mSceneCameraEnable) {
                            break;
                        }
                        this.mSceneRecognitionEnable = true;
                        break;
                    default:
                        break;
                }
            }
            if (lightSensorRateMillsLoaded && apicalADLevelLoaded && sceneRecognitionLoaded) {
                Slog.i(TAG, "getConfigFromeXML success!");
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
        Slog.e(TAG, "getConfigFromeXML failed!");
        return false;
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
            this.mProcessTriggerHandler.removeMessages(MSG_PROCESS_POLICY);
            this.mProcessTriggerHandler.sendEmptyMessage(OUTDOOR);
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
        this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(DEFAULT, this.mApicalADLevel, DEFAULT, DEFAULT, false, DEFAULT, DEFAULT);
    }

    private void uninitSBL() {
        this.mSmartBackLightOn = false;
        if (DEBUG) {
            Slog.i(TAG, "uninitSBL");
        }
        this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(DEFAULT, this.mApicalADLevel, DEFAULT, DEFAULT, false, DEFAULT, DEFAULT);
        if (this.mSceneRecognition != null) {
            this.mSceneRecognition.cleanScene();
        }
        this.mOutdoorScene = DEFAULT;
        this.mLastOutdoorScence = DEFAULT;
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
        this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(MSG_PROCESS_POLICY, getApicalADLevel(), getSmoothAmbientLux(), REFRESH_FRAMES_NUM, false, DEFAULT, DEFAULT);
    }

    private void turnOffSBL() {
        if (this.mSmartBackLightOn) {
            this.mSmartBackLightOn = false;
            if (DEBUG) {
                Slog.i(TAG, "turnOffSBL");
            }
            this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(MSG_PROCESS_POLICY, this.mApicalADLevel, DEFAULT, REFRESH_FRAMES_NUM, true, DEFAULT, DEFAULT);
            return;
        }
        Slog.w(TAG, "turnOffSBL but sbl is already off");
    }

    private void updateSBLLux() {
        if (this.mSmartBackLightOn) {
            if (DEBUG) {
                Slog.i(TAG, "updateSBLLux, mSmoothAmbientLux = " + this.mSmoothAmbientLux);
            }
            this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(MSG_PROCESS_POLICY, getApicalADLevel(), getSmoothAmbientLux(), REFRESH_FRAMES_NUM, false, DEFAULT, DEFAULT);
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
                this.mSmartBackLight.sendSmartBackLightWithRefreshFrames(MSG_PROCESS_POLICY, getApicalADLevel(), getSmoothAmbientLux(), REFRESH_FRAMES_NUM, false, DEFAULT, DEFAULT);
                return;
            }
            return;
        }
        Slog.w(TAG, "updateSBLBrightness but sbl is off");
    }

    private int getApicalADLevel() {
        if (!this.mVideoSceneEnhanceEnabled || !this.mVideoSceneEntered) {
            return this.mApicalADLevel;
        }
        if (this.mCurrentLux <= this.mDarknessThreshold) {
            return this.mDarknessApicalADLevel;
        }
        if (this.mCurrentLux <= this.mDarknessThreshold || this.mCurrentLux > this.mIndoorThreshold) {
            return this.mOutdoorApicalADLevel;
        }
        return this.mIndoorApicalADLevel;
    }

    private int getSmoothAmbientLux() {
        if (!this.mVideoSceneEnhanceEnabled || !this.mVideoSceneEntered) {
            return this.mSmoothAmbientLux;
        }
        if (this.mCurrentLux <= this.mDarknessThreshold) {
            return this.mSmoothAmbientLux + this.mDarknessLuxShift;
        }
        if (this.mCurrentLux <= this.mDarknessThreshold || this.mCurrentLux > this.mIndoorThreshold) {
            return this.mSmoothAmbientLux + this.mOutdoorLuxShift;
        }
        return this.mSmoothAmbientLux + this.mIndoorLuxShift;
    }

    private void processPolicy() {
        if (this.mOutdoorScene != this.mLastOutdoorScence || this.mAppSBLScene != this.mLastAppSBLScene || this.mNeedUpdateSBL || (this.mIsBrightnessUpdating && this.mSmartBackLightOn)) {
            boolean isOutdoor = this.mOutdoorScene == OUTDOOR;
            if (DEBUG && !this.mIsBrightnessUpdating) {
                Slog.i(TAG, "OutdoorScence = " + isOutdoor + ", AppSBLScene = " + this.mAppSBLScene + ", UpdateSBL = " + this.mNeedUpdateSBL + ", CurrentLux = " + this.mCurrentLux + ", SmoothLux = " + this.mSmoothAmbientLux + ", UpdataBright = " + this.mIsBrightnessUpdating);
            }
            if (!this.mWarmUpFirstTime) {
                if (this.mLastOutdoorScence == MSG_PROCESS_POLICY && this.mOutdoorScene == OUTDOOR) {
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
                } else if (this.mLastOutdoorScence == OUTDOOR && this.mOutdoorScene == MSG_PROCESS_POLICY) {
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
                } else if (this.mLastAppSBLScene || !this.mAppSBLScene) {
                    if (!this.mLastAppSBLScene || this.mAppSBLScene) {
                        if (this.mNeedUpdateSBL) {
                            if (this.mSmartBackLightOn) {
                                updateSBLLux();
                            }
                        } else if (this.mIsBrightnessUpdating && this.mSmartBackLightOn) {
                            updateSBLBrightness();
                        }
                    } else if (this.mOutdoorScene == MSG_PROCESS_POLICY) {
                        if (this.mSmartBackLightOn) {
                            if (DEBUG) {
                                Slog.i(TAG, "INDOOR, exit app SBL scene, turnOffSBL");
                            }
                            turnOffSBL();
                        } else {
                            Slog.w(TAG, "INDOOR, in app SBL scene, but SBL was off, error!");
                        }
                    }
                } else if (this.mOutdoorScene == MSG_PROCESS_POLICY) {
                    if (this.mSmartBackLightOn) {
                        Slog.w(TAG, "INDOOR, wasn't app SBL scene, but SBL was on, error!");
                    } else {
                        if (DEBUG) {
                            Slog.i(TAG, "INDOOR, enter app SBL scene, turnOnSBL");
                        }
                        turnOnSBL();
                    }
                }
                this.mNeedUpdateSBL = false;
                this.mLastOutdoorScence = this.mOutdoorScene;
                this.mLastAppSBLScene = this.mAppSBLScene;
            } else if (this.mOutdoorScene == 0) {
                if (DEBUG) {
                    Slog.i(TAG, "WARM UP, wait outdoor scene detect result");
                }
            } else {
                this.mWarmUpFirstTime = false;
                if (this.mOutdoorScene == OUTDOOR || (this.mOutdoorScene == MSG_PROCESS_POLICY && this.mAppSBLScene)) {
                    if (DEBUG) {
                        Slog.i(TAG, "WARM UP, turnOnSBL");
                    }
                    turnOnSBL();
                } else {
                    if (DEBUG) {
                        Slog.i(TAG, "WARM UP, initSBL");
                    }
                    initSBL();
                }
                this.mNeedUpdateSBL = false;
                this.mLastOutdoorScence = this.mOutdoorScene;
                this.mLastAppSBLScene = this.mAppSBLScene;
            }
        }
    }

    public void processSensorData(long timeInMs, int lux) {
        this.mCurrentLux = lux;
        this.mOutdoorDetector.handleLightSensorEvent(timeInMs, (float) lux);
        this.mOutdoorScene = this.mOutdoorDetector.getIndoorOutdoorFlagForSBL();
        this.mNeedUpdateSBL = this.mOutdoorDetector.getLuxChangedFlagForSBL();
        this.mSmoothAmbientLux = (int) this.mOutdoorDetector.getAmbientLuxForSBL();
        if (this.mLightSensorEnable) {
            this.mProcessTriggerHandler.sendEmptyMessage(MSG_PROCESS_POLICY);
        }
    }
}
