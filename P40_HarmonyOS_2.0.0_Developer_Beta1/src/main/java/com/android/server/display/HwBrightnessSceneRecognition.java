package com.android.server.display;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Xml;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.hidata.appqoe.HwAppQoeUtils;
import com.android.server.pm.auth.HwCertification;
import com.huawei.android.os.HwPowerManager;
import com.huawei.displayengine.DeLog;
import com.huawei.displayengine.DisplayEngineDbManager;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.msdp.movement.HwMSDPMovementChangeEvent;
import com.huawei.msdp.movement.HwMSDPMovementEvent;
import com.huawei.msdp.movement.HwMSDPMovementManager;
import com.huawei.msdp.movement.HwMSDPMovementServiceConnection;
import com.huawei.msdp.movement.HwMSDPMovementStatusChangeCallback;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public class HwBrightnessSceneRecognition {
    private static final int AR_REPORT_TIME_S = 1;
    private static final String CONTROL_XML_FILE = "/display/effect/displayengine/LABC_SR_control.xml";
    private static final int DEFAULT_APP_TYPE = 3;
    private static final int FRONT_CAMERA_APP_ENTER = 31;
    private static final int FRONT_CAMERA_APP_EXIT = 30;
    private static final int GAME_ENTER = 21;
    private static final int GAME_EXIT = 20;
    private static final int GAME_IS_DISABLE_AUTO_BRIGHTNESS = 29;
    private static final int GAME_IS_FRONT_STATE = 27;
    private static final int GAME_NOT_DISABLE_AUTO_BRIGHTNESS = 28;
    private static final int GAME_NOT_FRONT_STATE = 26;
    private static final int HDR_ENTER = 33;
    private static final int HDR_EXIT = 32;
    private static final int HOME_ENTER = 23;
    private static final int HOME_EXIT = 22;
    private static final int INVALID_VALUE = -1;
    private static final int MAX_BL_LEVEL = 255;
    private static final int MIN_BL_LEVEL = 4;
    private static final int MSG_HDR_STATE = 1;
    private static final int READING_ENTER = 1;
    private static final int READING_EXIT = 0;
    private static final int READ_APP_TYPE = 1;
    private static final int READ_EXIT_APP_TYPE = 2;
    private static final int RECONNECT_MAX_COUNT = 10;
    private static final int RECOONECT_WAIT_TIME_MS = 10000;
    private static final int SUCCESS_RETURN_VALUE = 0;
    private static final String TAG = "DE J HwBrightnessSceneRecognition";
    private static final int VIDEO_ENTER = 25;
    private static final int VIDEO_EXIT = 24;
    private Map<String, ArActivity> mArActivities;
    private int mArConfidenceTh = 30;
    private int mArMonitorSampleMaxNum;
    private int mArMonitorSampleTimeStepMs = HwAppQoeUtils.APP_TYPE_STREAMING;
    private int mArReconnectTimes;
    private int mArScene = -1;
    private boolean mArSceneEnable;
    private int mArScreenOnTimeThMs = 200;
    private HwMSDPMovementStatusChangeCallback mArSink = new HwMSDPMovementStatusChangeCallback() {
        /* class com.android.server.display.HwBrightnessSceneRecognition.AnonymousClass2 */

        @Override // com.huawei.msdp.movement.HwMSDPMovementStatusChangeCallback
        public void onMovementStatusChanged(int type, HwMSDPMovementChangeEvent changeEvent) {
            for (HwMSDPMovementEvent event : changeEvent.getMovementEvents()) {
                HwBrightnessSceneRecognition.this.updateArStatus(event.getMovement(), event.getEventType());
            }
            HwBrightnessSceneRecognition.this.setArSceneToBlControllerIfNeeded();
        }
    };
    private int mArWaitTimeMs = 10000;
    private BrightnessAwarenessFence mBrightnessAwarenessFence;
    private HwMSDPMovementServiceConnection mConnect = new HwMSDPMovementServiceConnection() {
        /* class com.android.server.display.HwBrightnessSceneRecognition.AnonymousClass1 */

        @Override // com.huawei.msdp.movement.HwMSDPMovementServiceConnection
        public void onServiceConnected() {
            DeLog.i(HwBrightnessSceneRecognition.TAG, "onServiceConnected()");
            HwBrightnessSceneRecognition.this.mIsArConnected = true;
            if (HwBrightnessSceneRecognition.this.mIsScreenOn) {
                HwBrightnessSceneRecognition.this.enableAr();
            }
        }

        @Override // com.huawei.msdp.movement.HwMSDPMovementServiceConnection
        public void onServiceDisconnected(Boolean isNormalDie) {
            DeLog.i(HwBrightnessSceneRecognition.TAG, "onServiceDisconnected(), isNormalDie = " + isNormalDie);
            HwBrightnessSceneRecognition.this.mIsArConnected = false;
            if (isNormalDie.booleanValue()) {
                DeLog.i(HwBrightnessSceneRecognition.TAG, "normal die, do nothing.");
            } else {
                HwBrightnessSceneRecognition.this.mHandler.postDelayed(new Runnable() {
                    /* class com.android.server.display.HwBrightnessSceneRecognition.AnonymousClass1.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        HwBrightnessSceneRecognition.access$308(HwBrightnessSceneRecognition.this);
                        if (!HwBrightnessSceneRecognition.this.mIsArConnected && HwBrightnessSceneRecognition.this.mArReconnectTimes < 10) {
                            DeLog.w(HwBrightnessSceneRecognition.TAG, "Ar retry " + HwBrightnessSceneRecognition.this.mArReconnectTimes + " time connectService...");
                            HwBrightnessSceneRecognition.this.connectArService();
                            HwBrightnessSceneRecognition.this.mHandler.postDelayed(this, 10000);
                        }
                    }
                }, 10000);
            }
        }
    };
    private final Context mContext;
    private DisplayEngineDbManager mDbManager;
    private int mDbUserDragMaxSize = 100;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private DisplayEngineManager mDisplayEngineManager;
    private boolean mEnable;
    private boolean mFrontCameraAppSceneEnable;
    private int mFrontCameraAppState;
    private Map<String, Integer> mFrontCameraAppWhiteList;
    private boolean mGameBlSceneEnable;
    private Map<String, Integer> mGameBrightnessLevelWhiteList;
    private int mGameBrightnessState;
    private boolean mGameDeSceneEnable;
    private boolean mGameDisableAutoBrightnessModeEnable = false;
    private int mGameHdrState;
    private Map<String, Integer> mGameHdrWhiteList;
    private Handler mHandler = new Handler();
    private boolean mHdrEnable;
    private boolean mHomeSceneEnable;
    private Handler mHwBrightnessSceneRecognitionHandler;
    private HwMSDPMovementManager mHwMsdpMovementManager;
    private boolean mIsArConnected;
    private boolean mIsAwarenessEnable;
    private volatile boolean mIsScreenOn = true;
    private int mLocationScene = -1;
    private boolean mNeedNotifyFrontCameraAppStateChange;
    private boolean mNeedNotifyGameCurveChange;
    private boolean mNeedNotifyPersonalizedCurveChange;
    private boolean mNeedNotifyVideoCurveChange;
    private boolean mPersonalizedCurveEnable;
    private String mPkgName = "";
    private ThreadPoolExecutor mPool = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(10), new DeThreadFactory(), new ThreadPoolExecutor.DiscardOldestPolicy());
    private volatile IPowerManager mPowerManagerService;
    private boolean mReadingSceneEnable;
    private int mReadingState = 1;
    private Map<String, Integer> mReadingWhiteList;
    private Map<String, Integer> mSceneTagMap;
    private volatile boolean mScreenOffStateCleanFlag = true;
    private long mScreenOnTimeMs;
    private Map<String, Integer> mTopApkBrightnessLevelWhiteList;
    private int mTopApkState = 3;
    private int mTopGameModeState = 20;
    private int mUserId;
    private boolean mVideoBlSceneEnable;
    private Map<String, Integer> mVideoBrightnessLevelWhiteList;
    private int mVideoBrightnessState;

    public static class FeatureTag {
        public static final String TAG_AR_SCENE = "ARScene";
        public static final String TAG_FRONT_CAMERA_APP_SCENE = "FrontCameraAppScene";
        public static final String TAG_GAME_BRIGHTNESS_SCENE = "GameBrightnessScene";
        public static final String TAG_GAME_DE_SCENE = "GameDEScene";
        public static final String TAG_GAME_DISABLE_AUTO_BRIGHTNESS_SCENE = "GameDisableAutoBrightnessScene";
        public static final String TAG_PERSONALIZED_CURVE = "PersonalizedCurve";
        public static final String TAG_READING_SCENE = "ReadingScene";
        public static final String TAG_VIDEO_BRIGHTNESS_SCENE = "VideoBrightnessScene";
    }

    public static class SceneTag {
        public static final String ACTIVITY_FAST_WALKING = "android.activity_recognition.fast_walking";
        public static final String ACTIVITY_IN_VEHICLE = "android.activity_recognition.in_vehicle";
        public static final String ACTIVITY_ON_BICYCLE = "android.activity_recognition.on_bicycle";
        public static final String ACTIVITY_ON_FOOT = "android.activity_recognition.on_foot";
        public static final String ACTIVITY_OUTDOOR = "android.activity_recognition.outdoor";
        public static final String ACTIVITY_RELATIVE_STILL = "android.activity_recognition.relative_still";
        public static final String ACTIVITY_RUNNING = "android.activity_recognition.running";
        public static final String ACTIVITY_STILL = "android.activity_recognition.still";
        public static final String ACTIVITY_TYPE_VE_HIGH_SPEED_RAIL = "android.activity_recognition.high_speed_rail";
        public static final String ACTIVITY_UNKNOWN = "android.activity_recognition.unknown";
        public static final String ACTIVITY_WALKING = "android.activity_recognition.walking";
        public static final String LOCATION_HOME = "location.home";
        public static final String LOCATION_NOT_HOME = "location.nothome";
        public static final String LOCATION_UNKNOWN = "location.unknown";
        public static final long MIN_REPORT_TIME = 1000000000;
    }

    static /* synthetic */ int access$308(HwBrightnessSceneRecognition x0) {
        int i = x0.mArReconnectTimes;
        x0.mArReconnectTimes = i + 1;
        return i;
    }

    private class DeThreadFactory implements ThreadFactory {
        private static final String NAME = "DisplayEngine_Default";
        private int mCounter;

        private DeThreadFactory() {
        }

        @Override // java.util.concurrent.ThreadFactory
        public Thread newThread(Runnable runnable) {
            this.mCounter++;
            return new Thread(runnable, "DisplayEngine_Default_Thread_" + this.mCounter);
        }
    }

    public HwBrightnessSceneRecognition(Context context) {
        this.mContext = context;
        this.mGameBrightnessLevelWhiteList = new HashMap();
        this.mVideoBrightnessLevelWhiteList = new HashMap();
        this.mFrontCameraAppWhiteList = new HashMap();
        this.mGameHdrWhiteList = new HashMap();
        this.mTopApkBrightnessLevelWhiteList = new HashMap();
        this.mReadingWhiteList = new HashMap();
        this.mArActivities = new HashMap();
        this.mDisplayEngineManager = new DisplayEngineManager();
        getConfigParam();
        this.mDbManager = DisplayEngineDbManager.getInstance(this.mContext);
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(this.mContext);
        this.mSceneTagMap = new HashMap<String, Integer>(9) {
            /* class com.android.server.display.HwBrightnessSceneRecognition.AnonymousClass3 */

            {
                put(SceneTag.ACTIVITY_UNKNOWN, 0);
                put("android.activity_recognition.still", 1);
                put("android.activity_recognition.on_foot", 2);
                put("android.activity_recognition.in_vehicle", 3);
                put("android.activity_recognition.on_bicycle", 4);
                put("android.activity_recognition.high_speed_rail", 5);
                put(SceneTag.LOCATION_UNKNOWN, 0);
                put(SceneTag.LOCATION_HOME, 10);
                put(SceneTag.LOCATION_NOT_HOME, 20);
            }
        };
        this.mBrightnessAwarenessFence = new BrightnessAwarenessFence(this.mContext, this);
        this.mHwBrightnessSceneRecognitionHandler = new HwBrightnessSceneRecognitionHandler();
    }

    /* access modifiers changed from: private */
    public static class ArActivity {
        public final int action;
        public int status = 2;

        public ArActivity(int action2) {
            this.action = action2;
        }
    }

    private void printConfigValue() {
        DeLog.i(TAG, "printConfigValue: mEnable = " + this.mEnable);
        for (Map.Entry<String, Integer> entry : this.mGameBrightnessLevelWhiteList.entrySet()) {
            DeLog.i(TAG, "printConfigValue: mGameBrightnessLevelWhiteList = " + entry.getKey() + ", " + entry.getValue());
        }
        for (Map.Entry<String, Integer> entry2 : this.mVideoBrightnessLevelWhiteList.entrySet()) {
            DeLog.i(TAG, "printConfigValue: mVideoBrightnessLevelWhiteList = " + entry2.getKey() + ", " + entry2.getValue());
        }
        for (Map.Entry<String, Integer> entry3 : this.mFrontCameraAppWhiteList.entrySet()) {
            DeLog.i(TAG, "printConfigValue: mFrontCameraAppWhiteList = " + entry3.getKey() + ", " + entry3.getValue());
        }
        for (Map.Entry<String, Integer> entry4 : this.mGameHdrWhiteList.entrySet()) {
            DeLog.i(TAG, "printConfigValue: mGameHdrWhiteList = " + entry4.getKey() + ", " + entry4.getValue());
        }
        for (Map.Entry<String, Integer> entry5 : this.mTopApkBrightnessLevelWhiteList.entrySet()) {
            DeLog.i(TAG, "printConfigValue: mTopApkBrightnessLevelWhiteList = " + entry5.getKey() + ", " + entry5.getValue());
        }
        for (Map.Entry<String, Integer> entry6 : this.mReadingWhiteList.entrySet()) {
            DeLog.i(TAG, "printConfigValue: mReadingWhiteList = " + entry6.getKey() + ", " + entry6.getValue());
        }
        printArConfig();
    }

    private void printArConfig() {
        Map<String, ArActivity> map = this.mArActivities;
        if (map != null) {
            for (Map.Entry<String, ArActivity> entry : map.entrySet()) {
                DeLog.i(TAG, "printConfigValue: mArActivities = " + entry.getKey() + ", " + entry.getValue().action);
            }
        } else {
            DeLog.i(TAG, "printConfigValue: mArActivities is null.");
        }
        DeLog.i(TAG, "printConfigValue: mArSceneEnable = " + this.mArSceneEnable);
        DeLog.i(TAG, "printConfigValue: mHoSceneEnable = " + this.mHomeSceneEnable);
        DeLog.i(TAG, "printConfigValue: mGameDisableAutoBrightnessModeEnable = " + this.mGameDisableAutoBrightnessModeEnable);
        DeLog.i(TAG, "printConfigValue: mVideoBlSceneEnable = " + this.mVideoBlSceneEnable);
        DeLog.i(TAG, "printConfigValue: mFrontCameraAppSceneEnable = " + this.mFrontCameraAppSceneEnable);
        DeLog.i(TAG, "printConfigValue: mReadingSceneEnable = " + this.mReadingSceneEnable);
        DeLog.i(TAG, "printConfigValue: mArScreenOnTimeThMs = " + this.mArScreenOnTimeThMs);
        DeLog.i(TAG, "printConfigValue: mArConfidenceTh = " + this.mArConfidenceTh);
        DeLog.i(TAG, "printConfigValue: mArScreenOnTimeThMs = " + this.mArScreenOnTimeThMs);
        DeLog.i(TAG, "printConfigValue: mArWaitTimeMs = " + this.mArWaitTimeMs);
        DeLog.i(TAG, "printConfigValue: mArMonitorSampleMaxNum = " + this.mArMonitorSampleMaxNum);
        DeLog.i(TAG, "printConfigValue: mArMonitorSampleTimeStepMs = " + this.mArMonitorSampleTimeStepMs);
    }

    private void getConfigParam() {
        try {
            if (!getConfig()) {
                DeLog.e(TAG, "getConfig failed!");
            }
            printConfigValue();
        } catch (IOException e) {
            DeLog.e(TAG, "getConfig failed setDefaultConfigValue!");
            printConfigValue();
        }
    }

    private boolean getConfig() throws IOException {
        DeLog.d(TAG, "getConfig");
        File xmlFile = HwCfgFilePolicy.getCfgFile(CONTROL_XML_FILE, 0);
        if (xmlFile == null) {
            DeLog.w(TAG, "get xmlFile :/display/effect/displayengine/LABC_SR_control.xml failed!");
            return false;
        }
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(xmlFile);
            if (!getConfigFromXml(inputStream2)) {
                DeLog.i(TAG, "get xmlFile error");
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    DeLog.e(TAG, "get xmlFile error: " + e);
                }
                return false;
            }
            try {
                inputStream2.close();
            } catch (IOException e2) {
                DeLog.e(TAG, "get xmlFile error: " + e2);
            }
            return true;
        } catch (FileNotFoundException e3) {
            DeLog.e(TAG, "get xmlFile error");
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    DeLog.e(TAG, "get xmlFile error: " + e4);
                }
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    DeLog.e(TAG, "get xmlFile error: " + e5);
                }
            }
            throw th;
        }
    }

    private boolean getConfigFromXml(InputStream inStream) {
        DeLog.d(TAG, "getConfigFromeXml");
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            if (handleTags(parser, parser.getEventType())) {
                return true;
            }
        } catch (XmlPullParserException e) {
            DeLog.e(TAG, "get xmlFile error: " + e);
        } catch (IOException e2) {
            DeLog.e(TAG, "get xmlFile error: " + e2);
        } catch (NumberFormatException e3) {
            DeLog.e(TAG, "get xmlFile error: " + e3);
        }
        DeLog.e(TAG, "getConfigFromeXml false!");
        return false;
    }

    private boolean handleTags(XmlPullParser parser, int eventType) throws XmlPullParserException, IOException {
        boolean configGroupLoadStarted = false;
        boolean loadFinished = false;
        while (eventType != 1) {
            if (eventType == 2) {
                String name = parser.getName();
                if (Objects.equals(name, "LABCSRControl")) {
                    configGroupLoadStarted = true;
                } else if (!handleEnableAndParamsTag(parser, name) && !handleWhiteListsTag(parser, name)) {
                    handleAwarenessTag(parser, name);
                }
            } else if (eventType == 3 && Objects.equals(parser.getName(), "LABCSRControl") && configGroupLoadStarted) {
                loadFinished = true;
                configGroupLoadStarted = false;
            }
            if (loadFinished) {
                break;
            }
            eventType = parser.next();
        }
        if (!loadFinished) {
            return false;
        }
        DeLog.i(TAG, "getConfigFromeXml success!");
        return true;
    }

    private boolean handleEnableAndParamsTag(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
        if (name == null) {
            return false;
        }
        char c = 65535;
        switch (name.hashCode()) {
            case -1610217974:
                if (name.equals("VideoBLSceneEnable")) {
                    c = 5;
                    break;
                }
                break;
            case -1089469156:
                if (name.equals("GameDisableAutoBrightnessModeEnable")) {
                    c = 7;
                    break;
                }
                break;
            case -1009825470:
                if (name.equals("PersonalizedCurveEnable")) {
                    c = '\b';
                    break;
                }
                break;
            case -812329181:
                if (name.equals("ReadingSceneEnable")) {
                    c = '\t';
                    break;
                }
                break;
            case -551881956:
                if (name.equals("GameDESceneEnable")) {
                    c = 2;
                    break;
                }
                break;
            case -389398916:
                if (name.equals("FrontCameraAppSceneEnable")) {
                    c = 6;
                    break;
                }
                break;
            case 115182256:
                if (name.equals("HomeSceneEnable")) {
                    c = 4;
                    break;
                }
                break;
            case 938503091:
                if (name.equals("GameBLSceneEnable")) {
                    c = 3;
                    break;
                }
                break;
            case 1283879560:
                if (name.equals("DBUserDragMaxSize")) {
                    c = 1;
                    break;
                }
                break;
            case 2079986083:
                if (name.equals("Enable")) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                this.mEnable = Boolean.parseBoolean(parser.nextText());
                return true;
            case 1:
                this.mDbUserDragMaxSize = Integer.parseInt(parser.nextText());
                return true;
            case 2:
                this.mGameDeSceneEnable = Boolean.parseBoolean(parser.nextText());
                return true;
            case 3:
                this.mGameBlSceneEnable = Boolean.parseBoolean(parser.nextText());
                return true;
            case 4:
                this.mHomeSceneEnable = Boolean.parseBoolean(parser.nextText());
                return true;
            case 5:
                this.mVideoBlSceneEnable = Boolean.parseBoolean(parser.nextText());
                return true;
            case 6:
                this.mFrontCameraAppSceneEnable = Boolean.parseBoolean(parser.nextText());
                return true;
            case 7:
                this.mGameDisableAutoBrightnessModeEnable = Boolean.parseBoolean(parser.nextText());
                return true;
            case '\b':
                this.mPersonalizedCurveEnable = Boolean.parseBoolean(parser.nextText());
                return true;
            case '\t':
                this.mReadingSceneEnable = Boolean.parseBoolean(parser.nextText());
                return true;
            default:
                return false;
        }
    }

    private boolean parseWhiteList(XmlPullParser parser, String name, Map<String, Integer> whitelist) throws XmlPullParserException, IOException {
        String[] values = parser.nextText().split(",");
        if (values.length != 2) {
            DeLog.d(TAG, "getConfigFromXml find illegal param, tag name = " + name);
            return false;
        }
        whitelist.put(values[0], Integer.valueOf(Integer.parseInt(values[1])));
        return true;
    }

    private boolean handleWhiteListsTag(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
        if (Objects.equals(name, "GameBrightnessLevelWhiteList")) {
            return parseWhiteList(parser, name, this.mGameBrightnessLevelWhiteList);
        }
        if (Objects.equals(name, "VideoBrightnessLevelWhiteList")) {
            return parseWhiteList(parser, name, this.mVideoBrightnessLevelWhiteList);
        }
        if (Objects.equals(name, "FrontCameraAppWhiteList")) {
            return parseWhiteList(parser, name, this.mFrontCameraAppWhiteList);
        }
        if (Objects.equals(name, "GameHDRWhiteList")) {
            return parseWhiteList(parser, name, this.mGameHdrWhiteList);
        }
        if (Objects.equals(name, "TopApkBrightnessLevelWhiteList")) {
            return parseWhiteList(parser, name, this.mTopApkBrightnessLevelWhiteList);
        }
        if (Objects.equals(name, "ReadingModeWhiteList")) {
            return parseWhiteList(parser, name, this.mReadingWhiteList);
        }
        return false;
    }

    private boolean handleAwarenessTag(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
        if (Objects.equals(name, "ARSceneEnable")) {
            this.mArSceneEnable = Boolean.parseBoolean(parser.nextText());
            return true;
        } else if (Objects.equals(name, "ARActivity")) {
            String[] values = parser.nextText().split(",");
            if (values.length != 2) {
                DeLog.d(TAG, "getConfigFromXml find illegal param, tag name = " + name);
                return false;
            }
            this.mArActivities.put(values[0], new ArActivity(Integer.parseInt(values[1])));
            return true;
        } else if (Objects.equals(name, "ARScreenOnTimeTHMs")) {
            this.mArScreenOnTimeThMs = Integer.parseInt(parser.nextText());
            return true;
        } else if (Objects.equals(name, "ARConfidenceTH")) {
            this.mArConfidenceTh = Integer.parseInt(parser.nextText());
            return true;
        } else if (Objects.equals(name, "ARWaitTimeMs")) {
            this.mArWaitTimeMs = Integer.parseInt(parser.nextText());
            return true;
        } else if (Objects.equals(name, "ARMonitorConfidenceSampleMaxNum")) {
            this.mArMonitorSampleMaxNum = Integer.parseInt(parser.nextText());
            return true;
        } else if (Objects.equals(name, "ARMonitorConfidenceSampleTimeStepMs")) {
            this.mArMonitorSampleTimeStepMs = Integer.parseInt(parser.nextText());
            return true;
        } else if (!Objects.equals(name, "AwarenessEnable")) {
            return false;
        } else {
            this.mIsAwarenessEnable = Boolean.parseBoolean(parser.nextText());
            DeLog.i(TAG, "AwarenessEnable mIsAwarenessEnable = " + this.mIsAwarenessEnable);
            return true;
        }
    }

    public boolean isEnable() {
        return this.mEnable;
    }

    public boolean isFeatureEnable(String tag) {
        if (!this.mEnable) {
            return false;
        }
        char c = 65535;
        switch (tag.hashCode()) {
            case -1394280224:
                if (tag.equals(FeatureTag.TAG_VIDEO_BRIGHTNESS_SCENE)) {
                    c = 2;
                    break;
                }
                break;
            case -1203035466:
                if (tag.equals(FeatureTag.TAG_GAME_DISABLE_AUTO_BRIGHTNESS_SCENE)) {
                    c = 6;
                    break;
                }
                break;
            case -1166219904:
                if (tag.equals(FeatureTag.TAG_READING_SCENE)) {
                    c = 5;
                    break;
                }
                break;
            case -1081926945:
                if (tag.equals(FeatureTag.TAG_PERSONALIZED_CURVE)) {
                    c = 4;
                    break;
                }
                break;
            case -204695479:
                if (tag.equals(FeatureTag.TAG_GAME_BRIGHTNESS_SCENE)) {
                    c = 1;
                    break;
                }
                break;
            case 608502681:
                if (tag.equals(FeatureTag.TAG_FRONT_CAMERA_APP_SCENE)) {
                    c = 3;
                    break;
                }
                break;
            case 2145497145:
                if (tag.equals(FeatureTag.TAG_GAME_DE_SCENE)) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return this.mGameDeSceneEnable;
            case 1:
                return this.mGameBlSceneEnable;
            case 2:
                return this.mVideoBlSceneEnable;
            case 3:
                return this.mFrontCameraAppSceneEnable;
            case 4:
                return this.mPersonalizedCurveEnable;
            case 5:
                return this.mReadingSceneEnable;
            case 6:
                return this.mGameDisableAutoBrightnessModeEnable;
            default:
                return false;
        }
    }

    public void initBootCompleteValues() {
        DisplayEngineDbManager displayEngineDbManager = this.mDbManager;
        if (displayEngineDbManager != null) {
            displayEngineDbManager.setMaxSize("DragInfo", this.mDbUserDragMaxSize);
        }
        if (this.mArSceneEnable) {
            connectArService();
        }
        if (this.mIsAwarenessEnable && !this.mBrightnessAwarenessFence.initBootCompleteValues()) {
            DeLog.e(TAG, "mAwarenessManager.connectService failed!");
        }
    }

    public void notifyTopApkChange(String pkgName) {
        if (pkgName == null || pkgName.length() <= 0) {
            DeLog.i(TAG, "pkgName is null || pkgName.length() <= 0!");
        }
        this.mPkgName = pkgName;
        updateTopApkSceneIfNeeded(pkgName);
        updateGameSceneIfNeeded(pkgName);
        updateReadingSceneIfNeeded(pkgName);
        updateVideoSceneIfNeeded(pkgName);
        updateFrontCameraSceneIfNeeded(pkgName);
        int topApkLevel = -1;
        boolean z = false;
        if (this.mNeedNotifyGameCurveChange) {
            this.mNeedNotifyGameCurveChange = false;
            if (!this.mGameDisableAutoBrightnessModeEnable) {
                if (this.mGameBrightnessState == 21) {
                    setTopApkLevelToBlControllerIfNeeded(21);
                    return;
                }
                topApkLevel = 20;
            }
        }
        if (this.mNeedNotifyVideoCurveChange) {
            this.mNeedNotifyVideoCurveChange = false;
            if (this.mVideoBrightnessState == 25) {
                setTopApkLevelToBlControllerIfNeeded(25);
            } else {
                setTopApkLevelToBlControllerIfNeeded(24);
            }
        } else if (this.mNeedNotifyFrontCameraAppStateChange) {
            this.mNeedNotifyFrontCameraAppStateChange = false;
            if (this.mFrontCameraAppState == 31) {
                z = true;
            }
            setFrontCameraAppState(z);
        } else {
            if (this.mNeedNotifyPersonalizedCurveChange) {
                this.mNeedNotifyPersonalizedCurveChange = false;
                topApkLevel = this.mTopApkState;
            }
            if (topApkLevel > 0) {
                setTopApkLevelToBlControllerIfNeeded(topApkLevel);
            }
        }
    }

    public void notifyScreenStatus(boolean isScreenOn) {
        this.mPool.execute(new Runnable(isScreenOn) {
            /* class com.android.server.display.$$Lambda$HwBrightnessSceneRecognition$tvelJL7SXjSvAaJ0mnjAGscPSQc */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwBrightnessSceneRecognition.this.lambda$notifyScreenStatus$0$HwBrightnessSceneRecognition(this.f$1);
            }
        });
        DeLog.i(TAG, "notifyScreenStatus = " + isScreenOn);
    }

    public /* synthetic */ void lambda$notifyScreenStatus$0$HwBrightnessSceneRecognition(boolean isScreenOn) {
        Thread.currentThread().setName("NotifyScreenStatusThread");
        boolean lastState = this.mIsScreenOn;
        this.mIsScreenOn = isScreenOn;
        if (!this.mIsScreenOn && lastState) {
            this.mScreenOffStateCleanFlag = true;
        }
        this.mScreenOnTimeMs = SystemClock.uptimeMillis();
        if (isScreenOn) {
            enableAr();
            return;
        }
        disableAr();
        this.mBrightnessAwarenessFence.onScreenStatusChanged();
    }

    public void notifyAutoBrightnessAdj() {
        if (!isFeatureEnable(FeatureTag.TAG_PERSONALIZED_CURVE)) {
            DeLog.i(TAG, "notifyAutoBrightnessAdj returned, isFeatureEnable(FeatureTag.TAG_PERSONALIZED_CURVE) returned false");
            return;
        }
        Bundle data = new Bundle();
        int retService = HwPowerManager.getHwBrightnessData("SceneRecognition", data);
        BrightnessAdj adj = new BrightnessAdj();
        if (retService == 0) {
            adj.startBrightness = data.getInt("StartBrightness");
            adj.endBrightness = data.getInt("EndBrightness");
            adj.alLux = data.getInt("FilteredAmbientLight");
            adj.proximityPositive = data.getBoolean("ProximityPositive");
            boolean isDeltaValid = data.getBoolean("DeltaValid");
            if (adj.startBrightness < 4 || adj.endBrightness < 4 || adj.startBrightness > 255 || adj.endBrightness > 255) {
                DeLog.w(TAG, "hwBrightnessGetData return invalid brightness, startBrightness = " + adj.startBrightness + ", endBrightness = " + adj.endBrightness);
            } else if (adj.alLux < 0) {
                DeLog.w(TAG, "hwBrightnessGetData return invalid alLux, alLux = " + adj.alLux);
            } else if (!isDeltaValid) {
                DeLog.w(TAG, "hwBrightnessGetData return delta invalid");
            } else {
                adj.userId = this.mUserId;
                adj.timeMillis = System.currentTimeMillis();
                adj.pkgName = this.mPkgName;
                adj.gameState = this.mGameBrightnessState;
                adj.topApkState = this.mTopApkState;
                writeDataBaseUserDrag(adj);
            }
        } else {
            DeLog.w(TAG, "hwBrightnessGetData return false or data is null, retService = " + retService);
        }
    }

    public void notifyUserChange(int userId) {
        this.mUserId = userId;
        DeLog.i(TAG, "notifyUserChange, new id = " + this.mUserId);
    }

    private void updateGameSceneIfNeeded(String pkgName) {
        if (this.mIsScreenOn) {
            boolean screenOffCleanFlag = this.mScreenOffStateCleanFlag;
            this.mScreenOffStateCleanFlag = false;
            int lastGameHdrState = this.mGameHdrState;
            int lastGameBrightnessState = this.mGameBrightnessState;
            if (pkgName == null || pkgName.length() <= 0) {
                this.mGameBrightnessState = 20;
            } else {
                if (this.mGameBrightnessLevelWhiteList.containsKey(pkgName)) {
                    this.mGameBrightnessState = 21;
                    DeLog.d(TAG, "apk " + pkgName + " is in the game brightness whitelist, state=" + this.mGameBrightnessState);
                } else {
                    this.mGameBrightnessState = 20;
                    DeLog.d(TAG, "apk " + pkgName + " is NOT in the game brightness whitelist, state = " + this.mGameBrightnessState);
                }
                Integer hdrLevel = this.mGameHdrWhiteList.get(pkgName);
                if (hdrLevel != null) {
                    this.mGameHdrState = hdrLevel.intValue() + 28;
                    DeLog.d(TAG, "apk " + pkgName + " is in the game hdr whitelist, state = " + this.mGameHdrState);
                } else {
                    this.mGameHdrState = 28;
                    DeLog.d(TAG, "apk " + pkgName + " is NOT in the game hdr whitelist, state = " + this.mGameHdrState);
                }
            }
            if (isFeatureEnable(FeatureTag.TAG_GAME_BRIGHTNESS_SCENE) && (this.mGameBrightnessState != lastGameBrightnessState || screenOffCleanFlag)) {
                this.mNeedNotifyGameCurveChange = true;
            }
            updateDeGameScene(screenOffCleanFlag, lastGameHdrState);
        }
    }

    private void updateDeGameScene(boolean screenOffCleanFlag, int lastGameHdrState) {
        if (this.mDisplayEngineManager == null) {
            DeLog.w(TAG, "mDisplayEngineManager is null !");
        } else if (isFeatureEnable(FeatureTag.TAG_GAME_DE_SCENE)) {
            if (this.mGameHdrState != lastGameHdrState || screenOffCleanFlag) {
                int i = this.mGameHdrState;
                if (i <= 28 || i > 30) {
                    this.mDisplayEngineManager.setScene(36, 28);
                    DeLog.d(TAG, "setScene DE_SCENE_GAME DE_ACTION_TOP_GAME_OFF");
                    return;
                }
                this.mDisplayEngineManager.setScene(36, i);
                DeLog.d(TAG, "setScene DE_SCENE_GAME DE_ACTION_TOP_GAME: TOP " + this.mGameHdrState);
            }
        }
    }

    private void updateReadingSceneIfNeeded(String pkgName) {
        Integer value;
        if (this.mIsScreenOn) {
            int lastReadingState = this.mReadingState;
            if (pkgName != null && pkgName.length() > 0 && (value = this.mReadingWhiteList.get(pkgName)) != null) {
                if (value.intValue() == 1) {
                    this.mReadingState = 1;
                    DeLog.d(TAG, "apk " + pkgName + " is in the reading whitelist group 1, state = " + this.mReadingState);
                } else if (value.intValue() == 2) {
                    this.mReadingState = 0;
                    DeLog.d(TAG, "apk " + pkgName + " is in the reading whitelist group 2, state = " + this.mReadingState);
                }
                if (this.mDisplayEngineManager == null) {
                    DeLog.w(TAG, "mDisplayEngineManager is null !");
                    return;
                }
                setReadingAction(lastReadingState);
                DeLog.d(TAG, "mReadingState = " + this.mReadingState);
            }
        }
    }

    private void setReadingAction(int lastReadingState) {
        int i;
        if (!isFeatureEnable(FeatureTag.TAG_READING_SCENE) || (i = this.mReadingState) == lastReadingState) {
            return;
        }
        if (i == 0) {
            DeLog.d(TAG, "setScene DE_SCENE_READMODE DE_ACTION_MODE_OFF");
            this.mDisplayEngineManager.setScene(45, 17);
            return;
        }
        DeLog.d(TAG, "setScene DE_SCENE_READMODE DE_ACTION_MODE_ON");
        this.mDisplayEngineManager.setScene(45, 16);
    }

    private void updateVideoSceneIfNeeded(String pkgName) {
        if (this.mIsScreenOn) {
            int lastVideoBrightnessState = this.mVideoBrightnessState;
            if (pkgName == null || pkgName.length() <= 0) {
                this.mVideoBrightnessState = 24;
            } else if (this.mVideoBrightnessLevelWhiteList.containsKey(pkgName)) {
                this.mVideoBrightnessState = 25;
                DeLog.d(TAG, "apk " + pkgName + " is in the video brightness whitelist, state = " + this.mVideoBrightnessState);
            } else {
                this.mVideoBrightnessState = 24;
                DeLog.d(TAG, "apk " + pkgName + " is NOT in the video brightness whitelist, state = " + this.mVideoBrightnessState);
            }
            if (isFeatureEnable(FeatureTag.TAG_VIDEO_BRIGHTNESS_SCENE) && this.mVideoBrightnessState != lastVideoBrightnessState) {
                this.mNeedNotifyVideoCurveChange = true;
            }
        }
    }

    private void updateTopApkSceneIfNeeded(String pkgName) {
        if (this.mIsScreenOn) {
            if (pkgName == null || pkgName.length() <= 0) {
                this.mTopApkState = 3;
            } else {
                Integer value = this.mTopApkBrightnessLevelWhiteList.get(pkgName);
                if (value != null) {
                    this.mTopApkState = value.intValue();
                    DeLog.d(TAG, "apk " + pkgName + " is in the top apk whitelist, state = " + this.mTopApkState);
                } else {
                    this.mTopApkState = 3;
                    DeLog.d(TAG, "apk " + pkgName + " is NOT in the top apk whitelist, state = " + this.mTopApkState);
                }
            }
            if (!isFeatureEnable(FeatureTag.TAG_PERSONALIZED_CURVE)) {
                DeLog.d(TAG, "updateTopApkSceneIfNeeded returned, TAG_PERSONALIZED_CURVE enable returned false");
                return;
            }
            DisplayEngineDbManager displayEngineDbManager = this.mDbManager;
            if (displayEngineDbManager != null && displayEngineDbManager.getSize("BrightnessCurveDefault", new Bundle()) > 0) {
                this.mNeedNotifyPersonalizedCurveChange = true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setTopApkLevelToBlControllerIfNeeded(int topApkState) {
        Bundle data = new Bundle();
        data.putInt("TopApkLevel", topApkState);
        int retService = HwPowerManager.setHwBrightnessData("PersonalizedBrightnessCurveLevel", data);
        DeLog.i(TAG, "setTopApkLevelToBlControllerIfNeeded, topApkState = " + topApkState);
        if (retService != 0) {
            DeLog.w(TAG, "setHwBrightnessData(PersonalizedBrightnessCurveLevel) returned " + retService);
        }
    }

    /* access modifiers changed from: private */
    public class BrightnessAdj {
        public int alLux;
        public int endBrightness;
        public int gameState;
        public String pkgName;
        public boolean proximityPositive;
        public int startBrightness;
        public long timeMillis;
        public int topApkState;
        public int userId;

        private BrightnessAdj() {
        }
    }

    private void writeDataBaseUserDrag(BrightnessAdj adj) {
        DeLog.d(TAG, "writeDataBaseUserDrag, userId = " + adj.userId + ", timeMillis = " + adj.timeMillis + ", startPoint = " + adj.startBrightness + ", endPoint = " + adj.endBrightness + ", pkgName = " + adj.pkgName + ", alLux = " + adj.alLux + ", proximityPositive = " + adj.proximityPositive + ", topApkState = " + adj.topApkState + ", gameState = " + adj.gameState);
        if (this.mDbManager != null) {
            Bundle data = new Bundle();
            data.putLong("TimeStamp", adj.timeMillis);
            data.putInt("UserID", adj.userId);
            data.putFloat("StartPoint", (float) adj.startBrightness);
            data.putFloat("StopPoint", (float) adj.endBrightness);
            data.putInt("AmbientLight", adj.alLux);
            data.putBoolean("ProximityPositive", adj.proximityPositive);
            data.putInt("GameState", adj.gameState == 21 ? 1 : -1);
            data.putInt("AppType", adj.topApkState);
            data.putString(HwCertification.KEY_PACKAGE_NAME, adj.pkgName);
            this.mDbManager.addOrUpdateRecord("DragInfo", data);
        }
    }

    public void connectArService() {
        Context context = this.mContext;
        if (context == null) {
            DeLog.w(TAG, "mContext is null! connect failed.");
            return;
        }
        this.mHwMsdpMovementManager = new HwMSDPMovementManager(context);
        DeLog.i(TAG, "connectARService");
        this.mHwMsdpMovementManager.connectService(this.mArSink, this.mConnect);
    }

    public void disconnectArService() {
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMsdpMovementManager;
        if (hwMSDPMovementManager != null && this.mIsArConnected) {
            hwMSDPMovementManager.disConnectService();
            this.mIsArConnected = false;
        }
    }

    public boolean isArConnected() {
        return this.mIsArConnected;
    }

    public boolean enableAr() {
        Map<String, ArActivity> map;
        boolean retOfAll = true;
        if (!this.mIsArConnected || this.mHwMsdpMovementManager == null || (map = this.mArActivities) == null) {
            DeLog.i(TAG, "enableAr failed, mIsArConnected||mHwMsdpMovementManager||mArActivities is null!");
        } else {
            for (String key : map.keySet()) {
                boolean ret = enableArActivity(key, 1);
                retOfAll = retOfAll && ret;
                DeLog.i(TAG, "enableAr " + key + ", ret = " + ret);
            }
        }
        return retOfAll;
    }

    private boolean enableArActivity(String activity, long reportTime) {
        if (this.mHwMsdpMovementManager == null) {
            return false;
        }
        long reportLatencyNs = 1000000000 * reportTime;
        if (!enableActivityEvent(activity, 1, reportLatencyNs) || !enableActivityEvent(activity, 2, reportLatencyNs)) {
            return false;
        }
        return true;
    }

    public boolean disableAr() {
        Map<String, ArActivity> map;
        boolean retOfAll = true;
        if (!this.mIsArConnected || this.mHwMsdpMovementManager == null || (map = this.mArActivities) == null) {
            DeLog.i(TAG, "disableAr failed, mIsArConnected||mHwMsdpMovementManager||mArActivities is null!");
        } else {
            for (Map.Entry<String, ArActivity> entry : map.entrySet()) {
                String key = entry.getKey();
                boolean ret = disableArActivity(key);
                retOfAll = retOfAll && ret;
                entry.getValue().status = 2;
                DeLog.i(TAG, "disableArActivity " + key + ", ret = " + ret);
            }
        }
        return retOfAll;
    }

    private boolean disableArActivity(String activity) {
        if (this.mHwMsdpMovementManager == null) {
            return false;
        }
        if (!disableActivityEvent(activity, 1) || !disableActivityEvent(activity, 2)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateArStatus(String activityType, int eventType) {
        Map<String, ArActivity> map = this.mArActivities;
        if (map == null) {
            DeLog.i(TAG, "mArActivities == null");
            return;
        }
        ArActivity activity = map.get(activityType);
        if (activity != null) {
            activity.status = eventType;
            DeLog.i(TAG, "updateArStatus, activityType:" + activityType + " update status: " + eventType);
            return;
        }
        DeLog.w(TAG, "updateArStatus, activity == null! activityType == " + activityType + ", eventType = " + eventType);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setArSceneToBlControllerIfNeeded() {
        for (Map.Entry<String, ArActivity> entry : this.mArActivities.entrySet()) {
            ArActivity activity = entry.getValue();
            String tag = entry.getKey();
            if (activity.status == 1) {
                if (SystemClock.uptimeMillis() - this.mScreenOnTimeMs > ((long) this.mArScreenOnTimeThMs)) {
                    this.mPool.execute(new ArTask(tag, activity, (long) this.mArWaitTimeMs, this.mArConfidenceTh));
                    if (this.mArMonitorSampleMaxNum > 0) {
                        this.mPool.execute(new ArMonitorTask(entry.getKey(), (long) this.mArMonitorSampleTimeStepMs, this.mArMonitorSampleMaxNum));
                        return;
                    }
                    return;
                }
                setArStatus(tag);
                if (this.mArScene == this.mSceneTagMap.get("android.activity_recognition.in_vehicle").intValue() && this.mLocationScene == this.mSceneTagMap.get(SceneTag.LOCATION_HOME).intValue()) {
                    DeLog.i(TAG, "skip in vescene due to hostatus.");
                    return;
                } else {
                    setTopApkLevelToBlControllerIfNeeded(activity.action);
                    return;
                }
            }
        }
        setTopApkLevelToBlControllerIfNeeded(18);
        setArStatus(SceneTag.ACTIVITY_UNKNOWN);
    }

    public class ArTask implements Runnable {
        private ArActivity mActivityOnQuest;
        private String mArTag;
        private int mConfidenceThreshold;
        private long mWaitTimeMs;

        ArTask(String ArTag, ArActivity activity, long waitTime, int confidenceThreshold) {
            this.mActivityOnQuest = activity;
            this.mWaitTimeMs = waitTime;
            this.mArTag = ArTag;
            this.mConfidenceThreshold = confidenceThreshold;
        }

        @Override // java.lang.Runnable
        public void run() {
            String activityType;
            DeLog.i(HwBrightnessSceneRecognition.TAG, "Wait " + this.mWaitTimeMs + "ms to verify " + this.mArTag + "with confidence threshold " + this.mConfidenceThreshold);
            try {
                Thread.sleep(this.mWaitTimeMs);
            } catch (InterruptedException e) {
                DeLog.i(HwBrightnessSceneRecognition.TAG, "ArTask Exception " + e);
            }
            if (!HwBrightnessSceneRecognition.this.mIsScreenOn) {
                DeLog.i(HwBrightnessSceneRecognition.TAG, "!mIsScreenOn, ArTask finished.");
                return;
            }
            HwMSDPMovementChangeEvent activityChangedEvent = HwBrightnessSceneRecognition.this.getCurrentActivityExtend();
            if (activityChangedEvent == null) {
                DeLog.i(HwBrightnessSceneRecognition.TAG, "activityChangedEvent == null, ArTask finished.");
                return;
            }
            for (HwMSDPMovementEvent event : activityChangedEvent.getMovementEvents()) {
                if (!(event == null || (activityType = event.getMovement()) == null)) {
                    int confidence = event.getConfidence();
                    if (!activityType.equals(this.mArTag) || confidence <= this.mConfidenceThreshold) {
                        DeLog.i(HwBrightnessSceneRecognition.TAG, "confidence < mConfidenceThreshold " + this.mConfidenceThreshold + ", Ar scene ignored.");
                    } else {
                        DeLog.i(HwBrightnessSceneRecognition.TAG, "confidence:" + confidence + " > mConfidenceThreshold:" + this.mConfidenceThreshold);
                        HwBrightnessSceneRecognition.this.setArStatus(this.mArTag);
                        if (HwBrightnessSceneRecognition.this.mArScene == ((Integer) HwBrightnessSceneRecognition.this.mSceneTagMap.get("android.activity_recognition.in_vehicle")).intValue() && HwBrightnessSceneRecognition.this.mLocationScene == ((Integer) HwBrightnessSceneRecognition.this.mSceneTagMap.get(SceneTag.LOCATION_HOME)).intValue()) {
                            DeLog.i(HwBrightnessSceneRecognition.TAG, "skip in vescene due to hostatus, ArTask finished.");
                            return;
                        }
                        HwBrightnessSceneRecognition.this.setTopApkLevelToBlControllerIfNeeded(this.mActivityOnQuest.action);
                    }
                }
            }
            DeLog.i(HwBrightnessSceneRecognition.TAG, "ArTask: finished.");
        }
    }

    public class ArMonitorTask implements Runnable {
        String mArTag;
        List mConfidenceSamples = new LinkedList();
        int mSampleMaxNum;
        long mSampleTimeStepMs;

        ArMonitorTask(String ArTag, long sampleTimeStepMs, int sampleMaxNum) {
            this.mArTag = ArTag;
            this.mSampleTimeStepMs = sampleTimeStepMs;
            this.mSampleMaxNum = sampleMaxNum;
        }

        @Override // java.lang.Runnable
        public void run() {
            DeLog.i(HwBrightnessSceneRecognition.TAG, "ArMonitorTask: start sampling...");
            int i = 0;
            while (true) {
                if (i >= this.mSampleMaxNum) {
                    break;
                }
                try {
                    Thread.sleep(this.mSampleTimeStepMs);
                } catch (InterruptedException e) {
                    DeLog.i(HwBrightnessSceneRecognition.TAG, "ArTask Exception " + e);
                }
                if (!HwBrightnessSceneRecognition.this.mIsScreenOn) {
                    DeLog.d(HwBrightnessSceneRecognition.TAG, "ArMonitorTask breaked due to screen off");
                    break;
                }
                HwMSDPMovementChangeEvent activityChangedEvent = HwBrightnessSceneRecognition.this.getCurrentActivityExtend();
                if (activityChangedEvent != null) {
                    for (HwMSDPMovementEvent event : activityChangedEvent.getMovementEvents()) {
                        if (event != null) {
                            String activityType = event.getMovement();
                            int confidence = event.getConfidence();
                            if (activityType != null && activityType.equals(this.mArTag)) {
                                DeLog.i(HwBrightnessSceneRecognition.TAG, "ArMonitorTask, confidence " + i + " = " + confidence);
                                this.mConfidenceSamples.add(Short.valueOf((short) confidence));
                            }
                        }
                    }
                    List list = this.mConfidenceSamples;
                    if (list != null && list.size() < i + 1) {
                        HwBrightnessSceneRecognition.this.sendAbnormalActivityRecognitionToMonitor(this.mArTag, this.mConfidenceSamples);
                        DeLog.d(HwBrightnessSceneRecognition.TAG, "ArMonitorTask breaked due to activity no longer exist.");
                        break;
                    }
                }
                i++;
            }
            DeLog.i(HwBrightnessSceneRecognition.TAG, "ArMonitorTask: finished.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAbnormalActivityRecognitionToMonitor(String tag, List confidenceSamples) {
        int monitorScene;
        if (this.mDisplayEffectMonitor != null && confidenceSamples != null && tag != null && (monitorScene = getMonitorUserScene(tag)) >= 0) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "userSceneMisrecognition");
            params.put("userScene", Integer.valueOf(monitorScene));
            params.put("confidence", confidenceSamples);
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    private void sendActivityRecognitionToMonitor() {
        if (this.mDisplayEffectMonitor != null) {
            int monitorScene = 0;
            int i = this.mArScene;
            if (i > 0) {
                monitorScene = 0 + i;
            }
            int i2 = this.mLocationScene;
            if (i2 > 0) {
                monitorScene += i2;
            }
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "userScene");
            params.put("userScene", Integer.valueOf(monitorScene));
            this.mDisplayEffectMonitor.sendMonitorParam(params);
        }
    }

    private int getMonitorUserScene(String tag) {
        Integer value = this.mSceneTagMap.get(tag);
        if (value != null) {
            return value.intValue();
        }
        return -1;
    }

    public void setArStatus(String tag) {
        this.mArScene = getMonitorUserScene(tag);
        sendActivityRecognitionToMonitor();
    }

    public void setLocationStatus(String tag) {
        DeLog.i(TAG, "setLocationStatus tag = " + tag);
        this.mLocationScene = getMonitorUserScene(tag);
        if (this.mHomeSceneEnable) {
            if (this.mLocationScene == this.mSceneTagMap.get(SceneTag.LOCATION_HOME).intValue()) {
                setTopApkLevelToBlControllerIfNeeded(23);
            } else {
                setTopApkLevelToBlControllerIfNeeded(22);
            }
        }
        if (this.mLocationScene == this.mSceneTagMap.get(SceneTag.LOCATION_NOT_HOME).intValue() && this.mArScene == this.mSceneTagMap.get("android.activity_recognition.in_vehicle").intValue()) {
            ArActivity activity = this.mArActivities.get("android.activity_recognition.in_vehicle");
            if (activity == null) {
                DeLog.i(TAG, "get(SceneTag.ACTIVITY_IN_VESCENE) return null!");
                return;
            } else {
                setTopApkLevelToBlControllerIfNeeded(activity.action);
                DeLog.i(TAG, "set in vescene due to leaving hostatus.");
            }
        }
        sendActivityRecognitionToMonitor();
    }

    public boolean enableActivityEvent(String activity, int eventType, long reportLatencyNs) {
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMsdpMovementManager;
        if (hwMSDPMovementManager != null) {
            return hwMSDPMovementManager.enableMovementEvent(0, activity, eventType, reportLatencyNs, null);
        }
        return false;
    }

    public boolean disableActivityEvent(String activity, int eventType) {
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMsdpMovementManager;
        if (hwMSDPMovementManager != null) {
            return hwMSDPMovementManager.disableMovementEvent(0, activity, eventType);
        }
        return false;
    }

    public boolean flushAr() {
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMsdpMovementManager;
        if (hwMSDPMovementManager != null) {
            return hwMSDPMovementManager.flush();
        }
        return false;
    }

    public HwMSDPMovementChangeEvent getCurrentActivityExtend() {
        HwMSDPMovementManager hwMSDPMovementManager = this.mHwMsdpMovementManager;
        if (hwMSDPMovementManager != null) {
            return hwMSDPMovementManager.getCurrentMovement(0);
        }
        return null;
    }

    public void setVideoPlayStatus(boolean isVideoPlay) {
        Bundle data = new Bundle();
        data.putBoolean("IsVideoPlay", isVideoPlay);
        int retService = HwPowerManager.setHwBrightnessData("QRCodeBrighten", data);
        DeLog.i(TAG, "QRCodeBrighten, IsVideoPlay = " + isVideoPlay);
        if (retService != 0) {
            DeLog.w(TAG, "setHwBrightnessData(QRCodeBrighten) returned " + retService);
        }
    }

    public void updateTopGameModeStatus(String pkgName) {
        if (this.mIsScreenOn) {
            int lastTopGameModeState = this.mTopGameModeState;
            if (pkgName == null || pkgName.length() <= 0) {
                this.mTopGameModeState = 20;
            } else if (!this.mGameBrightnessLevelWhiteList.containsKey(pkgName)) {
                this.mTopGameModeState = 20;
            } else {
                this.mTopGameModeState = 21;
            }
            if (lastTopGameModeState != this.mTopGameModeState) {
                DeLog.i(TAG, "apk=" + pkgName + ",state=" + this.mTopGameModeState);
                setTopApkLevelToBlControllerIfNeeded(this.mTopGameModeState);
            }
        }
    }

    public void updateGameEnterStatus(boolean gameEnterStatus) {
        if (gameEnterStatus) {
            setTopApkLevelToBlControllerIfNeeded(27);
        } else {
            setTopApkLevelToBlControllerIfNeeded(26);
        }
    }

    public void setGameDisableAutoBrightnessModeStatus(boolean enable) {
        DeLog.i(TAG, "setGameDisableAutoBrightnessModeStatus=" + enable);
        if (enable) {
            setTopApkLevelToBlControllerIfNeeded(29);
        } else {
            setTopApkLevelToBlControllerIfNeeded(28);
        }
    }

    public boolean getGameDisableAutoBrightnessModeStatus() {
        return getGameDisableAutoBrightnessMode();
    }

    private boolean getGameDisableAutoBrightnessMode() {
        Bundle data = new Bundle();
        if (HwPowerManager.getHwBrightnessData("GameDiableAutoBrightness", data) != 0) {
            return false;
        }
        boolean gameDisableAutoBrightnessModeStatus = data.getBoolean("GameDisableAutoBrightnessModeEnable");
        DeLog.i(TAG, "gameDisableAutoBrightnessModeStatus " + gameDisableAutoBrightnessModeStatus);
        return gameDisableAutoBrightnessModeStatus;
    }

    private void setFrontCameraAppState(boolean enable) {
        Bundle data = new Bundle();
        data.putBoolean("FrontCameraAppEnableState", enable);
        int retService = HwPowerManager.setHwBrightnessData("FrontCameraApp", data);
        DeLog.i(TAG, "setFrontCameraAppState, enable = " + enable);
        if (retService != 0) {
            DeLog.w(TAG, "setHwBrightnessData(setFrontCameraAppState) returned " + retService);
        }
    }

    private void updateFrontCameraSceneIfNeeded(String pkgName) {
        if (this.mIsScreenOn) {
            int lastFrontCameraAppState = this.mFrontCameraAppState;
            if (pkgName == null || pkgName.length() <= 0) {
                this.mFrontCameraAppState = 30;
            } else if (this.mFrontCameraAppWhiteList.containsKey(pkgName)) {
                this.mFrontCameraAppState = 31;
                DeLog.d(TAG, "apk " + pkgName + " is in the Camera brightness whitelist, state = " + this.mFrontCameraAppState);
            } else {
                this.mFrontCameraAppState = 30;
                DeLog.d(TAG, "apk " + pkgName + " is NOT in the Camera brightness whitelist, state = " + this.mFrontCameraAppState);
            }
            if (isFeatureEnable(FeatureTag.TAG_FRONT_CAMERA_APP_SCENE) && this.mFrontCameraAppState != lastFrontCameraAppState) {
                this.mNeedNotifyFrontCameraAppStateChange = true;
            }
        }
    }

    public void updateHdrStatus(boolean hdrEnable) {
        DeLog.i(TAG, "updateHdrStatus,hdrEnable=" + hdrEnable);
        this.mHdrEnable = hdrEnable;
        Handler handler = this.mHwBrightnessSceneRecognitionHandler;
        if (handler != null) {
            handler.removeMessages(1);
            this.mHwBrightnessSceneRecognitionHandler.sendEmptyMessage(1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHdrLevel(boolean hdrEnable) {
        DeLog.i(TAG, "updateHdrStatus-level,hdrEnable=" + hdrEnable);
        if (hdrEnable) {
            setTopApkLevelToBlControllerIfNeeded(33);
        } else {
            setTopApkLevelToBlControllerIfNeeded(32);
        }
    }

    private final class HwBrightnessSceneRecognitionHandler extends Handler {
        private HwBrightnessSceneRecognitionHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                HwBrightnessSceneRecognition hwBrightnessSceneRecognition = HwBrightnessSceneRecognition.this;
                hwBrightnessSceneRecognition.updateHdrLevel(hwBrightnessSceneRecognition.mHdrEnable);
            }
        }
    }
}
