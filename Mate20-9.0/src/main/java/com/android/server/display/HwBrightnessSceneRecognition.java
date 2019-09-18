package com.android.server.display;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Xml;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.huawei.android.location.activityrecognition.HwActivityChangedEvent;
import com.huawei.android.location.activityrecognition.HwActivityChangedExtendEvent;
import com.huawei.android.location.activityrecognition.HwActivityRecognition;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionEvent;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionExtendEvent;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionHardwareSink;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionServiceConnection;
import com.huawei.android.location.activityrecognition.HwEnvironmentChangedEvent;
import com.huawei.displayengine.DElog;
import com.huawei.displayengine.DisplayEngineDBManager;
import com.huawei.displayengine.DisplayEngineManager;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwBrightnessSceneRecognition {
    private static final int BINDER_REBUILD_COUNT_MAX = 10;
    private static final String CONTROL_XML_FILE = "/display/effect/displayengine/LABC_SR_control.xml";
    private static final int DEFAULT_APP_TYPE = 3;
    private static final int GAME_ENTER = 21;
    private static final int GAME_EXIT = 20;
    private static final int READING_ENTER = 1;
    private static final int READING_EXIT = 0;
    private static final int READ_APPTYPE = 1;
    private static final int READ_EXIT_APPTYPE = 2;
    private static final String TAG = "DE J HwBrightnessSceneRecognition";
    private HwActivityRecognitionHardwareSink hwArSink = new HwActivityRecognitionHardwareSink() {
        public void onActivityChanged(HwActivityChangedEvent activityChangedEvent) {
            StringBuffer bf = new StringBuffer();
            for (HwActivityRecognitionEvent event : activityChangedEvent.getActivityRecognitionEvents()) {
                String activityType = event.getActivity();
                int eventType = event.getEventType();
                bf.append(event.getActivity());
                bf.append(",");
                bf.append(HwBrightnessSceneRecognition.this.getAREventType(event.getEventType()));
                bf.append(",");
                bf.append(event.getTimestampNs());
                bf.append(",");
                HwBrightnessSceneRecognition.this.updateARStatus(activityType, eventType);
            }
            DElog.d(HwBrightnessSceneRecognition.TAG, bf.toString());
            HwBrightnessSceneRecognition.this.setARSceneToBLControllerIfNeeded();
        }

        public void onActivityExtendChanged(HwActivityChangedExtendEvent activityChangedExtendEvent) {
            DElog.d(HwBrightnessSceneRecognition.TAG, "onActivityExtendChanged .....");
        }

        public void onEnvironmentChanged(HwEnvironmentChangedEvent environmentChangedEvent) {
            DElog.d(HwBrightnessSceneRecognition.TAG, "onEnvironmentChanged .....");
        }
    };
    private Map<String, ARActivity> mARActivities;
    private int mARConfidenceTH = 30;
    private int mARMonitorConfidenceSampleMaxNum = 0;
    private int mARMonitorConfidenceSampleTimeStepMs = HwAPPQoEUtils.APP_TYPE_STREAMING;
    /* access modifiers changed from: private */
    public int mARScene = -1;
    private boolean mARSceneEnable = false;
    private int mARScreenOnTimeTHMs = 200;
    private int mARWaitTimeMs = 10000;
    private int mBinderRebuildCount = 0;
    private BrightnessAwarenessFence mBrightnessAwarenessFence;
    private HwActivityRecognitionServiceConnection mConnect = new HwActivityRecognitionServiceConnection() {
        public void onServiceConnected() {
            DElog.i(HwBrightnessSceneRecognition.TAG, "onServiceConnected()");
            HwBrightnessSceneRecognition.this.mIsARConnected = true;
            HwBrightnessSceneRecognition.this.getSupportedARActivities();
            if (HwBrightnessSceneRecognition.this.mIsScreenOn) {
                HwBrightnessSceneRecognition.this.enableAR();
            }
        }

        public void onServiceDisconnected() {
            DElog.i(HwBrightnessSceneRecognition.TAG, "onServiceDisconnected()");
            HwBrightnessSceneRecognition.this.mIsARConnected = false;
            HwBrightnessSceneRecognition.this.disableAR();
        }
    };
    private final Context mContext;
    private DisplayEngineDBManager mDBManager;
    private int mDBUserDragMaxSize = 100;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private DisplayEngineManager mDisplayEngineManager;
    private boolean mEnable = false;
    private boolean mGameBLSceneEnable = false;
    private Map<String, Integer> mGameBrightnessLevelWhiteList;
    private int mGameBrightnessState;
    private boolean mGameDESceneEnable = false;
    private int mGameHDRState;
    private Map<String, Integer> mGameHDRWhiteList;
    private HwActivityRecognition mHwActivityRecognition;
    boolean mIsARConnected = false;
    private boolean mIsAwarenessEnable;
    private volatile boolean mIsBinderBuilding = false;
    /* access modifiers changed from: private */
    public volatile boolean mIsScreenOn = true;
    /* access modifiers changed from: private */
    public int mLocationScene = -1;
    private Object mLockBinderBuilding;
    private Object mLockService;
    private boolean mNeedNotifyGameCurveChange = false;
    private boolean mNeedNotifyPersonalizedCurveChange = false;
    private boolean mPersonalizedCurveEnable = false;
    private String mPkgName = "";
    private volatile IPowerManager mPowerManagerService;
    private volatile boolean mPowerManagerServiceInitialized = false;
    private boolean mReadingSceneEnable = false;
    private int mReadingState;
    private Map<String, Integer> mReadingWhiteList;
    /* access modifiers changed from: private */
    public Map<String, Integer> mSceneTagMap;
    /* access modifiers changed from: private */
    public volatile boolean mScreenOffStateCleanFlag = true;
    /* access modifiers changed from: private */
    public long mScreenOnTimeMs;
    private Map<String, Integer> mTopApkBrightnessLevelWhiteList;
    private int mTopApkState = 3;
    private int mUserId = 0;

    private static class ARActivity {
        public final int mAction;
        public int mStatus = 2;

        public ARActivity(int action) {
            this.mAction = action;
        }
    }

    public class ARMonitorThread extends Thread {
        String mARTag;
        List mConfidenceSamples = new LinkedList();
        int mSampleMaxNum;
        long mSampleTimeStepMs;

        ARMonitorThread(String ARTag, long sampleTimeStepMs, int sampleMaxNum) {
            super("ARMonitorThread");
            this.mARTag = ARTag;
            this.mSampleTimeStepMs = sampleTimeStepMs;
            this.mSampleMaxNum = sampleMaxNum;
        }

        public void run() {
            DElog.i(HwBrightnessSceneRecognition.TAG, "ARMonitorThread: start sampling...");
            int i = 0;
            while (true) {
                if (i >= this.mSampleMaxNum) {
                    break;
                }
                try {
                    Thread.sleep(this.mSampleTimeStepMs);
                } catch (InterruptedException e) {
                    DElog.i(HwBrightnessSceneRecognition.TAG, "ARThread Exception " + e);
                }
                if (!HwBrightnessSceneRecognition.this.mIsScreenOn) {
                    DElog.d(HwBrightnessSceneRecognition.TAG, "ARMonitorThread breaked due to screen off");
                    break;
                }
                HwActivityChangedExtendEvent activityChangedEvent = HwBrightnessSceneRecognition.this.getCurrentActivityExtend();
                if (activityChangedEvent != null) {
                    for (HwActivityRecognitionExtendEvent event : activityChangedEvent.getActivityRecognitionExtendEvents()) {
                        if (event != null) {
                            String activityType = event.getActivity();
                            int confidence = event.getConfidence();
                            if (activityType != null && activityType.equals(this.mARTag)) {
                                DElog.d(HwBrightnessSceneRecognition.TAG, "ARMonitorThread, confidence " + i + " = " + confidence);
                                this.mConfidenceSamples.add(Short.valueOf((short) confidence));
                            }
                        }
                    }
                    if (this.mConfidenceSamples != null && this.mConfidenceSamples.size() < i + 1) {
                        HwBrightnessSceneRecognition.this.sendAbnormalActivityRecognitionToMonitor(this.mARTag, this.mConfidenceSamples);
                        DElog.d(HwBrightnessSceneRecognition.TAG, "ARMonitorThread breaked due to activity no longer exist.");
                        break;
                    }
                }
                i++;
            }
            DElog.i(HwBrightnessSceneRecognition.TAG, "ARMonitorThread: finished.");
        }
    }

    public class ARThread extends Thread {
        String mARTag;
        ARActivity mActivityOnQuest;
        int mConfidenceThreshold;
        long mWaitTimeMs;

        ARThread(String ARTag, ARActivity activity, long waitTime, int confidenceThreshold) {
            super("ARThread");
            this.mActivityOnQuest = activity;
            this.mWaitTimeMs = waitTime;
            this.mARTag = ARTag;
            this.mConfidenceThreshold = confidenceThreshold;
        }

        public void run() {
            DElog.i(HwBrightnessSceneRecognition.TAG, "Wait " + this.mWaitTimeMs + "ms to verify " + this.mARTag + "with confidence threshold " + this.mConfidenceThreshold);
            try {
                Thread.sleep(this.mWaitTimeMs);
            } catch (InterruptedException e) {
                DElog.i(HwBrightnessSceneRecognition.TAG, "ARThread Exception " + e);
            }
            if (!HwBrightnessSceneRecognition.this.mIsScreenOn) {
                DElog.i(HwBrightnessSceneRecognition.TAG, "!mIsScreenOn, ARThread finished.");
                return;
            }
            HwActivityChangedExtendEvent activityChangedEvent = HwBrightnessSceneRecognition.this.getCurrentActivityExtend();
            if (activityChangedEvent == null) {
                DElog.i(HwBrightnessSceneRecognition.TAG, "activityChangedEvent == null, ARThread finished.");
                return;
            }
            for (HwActivityRecognitionExtendEvent event : activityChangedEvent.getActivityRecognitionExtendEvents()) {
                if (event != null) {
                    String activityType = event.getActivity();
                    if (activityType == null) {
                        continue;
                    } else {
                        int confidence = event.getConfidence();
                        if (!activityType.equals(this.mARTag) || confidence <= this.mConfidenceThreshold) {
                            DElog.d(HwBrightnessSceneRecognition.TAG, "confidence < mConfidenceThreshold " + this.mConfidenceThreshold + ", AR scene ignored.");
                        } else {
                            DElog.i(HwBrightnessSceneRecognition.TAG, "confidence:" + confidence + " > mConfidenceThreshold:" + this.mConfidenceThreshold);
                            HwBrightnessSceneRecognition.this.setARStatus(this.mARTag);
                            if (HwBrightnessSceneRecognition.this.mARScene == ((Integer) HwBrightnessSceneRecognition.this.mSceneTagMap.get("android.activity_recognition.in_vehicle")).intValue() && HwBrightnessSceneRecognition.this.mLocationScene == ((Integer) HwBrightnessSceneRecognition.this.mSceneTagMap.get(SceneTag.LOCATION_HOME)).intValue()) {
                                DElog.i(HwBrightnessSceneRecognition.TAG, "skip in vehicle scene due to home status, ARThread finished.");
                                return;
                            }
                            HwBrightnessSceneRecognition.this.setTopApkLevelToBLControllerIfNeeded(this.mActivityOnQuest.mAction);
                        }
                    }
                }
            }
            DElog.i(HwBrightnessSceneRecognition.TAG, "ARThread: finished.");
        }
    }

    public static class FeatureTag {
        public static final String TAG_AR_SCENE = "ARScene";
        public static final String TAG_GAME_BRIGHTNESS_SCENE = "GameBrightnessScene";
        public static final String TAG_GAME_DE_SCENE = "GameDEScene";
        public static final String TAG_PERSONALIZED_CURVE = "PersonalizedCurve";
        public static final String TAG_READING_SCENE = "ReadingScene";
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

    public HwBrightnessSceneRecognition(Context context) {
        this.mContext = context;
        this.mLockService = new Object();
        this.mLockBinderBuilding = new Object();
        this.mGameBrightnessLevelWhiteList = new HashMap();
        this.mGameHDRWhiteList = new HashMap();
        this.mTopApkBrightnessLevelWhiteList = new HashMap();
        this.mReadingWhiteList = new HashMap();
        this.mARActivities = new HashMap();
        this.mDisplayEngineManager = new DisplayEngineManager();
        getConfigParam();
        this.mDBManager = DisplayEngineDBManager.getInstance(this.mContext);
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(this.mContext);
        this.mSceneTagMap = new HashMap();
        this.mSceneTagMap.put("android.activity_recognition.unknown", 0);
        this.mSceneTagMap.put("android.activity_recognition.still", 1);
        this.mSceneTagMap.put("android.activity_recognition.on_foot", 2);
        this.mSceneTagMap.put("android.activity_recognition.in_vehicle", 3);
        this.mSceneTagMap.put("android.activity_recognition.on_bicycle", 4);
        this.mSceneTagMap.put("android.activity_recognition.high_speed_rail", 5);
        this.mSceneTagMap.put(SceneTag.LOCATION_UNKNOWN, 0);
        this.mSceneTagMap.put(SceneTag.LOCATION_HOME, 10);
        this.mSceneTagMap.put(SceneTag.LOCATION_NOT_HOME, 20);
        this.mBrightnessAwarenessFence = new BrightnessAwarenessFence(this.mContext, this);
    }

    private void setDefaultConfigValue() {
    }

    private void printConfigValue() {
        DElog.i(TAG, "printConfigValue: mEnable = " + this.mEnable);
        if (this.mGameBrightnessLevelWhiteList != null) {
            for (Map.Entry<String, Integer> entry : this.mGameBrightnessLevelWhiteList.entrySet()) {
                DElog.i(TAG, "printConfigValue: mGameBrightnessLevelWhiteList = " + entry.getKey() + ", " + entry.getValue());
            }
        } else {
            DElog.i(TAG, "printConfigValue: mGameBrightnessLevelWhiteList is null.");
        }
        if (this.mGameHDRWhiteList != null) {
            for (Map.Entry<String, Integer> entry2 : this.mGameHDRWhiteList.entrySet()) {
                DElog.i(TAG, "printConfigValue: mGameHDRWhiteList = " + entry2.getKey() + ", " + entry2.getValue());
            }
        } else {
            DElog.i(TAG, "printConfigValue: mGameHDRWhiteList is null.");
        }
        if (this.mTopApkBrightnessLevelWhiteList != null) {
            for (Map.Entry<String, Integer> entry3 : this.mTopApkBrightnessLevelWhiteList.entrySet()) {
                DElog.i(TAG, "printConfigValue: mTopApkBrightnessLevelWhiteList = " + entry3.getKey() + ", " + entry3.getValue());
            }
        } else {
            DElog.i(TAG, "printConfigValue: mTopApkBrightnessLevelWhiteList is null.");
        }
        if (this.mReadingWhiteList != null) {
            for (Map.Entry<String, Integer> entry4 : this.mReadingWhiteList.entrySet()) {
                DElog.i(TAG, "printConfigValue: mReadingWhiteList = " + entry4.getKey() + ", " + entry4.getValue());
            }
        }
        if (this.mARActivities != null) {
            for (Map.Entry<String, ARActivity> entry5 : this.mARActivities.entrySet()) {
                DElog.i(TAG, "printConfigValue: mARActivities = " + entry5.getKey() + ", " + entry5.getValue().mAction);
            }
        } else {
            DElog.i(TAG, "printConfigValue: mARActivities is null.");
        }
        DElog.i(TAG, "printConfigValue: mARSceneEnable = " + this.mARSceneEnable);
        DElog.i(TAG, "printConfigValue: mReadingSceneEnable = " + this.mReadingSceneEnable);
        DElog.i(TAG, "printConfigValue: mARScreenOnTimeTHMs = " + this.mARScreenOnTimeTHMs);
        DElog.i(TAG, "printConfigValue: mARConfidenceTH = " + this.mARConfidenceTH);
        DElog.i(TAG, "printConfigValue: mARScreenOnTimeTHMs = " + this.mARScreenOnTimeTHMs);
        DElog.i(TAG, "printConfigValue: mARWaitTimeMs = " + this.mARWaitTimeMs);
        DElog.i(TAG, "printConfigValue: mARMonitorConfidenceSampleMaxNum = " + this.mARMonitorConfidenceSampleMaxNum);
        DElog.i(TAG, "printConfigValue: mARMonitorConfidenceSampleTimeStepMs = " + this.mARMonitorConfidenceSampleTimeStepMs);
    }

    private void getConfigParam() {
        try {
            if (!getConfig()) {
                DElog.e(TAG, "getConfig failed!");
                setDefaultConfigValue();
            }
            printConfigValue();
        } catch (IOException e) {
            DElog.e(TAG, "getConfig failed setDefaultConfigValue!");
            setDefaultConfigValue();
            printConfigValue();
        }
    }

    private boolean getConfig() throws IOException {
        String str;
        StringBuilder sb;
        DElog.d(TAG, "getConfig");
        File xmlFile = HwCfgFilePolicy.getCfgFile(CONTROL_XML_FILE, 0);
        if (xmlFile == null) {
            DElog.w(TAG, "get xmlFile :/display/effect/displayengine/LABC_SR_control.xml failed!");
            return false;
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(xmlFile);
            if (!getConfigFromXML(inputStream)) {
                DElog.i(TAG, "get xmlFile error");
                inputStream.close();
                try {
                    inputStream.close();
                } catch (IOException e) {
                    DElog.e(TAG, "get xmlFile error: " + e);
                }
                return false;
            }
            inputStream.close();
            try {
                inputStream.close();
            } catch (IOException e2) {
                DElog.e(TAG, "get xmlFile error: " + e2);
            }
            return true;
        } catch (RuntimeException e3) {
            DElog.e(TAG, "get xmlFile error: " + e3);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    e = e4;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
            return false;
        } catch (FileNotFoundException e5) {
            DElog.e(TAG, "get xmlFile error: " + e5);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    e = e6;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
            return false;
        } catch (IOException e7) {
            DElog.e(TAG, "get xmlFile error: " + e7);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e8) {
                    e = e8;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
            return false;
        } catch (Exception e9) {
            DElog.e(TAG, "get xmlFile error: " + e9);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e10) {
                    e = e10;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
            return false;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e11) {
                    DElog.e(TAG, "get xmlFile error: " + e11);
                }
            }
            throw th;
        }
        sb.append("get xmlFile error: ");
        sb.append(e);
        DElog.e(str, sb.toString());
        return false;
    }

    private boolean getConfigFromXML(InputStream inStream) {
        DElog.d(TAG, "getConfigFromeXML");
        boolean configGroupLoadStarted = false;
        boolean loadFinished = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            int eventType = parser.getEventType();
            while (true) {
                if (eventType != 1) {
                    switch (eventType) {
                        case 2:
                            String name = parser.getName();
                            if (!name.equals("LABCSRControl")) {
                                if (!name.equals("Enable")) {
                                    if (!name.equals("DBUserDragMaxSize")) {
                                        if (!name.equals("GameBrightnessLevelWhiteList")) {
                                            if (!name.equals("GameHDRWhiteList")) {
                                                if (!name.equals("TopApkBrightnessLevelWhiteList")) {
                                                    if (!name.equals("ReadingModeWhiteList")) {
                                                        if (!name.equals("GameDESceneEnable")) {
                                                            if (!name.equals("GameBLSceneEnable")) {
                                                                if (!name.equals("PersonalizedCurveEnable")) {
                                                                    if (!name.equals("ARSceneEnable")) {
                                                                        if (!name.equals("ReadingSceneEnable")) {
                                                                            if (!name.equals("ARActivity")) {
                                                                                if (!name.equals("ARScreenOnTimeTHMs")) {
                                                                                    if (!name.equals("ARConfidenceTH")) {
                                                                                        if (!name.equals("ARWaitTimeMs")) {
                                                                                            if (!name.equals("ARMonitorConfidenceSampleMaxNum")) {
                                                                                                if (!name.equals("ARMonitorConfidenceSampleTimeStepMs")) {
                                                                                                    if (name.equals("AwarenessEnable")) {
                                                                                                        this.mIsAwarenessEnable = Boolean.parseBoolean(parser.nextText());
                                                                                                        DElog.i(TAG, "AwarenessEnable mIsAwarenessEnable = " + this.mIsAwarenessEnable);
                                                                                                        break;
                                                                                                    }
                                                                                                } else {
                                                                                                    this.mARMonitorConfidenceSampleTimeStepMs = Integer.parseInt(parser.nextText());
                                                                                                    break;
                                                                                                }
                                                                                            } else {
                                                                                                this.mARMonitorConfidenceSampleMaxNum = Integer.parseInt(parser.nextText());
                                                                                                break;
                                                                                            }
                                                                                        } else {
                                                                                            this.mARWaitTimeMs = Integer.parseInt(parser.nextText());
                                                                                            break;
                                                                                        }
                                                                                    } else {
                                                                                        this.mARConfidenceTH = Integer.parseInt(parser.nextText());
                                                                                        break;
                                                                                    }
                                                                                } else {
                                                                                    this.mARScreenOnTimeTHMs = Integer.parseInt(parser.nextText());
                                                                                    break;
                                                                                }
                                                                            } else {
                                                                                String[] values = parser.nextText().split(",");
                                                                                if (values.length == 2) {
                                                                                    this.mARActivities.put(values[0], new ARActivity(Integer.parseInt(values[1])));
                                                                                    break;
                                                                                } else {
                                                                                    DElog.d(TAG, "getConfigFromXML find illegal param, tag name = " + name);
                                                                                    break;
                                                                                }
                                                                            }
                                                                        } else {
                                                                            this.mReadingSceneEnable = Boolean.parseBoolean(parser.nextText());
                                                                            break;
                                                                        }
                                                                    } else {
                                                                        this.mARSceneEnable = Boolean.parseBoolean(parser.nextText());
                                                                        break;
                                                                    }
                                                                } else {
                                                                    this.mPersonalizedCurveEnable = Boolean.parseBoolean(parser.nextText());
                                                                    break;
                                                                }
                                                            } else {
                                                                this.mGameBLSceneEnable = Boolean.parseBoolean(parser.nextText());
                                                                break;
                                                            }
                                                        } else {
                                                            this.mGameDESceneEnable = Boolean.parseBoolean(parser.nextText());
                                                            break;
                                                        }
                                                    } else {
                                                        String[] values2 = parser.nextText().split(",");
                                                        if (values2.length == 2) {
                                                            this.mReadingWhiteList.put(values2[0], Integer.valueOf(Integer.parseInt(values2[1])));
                                                            break;
                                                        } else {
                                                            DElog.d(TAG, "getConfigFromXML find illegal param, tag name = " + name);
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    String[] values3 = parser.nextText().split(",");
                                                    if (values3.length == 2) {
                                                        this.mTopApkBrightnessLevelWhiteList.put(values3[0], Integer.valueOf(Integer.parseInt(values3[1])));
                                                        break;
                                                    } else {
                                                        DElog.d(TAG, "getConfigFromXML find illegal param, tag name = " + name);
                                                        break;
                                                    }
                                                }
                                            } else {
                                                String[] values4 = parser.nextText().split(",");
                                                if (values4.length == 2) {
                                                    this.mGameHDRWhiteList.put(values4[0], Integer.valueOf(Integer.parseInt(values4[1])));
                                                    break;
                                                } else {
                                                    DElog.d(TAG, "getConfigFromXML find illegal param, tag name = " + name);
                                                    break;
                                                }
                                            }
                                        } else {
                                            String[] values5 = parser.nextText().split(",");
                                            if (values5.length == 2) {
                                                this.mGameBrightnessLevelWhiteList.put(values5[0], Integer.valueOf(Integer.parseInt(values5[1])));
                                                break;
                                            } else {
                                                DElog.d(TAG, "getConfigFromXML find illegal param, tag name = " + name);
                                                break;
                                            }
                                        }
                                    } else {
                                        this.mDBUserDragMaxSize = Integer.parseInt(parser.nextText());
                                        break;
                                    }
                                } else {
                                    this.mEnable = Boolean.parseBoolean(parser.nextText());
                                    break;
                                }
                            } else {
                                configGroupLoadStarted = true;
                                break;
                            }
                            break;
                        case 3:
                            if (parser.getName().equals("LABCSRControl") && configGroupLoadStarted) {
                                loadFinished = true;
                                configGroupLoadStarted = false;
                                break;
                            }
                    }
                    if (!loadFinished) {
                        eventType = parser.next();
                    }
                }
            }
            if (loadFinished) {
                DElog.i(TAG, "getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e) {
            DElog.e(TAG, "get xmlFile error: " + e);
        } catch (IOException e2) {
            DElog.e(TAG, "get xmlFile error: " + e2);
        } catch (NumberFormatException e3) {
            DElog.e(TAG, "get xmlFile error: " + e3);
        } catch (Exception e4) {
            DElog.e(TAG, "get xmlFile error: " + e4);
        }
        DElog.e(TAG, "getConfigFromeXML false!");
        return false;
    }

    public boolean isEnable() {
        return this.mEnable;
    }

    public boolean isFeatureEnable(String tag) {
        if (!this.mEnable) {
            return false;
        }
        char c = 65535;
        int hashCode = tag.hashCode();
        if (hashCode != -1166219904) {
            if (hashCode != -1081926945) {
                if (hashCode != -204695479) {
                    if (hashCode == 2145497145 && tag.equals(FeatureTag.TAG_GAME_DE_SCENE)) {
                        c = 0;
                    }
                } else if (tag.equals(FeatureTag.TAG_GAME_BRIGHTNESS_SCENE)) {
                    c = 1;
                }
            } else if (tag.equals(FeatureTag.TAG_PERSONALIZED_CURVE)) {
                c = 2;
            }
        } else if (tag.equals(FeatureTag.TAG_READING_SCENE)) {
            c = 3;
        }
        switch (c) {
            case 0:
                return this.mGameDESceneEnable;
            case 1:
                return this.mGameBLSceneEnable;
            case 2:
                return this.mPersonalizedCurveEnable;
            case 3:
                return this.mReadingSceneEnable;
            default:
                return false;
        }
    }

    private IPowerManager getPowerManagerService() throws RemoteException {
        if (this.mPowerManagerService == null && !this.mPowerManagerServiceInitialized) {
            synchronized (this.mLockService) {
                if (this.mPowerManagerService == null && !this.mPowerManagerServiceInitialized) {
                    buildBinder();
                    if (this.mPowerManagerService != null) {
                        this.mPowerManagerServiceInitialized = true;
                    }
                }
            }
        }
        if (this.mPowerManagerService != null || !this.mPowerManagerServiceInitialized) {
            return this.mPowerManagerService;
        }
        if (this.mBinderRebuildCount < 10) {
            throw new RemoteException("Try to rebuild binder " + this.mBinderRebuildCount + " times.");
        }
        throw new RemoteException("binder rebuilding failed!");
    }

    private void buildBinder() {
        IBinder binder = ServiceManager.getService("power");
        if (binder != null) {
            this.mPowerManagerService = IPowerManager.Stub.asInterface(binder);
            if (this.mPowerManagerService == null) {
                DElog.w(TAG, "service is null!");
                return;
            }
            return;
        }
        this.mPowerManagerService = null;
        DElog.w(TAG, "binder is null!");
    }

    /* access modifiers changed from: private */
    public void rebuildBinder() {
        DElog.i(TAG, "wait 800ms to rebuild binder...");
        SystemClock.sleep(800);
        DElog.i(TAG, "rebuild binder...");
        synchronized (this.mLockService) {
            buildBinder();
            if (this.mPowerManagerService != null) {
                DElog.i(TAG, "rebuild binder success.");
            } else {
                DElog.i(TAG, "rebuild binder failed!");
                this.mBinderRebuildCount++;
            }
        }
        synchronized (this.mLockBinderBuilding) {
            if (this.mBinderRebuildCount < 10) {
                this.mIsBinderBuilding = false;
            }
        }
    }

    private void rebuildBinderDelayed() {
        if (!this.mIsBinderBuilding) {
            synchronized (this.mLockBinderBuilding) {
                if (!this.mIsBinderBuilding) {
                    new Thread(new Runnable() {
                        public void run() {
                            HwBrightnessSceneRecognition.this.rebuildBinder();
                        }
                    }).start();
                    this.mIsBinderBuilding = true;
                }
            }
        }
    }

    public void initBootCompleteValues() {
        if (this.mDBManager != null) {
            this.mDBManager.setMaxSize("DragInfo", this.mDBUserDragMaxSize);
        }
        if (this.mARSceneEnable) {
            connectARService();
        }
        if (this.mIsAwarenessEnable && !this.mBrightnessAwarenessFence.initBootCompleteValues()) {
            DElog.e(TAG, "mAwarenessManager.connectService failed!");
        }
    }

    public void notifyTopApkChange(String pkgName) {
        if (pkgName == null || pkgName.length() <= 0) {
            DElog.i(TAG, "pkgName is null || pkgName.length() <= 0!");
        }
        this.mPkgName = pkgName;
        updateTopApkSceneIfNeeded(pkgName);
        updateGameSceneIfNeeded(pkgName);
        updateReadingSceneIfNeeded(pkgName);
        int topApkLevel = -1;
        if (this.mNeedNotifyGameCurveChange) {
            this.mNeedNotifyGameCurveChange = false;
            if (this.mGameBrightnessState == 21) {
                setTopApkLevelToBLControllerIfNeeded(21);
                return;
            }
            topApkLevel = 20;
        }
        if (this.mNeedNotifyPersonalizedCurveChange) {
            this.mNeedNotifyPersonalizedCurveChange = false;
            topApkLevel = this.mTopApkState;
        }
        if (topApkLevel > 0) {
            setTopApkLevelToBLControllerIfNeeded(topApkLevel);
        }
    }

    public void notifyScreenStatus(final boolean isScreenOn) {
        new Thread(new Runnable() {
            public void run() {
                boolean lastState = HwBrightnessSceneRecognition.this.mIsScreenOn;
                boolean unused = HwBrightnessSceneRecognition.this.mIsScreenOn = isScreenOn;
                if (!HwBrightnessSceneRecognition.this.mIsScreenOn && lastState) {
                    boolean unused2 = HwBrightnessSceneRecognition.this.mScreenOffStateCleanFlag = true;
                }
                long unused3 = HwBrightnessSceneRecognition.this.mScreenOnTimeMs = SystemClock.uptimeMillis();
                if (isScreenOn) {
                    HwBrightnessSceneRecognition.this.enableAR();
                } else {
                    HwBrightnessSceneRecognition.this.disableAR();
                }
            }
        }).start();
        DElog.i(TAG, "notifyScreenStatus = " + isScreenOn);
    }

    public void notifyAutoBrightnessAdj() {
        int retService;
        if (!isFeatureEnable(FeatureTag.TAG_PERSONALIZED_CURVE)) {
            DElog.i(TAG, "notifyAutoBrightnessAdj returned, isFeatureEnable(FeatureTag.TAG_PERSONALIZED_CURVE) returned false");
            return;
        }
        int userId = this.mUserId;
        Bundle data = new Bundle();
        int retService2 = -1;
        try {
            IPowerManager service = getPowerManagerService();
            if (service != null) {
                retService2 = service.hwBrightnessGetData("SceneRecognition", data);
            }
        } catch (RemoteException e) {
            DElog.e(TAG, "hwBrightnessGetData(SceneRecognition) has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
        }
        int retService3 = retService2;
        if (retService3 == 0) {
            int startBrightness = data.getInt("StartBrightness");
            int endBrightness = data.getInt("EndBrightness");
            int alLux = data.getInt("FilteredAmbientLight");
            boolean proximityPositive = data.getBoolean("ProximityPositive");
            boolean isDeltaValid = data.getBoolean("DeltaValid");
            if (startBrightness < 4 || endBrightness < 4 || startBrightness > 255) {
                int i = alLux;
                retService = endBrightness;
            } else if (endBrightness > 255) {
                int i2 = retService3;
                int i3 = alLux;
                retService = endBrightness;
            } else if (alLux < 0) {
                DElog.i(TAG, "hwBrightnessGetData return invalid alLux, alLux = " + alLux);
                return;
            } else if (!isDeltaValid) {
                DElog.i(TAG, "hwBrightnessGetData return delta invalid");
                return;
            } else {
                int i4 = alLux;
                int i5 = retService3;
                int retService4 = endBrightness;
                writeDataBaseUserDrag(userId, System.currentTimeMillis(), startBrightness, endBrightness, this.mPkgName, alLux, proximityPositive, this.mGameBrightnessState, this.mTopApkState);
                return;
            }
            DElog.i(TAG, "hwBrightnessGetData return invalid brightness, startBrightness = " + startBrightness + ", endBrightness = " + retService);
            return;
        }
        DElog.w(TAG, "hwBrightnessGetData return false");
    }

    public void notifyUserChange(int userId) {
        this.mUserId = userId;
        DElog.i(TAG, "notifyUserChange, new id = " + this.mUserId);
    }

    private void updateGameSceneIfNeeded(String pkgName) {
        if (this.mIsScreenOn) {
            boolean screenOffCleanFlag = this.mScreenOffStateCleanFlag;
            this.mScreenOffStateCleanFlag = false;
            int lastGameHDRState = this.mGameHDRState;
            int lastGameBrightnessState = this.mGameBrightnessState;
            if (pkgName == null || pkgName.length() <= 0) {
                this.mGameBrightnessState = 20;
            } else {
                if (this.mGameBrightnessLevelWhiteList.get(pkgName) != null) {
                    this.mGameBrightnessState = 21;
                    DElog.d(TAG, "apk " + pkgName + " is in the game brightness whitelist, state = " + this.mGameBrightnessState);
                } else {
                    this.mGameBrightnessState = 20;
                    DElog.d(TAG, "apk " + pkgName + " is NOT in the game brightness whitelist, state = " + this.mGameBrightnessState);
                }
                Integer value_hdr = this.mGameHDRWhiteList.get(pkgName);
                if (value_hdr != null) {
                    this.mGameHDRState = value_hdr.intValue() + 28;
                    DElog.d(TAG, "apk " + pkgName + " is in the game hdr whitelist, state = " + this.mGameHDRState);
                } else {
                    this.mGameHDRState = 28;
                    DElog.d(TAG, "apk " + pkgName + " is NOT in the game hdr whitelist, state = " + this.mGameHDRState);
                }
            }
            if (isFeatureEnable(FeatureTag.TAG_GAME_BRIGHTNESS_SCENE) && (this.mGameBrightnessState != lastGameBrightnessState || screenOffCleanFlag)) {
                this.mNeedNotifyGameCurveChange = true;
            }
            if (this.mDisplayEngineManager == null) {
                DElog.w(TAG, "mDisplayEngineManager is null !");
            } else if (isFeatureEnable(FeatureTag.TAG_GAME_DE_SCENE) && (this.mGameHDRState != lastGameHDRState || screenOffCleanFlag)) {
                if (this.mGameHDRState <= 28 || this.mGameHDRState > 30) {
                    this.mDisplayEngineManager.setScene(36, 28);
                    DElog.d(TAG, "setScene DE_SCENE_GAME DE_ACTION_TOP_GAME_OFF");
                } else {
                    this.mDisplayEngineManager.setScene(36, this.mGameHDRState);
                    DElog.d(TAG, "setScene DE_SCENE_GAME DE_ACTION_TOP_GAME: TOP " + this.mGameHDRState);
                }
            }
        }
    }

    private void updateReadingSceneIfNeeded(String pkgName) {
        if (this.mIsScreenOn) {
            int lastReadingState = this.mReadingState;
            if (pkgName != null && pkgName.length() > 0) {
                Integer num = this.mReadingWhiteList.get(pkgName);
                if (num instanceof Integer) {
                    if (num.intValue() == 1) {
                        this.mReadingState = 1;
                        DElog.d(TAG, "apk " + pkgName + " is in the reading whitelist group 1, state = " + this.mReadingState);
                    } else if (num.intValue() == 2) {
                        this.mReadingState = 0;
                        DElog.d(TAG, "apk " + pkgName + " is in the reading whitelist group 2, state = " + this.mReadingState);
                    }
                }
                if (this.mDisplayEngineManager == null) {
                    DElog.w(TAG, "mDisplayEngineManager is null !");
                } else if (isFeatureEnable(FeatureTag.TAG_READING_SCENE) && this.mReadingState != lastReadingState && this.mReadingState == 0) {
                    DElog.d(TAG, "setScene DE_SCENE_READMODE DE_ACTION_MODE_OFF");
                    this.mDisplayEngineManager.setScene(37, 17);
                }
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
                    DElog.d(TAG, "apk " + pkgName + " is in the top apk whitelist, state = " + this.mTopApkState);
                } else {
                    this.mTopApkState = 3;
                    DElog.d(TAG, "apk " + pkgName + " is NOT in the top apk whitelist, state = " + this.mTopApkState);
                }
            }
            if (!isFeatureEnable(FeatureTag.TAG_PERSONALIZED_CURVE)) {
                DElog.d(TAG, "updateTopApkSceneIfNeeded returned, isFeatureEnable(FeatureTag.TAG_PERSONALIZED_CURVE) returned false");
            } else if (this.mDBManager != null && this.mDBManager.getSize("BrightnessCurveDefault", new Bundle()) > 0) {
                this.mNeedNotifyPersonalizedCurveChange = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public void setTopApkLevelToBLControllerIfNeeded(int topApkState) {
        Bundle data = new Bundle();
        data.putInt("TopApkLevel", topApkState);
        int retService = -1;
        try {
            IPowerManager service = getPowerManagerService();
            if (service != null) {
                retService = service.hwBrightnessSetData("PersonalizedBrightnessCurveLevel", data);
                DElog.i(TAG, "setTopApkLevelToBLControllerIfNeeded, topApkState = " + topApkState);
            }
        } catch (RemoteException e) {
            DElog.e(TAG, "hwBrightnessGetData(PersonalizedBrightnessCurveLevel) has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
        }
        if (retService != 0) {
            DElog.w(TAG, "hwBrightnessGetData(PersonalizedBrightnessCurveLevel) returned " + retService);
        }
    }

    private void writeDataBaseUserDrag(int userId, long timeMillis, int startBrightness, int endBrightness, String pkgName, int alLux, boolean proximityPositive, int gameState, int topApkState) {
        DElog.d(TAG, "writeDataBaseUserDrag, userId = " + userId + ", timeMillis = " + timeMillis + ", startPoint = " + startBrightness + ", endPoint = " + endBrightness + ", pkgName = " + pkgName + ", alLux = " + alLux + ", proximityPositive = " + proximityPositive + ", topApkState = " + topApkState + ", gameState = " + gameState);
        if (this.mDBManager != null) {
            Bundle data = new Bundle();
            data.putLong("TimeStamp", System.currentTimeMillis());
            data.putInt("UserID", userId);
            data.putFloat("StartPoint", (float) startBrightness);
            data.putFloat("StopPoint", (float) endBrightness);
            data.putInt("AmbientLight", alLux);
            data.putBoolean("ProximityPositive", proximityPositive);
            data.putInt("GameState", gameState == 21 ? 1 : -1);
            data.putInt("AppType", topApkState);
            data.putString("PackageName", pkgName);
            this.mDBManager.addorUpdateRecord("DragInfo", data);
        }
    }

    public void connectARService() {
        if (this.mContext == null) {
            DElog.w(TAG, "mContext is null! connect failed.");
            return;
        }
        this.mHwActivityRecognition = new HwActivityRecognition(this.mContext);
        DElog.i(TAG, "connectARService");
        this.mHwActivityRecognition.connectService(this.hwArSink, this.mConnect);
    }

    public void disconnectARService() {
        if (this.mHwActivityRecognition != null && this.mIsARConnected) {
            this.mHwActivityRecognition.disconnectService();
            this.mIsARConnected = false;
        }
    }

    public boolean isARConnected() {
        return this.mIsARConnected;
    }

    public boolean enableAR() {
        boolean retall = true;
        if (!this.mIsARConnected || this.mHwActivityRecognition == null || this.mARActivities == null) {
            DElog.i(TAG, "enableAR failed, mIsARConnected == null || mHwActivityRecognition == null || mARActivities == null!");
        } else {
            for (String key : this.mARActivities.keySet()) {
                boolean ret = enableARActivity(key, 1);
                retall = retall && ret;
                DElog.i(TAG, "enableAR " + key + ", ret = " + ret);
            }
        }
        return retall;
    }

    private boolean enableARActivity(String activity, long reportTime) {
        if (this.mHwActivityRecognition == null) {
            return true;
        }
        long reportLatencyNs = 1000000000 * reportTime;
        boolean ret = true;
        boolean ret2 = enableActivityEvent(activity, 1, reportLatencyNs) && 1 != 0;
        if (!enableActivityEvent(activity, 2, reportLatencyNs) || !ret2) {
            ret = false;
        }
        return ret;
    }

    public boolean disableAR() {
        boolean retall = true;
        if (!this.mIsARConnected || this.mHwActivityRecognition == null || this.mARActivities == null) {
            DElog.i(TAG, "disableAR failed, mIsARConnected == null || mHwActivityRecognition == null || mARActivities == null!");
        } else {
            for (Map.Entry<String, ARActivity> entry : this.mARActivities.entrySet()) {
                String key = entry.getKey();
                boolean ret = disableARActivity(key);
                retall = retall && ret;
                entry.getValue().mStatus = 2;
                DElog.i(TAG, "disableARActivity " + key + ", ret = " + ret);
            }
        }
        return retall;
    }

    private boolean disableARActivity(String activity) {
        if (this.mHwActivityRecognition == null) {
            return true;
        }
        boolean ret = true;
        boolean ret2 = disableActivityEvent(activity, 1) && 1 != 0;
        if (!disableActivityEvent(activity, 2) || !ret2) {
            ret = false;
        }
        return ret;
    }

    /* access modifiers changed from: private */
    public void updateARStatus(String activityType, int eventType) {
        if (this.mARActivities == null) {
            DElog.i(TAG, "mARActivities == null");
            return;
        }
        ARActivity activity = this.mARActivities.get(activityType);
        if (activity != null) {
            activity.mStatus = eventType;
            DElog.i(TAG, "updateARStatus, activityType:" + activityType + " update status: " + eventType);
        } else {
            DElog.w(TAG, "updateARStatus, activity == null! activityType == " + activityType + ", eventType = " + eventType);
        }
    }

    /* access modifiers changed from: private */
    public void setARSceneToBLControllerIfNeeded() {
        for (Map.Entry<String, ARActivity> entry : this.mARActivities.entrySet()) {
            ARActivity activity = entry.getValue();
            String tag = entry.getKey();
            if (activity.mStatus == 1) {
                if (SystemClock.uptimeMillis() - this.mScreenOnTimeMs > ((long) this.mARScreenOnTimeTHMs)) {
                    ARThread aRThread = new ARThread(tag, activity, (long) this.mARWaitTimeMs, this.mARConfidenceTH);
                    aRThread.start();
                    if (this.mARMonitorConfidenceSampleMaxNum > 0) {
                        ARMonitorThread aRMonitorThread = new ARMonitorThread(entry.getKey(), (long) this.mARMonitorConfidenceSampleTimeStepMs, this.mARMonitorConfidenceSampleMaxNum);
                        aRMonitorThread.start();
                    }
                } else {
                    setARStatus(tag);
                    if (this.mARScene == this.mSceneTagMap.get("android.activity_recognition.in_vehicle").intValue() && this.mLocationScene == this.mSceneTagMap.get(SceneTag.LOCATION_HOME).intValue()) {
                        DElog.i(TAG, "skip in vehicle scene due to home status.");
                        return;
                    }
                    setTopApkLevelToBLControllerIfNeeded(activity.mAction);
                }
                return;
            }
        }
        setTopApkLevelToBLControllerIfNeeded(18);
        setARStatus("android.activity_recognition.unknown");
    }

    /* access modifiers changed from: private */
    public void sendAbnormalActivityRecognitionToMonitor(String tag, List confidenceSamples) {
        if (this.mDisplayEffectMonitor != null && confidenceSamples != null && tag != null) {
            int monitorScene = getMonitorUserScene(tag);
            if (monitorScene >= 0) {
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "userSceneMisrecognition");
                params.put("userScene", Integer.valueOf(monitorScene));
                params.put("confidence", confidenceSamples);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
            }
        }
    }

    private void sendActivityRecognitionToMonitor() {
        if (this.mDisplayEffectMonitor != null) {
            int monitorScene = 0;
            if (this.mARScene > 0) {
                monitorScene = 0 + this.mARScene;
            }
            if (this.mLocationScene > 0) {
                monitorScene += this.mLocationScene;
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

    public void setARStatus(String tag) {
        this.mARScene = getMonitorUserScene(tag);
        sendActivityRecognitionToMonitor();
    }

    public void setLocationStatus(String tag) {
        DElog.i(TAG, "setLocationStatus tag = " + tag);
        this.mLocationScene = getMonitorUserScene(tag);
        if (this.mLocationScene == this.mSceneTagMap.get(SceneTag.LOCATION_NOT_HOME).intValue() && this.mARScene == this.mSceneTagMap.get("android.activity_recognition.in_vehicle").intValue()) {
            ARActivity activity = this.mARActivities.get("android.activity_recognition.in_vehicle");
            if (activity == null) {
                DElog.i(TAG, "get(SceneTag.ACTIVITY_IN_VEHICLE) return null!");
                return;
            } else {
                setTopApkLevelToBLControllerIfNeeded(activity.mAction);
                DElog.i(TAG, "set in vehicle scene due to leaving home.");
            }
        }
        sendActivityRecognitionToMonitor();
    }

    /* access modifiers changed from: protected */
    public Object getAREventType(int type) {
        if (type == 1) {
            return "enter";
        }
        if (type == 2) {
            return "exit";
        }
        return String.valueOf(type);
    }

    public boolean enableActivityEvent(String activity, int eventType, long reportLatencyNs) {
        if (this.mHwActivityRecognition != null) {
            return this.mHwActivityRecognition.enableActivityEvent(activity, eventType, reportLatencyNs);
        }
        return false;
    }

    public boolean disableActivityEvent(String activity, int eventType) {
        if (this.mHwActivityRecognition != null) {
            return this.mHwActivityRecognition.disableActivityEvent(activity, eventType);
        }
        return false;
    }

    public void getSupportedARActivities() {
        DElog.d(TAG, "getSupportedARActivities....");
    }

    public boolean flushAR() {
        if (this.mHwActivityRecognition != null) {
            return this.mHwActivityRecognition.flush();
        }
        return false;
    }

    public HwActivityChangedExtendEvent getCurrentActivityExtend() {
        if (this.mHwActivityRecognition != null) {
            return this.mHwActivityRecognition.getCurrentActivityExtend();
        }
        return null;
    }

    public String getCurrentActivity() {
        if (this.mHwActivityRecognition != null) {
            return this.mHwActivityRecognition.getCurrentActivity();
        }
        return "android.activity_recognition.unknown";
    }

    public void setVideoPlayStatus(boolean isVideoPlay) {
        Bundle data = new Bundle();
        data.putBoolean("IsVideoPlay", isVideoPlay);
        int retService = -1;
        try {
            IPowerManager service = getPowerManagerService();
            if (service != null) {
                retService = service.hwBrightnessSetData("QRCodeBrighten", data);
                DElog.i(TAG, "QRCodeBrighten, IsVideoPlay = " + isVideoPlay);
            }
        } catch (RemoteException e) {
            DElog.e(TAG, "hwBrightnessGetData(QRCodeBrighten) has remote exception:" + e.getMessage());
            rebuildBinderDelayed();
        }
        if (retService != 0) {
            DElog.w(TAG, "hwBrightnessGetData(QRCodeBrighten) returned " + retService);
        }
    }
}
